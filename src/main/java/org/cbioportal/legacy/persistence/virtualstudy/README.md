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
- [x] Implement caching for the translation tables
- [ ] check if VirtaulisationService use cases can be simplified
- [ ] Test demo scenario with published virtual studies and keycloak
- [ ] Make code more optimized
- [ ] Implement sample counts for published virtual studies
- [ ] Fix infinite recursion for published dynamic studies! Here where we go into infinite recursion:
```
	at org.cbioportal.legacy.service.impl.VirtualStudyServiceImpl.populateVirtualStudySamples(VirtualStudyServiceImpl.java:94) ~[classes/:na]
	at org.cbioportal.legacy.service.impl.VirtualStudyServiceImpl.getPublishedVirtualStudies(VirtualStudyServiceImpl.java:142) ~[classes/:na]
	at org.cbioportal.legacy.persistence.virtualstudy.FilteredPublishedVirtualStudyService.getPublishedVirtualStudies(FilteredPublishedVirtualStudyService.java:33) ~[classes/:na]
	at org.cbioportal.legacy.persistence.virtualstudy.VirtualizationService.getPublishedVirtualStudies(VirtualizationService.java:53) ~[classes/:na]
	at org.cbioportal.legacy.persistence.virtualstudy.VirtualizationService.handleStudySampleData(VirtualizationService.java:748) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359) ~[spring-aop-6.2.1.jar:6.2.1]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:723) ~[spring-aop-6.2.1.jar:6.2.1]
	at org.cbioportal.legacy.persistence.virtualstudy.VirtualizationService$$SpringCGLIB$$0.handleStudySampleData(<generated>) ~[classes/:na]
	at org.cbioportal.legacy.persistence.virtualstudy.VSAwareSampleRepository.fetchSamples(VSAwareSampleRepository.java:161) ~[classes/:na]
	at org.cbioportal.legacy.persistence.virtualstudy.VSAwareSampleRepository.getAllSamples(VSAwareSampleRepository.java:41) ~[classes/:na]
	at org.cbioportal.legacy.persistence.virtualstudy.VSAwareSampleRepository.getAllSamplesInStudies(VSAwareSampleRepository.java:109) ~[classes/:na]
	at org.cbioportal.legacy.service.impl.SampleServiceImpl.getAllSamplesInStudies(SampleServiceImpl.java:92) ~[classes/:na]
	at org.cbioportal.legacy.web.util.StudyViewFilterApplier.apply(StudyViewFilterApplier.java:158) ~[classes/:na]
	at org.cbioportal.legacy.web.util.StudyViewFilterApplier.cachedApply(StudyViewFilterApplier.java:134) ~[classes/:na]
	at org.cbioportal.legacy.web.util.StudyViewFilterApplier.apply(StudyViewFilterApplier.java:127) ~[classes/:na]
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359) ~[spring-aop-6.2.1.jar:6.2.1]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:723) ~[spring-aop-6.2.1.jar:6.2.1]
	at org.cbioportal.legacy.web.util.StudyViewFilterApplier$$SpringCGLIB$$0.apply(<generated>) ~[classes/:na]
	at org.cbioportal.legacy.service.impl.VirtualStudyServiceImpl.populateVirtualStudySamples(VirtualStudyServiceImpl.java:94) ~[classes/:na]
```