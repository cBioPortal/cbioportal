# How to contribute

Thank you for your interest in contributing to cBioPortal! This document provides a brief set of guidelines for contributing.

# Who are you?

We are curious to learn more about you! We would love to help you get started! The contributors in our community all have different backgrounds. For instance some people have:

1. Engineering experience but no to little knowledge of cancer genomics
2. Knowledge about cancer genomics but no to little engineering experience
3. No engineering nor cancer genomics experience but an eagerness to contribute

if you feel like you don't fall into any of these categories, please reach out so you can help us update the above list 🙂! Note that there are many contributions that can be made to an open source commmunity without coding a single line of code. You can reach us through our [public slack channel](https://slack.cbioportal.org).

# Join the Slack!

Come and chat with us at https://slack.cbioportal.org 👋

# Making a code contribution

The cBioPortal currently uses a "fork and pull" model for collaborative software development.

From the [GitHub Help Page of Using Pull Requests](https://help.github.com/articles/using-pull-requests/):

"The fork & pull model lets anyone fork an existing repository and push changes to their personal fork without requiring access be granted to the source repository. The changes must then be pulled into the source repository by the project maintainer. This model reduces the amount of friction for new contributors and is popular with open source projects because it allows people to work independently without upfront coordination."

## Getting Started

### Programming
 * Make sure you have a [GitHub account](https://github.com/signup/free).
 * Familiarize yourself with the [project documentation](https://docs.cbioportal.org), including the [Feature Development Guide](https://docs.cbioportal.org/development/feature-development-guide/), the [Architecture docs](https://docs.cbioportal.org/2.1-deployment/architecture-overview), the [backend code organization](docs/Backend-Code-Organization.md) and [backend development guidelines](docs/Backend-Development-Guidelines.md).
 * Find a [good first issue](https://github.com/cBioPortal/cbioportal/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc+label%3A%22good+first+issue%22) to start with
 * Check if the issue will require frontend or backend changes. If it is for the frontend look at how to set up the [frontend repo](https://github.com/cbioPortal/cbioportal-frontend/) instead
 * Fork the cbioportal or cbioportal-frontend project on GitHub depending on what your working on.  For general instructions on forking a GitHub project, see [Forking a Repo](https://help.github.com/articles/fork-a-repo/) and [Syncing a fork](https://help.github.com/articles/syncing-a-fork/).
 * Reach out on slack or our [Google user group](https://groups.google.com/g/cbioportal) if you run into any issues

### Documentation
If you'd like to improve our documentation, have a look at https://docs.cbioportal.org. At the bottom of each page is an "edit page" button to make changes. You can use the GitHub UI to edit the pages and submit them.

## Contributing Code Changes via a Pull Request

Once you have forked the repo, you need to create your code contributions within a new branch of your forked repo.  For general background on creating and managing branches within GitHub, see:  [Git Branching and Merging](https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging).

* To begin, create a topic branch from where you want to base your work.
 * For any change that requires database migrations, this will be the **rc branch**. For all other changes, this will be the **master branch**. For additional details, see [Branches within cBioPortal](#branches-within-cbioportal) below.

You usually create a branch like so:

```
git checkout master
git checkout -b [name_of_your_new_branch]
```

You then usually commit code changes, and push your branch back to GitHub like so:

```git push origin [name_of_your_new_branch]```

A few tips:

* Make commits in logical/cohesive units.
* Make sure you have added the necessary tests for your changes.
* Run _all_ tests to assure nothing else was accidentally broken in the java part (data loading and front-end parts are tested by other scripts in github actions). This is done by running:  ```mvn integration-test```.

When you are ready to submit your pull-request:

* Push your branch to your GitHub project.
* Open the pull request to the branch you've based your work on

For more details on submitting a pull-request, please see:  [GitHub Guide to Collaborating with issues and pull requests](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests).

## Branches within cBioPortal

To figure out where your first pull request might go, it helps to have an understanding of cBioPortal's branching model. The cBioPortal currently maintains three branches in both the [frontend](https://github.com/cbioportal/cbioportal-frontend) and [backend repo](https://github.com/cbioportal/cbioportal):

 * **master**:  this reflects what will be released with our next weekly release (https://github.com/cBioPortal/cbioportal/releases). For the [frontend repo](https://github.com/cbioportal/cbioportal-frontend) this branch is automatically deployed to production. On the backend it is deployed at least once a week. New features, bug fixes and documentation updates can go here. Only if the feature requires a database migration it should go to **rc**.
  * **rc**:  release candidate branch, this branch contains new changes that require a database migration. It is deployed to https://rc.cbioportal.org. Once it's ready for more thorough product review a new branch is created with the name **release-x.y.z**. This way people can still continue to submit new changes to **rc**, while more thorough testing takes place of the **release-x.y.z** branch.
  *  **release-x.y.z**: this branch contains changes that require a database migration. It will be merged to master after thorough product review on https://beta.cbioportal.org.
  
We try to continuously merge new changes from `master` to `release-x.y.z`, and subsequently from the `release-x.y.z` branch to `rc` such that everybody is working on the latest code. Keep in mind though that occasionally there are conflicts that need to be resolved before we can merge. If you're working on e.g. the rc branch, you can check whether all the changes in master are in rc like this: https://github.com/cBioPortal/cbioportal/compare/rc...master. If a particular change you are waiting for is not there, one can help creating a pull request that merges these changes in. Try e.g. (if origin points to the cbioportal repo): `git fetch origin && git checkout origin/rc -b merge-master-to-rc && git merge origin/master`. Then resolve conflicts and push the branch `merge-master-to-rc` to your repo and open the PR.

### Getting your changes reviewed

Once you've submitted your pull request, you want
other members of the development community to review
whether integrating your change will cause problems
for any users or the maintainability of the software.

If you have an idea who might be able to spot such issues
in the parts of the code and functionality affected by your changes,
notify them by requesting a review using the **Reviewers** menu
to the right of the summary you just wrote
and/or `@`-mentioning them in a comment. Or reaching out them on [slack](https://slack.cbioportal.org).

Reviewers may request you to rephrase or adjust things
before they allow the changes to be integrated.
If they do, commit the amendments as new, separate changes,
to allow the reviewers to see what changed since they last read your code.
Do not overwrite previously-reviewed commits with
ones that include additional changes (by `--amend`ing or squashing)
until the reviewers approve.
Reviewers may request you to squash such amendment commits afterwards,
or offer to push rewritten versions of your commits themselves.

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

- Does the configuration style follow the config guidelines? That is compile.
- Runtime (Spring) goes in `application.properties`. Default values should be in `GlobalProperties.java`.
- Non-stable configuration should be done through war overlays.
- Is the configuration tested as part of the CI tests? It's not a necessity but be
  aware that untested configuration will be tough to maintain.
- Is there documentation on the proposed changes?

## Additional Resources

* [cBioPortal Issue Tracker](https://github.com/cBioPortal/cbioportal/issues)
* [General GitHub documentation](http://help.github.com/)
* [GitHub Pull Request documentation](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests)
