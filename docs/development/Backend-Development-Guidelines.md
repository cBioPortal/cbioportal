# cBioPortal Development Guidelines

## Architecture Guidelines

The cBioPortal backend follows a clean architecture pattern with clear separation of concerns. These guidelines help maintain code quality and consistency.

### Package Organization

1. **Domain Layer**: Place business logic and domain models in `org.cbioportal.domain.*` packages organized by feature
2. **Application Layer**: Place controllers, DTOs, and application services in `org.cbioportal.application.*` packages
3. **Infrastructure Layer**: Place data access, external services, and configuration in `org.cbioportal.infrastructure.*` packages
4. **Legacy Code**: Existing legacy code in `org.cbioportal.legacy.*` should be gradually migrated to the new architecture

### API Development

1. Place all new REST API controllers in the `application.rest` package
2. Make abstract data-driven web controllers part of the default/public interface (tag them with `@PublicApi`)
3. Make special purpose or visualization-driven web controllers part of the internal interface (tag them with `@InternalApi`)
4. Do not include business logic in controller handler functions. Limit processing to argument examination and service method selection
5. Test new data-driven web controllers for proper behavior on a portal deployment which requires user authentication and authorities

### Data Access

1. Do not call repository functions directly from controllers. Create service layer functions instead
2. Locate database query code in the infrastructure repository packages, and follow existing patterns
3. Use the new domain-based architecture for new features instead of legacy persistence patterns
4. Consider the tradeoffs between using database query constructs to accomplish business logic requirements versus writing service layer java code

### Legacy Code Migration

1. Avoid introducing new functionality in the legacy packages
2. When modifying legacy code, consider refactoring it to the new architecture
3. New features should be implemented using the clean architecture pattern
4. Gradually migrate legacy code to the new domain-based organization

### Code Quality

1. Follow the existing code style guidelines (see `CODE_STYLE.md`)
2. Write comprehensive tests for new functionality
3. Use dependency injection and Spring Boot best practices
4. Maintain backward compatibility when making changes to public APIs
