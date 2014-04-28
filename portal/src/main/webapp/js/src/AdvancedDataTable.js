/**
 * Designed as a base (abstract) class for an advanced implementation of data tables
 * with additional and more flexible options.
 *
 * @param options   table options
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function AdvancedDataTable(options)
{
	// global reference to this instance
	// (using "this" is sometimes dangerous)
	var self = this;

	self._defaultOpts = {
		// target container
		el: "",
		// default column options
		//
		// sTitle: display value
		// tip: tooltip value of the column header
		//
		// [data table options]: sType, sClass, sWidth, asSorting, ...
		columns: {},
		// display order of column headers
		columnOrder: [],
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
		columnVisibility: {},
		// Indicates whether a column is searchable or not.
		// Should be a boolean value or a function.
		//
		// All other columns will be initially non-searchable by default.
		columnSearch: {},
		// renderer functions:
		// returns the display value for a column (may contain html elements)
		// if no render function is defined for a column,
		// then we rely on a custom "mData" function.
		columnRender: {},
		// column sort functions:
		// returns the value to be used for column sorting purposes.
		// if no sort function is defined for a column,
		// then uses the render function for sorting purposes.
		columnSort: {},
		// column filter functions:
		// returns the value to be used for column sorting purposes.
		// if no filter function is defined for a column,
		// then uses the sort function value for filtering purposes.
		// if no sort function is defined either, then uses
		// the value returned by the render function.
		columnFilter: {},
		// native "mData" function for DataTables plugin. if this is implemented,
		// functions defined in columnRender and columnSort will be ignored.
		// in addition to the default source, type, and val parameters,
		// another parameter "indexMap" will also be passed to the function.
		columnData: {},
		// default tooltip functions
		columnTooltips: {},
		// default event listener config
		eventListeners: {},
		// sort functions for custom types
		customSort: {},
		// delay amount before applying the user entered filter query
		filteringDelay: 0,
		// WARNING: overwriting advanced DataTables options such as
		// aoColumnDefs, oColVis, and fnDrawCallback may break column
		// visibility, sorting, and filtering. Proceed wisely ;)
		dataTableOpts: {}
	};

	// merge options with default options to use defaults for missing values
	self._options = jQuery.extend(true, {}, self._defaultOpts, options);

	// custom event dispatcher
	self._dispatcher = {};
	_.extend(self._dispatcher, Backbone.Events);

	// reference to the data table object
	self._dataTable = null;

	/**
	 * Determines the visibility value for the given column name
	 *
	 * @param columnName    name of the column (header)
	 * @return {String}     visibility value for the given column
	 */
	self._visibilityValue = function(columnName)
	{
		// method body should be overridden by subclasses
		return "hidden";
	};

	/**
	 * Determines the search value for the given column name
	 *
	 * @param columnName    name of the column (header)
	 * @return {Boolean}    whether searchable or not
	 */
	self._searchValue = function(columnName)
	{
		// method body should be overridden by subclasses
		return false;
	};

	/**
	 * Formats the table with data tables plugin for the given
	 * row data array (each element represents a single row).
	 *
	 * @rows    row data as an array
	 */
	self.renderTable = function(rows)
	{
		var columnOrder = self._options.columnOrder;

		// build a map, to be able to use string constants
		// instead of integer constants for table columns
		var indexMap = DataTableUtil.buildColumnIndexMap(columnOrder);
		var nameMap = DataTableUtil.buildColumnNameMap(self._options.columns);

		// build a visibility map for column headers
		var visibilityMap = DataTableUtil.buildColumnVisMap(columnOrder, self._visibilityValue);

		// build a map to determine searchable columns
		var searchMap = DataTableUtil.buildColumnSearchMap(columnOrder, self._searchValue);

		// determine hidden and excluded columns
		var hiddenCols = DataTableUtil.getHiddenColumns(columnOrder, indexMap, visibilityMap);
		var excludedCols = DataTableUtil.getExcludedColumns(columnOrder, indexMap, visibilityMap);

		// determine columns to exclude from filtering (through the search box)
		var nonSearchableCols = DataTableUtil.getNonSearchableColumns(columnOrder, indexMap, searchMap);

		// add custom sort functions for specific columns
		self._addSortFunctions();

		// actual initialization of the DataTables plug-in
		self._dataTable = self._initDataTable(
			$(self._options.el), rows, self._options.columns, nameMap,
			indexMap, hiddenCols, excludedCols, nonSearchableCols);

		//self._dataTable.css("width", "100%");

		self._addEventListeners(indexMap);

		// add a delay to the filter
		if (self._options.filteringDelay > 0)
		{
			self._dataTable.fnSetFilteringDelay(self._options.filteringDelay);
		}
	};

	/**
	 * Generates the data table options for the given parameters.
	 *
	 * @param tableSelector jQuery selector for the target table
	 * @param rows          data rows
	 * @param columnOpts    column options
	 * @param nameMap       map of <column display name, column name>
	 * @param indexMap      map of <column name, column index>
	 * @param hiddenCols    indices of the hidden columns
	 * @param excludedCols  indices of the excluded columns
	 * @param nonSearchableCols    indices of the columns excluded from search
	 * @return {object}     DataTable options
	 */
	self._initDataTableOpts = function(tableSelector, rows, columnOpts, nameMap,
		indexMap, hiddenCols, excludedCols, nonSearchableCols)
	{
		// method body should be overridden by subclasses
		return null;
	};

	/**
	 * Initializes the data tables plug-in for the given table selector.
	 *
	 * @param tableSelector jQuery selector for the target table
	 * @param rows          data rows
	 * @param columnOpts    column options
	 * @param nameMap       map of <column display name, column name>
	 * @param indexMap      map of <column name, column index>
	 * @param hiddenCols    indices of the hidden columns
	 * @param excludedCols  indices of the excluded columns
	 * @param nonSearchableCols    indices of the columns excluded from search
	 * @return {object}     DataTable instance
	 */
	self._initDataTable = function(tableSelector, rows, columnOpts, nameMap,
		indexMap, hiddenCols, excludedCols, nonSearchableCols)
	{
		var tableOpts = self._initDataTableOpts(tableSelector, rows, columnOpts, nameMap,
			indexMap, hiddenCols, excludedCols, nonSearchableCols);

		// also add mData definitions (rendering, sort, etc.)
		var mData = DataTableUtil.getColumnData(indexMap,
			self._options.columnRender,
			self._options.columnSort,
			self._options.columnFilter,
			self._options.columnData);

		tableOpts.aoColumnDefs = tableOpts.aoColumnDefs.concat(mData);

		// merge with the one in the main options object
		//tableOpts = jQuery.extend(true, {}, _defaultOpts.dataTableOpts, tableOpts);
		tableOpts = jQuery.extend(true, {}, self._options.dataTableOpts, tableOpts);

		// format the table with the dataTable plugin and return the table instance
		return tableSelector.dataTable(tableOpts);
	};

	/**
	 * Adds custom DataTables sort function for specific columns.
	 */
	self._addSortFunctions = function()
	{
		_.each(_.pairs(self._options.customSort), function(pair) {
			var fnName = pair[0];
			var sortFn = pair[1];

			jQuery.fn.dataTableExt.oSort[fnName] = sortFn;
		});
	};

	/**
	 * Adds event listeners provided within the options object.
	 *
	 * @param indexMap  map of <column name, column index>
	 */
	self._addEventListeners = function(indexMap)
	{
		_.each(self._options.eventListeners, function(listenerFn) {
			listenerFn(self.getDataTable(), self._dispatcher, indexMap);
		});
	};

	/**
	 * Adds column (data) tooltips provided within the options object.
	 */
	self._addColumnTooltips = function()
	{
		var tableSelector = $(self._options.el);

		_.each(self._options.columnTooltips, function(tooltipFn) {
			if (_.isFunction(tooltipFn))
			{
				tooltipFn(tableSelector);
			}
		});
	};

	self.getColumnOptions = function()
	{
		return self._options.columns;
	};

	self.getDataTable = function()
	{
		return self._dataTable;
	};
}
