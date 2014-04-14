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

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);

		// init main controller
		this.controller = new MutationDetailsController(this,
			this.model.mutationProxy,
			this.model.sampleArray,
		    this.model.diagramOpts,
		    this.model.tableOpts,
		    this.options.mut3dVis);
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
			self._initDefaultView();
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

		if (self.model.mutationProxy.hasData())
		{
			var mainContent = self.$el.find(".mutation-details-content");
			mainContent.tabs();
			mainContent.tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
			mainContent.tabs("option", "active", 0);
			self.$el.find(".mutation-details-tabs-ref").tipTip(
				{defaultPosition: "bottom", delay:"100", edgeOffset: 10, maxWidth: 200});
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
//		var mainContent = self.$el.find(".mutation-details-content");
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
							geneSymbol: gene,
							geneId: cbio.util.safeProperty(gene)});

				listContent += _.template(
					$("#default_mutation_details_list_content_template").html(),
					{geneSymbol: gene,
						geneId: cbio.util.safeProperty(gene)});
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
	 */
	_initDefaultView: function()
	{
		var self = this;

		var contentSelector = self.$el.find(".mutation-details-content");

		// reset all previous tabs related listeners (if any)
		contentSelector.bind('tabscreate', false);
		contentSelector.bind('tabsactivate', false);

		// init view for the first gene only
		contentSelector.bind('tabscreate', function(event, ui) {
			// hide loader image
			self.$el.find(".mutation-details-loader").hide();

			// trigger corresponding event
			self.dispatcher.trigger(
				MutationDetailsEvents.GENE_TABS_CREATED);
		});

		// init other views upon selecting the corresponding tab
		contentSelector.bind('tabsactivate', function(event, ui) {
			// note: ui.index is replaced with ui.newTab.index() after jQuery 1.9
			//var gene = genes[ui.newTab.index()];

			// using index() causes problems with ui.tabs.paging plugin,
			// get the gene name directly from the html content
			var gene = ui.newTab.text().trim();

			// trigger corresponding event
			self.dispatcher.trigger(
				MutationDetailsEvents.GENE_TAB_SELECTED,
				gene);
		});
	}
});
