var sidebar = (function() {
    
    var render = function() {
        profileSpec.init("x");
        profileSpec.init("y");
    };
    
    var listener = function() {
        $("#" + ids.sidebar.x.data_type).change(function() {
            if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.genetic) {
                profileSpec.init("x");
            } else if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.clin) {
                clinSpec.init("x");
            }
        });
        $("#" + ids.sidebar.y.data_type).change(function() {
            if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.genetic) {
                profileSpec.init("y");
            } else if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.clin) {
                clinSpec.init("y");
            }
        });        
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