/*
 * AddCharts Component.
 * 
 * @param _param -- 
 *                  
 * @interface: getChart -- 
 *                                       
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 */

var AddCharts = (function(){
    var liClickCallback;
    
    function addEvents() {
        $('#study-view-add-chart ul').hide();
        $('#study-view-add-chart').hover(function(){
           $('#study-view-add-chart ul').stop().show(300);
        }, function(){
           $('#study-view-add-chart ul').stop().hide(300);
        });
    }
    
    function createDiv() {
        $("#study-view-header-function").append(StudyViewBoilerplate.addChartDiv);
    }
    
    function initAddChartsButton(_param) {
        var _name = _param.name,
            _dispalyedID = _param.displayedID,
            _displayName = _param.displayName;
        
        $('#study-view-add-chart ul').find('li').remove().end();
            
        $.each(_name, function(key, value) {
            if(_dispalyedID.indexOf(value) === -1){
                $('#study-view-add-chart ul')
                    .append($("<li></li>")
                        .attr("id",value)
                        .text(_displayName[key]));
            }
        });
        
        if($('#study-view-add-chart ul').find('li').length === 0 ){
            $('#study-view-add-chart').css('display','none');
        }else{
            bindliClickFunc();
        }
    }
    
    function bindliClickFunc() {
        $('#study-view-add-chart ul li').unbind('click');
        $('#study-view-add-chart ul li').click(function() {
            var _id = $(this).attr('id'),
                _text = $(this).text();
                
            liClickCallback(_id, _text);
        });
    }
    return {
        init: function() {
            createDiv();
            addEvents();
        },
        
        initAddChartsButton: initAddChartsButton,
        
        bindliClickFunc: bindliClickFunc,
        
        liClickCallback: function(_callback) {
            liClickCallback = _callback;
        }
    };
})();

