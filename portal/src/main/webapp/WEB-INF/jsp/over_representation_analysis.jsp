<script type="text/javascript" src="js/src/over-representation-analysis/main.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/data.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/view.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/boilerplate.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div class="section" id="or_analysis">
    <div id="or-analysis-info-box" style="padding: 10px;">
        Synthetic genetic arrays have been very effective at measuring genetic interactions in yeast in a high-throughput manner and recently have been expanded to measure quantitative changes in interaction, termed 'differential interactions', across multiple conditions. Here, we present a strategy that leverages statistical information from the experimental design to produce a novel, quantitative differential interaction score, which performs favorably compared to previous differential scores. We also discuss the added utility of differential genetic-similarity in differential network analysis.
    </div>
    <div id="or-analysis-tabs" class="or-analysis-tabs">
        <ul id='or-analysis-tabs-list'></ul>
        <div id='or-analysis-tabs-content'></div>
    </div>
</div>

<style>
    #or_analysis .or-analysis-tabs-ref {
        font-size: 11px !important;
    }
</style>

<script>
    $(document).ready( function() {

        var caseListObj = {};
        $.each(window.PortalGlobals.getAlteredSampleIdList().split(" "), function(_index, _sampleId) {
            caseListObj[_sampleId] = "altered";
        });
        $.each(window.PortalGlobals.getUnalteredSampleIdList().split(" "), function(_index, _sampleId) {
            caseListObj[_sampleId] = "unaltered";
        });
        
        var or_tab_init = false;
        if ($("#or_analysis").is(":visible")) {
            or_tab.init(caseListObj);
            or_tab_init = true;
        } else {
            $(window).trigger("resize");
        }
        $("#tabs").bind("tabsactivate", function(event, ui) {
            if (ui.newTab.text().trim().toLowerCase() === "over-representation analysis") {
                if (or_tab_init === false) {
                    or_tab.init(caseListObj);
                    or_tab_init = true;
                    $(window).trigger("resize");
                } else {
                    $(window).trigger("resize");
                }
            }
        });
    });
</script>
