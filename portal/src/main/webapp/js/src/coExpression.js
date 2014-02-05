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

var CoExpTable = (function() {

    var Names = {
            divPrefix: "coexp_",
            loadingImgPrefix: "coexp_loading_img_",
            tableDivPreFix: "coexp_table_div_",
            tablePrefix: "coexp_table_",
            plotPrefix: "coexp_plot_"
        },
        dim = {
            coexp_table_width: "380px",
            coexp_plots_width: "750px"
        };

    var CoExpTable = (function() {

        function getCoExpData(geneId) {
            var paramsGetCoExpData = {
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene: geneId,
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey(),
                is_full_result: "false"
            };
            $.post("getCoExp.do", paramsGetCoExpData, getCoExpDataCallBack(geneId), "json");
        }

        function getCoExpDataCallBack(geneId) {
            return function(result) {
                if (_.isEmpty(result)) {
                    $('#tabs').tabs("disable", "coexp");
                } else {
                     //figure out div id
                    var divId = Names.divPrefix + geneId;
                    var loadingImgId = Names.loadingImgPrefix + geneId;
                    var tableId = Names.tablePrefix + geneId;
                    var tableDivId = Names.tableDivPreFix + geneId;
                    var plotId = Names.plotPrefix + geneId;

                    $("#" + loadingImgId).hide();
                    $("#" + divId).append(
                        "<table>" +
                        "<tr>" +
                        "<td width='" + dim.coexp_table_width + "' valign='top'><div id='" + tableDivId + "'></div></td>" +
                        "<td width='" + dim.coexp_plots_width + "' valign='top'><div id='" + plotId + "'></div></td>" +
                        "</tr>" +
                        "</table>");

                    $("#" + tableDivId).addClass("coexp-table");
                    $("#" + tableDivId).addClass("coexp-plots");
                    $("#" + tableDivId).append("<table id='" + tableId + "' cellpadding='0' cellspacing='0' border='0' class='display'></table>");

                    var downloadFullResultForm = "<form style='float:right;' action='getCoExp.do' method='post'>" +
                        "<input type='hidden' name='cancer_study_id' value='" + window.PortalGlobals.getCancerStudyId() + "'>" +
                        "<input type='hidden' name='gene' value='" + geneId + "'>" +
                        "<input type='hidden' name='case_set_id' value='" + window.PortalGlobals.getCaseSetId() + "'>" +
                        "<input type='hidden' name='case_ids_key' value='" + window.PortalGlobals.getCaseIdsKey() + "'>" +
                        "<input type='hidden' name='is_full_result' value='true'>" +
                        "<input type='submit' value='Download Full Results'></form>";
                    $("#" + tableDivId).append(downloadFullResultForm);

                    $("#" + tableId).append(
                        "<thead style='font-size:70%;' >" +
                        "<tr>" + 
                        "<th>Correlated Gene</th>" +
                        "<th>Pearson's Correlation</th>" +
                        "<th>Spearman's Correlation</th>" +
                        "</tr>" +
                        "</thead><tbody></tbody>"
                    );

                    var coexp_table_arr = [];
                    $.each(result, function(i, obj) {
                        var tmp_arr = [];
                        tmp_arr.push(obj.gene);
                        tmp_arr.push(obj.pearson.toFixed(2));
                        tmp_arr.push(obj.spearman.toFixed(2));
                        coexp_table_arr.push(tmp_arr);
                    });

                    var _coExpTable = $("#" + tableId).dataTable({
                        "sDom": '<"H"f<"coexp-table-filter-custom">>t<"F"i>',
                        "sPaginationType": "full_numbers",
                        "bJQueryUI": true,
                        "bAutoWidth": false,
                        "aaData" : coexp_table_arr,
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
                        "sScrollY": "550px",
                        "bScrollCollapse": true,
                        iDisplayLength: 250,
                        "oLanguage": {
                            "sSearch": "Search Gene"
                        },
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
                        "bDeferRender": true
                    });  //close data table


                    $("#" + tableDivId).find('.coexp-table-filter-custom').append(
                        "<select id='coexp-table-select'>" +
                        "<option value='all'>Show All</option>" +
                        "<option value='positivePearson'>Show Only Positively Correlated (Pearson's)</option>" +
                        "<option value='negativePearson'>Show Only Negatively Correlated (Pearson's)</option>" +
                        "</select>");
                    $('select#coexp-table-select').change(function () {
                        if ($(this).val() === "negativePearson") {
                            _coExpTable.fnFilter("-", 1, false);
                        } else if ($(this).val() === "positivePearson") {
                            _coExpTable.fnFilter('^[0-9]*\.[0-9]*$', 1, true);
                        } else if ($(this).val() === "all") {
                            _coExpTable.fnFilter("", 1);
                        }
                    });

                    attachRowListener(_coExpTable, tableId, plotId, geneId);

                    //Init with selecting the first row
                    $('#' + tableId + ' tbody tr:eq(0)').click();
                    $('#' + tableId + ' tbody tr:eq(0)').addClass("row_selected");
                }
            }   
        }

        function attachRowListener(_coExpTable, tableId, plotId, geneId, profileDescription) {
            $("#" + tableId + " tbody tr").on('click', function (event) {
                //Highlight selected row
                $(_coExpTable.fnSettings().aoData).each(function (){
                    $(this.nTr).removeClass('row_selected');
                });
                $(event.target.parentNode).addClass('row_selected');

                //Get the gene name of the selected row
                var aData = _coExpTable.fnGetData(this);
                if (null !== aData) {
                    $("#" + plotId).empty();
                    $("#" + plotId).append("<img style='padding:220px;' src='images/ajax-loader.gif'>");
                    CoexpPlots.init(plotId, geneId, aData[0]);
                }
            })
        }

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

        return {
            init: function(geneId) {
                $(window).trigger("resize");
                var element =  document.getElementById(Names.tablePrefix + geneId);
                if (typeof(element) === 'undefined' || element === null) {
                    getCoExpData(geneId);
                }
            }
        }
    }());

    var Tabs = (function() {

        function appendTabsContent() {
            $.each(window.PortalGlobals.getGeneList(), function(index, value) {
                $("#coexp-tabs-list").append("<li><a href='#" + Names.divPrefix + value + "' class='coexp-tabs-ref'><span>" + value + "</span></a></li>");
            });
            $.each(window.PortalGlobals.getGeneList(), function(index, value) {
                $("#coexp-tabs-content").append("<div id='" + Names.divPrefix + value + "'>" +
                    "<div id='" + Names.loadingImgPrefix + value + "'>" +
                    "<img style='padding:20px;' src='images/ajax-loader.gif'>" +
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
                CoExpTable.init(_gene);
            });
        }

        return {
            appendTabsContent: appendTabsContent,
            generateTabs: generateTabs,
            bindListenerToTabs: bindListenerToTabs
        }
    }());

    return {
        initTabs: function() {
            Tabs.appendTabsContent();
            Tabs.generateTabs();
            Tabs.bindListenerToTabs();
        },
        initView: function() {
            var _genes = window.PortalGlobals.getGeneList();
            CoExpTable.init(_genes[0]);
        },
        downloadFullResult: function(_geneId) {
            var paramsGetCoExpData = {
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene: _geneId,
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey(),
                is_full_result: "true"
            };
            $.post("getCoExp.do", paramsGetCoExpData);
        }
    };

}());    //Closing CoExpTable
