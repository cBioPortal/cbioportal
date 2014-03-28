/**
 * Controller class for the Mutation Table view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param tableView         a MutationDetailsTableView instance
 * @param mutationDiagram   a MutationDiagram instance
 * @param mutationDetailsView   a MutationDetailsView instance
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsTableController = function(tableView, mutationDiagram, mutationDetailsView)
{
	function init()
	{
		// add listeners to the custom event dispatcher of the diagram

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.ALL_LOLLIPOPS_DESELECTED,
			allDeselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_DESELECTED,
			deselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_SELECTED,
			selectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_MOUSEOVER,
			mouseoverHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_MOUSEOUT,
			mouseoutHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_RESET,
			diagramResetHandler);

		// add listeners for the mutation details view
		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.GENE_TAB_SELECTED,
			geneTabSelectHandler);
	}

	function diagramResetHandler()
	{
		if (tableView)
		{
			// reset all previous table filters
			tableView.resetFilters();
		}
	}

	function allDeselectHandler()
	{
		if (tableView)
		{
			// remove all table highlights
			tableView.clearHighlights();

			// roll back the table to its previous state
			// (to the last state when a manual filtering applied)
			tableView.rollBack();
		}
	}

	function deselectHandler(datum, index)
	{
		if (tableView)
		{
			// remove all table highlights
			tableView.clearHighlights();

			var mutations = [];

			// get mutations for all selected elements
			_.each(mutationDiagram.getSelectedElements(), function (ele, i) {
				mutations = mutations.concat(ele.datum().mutations);
			});

			// reselect with the reduced selection
			if (mutations.length > 0)
			{
				// filter table for the selected mutations
				tableView.filter(mutations);
			}
			// rollback only if none selected
			else
			{
				// roll back the table to its previous state
				// (to the last state when a manual filtering applied)
				tableView.rollBack();
			}
		}
	}

	function selectHandler(datum, index)
	{
		if (tableView)
		{
			// remove all table highlights
			tableView.clearHighlights();

			var mutations = [];

			// get mutations for all selected elements
			_.each(mutationDiagram.getSelectedElements(), function (ele, i) {
				mutations = mutations.concat(ele.datum().mutations);
			});

			// filter table for the selected mutations
			tableView.filter(mutations);
		}
	}

	function mouseoverHandler(datum, index)
	{
		if (tableView)
		{
			// highlight mutations for the provided mutations
			tableView.highlight(datum.mutations);
		}
	}

	function mouseoutHandler(datum, index)
	{
		if (tableView)
		{
			// remove all highlights
			tableView.clearHighlights();
		}
	}

	function geneTabSelectHandler(gene)
	{
		if (tableView)
		{
			var oTable = tableView.tableUtil.getDataTable();

			// alternatively we can check if selected gene is this view's gene
			if (oTable.is(":visible"))
			{
				oTable.fnAdjustColumnSizing();
			}
		}
	}

	init();
};
