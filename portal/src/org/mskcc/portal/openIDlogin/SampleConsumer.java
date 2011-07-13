package org.mskcc.portal.openIDlogin;

/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;

/**
 * Sample Consumer (Relying Party) implementation. Based on SampleConsumer from
 * openid4java.
 * 
 * OpenID Relying Party top-level interface.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class SampleConsumer {
   // SampleConsumer singleton, based on the guidance in 
   // http://www.javaworld.com/javaworld/jw-04-2003/jw-0425-designpatterns.html?page=5
   // TODO: fix, this isn't thread-safe, because multiple clients may use it concurrently
   public final static SampleConsumer INSTANCE = new SampleConsumer();

   private SampleConsumer() {
      // Exists only to defeat instantiation.
   }

   private ConsumerManager        manager;
   private String                 returnToUrl;
   public HashMap<String, String> authRequestData;

   /**
    * Set the URL which will receive the authentication responses from the
    * OpenID provider.
    * 
    * @param returnToUrl
    * @throws ConsumerException
    */
   public void setHandlerURL(String returnToUrl) throws ConsumerException {
      // configure the return_to URL where your application will receive
      // the authentication responses from the OpenID provider
      this.returnToUrl = returnToUrl;

      // instantiate a ConsumerManager object
      manager = new ConsumerManager();
      manager.setAssociations(new InMemoryConsumerAssociationStore());
      manager.setNonceVerifier(new InMemoryNonceVerifier(5000));

      // for a working demo, not enforcing RP realm discovery
      // since this new feature is not deployed
      manager.getRealmVerifier().setEnforceRpId(false);
   }

   /**
    * Send an authentication request to an OpenID provider.
    * 
    * @param userSuppliedOpenID
    * @param httpReq http request for an OpenID login 
    * @param httpResp http response to this request
    * @param out  output to the browser, just for debugging
    * @throws IOException
    * @throws DiscoveryException if discovery fails to obtain a service endpoint for authentication with the OpenID provider
    */
   public void authRequest(String userSuppliedOpenID, HttpServletRequest httpReq, HttpServletResponse httpResp,
            PrintWriter out) throws IOException, DiscoveryException {

      // --- Forward proxy setup (only if needed) ---
      // ProxyProperties proxyProps = new ProxyProperties();
      // proxyProps.setProxyName("proxy.example.com");
      // proxyProps.setProxyPort(8080);
      // HttpClientFactory.setProxyProperties(proxyProps);

      // out.println( "<p>In SampleConsumer.authRequest" );

      // perform discovery on the user-supplied identifier
      List<DiscoveryInformation> discoveries = manager.discover(userSuppliedOpenID);
      /*
       * debugging
      for (DiscoveryInformation di : discoveries) {
         if (di.hasClaimedIdentifier()) {
            Identifier ClaimedIdentifier = di.getClaimedIdentifier();
            if (null != ClaimedIdentifier && null != ClaimedIdentifier.getIdentifier()) {
               out.println("<br>ClaimedIdentifier.getIdentifier(): " + ClaimedIdentifier.getIdentifier());
            }
         }
      }
       */

      try {
         // attempt to associate with the OpenID provider
         // and retrieve one service endpoint for authentication
         DiscoveryInformation discovered = manager.associate(discoveries);

         // store the discovery information in the user's session
         httpReq.getSession().setAttribute("openid-disc", discovered);

         // obtain a AuthRequest message to be sent to the OpenID provider
         AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

         // Attribute Exchange example: fetching the 'email' attribute
         final String yahooEndpoint = "https://me.yahoo.com";
         final String googleEndpoint = "https://www.google.com";

         FetchRequest fetch = FetchRequest.createFetchRequest();
         if (userSuppliedOpenID.startsWith(googleEndpoint)) {
            fetch.addAttribute("email", "http://axschema.org/contact/email", true);
            fetch.addAttribute("firstName", "http://axschema.org/namePerson/first", true);
            fetch.addAttribute("lastName", "http://axschema.org/namePerson/last", true);
         } else if (userSuppliedOpenID.startsWith(yahooEndpoint)) {
            fetch.addAttribute("email", "http://axschema.org/contact/email", true);
            fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
         } else { // works for myOpenID
            // TODO: what about other providers?
            fetch.addAttribute("fullname", "http://schema.openid.net/namePerson", true);
            fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
         }
         // attach the extension to the authentication request
         authReq.addExtension(fetch);

         // example using Simple Registration to fetch the 'email' attribute
         SRegRequest sregReq = SRegRequest.createFetchRequest();
         sregReq.addAttribute("email", true);
         authReq.addExtension(sregReq);

         /*
          * debugging
         out.println("<br>discovered.isVersion2(): " + discovered.isVersion2());
         out.println("<p>Calling: httpResp.sendRedirect(authReq.getDestinationUrl(true))");
         */
         httpResp.sendRedirect(authReq.getDestinationUrl(true));

      } catch (OpenIDException e) {
         // present error to the user
         out.println("<br>OpenIDException");
         throw new RuntimeException("wrap:" + e.getMessage(), e);
      }
   }

   /**
    * Process the authentication response. 
    * 
    * @param httpReq
    * @return
    */
   public Identifier verifyResponse(HttpServletRequest httpReq) {

      try {
         // extract the parameters from the authentication response
         // (which comes in as a HTTP request from the OpenID provider)
         ParameterList response = new ParameterList(httpReq.getParameterMap());

         // retrieve the previously stored discovery information
         DiscoveryInformation discovered = (DiscoveryInformation) httpReq.getSession().getAttribute("openid-disc");

         // extract the receiving URL from the HTTP request
         StringBuffer receivingURL = httpReq.getRequestURL();
         String queryString = httpReq.getQueryString();
         if (queryString != null && queryString.length() > 0)
            receivingURL.append("?").append(httpReq.getQueryString());

         // verify the response; ConsumerManager needs to be the same
         // (static) instance used to place the authentication request
         VerificationResult verification = manager.verify(receivingURL.toString(), response, discovered);

         // examine the verification result and extract the verified identifier
         Identifier verified = verification.getVerifiedId();
         this.authRequestData = new HashMap<String, String>();
         authRequestData.put("verified", "no");
         if (verified != null) {
            AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

            HttpSession session = httpReq.getSession(true);
            authRequestData.put("verified", "yes");

            session.setAttribute("openid_identifier", authSuccess.getIdentity());
            authRequestData.put("openid_identifier", authSuccess.getIdentity());
            authRequestData.put("Nonce", authSuccess.getNonce() );

            if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
               FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
               session.setAttribute("emailFromFetch", fetchResp.getAttributeValues("email").get(0));
               authRequestData.put("emailFromFetch", (String) fetchResp.getAttributeValues("email").get(0));
            }
            if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
               SRegResponse sregResp = (SRegResponse) authSuccess.getExtension(SRegMessage.OPENID_NS_SREG);
               session.setAttribute("emailFromSReg", sregResp.getAttributeValue("email"));
               authRequestData.put("emailFromSReg", sregResp.getAttributeValue("email"));
            }
            return verified; // success
         }
      } catch (OpenIDException e) {
         // present error to the user
         throw new RuntimeException("wrap: " + e.getMessage(), e);
      }

      return null;
   }
}
