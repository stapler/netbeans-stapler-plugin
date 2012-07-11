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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;

// See: https://wiki.jenkins-ci.org/display/JENKINS/Internationalization

@Hint(displayName = "#DN_MessagesHint", description = "#DESC_MessagesHint", category = "general", severity=Severity.HINT)
@Messages({
    "DN_MessagesHint=Use localizer.java.net",
    "DESC_MessagesHint=Adds a key to Messages.properties and replaces selected string with a Messages.java method call."
})
public class MessagesHint {

    @TriggerTreeKind({Tree.Kind.STRING_LITERAL, Tree.Kind.PLUS})
    @Messages("ERR_MessagesHint=Hardcoded string")
    public static ErrorDescription hardcodedString(HintContext ctx) {
        FileObject messagesProperties = ctx.getInfo().getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(ctx.getInfo().getCompilationUnit().getPackageName().toString().replace('.', '/') + "/Messages.properties");
        if (messagesProperties == null) {
            return null;
        }
        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), messagesProperties).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_MessagesHint(), fix);
    }

    private static final class FixImpl extends JavaFix {

        private final FileObject messagesProperties;

        FixImpl(CompilationInfo info, TreePath tp, FileObject messagesProperties) {
            super(info, tp);
            this.messagesProperties = messagesProperties;
        }
        
        @Messages("FIX_MessagesHint=Replace with Messages from localizer.java.net")
        @Override protected String getText() {
            return Bundle.FIX_MessagesHint();
        }
        
        @Override protected void performRewrite(TransformationContext ctx) throws Exception {
            EditableProperties ep = new EditableProperties(true);
            InputStream is = ctx.getResourceContent(messagesProperties);
            try {
                ep.load(is);
            } finally {
                is.close();
            }
            LiteralTree string = (LiteralTree) ctx.getPath().getLeaf();
            String text = (String) string.getValue();
            String cname = ((ClassTree) ctx.getWorkingCopy().getCompilationUnit().getTypeDecls().get(0)).getSimpleName().toString();
            String key = cname + '.' + text;
            String id = toJavaIdentifier(key);
            ep.put(key, text);
            OutputStream os = ctx.getResourceOutput(messagesProperties);
            try {
                ep.store(os);
            } finally {
                os.close();
            }
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            List<ExpressionTree> args = Collections.<ExpressionTree>emptyList();
            ctx.getWorkingCopy().rewrite(string, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("Messages"), id), args));
        }

    }

    // Cf. org.jvnet.localizer.Generator
    private static String toJavaIdentifier(String key) {
        return key.replace('.', '_');
    }

    private MessagesHint() {}

}
