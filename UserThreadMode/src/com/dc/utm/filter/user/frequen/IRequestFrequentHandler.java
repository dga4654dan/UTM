package com.dc.utm.filter.user.frequen;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.entity.IBaseUser;

/**
 * 用户请求过于频繁处理接口
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public interface IRequestFrequentHandler<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {

	/**
	 * 用户请求过于频繁
	 * 
	 * @param key 用户Id
	 * @param user 用户
	 * @param handler 请求的处理类(cmd对应的处理类)
	 * @param requestId 请求Id
	 * @param param 请求参数
	 */
	@SuppressWarnings("rawtypes")
	void requestFrequent(UserKey key, User user,
			IRequestHandler handler, int requestId, Object param);
}
