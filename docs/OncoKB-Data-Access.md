# Introduction

A data access token is required for versions `v3.2.4` and greater to use OncoKB. Please review 
OncoKB [terms](https://www.oncokb.org/terms) and request for [data access](https://www.oncokb.org/dataAccess) 
if you would like to use OncoKB data with your private cBioPortal instance.

# Configuration

OncoKB data can be enabled by configuring `show.oncokb` and `oncokb.token` values in `portal.properties`. 
`show.oncokb` should be set to `true` and `oncokb.token` should be set to a valid OncoKB access token value.
`oncokb.token` has to be provided in case `show.oncokb` is set to `true`, otherwise cBioPortal instance will 
stop working with an OncoKB specific error message. To avoid such an error, set `show.oncokb` to `false` if 
there is no OncoKB access token is available.