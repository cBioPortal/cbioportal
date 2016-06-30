/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.cbioportal.service.SVService;
import org.cbioportal.model.SV;
import org.cbioportal.persistence.mybatis.SVMapper;

import java.io.*;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jake
 */
public class ImportSVData {
    
    private File svFile;
    private String geneticProfileStableId;
    
    @Autowired
    private SVMapper svMapper;
    
    @Autowired
    private SVService svService;
    
    @Transactional
    public void insertSV(String sampleId, String annotation, String breakpoint_type, String comments,
            String confidence_class, String conn_type, String connection_type, String event_info, 
            String mapq, Integer normal_read_count, Integer normal_variant_count, Integer paired_end_read_support,
            String site1_chrom, String site1_desc, String site1_gene, Integer site1_pos, String site2_chrom,
            String site2_desc, String site2_gene, Integer site2_pos, Integer split_read_support, 
            String sv_class_name, String sv_desc, Integer sv_length, Integer sv_variant_id, 
            Integer tumor_read_count, Integer tumor_variant_count, String variant_status_name, 
            String geneticProfile){
        SV sv = new SV();
        
        Map<String, Object> map = new HashMap<>();
        map.put("sampleId", sampleId);
        map.put("annotation", annotation);
        map.put("breakpoint_type", breakpoint_type);
        map.put("comments", comments);
        map.put("confidence_class", confidence_class);
        map.put("conn_type", conn_type);
        map.put("connection_type", connection_type);
        map.put("event_info", event_info);
        map.put("mapq", mapq);
        map.put("normal_read_count", normal_read_count);
        map.put("normal_variant_count", normal_variant_count);
        map.put("paired_end_read_support", paired_end_read_support);
        map.put("site1_chrom", site1_chrom);
        map.put("site1_desc", site1_desc);
        map.put("site1_gene", site1_gene);
        map.put("site1_pos", site1_pos);
        map.put("site2_chrom", site2_chrom);
        map.put("site2_desc", site2_desc);
        map.put("site2_gene", site2_gene);
        map.put("site2_pos", site2_pos);
        map.put("split_read_support", split_read_support);
        map.put("sv_class_name", sv_class_name);
        map.put("sv_desc", sv_desc);
        map.put("sv_length", sv_length);
        map.put("sv_variant_id", sv_variant_id);
        map.put("tumor_read_count", tumor_read_count);
        map.put("tumor_variant_count", tumor_variant_count);
        map.put("variant_status_name", variant_status_name);
        map.put("geneticProfile", this.geneticProfileStableId);
        svService.insertSV(map);
    }
    
    public ImportSVData(File svFile, String geneticProfileId){
        this.svFile = svFile;
        this.geneticProfileStableId = geneticProfileId;
    }
    
    public void importData() throws IOException{
        FileReader reader = new FileReader(this.svFile);
        try (BufferedReader buffer = new BufferedReader(reader)) {
            String line = buffer.readLine(); //skip first line
            while((line = buffer.readLine()) != null){
                String data[] = line.split("\t");
                String sampleId = !data[0].isEmpty() ? data[0] : "n/a";
                String annotation = !data[1].isEmpty() ? data[1] : "n/a";
                String breakpoint_type = !data[2].isEmpty() ? data[2] : "n/a";
                String comments = !data[3].isEmpty() ? data[3] : "n/a";
                String confidence_class = !data[4].isEmpty() ? data[4] : "n/a";
                String conn_type = !data[5].isEmpty() ? data[5] : "n/a";
                String connection_type = !data[6].isEmpty() ? data[6] : "n/a";
                String event_info = !data[7].isEmpty() ? data[7] : "n/a";
                Integer mapq = !data[8].isEmpty() ? Integer.parseInt(data[8]): -1;
                Integer normal_read_count = !data[9].isEmpty() ? Integer.parseInt(data[9]) : -1;
                Integer normal_variant_count = !data[10].isEmpty() ? Integer.parseInt(data[10]): -1;
                Integer paired_end_read_support = !data[11].isEmpty() ? Integer.parseInt(data[11]): -1;
                String site1_chrom = !data[12].isEmpty() ? data[12] : "n/a";
                String site1_desc = !data[13].isEmpty() ? data[13] : "n/a";
                String site1_gene = !data[14].isEmpty() ? data[14] : "n/a";
                Integer site1_pos = !data[15].isEmpty() ? Integer.parseInt(data[15]): -1;
                String site2_chrom = !data[16].isEmpty() ? data[16] : "n/a";
                String site2_desc = !data[17].isEmpty() ? data[17] : "n/a";
                String site2_gene = !data[18].isEmpty() ? data[18] : "n/a";
                Integer site2_pos = !data[19].isEmpty() ? Integer.parseInt(data[19]): -1;
                Integer split_read_support = !data[20].isEmpty() ? Integer.parseInt(data[20]): -1;
                String sv_class_name = !data[21].isEmpty() ? data[21] : "n/a";
                String sv_desc = !data[22].isEmpty() ? data[22] : "n/a";
                Integer sv_length = !data[23].isEmpty() ? Integer.parseInt(data[23]): -1;
                Integer sv_variant_id = !data[24].isEmpty() ? Integer.parseInt(data[24]): -1;
                Integer tumor_read_count = !data[25].isEmpty() ? Integer.parseInt(data[25]): -1;
                Integer tumor_variant_count = !data[26].isEmpty() ? Integer.parseInt(data[26]): -1;
                String variant_status_name = !data[27].isEmpty() ? data[27] : "n/a";
                insertSV(sampleId, annotation, breakpoint_type, comments,
                        confidence_class, conn_type, connection_type, event_info,
                        mapq, normal_read_count, normal_variant_count, paired_end_read_support,
                        site1_chrom, site1_desc, site1_gene, site1_pos, site2_chrom,
                        site2_desc, site2_gene, site2_pos, split_read_support,
                        sv_class_name, sv_desc, sv_length, sv_variant_id,
                        tumor_read_count, tumor_variant_count, variant_status_name);
            }
        } //skip first line
        sv.setGeneticProfile(geneticProfile);
        
    }
}
