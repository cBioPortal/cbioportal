/*
 * Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

(function($, _, Backbone, d3) {

	// TODO 3d Visualizer should be initialized before document get ready
	// ...due to incompatible Jmol initialization behavior
	var _mut3dVis = null;
	_mut3dVis = new Mutation3dVis("crossCancer3dView");
	_mut3dVis.init();

	// Prepare eveything only if the page is ready to load
    $(function(){
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

        var getStudyAbbr = function(study, metaData) {
            return metaData.cancer_studies[study.studyId].short_name;
        };

        var calculateFrequency = function(d, i, type) {
            return d.alterations[type]/ d.caseSetLength;
        };

        var isThereHetLoss = false;
        var isThereGain = false;
        var filterAndSortData = function(histDataOrg) {
            var histData = [];
            _.each(histDataOrg, function(study) {
                var showStudy = $("#histogram-remove-study-" + study.studyId).is(":checked");
                if(!study.skipped && showStudy)
                    histData.push(study);

                if(study.alterations.cnaLoss > 0) { isThereHetLoss = true; }
                if(study.alterations.cnaGain > 0) { isThereGain = true; }
            });

            switch($("#histogram-sort-by").val()) {
                case "alteration":
                    // Sort by total number of frequency
                    histData.sort(function(a, b) {
                         return calculateFrequency(b, 0, "all") - calculateFrequency(a, 1, "all");
                    });
                    break;
                case "name":
                    break; // keep the order
            }

            return histData;
        };

        var fixFloat = function(number, digit) {
            var multiplier = Math.pow( 10, digit );
            return Math.round( number * multiplier ) / multiplier;
        };

        /* Views */
        var MainView = Backbone.View.extend({
            el: "#crosscancer-container",
            template: _.template($("#cross-cancer-main-tmpl").html()),

            render: function() {
                this.$el.html(this.template(this.model));

                $("#tabs").tabs({ active: this.model.tab == "mutation" ? 1 : 0 }).show();

                var priority = this.model.priority;
                if(priority == 2) {
                    $("#cc-mutations-link").parent().hide();
                } else {
                    $("#cc-mutations-link").parent().show();
                }

                var genes = this.model.genes;
                var orgQuery = this.model.genes;

                var studies = new Studies({
                    gene_list: genes,
                    data_priority: priority
                });

                studies.fetch({
                    success: function() {
                        window.studies = studies;

                        $.getJSON("portal_meta_data.json", function(metaData) {
                            var histDataOrg = studies.toJSON();
                            (new HideStudyControlView({
                                model: {
                                    metaData: metaData,
                                    studies: histDataOrg
                                }
                            })).render();
                            var histData = filterAndSortData(histDataOrg);

                            (new DownloadSummaryView({
                                model: {
                                    metaData: metaData,
                                    studies: histData
                                }
                            })).render();

                            var hiddenStudies = _.reduce(histDataOrg, function(seed, study) {
                                if(study.skipped) {
                                    seed.push(study);
                                }
                                return seed;
                            }, []);

                            (new StudiesWithNoDataView({
                                model: {
                                    hiddenStudies: hiddenStudies,
                                    metaData: metaData,
                                    priority: priority
                                }
                            })).render();

                            var studyLocIncrements = (width - (paddingLeft + paddingRight)) / histData.length;
                            var studyWidth = studyLocIncrements * .75;
                            var verticalCirclePadding = 20;
                            // Data type radius
                            var circleDTR = studyWidth / 4;
                            // Tumor type radius
                            var circleTTR = Math.min(studyWidth, 20) / 2;

                            var color = function(cType) {
                                return metaData.cancer_colors[cType];
                            };

                            var key = function(d) {
                                return d.studyId;
                            };

                            var yScale = d3.scale.linear()
                                .domain([
                                0,
                                Math.min(
                                    1.0,
                                    parseFloat(d3.max(histData, function (d, i) {
                                        return fixFloat(calculateFrequency(d, i, "all"), 1);
                                    })) + .05
                                )
                            ])
                            .range([histBottom-paddingTop, 0]);

                            // Empty the content
                            $("#cchistogram").html("");

                            // and initialize the histogram
                            var histogram = d3.select("#cchistogram")
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
                                .attr("y", function(d, i) { return yScale(calculateFrequency(d, i, "other")) + paddingTop; })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other"));
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
                                    return yScale(calculateFrequency(d, i, "mutation"))
                                        - ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                        + paddingTop;
                                })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation"));

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
                                    return yScale(calculateFrequency(d, i, "cnaLoss"))
                                        - (
                                        ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                        )
                                        + paddingTop;
                                })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaLoss"));
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
                                    return yScale(calculateFrequency(d, i, "cnaDown"))
                                        - (
                                            ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaLoss")))
                                        )
                                        + paddingTop;
                                })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaDown"));
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
                                    return yScale(calculateFrequency(d, i, "cnaUp"))
                                        - (
                                            ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaLoss")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaDown")))
                                        )
                                        + paddingTop;
                                })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaUp"));
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
                                    return yScale(calculateFrequency(d, i, "cnaGain"))
                                        - (
                                        ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaLoss")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaDown")))
                                            + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaUp")))
                                        )
                                        + paddingTop;
                                })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaGain"));
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
                                .attr("y", function(d, i) { return yScale(calculateFrequency(d, i, "all")) + paddingTop; })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "all"));
                                })
                                .style("opacity",0)
                                .style("stroke", "white")
                                .style("cursor", "pointer")
                                .style("stroke-width", "1")
                                .attr("class", function(d, i) { return d.studyId + " alt-info" })
                                .each(function(d, i) {
                                    var container = $("<div></div>");
                                    (new StudyToolTipView({
                                        el: container,
                                        model: {
                                            study: d,
                                            metaData: metaData,
                                            genes: orgQuery
                                        }
                                    })).render();

                                    var qOpts = _.extend(defaultQTipOptions, {
                                        content: container.html(),
                                        position: { viewport: $(window) }
                                    });
                                    $(this).qtip(qOpts);

                                    $(this).click(function(e) {
                                        e.preventDefault();

                                        var sLink = _.template($("#study-link-tmpl").html(), {
                                            study: d,
                                            genes: orgQuery
                                        });

                                        window.open($(sLink).attr("href"), "_blank");
                                    });
                                });


                            var annotations = histogram.append("g");
                            annotations.selectAll("text")
                                .data(["Cancer type", "Mutation data", "CNA data"])
                                .enter()
                                .append("text")
                                .attr("y", function(d, i) { return histBottom + verticalCirclePadding*(i+1) + 3 })
                                .attr("x", function(d, i) { return paddingLeft - 10; })
                                .text(function(d, i) { return d; })
                                .attr("text-anchor", "end")
                                .attr("font-family", fontFamily)
                                .attr("font-weight", "bold")
                                .attr("font-size", "10px")
                            ;

                            // This is for cancer type
                            var cancerTypes = histogram.append("g");
                            cancerTypes.selectAll("circle")
                                .data(histData, key)
                                .enter()
                                .append("circle")
                                .attr("fill", function(d, i) {
                                    return color(metaData.cancer_studies[d.studyId].type_of_cancer);
                                })
                                .attr("cx", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth/2; } )
                                .attr("cy", function(d, i) { return histBottom + verticalCirclePadding })
                                .attr("r", circleTTR)
                                .attr("class", function(d, i) { return d.studyId + " annotation-type" })
                                .style("stroke", "lightgray")
                                .style("stroke-width", "1")
                                .each(function(d, i) {
                                    var qOpts = _.extend(defaultQTipOptions, {
                                        content: metaData.type_of_cancers[metaData.cancer_studies[d.studyId].type_of_cancer],
                                        position: { viewport: $(window) }
                                    });
                                    $(this).qtip(qOpts);
                                });

                            var mutGroups = histogram.append("g");
                            // This is for mutation data availability
                            mutGroups.selectAll("text")
                                .data(histData, key)
                                .enter()
                                .append("text")
                                .attr("x", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth/2; } )
                                .attr("y", function() { return histBottom + verticalCirclePadding*2 + circleDTR/2 })
                                .text(function(d, i) {
                                    return metaData.cancer_studies[d.studyId].has_mutation_data ? "+" : "-";
                                })
                                .attr("text-anchor", "middle")
                                .attr("font-weight", "bold")
                                .attr("font-size", "10px")
                                .attr("class", function(d, i) { return d.studyId + " annotation-mut" })
                                .each(function(d, i) {
                                    var qOpts = _.extend(defaultQTipOptions, {
                                        content: metaData.cancer_studies[d.studyId].has_mutation_data
                                            ? "Mutation data available"
                                            : "Mutation data not available",
                                        position: { viewport: $(window) }
                                    });
                                    $(this).qtip(qOpts);
                                });


                            // This is for CNA data availability
                            var cnaGroups = histogram.append("g");
                            cnaGroups.selectAll("text")
                                .data(histData, key)
                                .enter()
                                .append("text")
                                .attr("x", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth/2; } )
                                .attr("y", function() { return histBottom + verticalCirclePadding*3 + circleDTR/2 })
                                .text(function(d, i) {
                                    return metaData.cancer_studies[d.studyId].has_cna_data ? "+" : "-";
                                })
                                .attr("text-anchor", "middle")
                                .attr("font-weight", "bold")
                                .attr("font-size", "10px")
                                .attr("class", function(d, i) { return d.studyId + " annotation-cna" })
                                .each(function(d) {
                                    var qOpts = _.extend(defaultQTipOptions, {
                                        content: metaData.cancer_studies[d.studyId].has_cna_data
                                            ? "CNA data available"
                                            : "CNA data not available",
                                        position: { viewport: $(window) }
                                    });
                                    $(this).qtip(qOpts);
                                });


                            var abbrGroups = histogram.append("g");
                            abbrGroups.selectAll("text")
                                .data(histData, key)
                                .enter()
                                .append("text")
                                .text(function(d, i) {
                                    return getStudyAbbr(d, metaData);
                                })
                                .attr("font-family", fontFamily)
                                .attr("font-size", function() { return Math.min((studyWidth * .65), 12) + "px"; })
                                .attr("x", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth*.5; })
                                .attr("y", function() { return histBottom + verticalCirclePadding*4 })
                                .attr("text-anchor", "end")
                                .attr("transform", function(d, i) {
                                    var xLoc = paddingLeft + i*studyLocIncrements + studyWidth*.5;
                                    var yLoc = histBottom + verticalCirclePadding*4;
                                    return "rotate(-60, " + xLoc + ", " + yLoc +  ")";
                                })
                                .attr("class", function(d, i) { return d.studyId + " annotation-abbr" })
                                .each(function(d, i) {
                                    var qOpts = _.extend(defaultQTipOptions, {
                                        content: metaData.cancer_studies[d.studyId].name,
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
                                .data(["Alteration frequency"])
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
                            switch(priority * 1) {
                                case 0:
                                    legendData.push(mutLegend);
                                    if(isThereHetLoss) {legendData.push(lossLegend); }
                                    legendData.push(delLegend);
                                    if(isThereGain) { legendData.push(gainLegend); }
                                    legendData.push(ampLegend);
                                    legendData.push(multpLegend);
                                    break;
                                case 1:
                                    legendData.push(mutLegend);
                                    break;
                                case 2:
                                    if(isThereHetLoss) {legendData.push(lossLegend); }
                                    legendData.push(delLegend);
                                    if(isThereGain) { legendData.push(gainLegend); }
                                    legendData.push(ampLegend);
                                    legendData.push(multpLegend);
                                    break;
                            }

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

                            // d3 formating TODO!
                            yAxisEl.selectAll("text")
                                .attr("font-family", fontFamily)
                                .attr("font-size", "11px")
                                .each(function(d, i) {
                                    $(this).text(fixFloat($(this).text() * 100, 1) + "%" );
                                });

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

                                studyLocIncrements = (width - (paddingLeft + paddingRight)) / histData.length;
                                studyWidth = studyLocIncrements * .75;
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
                                            return fixFloat(calculateFrequency(d, i, "all"), 1);
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
                                    .attr("y", function(d, i) { return yScale(calculateFrequency(d, i, "other")) + paddingTop; })
                                    .attr("width", studyWidth)
                                    .attr("height", function(d, i) {
                                        return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other"));
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
                                        return yScale(calculateFrequency(d, i, "mutation"))
                                            - ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                            + paddingTop;
                                    })
                                    .attr("width", studyWidth)
                                    .attr("height", function(d, i) {
                                        return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation"));

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
                                        return yScale(calculateFrequency(d, i, "cnaLoss"))
                                            - (
                                            ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                            )
                                            + paddingTop;
                                    })
                                    .attr("width", studyWidth)
                                    .attr("height", function(d, i) {
                                        return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaLoss"));
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
                                        return yScale(calculateFrequency(d, i, "cnaDown"))
                                            - (
                                            ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaLoss")))
                                            )
                                            + paddingTop;
                                    })
                                    .attr("width", studyWidth)
                                    .attr("height", function(d, i) {
                                        return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaDown"));
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
                                        return yScale(calculateFrequency(d, i, "cnaUp"))
                                            - (
                                            ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaLoss")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaDown")))
                                            )
                                            + paddingTop;
                                    })
                                    .attr("width", studyWidth)
                                    .attr("height", function(d, i) {
                                        return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaUp"));
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
                                        return yScale(calculateFrequency(d, i, "cnaGain"))
                                            - (
                                            ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "mutation")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "other")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaLoss")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaDown")))
                                                + ((histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaUp")))
                                            )
                                            + paddingTop;
                                    })
                                    .attr("width", studyWidth)
                                    .attr("height", function(d, i) {
                                        return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "cnaGain"));
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
                                    .attr("y", function(d, i) { return yScale(calculateFrequency(d, i, "all")) + paddingTop; })
                                    .attr("width", studyWidth)
                                    .attr("height", function(d, i) {
                                        return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "all"));
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
                                        return getStudyAbbr(d, metaData);
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

                            $("#histogram-show-colors, #histogram-sort-by, #cancerbycancer-controls input")
                                .change(function() {
                                    redrawHistogram();
                                    return true;
                               })
                            ;

                            $("#cc-select-all").click(function(e) {
                                $("#histogram-remove-notaltered").prop("checked", false);
                                e.preventDefault();
                                $("#cancerbycancer-controls input").each(function(idx, el) {
                                    $(el).prop("checked", true);
                                });
                                redrawHistogram();
                            });

                            $("#cc-select-none").click(function(e) {
                                $("#histogram-remove-notaltered").prop("checked", false);
                                e.preventDefault();
                                $("#cancerbycancer-controls input").each(function(idx, el) {
                                    $(el).prop("checked", false);
                                });
                                redrawHistogram();
                            });


                            $("#histogram-remove-notaltered").change(function() {
                                var checked = $(this).is(":checked");
                                $("#cancerbycancer-controls input").each(function(idx, el) {
                                    var altered = $(el).data("altered");

                                    if(checked && !altered) {
                                        $(el).prop("checked", false);
                                    } else if(!checked && !altered) {
                                        $(el).prop("checked", true);
                                    }
                                });

                                redrawHistogram();
                            });

			    // By default hide unaltered studies and animate this to warn user about this change
			    if( $("#histogram-remove-notaltered").trigger("click") ) {
                                setTimeout(redrawHistogram, 3000);
			    }

                            // Let's load the mutation details as well
                            var servletParams = {
                                data_priority: priority
                            };
                            var servletName = "crosscancermutation.json";
                            // init mutation data proxy with the data servlet config
                            var proxy = new MutationDataProxy(genes.join(" "));
                            proxy.initWithoutData(servletName, servletParams);

                            // init default mutation details view

	                        var el = "#mutation_details";
	                        $(el).html("");

	                        var options = {
		                        el: el,
		                        data: {
			                        geneList: proxy.getRawGeneList(),
			                        sampleList: []
		                        },
		                        proxy: {
			                        mutation: {
				                        instance: proxy
			                        }
		                        },
		                        view: {
			                        mutationDiagram: {
				                        showStats: true
			                        },
			                        mutationTable: {
				                        // TODO define custom functions where necessary
				                        columnVisibility: {
					                        "cancerStudy": "visible",
					                        // exclude tumor type for now
					                        "tumorType": "excluded"
				                        },
				                        dataTableOpts: {
					                        "sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t<"F"<"datatable-paging"pl>>',
					                        "deferRender": true,
					                        "bPaginate": true,
					                        "sPaginationType": "two_button",
					                        "bLengthChange": true,
					                        "iDisplayLength": 50,
					                        "aLengthMenu": [[5, 10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]],
					                        "oLanguage": {
						                        "sLengthMenu": "Show _MENU_ per page"
					                        }
				                        }
			                        }
		                        }
	                        };

                            var defaultMapper = MutationViewsUtil.initMutationMapper(
	                            el, // target div
	                            options, // mapper (view) options
	                            "#tabs", // main tabs (containing the mutations tab)
	                            "Mutations", // name of the mutations tab
	                            _mut3dVis);

                            // end of mutation details

                        });
                    }
                }); // Done with the histogram

                $("#customize-controls .close-customize a").click(function(e) {
                    e.preventDefault();
                    $("#customize-controls").slideToggle();
                });

                return this;
            }
        });

        var DownloadSummaryView = Backbone.View.extend({
            el: "#cc-download-text",

            render: function() {
                var studies = this.model.studies;
                var metaData = this.model.metaData;

                var dtxt = "STUDY_ABBREVIATION\tSTUDY_NAME\tNUM_OF_CASES_ALTERED\tPERCENT_CASES_ALTERED\n";
                _.each(studies, function(aStudy) {
                    dtxt += getStudyAbbr(aStudy, metaData)
                        + "\t" + metaData.cancer_studies[aStudy.studyId].name
                        + "\t" + aStudy.alterations.all
                        + "\t" + fixFloat(calculateFrequency(aStudy, 0, "all")*100, 1) + "%\n";
                });
                this.$el.text(dtxt);

                return this;
            }
        });

        var HideStudyControlView = Backbone.View.extend({
            el: "#cancerbycancer-controls",
            template: _.template($("#cc-remove-study-tmpl").html()),
            render: function() {

                var thatModel = this.model;
                var thatTmpl = this.template;
                var thatEl = this.$el;

                _.each(thatModel.studies, function(aStudy) {
                    if(aStudy.skipped) { return; }

                    thatEl.append(
                        thatTmpl({
                            studyId: aStudy.studyId,
                            name: thatModel.metaData.cancer_studies[aStudy.studyId].name,
                            checked: true,
                            altered: aStudy.alterations.all > 0
                        })
                    );
                });

                $("#show-hide-studies-toggle").click(function() {
                    $("#show-hide-studies .triangle").toggle();
                    $("#cancerbycancer-controls").slideToggle();
                });

                return this;
            }
        });

        var StudyToolTipView = Backbone.View.extend({
            template: _.template($("#study-tip-tmpl").html()),
            render: function() {
                var study = this.model.study;
                var metaData = this.model.metaData;
                var genes = this.model.genes;

                var summary = {
                    name: metaData.cancer_studies[study.studyId].name,
                    caseSetLength: study.caseSetLength,
                    // frequencies
                    allFrequency: fixFloat(calculateFrequency(study, 0, "all") * 100, 1),
                    mutationFrequency: fixFloat(calculateFrequency(study, 0, "mutation")  * 100, 1),
                    deletionFrequency: fixFloat(calculateFrequency(study, 0, "cnaDown") * 100, 1),
                    amplificationFrequency: fixFloat(calculateFrequency(study, 0, "cnaUp") * 100, 1),
                    lossFrequency: fixFloat(calculateFrequency(study, 0, "cnaLoss") * 100, 1),
                    gainFrequency: fixFloat(calculateFrequency(study, 0, "cnaGain") * 100, 1),
                    multipleFrequency: fixFloat(calculateFrequency(study, 0, "other") * 100, 1),
                    // raw counts
                    allCount: study.alterations.all,
                    mutationCount: study.alterations.mutation,
                    deletionCount: study.alterations.cnaDown,
                    amplificationCount: study.alterations.cnaUp,
                    gainCount: study.alterations.cnaGain,
                    lossCount: study.alterations.cnaLoss,
                    multipleCount: study.alterations.other,
                    // and create the link
                    studyLink: _.template($("#study-link-tmpl").html(), { study: study, genes: genes } )
                };

                this.$el.html(this.template(summary));
                this.$el.find("table.cc-tip-table tr.cc-hide").remove();
                this.$el.find("table.cc-tip-table").dataTable({
                    "sDom": 't',
                    "bJQueryUI": true,
                    "bDestroy": true,
                    "aaSorting": [[ 1, "desc" ]],
                    "aoColumns": [
                        { "bSortable": false },
                        { "bSortable": false }
                    ]
                });

	            // TODO this is a workaround to remove the sort icons,
	            // we should fix this thru the data tables API
	            this.$el.find("span.DataTables_sort_icon").remove();
	            this.$el.find("table.cc-tip-table th").removeClass("sorting_desc");

                return this;
            }
        });

        var StudiesWithNoDataView = Backbone.View.extend({
            el: "#studies-with-no-data",
            template:_.template($("#studies-with-no-data-tmpl").html()),

            render: function() {
                if(this.model.priority == 0) { return; } // no need

                var thatModel = this.model;

                if(thatModel.hiddenStudies.length > 0) {
                    this.$el.html(this.template(thatModel));
                    var ulEl = this.$el.find("#not-shown-studies");
                    _.each(thatModel.hiddenStudies, function(hiddenStudy) {
                        ulEl.append(
                            _.template($("#studies-with-no-data-item-tmpl").html(),
                            thatModel.metaData.cancer_studies[hiddenStudy.studyId])
                        );
                    });
                }
                return this;
            }
        });

        var EmptyView = Backbone.View.extend({
            el: "#crosscancer-container",
            template: _.template($("#cross-cancer-main-empty-tmpl").html()),

            render: function() {
                this.$el.html(this.template(this.model));
                return this;
            }
        });

        var CCTitleView = Backbone.View.extend({
            el: "#cctitlecontainer",
            template: _.template($("#crosscancer-title-tmpl").html()),

            render: function() {
                this.$el.html(this.template(this.model));

                // Let's bind button events
                $("#histogram-download-pdf").click(function() {
//                    var formElement = $("form.svg-to-pdf-form");
//                    formElement.find("input[name=svgelement]").val($("#cchistogram").html());
//                    formElement.submit();

	                // request download
	                cbio.util.requestDownload("svgtopdf.do",
						{filetype: "pdf",
		                    filename: "crosscancerhistogram.pdf",
		                    svgelement: $("#cchistogram").html()}
	                );
                });

                $("#histogram-download-svg").click(function() {
//                    var formElement = $("form.svg-to-file-form");
//                    formElement.find("input[name=svgelement]").val($("#cchistogram").html());
//                    formElement.submit();

	                  // request download
	                cbio.util.requestDownload("svgtopdf.do",
						{filetype: "svg",
		                    filename: "crosscancerhistogram.svg",
		                    svgelement: $("#cchistogram").html()}
	                );
                });

                $("#histogram-customize").click(function() {
                    $("#customize-controls").slideToggle();
                });

                return this;
            }
        });

        /* Models */
        var Study = Backbone.Model.extend({
            defaults: {
                studyId: "",
                caseSetId: "",
                genes: "",
                skipped: false,
                alterations: {
                    mutation: 0,
                    cnaUp: 0,
                    cnaDown: 0,
                    cnaLoss: 0,
                    cnaGain: 0,
                    other: 0,
                    all: 0
                }
            }
        });

        var Studies = Backbone.Collection.extend({
            model: Study,
            url: "crosscancerquery.json",
            defaults: {
                gene_list: "",
                data_priority: 0
            },

            initialize: function(options) {
                options = _.extend(this.defaults, options);
                this.url += "?gene_list=" + options.gene_list + "&data_priority=" + options.data_priority;

                return this;
            }
        });

        /* Routers */
        AppRouter = Backbone.Router.extend({
            routes: {
                "crosscancer/:tab/:priority/:genes": "mainView",
                "crosscancer/*actions": "emptyView"
            },

            emptyView: function(actions) {
                (new EmptyView()).render();
            },

            mainView: function(tab, priority, genes) {
                (new MainView({
                    model: {
                        tab: tab,
                        priority: priority,
                        genes: genes.replace(/_/g, "/")
                    }
                })).render();
            }
        });

        new AppRouter();
        Backbone.history.start();
    });

})(window.jQuery, window._, window.Backbone, window.d3);

