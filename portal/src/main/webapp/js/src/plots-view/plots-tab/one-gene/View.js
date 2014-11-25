    var View = (function() {

        var elem = {
                svg : "",
                elemDotsGroup : "",
                boxPlots: ""
            },   //DOM elements
            settings = {
                canvas_width: 750,
                canvas_height: 600
            },   //basic d3 canvas settings
            attr = {
                xScale : "",
                yScale : ""
            };

        var Axis = (function() {

            var xAxis = "",
                yAxis = "",
                xTitle = "",
                yTitle = "",
                xTitleHelp = "",
                yTitleHelp = "";

            function getAxisTitles() {
                //TODO: Change hard-coded menu items value
                if (Util.plotsTypeIsCopyNo()) {
                    var e = document.getElementById("data_type_copy_no");
                    xTitle = PlotsView.getUserSelection().gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_mrna");
                    yTitle = PlotsView.getUserSelection().gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                } else if (Util.plotsTypeIsMethylation()) {
                    var e = document.getElementById("data_type_dna_methylation");
                    xTitle = PlotsView.getUserSelection().gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_mrna");
                    yTitle = PlotsView.getUserSelection().gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                } else if (Util.plotsTypeIsRPPA()) {
                    var e = document.getElementById("data_type_mrna");
                    xTitle = PlotsView.getUserSelection().gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_rppa");
                    yTitle = PlotsView.getUserSelection().gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                } else if (Util.plotsTypeIsClinical()) {
                    e = document.getElementById("data_type_clinical");
                    xTitle = PlotsView.getUserSelection().gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    var e = document.getElementById("data_type_mrna");
                    yTitle = PlotsView.getUserSelection().gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                }
            }

            function initClinicalDiscretizedAxis() {
                var _dataAttr = PlotsData.getDataAttr();
                var min_x = _dataAttr.min_x;
                var max_x = _dataAttr.max_x;
                if (min_x !== max_x) {
                    var edge_x = (max_x - min_x) * 0.5;
                } else {
                    var edge_x = 0.5;
                }
                var min_y = _dataAttr.min_y;
                var max_y = _dataAttr.max_y;
                var edge_y = (max_y - min_y) * 0.1;
                attr.xScale = d3.scale.linear()
                    .domain([min_x - edge_x, max_x + edge_x])
                    .range([100, 600]);
                xAxis = d3.svg.axis()
                    .scale(attr.xScale)
                    .orient("bottom")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
                attr.yScale = d3.scale.linear()
                    .domain([min_y - edge_y, max_y + edge_y])
                    .range([520, 20]);
                yAxis = d3.svg.axis()
                    .scale(attr.yScale)
                    .orient("left")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);               
            }

            function initGisticAxis() {
                var _dataAttr = PlotsData.getDataAttr();
                var min_y = _dataAttr.min_y;
                var max_y = _dataAttr.max_y;
                //reset max_x as the range of slots
                // -- Not real max x value for scaling!!
                var slotsCnt = 0;
                var tmp_copy_no = [];
                $.each(PlotsData.getDotsGroup(), function(index, value) {
                    tmp_copy_no.push(value.xVal);
                });
                for (var j = -2; j < 3; j++) {
                    if (tmp_copy_no.indexOf(j.toString()) !== -1) {
                        slotsCnt += 1;
                    }
                }
                //Set the domain range for different cases
                var new_min_x, new_max_x;
                if (slotsCnt === 1) {
                    new_min_x = -0.5;
                    new_max_x = 0.5;
                } else if (slotsCnt === 2) {
                    new_min_x = -0.8;
                    new_max_x = 1.8;
                } else if (slotsCnt === 3) {
                    new_min_x = -0.8;
                    new_max_x = 2.8;
                } else if (slotsCnt === 4) {
                    new_min_x = -0.6;
                    new_max_x = 3.6;
                } else if (slotsCnt === 5) {
                    new_min_x = -0.6;
                    new_max_x = 4.6;
                }
                var edge_y = (max_y - min_y) * 0.1;
                //Define the axis
                attr.xScale = d3.scale.linear()
                    .domain([new_min_x, new_max_x])
                    .range([100, 600]);
                attr.yScale = d3.scale.linear()
                    .domain([min_y - edge_y, max_y + edge_y])
                    .range([520, 20]);
                xAxis = d3.svg.axis()
                    .scale(attr.xScale)
                    .orient("bottom")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
                yAxis = d3.svg.axis()
                    .scale(attr.yScale)
                    .orient("left")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function drawGisticAxis() {
                var textSet = [];
                var svg = elem.svg;
                var tmp_copy_no = [];
                $.each(PlotsData.getDotsGroup(), function(index, value) {
                    tmp_copy_no.push(value.xVal);
                });
                for (var j = -2; j < 3; j++) {
                    if (tmp_copy_no.indexOf(j.toString()) !== -1) {
                        textSet.push(PlotsView.getText().gistic_txt_val[j.toString()]);
                    }
                }
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 520)")
                    .attr("class", "plots-x-axis-class")
                    .call(xAxis.ticks(textSet.length))
                    .selectAll("text")
                    .data(textSet)
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black")
                    .text(function(d){return d;});
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 20)")
                    .call(xAxis.orient("bottom").ticks(0));
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(100, 0)")
                    .attr("class", "plots-y-axis-class")
                    .call(yAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(600, 0)")
                    .call(yAxis.orient("left").ticks(0));
            }

            function initContinuousAxisX() {
                var _dataAttr = PlotsData.getDataAttr();
                var min_x = _dataAttr.min_x;
                var max_x = _dataAttr.max_x;
                var edge_x = (max_x - min_x) * 0.2;
                if (Util.plotsTypeIsMethylation()){
                    //Range for DNA Methylation Data Type
                    //Need to be fixed as from 0 to 1.
                    attr.xScale = d3. scale.linear()
                        .domain([-0.02, 1.02])
                        .range([100,600]);
                } else {
                    attr.xScale = d3.scale.linear()
                        .domain([min_x - edge_x, max_x + edge_x])
                        .range([100, 600]);
                }
                xAxis = d3.svg.axis()
                    .scale(attr.xScale)
                    .orient("bottom")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function initContinuousAxisY() {
                var _dataAttr = PlotsData.getDataAttr();
                var min_y = _dataAttr.min_y;
                var max_y = _dataAttr.max_y;
                var edge_y = (max_y - min_y) * 0.1;
                attr.yScale = d3.scale.linear()
                    .domain([min_y - edge_y, max_y + edge_y])
                    .range([520, 20]);
                yAxis = d3.svg.axis()
                    .scale(attr.yScale)
                    .orient("left")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function drawContinuousAxisMainX() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 520)")
                    .attr("class", "plots-x-axis-class")
                    .call(xAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
            }

            function drawClinicalDiscretizedAxisMainX() {
                var _min_x = PlotsData.getDataAttr().min_x,
                    _max_x = PlotsData.getDataAttr().max_x,
                    textSet = [];
                for (var i = _min_x; i < _max_x + 1; i++) {
                    textSet.push(clinicalDataTranslator.translateNum(PlotsView.getUserSelection().clinical_attribute, i.toString()));
                }
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 520)")
                    .attr("class", "plots-x-axis-class")
                    .call(xAxis.ticks(textSet.length))
                    .selectAll("text")
                    .data(textSet)
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black")
                    .text(function(d){return d;});
            }

            function drawContinuousAxisEdgeX() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 20)")
                    .call(xAxis.orient("bottom").ticks(0));
            }

            function drawContinuousAxisMainY() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(100, 0)")
                    .attr("class", "plots-y-axis-class")
                    .call(yAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
            }

            function drawContinuousAxisEdgeY() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(600, 0)")
                    .call(yAxis.orient("left").ticks(0));
            }

            function addXaxisTitle(axisTitleGroup, xTitle) {
                axisTitleGroup.append("text")
                    .attr("class", "x-axis-title")
                    .attr("x", 350)
                    .attr("y", 580)
                    .style("text-anchor", "middle")
                    .style("font-weight","bold")
                    .text(xTitle);

            }

            function addYaxisTitle(axisTitleGroup, yTitle) {
                axisTitleGroup.append("text")
                    .attr("class", "y-axis-title")
                    .attr("transform", "rotate(-90)")
                    .attr("x", -270)
                    .attr("y", 45)
                    .style("text-anchor", "middle")
                    .style("font-weight","bold")
                    .text(yTitle);

            }

            function addxAxisHelp(axisTitleGroup, _xTitle) {
                Plots.addxAxisHelp(
                    elem.svg,
                    axisTitleGroup,
                    _xTitle,
                    "x-title-help",
                    xTitleHelp
                );
            }

            function addyAxisHelp(axisTitleGroup, _yTitle) {
                Plots.addyAxisHelp(
                    elem.svg,
                    axisTitleGroup,
                    _yTitle,
                    "y-title-help",
                    yTitleHelp
                );

            }

            return {
                init: function() {
                    getAxisTitles();
                    if (PlotsView.getUserSelection().plots_type.indexOf("clinical") !== -1 &&
                        Util.plotsIsDiscretized()) {
                            initClinicalDiscretizedAxis();
                            drawClinicalDiscretizedAxisMainX();
                            drawContinuousAxisEdgeX();
                            drawContinuousAxisMainY();
                            drawContinuousAxisEdgeY();
                    } else if (Util.plotsIsDiscretized()) {
                        initGisticAxis();
                        drawGisticAxis();                            
                    } else {
                        initContinuousAxisX();
                        initContinuousAxisY();
                        drawContinuousAxisMainX();
                        drawContinuousAxisEdgeX();
                        drawContinuousAxisMainY();
                        drawContinuousAxisEdgeY();
                    }
                    var axisTitleGroup = elem.svg.append("svg:g")
                        .attr("class", "axis");
                    addXaxisTitle(axisTitleGroup, xTitle);
                    addYaxisTitle(axisTitleGroup, yTitle);
                    addxAxisHelp(axisTitleGroup, xTitle);
                    addyAxisHelp(axisTitleGroup, yTitle);  
                },
                getXHelp: function() {
                    return xTitleHelp;
                },
                getYHelp: function() {
                    return yTitleHelp;
                },
                updateLogScaleX: function(applyLogScale) {
                    d3.select("#plots_box").select(".plots-x-axis-class").remove();
                    d3.select("#plots_box").select(".x-axis-title").remove();
                    d3.select("#plots_box").select(".x-title-help").remove();
                    var _dataAttr = PlotsData.getDataAttr();
                    if (applyLogScale) {
                        if (_dataAttr.min_x <= (Plots.getLogScaleThresholdDown())) {
                            var min_x = Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2);
                        } else {
                            var min_x = Math.log(_dataAttr.min_x) / Math.log(2);
                        }
                        if (_dataAttr.max_x >= (Plots.getLogScaleThresholdUp())) {
                            var max_x = Math.log(Plots.getLogScaleThresholdUp()) / Math.log(2);
                        } else {
                            var max_x = Math.log(_dataAttr.max_x) / Math.log(2);
                        }
                        var edge_x = (max_x - min_x) * 0.2;
                        attr.xScale = d3.scale.linear()
                            .domain([min_x - edge_x, max_x + edge_x])
                            .range([100, 600]);
                        xAxis = d3.svg.axis()
                            .scale(attr.xScale)
                            .orient("bottom")
                            .tickSize(6, 0, 0)
                            .tickPadding([8]);
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addXaxisTitle(axisTitleGroup, xTitle + "(log2)");
                        addxAxisHelp(axisTitleGroup, xTitle + "(log2)");
                    } else {
                        initContinuousAxisX();
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addXaxisTitle(axisTitleGroup, xTitle);
                        addxAxisHelp(axisTitleGroup, xTitle);
                    }
                    drawContinuousAxisMainX();
                },
                updateLogScaleY: function(applyLogScale) {
                    d3.select("#plots_box").select(".plots-y-axis-class").remove();
                    d3.select("#plots_box").select(".y-axis-title").remove();
                    d3.select("#plots_box").select(".y-title-help").remove();
                    var _dataAttr = PlotsData.getDataAttr();
                    if (applyLogScale) {
                        if (_dataAttr.min_y <= (Plots.getLogScaleThresholdDown())) {
                            var min_y = Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2);
                        } else {
                            var min_y = Math.log(_dataAttr.min_y) / Math.log(2);
                        }
                        if (_dataAttr.max_y >= (Plots.getLogScaleThresholdUp())) {
                            var max_y = Math.log(Plots.getLogScaleThresholdUp()) / Math.log(2);
                        } else {
                            var max_y = Math.log(_dataAttr.max_y) / Math.log(2);
                        }
                        var edge_y = (max_y - min_y) * 0.1;
                        attr.yScale = d3.scale.linear()
                            .domain([min_y - edge_y, max_y + edge_y])
                            .range([520, 20]);
                        yAxis = d3.svg.axis()
                            .scale(attr.yScale)
                            .orient("left")
                            .tickSize(6, 0, 0)
                            .tickPadding([8]);
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addYaxisTitle(axisTitleGroup, yTitle + "(log2)");
                        addyAxisHelp(axisTitleGroup, yTitle + "(log2)");
                    } else {
                        initContinuousAxisY();
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addYaxisTitle(axisTitleGroup, yTitle);
                        addyAxisHelp(axisTitleGroup, yTitle);
                    }
                    drawContinuousAxisMainY();
                }
            };
        }());

        var Qtips = (function() {

            function confContent(d) {
                var content = "<font size='2'>";
                if (Util.plotsTypeIsCopyNo()) {
                    if (Util.plotsIsDiscretized()) {
                        content += "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    } else {
                        content += "CNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                            "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"+
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            +"' target = '_blank'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                } else if (Util.plotsTypeIsMethylation()) {
                    content += "Methylation: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                        "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid" && !Util.isEmpty(d.gisticType)) {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                } else if (Util.plotsTypeIsRPPA()) {
                    content += "mRNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                        "RPPA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid" && !Util.isEmpty(d.gisticType)) {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                } else if (Util.plotsTypeIsClinical()) {
                    content += "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid" && !Util.isEmpty(d.gisticType)) {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"+
                            + cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            +"' target = '_blank'>" + d.caseId + "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                }
                content = content + "</font>";
                return content;
            }

            return {
                init: function(){
                    elem.elemDotsGroup.selectAll("path").each(
                        function(d) {
                            var content = confContent(d);
                            $(this).qtip(
                                {
                                    content: {text: content},
                                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                                    show: {event: "mouseover"},
                                    hide: {fixed:true, delay: 100, event: "mouseout"},
                                    position: {my:'left bottom',at:'top right', viewport: $(window)}
                                }
                            );
                            if (Util.plotsTypeIsCopyNo()) {    //Handle special symbols
                                var mouseOn = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(200)
                                            .type(function(d){
                                                return mutationStyle[d.mutationType].symbol;
                                            })
                                        )
                                        .attr("fill", function(d){
                                            return mutationStyle[d.mutationType].fill;
                                        })
                                        .attr("stroke", function(d){
                                            return mutationStyle[d.mutationType].stroke;
                                        })
                                        .attr("stroke-width", 1.2);
                                };
                                var mouseOff = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol()
                                            .size(20)
                                            .type(function(d){
                                                return mutationStyle[d.mutationType].symbol;
                                            })
                                        )
                                        .attr("fill", function(d){
                                            return mutationStyle[d.mutationType].fill;
                                        })
                                        .attr("stroke", function(d){
                                            return mutationStyle[d.mutationType].stroke;
                                        })
                                        .attr("stroke-width", 1.2);
                                };
                                elem.elemDotsGroup.selectAll("path").on("mouseover", mouseOn);
                                elem.elemDotsGroup.selectAll("path").on("mouseout", mouseOff);
                            } else {
                                var mouseOn = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(200));
                                };
                                var mouseOff = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(35));
                                };
                                elem.elemDotsGroup.selectAll("path").on("mouseover", mouseOn);
                                elem.elemDotsGroup.selectAll("path").on("mouseout", mouseOff);
                            }
                        }
                    );
                }
            };
        }());

        var ScatterPlots = (function() {

            function drawClinicalDiscretizedPlots() {
                var ramRatio = 30; //Define Noise
                elem.elemDotsGroup.selectAll("path")
                    .data(PlotsData.getDotsGroup())
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d){
                        var _x = attr.xScale(d.xVal) + (Math.random() * ramRatio - ramRatio/2);
                        var _y = attr.yScale(d.yVal);
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("symbol", "circle");
                        $(this).attr("size", 35);
                        return "translate(" + _x + ", " + _y + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(35)
                        .type("circle"))
                    .attr("fill", function(d) {
                        switch (d.mutationType) {
                            case "non" : {$(this).attr("fill", "white");return "white";}
                            default: {$(this).attr("fill", "orange");return "orange";}
                        }
                    })
                    .attr("fill-opacity", function(d) {
                        switch (d.mutationType) {
                            case "non" : return 0.0;
                            default : return 1.0;
                        }
                    })
                    .attr("stroke", function(d) {
                        if (Util.hasCopyNumberData()) {
                            if (Util.isEmpty(d.gisticType)) {
                                return gisticStyle.Unknown.stroke;
                            } else {
                                return gisticStyle[d.gisticType].stroke;
                            }
                        } else {
                            return "black";
                        }
                    })
                    .attr("stroke-width", 1.2)
                    .attr("class", function(d) { return d.caseId; });
            }

            function drawGisticPlots() { //GISTIC, RAE view
                var ramRatio = 30;  //Noise
                //Divide Data Set by Gistic Type
                var subDataSet = {
                    Homdel : [],
                    Hetloss : [],
                    Diploid : [],
                    Gain : [],
                    Amp : []
                };
                $.each(PlotsData.getDotsGroup(), function(index, value) {
                    if (value.gisticType === "Homdel") {
                        subDataSet.Homdel.push(value);
                    } else if (value.gisticType === "Hetloss") {
                        subDataSet.Hetloss.push(value);
                    } else if (value.gisticType === "Diploid") {
                        subDataSet.Diploid.push(value);
                    } else if (value.gisticType === "Gain") {
                        subDataSet.Gain.push(value);
                    } else if (value.gisticType === "Amp") {
                        subDataSet.Amp.push(value);
                    }
                });
                //Remove empty data set
                $.each(subDataSet, function(key, value) {
                    if (subDataSet[key].length === 0) {
                        delete subDataSet[key];
                    }
                });
                var posVal = 0;    //Index for Positioning
                $.each(subDataSet, function(key, value) {
                    var subDotsGrp = elem.elemDotsGroup.append("svg:g");
                    subDotsGrp.selectAll("path")
                        .data(value)
                        .enter()
                        .append("svg:path")
                        .attr("class", function(d){ return d.caseId;})
                        .attr("transform", function(d){
                            var _x = attr.xScale(posVal) + (Math.random() * ramRatio - ramRatio/2);
                            var _y = attr.yScale(d.yVal);
                            $(this).attr("x_pos", _x);
                            $(this).attr("y_pos", _y);
                            $(this).attr("x_val", d.xVal);
                            $(this).attr("y_val", d.yVal);
                            $(this).attr("size", 20);
                            return "translate(" + _x + "," + _y + ")";
                        })
                        .attr("d", d3.svg.symbol()
                            .size(20)
                            .type(function(d){
                                $(this).attr("symbol", mutationStyle[d.mutationType].symbol);
                                return mutationStyle[d.mutationType].symbol;
                            })
                        )
                        .attr("fill", function(d){
                            $(this).attr("fill", mutationStyle[d.mutationType].fill);
                            return mutationStyle[d.mutationType].fill;
                        })
                        .attr("stroke", function(d){
                            $(this).attr("stroke", mutationStyle[d.mutationType].stroke);
                            return mutationStyle[d.mutationType].stroke;
                        })
                        .attr("stroke-width", 1.2);
                    posVal += 1;
                });
            }

            function drawBoxPlots(applyLogScale){
                d3.select("#plots_box").select(".box_plots").remove();
                var boxPlotsElem = elem.boxPlots.append("svg:g").attr("class", "box_plots");
                var _dotsGroup = [];
                _dotsGroup.length = 0;
                _dotsGroup = jQuery.extend(true, {}, PlotsData.getDotsGroup());
                if (applyLogScale) {
                    $.each(_dotsGroup, function(index, value) {
                        if (value.yVal <= (Plots.getLogScaleThresholdDown())) {
                            value.yVal = Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2);
                        } else if (value.yVal >= (Plots.getLogScaleThresholdUp())) {
                            value.yVal = Math.log(Plots.getLogScaleThresholdUp() / Math.log(2));
                        } else {
                            value.yVal = Math.log(value.yVal) / Math.log(2);
                        }
                    });
                }

                var min_x = PlotsData.getDataAttr().min_x;
                var max_x = PlotsData.getDataAttr().max_x;

                //Not using real x value for positioning discretized data
                var pos = 0;   //position Indicator
                for (var i = min_x ; i < max_x + 1; i++) {
                    var top;
                    var bottom;
                    var quan1;
                    var quan2;
                    var mean;
                    var IQR;
                    var scaled_y_arr=[];
                    var tmp_y_arr = [];
                    //Find the middle (vertical) line for one box plot
                    var midLine = attr.xScale(pos);
                    //Find the max/min y value with certain x value;
                    $.each(_dotsGroup, function(index, value) {
                        if (value.xVal === i.toString()) {
                            tmp_y_arr.push(parseFloat(value.yVal));
                        }
                    });
                    tmp_y_arr.sort(function(a, b) { return (a - b); });
                    if (tmp_y_arr.length === 0) {
                        //Do nothing: DO NOT MOVE POSITION INDEX (pos)
                    } else if (tmp_y_arr.length === 1) {
                        mean = attr.yScale(tmp_y_arr[0]);
                        boxPlotsElem.append("line")
                            .attr("x1", midLine-30)
                            .attr("x2", midLine+30)
                            .attr("y1", mean)
                            .attr("y2", mean)
                            .attr("stroke-width", 2)
                            .attr("stroke", "grey");
                        pos += 1;
                    } else {
                        if (tmp_y_arr.length === 2) {
                            mean = attr.yScale((tmp_y_arr[0] + tmp_y_arr[1]) / 2);
                            quan1 = bottom = attr.yScale(tmp_y_arr[0]);
                            quan2 = top = attr.yScale(tmp_y_arr[1]);
                            IQR = Math.abs(quan2 - quan1);
                            pos += 1;
                        } else {
                            var yl = tmp_y_arr.length;
                            if (yl % 2 === 0) {
                                mean = attr.yScale((tmp_y_arr[(yl / 2)-1] + tmp_y_arr[yl / 2]) / 2);
                                if (yl % 4 === 0) {
                                    quan1 = attr.yScale((tmp_y_arr[(yl / 4)-1] + tmp_y_arr[yl / 4]) / 2);
                                    quan2 = attr.yScale((tmp_y_arr[(3*yl / 4)-1] + tmp_y_arr[3 * yl / 4]) / 2);
                                } else {
                                    quan1 = attr.yScale(tmp_y_arr[Math.floor(yl / 4)]);
                                    quan2 = attr.yScale(tmp_y_arr[Math.floor(3 * yl / 4)]);
                                }
                            } else {
                                mean = attr.yScale(tmp_y_arr[Math.floor(yl / 2)]);
                                var tmp_yl = Math.floor(yl / 2) + 1;
                                if (tmp_yl % 2 === 0) {
                                    quan1 = attr.yScale((tmp_y_arr[tmp_yl / 2 - 1] + tmp_y_arr[tmp_yl / 2]) / 2);
                                    quan2 = attr.yScale((tmp_y_arr[(3 * tmp_yl / 2) - 2] + tmp_y_arr[(3 * tmp_yl / 2) - 1]) / 2);
                                } else {
                                    quan1 = attr.yScale(tmp_y_arr[Math.floor(tmp_yl / 2)]);
                                    quan2 = attr.yScale(tmp_y_arr[tmp_yl - 1 + Math.floor(tmp_yl / 2)]);
                                }
                            }
                            for (var k = 0 ; k < tmp_y_arr.length ; k++) {
                                scaled_y_arr[k] = parseFloat(attr.yScale(tmp_y_arr[k]));
                            }
                            scaled_y_arr.sort(function(a,b) { return (a - b); });
                            IQR = Math.abs(quan2 - quan1);
                            var index_top = Util.searchIndexTop(scaled_y_arr, (quan2 - 1.5 * IQR));
                            top = scaled_y_arr[index_top];
                            var index_bottom = Util.searchIndexBottom(scaled_y_arr, (quan1 + 1.5 * IQR));
                            bottom = scaled_y_arr[index_bottom];
                            pos += 1;
                        }

                        //D3 Drawing
                        boxPlotsElem.append("rect")
                            .attr("x", midLine-40)
                            .attr("y", quan2)
                            .attr("width", 80)
                            .attr("height", IQR)
                            .attr("fill", "none")
                            .attr("stroke-width", 1)
                            .attr("stroke", "#BDBDBD");
                        boxPlotsElem.append("line")
                            .attr("x1", midLine-40)
                            .attr("x2", midLine+40)
                            .attr("y1", mean)
                            .attr("y2", mean)
                            .attr("stroke-width", 2)
                            .attr("stroke", "#BDBDBD");
                        boxPlotsElem.append("line")
                            .attr("x1", midLine-30)
                            .attr("x2", midLine+30)
                            .attr("y1", top)
                            .attr("y2", top)
                            .attr("stroke-width", 1)
                            .attr("stroke", "#BDBDBD");
                        boxPlotsElem.append("line")
                            .attr("x1", midLine-30)
                            .attr("x2", midLine+30)
                            .attr("y1", bottom)
                            .attr("y2", bottom)
                            .attr("stroke", "#BDBDBD")
                            .style("stroke-width", 1);
                        boxPlotsElem.append("line")
                            .attr("x1", midLine)
                            .attr("x2", midLine)
                            .attr("y1", quan1)
                            .attr("y2", bottom)
                            .attr("stroke", "#BDBDBD")
                            .attr("stroke-width", 1);
                        boxPlotsElem.append("line")
                            .attr("x1", midLine)
                            .attr("x2", midLine)
                            .attr("y1", quan2)
                            .attr("y2", top)
                            .attr("stroke", "#BDBDBD")
                            .style("stroke-width", 1);
                    }
                }
            }

            function drawLog2Plots() {
                elem.elemDotsGroup.selectAll("path")
                    .attr("class", "dots")
                    .data(PlotsData.getDotsGroup())
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d) {
                        var _x = attr.xScale(d.xVal);
                        var _y = attr.yScale(d.yVal);
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("symbol", "circle");
                        $(this).attr("size", 20);
                        return "translate(" + _x + ", " + _y + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(20)
                        .type(function(d){
                            return mutationStyle[d.mutationType].symbol;
                        })
                    )
                    .attr("fill", function(d){
                        $(this).attr("fill", mutationStyle[d.mutationType].fill);
                        return mutationStyle[d.mutationType].fill;
                    })
                    .attr("stroke", function(d){
                        $(this).attr("stroke", mutationStyle[d.mutationType].stroke);
                        return mutationStyle[d.mutationType].stroke;
                    })
                    .attr("stroke-width", 1.2)
                    .attr("class", function(d) { return d.caseId; });
            }

            function drawContinuousPlots() {  //RPPA, DNA Methylation Views
                elem.elemDotsGroup.selectAll("path")
                    .data(PlotsData.getDotsGroup())
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d){
                        var _x = attr.xScale(d.xVal);
                        var _y = attr.yScale(d.yVal);
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("symbol", "circle");
                        $(this).attr("size", 35);
                        return "translate(" + attr.xScale(d.xVal) + ", " + attr.yScale(d.yVal) + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(35)
                        .type("circle"))
                    .attr("fill", function(d) {
                        switch (d.mutationType) {
                            case "non" : {$(this).attr("fill", "white");return "white";}
                            default: {$(this).attr("fill", "orange");return "orange";}
                        }
                    })
                    .attr("fill-opacity", function(d) {
                        switch (d.mutationType) {
                            case "non" : return 0.0;
                            default : return 1.0;
                        }
                    })
                    .attr("stroke", function(d) {
                        if (Util.hasCopyNumberData()) {
                            if (Util.isEmpty(d.gisticType)) {
                                return gisticStyle.Unknown.stroke;
                            } else {
                                return gisticStyle[d.gisticType].stroke;
                            }
                        } else {
                            return "black";
                        }
                    })
                    .attr("stroke-width", 1.2)
                    .attr("class", function(d) { return d.caseId; });
            }

            return {
                init: function() {
                    elem.boxPlots = elem.svg.append("svg:g");
                    elem.elemDotsGroup = elem.svg.append("svg:g");
                    if (Util.plotsTypeIsCopyNo()) {
                        if (Util.plotsIsDiscretized()) {    //Gistic, RAE...
                            drawBoxPlots(false);
                            drawGisticPlots();
                        } else {   //Log2
                            drawLog2Plots();
                        }
                    } else if (Util.plotsTypeIsClinical()) {
                        if (Util.plotsIsDiscretized()) { //discretized clinical attrs
                            drawBoxPlots(false);
                            drawClinicalDiscretizedPlots();
                        } else { //continuous clinical attrs
                            drawContinuousPlots();
                        }
                    } else {  //Methylation, RPPA 
                        drawContinuousPlots();
                    }
                },
                updateLogScaleX: function(applyLogScale) {
                    elem.elemDotsGroup.selectAll("path")
                        .transition().duration(300)
                        .attr("transform", function() {
                            if (applyLogScale) {
                                if(d3.select(this).attr("x_val") <= (Plots.getLogScaleThresholdDown())) {
                                    var _post_x = attr.xScale(Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2));
                                } else if (d3.select(this).attr("x_val") >= (Plots.getLogScaleThresholdUp())) {
                                    var _post_x = attr.xScale(Math.log(Plots.getLogScaleThresholdUp()) / Math.log(2));
                                } else {
                                    var _post_x = attr.xScale(Math.log(d3.select(this).attr("x_val")) / Math.log(2));
                                }
                            } else {
                                var _post_x = attr.xScale(d3.select(this).attr("x_val"));
                            }
                            var _pre_y = d3.select(this).attr("y_pos");
                            d3.select(this).attr("x_pos", _post_x);
                            return "translate(" + _post_x + ", " + _pre_y + ")";
                        });
                },
                updateLogScaleY: function(applyLogScale) {
                    elem.elemDotsGroup.selectAll("path")
                        .transition().duration(300)
                        .attr("transform", function() {
                            var _pre_x = d3.select(this).attr("x_pos");
                            if (applyLogScale) {
                                if (parseFloat(d3.select(this).attr("y_val")) <= (Plots.getLogScaleThresholdDown())) {
                                    var _post_y = attr.yScale(Math.log(Plots.getLogScaleThresholdDown()) / Math.log(2));
                                } else if (parseFloat(d3.select(this).attr("y_val")) >= (Plots.getLogScaleThresholdUp())) {
                                    var _post_y = attr.yScale(Math.log(Plots.getLogScaleThresholdUp()) / Math.log(2));
                                } else {
                                    var _post_y = attr.yScale(Math.log(d3.select(this).attr("y_val")) / Math.log(2));
                                }
                            } else {
                                var _post_y = attr.yScale(d3.select(this).attr("y_val"));
                            }
                            d3.select(this).attr("y_pos", _post_y);
                            return "translate(" + _pre_x + ", " + _post_y + ")";
                        });
                    if (Util.plotsIsDiscretized()) {
                        drawBoxPlots(applyLogScale);
                    }
                }
            };
        }());

        var Legends = (function() {

            function drawCopyNoViewLegends() {
                //Only show glyphs whose mutation type
                //appeared in the current individual case
                var _appearedMutationTypes = [];
                _appearedMutationTypes.length = 0;
                $.each(PlotsData.getDotsGroup(), function(index, value) {
                    _appearedMutationTypes.push(value.mutationType);
                });

                //Convert object to array
                var mutationStyleArr = [];
                mutationStyleArr.length = 0;
                for (var key in mutationStyle) {
                    var obj = mutationStyle[key];
                    if (_appearedMutationTypes.indexOf(key) !== -1) {
                        mutationStyleArr.push(obj);
                    }
                }
                //If only contain non mutation cases, remove the glyphs completely
                if (mutationStyleArr.length === 1 &&
                    mutationStyleArr[0].legendText === mutationStyle["non"].legendText) {
                    mutationStyleArr.length = 0;
                }

                var legend = elem.svg.selectAll(".legend")
                    .data(mutationStyleArr)
                    .enter().append("svg:g")
                    .attr("transform", function(d, i) {
                        return "translate(610, " + (30 + i * 15) + ")";
                    });

                legend.append("path")
                    .data(mutationStyleArr)
                    .attr("width", 18)
                    .attr("height", 16)
                    .attr("d", d3.svg.symbol().size(30)
                        .type(function(d) {return d.symbol;}))
                    .attr("fill", function(d) {return d.fill;})
                    .attr("stroke", function(d){return d.stroke;})
                    .attr("stroke-width", 1.2);

                legend.append("text")
                    .attr("dx", ".75em")
                    .attr("dy", ".35em")
                    .style("text-anchor", "front")
                    .text(function(d){return d.legendText;});
            }

            function drawOtherViewLegends() {
                var gisticStyleArr = [];
                
                if (Util.hasCopyNumberData()) {
                    for (var key in gisticStyle) {
                        var obj = gisticStyle[key];
                        gisticStyleArr.push(obj);
                    }
                }
                
                var mutatedStyle = {
                        stroke : "none",
                        symbol : "circle",
                        fill : "orange",
                        legendText : "Mutated"
                    };
                gisticStyleArr.push(mutatedStyle);

                var legend = elem.svg.selectAll(".legend")
                    .data(gisticStyleArr)
                    .enter().append("g")
                    .attr("class", "legend")
                    .attr("transform", function(d, i) {
                        return "translate(610, " + (30 + i * 15) + ")";
                    });

                legend.append("path")
                    .attr("width", 18)
                    .attr("height", 18)
                    .attr("d", d3.svg.symbol()
                        .size(40)
                        .type(function(d) { return d.symbol; }))
                    .attr("fill", function (d) { return d.fill; })
                    .attr("stroke", function (d) { return d.stroke; })
                    .attr("stroke-width", 1.2);

                legend.append("text")
                    .attr("dx", ".75em")
                    .attr("dy", ".35em")
                    .style("text-anchor", "front")
                    .text(function(d) { return d.legendText; });    
            }

            return {
                init: function() {
                    if (Util.plotsTypeIsCopyNo()) {
                        drawCopyNoViewLegends();
                    } else {
                        drawOtherViewLegends();
                    }
                    if (!Util.plotsIsDiscretized()) {
                        var tmpDataAttr = PlotsData.getDataAttr();
                        var tmpPearson = "Pearson: " + tmpDataAttr.pearson,
                            tmpSpearman = "Spearman: " + tmpDataAttr.spearman;
                        var coExpLegend = elem.svg.selectAll(".coexp_legend")
                            .data(["Correlation", tmpPearson, tmpSpearman])
                            .enter().append("g")
                            .attr("class", "coexp_legend")
                            .attr("transform", function(d, i) {
                                return "translate(600, " + (150 + i * 15) + ")";
                            });
                        coExpLegend.append("text")
                                .attr("dx", ".75em")
                                .attr("dy", ".35em")
                                .style("text-anchor", "front")
                                .text(function(d) {
                                    return d;
                                });                      
                    }
                }
            };
        }());

        function initCanvas() {
            elem.svg = d3.select("#plots_box")
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
        }

        function drawErrMsgs() {
            var _xDataType = "",
                _yDataType = "";
            if (Util.plotsTypeIsCopyNo()) {
                var e = document.getElementById("data_type_copy_no");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_mrna");
                _yDataType = e.options[e.selectedIndex].text;
            } else if (Util.plotsTypeIsMethylation()) {
                var e = document.getElementById("data_type_dna_methylation");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_mrna");
                _yDataType = e.options[e.selectedIndex].text;
            } else if (Util.plotsTypeIsRPPA()) {
                var e = document.getElementById("data_type_mrna");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_rppa");
                _yDataType = e.options[e.selectedIndex].text;
            }

            var err_line1 = "There is no UNAVAILABLE_DATA_TYPE data";
            var err_line2 = "for " + PlotsView.getUserSelection().gene + " in the selected cancer study.";
            var _dataStatus = PlotsData.getDataStatus();
            if (!_dataStatus.xHasData && _dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", _xDataType);
            } else if (_dataStatus.xHasData && !_dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", _yDataType);
            } else if (!_dataStatus.xHasData && !_dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", "both selected data types");
            } else if (_dataStatus.xHasData &&_dataStatus.yHasData &&
                !_dataStatus.combineHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", "combined data types");
            }

            elem.svg.append("text")
                .attr("x", 350)
                .attr("y", 50)
                .attr("text-anchor", "middle")
                .attr("fill", "#DF3A01")
                .text(err_line1);
            elem.svg.append("text")
                .attr("x", 350)
                .attr("y", 70)
                .attr("text-anchor", "middle")
                .attr("fill", "#DF3A01")
                .text(err_line2);
            elem.svg.append("rect")
                .attr("x", 150)
                .attr("y", 30)
                .attr("width", 400)
                .attr("height", 50)
                .attr("fill", "none")
                .attr("stroke-width", 1)
                .attr("stroke", "#BDBDBD");
        }

        function drawImgConverter() {
            $('#view_title').empty();
            if (Util.plotsTypeIsCopyNo()) {
                $('#view_title').append(PlotsView.getUserSelection().gene + ": mRNA Expression v. CNA ");
            } else if (Util.plotsTypeIsMethylation()) {
                $('#view_title').append(PlotsView.getUserSelection().gene + ": mRNA Expression v. DNA Methylation ");
            } else if (Util.plotsTypeIsRPPA()) {
                $('#view_title').append(PlotsView.getUserSelection().gene + ": RPPA protein level v. mRNA Expression ");
            }
            var pdfConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank' " +
                "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='correlation_plot-" + PlotsView.getUserSelection().gene + ".pdf'>" +
                "<input type='submit' value='PDF'></form>";
            $('#view_title').append(pdfConverterForm);
            var svgConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank'" +
                "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' name='filename' value='correlation_plot-" + PlotsView.getUserSelection().gene + ".svg'>" +
                "<input type='submit' value='SVG'></form>";
            $('#view_title').append(svgConverterForm);
        }

        function applyLogScaleX(applyLogScale) {
            //Update the axis
            Axis.updateLogScaleX(applyLogScale);
            //Update the position of the dots
            ScatterPlots.updateLogScaleX(applyLogScale);
        }

        function applyLogScaleY(applyLogScale) {
            //Update the axis
            Axis.updateLogScaleY(applyLogScale);
            //Update the position of the dots
            ScatterPlots.updateLogScaleY(applyLogScale);
        }

        return {
            init: function() {
                initCanvas();
                if (PlotsData.getDotsGroup().length !== 0) {
                    drawImgConverter();
                    Axis.init();
                    ScatterPlots.init();
                    Legends.init();
                    Qtips.init();
                    if (document.getElementById("log_scale_option_x") !== null) {
                        var _applyLogScaleX = document.getElementById("log_scale_option_x").checked;
                        applyLogScaleX(_applyLogScaleX);
                    }
                    if (document.getElementById("log_scale_option_y") !== null) {
                        var _applyLogScaleY = document.getElementById("log_scale_option_y").checked;
                        applyLogScaleY(_applyLogScaleY);
                    }                        
                } else { //No available data
                    drawErrMsgs();
                }
            },
            applyLogScaleX: applyLogScaleX,
            applyLogScaleY: applyLogScaleY
        };
    }()); //closing plots view
