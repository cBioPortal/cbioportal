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
            .range(["white", "blue"]);
    
        var heatmapRow = svg.selectAll(".heatmap")
            .data(data)
            .enter().append("g");
    
        var heatmapRects = heatmapRow.selectAll(".rect")
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
            .style('stroke', "#D0D0D0");
        
        var columnLabel = svg.selectAll(".colLabel")
            .data(clinical_data_interpreter.get_text_labels("x"))
            .enter().append('svg:text')
            .attr('x', function(d,i) {
              return ((i + 0.5) * w) + 100;
            })
            .attr('y', 50)
            .attr('class','label')
            .style('text-anchor','middle')
            .text(function(d) {return d;});
        var rowLabel = svg.selectAll(".rowLabel")
                .data(clinical_data_interpreter.get_text_labels("y"))
                .enter().append('svg:text')
                .attr('x', 90)
                .attr('y', function(d, i) {
                    return ((i + 0.5) * h) + 50;
                })
                .attr('class', 'label')
                .attr('text-anchor', 'end')
                .text(function(d) {return d;});
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