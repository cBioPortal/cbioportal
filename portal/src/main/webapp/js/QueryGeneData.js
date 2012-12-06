// Gideon Dresdner
// dresdnerg@cbio.mskcc.org
// November 2012

// check out the wiki for some documentation on the Gene Data datastructure


QueryGeneData = function(data) {
    // data query tools
    // for gene alterations data

    var that = {};

    that.byHugo = function(hugo) {
        // returns all the data associated with a particular gene
        // (a row in the oncoprint matrix)
        var index = data.hugo_to_gene_index[hugo];

        return data.gene_data[index];
    };

    that.bySampleId = function(sample_id) {
        // returns all the data associated with a particular sample
        // (a column in the oncoprint matrix)
        var index = data.samples[sample_id];

        var toReturn = {};

        data.gene_data.forEach(function(gene) {
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

    that.getUnalteredSamples = function() {
        return sample_list.filter(function(sample) {return !that.isSampleAltered(sample); });
    };

    return that;
};
