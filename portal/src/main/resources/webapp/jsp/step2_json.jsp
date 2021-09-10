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

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="static org.mskcc.cbio.portal.servlet.QueryBuilder.DATA_PRIORITY" %>
<%
    String step2ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP2_ERROR_MSG);
%>

<div class="query_step_section" id="step2">
    <span class="step_header">Select Genomic Profiles:</span>

<%
if (step2ErrorMsg != null) {
    out.println("<div class='ui-state-error ui-corner-all' style='margin-top:4px; padding:5px;'>"
            + "<span class='ui-icon ui-icon-alert' style='float: left; margin-right: .3em;'></span>"
            + "<strong>" + step2ErrorMsg + "</strong>");
}
%>

<div id='genomic_profiles'>
</div>

<%
if (step2ErrorMsg != null) {
    out.println ("</div>");
}
%>

</div>


<%
    Integer priority;
    try {
        priority = Integer.parseInt(request.getParameter(DATA_PRIORITY));
    } catch (NumberFormatException e) {
        priority = 0;
    }

    String checked[] = {"", "", ""};
    if(priority == null)
        priority = 0;

    checked[priority] = " checked";
%>

<div class="query_step_section" id="step2cross">
    <span class="step_header">Select Data Type Priority:</span>
    <input type="radio" name="<%= DATA_PRIORITY %>" id="pri_mutcna" value=0<%=checked[0]%>>
    <label for="pri_mutcna">Mutation and CNA</label>

    <input type="radio" name="<%= DATA_PRIORITY %>" id="pri_mut" value=1<%=checked[1]%>>
    <label for="pri_mut">Only Mutation</label>

    <input type="radio" name="<%= DATA_PRIORITY %>" id="pri_cna" value=2<%=checked[2]%>>
    <label for="pri_cna">Only CNA</label>
</div>


