package com.dc.utm.user.logout;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.utm.center.DataCenter;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.filter.user.IUserRequestFilter;
import com.dc.utm.log.exception.IExceptionLogger;
import com.dc.utm.user.flag.SetLoginFlagResult;
import com.dc.utm.user.flag.UserFlagBusiness;
import com.dc.utm.user.queue.UserTaskQueueBusiness;

public abstract class UserLogoutCheckBusiness<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> implements Runnable {
	
	protected final UserCenter<UserKey, User> userCenter;
	protected final UserTaskQueueBusiness<UserKey, User> userTaskQueueBusiness;
	protected final IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, IBaseUser<UserKey>> userRequestFilter;
	
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	
	protected final IRequestHandler<IBaseUser<UserKey>, Object> onUserLoginHandler;
	
	protected final IExceptionLogger exceptionLogger;
	
	
	protected final ConcurrentHashMap<UserKey, WaitCheckUserInfo> waitCheckUser = 
			new ConcurrentHashMap<UserKey, WaitCheckUserInfo>();
	
	protected final int checkLoopSleepTime = 650;
	protected final int maxWaitLogoutTime = 5000;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public UserLogoutCheckBusiness( DataCenter dataCenter ) {
		
		this.userCenter = dataCenter.userCenter;
		this.userTaskQueueBusiness = dataCenter.userTaskQueueBusiness;
		this.userRequestFilter = dataCenter.userRequestFilter;
		this.userFlagBusiness = dataCenter.userFlagBusiness;
		this.onUserLoginHandler = dataCenter.onUserLoginHandler;
		this.exceptionLogger = dataCenter.exceptionLogger;
		
	}

	public void loginWhenOldUserLogout(int requestId, Visitor visitor, User user, Object param) {
		
		waitCheckUser.put( user.getUserKey(), 
				new WaitCheckUserInfo(requestId, visitor, user, param, System.currentTimeMillis() + maxWaitLogoutTime ) );
	}
	
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
				
				for( WaitCheckUserInfo waitCheckUserInfo : waitCheckUser.values() ) {
					
					int requestId = waitCheckUserInfo.requestId;
					Visitor visitor = waitCheckUserInfo.visitor;
					User user = waitCheckUserInfo.user;
					Object param = waitCheckUserInfo.param;
					
					if( System.currentTimeMillis() > waitCheckUserInfo.avalidTime ) {
						
						waitLogoutTimeOut(requestId, waitCheckUserInfo.visitor, user, param);
						waitCheckUser.remove( waitCheckUserInfo.user.getUserKey() );
						
					} else {
						
						try {
							
							UserKey userKey = user.getUserKey();
							
							ITaskQueue<User, UserKey, Object> taskQueue = userTaskQueueBusiness.getUserTaskQueue(userKey);
							taskQueue.getQueueOperateLock().lock();
							try {
								
								if( userFlagBusiness.setLoginFlag(requestId, visitor, user, param) == SetLoginFlagResult.SUCCESS ) {
								
									//处理login任务(将login加入到queue中)
									userRequestFilter.doFilter(userKey, user, onUserLoginHandler, requestId, param);
									waitCheckUser.remove( waitCheckUserInfo.user.getUserKey() );
								}
								
							} finally {
								
								taskQueue.getQueueOperateLock().unlock();
							}
							
						} catch(Exception e) {
							
							exceptionLogger.exception(e);
						}
						
					}
				}
				
			} catch(Exception e) {
				
				exceptionLogger.exception(e);
				
			} finally {
				
				try {
					TimeUnit.MILLISECONDS.sleep(checkLoopSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public abstract void waitLogoutTimeOut(int requestId, Visitor visitor, User user, Object param);
	
}








