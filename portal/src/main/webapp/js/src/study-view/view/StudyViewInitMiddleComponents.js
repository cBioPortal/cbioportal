/*
 * View for Middle Components 
 * 
 *                                       
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */


var StudyViewInitMiddleComponents = (function() {
    
    function addEvents() {
        $('#study-view-dataTable-updateCharts').unbind('click');
        $('#study-view-dataTable-updateCharts').click(function(){
            var _items = [];
            
            $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
               _items.push( $(this).text() );       
            });
            
            _items = $.unique( _items );

            StudyViewInitCharts.filterChartsByGivingIDs(_items);
        });
        $('#study-view-dataTable-updateTable').unbind('click');
        $('#study-view-dataTable-updateTable').click(function(){
            var _filteredResult = StudyViewInitCharts.getFilteredResults();
            StudyViewInitDataTable.getDataTable().updateTable(_filteredResult);
        });
    }
    
    function createDiv() {
        var _newElement = $("<div></div>");
        
        _newElement.attr('id', 'study-view-updateContent');
        
        $("#study-view-update").append(_newElement);
        $("#study-view-updateContent").append(StudyViewBoilerplate.downArrowDiv);
        $("#study-view-updateContent").append(StudyViewBoilerplate.updateTableDiv);
        $("#study-view-updateContent").append(StudyViewBoilerplate.upArrowDiv);
        $("#study-view-updateContent").append(StudyViewBoilerplate.updateChartsDiv);
    }
    
    return {
        init: function() {
            createDiv();
            addEvents();
        }
    };
})();