var boxPlots = (function () {
        var data;
        var axis;
        var elem;
        var stat;
        var min;
        var max;
        //data container
        var _arr = [], _pos = 0;
        var datum = {
            pos: 0, //the position on the discretized axis; doesn't represent the real value, since empty dataset got skipped
            val: 0,
            dataset: []
        };
        var color = "#000000";
        var opacity = 0.2;
        
        function render() {
            
            //width for each individual box changes based on the total number of categories
            var width = 0;
            if (_arr.length === 1 || _arr.length === 2) {
                width = 40;
            } else {
                width = 150 / _arr.length;
            }

            $.each(_arr, function(index, obj) {
                
                var top;
                var bottom;
                var quan1;
                var quan2;
                var mean;
                var IQR;
            
                //Find the middle (vertical) line for one box plot
                var midLine = elem[axis].scale(obj.val);

                //convert data from string to float
                var _data = [];
                $.each(obj.dataset, function(index, value) {
                    _data.push(parseFloat(value));
                });
                _data.sort(function(a, b) { return (a - b); });

                var _axis = (axis === "x")? "y": "x"; 
                if (obj.dataset.length === 1) { //if only one data dots, draw a simple line
                    mean = elem[_axis].scale(_data[0]);
                    if (axis === "x") {
                        elem.boxPlots.append("line")
                            .attr("x1", midLine - width)
                            .attr("x2", midLine + width)
                            .attr("y1", mean)
                            .attr("y2", mean)
                            .attr("stroke-width", 2)
                            .attr("stroke", "grey");
                    } else {
                        elem.boxPlots.append("line")
                            .attr("y1", midLine - width)
                            .attr("y2", midLine + width)
                            .attr("x1", mean)
                            .attr("x2", mean)
                            .attr("stroke-width", 2)
                            .attr("stroke", "grey");
                    }

                } else { 
                    if (obj.dataset.length === 2) {
                        mean = elem[_axis].scale((_data[0] + _data[1]) / 2);
                        quan1 = bottom = elem[_axis].scale(_data[0]);
                        quan2 = top = elem[_axis].scale(_data[1]);
                        IQR = Math.abs(quan2 - quan1);
                    } else { //a regular box 
                        var yl = _data.length;
                        if (yl % 2 === 0) {
                            mean = elem[_axis].scale((_data[(yl / 2)-1] + _data[yl / 2]) / 2);
                            if (yl % 4 === 0) {
                                quan1 = elem[_axis].scale((_data[(yl / 4)-1] + _data[yl / 4]) / 2);
                                quan2 = elem[_axis].scale((_data[(3*yl / 4)-1] + _data[3 * yl / 4]) / 2);
                            } else {
                                quan1 = elem[_axis].scale(_data[Math.floor(yl / 4)]);
                                quan2 = elem[_axis].scale(_data[Math.floor(3 * yl / 4)]);
                            }
                        } else {
                            mean = elem[_axis].scale(_data[Math.floor(yl / 2)]);
                            var tmp_yl = Math.floor(yl / 2) + 1;
                            if (tmp_yl % 2 === 0) {
                                quan1 = elem[_axis].scale((_data[tmp_yl / 2 - 1] + _data[tmp_yl / 2]) / 2);
                                quan2 = elem[_axis].scale((_data[(3 * tmp_yl / 2) - 2] + _data[(3 * tmp_yl / 2) - 1]) / 2);
                            } else {
                                quan1 = elem[_axis].scale(_data[Math.floor(tmp_yl / 2)]);
                                quan2 = elem[_axis].scale(_data[tmp_yl - 1 + Math.floor(tmp_yl / 2)]);
                            }
                        }

                        var _scaled_arr = [];
                        $.each(_data, function(index, value) {
                            _scaled_arr.push(elem[_axis].scale(value));
                        });
                        _scaled_arr.sort(function(a,b) { return (a - b); });
                        IQR = Math.abs(quan2 - quan1);

                        if (axis === "x") {
                            var index_top = searchIndexTop(_scaled_arr, (quan2 - 1.5 * IQR));
                            top = _scaled_arr[index_top];
                            var index_bottom = searchIndexBottom(_scaled_arr, (quan1 + 1.5 * IQR));
                            bottom = _scaled_arr[index_bottom];                        
                        } else {
                            var index_top = searchIndexTop(_scaled_arr, (quan1 - 1.5 * IQR));
                            top = _scaled_arr[index_top];
                            var index_bottom = searchIndexBottom(_scaled_arr, (quan2 + 1.5 * IQR));
                            bottom = _scaled_arr[index_bottom];                            
                        }

                    }

                    if (axis === "x") {
                        elem.boxPlots.append("rect")
                            .attr("x", midLine - width)
                            .attr("y", quan2)
                            .attr("width", width * 2)
                            .attr("height", IQR)
                            .attr("fill", "none")
                            .attr("stroke-width", 1)
                            .attr("stroke", color)
                            .attr('opacity', opacity);
                        elem.boxPlots.append("line")
                            .attr("x1", midLine - width)
                            .attr("x2", midLine + width)
                            .attr("y1", mean)
                            .attr("y2", mean)
                            .attr("stroke-width", 2)
                            .attr("stroke", color)
                            .attr('opacity', opacity);
                        elem.boxPlots.append("line")
                            .attr("x1", midLine - width)
                            .attr("x2", midLine + width)
                            .attr("y1", top)
                            .attr("y2", top)
                            .attr("stroke-width", 1)
                            .attr("stroke", color)
                            .attr('opacity', opacity);
                        elem.boxPlots.append("line")
                            .attr("x1", midLine - width)
                            .attr("x2", midLine + width)
                            .attr("y1", bottom)
                            .attr("y2", bottom)
                            .attr("stroke", color)
                            .style("stroke-width", 1)
                            .attr('opacity', opacity);
                        elem.boxPlots.append("line")
                            .attr("x1", midLine)
                            .attr("x2", midLine)
                            .attr("y1", quan1)
                            .attr("y2", bottom)
                            .attr("stroke", color)
                            .attr("stroke-width", 1)
                            .attr('opacity', opacity);
                        elem.boxPlots.append("line")
                            .attr("x1", midLine)
                            .attr("x2", midLine)
                            .attr("y1", quan2)
                            .attr("y2", top)
                            .attr("stroke", color)
                            .style("stroke-width", 1)
                            .attr('opacity', opacity);
                    } else {
                        elem.boxPlots.append("rect") 
                            .attr("x", quan1)
                            .attr("y", midLine - width)
                            .attr("height", width * 2)
                            .attr("width", IQR)
                            .attr("fill", "none")
                            .attr("stroke-width", 1)
                            .attr('opacity', opacity)
                            .attr("stroke", color);
                        elem.boxPlots.append("line") 
                            .attr("y1", midLine - width)
                            .attr("y2", midLine + width)
                            .attr("x1", mean)
                            .attr("x2", mean)
                            .attr("stroke-width", 2)
                            .attr('opacity', opacity)
                            .attr("stroke", color);
                        elem.boxPlots.append("line") 
                            .attr("y1", midLine-30)
                            .attr("y2", midLine+30)
                            .attr("x1", top)
                            .attr("x2", top)
                            .attr("stroke-width", 1)
                            .attr('opacity', opacity)
                            .attr("stroke", "#BDBDBD");
                        elem.boxPlots.append("line")
                            .attr("y1", midLine - width)
                            .attr("y2", midLine + width)
                            .attr("x1", bottom)
                            .attr("x2", bottom)
                            .attr("stroke", color)
                            .attr('opacity', opacity)
                            .style("stroke-width", 1);
                        if (obj.dataset.length === 2) {
                            elem.boxPlots.append("line")
                                .attr("y1", midLine)
                                .attr("y2", midLine)
                                .attr("x1", quan2)
                                .attr("x2", top)
                                .attr("stroke", color)
                                .attr('opacity', opacity)
                                .attr("stroke-width", 1);
                            elem.boxPlots.append("line")
                                .attr("y1", midLine)
                                .attr("y2", midLine)
                                .attr("x1", quan1)
                                .attr("x2", bottom)
                                .attr("stroke", color)
                                .attr('opacity', opacity)
                                .style("stroke-width", 1); 
                        } else {
                            elem.boxPlots.append("line")
                                .attr("y1", midLine)
                                .attr("y2", midLine)
                                .attr("x1", quan2)
                                .attr("x2", bottom)
                                .attr("stroke", color)
                                .attr('opacity', opacity)
                                .attr("stroke-width", 1);
                            elem.boxPlots.append("line")
                                .attr("y1", midLine)
                                .attr("y2", midLine)
                                .attr("x1", quan1)
                                .attr("x2", top)
                                .attr("stroke", color)
                                .attr('opacity', opacity)
                                .style("stroke-width", 1); 
                        }
                    } 
                }
            });
        }
        
        return {
            init: function(input_data, input_data_stat, input_axis, input_elem){
                
                d3.select("#" + ids.main_view.div).select("." + d3_class.box_plots).remove();
                input_elem.boxPlots = input_elem.svg.append("svg:g").attr("class", d3_class.box_plots);
                
                //clear out data container
                _arr = [];
                _arr.length = 0;
                _pos = 0;
                
                data = input_data;
                axis = input_axis;
                elem = input_elem;
                stat = input_data_stat;
                
                min = stat[axis].min;
                max = stat[axis].max;

                //divide data by discretized values
                for (var i = min ; i < max + 1; i++) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.val = i;
                    $.each(data, function(index, obj) {
                        var _val = (axis === "x")? obj.xVal: obj.yVal;
                        if (_val === i.toString()) {
                            var _tmp = (axis === "x")? obj.yVal: obj.xVal;
                            _datum.dataset.push(_tmp);
                        }
                    });
                    if (_datum.dataset.length !== 0) {
                        _datum.pos = _pos;
                        _pos += 1;
                        _arr.push(_datum);
                    }
                }
                
                render();
            }
        };
        
    }());
    
        
