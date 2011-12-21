grammar completeOncoPrintSpecAST;

options {output=AST;} 
@parser::header { 
	package org.mskcc.portal.oncoPrintSpecLanguage;
	import java.util.ArrayList; 
	import org.mskcc.portal.oncoPrintSpecLanguage.*;
	import static java.lang.System.out;
	import java.io.PrintStream;
}
@lexer::header{
	package org.mskcc.portal.oncoPrintSpecLanguage;
}

@members{
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
	ArrayList<String> errorMessages = Utilities.getErrorMessages();

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
}

// everything is a userGeneList, including a gene by itself
oncoPrintSpecification
	: 
	( userGeneList )+ -> userGeneList+
	;

userGeneList
	:
	geneList 
	| 
	( '{' geneList '}' 
	| STRING '{' geneList '}' ) -> ^(UserGeneList geneList STRING?)
	;	
UserGeneList :	 '&userGeneList';

// a list of genes and/or default specs
geneList
	:	
	// build a list of interleaved genes and defaultDataTypeSpecs
	// as Parr TDAR says, at start of 7.4
	// "the automatic AST construction mechanism ... [automatically] builds a tree, albeit a flat one."
	( individualGene | defaultDataTypeSpec )+
	;

// a gene and its optional data type specification
individualGene
	:
	ID fullDataTypeSpec? -> ^(IndividualGene ID fullDataTypeSpec?)
	;
IndividualGene 
	:	'&individualGene';

// sets the global default 
defaultDataTypeSpec
	:	'DATATYPES' 
	fullDataTypeSpec -> ^(DefaultDataTypeSpec fullDataTypeSpec)
	;
DefaultDataTypeSpec
	:	'&defaultDataTypeSpec';

// all the data type specs for a gene or DATATYPES
fullDataTypeSpec
	:	':' ( dataTypeSpec )+ ';' -> dataTypeSpec+
	;

// a single data type spec
// TODO: very strange; dataTypeSpec will not parse an ID, but fullDataTypeSpec will parse it; bizarre
dataTypeSpec
	:	
	ID -> ^(DataTypeOrLevel ID) // may be discrete or continuous dataType or discrete level
	| discreteDataType 
	| continuousDataTypeInequality
	;
DataTypeOrLevel
	:	'&DataTypeOrLevel' ;

// a discrete data type, currently either CNA or mutation
discreteDataType 
	:	
	( ID COMPARISON_OP ID )-> ^(DiscreteDataType ID COMPARISON_OP ID )	// all discrete levels that satisfy the condition
	| ( ID SIGNED_INT ) -> ^(DiscreteDataType ID SIGNED_INT )				// the discrete level indicated by the INT
	;
DiscreteDataType 	:	'&DiscreteDataType';

// a continous data type (expression or methylation) that satisfies the inequality
continuousDataTypeInequality
	:	
	( ID COMPARISON_OP floatOrInt ) -> 
		^(ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt ) 
	;

floatOrInt
	:	SIGNED_FLOAT | SIGNED_INT ;

// dummy tokens because ANTLR rewrite rules cannot use constants as node names! Argh!
ContinuousDataTypeInequality 	:	'&ContinuousDataTypeInequality';
	
COMPARISON_OP 
	: ( '<=' | '<' | '>' | '>=' )	
	;		 	

ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-'|'*'|'/')*
    ;

SIGNED_FLOAT
    :  ('-')? ( INT+ '.' INT* 
    |  '.' INT+ )
    ;

SIGNED_INT : ('-')?	INT+ ;

fragment INT : ('0' .. '9') ; 

WS :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
        ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
        ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;
