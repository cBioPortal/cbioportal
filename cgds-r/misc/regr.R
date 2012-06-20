#!/home/ajac/apps/bin/Rscript --no-save
#options(warn=-99)

library('ggplot2')
library('cgdsapi')

srv = CGDS("http://cbio.mskcc.org/cgds-public/",FALSE)
#test(srv) # very simple test of the web service

cancertype = commandArgs(TRUE)[1]
gene1 = commandArgs(TRUE)[2]
gene2 = commandArgs(TRUE)[3]

datanames = data.frame(
  ovc = c('ova_all','ova_gistic','ova_mrna_median'),
  pca.primary = c('pca_primary','pca_cna','pca_mrna'),
  pca = c('pca_all','pca_cna','pca_mrna'))

#print(cancertype)
#print(colnames(data))

if (!(cancertype %in% colnames(datanames))) {
  print("error: cancertype not defined")
  return()
}

case.set = datanames[1,cancertype]
cna.type = datanames[2,cancertype]
mrna.type = datanames[3,cancertype]

###

vp.layout <- function(x, y) viewport(layout.pos.row=x, layout.pos.col=y)
arrange <- function(..., nrow=NULL, ncol=NULL, as.table=FALSE) {
 dots <- list(...)
 n <- length(dots)
 if(is.null(nrow) & is.null(ncol)) { nrow = floor(n/2) ; ncol = ceiling(n/nrow)}
 if(is.null(nrow)) { nrow = ceiling(n/ncol)}
 if(is.null(ncol)) { ncol = ceiling(n/nrow)}
        ## NOTE see n2mfrow in grDevices for possible alternative
grid.newpage()
pushViewport(viewport(layout=grid.layout(nrow,ncol) ) )
 ii.p <- 1
 for(ii.row in seq(1, nrow)){
 ii.table.row <- ii.row 
 if(as.table) {ii.table.row <- nrow - ii.table.row + 1}
  for(ii.col in seq(1, ncol)){
   ii.table <- ii.p
   if(ii.p > n) break
   print(dots[[ii.table]], vp=vp.layout(ii.table.row, ii.col))
   ii.p <- ii.p + 1
  }
 }
}

# expression correlation of gene1 versus gene2, controlling for CNA status of gene2
# plot1: gene1 vs gene2 expression scatter
# plot2: gene2 CNA status
# plot3: series of plots,  gene1 vs gene2 expression scatter stratified by gene2 CNA status

x1=data.frame(getProfileData(srv,c(gene1),mrna.type,case.set))
x=data.frame(t(x1[,-c(1,2)]))

x2=data.frame(getProfileData(srv,c(gene2),mrna.type,case.set))
x=cbind(x,data.frame(t(x2[,-c(1,2)])))

cna=data.frame(getProfileData(srv,gene2,cna.type,case.set))
cna=data.frame(t(cna[,-c(1,2)]))
x=cbind(x,cna)

names(x) = c('gene1','gene2','gene2.CNA')

x = x[!is.na(x[,3]),]# remove samples with NA

theme_set(theme_bw())

gene1vsgene2.scatter <- qplot(data=x, x=gene1, y=gene2, alpha=0.3,size=2) + 
  stat_smooth(method='lm',se=FALSE,size=1,color='red') +
  xlab(paste(gene1,'expression'))+ylab(paste(gene2,'expression')) + opts(legend.position = "none");

gene2CNA.boxplot <- qplot(data=x, x=factor(gene2.CNA), y=gene2,alpha=0) +
  geom_boxplot(alpha=1,outlier.size=0,outlier.colour='white') +
  geom_jitter(size=2,alpha=0.3,position=position_jitter(width=0.2)) +
  xlab(paste(gene2,'CNA status')) + ylab(paste(gene2,'expression')) + opts(legend.position = "none");

gene1vsgene2.CNA.scatter <- qplot(data=x, x=gene1, y=gene2,alpha=0.3,size=2) + 
  stat_smooth(method='lm',se=FALSE,size=1,color='red') + facet_wrap(~ gene2.CNA) +
  xlab(paste(gene1,'expression'))+ylab(paste(gene2,'expression')) + opts(legend.position = "none");


pearson.test = cor.test(x$gene1,x$gene2)
regr.test = summary(lm(gene2 ~ gene2.CNA + gene1, data = x))

print(pearson.test)
print(regr.test)

pdf(paste(cancertype,'-',gene1,'-',gene2,'.pdf',sep=""))
arrange(gene1vsgene2.scatter,gene2CNA.boxplot,gene1vsgene2.CNA.scatter,ncol=2)
gp1=gpar(col="black", fontsize=8)
px = 0.55; py = 0.45; offset = 0.03;
grid.text(paste("Pearson r = ",sprintf("%.2f",pearson.test$estimate),", p-value = ",sprintf("%.1e",pearson.test$p.value)),px,py,gp=gp1,just='left')
grid.text("Regression : ",px,py-1*offset,gp=gp1,just='left')
grid.text(paste('CNA : z = ', sprintf("%.2f",regr.test$coefficients[2,3]),', p-value = ', sprintf("%.1e",regr.test$coefficients[2,4])),px,py-offset*2,gp=gp1,just='left')
grid.text(paste(gene1, ' : z = ', sprintf("%.2f",regr.test$coefficients[3,3]),', p-value = ', sprintf("%.1e",regr.test$coefficients[3,4])),px,py-offset*3,gp=gp1,just='left')
dev.off()
