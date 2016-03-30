package org.mskcc.cbio.portal.util.internal;

import org.mskcc.cbio.portal.util.UserDetails;

public class SpringUserDetailsWrapper implements UserDetails {
	
	private UserDetails details = null;
	
	SpringUserDetailsWrapper(Object d) { 
		if (d instanceof UserDetails) {
			details = (UserDetails) d;
		}
	}

	@Override
	public String getUsername() {
		if (details == null) {
			return null;
		} else {
			return details.getUsername();
		}
	}

}
