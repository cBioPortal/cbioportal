package org.mskcc.portal.oncoPrintSpecLanguage;

import java.util.Arrays;

import org.mskcc.portal.util.EqualsUtil;

enum ComparisonOpDirection{
    Bigger,
    Smaller;
}

public enum ComparisonOp {
    GreaterEqual    (">=", ComparisonOpDirection.Bigger),
    Greater         (">", ComparisonOpDirection.Bigger),
    LessEqual       ("<=", ComparisonOpDirection.Smaller),
    Less            ("<", ComparisonOpDirection.Smaller);

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
        }
        // keep compiler happy
        (new UnreachableCodeException( "")).printStackTrace();
        System.exit(1);
        return null;
    }

}    
