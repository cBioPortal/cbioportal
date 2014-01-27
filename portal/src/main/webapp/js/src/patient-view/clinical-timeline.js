(function() {
  
  clinicalTimeline = function() {
    var orient = "top",
        width = null,
        height = null,
        tickFormat = { format: d3.time.format("%I %p"), 
          tickTime: d3.time.hours, 
          tickNumber: 1, 
          tickSize: 6 },
        colorCycle = d3.scale.category20(),
        colorPropertyName = null,
        beginning = 0,
        ending = 0,
        margin = {left: 100, right:30, top: 30, bottom:30},
        stacked = false,
        rotateTicks = false,
        itemHeight = 20,
        itemMargin = 5
      ;

    function timeline (gParent) {
      var g = gParent.append("g");
      var gParentSize = gParent[0][0].getBoundingClientRect();

      var gParentItem = d3.select(gParent[0][0]);

      var yAxisMapping = {},
        maxStack = 1,
        minTime = Number.POSITIVE_INFINITY,
        maxTime = Number.NEGATIVE_INFINITY;
      
      setWidth();

      // check how many stacks we're gonna need
      // do this here so that we can draw the axis before the graph
      if (stacked || (ending == 0 && beginning == 0)) {
        g.each(function (d, i) {
          d.forEach(function (datum, index) {

            // create y mapping for stacked graph
            if (stacked && Object.keys(yAxisMapping).indexOf(index) == -1) {
              yAxisMapping[index] = maxStack;
              maxStack++;
            }

            // figure out beginning and ending times if they are unspecified
            if (ending == 0 && beginning == 0){
              datum.times.forEach(function (time, i) {
                if (time.starting_time < minTime)
                  minTime = time.starting_time;
                if (time.ending_time > maxTime)
                  maxTime = time.ending_time;
                if (time.starting_time > maxTime)
                  maxTime = time.starting_time;
              });
            }
          });
        });

        if (ending == 0 && beginning == 0) {
          beginning = minTime;
          ending = maxTime;
        }
      }

      var scaleFactor = (1/(ending - beginning)) * (width - margin.left - margin.right);

      // draw the axis
      var xScale = d3.time.scale()
        .domain([beginning, ending])
        .range([margin.left, width - margin.right]);

      var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient(orient)
        .tickFormat(tickFormat.format)
        .ticks(tickFormat.tickTime, tickFormat.tickNumber)
        .tickValues(tickFormat.tickValues)
        .tickSize(tickFormat.tickSize);

      g.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(" + 0 +","+(margin.top + (itemHeight + itemMargin) * maxStack)+")")
        .call(xAxis);

      // draw the chart
      g.each(function(d, i) {
        d.forEach( function(datum, index){
          var data = datum.times;
          var hasLabel = (typeof(datum.label) != "undefined");
          
          g.selectAll("svg").data(data).enter()
            .append(datum.display)
            .attr('x', getXPos)
            .attr("y", getStackPosition)
            .attr("width", function (d, i) {
              return (d.ending_time - d.starting_time) * scaleFactor;
            })
            .attr("cy", getStackPosition)
            .attr("cx", getXPos)
            .attr("r", itemHeight/2)
            .attr("height", itemHeight)
            .attr("tooltip",  function (d, i) {
              return d.tooltip;
            })
            .attr("class", "timeline-viz-elem")
            .style("fill", function(d, i){ 
              if( colorPropertyName ){ 
                return colorCycle( datum[colorPropertyName] ) 
              } 
              return colorCycle(index);  
            })
          ;

          // add the label
          if (hasLabel) {
            gParent.append('text')
              .attr("class", "timeline-label")
              .attr("transform", "translate("+ 0 +","+ (itemHeight/2 + margin.top + (itemHeight + itemMargin) * yAxisMapping[index])+")")
              .text(hasLabel ? datum.label : datum.id);
          }

          function getStackPosition(d, i) {
            if (stacked) {
              return margin.top + (itemHeight + itemMargin) * yAxisMapping[index];
            } 
            return margin.top;
          }
        });
      });
      
      if (rotateTicks) {
        g.selectAll("text")
          .attr("transform", function(d) {
            return "rotate(" + rotateTicks + ")translate("
              + (this.getBBox().width/2+10) + "," // TODO: change this 10
              + this.getBBox().height/2 + ")";
          });
      }

      var gSize = g[0][0].getBoundingClientRect();
      setHeight();

      function getXPos(d, i) {
        return margin.left + (d.starting_time - beginning) * scaleFactor;
      }

      function setHeight() {
        if (!height && !gParentItem.attr("height")) {
          if (itemHeight) {
            // set height based off of item height
            height = gSize.height + gSize.top - gParentSize.top;
            // set bounding rectangle height
            d3.select(gParent[0][0]).attr("height", height);
          } else {
            throw "height of the timeline is not set";
          }
        } else {
          if (!height) {
            height = gParentItem.attr("height");
          } else {
            gParentItem.attr("height", height);
          }
        }
      }

      function setWidth() {
        if (!width && !gParentSize.width) {
          throw "width of the timeline is not set. As of Firefox 27, timeline().with(x) needs to be explicitly set in order to render";
        } else if (!(width && gParentSize.width)) {
          if (!width) {
            width = gParentItem.attr("width");
          } else {
            gParentItem.attr("width", width);
          }
        }
        // if both are set, do nothing
      }
    }

    timeline.margin = function (p) {
      if (!arguments.length) return margin;
      margin = p;
      return timeline;
    }
    
    timeline.itemHeight = function (h) {
      if (!arguments.length) return itemHeight;
      itemHeight = h;
      return timeline;
    }

    timeline.itemMargin = function (h) {
      if (!arguments.length) return itemMargin;
      itemMargin = h;
      return timeline;
    }

    timeline.height = function (h) {
      if (!arguments.length) return height;
      height = h;
      return timeline;
    };

    timeline.width = function (w) {
      if (!arguments.length) return width;
      width = w;
      return timeline;
    };

    timeline.tickFormat = function (format) {
      if (!arguments.length) return tickFormat;
      tickFormat = format;
      return timeline;
    };

    timeline.colors = function (colorFormat) {
      if (!arguments.length) return colorCycle;
      colorCycle = colorFormat;
      return timeline;
    };

    timeline.beginning = function (b) {
      if (!arguments.length) return beginning;
      beginning = b;
      return timeline;
    };

    timeline.ending = function (e) {
      if (!arguments.length) return ending;
      ending = e;
      return timeline;
    };

    timeline.rotateTicks = function (degrees) {
      rotateTicks = degrees;
      return timeline;
    }

    timeline.stack = function () {
      stacked = !stacked;
      return timeline;
    };

    timeline.colorProperty = function(colorProp) {
      if (!arguments.length) return colorPropertyName;
      colorPropertyName = colorProp;
      return timeline;
    }
    
    return timeline;
  };
})();