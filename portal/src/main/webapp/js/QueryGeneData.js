// Gideon Dresdner
// dresdnerg@cbio.mskcc.org
// November 2012

//     manager to handle ajax requests for gene alteration data
//     (e.g. for tables, oncoprints, ...)
//     the idea is that you can call fire and not have to worry about whether you need to make a new AJAX call or not.
//     callback is an optional callback function
//
//     methods:
//     addListener:    function                        ->  return undefined
//     addListeners:   [list of functions]             ->  return undefined
//     getAlterations: (optional: callback function)   ->  return undefined, or alterations if they have already been loaded
//     redo:           new_sendData    redoes the request on the new data
//
//     sendData is an object literal that looks something like this :
//     {   cancer_study_id: \"tcga_gbm\",
//         genes:\"EGFR MDM2\",
//         samples: \"TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 TCGA-02-0006 TCGA-02-0007 TCGA-02-0009\",
//         geneticProfileIds: \"gbm_mutations gbm_cna_consensus\" }
//
//     doing a request returns an object like this:
//     {
//      gene_data : ~,
//      hugo_to_gene_index: ~,
//      samples: ~
//     }
//
//     gene_data:           *array* of objects like this
//     {
//         cna:                     array,
//         hugo:                    string,
//         mrna:                    array,
//         mutations:               array,
//         rppa:                    array,
//         percent_altered:         string
//     }
//
//     hugo_to_gene_index:  map into the gene_data array indices
//     samples:             map into the array indices of the data_types (cna, mutations, etc)

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

    that.getSampleList = function() {
        var samples = data.samples;

        var samples_l = [];

        for (var sample in samples) {
            samples_l.push(sample);
//                console.log(sample);
        }

        samples_l.sort(function(a,b) {
            return samples[a] - samples[b];
        });

        return samples_l;
    };

    that.data = function(sample_id, gene, data_type) {
        // _sample_id, _gene, _data_type -> data
        // e.g. "TCGA-04-1331", "BRCA2", "mutations" -> "C711*"
        // (an entry in the oncoprint matrix)

        return that.bySampleId(sample_id)[gene][data_type];
    };

    return that;
};
