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
        <h4>The following are tab-delimited data files:</h4>
        <ul id='data_downlonad_links_li'></ul>
    </div><!-- end data_download_tab_info_div -->

    <h4>Contents can be copied and pasted into Excel.</h4><br>
    <h3><small>Frequency of Gene Alteration:</small></h3>
    <textarea class="form-control" id="text_area_gene_alteration_freq"></textarea><br>
    <h3><small>Type of Genetic alterations across all cases: (Alterations are summarized as MUT, Gain, HetLoss, etc.)</small></h3>
    <textarea class="form-control" id="text_area_gene_alteration_type"></textarea><br>
    <h3><small>Cases affected: (Only cases with an alteration are included)</small></h3>
    <textarea class="form-control" id="text_area_case_affected"></textarea><br>
    <h3><small>Case matrix: (1= Case harbors alteration in one of the input genes)</small></h3>
    <textarea class="form-control" id="text_area_case_matrix"></textarea><br>

</div><!-- end data download div -->

<%
    String debugStr = request.getParameter("xdebug");
    boolean debug = false;
    if (debugStr != null && debugStr.equals("1")) {
        debug = true;
    }
    String textOnly = request.getParameter("text_only");
%>

<style>
    #data_download textarea {
        width: 90%;
        height: 200px;
    }
</style>


