/*
 * Copyright (c) 2017 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.Set;
import org.mskcc.cbio.portal.model.StructuralVariant;
import org.mskcc.cbio.portal.util.ProgressMonitor;

public class DaoStructuralVariant {

    private DaoStructuralVariant() {}

    /**
     * Adds a new Structural variant record to the database.
     * @param structuralVariant
     * @return number of records successfully added
     * @throws DaoException
     */

    public static void addStructuralVariantToBulkLoader(
        StructuralVariant structuralVariant
    )
        throws DaoException {
        MySQLbulkLoader bl = MySQLbulkLoader.getMySQLbulkLoader(
            "structural_variant"
        );
        String[] fieldNames = new String[] {
            "GENETIC_PROFILE_ID",
            "SAMPLE_ID",
            "SITE1_ENTREZ_GENE_ID",
            "SITE1_ENSEMBL_TRANSCRIPT_ID",
            "SITE1_EXON",
            "SITE1_CHROMOSOME",
            "SITE1_POSITION",
            "SITE1_DESCRIPTION",
            "SITE2_ENTREZ_GENE_ID",
            "SITE2_ENSEMBL_TRANSCRIPT_ID",
            "SITE2_EXON",
            "SITE2_CHROMOSOME",
            "SITE2_POSITION",
            "SITE2_DESCRIPTION",
            "SITE2_EFFECT_ON_FRAME",
            "NCBI_BUILD",
            "DNA_SUPPORT",
            "RNA_SUPPORT",
            "NORMAL_READ_COUNT",
            "TUMOR_READ_COUNT",
            "NORMAL_VARIANT_COUNT",
            "TUMOR_VARIANT_COUNT",
            "NORMAL_PAIRED_END_READ_COUNT",
            "TUMOR_PAIRED_END_READ_COUNT",
            "NORMAL_SPLIT_READ_COUNT",
            "TUMOR_SPLIT_READ_COUNT",
            "ANNOTATION",
            "BREAKPOINT_TYPE",
            "CENTER",
            "CONNECTION_TYPE",
            "EVENT_INFO",
            "CLASS",
            "LENGTH",
            "COMMENTS",
            "EXTERNAL_ANNOTATION",
            "DRIVER_FILTER",
            "DRIVER_FILTER_ANNOTATION",
            "DRIVER_TIERS_FILTER",
            "DRIVER_TIERS_FILTER_ANNOTATION"
        };
        bl.setFieldNames(fieldNames);

        // write to the temp file maintained by the MySQLbulkLoader
        bl.insertRecord(
            Integer.toString(structuralVariant.getGeneticProfileId()),
            Integer.toString(structuralVariant.getSampleIdInternal()),
            Long.toString(structuralVariant.getSite1EntrezGeneId()),
            structuralVariant.getSite1EnsemblTranscriptId(),
            Integer.toString(structuralVariant.getSite1Exon()),
            structuralVariant.getSite1Chromosome(),
            Integer.toString(structuralVariant.getSite1Position()),
            structuralVariant.getSite1Description(),
            Long.toString(structuralVariant.getSite2EntrezGeneId()),
            structuralVariant.getSite2EnsemblTranscriptId(),
            Integer.toString(structuralVariant.getSite2Exon()),
            structuralVariant.getSite2Chromosome(),
            Integer.toString(structuralVariant.getSite2Position()),
            structuralVariant.getSite2Description(),
            structuralVariant.getSite2EffectOnFrame(),
            structuralVariant.getNcbiBuild(),
            structuralVariant.getDnaSupport(),
            structuralVariant.getRnaSupport(),
            Integer.toString(structuralVariant.getNormalReadCount()),
            Integer.toString(structuralVariant.getTumorReadCount()),
            Integer.toString(structuralVariant.getNormalVariantCount()),
            Integer.toString(structuralVariant.getTumorVariantCount()),
            Integer.toString(structuralVariant.getNormalPairedEndReadCount()),
            Integer.toString(structuralVariant.getTumorPairedEndReadCount()),
            Integer.toString(structuralVariant.getNormalSplitReadCount()),
            Integer.toString(structuralVariant.getTumorSplitReadCount()),
            structuralVariant.getAnnotation(),
            structuralVariant.getBreakpointType(),
            structuralVariant.getCenter(),
            structuralVariant.getConnectionType(),
            structuralVariant.getEventInfo(),
            structuralVariant.getVariantClass(),
            Integer.toString(structuralVariant.getLength()),
            structuralVariant.getComments(),
            structuralVariant.getExternalAnnotation(),
            structuralVariant.getDriverFilter(),
            structuralVariant.getDriverFilterAnn(),
            structuralVariant.getDriverTiersFilter(),
            structuralVariant.getDriverTiersFilterAnn()
        );
    }
}
