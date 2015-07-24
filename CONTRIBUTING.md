# How to contribute

Guidelines for contributing to the cBioPortal project.

## Getting Started

 * Make sure you have a [GitHub account](https://github.com/signup/free).
 * Create an issue in our issues tracker, assuming one does not already exist.
 * Fork the cbioportal project on GitHub.

## Making Changes

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
