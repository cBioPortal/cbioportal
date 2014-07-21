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
    <div id='mutex-wrapper' style='width: 600px; margin-top: 20px; margin-left: 20px;'>
        <div id='mutex-header'>
            Co-occurrence and mutual exclusivity for all gene pairs:
            For each gene pair, this table lists the odds ratio<img src='images/help.png' id='odds-ratio-help'> and p-Values<img src='images/help.png' id='p-value-help'> for the observed co-occurrence in the queried data set.
        </div>
        <div id='mutex-loading-image'>
            <img style='padding:200px;' src='images/ajax-loader.gif'>
        </div>
        <div id="mutex-table-div" style='margin-top:10px;'></div>
    </div>
</div>


<script>
    $(document).ready( function() {
        MutexData.init();
    });
    $("#odds-ratio-help").qtip({
        content: { text:'Log odds ratio > 0 : Tendency towards co-occurrence <br>Log odds ratio < 0 : Tendency towards mutual exclusivity'},
        style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'left bottom',at:'top right',viewport: $(window)}
    })  
    $("#p-value-help").qtip({
        content: { text:'Drived from Fisher Exact Test'},
        style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'left bottom',at:'top right',viewport: $(window)}
    })     
</script>

<style type="text/css">
    #mutex-info-div {
        font-size: 10px;
        margin-top: 10px;
    }
    #mutex-table thead {
        font-size:70%;
    }
    #mutex table.dataTable thead th div.DataTables_sort_wrapper {
        position: relative;
        padding-right: 20px;
    }
    #mutex table.dataTable thead th div.DataTables_sort_wrapper span {
        position: absolute;
        top: 50%;
        margin-top: -8px;
        right: 0;
    }
</style>