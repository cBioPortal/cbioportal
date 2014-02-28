/**
 * Controller class for the 3D Mutation view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param mutationDetailsView   a MutationDetailsView instance
 * @param mainMutationView      a MainMutationView instance
 * @param mut3dVisView          a Mutation3dVisView instance
 * @param mut3dView             a Mutation3dView instance
 * @param mut3dVis              singleton Mutation3dVis instance
 * @param pdbProxy              proxy for pdb data
 * @param mutationUtil          data utility class (having the related mutations)
 * @param mutationDiagram       a MutationDiagram instance
 * @param tableView             a MutationDetailsTableView instance
 * @param geneSymbol            hugo gene symbol (string value)
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dController = function (mutationDetailsView, mainMutationView,
	mut3dVisView, mut3dView, mut3dVis, pdbProxy, mutationUtil,
	mutationDiagram, tableView, geneSymbol)
{
	// we cannot get pdb panel view as a constructor parameter,
	// since it is initialized after initializing this controller
	var _pdbPanelView = null;

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

		// add listeners for the mutation table view
		tableView.dispatcher.on(
			MutationDetailsEvents.PDB_LINK_CLICKED,
			pdbLinkHandler);

		// add listeners for the mutation 3d view
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
		if (_pdbPanelView)
		{
			_pdbPanelView.hideView();
		}
	}

	function mut3dInitHandler(event)
	{
		reset3dView();

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
			// highlight mutations on the diagram
			if (mutationDiagram.isHighlighted())
			{
				highlightSelected();
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
			highlightSelected();
		}
	}

	function pdbLinkHandler(mutationId)
	{
		var mutationMap = mutationUtil.getMutationIdMap();
		var mutation = mutationMap[mutationId];

		if (mutation)
		{
			// TODO ideally diagram should be highlighted by MutationDiagramController,
			// ...but we need to make sure that diagram is highlighted before refreshing the 3D view

			// highlight the corresponding pileup (without filtering the table)
			mutationDiagram.clearHighlights();
			mutationDiagram.highlightMutation(mutation.mutationSid);

			// reset the view with the selected chain
			reset3dView(mutation.pdbMatch.pdbId, mutation.pdbMatch.chainId);
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

	/**
	 * Highlights 3D residues for the selected diagram elements.
	 */
	function highlightSelected()
	{
		// selected pileups (mutations) on the diagram
		var selected = getSelectedPileups();

		// highlight 3D residues for the initially selected diagram elements
		var mappedCount = mut3dVisView.highlightView(selected, true);

		var unmappedCount = selected.length - mappedCount;

		// show a warning message if there is at least one unmapped selection
		if (unmappedCount > 0)
		{
			mut3dVisView.showResidueWarning(unmappedCount, selected.length);
		}
		else
		{
			mut3dVisView.hideResidueWarning();
		}
	}

	/**
	 * Resets the 3D view to its initial state. This function also initializes
	 * the PDB panel view if it is not initialized yet.
	 *
	 * @param pdbId     initial pdb structure to select
	 * @param chainId   initial chain to select
	 */
	function reset3dView(pdbId, chainId)
	{
		var gene = geneSymbol;
		var uniprotId = mut3dView.model.uniprotId; // TODO get this from somewhere else

		var initView = function(pdbColl)
		{
			// init pdb panel view if not initialized yet
			if (_pdbPanelView == null)
			{
				_pdbPanelView = mainMutationView.initPdbPanelView(pdbColl);

				// add listeners to the custom event dispatcher of the pdb panel
				_pdbPanelView.pdbPanel.dispatcher.on(
					MutationDetailsEvents.CHAIN_SELECTED,
					chainSelectHandler);
			}

			// reload the visualizer content with the given pdb and chain
			if (mut3dVisView != null &&
			    _pdbPanelView != null &&
			    pdbColl.length > 0)
			{
				updateColorMapper();
				_pdbPanelView.showView();

				if (pdbId && chainId)
				{
					_pdbPanelView.selectChain(pdbId, chainId);
				}
				else
				{
					// select default chain if none provided
					_pdbPanelView.selectDefaultChain();
				}
			}
		};

		// init view with the pdb data
		pdbProxy.getPdbData(uniprotId, initView);
	}

	/**
	 * Updates the color mapper of the 3D visualizer.
	 */
	function updateColorMapper()
	{
		// TODO this is not an ideal solution, but...
		// ...while we have multiple diagrams, the 3d visualizer is a singleton
		var colorMapper = function(mutationId, pdbId, chain) {
			var color = mutationDiagram.mutationColorMap[mutationId];

			if (color)
			{
				// this is for Jmol compatibility
				// (colors should start with an "x" instead of "#")
				color = color.replace("#", "x");
			}

			return color;
		};

		mut3dVis.updateOptions({mutationColorMapper: colorMapper});
	}

	init();
};
