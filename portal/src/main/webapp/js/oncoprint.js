var Oncoprint = function(wrapper, params) {
    var that = {};

    that.wrapper = wrapper;

    that.getData = function() {
        return params.data;
    };

    var query = QueryGeneData(params.data);
    var samples_list = query.getSampleList();
    var sorted_samples;
    var genes_list = query.getGeneList();

    var no_samples = samples_list.length,
        gene_data = params.data.gene_data,
        no_genes = gene_data.length;

    var rectHeight = 23;
    var littleRectHeight = rectHeight / 3;

    // global state variables
    var padding = true;
    var width_scalar = 1;

    var getRectWidth = function() {
        var unscaled = 5.5;

        return width_scalar * unscaled;
    };

    var getRectPadding = function() {
        var unscaled = 3;

        return padding ? (width_scalar * unscaled) : 0;
    };

    var getWidth = function() {

        return (getRectWidth() + getRectPadding()) * no_samples;
    };

    var getHeight = function() {
        return (rectHeight + 10) * no_genes;
    };

    var x = d3.scale.ordinal().rangeBands([0, getWidth()], 0);

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
            .attr('width', getWidth() + (2 * (getRectWidth() + getRectPadding())))
            .attr('height', getHeight());
        that.svg = svg;

        // name : name of track, e.g. hugo gene symbol (PTEN), or clinical data type, etc

        x.domain(samples_list);

        gene_data.forEach(function(gene_obj) {

            var cleaned_hugo = cleanHugo(gene_obj.hugo);

            var sample = svg.selectAll('.sample ' + cleaned_hugo)
                .data(samples_list, function(d) { return d;});

            var sample_enter = sample.enter().append('g')
                .attr('class', 'sample ' + cleaned_hugo)
                .attr('transform', function(d) {
                    return translate(x(d), y(gene_obj.hugo));
                });

            var cna = sample_enter.append('rect')
                .attr('class', function(d) {
                    var cna = query.data(d, gene_obj.hugo, 'cna');
                    return 'cna ' + (cna === null ? 'none' : cna);
                })
                .attr('width', getRectWidth())
                .attr('height', rectHeight);

            var mut = sample.append('rect')
                .attr('class', function(d) {
                    var mutation = query.data(d, gene_obj.hugo, 'mutation');
                    return 'mutation ' + (mutation === null ? 'none' : 'mut');
                })
                .attr('width', getRectWidth())
                .attr('height', littleRectHeight);

//        // ... mrna, rppa
        });

        return samples_list;
    };

    var transition = function() {
        // helper function

        var rect_width = getRectWidth();

        gene_data.forEach(function(gene_obj) {
            that.svg.selectAll('.sample.' + cleanHugo(gene_obj.hugo))
                .transition()
                .duration(1000)
                .attr('transform', function(d) {
                    return translate(x(d), y(gene_obj.hugo));
                });

            that.svg.selectAll('rect')
                .transition()
                .duration(1000)
                .attr('width', rect_width);
        });
    };

    that.memoSort = function() {
        var memoSort = MemoSort(params.data, genes_list);
        sorted_samples = memoSort.sort();

        x.domain(sorted_samples);       // reset global variable
        transition();

        return sorted_samples;
    };

    that.defaultSort = function() {
        x.domain(samples_list);
        transition();
    };

    that.toggleWhiteSpace = function() {
        padding = !padding;
        x.rangeBands([0, getWidth()]);
        transition();
    };

    that.scaleWidth = function(scalar) {
        width_scalar = scalar;

        x.rangeBands([0, getWidth()]);

        transition();
    };

    return that;
};
