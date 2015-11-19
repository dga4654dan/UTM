package com.dc.utm;

import java.util.Map;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;
import com.dc.utm.center.DataCenter;
import com.dc.utm.center.UserCenter;
import com.dc.utm.center.VisitorCenter;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.even.handler.IRequestUnknowCmdHandler;
import com.dc.utm.filter.user.IUserRequestFilter;
import com.dc.utm.filter.visitor.IVisitorRequestFilter;
import com.dc.utm.filter.visitor.VisitorRequestFilter;
import com.dc.utm.log.exception.IExceptionLogger;

public class UserThreadMode<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {
	
	protected final CmdType login;
	protected final CmdType disconect;
	
	protected final Map<CmdType, IRequestHandler<Visitor, ?>> visitorCmdMapHandler;
	protected final Map<CmdType, IRequestHandler<User, ?>> userCmdMapHandler;
	
	protected final VisitorCenter<ConnectKey, Visitor> visitorCenter;
	protected final UserCenter<UserKey, User> userCenter;

	protected final IVisitorRequestFilter<CmdType, ConnectKey, Visitor> visitorRequestFilter;
	protected final IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter;
	
	protected final IRequestUnknowCmdHandler<CmdType, ConnectKey, Visitor, UserKey, User> requestUnknowCmdHandler;
	
	protected final DataCenter<CmdType, ConnectKey, Visitor, UserKey, User> dataCenter;
	
	public UserThreadMode( int corePoolSize, int limitedQueueSize,
			CmdType login, CmdType disconect,
			Map<CmdType, IRequestHandler<Visitor, ?>> visitorCmdMapHandler,
			Map<CmdType, IRequestHandler<User, ?>> userCmdMapHandler,
			LimitedUnboundedThreadPoolExecutor pool,
			IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter,
			IRequestUnknowCmdHandler<CmdType, ConnectKey, Visitor, UserKey, User> requestUnknowCmdHandler,
			IExceptionLogger exceptionLogger ) {
		
		dataCenter = new DataCenter<CmdType, ConnectKey, Visitor, UserKey, User>();
		
		dataCenter.login = login;
		dataCenter.disconect = disconect;
		dataCenter.visitorCmdMapHandler = visitorCmdMapHandler;
		dataCenter.userCmdMapHandler = userCmdMapHandler;
		dataCenter.requestUnknowCmdHandler = requestUnknowCmdHandler;
		dataCenter.userRequestFilter = userRequestFilter;
		
		dataCenter.visitorCenter = new VisitorCenter<ConnectKey, Visitor>();
		dataCenter.userCenter = new UserCenter<UserKey, User>();
		
		dataCenter.visitorRequestFilter = 
				new VisitorRequestFilter<CmdType, ConnectKey, Visitor>( pool, exceptionLogger );

		
		this.login = login;
		this.disconect = disconect;
		this.visitorCmdMapHandler = visitorCmdMapHandler;
		this.userCmdMapHandler = userCmdMapHandler;
		this.requestUnknowCmdHandler = requestUnknowCmdHandler;
		this.userRequestFilter = userRequestFilter;
		
		
		this.visitorCenter = dataCenter.visitorCenter;
		this.userCenter = dataCenter.userCenter;
		this.visitorRequestFilter = dataCenter.visitorRequestFilter;
		
		
	}
	
	public DataCenter<CmdType, ConnectKey, Visitor, UserKey, User> getDataCenter() {
		
		return dataCenter;
	}
	
	public void connect( ConnectKey connectKey, Visitor visitor ) {
		
		visitorCenter.addVisitor(connectKey, visitor);
		
	}
	
	public void disconnect( int requestId, UserKey userKey ) {
		
		User user = userCenter.getUser( userKey );
		
		if( user != null )
			handleUserRequest(requestId, disconect, userKey, null);
	}
	
	@SuppressWarnings("unchecked")
	public void handleVisitorRequest( int requestId, CmdType cmd, ConnectKey connectKey, Object param ) {
		
		IRequestHandler<Visitor, ?> handler = visitorCmdMapHandler.get(cmd);
		Visitor visitor = visitorCenter.getVisitor(connectKey);
		if( visitor != null ) {
			
			if( handler == null ) {
				
				requestUnknowCmdHandler.visitorRequestUnknowCmd(connectKey, visitor, param);
				
			} else {
				
				visitorRequestFilter.doFilter(connectKey, visitor, (IRequestHandler<Visitor, Object>)handler, requestId, param);
				
			}
			
		}
	}
	
	public void handleUserRequest( int requestId, CmdType cmd, UserKey userKey, Object param ) {
		
		User user = userCenter.getUser( userKey );
		
		if( user != null ) {
			
			IRequestHandler<User, ?> handler = userCmdMapHandler.get(cmd);
			if( handler == null ) {
				
				requestUnknowCmdHandler.userRequestUnknowCmd(userKey, user, param);
				
			} else {
				
				userRequestFilter.doFilter(userKey, user, handler, requestId, param);
				
			}
			
		}
		
	}
	
	
}
