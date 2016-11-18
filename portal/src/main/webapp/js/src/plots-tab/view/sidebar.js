var sidebar = (function() {
    
    var render = function() {
        //only have profile data
        if (metaData.getClinAttrsMeta().length === 0 && metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0]).length !== 0) { 
            if (metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0]).length === 1 && 
                metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0])[0].type === "MUTATION_EXTENDED") {
                $("#plots").empty();
                $("#plots").append("No data available for generating plots.");               
            } else {
                $("#" + ids.sidebar.x.data_type).hide(); //if there's no clinical data, remove data type choices
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
        //only have clincal data
        } else if ((metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0]).length === 0 || 
                   (metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0]).length === 1 && 
                    metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0])[0].type === "MUTATION_EXTENDED"))&& 
                   metaData.getClinAttrsMeta().length !== 0) { 
            $("#" + ids.sidebar.x.data_type).hide();
            $("#" + ids.sidebar.y.data_type).hide(); 
            $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.clin + "']").attr('checked', 'checked');
            $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.clin + "']").attr('checked', 'checked');
            clinSpec.init("x");
            clinSpec.init("y");
            optSpec.init();  
        //no plots data at all
        } else if ((metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0]).length === 0 ||
                    metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0]).length === 1 && 
                    metaData.getGeneticProfilesMeta(window.QuerySession.getQueryGenes()[0])[0].type === "MUTATION_EXTENDED") && 
                    metaData.getClinAttrsMeta().length === 0) { 
            $("#plots").empty();
            $("#plots").append("No data available for generating plots.");
        //normal plots
        } else {
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
        }

    };
    
    var listener = function() {
        
        //listener on data types
        $("#" + ids.sidebar.x.data_type).change(function() {
            if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic) {
                profileSpec.init("x");
            } else if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin) {
                clinSpec.init("x");
            }
            profileSpec.appendLockGene();
            regenerate_plots("x");
        });
        $("#" + ids.sidebar.y.data_type).change(function() {
            if ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.genetic) {
                profileSpec.init("y");
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
            if (_x_opts.data_type === vals.data_type.genetic) {
                _x_opts.data_type = $("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val();
                _x_opts.gene_index = document.getElementById(ids.sidebar.x.gene).selectedIndex;
                _x_opts.profile_type_index = document.getElementById(ids.sidebar.x.profile_type).selectedIndex;
                _x_opts.profile_name_index = document.getElementById(ids.sidebar.x.profile_name).selectedIndex;
                _x_opts.apply_log_scale = $("#" + ids.sidebar.x.log_scale).prop('checked');  
            } else if (_x_opts.data_type === vals.data_type.clin) {
                _x_opts.clin_attr_index = document.getElementById(ids.sidebar.x.clin_attr).selectedIndex;
            }
            _y_opts.data_type = $("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val();
            if (_y_opts.data_type === vals.data_type.genetic) {
                _y_opts.gene_index = document.getElementById(ids.sidebar.y.gene).selectedIndex;
                _y_opts.profile_type_index = document.getElementById(ids.sidebar.y.profile_type).selectedIndex;
                _y_opts.profile_name_index = document.getElementById(ids.sidebar.y.profile_name).selectedIndex;
                _y_opts.apply_log_scale = $("#" + ids.sidebar.y.log_scale).prop('checked');              
                _y_opts.lock_gene = $("#" + ids.sidebar.y.lock_gene).prop('checked');
            } else if (_y_opts.data_type === vals.data_type.clin) {
                _y_opts.clin_attr_index = document.getElementById(ids.sidebar.y.clin_attr).selectedIndex;
                
            }
            //swap
            if (_y_opts.data_type === vals.data_type.genetic) {
                $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.genetic + "']").attr('checked', 'checked');
                profileSpec.init("x");
                document.getElementById(ids.sidebar.x.gene).selectedIndex = _y_opts.gene_index;
                document.getElementById(ids.sidebar.x.profile_type).selectedIndex = _y_opts.profile_type_index;
                profileSpec.updateProfileNameList("x");
                document.getElementById(ids.sidebar.x.profile_name).selectedIndex = _y_opts.profile_name_index;
                $("#" + ids.sidebar.x.log_scale).attr('checked', _y_opts.apply_log_scale);
            } else if (_y_opts.data_type === vals.data_type.clin) {
                $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.clin + "']").attr('checked', 'checked');
                clinSpec.init("x");
                $('#' + ids.sidebar.x.clin_attr + ' option').eq(_y_opts.clin_attr_index).attr('selected', 'selected');
                $("#" + ids.sidebar.x.clin_attr).chosen().change();
                $("#" + ids.sidebar.x.clin_attr).trigger("liszt:updated");
            }
            if (_x_opts.data_type === vals.data_type.genetic) {
                $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.genetic + "']").attr('checked', 'checked');
                profileSpec.init("y");
                document.getElementById(ids.sidebar.y.gene).selectedIndex = _x_opts.gene_index;
                document.getElementById(ids.sidebar.y.profile_type).selectedIndex = _x_opts.profile_type_index;
                profileSpec.updateProfileNameList("y"); 
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