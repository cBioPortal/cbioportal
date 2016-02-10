/**
 * Created by sderidder on 12/16/15.
 */
var ScatterTest = function(divName) {
    mySvg = d3.select("#" + divName).append("svg")
        .attr("width", 400)
        .attr("height", 400);


    sampleData = d3.range(25000).map(function (d) {
        var datapoint = {};
        //datapoint.id = "Sample Node " + d;
        datapoint.x = Math.random() * 500;
        datapoint.y = Math.random() * 500;

        return datapoint;
    })
/*
    quadtree = d3.geom.quadtree()
        .extent([[0, 0], [500, 500]])
        .x(function (d) {
            return d.x
        })
        .y(function (d) {
            return d.y
        });

    quadData = quadtree(sampleData);
*/
    mySvg.selectAll("circle").data(sampleData)
        .enter()
        .append("circle")
        .attr("r", 3)
        .attr("cx", function (d) {
            return d.x
        })
        .attr("cy", function (d) {
            return d.y
        })
        .style("fill", "pink")
        .style("stroke", "black")
        .style("stroke-width", "1px")
/*
    var brush = d3.svg.brush()
        .x(d3.scale.identity().domain([0, 500]))
        .y(d3.scale.identity().domain([0, 500]))
        .on("brush", brushed);

    mySvg.call(brush);

    function brushed() {
        var e = brush.extent();

        d3.selectAll("circle").filter(function (d) {
            return d.selected
        }).style("fill", "pink").each(function (d) {
            d.selected = false
        })

        quadData.visit(function (node, x1, y1, x2, y2) {
            if (node.point) {
                if (node.point.x >= e[0][0] && node.point.x <= e[1][0] && node.point.y >= e[0][1] && node.point.y <= e[1][1]) {
                    node.point.selected = true;
                }
            }
            return x1 > e[1][0] || y1 > e[1][1] || x2 < e[0][0] || y2 < e[0][1];
        })
        d3.selectAll("circle").filter(function (d) {
            return d.selected
        }).style("fill", "darkred")
    }*/
}