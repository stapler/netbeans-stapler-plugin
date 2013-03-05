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

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;

public class JellyHyperlinkProviderTest {

    private Document doc;

    @Before public void createDoc() throws Exception {
        doc = new PlainDocument();
        doc.insertString(0,
                  "<j:jelly xmlns:j=\"jelly:core\" xmlns:st=\"jelly:stapler\" xmlns:d=\"jelly:define\" xmlns:l=\"/lib/layout\"\n"
                + "          xmlns:t=\"/lib/hudson\" xmlns:f=\"/lib/form\" xmlns:i=\"jelly:fmt\" xmlns:p=\"/lib/hudson/project\">\n"
                + "  <f:entry title=\"${%Display Name}\" field=\"displayName\">\n"
                + "    <f:textbox />\n"
                + "  </f:entry>\n"
                + "  <j:if test=\"${whatever}\"/>\n"
                + "  <f:entry title=\"\">\n"
                + "    <div align=\"right\">\n"
                + "      <f:repeatableDeleteButton/>\n"
                + "    </div>\n"
                + "  </f:entry>\n"
                + "  <p:config-trigger />\n"
                + "</j:jelly>\n", null);
    }

    private int offsetOf(String text) throws Exception {
        int i = doc.getText(0, doc.getLength()).indexOf(text);
        assert i != -1;
        return i;
    }

    private String textFrom(int[] offsets) throws Exception {
        return doc.getText(0, doc.getLength()).substring(offsets[0], offsets[1]);
    }

    @Test public void getHyperlinkSpan() throws Exception {
        assertEquals("f:entry", textFrom(JellyHyperlinkProvider.hyperlinkSpan(doc, offsetOf("ntry title=\"${"))));
        assertEquals("f:repeatableDeleteButton", textFrom(JellyHyperlinkProvider.hyperlinkSpan(doc, offsetOf("peatableD"))));
        assertEquals("f:textbox", textFrom(JellyHyperlinkProvider.hyperlinkSpan(doc, offsetOf("f:textbox"))));
        assertEquals("p:config-trigger", textFrom(JellyHyperlinkProvider.hyperlinkSpan(doc, offsetOf("ig-tr"))));
    }

    @Test public void correspondingJellyResource() throws Exception {
        assertEquals(null, JellyHyperlinkProvider.correspondingJellyResource(doc, offsetOf("iv align")));
        assertEquals("/lib/form/entry.jelly", JellyHyperlinkProvider.correspondingJellyResource(doc, offsetOf("ntry title=\"${")));
        assertEquals("/lib/form/entry.jelly", JellyHyperlinkProvider.correspondingJellyResource(doc, offsetOf("ntry title=\"\"")));
        assertEquals("/lib/form/repeatableDeleteButton.jelly", JellyHyperlinkProvider.correspondingJellyResource(doc, offsetOf("peatableD")));
        assertEquals("/lib/form/textbox.jelly", JellyHyperlinkProvider.correspondingJellyResource(doc, offsetOf("f:textbox")));
        assertEquals("/lib/hudson/project/config-trigger.jelly", JellyHyperlinkProvider.correspondingJellyResource(doc, offsetOf("trigger")));
        assertEquals(null, JellyHyperlinkProvider.correspondingJellyResource(doc, offsetOf("j:if")));
    }

    // XXX test for taglib locating in performClickAction
    
}