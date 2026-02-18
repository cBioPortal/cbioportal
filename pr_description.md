Fix #11924

## Description
This Pull Request removes legacy JSP files from `src/main/resources/webapp/` that have been identified as defunct. These files have been superseded by Thymeleaf HTML templates or are no longer functional due to missing dependencies (e.g., the removal of `GlobalProperties`). This cleanup reduces technical debt and eliminates dead code from the repository.

## Changes Proposed
- Removed 13 defunct JSP files, including `index.jsp`, `login.jsp`, `netviz.jsp`, and others in `src/main/resources/webapp/`.
- Verified that the application's view resolution logic (in Controllers like `IndexPageController` and `LoginPageController`) now targets the corresponding HTML templates.

## Verification
- **Static Analysis**: Confirmed that the deleted JSP files are not referenced by active Java controllers.
- **Dependency Validation**: Verified that the `GlobalProperties` class, which was a dependency for several of these JSPs (e.g., `robots.jsp`, `sitemap_index.jsp`), is no longer present in the codebase, confirming these files were already broken.
- **Build Checks**: Ran build verification to ensure no implicit dependencies were broken.

## Checks
- [x] The commit log is comprehensible. It follows [7 rules of great commit messages](http://chris.beams.io/posts/git-commit/).
- [ ] Has tests or has a separate issue that describes the types of test that should be created. **(Reason: Cleanup of unused files; verified by absence of references.)**
- [ ] Is this PR adding logic based on one or more **clinical** attributes? **(No)**
- [ ] Make sure your PR has one of the labels defined in the release drafter.

## Reviewers
@aaronlisman @n1zea144
(Suggested based on git history of the affected files)
