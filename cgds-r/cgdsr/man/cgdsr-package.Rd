\name{cgdsr-package}
\alias{cgdsr-package}
\alias{cgdsr}
\docType{package}
\title{
CGDS-R : a library for accessing data in the MSKCC Cancer Genomics Data Server
(CGDS). 
}
\description{
The package provides a basic set of R functions for querying the Cancer
Genomics Data Server (CGDS), hosted by the Computational Biology Center
at Memorial-Sloan-Kettering Cancer Center (MSKCC). Read more about this
service at the cBio Cancer Genomics Portal,
\url{http://www.cbioportal.org/public-portal/}.
}
\details{
\tabular{ll}{
Package: \tab cgdsr\cr
Type: \tab Package\cr
License: \tab GPL\cr
LazyLoad: \tab yes\cr
}
The Cancer Genomic Data Server (CGDS) web service interface provides
direct programmatic access to all genomic data stored within the server.
This package provides a basic set of R functions for querying the CGDS
hosted by the Computational Biology Center at Memorial-Sloan-Kettering
Cancer Center (MSKCC).

The library can issue the following types of queries:

\enumerate{
\item \var{getCancerStudies()}: What cancer studies are hosted on the server?
For example TCGA Glioblastoma or TCGA Ovarian cancer.
\item \var{getGeneticProfiles()}: What genetic profile types are available for
cancer study X? For example mRNA expression or copy number alterations.
\item \var{getCaseLists()}: what case sets are available for cancer study X? For
example all samples or only samples corresponding to a given cancer
subtype.
\item \var{getProfileData()}: Retrieve slices of genomic data.  For
example, a client can retrieve all mutation data from PTEN and EGFR in
TCGA glioblastoma.
\item \var{getClinicalData()}: Retrieve clinical data (e.g. patient
survival time and age) for a given case list.
}
}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
  cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/}
}
\keyword{ package }

\seealso{
\code{\link{CGDS}}, \code{\link{getCancerStudies}},
\code{\link{getGeneticProfiles}}, \code{\link{getCaseLists}},
\code{\link{getProfileData}}, \code{\link{getClinicalData}}.
}

\examples{
# Create CGDS object
mycgds = CGDS("http://www.cbioportal.org/public-portal/")

# Test the CGDS endpoint URL using a few simple API tests
test(mycgds) 

# Get list of cancer studies at server
getCancerStudies(mycgds)

# Get available case lists (collection of samples) for a given cancer study  
mycancerstudy = getCancerStudies(mycgds)[2,1]
mycaselist = getCaseLists(mycgds,mycancerstudy)[1,1]

# Get available genetic profiles
mygeneticprofile = getGeneticProfiles(mycgds,mycancerstudy)[4,1]

# Get data slices for a specified list of genes, genetic profile and case list
getProfileData(mycgds,c('BRCA1','BRCA2'),mygeneticprofile,mycaselist)

# Get clinical data for the case list
myclinicaldata = getClinicalData(mycgds,mycaselist)
}
