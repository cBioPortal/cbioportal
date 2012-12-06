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
        return (getRectWidth() + getRectPadding()) * samples_visualized.length;
    };

    var getWidth = function() {
        return getXScale() + LABEL_PADDING + (2 * (getRectWidth() + getRectPadding()));
    };

    var getHeight = function() {
        return (RECT_HEIGHT + 10) * no_genes;
    };

    // functions that echo
    d3.select(wrapper)
        .style('width', '1300px')
        .style('overflow-x', 'auto')
        .style('overflow-y', 'hidden');

    that.wrapper = wrapper;

    that.getData = function() {
        return params.data;
    };

    // useful variables
    var data = params.data;
    var query = QueryGeneData(data);
    var genes_list = query.getGeneList();
    var gene_data = data.gene_data;
    var no_genes = gene_data.length;

    var samples_all = query.getSampleList();
    var samples_visualized = samples_all;

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

    var draw = function(track, hugo) {
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
        var sample_exit = sample.exit().remove();
    };

    that.redraw = function() {

        var svg = d3.select(wrapper).append('svg')
            .attr('width', getWidth())
            .attr('height', getHeight());
        that.svg = svg;

        // name : name of track, e.g. hugo gene symbol (PTEN), or clinical data type, etc

        x.domain(samples_visualized);

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

            draw(track, hugo);
        });

        return samples_visualized;
    };

    var transition = function() {
        // helper function

        x.domain(samples_visualized);
        x.rangeBands([0, getXScale()]);

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

        that.svg.transition().duration(1000).style('width', getWidth());
    };

    that.memoSort = function() {
        var samples = {};
        var data = params.data;

        samples_visualized.forEach(function(val, i) {
            samples[val] = i;
        });

        data[samples] = samples;
        var memoSort = MemoSort(data, genes_list);
        samples_visualized = memoSort.sort();

        transition();

        return samples_visualized;
    };

    that.defaultSort = function() {
        samples_visualized.sort(function(x,y) {
            return params.data.samples[x] - params.data.samples[y];
        });
        transition();

        return samples_visualized;
    };

    that.toggleWhiteSpace = function() {
        PADDING = !PADDING;
        transition();
    };

    that.scaleWidth = function(scalar) {
        WIDTH_SCALAR = scalar;
        transition();
    };

    var ALTERED_ONLY = false;
    that.toggleUnaltered = function() {

        if (ALTERED_ONLY = !ALTERED_ONLY) {
            samples_visualized = samples_all.filter(query.isSampleAltered);
        } else {
            samples_visualized = samples_all;
        }

        gene_data.forEach(function(gene, i) {
            var track = d3.select(d3.select(wrapper).selectAll('.track')[0][i]);
            draw(track, gene.hugo);
            transition();
        });
    };

    return that;
};
