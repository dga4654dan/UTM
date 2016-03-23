package com.dc.utm.handler.login;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.resource.user.UserResourceCenter;
import com.dc.utm.resource.user.queue.UserQueueResource;
import com.dc.utm.user.flag.UserFlagBusiness;

/**
 * 用户登录处理
 * 用户名密码等已经经过校验，（如果有已经登录的用户也已经退出了），
 * 该类主要做一些登录系统成功后的消息返回 和 其他相关消息推送等 和 一些业务处理
 * （该处理线程是utm的线程池中的线程）
 * 
 * @author Daemon
 * 
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 * @param <Param> 请求对象
 */
public abstract class OnUserLoginHandler<Visitor, UserKey, User extends IBaseUser<UserKey>, Param> 
	implements IRequestHandler<User, Param> {
	
	protected final UserCenter<UserKey, User> userCenter;
	
	@SuppressWarnings("rawtypes")
	protected final EventManager eventManager;
	
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	protected final UserQueueResource<Visitor, UserKey, User> userQueueResource;
	
	protected final UserResourceCenter<Visitor, UserKey, User> userResourceCenter;
	
	@SuppressWarnings("rawtypes")
	public OnUserLoginHandler( UserCenter<UserKey, User> userCenter,
			EventManager eventManager, UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			UserQueueResource<Visitor, UserKey, User> userQueueResource,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter ) {
		
		this.userCenter = userCenter;
		
		this.eventManager = eventManager;
		
		this.userFlagBusiness = userFlagBusiness;
		this.userQueueResource = userQueueResource;
		this.userResourceCenter = userResourceCenter;
	}

	@Override
	public boolean isLimited(int requestId, User user, Param param) {

		//登录检查的过程已经处理了，后续登录不受等待队列长度限制
		return false;
	}

	@Override
	public void queueFull(int requestId, User user, Param param){
		
		// isLimited return false, 此方法将不会被调用
	}

	@Override
	public void before(int requestId, User user, Param param) {
		
	}
	
	@Override
	public void after(int requestId, User user, Param param) {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handlerRequest(int requestId, User user, Param param) {

		UserKey userKey = user.getUserKey();
		
		//登录连接检查是否失败
		boolean loginLinkCheckError = true;
		try {
			
			//通知资源中心
			userResourceCenter.beforeLoginLinkCheck(userKey, user);
			
			if( loginLinkCheck(requestId, user, param) ) {
				
				//通知资源中心
				userResourceCenter.userIn(userKey, user);
				
				loginLinkCheckError = false;
				
				try {
					
					userLogin(requestId, user, param);
					//触发用户登录事件
					eventManager.getUserEventManager().userLogin(userKey, user);
					
				} catch(Exception e) {
					
					eventManager.getExceptionLogger().exception(e);
				}
				
			}
			
		} catch(Exception e) {
			
			loginLinkCheckError = true;
			eventManager.getExceptionLogger().exception(e);
		}
		
		if( loginLinkCheckError ) {
			
			//通知资源中心 回收用户资源
			userResourceCenter.failInLoginLinkCheck(userKey, user);
			
			try {
				
				//回收用户标志位信息
				userFlagBusiness.rollBackLoginFlagWhenLinkDisable(requestId, user, param);
				
			} catch(Exception e) {
				
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}
	
	/**
	 * 登录成功连接检查
	 * （登录检查等过程已经过去，现在再次确认下连接是否还在，
	 * 如果连接已经断开了，那么将会触发回收处理(userResource.failInLoginLinkCheck 和 serFlagBusiness.rollBackLoginFlagWhenLinkDisable )）
	 * 关于为什么要有loginLinkCheck问题：
	 * 有些socket的封装框架给的不是说一个socket断开了的事件或者回调，而是在当用户登录后才会有用户的断线事件，
	 * 设置这个loginLinkCheck方法就是为了处理这样的问题，在loginLinkCheck里面可以调用这些封装框架的登录事件，
	 * 如果登录失败（用户已经断开了），那么就认为是在登录处理过程中用户断线了，也视为没有登录成功，将会回收相应的资源和标志位，
	 * 如果登录成功，则接下来用户如果断线了，那么Disconect事件就会被触发=>OnUserDisconectHandler必定被触发
	 * 
	 * 这个方法如果返回true， 则接下来将会执行userLogin方法，
	 * 而且用户断线OnUserDisconectHandler必定被触发，且一定在userLogin方法之后（断线事件会被放到qtm队列中，qtm保证一个用户的任务被顺序执行）
	 * 
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 * @param param 请求参数
	 * @return 登录成功连接检查是否成功（用户是否依然连接着）
	 */
	public abstract boolean loginLinkCheck(int requestId, User user, Param param);
	
	/**
	 * 用户登录处理逻辑
	 * 
	 * 这个方法被调用, 意味着 如果用户断线, 则OnUserDisconectHandler必定被触发
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 * @param param 请求参数
	 */
	public abstract void userLogin(int requestId, User user, Param param);
}

