/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import static java.lang.System.out;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

public class CompleteOncoPrintSpecASTdriver {

    public static void main(String[] args) throws Exception {
       
       // TODO: MOVE TO TEST CODE
       for( int j=0; j<20; j++){
          String s = randomInput( 100 ) ;
          out.println( j + ": " + s );
          printObject( (OncoPrintSpecification)parseMethod( "oncoPrintSpecification",  s, args ) );
       }
       /*
       printObject( (OncoPrintSpecification)parseMethod( "oncoPrintSpecification",  
             "DATATYPES : EXP MUT ; gene2 g3:  CNA 2 ; g4 notGene \n", args ) );
       printObject( parseMethod( "oncoPrintSpecification", "", args ) );
       printObject( parseMethod( "oncoPrintSpecification", " \"hi mom\" { g3 g4} " +
            " { g1 DATATYPES : EXP MUT ; g2 }" +
            "DATATYPES : EXP MUT ; gene2 g3:  CNA 2 ; g4 notGene \n" +
            "", args ) );

       printObject( parseMethod( "userGeneList", " \"hi mom\" { g3 g4}", args ) );
       printObject( parseMethod( "userGeneList", " { g1 DATATYPES : EXP MUT ; g2 } ", args ) );
       
       printObject( parseMethod( "userGeneList",  " g0 GENE1 : CNA > HetLoss ; " +
             "DATATYPES : EXP MUT ; gene2 g3:  CNA 2 ; g4 notGene \n" +
             "f1: meth;", args ) );
       printObject( parseMethod( "geneList",  " g0 GENE1 : CNA > HetLoss ; " +
             "DATATYPES : EXP MUT ; gene2 g3:  CNA 2 ; g4 notGene \n" +
             "f1: meth;", args ) );
        
       printObject( parseMethod( "fullDataTypeSpec",  ": CNA > HetLoss Mut Exp < 3.5 ;", args ) );
       printObject( parseMethod( "fullDataTypeSpec",  ": Exp <= -1 Exp < -1 Exp > 1 Exp > 2 ;", args ) );
       printObject( parseMethod( "fullDataTypeSpec",  ": Exp foo;", args ) );
       printObject( parseMethod( "fullDataTypeSpec",  ": Exp foo bar 876 *&^ ;", args ) );

       printObject( parseMethod( "individualGene",  "foo", args ) );
       printObject( parseMethod( "individualGene",  "gene1: Methylation < 2;", args ) );
       printObject( parseMethod( "individualGene",  "notGene", args ) );
       
       printObject( parseMethod( "dataTypeSpec",  "CNA > HetLoss", args ) );
       printObject( parseMethod( "dataTypeSpec",  "\n\n  CNA > elbow", args ) );
       printObject( parseMethod( "dataTypeSpec",  "butt > HetLoss", args ) );
       printObject( parseMethod( "dataTypeSpec",  "CNA 2", args ) );
       printObject( parseMethod( "dataTypeSpec",  "CNA -1", args ) );

       printObject( parseMethod( "dataTypeSpec",  "CNA -4", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Mut -1", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Mut 0", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Mut 1", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Exp < 3.5", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Methylation < 2", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Methy < -1", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Ugly <= -1", args ) );
       */
       
       printObject( parseMethod( "dataTypeSpec",  "CNA", args ) );
       printObject( parseMethod( "dataTypeSpec",  "GAI", args ) );
       printObject( parseMethod( "dataTypeSpec",  "EXP", args ) );

       printObject( parseMethod( "dataTypeSpec",  "mrna", args ) );
       printObject( parseMethod( "dataTypeSpec",  "FOO", args ) );
       printObject( parseMethod( "dataTypeSpec",  "CNA < 3.5", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Exp << 3.5", args ) );
       printObject( parseMethod( "dataTypeSpec",  "Exp < x", args ) );
       printObject( parseMethod( "dataTypeSpec",  "foo < 3.5", args ) );
       printObject( parseMethod( "dataTypeSpec", "foo < 3.5", args ) );
    }
    
    static void printObject( Object o){
       if( null == o){
          // out.println( "null object" );
       }else{
          out.println( o.toString() );
       }
    }
    
    /**
     * return a string of n random tokens
     * @param n
     * @return
     */
    static Random r = new Random(17);
    private static String randomInput( int n ){
       StringBuffer sb=new StringBuffer();
       String tokens = "\\\" \t \r \n 0 1 2 3 4 5 6 .3 3. 0.2 . - + : ; : ; : ; : ; = == <= < > >= nonGene exp cna c mut methy expre mrna " +
       		"Gene1 gene2 DATATYPES \" DATATYPES \" { } { } { }";
       String[] tokenList = tokens.split( " +");
       // Random r = new Random();
       for( int i=0; i<n; i++){
          sb.append( tokenList[ r.nextInt(tokenList.length) ] ).append(" ");
       }
       return sb.toString();
    }
    
    // call method 'method' in the lexer and parser
    // loads of reflection
    public static Object parseMethod( String method, String prog, String[] args ) throws RecognitionException{

          try {
            CommonTokenStream tokens = getCommonTokenStream( prog, args );
             // Create a parser attached to the token stream
             completeOncoPrintSpecASTParser parser = new completeOncoPrintSpecASTParser(tokens);

             // Invoke the program rule in get return value
             // get parser class
             Class<?> parserClass = parser.getClass(); // Class.forName("main.completeOncoPrintSpecASTParser.parser");
             //System.out.println( "parserClass is: " + parserClass.getName() );
             // create (empty) arg list
             Class<?>[] emptyArgList = new Class[] {};
             // get method
             Method parserMethod = parserClass.getDeclaredMethod( method, emptyArgList);
             //System.out.println( "parserMethod is: " + parserMethod.getName() );
             
             // call method
             // was completeOncoPrintSpecASTParser.fullDataTypeSpec_return r = parser.fullDataTypeSpec();
             Object parserRV = parserMethod.invoke( parser );

             // now call returnedObject's getTree() 
             // get return value class
             Class<?> rvClass = parserRV.getClass();
             //System.out.println( "rvClass is: " + rvClass.getName() );
             // get method
             Method rvClassGetTree = rvClass.getDeclaredMethod( "getTree", emptyArgList);
             //System.out.println( "rvClassGetTree is: " + rvClassGetTree.getName() );
             // call method
             // was: CommonTree t = (CommonTree)r.getTree();
             CommonTree theCommonTree = (CommonTree)rvClassGetTree.invoke(parserRV );
             //System.out.println( "theCommonTree is: " + "\n"+ theCommonTree.toStringTree() );

             // Walk resulting tree; create treenode stream first
             CommonTreeNodeStream nodes = new CommonTreeNodeStream(theCommonTree);
             // AST nodes have payloads that point into token stream
             nodes.setTokenStream(tokens); 
             // Create a tree Walker attached to the nodes stream
             completeOncoPrintSpecASTwalker walker = new completeOncoPrintSpecASTwalker(nodes);

             // Invoke the start symbol, rule program
             // invoke method 'method' on walker
             return walker.getClass().getDeclaredMethod(method, emptyArgList).invoke(walker, (Object[])null);
             // was: return walker.fullDataTypeSpec();
             
         } catch (SecurityException e) {
            
            e.printStackTrace();
         } catch (IllegalArgumentException e) {
            
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (NoSuchMethodException e) {
            
            e.printStackTrace();
         } catch (IllegalAccessException e) {
            
            e.printStackTrace();
         } catch (InvocationTargetException e) {
            
            e.printStackTrace();
         }

      return null;
    }

    static private CommonTokenStream getCommonTokenStream( String prog, String[] args )throws IOException{
       ByteArrayInputStream bs = new ByteArrayInputStream( prog.getBytes());
       ANTLRInputStream input = new ANTLRInputStream(bs);
       // Create a lexer attached to that input stream
       completeOncoPrintSpecASTLexer lexer = new completeOncoPrintSpecASTLexer(input);
       // Create a stream of tokens pulled from the lexer
       return new CommonTokenStream(lexer);
    }
       
}