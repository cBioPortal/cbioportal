package org.mskcc.portal.oncoPrintSpecLanguage;

import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Holds a raw full data type spec, as produced by the parser production
 * fullDataTypeSpec. creates the permanent 
 * 
 * @author Arthur Goldberg
 * 
 */
public class ParsedFullDataTypeSpec {

    private ArrayList<DataTypeSpec> theDataTypeSpecs; // the parser loads all
                                                      // dataTypeSpecs in here

    public ParsedFullDataTypeSpec() {
        this.theDataTypeSpecs = new ArrayList<DataTypeSpec>();
    }

    public void addSpec(DataTypeSpec aDataTypeSpec) {
       if( aDataTypeSpec != null ){
          this.theDataTypeSpecs.add(aDataTypeSpec);
       }
    }

    /**
     * clean up from parse.
     * 
     * I. Organize: collect DataTypeSpecs for each data type; then simplify
     */
    public OncoPrintGeneDisplaySpec cleanUpInput() {

        EnumMap<GeneticDataTypes, TempFullDataTypeSpec> organizedDataTypeSpecs = 
            new EnumMap<GeneticDataTypes, TempFullDataTypeSpec>(
                GeneticDataTypes.class);

        for (GeneticDataTypes aGeneticDataType : GeneticDataTypes.values()) {
            organizedDataTypeSpecs.put(aGeneticDataType, new TempFullDataTypeSpec());
        }
        
        // copy all the DataTypeSpecs into the TempFullDataTypeSpec, preparing for clean up
        for (DataTypeSpec aDataTypeSpec : this.theDataTypeSpecs) {
            organizedDataTypeSpecs.get(aDataTypeSpec.theGeneticDataType).addSpec(aDataTypeSpec);
        }

        OncoPrintGeneDisplaySpec theResultFullDataTypeSpec = new OncoPrintGeneDisplaySpec();

        for (GeneticDataTypes aGeneticDataType : GeneticDataTypes.values()) {

           if( 0 < organizedDataTypeSpecs.get(aGeneticDataType).getTheDataTypeSpecs().size() ){
              organizedDataTypeSpecs.get(aGeneticDataType).cleanUpDataType(
                    theResultFullDataTypeSpec.createResultDataTypeSpec(aGeneticDataType),
                    aGeneticDataType);
           }
           
        }
        return theResultFullDataTypeSpec;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(); 
        for( DataTypeSpec aDataTypeSpec: this.theDataTypeSpecs){
           sb.append(aDataTypeSpec.toString()).append("\n");
        }
        return sb.toString(); 
    }

}