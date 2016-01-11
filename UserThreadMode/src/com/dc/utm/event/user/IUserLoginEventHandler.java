package com.dc.utm.event.user;

/**
 * 
 * 用户登录事件
 * 
 * @author Daemon
 *
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public interface IUserLoginEventHandler<UserKey, User> {

	/**
	 * 用户登录
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void userLogin(UserKey userKey, User user);
}
