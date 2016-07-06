/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


var StudyViewBoilerplate ={
    headerCaseSelectCustomDialog: {
        id: 'study-view-case-select-custom-dialog', // Since we're only creating one modal, give it an ID so we can style it
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
        "#bea413","#0c5922","#743411","#743440",
        "#9986e2","#6c3912","#788508","#609618",
        "#790099","#5099c6","#2d4477","#76aa00",
        "#882e2e","#916395","#794499","#92aa99",
        "#2aaa11","#5633cc","#667300","#100707",
        "#751067","#229262","#4574a6","#103eac",
        "#177322","#66d620","#291383","#94359e",
        "#5c5935","#29c413","#6a778d","#868d1c",
        "#5ea413","#6c5922","#243411","#103440",
        "#2886e2","#d93912","#f28508","#110618",
        "#970099","#0109c6","#d10477","#68aa00",
        "#b12e2e","#310395","#944499","#24aa99",
        "#a4aa11","#6333cc","#e77300","#820707",
        "#610067","#339262","#5874a6","#313eac",
        "#b67322","#13d620","#b81383","#f8359e",
        "#935935","#a10413","#29778d","#678d1c",
        "#b2a413","#075922","#763411","#773440",
        "#2996e2","#dc4912","#f81508","#104618",
        "#991099","#0049c6","#dd2477","#663a00",
        "#b84e2e","#312395","#993499","#223a99",
        "#aa1a11","#6673cc","#e66300","#8b5707",
        "#656067","#323262","#5514a6","#3b8eac",
        "#b71322","#165620","#b99383","#f4859e",
        "#9c4935","#a91413","#2a978d","#669d1c",
        "#be1413","#0c8922","#742411","#744440",
        "#2983e2","#dc3612","#f88808","#109518",
        "#990599","#0092c6","#dd4977","#66a900",
        "#b8282e","#316295","#994199","#22a499",
        "#aaa101","#66310c","#e67200","#8b0907",
        "#651167","#329962","#5573a6","#3b37ac",
        "#b77822","#16d120","#b91783","#f4339e",
        "#9c5105","#a9c713","#2a710d","#66841c",
        "#bea913","#0c5822","#743911","#743740",
        "#298632","#dc3922","#f88588","#109658",
        "#990010","#009916","#dd4447","#66aa60",
        "#b82e9e","#316365","#994489","#22aa69",
        "#aaaa51","#66332c","#e67390","#8b0777",
        "#651037","#329232","#557486","#3b3e4c",
        "#b77372","#16d690","#b91310","#f4358e",
        "#9c5910","#a9c493","#2a773d","#668d5c",
        "#bea463","#0c5952","#743471","#743450",
        "#2986e3","#dc3914","#f88503","#109614",
        "#990092","#0099c8","#dd4476","#66aa04",
        "#b82e27","#316397","#994495","#22aa93",
        "#aaaa14","#6633c1","#e67303","#8b0705",
        "#651062","#329267","#5574a1","#3b3ea5"
    ],
    pieLabelQtip: {
        content:{text: ""},
        style: { classes: 'qtip-light qtip-rounded qtip-shadow'  },
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
            yTitleHelp: "Number of somatic non-synonymous mutations"
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
    
    headerDiv: function() {
        var _header = $('<div></div>'),
            _headerLeft = $('<div></div>'),
            _headerRight = $('<div></div>'),

            _successBanner = $('<div></div>'),
            _breadcrumbs = $('<div></div>'),
            _span3 = $('<span></span>'),
            // span for the number of items found
            _span1 = $("<input type='button' />"),
            _span2 = $("<input type='button' />"),
            _span4 = $("<input type='button' />"),
            _span5 = $('<span></span>'),
            _span6 = $("<span></span>"),
            _form = $('<form id="study-view-form"></form>'),
            _input1 = $('<input></input>'),
            _input2 = $('<input></input>'),
            _input3 = $('<input></input>'),
            _input4 = $('<input></input>'),
            _queryByGene = $('<span></span>');
        
        _headerLeft.attr('id','study-view-header-left');

        _span1
            .attr({
                'id': 'study-view-header-right-1',
                'class': 'study-view-header-button',
                'role': 'button',
                'tabindex': '0'
            })
            .val('Select cases by IDs');

        // Build query button
        _form
            .attr({
                method: "post",
                action: "index.do",
                target: "_blank"
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
                id: "study-view-header-left-case_set-id",
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
                value: "Query",
                class: "study-view-header-button"
            });
        _form.append(_input1);
        _form.append(_input2);
        _form.append(_input3);
        _form.append(_input4);


        //Clear all button moved to after breadcrumbs
        _span2
            .attr({
                'id': 'study-view-header-left-2',
                'class': 'btn btn-default btn-xs study-view-header-clear-all'})
            .val('Clear all');

        // changed the initialisation for span 3
        _span3
            .attr({
                'id': 'study-view-header-left-3'
            })
            .text('Samples selected: ');

        // span5 attributes
        _span5.attr({
            'id': 'study-view-header-left-5',
            'class': 'study-view-header-button',
            'role': 'button',
            'tabindex': '0',
        });

        //Download button
        _span6
            .attr({
                'id': 'study-view-header-left-6',
                'class': 'study-view-header-button',
                'role': 'button',
                'tabindex': '0',
            })
            .html('<img src="images/in.svg" alt="download" />');

        _successBanner
            .attr({
               id: 'successBanner',
               class: 'alert alert-success fade in',
               style: 'display: none'
            });

        _headerLeft.append(_span3);
        _headerLeft.append(_span5);
        _headerLeft.append(_span6);
        _headerLeft.append(_queryByGene);
        _headerLeft.append("<img id='arrow_studyview' src='images/arrow_studyview.png' alt='' />");
        _headerLeft.append(_form);

        _headerRight.attr('id','study-view-header-right');
        _headerRight.append(_span1);

        _header.append(_headerLeft);
        _header.append(_headerRight);


        _queryByGene
            .attr({
               'id': 'query-by-gene-span'
            });

        var queryByGeneTextArea = $('<textarea></textarea>');
        queryByGeneTextArea
            .attr({
                'id': 'query-by-gene-textarea',
                'class': 'expand expandFocusOut',
                'rows': '1',
                'cols': '10',
                'title': 'query genes'
            });

        _queryByGene.append('<span id="queryByGeneTextSpan"></span>');
        _queryByGene.append(queryByGeneTextArea);

        // add a container for the breadcrumbs
        _breadcrumbs.attr({
            id: 'breadcrumbs_container',
            class: 'study-view-hidden'
        });
        _breadcrumbs.append('<span style="float:left">Your selections: </span>');
        _breadcrumbs.append('<div style="float:left" class="breadcrumbs_items"></div>');
        _breadcrumbs.children('.breadcrumbs_items').append(_span2);

        _header.append("<br/>");
        _header.append(_breadcrumbs);

        return _header;
    },
    
    customDialogDiv:
            "<div class='study-view-hidden' id='study-view-case-select-custom-dialog'>" +
                '<label>Please input IDs (one per line)' +
                "<textarea rows='20' cols='50' id='study-view-case-select-custom-input'></textarea></label><br/>" +
                '<label><input type="radio" id="study-view-case-select-custom-radio-sample" name="study-view-case-select-custom-radio" value="sample" checked>' +
                'By sample ID</label>' +
                '<label><input type="radio" id="study-view-case-select-custom-radio-patient"  name="study-view-case-select-custom-radio" value="patient">' +
                'By patient ID</label>' +
                "<button type='button' id='study-view-case-select-custom-submit-btn' style='float: right;'>Select</button>" +
            "</div>",
    addChartDiv:
            "<select id='study-view-add-chart' class='chosen-select' title='Add Chart'><option id=''>Add Chart</option></select>",
    
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
            "<img id='study-view-scatter-plot-download-icon' class='study-view-download-icon' src='images/in.svg' alt='download' />" +
            "<img style='width:10px; height:10px;margin-top:4px; margin-right:4px;float:left;' class='study-view-drag-icon' src='images/move.svg' alt='move' />"+
            "<span style='float:left;' class='study-view-chart-plot-delete study-view-scatter-plot-delete'>x</span>"+
            "</div></div>"+
            
            "<div id='study-view-scatter-plot-body'>"+
            "<div id='study-view-scatter-plot-body-top-chart'></div>"+
            "<div id='study-view-scatter-plot-body-svg'></div>"+
            "<div id='study-view-scatter-plot-body-right-chart'></div></div>"+
            "<div id='study-view-scatter-plot-loader' class='study-view-loader'>"+
            "<img src='images/ajax-loader.gif' alt='loading' /></div>"+
            "<div id='study-view-scatter-plot-control-panel'></div>"+
            "</div>",
    
    dataTableDiv: 
            "<table id='dataTable'>"+
            "<tfoot>"+
            "<tr>"+
            "</tr>"+
            "</tfoot>"+
            "</table>",

    // added for breadcrumbs
    breadCrumbDiv:
        "<div class='breadcrumb_container'>"+
            "<span class='breadcrumb_item'></span>"+
            "<img class='breadcrumb_remove' src='images/remove_breadcrumb_icon.png' alt='remove' />"+
        "</div>"

};
