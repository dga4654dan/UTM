package com.dc.utm;

import java.util.Map;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.filter.UserThreadModeFilter;
import com.dc.utm.filter.user.IUserRequestFilter;
import com.dc.utm.filter.user.frequen.RequestFrequentFilter;
import com.dc.utm.filter.visitor.IVisitorRequestFilter;
import com.dc.utm.filter.visitor.VisitorRequestFilterNotQueue;
import com.dc.utm.manager.UserManager;
import com.dc.utm.manager.UserQueueManager;
import com.dc.utm.resource.user.UserResourceCenter;
import com.dc.utm.resource.user.UserResourceManager;
import com.dc.utm.user.flag.UserFlagBusiness;

/**
 * 
 * UTM主类
 * 
 * 负责初始化utm的各个组件
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd的类型
 * @param <ConnectKey> 游客Id类型
 * @param <Visitor> 游客对象类型
 * @param <UserKey> 玩家Id类型
 * @param <User> 玩家对象类型
 */
public class UserThreadMode<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	protected final CmdType login;
	protected final CmdType disconect;
	
	protected final EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager;
	
	@SuppressWarnings("rawtypes")
	protected final Map<CmdType, IRequestHandler> visitorCmdMapHandler;
	@SuppressWarnings("rawtypes")
	protected final Map<CmdType, IRequestHandler> userCmdMapHandler;
	
	protected final UserCenter<UserKey, User> userCenter;

	protected final IVisitorRequestFilter<CmdType, ConnectKey, Visitor> visitorRequestFilter;
	protected final IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter;
	
	protected final UserResourceManager<ConnectKey, Visitor, UserKey, User> userResourceManager;
	
	protected final UserThreadModeFilter<CmdType, ConnectKey, Visitor, UserKey, User> userThreadModeFilter;
	
	protected final UserManager<CmdType, ConnectKey, Visitor, UserKey, User> userManager;
	protected final UserQueueManager<Visitor, UserKey, User> userQueueManager;
	
	/**
	 * 
	 * UTM最基本的构造方法，传入所有的组件
	 * 
	 * 
	 * @param login login Cmd
	 * @param disconect disconect Cmd
	 * @param eventManager 事件管理器
	 * @param visitorCmdMapHandler 游客Cmd对应处理器的Map
	 * @param userCmdMapHandler 用户Cmd对应处理器的Map
	 * @param userCenter 用户中心（存放用户）
	 * @param visitorRequestFilter 游客请求过滤器
	 * @param userRequestFilter 用户请求过滤器
	 * @param userResourceManager 用户资源管理器
	 * @param userThreadModeFilter UTM过滤器
	 * @param userManager 用户管理器
	 * @param userQueueManager 用户队列管理器
	 */
	@SuppressWarnings("rawtypes")
	public UserThreadMode(
			CmdType login,
			CmdType disconect,
			EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager,
			Map<CmdType, IRequestHandler> visitorCmdMapHandler,
			Map<CmdType, IRequestHandler> userCmdMapHandler,
			UserCenter<UserKey, User> userCenter,
			IVisitorRequestFilter<CmdType, ConnectKey, Visitor> visitorRequestFilter,
			IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter,
			UserResourceManager<ConnectKey, Visitor, UserKey, User> userResourceManager,
			UserThreadModeFilter<CmdType, ConnectKey, Visitor, UserKey, User> userThreadModeFilter,
			UserManager<CmdType, ConnectKey, Visitor, UserKey, User> userManager,
			UserQueueManager<Visitor, UserKey, User> userQueueManager ) {
		
		super();
		this.login = login;
		this.disconect = disconect;
		this.eventManager = eventManager;
		this.visitorCmdMapHandler = visitorCmdMapHandler;
		this.userCmdMapHandler = userCmdMapHandler;
		this.userCenter = userCenter;
		this.visitorRequestFilter = visitorRequestFilter;
		this.userRequestFilter = userRequestFilter;
		this.userResourceManager = userResourceManager;
		this.userThreadModeFilter = userThreadModeFilter;
		this.userManager = userManager;
		this.userQueueManager = userQueueManager;
	}
	
	/**
	 * 
	 * 自动设置以下属性：
	 * 
	 * visitorRequestFilter使用默认实现VisitorRequestFilterNotQueue
	 * userResourceCenter使用默认实现UserResourceCenter
	 * userResourceManager使用默认实现UserResourceManager
	 * userRequestFilter使用默认实现RequestFrequentFilter
	 * userThreadModeFilter使用默认实现UserThreadModeFilter
	 * userManager使用默认实现UserManager
	 * userQueueManager使用默认实现UserQueueManager
	 * 
	 * 
	 * @param login login Cmd
	 * @param disconect disconect Cmd
	 * @param visitorCmdMapHandler 游客Cmd对应处理器的Map
	 * @param userCmdMapHandler 用户Cmd对应处理器的Map
	 * @param pool 线程池
	 * @param userCenter 用户中心（存放用户）
	 * @param eventManager 事件管理器
	 * @param checkTime 用户在checkTime毫秒内最多允许maxCount次请求(详见RequestFrequentFilter类描述)
	 * @param maxCount 用户在checkTime毫秒内最多允许maxCount次请求(详见RequestFrequentFilter类描述)
	 */
	@SuppressWarnings("rawtypes")
	public UserThreadMode(
			CmdType login, CmdType disconect,
			Map<CmdType, IRequestHandler> visitorCmdMapHandler,
			Map<CmdType, IRequestHandler> userCmdMapHandler,
			LimitedUnboundedThreadPoolExecutor pool,
			UserCenter<UserKey, User> userCenter,
			EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager,
			UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			int checkTime,
			int maxCount ) {
		
		this.eventManager = eventManager;
		
		this.login = login;
		this.disconect = disconect;
		this.visitorCmdMapHandler = visitorCmdMapHandler;
		this.userCmdMapHandler = userCmdMapHandler;
		
		this.userCenter = userCenter;
		this.visitorRequestFilter = new VisitorRequestFilterNotQueue<CmdType, ConnectKey, Visitor>( pool, eventManager );
		
		UserResourceCenter<Visitor, UserKey, User> userResourceCenter = new UserResourceCenter<Visitor, UserKey, User>(eventManager);
		this.userResourceManager = new UserResourceManager<ConnectKey, Visitor, UserKey, User>(userCenter, userResourceCenter, pool, eventManager);
		
		RequestFrequentFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter 
			= new RequestFrequentFilter<CmdType, ConnectKey, Visitor, UserKey, User>( checkTime, maxCount,
					eventManager, userResourceManager.getUserQueueResource() );
		this.userRequestFilter = userRequestFilter;
		userResourceCenter.addUserResource(userRequestFilter);
		
		userThreadModeFilter = new UserThreadModeFilter<CmdType, ConnectKey, Visitor, UserKey, User>(login, disconect, eventManager, 
				visitorCmdMapHandler, userCmdMapHandler, userCenter, visitorRequestFilter, userRequestFilter);
		
		userManager = new UserManager<CmdType, ConnectKey, Visitor, UserKey, User>(
				userThreadModeFilter, userCenter, userResourceCenter, userFlagBusiness);
		userQueueManager = new UserQueueManager<Visitor, UserKey, User>(userResourceManager.getUserQueueResource());
	}

	/**
	 * 
	 * 自动设置以下属性：
	 * 
	 * userCenter使用默认实现UserCenter
	 * visitorRequestFilter使用默认实现VisitorRequestFilterNotQueue
	 * userResourceCenter使用默认实现UserResourceCenter
	 * userResourceManager使用默认实现UserResourceManager
	 * userRequestFilter使用默认实现RequestFrequentFilter
	 * userThreadModeFilter使用默认实现UserThreadModeFilter
	 * 
	 * 
	 * @param login login Cmd
	 * @param disconect disconect Cmd
	 * @param visitorCmdMapHandler 游客Cmd对应处理器的Map
	 * @param userCmdMapHandler 用户Cmd对应处理器的Map
	 * @param pool 线程池
	 * @param eventManager 事件管理器
	 * @param checkTime 用户在checkTime毫秒内最多允许maxCount次请求(详见RequestFrequentFilter类描述)
	 * @param maxCount 用户在checkTime毫秒内最多允许maxCount次请求(详见RequestFrequentFilter类描述)
	 */
	@SuppressWarnings("rawtypes")
	public UserThreadMode(
			CmdType login, CmdType disconect,
			Map<CmdType, IRequestHandler> visitorCmdMapHandler,
			Map<CmdType, IRequestHandler> userCmdMapHandler,
			LimitedUnboundedThreadPoolExecutor pool,
			EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager,
			UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			int checkTime,
			int maxCount ) {
		
		this.eventManager = eventManager;
		
		this.login = login;
		this.disconect = disconect;
		this.visitorCmdMapHandler = visitorCmdMapHandler;
		this.userCmdMapHandler = userCmdMapHandler;
		
		this.userCenter = new UserCenter<UserKey, User>();
		this.visitorRequestFilter = new VisitorRequestFilterNotQueue<CmdType, ConnectKey, Visitor>( pool, eventManager );
		
		UserResourceCenter<Visitor, UserKey, User> userResourceCenter = new UserResourceCenter<Visitor, UserKey, User>(eventManager);
		this.userResourceManager = new UserResourceManager<ConnectKey, Visitor, UserKey, User>(userCenter, userResourceCenter, pool, eventManager);
		
		RequestFrequentFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter 
			= new RequestFrequentFilter<CmdType, ConnectKey, Visitor, UserKey, User>( checkTime, maxCount,
					eventManager, userResourceManager.getUserQueueResource() );
		this.userRequestFilter = userRequestFilter;
		userResourceCenter.addUserResource(userRequestFilter);
		
		userThreadModeFilter = new UserThreadModeFilter<CmdType, ConnectKey, Visitor, UserKey, User>(login, disconect, eventManager, 
				visitorCmdMapHandler, userCmdMapHandler, userCenter, visitorRequestFilter, userRequestFilter);
		
		userManager = new UserManager<CmdType, ConnectKey, Visitor, UserKey, User>(
				userThreadModeFilter, userCenter, userResourceCenter, userFlagBusiness);
		userQueueManager = new UserQueueManager<Visitor, UserKey, User>(userResourceManager.getUserQueueResource());
	}
	
	
	/**
	 * 获得登录的cmd
	 * 
	 * @return 登录的cmd
	 */
	public CmdType getLogin() {
		return login;
	}
	
	/**
	 * 获得断线的cmd
	 * 
	 * @return 断线的cmd
	 */
	public CmdType getDisconect() {
		return disconect;
	}
	
	/**
	 * 获得 游客cmd对应处理类的 map
	 * 
	 * @return 游客cmd对应处理器的 map
	 */
	@SuppressWarnings("rawtypes")
	public Map<CmdType, IRequestHandler> getVisitorCmdMapHandler() {
		return visitorCmdMapHandler;
	}
	
	/**
	 * 获得 用户cmd对应处理类的 map
	 * 
	 * @return 用户cmd对应处理类的 map
	 */
	@SuppressWarnings("rawtypes")
	public Map<CmdType, IRequestHandler> getUserCmdMapHandler() {
		return userCmdMapHandler;
	}
	
	/**
	 * 获得用户中心
	 * 
	 * @return 用户中心
	 */
	public UserCenter<UserKey, User> getUserCenter() {
		return userCenter;
	}
	
	/**
	 * 获得游客请求过滤器
	 * 
	 * @return 游客请求过滤器
	 */
	public IVisitorRequestFilter<CmdType, ConnectKey, Visitor> getVisitorRequestFilter() {
		return visitorRequestFilter;
	}
	
	/**
	 * 获得用户请求过滤器
	 * 
	 * @return 用户请求过滤器
	 */
	public IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> getUserRequestFilter() {
		return userRequestFilter;
	}

	/**
	 * 获得用户资源管理器
	 * 
	 * @return 用户资源管理器
	 */
	public UserResourceManager<ConnectKey, Visitor, UserKey, User> getUserResourceManager() {
		
		return userResourceManager;
	}

	/**
	 * 获得事件管理器
	 * 
	 * @return 事件管理器
	 */
	public EventManager<CmdType, ConnectKey, Visitor, UserKey, User> getEventManager() {
		return eventManager;
	}

	/**
	 * 获得 utm过滤器
	 *
	 * @return utm过滤器
	 */
	public UserThreadModeFilter<CmdType, ConnectKey, Visitor, UserKey, User> getUserThreadModeFilter() {
		return userThreadModeFilter;
	}

	/**
	 * 获得用户管理器
	 * 
	 * @return 用户管理器
	 */
	public UserManager<CmdType, ConnectKey, Visitor, UserKey, User> getUserManager() {
		return userManager;
	}

	/**
	 * 获得用户队列管理器
	 * 
	 * @return 户队列管理器
	 */
	public UserQueueManager<Visitor, UserKey, User> getUserQueueManager() {
		return userQueueManager;
	}
	
	
	
	
}
