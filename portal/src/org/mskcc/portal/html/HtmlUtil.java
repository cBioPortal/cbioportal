package org.mskcc.portal.html;

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
}