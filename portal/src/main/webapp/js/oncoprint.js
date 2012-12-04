var Oncoprint = function(wrapper, params) {
    var that = {};

    that.wrapper = wrapper;

    var svg = d3.select(wrapper).append('svg')
        .attr('width', 500)
        .attr('height', 500);

    that.svg = svg;

    that.getData = function() {
        return params.data;
    };

    var query = QueryGeneData(params.data);
    var samples_list = query.getSampleList();
    var genes_list = query.getGeneList();

    var no_samples = samples_list.length,
        gene_indexes = d3.map(samples.huo_to_gene_index),
        gene_data = params.data.gene_data,
        no_genes = gene_data.length;

    var selector_id = '#oncoprints #' + params.cancer_study_id;

    var rectPadding = 1.5;
    var rectWidth = 5.5;
    var calcLittleRectWidth = function(width) { return (rectWidth + 1) / rectWidth * width;};
    var trackPadding = 3;
    var rectHeight = 23;
    var littleRectHeight = rectHeight / 3;

    // global boolean (reflects a state)
    var toggleRectPadding = rectPadding;

    var calcLaneWidth = function(rectWidth, no_samples) {
        // lane : the rectangles
        // track : the label and the rectangles together, this is just my naming convention,
        return no_samples * rectWidth;
    };

    var calcSvgHeight = function(rectHeight, trackPadding, no_tracks) {
        return (rectHeight + trackPadding) * no_tracks;
    };

    var x = d3.scale.ordinal().rangeBands([0, 500], 3);
    var y = d3.scale.ordinal().rangeBands([0, 500], .5)
        .domain(genes_list);

    var laneWidth = calcLaneWidth(rectWidth, no_samples);
    var height = calcSvgHeight(rectHeight, trackPadding, no_genes);

    var translate = function(x,y) {
        return "translate(" + x + "," + y + ")";
    };

    var redrawTrack = function(name) {
        // name : name of track, e.g. hugo gene symbol (PTEN), or clinical data type, etc

        x.domain(samples_list);

        data.gene_data.forEach(function(gene_obj) {

            var sample = svg.selectAll('.sample ' + gene_obj.hugo)
                .data(samples_list, function(d) { return d;});
//            .data(samples_list);

            var sample_enter = sample.enter().append('g')
                .attr('class', 'sample ' + gene_obj.hugo)
                .attr('transform', function(d) {
                    return translate(x(d) - 130, y(gene_obj.hugo));
                });

            var cna = sample_enter.append('rect')
                .attr('class', function(d) {
                    var cna = query.data(d, gene_obj.hugo, 'cna');
                    return 'cna ' + (cna === null ? 'none' : cna);
                })
                .attr('width', 25)
                .attr('height', 50);

            var mut = sample.append('rect')
                .attr('class', function(d) {
                    var mutation = query.data(d, gene_obj.hugo, 'mutation');
                    return 'mutation ' + (mutation === null ? 'none' : 'mut');
                })
                .attr('width', 25)
                .attr('height', 25);

//        // ... mrna, rppa
        });

        console.log(samples_list);
        return samples_list;
    };

    that.redrawTrack = redrawTrack;

    var memoSort = function() {
        var memoSort = MemoSort(params.data, "GENE1");
        samples_list = memoSort.sort();

        x.domain(samples_list);

        data.gene_data.forEach(function(gene_obj) {
            d3.selectAll('.sample.' + gene_obj.hugo)
                .transition()
                .duration(1000)
                .attr('transform', function(d) {
                    return translate(x(d) - 130, y(gene_obj.hugo));
                });
        });

        return samples_list;
    };
    that.memoSort = memoSort;

    return that;
};
