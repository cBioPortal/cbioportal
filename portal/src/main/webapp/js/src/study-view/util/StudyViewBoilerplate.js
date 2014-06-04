/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var StudyViewBoilerplate ={
    headerCaseSelectCustomDialog: {
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
    chartColors: [
        "#2986e2","#dc3912","#f88508","#109618",
        "#990099","#0099c6","#dd4477","#66aa00",
        "#b82e2e","#316395","#994499","#22aa99",
        "#aaaa11","#6633cc","#e67300","#8b0707",
        "#651067","#329262","#5574a6","#3b3eac",
        "#b77322","#16d620","#b91383","#f4359e",
        "#9c5935","#a9c413","#2a778d","#668d1c",
        "#bea413","#0c5922","#743411","#743440"
    ],
    pieLabelQtip: {
        content:{text: ""},
        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'right bottom',at:'top left', viewport: $(window)}
    },
    warningQtip: {
        overwrite: true,
        content: {
            text: $("")
        },
        position: {
            my: 'left bottom',
            at: 'top right',
            target: '',
            viewport: $(window)
        },
        show: { 
            when: false, 
            ready: true,
            event: function(){
                $(this).qtip('hide');
            }
        }, 
        hide: { 
            delay: 2000
        },
        style: {
            tip: true,
            classes: 'qtip-red'
        },
        event: {
            hide: function(event, api){
                api.destroy();
            }
        }
    },
    scatterPlotDataAttr: {
        min_x: 0,
        max_x: 0,
        min_y: 0,
        max_y: 0,
        mut_x : false, //have case(s) mutated in only gene x
        mut_y : false,  //have case(s) mutated in only gene y
        mut_both: false //have case(s) mutated in both genes
    },

    scatterPlotOptions: {
        canvas: {  //position of components
            /* //For Scatter Plot with 
            width: 430,
            height: 350,
            xLeft: 100,     //The left/starting point for x axis
            xRight: 415,   //The right/ending point for x axis
            yTop: 10,      //The top/ending point for y axis
            yBottom: 280   //The bottom/starting point for y axis
            */
           
            width: 370,
            height: 320,
            xLeft: 80,     //The left/starting point for x axis
            xRight: 350,   //The right/ending point for x axis
            yTop: 10,      //The top/ending point for y axis
            yBottom: 240   //The bottom/starting point for y axis
        },
        style: { //Default style setting
            fill: "#2986e2", 
            stroke: "#2986e2",
            stroke_width: "0",
            size: "60",
            shape: "circle" //default, may vary for different mutation types
        },

        names: { 
            div: "study-view-scatter-plot",
            header: "study-view-scatter-plot-header",
            body: "study-view-scatter-plot-body-svg",
            loading_img: "study-view-scatter-plot-loading-img",
            control_panel: "study-view-scatter-plot-control-panel",
            log_scale_x: "study-view-scatter-plot-log-scale-x",
            log_scale_y: "study-view-scatter-plot-log-scale-y",
            download_pdf: "study-view-scatter-plot-pdf",
            download_svg: "study-view-scatter-plot-svg"         
        },
        elem: {
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
            title: "Mutation Count vs CNA",
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
    },
    
    headerLeftDiv: function() {
        var _header = $('<div></div>'),
//            _span1 = $('<span></span>'),
//            _span2 = $('<span></span>'),
            _span3 = $('<span></span>'),
            _span1 = $("<input type='button' />"),
            _span2 = $("<input type='button' />"),
//            _span3 = $("<input type='button' />"),
            _form = $('<form></form>'),
            _input1 = $('<input></input>'),
            _input2 = $('<input></input>'),
            _input3 = $('<input></input>'),
            _input4 = $('<input></input>');
        
        _header.attr('id','study-view-header-left');
        _span1
            .attr({
                'id': 'study-view-header-left-0',
                'class': 'study-view-header-button'})
//                'class': 'study-view-header study-view-header-left'})
//            .text('Select cases by IDs');
            .val('Select cases by IDs');
        _form
            .attr({
                method: "post",
                action: "index.do",
            })
            .css({
                float: "left"
            });   
        _input1
            .attr({
                type: "hidden",
                name: "cancer_study_id",
                id: "study-view-header-left-cancer_study-ids",
                value: ""
            });
        _input2
            .attr({
                type: "hidden",
                name: "case_set_id",
                value: "-1"
            });
        _input3
            .attr({
                type: "hidden",
                id: "study-view-header-left-case-ids",
                name: "case_ids",
                value: ""
            });
        _input4
            .attr({
                type: "submit",
                id: "study-view-header-left-1",
                value: "Query selected cases",
//                class: "study-view-header hidden"
                class: "hidden study-view-header-button"
            });
        _form.append(_input1);
        _form.append(_input2);
        _form.append(_input3);
        _form.append(_input4);
        
        _span2
            .attr({
                'id': 'study-view-header-left-2',
//                'class': 'study-view-header hidden'})
//            .text('Reset all');
                'class': 'hidden study-view-header-button'})
            .val('Reset all');
        
        _span3
            .attr({
                'id': 'study-view-header-left-3',
                'class': 'hidden'})
            .text('');
//            .val('Reset all');
       
        _header.append(_span1);
        _header.append(_form);
        _header.append(_span2);
        _header.append(_span3);
        
        return _header;
    },
    
    customDialogDiv:
            "<div class='hidden' id='study-view-case-select-custom-dialog'>" +
                "Please input case IDs (one per line)" +
                "<textarea rows='20' cols='50' id='study-view-case-select-custom-input'></textarea><br/>" +
                "<button type='button' id='study-view-case-select-custom-submit-btn'>Select</button>" +
            "</div>",
    addChartDiv:
            "<select id='study-view-add-chart'><option id=''>Add Chart</option></select>",
//            "<div  id='study-view-add-chart' class='study-view-header'>" +
//                "<span>Add Chart</span><br>" +
//                "<ul>" +
//                "</ul>" +
//            "</div>",
    
    tutorialDiv:
            "<div  id='study-view-tutorial' class='study-view-header'>" +
                "<span>Tutorial</span>" +
            "</div>",
    
    updateTableDiv: 
            "<div style='float: left'>" +
            "<input type='button' "+
                "id='study-view-dataTable-updateTable' "+
                "class='study-view-middle-button' "+
                "value = 'Update Table'/>" +
            "</div>",
    
    updateChartsDiv: 
            "<div style='float: left'>" +
            "<input type='button' "+
                "id='study-view-dataTable-updateCharts' "+
                "class='study-view-middle-button' "+
                "value = 'Update Charts'/>" +
            "</div>",
    
    upArrowDiv:
            "<img src='images/arrow_top.png' " +
                "height='25px' alt='Update Charts' " +
                "style='float: left'/>",
    
    downArrowDiv:
            "<img src='images/arrow_bottom.png' " +
                "height='25px' alt='Update Table' " +
                "style='float: left'/>",
        
    scatterPlotDiv: 
            "<div id='study-view-scatter-plot' class='study-view-dc-chart w2 h1half'"+
            "data-step='1' data-intro='Scatter Plot<br/>x: CNA<br/>y: MUTATIONS COUNT'>" +
            "<div id='study-view-scatter-plot-header-wrapper' style='float:right; width: 100%; height: 22px;'>"+
            "<chartTitleH4 id='study-view-scatter-plot-title'>"+
            "Mutation Count vs CNA</chartTitleH4>"+
            "<div id='study-view-scatter-plot-header'>"+
            
//            "<form style='display:inline-block; margin-right:5px; float:left' action='svgtopdf.do' method='post' id='study-view-scatter-plot-pdf'>"+
//            "<input type='hidden' name='svgelement' id='study-view-scatter-plot-pdf-value'>"+
//            "<input type='hidden' name='filetype' value='pdf'>"+
//            "<input type='hidden' id='study-view-scatter-plot-pdf-name' name='filename' value=''>"+
//            "<input type='submit' style='font-size:10px' value='PDF'>"+          
//            "</form>"+
//            
//            "<form style='display:inline-block; margin-right:5px; float:left' action='svgtopdf.do' method='post' id='study-view-scatter-plot-svg'>"+
//            "<input type='hidden' name='svgelement' id='study-view-scatter-plot-svg-value'>"+
//            "<input type='hidden' name='filetype' value='svg'>"+
//            "<input type='hidden' id='study-view-scatter-plot-svg-name' name='filename' value=''>"+
//            "<input type='submit' style='font-size:10px' value='SVG'>"+    
//            "</form>"+
            
//            "<img id='study-view-scatter-plot-menu-icon' class='study-view-menu-icon' style='float:left; width:10px; height:10px;margin-top:4px; margin-right:4px;' class='study-view-menu-icon' src='images/menu.svg'/>"+
            "<img id='study-view-scatter-plot-download-icon' class='study-view-download-icon' src='images/in.svg'/>" +
            "<img style='width:10px; height:10px;margin-top:4px; margin-right:4px;float:left;' class='study-view-drag-icon' src='images/move.svg'/>"+
            "<span style='float:left;' class='study-view-chart-plot-delete study-view-scatter-plot-delete'>x</span>"+
            "</div></div>"+
            
            "<div id='study-view-scatter-plot-body'>"+
            "<div id='study-view-scatter-plot-body-top-chart'></div>"+
            "<div id='study-view-scatter-plot-body-svg'></div>"+
            "<div id='study-view-scatter-plot-body-right-chart'></div></div>"+
//            "<div id='study-view-scatter-plot-side'>"+
//            "<div class='study-view-side-item'><input type='checkbox' id='study-view-scatter-plot-log-scale-x'></input><span class='study-view-scatter-plot-checkbox'>Log Scale X</span></div>"+
//            "<div class='study-view-side-item'><input type='checkbox' id='study-view-scatter-plot-log-scale-y'></input><span class='study-view-scatter-plot-checkbox'>Log Scale y</span></div>"+
//            "</div>"+
            "<div id='study-view-scatter-plot-loader' class='study-view-loader'>"+
            "<img src='images/ajax-loader.gif'/></div>"+
            "<div id='study-view-scatter-plot-control-panel'></div>"+
            "</div>",
    
    wordCloudDiv:
            "<div id='study-view-word-cloud' "+
            "class='study-view-dc-chart study-view-word-cloud'>" +
            "<div id='study-view-word-cloud-side' class='study-view-pdf-svg-side'>"+
            "<form style='display:inline-block;' action='svgtopdf.do' method='post' id='study-view-word-cloud-pdf'>"+
            "<input type='hidden' name='svgelement' id='study-view-word-cloud-pdf-value'>"+
            "<input type='hidden' name='filetype' value='pdf'>"+
            "<input type='hidden' id='study-view-word-cloud-pdf-name' name='filename' value=''>"+
            "<input type='submit' style='font-size:10px' value='PDF'>"+          
            "</form>"+
            "<form style='display:inline-block' action='svgtopdf.do' method='post' id='study-view-word-cloud-svg'>"+
            "<input type='hidden' name='svgelement' id='study-view-word-cloud-svg-value'>"+
            "<input type='hidden' name='filetype' value='svg'>"+
            "<input type='hidden' id='study-view-word-cloud-svg-name' name='filename' value=''>"+
            "<input type='submit' style='font-size:10px' value='SVG'>"+    
            "</form></div>"+
            "<div id='study-view-word-cloud-title'>" +
            "<chartTitleH4>Mutated Genes</chartTitleH4>" +
            "<span class='study-view-chart-cloud-delete study-view-word-cloud-delete' "+
            "style = 'float:right;'>x</span><div style='width:14px; height:16px;float:right'>"+
            "<img style='width:10px; height:10px;margin-top:4px; margin-right:4px;' class='study-view-drag-icon' src='images/move.svg'/>"+
            "</div></div>" +
            "<div id='study-view-word-cloud-loader' style='width: 100%; display:none; text-align:center'>"+
            "<img src='images/ajax-loader.gif'/></div>"+
            "</div>",
    dataTableDiv: 
            "<table id='dataTable'>"+
            "<tfoot>"+
            "<tr>"+
            "</tr>"+
            "</tfoot>"+
            "</table>"
    
};