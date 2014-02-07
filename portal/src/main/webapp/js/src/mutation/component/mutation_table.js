/**
 * Class for formatting a table of mutations.
 * This class is specifically designed to convert a regular
 * html table into a DataTable.
 *
 * @param tableSelector jQuery selector for the target table
 * @param gene          hugo gene symbol
 * @param mutations     mutations as an array of raw JSON objects
 * @param options       visual options object
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
var MutationTable = function(tableSelector, gene, mutations, options)
{
	// TODO add more options if necessary (for other views: patient view, cross cancer, etc)

	// default options object
	var _defaultOpts = {
		// indicates the visibility of columns
		//
		// - visible: column will always be visible initially
		// - conditional: column will be visible conditionally
		// - hidden:  column will always be hidden initially,
		// but user can unhide the hidden columns via show/hide option
		// - exclude: columns will be hidden initially,
		// and the user cannot unhide these via show/hide option
		// - excludeIfHidden: if conditionally determined to be hidden initially, then exclude
		//
		// all other columns will be initially hidden by default
		columnVisibility: {"aa change": "visible",
			"case id": "visible",
			"type": "visible",
			"cosmic": "visible",
			"fis": "visible",
			"cons": "visible",
			"3d": "visible",
			"vs": "visible",
			"allele freq (t)": "visible",
			"copy #" : "visible",
			"#mut in sample": "visible",
			"mutation id": "exclude",
			"cancer study": "exclude",
			"bam": "excludeIfHidden",
			"ms": "conditional"},
		// visibility functions for conditionally visible columns,
		// i.e: columns whose visibility set to either "conditional" or "excludeIfHidden".
		// if no method is provided for conditionally hidden columns, then these columns
		// will be initially visible
		// TODO we may need more parameters than these two (util, gene)
		visibilityFn: {
			"bam": function (util, gene) {
				return util.containsIgvLink(gene);
			},
			"ms": function (util, gene) {
				return util.containsGermline(gene);
			}
		},
		// WARNING: overwriting advanced DataTables options such as
		// aoColumnDefs, oColVis, and fnDrawCallback may break column
		// visibility and sorting behaviour. Proceed wisely ;)
		dataTableOpts: {
			"sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
			"bJQueryUI": true,
			"bPaginate": false,
			"bFilter": true
		}
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	var _mutationUtil = new MutationDetailsUtil(
		new MutationCollection(mutations));

	// custom event dispatcher
	var _dispatcher = {};
	_.extend(_dispatcher, Backbone.Events);

	// flag used to switch callbacks on/off
	var _callbackActive = true;

	// reference to the data table object
	var _dataTable = null;

	// this is used to check if search string is changed after each redraw
	var _prevSearch = "";

	// last search string manually entered by the user
	var _manualSearch = "";

	/**
	 * Creates a mapping for given column headers. The mapped values
	 * will be the array indices for each element.
	 *
	 * @param headers   column header names
	 * @return {object} map of <column name, column index>
	 * @private
	 */
	function _buildColumnIndexMap(headers)
	{
		var map = {};

		for (var i=0; i < headers.length; i++)
		{
			if (map[headers[i].toLowerCase()] == undefined ||
			    map[headers[i].toLowerCase()] == null)
			{
				map[headers[i].toLowerCase()] = i;
			}
		}

		return map;
	}

	/**
	 * Creates an array of indices for the columns to be hidden.
	 *
	 * @param headers   column header names
	 * @param indexMap  map of <column name, column index>
	 * @return {Array}  an array of column indices
	 * @private
	 */
	function _getHiddenColumns(headers, indexMap)
	{
		// set hidden column indices
		var hiddenCols = [];

		// process all headers
		_.each(headers, function(ele, idx) {
			var visible = false;
			var header = ele.toLowerCase();
			var vis = _options.columnVisibility[header];

			// if not in the list, hidden by default
			if (!vis)
			{
				vis = "hidden";
			}

			// check known options
			if (vis === "hidden" ||
			    vis === "exclude")
			{
				visible = false;
			}
			else if (vis == "visible")
			{
				visible = true;
			}
			// check if conditionally hidden
			else if (vis === "conditional" ||
			    vis === "excludeIfHidden")
			{
				var visFn = _options.visibilityFn[header];

				// visibility function checks the condition
				if (_.isFunction(visFn))
				{
					visible = visFn(_mutationUtil, gene);
				}
				else
				{
					visible = true;
				}
			}
			// for unknown option, hide by default
			else
			{
				visible = false;
			}

			if (!visible)
			{
				hiddenCols.push(indexMap[header]);
			}
		});

		return hiddenCols;
	}

	/**
	 * Creates an array of indices for the columns to be completely excluded.
	 *
	 * @param headers       column header names
	 * @param indexMap      map of <column name, column index>
	 * @param hiddenCols    indices of the hidden columns
	 * @return {Array}  an array of column indices
	 * @private
	 */
	function _getExcludedColumns(headers, indexMap, hiddenCols)
	{
		// excluded column indices
		var excludedCols = [];

		// check all headers
		_.each(headers, function(ele, idx) {
			var excluded = false;
			var header = ele.toLowerCase();
			var vis = _options.columnVisibility[header];

			// check if excluded
			if (vis)
			{
				if (vis === "exclude")
				{
					excluded = true;
				}
				else if (vis === "excludeIfHidden")
				{
					// if the column is hidden, then exclude
					if ($.inArray(indexMap[header], hiddenCols) != -1)
					{
						excluded = true;
					}
				}
			}

			if (excluded)
			{
				excludedCols.push(indexMap[header]);
			}
		});

		return excludedCols;
	}

	/**
	 * Creates an array of indices for the columns to be excluded
	 * from search.
	 *
	 * @param indexMap  map of <column name, column index>
	 * @return {Array}  an array of column indices
	 */
	function _getNonSearchableCols(indexMap)
	{
		var count = 0;
		var cols = [];

		for (var key in indexMap)
		{
			count++;
		}

		// except these 4, exclude any other column from search
		for (var col=0; col<count; col++)
		{
			var searchable = col == indexMap["case id"] ||
					col == indexMap["mutation id"] ||
					col == indexMap["cancer study"] ||
					col == indexMap["aa change"] ||
					col == indexMap["type"];

			if (!searchable)
			{
				cols.push(col);
			}
		}

		return cols;
	}

	/**
	 * Initializes the data tables plug-in for the given table selector.
	 *
	 * @param tableSelector jQuery selector for the target table
	 * @param indexMap      map of <column name, column index>
	 * @param hiddenCols    indices of the hidden columns
	 * @param excludedCols  indices of the excluded columns
	 * @param nonSearchableCols    indices of the columns excluded from search
	 * @return {object}     DataTable instance
	 * @private
	 */
	function _initDataTable(tableSelector, indexMap, hiddenCols, excludedCols, nonSearchableCols)
	{
		// these are the parametric data tables options
		// TODO try to move these into the main defaultOpts object?
	    var tableOpts = {
//	        "aaData" : rows,
//	        "aoColumns" : columns,
	        "aoColumnDefs":[
	            {"sType": 'aa-change-col',
	                "aTargets": [ indexMap["aa change"] ]},
	            {"sType": 'label-int-col',
	                "sClass": "right-align-td",
	                "aTargets": [indexMap["cosmic"],
		                indexMap["start pos"],
		                indexMap["end pos"],
		                indexMap["var alt"],
		                indexMap["var ref"],
		                indexMap["norm alt"],
		                indexMap["norm ref"],
	                    indexMap["#mut in sample"]]},
	            {"sType": 'label-float-col',
	                "sClass": "right-align-td",
	                "aTargets": [indexMap["allele freq (t)"],
		                indexMap["allele freq (n)"]]},
	            {"sType": 'predicted-impact-col',
	                "aTargets": [indexMap["fis"]]},
		        {"sType": 'copy-number-col',
			        "sClass": "center-align-td",
			        "aTargets": [indexMap["copy #"]]},
	            {"asSorting": ["desc", "asc"],
	                "aTargets": [indexMap["cosmic"],
		                indexMap["fis"],
	                    indexMap["cons"],
	                    indexMap["3d"],
	                    indexMap["#mut in sample"]]},
	            {"bVisible": false,
	                "aTargets": hiddenCols},
		        {"bSearchable": false,
			        "aTargets": nonSearchableCols}
	        ],
			"oColVis": {"aiExclude": excludedCols}, // columns to always hide
	        "fnDrawCallback": function(oSettings) {
	            // add tooltips to the table
	            _addMutationTableTooltips(tableSelector);

		        var currSearch = oSettings.oPreviousSearch.sSearch;

		        // trigger the event only if the corresponding flag is set
		        // and there is a change in the search term
		        if (_callbackActive &&
		            _prevSearch != currSearch)
		        {
			        // trigger corresponding event
			        _dispatcher.trigger(
				        MutationDetailsEvents.MUTATION_TABLE_FILTERED,
				        tableSelector);

			        // assuming events are active for only manual filtering
			        // so update manual search string only after triggering the event
			        _manualSearch = currSearch;
		        }

		        // update prev search string reference for future use
		        _prevSearch = currSearch;
	        }
	    };

		// merge with the one in the main options object
		tableOpts = jQuery.extend(true, {}, _defaultOpts.dataTableOpts, tableOpts);

		// format the table with the dataTable plugin
		var oTable = tableSelector.dataTable(tableOpts);
	    oTable.css("width", "100%");

		// return the data table instance
		return oTable;
	}

	/**
	 * Add tooltips for the table header and the table data rows.
	 *
	 * @param tableSelector   jQuery selector for the target table
	 * @private
	 */
	function _addMutationTableTooltips(tableSelector)
	{
	    var qTipOptions = {content: {attr: 'alt'},
		    show: {event: 'mouseover'},
	        hide: {fixed: true, delay: 100, event: 'mouseout'},
	        style: {classes: 'mutation-details-tooltip qtip-shadow qtip-light qtip-rounded'},
	        position: {my:'top left', at:'bottom right'}};

	    var qTipOptionsHeader = {};
		var qTipOptionsFooter = {};
	    var qTipOptionsLeft = {};
	    jQuery.extend(true, qTipOptionsHeader, qTipOptions);
		jQuery.extend(true, qTipOptionsFooter, qTipOptions);
	    jQuery.extend(true, qTipOptionsLeft, qTipOptions);
	    qTipOptionsHeader.position = {my:'bottom center', at:'top center'};
		qTipOptionsFooter.position = {my:'top center', at:'bottom center'};
	    qTipOptionsLeft.position = {my:'top right', at:'bottom left'};

	    tableSelector.find('thead th').qtip(qTipOptionsHeader);
		tableSelector.find('tfoot th').qtip(qTipOptionsFooter);
	    //$('#mutation_details .mutation_details_table td').qtip(qTipOptions);

		tableSelector.find('.simple-tip').qtip(qTipOptions);
		tableSelector.find('.best_effect_transcript').qtip(qTipOptions);
        tableSelector.find('.cc-short-study-name').qtip(qTipOptions);
        tableSelector.find('.simple-tip-left').qtip(qTipOptionsLeft);

		// add tooltip for COSMIC value
		tableSelector.find('.mutation_table_cosmic').each(function() {
			var label = this;
			var mutationId = $(label).attr('alt');
			var mutation = _mutationUtil.getMutationIdMap()[mutationId];

			// copy default qTip options and modify "content" to customize for cosmic
			var qTipOptsCosmic = {};
			jQuery.extend(true, qTipOptsCosmic, qTipOptions);

			qTipOptsCosmic.content = {text: "NA"}; // content is overwritten on render
			qTipOptsCosmic.events = {render: function(event, api) {
				var model = {cosmic: mutation.cosmic,
					keyword: mutation.keyword,
					geneSymbol: gene,
					total: $(label).text()};

				var container = $(this).find('.qtip-content');

				// create & render cosmic tip view
				var cosmicView = new CosmicTipView({el: container, model: model});
				cosmicView.render();
			}};

			$(label).qtip(qTipOptsCosmic);
		});

		// add tooltip for Predicted Impact Score (FIS)
		tableSelector.find('.oma_link').each(function() {
			var links = $(this).attr('alt');
			var parts = links.split("|");

			// copy default qTip options and modify "content"
			// to customize for predicted impact score
			var qTipOptsOma = {};
			jQuery.extend(true, qTipOptsOma, qTipOptions);

			qTipOptsOma.content = {text: "NA"}; // content is overwritten on render
			qTipOptsOma.events = {render: function(event, api) {
				var model = {impact: parts[0],
					xvia: parts[1]};

				var container = $(this).find('.qtip-content');

				// create & render FIS tip view
				var fisTipView = new PredictedImpactTipView({el:container, model: model});
				fisTipView.render();
			}};

			$(this).qtip(qTipOptsOma);
		});
	}

	/**
	 * Helper function for predicted impact score sorting.
	 */
	function _assignValueToPredictedImpact(text, score)
	{
		// using score by itself may be sufficient,
		// but sometimes we have no numerical score value

		var value;
		text = text.toLowerCase();

		if (text == "low" || text == "l") {
			value = 2;
		} else if (text == "medium" || text == "m") {
			value = 3;
		} else if (text == "high" || text == "h") {
			value = 4;
		} else if (text == "neutral" || text == "n") {
			value = 1;
		} else {
			value = -1;
		}

		if (value > 0 && !isNaN(score))
		{
			//assuming FIS values cannot exceed 1000
			value += score / 1000;
		}

		return value;
	}

	function _assignValueToCna(text)
	{
		var value;
		text = text.toLowerCase();

		// TODO this is actually reverse mapping of MutationDetailsUtil._cnaMap
		if (text == "homdel") {
			value = 1;
		} else if (text == "hetloss") {
			value = 2;
		} else if (text == "diploid") {
			value = 3;
		} else if (text == "gain") {
			value = 4;
		} else if (text == "amp") {
			value = 5;
		} else { // unknown
			value = -1;
		}

		return value;
	}

	function _getAltTextValue(a)
	{
		var altValue = $(a).attr("alt");
		var value = parseFloat(altValue);

		if (isNaN(value))
		{
			value = "";
		}

		return value;
	}

	/**
	 * Helper function for predicted impact score sorting.
	 * Gets the score from the "alt" property within the given html string.
	 */
	function _getFisValue(a)
	{
		var score = "";
		var altValue = $(a).attr("alt");

		var parts = altValue.split("|");

		if (parts.length > 0)
		{
			if (parts[0].length > 0)
			{
				score = parseFloat(parts[0]);
			}
		}

		return score;
	}

	/**
	 * Helper function for sorting string values within label tag.
	 */
	function _getLabelTextValue(a)
	{
		if (a.indexOf("label") != -1)
		{
			return $(a).text().trim();
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Helper function for sorting int values within label tag.
	 */
	function _getLabelTextIntValue(a)
	{
		if (a.indexOf("label") != -1)
		{
			return parseInt($(a).text());
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Helper function for sorting float values within label tag.
	 */
	function _getLabelTextFloatValue(a)
	{
		if (a.indexOf("label") != -1)
		{
			return parseFloat($(a).text());
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Comparison function for ascending sort operations.
	 *
	 * @param a
	 * @param b
	 * @param av
	 * @param bv
	 * @return
	 * @private
	 */
	function _compareSortAsc(a, b, av, bv)
	{
		if (av >= 0) {
			if (bv >= 0) {
				return av==bv ? 0 : (av<bv ? -1:1);
			} else {
				return -1;
			}
		} else {
			if (bv >= 0) {
				return 1;
			} else {
				return a==b ? 0 : (a<b ? 1:-1);
			}
		}
	}

	/**
	 * Comparison function for descending sort operations.
	 *
	 * @param a
	 * @param b
	 * @param av
	 * @param bv
	 * @return
	 * @private
	 */
	function _compareSortDesc(a, b, av, bv)
	{
		if (av >= 0) {
			if (bv >= 0) {
				return av==bv ? 0 : (av<bv ? 1:-1);
			} else {
				return -1;
			}
		} else {
			if (bv >= 0) {
				return 1;
			} else {
				return a==b ? 0 : (a<b ? -1:1);
			}
		}
	}

	/**
	 * Adds custom DataTables sort function for specific columns.
	 *
	 * @private
	 */
	function _addSortFunctions()
	{
		/**
		 * Ascending sort function for protein (amino acid) change column.
		 */
		jQuery.fn.dataTableExt.oSort['aa-change-col-asc'] = function(a,b) {
			var ares = a.match(/.*[A-Z]([0-9]+)[^0-9]+/);
			var bres = b.match(/.*[A-Z]([0-9]+)[^0-9]+/);

			if (ares) {
				if (bres) {
					var ia = parseInt(ares[1]);
					var ib = parseInt(bres[1]);
					return ia==ib ? 0 : (ia<ib ? -1:1);
				} else {
					return -1;
				}
			} else {
				if (bres) {
					return 1;
				} else {
					return a==b ? 0 : (a<b ? -1:1);
				}
			}
		};

		/**
		 * Descending sort function for protein (amino acid) change column.
		 */
		jQuery.fn.dataTableExt.oSort['aa-change-col-desc'] = function(a,b) {
			var ares = a.match(/.*[A-Z]([0-9]+)[^0-9]+/);
			var bres = b.match(/.*[A-Z]([0-9]+)[^0-9]+/);

			if (ares) {
				if (bres) {
					var ia = parseInt(ares[1]);
					var ib = parseInt(bres[1]);
					return ia==ib ? 0 : (ia<ib ? 1:-1);
				} else {
					return -1;
				}
			} else {
				if (bres) {
					return 1;
				} else {
					return a==b ? 0 : (a<b ? 1:-1);
				}
			}
		};

		/**
		 * Ascending sort function for the copy number column.
		 */
		jQuery.fn.dataTableExt.oSort['copy-number-col-asc']  = function(a,b) {
			var av = _assignValueToCna(_getLabelTextValue(a));
			var bv = _assignValueToCna(_getLabelTextValue(b));

			return _compareSortAsc(a, b, av, bv);
		};

		/**
		 * Descending sort function for the copy number column.
		 */
		jQuery.fn.dataTableExt.oSort['copy-number-col-desc']  = function(a,b) {
			var av = _assignValueToCna(_getLabelTextValue(a));
			var bv = _assignValueToCna(_getLabelTextValue(b));

			return _compareSortDesc(a, b, av, bv);
		};

		/**
		 * Ascending sort function for predicted impact column.
		 */
		jQuery.fn.dataTableExt.oSort['predicted-impact-col-asc']  = function(a,b) {
			var av = _assignValueToPredictedImpact(_getLabelTextValue(a), _getFisValue(a));
			var bv = _assignValueToPredictedImpact(_getLabelTextValue(b), _getFisValue(b));

			return _compareSortAsc(a, b, av, bv);
		};

		/**
		 * Descending sort function for predicted impact column.
		 */
		jQuery.fn.dataTableExt.oSort['predicted-impact-col-desc']  = function(a,b) {
			var av = _assignValueToPredictedImpact(_getLabelTextValue(a), _getFisValue(a));
			var bv = _assignValueToPredictedImpact(_getLabelTextValue(b), _getFisValue(b));

			return _compareSortDesc(a, b, av, bv);
		};

		/**
		 * Ascending sort function for columns having int within label tag.
		 */
		jQuery.fn.dataTableExt.oSort['label-int-col-asc'] = function(a,b) {
			var av = _getLabelTextIntValue(a);
			var bv = _getLabelTextIntValue(b);

			return _compareSortAsc(a, b, av, bv);
		};

		/**
		 * Descending sort function for columns having int within label tag.
		 */
		jQuery.fn.dataTableExt.oSort['label-int-col-desc'] = function(a,b) {
			var av = _getLabelTextIntValue(a);
			var bv = _getLabelTextIntValue(b);

			return _compareSortDesc(a, b, av, bv);
		};

		/**
		 * Ascending sort function for columns having float within label tag.
		 */
		jQuery.fn.dataTableExt.oSort['label-float-col-asc'] = function(a,b) {
			var av = _getLabelTextFloatValue(a);
			var bv = _getLabelTextFloatValue(b);

			return _compareSortAsc(a, b, av, bv);
		};

		/**
		 * Descending sort function for columns having float within label tag.
		 */
		jQuery.fn.dataTableExt.oSort['label-float-col-desc'] = function(a,b) {
			var av = _getLabelTextFloatValue(a);
			var bv = _getLabelTextFloatValue(b);

			return _compareSortDesc(a, b, av, bv);
		};
	}

	this.getDataTable = function()
	{
		return _dataTable;
	};

	/**
	 * Formats the table with data tables plugin.
	 */
	this.formatTable = function()
	{
		var headers = [];

		// extract table header strings into an array
		tableSelector.find("thead .mutation-table-header").each(function(){
			headers.push($(this).text().trim());
		});

		// build a map, to be able to use string constants
		// instead of integer constants for table columns
		var indexMap = _buildColumnIndexMap(headers);

		// determine hidden and excluded columns
        var hiddenCols = _getHiddenColumns(headers, indexMap);
		var excludedCols = _getExcludedColumns(headers, indexMap, hiddenCols);
		var nonSearchableCols = _getNonSearchableCols(indexMap);

		// add custom sort functions for specific columns
		_addSortFunctions();

		// actual initialization of the DataTables plug-in
		_dataTable = _initDataTable(tableSelector, indexMap, hiddenCols, excludedCols, nonSearchableCols);

		// add a delay to the filter
		_dataTable.fnSetFilteringDelay(600);
	};

	/**
	 * Enables/disables callback functions (event triggering).
	 *
	 * @param active    boolean value
	 */
	this.setCallbackActive = function(active)
	{
		_callbackActive = active;
	};

	/**
	 * Resets filtering related variables to their initial state.
	 * Does not remove actual table filters.
	 */
	this.cleanFilters = function()
	{
		_prevSearch = "";
		_manualSearch = "";
	};

	this.getManualSearch = function()
	{
		return _manualSearch;
	};

	this.dispatcher = _dispatcher;
};
