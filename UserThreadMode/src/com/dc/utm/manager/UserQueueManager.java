package com.dc.utm.manager;

import java.util.List;
import java.util.concurrent.locks.Lock;

import com.dc.qtm.IAbandonQueueInvoke;
import com.dc.qtm.Task;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ExecutorResult;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.qtm.queue.QueueRuningInfo;
import com.dc.qtm.queue.TaskInterruptInfo;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.resource.user.queue.UserQueueResource;

/**
 * 用户队列管理，提供挂起、恢复用户队列等方法（请谨慎使用）
 * 
 * @author Daemon
 *
 * @param <Visitor> 游客对象
 * @param <UserKey> 用户id
 * @param <User> 用户对象
 */
public class UserQueueManager<Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	protected final UserQueueResource<Visitor, UserKey, User> userQueueResource;
	
	public UserQueueManager(
			UserQueueResource<Visitor, UserKey, User> userQueueResource) {
		this.userQueueResource = userQueueResource;
	}

	/**
	 * 
	 * 获得 任务队列 的操作锁( 除非熟悉实现,不然不建议这么操作该锁  )
	 * 
	 * eg:
	 * 业务需求: 同时添加两个任务,不能分离
	 * 执行细节: 1.获得锁后  2.添加任务A  3.添加任务B   4.释放锁
	 * 
	 * eg: 
	 * 业务需求: 在设置用户登录标志时,不允许logout入列,如果成功则login的业务必须入列
	 *     (为避免情况: 设置标志位后,其他业务认为其已登录,所以可以logout它,
	 *     而logout可能会重置了这个标志位,然后再执行login,则会出现 login了,但是标志位被清除了)
	 * 执行细节: 1.获得锁后  2.设置一个重要的标志位  3.设置成功后,紧接着添加一个任务A进去队列   4.释放锁
	 * 
	 * @Object userKey 用户的id
	 * @return 获得 任务队列 的操作锁，找不到该用户的任务队列则返回空
	 */
	public Lock getQueueOperateLock(UserKey userKey) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.getQueueOperateLock();
	}

	/**
	 * 
	 * 添加并执行任务
	 * 
	 * @Object requestId 请求的id
	 * @Object User 实体信息
	 * @Object handler 具体处理器
	 * @Object object 请求参数
	 * @return 执行结果 （ 成功 / 队列被抛弃，可能需要从新获取 / 异常 / 找不到该用户的任务队列则返回空）
	 */
	public ExecutorResult execTask( int requestId, User user, IRequestHandler<User, Object> handler, Object object ) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(user.getUserKey());
		if( queue == null )
			return null;
		else
			return queue.execTask(requestId, user, handler, object);
	}
	
	/**
	 * 
	 * 挂起队列
	 * 
	 * @Object userKey 用户的id
	 * @return 执行结果 （ 成功 / 队列被抛弃，可能需要从新获取 / 异常 / 找不到该用户的任务队列则返回空 ）
	 */
	public ExecutorResult holdQueue(UserKey userKey) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.holdQueue();
	}
	
	/**
	 * 
	 * 恢复队列
	 * 
	 * @Object userKey 用户的id
	 * @return 执行结果 （ 成功 / 队列被抛弃，可能需要从新获取 / 异常 / 找不到该用户的任务队列则返回空 ）
	 */
	public ExecutorResult resumeQueue(UserKey userKey) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.resumeQueue();
	}
	
	/**
	 * 
	 * 中断当前正在执行的任务（调用 thread.interrupt，只能中断一些等待之类的操作，但是如果是一个一直运行的程序是无法被中断的）
	 * 
	 * @Object userKey 用户的id
	 * @Object tagetId 中断的任务的id
	 * @return 被中断的任务的信息 / 找不到该用户的任务队列则返回空
	 */
	public TaskInterruptInfo<Object> interruptTaskNow( UserKey userKey, int tagetId ) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.interruptTaskNow(tagetId);
	}
	
	/**
	 * 
	 * 要求回收队列
	 * 
	 * @Object userKey 用户的id
	 * @Object abandonQueueInvoke 队列为空的时候，的回调函数
	 * @return 执行结果 （ 成功 / 队列被抛弃，可能需要从新获取 / 异常 / 找不到该用户的任务队列则返回空 ）
	 */
	public ExecutorResult requireAbandon( UserKey userKey, IAbandonQueueInvoke<UserKey> abandonQueueInvoke ) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.requireAbandon(abandonQueueInvoke);
	}
	
	/**
	 * 
	 * 将除了现在运行的任务 tagetIdNow 之外的任务都移除掉
	 * （用于紧急处理，由于某一个handler陷入死循环或者时间很长的循环的情况，将返回的任务放到一个新的队列中执行，
	 * 如果是被阻塞在io，应考虑用interruptTaskNow）
	 * 
	 * @Object userKey 用户的id
	 * @Object tagetIdNow 现在运行的任务Id（如果现在运行的任务非这个，则会返回null，且不会执行清除动作）
	 * @return 除了现在运行的任务 tagetIdNow 之外的任务（如果现在运行的任务非tagetIdNow，则会返回null，且不会执行清除动作） / 找不到该用户的任务队列则返回空
	 */
	public List< Task<User, UserKey, Object> > clearWaitTaskList(UserKey userKey, int tagetIdNow) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.clearWaitTaskList(tagetIdNow);
	}
	
	/**
	 * 
	 * 将除了现在运行的任务之外的任务都移除掉
	 * （用于紧急处理，由于某一个handler陷入死循环或者时间很长的循环的情况，将返回的任务放到一个新的队列中执行，
	 * 如果是被阻塞在io，应考虑用interruptTaskNow）
	 * 
	 * @Object userKey 用户的id
	 * @return 除了现在运行的任务 tagetIdNow 之外的任务 / 找不到该用户的任务队列则返回空
	 */
	public List<Task<User, UserKey, Object>> clearWaitTaskList(UserKey userKey) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.clearWaitTaskList();
	}
	
	/**
	 * 
	 * 获得队列的信息
	 * 
	 * @Object userKey 用户的id
	 * @return 队列的信息 / 找不到该用户的任务队列则返回空
	 */
	public QueueRuningInfo<UserKey, Object> getQueueRuningInfo( UserKey userKey, boolean needThreadTrackInfo ) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.getQueueRuningInfo(needThreadTrackInfo);
	}

	/**
	 * 
	 * 队列是否被要求挂起(holdQueue)
	 * 
	 * @Object userKey 用户的id
	 * @return 队列是否被要求挂起 / 找不到该用户的任务队列则返回空
	 */
	public Boolean isRequireHold(UserKey userKey) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.isRequireHold();
	}

	/**
	 * 
	 * 队列是否被要求回收
	 * 
	 * @Object userKey 用户的id
	 * @return 队列是否被要求回收(requireAbandon) / 找不到该用户的任务队列则返回空
	 */
	public Boolean isRequireAbandon(UserKey userKey) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.isRequireAbandon();
	}

	/**
	 * 线程开始执行当前任务的时间，没有被执行时=0
	 * 
	 * @Object userKey 用户的id
	 * @return 线程开始执行当前任务的时间，没有被执行时=0 / 找不到该用户的任务队列则返回空
	 */
	public Long getThreadStartTime(UserKey userKey) {
		
		ITaskQueue<User, UserKey, Object> queue = userQueueResource.getUserKeyMapQueue().get(userKey);
		if( queue == null )
			return null;
		else
			return queue.getThreadStartTime();
	}
}
