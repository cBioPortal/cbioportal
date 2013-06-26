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
    .plots-tabs-ref{
        font-size:11px !important;
    }
    #plots h4{
        padding-top:10px;
    }
    #plots h5{
        padding-top:10px;
        padding-bottom:10px;
        font-weight:bold;
    }
</style>

<div class="section" id="plots">
    <div id="plots-tabs" class="plots-tabs">

        <!--Generating sub tabs-->
        <ul>
            <li><a href="#plots_one_gene" title="Single Gene Query" class="plots-tabs-ref"><span>One Gene</span></a></li>
            <li><a href="#plots_two_genes" title="Cross Gene Query" class="plots-tabs-ref"><span>Two Genes</span></a></li>
            <li><a href="#plots_custom" title="Advanced Multi-gene view" class="plots-tabs-ref"><span>Custom</span></a></li>
        </ul>

        <div id="plots_one_gene">
            <table>
            <tr>
                <td>
                    <table>
                        <tr>
                            <td style="border:2px solid #BDBDBD;padding:10px;height:300px;width:300px;margin-top:50px;">

                                <h4>Plot Parameters</h4>

                                <h5>Gene</h5>
                                <select id='genes' onchange='PlotsView.init()'></select>

                                <h5>Plots Type</h5>
                                <select id='plots_type' onchange="PlotsMenu.update();"></select>

                                <h5>Data Type</h5>
                                <div id='mrna_dropdown' style='padding:5px;'>
                                    - mRNA - <br>
                                    <select id='data_type_mrna' onchange="PlotsView.init()"></select>
                                </div>
                                <div id='copy_no_dropdown'style='padding:5px;'>
                                    - Copy Number - <br>
                                    <select id='data_type_copy_no' onchange="PlotsView.init()"></select>
                                </div>
                                <div id='dna_methylation_dropdown'style='padding:5px;'>
                                    - DNA Methylation - <br>
                                    <select id='data_type_dna_methylation' onchange="PlotsView.init()"></select>
                                </div>
                                <div id='rppa_dropdown'style='padding:5px;'>
                                    - RPPA Protein Level - <br>
                                    <select id='data_type_rppa' onchange="PlotsView.init()"></select>
                                </div>
                                <!-- Hidden -->
                                <div id='mutation_dropdown' style='padding:5px;display:none'>
                                    <select id='data_type_mutation'></select>
                                </div>

                            </td>
                        </tr>
                        <tr style="height:320px;"></tr>
                    </table>
                </td>
                <td>
                    <div id='loading-image'>
                        <img style='padding:200px;' src='images/ajax-loader.gif'>
                    </div>
                    <b><div id='view_title' style="display:inline-block;padding-left:100px;"></div></b>
                    <div id="plots_box"></div>
                </td>
            </tr>
        </table>
        </div>
        <div id="plots_two_genes">
            <table>
                <tr>
                    <td>
                        <table>
                            <tr>
                                <td style="border:2px solid #BDBDBD;padding:10px;height:280px;width:300px;margin-top:50px;">

                                    <h4>Plot Parameters</h4>

                                    <h5>Genes</h5>
                                    x Axis<select id='geneX' onchange="PlotsTwoGenesView.init()"></select><br>
                                    y Axis<select id='geneY' onchange="PlotsTwoGenesView.init()"></select>

                                    <h5>Plots Type</h5>
                                    <select id='two_genes_plots_type' onchange="PlotsTwoGenesMenu.update()"></select>

                                    <h5>Platform</h5>
                                    <div id='two_genes_platform_select_div'></div>

                                    <br><label for="show_mutation">Show Mutation Data</label>
                                    <input type="checkbox" name="show_mutation" id="show_mutation"
                                           value="show_mutation" checked onchange='PlotsTwoGenesView.updateMutationDisplay();'/>

                                </td>
                            </tr>
                            <tr style="height:320px;"></tr>
                        </table>
                    </td>
                    <td>
                        <b><div id='two_genes_view_title' style="display:inline-block;padding-left:100px;"></div></b>
                        <div id="plots_box_two_genes"></div>
                    </td>
                </tr>
            </table>
        </div>
        <div id="plots_custom">
            <table>
                <tr>
                    <td>
                        <table>
                            <tr>
                                <td style="border:2px solid #BDBDBD;padding:10px;height:360px;width:300px;">

                                    <h4>Plot Prarmeters</h4>

                                    <h5>x Axis</h5>
                                    Gene<br>
                                    <select id='custom_gene1'></select><br>
                                    Plots Type<br>
                                    <select id='custom_plots_type_x' onchange='PlotsCustomMenu.update()'></select><br>
                                    Platform<br>
                                    <div id='custom_platform_select_div_x'></div>

                                    <br>
                                    <h5>y Axis</h5>
                                    Gene<br>
                                    <select id='custom_gene2'></select><br>
                                    Plots Type<br>
                                    <select id='custom_plots_type_y' onchange='PlotsCustomMenu.update()'></select><br>
                                    Platform<br>
                                    <div id='custom_platform_select_div_y'></div>

                                </td>
                            </tr>
                            <tr style="height:250px;"></tr>
                        </table>
                    </td>
                    <td>
                        <div id="plots_box_custom"></div>
                    </td>
                </tr>
            </table>
        </div>

    </div>
</div>

<script>
    $("#plots").tabs();
    window.onload = Plots.init();

    // Takes whatever is in the element #plots_box
    // and returns XML serialized *string*
    function loadSVGforPDF() {
        var mySVG = document.getElementById("plots_box");
        var svgDoc = mySVG.getElementsByTagName("svg");
        var xmlSerializer = new XMLSerializer();
        var xmlString = xmlSerializer.serializeToString(svgDoc[0]);
        xmlString = xmlString.replace(/<text y="9" x="0" dy=".71em"/g, "<text y=\"19\" x=\"0\" dy=\".71em\"");
        xmlString = xmlString.replace(/<text x="-9" y="0" dy=".32em"/g, "<text x=\"-9\" y=\"3\" dy=\".32em\"");
        return xmlString;
    }
    function loadSVGforSVG() {
        var mySVG = document.getElementById("plots_box");
        var svgDoc = mySVG.getElementsByTagName("svg");
        var xmlSerializer = new XMLSerializer();
        var xmlString = xmlSerializer.serializeToString(svgDoc[0]);
        return xmlString;
    }

    // Takes whatever is in the element #plots_box
    // and returns XML serialized *string*
    function loadSVGforPDFTwoGenes() {
        var mySVG = document.getElementById("plots_box_two_genes");
        var svgDoc = mySVG.getElementsByTagName("svg");
        var xmlSerializer = new XMLSerializer();
        var xmlString = xmlSerializer.serializeToString(svgDoc[0]);
        xmlString = xmlString.replace(/<text y="9" x="0" dy=".71em"/g, "<text y=\"19\" x=\"0\" dy=\".71em\"");
        xmlString = xmlString.replace(/<text x="-9" y="0" dy=".32em"/g, "<text x=\"-9\" y=\"3\" dy=\".32em\"");
        return xmlString;
    }
    function loadSVGforSVGTwoGenes() {
        var mySVG = document.getElementById("plots_box_two_genes");
        var svgDoc = mySVG.getElementsByTagName("svg");
        var xmlSerializer = new XMLSerializer();
        var xmlString = xmlSerializer.serializeToString(svgDoc[0]);
        return xmlString;
    }

    //Patch for the sub tab css style and qtip bug. (Overwrite)
    $(".plots-tabs-ref").tipTip(
            {defaultPosition: "top", delay:"100", edgeOffset: 10, maxWidth: 200});


</script>




