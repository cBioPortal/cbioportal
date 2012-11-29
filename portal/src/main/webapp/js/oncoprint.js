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

    var query = GeneAlterations.query(params.data);
    var samples_list = query.getSampleList();

    var samples_sorted = d3.map(params.data.samples),
        no_samples = samples_list.length,
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

    var laneWidth = calcLaneWidth(rectWidth, no_samples);
    var height = calcSvgHeight(rectHeight, trackPadding, no_genes);

    var translate = function(x,y) {
        return "translate(" + x + "," + y + ")";
    };

    var redrawTrack = function(name) {
        // name : name of track, e.g. hugo gene symbol (PTEN), or clinical data type, etc

        var data = query.byHugo(name);

        // reform the data into a list of elements that correspond to visualization elements
        var zipped = d3.zip(samples_list, data.cna, data.mutations, data.mrna, data.rppa)
            .map(function(i) {
                return { sample_id: i[0], cna: i[1], mutation: i[2], mrna: i[3], rppa: i[4] }
            });

        var sample = svg.selectAll('sample')
            .data(zipped, function(d) {return d.sample_id;});

        var sample_enter = sample.enter().append('g')
            .attr('class', 'sample')
            .attr('transform', function(d) {
                return translate(50 * samples_sorted.get(d.sample_id), 10);
            });

        var cna = sample_enter.append('rect')
            .attr('class', function(d) { return 'cna ' + (d.cna === null ? 'none' : d.cna); })
            .attr('width', 25)
            .attr('height', 50);

        var mut = sample.append('rect')
            .attr('class', function(d) { return 'mutation ' + (d.mutation === null ? 'none' : 'mut'); })
            .attr('width', 25)
            .attr('height', 25);

        // ... mrna, rppa

        var sample_update = d3.transition(sample)
            .attr('transform',  function(d) {
                console.log(d, d.sample_id, samples_sorted.get(d.sample_id));
                return translate(50 * samples_sorted.get(d.sample_id), 10);
            });
    };
    that.redrawTrack = redrawTrack;

    var memoSort = function() {
        var memoSort = MemoSort(params.data, "GENE1");
        samples_list = memoSort.sort();

        redrawTrack("GENE1");

        return samples_list;
    };
    that.memoSort = memoSort;

    return that;
};
