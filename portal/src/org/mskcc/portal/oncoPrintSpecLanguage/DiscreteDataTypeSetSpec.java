package org.mskcc.portal.oncoPrintSpecLanguage;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
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

    private Set<GeneticTypeLevel> specifiedValues;
    private Set<String> specifiedMutations;
    
    public DiscreteDataTypeSetSpec(GeneticDataTypes theGeneticDataType){
        this.theGeneticDataType = theGeneticDataType;
        specifiedValues = EnumSet.noneOf(GeneticTypeLevel.class);
        specifiedMutations = new HashSet<String>();
    }
    
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
            specifiedMutations = new HashSet<String>();
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
       
        theGeneticDataType = DiscreteDataTypeSpec.findDataType( theGeneticDataTypeName );
        // convertCode throws an exception if levelCode is not a level for theGeneticDataTypeName
        specifiedValues = EnumSet.of( GeneticTypeLevel.convertCode( this.theGeneticDataType, levelCode ));
        specifiedMutations = new HashSet<String>();
    }
    
    /**
     * 
     * @param theGeneticDataTypeNameString
     * @param levelCodeString
     * @return 
     */
    public static DiscreteDataTypeSetSpec discreteDataTypeSetSpecGeneratorByLevelCode(String theGeneticDataTypeNameString,
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
     * 
     * @param levelName
     * @return 
     */
    public static DiscreteDataTypeSetSpec discreteDataTypeSetSpecGeneratorByLevelName(String levelName){
       GeneticTypeLevel theGeneticTypeLevel = GeneticTypeLevel.findDataTypeLevel(levelName);

        // verify that levelName maps to a level within a discrete datatype
        if( theGeneticTypeLevel.getTheGeneticDataType().getTheDataTypeCategory()
            .equals(DataTypeCategory.Discrete) ){
            return new DiscreteDataTypeSetSpec(theGeneticTypeLevel.getTheGeneticDataType(),
                    theGeneticTypeLevel);
        }else{
            return null;
        }
    }
    
    /**
     * 
     * @param specificMutation
     * @return 
     */
    public static DiscreteDataTypeSetSpec specificMutationDataTypeSetSpecGenerator(String specificMutation){
        DiscreteDataTypeSetSpec ret = new DiscreteDataTypeSetSpec(GeneticDataTypes.Mutation);
        ret.addSpecificMutation(specificMutation);
        return ret;
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
    public boolean satisfy( Object value ) {
        if (value instanceof GeneticTypeLevel) {
            return this.specifiedValues.contains(value);
        } else if (theGeneticDataType == GeneticDataTypes.Mutation) {
            if (value instanceof String) {
                return satisfySpecificMutation((String)value);
            }
        }
        return false;
    }
    
    private boolean satisfySpecificMutation( String specificMutation ) {
        for ( String specifiedMutation : specifiedMutations ) {
            if (specificMutation.startsWith(specifiedMutation)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * add the level aGeneticTypeLevel to this instance's accepted levels;
     * if aGeneticTypeLevel is already accepted no change occurs.
     * @param aGeneticTypeLevel
     */
    public void addLevel( GeneticTypeLevel value ){
        this.specifiedValues.add(value);
    }
    
    public void addSpecificMutation(String specificMutation) {
        this.specifiedMutations.add(specificMutation);
    }

    /**
     * combine the aDiscreteDataTypeSetSpec's accepted levels into this class's levels
     * @param aDiscreteDataTypeSetSpec
     */
    public void combine(DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec) {
        this.specifiedValues.addAll(aDiscreteDataTypeSetSpec.getSpecifiedValues());
        this.specifiedMutations.addAll(aDiscreteDataTypeSetSpec.getSpecifiedMutations());
    }

    @Override
    public String toString() {
        //return theGeneticDataType.toString() + " in " + specifiedValues.toString();
        StringBuilder sb = new StringBuilder();
        for (GeneticTypeLevel value : specifiedValues){
            sb.append( value.toString() ).append(" ");
        }
        for (String str : specifiedMutations) {
            sb.append( str ).append( " " );
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
            EqualsUtil.areEqual(this.specifiedValues, that.specifiedValues) &&
            EqualsUtil.areEqual(this.specifiedMutations, that.specifiedMutations);
    }

    // TODO: TEST
    @Override
    public int hashCode( ) {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, theGeneticDataType );
        result = HashCodeUtil.hash( result, specifiedValues );
        result = HashCodeUtil.hash( result, specifiedMutations);
        return result;
    }

    public Set<GeneticTypeLevel> getSpecifiedValues() {
        return specifiedValues;
    }
    
    public Set<String> getSpecifiedMutations() {
        return specifiedMutations;
    }

    @Override public final Object clone() throws CloneNotSupportedException {
       throw new CloneNotSupportedException();
     }   
    
}