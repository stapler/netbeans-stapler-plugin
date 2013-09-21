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

import java.net.URI;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Lookup;

@LookupMerger.Registration(projectType="org-netbeans-modules-maven/hpi")
public class PluginSharabilityQueryMerger implements LookupMerger<SharabilityQueryImplementation2> {

    @Override public Class<SharabilityQueryImplementation2> getMergeableClass() {
        return SharabilityQueryImplementation2.class;
    }

    @Override public SharabilityQueryImplementation2 merge(final Lookup lookup) {
        return new SharabilityQueryImplementation2() {
            @Override public SharabilityQuery.Sharability getSharability(URI uri) {
                // Tried to separately add a @ProjectServiceProvider with a lower priority, but MavenSharabilityQueryImpl was always first in lookup, so doing it this way instead.
                // Will clash with any future LookupMerger<SharabilityQueryImplementation2> applied to projectType="org-netbeans-modules-maven", alas.
                Project project = FileOwnerQuery.getOwner(uri);
                if (project != null && (uri.equals(project.getProjectDirectory().toURI().resolve("work")) || uri.equals(project.getProjectDirectory().toURI().resolve("work/")))) {
                    System.err.println("TODO NOT_SHARABLE: " + uri);
                    return SharabilityQuery.Sharability.NOT_SHARABLE;
                }
                for (SharabilityQueryImplementation2 impl : lookup.lookupAll(SharabilityQueryImplementation2.class)) {
                    SharabilityQuery.Sharability r = impl.getSharability(uri);
                    if (r != SharabilityQuery.Sharability.UNKNOWN) {
                        return r;
                    }
                }
                return SharabilityQuery.Sharability.UNKNOWN;
            }
        };
    }

}
