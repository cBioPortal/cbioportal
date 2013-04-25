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

;
//
// namespace for Model Utility functions
var ModelUtils = (function() {
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

    return {
        makeHash: makeHash
    };
});

// trivial Backbone model
// todo: make this into a real model
var model = Backbone.Model.extend({});

// model for clinical datas
// params : [ initial lists of clinicals (usually []) , { case_set_id }]
var ClinicalColl = Backbone.Collection.extend({
    model: model,
    initialize: function(models, options) {
        this.case_set_id = options.case_set_id;
    },
    url: function() {
        return "webservice.do?cmd=getClinicalData&format=json&case_set_id=" + this.case_set_id;
    }
});

// model for gene datas (various molecular profiles)
// probably not to be used until we start GETing our data instead of POSTing it.
var GeneDataColl = Backbone.Collection.extend({
    model: model,
    initialize: function(models, options) {
        this.data = {       // jQuery param - data sent to the server
            genes : options.genes,
            geneticProfileIds : options.geneticProfileIds,
            samples : options.samples,
            caseSetId : options.caseSetId,
            z_score_threshold : options.z_score_threshold,
            rppa_score_threshold : options.rppa_score_threshold
        };
        this.type = "POST";
    },
    url: function() {
        return "GeneData.json";
    }
});
