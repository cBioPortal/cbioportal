/*
 * Copyright 2013 Memorial-Sloan Kettering Cancer Center.
 *
 * This file is part of PCViz.
 *
 * PCViz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PCViz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PCViz. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Backbone view for the BioGene information.
 */
var BioGeneView = Backbone.View.extend({

    render: function() {
        // pass variables in using Underscore.js template
        var variables = {
            geneDescription: this.model.geneDescription,
            geneAliases: this.parseDelimitedInfo(this.model.geneAliases, ":", ",", null),
            geneDesignations: this.parseDelimitedInfo(this.model.geneDesignations, ":", ",", null),
            geneLocation: this.model.geneLocation,
            geneMim: this.model.geneMim,
            geneId: this.model.geneId,
            geneUniprotId: this.extractFirstUniprotId(this.model.geneUniprotMapping),
            geneUniprotLinks: this.generateUniprotLinks(this.model.geneUniprotMapping),
            geneSummary: this.model.geneSummary
        };

        // compile the template using underscore
        var template = _.template( $("#biogene-template").html(), variables);

        // load the compiled HTML into the Backbone "el"
        this.$el.html(template);

        // format after loading
        this.format(this.model);

        return this;
    },
    format: function()
    {
        // hide rows with undefined data
        if (this.model.geneDescription == undefined)
            this.$el.find(".biogene-description").hide();

        if (this.model.geneAliases == undefined)
            this.$el.find(".biogene-aliases").hide();

        if (this.model.geneDesignations == undefined)
            this.$el.find(".biogene-designations").hide();

        if (this.model.geneChromosome == undefined)
            this.$el.find(".biogene-chromosome").hide();

        if (this.model.geneLocation == undefined)
            this.$el.find(".biogene-location").hide();

        if (this.model.geneMim == undefined)
            this.$el.find(".biogene-mim").hide();

        if (this.model.geneId == undefined)
            this.$el.find(".biogene-id").hide();

        if (this.model.geneUniprotMapping == undefined)
            this.$el.find(".biogene-uniprot-links").hide();

        if (this.model.geneSummary == undefined)
            this.$el.find(".node-details-summary").hide();

        var expanderOpts = {slicePoint: 150,
            expandPrefix: ' ',
            expandText: ' (...)',
            userCollapseText: ' (show less)',
            moreClass: 'expander-read-more',
            lessClass: 'expander-read-less',
            detailClass: 'expander-details',
            // do not use default effects
            // (see https://github.com/kswedberg/jquery-expander/issues/46)
            expandEffect: 'fadeIn',
            collapseEffect: 'fadeOut'};

        $(".biogene-info .expandable").expander(expanderOpts);

        expanderOpts.slicePoint = 2; // show comma and the space
        expanderOpts.widow = 0; // hide everything else in any case
    },
    generateUniprotLinks: function(mapping) {
        var formatter = function(id){
            return _.template($("#uniprot-link-template").html(), { id: id });
        };

        if (mapping == undefined || mapping == null)
        {
            return "";
        }

        // remove first id (assuming it is already processed)
        if (mapping.indexOf(':') < 0)
        {
            return "";
        }
        else
        {
            mapping = mapping.substring(mapping.indexOf(':') + 1);
            return ', ' + this.parseDelimitedInfo(mapping, ':', ',', formatter);
        }
    },
    extractFirstUniprotId: function(mapping) {
        if (mapping == undefined || mapping == null)
        {
            return "";
        }

        var parts = mapping.split(":");

        if (parts.length > 0)
        {
            return parts[0];
        }

        return "";
    },
    parseDelimitedInfo: function(info, delimiter, separator, formatter) {
        // do not process undefined or null values
        if (info == undefined || info == null)
        {
            return info;
        }

        var text = "";
        var parts = info.split(delimiter);

        if (parts.length > 0)
        {
            if (formatter)
            {
                text = formatter(parts[0]);
            }
            else
            {
                text = parts[0];
            }
        }

        for (var i=1; i < parts.length; i++)
        {
            text += separator + " ";

            if (formatter)
            {
                text += formatter(parts[i]);
            }
            else
            {
                text += parts[i];
            }
        }

        return text;
    }
});