var GeneAlterations = function(sendData) {
    // manager to handle ajax requests for gene alteration data
    // (e.g. for tables, oncoprints, ...)
    // the idea is that you can call fire and not have to worry about whether you need to make a new AJAX call or not.

    var json = 'GeneAlterations.json',      // json url
        data = {},                   // alterations object, the return of the json 'to be'
        listeners = [],                     // queue of callback functions
        that = {},                          // GeneAlterations object
        MAKE_NEW_REQUEST = true;            // indicates whether you should make a new request or not

    var addListener = function(fun) {
        listeners.unshift(fun);

        return true;
    };

    var fire_listeners = function(data) {
        // kaboom!

        for (var fun = listeners.pop(); fun !== undefined; fun = listeners.pop()) {
            fun(data);
        }

        return listeners;
    };

    that.addListener = addListener;

    that.addListeners = function(fun_l) {
        // adds a list of listeners
        listeners = fun_l.concat(listeners);
    };

    that.fire = function(optional_callback) {
        // fire, fire on the mountain

        if (optional_callback !== undefined) {
            addListener(optional_callback);
        }

        if (MAKE_NEW_REQUEST) {
            $.post(json, sendData, function(returnData) {
                // set alterations first, then fire callbacks
                data = returnData;
                fire_listeners(data);

                MAKE_NEW_REQUEST = false;
            });
        } else {
            fire_listeners(data);
        }
    };

    that.getSendData = function() {
        // gets the original data sent to the server
        return sendData;
    };

    that.compareSendDatas = function(obj1, obj2) {
        // basic object comparison, nothing fancy
        for (i in obj1) {
            if (obj1[i] !== obj2[i]) {
                return false;
            }
        }

        return true;
    };

    that.setSendData = function(new_SendData) {
        // set the data to the new data
        // N.B. you still need to add listeners and fire!

        sendData = new_SendData;

        MAKE_NEW_REQUEST = !that.compareSendDatas(sendData, new_SendData);
        // flip on the switch since you just updated the sendData
    };

    that.getQuery = function() {
        // N.B. you should only use this inside a callback
        return GeneAlterations.query(data);
    };

    return that;
};

GeneAlterations.query = function(data) {
    // data query tools
    // for gene alterations data

    return {
        geneByHugo : function(hugo) {
            // returns all the data associated with a particular gene
            // (a row in the oncoprint matrix)
            var index = data.hugo_to_gene_index[hugo];

            return data.gene_data[index];
        },

        bySampleId : function(sample_id) {
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
        },

        getSampleList: function() {
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
        },

        data : function(_sample_id, _gene, _data_type) {
            // _sample_id, _gene, _data_type -> data
            // e.g. "TCGA-04-1331", "BRCA2", "mutations" -> "C711*"
            // (an entry in the oncoprint matrix)

            var sample_i = data.samples[_sample_id];
            if (sample_i === undefined) {
                console.log("bad sample id:", _sample_id);
                return;
            }

            var gene_i = data.hugo_to_gene_index[_gene];
            if (gene_i === undefined) {
                console.log("bad gene:", _gene);
                return;
            }

            var gene = data.gene_data[gene_i];
            if (gene_i === undefined) {
                console.log("bad gene:", _gene);
                return;
            }

            var data_type = gene[_data_type];
            if (data_type === undefined) {
                console.log("bad data type:", _data_type);
                return;
            }

            return data_type[sample_i];
        }
    };
};

// {{{ test
GeneAlterations.test = function(sendData) {
//    var sendData = {
//        cancer_study_id: "tcga_gbm",
//        genes:"EGFR MDM2",
//        cases: "TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 TCGA-02-0006 TCGA-02-0007 TCGA-02-0009",
//        geneticProfileIds: "gbm_tcga_mutations"
//        //geneticProfileIds: "gbm_mutations gbm_cna_consensus"
//    };

    console.log("==== GeneAlterations test ====");
    console.log("sending this data...", sendData);

    var geneAlterations = GeneAlterations(sendData);
    console.log("geneAlterations sample", geneAlterations);

    var listen1 = function(data) {
        console.log("listen1", data);
    };

    var listen2 = function(data) {
        console.log("listen2", (data === 5) === false);
    };

    geneAlterations.addListener(listen1);
    geneAlterations.addListener(listen2);

    geneAlterations.fire(function(data) {
        console.log('final callback!');
    });

    geneAlterations.fire(function(data) {
        console.log("preloaded: ", data);
    });

    console.log("out of listeners. nothing should happen between here...");
    geneAlterations.fire();
    console.log("...and here");

    console.log("compareObj");
    console.log(geneAlterations.compareSendDatas({a:5}, {a:5}) === true);
    console.log(geneAlterations.compareSendDatas({a:5}, {a:4}) === false);
    console.log(geneAlterations.compareSendDatas({a:5}, {b:5}) === false);
    console.log(geneAlterations.compareSendDatas({a:5, b:12}, {b:5}) === false);        // prints error message

//    sendData.cases += " TCGA-08-0525";
//    geneAlterations.setSendData(sendData);
//    console.log("setSendData: ", geneAlterations.getSendData().cases === "TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 TCGA-02-0006 TCGA-02-0007 TCGA-02-0009"
//    + " TCGA-08-0525");


    var endTest = function() {
        console.log("==== END GeneAlterations test ====");
    };

    geneAlterations.addListener(listen1);

    geneAlterations.addListener(endTest);
    geneAlterations.fire();


    console.log("\nquery.samplesAsList");
    var d;
    geneAlterations.fire(function(data) { d = data; });
    var query = GeneAlterations.query(d);
    var samples = d.samples;
    var sample_l = query.getSampleList();

    console.log('0', (samples[sample_l[0]] === 0) || samples[samples_l[0]]);
    return true;
};

// }}} 
// GeneAlterations.test();
//

// {{{ help
GeneAlterations.help = function() {
    // constructor: GeneAlterations(sendData) 
    // sendData is an object literal that looks something like this : 
    // {   cancer_study_id: \"tcga_gbm\",
    //     genes:\"EGFR MDM2\", 
    //     cases: \"TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 TCGA-02-0006 TCGA-02-0007 TCGA-02-0009\",
    //     geneticProfileIds: \"gbm_mutations gbm_cna_consensus\" }

    // returns a list of objects like this:
    //     { alterations: (see below)
    //     hugoGeneSymbol: "BRCA1"
    //     percent_altered: "12%" }
    // where alterations is a list of objects like this:
    //    { 'sample' : "TCGA-13-0727",
    //    'unaltered_sample' : true,
    //    'alteration' : CNA_NONE | MRNA_NOTSHOWN | NORMAL | RPPA_NOTSHOWN }

    // methods: \
    // addListener:    function                        ->  return undefined
    // addListeners:   [list of functions]             ->  return undefined
    // getAlterations: (optional: callback function)   ->  return undefined, or alterations if they have already been loaded
    // redo:           new_sendData    redoes the request on the new data

};
// }}}
