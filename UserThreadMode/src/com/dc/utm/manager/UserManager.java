package com.dc.utm.manager;

import java.util.concurrent.TimeUnit;

import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.filter.UserThreadModeFilter;
import com.dc.utm.resource.user.UserResourceCenter;
import com.dc.utm.user.flag.UserFlagBusiness;

/**
 * 提供用户的部分管理功能
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd类型
 * @param <ConnectKey> 连接id
 * @param <Visitor> 游客对象
 * @param <UserKey> 用户id
 * @param <User> 用户对象
 */
public class UserManager<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	protected final UserThreadModeFilter<CmdType, ConnectKey, Visitor, UserKey, User> userThreadModeFilter;
	
	protected final UserCenter<UserKey, User> userCenter;
	protected final UserResourceCenter<Visitor, UserKey, User> userResourceCenter;
	protected final UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	
	public UserManager(
			UserThreadModeFilter<CmdType, ConnectKey, Visitor, UserKey, User> userThreadModeFilter,
			UserCenter<UserKey, User> userCenter,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter,
			UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness) {

		this.userThreadModeFilter = userThreadModeFilter;
		this.userCenter = userCenter;
		this.userResourceCenter = userResourceCenter;
		this.userFlagBusiness = userFlagBusiness;
	}

	/**
	 * 正常的断开用户，建议直接调用 user.killConnect()，只有在此方法无效的情况下，才采用这个方法尝试清理用户信息
	 * 注：出于严谨考虑，建议在调用该方法前先禁止用户登录，否则容易出现老用户被成功推出了，但是新用户登录上来了，导致该程序认为退出失败，尝试其他退出手段
	 * （该程序会尝试多种退出方式，并等候片刻后，再次检查用户登录标志位，如果登录标志位被移除了，则认为退出成功；
	 *      在尝试多种方法失败后，直接强制性移除用户标志位（如果老的连接事实上并未断开，可能会导致用户可以在本机上有两个客户连接的问题））
	 *  
	 *  该方法会返回处理的日志，可以从中获得本次处理的信息，并能协助定位问题。
	 *  
	 *  详见实现代码!
	 * 
	 * @param userKey 用户id
	 * @return 清理处理的日志（能更加清晰的定位问题）
	 */
	public String deepCleanUser(UserKey userKey) {
		
		StringBuilder logInfo = new StringBuilder("deepCleanUser userId:");
		logInfo.append(userKey).append("\n");
		
		// 如果该用户在本服务上登录才处理这个请求
		if( userFlagBusiness.isLoginLocal(userKey) ) {
			
			logInfo.append("-user is login local, try clean it now\n");
			
			// 尝试获得该用户，如果能找到该用户，则断开用户，并等待（希望能触发退出事件并正常处理）
			User user = userCenter.getUser(userKey);
			if( user != null ) {
				
				logInfo.append("-find user in userCenter, try disconect it\n");
				
				user.killConnect();
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} else {
				
				logInfo.append("*can't find user in userCenter, it means has some unknow problem !!!\n");
			}
			
			// 如果用户还是显示在本服务上
			if( userFlagBusiness.isLoginLocal(userKey) ) {
				
				logInfo.append("-find user still login local, try force trigger userThreadModeFilter.disconnect\n");
				
				// 强制触发用户断线事件，并等待（希望退出事件能正常的被处理）
				userThreadModeFilter.disconnect(-1, user);
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// 如果用户还是显示在本服务上
				if( userFlagBusiness.isLoginLocal(userKey) ) {
					
					// 用户退出彻底失败，采取强制性手段
					logInfo.append("-find user still login local, it means has some unknow problem !!!\n will force remove user resource and user flag!!!\n");
					
					// 如果用户存在，把用户资源移除
					if( user != null ) {
						//通知资源中心 回收用户资源
						userResourceCenter.userOut(userKey, user);
					}
					
					// 强制把该用户的标志位给移除掉
					userFlagBusiness.removeLoginFlag(-1, user, null);
					
				} else {
					logInfo.append("-user is logout success");
				}
				
			} else {
				logInfo.append("-user is logout success");
			}
			
		} else {
			
			logInfo.append("user is not login local server(userFlagBusiness.isLoginLocal return flase), will not do anything");
		}
		
		return logInfo.toString();
	}
	
}
