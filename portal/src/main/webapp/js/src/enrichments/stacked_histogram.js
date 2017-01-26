/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License,
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * stacked_histogram is a horizontal stacked histogram visualisation
 * each object provided requires:
 *      barName - the label used on the y-axis
 *      barPieceName - defines part of the bar for the barName
 *      barPieceValue - the value the piece has
 *      color - the color of the barpiece
 *
 * The following would be a valid structure:
 * var bardata = [{
*      barName: TNF,
*      barPieceName: "Query Genes Unaltered, TNF Unaltered",
*      barPieceValue: 100,
*      color: "lightgrey"
*  }, {
*      barName: TNF,
*      barPieceName: "Query Genes Unaltered, TNF Altered",
*      barPieceValue: 150,
*      color: "red"
*  }, {
*      barName: "QGenes",
*      barPieceName: "QGenes Altered",
*      barPieceValue: 200,
*      color: "blue"
*  }];
 *
 *  The stacked_histogram automatically creates 0-width elements for the missing barpieces, to end
 *  with a structure which has all components for all bars
 *  Hence, in the example, we'd end up with 6 rectangles, of which 3 or 0 width.
 *
 *  As an alternative, we could instead enforce the user to specify all components explicitly
 *
 * Based on http://jsfiddle.net/datashaman/rBfy5/4/light/
 *
 * @param divId: where the stacked histogram should be placed
 */
function stacked_histogram(divId) {
    var margins = {
        top: 12,
        left: 100,
        right: 24,
        bottom: 24
    };
    var width = 350 - margins.left - margins.right;
    var height = 85 - margins.top - margins.bottom;
    var animationDuration=500;
    var dataset, yAxisComponent, groups, svg;

    /**
     * transform the barData to the format required by the d3 component
     * @param barData: data to be used for the stacked histogram
     */
    function transformData(barData){
        dataset = new Array();
        // find all the barnames and barpieces
        var barNames = $.unique(barData.map(function (d) {
            return d.barName;
        }));

        var barPieces = $.unique(barData.map(function (d) {
            return d.barPieceName;
        }));

        // transform the barData to the previously explained format
        for (var i = 0; i < barPieces.length; i++) {
            var data = new Array();
            for (var j = 0; j < barNames.length; j++) {
                // find the correct item in the barsData
                var result = $.grep(barData, function (e) {
                    return e.barName == barNames[j] && e.barPieceName == barPieces[i];
                });

                // set a default for non-existing data
                var value = 0;
                var barColor = "black";
                var opacity=1;
                if (result.length > 0) {
                    value = result[0].barPieceValue;
                    barColor = result[0].color;
                    if(!_.isUndefined(result[0].opacity)){
                        opacity = result[0].opacity;
                    }
                }

                // tipLabel
                //var tipLabel = barPieces[i] + (barNames[j]==="None selected"?"":": "+Math.round(value));
                // add the data and create a tipLabel for the tooltip
                data.push({
                    x: barNames[j],
                    y: value,
                    color: barColor,
                    opacity: opacity,
                    tipLabel: barPieces[i]
                });
            }
            data.name = barPieces[i];
            dataset.push(data);
        }

        // converts two-dimensional data into stacked data
        var stack = d3.layout.stack();
        stack(dataset);

        // turn it into a horizontal stack
        dataset = dataset.map(function (group) {
            return group.map(function (d) {
                // Invert the x and y values, and y0 becomes x0
                return {
                    x: d.y,
                    y: d.x,
                    x0: d.y0,
                    color: d.color,
                    opacity: d.opacity,
                    tipLabel: d.tipLabel
                };
            });
        });
    }

    /**
     * create histogram based on the barData
     * @param barData: data to be used for the histogram
     */
    this.createStackedHistogram = function(barData){
        // transform the data to the necessary format
        transformData(barData);

        // append the svg with a width and height
        svg = d3.select(divId)
            .append('svg')
            .attr('width', width + margins.left + margins.right)
            .attr('height', height + margins.top + margins.bottom)
            .style('padding-left', '20px')
            .append('g')
            .attr('transform', 'translate(' + margins.left + ',' + margins.top + ')');

        var xScale = getXScale();
        var yScale = getYScale();
        // create an axis with the scaling
        var yAxis = getYAxis(yScale);

        // create a group element for each dataset entry
        groups = svg.selectAll('g')
            .data(dataset)
            .enter()
            .append('g');

        // create rectangles
        groups.selectAll('rect')
            .data(function (d) {
                return d;
            })
            .enter()
            .append('rect')
            .attr('x', function (d) {
                return xScale(d.x0);
            })
            .attr('y', function (d) {
                return yScale(d.y);
            })
            .attr('height', function (d) {
                return yScale.rangeBand();
            })
            .attr('width', function (d) {
                return xScale(d.x);
            })
            .style('fill', function (d) {
                return d.color;
            })
            .style('opacity', function (d) {
                return d.opacity;
            })
            .on("mouseover", function(d) {
                showTip(d, $(this));
            });

        // add the yAxis
        yAxisComponent = svg.append("g")
            .attr("class", "axis")
            .call(yAxis);

        addYAxisToolTip(yAxisComponent);

    }

    /**
     * creates a clean id based on the divId and the barName
     * @param barName
     * @returns {string}
     */
    function getCleanedTextId(barName){
        return (divId+barName+"_text").replace(/\W/g,'_');
    }

    /**
     * Add text to a lane / barName
     * @param barName: name of the lane
     * @param text: text to show
     */
    this.addTextToLane = function(barName, text){
        var yScale = getYScale();
        var yPos = yScale(barName) + yScale.rangeBand()*0.75;

        svg.append("g")
            .append("text")
            .attr("id", getCleanedTextId(barName))
            .attr("x", 5)
            .attr("y", yPos)
            .text(text)
            .attr("font-family", "Verdana, Arial, sans-serif")
            .attr("font-size", "11px")
            .attr("fill", "black");
    }

    /**
     * Remove text from a lane / barName
     * @param barName
     */
    this.removeTextFromLane = function(barName){
        $("#"+getCleanedTextId(barName)).remove();
    }

    /**
     * Update the stacked histogram with new data
     * @param barData
     */
    this.updateStackedHistogram = function(barData){
        // transform the data to the necessary format
        transformData(barData);

        // get x-scale, y-scale and y-axis
        var xScale = getXScale();
        var yScale = getYScale();
        var yAxis = getYAxis(yScale);

        // update the data
        groups.data(dataset);

        // update the rectangle with a transition
        groups.selectAll('rect')
            .data(function (d) {
                return d;
            })
            .transition()
            .duration(animationDuration)
            .attr('x', function (d) {
                return xScale(d.x0);
            })
            .attr('width', function (d) {
                return xScale(d.x);
            })
            .style('opacity', function (d) {
                return d.opacity;
            });

        // update the y-axis
        yAxisComponent.call(yAxis);
    }

    /**
     * show a qtip for the rectangle
     * @param element: contains the label to be used
     * @param rectangle: element to which the qtip should be added (or updated)
     */
    function showTip(element, rectangle){
        // if no qtip exists add one
        if(rectangle.attr("data-hasqtip")==undefined) {
            rectangle.qtip({
                    content: {text: element.tipLabel},
                    style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow qtip-wide'},
                    hide: {fixed: true, delay: 100, event: "mouseout"},
                    position: {my: 'left bottom', at: 'top right'}
                }
            )
        }
        // otherwise just update the content text
        else{
            rectangle.qtip('option', 'content.text', element.tipLabel);
        }
        // show the qtip
        rectangle.qtip("show");
    }

    /**
     * get a y-scale
     * @returns y-scale
     */
    function getYScale(){
        // determine the yLabels
        var yLabels = dataset[0].map(function (d) {
            return d.y;
        });

        // determine a scaling for the values
        var yScale = d3.scale.ordinal()
            .domain(yLabels)
            .rangeRoundBands([0, height], .1);
        return yScale;
    }

    /**
     * get a y-axis based on the scale
     * @param yScale
     * @returns y-axis
     */
    function getYAxis(yScale){
        var yAxis = d3.svg.axis()
            .scale(yScale)
            .orient('left');
        return yAxis;
    }

    /**
     * adds small information symbols to the y-axis.
     * @param yaxis
     */
    function addYAxisToolTip(yaxis){
        var yScale = getYScale();
        var imgHeight = 10;
        var ticks = yaxis.selectAll(".tick")[0];

        for(var i=0; i<ticks.length; i++){
            var curTick = ticks[i];
            var data = d3.select(curTick).data();
            // determine the y position for the image. Determine the y offset via the yScale, determine the height
            // of the bars, divide by 2 and compensate for the height of the image which you would like centered
            var yPos = yScale(data) + yScale.rangeBand()*0.5 - imgHeight/2;

            // add an image
            var curImage = yaxis.append("svg:image")
                .attr("xlink:href", "images/info.png")
                .attr("class", "plots-title-x-help")
                .attr("x", -100)
                .attr("y", yPos)
                .attr("width", "10")
                .attr("height", imgHeight);

            // determine the axisText
            var axisText = "Query Genes selected by user";
            if(i>0) axisText = "Gene selected in table";

            // create an element with a tipLabel, which is what the showTip function expects
            var element = {
                tipLabel: axisText
            };

            // add the tooltip
            showTip(element, $(curImage));
        }
    }

    /**
     * get an x-scale
     * @returns x-scale
     */
    function getXScale(){
        // find the maximum x; for each group find the maximum and then return the maximum of these maxima
        var xMax = d3.max(dataset, function (group) {
            return d3.max(group, function (d) {
                return d.x + d.x0;
            });
        });

        // create an x-scale based on the xMax and the width available for the histogram
        var xScale = d3.scale.linear()
            .domain([0, xMax])
            .range([0, width]);
        return xScale;
    }
}
