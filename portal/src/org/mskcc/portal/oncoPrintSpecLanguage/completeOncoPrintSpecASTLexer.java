// $ANTLR 3.2 Sep 23, 2009 12:02:23 ./completeOncoPrintSpecAST.g 2012-05-30 15:37:48

	package org.mskcc.portal.oncoPrintSpecLanguage;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class completeOncoPrintSpecASTLexer extends Lexer {
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int UserGeneList=5;
    public static final int T__21=21;
    public static final int COMPARISON_OP=10;
    public static final int UNICODE_ESC=18;
    public static final int OCTAL_ESC=19;
    public static final int HEX_DIGIT=20;
    public static final int INT=15;
    public static final int ID=6;
    public static final int EOF=-1;
    public static final int DiscreteDataType=12;
    public static final int ContinuousDataTypeInequality=14;
    public static final int SIGNED_INT=11;
    public static final int WS=16;
    public static final int ESC_SEQ=17;
    public static final int DataTypeOrLevel=9;
    public static final int DefaultDataTypeSpec=8;
    public static final int IndividualGene=7;
    public static final int SIGNED_FLOAT=13;
    public static final int STRING=4;

    // delegates
    // delegators

    public completeOncoPrintSpecASTLexer() {;} 
    public completeOncoPrintSpecASTLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public completeOncoPrintSpecASTLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "./completeOncoPrintSpecAST.g"; }

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:7:7: ( '{' )
            // ./completeOncoPrintSpecAST.g:7:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:8:7: ( '}' )
            // ./completeOncoPrintSpecAST.g:8:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:9:7: ( 'DATATYPES' )
            // ./completeOncoPrintSpecAST.g:9:9: 'DATATYPES'
            {
            match("DATATYPES"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:10:7: ( ':' )
            // ./completeOncoPrintSpecAST.g:10:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:11:7: ( ';' )
            // ./completeOncoPrintSpecAST.g:11:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "UserGeneList"
    public final void mUserGeneList() throws RecognitionException {
        try {
            int _type = UserGeneList;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:138:14: ( '&userGeneList' )
            // ./completeOncoPrintSpecAST.g:138:17: '&userGeneList'
            {
            match("&userGeneList"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UserGeneList"

    // $ANTLR start "IndividualGene"
    public final void mIndividualGene() throws RecognitionException {
        try {
            int _type = IndividualGene;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:155:2: ( '&individualGene' )
            // ./completeOncoPrintSpecAST.g:155:4: '&individualGene'
            {
            match("&individualGene"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IndividualGene"

    // $ANTLR start "DefaultDataTypeSpec"
    public final void mDefaultDataTypeSpec() throws RecognitionException {
        try {
            int _type = DefaultDataTypeSpec;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:163:2: ( '&defaultDataTypeSpec' )
            // ./completeOncoPrintSpecAST.g:163:4: '&defaultDataTypeSpec'
            {
            match("&defaultDataTypeSpec"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DefaultDataTypeSpec"

    // $ANTLR start "DataTypeOrLevel"
    public final void mDataTypeOrLevel() throws RecognitionException {
        try {
            int _type = DataTypeOrLevel;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:179:2: ( '&DataTypeOrLevel' )
            // ./completeOncoPrintSpecAST.g:179:4: '&DataTypeOrLevel'
            {
            match("&DataTypeOrLevel"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DataTypeOrLevel"

    // $ANTLR start "DiscreteDataType"
    public final void mDiscreteDataType() throws RecognitionException {
        try {
            int _type = DiscreteDataType;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:187:19: ( '&DiscreteDataType' )
            // ./completeOncoPrintSpecAST.g:187:21: '&DiscreteDataType'
            {
            match("&DiscreteDataType"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DiscreteDataType"

    // $ANTLR start "ContinuousDataTypeInequality"
    public final void mContinuousDataTypeInequality() throws RecognitionException {
        try {
            int _type = ContinuousDataTypeInequality;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:200:31: ( '&ContinuousDataTypeInequality' )
            // ./completeOncoPrintSpecAST.g:200:33: '&ContinuousDataTypeInequality'
            {
            match("&ContinuousDataTypeInequality"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ContinuousDataTypeInequality"

    // $ANTLR start "COMPARISON_OP"
    public final void mCOMPARISON_OP() throws RecognitionException {
        try {
            int _type = COMPARISON_OP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:203:2: ( ( '<=' | '<' | '>' | '>=' | '=' ) )
            // ./completeOncoPrintSpecAST.g:203:4: ( '<=' | '<' | '>' | '>=' | '=' )
            {
            // ./completeOncoPrintSpecAST.g:203:4: ( '<=' | '<' | '>' | '>=' | '=' )
            int alt1=5;
            switch ( input.LA(1) ) {
            case '<':
                {
                int LA1_1 = input.LA(2);

                if ( (LA1_1=='=') ) {
                    alt1=1;
                }
                else {
                    alt1=2;}
                }
                break;
            case '>':
                {
                int LA1_2 = input.LA(2);

                if ( (LA1_2=='=') ) {
                    alt1=4;
                }
                else {
                    alt1=3;}
                }
                break;
            case '=':
                {
                alt1=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // ./completeOncoPrintSpecAST.g:203:6: '<='
                    {
                    match("<="); 


                    }
                    break;
                case 2 :
                    // ./completeOncoPrintSpecAST.g:203:13: '<'
                    {
                    match('<'); 

                    }
                    break;
                case 3 :
                    // ./completeOncoPrintSpecAST.g:203:19: '>'
                    {
                    match('>'); 

                    }
                    break;
                case 4 :
                    // ./completeOncoPrintSpecAST.g:203:25: '>='
                    {
                    match(">="); 


                    }
                    break;
                case 5 :
                    // ./completeOncoPrintSpecAST.g:203:32: '='
                    {
                    match('='); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPARISON_OP"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:206:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '-' | '*' | '/' )* )
            // ./completeOncoPrintSpecAST.g:206:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '-' | '*' | '/' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // ./completeOncoPrintSpecAST.g:206:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '-' | '*' | '/' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='*'||LA2_0=='-'||(LA2_0>='/' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ./completeOncoPrintSpecAST.g:
            	    {
            	    if ( input.LA(1)=='*'||input.LA(1)=='-'||(input.LA(1)>='/' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "SIGNED_FLOAT"
    public final void mSIGNED_FLOAT() throws RecognitionException {
        try {
            int _type = SIGNED_FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:210:5: ( ( '-' )? ( ( INT )+ '.' ( INT )* | '.' ( INT )+ ) )
            // ./completeOncoPrintSpecAST.g:210:8: ( '-' )? ( ( INT )+ '.' ( INT )* | '.' ( INT )+ )
            {
            // ./completeOncoPrintSpecAST.g:210:8: ( '-' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='-') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // ./completeOncoPrintSpecAST.g:210:9: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // ./completeOncoPrintSpecAST.g:210:15: ( ( INT )+ '.' ( INT )* | '.' ( INT )+ )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>='0' && LA7_0<='9')) ) {
                alt7=1;
            }
            else if ( (LA7_0=='.') ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // ./completeOncoPrintSpecAST.g:210:17: ( INT )+ '.' ( INT )*
                    {
                    // ./completeOncoPrintSpecAST.g:210:17: ( INT )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // ./completeOncoPrintSpecAST.g:210:17: INT
                    	    {
                    	    mINT(); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt4 >= 1 ) break loop4;
                                EarlyExitException eee =
                                    new EarlyExitException(4, input);
                                throw eee;
                        }
                        cnt4++;
                    } while (true);

                    match('.'); 
                    // ./completeOncoPrintSpecAST.g:210:26: ( INT )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // ./completeOncoPrintSpecAST.g:210:26: INT
                    	    {
                    	    mINT(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // ./completeOncoPrintSpecAST.g:211:8: '.' ( INT )+
                    {
                    match('.'); 
                    // ./completeOncoPrintSpecAST.g:211:12: ( INT )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // ./completeOncoPrintSpecAST.g:211:12: INT
                    	    {
                    	    mINT(); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt6 >= 1 ) break loop6;
                                EarlyExitException eee =
                                    new EarlyExitException(6, input);
                                throw eee;
                        }
                        cnt6++;
                    } while (true);


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SIGNED_FLOAT"

    // $ANTLR start "SIGNED_INT"
    public final void mSIGNED_INT() throws RecognitionException {
        try {
            int _type = SIGNED_INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:214:12: ( ( '-' )? ( INT )+ )
            // ./completeOncoPrintSpecAST.g:214:14: ( '-' )? ( INT )+
            {
            // ./completeOncoPrintSpecAST.g:214:14: ( '-' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='-') ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // ./completeOncoPrintSpecAST.g:214:15: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // ./completeOncoPrintSpecAST.g:214:21: ( INT )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='0' && LA9_0<='9')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // ./completeOncoPrintSpecAST.g:214:21: INT
            	    {
            	    mINT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SIGNED_INT"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            // ./completeOncoPrintSpecAST.g:216:14: ( ( '0' .. '9' ) )
            // ./completeOncoPrintSpecAST.g:216:16: ( '0' .. '9' )
            {
            // ./completeOncoPrintSpecAST.g:216:16: ( '0' .. '9' )
            // ./completeOncoPrintSpecAST.g:216:17: '0' .. '9'
            {
            matchRange('0','9'); 

            }


            }

        }
        finally {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:218:4: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // ./completeOncoPrintSpecAST.g:218:8: ( ' ' | '\\t' | '\\r' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ./completeOncoPrintSpecAST.g:226:5: ( '\"' ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )* '\"' )
            // ./completeOncoPrintSpecAST.g:226:8: '\"' ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 
            // ./completeOncoPrintSpecAST.g:226:12: ( ESC_SEQ | ~ ( '\\\\' | '\"' ) )*
            loop10:
            do {
                int alt10=3;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='\\') ) {
                    alt10=1;
                }
                else if ( ((LA10_0>='\u0000' && LA10_0<='!')||(LA10_0>='#' && LA10_0<='[')||(LA10_0>=']' && LA10_0<='\uFFFF')) ) {
                    alt10=2;
                }


                switch (alt10) {
            	case 1 :
            	    // ./completeOncoPrintSpecAST.g:226:14: ESC_SEQ
            	    {
            	    mESC_SEQ(); 

            	    }
            	    break;
            	case 2 :
            	    // ./completeOncoPrintSpecAST.g:226:24: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "ESC_SEQ"
    public final void mESC_SEQ() throws RecognitionException {
        try {
            // ./completeOncoPrintSpecAST.g:231:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
            int alt11=3;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\\') ) {
                switch ( input.LA(2) ) {
                case '\"':
                case '\'':
                case '\\':
                case 'b':
                case 'f':
                case 'n':
                case 'r':
                case 't':
                    {
                    alt11=1;
                    }
                    break;
                case 'u':
                    {
                    alt11=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    alt11=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // ./completeOncoPrintSpecAST.g:231:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 
                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // ./completeOncoPrintSpecAST.g:232:9: UNICODE_ESC
                    {
                    mUNICODE_ESC(); 

                    }
                    break;
                case 3 :
                    // ./completeOncoPrintSpecAST.g:233:9: OCTAL_ESC
                    {
                    mOCTAL_ESC(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "ESC_SEQ"

    // $ANTLR start "OCTAL_ESC"
    public final void mOCTAL_ESC() throws RecognitionException {
        try {
            // ./completeOncoPrintSpecAST.g:238:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt12=3;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='\\') ) {
                int LA12_1 = input.LA(2);

                if ( ((LA12_1>='0' && LA12_1<='3')) ) {
                    int LA12_2 = input.LA(3);

                    if ( ((LA12_2>='0' && LA12_2<='7')) ) {
                        int LA12_5 = input.LA(4);

                        if ( ((LA12_5>='0' && LA12_5<='7')) ) {
                            alt12=1;
                        }
                        else {
                            alt12=2;}
                    }
                    else {
                        alt12=3;}
                }
                else if ( ((LA12_1>='4' && LA12_1<='7')) ) {
                    int LA12_3 = input.LA(3);

                    if ( ((LA12_3>='0' && LA12_3<='7')) ) {
                        alt12=2;
                    }
                    else {
                        alt12=3;}
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // ./completeOncoPrintSpecAST.g:238:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // ./completeOncoPrintSpecAST.g:238:14: ( '0' .. '3' )
                    // ./completeOncoPrintSpecAST.g:238:15: '0' .. '3'
                    {
                    matchRange('0','3'); 

                    }

                    // ./completeOncoPrintSpecAST.g:238:25: ( '0' .. '7' )
                    // ./completeOncoPrintSpecAST.g:238:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // ./completeOncoPrintSpecAST.g:238:36: ( '0' .. '7' )
                    // ./completeOncoPrintSpecAST.g:238:37: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 2 :
                    // ./completeOncoPrintSpecAST.g:239:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // ./completeOncoPrintSpecAST.g:239:14: ( '0' .. '7' )
                    // ./completeOncoPrintSpecAST.g:239:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // ./completeOncoPrintSpecAST.g:239:25: ( '0' .. '7' )
                    // ./completeOncoPrintSpecAST.g:239:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 3 :
                    // ./completeOncoPrintSpecAST.g:240:9: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 
                    // ./completeOncoPrintSpecAST.g:240:14: ( '0' .. '7' )
                    // ./completeOncoPrintSpecAST.g:240:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "OCTAL_ESC"

    // $ANTLR start "UNICODE_ESC"
    public final void mUNICODE_ESC() throws RecognitionException {
        try {
            // ./completeOncoPrintSpecAST.g:245:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // ./completeOncoPrintSpecAST.g:245:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            {
            match('\\'); 
            match('u'); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "UNICODE_ESC"

    // $ANTLR start "HEX_DIGIT"
    public final void mHEX_DIGIT() throws RecognitionException {
        try {
            // ./completeOncoPrintSpecAST.g:249:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // ./completeOncoPrintSpecAST.g:249:13: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HEX_DIGIT"

    public void mTokens() throws RecognitionException {
        // ./completeOncoPrintSpecAST.g:1:8: ( T__21 | T__22 | T__23 | T__24 | T__25 | UserGeneList | IndividualGene | DefaultDataTypeSpec | DataTypeOrLevel | DiscreteDataType | ContinuousDataTypeInequality | COMPARISON_OP | ID | SIGNED_FLOAT | SIGNED_INT | WS | STRING )
        int alt13=17;
        alt13 = dfa13.predict(input);
        switch (alt13) {
            case 1 :
                // ./completeOncoPrintSpecAST.g:1:10: T__21
                {
                mT__21(); 

                }
                break;
            case 2 :
                // ./completeOncoPrintSpecAST.g:1:16: T__22
                {
                mT__22(); 

                }
                break;
            case 3 :
                // ./completeOncoPrintSpecAST.g:1:22: T__23
                {
                mT__23(); 

                }
                break;
            case 4 :
                // ./completeOncoPrintSpecAST.g:1:28: T__24
                {
                mT__24(); 

                }
                break;
            case 5 :
                // ./completeOncoPrintSpecAST.g:1:34: T__25
                {
                mT__25(); 

                }
                break;
            case 6 :
                // ./completeOncoPrintSpecAST.g:1:40: UserGeneList
                {
                mUserGeneList(); 

                }
                break;
            case 7 :
                // ./completeOncoPrintSpecAST.g:1:53: IndividualGene
                {
                mIndividualGene(); 

                }
                break;
            case 8 :
                // ./completeOncoPrintSpecAST.g:1:68: DefaultDataTypeSpec
                {
                mDefaultDataTypeSpec(); 

                }
                break;
            case 9 :
                // ./completeOncoPrintSpecAST.g:1:88: DataTypeOrLevel
                {
                mDataTypeOrLevel(); 

                }
                break;
            case 10 :
                // ./completeOncoPrintSpecAST.g:1:104: DiscreteDataType
                {
                mDiscreteDataType(); 

                }
                break;
            case 11 :
                // ./completeOncoPrintSpecAST.g:1:121: ContinuousDataTypeInequality
                {
                mContinuousDataTypeInequality(); 

                }
                break;
            case 12 :
                // ./completeOncoPrintSpecAST.g:1:150: COMPARISON_OP
                {
                mCOMPARISON_OP(); 

                }
                break;
            case 13 :
                // ./completeOncoPrintSpecAST.g:1:164: ID
                {
                mID(); 

                }
                break;
            case 14 :
                // ./completeOncoPrintSpecAST.g:1:167: SIGNED_FLOAT
                {
                mSIGNED_FLOAT(); 

                }
                break;
            case 15 :
                // ./completeOncoPrintSpecAST.g:1:180: SIGNED_INT
                {
                mSIGNED_INT(); 

                }
                break;
            case 16 :
                // ./completeOncoPrintSpecAST.g:1:191: WS
                {
                mWS(); 

                }
                break;
            case 17 :
                // ./completeOncoPrintSpecAST.g:1:194: STRING
                {
                mSTRING(); 

                }
                break;

        }

    }


    protected DFA13 dfa13 = new DFA13(this);
    static final String DFA13_eotS =
        "\3\uffff\1\10\6\uffff\1\24\3\uffff\1\10\6\uffff\1\10\2\uffff\5\10"+
        "\1\36\1\uffff";
    static final String DFA13_eofS =
        "\37\uffff";
    static final String DFA13_minS =
        "\1\11\2\uffff\1\101\2\uffff\1\103\2\uffff\2\56\3\uffff\1\124\3\uffff"+
        "\1\141\2\uffff\1\101\2\uffff\1\124\1\131\1\120\1\105\1\123\1\52"+
        "\1\uffff";
    static final String DFA13_maxS =
        "\1\175\2\uffff\1\101\2\uffff\1\165\2\uffff\2\71\3\uffff\1\124\3"+
        "\uffff\1\151\2\uffff\1\101\2\uffff\1\124\1\131\1\120\1\105\1\123"+
        "\1\172\1\uffff";
    static final String DFA13_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\uffff\1\14\1\15\2\uffff\1\16"+
        "\1\20\1\21\1\uffff\1\6\1\7\1\10\1\uffff\1\13\1\17\1\uffff\1\11\1"+
        "\12\6\uffff\1\3";
    static final String DFA13_specialS =
        "\37\uffff}>";
    static final String[] DFA13_transitionS = {
            "\2\14\2\uffff\1\14\22\uffff\1\14\1\uffff\1\15\3\uffff\1\6\6"+
            "\uffff\1\11\1\13\1\uffff\12\12\1\4\1\5\3\7\2\uffff\3\10\1\3"+
            "\26\10\4\uffff\1\10\1\uffff\32\10\1\1\1\uffff\1\2",
            "",
            "",
            "\1\16",
            "",
            "",
            "\1\23\1\22\37\uffff\1\21\4\uffff\1\20\13\uffff\1\17",
            "",
            "",
            "\1\13\1\uffff\12\12",
            "\1\13\1\uffff\12\12",
            "",
            "",
            "",
            "\1\25",
            "",
            "",
            "",
            "\1\26\7\uffff\1\27",
            "",
            "",
            "\1\30",
            "",
            "",
            "\1\31",
            "\1\32",
            "\1\33",
            "\1\34",
            "\1\35",
            "\1\10\2\uffff\1\10\1\uffff\13\10\7\uffff\32\10\4\uffff\1\10"+
            "\1\uffff\32\10",
            ""
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__21 | T__22 | T__23 | T__24 | T__25 | UserGeneList | IndividualGene | DefaultDataTypeSpec | DataTypeOrLevel | DiscreteDataType | ContinuousDataTypeInequality | COMPARISON_OP | ID | SIGNED_FLOAT | SIGNED_INT | WS | STRING );";
        }
    }
 

}