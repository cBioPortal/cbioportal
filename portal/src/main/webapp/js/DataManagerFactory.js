// Gideon Dresdner
// dresdnerg@cbio.mskcc.org
// November 2012

var DataManagerFactory = (function() {

    var getNewDataManager = function() {

        var that = {};

        var listeners = [],                     // queue of callback functions
            data = {},                          // data holder
            FIRED = false;                      // have we fired already?

        that.fire = function(d) {
            // kaboom!

            data = d;

            for (var fun = listeners.pop(); fun !== undefined; fun = listeners.pop()) {
                fun(d);
            }

            FIRED = true;

            return listeners;
        };

        that.subscribe = function(fun) {
            // fun takes data as a parameter

            listeners.push(fun);
            // todo: does fire order matter?

            if (FIRED) {
                fun(data);
            }

            return true;
        };

        return that;
    };


    var that = {};

    var GENE_DATA_MANAGER = getNewDataManager();

    that.getGeneDataManager = function() {
        return GENE_DATA_MANAGER;
    };

    that.getGeneDataJsonUrl = function() { return 'GeneAlterations.json'; };     // json url

    that.getNewDataManager = function() { return getNewDataManager(); };

    return that;
})();
