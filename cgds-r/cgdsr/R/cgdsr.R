library(R.oo);

setConstructorS3("CGDS", function(url='',verbose=FALSE,ploterrormsg='') {
  extend(Object(), "CGDS",
         .url=url,
         .verbose=verbose,
         .ploterrormsg='')
})

setMethodS3("processURL","CGDS", private=TRUE, function(x, url, ...) {
  if (x$.verbose) cat(url,"\n")
  df = read.table(url, skip=0, header=TRUE, as.is=TRUE, sep="\t") 
})

setMethodS3("setPlotErrorMsg","CGDS", function(x, msg, ...) {
  x$.ploterrormsg = msg
  return(msg)
})

setMethodS3("setVerbose","CGDS", function(x, verbose, ...) {
  x$.verbose = verbose
  return(verbose)
})

setMethodS3("getCancerStudies","CGDS", function(x, ...) {
  url = paste(x$.url, "webservice.do?cmd=getCancerStudies&",sep="")
  df = processURL(x,url)
  return(df)
})

setMethodS3("getCaseLists","CGDS", function(x, cancerStudy, ...) {
  url = paste(x$.url, "webservice.do?cmd=getCaseLists&cancer_study_id=", cancerStudy, sep="")
  df = processURL(x,url)
  return(df)
})

setMethodS3("getGeneticProfiles","CGDS", function(x, cancerStudy, ...) {
  url = paste(x$.url, "webservice.do?cmd=getGeneticProfiles&cancer_study_id=", cancerStudy, sep="")
  df = processURL(x,url)
  return(df)
})

setMethodS3("getMutationData","CGDS", function(x, caseList, geneticProfile, genes, ...) {
  url = paste(x$.url, "webservice.do?cmd=getMutationData",
    "&case_set_id=", caseList,
    "&genetic_profile_id=", geneticProfile,
    "&gene_list=", paste(genes,collapse=","), sep="")
  df = processURL(x,url)
  return(df)
})

setMethodS3("getProfileData","CGDS", function(x, genes, geneticProfiles, caseList='', cases=c(), caseIdsKey = '', ...) {
  url = paste(x$.url, "webservice.do?cmd=getProfileData",
    "&gene_list=", paste(genes,collapse=","),
    "&genetic_profile_id=", paste(geneticProfiles,collapse=","),
    "&id_type=", 'gene_symbol',
    sep="")

  if (length(cases)>0) { url = paste(url,"&case_list=", paste(cases,collapse=","),sep='')
  } else if (caseIdsKey != '') { url = paste(url,"&case_ids_key=", caseIdsKey,sep='')
  } else { url = paste(url,"&case_set_id=", caseList,sep='') }
  
  df = processURL(x,url)

  if (nrow(df) == 0) { return(df) }
  
  m = matrix()
  # process data before returning
  if (length(geneticProfiles) > 1) {
    cnames = df[,1]
    m = t(df[,-c(1:4)])
    colnames(m) = cnames
  } else {
    cnames = df[,2]
    m = t(df[,-c(1:2)])
    colnames(m) = cnames
  }
  
  return(data.frame(m))
})

setMethodS3("getClinicalData","CGDS", function(x, caseList='', cases=c(), caseIdsKey = '', ...) {
  url = paste(x$.url, "webservice.do?cmd=getClinicalData",sep="")

  if (length(cases)>0) { url = paste(url,"&case_list=", paste(cases,collapse=","),sep='')
  } else if (caseIdsKey != '') { url = paste(url,"&case_ids_key=", caseIdsKey,sep='')
  } else { url = paste(url,"&case_set_id=", caseList,sep='') }
  
  df = processURL(x,url)
  rownames(df) = make.names(df$case_id)
  return(df[,-1])
})

setMethodS3("plot","CGDS", function(x, cancerStudy, genes, geneticProfiles, caseList='', cases=c(),  caseIdsKey = '', skin='cont', skin.normals='', skin.col.gp = c(), add.corr = '', legend.pos = 'topright', ...) {

  errormsg <- function(msg,error=TRUE) {
    # return empty plot with text
    if (error) {msg = paste('Error:',msg)}
    # override msg if global message provided in object
    if (x$.ploterrormsg != '') {msg = x$.ploterrormsg}
    plot.new()
    # set message text here ...
    #mtext(msg,cex=1.0,col='darkred')
    text(0.5,0.5,msg,cex=1.0,col='darkred')
    box()
    return(msg)
  }
    
  # we only allow the following combinations
  # a) gene1 (1 profile)  # b) gene1 vs gene2 (1 profile)
  # c) profile 1 vs profile 2 (1 gene)
  if((length(genes) > 1 & length(geneticProfiles) > 1) | (length(genes) > 2 | length(geneticProfiles) > 2)) {
    return(errormsg("use only 2 genetic profiles OR 2 genes"))
  }
  
  # make genenames conform to R variable names
  genesR = make.names(genes)
  
  # get data, check more than zero rows returned, otherwise return
  df = getProfileData(x, genes, geneticProfiles, caseList, cases, caseIdsKey)
  if (nrow(df) == 0) { return(errormsg(paste('empty data frame returned :\n',colnames(df)[1]))) }

  # check data returned with more than two genes or genetic profiles
  if (length(genes) == 2 & ncol(df) != 2) { return(errormsg(paste("gene not found:", setdiff(genesR,colnames(df))))) }
  if (length(geneticProfiles) == 2 & ncol(df) != 2) { return(errormsg("geneticProfile ID not found:", setdiff(geneticProfiles,colnames(df)))) }
  
  # get geneticProfiles annotation for axis labels
  gps = getGeneticProfiles(x, cancerStudy)
  if (nrow(gps) == 0) { errormsg(colnames(gps[1])) }
  rownames(gps) = gps[,1]
  gpaxisnames = gps[geneticProfiles,'genetic_profile_name']
  names(gpaxisnames) = geneticProfiles

  # we can have a situation where there is no data for a given combination of gene and genetic profile
  # in this case, we have a column of NaN, and we generate an error
  nacols = sapply(df, function(x) all(is.nan(x)))
  if (any(nacols)) {
    if (length(geneticProfiles) > 1) {
      # two genetic profiles in columns, one gene
      return(errormsg(paste(genes, "has no data\n for genetic profile(s):", paste(gpaxisnames[nacols],collapse=", ")),FALSE))
    } else {
      # one genetic profile, one or two genes in columns
      return(errormsg(paste(paste(genes[nacols],collapse=' and '), "has no data\n for genetic profile:", gpaxisnames),FALSE))
    }
  }

  # set sub title with correlation if specified
  plot.subtitle = ''
  if ((add.corr == 'pearson'  | add.corr == 'spearman') & ncol(df) == 2) {    
    ct = cor.test(df[,1],df[,2],method=add.corr)
    plot.subtitle = paste(add.corr, ' r = ', sprintf("%.2f",ct$estimate), ', p = ',sprintf("%.1e",ct$p.value))
  }
  
  ###
  ### Skins
  ###

  if (skin == 'cont') {

    if(length(genes) == 1 & length(geneticProfiles) == 1) {
      hist(df[,1],xlab=paste(genes," , ",gpaxisnames,sep=""),main='')
    } else if (length(genes) == 2) {
      # two genes
      plot(df[,genesR[1]],df[,genesR[2]] , main = '', xlab = paste(genes[1],", ",gpaxisnames,sep=""), ylab = paste(genes[2],", ",gpaxisnames,sep=""), pch = 1, col = 'black', sub = plot.subtitle)
    } else {
      # two genetic profiles
      gpa = geneticProfiles[1]
      gpb = geneticProfiles[2]
      plot(df[,gpa],df[,gpb] , main = '', xlab = paste(genes[1],", ",gpaxisnames[gpa],sep=""), ylab = paste(genes[1],", ",gpaxisnames[gpb],sep=""), pch = 1, col = 'black', sub = plot.subtitle)
    }
    
  } else if (skin == 'disc') {

    if(length(genes) == 1 & length(geneticProfiles) == 1) {
      barplot(table(df[,1]),xlab=paste(genes,", ",gpaxisnames,sep=""),main='',ylab='frequency')
    } else {
      #discrete vs discrete
      return(errormsg('discrete vs. discrete data not implemented'))
    }
    
  } else if (skin =='disc_cont') {

    # skin only valid for two genetic profiles
    if(length(geneticProfiles) != 2) {
      return(errormsg("two genetic profiles required for skin 'disc_cont'"))
    } else {
      # skin assumes that first genetic profile is discret
      gp.disc = geneticProfiles[1] # b
      gp.cont = geneticProfiles[2] # a
          
      boxplot(df[,gp.cont] ~ df[,gp.disc], outpch = NA, main = '', xlab = paste(genes[1],", ",gpaxisnames[gp.disc],sep=""), ylab = paste(genes[1],", ",gpaxisnames[gp.cont],sep=""), border = 'gray', sub = plot.subtitle)
      stripchart(df[,gp.cont] ~ df[,gp.disc],vertical = TRUE, add = TRUE, method = 'jitter', pch = 1, col = 'black')
    }
    
  } else if (skin == 'cna_mrna_mut') {

    # skin uses parameters
    # * skin.col.gp = mut
    # * skin.normals = normal_case_set [optional]

    # fetch optional normal mRNA data

    if (skin.normals != '') {
      df.norm = getProfileData(x, genes, geneticProfiles[2], skin.normals)
      if (nrow(df.norm) == 0) { return(errormsg(paste('empty data frame returned :\n',colnames(df.norm)[1]))) }
      # check if data is missing (NaN)
      if ( !all(is.nan(df.norm[,1])) ) {
        # add normal data to dataframe
        df.norm2 = cbind(rep(-3,nrow(df.norm)),df.norm[,1])
        colnames(df.norm2) = geneticProfiles
        df = rbind(df,df.norm2)
      }
    }
    
    # create boxplot
    df.nona = df[apply(df, 1, function(x) {!any(is.na(x))}),]
    ylim=range(df.nona[,geneticProfiles[2]],na.rm=TRUE)
    labels=seq(-3,2)
    names(labels)=c("Normal","Homdel","Hetloss","Diploid","Gain","Amp")
    labels.inuse = sort(unique(df.nona[,geneticProfiles[1]])) # sort removes any NA

    boxplot(df.nona[,geneticProfiles[2]] ~ df.nona[,geneticProfiles[1]], main='', outline=FALSE,
            xlab = paste(genes,", ",gpaxisnames[geneticProfiles[1]],sep=""),
            ylab = paste(genes,", ",gpaxisnames[geneticProfiles[2]],sep=""),
            border="gray",ylim=ylim, axes=FALSE, outpch = NA, sub = plot.subtitle)

    axis(1,at=seq(1,length(labels.inuse)),labels=names(labels)[match(labels.inuse,labels)],cex.axis=0.8)
    axis(2,cex.axis=0.8,las=2)
    # box()
    
    # manually jitter data

    # order data by CNA status
    df.nona=df.nona[order(df.nona[,geneticProfiles[1]]),]
    xy=list()
    xy$MRNA=df.nona[,geneticProfiles[2]]

    cats=cbind(1:length(labels.inuse),as.data.frame(table(df.nona[,geneticProfiles[1]])))
    colnames(cats)=c("X","Class","Count")
    rownames(cats)=names(labels)[match(cats[,2],labels)]

    xy$JITTER = unlist( apply(cats,1, function(cc) {
      y=rep.int(as.numeric(cc[1]),as.numeric(cc[3]))
      y=y+stats::runif(length(y),-0.1,0.1)
    }))
    xy=as.data.frame(xy)
    colnames(xy) = c('MRNA','JITTER')
    
    # Initialize plotting features
    nonmut.pch = 4
    N=nrow(xy)
    cex=rep(0.9,N)
    pch=rep(nonmut.pch,N)
    col=rep("royalblue",N)
    bg=rep(NA,N)

    # fetch mutation data for color coding    
    if (length(skin.col.gp == 1)) {
      df.mut = getProfileData(x, genes, skin.col.gp, caseList, cases, caseIdsKey)
      if (nrow(df.mut) == 0) { return(errormsg(paste('empty data frame returned :\n',colnames(df.mut)[1]))) }
      # get matrix corresponding to df.nona
      df.mut = df.mut[rownames(df.nona),1]
      # check if data is missing (NaN)
      mut=which(!is.na(df.mut))
      if(length(mut)>0) {
        # default mutation
        col[mut]="red3" 
        bg[mut]="goldenrod"
        pch[mut]=21
        mt=list()
        mt$pch=c(21,23,24,25,22)
        mt$type=c("missense","nonsense","splice","shift","in_frame")
        mt$pattern=c("^[a-z][0-9]+[a-z]$","[*x]$","^(e.+[0-9]|.+_splice)$","fs$","del$")
        mt$bg=c("goldenrod","darkblue","darkgray","black","goldenrod")
        mt=as.data.frame(mt)
        for(i in 1:nrow(mt)) {
          idx=grep(mt$pattern[i],tolower(df.mut))
          pch[idx]=mt$pch[i]
          bg[idx]=as.character(mt$bg[i])
        }
        #col[mut]="red3" # ??????
        legend("topleft",bty="n",
               as.character(as.vector(mt[["type"]])),col="red3",
               pt.bg=as.character(as.vector(mt[["bg"]])),
               pch=mt[["pch"]],cex=0.85,pt.cex=1.0
               )
      }
    }

    ## # Plot the jittered data, add mutated points last
    xy.mut = (pch != nonmut.pch)
    points(xy$JITTER[!xy.mut],xy$MRNA[!xy.mut],pch=pch[!xy.mut],cex=cex[!xy.mut],col=col[!xy.mut],bg=bg[!xy.mut])
    points(xy$JITTER[xy.mut],xy$MRNA[xy.mut],pch=pch[xy.mut],cex=cex[xy.mut],col=col[xy.mut],bg=bg[xy.mut])
    box()
    
  } else if (skin == 'meth_mrna_cna_mut' | skin == 'cna_mut') {

    # these skins use parameters
    # * skin.col.gp = (cna,mut) [optional]
    # * skin.normals = normal_case_set [optional]
    # [meth_mrna_cna_mut] forces x axis range to [0,1.05]
        
    # fetch cna and mut data for color coding
    pch=rep(1,nrow(df))
    col=rep("black",nrow(df))
    
    if (length(skin.col.gp) == 2) { # color by both CNA and mutation
        df.col = getProfileData(x, genes, skin.col.gp, caseList, cases, caseIdsKey)
      if (nrow(df.col) == 0) { return(errormsg(paste('empty data frame returned :\n',colnames(df.col)[1]))) }
      # because mut is text vector, we need to transform cna to integer vector instead of factor
      cna = as.integer(as.vector(df.col[,skin.col.gp[1]]))
      mut = as.vector(df.col[,skin.col.gp[2]])
      col[cna==-2]="darkblue"
      col[cna==-1]="deepskyblue"
      col[cna==1]="hotpink"
      col[cna==2]="red3"
      col[mut!="NaN"]="orange"
      pch[mut!="NaN"]=20
    }
    else if (length(skin.col.gp) == 1) { # color only by CNA
      df.col = getProfileData(x, genes, skin.col.gp, caseList, cases, caseIdsKey)
      if (nrow(df.col) == 0) { return(errormsg(paste('empty data frame returned :\n',colnames(df.col)[1]))) }
      # because mut is text vector, we need to transform cna to integer vector instead of factor
      cna = as.integer(as.vector(df.col[,1]))
      col[cna==-2]="darkblue"
      col[cna==-1]="deepskyblue"
      col[cna==1]="hotpink"
      col[cna==2]="red3"
    }
    
    # fetch optional normal methylation and mRNA data
    if (skin.normals != '') {
      df.norm = getProfileData(x, genes, geneticProfiles, skin.normals)
      if (nrow(df.norm) == 0) { return(errormsg(paste('empty data frame returned :\n',colnames(df.norm)[1]))) }
      # remove missing data
      df.norm = df.norm[apply(df.norm, 1, function(x) {!any(is.na(x))}),]
      if ( length(df.norm) > 0) {
        df = rbind(df,df.norm)
        col = append(col, rep("black",nrow(df.norm)))
        pch = append(pch, rep(20,nrow(df.norm)))
      }
    }

    xlim = range(df[,geneticProfiles[1]],na.rm = TRUE)
    # add 15% to range, to make room for legend
    if (legend.pos == 'topright') {
      xlim = c(min(xlim),max(xlim) + (max(xlim)-min(xlim))*0.15)
    }
    else if (legend.pos == 'topleft') {
      xlim = c(min(xlim) - (max(xlim)-min(xlim))*0.15,max(xlim))
    }
      
    if (skin == 'meth_mrna_cna_mut') {
      # force x axis range to [0,1.05] 
      xlim = c(0,1.05)
    }

    # now plot
    plot( df[,geneticProfiles[1]],df[,geneticProfiles[2]],main="",
         xlab=paste(genes,", ",gpaxisnames[geneticProfiles[1]],sep=""),
         ylab=paste(genes,", ",gpaxisnames[geneticProfiles[2]],sep=""),
         xlim=xlim,pch=pch,col=col,cex=1.2, sub = plot.subtitle
         )

    #abline(lm(d$rna~d$methylation),col="red3",lty=2,lwd=1.5)
    #lines(loess.smooth(d$methylation,d$rna),col="darkgray",lwd=2)

    # Replace with dynamically created legend when time permits
    legend(legend.pos,bty="n",
           c("Homdel","Hetloss","Diploid","Gain","Amp","Mutated","Normal"),
           col=c('darkblue','deepskyblue','black','hotpink','red','orange','black'),
           pch=c(1,1,1,1,1,20,20),cex=0.85,pt.cex=1.0
           )    

  } else {
    return(errormsg(paste("unkown skin:",skin)))
  }

  return(TRUE)
  
})


setMethodS3("test","CGDS", function(x, ...) {
  checkEq = function(a,b) { if (identical(a,b)) "OK\n" else "FAILED!\n" }
  checkGrt = function(a,b) { if (a > b) "OK\n" else "FAILED!\n" }
  cancerstudies = getCancerStudies(x)
  cat('getCancerStudies... ',
      checkEq(colnames(cancerstudies),c("cancer_study_id","name","description")))
  ct = cancerstudies[2,1] # should be row 1 instead ...

  cat('getCaseLists (1/2) ... ',
      checkEq(colnames(getCaseLists(x,ct)),
              c("case_list_id","case_list_name",
                "case_list_description","cancer_study_id","case_ids")))
  cat('getCaseLists (2/2) ... ',
      checkEq(colnames(getCaseLists(x,'xxx')),
              'Error..Problem.when.identifying.a.cancer.study.for.the.request.'))

  cat('getGeneticProfiles (1/2) ... ',
      checkEq(colnames(getGeneticProfiles(x,ct)),
              c("genetic_profile_id","genetic_profile_name","genetic_profile_description",
                "cancer_study_id","genetic_alteration_type","show_profile_in_analysis_tab")))
  cat('getGeneticProfiles (2/2) ... ',
      checkEq(colnames(getGeneticProfiles(x,'xxx')),
              'Error..Problem.when.identifying.a.cancer.study.for.the.request.'))

  # clinical data
  # check colnames
  cat('getClinicalData (1/1) ... ',
      checkEq(colnames(getClinicalData(x,'gbm_tcga_all')),
              c("overall_survival_months","overall_survival_status","disease_free_survival_months",
                "disease_free_survival_status","age_at_diagnosis")))
  # check value of overall_survival_months
  #cat('getClinicalData (2/3) ... ',
  #    checkGrt(getClinicalData(x,'gbm_tcga_all')['TCGA.02.0080','overall_survival_months'], 89))
  # check cases parameter
  #cat('getClinicalData (3/3) ... ',
  #    checkGrt(getClinicalData(x,cases=c('TCGA-02-0080'))['TCGA.02.0080','overall_survival_months'], 89))
  
  # check one gene, one profile
  cat('getProfileData (1/7) ... ',
      checkEq(colnames(getProfileData(x,'NF1','gbm_tcga_mrna','gbm_tcga_all')),
              "NF1"))
  # check many genes, one profile
  cat('getProfileData (2/7) ... ',
      checkEq(colnames(getProfileData(x,c('MDM2','MDM4'),'gbm_tcga_mrna','gbm_tcga_all')),
              c("MDM2","MDM4")))
  # check one gene, many profile
  cat('getProfileData (3/7) ... ',
      checkEq(colnames(getProfileData(x,'NF1',c('gbm_tcga_mrna','gbm_tcga_mutations'),'gbm_tcga_all')),
              c('gbm_tcga_mrna','gbm_tcga_mutations')))
  # check 3 cases returns matrix with 3 columns
  cat('getProfileData (4/7) ... ',
      checkEq(rownames(getProfileData(x,'BRCA1','gbm_tcga_mrna',cases=c('TCGA-02-0001','TCGA-02-0003'))),
              make.names(c('TCGA-02-0001','TCGA-02-0003'))))
  # invalid gene names return empty data.frame
  cat('getProfileData (5/7) ... ',
      checkEq(nrow(getProfileData(x,c('NF10','NF11'),'gbm_tcga_mrna','gbm_tcga_all')),as.integer(0)))
  # invalid case_list_id returns error
  cat('getProfileData (6/7) ... ',
      checkEq(colnames(getProfileData(x,'NF1','gbm_tcga_mrna','xxx')),
              'Error..Problem.when.identifying.a.cancer.study.for.the.request.'))
  # invalid genetic_profile_id returns error
  cat('getProfileData (7/7) ... ',
    checkEq(colnames(getProfileData(x,'NF1','xxx','gbm_tcga_all')),
            'No.genetic.profile.available.for.genetic_profile_id...xxx.'))  
})
