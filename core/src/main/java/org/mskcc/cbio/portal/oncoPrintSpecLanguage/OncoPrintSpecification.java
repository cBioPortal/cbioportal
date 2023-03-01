/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.util.ArrayList;
import static java.lang.System.out;

public class OncoPrintSpecification {
    ArrayList<GeneSet> GeneSets;

    public OncoPrintSpecification() {
        GeneSets = new ArrayList<GeneSet>();
    }
    
    /**
     * create an OncoPrintSpecification that has the OncoPrintGeneDisplaySpec default filter for a set of genes.
     * a convenience constructor.
     * @param genes
     */
    public OncoPrintSpecification(String[] genes) {
       OncoPrintGeneDisplaySpec aDefaultOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
       aDefaultOncoPrintGeneDisplaySpec.setDefault( 1.0, 1.0 );
       constructOncoPrintSpecification( genes, aDefaultOncoPrintGeneDisplaySpec );
   }
    
    /**
     * create an OncoPrintSpecification that has the given OncoPrintGeneDisplaySpec filter on all genes in an array.
     * @param genes
     * @param theOncoPrintGeneDisplaySpec
     */
    public OncoPrintSpecification(String[] genes, OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec ) {
       constructOncoPrintSpecification( genes, theOncoPrintGeneDisplaySpec );
   }
    
    private void constructOncoPrintSpecification(String[] genes, OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec ) {
       this.GeneSets = new ArrayList<GeneSet>();
       GeneSet aGeneSet = new GeneSet( );
       for( String g : genes){
          aGeneSet.addGeneWithSpec( new GeneWithSpec( g, theOncoPrintGeneDisplaySpec ) );
       }
       this.add(aGeneSet);
   }
   
    public ArrayList<GeneSet> getGeneSets() {
        return GeneSets;
    }
    
    public void add( GeneSet aGeneSet ){
        this.GeneSets.add(aGeneSet);
    }
    
   /**
    * return the names of genes in this spec; may contain duplicates
    * 
    * @return
    */
   public ArrayList<String> listOfGenes() {
      ArrayList<String> allGenes = new ArrayList<String>();
      for (GeneSet aGeneSet : this.GeneSets) {
         allGenes.addAll(aGeneSet.listOfGenes());
      }
      return allGenes;
   }

   public boolean containsGene(String geneName) {
      return this.listOfGenes().contains(geneName);
   }
   
   /**
    * return the GeneWithSpec for the first gene with the given name, if any
    * TODO: as the same gene could appear multiple times in a spec this should return a set of GeneWithSpec; for now return the 1st
    * @param geneName
    * @return
    */
   public GeneWithSpec getGeneWithSpec(String geneName) {
      for (GeneSet aGeneSet : this.GeneSets) {
         GeneWithSpec aGeneWithSpec = aGeneSet.getGeneWithSpec(geneName);
         if (null != aGeneWithSpec) {
            return aGeneWithSpec;
         }
      }
      return null;
   }
   
   /**
    * Get the union of all possible alterations.
    * This is used to determine legends for figures (like the OncoPrint)
    * that are configured by an OncoPrintSpecification, and overall errors in the OncoPrintSpecification.
    * <p>
    * E.g., if this OncoPrintSpecification describes two genes, and one gene shows AMP and Mutations, and the other AMP and under-expression below -2, 
    * then this will return an OncoPrintGeneDisplaySpec that would show AMP, Mutations, and under-expression below -2. 
    * The point is that a legend shouldn't list an alteration that cannot be presented.
    * <p>
    * @return the union of all possible alterations
    */
   // TODO: HIGH: UNIT TEST in OncoSpec
   public OncoPrintGeneDisplaySpec getUnionOfPossibleLevels() {
      
      ParsedFullDataTypeSpec aParsedFullDataTypeSpec = new ParsedFullDataTypeSpec();

      // use cleanUpInput(), the 'unioning' code that cleans up the input from the parser
      // for all gene sets, for all genes, for all genetic data types, get discrete or continuous type specs
      for( GeneSet aGeneSet: this.GeneSets){
         for( GeneWithSpec aGeneWithSpec: aGeneSet.getGenes()){
            for( GeneticDataTypes theGeneticDataType : GeneticDataTypes.values()){

               ResultDataTypeSpec theResultDataTypeSpec = 
                  aGeneWithSpec.theOncoPrintGeneDisplaySpec.getResultDataTypeSpec(theGeneticDataType);
               if( null != theResultDataTypeSpec ){
                  ResultDataTypeSpec copyOfResultDataTypeSpec = ResultDataTypeSpec.newInstance( theResultDataTypeSpec ); 
                  if( copyOfResultDataTypeSpec.isAcceptAll() ){
                     aParsedFullDataTypeSpec.addSpec( new ConcreteDataTypeSpec( theGeneticDataType ) );
                  }else{
                     aParsedFullDataTypeSpec.addSpec( copyOfResultDataTypeSpec.getTheDiscreteDataTypeSetSpec() );                     
                     aParsedFullDataTypeSpec.addSpec( copyOfResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec() );
                     aParsedFullDataTypeSpec.addSpec( copyOfResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec() );
                  }
               }
            }
         }
      }      

      OncoPrintGeneDisplaySpec tempOncoPrintGeneDisplaySpec = aParsedFullDataTypeSpec.cleanUpInput(); 
      return tempOncoPrintGeneDisplaySpec;
   }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer(); 
        for( GeneSet aGeneSet: this.GeneSets ) {
            sb.append(aGeneSet.toString() + "\n");
        }
        return sb.toString();
    }    
    // TODO: COPY
    // TODO: equals
    @Override public final Object clone() throws CloneNotSupportedException {
       throw new CloneNotSupportedException();
     }   

}