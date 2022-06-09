package org.cbioportal.persistence.mybatis.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonPropertyOrder({"Cancer_Study_Identifier", "Genetic_Profile_Identifier", "Hugo_Symbol", "Entrez_Gene_Identifier", "Sample_Identifier", "Value"})
public class TsvTestClass implements Serializable {
    @JsonProperty("Cancer_Study_Identifier")
    private String Cancer_Study_Identifier;
    @JsonProperty("Genetic_Profile_Identifier")
    private String Genetic_Profile_Identifier;
    @JsonProperty("Hugo_Symbol")
    private String Hugo_Symbol;
    @JsonProperty("Entrez_Gene_Identifier")
    private Integer Entrez_Gene_Identifier;
    @JsonProperty("Sample_Identifier")
    private String Sample_Identifier;
    @JsonProperty("Value")
    private String	Value;

    public TsvTestClass() {}

}
