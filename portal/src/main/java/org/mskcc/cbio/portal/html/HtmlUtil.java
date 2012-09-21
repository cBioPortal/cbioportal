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

    public static String createTextWithinSpan(String text, String cssClass) {
        if (text == null || cssClass == null) {
            return EMPTY_STRING;
        }
        return ("<span class='" + cssClass + "'>" + text + "</span>");
    }

	public static String createTextWithinSpan(String text, String cssClass, String title)
	{
		if (text == null || cssClass == null)
		{
			return EMPTY_STRING;
		}

		return ("<span class='" + cssClass + "' title='" + title + "'>" + text + "</span>");
	}

    public static String createLinkWithinSpan(String text, String cssClass) {
        if (text == null || cssClass == null) {
            return EMPTY_STRING;
        }
        return ("<span class='" + cssClass + "'>" + text + "</span>");
    }

    public static String createEmptySpacer() {
        return ("&nbsp;");
    }

    //  Creates a Row of Headers within a Table
    public static String createTableHeaderRow (ArrayList<String> headerList) {
        HtmlWriter writer = new HtmlWriter();
        if (headerList != null) {
            writer.append("<tr>");
            outputHeaders(headerList, writer);
            writer.append("</tr>");
            return writer.getHtml();
        } else {
            return EMPTY_STRING;
        }
    }

	//  Creates a row of headers with titles (used as tooltips)
	public static String createTableHeaderRowWithTitle(ArrayList<String> headerList) {
		HtmlWriter writer = new HtmlWriter();

		if (headerList != null)
		{
			writer.append("<tr>");

			for (String header:  headerList)
			{
				String[] parts = header.split("\t");

				String text = parts[0];

				if (parts.length > 1)
				{
					text = parts[1];
				}

				writer.append("<th title='" + parts[0] + "'>" +
				              text + "</th>");
			}

			writer.append("</tr>");
			return writer.getHtml();
		}
		else
		{
			return EMPTY_STRING;
		}
	}

    //  Creates a Row of Data within a Table
    public static String createTableRow (ArrayList<String> dataFieldList) {
        HtmlWriter writer = new HtmlWriter();
        if (dataFieldList != null) {
            writer.append("<tr>");
            outputDataFields(dataFieldList, writer);
            writer.append("</tr>");
            return writer.getHtml();
        } else {
            return EMPTY_STRING;
        }
    }

    public static String getSafeWebValue(String value) {
        if (value != null) {
            return value;
        } else {
            return HtmlUtil.createEmptySpacer();
        }
    }

    private static void outputHeaders(ArrayList<String> headerList, HtmlWriter writer) {
        for (String header:  headerList) {
            writer.append("<th>" + header + "</th>");
        }
    }

    private static void outputDataFields(ArrayList<String> dataFieldList, HtmlWriter writer) {
        for (int i=0; i<dataFieldList.size(); i++) {
            String header = dataFieldList.get(i);
            //  Do not allow breaking of 0th column
            if (i==0) {
                header = "<nobr>" + header + "</nobr>";
            }
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