<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticAlterationType" %>

<script type="text/javascript" src="js/lib/angular.min.js"></script> 
<script type="text/javascript" src="js/src/plots-tab/view/menu.js"></script>

<style>
    #plots .plots {
        border: 1px solid #aaaaaa;
        border-radius: 4px;
        margin: 10px;
    }
    #plots-sidebar {
        width: 350px;
        height: 610px;
    }
    #plots-sidebar-x {
        width: inherit;
        height: 250px;
    }
    #plots-sidebar-y {
        width: inherit;
        height: 250px;
    }
    #plots-sidebar-search {
        width: inherit;
        height: 50px;
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


<script>
    $(document).ready( function() {
        PlotsTabSidebar.init();
    });
</script>


