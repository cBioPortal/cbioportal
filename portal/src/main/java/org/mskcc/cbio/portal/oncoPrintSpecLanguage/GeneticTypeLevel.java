package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

/**
 * the levels within discrete GeneticDataTypes.
 * Maps between GeneticDataTypes and the levels.
 * @author Arthur Goldberg
 */
public enum GeneticTypeLevel{
    // this order must be maintained for DiscreteDataTypeSpec.satisfy 
    HomozygouslyDeleted (-2, GeneticDataTypes.CopyNumberAlteration, "Homdel"),
    HemizygouslyDeleted (-1, GeneticDataTypes.CopyNumberAlteration, "Hetloss"), 
    Diploid             (0, GeneticDataTypes.CopyNumberAlteration,  "" ), 
    Gained              (1, GeneticDataTypes.CopyNumberAlteration,  "" ), 
    Amplified           (2, GeneticDataTypes.CopyNumberAlteration,  "" ),
    Normal              (0, GeneticDataTypes.Mutation,  "" ), // TODO: IMPORTANT: verify that 0 & 1 are the right codes for mutation!
    Mutated             (1, GeneticDataTypes.Mutation,  "" );

    private final int code;
    private final GeneticDataTypes theGeneticDataType;
    private final String[] nicknames;

    private GeneticTypeLevel( int cNAcode, GeneticDataTypes theGeneticDataType, String nicknames ) {
        this.code = cNAcode;
        this.theGeneticDataType = theGeneticDataType;
        if( !nicknames.equals("")){
            this.nicknames = nicknames.split(" ");
        }else{
            this.nicknames = new String[0];
        }
    }

    public String[] getNicknames() {
        return nicknames;
    }

    public GeneticDataTypes getTheGeneticDataType() {
        return theGeneticDataType;
    }

    /**
     * map from a GeneticDataTypes and an integer code to a value for a discrete datatype
     * 
     * @param code
     * @return a GeneticTypeLevel enum for the code
     * @throws IllegalArgumentException when the code isn't a valid code for a value
     */
    public static GeneticTypeLevel convertCode( GeneticDataTypes aGeneticDataType, int code ) 
    throws IllegalArgumentException{
        for( GeneticTypeLevel v: GeneticTypeLevel.values() ){
            if( aGeneticDataType == v.theGeneticDataType && v.code == code ){
                return v;
            }
        }
        throw new IllegalArgumentException( code + " is invalid code for levels in " + aGeneticDataType );
    }
    
    /**
     * map from an integer CNA code to a GeneticTypeLevel for a CNA code.
     * @param code
     * @return a GeneticTypeLevel for a CNA code
     * @throws IllegalArgumentException when the code isn't a valid code for a CNA
     */
    public static GeneticTypeLevel convertCNAcode( int code ) throws IllegalArgumentException{
        return convertCode( GeneticDataTypes.CopyNumberAlteration, code );
    }
    
    /**
     * map from an integer Mutation code to a GeneticTypeLevel for a Mutation code.
     * @param code
     * @return a GeneticTypeLevel for a Mutation code
     * @throws IllegalArgumentException when the code isn't a valid code for a Mutation
     */
    public static GeneticTypeLevel convertMutationcode( int code ) throws IllegalArgumentException{
        return convertCode( GeneticDataTypes.Mutation, code );
    }
    
    /**
     * map from String name for a GeneticTypeLevel to a GeneticTypeLevel. Matches on any unique, case
     * insensitive prefix or nickname. Cool, huh?
     * 
     * @param name
     *            a unique prefix or nickname of a GeneticTypeLevel 
     * @return a GeneticTypeLevel enum for the name, if it is unique, null otherwise
     * @throws IllegalArgumentException
     */
    public static GeneticTypeLevel findDataTypeLevel(String name)
            throws IllegalArgumentException {

        GeneticTypeLevel aGeneticTypeLevel = (GeneticTypeLevel) UniqueEnumPrefix
                .findUniqueEnumMatchingPrefix(GeneticTypeLevel.class, name);
        if (aGeneticTypeLevel == null) {
            aGeneticTypeLevel = (GeneticTypeLevel) UniqueEnumPrefix
                    .findUniqueEnumWithNicknameMatchingPrefix(GeneticTypeLevel.class,
                            name);
        }
        if (aGeneticTypeLevel == null) {
           throw new IllegalArgumentException( "Not a value for a GeneticTypeLevel: " + name );
        }else{
           return aGeneticTypeLevel;
        }
    }   
    
}