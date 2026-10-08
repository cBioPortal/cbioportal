insert into type_of_cancer (type_of_cancer_id,name,dedicated_color,short_name,parent) values ('brca','breast invasive carcinoma','hotpink','breast','tissue');
insert into type_of_cancer (type_of_cancer_id,name,dedicated_color,short_name,parent) values ('acc','adrenocortical carcinoma','purple','acc','adrenal_gland');

insert into `reference_genome` values (1, 'human', 'hg19', 'grch37', null, 'http://hgdownload.cse.ucsc.edu/goldenpath/hg19/bigzips', '2009-02-01 00:00:00');
insert into `reference_genome` values (2, 'human', 'hg38', 'grch38', null, 'http://hgdownload.cse.ucsc.edu/goldenpath/hg38/bigzips', '2013-12-01 00:00:00');

insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public,pmid,citation,groups,status,import_date,reference_genome_id) values(1,'study_tcga_pub','brca','breast invasive carcinoma (tcga, nature 2012)','<a href=\"http://cancergenome.nih.gov/\">the cancer genome atlas (tcga)</a> breast invasive carcinoma project. 825 cases.<br><i>nature 2012.</i> <a href=\"http://tcga-data.nci.nih.gov/tcga/\">raw data via the tcga data portal</a>.',1,'23000897,26451490','tcga, nature 2012, ...','su2c-pi3k;public;gdac',0,'2011-12-18 13:17:17+00:00',1);
insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public,pmid,citation,groups,status,import_date,reference_genome_id) values(2,'acc_tcga','acc','adrenocortical carcinoma (tcga, provisional)','tcga adrenocortical carcinoma; raw data at the <a href="https://tcga-data.nci.nih.gov/">nci</a>.',1,'23000897','tcga, nature 2012','su2c-pi3k;public;gdac',0,'2013-10-12 11:11:15+00:00',1);
insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public,pmid,citation,groups,status,import_date,reference_genome_id) values(3,'study_genie_pub','mixed','GENIE Cohort public','GENIE public','1','','','GENIEPUB;GENIE',0,'2024-04-03 20:12:24.000000',1);

insert into cancer_study_tags (cancer_study_id,tags) values(1,'{"analyst": {"name": "jack", "email": "jack@something.com"}, "load id": 35}');
insert into cancer_study_tags (cancer_study_id,tags) values(2,'{"load id": 36}');

insert into genetic_entity (id,entity_type,stable_id) values (1,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (2,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (3,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (4,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (5,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (6,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (7,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (8,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (9,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (10,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (11,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (12,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (13,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (14,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (15,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (16,'gene', null);
insert into genetic_entity (id,entity_type,stable_id) values (17,'geneset', null);
insert into genetic_entity (id,entity_type,stable_id) values (18,'geneset', null);
insert into genetic_entity (id,entity_type,stable_id) values (19,'generic_assay','17-aag');
insert into genetic_entity (id,entity_type,stable_id) values (20,'generic_assay','aew541');
-- add genes, genetic entities and structural variants for structural_variant
insert into genetic_entity (id,entity_type) values(21,'gene');
insert into genetic_entity (id,entity_type) values(22,'gene');
insert into genetic_entity (id,entity_type) values(23,'gene');
insert into genetic_entity (id,entity_type) values(24,'gene');
insert into genetic_entity (id,entity_type) values(25,'gene');
insert into genetic_entity (id,entity_type) values(26,'gene');
insert into genetic_entity (id,entity_type) values(27,'gene');
insert into genetic_entity (id,entity_type,stable_id) values (28,'generic_assay','mean_1');
insert into genetic_entity (id,entity_type,stable_id) values (29,'generic_assay','mean_2');
insert into genetic_entity (id,entity_type,stable_id) values (30,'GENERIC_ASSAY','1p_status');
insert into genetic_entity (id,entity_type,stable_id) values (31,'GENERIC_ASSAY','DMETS_DX_ADRENAL');
insert into genetic_entity (id,entity_type,stable_id) values (32,'GENERIC_ASSAY','2p_status');
insert into genetic_entity (id,entity_type,stable_id) values (33,'GENERIC_ASSAY','9p_status');
insert into genetic_entity (id,entity_type,stable_id) values (34,'GENERIC_ASSAY','10p_status');
insert into genetic_entity (id,entity_type,stable_id) values (35,'GENERIC_ASSAY','sample_dup_status');

-- hugo_gene_symbol should be UPPERCASE
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
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(57670,'kiaa1549',21,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(8031,'ncoa4',22,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(5979,'ret',23,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(27436,'eml4',24,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(238,'alk',25,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(7113,'tmprss2',26,'protein-coding');
insert into gene (entrez_gene_id,hugo_gene_symbol,genetic_entity_id,type) values(2078,'erg',27,'protein-coding');

insert into gene_alias (entrez_gene_id,gene_alias) values (207,'akt alias');
insert into gene_alias (entrez_gene_id,gene_alias) values (207,'akt alias2');
insert into gene_alias (entrez_gene_id,gene_alias) values (675,'BRCA1 alias');

insert into reference_genome_gene (entrez_gene_id,cytoband,start,end,chr,reference_genome_id) values(207,'14q32.33',105235686,105262088,14,1);
insert into reference_genome_gene (entrez_gene_id,cytoband,start,end,chr,reference_genome_id) values(207,'14q32.33',104769349,104795751,14,2);
insert into reference_genome_gene (entrez_gene_id,cytoband,start,end,chr,reference_genome_id) values(208,'19q13.2', 40736224, 40791443,19,1);
insert into reference_genome_gene (entrez_gene_id,cytoband,start,end,chr,reference_genome_id) values(208,'19q13.2', 40230317, 40285536,19,2);

insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (2,'study_tcga_pub_gistic',1,'COPY_NUMBER_ALTERATION','discrete','putative copy-number alterations from gistic','putative copy-number from gistic 2.0. values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (3,'study_tcga_pub_mrna',1,'mrna_expression','z-score','mrna expression (microarray)','expression levels (agilent microarray).',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (10,'study_tcga_pub_m_na',1,'mrna_expression','z-score','mrna expression (microarray)','expression levels (agilent microarray).',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (4,'study_tcga_pub_log2cna',1,'COPY_NUMBER_ALTERATION','log2-value','log2 copy-number values','log2 copy-number valuesfor each gene (from affymetrix snp6).',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (5,'study_tcga_pub_methylation_hm27',1,'methylation','continuous','methylation (hm27)','methylation beta-values (hm27 platform). for genes with multiple methylation probes, the probe least correlated with expression is selected.',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (6,'study_tcga_pub_mutations',1,'MUTATION_EXTENDED','maf','mutations','mutation data from whole exome sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (7,'study_tcga_pub_sv',1,'structural_variant','sv','structural variants','structural variants detected by illumina hiseq sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (8,'acc_tcga_mutations',2,'MUTATION_EXTENDED','maf','mutations','mutation data from whole exome sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (13,'acc_tcga_sv',2,'structural_variant','sv','structural variants','structural variants detected by illumina hiseq sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (9,'study_tcga_pub_gsva_scores',1,'geneset_score','gsva-score','gsva scores','gsva scores for oncogenic signature gene sets from msigdb calculated with gsva version 1.22.4, r version 3.3.2.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab,generic_assay_type) values (11,'study_tcga_pub_treatment_ic50',1,'generic_assay','ic50','treatment ic50 values','treatment response ic50 values',1,'treatment_response');
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab,generic_assay_type) values (12,'study_tcga_pub_mutational_signature',1,'generic_assay','limit-value','mutational_signature values','mutational_signature values',1,'mutational_signature');
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (14,'study_tcga_pub_cna',1,'COPY_NUMBER_ALTERATION','discrete','Copy-number alterations','Copy number alterations (amplifications and deletions) from targeted sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (15,'study_tcga_pub_rppa',1,'PROTEIN_LEVEL','log2-value','Protein expression (RPPA)','Protein expression measured by reverse-phase protein array',0);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (16,'genie_public_mutations',3,'MUTATION_EXTENDED','maf','Somatic mutations','Somatic mutations from targeted sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab) values (17,'genie_public_cna',3,'COPY_NUMBER_ALTERATION','discrete','Copy-number alterations','Copy number alterations (amplifications and deletions) from targeted sequencing.',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab,generic_assay_type) values (18,'acc_tcga_armlevel_cna',2,'GENERIC_ASSAY','CATEGORICAL','Putative arm-level copy-number from GISTIC','Putative arm-level copy-number from GISTIC 2.0.',1,'ARMLEVEL_CNA');
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab,generic_assay_type,patient_level) values (19,'study_genie_pub_distant_mets',3,'GENERIC_ASSAY','CATEGORICAL','Patient-level information on distant mets','Patient-level information on distant mets from GENIE BPC NSCLC Public',1,'DISTANT_METS',1);
insert into genetic_profile (genetic_profile_id,stable_id,cancer_study_id,genetic_alteration_type,datatype,name,description,show_profile_in_analysis_tab,generic_assay_type) values (20,'study_genie_pub_sample_level_assay',3,'GENERIC_ASSAY','CATEGORICAL','Sample-level generic assay duplicates','Sample-level generic assay duplicates for sample counting coverage',1,'SAMPLE_LEVEL_ASSAY');

insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (2,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (3,'2,3,6,8,9,10,12,13,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (4,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (5,'2,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (11,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (12,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (14,'1,2,3,4,5,6,7,8,9,10,11,12,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values (15,'1,2,3,4,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values(10,'1,2,3,4,5,6,7,8,9,10,11,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values(18,'15,16,17,19,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values(19,'301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,316,317,318,319,320,321,322,323,324,325,326,327,');
insert into genetic_profile_samples (genetic_profile_id,ordered_sample_list) values(20,'321,322,325,326,');

insert into patient (internal_id,stable_id,cancer_study_id) values (1,'tcga-a1-a0sb',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (2,'tcga-a1-a0sd',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (3,'tcga-a1-a0se',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (4,'tcga-a1-a0sf',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (5,'tcga-a1-a0sg',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (6,'tcga-a1-a0sh',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (7,'tcga-a1-a0si',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (8,'tcga-a1-a0sj',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (9,'tcga-a1-a0sk',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (10,'tcga-a1-a0sm',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (11,'tcga-a1-a0sn',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (12,'tcga-a1-a0so',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (13,'tcga-a1-a0sp',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (14,'tcga-a1-a0sq',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (15,'tcga-a1-b0so',2);
insert into patient (internal_id,stable_id,cancer_study_id) values (16,'tcga-a1-b0sp',2);
insert into patient (internal_id,stable_id,cancer_study_id) values (17,'tcga-a1-b0sq',2);
insert into patient (internal_id,stable_id,cancer_study_id) values (18,'tcga-a1-a0sb',2);
insert into patient (internal_id,stable_id,cancer_study_id) values (301,'GENIE-TEST-301',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (302,'GENIE-TEST-302',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (303,'GENIE-TEST-303',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (304,'GENIE-TEST-304',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (305,'GENIE-TEST-305',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (306,'GENIE-TEST-306',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (307,'GENIE-TEST-307',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (308,'GENIE-TEST-308',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (309,'GENIE-TEST-309',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (310,'GENIE-TEST-310',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (311,'GENIE-TEST-311',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (312,'GENIE-TEST-312',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (313,'GENIE-TEST-313',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (314,'GENIE-TEST-314',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (315,'GENIE-TEST-315',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (316,'GENIE-TEST-316',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (317,'GENIE-TEST-317',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (318,'GENIE-TEST-318',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (319,'GENIE-TEST-319',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (320,'GENIE-TEST-320',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (321,'GENIE-TEST-321',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (322,'GENIE-TEST-322',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (323,'GENIE-TEST-323',3);
insert into patient (internal_id,stable_id,cancer_study_id) values (324,'GENIE-TEST-324',3);

insert into sample (internal_id,stable_id,sample_type,patient_id) values (1,'tcga-a1-a0sb-01','primary solid tumor',1);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (18,'tcga-a1-a0sb-02','primary solid tumor',1);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (2,'tcga-a1-a0sd-01','primary solid tumor',2);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (3,'tcga-a1-a0se-01','primary solid tumor',3);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (4,'tcga-a1-a0sf-01','primary solid tumor',4);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (5,'tcga-a1-a0sg-01','primary solid tumor',5);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (6,'tcga-a1-a0sh-01','primary solid tumor',6);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (7,'tcga-a1-a0si-01','primary solid tumor',7);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (8,'tcga-a1-a0sj-01','primary solid tumor',8);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (9,'tcga-a1-a0sk-01','primary solid tumor',9);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (10,'tcga-a1-a0sm-01','primary solid tumor',10);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (11,'tcga-a1-a0sn-01','primary solid tumor',11);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (12,'tcga-a1-a0so-01','primary solid tumor',12);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (13,'tcga-a1-a0sp-01','primary solid tumor',13);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (14,'tcga-a1-a0sq-01','primary solid tumor',14);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (15,'tcga-a1-b0so-01','primary solid tumor',15);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (16,'tcga-a1-b0sp-01','primary solid tumor',16);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (17,'tcga-a1-b0sq-01','primary solid tumor',17);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (19,'tcga-a1-a0sb-01','primary solid tumor',18);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (301,'GENIE-TEST-301-01','primary solid tumor',301);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (302,'GENIE-TEST-302-01','primary solid tumor',302);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (303,'GENIE-TEST-303-01','primary solid tumor',303);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (304,'GENIE-TEST-304-01','primary solid tumor',304);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (305,'GENIE-TEST-305-01','primary solid tumor',305);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (306,'GENIE-TEST-306-01','primary solid tumor',306);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (307,'GENIE-TEST-307-01','primary solid tumor',307);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (308,'GENIE-TEST-308-01','primary solid tumor',308);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (309,'GENIE-TEST-309-01','primary solid tumor',309);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (310,'GENIE-TEST-310-01','primary solid tumor',310);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (311,'GENIE-TEST-311-01','primary solid tumor',311);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (312,'GENIE-TEST-312-01','primary solid tumor',312);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (313,'GENIE-TEST-313-01','primary solid tumor',313);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (314,'GENIE-TEST-314-01','primary solid tumor',314);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (315,'GENIE-TEST-315-01','primary solid tumor',315);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (316,'GENIE-TEST-316-01','primary solid tumor',316);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (317,'GENIE-TEST-317-01','primary solid tumor',317);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (318,'GENIE-TEST-318-01','primary solid tumor',318);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (319,'GENIE-TEST-319-01','primary solid tumor',319);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (320,'GENIE-TEST-320-01','primary solid tumor',320);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (321,'GENIE-TEST-321-01','primary solid tumor',321);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (322,'GENIE-TEST-322-01','primary solid tumor',322);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (323,'GENIE-TEST-323-01','primary solid tumor',323);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (324,'GENIE-TEST-324-01','primary solid tumor',324);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (325,'GENIE-TEST-321-02','primary solid tumor',321);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (326,'GENIE-TEST-322-02','primary solid tumor',322);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (327,'GENIE-TEST-323-02','primary solid tumor',323);

insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2038,672,'17',41244748,41244748,'g','a','q934*','nonsense_mutation','37','+','snp','rs80357223','unknown','nm_007294','c.(2800-2802)cag>tag','p38398',934,934,1,'BRCA1 truncating');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (22604,672,'17',41258504,41258504,'a','c','c61g','missense_mutation','37','+','snp','rs28897672','bycluster','nm_007294','c.(181-183)tgt>ggt','p38398',61,61,1,'BRCA1 c61 missense');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2039,672,'17',41276033,41276033,'c','t','c27_splice','splice_site','37','+','snp','rs80358010','bycluster','nm_007294','c.e2+1','na',-1,-1,1,'BRCA1 truncating');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2040,207,'17',41244748,41244748,'g','a','q934*','nonsense_mutation','37','+','snp','rs80357223','unknown','nm_007294','c.(2800-2802)cag>tag','p38398',934,934,1,'BRCA1 truncating');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2041,207,'17',41258504,41258504,'a','c','c61g','missense_mutation','37','+','snp','rs28897672','bycluster','nm_007294','c.(181-183)tgt>ggt','p38398',61,61,1,'BRCA1 c61 missense');
insert into mutation_event (mutation_event_id,entrez_gene_id,chr,start_position,end_position,reference_allele,tumor_seq_allele,protein_change,mutation_type,ncbi_build,strand,variant_type,db_snp_rs,db_snp_val_status,refseq_mrna_id,codon_change,uniprot_accession,protein_pos_start,protein_pos_end,canonical_transcript,keyword) values (2042,208,'17',41276033,41276033,'c','t','c27_splice','splice_site','37','+','snp','rs80358010','bycluster','nm_007294','c.e2+1','na',-1,-1,1,'BRCA1 truncating');

insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2038,6,6, 'putative_driver', 'pathogenic', 'tier 1', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (22604,6,6, 'putative_passenger', 'pathogenic', 'tier 2', 'potentially actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2039,6,12, 'putative_passenger', 'pathogenic', 'tier 1', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2038,6,7, 'putative_driver', 'pathogenic', 'tier 2', 'potentially actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2039,6,13, 'putative_driver', 'pathogenic', 'tier 1', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2040,6,1, 'putative_driver', 'pathogenic', 'tier 1', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2041,6,2, 'putative_passenger', 'pathogenic', 'tier 2', 'potentially actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2042,6,3, 'putative_passenger', 'pathogenic', 'tier 1', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (2042,8,15, 'putative_driver', 'pathogenic', 'tier 1', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (1,7,1, 'putative_passenger', 'pathogenic', 'tier 1', 'potentially actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (3,7,1, 'putative_driver', 'pathogenic', 'class 2', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (5,7,2, 'putative_driver', 'pathogenic', 'class 3', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (1,2,1, 'putative_driver', 'pathogenic', 'tier 1', 'highly actionable');
insert into alteration_driver_annotation (alteration_event_id,genetic_profile_id,sample_id, driver_filter, driver_filter_annotation, driver_tiers_filter, driver_tiers_filter_annotation) values (3,2,2, 'putative_passenger', 'pathogenic', 'tier 2', 'potentially actionable');

insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2038,6,6,672,'genome.wustl.edu','illuminagaiix','GERMLINE','unknown','g','a','tcga-a1-a0sh-10a-03d-a099-09','g','a','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',1,0,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (22604,6,6,672,'genome.wustl.edu','illuminagaiix','GERMLINE','unknown','a','c','tcga-a1-a0sh-10a-03d-a099-09','a','c','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',-1,-1,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2039,6,12,672,'genome.wustl.edu','illuminagaiix','GERMLINE','unknown','t','t','tcga-a1-a0so-10a-03d-a099-09','t','t','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',-1,-1,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2038,6,7,672,'genome.wustl.edu','illuminagaiix','na','unknown','g','a','tcga-a1-a0sh-10a-03d-a099-09','g','a','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',-1,-1,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2039,6,13,672,'genome.wustl.edu','illuminagaiix','GERMLINE','unknown','t','t','tcga-a1-a0so-10a-03d-a099-09','t','t','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',-1,-1,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2040,6,1,207,'genome.wustl.edu','illuminagaiix','GERMLINE','unknown','g','a','tcga-a1-a0sh-10a-03d-a099-09','g','a','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',-1,-1,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2041,6,2,207,'genome.wustl.edu','illuminagaiix','GERMLINE','unknown','a','c','tcga-a1-a0sh-10a-03d-a099-09','a','c','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',0,-1,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2042,6,3,208,'genome.wustl.edu','illuminagaiix','GERMLINE','unknown','t','t','tcga-a1-a0so-10a-03d-a099-09','t','t','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',-1,-1,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');
insert into mutation (mutation_event_id,genetic_profile_id,sample_id,entrez_gene_id,center,sequencer,mutation_status,validation_status,tumor_seq_allele1,tumor_seq_allele2,matched_norm_sample_barcode,match_norm_seq_allele1,match_norm_seq_allele2,tumor_validation_allele1,tumor_validation_allele2,match_norm_validation_allele1,match_norm_validation_allele2,verification_status,sequencing_phase,sequence_source,validation_method,score,bam_file,tumor_alt_count,tumor_ref_count,normal_alt_count,normal_ref_count,amino_acid_change,annotation_json) values (2042,8,15,208,'genome.wustl.edu','illuminagaiix','GERMLINE','unknown','t','t','tcga-a1-a0so-10a-03d-a099-09','t','t','na','na','na','na','unknown','phase_iv','capture','na','1','dbgap',-1,-1,-1,-1,'cyclases/protein','{"zygosity":{"status": "heterozygous"}}');

insert into gene_panel (internal_id,stable_id,description) values (1,'testpanel1','a test panel consisting of a few genes');
insert into gene_panel (internal_id,stable_id,description) values (2,'testpanel2','another test panel consisting of a few genes');
insert into gene_panel (internal_id,stable_id,description) values (3,'testpanel3','third test panel consisting of a few genes');
insert into gene_panel (internal_id,stable_id,description) values (4,'testpanel4','fourth test panel consisting of a few genes');

insert into gene_panel_list (internal_id,gene_id) values (1,207);
insert into gene_panel_list (internal_id,gene_id) values (1,369);
insert into gene_panel_list (internal_id,gene_id) values (1,672);
insert into gene_panel_list (internal_id,gene_id) values (2,207);
insert into gene_panel_list (internal_id,gene_id) values (2,208);
insert into gene_panel_list (internal_id,gene_id) values (2,4893);
insert into gene_panel_list (internal_id,gene_id) values (3,472);
insert into gene_panel_list (internal_id,gene_id) values (3,673);
insert into gene_panel_list (internal_id,gene_id) values (3,675);
insert into gene_panel_list (internal_id,gene_id) values (4,208);
insert into gene_panel_list (internal_id,gene_id) values (4,673);
insert into gene_panel_list (internal_id,gene_id) values (4,675);

insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,2,1);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,3,1);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,6,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (11,6,1);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (1,7,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,2,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,3,1);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,4,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,5,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (2,6,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (3,2,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (3,3,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (3,4,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (3,6,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (4,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (4,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (5,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (5,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (6,2,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (6,3,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (6,4,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (6,6,2);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (7,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (7,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (7,6,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (8,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (8,3,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (8,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (8,6,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (9,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (9,3,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (9,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (9,6,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (10,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (10,3,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (10,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (10,6,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (11,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (11,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (12,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (12,3,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (12,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (12,6,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (13,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (13,3,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (13,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (13,6,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (14,2,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (14,4,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (15,13,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (301,16,3);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (301,17,3);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (302,16,3);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (303,17,3);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (304,17,4);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (305,16,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (306,16,null);
insert into sample_profile (sample_id,genetic_profile_id,panel_id) values (306,17,null);

insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (1,'study_tcga_pub_all','other',1,'all tumors','all tumor samples');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (2,'study_tcga_pub_acgh','other',1,'tumors acgh','all tumors with acgh data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (3,'study_tcga_pub_cnaseq','other',1,'tumors with sequencing and acgh data','all tumor samples that have cna and sequencing data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (4,'study_tcga_pub_complete','other',1,'complete samples (mutations, copy-number, expression)','samples with complete data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (5,'study_tcga_pub_log2cna','other',1,'tumors log2 copy-number','all tumors with log2 copy-number data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (6,'study_tcga_pub_methylation_hm27','all_cases_with_mutation_data',1,'tumors with methylation data','all samples with methylation (hm27) data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (7,'study_tcga_pub_mrna','other',1,'tumors with mrna data (agilent microarray)','all samples with mrna expression data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (8,'study_tcga_pub_sequenced','other',1,'sequenced tumors','all sequenced samples');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (9,'study_tcga_pub_cna','other',1,'tumor samples with cna data','all tumors with cna data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (10,'study_tcga_pub_rna_seq_v2_mrna','other',1,'tumor samples with mrna data (rna seq v2)','all samples with mrna expression data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (11,'study_tcga_pub_microrna','other',1,'tumors with microrna data (microrna-seq)','all samples with microrna data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (12,'study_tcga_pub_rppa','other',1,'tumor samples with rppa data','tumors with reverse phase protein array (rppa) data for about 200 antibodies');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (13,'study_tcga_pub_3way_complete','other',1,'all complete tumors','all tumor samples that have mrna,cna and sequencing data');
insert into sample_list (list_id,stable_id,category,cancer_study_id,name,description) values (14,'acc_tcga_all','other',2,'all tumors','all tumor samples');

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

insert into clinical_patient (internal_id,attr_id,attr_value) values (1,'retrospective_collection','no');
insert into clinical_patient (internal_id,attr_id,attr_value) values (1,'form_completion_date','2013-12-5');
insert into clinical_patient (internal_id,attr_id,attr_value) values (1,'other_patient_id','286cf147-b7f7-4a05-8e41-7fbd3717ad71');
insert into clinical_patient (internal_id,attr_id,attr_value) values (2,'prospective_collection','yes');
insert into clinical_patient (internal_id,attr_id,attr_value) values (15,'dfs_months','5.72');
insert into clinical_patient (internal_id,attr_id,attr_value) values (15,'dfs_status','1:recurred/progressed');
insert into clinical_patient (internal_id,attr_id,attr_value) values (15,'os_months','12.3');
insert into clinical_patient (internal_id,attr_id,attr_value) values (15,'os_status','0:living');
insert into clinical_patient (internal_id,attr_id,attr_value) values (18,'retrospective_collection','no');
insert into clinical_patient (internal_id,attr_id,attr_value) values (301,'center','msk');
insert into clinical_patient (internal_id,attr_id,attr_value) values (302,'center','msk');
insert into clinical_patient (internal_id,attr_id,attr_value) values (303,'center','msk');
insert into clinical_patient (internal_id,attr_id,attr_value) values (304,'center','dfci');
insert into clinical_patient (internal_id,attr_id,attr_value) values (305,'center','dfci');
insert into clinical_patient (internal_id,attr_id,attr_value) values (306,'center','chop');
insert into clinical_patient (internal_id,attr_id,attr_value) values (307,'center','chop');
insert into clinical_patient (internal_id,attr_id,attr_value) values (308,'center','mda');
insert into clinical_patient (internal_id,attr_id,attr_value) values (309,'center','ohsu');
insert into clinical_patient (internal_id,attr_id,attr_value) values (310,'center','ucsf');
insert into clinical_patient (internal_id,attr_id,attr_value) values (311,'center','NA');
insert into clinical_patient (internal_id,attr_id,attr_value) values (312,'center','');
insert into clinical_patient (internal_id,attr_id,attr_value) values (301,'wsi_slides','3');
insert into clinical_patient (internal_id,attr_id,attr_value) values (301,'dead','True');
insert into clinical_patient (internal_id,attr_id,attr_value) values (302,'dead','false');
insert into clinical_patient (internal_id,attr_id,attr_value) values (303,'dead','TRUE');
insert into clinical_patient (internal_id,attr_id,attr_value) values (304,'dead','False');
insert into clinical_patient (internal_id,attr_id,attr_value) values (305,'dead','FALSE');
insert into clinical_patient (internal_id,attr_id,attr_value) values (306,'dead','true');
insert into clinical_patient (internal_id,attr_id,attr_value) values (307,'dead','Not Released');
insert into clinical_patient (internal_id,attr_id,attr_value) values (308,'dead','Not Collected');
insert into clinical_patient (internal_id,attr_id,attr_value) values (309,'dead','FALSE');
insert into clinical_patient (internal_id,attr_id,attr_value) values (310,'dead','Unknown');
insert into clinical_patient (internal_id,attr_id,attr_value) values (311,'dead','N/A');
insert into clinical_patient (internal_id,attr_id,attr_value) values (312,'dead','');
insert into clinical_patient (internal_id,attr_id,attr_value) values (301,'age','<18');
insert into clinical_patient (internal_id,attr_id,attr_value) values (302,'age','<18');
insert into clinical_patient (internal_id,attr_id,attr_value) values (303,'age','<18');
insert into clinical_patient (internal_id,attr_id,attr_value) values (304,'age','>89');
insert into clinical_patient (internal_id,attr_id,attr_value) values (305,'age','>89');
insert into clinical_patient (internal_id,attr_id,attr_value) values (306,'age','18');
insert into clinical_patient (internal_id,attr_id,attr_value) values (307,'age','22');
insert into clinical_patient (internal_id,attr_id,attr_value) values (308,'age','42');
insert into clinical_patient (internal_id,attr_id,attr_value) values (309,'age','42');
insert into clinical_patient (internal_id,attr_id,attr_value) values (310,'age','66');
insert into clinical_patient (internal_id,attr_id,attr_value) values (311,'age','68');
insert into clinical_patient (internal_id,attr_id,attr_value) values (312,'age','77');
insert into clinical_patient (internal_id,attr_id,attr_value) values (313,'age','78');
insert into clinical_patient (internal_id,attr_id,attr_value) values (314,'age','79');
insert into clinical_patient (internal_id,attr_id,attr_value) values (315,'age','80');
insert into clinical_patient (internal_id,attr_id,attr_value) values (316,'age','80');
insert into clinical_patient (internal_id,attr_id,attr_value) values (317,'age','82');
insert into clinical_patient (internal_id,attr_id,attr_value) values (318,'age','82');
insert into clinical_patient (internal_id,attr_id,attr_value) values (319,'age','89');
insert into clinical_patient (internal_id,attr_id,attr_value) values (320,'age','N/A');
insert into clinical_patient (internal_id,attr_id,attr_value) values (321,'age','');
insert into clinical_patient (internal_id,attr_id,attr_value) values (322,'age','NAN');
insert into clinical_patient (internal_id,attr_id,attr_value) values (324,'age','UNKNOWN');
insert into clinical_patient (internal_id,attr_id,attr_value) values (313,'subtype','Triple Negative');
insert into clinical_patient (internal_id,attr_id,attr_value) values (314,'subtype','Luminal A');
insert into clinical_patient (internal_id,attr_id,attr_value) values (315,'subtype','HER2+');
insert into clinical_patient (internal_id,attr_id,attr_value) values (316,'subtype','NA');
insert into clinical_patient (internal_id,attr_id,attr_value) values (317,'subtype','Luminal B');
insert into clinical_patient (internal_id,attr_id,attr_value) values (318,'subtype','');

insert into clinical_sample (internal_id,attr_id,attr_value) values (1,'other_sample_id','5c631ce8-f96a-4c35-a459-556fc4ab21e1');
insert into clinical_sample (internal_id,attr_id,attr_value) values (1,'days_to_collection','276');
insert into clinical_sample (internal_id,attr_id,attr_value) values (1,'is_ffpe','no');
insert into clinical_sample (internal_id,attr_id,attr_value) values (1,'sample_type','secondary tumor');
insert into clinical_sample (internal_id,attr_id,attr_value) values (2,'oct_embedded','false');
insert into clinical_sample (internal_id,attr_id,attr_value) values (2,'days_to_collection','277');
insert into clinical_sample (internal_id,attr_id,attr_value) values (2,'pathology_report_file_name','tcga-gc-a3bm.f3408556-9259-4700-b9a0-f41e516b420c.pdf');
insert into clinical_sample (internal_id,attr_id,attr_value) values (2,'sample_type','primary tumor');
insert into clinical_sample (internal_id,attr_id,attr_value) values (15,'other_sample_id','91e7f41c-17b3-4724-96ef-d3c207b964e1');
insert into clinical_sample (internal_id,attr_id,attr_value) values (15,'days_to_collection','111');
insert into clinical_sample (internal_id,attr_id,attr_value) values (19,'days_to_collection','111');
insert into clinical_sample (internal_id,attr_id,attr_value) values (301,'mutation_count','11');
insert into clinical_sample (internal_id,attr_id,attr_value) values (302,'mutation_count','6');
insert into clinical_sample (internal_id,attr_id,attr_value) values (303,'mutation_count','4');
insert into clinical_sample (internal_id,attr_id,attr_value) values (304,'mutation_count','4');
insert into clinical_sample (internal_id,attr_id,attr_value) values (305,'mutation_count','2');
insert into clinical_sample (internal_id,attr_id,attr_value) values (306,'mutation_count','2');
insert into clinical_sample (internal_id,attr_id,attr_value) values (307,'mutation_count','2');
insert into clinical_sample (internal_id,attr_id,attr_value) values (308,'mutation_count','2');
insert into clinical_sample (internal_id,attr_id,attr_value) values (309,'mutation_count','1');
insert into clinical_sample (internal_id,attr_id,attr_value) values (310,'mutation_count','1');
insert into clinical_sample (internal_id,attr_id,attr_value) values (311,'mutation_count','NAN');
insert into clinical_sample (internal_id,attr_id,attr_value) values (312,'mutation_count','');
insert into clinical_sample (internal_id,attr_id,attr_value) values (15,'subtype','Luminal A');
insert into clinical_sample (internal_id,attr_id,attr_value) values (16,'subtype','Luminal B');
insert into clinical_sample (internal_id,attr_id,attr_value) values (17,'subtype','HER2+');
insert into clinical_sample (internal_id,attr_id,attr_value) values (19,'subtype','');

insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('retrospective_collection','tissue retrospective collection indicator','text indicator for the time frame of tissue procurement,indicating that the tissue was obtained and stored prior to the initiation of the project.','string',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('prospective_collection','tissue prospective collection indicator','text indicator for the time frame of tissue procurement,indicating that the tissue was procured in parallel to the project.','string',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('form_completion_date','form completion date','form completion date','string',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('other_patient_id','other patient id','legacy dmp patient identifier (dmpnnnn)','string',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('dfs_months','disease free (months)','disease free (months) since initial treatment.','number',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('dfs_status','disease free status','disease free status since initial treatment.','string',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('os_months','overall survival (months)','overall survival in months since initial diagonosis.','number',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('os_status','overall survival status','overall patient survival status.','string',1,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('other_sample_id','other sample id','legacy dmp sample identifier (dmpnnnn)','string',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('days_to_collection','days to sample collection.','days to sample collection.','string',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('is_ffpe','is ffpe','if the sample is from ffpe','string',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('oct_embedded','oct embedded','oct embedded','string',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('pathology_report_file_name','pathology report file name','pathology report file name','string',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('sample_type','sample type','the type of sample (i.e.,normal,primary,met,recurrence).','string',0,'1',1);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('retrospective_collection','tissue retrospective collection indicator','text indicator for the time frame of tissue procurement,indicating that the tissue was obtained and stored prior to the initiation of the project.','string',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('prospective_collection','tissue prospective collection indicator','text indicator for the time frame of tissue procurement,indicating that the tissue was procured in parallel to the project.','string',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('form_completion_date','form completion date','form completion date','string',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('other_patient_id','other patient id','legacy dmp patient identifier (dmpnnnn)','string',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('dfs_months','disease free (months)','disease free (months) since initial treatment.','number',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('dfs_status','disease free status','disease free status since initial treatment.','string',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('os_months','overall survival (months)','overall survival in months since initial diagonosis.','number',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('os_status','overall survival status','overall patient survival status.','string',1,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('other_sample_id','other sample id','legacy dmp sample identifier (dmpnnnn)','string',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('days_to_collection','days to sample collection.','days to sample collection.','string',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('is_ffpe','is ffpe','if the sample is from ffpe','string',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('oct_embedded','oct embedded','oct embedded','string',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('pathology_report_file_name','pathology report file name','pathology report file name','string',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('sample_type','sample type','the type of sample (i.e.,normal,primary,met,recurrence).','string',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('wsi_slides','WSI Slides per Patient','Number of whole slide images available for the patient.','number',1,'1',0);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('mutation_count','mutaiton count','mutation count','number',0,'30',3);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('age','age at metastatic diagnosis (years)','age at metastatic diagnosis (years)','number',1,'3',3);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('center','center','center of sequencing','string',1,'1',3);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('dead','vital status','is this patient known to be deceased','string',1,'1',3);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('subtype','tumor subtype','tumor subtype classification','string',0,'1',2);
insert into clinical_attribute_meta (attr_id,display_name,description,datatype,patient_attribute,priority,cancer_study_id) values ('subtype','tumor subtype','tumor subtype classification','string',1,'1',3);

insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,1,57670,'enst00000242365','7','exon',-1,'q13.4',138536968,'kiaa1549-BRAF.k16b10.cosf509_1',673,'enst00000288602','7','exon',-1,'p13.1',140482957,'kiaa1549-BRAF.k16b10.cosf509_2','grch37','no','yes',100000,90000,'kiaa1549-BRAF.k16b10.cosf509','fusion','gain-of-function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,2,57670,'enst00000242365','7','exon',-1,'q13.4',138536968,'kiaa1549-BRAF.k16b10.cosf509_1',673,'enst00000288602','7','exon',-1,'p13.1',140482957,'kiaa1549-BRAF.k16b10.cosf509_2','grch37','no','yes',100000,90000,'kiaa1549-BRAF.k16b10.cosf509','fusion','gain-of-function','GERMLINE');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,1,8031,'enst00000344348','10','exon',-1,'q13.4',51582939,'ncoa4-ret.n7r12_1',5979,'enst00000340058','10','exon',-1,'p13.1',43612031,'ncoa4-ret.n7r12_2','grch37','no','yes',100001,80000,'ncoa4-ret.n7r1','fusion','gain-of-function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,1,27436,'enst00000318522','2','exon',-1,'q13.4',42492091,'eml4-alk.e6ba20.ab374362_1',238,'enst00000389048','2','exon',-1,'p13.1',29446394,'eml4-alk.e6ba20.ab374362_2','grch37','no','yes',100002,70000,'eml4-alk.e6ba20.ab374362','fusion','gain-of-function','GERMLINE');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,2,27436,'enst00000318522','2','exon',-1,'q13.4',42492091,'eml4-alk.e6ba20.ab374362_1',238,'enst00000389048','2','exon',-1,'p13.1',29446394,'eml4-alk.e6ba20.ab374362_2','grch37','no','yes',100002,70000,'eml4-alk.e6ba20.ab374362-2','fusion','gain-of-function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,1,7113,'enst00000332149','21','exon',-1,'q13.4',42880007,'tmprss2-erg.t1e2.cosf23.1_1',2078,'enst00000442448','21','exon',-1,'p13.1',39956869,'tmprss2-erg.t1e2.cosf23.1_2','grch37','no','yes',100003,60000,'tmprss2-erg.t1e2.cosf23.1','fusion','gain-of-function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(7,2,57670,'enst00000242365','7','exon',-1,'q13.4',138536968,'kiaa1549-BRAF.k16b10.cosf509_1',673,'enst00000288602','7','exon',-1,'p13.1',140482957,'kiaa1549-BRAF.k16b10.cosf509_2','grch37','no','yes',100000,90000,'kiaa1549-BRAF.k16b10.cosf509','fusion','gain-of-function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(13,15,57670,'enst00000242365','7','exon',-1,'q13.4',138536968,'kiaa1549-BRAF.k16b10.cosf509_1',673,'enst00000288602','7','exon',-1,'p13.1',140482957,'kiaa1549-BRAF.k16b10.cosf509_2','grch37','no','yes',100000,90000,'kiaa1549-BRAF.k16b10.cosf509','fusion','gain-of-function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(13,15,8031,'enst00000344348','10','exon',-1,'q13.4',51582939,'ncoa4-ret.n7r12_1',5979,'enst00000340058','10','exon',-1,'p13.1',43612031,'ncoa4-ret.n7r12_2','grch37','no','yes',100001,80000,'ncoa4-ret.n7r1-2','fusion','gain-of-function','SOMATIC');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(13,15,7113,'enst00000332149','21','exon',-1,'q13.4',42880007,'tmprss2-erg.t1e2.cosf23.1_1',2078,'enst00000442448','21','exon',-1,'p13.1',39956869,'tmprss2-erg.t1e2.cosf23.1_2','grch37','no','yes',100003,60000,'tmprss2-erg.t1e2.cosf23.1','fusion','gain-of-function','GERMLINE');
insert into structural_variant (genetic_profile_id,sample_id,site1_entrez_gene_id,site1_ensembl_transcript_id,site1_chromosome,site1_region,site1_region_number,site1_contig,site1_position,site1_description,site2_entrez_gene_id,site2_ensembl_transcript_id,site2_chromosome,site2_region,site2_region_number,site2_contig,site2_position,site2_description,ncbi_build,dna_support,rna_support,tumor_read_count,tumor_variant_count,annotation,event_info,comments,sv_status)
values(13,15,8031,'enst00000344348','10','exon',-1,'q13.4',51582939,'ncoa4-null',null,'enst00000340058_null','10','exon',-1,'p13.1',43612031,'ncoa4-null','grch37','no','yes',100001,80000,'ncoa4-null','fusion','gain-of-function','SOMATIC');

insert into mut_sig (cancer_study_id,entrez_gene_id,rank,NumBasesCovered,NumMutations,p_value,q_value) values (1,207,1,998421,17,0.00000315,0.00233);
insert into mut_sig (cancer_study_id,entrez_gene_id,rank,NumBasesCovered,NumMutations,p_value,q_value) values (1,208,2,3200341,351,0.000000012,0.00000000000212);

-- gistic values (genetic_profile_id = 2) should only be {-2, -1, 0, 1, 2}
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (2,1,'-1,2,0,1,-2,2,-1,0,1,-2,2,0,1,-1,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (4,2,'1.4146,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1146,0.3498,0.0349,0.4927,-0.8665,-0.4754,-0.7221,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (3,2,'-0.8097,0.7360,-1.0225,-0.8922,0.7247,0.3537,1.2702,-0.1419,');
-- co-expression test data for study_tcga_pub_mrna (profile 3, samples: 2,3,6,8,9,10,12,13)
-- AKT1: all 8 samples have valid expression values
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (3,1,'0.5210,-0.3140,1.1550,0.2890,-0.8670,0.4230,-1.3640,0.7120,');
-- BRAF: samples 6 and 10 (positions 3 and 6) have NA — tests missing-value handling
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (3,6,'0.3450,-0.2180,NA,0.8910,-0.5670,NA,1.2340,-0.4560,');
-- KRAS: all samples have the same constant value — tests constant-gene filter (should be excluded)
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (3,10,'0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (9,17,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (9,18,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (11,19,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (11,20,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (12,28,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (12,29,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (14,1,'1,-1,NA,2,0,-2,1,NA,-1,0,2,-2,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (15,1,'-0.8097,0.7360,-0.1260,NA,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (18,30,'Loss,Gain,Unchanged,NA,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (18,32,'Loss,Unchanged,Unchanged,NA,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (18,33,'Loss,Gain,Unchanged,Unchanged,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (18,34,'Gain,Unchanged,Unchanged,NA,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (19,31,'No,NA,NA,NA,NA,NA,No,NA,NA,NA,No,No,NA,NA,NA,NA,NA,No,NA,No,No,NA,No,No,Yes,NA,No,');
insert into genetic_alteration (genetic_profile_id,genetic_entity_id,`values`) values (20,35,'Loss,Gain,Loss,Gain,');

insert into cna_event (cna_event_id,entrez_gene_id,alteration) values (1,207,-2);
insert into cna_event (cna_event_id,entrez_gene_id,alteration) values (2,208,2);
insert into cna_event (cna_event_id,entrez_gene_id,alteration) values (3,207,2);

insert into sample_cna_event (cna_event_id,sample_id,genetic_profile_id, annotation_json) values (1,1,2, '{"columnname":{"fieldname":"fieldvalue"}}');
insert into sample_cna_event (cna_event_id,sample_id,genetic_profile_id, annotation_json) values (2,1,2, '{"columnname":{"fieldname":"fieldvalue"}}');
insert into sample_cna_event (cna_event_id,sample_id,genetic_profile_id, annotation_json) values (3,2,2, '{"columnname":{"fieldname":"fieldvalue"}}');

insert into gistic (gistic_roi_id,cancer_study_id,chromosome,cytoband,wide_peak_start,wide_peak_end,q_value,amp) values (1,1,1,'1q32.32',123,136,0.0208839997649193,0);
insert into gistic (gistic_roi_id,cancer_study_id,chromosome,cytoband,wide_peak_start,wide_peak_end,q_value,amp) values (2,1,2,'2q30.32',324234,324280,0.000323799991747364,1);
insert into gistic (gistic_roi_id,cancer_study_id,chromosome,cytoband,wide_peak_start,wide_peak_end,q_value,amp) values (3,2,1,'1q3.32',123,136,0.000000129710002738648,0);

insert into gistic_to_gene (gistic_roi_id,entrez_gene_id) values (1,207);
insert into gistic_to_gene (gistic_roi_id,entrez_gene_id) values (1,208);
insert into gistic_to_gene (gistic_roi_id,entrez_gene_id) values (2,207);
insert into gistic_to_gene (gistic_roi_id,entrez_gene_id) values (3,208);

insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (1,1,123,0,'status');
insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (2,2,233,345,'SPECIMEN');
insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (3,2,213,445,'Treatment');
insert into clinical_event (clinical_event_id,patient_id,start_date,stop_date,event_type) values (4,2,211,441,'Seqencing');

insert into clinical_event_data (clinical_event_id,key,value) values (1,'status','radiographic_progression');
insert into clinical_event_data (clinical_event_id,key,value) values (1,'SAMPLE_ID','tcga-a1-a0sb-01');
insert into clinical_event_data (clinical_event_id,key,value) values (2,'surgery','oa ii initial');
insert into clinical_event_data (clinical_event_id,key,value) values (2,'SAMPLE_ID','tcga-a1-a0sd-01');
insert into clinical_event_data (clinical_event_id,key,value) values (3,'event_type_detailed','aa iii recurrence1');
insert into clinical_event_data (clinical_event_id,key,value) values (3,'AGENT','madeupanib');
insert into clinical_event_data (clinical_event_id,key,value) values (3,'agent_target','directly to forehead, elbow');
insert into clinical_event_data (clinical_event_id,key,value) values (3,'SAMPLE_ID','tcga-a1-a0sd-01');
insert into clinical_event_data (clinical_event_id,key,value) values (4,'SAMPLE_ID','tcga-a1-a0sd-01');

insert into geneset (id,genetic_entity_id,external_id,name,description,ref_link) values (1,17,'morf_atrx','morf atrx name','morf description','https://morf_link');
insert into geneset (id,genetic_entity_id,external_id,name,description,ref_link) values (2,18,'hinata_nfkb_matrix','hinata nfkb matrix name','hinata description','https://hinata_link');

insert into geneset_gene (geneset_id,entrez_gene_id) values (1,207);
insert into geneset_gene (geneset_id,entrez_gene_id) values (1,208);
insert into geneset_gene (geneset_id,entrez_gene_id) values (1,10000);
insert into geneset_gene (geneset_id,entrez_gene_id) values (2,369);
insert into geneset_gene (geneset_id,entrez_gene_id) values (2,472);

-- root node ->  sub node a -> parent node 1 -> morf_atrx
--    "              "             "         -> hinata_nfkb_matrix
--    "              "      -> parent node 2 -> hinata_nfkb_matrix
-- root node ->  sub node b -> x (dead branch)
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (1,'root node',null);
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (2,'sub node a',1);
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (3,'sub node b',1);
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (4,'parent node 1',2);
insert into geneset_hierarchy_node (node_id,node_name,parent_id) values (5,'parent node 2',2);

insert into geneset_hierarchy_leaf (node_id,geneset_id) values (4,1);
insert into geneset_hierarchy_leaf (node_id,geneset_id) values (4,2);
insert into geneset_hierarchy_leaf (node_id,geneset_id) values (5,2);

insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (6, 'AKT1 truncating', 207, 54, 64);
insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (6, null, 207, 21, 22);
insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (6, 'ARAF g1513 missense', 369, 1, 2);
insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (6, 'ARAF g1514 missense', 369, 4, 7);
insert into mutation_count_by_keyword (genetic_profile_id,keyword,entrez_gene_id,keyword_count,gene_count) values (8, 'NOC2L truncating', 26155, 1, 3);


-- treatment test data
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (1,19,'name','tanespimycin');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (2,19,'description','hsp90 inhibitor');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (3,19,'url','https://en.wikipedia.org/wiki/tanespimycin');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (4,20,'name','larotrectinib');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (5,20,'description','trka/b/c inhibitor');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (6,20,'url','https://en.wikipedia.org/wiki/larotrectinib');
-- WSI normalized hierarchy test data
insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public)
values (9001,'wsi_test_study','dummy','WSI test study','normalized WSI fixture',1);
insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public)
values (9002,'wsi_snapshot_study','dummy','WSI snapshot study','normalized WSI fixture',1);
insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public)
values (9003,'wsi_empty_hierarchy_study','dummy','WSI empty study','normalized WSI fixture',1);
insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public)
values (9004,'wsi_missing_data_study','dummy','WSI missing data study','normalized WSI fixture',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (9001,'WSI-PATIENT',9001);
insert into patient (internal_id,stable_id,cancer_study_id) values (9002,'SNAPSHOT-PATIENT',9002);
insert into patient (internal_id,stable_id,cancer_study_id) values (9003,'EMPTY-PATIENT',9003);
insert into patient (internal_id,stable_id,cancer_study_id) values (9004,'MISSING-DATA',9004);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (9001,'WSI-SAMPLE','primary tumor',9001);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (9002,'active-sample','primary tumor',9002);

-- Resource table privacy fixture. Kept in its own study so the study_tcga_pub resource counts
-- used by other tests are unchanged.
--  * WSI_SAMPLE (900501/900502) must never reach the generic resource APIs at all: not as rows,
--    tabs, search/filter matches, facets or discovered keys. Their public text ("Masson
--    trichrome", "liver") and slide keys appear nowhere else in the study.
--  * EXTERNAL_SLIDES (900505/900506) are WHOLE_SLIDE_IMAGE links outside the WSI resources, shown
--    in the generic table. Their sealed serving values contain words ("secretpath", "aaa"/"zzz")
--    that no public field contains, so a query that leaked wsi_serving into search, filters, sorting or
--    facets would be visible.
insert into cancer_study (cancer_study_id,cancer_study_identifier,type_of_cancer_id,name,description,public)
values (9005,'wsi_resource_table_study','dummy','WSI resource table study','resource table privacy fixture',1);
insert into patient (internal_id,stable_id,cancer_study_id) values (9005,'WSI-TABLE-PATIENT',9005);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (9005,'WSI-TABLE-SAMPLE-1','primary tumor',9005);
insert into sample (internal_id,stable_id,sample_type,patient_id) values (9006,'WSI-TABLE-SAMPLE-2','primary tumor',9005);
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority)
values ('WSI_SAMPLE',9005,'SAMPLE','Pathology slides','Whole-slide images linked to samples',0,1);
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority)
values ('PATHOLOGY_NOTES',9005,'PATIENT','Pathology notes','Untyped notes',0,2);
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority)
values ('EXTERNAL_SLIDES',9005,'SAMPLE','External slides','Slide links outside the WSI resources',0,3);
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900501,'WSI_SAMPLE',9005,'SAMPLE','WSI-TABLE-PATIENT','WSI-TABLE-SAMPLE-1','https://portal.example.org/wsi/patient/WSI-TABLE-PATIENT?studyId=wsi_resource_table_study&slideKey=5d41402abc4b2a76b9719d911017c592','Masson trichrome - Specimen 1 / Block 1','WHOLE_SLIDE_IMAGE','{"slide_key":"5d41402abc4b2a76b9719d911017c592","stain_name":"Masson trichrome","part_description":"liver wedge biopsy","wsi_serving":{"sealed_source":"zzz-wsipath-1-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900502,'WSI_SAMPLE',9005,'SAMPLE','WSI-TABLE-PATIENT','WSI-TABLE-SAMPLE-2','https://portal.example.org/wsi/patient/WSI-TABLE-PATIENT?studyId=wsi_resource_table_study&slideKey=7d793037a0760186574b0282f2f435e7','Masson trichrome - Specimen 2 / Block 1','WHOLE_SLIDE_IMAGE','{"slide_key":"7d793037a0760186574b0282f2f435e7","stain_name":"Masson trichrome","part_description":"liver margin","wsi_serving":{"sealed_source":"aaa-wsipath-2-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900505,'EXTERNAL_SLIDES',9005,'SAMPLE','WSI-TABLE-PATIENT','WSI-TABLE-SAMPLE-1','https://slides.example.org/viewer/ext-1','External slide 1','WHOLE_SLIDE_IMAGE','{"stain_name":"Periodic acid-Schiff","part_description":"left kidney core biopsy","wsi_serving":{"sealed_source":"zzz-secretpath-1-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900506,'EXTERNAL_SLIDES',9005,'SAMPLE','WSI-TABLE-PATIENT','WSI-TABLE-SAMPLE-2','https://slides.example.org/viewer/ext-2','External slide 2','WHOLE_SLIDE_IMAGE','{"stain_name":"H&E, Initial","part_description":"right kidney margin","wsi_serving":{"sealed_source":"aaa-secretpath-2-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900503,'PATHOLOGY_NOTES',9005,'PATIENT','WSI-TABLE-PATIENT',NULL,'https://example.com/notes/1.pdf','Board note',NULL,'{"note":"Reviewed by tumor board","wsi_serving":{"sealed_source":"secretpath-note-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900504,'PATHOLOGY_NOTES',9005,'PATIENT','WSI-TABLE-PATIENT',NULL,'https://example.com/notes/2.pdf','Follow-up note',NULL,'{"note":"Follow-up imaging"}');

-- WSI is served exclusively from generic resource_data. The nested wsi_serving object is
-- intentionally absent from generic table responses and is read only by the access repository.
-- The servable rows' sealed_source values are genuine seals (contract wsi-serving-v6 test key,
-- AAD = the row's slide_key); cBioPortal only checks their shape and forwards them.
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority)
values ('WSI_SAMPLE',9001,'SAMPLE','Pathology slides','Whole-slide images linked to samples',0,1);
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority)
values ('WSI_PATIENT',9001,'PATIENT','Pathology slides','Whole-slide images linked to patients',0,1);
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority)
values ('WSI_SAMPLE',9002,'SAMPLE','Pathology slides','Whole-slide images linked to samples',0,1);
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900101,'WSI_SAMPLE',9001,'SAMPLE','WSI-PATIENT','WSI-SAMPLE','https://portal.example.org/wsi/patient/WSI-PATIENT?studyId=wsi_test_study&slideKey=2351e12d49557627b24fe71e17ec5c64','H&E, Initial - Specimen 2 / Block 1','WHOLE_SLIDE_IMAGE','{"slide_key":"2351e12d49557627b24fe71e17ec5c64","reference_sample_id":"WSI-SAMPLE","part_key":"part:2351e12d49557627b24fe71e17ec5c64","part_number":"2","part_type":"FALLOPIAN TUBE","part_description":"Specimen 2","block_key":"block:2351e12d49557627b24fe71e17ec5c64","block_number":"1","block_label":"Block 1","stain_name":"H&E, Initial","stain_group":"H&E (Initial)","is_hne":true,"is_ihc":false,"magnification":"20x","file_size_bytes":716956681,"can_serve_tiles":true,"slide_type":"H&E","match_level":"BLOCK","specimen_key":"block::part:2351e12d49557627b24fe71e17ec5c64::block:2351e12d49557627b24fe71e17ec5c64","timeline_start_days":-17,"timeline_date_status":"AVAILABLE","timeline_date_kind":"RECORDED","timeline_date_source":"recorded_procedure_date","timeline_coordinate_system":"patient_first_tumor_sequencing_day_zero","timepoint_source":"Recorded procedure date relative to first tumor sequencing","wsi_serving":{"sealed_source":"AQEBAQEBAQEBAQEBMc_tL9dUso8ZSDg1JZIFeeclGKwdmpgGohT4JR793-tUE3oED7qLjtQhP_usT08BCC6kpTQl6az5wJ6AMvgEt1zCZgqnjk2gcXeEODZFCRVg80fRaEk2FQaFx_eROJgD6ajaxtdJ1OJw4aKN06MJD0Hc6zBDxi77WsoS","tile_metadata_json":{"dimensions":{"width":2048,"height":1024},"levels":1,"level_dimensions":[{"width":2048,"height":1024}],"level_downsamples":[1.0],"max_zoom":0,"tile_size":256},"thumbnail_width":128,"thumbnail_height":64,"thumbnail_content_type":"image/jpeg"}}');
-- Legacy WSI row without slide_key (pre-3.6.0 shape): never listed or served.
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900104,'WSI_SAMPLE',9001,'SAMPLE','WSI-PATIENT','WSI-SAMPLE','https://portal.example.org/wsi/patient/WSI-PATIENT?studyId=wsi_test_study&imageId=syn-legacy-0002','syn-legacy-0002','WHOLE_SLIDE_IMAGE','{"image_id":"syn-legacy-0002","reference_sample_id":"WSI-SAMPLE","part_key":"part:2351e12d49557627b24fe71e17ec5c64","part_number":"3","part_type":"FALLOPIAN TUBE","part_description":"Specimen 3","block_key":"block:2351e12d49557627b24fe71e17ec5c64","block_number":"1","block_label":"Block 1","stain_name":"H&E, Initial","stain_group":"H&E (Initial)","is_hne":true,"is_ihc":false,"magnification":"20x","can_serve_tiles":false,"slide_type":"H&E","match_level":"BLOCK","specimen_key":"block::part:2351e12d49557627b24fe71e17ec5c64::block:2351e12d49557627b24fe71e17ec5c64","timeline_date_status":"MISSING_PROCEDURE_DATE","timeline_date_kind":"UNDATED","timeline_date_source":"missing_procedure_date","timeline_date_reason":"procedure date unavailable","timeline_coordinate_system":"patient_first_tumor_sequencing_day_zero","timepoint_source":"Procedure date unavailable","wsi_serving":{}}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900102,'WSI_PATIENT',9001,'PATIENT','WSI-PATIENT',NULL,'https://portal.example.org/wsi/patient/WSI-PATIENT?studyId=wsi_test_study&slideKey=b3286836fc27c777260ada0dcb8a6857','H&E, Initial - Specimen 1 / Block 1','WHOLE_SLIDE_IMAGE','{"slide_key":"b3286836fc27c777260ada0dcb8a6857","reference_sample_id":"WSI-SAMPLE","part_key":"part:b3286836fc27c777260ada0dcb8a6857","part_number":"1","part_type":"SMALL BOWEL","part_description":"Specimen 1","block_key":"block:b3286836fc27c777260ada0dcb8a6857","block_number":"1","block_label":"Block 1","stain_name":"H&E, Initial","stain_group":"H&E (Initial)","is_hne":true,"is_ihc":false,"magnification":"20x","file_size_bytes":1014457317,"can_serve_tiles":false,"slide_type":"H&E","match_level":"UNMATCHED","specimen_key":"unmatched::part:b3286836fc27c777260ada0dcb8a6857::block:b3286836fc27c777260ada0dcb8a6857","timeline_date_status":"MISSING_PROCEDURE_DATE","timeline_date_kind":"UNDATED","timeline_date_source":"missing_procedure_date","timeline_date_reason":"procedure date unavailable","timeline_coordinate_system":"patient_first_tumor_sequencing_day_zero","timepoint_source":"Procedure date unavailable"}');
-- A WHOLE_SLIDE_IMAGE row outside the WSI_SAMPLE/WSI_PATIENT resources. The access lookup must
-- never serve it, even though it carries complete serving metadata.
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority)
values ('OTHER_SLIDES',9001,'PATIENT','Other slides','Slides outside the WSI resources',0,2);
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900103,'OTHER_SLIDES',9001,'PATIENT','WSI-PATIENT',NULL,'https://slides.example.org/viewer/other-1','Other slide','WHOLE_SLIDE_IMAGE','{"slide_key":"0f0e0d0c0b0a09080706050403020100","can_serve_tiles":true,"wsi_serving":{"sealed_source":"AgICAgICAgICAgICc82c3J3ELvii_D38NhOPdEvppO0JGdcHvP1iFH1htmuhz7A-eI-ubwjYnntpC2RNvORyvUFLuyRg9vQIXZcIzPEImdUBa8X96V1xUf1HR3v_9vW6bSMSBEKNmKtxctnoHLqQooRBJZYBrTfKvCVca-SWJs3LM5u30h-4","tile_metadata_json":{"dimensions":{"width":2048,"height":1024},"levels":1,"level_dimensions":[{"width":2048,"height":1024}],"level_downsamples":[1.0],"max_zoom":0,"tile_size":256},"thumbnail_width":128,"thumbnail_height":64,"thumbnail_content_type":"image/jpeg"}}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata)
values (900201,'WSI_SAMPLE',9002,'SAMPLE','SNAPSHOT-PATIENT','active-sample','https://portal.example.org/wsi/patient/SNAPSHOT-PATIENT?studyId=wsi_snapshot_study&slideKey=e8ac3c1341f0fb1fa1a7c8e69ba27a51','Specimen 1 / Block 1','WHOLE_SLIDE_IMAGE','{"slide_key":"e8ac3c1341f0fb1fa1a7c8e69ba27a51","reference_sample_id":"active-sample","part_key":"part:e8ac3c1341f0fb1fa1a7c8e69ba27a51","part_number":"1","part_type":"active part","part_description":"Specimen 1","block_key":"block:e8ac3c1341f0fb1fa1a7c8e69ba27a51","block_number":"1","block_label":"Block 1","is_hne":false,"is_ihc":false,"can_serve_tiles":false,"slide_type":"Other","match_level":"PART","specimen_key":"part::part:e8ac3c1341f0fb1fa1a7c8e69ba27a51::block:e8ac3c1341f0fb1fa1a7c8e69ba27a51","timeline_date_status":"MISSING_PROCEDURE_DATE","timeline_date_kind":"UNDATED","timeline_date_source":"missing_procedure_date","timeline_date_reason":"procedure date unavailable","timeline_coordinate_system":"patient_first_tumor_sequencing_day_zero","timepoint_source":"Procedure date unavailable"}');

-- generic assay test data
-- mutational signature test data
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (7,28,'name','mean_1');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (8,28,'description','description of mean_1');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (9,29,'name','mean_2');
insert into generic_entity_properties (id,genetic_entity_id,name,value) values (10,29,'description','description of mean_2');

-- allele specific copy number data
insert into allele_specific_copy_number (mutation_event_id, genetic_profile_id, sample_id, ascn_integer_copy_number, ascn_method, ccf_expected_copies_upper, ccf_expected_copies, clonal, minor_copy_number, expected_alt_copies, total_copy_number) values (2040, 6, 1, 3, 'facets', 1.25, 1.75, 'clonal', 2, 1, 4);
insert into allele_specific_copy_number (mutation_event_id, genetic_profile_id, sample_id, ascn_integer_copy_number, ascn_method, ccf_expected_copies_upper, ccf_expected_copies, clonal, minor_copy_number, expected_alt_copies, total_copy_number) values (2038, 6, 6, 1, 'facets', 1.25, 1.75, 'subclonal', 1, 1, 2);
-- resource table test data
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority) values ('HE_SLIDE', 1, 'SAMPLE', 'H&E Slide', 'Hematoxylin and Eosin Slide', 1, 1);
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority) values ('CT_SCAN', 1, 'PATIENT', 'CT Scan', 'CT Scan images', 1, 2);
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority) values ('FIGURES', 1, 'STUDY', 'Figures', 'Study figures', 1, 3);
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority, custom_metadata) values ('RADIOLOGY', 1, 'SAMPLE', 'Radiology', 'Radiology images', 1, 4, '{"version":1,"fields":[{"key":"score","type":"number","label":"Dose Score","description":"Radiation dose score","visibleByDefault":true},{"key":"dose_id","type":"string","label":"Dose ID"},{"key":"operator","type":"string","label":"Operator","filterable":false},{"key":"series","type":"string","label":"Series"}]}');

-- Multi-study contract divergence fixtures. These hang off their own patient so that the
-- cohort-filtering tests above keep the tab sets they were written for.
-- Contracts are ordered by cancer_study_identifier,
-- and 'acc_tcga' sorts before 'study_tcga_pub', so study 2's contract is the first one.
-- PATHOLOGY is declared by BOTH studies, differently: acc_tcga declares grade/reviewer,
-- study_tcga_pub declares stain/grade, and the two disagree on grade's type.
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority, custom_metadata) values ('PATHOLOGY', 1, 'SAMPLE', 'Pathology (tcga)', 'Pathology reports', 1, 5, '{"version":1,"fields":[{"key":"stain","type":"string","label":"Stain (tcga)"},{"key":"grade","type":"number","label":"Grade (tcga)","visibleByDefault":true}]}');
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority, custom_metadata) values ('PATHOLOGY', 2, 'SAMPLE', 'Pathology (acc)', 'Pathology reports', 1, 5, '{"version":1,"fields":[{"key":"grade","type":"string","label":"Grade (acc)"},{"key":"reviewer","type":"string","label":"Reviewer"}]}');
-- CYTOLOGY has rows in both studies but only acc_tcga declares it at all, so study_tcga_pub's
-- rows were never checked against that contract and it cannot be the whole column list for a
-- cohort over both. It is also the only resource with data in a study that does not define it,
-- which is what makes the tab label fall back to the resource id.
insert into resource_definition (resource_id, cancer_study_id, resource_type, display_name, description, open_by_default, priority, custom_metadata) values ('CYTOLOGY', 2, 'SAMPLE', 'Cytology (acc)', 'Cytology slides', 1, 6, '{"version":1,"fields":[{"key":"preparation","type":"string","label":"Preparation"}]}');

insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (1, 'HE_SLIDE', 1, 'SAMPLE', 'tcga-a1-a0sb', 'tcga-a1-a0sb-01', 'https://example.com/he1.jpg', 'H&E Sample 1', 'IMAGE', '{"stain":"HE","magnification":"20x"}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (2, 'HE_SLIDE', 1, 'SAMPLE', 'tcga-a1-a0sd', 'tcga-a1-a0sd-01', 'https://example.com/he2.jpg', 'H&E Sample 2', 'IMAGE', '{"stain":"HE","magnification":"40x"}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (3, 'CT_SCAN', 1, 'PATIENT', 'tcga-a1-a0sb', NULL, 'https://example.com/ct1.dcm', 'CT Scan Patient 1', 'LINK', NULL);
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (4, 'FIGURES', 1, 'STUDY', NULL, NULL, 'https://example.com/fig1.pdf', 'Study Figure', 'PDF', '{"pages":10}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (5, 'FIGURES', 1, 'STUDY', NULL, NULL, 'https://example.com/fig2.pdf', 'Study Figure 2', 'PDF', '{"pages":25}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (6, 'RADIOLOGY', 1, 'SAMPLE', 'tcga-a1-a0se', 'tcga-a1-a0se-01', 'https://example.com/rad1.dcm', 'Radiology 1', 'IMAGE', '{"dose_id":"1001","score":"85","operator":"tech-a","aperture":"wide"}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (7, 'RADIOLOGY', 1, 'SAMPLE', 'tcga-a1-a0sf', 'tcga-a1-a0sf-01', 'https://example.com/rad2.dcm', 'Radiology 2', 'IMAGE', '{"dose_id":"1002","score":"42","operator":"tech-b","aperture":"narrow"}');

-- Study 2 (acc_tcga) deliberately reuses stable ids that also exist in study 1. Stable ids are
-- unique only within a study, so a cohort spanning both studies must not match study 2's
-- 'tcga-a1-a0sb-01' just because study 1's sample of that name was selected, and the distinct
-- patient/sample counts must not collapse the two into one.
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (8, 'HE_SLIDE', 2, 'SAMPLE', 'tcga-a1-a0sb', 'tcga-a1-a0sb-01', 'https://example.com/acc-he-collides.jpg', 'ACC H&E (same barcode, different sample)', 'IMAGE', '{"stain":"HE","magnification":"20x"}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (9, 'HE_SLIDE', 2, 'SAMPLE', 'tcga-zz-9999', 'tcga-zz-9999-01', 'https://example.com/acc-he-own.jpg', 'ACC H&E (own sample)', 'IMAGE', '{"stain":"HE","magnification":"40x"}');

insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (14, 'PATHOLOGY', 1, 'SAMPLE', 'tcga-a1-a0zz', 'tcga-a1-a0zz-01', 'https://example.com/path1.pdf', 'Pathology 1', 'PDF', '{"stain":"HE","grade":"3"}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (15, 'PATHOLOGY', 2, 'SAMPLE', 'tcga-zz-9999', 'tcga-zz-9999-01', 'https://example.com/path2.pdf', 'Pathology 2', 'PDF', '{"grade":"high","reviewer":"dr-b"}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (16, 'CYTOLOGY', 1, 'SAMPLE', 'tcga-a1-a0zz', 'tcga-a1-a0zz-01', 'https://example.com/cyto1.jpg', 'Cytology 1', 'IMAGE', '{"fixative":"alcohol"}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (17, 'CYTOLOGY', 2, 'SAMPLE', 'tcga-zz-9999', 'tcga-zz-9999-01', 'https://example.com/cyto2.jpg', 'Cytology 2', 'IMAGE', '{"preparation":"smear"}');

-- Four rows on the SAME patient and sample, so (patient_id, sample_id) is not a unique ordering
-- and paging is only stable if the sort carries a tiebreaker. 'score' is stored as a JSON
-- *number*, and the values are chosen so lexicographic and numeric order disagree: sorted as text
-- they read 10, 100, 20, 9.
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (10, 'SLIDE_SET', 1, 'SAMPLE', 'tcga-a1-a0sk', 'tcga-a1-a0sk-01', 'https://example.com/set-9.svs',   'Set 9',   'IMAGE', '{"score":9,"file_size_bytes":100,"is_hne":true,"mpp":0.5}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (11, 'SLIDE_SET', 1, 'SAMPLE', 'tcga-a1-a0sk', 'tcga-a1-a0sk-01', 'https://example.com/set-10.svs',  'Set 10',  'IMAGE', '{"score":10,"file_size_bytes":2000,"is_hne":false,"mpp":0.25}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (12, 'SLIDE_SET', 1, 'SAMPLE', 'tcga-a1-a0sk', 'tcga-a1-a0sk-01', 'https://example.com/set-20.svs',  'Set 20',  'IMAGE', '{"score":20,"file_size_bytes":30,"is_hne":true,"mpp":1.0}');
insert into resource_data (resource_data_id, resource_id, cancer_study_id, entity_type, patient_id, sample_id, url, display_name, type, metadata) values (13, 'SLIDE_SET', 1, 'SAMPLE', 'tcga-a1-a0sk', 'tcga-a1-a0sk-01', 'https://example.com/set-100.svs', 'Set 100', 'IMAGE', '{"score":100,"file_size_bytes":400,"is_hne":false,"mpp":0.75}');
