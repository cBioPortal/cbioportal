# Release Procedure
We have release procedures for the following scenarios:

1. [cBioPortal community release of code already in production](#cbioportal-community-release-of-code-already-in-production)
2. [New Feature Release](#new-feature-release)

## cBioPortal community release of code already in production
We often run code in production that is not ready yet for use by the wider
cBioPortal community. We deploy to production what's in the master branch of
the backend repo and the frontend repo. Often times this is not a tagged
release. At some point this code should be released for the wider community.
These are the steps we follow:

1. Create a new frontend tag. The releases can be found here:
   https://github.com/cBioPortal/cbioportal-frontend/releases. We usually
   already have a MAJOR.MINOR number for this release. Increment the PATCH
   number, i.e. MAJOR.MINOR.PATCH. Look at what new commits are in this release
   compared to the old release by using e.g.:
   https://github.com/cBioPortal/cbioportal-frontend/compare/v2.1.0...master.
   Adjust the tag and branch name in between the `...` of the link accordingly
   to see the new commits. Add significant PRs and issues to the release log:
   https://github.com/cBioPortal/cbioportal-frontend/releases/new. Follow the
   same style as the other releases shown in:
   https://github.com/cBioPortal/cbioportal-frontend/releases. You can save
   your work as a draft if necessary.
2. Once the frontend code is tagged, create a pull request to the backend repo
   where the frontend version is incremented in `portal/pom.xml`:
   ```
	  <groupId>com.github.cbioportal</groupId>
	  <artifactId>cbioportal-frontend</artifactId>
	  <version>CHANGE_THIS_TO_THE_FRONTEND_TAG</version>
   ```
3. Once that PR is merged, one can create a tag for the backend repo with the
   same tag as the frontend repo. Copy over the release log from the frontend
   repo. You can look at backend specific commits in the same manner:
   https://github.com/cBioPortal/cbioportal/compare/v2.1.0...master. Note that
   the release log of frontend and backend should be identical. Both list the
   significant frontend and backend changes. You can update existing release
   logs using the github interface.

## New Feature Release
For new feature releases, we increase the MINOR number in MAJOR.MINOR.PATCH.
For those releases we have a separate branch (see
https://github.com/cBioPortal/cbioportal/blob/master/CONTRIBUTING.md#branches-within-cbioportal),
which needs to be merged to master on both backend and frontend:

1. Make sure no auto deployment is running for frontend from netlify
2. Merge frontend release-x.y.z branch to frontend master
3. Follow same procedure as for [a PATCH
   release](#cbioportal-community-release-of-code-already-in-production),
   but instead of having a separate PR to update the frontend (step 2) one can
   add it to the already existing backend branch release-x.y.z and open the PR
   from there to backend's master. This is merely for convenience to avoid
   having to create another branch just to update the frontend version.

## A note on versioning
We follow the following logic when deciding how/when to increment the version
of cBioPortal. It's a complete modification of semantic versioning
(MAJOR.MINOR.PATCH) more suitable for our purposes:

MAJOR
: A big change in how cBioPortal works. We changed the major version from 1 to
2 when we completely moved from using JSPs to a Single Page App written in
React calling a REST service.

MINOR
: Backwards incompatible features that requires both backend and frontend
changes. If the frontend change is very significant we can increase the MINOR
version as well.

PATCH
: Backwards compatible fixes. Can be e.g. frontend only changes or fixes to
backend.
