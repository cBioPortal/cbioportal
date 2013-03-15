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

var ClinicalData = (function() {
    // namespace

    var Clinical = Backbone.Model.extend({});

    var Clinicals = Backbone.Collection.extend({
        model: Clinical,
        initialize: function(models, options) {
            this.query = options.query;
        },
        url: function() {
//            http://localhost:8080/public-portal/clinical.json?samples=tcga-a1-a0sb
            return "clinical.json?cancer_study_id=" + this.query;
        }
    });

    var clinicals = new Clinicals([], {query: "brca_tcga"});
    clinicals.fetch({
        success: function(data) {
            console.log(data);
        }
    });

    var Router = Backbone.Router.extend({
        routes: {
            "clinical/:id": "req"
        }
    });

    var router = new Router();

    router.on('route:req', function(id) {
        console.log(id);
    });

    Backbone.history.start();       // N.B.
})();
