package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;
import org.mskcc.cbio.portal.util.EqualsUtil;
import org.mskcc.cbio.portal.util.HashCodeUtil;

/**
 * Represent a discrete dataTypeSpec, like CNA or mutation.
 * The specified values are given by an EnumSet, so any subset can be specified.
 * 
 * TODO: replace DiscreteDataTypeSpec with this
 * @author Arthur Goldberg
 */
public class DiscreteDataTypeSetSpec extends DataTypeSpec{

    private final Set<GeneticTypeLevel> specifiedValues;
    private final Set<String> mutationPatterns;
    
    public DiscreteDataTypeSetSpec(GeneticDataTypes theGeneticDataType){
        this.theGeneticDataType = theGeneticDataType;
        specifiedValues = EnumSet.noneOf(GeneticTypeLevel.class);
        mutationPatterns = new HashSet<String>();
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
            mutationPatterns = new HashSet<String>();
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
        mutationPatterns = new HashSet<String>();
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
        mutationPatterns = new HashSet<String>();
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
        if (levelName==null) {
            return null;
        }
        
        GeneticTypeLevel theGeneticTypeLevel = GeneticTypeLevel.findDataTypeLevel(levelName);

        // verify that levelName maps to a level within a discrete datatype
        if( theGeneticTypeLevel!=null 
                && theGeneticTypeLevel.getTheGeneticDataType().getTheDataTypeCategory()
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
        ret.addLevel(specificMutation);
        return ret;
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
            return this.specifiedValues.contains((GeneticTypeLevel)value);
        } else if (theGeneticDataType == GeneticDataTypes.Mutation) {
            if (value instanceof String) {
                return satisfySpecificMutation((String)value);
            }
        }
        return false;
    }
    
    private static Pattern SPECIFIC_POSITION_MUTATION_PATTERN = Pattern.compile("[^0-9]*([0-9]+)");
    private static Pattern SPECIFIC_RANGE_DELETION_PATTERN = Pattern.compile("[^0-9]*([0-9]+)[^0-9]+([0-9]+)[^0-9]*");
    private boolean satisfySpecificMutation( String specificMutations ) {
        String specificMutationsUpper = specificMutations.toUpperCase(); 
        for (String specificMutationUpper : specificMutationsUpper.split(",")) {
            if (mutationPatterns.contains(specificMutationUpper)) {
                // complete match, including specific mutation to an amino acid such V600E, D200fs
                return true;
            }

            for ( String mutationPattern : mutationPatterns ) {
                Matcher spm = SPECIFIC_POSITION_MUTATION_PATTERN.matcher(mutationPattern);
                if (spm.matches()) {// all mutations for a specific amino acid
                    String strPos = spm.group(1);
                    if (specificMutationUpper.matches(".*[^0-9]+"+strPos+"[^0-9\\+\\-]*")
                            || specificMutationUpper.matches(strPos+"[^0-9]+.*")) {
                        // To deal with cases that the query position appears in the mutation string
                        // e.g., S634*, S479_splice, *691S, 278in_frame_del, 1002_1003_del,
                        //       YQQQQQ263in_frame_del*, A1060fs, A277_splice
                        // Note: "S30" will not match "S300"
                        // Note: "S30" will not match "e30+1"
                        return true;
                    }
                    
                    Matcher delm = SPECIFIC_RANGE_DELETION_PATTERN.matcher(specificMutationUpper);
                    if (delm.matches()) {
                        // To deal with deletions
                        // e.g. query 1020 matches 1018_1023DNPPVL>V
                        int start = Integer.parseInt(delm.group(1));
                        int end = Integer.parseInt(delm.group(2));
                        int pos = Integer.parseInt(strPos);
                        if (start<pos && pos<end) {
                            return true;
                        }
                    }
                } 
                
                // The follow types are matched according the the mutaiton string,
                // which may not be correct. A more accurate solution would be using
                // the mutation_type from database directly
                else if (mutationPattern.equals("MS") || mutationPattern.equals("MISSENSE")) {
                    if (specificMutationUpper.matches("[A-Z][0-9]+[A-Z]")) {
                        return true;
                    }
                } else if (mutationPattern.equals("NS") || mutationPattern.equals("NONSENSE")) {
                    if (specificMutationUpper.matches("[A-Z][0-9]+\\*")) {
                        return true;
                    }
                } else if (mutationPattern.equals("NONSTART")) {
                    if (specificMutationUpper.matches("M1[^0-9]+")) {
                        return true;
                    }
                } else if (mutationPattern.equals("NONSTOP")) {
                    if (specificMutationUpper.matches(".*\\*[0-9]+.+")) {
                        return true;
                    }
                } else if (mutationPattern.equals("FS") || mutationPattern.equals("FRAMESHIFT")) {
                    if (specificMutationUpper.matches("[A-Z\\*][0-9]+FS")) {
                        return true;
                    }
                } else if (mutationPattern.equals("IF") || mutationPattern.equals("INFRAME")) {
                    if (specificMutationUpper.matches(".+IN_FRAME_((INS)|(DEL)).*")) {
                        return true;
                    }
                } else if (mutationPattern.equals("SP") || mutationPattern.equals("SPLICE")) {
                    if (specificMutationUpper.matches("[A-Z][0-9]_SPLICE") ||
                            specificMutationUpper.matches("E[0-9]+[\\+\\-][0-9]+")) {
                        return true;
                    }
                } else if (mutationPattern.equals("TRUNC")) {
                    //NONSENSE
                    if (specificMutationUpper.matches("[A-Z][0-9]+\\*")) {
                        return true;
                    }
                    //NONSTART
                    if (specificMutationUpper.matches("M1[^0-9]+")) {
                        return true;
                    }
                    //NONSTOP
                    if (specificMutationUpper.matches(".*\\*[0-9]+.+")) {
                        return true;
                    }
                    //FRAMESHIFT
                    if (specificMutationUpper.matches("[A-Z\\*][0-9]+FS")) {
                        return true;
                    }
                    //SPLICE
                    if (specificMutationUpper.matches("[A-Z][0-9]_SPLICE") ||
                            specificMutationUpper.matches("E[0-9]+[\\+\\-][0-9]+")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * add the level aGeneticTypeLevel to this instance's accepted levels;
     * if aGeneticTypeLevel is already accepted no change occurs.
     * @param aGeneticTypeLevel
     */
    public void addLevel( Object value ){
        if (value instanceof GeneticTypeLevel) {
            specifiedValues.add((GeneticTypeLevel) value);
        } else if (theGeneticDataType == GeneticDataTypes.Mutation && value instanceof String) {
            mutationPatterns.add(((String)value).toUpperCase());
        } else {
            throw new java.lang.IllegalArgumentException("Wrong level");
        }
    }

    /**
     * combine the aDiscreteDataTypeSetSpec's accepted levels into this class's levels
     * @param aDiscreteDataTypeSetSpec
     */
    public void combine(DiscreteDataTypeSetSpec aDiscreteDataTypeSetSpec) {
        this.specifiedValues.addAll(aDiscreteDataTypeSetSpec.getSpecifiedValues());
        this.mutationPatterns.addAll(aDiscreteDataTypeSetSpec.getMutationPatterns());
    }

    @Override
    public String toString() {
        //return theGeneticDataType.toString() + " in " + specifiedValues.toString();
        StringBuilder sb = new StringBuilder();
        for (GeneticTypeLevel value : specifiedValues){
            sb.append( value.toString() ).append(" ");
        }
        for (String str : mutationPatterns) {
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
            EqualsUtil.areEqual(this.mutationPatterns, that.mutationPatterns);
    }

    // TODO: TEST
    @Override
    public int hashCode( ) {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash( result, theGeneticDataType );
        result = HashCodeUtil.hash( result, specifiedValues );
        result = HashCodeUtil.hash( result, mutationPatterns);
        return result;
    }

    public Set<GeneticTypeLevel> getSpecifiedValues() {
        return specifiedValues;
    }
    
    public Set<String> getMutationPatterns() {
        return mutationPatterns;
    }

    @Override public final Object clone() throws CloneNotSupportedException {
       throw new CloneNotSupportedException();
     }   
    
}
