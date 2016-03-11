<script type="text/javascript" src="js/src/over-representation-analysis/main.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/data.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/view.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/plots.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/boilerplate.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/components/ScatterPlots.js?<%=GlobalProperties.getAppVersion()%>"></script>
<%--<script type="text/javascript" src="js/src/over-representation-analysis/volcano_plot_view.js?<%=GlobalProperties.getAppVersion()%>"></script>--%>

<script type="text/javascript" src="js/src/over-representation-analysis/stacked_histogram.js?<%=GlobalProperties.getAppVersion()%>"></script>
<link href="css/stacked_histogram.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<script type="text/javascript" src="js/lib/plotly.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/components/ScatterPlotly.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/components/MiniOnco.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/volcano_plot_view.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div class="section" id="or_analysis">
    <div id="or-analysis-info-box" style="padding: 10px;margin-top: -20px;"></div>
    <div id="or-analysis-tabs" class="or-analysis-tabs" style="margin-top:5px;">
        <ul id='or-analysis-tabs-list'></ul>
        <div id='or-analysis-tabs-content'></div>
    </div>
</div>

<style>

    @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";

    #or_analysis .or-analysis-tabs-ref {
        font-size: 10px !important;
    }
    #or_analysis table.dataTable thead th div.DataTables_sort_wrapper {
        font-size: 100%;
        position: relative;
        padding-right: 20px;
    }
    #or_analysis table.dataTable thead th div.DataTables_sort_wrapper span {
        position: absolute;
        top: 50%;
        margin-top: -8px;
        right: 0;
    }
    #or_analysis .label-or-analysis-significant {
        background-color: #58ACFA;
    }
    #or_analysis button:disabled {
        color: grey;
    }
    #or_analysis .help-img-icon {
        width: 13px;
        height: 13px;
    }
    #or_analysis .dataTables tbody tr {
        min-height: 28px;
    }
    #or_analysis td.rppa-details {
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

            var or_tab_init = false;
            $(window).trigger("resize");
            if ($("#or_analysis").is(":visible")) {
                or_tab.init(caseListObj);
                or_tab_init = true;
                $(window).trigger("resize");
            } else {
                $(window).trigger("resize");
            }
            $("#tabs").bind("tabsactivate", function(event, ui) {
                $(window).trigger("resize");
                if (ui.newTab.text().trim().toLowerCase().indexOf("enrichments") !== -1) {
                    $(window).trigger("resize");
                    if (or_tab_init === false) {
                        or_tab.init(caseListObj);
                        or_tab_init = true;
                        $(window).trigger("resize");
                    } else {
                        $(window).trigger("resize");
                    }
                }
            });

            //bind event listener to gene set selector
            $("#or_analysis_tab_gene_set_select").change(function() {
                if ($("#or_analysis_tab_gene_set_select").val() === "cancer_genes") {
                    $("#" + orAnalysis.ids.gene_set_warning).empty();
                    or_tab.update();
                } else if ($("#or_analysis_tab_gene_set_select").val() === "all_genes") {
                    $("#" + orAnalysis.ids.gene_set_warning).empty();
                    $("#" + orAnalysis.ids.gene_set_warning).append("Calculating and rendering...(this may a few seconds)");
                    or_tab.update();
                }
            });
        });
    });
</script>
