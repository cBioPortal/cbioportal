/*
 * Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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

(function($, _, Backbone, d3) {
    /* Views */
    var MainView = Backbone.View.extend({
        el: "#crosscancer-container",
        template: _.template($("#crosscancer-main-tmpl").html()),

        render: function() {
            this.$el.html(this.template(this.model));
            return this;
        }
    });

    var EmptyView = Backbone.View.extend({
        el: "#crosscancer-container",
        template: _.template($("#crosscancer-main-empty-tmpl").html()),

        render: function() {
            this.$el.html(this.template(this.model));
            return this;
        }
    });

    /* Models */
    var Study = Backbone.Model.extend({
        defaults: {
            studyId: "",
            caseSetId: "",
            alterations: {
                mutation: 0,
                cna: 0,
                other: 0
            }
        }
    });

    var Studies = Backbone.Collection.extend({
        model: Study,
        url: "crosscancerquery.do",
        defaults: {
            genes: "",
            priority: 0
        },

        initialize: function(options) {
            options = _.extend(this.defaults, options);
            this.url = "?genes=" + options.genes + "&priority=" + options.priority;

            return this;
        }
    });

    /* Routers */
    AppRouter = Backbone.Router.extend({
        routes: {
            "crosscancer/:tab/:priority/:genes": "mainView",
            "crosscancer/*actions": "emptyView"
        },

        emptyView: function(actions) {
            (new EmptyView()).render();
        },

        mainView: function(tab, priority, genes) {
            (new MainView({
                model: {
                    tab: tab,
                    priority: priority,
                    genes: genes
                }
            })).render();
        }
    });

    $(function(){
        new AppRouter();
        Backbone.history.start();
    });

})(window.jQuery, window._, window.Backbone, window.d3);

