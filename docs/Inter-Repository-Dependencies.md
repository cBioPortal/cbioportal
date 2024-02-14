# Inter-Repository Dependencies

## cbioportal-core Repository

[cbioportal-core](https://github.com/cBioPortal/cbioportal-core) is dependent on
[cbioportal](https://github.com/cBioPortal/cbioportal).
This can be seen in its [pom.xml](https://github.com/cBioPortal/cbioportal-core/blob/main/pom.xml).
By removing the dependency and attempting to build cbioportal-core, errors are reported. This
document captures a summary of the reported errors. This analysis was done at the time of release
v6.0.0.

cbioportal-core now primarily provides import functionality, which is mainly located
in the packages:
- [org.mskcc.cbio.portal.dao](https://github.com/cBioPortal/cbioportal-core/tree/main/src/main/java/org/mskcc/cbio/portal/dao)
- [org.mskcc.cbio.portal.model](https://github.com/cBioPortal/cbioportal-core/tree/main/src/main/java/org/mskcc/cbio/portal/model)
- [org.mskcc.cbio.portal.scripts](https://github.com/cBioPortal/cbioportal-core/tree/main/src/main/java/org/mskcc/cbio/portal/scripts)

### Dependencies in cbioportal-core import functionality

Code in cbioportal which is referenced directly by code in cbioportal-core consists of three
enum class definitions.

- cbioportal class [ResourceType](https://github.com/cBioPortal/cbioportal/blob/master/src/main/java/org/cbioportal/model/ResourceType.java) is imported by cbioportal-core class [ResourceDefinition](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/model/ResourceDefinition.java). This provides enum constants {STUDY, SAMPLE, PATIENT}.
- cbioportal class [EntityType](https://github.com/cBioPortal/cbioportal/blob/master/src/main/java/org/cbioportal/model/EntityType.java) is imported by cbioportal-core class [ImportTabDelimData](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/scripts/ImportTabDelimData.java). This provides enum constants {GENE, GENESET, PHOSPHOPROTEIN, GENERIC_ASSAY}.
- cbioportal class [CNA](https://github.com/cBioPortal/cbioportal/blob/master/src/main/java/org/cbioportal/model/CNA.java) is imported by cbioportal-core class [CnaEvent](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/model/CnaEvent.java). This provides enum constants {AMP, GAIN, DIPLOID, HETLOSS, HOMDEL}.

The cbioportal-core import functionality code also relies on several packaged libraries in cbioportal.

cbioportal packaged library org.apache.commons.commons-collections4 (version 4.4) is used by these cbioportal-core classes:
- [DaoPatient](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/dao/DaoPatient.java)
- [ImportClinicalData](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/scripts/ImportClinicalData.java)

cbioportal packaged library org.slf4j.slf4j-api (latest version) is used by these cbioportal-core classes:
- [JdbcUtil](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/dao/JdbcUtil.java)
- [ProgressMonitor](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/util/ProgressMonitor.java)
- [Patient](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/model/Patient.java)
- [AccessControlImpl](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/util/internal/AccessControlImpl.java)

cbioportal packaged library org.springframework.security.spring-security.core (latest compatible version)
and other related such as org.springframework.security.spring-security.web are used in these cbioportal-core classes:
- [AccessControl](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/util/AccessControl.java)
- [DaoCancerStudy](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/dao/DaoCancerStudy.java) (transitive dependency through AccessControl)

### Other dependencies in cbioportal-core

There are other (non-import) cbioportal-core uses of libraries packaged in cbioportal.

cbioportal packaged libraries jakarta.servlet jakarta.servlet.http (ServletConfig, HttpServlet, ServletException, HttpServletRequest, HttpServletResponse} are used by these cbioportal-core classes:
- [GetSurvivalDataJSON](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/servlet/GetSurvivalDataJSON.java)
- [XDebug](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/util/XDebug.java)
- [CalcCoExp](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/servlet/CalcCoExp.java)
- [BioGeneServlet](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/servlet/BioGeneServlet.java)
- [IGVLinkingJSON](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/servlet/IGVLinkingJSON.java)

cbioportal packaged library org.json.simple / com.googlecode.json-simple.json-simple (version 1.1.1) is used by these cbioportal-core classes:
- [CalcCoExp](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/servlet/CalcCoExp.java)
- [GetSurvivalDataJSON](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/servlet/GetSurvivalDataJSON.java)
- [IGVLinkingJSON](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/servlet/IGVLinkingJSON.java)
- [GetClinicalData](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/web_api/GetClinicalData.java)

cbioportal packaged library org.apache.commonsorg.apache.commons.commons-math3 (version 3.6.1) is used by this cbioportal-core class:
- [CalcCoExp](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/servlet/CalcCoExp.java)

cbioportal packaged library org.slf4j.slf4j-api (latest version) is used by this cbioportal-core class:
- [AccessControlImpl](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/util/internal/AccessControlImpl.java) (transitive dependency through many servlets)

cbioportal packaged library org.springframework.security.spring-security.core (latest compatible version)
and other related such as org.springframework.security.spring-security.web are used in these cbioportal-core classes:
- [AccessControl](https://github.com/cBioPortal/cbioportal-core/blob/main/src/main/java/org/mskcc/cbio/portal/util/AccessControl.java) (transitive dependency through many servlets)

### Consequences
When any of these dependencies are updated in cbioportal (or in the case of libraries, updated in a non-backwards-compatible way), the cbioportal-core repository should be built and tested using the updated dependencies and proper behavior of the import functionality should be tested. cbioportal-core should also be brought up to date by packaging the current version of cbioportal into cbioportal-core (in pom.xml).
