<script type="text/template" id="default_mutation_details_template">
	<div id='mutation_details_loader'>
		<img src='images/ajax-loader.gif'/>
	</div>
</script>

<script type="text/template" id="mutation_view_template">
	<h4>{{geneSymbol}}: {{mutationSummary}}</h4>
	<div id='uniprot_link_{{geneSymbol}}' class='diagram_uniprot_link'>
		<a href='http://www.uniprot.org/uniprot/{{uniprotId}}' target='_blank'>{{uniprotId}}</a>
	</div>
	<div id='mutation_diagram_{{geneSymbol}}' class='mutation-diagram-container'></div>
	<div id='mutation_table_{{geneSymbol}}'>
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
	 */
	var MainMutationView = Backbone.View.extend({
		render: function() {
			// pass variables in using Underscore.js template
			var variables = { geneSymbol: this.model.geneSymbol,
				mutationSummary: this.model.mutationSummary,
				uniprotId: this.model.uniprotId};

			// compile the template using underscore
			var template = _.template( $("#mutation_view_template").html(), variables);

			// load the compiled HTML into the Backbone "el"
			this.$el.html(template);
		}
	});

	/**
	 * Default mutation details view for the entire mutation details tab.
	 * Creates a separate MainMutationView (another Backbone view) for each gene.
	 */
	var MutationDetailsView = Backbone.View.extend({
		render: function() {
			var self = this;

			self.util = new MutationDetailsUtil(
					new MutationCollection(self.model.mutations));

			// compile the template using underscore
			var template = _.template( $("#default_mutation_details_template").html());

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
				self._drawMutationDiagram(gene, mutationMap[gene], response, diagramOpts);
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

			// overwrite container in any case (for consistency with the default view)
			options.el = "#mutation_diagram_" + gene.toUpperCase();

			// create a backbone collection for the given data
			var mutationColl = new MutationCollection(mutationData);

			var mutationDiagram = new MutationDiagram(gene, options, mutationColl);
			mutationDiagram.initDiagram(sequenceData);
		}
	});

	//TODO views & templates for mutation table
</script>