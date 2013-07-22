\name{cgdsr-getClinicalData}
\alias{cgdsr-getClinicalData}
\alias{getClinicalData}
\alias{getClinicalData.CGDS}
\title{Get clinical data for cancer study}
\description{Queries the CGDS API and returns clinical data for
  a given case list.}
\usage{\method{getClinicalData}{CGDS}(x, caseList, cases, caseIdsKey, ...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{caseList}{A case list ID}
  \item{cases}{A vector of case IDs}
  \item{caseIdsKey}{Only used by web portal.}
  \item{...}{Not used.}
}
\value{A data.frame with rows for each case, rownames corresponding to
  case IDs, and columns:
\enumerate{
\item \var{overall_survival_months}: Overall survival, in months.
\item \var{overall_survival_status}: Overall survival status, usually
indicated as "LIVING" or "DECEASED".
\item \var{disease_free_survival_months}: Disease free survival, in months.
\item \var{disease_free_survival_status}: Disease free survival status,
usually indicated as "DiseaseFree" or "Recurred/Progressed".
\item \var{age_at_diagnosis}: Age at diagnosis.
}}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
  cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/}
}
\seealso{
\code{\link{cgdsr}},\code{\link{CGDS}},\code{\link{getCaseLists}}
}
\examples{
# Create CGDS object
mycgds = CGDS("http://www.cbioportal.org/public-portal/")

getCancerStudies(mycgds)

# Get available case lists (collection of samples) for a given cancer study 
mycancerstudy = getCancerStudies(mycgds)[2,1]
mycaselist = getCaseLists(mycgds,mycancerstudy)[1,1]

# Get clinical data for caselist
getClinicalData(mycgds,mycaselist)

}
