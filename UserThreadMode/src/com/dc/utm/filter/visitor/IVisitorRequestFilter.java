package com.dc.utm.filter.visitor;

import com.dc.qtm.handle.IRequestHandler;

public interface IVisitorRequestFilter<CmdType, ConnectKey, Visitor>  {

	void doFilter( ConnectKey connectKey, Visitor visitor, IRequestHandler<Visitor, Object> handler, int requestId, Object param );
	
}
