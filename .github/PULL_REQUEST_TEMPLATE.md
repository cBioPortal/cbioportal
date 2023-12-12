Fix # (see https://help.github.com/en/articles/closing-issues-using-keywords)

Describe changes proposed in this pull request:
- a
- b

# Checks
- [ ] The commit log is comprehensible. It follows [7 rules of great commit messages](http://chris.beams.io/posts/git-commit/). We can fix this during merge by using a squash+merge if necessary
- [ ] Has tests or has a separate issue that describes the types of test that should be created. If no test is included it should explicitly be mentioned in the PR why there is no test.
- [ ] Is this PR adding logic based on one or more **clinical** attributes? If yes, please make sure validation for this attribute is also present in the data validation / data loading layers (in backend repo) and documented in [File-Formats Clinical data section](https://github.com/cBioPortal/cbioportal/blob/master/docs/File-Formats.md#clinical-data)!
- [ ] Make sure your PR has one of the labels defined in https://github.com/cBioPortal/cbioportal/blob/master/.github/release-drafter.yml

# Any screenshots or GIFs?
If this is a new visual feature please add a before/after screenshot or gif
here with e.g. [Giphy CAPTURE](https://giphy.com/apps/giphycapture) or [Peek](https://github.com/phw/peek)

# Notify reviewers
Read our [Pull request merging
policy](../CONTRIBUTING.md#pull-request-merging-policy). It can help to figure out who worked on the
file before you. Please use `git blame <filename>` to determine that
and notify them either through slack or by assigning them as a reviewer on the PR
