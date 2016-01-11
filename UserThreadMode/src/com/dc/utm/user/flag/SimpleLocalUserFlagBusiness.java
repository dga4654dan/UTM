package com.dc.utm.user.flag;

import java.util.concurrent.ConcurrentHashMap;

import com.dc.utm.entity.IBaseUser;

/**
 * 
 * 简单的基于hashmap的UserFlagBusiness的实现
 * 
 * 注意：这个只是用来方便前期开发的（为了前期快速开发出一个原型），并不建议最终使用这个实现，因为这个实现只能保证一个服务的正确性
 * （因为它仅仅是将信息记录在内存中，推荐将信息保存在db中或者cache中，这样多个服务可以共享这些数据），
 * 而且在数据发生错误时会比较麻烦（如果在db中只需更新下数据）
 * 
 * @author Daemon
 *
 */
public class SimpleLocalUserFlagBusiness extends UserFlagBusiness<Object, Object, IBaseUser<Object>> {

	protected final ConcurrentHashMap<Object, Object> userIdSet = new ConcurrentHashMap<Object, Object>();
	
	@Override
	public SetLoginFlagResult setLoginFlag(int requestId, Object visitor,
			IBaseUser<Object> user, Object param) {

		Object old = userIdSet.putIfAbsent(user.getUserKey(), user.getUserKey());
		
		if( old == null )
			return SetLoginFlagResult.SUCCESS;
		else
			return SetLoginFlagResult.FAIL_LOGIN_LOCAL;
	
	}

	@Override
	public void rollBackLoginFlagWhenLinkDisable(int requestId, IBaseUser<Object> user,
			Object param) {
		
		userIdSet.remove(user.getUserKey());
	}

	@Override
	public void removeLoginFlag(int requestId, IBaseUser<Object> user, Object param) {

		userIdSet.remove(user.getUserKey());
	}
	
	/**
	 * 移除登录标志位（为修复数据预留的接口）
	 * 
	 * @param user 要移除登录标志位的用户
	 */
	public void removeLoginFlag(IBaseUser<Object> user) {

		userIdSet.remove(user.getUserKey());
	}
	
	/**
	 * 返回 存放登录用户的ConcurrentHashMap  （为修复数据预留的接口）
	 * 
	 * @return 存放登录用户的ConcurrentHashMap
	 */
	public ConcurrentHashMap<Object, Object> getUserIdSet() {
		
		return userIdSet;
	}
}
