<%@ page import="org.mskcc.cbio.portal.util.Config" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>
<%@ page import="org.mskcc.cbio.portal.util.DataSetsUtil" %> 
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.cgds.model.CancerStudyStats" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<%
   Config globalConfig = Config.getInstance();
   String dataSetsHtml = globalConfig.getProperty("data_sets");
   String siteTitle = SkinUtil.getTitle();
   String dataSetsHeader = SkinUtil.getDataSetsHeader();
   String dataSetsFooter = SkinUtil.getDataSetsFooter();
   String segFileURL = globalConfig.getProperty("segfile.url");
   DataSetsUtil dataSetsUtil = new DataSetsUtil();

   // get list of cancer study stats to process
   List<CancerStudyStats> cancerStudyStats = null;
   try {
	   cancerStudyStats = dataSetsUtil.getCancerStudyStats();
   }
   catch (Exception e) {
	   cancerStudyStats = new ArrayList<CancerStudyStats>();
   }

   // we may have to insert num cancer studies in header
   if (dataSetsHeader != null && dataSetsHeader.indexOf("<NUM_CANCER_STUDIES>") != -1) {
      dataSetsHeader = dataSetsHeader.replace("<NUM_CANCER_STUDIES>", Integer.toString(cancerStudyStats.size()));
   }
%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Data Sets"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
<div id="main">
    <table cellspacing="2px">
        <tr>
            <td width="100%">
                <h1>Data Sets</h1>
           <div class="markdown">
           <p>
			  <%
			     if (dataSetsHeader != null) {
				     out.println("<p>" + dataSetsHeader + "<br><br></p>");
                 }
				 out.println("<table style='display:none;' cellpadding='0' cellspacing='0' border='0' class='display' id='data-set-table'>");
				 out.println("<thead><tr>");
				 out.println("<th>CancerStudy</th>");
				 out.println("<th>Reference</th>");
				 out.println("<th>All</th>");
				 out.println("<th>Sequenced</th>");
				 out.println("<th>aCGH</th>");
				 out.println("<th>Tumor mRNA (RNA-Seq)</th>");
				 out.println("<th>Tumor mRNA (microarray)</th>");
				 //out.println("<th>Normal mRNA</th>");
				 out.println("<th>Tumor miRNA</th>");
				 out.println("<th>Methylation</th>");
				 out.println("<th>RPPA</th>");
				 out.println("<th>Complete</th>");
				 out.println("</tr></thead>");
				 out.println("<tbody>");
				 // iterate over all cancer study attributes
				 int lc = 0;
				 for (CancerStudyStats stats : cancerStudyStats) {
					 // alternate every other row color
					 out.println(((++lc % 2) == 0) ? "<tr>" : "<tr class=\"rowcolor\">");
					 // cancer study name
					 String stableID = stats.getStableID();
					 String studyName = stats.getStudyName();
                                         String htmlStudyName = "<a href='"
                                                + SkinUtil.getLinkToCancerStudyView(stableID)
                                                + "'>" + studyName + "</a>";
                                         String reference = stats.getReference();
					 out.println("<td style=\"text-align: left;\"><b>" + htmlStudyName + "</b></td>");
					 out.println("<td style=\"text-align: left;\"><b>" + reference + "</b></td>");
					 // all
					 out.println("<td style=\"text-align: center;\"><b>" + stats.getAll() + "</b></td>");
					 // sequenced
					 String sequenced = (stats.getSequenced() != 0) ? stats.getSequenced().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + sequenced + "</td>");
					 // aCGH
					 String aCGH = (stats.getACGH() != 0) ? stats.getACGH().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + aCGH  + "</td>");
					 // RNA-Seq
					 String rnaSEQ = (stats.getRNASEQ() != 0) ? stats.getRNASEQ().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + rnaSEQ + "</td>");
					 // tumor mRNA
					 String mRNA = (stats.getTumorMRNA() != 0) ? stats.getTumorMRNA().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + mRNA + "</td>");
					 // normal
					 /*
					 String normal = stats.getNormal().toString();
					 /*
					 if (stableID.equals("prad_mskcc")) {
						 normal = "30";
					 }
					 else if (stableID.equals("sarc_mskcc")) {
						 normal = "8";
					 }
					 out.println("<td style=\"text-align: center;\">" + normal + "</td>");
					 */
					 // tumor miRNA
					 String miRNA = (stats.getTumorMIRNA() != 0) ? stats.getTumorMIRNA().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + miRNA + "</td>");
					 // methlyation
					 String meth = (stats.getMethylation() != 0) ? stats.getMethylation().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + meth + "</td>");
					 // rppa
					 String rppa = (stats.getRPPA() != 0) ? stats.getRPPA().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + rppa + "</td>");
					 // complete
					 String complete = (stats.getComplete() != 0) ? stats.getComplete().toString() : "";
					 out.println("<td style=\"text-align: center;\"><b>" + complete + "<b></td>");
				     out.println("</tr>");
				 }
				 out.println("</tbody></table>");
				 out.println("<br>Total number of samples: <b>" + dataSetsUtil.getTotalNumberOfSamples() + "</b>");
				 if (dataSetsFooter != null) {
				     out.println("<br><br>" + dataSetsFooter + "</p>");
                 }
			  %>
			</p>
          </div> 
            </td>
        </tr>
    </table>
</div>
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

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .dataTables_length {
                width: auto;
                float: right;
        }
        .dataTables_info {
                clear: none;
                width: auto;
                float: right;
        }
        .div.datatable-paging {
                width: auto;
                float: right;
        }
</style>

<script type="text/javascript">
    $('#data-set-table').dataTable({
                    "sDom": '<"H"fr>t<"F"<"datatable-paging"pil>>', // selectable columns
                    "bJQueryUI": true,
                    "bDestroy": true,
                    "oLanguage": {
                        "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                        "sInfoFiltered": "",
                        "sLengthMenu": "Show _MENU_ per page"
                    },
                    "iDisplayLength": -1,
                    "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
                }).show();
</script>

</body>
</html>
