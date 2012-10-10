/*
 * The MIT License
 *
 * Copyright 2012 Jesse Glick.
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

package org.kohsuke.stapler.netbeans.plugin;

import java.net.URL;
import org.junit.Test;
import org.jvnet.localizer.LocaleProvider;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;

public class MessagesHintTest {

    @Test public void noLocalizer() throws Exception {
        HintTest.create()
                .input("package test;\n"
                + "public class Test {\n"
                + "    void m() {\n"
                + "        String s = \"hello\";\n"
                + "    }\n"
                + "}\n")
                .input("test/Messages.properties", "", false)
                .run(MessagesHint.class)
                .assertWarnings();
    }

    @Test public void simpleString() throws Exception {
        HintTest.create().classpath(cp())
                .input("package test;\n"
                + "public class Test {\n"
                + "    void m() {\n"
                + "        String s = \"hello\";\n"
                + "    }\n"
                + "}\n")
                .input("test/Messages.properties", "", false)
                .run(MessagesHint.class)
                .findWarning("3:19-3:26:hint:" + Bundle.ERR_MessagesHint())
                .applyFix()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    void m() {\n"
                + "        String s = Messages.Test_hello();\n"
                + "    }\n"
                + "}\n")
                .assertVerbatimOutput("test/Messages.properties", "Test.hello=hello\n");
    }

    @Test public void compoundString() throws Exception {
        HintTest.create().classpath(cp())
                .input("package test;\n"
                + "public class Test {\n"
                + "    void m(int count) {\n"
                + "        String s = \"hello \" + count + \" times\";\n"
                + "    }\n"
                + "}\n")
                .input("test/Messages.properties", "", false)
                .run(MessagesHint.class)
                .assertWarnings("3:19-3:46:hint:" + Bundle.ERR_MessagesHint())
                .findWarning("3:19-3:46:hint:" + Bundle.ERR_MessagesHint())
                .applyFix()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    void m(int count) {\n"
                + "        String s = Messages.Test_hello_times(count);\n"
                + "    }\n"
                + "}\n")
                .assertVerbatimOutput("test/Messages.properties", "Test.hello_times=hello {0} times\n");
    }

    @Test public void compoundString2() throws Exception {
        HintTest.create().classpath(cp())
                .input("package test;\n"
                + "public class Test {\n"
                + "    void m(String name, int count) {\n"
                + "        String s = \"hello \" + name + \", \" + count + \" times\";\n"
                + "    }\n"
                + "}\n")
                .input("test/Messages.properties", "", false)
                .run(MessagesHint.class)
                .assertWarnings("3:19-3:60:hint:" + Bundle.ERR_MessagesHint())
                .findWarning("3:19-3:60:hint:" + Bundle.ERR_MessagesHint())
                .applyFix()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    void m(String name, int count) {\n"
                + "        String s = Messages.Test_hello_times(name, count);\n"
                + "    }\n"
                + "}\n")
                .assertVerbatimOutput("test/Messages.properties", "Test.hello_times=hello {0}, {1} times\n");
    }

    @Test public void unrelatedAddition() throws Exception {
        HintTest.create().classpath(cp())
                .input("package test;\n"
                + "public class Test {\n"
                + "    void m(int w, int h) {\n"
                + "        int perimeter = 2 * (w + h);\n"
                + "    }\n"
                + "}\n")
                .input("test/Messages.properties", "", false)
                .run(MessagesHint.class)
                .assertWarnings();
    }

    @Test public void messageFormatMetaChars() throws Exception {
        HintTest.create().classpath(cp())
                .input("package test;\n"
                + "public class Test {\n"
                + "    String warning(String name) {\n"
                + "        return \"Don't kill \" + name + \" :-{\";\n"
                + "    }\n"
                + "}\n")
                .input("test/Messages.properties", "", false)
                .run(MessagesHint.class)
                .assertWarnings("3:15-3:44:hint:" + Bundle.ERR_MessagesHint())
                .findWarning("3:15-3:44:hint:" + Bundle.ERR_MessagesHint())
                .applyFix()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    String warning(String name) {\n"
                + "        return Messages.Test_don_t_kill_(name);\n"
                + "    }\n"
                + "}\n")
                .assertVerbatimOutput("test/Messages.properties", "Test.don_t_kill_=Don''t kill {0} :-'{'\n");
    }

    /** localizer uses MessageFormat even there are no parameters, unlike (say) NbBundle.Messages */
    @Test public void noParamMessageFormatMetaChars() throws Exception {
        HintTest.create().classpath(cp())
                .input("package test;\n"
                + "public class Test {\n"
                + "    String warning() {\n"
                + "        return \"Don't kill me :-{\";\n"
                + "    }\n"
                + "}\n")
                .input("test/Messages.properties", "", false)
                .run(MessagesHint.class)
                .assertWarnings("3:15-3:34:hint:" + Bundle.ERR_MessagesHint())
                .findWarning("3:15-3:34:hint:" + Bundle.ERR_MessagesHint())
                .applyFix()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    String warning() {\n"
                + "        return Messages.Test_don_t_kill_me_();\n"
                + "    }\n"
                + "}\n")
                .assertVerbatimOutput("test/Messages.properties", "Test.don_t_kill_me_=Don''t kill me :-'{'\n");
    }

    @Test public void veryLongMessage() throws Exception {
        HintTest.create().classpath(cp())
                .input("package test;\n"
                + "public class Test {\n"
                + "    String description() {\n"
                + "        return \"What would you say if I ran this hint out of turn? Would you stand up and revert the change on me?\";\n"
                + "    }\n"
                + "}\n")
                .input("test/Messages.properties", "", false)
                .run(MessagesHint.class)
                .findWarning("3:15-3:115:hint:" + Bundle.ERR_MessagesHint())
                .applyFix()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    String description() {\n"
                + "        return Messages.Test_what_would_you_say_if_i_ran_this_hint_ou();\n"
                + "    }\n"
                + "}\n")
                .assertVerbatimOutput("test/Messages.properties", "Test.what_would_you_say_if_i_ran_this_hint_ou=What would you say if I ran this hint out of turn? Would you stand up and revert the change on me?\n");
    }

    // XXX existing key with similar name means uniquify (but preferably prompt user)
    // XXX no Messages.properties initially
    // XXX adds to existing Messages.properties with formatting intact

    private URL cp() {
        URL cp = LocaleProvider.class.getProtectionDomain().getCodeSource().getLocation();
        return cp.toString().endsWith("/") ? cp : FileUtil.getArchiveRoot(cp);
    }

}
