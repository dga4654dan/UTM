package com.dc.utm.event.user;

/**
 * 
 * 用户断线事件
 * ("用户退出" 和 "用户断线" 只有一个会被调用，用户退出后再断线时不会触发"用户断线"的，因为他已经不是用户了 )
 * 
 * @author Daemon
 *
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public interface IUserDisconnectEventHandler<UserKey, User> {

	/**
	 * 用户断线
	 * ("用户退出" 和 "用户断线" 只有一个会被调用，用户退出后再断线时不会触发"用户断线"的，因为他已经不是用户了 )
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void userDisconnect(UserKey userKey, User user);
}
