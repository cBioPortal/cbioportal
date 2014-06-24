\name{cgdsr-test}
\alias{cgdsr-test}
\alias{test}
\alias{test.CGDS}
\title{Simple test suite for CGDS object.}
\description{Queries the CGDS API and returns results of the tests.}
\usage{\method{test}{CGDS}(x, ...)}
\arguments{
  \item{x}{A CGDS object.}
  \item{...}{Not used.}
}
\value{ Test results in text format.}
\details{ A set of simple tests are evaluated. The format of the
  returned output from the following queries are tested: "getCancerStudies()",
  "getCaselists()", and "getGeneticProfiles()"}
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

# Run tests
test(mycgds)
}
