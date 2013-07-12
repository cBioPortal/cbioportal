<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.servlet.GeneratePlots" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticAlterationType" %>

<%
    String cancer_study_id = (String)request.getParameter("cancer_study_id");
    String case_set_id = (String)request.getParameter("case_set_id");
    String genetic_profile_id = (String)request.getParameter("genetic_profile_id");
    //Translate Onco Query Language
    ArrayList<String> listOfGenes = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
    String tmpGeneStr = "";
    for(String gene: listOfGenes) {
        tmpGeneStr += gene + " ";
    }
    tmpGeneStr = tmpGeneStr.trim();

%>

<script>
    var cancer_study_id = "<%out.print(cancer_study_id);%>",
            case_set_id = "<%out.print(case_set_id);%>";
    case_ids_key = "";
    if (case_set_id === "-1") {
        case_ids_key = "<%out.print(caseIdsKey);%>";
    }
    var genetic_profile_id = "<%out.print(genetic_profile_id);%>";
    var gene_list_str = "<%out.print(tmpGeneStr);%>";
    var gene_list = gene_list_str.split(/\s+/);
</script>

<script type="text/javascript" src="js/plots-view/plots_tab_model.js"></script>
<script type="text/javascript" src="js/plots-view/plots_tab.js"></script>
<script type="text/javascript" src="js/plots-view/plots_two_genes.js"></script>
<script type="text/javascript" src="js/plots-view/plots_custom.js"></script>

<style>
    #plots .plots {
        height: 610px;
    }
    #plots .plots.plots-menus {
        width: 320px;
        height: 685px;
    }
    #plots .plots.plots-view {
        border: 1px solid #aaaaaa;
        border-radius: 4px;
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
</style>

<script>
</script>

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
                        <select id='gene' onchange='PlotsView.init()'></select>
                        <h5>Plot Type</h5>
                        <select id='plots_type' onchange="PlotsMenu.update();PlotsView.init();"></select>
                        <h5>Data Type</h5>
                        <div id='one_gene_platform_select_div'></div>
                    </div>
                    <div id="plots_two_genes">
                        <h4>Plot Parameters</h4>
                        <h5>Genes</h5>
                        x Axis<select id='geneX' onchange="PlotsTwoGenesView.init()"></select><br>
                        y Axis<select id='geneY' onchange="PlotsTwoGenesView.init()"></select>
                        <h5>Plot Type</h5>
                        <select id='two_genes_plots_type' onchange="PlotsTwoGenesMenu.update();PlotsTwoGenesView.init();"></select>
                        <h5>Platform</h5>
                        <div id='two_genes_platform_select_div'></div>
                        <br><label for="show_mutation">Show Mutation Data</label>
                        <input type="checkbox" name="show_mutation" id="show_mutation"
                               value="show_mutation" checked onchange='PlotsTwoGenesView.updateMutationDisplay();'/>
                    </div>
                    <div id="plots_custom">
                        <h4>Plot Parameters</h4>
                        <h5>x Axis</h5>
                        Gene<br>
                        <select id='custom_geneX' onchange="PlotsCustomView.init()"></select><br>
                        Plot Type<br>
                        <select id='custom_plots_type_x' onchange='PlotsCustomMenu.update();PlotsCustomView.init();'></select><br>
                        Platform<br>
                        <div id='custom_platform_select_div_x'></div>
                        <br>
                        <h5>y Axis</h5>
                        Gene<br>
                        <select id='custom_geneY' onchange="PlotsCustomView.init()"></select><br>
                        Plot Type<br>
                        <select id='custom_plots_type_y' onchange='PlotsCustomMenu.update();PlotsCustomView.init();'></select><br>
                        Platform<br>
                        <div id='custom_platform_select_div_y'></div>
                        <br><label for="show_mutation_custom_view">Show Mutation Data</label>
                        <input type="checkbox" name="show_mutation_custom_view" id="show_mutation_custom_view"
                               value="show_mutation" checked onchange='PlotsCustomView.updateMutationDisplay();'/>
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

    $("#plots-menus").tabs();
    window.onload = Plots.init();

    // Takes the content in the plots svg element
    // and returns XML serialized *string*
    function loadSVG() {
        var shiftValueOnX = 8;
        var shiftValueOnY = 3;
        var mySVG = d3.select("#plots_box");
        var xAxisGrp = mySVG.select(".plots-x-axis-class");
        var yAxisGrp = mySVG.select(".plots-y-axis-class");
        cbio.util.alterAxesAttrForPDFConverter(xAxisGrp, shiftValueOnX, yAxisGrp, shiftValueOnY, false);
        var docSVG = document.getElementById("plots_box");
        var svgDoc = docSVG.getElementsByTagName("svg");
        var xmlSerializer = new XMLSerializer();
        var xmlString = xmlSerializer.serializeToString(svgDoc[0]);
        cbio.util.alterAxesAttrForPDFConverter(xAxisGrp, shiftValueOnX, yAxisGrp, shiftValueOnY, true);
        return xmlString;
    }

</script>

<script>
    //Patch for the sub tab css style and qtip bug. (Overwrite, stay bottom)
    $(".plots-tabs-ref").tipTip(
            {defaultPosition: "top", delay:"200", edgeOffset: 10, maxWidth: 200});
</script>



