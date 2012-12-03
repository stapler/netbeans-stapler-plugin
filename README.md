A NetBeans IDE plugin to support the Stapler web framework.
See [NetBeans plugin for Stapler](https://wiki.jenkins-ci.org/display/JENKINS/NetBeans+plugin+for+Stapler) for background.

[Plugin Portal downloads](http://plugins.netbeans.org/plugin/43938/)

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

* extend Output Window hyperlink to work from `exec:exec` on Winstone, `hudson-dev:run`, etc.
* navigate between types and their Jelly view folders (creating view folder as needed)
* find usages, find subtypes, etc. inside and between Jelly pages
* rename refactoring to rename view folders
* support `jenkins-module` packaging
* prominent Jenkins plugin archetype
* Java hint about `VirtualChannel.call` and `VirtualChannel.callAsync` (also `FilePath.act`)
  on anonymous inner classes or classes otherwise lacking `serialVersionUID`
  (cf. [JENKINS-14667](issues.jenkins-ci.org/browse/JENKINS-14667))

Also see:

* http://wiki.netbeans.org/HudsonInNetBeans
* https://github.com/CloudBees-community/netbeans-plugin
