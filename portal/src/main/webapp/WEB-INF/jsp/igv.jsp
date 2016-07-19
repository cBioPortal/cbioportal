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

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.google.common.base.Joiner" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.IGVLinking" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoGeneOptimized" %>
<%@ page import="org.mskcc.cbio.portal.model.CanonicalGene" %>

    <!-- Bootstrap CSS - for demo only, NOT REQUIRED FOR IGV -->
    <link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">

    <!-- jQuery UI CSS -->
    <link rel="stylesheet" type="text/css"
          href="//ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/themes/smoothness/jquery-ui.css"/>

    <!-- Font Awesome CSS -->
    <link rel="stylesheet" type="text/css"
          href="//maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css"/>

    <!-- IGV CSS -->
    <link rel="stylesheet" type="text/css" href="//igv.org/web/release/1.0.1/igv-1.0.1.css">

    <!-- bam.css n - for demo only, NOT REQUIRED FOR IGV -->
    <link rel="stylesheet" type="text/css" href="././css/bam.css">

    <!--
   <script type="text/javascript" src="././js/lib/jquery.min.js"></script>
    
  <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js"></script>

     
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
-->
    
    <script type="text/javascript" src="//igv.org/web/release/1.0.1/igv-1.0.1.js"></script>


<div class="section" id="igv_tab">IGV
    <!---<div id="gene"> </div>
 
                <a id="igvLaunch" href="#" onclick="prepIGVForSegView('<%= cancerTypeId %>')"><img src="images/webstart.jpg" alt="Launch IGV"/></a>
                <br>

 -->               
        
</div>

<script type="text/javascript">
/*{geneList: "KRAS+NRAS+BRAF", 
fileName: "coadread_tcga_pub_data_cna_hg19.seg", 
referenceId: "hg19",
segfileUrl:"http://cbio.mskcc.org/cancergenomics/public-portal/seg/coadread_tcga_pub_data_cna_hg19.seg"}
*/
/*
options = {
            showNavigation: true,
            showRuler: true,
            genome: "hg19",
            locus: "egfr",
            tracks: [
                        {
                            url: 'http://cbio.mskcc.org/cancergenomics/public-portal/seg/coadread_tcga_pub_data_cna_hg19.seg',
                            indexed: false,
                            name: 'Segmented CN'
                        }                      
            ]
        };
igv.createBrowser("#gene", options);

*/

</script>
