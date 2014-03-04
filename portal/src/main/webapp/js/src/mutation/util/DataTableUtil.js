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
	 * @param headers           column header names
	 * @param visibilityValue   function to determine visibility value (returns string)
	 * @return {object} map of <column name, visibility value>
	 * @private
	 */
	function buildColumnVisMap(headers, visibilityValue)
	{
		var map = {};

		_.each(headers, function(ele, idx) {
			var header = ele.toLowerCase();

			if (map[header] == null)
			{
				map[header] = _.isFunction(visibilityValue) && visibilityValue(header);
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
	function getExcludedColumns(headers, indexMap, visMap)
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

	return {
		buildColumnIndexMap: buildColumnIndexMap,
		buildColumnVisMap: buildColumnVisMap,
		getHiddenColumns: getHiddenColumns,
		getExcludedColumns: getExcludedColumns
	};
})();
