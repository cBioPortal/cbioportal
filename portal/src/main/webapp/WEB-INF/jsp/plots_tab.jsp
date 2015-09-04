<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

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
<script type="text/javascript" src="js/src/plots-tab/util/gisticInterpreter.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/clinicalDataInterpreter.js"></script>
<script type="text/javascript" src="js/src/plots-tab/util/stylesheet.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/profileSpec.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/clinSpec.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/optSpec.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/scatterPlots.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/boxPlots.js"></script>
<script type="text/javascript" src="js/src/plots-tab/view/components/heatMap.js"></script>

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
        height: 222px;
    }
    #plots-sidebar-y-div {
        width: inherit;
        height: 222px;
    }
    #plots-sidebar-util-div {
        width: inherit;
        height: 190px;
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
        width: 850px;
        height: 670px;
        float: right;
    }
    #plots-tab-swap-btn {
        -moz-box-shadow:inset 0px 1px 0px 0px #ffffff;
        -webkit-box-shadow:inset 0px 1px 0px 0px #ffffff;
        box-shadow:inset 0px 1px 0px 0px #ffffff;
        background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #f9f9f9), color-stop(1, #e9e9e9));
        background:-moz-linear-gradient(top, #f9f9f9 5%, #e9e9e9 100%);
        background:-webkit-linear-gradient(top, #f9f9f9 5%, #e9e9e9 100%);
        background:-o-linear-gradient(top, #f9f9f9 5%, #e9e9e9 100%);
        background:-ms-linear-gradient(top, #f9f9f9 5%, #e9e9e9 100%);
        background:linear-gradient(to bottom, #f9f9f9 5%, #e9e9e9 100%);
        filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#f9f9f9', endColorstr='#e9e9e9',GradientType=0);
        background-color:#f9f9f9;
        -moz-border-radius:6px;
        -webkit-border-radius:6px;
        border-radius:6px;
        border:1px solid #dcdcdc;
        display:inline-block;
        color:#666666;
        font-weight:bold;
        text-decoration:none;
        text-shadow:0px 1px 0px #ffffff;
        margin-top: -30px;
        float: right;
    }
</style>

<div class="section" id="plots">
    <table>
        <tr>
            <td>
                 <div id="plots-sidebar">
                    <div id="plots-sidebar-x-div" class="plots">
                        <h4>Horizontal Axis</h4>
                        <div id="plots-x-data-type" style="padding-left:20px;">
                            <input type="radio" name="plots-x-data-type" value="genetic_profile" checked>Genetic Profile
                            <input type="radio" name="plots-x-data-type" value="clinical_attribute">Clinical Attribute
                        </div>
                        <div id="plots-x-spec"></div>
                    </div>
                    <button id='plots-tab-swap-btn'><img src='images/swap.png'></button>
                    <div id="plots-sidebar-y-div" class="plots">
                        <h4>Vertical Axis</h4>
                        <div id="plots-y-data-type" style="padding-left:20px;">
                            <input type="radio" name="plots-y-data-type" value="genetic_profile" checked>Genetic Profile
                            <input type="radio" name="plots-y-data-type" value="clinical_attribute">Clinical Attribute
                        </div>
                        <div id="plots-y-spec"></div>
                    </div>
                    <div id="plots-sidebar-util-div" class="plots">
                        <h4>Utilities</h4>
                        <h5>Search Case(s)</h5><input type="text" id="case_id_search_keyword" name="case_id_search_keyword" placeholder="Case ID.." onkeyup="search_case_id();"><br>
                        <h5>Search Mutation(s)</h5><input type="text" id="mutation_search_keyword" name="mutation_search_keyword" placeholder="Protein Change.." onkeyup="search_mutation();"><br>
                        <div id="mutation_details_vs_gistic_view" class="mutation_details_vs_gistic_view" style="display:inline;"></div>
                        <h5>Download</h5><div id="download_buttons" style="display: inline;"></div>
                    </div>        
                </div>
            </td>
            <td>
                <div id="plots-box" class="plots" style="overflow: scroll;">
                </div>
            </td>
        </tr>
    </table>
</div>


<script>
    $(document).ready( function() {
        var plots_tab_init = false;
        if ($("#plots").is(":visible")) {
            plotsTab.init();
            plots_tab_init = true;
        } else {
            $(window).trigger("resize");
        }
        $("#tabs").bind("tabsactivate", function(event, ui) {
            if (ui.newTab.text().trim().toLowerCase() === "plots") {
                if (plots_tab_init === false) {
                    plotsTab.init();
                    plots_tab_init = true;
                    $(window).trigger("resize");
                } else {
                    $(window).trigger("resize");
                }
            }
        });
    });
</script>


