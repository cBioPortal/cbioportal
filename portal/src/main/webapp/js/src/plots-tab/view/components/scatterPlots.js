var scatterPlots = (function() {
    
    var settings = {
            canvas_width: 800,
            canvas_height: 600,
            axis: {
                x: {
                    range_min: 120,
                    range_max: 620,
                    orient: "bottom",
                    title_x: 350,
                    title_y: 590,
                    help_x: 350,
                    help_y: 575
                },
                y: {
                    range_min: 520,
                    range_max: 20,
                    orient: "left",
                    title_x: -240,
                    title_y: 55,
                    help_x: 135,
                    help_y: 125
                }
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
            box_plots_axis: ""
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
        
        if (genetic_vs_genetic()) {
            if (stat.applied_box_plots && stat.box_plots_axis === axis) {
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
                discretized_axis(_text_set);
            } else {
                continuous_axis();
            }
        } else {
            if ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.clin) {
                var _type = metaData.getClinicalAttrType($("#" + ids.sidebar[axis].clin_attr).val());
                if (_type === "STRING") {
                    discretized_axis(clinical_data_interpreter.get_text_labels(axis));
                } else if (_type === "NUMBER") {
                    continuous_axis();
                }
            } else {
                continuous_axis();
            }
        }
        
        function discretized_axis(_text_set) {
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
                .text(function(d){return d;});
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(" + bottom_x + ", " + bottom_y + ")")
                .call(elem[axis].axis.orient(settings.axis[axis].orient).ticks(0));              
        }
        function continuous_axis() {
            
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
                .style("fill", "black");
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
                .attr("stroke", function(d){
                    return mutationInterpreter.getStroke(d);
                })
                .attr("stroke-width", 1.2); 
        }

    }
    
    function log_scale() {
        
        var re_scale = function(_axis) {
            var _stat = plotsData.stat();
            var _new_min = Math.log(_stat[_axis].min) / Math.log(2);
            var _new_max = Math.log(_stat[_axis].max) / Math.log(2);
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
            _stat[_axis].min = Math.log(_stat[_axis].min) / Math.log(2);
            _stat[_axis].max = Math.log(_stat[_axis].max) / Math.log(2);
            if (_axis === "x") _stat[_axis].edge = (_stat[_axis].max - _stat[_axis].min) * 0.1;
            else if (_axis === "y") _stat[_axis].edge = (_stat[_axis].max - _stat[_axis].min) * 0.2;
            return _stat;
        };
        
        var update_data = function(_axis) {
            var _data = jQuery.extend(true, [], data);
            var _attr_name = (_axis === "x")? "yVal": "xVal";
            $.each(_data, function(index, _obj) {
                _obj[_attr_name] = (Math.log(_obj[_attr_name]) / Math.log(2)).toString();
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
            var _previous_text = d3.select("#plots-box").select("." + d3_class[_axis].axis_title).text();
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
                               var _x = _new_x_scale(Math.log(d3.select(this).attr("x_val")) / Math.log(2));
                               var _y = _new_y_scale(Math.log(d3.select(this).attr("y_val")) / Math.log(2));
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
                               var _log_pos = _new_scale(Math.log(d3.select(this).attr("x_val")) / Math.log(2));
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
                               var _log_pos = _new_scale(Math.log(d3.select(this).attr("y_val")) / Math.log(2));
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
            var _profile_axis = ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.genetic)? "x": "y";
            var _clin_axis = ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.genetic)? "y": "x";
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
                       var _log_pos = _new_scale(Math.log(d3.select(this).attr(_profile_axis + "_val")) / Math.log(2));
                       var _pre_pos = d3.select(this).attr(_clin_axis + "_pos");
                       return "translate(" + _pre_pos + ", " + _log_pos + ")";    
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
        var elt = ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.genetic)? document.getElementById(ids.sidebar[axis].profile_name):document.getElementById(ids.sidebar[axis].clin_attr);
        var _name = ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.genetic)? ($("#" + ids.sidebar[axis].gene).val() + ", " + elt.options[elt.selectedIndex].text): elt.options[elt.selectedIndex].text;
        var _id = elt.options[elt.selectedIndex].value;
        
        var _tmp_attr = (axis === "y")? "rotate(-90)": "";
        d3.select("#" + div).select(d3_class[axis].axis_title).remove();
        elem.axisTitleGroup.append("text")
            .attr("class", d3_class[axis].axis_title)
            .attr("transform", _tmp_attr)
            .attr("x", settings.axis[axis].title_x)
            .attr("y", settings.axis[axis].title_y)
            .style("text-anchor", "middle")
            .style("font-weight","bold")
            .text(_name);
        
        //append help icon (mouseover)
        var _pos_x = (axis==="x")? (settings.axis.x.title_x + _name.length / 2 * 8 + 5): (settings.axis.y.title_y - 11);
        var _pos_y = (axis==="x")? (settings.axis.x.title_y - 12): (settings.axis.y.title_x + 455 - _name.length / 2 * 8 );
        elem.axisTitleGroup.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", d3_class[axis].title_help)
            .attr("x", _pos_x)
            .attr("y", _pos_y)
            .attr("width", "16")
            .attr("height", "16");
        var _description = "";
        if ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.genetic) {
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
                    return "translate(630, " + (25 + i * 15) + ")";
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
    }
    
    function applyMouseover() {
        elem.dotsGroup.selectAll("path").each(function(d) {
            var _content = "<strong><a href='" +
                cbio.util.getLinkToSampleView(window.PortalGlobals.getCancerStudyId(), d.caseId) +
                "' target = '_blank'>" + d.caseId +
                "</a></strong>";
            if (genetic_vs_genetic()) {
                if (stat.applied_box_plots) {
                    if (stat.box_plots_axis === "x") {
                        _content += "<br>Horizontal: <b>" + gisticInterpreter.convert_to_val(d.xVal) + "</b><br>" + "Vertical: <b>" + d.yVal + "</b>";
                    } else if (stat.box_plots_axis === "y") {
                        _content += "<br>Horizontal: <b>" + d.xVal + "</b><br>" + "Vertical: <b>" + gisticInterpreter.convert_to_val(d.yVal) + "</b>";
                    }
                } else {
                    _content += "<br>Horizontal: <b>" + d.xVal + "</b><br>" + "Vertical: <b>" + d.yVal + "</b>";
                }
            } else {
                if (genetic_vs_clinical()) {
                    if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.clin) {
                        var _type = metaData.getClinicalAttrType($("#" + ids.sidebar.x.clin_attr).val());
                        if (_type === "STRING") {
                            _content += "<br>Horizontal: <b>" + clinical_data_interpreter.convert_to_text(d.xVal, "x") + "</b><br>" +
                                        "Vertical: <b>" + d.yVal + "</b>";
                        } else if (_type === "NUMBER") {
                            _content += "<br>Horizontal: <b>" + d.xVal + "</b><br>" + "Vertical: <b>" + d.yVal + "</b>";
                        }
                    } else if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.clin) {
                        var _type = metaData.getClinicalAttrType($("#" + ids.sidebar.y.clin_attr).val());
                        if (_type === "STRING") {
                            _content += "<br>Horizontal: <b>" + d.xVal + "</b><br>" +
                                        "Vertical: <b>" + clinical_data_interpreter.convert_to_text(d.yVal, "y") + "</b>";
                        } else if (_type === "NUMBER") {
                            _content += "<br>Horizontal: <b>" + d.xVal + "</b><br>" + "Vertical: <b>" + d.yVal + "</b>";
                        }
                    }
                } else if (clinical_vs_clinical()) {
                    var _type_x = metaData.getClinicalAttrType($("#" + ids.sidebar.x.clin_attr).val());
                    var _type_y = metaData.getClinicalAttrType($("#" + ids.sidebar.y.clin_attr).val());
                    var _text_x, _text_y;
                    if (_type_x === "STRING") {
                        _text_x = clinical_data_interpreter.convert_to_text(d.xVal, "x");
                    } else _text_x = d.xVal;
                    if (_type_y === "STRING") {
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

            $(this).qtip(
                {
                    content: {text: _content},
                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                    show: {event: "mouseover"},
                    hide: {fixed:true, delay: 100, event: "mouseout"},
                    position: {my:'left bottom',at:'top right', viewport: $(window)}
                }
            );
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
    
    function append_image_converters() {
        var pdfConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank' " +
            "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
            "<input type='hidden' name='svgelement'>" +
            "<input type='hidden' name='filetype' value='pdf'>" +
            "<input type='hidden' name='filename' value='correlation_plot-" + userSelection.gene + ".pdf'>" +
            "<input type='submit' value='PDF'></form>";
        $('#view_title').append(pdfConverterForm);
        var svgConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank'" +
            "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
            "<input type='hidden' name='svgelement'>" +
            "<input type='hidden' name='filetype' value='svg'>" +
            "<input type='hidden' name='filename' value='correlation_plot-" + userSelection.gene + ".svg'>" +
            "<input type='submit' value='SVG'></form>";
        $('#view_title').append(svgConverterForm);
    }
    
    return {
        init: function(_div, _data, _apply_box_plots, _box_plots_axis) {
            
            div = _div;
            stat.applied_box_plots = _apply_box_plots;
            stat.box_plots_axis = _box_plots_axis;
            
            data = [];
            glyphs = [];
            data.length = 0;
            glyphs.length = 0;

            //convert input data from JSON to array
            for (var key in _data) {
                data.push(_data[key]);
            }
            
            $("#" + _div).empty();
            //rendering
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
        log_scale: log_scale
    };
    
}());