var clinSpec = (function() {
    
    function appendAttrList(axis) {
        $("#" + ids.sidebar[axis].spec_div).append("<br><h5>Clinical Attribute</h5>");
        $("#" + ids.sidebar[axis].spec_div).append("<br><select style='margin-left:25px; max-width: 260px;margin-top: -5px;' id='" + ids.sidebar[axis].clin_attr + "'>");
        $.each(metaData.getClinAttrsMeta(), function(index, obj) {
            $("#" + ids.sidebar[axis].clin_attr).append(
                    "<option value='" + obj.id + "'>" + obj.name + "</option>");
        });
        $("#" + ids.sidebar[axis].clin_attr).append("</select>");
    }

    return {
        init: function(axis) {
            $("#" + ids.sidebar[axis].spec_div).empty();
            appendAttrList(axis);
        }
    };
}());