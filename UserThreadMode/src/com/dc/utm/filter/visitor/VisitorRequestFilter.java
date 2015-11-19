package com.dc.utm.filter.visitor;

import java.util.concurrent.ConcurrentHashMap;

import com.dc.qtm.IExceptionListener;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ExecutorResult;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.qtm.queue.lock.LockTaskQueue;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;

public class VisitorRequestFilter<CmdType, ConnectKey, Visitor> implements IVisitorRequestFilter<CmdType, ConnectKey, Visitor> {

	protected final LimitedUnboundedThreadPoolExecutor pool;
	protected final IExceptionListener exceptionListener;
	
	protected final ConcurrentHashMap<ConnectKey, ITaskQueue<Visitor, ConnectKey, Object>> connectKeyMapQueue 
		= new ConcurrentHashMap<ConnectKey, ITaskQueue<Visitor, ConnectKey, Object>>();
	
	public VisitorRequestFilter( LimitedUnboundedThreadPoolExecutor pool,
			IExceptionListener exceptionListener ) {
		
		this.pool = pool;
		this.exceptionListener = exceptionListener;
		
	}
	
	@Override
	public void doFilter(ConnectKey connectKey, Visitor visitor,
			IRequestHandler<Visitor, Object> handler, int requestId,
			Object param) {
		
		execTask(connectKey, visitor, handler, requestId, param);
	}

	
	protected ITaskQueue<Visitor, ConnectKey, Object> getUserTaskQueue( ConnectKey key ) {
		
		ITaskQueue<Visitor, ConnectKey, Object> queue = connectKeyMapQueue.get(key);
		if( queue == null ) {
			
			queue = new LockTaskQueue<Visitor, ConnectKey, Object>(key, pool, exceptionListener);
			ITaskQueue<Visitor, ConnectKey, Object> oldQueue = connectKeyMapQueue.putIfAbsent(key, queue);
			if( oldQueue != null )
				queue = oldQueue;
			
		}
		
		return queue;
		
	}
	
	@SuppressWarnings("unchecked")
	protected void execTask( ConnectKey key, Visitor visitor, IRequestHandler<Visitor, ?> handler, int requestId, Object param ) {
		
		for(;;) {
			
			ITaskQueue<Visitor, ConnectKey, Object> queue = getUserTaskQueue(key);
			ExecutorResult executorResult = queue.execTask(requestId, visitor, (IRequestHandler<Visitor, Object>)handler, param);
			
			//if QUEUE_ABANDON, retry again
			if( executorResult != ExecutorResult.QUEUE_ABANDON ) //SUCCESS, EXCEPTION
				break;
			
		}
		
	}
}
