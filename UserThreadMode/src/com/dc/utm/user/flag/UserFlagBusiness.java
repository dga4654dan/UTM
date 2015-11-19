package com.dc.utm.user.flag;

import com.dc.utm.entity.IBaseUser;

public abstract class UserFlagBusiness<Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	public abstract SetLoginFlagResult setLoginFlag(int requestId, Visitor visitor, User user, Object param);
	
	public abstract void rollBackLoginFlagWhenLinkDisable(int requestId, User user, Object param);
	
	public abstract void removeLoginFlag(int requestId, User user, Object param);
	
}
