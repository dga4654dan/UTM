package com.dc.utm.event.cmd.exception;

/**
 * 
 * 用户或游客客 请求了没有注册的cmd的处理接口
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public interface IRequestUnknowCmdHandler<CmdType, ConnectKey, Visitor, UserKey, User> {

	/**
	 * 游客请求了没有注册的cmd
	 * 
	 * @param connectKey 游客Id
	 * @param visitor 游客
	 * @param cmd cmd
	 * @param param 请求参数
	 */
	void visitorRequestUnknowCmd( ConnectKey connectKey, Visitor visitor, CmdType cmd, Object param );
	
	/**
	 * 用户请求了没有注册的cmd
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 * @param cmd cmd
	 * @param param 请求参数
	 */
	void userRequestUnknowCmd( UserKey userKey, User user, CmdType cmd, Object param );
}
