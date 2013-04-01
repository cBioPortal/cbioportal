/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

//
//
// Gideon Dresdner <dresdnerg@cbio.mskcc.org>
//

var Clinical = (function() {
    // namespace

    var querier = function(data) {
        // [ list of objects ] -> function bySample, function byAttr
        // [] -> a bunch of functions that will break!
        //
        // takes a list of objects, [{ case_id, attr_id, attr_val }]
        // and returns two functions for querying this data.
        // This is done by lazy creation of hashmaps and then
        // the trivial wrapping of query functions around those hashmaps

        var makeHash = function(data, key) {
            // [] -> {}
            // [ list of objects ], key -> { key : object }
            // note that this is doesn't make sense unless
            // the object have values for the given key
            var hash = {};
            _.each(data, function(i) {
                hash[i[key]] = i;
            });

            return hash;
        };

        var sample2data;
        var attr2data;

        return {
            bySample: function(sample_id) {
                // lazy initialization
                sample2data === undefined ? sample2data = makeHash(data, "case_id") : sample2data = sample2data;

                return sample2data[sample_id];
            },
            byAttr: function(attr_id) {
                attr2data === undefined ? attr2data = makeHash(data, "attr_id") : attr2data = attr2data;

                return attr2data[attr_id];
            }
        }
    };

    // trivial Backbone model
    var model = Backbone.Model.extend({});

    // collection of (clinical) models with the proper query strings
    var collection = Backbone.Collection.extend({
        model: model,
        initialize: function(models, options) {
            this.query_type = options.t;
            this.query = options.q;
        },
        url: function() {
            return "clinical.json?t=" + this.query_type + "&" + "q=" + this.query;
        }
    });

    return {
        model: model,
        collection: collection,
        querier:querier
    };

//    var clinicals = new collection([], {t: "cancer_study_id", q: "brca_tcga"});
//
//    clinicals.fetch({
//        success: function(data) {
//            console.log(data);
//        }
//    });
})();
