define(function() {
    return function(data, el, params) {

        // compute frequences
        data.forEach(function(d) {
            d.frequency = d.count / d.total;
        });

        data = data.sort(function(d, e) {
            return e.frequency - d.frequency;
        });

        params = params || {
            margin: {top: 20, right: 10, bottom: 20, left: 40},
            width: 840,
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
            .range([height, 0])
            .domain([0, d3.max(data.map(function(d) { return d.frequency; }))]);

        // make axises

        var xAxis = d3.svg.axis()
            .scale(x)
            .tickFormat("")
            .orient("bottom");

        var yAxis = d3.svg.axis()
            .scale(y)
            .tickFormat(d3.format("%.0"))
            .orient("left");
        yAxis.tickSize(yAxis.tickSize(), 0, 0);

        // append axises

        var xAxisEl = svg.append("g")
            .attr("transform", "translate(0," + height + ")")
            .attr('id', 'x-axis')
            .call(xAxis);

        xAxisEl.attr('fill', 'none')
            .attr('stroke', '#000')
            .attr('shape-rendering', 'crispEdges')
            ;

        xAxisEl.selectAll('text')
            .attr('transform', 'rotate(90)')
            .attr('x', 10)
            .attr('dy', -.3 * x.rangeBand())
            .style('text-anchor', 'start')
            ;

        var yAxisEl = svg.append("g")
            .call(yAxis)
            .attr('stroke', '#000')
            .attr('shape-rendering', 'crispEdges');

        yAxisEl.selectAll('path')
            .attr('fill', 'none');

        // colors for each bar by cancer_type
        var google_charts_colors = ["#3366cc","#dc3912","#ff9900","#109618",
        "#990099","#0099c6","#dd4477","#66aa00",
        "#b82e2e","#316395","#994499","#22aa99",
        "#aaaa11","#6633cc","#e67300","#8b0707",
        "#651067","#329262","#5574a6","#3b3eac",
        "#b77322","#16d620","#b91383","#f4359e",
        "#9c5935","#a9c413","#2a778d","#668d1c",
        "#bea413","#0c5922","#743411"];

        var color_scale = d3.scale.ordinal()
            .domain(data.map(function(d) { return d.cancer_type; }))
            .range(google_charts_colors);

        // make a bar chart
        var bar = svg.selectAll(".bar")
            .data(data)
            .enter().insert("rect")
            .attr("x", function(d) { return x(d.cancer_study); })
            .attr("y", function(d) { return y(d.frequency); })
            .attr("width", x.rangeBand())
            .attr("height", function(d) { return height - y(d.frequency); })
            .attr('fill', function(d) {
                return color_scale(d.cancer_type);
            })
            .on("mouseover", function() { d3.select(this).attr('opacity', '0.5'); })
            .on("mouseout", function() { d3.select(this).attr('opacity', '1'); })
            ;

        // add pointer triangle that points to this cancer study
        var cancerStudy = "Cancer Cell Line Encyclopedia (Novartis/Broad, Nature 2012)";
        svg.append('path')
            .attr('transform', 'translate('
                        + (x(cancerStudy) + x.rangeBand() * .5)
                        + ',' + (height + 15 )+ ')')
            .attr('d', d3.svg.symbol().type('triangle-up'));

        // add qtips for each bar
        bar.each(function(d) {
            $(this).qtip({
                content: {text: 'mouseover failed'},
                position: {my:'left bottom', at:'top right'},
                style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                hide: { fixed: true, delay: 100 },
                events: {
                    render: function(event, api) {
                        api.set('content.text',
                            "<b>" + d.count + "/" + d.total + "</b>"
                             + "<br/>" + d.cancer_study);
                    }
                }
            });
        });
    };
});
