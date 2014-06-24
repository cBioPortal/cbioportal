\name{cgdsr-getCancerStudies}
\alias{cgdsr-getCancerStudies}
\alias{getCancerStudies}
\alias{getCancerStudies.CGDS}
\title{Get available cancer studies available in CGDS}
\description{Queries the CGDS API and returns available cancer
  studies. Input is a CGDS object and output is a data.matrix with
  information regarding the different cancer studies.}
\usage{\method{getCancerStudies}{CGDS}(x, ...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{...}{Not used.}
}
\value{A data.frame with three colums: 
\enumerate{
\item \var{cancer_study_id}:  unique ID used to identify the cancer study in
subsequent interface calls.  This is a human readable ID.
\item \var{name}:  short name of the cancer type.
\item \var{description}:  short description of the cancer type, describing the
source of study.
}}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
 cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/}
}
\seealso{
\code{\link{cgdsr}},\code{\link{CGDS}},\code{\link{getGeneticProfiles}},\code{\link{getCaseLists}}
}
\examples{
# Create CGDS object
mycgds = CGDS("http://www.cbioportal.org/public-portal/")

getCancerStudies(mycgds)

# Get available case lists (collection of samples) for a given cancer study
mycancerstudy = getCancerStudies(mycgds)[2,1]
mycaselist = getCaseLists(mycgds,mycancerstudy)[1,1]

}
