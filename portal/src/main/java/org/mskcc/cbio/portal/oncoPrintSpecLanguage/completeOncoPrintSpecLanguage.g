grammar completeOncoPrintSpecLanguage;

// Old implementation used before breaking OQL into a tree generator and a tree walker
// not in use

@parser::header { 
	package main;
	import java.util.ArrayList; 
	import org.mskcc.cbio.portal.oncoPrintSpecLanguage.*;
	import static java.lang.System.out;
}
@lexer::header{
	package main;
}
@members{
	// create default, which can be replaced with 'DATATYPES' specs
	ResultFullDataTypeSpec theDefaultResultFullDataTypeSpec = new ResultFullDataTypeSpec( );
	
	String[] ruleNameToDataTypeNames = { 
		"geneName", "gene",
		"dataTypeName", "genetic data type",
		"dataTypeLevel", "genetic data level"};
	public String lookupDataTypeName( String ruleName ){
		for( int i = 0; i<ruleNameToDataTypeNames.length; i+=2){
			if( ruleNameToDataTypeNames[i].equals( ruleName ) ){
				return ruleNameToDataTypeNames[i+1];
			}
		}
		return "unknown type";			
	}

	// override to make comprehensible errors for users
   public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	//      String hdr = getErrorHeader(e);
		int c = e.charPositionInLine;
		String hdr = "Error at char " + c + " of line "+e.line+": ";
      String msg = getErrorMessage(e, tokenNames);
      emitErrorMessage( hdr +  msg);
   }

   public String getErrorMessage(RecognitionException e, String[] tokenNames) {
      String msg = e.getMessage();
      emitErrorMessage( "received " + e.getClass().getName() + " with token " + e.token.getText() );
      
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
         msg = "no viable alternative at input " + getTokenErrorDisplay(e.token);
      } else if (e instanceof EarlyExitException) {
         // EarlyExitException eee = (EarlyExitException)e;
         // for development, can add "(decision="+eee.decisionNumber+")"
         msg = "required (...)+ loop did not match anything at input "
               + getTokenErrorDisplay(e.token);
      } else if (e instanceof MismatchedSetException) {
         MismatchedSetException mse = (MismatchedSetException) e;
         msg = "mismatched input " + getTokenErrorDisplay(e.token) + " expecting set "
               + mse.expecting;
      } else if (e instanceof MismatchedNotSetException) {
         MismatchedNotSetException mse = (MismatchedNotSetException) e;
         msg = "mismatched input " + getTokenErrorDisplay(e.token) + " expecting set "
               + mse.expecting;
      } else if (e instanceof FailedPredicateException) {
         FailedPredicateException fpe = (FailedPredicateException) e;
			// msg = "rule " + fpe.ruleName + " failed predicate: {" + fpe.predicateText + "}?";
         msg = "token '" + e.token.getText() + "' is not a valid '" + lookupDataTypeName( fpe.ruleName ) +"'.";
      }
      return msg;
   }

    /*
public String getErrorMessage(RecognitionException e,
                              String[] tokenNames){
	List<String> stack = getRuleInvocationStack(e, this.getClass().getName());
	int charPos = e.charPositionInLine+1;
	String rule = stack.get(0);                              
	return " is not a valid " + e.getClass().getName() +" "+ rule + " at char " + charPos + " of line " + e.line;
    // "X" is not a valid "Y" at line l, char c; valid "Y"s are "".
    if( e isInstanceOf FailedPredicateException){
    }
     
}
    */
}

// everything is a groupedGeneList, including a gene by itself
oncoPrintSpecification returns [OncoPrintSpecification theOncoPrintSpecification]
@init{
	// ACTION: initialize default OncoPrintSpecification
	$theOncoPrintSpecification = new OncoPrintSpecification( );
	theDefaultResultFullDataTypeSpec.setDefault();
}
	: 
	( 	groupedGeneList 
	{
		$theOncoPrintSpecification.add( $groupedGeneList.theGeneSet );
	}
	)+
	;

groupedGeneList returns [GeneSet theGeneSet]
	:
	geneList 
	{
		$theGeneSet = $geneList.theGeneSet;
	}
	// TODO: next phase		
	| '{' geneList '}' 			
	{
		$theGeneSet = $geneList.theGeneSet;
		$theGeneSet.setUserGeneList(true);
	}
	// TODO: next phase		
	| STRING '{' geneList '}' 	
	{
		$theGeneSet = $geneList.theGeneSet;
		$theGeneSet.setName( $STRING.text );
	}
	;	

geneList returns [GeneSet theGeneSet]
@init{
	$theGeneSet = new GeneSet();
}
	:	
	( individualGene 				
	{
		// ACTION: add individualGene to G; 
		$theGeneSet.addGeneWithSpec( $individualGene.theGeneWithSpec );
	}
	| defaultDataTypeSpec 	// nothing to do: defaultDataTypeSpec sets theDefaultResultFullDataTypeSpec
	)+	
	{ // todo: ACTION: if G doesn't have any genes throw error
	}
	;

individualGene returns [GeneWithSpec theGeneWithSpec]
	:
	geneName
	{
		$theGeneWithSpec = new GeneWithSpec( $geneName.name );
		// copy the defaultDataTypeSpec to it
		$theGeneWithSpec.setTheResultFullDataTypeSpec( theDefaultResultFullDataTypeSpec );
	}
	( fullDataTypeSpec 
	{
		// ACTION: set dataTypeSpec for internalGeneWithSpec
		$theGeneWithSpec.setTheResultFullDataTypeSpec( $fullDataTypeSpec.theParsedFullDataTypeSpec.cleanUpInput() );
	}
	)?	
	;
	
geneNameOld returns [String name]
	: ID 
	{
		// ACTION: if the ID is a valid gene return ID else return null (Should we throw error?)
		if( Gene.valid( $ID.text )){
			$name = $ID.text;
		}else{
			$name = null;
		}
	}
	;
	
geneName returns [String name]
	:	{ Gene.valid( input.LT(1).getText() ) }? ID 
	// TODO: handle error when isDataTypeName fails
		{ $name = $ID.text; }
	;	

defaultDataTypeSpec returns [ResultFullDataTypeSpec theResultFullDataTypeSpec]
	:	'DATATYPES' fullDataTypeSpec
	{
	//	out.println( "in defaultDataTypeSpec, cleanUp  "+ $fullDataTypeSpec.theParsedFullDataTypeSpec.toString() );
		$theResultFullDataTypeSpec = $fullDataTypeSpec.theParsedFullDataTypeSpec.cleanUpInput();
		// ACTION: reset the local defaultDataTypeSpec
		theDefaultResultFullDataTypeSpec = $theResultFullDataTypeSpec;
	}
	;

fullDataTypeSpec returns [ParsedFullDataTypeSpec theParsedFullDataTypeSpec]
@init{
	$theParsedFullDataTypeSpec = new ParsedFullDataTypeSpec();
}
	:	':' ( dataTypeSpec 
	{
	/*
	out.print( "adding dataTypeSpec ");
	if(  $dataTypeSpec.theDataTypeSpec == null) {
		out.println( "that's null ");
	}else{
		out.println( $dataTypeSpec.theDataTypeSpec.toString()	);
	}	
	*/
		$theParsedFullDataTypeSpec.addSpec( $dataTypeSpec.theDataTypeSpec );
	}
	)+ ';'
	;

dataTypeSpec returns [DataTypeSpec theDataTypeSpec] 
	:	
	dataTypeName // may be discrete or continuous
	{
		$theDataTypeSpec = new ConcreteDataTypeSpec( $dataTypeName.text );
	}
	| dataTypeLevel  // must be discrete
	{
		$theDataTypeSpec = new DiscreteDataTypeSetSpec( $dataTypeLevel.text );
	}
	| discreteDataType  
	{
		$theDataTypeSpec = $discreteDataType.theDataTypeSpec;
	}
	| continuousDataTypeInterval
	{
		$theDataTypeSpec = $continuousDataTypeInterval.theContinuousDataTypeSpec;
	}
	;

discreteDataType returns [DataTypeSpec theDataTypeSpec] 
	:	
		//	( comparisonOP dataTypeLevel ) |  confuses next alternative with 'dataTypeName' 'comparisonOP dataTypeLevel'
	( dataTypeName comparisonOP dataTypeLevel 
	{
		$theDataTypeSpec = new DiscreteDataTypeSpec( 
			DiscreteDataTypeSpec.findDataType( $dataTypeName.text ),
				$comparisonOP.theComparisonOp, 
				GeneticTypeLevel.findDataTypeLevel( $dataTypeLevel.text ) );
	}
	) |
	( dataTypeName SIGNED_INT )
	{
		$theDataTypeSpec = new DiscreteDataTypeSetSpec( 
			$dataTypeName.text,  Integer.parseInt( $SIGNED_INT.text ) );
	}
	;

// TODO: rename to continuousDataTypeInequality
continuousDataTypeInterval returns [ContinuousDataTypeSpec theContinuousDataTypeSpec]
	:	
	{
		float threshold = 0.0f;
	}
	( dataTypeName comparisonOP 
		( SIGNED_INT { threshold = (float) Integer.parseInt( $SIGNED_INT.text);}
		| SIGNED_FLOAT { threshold = Float.parseFloat( $SIGNED_FLOAT.text);}
		) )
	{
		$theContinuousDataTypeSpec = new ContinuousDataTypeSpec( 
			ContinuousDataTypeSpec.findDataType( $dataTypeName.text ),
			$comparisonOP.theComparisonOp, threshold );
	}
	;

dataTypeName returns [String text]
	: { DataTypeSpecEnumerations.isDataTypeName( input.LT(1).getText() ) }?
	ID 	// was 
	//	{ DataTypeSpecEnumerations.isDataTypeName( $ID.text ) }?
		// TODO: handle error when isDataTypeName fails
		{ $text = $ID.text; }
	;
	
dataTypeLevel returns [String text] 
	:	
	// don't want this hoisted
	{ DataTypeSpecEnumerations.isDataTypeLevel( input.LT(1).getText() ) }? 
	ID 
	//	{ DataTypeSpecEnumerations.isDataTypeLevel( $ID.text ) }? 
	// TODO: handle error when isDataTypeLevel fails
		{ $text = $ID.text; }
	;

comparisonOP returns [ComparisonOp theComparisonOp ]
	:	COMPARISON_OP 
		{
			// ACTION: convert to enumeration
			$theComparisonOp = ComparisonOp.convertCode( $COMPARISON_OP.text );
		}
	;
	
COMPARISON_OP 
	// awkward to convert to enumeration in COMPARISON_OP cuz of char / text distinction for 1/longer tokens; see bottom p. 139 T. Parr 
	: ( '<=' | '<' | '>' | '>=' )	
	;		 	

ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

SIGNED_INT : ('-')?	'0'..'9'+ ;

SIGNED_FLOAT
    :  ('-')? ( ('0'..'9')+ '.' ('0'..'9')* 
    |  '.' ('0'..'9')+ 
    |  ('0'..'9')+ )
    ;

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
