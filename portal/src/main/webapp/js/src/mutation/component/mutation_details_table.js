/**
 * Constructor for the MutationDetailsTable class.
 *
 * @param options       visual options object
 * @param headers       array of header names
 * @param gene          hugo gene symbol
 * @param mutationUtil  mutation details util
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationDetailsTable(options, headers, gene, mutationUtil)
{
	// default options object
	var _defaultOpts = {
		el: "#mutation_details_table_d3",
		//elWidth: 740, // width of the container
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
		columnVisibility: {
			"datum": "excluded",
			"aa change": "visible",
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
		// Indicates whether a column is searchable or not.
		// Should be a boolean value or a function.
		//
		// All other columns will be initially non-searchable by default.
		columnSearch: {
			"case id": true,
			"mutation id": true,
			"cancer study": true,
			"aa change": true,
			"tumor type": true,
			"type": true
		},
		// custom width values for columns
		columnWidth: {
			"mutation assessor": "2%",
			"#mut in sample": "2%"
		},
		columnRender: {
			"mutation id": function(obj, mutation) {
				// TODO define 2 separate columns?
				return (mutation.mutationId + "-" + mutation.mutationSid);
			},
			"case id": function(obj, mutation) {
				var caseIdFormat = MutationDetailsTableFormatter.getCaseId(mutation.caseId);

				var vars = {};

				vars.linkToPatientView = mutation.linkToPatientView;
				vars.caseId = caseIdFormat.text;
				vars.caseIdClass = caseIdFormat.style;
				vars.caseIdTip = caseIdFormat.tip;

				return _.template(
					$("#mutation_table_case_id_template").html(), vars);
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

	// custom event dispatcher
	var _dispatcher = {};
	_.extend(_dispatcher, Backbone.Events);

	// reference to the data table object
	var _dataTable = null;

	// flag used to switch callbacks on/off
	var _callbackActive = true;

	// this is used to check if search string is changed after each redraw
	var _prevSearch = "";

	// last search string manually entered by the user
	var _manualSearch = "";

	var _rowMap = {};

	var _selectedRow = null;

	/**
	 * Initializes the data tables plug-in for the given table selector.
	 *
	 * @param tableSelector jQuery selector for the target table
	 * @param rows          data rows
	 * @param headers       column headers
	 * @param indexMap      map of <column name, column index>
	 * @param hiddenCols    indices of the hidden columns
	 * @param excludedCols  indices of the excluded columns
	 * @param nonSearchableCols    indices of the columns excluded from search
	 * @return {object}     DataTable instance
	 * @private
	 */
	function initDataTable(tableSelector, rows, headers,
		indexMap, hiddenCols, excludedCols, nonSearchableCols)
	{
		var columns = [];

		// set column options
		_.each(headers, function(header) {
			var column = {"sTitle": header,
				"sClass": "mutation-details-table-column"};
			var sWidth = _options.columnWidth[header.toLowerCase()];

			if (sWidth != null) {
				column.sWidth = sWidth;
			}

			columns.push(column);
		});

		// these are the parametric data tables options
		var tableOpts = {
			"aaData" : rows,
			"aoColumns" : columns,
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
				{"bVisible": false,
					"aTargets": hiddenCols},
				{"bSearchable": false,
					"aTargets": nonSearchableCols}
				// TODO may need to define sort targets as well
				//{"iDataSort": indexMap["uniprot from"],
				//	"aTargets": [indexMap["uniprot positions"]]}
			],
			"oColVis": {"aiExclude": excludedCols}, // columns to always hide
			"fnDrawCallback": function(oSettings) {
				addMutationTableTooltips();

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
			"fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
//				var datum = aData[indexMap["datum"]];
//				var key = PdbDataUtil.chainKey(datum.pdbId,
//				                               datum.chain.chainId);
//				_rowMap[key] = nRow;
			},
			"fnInitComplete": function(oSettings, json) {
//				// trigger corresponding event
//				_dispatcher.trigger(
//					MutationDetailsEvents.MUTATION_TABLE_READY);
			},
			"fnHeaderCallback": function(nHead, aData, iStart, iEnd, aiDisplay) {
			    addHeaderTooltips(nHead);
		    },
		    "fnFooterCallback": function(nFoot, aData, iStart, iEnd, aiDisplay) {
			    addFooterTooltips(nFoot);
		    }
		};

		// also add column renderers
		var renderers = DataTableUtil.getColumnRenderers(_options.columnRender, indexMap);
		tableOpts.aoColumnDefs = tableOpts.aoColumnDefs.concat(renderers);

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
	 * Determines the visibility value for the given column name
	 *
	 * @param columnName    name of the column (header)
	 * @return {String}     visibility value for the given column
	 */
	function visibilityValue(columnName)
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
			value = vis(mutationUtil, gene);
		}

		return value;
	}

	function searchValue(columnName)
	{
		var searchVal = _options.columnSearch[columnName];
		var value = searchVal;

		// if not in the list, hidden by default
		if (searchVal == null)
		{
			value = false;
		}
		// if function, then evaluate the value
		else if (_.isFunction(searchVal))
		{
			// TODO determine function params (if needed)
			value = searchVal();
		}

		return value;
	}

	/**
	 * Formats the table with data tables plugin for the given
	 * row data array (each element represents a single row).
	 *
	 * @rows    row data as an array
	 */
	function renderTable(rows)
	{
		// build a map, to be able to use string constants
		// instead of integer constants for table columns
		var indexMap = DataTableUtil.buildColumnIndexMap(headers);

		// build a visibility map for column headers
		var visibilityMap = DataTableUtil.buildColumnVisMap(headers, visibilityValue);

		// build a map to determine searchable columns
		var searchMap = DataTableUtil.buildColumnSearchMap(headers, searchValue);

		// determine hidden and excluded columns
		var hiddenCols = DataTableUtil.getHiddenColumns(headers, indexMap, visibilityMap);
		var excludedCols = DataTableUtil.getExcludedColumns(headers, indexMap, visibilityMap);

		// determine columns to exclude from filtering (through the search box)
		var nonSearchableCols = DataTableUtil.getNonSearchableColumns(headers, indexMap, searchMap);

		// add custom sort functions for specific columns
		addSortFunctions();

		// actual initialization of the DataTables plug-in
		_dataTable = initDataTable($(_options.el), rows, headers,
		                           indexMap, hiddenCols, excludedCols, nonSearchableCols);

		//addDefaultListeners(indexMap);

		// add a delay to the filter
		_dataTable.fnSetFilteringDelay(600);
	}

	function addDefaultListeners(indexMap)
	{
		//$(_options.el).on("click", "tr", function (event) {
		$(_options.el).on("click", ".pbd-chain-table-chain-cell a", function (event) {
			event.preventDefault();

			// remove previous highlights
			removeAllSelection();

			// get selected row via event target
			var selectedRow = $(event.target).closest("tr");

			// highlight selected row
			selectedRow.addClass('row_selected');

			//var data = _dataTable.fnGetData(this);
			var data = _dataTable.fnGetData(selectedRow[0]);
			var datum = data[indexMap["datum"]];

			// trigger corresponding event
			_dispatcher.trigger(
				MutationDetailsEvents.TABLE_CHAIN_SELECTED,
				datum.pdbId,
				datum.chain.chainId);
		});

		// TODO mouse over/out actions do not work as desired

		$(_options.el).on("mouseleave", "table", function (event) {
			//var data = _dataTable.fnGetData(this);

			// trigger corresponding event
//			_dispatcher.trigger(
//				MutationDetailsEvents.TABLE_CHAIN_MOUSEOUT);
		});

		$(_options.el).on("mouseenter", "tr", function (event) {
			var data = _dataTable.fnGetData(this);

			// trigger corresponding event
//			_dispatcher.trigger(
//				MutationDetailsEvents.TABLE_CHAIN_MOUSEOVER,
//				data[indexMap["pdb id"]],
//				data[indexMap["chain"]]);
		});
	}

	function cleanFilters()
	{
		// just show everything
		_dataTable.fnFilter("");
	}

	function getDataTable()
	{
		return _dataTable;
	}

	function selectRow(pdbId, chainId)
	{
		var key = PdbDataUtil.chainKey(pdbId, chainId);

		// remove previous highlights
		removeAllSelection();

		// highlight selected
		var nRow = _rowMap[key];
		$(nRow).addClass("row_selected");

		_selectedRow = nRow;
	}

	function removeAllSelection()
	{
		$(_options.el).find("tr").removeClass("row_selected");
	}

	function getSelectedRow()
	{
		return _selectedRow;
	}

	function addMutationTableTooltips()
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

	    var qTipOptionsLeft = {};
	    jQuery.extend(true, qTipOptionsLeft, qTipOptions);
	    qTipOptionsLeft.position = {my:'top right', at:'bottom left'};

		var tableSelector = $(_options.el);

	    //$('#mutation_details .mutation_details_table td').qtip(qTipOptions);

		tableSelector.find('.simple-tip').qtip(qTipOptions);
		tableSelector.find('.best_effect_transcript').qtip(qTipOptions);
        tableSelector.find('.cc-short-study-name').qtip(qTipOptions);
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
			var mutation = mutationUtil.getMutationIdMap()[mutationId];

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
	function addHeaderTooltips(nHead)
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

		var qTipOptionsHeader = {};
		jQuery.extend(true, qTipOptionsHeader, qTipOptions);
		qTipOptionsHeader.position = {my:'bottom center', at:'top center'};

		//tableSelector.find('thead th').qtip(qTipOptionsHeader);
		$(nHead).find("th").qtip(qTipOptionsHeader);
	}

	/**
	 * Adds tooltips for the table footer cells.
	 *
	 * @param nFoot table footer
	 * @private
	 */
	function addFooterTooltips(nFoot)
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

		var qTipOptionsFooter = {};
		jQuery.extend(true, qTipOptionsFooter, qTipOptions);
		qTipOptionsFooter.position = {my:'top center', at:'bottom center'};

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
	function addSortFunctions()
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

	return {
		renderTable: renderTable,
		selectRow: selectRow,
		cleanFilters: cleanFilters,
		getSelectedRow: getSelectedRow,
		getDataTable: getDataTable,
		dispatcher: _dispatcher
	};
}

