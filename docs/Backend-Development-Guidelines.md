# cBioPortal Development Guidelines

1. Avoid introducing new functionality in the core module or creating new dependencies on the existing core module code.
1. One exception to the previous guideline is that new data import functions should be added to the core module scripts package.
1. Do not expand the business module.
1. Place all new web API controllers in the web module.
1. Make abstract data-driven web controllers part of the default/public interface (tag them with @PublicApi).
1. Make Special purpose or visualization-driven web controllers part of the internal interface (tag them with @InternalApi).
1. Do not include business logic in the web module handler functions. Limit processing to argument examination and service module method selection.
1. Test new data-driven web controllers for proper behavior on a portal deployment which requires user authentication and authorities.
1. Do not call persistence module functions directly from the web module. Create pass-through service layer functions instead.
1. Locate database query code in the persistence modules, and follow existing patterns.
1. Consider the tradeoffs between using database query constructs to accomplish business logic requirements versus writing service layer java code.
