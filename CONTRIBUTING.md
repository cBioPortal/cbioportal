# How to contribute

Thank you for contributing to cBioPortal!  This document provides a brief set of guidelines for contributing.

# Background

The cBioPortal currently uses a "fork and pull" model for collaborative software development.

From the [GitHub Help Page of Using Pull Requests](https://help.github.com/articles/using-pull-requests/):

"The fork & pull model lets anyone fork an existing repository and push changes to their personal fork without requiring access be granted to the source repository. The changes must then be pulled into the source repository by the project maintainer. This model reduces the amount of friction for new contributors and is popular with open source projects because it allows people to work independently without upfront coordination."

## Branches within cBioPortal

The cBioPortal currently maintains three branches:

 * **master**:  this reflects what is currently running in production on cbioportal.org. Bug fixes and documentation fixes go here.
 * **rc**:  release candidate branch, incorporating all the latest features. You could see our **rc** branch as a development branch where we only accept high quality contributions. Once ready for testing on cbioportal.org/beta a new branch is formed with the name **release-x.y.z**.
 *  **release-x.y.z**: before each release a new branch is created from **rc** that has a name like **release-x.y.z** .This one is usually deployed to www.cbioportal.org/beta. 

## Getting Started

 * Make sure you have a [GitHub account](https://github.com/signup/free).
 * Create an issue in our issues tracker, assuming one does not already exist.
 * Fork the cbioportal project on GitHub.  For general instructions on forking a GitHub project, see [Forking a Repo](https://help.github.com/articles/fork-a-repo/) and [Syncing a fork](https://help.github.com/articles/syncing-a-fork/).

## Contributing Code Changes via a Pull Request

Once you have forked the repo, you need to create your code contributions within a new branch of your forked repo.  For general background on creating and managing branches within GitHub, see:  [Git Branching and Merging](https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging).

* To begin, create a topic branch from where you want to base your work.
 * For a new feature, this is usually the **rc branch**.  For documentation and bug fixes, this is usually the **master branch**.

You usually create a branch like so:

```git checkout master```
```git checkout -b [name_of_your_new_branch]```

You then usually commit code changes, and push your branch back to GitHub like so:

```git push origin [name_of_your_new_branch]```

A few tips:

* Make commits in logical/cohesive units.
* Make sure you have added the necessary tests for your changes.
* Run _all_ tests to assure nothing else was accidentally broken in the java part (data loading and front-end parts are tested by other scripts in travis). This is done by running:  ```mvn integration-test```.

When you are ready to submit your pull-request:

* Push your branch to your GitHub project.
* Open a Pull Request on GitHub to the **rc (release candidate)** branch for a new feature or the **master** branch for a bug fix or documentation fix.

For instructions on submitting a pull-request, please see:  [Using Pull Requests ](https://help.github.com/articles/using-pull-requests/) and [Sending Pull Requests](http://help.github.com/send-pull-requests/).

### Getting your changes reviewed

Once you've submitted your pull request, you want
other members of the development community to review
whether integrating your change will cause problems
for any users or the maintainability of the software.

If you have an idea who might be able to spot such issues
in the parts of the code and functionality affected by your changes,
notify them by requesting a review using the **Reviewers** menu
to the right of the summary you just wrote
and/or `@`-mentioning them in a comment.

Reviewers may request you to rephrase or adjust things
before they allow the changes to be integrated.
If they do, commit the amendments as new, separate changes,
to allow the reviewers to see what changed since they last read your code.
Do not overwrite previously-reviewed commits with
ones that include additional changes (by `--amend`ing or squashing)
until the reviewers approve.

## Automated tests on Travis CI
All Pull Requests are automatically tested on [Travis
CI](https://travis-ci.org/cBioPortal/cbioportal/pull_requests). Currently there
is a set of tests for the core module and a visual regression test that makes
some screenshots and compares them to the ones stored in the repository.

### What to do if the screenshot test fails
When the screenshot test fails, it means that the screenshot taken from your
instance of the portal differs from the screenshot stored in the repo.
Copy+Paste the URL in the Travis CI log to view the image diff online. Further
instructions are outlined on that page.

If you prefer to compare the images locally, you need to first download the
failing screenshot. The Travis CI log will show you where the image was
uploaded on [clbin.com](https://clbin.com). First, download the image and
replace the screenshot in the repo. For instance run in the root dir of
cBioPortal:

```bash
curl 'https://clbin.com/[replace-with-clbin-image-from-log].png' > test/end-to-end/screenshots/[replace-with-image-from-repo].png
```

Then follow the steps outlined in [this blog post](http://www.akikoskinen.info/image-diffs-with-git/) to compare the 
images locally. Run `git diff` from your repo to see the ImageMagick diff.

Once you downloaded the images you do the following for each screenshot:

- If the change in the screenshot is **undesired**, i.e. there is regression, you
  should fix your PR.
- If the change in the screenshot is **desired**, add the screenshot to the
  repo, commit it and push it to your PR's branch.

## Pull Request Reviewers Guide
If someone requests your review on a pull request,
read the title and description and assign any other collaborators
who would want to know about the proposed change.

Decide whether you think that your input is needed,
and that the PR should wait for your further review before being merged.
If not, un-assign yourself as a reviewer and leave a comment.

Here we describe the guidelines for the reviewer. Always follow the checks in
general, then follow the other checks that apply:

### General
- Double check all the things in the **Checks** section of the Pull Request.
  Remind the submitter if any of them are not fulfilled
- Are the test cases spanning a decent amount of scenarios? It is the
  submitters as well as the reviewers responsibility to not let any errors
  sneak into the portal.

Bug fixes:

- Should the bug that causes the issue be added as a test case?

New features:

- If this is a new feature make sure the proposed changes are in line with the
  current planning of cBioPortal e.g. is the right API used, is this in line
  with current refactoring efforts.

### Backend
New features:

- Is the new persistence stack used?

### Frontend
New features:

- What APIs are used to get the data? Is the REST API used?
- Should this be a separate library in a separate repo or should it be part of cBioPortal?
- Are dependencies properly listed? Ideally in a package.json
- How is the package included in cBioPortal?

### Devops
New features:

- Does the configuration style follow the config guidelines? That is compile
  (Maven) config goes in the appriopriate `pom.xml` (root, `scripts/`, `portal/`, `core/`).
  Runtime (Spring) goes in `portal.properties`. Default values should be in `GlobalProperties.java`.
- Non-stable configuration should be done through war overlays.
- Is the configuration tested as part of Travis CI? It's not a necessity but be
  aware that untested configuration will be tough to maintain.
- Is there documentation on the proposed changes?

## Additional Resources

* [cBioPortal Issue Tracker](https://github.com/cBioPortal/cbioportal/issues)
* [General GitHub documentation](http://help.github.com/)
* [GitHub pull request documentation](http://help.github.com/send-pull-requests/)
