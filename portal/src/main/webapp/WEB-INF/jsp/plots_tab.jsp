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

<!-- Global Variables -->
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

<!-- Data -->
<script type="text/javascript" src="js/plots-view/plots_tab_model.js"></script>
<!-- Tab1 : One Gene -->
<script type="text/javascript" src="js/plots-view/plots_tab.js"></script>
<!-- Tab2 : Two Genes -->
<script type="text/javascript" src="js/plots-view/plots_two_genes.js"></script>
<!-- Tab3 : Custom View -->
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
                <!-- Side Menu Column -->
                <td>
                    <table>
                        <tr>
                            <td style="border:2px solid #BDBDBD;padding:10px;height:300px;width:300px;">

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
                <!-- Plots View-->
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
                    <!-- Side Menu Column -->
                    <td>
                        <table>
                            <tr>
                                <td style="border:2px solid #BDBDBD;padding:10px;height:300px;width:300px;top:-100px;">
                                    <h4>Plot Parameters</h4>

                                    <h5>Genes</h5>
                                    x Axis
                                    <select id='gene1'></select><br>
                                    y Axis
                                    <select id='gene2'></select>

                                    <h5>Plots Type</h5>
                                    <select id='two_genes_plots_type'>
                                        <option value='mrna'>mRNA Expression</option>
                                        <option value='copy_no'>Copy Number Alteration</option>
                                        <option value='methylation'>DNA Methylation</option>
                                        <option value='rppa'>RPPA Protein Level</option>
                                    </select>

                                    <h5>Platform</h5>
                                    <div id='mrna_dropdown_two_genes' style='padding:5px;'>
                                        - mRNA - <br>
                                        <select id='data_type_mrna_two_genes'></select>
                                    </div>
                                    <div id='copy_no_dropdown_two_genes'style='padding:5px;'>
                                        - Copy Number - <br>
                                        <select id='data_type_copy_no_two_genes'></select>
                                    </div>
                                    <div id='dna_methylation_dropdown_two_genes'style='padding:5px;'>
                                        - DNA Methylation - <br>
                                        <select id='data_type_dna_methylation_two_genes'></select>
                                    </div>
                                    <div id='rppa_dropdown'style='padding:5px;'>
                                        - RPPA Protein Level - <br>
                                        <select id='data_type_rppa_two_genes'></select>
                                    </div>

                                </td>
                            </tr>
                            <!-- Place Holder at the buttom for the side menu-->
                            <tr style="height:320px;"></tr>
                        </table>
                    </td>
                    <!-- Plots View Two Genes-->
                    <td>
                        <div id="plots_box_two_genes"></div>
                    </td>
                </tr>
            </table>
        </div>
        <div id="plots_custom">
            <table>
                <tr>
                    <!-- Side Menu Column -->
                    <td>
                        <table>
                            <tr>
                                <td style="border:2px solid #BDBDBD;padding:10px;height:300px;width:300px;">

                                    <h4>Plot Prarmeters</h4>

                                    <h5>x Axis</h5>

                                    Genes
                                    <select id='custom_gene1'></select>

                                    <br>Plots Type
                                    <br>
                                    <select id='custom_plots_type_x'>
                                        <option value='mrna'>mRNA Expression</option>
                                        <option value='copy_no'>Copy Number Alteration</option>
                                        <option value='methylation'>DNA Methylation</option>
                                        <option value='rppa'>RPPA Protein Level</option>
                                    </select>

                                    <br>Platform
                                    <div id='mrna_dropdown_custom_x' style='padding:5px;'>
                                        - mRNA - <br>
                                        <select id='data_type_mrna_custom_x'></select>
                                    </div>
                                    <div id='copy_no_dropdown_custom_x'style='padding:5px;'>
                                        - Copy Number - <br>
                                        <select id='data_type_copy_no_custom_x'></select>
                                    </div>
                                    <div id='dna_methylation_dropdown_custom_x'style='padding:5px;'>
                                        - DNA Methylation - <br>
                                        <select id='data_type_dna_methylation_custom_x'></select>
                                    </div>
                                    <div id='rppa_dropdown'style='padding:5px;'>
                                        - RPPA Protein Level - <br>
                                        <select id='data_type_rppa_custom_x'></select>
                                    </div>

                                    <h5>y Axis</h5>

                                    Genes
                                    <select id='custom_gene2'></select>

                                    <br>Plots Type
                                    <br>
                                    <select id='custom_plots_type_y'>
                                        <option value='mrna'>mRNA Expression</option>
                                        <option value='copy_no'>Copy Number Alteration</option>
                                        <option value='methylation'>DNA Methylation</option>
                                        <option value='rppa'>RPPA Protein Level</option>
                                    </select>

                                    <br>Data Type
                                    <select id='custom_data_type_x_mrna'></select>

                                    <br>Platform
                                    <div id='mrna_dropdown_custom_y' style='padding:5px;'>
                                        - mRNA - <br>
                                        <select id='data_type_mrna_custom_y'></select>
                                    </div>
                                    <div id='copy_no_dropdown_custom_y'style='padding:5px;'>
                                        - Copy Number - <br>
                                        <select id='data_type_copy_no_custom_y'></select>
                                    </div>
                                    <div id='dna_methylation_dropdown_custom_y'style='padding:5px;'>
                                        - DNA Methylation - <br>
                                        <select id='data_type_dna_methylation_custom_y'></select>
                                    </div>
                                    <div id='rppa_dropdown'style='padding:5px;'>
                                        - RPPA Protein Level - <br>
                                        <select id='data_type_rppa_custom_y'></select>
                                    </div>

                                </td>
                            </tr>
                        </table>
                    </td>
                    <!-- Plots View Custom-->
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
    window.onload = PlotsData.init();
    window.onload = PlotsMenu.init();
    window.onload = PlotsMenu.update();
    window.onload = PlotsTwoGenesMenu.init();
    window.onload = PlotsCustomMenu.init();

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

    //Patch for the sub tab css style and qtip bug. (Overwrite)
    $(".plots-tabs-ref").tipTip(
            {defaultPosition: "top", delay:"100", edgeOffset: 10, maxWidth: 200});


</script>




