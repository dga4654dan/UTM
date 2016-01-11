package com.dc.utm.filter.user;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.entity.IBaseUser;

/**
 * 
 * 用户请求过滤器接口
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public interface IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {

	/**
	 * 用户请求过滤，在过滤方法内 如果没有问题 要执行handler方法处理请求（一般建议调用handler线程池处理 "userQueueResource.execTask" ）
	 * （doFilter这个方法是由调用线程直接执行的，不是handler线程池中线程执行的，所以这里不建议执行一些有阻塞的方法或复杂的处理）
	 * 
	 * @param key 用户Id
	 * @param user 用户
	 * @param handler 对应的处理类
	 * @param requestId 请求Id
	 * @param param 请求参数
	 */
	@SuppressWarnings("rawtypes")
	void doFilter( UserKey key, User user, IRequestHandler handler, int requestId, Object param );
	
}
