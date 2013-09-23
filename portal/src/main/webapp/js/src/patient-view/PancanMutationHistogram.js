// makes a Pancancer Mutation Histogram on the DOM el.
// parameters:
//      byKeywordData:                      [list of {cancer_study, cancer_type, hugo, keyword, count} ]
//      byGeneData:                         [list of {cancer_study, cancer_type, hugo, count} ]
//      cancer_study_meta_data:             [list of {cancer_study, cancer_type, num_sequenced_samples} ]
//      el:                                 DOM element
//      params:                             overrides default parameters: { margin: { top, bottom, right, left }, width, height, cancerStudyName (of this study) }
//
// returns:
//      an object {el, qtip} where qtip is a function: svg ->
//      undefined, creates qtips and their corresponding `rect .mouseOver`
//      elements
//
// Gideon Dresdner <dresdnerg@cbio.mskcc.org>
// September 2013
function PancanMutationHistogram(byKeywordData, byGeneData, cancer_study_meta_data, el, params) {
    params = $.extend({
        margin: {top: 20, right: 10, bottom: 20, left: 40},
        width: 840,
        height: 400,
        cancerStudyName: undefined
    }, params);

    var cancer_study2meta_data = generate_cancer_study2datum(cancer_study_meta_data);
    var all_cancer_studies = _.keys(cancer_study2meta_data);

    // --- data munging --- //

    // copy
    var bykeyword_data = deep_copy(byKeywordData);
    var bygene_data = deep_copy(byGeneData);

    // extend
    var keyword = bykeyword_data[0];
    bykeyword_data = extend_by_zero_set(bykeyword_data)
        .map(function(d) { d.keyword = keyword; return d; });     // make sure everything has a key.  TODO: remove this extra list traversal
    bygene_data = extend_by_zero_set(bygene_data);

    var cancer_study2datum = {
        bykeyword: generate_cancer_study2datum(bykeyword_data),
        bygene: generate_cancer_study2datum(bygene_data)
    };

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
        .rangeRoundBands([0, width], .1)
        .domain(all_data[0].map(function(d) { return d.cancer_study; }));

    yStackMax = d3.max(layers, function(layer) { return d3.max(layer, function(d) { return d.y0 + d.y; }); });
    var y = d3.scale.linear()
        .domain([0, yStackMax])
        .range([height, 0]);

    // axises

    var xAxis = d3.svg.axis()
        .scale(x)
        .tickFormat("")
        .orient("bottom");

    var percent_format = d3.format("%.0");
    var yAxis = d3.svg.axis()
        .scale(y)
        .tickFormat(percent_format)
        .orient("left");
    yAxis.tickSize(yAxis.tickSize(), 0, 0);

    // append axises

    var xAxisEl = svg.append("g")
        .attr("transform", "translate(0," + height + ")")
        .attr('id', 'x-axis')
        .call(xAxis);

    // apply css to xAxis

    xAxisEl.attr('fill', 'none')
        .attr('stroke', '#000')
        .attr('shape-rendering', 'crispEdges');

    var yAxisEl = svg.append("g")
        .call(yAxis)
        .attr('stroke', '#000')
        .attr('shape-rendering', 'crispEdges');

    var googleblue = "#3366cc";
    var googlered = "#dc3912";

    // bar chart
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
        .attr("width", x.rangeBand())
        .attr("height", function(d) { return y(d.y0) - y(d.y0 + d.y); })

    // title
    //var hugo_gene_name = _.find(layers[0], function(d) { return d.hugo !== undefined; }).hugo;
    //var title_string = hugo_gene_name + " mutations across all cancer studies in the cBioPortal";
    //svg.append('text')
    //    .text(title_string)
    //    .attr('x', .35 * d3.max(x.range()))
    //    .attr('y', .15 * d3.max(y.range()))
    //    .style("font-family", "Helvetica Neue, Helvetica, Arial, sans-serif")
    //    .style("font-size", "18px")

    function qtip(svg) {
        var mouseOverBar = d3.select(svg).selectAll('.mouseOver')
            .data(all_cancer_studies)
            .enter()
            .append('rect')
            .attr('class', 'mouseOver')
            .attr('x', function(d) {
                return x(d) + params.margin.left;
            })
            .attr('opacity', '0')
            .attr('height', height + params.margin.top + 5)
            .attr('width', x.rangeBand())
            .on('mouseover', function() { d3.select(this).attr('opacity', '0.25'); })
            .on('mouseout', function() { d3.select(this).attr('opacity', '0'); });

        function qtip_template(d) {
            var percent = percent_format(d.frequency || 0);
            var count = d.count || 0;
            var metadata = cancer_study2meta_data[d.cancer_study];
            var total = metadata.num_sequenced_samples;

            return (_.template("<span>{{percent}} ({{count}} / {{total}})</span>"))({percent: percent, count: count, total: total});
        }

        function repeat_string(str, n) {
            var repeated_str = "";
            for (var i = 0; i < n; i++) {
                repeated_str+=str;
            }
            return repeated_str;
        }

        // add qtips for each bar
        mouseOverBar.each(function(d) {
            $(this).qtip({
                content: {text: 'mouseover failed'},
                position: {my:'left top', at:'center right'},
                style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                hide: { fixed: true, delay: 100 },
                events: {
                    render: function(event, api) {
                        var data = getRectsByCancerStudy(d).map(function(rect) { return rect[0].__data__; });
                        var bykeyword = data.filter(function(d) { return _.has(d, "keyword"); })[0] || {};
                        var bygene = data.filter(function(d) { return !_.has(d, "keyword"); })[0] || {};
                        var cancer_study = bygene.cancer_study;     // there should always be a bygene datum
                        var text = "<p style='font-weight:bold;'>" + cancer_study + "</p>"
                            + "<p style='color: " + googleblue + "; margin-bottom:0;'>non truncating:" + repeat_string("&nbsp;", 1) + qtip_template(bygene) + "</p>"
                            + "<p style='color: " + googlered + "; margin-top:0;'>truncating:" + repeat_string("&nbsp;", 7) + qtip_template(bykeyword) + "</p>"
                        ;

                        api.set('content.text', text);
                    }
                }
            });
        });
    }

    function getRectsByCancerStudy(cancer_study) {
        return rect.filter(function(d) { return d.cancer_study === cancer_study; });
    }

    return {
        el: el,
        qtip: qtip
    };
};
