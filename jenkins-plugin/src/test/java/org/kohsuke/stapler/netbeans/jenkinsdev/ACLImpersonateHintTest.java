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

import hudson.security.ACL;
import java.net.URL;
import org.acegisecurity.Authentication;
import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;

public class ACLImpersonateHintTest {

    @Test public void warningIssued() throws Exception {
        HintTest.create().classpath(cpify(ACL.class), cpify(Authentication.class))
                .input("package test;\n"
                + "import hudson.model.AbstractProject;\n"
                + "import hudson.security.ACL;\n"
                + "import jenkins.model.Jenkins;\n"
                + "import org.acegisecurity.Authentication;\n"
                + "import org.acegisecurity.context.SecurityContextHolder;\n"
                + "public class Test {\n"
                + "    void m() {\n"
                + "        Authentication a = SecurityContextHolder.getContext().getAuthentication();\n"
                + "        try {\n"
                + "            SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);\n"
                + "            Jenkins.getInstance().getAllItems(AbstractProject.class);\n"
                + "        } finally {\n"
                + "            SecurityContextHolder.getContext().setAuthentication(a);\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(ACLImpersonateHint.class)
                // XXX ideally would show just one warning for the whole idiom, but need to show it also on unclosed switches e.g. at start of new thread
                .assertWarnings("10:47-10:64:warning:" + Bundle.ERR_ACLImpersonateHint(), "13:47-13:64:warning:" + Bundle.ERR_ACLImpersonateHint());
    }

    private URL cpify(Class<?> c) {
        URL cp = c.getProtectionDomain().getCodeSource().getLocation();
        return cp.toString().endsWith("/") ? cp : FileUtil.getArchiveRoot(cp);
    }

}
