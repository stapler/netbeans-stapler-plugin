package org.kohsuke.stapler.netbeans.plugin;

import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

public class ApplicationTest extends NbTestCase {

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ApplicationTest.class).
                gui(false).
                /* TODO fails with two exceptions: one about --jdkhome; another about jcl.over.slf4j_1.7.29 vs. org.slf4j
                failOnException(Level.INFO).
                */
                enableClasspathModules(false). // #271423
                clusters(".*").
                /* TODO does not suffice to hide, e.g.: the modules [org.netbeans.modules.form.nb] use org.jdesktop.layout which is deprecated: Use javax.swing.GroupLayout instead. …
                failOnMessage(Level.WARNING).
                hideExtraModules(true).
                enableModules("(?!org[.]netbeans[.]modules[.]masterfs[.](linux|macosx|windows)|org[.]netbeans[.]modules[.]extexecution[.]process[.]jdk9|org[.]netbeans[.]modules[.]form[.]nb).*").
                */
                suite();
    }

    public ApplicationTest(String n) {
        super(n);
    }

    public void testApplication() {
        // pass if there are merely no exceptions
    }

}
