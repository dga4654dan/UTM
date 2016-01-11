package com.dc.utm.event.user;

import java.util.ArrayList;
import java.util.List;

import com.dc.utm.event.EventManager;

/**
 * 
 * 用户事件管理类，提供用户事件的管理
 * 本身也继承了 IUserEventHandler 接口，负责分发用户事件
 * 
 * @author Daemon
 *
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public class UserEventManager<UserKey, User> implements IUserEventHandler<UserKey, User> {
	
	protected List<IUserEventHandler<UserKey, User>> userEventList = new ArrayList<IUserEventHandler<UserKey,User>>();

	protected List<IUserLoginEventHandler<UserKey, User>> userLoginEventList = new ArrayList<IUserLoginEventHandler<UserKey,User>>();
	protected List<IUserLogoutEventHandler<UserKey, User>> userLogoutEventList = new ArrayList<IUserLogoutEventHandler<UserKey,User>>();
	protected List<IUserDisconnectEventHandler<UserKey, User>> userDisconnectEventList = new ArrayList<IUserDisconnectEventHandler<UserKey,User>>();
	
	@SuppressWarnings("rawtypes")
	protected EventManager eventManager;
	
	@SuppressWarnings("rawtypes")
	public UserEventManager(EventManager eventManager) {
		
		this.eventManager = eventManager;
	}
	
	
	/**
	 * @param userEventHandler 添加用户事件处理类
	 */
	public void addUserEventHandler(IUserEventHandler<UserKey, User> userEventHandler) {
		
		userEventList.add(userEventHandler);
	}
	
	/**
	 * @param userEventHandler 移除用户事件处理类
	 */
	public void removeUserEventHandler(IUserEventHandler<UserKey, User> userEventHandler) {
		
		userEventList.remove(userEventHandler);
	}
	
	/**
	 * @param userLoginEventHandler 添加用户登录事件处理类
	 */
	public void addUserLoginEventHandler(IUserLoginEventHandler<UserKey, User> userLoginEventHandler) {
		
		userLoginEventList.add(userLoginEventHandler);
	}
	
	/**
	 * @param userLoginEventHandler 移除用户登录事件处理类
	 */
	public void removeUserLoginEventHandler(IUserLoginEventHandler<UserKey, User> userLoginEventHandler) {
		
		userLoginEventList.remove(userLoginEventHandler);
	}
	
	/**
	 * @param userLogoutEventHandler 添加用户退出事件处理类
	 */
	public void addUserLogoutEventHandler(IUserLogoutEventHandler<UserKey, User> userLogoutEventHandler) {
		
		userLogoutEventList.add(userLogoutEventHandler);
	}
	
	/**
	 * @param userLogoutEventHandler 移除用户退出事件处理类
	 */
	public void removeUserLogoutEventHandler(IUserLogoutEventHandler<UserKey, User> userLogoutEventHandler) {
		
		userLogoutEventList.remove(userLogoutEventHandler);
	}
	
	/**
	 * @param userDisconnectEventHandler 添加用户断线事件处理类
	 */
	public void addUserDisconnectEventHandler(IUserDisconnectEventHandler<UserKey, User> userDisconnectEventHandler) {
		
		userDisconnectEventList.add(userDisconnectEventHandler);
	}
	
	/**
	 * @param userDisconnectEventHandler 移除用户断线事件处理类
	 */
	public void removeUserDisconnectEventHandler(IUserDisconnectEventHandler<UserKey, User> userDisconnectEventHandler) {
		
		userDisconnectEventList.remove(userDisconnectEventHandler);
	}
	
	
	
	
	/**
	 * 分发用户登录事件
	 */
	@Override
	public void userLogin(UserKey userKey, User user) {
		
		for( IUserEventHandler<UserKey, User> handler : userEventList ) {
			
			try {
				handler.userLogin(userKey, user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
		for( IUserLoginEventHandler<UserKey, User> handler : userLoginEventList ) {
			
			try {
				handler.userLogin(userKey, user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
	}

	/**
	 * 分发用户退出事件
	 */
	@Override
	public void userLogout(UserKey userKey, User user) {
		
		for( IUserEventHandler<UserKey, User> handler : userEventList ) {
			
			try {
				handler.userLogout(userKey, user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
		for( IUserLogoutEventHandler<UserKey, User> handler : userLogoutEventList ) {
			
			try {
				handler.userLogout(userKey, user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
	}

	/**
	 * 分发用户断线事件
	 */
	@Override
	public void userDisconnect(UserKey userKey, User user) {
		
		for( IUserEventHandler<UserKey, User> handler : userEventList ) {
			
			try {
				handler.userDisconnect(userKey, user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
		
		for( IUserDisconnectEventHandler<UserKey, User> handler : userDisconnectEventList ) {
			
			try {
				handler.userDisconnect(userKey, user);
			} catch (Exception e) {
				eventManager.getExceptionLogger().exception(e);
			}
		}
	}
}
