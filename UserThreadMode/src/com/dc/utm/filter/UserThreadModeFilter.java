package com.dc.utm.filter;

import java.util.Map;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.center.UserCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.event.EventManager;
import com.dc.utm.filter.user.IUserRequestFilter;
import com.dc.utm.filter.visitor.IVisitorRequestFilter;

/**
 * 
 * UTM 过滤器
 * 
 * @author Daemon
 *
 * @param <CmdType> cmd
 * @param <ConnectKey> 游客Id
 * @param <Visitor> 游客
 * @param <UserKey> 玩家Id
 * @param <User> 玩家
 */
public class UserThreadModeFilter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	protected final CmdType login;
	protected final CmdType disconect;
	
	protected final EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager;
	
	@SuppressWarnings("rawtypes")
	protected final Map<CmdType, IRequestHandler> visitorCmdMapHandler;
	@SuppressWarnings("rawtypes")
	protected final Map<CmdType, IRequestHandler> userCmdMapHandler;
	
	protected final UserCenter<UserKey, User> userCenter;

	protected final IVisitorRequestFilter<CmdType, ConnectKey, Visitor> visitorRequestFilter;
	protected final IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter;

	@SuppressWarnings("rawtypes")
	public UserThreadModeFilter(
			CmdType login,
			CmdType disconect,
			EventManager<CmdType, ConnectKey, Visitor, UserKey, User> eventManager,
			Map<CmdType, IRequestHandler> visitorCmdMapHandler,
			Map<CmdType, IRequestHandler> userCmdMapHandler,
			UserCenter<UserKey, User> userCenter,
			IVisitorRequestFilter<CmdType, ConnectKey, Visitor> visitorRequestFilter,
			IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter) {

		this.login = login;
		this.disconect = disconect;
		this.eventManager = eventManager;
		this.visitorCmdMapHandler = visitorCmdMapHandler;
		this.userCmdMapHandler = userCmdMapHandler;
		this.userCenter = userCenter;
		this.visitorRequestFilter = visitorRequestFilter;
		this.userRequestFilter = userRequestFilter;
	}

	/**
	 *  用户断开连接
	 * 
	 * @param requestId 请求Id
	 * @param userKey 用户Id
	 */
	public void disconnect( int requestId, UserKey userKey ) {
		
		handleUserRequest(requestId, disconect, userKey, null);
	}
	
	/**
	 *  用户断开连接
	 * 
	 * @param requestId 请求Id
	 * @param user 用户
	 */
	public void disconnect( int requestId, User user ) {
		
		handleUserRequest(requestId, disconect, user, null);
	}
	
	/**
	 * 
	 * 游客cmd请求
	 * 
	 * @param requestId 请求Id
	 * @param cmd cmd
	 * @param connectKey 游客Id
	 * @param visitor 游客
	 * @param param 请求参数
	 */
	@SuppressWarnings("unchecked")
	public void handleVisitorRequest( int requestId, CmdType cmd, ConnectKey connectKey, Visitor visitor, Object param ) {
		
		@SuppressWarnings("rawtypes")
		IRequestHandler handler = visitorCmdMapHandler.get(cmd);
		if( handler == null ) {
			
			eventManager.getRequestUnknowCmdHandler().visitorRequestUnknowCmd(connectKey, visitor, cmd, param);
			
		} else {
			
			visitorRequestFilter.doFilter(connectKey, visitor, (IRequestHandler<Visitor, Object>)handler, requestId, param);
			
		}
		
	}
	
	/**
	 * 
	 * 用户cmd请求
	 * 
	 * @param requestId 请求Id
	 * @param cmd cmd
	 * @param userKey 用户Id
	 * @param param 请求参数
	 */
	public void handleUserRequest( int requestId, CmdType cmd, UserKey userKey, Object param ) {
		
		User user = userCenter.getUser( userKey );
		
		if( user != null ) {
			
			@SuppressWarnings("rawtypes")
			IRequestHandler handler = userCmdMapHandler.get(cmd);
			if( handler == null ) {
				
				eventManager.getRequestUnknowCmdHandler().userRequestUnknowCmd(userKey, user, cmd, param);
				
			} else {
				
				userRequestFilter.doFilter(userKey, user, handler, requestId, param);
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * 用户cmd请求
	 * 
	 * @param requestId 请求Id
	 * @param cmd cmd
	 * @param user 用户
	 * @param param 请求参数
	 */
	public void handleUserRequest( int requestId, CmdType cmd, User user, Object param ) {
		
		if( user != null ) {
			
			@SuppressWarnings("rawtypes")
			IRequestHandler handler = userCmdMapHandler.get(cmd);
			if( handler == null ) {
				
				eventManager.getRequestUnknowCmdHandler().userRequestUnknowCmd(user.getUserKey(), user, cmd, param);
				
			} else {
				
				userRequestFilter.doFilter(user.getUserKey(), user, handler, requestId, param);
				
			}
			
		}
		
	}
	
}
