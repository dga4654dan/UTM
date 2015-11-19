package com.dc.utm.center;

import java.util.concurrent.ConcurrentHashMap;

public class VisitorCenter<ConnectKey, Visitor> {

	protected final ConcurrentHashMap<ConnectKey, Visitor> visitorMap = new ConcurrentHashMap<ConnectKey, Visitor>();
	
	public Visitor getVisitor( ConnectKey key ) {
		
		return visitorMap.get(key);
	}
	
	public Visitor addVisitor( ConnectKey key, Visitor visitor ) {
		
		return visitorMap.put(key, visitor);
	}
	
	public Visitor removeVisitor( ConnectKey key, Visitor visitor ) {
		
		return visitorMap.remove(visitor);
	}
}
