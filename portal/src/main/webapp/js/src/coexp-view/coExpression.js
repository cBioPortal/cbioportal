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
 * Render the Co-expression view using dataTable Jquery Plugin
 *
 * User: yichao
 * Date: 12/5/13
 */

var CoExpTable = (function() {

    var Names = {
        divPrefix: "coexp_",
        tablePrefix: "coexp_table_",
        loadingImgPrefix: "coexp_loading_img_",
        plotPrefix: "coexp_plot_"
    };

    var CoExpTable = (function() {

        function getCoExpData(geneId) {
            var paramsGetCoExpData = {
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene: geneId,
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey()
            };
            $.post("getCoExp.do", paramsGetCoExpData, getCoExpDataCallBack(geneId), "json");
        }

        function getCoExpDataCallBack(geneId) {
            return function(result) {
                //figure out div id
                var divId = Names.divPrefix + geneId;
                var tableId = Names.tablePrefix + geneId;
                var loadingImgId = Names.loadingImgPrefix + geneId;

                //Render
                $("#" + loadingImgId).hide();

                $("#" + divId).append(
                    "<table id='" + tableId + "' cellpadding='0' cellspacing='0' border='0' class='display'></table>"
                );
                var description = "Pearson product-moment correlation coefficient, " +
                    "a measure of the degree of linear dependence between two variables, " +
                    "giving a value between +1 and -1 inclusive, where 1 is total positive correlation, " +
                    "0 is no correlation, and -1 is total negative correlation. " +
                    "The scores are ranked by absolute values.";
                $("#" + tableId).append(
                    "<thead style='font-size:70%;' >" +
                    "<tr><th>Correlated/Anti-correlated Genes</th>" +
                    "<th>Pearson's Correction" +
                    "<img class='profile_help' src='images/help.png' title='"+ description + "'></th>" +
                    "<th>Plots</th></tr>" +
                    "</thead><tbody></tbody>"
                );

                jQuery.fn.dataTableExt.oSort['coexp-absolute-value-desc']  = function(a,b) {
                    if (Math.abs(a) > Math.abs(b)) return -1;
                    else if (Math.abs(a) < Math.abs(b)) return 1;
                    else return 0;
                };

                jQuery.fn.dataTableExt.oSort['coexp-absolute-value-asc']  = function(a,b) {
                    if (Math.abs(a) > Math.abs(b)) return 1;
                    else if (Math.abs(a) < Math.abs(b)) return -1;
                    else return 0;
                };

                var _coExpTable = $("#" + tableId).dataTable({
                    "sDom": '<"H"i<"coexp-table-filter-custom">f>t<"F"<"datatable-paging">lp>',
                    "sPaginationType": "full_numbers",
                    "bJQueryUI": true,
                    "bAutoWidth": false,
                    "aaSorting": [[1, 'desc']],
                    "iDisplayLength": 50,
                    "aoColumnDefs": [
                        {
                            "bSearchable": true,
                            "aTargets": [ 0 ]
                        },
                        {
                            "sType": 'coexp-absolute-value',
                            "bSearchable": false,
                            "aTargets": [ 1 ]
                        },
                        {
                            "bSearchable": false,
                            "aTargets": [ 2 ],
                            "bSortable": false,
                            "fnRender": function() {
                                return "<img id='coexp_plot_icon' class='details_close' src='images/details_open.png'>";
                            }
                        }
                    ],
                    "sScrollY": "200px"
                });  //close data table

                $('#coexp_plot_icon').live('click', function () {
                    var nTr = this.parentNode.parentNode;
                    if ( this.src.match('details_close') ) {
                        this.src = "images/details_open.png";
                        _coExpTable.fnClose(nTr);
                    } else {
                        this.src = "images/details_close.png";
                        var aData = _coExpTable.fnGetData( nTr );
                        var _divName = Names.plotPrefix + geneId + "_" + aData[0];
                        _coExpTable.fnOpen(
                            nTr,
                            "<div id='" + _divName + "'>" +
                            "<img style='padding:20px; float: right;' id='" + _divName  + "_loading_img'" +
                            "src='images/ajax-loader.gif'></div>",
                            'coexp-details'
                        );
                        SimplePlot.init(_divName, geneId, aData[0]);
                    }
                } );

                $("#" + divId).find('.coexp-table-filter-custom').append(
                    "<select id='coexp-table-select'>" +
                    "<option value='all'>Show All</option>" +
                    "<option value='positive'>Show Only Positive Correlated</option>" +
                    "<option value='negative'>Show only Negative Correlated</option>" +
                    "</select>");
                $('select#coexp-table-select').change( function () {
                    if ($(this).val() === "negative") {
                        _coExpTable.fnFilter("-", 1, false);
                    } else if ($(this).val() === "positive") {
                        _coExpTable.fnFilter('^[0-9]*\.[0-9]*$', 1, true);
                    } else if ($(this).val() === "all") {
                        _coExpTable.fnFilter("", 1);
                    }
                } );

                attachDataToTable(result, tableId);
            }

        }

        function attachDataToTable(result, tableId) {
            $.each(result, function(i, _obj) {
                $("#" + tableId).dataTable().fnAddData([_obj.gene, _obj.pearson.toFixed(3), "(+)"]);
            });
        }

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
                console.log(_gene);
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
        }
    };

}());    //Closing CoExpTable
