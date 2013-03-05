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

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * Hyperlinks e.g. {@code <f:textbox />}.
 */
@MimeRegistration(service=HyperlinkProviderExt.class, mimeType=JellyTagSchemaCatalog.MIME_TYPE)
public class JellyHyperlinkProvider implements HyperlinkProviderExt {

    @Override public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return correspondingJellyResource(doc, offset) != null;
    }

    @Override public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return hyperlinkSpan(doc, offset);
    }

    @Override public void performClickAction(Document doc, int offset, HyperlinkType type) {
        FileObject f = NbEditorUtilities.getFileObject(doc);
        if (f == null) {
            StatusDisplayer.getDefault().setStatusText("No file associated with this document.");
            return;
        }
        String rAbsolute = correspondingJellyResource(doc, offset);
        assert rAbsolute != null;
        String r = rAbsolute.substring(1);
        FileObject owner = null;
        FileObject taglib = null;
        for (String cpType : new String[] {ClassPath.COMPILE, ClassPath.SOURCE}) { // EXECUTE lacks jenkins-core.jar!
            ClassPath cp = ClassPath.getClassPath(f, cpType);
            if (cp != null) {
                taglib = cp.findResource(r);
                if (taglib != null) {
                    owner = cp.findOwnerRoot(taglib);
                    assert owner != null;
                    break;
                }
            }
        }
        if (taglib == null) {
            StatusDisplayer.getDefault().setStatusText("Could not find " + r + " in classpath of " + FileUtil.getFileDisplayName(f));
            return;
        }
        SourceForBinaryQuery.Result2 sfbq = SourceForBinaryQuery.findSourceRoots2(owner.toURL());
        if (sfbq.preferSources()) {
            for (FileObject root : sfbq.getRoots()) {
                FileObject _taglib = root.getFileObject(r);
                if (_taglib != null) {
                    taglib = _taglib;
                }
            }
        }
        try {
            NbDocument.openDocument(DataObject.find(taglib), 0, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
        } catch (DataObjectNotFoundException x) {
            assert false : x;
        }
    }

    @Override public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return correspondingJellyResource(doc, offset);
    }
    
    private static final Pattern OPEN_TAG = Pattern.compile("<((\\w+):([\\w-]+))[> \n/]");

    static int[] hyperlinkSpan(Document doc, int offset) {
        try {
            String all = doc.getText(0, doc.getLength());
            int start = all.lastIndexOf('<', offset);
            if (start == -1) {
                return new int[] {0, 0};
            }
            Matcher m = OPEN_TAG.matcher(all.substring(start));
            if (!m.lookingAt()) {
                return new int[] {0, 0};
            }
            String fqn = m.group(1);
            return new int[] {start + 1, start + 1 + fqn.length()};
        } catch (BadLocationException x) {
            Exceptions.printStackTrace(x);
            return new int[] {0, 0};
        }
    }

    /**
     * Determines if the caret seems to be on a namespaced taglib.
     * I.e. if it is on an XML tag whose names starts with {@code /}.
     * @return {@code /lib/form/textbox.jelly} or null
     */
    static @CheckForNull String correspondingJellyResource(Document doc, int offset) {
        int[] span = hyperlinkSpan(doc, offset);
        if (span[1] == 0) {
            return null;
        }
        try {
            String fqn = doc.getText(span[0], span[1] - span[0]);
            int colon = fqn.indexOf(':');
            assert colon != -1 : fqn;
            String prefix = fqn.substring(0, colon);
            String element = fqn.substring(colon + 1);
            String all = doc.getText(0, doc.getLength());
            String decl = "xmlns:" + prefix + "=\"/";
            int declStart = all.indexOf(decl);
            if (declStart == -1) {
                return null;
            }
            int path = declStart + decl.length() - /* '/' */1;
            int end = all.indexOf('"', path);
            if (end == -1) {
                return null;
            }
            return all.substring(path, end) + "/" + element + ".jelly";
        } catch (BadLocationException x) {
            Exceptions.printStackTrace(x);
            return null;
        }
    }

}
