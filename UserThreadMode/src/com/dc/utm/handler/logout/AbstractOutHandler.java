package com.dc.utm.handler.logout;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.resource.user.UserResourceCenter;
import com.dc.utm.user.flag.UserFlagBusiness;

/**
 * 
 * 用户退出登录 或者 用户断线的处理
 * 
 * @author Daemon
 *
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 * @param <Param> 参数
 */
public abstract class AbstractOutHandler<Visitor, UserKey, User extends IBaseUser<UserKey>, Param> 
	implements IRequestHandler<User, Param> {
	
	protected final UserCenter<UserKey, User> userCenter;
	
	@SuppressWarnings("rawtypes")
	protected final EventManager eventManager;
	
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	protected final UserResourceCenter<Visitor, UserKey, User> userResourceCenter;
	
	@SuppressWarnings("rawtypes")
	public AbstractOutHandler( UserCenter<UserKey, User> userCenter,
			EventManager eventManager, UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter) {
		
		this.userCenter = userCenter;
		this.eventManager = eventManager;
		this.userFlagBusiness = userFlagBusiness;
		this.userResourceCenter = userResourceCenter;
	}
	
	@Override
	public boolean isLimited(int requestId, User user, Param param) {
		
		//用户退出登录 或者 用户断线时重要的请求，这里不应该因为等待队列过长而被过滤掉
		return false;
	}

	@Override
	public void queueFull(int requestId, User user, Param param) {

		// isLimited return false, 此方法将不会被调用
	}

	@Override
	public void before(int requestId, User user, Param param) {
		
	}
	
	@Override
	public void after(int requestId, User user, Param param) {
		
	}

	@Override
	public void handlerRequest(int requestId, User user, Param param) {
		
		//判断用户是否登录，防止 用户断线或退出的 业务处理被执行多次
		if( user.isLogining() ) {
			
			try {
				
				userOut(requestId, user, param);
				
			} catch(Exception e) {
				
				eventManager.getExceptionLogger().exception(e);
			}
			
			try {
				
				userOutOver(requestId, user, param);
				
			} catch(Exception e) {
				
				eventManager.getExceptionLogger().exception(e);
			}
			
			UserKey userKey = user.getUserKey();
			
			//通知资源中心 回收用户资源
			userResourceCenter.userOut(userKey, user);
			
			try {
				
				//回收用户登录标志
				userFlagBusiness.removeLoginFlag(requestId, user, param);
				
			} catch(Exception e) {
				
				eventManager.getExceptionLogger().exception(e);
			}
			
		}
		
	}
	
	/**
	 * 用户退出登录 或者 用户断线的业务处理
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 * @param param 参数
	 */
	public abstract void userOut(int requestId, User user, Param param);
	
	/**
	 * 用户退出登录 或者 用户断线 处理结束（用来发布用户断线事件，做相应的日志之类的）
	 * （注意：此时用户的相关资源还没有被回收，该处理完后将会回收这些资源）
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 * @param param 参数
	 */
	public abstract void userOutOver(int requestId, User user, Param param);
}




