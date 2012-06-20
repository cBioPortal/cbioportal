\name{cgdsr-getCancerTypes}
\alias{cgdsr-getCancerTypes}
\alias{getCancerTypes}
\alias{getCancerTypes.CGDS}
\title{Get available cancer types available in CGDS}
\description{Queries the CGDS API and returns available cancer
  types. Input is a CGDS object and output is a data.matrix with
  information regarding the different cancer types.}
\usage{\method{getCancerTypes}{CGDS}(x, ...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{...}{Not used.}
}
\value{A data.frame with three colums: 
\enumerate{
\item \var{cancer_type_id}:  unique ID used to identify the cancer type in
subsequent interface calls.  This is a human readable ID.  For example,
"gbm" identifies the TCGA GBM data set.
\item \var{name}:  short name of the cancer type.
\item \var{description}:  short description of the cancer type, describing the
source of study.
}}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
 cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/cgx/}
}
\seealso{
\code{\link{cgdsr}},\code{\link{CGDS}},\code{\link{getGeneticProfiles}},\code{\link{getCaseLists}}
}
\examples{
# Create CGDS object
mycgds = CGDS("http://cbio.mskcc.org/cgds-public/")

getCancerTypes(mycgds)

# Get available case lists (collection of samples) for a given cancer type  
mycancertype = getCancerTypes(mycgds)[1,1]
mycaselist = getCaseLists(mycgds,mycancertype)[1,1]

}
