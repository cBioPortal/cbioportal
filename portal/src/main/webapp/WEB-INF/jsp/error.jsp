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
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%
    request.setAttribute(QueryBuilder.HTML_TITLE, "cBio Cancer Genomics Pathway Portal::Error");
    String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
    String emailContact = (userMessage != null && userMessage.contains("not authorized")) ?
      "cbioportal-access at cbio dot mskcc dot org" : GlobalProperties.getEmailContact();
%>

<jsp:include page="global/header.jsp" flush="true" />
            <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:95%;margin-top:10px;margin-bottom:20px">
            <p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
            <% if (userMessage != null) { %>
            <strong>Oops!  <%= userMessage %><br><br>You may have caught us in the middle of updating data. Please check back later.</strong></p>
            <% } else {%>
                Oops!  An Error Has occurred while processing your request.
              <% } %>
            &nbsp;Please try again or send email to <%= emailContact %> if this is any error in this portal.
            </div>
  </td>
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="global/xdebug.jsp" flush="true" />
</body>
</html>
