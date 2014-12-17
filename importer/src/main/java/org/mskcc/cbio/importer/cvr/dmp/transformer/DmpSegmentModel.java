package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.google.common.base.Preconditions;
import org.mskcc.cbio.importer.cvr.dmp.model.SegmentDatum;
import org.mskcc.cbio.importer.persistence.staging.segment.SegmentModel;

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
 * Created by fcriscuo on 11/15/14.
 */
public class DmpSegmentModel extends SegmentModel {
    private final SegmentDatum segmentDatum;

    public DmpSegmentModel(SegmentDatum sd){
        Preconditions.checkArgument(sd!= null, "A SegmentData instance is required");
        this.segmentDatum = sd;
    }

    @Override
    public String getID() {
        return this.segmentDatum.getID();
    }

    @Override
    public String getChromosome() {
        return this.segmentDatum.getChrom();
    }

    @Override
    public String getLocStart() {
        return this.segmentDatum.getLocStart();
    }

    @Override
    public String getLocEnd() {
        return this.segmentDatum.getLocEnd();
    }

    @Override
    public String getNumMark() {
        return this.segmentDatum.getNumMark();
    }

    @Override
    public String getSegMean() {
        return this.segmentDatum.getSegMean().toString();
    }
}
