\name{cgdsr-getGeneticProfiles}
\alias{cgdsr-getGeneticProfiles}
\alias{getGeneticProfiles}
\alias{getGeneticProfiles.CGDS}
\title{Get available genetic data profiles for a specific cancer study}
\description{Queries the CGDS API and returns the available genetic
  profiles, e.g. mutation or copy number profiles, stored about a
  specific cancer study.
}
\usage{\method{getGeneticProfiles}{CGDS}(x,cancerStudy,...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{cancerStudy}{cancer study ID (required)}
  \item{...}{Not used.}
}
\value{A data.frame with six columns:
\enumerate{
\item \var{genetic_profile_id}:  a unique ID used to identify the genetic profile ID
in subsequent interface calls.  This is a human readable ID.  For
example, "gbm_tcga_mutations" identifies the TCGA GBM mutation genetic profile.
\item \var{genetic_profile_name}:  short profile name.
\item \var{genetic_profile_description}:  short profile description.
\item \var{cancer_study_id}:  cancer study ID tied to this genetic profile.  Will
match the input cancer_study_id.
\item \var{genetic_alteration_type}:  indicates the profile type.  Will be one of:
MUTATION, MUTATION_EXTENDED, COPY_NUMBER_ALTERATION, MRNA_EXPRESSION.
\item \var{show_profile_in_analysis_tab}:  a boolean flag used for internal purposes
(you can safely ignore it).
}
}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
 cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/}
}
\seealso{
\code{\link{cgdsr}},\code{\link{CGDS}},\code{\link{getCancerStudies}},\code{\link{getCaseLists}},\code{\link{getProfileData}}
}
\examples{
# Create CGDS object
mycgds = CGDS("http://www.cbioportal.org/public-portal/")

# Get list of cancer studys at server
getCancerStudies(mycgds)

# Get available case lists (collection of samples) for a given cancer study  
mycancerstudy = getCancerStudies(mycgds)[2,1]
mycaselist = getCaseLists(mycgds,mycancerstudy)[1,1]

# Get available genetic profiles
mygeneticprofile = getGeneticProfiles(mycgds,mycancerstudy)[4,1]

# Get data slices for a specified list of genes, genetic profile and case list
getProfileData(mycgds,c('BRCA1','BRCA2'),mygeneticprofile,mycaselist)
}
