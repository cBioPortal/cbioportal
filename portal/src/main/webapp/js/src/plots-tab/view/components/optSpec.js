var optSpec = (function() {
    
    var append_view_switch_opt = function() {
        var stat = plotsData.stat();
        var tmp = setInterval(function () {timer();}, 1000);
        function timer() {
            if (metaData.getRetrieveStatus() !== -1 && stat.retrieved) {
                clearInterval(tmp);
                append();
            }
        }
        function append() {
            $("#" + ids.sidebar.util.view_switch + "-div").empty();
            if (genetic_vs_genetic()) {
                if(isSameGene()) {
                    if (stat.hasCnaAnno) {
                        $("#" + ids.sidebar.util.view_switch + "-div").append(
                            "<h5>View</h5>" + 
                            "<input type='radio' value='mutation_details' name='mutation_details_vs_gistic_view' checked>Mutation Type" + 
                            "<input type='radio' value='gistic' name='mutation_details_vs_gistic_view' >Copy-number"
                        );                    
                    }
                }                
            }
        }
    };
 
    return {
        init: function() {
            if (!document.getElementById(ids.sidebar.util.view_switch) + "-div") {
                $("#" + ids.sidebar.util.div).append("<div id='" + 
                        ids.sidebar.util.view_switch + "-div'></div>");
            } else {
                $("#" + ids.sidebar.util.view_switch + "-div").empty();
            }
            append_view_switch_opt();
        }
    };
    
}());