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

   public User() {
      this.name = "";
   }

   public User(String email, String name) throws IllegalArgumentException {
      this();
      if( null == email ){
         throw new IllegalArgumentException ("email is null.");
      }
      this.email = email;
      if( null != name ){
         this.name = name;
      }
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