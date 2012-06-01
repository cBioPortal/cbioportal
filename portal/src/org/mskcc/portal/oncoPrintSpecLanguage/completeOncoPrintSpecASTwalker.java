// $ANTLR 3.2 Sep 23, 2009 12:02:23 ../completeOncoPrintSpecASTwalker.g 2010-12-14 17:00:19
// TODO: upgrade to ANTLR 3.3

	package org.mskcc.portal.oncoPrintSpecLanguage;
	import java.util.ArrayList; 
	import org.mskcc.portal.oncoPrintSpecLanguage.*;
	import static java.lang.System.out;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class completeOncoPrintSpecASTwalker extends TreeParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "STRING", "UserGeneList", "ID", "IndividualGene", "DefaultDataTypeSpec", "DataTypeOrLevel", "COMPARISON_OP", "SIGNED_INT", "DiscreteDataType", "SIGNED_FLOAT", "ContinuousDataTypeInequality", "INT", "WS", "ESC_SEQ", "UNICODE_ESC", "OCTAL_ESC", "HEX_DIGIT", "'{'", "'}'", "'DATATYPES'", "':'", "';'"
    };
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


        public completeOncoPrintSpecASTwalker(TreeNodeStream input) {
            this(input, new RecognizerSharedState());
        }
        public completeOncoPrintSpecASTwalker(TreeNodeStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return completeOncoPrintSpecASTwalker.tokenNames; }
    public String getGrammarFileName() { return "../completeOncoPrintSpecASTwalker.g"; }


    	// create default, which can be replaced with 'DATATYPES' specs
    	OncoPrintGeneDisplaySpec theDefaultOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec( );

    	// APG ERRORS: list of errors
    	ArrayList<OncoPrintLangException> listOfErrors;
    	
    	public int increment( int n ){
    		return n+1;
    	}
    	
    	public void generateOncoError( int charPos, int line, String token, String msg) throws IllegalArgumentException{

    		throw new OncoPrintLangException( "Error at char " + increment(charPos) + " of line " + line + 
    		": '" + token + "'" + msg );
    	}

    	// make comprehensible errors for users
    	// TODO: AVOID DUPLICATION WITH completeOncoPrintSpecAST.g
       public void newReportError(OncoPrintLangException e) throws OncoPrintLangException{
           // if we've already reported an error and have not matched a token
           // yet successfully, don't report any errors.
           if ( state.errorRecovery ) {
             //System.err.print("[SPURIOUS] ");
              return;
           }
           state.syntaxErrors++; // don't count spurious
           state.errorRecovery = true;
    			// displayRecognitionError(this.getTokenNames(), e);
           /* APG ERROR handling
            if collecting errors, add to list
            else throw IAE
           */
           if( null != listOfErrors){ 
           		listOfErrors.add( e );
           }else{
           		throw e;
           }
       }


    public static class oncoPrintSpecification_return extends TreeRuleReturnScope {
        public OncoPrintSpecification theOncoPrintSpecification;
        public ArrayList<OncoPrintLangException> returnListOfErrors;
    };

    // $ANTLR start "oncoPrintSpecification"
    // ../completeOncoPrintSpecASTwalker.g:68:1: oncoPrintSpecification returns [OncoPrintSpecification theOncoPrintSpecification, \n\tArrayList<OncoPrintLangException> returnListOfErrors] : ( userGeneList )+ ;

    // TODO: MAJOR, MAJOR PROBLEM; figure out how to do this without hand-altering the parser
    public final completeOncoPrintSpecASTwalker.oncoPrintSpecification_return oncoPrintSpecification( OncoPrintGeneDisplaySpec anOncoPrintGeneDisplaySpec ) 
    throws RecognitionException {
        completeOncoPrintSpecASTwalker.oncoPrintSpecification_return retval = new completeOncoPrintSpecASTwalker.oncoPrintSpecification_return();
        retval.start = input.LT(1);

        GeneSet userGeneList1 = null;



        	// ACTION: initialize default OncoPrintSpecification
        	retval.theOncoPrintSpecification = new OncoPrintSpecification( );
        	// TODO: recompile grammar, eliminating this
        	theDefaultOncoPrintGeneDisplaySpec = anOncoPrintGeneDisplaySpec;

        	// APG ERRORS: initialize error list
        	listOfErrors = new ArrayList<OncoPrintLangException>(); 	

        try {
            // ../completeOncoPrintSpecASTwalker.g:78:2: ( ( userGeneList )+ )
            // ../completeOncoPrintSpecASTwalker.g:79:2: ( userGeneList )+
            {
            // ../completeOncoPrintSpecASTwalker.g:79:2: ( userGeneList )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==UserGeneList||(LA1_0>=IndividualGene && LA1_0<=DefaultDataTypeSpec)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../completeOncoPrintSpecASTwalker.g:79:4: userGeneList
            	    {
            	    pushFollow(FOLLOW_userGeneList_in_oncoPrintSpecification62);
            	    userGeneList1=userGeneList();

            	    state._fsp--;


            	    		retval.theOncoPrintSpecification.add( userGeneList1 );
            	    	

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            		retval.returnListOfErrors = listOfErrors;
            	

            }

        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "oncoPrintSpecification"


    // $ANTLR start "userGeneList"
    // ../completeOncoPrintSpecASTwalker.g:90:1: userGeneList returns [GeneSet theGeneSet] : ( ^( UserGeneList geneList ( STRING )? ) | geneList );
    public final GeneSet userGeneList() throws RecognitionException {
        GeneSet theGeneSet = null;

        CommonTree STRING3=null;
        GeneSet geneList2 = null;

        GeneSet geneList4 = null;


        try {
            // ../completeOncoPrintSpecASTwalker.g:91:2: ( ^( UserGeneList geneList ( STRING )? ) | geneList )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==UserGeneList) ) {
                alt3=1;
            }
            else if ( ((LA3_0>=IndividualGene && LA3_0<=DefaultDataTypeSpec)) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // ../completeOncoPrintSpecASTwalker.g:91:4: ^( UserGeneList geneList ( STRING )? )
                    {
                    match(input,UserGeneList,FOLLOW_UserGeneList_in_userGeneList91); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_geneList_in_userGeneList93);
                    geneList2=geneList();

                    state._fsp--;

                    // ../completeOncoPrintSpecASTwalker.g:91:28: ( STRING )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==STRING) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // ../completeOncoPrintSpecASTwalker.g:91:28: STRING
                            {
                            STRING3=(CommonTree)match(input,STRING,FOLLOW_STRING_in_userGeneList95); 

                            }
                            break;

                    }


                    match(input, Token.UP, null); 

                    		geneList2.setUserGeneList( (STRING3!=null?STRING3.getText():null));
                    		theGeneSet = geneList2;
                    	

                    }
                    break;
                case 2 :
                    // ../completeOncoPrintSpecASTwalker.g:96:4: geneList
                    {
                    pushFollow(FOLLOW_geneList_in_userGeneList106);
                    geneList4=geneList();

                    state._fsp--;


                    		theGeneSet = geneList4;
                    	

                    }
                    break;

            }
        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return theGeneSet;
    }
    // $ANTLR end "userGeneList"


    // $ANTLR start "geneList"
    // ../completeOncoPrintSpecASTwalker.g:102:1: geneList returns [GeneSet theGeneSet] : ( individualGene | defaultDataTypeSpec )+ ;
    public final GeneSet geneList() throws RecognitionException {
        GeneSet theGeneSet = null;

        GeneWithSpec individualGene5 = null;



        	// create GeneSet
        	theGeneSet = new GeneSet();

        try {
            // ../completeOncoPrintSpecASTwalker.g:107:2: ( ( individualGene | defaultDataTypeSpec )+ )
            // ../completeOncoPrintSpecASTwalker.g:108:2: ( individualGene | defaultDataTypeSpec )+
            {
            // ../completeOncoPrintSpecASTwalker.g:108:2: ( individualGene | defaultDataTypeSpec )+
            int cnt4=0;
            loop4:
            do {
                int alt4=3;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==IndividualGene) ) {
                    alt4=1;
                }
                else if ( (LA4_0==DefaultDataTypeSpec) ) {
                    alt4=2;
                }


                switch (alt4) {
            	case 1 :
            	    // ../completeOncoPrintSpecASTwalker.g:108:3: individualGene
            	    {
            	    pushFollow(FOLLOW_individualGene_in_geneList132);
            	    individualGene5=individualGene();

            	    state._fsp--;


            	    		// add gene to the set
            	    		theGeneSet.addGeneWithSpec( individualGene5 );
            	    	

            	    }
            	    break;
            	case 2 :
            	    // ../completeOncoPrintSpecASTwalker.g:113:4: defaultDataTypeSpec
            	    {
            	    pushFollow(FOLLOW_defaultDataTypeSpec_in_geneList140);
            	    defaultDataTypeSpec();

            	    state._fsp--;


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


            }

        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return theGeneSet;
    }
    // $ANTLR end "geneList"


    // $ANTLR start "individualGene"
    // ../completeOncoPrintSpecASTwalker.g:116:1: individualGene returns [GeneWithSpec theGeneWithSpec] : ^( IndividualGene ID ( fullDataTypeSpec )? ) ;
    public final GeneWithSpec individualGene() throws RecognitionException {
        GeneWithSpec theGeneWithSpec = null;

        CommonTree ID6=null;
        OncoPrintGeneDisplaySpec fullDataTypeSpec7 = null;


        try {
            // ../completeOncoPrintSpecASTwalker.g:117:2: ( ^( IndividualGene ID ( fullDataTypeSpec )? ) )
            // ../completeOncoPrintSpecASTwalker.g:118:2: ^( IndividualGene ID ( fullDataTypeSpec )? )
            {
            match(input,IndividualGene,FOLLOW_IndividualGene_in_individualGene161); 

            match(input, Token.DOWN, null); 
            ID6=(CommonTree)match(input,ID,FOLLOW_ID_in_individualGene163); 
            // ../completeOncoPrintSpecASTwalker.g:118:22: ( fullDataTypeSpec )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==DataTypeOrLevel||LA5_0==DiscreteDataType||LA5_0==ContinuousDataTypeInequality) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // ../completeOncoPrintSpecASTwalker.g:118:22: fullDataTypeSpec
                    {
                    pushFollow(FOLLOW_fullDataTypeSpec_in_individualGene165);
                    fullDataTypeSpec7=fullDataTypeSpec();

                    state._fsp--;


                    }
                    break;

            }


            match(input, Token.UP, null); 

            		// TODO: remove this test, because gene names are checked elsewhere
            		// check that ID is a gene
            		if( !Gene.valid( (ID6!=null?ID6.getText():null) ) ){
            			generateOncoError(  (ID6!=null?ID6.getCharPositionInLine():0), (ID6!=null?ID6.getLine():0), (ID6!=null?ID6.getText():null), " is not a valid gene or microRNA name." );
            		}
            		theGeneWithSpec = GeneWithSpec.geneWithSpecGenerator( (ID6!=null?ID6.getText():null), 
            			fullDataTypeSpec7,
            			theDefaultOncoPrintGeneDisplaySpec );
            	

            }

        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return theGeneWithSpec;
    }
    // $ANTLR end "individualGene"


    // $ANTLR start "defaultDataTypeSpec"
    // ../completeOncoPrintSpecASTwalker.g:132:1: defaultDataTypeSpec : ^( DefaultDataTypeSpec fullDataTypeSpec ) ;
    public final void defaultDataTypeSpec() throws RecognitionException {
        OncoPrintGeneDisplaySpec fullDataTypeSpec8 = null;


        try {
            // ../completeOncoPrintSpecASTwalker.g:133:2: ( ^( DefaultDataTypeSpec fullDataTypeSpec ) )
            // ../completeOncoPrintSpecASTwalker.g:133:4: ^( DefaultDataTypeSpec fullDataTypeSpec )
            {
            match(input,DefaultDataTypeSpec,FOLLOW_DefaultDataTypeSpec_in_defaultDataTypeSpec185); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_fullDataTypeSpec_in_defaultDataTypeSpec187);
            fullDataTypeSpec8=fullDataTypeSpec();

            state._fsp--;


            match(input, Token.UP, null); 

            		// set the global default 
            		theDefaultOncoPrintGeneDisplaySpec = fullDataTypeSpec8;
            	

            }

        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "defaultDataTypeSpec"


    // $ANTLR start "fullDataTypeSpec"
    // ../completeOncoPrintSpecASTwalker.g:140:1: fullDataTypeSpec returns [OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec] : ( dataTypeSpec )+ ;
    public final OncoPrintGeneDisplaySpec fullDataTypeSpec() throws RecognitionException {
        OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec = null;

        DataTypeSpec dataTypeSpec9 = null;



        		// create a Parsed ...
        		ParsedFullDataTypeSpec theParsedFullDataTypeSpec = new ParsedFullDataTypeSpec();

        try {
            // ../completeOncoPrintSpecASTwalker.g:145:2: ( ( dataTypeSpec )+ )
            // ../completeOncoPrintSpecASTwalker.g:146:2: ( dataTypeSpec )+
            {
            // ../completeOncoPrintSpecASTwalker.g:146:2: ( dataTypeSpec )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==DataTypeOrLevel||LA6_0==DiscreteDataType||LA6_0==ContinuousDataTypeInequality) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ../completeOncoPrintSpecASTwalker.g:146:3: dataTypeSpec
            	    {
            	    pushFollow(FOLLOW_dataTypeSpec_in_fullDataTypeSpec213);
            	    dataTypeSpec9=dataTypeSpec();

            	    state._fsp--;


            	    		// save to a Parsed ...
            	    		theParsedFullDataTypeSpec.addSpec( dataTypeSpec9 );
            	    	

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


            		// clean the parsed, and return the simplified spec
            		theOncoPrintGeneDisplaySpec = theParsedFullDataTypeSpec.cleanUpInput();
            	

            }

        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return theOncoPrintGeneDisplaySpec;
    }
    // $ANTLR end "fullDataTypeSpec"


    // $ANTLR start "dataTypeSpec"
    // ../completeOncoPrintSpecASTwalker.g:158:1: dataTypeSpec returns [DataTypeSpec theDataTypeSpec] : ( ^( DataTypeOrLevel ID ) | discreteDataType | continuousDataTypeInequality );
    public final DataTypeSpec dataTypeSpec() throws RecognitionException {
        DataTypeSpec theDataTypeSpec = null;

        CommonTree ID10=null;
        DataTypeSpec discreteDataType11 = null;

        ContinuousDataTypeSpec continuousDataTypeInequality12 = null;


        try {
            // ../completeOncoPrintSpecASTwalker.g:159:2: ( ^( DataTypeOrLevel ID ) | discreteDataType | continuousDataTypeInequality )
            int alt7=3;
            switch ( input.LA(1) ) {
            case DataTypeOrLevel:
                {
                alt7=1;
                }
                break;
            case DiscreteDataType:
                {
                alt7=2;
                }
                break;
            case ContinuousDataTypeInequality:
                {
                alt7=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }

            switch (alt7) {
                case 1 :
                    // ../completeOncoPrintSpecASTwalker.g:160:2: ^( DataTypeOrLevel ID )
                    {
                    match(input,DataTypeOrLevel,FOLLOW_DataTypeOrLevel_in_dataTypeSpec242); 

                    match(input, Token.DOWN, null); 
                    ID10=(CommonTree)match(input,ID,FOLLOW_ID_in_dataTypeSpec244); 

                    match(input, Token.UP, null); 

                    		theDataTypeSpec = ConcreteDataTypeSpec.concreteDataTypeSpecGenerator( (ID10!=null?ID10.getText():null) );
                    		if( null == theDataTypeSpec ){
                    			theDataTypeSpec = DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGeneratorByLevelName( (ID10!=null?ID10.getText():null) );
                    		}
                    		if( null == theDataTypeSpec ){
                    					// APG ERRORS: throw IAE
                    			generateOncoError(   (ID10!=null?ID10.getCharPositionInLine():0), (ID10!=null?ID10.getLine():0),   (ID10!=null?ID10.getText():null), " is not a valid genetic data type or data level."   );
                    		}
                    	

                    }
                    break;
                case 2 :
                    // ../completeOncoPrintSpecASTwalker.g:171:4: discreteDataType
                    {
                    pushFollow(FOLLOW_discreteDataType_in_dataTypeSpec255);
                    discreteDataType11=discreteDataType();

                    state._fsp--;


                    		theDataTypeSpec = discreteDataType11;
                    	

                    }
                    break;
                case 3 :
                    // ../completeOncoPrintSpecASTwalker.g:175:4: continuousDataTypeInequality
                    {
                    pushFollow(FOLLOW_continuousDataTypeInequality_in_dataTypeSpec264);
                    continuousDataTypeInequality12=continuousDataTypeInequality();

                    state._fsp--;


                    		theDataTypeSpec = continuousDataTypeInequality12;
                    	

                    }
                    break;

            }
        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return theDataTypeSpec;
    }
    // $ANTLR end "dataTypeSpec"


    // $ANTLR start "discreteDataType"
    // ../completeOncoPrintSpecASTwalker.g:181:1: discreteDataType returns [DataTypeSpec theDataTypeSpec] : ( ^( DiscreteDataType type= ID COMPARISON_OP level= ID ) | ^( DiscreteDataType ID SIGNED_INT ) );
    public final DataTypeSpec discreteDataType() throws RecognitionException {
        DataTypeSpec theDataTypeSpec = null;

        CommonTree type=null;
        CommonTree level=null;
        CommonTree COMPARISON_OP13=null;
        CommonTree ID14=null;
        CommonTree SIGNED_INT15=null;

        try {
            // ../completeOncoPrintSpecASTwalker.g:182:2: ( ^( DiscreteDataType type= ID COMPARISON_OP level= ID ) | ^( DiscreteDataType ID SIGNED_INT ) )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==DiscreteDataType) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==DOWN) ) {
                    int LA8_2 = input.LA(3);

                    if ( (LA8_2==ID) ) {
                        int LA8_3 = input.LA(4);

                        if ( (LA8_3==COMPARISON_OP) ) {
                            alt8=1;
                        }
                        else if ( (LA8_3==SIGNED_INT) ) {
                            alt8=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 8, 3, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 2, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // ../completeOncoPrintSpecASTwalker.g:184:2: ^( DiscreteDataType type= ID COMPARISON_OP level= ID )
                    {
                    match(input,DiscreteDataType,FOLLOW_DiscreteDataType_in_discreteDataType288); 

                    match(input, Token.DOWN, null); 
                    type=(CommonTree)match(input,ID,FOLLOW_ID_in_discreteDataType292); 
                    COMPARISON_OP13=(CommonTree)match(input,COMPARISON_OP,FOLLOW_COMPARISON_OP_in_discreteDataType294); 
                    level=(CommonTree)match(input,ID,FOLLOW_ID_in_discreteDataType298); 

                    match(input, Token.UP, null); 

                    		theDataTypeSpec = DiscreteDataTypeSpec.discreteDataTypeSpecGenerator( (type!=null?type.getText():null), (COMPARISON_OP13!=null?COMPARISON_OP13.getText():null), (level!=null?level.getText():null) );
                    		// TODO: more specific error about type or level
                    		if( null == theDataTypeSpec ){
                    			// APG ERRORS: throw IAE
                    			generateOncoError(   (type!=null?type.getCharPositionInLine():0), (type!=null?type.getLine():0), (type!=null?type.getText():null)+ " " + (COMPARISON_OP13!=null?COMPARISON_OP13.getText():null)+ " " +  (level!=null?level.getText():null),
                    				" is not a valid discrete genetic data type and discrete genetic data level." );
                    		}
                    	

                    }
                    break;
                case 2 :
                    // ../completeOncoPrintSpecASTwalker.g:195:2: ^( DiscreteDataType ID SIGNED_INT )
                    {
                    match(input,DiscreteDataType,FOLLOW_DiscreteDataType_in_discreteDataType311); 

                    match(input, Token.DOWN, null); 
                    ID14=(CommonTree)match(input,ID,FOLLOW_ID_in_discreteDataType313); 
                    SIGNED_INT15=(CommonTree)match(input,SIGNED_INT,FOLLOW_SIGNED_INT_in_discreteDataType315); 

                    match(input, Token.UP, null); 

                    		theDataTypeSpec = DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGeneratorByLevelCode( (ID14!=null?ID14.getText():null), (SIGNED_INT15!=null?SIGNED_INT15.getText():null) );
                    		if( null == theDataTypeSpec ){
                    			// APG ERRORS: throw IAE
                    			generateOncoError(   (ID14!=null?ID14.getCharPositionInLine():0), (ID14!=null?ID14.getLine():0), (ID14!=null?ID14.getText():null) + " " + (SIGNED_INT15!=null?SIGNED_INT15.getText():null), 
                    				" is not a valid genetic data type and GISTIC code." );
                    		}
                    	

                    }
                    break;

            }
        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return theDataTypeSpec;
    }
    // $ANTLR end "discreteDataType"


    // $ANTLR start "floatOrInt"
    // ../completeOncoPrintSpecASTwalker.g:206:1: floatOrInt returns [String number] : ( SIGNED_FLOAT | SIGNED_INT );
    public final String floatOrInt() throws RecognitionException {
        String number = null;

        CommonTree SIGNED_FLOAT16=null;
        CommonTree SIGNED_INT17=null;

        try {
            // ../completeOncoPrintSpecASTwalker.g:207:2: ( SIGNED_FLOAT | SIGNED_INT )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==SIGNED_FLOAT) ) {
                alt9=1;
            }
            else if ( (LA9_0==SIGNED_INT) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // ../completeOncoPrintSpecASTwalker.g:208:2: SIGNED_FLOAT
                    {
                    SIGNED_FLOAT16=(CommonTree)match(input,SIGNED_FLOAT,FOLLOW_SIGNED_FLOAT_in_floatOrInt337); 

                    		number = (SIGNED_FLOAT16!=null?SIGNED_FLOAT16.getText():null);
                    	

                    }
                    break;
                case 2 :
                    // ../completeOncoPrintSpecASTwalker.g:212:4: SIGNED_INT
                    {
                    SIGNED_INT17=(CommonTree)match(input,SIGNED_INT,FOLLOW_SIGNED_INT_in_floatOrInt346); 

                    		number = (SIGNED_INT17!=null?SIGNED_INT17.getText():null);
                    	

                    }
                    break;

            }
        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return number;
    }
    // $ANTLR end "floatOrInt"


    // $ANTLR start "continuousDataTypeInequality"
    // ../completeOncoPrintSpecASTwalker.g:218:1: continuousDataTypeInequality returns [ContinuousDataTypeSpec theContinuousDataTypeSpec] : ^( ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt ) ;
    public final ContinuousDataTypeSpec continuousDataTypeInequality() throws RecognitionException {
        ContinuousDataTypeSpec theContinuousDataTypeSpec = null;

        CommonTree ID18=null;
        CommonTree COMPARISON_OP19=null;
        String floatOrInt20 = null;


        try {
            // ../completeOncoPrintSpecASTwalker.g:219:2: ( ^( ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt ) )
            // ../completeOncoPrintSpecASTwalker.g:220:2: ^( ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt )
            {
            match(input,ContinuousDataTypeInequality,FOLLOW_ContinuousDataTypeInequality_in_continuousDataTypeInequality368); 

            match(input, Token.DOWN, null); 
            ID18=(CommonTree)match(input,ID,FOLLOW_ID_in_continuousDataTypeInequality370); 
            COMPARISON_OP19=(CommonTree)match(input,COMPARISON_OP,FOLLOW_COMPARISON_OP_in_continuousDataTypeInequality372); 
            pushFollow(FOLLOW_floatOrInt_in_continuousDataTypeInequality374);
            floatOrInt20=floatOrInt();

            state._fsp--;


            match(input, Token.UP, null); 

            		theContinuousDataTypeSpec = ContinuousDataTypeSpec.continuousDataTypeSpecGenerator( 
            			(ID18!=null?ID18.getText():null), (COMPARISON_OP19!=null?COMPARISON_OP19.getText():null), floatOrInt20 );
            		if( null == theContinuousDataTypeSpec ){
            				generateOncoError(   (ID18!=null?ID18.getCharPositionInLine():0), (ID18!=null?ID18.getLine():0), (ID18!=null?ID18.getText():null), " is not a valid genetic data type." );
            		}
            	

            }

        }
         
            catch (RecognitionException re) {
            		// trees s/b all well-formed, so don't expect these
                reportError(re);
                recover(input,re);
            } catch (OncoPrintLangException e) {
                newReportError(e);        
                recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
            }
        finally {
        }
        return theContinuousDataTypeSpec;
    }
    // $ANTLR end "continuousDataTypeInequality"

    // Delegated rules


 

    public static final BitSet FOLLOW_userGeneList_in_oncoPrintSpecification62 = new BitSet(new long[]{0x00000000000001A2L});
    public static final BitSet FOLLOW_UserGeneList_in_userGeneList91 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_geneList_in_userGeneList93 = new BitSet(new long[]{0x0000000000000018L});
    public static final BitSet FOLLOW_STRING_in_userGeneList95 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_geneList_in_userGeneList106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_individualGene_in_geneList132 = new BitSet(new long[]{0x00000000000001A2L});
    public static final BitSet FOLLOW_defaultDataTypeSpec_in_geneList140 = new BitSet(new long[]{0x00000000000001A2L});
    public static final BitSet FOLLOW_IndividualGene_in_individualGene161 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_individualGene163 = new BitSet(new long[]{0x0000000000005208L});
    public static final BitSet FOLLOW_fullDataTypeSpec_in_individualGene165 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_DefaultDataTypeSpec_in_defaultDataTypeSpec185 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_fullDataTypeSpec_in_defaultDataTypeSpec187 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_dataTypeSpec_in_fullDataTypeSpec213 = new BitSet(new long[]{0x0000000000005202L});
    public static final BitSet FOLLOW_DataTypeOrLevel_in_dataTypeSpec242 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_dataTypeSpec244 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_discreteDataType_in_dataTypeSpec255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_continuousDataTypeInequality_in_dataTypeSpec264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DiscreteDataType_in_discreteDataType288 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_discreteDataType292 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_COMPARISON_OP_in_discreteDataType294 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_discreteDataType298 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_DiscreteDataType_in_discreteDataType311 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_discreteDataType313 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_SIGNED_INT_in_discreteDataType315 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SIGNED_FLOAT_in_floatOrInt337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SIGNED_INT_in_floatOrInt346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ContinuousDataTypeInequality_in_continuousDataTypeInequality368 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_continuousDataTypeInequality370 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_COMPARISON_OP_in_continuousDataTypeInequality372 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_floatOrInt_in_continuousDataTypeInequality374 = new BitSet(new long[]{0x0000000000000008L});

}