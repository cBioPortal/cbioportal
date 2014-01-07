<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.*" %>
<%@ page import="java.net.URLEncoder" %>

<%
    String siteTitle = GlobalProperties.getTitle();
    String popeye = GlobalProperties.getProperty("popeye");

    if (popeye == null) {
        popeye = "preview.jsp";
    } 
    if (siteTitle == null) {
        siteTitle = "cBioPortal for Cancer Genomics";
    }
    String tabIndex = request.getParameter(QueryBuilder.TAB_INDEX);
    if (tabIndex == null) {
        tabIndex = QueryBuilder.TAB_VISUALIZE;
    } else {
        tabIndex = URLEncoder.encode(tabIndex);
    }
%>

<%
    request.setAttribute("index.jsp", Boolean.TRUE);
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
%>

<jsp:include page="global/header.jsp" flush="true" />

<script type="text/javascript">

// non-jQuery function to display IE warning message
// TODO we may want to move this function into cbio.util
(function() {
	if (cbio.util.browser.msie)
	{
		var detectIE = function()
		{
			var version = cbio.util.browser.version;
			//version = /^([0-9]+)/.exec(version);
			if (version && version.length && parseInt(version) <= 8)
			{
				// show warning messages for IE 8 or below
				document.getElementById("ie8-warning").style.display = "block";
			}
		};

		if (window.addEventListener)
		{
			window.addEventListener('load', detectIE, false);
		}
		else if (window.attachEvent)
		{
			window.attachEvent('onload', detectIE);
		}
	}
})();

$(document).ready(function(){
    $('#td-content').width("70%");

	// TODO IE detection doesn't work inside document.ready because jQuery 2.0 functions
	// don't work with IE8 or below! So, the check is moved outside document.ready
//	if (cbio.util.browser.msie) {
//        var version = cbio.util.browser.version;
//        //version = /^([0-9]+)/.exec(version);
//        if (version && version.length && parseInt(version) <= 8)
//        {
//            $('#ie8-warning').show();
//        }
//    }
});
</script>

<p id="ie8-warning" style="background-color:red;display:none;">
    <img src="images/warning.gif"/>
    You are using an old version of Internet Explorer. For better performance, we recommend
    using <b>Google Chrome, Firefox, Safari, or Internet Explorer V9 or above to visit this web site.</b>.
</p>

    <table cellspacing="2px">
        <tr>
            <td>
            <div class="welcome">
                <table>
                <tr>
                   <td valign=top>
	              <div style="position: relative; z-index: 999;">
                        <p><%= GlobalProperties.getBlurb() %></p>
                        <p>Please adhere to <a href="http://cancergenome.nih.gov/abouttcga/policies/publicationguidelines">
                            the TCGA publication guidelines</a> when using any TCGA data in your publications.</p>
                        <p>The portal is developed and maintained by
                            the <a href="http://cbio.mskcc.org/">Computational Biology Center</a>
                            at <a href="http://www.mskcc.org/">Memorial
                            Sloan-Kettering Cancer Center</a>. </p>
                        <p>References: <a href="http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract">Cerami et al. <i>Cancer Discov.</i> 2012</a>
                                &amp;  <a href="http://www.cbioportal.org/public-portal/sci_signal_reprint.jsp">Gao et al. <i>Sci. Signal.</i> 2013.</p>
                        </div>
                        <br/>
                   </td>
                   <td valign=top>
			<jsp:include page="<%= popeye %>" flush="true" />
                   </td>
                </tr>
                </table>
            </div>

            <%
            if (userMessage != null) { %>
                <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:95%;margin-top:10px;margin-bottom:20px">
                    <p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
                    <strong><%= userMessage %></strong></p>
                </div>
            <% } %>

            <%
                //  Outputs Query and Download Tabs
                if (tabIndex.equals(QueryBuilder.TAB_VISUALIZE)) {
                    out.println ("<span class='tab_active'>Query</span>");
                    out.println ("<span class='tab_inactive'><a id='download_tab' href=''>Download Data</a></span>");
                } else {
                    out.println ("<span class='tab_inactive'><a id='query_tab' href=''>Query</a></span></span>");
                    out.println ("<span class='tab_active'>Download Data</span>");
                }
            %>
            <%@ include file="query_form.jsp" %>

            </td>
        </tr>
    </table>
    </td>
    <td>
	<jsp:include page="global/right_column.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td colspan="3">
        <script type="text/javascript">
            $(document).ready(function() {
               window.sessionStorage.clear(); 
            });
        </script>
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
