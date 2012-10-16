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