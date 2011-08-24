package org.mskcc.cgds.model;

/**
 * The secret key that a client must have to register users in cgds.
 * keys are secured by JASYPT by hashing repeatedly with a random salt.
 * See: http://www.jasypt.org/howtoencryptuserpasswords.html
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 *
 */
public class SecretKey {
   
   String encryptedKey;
   
   public String getEncryptedKey() {
      return encryptedKey;
   }

   public void setEncryptedKey(String encryptedKey) {
      this.encryptedKey = encryptedKey;
   }
   
}