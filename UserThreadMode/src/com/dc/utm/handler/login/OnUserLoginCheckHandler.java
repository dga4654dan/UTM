package com.dc.utm.handler.login;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.filter.user.IUserRequestFilter;
import com.dc.utm.resource.user.UserResourceCenter;
import com.dc.utm.resource.user.queue.UserQueueResource;
import com.dc.utm.user.flag.UserFlagBusiness;
import com.dc.utm.user.logout.UserLogoutCheckBusiness;

/**
 * 
 * 用户登录检查
 * 该类为抽象类，需要实现其中的一些方法
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 * @param <Param> 请求参数
 */
public abstract class OnUserLoginCheckHandler<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>, Param> 
	implements IRequestHandler<Visitor, Param> {

	protected final UserCenter<UserKey, User> userCenter;
	
	@SuppressWarnings("rawtypes")
	protected final EventManager eventManager;
	
	protected final UserQueueResource<Visitor, UserKey, User> userQueueResource;
	protected final IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter;
	
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	protected final UserLogoutCheckBusiness<CmdType, ConnectKey, Visitor, UserKey, User> userLogoutCheckBusiness;
	
	@SuppressWarnings("rawtypes")
	protected final IRequestHandler onUserLoginHandler;
	
	protected final UserResourceCenter<Visitor, UserKey, User> userResourceCenter;
	
	@SuppressWarnings("rawtypes")
	public OnUserLoginCheckHandler( UserCenter<UserKey, User> userCenter, 
			EventManager eventManager, UserQueueResource<Visitor, UserKey, User> userQueueResource,
			IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter,
			UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			UserLogoutCheckBusiness<CmdType, ConnectKey, Visitor, UserKey, User> userLogoutCheckBusiness,
			IRequestHandler onUserLoginHandler,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter ) {
		
		this.userCenter = userCenter;
		this.eventManager = eventManager;
		this.userQueueResource = userQueueResource;
		this.userRequestFilter = userRequestFilter;
		this.userFlagBusiness = userFlagBusiness;
		this.userLogoutCheckBusiness = userLogoutCheckBusiness;
		this.onUserLoginHandler = onUserLoginHandler;
		this.userResourceCenter = userResourceCenter;
	}
	
	@Override
	public boolean isLimited(int requestId, Visitor visitor, Param param) {
		
		//受到等待队列限制，如果等待队列过长，则会触发调用queueFull方法，并不会处理这次登录请求
		return true;
	}

	@Override
	public abstract void queueFull(int requestId, Visitor visitor, Param param);
	
	@Override
	public void before(int requestId, Visitor visitor, Param param) {
		
		userResourceCenter.beforeUserLoginCheck(visitor, param);
	}
	
	@Override
	public void after(int requestId, Visitor visitor, Param param) {
	}

	@Override
	public void handlerRequest(int requestId, Visitor visitor, Param param) {
		
		User user = null;
		try {
			
			user = loginCheck(requestId, visitor, param);
			
		} catch(Exception e) {
			
			eventManager.getExceptionLogger().exception(e);
		}
		
		if( user != null ) {
			
			//user != null，用户密码等信息验证成功
			
			UserKey userKey = user.getUserKey();
			
			try {
				
				//检查用户是否已经登录
				switch( userFlagBusiness.setLoginFlag(requestId, visitor, user, param) ) {
				
				//用户未在其他地方登录，且 登录标志（LoginFlag）设置成功
				case SUCCESS:
					
					//通知资源管理器
					userResourceCenter.setLoginFlagSuccess(userKey, user);
					
					//调用utm调用onUserLoginHandler处理登录事件(调用线程池处理)
					ITaskQueue<User, UserKey, Object> taskQueue = userQueueResource.getUserTaskQueue(userKey);
					taskQueue.getQueueOperateLock().lock();
					try {
						
						userRequestFilter.doFilter(userKey, user, onUserLoginHandler, requestId, param);
						
					} finally {
						taskQueue.getQueueOperateLock().unlock();
					}
					
					break;
					
				//用户在本地登录
				case FAIL_LOGIN_LOCAL:
					
					//将用户信息放入等待退出的用户队列中（该队列会检查用户是否已经退出了，退出了就触发=>调用utm调用onUserLoginHandler处理登录事件(调用线程池处理)
					// 如果长时间没有退出则会触发=>UserLogoutCheckBusiness.waitLogoutTimeOut）
					try {
						
						logoutLocalUserWhenUserReLogin(requestId, visitor, user, param);
						
					} catch(Exception e) {
						
						eventManager.getExceptionLogger().exception(e);
					}
					
					userLogoutCheckBusiness.loginWhenOldUserLogout(requestId, visitor, user, param);
					break;
				
				//用户在其他服务上登录了
				case FAIL_LOGIN_OTHER:
					
					//将用户信息放入等待退出的用户队列中（该队列会检查用户是否已经退出了，退出了就触发=>调用utm调用onUserLoginHandler处理登录事件(调用线程池处理)
					// 如果长时间没有退出则会触发=>UserLogoutCheckBusiness.waitLogoutTimeOut）
					try {
						
						logoutRemoteUserWhenUserReLogin(requestId, visitor, user, param);
						
					} catch(Exception e) {
						
						eventManager.getExceptionLogger().exception(e);
					}
					
					userLogoutCheckBusiness.loginWhenOldUserLogout(requestId, visitor, user, param);
					break;
					
				default:
					break;
					
				}
				
			}  catch(Exception e) {
				
				eventManager.getExceptionLogger().exception(e);
				
			}
			
		} else {
			
			//user == null，用户密码等信息验证失败
			
			//通知资源管理器，具体资源实现类回收已经申请的资源
			userResourceCenter.userLoginCheckFail(visitor, param);
		}
		
	}
	
	/**
	 * 检查用户的账号 密码是否正确 等检查
	 * （如果正确，则会继续出发登录操作（执行onUserLogin或者UserLogoutCheckBusiness.waitLogoutTimeOut（用户在其他机器上登录且在一定时间内并未退出）），
	 * 所以在方法里一般不需要给用户发送什么信息，
	 * 如果错误，那么一般在该方法内就要给用户发送错误的信息，因为return null，就不会触发流程继续走下去了）
	 * 
	 * @param requestId 请求的Id
	 * @param visitor 游客(要登录变成用户的游客对象)
	 * @param param 登录的参数
	 * @return 如果登录的用户符合规定,则返回 对应的 User实体（为即将登录的用户生成的对象）, 否则返回空
	 */
	public abstract User loginCheck(int requestId, Visitor visitor, Param param);
	
	/**
	 * 用户登录，但是发现他已经在本服务上登录了，需要退出原来的客户（一般是告诉原来的用户 你的账号在其他地方登录了，然后再断开 ）
	 * 
	 * @param requestId 请求的Id
	 * @param visitor 游客(要登录变成用户的游客对象)
	 * @param newUser 为即将登录的用户生成的对象
	 * @param param 登录的参数
	 */
	public abstract void logoutLocalUserWhenUserReLogin(int requestId, Visitor visitor, User newUser, Param param);
	
	/**
	 * 用户登录，但是发现他已经在 其他 服务上登录了，需要退出原来的客户（一般是发通知给 那一个服务，告诉原来的用户 你的账号在其他地方登录了，然后再断开 ）
	 * 
	 * @param requestId 请求的Id
	 * @param visitor 游客(要登录变成用户的游客对象)
	 * @param newUser 为即将登录的用户生成的对象
	 * @param param 登录的参数
	 */
	public abstract void logoutRemoteUserWhenUserReLogin(int requestId, Visitor visitor, User newUser, Param param);
	
	
}





