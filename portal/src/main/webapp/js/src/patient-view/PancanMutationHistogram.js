/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

// makes a Pancancer Mutation Histogram on the DOM el.
// parameters:
//      byKeywordData:                      [list of {cancer_study, cancer_type, hugo, keyword, count} ]
//      byGeneData:                         [list of {cancer_study, cancer_type, hugo, count} ]
//      cancer_study_meta_data:             [list of {cancer_study, cancer_type, num_sequenced_samples} ]
//      el:                                 DOM element
//      params:                             overrides default parameters: { margin: { top, bottom, right, left }, width, height, this_cancer_study }
//
// returns:
//      an object {el, qtip} where qtip is a function: svg ->
//      undefined, creates qtips and their corresponding `rect .mouseOver`
//      elements
//
// Gideon Dresdner <dresdnerg@cbio.mskcc.org>
// September 2013
//
function PancanMutationHistogram(byKeywordData, byGeneData, cancer_study_meta_data, el, params) {

    params = params || {};
    if (params.sparkline) {
        params = _.extend({
            margin: {top: 0, right: 0, bottom: 0, left: 0},
            width: 30,
            height: 12,
            this_cancer_study: undefined
        }, params);
    } else {
        params = _.extend({
            margin: {top: 43, right: 10, bottom: 20, left: 40},
            width: 600,
            height: 300,
            this_cancer_study: undefined
        }, params);
    }

    var cancer_study2meta_data = generate_cancer_study2datum(cancer_study_meta_data);
    var all_cancer_studies = _.keys(cancer_study2meta_data);

    // --- data munging --- //

    // copy
    var bykeyword_data = deep_copy(byKeywordData);
    var bygene_data = deep_copy(byGeneData);

    // extend
    var keyword = bykeyword_data[0].keyword;
    bykeyword_data = extend_by_zero_set(bykeyword_data)
        .map(function(d) { d.keyword = keyword; return d; });     // make sure everything has a key.  TODO: remove this extra list traversal
    bygene_data = extend_by_zero_set(bygene_data);

    var cancer_study2datum = {
        bykeyword: generate_cancer_study2datum(bykeyword_data),
        bygene: generate_cancer_study2datum(bygene_data)
    };
    
    var commonKeys = _.intersection( _.keys(cancer_study2datum.bykeyword), _.keys(cancer_study2datum.bygene) );
    bykeyword_data = [];
    bygene_data = [];
    _.each(commonKeys, function(aKey) {
	bykeyword_data.push(cancer_study2datum.bykeyword[aKey]);
        bygene_data.push(cancer_study2datum.bygene[aKey]);
    });


    if (bygene_data.length !== bykeyword_data.length) {
        throw new Error("must be same length");
    }

    if (bygene_data.length !== all_cancer_studies.length) {
        throw new Error("there must be a datum for every cancer study and visa versa");
    }

    // subtract off counts in bykeyword_data from bygene_data
    // because the counts in bygene_data include the ones in bykeyword_data
    // and we don't want to count the same thing twice.
    bygene_data.forEach(function(bygene_datum) {
        var bykeyword_datum = cancer_study2datum.bykeyword[bygene_datum.cancer_study];
        var new_count = bygene_datum.count - bykeyword_datum.count;

        if (new_count < 0) {
            throw new Error("more mutations for a particular keyword than "
                + "for all keywords of a particular gene");
        }

        bygene_datum.count = new_count;
    });
    
    var totalByGene = _.reduce(bygene_data, function(memo, datum){ return memo + datum.count; }, 0);
    var totalByKeyword = _.reduce(bykeyword_data, function(memo, datum){ return memo + datum.count; }, 0);
    var totalSequenced = _.reduce(cancer_study2meta_data, function(memo, datum){ return memo + datum.num_sequenced_samples; }, 0);

    _.mixin({
        unzip: function(array) {
            return _.zip.apply(_, array);
        }
    });

    var all_data = bykeyword_data.concat(bygene_data);
    try {
        all_data = _.chain(all_data)
            .map(compute_frequency)
            .groupBy(function(d) {
                return d.cancer_study;
            })
            .map(_.identity)    // extract groups
            .sortBy(cancer_type)
            .unzip()            // turn into layers for d3.stack
            .value();
    } catch(e) {
        throw new Error(e);
    }

    function deep_copy(list_of_objects) {
        return list_of_objects.map(_.clone);
    }

    function generate_cancer_study2datum(data) {
        return _.reduce(data, function(acc, next) {
            acc[next.cancer_study] = next;
            return acc;
        }, {});
    }

    function compute_frequency(d) {
        var num_sequenced_samples = cancer_study2meta_data[d.cancer_study].num_sequenced_samples;
        d.num_sequenced_samples = num_sequenced_samples;
        d.frequency = d.count / num_sequenced_samples;
        return d;
    }

    // takes a list of cancer studies (presumably one which contains all the
    // cancer studies for a cancer type) and returns the total frequency in
    // that list
    //
    // *signature:* `array -> number`
    function total_frequency(group) {
        var total_frequency = _.reduce(group, function(acc, next) { return acc + next.frequency }, 0);
        return -1 * total_frequency;
    }

    // returns the cancer type of a group
    // *throws* error if not all elements in the list have the same cancer type
    //
    // *signature:* `array -> string`
    function cancer_type(group) {
        var cancerType = group[0].cancer_type;
        if (!_.every(group, function(d) { return d.cancer_type === cancerType; })) {
            throw new Error("not all data in a group have the same cancer type");
        }

        return cancerType;
    }

    // add in missing cancer studies as data points with count = 0
    function zero_set(data) {
        var cancer_study2datum = generate_cancer_study2datum(data);
        // TODO: this could be optimized by referring to the `cancer_study2datum` object

        function zero_datum(cancer_study) {
            return {
                cancer_study: cancer_study,
                count: 0,
                cancer_type: cancer_study2meta_data[cancer_study].cancer_type,
                num_sequenced_samples: cancer_study2meta_data[cancer_study].num_sequenced_samples
            }
        }

        return _.chain(all_cancer_studies)
            .reduce(function(acc, study) {
                if (!_.has(cancer_study2datum, study)) {
                    // do all_cancer_studies *setminus* cancer_study2datum
                    acc.push(study);
                }
                return acc;
            }, [])
            .map(zero_datum)
            .value();
    }

    function extend_by_zero_set(data) {
        return data.concat(zero_set(data));
    }

    // --- visualization --- //

    // margin conventions http://bl.ocks.org/mbostock/3019563
    var width = params.width - params.margin.left - params.margin.left;
    var height = params.height - params.margin.top - params.margin.bottom;

    var svg = d3.select(el).append("svg")
        .attr("width", params.width)
        .attr("height", params.height)
        .append("g")
        .attr("transform", "translate(" + params.margin.left + "," + params.margin.top + ")");

    var stack = d3.layout.stack()
            .x(function(d) { return d.cancer_study; })
            .y(function(d) { return d.frequency; })
        ;

    var layers = stack(all_data);
//    console.log(layers);

    var x = d3.scale.ordinal()
        .domain(all_data[0].map(function(d) { return d.cancer_study; }))
        .rangeBands([0, width], .1);

    // sparkline y axis does not scale: will always be from 0 to 1
    var sparkline_y_threshold = .2
    var yStackMax = params.sparkline ? sparkline_y_threshold
        : d3.max(layers, function(layer) { return d3.max(layer, function(d) { return d.y0 + d.y; }); });

    var y = d3.scale.linear()
        .domain([0, yStackMax])
        .range([height, 0])
        .clamp(true)
        ;

    // --- bar chart ---

    var googleblue = "LimeGreen";
    var googlered = "Green";

    var layer = svg.selectAll(".layer")
        .data(layers)
        .enter().append("g")
        .attr("class", "layer")
        .style("fill", function(d, i) { return [googlered, googleblue][i]; });

    var rect = layer.selectAll("rect")
        .data(function(d) { return d; })
        .enter().append("rect")
        .attr("x", function(d) { return x(d.cancer_study); })
        .attr("y", function(d) { return y(d.y0 + d.y); })
        .attr("width", function(d) { return x.rangeBand(); })
        .attr("height", function(d) { return y(d.y0) - y(d.y0 + d.y); })

    // *** kill process, do nothing more ***
    if (params.sparkline) {
        return {
            el: el,
            qtip: function() { throw new Error("don't qtip a sparkline"); }
        };
    }

    // --- axises --- //

    var percent_format = d3.format(yStackMax > .1 ? ".0%" : ".1%");
    var yAxis = d3.svg.axis()
        .scale(y)
        .tickFormat(percent_format)
        .orient("left");
    yAxis.tickSize(yAxis.tickSize(), 0, 0);

    // list of element that represent the start and end of each cancer type in
    // the sorted list of cancer studies
    var study_start_ends = (function() {
        var first = all_data[0][0];

        function new_element_from_datum(d) {
            return {
                cancer_type: d.cancer_type,
                start: d.cancer_study,
                end: d.cancer_study,
                color: cancer_study2meta_data[d.cancer_study].color
            };
        }

        return _.chain(all_data[0])
            .reduce(function(acc, next) {
                var last = _.last(acc);

                // beginning of a new cancer type, create a first cancer_study
                if (last.cancer_type !== next.cancer_type) {
                    return acc.concat(new_element_from_datum(next));
                }

                // within a cancer type, continue updating the last
                // cancer_study
                if (last.cancer_type === next.cancer_type) {
                    last.end = next.cancer_study;
                    return acc;
                }

            }, [ new_element_from_datum(first) ])
            .value();
    }());

    // add the cancer type axis
    svg.selectAll('line')
        .data(study_start_ends)
        .enter()
        .append('line')
        .attr('x1', function(d) { return x(d.start); })
        .attr('x2', function(d) { return x(d.end) + x.rangeBand(); })
        .attr('y1', height + params.margin.bottom / 3)
        .attr('y2', height + params.margin.bottom / 3)
        .style('stroke-width', 5)
        .style('stroke', function(d) { return d.color; })
    ;

    // append y axis

    var yAxisEl = svg.append("g")
        .call(yAxis)
        .attr('stroke', '#000')
        .attr('shape-rendering', 'crispEdges');

    var hugo_gene_name = _.find(layers[0], function(d) { return d.hugo !== undefined; }).hugo;
    var keyword = _.find(layers[0], function(d) { return d.keyword !== undefined; }).keyword;
    
    // title
    var title_string = hugo_gene_name + " mutations across all cancer studies";
    svg.append('text')
        .text(title_string)
        .attr('x', 10)
        .attr('y', -28)
        .style("font-family", "Helvetica Neue, Helvetica, Arial, sans-serif")
        .style("font-size", "18px");

    // count
    var count_string = countText({count:totalByKeyword}, {count:totalByGene}, totalSequenced);
    svg.append('foreignobject')
        .attr('x', 10)
        .attr('y', -23)
        .attr('width',400)
        .attr('height',30)
        .append("xhtml:body")
        .style("font-family", "Helvetica Neue, Helvetica, Arial, sans-serif")
        .style("font-size", "11px")
        .html(count_string);

    // star the current cancer study if this_cancer_study is provided.
    if (!_.isUndefined(params.this_cancer_study)) {
        star_this_cancer_study();
    }

    function star_this_cancer_study() {
        var this_cancer_study_data = _.find(all_data[0], function(d) {
            return d.cancer_study === params.this_cancer_study;
        });

        var this_cancer_type;
        try {
            this_cancer_type = this_cancer_study_data.cancer_type;
        } catch(e) {
            throw new Error(e + ": could not find this the corresponding datum for this cancer study, [" + params.this_cancer_study + "]");
        }

        var find_this_cancer_studdy_datum = function(group) {
            return _.find(group, function(d) {
                return d.cancer_study === params.this_cancer_study;
            });
        };

        var this_cancer_type_group = _.zip.apply(null, all_data);
        this_cancer_type_group = _.find(this_cancer_type_group, find_this_cancer_studdy_datum);

        var total_freq = total_frequency(this_cancer_type_group);

        svg.append('text')
            .text('*')
            .attr('id', 'star')
            .attr('x', x(params.this_cancer_study))
            .attr('y', y(-1 * total_freq) + 10)
            .style("font-family", "Helvetica Neue, Helvetica, Arial, sans-serif")
            .style("font-size", (x.rangeBand()*3) + "px");
    }

    function qtip(svg) {
        var mouseOverBar = d3.select(svg).selectAll('.mouseOver')
            .data(all_cancer_studies)
            .enter()
            .append('rect')
            .attr('class', 'mouseOver')
            .attr('y', params.margin.top)
            .attr('x', function(d) {
                return x(d) + params.margin.left;
            })
            .attr('opacity', '0')
            .attr('height', height + 5)
            .attr('width', x.rangeBand())
            .on('mouseover', function() { d3.select(this).attr('opacity', '0.25'); })
            .on('mouseout', function() { d3.select(this).attr('opacity', '0'); });

        // add qtips for each bar
        mouseOverBar.each(function(d) {
            $(this).qtip({
                content: {text: 'mouseover failed'},
                position: {my:'left top', at:'center right', viewport: $(window)},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-wide' },
                hide: { fixed: true, delay: 100 },
                events: {
                    render: function(event, api) {
                        var data = getRectsByCancerStudy(d).map(function(rect) { return rect[0].__data__; });
                        var bykeyword = data.filter(function(d) { return _.has(d, "keyword"); })[0] || {};
                        var bygene = data.filter(function(d) { return !_.has(d, "keyword"); })[0] || {};
                        var cancer_study = bygene.cancer_study;     // there should always be a bygene datum
                        var total = cancer_study2meta_data[cancer_study].num_sequenced_samples;
                        var text = "<p style='font-weight:bold;'>" + cancer_study + "</p>"
                            + countText(bykeyword, bygene, total);

                        api.set('content.text', text);
                    }
                }
            });
        });
    }

    function qtip_template(d, total) {
        var count = d.count || 0;
        if (!('frequency' in d)) d.frequency = count / total;
        var percent = (d.frequency * 100).toFixed(1)+'%';
        return (_.template("<span><b>{{percent}}</b> (<b>{{count}}</b> of {{total}} sequenced samples)</span>"))({percent: percent, count: count, total: total});
    }
    
    function countText(bykeyword, bygene, total) {
        return "<p style='color: " + googlered + "; margin-bottom:0;'>"
                + keyword  + ": "  + qtip_template(bykeyword, total) + "</p>"
                + "<p style='color: " + googleblue + "; margin-top:0;'>"
                + "Other " + hugo_gene_name +  " mutations: "  + qtip_template(bygene, total) + "</p>";
    }

    function getRectsByCancerStudy(cancer_study) {
        return rect.filter(function(d) { return d.cancer_study === cancer_study; });
    }

    return {
        el: el,
        qtip: qtip
    };
};
