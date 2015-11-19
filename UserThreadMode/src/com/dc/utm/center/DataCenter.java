package com.dc.utm.center;

import java.util.Map;

import com.dc.qtm.handle.IRequestHandler;
import com.dc.utm.entity.IBaseUser;
import com.dc.utm.even.handler.IRequestUnknowCmdHandler;
import com.dc.utm.filter.user.IUserRequestFilter;
import com.dc.utm.filter.visitor.IVisitorRequestFilter;
import com.dc.utm.log.exception.IExceptionLogger;
import com.dc.utm.user.flag.UserFlagBusiness;
import com.dc.utm.user.logout.UserLogoutCheckBusiness;
import com.dc.utm.user.queue.UserTaskQueueBusiness;

public class DataCenter<CmdType, ConnectKey, Visitor, UserKey, User extends IBaseUser<UserKey>> {

	public CmdType login;
	public CmdType disconect;
	
	public Map<CmdType, IRequestHandler<Visitor, ?>> visitorCmdMapHandler;
	public Map<CmdType, IRequestHandler<User, ?>> userCmdMapHandler;
	
	public IRequestUnknowCmdHandler<CmdType, ConnectKey, Visitor, UserKey, User> requestUnknowCmdHandler;
	
	public UserCenter<UserKey, User> userCenter;
	public VisitorCenter<ConnectKey, Visitor> visitorCenter;
	
	public IVisitorRequestFilter<CmdType, ConnectKey, Visitor> visitorRequestFilter;
	public IUserRequestFilter<CmdType, ConnectKey, Visitor, UserKey, User> userRequestFilter;
	
	
	
	
	public UserTaskQueueBusiness<UserKey, User> userTaskQueueBusiness;
	
	public IRequestHandler<IBaseUser<UserKey>, Object> onUserLoginHandler;
	
	public UserLogoutCheckBusiness<CmdType, ConnectKey, Visitor, UserKey, User> userLogoutCheckBusiness;
	
	public UserFlagBusiness<Visitor, UserKey, User> userFlagBusiness;
	
	public IExceptionLogger exceptionLogger;
	
}
