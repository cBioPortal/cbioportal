/**
 * Controller class for the Mutation Table view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param tableView         a MutationDetailsTableView instance
 * @param mutationDiagram   a MutationDiagram instance
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsTableController = function(tableView, mutationDiagram)
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
	}

	function allDeselectHandler()
	{
		// remove all table highlights
		tableView.clearHighlights();

		// roll back the table to its previous state
		// (to the last state when a manual filtering applied)
		tableView.rollBack();
	}

	function deselectHandler(datum, index)
	{
		// remove all table highlights
		tableView.clearHighlights();

		// TODO this needs revision for multiple select
		// roll back the table to its previous state
		// (to the last state when a manual filtering applied)
		tableView.rollBack();
	}

	function selectHandler(datum, index)
	{
		// remove all table highlights
		tableView.clearHighlights();

		// filter table for the given mutations
		tableView.filter(datum.mutations);
	}

	function mouseoverHandler(datum, index)
	{
		// highlight mutations for the provided mutations
		tableView.highlight(datum.mutations);
	}

	function mouseoutHandler(datum, index)
	{
		// remove all highlights
		tableView.clearHighlights();
	}

	init();
};
