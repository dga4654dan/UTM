package com.dc.utm.filter.user.frequen;

import java.util.concurrent.atomic.AtomicInteger;

public class UserRequestFrequentInfo {
	
	public final AtomicInteger illgalCount = new AtomicInteger(0);
	public final RingLongRecord ringRecord;
	
	public UserRequestFrequentInfo(RingLongRecord ringRecord) {
		
		this.ringRecord = ringRecord;
	}
	
}
