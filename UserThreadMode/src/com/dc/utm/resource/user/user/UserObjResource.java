package com.dc.utm.resource.user.user;

import java.util.Set;

import com.dc.utm.center.UserCenter;
import com.dc.utm.resource.user.IUserResource;

public class UserObjResource<Visitor, UserKey, User>  implements IUserResource<Visitor, UserKey, User> {

	protected final UserCenter<UserKey, User> userCenter;
	
	public UserObjResource(UserCenter<UserKey, User> userCenter) {
		
		this.userCenter = userCenter;
	}
	
	@Override
	public void beforeUserLoginCheck(Visitor visitor, Object param) {
	}

	@Override
	public void userLoginCheckFail(Visitor visitor, Object param) {
	}

	@Override
	public void setLoginFlagSuccess(UserKey userKey, User user) {
	}

	@Override
	public void waitUserLogoutTimeOut(UserKey userKey, User user) {
	}

	@Override
	public void beforeLoginLinkCheck(UserKey userKey, User user) {

		userCenter.addUser(userKey, user);
	}

	@Override
	public void failInLoginLinkCheck(UserKey userKey, User user) {

		userCenter.removeUser(userKey, user);
	}

	@Override
	public void userIn(UserKey userKey, User user) {
	}

	@Override
	public void userOut(UserKey userKey, User user) {
		
		userCenter.removeUser(userKey, user);
	}
	
	@Override
	public int getActiveCount() {

		return userCenter.size();
	}

	@Override
	public Set<UserKey> getAciveUserId() {

		return userCenter.keySet();
	}
	
	
}
