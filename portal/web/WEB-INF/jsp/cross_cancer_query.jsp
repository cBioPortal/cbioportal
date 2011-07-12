<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%@ page import="org.mskcc.portal.util.Config" %>

<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");
    String popeye = globalConfig.getProperty("popeye");

    if (popeye == null) {
        popeye = "preview.jsp";
    } 
    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
%>

<%
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
%>


<jsp:include page="global/header.jsp" flush="true" />
    <table cellspacing="2px">
        <tr>
            <td>
            <div class="welcome">
                <table>
                <tr>
                   <td style="width: 350px">
                      <p><%= SkinUtil.getBlurb() %></p>
                      <p>The portal is developed and maintained by the <a href="http://cbio.mskcc.org/">Computational Biology Center</a>
                          at <br/><a href="http://www.mskcc.org/">Memorial Sloan-Kettering Cancer Center</a>. </p>
                   </td>
                   <td style="width: 300px">
                       <jsp:include page="<%= popeye %>" flush="true" />
                   </td>
                </tr>
                </table>
            </div>

            <%
            if (userMessage != null) {
                out.println ("<div class='user_message'>" + userMessage + "</div>");
            }
            %>                  
            <div class="main_panel">
                <br/>
                <h4>Enter HUGO Gene Symbols Below:</h4>
                <form id="main_form" action="cross_cancer.do" method="post">
                <br/>
                <textarea rows="5" cols="50"></textarea>
                <br/><br/>
                <input type="submit" value="Submit"/>
                </form>
            </div>

            </td>
        </tr>
    </table>

    <!-- End DIV for id="content" -->
    </div>

    </td>
    <td width="172">
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
</div>
</center>
<jsp:include page="global/xdebug.jsp" flush="true" />
</body>
</html>
