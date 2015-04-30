var clinSpec = (function() {
    
    function appendAttrList(axis) {
        $("#" + ids.sidebar[axis].spec_div).append("<br><h5>Clinical Attribute</h5>");
        $("#" + ids.sidebar[axis].spec_div).append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<select class='chosen' style='max-width: 250px;' id='" + ids.sidebar[axis].clin_attr + "'>");
        $.each(metaData.getClinAttrsMeta(), function(index, obj) {
            $("#" + ids.sidebar[axis].clin_attr).append(
                    "<option value='" + obj.id + "'>" + obj.name + "</option>");
        });
        $("#" + ids.sidebar[axis].clin_attr).append("</select>");
        $("#" + ids.sidebar[axis].clin_attr).chosen({ 
            search_contains: true,
            width: "80%"
        });
    }

    return {
        init: function(axis) {
            $("#" + ids.sidebar[axis].spec_div).empty();
            appendAttrList(axis);
            $("#" + ids.sidebar[axis].clin_attr).bind("change", function() { regenerate_plots(axis); });
        }
    };
    
}());