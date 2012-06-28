\name{cgdsr-plot}
\alias{cgdsr-plot}
\alias{plot}
\alias{plot.CGDS}
\title{Generic plot function for CGDS API data.}
\description{Queries the CGDS API and plots data for specified genes and genetic profiles.}
\usage{\method{plot}{CGDS}(x,cancerStudy, genes, geneticProfiles,
caseList, cases, caseIdsKey, skin, skin.normals, skin.col.gp, add.corr, legend.pos, ...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{cancerStudy}{cancer study ID (required)}
  \item{genes}{A vector of gene names or a String specifying a single gene (required)}
  \item{geneticProfiles}{ A vector of genetic profile IDs or String specifying
    a single genetic profile (required)}
  \item{caseList}{A case list ID}
  \item{cases}{A vector of case IDs)}
    \item{caseIdsKey}{Only used by web portal.}
  \item{skin}{A string specifying which plotting layout skin to use
    (default is continous data 'cont')}
  \item{skin.normals}{Specify a case list ID with normal samples, only
    some skins handle normal data.}
  \item{skin.col.gp}{Specify a vector of additional case list IDs to use
    for color coding of data points. Color coding is only handled by
    some skins.}
  \item{add.corr}{Computes correlation between the two data
    vectors. Specify correlation method ('pearson' or 'spearman') as
    argument.}
    \item{legend.pos}{Position of legend in plot (default is 'topright').}
  \item{...}{Not used.}
}
\details{Queries the CGDS API and plots data for specified genes and
  genetic profiles.

  The following combinations are allowed:
  \enumerate{
    \item 1 gene and 1 genetic profile. Plots genetic profile data histogram for specified gene.
    \item 2 genes and 1 genetic profile. Scatter plot of continuous genetic profile data for the two genes.
    \item 3 1 gene and 2 genetic profiles. Scatterplot or boxplot
    relating two genetic profile datasets for single gene.
  }

  The function currently implements the following skins:
  \enumerate{
    \item \var{cont}: This is the default skin. It treats all data as
    being continuous.
    \item \var{disc}: Requires a single gene and a single genetic
    profile. The genetic profile data is handled as a discrete dataset and
    barplot is returned.
    being continuous.
    \item \var{disc_cont}: Requires two genetic profiles. The first dataset is
    handled as being discrete data, and the function generates a boxplot
    with distributions for each level of the discrete genetic profile.
    \item \var{cna_mrna_mut}: This skin plots mRNA expression level as
    function of copy number status for a given gene. Data points are
    colored by mutation status if specified (\var{skin.col.gp}), and
    normal data points are included if specified (\var{skin.normals}).
    \item \var{cna_mrna_mut}: This skin plots mRNA expression level as
    function of DNA methylation status for a given gene. Data points are
    colored by copy number and mutation status if specified (two element
    vector of copy number and mutation genetic profiles specified for
    \var{skin.col.gp}). Normal data points are included if specified
    (\var{skin.normals}).    
  }
  
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

# histogram of genetic profile data for gene
plot(mycgds,mycancerstudy,'MDM2',mygeneticprofile,mycaselist)

# scatter plot of genetic profile data for two genes
plot(mycgds,mycancerstudy,c('MDM2','MDM4'),mygeneticprofile,mycaselist)

# See vignette for more details ...

}
