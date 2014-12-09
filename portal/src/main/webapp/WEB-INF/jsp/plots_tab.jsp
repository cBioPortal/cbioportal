<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticAlterationType" %>

<script type="text/javascript" src="js/src/plots-tab/plots.js"></script>
<script type="text/javascript" src="js/src/plots-tab/proxy/fetchMetaData.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/IdMapper.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/profileSpec.js"></script>

<style>
    #plots .plots {
        border: 1px solid #aaaaaa;
        border-radius: 4px;
        margin: 10px;
    }
    
    #plots-sidebar {
        width: 350px;
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
        height: 90px;
    }
    
    #plots-sidebar h3 {
        margin: 10px;
    }
    #plots-sidebar h4 {
        margin: 10px;
    }
    #plots-sidebar h5 {
        margin-top: -5px;
        margin-left: 20px;
        padding-left: 5px;
        padding-right: 5px;
        display: inline-block;
        font-weight: bold;  
        margin-bottom: 20px;
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
            <h5>Data Type</h5> 
            <select>
                <option value="genetic_profile">Genetic Profile</option>
                <option value="clinical_attribute">Clinical Attribute</option>
            </select>
            <div id="plots-x-spec"></div>
        </div>
        <div id="plots-sidebar-y-div" class="plots">
            <h4>Y Axis</h4>
            <h5>Data Type</h5>
            <select>
                <option value="genetic_profile">Genetic Profile</option>
                <option value="clinical_attribute">Clinical Attribute</option>
            </select>
            <div id="plots-y-spec"></div>
        </div>
        <div id="plots-sidebar-util-div" class="plots">
            <h4>Search</h4>
            <h5>By Case Id</h5>
            <input type="text" name="fname"><br>
        </div>        
    </div>
</div>


<script>
    $(document).ready( function() {
        Plots.init();
        profileSpec.init("x");
        profileSpec.init("y");
    });
</script>


