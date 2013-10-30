/**
 * Actual 3D Visualizer view. This view is designed to contain the 3D
 * structure visualizer app and its control buttons.
 *
 * options: {el: [target container],
 *           parentEl: [parent container],
 *           mut3dVis: [optional] reference to the Mutation3dVis instance
 *          }
 */
var Mutation3dVisView = Backbone.View.extend({
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var template = _.template(
				$("#mutation_3d_vis_template").html(), {});

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);

		// format after rendering
		this.format();
	},
	format: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// initially hide the 3d visualizer container
		var container3d = self.$el;
		container3d.hide();

		// update the container of 3d visualizer
		if (mut3dVis != null)
		{
			mut3dVis.updateContainer(container3d);
		}

		// add click listener to the close icon of the 3d vis container
		var closeHandler = function() {
			// hide the vis pane
			if (mut3dVis != null)
			{
				mut3dVis.hide();
			}

			// also hide all pdb panel views
			self.options.parentEl.find(".mutation-pdb-panel-view").hide();
		};

		self.$el.find(".mutation-3d-close").click(closeHandler);

		// format toolbar elements

		// spin toggle
		var spinChecker = self.$el.find(".mutation-3d-spin");

		spinChecker.change(function(){
			if (mut3dVis != null)
			{
				mut3dVis.toggleSpin();
			}
		});

		// style selection menu
		var styleMenu = self.$el.find(".mutation-3d-style-select");

		styleMenu.chosen({width: 120, disable_search: true});
		styleMenu.change(function(){
			var selected = $(this).val();

			if (mut3dVis != null)
			{
				mut3dVis.changeStyle(selected);
			}

		});

		// TODO this is an access to a global div out of this view's template...
		$("#tabs").bind("tabsselect", function(event, ui){
			// close the vis panel only if the selected tab is one of the main tabs
			// (i.e.: do not close panel if a gene tab selected)
			if (ui.tab.className != "mutation-details-tabs-ref")
			{
				closeHandler();
			}
		});
	},
	/**
	 * Updates the 3D visualizer content for the given gene,
	 * pdb id, and chain.
	 *
	 * @param geneSymbol    hugo gene symbol
	 * @param pdbId         pdb id
	 * @param chain         PdbChainModel instance
	 */
	updateView: function(geneSymbol, pdbId, chain)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;
		var pdbProxy = self.options.pdbProxy;

		var mapCallback = function(positionMap) {
			// update position map of the chain
			chain.positionMap = positionMap;



			// reload the selected pdb and chain data
			mut3dVis.show();
			mut3dVis.reload(pdbId, chain);
		};

		var infoCallback = function(pdbInfo) {
			var infoVars = {pdbId: pdbId,
				chainId: chain.chainId,
				pdbInfo: ""};

			if (pdbInfo)
			{
				infoVars.pdbInfo = pdbInfo;
			}

			// TODO define another backbone view for this template
			var infoTemplate = _.template(
				$("#mutation_3d_vis_info_template").html(), infoVars);

			self.$el.find(".mutation-3d-info").html(infoTemplate);

			// update positionMap for the chain
			// (retrieve data only once)
			pdbProxy.getPositionMap(geneSymbol, chain, mapCallback);
		};

		pdbProxy.getPdbInfo(pdbId, infoCallback);
	},
	isVisible: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		return mut3dVis.isVisible();
	},
	focusView: function(pileup)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		mut3dVis.focusOn(pileup);
	}
});