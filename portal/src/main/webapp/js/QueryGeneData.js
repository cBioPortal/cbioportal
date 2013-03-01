// Gideon Dresdner
// dresdnerg@cbio.mskcc.org
// November 2012

// check out the wiki for some documentation on the Gene Data datastructure


QueryGeneData = function(data) {
    // data query tools
    // for gene alterations data

    var that = {};

    var data_values = {     // unfortunately, this is copied right out of GeneEventImpl.java
        cna: [ "AMPLIFIED", "GAINED", "DIPLOID", "HEMIZYGOUSLYDELETED", "HOMODELETED", "NONE" ],
        mrna: [ "UPREGULATED", "NORMAL", "DOWNREGULATED", "NOTSHOWN" ],
        rppa: [ "UPREGULATED", "NORMAL", "DOWNREGULATED", "NOTSHOWN" ],
        mutations: [ "MUTATED", "UNMUTATED", "NONE" ]
    };

    var gene_data = data.gene_data;

    that.byHugo = function(hugo) {
        // returns all the data associated with a particular gene
        // (a row in the oncoprint matrix)
        var index = data.hugo_to_gene_index[hugo];

        return gene_data[index];
    };

    that.bySampleId = function(sample_id) {
        // returns all the data associated with a particular sample
        // (a column in the oncoprint matrix)
        var index = data.samples[sample_id];

        var toReturn = {};

        gene_data.forEach(function(gene) {
            toReturn[gene.hugo] = {
                mutation: gene.mutations[index],
                cna: gene.cna[index],
                mrna: gene.mrna[index],
                rppa: gene.rppa[index]
            }
        });

        return toReturn;
    };

    that.data = function(sample_id, gene, data_type) {
        // _sample_id, _gene, _data_type -> data
        // e.g. "TCGA-04-1331", "BRCA2", "mutations" -> "C711*"
        // (an entry in the oncoprint matrix)

        return that.bySampleId(sample_id)[gene][data_type];
    };

    var getMapAsList = function(map) {
        var list = [];

        // get keys
        for (var key in map) {
            list.push(key);
        }

        list.sort(function(a,b) {
            return map[a] - map[b];
        });

        return list;
    };

    var sample_list = getMapAsList(data.samples);

    that.getSampleList = function() { return sample_list};

    that.getGeneList = function() { return getMapAsList(data.hugo_to_gene_index); };

    that.isSampleAltered = function(sample) {
        // returns boolean

        var data = that.bySampleId(sample);
        var genes = that.getGeneList(),
            length = genes.length;

        for (var gene_i = 0; gene_i < length ; gene_i += 1) {
            var g = data[genes[gene_i]];
//            console.log(g);

            if ((g.mutation || g.cna || g.mrna || g.rppa) !== null) {
                return true;
            }
        }
        return false;
    };

    that.unaltered_samples = (function() {
        return sample_list.filter(function(sample) {return !that.isSampleAltered(sample); });
    })();

    that.altered_samples = (function() {
        return sample_list.filter(function(sample) {return that.isSampleAltered(sample); });
    })();

    that.percent_altered = that.altered_samples.length / sample_list.length;

    that.data_types = function() {
        // returns all the datatypes that are represented in the data set
        // that is, all the ones that have non null values

        var flatten = function(prev, curr) {
            return prev.concat(curr);
        };

        var notNull = function(list) {
            var len = list.length;

            var i;
            for (i = 0; i < len; i += 1) {
                if (list[i] !== null) {
                    return true;
                }
            }
            return false;
        };

        var cna = gene_data.map(function(i) {
            return i.cna;
        }).reduce(flatten);

        var mutation = gene_data.map(function(i) {
            return i.mutations;
        }).reduce(flatten);

        var mrna = gene_data.map(function(i) {
            return i.mrna;
        }).reduce(flatten);

        var rppa = gene_data.map(function(i) {
            return i.rppa;
        }).reduce(flatten);

        var to_return = [];

        if (notNull(cna)) {
            to_return.push("cna");
        }

        if (notNull(mutation)) {
            to_return.push("mutation");
        }

        if (notNull(mrna)) {
            to_return.push("mrna");
        }

        if (notNull(rppa)) {
            to_return.push("rppa");
        }

        return to_return;
    };

    that.getDataRange = function(samples_l) {
        // returns the range of values in data per data type
        // list of samples, nothing -> range of samples, range of all samples
        // i.e. defaults to all samples

        // samples_l defaults to the entire list
        samples_l === undefined ? samples_l = sample_list : samples_l = samples_l;

        // value to return
        var range = {
            cna: {},
            mrna: {},
            rppa: {},
            mutations: {}
        };

        var appendToMap = function(map, item) {
            if (item === null) {
                // null items don't count
                return 0;
            }

            if (map[item] === undefined) {
                map[item] = 1;
                return 0;
            }
            return 1;
        };

        samples_l.forEach(function(i) {
            var genes = that.bySampleId(i);

            for (var g in genes) {
                appendToMap(range.cna, genes[g].cna);
                appendToMap(range.mrna, genes[g].mrna);
                appendToMap(range.rppa, genes[g].rppa);
                if ($.isEmptyObject(range.mutations)) { appendToMap(range.mutations, genes[g].mutation); }
            }
        });

//        console.log(range);

        return range;
    };

    that.getDataRange();

    return that;
};
