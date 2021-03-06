package com.dc.utm.resource.user.queue;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dc.qtm.IAbandonQueueInvoke;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ExecutorResult;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.qtm.queue.lock.LockTaskQueue;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.resource.user.IUserResource;

public class UserQueueResource<Visitor, UserKey, User extends IBaseUser<UserKey>> implements IUserResource<Visitor, UserKey, User> {
	
	protected final LimitedUnboundedThreadPoolExecutor pool;
	@SuppressWarnings("rawtypes")
	protected final EventManager eventManager;
	
	protected final ConcurrentHashMap<UserKey, ITaskQueue<User, UserKey, Object>> userKeyMapQueue 
		= new ConcurrentHashMap<UserKey, ITaskQueue<User, UserKey, Object>>();
	
	protected final IAbandonQueueInvoke<UserKey> abandonQueueInvoke = new IAbandonQueueInvoke<UserKey>() {

		@Override
		public boolean queueEmptyNowSureToAbandon(UserKey queueInfo) {
			return true;
		}
		
	};
	
	@SuppressWarnings("rawtypes")
	public UserQueueResource(
			LimitedUnboundedThreadPoolExecutor pool,
			EventManager eventManager ) {
		
		this.pool = pool;
		this.eventManager = eventManager;
		
	}
	
	public HashMap<UserKey, ITaskQueue<User, UserKey, Object>> getUserKeyMapQueue_Image() {
		
		return new HashMap<UserKey, ITaskQueue<User,UserKey,Object>>( userKeyMapQueue );
	}
	
	public ConcurrentHashMap<UserKey, ITaskQueue<User, UserKey, Object>> getUserKeyMapQueue() {
		
		return userKeyMapQueue;
	}
	
	protected String getTheadTrackInfo() {
		
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		StringBuilder builder = new StringBuilder( elements.length << 5 );
		for( StackTraceElement element : elements )
			builder.append(element).append("\n");
		
		return builder.toString();
	}

	public ITaskQueue<User, UserKey, Object> getUserTaskQueue( UserKey key ) {
		
		ITaskQueue<User, UserKey, Object> queue = userKeyMapQueue.get(key);
		return queue;
		
	}
	
	public void abandonTaskQueueWhenLogout( UserKey key, ITaskQueue<User, UserKey, Object> taskQueue ) {
		
		userKeyMapQueue.remove(key, taskQueue);
		
		taskQueue.holdQueue();
		taskQueue.clearWaitTaskList();
		taskQueue.requireAbandon( abandonQueueInvoke );
	}
	
	/**
	 * @param key
	 * @param user
	 * @param handler
	 * @param requestId
	 * @param param
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void execTask( UserKey key, User user, IRequestHandler handler, 
			int requestId, Object param ) {
		
		for(;;) {
			
			ITaskQueue<User, UserKey, Object> queue = getUserTaskQueue(key);
			if( queue == null ) {
				//不存在队列资源
				
				if( user.isLogining() ) {
					
					//如果用户是登录状态，则给他创建队列，并提示可能资源管理有问题
					
					eventManager.getExceptionLogger().erroState("151227_91 can't find task queeue for user:" 
							+ key + "(mean queue reasource has problem), create one for now\n" + getTheadTrackInfo());
					
					queue = new LockTaskQueue<User, UserKey, Object>(key, pool, eventManager.getExceptionLogger());
					ITaskQueue<User, UserKey, Object> oldQueue = userKeyMapQueue.putIfAbsent(key, queue);
					if( oldQueue != null )
						queue = oldQueue;
					
				} else {
					
					//没有登录则认为用户已经断开连接或者退出，不予处理
					//（eg：用户 "退出登录" 后   "断线"，程序先将"退出登录"放入任务队列中，然后再将"断线"放入任务队列中，
					//则断线处理有可能找不到队列，因为可能在退出的处理线程中已经将它回收）
					
//					System.out.println(user.getUserKey() + ":" + handler.getClass().getSimpleName() + " abandon when user logouted");
					break;
				}
				
			}
			
			ExecutorResult executorResult = queue.execTask(requestId, user, (IRequestHandler<User, Object>)handler, param);
			
			//if QUEUE_ABANDON, retry again
			if( executorResult != ExecutorResult.QUEUE_ABANDON ) //SUCCESS, EXCEPTION
				break;
			
		}
		
	}
	
	protected void createUserQueue(UserKey key) {
		
		ITaskQueue<User, UserKey, Object> queue = new LockTaskQueue<User, UserKey, Object>(key, pool, eventManager.getExceptionLogger());
		if( userKeyMapQueue.putIfAbsent(key, queue) != null ) {
			
			eventManager.getExceptionLogger().erroState("151227_125 has queue before create(mean queue reasource has problem), user:" + key + "\n"  + getTheadTrackInfo());
		}
	}
	
	
	
	
	@Override
	public void beforeUserLoginCheck(Visitor visitor, Object param) {
	}

	@Override
	public void userLoginCheckFail(Visitor visitor, Object param) {
	}

	@Override
	public void setLoginFlagSuccess(UserKey userKey, User user) {
		
		createUserQueue(userKey);
	}

	@Override
	public void waitUserLogoutTimeOut(UserKey userKey, User user) {
	}

	@Override
	public void beforeLoginLinkCheck(UserKey userKey, User user) {
	}

	@Override
	public void failInLoginLinkCheck(UserKey userKey, User user) {
		
		ITaskQueue<User, UserKey, Object> taskQueue = getUserTaskQueue(userKey);
		taskQueue.getQueueOperateLock().lock();
		try {
			
			abandonTaskQueueWhenLogout(userKey, taskQueue);
			
		} finally {
			
			taskQueue.getQueueOperateLock().unlock();
		}
		
	}

	@Override
	public void userIn(UserKey userKey, User user) {
	}

	@Override
	public void userOut(UserKey userKey, User user) {
		
		ITaskQueue<User, UserKey, Object> taskQueue = getUserTaskQueue(userKey);
		taskQueue.getQueueOperateLock().lock();
		try {
			
			abandonTaskQueueWhenLogout(userKey, taskQueue);
			
		} finally {
			
			taskQueue.getQueueOperateLock().unlock();
		}
	}

	@Override
	public int getActiveCount() {
		return userKeyMapQueue.size();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set getAciveUserInfo() {
		return userKeyMapQueue.keySet();
	}

	@Override
	public String getName() {
		return "UserQueue";
	}
	
}
