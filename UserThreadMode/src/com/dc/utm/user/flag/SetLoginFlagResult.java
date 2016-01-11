package com.dc.utm.user.flag;

/**
 * 
 * 设置用户登录标志位结果
 * 
 * @author Daemon
 * 
 */
public enum SetLoginFlagResult {
	
	/**
	 * 成功
	 */
	SUCCESS,
	
	/**
	 * 失败，用户在本服务上登录了
	 */
	FAIL_LOGIN_LOCAL,
	
	/**
	 * 失败，用户在其他服务上登录了
	 */
	FAIL_LOGIN_OTHER
}
