//
//var bars=[{
//    barName: "QGenes",
//    barPieceName: "QGenes Altered",
//    barPieceValue: 100
//}, {
//    barName: "QGenes",
//    barPieceName: "QGenes Unaltered",
//    barPieceValue: 75
//}, {
//    barName: "TNF",
//    barPieceName: "QGenes Altered, TNF Unaltered",
//    barPieceValue: 125
//}, {
//    barName: "TNF",
//    barPieceName: "QGenes Altered, TNF Altered",
//    barPieceValue: 25
//}, {
//    barName: "TNF",
//    barPieceName: "QGenes Unaltered, TNF Unaltered",
//    barPieceValue: 25
//}, {
//    barName: "TNF",
//    barPieceName: "QGenes Unaltered, TNF Altered",
//    barPieceValue: 25
//}];

function stacked_histogram() {
    var dataset = new Array();

    this.init = function(){
        var barNames = $.unique(barData.map(function (d) {
            return d.barName;
        }));

        var barPieces = $.unique(barData.map(function (d) {
            return d.barPieceName;
        }));

        for (var i = 0; i < barPieces.length; i++) {
            var data = new Array();
            for (var j = 0; j < barNames.length; j++) {
                // find the correct item in the barsData
                var result = $.grep(barData, function (e) {
                    return e.barName == barNames[j] && e.barPieceName == barPieces[i];
                });

                var value = 0;
                if (result.length > 0) value = result[0].barPieceValue;
                data.push({
                    yLabel: barNames[j],
                    value: value
                    //color: "red"
                });
            }
            data.name = barPieces[i];
            dataset.push(data);
        }
    }


    this.addStackedHistogram = function(divId){
        var margins = {
                top: 12,
                left: 48,
                right: 24,
                bottom: 24
            },
            legendPanel = {
                width: 180
            },
            width = 500 - margins.left - margins.right - legendPanel.width,
            height = 100 - margins.top - margins.bottom;
        var series = dataset.map(function (d) {
            return d.name;
        });

        dataset = dataset.map(function (d) {
            return d.map(function (o, i) {
                // Structure it so that your numeric
                // axis (the stacked amount) is y
                return {
                    y: o.value,
                    x: o.yLabel,
                    //color: o.color
                };
            });
        });

        stack = d3.layout.stack();
        stack(dataset);

        dataset = dataset.map(function (group) {
            return group.map(function (d) {
                // Invert the x and y values, and y0 becomes x0
                return {
                    x: d.y,
                    y: d.x,
                    x0: d.y0,
                    //color: d.color
                };
            });
        });

        svg = d3.select(divId)
            .append('svg')
            .attr('width', width + margins.left + margins.right + legendPanel.width)
            .attr('height', height + margins.top + margins.bottom)
            .append('g')
            .attr('transform', 'translate(' + margins.left + ',' + margins.top + ')'),
            xMax = d3.max(dataset, function (group) {
                return d3.max(group, function (d) {
                    return d.x + d.x0;
                });
            }),
            xScale = d3.scale.linear()
                .domain([0, xMax])
                .range([0, width]),
            yLabels = dataset[0].map(function (d) {
                return d.y;
            }),
            //_ = console.log(yLabels),
            yScale = d3.scale.ordinal()
                .domain(yLabels)
                .rangeRoundBands([0, height], .1),
            xAxis = d3.svg.axis()
                .scale(xScale)
                .orient('bottom'),
            yAxis = d3.svg.axis()
                .scale(yScale)
                .orient('left'),
            colours = d3.scale.category10(),
            groups = svg.selectAll('g')
                .data(dataset)
                .enter()
                .append('g')
                .style('fill', function (d, i) {
                    return colours(i);
                    //return d[0].color;
                }),
            rects = groups.selectAll('rect')
                .data(function (d) {
                    return d;
                })
                .enter()
                .append('rect')
                .attr('x', function (d) {
                    return xScale(d.x0);
                })
                .attr('y', function (d, i) {
                    return yScale(d.y);
                })
                .attr('height', function (d) {
                    return yScale.rangeBand();
                })
                .attr('width', function (d) {
                    return xScale(d.x);
                })
                .on('mouseover', function (d) {
                    var xPos = parseFloat(d3.select(this).attr('x')) / 2 + width / 2;
                    var yPos = parseFloat(d3.select(this).attr('y')) + yScale.rangeBand() / 2;

                    d3.select('#tooltip')
                        .style('left', xPos + 'px')
                        .style('top', yPos + 'px')
                        .select('#value')
                        .text(d.x);

                    d3.select('#tooltip').classed('hidden', false);
                })
                .on('mouseout', function () {
                    d3.select('#tooltip').classed('hidden', true);
                });

        svg.append('g')
            .attr('class', 'axis')
            .call(yAxis);


//svg.append('g')
//    .attr('class', 'axis')
//    .attr('transform', 'translate(0,' + height + ')')
//    .call(xAxis);
//
//svg.append('rect')
//    .attr('fill', 'yellow')
//    .attr('width', 160)
//    .attr('height', 30 * dataset.length)
//    .attr('x', width + margins.left)
//    .attr('y', 0);
//
//series.forEach(function (s, i) {
//    svg.append('text')
//        .attr('fill', 'black')
//        .attr('x', width + margins.left + 8)
//        .attr('y', i * 24 + 24)
//        .text(s);
//    svg.append('rect')
//        .attr('fill', colours(i))
//        .attr('width', 60)
//        .attr('height', 20)
//        .attr('x', width + margins.left + 90)
//        .attr('y', i * 24 + 6);
//});
    }

    this.init();
}