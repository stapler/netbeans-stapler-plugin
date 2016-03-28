/*
 * The MIT License
 *
 * Copyright 2016 CloudBees.
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

import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider;
import org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider;
import org.netbeans.spi.project.ProjectServiceProvider;

/** Like {@code MavenProjectProfilingSupportProvider}. */
@ProjectServiceProvider(service=ProjectProfilingSupportProvider.class, projectType="org-netbeans-modules-maven")
public class PluginProfilingSupportProvider extends JavaProjectProfilingSupportProvider {

    public PluginProfilingSupportProvider(Project project) {
        super(project);
    }

    @Override public boolean isProfilingSupported() {
        NbMavenProject mproject = getProject().getLookup().lookup(NbMavenProject.class);
        return mproject != null ? mproject.getPackagingType().equals("hpi") : false;
    }

    @Override public boolean checkProjectIsModifiedForProfiler() {
        return true;
    }

    @Override public JavaPlatform resolveProjectJavaPlatform() {
        return JavaPlatform.getDefaultPlatform();
    }

}
