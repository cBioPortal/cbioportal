/**
 * Tooltip view for the mutation table's FIS column.
 *
 * options: {el: [target container],
 *           model: {xvia: [link to Mutation Assessor],
 *                   impact: [impact text or value]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PredictedImpactTipView = Backbone.View.extend({
	render: function()
	{
		// compile the template
		var template = this.compileTemplate();

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);
		this.format();
	},
	format: function()
	{
		var isValidLink = function(url)
		{
			var valid = true;

			if (url == null || url == "NA" || url.length == 0)
			{
				valid = false;
			}

			return valid;
		};

		var xvia = this.model.xvia;

		if (!isValidLink(xvia))
		{
			this.$el.find(".mutation-assessor-main-link").hide();
		}

		var pdbLink = this.model.pdbLink;

		if (!isValidLink(pdbLink))
		{
			this.$el.find(".mutation-assessor-3d-link").hide();
		}

		var msaLink = this.model.msaLink;

		if (!isValidLink(msaLink))
		{
			this.$el.find(".mutation-assessor-msa-link").hide();
		}
	},
	compileTemplate: function()
	{
		// pass variables in using Underscore.js template
		var variables = {linkOut: this.model.xvia,
			msaLink: this.model.msaLink,
			pdbLink: this.model.pdbLink,
			impact: this.model.impact};

		// compile the template using underscore
		return _.template(
				$("#mutation_details_fis_tip_template").html(),
				variables);
	}
});

