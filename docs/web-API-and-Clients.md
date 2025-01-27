# API and API Clients

cBioPortal provides a REST API for programmatic access to the data. The visualizations one can see on the website leverage the same API. By connecting to the API directly, anyone can build their own visalizations/reports.

Please see the full reference documentation for the API [here](https://www.cbioportal.org/api/swagger-ui/index.html).

## API Clients

The cBioPortal REST API is described using Swagger/OpenAPI, which allows one to generate a client in most programming languages. One can use the command line tool `curl` for dowloading data on the command line or use another language such as `Python` or `R` to make visualizations. We list some common examples below, but if your language is not listed, there is likely a client generator available elsewhere (see e.g. https://swagger.io/tools/swagger-codegen/). Do reach out if you'd like us to add a language.

### R clients

There are multiple ways to access the API using R. Below are two recommended R packages to access cBioPortal data.

#### cBioPortalData (recommended)
*Maintainers: [Marcel Ramos Pérez](https://github.com/LiNk-NY), [Levi Waldron](https://github.com/lwaldron)*

cBioPortalData aims to import all cBioPortal datasets as MultiAssayExperiment objects in Bioconductor. Some of its key features:

1. The MultiAssayExperiment class explicitly links all assays to the patient clinical/pathological data
2. The MultiAssayExperiment class provides a flexible API including harmonized subsetting and reshaping to convenient wide and long formats.
3. It provides complete datasets, not just for subsets of genes
4. It provides automatic local caching, thanks to BiocFileCache.

For a comprehensive user guide to `cBioportalData` see: https://waldronlab.io/cBioPortalData/articles/cBioPortalData.html

See also the workshop materials from our webinar which include an intro to `cBioPortalData`: https://github.com/cBioPortal/2020-cbioportal-r-workshop.

Note that one can point to private authenticated instances like this:

```
cBioPortal(
    hostname = "genie.cbioportal.org",
    token = "~/Downloads/cbioportal_data_access_token.txt"
)
```

#### cbioportalR (recommended)
*Maintainer: [Karissa Whiting](https://github.com/karissawhiting)*

cbioportalR offers easy-to-use functions that allow users to browse and pull data from public or institutional cBioPortal sites without knowledge of web service or Bioconductor infrastructures. The package is tidyverse-compatible. Key package features include:

1. Comprehensive documentation aimed at helping clinical researchers understand the underlying structure of cBioPortal data
2. [Tutorials]([https://www.karissawhiting.com/cbioportalR/articles/overview-of-workflow.html]) for quick API authentication and set up
3. Functions to pull complete clinical and genomic data by study ID, molecular profile ID, sample list IDs or individual sample ID (e.g. `get_genetics_by_study()`, `get_genetics_by_sample()`)
4. Functions to navigate and identify patient IDs, sample IDs or study IDs as needed, or infer necessary ID information for queries when not supplied by user.
5. Helper functions to pull information on gene panels (`get_gene_panel()`), or lookup entrez ID (`get_entrez_id()`), Hugo Symbol (`get_hugo_symbol()`) or common gene aliases (`get_alias()`) of genes
6. Capability to query multiple sample IDs from different studies concurrently

For a detailed tutorial on `cbioportalR`, see the package website: https://www.karissawhiting.com/cbioportalR/articles/overview-of-workflow.html

#### rapiclient
*Maintainers: [Darko Bergant](https://github.com/bergant), [Marcel Ramos Pérez](https://github.com/LiNk-NY)*

Although we recommend [cBioPortalData](/#cbioportaldata-recommended) or [cbioportalR](/#cbioportalR-recommended) for most use cases, it is possible to connect to the API directly using [rapiclient](https://github.com/bergant/rapiclient):

```
library(rapiclient)
client <- get_api(url = "https://www.cbioportal.org/api/v2/api-docs")
```

#### CGDSR (deprecated)

The CGDS-R package connected to an older version of our web API (`webservice.do`). We no longer support this API and recommend people switch to our new API (`cbioportal.org/api`) by migrating to one of the other R clients instead.

### Python client

There are multiple ways to access the API using Python. One can use the `bravado` package to access the API directly, or use the `cbio_py` client, which provides a simple wrapper for the API and returns data in a format that is easy to work with.

#### bravado
*Maintainer: Yelp*

Generate a client in Python using [bravado](https://github.com/Yelp/bravado) like this:

```python
from bravado.client import SwaggerClient
cbioportal = SwaggerClient.from_url('https://www.cbioportal.org/api/v2/api-docs',
                                    config={"validate_requests":False,"validate_responses":False,"validate_swagger_spec": False})
```

This allows you to access all API endpoints:

```python
>>> dir(cbioportal)
['Cancer Types',
 'Clinical Attributes',
 'Clinical Data',
 'Clinical Events',
 'Copy Number Segments',
 'Discrete Copy Number Alterations',
 'Gene Panels',
 'Genes',
 'Molecular Data',
 'Molecular Profiles',
 'Mutations',
 'Patients',
 'Sample Lists',
 'Samples',
 'Studies']
```

For easy tab completion you can add lower cases and underscores:

```python
for a in dir(cbioportal):
    cbioportal.__setattr__(a.replace(' ', '_').lower(), cbioportal.__getattr__(a))
```

This example gets you all mutation data for the MSK-IMPACT 2017 study:

```python
muts = cbioportal.mutations.getMutationsInMolecularProfileBySampleListIdUsingGET(
    molecularProfileId="msk_impact_2017_mutations", # {study_id}_mutations gives default mutations profile for study 
    sampleListId="msk_impact_2017_all", # {study_id}_all includes all samples
    projection="DETAILED" # include gene info
).result()
```

For a portal that requires authentication one can use (see [Data Access Using Tokens](/deployment/authorization-and-authentication/Authenticating-Users-via-Tokens.md)):

```
from bravado.client import SwaggerClient
from bravado.requests_client import RequestsClient

http_client = RequestsClient()
http_client.set_api_key(
    'genie.cbioportal.org', 'Bearer <TOKEN>',
    param_name='Authorization', param_in='header'
)

cbioportal = SwaggerClient.from_url('https://genie.cbioportal.org/api/v2/api-docs',
                                    http_client=http_client,
                                    config={"validate_requests":False,
                                            "validate_responses":False,
                                            "validate_swagger_spec": False}
)
```

A Jupyter notebook with more examples can be found [here](https://github.com/mskcc/cbsp-hackathon/blob/master/0-introduction/cbsp\_hackathon.ipynb).

#### cbio\_py
*Maintainer: [Sasha Dagayev](https://www.linkedin.com/in/sasha-dagayev/)*

See the `cbio_py` documentation: https://pypi.org/project/cbio-py/.


#### gget
*Maintainer: [Sam Wagenaar](https://github.com/techno-sam)*

Plot cancer genomics heatmaps using data from cBioPortal using Ensembl IDs or gene names. See: https://pachterlab.github.io/gget/en/cbio.html. The `gget` client is also a command line client, providing access to many other bioinformatics resources beyond cBioPortal.
