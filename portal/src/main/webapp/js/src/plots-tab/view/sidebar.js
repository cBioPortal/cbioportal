var sidebar = (function() {
    
    var render = function() {
        //if there's no clinical data, remove data type choices
        if (metaData.getClinAttrsMeta().length === 0 && metaData.getGeneticProfilesMeta(window.PortalGlobals.getGeneList()[0]).length !== 0) { //only have profile data
            if (metaData.getGeneticProfilesMeta(window.PortalGlobals.getGeneList()[0]).length === 1 && 
                metaData.getGeneticProfilesMeta(window.PortalGlobals.getGeneList()[0])[0].type === "MUTATION_EXTENDED") {
                $("#plots").empty();
                $("#plots").append("No data available for generating plots.");               
            } else {
                $("#" + ids.sidebar.x.data_type).hide();
                $("#" + ids.sidebar.y.data_type).hide(); 
                profileSpec.init("x");
                profileSpec.init("y");
                optSpec.init();
                //reset the default value of x: default is always x copy num, y mrna
                document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
                profileSpec.updateProfileNameList("x");
                regenerate_plots("x");                
            }
        } else if (metaData.getGeneticProfilesMeta(window.PortalGlobals.getGeneList()[0]).length === 0 && metaData.getClinAttrsMeta().length !== 0) { //only have clincal data
            $("#" + ids.sidebar.x.data_type).hide();
            $("#" + ids.sidebar.y.data_type).hide(); 
            clinSpec.init("x");
            clinSpec.init("y");
            optSpec.init();                
        } else if (metaData.getGeneticProfilesMeta(window.PortalGlobals.getGeneList()[0]).length === 0 && metaData.getClinAttrsMeta().length === 0) { //no plots data
            $("#plots").empty();
            $("#plots").append("No data available for generating plots.");
        } else {
            profileSpec.init("x");
            profileSpec.init("y");
            optSpec.init();
            //reset the default value of x: default is always x copy num, y mrna
            if (metaData.getGeneticProfilesMeta(window.PortalGlobals.getGeneList()[0].length !== 1)) {
                document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
                profileSpec.updateProfileNameList("x");
                regenerate_plots("x");
            }
        }

    };
    
    var listener = function() {
        
        //listener on data types
        $("#" + ids.sidebar.x.data_type).change(function() {
            if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic) {
                profileSpec.init("x");
                optSpec.init();
            } else if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin) {
                clinSpec.init("x");
            }
            profileSpec.appendLockGene();
            regenerate_plots("x");
        });
        $("#" + ids.sidebar.y.data_type).change(function() {
            if ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.genetic) {
                profileSpec.init("y");
                optSpec.init();
                profileSpec.appendLockGene();
            } else if ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.clin) {
                clinSpec.init("y");
            }
            regenerate_plots("y");
        });
        $("#" + ids.sidebar.util.view_switch).change(function() {
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