<%@ page import="org.mskcc.portal.servlet.ProteinArraySignificanceTestJSON" %>

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .dataTables_length {
                width: auto;
        }
        td.rppa-details {
                background-color : white;
        }
</style>

<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.ColVis.min.js"></script> 

<script type="text/javascript">
    jQuery.fn.dataTableExt.oSort['num-nan-col-asc']  = function(a,b) {
	var x = parseFloat(a);
	var y = parseFloat(b);
        if (isNaN(x)) {
            return isNaN(y) ? 0 : 1;
        }
        if (isNaN(y))
            return -1;
	return ((x < y) ? -1 : ((x > y) ?  1 : 0));
    };

    jQuery.fn.dataTableExt.oSort['num-nan-col-desc'] = function(a,b) {
	var x = parseFloat(a);
	var y = parseFloat(b);
        if (isNaN(x)) {
            return isNaN(y) ? 0 : 1;
        }
        if (isNaN(y))
            return -1;
	return ((x < y) ? 1 : ((x > y) ?  -1 : 0));
    };
    
    (function($) {
    /*
     * Function: fnGetColumnData
     * Purpose:  Return an array of table values from a particular column.
     * Returns:  array string: 1d data array 
     * Inputs:   object:oSettings - dataTable settings object. This is always the last argument past to the function
     *           int:iColumn - the id of the column to extract the data from
     *           bool:bUnique - optional - if set to false duplicated values are not filtered out
     *           bool:bFiltered - optional - if set to false all the table data is used (not only the filtered)
     *           bool:bIgnoreEmpty - optional - if set to false empty values are not filtered from the result array
     * Author:   Benedikt Forchhammer <b.forchhammer /AT\ mind2.de>
     */
    $.fn.dataTableExt.oApi.fnGetColumnData = function ( oSettings, iColumn, bUnique, bFiltered, bIgnoreEmpty ) {
            // check that we have a column id
            if ( typeof iColumn == "undefined" ) return new Array();

            // by default we only wany unique data
            if ( typeof bUnique == "undefined" ) bUnique = true;

            // by default we do want to only look at filtered data
            if ( typeof bFiltered == "undefined" ) bFiltered = true;

            // by default we do not wany to include empty values
            if ( typeof bIgnoreEmpty == "undefined" ) bIgnoreEmpty = true;

            // list of rows which we're going to loop through
            var aiRows;

            // use only filtered rows
            if (bFiltered == true) aiRows = oSettings.aiDisplay; 
            // use all rows
            else aiRows = oSettings.aiDisplayMaster; // all row numbers

            // set up data array	
            var asResultData = new Array();

            for (var i=0,c=aiRows.length; i<c; i++) {
                    iRow = aiRows[i];
                    var aData = this.fnGetData(iRow);
                    var sValue = aData[iColumn];

                    // ignore empty values?
                    if (bIgnoreEmpty == true && sValue.length == 0) continue;

                    // ignore unique values?
                    else if (bUnique == true && jQuery.inArray(sValue, asResultData) > -1) continue;

                    // else push the value onto the result data array
                    else asResultData.push(sValue);
            }

            return asResultData;
    }}(jQuery));

    function fnCreateSelect(aData, id, defaultOpt)
    {
            var r='<select id="'+id+'">', i, iLen=aData.length;
            for ( i=0 ; i<iLen ; i++ )
            {
                if (defaultOpt!=null && aData[i]==defaultOpt)
                    r += '<option value="'+aData[i]+'" selected="selected">'+aData[i]+'</option>';
                else
                    r += '<option value="'+aData[i]+'">'+aData[i]+'</option>';
            }
            return r+'</select>';
    }
    
    $(document).ready(function(){
        $('table#protein_expr_wrapper').hide();
        var params = {<%=ProteinArraySignificanceTestJSON.HEAT_MAP%>:$("textarea#heat_map").html(),
            <%=ProteinArraySignificanceTestJSON.GENE%>:'Any',
            <%=ProteinArraySignificanceTestJSON.ALTERATION_TYPE%>:'Any'
        };
        $.post("ProteinArraySignificanceTest.json", 
            params,
            function(aDataSet){
                //$("div#protein_exp").html(aDataSet);
                //alert(aDataSet);
                var aiExclude = [0,1,2,10];
                var oTable = $('table#protein_expr').dataTable( {
                        "sDom": '<"H"Cfr>t<"F"lip>', // selectable columns
			"oColVis": {
                            //"aiExclude": aiExclude
                        },
                        "bJQueryUI": true,
                        "aaData": aDataSet,
                        "aoColumnDefs":[
                            { //"sTitle": "Gene",
                              "bVisible": false,
                              "aTargets": [ 0 ]
                            },
                            { //"sTitle": "Alteration type",
                              "bVisible": false,
                              "aTargets": [ 1 ] 
                            },
                            { //"sTitle": "Type",
                              "bVisible": false,
                              "aTargets": [ 2 ]
                            },
                            { //"sTitle": "Target Gene",
                              "aTargets": [ 3 ] 
                            },
                            { //"sTitle": "Target Residue",
                              "aTargets": [ 4 ] 
                            },
                            { //"sTitle": "Source organism",
                              "bVisible": false, 
                              "aTargets": [ 5 ] 
                            },
                            { //"sTitle": "Validated?",
                              "bVisible": false,
                              "aTargets": [ 6 ]
                            },
                            { //"sTitle": "Ave. Altered<sup>1</sup>",
                              "sType": "num-nan-col",
                              "bSearchable": false,
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    return value.toFixed(2);
                              },
                              "bSearchable": false,
                              "aTargets": [ 7 ]
                            },
                            { //"sTitle": "Ave. Unaltered<sup>1</sup>",
                              "sType": "num-nan-col",
                              "bSearchable": false,
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    return value.toFixed(2);
                              },
                              "bSearchable": false,
                              "aTargets": [ 8 ]
                            },
                            { //"sTitle": "p-value",
                              "sType": "num-nan-col",
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    
                                    var ret = value < 0.001 ? value.toExponential(2) : value.toFixed(3);
                                    
                                    var eps = 10e-5;
                                    var abunUnaltered = parseFloat(obj.aData[7]);
                                    var abunAltered = parseFloat(obj.aData[8]);
                                    
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
                              "fnRender": function(obj) {
                                    if (isNaN(parseFloat(obj.aData[9])))
                                        return "";
                                    //return "<a href=\"javascript:boxplot('"+obj.aData[10]+"','')\">Boxplot</a>";
                                    return "<img class=\"details_img\" src=\"images/details_open.png\">";
                              },
                              "aTargets": [ 11 ]
                                
                            }
                        ],
                        "aaSorting": [[9,'asc']],
                        "oLanguage": {
                            "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)",
                            "sInfoFiltered": "",
                            "sLengthMenu": "Show _MENU_ per page"
                        },
                        "iDisplayLength": -1,
                        "aLengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]]
                } );
                
                // filter for antibody type
                $('div#protein_expr_filter').append("<br/>Antibody Type: "+fnCreateSelect(oTable.fnGetColumnData(2),"array_type_alteration_select","phosphorylation"));
                $('select#array_type_alteration_select').change( function () {
                        oTable.fnFilter( $(this).val(), 2);
                } );
                oTable.fnFilter("phosphorylation",2);
                
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
                $('.details_img').live('click', function () {
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
                        var data = aData[10];
                        var xlabel = "";
                        var param = 'data='+data+'&xlabel='+xlabel
                            +'&ylabel=Median-centered RPPA score&width=500&height=400';
                        var html = '<img src="boxplot.do?'+param+'">'
                                +'<br/>&nbsp;&nbsp;'
                                +'<a href="boxplot.pdf?'+param+'&format=pdf" target="_blank">PDF</a>';
                        oTable.fnOpen( nTr, html, 'rppa-details' );
                    }
                } );
                
                $('table#protein_expr_wrapper').show();
                $('div#protein_expr_wait').remove();
            }
            ,"json"
        );
        
        
    });
</script>

<div class="section" id="protein_exp">
    <div id="protein_expr_wait"><img src="images/ajax-loader.gif"/></div>
    
    <table cellpadding="0" cellspacing="0" border="0" id="protein_expr_wrapper">
        
        <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="protein_expr">
                <thead>
                    <tr valign="bottom">
                        <th rowspan="2" nowrap="nowrap" style="font-size:80%">Gene</th>
                        <th rowspan="2" nowrap="nowrap" style="font-size:80%">Alteration</th>
                        <th rowspan="2" nowrap="nowrap" style="font-size:80%">Type</th>
                        <th colspan="2" nowrap="nowrap" class="ui-state-default" style="font-size:80%">Target</th>
                        <th rowspan="2" nowrap="nowrap" style="font-size:80%">Source Organism</th>
                        <th rowspan="2" nowrap="nowrap" style="font-size:80%">Validated?</th>
                        <th colspan="2" nowrap="nowrap" class="ui-state-default" style="font-size:80%">Ave. Abundance<a href="#" title="Average of median centered protein abundance scores for unaltered cases and altered cases, respectively."><sup>1</sup></a></th>
                        <th rowspan="2" nowrap="nowrap" style="font-size:80%">p-value<a href="#" title="Based on two-sided two sample student t-test."><sup>2</sup></a></th>
                        <th rowspan="2" nowrap="nowrap" style="font-size:80%">data</th>
                        <th rowspan="2" nowrap="nowrap" style="font-size:80%">Plot</th>
                    </tr>
                    <tr>
                        <th nowrap="nowrap" style="font-size:80%">Protein</th>
                        <th nowrap="nowrap" style="font-size:80%">Residue</th>
                        <th nowrap="nowrap" style="font-size:80%">Unaltered</th>
                        <th nowrap="nowrap" style="font-size:80%">Altered</th>
                    </tr>
                </thead>
                <tfoot>
                    <tr valign="bottom">
                        <th>Gene</th>
                        <th>Alteration</th>
                        <th>Type</th>
                        <th>Protein</th>
                        <th>Residue</th>
                        <th>Source Organism</th>
                        <th>Validated?</th>
                        <th>Unaltered</th>
                        <th>Altered</th>
                        <th>p-value</th>
                        <th>data</th>
                        <th>Plot</th>
                    </tr>
                </tfoot>
            </table>
        </td></tr>
    </table>
</div>