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

package org.kohsuke.stapler.netbeans.plugin;

import java.io.IOException;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.TopComponent;

@Messages("JellyDataObject=Jelly XML scripts")
@MIMEResolver.NamespaceRegistration(
    displayName="#JellyDataObject",
    position=454,
    mimeType=JellyDataObject.MIME_TYPE,
    acceptedExtension="jelly",
    elementName="jelly",
    elementNS="jelly:core"
)
@DataObject.Registration(displayName="#JellyDataObject", iconBase=JellyDataObject.ICON, mimeType=JellyDataObject.MIME_TYPE)
@ActionReferences({
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.OpenAction"), path=JellyDataObject.ACTIONS, position=100),
    @ActionReference(id=@ActionID(category="XML", id="org.netbeans.modules.xml.tools.actions.ValidateAction"), path=JellyDataObject.ACTIONS, position=300, separatorBefore=200),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.CutAction"), path=JellyDataObject.ACTIONS, position=600, separatorBefore=500),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.CopyAction"), path=JellyDataObject.ACTIONS, position=700, separatorAfter=800),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.DeleteAction"), path=JellyDataObject.ACTIONS, position=900),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.RenameAction"), path=JellyDataObject.ACTIONS, position=1000, separatorAfter=1100),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.SaveAsTemplateAction"), path=JellyDataObject.ACTIONS, position=1200),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.FileSystemAction"), path=JellyDataObject.ACTIONS, position=1250, separatorAfter=1300),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.ToolsAction"), path=JellyDataObject.ACTIONS, position=1400),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.PropertiesAction"), path=JellyDataObject.ACTIONS, position=1500)
})
public class JellyDataObject extends MultiDataObject {

    public static final String MIME_TYPE = "text/x-jelly+xml";

    /** copied from org/netbeans/modules/xml/resources/xmlObject.gif, which oddly is different from org/openide/loaders/xmlObject.gif */
    static final @StaticResource String ICON = "org/kohsuke/stapler/netbeans/plugin/xmlObject.gif";

    static final String ACTIONS = "Loaders/" + MIME_TYPE + "/Actions";

    public JellyDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new JellyDataEditor());
        cookies.add(new ValidateXMLSupport(DataObjectAdapters.inputSource(this)));
    }

    @Messages("CTL_SourceTabCaption=&Source")
    @MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase=ICON,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="jelly",
        mimeType=MIME_TYPE,
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override protected int associateLookup() {
        return 1;
    }

    /**
     * Inspired by {@code POMDataEditor}.
     */
    private class JellyDataEditor extends DataEditorSupport implements EditorCookie.Observable, OpenCookie, EditCookie, PrintCookie, CloseCookie {

        private final SaveCookie save = new SaveCookie() {
            public @Override void save() throws IOException {
                saveDocument();
            }
            @Override public String toString() {
                return getPrimaryFile().getNameExt();
            }
        };

        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                updateTitles();
            }
        };

        JellyDataEditor() {
            super(JellyDataObject.this, null, new JellyEnv(JellyDataObject.this));
            getPrimaryFile().addFileChangeListener(FileUtil.weakFileChangeListener(listener, getPrimaryFile()));
        }

        @Override protected CloneableEditorSupport.Pane createPane() {
            return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView(MIME_TYPE, getDataObject());
        }

        protected @Override boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            if (getLookup().lookup(SaveCookie.class) == null) {
                getCookieSet().add(save);
                setModified(true);
            }
            return true;
        }

        protected @Override void notifyUnmodified() {
            super.notifyUnmodified();
            if (getLookup().lookup(SaveCookie.class) == save) {
                getCookieSet().remove(save);
                setModified(false);
            }
        }

        protected @Override String messageName() {
            return super.messageName() + suffix();
        }

        protected @Override String messageHtmlName() {
            return super.messageHtmlName() + suffix();
        }

        private String suffix() {
            return " [" + getPrimaryFile().getParent().getName() + "]";
        }

        protected @Override boolean asynchronousOpen() {
            return true;
        }

    }

    private static class JellyEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1L;

        JellyEnv(MultiDataObject d) {
            super(d);
        }

        protected @Override FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected @Override FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        public @Override CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getLookup().lookup(JellyDataEditor.class);
        }

    }

}
