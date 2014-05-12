/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.portal.util.EqualsUtil;

/**
 * This represents a user, identified by an email address.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Benjamin Gross
 */
public class User {
	private String email;
	private String name;
    private boolean enabled;

	public User(String email, String name, boolean enabled) {
		if (null == email) {
			throw new IllegalArgumentException ("email is null.");
		}
		this.email = email;
		if (null == name) {
            throw new IllegalArgumentException ("name is null.");
		}
        this.name = name;
		this.enabled = enabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean equals(Object otherUser) {
		if (this == otherUser) {
			return true;
                }
                
		if (!(otherUser instanceof User)) {
			return false;
                }
                
		User that = (User) otherUser;
		return EqualsUtil.areEqual(this.email, that.email) 
			&& EqualsUtil.areEqual(this.name, that.name);
	}

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.email != null ? this.email.hashCode() : 0);
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}