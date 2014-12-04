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
    #plots-sidebar-title {
        margin: 10px;
    }
    #plots-sidebar-x-axis {
        height: 230px;
    }
    #plots-sidebar-y-axis {
        height: 230px;
    }
    #plots-sidebar-filters {
        height: 90px;
    }
    
    #plots-sidebar .plots-opts {
        margin-left: 40px;
        margin-top: 10px;
    }
    #plots-sidebar h5 {
        margin-top: -5px;
        margin-left: 20px;
        padding-left: 5px;
        padding-right: 5px;
        background-color: white;
        display: inline-block;
        font-weight: bold;  
        margin-bottom: 20px;
    }
    #plots-content {
        width: 800px;
        height: 610px;
    }
</style>


<div id="plots" class="section" np-app="menuApp">
    <div id='test-angular' ng-controller='MenuController as menu'>
        <div ng-repeat="item in menu.items" ng-show="item.isGenomicProfile">
            <span>{{item.title}}</span>
            <select ng-model="item" ng-options="opt as opt.label for opt in item.options"></select>
        </div>
    </div>
    <table>
        <tr>
            <td>
                <div id="plots-sidebar">
                    <div id='plots-sidebar-title'><h4>Plots Parameters</h4></div>
                    <div id='plots-sidebar-x-axis' class='plots'>
                        <h5>X Axis</h5>
                        <div id='plots-x-axis-plots-type-div' class='plots-opts'>
                            <label for="plots-x-axis-plots-type">Plots Type</label>
                            <select id="plots-x-axis-plots-type">
                                <option value="genomic-profile">Genomic Profile</option>
                                <option value="clinical-attributes">Clinical Attributes</option>
                            </select>
                        </div>
                        <!-- genome profile vision-->
                        <div id="plots-x-axis-profile-type-div" class='plots-opts' style="display:none">
                            <label for="plots-x-axis-profile-type">Profile Type</label>
                            <select id="plots-x-axis-profile-type"></select>
                        </div>
                        <div id="plots-x-axis-profile-name-div" class='plots-opts' style="display:none">
                            <label for="plots-x-axis-profile-type">Profile Name</label>
                            <select id="plots-x-axis-profile-name"></select>
                        </div>
                        <div id="plots-x-axis-gene-div" class='plots-opts' style="display:none">
                            <label for="plots-x-axis-profile-type">Gene</label>
                            <select id="plots-x-axis-gene"></select>
                        </div>
                        <!-- clinical attributes vision-->
                        <div id="plots-x-axis-clinical-attr-div" class='plots-opts' style="display:none">
                            <label for="plots-x-axis-profile-type">Clinical Attribute</label>
                            <select id="plots-x-axis-clinical-attr"></select>
                        </div>
                    </div>
                    <div id='plots-sidebar-y-axis' class='plots'>
                        <h5>Y Axis</h5>
                        <div id='plots-y-axis-plots-type-div' class='plots-opts'>
                            <label for="plots-y-axis-plots-type">Plots Type</label>
                            <select id="plots-y-axis-plots-type">
                                <option value="genomic-profile">Genomic Profile</option>
                                <option value="clinical-attributes">Clinical Attributes</option>
                            </select>
                        </div>
                        <!-- genome profile vision-->
                        <div id="plots-y-axis-profile-type-div" class='plots-opts' style="display:none"></div>
                        <div id="plots-y-axis-profile-name-div" class='plots-opts' style="display:none"></div>
                        <div id="plots-y-axis-gene-div" class='plots-opts' style="display:none"></div>
                        <!-- clinical attributes vision-->
                        <div id="plots-y-axis-clinical-attr" class='plots-opts' style="display:none"></div>
                    </div>
                    <div id='plots-sidebar-filters' class='plots'>
                        <h5>Filters</h5>
                    </div>
                </div>
            </td>
            <td>
                <div id="plots-content" class='plots'>
                    
                </div>
            </td>        
        </tr>
    </table>
</div>
