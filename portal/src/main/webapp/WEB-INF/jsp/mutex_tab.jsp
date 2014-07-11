<%@ page import="org.mskcc.cbio.portal.stats.OddsRatio" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.apache.commons.lang.math.DoubleRange" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>

<script type="text/javascript" src="js/src/mutex/dataProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutex/view.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div class="section" id="mutex" class="mutex">
    <div id='mutex-loading-image'>
        <img style='padding:200px;' src='images/ajax-loader.gif'>
    </div>
    <div id="mutex-table-div"></div>
    <div id="mutex-info-div" style='margin-top: 10px;'>
        <table class='mutex-info-table'>
            <tr>
                <td> ** </td>
                <td>
                    p-values are derived via Fisher's Exact Test. (p-values are not adjust for FDR.)
                </td>
            </tr>
            <tr>
                <td> ** </td>
                <td>
                    Strong tendency towards mutual exclusivity (0 < Odds Ratio < 0.1)<br>
                    Some tendency towards mutual exclusivity (0.1 < Odds Ratio < 0.5)<br>
                    No association (0.5 < Odds Ratio < 2)<br>
                    Tendency toward co-occurrence (2 < Odds Ratio < 10)<br>
                    Strong tendendency towards co-occurrence (Odds Ratio > 10)<br>
                    No events recorded for one or both genes
                </td>
            </tr>
        </table>
    </div>
</div>


<script>
    $(document).ready( function() {
        MutexData.init();
    });    
</script>

<style type="text/css">
    .mutex-info-table td {
        vertical-align: top;
    }
</style>