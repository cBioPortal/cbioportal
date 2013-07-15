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
    //String[] gene_list = ((String)request.getParameter("gene_list")).split("\\s+");

    //Interprete Onco Query Genelist for plots view
    String[] plotsGeneList = new String[geneWithScoreList.size()];
    for (int i = 0; i < geneWithScoreList.size(); i++)
    {
        GeneWithScore tmpSingleGene = geneWithScoreList.get(i);
        String singleGene = tmpSingleGene.getGene();
        plotsGeneList[i] = singleGene;
    }
    String[] gene_list = plotsGeneList;
%>

<!-- Global Variables -->
<script>
    var cancer_study_id = "<%out.print(cancer_study_id);%>",
            case_set_id = "<%out.print(case_set_id);%>";
        case_ids_key = "";
    if (case_set_id === "-1") {
        case_ids_key = "<%out.print(caseIdsKey);%>";
    }
</script>
<script type="text/javascript" src="js/plots_tab.js"></script>

<div class="section" id="plots">
    <table>
        <tr>
            <td>
                <table>
                    <tr>
                        <td style="border:2px solid #BDBDBD;padding:10px;height:300px;">
                            <h4 style="padding-top:10px;">Plot Parameters</h4>
                            <br>
                            <b>Gene</b><br>
                            <%
                                if (gene_list.length == 1) {
                                    out.print("&nbsp;" + gene_list[0]);
                                }
                            %>
                            <%
                                if (gene_list.length == 1){
                                    out.println("<select id='genes' style='display:none;'>");
                                    out.println("<option value='" + gene_list[0].toUpperCase() + "'>" + gene_list[0].toUpperCase() + "</option>");
                                } else {
                                    out.println("<select id='genes' onchange=\"viewController.updateView();\">");
                                    for (int i = 0; i < gene_list.length; i++){
                                        out.println("<option value='" + gene_list[i].toUpperCase() + "'>" + gene_list[i].toUpperCase() + "</option>");
                                    }
                                }
                            %>
                            </select>
                            <br><br>
                            <b>Plot Type</b><br>
                            <select id='plot_type'  onchange="viewController.updateView();"></select>
                            <br><br>
                            <b>Data Type</b><br>
                            <div id='mrna_dropdown' style='padding:5px;'>
                                - mRNA - <br>
                                <select id='data_type_mrna' onchange="viewController.updatePlots();"></select>
                            </div>
                            <div id='copy_no_dropdown'style='padding:5px;'>
                                - Copy Number - <br>
                                <select id='data_type_copy_no' onchange="viewController.updatePlots();"></select>
                            </div>
                            <div id='dna_methylation_dropdown'style='padding:5px;'>
                                - DNA Methylation - <br>
                                <select id='data_type_dna_methylation' onchange="viewController.updatePlots();"></select>
                            </div>
                            <div id='rppa_dropdown'style='padding:5px;'>
                                - RPPA Protein Level - <br>
                                <select id='data_type_rppa' onchange="viewController.updatePlots();"></select>
                            </div>
                            <!--a href="#"><img src='images/next_button.gif' style="padding-top:30px;"></a-->
                        </td>
                    </tr>
                    <tr style="height:320px;"></tr>
                </table>
            </td>
            <td>
                <div id='plots_tab'>
                    <br><b>
                    <div id='div-loading-image'>
                        <img style='padding:200px;' src='images/ajax-loader.gif'>
                    </div>
                    <div id='img_center'>
                        <div id="img_center_text" style="display:inline-block; padding-left:100px;"></div></b>
                        <form style="display:inline-block" action='svgtopdf.do' method='post' onsubmit="this.elements['svgelement'].value=loadSVG();">
                            <input type='hidden' name='svgelement'>
                            <input type='hidden' name='filetype' value='pdf'>
                            <input type='hidden' name='filename' value='plots.pdf'>
                            <input type='submit' value='PDF'>
                        </form>
                        <form style="display:inline-block" action='svgtopdf.do' method='post' onsubmit="this.elements['svgelement'].value=loadSVG();">
                            <input type='hidden' name='svgelement'>
                            <input type='hidden' name='filetype' value='svg'>
                            <input type='hidden' name='filename' value='plots.svg'>
                            <input type='submit' value='SVG'>
                        </form>
                    </div>
                    <div id="plots_box"></div>
                </div>
            </td>
        </tr>
    </table>
</div>

<script>
    window.onload = viewController.initView();

    // Takes whatever is in the element #plots_box
    // and returns XML serialized *string*
    function loadSVG() {
        var mySVG = document.getElementById("plots_box");
        var svgDoc = mySVG.getElementsByTagName("svg");
        var xmlSerializer = new XMLSerializer();
        var xmlString = xmlSerializer.serializeToString(svgDoc[0]);
        xmlString = xmlString.replace(/<\/line><text y="9" x="0" dy=".71em"/g, "</line><text y=\"19\" x=\"0\" dy=\".71em\"");
        xmlString = xmlString.replace(/<\/line><text x="-9" y="0" dy=".32em"/g, "</line><text x=\"-9\" y=\"3\" dy=\".32em\"");
        return xmlString;
    }

</script>

<%!
    public int countProfiles (ArrayList<GeneticProfile> profileList, GeneticAlterationType type) {
        int counter = 0;
        for (int i = 0; i < profileList.size(); i++) {
            GeneticProfile profile = profileList.get(i);
            if (profile.getGeneticAlterationType() == type) {
                counter++;
            }
        }
        return counter;
    }
%>
