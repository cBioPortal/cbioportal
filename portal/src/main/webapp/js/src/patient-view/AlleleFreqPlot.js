// Gideon Dresdner June 2013
//
// thanks Mike Bostock, http://bl.ocks.org/mbostock/4341954
;

// some utility functions for calculating kernel density estimates, a few
// kernel to try, and some helper functions for processing mutation allele data
// from the patient view
var AlleleFreqPlotUtils = (function() {
    var NO_DATA = undefined;       // this was set by the patient view

    // params: alt_counts (array of numbers), ref_counts (array of numbers)
    // returns array of numbers
    //
    // returns an array of alt_count / (alt_count + ref_count).  If either
    // alt_count or ref_count equal NO_DATA, then returns 0 in that entry
    //
    // or, if there is no valid data, returns `undefined`
    var process_data = function(alt_counts, ref_counts, caseId) {

        // validate:
        // * that data exists
        var validated = _.find(alt_counts, function(data) { return data[caseId]!==NO_DATA; });

        if (!validated) {
            return undefined;
        }

        return d3.zip(alt_counts, ref_counts).map(function(pair) {
            return pair[0][caseId] === NO_DATA === pair[1][caseId] ? 0 : (pair[0][caseId] / (pair[0][caseId] + pair[1][caseId]));
        });
    };

    // params: instance of GenomicEventObserver, from the patient view
    //
    // extracts the relevant data from the GenomicEventObserver and runs
    // process_data
    var extract_and_process = function(genomicEventObs, caseId) {
        var alt_counts = genomicEventObs.mutations.data['alt-count'];
        var ref_counts = genomicEventObs.mutations.data['ref-count'];

        return process_data(alt_counts, ref_counts, caseId);
    };

    // params: variance, basically a smoothing parameter for this situation
    //
    // returns the gaussian function, aka kernel
    var gaussianKernel = function(variance) {
        var mean = 0;
        return function(x) {
            return (1 / (variance * Math.sqrt(2 * Math.PI)))
                * Math.exp( -1 * Math.pow((x - mean), 2) / (2 * Math.pow(variance, 2)));
        };
    };

    // params: kernel, list of points
    //
    // returns a function, call it kde, that takes a list of sample points, samples, and
    // returns a list of pairs [x, y] where y is the average of
    // kernel(sample - x)
    // for all sample in samples
    var kernelDensityEstimator = function(kernel, x) {
        return function(sample) {
            return x.map(function(z) {
                return [z, d3.mean(sample, function(v) { return kernel(z - v); })];
            });
        };
    };

    // params: u, some number
    // uniform kernel
    var uniform = function(u) {
        return Math.abs(u) <= 1 ? .5 * u : 0;
    };

    // Silverman's rule of thumb for calculating the bandwith parameter for
    // kde with Gaussian kernel.  Turns out that one can calculate the optimal
    // bandwidth when using the Gaussian kernel (not a surprise).  It turns out
    // to basically be, a function of the variance of the data and the number
    // of data points.
    //
    // http://en.wikipedia.org/wiki/Kernel_density_estimation#Practical_estimation_of_the_bandwidth
    var calculate_bandwidth = function(data) {
        var mean = d3.mean(data);
        var variance = d3.mean(data.map(function(d) { return Math.pow(d - mean, 2); }));
        var standard_deviation = Math.pow(variance, .5);
        var bandwidth = 1.06 * standard_deviation * Math.pow(data.length, -1/5);

        return bandwidth;
    };

    return {
        process_data: process_data,
        extract_and_process: extract_and_process,
        kernelDensityEstimator: kernelDensityEstimator,
        gaussianKernel: gaussianKernel,
        calculate_bandwidth: calculate_bandwidth
    };
}());

var AlleleFreqPlotMulti = function(div, data, options) {
    //var fillcolors = d3.scale.category10();
    // construct colors, if a duplicate found replace it with 'darker'
    var colors = {};
    var colorhist = {};
    for (var k in data) {
        if (data.hasOwnProperty(k)) {
            /*var ind = Object.keys(colors).length;
            colors[k] = {stroke:fillcolors(ind), fill:fillcolors(ind)};*/
            var col = d3.rgb(window.caseMetaData.color[k]).toString();
            while (col in colorhist && col !== "#000000") {
                col = d3.rgb(col).darker(0.4).toString();
            }
            colorhist[col] = true;
            colors[k] = {stroke:col, fill:col};
        }
    }
    // data is a map of sample id to list of data
    var options = options || { label_font_size: "11.5px", xticks: 3, yticks: 8, nolegend:false}; // init
    
    var label_dist_to_axis = options.xticks === 0 ? 13 : 30;
    
    options.margin = options.margin || {};
    var margin = $.extend({top: 20, right: 30, bottom: 30 + label_dist_to_axis / 2, left: 50},
            options.margin);
    var width = options.width || 200;
    var height = options.height || (500 + label_dist_to_axis) / 2 - margin.top - margin.bottom;
    
    // x scale and axis
    var x = d3.scale.linear()
            .domain([-0.1,1])
            .range([0,width]);
    
    var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom")
            .ticks(options.xticks);
    
    var utils = AlleleFreqPlotUtils; // alias
    
    // make kde's
    var bandwidth = {};
    var kde = {};
    var plot_data = {};
    for (var k in data) {
        if (data.hasOwnProperty(k)) {
            bandwidth[k] = utils.calculate_bandwidth(data[k]);
            kde[k] = utils.kernelDensityEstimator(utils.gaussianKernel(bandwidth[k]), x.ticks(100));
            plot_data[k] = kde[k](data[k]);
        }
    }
    // make histograms
    var histogram = d3.layout.histogram().frequency(false).bins(x.ticks(30));
    var binned_data = {};
    for (var k in data) {
        if (data.hasOwnProperty(k)) {
            binned_data[k] = histogram(data[k]);
        }
    }
    
    // calculate range of values that y takes
    var all_plot_data = [];
    for (var k in plot_data) {
        if (plot_data.hasOwnProperty(k)) {
            all_plot_data = all_plot_data.concat(plot_data[k]);
        }
    }
    var ycoords = all_plot_data.map(function(d) { return d[1]; });
    var ydomain = [d3.min(ycoords), d3.max(ycoords)];
    var y = d3.scale.linear()
            .domain(ydomain)
            .range([height, 0]);
    var line = d3.svg.line()
            .x(function(d) { return x(d[0]); })
            .y(function(d) { return y(d[1]); });
    
    var svg = d3.select(div).append("svg")
            .attr("width", width + margin.left + (options.yticks === 0 ? 0 : margin.right))
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + (options.yticks === 0 ? margin.left / 2 : margin.left) + "," + margin.top + ")");
    
    // x axis
    var x_axis = svg.append("g")
            .attr("transform", "translate(0,"+height+")")
            .call(xAxis);
    
    // applies some common line and path css attributes to the selection. The 
    // only reason this isn't getting put into its own css file is due to the 
    // spectre of pdf export
    var applyCss = function(selection) {
        return selection
                .attr('fill', 'none')
                .attr('stroke', '#000')
                .attr('shape-rendering', 'crispEdges');
    };
    
    applyCss(x_axis.selectAll('path'));
    applyCss(x_axis.selectAll('line'));
    
    // x-axis label
    x_axis
            .append("text")
            //.attr("class", "label")
            .attr("x", width / 2)
            .attr("y", label_dist_to_axis)
            .attr("font-size", options.label_font_size)
            .style("text-anchor", "middle")
            .style("font-style", "italic")
            .text("variant allele frequency");
    
    // make the y-axis mutation count
    var all_binned_data = [];
    for (var k in binned_data) {
        if (binned_data.hasOwnProperty(k)) {
            all_binned_data = all_binned_data.concat(binned_data[k]);
        }
    }
    var mutation_counts = all_binned_data.map(function(d) { return d.length; });
    var mutation_count_range = [0, d3.max(mutation_counts)]
    
    // create y axis
    var yAxis = d3.svg.axis()
            .scale(y.copy().domain(mutation_count_range))
            .orient("left")
            .ticks(options.yticks);
    
    // render axis
    var y_axis = svg.append("g").call(yAxis);
    
    // takes a number and returns a displacement length for the
    // yaxis label
    //
    // *signature:* `number -> number`
    var displace_by_digits = function(n) {
        var stringified = "" + n;
        var no_digits = stringified.split("").length;

        // there will be a comma in the string, i.e. 1,000 not 1000
        if (no_digits >= 4) {
            no_digits += 1.5;
        }

        return no_digits * 7 / 3;
    };
    
    var ylabel_dist_to_axis = label_dist_to_axis;
    ylabel_dist_to_axis += options.yticks === 0 ? 0 : displace_by_digits(mutation_count_range[1]);
    
    // y axis label
    y_axis
            .append("text")
            //.attr("class","label")
            .attr("transform", "rotate(-90)")
            .attr("x", - height/2)
            .attr("y", -ylabel_dist_to_axis)
            .attr("font-size", options.label_font_size)
            .style("text-anchor","middle")
            .style("font-style", "italic")
            .text("mutation count");
    
    applyCss(y_axis.selectAll('path')).attr('display', options.yticks === 0? '' : 'none');
    applyCss(y_axis.selectAll('line'));
    
    // calculate new domain for the binned data
    var binned_yvalues = all_binned_data.map(function(d) { return d.y;});
    var binned_ydomain = [d3.min(binned_yvalues), d3.max(binned_yvalues)];
    var binned_yscale = y.copy();
    binned_yscale.domain(binned_ydomain);
    
    // make bar charts
    for (var k in binned_data) {
        if (binned_data.hasOwnProperty(k)) {
            svg.selectAll(".bar")
                .data(binned_data[k])
                .enter().insert("rect")
                .attr("x", function(d) { return x(d.x) + 1; })
                .attr("y", function(d) { return binned_yscale(d.y); })
                .attr('class', window.caseIds.indexOf(k)+'_viz_hist viz_hist')
                .attr("width", x(binned_data[k][0].dx + binned_data[k][0].x) - x(binned_data[k][0].x) - 1)
                .attr("height", function(d) {return (height - binned_yscale(d.y)); })
                .attr('fill', colors[k].fill)
                .attr('opacity', (Object.keys(data).length > 1 ? '0.1': '0.3'))
                ;
        }
    }
    
    // make kde plots
    for (var k in plot_data) {
        if (plot_data.hasOwnProperty(k)) {
            svg.append("path")
                    .datum(plot_data[k])
                    .attr("d", line)
                    .attr('class', 'curve '+window.caseIds.indexOf(k)+'_viz_curve viz_curve')
                    .attr('fill', 'none')
                    .attr('stroke', colors[k].stroke)
                    .attr('stroke-width', '1.5px')
                    .attr('opacity', '0.9')
                    .attr('data-legend',function() { return k; })
            ;
        }
    }
    
    // make legend
    if (!options.nolegend) {
        var legend_font_size = 13;
        var legend = svg.append("g")
                .attr('class', 'legend')
                .attr('transform', 'translate('+(width-70)+',30)')
                .style('font-size', legend_font_size+"px")
        ;
        d3.legend(legend, legend_font_size);
    }
    return div;
}


// d3.legend.js 
// (C) 2012 ziggy.jonsson.nyc@gmail.com
// Modifications by Adam Abeshouse adama@cbio.mskcc.org
// MIT licence
var highlightSample = function (k) {
    $('.viz_hist').hide();
    $('.viz_curve').hide();
    if (window.allele_freq_plot_histogram_toggle) {
        $("." + window.caseIds.indexOf(k) + "_viz_hist").show();
        $("." + window.caseIds.indexOf(k) + "_viz_hist").attr('opacity', '0.5');
    }
    if (window.allele_freq_plot_curve_toggle) {
        $("." + window.caseIds.indexOf(k) + "_viz_curve").show();
    }
}
var showSamples = function () {
    if (window.allele_freq_plot_histogram_toggle) {
        $('.viz_hist').show();
        $(".viz_hist").attr('opacity', '0.1');
    }
    if (window.allele_freq_plot_curve_toggle) {
        $('.viz_curve').show();
    }
}
d3.legend = function(g, font_size) {
  g.each(function() {
    var g= d3.select(this),
        items = {},
        svg = d3.select(g.property("nearestViewportElement")),
        legendPadding = g.attr("data-style-padding") || 5,
        lb = g.selectAll(".legend-box").data([true]),
        li = g.selectAll(".legend-items").data([true])

    li.enter().append("g").classed("legend-items",true);

    svg.selectAll("[data-legend]").each(function() {
        var self = d3.select(this)
        items[self.attr("data-legend")] = {
          pos : self.attr("data-legend-pos") || this.getBBox().y,
          color : self.attr("data-legend-color") != undefined ? self.attr("data-legend-color") : self.style("fill") != 'none' ? self.style("fill") : self.style("stroke") 
        }
      })

    items = d3.entries(items).sort(function(a,b) { return a.value.pos-b.value.pos})
    
    
        
    var spacing = 0.4;
    li.selectAll("text")
        .data(items,function(d) { return d.key;})
        .call(function(d) { d.enter().append("text");})
        .call(function(d) { d.exit().remove();})
        .attr("y",function(d,i) { return (i-0.1+i*spacing)+"em";})
        .attr("x","1em")
        .text(function(d) { 
            var key = d.key;
            var label = key.length > 9 ? key.substring(0,3) + "..." + key.slice(-3) : key;
            return label;
        })
    ;
    li.selectAll("circle")
        .data(items,function(d) { return d.key})
        .call(function(d) { d.enter().append("circle");})
        .call(function(d) { d.exit().remove()})
        .attr("cy",function(d,i) { return (i-0.5+i*spacing)+"em"})
        .attr("cx",0)
        .attr("r","0.5em")
        .style("fill",function(d) { return d.value.color;})
    ;
    for (var i=0, keys=Object.keys(items); i<keys.length; i++) {
        li.append("text")
        .attr("x",0)
        .attr("y", (i-0.5+i*spacing)*font_size)
        .attr("fill","white")
        .attr("font-size","10")
        .attr("dy","0.34em")
        .attr("text-anchor","middle")
        .text(window.caseMetaData.label[items[keys[i]].key])
        ;
    }
    li.selectAll("rect")
            .data(items, function(d) { return d.key;})
            .call(function(d) { d.enter().append("rect");})
            .call(function(d) { d.exit().remove();})
            .attr("y", function(d,i) { return (i-1+i*spacing-spacing/2)+"em";})
            .attr("x","-0.8em")
            .attr('width',function(d) { return d.key.length+'em';})
            .attr('height',(1+spacing)+'em')
            .style('fill',function(d) { return d.value.color;})
            .attr('opacity', '0')        
            .on('mouseover', (Object.keys(items).length > 1 ? 
                function(d) { 
                    $(this).attr('opacity','0.2'); highlightSample(d.key);
                } : function(d) {}))
            .on('mouseout', function(d) { $(this).attr('opacity','0');})
    ;
    li.on('mouseout', (Object.keys(items).length > 1 ? function() { showSamples(); } : function() {}));
    // Reposition and resize the box
    var lbbox = li[0][0].getBBox()  
    lb.attr("x",(lbbox.x-legendPadding))
        .attr("y",(lbbox.y-legendPadding))
        .attr("height",(lbbox.height+2*legendPadding))
        .attr("width",(lbbox.width+2*legendPadding));
  })
  return g;
}
//
// appends labels and such that are specific to the allele frequency density
// plot
//
// makes a kernel density plot of the data and dumps it into the div.  Optional
// options parameter which supports attributes:
//  * `width`               number
//  * `height`              number
//  * `label_font_size`     string in pixels (px)
//
// *signature:* `DOM el, array, obj -> DOM el (with plot inside)`
var AlleleFreqPlot = function(div, data, options) {
    var options = options || { label_font_size: "10.5px", xticks: 3, yticks: 8 };        // init

    var label_dist_to_axis = options.xticks === 0 ? 13 : 30;

    options.margin = options.margin || {};
    var margin = $.extend({top: 20, right: 30, bottom: 30 + label_dist_to_axis / 2, left: 50},
            options.margin);

    var width = options.width || 200,
        height = options.height || (500 + label_dist_to_axis) / 2 - margin.top - margin.bottom;

    // x scale and axis
    var x = d3.scale.linear()
        .domain([-.1, 1])
        .range([0, width]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom")
        .ticks(options.xticks);

    var utils =  AlleleFreqPlotUtils;        // alias

    // make a kde
    var bandwidth = utils.calculate_bandwidth(data);
    var kde = utils.kernelDensityEstimator(utils.gaussianKernel(bandwidth), x.ticks(100));
    var plot_data = kde(data);

    // make a histogram
    var histogram = d3.layout.histogram()
        .frequency(false)
        .bins(x.ticks(30));
    var binned_data = histogram(data);

    // calculate the range of values that y takes
    var ydomain = plot_data.map(function(d){ return d[1]; });       // plot_data is a list of 2-ples
    ydomain = [d3.min(ydomain), d3.max(ydomain)];

    var y = d3.scale.linear()
        .domain(ydomain)
        .range([height, 0]);

    var line = d3.svg.line()
        .x(function(d) { return x(d[0]); })
        .y(function(d) { return y(d[1]); });

    var svg = d3.select(div).append("svg")
        .attr("width", width + margin.left + (options.yticks === 0 ? 0 : margin.right))
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + (options.yticks === 0 ? margin.left / 2 : margin.left) + "," + margin.top + ")");

    // x axis
    var x_axis = svg.append("g")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    // applies some common line and path css attributes to the selection.  The
    // only reason this isn't getting put into its own css file is due to the
    // spectre of pdf export
    var applyCss = function(selection) {
        return selection
            .attr('fill', 'none')
            .attr('stroke', '#000')
            .attr('shape-rendering', 'crispEdges');
    };

    applyCss(x_axis.selectAll('path'));
    applyCss(x_axis.selectAll('line'));

    // x-axis label
    x_axis
        .append("text")
        .attr("class", "label")
        .attr("x", width / 2)
        .attr("y", label_dist_to_axis)
        .attr('font-size', options.label_font_size)
        .style("text-anchor", "middle")
        .text("variant allele frequency");

    // make the y-axis mutation count
    mutation_count_range = binned_data.map(function(d) { return d.length; });
    var max_no_mutations = d3.max(mutation_count_range);
    mutation_count_range = [d3.min(mutation_count_range),
                                max_no_mutations];

    // create axis
    var yAxis = d3.svg.axis()
        .scale(y.copy().domain(mutation_count_range))
        .orient("left")
        .ticks(options.yticks);

    // render axis
    var y_axis = svg.append("g")
        //.attr("transform", "translate(" + -10 + ",0)")
        .call(yAxis);

    // takes a number and returns a displacement length for the
    // yaxis label
    //
    // *signature:* `number -> number`
    var displace_by_digits = function(n) {
        var stringified = "" + n;
        var no_digits = stringified.split("").length;

        // there will be a comma in the string, i.e. 1,000 not 1000
        if (no_digits >= 4) {
            no_digits += 1.5;
        }

        return no_digits * 7 / 3;
    };

    var ylabel_dist_to_axis = label_dist_to_axis;
    ylabel_dist_to_axis += options.yticks === 0 ? 0 : displace_by_digits(max_no_mutations);

    // y-axis label
    y_axis
        .append("text")
        .attr("class", "label")
        .attr("transform", "rotate(" + "-" + 90 + ")")
        // axis' have also been rotated
        .attr("x", - height / 2)
        .attr("y", - ylabel_dist_to_axis)
        .attr('font-size', options.label_font_size)
        .style("text-anchor", "middle")
        .text("mutation count");

    applyCss(y_axis.selectAll('path')).attr('display', options.yticks === 0 ? '' : 'none');
    applyCss(y_axis.selectAll('line'));

    // calculate a new domain for the binned data
    var binned_ydomain = binned_data.map(function(d) { return d.y; });
    binned_ydomain = [d3.min(binned_ydomain), d3.max(binned_ydomain)];
    var binned_yscale = y.copy();
    binned_yscale.domain(binned_ydomain);

    // make a bar chart
    svg.selectAll(".bar")
        .data(binned_data)
        .enter().insert("rect")
        .attr("x", function(d) { return x(d.x) + 1; })
        .attr("y", function(d) { return binned_yscale(d.y); })
        .attr("width", x(binned_data[0].dx + binned_data[0].x) - x(binned_data[0].x) - 1)
        .attr("height", function(d) {return (height - binned_yscale(d.y)); })
        .attr('fill', '#1974b8')
        ;

    var kde_path = svg.append("path")
        .datum(plot_data)
        .attr("d", line)
        .attr('class', 'curve')
        .attr('fill', 'none')
        .attr('stroke', '#000')
        .attr('stroke-width', '1.5px');

    return div;
};
