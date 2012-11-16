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
