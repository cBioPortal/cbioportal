package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class MutationalSignatureMeta implements Serializable {
    
    @NotNull
    private String mutationalSignatureId; //e.g. mutational_signature_1
    @NotNull
    private Integer geneticEntityId; //genetic id/foreign key
    private String description; //e.g. "weight of mutational signature 1 in sample's signature"
    
    public MutationalSignatureMeta(){
        
    }
    
    public MutationalSignatureMeta(String mutationalSignatureId, String description){
        this.mutationalSignatureId = mutationalSignatureId;
        this.description = description;
    }
    
    public String getMutationalSignatureId(){
        return mutationalSignatureId;
    }

    public void setMutationalSignatureId(String mutationalSignatureId){
        this.mutationalSignatureId = mutationalSignatureId;
    }

    public Integer getGeneticEntityId(){
        return geneticEntityId;
    }

    public void setGeneticEntityId(Integer geneticEntityId){
        this.geneticEntityId = geneticEntityId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}