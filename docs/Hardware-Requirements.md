# Hardware Requirements

Hardware requirements will vary depending on the volume of users you anticipate
will access your cBioPortal instance and the amount of data loaded in the
portal. We run [cbioportal.org](https://www.cbioportal.org) on an AWS r5.xlarge
instance with 32 GB and 4 vCPUs. The public database consumes ~50 GB of disk
space. The site is visited by several thousands of users a day. For on-premise
installation recommendations one can look at the AWS instance type specs:
 
| Platform | instance type | (v)CPUs | RAM(GB) | Storage (GB) |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| aws | r5.xlarge | 4 | 32 | 50 |
| on-premise | - | 4 | 32 | 50 |


The hardware requirements are much lower when one has only a few users a day.
Minimally, 2GB of RAM is needed to run a cBioPortal instance. If you do not
plan to import public studies, depending on the size of your private data, 10GB
of disk space may be sufficient.

Another possible consideration is caching. The portal can cache responses to
requests so that repeated database queries are avoided. On the public cBioPortal
deployment we enable this cache and allocate 1GB of additional RAM and 4GB of
additional disk space for caching. For directions on configuring caching, see
[Ehcache Settings](/deployment/customization/application.properties-Reference.md#cache-settings)
