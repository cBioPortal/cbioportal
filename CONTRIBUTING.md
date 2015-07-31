# How to contribute

Guidelines for contributing to the cBioPortal project.

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

* Create a topic branch from where you want to base your work.
 * For bug fixes, this is usually the hotfix branch.  For a new feature, this is usually the master branch.
* Make commits in logical/cohesive units.
* Make sure your commit messages are in the proper format, ending in a Signed-off-by string (this line can be automatically added by git if you run the git-commit command with the -s option).

````
    (1) Make the example in CONTRIBUTING imperative and concrete

    Without this patch applied the example commit message in the CONTRIBUTING
    document is not a concrete example.  This is a problem because the
    contributor is left to imagine what the commit message should look like
    based on a description rather than an example.  This patch fixes the
    problem by making the example concrete and imperative.

    The first line is a real life imperative statement with a ticket number
    from our issue tracker.  The body describes the behavior without the patch,
    why this is a problem, and how the patch fixes the problem when applied.
    
    Signed-off-by: Random J Developer <random@developer.example.org>
````

* Make sure you have added the necessary tests for your changes.
* Run _all_ tests to assure nothing else was accidentally broken.

## Submitting Changes

* PLACE-HOLDER - Developer Certificate of Origin
* Push this branch to your GitHub project.
* Open a Pull Request on GitHub to the rc (release candidate) branch for a new feature or the hotfix branch for a bug fix.

## Additional Resources

* [cBioPortal Issue Tracker](https://github.com/cBioPortal/cbioportal/issues)
* [PLACE-HOLDER- Developer Certificate of Origin]
* [General GitHub documentation](http://help.github.com/)
* [GitHub pull request documentation](http://help.github.com/send-pull-requests/)
