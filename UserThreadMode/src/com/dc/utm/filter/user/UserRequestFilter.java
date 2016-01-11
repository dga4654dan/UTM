package com.dc.utm.filter.user;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.resource.user.queue.UserQueueResource;

/**
 * 
 * 基本的用户请求过滤器，没有进行什么处理，直接调用线程池去处理该请求
 * (这里采用的qtm，就是一个用户的来说，请求时有顺序的执行的)
 * 
 * @author Daemon
 *
 * @param <CmdType>
 * @param <ConnectKey>
 * @param <Visitor>
 * @param <UserKey>
 * @param <User>
 */
public class UserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> 
	implements IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> {
	
	protected final UserQueueResource<Visitor, UserKey, User> userQueueResource;

	public UserRequestFilter( UserQueueResource<Visitor, UserKey, User> userQueueResource ) {
		
		this.userQueueResource = userQueueResource;
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public void doFilter(UserKey key, User user, IRequestHandler handler, 
			int requestId, Object param) {
		
		//直接调用qtm去处理该请求
		userQueueResource.execTask(key, user, handler, requestId, param);
		
	}
	
	

}
