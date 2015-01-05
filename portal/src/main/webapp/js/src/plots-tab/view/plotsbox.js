var plotsbox = (function() {
    
    var render = function(data) {
        if(isSameGene()) {
            console.log("in is one gene");
            if(isDiscretized("x") && !isDiscretized("y")) { //copy number profile is gistic
                scatterPlots.init(ids.main_view.div, data, true, "x");
            } else if (!isDiscretized("x") && isDiscretized("y")) {
                scatterPlots.init(ids.main_view.div, data, true, "y");
            } else if (!isDiscretized("x") && !isDiscretized("y")) { 
                scatterPlots.init(ids.main_view.div, data, false, "");
            } else if (isDiscretized("x") && isDiscretized("y")) {
                //TODO: heatmap
            }
        } else if (isTwoGenes()) {
            console.log("in two genes");
            if(isDiscretized("x") && !isDiscretized("y")) { //copy number profile is gistic
                scatterPlots.init(ids.main_view.div, data, true, "x");
            } else if (!isDiscretized("x") && isDiscretized("y")) {
                scatterPlots.init(ids.main_view.div, data, true, "y");
            } else if (!isDiscretized("x") && !isDiscretized("y")) { 
                scatterPlots.init(ids.main_view.div, data, false, "");
            } else if (isDiscretized("x") && isDiscretized("y")) {
                //TODO: heatmap
            }
        } else if (isGeneVsClinical()) {
            
        } else if (isClinical()) {
            
        }
    };
    
    return {
        init: function() {
            plotsData.get(render);
        }        
    };
    
}());