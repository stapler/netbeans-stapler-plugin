/*
 * The MIT License
 *
 * Copyright 2017 CloudBees.
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

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

@NbBundle.Messages("JenkinsRuleTest.java=JenkinsRule-based test")
public class JenkinsWizardIterators {

    @TemplateRegistration(content = "JenkinsRuleTest.java.template", folder = "UnitTests", position = 950, scriptEngine = "freemarker",
            displayName = "#JenkinsRuleTest.java",
            iconBase = "org/netbeans/modules/junit/ui/resources/JUnitLogo.png",
            description = "JenkinsRuleTest.html", category = "junit")
    public static WizardDescriptor.InstantiatingIterator<?> jenkinsRuleTest() {
        return JavaTemplates.createJavaTemplateIterator();
    }

    private JenkinsWizardIterators() {}

}
