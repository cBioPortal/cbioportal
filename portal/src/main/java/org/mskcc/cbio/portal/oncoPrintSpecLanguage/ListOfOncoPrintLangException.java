package org.mskcc.portal.oncoPrintSpecLanguage;

// TODO: delete
import java.util.ArrayList;

/*
 * this exception is thrown by a method that's declared to throw OncoPrintLangException (org.antlr.runtime.OncoPrintLangException extends java.lang.Exception)
 */
public class ListOfOncoPrintLangException extends RuntimeException {

   private ArrayList<OncoPrintLangException> theOncoPrintLangExceptions; 

   public ListOfOncoPrintLangException(ArrayList<OncoPrintLangException> theOncoPrintLangExceptions) {
      this.theOncoPrintLangExceptions = theOncoPrintLangExceptions;
  }

   public ArrayList<OncoPrintLangException> getTheOncoPrintLangExceptions() {
      return theOncoPrintLangExceptions;
   }
   
}
