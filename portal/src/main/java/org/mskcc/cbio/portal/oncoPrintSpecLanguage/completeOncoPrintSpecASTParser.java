// $ANTLR 3.2 Sep 23, 2009 12:02:23 ../completeOncoPrintSpecAST.g 2010-12-14 17:00:18
 
	package org.mskcc.cbio.portal.oncoPrintSpecLanguage;
	import java.util.ArrayList; 
	import org.mskcc.cbio.portal.oncoPrintSpecLanguage.*;
	import static java.lang.System.out;
	import java.io.PrintStream;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class completeOncoPrintSpecASTParser extends Parser {
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
    public static final int ESC_SEQ=17;
    public static final int WS=16;
    public static final int IndividualGene=7;
    public static final int DefaultDataTypeSpec=8;
    public static final int DataTypeOrLevel=9;
    public static final int SIGNED_FLOAT=13;
    public static final int STRING=4;

    // delegates
    // delegators


        public completeOncoPrintSpecASTParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public completeOncoPrintSpecASTParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return completeOncoPrintSpecASTParser.tokenNames; }
    public String getGrammarFileName() { return "../completeOncoPrintSpecAST.g"; }


    	/*
    	DAMN, won't work; this returns only an AST, and we don't want to throw exceptions because then the parse won't complete
    	
    	Error handling strategy: We serve two purposes, 
    	1) when calling the root production, oncoPrintSpecification, return a list of all errors, each a String;
    		similarly for the tree grammar; thus if syntax error(s) occur, the driver program gets all of them, and doesn't call the 
    		tree parser; and if semantics error(s) occur, the driver will get all of them
    	2) when calling any other production, as for unit testing, throw an RecognitionException
    	Implementation:
    	a) in reportError, determine whether we're in case 1 or 2, and collect or throw accordingly
    	b) in the root production if errors have been thrown, rethrow the list 
    	*/

    	// get a list to hold errors
    // TODO: HIGH: UNIT TEST in OncoSpec
    	ArrayList<String> errorMessages = new ArrayList<String>(); // APG: TMP FIX: Utilities.getErrorMessages();

    	// override to make comprehensible errors for users
       public void reportError(RecognitionException e){
              // if we've already reported an error and have not matched a token
              // yet successfully, don't report any errors.
              if ( state.errorRecovery ) {
                      //System.err.print("[SPURIOUS] ");
                      return;
              }
              state.syntaxErrors++; // don't count spurious
              state.errorRecovery = true;
    			 displayRecognitionError(this.getTokenNames(), e);
       }

       public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
    	//      String hdr = getErrorHeader(e);
    		int normalPos = e.charPositionInLine + 1;
    		String hdr = "Syntax error at char " + normalPos + " of line "+e.line+": ";
          String msg = getErrorMessage(e, tokenNames);
          // TODO: change this to print to a tmp file
          // get ASTparser to write errors to a PrintStream, connected to a file
    		// System.err.println( hdr + msg);
          errorMessages.add( hdr + msg );
       }

       public String getErrorMessage(RecognitionException e, String[] tokenNames) {
          String msg = e.getMessage();
    		// emitErrorMessage( "APG received " + e.getClass().getName() + " with token " + e.token.getText() );
          
          if (e instanceof UnwantedTokenException) {
             UnwantedTokenException ute = (UnwantedTokenException) e;
             String tokenName = "<unknown>";
             if (ute.expecting == Token.EOF) {
                tokenName = "EOF";
             } else {
                tokenName = tokenNames[ute.expecting];
             }
             msg = "extraneous input " + getTokenErrorDisplay(ute.getUnexpectedToken()) + " expecting "
                   + tokenName;
          } else if (e instanceof MissingTokenException) {
             MissingTokenException mte = (MissingTokenException) e;
             String tokenName = "<unknown>";
             if (mte.expecting == Token.EOF) {
                tokenName = "EOF";
             } else {
                tokenName = tokenNames[mte.expecting];
             }
             msg = "missing " + tokenName + " at " + getTokenErrorDisplay(e.token);
          } else if (e instanceof MismatchedTokenException) {
             MismatchedTokenException mte = (MismatchedTokenException) e;
             String tokenName = "<unknown>";
             if (mte.expecting == Token.EOF) {
                tokenName = "EOF";
             } else {
                tokenName = tokenNames[mte.expecting];
             }
             msg = "mismatched input " + getTokenErrorDisplay(e.token) + " expecting " + tokenName;
          } else if (e instanceof MismatchedTreeNodeException) {
             MismatchedTreeNodeException mtne = (MismatchedTreeNodeException) e;
             String tokenName = "<unknown>";
             if (mtne.expecting == Token.EOF) {
                tokenName = "EOF";
             } else {
                tokenName = tokenNames[mtne.expecting];
             }
             msg = "mismatched tree node: " + mtne.node + " expecting " + tokenName;
          } else if (e instanceof NoViableAltException) {
             // NoViableAltException nvae = (NoViableAltException)e;
             // for development, can add
             // "decision=<<"+nvae.grammarDecisionDescription+">>"
             // and "(decision="+nvae.decisionNumber+") and
             // "state "+nvae.stateNumber
             msg = "Syntax error at " + getTokenErrorDisplay(e.token);
          } else if (e instanceof EarlyExitException) {
             // EarlyExitException eee = (EarlyExitException)e;
             // for development, can add "(decision="+eee.decisionNumber+")"
             // TODO: better error message
             msg = "found empty list at "  // "required (...)+ loop did not match anything at input "
                   + getTokenErrorDisplay(e.token);
          } else if (e instanceof MismatchedSetException) {
             MismatchedSetException mse = (MismatchedSetException) e;
             msg = "mismatched input " + getTokenErrorDisplay(e.token) + "."; // + " expecting set " + mse.expecting;
          } else if (e instanceof MismatchedNotSetException) {
             MismatchedNotSetException mse = (MismatchedNotSetException) e;
             msg = "mismatched input " + getTokenErrorDisplay(e.token) + "."; // + " expecting set " + mse.expecting;
          } else if (e instanceof FailedPredicateException) {
             FailedPredicateException fpe = (FailedPredicateException) e;
    			// msg = "rule " + fpe.ruleName + " failed predicate: {" + fpe.predicateText + "}?";
    			// msg = "token '" + e.token.getText() + "' is not a valid '" + lookupDataTypeName( fpe.ruleName ) +"'.";
          }
          return msg;
       }


    public static class oncoPrintSpecification_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "oncoPrintSpecification"
    // ../completeOncoPrintSpecAST.g:126:1: oncoPrintSpecification : ( userGeneList )+ -> ( userGeneList )+ ;
    public final completeOncoPrintSpecASTParser.oncoPrintSpecification_return oncoPrintSpecification() throws RecognitionException {
       // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());
        completeOncoPrintSpecASTParser.oncoPrintSpecification_return retval = new completeOncoPrintSpecASTParser.oncoPrintSpecification_return();
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());
        retval.start = input.LT(1);
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());

        Object root_0 = null;

        completeOncoPrintSpecASTParser.userGeneList_return userGeneList1 = null;

        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());

        RewriteRuleSubtreeStream stream_userGeneList=new RewriteRuleSubtreeStream(adaptor,"rule userGeneList");
        try {
            // ../completeOncoPrintSpecAST.g:127:2: ( ( userGeneList )+ -> ( userGeneList )+ )
            // ../completeOncoPrintSpecAST.g:128:2: ( userGeneList )+
            {
            // ../completeOncoPrintSpecAST.g:128:2: ( userGeneList )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==STRING||LA1_0==ID||LA1_0==21||LA1_0==23) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../completeOncoPrintSpecAST.g:128:4: userGeneList
            	    {
            	    pushFollow(FOLLOW_userGeneList_in_oncoPrintSpecification45);
            	    userGeneList1=userGeneList();

            	    state._fsp--;

            	    stream_userGeneList.add(userGeneList1.getTree());

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



            // AST REWRITE
            // elements: userGeneList
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 128:20: -> ( userGeneList )+
            {
                if ( !(stream_userGeneList.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_userGeneList.hasNext() ) {
                    adaptor.addChild(root_0, stream_userGeneList.nextTree());

                }
                stream_userGeneList.reset();

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "oncoPrintSpecification"

    public static class userGeneList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "userGeneList"
    // ../completeOncoPrintSpecAST.g:131:1: userGeneList : ( geneList | ( '{' geneList '}' | STRING '{' geneList '}' ) -> ^( UserGeneList geneList ( STRING )? ) );
    public final completeOncoPrintSpecASTParser.userGeneList_return userGeneList() throws RecognitionException {
        completeOncoPrintSpecASTParser.userGeneList_return retval = new completeOncoPrintSpecASTParser.userGeneList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal3=null;
        Token char_literal5=null;
        Token STRING6=null;
        Token char_literal7=null;
        Token char_literal9=null;
        completeOncoPrintSpecASTParser.geneList_return geneList2 = null;

        completeOncoPrintSpecASTParser.geneList_return geneList4 = null;

        completeOncoPrintSpecASTParser.geneList_return geneList8 = null;


        Object char_literal3_tree=null;
        Object char_literal5_tree=null;
        Object STRING6_tree=null;
        Object char_literal7_tree=null;
        Object char_literal9_tree=null;
        RewriteRuleTokenStream stream_21=new RewriteRuleTokenStream(adaptor,"token 21");
        RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
        RewriteRuleTokenStream stream_STRING=new RewriteRuleTokenStream(adaptor,"token STRING");
        RewriteRuleSubtreeStream stream_geneList=new RewriteRuleSubtreeStream(adaptor,"rule geneList");
        try {
            // ../completeOncoPrintSpecAST.g:132:2: ( geneList | ( '{' geneList '}' | STRING '{' geneList '}' ) -> ^( UserGeneList geneList ( STRING )? ) )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ID||LA3_0==23) ) {
                alt3=1;
            }
            else if ( (LA3_0==STRING||LA3_0==21) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // ../completeOncoPrintSpecAST.g:133:2: geneList
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_geneList_in_userGeneList65);
                    geneList2=geneList();

                    state._fsp--;

                    adaptor.addChild(root_0, geneList2.getTree());

                    }
                    break;
                case 2 :
                    // ../completeOncoPrintSpecAST.g:135:2: ( '{' geneList '}' | STRING '{' geneList '}' )
                    {
                    // ../completeOncoPrintSpecAST.g:135:2: ( '{' geneList '}' | STRING '{' geneList '}' )
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==21) ) {
                        alt2=1;
                    }
                    else if ( (LA2_0==STRING) ) {
                        alt2=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 2, 0, input);

                        throw nvae;
                    }
                    switch (alt2) {
                        case 1 :
                            // ../completeOncoPrintSpecAST.g:135:4: '{' geneList '}'
                            {
                            char_literal3=(Token)match(input,21,FOLLOW_21_in_userGeneList75);  
                            stream_21.add(char_literal3);

                            pushFollow(FOLLOW_geneList_in_userGeneList77);
                            geneList4=geneList();

                            state._fsp--;

                            stream_geneList.add(geneList4.getTree());
                            char_literal5=(Token)match(input,22,FOLLOW_22_in_userGeneList79);  
                            stream_22.add(char_literal5);


                            }
                            break;
                        case 2 :
                            // ../completeOncoPrintSpecAST.g:136:4: STRING '{' geneList '}'
                            {
                            STRING6=(Token)match(input,STRING,FOLLOW_STRING_in_userGeneList85);  
                            stream_STRING.add(STRING6);

                            char_literal7=(Token)match(input,21,FOLLOW_21_in_userGeneList87);  
                            stream_21.add(char_literal7);

                            pushFollow(FOLLOW_geneList_in_userGeneList89);
                            geneList8=geneList();

                            state._fsp--;

                            stream_geneList.add(geneList8.getTree());
                            char_literal9=(Token)match(input,22,FOLLOW_22_in_userGeneList91);  
                            stream_22.add(char_literal9);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: STRING, geneList
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 136:30: -> ^( UserGeneList geneList ( STRING )? )
                    {
                        // ../completeOncoPrintSpecAST.g:136:33: ^( UserGeneList geneList ( STRING )? )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(UserGeneList, "UserGeneList"), root_1);

                        adaptor.addChild(root_1, stream_geneList.nextTree());
                        // ../completeOncoPrintSpecAST.g:136:57: ( STRING )?
                        if ( stream_STRING.hasNext() ) {
                            adaptor.addChild(root_1, stream_STRING.nextNode());

                        }
                        stream_STRING.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "userGeneList"

    public static class geneList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "geneList"
    // ../completeOncoPrintSpecAST.g:141:1: geneList : ( individualGene | defaultDataTypeSpec )+ ;
    public final completeOncoPrintSpecASTParser.geneList_return geneList() throws RecognitionException {
        completeOncoPrintSpecASTParser.geneList_return retval = new completeOncoPrintSpecASTParser.geneList_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        completeOncoPrintSpecASTParser.individualGene_return individualGene10 = null;

        completeOncoPrintSpecASTParser.defaultDataTypeSpec_return defaultDataTypeSpec11 = null;



        try {
            // ../completeOncoPrintSpecAST.g:142:2: ( ( individualGene | defaultDataTypeSpec )+ )
            // ../completeOncoPrintSpecAST.g:146:2: ( individualGene | defaultDataTypeSpec )+
            {
            root_0 = (Object)adaptor.nil();

            // ../completeOncoPrintSpecAST.g:146:2: ( individualGene | defaultDataTypeSpec )+
            int cnt4=0;
            loop4:
            do {
                int alt4=3;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==ID) ) {
                    alt4=1;
                }
                else if ( (LA4_0==23) ) {
                    alt4=2;
                }


                switch (alt4) {
            	case 1 :
            	    // ../completeOncoPrintSpecAST.g:146:4: individualGene
            	    {
            	    pushFollow(FOLLOW_individualGene_in_geneList135);
            	    individualGene10=individualGene();

            	    state._fsp--;

            	    adaptor.addChild(root_0, individualGene10.getTree());

            	    }
            	    break;
            	case 2 :
            	    // ../completeOncoPrintSpecAST.g:146:21: defaultDataTypeSpec
            	    {
            	    pushFollow(FOLLOW_defaultDataTypeSpec_in_geneList139);
            	    defaultDataTypeSpec11=defaultDataTypeSpec();

            	    state._fsp--;

            	    adaptor.addChild(root_0, defaultDataTypeSpec11.getTree());

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

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "geneList"

    public static class individualGene_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "individualGene"
    // ../completeOncoPrintSpecAST.g:150:1: individualGene : ID ( fullDataTypeSpec )? -> ^( IndividualGene ID ( fullDataTypeSpec )? ) ;
    public final completeOncoPrintSpecASTParser.individualGene_return individualGene() throws RecognitionException {
        completeOncoPrintSpecASTParser.individualGene_return retval = new completeOncoPrintSpecASTParser.individualGene_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID12=null;
        completeOncoPrintSpecASTParser.fullDataTypeSpec_return fullDataTypeSpec13 = null;


        Object ID12_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_fullDataTypeSpec=new RewriteRuleSubtreeStream(adaptor,"rule fullDataTypeSpec");
        try {
            // ../completeOncoPrintSpecAST.g:151:2: ( ID ( fullDataTypeSpec )? -> ^( IndividualGene ID ( fullDataTypeSpec )? ) )
            // ../completeOncoPrintSpecAST.g:152:2: ID ( fullDataTypeSpec )?
            {
            ID12=(Token)match(input,ID,FOLLOW_ID_in_individualGene155);  
            stream_ID.add(ID12);

            // ../completeOncoPrintSpecAST.g:152:5: ( fullDataTypeSpec )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==24) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // ../completeOncoPrintSpecAST.g:152:5: fullDataTypeSpec
                    {
                    pushFollow(FOLLOW_fullDataTypeSpec_in_individualGene157);
                    fullDataTypeSpec13=fullDataTypeSpec();

                    state._fsp--;

                    stream_fullDataTypeSpec.add(fullDataTypeSpec13.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: ID, fullDataTypeSpec
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 152:23: -> ^( IndividualGene ID ( fullDataTypeSpec )? )
            {
                // ../completeOncoPrintSpecAST.g:152:26: ^( IndividualGene ID ( fullDataTypeSpec )? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(IndividualGene, "IndividualGene"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                // ../completeOncoPrintSpecAST.g:152:46: ( fullDataTypeSpec )?
                if ( stream_fullDataTypeSpec.hasNext() ) {
                    adaptor.addChild(root_1, stream_fullDataTypeSpec.nextTree());

                }
                stream_fullDataTypeSpec.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "individualGene"

    public static class defaultDataTypeSpec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "defaultDataTypeSpec"
    // ../completeOncoPrintSpecAST.g:158:1: defaultDataTypeSpec : 'DATATYPES' fullDataTypeSpec -> ^( DefaultDataTypeSpec fullDataTypeSpec ) ;
    public final completeOncoPrintSpecASTParser.defaultDataTypeSpec_return defaultDataTypeSpec() throws RecognitionException {
        completeOncoPrintSpecASTParser.defaultDataTypeSpec_return retval = new completeOncoPrintSpecASTParser.defaultDataTypeSpec_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal14=null;
        completeOncoPrintSpecASTParser.fullDataTypeSpec_return fullDataTypeSpec15 = null;


        Object string_literal14_tree=null;
        RewriteRuleTokenStream stream_23=new RewriteRuleTokenStream(adaptor,"token 23");
        RewriteRuleSubtreeStream stream_fullDataTypeSpec=new RewriteRuleSubtreeStream(adaptor,"rule fullDataTypeSpec");
        try {
            // ../completeOncoPrintSpecAST.g:159:2: ( 'DATATYPES' fullDataTypeSpec -> ^( DefaultDataTypeSpec fullDataTypeSpec ) )
            // ../completeOncoPrintSpecAST.g:159:4: 'DATATYPES' fullDataTypeSpec
            {
            string_literal14=(Token)match(input,23,FOLLOW_23_in_defaultDataTypeSpec190);  
            stream_23.add(string_literal14);

            pushFollow(FOLLOW_fullDataTypeSpec_in_defaultDataTypeSpec194);
            fullDataTypeSpec15=fullDataTypeSpec();

            state._fsp--;

            stream_fullDataTypeSpec.add(fullDataTypeSpec15.getTree());


            // AST REWRITE
            // elements: fullDataTypeSpec
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 160:19: -> ^( DefaultDataTypeSpec fullDataTypeSpec )
            {
                // ../completeOncoPrintSpecAST.g:160:22: ^( DefaultDataTypeSpec fullDataTypeSpec )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DefaultDataTypeSpec, "DefaultDataTypeSpec"), root_1);

                adaptor.addChild(root_1, stream_fullDataTypeSpec.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "defaultDataTypeSpec"

    public static class fullDataTypeSpec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fullDataTypeSpec"
    // ../completeOncoPrintSpecAST.g:166:1: fullDataTypeSpec : ':' ( dataTypeSpec )+ ';' -> ( dataTypeSpec )+ ;
    public final completeOncoPrintSpecASTParser.fullDataTypeSpec_return fullDataTypeSpec() throws RecognitionException {
        completeOncoPrintSpecASTParser.fullDataTypeSpec_return retval = new completeOncoPrintSpecASTParser.fullDataTypeSpec_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal16=null;
        Token char_literal18=null;
        completeOncoPrintSpecASTParser.dataTypeSpec_return dataTypeSpec17 = null;


        Object char_literal16_tree=null;
        Object char_literal18_tree=null;
        RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
        RewriteRuleTokenStream stream_25=new RewriteRuleTokenStream(adaptor,"token 25");
        RewriteRuleSubtreeStream stream_dataTypeSpec=new RewriteRuleSubtreeStream(adaptor,"rule dataTypeSpec");
        try {
            // ../completeOncoPrintSpecAST.g:167:2: ( ':' ( dataTypeSpec )+ ';' -> ( dataTypeSpec )+ )
            // ../completeOncoPrintSpecAST.g:167:4: ':' ( dataTypeSpec )+ ';'
            {
            char_literal16=(Token)match(input,24,FOLLOW_24_in_fullDataTypeSpec222);  
            stream_24.add(char_literal16);

            // ../completeOncoPrintSpecAST.g:167:8: ( dataTypeSpec )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==ID) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ../completeOncoPrintSpecAST.g:167:10: dataTypeSpec
            	    {
            	    pushFollow(FOLLOW_dataTypeSpec_in_fullDataTypeSpec226);
            	    dataTypeSpec17=dataTypeSpec();

            	    state._fsp--;

            	    stream_dataTypeSpec.add(dataTypeSpec17.getTree());

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

            char_literal18=(Token)match(input,25,FOLLOW_25_in_fullDataTypeSpec231);  
            stream_25.add(char_literal18);



            // AST REWRITE
            // elements: dataTypeSpec
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 167:30: -> ( dataTypeSpec )+
            {
                if ( !(stream_dataTypeSpec.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_dataTypeSpec.hasNext() ) {
                    adaptor.addChild(root_0, stream_dataTypeSpec.nextTree());

                }
                stream_dataTypeSpec.reset();

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fullDataTypeSpec"

    public static class dataTypeSpec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dataTypeSpec"
    // ../completeOncoPrintSpecAST.g:172:1: dataTypeSpec : ( ID -> ^( DataTypeOrLevel ID ) | discreteDataType | continuousDataTypeInequality );
    public final completeOncoPrintSpecASTParser.dataTypeSpec_return dataTypeSpec() throws RecognitionException {
        completeOncoPrintSpecASTParser.dataTypeSpec_return retval = new completeOncoPrintSpecASTParser.dataTypeSpec_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID19=null;
        completeOncoPrintSpecASTParser.discreteDataType_return discreteDataType20 = null;

        completeOncoPrintSpecASTParser.continuousDataTypeInequality_return continuousDataTypeInequality21 = null;


        Object ID19_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // ../completeOncoPrintSpecAST.g:173:2: ( ID -> ^( DataTypeOrLevel ID ) | discreteDataType | continuousDataTypeInequality )
            int alt7=3;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==ID) ) {
                switch ( input.LA(2) ) {
                case COMPARISON_OP:
                    {
                    int LA7_2 = input.LA(3);

                    if ( (LA7_2==ID) ) {
                        alt7=2;
                    }
                    else if ( (LA7_2==SIGNED_INT||LA7_2==SIGNED_FLOAT) ) {
                        alt7=3;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 2, input);

                        throw nvae;
                    }
                    }
                    break;
                case SIGNED_INT:
                    {
                    alt7=2;
                    }
                    break;
                case ID:
                case 25:
                    {
                    alt7=1;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // ../completeOncoPrintSpecAST.g:174:2: ID
                    {
                    ID19=(Token)match(input,ID,FOLLOW_ID_in_dataTypeSpec251);  
                    stream_ID.add(ID19);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 174:5: -> ^( DataTypeOrLevel ID )
                    {
                        // ../completeOncoPrintSpecAST.g:174:8: ^( DataTypeOrLevel ID )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DataTypeOrLevel, "DataTypeOrLevel"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // ../completeOncoPrintSpecAST.g:175:4: discreteDataType
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_discreteDataType_in_dataTypeSpec265);
                    discreteDataType20=discreteDataType();

                    state._fsp--;

                    adaptor.addChild(root_0, discreteDataType20.getTree());

                    }
                    break;
                case 3 :
                    // ../completeOncoPrintSpecAST.g:176:4: continuousDataTypeInequality
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_continuousDataTypeInequality_in_dataTypeSpec271);
                    continuousDataTypeInequality21=continuousDataTypeInequality();

                    state._fsp--;

                    adaptor.addChild(root_0, continuousDataTypeInequality21.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "dataTypeSpec"

    public static class discreteDataType_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "discreteDataType"
    // ../completeOncoPrintSpecAST.g:182:1: discreteDataType : ( ( ID COMPARISON_OP ID ) -> ^( DiscreteDataType ID COMPARISON_OP ID ) | ( ID SIGNED_INT ) -> ^( DiscreteDataType ID SIGNED_INT ) );
    public final completeOncoPrintSpecASTParser.discreteDataType_return discreteDataType() throws RecognitionException {
        completeOncoPrintSpecASTParser.discreteDataType_return retval = new completeOncoPrintSpecASTParser.discreteDataType_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID22=null;
        Token COMPARISON_OP23=null;
        Token ID24=null;
        Token ID25=null;
        Token SIGNED_INT26=null;

        Object ID22_tree=null;
        Object COMPARISON_OP23_tree=null;
        Object ID24_tree=null;
        Object ID25_tree=null;
        Object SIGNED_INT26_tree=null;
        RewriteRuleTokenStream stream_SIGNED_INT=new RewriteRuleTokenStream(adaptor,"token SIGNED_INT");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMPARISON_OP=new RewriteRuleTokenStream(adaptor,"token COMPARISON_OP");

        try {
            // ../completeOncoPrintSpecAST.g:183:2: ( ( ID COMPARISON_OP ID ) -> ^( DiscreteDataType ID COMPARISON_OP ID ) | ( ID SIGNED_INT ) -> ^( DiscreteDataType ID SIGNED_INT ) )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==COMPARISON_OP) ) {
                    alt8=1;
                }
                else if ( (LA8_1==SIGNED_INT) ) {
                    alt8=2;
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
                    // ../completeOncoPrintSpecAST.g:184:2: ( ID COMPARISON_OP ID )
                    {
                    // ../completeOncoPrintSpecAST.g:184:2: ( ID COMPARISON_OP ID )
                    // ../completeOncoPrintSpecAST.g:184:4: ID COMPARISON_OP ID
                    {
                    ID22=(Token)match(input,ID,FOLLOW_ID_in_discreteDataType297);  
                    stream_ID.add(ID22);

                    COMPARISON_OP23=(Token)match(input,COMPARISON_OP,FOLLOW_COMPARISON_OP_in_discreteDataType299);  
                    stream_COMPARISON_OP.add(COMPARISON_OP23);

                    ID24=(Token)match(input,ID,FOLLOW_ID_in_discreteDataType301);  
                    stream_ID.add(ID24);


                    }



                    // AST REWRITE
                    // elements: ID, COMPARISON_OP, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 184:25: -> ^( DiscreteDataType ID COMPARISON_OP ID )
                    {
                        // ../completeOncoPrintSpecAST.g:184:28: ^( DiscreteDataType ID COMPARISON_OP ID )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DiscreteDataType, "DiscreteDataType"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_COMPARISON_OP.nextNode());
                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // ../completeOncoPrintSpecAST.g:185:4: ( ID SIGNED_INT )
                    {
                    // ../completeOncoPrintSpecAST.g:185:4: ( ID SIGNED_INT )
                    // ../completeOncoPrintSpecAST.g:185:6: ID SIGNED_INT
                    {
                    ID25=(Token)match(input,ID,FOLLOW_ID_in_discreteDataType323);  
                    stream_ID.add(ID25);

                    SIGNED_INT26=(Token)match(input,SIGNED_INT,FOLLOW_SIGNED_INT_in_discreteDataType325);  
                    stream_SIGNED_INT.add(SIGNED_INT26);


                    }



                    // AST REWRITE
                    // elements: ID, SIGNED_INT
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 185:22: -> ^( DiscreteDataType ID SIGNED_INT )
                    {
                        // ../completeOncoPrintSpecAST.g:185:25: ^( DiscreteDataType ID SIGNED_INT )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DiscreteDataType, "DiscreteDataType"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_SIGNED_INT.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "discreteDataType"

    public static class continuousDataTypeInequality_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "continuousDataTypeInequality"
    // ../completeOncoPrintSpecAST.g:190:1: continuousDataTypeInequality : ( ID COMPARISON_OP floatOrInt ) -> ^( ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt ) ;
    public final completeOncoPrintSpecASTParser.continuousDataTypeInequality_return continuousDataTypeInequality() throws RecognitionException {
        completeOncoPrintSpecASTParser.continuousDataTypeInequality_return retval = new completeOncoPrintSpecASTParser.continuousDataTypeInequality_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token ID27=null;
        Token COMPARISON_OP28=null;
        completeOncoPrintSpecASTParser.floatOrInt_return floatOrInt29 = null;


        Object ID27_tree=null;
        Object COMPARISON_OP28_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMPARISON_OP=new RewriteRuleTokenStream(adaptor,"token COMPARISON_OP");
        RewriteRuleSubtreeStream stream_floatOrInt=new RewriteRuleSubtreeStream(adaptor,"rule floatOrInt");
        try {
            // ../completeOncoPrintSpecAST.g:191:2: ( ( ID COMPARISON_OP floatOrInt ) -> ^( ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt ) )
            // ../completeOncoPrintSpecAST.g:192:2: ( ID COMPARISON_OP floatOrInt )
            {
            // ../completeOncoPrintSpecAST.g:192:2: ( ID COMPARISON_OP floatOrInt )
            // ../completeOncoPrintSpecAST.g:192:4: ID COMPARISON_OP floatOrInt
            {
            ID27=(Token)match(input,ID,FOLLOW_ID_in_continuousDataTypeInequality366);  
            stream_ID.add(ID27);

            COMPARISON_OP28=(Token)match(input,COMPARISON_OP,FOLLOW_COMPARISON_OP_in_continuousDataTypeInequality368);  
            stream_COMPARISON_OP.add(COMPARISON_OP28);

            pushFollow(FOLLOW_floatOrInt_in_continuousDataTypeInequality370);
            floatOrInt29=floatOrInt();

            state._fsp--;

            stream_floatOrInt.add(floatOrInt29.getTree());

            }



            // AST REWRITE
            // elements: ID, floatOrInt, COMPARISON_OP
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 192:34: -> ^( ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt )
            {
                // ../completeOncoPrintSpecAST.g:193:3: ^( ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ContinuousDataTypeInequality, "ContinuousDataTypeInequality"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                adaptor.addChild(root_1, stream_COMPARISON_OP.nextNode());
                adaptor.addChild(root_1, stream_floatOrInt.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "continuousDataTypeInequality"

    public static class floatOrInt_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "floatOrInt"
    // ../completeOncoPrintSpecAST.g:196:1: floatOrInt : ( SIGNED_FLOAT | SIGNED_INT );
    public final completeOncoPrintSpecASTParser.floatOrInt_return floatOrInt() throws RecognitionException {
        completeOncoPrintSpecASTParser.floatOrInt_return retval = new completeOncoPrintSpecASTParser.floatOrInt_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set30=null;

        Object set30_tree=null;

        try {
            // ../completeOncoPrintSpecAST.g:197:2: ( SIGNED_FLOAT | SIGNED_INT )
            // ../completeOncoPrintSpecAST.g:
            {
            root_0 = (Object)adaptor.nil();

            set30=(Token)input.LT(1);
            if ( input.LA(1)==SIGNED_INT||input.LA(1)==SIGNED_FLOAT ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(set30));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "floatOrInt"

    // Delegated rules


 

    public static final BitSet FOLLOW_userGeneList_in_oncoPrintSpecification45 = new BitSet(new long[]{0x0000000000A00052L});
    public static final BitSet FOLLOW_geneList_in_userGeneList65 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_userGeneList75 = new BitSet(new long[]{0x0000000000800040L});
    public static final BitSet FOLLOW_geneList_in_userGeneList77 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_userGeneList79 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_userGeneList85 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_userGeneList87 = new BitSet(new long[]{0x0000000000800040L});
    public static final BitSet FOLLOW_geneList_in_userGeneList89 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_userGeneList91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_individualGene_in_geneList135 = new BitSet(new long[]{0x0000000000800042L});
    public static final BitSet FOLLOW_defaultDataTypeSpec_in_geneList139 = new BitSet(new long[]{0x0000000000800042L});
    public static final BitSet FOLLOW_ID_in_individualGene155 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_fullDataTypeSpec_in_individualGene157 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_defaultDataTypeSpec190 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_fullDataTypeSpec_in_defaultDataTypeSpec194 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_fullDataTypeSpec222 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_dataTypeSpec_in_fullDataTypeSpec226 = new BitSet(new long[]{0x0000000002000040L});
    public static final BitSet FOLLOW_25_in_fullDataTypeSpec231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_dataTypeSpec251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_discreteDataType_in_dataTypeSpec265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_continuousDataTypeInequality_in_dataTypeSpec271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_discreteDataType297 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_COMPARISON_OP_in_discreteDataType299 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_discreteDataType301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_discreteDataType323 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_SIGNED_INT_in_discreteDataType325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_continuousDataTypeInequality366 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_COMPARISON_OP_in_continuousDataTypeInequality368 = new BitSet(new long[]{0x0000000000002800L});
    public static final BitSet FOLLOW_floatOrInt_in_continuousDataTypeInequality370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_floatOrInt0 = new BitSet(new long[]{0x0000000000000002L});

}