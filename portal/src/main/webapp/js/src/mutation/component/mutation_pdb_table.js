/**
 * Constructor for the MutationPdbTable class.
 *
 * @param options   visual options object
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationPdbTable(options)
{
	// default options object
	var _defaultOpts = {
		el: "#mutation_pdb_table_d3",
		elWidth: 740, // width of the container
		// default column header information
		//
		// name: internal name used to define column specific properties
		// display: display value
		// tip: tooltip value
		headers: {
			datum: {display: "datum",
				tip:""},
			pdbId: {display: "PDB Id",
				tip:""},
			chain: {display: "Chain",
				tip:""},
			uniprotFrom: {display: "Uniprot From",
				tip:""},
			uniprotPos: {display: "Uniprot Positions",
				tip:""},
			identityPercent: {display: "Identity Percent",
				tip:""},
			organism: {display: "Organism",
				tip:""},
			summary: {display: "Summary",
				tip:""}
		},
		columnOrder: ["datum", "pdbId", "chain", "uniprotFrom",
			"uniprotPos", "identityPercent", "organism", "summary"],
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
			"pdbId": "visible",
			"chain": "visible",
			"uniprotPos": "visible",
			"identityPercent": "hidden",
			"organism": "visible",
			"summary": "visible",
			"uniprotFrom": "excluded",
			"datum": "excluded"
		},
		// Indicates whether a column is searchable or not.
		// Should be a boolean value or a function.
		//
		// All other columns will be initially non-searchable by default.
		columnSearch: {
			"pdbId": true,
			"organism": true,
			"summary": true
		},
		// custom width values for columns
		columnWidth: {
			"summary": "65%"
		},
		columnRender: {
			identityPercent: function(obj, datum) {
				// format as a percentage value
				return Math.round(datum.chain.mergedAlignment.identityPerc * 100);
			},
			pdbId: function(obj, datum) {
				// format using the corresponding template
				return _.template($("#mutation_pdb_table_pdb_cell_template").html(),
		                  {pdbId: datum.pdbId});
			},
			chain: function(obj, datum) {
				// format using the corresponding template
				return _.template($("#mutation_pdb_table_chain_cell_template").html(),
		                  {chainId: datum.chain.chainId});
			},
			organism: function(obj, datum) {
				return datum.organism;
			},
			summary: function(obj, datum) {
				var vars = {summary: datum.summary.title,
					molecule: datum.summary.molecule};

				// format using the corresponding template
				return _.template(
					$("#mutation_pdb_table_summary_cell_template").html(),
					vars);
			},
			uniprotPos: function(obj, datum) {
				// there is no data (null) for uniprot positions,
				// so set the display value by using the hidden
				// column "datum"
				return datum.chain.mergedAlignment.uniprotFrom + "-" +
				       datum.chain.mergedAlignment.uniprotTo;
			},
			uniprotFrom: function(obj, datum) {
				return datum.chain.mergedAlignment.uniprotFrom;
			}
		},
		// WARNING: overwriting advanced DataTables options such as
		// aoColumnDefs, oColVis, and fnDrawCallback may break column
		// visibility, sorting, and filtering. Proceed wisely ;)
		dataTableOpts: {
			//"sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
			"sDom": '<"H"<"mutation_pdb_datatable_info"i><"mutation_pdb_datatable_filter"f>>t',
			"bJQueryUI": true,
			"bPaginate": false,
			"bFilter": true,
			"sScrollY": "200px",
			"bScrollCollapse": true,
			"oLanguage": {
				"sInfo": "Showing _TOTAL_ PDB chain(s)",
				"sInfoFiltered": "(out of _MAX_ total chains)",
				"sInfoEmpty": "No chains to show"
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
		// set column options
		var columns = DataTableUtil.getColumnOptions(headers,
			indexMap,
			_options.columnWidth);

		// these are the parametric data tables options
		var tableOpts = {
	        "aaData" : rows,
	        "aoColumns" : columns,
			"aoColumnDefs":[
				{"bVisible": false,
					"aTargets": hiddenCols},
				{"bSearchable": false,
					"aTargets": nonSearchableCols},
				// sort uniprot positions wrt "uniprot from"
				{"iDataSort": indexMap["uniprotFrom"],
					"aTargets": [indexMap["uniprotPos"]]}
			],
			"oColVis": {"aiExclude": excludedCols}, // columns to always hide
			"fnDrawCallback": function(oSettings) {
				addDefaultTooltips();
			},
			"fnHeaderCallback": function(nHead, aData, iStart, iEnd, aiDisplay) {
				$(nHead).find('th').addClass("mutation-pdb-table-header");
			},
			"fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
				var datum = aData[indexMap["datum"]];
				var key = PdbDataUtil.chainKey(datum.pdbId,
				                               datum.chain.chainId);
				_rowMap[key] = nRow;
			},
			"fnInitComplete": function(oSettings, json) {
				// trigger corresponding event
				_dispatcher.trigger(
					MutationDetailsEvents.PDB_TABLE_READY);
			}
		};

		// also add column renderers
		var renderers = DataTableUtil.getColumnRenderers(_options.columnRender, indexMap);
		tableOpts.aoColumnDefs = tableOpts.aoColumnDefs.concat(renderers);

		// merge with the one in the main options object
		tableOpts = jQuery.extend(true, {}, _defaultOpts.dataTableOpts, tableOpts);

		// format the table with the dataTable plugin and return the instance
		return tableSelector.dataTable(tableOpts);
	}

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
			// TODO determine function params (if needed)
			value = vis();
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
		var headers = _options.columnOrder;

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
		//_addSortFunctions();

		// actual initialization of the DataTables plug-in
		_dataTable = initDataTable($(_options.el), rows, _options.headers,
			indexMap, hiddenCols, excludedCols, nonSearchableCols);

		_dataTable.css("width", "100%");

		addDefaultListeners(indexMap);
		// add a delay to the filter
		//_dataTable.fnSetFilteringDelay(600);
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

//		$(_options.el).on("mouseleave", "table", function (event) {
//			//var data = _dataTable.fnGetData(this);
//
//			// trigger corresponding event
//			_dispatcher.trigger(
//				MutationDetailsEvents.TABLE_CHAIN_MOUSEOUT);
//		});
//
//		$(_options.el).on("mouseenter", "tr", function (event) {
//			var data = _dataTable.fnGetData(this);
//
//			// trigger corresponding event
//			_dispatcher.trigger(
//				MutationDetailsEvents.TABLE_CHAIN_MOUSEOVER,
//				data[indexMap["pdb id"]],
//				data[indexMap["chain"]]);
//		});
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

	function addDefaultTooltips()
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();
		$(_options.el).find(".simple-tip").qtip(qTipOptions);
	}

	function getHeaders()
	{
		return _options.headers;
	}

	return {
		renderTable: renderTable,
		selectRow: selectRow,
		cleanFilters: cleanFilters,
		getSelectedRow: getSelectedRow,
		getDataTable: getDataTable,
		getHeaders: getHeaders,
		dispatcher: _dispatcher
	};
}
