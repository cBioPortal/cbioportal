# Backend implementation of Published Virtual Studies

## RFC96: Published Virtual Study Authorization Model
The idea of bringing the concept of published virtual studies to the backend addresses the need to support an authorization model for virtual studies, just as we do for regular studies.
For more details, please refer to the [RFC96](https://docs.google.com/document/d/1aLRzLZvz0hzIM3nf2vqnSayEEjiUVOt3mP2xrUhufaU/edit?tab=t.0#heading=h.4ow08ycx7u0g)

## Issues

### No support of gene profile merging

A virtual studies that are defined in terms of multiple studies may have gene profiles of the same type (e.g. mutation) that are defined in different studies.
The current implementation, unlike frontend support of virtual studies, does not support merging of these profiles, which means that the user will see all distinct profiles in the UI.
We don't plan to implement this feature in the backend in scope of RFC96.

## TODO

- [x] Make it possible to give a custom name for a published virtual study. [cBioPortal PR](https://github.com/cBioPortal/cbioportal/pull/11611) and [Session Service PR](https://github.com/cBioPortal/cbioportal/pull/11611)
- [x] Implement or remove all methods that throw UnsupportedOperationException
- [x] Improve error message on vs study id collision
- [ ] We had call with Pieter and decided to accept this limitation. ~~Make sure VS is created in terms of regular studies even when user select a published virtual study for a definition~~
- [ ] Fix all TODOs and FIXMEs in the code
- [ ] Make sure you can't get published VS via regular VS endpoints without permissions!
- [ ] Bring all virtualization (id calculation and bean virtualization ones) methods in one place
- [ ] Test demo scenario with published virtual studies and keycloak
- [ ] Implement caching for the translation tables
- [ ] Make code more optimized
- [ ] Implement sample counts for published virtual studies
- [ ] Implement virtualization for StudyViewRepository
- [ ] Implement virtualization for ResourceDefinitionRepository and ResourceDataRepository
- [ ] Implement virtualization for PatientViewRepository
- [ ] Implement virtualization for SignificantMutatedGeneRepository
- [ ] Implement virtualization for SignificantCopyNumberRegionRepository
- [ ] Implement virtualization for AlterationDriverAnnotationRepository
- [ ] Implement virtualization for NamespaceRepository
- [ ] Implement virtualization for TreatmentRepository
- [ ] Investigate IDs collision risk between calculated IDs and existing IDs in the database
