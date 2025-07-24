# Backend implementation of Published Virtual Studies

## RFC96: Published Virtual Study Authorization Model
The idea of bringing the concept of published virtual studies to the backend addresses the need to support an authorization model for virtual studies, just as we do for regular studies.
For more details, please refer to the [RFC96](https://docs.google.com/document/d/1aLRzLZvz0hzIM3nf2vqnSayEEjiUVOt3mP2xrUhufaU/edit?tab=t.0#heading=h.4ow08ycx7u0g)

## Issues

### No support of multi source published virtual studies
 
The backend implementation of published virtual studies that are defined in terms of multiple studies are not supported yet. The current implementation only supports published virtual studies that are defined in terms of a single study.

## TODO

- [x] Make it possible to give a custom name for a published virtual study. [cBioPortal PR](https://github.com/cBioPortal/cbioportal/pull/11611) and [Session Service PR](https://github.com/cBioPortal/cbioportal/pull/11611)
- [x] Implement or remove all methods that throw UnsupportedOperationException
- [x] Improve error message on vs study id collision
- [x] make all repositories to use VirtualizationService instead of VirtualStudyService
- [x] We had call with Pieter and decided to accept this limitation. ~~Make sure VS is created in terms of regular studies even when user select a published virtual study for a definition~~
- [x] Fix all TODOs and FIXMEs in the code
- [x] Make sure you can't get published VS via regular VS endpoints without permissions!
- [x] ~~Bring all virtualization (id calculation and bean virtualization ones) methods in one place~~ We don't calculate sample and patient ids anymore, so this is not needed.
- [x] Investigate IDs collision risk between calculated IDs and existing IDs in the database
- [x] ~~Implement virtualization for PatientViewRepository~~ I can't find this repository in the codebase anymore.
- [?] Implement virtualization for StudyViewRepository - Is this guy even used?
- [x] Implement virtualization for ResourceDefinitionRepository and ResourceDataRepository
- [?] Implement virtualization for SignificantMutatedGeneRepository - Do we really need this?
- [?] Implement virtualization for SignificantCopyNumberRegionRepository - Do we really need this?
- [x] Implement virtualization for AlterationDriverAnnotationRepository
- [x] Implement virtualization for NamespaceRepository
- [x] Implement virtualization for TreatmentRepository
- [ ] check if VirtaulisationService use cases can be simplified
- [ ] Test demo scenario with published virtual studies and keycloak
- [ ] Implement caching for the translation tables
- [ ] Make code more optimized
- [ ] Implement sample counts for published virtual studies
