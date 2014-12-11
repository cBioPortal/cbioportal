var plotsbox = (function() {
    
    
    var render = function(data) {
        scatterPlots.init(ids.main_view.div, data);
    };
    
    return {
        init: function() {
            $("#" + ids.main_view.div).empty();
            plotsData.get(render);
        }        
    };
    
}());