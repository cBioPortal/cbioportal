package org.mskcc.portal.openIDlogin;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.httpclient.NameValuePair;
import org.mskcc.portal.util.XDebug;
import org.mskcc.portal.util.UserInfo;

/**
 * Service class for AccessControl.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class PortalAccessControl {
   private static final String EMAIL = "email_address"; // key for email param stored in session
   public static final String SECRET = "secret_key"; // key for secret key param stored in session
   
   public static NameValuePair[] addAccessParams( HttpServletRequest request, NameValuePair[] data ){
      
      // if the request isn't available (as when testing) make no changes
      if( null == request ){
         return data;
      }
      // TODO: Later: ACCESS CONTROL: move elsewhere
      request.getSession().setAttribute( PortalAccessControl.SECRET, "Arthur's1STroNgKee" );

      // TODO: Later: ACCESS CONTROL: don't add email and key if command doesn't need them
      // ACCESS CONTROL: IF EMAIL SET, ADD IT AND KEY TO request params
      if( null != request.getSession().getAttribute( PortalAccessControl.EMAIL ) ){
         NameValuePair[] newData = new NameValuePair[ data.length + 2 ]; 
         System.arraycopy(data, 0, newData, 0, data.length );

         int i = data.length;
         // email
         newData[ i++ ] = new NameValuePair( EMAIL, UserInfo.getEmailId(request));
         // key
         newData[ i++ ] = new NameValuePair( SECRET, UserInfo.getEmailId(request));
         return newData;
      }
      return data;

   }
   
   public static void createDebugMessages( Object o, XDebug xdebug, NameValuePair[] data ){
      xdebug.logMsg(o, "CGDS URL:" );
      for( NameValuePair nvp: data ){
         StringBuffer buf = new StringBuffer(); 
         buf.append( nvp.getName() + ": " + nvp.getValue() + "\n" );
         xdebug.logMsg(o, buf.toString() );
      }
   }

   public static String urlRequest( NameValuePair[] data ){
      StringBuffer buf = new StringBuffer(); 
      for( NameValuePair nvp: data ){
         buf.append( nvp.getName() + ": " + nvp.getValue() + "\n" );
      }
      return buf.toString();
   }

}