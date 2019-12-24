# How to contribute

Thank you for your interest in contributing to cBioPortal! This document provides a brief set of guidelines for contributing.

# Who are you?

We are curious to learn more about you and would love to help you get started! The contributors in our community all have different backgrounds. They broadly fall into one of the following categories

1. Not much engineering nor genomics experience but eager to contribute
2. Engineer with no to little knowledge of genomics
3. Genomics researcher or bioinformatician with no to little engineering experience

But even if you feel like you don't fall into any of these categories, please reach out so you can help us update the above list 🙂! Note that there are many contributions that can be made to an open source commmunity without coding a single line of code. You can reach us through our [public slack channel](https://slack.cbioprtal.org).

# Join the Slack!

Come and chat with us at https://slack.cbioportal.org

# Architecture

If you are looking to make a code contribution have a look at the [Architecture docs](https://docs.cbioportal.org/2.1-deployment/architecture-overview). This helps to get an idea of the various components of the project

# How to make changes to the code

The cBioPortal currently uses a "fork and pull" model for collaborative software development.

From the [GitHub Help Page of Using Pull Requests](https://help.github.com/articles/using-pull-requests/):

"The fork & pull model lets anyone fork an existing repository and push changes to their personal fork without requiring access be granted to the source repository. The changes must then be pulled into the source repository by the project maintainer. This model reduces the amount of friction for new contributors and is popular with open source projects because it allows people to work independently without upfront coordination."


## Branches within cBioPortal

The cBioPortal currently maintains three branches:

 * **master**:  this reflects what will be released with our next weekly release (https://github.com/cBioPortal/cbioportal/releases). For the [frontend repo](https://github.com/cbioprotal/cbioprtal-frontend) this branch is automatically deployed to production. On the backend it is deployed at least once a week. New features, bug fixes and documentation updates can go here. Only if the feature requires a database migration it should go to **rc**.
  * **rc**:  release candidate branch, this branch contains new changes that require a database migration. It is deployed to https://rc.cbioportal.org. Once its ready for more thorough product review a new branch is created with the name **release-x.y.z**. This way people can still continue to submit new changes to **rc**, while more thorough testing takes place of the **release-x.y.z** branch.
  *  **release-x.y.z**: this branch contains changes that require a database migration. It will be merged to master after thorough product review on https://beta.cbioportal.org. 

## Getting Started

 * Make sure you have a [GitHub account](https://github.com/signup/free).
 * Create an issue in our issues tracker, assuming one does not already exist.
 * Fork the cbioportal project on GitHub.  For general instructions on forking a GitHub project, see [Forking a Repo](https://help.github.com/articles/fork-a-repo/) and [Syncing a fork](https://help.github.com/articles/syncing-a-fork/).
 * Familiarize yourself with the [project documentation](https://docs.cbioportal.org), including [backend code organization](Backend-Code-Organization.md) and [backend development guidelines](Backend-Development-Guidelines.md).

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
