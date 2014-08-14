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
    <div id='mutex-wrapper' style='width: 1000px; margin-top: 20px; margin-left: 20px;'>
        <div id="mutex-info-div">
            <p>The query contains <span id='num_of_mutex' class='stat_num'></span> gene pair<span id='plu_s_mutex'></span> with mutually exclusive alterations<span id='stat_sig_mutex'> (<span id='num_of_sig_mutex' class='stat_num'></span> significant)</span>,
            and <span id='num_of_co_oc' class='stat_num'></span> gene pair<span id='plu_s_co_oc'></span> with co-occurrent alterations<span id='stat_sig_co_oc'> (<span id='num_of_sig_co_oc' class='stat_num'></span> significant)</span>.
        <div id='mutex-loading-image'>
            <img style='padding:200px;' src='images/ajax-loader.gif'>
        </div>
        <div id="mutex-table-div" style='margin-top:10px;'></div>
    </div>
</div>


<script>
    $(document).ready( function() {
        PortalDataCollManager.subscribeOncoprint(function() {
            MutexData.setOncoprintData(PortalDataColl.getOncoprintData()); 
        });
        $("#tabs").bind("tabsactivate", function(event, ui) {
            if (!MutexView.isTableInstanceExisted()) {
                MutexData.init();
                if (ui.newTab.text().trim().toLowerCase() === "mutual exclusivity") {
                    MutexView.resize();
                }
            }
        });
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
    #mutex .classMutexTable { 
        text-align: right; 
    }
    #mutex .stat_num {
        font-size: 105%;
        color: #3366CC;
        font-weight:bold;
    }
</style>