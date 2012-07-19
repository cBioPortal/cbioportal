<%@ page import="org.mskcc.portal.r_bridge.SurvivalAnalysis" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="org.mskcc.portal.util.UrlUtil" %>
<%@ page import="org.mskcc.cgds.model.ClinicalData" %>

<div class="section" id="survival">


    <%
//         String osUrl = UrlUtil.getCurrentUrl(request) + "&" + QueryBuilder.OUTPUT + "="
//                     + QueryBuilder.OS_SURVIVAL_PLOT;
		
    	String osUrl = UrlUtil.getUrlWithCaseIdsKey(request) + QueryBuilder.OUTPUT + "="
			+ QueryBuilder.OS_SURVIVAL_PLOT;

        String osPdfUrl = osUrl.replace("index.do", "survival_plot.pdf") + "&format=pdf";
        
//         String dfsUrl = UrlUtil.getCurrentUrl(request) + "&" + QueryBuilder.OUTPUT + "="
//                     + QueryBuilder.DFS_SURVIVAL_PLOT;
        
        String dfsUrl = UrlUtil.getUrlWithCaseIdsKey(request) + QueryBuilder.OUTPUT + "="
                + QueryBuilder.DFS_SURVIVAL_PLOT;
    
        String dfsPdfUrl = dfsUrl.replace("index.do", "survival_plot.pdf") + "&format=pdf";
        if (dataSummary.getNumCasesAffected() == 0) {
            out.println("<H4>No cases are altered for the specified gene set.  Therefore, survival analysis is not available.");
        } else {
            try {
                SurvivalAnalysis survivalAnalysis = new SurvivalAnalysis(clinicalDataList, dataSummary);
                DecimalFormat decimalFormat = new DecimalFormat("#.######");
                if (survivalAnalysis.getOsError() == 1) {
                    out.println("<H4>Overall Survival:  Not Available Due to Missing or Incomplete Data.</h4>");
                } else if (survivalAnalysis.numGroups() <= 1) {
                    out.println("<H4>Overall Survival:</h4>");
                    out.println("<div id=\"load\">&nbsp;</div>");
                    out.println("<div class=\"markdown\">");
                    out.println("<IMG src=\"" + osUrl + "\" width=600 height=600>");
                    out.println("</div>");
                } else {
                    out.println("<H4>Overall Survival, Logrank Test P-Value:  "
                            + decimalFormat.format(survivalAnalysis.getOsLogRankPValue())
                            + " [<a href='" + osPdfUrl + "'>PDF</a>]</h4>");
                    out.println("<div id=\"load\">&nbsp;</div>");
                    out.println("<div class=\"markdown\">");
                    out.println("<IMG src=\"" + osUrl + "\" width=600 height=600>");
                    out.println("<PRE><CODE>");
                    out.println(survivalAnalysis.getOsSurvivalTable());
                    out.println("</CODE></PRE>");
                    out.println("</div>");
                }

                if (survivalAnalysis.getDfsError() == 1) {
                    out.println("<BR><H4>Disease Free Survival:  Not Available Due to Missing or Incomplete Data.</h4>");
                } else if (survivalAnalysis.numGroups() <=1) {
                    out.println("<BR><H4>Disease Free Survival:</h4>");
                    out.println("<div id=\"load\">&nbsp;</div>");
                    out.println("<P><IMG src=\"" + dfsUrl + "\" width=600 height=600>");
                } else {
                    out.println("<BR><H4>Disease Free Survival, Logrank Test P-Value:  "
                            + decimalFormat.format(survivalAnalysis.getDfsLogRankPValue())
                            + " [<a href='" + dfsPdfUrl + "'>PDF</a>]</h4>");
                    out.println("<div id=\"load\">&nbsp;</div>");
                    out.println("<P><IMG src=\"" + dfsUrl + "\" width=600 height=600>");
                    out.println("<div class=\"markdown\">");
                    out.println("<PRE><CODE>");
                    out.println(survivalAnalysis.getDfsSurvivalTable());
                    out.println("</CODE></PRE>");
                    out.println("</div>");
                }
            } catch (Exception exc) {
                out.println("<div class=\"markdown\">");
                out.println("<PRE><CODE>");
                out.println(exc.getMessage());
                //exc.printStackTrace();
                out.println("</CODE></PRE>");
                out.println("</div>");
            }
        }
    %>
</div>
