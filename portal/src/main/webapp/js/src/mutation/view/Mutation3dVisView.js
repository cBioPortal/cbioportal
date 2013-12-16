/**
 * Actual 3D Visualizer view. This view is designed to contain the 3D
 * structure visualizer app and its control buttons.
 *
 * options: {el: [target container],
 *           parentEl: [parent container],
 *           mut3dVis: [optional] reference to the Mutation3dVis instance,
 *           pdbProxy: [optional] PDB data proxy
 *          }
 */
var Mutation3dVisView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};
	},
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

		// click listener for the close icon of the 3d vis container
		var closeHandler = function() {
			// hide the vis pane
			if (mut3dVis != null)
			{
				mut3dVis.hide();
			}

			// also hide all pdb panel views
			self.options.parentEl.find(".mutation-pdb-panel-view").slideUp();
		};

		// add listeners to panel (header) buttons

		self.$el.find(".mutation-3d-close").click(closeHandler);

		self.$el.find(".mutation-3d-minimize").click(function(){
			if (mut3dVis != null)
			{
				mut3dVis.toggleSize();
			}
		});

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

		// TODO chosen is sometimes problematic in Firefox when overflow is hidden...
		styleMenu.chosen({width: 120, disable_search: true});

		styleMenu.change(function(){
			var selected = $(this).val();

			if (mut3dVis != null)
			{
				mut3dVis.changeStyle(selected);
			}
		});

		// zoom buttons

		var zoomIn = self.$el.find(".mutation-3d-zoomin");
		var zoomOut = self.$el.find(".mutation-3d-zoomout");
		var zoomActual = self.$el.find(".mutation-3d-zoomactual");

		var zoomButtonSet = self.$el.find(".mutation-3d-button");
		zoomButtonSet.tipTip();

		// TODO add also tips
		zoomIn.click(function() {
			if (mut3dVis != null)
			{
				mut3dVis.zoomIn();
			}
		});

		zoomOut.click(function() {
			if (mut3dVis != null)
			{
				mut3dVis.zoomOut();
			}
		});

		zoomActual.click(function() {
			if (mut3dVis != null)
			{
				mut3dVis.zoomActual();
			}
		});


		// TODO this is an access to a global div out of this view's template...
		$("#tabs").bind("tabsactivate", function(event, ui){
			closeHandler();
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

			// store pdb id and chain for future reference
			self.pdbId = pdbId;
			self.chain = chain;
		};

		var infoCallback = function(pdbInfo) {
			var model = {pdbId: pdbId,
				chainId: chain.chainId,
				pdbInfo: ""};

			if (pdbInfo)
			{
				model.pdbInfo = pdbInfo;
			}

			// init info view
			var infoView = new Mutation3dVisInfoView(
				{el: self.$el.find(".mutation-3d-info"), model: model});
			infoView.render();

			// update positionMap for the chain
			// (retrieve data only once)
			pdbProxy.getPositionMap(geneSymbol, chain, mapCallback);
		};

		pdbProxy.getPdbInfo(pdbId, infoCallback);
	},
	/**
	 * Refreshes (reloads) the 3D visualizer for the last pdb id
	 * and chain.
	 */
	refreshView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// just reload with the last known pdb id and chain
		mut3dVis.reload(self.pdbId, self.chain);
	},
	/**
	 * Minimizes the 3D visualizer panel.
	 */
	minimizeView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		mut3dVis.minimize();
	},
	/**
	 * Restores the 3D visualizer panel to its full size.
	 */
	maximizeView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		mut3dVis.maximize();
	},
	isVisible: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		return mut3dVis.isVisible();
	},
	/**
	 * Focuses the 3D visualizer on the residue
	 * corresponding to the given pileup of mutations.
	 *
	 * If this function is invoked without a parameter,
	 * then resets the focus to the default state.
	 *
	 * @param pileup    Pileup instance
	 */
	focusView: function(pileup)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		if (pileup)
		{
			mut3dVis.focusOn(pileup);
		}
		else
		{
			mut3dVis.resetFocus();
		}
	}
});