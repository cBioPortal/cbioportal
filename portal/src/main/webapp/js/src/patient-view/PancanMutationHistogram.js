define(function() {
    return function(bykeyword_data, bygene_data, cancer_study2num_sequenced_samples, el, params) {

        var progname = "PancanMutationsHistogram";

        // --- data munging --- //

        // the counts in bygene_data include the counts in bykeyword_data, so
        // we are going to subtract them off
        var hugo2keywordData = bykeyword_data.reduce(function(acc, next) {
            acc[next.hugo] = next;
            return acc;
        }, {});

        bygene_data.forEach(function(d) {
            var this_mutation = hugo2keywordData[d.hugo];
            var new_count = d.count - this_mutation.count;

            if (new_count < 0) {
                throw new Error("more mutations for a particular keyword than for all keywords");
            }

            d.count = new_count;
        });

        bykeyword_data.concat(bygene_data)
            .forEach(function(d) {
                var num_sequenced_samples = cancer_study2num_sequenced_samples[d.cancer_study];
                d.frequency = d.count / num_sequenced_samples;
            });

        console.log(bygene_data);

        // compute frequences
        //data.forEach(function(d) {
        //    d.frequency = d.count / d.total;
        //});

        //data = data.sort(function(d, e) {
        //    return e.frequency - d.frequency;
        //});

        //params = $.extend({
        //    margin: {top: 20, right: 10, bottom: 20, left: 40},
        //    width: 840,
        //    height: 400,
        //    cancerStudyName: undefined
        //}, params);

        //// margin conventions http://bl.ocks.org/mbostock/3019563
        //var width = params.width - params.margin.left - params.margin.left;
        //var height = params.height - params.margin.top - params.margin.bottom;

        //var svg = d3.select(el).append("svg")
        //    .attr("width", params.width)
        //    .attr("height", params.height)
        //.append("g")
        //    .attr("transform", "translate(" + params.margin.left + "," + params.margin.top + ")");

        //var x = d3.scale.ordinal()
        //    .rangeRoundBands([0, width], .1)
        //    .domain(data.map(function(d) { return d.cancer_study; }));

        //var y = d3.scale.linear()
        //    .range([height, 0])
        //    .domain([0, d3.max(data.map(function(d) { return d.frequency; }))]);

        //// make axises

        //var xAxis = d3.svg.axis()
        //    .scale(x)
        //    .tickFormat("")
        //    .orient("bottom");

        //var yAxis = d3.svg.axis()
        //    .scale(y)
        //    .tickFormat(d3.format("%.0"))
        //    .orient("left");
        //yAxis.tickSize(yAxis.tickSize(), 0, 0);

        //// append axises

        //var xAxisEl = svg.append("g")
        //    .attr("transform", "translate(0," + height + ")")
        //    .attr('id', 'x-axis')
        //    .call(xAxis);

        //xAxisEl.attr('fill', 'none')
        //    .attr('stroke', '#000')
        //    .attr('shape-rendering', 'crispEdges')
        //    ;

        //xAxisEl.selectAll('text')
        //    .attr('transform', 'rotate(90)')
        //    .attr('x', 10)
        //    .attr('dy', -.3 * x.rangeBand())
        //    .style('text-anchor', 'start')
        //    ;

        //var yAxisEl = svg.append("g")
        //    .call(yAxis)
        //    .attr('stroke', '#000')
        //    .attr('shape-rendering', 'crispEdges');

        //yAxisEl.selectAll('path')
        //    .attr('fill', 'none');

        //// colors for each bar by cancer_type
        //var googleblue = "#3366cc";
        //var googlered = "#dc3912";

        //// find the cancer type form the cancer study name
        //var this_cancer_study = data.filter(function(d) {
        //    return d.cancer_study === params.cancerStudyName;
        //});

        //if (this_cancer_study.length !== 1) {
        //    throw new Error(progname + ": multiple cancer studies found that have indentical names to this one");
        //}

        //var this_cancer_type = this_cancer_study[0].cancer_type;

        //// make a bar chart
        //var bar = svg.selectAll(".bar")
        //    .data(data)
        //    .enter().insert("rect")
        //    .attr("x", function(d) { return x(d.cancer_study); })
        //    .attr("y", function(d) { return y(d.frequency); })
        //    .attr("width", x.rangeBand())
        //    .attr("height", function(d) { return height - y(d.frequency); })
        //    .attr('fill', function(d) {
        //        return d.cancer_type === this_cancer_type ? googlered : googleblue;
        //    })
        //    .on("mouseover", function() { d3.select(this).attr('opacity', '0.5'); })
        //    .on("mouseout", function() { d3.select(this).attr('opacity', '1'); })
        //    ;

        //// add pointer triangle that points to this cancer study
        //var triangleXcoordinate = (x(params.cancerStudyName) + x.rangeBand() * .5);

        //if (!params.cancerStudyName) {
        //    console.log("did not provide a cancerStudyName to " + progname);
        //    triangleXcoordinate = -10 * x.rangeBand(); // hide it outside the svg element
        //}

        //svg.append('path')
        //    .attr('transform', 'translate('
        //                + triangleXcoordinate
        //                + ',' + (height + 15) + ')')
        //    .attr('d', d3.svg.symbol().type('triangle-up'))
        //    .attr('fill', googlered)
        //    ;

        //// add qtips for each bar
        //bar.each(function(d) {
        //    $(this).qtip({
        //        content: {text: 'mouseover failed'},
        //        position: {my:'left bottom', at:'top right'},
        //        style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
        //        hide: { fixed: true, delay: 100 },
        //        events: {
        //            render: function(event, api) {
        //                api.set('content.text',
        //                    "<b>" + d.count + "/" + d.total + "</b>"
        //                     + "<br/>" + d.cancer_study);
        //            }
        //        }
        //    });
        //});
    };
});
