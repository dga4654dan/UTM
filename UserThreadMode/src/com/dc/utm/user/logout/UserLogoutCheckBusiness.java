package com.dc.utm.user.logout;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.filter.user.IUserRequestFilter;
import com.dc.utm.resource.user.UserResourceCenter;
import com.dc.utm.resource.user.queue.UserQueueResource;
import com.dc.utm.user.flag.SetLoginFlagResult;
import com.dc.utm.user.flag.UserFlagBusiness;

/**
 * 
 * 用户退出检查线程
 * 
 * 检查需要退出的用户（用户重新登录）是否已经退出，退出了则继续执行登录处理onUserLoginHandler，
 * 等候超时了则调用waitLogoutTimeOut给用户推送消息，并通知资源管理器回收相应的资源
 * 注意：需要启动一个线程运行这个类（不断地检查用户是否已经退出（不断的调用userFlagBusiness.setLoginFlag方法，如果返回成功则说明旧的用户已经退出））
 * 
 * checkLoopSleepTime:线程检查完后挂起时间（多久开启下一轮检查）
 * maxWaitLogoutTime:最多运行等候多久（超过该时间则调用waitLogoutTimeOut给用户推送消息，并通知资源管理器回收相应的资源）
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public abstract class UserLogoutCheckBusiness<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> implements Runnable {
	
	protected final UserCenter<UserKey, User> userCenter;
	protected final UserQueueResource<Visitor, UserKey, User> userQueueResource;
	protected final IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter;
	
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	
	@SuppressWarnings("rawtypes")
	protected final IRequestHandler onUserLoginHandler;
	
	protected final EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager;
	
	protected final UserResourceCenter<Visitor, UserKey, User> userResourceCenter;
	
	
	protected final ConcurrentHashMap<UserKey, WaitCheckUserInfo> waitCheckUser = 
			new ConcurrentHashMap<UserKey, WaitCheckUserInfo>();
	
	protected final int checkLoopSleepTime;
	protected final int maxWaitLogoutTime;
	
	@SuppressWarnings("rawtypes")
	public UserLogoutCheckBusiness( UserCenter<UserKey, User> userCenter, 
			UserQueueResource<Visitor, UserKey, User> userQueueResource, 
			IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter,
			UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			IRequestHandler onUserLoginHandler,
			EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter) {
		
		this.checkLoopSleepTime = 650;
		this.maxWaitLogoutTime = 5000;
		
		this.userCenter = userCenter;
		this.userQueueResource = userQueueResource;
		this.userRequestFilter = userRequestFilter;
		this.userFlagBusiness = userFlagBusiness;
		this.onUserLoginHandler = onUserLoginHandler;
		this.eventManager = eventManager;
		this.userResourceCenter = userResourceCenter;
	}
	
	@SuppressWarnings("rawtypes")
	public UserLogoutCheckBusiness( int checkLoopSleepTime, int maxWaitLogoutTime,
			UserCenter<UserKey, User> userCenter, 
			UserQueueResource<Visitor, UserKey, User> userQueueResource, 
			IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter,
			UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			IRequestHandler onUserLoginHandler,
			EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter) {
		
		this.checkLoopSleepTime = checkLoopSleepTime;
		this.maxWaitLogoutTime = maxWaitLogoutTime;
		
		this.userCenter = userCenter;
		this.userQueueResource = userQueueResource;
		this.userRequestFilter = userRequestFilter;
		this.userFlagBusiness = userFlagBusiness;
		this.onUserLoginHandler = onUserLoginHandler;
		this.eventManager = eventManager;
		this.userResourceCenter = userResourceCenter;
	}

	/**
	 * 将用户加入检查队列中（检查用户什么时候退出，退出了则继续执行登录处理onUserLoginHandler，
	 * 等候超时了则调用waitLogoutTimeOut给用户推送消息，并通知资源管理器回收相应的资源）
	 * 
	 * @param requestId
	 * @param visitor
	 * @param user
	 * @param param
	 */
	public void loginWhenOldUserLogout(int requestId, Visitor visitor, User user, Object param) {
		
		waitCheckUser.put( user.getUserKey(), 
				new WaitCheckUserInfo(requestId, visitor, user, param, System.currentTimeMillis() + maxWaitLogoutTime ) );
	}
	
	/**
	 * 
	 * 等待用户信息类
	 * 
	 * @author Daemon
	 *
	 */
	class WaitCheckUserInfo {
		
		final int requestId;
		final Visitor visitor;
		final User user;
		final Object param;
		
		final long avalidTime;

		public WaitCheckUserInfo(int requestId, Visitor visitor,
				User user, Object param, long avalidTime) {

			this.requestId = requestId;
			this.visitor = visitor;
			this.user = user;
			this.param = param;
			this.avalidTime = avalidTime;
		}
		
	}

	@Override
	public void run() {
		
		for( ;; ) {
			
			try {
				
				//遍历所有等待的用户
				for( WaitCheckUserInfo waitCheckUserInfo : waitCheckUser.values() ) {
					
					int requestId = waitCheckUserInfo.requestId;
					Visitor visitor = waitCheckUserInfo.visitor;
					User user = waitCheckUserInfo.user;
					Object param = waitCheckUserInfo.param;
					
					//判断是否等待超时
					if( System.currentTimeMillis() > waitCheckUserInfo.avalidTime ) {
						
						//该用户等待超时
						
						try {
							
							waitLogoutTimeOut(requestId, waitCheckUserInfo.visitor, user, param);
							
						} catch(Exception e) {
							
							eventManager.getExceptionLogger().exception(e);
						}
						
						//通知资源中心 回收相关资源
						userResourceCenter.waitUserLogoutTimeOut(user.getUserKey(), user);
						
						//从等待用户中移除
						waitCheckUser.remove( waitCheckUserInfo.user.getUserKey() );
						
					} else {
						
						//该用户未等待超时
						
						try {
							
							UserKey userKey = user.getUserKey();
							
							try {
								
								//尝试设置登录标志位
								if( userFlagBusiness.setLoginFlag(requestId, visitor, user, param) == SetLoginFlagResult.SUCCESS ) {
									
									//通知资源中心
									userResourceCenter.setLoginFlagSuccess(userKey, user);
									
									//调用utm调用onUserLoginHandler处理登录事件(调用线程池处理)
									ITaskQueue<User, UserKey, Object> taskQueue = userQueueResource.getUserTaskQueue(userKey);
									taskQueue.getQueueOperateLock().lock();
									try {
										
										userRequestFilter.doFilter(userKey, user, onUserLoginHandler, requestId, param);
										
									} finally {
										taskQueue.getQueueOperateLock().unlock();
									}
									
									//从等待用户中移除
									waitCheckUser.remove( waitCheckUserInfo.user.getUserKey() );
								}
								
							} catch(Exception e) {
								
								eventManager.getExceptionLogger().exception(e);
							}
							
						} catch(Exception e) {
							
							eventManager.getExceptionLogger().exception(e);
						}
						
					}
				}
				
			} catch(Exception e) {
				
				eventManager.getExceptionLogger().exception(e);
				
			} finally {
				
				//挂起checkLoopSleepTime毫秒
				try {
					TimeUnit.MILLISECONDS.sleep(checkLoopSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	/**
	 * 等待时间超过maxWaitLogoutTime毫秒，用户依然未从原来的服务上退出
	 * （该方法主要是负责通知前端说登录失败）
	 * 
	 * @param requestId 请求Id
	 * @param visitor 游客对象
	 * @param user 用户对象
	 * @param param 请求参数
	 */
	public abstract void waitLogoutTimeOut(int requestId, Visitor visitor, User user, Object param);
	
}








