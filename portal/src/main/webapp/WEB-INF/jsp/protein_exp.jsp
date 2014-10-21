<%@ page import="org.mskcc.cbio.portal.servlet.ProteinArraySignificanceTestJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.web_api.GetProteinArrayData" %>
<%@ page import="java.util.*" %>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%
    Set<String> antibodyTypes = GetProteinArrayData.getProteinArrayTypes();
%>

<div class="section" id="protein_exp">
    <div id="protein_expr_wait"><img src="images/ajax-loader.gif"/></div>
    <table cellpadding="0" cellspacing="0" border="0" id="protein_expr_wrapper" width="100%">
        <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="protein_expr">
                <thead style="font-size:80%">
                    <tr valign="bottom">
                        <th rowspan="2">RPPA ID</th>
                        <th rowspan="2">Gene</th>
                        <th rowspan="2">Alteration</th>
                        <th rowspan="2">Type</th>
                        <th colspan="2" class="ui-state-default">Target</th>
                        <th colspan="3" class="ui-state-default">Ave. Abundance<img class="datatable_help" src="images/help.png" title="Average of protein abundance z-scores for unaltered cases and altered cases, respectively."/></th>
                        <th rowspan="2" nowrap="nowrap">p-value<img class="datatable_help" src="images/help.png" title="Based on two-sided two sample student t-test."/></th>
                        <th rowspan="2">data</th>
                        <th rowspan="2">Plot</th>
                    </tr>
                    <tr>
                        <th>Protein</th>
                        <th>Residue</th>
                        <th>Unaltered</th>
                        <th>Altered</th>
                        <th nowrap="nowrap">Abs. Diff.<!--img class="datatable_help" src="images/help.png" title="Absolute difference of average RPPA scores between altered and unaltered cases."/--></th>
                    </tr>
                </thead>
                <tfoot>
                    <tr valign="bottom">
                        <th>RPPA ID</th>
                        <th>Gene</th>
                        <th>Alteration</th>
                        <th>Type</th>
                        <th>Protein</th>
                        <th>Residue</th>
                        <th>Unaltered</th>
                        <th>Altered</th>
                        <th>Abs. Diff.</th>
                        <th>p-value</th>
                        <th>data</th>
                        <th>Plot</th>
                    </tr>
                </tfoot>
            </table>
        </td></tr>
    </table>
</div>

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
        @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
        #protein_exp .ColVis {
                float: left;
                margin-bottom: 0
        }
        .datatable-filter-custom {
                float: left
        }
        #protein_exp .dataTables_length {
                width: auto;
                float: right;
        }
        #protein_exp .dataTables_info {
                width: auto;
                float: right;
        }
        #protein_exp .div.datatable-paging {
                width: auto;
                float: right;
        }
        #protein_exp td.rppa-details {
                background-color : white;
        }
</style>

<script type="text/javascript" src="js/src/protein_exp/rppa_plots.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/protein_exp/protein_exp_datatable.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript">
    function getRppaPlotsCaseList() {
    <%
        JSONObject result = new JSONObject();
        for (String patientId : mergedPatientList) {
            //Is altered or not (x value)
            if (dataSummary.isCaseAltered(patientId)) {
                result.put(patientId, "altered");
            } else {
                result.put(patientId, "unaltered");
            }
        }
    %>
        var obj = jQuery.parseJSON('<%=result%>');
        return obj;
    }
    
    function getProteinArrayTypes() {
        var ret = Array();
        var i = 0;
        <%for (String type : antibodyTypes) {%>
                ret[i++] = "<%=type%>";
        <%}%>
        return ret;
    }

    function getAlterations() {
    <%
        JSONObject alterationResults = new JSONObject();
        for (String patientId : mergedPatientList) {
            JSONObject _alterationResult = new JSONObject();
            for (GeneWithScore geneWithScore : geneWithScoreList) {
                String singleGeneResult = "";
                String value = mergedProfile.getValue(geneWithScore.getGene(), patientId);
                ValueParser parser = ValueParser.generateValueParser( geneWithScore.getGene(), value,
                        zScoreThreshold, rppaScoreThreshold, theOncoPrintSpecification );
                if( null == parser){
                    System.err.println( "null valueParser: cannot find: " + geneWithScore.getGene() );
                    break;
                }
                if (parser.isCnaAmplified()) {
                    singleGeneResult += "AMP;";
                }
                if (parser.isCnaHomozygouslyDeleted()) {
                    singleGeneResult += "HOMDEL;";
                }
                if (parser.isCnaGained()) {
                    singleGeneResult += "GAIN;";
                }
                if (parser.isCnaHemizygouslyDeleted()) {
                    singleGeneResult += "HETLOSS;";
                }
                if (parser.isMutated()) {
                    singleGeneResult += "MUT;";
                }
                if (parser.isMRNAWayUp()) {
                    singleGeneResult += "UP;";
                }
                if (parser.isMRNAWayDown()) {
                    singleGeneResult += "DOWN;";
                }
                if (parser.isRPPAWayUp()) {
                    singleGeneResult += "RPPA-UP;";
                }
                if (parser.isRPPAWayDown()) {
                    singleGeneResult += "RPPA-DOWN;";
                }
                _alterationResult.put(geneWithScore.getGene(), singleGeneResult);
            }
            alterationResults.put(patientId, _alterationResult);
        }
    %>
        var alterationResults = jQuery.parseJSON('<%=alterationResults%>');;
        return alterationResults;
    }

</script>

