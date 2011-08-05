package org.mskcc.cgds.model;

import org.mskcc.cgds.util.EqualsUtil;

/**
 * This represents a user, identified by an email address.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class User {
	private String email;
	private String name;     // optional
    private boolean enabled;
	private String consumerKey;
	private String consumerSecret;

	public User() {
		this.name = "";
		this.enabled = false;
	}

	public User(String email, String name) throws IllegalArgumentException {
		this(email, name, false, "consumer_key", "consumer_secret");
	}

	public User(String email, String name, boolean enabled, String consumerKey, String consumerSecret) throws IllegalArgumentException {
		this();
		if (null == email) {
			throw new IllegalArgumentException ("email is null.");
		}
		this.email = email;
		if (null != name) {
			this.name = name;
		}
		this.enabled = enabled;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	@Override
	public boolean equals(Object otherUser) {
		if (this == otherUser)
			return true;
		if (!(otherUser instanceof User))
			return false;
		User that = (User) otherUser;
		return EqualsUtil.areEqual(this.email, that.email) 
			&& EqualsUtil.areEqual(this.name, that.name);
	}
}