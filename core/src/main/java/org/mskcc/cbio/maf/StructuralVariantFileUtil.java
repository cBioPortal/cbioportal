///*
// * Copyright (c) 2017 The Hyve B.V.
// * This code is licensed under the GNU Affero General Public License (AGPL),
// * version 3, or (at your option) any later version.
// */
//
///*
// * This file is part of cBioPortal.
// *
// * cBioPortal is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as
// * published by the Free Software Foundation, either version 3 of the
// * License.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package org.mskcc.cbio.maf;
//
//import java.util.HashMap;
//
//import org.mskcc.cbio.portal.model.StructuralVariant;
//
///**
// * Utility Class for Parsing Structural Variant Files.
// *
// * This utility class handles variable columns and column orderings
// * within Fusion files.
// *
// * @author Sander Tan
// */
//public class StructuralVariantFileUtil {
//    public static final String TUMOR_SAMPLE_BARCODE = "Tumor_Sample_Barcode";
//    public static final String SITE1_ENTREZ_GENE_ID = "Site1_Entrez_Gene_Id";
//    public static final String SITE1_HUGO_SYMBOL = "Site1_Hugo_Symbol";
//    public static final String SITE1_ENSEMBL_TRANSCRIPT_ID = "Site1_Ensembl_Transcript_Id";
//    public static final String SITE1_EXON = "Site1_Exon";
//    public static final String SITE1_CHROMOSOME = "Site1_Chromosome";
//    public static final String SITE1_POSITION = "Site1_Position";
//    public static final String SITE1_DESCRIPTION = "Site1_Description";
//    public static final String SITE2_ENTREZ_GENE_ID = "Site2_Entrez_Gene_Id";
//    public static final String SITE2_HUGO_SYMBOL = "Site2_Hugo_Symbol";
//    public static final String SITE2_ENSEMBL_TRANSCRIPT_ID = "Site2_Ensembl_Transcript_Id";
//    public static final String SITE2_EXON = "Site2_Exon";
//    public static final String SITE2_CHROMOSOME = "Site2_Chromosome";
//    public static final String SITE2_POSITION = "Site2_Position";
//    public static final String SITE2_DESCRIPTION = "Site2_Description";
//    public static final String SITE2_EFFECT_ON_FRAME = "Site2_Effect_On_Frame";
//    public static final String DNA_SUPPORT = "DNA_Support";
//    public static final String RNA_SUPPORT = "RNA_Support";
//    public static final String NORMAL_READ_COUNT = "Normal_Read_Count";
//    public static final String TUMOR_READ_COUNT = "Tumor_Read_Count";
//    public static final String NORMAL_VARIANT_COUNT = "Normal_Variant_Count";
//    public static final String TUMOR_VARIANT_COUNT = "Tumor_Variant_Count";
//    public static final String NORMAL_PAIRED_END_READ_COUNT = "Normal_Paired_End_Read_Count";
//    public static final String TUMOR_PAIRED_END_READ_COUNT = "Tumor_Paired_End_Read_Count";
//    public static final String NORMAL_SPLIT_READ_COUNT = "Normal_Split_Read_Count";
//    public static final String TUMOR_SPLIT_READ_COUNT = "Tumor_Split_Read_Count";
//    public static final String ANNOTATION = "Annotation";
//    public static final String BREAKPOINT_TYPE = "Breakpoint_Type";
//    public static final String CENTER = "Center";
//    public static final String CONNECTION_TYPE = "Connection_Type";
//    public static final String EVENT_INFO = "Event_Info";
//    public static final String VARIANT_CLASS = "Class";
//    public static final String LENGTH = "Length";
//    public static final String COMMENTS = "Comments";
//
//    // number of headers in the header line
//    private int headerCount;
//
//    // mapping for all column names (both standard and custom columns)
//    private HashMap<String, Integer> columnIndexMap;
//
//    /**
//     * Constructor.
//     *
//     * @param headerLine    Header Line.
//     */
//    public StructuralVariantFileUtil(String headerLine)
//    {
//        // init column index map
//        this.columnIndexMap = new HashMap<String, Integer>();
//
//        // split header names
//        String parts[] = headerLine.split("\t");
//
//        // update header count
//        this.headerCount = parts.length;
//
//        // find required header indices
//        for (int i=0; i<parts.length; i++)
//        {
//            String header = parts[i];
//
//            // put the index to the map
//            this.columnIndexMap.put(header.toLowerCase(), i);
//        }
//    }
//
//    public StructuralVariantRecord parseRecord(String line)
//    {
//        String parts[] = line.split("\t", -1);
//
////        StructuralVariantRecord record = new StructuralVariantRecord();
//        StructuralVariant structuralVariant = new StructuralVariant();
//
//        structuralVariant.setSampleId(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.TUMOR_SAMPLE_BARCODE), parts));
//        structuralVariant.setSite1EntrezGeneId(TabDelimitedFileUtil.getPartLong(this.getColumnIndex(StructuralVariantFileUtil.SITE1_ENTREZ_GENE_ID), parts));
//        structuralVariant.setSite1HugoSymbol(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE1_HUGO_SYMBOL), parts));
//        structuralVariant.setSite1EnsemblTranscriptId(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE1_ENSEMBL_TRANSCRIPT_ID), parts));
//        structuralVariant.setSite1Exon(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.SITE1_EXON), parts));
//        structuralVariant.setSite1Chromosome(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE1_CHROMOSOME), parts));
//        structuralVariant.setSite1Position(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.SITE1_POSITION), parts));
//        structuralVariant.setSite1Description(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE1_DESCRIPTION), parts));
//        structuralVariant.setSite2EntrezGeneId(TabDelimitedFileUtil.getPartLong(this.getColumnIndex(StructuralVariantFileUtil.SITE2_ENTREZ_GENE_ID), parts));
//        structuralVariant.setSite2HugoSymbol(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE2_HUGO_SYMBOL), parts));
//        structuralVariant.setSite2EnsemblTranscriptId(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE2_ENSEMBL_TRANSCRIPT_ID), parts));
//        structuralVariant.setSite2Exon(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.SITE2_EXON), parts));
//        structuralVariant.setSite2Chromosome(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE2_CHROMOSOME), parts));
//        structuralVariant.setSite2Position(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.SITE2_POSITION), parts));
//        structuralVariant.setSite2Description(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE2_DESCRIPTION), parts));
//        structuralVariant.setSite2EffectOnFrame(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.SITE2_EFFECT_ON_FRAME), parts));
//        structuralVariant.setDnaSupport(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.DNA_SUPPORT), parts));
//        structuralVariant.setRnaSupport(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.RNA_SUPPORT), parts));
//        structuralVariant.setNormalReadCount(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.NORMAL_READ_COUNT), parts));
//        structuralVariant.setTumorReadCount(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.TUMOR_READ_COUNT), parts));
//        structuralVariant.setNormalVariantCount(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.NORMAL_VARIANT_COUNT), parts));
//        structuralVariant.setTumorVariantCount(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.TUMOR_VARIANT_COUNT), parts));
//        structuralVariant.setNormalPairedEndReadCount(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.NORMAL_PAIRED_END_READ_COUNT), parts));
//        structuralVariant.setTumorPairedEndReadCount(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.TUMOR_PAIRED_END_READ_COUNT), parts));
//        structuralVariant.setNormalSplitReadCount(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.NORMAL_SPLIT_READ_COUNT), parts));
//        structuralVariant.setTumorSplitReadCount(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.TUMOR_SPLIT_READ_COUNT), parts));
//        structuralVariant.setAnnotation(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.ANNOTATION), parts));
//        structuralVariant.setBreakpointType(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.BREAKPOINT_TYPE), parts));
//        structuralVariant.setCenter(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.CENTER), parts));
//        structuralVariant.setConnectionType(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.CONNECTION_TYPE), parts));
//        structuralVariant.setEventInfo(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.EVENT_INFO), parts));
//        structuralVariant.setVariantClass(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.VARIANT_CLASS), parts));
//        structuralVariant.setLength(TabDelimitedFileUtil.getPartInt(this.getColumnIndex(StructuralVariantFileUtil.LENGTH), parts));
//        structuralVariant.setComments(TabDelimitedFileUtil.getPartString(this.getColumnIndex(StructuralVariantFileUtil.COMMENTS), parts));
//
//        return structuralVariant;
//    }
//
//    // TODO this is a duplicate (MafUtil has the same method)
//    // try to factor out it into TabDelimitedFileUtil
//    public int getColumnIndex(String colName)
//    {
//        Integer index = this.columnIndexMap.get(colName.toLowerCase());
//
//        if (index == null)
//        {
//            index = -1;
//        }
//
//        return index;
//    }
//}
