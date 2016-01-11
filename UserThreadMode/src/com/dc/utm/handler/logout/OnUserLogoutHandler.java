package com.dc.utm.handler.logout;

import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.resource.user.UserResourceCenter;
import com.dc.utm.user.flag.UserFlagBusiness;

/**
 * 
 * 用户退出处理
 * 
 * OnUserLogoutHandler 和 OnUserDisconectHandler只有一个会被触发
 * （OnUserLogoutHandler后用户将认为是游客，其断线将不会触发OnUserDisconectHandler）
 * 
 * @author Daemon
 *
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 * @param <Param> 退出参数
 */
public abstract class OnUserLogoutHandler<Visitor, UserKey, User extends IBaseUser<UserKey>, Param> 
	extends AbstractOutHandler<Visitor, UserKey, User, Param> {

	@SuppressWarnings("rawtypes")
	public OnUserLogoutHandler(UserCenter<UserKey, User> userCenter,
			EventManager eventManager,
			UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter) {
		super(userCenter, eventManager, userFlagBusiness, userResourceCenter);
	}
	
	@SuppressWarnings("unchecked")
	public void userOutOver(int requestId, User user, Param param) {
		
		//发布用户退出登录事件
		eventManager.getUserEventManager().userLogout(user.getUserKey(), user);
	}
	
	public void userOut(int requestId, User user, Param param) {
		
		userLogout(requestId, user, param);
	}
	
	/**
	 * 用户退出登录相应的处理
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 * @param param 请求参数
	 */
	public abstract void userLogout(int requestId, User user, Param param);
	
}
