Fix # (see https://help.github.com/en/articles/closing-issues-using-keywords)

Describe changes proposed in this pull request:
- a
- b

# Checks
- [ ] Runs on heroku
- [ ] Has tests or has a separate issue that describes the types of test that should be created. If no test is included it should explicitly be mentioned in the PR why there is no test.
- [ ] The commit log is comprehensible. It follows [7 rules of great commit messages](http://chris.beams.io/posts/git-commit/). For most PRs a single commit should suffice, in some cases multiple topical commits can be useful. During review it is ok to see tiny commits (e.g. Fix reviewer comments), but right before the code gets merged to master or rc branch, any such commits should be squashed since they are useless to the other developers. Definitely avoid [merge commits, use rebase instead.](http://nathanleclaire.com/blog/2014/09/14/dont-be-scared-of-git-rebase/)
- [ ] Is this PR adding logic based on one or more **clinical** attributes? If yes, please make sure validation for this attribute is also present in the data validation / data loading layers (in backend repo) and documented in [File-Formats Clinical data section](https://github.com/cBioPortal/cbioportal/blob/master/docs/File-Formats.md#clinical-data)!

# Any screenshots or GIFs?
If this is a new visual feature please add a before/after screenshot or gif
here with e.g. [GifGrabber](http://www.gifgrabber.com/).

# Notify reviewers
Read our [Pull request merging
policy](../CONTRIBUTING.md#pull-request-merging-policy). It can help to figure out who worked on the
file before you. Please use `git blame <filename>` to determine that
and notify them either through slack or by assigning them as a reviewer on the PR
