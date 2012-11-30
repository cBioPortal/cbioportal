// Gideon Dresdner
// dresdnerg@cbio.mskcc.org
// November 2012

var DataManager = (function() {
    // initialization

    var listeners = [],                     // queue of callback functions
        data = {},                          // data holder
        FIRED = false;                      // have we fired already?

    var that = {};
    that.getGeneDataJsonUrl = function() { return 'GeneAlterations.json'; };     // json url

    that.INSTANCE = {

        fire: function(d) {
            // kaboom!

            data = d;

            for (var fun = listeners.pop(); fun !== undefined; fun = listeners.pop()) {
                fun(d);
            }

            FIRED = true;

            return listeners;
        },

        subscribe: function(fun) {
            // fun takes data as a parameter

            listeners.push(fun);
            // todo: does fire order matter?

            if (FIRED) {
                fun(data);
            }

            return true;
        }

    };

    return that;
})();
