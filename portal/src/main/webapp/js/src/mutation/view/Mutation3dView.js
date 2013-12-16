/**
 * 3D visualizer controls view.
 *
 * This view is designed to provide controls to initialize, show or hide
 * the actual 3D visualizer panel. PDB chain panel, which is supposed to
 * be displayed just below the mutation diagram, is initialized by this view.
 *
 * IMPORTANT NOTE: This view does not initialize the actual 3D visualizer.
 * 3D visualizer is a global instance bound to MainMutationView
 * and it is maintained by Mutation3dVisView.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: hugo gene symbol,
 *                   uniprotId: uniprot identifier for this gene,
 *                   pdbProxy: pdb data proxy}
 *           mut3dVisView: [optional] reference to the Mutation3dVisView instance,
 *           diagram: [optional] reference to the MutationDiagram instance
 *          }
 */
var Mutation3dView = Backbone.View.extend({
	render: function()
	{
		var self = this;
		var gene = self.model.geneSymbol;

		// compile the template using underscore
		var template = _.template(
				$("#mutation_3d_view_template").html(), {});

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);

		// format after rendering
		this.format();
	},
	format: function()
	{
		var self = this;

		// add click listener for the 3d visualizer initializer
		self.$el.find(".mutation-3d-vis").click(self._getClickHandlerFn());
	},
	resetView: function()
	{
		var self = this;

		// call the click handler function to reset the 3D view
		var fn = self._getClickHandlerFn();
		fn();
	},
	/**
	 * Returns the handler function for the click action on the initializer.
	 *
	 * @return {Function}   handler function to init the view
	 */
	_getClickHandlerFn: function()
	{
		var self = this;

		// actual event handler (listener) function
		var handler = function() {
			var gene = self.model.geneSymbol;
			var uniprotId = self.model.uniprotId;
			var vis = self.options.mut3dVisView;
			var panel = self.pdbPanelView;
			var pdbProxy = self.model.pdbProxy;

			var initView = function(pdbColl)
			{
				// init pdb panel view if not initialized yet
				if (panel == undefined)
				{
					var panelOpts = {el: "#mutation_pdb_panel_view_" + gene.toUpperCase(),
						model: {geneSymbol: gene, pdbColl: pdbColl},
						mut3dVisView: self.options.mut3dVisView,
						diagram: self.options.diagram};

					var pdbPanelView = new PdbPanelView(panelOpts);
					panel = self.pdbPanelView = pdbPanelView;

					pdbPanelView.render();
				}

				if (vis != null &&
				    panel != null &&
				    pdbColl.length > 0)
				{
					panel.showView();

					// reload the visualizer content with the default pdb and chain
					panel.loadDefaultChain();
				}
			};

			// init view with the pdb data
			pdbProxy.getPdbData(uniprotId, initView);
		};

		return handler;
	}
});
