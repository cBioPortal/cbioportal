insert into type_of_cancer (type_of_cancer_id,name,dedicated_color,short_name,parent) values ('brca','Breast Invasive Carcinoma','HotPink','Breast','tissue');
insert into type_of_cancer (type_of_cancer_id,name,dedicated_color,short_name,parent) values ('acc','Adrenocortical Carcinoma','Purple','ACC','adrenal_gland');

insert into `reference_genome` values (1, 'human', 'hg19', 'GRCh37', NULL, 'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips', '2009-02-01 00:00:00');
insert into `reference_genome` values (2, 'human', 'hg38', 'GRCh38', NULL, 'http://hgdownload.cse.ucsc.edu/goldenPath/hg38/bigZips', '2013-12-01 00:00:00');

insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public,pmid,citation,groups,status,import_date,reference_genome_id) values(1,'study_tcga_pub','brca','Breast Invasive Carcinoma (TCGA, Nature 2012)','<a href=\"http://cancergenome.nih.gov/\">The Cancer Genome Atlas (TCGA)</a> Breast Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\"http://tcga-data.nci.nih.gov/tcga/\">Raw data via the TCGA Data Portal</a>.',1,'23000897,26451490','TCGA, Nature 2012, ...','SU2C-PI3K;PUBLIC;GDAC',0,'2011-12-18 13:17:17+00:00',1);
insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public,pmid,citation,groups,status,import_date,reference_genome_id) values(2,'acc_tcga','acc','Adrenocortical Carcinoma (TCGA, Provisional)','TCGA Adrenocortical Carcinoma; raw data at the <A HREF="https://tcga-data.nci.nih.gov/">NCI</A>.',1,'23000897','TCGA, Nature 2012','SU2C-PI3K;PUBLIC;GDAC',0,'2013-10-12 11:11:15+00:00',1);

insert into cancer_study_tags (cancer_study_id,tags) values(1,'{"Analyst": {"Name": "Jack", "Email": "jack@something.com"}, "Load id": 35}');
insert into cancer_study_tags (cancer_study_id,tags) values(2,'{"Load id": 36}');

insert into genetic_entity (id,entity_type,stable_id) values (1,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (2,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (3,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (4,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (5,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (6,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (7,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (8,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (9,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (10,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (11,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (12,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (13,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (14,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (15,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (16,'GENE', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (17,'GENESET', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (18,'GENESET', NULL);
insert into genetic_entity (id,entity_type,stable_id) values (19,'GENERIC_ASSAY','17-AAG');
insert into genetic_entity (id,entity_type,stable_id) values (20,'GENERIC_ASSAY','AEW541');
insert into genetic_entity (id,entity_type,stable_id) values (28,'GENERIC_ASSAY','mean_1');
insert into genetic_entity (id,entity_type,stable_id) values (29,'GENERIC_ASSAY','mean_2');

insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(207,'AKT1',1,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(208,'AKT2',2,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(10000,'AKT3',3,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(369,'ARAF',4,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(472,'ATM',5,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(673,'BRAF',6,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(672,'BRCA1',7,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(675,'BRCA2',8,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(3265,'HRAS',9,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(3845,'KRAS',10,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(4893,'NRAS',11,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(79501,'OR4F5',12,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(148398,'SAMD11',13,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(26155,'NOC2L',14,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(2064,'ERBB2',15,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(2886,'GRB7',16,'protein-coding');
insert into cosmic_mutation (cosmic_mutation_id,protein_change,entrez_gene_id,count,keyword) values(3677745,'D45A',79501,1,'OR4F5 D45 missense');
insert into cosmic_mutation (cosmic_mutation_id,protein_change,entrez_gene_id,count,keyword) values(426644,'G145C',79501,1,'OR4F5 G145 missense');
insert into cosmic_mutation (cosmic_mutation_id,protein_change,entrez_gene_id,count,keyword) values(460103,'P23P',148398,1,'SAMD11 P23 silent');
insert into cosmic_mutation (cosmic_mutation_id,protein_change,entrez_gene_id,count,keyword) values(4010395,'S146S',26155,1,'NOC2L S146 silent');
insert into cosmic_mutation (cosmic_mutation_id,protein_change,entrez_gene_id,count,keyword) values(1290240,'M1T',26155,1,'NOC2L truncating');
insert into cosmic_mutation (cosmic_mutation_id,protein_change,entrez_gene_id,count,keyword) values(4010425,'Q197*',26155,1,'NOC2L truncating');

insert into gene_alias (entrez_gene_id,gene_alias) values (207,'AKT alias');
insert into gene_alias (entrez_gene_id,gene_alias) values (207,'AKT alias2');
insert into gene_alias (entrez_gene_id,gene_alias) values (675,'BRCA1 alias');

insert into reference_genome_gene (entrez_gene_id,cytoband,start,end,chr,reference_genome_id) values(207,'14q32.33',105235686,105262088,14,1);
insert into reference_genome_gene (entrez_gene_id,cytoband,start,end,chr,reference_genome_id) values(207,'14q32.33',104769349,104795751,14,2);
insert into reference_genome_gene (entrez_gene_id,cytoband,start,end,chr,reference_genome_id) values(208,'19q13.2', 40736224, 40791443,19,1);
insert into reference_genome_gene (entrez_gene_id,cytoband,start,end,chr,reference_genome_id) values(208,'19q13.2', 40230317, 40285536,19,2);

insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (2,'study_tcga_pub_gistic',1,'COPY_NUMBER_ALTERATION','DISCRETE','Putative copy-number alterations from GISTIC','Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (3,'study_tcga_pub_mrna',1,'MRNA_EXPRESSION','Z-SCORE','mRNA expression (microarray)','Expression levels (Agilent microarray).',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (10,'study_tcga_pub_m_na',1,'MRNA_EXPRESSION','Z-SCORE','mRNA expression (microarray)','Expression levels (Agilent microarray).',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (4,'study_tcga_pub_log2CNA',1,'COPY_NUMBER_ALTERATION','LOG2-VALUE','Log2 copy-number values','Log2 copy-number VALUESfor each gene (from Affymetrix SNP6).',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (5,'study_tcga_pub_methylation_hm27',1,'METHYLATION','CONTINUOUS','Methylation (HM27)','Methylation beta-VALUES (HM27 platform). For genes with multiple methylation probes, the probe least correlated with expression is selected.',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (6,'study_tcga_pub_mutations',1,'MUTATION_EXTENDED','MAF','Mutations','Mutation data from whole exome sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (7,'study_tcga_pub_sv',1,'STRUCTURAL_VARIANT','SV','Structural Variants','Structural Variants detected by Illumina HiSeq sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (8,'acc_tcga_mutations',2,'MUTATION_EXTENDED','MAF','Mutations','Mutation data from whole exome sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (13,'acc_tcga_sv',2,'STRUCTURAL_VARIANT','SV','Structural Variants','Structural Variants detected by Illumina HiSeq sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (9,'study_tcga_pub_gsva_scores',1,'GENESET_SCORE','GSVA-SCORE','GSVA scores','GSVA scores for oncogenic signature gene sets from MsigDB calculated with GSVA version 1.22.4, R version 3.3.2.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab,generic_assay_type) values (11,'study_tcga_pub_treatment_ic50',1,'GENERIC_ASSAY','IC50','Treatment IC50 values','Treatment response IC50 values',1,'TREATMENT_RESPONSE');
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab,generic_assay_type) values (12,'study_tcga_pub_mutational_signature',1,'GENERIC_ASSAY','LIMIT-VALUE','mutational_signature values','mutational_signature values',1,'MUTATIONAL_SIGNATURE');

insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (2,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (3,'2,3,6,8,9,10,12,13,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (4,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (5,'2,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (11,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');

insert into patient (internal_id,stable_id,cancer_study_id) values (1,'TCGA-A1-A0SB',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (2,'TCGA-A1-A0SD',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (3,'TCGA-A1-A0SE',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (4,'TCGA-A1-A0SF',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (5,'TCGA-A1-A0SG',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (6,'TCGA-A1-A0SH',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (7,'TCGA-A1-A0SI',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (8,'TCGA-A1-A0SJ',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (9,'TCGA-A1-A0SK',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (10,'TCGA-A1-A0SM',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (11,'TCGA-A1-A0SN',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (12,'TCGA-A1-A0SO',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (13,'TCGA-A1-A0SP',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (14,'TCGA-A1-A0SQ',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (15,'TCGA-A1-B0SO',2);
insert into patient (internal_id,stable_id,cancer_study_id) values (16,'TCGA-A1-B0SP',2);
insert into patient (internal_id,stable_id,cancer_study_id) values (17,'TCGA-A1-B0SQ',2);
insert into patient (internal_id,stable_id,cancer_study_id) values (18,'TCGA-A1-A0SB',2);
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values(10,'1,2,3,4,5,6,7,8,9,10,11,');

insert into sample (internal_id,stable_id,sample_type,patient_id) values (1,'TCGA-A1-A0SB-01','Primary Solid Tumor',1);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (2,'TCGA-A1-A0SD-01','Primary Solid Tumor',2);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (3,'TCGA-A1-A0SE-01','Primary Solid Tumor',3);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (4,'TCGA-A1-A0SF-01','Primary Solid Tumor',4);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (5,'TCGA-A1-A0SG-01','Primary Solid Tumor',5);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (6,'TCGA-A1-A0SH-01','Primary Solid Tumor',6);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (7,'TCGA-A1-A0SI-01','Primary Solid Tumor',7);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (8,'TCGA-A1-A0SJ-01','Primary Solid Tumor',8);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (9,'TCGA-A1-A0SK-01','Primary Solid Tumor',9);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (10,'TCGA-A1-A0SM-01','Primary Solid Tumor',10);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (11,'TCGA-A1-A0SN-01','Primary Solid Tumor',11);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (12,'TCGA-A1-A0SO-01','Primary Solid Tumor',12);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (13,'TCGA-A1-A0SP-01','Primary Solid Tumor',13);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (14,'TCGA-A1-A0SQ-01','Primary Solid Tumor',14);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (15,'TCGA-A1-B0SO-01','Primary Solid Tumor',15);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (16,'TCGA-A1-B0SP-01','Primary Solid Tumor',16);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (17,'TCGA-A1-B0SQ-01','Primary Solid Tumor',17);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (18,'TCGA-A1-A0SB-02','Primary Solid Tumor',1);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (19,'TCGA-A1-A0SB-01','Primary Solid Tumor',18);


insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2038,672,'17',41244748,41244748,'G','A','Q934*','Nonsense_Mutation','37','+','SNP','rs80357223','unknown','NM_007294','c.(2800-2802)CAG>TAG','P38398',934,934,1,'BRCA1 truncating');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (22604,672,'17',41258504,41258504,'A','C','C61G','Missense_Mutation','37','+','SNP','rs28897672','byCluster','NM_007294','c.(181-183)TGT>GGT','P38398',61,61,1,'BRCA1 C61 missense');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2039,672,'17',41276033,41276033,'C','T','C27_splice','Splice_Site','37','+','SNP','rs80358010','byCluster','NM_007294','c.e2+1','NA',-1,-1,1,'BRCA1 truncating');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2040,207,'17',41244748,41244748,'G','A','Q934*','Nonsense_Mutation','37','+','SNP','rs80357223','unknown','NM_007294','c.(2800-2802)CAG>TAG','P38398',934,934,1,'BRCA1 truncating');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2041,207,'17',41258504,41258504,'A','C','C61G','Missense_Mutation','37','+','SNP','rs28897672','byCluster','NM_007294','c.(181-183)TGT>GGT','P38398',61,61,1,'BRCA1 C61 missense');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2042,208,'17',41276033,41276033,'C','T','C27_splice','Splice_Site','37','+','SNP','rs80358010','byCluster','NM_007294','c.e2+1','NA',-1,-1,1,'BRCA1 truncating');

insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2038,6,6, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (22604,6,6, 'Putative_Passenger', 'Pathogenic', 'Tier 2', 'Potentially Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2039,6,12, 'Putative_Passenger', 'Pathogenic', 'Tier 1', 'Highly Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2038,6,7, 'Putative_Driver', 'Pathogenic', 'Tier 2', 'Potentially Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2039,6,13, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2040,6,1, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2041,6,2, 'Putative_Passenger', 'Pathogenic', 'Tier 2', 'Potentially Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2042,6,3, 'Putative_Passenger', 'Pathogenic', 'Tier 1', 'Highly Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2042,8,15, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');

insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2038,6,6,672,'genome.wustl.edu','IlluminaGAIIx','NA','Unknown','G','A','TCGA-A1-A0SH-10A-03D-A099-09','G','A','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',1,0,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (22604,6,6,672,'genome.wustl.edu','IlluminaGAIIx','GERMLINE','Unknown','A','C','TCGA-A1-A0SH-10A-03D-A099-09','A','C','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2039,6,12,672,'genome.wustl.edu','IlluminaGAIIx','GERMLINE','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2038,6,7,672,'genome.wustl.edu','IlluminaGAIIx','GERMLINE','Unknown','G','A','TCGA-A1-A0SH-10A-03D-A099-09','G','A','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2039,6,13,672,'genome.wustl.edu','IlluminaGAIIx','GERMLINE','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2040,6,1,207,'genome.wustl.edu','IlluminaGAIIx','GERMLINE','Unknown','G','A','TCGA-A1-A0SH-10A-03D-A099-09','G','A','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2041,6,2,207,'genome.wustl.edu','IlluminaGAIIx','GERMLINE','Unknown','A','C','TCGA-A1-A0SH-10A-03D-A099-09','A','C','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',0,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2042,6,3,208,'genome.wustl.edu','IlluminaGAIIx','GERMLINE','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2042,8,15,208,'genome.wustl.edu','IlluminaGAIIx','GERMLINE','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');

insert into gene_panel (internal_id,stable_id,description) values (1,'TESTPANEL1','A test panel consisting of a few genes');
insert into gene_panel (internal_id,stable_id,description) values (2,'TESTPANEL2','Another test panel consisting of a few genes');

insert into gene_panel_list (internal_id,gene_id) values (1,207);
insert into gene_panel_list (internal_id,gene_id) values (1,369);
insert into gene_panel_list (internal_id,gene_id) values (1,672);
insert into gene_panel_list (internal_id,gene_id) values (2,207);
insert into gene_panel_list (internal_id,gene_id) values (2,208);
insert into gene_panel_list (internal_id,gene_id) values (2,4893);

insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,2,1);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,3,1);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,2,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,3,1);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,4,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,5,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (3,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (3,3,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (3,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (3,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (4,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (4,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (5,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (5,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (6,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (6,3,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (6,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (6,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (7,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (7,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (7,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (8,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (8,3,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (8,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (8,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (9,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (9,3,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (9,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (9,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (10,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (10,3,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (10,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (10,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (11,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (11,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (12,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (12,3,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (12,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (12,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (13,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (13,3,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (13,4,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (13,6,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (14,2,NULL);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (14,4,NULL);

insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (1,'study_tcga_pub_all','other',1,'All Tumors','All tumor samples');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (2,'study_tcga_pub_acgh','other',1,'Tumors aCGH','All tumors with aCGH data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (3,'study_tcga_pub_cnaseq','other',1,'Tumors with sequencing and aCGH data','All tumor samples that have CNA and sequencing data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (4,'study_tcga_pub_complete','other',1,'Complete samples (mutations, copy-number, expression)','Samples with complete data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (5,'study_tcga_pub_log2CNA','other',1,'Tumors log2 copy-number','All tumors with log2 copy-number data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (6,'study_tcga_pub_methylation_hm27','all_cases_with_mutation_data',1,'Tumors with methylation data','All samples with methylation (HM27) data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (7,'study_tcga_pub_mrna','other',1,'Tumors with mRNA data (Agilent microarray)','All samples with mRNA expression data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (8,'study_tcga_pub_sequenced','other',1,'Sequenced Tumors','All sequenced samples');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (9,'study_tcga_pub_cna','other',1,'Tumor Samples with CNA data','All tumors with CNA data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (10,'study_tcga_pub_rna_seq_v2_mrna','other',1,'Tumor Samples with mRNA data (RNA Seq V2)','All samples with mRNA expression data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (11,'study_tcga_pub_microrna','other',1,'Tumors with microRNA data (microRNA-Seq)','All samples with microRNA data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (12,'study_tcga_pub_rppa','other',1,'Tumor Samples with RPPA data','Tumors with reverse phase protein array (RPPA) data for about 200 antibodies');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (13,'study_tcga_pub_3way_complete','other',1,'All Complete Tumors','All tumor samples that have mRNA,CNA and sequencing data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (14,'acc_tcga_all','other',2,'All Tumors','All tumor samples');

insert into sample_list_list (list_id,sample_id) values (1,1);
insert into sample_list_list (list_id,sample_id) values (1,2);
insert into sample_list_list (list_id,sample_id) values (1,3);
insert into sample_list_list (list_id,sample_id) values (1,4);
insert into sample_list_list (list_id,sample_id) values (1,5);
insert into sample_list_list (list_id,sample_id) values (1,6);
insert into sample_list_list (list_id,sample_id) values (1,7);
insert into sample_list_list (list_id,sample_id) values (1,8);
insert into sample_list_list (list_id,sample_id) values (1,9);
insert into sample_list_list (list_id,sample_id) values (1,10);
insert into sample_list_list (list_id,sample_id) values (1,11);
insert into sample_list_list (list_id,sample_id) values (1,12);
insert into sample_list_list (list_id,sample_id) values (1,13);
insert into sample_list_list (list_id,sample_id) values (1,14);
insert into sample_list_list (list_id,sample_id) values (2,1);
insert into sample_list_list (list_id,sample_id) values (2,2);
insert into sample_list_list (list_id,sample_id) values (2,3);
insert into sample_list_list (list_id,sample_id) values (2,4);
insert into sample_list_list (list_id,sample_id) values (2,5);
insert into sample_list_list (list_id,sample_id) values (2,6);
insert into sample_list_list (list_id,sample_id) values (2,7);
insert into sample_list_list (list_id,sample_id) values (2,8);
insert into sample_list_list (list_id,sample_id) values (2,9);
insert into sample_list_list (list_id,sample_id) values (2,10);
insert into sample_list_list (list_id,sample_id) values (2,11);
insert into sample_list_list (list_id,sample_id) values (2,12);
insert into sample_list_list (list_id,sample_id) values (2,13);
insert into sample_list_list (list_id,sample_id) values (2,14);
insert into sample_list_list (list_id,sample_id) values (3,2);
insert into sample_list_list (list_id,sample_id) values (3,3);
insert into sample_list_list (list_id,sample_id) values (3,6);
insert into sample_list_list (list_id,sample_id) values (3,8);
insert into sample_list_list (list_id,sample_id) values (3,9);
insert into sample_list_list (list_id,sample_id) values (3,10);
insert into sample_list_list (list_id,sample_id) values (3,12);
insert into sample_list_list (list_id,sample_id) values (4,2);
insert into sample_list_list (list_id,sample_id) values (4,3);
insert into sample_list_list (list_id,sample_id) values (4,6);
insert into sample_list_list (list_id,sample_id) values (4,8);
insert into sample_list_list (list_id,sample_id) values (4,9);
insert into sample_list_list (list_id,sample_id) values (4,10);
insert into sample_list_list (list_id,sample_id) values (4,12);
insert into sample_list_list (list_id,sample_id) values (5,1);
insert into sample_list_list (list_id,sample_id) values (5,2);
insert into sample_list_list (list_id,sample_id) values (5,3);
insert into sample_list_list (list_id,sample_id) values (5,4);
insert into sample_list_list (list_id,sample_id) values (5,5);
insert into sample_list_list (list_id,sample_id) values (5,6);
insert into sample_list_list (list_id,sample_id) values (5,7);
insert into sample_list_list (list_id,sample_id) values (5,8);
insert into sample_list_list (list_id,sample_id) values (5,9);
insert into sample_list_list (list_id,sample_id) values (5,10);
insert into sample_list_list (list_id,sample_id) values (5,11);
insert into sample_list_list (list_id,sample_id) values (5,12);
insert into sample_list_list (list_id,sample_id) values (5,13);
insert into sample_list_list (list_id,sample_id) values (5,14);
insert into sample_list_list (list_id,sample_id) values (6,2);
insert into sample_list_list (list_id,sample_id) values (7,2);
insert into sample_list_list (list_id,sample_id) values (7,3);
insert into sample_list_list (list_id,sample_id) values (7,6);
insert into sample_list_list (list_id,sample_id) values (7,8);
insert into sample_list_list (list_id,sample_id) values (7,9);
insert into sample_list_list (list_id,sample_id) values (7,10);
insert into sample_list_list (list_id,sample_id) values (7,12);
insert into sample_list_list (list_id,sample_id) values (7,13);
insert into sample_list_list (list_id,sample_id) values (8,2);
insert into sample_list_list (list_id,sample_id) values (8,3);
insert into sample_list_list (list_id,sample_id) values (8,6);
insert into sample_list_list (list_id,sample_id) values (8,8);
insert into sample_list_list (list_id,sample_id) values (8,9);
insert into sample_list_list (list_id,sample_id) values (8,10);
insert into sample_list_list (list_id,sample_id) values (8,12);
insert into sample_list_list (list_id,sample_id) values (9,2);
insert into sample_list_list (list_id,sample_id) values (9,3);
insert into sample_list_list (list_id,sample_id) values (9,6);
insert into sample_list_list (list_id,sample_id) values (9,8);
insert into sample_list_list (list_id,sample_id) values (9,9);
insert into sample_list_list (list_id,sample_id) values (9,10);
insert into sample_list_list (list_id,sample_id) values (9,12);
insert into sample_list_list (list_id,sample_id) values (10,2);
insert into sample_list_list (list_id,sample_id) values (10,3);
insert into sample_list_list (list_id,sample_id) values (10,6);
insert into sample_list_list (list_id,sample_id) values (10,8);
insert into sample_list_list (list_id,sample_id) values (10,9);
insert into sample_list_list (list_id,sample_id) values (10,10);
insert into sample_list_list (list_id,sample_id) values (10,12);
insert into sample_list_list (list_id,sample_id) values (13,2);
insert into sample_list_list (list_id,sample_id) values (13,3);
insert into sample_list_list (list_id,sample_id) values (13,6);
insert into sample_list_list (list_id,sample_id) values (13,8);
insert into sample_list_list (list_id,sample_id) values (13,9);
insert into sample_list_list (list_id,sample_id) values (13,10);
insert into sample_list_list (list_id,sample_id) values (13,12);
insert into sample_list_list (list_id,sample_id) values (14,15);

insert into copy_number_seg (seg_id,cancer_study_id,sample_id,chr,start,end,num_probes,segment_mean) values (50236594,1,1,'1',324556,180057677,291,0.0519);
insert into copy_number_seg (seg_id,cancer_study_id,sample_id,chr,start,end,num_probes,segment_mean) values (50236595,1,1,'2',224556,327677,391,0.0219);
insert into copy_number_seg (seg_id,cancer_study_id,sample_id,chr,start,end,num_probes,segment_mean) values (50236593,1,2,	'2',1402650,190262486,207,0.0265);
insert into copy_number_seg (seg_id,cancer_study_id,sample_id,chr,start,end,num_probes,segment_mean) values (50236592,1,3,	'3',1449872,194238390,341,0.0347);
insert into copy_number_seg (seg_id,cancer_study_id,sample_id,chr,start,end,num_probes,segment_mean) values (50236500,2,15,	'2',14492,19423390,41,0.047);

insert into clinical_patient (internal_id,attr_id,attr_value) values (1,'RETROSPECTIVE_COLLECTION','NO');
insert into clinical_patient (internal_id,attr_id,attr_value) values (1,'FORM_COMPLETION_DATE','2013-12-5');
insert into clinical_patient (internal_id,attr_id,attr_value) values (1,'OTHER_PATIENT_ID','286CF147-B7F7-4A05-8E41-7FBD3717AD71');
insert into clinical_patient (internal_id,attr_id,attr_value) values (2,'PROSPECTIVE_COLLECTION','YES');
insert into clinical_patient (internal_id,attr_id,attr_value) values (15,'DFS_MONTHS','5.72');
insert into clinical_patient (internal_id,attr_id,attr_value) values (15,'DFS_STATUS','1:Recurred/Progressed');
insert into clinical_patient (internal_id,attr_id,attr_value) values (15,'OS_MONTHS','12.3');
insert into clinical_patient (internal_id,attr_id,attr_value) values (15,'OS_STATUS','0:LIVING');
insert into clinical_patient (internal_id,attr_id,attr_value) values (18,'RETROSPECTIVE_COLLECTION','NO');

insert into clinical_sample (internal_id,attr_id,attr_value) values (1,'OTHER_SAMPLE_ID','5C631CE8-F96A-4C35-A459-556FC4AB21E1');
insert into clinical_sample (internal_id,attr_id,attr_value) values (1,'DAYS_TO_COLLECTION','276');
insert into clinical_sample (internal_id,attr_id,attr_value) values (1,'IS_FFPE','NO');
insert into clinical_sample (internal_id,attr_id,attr_value) values (1,'SAMPLE_TYPE','Secondary Tumor');
insert into clinical_sample (internal_id,attr_id,attr_value) values (2,'OCT_EMBEDDED','false');
insert into clinical_sample (internal_id,attr_id,attr_value) values (2,'DAYS_TO_COLLECTION','277');
insert into clinical_sample (internal_id,attr_id,attr_value) values (2,'PATHOLOGY_REPORT_FILE_NAME','TCGA-GC-A3BM.F3408556-9259-4700-B9A0-F41E516B420C.pdf');
insert into clinical_sample (internal_id,attr_id,attr_value) values (2,'SAMPLE_TYPE','Primary Tumor');
insert into clinical_sample (internal_id,attr_id,attr_value) values (15,'OTHER_SAMPLE_ID','91E7F41C-17B3-4724-96EF-D3C207B964E1');
insert into clinical_sample (internal_id,attr_id,attr_value) values (15,'DAYS_TO_COLLECTION','111');
insert into clinical_sample (internal_id,attr_id,attr_value) values (19,'DAYS_TO_COLLECTION','111');


insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('RETROSPECTIVE_COLLECTION','Tissue Retrospective Collection Indicator','Text indicator for the time frame of tissue procurement,indicating that the tissue was obtained and stored prior to the initiation of the project.','STRING',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('PROSPECTIVE_COLLECTION','Tissue Prospective Collection Indicator','Text indicator for the time frame of tissue procurement,indicating that the tissue was procured in parallel to the project.','STRING',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('FORM_COMPLETION_DATE','Form completion date','Form completion date','STRING',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OTHER_PATIENT_ID','Other Patient ID','Legacy DMP patient identifier (DMPnnnn)','STRING',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('DFS_MONTHS','Disease Free (Months)','Disease free (months) since initial treatment.','NUMBER',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('DFS_STATUS','Disease Free Status','Disease free status since initial treatment.','STRING',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OS_MONTHS','Overall Survival (Months)','Overall survival in months since initial diagonosis.','NUMBER',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OS_STATUS','Overall Survival Status','Overall patient survival status.','STRING',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OTHER_SAMPLE_ID','Other Sample ID','Legacy DMP sample identifier (DMPnnnn)','STRING',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('DAYS_TO_COLLECTION','Days to Sample Collection.','Days to sample collection.','NUMBER',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('IS_FFPE','Is FFPE','If the sample is from FFPE','STRING',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OCT_EMBEDDED','Oct embedded','Oct embedded','STRING',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('PATHOLOGY_REPORT_FILE_NAME','Pathology Report File Name','Pathology Report File Name','STRING',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('SAMPLE_TYPE','Sample Type','The type of sample (i.e.,normal,primary,met,recurrence).','STRING',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('RETROSPECTIVE_COLLECTION','Tissue Retrospective Collection Indicator','Text indicator for the time frame of tissue procurement,indicating that the tissue was obtained and stored prior to the initiation of the project.','STRING',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('PROSPECTIVE_COLLECTION','Tissue Prospective Collection Indicator','Text indicator for the time frame of tissue procurement,indicating that the tissue was procured in parallel to the project.','STRING',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('FORM_COMPLETION_DATE','Form completion date','Form completion date','STRING',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OTHER_PATIENT_ID','Other Patient ID','Legacy DMP patient identifier (DMPnnnn)','STRING',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('DFS_MONTHS','Disease Free (Months)','Disease free (months) since initial treatment.','NUMBER',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('DFS_STATUS','Disease Free Status','Disease free status since initial treatment.','STRING',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OS_MONTHS','Overall Survival (Months)','Overall survival in months since initial diagonosis.','NUMBER',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OS_STATUS','Overall Survival Status','Overall patient survival status.','STRING',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OTHER_SAMPLE_ID','Other Sample ID','Legacy DMP sample identifier (DMPnnnn)','STRING',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('DAYS_TO_COLLECTION','Days to Sample Collection.','Days to sample collection.','NUMBER',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('IS_FFPE','Is FFPE','If the sample is from FFPE','STRING',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('OCT_EMBEDDED','Oct embedded','Oct embedded','STRING',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('PATHOLOGY_REPORT_FILE_NAME','Pathology Report File Name','Pathology Report File Name','STRING',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('SAMPLE_TYPE','Sample Type','The type of sample (i.e.,normal,primary,met,recurrence).','STRING',0,'1',2);

-- Add genes, genetic entities and structural variants for structural_variant
insert into genetic_entity (id,entity_type) values(21,'GENE');
insert into genetic_entity (id,entity_type) values(22,'GENE');
insert into genetic_entity (id,entity_type) values(23,'GENE');
insert into genetic_entity (id,entity_type) values(24,'GENE');
insert into genetic_entity (id,entity_type) values(25,'GENE');
insert into genetic_entity (id,entity_type) values(26,'GENE');
insert into genetic_entity (id,entity_type) values(27,'GENE');

insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(57670,'KIAA1549',21,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(8031,'NCOA4',22,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(5979,'RET',23,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(27436,'EML4',24,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(238,'ALK',25,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(7113,'TMPRSS2',26,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(2078,'ERG',27,'protein-coding');

insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,1,57670,'ENST00000242365','7','exon',-1,'q13.4',138536968,'KIAA1549-BRAF.K16B10.COSF509_1',673,'ENST00000288602','7','exon',-1,'p13.1',140482957,'KIAA1549-BRAF.K16B10.COSF509_2','GRCh37','no','yes',100000,90000,'KIAA1549-BRAF.K16B10.COSF509','Fusion','Gain-of-Function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,2,57670,'ENST00000242365','7','exon',-1,'q13.4',138536968,'KIAA1549-BRAF.K16B10.COSF509_1',673,'ENST00000288602','7','exon',-1,'p13.1',140482957,'KIAA1549-BRAF.K16B10.COSF509_2','GRCh37','no','yes',100000,90000,'KIAA1549-BRAF.K16B10.COSF509','Fusion','Gain-of-Function','GERMLINE');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,1,8031,'ENST00000344348','10','exon',-1,'q13.4',51582939,'NCOA4-RET.N7R12_1',5979,'ENST00000340058','10','exon',-1,'p13.1',43612031,'NCOA4-RET.N7R12_2','GRCh37','no','yes',100001,80000,'NCOA4-RET.N7R1','Fusion','Gain-of-Function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,1,27436,'ENST00000318522','2','exon',-1,'q13.4',42492091,'EML4-ALK.E6bA20.AB374362_1',238,'ENST00000389048','2','exon',-1,'p13.1',29446394,'EML4-ALK.E6bA20.AB374362_2','GRCh37','no','yes',100002,70000,'EML4-ALK.E6bA20.AB374362','Fusion','Gain-of-Function','GERMLINE');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,2,27436,'ENST00000318522','2','exon',-1,'q13.4',42492091,'EML4-ALK.E6bA20.AB374362_1',238,'ENST00000389048','2','exon',-1,'p13.1',29446394,'EML4-ALK.E6bA20.AB374362_2','GRCh37','no','yes',100002,70000,'EML4-ALK.E6bA20.AB374362-2','Fusion','Gain-of-Function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,1,7113,'ENST00000332149','21','exon',-1,'q13.4',42880007,'TMPRSS2-ERG.T1E2.COSF23.1_1',2078,'ENST00000442448','21','exon',-1,'p13.1',39956869,'TMPRSS2-ERG.T1E2.COSF23.1_2','GRCh37','no','yes',100003,60000,'TMPRSS2-ERG.T1E2.COSF23.1','Fusion','Gain-of-Function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,2,57670,'ENST00000242365','7','exon',-1,'q13.4',138536968,'KIAA1549-BRAF.K16B10.COSF509_1',673,'ENST00000288602','7','exon',-1,'p13.1',140482957,'KIAA1549-BRAF.K16B10.COSF509_2','GRCh37','no','yes',100000,90000,'KIAA1549-BRAF.K16B10.COSF509','Fusion','Gain-of-Function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(13,15,57670,'ENST00000242365','7','exon',-1,'q13.4',138536968,'KIAA1549-BRAF.K16B10.COSF509_1',673,'ENST00000288602','7','exon',-1,'p13.1',140482957,'KIAA1549-BRAF.K16B10.COSF509_2','GRCh37','no','yes',100000,90000,'KIAA1549-BRAF.K16B10.COSF509','Fusion','Gain-of-Function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(13,15,8031,'ENST00000344348','10','exon',-1,'q13.4',51582939,'NCOA4-RET.N7R12_1',5979,'ENST00000340058','10','exon',-1,'p13.1',43612031,'NCOA4-RET.N7R12_2','GRCh37','no','yes',100001,80000,'NCOA4-RET.N7R1-2','Fusion','Gain-of-Function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(13,15,7113,'ENST00000332149','21','exon',-1,'q13.4',42880007,'TMPRSS2-ERG.T1E2.COSF23.1_1',2078,'ENST00000442448','21','exon',-1,'p13.1',39956869,'TMPRSS2-ERG.T1E2.COSF23.1_2','GRCh37','no','yes',100003,60000,'TMPRSS2-ERG.T1E2.COSF23.1','Fusion','Gain-of-Function','GERMLINE');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(13,15,8031,'ENST00000344348','10','exon',-1,'q13.4',51582939,'NCOA4-NULL',NULL,'ENST00000340058_NULL','10','exon',-1,'p13.1',43612031,'NCOA4-NULL','GRCh37','no','yes',100001,80000,'NCOA4-NULL','Fusion','Gain-of-Function','SOMATIC');

insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation)
values (1,7,1, 'Putative_Passenger', 'Pathogenic', 'Tier 1', 'Potentially Actionable');

insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation)
values (3,7,1, 'Putative_Driver', 'Pathogenic', 'Class 2', 'Highly Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation)
values (5,7,2, 'Putative_Driver', 'Pathogenic', 'Class 3', 'Highly Actionable');

insert into mut_sig (cancer_study_id,entrez_gene_id,rank,numbasescovered,nummutations,p_value,q_value) values (1,207,1,998421,17,0.00000315,0.00233);
insert into mut_sig (cancer_study_id,entrez_gene_id,rank,numbasescovered,nummutations,p_value,q_value) values (1,208,2,3200341,351,0.000000012,0.00000000000212);

insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (2,1,'-0.4674,-0.6270,-1.2266,-1.2479,-1.2262,0.6962,-0.3338,-0.1264,0.7559,-1.1267,-0.5893,-1.1546,-1.0027,-1.3157,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (2,2,'1.4146,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1146,0.3498,0.0349,0.4927,-0.8665,-0.4754,-0.7221,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (3,2,'-0.8097,0.7360,-1.0225,-0.8922,0.7247,0.3537,1.2702,-0.1419,');

insert into cna_event (cna_event_id,entrez_gene_id,alteration) values (1,207,-2);
insert into cna_event (cna_event_id,entrez_gene_id,alteration) values (2,208,2);
insert into cna_event (cna_event_id,entrez_gene_id,alteration) values (3,207,2);

insert into sample_cna_event (cna_event_id,sample_id,genetic_profile_id, annotation_json) values (1,1,2, '{"columnName":{"fieldName":"fieldValue"}}');
insert into sample_cna_event (cna_event_id,sample_id,genetic_profile_id, annotation_json) values (2,1,2, '{"columnName":{"fieldName":"fieldValue"}}');
insert into sample_cna_event (cna_event_id,sample_id,genetic_profile_id, annotation_json) values (3,2,2, '{"columnName":{"fieldName":"fieldValue"}}');

insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (1,2,1, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (3,2,2, 'Putative_Passenger', 'Pathogenic', 'Tier 2', 'Potentially Actionable');

insert into gistic (gistic_roi_id,cancer_study_id,chromosome,cytoband,wide_peak_start,wide_peak_end,q_value,amp) values (1,1,1,'1q32.32',123,136,0.0208839997649193,0);
insert into gistic (gistic_roi_id,cancer_study_id,chromosome,cytoband,wide_peak_start,wide_peak_end,q_value,amp) values (2,1,2,'2q30.32',324234,324280,0.000323799991747364,1);
insert into gistic (gistic_roi_id,cancer_study_id,chromosome,cytoband,wide_peak_start,wide_peak_end,q_value,amp) values (3,2,1,'1q3.32',123,136,0.000000129710002738648,0);

insert into gistic_to_gene (gistic_roi_id,entrez_gene_id) values (1,207);
insert into gistic_to_gene (gistic_roi_id,entrez_gene_id) values (1,208);
insert into gistic_to_gene (gistic_roi_id,entrez_gene_id) values (2,207);
insert into gistic_to_gene (gistic_roi_id,entrez_gene_id) values (3,208);

insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (1,1,123,NULL,'STATUS');
insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (2,1,233,345,'SPECIMEN');
insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (3,2,213,445,'TREATMENT');
insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (4,2,211,441,'SEQENCING');
insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (5,2,313,543,'TREATMENT');

insert into clinical_event_data (clinical_event_id,key,value) values (1,'STATUS','radiographic_progression');
insert into clinical_event_data (clinical_event_id,key,value) values (1,'SAMPLE_ID','TCGA-A1-A0SB-01');
insert into clinical_event_data (clinical_event_id,key,value) values (2,'SURGERY','OA II Initial');
insert into clinical_event_data (clinical_event_id,key,value) values (2,'SAMPLE_ID','TCGA-A1-A0SB-01');
insert into clinical_event_data (clinical_event_id,key,value) values (3,'EVENT_TYPE_DETAILED','AA III Recurrence1');
insert into clinical_event_data (clinical_event_id,key,value) values (3,'AGENT','Madeupanib');
insert into clinical_event_data (clinical_event_id,key,value) values (3,'AGENT_TARGET','Directly to forehead, Elbow');
insert into clinical_event_data (clinical_event_id,key,value) values (3,'SAMPLE_ID','TCGA-A1-A0SD-01');
insert into clinical_event_data (clinical_event_id,key,value) values (4,'SAMPLE_ID','TCGA-A1-A0SD-01');
insert into clinical_event_data (clinical_event_id,key,value) values (5,'EVENT_TYPE_DETAILED','AA III Recurrence1');
insert into clinical_event_data (clinical_event_id,key,value) values (5,'AGENT','abc');
insert into clinical_event_data (clinical_event_id,key,value) values (5,'AGENT_TARGET','Left arm, Ankle');

insert into geneset (id,genetic_entity_id,external_id,name,description,ref_link) values (1,17,'MORF_ATRX','MORF ATRX name','Morf description','https://morf_link');
insert into geneset (id,genetic_entity_id,external_id,name,description,ref_link) values (2,18,'HINATA_NFKB_MATRIX','HINATA NFKB MATRIX name','Hinata description','https://hinata_link');

insert into geneset_gene (geneset_id,entrez_gene_id) values (1,207);
insert into geneset_gene (geneset_id,entrez_gene_id) values (1,208);
insert into geneset_gene (geneset_id,entrez_gene_id) values (1,10000);
insert into geneset_gene (geneset_id,entrez_gene_id) values (2,369);
insert into geneset_gene (geneset_id,entrez_gene_id) values (2,472);

insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (9,17,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (9,18,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');


-- Root node ->  sub node A -> parent node 1 -> MORF_ATRX
--    "              "             "         -> HINATA_NFKB_MATRIX
--    "              "      -> parent node 2 -> HINATA_NFKB_MATRIX
-- Root node ->  sub node B -> x (dead branch)
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (1,'Root node',NULL);
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (2,'Sub node A',1);
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (3,'Sub node B',1);
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (4,'Parent node 1',2);
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (5,'Parent node 2',2);

insert into geneset_hierarchy_leaf (node_id,geneset_id) values (4,1);
insert into geneset_hierarchy_leaf (node_id,geneset_id) values (4,2);
insert into geneset_hierarchy_leaf (node_id,geneset_id) values (5,2);

insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (6, 'AKT1 truncating', 207, 54, 64);
insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (6, NULL, 207, 21, 22);
insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (6, 'ARAF G1513 missense', 369, 1, 2);
insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (6, 'ARAF G1514 missense', 369, 4, 7);
insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (8, 'NOC2L truncating', 26155, 1, 3);

insert into users (email, name, enabled) values ('mockemail@email.com', 'MOCK USER', 1);
insert into users (email, name, enabled) values ('mockemail2@email.com', 'MOCK USER 2', 1);
insert into users (email, name, enabled) values ('mockemail3@email.com', 'MOCK USER 3', 1);
insert into users (email, name, enabled) values ('mockemail4@email.com', 'MOCK USER 4', 1);

insert into data_access_tokens (token, username, expiration, creation) values ('6c9a641e-9719-4b09-974c-f17e089b37e8', 'mockemail@email.com', '2018-11-12 11:11:15+00:00', '2018-10-12 11:11:15+00:00');
insert into data_access_tokens (token, username, expiration, creation) values ('6c9a641e-9719-fake-data-f17e089b37e8', 'mockemail2@email.com', '2018-5-14 11:11:15+00:00', '2018-4-14 11:11:15+00:00');
insert into data_access_tokens (token, username, expiration, creation) values ('12345678-119e-4bC9-9a4c-f123kl9b37e8', 'mockemail3@email.com', '2017-1-12 11:11:15+00:00', '2016-12-12 11:11:15+00:00');
insert into data_access_tokens (token, username, expiration, creation) values ('6c9a641e-9719-4b09-974c-4rb1tr4ry5tr', 'mockemail3@email.com', '2017-10-9 11:11:15+00:00', '2017-9-9 11:11:15+00:00');
insert into data_access_tokens (token, username, expiration, creation) values ('1337rand-ki1n-4bna-974c-s4sk3n4rut0l', 'mockemail3@email.com', '2018-8-25 11:11:15+00:00', '2018-7-25 11:11:15+00:00');
insert into data_access_tokens (token, username, expiration, creation) values ('12445678-119e-4bC9-9a4c-f124kl9b47e8', 'mockemail4@email.com', '2017-1-12 11:11:15+00:00', '2016-12-12 11:11:15+00:00');
insert into data_access_tokens (token, username, expiration, creation) values ('6cokl41e-9719-4b09-974c-4rb1tr4ry5tr', 'mockemail4@email.com', '2017-10-9 11:11:15+00:00', '2017-9-9 11:11:15+00:00');
insert into data_access_tokens (token, username, expiration, creation) values ('1447rand-ki1n-4bna-974c-s4sk4n4rut0l', 'mockemail4@email.com', '2018-8-25 11:11:15+00:00', '2018-7-25 11:11:15+00:00');

-- treatment test data
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (1,19,'NAME','Tanespimycin');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (2,19,'DESCRIPTION','Hsp90 inhibitor');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (3,19,'URL','https://en.wikipedia.org/wiki/Tanespimycin');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (4,20,'NAME','Larotrectinib');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (5,20,'DESCRIPTION','TrkA/B/C inhibitor');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (6,20,'URL','https://en.wikipedia.org/wiki/Larotrectinib');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (11,19,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (11,20,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');

-- allele specific copy number data
insert into allele_specific_copy_number (mutation_event_id, genetic_profile_id, sample_id, ascn_integer_copy_number, ascn_method, ccf_expected_copies_upper, ccf_expected_copies, clonal, minor_copy_number, expected_alt_copies, total_copy_number) values (2040, 6, 1, 3, 'FACETS', 1.25, 1.75, 'CLONAL', 2, 1, 4);
insert into allele_specific_copy_number (mutation_event_id, genetic_profile_id, sample_id, ascn_integer_copy_number, ascn_method, ccf_expected_copies_upper, ccf_expected_copies, clonal, minor_copy_number, expected_alt_copies, total_copy_number) values (2038, 6, 6, 1, 'FACETS', 1.25, 1.75, 'SUBCLONAL', 1, 1, 2);
-- generic assay test data
-- mutational signature test data
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (7,28,'name','mean_1');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (8,28,'description','description of mean_1');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (9,29,'name','mean_2');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (10,29,'description','description of mean_2');

insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (12,28,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (12,29,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');

insert into resource_definition (resource_id, display_name, description, resource_type, open_by_default, priority, cancer_study_id) values ('HE', 'H&E Slide', 'H&E Slide', 'SAMPLE', 1, 1, 1);
insert into resource_definition (resource_id, display_name, description, resource_type, open_by_default, priority, cancer_study_id) values ('IDC_OHIF_V2', 'CT Scan', 'CT Scan', 'PATIENT', 1, 1, 1);
insert into resource_definition (resource_id, display_name, description, resource_type, open_by_default, priority, cancer_study_id) values ('FIGURES', 'Figures', 'Figures', 'STUDY', 1, 1, 2);

insert into resource_sample (internal_id, resource_id, url) values (1, 'HE', 'https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg');
insert into resource_sample (internal_id, resource_id, url) values (2, 'HE', 'https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg');
insert into resource_sample (internal_id, resource_id, url) values (3, 'HE', 'https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg');
insert into resource_sample (internal_id, resource_id, url) values (4, 'HE', 'https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg');
insert into resource_sample (internal_id, resource_id, url) values (5, 'HE', 'https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg');
insert into resource_sample (internal_id, resource_id, url) values (6, 'HE', 'https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg');
insert into resource_sample (internal_id, resource_id, url) values (18, 'HE', 'https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg');

insert into resource_patient (internal_id, resource_id, url) values (1, 'IDC_OHIF_V2', 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/SADDLE_PE.JPG/721px-SADDLE_PE.JPG');
insert into resource_patient (internal_id, resource_id, url) values (2, 'IDC_OHIF_V2', 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/SADDLE_PE.JPG/721px-SADDLE_PE.JPG');
insert into resource_patient (internal_id, resource_id, url) values (3, 'IDC_OHIF_V2', 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/SADDLE_PE.JPG/721px-SADDLE_PE.JPG');
insert into resource_patient (internal_id, resource_id, url) values (4, 'IDC_OHIF_V2', 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/SADDLE_PE.JPG/721px-SADDLE_PE.JPG');
insert into resource_patient (internal_id, resource_id, url) values (5, 'IDC_OHIF_V2', 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/SADDLE_PE.JPG/721px-SADDLE_PE.JPG');
insert into resource_patient (internal_id, resource_id, url) values (6, 'IDC_OHIF_V2', 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/SADDLE_PE.JPG/721px-SADDLE_PE.JPG');

insert into resource_study (internal_id, resource_id, url) values (2, 'FIGURES', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Tumor_Mesothelioma2_legend.jpg/220px-Tumor_Mesothelioma2_legend.jpg');