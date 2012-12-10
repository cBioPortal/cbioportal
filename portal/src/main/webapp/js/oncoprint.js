var Oncoprint = function(wrapper, params) {
    var that = {};

    // constants
    var RECT_HEIGHT = 23;
    var LITTLE_RECT_HEIGHT = RECT_HEIGHT / 3;
    var LABEL_PADDING = 130;

    // useful variables
    var data = params.data;
    var query = QueryGeneData(data);
    var genes_list = query.getGeneList();
    var gene_data = data.gene_data;
    var no_genes = gene_data.length;
    var samples_all = query.getSampleList();

    // useful functions
    var translate = function(x,y) {
        return "translate(" + x + "," + y + ")";
    };

    var cleanHugo = function(hugo) {
        // can't have '/' in DOM id
        return hugo.replace("/", "_");
    };

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
        return (RECT_HEIGHT + 10) * no_genes;
    };

    // scales
    var x = d3.scale.ordinal().rangeBands([0, getXScale(samples_all.length)], 0);

    var y = d3.scale.ordinal().rangeBands([0, getHeight()], 0)
        .domain(genes_list);


    // functions that echo
    d3.select(wrapper)
        .style('width', '1300px')
        .style('overflow-x', 'auto')
        .style('overflow-y', 'hidden');

    that.wrapper = wrapper;

    that.getData = function() {
        return params.data;
    };

    var draw = function(samples_visualized, track, hugo) {
        var sample = track.selectAll('.sample')
            .data(samples_visualized, function(d) { return d;});

        // enter
        var sample_enter = sample.enter().append('g')
            .attr('class', 'sample')
            .attr('transform', function(d) {
                return translate(x(d), y(hugo));
            });

        var cna = sample_enter.append('rect')
            .attr('class', function(d) {
                var cna = query.data(d, hugo, 'cna');
                return 'cna ' + (cna === null ? 'none' : cna);
            })
            .attr('width', getRectWidth())
            .attr('height', RECT_HEIGHT);

        var mut = sample_enter.append('rect')
            .attr('class', function(d) {
                var mutation = query.data(d, hugo, 'mutation');
                return 'mutation ' + (mutation === null ? 'none' : 'mut');
            })
            .attr('width', getRectWidth())
            .attr('height', LITTLE_RECT_HEIGHT);

//        // ... mrna, rppa

        // exit
        var sample_exit = sample.exit()
//            .transition()
//            .duration(750)
//            .attr('transform', function(d) {
//                return translate(3000, y(hugo));
//            })
            .remove();
    };

    that.redraw = function() {

        var svg = d3.select(wrapper).append('svg')
            .attr('width', getWidth(samples_all.length))
            .attr('height', getHeight());
        that.svg = svg;

        // name : name of track, e.g. hugo gene symbol (PTEN), or clinical data type, etc

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
                .attr('y', y(hugo) + .5 * RECT_HEIGHT);

            label.append('tspan')
                .attr('text-anchor', 'start')
                .text(gene_obj.hugo);

            label.append('tspan')
                .attr('text-anchor', 'end')
                .attr('x', 0 - 5)
                .text(gene_obj.percent_altered);

            draw(samples_all, track, hugo);
        });
    };

    var transition = function() {
        // helper function

        var samples_visualized = getVisualizedSamples();
        var no_samples = samples_visualized.length;

        x.domain(samples_visualized);
        x.rangeBands([0, getXScale(no_samples)]);

        that.svg.selectAll('.track')[0].forEach(function(val, i) {
            d3.select(val).selectAll('.sample')
                .transition()
                .duration(1000)
                .attr('transform', function(d) {
                    return translate(x(d), y(genes_list[i]));
                });

            var rect_width = getRectWidth();
            that.svg.selectAll('rect')
                .transition()
                .duration(1000)
                .attr('width', rect_width);
        });

        that.svg.transition().duration(1000).style('width', getWidth(no_samples));
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
            draw(samples_visualized, track, gene.hugo);
            transition();
        });
    };

    return that;
};
