/**
 * Singleton utility class for DataTables related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var DataTableUtil = (function()
{
	/**
	 * Creates a mapping for the given column headers. The mapped values
	 * will be the array indices for each element.
	 *
	 * @param headers   column header names
	 * @return {object} map of <column name, column index>
	 * @private
	 */
	function buildColumnIndexMap(headers)
	{
		var map = {};

		_.each(headers, function(header, index)
		{
			if (map[header] == null)
			{
				map[header] = index;
			}
		});

		return map;
	}

	/**
	 * Creates a mapping for the given column headers.
	 *
	 * @param columns   column options
	 * @return {object} map of <column display name, column name>
	 * @private
	 */
	function buildColumnNameMap(columns)
	{
		var map = {};

		_.each(_.pairs(columns), function(pair, index)
		{
			var name = pair[0];
			var options = pair[1];

			if (options.sTitle != null &&
			    options.sTitle.length > 0)
			{
				map[options.sTitle] = name;
			}
		});

		return map;
	}

	/**
	 * Creates a mapping for the given column headers. The mapped values
	 * will be one of these visibility values: visible, hidden, excluded.
	 *
	 * @param headers           column header names
	 * @param visibilityValue   function to determine visibility value (returns string)
	 * @return {object} map of <column name, visibility value>
	 * @private
	 */
	function buildColumnVisMap(headers, visibilityValue)
	{
		var map = {};

		_.each(headers, function(header, idx) {
			if (map[header] == null)
			{
				// TODO sanitize return value of the custom function
				if (_.isFunction(visibilityValue))
				{
					map[header] = visibilityValue(header);
				}
				else
				{
					map[header] = "hidden";
				}
			}
		});

		return map;
	}

	/**
	 * Creates a mapping for the given column headers. The mapped values
	 * will be a boolean.
	 *
	 * @param headers       column header names
	 * @param searchValue   function to determine search value (returns boolean)
	 * @return {object} map of <column name, search value>
	 * @private
	 */
	function buildColumnSearchMap(headers, searchValue)
	{
		var map = {};

		_.each(headers, function(header, idx) {
			if (map[header] == null)
			{
				map[header] = _.isFunction(searchValue) && searchValue(header);
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
	function getHiddenColumns(headers, indexMap, visMap)
	{
		// set hidden column indices
		var hiddenCols = [];

		// process all headers
		_.each(headers, function(header, idx) {
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
	function getExcludedColumns(headers, indexMap, visMap)
	{
		// excluded column indices
		var excludedCols = [];

		// check all headers
		_.each(headers, function(header, idx) {
			// determine visibility
			if (visMap[header] == "excluded")
			{
				excludedCols.push(indexMap[header]);
			}
		});

		return excludedCols;
	}

	/**
	 *
	 * @param headers   column header names
	 * @param indexMap  map of <column name, column index>
	 * @param searchMap map of <column name, column search value>
	 * @return {Array}  an array of column indices
	 */
	function getNonSearchableColumns(headers, indexMap, searchMap)
	{
		// nonSearchableCols column indices
		var nonSearchableCols = [];

		// check all headers
		_.each(headers, function(header, idx) {
			// check if searchable
			if (searchMap[header] === false)
			{
				nonSearchableCols.push(indexMap[header]);
			}
		});

		return nonSearchableCols;
	}

	/**
	 * Generates renderer functions for each column.
	 *
	 * @param renderers map of <column name, renderer>
	 * @param indexMap  map of <column name, column index>
	 * @returns {Array} array of renderer functions
	 */
	function getColumnRenderers(renderers, indexMap)
	{
		var columnRenderers = [];

		_.each(_.pairs(renderers), function(pair) {
			var columnName = pair[0];
			var renderFn = pair[1];

			var columnIdx = indexMap[columnName];

			if (columnIdx != null)
			{
				var renderer = {
					"fnRender": function(obj) {
						// assuming the data table has a datum column
						var datum = obj.aData[indexMap["datum"]];

						// assuming renderFn takes these 2 parameters
						return renderFn(obj, datum);
					},
					"aTargets": [columnIdx]
				};

				columnRenderers.push(renderer);
			}
		});

		return columnRenderers;
	}

	/**
	 * Generates "mData" functions for each column.
	 *
	 * @param indexMap  map of <column name, column index>
	 * @param columnRender map of <column name, renderer>
	 * @param columnSort map of <column name, sort function>
	 * @param columnFilter map of <column name, filter function>
	 * @param columnData map of <column name, mData function>
	 *
	 * @returns {Array} array of mData functions
	 */
	function getColumnData(indexMap, columnRender, columnSort, columnFilter, columnData)
	{
		var mData = {};

		// iterate over list of renderers
		// (assuming each column has its corresponding renderer)
		_.each(_.pairs(columnRender), function(pair) {
			var columnName = pair[0];
			var renderFn = pair[1];
			var sortFn = columnSort[columnName];
			var filterFn = columnFilter[columnName];

			var columnIdx = indexMap[columnName];

			var sortValue = function(datum)
			{
				// try to use a sort function
				if (sortFn != null)
				{
					return sortFn(datum);
				}
				// if no sort function defined,
				// use the render function
				{
					return renderFn(datum);
				}
			};

			if (columnIdx != null)
			{
				var def = {
					"mData": function(source, type, val) {
						var datum = source[indexMap["datum"]];

						if (type === "set") {
							return null;
						}
						else if (type === "display")
						{
							return renderFn(datum);
						}
						else if (type === "sort")
						{
							return sortValue(datum);
						}
						else if (type === "filter")
						{
							if (filterFn != null)
							{
								return filterFn(datum);
							}
							else
							{
								return sortValue(datum);
							}
						}
//						else if (type === "type")
//						{
//							return 0.0;
//						}

						return source[columnIdx];
					},
					"aTargets": [columnIdx]
				};

				mData[columnName] = def;
			}
		});

		// now process columnData ("mData") functions
		// (this will override prev definition, if any)
		_.each(_.pairs(columnData), function(pair) {
			var columnName = pair[0];
			var mDataFn = pair[1];
			var columnIdx = indexMap[columnName];

			var def = {
				"mData": function(source, type, val) {
					return mDataFn(source, type, val, indexMap);
				},
				"aTargets": [columnIdx]
			};

			mData[columnName] = def;
		});

		// return an array of values (not a map)
		return _.values(mData);
	}

	/**
	 * Generates basic column options for the given headers.
	 *
	 * @param columns   column options object
	 * @param indexMap  map of <column name, column index>
	 * @returns {Array} array of column options
	 */
	function getColumnOptions(columns, indexMap)
	{
		var columnOpts = [];

		// set column options
		_.each(_.pairs(columns), function(pair) {
			var name = pair[0];
			var column = pair[1];

			// TODO column may have non-datatable options

			var idx = indexMap[name];

			if (idx > 0)
			{
				columnOpts[idx] = column;
			}
		});

		return columnOpts;
	}

	function getAltTextValue(a)
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
	 * Helper function for sorting string values within label tag.
	 */
	function getLabelTextValue(a)
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
	function getLabelTextIntValue(a)
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
	function getLabelTextFloatValue(a)
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
	 */
	function compareSortAsc(a, b, av, bv)
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
	 */
	function compareSortDesc(a, b, av, bv)
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

	return {
		buildColumnIndexMap: buildColumnIndexMap,
		buildColumnNameMap: buildColumnNameMap,
		buildColumnVisMap: buildColumnVisMap,
		buildColumnSearchMap: buildColumnSearchMap,
		getHiddenColumns: getHiddenColumns,
		getExcludedColumns: getExcludedColumns,
		getNonSearchableColumns: getNonSearchableColumns,
		getColumnOptions: getColumnOptions,
		getColumnRenderers: getColumnRenderers,
		getColumnData: getColumnData,
		compareSortAsc: compareSortAsc,
		compareSortDesc: compareSortDesc,
		getAltTextValue: getAltTextValue,
		getLabelTextValue: getLabelTextValue,
		getLabelTextIntValue: getLabelTextIntValue,
		getLabelTextFloatValue: getLabelTextFloatValue
	};
})();
