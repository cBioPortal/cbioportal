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
	// default options object
	var _defaultOpts = {
		// Indicates the visibility of columns
		//
		// - Valid string constants:
		// "visible": column will be visible initially
		// "hidden":  column will be hidden initially,
		// but user can unhide the column via show/hide option
		// "excluded": column will be hidden initially,
		// and the user cannot unhide the column via show/hide option
		//
		// - Custom function: It is also possible to set a custom function
		// to determine the visibility of a column. A custom function
		// should return one of the valid string constants defined above.
		// For any unknown visibility value, column will be hidden by default.
		//
		// All other columns will be initially hidden by default.
		columnVisibility: {"aa change": "visible",
			"case id": "visible",
			"type": "visible",
			"cosmic": "visible",
			"mutation assessor": "visible",
			"#mut in sample": "visible",
			"mutation id": "excluded",
			"cancer study": "excluded",
			// TODO we may need more parameters than these two (util, gene)
			"copy #" : function (util, gene) {
				if (util.containsCnaData(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"allele freq (t)": function (util, gene) {
				if (util.containsAlleleFreqT(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"bam": function (util, gene) {
				if (util.containsIgvLink(gene)) {
					return "visible";
				}
				else {
					return "excluded";
				}
			},
			"ms": function (util, gene) {
				if (util.containsGermline(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"vs": function (util, gene) {
				if (util.containsValidStatus(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"tumor type": function (util, gene) {
				var count = util.distinctTumorTypeCount(gene);

				if (count > 1) {
					return "visible";
				}
				else if (count > 0) {
					return "hidden";
				}
				else { // if (count <= 0)
					return "excluded";
				}
			}
		},
		// WARNING: overwriting advanced DataTables options such as
		// aoColumnDefs, oColVis, and fnDrawCallback may break column
		// visibility, sorting, and filtering. Proceed wisely ;)
		dataTableOpts: {
			"sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
			"bJQueryUI": true,
			"bPaginate": false,
			"bFilter": true,
			"sScrollY": "600px",
			"bScrollCollapse": true,
			"oLanguage": {
				"sInfo": "Showing _TOTAL_ mutation(s)",
				"sInfoFiltered": "(out of _MAX_ total mutations)",
				"sInfoEmpty": "No mutations to show"
			}
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
	 * Creates a mapping for the given column headers. The mapped values
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
			if (map[headers[i].toLowerCase()] == null)
			{
				map[headers[i].toLowerCase()] = i;
			}
		}

		return map;
	}

	/**
	 * Creates a mapping for the given column headers. The mapped values
	 * will be one of these visibility values: visible, hidden, excluded.
	 *
	 * @param headers   column header names
	 * @return {object} map of <column name, visibility value>
	 * @private
	 */
	function _buildColumnVisMap(headers)
	{
		var map = {};

		_.each(headers, function(ele, idx) {
			var header = ele.toLowerCase();

			if (map[header] == null)
			{
				map[header] = _visibilityValue(header);
			}
		});

		return map;
	}

	/**
	 * Creates an array of indices for the columns to be hidden.
	 *
	 * @param headers   column header names
	 * @param indexMap  map of <column name, column index>
	 * @param visMap    map of <column name, column visibility value>
	 * @return {Array}  an array of column indices
	 * @private
	 */
	function _getHiddenColumns(headers, indexMap, visMap)
	{
		// set hidden column indices
		var hiddenCols = [];

		// process all headers
		_.each(headers, function(ele, idx) {
			var header = ele.toLowerCase();

			// determine visibility
			if (visMap[header] != "visible")
			{
				// include in hidden columns list if not visible
				hiddenCols.push(indexMap[header]);
			}
		});

		return hiddenCols;
	}

	/**
	 * Creates an array of indices for the columns to be completely excluded.
	 *
	 * @param headers   column header names
	 * @param indexMap  map of <column name, column index>
	 * @param visMap    map of <column name, column visibility value>
	 * @return {Array}  an array of column indices
	 * @private
	 */
	function _getExcludedColumns(headers, indexMap, visMap)
	{
		// excluded column indices
		var excludedCols = [];

		// check all headers
		_.each(headers, function(ele, idx) {
			var header = ele.toLowerCase();

			// determine visibility
			if (visMap[header] == "excluded")
			{
				excludedCols.push(indexMap[header]);
			}
		});

		return excludedCols;
	}

	/**
	 * Determines the visibility value for the given column name
	 *
	 * @param columnName    name of the column (header)
	 * @return {String}     visibility value for the given column
	 */
	function _visibilityValue(columnName)
	{
		var vis = _options.columnVisibility[columnName];
		var value = vis;

		// if not in the list, hidden by default
		if (!vis)
		{
			value = "hidden";
		}
		// if function, then evaluate the value
		else if (_.isFunction(vis))
		{
			value = vis(_mutationUtil, gene);
		}

		return value;
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

		// except the ones below, exclude any other column from search
		for (var col=0; col<count; col++)
		{
			var searchable = col == indexMap["case id"] ||
					col == indexMap["mutation id"] ||
					col == indexMap["cancer study"] ||
					col == indexMap["aa change"] ||
					col == indexMap["tumor type"] ||
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
		        {"sType": 'string',
			        "sClass": "center-align-td",
			        "aTargets": [indexMap["vs"],
				        indexMap["ms"],
				        indexMap["type"],
				        indexMap["center"]]},
	            {"sType": 'label-float-col',
	                "sClass": "right-align-td",
	                "aTargets": [indexMap["allele freq (t)"],
		                indexMap["allele freq (n)"]]},
	            {"sType": 'predicted-impact-col',
		            "sClass": "center-align-td",
	                "aTargets": [indexMap["mutation assessor"]]},
		        {"sType": 'copy-number-col',
			        "sClass": "center-align-td",
			        "aTargets": [indexMap["copy #"]]},
	            {"asSorting": ["desc", "asc"],
	                "aTargets": [indexMap["cosmic"],
		                indexMap["mutation assessor"],
	                    indexMap["#mut in sample"]]},
		        {"sWidth": "2%",
			        "aTargets": [indexMap["mutation assessor"],
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
	        },
		    "fnHeaderCallback": function(nHead, aData, iStart, iEnd, aiDisplay) {
			    _addHeaderTooltips(nHead);
		    },
		    "fnFooterCallback": function(nFoot, aData, iStart, iEnd, aiDisplay) {
			    _addFooterTooltips(nFoot);
		    }
	    };

		// merge with the one in the main options object
		tableOpts = jQuery.extend(true, {}, _defaultOpts.dataTableOpts, tableOpts);

		// format the table with the dataTable plugin
		var oTable = tableSelector.dataTable(tableOpts);
	    //oTable.css("width", "100%");

		$(window).bind('resize', function () {
			if (oTable.is(":visible"))
			{
				oTable.fnAdjustColumnSizing();
			}
		});

		// return the data table instance
		return oTable;
	}

	/**
	 * Adds tooltips for the table data rows.
	 *
	 * @param tableSelector   jQuery selector for the target table
	 * @private
	 */
	function _addMutationTableTooltips(tableSelector)
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

	    var qTipOptionsLeft = {};
	    jQuery.extend(true, qTipOptionsLeft, qTipOptions);
	    qTipOptionsLeft.position = {my:'top right', at:'bottom left', viewport: $(window)};

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

			var mutationId = parts[1];
			var mutation = _mutationUtil.getMutationIdMap()[mutationId];

			// copy default qTip options and modify "content"
			// to customize for predicted impact score
			var qTipOptsOma = {};
			jQuery.extend(true, qTipOptsOma, qTipOptionsLeft);

			qTipOptsOma.content = {text: "NA"}; // content is overwritten on render
			qTipOptsOma.events = {render: function(event, api) {
				var model = {impact: parts[0],
					xvia: mutation.xVarLink,
					msaLink: mutation.msaLink,
					pdbLink: mutation.pdbLink};

				var container = $(this).find('.qtip-content');

				// create & render FIS tip view
				var fisTipView = new PredictedImpactTipView({el:container, model: model});
				fisTipView.render();
			}};

			$(this).qtip(qTipOptsOma);
		});
	}

	/**
	 * Adds tooltips for the table header cells.
	 *
	 * @param nHead table header
	 * @private
	 */
	function _addHeaderTooltips(nHead)
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

		var qTipOptionsHeader = {};
		jQuery.extend(true, qTipOptionsHeader, qTipOptions);
		qTipOptionsHeader.position = {my:'bottom center', at:'top center', viewport: $(window)};

		//tableSelector.find('thead th').qtip(qTipOptionsHeader);
		$(nHead).find("th").qtip(qTipOptionsHeader);
	}

	/**
	 * Adds tooltips for the table footer cells.
	 *
	 * @param nFoot table footer
	 * @private
	 */
	function _addFooterTooltips(nFoot)
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

		var qTipOptionsFooter = {};
		jQuery.extend(true, qTipOptionsFooter, qTipOptions);
		qTipOptionsFooter.position = {my:'top center', at:'bottom center', viewport: $(window)};

		//tableSelector.find('tfoot th').qtip(qTipOptionsFooter);
		$(nFoot).find("th").qtip(qTipOptionsFooter);
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
			// TODO temp workaround
			return $(a).find("label").text().trim() || $(a).text().trim();
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

		// build a visibility map for column headers
		var visibilityMap = _buildColumnVisMap(headers);

		// determine hidden and excluded columns
        var hiddenCols = _getHiddenColumns(headers, indexMap, visibilityMap);
		var excludedCols = _getExcludedColumns(headers, indexMap, visibilityMap);

		// determine columns to exclude from filtering (through the search box)
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
