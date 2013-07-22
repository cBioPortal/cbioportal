/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.html;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.html.HtmlUtil;

import java.util.ArrayList;

/**
 * Tests the HtmlUtil Class.
 *
 * @author Ethan Cerami.
 */
public class TestHtmlUtil extends TestCase {

    public void test1() {
        String html = HtmlUtil.createLink("http://www.google.com", "Google");
        assertEquals("<a href='http://www.google.com'>Google</a>", html);

        html = HtmlUtil.createLink(null, null);
        assertEquals("", html);
    }

    public void test2() {
        String html = HtmlUtil.createLinkWithinSpan("http://www.google.com", "Google", "box");
        assertEquals("<span class='box'><a href='http://www.google.com'>Google</a></span>", html);

        html = HtmlUtil.createLinkWithinSpan(null, null, null);
        assertEquals("", html);
    }

    public void test3() {
        String html = HtmlUtil.createLinkWithinSpan("Google", "box");
        assertEquals("<span class='box'>Google</span>", html);

        html = HtmlUtil.createLinkWithinSpan(null, null);
        assertEquals("", html);
    }

    public void test4() {
        String html = HtmlUtil.createEmptySpacer();
        assertEquals("&nbsp;", html);
    }

    public void test5() {
        String html = HtmlUtil.createLinkWithinSpan("http://www.google.com", "Google", "box1",
                "box2");
        assertEquals("<span class='box1 box2'><a href='http://www.google.com'>Google</a></span>",
                html);
        html = HtmlUtil.createLinkWithinSpan(null, null, null);
        assertEquals("", html);
    }

    public void test6() {
        ArrayList<String> headerList = new ArrayList<String>();
        headerList.add("First");
        headerList.add("Last");
        headerList.add("Zip Code");
        String html = HtmlUtil.createTableHeaderRow(headerList);
        assertEquals("<tr>\n" +
                "<th>First</th>\n" +
                "<th>Last</th>\n" +
                "<th>Zip Code</th>\n" +
                "</tr>\n", html);
        html = HtmlUtil.createTableHeaderRow(null);
        assertEquals("", html);
    }

    public void test7() {
        String html = HtmlUtil.createTextWithinSpan("hello", "bold");
        assertEquals("<span class='bold'>hello</span>", html);
    }

    public void test8() {
        ArrayList<String> dataFieldList = new ArrayList<String>();
        dataFieldList.add("Ethan");
        dataFieldList.add("Cerami");
        dataFieldList.add("10021");
        String html = HtmlUtil.createTableRow(dataFieldList);
        assertEquals("<tr>\n" +
                "<td><nobr>Ethan</nobr></td>\n" +
                "<td>Cerami</td>\n" +
                "<td>10021</td>\n" +
                "</tr>\n", html);
        html = HtmlUtil.createTableRow(null);
        assertEquals("", html);
    }

}
