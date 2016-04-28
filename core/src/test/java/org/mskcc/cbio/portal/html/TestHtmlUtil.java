/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.html;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

/**
 * Tests the HtmlUtil Class.
 *
 * @author Ethan Cerami.
 */
public class TestHtmlUtil {
	
	@Test
    public void test1() {
        String html = HtmlUtil.createLink("http://www.google.com", "Google");
        assertEquals("<a href='http://www.google.com'>Google</a>", html);

        html = HtmlUtil.createLink(null, null);
        assertEquals("", html);
    }
	@Test
    public void test2() {
        String html = HtmlUtil.createLinkWithinSpan("http://www.google.com", "Google", "box");
        assertEquals("<span class='box'><a href='http://www.google.com'>Google</a></span>", html);

        html = HtmlUtil.createLinkWithinSpan(null, null, null);
        assertEquals("", html);
    }
	@Test
    public void test3() {
        String html = HtmlUtil.createLinkWithinSpan("Google", "box");
        assertEquals("<span class='box'>Google</span>", html);

        html = HtmlUtil.createLinkWithinSpan(null, null);
        assertEquals("", html);
    }
	@Test
    public void test4() {
        String html = HtmlUtil.createEmptySpacer();
        assertEquals("&nbsp;", html);
    }
	@Test
    public void test5() {
        String html = HtmlUtil.createLinkWithinSpan("http://www.google.com", "Google", "box1",
                "box2");
        assertEquals("<span class='box1 box2'><a href='http://www.google.com'>Google</a></span>",
                html);
        html = HtmlUtil.createLinkWithinSpan(null, null, null);
        assertEquals("", html);
    }
	@Test
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
	@Test
    public void test7() {
        String html = HtmlUtil.createTextWithinSpan("hello", "bold");
        assertEquals("<span class='bold'>hello</span>", html);
    }
	@Test
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
