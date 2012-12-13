var Oncoprint = function(wrapper, params) {
    var that = {};

    // constants
    var RECT_HEIGHT = 23;
    var LITTLE_RECT_HEIGHT = RECT_HEIGHT / 3;
//    var LABEL_PADDING = 130;


    var UPREGULATED = "UPREGULATED";
    var DOWNREGULATED = "DOWNREGULATED";

    // useful variables
    var data = params.data;
    var query = QueryGeneData(data);
    var genes_list = query.getGeneList();
    var gene_data = data.gene_data;
    var no_genes = gene_data.length;
    var samples_all = query.getSampleList()

    // useful functions
    var translate = function(x,y) {
        return "translate(" + x + "," + y + ")";
    };

    // todo: am i using this?  what happens when you submit MIR_##/##?
    var cleanHugo = function(hugo) {
        // can't have '/' in DOM id
        return hugo.replace("/", "_");
    };

    var LABEL_PADDING = (function() {
        var avg_char_width = 13;

        var list_char_no = genes_list.map(function(i) {
            return i.split("").length;
        });

        return d3.max(list_char_no) * avg_char_width;
    })();

    // global state of the oncoprint
    var state = {
        padding: true,
        width_scalar: 1,
        show_unaltered: true,
        memo_sort: false
    };

    // functions that get state

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

    var getTriangleBase = function() {
        return getRectWidth() / 2;
    };

    var getTrianglePath = function(up) {
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

    var getWidth = function(no_samples) {
        return getXScale(no_samples) + LABEL_PADDING + (2 * (getRectWidth() + getRectPadding()));
    };

    var getHeight = function() {
        return (RECT_HEIGHT + 7) * no_genes;
    };

    // scales
    var x = d3.scale.ordinal().rangeBands([0, getXScale(samples_all.length)], 0);

    var y = d3.scale.ordinal().rangeBands([0, getHeight()], 0)
        .domain(genes_list);

    d3.select(wrapper)
        .style('width', '1300px')
        .style('overflow-x', 'auto')
        .style('overflow-y', 'hidden');

    // functions that echo
    that.wrapper = wrapper;

    that.getData = function() {
        return params.data;
    };

    var redraw = function(samples_visualized, track, hugo) {
        var sample = track.selectAll('.sample')
            .data(samples_visualized, function(d) { return d;});

        // enter
        var sample_enter = sample.enter().append('g')
            .attr('class', 'sample')
            .attr('transform', function(d) {
                return translate(x(d), y(hugo));
            });

        var width = getRectWidth();

        var cna = sample_enter.append('rect')
            .attr('class', function(d) {
                var cna = query.data(d, hugo, 'cna');
                return 'cna ' + (cna === null ? 'none' : cna);
            })
            .attr('width', width)
            .attr('height', RECT_HEIGHT);

        var mrna = sample_enter.append('rect')
            .attr('class', function(d) {
                var mrna = query.data(d, hugo, 'mrna');
                return 'mrna ' + (mrna === null ? 'none' : mrna);
            })
            .attr('width', width)
            .attr('height', RECT_HEIGHT);

        // remove all the null mrna squares
        mrna.filter(function(d) {
            var mrna = query.data(d, hugo, 'mrna');
            return mrna === null;
        }).remove();

//        var mutation_width = width + 2;
        var mut = sample_enter.append('rect')
            .attr('class', function(d) {
                var mutation = query.data(d, hugo, 'mutation');
                return 'mutation ' + (mutation === null ? 'none' : 'mut');
            })
//            .attr('x', -1)
            .attr('y', LITTLE_RECT_HEIGHT)
            .attr('width', width)
//            .attr('width', mutation_width)
            .attr('height', LITTLE_RECT_HEIGHT);

        // remove all the null mutation squares
        mut.filter(function(d) {
            var mutation = query.data(d, hugo, 'mutation');
            return mutation === null;
        }).remove();

        var up_triangle = getTrianglePath(true);
        var down_triangle = getTrianglePath(false);

        var rppa = sample_enter.append('path')
            .attr('class', 'rppa')
            .attr('d', function(d) {
                var rppa = query.data(d, hugo, 'rppa');

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
            var rppa = query.data(d, hugo, 'rppa');

            return rppa === null;
        }).remove();

        // exit
        var sample_exit = sample.exit()
//            .transition()
//            .duration(750)
//            .attr('transform', function(d) {
//                return translate(3000, y(hugo));
//            })
            .remove();
    };

    /*
     * For the given oncoprint reference, returns the SVG Dom as string
     * for the body canvas.
     *
     * oncoprint - opaque reference to oncoprint system
     *
     */
//    var svg;
//    function GetOncoPrintBodyXML(svg) {
//
//        // outta here
//        return (new XMLSerializer()).serializeToString(svg);
//    }
//
//    var doXml = function() {
//        console.log(this);
////        this.elements['xml'].value=GetOncoPrintBodyXML(svg);
//        return true;
//    };

    that.draw = function() {

        svg = d3.select(wrapper).append('svg')
//        svg = d3.select(wrapper).insert('svg', ":first-child")
            .attr('width', getWidth(samples_all.length))
            .attr('height', getHeight());

        that.getSvg = function() { return svg; };

//        var svg_ify = $('<form>', {
//            action: "oncoprint_converter.svg",
//            enctype: "multipart/form-data",
//            method: "POST",
//            onsubmit: doXml,
//            target: "blank"
//        }).append("<span>Get OncoPrint</span><input type=\"submit\" value=\"SVG\"/>");
//        $(wrapper).append(svg_ify);

        $(wrapper).prepend('<div><h4>OncoPrint</h4></br>'
            + "<h4 onclick='$(\"#oncoprint_controls\").toggle();'>customize</h4>"
            + '<div id="oncoprint_controls" style="display:show;">'
            + "<button type='button' onclick='oncoprint.defaultSort()'>default sort</button>"
            +    "<button type='button' onclick='oncoprint.memoSort()'>memo sort</button>"
            +    "<button type='button' onclick='oncoprint.toggleWhiteSpace()'>toggle white space</button>"
            +    "<button type='button' onclick='oncoprint.scaleWidth(3)'>scale width 3</button>"
            +    "<button type='button' onclick='oncoprint.scaleWidth(1.5)'>scale width 1.5</button>"
            +    "<button type='button' onclick='oncoprint.scaleWidth(1)'>scale width 1</button>"
            +    "<button type='button' onclick='oncoprint.scaleWidth(.5)'>scale width .5</button>"
            + '</div>'
            + '<p>Case Set: ' + params.case_set_str + '</p></div>'
            + '<p>Altered in ' + query.altered_samples.length + ' (' + d3.format("%")(query.percent_altered)
                + ') of cases</p></div>');

        x.domain(samples_all);

        gene_data.forEach(function(gene_obj) {

            var hugo = gene_obj.hugo;
            var cleaned_hugo = cleanHugo(hugo);

            // N.B. there is no data bound to g,
            // is this bad form?
            var track = svg.append('g')
                .attr('transform', translate(LABEL_PADDING, 0))
                .attr('class', 'track');

            var label = track.append('text')
                .attr('position', 'static')
                .attr('left', 0)
                .attr('x', -LABEL_PADDING)
                .attr('y', y(hugo) + .75 * RECT_HEIGHT);

            label.append('tspan')
                .attr('text-anchor', 'start')
                .text(gene_obj.hugo);

            label.append('tspan')
                .attr('text-anchor', 'end')
                .attr('x', 0 - 5)
                .text(gene_obj.percent_altered);

            toggleKey();

            redraw(samples_all, track, hugo);

        });
    };

    var toggleKey = function() {
        // show/hide the keys for the relevant data types
        var data_types = query.data_types;

        d3.select('#oncoprint_key').style('padding-left', LABEL_PADDING + "px");

        $('#oncoprint_key').children().each(function(i, el) {
            if(data_types.indexOf($(el).attr('id')) === -1) {
                $(el).hide();
            } else {
                $(el).show();
            }
        });
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
                    return translate(x(d), y(hugo));
                });

            var rect_width = getRectWidth();
            transition.selectAll('rect')
                .transition()
                .duration(1000)
                .attr('width', rect_width);

            var up_triangle = getTrianglePath(true);
            var down_triangle = getTrianglePath(false);
            transition.selectAll('.rppa')
                .transition()
                .duration(1000)
                .attr('d', function(d) {
                    var rppa = query.data(d, hugo, 'rppa');

                    if (rppa === UPREGULATED) {
                        return up_triangle;
                    }
                    if (rppa === DOWNREGULATED) {
                        return down_triangle;
                    }
                });
        });

        svg.transition().duration(1000).style('width', getWidth(no_samples));
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
