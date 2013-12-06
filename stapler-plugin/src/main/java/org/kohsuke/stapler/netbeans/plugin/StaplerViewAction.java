/*
 * The MIT License
 *
 * Copyright 2012 CloudBees.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle.Messages;

@ActionID(category="Project", id="org.kohsuke.stapler.netbeans.plugin.StaplerViewAction")
@ActionRegistration(displayName="#CTL_StaplerViewAction")
@ActionReference(path="Menu/GoTo", position=411)
@Messages("CTL_StaplerViewAction=Go to Stapler View/Model")
public final class StaplerViewAction implements ActionListener {

    private final FileObject file;

    public StaplerViewAction(DataObject context) {
        file = context.getPrimaryFile();
    }

    @Override public void actionPerformed(ActionEvent ev) {
        ClassPath sources = ClassPath.getClassPath(file, ClassPath.SOURCE);
        if (sources == null) {
            StatusDisplayer.getDefault().setStatusText(FileUtil.getFileDisplayName(file) + " does not appear to be in any source path");
            return;
        }
        try {
            if (file.hasExt("java")) { // file is the model
                String name = sources.getResourceName(file);
                assert name != null : file + " not really in " + sources;
                String vname = name.replaceFirst("[.]java$", "");
                FileObject viewF = sources.findResource(vname);
                if (viewF == null) { // no view yet
                    Project p = FileOwnerQuery.getOwner(file);
                    if (p == null) {
                        StatusDisplayer.getDefault().setStatusText("No project for " + FileUtil.getFileDisplayName(file));
                        return;
                    }
                    SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
                    if (groups.length == 0) {
                        groups = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    }
                    if (groups.length == 0 || groups.length > 1) {
                        StatusDisplayer.getDefault().setStatusText("Not sure where to create resources for " + ProjectUtils.getInformation(p).getDisplayName());
                        return;
                    }
                    try {
                        viewF = FileUtil.createFolder(groups[0].getRootFolder(), vname);
                    } catch (IOException x) {
                        StatusDisplayer.getDefault().setStatusText("Could not create view folder " + vname + " in " + FileUtil.getFileDisplayName(groups[0].getRootFolder()));
                        return;
                    }
                    instantiate(viewF);
                } else {
                    // pick a commonly-used view
                    for (String page : new String[] {"config", "global", "index"}) {
                        for (String lang : new String[] {"groovy", "jelly"}) {
                            FileObject view = sources.findResource(vname + "/" + page + "." + lang);
                            if (view != null) {
                                NbDocument.openDocument(DataObject.find(view), 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                                return;
                            }
                        }
                    }
                    // next, look for any view (not e.g. *.properties)
                    for (FileObject view : viewF.getChildren()) {
                        if (view.isData() && (view.hasExt("groovy") || view.hasExt("jelly"))) {
                            NbDocument.openDocument(DataObject.find(view), 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            return;
                        }
                    }
                    // OK, just look for anything here
                    for (FileObject view : viewF.getChildren()) {
                        if (view.isData()) {
                            NbDocument.openDocument(DataObject.find(view), 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            return;
                        }
                    }
                    // just an empty folder
                    instantiate(viewF);
                }
            } else { // file is the view
                FileObject folder = file.isFolder() ? file : file.getParent();
                String name = sources.getResourceName(folder);
                assert name != null : folder + " not really in " + sources;
                while (true) {
                    FileObject model = sources.findResource(name + ".java");
                    if (model != null) {
                        NbDocument.openDocument(DataObject.find(model), /* TODO nicer to jump to actual type */ 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                        break;
                    }
                    int slash = name.lastIndexOf('/');
                    if (slash == -1) {
                        StatusDisplayer.getDefault().setStatusText("No model found matching " + FileUtil.getFileDisplayName(folder));
                        break;
                    }
                    name = name.substring(0, slash);
                }
            }
        } catch (DataObjectNotFoundException x) {
            assert false : x;
        }
    }

    private void instantiate(FileObject viewF) {
        TemplateWizard wiz = new TemplateWizard();
        wiz.setTargetName("config"); // TODO this does not work, why?
        try {
            FileObject template = FileUtil.getConfigFile("Templates/XML/sample.jelly");
            assert template != null;
            Set<DataObject> created = wiz.instantiate(DataObject.find(template), DataFolder.findFolder(viewF));
            if (created != null) {
                for (DataObject nue : created) {
                    NbDocument.openDocument(nue, 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                }
            }
        } catch (IOException x) {
            Logger.getLogger(StaplerViewAction.class.getName()).log(Level.WARNING, null, x);
        }
    }

}
