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
    <div id='mutex-wrapper' style='width: 55%; margin-top: 20px; margin-left: 20px;'>
        <div id='mutex-loading-image'>
            <img style='padding:200px;' src='images/ajax-loader.gif'>
        </div>
        <div id='mutex-header'>
            This table lists the queried gene pairs with <a href='http://en.wikipedia.org/wiki/Odds_ratio'>Odds Ratio</a> and p-Values (derived from <a href="http://en.wikipedia.org/wiki/Fisher's_exact_test" target="_blank">Fisher's Exact Test</a>), <br>as references to measure mutual exclusive / co-occurrence.
        </div>
        <div id="mutex-table-div" style='margin-top:10px;'></div>
        <div id="mutex-info-div" style='margin-top:10px;'>
            ** Odds Ratio(log) > 0 -- Tendency towards co-occurrence <br>
            ** Odds Ratio(log) < 0 -- Tendency towards mutual exclusivity
        </div>
    </div>
</div>


<script>
    $(document).ready( function() {
        MutexData.init();
    });    
</script>

<style type="text/css">
    #mutex-info-div {
        font-size: 10px;
        margin-top: 10px;
    }
    #mutex-table thead {
        font-size:70%;
    }
</style>