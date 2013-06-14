// Gideon Dresdner June 2013
//
// thanks Mike Bostock, http://bl.ocks.org/mbostock/4341954
;

// some utility functions for calculating kernel density estimates, a few
// kernel to try, and some helper functions for processing mutation allele data
// from the patient view
var AlleleFreqPlotUtils = (function() {
    var NO_DATA = -1;       // this was set by the patient view

    // params: alt_counts (array of numbers), ref_counts (array of numbers)
    // returns array of numbers
    //
    // returns an array of alt_count / (alt_count + ref_count).  If either
    // alt_count or ref_count equal NO_DATA, then returns 0 in that entry
    var process_data = function(alt_counts, ref_counts) {
        return d3.zip(alt_counts, ref_counts).map(function(pair) {
            return pair[0] === NO_DATA === pair[1] ? 0 : (pair[0] / (pair[0] + pair[1]));
        });
    };

    // params: instance of GenomicEventObserver, from the patient view
    //
    // extracts the relevant data from the GenomicEventObserver and runs
    // process_data
    var extract_and_process = function(genomicEventObs) {
        var alt_counts = genomicEventObs.mutations.data['alt-count'];
        var ref_counts = genomicEventObs.mutations.data['ref-count'];

        return process_data(alt_counts, ref_counts);
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

    // params: scale, smoothing a parameter
    // Epanechnikov kernel function
    var epanechnikovKernel = function (scale) {
        return function(u) {
            return Math.abs(u /= scale) <= 1 ? .75 * (1 - u * u) / scale : 0;
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
            return x.map(function(x) {
                return [x, d3.mean(sample, function(v) { return kernel(x - v); })];
            });
        };
    };

    // params: u, some number
    // uniform kernel
    var uniform = function(u) {
        return Math.abs(u) <= 1 ? .5 * u : 0;
    };

    return {
        process_data: process_data,
        extract_and_process: extract_and_process,
        kernelDensityEstimator: kernelDensityEstimator,
        epanechnikovKernel: epanechnikovKernel,
        uniform: uniform,
        gaussianKernel: gaussianKernel,
    };
}());

// makes a kernel density plot of the data and sticks it into the div
//
// appends labels and such that are specific to the allele frequency density
// plot
var AlleleFreqPlot = function(div, data) {
    var margin = {top: 20, right: 30, bottom: 30, left: 40},
        width = 560 / 2 - margin.left - margin.right,
        height = 500 / 2 - margin.top - margin.bottom;

    var x = d3.scale.linear()
        .domain([-.1, 1])
        .range([0, width]);

    var y = d3.scale.linear()
        .domain([0, 2.3])
        .range([height, 0]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom")
        .ticks(3);

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left")
        .ticks(5);

    var line = d3.svg.line()
        .x(function(d) { return x(d[0]); })
        .y(function(d) { return y(d[1]); });

    // applies some common line and path css attributes to the selection.  The
    // only reason this isn't getting put into its own css file is due to the
    // spectre of pdf export
    var applyCss = function(selection) {
        return selection
            .attr('fill', 'none')
            .attr('stroke', '#000')
            .attr('shape-rendering', 'crispEdges');
    };

    var svg = d3.select(div).append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    // x axis
    var x_axis = svg.append("g")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    applyCss(x_axis.selectAll('path'));
    applyCss(x_axis.selectAll('line'));

    x_axis
        .append("text")
        .attr("class", "label")
        .attr("x", width)
        .attr("y", -6)
        .style("text-anchor", "end")
        .text("allele frequency");

    // y axis
    var y_axis = svg.append("g")
        .call(yAxis);

    applyCss(y_axis.selectAll('path')).attr('display', 'none');
    applyCss(y_axis.selectAll('line'));

    var utils =  AlleleFreqPlotUtils;        // alias
    var kde = utils.kernelDensityEstimator(utils.gaussianKernel(.07), x.ticks(100));
    var plot_data = kde(data);

    // rescale the y scale to fit actual values
    y.domain([0, d3.max(plot_data.map(function(i) { return i[1]; })) * 1.1 ]);

    var kde_path = svg.append("path")
        .datum(plot_data)
        .attr("d", line)
        .attr('fill', 'none')
        .attr('stroke', '#000')
        .attr('stroke-width', '1.5px');

    return div;
};
