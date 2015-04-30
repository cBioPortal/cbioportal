var heat_map = (function() {
    
    var raw_data = {};
        data = [],
        stat = {
            x: {
                min: "0",
                max: "0"
            },
            y: {
                min: "0",
                max: "0"
            },
            count: {
                min: 0,
                max: 0
            }
        },
        settings = {
            max_x_title_length: 70,
            max_y_title_length: 30
        };
        
    
    function process_data(_raw_data) {
        var datum = {
            count: 0,
            x: "0",
            y: "0"
        }, _data_arr = [];
        
        for(var key in _raw_data) {
            var _exist = false;
            var _dot_obj = _raw_data[key];
            //see if x,y pair already exists
            if (_data_arr.length > 0) {
                $.each(_data_arr, function(index, obj) {
                    if (obj.x === _dot_obj.xVal && obj.y === _dot_obj.yVal) {
                        _exist = true;
                        obj.count += 1;
                    } 
                }); 
            } 
            //if not, create and push a new datum
            if (!_exist) {
                var _datum = jQuery.extend(true, {}, datum);
                _datum.x = _dot_obj.xVal;
                _datum.y = _dot_obj.yVal;
                _datum.count = 1;
                _data_arr.push(_datum);
                //find max & min  - x & y
                if (parseInt(_dot_obj.xVal) < parseInt(stat.x.min)) stat.x.min = _dot_obj.xVal;
                if (parseInt(_dot_obj.xVal) > parseInt(stat.x.max)) stat.x.max = _dot_obj.xVal;
                if (parseInt(_dot_obj.yVal) < parseInt(stat.y.min)) stat.y.min = _dot_obj.yVal;
                if (parseInt(_dot_obj.yVal) > parseInt(stat.y.max)) stat.y.max = _dot_obj.yVal;
            }
        }
        
        // find min & max count
        $.each(_data_arr, function(index, obj) {
            if (obj.count > stat.count.max) stat.count.max = obj.count;
            if (obj.count < stat.count.min) stat.count.min = obj.count;
        });
        
        //fill the gaps -- in order to have a full matrix
        for (var i = parseInt(stat.x.min); i < parseInt(stat.x.max) + 1; i++) {
            for (var j = parseInt(stat.y.min); j < parseInt(stat.y.max) + 1; j++) {
                var _found = false;
                $.each(_data_arr, function(index, obj) {
                    if (obj.x === i.toString() && obj.y === j.toString()) {
                        _found = true;
                        data.push(obj);
                    }
                });
                if (!_found) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.x = i.toString();
                    _datum.y = j.toString();
                    _datum.count = 0;
                    data.push(_datum);
                }
            }
        }
    }
    
    var render = function(div) {
        
        $("#" + div).empty();
        
        var h = 20; //height of each row
        var w = 70; //width of each column
        var edge_top = 200;
        var edge_left = 100;
        
        var svg = d3.select("#" + div)
                    .append("svg")
                    .attr("width", 200 + (stat.x.max - stat.x.min) * 150 + 150) 
                    .attr("height", 400 + (stat.y.max - stat.y.min) * 100);
            
        var axisTitleGroup = svg.append("svg:g").attr("class", "axis");
            
        var colorScale = d3.scale.linear()
            .domain([stat.count.min, stat.count.max])
            .range(["white", "#0066CC"]);
            
        var textColorScale = d3.scale.linear()
            .domain([stat.count.min + (stat.count.max - stat.count.min)/2 , stat.count.max])
            .range(["black", "black"]);
    
    
        //pile rects
        var heatmapRects = svg.selectAll("rect")
            .data(function(d) {
                var _data = jQuery.extend(true, [], data);
                if (stat.x.min < 0) {
                    var _gap = 0 - stat.x.min;
                    $.each(_data, function(index, obj) {
                        obj.x = parseInt(obj.x) + _gap;
                    });
                } 
                if (stat.y.min < 0) {
                    var _gap = 0 - stat.y.min;
                    $.each(_data, function(index, obj) {
                        obj.y = parseInt(obj.y) + _gap;
                    });
                }
                return _data;
            })
            .enter()
            .append("svg:rect")
            .attr('width',w)
            .attr('height',h)
            .attr('x', function(d) {
                return (d.x * w) + edge_left;
            })
            .attr('y', function(d) {
                return (d.y * h) + edge_top;
            })
            .style('fill',function(d) {
                return colorScale(d.count);
            })
            .style('stroke', "#D0D0D0")
            .attr("count", function(d) { return d.count; });
    
        //annotation for each brick
        svg.selectAll(".overlayText")
                .data(function(d) {
                    var _data = jQuery.extend(true, [], data);
                    if (stat.x.min < 0) {
                        var _gap = 0 - stat.x.min;
                        $.each(_data, function(index, obj) {
                            obj.x = parseInt(obj.x) + _gap;
                        });
                    } 
                    if (stat.y.min < 0) {
                        var _gap = 0 - stat.y.min;
                        $.each(_data, function(index, obj) {
                            obj.y = parseInt(obj.y) + _gap;
                        });
                    }
                    return _data;
                })
                .enter().append("text")
                    .attr("x", function(d) { return ((d.x * w) + w / 2 - 4 + edge_left);})
                    .attr("y", function(d) { return ((d.y * h) + h / 2 + 5 + edge_top); })
                    .attr("text-anchor", "middle")
                    .attr("fill", function(d) { 
                        if (d.count > (stat.count.min + (stat.count.max - stat.count.min)/2)) {
                            return "white";
                        } else return "black";
                    })
                    .text(function(d) { return d.count; });
        
        //labels for rows/columns
        var _col_text_set = [], _row_text_set = [];
        if (clinical_vs_clinical()) {
            _col_text_set = clinical_data_interpreter.get_text_labels("x");
            _row_text_set = clinical_data_interpreter.get_text_labels("y");
        } else if (genetic_vs_genetic()) {
            for (var i = stat.x.min; i < (parseInt(stat.x.max) + 1); i++) {
                _col_text_set.push(gisticInterpreter.convert_to_val(i));
            }
            for (var j = stat.y.min; j < (parseInt(stat.y.max) + 1); j++) {
                _row_text_set.push(gisticInterpreter.convert_to_val(j));
            }
        } else if (genetic_vs_clinical()) {
            if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic) {
                for (var i = stat.x.min; i < (parseInt(stat.x.max) + 1); i++) {
                    _col_text_set.push(gisticInterpreter.convert_to_val(i));
                }
                _row_text_set = clinical_data_interpreter.get_text_labels("y");
            } else {
                _col_text_set = clinical_data_interpreter.get_text_labels("x");
                for (var j = stat.y.min; j < (parseInt(stat.y.max) + 1); j++) {
                    _row_text_set.push(gisticInterpreter.convert_to_val(j));
                }
            }
        }
        svg.selectAll(".colLabel")
            .data(_col_text_set)
            .enter().append('text')
            .attr("dy", ".35em")
            .attr("transform", function(d, i) {
                return "translate(" + ((i + 0.5) * w + edge_left) + "," + (edge_top - 10) + ") rotate(-15)";
            })
            .attr('class','label')
            .style('text-anchor','start')
            .text(function(d) {return d;});
        svg.selectAll(".rowLabel")
                .data(_row_text_set)
                .enter().append('svg:text')
                .attr('x', parseInt(stat.x.max - stat.x.min) * w + w + 10 + edge_left)
                .attr('y', function(d, i) {
                    return ((i + 0.5) * h) + edge_top;
                })
                .attr('class', 'label')
                .attr('text-anchor', 'start')
                .text(function(d) {return d;}); 

        //axis titles & helps
        var elt_x = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)?document.getElementById(ids.sidebar.x.profile_name):document.getElementById(ids.sidebar.x.clin_attr);
        var elt_y = ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.genetic)?document.getElementById(ids.sidebar.y.profile_name):document.getElementById(ids.sidebar.y.clin_attr);
        var _x_text = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)? $("#" + ids.sidebar.x.gene).val() + ", " + elt_x.options[elt_x.selectedIndex].text: elt_x.options[elt_x.selectedIndex].text;
        var _y_text = ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.genetic)? $("#" + ids.sidebar.y.gene).val() + ", " + elt_y.options[elt_y.selectedIndex].text: elt_y.options[elt_y.selectedIndex].text; 
        var _x_id = elt_x.options[elt_x.selectedIndex].value;
        var _y_id = elt_y.options[elt_y.selectedIndex].value;
        var _x_description = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)? metaData.getProfileDescription($("#" + ids.sidebar.x.gene).val(), _x_id): metaData.getClinicalAttrDescription(_x_id);
        var _y_description = ($("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() === vals.data_type.genetic)? metaData.getProfileDescription($("#" + ids.sidebar.y.gene).val(), _y_id): metaData.getClinicalAttrDescription(_y_id);
        //trim titles that are too long
        var _x_text_trimmed = (_x_text.length > settings.max_x_title_length)? (_x_text.substring(0, settings.max_x_title_length) + "..."): _x_text;
        var _y_text_trimmed = (_y_text.length > settings.max_y_title_length)? (_y_text.substring(0, settings.max_y_title_length) + "..."): _y_text;
        //append titles
        svg.append("text")
                .attr("x", edge_left + 20)
                .attr("y", parseInt(stat.y.max - stat.y.min) * h + h + 23 + edge_top)
                .text(_x_text_trimmed)
                .attr("class", "x_title")
                .attr("font-family", "sans-serif")
                .attr("font-size", "12px")
                .attr("fill","black");
        svg.append("text")
                .text(_y_text_trimmed)
                .attr("class", "y_title")
                .attr("font-family", "sans-serif")
                .attr("font-size", "12px")
                .attr("fill","black")
                .attr("dy", ".35em")
                .attr("text-anchor", "start")
                .attr("transform", function(d) {
                    return "translate(" + (edge_left - 20) + " ," + (parseInt(stat.y.max - stat.y.min) * h + h + edge_top - 15) + ") rotate(-90)";
                });
        //append mouse-over if the titles are trimmed
        if (_x_text.length > settings.max_x_title_length) {
            svg.select(".x_title").each(
                function() {
                    $(this).qtip(
                        {
                            content: {text: "<font size=2>" +  _x_text + "</font>" },
                            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                            show: {event: "mouseover"},
                            hide: {fixed:true, delay: 100, event: "mouseout"},
                            position: {my:'left top',at:'bottom right', viewport: $(window)}
                        }
                    );
                }
            );     
        }
        if (_y_text.length > settings.max_y_title_length) {
            svg.select(".y_title").each(
                function() {
                    $(this).qtip(
                        {
                            content: {text: "<font size=2>" +  _y_text + "</font>" },
                            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                            show: {event: "mouseover"},
                            hide: {fixed:true, delay: 100, event: "mouseout"},
                            position: {my:'left top',at:'bottom right', viewport: $(window)}
                        }
                    );
                }
            );     
        }
        //append help icon
        svg.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", d3_class.x.title_help)
            .attr("x", edge_left)
            .attr("y", parseInt(stat.y.max - stat.y.min) * h + h + 10 + edge_top)
            .attr("width", "16")
            .attr("height", "16");
        svg.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", d3_class.y.title_help)
            .attr("x", (edge_left - 28))
            .attr("y", parseInt(stat.y.max - stat.y.min) * h + h + edge_top - 10)
            .attr("width", "16")
            .attr("height", "16");

        svg.select("." + d3_class.x.title_help).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" +  _x_description + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left top',at:'bottom right', viewport: $(window)}
                    }
                );
            }
        );  
        svg.select("." + d3_class.y.title_help).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + _y_description  + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'right bottom',at:'top left', viewport: $(window)}
                    }
                );
            }
        );  
    };
    
    function error_msg(div) {
        $("#" + div).empty();
        var svg = d3.select("#" + div)
            .append("svg")
            .attr("width", 800) 
            .attr("height", 600);
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
    
    return {
        init: function(_div, _data) {
            
            raw_data = _data;
            
            data = [];
            data.length = 0;
            stat.x.min = "0";
            stat.x.max = "0";
            stat.y.min = "0";
            stat.y.max = "0";
            stat.count.min = "0";
            stat.count.max = "0";
            
            process_data(_data);
            if (data.length === 0) {
                error_msg(_div);
            } else render(_div);
        },
        get_tab_delimited_data: function() {
            var elt_x = document.getElementById(ids.sidebar.x.clin_attr);
            var elt_y = document.getElementById(ids.sidebar.y.clin_attr);
            var _title_x = elt_x.options[elt_x.selectedIndex].text;
            var _title_y = elt_y.options[elt_y.selectedIndex].text;            
            var result_str = "Sample Id" + "\t" + _title_x + "\t" + _title_y + "\n";
            for(var key in raw_data) {
                var _obj = raw_data[key];
                //case Id
                var _current_line = _obj.caseId + "\t";
                //x,y value
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
                //assemble overall result string
                result_str += _current_line + "\n";
            }
            return result_str;
        }
    };
    
}());