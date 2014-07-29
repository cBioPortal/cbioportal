/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.util;

/**
 * A Java Singleton implemented as an enum to support a common dynamic state across the cBioPortal application
 * @author criscuof
 */
public enum DynamicState {
    INSTANCE;
    
    private String currentUser = "";
    
    public void setCurrentUser(String aUser) {
        // n.b allow property yo be set to null or empty
        currentUser = aUser;
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    
}
