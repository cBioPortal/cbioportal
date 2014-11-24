package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.StructuralVariant;
import org.mskcc.cbio.importer.foundation.support.FoundationCommonNames;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/24/14.
 */
public class DmpFusionModel extends FusionModel {

    private final static Logger logger = Logger.getLogger(DmpFusionModel.class);
    private final StructuralVariant structuralVariant;
    private final Boolean primaryGene;

    /*
    the primary attribute determines which of the two genes in a translocation
    is being processed (e.g. in BCR-ABL, BCR is primary, ABL is secondary)
     */

    public DmpFusionModel(StructuralVariant sv, boolean primary){
        Preconditions.checkArgument(null !=sv ,
                "A SMP StructuralVariant object is required");
        this.structuralVariant = sv;
        this.primaryGene = primary;

    }
    @Override
    public String getGene() {
        return (primaryGene) ? this.structuralVariant.getSite1Gene():
                this.structuralVariant.getSite2Gene();
    }

    @Override
    public String getEntrezGeneId() {
        try {
            return (Strings.isNullOrEmpty(geneMapper.symbolToEntrezID(this.getGene())))
                    ? "" : geneMapper.symbolToEntrezID(this.getGene());
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getCenter() {
        return "MSKCC_DMP";
    }

    @Override
    public String getTumorSampleBarcode() {
        return this.structuralVariant.getDmpSampleId();
    }
    
    @Override
    public String getFusion() {
        if( this.structuralVariant.getSite1Gene().equals(this.structuralVariant.getSite2Gene()) ){
            return this.getGene() +"-intragenic";
        }
        if(this.primaryGene){
            return this.structuralVariant.getSite1Gene() +"-"
                    +this.structuralVariant.getSite2Gene()
                    +" fusion";
        }
        return this.structuralVariant.getSite2Gene() +"-"
                +this.structuralVariant.getSite1Gene()
                +" fusion";
    }

    @Override
    public String getDNASupport() {
         return FoundationCommonNames.DEFAULT_DNA_SUPPORT;
    }

    @Override
    public String getRNASupport() {
        return FoundationCommonNames.DEFAULT_RNA_SUPPORT;
    }

    @Override
    public String getMethod() {
        return FoundationCommonNames.DEFAULT_FUSION_METHOD;
    }

    @Override
    public String getFrame() {
       if (!this.structuralVariant.getEventInfo().contains(" frame ")) {
           return "unknown";
       }
        if (this.structuralVariant.getEventInfo().contains(" out of ")){
            return "out of frame";
        }
        return "in frame";
    }
}
