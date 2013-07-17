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
        makeHash: makeHash
    };
})();

// params: sample, cancer_study_id, attr_id
// When you use the method `fetch()`, the attr_val is added to the object (from server)
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

// params: [cancer_study_id] , [attr_id], case_list (list of case_ids separated by space)
// when you call the method fetch() you get back a list of ClinicalModels
// AND a list of attribute objects which provide metadata about attributes in the cohort
var ClinicalColl = Backbone.Collection.extend({
    model: ClinicalModel,
    initialize: function(options) {
        this.cancer_study_id = options.cancer_study_id;
        this.case_list = options.case_list;
        this.attr_id = options.attr_id;
//        this.case_set_id = options.case_set_id;
//        this.case_ids_key = options.case_ids_key;
    },
    parse: function(response) {
        this.attributes = function() { return response.attributes; };   // save the attributes
        return response.data;    // but the data is what is to be model-ed
    },
    url: function() {
        var url_str = "webservice.do?cmd=getClinicalData&format=json&";
        if (this.cancer_study_id) {
            url_str += "cancer_study_id=" + this.cancer_study_id + "&";
        }
        if (this.attr_id) {
            url_str += "attribute_id=" + this.attr_id + "&";
        }
        url_str += "case_list=" + this.case_list;
        return url_str;
    }
});

// params : sample, gene, cancer_study_id, geneticProfileIds (string of genetic
// profile ids separated by a space), [z_score_threshold],
// [rppa_score_threshold]
var GeneDataModel = Backbone.Model.extend({
    initialize: function(params) {
        this.case_list = params.sample;
        this.genes = params.gene;
        this.cancer_study_id = params.cancer_study_id;
        this.genetic_profiles = params.genetic_profiles;
        this.z_score_threshold = params.z_score_threshold || "2.0";     // defaults
        this.rppa_score_threshold = params.rppa_score_threshold || "2.0";
    },
    parse: function(res, xhr) {
        if (res.length === 1) {
            return res[0];
        } else {
            return res;
        }
    },
    url: function() {
        var url_str = "GeneData.json?format=json&";
        url_str += "case_list=" + this.case_list + "&";
        url_str += "genes=" + this.genes + "&";
        url_str += "cancer_study_id=" + this.cancer_study_id + "&";
        url_str += "geneticProfileIds=" + this.genetic_profiles + "&";
        url_str += "z_score_threshold=" + this.z_score_threshold + "&";
        url_str += "rppa_score_threshold=" + this.rppa_score_threshold + "&";

        return url_str;
    }
});

// example
//
//var foobar = new GeneDataModel({
//    sample: cases.split(" ")[0],
//    gene: raw_genes_str.split(" ")[0],
//    cancer_study_id: cancer_study_id_selected,
//    genetic_profiles: genetic_profiles,
//    z_score_threshold: zscore_threshold,
//    rppa_score_threshold: rppa_score_threshold
//});
//foobar.fetch();
;
// params : cancer_study_id, genes, case_list, genetic_profiles,
// [z_score_threshold], [rppa_score_threshold]
var GeneDataColl = Backbone.Collection.extend({
    model: GeneDataModel,
    initialize: function(attributes) {
        this.cancer_study_id = attributes.cancer_study_id;
        this.genes = attributes.genes;
        this.case_list = attributes.case_list;
        this.genetic_profiles = attributes.genetic_profiles;
        this.z_score_threshold = attributes.z_score_threshold || 2;
        this.rppa_score_threshold = attributes.rppa_score_threshold || 2;
    },
    url: function() {
        return "GeneData.json?format=json&"
            + "cancer_study_id=" + this.cancer_study_id + "&"
            + "genes=" + this.genes + "&"
            + "geneticProfileIds=" + this.genetic_profiles + "&"
            + "z_score_threshold=" + this.z_score_threshold + "&"
            + "rppa_score_threshold=" + this.rppa_score_threshold + "&"
            + "case_list=" + this.case_list;
    }
});

// example
//
//var foobar = new GeneDataColl({
//    cancer_study_id: cancer_study_id_selected,
//    case_list: cases,
//    genes: gene_list,
//    genetic_profiles: genetic_profiles,
//    z_score_threshold: zscore_threshold,
//    rppa_score_threshold: rppa_score_threshold
//});
//foobar.fetch({type: "POST"});
;

// params : object literal { case_list: <string of cases separated by space> }

// on fetch(), populates list of object literals with the fields:
// [attr_id, display_name, description, datatype]
var ClinicalAttributesColl= Backbone.Collection.extend({
    model: Backbone.Model.extend({}),       // the trivial model
    initialize: function(attributes) {
        this.case_list = attributes.case_list;
    },
    url: function() {
        return "clinicalAttributes.json?case_list=" + this.case_list;
    }
});

// example
//
// var foobar = new ClinicalAttributesColl({case_list: cases.split(" ")});
// x.fetch();
