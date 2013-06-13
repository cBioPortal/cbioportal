package org.mskcc.cbio.importer.util.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

public class HeaderHandlerResolver implements HandlerResolver
{
	private HeaderHandler securityHandler;

	public HeaderHandler getSecurityHandler()
	{
		return securityHandler;
	}

	public HeaderHandlerResolver()
	{
		super();

		this.securityHandler = new HeaderHandler();
	}

	public List<Handler> getHandlerChain(PortInfo portInfo)
	{
		List<Handler> handlerChain = new ArrayList<Handler>();
		handlerChain.add(this.securityHandler);

		return handlerChain;
	}
}
