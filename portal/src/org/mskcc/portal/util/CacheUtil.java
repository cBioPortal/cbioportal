package org.mskcc.portal.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CacheUtil
{
    public static final String encodeAlgorithm = "MD5";
    public static final String characterEncoding = "UTF-8";
    public static final int md5Base = 16;

    /**
	 * Generates an MD5 sum for the given string.
	 * 
	 * @param message	string to digested
	 * @return			MD5 sum corresponding to the given text
	 */
    public static String md5sum(String message)
    {
    	MessageDigest md5;
    	byte[] md5sumBytes;
    	String sum = null;
    	
        try {
			md5 = MessageDigest.getInstance(encodeAlgorithm);
			md5sumBytes = md5.digest(message.getBytes(characterEncoding));
			sum = (new BigInteger(1, md5sumBytes)).toString(md5Base);
		} catch (NoSuchAlgorithmException e) {
			sum = null;
		} catch (UnsupportedEncodingException e) {
			sum = null;
		}
        
        return sum;
    }

}
