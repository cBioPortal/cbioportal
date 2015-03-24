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

