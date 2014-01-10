var CoexpPlotsView = (function() {

    var style = {},
        canvas = {},
        elem = {},
        names = {};

    function settings() {
        //css style
        style = jQuery.extend(true, {}, PlotsBoilerplate.style);
        //positions
        canvas = jQuery.extend(true, {}, PlotsBoilerplate.canvas);
        //svg elements
        elem = jQuery.extend(true, {}, PlotsBoilerplate.elem);
        //div ids
        names = jQuery.extend(true, {}, PlotsBoilerplate.names);
        names.header = divName + names.header;
        names.body = divName + names.body;   //the actual svg plots
    }

    function layout(divName) {
        $("#" + divName).append("<div id='" + names.header + "' style='padding-left: 100px; padding-top: 30px;'></div>");
        $("#" + divName).append("<div id='" + names.plots + "'></div>");
    }

    function show() {
        ScatterPlots.init();
    }

    function update() {
        ScatterPlots.update();
    }

    return {
        init: function(divName, geneX, geneY) {
            settings(divName);
            layout(divName);

        },
        show: show,
        update: update
    }

}());