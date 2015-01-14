var sidebar = (function() {
    
    var render = function() {
        profileSpec.init("x");
        profileSpec.init("y");
        optSpec.init();
        //reset the default value of x: default is always x copy num, y mrna
        document.getElementById(ids.sidebar.x.profile_type).selectedIndex = "1";
        profileSpec.updateProfileNameList("x");
    };
    
    var listener = function() {
        
        //listener on data types
        $("#" + ids.sidebar.x.data_type).change(function() {
            if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.genetic) {
                profileSpec.init("x");
                optSpec.init();
                $("#" + ids.sidebar.x.gene).change(function() {
                    profileSpec.update("x");
                    optSpec.update();
                });
            } else if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.clin) {
                clinSpec.init("x");
            }
        });
        $("#" + ids.sidebar.y.data_type).change(function() {
            if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.genetic) {
                profileSpec.init("y");
                optSpec.init();
                
                $("#" + ids.sidebar.y.gene).change(function() {
                    profileSpec.update("y");
                    optSpec.init();
                });
            } else if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.clin) {
                clinSpec.init("y");
            }
        });  
        
        //listener on genes
        $("#" + ids.sidebar.x.gene).change(function() {
            profileSpec.update("x");
            optSpec.update();
        });
        $("#" + ids.sidebar.y.gene).change(function() {
            profileSpec.update("y");
            optSpec.update();
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