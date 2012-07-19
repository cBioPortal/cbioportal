package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.util.EnumSet;
import java.util.Iterator;
import org.mskcc.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;
import org.mskcc.portal.util.EqualsUtil;
import org.mskcc.portal.util.HashCodeUtil;

/**
 * Represent a discrete dataTypeSpec, like CNA or mutation.
 * The specified values are given by an EnumSet, so any subset can be specified.
 * 
 * TODO: replace DiscreteDataTypeSpec with this
 * @author Arthur Goldberg
 */
public class DiscreteDataTypeSetSpec extends DataTypeSpec{

    private EnumSet<GeneticTypeLevel> specifiedValues;
    
    /**
     * create a DiscreteDataTypeSetSpec from a datatype name and a data type level
     * @param theGeneticDataType
     * @param validValues1
     */
    public DiscreteDataTypeSetSpec(GeneticDataTypes theGeneticDataType,
            GeneticTypeLevel validValues1) throws IllegalArgumentException{

        // verify that validValues1 is a level for theGeneticDataType
        if( validValues1.getTheGeneticDataType().equals(theGeneticDataType)){
            this.theGeneticDataType = theGeneticDataType;
            this.specifiedValues = EnumSet.of(validValues1);
        }else{
            throw new IllegalArgumentException( validValues1 + " is not a level for " + theGeneticDataType );
        }
    }
    
    /**
     * create a DiscreteDataTypeSetSpec from a datatype name and a numerical level code.
     * 
     * @param theGeneticDataTypeName
     * @param levelCode
     */
    public DiscreteDataTypeSetSpec(String theGeneticDataTypeName,
            int levelCode) {
       //System.out.format( "DiscreteDataTypeSetSpec: constructed with '%s' and '%d'%n", theGeneticDataTypeName, levelCode );
       
        this.theGeneticDataType = DiscreteDataTypeSpec.findDataType( theGeneticDataTypeName );
        // convertCode throws an exception if levelCode is not a level for theGeneticDataTypeName
        this.specifiedValues = EnumSet.of( GeneticTypeLevel.convertCode( this.theGeneticDataType, levelCode ));
    }
    
    public static DiscreteDataTypeSetSpec discreteDataTypeSetSpecGenerator(String theGeneticDataTypeNameString,
          String levelCodeString ){
       
       try {
         return new DiscreteDataTypeSetSpec( theGeneticDataTypeNameString, Integer.parseInt(levelCodeString) );
      } catch (NumberFormatException e) {
         return null;
      } catch (IllegalArgumentException e) {
         return null;
      }
       
    }

    /**
     * create a DiscreteDataTypeSetSpec from the name of a (hopefully) discrete level.
     * 
     * @param levelName
     * @throws IllegalArgumentException
     */
    public DiscreteDataTypeSetSpec(String levelName) throws IllegalArgumentException{
        GeneticTypeLevel theGeneticTypeLevel = GeneticTypeLevel.findDataTypeLevel(levelName);
        //out.format( "DiscreteDataTypeSetSpec: called with '%s'%n", levelName );

        // verify that levelName maps to a level within a discrete datatype
        if( theGeneticTypeLevel.getTheGeneticDataType().getTheDataTypeCategory()
            .equals(DataTypeCategory.Discrete) ){
            this.specifiedValues = EnumSet.of( theGeneticTypeLevel );
            this.theGeneticDataType = theGeneticTypeLevel.getTheGeneticDataType();
            //System.out.format( "DiscreteDataTypeSetSpec: constructed with '%s'%n", levelName );
        }else{
            throw new IllegalArgumentException( levelName + " is not a level within a discrete datatype" );
        }
    }

    public static DiscreteDataTypeSetSpec discreteDataTypeSetSpecGenerator(String levelName){
       
       try {
         return new DiscreteDataTypeSetSpec( levelName );
      } catch (IllegalArgumentException e) {
         return null;
      }
    }
    
    /**
     * create a DiscreteDataTypeSetSpec from a datatype name and a set of levels.
     * 
     * @param theGeneticDataType
     * @param validValues
     */
    public DiscreteDataTypeSetSpec(GeneticDataTypes theGeneticDataType,
            GeneticTypeLevel... validValues) {
        this.theGeneticDataType = theGeneticDataType;
        this.specifiedValues = EnumSet.noneOf(GeneticTypeLevel.class);
        // TODO: perhaps make it an error to provide no validValues 
        for( GeneticTypeLevel c : validValues){
            this.specifiedValues.add(c);
        }
    }
    
    /**
     * create a DiscreteDataTypeSetSpec that accepts no levels
     * @param theGeneticDataType
     */
    public DiscreteDataTypeSetSpec(GeneticDataTypes theGeneticDataType) {
        this.theGeneticDataType = theGeneticDataType;
        this.specifiedValues = EnumSet.noneOf(GeneticTypeLevel.class);
    }

    public static GeneticDataTypes findDataType( String name )
    throws IllegalArgumentException{
        return DataTypeSpec.genericFindDataType( name, DataTypeCategory.Discrete );
    }

    /**
     * Alternative style for a copy constructor, using a static newInstance
     * method.
     */
     public static DiscreteDataTypeSetSpec newInstance( DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec ) {
        if( null == aDiscreteDataTypeSetSpec ){
           return null;
        }
        DiscreteDataTypeSetSpec theNewDiscreteDataTypeSetSpec = 
           new DiscreteDataTypeSetSpec( aDiscreteDataTypeSetSpec.getTheGeneticDataType() );
        theNewDiscreteDataTypeSetSpec.combine(aDiscreteDataTypeSetSpec);
        return theNewDiscreteDataTypeSetSpec;
     }
     
    /**
     * indicate whether value satisfies this DiscreteDataTypeSetSpec
     * @param value
     * @return true if value satisfies this DiscreteDataTypeSetSpec
     */
    public boolean satisfy( GeneticTypeLevel value) {
        return this.specifiedValues.contains(value);
    }
    
    /**
     * add the level aGeneticTypeLevel to this instance's accepted levels;
     * if aGeneticTypeLevel is already accepted no change occurs.
     * @param aGeneticTypeLevel
     */
    public void addLevel( GeneticTypeLevel aGeneticTypeLevel ){
        this.specifiedValues.add(aGeneticTypeLevel);
    }

    /**
     * combine the aDiscreteDataTypeSetSpec's accepted levels into this class's levels
     * @param aDiscreteDataTypeSetSpec
     */
    public void combine(DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec) {
        this.specifiedValues.addAll(aDiscreteDataTypeSetSpec.getSpecifiedValues());
    }

    @Override
    public String toString() {
        //return theGeneticDataType.toString() + " in " + specifiedValues.toString();
        StringBuffer sb = new StringBuffer();
        Iterator<GeneticTypeLevel> anIterator = specifiedValues.iterator();
        while( anIterator.hasNext() ){
            sb.append( anIterator.next().toString() ).append(" ");
        }
        return sb.toString();
    }

    @Override
    public boolean equals( Object aThat) {
        if( this == aThat ) return true;
        if ( !(aThat instanceof DiscreteDataTypeSetSpec) ) return false;
        DiscreteDataTypeSetSpec that = (DiscreteDataTypeSetSpec) aThat;
        return
            EqualsUtil.areEqual(this.theGeneticDataType, that.theGeneticDataType) &&
            EqualsUtil.areEqual(this.specifiedValues, that.specifiedValues);
    }

    // TODO: TEST
    @Override
    public int hashCode( ) {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, theGeneticDataType );
        result = HashCodeUtil.hash( result, specifiedValues );
        return result;
    }

    public EnumSet<GeneticTypeLevel> getSpecifiedValues() {
        return specifiedValues;
    }

    @Override public final Object clone() throws CloneNotSupportedException {
       throw new CloneNotSupportedException();
     }   
    
}