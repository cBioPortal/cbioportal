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
        $('#study-view-add-chart').mouseenter(function(){
           $('#study-view-add-chart ul').show(300);
        });
        $('#study-view-add-chart').mouseleave(function(){
           $('#study-view-add-chart ul').hide(300);
        });
    }
    
    function createDiv() {
        var _header = $('<div></div>'),
            _span = $('<span><span>');
        
        _span.css({
                    color: 'grey'
                })
            .text('Add Chart');
        
        _header.attr({
            id: 'study-view-add-chart',
            class: 'study-view-header boxRight'
        });
        
        _header.append(_span);
        _header.append("<ul></ul>");
        $("#study-view-header-function").append(_header);
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

