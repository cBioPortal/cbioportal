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

import java.util.ArrayList;

/**
 * A temporary class, created by RawFullDataTypeSpec while it is cleaning up parser 
 * output from fullDataTypeSpec and creating the ResultFullDataTypeSpec.
 * 
 * @author Arthur Goldberg
 *
 */
public class TempFullDataTypeSpec {
    private ArrayList<DataTypeSpec> theDataTypeSpecs;
    private ArrayList<DiscreteDataTypeSpec> theDiscreteDataTypeSpecs;
    private ArrayList<DiscreteDataTypeSetSpec> theDiscreteDataTypeSetSpecs;
    private ArrayList<ConcreteDataTypeSpec> theConcreteDataTypeSpecs;
    private ArrayList<ContinuousDataTypeSpec> theContinuousDataTypeSpecs;

    public TempFullDataTypeSpec() {
        initCollections();
    }

    private void initCollections(){
        this.theDataTypeSpecs = new ArrayList<DataTypeSpec>();
        this.theDiscreteDataTypeSpecs = new ArrayList<DiscreteDataTypeSpec>();
        this.theDiscreteDataTypeSetSpecs = new ArrayList<DiscreteDataTypeSetSpec>();
        this.theConcreteDataTypeSpecs = new ArrayList<ConcreteDataTypeSpec>();
        this.theContinuousDataTypeSpecs = new ArrayList<ContinuousDataTypeSpec>();
    }

    public void addSpec(DataTypeSpec aDataTypeSpec){
        this.theDataTypeSpecs.add(aDataTypeSpec);
    }

    /**
     * Given a FullDataTypeSpec which contains DataTypeSpecs for a single GeneticDataTypes, simplify them as much as possible, 
     * putting the result into theResultDataTypeSpec.
     * <p> 
     * TODO: check that the FullDataTypeSpec which contains DataTypeSpecs for a single GeneticDataTypes
     * <p> 
     * II. simplify by eliminating redundant constraints; not an error
     * 1) if contains a ConcreteDataTypeSpec, discard all other narrowing DataTypeSpecs; should we issue a warning?
     * 2) otherwise, 
     * A) For discrete data types:
     * a) convert all DiscreteDataTypeSpecs into creating DiscreteDataTypeSetSpecs
     * b) combine multiple DiscreteDataTypeSetSpecs into a single DiscreteDataTypeSetSpec that accepts anything the DiscreteDataTypeSetSpecs 
     *      would have accepted; not an error
     * Done.
     * <p> 
     * B) For continuous data types:
     * a) combine together redundant ContinuousDataTypeSpecs
     * Done.
     * <p> 
     * TODO: eliminate DiscreteDataTypeSpecs entirely, creating DiscreteDataTypeSetSpecs instead
     */
    void cleanUpDataType( ResultDataTypeSpec theResultDataTypeSpec, GeneticDataTypes theGeneticDataType ){
       
        // organize by class
        for( DataTypeSpec aDataTypeSpec : this.theDataTypeSpecs){

            if( aDataTypeSpec instanceof ConcreteDataTypeSpec) {
                this.theConcreteDataTypeSpecs.add( (ConcreteDataTypeSpec)aDataTypeSpec );
                
            } else if( aDataTypeSpec instanceof ContinuousDataTypeSpec){
                this.theContinuousDataTypeSpecs.add( (ContinuousDataTypeSpec)aDataTypeSpec );
                
            } else if( aDataTypeSpec instanceof DiscreteDataTypeSetSpec) {
                this.theDiscreteDataTypeSetSpecs.add( (DiscreteDataTypeSetSpec)aDataTypeSpec );

            } else if( aDataTypeSpec instanceof DiscreteDataTypeSpec) {
                this.theDiscreteDataTypeSpecs.add( (DiscreteDataTypeSpec)aDataTypeSpec );

            } else {
               // TODO: throw exception
                System.err.println("unexpected type");
            }
        }

        //      * 1) if contains a ConcreteDataTypeSpec, discard all other narrowing DataTypeSpecs; should we issue a warning?
        if( 0 < this.theConcreteDataTypeSpecs.size() ){
            this.theContinuousDataTypeSpecs.clear();
            this.theDiscreteDataTypeSetSpecs.clear();
            this.theDiscreteDataTypeSpecs.clear();
            theResultDataTypeSpec.setAcceptAll(true);
            return;
        }
        
        /*
         * A) For discrete data types:
         * a) convert all DiscreteDataTypeSpecs into DiscreteDataTypeSetSpecs
         */
        for( DiscreteDataTypeSpec aDiscreteDataTypeSpec : this.theDiscreteDataTypeSpecs ){
            this.theDiscreteDataTypeSetSpecs.add( aDiscreteDataTypeSpec.convertToDiscreteDataTypeSetSpec());
        }
        this.theDiscreteDataTypeSpecs.clear();

        /* 
         * b) combine multiple DiscreteDataTypeSetSpecs into a single DiscreteDataTypeSetSpec that accepts anything the DiscreteDataTypeSetSpecs 
         *      would have accepted; not a language error
         */
        DiscreteDataTypeSetSpec combinedDiscreteDataTypeSetSpec = new DiscreteDataTypeSetSpec( theGeneticDataType );
        for( DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec : this.theDiscreteDataTypeSetSpecs){
            combinedDiscreteDataTypeSetSpec.combine( aDiscreteDataTypeSetSpec );
        }
        this.theDiscreteDataTypeSetSpecs.clear();
        theResultDataTypeSpec.setTheDiscreteDataTypeSetSpec(combinedDiscreteDataTypeSetSpec);

        /* 
         * B) For continuous data types:
         * a) combine together redundant ContinuousDataTypeSpecs
         */
        ContinuousDataTypeSpec combinedGreaterContinuousDataTypeSpec = null;
        ContinuousDataTypeSpec combinedLesserContinuousDataTypeSpec = null;
        for( ContinuousDataTypeSpec aContinuousDataTypeSpec : this.theContinuousDataTypeSpecs){
            switch( aContinuousDataTypeSpec.getComparisonOp().getTheComparisonOpDirection()){
            case Smaller:
                if( null == combinedLesserContinuousDataTypeSpec ){
                    combinedLesserContinuousDataTypeSpec = aContinuousDataTypeSpec;
                }else{
                    combinedLesserContinuousDataTypeSpec.combine( aContinuousDataTypeSpec );
                }
                break;

            case Bigger:
                if( null == combinedGreaterContinuousDataTypeSpec ){
                    combinedGreaterContinuousDataTypeSpec = aContinuousDataTypeSpec;
                }else{
                    combinedGreaterContinuousDataTypeSpec.combine( aContinuousDataTypeSpec );
                }
                break;
                
            }
        }
        theResultDataTypeSpec.setCombinedLesserContinuousDataTypeSpec(combinedLesserContinuousDataTypeSpec);
        theResultDataTypeSpec.setCombinedGreaterContinuousDataTypeSpec(combinedGreaterContinuousDataTypeSpec);
    }

   public ArrayList<DataTypeSpec> getTheDataTypeSpecs() {
      return theDataTypeSpecs;
   }

}
