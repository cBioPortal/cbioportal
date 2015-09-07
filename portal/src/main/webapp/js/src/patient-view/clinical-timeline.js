/* v0.0.3-1-g626aac9 */
// vim: ts=2 sw=2
(function () {
  d3.timeline = function() {
    var DISPLAY_TYPES = ["circle", "rect"];

    var hover = function () {},
        mouseover = function () {},
        mouseout = function () {},
        click = function () {},
        scroll = function () {},
        orient = "bottom",
        width = null,
        height = null,
        rowSeperatorsColor = null,
        backgroundColor = null,
        tickFormat = { format: function(d) { var format = d3.time.format("%I %p"); return format(d) },
          tickTime: d3.time.hours,
          tickInterval: 1,
          tickSize: 6,
          tickValues: null
        },
        colorCycle = d3.scale.category20(),
        colorPropertyName = null,
        display = "rect",
        beginning = 0,
        ending = 0,
        margin = {left: 30, right:30, top: 30, bottom:30},
        stacked = false,
        stackSlack = 0,
        rotateTicks = false,
        timeIsRelative = false,
        itemHeight = 20,
        itemMargin = 5,
        showTimeAxis = true,
        showTodayLine = false,
        timeAxisTick = false,
        timeAxisTickFormat = {stroke: "stroke-dasharray", spacing: "4 10"},
        showTodayFormat = {marginTop: 25, marginBottom: 0, width: 1, color: colorCycle},
        showBorderLine = false,
        showBorderFormat = {marginTop: 25, marginBottom: 0, width: 1, color: colorCycle}
      ;

    function timeline (gParent) {
      var g = gParent.append("g");
      var gParentSize = gParent[0][0].getBoundingClientRect();

      var gParentItem = d3.select(gParent[0][0]);

      var yAxisMapping = {},
        maxStack = 1,
        minTime = 0,
        maxTime = 0;

      setWidth();

      // check if the user wants relative time
      // if so, substract the first timestamp from each subsequent timestamps
      if(timeIsRelative){
        g.each(function (d, i) {
          d.forEach(function (datum, index) {
            datum.times.forEach(function (time, j) {
              if(index === 0 && j === 0){
                originTime = time.starting_time;               //Store the timestamp that will serve as origin
                time.starting_time = 0;                        //Set the origin
                time.ending_time = time.ending_time - originTime;     //Store the relative time (millis)
              }else{
                time.starting_time = time.starting_time - originTime;
                time.ending_time = time.ending_time - originTime;
              }
            });
          });
        });
      }

      // check how many stacks we're gonna need
      // do this here so that we can draw the axis before the graph
      if (stacked || ending === 0 || beginning === 0) {
        g.each(function (d, i) {
          d.forEach(function (datum, index) {
            // Set stack attribute for time points based on overlapping time
            var overlapMaxStack = 0;

            if (!datum.collapse) {
              var overlapGroups = groupByOverlap(datum.times, stackSlack);

              overlapGroups.forEach(function(overlapGroup, j) {
                var overlapStack = 0;
                overlapGroup.forEach(function(time, k) {
                  time.stack = maxStack + overlapStack;
                  if (overlapStack > overlapMaxStack) {
                    overlapMaxStack = overlapStack;
                  }
                  overlapStack++;
                });
              });
            } else {
              datum.times.forEach(function(t) {
                t.stack = maxStack;
              });
            }

            // create y mapping for stacked graph
            if (stacked && Object.keys(yAxisMapping).indexOf(index) == -1) {
              yAxisMapping[index] = maxStack;
              maxStack++;
            }
            maxStack = maxStack + overlapMaxStack;


            // figure out beginning and ending times if they are unspecified
            datum.times.forEach(function (time, i) {
              if(beginning === 0)
                if (time.starting_time < minTime || (minTime === 0 && timeIsRelative === false))
                  minTime = time.starting_time;
              if(ending === 0)
                if (time.ending_time > maxTime)
                  maxTime = time.ending_time;
            });
          });
        });

        if (ending === 0) {
          ending = maxTime;
        }
        if (beginning === 0) {
          beginning = minTime;
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
        .tickSize(tickFormat.tickSize);

      if (tickFormat.tickValues !== null) {
        xAxis.tickValues(tickFormat.tickValues);
      } else {
        xAxis.ticks(tickFormat.numTicks || tickFormat.tickTime, tickFormat.tickInterval);
      }
        //.tickFormat(
        //    {format: function(d) { return d - 10; },
        //    tickTime: d3.time.hours,
        //    tickInterval: 1,
        //    tickSize: 30});

      if (showTimeAxis) {
        g.append("g")
          .attr("class", "axis")
          .attr("transform", "translate(" + 0 +","+(margin.top + (itemHeight + itemMargin) * maxStack)+")")
          .call(xAxis);
      }

      if (timeAxisTick) {
        g.append("g")
          .attr("class", "axis")
          .attr("transform", "translate(" + 0 +","+
            (margin.top + (itemHeight + itemMargin) * maxStack)+")")
          .attr(timeAxisTickFormat.stroke, timeAxisTickFormat.spacing)
          .call(xAxis.tickFormat("").tickSize(-(margin.top + (itemHeight + itemMargin) * (maxStack - 1) + 3),0,0));
      }
      
      // draw the chart
      g.each(function(d, i) {
        d.forEach( function(datum, index){
          var data = datum.times;
          var hasLabel = (typeof(datum.label) != "undefined");

          // issue warning about using id per data set. Ids should be individual to data elements
          if (typeof(datum.id) != "undefined") {
            console.warn("d3Timeline Warning: Ids per dataset is deprecated in favor of a 'class' key. Ids are now per data element.");
          }

          if (backgroundColor) {
            var greenbarYAxis = ((itemHeight + itemMargin) * yAxisMapping[index]);
            g.selectAll("svg").data(data).enter()
              .insert("rect")
              .attr("class", "row-green-bar")
              .attr("x", 0 + margin.left)
              .attr("width", width - margin.right - margin.left)
              .attr("y", greenbarYAxis)
              .attr("height", itemHeight)
              .attr("fill", backgroundColor)
            ;
          }

          var timePoints = g.selectAll("svg").data(data).enter()
            .append(getShape)
            .attr("x", getXPos)
            .attr("y", getStackPosition)
            .attr("width", getWidth)
            .attr("cy", function(d, i) {
              return getStackPosition(d, i) + itemHeight/2;
            })
            .attr("cx", getXPos)
            .attr("r", getHeight)
            .attr("height", getHeight)
            .style("fill", function(d, i){
              var dColorPropName;
              if ("display" in d && d.display.split(" ").indexOf("unfilled") !== -1) {
                return "rgb(255, 255, 255)";
              }
              if (d.color) return d.color;
              if( colorPropertyName ){
                dColorPropName = d[colorPropertyName];
                if ( dColorPropName ) {
                  return colorCycle( dColorPropName );
                } else {
                  return colorCycle( datum[colorPropertyName] );
                }
              } else if (hasLabel) {
                  return colorCycle(datum.label);
              }
              return colorCycle(index);
            })
            .style("stroke", function(d, i) {
              if ("display" in d && d.display.split(" ").indexOf("unfilled") !== -1) {
                if (d.color) return d.color;
                if (hasLabel) return colorCycle(datum.label);
                return colorCycle(index);
              }
            })
            .attr("filter", function(d, i) {
              if ("display" in d && d.display.split(" ").indexOf("dropshadow") !== -1) {
                return "url(#dropshadow)";
              } else {
                return "";
              }
            })
            .on("mousemove", function (d, i) {
              hover(d, index, datum);
            })
            .on("mouseover", function (d, i) {
              mouseover(d, i, datum);
            })
            .on("mouseout", function (d, i) {
              mouseout(d, i, datum);
            })
            .on("click", function (d, i) {
              click(d, index, datum);
            })
            .attr("class", function (d, i) {
              return datum.class ? "timelineSeries_"+datum.class : "timelineSeries_"+index;
            })
            .attr("id", function(d, i) {
              // use deprecated id field
              if (datum.id && !d.id) {
                return 'timelineItem_'+datum.id;
              }
              
              return d.id ? d.id : "timelineItem_"+index+"_"+i;
            })
          ;

          g.selectAll("svg").data(data).enter()
            .append("text")
            .attr("x", getXTextPos)
            .attr("y", getStackTextPosition)
            .text(function(d) {
              return d.label;
            })
          ;

          if (rowSeperatorsColor) {
            var lineYAxis = ( itemHeight + itemMargin / 2 + margin.top + (itemHeight + itemMargin) * yAxisMapping[index]);
            gParent.append("svg:line")
              .attr("class", "row-seperator")
              .attr("x1", 0 + margin.left)
              .attr("x2", width - margin.right)
              .attr("y1", lineYAxis)
              .attr("y2", lineYAxis)
              .attr("stroke-width", 1)
              .attr("stroke", rowSeperatorsColor);
            ;
          }

          // add the label
          if (hasLabel) {
            gParent.append("text")
              .attr("class", "timeline-label")
              .attr("transform", "translate("+ 0 +","+ (itemHeight * 0.75 + margin.top + (itemHeight + itemMargin) * yAxisMapping[index])+")")
              .text(hasLabel ? datum.label : datum.id)
              .on("click", function (d, i) {
                click(d, index, datum);
              });
          }

          if (typeof(datum.icon) !== "undefined") {
            gParent.append("image")
              .attr("class", "timeline-label")
              .attr("transform", "translate("+ 0 +","+ (margin.top + (itemHeight + itemMargin) * yAxisMapping[index])+")")
              .attr("xlink:href", datum.icon)
              .attr("width", margin.left)
              .attr("height", itemHeight);
          }

          function getStackPosition(d, i) {
            if (stacked) {
              return margin.top + (itemHeight + itemMargin) * d.stack; // yAxisMapping[index];
            }
            return margin.top;
          }
          function getStackTextPosition(d, i) {
            if (stacked) {
              return margin.top + (itemHeight + itemMargin) * yAxisMapping[index] + itemHeight * 0.75;
            }
            return margin.top + itemHeight * 0.75;
          }
        });
      });

      if (width > gParentSize.width) {
        var move = function() {
          var x = Math.min(0, Math.max(gParentSize.width - width, d3.event.translate[0]));
          zoom.translate([x, 0]);
          g.attr("transform", "translate(" + x + ",0)");
          scroll(x*scaleFactor, xScale);
        };

        var zoom = d3.behavior.zoom().x(xScale).on("zoom", move);

        gParent
          .attr("class", "scrollable")
          .call(zoom);
      }

      if (rotateTicks) {
        g.selectAll(".tick text")
          .attr("transform", function(d) {
            return "rotate(" + rotateTicks + ")translate("
              + (this.getBBox().width / 2 + 10) + "," // TODO: change this 10
              + this.getBBox().height / 2 + ")";
          });
      }

      var gSize = g[0][0].getBoundingClientRect();
      setHeight();

      if (showBorderLine) {
        g.each(function (d, i) {
          d.forEach(function (datum) {
            var times = datum.times;
            times.forEach(function (time) {
              appendLine(xScale(time.starting_time), showBorderFormat);
              appendLine(xScale(time.ending_time), showBorderFormat);
            });
          });
        });
      }

      if (showTodayLine) {
        var todayLine = xScale(new Date());
        appendLine(todayLine, showTodayFormat);
      }

      function groupByOverlap(times, slack) {
          var end = -Infinity,
              groups = [],
              group = [];

          var sortedTimes = times.sort(function(a, b) {
              return parseInt(a.starting_time) - parseInt(b.starting_time);
          });

          for (var i = 0; i < sortedTimes.length; i++) {
            if ((parseInt(sortedTimes[i].starting_time) === parseInt(sortedTimes[i].ending_time) &&
                 (parseInt(sortedTimes[i].starting_time) <= parseInt(end) + slack)) ||
                (parseInt(sortedTimes[i].starting_time) !== parseInt(sortedTimes[i].ending_time) &&
                 (parseInt(sortedTimes[i].starting_time) < parseInt(end)))
                ) {
                group.push(sortedTimes[i]);
                if (parseInt(sortedTimes[i].ending_time) > end) {
                  end = parseInt(sortedTimes[i].ending_time);
                }
            } else {
              if (group.length > 0) {
                  groups.push(group);
              }
              group = [sortedTimes[i]];
              end = parseInt(sortedTimes[i].ending_time);
            }
          }
          groups.push(group);
          return groups;
      }

      function getShape(d, i) {
        var shape;
        if ("display" in d) {
          if (d.display === "rect" || d.display === "circle") {
            shape = d.display;
          } else if (d.display === "square") {
            shape = "rect";
          } else if (d.display.split(" ").indexOf("circle") !== -1) {
            shape = "circle";
          } else {
            console.warn("d3Timeline Warning: unrecognized display attribute: " + d.display);
          }
        } else {
          shape = display;
        }
        return document.createElementNS(d3.ns.prefix.svg, shape);
      }

      function getHeight(d, i) {
        return parseInt(d.size) || itemHeight;
      }

      function getWidth(d, i) {
        if ("display" in d && d.display === "square") {
          return itemHeight;
        } else {
          return ((d.ending_time - d.starting_time) * scaleFactor);
        }
      }

      function getXPos(d, i) {
        if ("display" in d && d.display === "square") {
          return margin.left + (d.starting_time - beginning) * scaleFactor - itemHeight/2;
        } else {
          return margin.left + (d.starting_time - beginning) * scaleFactor;
        }
      }

      function getXTextPos(d, i) {
        return margin.left + (d.starting_time - beginning) * scaleFactor + 5;
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
          try { 
            width = gParentItem.attr("width");
            if (!width) {
              throw "width of the timeline is not set. As of Firefox 27, timeline().with(x) needs to be explicitly set in order to render";
            }            
          } catch (err) {
            console.log( err );
          }
        } else if (!(width && gParentSize.width)) {
          try { 
            width = gParentItem.attr("width");
          } catch (err) {
            console.log( err );
          }
        }
        // if both are set, do nothing
      }

      function appendLine(lineScale, lineFormat) {
        gParent.append("svg:line")
          .attr("x1", lineScale)
          .attr("y1", lineFormat.marginTop)
          .attr("x2", lineScale)
          .attr("y2", height - lineFormat.marginBottom)
          .style("stroke", lineFormat.color)//"rgb(6,120,155)")
          .style("stroke-width", lineFormat.width);
      }

    }

    // SETTINGS

    timeline.margin = function (p) {
      if (!arguments.length) return margin;
      margin = p;
      return timeline;
    };

    timeline.orient = function (orientation) {
      if (!arguments.length) return orient;
      orient = orientation;
      return timeline;
    };

    timeline.itemHeight = function (h) {
      if (!arguments.length) return itemHeight;
      itemHeight = h;
      return timeline;
    };

    timeline.itemMargin = function (h) {
      if (!arguments.length) return itemMargin;
      itemMargin = h;
      return timeline;
    };

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

    timeline.display = function (displayType) {
      if (!arguments.length || (DISPLAY_TYPES.indexOf(displayType) == -1)) return display;
      display = displayType;
      return timeline;
    };

    timeline.tickFormat = function (format) {
      if (!arguments.length) return tickFormat;
      tickFormat = format;
      return timeline;
    };

    timeline.hover = function (hoverFunc) {
      if (!arguments.length) return hover;
      hover = hoverFunc;
      return timeline;
    };

    timeline.mouseover = function (mouseoverFunc) {
      if (!arguments.length) return mouseoverFunc;
      mouseover = mouseoverFunc;
      return timeline;
    };

    timeline.mouseout = function (mouseoverFunc) {
      if (!arguments.length) return mouseoverFunc;
      mouseout = mouseoverFunc;
      return timeline;
    };

    timeline.click = function (clickFunc) {
      if (!arguments.length) return click;
      click = clickFunc;
      return timeline;
    };

    timeline.scroll = function (scrollFunc) {
      if (!arguments.length) return scroll;
      scroll = scrollFunc;
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
    };

    timeline.stack = function () {
      stacked = !stacked;
      return timeline;
    };

    timeline.stackSlack = function () {
      if (!arguments.length) return stackSlack;
      return timeline;
    };

    timeline.relativeTime = function() {
      timeIsRelative = !timeIsRelative;
      return timeline;
    };

    timeline.showBorderLine = function () {
        showBorderLine = !showBorderLine;
        return timeline;
    };

    timeline.showBorderFormat = function(borderFormat) {
      if (!arguments.length) return showBorderFormat;
      showBorderFormat = borderFormat;
      return timeline;
    };

    timeline.showToday = function () {
      showTodayLine = !showTodayLine;
      return timeline;
    };

    timeline.showTodayFormat = function(todayFormat) {
      if (!arguments.length) return showTodayFormat;
      showTodayFormat = todayFormat;
      return timeline;
    };

    timeline.colorProperty = function(colorProp) {
      if (!arguments.length) return colorPropertyName;
      colorPropertyName = colorProp;
      return timeline;
    };

    timeline.rowSeperators = function (color) {
      if (!arguments.length) return rowSeperatorsColor;
      rowSeperatorsColor = color;
      return timeline;
    };

    timeline.background = function (color) {
      if (!arguments.length) return backgroundColor;
      backgroundColor = color;
      return timeline;
    };

    timeline.showTimeAxis = function () {
      showTimeAxis = !showTimeAxis;
      return timeline;
    };

    timeline.showTimeAxisTick = function () {
      timeAxisTick = !timeAxisTick;
      return timeline;
    };

    timeline.showTimeAxisTickFormat = function(format) {
      if (!arguments.length) return timeAxisTickFormat;
      timeAxisTickFormat = format;
      return timeline;
    }

    return timeline;
  };
})();
// vim: ts=2 sw=2
window.clinicalTimeline = (function(){
  var allData,
      colorCycle = d3.scale.category20(),
      itemHeight = 6,
      itemMargin = 8,
      divId = null,
      width = null,
      postTimelineHooks = [],
      enableTrackTooltips = true,
      stackSlack = null;

  function getTrack(data, track) {
    return data.filter(function(x) {
      return $.trim(x.label) === $.trim(track);
    })[0];
  }

  function timeline() {
    visibleData = allData.filter(function(x) {
        return x.visible;
    });

    var maxDays = Math.max.apply(Math, [getMaxEndingTime(allData), 1]);
    if (stackSlack === null) {
      if (maxDays > 300) {
        stackSlack = 5;
      }
      if (maxDays > 600) {
        stackSlack = 10;
      }
      if (maxDays > 900) {
        stackSlack = 20;
      }
    }

    var chart = d3.timeline()
      .stack()
      .margin({left:200, right:30, top:15, bottom:0})
      .tickFormat({
        format: function(d) { return formatTime(daysToTimeObject(d.valueOf())); },
        tickValues: getTickValues(allData),
        tickSize: 6
      })
      .beginning("0")
      .ending(maxDays)
      .stackSlack(stackSlack)
      .orient('top')
      .itemHeight(itemHeight)
      .itemMargin(itemMargin)
      .colors(colorCycle);


    $(divId).html("");
    var svg = d3.select(divId).append("svg").attr("width", width);

    // Add dropshadow filter
    svg.append('defs').html('' +
      '<filter id="dropshadow" x="0" y="0" width="200%" height="200%">' +
      '  <feOffset result="offOut" in="SourceAlpha" dx="1.5" dy="1.5" />' +
      '  <feGaussianBlur result="blurOut" in="offOut" stdDeviation="1" />' +
      '  <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />' +
      '</filter>');

    svg.datum(mergeAllTooltipTablesAtEqualTimepoint(visibleData)).call(chart);

    $("[id^='timelineItem']").each(function() {
      timeline.addDataPointTooltip($(this));
    });
    $("[id^='timelineItem']").each(function() {
      $(this).on("mouseover", function() {
          $(this).attr("r", parseInt($(this).attr("r")) + 2);
          $(this).attr("height", parseInt($(this).attr("height")) + 2);
      });
      $(this).on("mouseout", function() {
          $(this).attr("r",  parseInt($(this).attr("r")) - 2);
          $(this).attr("height", parseInt($(this).attr("height")) - 2);
      });
    });
    if (enableTrackTooltips) {
      $(".timeline-label").each(function(i) {
        if ($(this).prop("__data__")[i].split && !$(this).prop("__data__")[i].parent_track) {
          addSplittedTrackTooltip($(this), allData);
        } else {
          addTrackTooltip($(this), allData);
        }
      });
      // Add track button
      svg.attr("height", parseInt(svg.attr("height")) + 15);
      svg.insert("text")
        .attr("transform", "translate(0,"+svg.attr("height")+")")
        .attr("class", "timeline-label")
        .text("Add track")
        .attr("id", "addtrack");
      addNewTrackTooltip($("#addtrack"));
    }
    svg.insert("text")
      .attr("transform", "translate(0, 15)")
      .attr("class", "timeline-label")
      .text("Time since diagnosis");
    d3.select(".axis").attr("transform", "translate(0,20)");

    // preserve whitespace for easy indentation of labels
    $(".timeline-label").each(function(i, x) {
      x.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space", "preserve");
    });

    postTimelineHooks.forEach(function(hook) {
      hook.call();
    });
  }

  /**
   * Checks wheter a timeline track contains timepoints with varying start and
   * end dates.
   */
  function isDurationTrack(trackData) {
    isDuration = false;
    trackData.times.forEach(function(x) {
      if (parseInt(x.starting_time) !== parseInt(x.ending_time)) {
        isDuration = true;
      }
    });
    return isDuration;
  }

  function splitTooltipTables(trackData) {
    var expandedTimes = [];

    for (var i = 0; i < trackData.times.length; i++) {
      var t = trackData.times[i];
      if (t.tooltip_tables.length > 1) {
        for (var j = 0; j < t.tooltip_tables.length; j++) {
          expandedTimes = expandedTimes.concat({
            "starting_time":t.starting_time,
            "ending_time":t.ending_time,
            "display":"circle",
            "tooltip_tables": [t.tooltip_tables[j]]
          });
        }
      } else {
        expandedTimes = expandedTimes.concat(t);
      }
    }
    trackData.times = expandedTimes;
  }

  /*
   * Merge timepoints that have the same starting_time into one timepoint with
   * multiple tooltip_tables
   */
  function mergeTooltipTablesAtEqualTimepoint(trackData) {
    if (!trackData || trackData.times.length === 0) return;

    var collapsedTimes = [],
        group = [],
        startingTime = undefined;

    var sortedTimes = trackData.times.sort(function(a, b) {
      return parseInt(a.starting_time) - parseInt(b.starting_time);
    });

    var mergeTimepoints = function(startingTime, group) {
      if (group.length === 1) {
        return group[0];
      } else {
        return {
          "starting_time":startingTime,
          "ending_time":startingTime,
          "display":"dropshadow circle",
          "tooltip_tables": _.reduce(group.map(function(x) {
            return x.tooltip_tables;
          }), function(a, b) {
            return a.concat(b);
          }, [])
        };
      }
    };

    for (var i = 0; i < sortedTimes.length; i++) {
      var t = sortedTimes[i];
      if (parseInt(t.starting_time) === startingTime) {
        group = group.concat(t);
      } else {
        if (group.length > 0) {
          collapsedTimes = collapsedTimes.concat(
              mergeTimepoints(startingTime, group));
        }
        group = [t];
        startingTime = parseInt(t.starting_time);
      }
    }
    collapsedTimes = collapsedTimes.concat(
        mergeTimepoints(startingTime, group));
    trackData.times = collapsedTimes;
  }

  function mergeAllTooltipTablesAtEqualTimepoint(data) {
    var collapsedTracks = data.filter(function(trackData) {
      return trackData.collapse && !isDurationTrack(trackData);
    });
    collapsedTracks.forEach(mergeTooltipTablesAtEqualTimepoint);
    return data;
  }

  function getClinicalAttributes(data, track) {
    return _.union.apply(_, getTrack(data, track).times.map(function(x) {
          // return union of attributes in tooltip (in case there are multiple)
          return _.union.apply(_, x.tooltip_tables.map(function(y) {
            return y.map(function(z) {
              return z[0];
            });
          }));
      })
    );
  }

  function groupByClinicalAttribute(track, attr) {
    return _.groupBy(getTrack(allData, track).times, function(x) {
      // return attribute value if there is one tooltip table
      if (x.tooltip_tables.length === 1) {
        return _.reduce(x.tooltip_tables[0], function(a,b) {
          a[b[0]] = b[1];
          return a;
        }, {})[attr];
      } else {
        return undefined;
      }
    });
  }

  function splitByClinicalAttribute(track, attr) {
    // split tooltip_tables into separate time points
    splitTooltipTables(getTrack(allData, track));

    var g = groupByClinicalAttribute(track, attr);
    var trackIndex = _.findIndex(allData, function(x) {
      return x.label == track;
    });
    var indent = allData[trackIndex].split? track.match(new RegExp("^\ *"))[0] : "";
    indent += "    ";

    // remove track
    allData = allData.filter(function(x) {return x.label !== track;});
    // Add new track
    allData.splice(trackIndex, 0, {"label":track+"."+attr,"times":[],"visible":true,"split":true});
    var attrValues = Object.keys(g);
    for (var i=0; i < attrValues.length; i++) {
      allData.splice(trackIndex+i+1, 0, {"label":indent+attrValues[i], "times":g[attrValues[i]], "visible":true,"split":true,"parent_track":track});
    }
  }

  function unSplitTrack(track) {
    var trackData = allData.filter(function(x) {return x.label.split(".")[0] === track;})[0];
    var times = allData.filter(function(x) {return x.split && x.parent_track === track;}).reduce(function(a, b) {
      return a.concat(b.times);
    }, []);
    trackData.times = times;
    trackData.visible = true;
    trackData.label = track;
    delete trackData.split;
    allData = allData.filter(function(x) {return !(x.split && x.parent_track === track);});
    timeline();
  }

  function sizeByClinicalAttribute(track, attr, minSize, maxSize) {
    var arr = getTrack(allData, track).times.map(function(x) {
      if (x.tooltip_tables.length === 1) {
        return parseFloat(String(x.tooltip_tables[0].filter(function(x) {
          return x[0] === attr;})[0][1]).replace(/[^\d.-]/g, ''));
      } else {
        return undefined;
      }
    });
    var scale = d3.scale.linear()
      .domain([d3.min(arr), d3.max(arr)])
      .range([minSize, maxSize]);
    getTrack(allData, track).times.forEach(function(x) {
      if (x.tooltip_tables.length === 1) {
        x.size = scale(parseFloat(String(x.tooltip_tables[0].filter(function(x) {
          return x[0] === attr;})[0][1]).replace(/[^\d.-]/g, ''))) || minSize;
      }
    });
  }

  function colorByClinicalAttribute(track, attr) {
    var g = groupByClinicalAttribute(track, attr);
    Object.keys(g).forEach(function(k) {
      g[k].forEach(function(x) {
        x.color = colorCycle(k);
      });
    });
  }

  function clearColors(track) {
    var times = getTrack(allData, track).times;
    for (var i=0; i < times.length; i++) {
      if ("color" in times[i]) {
        delete times[i].color;
      }
    }
  }

  timeline.addDataPointTooltip = function addDataPointTooltip(elem) {
    function createDataTable(tooltip_table) {
      dataTable = {
        "sDom": 't',
        "bJQueryUI": true,
        "bDestroy": true,
        "aaData": tooltip_table,
        "aoColumnDefs": [
          {
            "aTargets": [ 0 ],
            "sClass": "left-align-td",
            "mRender": function ( data, type, full ) {
               return '<b>'+data+'</b>';
            }
          },
          {
            "aTargets": [ 1 ],
            "sClass": "left-align-td",
            "bSortable": false
          }
        ],
        "fnDrawCallback": function ( oSettings ) {
          $(oSettings.nTHead).hide();
        },
        "aaSorting": []
      };
      return dataTable;
    }
    elem.qtip({
      content: {
        text: "table"
      },
      events: {
        render: function(event, api) {
          var tooltipDiv = $.parseHTML("<div></div>");
          var d = elem.prop("__data__");
          var table;
          if ("tooltip_tables" in d) {
            for (var i=0; i < d.tooltip_tables.length; i++) {
              if (i !== 0) {
                $(tooltipDiv).append("<hr />");
              }
              table = $.parseHTML("<table style='text-align:left; background-color: white;'></table>");
              $(table).dataTable(createDataTable(d.tooltip_tables[i]));
              $(tooltipDiv).append(table);
            }
          } else if ("tooltip" in d) {
            table = $.parseHTML("<table style='text-align:left; background-color: white;'></table>");
            $(table).dataTable(createDataTable(d.tooltip));
            $(tooltipDiv).append(table);
          }
          $(this).html(tooltipDiv);
          // Detect when point it was clicked and store it
          api.elements.target.click(function(e) {
            if (api.wasClicked) {
              api.hide();
              api.wasClicked = false;
            }
            else {
              api.wasClicked = !api.wasClicked;
            }
          });
        },
        hide: function(event, api) {
          // Prevent hiding if the point was clicked or if the
          // tooltip is already showing because of the mouseover
          if ((api.wasClicked && event.originalEvent.type === 'mouseleave') ||
              (!api.wasClicked && event.originalEvent.type === 'click')) {
              try{ event.preventDefault(); } catch(e) {}
          }
        }
      },
      show: {event: "mouseover"},
      hide: {event: "mouseleave"},
      style: { classes: 'qtip-light qtip-rounded qtip-wide' },
      position: {my:'top middle',at:'bottom middle',viewport: $(window)},
   });
  };

  function toggleTrackVisibility(trackName) {
    var trackData = getTrack(allData, trackName);
    trackData.visible = trackData.visible? false : true;
  }

  function toggleTrackCollapse(trackName) {
    var trackData = getTrack(allData, trackName);
    if (trackData.collapse) {
      trackData.collapse = false;
      splitTooltipTables(trackData);
    } else {
      if (!isDurationTrack(trackData)) {
        trackData.collapse = true;
      }
    }
  }

  function addNewTrackTooltip(elem) {
    elem.qtip({
    content: {
      text: 'addtrack'
    },
    events: {
      render: function(event, api) {
        invisibleTracks = allData.filter(function (x) {
          return !x.visible;
        });
        if (invisibleTracks.length === 0) {
          $(this).html("All tracks shown");
        }
        else {
          var trackAnchors = "";
          for (var i=0; i<invisibleTracks.length; i++) {
            trackAnchors +=
              "<a href='#' onClick='return false' class='show-track'>"+invisibleTracks[i].label+"</a><br />";
          }
          $(this).html(trackAnchors);
          $('.show-track').each(function () {
            $(this).on("click", function() {
              toggleTrackVisibility($(this).prop("innerHTML"));
            });
          });
        }
      }
    },
    show: {event: "mouseover"},
    hide: {fixed: true, delay: 0, event: "mouseout"},
    style: { classes: 'qtip-light qtip-rounded qtip-wide' },
    position: {my:'top middle',at:'top middle',viewport: $(window)},
    });
  }

  function addClinicalAttributesTooltip(elem, track, clickHandlerType) {
    function colorClickHandler() {
      colorByClinicalAttribute(track, $(this).prop("innerHTML"));
      timeline();
    }
    function splitClickHandler() {
      splitByClinicalAttribute(track, $(this).prop("innerHTML"));
      timeline();
    }
    function sizeByClickHandler() {
      sizeByClinicalAttribute(track, $(this).prop("innerHTML"), 2, itemHeight);
      timeline();
    }
    elem.qtip({
      content: {
        text: ''
      },
      events: {
        render: function(event, api) {
          if (clickHandlerType === "color") {
            clickHandler = colorClickHandler;
          } else if (clickHandlerType === "split") {
            clickHandler = splitClickHandler;
          } else if (clickHandlerType === "size") {
            clickHandler = sizeByClickHandler;
          } else {
            console.log("Unknown clickHandler for clinical attributes tooltip.");
          }
          var colorByAttribute = $.parseHTML("<div class='color-by-attr-tooltip'></div>");
          var clinAtts = getClinicalAttributes(allData, track);
          for (var i=0; i < clinAtts.length; i++) {
            var a = $.parseHTML("<a href='#' onClick='return false'>"+clinAtts[i]+"</a>");
            $(a).on("click", clickHandler);
            $(colorByAttribute).append(a);
            $(colorByAttribute).append("<br />");
          }
          $(this).html(colorByAttribute);
        }
      },
      show: {event: "click"},
      hide: {fixed: true, delay: 0, event: "mouseout"},
      style: { classes: 'qtip-light qtip-rounded qtip-wide' },
      position: {my:'left middle',at:'top middle',viewport: $(window)},
    });
  }


  function addSplittedTrackTooltip(elem, data) {
    function unSplitClickHandler(trackName) {
       return function() {
         unSplitTrack(trackName);
       };
    }
    elem.qtip({
      content: {
        text: 'track'
      },
      events: {
        render: function(event, api) {
          var trackTooltip = $.parseHTML("<div class='track-toolip'></div>");
          var a = $.parseHTML("<a href='#' onClick='return false' class='hide-track'>Unsplit</a>");
          $(a).on("click", unSplitClickHandler(elem.prop("innerHTML").split(".")[0]));
          $(trackTooltip).append(a);
          $(this).html(trackTooltip);
        }
      },
      show: {event: "mouseover"},
      hide: {fixed: true, delay: 0, event: "mouseout"},
      style: { classes: 'qtip-light qtip-rounded qtip-wide' },
      position: {my:'top middle',at:'top middle',viewport: $(window)},
    });
  }
  

  function addTrackTooltip(elem, data) {
    function hideTrackClickHandler(trackName) {
       return function() {
         toggleTrackVisibility(trackName);
         timeline();
       };
    }
    function collapseTrackClickHandler(trackName) {
      return function() {
        toggleTrackCollapse(trackName);
        timeline();
      };
    }
    elem.qtip({
      content: {
        text: 'track'
      },
      events: {
        render: function(event, api) {
          var trackTooltip = $.parseHTML("<div class='track-toolip'></div>");
          var a = $.parseHTML("<a href='#' onClick='return false' class='hide-track'>Hide " +
            elem.prop("innerHTML") + "</a>");
          $(a).on("click", hideTrackClickHandler(elem.prop("innerHTML")));
          $(trackTooltip).append(a);
          $(trackTooltip).append("<br />");

          if (!isDurationTrack(getTrack(allData, elem.prop("innerHTML")))) {
            a = $.parseHTML("<a href='#' onClick='return false' class='hide-track'>Collapse/Stack</a>");
            $(a).on("click", collapseTrackClickHandler(elem.prop("innerHTML")));
            $(trackTooltip).append(a);
            $(trackTooltip).append("<br />");
          }

          var colorBy = $.parseHTML("<a href='#' onClick='return false' class='color-by-attr'>Color by</a>");
          $(trackTooltip).append(colorBy);
          
          var clearColorsA = $.parseHTML("&nbsp;<a href='#' onClick='return false'>Clear</a>");
          $(clearColorsA).on("click", function() {
            clearColors(elem.prop("innerHTML"));
            timeline();
          });
          $(trackTooltip).append(clearColorsA);
          $(trackTooltip).append("<br />");

          var splitBy = $.parseHTML("<a href='#' onClick='return false' class='split-by-attr'>Split by</a>");
          $(trackTooltip).append(splitBy);
          $(trackTooltip).append("<br />");

          var sizeBy = $.parseHTML("<a href='#' onClick='return false' class='split-by-attr'>Size by</a>");
          $(trackTooltip).append(sizeBy);

          $(this).html(trackTooltip);
          addClinicalAttributesTooltip($(colorBy), elem.prop("innerHTML"), "color");
          addClinicalAttributesTooltip($(splitBy), elem.prop("innerHTML"), "split");
          addClinicalAttributesTooltip($(sizeBy), elem.prop("innerHTML"), "size");
        }
      },
      show: {event: "mouseover"},
      hide: {fixed: true, delay: 0, event: "mouseout"},
      style: { classes: 'qtip-light qtip-rounded qtip-wide' },
      position: {my:'top middle',at:'top middle',viewport: $(window)},
    });
  }

  function filterTrack(data, track) {
      return data.filter(function(x) {
          return x.label !== track;
      });
  }

  function getMaxEndingTime(data) {
      return Math.max.apply(Math, data.map(function (o){
          return Math.max.apply(Math, o.times.map(function(t) {
              return t.ending_time || 0;
          }));
      }));
  }

  function daysToTimeObject(dayCount) {
      var time = {};
      var daysPerYear = 365;
      var daysPerMonth = 30;
      time.daysPerYear = daysPerYear;
      time.daysPerMonth = daysPerMonth;
      time.y = dayCount > 0? Math.floor(dayCount / daysPerYear) : Math.ceil(dayCount / daysPerYear);
      time.m = dayCount > 0? Math.floor((dayCount % daysPerYear) / daysPerMonth) : Math.ceil((dayCount % daysPerYear) / daysPerMonth);
      time.d = Math.floor((dayCount % daysPerYear) % daysPerMonth);
      return time;
  }

  function formatTime(time) {
      var dayFormat = [];
      if (time.y !== 0) {
          dayFormat = dayFormat.concat(time.y+"y");
      }
      if (time.m !== 0) {
          dayFormat = dayFormat.concat(time.m+"m");
      }
      if (time.d !== 0) {
          dayFormat = dayFormat.concat(time.d+"d");
      }
      if (time.y === 0 && time.m === 0 && time.d === 0) {
          dayFormat = [0];
      }
      return dayFormat.join(" ");
  }

  function getTickValues(data) {
      tickValues = [];
      maxDays = Math.max.apply(Math, [getMaxEndingTime(allData), 1]);
      maxTime = daysToTimeObject(maxDays);
      if (maxTime.y >= 1) {
          for (var i=0; i <= maxTime.y; i++) {
              tickValues.push(i * maxTime.daysPerYear);
          }
      } else if (maxTime.y > 0 || maxTime.m  >= 1) {
          for (var i=0; i <= maxTime.m + (maxTime.y * maxTime.daysPerYear) / maxTime.daysPerMonth; i++) {
              tickValues.push(i * maxTime.daysPerMonth);
          }
      } else {
          for (var i=0; i <= maxDays; i++) {
              tickValues.push(i);
          }
      }
      return tickValues;
  }

  timeline.enableTrackTooltips = function(b) {
    if (!arguments.length) return enableTrackTooltips;

    if (b === true || b === false) {
      enableTrackTooltips = b;
    }
    return timeline;
  };

  timeline.width = function (w) {
    if (!arguments.length) return width;
    width = w;
    return timeline;
  };

  timeline.stackSlack = function () {
    if (!arguments.length) return stackSlack;
    return timeline;
  };

  timeline.data = function (data) {
    if (!arguments.length) return allData;
    allData = data;
    return timeline;
  };

  timeline.divId = function (name) {
    if (!arguments.length) return divId;
    divId = name;
    return timeline;
  };

  timeline.collapseAll = function() {
    var singlePointTracks = allData.filter(function(trackData) {
      return !isDurationTrack(trackData);
    });
    singlePointTracks.forEach(mergeTooltipTablesAtEqualTimepoint);
    singlePointTracks.forEach(function(x) { x.collapse = true; });
    return timeline;
  };

  /*
   * Order tracks by given array of label names. Tracks with label names not
   * included in the sequence are appended to the end in alhpanumeric order.
   */
  timeline.orderTracks = function(labels) {
    if (!arguments.length) {
      allData = _.sortBy(allData, 'label');
      return timeline;
    }

    var data = [];
    var track;
    // append given label ordering
    for (var i = 0; i < labels.length; i++) {
      track = getTrack(allData, labels[i]);
      if (track) {
        data = data.concat(track);
      }
    }
    // append missing labels
    data = data.concat(_.sortBy(allData.filter(function(x) {
      return labels.indexOf(x.label) === -1;
    }), 'label'));

    allData = data;
    return timeline;
  };

  /*
   * Split a track into multiple tracks based on the value of an
   * attribute in the tooltip_tables.
   */
  timeline.splitByClinicalAttribute = function(track, attr) {
    var trackData = getTrack(allData, track);
    if (trackData) {
      var attrData = trackData.times[0].tooltip_tables[0].filter(function(x) {
        return x[0] === attr;
      });
      if (attrData.length === 1) {
        splitByClinicalAttribute(track, attr);
      }
    }
    return timeline;
  };

  /*
   * Set the size of each timepoint in a track based on the value of an
   * attribute in the tooltip_tables.
   */
  timeline.sizeByClinicalAttribute = function(track, attr) {
    var trackData = getTrack(allData, track);
    if (trackData) {
      var attrData = trackData.times[0].tooltip_tables[0].filter(function(x) {
        return x[0] === attr;
      });
      if (attrData.length === 1) {
        sizeByClinicalAttribute(track, attr, 2, itemHeight);
      }
    }
    return timeline;
  };

  /*
   * Collapse or stack timepoints on given track
   */
  timeline.toggleTrackCollapse = function(track) {
    var trackData = getTrack(allData, track);
    if (trackData) {
      toggleTrackCollapse(track);
    }
    return timeline;
  };

  /*
   * Set the display attribute for all timepoints with one tooltip_table on a
   * given track.
   */
  timeline.setTimepointsDisplay = function(track, display) {
    var trackData = getTrack(allData, track);
    if (trackData) {
      trackData.times.forEach(function(x) {
        if (x.tooltip_tables.length === 1) {
          x.display = display;
        }
      });
    }
    return timeline;
  };

  timeline.addPostTimelineHook = function(hook) {
    postTimelineHooks = postTimelineHooks.concat(hook);
    return timeline;
  };

  return timeline;
})();
// vim: ts=2 sw=2
window.clinicalTimelineParser = (function() {
  function transformToTimelineJSON(clinical_timeline_data) {
    var transformToTrack = function(clin_events) {
      var track = {
        "label":clin_events[0].eventType,
        visible:true,
        "times":[]
      };
      clin_events.forEach(function(x) {
        var timepoint = {
          "starting_time":x.startDate,
          "ending_time":x.stopDate !== null? x.stopDate:x.startDate,
          "display":x.stopDate !== null? "rect":"circle",
          tooltip_tables:[Object.keys(x.eventData).map(function(k) {
            return [k, x.eventData[k]];
          })]
        };
        timepoint.tooltip_tables[0].push(["START_DATE", x.startDate]);
        if (x.stopDate !== null) {
          timepoint.tooltip_tables[0].push(["STOP_DATE", x.stopDate]);
        }
        track.times.push(timepoint);
      });
      return track;
    };
    var g = _.groupBy(clinical_timeline_data, "eventType");
    var tracks = [];
    _.each(g, function(x) {tracks.push(transformToTrack(x));});
    return tracks;
  }

  return transformToTimelineJSON;
})();
