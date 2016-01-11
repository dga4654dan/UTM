package com.dc.utm.resource.user;

/**
 * 用户资源基本事件接口
 * 
 * 定义的是 一个用户生命周期重要的时间点
 * 
 * 各个时间点逻辑顺序：
 * 1. beforeUserLoginCheck -> 2.1 setLoginFlagSuccess   -> 3.1 beforeLoginLinkCheck -> 4.1 userIn               -> 5. userOut
 *                         -> 2.2 waitUserLogoutTimeOut                             -> 4.2 failInLoginLinkCheck
 *                         -> 2.3 userLoginCheckFail
 * 
 * 
 * @author Daemon
 *
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public interface IUserResourceEvent<Visitor, UserKey, User> {

	/**
	 * 游客申请登录（在onUserLoginCheck.before中被调用）
	 * 
	 * @param visitor 游客
	 * @param param 请求参数
	 */
	void beforeUserLoginCheck(Visitor visitor, Object param);
	
	/**
	 * 登录失败（用户名 密码等信息不正确（onUserLoginCheck.loginCheck返回null））
	 * 
	 * @param visitor 游客
	 * @param param 请求参数
	 */
	void userLoginCheckFail(Visitor visitor, Object param);
	
	/**
	 * 设置用户登录标志位成功（UserFlagBusiness.setLoginFlag返回成功）
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void setLoginFlagSuccess(UserKey userKey, User user);
	
	/**
	 * 等待老用户退出超时（详见UserLogoutCheckBusiness）
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void waitUserLogoutTimeOut(UserKey userKey, User user);
	
	/**
	 * 准备 "登录连接检查" （OnUserLoginHandler.loginLinkCheck前）
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void beforeLoginLinkCheck(UserKey userKey, User user);
	
	/**
	 *  "登录连接检查"失败，用户已经断线（OnUserLoginHandler.loginLinkCheck返回false）
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void failInLoginLinkCheck(UserKey userKey, User user);

	/**
	 * 用户登录成功
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void userIn(UserKey userKey, User user);
	
	/**
	 * 用户退出或断线
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	void userOut(UserKey userKey, User user);
}
