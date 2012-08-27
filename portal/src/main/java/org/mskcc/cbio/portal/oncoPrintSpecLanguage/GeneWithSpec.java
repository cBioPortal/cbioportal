package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import org.mskcc.cbio.portal.util.EqualsUtil;
import org.mskcc.cbio.portal.util.HashCodeUtil;

/**
 * a gene with an OncoPrint Spec
 * @author Arthur Goldberg
 */
public class GeneWithSpec {
    String name;
    OncoPrintGeneDisplaySpec theOncoPrintGeneDisplaySpec;
    
    public GeneWithSpec(String name) {
        this.name = name;
    }

    public GeneWithSpec(String name,
            OncoPrintGeneDisplaySpec theResultFullDataTypeSpec) {
        this.name = name;
        this.theOncoPrintGeneDisplaySpec = theResultFullDataTypeSpec;
        // System.out.println( "Creating GeneWithSpec '" + name + "' with spec :" + theResultFullDataTypeSpec.toString());
        // TODO: should clone, or make certain that theResultFullDataTypeSpec isn't changed        this.theResultFullDataTypeSpec = theResultFullDataTypeSpec.clone();
    }
    
    /**
     * generate a GeneWithSpec; use a custom spec for the gene if it's initialized (non null) in theResultFullDataTypeSpec, 
     * otherwise use the default from theDefaultResultFullDataTypeSpec.
     * Used by the parser.
     * @param name
     * @param theResultFullDataTypeSpec
     * @param theDefaultResultFullDataTypeSpec
     * @return the new GeneWithSpec, or null if name is null or both of the ResultFullDataTypeSpecs
     */
    public static GeneWithSpec geneWithSpecGenerator(String name, OncoPrintGeneDisplaySpec theResultFullDataTypeSpec, 
          OncoPrintGeneDisplaySpec theDefaultResultFullDataTypeSpec){
       if( null != name ){
          if( null != theResultFullDataTypeSpec ){
             return new GeneWithSpec( name, theResultFullDataTypeSpec);
          }else{
             if( null != theDefaultResultFullDataTypeSpec ){
                return new GeneWithSpec( name, theDefaultResultFullDataTypeSpec);
             }
          }
       }
       return null;
    }

    public String getName() {
        return name;
    }

    public OncoPrintGeneDisplaySpec getTheOncoPrintGeneDisplaySpec() {
        return theOncoPrintGeneDisplaySpec;
    }

    public void setTheResultFullDataTypeSpec(
            OncoPrintGeneDisplaySpec theResultFullDataTypeSpec) {
        // TODO: should clone, as above
        this.theOncoPrintGeneDisplaySpec = theResultFullDataTypeSpec;
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append( this.name );
        if( null != this.theOncoPrintGeneDisplaySpec ){
           sb.append(": ").append( this.theOncoPrintGeneDisplaySpec.toString() ).append("\n");
        }
        return sb.toString();
    }

    // TODO: TEST
    @Override
    public boolean equals( Object otherGeneWithSpec ) {
        if( this == otherGeneWithSpec ) return true;
        if ( !(otherGeneWithSpec instanceof GeneWithSpec ) ) return false;
        GeneWithSpec that = (GeneWithSpec) otherGeneWithSpec;
        return
           EqualsUtil.areEqual( this.name, that.name) &&
           EqualsUtil.areEqual( this.theOncoPrintGeneDisplaySpec, that.theOncoPrintGeneDisplaySpec );
    }

    // TODO: TEST
    @Override
    public int hashCode( ) {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, this.name );
        result = HashCodeUtil.hash( result, this.theOncoPrintGeneDisplaySpec );
        return result;
    }

}