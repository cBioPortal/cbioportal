<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticAlterationType" %>

<script type="text/javascript" src="js/src/plots-tab/plots_tab_model.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/plots-tab/plots_tab.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/plots-tab/plots_two_genes.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/plots-tab/plots_custom.js?<%=GlobalProperties.getAppVersion()%>"></script>

<style>
    #plots .plots {
        height: 610px;
        border: 1px solid #aaaaaa;
        border-radius: 4px;
    }
    #plots .plots.plots-menus {
        width: 320px;
        height: 685px;
    }
    #plots .plots.plots-view {
        padding: 40px;
        width: 720px;
    }
    #plots .plots-tabs-ref {
        font-size: 11px !important;
    }
    #plots h4 {
        padding-top: 15px;
        padding-bottom: 15px;
    }
    #plots h5 {
        padding-top: 10px;
        padding-bottom: 10px;
        font-weight: bold;
    }
    #plots .plots-firefox {
        font-size: 10px;
    }
    #plots .plots-select {
        width: 250px;
    }
    #plots .ui-tabs .ui-state-disabled {
        display: none; /* disabled tabs don't show up */
    }
</style>


<div class="section" id="plots" class="plots">
    <table>
        <tr>
            <td>
                <div id="plots-menus" class="plots plots-menus">
                    <ul>
                        <li><a href="#plots_one_gene" title="Single Gene Query" class="plots-tabs-ref"><span>One Gene</span></a></li>
                        <li><a href="#plots_two_genes" title="Cross Gene Query" class="plots-tabs-ref"><span>Two Genes</span></a></li>
                        <li><a href="#plots_custom" title="Advanced Cross Gene Query" class="plots-tabs-ref"><span>Custom</span></a></li>
                    </ul>
                    <div id="plots_one_gene">
                        <h4>Plot Parameters</h4>
                        <h5>Gene</h5>
                        <select id='gene' onchange='PlotsMenu.updateMenu();PlotsView.init();'></select>
                        <div id='menu_err_msg'></div>
                        <div id='one_gene_type_specification'>
                            <h5>Plot Type</h5>
                            <select id='plots_type' onchange="PlotsMenu.updateDataType();PlotsView.init();"></select>
                            <h5>Data Type</h5>
                            <div id='one_gene_platform_select_div'></div>
                        </div>
                        <div id="inner-search-box-one-gene">
                            <h5>Search case(s)</h5>
                            <input type="text" id="search_plots_one_gene" placeholder="Case ID..." onkeyup="Plots.searchPlots('one_gene');">
                        </div>
                    </div>
                    <div id="plots_two_genes">
                        <h4>Plot Parameters</h4>
                        <h5>Genes</h5>
                        x Axis<select id='geneX' onchange="PlotsTwoGenesMenu.updateMenu();PlotsTwoGenesView.init();"></select>
                        <br>
                        y Axis<select id='geneY' onchange="PlotsTwoGenesMenu.updateMenu();PlotsTwoGenesView.init();"></select>
                        <h5>Plot Type</h5>
                        <select id='two_genes_plots_type' onchange="PlotsTwoGenesMenu.updateDataType();PlotsTwoGenesView.init();"></select>
                        <h5>Platform</h5>
                        <div id='two_genes_platform_select_div'></div>
                        <div id='two_genes_apply_log_scale_div_x'></div>
                        <div id='two_genes_apply_log_scale_div_y'></div>
                        <br>
                        <div id='two_genes_view_options'>
                            <h5>Options</h5>
                            <div id='two_genes_show_mutation_div'>
                                <input type="checkbox" id="show_mutation" checked onchange='PlotsTwoGenesView.updateMutationDisplay();'/>
                                show mutation data
                            </div>
                        </div>
                        <div id="inner-search-box-two-genes">
                            <h5>Search case(s)</h5>
                            <input type="text" id="search_plots_two_genes" placeholder="Case ID..." onkeyup="Plots.searchPlots('two_genes');">
                        </div>
                    </div>
                    <div id="plots_custom">
                        <h4>Plot Parameters</h4>
                        <h5>x Axis</h5>
                        Gene<br>
                        <select id='custom_geneX' onchange="PlotsCustomMenu.updateX();PlotsCustomView.init();"></select><br>
                        Plot Type<br>
                        <select id='custom_plots_type_x' onchange='PlotsCustomMenu.updateX();PlotsCustomView.init();'></select><br>
                        Platform<br>
                        <div id='custom_platform_select_div_x'></div>
                        <div id='custom_genes_apply_log_scale_div_x'></div>
                        <br>
                        <h5>y Axis</h5>
                        Gene<br>
                        <select id='custom_geneY' onchange="PlotsCustomMenu.updateY();PlotsCustomView.init();"></select><br>
                        Plot Type<br>
                        <select id='custom_plots_type_y' onchange='PlotsCustomMenu.updateY();PlotsCustomView.init();'></select><br>
                        Platform<br>
                        <div id='custom_platform_select_div_y'></div>
                        <div id='custom_genes_apply_log_scale_div_y'></div>
                        <br>
                        <div id='custom_genes_view_options'>
                            <h5>Options</h5>
                            <div id='custom_genes_show_mutation_div'>
                                <input type="checkbox" id="show_mutation_custom_view" checked onchange='PlotsCustomView.updateMutationDisplay();'/>
                                show mutation data
                            </div>
                        </div>
                        <div id="inner-search-box-custom">
                            <h5>Search case(s)</h5>
                            <input type="text" id="search_plots_custom" placeholder="Case ID..." onkeyup="Plots.searchPlots('custom');">
                        </div>
                    </div>
                </div>
            </td>
            <td>
                <div id="plots-view" class="plots plots-view">
                    <div id='loading-image'>
                        <img style='padding:200px;' src='images/ajax-loader.gif'>
                    </div>
                    <b><div id='view_title' style="display:inline-block;padding-left:100px;"></div></b>
                    <div id="plots_box"></div>
                </div>
            </td>
        </tr>
    </table>
</div>

<script>

    if (gene_list.length !== 1) {
        $("#plots-menus").tabs();
    } else {
        $("#plots-menus").tabs();
        $("#plots-menus").tabs("disable", 1);
    }
    
    $(document).ready( function() {
        var plots_tab_init = false;
        $("#tabs").bind("tabsactivate", function(event, ui) {
            if (ui.newTab.text().trim().toLowerCase() === "plots") {
                if (plots_tab_init === false) {
                    Plots.init();
                    plots_tab_init = true;
                }
            }
        });
    });


</script>

<script>
    //Patch for the sub tab css style and qtip bug. (Overwrite, stay bottom)
    $(".plots-tabs-ref").tipTip(
            {defaultPosition: "top", delay:"200", edgeOffset: 10, maxWidth: 200});
    //Patch for fixing the font size in firefox
    if (cbio.util.browser.mozilla) {
        var element = document.getElementById("plots-menus");
        element.className += " " + "plots-firefox";
    }

</script>