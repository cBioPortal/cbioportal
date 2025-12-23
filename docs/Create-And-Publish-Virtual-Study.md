# Create and Publish Virtual Study

A [Virtual Study](./user-guide/faq.md#what-is-a-virtual-study) defines a subset or a combination of samples from one or more studies in the system.

A [Virtual Study](./user-guide/faq.md#what-is-a-virtual-study) is a custom study made up of samples from one or more existing studies. By default, a virtual study appears only under **My Virtual Studies** for its author. When it is published to the `/api/public_virtual_studies` endpoint, it becomes a **public virtual study** that is visible to all users on the landing page and can be used as a shared, reusable cohort. Publishing works for both static virtual studies (saved cohorts) and dynamic virtual studies whose contents are recomputed from filters on each load.

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

Publishing requires the publisher API key in the `X-PUBLISHER-API-KEY` header and can now happen in two different ways:

1. **Create in the UI, then publish by hash ID.** This is the original workflow where you first create a virtual study through the interface (the ID in the study URL is a MongoDB hash), and then publish that specific study.
2. **Create and publish with a custom ID.** This RFC96 addition lets you publish a public virtual study in a single request by providing its definition (constraints/filters) and the desired ID. The ID must be unique and is typically a human-readable string so permissions can be managed up front.

### Publish an existing virtual study

After creating the study in the UI and copying its ID (hash), publish it via:

```shell
curl \
  -X POST \
  -H 'X-PUBLISHER-API-KEY: <session.endpoint.publisher-api-key>' \
  -v 'http://<cbioportal_host>/api/public_virtual_studies/<virtual_study_id>'
```

You can set optional metadata while publishing:

```shell
curl \
  -X POST \
  -H 'X-PUBLISHER-API-KEY: <session.endpoint.publisher-api-key>' \
  -v 'http://<cbioportal_host>/api/public_virtual_studies/<virtual_study_id>?pmid=<pmid>&typeOfCancerId=<code>'
```

The `typeOfCancerId` must match an existing cancer type so it shows up in that category on the landing page; `pmid` links the public study to PubMed.

### Create and publish with a custom ID

To publish a new public virtual study in one go, call the same endpoint but supply:

- `/<custom_id>` path segment with the ID you want to assign
- `Content-Type: application/json`
- A request body containing the virtual study definition (filters/constraints)

Example:

```shell
curl \
  -X POST \
  -H 'X-PUBLISHER-API-KEY: <session.endpoint.publisher-api-key>' \
  -H 'Content-Type: application/json' \
  -v 'http://<cbioportal_host>/api/public_virtual_studies/<custom_id>' \
  --data @virtual_study_definition.json
```

See the [Virtual Study JSON Schema](./Virtual-Study-Data-Schema.md) for the full payload shape you can post to create and publish a study with a custom ID. If the ID already exists you will receive a `409 Conflict`. When the request succeeds, the virtual study is immediately available to all users under the `Public Virtual Studies` section, optionally enriched with `pmid` and `typeOfCancerId` query parameters the same way as above.

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
