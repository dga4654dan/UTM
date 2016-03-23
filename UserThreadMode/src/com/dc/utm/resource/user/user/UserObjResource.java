package com.dc.utm.resource.user.user;

import java.util.Set;

import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.resource.user.IUserResource;

public class UserObjResource<Visitor, UserKey, User extends IBaseUser<UserKey>>  implements IUserResource<Visitor, UserKey, User> {

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

		//这里采取一个先设置登录参数的策略，如果 登录连接检查失败 将会重新设置为logout
		//因为对于用户的消息处理是线性的，所以这里不会引起其他的问题
		user.setLogin();
		
		userCenter.addUser(userKey, user);
	}

	@Override
	public void failInLoginLinkCheck(UserKey userKey, User user) {

		//用户 登录连接检查失败，设置用户为未登录
		user.setLogout();
				
		userCenter.removeUser(userKey, user);
		
	}

	@Override
	public void userIn(UserKey userKey, User user) {
	}

	@Override
	public void userOut(UserKey userKey, User user) {
		
		//设置标志位
		user.setLogout();
		
		userCenter.removeUser(userKey, user);
	}
	
	@Override
	public int getActiveCount() {

		return userCenter.size();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set getAciveUserInfo() {

		return userCenter.keySet();
	}
	
	@Override
	public String getName() {
		return "UserObj";
	}
}
