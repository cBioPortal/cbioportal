package org.mskcc.portal.test.html;

import junit.framework.TestCase;
import org.mskcc.portal.html.HtmlUtil;

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
        String html = HtmlUtil.createSpacer();
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
        assertEquals("<thead>\n" +
                "<tr>\n" +
                "<td>First</td>\n" +
                "<td>Last</td>\n" +
                "<td>Zip Code</td>\n" +
                "</tr>\n" +
                "</thead>\n", html);
        html = HtmlUtil.createTableHeaderRow(null);
        assertEquals("", html);
    }

}
