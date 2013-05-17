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
        makeHash: makeHash,
    };
})();

// You create a new ClinicalModel, you pass an object with the fields: sample,
// cancer_study_id, attr_id.  When you .fetch(), you get back the attr_val.
var ClinicalModel = Backbone.Model.extend({
    initialize: function(attributes) {
        this.sample = attributes.sample;
        this.cancer_study_id = attributes.cancer_study_id;
        this.attr_id = attributes.attr_id;
    },
    url: function() {
        return "webservice.do?cmd=getClinicalData&format=json"
                + "&case_list=" + this.sample
                + "&cancer_study_id=" + this.cancer_study_id
                + "&attribute_id=" + this.attr_id;
    }
});

// param: [ case_list | case_set_id | case_ids_key ]
var ClinicalColl = Backbone.Collection.extend({
    model: ClinicalModel,
    initialize: function(models, options) {
        this.cancer_study_id = options.cancer_study_id;
        this.case_list = options.case_list;
        this.case_set_id = options.case_set_id;
        this.case_ids_key = options.case_ids_key;
    },
    parse: function(res) {
        this.attributes = res.attributes;   // save the attributes
        return res.data;                    // but the data is what is to be model-ed
    },
    url: function() {
        var url_str = "webservice.do?cmd=getClinicalData&format=json&";
        if (this.cancer_study_id) {
            url_str += "cancer_study_id=" + this.cancer_study_id + "&";
        }

        if (this.case_list) {
            url_str += "case_list=" + this.case_list;
        }
        else if (this.case_set_id) {
            url_str += "case_set_id=" + this.case_set_id;
        }
        else if (this.case_ids_key) {
            url_str += "case_ids_key=" + this.case_ids_key;
        }
        else {
            throw new Error("invalid parameters to ClinicalColl");
        }
        return url_str;
    }
});

var GeneDataModel = Backbone.Model.extend({
    initialize: function(attributes) {
        this.case_list = attributes.sample;
        this.gene = attributes.gene;
        this.cancer_study_id = attributes.cancer_study_id;
        this.geneticProfileIds = attributes.geneticProfileIds;
        this.z_score_threshold = attributes.z_score_threshold || 2;     // default
        this.rppa_score_threshold = attributes.rppa_score_threshold || 2;

        // for jQuery
        this.data = this.attributes;
        this.type = "POST";
    },
    url: function() {
        return "GeneData.json";
    }
});

// model for gene datas (various molecular profiles)
// probably not to be used until we start GETing our data instead of POSTing it.
//var GeneDataColl = Backbone.Collection.extend({
//    model: model,
//    initialize: function(models, options) {
//        this.data = {       // jQuery param - data sent to the server
//            genes : options.genes,
//            geneticProfileIds : options.geneticProfileIds,
//            samples : options.samples,
//            caseSetId : options.caseSetId,
//            z_score_threshold : options.z_score_threshold,
//            rppa_score_threshold : options.rppa_score_threshold
//        };
//        this.type = "POST";
//    },
//    url: function() {
//        return "GeneData.json";
//    }
//});
