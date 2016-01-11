package com.dc.utm.handler.logout;

import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.resource.user.UserResourceCenter;
import com.dc.utm.user.flag.UserFlagBusiness;

/**
 * 
 * 用户断线处理
 * 
 * OnUserLogoutHandler 和 OnUserDisconectHandler只有一个会被触发
 * （OnUserLogoutHandler后用户将认为是游客，其断线将不会触发OnUserDisconectHandler）
 * 
 * @author Daemon
 *
 * @param <Visitor> 游客
 * @param <UserKey> 用户Id
 * @param <User> 用户
 * @param <Param> 参数
 */
public abstract class OnUserDisconectHandler<Visitor, UserKey, User extends IBaseUser<UserKey>, Param> 
	extends AbstractOutHandler<Visitor, UserKey, User, Param> {

	@SuppressWarnings("rawtypes")
	public OnUserDisconectHandler(UserCenter<UserKey, User> userCenter,
			EventManager eventManager,
			UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness,
			UserResourceCenter<Visitor, UserKey, User> userResourceCenter) {
		super(userCenter, eventManager, userFlagBusiness, userResourceCenter);
	}

	@SuppressWarnings("unchecked")
	public void userOutOver(int requestId, User user, Param param) {
		
		//发布用户断线事件
		eventManager.getUserEventManager().userDisconnect(user.getUserKey(), user);
	}
	
	public void userOut(int requestId, User user, Param param) {
		
		userDisconect(requestId, user, param);
	}
	
	/**
	 * 用户断线相应的处理
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 * @param param 参数
	 */
	public abstract void userDisconect(int requestId, User user, Param param);
}
