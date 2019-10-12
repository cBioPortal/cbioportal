/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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

package org.mskcc.cbio.portal.util;

import java.util.HashMap;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.model.StructuralVariant;

/**
 * @author ochoaa
 */
public class StructuralVariantUtil {
    private HashMap<String, Integer> columnIndexMap;

    // Column names structural variant file
    public static final String SAMPLE_ID = "Sample_ID";
    public static final String SITE1_ENTREZ_GENE_ID = "Site1_Entrez_Gene_Id";
    public static final String SITE1_HUGO_SYMBOL = "Site1_Hugo_Symbol";
    public static final String SITE1_ENSEMBL_TRANSCRIPT_ID = "Site1_Ensembl_Transcript_Id";
    public static final String SITE1_EXON = "Site1_Exon";
    public static final String SITE1_CHROMOSOME = "Site1_Chromosome";
    public static final String SITE1_POSITION = "Site1_Position";
    public static final String SITE1_DESCRIPTION = "Site1_Description";
    public static final String SITE2_ENTREZ_GENE_ID = "Site2_Entrez_Gene_Id";
    public static final String SITE2_HUGO_SYMBOL = "Site2_Hugo_Symbol";
    public static final String SITE2_ENSEMBL_TRANSCRIPT_ID = "Site2_Ensembl_Transcript_Id";
    public static final String SITE2_EXON = "Site2_Exon";
    public static final String SITE2_CHROMOSOME = "Site2_Chromosome";
    public static final String SITE2_POSITION = "Site2_Position";
    public static final String SITE2_DESCRIPTION = "Site2_Description";
    public static final String SITE2_EFFECT_ON_FRAME = "Site2_Effect_On_Frame";
    public static final String NCBI_BUILD = "NCBI_Build";
    public static final String DNA_SUPPORT = "DNA_Support";
    public static final String RNA_SUPPORT = "RNA_Support";
    public static final String NORMAL_READ_COUNT = "Normal_Read_Count";
    public static final String TUMOR_READ_COUNT = "Tumor_Read_Count";
    public static final String NORMAL_VARIANT_COUNT = "Normal_Variant_Count";
    public static final String TUMOR_VARIANT_COUNT = "Tumor_Variant_Count";
    public static final String NORMAL_PAIRED_END_READ_COUNT = "Normal_Paired_End_Read_Count";
    public static final String TUMOR_PAIRED_END_READ_COUNT = "Tumor_Paired_End_Read_Count";
    public static final String NORMAL_SPLIT_READ_COUNT = "Normal_Split_Read_Count";
    public static final String TUMOR_SPLIT_READ_COUNT = "Tumor_Split_Read_Count";
    public static final String ANNOTATION = "Annotation";
    public static final String BREAKPOINT_TYPE = "Breakpoint_Type";
    public static final String CENTER = "Center";
    public static final String CONNECTION_TYPE = "Connection_Type";
    public static final String EVENT_INFO = "Event_Info";
    public static final String VARIANT_CLASS = "Class";
    public static final String LENGTH = "Length";
    public static final String COMMENTS = "Comments";
    public static final String EXTERNAL_ANNOTATION = "External_Annotation";
    public static final String DRIVER_FILTER = "cbp_driver";
    public static final String DRIVER_FILTER_ANNOTATION = "cbp_driver_annotation";
    public static final String DRIVER_TIERS_FILTER = "cbp_driver_tiers";
    public static final String DRIVER_TIERS_FILTER_ANNOTATION = "cbp_driver_tiers_annotation";

    public StructuralVariantUtil(){}

    public StructuralVariantUtil(String line) {
        this.columnIndexMap = new HashMap<String, Integer>();
        String[] headerParts = line.trim().split("\t");

        // Find header indices
        for (int i=0; i<headerParts.length; i++) {
            // Put the index in the map
            this.columnIndexMap.put(headerParts[i].toLowerCase(), i);
        }
    }

    public StructuralVariant parseStructuralVariantRecord(String[] parts) {
        StructuralVariant structuralVariant = new StructuralVariant();
        structuralVariant.setSampleId(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SAMPLE_ID), parts));
        structuralVariant.setSite1EntrezGeneId(TabDelimitedFileUtil.getPartLong(getColumnIndex(StructuralVariantUtil.SITE1_ENTREZ_GENE_ID), parts));
        structuralVariant.setSite1HugoSymbol(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE1_HUGO_SYMBOL), parts));
        structuralVariant.setSite1EnsemblTranscriptId(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE1_ENSEMBL_TRANSCRIPT_ID), parts));
        structuralVariant.setSite1Exon(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.SITE1_EXON), parts));
        structuralVariant.setSite1Chromosome(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE1_CHROMOSOME), parts));
        structuralVariant.setSite1Position(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.SITE1_POSITION), parts));
        structuralVariant.setSite1Description(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE1_DESCRIPTION), parts));
        structuralVariant.setSite2EntrezGeneId(TabDelimitedFileUtil.getPartLong(getColumnIndex(StructuralVariantUtil.SITE2_ENTREZ_GENE_ID), parts));
        structuralVariant.setSite2HugoSymbol(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE2_HUGO_SYMBOL), parts));
        structuralVariant.setSite2EnsemblTranscriptId(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE2_ENSEMBL_TRANSCRIPT_ID), parts));
        structuralVariant.setSite2Exon(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.SITE2_EXON), parts));
        structuralVariant.setSite2Chromosome(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE2_CHROMOSOME), parts));
        structuralVariant.setSite2Position(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.SITE2_POSITION), parts));
        structuralVariant.setSite2Description(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE2_DESCRIPTION), parts));
        structuralVariant.setSite2EffectOnFrame(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.SITE2_EFFECT_ON_FRAME), parts));
        structuralVariant.setNcbiBuild(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.NCBI_BUILD), parts));
        structuralVariant.setDnaSupport(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.DNA_SUPPORT), parts));
        structuralVariant.setRnaSupport(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.RNA_SUPPORT), parts));
        structuralVariant.setNormalReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.NORMAL_READ_COUNT), parts));
        structuralVariant.setTumorReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.TUMOR_READ_COUNT), parts));
        structuralVariant.setNormalVariantCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.NORMAL_VARIANT_COUNT), parts));
        structuralVariant.setTumorVariantCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.TUMOR_VARIANT_COUNT), parts));
        structuralVariant.setNormalPairedEndReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.NORMAL_PAIRED_END_READ_COUNT), parts));
        structuralVariant.setTumorPairedEndReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.TUMOR_PAIRED_END_READ_COUNT), parts));
        structuralVariant.setNormalSplitReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.NORMAL_SPLIT_READ_COUNT), parts));
        structuralVariant.setTumorSplitReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.TUMOR_SPLIT_READ_COUNT), parts));
        structuralVariant.setAnnotation(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.ANNOTATION), parts));
        structuralVariant.setBreakpointType(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.BREAKPOINT_TYPE), parts));
        structuralVariant.setCenter(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.CENTER), parts));
        structuralVariant.setConnectionType(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.CONNECTION_TYPE), parts));
        structuralVariant.setEventInfo(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.EVENT_INFO), parts));
        structuralVariant.setVariantClass(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.VARIANT_CLASS), parts));
        structuralVariant.setLength(TabDelimitedFileUtil.getPartInt(getColumnIndex(StructuralVariantUtil.LENGTH), parts));
        structuralVariant.setComments(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.COMMENTS), parts));
        structuralVariant.setExternalAnnotation(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.EXTERNAL_ANNOTATION), parts));
        structuralVariant.setDriverFilter(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.DRIVER_FILTER), parts));
        structuralVariant.setDriverFilterAnn(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.DRIVER_FILTER_ANNOTATION), parts));
        structuralVariant.setDriverTiersFilter(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.DRIVER_TIERS_FILTER), parts));
        structuralVariant.setDriverTiersFilterAnn(TabDelimitedFileUtil.getPartString(getColumnIndex(StructuralVariantUtil.DRIVER_TIERS_FILTER_ANNOTATION), parts));
        return structuralVariant;
    }

    public int getColumnIndex(String colName) {
        Integer index = this.columnIndexMap.get(colName.toLowerCase());
        if (index == null) {
            index = -1;
        }
        return index;
    }

    public Boolean hasRequiredStructuralVariantFields(StructuralVariant record) {
        return !record.getSite1EnsemblTranscriptId().equalsIgnoreCase(TabDelimitedFileUtil.NA_STRING) &&
                !record.getSite2EnsemblTranscriptId().equalsIgnoreCase(TabDelimitedFileUtil.NA_STRING) &&
                record.getSite1Exon() != -1 &&
                record.getSite2Exon() != -1 &&
                (record.getSite1EntrezGeneId() != Long.MIN_VALUE || !record.getSite1HugoSymbol().equalsIgnoreCase(TabDelimitedFileUtil.NA_STRING)) &&
                (record.getSite2EntrezGeneId() != Long.MIN_VALUE || !record.getSite2HugoSymbol().equalsIgnoreCase(TabDelimitedFileUtil.NA_STRING));
    }

}
