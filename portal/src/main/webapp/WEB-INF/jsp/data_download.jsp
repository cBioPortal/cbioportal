<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.util.ValueParser" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="org.mskcc.cbio.portal.model.DownloadLink" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>

<script type="text/javascript" src="js/src/data_download.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div class="section" id="data_download">

    <div id='data_download_tab_links_li_div'>
        <h4>The following are downloadable data files (click to download) </h4>
        <ul id='data_download_links_li'></ul>
        <div id='data_download_redirect_home_page'></div>
    </div><!-- end data_download_tab_info_div -->

    <div id='data_download_tab_text_areas'>
        <h4>Contents below can be copied and pasted into Excel</h4><br>
        <h3><small>Frequency of Gene Alteration:</small></h3>
        <textarea class="form-control" id="text_area_gene_alteration_freq" title="Frequency of Gene Alteration"></textarea><br>
        <h3><small>Type of Genetic alterations across all cases: (Alterations are summarized as MUT, Gain, HetLoss, etc.)</small></h3>
        <textarea class="form-control" id="text_area_gene_alteration_type" title="Type of Genetic alterations across all cases"></textarea><br>
        <h3><small>Cases affected: (Only cases with an alteration are included)</small></h3>
        <textarea class="form-control" id="text_area_case_affected" title="Cases affected"></textarea><br>
        <h3><small>Case matrix: (1= Case harbors alteration in one of the input genes)</small></h3>
        <textarea class="form-control" id="text_area_case_matrix" title="Case matrix"></textarea><br>
    </div>

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

    #data_download_tab_text_areas {
        margin-top: 30px;
    }

</style>

