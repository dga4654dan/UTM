package com.dc.utm.filter.visitor;

import com.dc.qtm.handle.IRequestHandler;

/**
 * 
 * 游客请求过滤器接口
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 */
public interface IVisitorRequestFilter<CmdType, ConnectKey, Visitor>  {

	/**
	 * 游客请求过滤，在过滤方法内 如果没有问题 要执行handler方法处理请求（一般建议调用线程池处理 ）
	 * （doFilter这个方法是由调用线程直接执行的，不是线程池中线程执行的，所以这里不建议执行一些有阻塞的方法或复杂的处理）
	 * 
	 * @param connectKey 游客Id
	 * @param visitor 游客
	 * @param handler 对应的处理类
	 * @param requestId 请求Id
	 * @param param 请求的参数
	 */
	void doFilter( ConnectKey connectKey, Visitor visitor, IRequestHandler<Visitor, Object> handler, int requestId, Object param );
	
}
