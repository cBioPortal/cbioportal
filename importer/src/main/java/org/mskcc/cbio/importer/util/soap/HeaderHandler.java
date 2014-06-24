package org.mskcc.cbio.importer.util.soap;

import java.util.Set;
import java.util.HashSet;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Handler class to add authentication information into the SOAP header.
 */
public class HeaderHandler implements SOAPHandler<SOAPMessageContext>
{
	// service username
	private String serviceUser;

	public void setServiceUser(String serviceUser)
	{
		this.serviceUser = serviceUser;
	}

	// service password
	private String servicePassword;

	public void setServicePassword(String servicePassword)
	{
		this.servicePassword = servicePassword;
	}

    public boolean handleMessage(SOAPMessageContext smc) {

        Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty) {

            SOAPMessage message = smc.getMessage();

            try {

				SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();
                SOAPHeader header = envelope.addHeader();

				// name of element ----  namespace prefix ----  uri of the name space

                SOAPElement security =
					header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");

                SOAPElement usernameToken =
					security.addChildElement("UsernameToken", "wsse");
                usernameToken.addAttribute(new QName("xmlns:wsu"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

                SOAPElement username =
					usernameToken.addChildElement("Username", "wsse");
                username.addTextNode(this.serviceUser);

                SOAPElement password =
					usernameToken.addChildElement("Password", "wsse");
                password.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
				password.addTextNode(this.servicePassword);

                //Print out the outbound SOAP message to System.out
                //message.writeTo(System.out);
                //System.out.println("");
                
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                
                //This handler does nothing with the response from the Web Service so
                //we just print out the SOAP message.
                SOAPMessage message = smc.getMessage();
                //message.writeTo(System.out);
                //System.out.println("");

            } catch (Exception ex) {
                ex.printStackTrace();
            } 
        }

        return outboundProperty;
    }

    public Set getHeaders()
    {
		final QName securityHeader = new QName(
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
			"Security",
			"wsse");

		final HashSet headers = new HashSet();
        headers.add(securityHeader);
 
        // notify the runtime that this is handled
        return headers;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {}
}
