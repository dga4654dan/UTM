package com.dc.utm.handler;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.center.DataCenter;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.user.flag.UserFlagBusiness;

public abstract class OnUserLoginHandler<Visitor, UserKey, User extends IBaseUser<UserKey>, Param> 
	implements IRequestHandler<User, Param> {
	
	protected final UserCenter<UserKey, User> userCenter;
	
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public OnUserLoginHandler( DataCenter dataCenter ) {
		
		this.userCenter = dataCenter.userCenter;
		
		this.userFlagBusiness = dataCenter.userFlagBusiness;
		
		dataCenter.onUserLoginHandler = this;
	}

	@Override
	public boolean isLimited(int requestId, User user, Param param) {

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

	@Override
	public void handlerRequest(int requestId, User user, Param param) {
		
		if( loginLinkCheck(requestId, user, param) ) {
			
			user.setLogin();
			userCenter.addUser(user.getUserKey(), user);
			
			userLogin(requestId, user, param);
			
		} else {
			
			userFlagBusiness.rollBackLoginFlagWhenLinkDisable(requestId, user, param);
		}
		
	}
	
	public abstract boolean loginLinkCheck(int requestId, User user, Param param);
	
	/**
	 * 
	 * 用户登录处理逻辑
	 * 
	 * 这个方法被调用, 意味着 如果用户断线, 则OnUserDisconectHandler必定被触发
	 * 
	 * @param requestId
	 * @param user
	 * @param param
	 */
	public abstract void userLogin(int requestId, User user, Param param);
}

