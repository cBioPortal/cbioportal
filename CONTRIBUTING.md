# How to contribute

Thank you for contributing to cBioPortal!  This document provides a brief set of guidelines for contributing.

# Background

The cBioPortal currently uses a "fork and pull" model for collaborative software development.

From the [GitHub Help Page of Using Pull Requests](https://help.github.com/articles/using-pull-requests/):

"The fork & pull model lets anyone fork an existing repository and push changes to their personal fork without requiring access be granted to the source repository. The changes must then be pulled into the source repository by the project maintainer. This model reduces the amount of friction for new contributors and is popular with open source projects because it allows people to work independently without upfront coordination."

## Branches within cBioPortal

The cBioPortal currently maintains three branches:

 * **master**:  this is the most stable branch, reflecting that currently running in production on cbioportal.org.
 * **rc**:  release candidate branch, reflecting the next release candidate we are readying for deployment.  Once deployed to production, the rc branch is merged into master.
 * **hotfix**:  hot fix branch, used only for mission critical bug fixes that must be pushed out immediately.  Once deployed to production, the hotfix branch is merged into master. 

## Getting Started

 * Make sure you have a [GitHub account](https://github.com/signup/free).
 * Create an issue in our issues tracker, assuming one does not already exist.
 * Fork the cbioportal project on GitHub.  For general instructions on forking a GitHub project, see [Forking a Repo](https://help.github.com/articles/fork-a-repo/) and [Syncing a fork](https://help.github.com/articles/syncing-a-fork/).

## Contributing Code Changes via a Pull Request

Once you have forked the repo, you need to create your code contributions within a new branch of your forked repo.  For general background on creating and managing branches within GitHub, see:  [Git Branching and Merging](https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging).

* To begin, create a topic branch from where you want to base your work.
 * For a new feature, this is usually the **master branch**.  For bug fixes, this is usually the **hotfix branch**.

You usually create a branch like so:

```git checkout master```
```git checkout -b [name_of_your_new_branch]```

You then usually commit code changes, and push your branch back to GitHub like so:

```git push origin [name_of_your_new_branch]```

A few tips:

* Make commits in logical/cohesive units.
* Make sure your commit messages end with a Signed-off-by string (this line can be automatically added by git if you run the git-commit command with the -s option).
* Make sure you have added the necessary tests for your changes.
* Run _all_ tests to assure nothing else was accidentally broken.  This is done by running:  ```mvn test```.

When you are ready to submit your pull-request:

* Push your branch to your GitHub project.
* Open a Pull Request on GitHub to the **rc (release candidate)** branch for a new feature or the **hotfix** branch for a bug fix.

For instructions on submitting a pull-request, please see:  [Using Pull Requests ](https://help.github.com/articles/using-pull-requests/) and [Sending Pull Requests](http://help.github.com/send-pull-requests/).

## Additional Resources

* [cBioPortal Issue Tracker](https://github.com/cBioPortal/cbioportal/issues)
* [General GitHub documentation](http://help.github.com/)
* [GitHub pull request documentation](http://help.github.com/send-pull-requests/)
