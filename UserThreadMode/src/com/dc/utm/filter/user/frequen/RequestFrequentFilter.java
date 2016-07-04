package com.dc.utm.filter.user.frequen;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.filter.user.UserRequestFilter;
import com.dc.utm.resource.user.IUserResource;
import com.dc.utm.resource.user.queue.UserQueueResource;

/**
 * 过滤掉请求过于频繁的用户的请求
 * 注：用户在checkTime毫秒内最多允许maxCount次请求，每隔checkTime毫秒清理一次统计；
 *        该处理并非严格符合（在checkTime毫秒内最多允许maxCount次请求），建议将maxCount设置的比预想值大1或2
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd类型
 * @param <ConnectKey> 连接id
 * @param <Visitor> 游客对象
 * @param <UserKey> 用户id
 * @param <User> 用户对象
 */
public class RequestFrequentFilter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>>
		extends UserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User>
		implements IUserResource<Visitor, UserKey, User> {

	protected final int checkTime;
	protected final int maxCount;
	protected final EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager;

	protected final ConcurrentHashMap<UserKey, AtomicInteger> userKeyMapRequestCounter = new ConcurrentHashMap<UserKey, AtomicInteger>();

	public RequestFrequentFilter(
			int checkTime,
			int maxCount,
			EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager,
			UserQueueResource<Visitor, UserKey, User> userQueueResource) {

		super(userQueueResource);

		this.checkTime = checkTime;
		this.maxCount = maxCount;
		this.eventManager = eventManager;

		Thread restCounterThread = new Thread(new RestCounterThread());
		restCounterThread.setName("-RequestFrequentFilter-RestCounterThread-");
		restCounterThread.start();
	}

	class RestCounterThread implements Runnable {

		@Override
		public void run() {

			long sleepTime;
			long timeBegin = System.currentTimeMillis();
			int checkTime = RequestFrequentFilter.this.checkTime;
			for (;;) {

				try {

					sleepTime = timeBegin - System.currentTimeMillis() + checkTime; // sleepTime = checkTime - (System.currentTimeMillis() - timeBegin)
					if (sleepTime > 0)
						TimeUnit.MILLISECONDS.sleep(sleepTime);

					timeBegin = System.currentTimeMillis();
					for (AtomicInteger atomicInteger : userKeyMapRequestCounter
							.values())
						atomicInteger.set(0);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doFilter(UserKey key, User user, IRequestHandler handler,
			int requestId, Object param) {

		AtomicInteger userRequestCounter = getUserRequestCounter(key);
		if (userRequestCounter == null) {

			if (handler instanceof com.dc.utm.handler.logout.OnUserDisconectHandler
					&& !user.isLogining()) {

				// 没有登录则认为用户已经断开连接或者退出，不予处理
				// （eg：用户 "退出登录" 后 "断线"，程序先将"退出登录"放入任务队列中，然后再将"断线"放入任务队列中，
				// 则断线处理有可能找不到“用户请求信息记录”，因为可能在退出的处理线程中已经将它回收）

				// System.out.println(user.getUserKey() + ":" +
				// handler.getClass().getSimpleName() +
				// " abandon when user logouted");
				return;
			}

			// 给用户创建 “用户请求信息记录”，并提示可能资源管理有问题

			eventManager.getExceptionLogger().erroState(
							"160107_79 can't find userRequestCounter for user:"
									+ key
									+ "(mean userRequestCounter reasource has problem), create one for now, userId:"
									+ user.getUserKey() + " userIsLogin:"
									+ user.isLogining() + " user:" + user
									+ " handler:" + handler + " param:" + param);

			AtomicInteger old = userKeyMapRequestCounter.putIfAbsent(key,
					new AtomicInteger(0));
			if (old != null)
				userRequestCounter = old;
		}

		// 判断时候超出限制（不受限制的处理器将不会判断，因为"不受限制"意味着这是一个重要的请求（用户退出 断线等） 不能过滤掉）
		if (handler.isLimited(requestId, user, param)
				&& userRequestCounter.incrementAndGet() > maxCount) {

			// 超出该限制则 调用回调方法通知 程序，并返回（该请求将不会被执行）
			try {
				this.requestFrequent(key, user, handler, requestId, param);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}

			System.out.println(key + " requestFrequent");

			return;

		}

		// 没有超出限制则执行该请求
		userQueueResource.execTask(key, user, handler, requestId, param);

	}

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
	public void requestFrequent(UserKey key, User user,
			IRequestHandler handler, int requestId, Object param) {

		eventManager.getRequestFrequentHandler().requestFrequent(key, user,
				handler, requestId, param);
	}

	/**
	 * 创建用户的 “用户请求信息记录”
	 * 
	 * @param key 用户Id
	 */
	protected void createUserKeyMapRequestCounter(UserKey key) {

		if (userKeyMapRequestCounter.putIfAbsent(key, new AtomicInteger(0)) != null) {

			/*
			 * 该资源已经存在，意味着资源可能没有被正确的回收
			 */
			eventManager
					.getExceptionLogger()
					.erroState(
							"160107_142 has userRequestCounter before create(mean userRequestCounter reasource has problem), user:"
									+ key);
		}
	}

	/**
	 * 获得用户的 “用户请求信息记录”
	 * 
	 * @param key 用户Id
	 * @return 用户请求信息记录
	 */
	protected AtomicInteger getUserRequestCounter(UserKey key) {

		return userKeyMapRequestCounter.get(key);
	}

	/**
	 * 移除用户的 “用户请求信息记录”
	 * 
	 * @param key 用户Id
	 */
	protected void removeUserRequestCounter(UserKey key) {

		userKeyMapRequestCounter.remove(key);
	}
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public void beforeUserLoginCheck(Visitor visitor, Object param) {
	}

	@Override
	public void userLoginCheckFail(Visitor visitor, Object param) {
	}

	@Override
	public void setLoginFlagSuccess(UserKey userKey, User user) {

		/*
		 * 当用户登录成功后，生成对象 并放入Map中
		 */
		createUserKeyMapRequestCounter(userKey);

	}

	@Override
	public void waitUserLogoutTimeOut(UserKey userKey, User user) {
	}

	@Override
	public void beforeLoginLinkCheck(UserKey userKey, User user) {
	}

	@Override
	public void failInLoginLinkCheck(UserKey userKey, User user) {

		/*
		 * 用户登录过程中失败，移除对象
		 */
		removeUserRequestCounter(userKey);
	}

	@Override
	public void userIn(UserKey userKey, User user) {
	}

	@Override
	public void userOut(UserKey userKey, User user) {

		/*
		 * 用户离开，移除对象
		 */
		removeUserRequestCounter(userKey);
	}

	@Override
	public int getActiveCount() {
		return userKeyMapRequestCounter.size();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set getAciveUserInfo() {

		return userKeyMapRequestCounter.keySet();
	}

	@Override
	public String getName() {
		return "UserRequestCounter";
	}

}
