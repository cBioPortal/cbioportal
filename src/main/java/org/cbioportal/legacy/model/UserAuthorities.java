/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.model;

// imports
import java.io.Serializable;
import java.util.List;

/**
 * User authorites bean.
 *
 * @author Benjamin Gross
 */
public class UserAuthorities implements Serializable {

  private String email;
  private List<String> authorities;

  public UserAuthorities() {}

  /** Constructor. */
  public UserAuthorities(String email, List<String> authorities) {
    this.email = email;
    this.authorities = authorities;
  }

  // accessors
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email.toLowerCase();
  }

  public List<String> getAuthorities() {
    return authorities;
  }

  public void setAuthorities(List<String> authorities) {
    this.authorities = authorities;
  }
}
