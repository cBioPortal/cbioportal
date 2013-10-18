//
//
// Gideon Dresdner <dresdnerg@cbio.mskcc.org> May 2013
//
//
;
// data is assumed to be in the form that is used in the OncoPrint.
// Eventually this can be modified so that it can take different json formats.
//
// attributes : the ordering of the attributes.  Omission basically amounts to "smallest"
//
// returns the data, sorted
define(function() {
    return function(data, attributes) {
        // compares two objects that have gene data (cna, mutation, mrna, rppa).
        // Returns a number that indicates the order.
        var comp_genes = function(attr1, attr2) {
            var cna_order = {AMPLIFIED:4, HOMODELETED:3, GAINED:2, HEMIZYGOUSLYDELETED:1, DIPLOID: 0, undefined: 0},
                regulated_order = {UPREGULATED: 2, DOWNREGULATED: 1, undefined: 0},
                mutation_order_f = function(m) { return m === undefined ? 0 : (/fusion($|,)/i.test(m)?2:1); };

            var cna_diff = cna_order[attr2.cna] - cna_order[attr1.cna];
            if (cna_diff !== 0) {
                return cna_diff;
            }

            var mutation_diff = mutation_order_f(attr2.mutation) - mutation_order_f(attr1.mutation);
            if (mutation_diff !== 0) {
                return mutation_diff;
            }

            var mrna_diff = regulated_order[attr2.mrna] - regulated_order[attr1.mrna];
            if (mrna_diff !== 0) {
                return mrna_diff;
            }

            var rppa_diff = regulated_order[attr2.rppa] - regulated_order[attr1.rppa];
            if (rppa_diff !== 0) {
                return rppa_diff;
            }

            return 0;       // they are equal in every way
        };

        // compares two objects of clinical data (attr_ids and attr_vals)
        // returns a *number* that indicates which one is larger
        var comp_clinical = function(attr1, attr2) {
            var discrete = isNaN(parseInt(val1));
            var val1 = attr1.attr_val;
            var val2 = attr2.attr_val;

            // "NA" value goes to the end
            if (val1 === "NA") {
                return val2 === "NA" ? 0 : 1;
            }
            if (val2 === "NA") {
                return val1 === "NA" ? 0 : -1;
            }

            // must return a number
            if (discrete) {
                if (val1 < val2) {
                    return 1;
                } else if (val2 < val1) {
                    return -1;
                } else {
                    return 0;
                }
            }
            else {  // continuous value type
                return val2 - val1;
            }
        };

        var getAttr = function(d) {
            return d.gene || d.attr_id;
        };

        // throws an error without dying
        var assert = function(bool) {
            if (!bool) {
                throw new Error("Assertion failure " + assert.caller());
            }
        };

        // a little bit of optimization
        var attr2index = (function() {
            var toReturn = {};
            for (var i = 0; i < attributes.length; i+=1) {
                toReturn[attributes[i]] = i;
            }
            return toReturn;
        }());

        var comp = function(x,y) {
            for (var i = 0; i < x.values.length; i+=1) {

                // sort attributes according to the order specified by the user
                var x_attrs = x.values
                    .sort(function(x,y) { return attr2index[getAttr(x)] - attr2index[getAttr(y)]; });
                var y_attrs = y.values
                    .sort(function(x,y) { return attr2index[getAttr(x)] - attr2index[getAttr(y)]; });

                // this is a hack
                // if there is missing data, just put the one with less data to the right
                if (x_attrs.length !== y_attrs.length) {
                    return y_attrs.length - x_attrs.length;
                }

                // iterate over the attributes of x and y in the user defined
                // order, comparing along the way
                for (var j = 0; j < x_attrs.length; j+=1) {

                    var xj = x_attrs[j];
                    var yj = y_attrs[j];

                    assert(xj.gene === yj.gene);        // what we are comparing are comparable
                    assert(xj.attr_id === yj.attr_id);

                    var diff = (xj.gene === undefined
                        ? comp_clinical(xj, yj)
                        :  comp_genes(xj, yj));

                    // return the first nonzero diff
                    if (diff !== 0) {
                        return diff;
                    }
                }
            }
            // if they are equal in all diffs, then they are truly equal.
            return 0;
        };

        return data.sort(comp);
    };
});
