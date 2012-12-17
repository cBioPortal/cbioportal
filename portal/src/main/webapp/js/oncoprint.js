var Oncoprint = function(wrapper, params) {
    var that = {};

    var RECT_HEIGHT = 23;
    var LITTLE_RECT_HEIGHT = RECT_HEIGHT / 3;
    var UPREGULATED = "UPREGULATED";
    var DOWNREGULATED = "DOWNREGULATED";

    var data = params.data;
    var query = QueryGeneData(data);
    var genes_list = query.getGeneList();
    var gene_data = data.gene_data;
    var no_genes = gene_data.length;
    var samples_all = query.getSampleList();

    var translate = function(x,y) {
        return "translate(" + x + "," + y + ")";
    };

    var cleanHugo = function(hugo) {
        // can't have '/' in DOM id
        return hugo.replace("/", "_");
    };

    var label_width = (function() {
        var avg_char_width = 2;

        var list_char_no = genes_list.map(function(i) {
            return i.split("").length;
        });

        return 100 + d3.max(list_char_no) * avg_char_width;
    })();

    // global state of the oncoprint
    var state = {
        padding: true,
        width_scalar: 1,
        show_unaltered: true,
        memo_sort: true
    };

    var getVisualizedSamples = function() {
        // get state of samples
        var samples_copy = samples_all.map(function(i) { return i;});

        // todo: note that you must sort first!
        // MemoSort behaves differently when it has different lists, that is,
        // it does not deterministically deal with samples that are equal
        if (state.memo_sort) {
            samples_copy = MemoSort(data, samples_copy, genes_list).sort();
        }

        if (!state.show_unaltered) {
            samples_copy = samples_copy.filter(query.isSampleAltered);
        }

        return samples_copy;
    };

    var getRectWidth = function() {
        var unscaled = 5.5;
        return state.width_scalar * unscaled;
    };

    var getTrianglePath = function(rect_width, up) {

        var getTriangleBase = function() {
            return rect_width / 2;
        };

        var base = getTriangleBase();

        if (up) {
            return "M 0 " + LITTLE_RECT_HEIGHT + " l "  + base + " -" + LITTLE_RECT_HEIGHT
                + " l " + base + " " + LITTLE_RECT_HEIGHT + " l 0 0";
//                return "M 0 7 l 2.75 -7 l 2.75 7 l 0 0";
        }
//        return "M 0 16 l " + base + " 7 l " + base + " -7 l 0 0";
        return "M 0 15 l " + base + " " + LITTLE_RECT_HEIGHT + " l "
            + base + " -" + LITTLE_RECT_HEIGHT + " l 0 0";
//            return "M 0 16 l 2.75 7 l 2.75 -7 l 0 0";
    };

    var getRectPadding = function() {
        var unscaled = 3;
        return state.padding ? (state.width_scalar * unscaled) : 0;
    };

    var getXScale = function(no_samples) {
        return (getRectWidth() + getRectPadding()) * no_samples;
    };

    var getHeight = function() {
        return (RECT_HEIGHT + 7) * no_genes;
    };

    // scales
    var x = d3.scale.ordinal().rangeBands([0, getXScale(samples_all.length)], 0);

    var y = d3.scale.ordinal().rangeBands([0, getHeight()], 0)
        .domain(genes_list);

    that.getData = function() {
        return params.data;
    };

    var redraw = function(samples_visualized, track, hugo) {
        var join_with_hugo = samples_visualized.map(function(i) {
            return {
                sample: i,
                hugo: hugo
            };
        });

        var sample = track.selectAll('.sample')
            .data(join_with_hugo, function(d) { return d.sample;});

        // update
        sample.transition()
            .duration(500)
            .attr('transform', function(d) {
                return translate(x(d.sample), y(hugo));
            });

        // enter
        var sample_enter = sample.enter()
            .append('g')
                .attr('class', 'sample')
                .attr('transform', function(d) {
                    return translate(x(d.sample), y(hugo));
                });

        var rect_width = getRectWidth();

        var cna = sample_enter.append('rect')
            .attr('class', function(d) {
                var cna = query.data(d.sample, hugo, 'cna');
                return 'cna ' + (cna === null ? 'none' : cna);
            })
            .attr('width', rect_width)
            .attr('height', RECT_HEIGHT);

        var mrna = sample_enter.append('rect')
            .attr('class', function(d) {
                var mrna = query.data(d.sample, hugo, 'mrna');
                return 'mrna ' + (mrna === null ? 'none' : mrna);
            })
            .attr('width', rect_width)
            .attr('height', RECT_HEIGHT);

        // remove all the null mrna squares
        mrna.filter(function(d) {
            var mrna = query.data(d.sample, hugo, 'mrna');
            return mrna === null;
        }).remove();

        var mut = sample_enter.append('rect')
            .attr('class', function(d) {
                var mutation = query.data(d.sample, hugo, 'mutation');
                return 'mutation ' + (mutation === null ? 'none' : 'mut');
            })
//            .attr('x', -1)
            .attr('y', LITTLE_RECT_HEIGHT)
            .attr('width', rect_width)
//            .attr('width', mutation_width)
            .attr('height', LITTLE_RECT_HEIGHT);

        // remove all the null mutation squares
        mut.filter(function(d) {
            var mutation = query.data(d.sample, hugo, 'mutation');
            return mutation === null;
        }).remove();

        var up_triangle = getTrianglePath(rect_width, true);
        var down_triangle = getTrianglePath(rect_width, false);

        var rppa = sample_enter.append('path')
            .attr('class', function(d) {
                if (query.data(d.sample, hugo, 'cna') === null) {
                    return 'rppa dark';
                }
                return 'rppa light';
            })
            .attr('d', function(d) {
                var rppa = query.data(d.sample, hugo, 'rppa');

                if (rppa === UPREGULATED) {
                    return up_triangle;
                }
                if (rppa === DOWNREGULATED) {
                    return down_triangle;
                }
                if (rppa === null) {
                    return 'M 0 0';
                }
            });

        rppa.filter(function(d) {
            var rppa = query.data(d.sample, hugo, 'rppa');

            return rppa === null;
        }).remove();

        // exit
        var sample_exit = sample.exit().remove();
    };

    var svg;        // global scope
    that.getSvg = function() { return svg; };

    // ** icing on the cake functions **
    var visKeySetup = function() {
        // hide/show keys for relevant data types
        var data_types = query.data_types;

        d3.select('#oncoprint_key').style('padding-left', (label_width + getRectWidth()) + "px");

        $('#oncoprint_key').children().each(function(i, el) {
            if(data_types.indexOf($(el).attr('id')) === -1) {
                $(el).hide();
            } else {
                $(el).show();
            }
        });
    };

    var makeQtip = function() {
        var formatMutation = function(sample, hugo) {
            // helper function
            var mutation = query.data(sample, hugo, 'mutation');

            if (mutation !== null) {
                return "Mutation: <b>" + mutation + "</b><br/>";
            }
            return "";
        };

        var patientViewUrl = function(sample_id) {
            // helper function
            var href = "http://localhost:8080/public-portal/tumormap.do?case_id=" + sample_id
                + "&cancer_study_id=" + params.cancer_study_id;

            return "<a href='" + href + "'>" + sample_id + "</a>";
        };

        // make qtip
        d3.selectAll('.sample').each(function(d, i) {
            $(this).qtip({
                content: {text: '<font size="2">'
                    + formatMutation(d.sample, d.hugo)
                    + patientViewUrl(d.sample)
                    + '</font>'},

                hide: { fixed: true, delay: 100 },
                style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                position: {my:'top center',at:'bottom center'}
            });
        });
    };

    var widthScrollerSetup = function() {
        $('<div>', { id: "width_slider", width: "100"})
            .slider({
                text: "Adjust Width ",
                min: .1,
                max: 1,
                step: .01,
                value: 1,
                change: function(event, ui) {
//                    console.log(ui.value);
                    oncoprint.scaleWidth(ui.value);
                }
            }).appendTo($('#oncoprint_controls #width_scroller'));
    };

    that.draw = function() {

        var $header = $('<div>', {id:'oncoprint_header'});
        $(wrapper).append($header);

        $header.append(
            '<p>Case Set: ' + params.case_set_str + '</p></div>'
                + '<p>Altered in ' + query.altered_samples.length + ' (' + d3.format("%")(query.percent_altered) + ')'
                + ' of cases</p></div>');

        var visualized_samples = getVisualizedSamples();

        x.domain(visualized_samples);

//        var table_wrap = d3.select(wrapper).insert('table', ':first-child').append('tr');
        var table_wrap = d3.select(wrapper).append('table').append('tr');

        var label_svg = table_wrap.insert('td').insert('svg', ':first-child')
            .attr('id', "oncoprint_label")
            .attr('width', label_width)
            .attr('height', getHeight());

        var body_wrap = table_wrap.append('td').append('div')
            .style('width', (1200 - label_width) + 'px')
            .style('display', 'inline-block')
            .style('overflow-x', 'auto')
            .style('overflow-y', 'hidden');

        svg = body_wrap.append('svg')
            .attr('width', getXScale(samples_all.length))
            .attr('height', getHeight());

        gene_data.forEach(function(gene_obj) {

            var hugo = gene_obj.hugo;
            var cleaned_hugo = cleanHugo(hugo);

            var track = svg.append('g')
                .attr('class', 'track');

            var label = label_svg.append('text')
                .attr('position', 'static')
                .attr('left', 0)
                .attr('x', 0)
                .attr('y', y(hugo) + .75 * RECT_HEIGHT);

            label.append('tspan')
                .attr('text-anchor', 'start')
                .text(gene_obj.hugo);

            label.append('tspan')
                .attr('text-anchor', 'end')
                .attr('x', label_width)
                .text(gene_obj.percent_altered);

            if (params.vis_key) {       // toggle the key to the visualization
                visKeySetup();
            }

            redraw(visualized_samples, track, hugo);
        });

        makeQtip();

        if (params.customize) {         // toggle the setup of the customization controls
            widthScrollerSetup();
        }
    };

    var transition = function() {
        // helper function

        var samples_visualized = getVisualizedSamples();
        var no_samples = samples_visualized.length;

        x.domain(samples_visualized);
        x.rangeBands([0, getXScale(no_samples)]);

        svg.selectAll('.track')[0].forEach(function(val, i) {

            var hugo = genes_list[i];

            var transition = d3.select(val).transition();

            transition.selectAll('.sample')
                .transition()
                .duration(1000)
                .attr('transform', function(d) {
                    return translate(x(d.sample), y(hugo));
                });

            var rect_width = getRectWidth();
            transition.selectAll('rect')
                .transition()
                .duration(1000)
                .attr('width', rect_width);

            var up_triangle = getTrianglePath(rect_width, true);
            var down_triangle = getTrianglePath(rect_width, false);
            transition.selectAll('.rppa')
                .transition()
                .duration(1000)
                .attr('d', function(d) {
                    var rppa = query.data(d.sample, hugo, 'rppa');

                    if (rppa === UPREGULATED) {
                        return up_triangle;
                    }
                    if (rppa === DOWNREGULATED) {
                        return down_triangle;
                    }
                });
        });

        svg.transition().duration(1000).style('width', getXScale(no_samples));
    };

    that.memoSort = function() {

        if (state.memo_sort) {
            return;
        }

        state.memo_sort = true;

        transition();
    };

    that.defaultSort = function() {

        if (!state.memo_sort) {
            return;
        }

        state.memo_sort = false;

        transition();
    };

    that.toggleWhiteSpace = function() {
        state.padding = !state.padding;

        transition();
    };

    that.scaleWidth = function(scalar) {
        state.width_scalar = scalar;
        transition();
    };

    that.toggleUnaltered = function() {
        state.show_unaltered = !state.show_unaltered;

        var samples_visualized = getVisualizedSamples();

        gene_data.forEach(function(gene, i) {
            var track = d3.select(d3.select(wrapper).selectAll('.track')[0][i]);
            redraw(samples_visualized, track, gene.hugo);
            transition();
        });
    };
    return that;
};
