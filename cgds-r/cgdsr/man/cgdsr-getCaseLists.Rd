\name{cgdsr-getCaseLists}
\alias{cgdsr-getCaseLists}
\alias{getCaseLists}
\alias{getCaseLists.CGDS}
\title{Get available case lists for a specific cancer study
}
\description{Queries the CGDS API and returns available case lists for a
  specific cancer study.
}
\usage{\method{getCaseLists}{CGDS}(x,cancerStudy,...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{cancerStudy}{cancer study ID (required)}
  \item{...}{Not used.}
}
\value{A data.frame with five columns:
\enumerate{
\item \var{case_list_id}:  a unique ID used to identify the case list ID in
subsequent interface calls.  This is a human readable ID.  For example,
"gbm_tcga_all" identifies all cases profiles in the TCGA GBM study.
\item \var{case_list_name}:  short name for the case list.
\item \var{case_list_description}:  short description of the case list.
\item \var{cancer_study_id}:  cancer study ID tied to this genetic profile.  Will
match the input cancer_study_id.
\item \var{case_ids}:  space delimited list of all case IDs that make up this case list.
}
}
\details{Queries the CGDS API and returns available case lists for a
  specific cancer study. For example, a within a particular study, only
  some cases may have sequence data, and another subset of cases may
  have been sequenced and treated with a specific therapeutic protocol.  Multiple 
  case lists may be associated with each cancer study, and this method
  enables you to retrieve meta-data regarding all of these case lists.
}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
 cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/}
}
\seealso{
\code{\link{cgdsr}},\code{\link{CGDS}},\code{\link{getCancerStudies}},\code{\link{getGeneticProfiles}},\code{\link{getProfileData}}
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
}
