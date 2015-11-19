package com.dc.utm.filter.visitor;

import com.dc.qtm.IExceptionListener;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;

public class VisitorRequestFilterNotQueue<CmdType, ConnectKey, Visitor> 
	implements IVisitorRequestFilter<CmdType, ConnectKey, Visitor> {

	protected final LimitedUnboundedThreadPoolExecutor pool;
	protected final IExceptionListener exceptionListener;
	
	public VisitorRequestFilterNotQueue( LimitedUnboundedThreadPoolExecutor pool,
			IExceptionListener exceptionListener ) {
		
		this.pool = pool;
		this.exceptionListener = exceptionListener;
		
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
					exceptionListener.exception(e);
				}
				
				try {
					
					handler.handlerRequest(requestId, visitor, param);
					
				} catch (Exception e) {
					exceptionListener.exception(e);
				}

				try {
					
					handler.after(requestId, visitor, param);
					
				} catch (Exception e) {
					exceptionListener.exception(e);
				}
				
			} catch (Exception e) {
				
				exceptionListener.exception(e);
				
			}
			
		}
		
	}
}
