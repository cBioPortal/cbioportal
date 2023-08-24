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

<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>

<%
    Logger log = LoggerFactory.getLogger(this.getClass());
    request.setAttribute("html_title", "MSK cBioPortal");
    String userId = (String)session.getAttribute("user_id");
    if (userId != null) {
       log.info("restful_login: Query initiated by user: " + userId);
    }
    else {
       log.info("restful_login: Query initiated by unknown user.");
    }
%>

<jsp:include page="src/main/webapp/jsp/global/header.jsp" flush="true" />
            <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:95%;margin-top:10px;margin-bottom:20px">
            <p><strong>Connecting to the MSK cBioPortal...</strong></p>
             </div>
             <div><img src="images/ajax-loader-no-text.gif" alt="loading" /></div>
  </td>
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="src/main/webapp/jsp/global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="src/main/webapp/jsp/global/xdebug.jsp" flush="true" />
</body>
</html>
