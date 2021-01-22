# Introduction

Using OncoKB does not require a data access token. But the instance you are connecting to only includes biological information by default. 
If you want to include tumor type summary, therapeutic levels and more, please consider obtaining a license from OncoKB.

# How to obtain an OncoKB license
1. Please review OncoKB [terms](https://www.oncokb.org/terms) 
2. Please request for [data access](https://www.oncokb.org/dataAccess)
3. You can find your token in your [Account Settings](https://www.oncokb.org/account/settings) after login.

# Set up cBioPortal to include full OncoKB content
Following properties can be edited in the `portal.properties` file or set in system variables if you are using docker. 
- `show.oncokb` should be set to `true`
- `oncokb.token` should be set to a valid OncoKB access token value
- `oncokb.public_api.url` should be set to `https://www.oncokb.org/api/v1`

Thank you for supporting future OncoKB development.

# Include MSI-H and TMB-H annotation
If you want to include the MSI-H and TMB-H annotation on patient view, please follow the instruction to [import required clinical data](File-Formats.md#clinical-data).  
For MSI-H, a clinical attribute MSI_TYPE with value Instable is required.  
For TMB-H, a clinical attribute TMB_SCORE with value >=10 is required.  

# Disable OncoKB Service

Please set `show.oncokb` to `false` in `portal.properties` or in system variables if you are using docker.
