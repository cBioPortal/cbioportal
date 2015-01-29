var heat_map = (function() {
    
    var data = [],
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
        };
    
    function process_data(_raw_data) {
        var datum = {
            count: 0,
            x: 0,
            y: 0
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
                if (_dot_obj.xVal < stat.x.min) stat.x.min = _dot_obj.xVal;
                if (_dot_obj.xVal > stat.x.max) stat.x.max = _dot_obj.xVal;
                if (_dot_obj.yVal < stat.y.min) stat.y.min = _dot_obj.yVal;
                if (_dot_obj.yVal > stat.y.max) stat.y.max = _dot_obj.yVal;
            }
        }
        
        // find min & max count
        $.each(_data_arr, function(index, obj) {
            if (obj.count > stat.count.max) stat.count.max = obj.count;
            if (obj.count < stat.count.min) stat.count.min = obj.count;
        });
        
        //fill the gaps -- in order to have a full matrix
        for (var i = 0; i < parseInt(stat.x.max) + 1; i++) {
            for (var j = 0; j < parseInt(stat.y.max) + 1; j++) {
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
        
        var svg = d3.select("#" + div)
                    .append("svg")
                    .attr("width", 800) 
                    .attr("height", 600);
            
        var colorScale = d3.scale.linear()
            .domain([stat.count.min, stat.count.max])
            .range(["white", "#0066CC"]);
            
        var textColorScale = d3.scale.linear()
            .domain([stat.count.min, stat.count.max])
            .range(["#0066CC", "white"]);
    
    
        //pile rects
        var heatmapRects = svg.selectAll("rect")
            .data(data)
            .enter()
            .append("svg:rect")
            .attr('width',w)
            .attr('height',h)
            .attr('x', function(d) {
                return (d.x * w) + 100;
            })
            .attr('y', function(d) {
                return (d.y * h) + 50;
            })
            .style('fill',function(d) {
                return colorScale(d.count);
            })
            .style('stroke', "#D0D0D0")
            .attr("count", function(d) { return d.count; });
    
        //annotation for each brick
        svg.selectAll(".overlayText").data(data).enter().append("text")
                    .attr("x", function(d) { return ((d.x * w) + w/2 + 90);})
                    .attr("y", function(d) { return ((d.y * h) + h / 2 + 5 + 50); })
                    .attr("fill", function(d) { return textColorScale(d.count); })
                    .text(function(d) { return d.count; });
        
        //labels for rows/columns
        svg.selectAll(".colLabel")
            .data(clinical_data_interpreter.get_text_labels("x"))
            .enter().append('text')
            .attr("dy", ".35em")
            .attr("transform", function(d, i) {
                return "translate(" + ((i + 0.5) * w + 100) + ",40) rotate(-15)";
            })
            .attr('class','label')
            .style('text-anchor','start')
            .text(function(d) {return d;});
        svg.selectAll(".rowLabel")
                .data(clinical_data_interpreter.get_text_labels("y"))
                .enter().append('svg:text')
                .attr('x', parseInt(stat.x.max) * w + 190)
                .attr('y', function(d, i) {
                    return ((i + 0.5) * h) + 53;
                })
                .attr('class', 'label')
                .attr('text-anchor', 'start')
                .text(function(d) {return d;});
        
        //axis titles & helps
        var elt_x = document.getElementById(ids.sidebar.x.clin_attr);
        var elt_y = document.getElementById(ids.sidebar.y.clin_attr);
        var _x_text = "Rows: " + elt_x.options[elt_x.selectedIndex].text;
        var _y_text = "Columns:  " + elt_y.options[elt_y.selectedIndex].text;
        var _x_id = elt_x.options[elt_x.selectedIndex].value;
        var _y_id = elt_x.options[elt_y.selectedIndex].value;
        var _x_description = metaData.getClinicalAttrDescription(_x_id);
        var _y_description = metaData.getClinicalAttrDescription(_y_id);
        svg.append("text")
                .attr("x", 120)
                .attr("y", parseInt(stat.y.max) * h + 120)
                .text(_x_text)
                .attr("font-family", "sans-serif")
                .attr("font-size", "12px")
                .attr("fill","black");
        svg.append("text")
                .attr("x", 120)
                .attr("y", parseInt(stat.y.max) * h + 135)
                .text(_y_text)
                .attr("font-family", "sans-serif")
                .attr("font-size", "12px")
                .attr("fill","black");
        
        svg.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", "x_help")
            .attr("x", 100)
            .attr("y", parseInt(stat.y.max) * h + 108)
            .attr("width", "16")
            .attr("height", "16");
        svg.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", "y_help")
            .attr("x", 100)
            .attr("y", parseInt(stat.y.max) * h + 123)
            .attr("width", "16")
            .attr("height", "16");

        svg.select(".x_help").each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" +  _x_description + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right', viewport: $(window)}
                    }
                );
            }
        );  
        svg.select(".y_help").each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + _y_description  + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right', viewport: $(window)}
                    }
                );
            }
        );  
    };
    
    return {
        init: function(_div, _data) {
            
            data = [];
            data.length = 0;
            stat.x.min = "0";
            stat.x.max = "0";
            stat.y.min = "0";
            stat.y.max = "0";
            stat.count.min = "0";
            stat.count.max = "0";
            
            process_data(_data);
            render(_div);
        }
    };
    
}());