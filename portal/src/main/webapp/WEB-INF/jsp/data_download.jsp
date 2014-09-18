<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.util.ValueParser" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ShowData" %>
<%@ page import="org.mskcc.cbio.portal.model.DownloadLink" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>

<script type="text/javascript" src="js/src/data_download.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div class="section" id="data_download">

    <div id='data_download_tab_links_li_div'>
        <h3>The following are tab-delimited data files:</h3>
        <ul id='data_downlonad_links_li'></ul>
    </div><!-- end data_download_tab_info_div -->

    <div id='data_download_tab_copy_tables_div'>
        <h3>Contents can be copied and pasted into Excel.</h3>
        <h3><small>Frequency of Gene Alteration:</small></h3>
        <textarea rows="10" cols="40" id="text_area_frenquency"></textarea><br><br>
        <h3><small>Type of Genetic alterations across all cases: (Alterations are summarized as MUT, Gain, HetLoss, etc.)</small></h3>
        <textarea rows="10" cols="40" id="text_area_type_of_alteration"></textarea><br><br>
        <h3><small>Cases affected: (Only cases with an alteration are included)</small></h3>
        <textarea rows="10" cols="40" id="text_area_case_affected"></textarea><br><br>
        <h3><small>Case matrix: (1= Case harbors alteration in one of the input genes)</small></h3>
        <textarea rows="10" cols="40" id="text_area_case_matrix"></textarea><br><br>
    </div><!-- end data_download_tab_copy_tables_div -->

</div><!-- end data download div -->

<%
    String debugStr = request.getParameter("xdebug");
    boolean debug = false;
    if (debugStr != null && debugStr.equals("1")) {
        debug = true;
    }
    String textOnly = request.getParameter("text_only");
%>

<!--
<ul>
<%
    ArrayList<DownloadLink> downloadLinkList = (ArrayList<DownloadLink>)
            request.getSession().getAttribute(QueryBuilder.DOWNLOAD_LINKS);

    int i = 0;
    for (DownloadLink link:  downloadLinkList) {
        out.println ("<li><a href='show_data.do?" + ShowData.INDEX + "=" + i + "'>"
                + link.getProfile().getProfileName() +"</a>:  ["
                + link.getGeneList().size() + " genes]");
        out.println ("&nbsp;<a href='show_data.do?transpose_matrix=1&" + ShowData.INDEX + "=" + i + "'>" 
                + "[Transposed Matrix]</a></li>");
        i++;
    }
%>
</ul>

<%
        out.print ("GENE_SYMBOL\tNUM_CASES_ALTERED\tPERCENT_CASES_ALTERED\n");
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            out.print (geneWithScore.getGene() + "\t");
            double fractionAltered = dataSummary.getPercentCasesWhereGeneIsAltered
                            (geneWithScore.getGene());
            out.print (dataSummary.getNumCasesWhereGeneIsAltered(geneWithScore.getGene()) + "\t");
            out.println (percentFormat.format(fractionAltered));
        }
             %>
        </textarea>
<%
        out.println ("<P>Type of Genetic alterations across all cases:  (Alterations are summarized as MUT, Gain, HetLoss, etc.)");
        out.println ("<P><textarea rows=10 cols=80 id=\"heat_map\">");
        out.print ("Case ID\t");
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            out.print(geneWithScore.getGene().toUpperCase().trim() + "\t");
        }
        out.print ("\n");
        for (String patientId : mergedPatientList) {
            out.print(patientId + "\t");
            for (GeneWithScore geneWithScore : geneWithScoreList) {
                String value = mergedProfile.getValue(geneWithScore.getGene(), patientId);
                ValueParser parser = ValueParser.generateValueParser( geneWithScore.getGene(), value, 
                         zScoreThreshold, rppaScoreThreshold, theOncoPrintSpecification );
                if( null == parser){
                   System.err.println( "null valueParser: cannot find: " + geneWithScore.getGene() );
                   break;
                }
                if (parser.isCnaAmplified()) {
                    out.print ("AMP;");
                }
                if (parser.isCnaHomozygouslyDeleted()) {
                   out.print ("HOMDEL;");
               }
                if (parser.isCnaGained()) {
                   out.print ("GAIN;");
               }
                if (parser.isCnaHemizygouslyDeleted()) {
                   out.print ("HETLOSS;");
               }
                if (parser.isMutated()) {
                    out.print ("MUT;");
                }
                if (parser.isMRNAWayUp()) {
                    out.print ("UP;");
                }
                if (parser.isMRNAWayDown()) {
                    out.print ("DOWN;");
                }
                if (parser.isRPPAWayUp()) {
                    out.print("RPPA-UP;");
                }
                if (parser.isRPPAWayDown()) {
                    out.print("RPPA-DOWN;");
                }
                out.print ("\t");
            }
            out.print ("\n");
        }
        out.println ("</textarea>");

        out.println ("<P>Cases affected:  (Only cases with an alteration are included)");
        out.println ("<P><textarea rows=10 cols=80>");
        for (String patientId : mergedPatientList) {
            if (dataSummary.isCaseAltered(patientId)) {
                out.println (patientId);
            }
        }
        out.println ("</textarea>");

        out.println ("<P>Case matrix: (1= Case harbors alteration in one of the input genes)</P>");
        out.println ("<P><textarea rows=10 cols=80>");
        for (String patientId : mergedPatientList) {
            if (dataSummary.isCaseAltered(patientId)) {
                out.println (patientId + "\t1");
            } else {
                out.println (patientId + "\t0");
            }
        }
        out.println ("</textarea>");
%>
-->

