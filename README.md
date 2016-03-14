A NetBeans IDE plugin suite to support Jenkins plugin development and the Stapler web framework.
See [NetBeans plugin for Stapler](https://wiki.jenkins-ci.org/display/JENKINS/NetBeans+plugin+for+Stapler) for background.

[Plugin Portal downloads](http://plugins.netbeans.org/plugin/43938/)

Upcoming in 1.6:
* Always using the latest available version of the Jenkins plugin archetype.
* More readily usable Jelly template.
* Fixed browser opening from `mvn hpi:run` to work with newer versions of Jetty.

Implemented in 1.5:
* `*.jelly` tabs display the simple name of the corresponding model, e.g. `index.jelly [HelloWorldBuilder]`
* `*.jelly` did not show _History_ tab, and did not correctly display updated VCS modification status
* new versions of `hpi:run` publish `http://localhost:8080/jenkins/` rather than `http://localhost:8080/`; open the right one in the browser
* _Go to Stapler View/Model_ now prefers `*.jelly` to `*.properties`
* Stapler model navigation did not work from a view of a nested class

Implemented in 1.4:
* plugin template based on 1.509.3
* hyperlinking custom Jelly tags (e.g. `<f:textbox/>`) to their taglib definitions (`textbox.jelly`)
* editor hint for cases where `ACL.impersonate` is needed
* asks the project’s SCM to ignore the `work` directory of a plugin

Implemented in 1.3:

* navigate between types and their Jelly view folders (creating view folder as needed)
* New File template for Jelly scripts
* prominent Jenkins plugin archetype
* 7.3 baseline

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

Also see:

* http://wiki.netbeans.org/HudsonInNetBeans
* https://github.com/CloudBees-community/netbeans-plugin
