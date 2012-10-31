var GeneAlterations = function(sendData) {
    // manager to handle ajax requests for gene alteration data
    // (e.g. for tables, oncoprints, ...)

    var json = 'GeneAlterations.json',      // json url
        alterations = {},                   // alterations object, the return of the json 'to be'
        listeners = [],                     // queue of callback functions
        that = {};                          // GeneAlterations object

    var fireAll = function(data) {
        // kaboom!

        for (var fun = listeners.pop(); fun = listeners.pop(); fun !== undefined) {
            fun(data);
        }
    };

    var doRequest = function() {
        $.get(json, sendData, function(returnData) {
            // set alterations first, then fire callbacks
            alterations = returnData;
            fireAll(returnData);
        });
    };

    that.addListener = function(fun) {
        listeners.unshift(fun);
    }

    that.addListeners = function(fun_l) {
        // adds a list of listeners
        listeners = fun_l.concat(listeners);
    }

    that.getAlterations = function(callback) {
        // if there's no data, add callback to the list of callbacks (if there
        // is a callback), get the data, and fire

        if (callback !== undefined) {
            that.addListener(callback);
        }

        if ($.isEmptyObject(alterations)) {
            doRequest();
        } else {
            return alterations;
        }
    };

    that.getSendData = function() {
        // gets the original data sent to the server
        return sendData;
    };

    that.redo = function(new_SendData) {
        // set the data to the new data, do another request, and fire.
        // NB : you probably need to add listeners first
        sendData = new_SendData;

        doRequest();
    };

    return that;
};

// {{{ test
GeneAlterations.test = function() {
    var sendData = {
        cancer_study_id: "tcga_gbm",
        genes:"EGFR MDM2",
        cases: "TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 TCGA-02-0006 TCGA-02-0007 TCGA-02-0009",
        geneticProfileIds: "gbm_mutations gbm_cna_consensus"
    };

    console.log("==== GeneAlterations test ====");

    var geneAlterations = GeneAlterations(sendData);
    console.log("geneAlterations sample", geneAlterations);

    var listen1 = function(data) {
        console.log("listen1", data);
    }

    var listen2 = function(data) {
        console.log("listen2", data === 5);
    }

    var preloaded = function(data) {
        console.log("preloaded:", geneAlterations.getAlterations());
    };

    geneAlterations.addListener(listen1);
    geneAlterations.addListener(listen2);
    geneAlterations.addListener(preloaded);

    geneAlterations.getAlterations(function(data) {
        console.log('final callback!');
    });

    sendData.cases += " TCGA-08-0525";
    console.log('nothing happened between here');
    geneAlterations.redo(sendData);
    console.log('...and here');

    var endTest = function() {
        console.log("==== END GeneAlterations test ====\n");
    };

    geneAlterations.addListener(listen1);
    geneAlterations.addListener(endTest);
    geneAlterations.redo(sendData);
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

    // methods: \
    // addListener:    function                        ->  return undefined
    // addListeners:   [list of functions]             ->  return undefined
    // getAlterations: (optional: callback function)   ->  return undefined, or alterations if they have already been loaded
    // redo:           new_sendData    redoes the request on the new data
};
// }}}
