/**
 * Controller class for the Mutation Diagram.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @author Selcuk Onur Sumer
 */
var MutationDiagramController = function(mutationDiagram, mutationTable, mutationUtil)
{
	function init()
	{
		// add listeners to the custom event dispatcher of the mutation table

		mutationTable.dispatcher.on(
			MutationDetailsEvents.MUTATION_TABLE_FILTERED,
			tableFilterHandler);
	}

	function tableFilterHandler(tableSelector)
	{
		var mutationMap = mutationUtil.getMutationIdMap();
		var currentMutations = [];

		// add current mutations into an array
		var rows = tableSelector.find("tr");
		_.each(rows, function(element, index) {
			var mutationId = $(element).attr("id");

			if (mutationId)
			{
				var mutation = mutationMap[mutationId];

				if (mutation)
				{
					currentMutations.push(mutation);
				}
			}
		});

		// update mutation diagram with the current mutations
		if (mutationDiagram !== null)
		{
			var mutationData = new MutationCollection(currentMutations);
			mutationDiagram.updatePlot(mutationData);
		}
	}

	init();
};
