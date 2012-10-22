tree grammar completeOncoPrintSpecASTwalker;

options { 
	tokenVocab=completeOncoPrintSpecAST; // import tokens from completeOncoPrintSpecAST.g
	ASTLabelType=CommonTree;
}
@header { 
	package org.mskcc.cbio.portal.oncoPrintSpecLanguage;
	import java.util.ArrayList; 
	import org.mskcc.cbio.portal.oncoPrintSpecLanguage.*;
	import static java.lang.System.out;
}

// Alter code generation so catch-clauses get replaced with this action. 
@rulecatch { 
    catch (RecognitionException re) {
    		// trees s/b all well-formed, so don't expect these
        reportError(re);
        recover(input,re);
    } catch (OncoPrintLangException e) {
        newReportError(e);        
        recover(input, null ); // recover doesn't use the RecognitionException, so null's OK
    }
}

@members{
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
}

// everything is a userGeneList or a geneList, including a gene by itself
oncoPrintSpecification returns [OncoPrintSpecification theOncoPrintSpecification, 
	ArrayList<OncoPrintLangException> returnListOfErrors]
@init{
	// ACTION: initialize default OncoPrintSpecification
	$theOncoPrintSpecification = new OncoPrintSpecification( );
	//	theDefaultOncoPrintGeneDisplaySpec.setDefault();

	// APG ERRORS: initialize error list
	listOfErrors = new ArrayList<OncoPrintLangException>(); 	
}
	: 
	(	userGeneList 
	{
		$theOncoPrintSpecification.add( $userGeneList.theGeneSet );
	}
	)+
	// APG ERRORS: return list of all exceptions
	{
		$returnListOfErrors = listOfErrors;
	}
	;

userGeneList returns [GeneSet theGeneSet]
	: ^(UserGeneList geneList STRING? )
	{
		$geneList.theGeneSet.setUserGeneList( $STRING.text);
		$theGeneSet = $geneList.theGeneSet;
	}
	| geneList 
	{
		$theGeneSet = $geneList.theGeneSet;
	}
	;

geneList returns [GeneSet theGeneSet]
@init{
	// create GeneSet
	$theGeneSet = new GeneSet();
}
	:	
	(individualGene
	{
		// add gene to the set
		$theGeneSet.addGeneWithSpec( $individualGene.theGeneWithSpec );
	}
	| defaultDataTypeSpec )+ // nothing to do, but might be in the list
	;

individualGene returns [GeneWithSpec theGeneWithSpec]
	:
	^(IndividualGene ID fullDataTypeSpec?)
	// optional elements are null if not present
	{
		// TODO: remove this test, because gene names are checked elsewhere
		// check that ID is a gene
		if( !Gene.valid( $ID.text ) ){
			generateOncoError(  $ID.pos, $ID.line, $ID.text, " is not a valid gene or microRNA name." );
		}
		$theGeneWithSpec = GeneWithSpec.geneWithSpecGenerator( $ID.text, 
			$fullDataTypeSpec.theOncoPrintGeneDisplaySpec,
			theDefaultOncoPrintGeneDisplaySpec );
	}
	;

defaultDataTypeSpec 
	:	^(DefaultDataTypeSpec fullDataTypeSpec)
	{
		// set the global default 
		theDefaultOncoPrintGeneDisplaySpec = $fullDataTypeSpec.theOncoPrintGeneDisplaySpec;
	}
	;

fullDataTypeSpec returns [OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec]
@init{
		// create a Parsed ...
		ParsedFullDataTypeSpec theParsedFullDataTypeSpec = new ParsedFullDataTypeSpec();
}
	:	
	(dataTypeSpec
	{
		// save to a Parsed ...
		theParsedFullDataTypeSpec.addSpec( $dataTypeSpec.theDataTypeSpec );
	}
	)+
	{
		// clean the parsed, and return the simplified spec
		$theOncoPrintGeneDisplaySpec = theParsedFullDataTypeSpec.cleanUpInput();
	}
	;

dataTypeSpec returns [DataTypeSpec theDataTypeSpec] 
	:	
	^(DataTypeOrLevel ID) // may be discrete or continuous dataType or discrete level
		{
		$theDataTypeSpec = ConcreteDataTypeSpec.concreteDataTypeSpecGenerator( $ID.text );
		if( null == $theDataTypeSpec ){
			$theDataTypeSpec = DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGeneratorByLevelName( $ID.text );
		}
		if( null == $theDataTypeSpec ){
					// APG ERRORS: throw IAE
			generateOncoError(   $ID.pos, $ID.line,   $ID.text, " is not a valid genetic data type or data level."   );
		}
	}
	| discreteDataType 
	{
		$theDataTypeSpec = $discreteDataType.theDataTypeSpec;
	}
	| continuousDataTypeInequality
	{
		$theDataTypeSpec = $continuousDataTypeInequality.theContinuousDataTypeSpec;
	}
	;

discreteDataType returns [DataTypeSpec theDataTypeSpec] 
	:	
	// since the tree has 2 IDs, they need separate names; handy ANTLR feature
	^(DiscreteDataType type=ID COMPARISON_OP level=ID )
	{
		$theDataTypeSpec = DiscreteDataTypeSpec.discreteDataTypeSpecGenerator( $type.text, $COMPARISON_OP.text, $level.text );
		// TODO: more specific error about type or level
		if( null == $theDataTypeSpec ){
			// APG ERRORS: throw IAE
			generateOncoError(   $type.pos, $type.line, $type.text+ " " + $COMPARISON_OP.text+ " " +  $level.text,
				" is not a valid discrete genetic data type and discrete genetic data level." );
		}
	}
	| 
	^(DiscreteDataType ID SIGNED_INT )
	{
		$theDataTypeSpec = DiscreteDataTypeSetSpec.discreteDataTypeSetSpecGeneratorByLevelCode( $ID.text, $SIGNED_INT.text );
		if( null == $theDataTypeSpec ){
			// APG ERRORS: throw IAE
			generateOncoError(   $ID.pos, $ID.line, $ID.text + " " + $SIGNED_INT.text, 
				" is not a valid genetic data type and GISTIC code." );
		}
	}
	;

floatOrInt returns [String number]
	:	
	SIGNED_FLOAT 
	{
		$number = $SIGNED_FLOAT.text;
	}
	| SIGNED_INT 
	{
		$number = $SIGNED_INT.text;
	}
	;

continuousDataTypeInequality returns [ContinuousDataTypeSpec theContinuousDataTypeSpec]
	:	
	^(ContinuousDataTypeInequality ID COMPARISON_OP floatOrInt )
	{
		$theContinuousDataTypeSpec = ContinuousDataTypeSpec.continuousDataTypeSpecGenerator( 
			$ID.text, $COMPARISON_OP.text, $floatOrInt.number );
		if( null == $theContinuousDataTypeSpec ){
				generateOncoError(   $ID.pos, $ID.line, $ID.text, " is not a valid genetic data type." );
		}
	}
	;