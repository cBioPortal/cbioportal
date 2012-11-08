package org.mskcc.mondrian.client;

/**
 * MSKCC Genetic Profiles
 * 
 * @author Dazhi Jiao
  */
public class GeneticProfile {
    private String id;
    private String name;
    private GENETIC_PROFILE_TYPE type;
    private String description;

    public GeneticProfile(String id, String name, String description, String type) {
        this(id, name, description, inferType(type));
    }

    public GeneticProfile(String id, String name, String description, GENETIC_PROFILE_TYPE type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;

    }

    private static GENETIC_PROFILE_TYPE inferType(String type) {
        try {
            return GENETIC_PROFILE_TYPE.valueOf(type);
        } catch (Exception e) {
            return GENETIC_PROFILE_TYPE.NOT_KNOWN;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GENETIC_PROFILE_TYPE getType() {
        return type;
    }

    public void setType(GENETIC_PROFILE_TYPE type) {
        this.type = type;
    }

    public static enum GENETIC_PROFILE_TYPE {
        NOT_KNOWN("NOT_KNOWN"),
        COPY_NUMBER_ALTERATION("COPY_NUMBER_ALTERATION"),
        MRNA_EXPRESSION("MRNA_EXPRESSION"),
        METHYLATION("METHYLATION"),
        METHYLATION_BINARY("METHYLATION_BINARY"),
        MUTATION_EXTENDED("MUTATION_EXTENDED"),
        PROTEIN_ARRAY_PROTEIN_LEVEL("PROTEIN_ARRAY_PROTEIN_LEVEL"),
        PROTEIN_ARRAY_PHOSPHORYLATION("PROTEIN_ARRAY_PHOSPHORYLATION");
        
        private String name;
        GENETIC_PROFILE_TYPE(String name) { this.name = name; }
        public String toString() { return name; }
    }
    
    public String toString() {
    	return name;
    }
}