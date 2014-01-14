//Collection of default setting/data objects
var PlotsBoilerplate = {
    datum : {
        x_val: "",
        y_val: "",
        case_id: "",
        qtip: ""
    },
    style : {
        fill: "#58ACFA",
        stroke: "#0174DF",
        stroke_width: "1.2",
        size: "20",
        shape: "circle"
    },
    canvas : {  //position of components
        width: 600,
        height: 595,
        xLeft: 90,     //The left/starting point for x axis
        xRight: 590,   //The right/ending point for x axis
        yTop: 20,      //The top/ending point for y axis
        yBottom: 520   //The bottom/starting point for y axis
    },
    elem : {
        svg: "",
        xScale: "",
        yScale: "",
        xAxis: "",
        yAxis: "",
        dotsGroup: ""
    },
    names: { //naming conventions
        header: "_header", 
        body: "_body",  // the actual plots
        div: "" // the overall div name
    }
};
