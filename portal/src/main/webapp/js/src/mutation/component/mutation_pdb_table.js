/**
 * Constructor for the MutationPdbTable class.
 *
 * @param options   visual options object
 * @param headers   array of header names
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationPdbTable(options, headers)
{
	// default options object
	var _defaultOpts = {
		el: "mutation_pdb_table_d3",
		elWidth: 740, // width of the container
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
			"pdb id": "visible",
			"chain": "visible",
			"uniprot positions": "visible",
			"identitiy percent": "visible",
			"organism": "visible",
			"summary": "visible",
			"uniprot from": "excluded",
			"uniprot to": "excluded"
		},
		// custom width values for columns
		columnWidth: {
			"summary": "65%"
		},
		// WARNING: overwriting advanced DataTables options such as
		// aoColumnDefs, oColVis, and fnDrawCallback may break column
		// visibility, sorting, and filtering. Proceed wisely ;)
		dataTableOpts: {
			//"sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
			"sDom": '<"H"<"mutation_datatables_filter"f><"mutation_datatables_info"i>>t',
			"bJQueryUI": true,
			"bPaginate": false,
			"bFilter": true,
			"sScrollY": "200px",
			"bScrollCollapse": true,
			"oLanguage": {
				"sInfo": "Showing _TOTAL_ PDB chains",
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
				"sClass": "mutation-pdb-table-column"};
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
				{"bVisible": false,
					"aTargets": hiddenCols},
				{"bSearchable": false,
					"aTargets": nonSearchableCols},
				{"fnRender": function(obj) {
						// format as a percentage value
						return Math.round(obj.aData[obj.iDataColumn] * 100);
					},
					"aTargets": [indexMap["identity percent"]]},
				{"fnRender": function(obj) {
						// there is no data (null) for uniprot positions,
						// so set the display value by using the hidden
						// columns ("uniprot from" and "uniprot to")
						return obj.aData[indexMap["uniprot from"]] + "-" +
						       obj.aData[indexMap["uniprot to"]];
					},
					"aTargets": [indexMap["uniprot positions"]]},
				// sort uniprot positions wrt "uniprot from"
				{"iDataSort": indexMap["uniprot from"],
					"aTargets": [indexMap["uniprot positions"]]}
			],
			"oColVis": {"aiExclude": excludedCols}, // columns to always hide
//			"fnDrawCallback": function(oSettings) {
//
//			},
			"fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
				var key = PdbDataUtil.chainKey(aData[indexMap["pdb id"]],
				                               aData[indexMap["chain"]]);
				_rowMap[key] = nRow;
			},
			"fnInitComplete": function(oSettings, json) {
				// trigger corresponding event
				_dispatcher.trigger(
					MutationDetailsEvents.PDB_TABLE_READY);
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
			// TODO determine vis params (if needed)
			value = vis();
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

		// determine hidden and excluded columns
		var hiddenCols = DataTableUtil.getHiddenColumns(headers, indexMap, visibilityMap);
		var excludedCols = DataTableUtil.getExcludedColumns(headers, indexMap, visibilityMap);

		// determine columns to exclude from filtering (through the search box)
		//var nonSearchableCols = _getNonSearchableCols(indexMap);
		var nonSearchableCols = [];

		// add custom sort functions for specific columns
		//_addSortFunctions();

		// actual initialization of the DataTables plug-in
		_dataTable = initDataTable($(_options.el), rows, headers,
			indexMap, hiddenCols, excludedCols, nonSearchableCols);

		addDefaultListeners(indexMap);
		// add a delay to the filter
		//_dataTable.fnSetFilteringDelay(600);
	}

	function addDefaultListeners(indexMap)
	{
		$(_options.el).on("click", "tr", function (event) {
			// remove previous highlights
			removeAllSelection();

			// highlight selected row
			$(event.target.parentNode).addClass('row_selected');

			var data = _dataTable.fnGetData(this);

			// trigger corresponding event
			_dispatcher.trigger(
				MutationDetailsEvents.TABLE_CHAIN_SELECTED,
				data[indexMap["pdb id"]],
				data[indexMap["chain"]]);
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

	function selectRow(pdbId, chainId)
	{
		var key = PdbDataUtil.chainKey(pdbId, chainId);

		// remove previous highlights
		removeAllSelection();

		// highlight selected
		var nRow = _rowMap[key];
		$(nRow).addClass("row_selected");
	}

	function removeAllSelection()
	{
//			$(_dataTable.fnSettings().aoData).each(function (){
//				$(this.nTr).removeClass('row_selected');
//			});

		_.each(_rowMap, function(nRow) {
			$(nRow).removeClass("row_selected");
		});
	}

	return {
		renderTable: renderTable,
		selectRow: selectRow,
		dispatcher: _dispatcher
	};
}
