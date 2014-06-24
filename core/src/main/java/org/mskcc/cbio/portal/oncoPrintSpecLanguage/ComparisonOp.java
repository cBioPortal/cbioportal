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

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.util.Arrays;

import org.mskcc.cbio.portal.util.EqualsUtil;

enum ComparisonOpDirection{
    Bigger,
    Smaller,
    Equal;
}

public enum ComparisonOp {
    GreaterEqual    (">=", ComparisonOpDirection.Bigger),
    Greater         (">", ComparisonOpDirection.Bigger),
    LessEqual       ("<=", ComparisonOpDirection.Smaller),
    Less            ("<", ComparisonOpDirection.Smaller),
    Equal           ("=", ComparisonOpDirection.Equal);

    private final String token;
    private final ComparisonOpDirection theComparisonOpDirection;
    ComparisonOp( String token, ComparisonOpDirection theComparisonOpDirection){
        this.token = token;
        this.theComparisonOpDirection = theComparisonOpDirection;
    }
    
    public ComparisonOpDirection getTheComparisonOpDirection() {
        return theComparisonOpDirection;
    }

    /**
     * map from an String ComparisonOp token to a ComparisonOp.
     * @param parsedToken
     * @return a ComparisonOp enum for the token
     * @throws IllegalArgumentException when the token isn't a valid token for a ComparisonOp
     */
    public static ComparisonOp convertCode( String parsedToken ) throws IllegalArgumentException{
        for( ComparisonOp v: ComparisonOp.values() ){
            if( v.token.equals(parsedToken) ){
                return v;
            }
        }
        throw new IllegalArgumentException( "Invalid parsedToken: " + parsedToken );
    }

    public String getToken() {
        return token;
    }
    
    /**
     * useful for testing.
     * @param aComparisonOp
     * @return
     */
    public ComparisonOp oppositeComparisonOp( ){
        switch (this) {
        case GreaterEqual:
            return Less;
        case Greater:
            return LessEqual;
        case LessEqual:
            return Greater;
        case Less:
            return GreaterEqual;
        case Equal:
            return Equal; //todo: not correct
        }
        // keep compiler happy
        (new UnreachableCodeException( "")).printStackTrace();
        return null;
    }

}    
