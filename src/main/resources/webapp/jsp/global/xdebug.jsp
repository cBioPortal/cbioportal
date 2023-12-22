<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.mskcc.cbio.portal.util.XDebug" %>
<%@ page import="org.mskcc.cbio.portal.util.XDebugParameter" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.cbio.portal.util.XDebugMessage" %>
<%
    XDebug xdebug = (XDebug) request.getAttribute("xdebug_object");
    if (xdebug != null) {
        xdebug.stopTimer();
        Enumeration enum0 = request.getAttributeNames();
        while (enum0.hasMoreElements()) {
            String name = (String) enum0.nextElement();
            Object value = request.getAttribute(name);
            xdebug.addParameter(XDebugParameter.REQUEST_ATTRIBUTE_TYPE, name,
                    value.toString());
        }
%>
<%
    String xdebugParameter = request.getParameter("xdebug");
    if (xdebugParameter != null) {
%>
<table class="xdebug" width="100%">
    <tr bgcolor="#DDDDDD" valign="top">
		<td align="left">
            Total Time to Generate Page
        </td>
		<td colspan="2">
            <%= xdebug.getTimeElapsed() %> ms
        </td>
    </tr>
    <tr bgcolor="#DDDDDD" valign="top">
		<td align="left">
            Current Time
        </td>
		<td colspan="2">
            <%= new Date() %> ms
        </td>
	</tr>
    <tr bgcolor="#DDDDDD" valign="top">
		<td align="left">
            Class Name
        </td>
		<td align="left" colspan="2">
            Message
        </td>
	</tr>

	<%--
				***********************************
				Output Log Messages
				***********************************
	--%>
	<%
        ArrayList messages = xdebug.getDebugMessages();
        for (int msgIndex = 0; msgIndex < messages.size(); msgIndex++) {
            XDebugMessage msg = (XDebugMessage) messages.get(msgIndex);
    %>
		<tr bgcolor="#ccccff" valign="top">
			<td>
                <%= wrapText(msg.getClassName()) %>
            </td>
			<td colspan="2">
				<font color="<%= msg.getColor() %>">
				<%= wrapText(msg.getMessage()) %>
				</font>
            </td>
		</tr>
    <% } %>
	<%--
				***********************************
				Output Parameter Values
				***********************************
	--%>
	<tr bgcolor="#DDDDDD">
		<td align="left">Parameter Type</td>
		<td align="left">Name</td>
		<td align="left">Value</td>
	</tr>
	<%
		ArrayList parameters = xdebug.getParameters();
		String bgcolor;
		for (int paramIndex=0; paramIndex<parameters.size(); paramIndex++) {
			XDebugParameter param = (XDebugParameter)
                    parameters.get(paramIndex);
			if (param.getType()==XDebugParameter.USER_TYPE)
                bgcolor = "#C9FFD3";
			else if (param.getType()==XDebugParameter.HTTP_HEADER_TYPE)
                bgcolor="LIGHTBLUE";
			else if (param.getType()==XDebugParameter.COOKIE_TYPE)
                bgcolor="PINK";
			else if (param.getType()==XDebugParameter.SESSION_TYPE)
                bgcolor="#FFCBFF";
			else if (param.getType()==XDebugParameter.ENVIRONMENT_TYPE)
                bgcolor="LIGHTYELLOW";
			else if (param.getType()==XDebugParameter.HTTP_TYPE)
                bgcolor="LIGHTGREEN";
			else if (param.getType()==XDebugParameter.REQUEST_ATTRIBUTE_TYPE)
                bgcolor="LIGHTYELLOW";
            else if (param.getType()==XDebugParameter.SERVLET_CONTEXT_TYPE)
                bgcolor="LIGHTBLUE";
			else bgcolor = "WHITE";
	%>

		<tr bgcolor="<%= bgcolor %>">
			<td valign="top"><%= wrapText(param.getTypeName()) %></td>
			<td valign="top"><%= wrapText(param.getName()) %></td>
			<td valign="top"><%= wrapText(param.getValue()) %></td>
		</tr>
		<% } %>
</table>
<% } %>
<% } %>

<%!

//  This is a little hack to replace .'s with .'s followed by a zero-width spacer.
//  Enables browsers to wrap long words within tables
private String wrapText (String text) {
    if (text != null) {
        text = text.replaceAll("\\.", "\\. ");
        text = text.replaceAll("&", "&amp;");
        return text;
    } else {
        return new String ("Not Available");
    }
}
%>
