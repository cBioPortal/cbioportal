# cBioPortal Backend Code Organization

## Spring Boot Project

The backend code is structured as a single-module [Spring Boot](https://spring.io/projects/spring-boot) application built with [Maven](https://maven.apache.org/index.html). The project uses a modern clean architecture pattern with clear separation of concerns.

### Frontend Integration

The frontend is a [React](https://reactjs.org/) javascript application located
in a separate [GitHub
repository](https://github.com/cBioPortal/cbioportal-frontend) repo, which
communicates with the backend via a REST API. The frontend is packaged with the backend by default, but can also be deployed independently.

### Current Code Organization

The backend follows a clean architecture pattern with the following main packages:

#### Domain Layer (`org.cbioportal.domain.*`)

Contains business logic and domain models organized by feature:
- `alteration/` - Gene alteration analysis and enrichment
- `cancerstudy/` - Cancer study metadata and types
- `clinical_attributes/` - Clinical attribute definitions
- `clinical_data/` - Clinical data processing
- `patient/` - Patient-related business logic
- `sample/` - Sample-related business logic
- `studyview/` - Study view filtering and services

#### Application Layer (`org.cbioportal.application.*`)

Contains application services and controllers:
- `rest/` - REST API controllers and DTOs
- `security/` - Authentication and authorization
- `file/export/` - Data export functionality
- `proxy/` - Proxy services for external APIs

#### Infrastructure Layer (`org.cbioportal.infrastructure.*`)

Contains external concerns and technical implementations:
- `repository/` - Data access implementations
- `service/` - Infrastructure services
- `config/` - Configuration classes

#### Legacy Layer (`org.cbioportal.legacy.*`)

Contains legacy code that is being gradually refactored:
- `web/` - Legacy web controllers and services
- `persistence/` - Legacy data access layer
- `service/` - Legacy business services
- `model/` - Legacy data models

### External Dependencies

* [cbioportal-frontend](https://github.com/cBioPortal/cbioportal-frontend) : a React application using MobX and TypeScript
* [session-service](https://github.com/cBioPortal/session-service) : an external session key/query specifier storage system

**cbioportal-frontend** is packaged in the web application as a default frontend
implementation, but the source of the frontend code can also be directed to an external
source host and be deployed independently of the backend web application. See
[details](Deployment-Procedure.md)

**session-service** is imported into the web module in order to set up a proxy service
which receives and forwards requests for saved cBioPortal sessions to a separate system
providing this storage (using a document based database) The code is needed for handling
modeled types such as VirtualStudy and Session

### Architecture Principles

The current architecture follows these principles:

1. **Clean Architecture**: Clear separation between domain, application, and infrastructure layers
2. **Domain-Driven Design**: Business logic organized by domain concepts
3. **Dependency Inversion**: High-level modules don't depend on low-level modules
4. **Legacy Gradual Migration**: Legacy code is being gradually refactored into the new architecture

### Migration from Multi-Module to Single-Module

The project has been migrated from a multi-module Maven project to a single-module Spring Boot application. The legacy multi-module structure (core, web, service, persistence, etc.) has been consolidated into a single module with clear package-based organization.
