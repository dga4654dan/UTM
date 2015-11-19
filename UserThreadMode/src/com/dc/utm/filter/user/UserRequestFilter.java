package com.dc.utm.filter.user;

import com.dc.qtm.IExceptionListener;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.user.queue.UserTaskQueueBusiness;

public class UserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> 
	implements IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> {
	
	protected final UserTaskQueueBusiness<UserKey, User> userTaskQueueBusiness;

	public UserRequestFilter( LimitedUnboundedThreadPoolExecutor pool,
			IExceptionListener exceptionListener ) {
		
		this.userTaskQueueBusiness = new UserTaskQueueBusiness<UserKey, User>(pool, exceptionListener);
	}
	
	@Override
	public void doFilter(UserKey key, User user, IRequestHandler<User, ?> handler, 
			int requestId, Object param) {

		userTaskQueueBusiness.execTask(key, user, handler, requestId, param);
		
	}
	
	

}
