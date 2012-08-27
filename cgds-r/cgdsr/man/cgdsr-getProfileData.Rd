\name{cgdsr-getProfileData}
\alias{cgdsr-getProfileData}
\alias{getProfileData}
\alias{getProfileData.CGDS}
\title{Retrieves genomic profile data for genes and genetic profiles.}
\description{Queries the CGDS API and returns data based on gene(s),
  genetic profile(s),  and a case list.}
\usage{\method{getProfileData}{CGDS}(x,genes,geneticProfiles,caseList,cases,caseIdsKey,...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{genes}{A vector of gene names or a String specifying a single gene (required)}
  \item{geneticProfiles}{ A vector of genetic profile IDs or String specifying
    a single genetic profile (required)}
  \item{caseList}{A case list ID}
  \item{cases}{A vector of case IDs)}
  \item{caseIdsKey}{Only used by web portal.}
  \item{...}{Not used.}
}
\value{

When requesting one or multiple genes and a single genetic profile,
the function returns a data.frame with genetic profile data in columns for each gene.

When requesting a single gene and multiple genetic profiles,
the function returns a data.frame containing columns with data for each genetic profile.

Cases can be specified either through a case list ID, or a vector of
case IDs.
}
\details{ Only one list is allowed, specify either a list of genes or
  genetic profiles. The format of the output data.frame depends on if
  a single or a list of genes was specified in the arguments.
}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
 cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/}
}
\seealso{
\code{\link{cgdsr}},\code{\link{CGDS}},\code{\link{getCancerStudies}},\code{\link{getGeneticProfiles}},\code{\link{getCaseLists}}
}
\examples{
# Create CGDS object
mycgds = CGDS("http://www.cbioportal.org/public-portal/")

# Get list of cancer studies at server
getCancerStudies(mycgds)

# Get available case lists (collection of samples) for a given cancer study  
mycancerstudy = getCancerStudies(mycgds)[2,1]
mycaselist = getCaseLists(mycgds,mycancerstudy)[1,1]

# Get available genetic profiles
mygeneticprofile = getGeneticProfiles(mycgds,mycancerstudy)[4,1]

# Get data slices for a specified list of genes, genetic profile and case list
getProfileData(mycgds,c('BRCA1','BRCA2'),mygeneticprofile,mycaselist)

# Get data slice for a single gene
getProfileData(mycgds,'HMGA2',mygeneticprofile,mycaselist)

# Get data slice for multiple genetic profiles and single gene
getProfileData(mycgds,'HMGA2',getGeneticProfiles(mycgds,mycancerstudy)[c(3,4),1],mycaselist)

# Get the same dataset from a vector of case IDs
cases = unlist(strsplit(getCaseLists(mycgds,mycancerstudy)[1,'case_ids'],' '))
getProfileData(mycgds,'HMGA2',getGeneticProfiles(mycgds,mycancerstudy)[c(3,4),1],cases=cases)

}
