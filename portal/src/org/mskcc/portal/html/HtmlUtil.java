package org.mskcc.portal.html;

import java.util.ArrayList;

/**
 * Misc. Public Static Methods for Creating HTML.
 *
 * In the event of Null Object parameters, most methods will return an
 * EMPTY_STRING for safe HTML rendering.
 *
 * @author Ethan Cerami.
 */
public class HtmlUtil {
    public static final String EMPTY_STRING = "";

    public static String createLink(String href, String text) {
        if (href==null || text == null) {
            return EMPTY_STRING;
        }
        return ("<a href='" + href + "'>" + text + "</a>");
    }

    public static String createLinkWithinSpan(String href, String text, String cssClass) {
        if (href==null || text == null || cssClass == null) {
            return EMPTY_STRING;
        }
        return ("<span class='" + cssClass + "'><a href='" + href + "'>" + text + "</a></span>");
    }

    public static String createLinkWithinSpan(String href, String text, String cssClass1,
        String cssClass2) {
        if (href==null || text == null || cssClass1 == null || cssClass2 == null) {
            return EMPTY_STRING;
        }
        return ("<span class='" + cssClass1 + " " + cssClass2 
            + "'><a href='" + href + "'>" + text + "</a></span>");
    }

    public static String createLinkWithinSpan(String text, String cssClass) {
        if (text == null || cssClass == null) {
            return EMPTY_STRING;
        }
        return ("<span class='" + cssClass + "'>" + text + "</span>");
    }

    public static String createSpacer() {
        return ("&nbsp;");
    }

    //  Creates a Row of Headers within a Table
    public static String createTableHeaderRow (ArrayList<String> headerList) {
        HtmlWriter writer = new HtmlWriter();
        if (headerList != null) {
            writer.append("<thead>");
            writer.append("<tr>");
            outputHeaders(headerList, writer);
            writer.append("</tr>");
            writer.append("</thead>");
            return writer.getHtml();
        } else {
            return EMPTY_STRING;
        }
    }

    private static void outputHeaders(ArrayList<String> headerList, HtmlWriter writer) {
        for (String header:  headerList) {
            writer.append("<td>" + header + "</td>");
        }
    }
}

class HtmlWriter {
    private static final String NEW_LINE = "\n";
    StringBuffer writer = new StringBuffer();

    void append(String html) {
        writer.append(html + NEW_LINE);
    }

    String getHtml() {
        return writer.toString();
    }

}