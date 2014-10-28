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

/******************************************************************************************
 * Render Protein Expression jQuery table for "protein change" tab
 * @author JJ, Yichao
 * @date Oct 2014
 ******************************************************************************************/

function renderDataTable(result) {

    var aDataSet = result;

    if (aDataSet.length === 0) return;

    var showPValueColumn = aDataSet[0][9] !== "NaN";
    var showAbsDiffColumn = !showPValueColumn && aDataSet[0][8] !== "NaN";

    var sortingColumn;
    if (showPValueColumn) {
        sortingColumn = [9,'asc'];
    } else if (showAbsDiffColumn) {
        sortingColumn = [8, 'desc'];
    } else if (aDataSet[0][6] !== "NaN") {
        sortingColumn = [6, 'desc'];
    } else {
        sortingColumn = [7, 'desc'];
    }

    var aiExclude = [1,2,3,10];
    var oTable = $('table#protein_expr').dataTable( {
            "sDom": '<"H"<"datatable-filter-custom">fr>t<"F"C<"datatable-paging"pil>>', // selectable columns
            "oColVis": {
                //"aiExclude": aiExclude
            },
            "bJQueryUI": true,
            "bDestroy": true,
            "aaData": aDataSet,
            "aoColumnDefs":[
                { //"sTitle": "RPPA ID",
                  "bVisible": false,
                  "aTargets": [ 0 ]
                },
                { //"sTitle": "Gene",
                  "bVisible": false,
                  "aTargets": [ 1 ]
                },
                { //"sTitle": "Alteration type",
                  "bVisible": false,
                  "aTargets": [ 2 ] 
                },
                { //"sTitle": "Type",
                  "bVisible": false,
                  "aTargets": [ 3 ]
                },
                { //"sTitle": "Target Gene",
                  "mRender": function(data) {
                        return '<b>'+data+'</b>';
                  },
                  "aTargets": [ 4 ] 
                },
                { //"sTitle": "Target Residue",
                  "aTargets": [ 5 ] 
                },
                { //"sTitle": "Ave. Altered<sup>1</sup>",
                  "sType": "num-nan-col",
                  "bSearchable": false,
                  "mRender": function(data) {
                        var value = parseFloat(data);
                        if (isNaN(value))
                            return "NaN";
                        return value.toFixed(2);
                  },
                  "aTargets": [ 6 ]
                },
                { //"sTitle": "Ave. Unaltered<sup>1</sup>",
                  "sType": "num-nan-col",
                  "bSearchable": false,
                  "mRender": function(data) {
                        var value = parseFloat(data);
                        if (isNaN(value))
                            return "NaN";
                        return value.toFixed(2);
                  },
                  "aTargets": [ 7 ]
                },
                { //"sTitle": "abs diff",
                  "bVisible": showAbsDiffColumn,
                  "sType": "num-nan-col",
                  "mRender": function(data, type, full) {
                        var value = parseFloat(data);
                        if (isNaN(value))
                            return "NaN";

                        var ret = value.toFixed(2);

                        var eps = 10e-5;
                        var abunUnaltered = parseFloat(full[6]);
                        var abunAltered = parseFloat(full[7]);

                        if (value<eps)
                            return ret;
                        if (abunUnaltered < abunAltered)
                            return ret + "<img src=\"images/up1.png\"/>";

                        return ret + "<img src=\"images/down1.png\"/>";                                    
                  },
                  "bSearchable": false,
                  "aTargets": [ 8 ]
                },
                { //"sTitle": "p-value",
                  "bVisible": showPValueColumn,
                  "sType": "num-nan-col",
                  "mRender": function(data, display, full) {
                        var value = parseFloat(data);
                        if (isNaN(value))
                            return "NaN";

                        var ret = value < 0.001 ? value.toExponential(2) : value.toFixed(3);
                        if (value <= 0.05)
                            ret = '<b>'+ret+'</b>';

                        var eps = 10e-5;
                        var abunUnaltered = parseFloat(full[6]);
                        var abunAltered = parseFloat(full[7]);

                        if (Math.abs(abunUnaltered-abunAltered)<eps)
                            return ret;
                        if (abunUnaltered < abunAltered)
                            return ret + "<img src=\"images/up1.png\"/>";

                        return ret + "<img src=\"images/down1.png\"/>";                                    
                  },
                  "bSearchable": false,
                  "aTargets": [ 9 ]
                },
                { //"sTitle": "data",
                  "bVisible": false,
                  "bSearchable": false,
                  "bSortable": false,
                  "aTargets": [ 10 ]
                },
                { //"sTitle": "plot",
                  "bSearchable": false,
                  "bSortable": false,
                  "mRender": function(data) {
                        return "<img class=\"details_img\" src=\"images/details_open.png\">";
                  },
                  "aTargets": [ 11 ]

                }
            ],
            "aaSorting": [sortingColumn],
            "oLanguage": {
                "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                "sInfoFiltered": "",
                "sLengthMenu": "Show _MENU_ per page"
            },
            "iDisplayLength": 100,
            "aLengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]]
    } );

    $('.datatable_help').tipTip();

    // remove element from selectable columns - to fix a bug of ColVis
    var excludeButtonRemoved = false;
    $('div.ColVis button.ColVis_Button').click(function() {
        if (!excludeButtonRemoved) {
            for (var i=aiExclude.length-1; i>=0; i--) {
                $('div.ColVis_collection button.ColVis_Button').eq(aiExclude[i]).remove();
            }
            excludeButtonRemoved = true;
        }
    });

    /* Add event listener for opening and closing details
     * Note that the indicator for showing which row is open is not controlled by DataTables,
     * rather it is done here
     */
    $(document).on('click', '.details_img', function () {
        var nTr = this.parentNode.parentNode;
        if ( this.src.match('details_close') ) {
            /* This row is already open - close it */
            this.src = "images/details_open.png";
            oTable.fnClose( nTr );
        } else {
            /* Open this row */
            this.src = "images/details_close.png";
            $(this).removeClass('p-value-plot-hide').addClass('p-value-plot-show');
            var aData = oTable.fnGetData( nTr );
            //var data = aData[10];
            var antibody = "antibody:" + aData[4].replace(/<[^>]*>/g,"");
            if (aData[5])
                antibody += ' ['+aData[5]+']';
            var xlabel = "Query: ";
            if (aData[1] === "Any")
                xlabel += window.PortalGlobals.getGeneListString();
            else
                xlabel += aData[1];
            var pvalue = parsePValue(aData[9]);
            
            if (!isNaN(pvalue)) {
                xlabel += " (p-value: " + pvalue + ")";
            }
            var ylabel = "RPPA score (" + antibody + ")";

            //Render plots under the expansion button 
            var title = "Boxplots of RPPA data (" + antibody + ") for altered and unaltered cases ";
            var _divName = "rppa-plots-" + aData[4].replace(/<[^>]*>/g,"") + aData[5];
            _divName = _divName.replace(/\//g, "");
            oTable.fnOpen( nTr, "<div id='" + _divName + "'><img style='padding:200px;' src='images/ajax-loader.gif'></div>", 'rppa-details' );
            rppaPlots.init(xlabel, ylabel, title, _divName, getRppaPlotsCaseList(), aData[0], getAlterations()); //aData[0]-->protein array id
        }
    });

    // filter for antibody type
    oTable.fnFilter("phosphorylation",3);
    $('div.datatable-filter-custom').html("Antibody Type: "+
        fnCreateSelect(getProteinArrayTypes(),"array_type_alteration_select","phosphorylation")
        );
    $('select#array_type_alteration_select').change( function () {
            oTable.fnFilter( $(this).val(), 3);
    } );

    $('table#protein_expr').css("width","100%"); // widen the rppa data
    $('div#protein_expr_wait').remove();
    $('table#protein_expr_wrapper').show();
        
}

function parsePValue(str) {
    var value = parseFloat(str);
    if (isNaN(value))
        return "NaN";
    var ret = value < 0.001 ? value.toExponential(2) : value.toFixed(3);
    return ret;
}
    
jQuery.fn.dataTableExt.oSort['num-nan-col-asc']  = function(a,b) {
    var x = parsePValue(a);
    var y = parsePValue(b);
        if (isNaN(x)) {
            return isNaN(y) ? 0 : 1;
        }
        if (isNaN(y))
            return -1;
    return ((x < y) ? -1 : ((x > y) ?  1 : 0));
};

jQuery.fn.dataTableExt.oSort['num-nan-col-desc'] = function(a,b) {
    var x = parsePValue(a);
    var y = parsePValue(b);
        if (isNaN(x)) {
            return isNaN(y) ? 0 : 1;
        }
        if (isNaN(y))
            return -1;
    return ((x < y) ? 1 : ((x > y) ?  -1 : 0));
};

function fnCreateSelect(aData, id, defaultOpt) {
    var r = '<select id="'+id+'">', i, iLen=aData.length;
    for ( i = 0 ; i < iLen ; i++ )
    {
        if (defaultOpt !== null && aData[i] === defaultOpt)
            r += '<option value="' + aData[i] + '" selected="selected">' + aData[i] + '</option>';
        else
            r += '<option value="' + aData[i] + '">' + aData[i] + '</option>';
    }
    return r + '</select>';
}

function sortOncoprintData(_raw_result) { //sort the data array by original sample order
    var result = [];
    $.each(window.PortalGlobals.getSampleIds(), function(index, sampleId) {
        $.grep(_raw_result, function( n, i ) {
            if (n.key === sampleId) {
                result.push(n);
            }
        });
    });
    return result;
}

function getProteinArraySignificanceTest(_oncoprintData, _callbackFunc) {
    //Re-generate the heatmap using oncoprintData
    var heatMap = "";
    heatMap = "Case ID" + "\t";
    $.each(window.PortalGlobals.getGeneList(), function(index, val) {
        heatMap += val + "\t";    
    });
    heatMap += "\n";
    $.each(_oncoprintData, function(outer_index, outer_obj) {
        heatMap += outer_obj.key + "\t";
        $.each(outer_obj.values, function(inner_key, inner_obj) {
            if (Object.keys(inner_obj).length === 2) {
                heatMap += "  " + "\t";
            } else {
                if (inner_obj.hasOwnProperty("mutation")) {
                    heatMap += "MUT;";
                }
                if (inner_obj.hasOwnProperty("cna")) {
                    if (inner_obj.cna === "AMPLIFIED") {
                        heatMap += "AMP;";
                    } else if (inner_obj.cna === "GAINED") {
                        heatMap += "GAIN;";
                    } else if (inner_obj.cna === "HEMIZYGOUSLYDELETED") {
                        heatMap += "HETLOSS;";
                    } else if (inner_obj.cna === "HOMODELETED") {
                        heatMap += "HOMDEL;";
                    }
                }
                if (inner_obj.hasOwnProperty("mrna")) {
                    if (inner_obj.mrna === "UPREGULATED") {
                        heatMap += "UP;";
                    } else if (inner_obj.mrna === "DOWNREGULATED") {
                        heatMap += "DOWN;";
                    }
                }
                if (inner_obj.hasOwnProperty("rppa")) {
                    if (inner_obj.rppa === "UPREGULATED") {
                        heatMap += "RPPA-UP;";
                    } else if (inner_obj.rppa === "DOWNREGULATED") {
                        heatMap += "RPPA-DOWN;";
                    }
                }
                heatMap += "\t";
            }
        });
        heatMap += "\n";
    });

    var params = {
        "cancer_study_id" : window.PortalGlobals.getCancerStudyId(),
        "heat_map": heatMap,
        "gene": "Any",
        "alteration": "Any"
    };
    if (cbio.util.browser.msie) params.data_scale = '100'; //TODO: this is a temporary fix for bug #74

    $.post(
        "ProteinArraySignificanceTest.json", 
        params, 
        _callbackFunc, 
        "json"
    );
}

$(document).ready(function(){

    PortalDataCollManager.subscribeOncoprint(function() {
        var oncoprintData = sortOncoprintData(PortalDataColl.getOncoprintData());
        getProteinArraySignificanceTest(oncoprintData, renderDataTable);
    });

});


