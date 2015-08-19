function PancancerStudySummaryHistogram()
{
	
	// Some semi-global utilities
    // Here are some options that we will use in this view
    var width = 1100;
    var height = 650;
    var paddingLeft = 80;
    var paddingRight = 50;
    var paddingTop = 10;
    var histBottom = 400;
    var fontFamily = "sans-serif";
    var animationDuration = 1000;
    var maxStudyBarWidth = 30;

    var defaultQTipOptions = {
        content: {
            text: "Default qtip text"
        },
        hide: {
            fixed: true,
            delay: 250,
            event: 'mouseout'
        },
        show: {
            event: 'mouseover'
        },
        style: {
            classes: 'qtip-light qtip-rounded qtip-shadow cc-study-tip cc-ui-tooltip'
        },
        position: {
            my:'bottom left', at:'top center'
        }
    };

    var getTypeOfCancer = function(study) {
        //return metaData.cancer_studies[study.studyId].short_name;  //maybe add this to study? Using id for now:
    	return study.typeOfCancer; //PA
    };

    var calculateFrequency = function(d, type) {
        return d.alterations[type]/ d.caseSetLength;
    };

    var hasCnaData = function(alterations) {
    	//TODO maybe include cnaLoss, cnaGain?
    	if (alterations.cnaUp > 0 || alterations.cnaDown > 0)
    		return true;
    	return false;
    }
    
    
	this.init = function(histogramEl, model){
		
	    var getY = function(d, type) {
	    	if (model.get("sortYAxis") == "Absolute Counts")
	    		return d.alterations[type];
	    	else
	    		return calculateFrequency(d, type);
	    };
		
	    var getYlabel = function(d) {
	    	if (model.get("sortYAxis") == "Absolute Counts")
	    		return d
	    	else
	    		return Math.round(parseFloat(d) * 100) + "%"; 
	    };

	    //get data via presenter layer:
	    var histogramPresenter = new HistogramPresenter(model);	    
		var histData = histogramPresenter.getJSONDataForHistogram();
		//var metaData = metadata_temp;
		
		// main values: 
	    var studyWidth = Math.min(((width - (paddingLeft + paddingRight)) / histData.length) * .75, maxStudyBarWidth);
	    var studyLocIncrements = studyWidth / .75;
	    // Data type radius
	    var circleDTR = studyWidth / 4;
	    // Tumor type radius
	    var circleTTR = Math.min(studyWidth, 20) / 2;

	    var key = function(d) {
	        return d.typeOfCancer;
	    };

	    var yMax = parseFloat(d3.max(histData, function (d) {
            return getY(d, "all");
        })) + .05;
	    //alert(yMax);
	    
	    var yScale = d3.scale.linear()
	        .domain([
	        0,
	        yMax
	    ])
	    .range([histBottom-paddingTop, 0]);    

		
		
		// Empty the content
	    $(histogramEl).html("");
	
	    // and initialize the histogram
	    var histogram = d3.select(histogramEl)
	        .append("svg")
	        .attr("width", width)
	        .attr("height", height);
	
	    // define Y axis
	    var yAxis = d3.svg.axis()
	        .scale(yScale)
	        .orient("left");
	
	    var otherBarGroup = histogram.append("g");
	    otherBarGroup.selectAll("rect")
	        .data(histData, key)
	        .enter()
	        .append("rect")
	        .attr("fill", "#aaaaaa")
	        .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	        .attr("y", function(d, i) { return yScale(getY(d, "other")) + paddingTop; })
	        .attr("width", studyWidth)
	        .attr("height", function(d, i) {
	            return (histBottom-paddingTop) - yScale(getY(d, "other"));
	        })
	        .style("stroke", "white")
	        .style("stroke-width", "1")
	        .attr("class", function(d, i) { return d.studyId + " alt-other" })
	    ;
	
	    var mutBarGroup = histogram.append("g");
	    mutBarGroup.selectAll("rect")
	        .data(histData, key)
	        .enter()
	        .append("rect")
	        .attr("fill", "green")
	        .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	        .attr("y", function(d, i) {
	            return yScale(getY(d, "mutation"))
	                - ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                + paddingTop;
	        })
	        .attr("width", studyWidth)
	        .attr("height", function(d, i) {
	            return (histBottom-paddingTop) - yScale(getY(d, "mutation"));
	
	        })
	        .style("stroke", "white")
	        .style("stroke-width", "1")
	        .attr("class", function(d, i) { return d.studyId + " alt-mut" })
	    ;
	
	    var cnalossBarGroup = histogram.append("g");
	    cnalossBarGroup.selectAll("rect")
	        .data(histData, key)
	        .enter()
	        .append("rect")
	        .attr("fill", "skyblue")
	        .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	        .attr("y", function(d, i) {
	            return yScale(getY(d, "cnaLoss"))
	                - (
	                ((histBottom-paddingTop) - yScale(getY(d, "mutation")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                )
	                + paddingTop;
	        })
	        .attr("width", studyWidth)
	        .attr("height", function(d, i) {
	            return (histBottom-paddingTop) - yScale(getY(d, "cnaLoss"));
	        })
	        .style("stroke", "white")
	        .style("stroke-width", "1")
	        .attr("class", function(d, i) { return d.studyId + " alt-cnaloss" })
	    ;
	
	
	    var cnadownBarGroup = histogram.append("g");
	    cnadownBarGroup.selectAll("rect")
	        .data(histData, key)
	        .enter()
	        .append("rect")
	        .attr("fill", "blue")
	        .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	        .attr("y", function(d, i) {
	            return yScale(getY(d, "cnaDown"))
	                - (
	                    ((histBottom-paddingTop) - yScale(getY(d, "mutation")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "cnaLoss")))
	                )
	                + paddingTop;
	        })
	        .attr("width", studyWidth)
	        .attr("height", function(d, i) {
	            return (histBottom-paddingTop) - yScale(getY(d, "cnaDown"));
	        })
	        .style("stroke", "white")
	        .style("stroke-width", "1")
	        .attr("class", function(d, i) { return d.studyId + " alt-cnadown" })
	    ;
	
	    var cnaupBarGroup = histogram.append("g");
	    cnaupBarGroup.selectAll("rect")
	        .data(histData, key)
	        .enter()
	        .append("rect")
	        .attr("fill", "red")
	        .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	        .attr("y", function(d, i) {
	            return yScale(getY(d, "cnaUp"))
	                - (
	                    ((histBottom-paddingTop) - yScale(getY(d, "mutation")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "cnaLoss")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "cnaDown")))
	                )
	                + paddingTop;
	        })
	        .attr("width", studyWidth)
	        .attr("height", function(d, i) {
	            return (histBottom-paddingTop) - yScale(getY(d, "cnaUp"));
	        })
	        .style("stroke", "white")
	        .style("stroke-width", "1")
	        .attr("class", function(d, i) { return d.studyId + " alt-cnaup" })
	    ;
	
	    var cnagainBarGroup = histogram.append("g");
	    cnagainBarGroup.selectAll("rect")
	        .data(histData, key)
	        .enter()
	        .append("rect")
	        .attr("fill", "lightpink")
	        .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	        .attr("y", function(d, i) {
	            return yScale(getY(d, "cnaGain"))
	                - (
	                ((histBottom-paddingTop) - yScale(getY(d, "mutation")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "cnaLoss")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "cnaDown")))
	                    + ((histBottom-paddingTop) - yScale(getY(d, "cnaUp")))
	                )
	                + paddingTop;
	        })
	        .attr("width", studyWidth)
	        .attr("height", function(d, i) {
	            return (histBottom-paddingTop) - yScale(getY(d, "cnaGain"));
	        })
	        .style("stroke", "white")
	        .style("stroke-width", "1")
	        .attr("class", function(d, i) { return d.studyId + " alt-cnagain" })
	    ;
	
	    var infoBarGroup = histogram.append("g");
	    infoBarGroup.selectAll("rect")
	        .data(histData, key)
	        .enter()
	        .append("rect")
	        .attr("fill", "#aaaaaa")
	        .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	        .attr("y", function(d, i) { return yScale(getY(d, "all")) + paddingTop; })
	        .attr("width", studyWidth)
	        .attr("height", function(d, i) {
	            return (histBottom-paddingTop) - yScale(getY(d, "all"));
	        })
	        .style("opacity",0)
	        .style("stroke", "white")
	        .style("cursor", "pointer")
	        .style("stroke-width", "1")
	        .attr("class", function(d, i) { return d.studyId + " alt-info" })
	        .each(function(d, i) {
	            var container = $("<div></div>");
	            //(new StudyToolTipView({  //see crosscancer.js for example
	        });
	
	    var abbrGroups = histogram.append("g");
	    abbrGroups.selectAll("text")
	        .data(histData, key)
	        .enter()
	        .append("text")
	        .text(function(d, i) {
	            return getTypeOfCancer(d);
	        })
	        .attr("font-family", fontFamily)
	        .attr("font-size", function() { return Math.min((studyWidth * .65), 12) + "px"; })
	        .attr("x", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth*.5; })
	        .attr("y", function() { return histBottom + 10; })
	        .attr("text-anchor", "end")
	        .attr("transform", function(d, i) {
	            var xLoc = paddingLeft + i*studyLocIncrements + studyWidth*.5;
	            var yLoc = histBottom + 10;
	            return "rotate(-60, " + xLoc + ", " + yLoc +  ")";
	        })
	        .attr("class", function(d, i) { return d.studyId + " annotation-abbr" })
	        .each(function(d, i) {
	            var qOpts = _.extend(defaultQTipOptions, {
	                content: d.studyId, //metaData.cancer_studies[d.studyId].name, //TODO maybe add this to study itself....using id for now....OR typeOfCancer??
	                position: { viewport: $(window) }
	            });
	            $(this).qtip(qOpts);
	        })
	    ;
	    
	    var yAxisEl = histogram.append("g")
	        .attr("class", "axis")
	        .attr("transform", "translate(" + (paddingLeft-10) + ", " + paddingTop + ")")
	        .call(yAxis);
	
	    // Define where the label should appear
	    var labelCorX = 15;
	    var labelCorY = paddingTop + (histBottom/2);
	
	    // Add axis label
	    histogram.append("g")
	        .selectAll("text")
	        .data([model.get("sortYAxis")])
	        .enter()
	        .append("text")
	        .text(function(d, i) { return d; })
	        .attr("font-family", fontFamily)
	        .attr("font-size", "13px")
	        .attr("x", labelCorX)
	        .attr("y", labelCorY)
	        .attr("transform", "rotate(-90, " + labelCorX + ", " + labelCorY +")")
	    ;
	
	    var mutLegend = { label: "Mutation", color: "green"};
	    var lossLegend = { label: "Het. Loss", color: "skyblue"};
	    var delLegend = { label: "Deletion", color: "blue"};
	    var gainLegend = { label: "Gain", color: "lightpink"};
	    var ampLegend = { label: "Amplification", color: "red"};
	    var multpLegend = { label: "Multiple alterations", color: "#aaaaaa" };
	
	    var legendData = [];
	    /*switch(priority * 1) {
	        case 0:
	            legendData.push(mutLegend);
	            if(isThereHetLoss) {legendData.push(lossLegend); }
	            legendData.push(delLegend);
	            if(isThereGain) { legendData.push(gainLegend); }
	            legendData.push(ampLegend);
	            legendData.push(multpLegend);
	            break;
	        case 1:*/
	            legendData.push(mutLegend);
	            //break;
	        /*case 2:
	            if(isThereHetLoss) {legendData.push(lossLegend); }
	            legendData.push(delLegend);
	            if(isThereGain) { legendData.push(gainLegend); }
	            legendData.push(ampLegend);
	            legendData.push(multpLegend);
	            break;
	    }*/
	
	    var legendWidth = 125;
	    var numOfLegends = legendData.length;
	    var legBegPoint = (width-paddingLeft-paddingRight-(numOfLegends*legendWidth))/2;
	    // Now add the legends
	    var legend = histogram.append("g");
	    legend.selectAll("rect")
	        .data(legendData)
	        .enter()
	        .append("rect")
	        .attr('x', function(d, i) { return legBegPoint + i*legendWidth + 10; })
	        .attr('y', height-20)
	        .attr('width', 19)
	        .attr('height', 19)
	        .style('fill', function(d) { return d.color; })
	    ;
	    legend.selectAll("text")
	        .data(legendData)
	        .enter()
	        .append("text")
	        .attr('x', function(d, i) { return legBegPoint + i*legendWidth + 35; })
	        .attr('y', height-5)
	        .text(function(d, i) { return d.label; })
	        .attr("font-family", fontFamily)
	        .attr("font-size", "15px")
	    ;
	
	    // Give some style
	    yAxisEl.selectAll("path, line")
	        .attr("fill", "none")
	        .attr("stroke", "black")
	        .attr("shape-rendering", "crispEdges");
	
	    // d3 formating 
	    yAxisEl.selectAll("text")
	        .attr("font-family", fontFamily)
	        .attr("font-size", "11px")
	        .each(function(d) {
	            $(this).text(getYlabel(d) );
	        });
	}
	
}


//'presenter' layer to expose the parameters from query, formating its data for display in the views
function HistogramPresenter(model)
{
	this.model = model;
	
	// this method will retrieve the data for the histogram according to the 
	// settings found in the model and format this into the correct JSON format
	// to be used in the D3JS functions to draw the histogram:
	this.getJSONDataForHistogram = function(){
		//get data (from external temp files for now):
		var histData = data_temp; //TODO call the "data manager" layer to retrieve the data and transform to correct JSON structure
		return histData;
	}

}

	/*
	 * is this for the animation effect???
	 * 
	    var genes = _.last(histData).genes;
	    var numOfGenes = genes.length;
	    var numOfStudies = histData.length;
	
	    (new CCTitleView({
	       model: {
	           numOfStudies: numOfStudies,
	           numOfGenes: numOfGenes,
	           genes: genes.join(', ')
	               }
	            })).render();
	
	     var redrawHistogram = function() {
	        histData = filterAndSortData(histDataOrg);
	
	        studyWidth = Math.min(((width - (paddingLeft + paddingRight)) / histData.length) * .75, maxStudyBarWidth);
	        studyLocIncrements = studyWidth / .75;
	                // Data type radius
	        circleDTR = studyWidth / 4;
	        // Tumor type radius
	        circleTTR = Math.min(studyWidth, 20) / 2;
	
	        var stacked = $("#histogram-show-colors").is(":checked");
	        var outX = width + 1000;
	
	        yScale
	            .domain([
	            0,
	            Math.min(
	                1.0,
	                parseFloat(d3.max(histData, function (d, i) {
	                    return fixFloat(getY(d, "all"), 1);
	                })) + .05
	            )
	        ])
	        .range([histBottom-paddingTop, 0]);
	
	        yAxisEl
	            .transition()
	            .duration(animationDuration)
	            .call(yAxis);
	        // Give some style
	        yAxisEl.selectAll("path, line")
	            .attr("fill", "none")
	            .attr("stroke", "black")
	            .attr("shape-rendering", "crispEdges");
	        yAxisEl.selectAll("text")
	            .attr("font-family", fontFamily)
	            .attr("font-size", "11px")
	            .each(function(d, i) {
	                $(this).text(fixFloat($(this).text() * 100, 1) + "%" );
	            });
	
	        var obg = otherBarGroup.selectAll("rect").data(histData, key);
	        obg.exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        obg.transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	            .attr("y", function(d, i) { return yScale(getY(d, "other")) + paddingTop; })
	            .attr("width", studyWidth)
	            .attr("height", function(d, i) {
	                return (histBottom-paddingTop) - yScale(getY(d, "other"));
	            })
	        ;
	
	        var mbg = mutBarGroup.selectAll("rect").data(histData, key);
	        mbg.exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        mbg.transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	            .attr("y", function(d, i) {
	                return yScale(getY(d, "mutation"))
	                    - ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                    + paddingTop;
	            })
	            .attr("width", studyWidth)
	            .attr("height", function(d, i) {
	                return (histBottom-paddingTop) - yScale(getY(d, "mutation"));
	
	            })
	        ;
	
	        var clbg = cnalossBarGroup.selectAll("rect").data(histData, key);
	        clbg
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        clbg
	            .transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	            .attr("y", function(d, i) {
	                return yScale(getY(d, "cnaLoss"))
	                    - (
	                    ((histBottom-paddingTop) - yScale(getY(d, "mutation")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                    )
	                    + paddingTop;
	            })
	            .attr("width", studyWidth)
	            .attr("height", function(d, i) {
	                return (histBottom-paddingTop) - yScale(getY(d, "cnaLoss"));
	            })
	        ;
	
	
	        var cdbg = cnadownBarGroup.selectAll("rect").data(histData, key);
	        cdbg
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        cdbg
	            .transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	            .attr("y", function(d, i) {
	                return yScale(getY(d, "cnaDown"))
	                    - (
	                    ((histBottom-paddingTop) - yScale(getY(d, "mutation")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "cnaLoss")))
	                    )
	                    + paddingTop;
	            })
	            .attr("width", studyWidth)
	            .attr("height", function(d, i) {
	                return (histBottom-paddingTop) - yScale(getY(d, "cnaDown"));
	            })
	        ;
	
	        var cubp = cnaupBarGroup.selectAll("rect").data(histData, key);
	        cubp
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        cubp
	            .transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	            .attr("y", function(d, i) {
	                return yScale(getY(d, "cnaUp"))
	                    - (
	                    ((histBottom-paddingTop) - yScale(getY(d, "mutation")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "cnaLoss")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "cnaDown")))
	                    )
	                    + paddingTop;
	            })
	            .attr("width", studyWidth)
	            .attr("height", function(d, i) {
	                return (histBottom-paddingTop) - yScale(getY(d, "cnaUp"));
	            })
	        ;
	
	        var cgbp = cnagainBarGroup.selectAll("rect").data(histData, key);
	        cgbp
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        cgbp
	            .transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	            .attr("y", function(d, i) {
	                return yScale(getY(d, "cnaGain"))
	                    - (
	                    ((histBottom-paddingTop) - yScale(getY(d, "mutation")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "other")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "cnaLoss")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "cnaDown")))
	                        + ((histBottom-paddingTop) - yScale(getY(d, "cnaUp")))
	                    )
	                    + paddingTop;
	            })
	            .attr("width", studyWidth)
	            .attr("height", function(d, i) {
	                return (histBottom-paddingTop) - yScale(getY(d, "cnaGain"));
	            })
	        ;
	
	        var ibg = infoBarGroup.selectAll("rect").data(histData, key);
	        ibg
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        ibg
	            .transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
	            .attr("y", function(d, i) { return yScale(getY(d, "all")) + paddingTop; })
	            .attr("width", studyWidth)
	            .attr("height", function(d, i) {
	                return (histBottom-paddingTop) - yScale(getY(d, "all"));
	            })
	            .style("opacity", stacked ? 0 : 1)
	        ;
	
	        var ct = cancerTypes.selectAll("circle").data(histData, key);
	        ct
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("cx", outX)
	        ;
	        ct
	            .transition()
	            .duration(animationDuration)
	            .attr("cx", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth/2; } )
	            .attr("cy", function(d, i) { return histBottom + verticalCirclePadding })
	            .attr("r", circleTTR)
	        ;
	
	        var mg = mutGroups.selectAll("text").data(histData, key);
	        mg
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        mg
	            .transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth/2; } )
	        ;
	
	        var cg = cnaGroups.selectAll("text").data(histData, key);
	        cg
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	        ;
	        cg
	            .transition()
	            .duration(animationDuration)
	            .attr("x", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth/2; } )
	        ;
	
	        var ag = abbrGroups.selectAll("text").data(histData, key);
	        ag
	            .exit()
	            .transition()
	            .duration(animationDuration)
	            .attr("x", outX)
	            .attr("transform", function(d, i) {
	                var xLoc = paddingLeft + i*studyLocIncrements + studyWidth*.75;
	                var yLoc = histBottom + verticalCirclePadding*4;
	                return "rotate(-60, " + xLoc + ", " + yLoc +  ")";
	            })
	        ;
	        ag
	            .transition()
	            .duration(animationDuration)
	            .text(function(d, i) {
	                return getTypeOfCancer(d, metaData);
	            })
	            .attr("font-size", function() { return Math.min((studyWidth * .65), 12) + "px"; })
	            .attr("x", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth*.5; })
	            .attr("transform", function(d, i) {
	                var xLoc = paddingLeft + i*studyLocIncrements + studyWidth*.5;
	                var yLoc = histBottom + verticalCirclePadding*4;
	                return "rotate(-60, " + xLoc + ", " + yLoc +  ")";
	            })
	        ;
	
	        legend
	            .transition()
	            .duration(animationDuration)
	            .style("opacity", stacked ? 1 : 0)
	        ;
	    }; // end of redraw
	*/
