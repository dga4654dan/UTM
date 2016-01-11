package com.dc.utm.entity;

/**
 * 用户接口，定义用户的一些基本方法
 * 
 * @author Daemon
 *
 * @param <UserKey> 用户Id
 */
public interface IBaseUser<UserKey> {
	
	/**
	 * @return 返回该用户的Id
	 */
	UserKey getUserKey();

	/**
	 * @return 用户是否登录
	 */
	boolean isLogining();
	
	/**
	 * 用户登录
	 */
	void setLogin();
	
	/**
	 * 用户注销
	 */
	void setLogout();
	
	/**
	 * 将用户断开链接
	 */
	void killConnect();
	
}
