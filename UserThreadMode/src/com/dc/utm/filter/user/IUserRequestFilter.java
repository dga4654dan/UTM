package com.dc.utm.filter.user;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.entity.IBaseUser;

public interface IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {

	void doFilter( UserKey key, User user, IRequestHandler<User, ?> handler, int requestId, Object param );
	
}
