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

<div class="section" id="igv_tab">
</div>

<script type="text/javascript">
           //igv graph
    $("a.result-tab").click(function(){
 /*      $.when($.ajax({
            method : "POST",
            url : 'igvlinking.json',
            data : {
                cmd : 'get_igv_args',
                cancer_study_id : _studyId,
                gene_list : window.QuerySession.getQueryGenes().join(" ")
            }
        })).then(
                function(response) {
                    igvForSegViewResp = response;
                    igv_data_fetched = true;
                    response['segfileUrl'], response['geneList'],
                            response['referenceId'], response['fileName'])
                });
*/
        var querryGenes =window.QuerySession.getQueryGenes();
        var targetGene = querryGenes[1];
          
        /*         
        {
            fileName:"coadread_tcga_pub_data_cna_hg19.seg"
            geneList:"KRAS+NRAS+BRAF"
            referenceId:"hg19"
            segfileUrl:"http://cbio.mskcc.org/cancergenomics/public-portal/seg/coadread_tcga_pub_data_cna_hg19.seg"
        }
        */
        options = {
                    showNavigation: true,
                    showRuler: true,
                    genome: "hg19",
                    locus: targetGene.toLowerCase(),
                    tracks: [
                        {
                            url: "http://cbio.mskcc.org/cancergenomics/public-portal/seg/coadread_tcga_pub_data_cna_hg19.seg",
                            indexed: false,
                            name: 'Segmented CN'
                        },
                        {
                            name: "Genes",
                            url: "https://s3.amazonaws.com/igv.broadinstitute.org/annotations/hg19/genes/gencode.v18.collapsed.bed",
                            order: Number.MAX_VALUE,
                            displayMode: "EXPANDED"
                        }
                    ]
                };
        igv.createBrowser("#igv_tab", options);
    });
</script>
