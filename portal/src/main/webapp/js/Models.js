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

    // hack to get the proper case list parameter and value
    var findCaseList = function(obj) {
        var toReturn = ""
        if (this.case_list) {
            toReturn += "case_list=" + this.case_list + "&";
        }
        else if (this.case_set_id) {
            toReturn += "case_set_id=" + this.case_set_id + "&";
        }
        else if (this.case_ids_key) {
            toReturn += "case_ids_key=" + this.case_ids_key + "&";
        }
        else {
            throw new Error("invalid parameters to case set parameter");
        }
        return toReturn;
    };

    return {
        makeHash: makeHash,
        findCaseList: findCaseList
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
        return url_str + ModelUtils.findCaseList(this);
    }
});

// params : sample, gene, cancer_study_id, geneticProfileIds (string of genetic
// profile ids separated by a space), [z_score_threshold],
// [rppa_score_threshold]
var GeneDataModel = Backbone.Model.extend({
    initialize: function(attributes) {
        this.data = {       // for jQuery AJAX request
            case_list : attributes.sample,
            genes : attributes.gene,
            cancer_study_id : attributes.cancer_study_id,
            geneticProfileIds : attributes.geneticProfileIds,
            z_score_threshold : attributes.z_score_threshold || 2,     // default
            rppa_score_threshold : attributes.rppa_score_threshold || 2
        };
    },
    type: "POST",
    url: function() {
        var url_str = "GeneData.json?format=json&";
        for (var key in this.data) {
            if (undefined !== this.data[key]) {
                url_str += key + "=" + this.data[key] + "&";
            }
        }
        return url_str;
    }
});

var GeneDataColl = Backbone.Model.extend({
    initialize: function(attributes) {
        this.cancer_study_id = attributes.cancer_study_id;
        this.genes = attributes.genes;
        this.geneticProfileIds = attributes.geneticProfileIds;
        this.z_score_threshold = attributes.z_score_threshold || 2;
        this.rppa_score_threshold = attributes.rppa_score_threshold || 2;
    },
    url: function() {
        return "GeneData.json?format=json&"
            + "cancer_study_id=" + this.cancer_study_id + "&"
            + "genes=" + this.genes + "&"
            + "geneticProfileIds=" + this.geneticProfileIds + "&"
            + "z_score_threshold=" + this.z_score_threshold + "&"
            + "rppa_score_threshold=" + this.rppa_score_threshold + "&"
            + ModelUtils.findCaseList(this.attributes);
    }
});

var x = new GeneDataColl(
        {   cancer_study_id:"ov_tcga",
            genes:"BRCA1 BRCA2",
            geneticProfileIds:"ov_tcga_mutations ov_tcga_gistic",
            z_score_threshold:2,
            rppa_score_threshold:2,
            case_ids_key:"74e69883f33b8482934f5d75aa8e16d0"
        }
        );

x.fetch()



