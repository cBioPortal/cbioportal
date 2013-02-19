<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>

<%
    org.mskcc.cbio.portal.servlet.ServletXssUtil xssUtil = ServletXssUtil.getInstance();
    Config globalConfig = Config.getInstance();
    String siteTitle = SkinUtil.getTitle();
    String popeye = globalConfig.getProperty("popeye");

    if (popeye == null) {
        popeye = "preview.jsp";
    } 
    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
    String tabIndex = xssUtil.getCleanInput(request, QueryBuilder.TAB_INDEX);
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
    
$(document).ready(function(){
    $('#td-content').width("70%");
    if ($.browser.msie) {
        var version = $.browser.version;
        version = /^([0-9]+)/.exec(version);
        if (version && version.length && parseInt(version[0])<=8) {
            $('#ie8-warning').show();
        }
    }
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
                        <p><%= SkinUtil.getBlurb() %></p>
                        <p>Please adhere to <a href="http://cancergenome.nih.gov/abouttcga/policies/publicationguidelines">
                            the TCGA publication guidelines</a> when using any TCGA data in your publications.</p>
                        <p>The portal is developed and maintained by
                            the <a href="http://cbio.mskcc.org/">Computational Biology Center</a>
                            at <a href="http://www.mskcc.org/">Memorial
                            Sloan-Kettering Cancer Center</a>. </p>
                        <p><i>Cancer Discovery</i>. May 2012 2; 401. [<a href="http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract">Abstract</a>].</p>
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
