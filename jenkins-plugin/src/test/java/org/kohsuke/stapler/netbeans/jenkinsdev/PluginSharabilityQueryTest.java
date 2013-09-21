/*
 * The MIT License
 *
 * Copyright 2013 CloudBees.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.kohsuke.stapler.netbeans.jenkinsdev;

import java.io.File;
import java.io.PrintWriter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

public class PluginSharabilityQueryTest extends NbTestCase {

    public static Test suite() {
        //return NbModuleSuite.createConfiguration(PluginSharabilityQueryTest.class).gui(false).enableModules(".*").suite();
        return new TestSuite(); // TODO org.netbeans.Main not found, why?!
    }

    public PluginSharabilityQueryTest(String testName) {
        super(testName);
    }

    public void testSharability() throws Exception {
        File prj = new File(getWorkDir(), "prj");
        prj.mkdirs();
        PrintWriter pw = new PrintWriter(new File(prj, "pom.xml"), "UTF-8");
        try {
            pw.println("<project><modelVersion>4.0.0</modelVersion>");
            pw.println("<groupId>g</groupId><artifactId>a</artifactId>");
            pw.println("<packaging>hpi</packaging><version>0</version></project>");
            pw.flush();
        } finally {
            pw.close();
        }
        File work = new File(prj, "work");
        assertEquals(SharabilityQuery.Sharability.NOT_SHARABLE, SharabilityQuery.getSharability(Utilities.toURI(work)));
        work.mkdir();
        assertEquals(SharabilityQuery.Sharability.NOT_SHARABLE, SharabilityQuery.getSharability(Utilities.toURI(work)));
    }

}
