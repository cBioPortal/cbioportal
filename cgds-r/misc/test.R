setwd("/home/ajac/cvs/cgds-r/misc")

library('cgdsr')

## test miso DEV
c = CGDS("http://miso-dev.cbio.mskcc.org:38080/gdac-portal/")

pdf('test_brca_tp53.pdf')
plot(c,'brca','TP53',c('brca_mrna','brca_RPPA_protein_level'),'brca_basal',skin='cna_mut',skin.col.gp=c('brca_gistic','brca_mutations'))
plot(c,'brca','TP53',c('brca_mrna','brca_RPPA_protein_level'),'brca_basal',skin='cna_mut',skin.col.gp=c('brca_gistic','brca_mutations'),legend.pos='topleft')
plot(c,'brca','TP53',c('brca_mrna','brca_RPPA_protein_level'),'brca_basal',skin='cna_mut',skin.col.gp=c('brca_gistic','brca_mutations'),legend.pos='bottom')
plot(c,'brca','TP53',c('brca_mrna','brca_RPPA_protein_level'),'brca_basal',skin='cna_mut',skin.col.gp=c('brca_gistic','brca_mutations'),legend.pos='topleft',add.corr='pearson')
plot(c,'brca','TP53',c('brca_mrna','brca_RPPA_protein_level'),'brca_basal',skin='cna_mut',skin.col.gp=c('brca_gistic','brca_mutations'),legend.pos='topleft',add.corr='spearman')
plot(c,'brca','TP53',c('brca_mrna','brca_RPPA_protein_level'),'brca_luma',skin='cna_mut',skin.col.gp=c('brca_gistic','brca_mutations'),legend.pos='topleft',add.corr='pearson',)
plot(c,'brca','TP53',c('brca_mrna','brca_RPPA_protein_level'),'brca_lumb',skin='cna_mut',skin.col.gp=c('brca_gistic','brca_mutations'),legend.pos='topleft',add.corr='pearson')
dev.off()

# tests for manuscript

# Install R package and dependencies from CRAN
> install.packages("cgdsr")

# load library and establish connection to CGDS Web API
> library(cgdsr)
> mycgds =  CGDS("http://www.cbioportal.org/public-portal/")

# Browse CGDS-R package tutorial PDF
> vignette("cgdsr")

# Which cancer types are available?
> getCancerStudies(mycgds)[, c(1, 2)]

# Get DNA copy number and mRNA expression data for NF1 gene in GBM dataset
# first three sample shown
> getProfileData(mycgds, "NF1", c("gbm_cna_rae", "gbm_mrna"), "gbm_all")

# Use CGDS-R plot function to plot DNA CNA and mRNA exp. data for NF1
> plot(mycgds, "gbm", "NF1", c("gbm_cna_rae", "gbm_mrna"),
       "gbm_all", skin = "disc_cont")

# Scatter plot of MDM2 and MDM4 expression levels in GBM dataset
> plot(mycgds, "gbm", c("MDM2", "MDM4"), "gbm_mrna", "gbm_all")


####
####
####

# test public portal
c = CGDS("http://www.cbioportal.org/public-portal/")
test(c)

pdf('test_plots_public_portal.pdf')
plot(c, 'gbm_tcga', 'MDM2', 'gbm_tcga_mrna', 'gbm_tcga_all') # skin = 'cont'
plot(c, 'gbm_tcga', 'MDM2', 'gbm_tcga_mrna', 'gbm_tcga_all',skin = 'cont')
plot(c, 'ov_tcga', 'MDM2', 'ov_tcga_mrna_median', 'ov_tcga_all')
plot(c, 'ov_tcga', 'BRCA1', 'ov_tcga_mrna_median', cases=c('TCGA-61-2109','TCGA-61-2110','TCGA-61-2111'))
plot(c, 'gbm_tcga', 'MDM2', 'gbm_tcga_cna_rae', 'gbm_tcga_all', skin = 'disc')

plot(c, 'gbm_tcga', c('MDM2','MDM4'), 'gbm_tcga_mrna' , 'gbm_tcga_all')
#plot(c, 'ov_tcga', c('hsa-miR-29a','DNMT3A'), 'ov_tcga_mrna_median' , 'ov_tcga_all')

plot(c, 'gbm_tcga', c('NF1'), c('gbm_tcga_cna_rae','gbm_tcga_mrna'), 'gbm_tcga_all', skin = 'disc_cont')
plot(c, 'ov_tcga', c('BRCA2'), c('ov_tcga_gistic','ov_tcga_mrna_median'), 'ov_tcga_all', skin = 'disc_cont')
#plot(c, 'ov_tcga', c('hsa-miR-29a'), c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'disc_cont')

plot(c, 'ov_tcga', 'TP53', c('ov_tcga_gistic','ov_tcga_mrna_median'), 'ov_tcga_all', skin = 'cna_mrna_mut' , skin.col.gp='ov_tcga_mutations')

## plot(c, 'ov_tcga', 'BRCA2', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut')
## plot(c, 'ov_tcga', 'BRCA2', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.normals='ova_normal_mrna')
## plot(c, 'ov_tcga', 'CCNE1', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.normals='ova_normal_mrna')
## plot(c, 'ov_tcga', 'hsa-miR-29a', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.normals='ova_normal_mrna')
## plot(c, 'ov_tcga', 'BRCA2', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.col.gp='ova_mutations_next_gen')
## plot(c, 'ov_tcga', 'BRCA2', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.normals='ova_normal_mrna', skin.col.gp='ova_mutations_next_gen')
## plot(c, 'ov_tcga', 'TP53', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.col.gp='ova_mutations_next_gen')
## plot(c, 'ov_tcga', 'TP53', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.normals='ova_normal_mrna', skin.col.gp='ova_mutations_next_gen')
## plot(c, 'ov_tcga', 'hsa-miR-29a', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.col.gp='ova_mutations_next_gen')
## plot(c, 'ov_tcga', 'hsa-miR-29a', c('ova_rae','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mrna_mut', skin.normals='ova_normal_mrna', skin.col.gp='ova_mutations_next_gen')

## plot(c, 'ov_tcga', 'BRCA1', c('ova_methylation','ov_tcga_mrna_median'), 'ova_all', skin = 'meth_mrna_cna_mut', skin.col.gp=c('ova_rae','ova_mutations_next_gen'))
## plot(c, 'ov_tcga', 'BRCA1', c('ova_methylation','ov_tcga_mrna_median'), 'ova_all', skin = 'meth_mrna_cna_mut', skin.normals='ova_normal_mrna', skin.col.gp=c('ova_rae','ova_mutations_next_gen'))
## plot(c, 'ov_tcga', 'BRCA2', c('ova_methylation','ov_tcga_mrna_median'), 'ova_all', skin = 'meth_mrna_cna_mut', skin.col.gp=c('ova_rae','ova_mutations_next_gen'))
## plot(c, 'ov_tcga', 'BRCA2', c('ova_methylation','ov_tcga_mrna_median'), 'ova_all', skin = 'meth_mrna_cna_mut', skin.normals='ova_normal_mrna', skin.col.gp=c('ova_rae','ova_mutations_next_gen'))

# testcase for skin cna_mut, should not limit x axis to range 0-1, we also test switching axis
plot(c, 'ov_tcga', 'BRCA2', c('ova_methylation','ov_tcga_mrna_median'), 'ova_all', skin = 'cna_mut', skin.normals='ova_normal_mrna', skin.col.gp=c('ova_rae','ova_mutations_next_gen'))
plot(c, 'ov_tcga', 'BRCA2', c('ov_tcga_mrna_median','ova_methylation'), 'ova_all', skin = 'cna_mut', skin.normals='ova_normal_mrna', skin.col.gp=c('ova_rae','ova_mutations_next_gen'))

# testcase with no expression data for normal cases
plot(c, 'ov_tcga', 'BRCA1', c('ova_rae','ov_tcga_mrna_median_Zscores'), 'ova_all', skin = 'cna_mrna_mut', skin.normals='ova_normal_mrna')
# testcase with no expression data for normal cases
plot(c, 'ov_tcga', 'BRCA1', c('ova_methylation','ov_tcga_mrna_median_Zscores'), 'ova_all', skin = 'meth_mrna_cna_mut', skin.normals='ova_normal_mrna', skin.col.gp=c('ova_rae','ova_mutations_next_gen'))

# some tests of plotting with limited data
plot(c, 'ov_tcga', 'TP53', c('ov_tcga_rae','ov_tcga_mrna_median'), cases=c('TCGA-04-1331','TCGA-04-1336'), skin = 'cna_mrna_mut', skin.normals='ov_tcga_normal_mrna', skin.col.gp='ov_tcga_mutations')
plot(c, 'ov_tcga', 'TP53', c('ov_tcga_rae','ov_tcga_mrna_median'), cases=c('TCGA-04-1331','TCGA-04-1332','TCGA-04-1336'), skin = 'cna_mrna_mut', skin.col.gp='ov_tcga_mutations')
plot(c, 'ov_tcga', 'TP53', c('ov_tcga_rae','ov_tcga_mrna_median'), cases=c('TCGA-04-1331','TCGA-04-1336'), skin = 'cna_mrna_mut', skin.col.gp='ov_tcga_mutations')
plot(c, 'ov_tcga', 'TP53', c('ov_tcga_rae','ov_tcga_mrna_median'), cases=c('TCGA-04-1331'), skin = 'cna_mrna_mut', skin.col.gp='ov_tcga_mutations')

dev.off()

###
# get mutation data

getMutationData(c,'gbm_tcga_all','gbm_tcga_mutations',c('EGFR','PTEN'))

# sarcoma testcase with NAs in mRNA data
#plot(c, 'mskcc_broad_sarc', c('CBFA2T3'), c('Sarc_cna','Sarc_mrna'), 'Sarc_all', skin='cna_mrna_mut' , skin.col.gp=c('Sarc_mutations'), skin.normals='Sarc_normal')

####

# gbm test case
c = CGDS("http://www.cbioportal.org/public-portal/")
test(c)
setVerbose(c,1)
setPlotErrorMsg(c,"Some text here ...")

plot(c, 'gbm_tcga', c('EGFR'), c('gbm_tcga_gistic','gbm_tcga_mrna'), cases=c("TCGA-12-0772","TCGA-06-6700"), skin='cna_mrna_mut' , skin.col.gp=c('gbm_tcga_mutations'));

plot(c, 'gbm_tcga', c('EGFR'), c('gbm_tcga_gistic','gbm_tcga_mrna'), cases=c("TCGA-12-0772","TCGA-06-6700"), skin='cna_mrna_mut' , skin.col.gp='gbm_tcga_mutations');

plot(c, 'gbm_tcga', c('EGFR'), c('gbm_tcga_gistic'), cases=c("TCGA-12-0772","TCGA-06-6700"));

getProfileData(c, c('EGFR'), 'gbm_tcga_mutations', cases=c("TCGA-12-0772","TCGA-06-6700"))
getProfileData(c, c('EGFR'), 'gbm_tcga_mutations', 'gbm_tcga_all')

getProfileData(c, c('TP53'), 'ov_tcga_mutations', cases=c("TCGA-12-0772","TCGA-06-6700"))

####

pdf('test_firehose.pdf')
c = CGDS("http://buri.cbio.mskcc.org:38080/cgds_tcga_internal_portal/")
# test for mutations, splice site mutation (e...2) should not be detected different from missense (e23f)
plot(c, 'gbm_tcga', 'RANBP6', c('gbm_gistic','gbm_tcga_mrna_median_Zscores'), 'gbm_3way_complete', skin = 'cna_mrna_mut',skin.col.gp=c('gbm_mutations'))
dev.off()

# The following test should all raise errors

pdf('test_errors.pdf')

plot(c, 'gbm_tcga', 'MDM2', c('gbm_tcga_mrna','gbm_tcga_mrna'), 'gbm_tcga_all', skin = 'disc')
plot(c, 'gbm_tcga', c('MDM2','MDM2','MDM2'), 'gbm_tcga_mrna', 'gbm_tcga_all')
plot(c, 'gbm_tcga', c('MDM2','MDM2'), c('gbm_tcga_mrna','gbm_tcga_mrna'), 'gbm_tcga_all')

# unknown gene
plot(c, 'gbm_tcga', 'MDM12', 'gbm_tcga_mrna', 'gbm_tcga_all')

# unknown case set id
plot(c, 'gbm_tcga', 'MDM2', 'gbm_tcga_mrna', 'xxx')

# this should be a text error, has to be fixed at WEB API
#plot2(c, 'gbm_tcga', 'MDM12', c('gbm_tcga_mrna','gbm_tcga_mrna'), 'cont', 'gbm_tcga_all')

# wrong IDs
plot(c, 'gbm_tcga', 'DNMT3A', c('ova_ra','ov_tcga_mrna_median'), 'ova_all')
plot(c, 'gbm_tcga', c('hsa-miR-29A','DNMT3A'), 'ov_tcga_mrna_median', 'ova_all')
plot(c, 'gbm_tcga', c('DNMT3B','DNMT3D'), 'ov_tcga_mrna_median', 'ova_all')
plot(c, 'gbm_tcga', c('DNMT3C','DNMT3D'), 'ov_tcga_mrna_median', 'ova_all')

# wrong caseList
plot(c, 'gbm_tcga', 'MDM2', 'gbm_tcga_mrna', 'cont', 'gbm_tcga_alls')

# no data for combination of gene and genetic profile
plot(c,'ov_tcga',c('BRCA1','CCNE1'),'ova_methylation','ova_all')
plot(c,'ov_tcga','CCNE1',c('ov_tcga_mrna_median','ova_methylation'),'ova_all')
plot(c,'ov_tcga','CCNE1',c('ova_methylation'),'ova_all')
plot(c,'ov_tcga',c('CCNE1','hsa-miR-21'),c('ova_methylation'),'ova_all')

# unknown skin ID
plot(c, 'gbm_tcga', 'MDM2', 'gbm_tcga_mrna', 'gbm_tcga_all', skin = 'xxx')

dev.off()
