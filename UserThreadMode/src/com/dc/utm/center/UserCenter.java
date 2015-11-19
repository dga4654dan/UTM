package com.dc.utm.center;

import java.util.concurrent.ConcurrentHashMap;

public class UserCenter<UserKey, User> {

	protected final ConcurrentHashMap<UserKey, User> userMap = new ConcurrentHashMap<UserKey, User>();
	
	public User getUser( UserKey key ) {
		
		return userMap.get(key);
	}
	
	public User addUser( UserKey key, User user ) {
		
		return userMap.put(key, user);
	}
	
	public User removeUser( UserKey key, User user ) {
		
		return userMap.remove(user);
	}
	
}
