package com.dc.utm.center;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 用户中心，存放所有在线用户的信息
 * 
 * @author Daemon
 *
 * @param <UserKey> 用户Id
 * @param <User> 用户对象
 */
public class UserCenter<UserKey, User> {

	protected final ConcurrentHashMap<UserKey, User> userMap = new ConcurrentHashMap<UserKey, User>();
	
	/**
	 * 根据用户id获得用户对象
	 * 
	 * @param key 用户Id
	 * @return 用户对象
	 */
	public User getUser( UserKey key ) {
		
		return userMap.get(key);
	}
	
	/**
	 * 
	 * 添加用户
	 * 
	 * @param key 用户Id
	 * @param user
	 * @return 用户对象
	 */
	public User addUser( UserKey key, User user ) {
		
		return userMap.put(key, user);
	}
	
	/**
	 * 
	 * 移除用户
	 * 
	 * @param key 用户Id
	 * @param user
	 * @return 用户对象
	 */
	public boolean removeUser( UserKey key, User user ) {
		
		return userMap.remove(key, user);
	}

	/**
	 * @return 在线人数
	 */
	public int size() {
		
		return userMap.size();
	}
	
	/**
	 * @return 所有用户id
	 */
	public Set<UserKey> keySet() {
		
		return userMap.keySet();
	}
	
	/**
	 * @return 在线用户列表
	 */
	public ArrayList<User> getUserList() {
		
		return new ArrayList<User>(userMap.values());
	}
	
}
