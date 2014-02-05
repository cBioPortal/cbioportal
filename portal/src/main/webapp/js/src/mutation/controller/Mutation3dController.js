/**
 * Controller class for the 3D Mutation view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param mut3dVisView      a Mutation3dVisView instance
 * @param mut3dView         a Mutation3dView instance
 * @param mutationDiagram   a MutationDiagram instance
 * @param geneSymbol        hugo gene symbol (string value)
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dController = function (mut3dVisView, mut3dView, mutationDiagram, geneSymbol)
{
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
	}

	function pdbPanelInitHandler(pdbPanel)
	{
		// we cannot add pdbPanel listeners at actual init of the controller,
		// since pdb panel is conditionally initialized at a later point

		// add listeners to the custom event dispatcher of the pdb panel
		pdbPanel.dispatcher.on(
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
			if (mut3dVisView.isHighlighted())
			{
				var selected = mut3dVisView.getSelectedElements();

				// TODO assuming there is only one selected element
				// ... we need to update this part for multiple selection
				if (!mut3dVisView.highlightView(selected[0].datum(), true))
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
			mut3dVisView.removeHighlight();
			mut3dVisView.hideResidueWarning();
		}
	}

	function diagramDeselectHandler(datum, index)
	{
		// TODO need to change this for multiple selection
		allDeselectHandler();
	}

	function diagramSelectHandler(datum, index)
	{
		// highlight the corresponding residue in 3D view
		if (mut3dVisView && mut3dVisView.isVisible())
		{
			// highlight view for the selected datum
			// TODO for now removing previous highlights,
			// ...we need to change this to allow multiple selection
			if (mut3dVisView.highlightView(datum, true))
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

	init();
};
