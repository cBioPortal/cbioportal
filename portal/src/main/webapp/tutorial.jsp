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
    String siteTitle = GlobalProperties.getTitle();
%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Video Tutorial"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />

    <div id="main">
        <table cellspacing="2px">
        <tr>
            <td>
                <h1>Tutorials</h1>
                <h2>Step-by-step Guide to cBioPortal: a Protocol Paper</h2>
                <p>Gao, Aksoy, Dogrusoz, Dresdner, Gross, Sumer, Sun, Jacobsen, Sinha, Larsson, Cerami, Sander, Schultz. <br/>
                    <b>Integrative analysis of complex cancer genomics and clinical profiles using the cBioPortal.</b> <br/>
                    <i>Sci. Signal.</i> 6, pl1 (2013).
                    [<a href="http://www.ncbi.nlm.nih.gov/pubmed/23550210">Reprint</a>].</p>
                
                <h2>Tutorial #1:  Getting Started</h2>
                <div style="width:595px" id="__ss_10438088"> <strong style="display:block;margin:12px 0 4px"><a href="http://www.slideshare.net/EthanCerami/cbio-cancer-genomics-portal-getting-started" title="cBioPortal for Cancer Genomics: Getting started" target="_blank">cBioPortal for Cancer Genomics: Getting started</a></strong> <iframe src="http://www.slideshare.net/slideshow/embed_code/10438088" width="595" height="497" frameborder="0" marginwidth="0" marginheight="0" scrolling="no"></iframe> <div style="padding:5px 0 12px"> View more <a href="http://www.slideshare.net/" target="_blank">presentations</a> from <a href="http://www.slideshare.net/EthanCerami" target="_blank">Ethan Cerami</a> </div> </div>

                <h2>Tutorial #2:  Network View</h2>
                <div style="width:595px" id="__ss_10579031"> <strong style="display:block;margin:12px 0 4px"><a href="http://www.slideshare.net/EthanCerami/network-view" title="cBioPortal for Cancer Genomics: Network View" target="_blank">cBioPortal for Cancer Genomics: Network View</a></strong> <iframe src="http://www.slideshare.net/slideshow/embed_code/10579031" width="595" height="497" frameborder="0" marginwidth="0" marginheight="0" scrolling="no"></iframe> <div style="padding:5px 0 12px"> View more <a href="http://www.slideshare.net/" target="_blank">presentations</a> from <a href="http://www.slideshare.net/EthanCerami" target="_blank">Ethan Cerami</a> </div> </div>
            </td>
        </tr>
    </table>
        </div>
    </td>
    <td width="172">
	<jsp:include page="WEB-INF/jsp/global/right_column.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />
</body>
</html>
