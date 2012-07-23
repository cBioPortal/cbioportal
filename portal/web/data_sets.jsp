<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%@ page import="org.mskcc.portal.util.DataSetsUtil" %> 
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cgds.model.CancerStudyStats" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<%
   // i thought about putting these in db or CancerStudy class
   // but think its best to leave them here for now...
   String pradMSKCCPublishedStudyLink = "<a href=\"http://www.cell.com/cancer-cell/fulltext/S1535-6108(10)00238-2\">Cancer Cell</a>";
   String sarcMSKCCPublishedStudyLink = "<a href=\"http://www.nature.com/ng/journal/vaop/ncurrent/full/ng.619.html\">Nature</a>";
   String gbmTCGAPublishedStudyLink = "<a href=\"http://www.nature.com/nature/journal/v455/n7216/full/nature07385.html\">Nature</a>";
   String ovTCGAPublishedStudyLink = "<a href=\"http://www.nature.com/nature/journal/v474/n7353/full/nature10166.html\">Nature</a>";

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
				 out.println("<table>");
				 out.println("<tr>");
				 out.println("<th>CancerStudy</th>");
				 out.println("<th>Ref.</th>");
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
				 out.println("</tr>");
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
                                         String reference = "";
					 if (stableID.equals("prad_mskcc")) {
						 reference = pradMSKCCPublishedStudyLink;
					 }
					 else if (stableID.equals("sarc_mskcc")) {
						 reference = sarcMSKCCPublishedStudyLink;
					 }
					 else if (stableID.equals("gbm_tcga")) {
						 reference = gbmTCGAPublishedStudyLink;
					 }
					 else if (stableID.equals("ov_tcga")) {
						 reference = ovTCGAPublishedStudyLink;
					 }
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
				 out.println("</table>");
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
