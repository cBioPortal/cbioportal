<%@ page import="org.mskcc.portal.servlet.ProteinArraySignificanceTestJSON" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.remote.GetProteinArrayData" %>
<%@ page import="java.util.Set" %>
<%
    Set<String> antibodyTypes = GetProteinArrayData.getProteinArrayTypes();
    String cancerStudyId_RPPA = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);
%>

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .datatable-filter-custom {
                float: left
        }
        .dataTables_length {
                width: auto;
                float: right;
        }
        .dataTables_info {
                width: auto;
                float: right;
        }
        .div.datatable-paging {
                width: auto;
                float: right;
        }
        td.rppa-details {
                background-color : white;
        }
</style>

<script type="text/javascript">
    function parsePValue(str) {
        return parseFloat(str.replace(/<[^>]*>/g,""));
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
    
    function getProteinArrayTypes() {
        var ret = Array();
        var i = 0;
        <%for (String type : antibodyTypes) {%>
                ret[i++] = "<%=type%>";
        <%}%>
        return ret;
    }

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
        var params = {<%=ProteinArraySignificanceTestJSON.CANCER_STUDY_ID%>:'<%=cancerStudyId_RPPA%>',
            <%=ProteinArraySignificanceTestJSON.HEAT_MAP%>:$("textarea#heat_map").html(),
            <%=ProteinArraySignificanceTestJSON.GENE%>:'Any',
            <%=ProteinArraySignificanceTestJSON.ALTERATION_TYPE%>:'Any'
        };
        if ($.browser.msie) //TODO: this is a temporary fix for bug #74
            params['<%=ProteinArraySignificanceTestJSON.DATA_SCALE%>'] = '100';
                        
        $.post("ProteinArraySignificanceTest.json", 
            params,
            function(aDataSet){
                //$("div#protein_exp").html(aDataSet);
                //alert(aDataSet);
                if (aDataSet.length==0)
                    return;
                
                var showPValueColumn = aDataSet[0][9]!="NaN";
                var showAbsDiffColumn = !showPValueColumn && aDataSet[0][8]!="NaN";
                
                var sortingColumn;
                if (showPValueColumn) {
                    sortingColumn = [9,'asc'];
                } else if (showAbsDiffColumn) {
                    sortingColumn = [8, 'desc'];
                } else if (aDataSet[0][6]!="NaN") {
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
                              "fnRender": function(obj) {
                                    return '<b>'+obj.aData[ obj.iDataColumn ]+'</b>';
                              },
                              "aTargets": [ 4 ] 
                            },
                            { //"sTitle": "Target Residue",
                              "aTargets": [ 5 ] 
                            },
//                            { //"sTitle": "Source organism",
//                              "bVisible": false, 
//                              "aTargets": [ 6 ] 
//                            },
//                            { //"sTitle": "Validated?",
//                              "bVisible": false,
//                              "aTargets": [ 7 ]
//                            },
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
                              "aTargets": [ 6 ]
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
                              "aTargets": [ 7 ]
                            },
                            { //"sTitle": "abs diff",
                              "bVisible": showAbsDiffColumn,
                              "sType": "num-nan-col",
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    
                                    var ret = value.toFixed(2);
                                    
                                    var eps = 10e-5;
                                    var abunUnaltered = parseFloat(obj.aData[6]);
                                    var abunAltered = parseFloat(obj.aData[7]);
                                    
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
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    
                                    var ret = value < 0.001 ? value.toExponential(2) : value.toFixed(3);
                                    if (value <= 0.05)
                                        ret = '<b>'+ret+'</b>';
                                    
                                    var eps = 10e-5;
                                    var abunUnaltered = parseFloat(obj.aData[6]);
                                    var abunAltered = parseFloat(obj.aData[7]);
                                    
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

                // help
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
                        var antibody = "antibody:"+aData[4].replace(/<[^>]*>/g,"");
                        if (aData[5])
                            antibody += ' ['+aData[5]+']';
                        var xlabel = "Query: ";
                        if (aData[1] == "Any")
                            xlabel += '<%=geneList.replaceAll("\r?\n"," ")%>';
                        else
                            xlabel += aData[1];
                        var pvalue = parsePValue(aData[9]);
                        if (!isNaN(pvalue)) {
                            xlabel += " (p-value: "+pvalue+")";
                        }
                        var ylabel = "RPPA score ("+antibody+")";
                        var param = 'xlabel='+xlabel+'&ylabel='+ylabel+'&width=500&height=400&data='+data;
                        var html = 'Boxplots of RPPA data ('+antibody+') for altered and unaltered cases ';
                        html += ' [<a href="boxplot.pdf?'+'format=pdf&'+param+'" target="_blank">PDF</a>]<br/>' 
                                + '<img src="boxplot.do?'+param+'">';
                        oTable.fnOpen( nTr, html, 'rppa-details' );
                    }
                } );
                
                // filter for antibody type
                oTable.fnFilter("phosphorylation",3);
                $('div.datatable-filter-custom').html("Antibody Type: "+
                    fnCreateSelect(getProteinArrayTypes(),"array_type_alteration_select","phosphorylation")
                    );
                $('select#array_type_alteration_select').change( function () {
                        oTable.fnFilter( $(this).val(), 3);
                } );
                
                // widen the rppa data
                $('table#protein_expr').css("width","100%");
                
                $('div#protein_expr_wait').remove();
                $('table#protein_expr_wrapper').show();
            }
            ,"json"
        );
    });
</script>

<div class="section" id="protein_exp">
    <div id="protein_expr_wait"><img src="images/ajax-loader.gif"/></div>
    
    <table cellpadding="0" cellspacing="0" border="0" id="protein_expr_wrapper" width="100%">
        
        <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="protein_expr">
                <thead style="font-size:80%">
                    <tr valign="bottom">
                        <th rowspan="2">RPPA ID</th>
                        <th rowspan="2">Gene</th>
                        <th rowspan="2">Alteration</th>
                        <th rowspan="2">Type</th>
                        <th colspan="2" class="ui-state-default">Target</th>
                        <th colspan="3" class="ui-state-default">Ave. Abundance<img class="datatable_help" src="images/help.png" title="Average of median centered protein abundance scores for unaltered cases and altered cases, respectively."/></th>
                        <th rowspan="2" nowrap="nowrap">p-value<img class="datatable_help" src="images/help.png" title="Based on two-sided two sample student t-test."/></th>
                        <th rowspan="2">data</th>
                        <th rowspan="2">Plot</th>
                    </tr>
                    <tr>
                        <th>Protein</th>
                        <th>Residue</th>
                        <th>Unaltered</th>
                        <th>Altered</th>
                        <th nowrap="nowrap">Abs. Diff.<!--img class="datatable_help" src="images/help.png" title="Absolute difference of average RPPA scores between altered and unaltered cases."/--></th>
                    </tr>
                </thead>
                <tfoot>
                    <tr valign="bottom">
                        <th>RPPA ID</th>
                        <th>Gene</th>
                        <th>Alteration</th>
                        <th>Type</th>
                        <th>Protein</th>
                        <th>Residue</th>
                        <th>Unaltered</th>
                        <th>Altered</th>
                        <th>Abs. Diff.</th>
                        <th>p-value</th>
                        <th>data</th>
                        <th>Plot</th>
                    </tr>
                </tfoot>
            </table>
        </td></tr>
    </table>
</div>