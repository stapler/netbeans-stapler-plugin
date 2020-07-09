A NetBeans IDE plugin suite to support Jenkins plugin development and the Stapler web framework.

[Plugin Portal downloads](http://plugins.netbeans.org/plugin/43938/)

## Changelog

See [GitHub Releases](https://github.com/stapler/netbeans-stapler-plugin/releases) going forward.

## Open issues

TODO move to https://github.com/stapler/netbeans-stapler-plugin/issues after verifying

* For new plugin wizard, `version` is initially 1 and `artifactId` wrong.
* Stapler view navigation should walk up the inheritance hierarchy if necessary, and consider a nested class if the caret is in one
* Stapler view creation should assume src/main/resources if there are multiple resource folders available
* extend Output Window hyperlink to work from `hudson-dev:run`
* find usages, find subtypes, etc. inside and between Jelly pages
* rename refactoring to rename view folders
* support `jenkins-module` packaging
* Java hint about `VirtualChannel.call` and `VirtualChannel.callAsync` (also `FilePath.act`)
  on anonymous inner classes or classes otherwise lacking `serialVersionUID`
  (cf. [JENKINS-14667](https://issues.jenkins-ci.org/browse/JENKINS-14667) and [b807845](https://github.com/jenkinsci/jenkins/commit/b807845b9b03bbe02babcf03fa7e6dbd80b41fcf))
* Code completion or similar for Groovy views
* Offer something like [jelly2groovy](https://github.com/slide/jelly2groovy) as a context menu action on Jelly views
* hyperlink methods in `Messages` to their `Messages.properties` definitions
* `textbox.jelly` cannot be validated: tried to load nonexistent `hudson.util.jelly.MorphTagLibrary.xsd`
* should prompt user for `Messages` key (if it can be detected that the hint is running interactively)
* `st:include` should hyperlink the included page (when it can be statically determined)
