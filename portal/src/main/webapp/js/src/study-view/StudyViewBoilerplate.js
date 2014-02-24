/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var StudyViewBoilerplate ={
    headerCaseSelectCustomDialog :{
        id: 'modal', // Since we're only creating one modal, give it an ID so we can style it
        content: {
                text: '',
                title: {
                        text: 'Custom case selection',
                        button: true
                }
        },
        position: {
                my: 'center', // ...at the center of the viewport
                at: 'center',
                target: ''
        },
        show: {
                event: 'click', // Show it on click...
                solo: true // ...and hide all other tooltips...
        },
        hide: false,
        style: 'qtip-light qtip-rounded qtip-wide'
    },
    pieLabelQtip: {
        content:{text: ""},
        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'right bottom',at:'top left'}
    },
    scatterPlotDataAttr : {
        min_x: 0,
        max_x: 0,
        min_y: 0,
        max_y: 0,
        mut_x : false, //have case(s) mutated in only gene x
        mut_y : false,  //have case(s) mutated in only gene y
        mut_both: false //have case(s) mutated in both genes
    },

    scatterPlotOptions: {
        canvas : {  //position of components
            width: 560,
            height: 440,
            xLeft: 100,     //The left/starting point for x axis
            xRight: 500,   //The right/ending point for x axis
            yTop: 10,      //The top/ending point for y axis
            yBottom: 350   //The bottom/starting point for y axis
        },
        style : { //Default style setting
            fill: "#3366cc", //light blue
            stroke: "#0174DF", //dark blue
            stroke_width: "1.2",
            size: "60",
            shape: "circle" //default, may vary for different mutation types
        },

        names: { 
            div: "study-view-scatter-plot",
            header: "study-view-scatter-plot-header",
            body: "study-view-scatter-plot-body",
            loading_img: "study-view-scatter-plot-loading-img",
            control_panel: "study-view-scatter-plot-control-panel",
            log_scale_x: "study-view-scatter-plot-log-scale-x",
            log_scale_y: "study-view-scatter-plot-log-scale-y",
            download_pdf: "study-view-scatter-plot-pdf",
            download_svg: "study-view-scatter-plot-svg"         
        },
        elem : {
            svg: "",
            xScale: "",
            yScale: "",
            xAxis: "",
            yAxis: "",
            dotsGroup: "",
            axisGroup: "",
            axisTitleGroup: "",
            brush: ""
        },
        text: {
            xTitle: "Fraction of copy number altered genome",
            yTitle: "# of mutations",
            title: "Mutation Count vs Copy Number Alterations",
            fileName: "",
            xTitleHelp: "Fraction of genome that has log2 copy number value above 0.2 or bellow -0.2",
            yTitleHelp: "Number of sometic non-synonymous mutations"
        },
        legends: []
    },
    scatterPlotDatum: {              
        x_val: "",
        y_val: "",
        case_id: "",
        qtip: "",
        stroke: "",
        fill: ""
    }
}
 