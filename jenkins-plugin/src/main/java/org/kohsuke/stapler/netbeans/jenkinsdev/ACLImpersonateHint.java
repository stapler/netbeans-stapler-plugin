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

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle.Messages;

/**
 * @see <a href="cf. https://github.com/h3xstream/find-sec-bugs/issues/8">FindBugs Sec issue</a>
 */
@Hint(displayName="#DN_ACLImpersonateHint", description="#DESC_ACLImpersonateHint", category="general", severity=Severity.WARNING, suppressWarnings=/* XXX FindBugs key when available */"ACL.impersonate")
@Messages({
    "DN_ACLImpersonateHint=Unsafe ACEGI Security idiom",
    "DESC_ACLImpersonateHint=Temporarily replacing the Authentication in the current SecurityContext is unsafe, because this context may be shared by multiple threads in the same HTTP session. Instead, temporarily switch to a different context, as with ACL.impersonate."
})
public class ACLImpersonateHint {

    @TriggerPattern("org.acegisecurity.context.SecurityContextHolder.getContext().setAuthentication($auth)")
    @Messages("ERR_ACLImpersonateHint=Use ACL.impersonate rather than setting authentication on the current security context")
    public static ErrorDescription hardcodedString(HintContext ctx) {
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ACLImpersonateHint());
        // XXX offer a fix (switch to ACL.impersonate) if ctx.getInfo().getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE).findResource("hudson/security/ACL.class") != null and 1.462+
    }

    private ACLImpersonateHint() {}

}
