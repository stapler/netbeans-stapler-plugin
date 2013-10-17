package org.kohsuke.stapler.netbeans.plugin;

import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

public class ApplicationTest extends NbTestCase {

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ApplicationTest.class).
                gui(false).
                failOnMessage(Level.WARNING).
                failOnException(Level.INFO).
                suite();
    }

    public ApplicationTest(String n) {
        super(n);
    }

    public void testApplication() {
        // pass if there are merely no warnings/exceptions
    }

}
