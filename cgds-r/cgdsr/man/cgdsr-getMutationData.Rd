\name{cgdsr-getMutationData}
\alias{cgdsr-getMutationData}
\alias{getMutationData}
\alias{getMutationData.CGDS}
\title{Get mutation data for cancer study}
\description{Queries the CGDS API and returns mutation data for
  a given case set and list of genes.}
\usage{\method{getMutationData}{CGDS}(x, caseList, geneticProfile, genes, ...)}
\arguments{
  \item{x}{A CGDS object (required)}
  \item{caseList}{A case list ID}
  \item{geneticProfile}{A genetic profile ID with mutation data}
  \item{genes}{A vector of query genes}
  \item{...}{Not used.}
}
\value{A data.frame with rows for each sample/case, rownames corresponding to
  case IDs, and columns corresponding to:
\enumerate{
\item \var{entrez_gene_id}: Entrez gene ID
\item \var{gene_symbol}: HUGO gene symbol
\item \var{sequencing_center}: Sequencer Center responsible for identifying this mutation.
\item \var{mutation_status}: somatic or germline mutation status. all mutations returned will be of type somatic.
\item \var{age_at_diagnosis}: Age at diagnosis.
\item \var{mutation_type}: mutation type, such as nonsense, missense, or
frameshift_ins.
\item \var{validation_status}: validation status. Usually valid,
invalid, or unknown.
\item \var{amino_acid_change}: amino acid change resulting from the mutation.
\item \var{functional_impact_score}: predicted functional impact score,
as predicted by Mutation Assessor.
\item \var{xvar_link}: Link to the Mutation Assessor web site.
\item \var{xvar_link_pdb}: Link to the Protein Data Bank (PDB) View
within Mutation Assessor web site.
\item \var{xvar_link_msa}: Link the Multiple Sequence Alignment (MSA)
view within the Mutation Assessor web site.
\item \var{chr}: chromosome where mutation occurs.
\item \var{start_position}: start position of mutation.
\item \var{end_position}: end position of mutation
}}
\author{
<jacobsen@cbio.mskcc.org>
}
\references{
  cBio Cancer Genomics Portal: \url{http://www.cbioportal.org/}
}
\seealso{
\code{\link{cgdsr}},\code{\link{CGDS}}
}
\examples{
# Create CGDS object
mycgds = CGDS("http://www.cbioportal.org/public-portal/")

getCancerStudies(mycgds)

# Get available case lists (collection of samples) for a given cancer study 
# Get Extended Mutation Data for EGFR and PTEN in TCGA GBM
#
# getMutationData(mycgds,gbm_tcga_all,gbm_tcga_mutations,c('EGFR','PTEN'))

}
