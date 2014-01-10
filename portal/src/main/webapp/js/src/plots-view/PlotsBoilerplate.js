//Collection of default setting/data objects
var PlotsBoilerplate = {
    datum : {
        x_val: "",
        y_val: "",
        case_id: "",
        qtip: ""
    },
    style : {
        stroke: "#58ACFA",
        fill: "#0174DF"
    },
    canvas : {  //position of components
        width: 580,
        height: 580,
        xLeft: 60,     //The left/starting point for x axis
        xRight: 560,   //The right/ending point for x axis
        yTop: 20,      //The top/ending point for y axis
        yBottom: 520   //The bottom/starting point for y axis
    },
    elem : {
        svg: "",
        xScale: "",
        yScale: "",
        xAxis: "",
        yAxis: ""
    },
    names: { //naming conventions
        header: "_header",
        body: "_body"
    }
};
