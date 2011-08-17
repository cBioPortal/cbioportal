<%@ page import="org.mskcc.portal.servlet.ProteinArraySignificanceTestJSON" %>

<link href="css/data_table.css" type="text/css" rel="stylesheet"/>

<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script> 

<script type="text/javascript">
    var newwindow = ''
    function imgpopitup(url) {
        if (newwindow.location && !newwindow.closed) {
            newwindow.location.href = url; 
            newwindow.focus(); } 
        else { 
            newwindow=window.open(url,'htmlname','width=600,height=600,resizable=1');} 
    }
    function boxplot(data,xlabel) {
        // todo: rinstalled?
        var url = 'boxplot.do?data='+data+'&xlabel='+xlabel+'&ylabel=Median-centered RPPA score';
        imgpopitup(url);
    }
    // Based on JavaScript provided by Peter Curtis at www.pcurtis.com -->
</script>

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
        $.post("ProteinArraySignificanceTest.json", 
            {<%=ProteinArraySignificanceTestJSON.HEAT_MAP%>:$("textarea#heat_map").html()
            },
            function(aDataSet){
                //$("div#protein_exp").html(aDataSet);
                //alert(aDataSet);
                //$('div#protein_exp').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" class="display" id="protein_expr"></table>' );
                var oTable = $('table#protein_expr').dataTable( {
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
                                    
                                    var ret =value < 0.001 ? value.toExponential(2) : value.toFixed(3);
                                    
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
                            { //"sTitle": "plot",
                              //"bVisible": false,
                              "bSearchable": false,
                              "bSortable": false,
                              "fnRender": function(obj) {
//                                    if (isNaN(parseFloat(obj.aData[9])))
//                                        return "";
                                    return "<a href=\"javascript:boxplot('"+obj.aData[10]+"','')\">Boxplot</a>";                                   
                              },
                              "aTargets": [ 10 ]
                            }
                        ],
                        "aaSorting": [[9,'asc']],
                        "iDisplayLength": 25
                } );
                
                /* Add select menu*/
                $("div#gene_select_box").html("<b>Gene</b>:<br/>"+fnCreateSelect(oTable.fnGetColumnData(0),"rppa_gene_select","Any"));
                $('select#rppa_gene_select').change( function () {
                        oTable.fnFilter( $(this).val(), 0);
                } );
                oTable.fnFilter("Any",0);
                
                $("div#alteration_select_box").html("<br/><b>Alteration</b>:<br/>"+fnCreateSelect(oTable.fnGetColumnData(1),"rppa_alteration_select","Any"));
                $('select#rppa_alteration_select').change( function () {
                        oTable.fnFilter( $(this).val(), 1);
                } );
                oTable.fnFilter("Any",1);
                
                $("div#array_type_select_box").html("<br/><b>Antibody Type</b>:<br/>"+fnCreateSelect(oTable.fnGetColumnData(2),"array_type_alteration_select","phosphorylation"));
                $('select#array_type_alteration_select').change( function () {
                        oTable.fnFilter( $(this).val(), 2);
                } );
                oTable.fnFilter("phosphorylation",2);
                
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
        
        <tr><td>
                <br/><br/><br/>
            <fieldset><legend>Parameters</legend>
                <div id="gene_select_box"></div>
                <div id="alteration_select_box"></div>
                <div id="array_type_select_box"></div>
            </fieldset>                
        </td>
        <td>&nbsp;&nbsp;</td>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="protein_expr">
                <thead>
                    <tr valign="bottom">
                        <th rowspan="2">Gene</th>
                        <th rowspan="2">Alteration</th>
                        <th rowspan="2">Type</th>
                        <th colspan="2">Target</th>
                        <th rowspan="2">Source organism</th>
                        <th rowspan="2">Validated?</th>
                        <th colspan="2">Ave. Abundance<a href="#" title="Average of median centered protein abundance scores for unaltered cases and altered cases, respectively."><sup>1</sup></a></th>
                        <th rowspan="2">p-value<a href="#" title="Based on two-sided two sample student t-test."><sup>2</sup></a></th>
                        <th rowspan="2">Plot</th>
                    </tr>
                    <tr>
                        <th>Protein</th>
                        <th>Residue</th>
                        <th>Unaltered</th>
                        <th>Altered</th>
                    </tr>
                </thead>
            </table>
        </td></tr>
    </table>
</div>