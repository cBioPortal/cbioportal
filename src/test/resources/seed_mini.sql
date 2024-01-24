--
-- Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
--
-- This library is distributed in the hope that it will be useful,but WITHOUT
-- ANY WARRANTY,WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
-- FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
-- is on an "as is" basis,and Memorial Sloan-Kettering Cancer Center has no
-- obligations to provide maintenance,support,updates,enhancements or
-- modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
-- liable to any party for direct,indirect,special,incidental or
-- consequential damages,including lost profits,arising out of the use of this
-- software and its documentation,even if Memorial Sloan-Kettering Cancer
-- Center has been advised of the possibility of such damage.
--
-- This file is part of cBioPortal.
--
-- cBioPortal is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as
-- published by the Free Software Foundation,either version 3 of the
-- License.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not,see <http://www.gnu.org/licenses/>.
-- ----------------------------------------------------------------------------
-- A manually extracted subset of data for a small number of genes and samples from the BRCA
-- data set. This is intended to be used during unit testing,to validate the portal APIs.
-- In theory,it should be enough to run up a portal.
--
-- Prepared by Stuart Watt -- 13th May 2015

SET SESSION sql_mode = 'ANSI_QUOTES';

DELETE FROM structural_variant;
DELETE FROM clinical_event_data;
DELETE FROM clinical_event;
DELETE FROM cosmic_mutation;
DELETE FROM copy_number_seg_file;
DELETE FROM copy_number_seg;
DELETE FROM sample_cna_event;
DELETE FROM cna_event;
DELETE FROM gistic_to_gene;
DELETE FROM gistic;
DELETE FROM mut_sig;
DELETE FROM clinical_attribute_meta;
DELETE FROM mutation;
DELETE FROM mutation_event;
DELETE FROM sample_profile;
DELETE FROM gene_panel;
DELETE FROM gene_panel_list;
DELETE FROM genetic_profile_samples;
DELETE FROM genetic_alteration;
DELETE FROM genetic_profile;
DELETE FROM gene_alias;
DELETE FROM gene;
DELETE FROM clinical_sample;
DELETE FROM sample;
DELETE FROM sample_list_list;
DELETE FROM sample_list;
DELETE FROM clinical_patient;
DELETE FROM patient;
DELETE FROM authorities;
DELETE FROM users;
DELETE FROM cancer_study;
DELETE FROM type_of_cancer;
DELETE FROM genetic_entity;

-- type_of_cancer
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('acc','Adrenocortical Carcinoma','Purple','ACC','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('acbc','Adenoid Cystic Breast Cancer','HotPink','ACBC','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('acyc','Adenoid Cystic Carcinoma','DarkRed','ACyC','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('aeca','Sweat Gland Carcinoma (Apocrine Eccrine Carcinoma)','Black','AECA','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('afx','Atypical Fibroxanthoma','Black','AFX','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('all','Pediatric Acute Lymphoid Leukemia','LightSalmon','ALL','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('aml','Acute Monocytic Leukemia','LightSalmon','AML-M5','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('blad','Bladder Urothelial Adenocarcinoma','Yellow','Bladder','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('blca','Bladder Urothelial Carcinoma','Yellow','Bladder','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('bpdcn','Blastic Plasmacytoid Dendritic Cell Neoplasm','LightSalmon','BPDCN','tissue');
INSERT INTO "type_of_cancer" ("TYPE_OF_CANCER_ID","NAME","DEDICATED_COLOR","SHORT_NAME","PARENT") VALUES ('brca','Breast Invasive Carcinoma','HotPink','Breast','tissue');

-- reference_genome
INSERT INTO `reference_genome` VALUES (1,'human','hg19','GRCh37',2897310462,'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips','2009-02-01');
INSERT INTO `reference_genome` VALUES (2,'human','hg38','GRCh38',3049315783,'http://hgdownload.cse.ucsc.edu/goldenPath/hg38/bigZips','2013-12-01');

-- cancer_study
INSERT INTO "cancer_study" ("CANCER_STUDY_ID","CANCER_STUDY_IDENTIFIER","TYPE_OF_CANCER_ID","NAME","DESCRIPTION","PUBLIC","PMID","CITATION","GROUPS","REFERENCE_GENOME_ID") 
VALUES (1,'study_tcga_pub','brca','Breast Invasive Carcinoma (TCGA,Nature 2012)','<a href=\"http://cancergenome.nih.gov/\">The Cancer Genome Atlas (TCGA)</a> Breast Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\"http://tcga-data.nci.nih.gov/tcga/\">Raw data via the TCGA Data Portal</a>.',1,'23000897,26451490','TCGA,Nature 2012,...','SU2C-PI3K;GDAC',1);

INSERT INTO "cancer_study" ("CANCER_STUDY_ID","CANCER_STUDY_IDENTIFIER","TYPE_OF_CANCER_ID","NAME","DESCRIPTION","PUBLIC","PMID","CITATION","GROUPS","REFERENCE_GENOME_ID")
VALUES(2,'acc_tcga','acc','Adrenocortical Carcinoma (TCGA, Provisional)','TCGA Adrenocortical Carcinoma; raw data at the <A HREF="https://tcga-data.nci.nih.gov/">NCI</A>.',1,'23000897','TCGA, Nature 2012','SU2C-PI3K;GDAC',1);


-- gene as genetic_entity
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,207,'AKT1','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,208,'AKT2','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,10000,'AKT3','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,369,'ARAF','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,472,'ATM','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,673,'BRAF','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,672,'BRCA1','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,675,'BRCA2','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,3265,'HRAS','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,3845,'KRAS','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,4893,'NRAS','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,51259,'TMEM216','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,282770,'OR10AG1','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,983,'CDK1','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,8085,'KMT2D','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
-- add genes for structural variant events
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,57670,'KIAA1549','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,27436,'EML4','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,238,'ALK','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,2115,'ETV1','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,7273,'TTN','protein-coding');

-- for test panels 1 and 2
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,3983,'ABLIM1','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,80070,'ADAMTS20','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,253559,'CADM2','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,1838,'DTNB','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,2648,'KAT2A','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,4437,'MSH3','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,4602,'MYB','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,440348,'NPIPB15','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,56914,'OTOR','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,27334,'P2RY10','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,9780,'PIEZO1','protein-coding');
INSERT INTO "genetic_entity" ("ENTITY_TYPE") VALUES ('GENE');
SET @max_entity_id = (Select MAX(ID) from genetic_entity);
INSERT INTO "gene" ("GENETIC_ENTITY_ID","ENTREZ_GENE_ID","HUGO_GENE_SYMBOL","TYPE") VALUES (@max_entity_id,2261,'FGFR3','protein-coding');

-- cna_event
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (20093,207,-2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (20092,207,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (26161,208,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (2774,10000,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (16668,472,-2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (16669,472,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (11284,673,-2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (11283,673,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (23934,672,-2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (23933,672,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (18460,675,-2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (18459,675,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (15117,3265,-2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (15118,3265,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (17280,3845,2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (1678,4893,-2);
INSERT INTO "cna_event" ("CNA_EVENT_ID","ENTREZ_GENE_ID","ALTERATION") VALUES (1677,4893,2);

-- cosmic_mutation
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('78883','17',41197784,'G','A','-','c.5503C>T',672,'R1835*',2,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('23923','17',41199686,'GC','G','-','c.5440delG',672,'A1814fs*20',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('24529','17',41201160,'A','AA','-','c.5383_5384insT',672,'S1796fs*34',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('1577248','17',41201164,'C','A','-','c.5380G>T',672,'E1794*',2,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('35891','17',41209082,'G','GG','-','c.5263_5264insC',672,'Q1756fs*74',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('51256','17',41209095,'G','A','-','c.5251C>T',672,'R1751*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('24528','17',41215370,'C','A','-','c.5173G>T',672,'E1725*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('68967','17',41215913,'TC','T','-','c.5129delG',672,'G1710fs*4',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('95581','17',41223001,'C','A','-','c.4930G>T',672,'E1644*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('219051','17',41223106,'C','A','-','c.4825G>T',672,'E1609*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('78884','17',41226411,'G','A','-','c.4612C>T',672,'Q1538*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('187264','17',41226515,'G','T','-','c.4508C>A',672,'S1503*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('3983467','17',41228590,'G','A','-','c.4399C>T',672,'Q1467*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('979730','17',41234451,'G','A','-','c.4327C>T',672,'R1443*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('23959','17',41243685,'TC','T','-','c.3862delG',672,'E1288fs*19',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('1302865','17',41243707,'G','A','-','c.3841C>T',672,'Q1281*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('1577247','17',41243721,'A','T','-','c.3827T>A',672,'L1276*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('69203','17',41243753,'A','ATT','-','c.3794_3795insAA',672,'N1265fs*4',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('1577246','17',41243781,'GTGTTC','G','-','c.3762_3766delGAACA',672,'K1254fs*11',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('112117','17',41243899,'A','ATAAGTTCT','-','c.3648_3649insAGAACTTA',672,'S1217fs*21',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('1325059','17',41243984,'CCT','C','-','c.3562_3563delAG',672,'R1188fs*3',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('23931','17',41243998,'CTT','C','-','c.3548_3549delAA',672,'K1183fs*4',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('1666857','17',41244021,'AC','A','-','c.3526_3526delG',672,'V1176fs*34',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('706227','17',41244070,'T','A','-','c.3478A>T',672,'K1160*',1,'BRCA1 truncating');
INSERT INTO "cosmic_mutation" ("COSMIC_MUTATION_ID","CHR","START_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","STRAND","CODON_CHANGE","ENTREZ_GENE_ID","PROTEIN_CHANGE","COUNT","KEYWORD") VALUES ('1325058','17',41244082,'C','CATCTAACA','-','c.3465_3466insTGTTAGAT',672,'D1156fs*2',1,'BRCA1 truncating');

-- gene_alias
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (207,'AKT');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (207,'CWS6');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (208,'HIHGHH');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (208,'PKBB');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (10000,'MPPH');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (10000,'MPPH2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (369,'A-RAF');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (369,'ARAF1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (472,'AT1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (472,'ATA');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (673,'B-RAF1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (673,'BRAF1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (672,'BRCAI');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (672,'BRCC1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (675,'BRCC2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (675,'BROVCA2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (675,'FACD');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3265,'C-BAS/HAS');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3265,'C-H-RAS');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3845,'C-K-RAS');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3845,'KRAS1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3845,'KRAS2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4893,'N-ras');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4893,'NCMS');

-- for test panels 1 and 2
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (56914,'FDP');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (56914,'MIAL1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (253559,'IGSF4D');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (253559,'Necl-3');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (253559,'NECL3');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (253559,'SynCAM 2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (253559,'synCAM2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4437,'MRP1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4437,'DUP');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4437,'FAP4');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (27334,'P2Y10');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (27334,'LYPSR2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (80070,'ADAM-TS20');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (80070,'GON-1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (80070,'ADAMTS-20');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2648,'hGCN5');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2648,'GCN5');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2648,'GCN5L2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2648,'PCAF-b');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4602,'c-myb_CDS');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4602,'Cmyb');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4602,'c-myb');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (4602,'efg');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (440348,'A-761H5.4');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (440348,'NPIPL2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3983,'LIMAB1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3983,'abLIM-1');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3983,'ABLIM');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (3983,'LIMATIN');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (9780,'FAM38A');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (9780,'Mib');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (9780,'LMPH3');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (9780,'DHS');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2261,'JTK4');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2261,'HSFGFR3EX');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2261,'ACH');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2261,'CEK2');
INSERT INTO "gene_alias" ("ENTREZ_GENE_ID","GENE_ALIAS") VALUES (2261,'CD333');

INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,START,END,CHR,REFERENCE_GENOME_ID) VALUES(207,'14q32.33',105235686,105262088,14,1);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,START,END,CHR,REFERENCE_GENOME_ID) VALUES(207,'14q32.33',104769349,104795751,14,2);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,START,END,CHR,REFERENCE_GENOME_ID) VALUES(208,'19q13.2',40736224,40791443,19,1);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,START,END,CHR,REFERENCE_GENOME_ID) VALUES(208,'19q13.2',40230317,40285536,19,2);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,START,END,CHR,REFERENCE_GENOME_ID) VALUES(51259,'11q12.2',61159159,61166335,11,1);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,START,END,CHR,REFERENCE_GENOME_ID) VALUES(51259,'11q12.2',61391687,61398863,11,2);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,START,END,CHR,REFERENCE_GENOME_ID) VALUES(282770,'11q12.1',55734975,55735990,11,1);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,START,END,CHR,REFERENCE_GENOME_ID) VALUES(282770,'11q12.1',55967558,55968463,11,2);

INSERT INTO "genetic_profile" ("GENETIC_PROFILE_ID","STABLE_ID","CANCER_STUDY_ID","GENETIC_ALTERATION_TYPE","DATATYPE","NAME","DESCRIPTION","SHOW_PROFILE_IN_ANALYSIS_TAB") VALUES (2,'study_tcga_pub_gistic',1,'COPY_NUMBER_ALTERATION','DISCRETE','Putative copy-number alterations from GISTIC','Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.','1');
INSERT INTO "genetic_profile" ("GENETIC_PROFILE_ID","STABLE_ID","CANCER_STUDY_ID","GENETIC_ALTERATION_TYPE","DATATYPE","NAME","DESCRIPTION","SHOW_PROFILE_IN_ANALYSIS_TAB") VALUES (3,'study_tcga_pub_mrna',1,'MRNA_EXPRESSION','Z-SCORE','mRNA expression (microarray)','Expression levels (Agilent microarray).','0');
INSERT INTO "genetic_profile" ("GENETIC_PROFILE_ID","STABLE_ID","CANCER_STUDY_ID","GENETIC_ALTERATION_TYPE","DATATYPE","NAME","DESCRIPTION","SHOW_PROFILE_IN_ANALYSIS_TAB") VALUES (4,'study_tcga_pub_log2CNA',1,'COPY_NUMBER_ALTERATION','LOG2-VALUE','Log2 copy-number values','Log2 copy-number values for each gene (from Affymetrix SNP6).','0');
INSERT INTO "genetic_profile" ("GENETIC_PROFILE_ID","STABLE_ID","CANCER_STUDY_ID","GENETIC_ALTERATION_TYPE","DATATYPE","NAME","DESCRIPTION","SHOW_PROFILE_IN_ANALYSIS_TAB") VALUES (5,'study_tcga_pub_methylation_hm27',1,'METHYLATION','CONTINUOUS','Methylation (HM27)','Methylation beta-values (HM27 platform). For genes with multiple methylation probes,the probe least correlated with expression is selected.','0');
INSERT INTO "genetic_profile" ("GENETIC_PROFILE_ID","STABLE_ID","CANCER_STUDY_ID","GENETIC_ALTERATION_TYPE","DATATYPE","NAME","DESCRIPTION","SHOW_PROFILE_IN_ANALYSIS_TAB") VALUES (6,'study_tcga_pub_mutations',1,'MUTATION_EXTENDED','MAF','Mutations','Mutation data from whole exome sequencing.','1');
INSERT INTO "genetic_profile" ("GENETIC_PROFILE_ID","STABLE_ID","CANCER_STUDY_ID","GENETIC_ALTERATION_TYPE","DATATYPE","NAME","DESCRIPTION","SHOW_PROFILE_IN_ANALYSIS_TAB") VALUES (7,'study_tcga_pub_structural_variants',1,'STRUCTURAL_VARIANT','SV','Structural Variants','Structural Variants test data.','1');

-- genetic_alteration
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 10000),'0,0,1,2,0,1,1,1,0,1,1,1,0,1,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 207),'0,0,0,0,0,0,0,0,-1,0,0,0,-1,0,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 208),'0,0,0,1,0,0,0,1,0,0,0,2,0,2,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 3265),'0,0,0,0,0,0,1,0,0,0,0,-1,0,0,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 3845),'0,0,0,1,0,0,-1,1,1,0,1,2,2,0,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 472),'0,-1,-1,0,0,-1,0,1,0,-1,-1,1,-1,-1,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 4893),'0,-1,0,-1,0,0,0,0,-1,1,-1,0,0,0,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 672),'0,0,0,0,0,1,0,-1,0,-1,-1,0,-1,0,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 673),'0,0,0,1,0,0,0,0,0,0,-1,0,0,0,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (2,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 675),'0,-1,0,0,0,0,-1,0,1,0,-1,1,-1,-1,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 10000),'-0.473833333333333,1.51866666666667,0.148333333333333,-0.187666666666667,0.914,-0.664333333333333,-1.70783333333333,0.976,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 207),'-1.102375,-0.24375,0.018625,-0.157,0.33075,1.008,0.68175,-0.664875,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 208),'-1.22235714285714,-0.592571428571429,-0.176642857142857,-0.310428571428571,-1.19892857142857,-0.670142857142857,0.0779285714285714,-0.302642857142857,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 3265),'0.068,-0.062,-0.167833333333333,0.511666666666667,2.02066666666667,0.389166666666667,-0.724666666666666,0.9485,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 369),'-1.12475,-0.30675,0.1805,-0.59775,0.16625,0.402,0.243,-0.996,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 3845),'-0.17075,0.4045,0.185333333333333,0.4285,1.67616666666667,0.238,0.469833333333333,2.15883333333333,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 472),'-1.50341666666667,-1.92183333333333,-1.75541666666667,-1.57325,-1.02958333333333,-1.39791666666667,-1.51483333333333,-2.07091666666667,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 4893),'-1.91125,-2.0595,-1.22825,-1.319,-4.16675,-1.187875,0.280625,-0.13075,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 672),'-1.66108333333333,-1.38791666666667,-1.92483333333333,-1.65625,-0.35825,-1.99566666666667,-0.136416666666667,-0.70975,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 673),'0.2305,0.56,-0.10225,-0.0855,-0.012,0.138,0.141,0.6095,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (3,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 675),'-0.57075,-1.3405,-1.541,-0.40475,0.629,-1.2315,0.768,-0.033,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 10000),'0.017,0.032,0.872,0.704,0.009,0.485,0.848,0.685,-0.270,1.054,0.658,0.455,0.256,0.628,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 207),'-0.005,-0.003,-0.022,0.072,-0.042,0.015,0.045,0.026,-0.441,-0.030,0.009,0.154,-0.348,0.011,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 208),'0.045,-0.048,0.043,0.324,-0.064,0.036,0.147,0.404,0.112,0.012,0.043,1.418,0.069,1.670,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 3265),'0.003,-0.043,0.009,0.009,-0.043,0.031,0.411,-0.048,0.049,-0.030,0.056,-0.695,-0.022,-0.001,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 3845),'-0.009,0.001,0.006,0.379,0.022,-0.010,-0.473,1.172,2.823,0.014,0.339,1.450,3.160,0.026,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 472),'-0.006,-0.500,-0.462,-0.013,-0.003,-0.448,0.135,0.522,0.211,-0.578,-0.350,0.655,-0.449,-0.668,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 4893),'0.011,-0.420,0.014,-0.341,-0.019,0.020,0.032,-0.052,-0.453,0.455,-0.318,0.278,0.240,0.034,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 672),'0.044,-0.082,0.027,-0.003,-0.012,0.486,-0.300,-0.397,-0.284,-0.547,-0.301,0.218,-0.389,-0.011,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 673),'0.015,0.005,0.005,0.344,-0.011,0.171,0.140,-0.013,0.021,0.010,-0.385,-0.177,0.075,0.002,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (4,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 675),'-0.005,-0.502,-0.240,0.016,0.022,-0.029,-0.319,0.004,1.020,-0.024,-0.366,0.768,-0.397,-0.667,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 10000),'0.841525757,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 207),'0.134488708,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 208),'0.072712077,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 3265),'0.039372,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 369),'0.125441004,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 3845),'0.049338479,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 472),'0.025402093,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 4893),'0.105977072,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 672),'0.066638638,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 673),'0.020369562,');
INSERT INTO "genetic_alteration" ("GENETIC_PROFILE_ID","GENETIC_ENTITY_ID","VALUES") VALUES (5,(Select "GENETIC_ENTITY_ID" from "gene" where "ENTREZ_GENE_ID" = 675),'0.793930197,');

-- genetic_profile_samples
INSERT INTO "genetic_profile_samples" ("GENETIC_PROFILE_ID","ORDERED_SAMPLE_LIST") VALUES (2,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
INSERT INTO "genetic_profile_samples" ("GENETIC_PROFILE_ID","ORDERED_SAMPLE_LIST") VALUES (3,'2,3,6,8,9,10,12,13,');
INSERT INTO "genetic_profile_samples" ("GENETIC_PROFILE_ID","ORDERED_SAMPLE_LIST") VALUES (4,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
INSERT INTO "genetic_profile_samples" ("GENETIC_PROFILE_ID","ORDERED_SAMPLE_LIST") VALUES (5,'2,');

-- patient
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (1,'TCGA-A1-A0SB',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (2,'TCGA-A1-A0SD',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (3,'TCGA-A1-A0SE',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (4,'TCGA-A1-A0SF',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (5,'TCGA-A1-A0SG',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (6,'TCGA-A1-A0SH',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (7,'TCGA-A1-A0SI',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (8,'TCGA-A1-A0SJ',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (9,'TCGA-A1-A0SK',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (10,'TCGA-A1-A0SM',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (11,'TCGA-A1-A0SN',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (12,'TCGA-A1-A0SO',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (13,'TCGA-A1-A0SP',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (14,'TCGA-A1-A0SQ',1);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (15,'TCGA-XX-0800',2);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (16,'TCGA-XX-0900',2);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (17,'TCGA-AA-3664',2);
INSERT INTO "patient" ("INTERNAL_ID","STABLE_ID","CANCER_STUDY_ID") VALUES (18,'TCGA-AA-3665',2);

-- sample
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (1,'TCGA-A1-A0SB-01','Primary Solid Tumor',1);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (2,'TCGA-A1-A0SD-01','Primary Solid Tumor',2);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (3,'TCGA-A1-A0SE-01','Primary Solid Tumor',3);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (4,'TCGA-A1-A0SF-01','Primary Solid Tumor',4);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (5,'TCGA-A1-A0SG-01','Primary Solid Tumor',5);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (6,'TCGA-A1-A0SH-01','Primary Solid Tumor',6);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (7,'TCGA-A1-A0SI-01','Primary Solid Tumor',7);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (8,'TCGA-A1-A0SJ-01','Primary Solid Tumor',8);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (9,'TCGA-A1-A0SK-01','Primary Solid Tumor',9);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (10,'TCGA-A1-A0SM-01','Primary Solid Tumor',10);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (11,'TCGA-A1-A0SN-01','Primary Solid Tumor',11);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (12,'TCGA-A1-A0SO-01','Primary Solid Tumor',12);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (13,'TCGA-A1-A0SP-01','Primary Solid Tumor',13);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (14,'TCGA-A1-A0SQ-01','Primary Solid Tumor',14);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (15,'TCGA-XX-0800-01','Primary Solid Tumor',15);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (16,'TCGA-XX-0900-01','Primary Solid Tumor',16);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (17,'TCGA-AA-3664-01','Primary Solid Tumor',17);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (18,'TCGA-AA-3665-01','Primary Solid Tumor',18);
INSERT INTO "sample" ("INTERNAL_ID","STABLE_ID","SAMPLE_TYPE","PATIENT_ID") VALUES (19,'TCGA-A1-A0SB-02','Primary Solid Tumor',1);

-- mutation_event
INSERT INTO "mutation_event" ("MUTATION_EVENT_ID","ENTREZ_GENE_ID","CHR","START_POSITION","END_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","PROTEIN_CHANGE","MUTATION_TYPE","NCBI_BUILD","STRAND","VARIANT_TYPE","DB_SNP_RS","DB_SNP_VAL_STATUS","CANONICAL_TRANSCRIPT","KEYWORD") VALUES (2038,672,'17',41244748,41244748,'G','A','Q934*','Nonsense_Mutation','37','+','SNP','rs80357223','unknown',1,'BRCA1 truncating');
INSERT INTO "mutation_event" ("MUTATION_EVENT_ID","ENTREZ_GENE_ID","CHR","START_POSITION","END_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","PROTEIN_CHANGE","MUTATION_TYPE","NCBI_BUILD","STRAND","VARIANT_TYPE","DB_SNP_RS","DB_SNP_VAL_STATUS","CANONICAL_TRANSCRIPT","KEYWORD") VALUES (22604,672,'17',41258504,41258504,'A','C','C61G','Missense_Mutation','37','+','SNP','rs28897672','byCluster',1,'BRCA1 C61 missense');
INSERT INTO "mutation_event" ("MUTATION_EVENT_ID","ENTREZ_GENE_ID","CHR","START_POSITION","END_POSITION","REFERENCE_ALLELE","TUMOR_SEQ_ALLELE","PROTEIN_CHANGE","MUTATION_TYPE","NCBI_BUILD","STRAND","VARIANT_TYPE","DB_SNP_RS","DB_SNP_VAL_STATUS","CANONICAL_TRANSCRIPT","KEYWORD") VALUES (2039,672,'17',41276033,41276033,'C','T','C27_splice','Splice_Site','37','+','SNP','rs80358010','byCluster',1,'BRCA1 truncating');

-- mutation
INSERT INTO "mutation" ("MUTATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","ENTREZ_GENE_ID","CENTER","SEQUENCER","MUTATION_STATUS","VALIDATION_STATUS","TUMOR_SEQ_ALLELE1","TUMOR_SEQ_ALLELE2","MATCHED_NORM_SAMPLE_BARCODE","MATCH_NORM_SEQ_ALLELE1","MATCH_NORM_SEQ_ALLELE2","TUMOR_VALIDATION_ALLELE1","TUMOR_VALIDATION_ALLELE2","MATCH_NORM_VALIDATION_ALLELE1","MATCH_NORM_VALIDATION_ALLELE2","VERIFICATION_STATUS","SEQUENCING_PHASE","SEQUENCE_SOURCE","VALIDATION_METHOD","SCORE","BAM_FILE","TUMOR_ALT_COUNT","TUMOR_REF_COUNT","NORMAL_ALT_COUNT","NORMAL_REF_COUNT") VALUES (2038,6,6,672,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','G','A','TCGA-A1-A0SH-10A-03D-A099-09','G','A','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1);
INSERT INTO "mutation" ("MUTATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","ENTREZ_GENE_ID","CENTER","SEQUENCER","MUTATION_STATUS","VALIDATION_STATUS","TUMOR_SEQ_ALLELE1","TUMOR_SEQ_ALLELE2","MATCHED_NORM_SAMPLE_BARCODE","MATCH_NORM_SEQ_ALLELE1","MATCH_NORM_SEQ_ALLELE2","TUMOR_VALIDATION_ALLELE1","TUMOR_VALIDATION_ALLELE2","MATCH_NORM_VALIDATION_ALLELE1","MATCH_NORM_VALIDATION_ALLELE2","VERIFICATION_STATUS","SEQUENCING_PHASE","SEQUENCE_SOURCE","VALIDATION_METHOD","SCORE","BAM_FILE","TUMOR_ALT_COUNT","TUMOR_REF_COUNT","NORMAL_ALT_COUNT","NORMAL_REF_COUNT") VALUES (22604,6,6,672,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','A','C','TCGA-A1-A0SH-10A-03D-A099-09','A','C','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1);
INSERT INTO "mutation" ("MUTATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","ENTREZ_GENE_ID","CENTER","SEQUENCER","MUTATION_STATUS","VALIDATION_STATUS","TUMOR_SEQ_ALLELE1","TUMOR_SEQ_ALLELE2","MATCHED_NORM_SAMPLE_BARCODE","MATCH_NORM_SEQ_ALLELE1","MATCH_NORM_SEQ_ALLELE2","TUMOR_VALIDATION_ALLELE1","TUMOR_VALIDATION_ALLELE2","MATCH_NORM_VALIDATION_ALLELE1","MATCH_NORM_VALIDATION_ALLELE2","VERIFICATION_STATUS","SEQUENCING_PHASE","SEQUENCE_SOURCE","VALIDATION_METHOD","SCORE","BAM_FILE","TUMOR_ALT_COUNT","TUMOR_REF_COUNT","NORMAL_ALT_COUNT","NORMAL_REF_COUNT") VALUES (2039,6,12,672,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1);

-- sample_list
INSERT INTO "sample_list" ("LIST_ID","STABLE_ID","CATEGORY","CANCER_STUDY_ID","NAME","DESCRIPTION") VALUES (1,'study_tcga_pub_all','other',1,'All Tumors','All tumor samples (14 samples)');
INSERT INTO "sample_list" ("LIST_ID","STABLE_ID","CATEGORY","CANCER_STUDY_ID","NAME","DESCRIPTION") VALUES (2,'study_tcga_pub_acgh','other',1,'Tumors aCGH','All tumors with aCGH data (778 samples)');
INSERT INTO "sample_list" ("LIST_ID","STABLE_ID","CATEGORY","CANCER_STUDY_ID","NAME","DESCRIPTION") VALUES (3,'study_tcga_pub_cnaseq','other',1,'Tumors with sequencing and aCGH data','All tumor samples that have CNA and sequencing data (482 samples)');
INSERT INTO "sample_list" ("LIST_ID","STABLE_ID","CATEGORY","CANCER_STUDY_ID","NAME","DESCRIPTION") VALUES (4,'study_tcga_pub_complete','other',1,'Complete samples (mutations,copy-number,expression)','Samples with complete data (463 samples)');
INSERT INTO "sample_list" ("LIST_ID","STABLE_ID","CATEGORY","CANCER_STUDY_ID","NAME","DESCRIPTION") VALUES (5,'study_tcga_pub_log2CNA','other',1,'Tumors log2 copy-number','All tumors with log2 copy-number data (778 samples)');
INSERT INTO "sample_list" ("LIST_ID","STABLE_ID","CATEGORY","CANCER_STUDY_ID","NAME","DESCRIPTION") VALUES (6,'study_tcga_pub_methylation_hm27','other',1,'Tumors with methylation data','All samples with methylation (HM27) data (311 samples)');
INSERT INTO "sample_list" ("LIST_ID","STABLE_ID","CATEGORY","CANCER_STUDY_ID","NAME","DESCRIPTION") VALUES (7,'study_tcga_pub_mrna','other',1,'Tumors with mRNA data (Agilent microarray)','All samples with mRNA expression data (526 samples)');
INSERT INTO "sample_list" ("LIST_ID","STABLE_ID","CATEGORY","CANCER_STUDY_ID","NAME","DESCRIPTION") VALUES (8,'study_tcga_pub_sequenced','other',1,'Sequenced Tumors','All sequenced samples (507 samples)');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (14,'acc_tcga_all','other',2,'All Tumors','All tumor samples');

-- sample_list_list
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,1);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,2);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,3);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,4);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,5);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,6);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,7);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,8);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,9);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,10);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,11);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,12);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,13);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (1,14);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,1);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,2);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,3);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,4);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,5);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,6);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,7);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,8);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,9);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,10);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,11);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,12);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,13);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (2,14);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (3,2);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (3,3);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (3,6);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (3,8);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (3,9);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (3,10);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (3,12);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (4,2);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (4,3);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (4,6);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (4,8);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (4,9);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (4,10);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (4,12);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,1);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,2);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,3);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,4);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,5);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,6);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,7);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,8);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,9);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,10);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,11);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,12);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,13);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (5,14);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (6,2);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (7,2);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (7,3);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (7,6);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (7,8);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (7,9);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (7,10);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (7,12);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (7,13);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (8,2);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (8,3);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (8,6);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (8,8);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (8,9);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (8,10);
INSERT INTO "sample_list_list" ("LIST_ID","SAMPLE_ID") VALUES (8,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (14,15);


-- sample_cna_event
INSERT INTO "sample_cna_event" ("CNA_EVENT_ID","SAMPLE_ID","GENETIC_PROFILE_ID") VALUES (2774,4,2);
INSERT INTO "sample_cna_event" ("CNA_EVENT_ID","SAMPLE_ID","GENETIC_PROFILE_ID") VALUES (17280,12,2);
INSERT INTO "sample_cna_event" ("CNA_EVENT_ID","SAMPLE_ID","GENETIC_PROFILE_ID") VALUES (26161,12,2);
INSERT INTO "sample_cna_event" ("CNA_EVENT_ID","SAMPLE_ID","GENETIC_PROFILE_ID") VALUES (17280,13,2);
INSERT INTO "sample_cna_event" ("CNA_EVENT_ID","SAMPLE_ID","GENETIC_PROFILE_ID") VALUES (26161,14,2);

-- alteration_driver_annotation
    -- from sample_cna_event
INSERT INTO "alteration_driver_annotation" ("ALTERATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","DRIVER_FILTER","DRIVER_FILTER_ANNOTATION","DRIVER_TIERS_FILTER","DRIVER_TIERS_FILTER_ANNOTATION") VALUES (2774,2,4,'Putative_Driver','Pathogenic','Tier 1','Highly Actionable');
INSERT INTO "alteration_driver_annotation" ("ALTERATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","DRIVER_FILTER","DRIVER_FILTER_ANNOTATION","DRIVER_TIERS_FILTER","DRIVER_TIERS_FILTER_ANNOTATION") VALUES (17280,2,12,'Putative_Driver','Pathogenic','Tier 1','Highly Actionable');
INSERT INTO "alteration_driver_annotation" ("ALTERATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","DRIVER_FILTER","DRIVER_FILTER_ANNOTATION","DRIVER_TIERS_FILTER","DRIVER_TIERS_FILTER_ANNOTATION") VALUES (26161,2,12,'Putative_Passenger','Pathogenic','Tier 2','Potentially Actionable');
INSERT INTO "alteration_driver_annotation" ("ALTERATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","DRIVER_FILTER","DRIVER_FILTER_ANNOTATION","DRIVER_TIERS_FILTER","DRIVER_TIERS_FILTER_ANNOTATION") VALUES (17280,2,13,'Putative_Passenger','Pathogenic','Tier 1','Highly Actionable');
INSERT INTO "alteration_driver_annotation" ("ALTERATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","DRIVER_FILTER","DRIVER_FILTER_ANNOTATION","DRIVER_TIERS_FILTER","DRIVER_TIERS_FILTER_ANNOTATION") VALUES (26161,2,14,'Putative_Driver','Pathogenic','Tier 2','Potentially Actionable');
    -- from mutation
INSERT INTO "alteration_driver_annotation" ("ALTERATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","DRIVER_FILTER","DRIVER_FILTER_ANNOTATION","DRIVER_TIERS_FILTER","DRIVER_TIERS_FILTER_ANNOTATION") VALUES (26161,6,6,'Putative_Driver','Pathogenic','Tier 1','Highly Actionable');
INSERT INTO "alteration_driver_annotation" ("ALTERATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","DRIVER_FILTER","DRIVER_FILTER_ANNOTATION","DRIVER_TIERS_FILTER","DRIVER_TIERS_FILTER_ANNOTATION") VALUES (22604,6,6,'Putative_Passenger','Pathogenic','Tier 2','Potentially Actionable');
INSERT INTO "alteration_driver_annotation" ("ALTERATION_EVENT_ID","GENETIC_PROFILE_ID","SAMPLE_ID","DRIVER_FILTER","DRIVER_FILTER_ANNOTATION","DRIVER_TIERS_FILTER","DRIVER_TIERS_FILTER_ANNOTATION") VALUES (2039,6,12,'Putative_Passenger','Pathogenic','Tier 1','Highly Actionable');

-- sample_profile
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (1,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (1,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (2,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (2,3,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (2,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (2,5,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (2,6,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (3,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (3,3,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (3,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (3,6,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (4,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (4,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (5,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (5,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (6,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (6,3,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (6,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (6,6,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (7,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (7,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (8,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (8,3,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (8,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (8,6,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (9,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (9,3,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (9,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (9,6,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (10,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (10,3,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (10,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (10,6,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (11,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (11,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (12,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (12,3,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (12,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (12,6,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (13,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (13,3,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (13,4,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (14,2,NULL);
INSERT INTO "sample_profile" ("SAMPLE_ID","GENETIC_PROFILE_ID","PANEL_ID") VALUES (14,4,NULL);

-- users
INSERT INTO users (EMAIL,NAME,ENABLED) values ('jami@gmail.com','Jami Bax',1);
INSERT INTO users (EMAIL,NAME,ENABLED) values ('Lonnie@openid.org','Lonnie Penaloza',0);
INSERT INTO users (EMAIL,NAME,ENABLED) values ('Dhorak@yahoo.com','Darryl Horak',1);

-- authorities
INSERT INTO authorities (EMAIL,AUTHORITY) values ('jami@gmail.com','ROLE_USER');
INSERT INTO authorities (EMAIL,AUTHORITY) values ('Lonnie@openid.org','ROLE_USER');
INSERT INTO authorities (EMAIL,AUTHORITY) values ('Dhorak@yahoo.com','ROLE_USER');
INSERT INTO authorities (EMAIL,AUTHORITY) values ('Dhorak@yahoo.com','ROLE_MANAGER');
