package com.dc.utm.entity;

/**
 * 基础用户对象，
 * 提供了 isLogining，setLogin，setLogout三个方法的实现
 * 
 * @author Daemon
 *
 * @param <UserKey> 用户Id
 */
public abstract class BaseUser<UserKey> implements IBaseUser<UserKey> {

	@Override
	public abstract UserKey getUserKey();
	
	@Override
	public abstract void killConnect();

	
	protected boolean isLogining = false;
	
	@Override
	public boolean isLogining() {
		return isLogining;
	}
	
	@Override
	public void setLogin() {
		isLogining = true;
	}
	
	@Override
	public void setLogout() {
		isLogining = false;
	}

}
