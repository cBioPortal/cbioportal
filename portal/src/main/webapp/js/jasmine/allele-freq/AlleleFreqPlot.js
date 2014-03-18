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
