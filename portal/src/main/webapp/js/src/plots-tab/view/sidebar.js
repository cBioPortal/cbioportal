var sidebar = (function() {
    
    var render = function() {
        profileSpec.init("x");
        profileSpec.init("y");
        optSpec.init();
        //reset the default value of x: default is always x copy num, y mrna
        document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
        profileSpec.updateProfileNameList("x");
        regenerate_plots("x");
    };
    
    var listener = function() {
        
        //listener on data types
        $("#" + ids.sidebar.x.data_type).change(function() {
            if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.genetic) {
                profileSpec.init("x");
                optSpec.init();
            } else if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.clin) {
                clinSpec.init("x");
            }
            profileSpec.appendLockGene();
            regenerate_plots("x");
        });
        $("#" + ids.sidebar.y.data_type).change(function() {
            if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.genetic) {
                profileSpec.init("y");
                optSpec.init();
                profileSpec.appendLockGene();
            } else if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.clin) {
                clinSpec.init("y");
            }
            regenerate_plots("y");
        });
        $("#" + ids.sidebar.util.view_switch + "-div").change(function() {
            mutation_copy_no_view_switch();
        });
        
    };
    
    var mutation_copy_no_view_switch = function() {
        clear_plot_box();
        plotsData.fetch("x");
        plotsData.fetch("y");
        plotsbox.init();       
    };
    
    return {
        init: function() {
            var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                if (metaData.getRetrieveStatus() !== -1) {
                    clearInterval(tmp);
                    render();
                    listener();
                }
            }
        },
        getStat: function(axis, opt) {
            return $("#" + ids.sidebar[axis][opt]).val();
        }
    };

}());