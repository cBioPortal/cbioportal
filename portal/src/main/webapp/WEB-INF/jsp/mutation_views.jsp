<script type="text/template" id="default_mutation_details_template">
	<div id='mutation_details_loader'>
		<img src='{{loaderImage}}'/>
	</div>
</script>

<script type="text/template" id="mutation_view_template">
	<h4>{{geneSymbol}}: {{mutationSummary}}</h4>
	<div id='mutation_diagram_toolbar_{{geneSymbol}}' class='mutation-diagram-toolbar'>
		<a href='http://www.uniprot.org/uniprot/{{uniprotId}}'
		   class='mutation-details-uniprot-link'
		   target='_blank'>{{uniprotId}}</a>
		<form style="display:inline-block"
		      action='svgtopdf.do'
		      method='post'
		      class='svg-to-pdf-form'>
			<input type='hidden' name='svgelement'>
			<input type='hidden' name='filetype' value='pdf'>
			<input type='hidden' name='filename' value='mutation_diagram_{{geneSymbol}}.pdf'>
		</form>
		<form style="display:inline-block"
		      action='svgtopdf.do'
		      method='post'
		      class='svg-to-file-form'>
			<input type='hidden' name='svgelement'>
			<input type='hidden' name='filetype' value='svg'>
			<input type='hidden' name='filename' value='mutation_diagram_{{geneSymbol}}.svg'>
		</form>
		<button class='diagram-to-pdf'>PDF</button>
		<button class='diagram-to-svg'>SVG</button>
	</div>
	<div id='mutation_diagram_{{geneSymbol}}' class='mutation-diagram-container'></div>
	<div id='mutation_table_{{geneSymbol}}' class='mutation-table-container'>
		<img src='images/ajax-loader.gif'/>
	</div>
</script>

<script type="text/javascript">
	// TODO this is duplicate! it is better to put this into a global js
	// This is for the moustache-like templates
	// prevents collisions with JSP tags
	_.templateSettings = {
		interpolate : /\{\{(.+?)\}\}/g
	};

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
		}
	});

	/**
	 * Default mutation details view for the entire mutation details tab.
	 * Creates a separate MainMutationView (another Backbone view) for each gene.
	 *
	 * options: {el: [target container],
	 *           model: {mutations: [mutation data as an array of JSON objects],
	 *                   sampleArray: [list of case ids as an array of strings],
	 *                   diagramOpts: [mutation diagram options -- optional]}
	 *          }
	 */
	var MutationDetailsView = Backbone.View.extend({
		render: function() {
			var self = this;

			self.util = new MutationDetailsUtil(
					new MutationCollection(self.model.mutations));

			// TODO make the image customizable?
			var variables = {loaderImage: "images/ajax-loader.gif"};

			// compile the template using underscore
			var template = _.template(
				$("#default_mutation_details_template").html(),
				variables);

			// load the compiled HTML into the Backbone "el"
			self.$el.html(template);

			self._initDefaultView(self.$el,
				self.model.sampleArray,
				self.model.diagramOpts);
		},
		/**
		 * Initializes the mutation view for the current mutation data.
		 * Use this function if you want to have a default view of mutation
		 * details composed of different backbone views (by default params).
		 *
		 * If you want to have more customized components, it is better
		 * to initialize all the component separately.
		 *
		 * @param container     target container selector for the main view
		 * @param cases         array of case ids (samples)
		 * @param diagramOpts   [optional] mutation diagram options
		 * TODO table options?
		 */
		_initDefaultView: function(container, cases, diagramOpts)
		{
			var self = this;

			// check if there is mutation data
			if (self.model.mutations.length == 0)
			{
				// display information if no data is available
				// TODO also factor this out as a backbone template?
				container.html(
					"<p>There are no mutation details available for the gene set entered.</p>" +
					"<br><br>");
			}
			else
			{
				// init main view for each gene
				for (var key in self.util.getMutationGeneMap())
				{
					// TODO also factor this out to a backbone template?
					container.append("<div id='mutation_details_" + key +"'></div>");
					self._initView(key, cases, diagramOpts);
				}
			}
		},
	    /**
		 * Initializes mutation view for the given gene and cases.
		 *
		 * @param gene          hugo gene symbol
	     * @param cases         array of case ids (samples)
	     * @param diagramOpts   [optional] mutation diagram options
	     * TODO table options?
		 */
		_initView: function(gene, cases, diagramOpts)
		{
			var self = this;
			var mutationMap = self.util.getMutationGeneMap();

			// callback function to init view after retrieving
			// sequence information.
			var init = function(response)
			{
				// calculate somatic & germline mutation rates
				var mutationCount = self.util.countMutations(gene, cases);
				// generate summary string for the calculated mutation count values
				var summary = self.util.generateSummary(mutationCount);

				// prepare data for mutation view
				var mutationInfo = {geneSymbol: gene,
					mutationSummary: summary,
					uniprotId : response.identifier};

				// reset the loader image
				self.$el.find("#mutation_details_loader").empty();

				// init the view
				var mainView = new MainMutationView({
					el: "#mutation_details_" + gene,
					model: mutationInfo});

				mainView.render();

				// draw mutation diagram
				var diagram = self._drawMutationDiagram(
						gene, mutationMap[gene], response, diagramOpts);

				var pdfButton = mainView.$el.find(".diagram-to-pdf");
				var svgButton = mainView.$el.find(".diagram-to-svg");
				var toolbar = mainView.$el.find(".mutation-diagram-toolbar");

				// check if diagram is initialized successfully.
				// if not, disable any diagram related functions
				if (!diagram)
				{
					console.log("Error initializing mutation diagram: %s", gene);
					toolbar.hide();
				}

				// helper function to trigger submit event for the svg and pdf button clicks
				var submitForm = function(alterFn, diagram, formClass)
				{
					// alter diagram to have the desired output
					alterFn(diagram);

					// convert svg content to string
					var xmlSerializer = new XMLSerializer();
					var svgString = xmlSerializer.serializeToString(diagram.svg[0][0]);

					// restore previous settings after generating xml string
					alterFn(diagram, true);

					// temp hack for shifted axis values (see also loadSVG function in plots_tab.jsp)
//					svgString = svgString.replace(/<text y="9" x="0" dy=".71em"/g,
//						"<text y=\"19\" x=\"0\" dy=\".71em\"");
//					svgString = svgString.replace(/<text x="-9" y="0" dy=".32em"/g,
//						"<text x=\"-9\" y=\"3\" dy=\".32em\"");

					// set actual value of the form element (svgelement)
					var form = mainView.$el.find("." + formClass);
					form.find('input[name="svgelement"]').val(svgString);

					// submit form
					form.submit();
				};

				//add listener to the svg button
				svgButton.click(function (event) {
					// TODO setting & rolling back diagram values (which may not be safe)
					// helper function to adjust SVG for file output
					var alterDiagramForSvg = function(diagram, rollback)
					{
						var topLabel = gene;

						if (rollback)
						{
							topLabel = "";
						}

						// adding a top left label (to include a label in the file)
						diagram.updateTopLabel(topLabel);
					};

					// submit svg form
					submitForm(alterDiagramForSvg, diagram, "svg-to-file-form");
				});

				// add listener to the pdf button
				pdfButton.click(function (event) {
					// TODO setting & rolling back diagram values (which may not be safe)
					// helper function to adjust SVG for PDF output
					var alterDiagramForPdf = function(diagram, rollback)
					{
						var topLabel = gene;
						var xShift = 8;
						var yShift = 3;

						if (rollback)
						{
							topLabel = "";
							xShift = -1 * xShift;
							yShift = -1 * yShift;
						}

						var xLabels = diagram.svg
							.select(".mut-dia-x-axis")
							.selectAll(".tick")
							.selectAll("text");

						var yLabels = diagram.svg
							.select(".mut-dia-y-axis")
							.selectAll(".tick")
							.selectAll("text");

						// adding a top left label (to include a label in PDF)
						diagram.updateTopLabel(topLabel);

						// shifting axis tick labels a little bit because of
						// a bug in the PDF converter library (this is a hack!)
						var xy = parseInt(xLabels.attr("y"));
						var yy = parseInt(yLabels.attr("y"));

						xLabels.attr("y", xy + xShift);
						yLabels.attr("y", yy + yShift);
					};

					// submit pdf form
					submitForm(alterDiagramForPdf, diagram, "svg-to-pdf-form");
				});

				// TODO draw mutation table
			};

			// TODO cache sequence for each gene (implement another class for this)?
			$.getJSON("getPfamSequence.json", {geneSymbol: gene}, init);
		},
		/**
		 * Initializes the mutation diagram view.
		 *
		 * @param gene          hugo gene symbol
		 * @param mutationData  mutation data (array of JSON objects)
		 * @param sequenceData  sequence data (as a JSON object)
		 * @param options       [optional] diagram options
		 */
		_drawMutationDiagram: function(gene, mutationData, sequenceData, options)
		{
			// use defaults if no options provided
			if (!options)
			{
				options = {};
			}

			// do not draw the diagram if there is a critical error with
			// the sequence data
			if (sequenceData.sequenceLength == "" ||
			    sequenceData.sequenceLength <= 0)
			{
				// return null to indicate an error
				return null;
			}

			// overwrite container in any case (for consistency with the default view)
			options.el = "#mutation_diagram_" + gene.toUpperCase();

			// create a backbone collection for the given data
			var mutationColl = new MutationCollection(mutationData);

			var mutationDiagram = new MutationDiagram(gene, options, mutationColl);
			mutationDiagram.initDiagram(sequenceData);

			return mutationDiagram;
		}
	});

	//TODO views & templates for mutation table
</script>