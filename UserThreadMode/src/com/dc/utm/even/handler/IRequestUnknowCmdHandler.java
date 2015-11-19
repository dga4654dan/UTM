package com.dc.utm.even.handler;

public interface IRequestUnknowCmdHandler<CmdType, ConnectKey, Visitor, UserKey, User> {

	void visitorRequestUnknowCmd( ConnectKey connectKey, Visitor visitor, Object param );
	
	void userRequestUnknowCmd( UserKey userKey, User user, Object param );
}
