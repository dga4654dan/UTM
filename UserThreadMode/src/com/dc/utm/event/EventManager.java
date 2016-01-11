package com.dc.utm.event;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.cmd.exception.IRequestUnknowCmdHandler;
import com.dc.utm.event.exception.IExceptionLogger;
import com.dc.utm.event.user.IUserDisconnectEventHandler;
import com.dc.utm.event.user.IUserEventHandler;
import com.dc.utm.event.user.IUserLoginEventHandler;
import com.dc.utm.event.user.IUserLogoutEventHandler;
import com.dc.utm.event.user.UserEventManager;
import com.dc.utm.filter.user.frequen.IRequestFrequentHandler;

/**
 * 
 * 事件管理类，utm相关的事件都包含在里面，如果有需要更改部分事件可以继承覆盖该类。
 * 
 * 提供了 "用户事件", "异常处理事件", "请求了没有注册的cmd事件", "用户请求过于频繁事件" 的基本实现
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 */
public class EventManager<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	
	protected UserEventManager<UserKey, User> userEventManager = new UserEventManager<UserKey, User>(this);
	
	/**
	 * @return 用户事件分发类
	 */
	public IUserEventHandler<UserKey, User> getUserEventManager() {
		
		return userEventManager;
	}
	
	
	
	
	/**
	 * 简单的异常处理类(仅仅打印信息到System.err)
	 */
	protected IExceptionLogger exceptionLogger = new IExceptionLogger() {
		
		@Override
		public void exception(Exception e) {
			e.printStackTrace();
		}
		
		@Override
		public void erroState(String erroInfo) {
			System.err.println(erroInfo);
		}
	};
	
	/**
	 * @return 异常处理类
	 */
	public IExceptionLogger getExceptionLogger() {
		
		return exceptionLogger;
	}
	
	
	/**
	 * 简单的用户或游客客 请求了没有注册的cmd的处理类(仅仅打印信息到System.err)
	 */
	protected IRequestUnknowCmdHandler<CmdType, ConnectKey, Visitor, UserKey, User> requestUnknowCmdHandler
		= new IRequestUnknowCmdHandler<CmdType, ConnectKey, Visitor, UserKey, User>() {
	
			@Override
			public void visitorRequestUnknowCmd(ConnectKey connectKey,
					Visitor visitor, CmdType cmd, Object param) {
				System.err.println("visitor:" + visitor + " request unknow cmd:" + cmd + ", param:" + param.toString());
			}
	
			@Override
			public void userRequestUnknowCmd(UserKey userKey, User user,
					CmdType cmd, Object param) {
				System.err.println("user:(key:" + userKey + ") request unknow cmd:" + cmd + ", param:" + param.toString());
			}
		};
	
	/**
	 * @return 用户或游客客 请求了没有注册的cmd的处理类
	 */
	public IRequestUnknowCmdHandler<CmdType, ConnectKey, Visitor, UserKey, User> getRequestUnknowCmdHandler() {
		
		return requestUnknowCmdHandler;
	}
	
	
	
	/**
	 * 简单的用户请求过于频繁处理类 (直接断开用户)
	 */
	protected IRequestFrequentHandler<CmdType, ConnectKey, Visitor, UserKey, User> requestFrequentHandler 
		= new IRequestFrequentHandler<CmdType, ConnectKey, Visitor, UserKey, User>() {

		@SuppressWarnings("rawtypes")
		@Override
		public void requestFrequent(UserKey key, User user,
				IRequestHandler handler, int requestId, Object param) {
			
			user.killConnect();
		}
	};
	
	/**
	 * @return 用户请求过于频繁处理类
	 */
	public IRequestFrequentHandler<CmdType, ConnectKey, Visitor, UserKey, User> getRequestFrequentHandler() {
		return requestFrequentHandler;
	}
	
	
	

	
	

	/**
	 * @param userEventHandler 添加用户事件处理类
	 */
	public void addUserEventHandler(IUserEventHandler<UserKey, User> userEventHandler) {
		
		userEventManager.addUserEventHandler(userEventHandler);
	}
	
	/**
	 * @param userEventHandler 移除用户事件处理类
	 */
	public void removeUserEventHandler(IUserEventHandler<UserKey, User> userEventHandler) {
		
		userEventManager.removeUserEventHandler(userEventHandler);
	}
	
	/**
	 * @param userLoginEventHandler 添加用户登录事件处理类
	 */
	public void addUserLoginEventHandler(IUserLoginEventHandler<UserKey, User> userLoginEventHandler) {
		
		userEventManager.addUserLoginEventHandler(userLoginEventHandler);
	}
	
	/**
	 * @param userLoginEventHandler 移除用户登录事件处理类
	 */
	public void removeUserLoginEventHandler(IUserLoginEventHandler<UserKey, User> userLoginEventHandler) {
		
		userEventManager.removeUserLoginEventHandler(userLoginEventHandler);
	}
	
	/**
	 * @param userLogoutEventHandler 添加用户退出事件处理类
	 */
	public void addUserLogoutEventHandler(IUserLogoutEventHandler<UserKey, User> userLogoutEventHandler) {
		
		userEventManager.addUserLogoutEventHandler(userLogoutEventHandler);
	}
	
	/**
	 * @param userLogoutEventHandler 移除用户退出事件处理类
	 */
	public void removeUserLogoutEventHandler(IUserLogoutEventHandler<UserKey, User> userLogoutEventHandler) {
		
		userEventManager.removeUserLogoutEventHandler(userLogoutEventHandler);
	}
	
	/**
	 * @param userDisconnectEventHandler 添加用户断线事件处理类
	 */
	public void addUserDisconnectEventHandler(IUserDisconnectEventHandler<UserKey, User> userDisconnectEventHandler) {
		
		userEventManager.addUserDisconnectEventHandler(userDisconnectEventHandler);
	}
	
	/**
	 * @param userDisconnectEventHandler 移除用户断线事件处理类
	 */
	public void removeUserDisconnectEventHandler(IUserDisconnectEventHandler<UserKey, User> userDisconnectEventHandler) {
		
		userEventManager.removeUserDisconnectEventHandler(userDisconnectEventHandler);
	}
	
}
