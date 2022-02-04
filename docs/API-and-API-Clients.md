# Web API
cBioPortal provides a REST API for programmatic access to the data. The visualizations one can see on the website leverage the same API. By connecting to the API directly, anyone can build their own visalizations/reports.

Please see the full reference documentation for the API
[here](https://www.cbioportal.org/api/swagger-ui.html).

## API Clients
The cBioPortal REST API is described using Swagger/OpenAPI, which allows one to generate a client in most programming languages. One can use the command line tool `curl` for dowloading data on the command line or use another language such as `Python` or `R` to make visualizations. We list some common examples below, but if your language is not listed, there is likely a client generator available elsewhere (see e.g. https://swagger.io/tools/swagger-codegen/). Do reach out if you'd like us to add a language.

### R client
There are multiple ways to access the API using R. The recommended way is to use `cBioPortalData`.

#### cBioPortalData (recommended)
cBioPortalData aims to import all cBioPortal datasets as MultiAssayExperiment objects in Bioconductor. Some of its key features:

1. The MultiAssayExperiment class explicitly links all assays to the patient clinical/pathological data
2. The MultiAssayExperiment class provides a flexible API including harmonized subsetting and reshaping to convenient wide and long formats.
3. It provides complete datasets, not just for subsets of genes
4. It provides automatic local caching, thanks to BiocFileCache.

For a comprehensive user guide to `cBioportalData` see: https://waldronlab.io/cBioPortalData/articles/cBioPortalData.html

See also the workshop materials from our webinar which include an intro to `cBioPortalData`: https://github.com/cBioPortal/2020-cbioportal-r-workshop.

Note that one can point to private authenticated instances like this:

```R
cBioPortal(
    hostname = "genie.cbioportal.org",
    token = "~/Downloads/cbioportal_data_access_token.txt"
)
```

#### rapiclient
Although we recommend to use [cBioPortalData](#cBioPortalData) for most use cases, it is possible to connect to the API directly using [rapiclient](https://github.com/bergant/rapiclient):

```R
library(rapiclient)
client <- get_api(url = "https://www.cbioportal.org/api/api-docs")
```

#### CGDSR (will be deprecated)
The CGDS-R package connects an older version of our web API (`webservice.do`). Althought we will continue to keep `webservice.do` running for a while, we can't guarantee the same level of quality as our new API (`cbioportal.org/api`) provides. Therefore we recommend that you use `cBioPortalData` instead.

### Python client
There are multiple ways to access the API using Python. One can use the `bravado` package to access the API directly, or use the `cbio_py` client, which provides a simple wrapper for the API and returns data in a format that is easy to work with.

#### bravado
Generate a client in Python using [bravado](https://github.com/Yelp/bravado)
like this:

```python
from bravado.client import SwaggerClient
cbioportal = SwaggerClient.from_url('https://www.cbioportal.org/api/api-docs',
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

For a portal that requires authentication one can use (see [Data Access Using Tokens](https://docs.cbioportal.org/2.2-authorization-and-authentication/authenticating-users-via-tokens#using-data-access-tokens)):

```
headers = {
  'Authorization': 'Bearer 63efa81c-2490-4e15-9d1c-fb6e8e50e35d'
}
requestOptions = {
   'headers': headers,
}
cbioportal = SwaggerClient.from_url('https://genie.cbioportal.org',
                                    request_headers=headers,
                                    config={"validate_requests":False,
                                            "validate_responses":False,
                                            "validate_swagger_spec": False}
)
```

A Jupyter notebook with more examples can be found [here](https://github.com/mskcc/cbsp-hackathon/blob/master/0-introduction/cbsp_hackathon.ipynb).

#### cbio_py
See the `cbio_py` documentation: https://pypi.org/project/cbio-py/.
