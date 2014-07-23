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
    parse: function(response) {
        this.attributes = function() { return response.attributes; };   // save the attributes
        return response.data;    // but the data is what is to be model-ed
    },
    url: "webservice.do?cmd=getClinicalData&format=json"
});

var ClinicalMutationColl = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    parse: function(response) {
        this.attributes=function() { return [{attr_id: "mutations", datatype: "NUMBER",description: "Number of mutation", display_name: "Mutations"}]; };   // save the attributes
        var result = new Array();
        for (var property in response)
        {
            var tem = new Object();
            tem.attr_val = response[property];
            tem.sample =property;
            tem.attr_id = "mutations";
            result.push(tem);
        }
 
        return result;    // but the data is what is to be model-ed
    },
    url: "mutations.json"
});

var GeneDataColl = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    url: "GeneData.json"
});

var ClinicalAttributesColl= Backbone.Collection.extend({
    model: Backbone.Model.extend({}),       // the trivial model
    url: function() {
        return "clinicalAttributes.json";
    }
});
