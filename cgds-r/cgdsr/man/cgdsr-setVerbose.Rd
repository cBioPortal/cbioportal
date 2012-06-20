\name{cgdsr-setVerbose}
\alias{cgdsr-setVerbose}
\alias{setVerbose}
\alias{setVerbose.CGDS}
\title{Set verbose logging level for CGDS function calls}
\description{Sets verbose logging level for CGDS function calls.}
\usage{\method{setVerbose}{CGDS}(x, verbose, ...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{verbose}{Activate verbose logging (boolean)}
  \item{...}{Not used.}
}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
 cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/public-portal/}
}
\seealso{
\code{\link{cgdsr}},\code{\link{CGDS}}
}
\examples{
# Create CGDS object
mycgds = CGDS("http://www.cbioportal.org/public-portal/")

getCancerStudies(mycgds)

# Activate verbose logging
setVerbose(mycgds, TRUE)

getCancerStudies(mycgds)

}
