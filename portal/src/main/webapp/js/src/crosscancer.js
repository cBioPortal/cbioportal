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
    // Prepare eveything only if the page is ready to load
    $(function(){
        // Some semi-global utilities
        // Here are some options that we will use in this view
        var width = 1000;
        var height = 650;
        var paddingLeft = 80;
        var paddingRight = 50;
        var paddingTop = 10;
        var histBottom = 400;
        var fontFamily = "sans-serif";
        // Data N/A color
        var dataNAColor = "#e0e0e0";
        // Data Avail. color
        var dataAColor = "#000000";

        var calculateFrequency = function(d, i, type) {
            return d.alterations[type]/ d.caseSetLength;
        };

        var filterAndSortData = function(histDataOrg) {
            var histData = [];
            _.each(histDataOrg, function(study) {
                if(!study.skipped)
                    histData.push(study);
            });

            // Sort by total number of frequency
             histData.sort(function(a, b) {
                 return calculateFrequency(b, 0, "all") - calculateFrequency(a, 1, "all");
             });

            return histData;
        };


        /* Views */
        var MainView = Backbone.View.extend({
            el: "#crosscancer-container",
            template: _.template($("#cross-cancer-main-tmpl").html()),

            render: function() {
                this.$el.html(this.template(this.model));
                $("#tabs").tabs().show();
                var genes = this.model.genes;
                var priority = this.model.priority;

                var studies = new Studies({
                    gene_list: genes,
                    data_priority: priority
                });

                studies.fetch({
                    success: function() {
                        window.studies = studies;

                        $.getJSON("portal_meta_data.json", function(metaData) {
                            var histDataOrg = studies.toJSON();
                            var histData = filterAndSortData(histDataOrg);

                            var numOfStudiesHidden = histDataOrg.length - histData.length;
                            if(numOfStudiesHidden > 0) {
                                $("#cc-study-help")
                                    .show()
                                    .qtip({
                                        content: numOfStudiesHidden + " studies were excluded from this view.",
                                        show: 'mouseover',
                                        hide: {
                                            fixed:true,
                                            delay: 100
                                        }
                                    })
                                ;
                            } else {
                                $("#cc-study-help").hide();
                            }

                            var studyLocIncrements = (width - (paddingLeft + paddingRight)) / histData.length;
                            var studyWidth = studyLocIncrements * .75;
                            var verticalCirclePadding = 20;
                            // Data type radius
                            var circleDTR = studyWidth / 4;
                            // Tumor type radius
                            var circleTTR = studyWidth / 2;

                            var color = d3.scale.category20c();

                            var key = function(d) {
                                return d.studyId;
                            };

                            var yScale = d3.scale.linear()
                                .domain([
                                0,
                                Math.min(
                                    1.0,
                                    parseFloat(d3.max(histData, function (d, i) {
                                        return calculateFrequency(d, i, "all").toFixed(1);
                                    })) + .1
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

                            // These bars represents the overall changes
                            // The rest of the stacked bars should hide these bars
                            // Good for debugging (w/ yellow color)
                            /*
                            var debugBarGroup = histogram.append("g");
                            debugBarGroup.selectAll("rect")
                                .data(histData, key)
                                .enter()
                                .append("rect")
                                .attr("fill", "yellow")
                                .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
                                .attr("y", function(d, i) { return yScale(calculateFrequency(d, i, "all")) + paddingTop; })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "all"));
                                })
                                .each(function(d, i) {
                                    $(this).qtip({
                                        content: "" + calculateFrequency(d, i, "all"),
                                        show: 'mouseover',
                                        hide: {
                                            fixed:true,
                                            delay: 100
                                        }
                                    });
                                });
                            */

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
                                .transition().duration(1000)
                                .attr("class", function(d, i) { return d.studyId + " alt-mut" })
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

                            var infoBarGroup = histogram.append("g");
                            infoBarGroup.selectAll("rect")
                                .data(histData, key)
                                .enter()
                                .append("rect")
                                .attr("fill", "white")
                                .attr("x", function(d, i) { return paddingLeft + i * studyLocIncrements; } )
                                .attr("y", function(d, i) { return yScale(calculateFrequency(d, i, "all")) + paddingTop; })
                                .attr("width", studyWidth)
                                .attr("height", function(d, i) {
                                    return (histBottom-paddingTop) - yScale(calculateFrequency(d, i, "all"));
                                })
                                .style("opacity",0)
                                .attr("class", function(d, i) { return d.studyId + " alt-info" })
                                .each(function(d, i) {
                                    $(this).qtip({
                                        content: "" + calculateFrequency(d, i, "all"),
                                        show: 'mouseover',
                                        hide: {
                                            fixed:true,
                                            delay: 100
                                        }
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
                                .attr("font-size", "9px")
                            ;

                            // This is for cancer type
                            var cancerTypes = histogram.append("g");
                            cancerTypes.selectAll("circle")
                                .data(histData, key)
                                .enter()
                                .append("circle")
                                .attr("fill", function(d, i) {
                                    return color(metaData.type_of_cancers[metaData.cancer_studies[d.studyId].type_of_cancer]);
                                })
                                .attr("cx", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth/2; } )
                                .attr("cy", function(d, i) { return histBottom + verticalCirclePadding })
                                .attr("r", circleTTR)
                                .attr("class", function(d, i) { return d.studyId + " annotation-type" })
                                .each(function(d, i) {
                                    $(this).qtip({
                                        content: metaData.type_of_cancers[metaData.cancer_studies[d.studyId].type_of_cancer],
                                        show: 'mouseover',
                                        hide: {
                                            fixed:true,
                                            delay: 100
                                        }
                                    });
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
                                    $(this).qtip({
                                        content: metaData.cancer_studies[d.studyId].has_mutation_data
                                            ? "Mutation data available"
                                            : "Mutation data not available",
                                        show: 'mouseover',
                                        hide: {
                                            fixed:true,
                                            delay: 100
                                        }
                                    });
                                });


                            // This is for CNA data availability
                            var cnaGroups = histogram.append("g");
                            cnaGroups.selectAll("circle")
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
                                    $(this).qtip({
                                        content: metaData.cancer_studies[d.studyId].has_cna_data
                                            ? "CNA data available"
                                            : "CNA data not available",
                                        show: 'mouseover',
                                        hide: {
                                            fixed:true,
                                            delay: 100
                                        }
                                    });
                                });

                            var abbrGroups = histogram.append("g");
                            abbrGroups.selectAll("text")
                                .data(histData, key)
                                .enter()
                                .append("text")
                                .text(function(d, i) {
                                    return d.studyId
                                        .toLocaleUpperCase()
                                        .replace("_", " (")
                                        .concat(")")
                                        .replace(/_/g, " ")
                                    ;
                                })
                                .attr("font-family", fontFamily)
                                .attr("font-size", function() { return Math.min((studyWidth * .65), 12) + "px"; })
                                .attr("x", function(d, i) { return paddingLeft + i*studyLocIncrements + studyWidth*.75; })
                                .attr("y", function() { return histBottom + verticalCirclePadding*4 })
                                .attr("text-anchor", "end")
                                .attr("transform", function(d, i) {
                                    var xLoc = paddingLeft + i*studyLocIncrements + studyWidth*.75;
                                    var yLoc = histBottom + verticalCirclePadding*4;
                                    return "rotate(-60, " + xLoc + ", " + yLoc +  ")";
                                })
                                .attr("class", function(d, i) { return d.studyId + " annotation-abbr" })
                                .each(function(d, i) {
                                    $(this).qtip({
                                        content: metaData.cancer_studies[d.studyId].name,
                                        show: 'mouseover',
                                        hide: {
                                            fixed:true,
                                            delay: 100
                                        }
                                    });
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

                            // Now add the legends
                            var legendData = [
                                { label: "Mutation", color: "green", x: paddingLeft + 175},
                                { label: "Deletion", color: "blue", x: paddingLeft + 275},
                                { label: "Amplification", color: "red", x: paddingLeft + 375},
                                { label: "Multiple alterations", color: "#aaaaaa", x: paddingLeft + 500}
                            ];
                            var legend = histogram.append("g");
                            var legendX = paddingLeft + 100;
                            legend.selectAll("rect")
                                .data(legendData)
                                .enter()
                                .append("rect")
                                .attr('x', function(d, i) { return d.x; })
                                .attr('y', height-25)
                                .attr('width', 19)
                                .attr('height', 19)
                                .style('fill', function(d) { return d.color; })
                            ;
                            legendX = paddingLeft + 100 + 25;
                            legend.selectAll("text")
                                .data(legendData)
                                .enter()
                                .append("text")
                                .attr('x', function(d, i) { return d.x + 25; })
                                .attr('y', height-(25-19)-4)
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
                                    $(this).text( ($(this).text() * 100) + "%" );
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
                        });
                    }
                }); // Done with the histogram


                // Let's load the mutation details as well
                var servletParams = {
                    gene_list: genes,
                    data_priority: priority
                };
                var servletName = "crosscancermutation.json";
                // init mutation data proxy with the data servlet config
                var proxy = new MutationDataProxy(genes);
                proxy.initWithoutData(servletName, servletParams);
                // init default mutation details view
                var model = {
                    mutationProxy: proxy,
                    sampleArray: []
                };

                var el = "#ccmutationdetails";
                $(el).html("");

                var defaultView = new MutationDetailsView({
                    el: el,
                    model: model,
                    mut3dVis: null // nope, not yet
                });
                defaultView.render();
                // end of mutation details

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
                        genes: genes
                    }
                })).render();
            }
        });

        new AppRouter();
        Backbone.history.start();
    });

})(window.jQuery, window._, window.Backbone, window.d3);

