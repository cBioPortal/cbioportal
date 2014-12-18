var plotsbox = (function() {
    
    
    var render = function(data) {
        if(isSameGene()) {
            if(isDiscretized("x") && !isDiscretized("y")) { //copy number profile is gistic
                scatterPlots.init(ids.main_view.div, data, true, "x");
            } else if (!isDiscretized("x") && isDiscretized("y")) {
                scatterPlots.init(ids.main_view.div, data, true, "y");
            } else if (!isDiscretized("x") && !isDiscretized("y")) { 
                scatterPlots.init(ids.main_view.div, data, false);
            } else if (isDiscretized("x") && isDiscretized("y")) {
                //TODO: heatmap
            }
        } else if (isTwoGenes()) {
            if(isDiscretized("x") && !isDiscretized("y")) { //copy number profile is gistic
                scatterPlots.init(ids.main_view.div, data, true, "x");
            } else if (!isDiscretized("x") && isDiscretized("y")) {
                scatterPlots.init(ids.main_view.div, data, true, "y");
            } else if (!isDiscretized("x") && !isDiscretized("y")) { 
                scatterPlots.init(ids.main_view.div, data, false);
            } else if (isDiscretized("x") && isDiscretized("y")) {
                //TODO: heatmap
            }
        } else if (isGeneVsClinical()) {
            
        } else if (isClinical()) {
            
        }
    };
    
    return {
        init: function() {
            $("#" + ids.main_view.div).empty();
            plotsData.get(render);
        }        
    };
    
}());