var plotsbox = (function() {
    
    var render = function(data) {
        //scatterPlots(_div, _data, _apply_box_plots, _box_plots_axis)
        if (genetic_vs_genetic()) {
            if(isSameGene()) {
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
                if(isDiscretized("x") && !isDiscretized("y")) { //copy number profile is gistic
                    scatterPlots.init(ids.main_view.div, data, true, "x");
                } else if (!isDiscretized("x") && isDiscretized("y")) {
                    scatterPlots.init(ids.main_view.div, data, true, "y");
                } else if (!isDiscretized("x") && !isDiscretized("y")) { 
                    scatterPlots.init(ids.main_view.div, data, false, "");
                } else if (isDiscretized("x") && isDiscretized("y")) {
                    //TODO: heatmap
                }
            }
        } else if (genetic_vs_clinical()) {
            scatterPlots.init(ids.main_view.div, data, false, "");
        } else if (clinical_vs_clinical()) {
            console.log(data);
            scatterPlots.init(ids.main_view.div, data, false, "");
        }
    };
    
    return {
        init: function() {
            plotsData.get(render);
        }        
    };
    
}());