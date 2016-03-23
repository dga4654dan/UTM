package com.dc.utm.resource.user;

import java.util.ArrayList;
import java.util.List;

import com.dc.utm.event.EventManager;

/**
 * 
 * 用户资源中心
 * 
 * 主要负责分发 用户资源 事件给各个具体的用户资源
 * 
 * @author Daemon
 *
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public class UserResourceCenter<Visitor, UserKey, User> implements IUserResourceEvent<Visitor, UserKey, User> {
	
	@SuppressWarnings("rawtypes")
	protected final EventManager eventManager;
	
	protected List<IUserResource<Visitor, UserKey, User>> userResourceList 
		= new ArrayList<IUserResource<Visitor, UserKey, User>>();
	
	@SuppressWarnings("rawtypes")
	public UserResourceCenter(EventManager eventManager) {
		
		this.eventManager = eventManager;
	}
	
	/**
	 * 添加用户资源
	 * 
	 * @param userResource 用户资源
	 */
	public void addUserResource(IUserResource<Visitor, UserKey, User> userResource) {
		
		userResourceList.add(userResource);
	}
	
	/**
	 * 删除用户资源
	 * 
	 * @param userResource 用户资源
	 */
	public void removeUserResource(IUserResource<Visitor, UserKey, User> userResource) {
		
		userResourceList.remove(userResource);
	}
	
	/**
	 * 获得所有的 用户资源 
	 * 
	 * @return 用户资源列表
	 */
	public List<IUserResource<Visitor, UserKey, User>> getUserResourceList() {
		
		return userResourceList;
	}
	
	
	
	
	
	/**
	 * 分发事件：游客申请登录（在onUserLoginCheck.before中被调用）
	 * 
	 * @param visitor 游客
	 * @param param 请求参数
	 */
	@Override
	public void beforeUserLoginCheck(Visitor visitor, Object param) {
		
		for( IUserResource<Visitor, UserKey, User> userResource : userResourceList ) {
			
			try {
				userResource.beforeUserLoginCheck(visitor, param);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}

	/**
	 * 分发事件：登录失败（用户名 密码等信息不正确（onUserLoginCheck.loginCheck返回null））
	 * 
	 * @param visitor 游客
	 * @param param 请求参数
	 */
	@Override
	public void userLoginCheckFail(Visitor visitor, Object param) {

		for( IUserResource<Visitor, UserKey, User> userResource : userResourceList ) {
			
			try {
				userResource.userLoginCheckFail(visitor, param);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}

	/**
	 * 分发事件：设置用户登录标志位成功（UserFlagBusiness.setLoginFlag返回成功）
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	@Override
	public void setLoginFlagSuccess(UserKey userKey, User user) {

		for( IUserResource<Visitor, UserKey, User> userResource : userResourceList ) {
			
			try {
				userResource.setLoginFlagSuccess(userKey,user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}

	/**
	 * 分发事件：等待老用户退出超时（详见UserLogoutCheckBusiness）
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	@Override
	public void waitUserLogoutTimeOut(UserKey userKey, User user) {

		for( IUserResource<Visitor, UserKey, User> userResource : userResourceList ) {
			
			try {
				userResource.waitUserLogoutTimeOut(userKey,user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}

	/**
	 * 分发事件：准备 "登录连接检查" （OnUserLoginHandler.loginLinkCheck前）
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	@Override
	public void beforeLoginLinkCheck(UserKey userKey, User user) {

		for( IUserResource<Visitor, UserKey, User> userResource : userResourceList ) {
			
			try {
				userResource.beforeLoginLinkCheck(userKey,user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}

	/**
	 * 分发事件： "登录连接检查"失败，用户已经断线（OnUserLoginHandler.loginLinkCheck返回false）
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	@Override
	public void failInLoginLinkCheck(UserKey userKey, User user) {

		for( IUserResource<Visitor, UserKey, User> userResource : userResourceList ) {
			
			try {
				userResource.failInLoginLinkCheck(userKey,user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}

	/**
	 * 分发事件：用户登录成功
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	@Override
	public void userIn(UserKey userKey, User user) {

		for( IUserResource<Visitor, UserKey, User> userResource : userResourceList ) {
			
			try {
				userResource.userIn(userKey,user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}

	/**
	 * 分发事件：用户退出或断线
	 * 
	 * @param userKey 用户Id
	 * @param user 用户
	 */
	@Override
	public void userOut(UserKey userKey, User user) {

		for( IUserResource<Visitor, UserKey, User> userResource : userResourceList ) {
			
			try {
				userResource.userOut(userKey,user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
	}
	

}
