/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

public class CallOncoPrintSpecParser {

   /**
    * call the parser's root
    * @param language
    * @return 
    */
  public static ParserOutput callOncoPrintSpecParser( String specLanguage, OncoPrintGeneDisplaySpec anOncoPrintGeneDisplaySpec ){

     try {
        
        specLanguage = reverseXSSclean( specLanguage );

//        Utilities.clearErrorMessageList();         
        // add end of line ;s where needed
         ByteArrayInputStream bs = new ByteArrayInputStream( Utilities.appendSemis(specLanguage).getBytes() );
        ANTLRInputStream input = new ANTLRInputStream(bs);
        
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getLineNumber());
        // Create a lexer attached to that input stream
        completeOncoPrintSpecASTLexer lexer = new completeOncoPrintSpecASTLexer(input);
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());
        // Create a stream of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());
        // Create a parser attached to the token stream
        completeOncoPrintSpecASTParser parser = new completeOncoPrintSpecASTParser(tokens);
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());

        // Invoke the program rule in get return value
        completeOncoPrintSpecASTParser.oncoPrintSpecification_return r = parser.oncoPrintSpecification();
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());

        // now call returnedObject's getTree()
        CommonTree t = (CommonTree)r.getTree();
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());
        // Walk resulting tree; create treenode stream first
        CommonTreeNodeStream nodes = new CommonTreeNodeStream( t );
        // AST nodes have payloads that point into token stream
        // for debugging   System.err.println( "at " + Thread.currentThread().getStackTrace()[1].getFileName() + ":" + Thread.currentThread().getStackTrace()[1].getLineNumber());
        nodes.setTokenStream(tokens);
        // Create a tree Walker attached to the nodes stream
        completeOncoPrintSpecASTwalker walker = new completeOncoPrintSpecASTwalker(nodes);
        
        // Invoke the start symbol
        completeOncoPrintSpecASTwalker.oncoPrintSpecification_return rv 
           = (completeOncoPrintSpecASTwalker.oncoPrintSpecification_return) walker.oncoPrintSpecification( anOncoPrintGeneDisplaySpec );
        
        ParserOutput theParserOutput = new ParserOutput(
//              Utilities.getErrorMessages(),
                 // TODO: HIGH: UNIT TEST in OncoSpec                 
                 null,
              rv.returnListOfErrors,
              rv.theOncoPrintSpecification );
        return theParserOutput;

   } catch (IOException e) {
      e.printStackTrace();
   } catch (RecognitionException e) {
      e.printStackTrace();
   }
   return null;
  }
  
  // TODO: replace this with a comprehensive inverse of ServletXssUtil.getCleanInput(); cannot find any documentation on that now
  public static String reverseXSSclean( String langInput ){
     // xssUtil cleaning screws up < and > signs; replace them
     return langInput.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
  }

}