var sidebar = (function() {
    
    var render = function() {
    	
    	clinicalData = false;
    	geneProfileData = false;
    	onlyMutData = false;
    	sData = false;
    	
    	//check clinical data
    	if (metaData.getClinAttrsMeta().length !== 0) {
    		clinicalData = true;
    	}
    	
    	// check genetic profile data (i.e. is there any gene based data like mrna, rppa, cna or mutation data profiles?)
    	if (window.QuerySession.getQueryGenes() !== null &&
    			metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0]).length !== 0) {
    		geneProfileData = true;
    
    		// check if only mutation data
    		if (metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0]).length === 1 && 
                    metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0])[0].type === "MUTATION_EXTENDED") {
    			onlyMutData = true;
    		}
    	}
    		
		// check geneset data
    	if (window.QuerySession.getQueryGenesets() !== null &&
    			metaData.getGenesetsMeta(window.QuerySession.getQueryGenesets()[0]).length !== 0) {
    		genesetsData = true;
    	}
    	
        //the study only has profile data
        if (!clinicalData && geneProfileData && !genesetsData) { 
            if (onlyMutData) {
                $("#plots").empty();
                $("#plots").append("No data available for generating plots.");               
            } else {
                $("#" + ids.sidebar.x.data_type).hide(); //if there's no clinical data and profile data, remove data type choices
                $("#" + ids.sidebar.y.data_type).hide(); 
                profileSpec.init("x");
                profileSpec.init("y");
                optSpec.init();
                //reset the default value of x: default is always x copy num, y mrna
                if (document.getElementById(ids.sidebar.x.profile_type).length > 1) {
                    document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
                    profileSpec.updateProfileNameList("x");                    
                }
            }
        // only have gsva data
        } else if ((!geneProfileData || onlyMutData) && !clinicalData && genesetsData) {
        	$("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.geneset + "']").attr('checked', 'checked');
            $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.geneset + "']").attr('checked', 'checked');
            $("#" + ids.sidebar.x.data_type).hide(); //if there's no clinical data and profile data, remove data type choices
            $("#" + ids.sidebar.y.data_type).hide();
            genesetsSpec.init("x");
            genesetsSpec.init("y");
            optSpec.init();
            //reset the default value of x: default is always x copy num, y mrna
            if (document.getElementById(ids.sidebar.x.profile_type).length > 1) {
                document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
                genesetsSpec.updatePlotValueList("x");                    
            }
                
        //only have clinical data
        } else if ((!geneProfileData || onlyMutData) && clinicalData && !genesetsData) { 
            $("#" + ids.sidebar.x.data_type).hide(); //Hide all buttons
            $("#" + ids.sidebar.y.data_type).hide(); 
            $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.clin + "']").attr('checked', 'checked');
            $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.clin + "']").attr('checked', 'checked');
            clinSpec.init("x");
            clinSpec.init("y");
            optSpec.init();
            
        //only clinical and profile data
        } else if ((!genesetsData) && clinicalData && geneProfileData) {
        	$("#button-x-gene-set").hide(); //Hide gene set button
        	$("#button-y-gene-set").hide();
            profileSpec.init("x");
            profileSpec.init("y");
            optSpec.init();
            //reset the default value of x: default is always x copy num, y mrna
            var _type_arr = [];
            $.each(metaData.getGeneticProfilesMeta($("#" + ids.sidebar.x.gene).val()), function(index, obj) {
                if($.inArray(obj.type, _type_arr) === -1 &&
                    obj.type !== "MUTATION_EXTENDED" &&
                    obj.type !== "PROTEIN_LEVEL") //skip mutation & (old)protein profile
                    _type_arr.push(obj.type);
            });

            if (_type_arr.length > 1) { //if there's only one profile type don't adjust the default settings
                document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
                profileSpec.updateProfileNameList("x");
            }
        // only clinical and gsva data
        } else if ((!geneProfileData || onlyMutData) && clinicalData && genesetsData) {
            $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.geneset + "']").attr('checked', 'checked');
            $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.geneset + "']").attr('checked', 'checked');
        	$("#button-x-gene").hide(); //Hide gene button
        	$("#button-y-gene").hide();
        	genesetsSpec.init("x");
            genesetsSpec.init("y");
            optSpec.init();
            //reset the default value of x: default is always x copy num, y mrna
            var _type_arr = [];
            $.each(metaData.getGenesetsMeta($("#" + ids.sidebar.x.gene).val()), function(index, obj) {
                _type_arr.push(obj.type);
            });

            if (_type_arr.length > 1) { //if there's no profile type don't adjust the default settings
                document.getElementById(ids.sidebar.x.profile_name).selectedIndex = "1";
                genesetsSpec.updatePlotValueList("x");
            }
        //only profile and gsva data
        } else if (geneProfileData && !onlyMutData && !clinicalData && genesetsData) {
            $("#button-x-clinical").hide(); //Hide clinical data button
        	$("#button-y-clinical").hide();
            profileSpec.init("x");
            profileSpec.init("y");
            optSpec.init();
            //reset the default value of x: default is always x copy num, y mrna
            if (document.getElementById(ids.sidebar.x.profile_type).length > 1) {
                document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
                profileSpec.updateProfileNameList("x");                    
            }
        //no plots data at all
        } else if ((!geneProfileData || onlyMutData) && !clinicalData && !genesetsData) { 
            $("#plots").empty();
            $("#plots").append("No data available for generating plots.");
        //normal plots
        } else {
        	profileSpec.init("x")
        	profileSpec.init("y")
            optSpec.init();
          //reset the default value of x: default is always x copy num, y mrna
            if (document.getElementById(ids.sidebar.x.profile_type).length > 1) {
                document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
                profileSpec.updateProfileNameList("x");                    
            }
        }
    };
    
    var listener = function() {
        
        //listener on data types
        $("#" + ids.sidebar.x.data_type).change(function() {
            if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.gene) {
                profileSpec.init("x");
            } else if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.geneset) {
                genesetsSpec.init("x");
            } else if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin) {
                clinSpec.init("x");
            }
            profileSpec.appendLockGene();
            regenerate_plots("x");
        });
        $("#" + ids.sidebar.y.data_type).change(function() {
            if ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.gene) {
                profileSpec.init("y");
            } else if ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.geneset) {
                genesetsSpec.init("y");
            } else if ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.clin) {
                clinSpec.init("y");
            }
            regenerate_plots("y");
        });

        //listener on view change button
        $("#" + ids.sidebar.util.view_switch).change(function() {
            mutation_copy_no_view_switch();
        });

        //listener on axis swap button
        $("#plots-tab-swap-btn").click(function() {
            //preserve the previous selection
            var _x_opts = {}, _y_opts = {};
            _x_opts.data_type = $("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val();
            if (_x_opts.data_type === vals.data_type.gene) {
                _x_opts.data_type = $("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val();
                _x_opts.gene_index = document.getElementById(ids.sidebar.x.gene).selectedIndex;
                _x_opts.profile_type_index = document.getElementById(ids.sidebar.x.profile_type).selectedIndex;
                _x_opts.profile_name_index = document.getElementById(ids.sidebar.x.profile_name).selectedIndex;
                _x_opts.apply_log_scale = $("#" + ids.sidebar.x.log_scale).prop('checked');
            } else if (_x_opts.data_type === vals.data_type.geneset) {
                _x_opts.data_type = $("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val();
                _x_opts.gene_index = document.getElementById(ids.sidebar.x.gene).selectedIndex;
                _y_opts.profile_name_index = document.getElementById(ids.sidebar.x.profile_name).selectedIndex;
                _x_opts.apply_log_scale = $("#" + ids.sidebar.x.log_scale).prop('checked');  
            } else if (_x_opts.data_type === vals.data_type.clin) {
                _x_opts.clin_attr_index = document.getElementById(ids.sidebar.x.clin_attr).selectedIndex;
            }
            _y_opts.data_type = $("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val();
            if (_y_opts.data_type === vals.data_type.gene) {
                _y_opts.gene_index = document.getElementById(ids.sidebar.y.gene).selectedIndex;
                _y_opts.profile_type_index = document.getElementById(ids.sidebar.y.profile_type).selectedIndex;
                _y_opts.profile_name_index = document.getElementById(ids.sidebar.y.profile_name).selectedIndex;
                _y_opts.apply_log_scale = $("#" + ids.sidebar.y.log_scale).prop('checked');              
                _y_opts.lock_gene = $("#" + ids.sidebar.y.lock_gene).prop('checked');
            } else if (_y_opts.data_type === vals.data_type.geneset) {
                _y_opts.gene_index = document.getElementById(ids.sidebar.y.gene).selectedIndex;
                _y_opts.profile_name_index = document.getElementById(ids.sidebar.y.profile_name).selectedIndex;
            } else if (_y_opts.data_type === vals.data_type.clin) {
                _y_opts.clin_attr_index = document.getElementById(ids.sidebar.y.clin_attr).selectedIndex;
                
            }
            //swap
            if (_y_opts.data_type === vals.data_type.gene) {
                $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.gene + "']").attr('checked', 'checked');
                profileSpec.init("x");
                document.getElementById(ids.sidebar.x.gene).selectedIndex = _y_opts.gene_index;
                document.getElementById(ids.sidebar.x.profile_type).selectedIndex = _y_opts.profile_type_index;
                profileSpec.updateProfileNameList("x");
                document.getElementById(ids.sidebar.x.profile_name).selectedIndex = _y_opts.profile_name_index;
                $("#" + ids.sidebar.x.log_scale).attr('checked', _y_opts.apply_log_scale);
            } else if (_y_opts.data_type === vals.data_type.geneset) {
                $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.geneset + "']").attr('checked', 'checked');
                genesetsSpec.init("x");
                document.getElementById(ids.sidebar.x.gene).selectedIndex = _y_opts.gene_index;
                genesetsSpec.updatePlotValueList("x");
                document.getElementById(ids.sidebar.x.profile_name).selectedIndex = _y_opts.profile_name_index;
                $("#" + ids.sidebar.x.log_scale).attr('checked', _y_opts.apply_log_scale);
            } else if (_y_opts.data_type === vals.data_type.clin) {
                $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.clin + "']").attr('checked', 'checked');
                clinSpec.init("x");
                $('#' + ids.sidebar.x.clin_attr + ' option').eq(_y_opts.clin_attr_index).attr('selected', 'selected');
                $("#" + ids.sidebar.x.clin_attr).chosen().change();
                $("#" + ids.sidebar.x.clin_attr).trigger("liszt:updated");
            }
            if (_x_opts.data_type === vals.data_type.gene) {
                $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.gene + "']").attr('checked', 'checked');
                profileSpec.init("y");
                document.getElementById(ids.sidebar.y.gene).selectedIndex = _x_opts.gene_index;
                document.getElementById(ids.sidebar.y.profile_type).selectedIndex = _x_opts.profile_type_index;
                profileSpec.updateProfileNameList("y"); 
                document.getElementById(ids.sidebar.y.profile_name).selectedIndex = _x_opts.profile_name_index;    
                $("#" + ids.sidebar.y.log_scale).attr('checked', _x_opts.apply_log_scale);
                $("#" + ids.sidebar.y.lock_gene).attr('checked', _y_opts.lock_gene);
                document.getElementById(ids.sidebar.y.gene).disabled = $("#" + ids.sidebar.y.lock_gene).attr('checked');
            } else if (_x_opts.data_type === vals.data_type.geneset) {
                $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.geneset + "']").attr('checked', 'checked');
                genesetsSpec.init("y");
                document.getElementById(ids.sidebar.y.gene).selectedIndex = _x_opts.gene_index;
                genesetsSpec.updatePlotValueList("y");
                document.getElementById(ids.sidebar.y.profile_name).selectedIndex = _x_opts.profile_name_index;    
                $("#" + ids.sidebar.y.log_scale).attr('checked', _x_opts.apply_log_scale);
                $("#" + ids.sidebar.y.lock_gene).attr('checked', _y_opts.lock_gene);
                document.getElementById(ids.sidebar.y.gene).disabled = $("#" + ids.sidebar.y.lock_gene).attr('checked');
            } else if (_x_opts.data_type === vals.data_type.clin) {
                $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.clin + "']").attr('checked', 'checked');
                clinSpec.init("y");
                $('#' + ids.sidebar.y.clin_attr + ' option').eq(_x_opts.clin_attr_index).attr('selected', 'selected');
                $("#" + ids.sidebar.y.clin_attr).chosen().change();
                $("#" + ids.sidebar.y.clin_attr).trigger("liszt:updated");
            }
            //update plots
            regenerate_plots("xy");
        });
        
    };
    
    var mutation_copy_no_view_switch = function() {
        //update plots
        regenerate_plots("xy");
    };
    
    return {
        init: function() {
            render();
            listener();
        },
        getStat: function(axis, opt) {
            return $("#" + ids.sidebar[axis][opt]).val();
        }
    };

}());