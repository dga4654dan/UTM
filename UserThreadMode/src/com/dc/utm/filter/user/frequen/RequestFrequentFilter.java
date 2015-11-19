package com.dc.utm.filter.user.frequen;

import java.util.concurrent.ConcurrentHashMap;

import com.dc.qtm.IExceptionListener;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.filter.user.UserRequestFilter;

public abstract class RequestFrequentFilter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> 
	extends UserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> {

	private final long minGap;
	private final int illegalMax;
	private final int ringSize;
	
	protected ConcurrentHashMap<UserKey, UserRequestFrequentInfo> userKeyMapRequestInfo
		= new ConcurrentHashMap<UserKey, UserRequestFrequentInfo>();
	
	public RequestFrequentFilter( long minGap, int ringSize, int illegalMax,
			LimitedUnboundedThreadPoolExecutor pool,
			IExceptionListener exceptionListener ) {
		
		super(pool, exceptionListener);
		
		this.minGap = minGap;
		this.illegalMax = illegalMax;
		this.ringSize = ringSize;
		
	}
	
	public void removeUserInfoWhenUserLogout(UserKey key) {
		
		userKeyMapRequestInfo.remove(key);
	}
	
	@Override
	public void doFilter(UserKey key, User user,
			IRequestHandler<User, ?> handler, int requestId, Object param) {
		
		UserRequestFrequentInfo userRequestFrequentInfo = userKeyMapRequestInfo.get(key);
		if( userRequestFrequentInfo == null ) {
			
			userRequestFrequentInfo = new UserRequestFrequentInfo( new RingLongRecord(ringSize) );
			UserRequestFrequentInfo old = userKeyMapRequestInfo.putIfAbsent(key, userRequestFrequentInfo);
			if( old != null )
				userRequestFrequentInfo = old;
		}
		
		long timeNow = System.currentTimeMillis();
		
		if ( ! userRequestFrequentInfo.ringRecord.setFirstRecordNCompare(timeNow - minGap, timeNow) ) {

			int illgalCount = userRequestFrequentInfo.illgalCount.incrementAndGet();
			
			if( illgalCount > illegalMax ) {
				
				this.requestFrequent(key, user, handler, requestId, param);
				return;
				
			}
			
		}
		
		userTaskQueueBusiness.execTask(key, user, handler, requestId, param);
		
	}
	
	public abstract void requestFrequent(UserKey key, User user,
			IRequestHandler<User, ?> handler, int requestId, Object param);
	
}
