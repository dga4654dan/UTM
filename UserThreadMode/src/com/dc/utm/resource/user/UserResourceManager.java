package com.dc.utm.resource.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.resource.user.queue.UserQueueResource;
import com.dc.utm.resource.user.user.UserObjResource;

/**
 * 用户资源管理
 * 暂时没有实现管理监控相关的功能，
 * 只是在实例化UserResourceManager时加入两个utm必须要的资源控制器：qtm队列 和 userCenter中的对象管理
 * 
 * @author Daemon
 *
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public class UserResourceManager<ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	protected final UserResourceCenter<Visitor, UserKey, User> userResourceCenter;
	
	protected final UserQueueResource<Visitor, UserKey, User> userQueueResource;
	protected final UserObjResource<Visitor, UserKey, User> userObjResource;
	
	@SuppressWarnings("rawtypes")
	public UserResourceManager(UserCenter<UserKey, User> userCenter,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter,
			LimitedUnboundedThreadPoolExecutor pool,
			EventManager eventManager) {
		
		this.userResourceCenter = userResourceCenter;
		
		this.userQueueResource = new UserQueueResource<Visitor, UserKey, User>(pool, eventManager);
		this.userObjResource = new UserObjResource<Visitor, UserKey, User>(userCenter);
		
		//加入两个utm必须要的资源控制器：qtm队列 和 userCenter中的对象管理
		
		userResourceCenter.addUserResource(userQueueResource);
		userResourceCenter.addUserResource(userObjResource);
		
	}
	
	/**
	 * 获得用户资源中心
	 * 
	 * @return 用户资源中心
	 */
	public UserResourceCenter<Visitor, UserKey, User> getUserResourceCenter() {
		
		return userResourceCenter;
	}
	
	/**
	 * 获得 "用户队列资源" 控制器
	 * 
	 * @return "用户队列资源" 控制器
	 */
	public UserQueueResource<Visitor, UserKey, User> getUserQueueResource() {
		
		return userQueueResource;
	}

	/**
	 * 获得 "userCenter中User资源" 控制器（何时往userCenter添加User和移除User）
	 * 
	 * @return "userCenter中User资源" 控制器（何时往userCenter添加User和移除User）
	 */
	public UserObjResource<Visitor, UserKey, User> getUserObjResource() {
		
		return userObjResource;
	}
	
	/**
	 * 获得 资源名称 对应 每个资源活跃的用户个数
	 * 
	 * @return 资源名称 对应 每个资源活跃的用户个数
	 */
	public Map<String, Integer> getResourceNameMapActiveNum() {
		
		List<IUserResource<Visitor, UserKey, User>> userResourceList = userResourceCenter.getUserResourceList();
		
		HashMap<String, Integer> resourceNameMapActiveNum = new HashMap<String, Integer>(userResourceList.size()<<1);
		
		for( IUserResource<Visitor, UserKey, User> resource : userResourceList ) {
			
			resourceNameMapActiveNum.put( resource.getName(), resource.getActiveCount() );
		}
		
		return resourceNameMapActiveNum;
	}
}




