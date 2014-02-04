/**
 * Default mutation details view for the entire mutation details tab.
 * Creates a separate MainMutationView (another Backbone view) for each gene.
 *
 * options: {el: [target container],
 *           model: {mutationProxy: [mutation data proxy],
 *                   sampleArray: [list of case ids as an array of strings],
 *                   diagramOpts: [mutation diagram options -- optional],
 *                   tableOpts: [mutation table options -- optional]}
 *           mut3dVis: [optional] reference to the 3d structure visualizer
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};
	},
	render: function() {
		var self = this;

		// init tab view flags (for each gene)
		self.geneTabView = {};

		var content = self._generateContent();

		// TODO make the image customizable?
		var variables = {loaderImage: "images/ajax-loader.gif",
			listContent: content.listContent,
			mainContent: content.mainContent};

		// compile the template using underscore
		var template = _.template(
			$("#default_mutation_details_template").html(),
			variables);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		if (self.model.mutationProxy.hasData())
		{
			self._initDefaultView(self.model.sampleArray,
                    self.model.diagramOpts,
					self.model.tableOpts);
		}

		// format after render
		self.format();
	},
	/**
	 * Formats the contents of the view after the initial rendering.
	 */
	format: function()
	{
		var self = this;
		var container3d = self.$el.find("#mutation_3d_container");

		// hide loader image
		self.$el.find("#mutation_details_loader").hide();

		if (self.model.mutationProxy.hasData())
		{
			var mainContent = self.$el.find("#mutation_details_content");
			mainContent.tabs();
			mainContent.tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
			mainContent.tabs("option", "active", 0);
			self.$el.find(".mutation-details-tabs-ref").tipTip(
				{defaultPosition: "bottom", delay:"100", edgeOffset: 10, maxWidth: 200});
		}

		// init 3D view if the visualizer is available
		if (self.options.mut3dVis)
		{
			var mutation3dVisView = new Mutation3dVisView(
					{el: container3d,
					parentEl: self.$el,
					mut3dVis: self.options.mut3dVis,
					pdbProxy: self.pdbProxy,
					mutationProxy: self.model.mutationProxy});

			mutation3dVisView.render();

			// update reference to the 3d vis view
			self.mut3dVisView = mutation3dVisView;
		}
		// if no visualizer, hide the 3D vis container
		else
		{
		   $(container3d).hide();
		}
	},
	/**
	 * Refreshes the genes tab.
	 * (Intended to fix a resize problem with ui.tabs.paging plugin)
	 */
	refreshGenesTab: function()
	{
		// tabs("refresh") is problematic...
//		var self = this;
//		var mainContent = self.$el.find("#mutation_details_content");
//		mainContent.tabs("refresh");

        // just trigger the window resize event,
        // rest is handled by the resize handler in ui.tabs.paging plugin.
		// it would be better to directly call the resize handler of the plugin,
		// but the function doesn't have public access...
		$(window).trigger('resize');
	},
	/**
	 * Generates the content structure by creating div elements for each
	 * gene.
	 *
	 * @return {Object} content backbone with div elements for each gene
	 */
	_generateContent: function()
	{
		var self = this;
		var mainContent = "";
		var listContent = "";

		// check if there is available mutation data
		if (!self.model.mutationProxy.hasData())
		{
			// display information if no data is available
			mainContent = _.template($("#default_mutation_details_info_template").html(), {});
		}
		else
		{
			// create a div for for each gene
			_.each(self.model.mutationProxy.getGeneList(), function(gene, idx) {
				mainContent += _.template(
					$("#default_mutation_details_main_content_template").html(),
						{loaderImage: "images/ajax-loader.gif",
						geneSymbol: gene});

				listContent += _.template(
					$("#default_mutation_details_list_content_template").html(),
					{geneSymbol: gene});
			});
		}

		return {mainContent: mainContent,
			listContent: listContent};
	},
	/**
	 * Initializes the mutation view for the current mutation data.
	 * Use this function if you want to have a default view of mutation
	 * details composed of different backbone views (by default params).
	 *
	 * If you want to have more customized components, it is better
	 * to initialize all the component separately.
	 *
	 * @param cases         array of case ids (samples)
	 * @param diagramOpts   [optional] mutation diagram options
	 * @param tableOpts     [optional] mutation table options
	 */
	_initDefaultView: function(cases, diagramOpts, tableOpts)
	{
		var self = this;

		var genes = self.model.mutationProxy.getGeneList();

		self.pdbProxy = new PdbDataProxy(
				self.model.mutationProxy.getMutationUtil());

		var contentSelector = self.$el.find("#mutation_details_content");

		// reset all previous tabs related listeners (if any)
		contentSelector.bind('tabscreate', false);
		contentSelector.bind('tabsactivate', false);

		// init view for the first gene only
		contentSelector.bind('tabscreate', function(event, ui) {
			self._initView(genes[0], cases, diagramOpts, tableOpts);
		});

		// init other views upon selecting the corresponding tab
		contentSelector.bind('tabsactivate', function(event, ui) {
			// TODO using index() causes problems with ui.tabs.paging plugin
			// note: ui.index is replaced with ui.newTab.index() after jQuery 1.9
			//var gene = genes[ui.newTab.index()];

			// get the gene name directly from the html content
			var gene = ui.newTab.text().trim();

			// init view for the selected tab (if not initialized before)
			if (self.geneTabView[gene] == undefined)
			{
				// init view (self.geneTabView mapping is updated within this function)
				self._initView(gene, cases, diagramOpts, tableOpts);
			}
			// check if 3D panel is visible
			else if (self.mut3dVisView &&
				self.mut3dVisView.isVisible())
			{
				// reset the 3d vis content for the current tab
				if (self.geneTabView[gene].mut3dView)
				{
					self.geneTabView[gene].mut3dView.resetView();
				}
			}
		});
	},
    /**
	 * Initializes mutation view for the given gene and cases.
	 *
	 * @param gene          hugo gene symbol
     * @param cases         array of case ids (samples)
     * @param diagramOpts   [optional] mutation diagram options
     * @param tableOpts     [optional] mutation table options
	 */
	_initView: function(gene, cases, diagramOpts, tableOpts)
	{
		var self = this;
		var mutationDiagram = null;
		var mainMutationView = null;
		var mutationData = null;
		var mutationUtil = self.model.mutationProxy.getMutationUtil();

		/**
		 * Updates the other components of the mutation view after each change
		 * in the mutation table. This maintains synchronizing between the table
		 * and other view components (diagram and 3d visualizer).
		 *
		 * @param tableSelector selector for the mutation table
		 */
		var syncWithMutationTable = function(tableSelector)
		{
			var mutationMap = mutationUtil.getMutationIdMap();
			var currentMutations = [];

			// add current mutations into an array
			var rows = tableSelector.find("tr");
			_.each(rows, function(element, index) {
				var mutationId = $(element).attr("id");

				if (mutationId)
				{
					var mutation = mutationMap[mutationId];

					if (mutation)
					{
						currentMutations.push(mutation);
					}
				}
			});

			// update mutation diagram with the current mutations
			if (mutationDiagram !== null)
			{
				var mutationData = new MutationCollection(currentMutations);
				mutationDiagram.updatePlot(mutationData);

				if (mutationDiagram.isFiltered())
				{
					// display info text
					mainMutationView.showFilterInfo();
				}
				else
				{
					// hide info text
					mainMutationView.hideFilterInfo();
				}
			}

			var view3d = self.mut3dVisView;

			// refresh 3d view with filtered positions
			if (view3d && view3d.isVisible())
			{
				view3d.refreshView();
			}
		};

		/**
		 * Add listeners to the diagram plot elements.
		 *
		 * @param diagram   mutation diagram
		 * @param tableView mutation table view
		 * @param view3d    3D mutation visualizer view
		 */
		var addPlotListeners = function(diagram, tableView, view3d)
		{
			diagram.addListener(".mut-dia-data-point", "mouseout", function() {
				// remove all highlights
				tableView.clearHighlights();
			});

			diagram.addListener(".mut-dia-data-point", "mouseover", function(datum, index) {
				// highlight mutations for the provided mutations
				tableView.highlight(datum.mutations);
			});

			diagram.addListener(".mut-dia-data-point", "click", function(datum, index) {
				// just ignore the action if the diagram is already in a graphical transition.
				// this is to prevent inconsistency due to fast clicks on the diagram.
				if (diagram.isInTransition())
				{
					return;
				}

				// if already highlighted, remove highlight on a second click
				if (diagram.isHighlighted(this))
				{
					// remove highlight for the target circle
					diagram.removeHighlight(this);

					// remove all table highlights
					tableView.clearHighlights();

					// roll back the table to its previous state
					// (to the last state when a manual filtering applied)
					tableView.rollBack();

					// hide filter reset info
					if (!diagram.isFiltered())
					{
						mainMutationView.hideFilterInfo();
					}

					// reset highlight of the 3D view
					if (view3d && view3d.isVisible())
					{
						view3d.removeHighlight(datum);
						view3d.hideResidueWarning();
					}
				}
				else
				{
					// remove all table & diagram highlights
					diagram.clearHighlights();
					tableView.clearHighlights();

					// highlight the target circle on the diagram
					diagram.highlight(this);

					// filter table for the given mutations
					tableView.filter(datum.mutations);

					// show filter reset info
					mainMutationView.showFilterInfo();

					// highlight the corresponding residue in 3D view
					if (view3d && view3d.isVisible())
					{
						// highlight view for the selected datum
						// TODO for now removing previous highlights,
						// ...we need to change this to allow multiple selection
						if (view3d.highlightView(datum, true))
						{
							view3d.hideResidueWarning();
						}
						// display a warning message if there is no corresponding residue
						else
						{
							view3d.showResidueWarning();
						}
					}
				}
			});

			// add listener to the diagram background to remove highlights
			diagram.addListener(".mut-dia-background", "click", function(datum, index) {
				// just ignore the action if the diagram is already in a graphical transition.
				// this is to prevent inconsistency due to fast clicks on the diagram.
				if (diagram.isInTransition())
				{
					return;
				}

				// check if there is a highlighted circle
				// no action required if no circle is highlighted
				if (!diagram.isHighlighted())
				{
					return;
				}

				// remove all diagram highligts
				diagram.clearHighlights('circle');

				// remove all table highlights
				tableView.clearHighlights();

				// roll back the table to its previous state
				// (to the last state when a manual filtering applied)
				tableView.rollBack();

				// hide filter reset info
				if (!diagram.isFiltered())
				{
					mainMutationView.hideFilterInfo();
				}

				// reset highlight of the 3D view
				if (view3d && view3d.isVisible())
				{
					view3d.removeHighlight();
					view3d.hideResidueWarning();
				}
			});
		};

		// callback function to init view after retrieving
		// sequence information.
		var init = function(sequenceData)
		{
			// TODO sequenceData may be null for unknown genes...
			// get the first sequence from the response
			var sequence = sequenceData[0];

            var summary = "";

            if(cases.length > 0) {
                // calculate somatic & germline mutation rates
                var mutationCount = mutationUtil.countMutations(gene, cases);
                // generate summary string for the calculated mutation count values
                summary = mutationUtil.generateSummary(mutationCount);
            }

			// prepare data for mutation view
			var model = {geneSymbol: gene,
				mutationSummary: summary,
				uniprotId: sequence.metadata.identifier};

			// reset the loader image
			self.$el.find("#mutation_details_loader").empty();

			// init the main view
			var mainView = new MainMutationView({
				el: "#mutation_details_" + gene,
				model: model});

			mainView.render();

			// update the references after rendering the view
			mainMutationView = mainView;
			self.geneTabView[gene].mainMutationView = mainView;

			// draw mutation diagram
			var diagram = self._drawMutationDiagram(
					gene, mutationData, sequence, diagramOpts);

			// check if diagram is initialized successfully.
			if (diagram)
			{
				// init diagram toolbar
				mainView.initToolbar(diagram, gene);

				if (self.mut3dVisView)
				{
					// init the 3d view
					var view3d = new Mutation3dView({
						el: "#mutation_3d_" + gene,
						model: {uniprotId: sequence.metadata.identifier,
							geneSymbol: gene,
							pdbProxy: self.pdbProxy},
						mut3dVisView: self.mut3dVisView,
						diagram: diagram});

					view3d.render();

					// update reference for future use
					self.geneTabView[gene].mut3dView = view3d;

					// also reset (init) the 3D view if the 3D panel is already active
					if (self.mut3dVisView.isVisible())
					{
						view3d.resetView();
					}
				}
			}
			else
			{
				console.log("Error initializing mutation diagram: %s", gene);
			}

			// draw mutation table

			var mutationTableView = new MutationDetailsTableView(
					{el: "#mutation_table_" + gene,
					model: {geneSymbol: gene,
						mutations: mutationData,
						syncFn: syncWithMutationTable,
						tableOpts: tableOpts}});

			mutationTableView.render();

			// update reference after rendering the table
			mutationDiagram = diagram;

			// add default event listeners for the diagram
			addPlotListeners(diagram, mutationTableView, self.mut3dVisView);

			// init reset info text content for the diagram
			mainView.initResetFilterInfo(diagram, mutationTableView, self.mut3dVisView);
		};

		// get mutation data for the current gene
		self.model.mutationProxy.getMutationData(gene, function(data) {
			// init reference mapping
			self.geneTabView[gene] = {};

			// update mutation data reference
			mutationData = data;

			// display a message if there is no mutation data available for this gene
			if (mutationData == null ||
			    mutationData.length == 0)
			{
				self.$el.find("#mutation_details_" + gene).html(
					_.template($("#default_gene_mutation_details_info_template").html(), {}));
			}
			// get the sequence data for the current gene & init view
			else
			{
				$.getJSON("getPfamSequence.json", {geneSymbol: gene}, init);
			}
		});

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
		if (sequenceData["length"] == "" ||
		    parseInt(sequenceData["length"]) <= 0)
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
