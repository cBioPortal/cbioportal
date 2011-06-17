<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>

<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");

    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Video Tutorial"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />

    <div id="main">
    <table cellspacing="2px">
        <tr>
            <td>
                <h1>Video Tutorial</h1>
                    <p>   Check out our updated video tutorial below. </p>
                <br>
                <object width="640" height="505"><param name="movie"
                value="http://www.youtube.com/v/YW2Thf6PNS8?fs=1&amp;hl=en_US">
                </param><param name="allowFullScreen" value="true"></param>
                <param name="allowscriptaccess" value="always"></param>
                <embed src="http://www.youtube.com/v/YW2Thf6PNS8?fs=1&amp;hl=en_US"
               type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="640" height="505">
               </embed></object>
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
