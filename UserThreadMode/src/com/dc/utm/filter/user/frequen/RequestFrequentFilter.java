package com.dc.utm.filter.user.frequen;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.filter.user.UserRequestFilter;
import com.dc.utm.resource.user.IUserResource;
import com.dc.utm.resource.user.queue.UserQueueResource;

/**
 * 
 * 过滤掉请求过于频繁的用户的请求
 * ( minGap毫秒内超过ringSize个请求，而且违反次数超过illegalMax次，则会认定请求过于频繁  )
 * 
 * minGap毫秒内超过ringSize个请求：
 * RingLongRecord 会记录用户最近的ringSize个请求，如果新的请求和 RingLongRecord记录的最早一个请求之间的时间差少于minGap毫秒，则认为超出限制，将会记录到违规次数
 * eg：  假设 minGap=2000，ringSize=30
 * 下面分别是请求次数和时间：
 *    1      2       3            30       31       32      33
 * ( 3点整, 3点1s, 3点1.1s, .... 3点1.4s,  3点1.5s, 3点2.5s, 3点3.5s )
 * ==>> 第31个请求 和 第1个请求相差1.5秒，不够2秒，用户的违规次数+1
 * ==>> 第32个请求 和 第2个请求相差1.5秒，不够2秒，用户的违规次数+1
 * ==>> 第33个请求 和 第3个请求相差2.4秒，超过2秒，属于正常请求频率
 * 
 * 参数illegalMax说明：即使在用户违反上面的规定后，会增加他的违规次数，如果违规次数没有超过illegalMax，依旧允许他执行
 * ( 用户退出后，他的记录会回收，下次登录他的违规次数为0 )
 * 
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public class RequestFrequentFilter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> 
	extends UserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> 
	implements IUserResource<Visitor, UserKey, User> {

	private final long minGap;
	private final int illegalMax;
	private final int ringSize;
	
	protected final EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager;
	
	protected final ConcurrentHashMap<UserKey, UserRequestFrequentInfo> userKeyMapRequestInfo
		= new ConcurrentHashMap<UserKey, UserRequestFrequentInfo>();
	
	public RequestFrequentFilter( long minGap, int ringSize, int illegalMax,
			EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager,
			UserQueueResource<Visitor, UserKey, User> userQueueResource) {
		
		super(userQueueResource);
		
		this.minGap = minGap;
		this.illegalMax = illegalMax;
		this.ringSize = ringSize;
		this.eventManager = eventManager;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doFilter(UserKey key, User user,
			IRequestHandler handler, int requestId, Object param) {
		
		UserRequestFrequentInfo userRequestFrequentInfo = getUserRequestFrequentInfo(key);
		if( userRequestFrequentInfo == null ) {
			
			if( handler instanceof com.dc.utm.handler.logout.OnUserDisconectHandler && !user.isLogining() ) {
				
				//没有登录则认为用户已经断开连接或者退出，不予处理
				//（eg：用户 "退出登录" 后   "断线"，程序先将"退出登录"放入任务队列中，然后再将"断线"放入任务队列中，
				//则断线处理有可能找不到“用户请求信息记录”，因为可能在退出的处理线程中已经将它回收）
				
//				System.out.println(user.getUserKey() + ":" + handler.getClass().getSimpleName() + " abandon when user logouted");
				return;
			}
			
			//给用户创建 “用户请求信息记录”，并提示可能资源管理有问题
			
			eventManager.getExceptionLogger().erroState("160107_79 can't find userRequestFrequentInfo for user:" + key 
					+ "(mean userRequestFrequentInfo reasource has problem), create one for now, userId:" + user.getUserKey() 
					+ " userIsLogin:" + user.isLogining() + " user:" + user + " handler:" + handler + " param:" + param);
			
			userRequestFrequentInfo = new UserRequestFrequentInfo( new RingLongRecord(ringSize) );
			UserRequestFrequentInfo old = userKeyMapRequestInfo.putIfAbsent(key, userRequestFrequentInfo);
			if( old != null )
				userRequestFrequentInfo = old;
		}
		
		long timeNow = System.currentTimeMillis();
		
		//判断时候超出限制（不受限制的处理器将不会判断，因为"不受限制"意味着这是一个重要的请求（用户退出  断线等） 不能过滤掉）
		if ( handler.isLimited(requestId, user, param)
				&& ! userRequestFrequentInfo.ringRecord.setFirstRecordNCompare(timeNow - minGap, timeNow) ) {

			int illgalCount = userRequestFrequentInfo.illgalCount.incrementAndGet();
			
			if( illgalCount > illegalMax ) {
				
				//超出该限制则 调用回调方法通知 程序，并返回（该请求将不会被执行）
				
				try {
					
					this.requestFrequent(key, user, handler, requestId, param);
					
				} catch(Exception e) {
					
					eventManager.getExceptionLogger().exception(e);
				}
				
				return;
				
			}
			
		}
		
		//没有超出限制则执行该请求
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
		
		eventManager.getRequestFrequentHandler().requestFrequent(key, user, handler, requestId, param);
	}

	
	
	
	
	
	
	
	
	/**
	 * 创建用户的 “用户请求信息记录”
	 * 
	 * @param key 用户Id
	 */
	protected void createUserRequestFrequentInfo(UserKey key) {
		
		UserRequestFrequentInfo userRequestFrequentInfo = new UserRequestFrequentInfo( new RingLongRecord(ringSize) );
		if( userKeyMapRequestInfo.putIfAbsent(key, userRequestFrequentInfo) != null ) {
			
			/*
			 * 该资源已经存在，意味着资源可能没有被正确的回收
			 */
			eventManager.getExceptionLogger().erroState(
					"160107_142 has userRequestFrequentInfo before create(mean userRequestFrequentInfo reasource has problem), user:" + key);
		}
	}
	
	/**
	 * 获得用户的 “用户请求信息记录”
	 * 
	 * @param key 用户Id
	 * @return 用户请求信息记录
	 */
	protected UserRequestFrequentInfo getUserRequestFrequentInfo(UserKey key) {
		
		UserRequestFrequentInfo userRequestFrequentInfo = userKeyMapRequestInfo.get(key);
		return userRequestFrequentInfo;
	}
	
	/**
	 * 移除用户的 “用户请求信息记录”
	 * 
	 * @param key 用户Id
	 */
	protected void removeUserRequestFrequentInfo(UserKey key) {
		
		userKeyMapRequestInfo.remove(key);
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
		createUserRequestFrequentInfo(userKey);
		
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
		removeUserRequestFrequentInfo(userKey);
	}

	@Override
	public void userIn(UserKey userKey, User user) {
	}

	@Override
	public void userOut(UserKey userKey, User user) {
		
		/*
		 * 用户离开，移除对象
		 */
		removeUserRequestFrequentInfo(userKey);
	}

	@Override
	public int getActiveCount() {
		return userKeyMapRequestInfo.size();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set getAciveUserInfo() {
		
		return userKeyMapRequestInfo.keySet();
	}

	@Override
	public String getName() {
		return "UserRequestInfo";
	}
	
}
