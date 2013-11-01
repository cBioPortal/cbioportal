/**
 * Default mutation view for a single gene.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: [hugo gene symbol],
 *                   mutationSummary: [single line summary text],
 *                   uniprotId: [gene identifier]}
 *          }
 */
var MainMutationView = Backbone.View.extend({
	render: function() {
		// pass variables in using Underscore.js template
		var variables = { geneSymbol: this.model.geneSymbol,
			mutationSummary: this.model.mutationSummary,
			uniprotId: this.model.uniprotId};

		// compile the template using underscore
		var template = _.template(
			$("#mutation_view_template").html(),
			variables);

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);

		// format after rendering
		this.format();
	},
	format: function() {
		var self = this;

		// hide the mutation diagram filter info text by default
		self.hideFilterInfo();
		// hide the toolbar by default
		self.$el.find(".mutation-diagram-toolbar").hide();
	},
	/**
	 * Initializes the toolbar over the mutation diagram.
	 *
	 * @param diagram       the mutation diagram instance
	 * @param geneSymbol    gene symbol as a string
	 */
	initToolbar: function(diagram, geneSymbol) {
		var self = this;

		var toolbar = self.$el.find(".mutation-diagram-toolbar");
		var pdfButton = self.$el.find(".diagram-to-pdf");
		var svgButton = self.$el.find(".diagram-to-svg");

		// helper function to trigger submit event for the svg and pdf button clicks
		var submitForm = function(alterFn, diagram, formClass)
		{
			// alter diagram to have the desired output
			alterFn(diagram, false);

			// convert svg content to string
			var xmlSerializer = new XMLSerializer();
			var svgString = xmlSerializer.serializeToString(diagram.svg[0][0]);

			// restore previous settings after generating xml string
			alterFn(diagram, true);

			// set actual value of the form element (svgelement)
			var form = self.$el.find("." + formClass);
			form.find('input[name="svgelement"]').val(svgString);

			// submit form
			form.submit();
		};

		// helper function to adjust SVG for file output
		var alterDiagramForSvg = function(diagram, rollback)
		{
			var topLabel = geneSymbol;

			if (rollback)
			{
				topLabel = "";
			}

			// adding a top left label (to include a label in the file)
			diagram.updateTopLabel(topLabel);
		};

		// helper function to adjust SVG for PDF output
		var alterDiagramForPdf = function(diagram, rollback)
		{
			// we also need the same changes (top label) in pdf
			alterDiagramForSvg(diagram, rollback);

			cbio.util.alterAxesAttrForPDFConverter(
					diagram.svg.select(".mut-dia-x-axis"), 8,
					diagram.svg.select(".mut-dia-y-axis"), 3,
					rollback);
		};

		//add listener to the svg button
		svgButton.click(function (event) {
			// submit svg form
			submitForm(alterDiagramForSvg, diagram, "svg-to-file-form");
		});

		// add listener to the pdf button
		pdfButton.click(function (event) {
			// submit pdf form
			submitForm(alterDiagramForPdf, diagram, "svg-to-pdf-form");
		});

		toolbar.show();
	},
	/**
	 * Initializes the filter reset link, which is a part of filter info
	 * text on top of the diagram.
	 *
	 * @param diagram       mutation diagram instance
	 * @param tableView     [optional] mutation table view instance
	 * @param mut3dVisView  [optional] 3D vis view instance
	 */
	initResetFilterInfo: function(diagram, tableView, mut3dVisView) {
		var self = this;
		var resetLink = self.$el.find(".mutation-details-filter-reset");

		// add listener to diagram reset link
		resetLink.click(function (event) {
			// reset the diagram contents
			diagram.resetPlot();
			// also reset the table filters (if provided)
			if (tableView)
			{
				// reset all previous table filters
				tableView.resetFilters();
			}
			// also reset the 3d vis focus (if provided)
			if (mut3dVisView)
			{
				// reset all previous table filters
				mut3dVisView.focusView(false);
			}
			// hide the filter info text
			self.hideFilterInfo();
		});
	},
	showFilterInfo: function() {
		this.$el.find(".mutation-details-filter-info").show();
	},
	hideFilterInfo: function() {
		this.$el.find(".mutation-details-filter-info").hide();
	}
});
