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

var ClinicalColl = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    initialize: function(options) {
        this.cancer_study_id = options.cancer_study_id;
        this.case_list = options.case_list;
        this.attr_id = options.attr_id;
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

var GeneDataColl = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    url: "GeneData.json"
});

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
