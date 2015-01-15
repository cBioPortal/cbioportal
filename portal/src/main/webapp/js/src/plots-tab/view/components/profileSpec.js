var profileSpec = (function() {
        
    function appendGeneList(axis) {
        $("#" + ids.sidebar[axis].spec_div).append("<br><h5>Gene</h5>");
        $("#" + ids.sidebar[axis].spec_div).append("<select id='" + ids.sidebar[axis].gene + "'>");
        $.each(window.PortalGlobals.getGeneList(), function(index, value) {
            $("#" + ids.sidebar[axis].gene).append(
                    "<option value='" + value + "'>" + value + "</option>");
        });
        $("#" + ids.sidebar[axis].spec_div).append("</select>");
        $("#" + ids.sidebar[axis].gene).change(function() {
            regenerate_plots(axis);
        });
    }
    
    function appendProfileTypeList(axis) {
        $("#" + ids.sidebar[axis].spec_div).append("<br><h5>Profile Type</h5>");
        $("#" + ids.sidebar[axis].spec_div).append("<select id='" + ids.sidebar[axis].profile_type + "'>");
        append();

        function append() {
            var _tmp = [];
            $.each(metaData.getGeneticProfilesMeta($("#" + ids.sidebar[axis].gene).val()), function(index, obj) {
                if($.inArray(obj.type, _tmp) === -1 && 
                    obj.type !== "MUTATION_EXTENDED") //tmp: skip mutation profile
                        _tmp.push(obj.type);
            });

            $.each(_tmp, function(index, value) {
                $("#" + ids.sidebar[axis].profile_type).append(
                        "<option value='" + value + "'>" + vals.profile_type[value] + "</option>");
            });
            $("#" + ids.sidebar[axis].spec_div).append("</select>");            
        }

        $("#" + ids.sidebar[axis].gene).change(function() {
            $("#" + ids.sidebar[axis].profile_type).empty();
            append();
        });
        
        $("#" + ids.sidebar[axis].profile_type).change(function() {
            regenerate_plots(axis);
        });
    }

    function appendProfileNameList(axis) {
        
        $("#" + ids.sidebar[axis].spec_div).append("<br><h5>Profile Name</h5>");
        $("#" + ids.sidebar[axis].spec_div).append("<select id='" + ids.sidebar[axis].profile_name + "'>");
        append();

        function append() {
            $.each(metaData.getGeneticProfilesMeta($("#" + ids.sidebar[axis].gene).val()), function(index, obj) {
                if (obj.type === $("#" + ids.sidebar[axis].profile_type).val()) {
                    $("#" + ids.sidebar[axis].profile_name).append(
                            "<option value='" + obj.id + "'>" + obj.name + "</option>");
                }
            });

        };
        
        $("#" + ids.sidebar[axis].gene).change(function() {
            $("#" + ids.sidebar[axis].profile_name).empty();
            append();
        });

        $("#" + ids.sidebar[axis].profile_type).change(function() {
            $("#" + ids.sidebar[axis].profile_name).empty();
            append();
        });
        
        $("#" + ids.sidebar[axis].profile_name).change(function() {
            regenerate_plots(axis);
        });
        
    }
    
    function appendLogScaleOption (axis) {
 
        $("#" + ids.sidebar[axis].spec_div).append("<div id='" + ids.sidebar[axis].log_scale + "-div'></div>");
        append();
        
        $("#" + ids.sidebar[axis].gene).change(function() {
            append();
        });
        $("#" + ids.sidebar[axis].profile_type).change(function() {
            append();
        });
        $("#" + ids.sidebar[axis].profile_name).change(function() {
            append();
        });
        
        function append() {
            $("#" + ids.sidebar[axis].log_scale + "-div").empty();
            if ($("#" + ids.sidebar[axis].profile_name).val().toLowerCase().indexOf(("zscores")) === -1 &&
                $("#" + ids.sidebar[axis].profile_name).val().toLowerCase().indexOf(("rna_seq")) !== -1) { //if rna seq (no z-score) profile, show log scale option
                    $("#" + ids.sidebar[axis].log_scale + "-div").append(
                        "<h5>Apply Log Scale</h5>" +
                        "<input type='checkbox' id='" + ids.sidebar[axis].log_scale + "'>");
                    $("#" + ids.sidebar[axis].log_scale).change(function() { scatterPlots.log_scale(); });
            }      
        }
        
    }
    
    return {
        init: function(axis) {
            $("#" + ids.sidebar[axis].spec_div).empty();
            appendGeneList(axis);
            appendProfileTypeList(axis);
            appendProfileNameList(axis);
            appendLogScaleOption(axis);
        }
    };
}());