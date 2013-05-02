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

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Parameters;

@Hint(displayName = "#DN_MessagesHint", description = "#DESC_MessagesHint", category = "general", severity=Severity.HINT)
@Messages({
    "DN_MessagesHint=Use localizer.java.net",
    "DESC_MessagesHint=Adds a key to Messages.properties and replaces selected string with a Messages.java method call."
})
public class MessagesHint {

    @TriggerTreeKind({Tree.Kind.STRING_LITERAL, Tree.Kind.PLUS})
    @Messages("ERR_MessagesHint=Hardcoded string")
    public static ErrorDescription hardcodedString(HintContext ctx) {
        if (ctx.getInfo().getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE).findResource("org/jvnet/localizer/LocaleProvider.class") == null) {
            return null;
        }
        TreePath path = ctx.getPath();
        if (path.getParentPath().getLeaf().getKind() == Tree.Kind.PLUS) {
            return null; // only show on outermost enclosing tree
        }
        Text text = textOf((ExpressionTree) path.getLeaf(), true, 0);
        if (text.literal.isEmpty()) {
            return null; // no strings involved
        }
        assert text.properlyQuoted() : text.messageFormat;
        Fix fix = new FixImpl(ctx.getInfo(), path).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, path, Bundle.ERR_MessagesHint(), fix);
    }

    private static final class FixImpl extends JavaFix {

        FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }
        
        @Messages("FIX_MessagesHint=Replace with Messages from localizer.java.net")
        @Override protected String getText() {
            return Bundle.FIX_MessagesHint();
        }

        @Messages({"# {0} - resource path", "MessagesHint.create_resource=You need to create a resource {0} to use this hint."})
        @Override protected void performRewrite(TransformationContext ctx) throws Exception {
            Parameters.notNull("ctx", ctx);
            WorkingCopy wc = ctx.getWorkingCopy();
            if (wc == null) {
                throw new IllegalStateException();
            }
            CompilationUnitTree cu = wc.getCompilationUnit();
            if (cu == null) {
                throw new IllegalStateException();
            }
            ExpressionTree packageName = cu.getPackageName();
            if (packageName == null) {
                throw new IllegalStateException();
            }
            String packageNameS = packageName.toString();
            if (packageNameS == null) {
                throw new IllegalStateException();
            }
            String resource = packageNameS.replace('.', '/') + "/Messages.properties";
            FileObject messagesProperties = wc.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(resource);
            if (messagesProperties == null) {
                StatusDisplayer.getDefault().setStatusText(Bundle.MessagesHint_create_resource(resource));
                return;
            }
            EditableProperties ep = new EditableProperties(true);
            InputStream is = ctx.getResourceContent(messagesProperties);
            try {
                ep.load(is);
            } finally {
                is.close();
            }
            Text text = textOf((ExpressionTree) ctx.getPath().getLeaf(), true, 0);
            String cname = ((ClassTree) cu.getTypeDecls().get(0)).getSimpleName().toString();
            String key = cname + '.' + text.key();
            String id = toJavaIdentifier(key);
            ep.put(key, text.messageFormat);
            OutputStream os = ctx.getResourceOutput(messagesProperties);
            try {
                ep.store(os);
            } finally {
                os.close();
            }
            TreeMaker make = wc.getTreeMaker();
            wc.rewrite(ctx.getPath().getLeaf(), make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("Messages"), id), text.params));
        }

    }

    private static class Text {
        final String messageFormat;
        final String literal;
        final List<ExpressionTree> params;
        Text(String messageFormat, String literal, List<ExpressionTree> params) {
            this.messageFormat = messageFormat;
            this.literal = literal;
            this.params = params;
        }
        /** only call on a top-level Text */
        boolean properlyQuoted() {
            Object[] blanks = new String[params.size()];
            for (int i = 0; i < blanks.length; i++) {
                blanks[i] = "";
            }
            return literal.equals(MessageFormat.format(messageFormat, blanks));
        }
        String key() {
            String k = literal.replaceAll("[^a-zA-Z0-9_]+", "_").toLowerCase(Locale.ENGLISH);
            if (k.length() > 40) {
                k = k.substring(0, 40);
            }
            return k;
        }
    }

    private static @NonNull Text textOf(ExpressionTree tree, boolean topLevel, int idx) {
        switch (tree.getKind()) {
        case STRING_LITERAL:
            String text = (String) ((LiteralTree) tree).getValue();
            return new Text(text.replace("'", "''").replace("{", "'{'"), text, Collections.<ExpressionTree>emptyList());
        case PLUS:
            BinaryTree plus = (BinaryTree) tree;
            Text lhs = textOf(plus.getLeftOperand(), false, idx);
            Text rhs = textOf(plus.getRightOperand(), false, idx + lhs.params.size());
            List<ExpressionTree> exprs = new ArrayList<ExpressionTree>(lhs.params);
            exprs.addAll(rhs.params);
            return new Text(lhs.messageFormat + rhs.messageFormat, lhs.literal + rhs.literal, exprs);
        default:
            assert !topLevel;
            return new Text("{" + idx + "}", "", Collections.singletonList(tree));
        }
    }

    // Cf. org.jvnet.localizer.Generator
    private static String toJavaIdentifier(String key) {
        return key.replace('.', '_');
    }

    private MessagesHint() {}

}
