var Oncoprint = function(wrapper, params) {
    var that = {};

    // constants
    var RECT_HEIGHT = 23;
    var LITTLE_RECT_HEIGHT = RECT_HEIGHT / 3;
    var LABEL_PADDING = 130;

    // global state variables
    var PADDING = true;
    var WIDTH_SCALAR = 1;

    // functions that get state
    var getRectWidth = function() {
        var unscaled = 5.5;
        return WIDTH_SCALAR * unscaled;
    };

    var getRectPadding = function() {
        var unscaled = 3;
        return PADDING ? (WIDTH_SCALAR * unscaled) : 0;
    };

    var getXScale = function() {
        return (getRectWidth() + getRectPadding()) * no_samples;
    };

    var getWidth = function() {
        return getXScale() + LABEL_PADDING + (2 * (getRectWidth() + getRectPadding()));
    };

    var getHeight = function() {
        return (RECT_HEIGHT + 10) * no_genes;
    };

    // functions that echo
    d3.select(wrapper)
        .attr('width', 600)
        .attr('overflow-x', 'auto')
        .attr('overflow-y', 'hidden');

    that.wrapper = wrapper;

    that.getData = function() {
        return params.data;
    };

    // useful variables
    var query = QueryGeneData(params.data);
    var samples_list = query.getSampleList();
    var sorted_samples;
    var no_samples = samples_list.length;
    var genes_list = query.getGeneList();
    var gene_data = params.data.gene_data;
    var no_genes = gene_data.length;

    var x = d3.scale.ordinal().rangeBands([0, getXScale()], 0);

    var y = d3.scale.ordinal().rangeBands([0, getHeight()], 0)
        .domain(genes_list);

    var translate = function(x,y) {
        return "translate(" + x + "," + y + ")";
    };

    var cleanHugo = function(hugo) {
        // can't have '/' in DOM id
        return hugo.replace("/", "_");
    };

    that.draw = function() {

        var svg = d3.select(wrapper).append('svg')
            .attr('width', getWidth())
            .attr('height', getHeight());
        that.svg = svg;

        // name : name of track, e.g. hugo gene symbol (PTEN), or clinical data type, etc

        x.domain(samples_list);

        gene_data.forEach(function(gene_obj) {

            var hugo = gene_obj.hugo;
            var cleaned_hugo = cleanHugo(hugo);

            // N.B. there is no data bound to g,
            // is this bad form?
            var track = svg.append('g')
                .attr('transform', translate(LABEL_PADDING, 0))
                .attr('class', 'track');

            var label = track.append('text')
                .attr('x', -LABEL_PADDING)
                .attr('y', y(hugo) + .5 * RECT_HEIGHT);

            label.append('tspan')
                .attr('text-anchor', 'start')
                .text(gene_obj.hugo);

            label.append('tspan')
                .attr('text-anchor', 'end')
                .attr('x', 0 - 5)
                .text(gene_obj.percent_altered);

            var sample = track.selectAll('.sample')
                .data(samples_list, function(d) { return d;});

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

            var mut = sample.append('rect')
                .attr('class', function(d) {
                    var mutation = query.data(d, hugo, 'mutation');
                    return 'mutation ' + (mutation === null ? 'none' : 'mut');
                })
                .attr('width', getRectWidth())
                .attr('height', LITTLE_RECT_HEIGHT);

//        // ... mrna, rppa
        });

        return samples_list;
    };

    var transition = function() {
        // helper function

        that.svg.transition().duration(1000).attr('width', getWidth());

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
    };

    that.memoSort = function() {
        // todo: precompute this
        var memoSort = MemoSort(params.data, genes_list);
        sorted_samples = memoSort.sort();

        x.domain(sorted_samples);
        transition();

        return sorted_samples;
    };

    that.defaultSort = function() {
        x.domain(samples_list);
        transition();
    };

    that.toggleWhiteSpace = function() {
        PADDING = !PADDING;
        x.rangeBands([0, getXScale()]);
        transition();
    };

    that.scaleWidth = function(scalar) {
        WIDTH_SCALAR = scalar;

        x.rangeBands([0, getXScale()]);

        transition();
    };

    return that;
};
