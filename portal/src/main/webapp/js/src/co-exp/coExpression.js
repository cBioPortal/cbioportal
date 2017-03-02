/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
            plotPrefix: "coexp_plot_"
        },
        dim = {
            coexp_table_width: "380px",
            coexp_plots_width: "750px"
        },
        has_mutation_data = false;
    //Containers    
    var profileList = []; //Profile Lists for all queried genes
    
    function getGeneEntityIds(genes, genesets) {
        var geneIds = [];
        if (genes !== null) {
            geneIds = genes;
        }
        var genesetIds = [];
        if (genesets !== null) {
            genesetIds = genesets;
        }
        var geneEntityIds = geneIds.concat(genesetIds);
        return geneEntityIds;
    }

    //Sub tabs
    var Tabs = (function() {

        /**
         * This function creates a sub-tab for each queried genetic entity.
         * 
         * @returns
         */
        function appendTabsContent() {
            geneEntityIds = getGeneEntityIds(window.QuerySession.getQueryGenes(), window.QuerySession.getQueryGenesets()); 
            $.each(geneEntityIds, function(index, value) {
                $("#coexp-tabs-list").append("<li><a href='#" + Prefix.divPrefix + cbio.util.safeProperty(value) + 
                  "' class='coexp-tabs-ref'><span>" + value + "</span></a></li>");
            });
        }

        /**
         * This function appends a loading image and a message in the selected sub-tab.
         * 
         * @returns
         */
        function appendLoadingImgs() {
            geneEntityIds = getGeneEntityIds(window.QuerySession.getQueryGenes(), window.QuerySession.getQueryGenesets());
            $.each(geneEntityIds, function(index, value) {
                $("#coexp-tabs-content").append("<div id='" + Prefix.divPrefix + cbio.util.safeProperty(value) + "'>" +
                    "<div id='" + Prefix.loadingImgPrefix + cbio.util.safeProperty(value) + "'>" +
                    "<table><tr><td><img style='padding:20px;' src='images/ajax-loader.gif' alt='loading' /></td>" +
                    "<td>Calculating and rendering... (this may take up to 1 minute)</td></tr></table>" +
                    "</div></div>");
            });
        }

        function generateTabs() {
            $("#coexp-tabs").tabs();
            $("#coexp-tabs").tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
            $("#coexp-tabs").tabs("option", "active", 0);
            $(window).trigger("resize");
        }

        /**
         * This function initializes the sub-tab that has been clicked.
         * @returns
         */
        function bindListenerToTabs() {
            $("#coexp-tabs").on("tabsactivate", function(event, ui) {
                var _genetic_entity = ui.newTab.text();
                var _genetic_entity_type = null;
                if (window.QuerySession.getQueryGenes() !== null) {
                    if (window.QuerySession.getQueryGenes().indexOf(_genetic_entity) !== -1) {
                        _genetic_entity_type = "GENE";
                    }
                } 
                if (window.QuerySession.getQueryGenesets() !== null) {
                    if (window.QuerySession.getQueryGenesets().indexOf(_genetic_entity) !== -1) {
                        _genetic_entity_type = "GENESET";
                    }
                }
                var coExpSubTabView = new CoExpSubTabView();
                coExpSubTabView.init(_genetic_entity, _genetic_entity_type);
            });
        }

        return {
            appendTabsContent: appendTabsContent,
            appendLoadingImgs: appendLoadingImgs,
            generateTabs: generateTabs,
            bindListenerToTabs: bindListenerToTabs
        };

    }());

    var ProfileSelector = (function() {

        function filterProfiles(_profileList) {
            $.each(_profileList, function(i, obj) {
                if (obj["GENETIC_ALTERATION_TYPE"] === "MRNA_EXPRESSION" || obj["GENETIC_ALTERATION_TYPE"] === "PROTEIN_LEVEL") {
                    if (obj["STABLE_ID"].toLowerCase().indexOf("zscores") !== -1) {
                        if (obj["STABLE_ID"].toLowerCase().indexOf("merged_median_zscores") !== -1) {
                            profileList.push(obj);
                        }
                    } else {
                        profileList.push(obj);
                    }
                } else if (obj["GENETIC_ALTERATION_TYPE"] === "MUTATION_EXTENDED") {
                    if (window.QuerySession.getQueryGenes() !== null) {
                        has_mutation_data = true;
                    }
                }
            });
            //swap the rna seq profile to the top
            $.each(profileList, function(i, obj) {
                if (obj["STABLE_ID"].toLowerCase().indexOf("rna_seq") !== -1) {
                    cbio.util.swapElement(profileList, i, 0);
                }
            });
        }

        function drawProfileSelector() {
            $("#coexp-profile-selector-dropdown").append(
                "Gene Expression Data Set " + 
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
                geneEntityIds = getGeneEntityIds(window.QuerySession.getQueryGenes(), window.QuerySession.getQueryGenesets());
                $.each(geneEntityIds, function(index, value) {
                    //Destroy all the sub-view instances
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
                        "<table><tr><td><img style='padding:20px;' src='images/ajax-loader.gif' alt='loading' /></td>" +
                        "<td>Calculating and rendering may take up to 1 minute.</td></tr></table>" +
                        "</div>");
                    //Clean geneMap and genesetMap
                    geneMap = {};
                    genesetMap = {};
                });
                //Re-draw the currently selected sub-tab view
                var curTabIndex = $("#coexp-tabs").tabs("option", "active");
                var _genetic_entity_type = null;
                if ((window.QuerySession.getQueryGenes()).indexOf(geneEntityIds[curTabIndex]) !== -1) {
                        _genetic_entity_type = "GENE";
                } else if ((window.QuerySession.getQueryGenesets()).indexOf(geneEntityIds[curTabIndex]) !== -1) {
                        _genetic_entity_type = "GENESET";
                }
                var coExpSubTabView = new CoExpSubTabView();
                coExpSubTabView.init(geneEntityIds[curTabIndex], _genetic_entity_type);
            });
        }

        return {
            init: function(_profileList) {
                filterProfiles(_profileList);
                drawProfileSelector();
                bindListener();
            }
        };

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
            geneEntityId = "", //Genetic entity of this sub tab instance
            geneEntityType = "", //Type of genetic entity (gene or gene set)
            geneticEntityProfile = ""; //Profile of the genetic entity of the sub-tab
            coexpTableArr = [], //Data array for the datatable
            coExpTableInstance = "";

        var CoExpTable = function() {
            function shortenString(data, maxChar) {
                return data.length > maxChar ? "<span title=" + data +">" + data.substr( 0, maxChar
                        ) +'...' + "</span>" :
                data;
            }

            function configTable() {
                //Draw out the markdown of the datatable
                $("#" + Names.tableId).append(
                    "<thead style='font-size:70%;' >" +
                    "<tr>" + 
                    "<th>Correlated Genetic Entity</th>" +
                    "<th>Cytoband</th>" +
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
                            "sWidth": "56%",
                            render: function ( data, type, row ) {
                                return shortenString(data, 12);
                            }
                        },
                        {
                            "bSearchable": true, 
                            "aTargets": [ 1 ],
                            "sWidth": "22%"
                        },
                        {
                            "sType": 'coexp-absolute-value',
                            //TODO: should be disabled; this is just a quick fix, otherwise the fnfilter would work on this column
                            //"bSearchable": false, 
                            "bSearchable": true, 
                            "aTargets": [ 2 ],
                            "sWidth": "22%"
                        },
                        {
                            "sType": 'coexp-absolute-value',
                            "bSearchable": false,
                            "aTargets": [ 3 ],
                            "sWidth": "22%"
                        }
                    ],
                    "sScrollY": "600px",
                    "bScrollCollapse": true,
                    //iDisplayLength: coexp_table_arr.length,
                    "oLanguage": {
                        "sSearch": "Search Genetic Entity"
                    },
                    "bDeferRender": true,
                    "iDisplayLength": 30,
                    "fnRowCallback": function(nRow, aData) {
                        $('td:eq(0)', nRow).css("font-weight", "bold");
                        $('td:eq(2)', nRow).css("font-weight", "bold");
                        if (aData[2] > 0) {
                            $('td:eq(2)', nRow).css("color", "#3B7C3B");
                        } else {
                            $('td:eq(2)', nRow).css("color", "#B40404");
                        }
                        if (aData[3] > 0) {
                            $('td:eq(3)', nRow).css("color", "#3B7C3B");
                        } else {
                            $('td:eq(3)', nRow).css("color", "#B40404");
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

            /**
             * Adds a button in the bottom of the table to download the results displayed in the table.
             * 
             * @returns
             */
            function attachDownloadResultButton() {
                $("#" + Names.tableDivId).append("<button id='download_button' style='float:right;'>Download Results</button>");
                document.getElementById("download_button").onclick = function() {
                        //Function that creates the document to download the table in the data
                        var tableData=$("#" + Names.tableId).dataTable().fnGetData();
                        //Create a string with the full results, containing also the header
                        fullResultStr = ("Genetic Entity Symbol\tPearson Score\tSpearman Score\n");
                        tableData.forEach(function(geneticEntity) {
                                _row = "";
                                geneticEntity.forEach(function(value) {
                                        _row += value + "\t";
                                });
                                //Replace last '\t' for '\n'
                                _row = _row.slice(0,-1);
                                _row += "\n";
                                //Add the genetic entity to the final string
                                fullResultStr += _row;
                        });
                        //construct file name
                        _comparedGeneticEntities ="";
                        if ($("#gene_checkbox"+cbio.util.safeProperty(geneEntityId)).prop('checked')) {
                                if ($("#geneset_checkbox"+cbio.util.safeProperty(geneEntityId)).prop('checked')) {
                                        _comparedGeneticEntities = "genes_and_genesets";
                                } else {
                                        _comparedGeneticEntities +="genes";
                                }
                        } else {
                                if ($("#geneset_checkbox"+cbio.util.safeProperty(geneEntityId)).prop('checked')) {
                                        _comparedGeneticEntities += "genesets";
                                }
                        }
                        _fileName = "coexpression_" + geneEntityId + "_(" +
                        geneticEntityProfile.replace(/\\s+/g, "_") + ")_vs_" +
                        _comparedGeneticEntities + ".txt";
                        //Download the file
                        cbio.download.initDownload(fullResultStr, {filename: _fileName, contentType: 'text/plain', preProcess: null});
                }
            }

            function attachPearsonFilter() { 
                //Add drop down filter for positive/negative pearson display
                $("#" + Names.tableDivId).find('.coexp-table-filter-pearson').append(
                    "<select id='coexp-table-select-" + cbio.util.safeProperty(geneEntityId) + "' style='width: 230px; margin-left: 5px;'>" +
                    "<option value='all'>Show All</option>" +
                    "<option value='positivePearson'>Show Only Positively Correlated</option>" +
                    "<option value='negativePearson'>Show Only Negatively Correlated</option>" +
                    "</select>");
                $("select#coexp-table-select-" + cbio.util.safeProperty(geneEntityId)).change(function () {
                    if ($(this).val() === "negativePearson") {
                        $("#" + Names.tableId).dataTable().fnFilter("-", 2, false);
                    } else if ($(this).val() === "positivePearson") {
                        $("#" + Names.tableId).dataTable().fnFilter('^[0-9]*\.[0-9]*$', 2, true);
                    } else if ($(this).val() === "all") {
                        $("#" + Names.tableId).dataTable().fnFilter("", 2);
                    }
                });
            }
            
            /**
             * This function adds the 'Gene' and 'Gene Set' checkboxes if we have genesetProfile, and ensures that when these boxes are
             * checked, they display the correct information in the table.
             * 
             * @returns
             */
            function attachGeneticEntityButtons() {
                if (typeof (genesetProfile) !== "undefined") {
                    if (geneMap.hasOwnProperty(cbio.util.safeProperty(geneEntityId)) && geneMap[cbio.util.safeProperty(geneEntityId)].length >= 1) {
                        $("#" + Names.tableDivId).find('.coexp-table-filter-pearson').append(
                                "<br><input type='checkbox' id='gene_checkbox"+cbio.util.safeProperty(geneEntityId)+"' checked><label for='gene_checkbox'>Genes</label>" +
                                "<input type='checkbox' id='geneset_checkbox"+cbio.util.safeProperty(geneEntityId)+"' ><label for='geneset_checkbox'>Gene Sets</label>"
                                );
                    } else {
                        $("#" + Names.tableDivId).find('.coexp-table-filter-pearson').append(
                                "<br><input type='checkbox' id='gene_checkbox"+cbio.util.safeProperty(geneEntityId)+"' ><label for='gene_checkbox'>Genes</label>" +
                                "<input type='checkbox' id='geneset_checkbox"+cbio.util.safeProperty(geneEntityId)+"' checked><label for='geneset_checkbox'>Gene Sets</label>"
                                );
                    }
                }
                $("#" + Names.tableDivId).find('.coexp-table-filter-pearson').append("<div class='alert alert-warning' id='no_genes"+Names.tableDivId+"' style='display: none; padding: 3px; margin-bottom: 0px'>No correlated genes found.</div>");
                $("#" + Names.tableDivId).find('.coexp-table-filter-pearson').append("<div class='alert alert-warning' id='no_genesets"+Names.tableDivId+"' style='display: none; padding: 3px; margin-bottom: 0px'>No correlated gene sets found.</div>");
               
                $("#gene_checkbox"+cbio.util.safeProperty(geneEntityId)).change(function() {
                    if ($("#gene_checkbox"+cbio.util.safeProperty(geneEntityId)).prop('checked')) {
                        //The function will not be undefined since we always retrieve genes
                        if (geneMap.hasOwnProperty(cbio.util.safeProperty(geneEntityId)) && geneMap[cbio.util.safeProperty(geneEntityId)].length >= 1){ //Add the genes into the coexpTable again if they are not there
                            $("#" + Names.tableId).dataTable().fnAddData(geneMap[cbio.util.safeProperty(geneEntityId)]);
                        } else {
                            document.getElementById("no_genes"+Names.tableDivId).style.display = 'block'
                            }
                    } else { //Checkbox not checked, clear the whole table
                        $("#" + Names.tableId).dataTable().fnClearTable();
                        document.getElementById("no_genes"+Names.tableDivId).style.display = 'none'
                        if ($("#geneset_checkbox"+cbio.util.safeProperty(geneEntityId)).prop('checked')) { //If the gene sets box is checked, keep the gene sets
                            if (genesetMap.hasOwnProperty(cbio.util.safeProperty(geneEntityId)) && genesetMap[cbio.util.safeProperty(geneEntityId)].length >= 1) {
                                $("#" + Names.tableId).dataTable().fnAddData(genesetMap[cbio.util.safeProperty(geneEntityId)]);
                            }
                        }
                   }
               });
               $("#geneset_checkbox"+cbio.util.safeProperty(geneEntityId)).change(function() {
                   if ($("#geneset_checkbox"+cbio.util.safeProperty(geneEntityId)).prop('checked')) {
                        if (genesetMap.hasOwnProperty(cbio.util.safeProperty(geneEntityId))) { //Add the gene sets into the coexpTable
                            if (genesetMap[cbio.util.safeProperty(geneEntityId)].length >= 1) { //Only add gene sets if we have them
                                $("#" + Names.tableId).dataTable().fnAddData(genesetMap[cbio.util.safeProperty(geneEntityId)]);
                            } else {
                                document.getElementById("no_genesets"+Names.tableDivId).style.display = 'block'
                            }
                        } else { //If it is the first time that the checkbox is checked, retrieve the data
                            var paramsGetCoExpData = {
                                    cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
                                    genetic_entity: geneEntityId,
                                    genetic_entity_profile_id: genesetProfile,
                                    genetic_entity_profile_id: geneticEntityProfile,
                                    correlated_entities_to_find: "GENESET",
                                    correlated_entities_profile_id: genesetProfile,
                                    genetic_entity_type: geneEntityType, 
                                    case_set_id: window.QuerySession.getCaseSetId(),
                                    case_ids_key: window.QuerySession.getCaseIdsKey(),
                            };
                            $.post("getCoExp.do", paramsGetCoExpData, function(result) {
                                convertData(result, geneEntityId, "GENESET");
                                if (genesetMap[cbio.util.safeProperty(geneEntityId)].length >= 1) { //Only add gene sets if we have them
                                    $("#" + Names.tableId).dataTable().fnAddData(genesetMap[cbio.util.safeProperty(geneEntityId)]);
                                    } else {
                                        document.getElementById("no_genesets"+Names.tableDivId).style.display = 'block'
                                    }
                            }, "json");
                        }
                    } else { //Checkbox not checked, clear the whole table
                        $("#" + Names.tableId).dataTable().fnClearTable();
                        document.getElementById("no_genesets"+Names.tableDivId).style.display = 'none'
                        if ($("#gene_checkbox"+cbio.util.safeProperty(geneEntityId)).prop('checked')) { //If the gene box is checked, keep the genes
                            if (geneMap.hasOwnProperty(cbio.util.safeProperty(geneEntityId)) && geneMap.hasOwnProperty(cbio.util.safeProperty(geneEntityId)) && geneMap[cbio.util.safeProperty(geneEntityId)].length >= 1) {
                                $("#" + Names.tableId).dataTable().fnAddData(geneMap[cbio.util.safeProperty(geneEntityId)]);
                            }
                        }
                   }
               });
           }

            function attachRowListener() {
                $("#" + Names.tableId + " tbody tr").live('click', function (event) {
                    //Highlight selected row
                    $($("#" + Names.tableId).dataTable().fnSettings().aoData).each(function (){
                        $(this.nTr).removeClass('row_selected');
                    });
                    $(event.target.parentNode).addClass('row_selected');
                    //Get the gene name of the selected row
                    var aData = $("#" + Names.tableId).dataTable().fnGetData(this);
                    if (null !== aData) {
                        $("#" + Names.plotId).empty();
                        $("#" + Names.plotId).append("<img style='padding:220px;' src='images/ajax-loader.gif' alt='loading' />");
                        var coexpPlots = new CoexpPlots();
                        var entityXProfileId = null;
                        if (geneEntityType == "GENE") {
                            entityXProfileId = $("#coexp-profile-selector :selected").val();
                        } else if (geneEntityType == "GENESET") {
                            entityXProfileId = genesetProfile;
                        }
                        var entityXId = geneEntityId;
                        var entityYId = aData[0];
                        var entityYProfileId = null;
                        if (entityProfileMap[entityXId].hasOwnProperty("GENE") && entityProfileMap[entityXId]["GENE"].hasOwnProperty(entityYId)) {
                            entityYProfileId = entityProfileMap[entityXId]["GENE"][entityYId];
                        } else if (entityProfileMap[entityXId].hasOwnProperty("GENESET") && entityProfileMap[entityXId]["GENESET"].hasOwnProperty(entityYId)) {
                            entityYProfileId = entityProfileMap[entityXId]["GENESET"][entityYId];
                        }   
                        coexpPlots.init(Names.plotId, entityXId, entityYId, aData[2], aData[3], entityXProfileId, entityYProfileId);
                    }
                });
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

            function convertData(_result, geneEntityId, correlatedEntitiesToFind) {
                //Convert the format of the callback result to fit datatable, and fill the geneArr or genesetArr
                coexpTableArr = [];
                geneArr = [];
                genesetArr = [];
                entityProfileMapSubTab = {};
                $.each(_result, function(i, obj) {
                    var tmp_arr = [];
                    tmp_arr.push(obj.gene);
                    tmp_arr.push(obj.cytoband);
                    tmp_arr.push(obj.pearson.toFixed(2));
                    tmp_arr.push(obj.spearman.toFixed(2));
                    entityProfileMapSubTab[obj.gene] = obj.profileId;
                    coexpTableArr.push(tmp_arr);
                    if (correlatedEntitiesToFind == "GENE") {
                        geneArr.push(tmp_arr);
                    } else if (correlatedEntitiesToFind == "GENESET") {
                        genesetArr.push(tmp_arr);
                    }
                });
                if (correlatedEntitiesToFind == "GENE") {
                    geneMap[cbio.util.safeProperty(geneEntityId)] = geneArr;
                } else if (correlatedEntitiesToFind == "GENESET") {
                    genesetMap[cbio.util.safeProperty(geneEntityId)] = genesetArr;
                }
                if (!(entityProfileMap.hasOwnProperty(geneEntityId))) {
                    entityProfileMap[geneEntityId] = {};
                }
                entityProfileMap[geneEntityId][correlatedEntitiesToFind] = entityProfileMapSubTab;
            }

            function getCoExpDataCallBack(_result, correlatedEntitiesToFind, geneticEntityId, geneticEntityType) {
                //Hide the loading img
                $("#" + Names.loadingImgId).empty();
                if (_result.length === 0) { 
                    //If the call does not return correlated genes, find correlated gene sets
                    var paramsGetCoExpData = {
                            cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
                            genetic_entity: geneticEntityId,
                            genetic_entity_profile_id: geneticEntityProfile,
                            correlated_entities_to_find: "GENESET",
                            correlated_entities_profile_id: genesetProfile,
                            genetic_entity_type: geneticEntityType, 
                            case_set_id: window.QuerySession.getCaseSetId(),
                            case_ids_key: window.QuerySession.getCaseIdsKey(),
                };
                    $.post("getCoExp.do", paramsGetCoExpData, function(result) {
                        if (result.length === 0) {
                            $("#" + Names.tableDivId).append("There are no genes or gene sets paired with the queried genetic entity with a Pearson or Spearman score > 0.3 or < -0.3.");
                            } else {
                                convertData(result, geneticEntityId, "GENESET");
                                configTable();
                                attachDownloadResultButton();
                                attachPearsonFilter();
                                attachGeneticEntityButtons();
                                attachRowListener();
                                initTable();
                                }
                        },
                        "json");
                } else {
                    convertData(_result, geneticEntityId, correlatedEntitiesToFind);
                    overWriteFilters(); 
                    configTable();
                    attachDownloadResultButton();
                    attachPearsonFilter();
                    attachGeneticEntityButtons();
                    attachRowListener();
                    initTable();
                }
            }
            
            function getCoExpData(cancerStudyId, geneticEntity, geneticEntityProfileId, correlatedEntitiesToFind,
                    correlatedEntitiesProfileId, geneticEntityType, caseSetId, caseIdsKey) {
                var paramsGetCoExpData = {
                        cancer_study_id: cancerStudyId,
                        genetic_entity: geneticEntity,
                        genetic_entity_profile_id: geneticEntityProfileId,
                        correlated_entities_to_find: correlatedEntitiesToFind,
                        correlated_entities_profile_id: correlatedEntitiesProfileId,
                        genetic_entity_type: geneticEntityType, 
                        case_set_id: caseSetId,
                        case_ids_key: caseIdsKey,
                   };
                   $.post(
                       "getCoExp.do", 
                       paramsGetCoExpData, 
                       function(result) {
                           getCoExpDataCallBack(result, correlatedEntitiesToFind, geneticEntity, geneticEntityType);
                      },
                      "json"
                   );
            }

            return {
                init: function(_geneticEntityId, _geneticEntityType) {
                    //Getting co-exp data (for currently selected gene/profile) from servlet
                    $("#" + Names.plotId).empty();
                    //Determine the profile for the correlated entities
                    if (_geneticEntityType == "GENE") {
                        geneticEntityProfile = $("#coexp-profile-selector :selected").val();
                    } else if (_geneticEntityType == "GENESET") {
                        geneticEntityProfile = genesetProfile;
                    }
                    //By default, make a call to retrieve the correlated genes for the query
                    //All instances initialize with showing only correlated genes, except if the 
                    //query genetic entities appear only on gsva profiles
                    if (typeof ($("#coexp-profile-selector :selected").val()) !== "undefined") {
                        getCoExpData(window.QuerySession.getCancerStudyIds()[0], _geneticEntityId, geneticEntityProfile,
                                "GENE", $("#coexp-profile-selector :selected").val(), _geneticEntityType,
                                window.QuerySession.getCaseSetId(), window.QuerySession.getCaseIdsKey());
                    } else if (typeof (genesetProfile) !== "undefined") {
                        // If the query genetic entities appear only on gsva profiles, make a call to retrieve the 
                        //correlated gene sets for the query
                        getCoExpData(window.QuerySession.getCancerStudyIds()[0], _geneticEntityId, geneticEntityProfile,
                                "GENESET", genesetProfile, _geneticEntityType, window.QuerySession.getCaseSetId(),
                                window.QuerySession.getCaseIdsKey());
                    } else {
                        throw new Error("No profiles retrieved!")
                    }
                }
            };
        }; //Closing CoExpTable

        function assembleNames() {
            //figure out div id
            var safeGeneId = cbio.util.safeProperty(geneEntityId);
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
                "<table id='" + Names.tableId + "' class='display coexp_datatable_" + cbio.util.safeProperty(geneEntityId) + "' cellpadding='0' cellspacing='0' border='0'></table>");
        }
        
        return {
            init: function(_geneEntityId, _geneEntityType) {
                //Set the attributes of the sub-view instance
                geneEntityId = _geneEntityId;
                geneEntityType = _geneEntityType;
                //TODO: Just a quick fix for the sub-tab collapse bug
                $(window).trigger("resize");
                //Get the div id of the right sub-tab
                var element = $(".coexp_datatable_" + cbio.util.safeProperty(_geneEntityId));
                if (element.length === 0) { //Avoid duplication (see if the sub-tab instance already exists)
                    assembleNames();
                    drawLayout();
                    var coExpTable = new CoExpTable();
                    coExpTable.init(geneEntityId, geneEntityType);
                }
            }
        };

    };   //Closing coExpSubTabView

    /**
     * This function gets the genetic profiles obtained by the Jquery call and passes them to the Profile selector.
     * After that, it initializes the coExpSubTabView.
     * 
     * @param result Jquery result.
     * @param geneticEntityType the type of the genetic entity query (GENE or GENESET)
     * @returns
     */
    function getGeneticProfileCallback(result, geneticEntityType) {
        if (geneticEntityType === "GENESET") {
            //Create the drop-down menu if necessary
            ProfileSelector.init(result);
            if (Object.keys(result).length === 1) {
                $("#coexp-profile-selector-dropdown").hide();
            }
        } else {
        var _genes = window.QuerySession.getQueryGenes();
        //Init Profile selector
        var _profile_list = {};
        _.each(_genes, function(_gene) {
            _profile_list = _.extend(_profile_list, result[_gene]);
        });
        ProfileSelector.init(_profile_list);
        if (profileList.length === 1) {
            $("#coexp-profile-selector-dropdown").hide();
        }
        var coExpSubTabView = new CoExpSubTabView();
        coExpSubTabView.init(_genes[0], "GENE");
        }
    }
    
    /**
     * This function gets the gene set profile obtained by the Jquery call and passes it to the Profile selector.
     * After that, it initializes the coExpSubTabView if specified.
     * 
     * @param result
     * @param initCoExpSubTabView Boolean which specifies if initialize the coExpSubTabView.
     * @returns
     */
    function getGenesetsProfileCallback(result, initCoExpSubTabView) {
        var _genesets = window.QuerySession.getQueryGenesets();
        //Set gene set profile variable
        genesetProfile = '';
        //Retrieve the gene set profile
        var _genesetProfile_list = {};
        _.each(_genesets, function(_geneset) {
                _genesetProfile_list = _.extend(_genesetProfile_list, result[_geneset]);
        });
        //Select the profiles that are GSVA scores and discard the profiles with GSVA P-values
        var _genesetScoresProfileList = [];
        $.each(_genesetProfile_list, function(i, obj) {
            if (obj["DATATYPE"] === "GSVA-SCORE") {
                _genesetScoresProfileList.push(obj);
            }
        });
        if (_genesetScoresProfileList.length === 1) { //Assumption: only one GSVA-score per study
                $.each(_genesetScoresProfileList, function(i, obj) {
                        genesetProfile = obj["STABLE_ID"];
                });
        } else  if (_genesetScoresProfileList.length === 0) {
                throw new Error("There are no profiles with GSVA-scores available for this study");
        } else {
                throw new Error("This study contains more than one GSVA-scores profile");
        }
        if (initCoExpSubTabView) {
            var coExpSubTabView = new CoExpSubTabView();
            coExpSubTabView.init(_genesets[0], "GENESET");
        }
    }
    
    /**
     * This function gets the gene set profile from all the profiles of the study.
     * 
     * @param result
     * @returns
     */
    function filterGenesetProfile(result) {
        //Select the profiles that are GSVA scores and discard the profiles with GSVA P-values
        var _genesetScoresProfileList = [];
        $.each(result, function(i, obj) {
            if (obj["DATATYPE"] === "GSVA-SCORE") {
                _genesetScoresProfileList.push(obj);
            }
        });
        if (_genesetScoresProfileList.length === 1) { //Assumption: only one GSVA-score per study
                $.each(_genesetScoresProfileList, function(i, obj) {
                        genesetProfile = obj["STABLE_ID"];
                });
        } else  if (_genesetScoresProfileList.length === 0) {
                throw new Error("There are no profiles with GSVA-scores available for this study");
        } else {
                throw new Error("This study contains more than one GSVA-scores profile");
        }
    }

    return {
        init: function() {
            //Create geneMap and genesetMap
            geneMap = {};
            genesetMap = {};
            entityProfileMap = {};
            //Init Tabs
            Tabs.appendTabsContent();
            Tabs.appendLoadingImgs();
            Tabs.generateTabs();
            Tabs.bindListenerToTabs();
            //Get all the genetic profiles with data available 
            if (window.QuerySession.getQueryGenes()) {
                var paramsGetProfiles = {
                        cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
                        case_set_id: window.QuerySession.getCaseSetId(),
                        case_ids_key: window.QuerySession.getCaseIdsKey(),
                        genetic_entity_list: window.QuerySession.getQueryGenes().join(" "),
                        genetic_entity_type: "GENE"
                };
                $.post("getGeneticProfile.json", paramsGetProfiles, getGeneticProfileCallback, "json");
                if (!(window.QuerySession.getQueryGenesets())) { //We have only genes, retrieve all the profiles to get the GSVA-SCORE profile from the study
                    var paramsGetGeneticProfilesNoGenesets = {
                            cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
                            //case_set_id: "",
                            case_ids_key: window.QuerySession.getCaseIdsKey(),
                            //genetic_entity_list: "",
                            genetic_entity_type: "GENE"
                    };
                    $.post("getGeneticProfile.json", paramsGetGeneticProfilesNoGenesets, function(result){
                        filterGenesetProfile(result, false);
                    }, "json");
                }                
            } else { //We only have query gene sets, retrieve genetic profiles without query genes
                var paramsGetGeneticProfilesNoGenes = {
                        cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
                        //case_set_id: "",
                        case_ids_key: window.QuerySession.getCaseIdsKey(),
                        //genetic_entity_list: "",
                        genetic_entity_type: "GENESET"
                };
                $.post("getGeneticProfile.json", paramsGetGeneticProfilesNoGenes, function(result){
                    getGeneticProfileCallback(result, "GENESET");
                }, "json");
            }
            //Get the gene set profiles
            if (window.QuerySession.getQueryGenesets()) {
                var paramsGetGenesetProfiles = {
                    cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
                    case_set_id: window.QuerySession.getCaseSetId(),
                    case_ids_key: window.QuerySession.getCaseIdsKey(),
                    genetic_entity_list: window.QuerySession.getQueryGenesets().join(" "),
                    genetic_entity_type: "GENESET"
               };
               $.post("getGeneticProfile.json", paramsGetGenesetProfiles, function (result) {
                   //If only query gene sets, initialize the coExpSubTab, otherwise the gene call does it
                   if (!(window.QuerySession.getQueryGenes())) { 
                       getGenesetsProfileCallback(result, true);
                   } else {
                       getGenesetsProfileCallback(result, false);
                   }
               }, "json");
            }
        },
        has_mutation_data: function() {
            return has_mutation_data;
        }
    };

}());    //Closing CoExpView