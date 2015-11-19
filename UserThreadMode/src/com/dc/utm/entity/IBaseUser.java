package com.dc.utm.entity;

public interface IBaseUser<UserKey> {
	
	UserKey getUserKey();

	boolean isLogining();
	
	void setLogin();
	
	void setLogout();
	
}
