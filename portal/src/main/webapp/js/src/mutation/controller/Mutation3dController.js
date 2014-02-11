/**
 * Controller class for the 3D Mutation view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param mutationDetailsView   a MutationDetailsView instance
 * @param mut3dVisView          a Mutation3dVisView instance
 * @param mut3dView             a Mutation3dView instance
 * @param mutationDiagram       a MutationDiagram instance
 * @param geneSymbol            hugo gene symbol (string value)
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dController = function (
	mutationDetailsView, mut3dVisView, mut3dView, mutationDiagram, geneSymbol)
{
	// we cannot get pdb panel view as a constructor parameter,
	// since it is initialized after initializing the controller
	var pdbPanelView = null;

	function init()
	{
		// add listeners to the custom event dispatcher of the diagram

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.ALL_LOLLIPOPS_DESELECTED,
			allDeselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_DESELECTED,
			diagramDeselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_SELECTED,
			diagramSelectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_UPDATED,
			diagramUpdateHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_RESET,
			diagramResetHandler);

		// add listeners for the mutation 3d view
		mut3dView.dispatcher.on(
			MutationDetailsEvents.PDB_PANEL_INIT,
			pdbPanelInitHandler);

		mut3dView.addInitCallback(mut3dInitHandler);

		// add listeners for the mutation 3d vis view
		mut3dVisView.dispatcher.on(
			MutationDetailsEvents.VIEW_3D_PANEL_CLOSED,
			view3dPanelCloseHandler);

		// add listeners for the mutation details view
		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.GENE_TAB_SELECTED,
			geneTabSelectHandler);
	}

	function geneTabSelectHandler(gene)
	{
//		var sameGene = (gene.toLowerCase() == geneSymbol.toLowerCase());
//		var reset = sameGene &&
//		            mut3dView &&
//					mut3dVisView &&
//		            mut3dVisView.isVisible();

		// reset if the 3D panel is visible,
		// and selected gene is this controller's gene
//		if (reset)
//		{
//			// TODO instead of reset, restore to previous config:
//			// may need to update resetView and loadDefaultChain methods
//			// (see issue #456)
//			mut3dView.resetView();
//		}

		// just hide the 3D view for now

		if (mut3dVisView &&
		    mut3dVisView.isVisible())
		{
			mut3dVisView.hideView();
		}
	}

	function view3dPanelCloseHandler()
	{
		// hide the corresponding pdb panel view
		if (pdbPanelView)
		{
			pdbPanelView.hideView();
		}
	}

	function pdbPanelInitHandler(panelView)
	{
		pdbPanelView = panelView;

		// we cannot add pdbPanel listeners at actual init of the controller,
		// since pdb panel is conditionally initialized at a later point

		// add listeners to the custom event dispatcher of the pdb panel
		panelView.pdbPanel.dispatcher.on(
			MutationDetailsEvents.CHAIN_SELECTED,
			chainSelectHandler);
	}

	function mut3dInitHandler(event)
	{
		mut3dView.resetView();

		if (mut3dVisView != null)
		{
			mut3dVisView.maximizeView();
		}
	}

	function chainSelectHandler(element)
	{
		// TODO ideally, we should queue every script call in JSmolWrapper,
		// ...and send request to the frame one by one, but it is complicated

		// calling another script immediately after updating the view
		// does not work, so register a callback for update function
		var callback = function() {
			// focus view on already selected diagram location
			if (mutationDiagram.isHighlighted())
			{
				// highlight 3D residues for the initially selected diagram elements
				if (!mut3dVisView.highlightView(getSelectedPileups(), true))
				{
					mut3dVisView.showResidueWarning();
				}
			}
		};

		// update view with the selected chain data
		var datum = element.datum();
		mut3dVisView.updateView(geneSymbol, datum.pdbId, datum.chain, callback);
	}

	function diagramResetHandler()
	{
		if (mut3dVisView && mut3dVisView.isVisible())
		{
			// reset all previous visualizer filters
			mut3dVisView.refreshView();
		}
	}

	function diagramUpdateHandler()
	{
		// refresh 3d view with filtered positions
		if (mut3dVisView && mut3dVisView.isVisible())
		{
			mut3dVisView.refreshView();
		}
	}

	function allDeselectHandler()
	{
		if (mut3dVisView && mut3dVisView.isVisible())
		{
			mut3dVisView.resetHighlight();
			mut3dVisView.hideResidueWarning();
		}
	}

	function diagramDeselectHandler(datum, index)
	{
		// check if the diagram is still highlighted
		if (mutationDiagram.isHighlighted())
		{
			// reselect with the reduced selection
			diagramSelectHandler();
		}
		else
		{
			// no highlights (all deselected)
			allDeselectHandler();
		}
	}

	function diagramSelectHandler(datum, index)
	{
		// highlight the corresponding residue in 3D view
		if (mut3dVisView && mut3dVisView.isVisible())
		{
			// highlight view for the selected pileups
			if (mut3dVisView.highlightView(getSelectedPileups(), true) > 0)
			{
				mut3dVisView.hideResidueWarning();
			}
			// display a warning message if there is no corresponding residue
			else
			{
				mut3dVisView.showResidueWarning();
			}
		}
	}

	/**
	 * Retrieves the pileup data from the selected mutation diagram
	 * elements.
	 *
	 * @return {Array} an array of Pileup instances
	 */
	function getSelectedPileups()
	{
		var pileups = [];

		// get mutations for all selected elements
		_.each(mutationDiagram.getSelectedElements(), function (ele, i) {
			pileups = pileups.concat(ele.datum());
		});

		return pileups;
	}

	init();
};
