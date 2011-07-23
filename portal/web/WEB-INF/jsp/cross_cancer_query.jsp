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


                <div class="markdown" id="cancer_results">
                </div>

                <script type="text/javascript">
                $(document).ready(function(){
                    jQuery.getJSON("cancer_studies.json",function(json){
                        jQuery.each(json,function(key,cancer_study){
                            //$("#results").append('<p>' + cancer_study);
                            $("#cancer_results").append('<h1>Cancer Study:  ' + key + '</h1>');
                            $("#cancer_results").append('<h2>Genomic Profiles:' + '</h2>');
                            $("#cancer_results").append('<ul>');
                            jQuery.each(cancer_study.genomic_profiles,function(i, genomic_profile) {
                                $("#cancer_results").append('<li>' + genomic_profile.name + ': ' + genomic_profile.description + "</li>'");
                            }); //  end for each genomic profile loop
                            $("#cancer_results").append('</ul>');
                            $("#cancer_results").append('<h2>Case Sets:' + '</h2>');
                            $("#cancer_results").append('<ul>');
                            jQuery.each(cancer_study.case_sets,function(i, case_set) {
                                $("#cancer_results").append('<li>' + case_set.name + ': ' + case_set.description + "</li>'");
                            }); //  end for each genomic profile loop
                            $("#cancer_results").append('</ul>');
                        });  //  end for each cancer study loop
                    });  //  end getJSON function
                });  //  end document ready function
                </script>
                
                <br/>
                <h4>Gene Set:</h4>
                <form id="main_form" action="cross_cancer.do" method="post">
                <br/>
                <textarea name="<%= QueryBuilder.GENE_LIST%>" rows="5" cols="50" placeholder="Enter HUGO Gene Symbols" required></textarea>
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
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</div>
</center>
<jsp:include page="global/xdebug.jsp" flush="true" />
</body>
</html>
