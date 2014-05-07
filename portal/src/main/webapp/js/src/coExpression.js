/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

/**
 * Render the Co-expression view using dataTable Jquery Plugin,
 * along side a plot for selected row
 *
 * User: yichao
 * Date: 12/5/13
 */

var CoExpView = (function() {

    //Pre settings for every sub tab instance
    var Prefix = {
            divPrefix: "coexp_",
            loadingImgPrefix: "coexp_loading_img_",
            tableDivPrefix: "coexp_table_div_",
            tablePrefix: "coexp_table_",
            plotPrefix: "coexp_plot_",
        },
        dim = {
            coexp_table_width: "380px",
            coexp_plots_width: "750px"
        },
        threshold = 0.3;
    //Containers    
    var profileList = [], //Profile Lists for all queried genes
        coExpTableArr = [];

    //Sub tabs
    var Tabs = (function() {

        function appendTabsContent() {
            $.each(window.PortalGlobals.getGeneList(), function(index, value) {
                $("#coexp-tabs-list").append("<li><a href='#" + Prefix.divPrefix + cbio.util.safeProperty(value) + 
                  "' class='coexp-tabs-ref'><span>" + value + "</span></a></li>");
            });
        }

        function appendLoadingImgs() {
            $.each(window.PortalGlobals.getGeneList(), function(index, value) {
                $("#coexp-tabs-content").append("<div id='" + Prefix.divPrefix + cbio.util.safeProperty(value) + "'>" +
                    "<div id='" + Prefix.loadingImgPrefix + cbio.util.safeProperty(value) + "'>" +
                    "<table><tr><td><img style='padding:20px;' src='images/ajax-loader.gif'></td>" + 
                    "<td>Calculating and rendering may take up to 1 minute.</td></tr></table>" + 
                    "</div></div>");
            });
        }

        function generateTabs() {
            $("#coexp-tabs").tabs();
            $("#coexp-tabs").tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
            $("#coexp-tabs").tabs("option", "active", 0);
            $(window).trigger("resize");
        }

        function bindListenerToTabs() {
            $("#coexp-tabs").on("tabsactivate", function(event, ui) {
                var _gene = ui.newTab.text();
                var coExpSubTabView = new CoExpSubTabView();
                coExpSubTabView.init(_gene);
            });
        }

        return {
            appendTabsContent: appendTabsContent,
            appendLoadingImgs: appendLoadingImgs,
            generateTabs: generateTabs,
            bindListenerToTabs: bindListenerToTabs
        }

    }());

    var ProfileSelector = (function() {

        function filterProfiles(_profileList) {
            $.each(_profileList, function(i, obj) {
                if (obj["GENETIC_ALTERATION_TYPE"] === "MRNA_EXPRESSION") {
                    if (obj["STABLE_ID"].toLowerCase().indexOf("zscores") !== -1) {
                        if (obj["STABLE_ID"].toLowerCase().indexOf("merged_median_zscores") !== -1) {
                            profileList.push(obj);
                        }
                    } else {
                        profileList.push(obj);
                    }
                }
            })
            //swap the rna seq profile to the top
            $.each(profileList, function(i, obj) {
                if (obj["STABLE_ID"].toLowerCase().indexOf("rna_seq") !== -1) {
                    cbio.util.swapElement(profileList, i, 0);
                }
            });
        }

        function drawProfileSelector() {
            $("#coexp-profile-selector-dropdown").append(
                "Data Set " + 
                "<select id='coexp-profile-selector'></select>");
            $.each(profileList, function(index, value) {
                $("#coexp-profile-selector").append(
                    "<option value='" + value["STABLE_ID"] + "'>" +
                    value["NAME"] + "</option>"
                );            
            });
        }

        function bindListener() {
            $("#coexp-profile-selector").change(function() {
                var geneIds = window.PortalGlobals.getGeneList();
                $.each(geneIds, function(index, value) {
                    //Distroy all the subview instances
                    var element =  document.getElementById(Prefix.tableDivPrefix + cbio.util.safeProperty(value));
                    if (typeof(element) !== 'undefined' && element !== null) { 
                        element.parentNode.removeChild(element); //destroy all the existing instances
                    }
                    element =  document.getElementById(Prefix.plotPrefix + cbio.util.safeProperty(value));
                    if (typeof(element) !== 'undefined' && element !== null) { 
                        element.parentNode.removeChild(element); //destroy all the existing instances
                    }   
                    //Empty all the sub divs
                    $("#" + Prefix.tableDivPrefix + cbio.util.safeProperty(value)).empty();
                    $("#" + Prefix.plotsPreFix + cbio.util.safeProperty(value)).empty();
                    $("#" + Prefix.loadingImgPrefix + cbio.util.safeProperty(value)).empty();
                    //Add back loading imgs
                    $("#" + Prefix.loadingImgPrefix + cbio.util.safeProperty(value)).append(
                        "<table><tr><td><img style='padding:20px;' src='images/ajax-loader.gif'></td>" + 
                        "<td>Calculating and rendering may take up to 1 minute.</td></tr></table>" + 
                        "</div>");
                });
                //Re-draw the currently selected sub-tab view
                var curTabIndex = $("#coexp-tabs").tabs("option", "active");
                var coExpSubTabView = new CoExpSubTabView();
                coExpSubTabView.init(geneIds[curTabIndex]);
            });
        }

        return {
            init: function(_profileList) {
                filterProfiles(_profileList);
                drawProfileSelector();
                bindListener();
            }
        }

    }()); //Closing Profile Selector


    //Instance of each sub tab
    var CoExpSubTabView = function() {

        var Names = {
                divId: "", //Id for the div of the single query gene (both coexp table and plot)
                loadingImgId: "", //Id for ajax loading img
                tableId: "", //Id for the co-expression table
                tableDivId: "", //Id for the div of the co-expression table
                plotsId: "" //Id for the plots on the right
            },
            geneId = "", //Gene of this sub tab instance
            coexpTableArr = [], //Data array for the datatable
            coExpTableInstance = "";

        var CoExpTable = function() {

            function configTable() {
                //Draw out the markdown of the datatable
                $("#" + Names.tableId).append(
                    "<thead style='font-size:70%;' >" +
                    "<tr>" + 
                    "<th>Correlated Gene</th>" +
                    "<th>Pearson's Correlation</th>" +
                    "<th>Spearman's Correlation</th>" +
                    "</tr>" +
                    "</thead><tbody></tbody>"
                );

                //Configure the datatable with  jquery
                coExpTableInstance = $("#" + Names.tableId).dataTable({
                    "sDom": '<"H"f<"coexp-table-filter-pearson">>t<"F"i<"datatable-paging"p>>',
                    "bPaginate": true,
                    "sPaginationType": "two_button",
                    "bInfo": true,
                    "bJQueryUI": true,
                    "bAutoWidth": false,
                    "aaData" : coexpTableArr,
                    "aaSorting": [[1, 'desc']],
                    "aoColumnDefs": [
                        {
                            "bSearchable": true,
                            "aTargets": [ 0 ],
                            "sWidth": "56%"
                        },
                        {
                            "sType": 'coexp-absolute-value',
                            "bSearchable": false,
                            "aTargets": [ 1 ],
                            "sWidth": "22%"
                        },
                        {
                            "sType": 'coexp-absolute-value',
                            "bSearchable": false,
                            "aTargets": [ 2 ],
                            "sWidth": "22%"
                        }
                    ],
                    "sScrollY": "600px",
                    "bScrollCollapse": true,
                    //iDisplayLength: coexp_table_arr.length,
                    "oLanguage": {
                        "sSearch": "Search Gene"
                    },
                    "bDeferRender": true,
                    "iDisplayLength": 30,
                    "fnRowCallback": function(nRow, aData) {
                        $('td:eq(0)', nRow).css("font-weight", "bold");
                        $('td:eq(1)', nRow).css("font-weight", "bold");
                        if (aData[1] > 0) {
                            $('td:eq(1)', nRow).css("color", "#3B7C3B");
                        } else {
                            $('td:eq(1)', nRow).css("color", "#B40404");
                        }
                        if (aData[2] > 0) {
                            $('td:eq(2)', nRow).css("color", "#3B7C3B");
                        } else {
                            $('td:eq(2)', nRow).css("color", "#B40404");
                        }
                    },
                    "fnInfoCallback": function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
                        if (iTotal === iMax) {
                            return iStart +" to "+ iEnd + " of " + iTotal;
                        } else {
                            return iStart + " to " + iEnd + " of " + iTotal + " (filtered from " + iMax + " total)";
                        }
                    }
                });  
            }

            function attachDownloadFullResultButton() {
                //Append download full result button at the bottom of the table
                var downloadFullResultForm = "<form style='float:right;' action='getCoExp.do' method='post'>" +
                    "<input type='hidden' name='cancer_study_id' value='" + window.PortalGlobals.getCancerStudyId() + "'>" +
                    "<input type='hidden' name='gene' value='" + geneId + "'>" +
                    "<input type='hidden' name='profile_id' value='" + $("#coexp-profile-selector :selected").val() + "'>" + 
                    "<input type='hidden' name='case_set_id' value='" + window.PortalGlobals.getCaseSetId() + "'>" +
                    "<input type='hidden' name='case_ids_key' value='" + window.PortalGlobals.getCaseIdsKey() + "'>" +
                    "<input type='hidden' name='is_full_result' value='true'>" +
                    "<input type='submit' value='Download Full Results'></form>";
                $("#" + Names.tableDivId).append(downloadFullResultForm);            
            }

            function attachPearsonFilter() { 
                //Add drop down filter for positive/negative pearson display
                $("#" + Names.tableDivId).find('.coexp-table-filter-pearson').append(
                    "<select id='coexp-table-select-" + cbio.util.safeProperty(geneId) + "' style='width: 230px; margin-left: 5px;'>" +
                    "<option value='all'>Show All</option>" +
                    "<option value='positivePearson'>Show Only Positively Correlated</option>" +
                    "<option value='negativePearson'>Show Only Negatively Correlated</option>" +
                    "</select>");
                $("select#coexp-table-select-" + cbio.util.safeProperty(geneId)).change(function () {
                    if ($(this).val() === "negativePearson") {
                        coExpTableInstance.fnFilter("-", 1, false);
                    } else if ($(this).val() === "positivePearson") {
                        coExpTableInstance.fnFilter('^[0-9]*\.[0-9]*$', 1, true);
                    } else if ($(this).val() === "all") {
                        coExpTableInstance.fnFilter("", 1);
                    }
                });
            }

            function attachRowListener() {
                $("#" + Names.tableId + " tbody tr").live('click', function (event) {
                    //Highlight selected row
                    $(coExpTableInstance.fnSettings().aoData).each(function (){
                        $(this.nTr).removeClass('row_selected');
                    });
                    $(event.target.parentNode).addClass('row_selected');
                    //Get the gene name of the selected row
                    var aData = coExpTableInstance.fnGetData(this);
                    if (null !== aData) {
                        $("#" + Names.plotId).empty();
                        $("#" + Names.plotId).append("<img style='padding:220px;' src='images/ajax-loader.gif'>");
                        var coexpPlots = new CoexpPlots();
                        coexpPlots.init(Names.plotId, geneId, aData[0], aData[1], aData[2], $("#coexp-profile-selector :selected").val());
                    }
                })
            }

            function initTable() {
                //Init with selecting the first row
                $('#' + Names.tableId + ' tbody tr:eq(0)').click();
                $('#' + Names.tableId + ' tbody tr:eq(0)').addClass("row_selected");
            }

            //Overwrite some datatable function for custom filtering
            function overWriteFilters() {
                jQuery.fn.dataTableExt.oSort['coexp-absolute-value-desc'] = function(a,b) {
                    if (Math.abs(a) > Math.abs(b)) return -1;
                    else if (Math.abs(a) < Math.abs(b)) return 1;
                    else return 0;
                };
                jQuery.fn.dataTableExt.oSort['coexp-absolute-value-asc'] = function(a,b) {
                    if (Math.abs(a) > Math.abs(b)) return 1;
                    else if (Math.abs(a) < Math.abs(b)) return -1;
                    else return 0;
                };
            }  

            function convertData(_result) {
                //Convert the format of the callback result to fit datatable
                coexpTableArr = [];
                $.each(_result, function(i, obj) {
                    var tmp_arr = [];
                    tmp_arr.push(obj.gene);
                    tmp_arr.push(obj.pearson.toFixed(2));
                    tmp_arr.push(obj.spearman.toFixed(2));
                    coexpTableArr.push(tmp_arr);
                });           
            }

            function getCoExpDataCallBack(result, geneId) {
                //Hide the loading img
                $("#" + Names.loadingImgId).empty();
                //Render datatable
                convertData(result);
                overWriteFilters(); 
                configTable();
                attachDownloadFullResultButton();
                attachPearsonFilter();
                attachRowListener();
                initTable();
            }

            return {
                init: function(_geneId) {
                    //Getting co-exp data (for currently selected gene/profile) from servlet
                    $("#" + Names.plotId).empty();
                    var paramsGetCoExpData = {
                         cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                         gene: _geneId,
                         profile_id: $("#coexp-profile-selector :selected").val(),
                         case_set_id: window.PortalGlobals.getCaseSetId(),
                         case_ids_key: window.PortalGlobals.getCaseIdsKey(),
                         is_full_result: "false"
                    };
                    $.post(
                        "getCoExp.do", 
                        paramsGetCoExpData, 
                        function(result) {
                            getCoExpDataCallBack(result, _geneId);
                        },
                        "json"
                    );
                }
            }          
        } //Closing CoExpTable

        function assembleNames() {
            //figure out div id
            var safeGeneId = cbio.util.safeProperty(geneId);
            Names.divId = Prefix.divPrefix + safeGeneId;
            Names.loadingImgId = Prefix.loadingImgPrefix + safeGeneId;
            Names.tableId = Prefix.tablePrefix + safeGeneId + jQuery.now();
            Names.tableDivId = Prefix.tableDivPrefix + safeGeneId;
            Names.plotId = Prefix.plotPrefix + safeGeneId;
        }

        function drawLayout() {
            //Configure the layout(div) of table and plots
            $("#" + Names.divId).append(
                "<table>" +
                "<tr>" +
                "<td width='" + dim.coexp_table_width + "' valign='top'>" + 
                "<div id='" + Names.tableDivId + "'></div></td>" +
                "<td width='" + dim.coexp_plots_width + "' valign='top'>" + 
                "<div id='" + Names.plotId + "'></div></td>" +
                "</tr>" +
                "</table>");
            $("#" + Names.tableDivId).addClass("coexp-table");
            $("#" + Names.tableDivId).addClass("coexp-plots");
            $("#" + Names.tableDivId).append(
                "<table id='" + Names.tableId + "' class='display coexp_datatable_" + geneId + "' cellpadding='0' cellspacing='0' border='0'></table>");
        }

        return {
            init: function(_geneId) {
                //Set the attributes of the sub-view instance
                geneId = _geneId;
                //TODO: Just a quick fix for the sub-tab collapse bug
                $(window).trigger("resize");
                //Get the div id of the right sub-tab
                var element = $(".coexp_datatable_" + cbio.util.safeProperty(_geneId));
                if (element.length === 0) { //Avoid duplication (see if the subtab instance already exists)
                    assembleNames();
                    drawLayout();
                    var coExpTable = new CoExpTable();
                    coExpTable.init(geneId);
                }
            }
        }

    }   //Closing coExpSubTabView

    function getGeneticProfileCallback(result) {
        var _genes = window.PortalGlobals.getGeneList();
        //Init Profile selector
        ProfileSelector.init(result[_genes[0]]); 
        if (profileList.length === 1) {
            $("#coexp-profile-selector-dropdown").hide();
        }
        var coExpSubTabView = new CoExpSubTabView();
        coExpSubTabView.init(_genes[0]);
    }

    return {
        init: function() {
            //Init Tabs
            Tabs.appendTabsContent();
            Tabs.appendLoadingImgs();
            Tabs.generateTabs();
            Tabs.bindListenerToTabs();
            //Get all the genetic profiles with data available 
            var paramsGetProfiles = {
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey(),
                gene_list: window.PortalGlobals.getGeneListString()
            };
            $.post("getGeneticProfile.json", paramsGetProfiles, getGeneticProfileCallback, "json");
        }
    };

}());    //Closing CoExpView
