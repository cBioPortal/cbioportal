/**
 * Utility class for formatting a table of mutations.
 * This class is specifically designed to convert a regular
 * html table into a DataTable.
 *
 * @param tableSelector jQuery selector for the target table
 * @param gene          hugo gene symbol
 * @param mutations     mutations as an array of raw JSON objects
 * @constructor
 */
var MutationTableUtil = function(tableSelector, gene, mutations)
{
	var mutationUtil = new MutationDetailsUtil(
		new MutationCollection(mutations));

	// a list of registered callback functions
	var callbackFunctions = [];

	// flag used to switch callbacks on/off
	var callbackActive = true;

	// reference to the data table object
	var dataTable = null;

	// this is used to check if search string is changed after each redraw
	var prevSearch = "";

	// last search string manually entered by the user
	var manualSearch = "";

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
	 * @param headers           column header names
	 * @param indexMap          map of <column name, column index>
	 * @param containsGermline  whether the table contains a germline mutation
	 * @param containsIgvLink   whether the table contains a valid link to IGV
	 * @return {Array}          an array of column indices
	 * @private
	 */
	function _getHiddenColumns(headers, indexMap, containsGermline, containsIgvLink)
	{
		// set hidden column indices
		var hiddenCols = [];
		var count = 0;

		for (var key in headers)
		{
			count++;
		}

		// always hide id
		hiddenCols.push(indexMap["mutation id"]);

		// hide less important columns by default
		for (var col=indexMap["vs"] + 1; col<count; col++)
		{
			// do not hide allele frequency (T) and count columns
			if (!(col == indexMap["allele freq (t)"] ||
			      col == indexMap["#mut in sample"] ||
			      col == indexMap["bam"]))
			{
				hiddenCols.push(col);
			}
		}

		// conditionally hide mutation status column if there is no germline mutation
		if (!containsGermline)
		{
			hiddenCols.push(indexMap["ms"]);
		}

		// conditionally hide BAM file column if there is no valid link available
		if (!containsIgvLink)
		{
			hiddenCols.push(indexMap["bam"]);
		}

		return hiddenCols;
	}

	/**
	 * Initializes the data tables plug-in for the given table selector.
	 *
	 * @param tableSelector jQuery selector for the target table
	 * @param indexMap      map of <column name, column index>
	 * @param hiddenCols    indices of the hidden columns
	 * @return {object}     DataTable instance
	 * @private
	 */
	function _initDataTable(tableSelector, indexMap, hiddenCols)
	{
		// format the table with the dataTable plugin
	    var oTable = tableSelector.dataTable({
	        "sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
	        "bJQueryUI": true,
	        "bPaginate": false,
	        "bFilter": true,
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
	            {"asSorting": ["desc", "asc"],
	                "aTargets": [indexMap["cosmic"],
		                indexMap["fis"],
	                    indexMap["cons"],
	                    indexMap["3d"],
	                    indexMap["#mut in sample"]]},
	            {"bVisible": false,
	                "aTargets": hiddenCols}
	        ],
			"oColVis": {"aiExclude": [indexMap["mutation id"]]}, // always hide id column
	        "fnDrawCallback": function(oSettings) {
	            // add tooltips to the table
	            _addMutationTableTooltips(tableSelector);

		        var currSearch = oSettings.oPreviousSearch.sSearch;

		        // call the functions only if the corresponding flag is set
		        // and there is a change in the search term
		        if (callbackActive &&
		            prevSearch != currSearch)
		        {
			        // call registered callback functions
			        for (var i=0; i < callbackFunctions.length; i++)
			        {
				        callbackFunctions[i](tableSelector);
			        }

			        // assuming callbacks are active for only manual filtering
			        // so update manual search string only if callbacks are active
			        manualSearch = currSearch;
		        }

		        // update prev search string reference for future use
		        prevSearch = currSearch;
	        }
	    });

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
	        style: { classes: 'mutation-details-tooltip ui-tooltip-shadow ui-tooltip-light ui-tooltip-rounded' },
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
		tableSelector.find('.simple-tip-left').qtip(qTipOptionsLeft);

		// add tooltip for COSMIC value
		tableSelector.find('.mutation_table_cosmic').each(function() {
			var label = this;
			var mutationId = $(label).attr('alt');
			var mutation = mutationUtil.getMutationIdMap()[mutationId];

			// copy default qTip options and modify "content" to customize for cosmic
			var qTipOptsCosmic = {};
			jQuery.extend(true, qTipOptsCosmic, qTipOptions);

			qTipOptsCosmic.content = {text: "NA"}; // content is overwritten on render
			qTipOptsCosmic.events = {render: function(event, api) {
				var model = {cosmic: mutation.cosmic,
					keyword: mutation.keyword,
					geneSymbol: gene,
					total: $(label).text()};

				var container = $(this).find('.ui-tooltip-content');

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

				var container = $(this).find('.ui-tooltip-content');

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
		return dataTable;
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

		// determine hidden columns
		var hiddenCols = _getHiddenColumns(headers,
			indexMap,
			mutationUtil.containsGermline(gene),
			mutationUtil.containsIgvLink(gene));

		// add custom sort functions for specific columns
		_addSortFunctions();

		// actual initialization of the DataTables plug-in
		dataTable = _initDataTable(tableSelector, indexMap, hiddenCols);
	};

	/**
	 * Registers a callback function which is to be called
	 * for each rendering of the table.
	 *
	 * @param callbackFn    function to register
	 */
	this.registerCallback = function(callbackFn)
	{
		if (_.isFunction(callbackFn))
		{
			callbackFunctions.push(callbackFn);
		}
	};

	/**
	 * Removes a previously registered callback function.
	 *
	 * @param callbackFn    function to unregister
	 */
	this.unregisterCallback = function(callbackFn)
	{
		var index = $.inArray(callbackFn);

		// remove the function at the specified index
		if (index >= 0)
		{
			callbackFunctions.splice(index, 1);
		}
	};

	/**
	 * Enables/disables callback functions.
	 *
	 * @param active    boolean value
	 */
	this.setCallbackActive = function(active)
	{
		callbackActive = active;
	};

	/**
	 * Resets filtering related variables to their initial state.
	 * Does not remove actual table filters.
	 */
	this.cleanFilters = function()
	{
		prevSearch = "";
		manualSearch = "";
	};

	this.getManualSearch = function()
	{
		return manualSearch;
	};
};
