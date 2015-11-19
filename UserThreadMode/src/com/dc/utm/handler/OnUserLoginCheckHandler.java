package com.dc.utm.handler;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.utm.center.DataCenter;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.filter.user.IUserRequestFilter;
import com.dc.utm.user.flag.UserFlagBusiness;
import com.dc.utm.user.logout.UserLogoutCheckBusiness;
import com.dc.utm.user.queue.UserTaskQueueBusiness;

public abstract class OnUserLoginCheckHandler<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>, Param> 
	implements IRequestHandler<Visitor, Param> {

	protected final UserCenter<UserKey, User> userCenter;
	protected final UserTaskQueueBusiness<UserKey, User> userTaskQueueBusiness;
	protected final IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, IBaseUser<UserKey>> userRequestFilter;
	
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	protected final UserLogoutCheckBusiness<CmdType, ConnectKey, Visitor, UserKey, IBaseUser<UserKey>> userLogoutCheckBusiness;
	
	protected final IRequestHandler<IBaseUser<UserKey>, Object> onUserLoginHandler;
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public OnUserLoginCheckHandler( DataCenter dataCenter ) {
		
		this.userCenter = dataCenter.userCenter;
		this.userTaskQueueBusiness = dataCenter.userTaskQueueBusiness;
		this.userRequestFilter = dataCenter.userRequestFilter;
		this.userFlagBusiness = dataCenter.userFlagBusiness;
		this.userLogoutCheckBusiness = dataCenter.userLogoutCheckBusiness;
		this.onUserLoginHandler = dataCenter.onUserLoginHandler;
		
	}
	
	@Override
	public boolean isLimited(int requestId, Visitor visitor, Param param) {

		return true;
	}

	@Override
	public abstract void queueFull(int requestId, Visitor visitor, Param param);
	
	@Override
	public void before(int requestId, Visitor visitor, Param param) {
	}
	
	@Override
	public void after(int requestId, Visitor visitor, Param param) {
	}

	@Override
	public void handlerRequest(int requestId, Visitor visitor, Param param) {
		
		User user = loginCheck(requestId, visitor, param);
		if( user != null ) {
			
			UserKey userKey = user.getUserKey();
			
			ITaskQueue<User, UserKey, Object> taskQueue = userTaskQueueBusiness.getUserTaskQueue(userKey);
			taskQueue.getQueueOperateLock().lock();
			try {
				
				switch( userFlagBusiness.setLoginFlag(requestId, visitor, user, param) ) {
				
				case SUCCESS:
					
					//处理login任务(将login加入到queue中)
					userRequestFilter.doFilter(userKey, user, onUserLoginHandler, requestId, param);
					break;
					
				case FAIL_LOGIN_LOCAL:
					
					logoutLocalUserWhenUserReLogin(requestId, visitor, user, param);
					userLogoutCheckBusiness.loginWhenOldUserLogout(requestId, visitor, user, param);
					break;
					
				case FAIL_LOGIN_OTHER:
					
					logoutRemoteUserWhenUserReLogin(requestId, visitor, user, param);
					userLogoutCheckBusiness.loginWhenOldUserLogout(requestId, visitor, user, param);
					break;
				
				default:
					break;
					
				}
				
			} finally {
				
				taskQueue.getQueueOperateLock().unlock();
			}
			
		}
		
	}
	
	/**
	 * 
	 * 检查用户的账号 密码是否正确 等检查
	 * 
	 * 
	 * @param param 登录的参数
	 * @return 如果登录的用户符合规定,则返回 对应的 User实体, 否则返回空
	 */
	public abstract User loginCheck(int requestId, Visitor visitor, Param param);
	
	public abstract void logoutLocalUserWhenUserReLogin(int requestId, Visitor visitor, User newUser, Param param);
	
	public abstract void logoutRemoteUserWhenUserReLogin(int requestId, Visitor visitor, User newUser, Param param);
}
