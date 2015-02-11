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
package org.mskcc.cbio.portal.authentication.googleplus;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * provide an implementation of a ConnectionSignup that facilitates finding a user id in
 * the user connection repository
 * @author criscuof
 *
 */
public final class GoogleplusConnectionSignUp implements ConnectionSignUp {

	/* (non-Javadoc)
	 * @see org.springframework.social.connect.ConnectionSignUp#execute(org.springframework.social.connect.Connection)
	 */
	@Override
	public String execute(Connection<?> connection) {
		Preconditions.checkArgument(null!=connection, "A Connection property is required");
		Preconditions.checkArgument(null != connection.getKey(), "The Connection must have a key");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(connection.getKey().getProviderUserId()), "The Connection key must have a provider user id");
		return connection.getKey().getProviderUserId();
		
	}

}
