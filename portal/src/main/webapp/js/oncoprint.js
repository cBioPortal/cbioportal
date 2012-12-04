var Oncoprint = function(wrapper, params) {
    var that = {};

    that.wrapper = wrapper;

    that.getData = function() {
        return params.data;
    };

    var query = QueryGeneData(params.data);
    var samples_list = query.getSampleList();
    var genes_list = query.getGeneList();

    var no_samples = samples_list.length,
        gene_data = params.data.gene_data,
        no_genes = gene_data.length;

    var rectWidth = 5.5;
    var rectHeight = 23;
    var rectPadding = 3;
    var littleRectHeight = rectHeight / 3;
    var getLaneWidth = function(padding) {
        // padding : boolean

        var _rectPadding = padding ? rectPadding : 0;
        return (rectWidth + _rectPadding) * no_samples;
    };

    var getHeight = function() {
        return (rectHeight + 10) * no_genes;
    };

    var x = d3.scale.ordinal().rangeBands([0, getLaneWidth(true)], 0);

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
            .attr('width', getLaneWidth() + (2 * (rectWidth + rectPadding)))
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
                .attr('width', rectWidth)
                .attr('height', rectHeight);

            var mut = sample.append('rect')
                .attr('class', function(d) {
                    var mutation = query.data(d, gene_obj.hugo, 'mutation');
                    return 'mutation ' + (mutation === null ? 'none' : 'mut');
                })
                .attr('width', rectWidth)
                .attr('height', littleRectHeight);

//        // ... mrna, rppa
        });

        return samples_list;
    };

    that.memoSort = function() {
        var memoSort = MemoSort(params.data, genes_list);
        samples_list = memoSort.sort();

        x.domain(samples_list);

        gene_data.forEach(function(gene_obj) {
            that.svg.selectAll('.sample.' + cleanHugo(gene_obj.hugo))
                .transition()
                .duration(1000)
                .attr('transform', function(d) {
                    return translate(x(d), y(gene_obj.hugo));
                });
        });

        return samples_list;
    };

    return that;
};
