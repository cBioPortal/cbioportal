package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

/**
 * used to report errors in the AST walker 
 * @author Arthur Goldberg
 */
public class OncoPrintLangException extends RuntimeException{
   public OncoPrintLangException( String msg){
      super(msg);
   }
}
