/**
 * Singleton utility class for Mutation View related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var MutationViewsUtil = (function()
{
	/**
	 * Mapping between the mutation type (data) values and
	 * view values.
	 */
	var _mutationTypeMap = {
		missense_mutation: {label: "Missense", style: "missense_mutation"},
		nonsense_mutation: {label: "Nonsense", style: "trunc_mutation"},
		nonstop_mutation: {label: "Nonstop", style: "trunc_mutation"},
		frame_shift_del: {label: "FS del", style: "trunc_mutation"},
		frame_shift_ins: {label: "FS ins", style: "trunc_mutation"},
		in_frame_ins: {label: "IF ins", style: "inframe_mutation"},
		in_frame_del: {label: "IF del", style: "inframe_mutation"},
		splice_site: {label: "Splice", style: "trunc_mutation"},
		other: {style: "other_mutation"}
	};

	/**
	 * Mapping between the validation status (data) values and
	 * view values.
	 */
	var _validationStatusMap = {
		valid: {label: "V", style: "valid", tooltip: "Valid"},
		validated: {label: "V", style: "valid", tooltip: "Valid"},
		wildtype: {label: "W", style: "wildtype", tooltip: "Wildtype"},
		unknown: {label: "U", style: "unknown", tooltip: "Unknown"},
		not_tested: {label: "U", style: "unknown", tooltip: "Unknown"},
		none: {label: "U", style: "unknown", tooltip: "Unknown"},
		na: {label: "U", style: "unknown", tooltip: "Unknown"}
	};

	/**
	 * Mapping between the mutation status (data) values and
	 * view values.
	 */
	var _mutationStatusMap = {
		somatic: {label: "S", style: "somatic", tooltip: "Somatic"},
		germline: {label: "G", style: "germline", tooltip: "Germline"},
		unknown: {label: "U", style: "unknown", tooltip: "Unknown"},
		none: {label: "U", style: "unknown", tooltip: "Unknown"},
		na: {label: "U", style: "unknown", tooltip: "Unknown"}
	};

	/**
	 * Mapping between the functional impact score (data) values and
	 * view values.
	 */
	var _omaScoreMap = {
		h: {label: "High", style: "oma_high", tooltip: "High"},
		m: {label: "Medium", style: "oma_medium", tooltip: "Medium"},
		l: {label: "Low", style: "oma_low", tooltip: "Low"},
		n: {label: "Neutral", style: "oma_neutral", tooltip: "Neutral"}
	};

	/**
	 * Mapping btw the copy number (data) values and view values.
	 */
	var _cnaMap = {
		"-2": {label: "HOMDEL", style: "cna-homdel", tooltip: "Homozygously deleted"},
		"-1": {label: "hetloss", style: "cna-hetloss", tooltip: "Heterozygously deleted"},
		"0": {label: "diploid", style: "cna-diploid", tooltip: "Diploid / normal"},
		"1": {label: "gain", style: "cna-gain", tooltip: "Low-level gain"},
		"2": {label: "AMP", style: "cna-amp", tooltip: "High-level amplification"},
		"unknown" : {label: "NA", style: "cna-unknown", tooltip: "CNA data is not available for this gene"}
	};

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

	/**
	 * Returns all visual style mappings in a single object.
	 *
	 * @return {Object} style maps in a single object
	 */
	function getVisualStyleMaps()
	{
		return {
			mutationType: _mutationTypeMap,
			validationStatus: _validationStatusMap,
			mutationStatus: _mutationStatusMap,
			omaScore: _omaScoreMap,
			cna: _cnaMap
		};
	}

	function defaultTableTooltipOpts()
	{
		return {
			content: {attr: 'alt'},
			show: {event: 'mouseover'},
			hide: {fixed: true, delay: 100, event: 'mouseout'},
			style: {classes: 'mutation-details-tooltip qtip-shadow qtip-light qtip-rounded'},
			position: {my:'top left', at:'bottom right', viewport: $(window)}
		};
	}

	return {
		initMutationDetailsView: delayedInitMutationDetailsView,
		defaultTableTooltipOpts: defaultTableTooltipOpts,
		getVisualStyleMaps: getVisualStyleMaps
	};
})();
