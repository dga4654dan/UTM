package com.dc.utm.handler;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.center.DataCenter;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.user.flag.UserFlagBusiness;

public abstract class OnUserDisconectHandler<Visitor, UserKey, User extends IBaseUser<UserKey>, Param> 
	implements IRequestHandler<User, Param> {

	protected final UserCenter<UserKey, User> userCenter;
	
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public OnUserDisconectHandler( DataCenter dataCenter ) {
		
		this.userCenter = dataCenter.userCenter;
		this.userFlagBusiness = dataCenter.userFlagBusiness;
	}
	
	@Override
	public boolean isLimited(int requestId, User user, Param param) {
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
		
		if( user.isLogining() ) {
			
			userDisconect(requestId, user, param);
			
			userFlagBusiness.removeLoginFlag(requestId, user, param);
			
			userCenter.removeUser(user.getUserKey(), user);
			user.setLogout();
		}
		
	}
	
	public abstract void userDisconect(int requestId, User user, Param param);
	
}
