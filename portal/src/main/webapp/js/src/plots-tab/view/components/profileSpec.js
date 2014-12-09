var profileSpec = (function() {
        
    function appendGeneList(axis) {
        $("#" + ids.sidebar[axis].div).append("<h5>Gene</h5>");
        $("#" + ids.sidebar[axis].div).append("<select id='" + ids.sidebar[axis].gene + "'>");
        $.each(window.PortalGlobals.getGeneList(), function(index, value) {
            $("#" + ids.sidebar[axis].gene).append(
                    "<option value='" + value + "'>" + value + "</option>");
        });
        $("#" + ids.sidebar[axis].div).append("</select>");
    }
    
    return {
        init: function(axis) {
            appendGeneList(axis);
        }
    };
}());