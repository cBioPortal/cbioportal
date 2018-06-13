package org.cbioportal.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class MutationalSignature implements Serializable {

    @NotNull
    private Integer internalId; //primary key
    @NotNull
    private Integer geneticEntityId; //genetic id/foreign key
    @NotNull
    private String mutationalSignatureId; //e.g. mutationalsignature1
    private String name; //e.g. "Mutational Signature 1"
    private String description; //e.g. "weight of mutational signature 1 in sample's signature"

    public Integer getInternalId(){
        return internalId;
    }

    public void setInternalId(Integer internalId) {
		this.internalId = internalId;
    }
    
    public String getMutationalSignatureId(){
        return mutationalSignatureId;
    }

    public void setMutationalSignatureId(String mutationalSignatureId){
        this.mutationalSignature = mutationalSignatureId;
    }

    public String getGeneticEntityId(){
        return geneticEntityId;
    }

    public void setGeneticEntityId(String geneticEntityId){
        this.geneticEntityId = geneticEntityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}