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
    String[] gene_list = ((String)request.getParameter("gene_list")).split("\\s+");
%>

<!-- Global Variables -->
<script>
    var cancer_study_id = "<%out.print(cancer_study_id);%>",
            case_set_id = "<%out.print(case_set_id);%>";
</script>
<script type="text/javascript" src="js/d3.v3.js"></script>
<script type="text/javascript" src="js/plots_tab.js"></script>

<div class="section" id="plots">
    <table >
        <tr>
            <td>
                <table>
                    <tr><td style="border:2px solid #BDBDBD;height:300px;padding:20px;">
                        <h4>Plot Parameters</h4>
                        <br>
                        <b>Gene</b><br>
                        <select id='genes'>
                            <%
                                for (int i=0; i<gene_list.length; i++){
                                    out.println("<option value='" + gene_list[i] + "'>" + gene_list[i] + "</option>");
                                }
                            %>
                        </select>
                        <br><br>
                        <b>Plot Type</b><br>
                        <select id='plot_type'></select>
                        <br><br>
                        <b>Data Type</b><br>
                        <br>
                        mRNA
                        <br><select id='data_type_mrna'></select><br>
                        Copy Number
                        <br><select id='data_type_copy_no'></select>
                        <br><br><br>
                        <a href="#" onclick="updateScatterPlots()"><img src='images/next_button.gif'></a>
                    </td>
                    <tr><td style='height:250px;'></td>
                </table>
            </td>
            <td>
                <br><b><div id="img_center" style="display:inline-block; padding-left:100px;"></div></b>
                <form style="display:inline-block" action='svgtopdf.do' method='post' onsubmit="this.elements['svgelement'].value=loadSVG();">
                    <input type='hidden' name='svgelement'>
                    <input type='hidden' name='filetype' value='pdf'>
                    <input type='submit' value='PDF'>
                </form>
                <form style="display:inline-block" action='svgtopdf.do' method='post' onsubmit="this.elements['svgelement'].value=loadSVG();">
                    <input type='hidden' name='svgelement'>
                    <input type='hidden' name='filetype' value='svg'>
                    <input type='submit' value='SVG'>
                </form>
                <div id="plots_tab"></div>
            </td>
        </tr>
    </table>
</div>

<script>window.onload = initView(); </script>

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
