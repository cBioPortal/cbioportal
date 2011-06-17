package org.mskcc.portal.oncoPrintSpecLanguage;

import java.util.ArrayList;

/**
 * the data returned by a call to the parser
 * @author Arthur Goldberg
 */
public class ParserOutput {
   ArrayList<String> syntaxErrors;
   ArrayList<OncoPrintLangException> semanticsErrors;
   OncoPrintSpecification theOncoPrintSpecification;

   public ParserOutput(ArrayList<String> syntaxErrors, ArrayList<OncoPrintLangException> semanticsErrors,
        OncoPrintSpecification theOncoPrintSpecification) {
      // TODO: make sure parser cannot store nulls in these 2, and remove the conditionals
      if( null == syntaxErrors ){
         this.syntaxErrors = new ArrayList<String>();
      }else{
         this.syntaxErrors = syntaxErrors;
      }
      if( null == semanticsErrors ){
         this.semanticsErrors = new ArrayList<OncoPrintLangException>();
      }else{
         this.semanticsErrors = semanticsErrors;
      }
     this.theOncoPrintSpecification = theOncoPrintSpecification;
  }

   public ArrayList<String> getSyntaxErrors() {
      return syntaxErrors;
   }

   public ArrayList<OncoPrintLangException> getSemanticsErrors() {
      return semanticsErrors;
   }

   public OncoPrintSpecification getTheOncoPrintSpecification() {
      return theOncoPrintSpecification;
   }
   
}

