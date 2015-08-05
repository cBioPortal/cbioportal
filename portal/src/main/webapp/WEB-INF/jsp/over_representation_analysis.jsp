<script type="text/javascript" src="js/src/over-representation-analysis/main.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/data.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/view.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/plots.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/boilerplate.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div class="section" id="or_analysis">
    <div id="or-analysis-info-box" style="padding: 10px;">
        <!--Gene Set
        <select id="or_analysis_tab_gene_set_select">
            <option value="cancer_genes">All Cancer Genes in cbio portal</option>
            <option value="all_genes">All Genes in cbio portal</option>
            <option value="custom_genes">Custom Gene Set</option>
        </select>
        <span id="or_analysis_tab_warning"></span>
        <br>
        <div id="or_analysis_tab_custom_gene_set_text_area"></div>
        <br>-->
    </div>
    <div id="or-analysis-tabs" class="or-analysis-tabs" style="margin-top:-40px;">
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

//        //bind event listener to gene set selector
//        $("#or_analysis_tab_gene_set_select").change(function() {
//            if ($("#or_analysis_tab_gene_set_select").val() === "all_genes") {
//                $("#or_analysis_tab_warning").append("<span style='color:red;'>Calculating and rendering may take up to 10 minutes...</span>");
//                $("#or_analysis_tab_custom_gene_set_text_area").empty();
//                or_tab.update();
//            } else if ($("#or_analysis_tab_gene_set_select").val() === "custom_genes") {
//                $("#or_analysis_tab_warning").empty();
//                $("#or_analysis_tab_custom_gene_set_text_area").append("<br><textarea class='form-control' rows='5' id='or_analysis_tab_custom_gene_set_text_area_id'></textarea>");
//                $("#or_analysis_tab_custom_gene_set_text_area").append("<button id='or_analysis_tab_custom_gene_set_submit_btn'>Submit Custom Gene Set</button>");
//            } else {
//                $("#or_analysis_tab_warning").empty();
//                $("#or_analysis_tab_custom_gene_set_text_area").empty();
//                or_tab.update();
//            }
//
//        });

    });
</script>
