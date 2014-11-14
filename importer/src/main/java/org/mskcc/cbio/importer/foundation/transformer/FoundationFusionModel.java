package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.RearrangementType;
import org.mskcc.cbio.importer.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.foundation.support.FoundationCommonNames;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;

import javax.annotation.Nullable;

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
 * Created by fcriscuo on 11/10/14.
 */
public class FoundationFusionModel extends FusionModel {
    private static final Logger logger = Logger.getLogger(FoundationFusionModel.class);
    private final RearrangementType rearrangementType;
    private final String tumorSampleBarcode;

    public FoundationFusionModel(RearrangementType rType, String sampleId) {
        Preconditions.checkArgument(null!= rType,"A RearrangementType object is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleId),"A sample ID is required");
        this.rearrangementType = rType;
        this.tumorSampleBarcode = sampleId;
    }

    @Override
    public String getGene() {
        return rearrangementType.getTargetedGene();
    }

    @Override
    public String getEntrezGeneId() {
        try {
            return (Strings.isNullOrEmpty(geneMapper.symbolToEntrezID(rearrangementType.getTargetedGene())))
                    ? "" : geneMapper.symbolToEntrezID(rearrangementType.getTargetedGene());
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getCenter() {
        return  DMPCommonNames.CENTER_MSKCC;
    }

    @Override
    public String getTumorSampleBarcode() {
        return this.tumorSampleBarcode;
    }

    @Override
    public String getFusion() {
        return rearrangementType.getTargetedGene() +"-" +rearrangementType.getOtherGene() +" fusion";
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
        return this.functionFrameFunction.apply(this.rearrangementType);
    }

    private Function<RearrangementType,String> functionFrameFunction = new Function<RearrangementType, String>() {
        @Nullable
        @Override
        public String apply(RearrangementType rt) {
            if (Strings.isNullOrEmpty(rt.getInFrame()) || rt.getInFrame().equals(FoundationCommonNames.UNKNOWN)) {
                return FoundationCommonNames.UNKNOWN;
            }
            return (rt.getInFrame().equals("No")) ? FoundationCommonNames.OUT_OF_FRAME : FoundationCommonNames.IN_FRAME;
        }
    };


}
