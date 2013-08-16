define(function() {
    return function(data, el, params) {
        console.log(data);

        data = data.sort(function(d, e) {
            return d.count - e.count;
        });

        params = params || {
            margin: {top: 20, right: 10, bottom: 20, left: 10},
            width: 400,
            height: 400,
        };

        // margin conventions http://bl.ocks.org/mbostock/3019563
        var width = params.width - params.margin.left - params.margin.left;
        var height = params.height - params.margin.top - params.margin.bottom;

        var svg = d3.select(el).append("svg")
            .attr("width", params.width)
            .attr("height", params.height)
        .append("g")
            .attr("transform", "translate(" + params.margin.left + "," + params.margin.top + ")");

        var x = d3.scale.ordinal()
            .rangeRoundBands([0, width], .1)
            .domain(data.map(function(d) { return d.cancer_study; }));

        var y = d3.scale.linear()
            .range([0, height])
            .domain([0, d3.max(data.map(function(d) { return d.count; }))]);

        // make a bar chart
        svg.selectAll(".bar")
            .data(data)
            .enter().insert("rect")
            .attr("x", function(d) { return x(d.cancer_study); })
            .attr("y", function(d) { return y(d.count); })
            .attr("width", x.rangeBand())
            .attr("height", function(d) { return height - y(d.count); })
            .attr('fill', '#1974b8')
            ;


    };
});
