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

// makes a kernel density plot of the data and sticks it into the div
//
// appends labels and such that are specific to the allele frequency density
// plot
var AlleleFreqPlot = function(div, data) {
    var label_margin = 30;      // the y coordinate of the label
    var margin = {top: 20, right: 30, bottom: 30 + label_margin / 2, left: 40},
        width = 560 / 2 - margin.left - margin.right,
        height = (500 + label_margin) / 2 - margin.top - margin.bottom;

    var utils =  AlleleFreqPlotUtils;        // alias

    // x scale and axis
    var x = d3.scale.linear()
        .domain([-.1, 1])
        .range([0, width]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom")
        .ticks(3);

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
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

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
        .attr("x", width)
        .attr("y", label_margin)
        .style("text-anchor", "end")
        .text("allele frequency");

    // make the y-axis mutation count
    mutation_count_range = binned_data.map(function(d) { return d.length; });
    mutation_count_range = [d3.min(mutation_count_range),
                                d3.max(mutation_count_range)];

    // create axis
    var yAxis = d3.svg.axis()
        .scale(y.copy().domain(mutation_count_range))
        .orient("left")
        .ticks(5);

    // render axis
    var y_axis = svg.append("g")
        .call(yAxis);

    // y-axis label
    y_axis
        .append("text")
        .attr("class", "label")
        .attr("transform", "rotate(" + 90 + ")")
        .attr("x", height / 3)
        .attr("y", margin.left - 10)
        .style("text-anchor", "start")
        .text("allele count");

    applyCss(y_axis.selectAll('path')).attr('display', 'none');
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
        .attr('fill', 'none')
        .attr('stroke', '#000')
        .attr('stroke-width', '1.5px');

    return div;
};
