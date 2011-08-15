<%@ page import="org.mskcc.portal.servlet.ProteinArraySignificanceTestJSON" %>

<link href="css/data_table.css" type="text/css" rel="stylesheet"/>

<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script> 

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
    
    $(document).ready(function(){
        var heat_map = $("textarea#heat_map").html();
        
        $.post("ProteinArraySignificanceTest.json", 
            {<%=ProteinArraySignificanceTestJSON.HEAT_MAP%>:heat_map
            },
            function(aDataSet){
                //$("div#protein_exp").html(data);
                //alert(data);
                //$('div#protein_exp').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" class="display" id="protein_expr"></table>' );
                $('table#protein_expr').dataTable( {
                        "aaData": aDataSet,
                        "aoColumnDefs":[
                            { //"sTitle": "Type",
                              "aTargets": [ 0 ]
                            },
                            { //"sTitle": "Target Gene",
                              "aTargets": [ 1 ] 
                            },
                            { //"sTitle": "Target Residue",
                              "aTargets": [ 2 ] 
                            },
                            { //"sTitle": "Source organism",
                              "bVisible": false, 
                              "aTargets": [ 3 ] 
                            },
                            { //"sTitle": "Validated?",
                              "bVisible": false,
                              "aTargets": [ 4 ]
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
                               "aTargets": [ 5 ]
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
                               "aTargets": [ 6 ]
                            },
                            { //"sTitle": "p-value",
                              "sType": "num-nan-col",
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    
                                    var ret = value < 0.001 ? value.toExponential(2) : value.toFixed(3);
                                    
                                    var eps = 10e-5;
                                    var abunUnaltered = parseFloat(obj.aData[5]);
                                    var abunAltered = parseFloat(obj.aData[6]);
                                    
                                    if (Math.abs(abunUnaltered-abunAltered)<eps)
                                        return ret;
                                    if (abunUnaltered < abunAltered)
                                        return ret + "<img src=\"images/up1.png\"/>";
                                    
                                    return ret + "<img src=\"images/down1.png\"/>";                                    
                               },
                               "aTargets": [ 7 ]
                            },
                            { //"sTitle": "data",
                              "bVisible": false,
                              "bSearchable": false,
                              "bSortable": false,
                              "aTargets": [ 8 ]
                            }
                        ],
                        "aaSorting": [[7,'asc']],
                        "iDisplayLength": 25
                } );
            },
            "json"
        );
    });
</script>

<div class="section" id="protein_exp">
    <table cellpadding="0" cellspacing="0" border="0" class="display" class="display" id="protein_expr">
        <thead>
            <tr valign="bottom">
                <th rowspan="2">Type</th>
                <th colspan="2">Target</th>
                <th rowspan="2">Source organism</th>
                <th rowspan="2">Validated?</th>
                <th colspan="2">Ave. Abundance<a href="#" title="Average of median centered protein abundance scores for unaltered cases and altered cases, respectively."><sup>1</sup></a></th>
                <th rowspan="2">p-value<a href="#" title="Based on two-sided two sample student t-test."><sup>2</sup></a></th>
                <th rowspan="2">Data</th>
            </tr>
            <tr>
                <th>Protein</th>
                <th>Residue</th>
                <th>Unaltered</th>
                <th>Altered</th>
            </tr>
        </thead>
        <tbody>
            <tr><td><img src="images/ajax-loader.gif"/></td></tr>
        </tbody>
    </table>
</div>