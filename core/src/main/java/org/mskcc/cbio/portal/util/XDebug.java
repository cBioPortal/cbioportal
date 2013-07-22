/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.util;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * Encapsulates Real-time debugging information.
 * XDebug provides a simple facility for logging debug messages,
 * and recording debug parameters. Results of XDebug are conditionally
 * displayed at the bottom of the JSP page.
 */
public class XDebug {
    private ArrayList messages;
    private ArrayList parameters;
    private Date startTime;
    private Date stopTime;
    private long timeElapsed;
    private HttpServletRequest request;

    /**
     * Constructor.
     */
    public XDebug() {
       messages = new ArrayList();
       parameters = new ArrayList();
       startTime = null;
       stopTime = null;
       timeElapsed = -1;

   }
    
    // FOR ACCESS CONTROL, so CgdsProtocol can get the session; bit of a hack
    public XDebug(HttpServletRequest request) {
       this();
       this.request = request;

       // FOR ACCESS CONTROL, DEBUGGING
       if(UserInfo.isUserAuthenticated(request)) {
          this.logMsg( this, "Logged in with: " + UserInfo.getEmailId(request));
       }else{
          this.logMsg( this, "Not logged in." );
       }
       
   }

    public HttpServletRequest getRequest() {
      return request;
   }

   /**
     * Logs a new message with the specified color code.
     *
     * @param caller object that is making the log request
     * @param msg    message to log
     * @param color  color of message, e.g. "RED, "GREEN"
     */
    public void logMsg(Object caller, String msg, String color) {
        Class callerClass = caller.getClass();
        XDebugMessage message = new XDebugMessage(callerClass.getName(), msg, color);
        messages.add(message);
    }

    /**
     * Logs a new message.
     *
     * @param caller object that is making the log request
     * @param msg    message to log
     */
    public void logMsg(Object caller, String msg) {
        logMsg(caller, msg, "black");
    }

    /**
     * Adds a new String Parameter.
     *
     * @param type  parameter type code, e.g. COOKIE_TYPE, ENVIRONMENT_TYPE
     * @param name  parameter name
     * @param value parameter String value
     */
    public void addParameter(int type, String name, String value) {
        XDebugParameter param = new XDebugParameter(type, name, value);
        parameters.add(param);
    }

    /**
     * Adds a new integer Parameter.
     *
     * @param type  parameter type code, e.g. COOKIE_TYPE, ENVIRONMENT_TYPE
     * @param name  parameter name
     * @param value parameter integer value
     */
    public void addParameter(int type, String name, int value) {
        XDebugParameter param = new XDebugParameter(type, name, value);
        parameters.add(param);
    }

    /**
     * Adds a new boolean Parameter.
     *
     * @param type  parameter type code, e.g. COOKIE_TYPE, ENVIRONMENT_TYPE
     * @param name  parameter name
     * @param value parameter boolean value
     */
    public void addParameter(int type, String name, boolean value) {
        XDebugParameter param = new XDebugParameter(type, name, value);
        parameters.add(param);
    }

    /**
     * Gets all Debug Messages.
     *
     * @return Vector of XDebugMessage objects
     */
    public ArrayList getDebugMessages() {
        return messages;
    }

    /**
     * Gets all Parameters.
     *
     * @return Vector of XDebugParameter objects
     */
    public ArrayList getParameters() {
        return parameters;
    }

    /**
     * Starts the internal timer.
     */
    public void startTimer() {
        this.startTime = new Date();
    }

    /**
     * Stops the internal timer.
     */
    public void stopTimer() {
        this.stopTime = new Date();
        if (startTime != null) {
            this.timeElapsed = stopTime.getTime() - startTime.getTime();
        }
    }

    /**
     * Gets the total time elapsed (in milliseconds).
     *
     * @return totalTimeElapsed (ms)
     */
    public long getTimeElapsed() {
        return timeElapsed;
    }

    /**
     * Gets Complete Log.
     * Useful for command line utilities.
     *
     * @return Complete Log.
     */
    public String getCompleteLog() {
        StringBuffer log = new StringBuffer();
        if (messages == null || messages.size() == 0) {
            log.append("No Log Messages");
        }
        for (int i = 0; i < messages.size(); i++) {
            XDebugMessage msg = (XDebugMessage) messages.get(i);
            log.append(msg.getMessage() + "\n");
        }
        return log.toString();
    }
}
