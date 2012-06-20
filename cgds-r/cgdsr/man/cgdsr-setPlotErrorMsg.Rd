\name{cgdsr-setPlotErrorMsg}
\alias{cgdsr-setPlotErrorMsg}
\alias{setPlotErrorMsg}
\alias{setPlotErrorMsg.CGDS}
\title{Set custom plot error message}
\description{Sets custom plot error message.}
\usage{\method{setPlotErrorMsg}{CGDS}(x, msg, ...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{msg}{A custom message (string)}
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

# Set custom error plot message
setPlotErrorMsg(mycgds, 'My message ...')

getCancerStudies(mycgds)

}
