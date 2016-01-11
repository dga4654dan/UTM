package com.dc.utm.filter.visitor;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;
import com.dc.utm.event.EventManager;

/**
 * 
 * 基本的游客请求过滤器，没有进行什么处理，直接调用线程池去处理该请求
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 */
public class VisitorRequestFilterNotQueue<CmdType, ConnectKey, Visitor> 
	implements IVisitorRequestFilter<CmdType, ConnectKey, Visitor> {

	protected final LimitedUnboundedThreadPoolExecutor pool;
	@SuppressWarnings("rawtypes")
	protected final EventManager eventManage;
	
	@SuppressWarnings("rawtypes")
	public VisitorRequestFilterNotQueue( LimitedUnboundedThreadPoolExecutor pool,
			EventManager eventManage ) {
		
		this.pool = pool;
		this.eventManage = eventManage;
		
	}
	
	@Override
	public void doFilter(ConnectKey connectKey, Visitor visitor,
			IRequestHandler<Visitor, Object> handler, int requestId,
			Object param) {
		
		if( handler.isLimited(requestId, visitor, param) ) {
			
			if( ! pool.executeLimited( new VisitorRunnable(connectKey, visitor, handler, requestId, param) ) ) {
				
				// 队列满
				handler.queueFull(requestId, visitor, param);
				
			}
			
		} else {
			
			pool.executeUnbounded( new VisitorRunnable(connectKey, visitor, handler, requestId, param) );
		}
		
	}
	
	/**
	 * 处理游客请求的线程的执行类
	 * 
	 * @author Daemon
	 *
	 */
	class VisitorRunnable implements Runnable {
		
		final ConnectKey connectKey;
		final Visitor visitor;
		final IRequestHandler<Visitor, Object> handler;
		final int requestId;
		final Object param;

		
		public VisitorRunnable(ConnectKey connectKey, Visitor visitor,
				IRequestHandler<Visitor, Object> handler, int requestId,
				Object param) {

			this.connectKey = connectKey;
			this.visitor = visitor;
			this.handler = handler;
			this.requestId = requestId;
			this.param = param;
		}


		@Override
		public void run() {
			
			try {
				
				try {
					
					handler.before(requestId, visitor, param);
					
				} catch (Exception e) {
					eventManage.getExceptionLogger().exception(e);
				}
				
				try {
					
					handler.handlerRequest(requestId, visitor, param);
					
				} catch (Exception e) {
					eventManage.getExceptionLogger().exception(e);
				}

				try {
					
					handler.after(requestId, visitor, param);
					
				} catch (Exception e) {
					eventManage.getExceptionLogger().exception(e);
				}
				
			} catch (Exception e) {
				
				eventManage.getExceptionLogger().exception(e);
				
			}
			
		}
		
	}
}
