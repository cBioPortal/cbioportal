//Collection of default setting/data objects

var PlotsBoilerplate = (function() {

    var options = {


        },
        datum = {
            x_val: "",
            y_val: "",
            case_id: "",
            qtip: ""
        },
        style = {
            stroke: "#58ACFA",
            fill: "#0174DF"
        },
        text = {

        },
        canvas = {
            width: 580,
            height: 580,
            xLeft: 60,     //The left/starting point for x axis
            xRight: 560,   //The right/ending point for x axis
            yTop: 20,      //The top/ending point for y axis
            yBottom: 520   //The bottom/starting point for y axis
        },
        elem = {
            svg: "",
            xScale: "",
            yScale: "",
            xAxis: "",
            yAxis: ""
        },
        names = {
            header: "_header",
            body: "_body"
        }

    return {
        getOptions: function() {
            return options;
        },
        getDatum : function() {
            return datum;
        },
        getStyle: function() {
            return style;
        },
        getText: function() {
            return text;
        },
        getCanvas: function() {
            return canvas;
        },
        getElem: function() {
            return elem;
        },
        getNames: function(divName) {
            names.header = divName + names.header;
            names.body = divName + names.body;
            return names;
        }
    }

}());