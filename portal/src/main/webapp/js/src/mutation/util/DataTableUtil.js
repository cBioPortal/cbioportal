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

		_.each(headers, function(ele, idx) {
			var header = ele.toLowerCase();

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

	function getNonSearchableColumns(headers, indexMap, searchMap)
	{
		// nonSearchableCols column indices
		var nonSearchableCols = [];

		// check all headers
		_.each(headers, function(ele, idx) {
			var header = ele.toLowerCase();

			// check if searchable
			if (searchMap[header] === false)
			{
				nonSearchableCols.push(indexMap[header]);
			}
		});

		return nonSearchableCols;
	}

	return {
		buildColumnIndexMap: buildColumnIndexMap,
		buildColumnVisMap: buildColumnVisMap,
		buildColumnSearchMap: buildColumnSearchMap,
		getHiddenColumns: getHiddenColumns,
		getExcludedColumns: getExcludedColumns,
		getNonSearchableColumns: getNonSearchableColumns
	};
})();
