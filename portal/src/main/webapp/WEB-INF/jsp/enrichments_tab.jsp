<script type="text/javascript" src="js/src/enrichments/main.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/data.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/view.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/plots.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/boilerplate.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/stacked_histogram.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/plotly.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/ScatterPlotly.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/MiniOnco.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/enrichments/volcano_plot_view.js?<%=GlobalProperties.getAppVersion()%>"></script>
<link href="css/stacked_histogram.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />

<div class="section" id="enrichementTabDiv">
    <div id="enrichments-tab-info-box" style="padding: 10px;margin-top: -20px;"></div>
    <div id="enrichments-tab-tabs" class="enrichments-tab-tabs" style="margin-top:5px;">
        <ul id='enrichments-tab-tabs-list'></ul>
        <div id='enrichments-tab-tabs-content'></div>
    </div>
</div>

<style>

    @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";

    #enrichementTabDiv .enrichments-tabs-ref {
        font-size: 10px !important;
    }
    #enrichementTabDiv table.dataTable thead th div.DataTables_sort_wrapper {
        font-size: 100%;
        position: relative;
        padding-right: 20px;
    }
    #enrichementTabDiv table.dataTable thead th div.DataTables_sort_wrapper span {
        position: absolute;
        top: 50%;
        margin-top: -8px;
        right: 0;
    }
    #enrichementTabDiv .label-or-analysis-significant {
        background-color: #58ACFA;
    }
    #enrichementTabDiv button:disabled {
        color: grey;
    }
    #enrichementTabDiv .help-img-icon {
        width: 13px;
        height: 13px;
    }
    #enrichementTabDiv .dataTables tbody tr {
        min-height: 28px;
    }
    #enrichementTabDiv td.rppa-details {
        background-color : white;
        width: 600px;
    }

    /* elements for table gene highlighting */
    span.selectHighlight{
        padding: 1px;
        border: 2px solid transparent;
        width: 150%;
    }
    span.selectHighlight:hover{
        padding: 1px;
        border: 2px solid #1974b8;
        cursor: pointer;
    }
    span.geneSelected{
        font-weight: bold;
    }
    div.geneCheckboxDiv{
        width: 100px;
    }
    div.loaderIcon{
        width: 100%;
        display:none;
        position: absolute;
        left: 65%;
        top: 30%;
    }

    div.loaderIconLoading{
        display:block;
    }

    table.tableLoading{
        opacity: 0.5;
    }
</style>

<script>
    $(document).ready( function() {

        $.when(window.QuerySession.getAlteredSamples(), window.QuerySession.getUnalteredSamples()).then(function(altered_samples, unaltered_samples) {
            var caseListObj = {};
            $.each(altered_samples, function(_index, _sampleId) {
                caseListObj[_sampleId] = "altered";
            });
            $.each(unaltered_samples, function(_index, _sampleId) {
                caseListObj[_sampleId] = "unaltered";
            });

            var enrichments_tab_init = false;
            $(window).trigger("resize");
            if ($("#" + enrichmentsTabSettings.ids.main_div).is(":visible")) {
                enrichmentsTab.init(caseListObj);
                enrichments_tab_init = true;
                $(window).trigger("resize");
            } else {
                $(window).trigger("resize");
            }
            $("#tabs").bind("tabsactivate", function(event, ui) {
                $(window).trigger("resize");
                if (ui.newTab.text().trim().toLowerCase().indexOf("enrichments") !== -1) {
                    $(window).trigger("resize");
                    if (enrichments_tab_init === false) {
                        enrichmentsTab.init(caseListObj);
                        enrichments_tab_init = true;
                        $(window).trigger("resize");
                    } else {
                        $(window).trigger("resize");
                    }
                }
            });

        });
    });
</script>
