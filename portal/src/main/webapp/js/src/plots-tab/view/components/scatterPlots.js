var scatterPlots = (function() {
    
    var settings = {
            canvas_width: 810,
            canvas_height: 650,
            axis: {
                x: {
                    range_min: 170,
                    range_max: 670,
                    orient: "bottom",
                    title_x: 400,
                    title_y: 640,
                    help_x: 350,
                    help_y: 575
                },
                y: {
                    range_min: 520,
                    range_max: 20,
                    orient: "left",
                    title_x: -280,
                    title_y: 55,
                    help_x: 135,
                    help_y: 145
                }
            },
            log_scale: {
                threshold_down : 0.17677669529,  //-2.5 to 10
                threshold_up : 1.2676506e+30
            }
        },
        div = "",
        elem = {
            svg : "",
            x: {
                scale: "",
                axis: ""
            },
            y: {
                scale: "",
                axis: ""
            },
            dotsGroup : "",   //Group of single Dots
            axisTitleGroup: "",
            boxPlots: ""
        },
        data = [],
        glyphs = [],
        stat = {
            applied_box_plots: false,
            box_plots_axis: "",
            apply_co_exp: false
        },
        scores = {
            pearson: 0,
            spearman: 0
        };
    
    function initCanvas() {
        elem.svg = d3.select("#" + div)
            .append("svg")
            .attr("width", settings.canvas_width)
            .attr("height", settings.canvas_height);
        elem.dotsGroup = elem.svg.append("svg:g").attr("class", d3_class.dots);
        elem.axisTitleGroup = elem.svg.append("svg:g").attr("class", "axis");
    }
    
    function initAxis(axis) {
        var stat = plotsData.stat();
        var domainMin = stat[axis].min - stat[axis].edge;
        var domainMax = stat[axis].max + stat[axis].edge;
        if (domainMin === domainMax && domainMin === 0) {
            elem[axis].scale = d3.scale.linear()
                .domain([-1, 1])
                .range([settings.axis[axis].range_min, settings.axis[axis].range_max]);            
        } else {
            elem[axis].scale = d3.scale.linear()
                .domain([domainMin, domainMax])
                .range([settings.axis[axis].range_min, settings.axis[axis].range_max]);                    
        }

        elem[axis].axis = d3.svg.axis()
            .scale(elem[axis].scale)
            .orient(settings.axis[axis].orient)
            .tickSize(6, 0, 0)
            .tickPadding([8]);
    }
    
    function drawAxis(axis) {
        
        d3.select("#" + div).select("." + d3_class[axis].axis).remove();
        var top_x, top_y, bottom_x, bottom_y;
        if (axis === "x") {
            top_x = 0;
            top_y = settings.axis.y.range_min;
            bottom_x = 0;
            bottom_y = settings.axis.y.range_max;
        } else if (axis === "y") {
            top_x = settings.axis.x.range_min;
            top_y = 0;
            bottom_x = settings.axis.x.range_max;
            bottom_y = 0;
        }

        if ($("input:radio[name='" + ids.sidebar[axis].data_type + "']:checked").val() === vals.data_type.genetic) {
            if (is_profile_discretized(axis)) {
                function sort_by_cna(a, b){
                    var _attr_name = (stat.box_plots_axis === "x")? "xVal": "yVal";
                    var a_val = parseInt(a[_attr_name]);
                    var b_val = parseInt(b[_attr_name]); 
                    return ((a_val < b_val) ? -1 : ((a_val > b_val) ? 1 : 0));
                }
                var _data_for_sorting = jQuery.extend(true, [], data);
                _data_for_sorting = _data_for_sorting.sort(sort_by_cna);
                var _text_set = [];
                $.each(_data_for_sorting, function(index, obj) {
                    var _attr_name = (stat.box_plots_axis === "x")? "xVal": "yVal";
                    if($.inArray(gisticInterpreter.convert_to_val(obj[_attr_name]), _text_set) === -1) {
                        _text_set.push(gisticInterpreter.convert_to_val(obj[_attr_name]));
                    }
                });
                discretized_axis(_text_set, true);
            } else continuous_axis(false);
        } else if ($("input:radio[name='" + ids.sidebar[axis].data_type + "']:checked").val() === vals.data_type.clin) {
            if (clinical_attr_is_discretized(axis)) {
                discretized_axis(clinical_data_interpreter.get_text_labels(axis), true);
            } else continuous_axis(true);
        } else {
            continuous_axis(false);
        }
        
        function discretized_axis(_text_set, _rotate_flag) {
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(" + top_x + ", " + top_y + ")")
                .attr("class", d3_class[axis].axis)
                .call(elem[axis].axis.ticks(_text_set.length))
                .selectAll("text")
                .data(_text_set)
                .style("font-family", "sans-serif")
                .style("font-size", "12px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black")
                .style("text-anchor", "end")
                .attr("transform", function(d) {
                    if (_rotate_flag) {
                        return "rotate(-30)"; 
                    }
                })
                .attr("class", function(d) {
                    if (axis === "x") {
                        if (d.length > 8) return "trimmed_label";
                        else return "axis_label";
                    } else if (axis === "y") {
                        if (d.length > 5) return "trimmed_label";
                        else return "axis_label";
                    }
                })
                .text(function(d) {
                    if (_rotate_flag) {
                        if (axis === "x") {
                            if (d.length > 20) {
                                return d.substring(0, 20) + "...";   
                            } 
                            else return d;                            
                        } else if (axis === "y") {
                            if (d.length > 12) {
                                return d.substring(0, 12) + "...";   
                            } 
                            else return d;                            
                        }
                    } else return d;
                });
                elem.svg.selectAll(".trimmed_label").each(function(d) {
                    $(this).qtip(
                        {
                            content: {text: "<font size=2>" + d + "</font>" },
                            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                            show: {event: "mouseover"},
                            hide: {fixed:true, delay: 100, event: "mouseout"},
                            position: {my:'left bottom',at:'top right', viewport: $(window)}
                        }
                    );
                });
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(" + bottom_x + ", " + bottom_y + ")")
                .call(elem[axis].axis.orient(settings.axis[axis].orient).ticks(0));              
        }
        function continuous_axis(_rotate_flag) {
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(" + top_x + ", " + top_y + ")")
                .attr("class", d3_class[axis].axis)
                .call(elem[axis].axis)
                .selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "12px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black")
                .style("text-anchor", "end")
                .attr("transform", function(d) {
                    if (_rotate_flag) {
                        return "rotate(-25)"; 
                    }
                });
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(" + bottom_x + ", " + bottom_y + ")")
                .call(elem[axis].axis.orient(settings.axis[axis].orient).ticks(0));            
        }
    }
    
    function drawDots(_apply_box_plots, _box_plots_axis) {

        elem.dotsGroup = elem.svg.append("svg:g");
        elem.dotsGroup.selectAll("path").remove();

        var stat = plotsData.stat();
        
        //sort data array (to plot the mutated dots last)
        $.each(data, function(index, obj) {
            if (Object.keys(obj.mutation).length === 0) {
                bubble_up(data, index);
            }
        });
        
        if (genetic_vs_genetic()) {
            if ($("input[name=" + ids.sidebar.util.view_switch + "]:checked").val() === "gistic" && isSameGene() && stat.hasCnaAnno) {
                //gistic view (only apply to one gene and having cna profile data case)
                elem.dotsGroup.selectAll("path")
                    .data(data)
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d){

                        var _x, _y;
                        if (_apply_box_plots) { //apply noise
                            if (_box_plots_axis === "x") {
                                _x = elem.x.scale(d.xVal) + (Math.random() * 20 - 20/2);
                                _y = elem.y.scale(d.yVal);
                            } else {
                                _x = elem.x.scale(d.xVal);
                                _y = elem.y.scale(d.yVal) + (Math.random() * 20 - 20/2);
                            }
                        } else {
                            _x = elem.x.scale(d.xVal);
                            _y = elem.y.scale(d.yVal);
                        }
                        
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("case_id", d.caseId);
                        $(this).attr("size", 35);
                        $(this).attr("shape", gisticInterpreter.getSymbol(d));

                        var _mutation_details = "";
                        if (Object.keys(d.mutation).length !== 0) {
                            $.each(Object.keys(d.mutation), function(index, gene) {
                                _mutation_details += "<br>" + gene + ": " + d.mutation[gene].details;
                            });
                        }
                        $(this).attr("mutation_details", _mutation_details);

                        return "translate(" + _x + ", " + _y + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(35)
                        .type(function(d){
                            return gisticInterpreter.getSymbol(d);
                        }))
                    .attr("fill", function(d){
                        return gisticInterpreter.getFill(d);
                    })
                    .attr("stroke", function(d){
                        return gisticInterpreter.getStroke(d);
                    })
                    .attr("stroke-width", 1.2);
            } else {
                elem.dotsGroup.selectAll("path")
                    .data(data)
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d){
                        
                        var _x, _y;
                        if (_apply_box_plots) { //apply noise
                            if (_box_plots_axis === "x") {
                                _x = elem.x.scale(d.xVal) + (Math.random() * 20 - 20/2);
                                _y = elem.y.scale(d.yVal);
                            } else {
                                _x = elem.x.scale(d.xVal);
                                _y = elem.y.scale(d.yVal) + (Math.random() * 20 - 20/2);
                            }
                        } else {
                            _x = elem.x.scale(d.xVal);
                            _y = elem.y.scale(d.yVal);
                        }
                        
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("case_id", d.caseId);
                        $(this).attr("size", 20);
                        $(this).attr("shape", mutationInterpreter.getSymbol(d));

                        var _mutation_details = "";
                        if (Object.keys(d.mutation).length !== 0) {
                            $.each(Object.keys(d.mutation), function(index, gene) {
                                _mutation_details += "<br>" + gene + ": " + d.mutation[gene].details;
                            });
                        }
                        $(this).attr("mutation_details", _mutation_details);

                        return "translate(" + _x + ", " + _y + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(20)
                        .type(function(d){
                            return mutationInterpreter.getSymbol(d);
                        }))
                    .attr("fill", function(d){
                        return mutationInterpreter.getFill(d);
                    })
                    .style("opacity", function(d){
                        return mutationInterpreter.getOpacity();
                    })
                    .attr("stroke", function(d){
                        return mutationInterpreter.getStroke(d);
                    })
                    .attr("stroke-width", 1.2); 
            } 
        } else if (genetic_vs_clinical()) {
            elem.dotsGroup.selectAll("path")
                .data(data)
                .enter()
                .append("svg:path")
                .attr("transform", function(d){
                    
                    var _x, _y;
                    if (_apply_box_plots) { //apply noise
                        if (_box_plots_axis === "x") {
                            _x = elem.x.scale(d.xVal) + (Math.random() * 20 - 20/2);
                            _y = elem.y.scale(d.yVal);
                        } else {
                            _x = elem.x.scale(d.xVal);
                            _y = elem.y.scale(d.yVal) + (Math.random() * 20 - 20/2);
                        }
                    } else {
                        _x = elem.x.scale(d.xVal);
                        _y = elem.y.scale(d.yVal);
                    }
                    
                    $(this).attr("x_pos", _x);
                    $(this).attr("y_pos", _y);
                    $(this).attr("x_val", d.xVal);
                    $(this).attr("y_val", d.yVal);
                    $(this).attr("case_id", d.caseId);
                    $(this).attr("size", 20);
                    $(this).attr("shape", mutationInterpreter.getSymbol(d));
                    
                    var _mutation_details = "";
                    if (Object.keys(d.mutation).length !== 0) {
                        $.each(Object.keys(d.mutation), function(index, gene) {
                            _mutation_details += "<br>" + gene + ": " + d.mutation[gene].details;
                        });
                    }
                    $(this).attr("mutation_details", _mutation_details);

                    return "translate(" + _x + ", " + _y + ")";
                })
                .attr("d", d3.svg.symbol()
                    .size(20)
                    .type(function(d){
                        return mutationInterpreter.getSymbol(d);
                    }))
                .attr("fill", function(d){
                    return mutationInterpreter.getFill(d);
                })
                .style("opacity", function(d){
                    return mutationInterpreter.getOpacity();
                })
                .attr("stroke", function(d){
                    return mutationInterpreter.getStroke(d);
                })
                .attr("stroke-width", 1.2); 
        } else if (clinical_vs_clinical()) {
            elem.dotsGroup.selectAll("path")
                .data(data)
                .enter()
                .append("svg:path")
                .attr("transform", function(d){
                    
                    var _x, _y;
                    if (_apply_box_plots) { //apply noise
                        if (_box_plots_axis === "x") {
                            _x = elem.x.scale(d.xVal) + (Math.random() * 20 - 20/2);
                            _y = elem.y.scale(d.yVal);
                        } else {
                            _x = elem.x.scale(d.xVal);
                            _y = elem.y.scale(d.yVal) + (Math.random() * 20 - 20/2);
                        }
                    } else {
                        _x = elem.x.scale(d.xVal);
                        _y = elem.y.scale(d.yVal);
                    }

                    $(this).attr("x_pos", _x);
                    $(this).attr("y_pos", _y);
                    $(this).attr("x_val", d.xVal);
                    $(this).attr("y_val", d.yVal);
                    $(this).attr("case_id", d.caseId);
                    $(this).attr("size", 20);
                    $(this).attr("shape", mutationInterpreter.getSymbol(d));

                    var _mutation_details = "";
                    if (Object.keys(d.mutation).length !== 0) {
                        $.each(Object.keys(d.mutation), function(index, gene) {
                            _mutation_details += "<br>" + gene + ": " + d.mutation[gene].details;
                        });
                    }
                    $(this).attr("mutation_details", _mutation_details);
                    
                    return "translate(" + _x + ", " + _y + ")";
                })
                .attr("d", d3.svg.symbol()
                    .size(20)
                    .type(function(d){
                        return mutationInterpreter.getSymbol(d);
                    }))
                .attr("fill", function(d){
                    return mutationInterpreter.getFill(d);
                })
                .style("opacity", function(d){
                    return mutationInterpreter.getOpacity();
                })
                .attr("stroke", function(d){
                    return mutationInterpreter.getStroke(d);
                })
                .attr("stroke-width", 1.2); 
        }

    }
    
    function log_scale() {
        
        var re_scale = function(_axis) {
            var _stat = plotsData.stat();
            var _new_min = (_stat[_axis].min <= settings.log_scale.threshold_down)? Math.log(settings.log_scale.threshold_down)/Math.log(2):Math.log(_stat[_axis].min) / Math.log(2);
            var _new_max = (_stat[_axis].min >= settings.log_scale.threshold_up)? Math.log(settings.log_scale.threshold_up)/Math.log(2):Math.log(_stat[_axis].max) / Math.log(2);
            var _new_edge;
            if (_axis === "x") _new_edge = (_new_max - _new_min) * 0.1;
            else if (_axis === "y") _new_edge = (_new_max - _new_min) * 0.2;
            var _new_scale = d3.scale.linear()
                .domain([_new_min - _new_edge, _new_max + _new_edge])
                .range([settings.axis[_axis].range_min, settings.axis[_axis].range_max]); 
            return _new_scale;
        };
        
        var re_draw_axis = function(_axis) {
            d3.select("#" + div).select("." + d3_class[_axis].axis).remove();
            var _new_scale = re_scale(_axis);
            var _new_axis = d3.svg.axis()
                .scale(_new_scale)
                .orient(settings.axis[_axis].orient)
                .tickSize(6, 0, 0)
                .tickPadding([8]);
        
            var top_x, top_y;
            if (_axis === "x") {
                top_x = 0;
                top_y = settings.axis.y.range_min;
            } else if (_axis === "y") {
                top_x = settings.axis.x.range_min;
                top_y = 0;
            }
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(" + top_x + ", " + top_y + ")")
                .attr("class", d3_class[_axis].axis)
                .call(_new_axis)
                .selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "12px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black");
        };
        
        var update_stat = function(_axis) {
            //flip the axis
            _axis = (_axis === "x")? "y": "x";
            var _stat = jQuery.extend(true, {}, plotsData.stat());
            _stat[_axis].min = (_stat[_axis].min <= settings.log_scale.threshold_down) ? Math.log(settings.log_scale.threshold_down) / Math.log(2) : Math.log(_stat[_axis].min) / Math.log(2);
            _stat[_axis].max = (_stat[_axis].max >= settings.log_scale.threshold_up) ? Math.log(settings.log_scale.threshold_up) / Math.log(2) : Math.log(_stat[_axis].max) / Math.log(2);
            if (_axis === "x") _stat[_axis].edge = (_stat[_axis].max - _stat[_axis].min) * 0.1;
            else if (_axis === "y") _stat[_axis].edge = (_stat[_axis].max - _stat[_axis].min) * 0.2;
            return _stat;
        };
        
        var update_data = function(_axis) {
            var _data = jQuery.extend(true, [], data);
            var _attr_name = (_axis === "x")? "yVal": "xVal";
            $.each(_data, function(index, _obj) {
                _obj[_attr_name] = (_obj[_attr_name] <= settings.log_scale.threshold_down)? (Math.log(settings.log_scale.threshold_down)/Math.log(2)).toString():(Math.log(_obj[_attr_name]) / Math.log(2)).toString();
            });
            return _data;
        };
        
        var update_box_plots = function(_data, _stat) {
            d3.select("#" + div).select("." + d3_class.box_plots).remove();
            var _elem = jQuery.extend(true, {}, elem);
            var _axis = (stat.box_plots_axis === "x")? "y": "x";
            _elem[_axis].scale = re_scale(_axis);
            boxPlots.init(_data, _stat, stat.box_plots_axis, _elem);
        };
        
        var restore_box_plots = function() {
            boxPlots.init(data, plotsData.stat(), stat.box_plots_axis, elem);
        };
        
        var update_axis_title = function(_axis, _opt) {
            var _previous_text;
            if ($('.' + d3_class[_axis].axis_title).length !== 0) {
                _previous_text = d3.select("#plots-box").select("." + d3_class[_axis].axis_title).text();
            } else {
                _previous_text = d3.select("#plots-box").select("." + d3_class[_axis].axis_title + "_trimmed").text();
            }
            if (_opt === "append") {
                if (_previous_text.indexOf("(log2)") === -1) {
                    d3.select("#plots-box").select("." + d3_class[_axis].axis_title).text(_previous_text + " (log2)");
                    var _pre_x = d3.select("#" + ids.main_view.div).select("." + d3_class[_axis].title_help).attr("x");
                    var _pre_y = d3.select("#" + ids.main_view.div).select("." + d3_class[_axis].title_help).attr("y");
                    if (_axis === "x") {
                        d3.select("#" + ids.main_view.div).select("." + d3_class[_axis].title_help).attr("x", parseInt(_pre_x) + 15);
                    } else if (_axis === "y") {
                        d3.select("#" + ids.main_view.div).select("." + d3_class[_axis].title_help).attr("y", parseInt(_pre_y) - 15);
                    }
                }
            } else if (_opt === "remove") {
                if (_previous_text.indexOf("(log2)") !== -1) {
                    d3.select("#plots-box").select("." + d3_class[_axis].axis_title).text(_previous_text.replace(" (log2)", "")); 
                    var _pre_x = d3.select("#" + ids.main_view.div).select("." + d3_class[_axis].title_help).attr("x");
                    var _pre_y = d3.select("#" + ids.main_view.div).select("." + d3_class[_axis].title_help).attr("y");
                    if (_axis === "x") {
                        d3.select("#" + ids.main_view.div).select("." + d3_class[_axis].title_help).attr("x", parseInt(_pre_x) - 15);
                    } else if (_axis === "y") {
                        d3.select("#" + ids.main_view.div).select("." + d3_class[_axis].title_help).attr("y", parseInt(_pre_y) + 15);
                    }                    
                }
            }
        };
        
        if (genetic_vs_genetic()) {
            if ($("#" + ids.sidebar.x.log_scale).is(':checked') && $("#" + ids.sidebar.y.log_scale).is(':checked')) {

                var _new_x_scale = re_scale("x");
                var _new_y_scale = re_scale("y");
                re_draw_axis("x");
                re_draw_axis("y");
                update_axis_title("x", "append");
                update_axis_title("y", "append");
                if (stat.applied_box_plots) update_box_plots(update_data(stat.box_plots_axis), update_stat(stat.box_plots_axis));

                elem.dotsGroup.selectAll("path")
                    .transition().duration(300)
                    .attr("transform", function() {
                        var _x_val = (d3.select(this).attr("x_val") <= settings.log_scale.threshold_down)? Math.log(settings.log_scale.threshold_down)/Math.log(2):Math.log(d3.select(this).attr("x_val")) / Math.log(2);
                        var _y_val = (d3.select(this).attr("y_val") <= settings.log_scale.threshold_down)? Math.log(settings.log_scale.threshold_down)/Math.log(2):Math.log(d3.select(this).attr("y_val")) / Math.log(2);
                        var _x = _new_x_scale(_x_val);
                        var _y = _new_y_scale(_y_val);
                        return "translate(" + _x + ", " + _y + ")";
                    }); 

            } else if ($("#" + ids.sidebar.x.log_scale).is(':checked') && (!$("#" + ids.sidebar.y.log_scale).is(':checked'))) {

                var _new_scale = re_scale("x");
                re_draw_axis("x");
                initAxis("y");
                drawAxis("y");
                update_axis_title("x", "append");
                update_axis_title("y", "remove");
                if (stat.applied_box_plots) update_box_plots(update_data(stat.box_plots_axis), update_stat(stat.box_plots_axis));

                elem.dotsGroup.selectAll("path")
                    .transition().duration(300)
                    .attr("transform", function() {
                        var _log_val = d3.select(this).attr("x_val") <= settings.log_scale.threshold_down ? Math.log(settings.log_scale.threshold_down)/Math.log(2): Math.log(d3.select(this).attr("x_val"))/Math.log(2);
                        var _log_pos = _new_scale(_log_val);
                        var _pre_pos = d3.select(this).attr("y_pos");
                        return "translate(" + _log_pos + ", " + _pre_pos + ")";    
                    }); 

            } else if ((!$("#" + ids.sidebar.x.log_scale).is(':checked')) && $("#" + ids.sidebar.y.log_scale).is(':checked')) {

                var _new_scale = re_scale("y");
                initAxis("x");
                drawAxis("x");
                re_draw_axis("y");
                update_axis_title("x", "remove");
                update_axis_title("y", "append");
                if (stat.applied_box_plots) update_box_plots(update_data(stat.box_plots_axis), update_stat(stat.box_plots_axis));

                elem.dotsGroup.selectAll("path")
                    .transition().duration(300)
                    .attr("transform", function() {
                        var _log_val = d3.select(this).attr("y_val") <= settings.log_scale.threshold_down ? Math.log(settings.log_scale.threshold_down)/Math.log(2): Math.log(d3.select(this).attr("y_val"))/Math.log(2);
                        var _log_pos = _new_scale(_log_val);
                        var _pre_pos = d3.select(this).attr("x_pos");
                        return "translate(" + _pre_pos + ", " + _log_pos + ")";    
                    }); 

            } else if ((!$("#" + ids.sidebar.x.log_scale).is(':checked')) && (!$("#" + ids.sidebar.y.log_scale).is(':checked'))) {

                initAxis("x");
                drawAxis("x");
                initAxis("y");
                drawAxis("y");
                update_axis_title("x", "remove");
                update_axis_title("y", "remove");
                if (stat.applied_box_plots) restore_box_plots();
                elem.dotsGroup.selectAll("path")
                    .transition().duration(300)
                    .attr("transform", function() {
                        var _x = d3.select(this).attr("x_pos");
                        var _y = d3.select(this).attr("y_pos");
                        return "translate(" + _x + ", " + _y + ")";
                    });
            }           
        } else if (genetic_vs_clinical()) {
            var _profile_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)? "x": "y";
            var _clin_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)? "y": "x";
            
            if ($("#" + ids.sidebar[_profile_axis].log_scale).is(":checked")) {
                var _new_scale = re_scale(_profile_axis);
                initAxis(_clin_axis);
                drawAxis(_clin_axis);
                re_draw_axis(_profile_axis);
                update_axis_title(_clin_axis, "remove");
                update_axis_title(_profile_axis, "append");
                if (stat.applied_box_plots) update_box_plots(update_data(stat.box_plots_axis), update_stat(stat.box_plots_axis));
                
                elem.dotsGroup.selectAll("path")
                   .transition().duration(300)
                   .attr("transform", function() {
                       var _log_val = d3.select(this).attr(_profile_axis + "_val") <= settings.log_scale.threshold_down? Math.log(settings.log_scale.threshold_down)/Math.log(2): Math.log(d3.select(this).attr(_profile_axis + "_val")) / Math.log(2);
                       var _log_pos = _new_scale(_log_val);
                       var _pre_pos = d3.select(this).attr(_clin_axis + "_pos");
                       if (_profile_axis === "x") return "translate(" + _log_pos + ", " + _pre_pos + ")";  
                       else return "translate(" + _pre_pos + ", " + _log_pos + ")";  
                   }); 
                
            } else if (!$("#" + ids.sidebar[_profile_axis].log_scale).is(":checked")) {
                initAxis("x");
                drawAxis("x");
                initAxis("y");
                drawAxis("y");
                update_axis_title("x", "remove");
                update_axis_title("y", "remove");
                if (stat.applied_box_plots) restore_box_plots();
                elem.dotsGroup.selectAll("path")
                    .transition().duration(300)
                    .attr("transform", function() {
                        var _x = d3.select(this).attr("x_pos");
                        var _y = d3.select(this).attr("y_pos");
                        return "translate(" + _x + ", " + _y + ")";
                    }); 
            }
            
        }
       
    }
    
    function appendTitle(axis) { //axis titles
        var elt = ($("input:radio[name='" + ids.sidebar[axis].data_type + "']:checked").val() === vals.data_type.genetic)? document.getElementById(ids.sidebar[axis].profile_name):document.getElementById(ids.sidebar[axis].clin_attr);
        var _name = ($("input:radio[name='" + ids.sidebar[axis].data_type + "']:checked").val() === vals.data_type.genetic)? ($("#" + ids.sidebar[axis].gene).val() + ", " + elt.options[elt.selectedIndex].text): elt.options[elt.selectedIndex].text;
        var _id = elt.options[elt.selectedIndex].value;
        
        //trimm exceedingly long titles
        var _max_length = 60, _trimmed_name = "";
        if (_name.length > _max_length) {
            _trimmed_name = _name.substring(0, _max_length) + "...";
        } else _trimmed_name = _name;
        
        var _tmp_attr = (axis === "y")? "rotate(-90)": "";
        d3.select("#" + div).select(d3_class[axis].axis_title).remove();
        elem.axisTitleGroup.append("text")
            .attr("class", function() {
                if (_name.length > _max_length) return d3_class[axis].axis_title + "_trimmed";
                else return d3_class[axis].axis_title;
            })
            .attr("transform", _tmp_attr)
            .attr("x", settings.axis[axis].title_x)
            .attr("y", settings.axis[axis].title_y)
            .style("text-anchor", "middle")
            .style("font-weight","bold")
            .text(_trimmed_name);
    
        //apply mouse over to trimmed titles
        elem.svg.selectAll("." + d3_class[axis].axis_title + "_trimmed").each(function(d) {
            $(this).qtip(
                {
                    content: {text: "<font size=2>" + _name + "</font>" },
                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                    show: {event: "mouseover"},
                    hide: {fixed:true, delay: 100, event: "mouseout"},
                    position: {my:'left bottom',at:'top right', viewport: $(window)}
                }
            );
        });
        
        //append help icon (mouseover)
        var _dist_x = 0; //decide the distance between the title and the x help icon
        if (_trimmed_name.length < 50) _dist_x = 10;
        else if (_trimmed_name.length > 50 && _trimmed_name.length < 63) _dist_x = 0;
        else _dist_x = -10;
        var _dist_y = 0; //decide the distance between the title and the y help icon 
        if (_trimmed_name.length < 50) _dist_y = 535;
        else if (_trimmed_name.length > 50 && _trimmed_name.length < 63) _dist_y = 550;
        else _dist_y = 560;
        var _pos_x = (axis==="x")? (settings.axis.x.title_x + _trimmed_name.length * 8 / 2 + _dist_x ): (settings.axis.y.title_y - 11);
        var _pos_y = (axis==="x")? (settings.axis.x.title_y - 12): (settings.axis.y.title_x + _dist_y - _trimmed_name.length / 2 * 8 );
        elem.axisTitleGroup.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", d3_class[axis].title_help)
            .attr("x", _pos_x)
            .attr("y", _pos_y)
            .attr("width", "16")
            .attr("height", "16");
        var _description = "";
        if ($("input:radio[name='" + ids.sidebar[axis].data_type + "']:checked").val() === vals.data_type.genetic) {
            _description = metaData.getProfileDescription($("#" + ids.sidebar[axis].gene).val(), _id); 
        } else {
            _description = metaData.getClinicalAttrDescription(_id);
        }
        elem.svg.select("." + d3_class[axis].title_help).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + _description + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right', viewport: $(window)}
                    }
                );
            }
        );            

    }
    
    function appendGlyphs() {
        var legend = elem.svg.selectAll(".legend")
                .data(glyphs)
                .enter().append("g")
                .attr("class", "legend")
                .attr("transform", function(d, i) {
                    return "translate(680, " + (25 + i * 15) + ")";
                });

            legend.append("path")
                .attr("width", 18)
                .attr("height", 18)
                .attr("d", d3.svg.symbol()
                    .size(30)
                    .type(function(d) { return d.symbol; }))
                .attr("fill", function (d) { return d.fill; })
                .attr("stroke", function (d) { return d.stroke; })
                .attr("stroke-width", 1.1);

            legend.append("text")
                .attr("dx", ".75em")
                .attr("dy", ".35em")
                .style("text-anchor", "front")
                .text(function(d){return d.legendText;});
        
        //co-expression scores
        if (stat.apply_co_exp) {
            var coExpLegend = elem.svg.selectAll(".coexp_legend")
                .data(["Correlation", "Pearson: " + scores.pearson, "Spearman: " + scores.spearman])
                .enter().append("g")
                .attr("class", "coexp_legend")
                .attr("transform", function(d, i) {
                    return "translate(670, " + (150 + i * 15) + ")";
                });
                coExpLegend.append("text")
                        .attr("dx", ".75em")
                        .attr("dy", ".35em")
                        .style("text-anchor", "front")
                        .text(function(d) {
                            return d;
                        });
        }
        
        //no-mutation data info
//        var _stat = plotsData.stat();
//        if (!_stat.x.has_mutation_data && $("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic) {
//            var mut_exp_x = 
//                elem.svg.selectAll(".mut_exp_x")
//                .data(["*No Mutation Data for " + $("#" + ids.sidebar.x.gene).val()])
//                .enter().append("g")
//                .attr("class", "mut_exp_x")
//                .attr("transform", function() {
//                    return "translate(615, 500)";
//                });
//                mut_exp_x.append("text")
//                    .attr("dx", ".75em")
//                    .attr("dy", ".35em")
//                    .style("text-anchor", "front")
//                    .style("font-size", "11px")
//                    .text(function(d) {return d;}); 
//        }
//        if (!_stat.y.has_mutation_data && $("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.genetic) {
//            if (!(genetic_vs_genetic() && isSameGene())) {
//                var mut_exp_y = elem.svg.selectAll(".mut_exp_y")
//                    .data(["*No Mutation Data for " + $("#" + ids.sidebar.y.gene).val()])
//                    .enter().append("g")
//                    .attr("class", "mut_exp_y")
//                    .attr("transform", function() {
//                        return "translate(615, 510)";
//                    });
//                    mut_exp_y.append("text")
//                            .attr("dx", ".75em")
//                            .attr("dy", ".35em")
//                            .style("font-size", "11px")
//                            .style("text-anchor", "front")
//                            .text(function(d) {return d;});                 
//                }
//        }
    }
    
    function applyMouseover() {
        elem.dotsGroup.selectAll("path").each(function(d) {
            var _content = "<strong><a href='" +
                cbio.util.getLinkToSampleView(window.QuerySession.getCancerStudyIds()[0], d.caseId) +
                "' target = '_blank'>" + d.caseId +
                "</a></strong>";
            if (genetic_vs_genetic()) {
                if (!(is_profile_discretized("x")) && !(is_profile_discretized("y"))) {
                    _content += "<br>Horizontal: <b>" + d.xVal + "</b><br>" + "Vertical: <b>" + d.yVal + "</b>";
                } else if (is_profile_discretized("x")) {
                    _content += "<br>Horizontal: <b>" + gisticInterpreter.convert_to_val(d.xVal) + "</b><br>" + "Vertical: <b>" + d.yVal + "</b>";
                } else if (is_profile_discretized("y")) {
                    _content += "<br>Horizontal: <b>" + d.xVal + "</b><br>" + "Vertical: <b>" + gisticInterpreter.convert_to_val(d.yVal) + "</b>";
                }
            } else {
                if (genetic_vs_clinical()) {
                    if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin) {
                        var _text_x = clinical_attr_is_discretized("x")? clinical_data_interpreter.convert_to_text(d.xVal, "x"): d.xVal;
                        var _text_y = is_profile_discretized("y")? gisticInterpreter.convert_to_val(d.yVal): d.yVal;
                        _content += "<br>Horizontal: <b>" + _text_x + "</b><br>" + "Vertical: <b>" + _text_y + "</b>";
                    } else if ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.clin) {
                        var _text_x = is_profile_discretized("x")? gisticInterpreter.convert_to_val(d.xVal, "x"): d.xVal;
                        var _text_y = clinical_attr_is_discretized("y")? clinical_data_interpreter.convert_to_text(d.yVal, "y"): d.yVal;
                        _content += "<br>Horizontal: <b>" + _text_x + "</b><br>" + "Vertical: <b>" + _text_y + "</b>";
                    }
                } else if (clinical_vs_clinical()) {
                    if (clinical_attr_is_discretized("x")) {
                        _text_x = clinical_data_interpreter.convert_to_text(d.xVal, "x");
                    } else _text_x = d.xVal;
                    if (clinical_attr_is_discretized("y")) {
                        _text_y = clinical_data_interpreter.convert_to_text(d.yVal, "y");
                    } else _text_y = d.yVal;
                    _content += "<br>Horizontal: <b>" + _text_x + "</b><br>" +
                                "Vertical: <b>" + _text_y + "</b>";
                }
            }
            
            if (Object.keys(d.mutation).length !== 0) {
                $.each(Object.keys(d.mutation), function(index, gene) {
                    _content += "<br>" + gene + ": " + d.mutation[gene].details;
                });
            }

            //make qtip for an element on first mouseenter:
            cbio.util.addTargetedQTip($(this), {content: {text: _content}});           
        });
        var mouseOn = function() {
            var dot = d3.select(this);
            dot.transition()
                .ease("elastic")
                .duration(600)
                .delay(100)
                .attr("d", d3.svg.symbol()
                    .size(200)
                    .type(function(d) {
                        return $(this).attr("shape");
                    })
                );

        };
        var mouseOff = function() {
            var dot = d3.select(this);
            dot.transition()
                .ease("elastic")
                .duration(600)
                .delay(100)
                .attr("d", d3.svg.symbol()
                    .size(function(d) {
                        return $(this).attr("size");
                    })
                    .type(function(d) {
                        return $(this).attr("shape");
                    })
                );
        };
        elem.dotsGroup.selectAll("path").on("mouseover", mouseOn);
        elem.dotsGroup.selectAll("path").on("mouseout", mouseOff);
    }
    
    function get_tab_delimited_data() {
        var result_str = "";
        var elt_x = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)? document.getElementById(ids.sidebar.x.profile_name):document.getElementById(ids.sidebar.x.clin_attr);
        var elt_y = ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.genetic)? document.getElementById(ids.sidebar.y.profile_name):document.getElementById(ids.sidebar.y.clin_attr);
        var _title_x = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)? ($("#" + ids.sidebar.x.gene).val() + ", " + elt_x.options[elt_x.selectedIndex].text): elt_x.options[elt_x.selectedIndex].text;
        var _title_y = ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.genetic)? ($("#" + ids.sidebar.y.gene).val() + ", " + elt_y.options[elt_y.selectedIndex].text): elt_y.options[elt_y.selectedIndex].text;            
        //titles
        if (clinical_vs_clinical()) {
            result_str += "Sample Id" + "\t" + _title_x + "\t" + _title_y + "\n";
        } else {
            result_str += "Sample Id" + "\t" + _title_x + "\t" + _title_y + "\t" + "Mutations" + "\n";
        }
        //single rows
        $.each(data, function(index, _obj) {
            //case Id
            var _current_line = _obj.caseId + "\t";
            //x,y value
            if (genetic_vs_genetic()) {
                if (stat.applied_box_plots) {
                    if (stat.box_plots_axis === "x") {
                        _current_line += gisticInterpreter.convert_to_val(_obj.xVal) + "\t" + _obj.yVal + "\t";
                    } else if (stat.box_plots_axis === "y") {
                        _current_line +=  _obj.xVal + "\t" + gisticInterpreter.convert_to_val(_obj.yVal) + "\t";
                    }
                } else {
                    _current_line += _obj.xVal + "\t" + _obj.yVal + "\t";
                }                    
            } else {
                if (genetic_vs_clinical()) {
                    if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin) {
                        var _type = metaData.getClinicalAttrType($("#" + ids.sidebar.x.clin_attr).val());
                        if (_type === "STRING") {
                            _current_line += clinical_data_interpreter.convert_to_text(_obj.xVal, "x") + "\t" + _obj.yVal + "\t";
                        } else if (_type === "NUMBER") {
                            _current_line += _obj.xVal + "\t" + _obj.yVal + "\t";
                        }
                    } else if ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.clin) {
                        var _type = metaData.getClinicalAttrType($("#" + ids.sidebar.y.clin_attr).val());
                        if (_type === "STRING") {
                            _current_line += _obj.xVal + "\t" + clinical_data_interpreter.convert_to_text(_obj.yVal, "y") + "\t";
                        } else if (_type === "NUMBER") {
                            _current_line += _obj.xVal + "\t" + _obj.yVal + "\t";
                        }
                    }
                } else if (clinical_vs_clinical()) {
                    var _type_x = metaData.getClinicalAttrType($("#" + ids.sidebar.x.clin_attr).val());
                    var _type_y = metaData.getClinicalAttrType($("#" + ids.sidebar.y.clin_attr).val());
                    var _text_x, _text_y;
                    if (_type_x === "STRING") {
                        _text_x = clinical_data_interpreter.convert_to_text(_obj.xVal, "x");
                    } else _text_x = _obj.xVal;
                    if (_type_y === "STRING") {
                        _text_y = clinical_data_interpreter.convert_to_text(_obj.yVal, "y");
                    } else _text_y = _obj.yVal;
                    _current_line += _text_x + "\t" + _text_y + "\t";
                }
            }
            //extract mutation details
            if(!clinical_vs_clinical()) {
                var _mutation_details = "";
                if (typeof(_obj.mutation) !== "undefined" && _obj.mutation !== null) {
                    $.each(Object.keys(_obj.mutation), function(index, gene) {
                        _mutation_details += gene + ": " + _obj.mutation[gene].details;
                    });
                }
                _current_line += _mutation_details + "\t";
            } 
            //assemble overall result string
            result_str += _current_line + "\n";
        });
        return result_str;
    }
    
    return {
        init: function(_div, _data, _apply_box_plots, _box_plots_axis, _calculate_co_exp) {

            data = [];
            glyphs = [];
            data.length = 0;
            glyphs.length = 0;

            div = _div;
            stat.applied_box_plots = _apply_box_plots;
            stat.box_plots_axis = _box_plots_axis;
            
            //convert input data from JSON to array
            for (var key in _data) {
                data.push(_data[key]);
            }
            
            stat.apply_co_exp = _calculate_co_exp && (data.length > 1);
            
            if (data.length === 0) {
                error_msg();
            } else {
                if (stat.apply_co_exp) {
                    var tmpGeneXcoExpStr = "",
                        tmpGeneYcoExpStr = "";
                    $.each(data, function(index, obj) {
                        if (obj.xVal.length !== 0 && obj.xVal !== "" && obj.yVal.length !== 0 && obj.yVal !== "") {
                            tmpGeneXcoExpStr += obj.xVal + " ";
                            tmpGeneYcoExpStr += obj.yVal + " ";
                        }
                    });
                    var paramsCalcCoexp = {
                        gene_x : tmpGeneXcoExpStr,
                        gene_y : tmpGeneYcoExpStr
                    };
                    $.post("calcCoExp.do", paramsCalcCoexp, getCalcCoExpCallBack, "json");
                } else {
                    render();
                }                
            }
            
            function getCalcCoExpCallBack(result) {
                var tmpArrCoexpScores = result.split(" ");
                scores.pearson = parseFloat(tmpArrCoexpScores[0]).toFixed(3);
                scores.spearman = parseFloat(tmpArrCoexpScores[1]).toFixed(3);
                render();
            }
            
            function error_msg() {
                $("#" + _div).empty();
                initCanvas();
                elem.svg.append("text")
                    .attr("x", 350)
                    .attr("y", 70)
                    .attr("text-anchor", "middle")
                    .attr("fill", "#DF3A01")
                    .text("Sorry, no data for selected query.");
                elem.svg.append("rect")
                    .attr("x", 150)
                    .attr("y", 30)
                    .attr("width", 400)
                    .attr("height", 70)
                    .attr("fill", "none")
                    .attr("stroke-width", 1)
                    .attr("stroke", "#BDBDBD");
            }
            
            function render() {
                $("#" + _div).empty();
                initCanvas(div);
                initAxis("x");
                initAxis("y");
                drawAxis("x");
                drawAxis("y");
                if (_apply_box_plots) {
                    boxPlots.init(data, plotsData.stat(), _box_plots_axis, elem);
                }
                drawDots(_apply_box_plots, _box_plots_axis);
                applyMouseover();
                appendTitle("x");
                appendTitle("y");
                appendGlyphs();
                if (!$('#' + ids.sidebar.x.log_scale).is(':empty')) {
                    log_scale();
                }
                if (!$('#' + ids.sidebar.y.log_scale).is(':empty')) {
                    log_scale();
                }
            }
        },
        addGlyph: function(obj) {
            glyphs.push(obj);
        },
        isGlyphExist: function(type_id) {
            var _result = false;
            $.each(glyphs, function(index, _obj) {
                if(_obj.typeName === type_id) _result = true; 
            });
            return _result;
        },
        isGisticGlyphExist: function(_value) {
            var _result = false;
            $.each(glyphs, function(index, obj) {
                if (obj.value === _value) {
                    _result = true;
                }
            });
            return _result;
        },
        log_scale: log_scale,
        get_tab_delimited_data: get_tab_delimited_data
    };

}());