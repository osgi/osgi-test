# Contributing

Want to hack? Here are instructions to get you
started. They are probably not perfect, please let us know if anything
feels wrong or incomplete.

## Reporting Issues

Issues can be reported in [the GitHub issue tracker](https://github.com/osgi/osgi-test/issues).
Please include the steps required to reproduce the problem if possible and applicable.
This information will help us review and fix your issue faster.

## Build Environment

The only thing you need to build Java. We require at least Java 8.
We use Maven to build and the repo includes `mvnw`.
You can use your system `mvn` but we require a recent version.

- `./mvnw clean install` - Assembles and tests the project

We use [GitHub Actions](https://github.com/osgi/osgi-test/actions?query=workflow%3A%22CI%20Build%22) and the repo includes a
`.github/workflows/cibuild.yml` file to build with GitHub Actions.

## Workflow

We use [git triangular workflow](https://github.blog/2015-07-29-git-2-5-including-multiple-worktrees-and-triangular-workflows/).
This means that no one, not even the maintainers, push contributions directly into the [main repo](https://github.com/osgi/osgi-test). All contribution come in through pull requests.
So each contributor will need to [fork the main repo](https://github.com/osgi/osgi-test/fork)
on GitHub. All contributions are made as commits to your fork. Then you submit a
pull request to have them considered for merging into the main repo.

### Setting up the triangular workflow

After forking the main repo on GitHub, you can clone the main repo to your system:

    git clone https://github.com/osgi/osgi-test.git

This will clone the main repo to a local repo on your disk and set up the `origin` remote in Git.
Next you will set up the the second side of the triangle to your fork repo.

    cd osgi-test
    git remote add fork git@github.com:github-user/osgi-test.git

Make sure to replace the URL with the SSH URL to your fork repo on GitHub. Then we configure
the local repo to push your commits to the fork repo.

    git config remote.pushdefault fork

So now you will pull from `origin`, the main repo, and push to `fork`, your fork repo.
This option requires at least Git 1.8.4. It is also recommended that you configure

    git config push.default simple

unless you are already using Git 2.0 where it is the default.

Finally, the third side of the triangle is pull requests from your fork repo to the
main repo.

## Contribution guidelines

### Pull requests are always welcome

We are always thrilled to receive pull requests, and do our best to
process them as fast as possible. Not sure if that typo is worth a pull
request? Do it! We will appreciate it.

If your pull request is not accepted on the first try, don't be
discouraged! If there's a problem with the implementation, hopefully you
received feedback on what to improve.

### Create issues...

Any significant improvement should be documented as [a GitHub
issue](https://github.com/osgi/osgi-test/issues) before anybody
starts working on it.

### ...but check for existing issues first!

Please take a moment to check that an issue doesn't already exist
documenting your bug report or improvement proposal. If it does, it
never hurts to add a quick "+1" or "I have this problem too". This will
help prioritize the most common problems and requests.

### Conventions

Fork the repo and make changes on your fork in a feature branch:

- If it's a bugfix branch, name it XXX-something where XXX is the number of the
  issue
- If it's a feature branch, create an enhancement issue to announce your
  intentions, and name it XXX-something where XXX is the number of the issue.

Submit unit tests for your changes. We use JUnit 5. Run the full build including all
the tests in your branch before submitting a pull request. Having GitHub Actions set up for your fork repo is quite a help here.

Write clean code. Universally formatted code promotes ease of writing, reading,
and maintenance. We use Eclipse and the project has Eclipse `.settings` which
will properly format the code. Make sure to avoid unnecessary white space changes
which complicate diffs and make reviewing pull requests much more time consuming.

Pull requests descriptions should be as clear as possible and include a
reference to all the issues that they address.

Pull requests must not contain commits from other users or branches.

Commit messages must start with a short summary (max. 50
chars) written in the imperative, followed by an optional, more detailed
explanatory text which is separated from the summary by an empty line.

    index: Remove absolute URLs from the OBR index

    The url for the root was missing a trailing slash. Using File.toURI to
    create an acceptable url.

Code review comments may be added to your pull request. Discuss, then make the
suggested modifications and push additional commits to your feature branch. Be
sure to post a comment after pushing. The new commits will show up in the pull
request automatically, but the reviewers will not be notified unless you
comment.

Before the pull request is merged, make sure that you squash your commits into
logical units of work using `git rebase -i` and `git push -f`. After every
commit, the test suite should be passing. Include documentation changes in the
same commit so that a revert would remove all traces of the feature or fix.

Commits that fix or close an issue should include a reference like `Closes #XXX`
or `Fixes #XXX`, which will automatically close the issue when merged.

### Large changes/Work-In-Progress

Sometimes for big changes/feature additions, you may wish to submit a pull
request before it is fully ready to merge, in order to solicit feedback from the
core developers and ensure you're on the right track before proceeding too far.
In this case, you can submit a pull request and mark it as a
draft.

Once your pull request is ready for consideration to merge, remove the 
draft marker to signal this fact to the core team. While the pull request is
marked as draft, the maintainers are unlikely to know that it is ready, the
review process won't start and your PR won't get merged.

### Sign your work

The sign-off is a simple line at the end of the commit message
which certifies that you wrote it or otherwise have the right to
pass it on as an open-source patch.  The rules are pretty simple: if you
can certify the below (from
[developercertificate.org](https://developercertificate.org/)):

```
Developer Certificate of Origin
Version 1.1

Copyright (C) 2004, 2006 The Linux Foundation and its contributors.
660 York Street, Suite 102,
San Francisco, CA 94110 USA

Everyone is permitted to copy and distribute verbatim copies of this
license document, but changing it is not allowed.


Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
    have the right to submit it under the open source license
    indicated in the file; or

(b) The contribution is based upon previous work that, to the best
    of my knowledge, is covered under an appropriate open source
    license and I have the right under that license to submit that
    work with modifications, whether created in whole or in part
    by me, under the same open source license (unless I am
    permitted to submit under a different license), as indicated
    in the file; or

(c) The contribution was provided directly to me by some other
    person who certified (a), (b) or (c) and I have not modified
    it.

(d) I understand and agree that this project and the contribution
    are public and that a record of the contribution (including all
    personal information I submit with it, including my sign-off) is
    maintained indefinitely and may be redistributed consistent with
    this project or the open source license(s) involved.
```

then you just add a line to end of the git commit message:

    Signed-off-by: Joe Smith <joe.smith@email.com>

using your real name. Sorry, no pseudonyms or anonymous contributions.

Many Git UI tools have support for adding the `Signed-off-by` line to the end of your commit
message. This line can be automatically added by the `git commit` command by using the `-s` option.

It is important that you read and understand the legal considerations found
below when signing off or contributing any commit.

### Merge approval

The maintainers will review your pull request and, if approved, will merge into
the main repo.

If your pull request was originally a work-in-progress, don't forget to remove WIP from its title
to signal to the maintainers that it is ready for review.

### How can I become a maintainer?

* Step 1: learn the code inside out
* Step 2: make yourself useful by contributing code, bugfixes, support etc.
* Step 3: introduce your self to the other maintainers

Don't forget: being a maintainer is a time investment. Make sure you will have time
to make yourself available. You don't have to be a maintainer to make a difference
on the project!

## Legal considerations

Please read the [Eclipse Foundation policy on accepting contributions via Git](http://wiki.eclipse.org/Development_Resources/Contributing_via_Git).

Your contribution cannot be accepted unless you have a signed [ECA - Eclipse Foundation Contributor Agreement](http://www.eclipse.org/legal/ECA.php) in place.

Here is the checklist for contributions to be _acceptable_:

1. [Create an account at Eclipse](https://dev.eclipse.org/site_login/createaccount.php).
2. Add your GitHub user name in your account settings.
3. [Log into the project's portal](https://projects.eclipse.org/) and sign the ["Eclipse ECA"](https://projects.eclipse.org/user/sign/cla).
4. Ensure that you [_sign-off_](https://wiki.eclipse.org/Development_Resources/Contributing_via_Git#Signing_off_on_a_commit) your Git commits.
5. Ensure that you use the _same_ email address as your Eclipse account in commits.
6. Include the appropriate copyright notice and license at the top of each file.

Your signing of the ECA will be verified by a webservice called 'ip-validation'
that checks the email address that signed-off on your commits has signed the
ECA. **Note**: This service is case-sensitive, so ensure the email that signed
the ECA and that signed-off on your commits is the same, down to the case.

### Copyright Notice and Licensing Requirements

**It is the responsibility of each contributor to obtain legal advice, and
to ensure that their contributions fulfill the legal requirements of their
organization. This document is not legal advice.**

This project is licensed under the Apache
License v2.0. Any previously unlicensed contribution should be released under
the same license.

- If you wish to contribute code under a different license, you must consult
with a committer before contributing.
- For any scenario not covered by this document, please discuss the copyright
notice and licensing requirements with a committer before contributing.

The template for the copyright notice and license is as follows:

```java
/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
```
