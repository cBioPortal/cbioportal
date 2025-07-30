
It might help to quickly review what RFC96 is about by checking this document:  
[https://docs.google.com/document/d/1aLRzLZvz0hzIM3nf2vqnSayEEjiUVOt3mP2xrUhufaU/edit?tab=t.0#heading=h.4ow08ycx7u0g](https://docs.google.com/document/d/1aLRzLZvz0hzIM3nf2vqnSayEEjiUVOt3mP2xrUhufaU/edit?tab=t.0#heading=h.4ow08ycx7u0g)

# Preparation

## Configure API secret keys

cBioPortal currently does not support user roles, so there is no admin concept.  
To give users special access, we use secret tokens. If someone knows the token, they are allowed to perform the related actions.

## Publish Virtual Studies

By default, publishing virtual studies (see RFC83) is turned off.  
To enable it, set a value for `session.endpoint.publisher-api-key` in `application.properties`. For example:

```
session.endpoint.publisher-api-key=TEST_PUBLISHER_API_KEY
```

### To test publishing:

1. Create a virtual study.
2. Note its ID.
3. Run the following curl command to publish:

```
curl -v -X POST -H 'X-PUBLISHER-API-KEY: TEST_PUBLISHER_API_KEY' http://localhost:8080/api/public_virtual_studies/{hash_of_your_virtual_study}
```

For more details:  
[https://docs.cbioportal.org/create-and-publish-virtual-study/](https://docs.cbioportal.org/create-and-publish-virtual-study/)

> Note: RFC96 introduces the ability to assign custom IDs to published virtual studies.  
> This feature is still in progress. We will test it once it is available.

## Clear Cache Endpoint

cBioPortal uses a cache. With RFC96, we need to clear the cache after uploading study data.  
To enable the cache-clearing endpoint, add these to `application.properties`:

```
cache.endpoint.enabled=true  
cache.endpoint.api-key=TEST_CACHE_MANAGEMENT_API_KEY
```

### To test cache clearing:

```
curl -v -X DELETE http://localhost:8080/api/cache -H 'X-API-KEY: TEST_CACHE_MANAGEMENT_API_KEY'
```

You should see a successful HTTP status if the cache is cleared.

More info:  
[https://docs.cbioportal.org/deployment/customization/caching/#cache-eviction](https://docs.cbioportal.org/deployment/customization/caching/#cache-eviction)

## Configure Keycloak

To test the authorization model, you need to configure Keycloak.

See:  
[https://docs.cbioportal.org/deployment/authorization-and-authentication/authenticating-and-authorizing-users-via-keycloak/](https://docs.cbioportal.org/deployment/authorization-and-authentication/authenticating-and-authorizing-users-via-keycloak/)

# Testing

The feature is in a separate branch:  
[https://github.com/cBioPortal/cbioportal/tree/rfc96-published-vs-auth-model](https://github.com/cBioPortal/cbioportal/tree/rfc96-published-vs-auth-model)

To enable the feature, set this in `application.properties`:

```
feature.published_virtual_study.single-sourced.backend-mode=true
```

Once enabled, published virtual studies based on a single study will be handled by the backend.  
You can assign different permissions to these studies.  
Multi-study (multi-sourced) virtual studies will continue to work the same way.

## Test Scenarios

### Virtual Study with All Samples

1. Create a virtual study that includes all samples of a physical study.
2. Publish it.
3. Check that the UI shows the same data for both.
4. Note: Published virtual studies might not show specific sample counts yet.  
   This is a known issue and will be fixed.  
   Pages should not load slower than the original study pages.

### Permission Testing

RFC96 allows setting permissions for virtual studies separately from the source study.

**Test steps:**

1. Upload a private study.
2. Create and publish a virtual study pointing to it.
3. Give a user access to the virtual study only.
4. Confirm that the user **can** access the virtual study and **cannot** access the physical study.

### Cache and Dynamic Study Testing

1. Create a dynamic public study and publish it.
2. Note the number of samples.
3. Upload new data to add or remove samples from the source study.
4. Clear the cache.
5. Check that sample counts in the published virtual study update correctly.