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
            _nameKeys = Object.keys(_name),
            _nameKeyLength = _nameKeys.length,
            _dispalyedID = _param.displayedID,
            _displayName = _param.displayName,
            _showedNames = [],
            _showedNamesLength = 0;
        
        $('#study-view-add-chart ul').find('li').remove().end();
        
        for ( var i = 0 ; i < _nameKeyLength; i++) {
            if(_dispalyedID.indexOf(_name[_nameKeys[i]]) === -1){
                var _datum = {};
                _datum.displayName = _displayName[_nameKeys[i]];
                _datum.name = _name[_nameKeys[i]];
                _showedNames.push(_datum);
            }
        }
        
        _showedNamesLength = _showedNames.length;
        
        _showedNames.sort(function(a, b){
            var _aValue = a.displayName.toUpperCase();
            var _bValue = b.displayName.toUpperCase();
            
            return _aValue.localeCompare(_bValue);
        });
        
        for(var i = 0; i < _showedNamesLength; i++){
            $('#study-view-add-chart ul')
                    .append($("<li></li>")
                        .attr("id",_showedNames[i].name)
                        .text(_showedNames[i].displayName));
        }
        
        
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

