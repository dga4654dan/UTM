package com.dc.utm.filter.user.frequen;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户请求信息记录，用于计算用户是否请求过于频繁
 * 
 * @author Daemon
 *
 */
public class UserRequestFrequentInfo {
	
	public final AtomicInteger illgalCount = new AtomicInteger(0);
	public final RingLongRecord ringRecord;
	
	public UserRequestFrequentInfo(RingLongRecord ringRecord) {
		
		this.ringRecord = ringRecord;
	}
	
}
