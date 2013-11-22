/**
 * Singleton utility class for Mutation View related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var MutationViewsUtil = (function()
{
	/**
	 * Initializes a MutationDetailsView instance. Postpones the actual rendering of
	 * the view contents until clicking on the corresponding mutations tab. Provided
	 * tabs assumed to be the main tabs instance containing the mutation tabs.
	 *
	 * @param el        {String} container selector
	 * @param options   {Object} view options
	 * @param tabs      {String} tabs selector (main tabs containing mutations tab)
	 * @param tabName   {String} name of the target tab (actual mutations tab)
	 * @return {MutationDetailsView}    a MutationDetailsView instance
	 */
	function delayedInitMutationDetailsView(el, options, tabs, tabName)
	{
		var view = new MutationDetailsView(options);
		var initialized = false;

		// init view without a delay if the target container is already visible
		if ($(el).is(":visible"))
		{
			view.render();
			initialized = true;
		}

		// add a click listener for the "mutations" tab
		$(tabs).bind("tabsactivate", function(event, ui) {
			// init when clicked on the mutations tab, and init only once
			if (ui.newTab.text().trim().toLowerCase() == tabName.toLowerCase())
			{
				// init only if it is not initialized yet
				if (!initialized)
				{
					view.render();
					initialized = true;
				}
				// if already init, then refresh genes tab
				// (a fix for ui.tabs.plugin resize problem)
				else
				{
					view.refreshGenesTab();
				}
			}
		});

		return view;
	}

	return {
		initMutationDetailsView : delayedInitMutationDetailsView
	};
})();
