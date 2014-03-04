/**
 * Constructor for the MutationPdbTable class.
 *
 * @param options   visual options object
 * @param data      PDB data (collection of PdbModel instances)
 * @param headers   array of header names
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationPdbTable(options, data, headers)
{
	// default options object
	var _defaultOpts = {
		el: "mutation_pdb_table_d3",
		// WARNING: overwriting advanced DataTables options such as
		// aoColumnDefs, oColVis, and fnDrawCallback may break column
		// visibility, sorting, and filtering. Proceed wisely ;)
		dataTableOpts: {
			"sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
			"bJQueryUI": true,
			"bPaginate": false,
			"bFilter": true
		}
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// custom event dispatcher
	var _dispatcher = {};
	_.extend(_dispatcher, Backbone.Events);

	// reference to the data table object
	var _dataTable = null;

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

		_.each(headers, function(header) {
			columns.push({"sTitle": header})
		});

		// these are the parametric data tables options
		var tableOpts = {
	        "aaData" : rows,
	        "aoColumns" : columns,
			"aoColumnDefs":[
				{"bVisible": false,
					"aTargets": hiddenCols},
				{"bSearchable": false,
					"aTargets": nonSearchableCols}
			],
			"oColVis": {"aiExclude": excludedCols}, // columns to always hide
			"fnDrawCallback": function(oSettings) {

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
		// TODO define & use custom visibility functions in the options object if needed
		// (see mutation table implementation)
		return "visible";
	}

	/**
	 * Formats the table with data tables plugin.
	 */
	function renderTable ()
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
		_dataTable = initDataTable($(_options.el), data, headers,
			indexMap, hiddenCols, excludedCols, nonSearchableCols);

		// add a delay to the filter
		//_dataTable.fnSetFilteringDelay(600);
	}

	return {
		renderTable: renderTable
	};
}
