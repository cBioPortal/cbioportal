# Create and Publish Virtual Study

A [Virtual Study](./user-guide/faq.md#what-is-a-virtual-study) defines a subset or a combination of samples from one or more studies in the system.

*Note*: To publish or un-publish a virtual study, your cBioPortal instance must be configured with  `session.endpoint.publisher-api-key` in the `application.properties`.

## Create Virtual Study

To create a virtual study in cBioPortal, follow these steps:

1. Define the desired filters on the study or multiple studies summary page.
2. Click the button with the bookmark icon () in the top right corner of the screen.
3. Provide a title and description, then click the Save button. You will see a link that looks like:

```
https://<cbioportal_host>/study?id=<virtual_study_id>
```

4. Save the virtual study link or ID if you want to publish it.

If you are logged in, this virtual study will appear in the `My Virtual Studies` section on the landing page.
You can always find the ID of the virtual study from the URL of the page that opens after clicking on it.

## Publish Virtual Study

To publish a virtual study, you need to supply the publisher API key in the `X-PUBLISHER-API-KEY` header.

Here is a curl command to publish a virtual study:
```shell
curl \
  -X POST \
  -H 'X-PUBLISHER-API-KEY: <session.endpoint.publisher-api-key>' \
  -v 'http://<cbioportal_host>/api/public_virtual_studies/<virtual_study_id>'
```
The published virtual study will appear under the `Public Virtual Studies` section (next to the `My Virtual Studies` section) on the landing page for all users of cBioPortal.

While publishing, you can specify the PubMed ID (`pmid`) and `typeOfCancerId` of the virtual study using the following command:
```shell
curl \
  -X POST \
  -H 'X-PUBLISHER-API-KEY: <session.endpoint.publisher-api-key>' \
  -v 'http://<cbioportal_host>/api/public_virtual_studies/<virtual_study_id>?pmid=<pmid>&typeOfCancerId=<code>'
```

The type of cancer code should match the known types of cancers in the cBioPortal database.
If the type of cancer is specified, the published virtual study will appear under the respective cancer section on the landing page.
Specifying the `pmid` enables a link to the PubMed page of the study.

## Un-publish Virtual Study

To un-publish a virtual study, you need to supply the publisher API key in the `X-PUBLISHER-API-KEY` header.
After un-publishing, virtual study will no longer be displayed in the `Public Virtual Studies` section on the landing page.
However, it reappears in the `My Virtual Studies` section for the owner.

Here is the command to un-publish a virtual study:
```shell
curl \
  -X DELETE \
  -H 'X-PUBLISHER-API-KEY: <session.endpoint.publisher-api-key>' \
  -v 'http://<cbioportal_host>/api/public_virtual_studies/<virtual_study_id>'
```