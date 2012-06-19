# package.skeleton(name="cgdsr",path="~/work/cgds/pkg/")

### Status
#  x       getCancerTypes=function()
#  x       getGeneticProfiles=function(cancerTypeId)
#  x       getCaseLists(cancerTypeId)
#  x       getProfileData=function(geneList, geneticProfileId, caseSetId) {}
## Not implemented
# multiple calls ... single gene. getProfileData should handle this, Error when multiple genes and profiles.
# getProfileDataBatch=function(geneList, geneticProfileIdList, caseSetId) {}
# getMutationData(geneList, geneticProfileId, caseSetId)

## TODO:
# see if we can set baseURL as an object/class variable
# look at error for testUrl, should be fine ...
# getMutationData(geneList, geneticProfileId, caseSetId)

### Questions,
# * Should we do some kind of unit testing?
#   If done properly, this would require some a static working URL
#   and static meaningful types for some of the functions
# * Some of the more specific functions, i.e. getSomaticMutationFrequency,
#   probably requires static types ...

# Building
#> R CMD check cgdsr
#> R CMD build cgdsr

# Installing

#> R CMD INSTALL cgdsr_1.0.1.tar.gz

#detach(package:cgdsr)
library('cgdsr')
ovc = CGDS("http://cbio.mskcc.org/cgds-public-ovarian/")
cgdspub = CGDS("http://cbio.mskcc.org/cgds-public/")
test(ovc) # very simple test of the web service
test(cgdspub)
#help(cgdsr)
help(test)

getCaseLists(ovc,'ova')
getGeneticProfiles(ovc,'ova')
x=getProfileData(ovc,c('BRCA1','BRCA2'),'ova_mrna_median','ova_all')
y=getProfileData(ovc,'BRCA1',c('ova_mrna_median','ova_gistic'),'ova_all') # not implemented at the ova portal yet

getCancerTypes(cgdspub)
getCaseLists(cgdspub,'pca')
getGeneticProfiles(cgdspub,'pca')
x2=getProfileData(cgdspub,c('BRCA1','BRCA2'),'pca_mrna','pca_all') # works
y2=getProfileData(cgdspub,'BRCA1',c('pca_cna','pca_mrna'),'pca_all') # works

#scp ../index.html cgdsr_1.0.1.targ.z cbio:public_html/cgdsr/

### Get top CNA loci in ovarian cancer

getCaseLists(ovc,'ova') # 'ova_all'
getGeneticProfiles(ovc,'ova')
getProfileData(ovc,c('BRCA1'),c('ova_rae'),'ova_all')
getProfileData(ovc,c('BRCA1','BRCA4','BRCA2'),c('ova_rae'),'ova_all')


# test warnings and error messages

getCaseLists(mycgds,'xxx') # OK, error no case lists
getGeneticProfiles(mycgds,'xxx') # OK, error no genetic profiles
getProfileData(mycgds,'NF13','gbm_mrna','gbm_all') # ! No warnings that NF13 could not be found, we cant handle warnings properly
getProfileData(mycgds,'NF1','gbm_rna','gbm_all') # OK - error no genetic profiles
getProfileData(mycgds,'NF1','gbm_mrna','gbm_ll') # ! error




# Action items

# how could we get data for many genes ... 'gene lists: i.e. Proteins, miRNAs, ncRNAs, All ...'
# can we get CNA scores for individual loci ...

