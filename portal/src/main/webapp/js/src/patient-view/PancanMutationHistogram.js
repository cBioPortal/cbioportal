define(function() {
    return function(bykeyword_data, bygene_data, cancer_study2num_sequenced_samples, el, params) {

        params = $.extend({
            margin: {top: 20, right: 10, bottom: 20, left: 40},
            width: 840,
            height: 400,
            cancerStudyName: undefined
        }, params);

        // --- data munging --- //

        // make sure there is bykeyword data for every bygene data, i.e. for
        // every cancer study.  Ones that are missing from bykeyword data are
        // just given a count of zero.
        bykeyword_data = bykeyword_data.concat((function() {
            var bykeyword_keys = d3.set(bykeyword_data.map(keygen));

            var bygeneData_setminus_byKeywordData = bygene_data.filter(function(d) {
                var key = keygen(d);
                return !bykeyword_keys.has(key);
            });

            bygeneData_setminus_byKeywordData.forEach(function(d) { d.count = 0; });  // count = 0 in those cancer studies

            return bygeneData_setminus_byKeywordData;
        })());

        if (bygene_data.length !== bykeyword_data.length) {
            throw new Error("must be same length");
        }

        var getKeywordDataImpl = getKeywordData(bykeyword_data);

        // the counts in bygene_data include the counts in bykeyword_data, so
        // we are going to subtract them off
        bygene_data.forEach(function(d) {
            var keyword_data = getKeywordDataImpl(d);

            if (keyword_data === undefined) {
                console.log(d);
            }

            var subtract_off = keyword_data.count;

            var new_count = d.count - subtract_off;

            if (new_count < 0) {
                throw new Error("more mutations for a particular keyword than"
                    + "for all keywords linked to gene");
            }

            d.count = new_count;
        });

        // avoid mixing in to the global `_` object
        var underscore = _.noConflict();
        underscore.mixin({
            unzip: function(array) {
                return underscore.zip.apply(underscore, array);
            }
        });

        var all_data = bykeyword_data.concat(bygene_data);
        all_data = underscore.chain(all_data)
            .map(function(d) {
                // compute frequences
                var num_sequenced_samples = cancer_study2num_sequenced_samples[d.cancer_study];
                d.num_sequenced_samples = num_sequenced_samples;
                d.frequency = d.count / num_sequenced_samples;
                return d;
            })
            .groupBy(function(d) {
                // group the data by cancer study, sort by total count (total height of
                // bar), and then unzip to create two separate layers
                return d.cancer_study;
            })
            .map(underscore.identity)    // extract groups
            .sortBy(function(grp) {
                var total_frequency
                    = underscore.reduce(grp, function(acc, next) { return acc + next.frequency }, 0);
                return -1 * total_frequency;
            })
            .unzip()        // turn into layers for d3.stack
            .value();

        // *signature:* `{hugo, cancer_study} -> string`
        // throws an Error
        function keygen(d) {
            var hugo = d.hugo;
            var cancer_study = d.cancer_study;

            if (hugo === undefined || cancer_study === undefined) {
                throw new Error("cannot generate key with undefined field(s)");
            }

            return hugo + " " + cancer_study;
        }

        // *signature:* `keyword_data -> ( {hugo, cancer_study} -> keyword datum )`
        function getKeywordData(bykeyword_data) {
            return (function(d) {

                // key -> bykeyword datum
                var map = bykeyword_data.reduce(function(acc, next) {
                    var key = keygen(next);

                    acc[key] = next;
                    return acc;
                }, {});

                var key;
                try { key = keygen(d); } catch(e) { throw new Error(e); }

                var keyword_data = map[key];

                return keyword_data;
            });
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
        console.log(layers);

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

        var yAxis = d3.svg.axis()
            .scale(y)
            .tickFormat(d3.format("%.0"))
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

        // colors for each bar by cancer_type
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
            .on("mouseover", function() { d3.select(this).attr('opacity', '0.5'); })
            .on("mouseout", function() { d3.select(this).attr('opacity', '1'); });

        // add qtips for each bar
        svg.selectAll('rect').each(function(d) {
            $(this).qtip({
                content: {text: 'mouseover failed'},
                position: {my:'left bottom', at:'top right'},
                style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                hide: { fixed: true, delay: 100 },
                events: {
                    render: function(event, api) {
                        api.set('content.text',
                            "<b>" + d.count + "/" + d.num_sequenced_samples + "</b>"
                             + "<br/>" + d.cancer_study);
                    }
                }
            });
        });

        //// find the cancer type form the cancer study name
        ////var this_cancer_study = data.filter(function(d) {
        ////    return d.cancer_study === params.cancerStudyName;
        ////});

        ////if (this_cancer_study.length !== 1) {
        ////    throw new Error(progname + ": multiple cancer studies found that have indentical names to this one");
        ////}

        ////var this_cancer_type = this_cancer_study[0].cancer_type;

        ////// add pointer triangle that points to this cancer study
        ////var triangleXcoordinate = (x(params.cancerStudyName) + x.rangeBand() * .5);
        ////
        ////if (!params.cancerStudyName) {
        ////    console.log("did not provide a cancerStudyName to " + progname);
        ////    triangleXcoordinate = -10 * x.rangeBand(); // hide it outside the svg element
        ////}
        ////
        ////svg.append('path')
        ////    .attr('transform', 'translate('
        ////                + triangleXcoordinate
        ////                + ',' + (height + 15) + ')')
        ////    .attr('d', d3.svg.symbol().type('triangle-up'))
        ////    .attr('fill', googlered)
        ////    ;

        //var keyword_bars = svg.selectAll(".keyword-bar")
        //    .data(bykeyword_data)
        //    .enter().insert("rect")
        //    .attr('class', 'keyword-bar')
        //    .attr("x", function(d) { return x(d.cancer_study); })
        //    .attr("y", function(d) { return y(d.frequency); })
        //    .attr("width", x.rangeBand())
        //    .attr("height", function(d) { return height - y(d.frequency); })
        //    .attr('fill', function(d) {
        //        return googleblue;
        //        //return d.cancer_type === this_cancer_type ? googlered : googleblue;
        //    })
        //    .on("mouseover", function() { d3.select(this).attr('opacity', '0.5'); })
        //    .on("mouseout", function() { d3.select(this).attr('opacity', '1'); });

        //var gene_bars = svg.selectAll(".gene-bar")
        //    .data(bygene_data)
        //    .enter().insert("rect")
        //    .attr('class', 'gene-bar')
        //    .attr("x", function(d) { return x(d.cancer_study); })
        //    .attr("y", function(d) {
        //        var keyword_data = getKeywordData(d);
        //        return y(d.frequency + (keyword_data ? keyword_data.frequency : 0))
        //    })
        //    .attr("width", x.rangeBand())
        //    .attr("height", function(d) { return height - y(d.frequency); })
        //    .attr('fill', function(d) {
        //        return googleblue;
        //        //return d.cancer_type === this_cancer_type ? googlered : googleblue;
        //    })
        //    .on("mouseover", function() { d3.select(this).attr('opacity', '0.5'); })
        //    .on("mouseout", function() { d3.select(this).attr('opacity', '1'); })
        //    ;
    };
});
