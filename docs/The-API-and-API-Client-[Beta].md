_**Note: this API is still under development and subject to change.**_

Please see the documentation for the API
[here](http://www.cbioportal.org/api/swagger-ui.html).  We are using
Swagger/OpenAPI to describe the API, so you can easily generate a client to
connect to it in whichever language you prefer. We list some common examples
below, but if your language is not listed, fear not, there is likely a
generator available elsewhere.

# Python client
Generate a client in Python using [bravado](https://github.com/Yelp/bravado)
like this:

```python
from bravado.client import SwaggerClient
cbioportal = SwaggerClient.from_url('http://www.cbioportal.org/api/api-docs')
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

# R client
Generate a client in R using [rapiclient](https://github.com/bergant/rapiclient):
```R
library(rapiclient)
client <- get_api(url = "http://www.cbioportal.org/api/api-docs")
```
