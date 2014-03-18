/**
 * Controller class for the Main Mutation view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param mainMutationView  a MainMutationView instance
 * @param mutationDiagram   a MutationDiagram instance
 *
 * @author Selcuk Onur Sumer
 */
var MainMutationController = function (mainMutationView, mutationDiagram)
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
			MutationDetailsEvents.DIAGRAM_PLOT_UPDATED,
			diagramUpdateHandler);

		// also init reset link call back
		mainMutationView.addResetCallback(handleReset);
	}

	function handleReset(event)
	{
		// reset the diagram contents
		mutationDiagram.resetPlot();

		// hide the filter info text
		mainMutationView.hideFilterInfo();
	}

	function diagramUpdateHandler()
	{
		if (mutationDiagram.isFiltered())
		{
			// display info text
			mainMutationView.showFilterInfo();
		}
		else
		{
			// hide info text
			mainMutationView.hideFilterInfo();
		}
	}

	function allDeselectHandler()
	{
		// hide filter reset info
		if (!mutationDiagram.isFiltered())
		{
			mainMutationView.hideFilterInfo();
		}
	}

	function deselectHandler(datum, index)
	{
		// check if all deselected
		// (always show text if still there is a selected data point)
		if (mutationDiagram.getSelectedElements().length == 0)
		{
			// hide filter reset info
			allDeselectHandler();
		}
	}

	function selectHandler(datum, index)
	{
		// show filter reset info
		mainMutationView.showFilterInfo();
	}

	init();
};
