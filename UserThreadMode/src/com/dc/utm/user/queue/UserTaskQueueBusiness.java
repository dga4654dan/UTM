package com.dc.utm.user.queue;

import java.util.concurrent.ConcurrentHashMap;

import com.dc.qtm.IExceptionListener;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ExecutorResult;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.qtm.queue.lock.LockTaskQueue;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;
import com.dc.utm.entity.IBaseUser;

public class UserTaskQueueBusiness<UserKey, User extends IBaseUser<UserKey>> {
	
	protected final LimitedUnboundedThreadPoolExecutor pool;
	protected final IExceptionListener exceptionListener;
	
	protected final ConcurrentHashMap<UserKey, ITaskQueue<User, UserKey, Object>> userKeyMapQueue 
		= new ConcurrentHashMap<UserKey, ITaskQueue<User, UserKey, Object>>();
	
	public UserTaskQueueBusiness(
			LimitedUnboundedThreadPoolExecutor pool,
			IExceptionListener exceptionListener ) {
		
		this.pool = pool;
		this.exceptionListener = exceptionListener;
		
	}

	public ITaskQueue<User, UserKey, Object> getUserTaskQueue( UserKey key ) {
		
		ITaskQueue<User, UserKey, Object> queue = userKeyMapQueue.get(key);
		if( queue == null ) {
			
			queue = new LockTaskQueue<User, UserKey, Object>(key, pool, exceptionListener);
			ITaskQueue<User, UserKey, Object> oldQueue = userKeyMapQueue.putIfAbsent(key, queue);
			if( oldQueue != null )
				queue = oldQueue;
			
		}
		
		return queue;
		
	}
	
	@SuppressWarnings("unchecked")
	public void execTask( UserKey key, User user, IRequestHandler<User, ?> handler, 
			int requestId, Object param ) {
		
		for(;;) {
			
			ITaskQueue<User, UserKey, Object> queue = getUserTaskQueue(key);
			ExecutorResult executorResult = queue.execTask(requestId, user, (IRequestHandler<User, Object>)handler, param);
			
			//if QUEUE_ABANDON, retry again
			if( executorResult != ExecutorResult.QUEUE_ABANDON ) //SUCCESS, EXCEPTION
				break;
			
		}
		
	}
	
}
