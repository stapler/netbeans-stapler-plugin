A NetBeans IDE plugin to support the Stapler web framework.
See [NetBeans plugin for Stapler](https://wiki.jenkins-ci.org/display/JENKINS/NetBeans+plugin+for+Stapler) for background.

[Plugin Portal downloads](http://plugins.netbeans.org/plugin/43938/)

Implemented in 1.3:

* navigate between types and their Jelly view folders (creating view folder as needed)
* New File template for Jelly scripts
* prominent Jenkins plugin archetype

Implemented in 1.2:

* Taking over Jenkins-dev-specific functionality of current `maven.hudson` module (in IDE distro),
  in a separate plugin (see [NB #219789](https://netbeans.org/bugzilla/show_bug.cgi?id=219789) for patch):
    * default actions, icon for `hpi` packaging
    * `text/x-jelly+xml` MIME registration and Jelly/Stapler schema validation support
    * show `localhost:8080` when running Jenkins
* Signing NBMs.

Implemented in 1.1:

* Packaging improvements.
* NullPointerException fix.

Implemented in 1.0:

* replace hardcoded string (`"including " + variable + " elements"`) with `Messages.properties`;
  see [Internationalization](https://wiki.jenkins-ci.org/display/JENKINS/Internationalization)

To do:

* Stapler view navigation should walk up the inheritance hierarchy if necessary
* Stapler view creation should assume src/main/resources if there are multiple resource folders available
* extend Output Window hyperlink to work from `exec:exec` on Winstone, `hudson-dev:run`, etc.
* find usages, find subtypes, etc. inside and between Jelly pages
* rename refactoring to rename view folders
* support `jenkins-module` packaging
* Java hint about `VirtualChannel.call` and `VirtualChannel.callAsync` (also `FilePath.act`)
  on anonymous inner classes or classes otherwise lacking `serialVersionUID`
  (cf. [JENKINS-14667](https://issues.jenkins-ci.org/browse/JENKINS-14667) and [b807845](https://github.com/jenkinsci/jenkins/commit/b807845b9b03bbe02babcf03fa7e6dbd80b41fcf))
* Java hint to replace `SecurityContextHolder.getContext().setAuthentication` with `ACL.impersonate` in 1.462+
* Code completion or similar for Groovy views
* Offer something like [jelly2groovy](https://github.com/slide/jelly2groovy) as a context menu action on Jelly views

Also see:

* http://wiki.netbeans.org/HudsonInNetBeans
* https://github.com/CloudBees-community/netbeans-plugin
