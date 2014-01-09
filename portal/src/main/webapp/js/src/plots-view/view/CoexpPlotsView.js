var CoexpPlotsView = (function() {

    var style = {},
        canvas = {},
        elem = {},
        names = {};

    function init(divName, geneX, geneY) {
        style = jQuery.extend(true, {}, PlotsBoilerplate.getStyle());
        canvas = jQuery.extend(true, {}, PlotsBoilerplate.getCanvas());
        elem = jQuery.extend(true, {}, PlotsBoilerplate.getElem());
        names = jQuery.extend(true, {}, PlotsBoilerplate.getNames(divName));
        ScatterPlots.init(style, canvas, elem, divName, CoexpPlotsProxy.getData());
    }

    function initDiv(divName) {
        $("#" + divName + "_plot_loading_img").hide();
        $("#" + divName).append("<div id='" + names.header + "' style='padding-left: 100px; padding-top: 30px;'></div>");
        $("#" + divName).append("<div id='" + names.plots + "'></div>")
    }

    function show() {
        ScatterPlots.show();
    }

    function update() {
        ScatterPlots.update();
    }

    return {
        init: init,
        show: show,
        update: update
    }

}());