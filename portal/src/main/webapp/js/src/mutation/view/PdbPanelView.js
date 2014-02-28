/**
 * PDB Panel View.
 *
 * This view is designed to function in parallel with the 3D visualizer.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: hugo gene symbol,
 *                   pdbColl: collection of PdbModel instances,
 *                   pdbProxy: pdb data proxy},
 *           diagram: [optional] reference to the MutationDiagram instance
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PdbPanelView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};
		this.collapseTimer = null;
		this.expandTimer = null;
	},
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var template = _.template($("#pdb_panel_view_template").html(), {});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// init pdb panel
		self.pdbPanel = self._initPdbPanel();

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;

		// hide view initially
		self.$el.hide();

		// format panel controls

		var expandButton = self.$el.find(".expand-collapse-pdb-panel");

		// hide expand button if there is no more chain to show
		if (!self.pdbPanel.hasMoreChains())
		{
			expandButton.hide();
		}
		else
		{
			expandButton.button({
				icons: {primary: "ui-icon-triangle-2-n-s"},
				text: false});
			expandButton.css({width: "300px", height: "12px"});

			expandButton.click(function() {
				self.pdbPanel.toggleHeight();
			});
		}

		self.$el.mouseenter(function(evt) {
			self.autoExpand();
		});

		self.$el.mouseleave(function(evt) {
			self.autoCollapse();
		});
	},
	hideView: function()
	{
		var self = this;
		self.$el.slideUp();
	},
	showView: function()
	{
		var self = this;
		self.$el.slideDown();
	},
	/**
	 * Selects the default pdb and chain for the 3D visualizer.
	 * Default chain is one of the chains in the first row.
	 */
	selectDefaultChain: function()
	{
		var self = this;
		var panel = self.pdbPanel;
		var gChain = panel.getDefaultChainGroup();

		// clear previous timer
		if (self.collapseTimer != null)
		{
			clearTimeout(self.collapseTimer);
		}

		// restore to full view
		panel.restoreToFull(function() {
			// highlight the default chain
			panel.highlight(gChain);
		});

		// TODO call this in the controller?
		// initiate auto-collapse
		self.autoCollapse();
	},
	/**
	 * Selects the given pdb and chain for the 3D visualizer.
	 *
	 * @param pdbId     pdb to be selected
	 * @param chainId   chain to be selected
	 */
	selectChain: function(pdbId, chainId)
	{
		var self = this;
		var panel = self.pdbPanel;

		// clear previous timers
		self.clearTimers();

		// restore to original positions & highlight the chain
		panel.restoreChainPositions(function() {
			// expand the panel up to the level of the given chain
			panel.expandToChainLevel(pdbId, chainId);

			// get the chain group
			var gChain = panel.getChainGroup(pdbId, chainId);

			// highlight the chain group
			if (gChain)
			{
				panel.highlight(gChain);
			}

			// TODO call this in the controller?
			self.autoCollapse(0);
			// minimize to selected
			//panel.minimizeToHighlighted();
		});
	},
	/**
	 * Initializes the auto collapse process.
	 *
	 * @delay time to minimization
	 */
	autoCollapse: function(delay)
	{
		if (delay == null)
		{
			delay = 2000;
		}

		var self = this;
		var expandButton = self.$el.find(".expand-collapse-pdb-panel");

		// clear previous timers
		self.clearTimers();

		// set new timer
		self.collapseTimer = setTimeout(function() {
			self.pdbPanel.minimizeToHighlighted();
			expandButton.slideUp();
		}, delay);
	},
	/**
	 * Initializes the auto expand process.
	 *
	 * @delay time to minimization
	 */
	autoExpand: function(delay)
	{
		if (delay == null)
		{
			delay = 400;
		}

		var self = this;
		var expandButton = self.$el.find(".expand-collapse-pdb-panel");

		// clear previous timers
		self.clearTimers();

		// set new timer
		self.expandTimer = setTimeout(function() {
			self.pdbPanel.restoreToFull();

			if (self.pdbPanel.hasMoreChains())
			{
				expandButton.slideDown();
			}
		}, delay);
	},
	clearTimers: function()
	{
		var self = this;

		if (self.collapseTimer != null)
		{
			clearTimeout(self.collapseTimer);
		}

		if (self.expandTimer != null)
		{
			clearTimeout(self.expandTimer);
		}
	},
	/**
	 * Initializes the PDB chain panel.
	 *
	 * @return {MutationPdbPanel}   panel instance
	 */
	_initPdbPanel: function()
	{
		var self = this;
		var panel = null;

		var pdbColl = self.model.pdbColl;
		var pdbProxy = self.model.pdbProxy;
		var mutationDiagram = self.options.diagram;

		if (mutationDiagram != null)
		{
			var xScale = mutationDiagram.xScale;

			// set margin same as the diagram margin for correct alignment with x-axis
			var options = {el: self.$el.find(".mutation-pdb-panel-container"),
				marginLeft: mutationDiagram.options.marginLeft,
				marginRight: mutationDiagram.options.marginRight};

			// init panel
			panel = new MutationPdbPanel(options, pdbColl, pdbProxy, xScale);
			panel.init();
		}

		return panel;
	}
});
