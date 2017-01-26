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
 * Enrichment Analysis Tab data table.
 * @param plot_div: optional parameter, i.e. the div where to add a volcano plot representation of the data.
 If set, a volcano plot is rendered next to the data table.
 */
var enrichmentsTabTable = function(plot_div, minionco_div, loading_div, profile_plot_div) {

    var self = this;
    self.plot_div = plot_div;
    self.minionco_div = minionco_div;
    self.loading_div = loading_div;
    self.profile_plot_div = profile_plot_div;

    var div_id, table_id, data, titles; //titles is formatted string of column names with html markdown in
    var col_index, enrichmentsTableInstance, profile_type, profile_id, table_title, data_type;
    var selected_genes = [];
    var assumeLogSpace=false;

    function configTable() {

        //sortings
        jQuery.fn.dataTableExt.oSort['enrichments-p-value-desc'] = function(a,b) {

            if (profile_type === enrichmentsTabSettings.profile_type.mrna) {
                if (a.indexOf("up1") !== -1) a = a.replace("<img src=\"images/up1.png\"/>",  "");
                if (a.indexOf("down1") !== -1) a = a.replace("<img src=\"images/down1.png\"/>",  "");
                if (b.indexOf("up1") !== -1) b = b.replace("<img src=\"images/up1.png\"/>",  "");
                if (b.indexOf("down1") !== -1) b = b.replace("<img src=\"images/down1.png\"/>",  "");
            }

            if (parseFloat(a) > parseFloat(b)) return -1;
            else if (parseFloat(a) < parseFloat(b)) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['enrichments-p-value-asc'] = function(a,b) {

            if (profile_type === enrichmentsTabSettings.profile_type.mrna) {
                if (a.indexOf("up1") !== -1) a = a.replace("<img src=\"images/up1.png\"/>",  "");
                if (a.indexOf("down1") !== -1) a = a.replace("<img src=\"images/down1.png\"/>",  "");
                if (b.indexOf("up1") !== -1) b = b.replace("<img src=\"images/up1.png\"/>",  "");
                if (b.indexOf("down1") !== -1) b = b.replace("<img src=\"images/down1.png\"/>",  "");
            }

            if (parseFloat(a) > parseFloat(b)) return 1;
            else if (parseFloat(a) < parseFloat(b)) return -1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['enrichments-q-value-desc'] = function(a,b) {

            if (profile_type === enrichmentsTabSettings.profile_type.mrna) {
                if (a.indexOf("up1") !== -1) a = a.replace("<img src=\"images/up1.png\"/>",  "");
                if (a.indexOf("down1") !== -1) a = a.replace("<img src=\"images/down1.png\"/>",  "");
                if (b.indexOf("up1") !== -1) b = b.replace("<img src=\"images/up1.png\"/>",  "");
                if (b.indexOf("down1") !== -1) b = b.replace("<img src=\"images/down1.png\"/>",  "");
            }

            if (parseFloat(a) > parseFloat(b)) return -1;
            else if (parseFloat(a) < parseFloat(b)) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['enrichments-q-value-asc'] = function(a,b) {

            if (profile_type === enrichmentsTabSettings.profile_type.mrna) {
                if (a.indexOf("up1") !== -1) a = a.replace("<img src=\"images/up1.png\"/>",  "");
                if (a.indexOf("down1") !== -1) a = a.replace("<img src=\"images/down1.png\"/>",  "");
                if (b.indexOf("up1") !== -1) b = b.replace("<img src=\"images/up1.png\"/>",  "");
                if (b.indexOf("down1") !== -1) b = b.replace("<img src=\"images/down1.png\"/>",  "");
            }

            if (parseFloat(a) > parseFloat(b)) return 1;
            else if (parseFloat(a) < parseFloat(b)) return -1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['enrichments-log-ratio-desc'] = function(a,b) {
            if (a === "<-10") { a = -11; }
            if (b === "<-10") { b = -11; }
            if (a === ">10") { a = 11; }
            if (b === ">10") { b = 11; }
            if (a < 0 && b < 0) {
                if (Math.abs(a) > Math.abs(b)) return 1;
                else return -1;
            } else {
                if (a > b) return -1;
                else if (a < b) return 1;
                else return 0;
            }
        };
        jQuery.fn.dataTableExt.oSort['enrichments-log-ratio-asc'] = function(a,b) {
            if (a === "<-10") { a = -11; }
            if (b === "<-10") { b = -11; }
            if (a === ">10") { a = 11; }
            if (b === ">10") { b = 11; }
            if (a < 0 && b < 0) {
                if (Math.abs(a) > Math.abs(b)) return -1;
                else return 1;
            } else {
                if (a > b) return 1;
                else if (a < b) return -1;
                else return 0;
            }
        };
        jQuery.fn.dataTableExt.oSort['enrichments-pct-altered-desc'] = function(a,b) {
            a = parseFloat(a.substring(a.indexOf("(") + 1, a.indexOf(")") - 1));
            b = parseFloat(b.substring(b.indexOf("(") + 1, b.indexOf(")") - 1));
            if (a > b) return -1;
            else if (a < b) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['enrichments-pct-altered-asc'] = function(a,b) {
            a = parseFloat(a.substring(a.indexOf("(") + 1, a.indexOf(")") - 1));
            b = parseFloat(b.substring(b.indexOf("(") + 1, b.indexOf(")") - 1));
            if (a > b) return 1;
            else if (a < b) return -1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['enrichments-pct-unaltered-desc'] = function(a,b) {
            a = parseFloat(a.substring(a.indexOf("(") + 1, a.indexOf(")") - 1));
            b = parseFloat(b.substring(b.indexOf("(") + 1, b.indexOf(")") - 1));
            if (a > b) return -1;
            else if (a < b) return 1;
            else return 0;
        };
        jQuery.fn.dataTableExt.oSort['enrichments-pct-unaltered-asc'] = function(a,b) {
            a = parseFloat(a.substring(a.indexOf("(") + 1, a.indexOf(")") - 1));
            b = parseFloat(b.substring(b.indexOf("(") + 1, b.indexOf(")") - 1));
            if (a > b) return 1;
            else if (a < b) return -1;
            else return 0;
        };

        //Draw out the markdown of the datatable
        $("#" + table_id).append(
            "<thead style='font-size:60%;'>" +
            "<tr>" + titles + "</tr>" +
            "</thead><tbody></tbody>"
        );

        //Configure the datatable with  jquery
        enrichmentsTableInstance = $("#" + table_id).dataTable({
            "sDom": "<'H'f<'" + table_id + "_filter'>>t<'F'ip>",
            "bPaginate": true,
            "sPaginationType": "full_numbers",
            "bInfo": true,
            "bJQueryUI": true,
            "bAutoWidth": false,
            "aaData" : data,
            "aaSorting": [[col_index.p_val, 'asc']],
            "bScrollCollapse": true,
            "oLanguage": {
                "sSearch": "Search Gene  "
            },
            "aoColumnDefs": [
                {
                    "aTargets": [col_index.gene],
                    "mDataProp": function (data, type, val) {
                        // for display, add the tags; for other purposes (e.g. filtering), use the data
                        if ( type === 'display' ) {
                            return "<div class='geneCheckboxDiv'>" +
                                "<input type='checkbox' class='" +table_id + enrichmentsTabSettings.postfix.datatable_gene_checkbox_class + "' value='"+ data[col_index.gene] + "'>" +
                                "<span class='selectHighlight_"+table_id+" selectHighlight'>" + data[col_index.gene] + "</span>" +
                                "</div>";
                        }
                        return data[col_index.gene];
                    }
                },
                {
                    "bSearchable": false,
                    "aTargets": [ col_index.cytoband ]
                },
                {
                    "sType": 'enrichments-p-value',
                    "bSearchable": false,
                    "aTargets": [ col_index.p_val ]
                },
                {
                    "sType": 'enrichments-q-value',
                    "bSearchable": false,
                    "aTargets": [ col_index.q_val ]
                },
                {
                    "sType": 'enrichments-log-ratio',
                    "bSearchable": false,
                    "aTargets": [ col_index.log_ratio ]
                },
                {
                    "sType": 'enrichments-pct-altered',
                    "bSearchable": false,
                    "aTargets": [col_index.altered_pct]
                },
                {
                    "sType": 'enrichments-pct-unaltered',
                    "bSearchable": false,
                    "aTargets": [col_index.unaltered_pct]
                }
            ],
            "fnRowCallback": function(nRow, aData) {
                $('td:eq(' + col_index.gene + ')', nRow).css("font-weight", "bold");
                $('td:eq(' + col_index.gene + ')', nRow).css("font-size", "10px");
                $('td:eq(' + col_index.log_ratio + ')', nRow).css("font-size", "10px");
                $('td:eq(' + col_index.altered_pct + ')', nRow).css("font-size", "10px");
                $('td:eq(' + col_index.unaltered_pct + ')', nRow).css("font-size", "10px");
                $('td:eq(' + col_index.p_val + ')', nRow).css("font-size", "10px");
                $('td:eq(' + col_index.q_val + ')', nRow).css("font-size", "10px");
                $('td:eq(' + col_index.direction + ')', nRow).css("font-size", "10px");

                if (profile_type === enrichmentsTabSettings.profile_type.copy_num || profile_type === enrichmentsTabSettings.profile_type.mutations) {
                    if (aData[col_index.log_ratio] > 0 || aData[col_index.log_ratio] === ">10") {
                        $('td:eq('+ col_index.log_ratio +')', nRow).css("color", "#3B7C3B");
                    } else if (aData[col_index.log_ratio] < 0 || aData[col_index.log_ratio] === "<-10") {
                        $('td:eq(' + col_index.log_ratio + ')', nRow).css("color", "#B40404");
                    }
                    //bold significant pvalue and qvalue
                    if (aData[col_index.p_val] === "<0.001" ||
                        aData[col_index.p_val] < enrichmentsTabSettings.settings.p_val_threshold) { //significate p value
                        $('td:eq(' + col_index.p_val + ')', nRow).css("font-weight", "bold");
                    }
                    if (aData[col_index.q_val] === "<0.001" ||
                        aData[col_index.q_val] < enrichmentsTabSettings.settings.p_val_threshold) { //significate q value
                        $('td:eq(' + col_index.q_val + ')', nRow).css("font-weight", "bold");
                    }
                } else if (profile_type === enrichmentsTabSettings.profile_type.mrna || profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                    var _p_val = aData[col_index.p_val],
                        _q_val = aData[col_index.q_val];
                    if (_p_val.indexOf("up1") !== -1) _p_val = _p_val.replace("<img src=\"images/up1.png\"/>",  "");
                    if (_p_val.indexOf("down1") !== -1) _p_val = _p_val.replace("<img src=\"images/down1.png\"/>",  "");
                    if (_q_val.indexOf("up1") !== -1) _q_val = _q_val.replace("<img src=\"images/up1.png\"/>",  "");
                    if (_q_val.indexOf("down1") !== -1) _q_val = _q_val.replace("<img src=\"images/down1.png\"/>",  "");

                    if (_p_val === "<0.001" || _p_val < enrichmentsTabSettings.settings.p_val_threshold) {
                        $('td:eq(' + col_index.p_val + ')', nRow).css("font-weight", "bold");
                    }
                    if (_q_val === "<0.001" || _q_val < enrichmentsTabSettings.settings.p_val_threshold) {
                        $('td:eq(' + col_index.q_val + ')', nRow).css("font-weight", "bold");
                    }

                    // Check whether we encounter a negative value. 
                    // If we do, assume data is already in log-space. This is a workaround for the data not
                    // having a descriptive data_type
                    if(Number(aData[col_index.altered_mean])<=0 || Number(aData[col_index.unaltered_mean])<=0){
                        assumeLogSpace=true;
                    }
                }
            },
            "fnDrawCallback": function() {
                activateUpdateQueryBtns(table_id + enrichmentsTabSettings.postfix.datatable_update_query_button);
                activeDownloadBtn();
                addGeneClick();
            },
            "bDeferRender": true,
            "iDisplayLength": 14
        });

    }

    /**
     * when a gene in the table is clicked, show the gene in the mini-onco
     */
    function addGeneClick(){
        $('.selectHighlight_'+table_id).on('click', function() {
            var current_gene = $(this).text();
            var _pValObj = _.filter(self.originalData, function(_dataObj) {return _dataObj.Gene == current_gene; });
            var _pVal = _pValObj[0]["p-Value"];
            if (profile_type === enrichmentsTabSettings.profile_type.mrna || profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                enrichmentsTabPlots.init(self.profile_plot_div, current_gene, profile_type, profile_id, table_title, _pVal);
            } else {
                self.miniOnco.render(current_gene);
            }
        });
    }

    /**
     * show the loading icon and decrease visibility of table
     */
    function startLoading() {
        $("#" + table_id).addClass('tableLoading');
        $('#' + loading_div).addClass('loaderIconLoading');
    }

    /**
     * hide the loading icon and restore the visibility of table
     */
    function stopLoading() {
        $("#" + table_id).removeClass('tableLoading');
        $('#' + loading_div).removeClass('loaderIconLoading');
    }

    /**
     * Search the table by building a regular expression
     * Shows the loading icon while searching
     *
     * @param selection: list of genes to search for
     */
    this.searchTable = function(selection) {
        // set loading icon for table
        startLoading();

        // add timeout to actually show the loading image
        setTimeout(function() {
            var searchExpression = "";

            // if no genes, search will be with an empty filter, otherwise create a (maybe extremely long...) searchExpression
            if (selection.length > 0) {
                searchExpression = "^" + selection.join("$|^") + "$";
            }
            //apply search expression to the dataTable:
            enrichmentsTableInstance.DataTable().column(0).search(
                searchExpression,
                true,
                false
            ).draw();

            // stop loading
            stopLoading();
        }, 1);
    }

    /**
     * check whether the profile_type supports a mini-onco. This is the case for mutations and copy number
     * @returns {boolean}
     */
    this.supportsMiniOnco = function(){
        return profile_type === enrichmentsTabSettings.profile_type.mutations || profile_type === enrichmentsTabSettings.profile_type.copy_num;
    }

    /**
     * check whether the datatype is LOG-VALUE, LOG2-VALUE or whether LOG space is assumed because
     * a negative value was found to prevent logging it again
     * assumeLogSpace is a backwards compatibility for MSK
     * @returns {boolean}
     */
    this.hasLogData = function(){
        return data_type === "LOG-VALUE" || data_type==="LOG2-VALUE" || assumeLogSpace;
    }

    /**
     * return whether log-space was assumed
     * @returns {boolean}
     */
    this.assumedLogSpace = function(){
        return assumeLogSpace && !(data_type === "LOG-VALUE" || data_type==="LOG2-VALUE");
    }

    /**
     * check whether the profile_type requires calculation of the log ratio, which is the case for mRNA and protein expression
     * @returns {boolean}
     */
    this.requiresLogRatioCalculation = function(){
        return profile_type === enrichmentsTabSettings.profile_type.mrna || profile_type === enrichmentsTabSettings.profile_type.protein_exp;
    }

    function attachFilters() {

        if (profile_type === enrichmentsTabSettings.profile_type.copy_num || profile_type === enrichmentsTabSettings.profile_type.mutations) {

            $("#" + div_id).find("." + table_id + "_filter").append(
                "<input type='checkbox' class='" + table_id + "-checkbox' checked id='" + table_id + "-checkbox-mutex'><span style='font-size:10px;'>Mutual exclusivity</span></option> &nbsp;&nbsp;" +
                "<input type='checkbox' class='" + table_id + "-checkbox' checked id='" + table_id + "-checkbox-co-oc'><span style='font-size:10px;'>Co-occurrence</span></option> &nbsp;&nbsp;" +
                "<input type='checkbox' class='" + table_id + "-checkbox' id='" + table_id + "-checkbox-sig-only'><span style='font-size:10px;'>Significant gene(s)</span></option> &nbsp; &nbsp;"
            );

            var _sig_only_all_fn = function() {
                    enrichmentsTableInstance.fnFilter("", col_index.log_ratio);
                    enrichmentsTableInstance.fnFilter("", col_index.direction);
                    enrichmentsTableInstance.fnFilter("Significant", col_index.direction);
                },
                _sig_only_mutex_fn = function() {
                    enrichmentsTableInstance.fnFilter("", col_index.direction);
                    enrichmentsTableInstance.fnFilter("", col_index.log_ratio);
                    enrichmentsTableInstance.fnFilter("^(.)*mutual(.)*Significant$", col_index.direction, true);
                },
                _sig_only_co_oc_fn = function() {
                    enrichmentsTableInstance.fnFilter("", col_index.direction);
                    enrichmentsTableInstance.fnFilter("", col_index.log_ratio);
                    enrichmentsTableInstance.fnFilter("^(.)*occurrence(.)*Significant$", col_index.direction, true);
                },
                _mutex_fn = function() {
                    enrichmentsTableInstance.fnFilter("", col_index.direction);
                    enrichmentsTableInstance.fnFilter("", col_index.log_ratio);
                    enrichmentsTableInstance.fnFilter("^(.)*mutual(.)*$", col_index.direction, true);
                },
                _co_oc_fn = function() {
                    enrichmentsTableInstance.fnFilter("", col_index.direction);
                    enrichmentsTableInstance.fnFilter("", col_index.log_ratio);
                    enrichmentsTableInstance.fnFilter("^(.)*occurrence(.)*$", col_index.direction, true);
                },
                _all_fn = function() {
                    enrichmentsTableInstance.fnFilter("", col_index.direction);
                    enrichmentsTableInstance.fnFilter("", col_index.log_ratio);
                },
                _empty_fn = function() {
                    enrichmentsTableInstance.fnFilter("&", col_index.log_ratio);
                };

            $("." + table_id + "-checkbox").change(function () {

                var _mutex_checked = false,
                    _co_oc_checked = false,
                    _sig_checked = false;

                if ($("#" + table_id + "-checkbox-sig-only").is(':checked')) _sig_checked = true;
                if ($("#" + table_id + "-checkbox-mutex").is(':checked')) _mutex_checked = true;
                if ($("#" + table_id + "-checkbox-co-oc").is(':checked')) _co_oc_checked = true;

                if (_mutex_checked && _co_oc_checked) {
                    if (_sig_checked) _sig_only_all_fn();
                    else _all_fn();
                } else if (_mutex_checked && !_co_oc_checked) {
                    if (_sig_checked) _sig_only_mutex_fn();
                    else _mutex_fn();
                } else if (!_mutex_checked && _co_oc_checked) {
                    if (_sig_checked) _sig_only_co_oc_fn();
                    else _co_oc_fn();
                } else {
                    _empty_fn();
                }

                if(self.volcanoPlot) {
                    //Selected data items remaining after filter:
                    var remainingItems = enrichmentsTableInstance.DataTable().rows({search: 'applied'}).data();
                    var remainingGenes = [];
                    for (var i = 0; i < remainingItems.length; i++) {
                        remainingGenes.push(remainingItems[i][0]);
                    }
                    self.volcanoPlot.selectItems(remainingGenes);
                }
            });
        }

    }

    function addHeaderQtips() {
        $("#" + table_id + enrichmentsTabSettings._title_ids.gene).qtip({
            content: { text:"Select gene(s) you are interested and click 'add to query' button to re-query alone with the new genes."},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + enrichmentsTabSettings._title_ids.pct).qtip({
            content: { text:"Percentages of altered cases in altered or unaltered sample groups"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + enrichmentsTabSettings._title_ids.log_ratio).qtip({
            content: { text:"Log2 based ratio of (pct in altered / pct in unaltered)"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + enrichmentsTabSettings._title_ids.p_val).qtip({
            content: { text:"Derived from Fisher Exact Test"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + enrichmentsTabSettings._title_ids.q_val).qtip({
            content: { text:"Derived from Benjamini-Hochberg procedure"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + enrichmentsTabSettings._title_ids.direction).qtip({
            content: { text:'Log odds ratio > 0&nbsp;&nbsp;&nbsp;: Enriched in altered group<br>' +
            'Log odds ratio <= 0&nbsp;: Enriched in unaltered group<br>' +
            'p-Value < 0.05&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: Significant association'},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + enrichmentsTabSettings._title_ids.mean_alt).qtip({
            content: { text:"Mean of expression values in altered or unaltered group"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + enrichmentsTabSettings._title_ids.stdev_alt).qtip({
            content: { text:"Standard Deviation in altered or unaltered group"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
        $("#" + table_id + enrichmentsTabSettings._title_ids.p_val_t_test).qtip({
            content: { text:"Derived from Student T-test"},
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow qtip-ui-wide'},
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left bottom',at:'top right',viewport: $(window)}
        });
    }

    function activateUpdateQueryBtns(btn_id) {

        //document.getElementsByClassName(table_id + enrichmentsTabSettings.postfix.datatable_gene_checkbox_class).removeEventListener("click");
        $("." + table_id + enrichmentsTabSettings.postfix.datatable_gene_checkbox_class).unbind("click");

        //attach event listener to checkboxes
        $("." + table_id + enrichmentsTabSettings.postfix.datatable_gene_checkbox_class).click(function() {

            $("#" + table_id + enrichmentsTabSettings.postfix.update_query_gene_list).empty();
            if ($(this).is(':checked')) {
                selected_genes.push($(this).attr("value"));
            } else {
                selected_genes.splice($.inArray($(this).attr("value"), selected_genes), 1);
            }
            $("#" + table_id + enrichmentsTabSettings.postfix.update_query_gene_list).append("&nbsp;&nbsp;Selected genes: " + selected_genes.join(", "));

            if (selected_genes.length !== 0) {
                document.getElementById(btn_id).disabled = false;
            } else {
                document.getElementById(btn_id).disabled = true;
            }
            //If there is a plot, update plot to reflect selection:
            if (self.plot_div != null) {
                self.volcanoPlot.specialSelectItems(selected_genes);
            }
        });

        $("#" + btn_id).click(function() {

            if (window.QuerySession.getCaseSetId() !== "-1") {
                var _start_pos_gene_list = document.URL.indexOf("gene_list=") + "gene_list=".length;
                var _end_pos_gene_list = document.URL.indexOf("&", _start_pos_gene_list);
                var pre_gene_list = document.URL.substring(0, _start_pos_gene_list);
                var post_gene_list = document.URL.substring(_end_pos_gene_list);
                var new_gene_list = encodeURIComponent(window.QuerySession.getOQLQuery() + "\n" + selected_genes.join("\n"));
                var _new_url = pre_gene_list + new_gene_list + post_gene_list;
                window.location.replace(_new_url);
            } else {
                var _original_url = document.URL.substring(0, document.URL.indexOf("index.do") + ("index.do").length);

                //genetic profiles separate
                var _tmp_profile_id_list = "";
                $.each(window.QuerySession.getGeneticProfileIds(), function(index, _profile_id) {
                    _tmp_profile_id_list += "genetic_profile_ids=" + _profile_id + "&";
                });
                var _new_url = _original_url.concat(
                    "?" + "tab_index=tab_visualize" + "&" +
                    "cancer_study_id=" + window.QuerySession.getCancerStudyIds()[0] + "&" +
                    _tmp_profile_id_list +
                    "gene_list=" + window.QuerySession.getOQLQuery() + encodeURIComponent("\n") + selected_genes.join(encodeURIComponent("\n")) + "&" +
                    "case_set_id=" + window.QuerySession.getCaseSetId() + "&" +
                    "case_ids_key=" + window.QuerySession.getCaseIdsKey() + "&" +
                    "Action=Submit"
                );
                if (selected_genes.length !== 0) {
                    window.location.replace(_new_url);
                }
            }
        });
    }

    function activeDownloadBtn() {
        $("#" + table_id + "_download_btn").click(function() {
            cbio.download.clientSideDownload([get_tab_delimited_data()], "enrichments-analysis-result.txt");
        });
    }

    function get_tab_delimited_data() {
        var result_str = "";
        //column headers
        if (profile_type === enrichmentsTabSettings.profile_type.copy_num || profile_type === enrichmentsTabSettings.profile_type.mutations) {
            result_str = "Gene\tCytoband\tPercentage of alteration(Altered)\tPercentage of Alteration(Unaltered)\t" +
                "Log ratio\tp-Value\tq-Value\tDirection/Tendency\n";
        } else if (profile_type === enrichmentsTabSettings.profile_type.mrna) {
            result_str = "Gene\tCytoband\tMean mRNA expression(Altered)\tMean mRNA expression(Unaltered)\t" +
                "Standard deviation of mRNA expression(Altered)\tStandard deviation of mRNA expression(Unaltered)\t" +
                "p-Value\tq-Value\n";
        } else if (profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
            result_str = "Gene\tCytoband\tMean protein expression(Altered)\tMean protein expression(Unaltered)\t" +
                "Standard deviation of protein expression(Altered)\tStandard deviation of protein expression(Unaltered)\t" +
                "p-Value\tq-Value\n";
        }
        //content
        _.each(data, function(entry) {
            _.each(entry, function(column) {
                if (column !== null) {
                    if (column.indexOf("<input type='checkbox'") !== -1) {
                        column = column.substring(column.indexOf(">") + 1);
                    } else {
                        column = column.replace("<img src=\"images/up1.png\"/>", "");
                        column = column.replace("<img src=\"images/down1.png\"/>", "");
                        column = column.replace("&nbsp;&nbsp;&nbsp;<span class='label label-or-analysis-significant'>", "<");
                        column = column.replace("</span>", ">");
                    }
                    result_str += column + "\t";
                } else {
                    result_str += "NaN" + "\t";
                }
            });
            result_str += "\n";
        });

        return result_str;
    }

    function define_titles() {

        var _title_str = "";

        if (profile_type === enrichmentsTabSettings.profile_type.copy_num) {
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.gene + "'>Gene</th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.cytoband + "'>Cytoband</th>";
            _title_str += "<th colspan='2'>Percentage of alteration &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.pct + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.log_ratio + "'>Log Ratio &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.log_ratio + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.p_val + "'>p-Value &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.p_val + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.q_val + "'>q-Value &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.q_val + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.direction + "'>Direction/Tendency &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.direction + "'></th>";
            _title_str += "</tr><tr><th width='" + enrichmentsTabSettings.col_width.altered_pct + "'>in altered group</th>";
            _title_str += "<th width='" + enrichmentsTabSettings.col_width.unaltered_pct + "'>in unaltered group</th>";
        } else if (profile_type === enrichmentsTabSettings.profile_type.mutations) {
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.gene + "'>Gene</th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.cytoband + "'>Cytoband</th>";
            _title_str += "<th colspan='2'>Percentage of alteration &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.pct + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.log_ratio + "'>Log Ratio &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.log_ratio + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.p_val + "'>p-Value &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.p_val + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.q_val + "'>q-Value &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.q_val + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.direction + "'>Direction/Tendency &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.direction + "'></th>";
            _title_str += "</tr><tr><th width='" + enrichmentsTabSettings.col_width.altered_pct + "'>in altered group</th>";
            _title_str += "<th width='" + enrichmentsTabSettings.col_width.unaltered_pct + "'>in unaltered group</th>";
        } else if (profile_type === enrichmentsTabSettings.profile_type.mrna) {
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.gene + "'>Gene</th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.cytoband + "'>Cytoband</th>";
            _title_str += "<th colspan='2'>Mean mRNA expression &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.mean_alt + "'></th>";
            _title_str += "<th colspan='2'>Standard deviation of mRNA expression &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.stdev_alt + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.p_val + "'>p-Value &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.p_val_t_test + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.q_val + "'>q-Value &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.q_val + "'></th>";
            _title_str += "</tr><tr>";
            _title_str += "<th width='100'>in altered group</th>";
            _title_str += "<th width='100'>in unaltered group</th>";
            _title_str += "<th width='100'>in altered group</th>";
            _title_str += "<th width='100'>in unaltered group</th>";
        } else if (profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.gene + "'>Gene</th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.cytoband + "'>Cytoband</th>";
            _title_str += "<th colspan='2'>Mean protein expression &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.mean_alt + "'></th>";
            _title_str += "<th colspan='2'>Standard deviation of protein expression &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.stdev_alt + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.p_val + "'>p-Value &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.p_val_t_test + "'></th>";
            _title_str += "<th rowspan='2' width='" + enrichmentsTabSettings.col_width.q_val + "'>q-Value &nbsp;<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.q_val + "'></th>";
            _title_str += "</tr><tr>";
            _title_str += "<th width='100'>in altered group</th>";
            _title_str += "<th width='100'>in unaltered group</th>";
            _title_str += "<th width='100'>in altered group</th>";
            _title_str += "<th width='100'>in unaltered group</th>";
        }

        return _title_str;
    }

    return {
        /**
         * Init function to bind data to table and optionally to the plot and render it.
         *
         * @param originalData : the original (json) data as received from enrichmentsTabData.get() function
         * @param _converted_data : the originalData converted to array format, compatible with the table render function
         */
        init: function(originalData, _converted_data, _div_id, _table_div, _table_id, _table_title, _profile_type, _profile_id, _last_profile, _data_type) {

            self.originalData = originalData;
            if (Object.keys(_converted_data).length !== 0 &&
                Object.keys(_converted_data)[0] !== enrichmentsTabSettings.texts.null_result &&
                Object.keys(_converted_data)[0] !== "") {

                div_id = _div_id;
                table_id = _table_id;
                data = _converted_data;
                profile_type = _profile_type;
                profile_id = _profile_id;
                table_title = _table_title;
                data_type = _data_type;

                $("#" + div_id + "_loading_img").empty();

                if (profile_type === enrichmentsTabSettings.profile_type.copy_num) {
                    col_index = enrichmentsTabSettings.col_index.copy_num;
                } else if (profile_type === enrichmentsTabSettings.profile_type.mutations) {
                    col_index = enrichmentsTabSettings.col_index.mutations;
                } else if (profile_type === enrichmentsTabSettings.profile_type.mrna) {
                    col_index = enrichmentsTabSettings.col_index.mrna;
                } else if (profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                    col_index = enrichmentsTabSettings.col_index.protein_exp;
                }

                titles = define_titles();

                $("#" + _table_div).empty();
                $("#" + _table_div).append("<span style='font-weight:bold;'>" + _table_title +
                    "</span>&nbsp;&nbsp;&nbsp;&nbsp;<button type='button' id='" + table_id +
                    enrichmentsTabSettings.postfix.datatable_update_query_button + "'>Add checked genes to query" +
                    "</button>" +
                    "<img class='help-img-icon' src='" + enrichmentsTabSettings.settings.help_icon_img_src + "' id='" + table_id + enrichmentsTabSettings._title_ids.gene + "'>" +
                    "<span id='" + table_id + enrichmentsTabSettings.postfix.update_query_gene_list + "'></span>");
                document.getElementById(table_id + enrichmentsTabSettings.postfix.datatable_update_query_button).disabled = true;

                $("#" + _table_div).append("<table id='" + table_id + "' cellpadding='0' cellspacing='0' border='0' class='" + table_id + "_datatable_class'></table>");
                $("#" + _table_div).append("<button id='" + table_id + "_download_btn'>Download Complete Result</button>");
                configTable();
                attachFilters();
                addHeaderQtips();

            } else {
                if (_profile_type === enrichmentsTabSettings.profile_type.mrna) {
                    $("#" + _table_div).empty();
                    $("#" + _table_div).append("No data/result available");
                } else {
                    $("#" + _table_div).remove();
                    if (_last_profile) {
                        $("#" + _div_id).empty();
                        $("#" + _div_id).append("No result available.");
                    }
                }
            }
            //if plot_div is set, add a volcano plot:
            if (self.plot_div != null) {
                if (_profile_type === enrichmentsTabSettings.profile_type.mutations || _profile_type === enrichmentsTabSettings.profile_type.copy_num) {
                    // create mini onco and render it
                    self.miniOnco = new MiniOnco(self.plot_div, minionco_div, originalData);
                    self.miniOnco.render("none");
                }

                // create volcanoplot and render it
                self.volcanoPlot = new VolcanoPlot();
                self.volcanoPlot.render(self);
            }
        }
    };
}; //close enrichmentsTabTable

var orSubTabView = function() {

    return {
        init: function(_div_id, _profile_list, _profile_type, _gene_set) {

            //for protein_exp and mrna sub tab, there is an EXTRA dropdown menu for selecting profiles
            //order profiles by priority list -- swap the rna seq profile to the top
            $.each(_profile_list, function(i, obj) {
                if (obj.STABLE_ID.indexOf("rna_seq") !== -1) {
                    cbio.util.swapElement(_profile_list, i, 0);
                }
                if (obj.STABLE_ID.indexOf("ms_abundance") !== -1) {
                    cbio.util.swapElement(_profile_list, i, 0);
                }
            });

            $("#" + _div_id).css("padding-right","10px");

            // add drop down menu for selecting profiles under mrna sub tab and protein sub tab
            if (_profile_type === enrichmentsTabSettings.profile_type.mrna ) {
                addDropDownMenu(enrichmentsTabSettings.postfix.mrna_sub_tab_profile_selection_dropdown_menu);
            } else if (_profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                addDropDownMenu(enrichmentsTabSettings.postfix.protein_exp_sub_tab_profile_selection_dropdown_menu);
            }
            function addDropDownMenu (_elemId) {
                $("#" + _div_id + "_loading_img").empty();
                $("#" + _div_id).append("<div id='" + _div_id + _elemId + "_div'></div>");
                if ($("#" + _div_id + _elemId + "_div").is(":empty")) {
                    $("#" + _div_id + _elemId + "_div").append(
                        "Data Set <select id='" + _div_id + _elemId + "'></select>"
                    );
                }
                $.each(_profile_list, function(_index, _profile_obj) {
                    $("#" + _div_id + _elemId).append(
                        "<option value='" + _profile_obj.STABLE_ID + "'>" + _profile_obj.NAME + "</option>"
                    );
                });
                $("#" + _div_id + _elemId).change(function() {
                    var selected_profile_id = $( "#" + _div_id + _elemId).val();
                    $.each(_profile_list, function(_index, _profile_obj) {
                        var usableId = _profile_obj.STABLE_ID.replace(/\./g, "_");
                        if (_profile_obj.STABLE_ID !== selected_profile_id) {
                            $("#" + usableId + "_container_table").hide();
                        } else {
                            $("#" + usableId + "_container_table").show();
                        }
                    });
                });
                //adding loading image for table
                $("#" + _div_id).append("<div id='" + _div_id + "_table_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif' alt='loading' /></div>");
            }
            
            $.each(_profile_list, function(_index, _profile_obj) {

                var element = $("." + _profile_obj.STABLE_ID.replace(/\./g, "_") + enrichmentsTabSettings.postfix.datatable_class);
                if (element.length === 0) { //avoid duplicated initiation
                    var _table_div = _profile_obj.STABLE_ID.replace(/\./g, "_") + enrichmentsTabSettings.postfix.datatable_div;
                    var _table_id = _profile_obj.STABLE_ID.replace(/\./g, "_") + enrichmentsTabSettings.postfix.datatable_id;
                    var _plot_div = _profile_obj.STABLE_ID.replace(/\./g, "_") + enrichmentsTabSettings.postfix.plot_div;
                    var _profile_plot_div = _profile_obj.STABLE_ID.replace(/\./g, "_") + enrichmentsTabSettings.postfix.profile_plot_div;
                    var loading_div = _table_div + "_loading_img";
                    var minionco_div = "minionco" + _plot_div; //for the mini-onco diagram

                    var html = "<div id='" + _profile_obj.STABLE_ID.replace(/\./g, "_") + "_container' style='float: left; position: relative; width: 100%;'>"+
                        "<table width='100%' id='" + _profile_obj.STABLE_ID.replace(/\./g, "_") + "_container_table'><tr>" +
                        "<td width='40%'>" +
                        "<div id='" + _plot_div + "' style='position: absolute; top: 5px; float: left;'></div>" +
                        "<div id='" + _profile_plot_div + "' style='position: relative; top:200px; float: left;'></div>" +
                        "</td>" +
                        "<td width='60%'>" +
                        "<div id='" + _table_div + "' style='float:right;'></div>" +
                        "</td>" +
                        "</tr></table>" +
                        "<div id='" + loading_div + "' class='loaderIcon'><img src='images/ajax-loader.gif' alt='loading' /></div>"+
                        "</div>";
                    $("#" + _div_id).append(html);
                    //adding this to contain floated plot (see "float: left"  above):
                    $("#" + _div_id).css("overflow", "hidden");

                    // default message for profile plots
                    if (_profile_type === enrichmentsTabSettings.profile_type.mrna || _profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                        $("#" + _profile_plot_div).append("<span style='margin-left: 55px; color: darkgray;'><b>Click on a gene in table to render plots here. </b></span>");
                    }

                    //if this is the last profile
                    var last_profile = false;
                    if (_index + 1 === _profile_list.length) { //if it's last table, the instance would also in charge of clear the loading img and put an error msg
                        last_profile = true;
                    } else {
                        last_profile = false;
                    }

                    //init and get calculation result from the server
                    var param = new orAjaxParam(enrichmentsTab.getAlteredCaseList(), enrichmentsTab.getUnalteredCaseList(), _profile_obj.STABLE_ID, _gene_set);
                    var or_data = new enrichmentsTabData();
                    or_data.init(param, _table_id);
                    var or_table = new enrichmentsTabTable(_plot_div, minionco_div, loading_div, _profile_plot_div);
                    if (_profile_obj.STABLE_ID.indexOf("rna_seq") !== -1) {
                        or_data.get(or_table.init, _div_id, _table_div, _table_id, _profile_obj.NAME + enrichmentsTabSettings.postfix.title_log, _profile_type, _profile_obj.STABLE_ID.replace(/\./g, "_"), last_profile,_profile_obj.DATATYPE);
                    } else {
                        or_data.get(or_table.init, _div_id, _table_div, _table_id, _profile_obj.NAME, _profile_type, _profile_obj.STABLE_ID.replace(/\./g, "_"), last_profile, _profile_obj.DATATYPE);
                    }

                    $("#" + loading_div).empty();

                    // hide mrna and protein tables initially
                    if (_profile_type === enrichmentsTabSettings.profile_type.mrna || _profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                        $("#" + _profile_obj.STABLE_ID.replace(/\./g, "_") + "_container_table").hide();
                    }
                }

            });

            //show mrna or protein exp table that's being selected
            if (_profile_type === enrichmentsTabSettings.profile_type.mrna) {
                showSelectedTable(enrichmentsTabSettings.postfix.mrna_sub_tab_profile_selection_dropdown_menu);
            } else if (_profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                showSelectedTable(enrichmentsTabSettings.postfix.protein_exp_sub_tab_profile_selection_dropdown_menu);
            }
            function showSelectedTable(_elemId) {
                var tmp = setInterval(function () { timer(); }, 1000);
                function timer() {
                    var selectedVal = $("#" + _div_id + _elemId).val().replace(/\./g, "_");
                    var _target_table_div = selectedVal + enrichmentsTabSettings.postfix.datatable_div;
                    if (!$( "#" + _target_table_div).is(":empty")) {
                        clearInterval(tmp);
                        $("#" + _div_id + "_table_loading_img").empty();
                        $("#" + selectedVal + "_container_table").show();
                    }
                }
            }

        }
    };
}; //close orSubTabView









