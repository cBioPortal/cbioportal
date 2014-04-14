/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


dc.redrawAllDataTable = function(group) {
        var dataTable1 = $('#dataTable').dataTable();

        dataTable1.fnDestroy();

        var charts = dc.chartRegistry.list(group);
        for (var i = 0; i < charts.length; i++) {
            charts[i].redraw();
        }

        if(dc._renderlet !== null)
            dc._renderlet(group);

        dataTable1 = $('#dataTable').dataTable({
                "sScrollX": "1200px",
                "sScrollY": "300px",
                "bPaginate": false,
                "bFilter":true,
                "bScrollCollapse": true
        });
        for(var i =0 ; i< removeKeyIndex.length ; i++){
            dataTable1.fnSetColumnVis(removeKeyIndex[i],false);
        }

        var numColumns = dataTable1.fnSettings().aoColumns.length;
        var maxX = 0;

        for(var i =1;i<=numColumns ; i++){
            var rotatedX = $("table.dataTable>thead>tr>th:nth-child("+i+")").height();
            if(rotatedX > maxX)
                maxX = rotatedX;
        }

        for(var i =1;i<=numColumns ; i++){
            $("table.dataTable>thead>tr>th:nth-child("+i+")").height(maxX);
        }  

        new FixedColumns( dataTable1); 
        dataTable1.fnAdjustColumnSizing();
        $(".DTFC_LeftBodyLiner").css("overflow-y","hidden");
        $(".dataTables_scroll").css("overflow-x","scroll");
        $(".DTFC_LeftHeadWrapper").css("background-color","white");
        
        $("#dataTable_filter label input").attr("value","");    
        $('#study-view-dataTable-header').click(function(){
                if($("#dataTable_filter label input").val() !== ""){			
                        var items=[];
                        $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
                           items.push( $(this).text() );       
                        });
                        var items = $.unique( items );
                        dataTableDC.filter(null);
                        dataTableDC.filter([items]);
                        dc.redrawAll();
                }else{
                        dataTableDC.filter(null);
                        dc.redrawAll();
                }
        });
        $('#study-view-dataTable-updateTable').click(function(){
                dc.redrawAllDataTable("group1");
        });
    };

