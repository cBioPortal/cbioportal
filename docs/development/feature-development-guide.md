# Feature Development Guide

This is a guide for developers that are implementing a new feature.

## Before Implementation

As a first step it is important to determine how complex the proposed feature is. Incremental improvements on existing features are often easier to accomplish and require input from fewer people. Most minor changes can be submitted as a Pull Request. If the proposed feature would require one or more days of work it makes sense to connect on [slack](https://slack.cbioportal.org) to discuss the idea. For more complex new features that require weeks of work or more, it is best to get input from several people in the cBioPortal community, including people with a deep understanding of the cBioPortal product and its users as well as the engineers that write the software. In that case we often start out with a Request For Comments document that describes the feature in more detail, see [our list of RFCs ](../RFC-List.md)for some examples. The community can then help guide the feature development in the right direction.

During this process you will most likely receive some pointers which part of the stack you will be editing (see [Architecture Overview](../Architecture-Overview.md)). This will be helpful when actually starting your implementation and figuring out how to set up your development environment. For many features it is not necessary to understand all parts of the stack, so seeking out advice on this is highly recommended.

Before you start implementing a more complex feature, ideally many of these things are clear:

* Who can you contact for help?
* Who will be helping to review the code?
* What part of the stack will you work on?

Gold stars if you already start thinking about:

* How do we release the feature incrementally?
* When is feature development done?

See more thoughts about these topics further below

## Starting Implementation

Once you are ready to start implementing, the first thing is to set up the development environment. We strive to make this as easy as possible, but it can often still take some time so definitely reach out if you run into issues. If you haven't submitted a Pull Request to cBioPortal before, it might make sense to look at some [good first issues](https://github.com/cBioPortal/cbioportal/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22) before starting with your feature. This will help to get some familiarity with the process of proposing a change, getting it reviewed, making edits and getting it to production. Don't pick anything too complicated for a first issue, it could be as simple as fixing some typos in the `README`.

## During Implementation

The most important part during implementation is communication. Continue getting feedback as your implementation evolves. One of the best ways to do this is to fully integrate into the development team while you work on a feature. Anybody is welcome to join our weekly planning meeting (Tuesdays 11AM-12PM Eastern Time) and our daily standups 2.30-3PM Eastern Time. Please reach out on [slack](https://slack.cbioportal.org) to get an invite. If these times are not ideal or you're working on the feature more sporadically then it's totally fine to skip them.

## Plan to release to production early

Don't wait until the feature is fully finished to get feedback from the product team and the engineering team. Think about ways we can release a portion of the feature to production without finishing the entire thing. We have found feature flags to work well here. Instead of using long running feature branches we try to add a configuration property that allows us to turn the feature on or off. That way portions of the code can be released to production early on. We want to avoid working on some piece of code for more than a week or so without being able to release it. For instance: if one is trying to add some new tab on the Patient View Page, one could start with adding the on/off configuration switch for this tab. That could be released to production relatively quickly.

## Regression Testing

Make sure to think about ways to incorporate testing for your feature. We have an extensive suite of unit, integration and end to end tests including automated browser testing that mimics user interaction. Adding some regression tests will make sure the feature won't break with new versions of cBioPortal.

## When is feature development done?

There are many stages in feature development:

* Design
* Implementation
* Review
* Production Deployment
* Production Usage Monitoring

The process is hardly ever a linear line and it can move back and forth between any stage. This is expected and one of the reasons why time estimations are notoriously hard. An additional note is that feature development usually isn't done at the moment it gets merged to the main branch and deployed, but rather only after a few weeks of using it in production and not identifying any new issues.
