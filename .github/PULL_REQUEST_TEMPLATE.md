# What? Why?
Fix # .

Changes proposed in this pull request:
- a
- b

# Checks
- [ ] Runs on Heroku.
- [ ] Follows [7 rules of great commit messages](http://chris.beams.io/posts/git-commit/). For most PRs a single commit should suffice, in some cases multiple topical commits can be useful. During review it is ok to see tiny commits (e.g. Fix reviewer comments), but right before the code gets merged to master or rc branch, any such commits should be squashed since they are useless to the other developers. Definitely avoid [merge commits, use rebase instead.](http://nathanleclaire.com/blog/2014/09/14/dont-be-scared-of-git-rebase/)
- [ ] Follows the [Google Style Guide](https://github.com/google/styleguide).
- [ ] Make sure your commit messages end with a Signed-off-by string (this line
  can be automatically added by git if you run the `git-commit` command with
  the `-s` option)
- [ ] If this is a feature, the PR is to rc. If this is a bug fix, the PR is to
  hotfix.

# Any screenshots or GIFs?
If this is a new visual feature please add a before/after screenshot or gif
here with e.g. [GifGrabber](http://www.gifgrabber.com/).

# Notify reviewers
If your pull request involves changes to an existing file, one or more of the
people that edited before you might be good candidates to review it. Please use
`git blame <filename>` to determine that and notify them here. Otherwise notify
everybody in the core team:
@cBioPortal/core-developers
