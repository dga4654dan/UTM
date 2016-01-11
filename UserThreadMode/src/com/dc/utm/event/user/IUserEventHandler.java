package com.dc.utm.event.user;


/**
 * 
 * 用户事件
 * 
 * @author Daemon
 *
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public interface IUserEventHandler<UserKey, User> {
	
	/**
	 * 用户登录
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void userLogin(UserKey userKey, User user);
	
	/**
	 * 用户退出
	 * ("用户退出" 和 "用户断线" 只有一个会被调用，用户退出后再断线时不会触发"用户断线"的，因为他已经不是用户了 )
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void userLogout(UserKey userKey, User user);
	
	/**
	 * 用户断线
	 * ("用户退出" 和 "用户断线" 只有一个会被调用，用户退出后再断线时不会触发"用户断线"的，因为他已经不是用户了 )
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void userDisconnect(UserKey userKey, User user);
	
}
