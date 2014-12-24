<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticAlterationType" %>

<script type="text/javascript" src="js/src/plots-tab/plotsTab.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/sidebar.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/plotsbox.js"></script>
<script type="text/javascript" src="js/src/plots-tab/proxy/metaData.js"></script>
<script type="text/javascript" src="js/src/plots-tab/proxy/plotsData.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/map.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/plotsUtil.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/mutationInterpreter.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/stylesheet.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/profileSpec.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/clinSpec.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/scatterPlots.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/boxPlots.js"></script>

<style>
    #plots .plots {
        border: 1px solid #aaaaaa;
        border-radius: 4px;
        margin: 15px;
    }
    
    #plots-sidebar {
        width: 320px;
    }
    #plots-sidebar-x-div {
        width: inherit;
        height: 200px;
    }
    #plots-sidebar-y-div {
        width: inherit;
        height: 200px;
    }
    #plots-sidebar-util-div {
        width: inherit;
        height: 140px;
    }
    
    #plots-sidebar h3 {
        margin: 12px;
        margin-bottom: 20px;
        font-size: 14px;
    }
    #plots-sidebar h4 {
        margin: 15px;
        font-size: 12px;
        color: grey;
        background-color: white;
        margin-top: -6px;
        display: table;
        padding: 5px;
    }
    #plots-sidebar h5 {
        margin-left: 20px;
        padding-left: 5px;
        padding-right: 5px;
        display: inline-block;
        margin-bottom: 10px;
    }
    #plots-sidebar select {
        max-width: 180px;
    }
    #plots-box {
        width: 820px;
        height: 610px;
        float: right;
    }
</style>

<div class="section" id="plots">
    <table>
        <tr>
            <td>
                 <div id="plots-sidebar">
                    <h3>Plots Parameters</h3>
                    <div id="plots-sidebar-x-div" class="plots">
                        <h4>X Axis</h4>
                        <br><h5>Data Type</h5> 
                        <select id="plots-x-data-type">
                            <option value="genetic_profile">Genetic Profile</option>
                            <option value="clinical_attribute">Clinical Attribute</option>
                        </select>
                        <div id="plots-x-spec"></div>
                    </div>
                    <div id="plots-sidebar-y-div" class="plots">
                        <h4>Y Axis</h4>
                        <br><h5>Data Type</h5>
                        <select id="plots-y-data-type">
                            <option value="genetic_profile">Genetic Profile</option>
                            <option value="clinical_attribute">Clinical Attribute</option>
                        </select>
                        <div id="plots-y-spec"></div>
                    </div>
                    <div id="plots-sidebar-util-div" class="plots">
                        <h4>Search</h4>
                        <br><h5>Case(s)</h5><input type="text" name="case_id_search_keyword" placeholder="case id.."><br>
                        <h5>Mutation(s)</h5><input type="text" name="mutation_search_keyword" placeholder="protein change.."><br>
                    </div>        
                </div>
            </td>
            <td>
                <div id="plots-box" class="plots"></div>
            </td>
        </tr>
    </table>
</div>


<script>
    $(document).ready( function() {
        plotsTab.init();
    });
</script>


