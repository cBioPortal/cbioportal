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
<script type="text/javascript" src="js/src/plots-tab/util/idMapper.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/textMapper.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/mutationTranslator.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/profileSpec.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/clinSpec.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/scatterPlots.js"></script>

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
        margin: 10px;
        margin-bottom: 20px;
        font-size: 14px;
    }
    #plots-sidebar h4 {
        margin: 20px;
        font-size: 12px;
        color: grey;
        background-color: white;
        margin-top: -35px;
        display: inline-table;
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
    #plots-content {
        width: 800px;
        height: 610px;
    }
</style>

<div class="section" id="plots">
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
            <h4>Filters</h4>
            <br><h5>Search case(s)</h5><input type="text" name="case_id_search_keyword"><br>
            <h5>Search mutation(s)</h5><input type="text" name="mutation_search_keyword"><br>
        </div>        
    </div>
</div>


<script>
    $(document).ready( function() {
        plotsTab.init();
    });
</script>


