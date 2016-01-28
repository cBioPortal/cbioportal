var plotsbox = (function() {
    
    //scatterPlots.init(div, data, if_apply_box_plots, box_plots_axis, if_calculate_co_exp);
    var render = function(data) {

        if (genetic_vs_genetic()) {
            if(isSameGene()) {
                if(is_profile_discretized("x") && !is_profile_discretized("y")) { //copy number profile is gistic
                    scatterPlots.init(ids.main_view.div, data, true, "x", false);
                } else if (!is_profile_discretized("x") && is_profile_discretized("y")) {
                    scatterPlots.init(ids.main_view.div, data, true, "y", false);
                } else if (!is_profile_discretized("x") && !is_profile_discretized("y")) { 
                    scatterPlots.init(ids.main_view.div, data, false, "", true);
                } else if (is_profile_discretized("x") && is_profile_discretized("y")) {
                    heat_map.init(ids.main_view.div, data);
                }
            } else if (isTwoGenes()) {
                if(is_profile_discretized("x") && !is_profile_discretized("y")) { //copy number profile is gistic
                    scatterPlots.init(ids.main_view.div, data, true, "x", false);
                } else if (!is_profile_discretized("x") && is_profile_discretized("y")) {
                    scatterPlots.init(ids.main_view.div, data, true, "y", false);
                } else if (!is_profile_discretized("x") && !is_profile_discretized("y")) { 
                    scatterPlots.init(ids.main_view.div, data, false, "", true);
                } else if (is_profile_discretized("x") && is_profile_discretized("y")) {
                    heat_map.init(ids.main_view.div, data);
                }
            }
        } else if (genetic_vs_clinical()) {
            var _clin_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin)? "x": "y";
            var _genetic_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin)? "y": "x";
            if (clinical_attr_is_discretized(_clin_axis) && is_profile_discretized(_genetic_axis)) {
                heat_map.init(ids.main_view.div, data);
            } else if (clinical_attr_is_discretized(_clin_axis) && !is_profile_discretized(_genetic_axis)) {
                scatterPlots.init(ids.main_view.div, data, true, _clin_axis, false);
            } else if (!clinical_attr_is_discretized(_clin_axis) && is_profile_discretized(_genetic_axis)) {
                scatterPlots.init(ids.main_view.div, data, true, _genetic_axis, false);
            } else if (!clinical_attr_is_discretized(_clin_axis) && !is_profile_discretized(_genetic_axis)) {
                scatterPlots.init(ids.main_view.div, data, false, "", true);
            }
        } else if (clinical_vs_clinical()) {
            if (clinical_attr_is_discretized("x") && clinical_attr_is_discretized("y")) {
                heat_map.init(ids.main_view.div, data);
            } else if (clinical_attr_is_discretized("x") && !clinical_attr_is_discretized("y")) {
                scatterPlots.init(ids.main_view.div, data, true, "x", false);
            } else if (!clinical_attr_is_discretized("x") && clinical_attr_is_discretized("y")) {
                scatterPlots.init(ids.main_view.div, data, true, "y", false);
            } else if (!clinical_attr_is_discretized("x") && !clinical_attr_is_discretized("y")) {
                scatterPlots.init(ids.main_view.div, data, false, "", true);
            }
        }
    };
    
    return {
        init: function() {
        	render(plotsData.get());
        }        
    };
    
}());