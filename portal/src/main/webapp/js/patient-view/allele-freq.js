// Gideon Dresdner June 2013

// thanks Mike Bostock, http://bl.ocks.org/mbostock/4341954
;

var AlleleFreqPlotUtils = (function() {
    var process_data = function(alt_counts, ref_counts) {
        return d3.zip(alt_counts, ref_counts).map(function(pair) {
            return pair[0] / (pair[0] + pair[1]);
        });
    };

    var extract_and_process = function(genomicEventObs) {
        var alt_counts = genomicEventObs.mutations.data['alt-count'];
        var ref_counts = genomicEventObs.mutations.data['ref-count'];

        return process_data(alt_counts, ref_counts);
    };

    var kernelDensityEstimator = function(kernel, x) {
        return function(sample) {
            return x.map(function(x) {
                return [x, d3.mean(sample, function(v) { return kernel(x - v); })];
            });
        };
    };

    var epanechnikovKernel = function (scale) {
        return function(u) {
            return Math.abs(u /= scale) <= 1 ? .75 * (1 - u * u) / scale : 0;
        };
    };

    return {
        process_data: process_data,
        extract_and_process: extract_and_process,
        kernelDensityEstimator: kernelDensityEstimator,
        epanechnikovKernel: epanechnikovKernel
    };
}());

// makes an allele frequency density plot on the div
var AlleleFreqPlot = function(div, data) {
    var margin = {top: 20, right: 30, bottom: 30, left: 40},
        width = 960 - margin.left - margin.right,
        height = 500 - margin.top - margin.bottom;

    var x = d3.scale.linear()
        .domain([30, 110])
        .range([0, width]);

    var y = d3.scale.linear()
        .domain([0, .1])
        .range([height, 0]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left")
        .tickFormat(d3.format("%"));

    var line = d3.svg.line()
        .x(function(d) { console.log(d); return x(d[0]); })
        .y(function(d) { return y(d[1]); });

    var svg = d3.select(div).append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis)
        .append("text")
        .attr("class", "label")
        .attr("x", width)
        .attr("y", -6)
        .style("text-anchor", "end")
        .text("mutation frequency");

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis);

//    .axis path,
//    .axis line {
//        fill: none;
//        stroke: #000;
//        shape-rendering: crispEdges;
//    }
//
//    .y.axis path {
//        display: none;
//    }

    var utils =  AlleleFreqPlotUtils;        // alias

    var kde = utils.kernelDensityEstimator(utils.epanechnikovKernel(7), x.ticks(100));

    console.log(kde(data));

    svg.append("path")
        .datum(kde(data))
        .attr("class", "line")
        .attr("d", line)
        .attr('fill', 'none')
        .attr('stroke', '#000')
        .attr('stroke-width', '1.5px');
};
