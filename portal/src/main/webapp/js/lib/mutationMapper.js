/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	 * Creates an array of indices for the columns to be ignored during search.
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

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility class for Mutation View related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var MutationViewsUtil = (function()
{
	/**
	 * Mapping between the mutation type (data) values and
	 * view values.
	 */

	var _mutationStyleMap = {
		missense: {label: "Missense",
			longName: "Missense",
			style: "missense_mutation",
			mainType: "missense",
			priority: 1},
		inframe: {label: "IF",
			longName: "In-frame",
			style: "inframe_mutation",
			mainType: "inframe",
			priority: 2},
		truncating: {
			label: "Truncating",
			longName: "Truncating",
			style: "trunc_mutation",
			mainType: "truncating",
			priority: 4},
		nonsense: {label: "Nonsense",
			longName: "Nonsense",
			style: "trunc_mutation",
			mainType: "truncating",
			priority: 6},
		nonstop: {label: "Nonstop",
			longName: "Nonstop",
			style: "trunc_mutation",
			mainType: "truncating",
			priority: 7},
		nonstart: {label: "Nonstart",
			longName: "Nonstart",
			style: "trunc_mutation",
			mainType: "truncating",
			priority: 8},
		frameshift: {label: "FS",
			longName: "Frame Shift",
			style: "trunc_mutation",
			mainType: "truncating",
			priority: 4},
		frame_shift_del: {label: "FS del",
			longName: "Frame Shift Deletion",
			style: "trunc_mutation",
			mainType: "truncating",
			priority: 4},
		frame_shift_ins: {label: "FS ins",
			longName: "Frame Shift Insertion",
			style: "trunc_mutation",
			mainType: "truncating",
			priority: 5},
		in_frame_ins: {label: "IF ins",
			longName: "In-frame Insertion",
			style: "inframe_mutation",
			mainType: "inframe",
			priority: 3},
		in_frame_del: {label: "IF del",
			longName: "In-frame Deletion",
			style: "inframe_mutation",
			mainType: "inframe",
			priority: 2},
		splice_site: {label: "Splice",
			longName: "Splice site",
			style: "trunc_mutation",
			mainType: "truncating",
			priority: 9},
		fusion: {label: "Fusion",
			longName: "Fusion",
			style: "fusion",
			mainType: "other",
			priority: 10},
		other: {style: "other_mutation",
			mainType: "other",
			priority: 11}
	};

	var _mutationTypeMap = {
		"missense_mutation": _mutationStyleMap.missense,
		"missense": _mutationStyleMap.missense,
		"missense_variant": _mutationStyleMap.missense,
		"frame_shift_ins": _mutationStyleMap.frame_shift_ins,
		"frame_shift_del": _mutationStyleMap.frame_shift_del,
		"frameshift": _mutationStyleMap.frameshift,
		"frameshift_deletion": _mutationStyleMap.frame_shift_del,
		"frameshift_insertion": _mutationStyleMap.frame_shift_ins,
		"de_novo_start_outofframe": _mutationStyleMap.frameshift,
		"frameshift_variant": _mutationStyleMap.frameshift,
		"nonsense_mutation": _mutationStyleMap.nonsense,
		"nonsense": _mutationStyleMap.nonsense,
		"stopgain_snv": _mutationStyleMap.nonsense,
		"splice_site": _mutationStyleMap.splice_site,
		"splice": _mutationStyleMap.splice_site,
		"splice site": _mutationStyleMap.splice_site,
		"splicing": _mutationStyleMap.splice_site,
		"splice_site_snp": _mutationStyleMap.splice_site,
		"splice_site_del": _mutationStyleMap.splice_site,
		"splice_site_indel": _mutationStyleMap.splice_site,
		"translation_start_site":  _mutationStyleMap.nonstart,
		"start_codon_snp": _mutationStyleMap.nonstart,
		"start_codon_del": _mutationStyleMap.nonstart,
		"nonstop_mutation": _mutationStyleMap.nonstop,
		"in_frame_del": _mutationStyleMap.in_frame_del,
		"in_frame_ins": _mutationStyleMap.in_frame_ins,
		"indel": _mutationStyleMap.in_frame_del,
		"nonframeshift_deletion": _mutationStyleMap.inframe,
		"nonframeshift": _mutationStyleMap.inframe,
		"nonframeshift insertion": _mutationStyleMap.inframe,
		"nonframeshift_insertion": _mutationStyleMap.inframe,
		"targeted_region": _mutationStyleMap.inframe,
		"inframe": _mutationStyleMap.inframe,
		"truncating": _mutationStyleMap.truncating,
		"fusion": _mutationStyleMap.fusion,
		"other": _mutationStyleMap.other
	};

	/**
	 * Mapping between the validation status (data) values and
	 * view values.
	 */
	var _validationStatusMap = {
		valid: {label: "V", style: "valid", tooltip: "Valid"},
		validated: {label: "V", style: "valid", tooltip: "Valid"},
		wildtype: {label: "W", style: "wildtype", tooltip: "Wildtype"},
		unknown: {label: "U", style: "unknown", tooltip: "Unknown"},
		not_tested: {label: "U", style: "unknown", tooltip: "Unknown"},
		none: {label: "U", style: "unknown", tooltip: "Unknown"},
		na: {label: "U", style: "unknown", tooltip: "Unknown"}
	};

	/**
	 * Mapping between the mutation status (data) values and
	 * view values.
	 */
	var _mutationStatusMap = {
		somatic: {label: "S", style: "somatic", tooltip: "Somatic"},
		germline: {label: "G", style: "germline", tooltip: "Germline"},
		unknown: {label: "U", style: "unknown", tooltip: "Unknown"},
		none: {label: "U", style: "unknown", tooltip: "Unknown"},
		na: {label: "U", style: "unknown", tooltip: "Unknown"}
	};

	/**
	 * Mapping between the functional impact score (data) values and
	 * view values.
	 */
	var _omaScoreMap = {
		h: {label: "High", style: "oma_high", tooltip: "High"},
		m: {label: "Medium", style: "oma_medium", tooltip: "Medium"},
		l: {label: "Low", style: "oma_low", tooltip: "Low"},
		n: {label: "Neutral", style: "oma_neutral", tooltip: "Neutral"}
	};

	/**
	 * Mapping btw the copy number (data) values and view values.
	 */
	var _cnaMap = {
		"-2": {label: "DeepDel", style: "cna-homdel", tooltip: "Deep deletion"},
		"-1": {label: "ShallowDel", style: "cna-hetloss", tooltip: "Shallow deletion"},
		"0": {label: "Diploid", style: "cna-diploid", tooltip: "Diploid / normal"},
		"1": {label: "Gain", style: "cna-gain", tooltip: "Low-level gain"},
		"2": {label: "AMP", style: "cna-amp", tooltip: "High-level amplification"},
		"unknown" : {label: "NA", style: "cna-unknown", tooltip: "CNA data is not available for this gene"}
	};

	/**
	 * Initializes a MutationMapper instance. Postpones the actual rendering of
	 * the view contents until clicking on the corresponding mutations tab. Provided
	 * tabs assumed to be the main tabs instance containing the mutation tabs.
	 *
	 * @param el        {String} container selector
	 * @param options   {Object} view (mapper) options
	 * @param tabs      {String} tabs selector (main tabs containing mutations tab)
	 * @param tabName   {String} name of the target tab (actual mutations tab)
	 * @return {MutationMapper}    a MutationMapper instance
	 */
	function delayedInitMutationMapper(el, options, tabs, tabName)
	{
		var mutationMapper = new MutationMapper(options);
		var initialized = false;

		// init view without a delay if the target container is already visible
		if ($(el).is(":visible"))
		{
			mutationMapper.init();
			initialized = true;
		}

		// add a click listener for the "mutations" tab
		$(tabs).bind("tabsactivate", function(event, ui) {
			// init when clicked on the mutations tab, and init only once
			if (ui.newTab.text().trim().toLowerCase() == tabName.toLowerCase())
			{
				// init only if it is not initialized yet
				if (!initialized)
				{
					mutationMapper.init();
					initialized = true;
				}
				// if already init, then refresh genes tab
				// (a fix for ui.tabs.plugin resize problem)
				else
				{
					mutationMapper.getView().refreshGenesTab();
				}
			}
		});

		return mutationMapper;
	}

	/**
	 * Returns all visual style mappings in a single object.
	 *
	 * @return {Object} style maps in a single object
	 */
	function getVisualStyleMaps()
	{
		return {
			mutationType: _mutationTypeMap,
			validationStatus: _validationStatusMap,
			mutationStatus: _mutationStatusMap,
			omaScore: _omaScoreMap,
			cna: _cnaMap
		};
	}

	function defaultTableTooltipOpts()
	{
		return {
			content: {attr: 'alt'},
			show: {event: 'mouseover'},
			hide: {fixed: true, delay: 100, event: 'mouseout'},
			style: {classes: 'mutation-details-tooltip qtip-shadow qtip-light qtip-rounded'},
			position: {my:'top left', at:'bottom right', viewport: $(window)}
		};
	}

	/**
	 * Renders a placeholder image for data tables cell.
	 *
	 * @param imageLocation place holder image location (url)
	 * @returns {String} html string
	 */
	function renderTablePlaceholder(imageLocation)
	{
		imageLocation = imageLocation || "images/ajax-loader.gif";

		// TODO customize width & height?
		var vars = {loaderImage: imageLocation, width: 15, height: 15};
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_placeholder_template");
		return templateFn(vars);
	}

	/**
	 * Refreshes the entire column in the data table.
	 * This function does NOT update the actual value of the cells.
	 * The update is for re-rendering purposes only.
	 *
	 * @param dataTable
	 * @param indexMap
	 * @param columnName
	 */
	function refreshTableColumn(dataTable, indexMap, columnName)
	{
		var tableData = dataTable.fnGetData();

		_.each(tableData, function(ele, i) {
			dataTable.fnUpdate(null, i, indexMap[columnName], false, false);
		});

		if (tableData.length > 0)
		{
			// this update is required to re-render the entire column!
			dataTable.fnUpdate(null, 0, indexMap[columnName]);
		}
	}

	return {
		initMutationMapper: delayedInitMutationMapper,
		renderTablePlaceHolder: renderTablePlaceholder,
		refreshTableColumn: refreshTableColumn,
		defaultTableTooltipOpts: defaultTableTooltipOpts,
		getVisualStyleMaps: getVisualStyleMaps
	};
})();

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility class to format Mutation Details Table View content.
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsTableFormatter = (function()
{
	var _visualStyleMaps = MutationViewsUtil.getVisualStyleMaps();

	var _mutationTypeMap = _visualStyleMaps.mutationType;
	var _validationStatusMap = _visualStyleMaps.validationStatus;
	var _mutationStatusMap = _visualStyleMaps.mutationStatus;
	var _omaScoreMap = _visualStyleMaps.omaScore;
	var _cnaMap = _visualStyleMaps.cna;

	// TODO identify similar get functions to avoid code duplication

	function getCNA(value)
	{
		return _getCNA(_cnaMap, value);
	}

	function _getCNA(map, value)
	{
		var style, label, tip;

		if (map[value] != null)
		{
			style = map[value].style;
			label = map[value].label;
			tip = map[value].tooltip;
		}
		else
		{
			style = map.unknown.style;
			label = map.unknown.label;
			tip = map.unknown.tooltip;
		}

		return {style: style, tip: tip, text: label};
	}

    /**
     * Returns the text content, the css class, and the tooltip
     * for the given case id value. If the length of the actual
     * case id string is too long, then creates a short form of
     * the case id ending with an ellipsis.
     *
     * @param caseId    actual case id string
     * @return {{style: string, text: string, tip: string}}
     * @private
     */
	function getCaseId(caseId)
	{
		// TODO customize this length?
		var maxLength = 16;

		var text = caseId;
		var style = ""; // no style for short case id strings
		var tip = caseId; // display full case id as a tip

		// no need to bother with clipping the text for 1 or 2 chars.
		if (caseId != null &&
		    caseId.length > maxLength + 2)
		{
			text = caseId.substring(0, maxLength) + "...";
			style = "simple-tip"; // enable tooltip for long strings
		}

		return {style: style, tip: tip, text: text};
	}

	function getMutationType(value)
	{
		return _getMutationType(_mutationTypeMap, value);
	}

    /**
     * Returns the text content and the css class for the given
     * mutation type value.
     *
     * @param map   map of <mutationType, {label, style}>
     * @param value actual string value of the mutation type
     * @return {{style: string, text: string}}
     * @private
     */
	function _getMutationType(map, value)
	{
		var style, text;

		if (value != null)
		{
			value = value.toLowerCase();
		}

		if (map[value] != null)
		{
			style = map[value].style;
			text = map[value].label;
		}
		else
		{
			style = map.other.style;
			text = value;
		}

		return {style: style, text: text};
	}

	function getMutationStatus(value)
	{
		return _getMutationStatus(_mutationStatusMap, value);
	}

	/**
     * Returns the text content, the css class, and the tooltip
	 * for the given mutation type value.
     *
     * @param map   map of <mutationStatus, {label, style, tooltip}>
     * @param value actual string value of the mutation status
     * @return {{style: string, text: string, tip: string}}
     * @private
     */
	function _getMutationStatus(map, value)
	{
		var style = "simple-tip";
		var text = value;
		var tip = "";

		if (value != null)
		{
			value = value.toLowerCase();
		}

		if (map[value] != null)
		{
			style = map[value].style;
			text = map[value].label;
			tip = map[value].tooltip;
		}

		return {style: style, tip: tip, text: text};
	}

	function getValidationStatus(value)
	{
		return _getValidationStatus(_validationStatusMap, value);
	}

	/**
	 * Returns the text content, the css class, and the tooltip
	 * for the given validation status value.
	 *
	 * @param map   map of <validationStatus, {label, style, tooltip}>
	 * @param value actual string value of the validation status
	 * @return {{style: string, text: string, tip: string}}
	 * @private
	 */
	function _getValidationStatus(map, value)
	{
		var style, label, tip;

		if (value != null)
		{
			value = value.toLowerCase();
		}

		if (map[value] != null)
		{
			style = map[value].style;
			label = map[value].label;
			tip = map[value].tooltip;
		}
		else
		{
			style = map.unknown.style;
			label = map.unknown.label;
			tip = map.unknown.tooltip;
		}

		return {style: style, tip: tip, text: label};
	}

	function getFis(fis, fisValue)
	{
		return _getFis(_omaScoreMap, fis, fisValue);
	}

	/**
	 * Returns the text content, the css classes, and the tooltip
	 * for the given string and numerical values of a
	 * functional impact score.
	 *
	 * @param map       map of <FIS, {label, style, tooltip}>
	 * @param fis       string value of the functional impact (h, l, m or n)
	 * @param fisValue  numerical value of the functional impact score
	 * @return {{fisClass: string, omaClass: string, value: string, text: string}}
	 * @private
	 */
	function _getFis(map, fis, fisValue)
	{
		var text = "";
		var fisClass = "";
		var omaClass = "";
		var value = "";

		if (fis != null)
		{
			fis = fis.toLowerCase();
		}

		if (map[fis] != null)
		{
			value = map[fis].tooltip;

			if (fisValue != null)
			{
				value = fisValue.toFixed(2);
			}

			text = map[fis].label;
			fisClass = map[fis].style;
			omaClass = "oma_link";
		}

		return {fisClass: fisClass, omaClass: omaClass, value: value, text: text};
	}

	/**
	 * Returns the text content, the css classes, and the total
	 * allele count for the given allele frequency.
	 *
	 * @param frequency allele frequency
	 * @param alt       alt allele count
	 * @param ref       ref allele count
	 * @param tipClass  css class for the tooltip
	 * @return {{text: string, total: number, style: string, tipClass: string}}
	 * @private
	 */
	function getAlleleFreq(frequency, alt, ref, tipClass)
	{
		var text = "NA";
		var total = alt + ref;
		var style = "";
		var tipStyle = "";

		if (frequency)
		{
			style = "mutation_table_allele_freq";
			text = frequency.toFixed(2);
			tipStyle = tipClass;
		}

		return {text: text, total: total, style: style, tipClass: tipStyle};
	}

	function getPdbMatchLink(mutation)
	{
		return getLink(mutation.get("pdbMatch"));
	}

	function getIgvLink(mutation)
	{
		return getLink(mutation.get("igvLink"));
	}

	function getLink(value)
	{
		if (value)
		{
			// this is not a real link,
			// actual action is performed by an event listener
			// "#" indicates that this is a valid link
			return "#";
		}
		else
		{
			// an empty string indicates that this is not a valid link
			// invalid links are removed by the view itself after rendering
			return "";
		}
	}

	function getProteinChange(mutation)
	{
		var style = "mutation-table-protein-change";
		var tip = "click to highlight the position on the diagram";
		var additionalTip = "";

		// TODO additional tooltips are enabled (hardcoded) only for msk-impact study for now
		// this is cBioPortal specific implementation, we may want to make it generic in the future
		if (mutation.get("aminoAcidChange") != null &&
		    mutation.get("aminoAcidChange").length > 0 &&
			mutation.get("aminoAcidChange") !== "NA" &&
			mutation.get("cancerStudyShort") != null &&
			mutation.get("cancerStudyShort").toLowerCase().indexOf("msk-impact") != -1 &&
		    isDifferentProteinChange(mutation.get("proteinChange"), mutation.get("aminoAcidChange")))
		{
			additionalTip = "The original annotation file indicates a different value: <b>" +
			                normalizeProteinChange(mutation.get("aminoAcidChange")) + "</b>";
		}

		// TODO disabled temporarily, enable when isoform support completely ready
//        if (!mutation.canonicalTranscript)
//        {
//            style = "best_effect_transcript " + style;
//            // TODO find a better way to display isoform information
//            tip = "Specified protein change is for the best effect transcript " +
//                "instead of the canonical transcript.<br>" +
//                "<br>RefSeq mRNA id: " + "<b>" + mutation.refseqMrnaId + "</b>" +
//                "<br>Codon change: " + "<b>" + mutation.codonChange + "</b>" +
//                "<br>Uniprot id: " + "<b>" + mutation.uniprotId + "</b>";
//        }

		return {text: normalizeProteinChange(mutation.get("proteinChange")),
			style : style,
			tip: tip,
			additionalTip: additionalTip};
	}

	/**
	 * Checks if given 2 protein changes are completely different from each other.
	 *
	 * @param proteinChange
	 * @param aminoAcidChange
	 * @returns {boolean}
	 */
	function isDifferentProteinChange(proteinChange, aminoAcidChange)
	{
		var different = false;

		proteinChange = normalizeProteinChange(proteinChange);
		aminoAcidChange = normalizeProteinChange(aminoAcidChange);

		// if the normalized strings are exact, no need to do anything further
		if (aminoAcidChange !== proteinChange)
		{
			// assuming each uppercase letter represents a single protein
			var proteinMatch1 = proteinChange.match(/[A-Z]/g);
			var proteinMatch2 = aminoAcidChange.match(/[A-Z]/g);

			// assuming the first numeric value is the location
			var locationMatch1 = proteinChange.match(/[0-9]+/);
			var locationMatch2 = aminoAcidChange.match(/[0-9]+/);

			// assuming first lowercase value is somehow related to
			var typeMatch1 = proteinChange.match(/([a-z]+)/);
			var typeMatch2 = aminoAcidChange.match(/([a-z]+)/);

			if (locationMatch1 && locationMatch2 &&
			    locationMatch1.length > 0 && locationMatch2.length > 0 &&
			    locationMatch1[0] != locationMatch2[0])
			{
				different = true;
			}
			else if (proteinMatch1 && proteinMatch2 &&
			         proteinMatch1.length > 0 && proteinMatch2.length > 0 &&
			         proteinMatch1[0] !== "X" && proteinMatch2[0] !== "X" &&
			         proteinMatch1[0] !== proteinMatch2[0])
			{
				different = true;
			}
			else if (proteinMatch1 && proteinMatch2 &&
			         proteinMatch1.length > 1 && proteinMatch2.length > 1 &&
			         proteinMatch1[1] !== proteinMatch2[1])
			{
				different = true;
			}
			else if (typeMatch1 && typeMatch2 &&
			         typeMatch1.length > 0 && typeMatch2.length > 0 &&
			         typeMatch1[0] !== typeMatch2[0])
			{
				different = true;
			}
		}

		return different;
	}

	function normalizeProteinChange(proteinChange)
	{
		var prefix = "p.";

		if (proteinChange.indexOf(prefix) != -1)
		{
			proteinChange = proteinChange.substr(proteinChange.indexOf(prefix) + prefix.length);
		}

		return proteinChange;
	}

	function getTumorType(mutation)
	{
		var style = "tumor_type";
		var tip = "";

		return {text: mutation.get("tumorType"),
			style : style,
			tip: tip};
	}

	/**
	 * Returns the css class and text for the given cosmic count.
	 *
	 * @param count number of occurrences
	 * @return {{style: string, count: string}}
	 * @private
	 */
	function getCosmic(count)
	{
		var style = "";
		var text = "";

		if (count > 0)
		{
			style = "mutation_table_cosmic";
			text = count;
		}

		return {style: style,
			count: text};
    }

	/**
	 * Returns the css class and text for the given cosmic count.
	 *
	 * @param frequency frequency value in cbio portal
	 * @return {{style: string, frequency: string}}
	 * @private
	 */
	function getCbioPortal(frequency)
	{
		var style = "";
		var text = "";

		if (frequency > 0)
		{
			style = "mutation_table_cbio_portal";
			text = frequency;
		}

		return {style: style,
			frequency: text};
	}

	/**
	 * Returns the text and css class values for the given integer value.
	 *
	 * @param value an integer value
	 * @return {{text: *, style: string}}
	 * @private
	 */
	function getIntValue(value)
	{
		var text = value;
		var style = "mutation_table_int_value";

		if (value == null)
		{
			text = "NA";
			style = "";
		}

		return {text: text, style: style};
	}

	/**
	 * Returns the text and css class values for the given allele count value.
	 *
	 * @param count an integer value
	 * @return {{text: *, style: string}}
	 * @private
	 */
	function getAlleleCount(count)
	{
		var text = count;
		var style = "mutation_table_allele_count";

		if (count == null)
		{
			text = "NA";
			style = "";
		}

		return {text: text, style: style};
    }


	/**
	 * Helper function for predicted impact score sorting.
	 */
	function assignValueToPredictedImpact(text, score)
	{
		// using score by itself may be sufficient,
		// but sometimes we have no numerical score value

		var value;

		if (text != null)
		{
			text = text.toLowerCase();
		}

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

	function assignIntValue(value)
	{
		var val = parseInt(value);

		if (isNaN(val))
		{
			val = -Infinity;
		}

		return val;
	}

	function assignFloatValue(value)
	{
		var val = parseFloat(value);

		if (isNaN(val))
		{
			val = -Infinity;
		}

		return val;
	}

	return {
		getCaseId: getCaseId,
		getProteinChange: getProteinChange,
		getPdbMatchLink: getPdbMatchLink,
		getIgvLink: getIgvLink,
		getAlleleCount: getAlleleCount,
		getAlleleFreq: getAlleleFreq,
		getCNA: getCNA,
		getMutationType: getMutationType,
		getMutationStatus: getMutationStatus,
		getValidationStatus: getValidationStatus,
		getFis: getFis,
		getTumorType: getTumorType,
		getCosmic: getCosmic,
		getCbioPortal: getCbioPortal,
		getIntValue: getIntValue,
		assignValueToPredictedImpact: assignValueToPredictedImpact,
		assignIntValue: assignIntValue,
		assignFloatValue: assignFloatValue
	}
})();


/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility class for pileup related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var PileupUtil = (function()
{
	var _idCounter = 0;

	/**
	 * Processes a Pileup instance, and creates a map of
	 * <mutation type, mutation array> pairs.
	 *
	 * @param pileup    a pileup instance
	 * @return {object} map of mutations (keyed on mutation type)
	 * @private
	 */
	function generateTypeMap(pileup)
	{
		var mutations = pileup.mutations;
		var mutationMap = {};

		// process raw data to group mutations by types
		_.each(mutations, function(mutation) {
			var type = mutation.get("mutationType") || "";
			type = type.trim().toLowerCase();

			if (mutationMap[type] == undefined)
			{
				mutationMap[type] = [];
			}

			mutationMap[type].push(mutation);
		});

		return mutationMap;
	}

	/**
	 * Processes a Pileup instance, and creates an array of
	 * <mutation type, count> pairs. The final array is sorted
	 * by mutation count.
	 *
	 * @param pileup    a pileup instance
	 * @return {Array}  array of mutation type and count pairs
	 */
	function groupMutationsByType(pileup)
	{
		var map = generateTypeMap(pileup);
		var typeArray = [];

		// convert to array and sort by length (count)
		_.each(_.keys(map), function(key) {
			typeArray.push({type: key, count: map[key].length});
		});

		typeArray.sort(function(a, b) {
			// TODO tie condition: priority?
			// descending sort
			return b.count - a.count;
		});

		return typeArray;
	}

	/**
	 * Processes a Pileup instance, and creates an array of
	 * <mutation type group, count> pairs. The final array
	 * is sorted by mutation count.
	 *
	 * @param pileup    a pileup instance
	 * @return {Array}  array of mutation type group and count pairs
	 */
	function groupMutationsByMainType(pileup)
	{
		var mutationTypeMap = MutationViewsUtil.getVisualStyleMaps().mutationType;

		var typeMap = generateTypeMap(pileup);
		var groupArray = [];
		var groupCountMap = {};

		// group mutation types by using the type map
		// and count number of mutations in a group

		_.each(_.keys(typeMap), function(type) {
			// grouping mutations by the style (not by the type)
			var group = undefined;

			if (mutationTypeMap[type] != null)
			{
				group = mutationTypeMap[type].mainType;
			}

			if (group == undefined)
			{
				group = mutationTypeMap.other.mainType;
			}

			if (groupCountMap[group] == undefined)
			{
				// init count
				groupCountMap[group] = 0;
			}

			groupCountMap[group] += typeMap[type].length;
		});

		// convert to array and sort by length (count)

		_.each(_.keys(groupCountMap), function(group) {
			groupArray.push({type: group,
				count: groupCountMap[group],
				priority: mutationTypeMap[group].priority});
		});

		groupArray.sort(function(a, b) {
			if (b.count === a.count) {
				// tie condition: use mutation type priority
				return b.priority - a.priority;
			}
			else {
				// descending sort
				return b.count - a.count;
			}
		});

		return groupArray;
	}

	function nextId()
	{
		_idCounter++;

		return "pileup_" + _idCounter;
	}

	/**
	 * Creates a map of <mutation sid>, <pileup id> pairs.
	 *
	 * @param pileups   list of pileups
	 * @return {Object} <mutation sid> to <pileup id> map
	 */
	function mapToMutations(pileups)
	{
		var map = {};

		// map each mutation sid to its corresponding pileup
		_.each(pileups, function(pileup) {
			_.each(pileup.mutations, function(mutation) {
				map[mutation.get("mutationSid")] = pileup.pileupId;
			})
		});

		return map;
	}

	/**
	 * Converts the provided mutation data into a list of Pileup instances.
	 *
	 * @param mutationColl  collection of Mutation models (MutationCollection)
	 * @param converter     [optional] custom pileup converter function
	 * @return {Array}      a list of pileup mutations
	 */
	function convertToPileups(mutationColl, converter)
	{
		// remove redundant mutations by sid
		mutationColl = removeRedundantMutations(mutationColl);

		// create a map of mutations (key is the mutation location)
		var mutations = {};

		for (var i=0; i < mutationColl.length; i++)
		{
			var mutation = mutationColl.at(i);

			var location = mutation.getProteinStartPos();
			var type = mutation.get("mutationType") || "";
			type = type.trim().toLowerCase();

			if (location != null && type != "fusion")
			{
				if (mutations[location] == null)
				{
					mutations[location] = [];
				}

				mutations[location].push(mutation);
			}
		}

		// convert map into an array of piled mutation objects
		var pileupList = [];

		_.each(_.keys(mutations), function(key) {
			var pileup = {};

			if (_.isFunction(converter)) {
				pileup = converter(mutations, key);
			}
			else {
				pileup = initPileup(mutations, key);
			}

			pileupList.push(new Pileup(pileup));
		});

		// sort (descending) the list wrt mutation count
		pileupList.sort(function(a, b) {
			var diff = b.count - a.count;

			// if equal, then compare wrt position (for consistency)
			if (diff == 0)
			{
				diff = b.location - a.location;
			}

			return diff;
		});

		return pileupList;
	}

	function initPileup(mutations, location)
	{
		var pileup = {};

		pileup.pileupId = PileupUtil.nextId();
		pileup.mutations = mutations[location];
		pileup.count = mutations[location].length;
		pileup.location = parseInt(location);
		pileup.label = generateLabel(mutations[location]);

		// TODO can we separate this in the cbioportal codebase as a custom converter?
		// The following calculates dist of mutations by cancer type
		pileup.stats = _.chain(mutations[location])
			.groupBy(function(mut) { return mut.get("cancerType"); })
			.sortBy(function(stat) { return -stat.length; })
			.reduce(function(seed, o) {
				seed.push({ cancerType: o[0].get("cancerType"), count: o.length });
				return seed;
			}, []).value();

		return pileup;
	}

	// TODO first remove by mutationSid, and then remove by patientId
	function removeRedundantMutations(mutationData)
	{
		// remove redundant mutations by sid
		var redMap = {};
		var removeItems = [];

		for (var i=0; i < mutationData.length; i++)
		{
			var aMutation = mutationData.at(i);
			var exists = redMap[aMutation.get("mutationSid")];
			if(exists == null) {
				redMap[aMutation.get("mutationSid")] = true;
			} else {
				removeItems.push(aMutation);
			}
		}

		mutationData.remove(removeItems);

		return mutationData;
	}

	/**
	 * Generates a pileup label by joining all unique protein change
	 * information in the given array of mutations.
	 *
 	 * @param mutations     a list of mutations
	 * @returns {string}    pileup label
	 */
	function generateLabel(mutations)
	{
		var mutationSet = {};

		// create a set of protein change labels
		// (this is to eliminate duplicates)
		_.each(mutations, function(mutation) {
			if (mutation.get("proteinChange") != null &&
			    mutation.get("proteinChange").length > 0)
			{
				mutationSet[mutation.get("proteinChange")] = mutation.get("proteinChange");
			}
		});

		// convert to array & sort
		var mutationArray = _.keys(mutationSet).sort();

		// find longest common starting substring
		// (this is to truncate redundant starting substring)

		var startStr = "";

		if (mutationArray.length > 1)
		{
			startStr = cbio.util.lcss(mutationArray[0],
			                          mutationArray[mutationArray.length - 1]);

//			console.log(mutationArray[0] + " n " +
//			            mutationArray[mutationArray.length - 1] + " = " +
//			            startStr);
		}

		// generate the string
		var label = startStr;

		_.each(mutationArray, function(mutation) {
			label += mutation.substring(startStr.length) + "/";
		});

		// remove the last slash
		return label.substring(0, label.length - 1);
	}

	/**
	 * Counts the number of total mutations for the given
	 * Pileup array.
	 *
	 * @param pileups   an array of Pileup instances
	 */
	function countMutations(pileups)
	{
		var total = 0;

		_.each(pileups, function(pileup) {
			total += pileup.count;
		});

		return total;
	}

	/**
	 * Returns all the mutation model instances within the given
	 * collection of pileups.
	 *
	 * @param pileups   a collection of pileups
	 * @returns {Array} mutations within the given pileups
	 */
	function getPileupMutations(pileups)
	{
		var mutations = [];

		_.each(pileups, function(pileup) {
			mutations = mutations.concat(pileup.get("mutations") || []);
		});

		return mutations;
	}

	return {
		nextId: nextId,
		mapToMutations: mapToMutations,
		convertToPileups: convertToPileups,
		initPileup: initPileup,
		countMutations: countMutations,
		getPileupMutations: getPileupMutations,
		getMutationTypeMap: generateTypeMap,
		groupMutationsByType: groupMutationsByType,
		groupMutationsByMainType: groupMutationsByMainType
	};
})();
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility class to precompile & cache backbone templates.
 * Using precompiled templates increases rendering speed dramatically.
 *
 * @author Selcuk Onur Sumer
 */
var BackboneTemplateCache = (function () {
	var _cache = {};

	/**
	 * Compiles the template for the given template id
	 * by using underscore template function.
	 *
	 * @param templateId    html id of the template content
	 * @returns function    compiled template function
	 */
	function compileTemplate(templateId)
	{
		return _.template($("#" + templateId).html());
	}

	/**
	 * Gets the template function corresponding to the given template id.
	 *
	 * @param templateId    html id of the template content
	 * @returns function    template function
	 */
	function getTemplateFn(templateId)
	{
		// try to use the cached value first
		var templateFn = _cache[templateId];

		// compile if not compiled yet
		if (templateFn == null)
		{
			templateFn = compileTemplate(templateId);
			_cache[templateId] = templateFn;
		}

		return templateFn;
	}

	return {
		getTemplateFn: getTemplateFn
	};
})();

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility class for data proxy related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var DataProxyUtil = (function()
{
	/**
	 * Initializes data proxy instances for the given options.
	 *
	 * @param options   data proxy options (for all proxies)
	 */
	function initDataProxies(options)
	{
		// init proxies
		var dataProxies = {};

		// workaround: alphabetically sorting to ensure that mutationProxy is
		// initialized before pdpProxy, since pdbProxy depends on the mutationProxy instance
		_.each(_.keys(options).sort(), function(proxy) {
			var proxyOpts = options[proxy];
			var instance = null;

			// TODO see if it is possible to remove pdb proxy's dependency on mutation proxy

			// special initialization required for mutation proxy
			// and pdb proxy, so a custom function is provided
			// as an additional parameter to the initDataProxy function
			if (proxy == "pdbProxy")
			{
				instance = initDataProxy(proxyOpts, function(proxyOpts) {
					var mutationProxy = dataProxies["mutationProxy"];

					if (mutationProxy != null &&
					    mutationProxy.hasData())
					{
						proxyOpts.options.mutationUtil = mutationProxy.getMutationUtil();
						return true;
					}
					else
					{
						// do not initialize pdbProxy at all
						return false;
					}
				});
			}
			else
			{
				// regular init for all other proxies...
				instance = initDataProxy(proxyOpts);
			}

			dataProxies[proxy] = instance;
		});

		return dataProxies;
	}

	/**
	 *
	 * @param proxyOpts     data proxy options (for a single proxy)
	 * @param preProcessFn  [optional] pre processing function, should return a boolean value.
	 * @returns {Object}    a data proxy instance
	 */
	function initDataProxy(proxyOpts, preProcessFn)
	{
		// use the provided custom instance if available
		var instance = proxyOpts.instance;

		if (instance == null)
		{
			// custom pre process function for the proxy options
			// before initialization
			if (preProcessFn != null &&
			    _.isFunction(preProcessFn))
			{
				// if preprocess is not successful do not initialize
				if (!preProcessFn(proxyOpts))
				{
					return null;
				}
			}

			// init data proxy
			var Constructor = proxyOpts.instanceClass;
			instance = new Constructor(proxyOpts.options);
			instance.init();
		}

		return instance;
	}

	return {
		initDataProxies: initDataProxies,
		initDataProxy: initDataProxy
	};
})();

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Utility class to initialize the 3D mutation visualizer with JSmol (HTML5)
 * instance.
 *
 * This class is designed to initialize the JSmol visualizer within
 * a separate frame (due to JSmol incompatibilities with jQuery lib)
 *
 * Note: This class is assumed to have the same interface
 * (the same signature for all public functions) with the JmolWrapper.
 *
 * @author Selcuk Onur Sumer
 */
function JSmolWrapper()
{
	var _appName = null;
	var _options = null;
	var _frameHtml = null;
	var _targetWindow = null;
	var _targetDocument = null;
	var _container = null;
	var _origin = cbio.util.getWindowOrigin();
	var _commandQueue = [];
	var _commandMap = {};

	var _idCounter = 0;

	// default options
	var defaultOpts = {
		use: "HTML5",
		j2sPath: "js/lib/jsmol/j2s",
		//defaultModel: "$dopamine",
		disableJ2SLoadMonitor: true,
		disableInitialConsole: true
	};

	/**
	 * Initializes the visualizer.
	 *
	 * @param name      name of the application
	 * @param options   app options
	 * @param frame     jsmol frame location
	 */
	function init(name, options, frame)
	{
		// init vars
		_appName = name;
		_options = jQuery.extend(true, {}, defaultOpts, options);

		var w = _options.width;
		// TODO this (x4) is a workaround for the menu to overflow
		var h = _options.height * 4;

		// TODO send custom opts via GET? (i.e: jsmol_frame.jsp?name=n&width=400&...)
		_frameHtml = '<iframe id="jsmol_frame" ' +
		             'src="' + frame + '" ' +
		             'seamless="seamless" ' +
		             'width="' + w + '" ' +
		             'height="' + h + '" ' +
		             'frameBorder="0" ' +
		             'scrolling="no"></iframe>';

		// add listener to process messages coming from the iFrame

		var _processMessage = function(event)
		{
			// only accept messages from the local origin
			if (cbio.util.getWindowOrigin() != event.origin)
			{
				return;
			}

			// ready event: supposed to be fired when frame gets ready
			if (event.data.type == "ready")
			{
				if (_targetWindow)
				{
					_targetDocument = cbio.util.getTargetDocument("jsmol_frame");

					// TODO JSmol init doesn't work after document ready
					//var data = {type: "init", content: _options};
					//_targetWindow.postMessage(data, _origin);
				}
			}
			// menu event: supposed to be fired when JSmol menu becomes active
			else if (event.data.type == "menu")
			{
				// show or hide the overlay wrt the menu event
				if (_container)
				{
					if (event.data.content == "visible")
					{
						_container.css("overflow", "visible");
					}
					else if (event.data.content == "hidden")
					{
						_container.css("overflow", "hidden");
					}
				}
			}
			// done event: supposed to be fired when JSmol finishes executing a script
			else if (event.data.type == "done")
			{
				var command = _commandMap[event.data.scriptId];

				if (command != null)
				{
					// remove the command to prevent possible multiple executions
					delete _commandMap[event.data.scriptId];

					// check for a registered callback

					var callback = command.callback;

					if (callback &&
					    _.isFunction(callback))
					{
						// call the registered callback function
						callback();
					}
				}

				// see if there are more commands to send
				if (!_.isEmpty(_commandQueue))
				{
					// get the next command from the queue
					command = _commandQueue.shift();
					// add it to the map to access the callback when "done"
					_commandMap[command.data.scriptId] = command;
					// send the command
					_targetWindow.postMessage(command.data, _origin);
				}
			}
		};

		window.addEventListener("message", _processMessage, false);
	}

	/**
	 * Updates the container of the visualizer object.
	 *
	 * @param container container selector
	 */
	function updateContainer(container)
	{
		// init the iFrame for the given container
		if (container && _frameHtml)
		{
			container.empty();
			container.append(_frameHtml);
			_container = container;
		}

		_targetWindow = cbio.util.getTargetWindow("jsmol_frame");

		if (!_targetWindow)
		{
			console.log("warning: JSmol frame cannot be initialized properly");
		}
	}

	/**
	 * Runs the given command as a script on the 3D visualizer object.
	 *
	 * @param command   command to send
	 * @param callback  function to call after execution of the script
	 */
	function script(command, callback)
	{
		if (_targetWindow)
		{
			_idCounter = (_idCounter + 1) % 1000000;

			var data = {type: "script",
				content: command,
				scriptId: "script_" + (_idCounter)};

			// queue the command, this prevents simultaneous JSmol script calls
			queue(data, callback);
		}
	}

	/**
	 * Adds the given data package and callback function to the command queue.
	 * If the queue is currently empty, immediately posts the message to the
	 * target window.
	 *
	 * @param data      data package to send
	 * @param callback  callback function for this command
	 */
	function queue(data, callback)
	{
		var command = {data: data, callback: callback};

		// TODO not always safe -- producer/consumer problem, may run into a deadlock...

		// immediately post message if the queue is empty
		if (_.isEmpty(_commandQueue))
		{
			// add the command to the map to access the callback when "done"
			_commandMap[command.data.scriptId] = command;
			// send the command
			_targetWindow.postMessage(data, _origin);
		}
		// add command to the queue (message will be post after a "done" event)
		else
		{
			// add the command to the queue
			_commandQueue.push(command);
		}
	}

	return {
		init: init,
		updateContainer: updateContainer,
		script: script
	};
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * JmolScriptGenerator class (extends MolScriptGenerator)
 *
 * Script generator for Jmol/JSmol applications.
 *
 * @author Selcuk Onur Sumer
 */
function JmolScriptGenerator()
{
	// Predefined style scripts for Jmol
	var _styleScripts = {
		ballAndStick: "wireframe ONLY; wireframe 0.15; spacefill 20%;",
		spaceFilling: "spacefill ONLY; spacefill 100%;",
		ribbon: "ribbon ONLY;",
		cartoon: "cartoon ONLY;",
		trace: "trace ONLY;"
	};

	function loadPdb(pdbId)
	{
		return "load=" + pdbId + ";";
	}

	function selectAll()
	{
		return "select all;";
	}

	function selectNone()
	{
		return "select none;";
	}

	function setScheme(schemeName)
	{
		return _styleScripts[schemeName];
	}

	function setColor (color)
	{
		return "color [" + formatColor(color) + "];"
	}

	function selectChain(chainId)
	{
		return "select :" + chainId + ";";
	}

	function selectAlphaHelix(chainId)
	{
		return "select :" + chainId + " and helix;";
	}

	function selectBetaSheet(chainId)
	{
		return "select :" + chainId + " and sheet;";
	}

	function rainbowColor(chainId)
	{
		// min atom no within the selected chain
		var rangeMin = "@{{:" + chainId + "}.atomNo.min}";
		// max atom no within the selected chain
		var rangeMax = "@{{:" + chainId + "}.atomNo.max}";

		// max residue no within the selected chain
		//var rangeMin = "@{{:" + chain.chainId + "}.resNo.min}";
		// max residue no within the selected chain
		//var rangeMax = "@{{:" + chain.chainId + "}.resNo.max}";

		// color the chain by rainbow coloring scheme (gradient coloring)
		return 'color atoms property atomNo "roygb" ' +
			'range ' + rangeMin + ' ' + rangeMax + ';';
	}

	function cpkColor(chainId)
	{
		return "color atoms CPK;";
	}

	function hideBoundMolecules()
	{
		return "restrict protein;";
	}

	function setTransparency(transparency)
	{
		// TODO we should use the given transparency value...
		if (transparency > 0)
		{
			return "color translucent;";
		}
		else
		{
			return "color opaque;";
		}
	}

	/**
	 * Generates a position string for Jmol scripting.
	 *
	 * @position object containing PDB position info
	 * @return {string} position string for Jmol
	 */
	function scriptPosition(position)
	{
		var insertionStr = function(insertion) {
			var posStr = "";

			if (insertion != null &&
			    insertion.length > 0)
			{
				posStr += "^" + insertion;
			}

			return posStr;
		};

		var startPdbPos = position.start.pdbPos || position.start.pdbPosition;
		var endPdbPos = position.end.pdbPos || position.end.pdbPosition;

		var posStr = startPdbPos +
		             insertionStr(position.start.insertion);

		if (endPdbPos > startPdbPos)
		{
			posStr += "-" + endPdbPos +
			          insertionStr(position.end.insertion);
		}

		return posStr;
	}

	function selectPositions(scriptPositions, chainId)
	{
		return "select (" + scriptPositions.join(", ") + ") and :" + chainId + ";";
	}

	function selectSideChains(scriptPositions, chainId)
	{
		return "select ((" + scriptPositions.join(", ") + ") and :" + chainId + " and sidechain) or " +
		"((" + scriptPositions.join(", ") + ") and :" + chainId + " and *.CA);"
	}

	function enableBallAndStick()
	{
		return "wireframe 0.15; spacefill 25%;";
	}

	function disableBallAndStick()
	{
		return "wireframe OFF; spacefill OFF;";
	}

	function center(position, chainId)
	{
		var self = this;
		var scriptPos = self.scriptPosition(position);
		return "center " + scriptPos + ":" + chainId + ";"
	}

	function defaultCenter()
	{
		return "center;";
	}

	function zoom(zoomValue)
	{
		// center and zoom to the selection
		return "zoom " + zoomValue + ";";
	}

	function defaultZoomIn()
	{
		return "zoom in;"
	}

	function defaultZoomOut()
	{
		return "zoom out;"
	}

	function spin(value)
	{
		return "spin " + value + ";";
	}

	/**
	 * Generates highlight script by using the converted highlight positions.
	 *
	 * @param scriptPositions   script positions
	 * @param color             highlight color
	 * @param options           visual style options
	 * @param chain             a PdbChainModel instance
	 * @return {Array} script lines as an array
	 */
	function highlightScript(scriptPositions, color, options, chain)
	{
		var self = this;
		var script = [];

		// add highlight color
		// "select (" + scriptPositions.join(", ") + ") and :" + chain.chainId + ";"
		script.push(self.selectPositions(scriptPositions, chain.chainId));
		script.push(self.setColor(color));

		var displaySideChain = options.displaySideChain != "none";

		// show/hide side chains
		script = script.concat(
			self.generateSideChainScript(scriptPositions, displaySideChain, options, chain));

		return script;
	}

	function formatColor(color)
	{
		// this is for Jmol compatibility
		// (colors should start with an "x" instead of "#")
		return color.replace("#", "x");
	}

	// override required functions
	this.loadPdb = loadPdb;
	this.selectAll = selectAll;
	this.selectNone = selectNone;
	this.setScheme = setScheme;
	this.setColor = setColor;
	this.selectChain = selectChain;
	this.selectAlphaHelix = selectAlphaHelix;
	this.selectBetaSheet = selectBetaSheet;
	this.rainbowColor = rainbowColor;
	this.cpkColor = cpkColor;
	this.hideBoundMolecules = hideBoundMolecules;
	this.setTransparency = setTransparency;
	this.scriptPosition = scriptPosition;
	this.selectPositions = selectPositions;
	this.selectSideChains = selectSideChains;
	this.enableBallAndStick = enableBallAndStick;
	this.disableBallAndStick = disableBallAndStick;
	this.highlightScript = highlightScript;
	this.center = center;
	this.defaultZoomIn = defaultZoomIn;
	this.defaultZoomOut = defaultZoomOut;
	this.defaultCenter = defaultCenter;
	this.spin = spin;
}

// JmolScriptGenerator extends MolScriptGenerator...
JmolScriptGenerator.prototype = new MolScriptGenerator();
JmolScriptGenerator.prototype.constructor = JmolScriptGenerator;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Utility class to initialize the 3D mutation visualizer with Jmol (Java)
 * instance.
 *
 * Note: This class is assumed to have the same interface
 * (the same signature for all public functions) with the JSmolWrapper.
 *
 * @author Selcuk Onur Sumer
 */
function JmolWrapper(useJava)
{
	// Jmol applet reference
	var _applet = null;

	// wrapper, created by the Jmol lib -- html element
	var _wrapper = null;

	// default options (parameters required to init with the applet)
	var defaultOpts = initDefaultOpts(useJava);

	var _options = null;

	/**
	 * Initializes the visualizer.
	 *
	 * @param name      name of the application
	 * @param options   app options
	 */
	function init(name, options)
	{
		_options = jQuery.extend(true, {}, defaultOpts, options);

		// disable the Jmol tracker
		delete Jmol._tracker;

		// init applet
		_applet = Jmol.getApplet(name, _options);

		// update wrapper reference
		// TODO the wrapper id depends on the JMol implementation
		_wrapper = $("#" + name + "_appletinfotablediv");
		_wrapper.hide();
	}

	/**
	 * Updates the container of the visualizer object.
	 *
	 * @param container container selector
	 */
	function updateContainer(container)
	{
		// move visualizer into its new container
		if (_wrapper != null)
		{
			container.append(_wrapper);
			_wrapper.show();
		}
	}

	/**
	 * Runs the given command as a script on the 3D visualizer object.
	 *
	 * @param command   command to send
	 * @param callback  function to call after execution of the script
	 */
	function script(command, callback)
	{
		// run Jmol script
		Jmol.script(_applet, command);

		// call the callback function after script completed
		if(_.isFunction(callback))
		{
			callback();
		}
	}

	function initDefaultOpts(useJava)
	{
		if (useJava)
		{
			return {
				//defaultModel: "$dopamine",
				jarPath: "js/lib/jmol/",
				jarFile: "JmolAppletSigned.jar",
				disableJ2SLoadMonitor: true,
				disableInitialConsole: true
			};
		}
		else
		{
			return {
				use: "HTML5",
				j2sPath: "js/lib/jsmol/j2s",
				disableJ2SLoadMonitor: true,
				disableInitialConsole: true
			}
		}
	}

	return {
		init: init,
		updateContainer: updateContainer,
		script: script
	};
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Utility class to create segments from a merged alignment.
 * (See PdbChainModel.mergeAlignments function for details of merged alignments)
 *
 * @param mergedAlignment   merged alignment object (see PdbChainModel.mergedAlignment field)
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MergedAlignmentSegmentor(mergedAlignment)
{
	var _mergedAlignment = mergedAlignment;

	// start position (initially zero)
	var _start = 0;

	/**
	 * Checks if there are more segments in this merged alignment.
	 *
	 * @return {boolean}
	 */
	function hasNextSegment()
	{
		return (_start < _mergedAlignment.mergedString.length);
	}

	/**
	 * Extracts the next segment from the merged alignment string. Returns
	 * the segment as an object with the actual segment string,
	 * start (uniprot) position, and end (uniprot) position.
	 *
	 * @return {object} segment with string, start, and end info
	 */
	function getNextSegment()
	{
		var str = _mergedAlignment.mergedString;

		var segment = {};
		segment.start = _start + _mergedAlignment.uniprotFrom;
		var symbol = str[_start];
		var end = _start;

		// for each special symbol block, a new segment is created
		if (isSpecialSymbol(symbol))
		{
			segment.type = symbol;

			while (str[end] == symbol &&
			       end <= str.length)
			{
				end++;
			}
		}
		else
		{
			segment.type = "regular";

			while (!isSpecialSymbol(str[end]) &&
			       end <= str.length)
			{
				end++;
			}
		}

		segment.end = end + _mergedAlignment.uniprotFrom;
		segment.str = str.substring(_start, end);

		// update start for the next segment
		_start = end;

		return segment;
	}

	function isSpecialSymbol(symbol)
	{
		// considering symbols other than GAP as special
		// results in too many segments...
//		return (symbol == PdbDataUtil.ALIGNMENT_GAP) ||
//		       (symbol == PdbDataUtil.ALIGNMENT_MINUS) ||
//		       (symbol == PdbDataUtil.ALIGNMENT_PLUS) ||
//		       (symbol == PdbDataUtil.ALIGNMENT_SPACE);

		return (symbol == PdbDataUtil.ALIGNMENT_GAP);
	}

	return {
		hasNextSegment: hasNextSegment,
		getNextSegment: getNextSegment
	};
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Mol3DScriptGenerator class (extends MolScriptGenerator)
 *
 * Script generator for 3Dmol.js applications.
 *
 * @author Selcuk Onur Sumer
 */
function Mol3DScriptGenerator()
{
	// PDB URI to use to download PDB data
	var _pdbUri = null;

	// reference to the 3Dmol viewer.
	var _viewer = null;

	// latest selection
	var _selected = null;

	// latest style
	var _style = null;

	// latest color
	var _color = null;

	var _styleSpecs = {
		ballAndStick: {stick: {}, sphere: {scale: 0.25}},
		spaceFilling: {sphere: {scale: 0.6}},
		cartoon: {cartoon: {}},
		ribbon: {cartoon: {style: "ribbon"}},
		trace: {cartoon: {style: "trace"}}
	};

	/**
	 * Loads the pdb file for the given pdb ID.
	 *
	 * @param pdbId     pdb ID to load
	 * @param callback  to be invoked after the model is loaded
	 * @returns {string}
	 */
	function loadPdb(pdbId, callback)
	{
		// clear current content
		_viewer.clear();

		var options = {
			doAssembly: true,
			pdbUri: _pdbUri
		};
		// reload with the given pdbId
		$3Dmol.download("pdb:" + pdbId, _viewer, options, callback);
		return "$3Dmol";
	}

	function selectAll()
	{
		_selected = {};
		return "";
	}

	function setScheme(schemeName)
	{
		_style = _.extend({}, _styleSpecs[schemeName]);
		_viewer.setStyle(_selected, _style);
		return "";
	}

	function setColor(color)
	{
		// save the color selection
		_color = formatColor(color);

		// update current style with color information
		_.each(_style, function(ele) {
			ele.color = _color;
		});

		_viewer.setStyle(_selected, _style);
		return "";
	}

	function selectChain(chainId)
	{
		_selected = {chain: chainId};
		return "";
	}

	function selectAlphaHelix(chainId)
	{
		_selected = {chain: chainId, ss: "h"};
		return "";
	}

	function selectBetaSheet(chainId)
	{
		_selected = {chain: chainId, ss: "s"};
		return "";
	}

	/**
	 * Generates a position array for 3Dmol.js.
	 *
	 * @position object containing PDB position info
	 * @return {Array} residue code (rescode) array for 3Dmol.js
	 */
	function scriptPosition(position)
	{
		var residues = [];
		var startPdbPos = position.start.pdbPos || position.start.pdbPosition;
		var endPdbPos = position.end.pdbPos || position.end.pdbPosition;

		var start = parseInt(startPdbPos);
		var end = parseInt(endPdbPos);

		for (var i=start; i <= end; i++)
		{
			residues.push(i);
		}

		// TODO this may not be accurate if residues.length > 2

		if (position.start.insertion)
		{
			residues[0] += "^" + position.start.insertion;
		}

		if (residues.length > 1 &&
		    position.end.insertion)
		{
			residues[residues.length - 1] += "^" + position.end.insertion;
		}

		return residues;
	}

	function selectPositions(scriptPositions, chainId)
	{
		_selected = {rescode: scriptPositions, chain: chainId};
		return "";
	}

	function selectSideChains(scriptPositions, chainId)
	{
		// TODO determine side chain atoms!
		_selected = {
			rescode: scriptPositions,
			chain: chainId/*,
			atom: ["CA"]*/
		};
		return "";
	}

	/**
	 * Generates highlight script by using the converted highlight positions.
	 *
	 * @param scriptPositions   script positions
	 * @param color             highlight color
	 * @param options           visual style options
	 * @param chain             a PdbChainModel instance
	 * @return {Array} script lines as an array
	 */
	function highlightScript(scriptPositions, color, options, chain)
	{
		var self = this;
		var script = [""];

		// add highlight color
		self.selectPositions(scriptPositions, chain.chainId);
		self.setColor(color);

		var displaySideChain = options.displaySideChain != "none";

		// show/hide side chains
		self.generateSideChainScript(scriptPositions, displaySideChain, options, chain);

		return script;
	}

	function enableBallAndStick()
	{
		// extend current style with ball and stick
		var style = _.extend({}, _style, _styleSpecs.ballAndStick);
		// use the latest defined color
		// (this is not the best function to set the side chain color, it should be set
		// in a method like generateSideChainScript or generateVisualStyleScript)
		style.sphere.color = _color;
		style.stick.color = _color;
		// update style of the selection
		_viewer.setStyle(_selected, style);
		return "";
	}

	function disableBallAndStick()
	{
		// looks like this method is obsolete for 3Dmol.js
		//return "wireframe OFF; spacefill OFF;";
		return "";
	}

	function rainbowColor(chainId)
	{
		_selected = {chain: chainId};
		setColor("spectrum");
		return "";
	}

	function cpkColor(chainId)
	{
		_selected = {chain: chainId};

		_.each(_style, function(ele) {
			// remove previous single color
			delete ele.color;

			// add default color scheme
			ele.colors = $3Dmol.elementColors.defaultColors;
		});

		_viewer.setStyle(_selected, _style);
		return "";
	}

	function formatColor(color)
	{
		// this is for 3Dmol.js compatibility
		// (colors should start with an "0x" instead of "#")
		return color.replace("#", "0x");
	}

	function setViewer(viewer)
	{
		_viewer = viewer;
	}

	function setPdbUri(pdbUri)
	{
		_pdbUri = pdbUri;
	}

	function hideBoundMolecules()
	{
		// since there is no built-in "restrict protein" command,
		// we need to select all non-protein structure...
		var selected = {
			resn: [
				"asp", "glu", "arg", "lys", "his", "asn", "thr", "cys", "gln", "tyr", "ser",
				"gly", "ala", "leu", "val", "ile", "met", "trp", "phe", "pro",
				"ASP", "GLU", "ARG", "LYS", "HIS", "ASN", "THR", "CYS", "GLN", "TYR", "SER",
				"GLY", "ALA", "LEU", "VAL", "ILE", "MET", "TRP", "PHE", "PRO"
			],
			invert: true
		};

		var style = {sphere: {hidden: true}};
		_viewer.setStyle(selected, style);
	}

	function setTransparency(transparency)
	{
		_.each(_style, function(ele) {
			ele.opacity = (10 - transparency) / 10;
		});

		_viewer.setStyle(_selected, _style);
	}

	// class specific functions
	this.setViewer = setViewer;
	this.setPdbUri = setPdbUri;

	// override required functions
	this.loadPdb = loadPdb;
	this.selectAll = selectAll;
	this.setScheme = setScheme;
	this.setColor = setColor;
	this.selectChain = selectChain;
	this.selectAlphaHelix = selectAlphaHelix;
	this.selectBetaSheet = selectBetaSheet;
	this.scriptPosition = scriptPosition;
	this.selectPositions = selectPositions;
	this.selectSideChains = selectSideChains;
	this.highlightScript = highlightScript;
	this.rainbowColor = rainbowColor;
	this.cpkColor = cpkColor;
	this.enableBallAndStick = enableBallAndStick;
	this.disableBallAndStick = disableBallAndStick;
	this.hideBoundMolecules = hideBoundMolecules;
	this.setTransparency = setTransparency;
}

// JmolScriptGenerator extends MolScriptGenerator...
Mol3DScriptGenerator.prototype = new MolScriptGenerator();
Mol3DScriptGenerator.prototype.constructor = Mol3DScriptGenerator;


/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Utility class to initialize the 3D mutation visualizer with 3Dmol.js
 *
 * Note: This class is assumed to have the same interface
 * (the same signature for all public functions) with the JmolWrapper.
 *
 * @author Selcuk Onur Sumer
 */
function Mol3DWrapper()
{
	// TODO default options
	var defaultOpts = {};

	var _options = null;
	var _viewer = null;

	/**
	 * Initializes the visualizer.
	 *
	 * @param name      name of the application
	 * @param options   app options
	 */
	function init(name, options)
	{
		_options = jQuery.extend(true, {}, defaultOpts, options);

		// update wrapper reference
		$(options.el).append("<div id='" + name + "' " +
			"style='width: " + _options.width + "px; height: " + _options.height +
			"px; margin: 0; padding: 0; border: 0;'></div>");
		var wrapper = $("#" + name);
		wrapper.hide();

		var viewer = $3Dmol.createViewer(wrapper,
			{defaultcolors: $3Dmol.elementColors.rasmol});
		viewer.setBackgroundColor(0xffffff);

		_viewer = viewer;
	}

	/**
	 * Updates the container of the visualizer object.
	 *
	 * @param container container selector
	 */
	function updateContainer(container)
	{
		// move visualizer into its new container
		if (_viewer != null)
		{
			_viewer.setContainer(container);
		}
	}

	/**
	 * Runs the given command as a script on the 3D visualizer object.
	 *
	 * @param command   command to send
	 * @param callback  function to call after execution of the script
	 */
	function script(command, callback)
	{
		if (command != null &&
		    _viewer != null)
		{
			// render after running the script
			_viewer.render();
		}

		// call the callback function after script completed
		if(_.isFunction(callback))
		{
			callback();
		}
	}

	function getViewer()
	{
		return _viewer;
	}

	return {
		init: init,
		updateContainer: updateContainer,
		getViewer: getViewer,
		script: script
	};
}



/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Base (abstract) script generator class for molecular structure visualizers
 * such as Jmol and Pymol.
 *
 * @author Selcuk Onur Sumer
 */
function MolScriptGenerator()
{
	this.loadPdb = function(pdbId) {
		return "";
	};

	this.selectAll = function() {
		return "";
	};

	this.selectNone = function() {
		return "";
	};

	this.setScheme = function(schemeName) {
		return "";
	};

	this.setColor = function(color) {
		return "";
	};

	this.selectChain = function(chainId) {
		return "";
	};

	this.selectAlphaHelix = function(chainId) {
		return "";
	};

	this.selectBetaSheet = function(chainId) {
		return "";
	};

	this.rainbowColor = function(chainId) {
		return "";
	};

	this.cpkColor = function(chainId) {
		return "";
	};

	this.hideBoundMolecules = function() {
		return "";
	};

	this.setTransparency = function(transparency) {
		return "";
	};

	this.scriptPosition = function(position) {
		return "";
	};

	this.selectPositions = function(scriptPositions, chainId) {
		return "";
	};

	this.selectSideChains = function(scriptPositions, chainId) {
		return "";
	};

	this.enableBallAndStick = function() {
		return "";
	};

	this.disableBallAndStick = function() {
		return "";
	};

	this.center = function(position, chainId) {
		return "";
	};

	this.zoom = function(zoomValue) {
		return "";
	};

	this.defaultZoomIn = function() {
		return "";
	};

	this.defaultZoomOut = function() {
		return "";
	};

	this.defaultCenter = function() {
		return "";
	};

	this.spin = function(value) {
		return "";
	};

	/**
	 * Generates highlight script by using the converted highlight positions.
	 *
	 * @param scriptPositions   script positions
	 * @param color             highlight color
	 * @param options           visual style options
	 * @param chain             a PdbChainModel instance
	 * @return {Array} script lines as an array
	 */
	this.highlightScript = function(scriptPositions, color, options, chain)
	{
		return [];
	};

	/**
	 * Generates the visual style (scheme, coloring, selection, etc.) script
	 * to be sent to the 3D app.
	 *
	 * @param selection map of script positions
	 * @param chain     a PdbChainModel instance
	 * @param options   visual style options
	 *
	 * @return {Array}  script lines as an array
	 */
	this.generateVisualStyleScript = function(selection, chain, options)
	{
		var self = this;
		var script = [];

		script.push(self.selectAll()); // select everything
		script.push(self.setScheme(options.proteinScheme)); // show selected style view

		// do the initial (uniform) coloring

		script.push(self.setColor(options.defaultColor)); // set default color
		//script.push("translucent [" + _options.defaultTranslucency + "];"); // set default opacity
		script.push(self.setTransparency(options.defaultTranslucency));
		script.push(self.selectChain(chain.chainId)); // select the chain
		script.push(self.setColor(options.chainColor)); // set chain color
		//script.push("translucent [" + _options.chainTranslucency + "];"); // set chain opacity
		script.push(self.setTransparency(options.chainTranslucency));

		// additional coloring for the selected chain
		script.push(self.selectChain(chain.chainId));

		if (options.colorProteins == "byAtomType")
		{
			script.push(self.cpkColor(chain.chainId));
		}
		else if (options.colorProteins == "bySecondaryStructure")
		{
			// color secondary structure (for the selected chain)
			script.push(self.selectAlphaHelix(chain.chainId)); // select alpha helices
			script.push(self.setColor(options.structureColors.alphaHelix)); // set color
			script.push(self.selectBetaSheet(chain.chainId)); // select beta sheets
			script.push(self.setColor(options.structureColors.betaSheet)); // set color
		}
		else if (options.colorProteins == "byChain")
		{
			// select the chain
			script.push(self.selectChain(chain.chainId));

			// color the chain by rainbow coloring scheme (gradient coloring)
			script.push(self.rainbowColor(chain.chainId));
		}

		// process mapped residues
		_.each(_.keys(selection), function(color) {
			// select positions (mutations)
			script.push(self.selectPositions(selection[color], chain.chainId));

			// color each residue with a mapped color (this is to sync with diagram colors)

			// use the actual mapped color
			if (options.colorMutations == "byMutationType")
			{
				// color with corresponding mutation color
				script.push(self.setColor(color));
			}
			// use a uniform color
			else if (options.colorMutations == "uniform")
			{
				// color with a uniform mutation color
				script.push(self.setColor(options.mutationColor));
			}

			// show/hide side chains
			script = script.concat(
				self.generateSideChainScript(selection[color],
					options.displaySideChain == "all",
					options,
					chain));
		});

		if (options.restrictProtein)
		{
			script.push(self.hideBoundMolecules());
		}

		return script;
	};

	/**
	 * Generates the script to show/hide the side chain for the given positions.
	 * Positions can be in the form of "666" or "666:C", both are fine.
	 *
	 * @param scriptPositions   an array of already generated script positions
	 * @param displaySideChain  flag to indicate to show/hide the side chain
	 * @param options           visual style options
	 * @param chain             a PdbChainModel instance
	 */
	this.generateSideChainScript = function(scriptPositions, displaySideChain, options, chain)
	{
		var self = this;
		var script = [];

		// display side chain (no effect for space-filling)
		if (!(options.proteinScheme == "spaceFilling"))
		{
			// select the corresponding side chain and also the CA atom on the backbone
			script.push(self.selectSideChains(scriptPositions, chain.chainId));

			if (displaySideChain)
			{
				// display the side chain with ball&stick style
				script.push(self.enableBallAndStick());

				// TODO also color side chain wrt atom type (CPK)?
			}
			else
			{
				// hide the side chain
				script.push(self.disableBallAndStick());
			}
		}

		return script;
	};

	/**
	 * Generates the highlight script to be sent to the 3D app.
	 *
	 * @param positions mutation positions to highlight
	 * @param color     highlight color
	 * @param options   visual style options
	 * @param chain     a PdbChainModel instance
	 * @return {Array}  script lines as an array
	 */
	this.generateHighlightScript = function(positions, color, options, chain)
	{
		var self = this;
		var script = [];

		// highlight the selected positions
		if (!_.isEmpty(positions))
		{
			// convert positions to script positions
			var scriptPositions = self.highlightScriptPositions(positions);

			script = script.concat(self.highlightScript(
				scriptPositions, color, options, chain));
		}

		return script;
	};

	this.highlightScriptPositions = function(positions)
	{
		var self = this;
		var scriptPositions = [];

		// convert positions to script positions
		_.each(positions, function(position) {
			scriptPositions.push(self.scriptPosition(position));
		});

		return scriptPositions;
	};
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Utility class for processing collection of mutations.
 *
 * @param mutations     [optional] a MutationCollection instance
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsUtil = function(mutations)
{
	var GERMLINE = "germline"; // germline mutation constant
	var VALID = "valid";

	// init class variables
	var _mutationGeneMap = {};
	var _mutationCaseMap = {};
	var _mutationIdMap = {};
	var _mutationKeywordMap = {};
	var _mutationProteinChangeMap = {};
	var _mutationProteinPosStartMap = {};
	var _mutations = [];

	this.getMutationGeneMap = function()
	{
		return _mutationGeneMap;
	};

	this.getMutationCaseMap = function()
	{
		return _mutationCaseMap;
	};

	this.getMutationIdMap = function()
	{
		return _mutationIdMap;
	};

	this.getMutations = function()
	{
		return _mutations;
	};

	/**
	 * Updates existing maps and collections by processing the given mutations.
	 * This method appends given mutations to the existing ones, it does not
	 * reset previous mutations.
	 *
	 * @param mutations a MutationCollection instance (list of mutations)
	 */
	this.processMutationData = function(mutations)
	{
		// update collections, arrays, maps, etc.
		_mutationGeneMap = this._updateGeneMap(mutations);
		_mutationCaseMap = this._updateCaseMap(mutations);
		_mutationIdMap = this._updateIdMap(mutations);
		_mutationKeywordMap = this._updateKeywordMap(mutations);
		_mutationProteinChangeMap = this._updateProteinChangeMap(mutations);
		_mutationProteinPosStartMap = this._updateProteinPosStartMap(mutations);
		_mutations = _mutations.concat(mutations.models);
	};

	/**
	 * Retrieves protein positions corresponding to the mutations
	 * for the given gene symbol.
	 *
	 * @param gene      hugo gene symbol
	 * @return {Array}  array of protein positions
	 */
	this.getProteinPositions = function(gene)
	{
		var mutations = _mutationGeneMap[gene];

		var positions = [];

		if (mutations != null)
		{
			for(var i=0; i < mutations.length; i++)
			{
				var position = {id: mutations[i].get("mutationId"),
					start: mutations[i].getProteinStartPos(),
					end: mutations[i].get("proteinPosEnd")};

				positions.push(position);
			}
		}

		return positions;
	};

	this.getAllKeywords = function()
	{
		return _.keys(_mutationKeywordMap);
	};

	this.getAllProteinChanges = function()
	{
		return _.keys(_mutationProteinChangeMap);
	};

	this.getAllProteinPosStarts = function()
	{
		return _.keys(_mutationProteinPosStartMap);
	};

	this.getAllGenes = function()
	{
		return _.keys(_mutationGeneMap);
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <geneSymbol, mutation array> pairs.
	 *
	 * @param mutations collection of mutations
	 * @return {object} map of mutations (keyed on gene symbol)
	 * @private
	 */
	this._updateGeneMap = function(mutations)
	{
		var mutationMap = _mutationGeneMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			var gene = mutations.at(i).get("geneSymbol");

			if (gene != null)
			{
				gene = gene.toUpperCase();

				if (mutationMap[gene] == undefined)
				{
					mutationMap[gene] = [];
				}

				mutationMap[gene].push(mutations.at(i));
			}
		}

		return mutationMap;
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <case id, mutation array> pairs.
	 *
	 * @param mutations collection of mutations
	 * @return {object} map of mutations (keyed on case id)
	 * @private
	 */
	this._updateCaseMap = function(mutations)
	{
		var mutationMap = _mutationCaseMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			var caseId = mutations.at(i).get("caseId");

			if (caseId != null)
			{
				caseId = caseId.toLowerCase();

				if (mutationMap[caseId] == undefined)
				{
					mutationMap[caseId] = [];
				}

				mutationMap[caseId].push(mutations.at(i));
			}
		}

		return mutationMap;
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <mutation id, mutation> pairs.
	 *
	 * @param mutations collection of mutations
	 * @return {object} map of mutations (keyed on mutation id)
	 * @private
	 */
	this._updateIdMap = function(mutations)
	{
		var mutationMap = _mutationIdMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			var mutationId = mutations.at(i).get("mutationId");
			mutationMap[mutationId] = mutations.at(i);
		}

		return mutationMap;
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <mutation keyword, mutation array> pairs.
	 *
	 * @param mutations collection of mutations
	 * @return {object} map of mutations (keyed on mutation keyword)
	 * @private
	 */
	this._updateKeywordMap = function(mutations)
	{
		var mutationMap = _mutationKeywordMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			var keyword = mutations.at(i).get("keyword");

			if (keyword != null)
			{
				if (mutationMap[keyword] == undefined)
				{
					mutationMap[keyword] = [];
				}

				mutationMap[keyword].push(mutations.at(i));
			}
		}

		return mutationMap;
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <protein change, mutation array> pairs.
	 *
	 * @param mutations collection of mutations
	 * @returns {object} map of mutations (keyed on protein change)
	 * @private
	 */
	this._updateProteinChangeMap = function(mutations)
	{
		var mutationMap = _mutationProteinChangeMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			var proteinChange = mutations.at(i).get("proteinChange");

			if (proteinChange != null)
			{
				if (mutationMap[proteinChange] == undefined)
				{
					mutationMap[proteinChange] = [];
				}

				mutationMap[proteinChange].push(mutations.at(i));
			}
		}

		return mutationMap;
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <protein position start, mutation array> pairs.
	 *
	 * @param mutations collection of mutations
	 * @returns {object} map of mutations (keyed on protein position start)
	 * @private
	 */
	this._updateProteinPosStartMap = function(mutations)
	{
		var mutationMap = _mutationProteinPosStartMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			// using only protein position start is ambiguous,
			// so we also need gene symbol for the key...
			var gene = mutations.at(i).get("geneSymbol");
			var proteinPosStart = mutations.at(i).get("proteinPosStart");

			if (proteinPosStart != null && gene != null)
			{
				var key = gene + "_" + proteinPosStart;

				if (mutationMap[key] == undefined)
				{
					mutationMap[key] = [];
				}

				mutationMap[key].push(mutations.at(i));
			}
		}

		return mutationMap;
	};

	/**
	 * Generates a single line summary with mutation rate.
	 *
	 * @param mutationCount mutation count values as an object
	 *                      {numCases, numMutated, numSomatic, numGermline}
	 * @return {string}     single line summary string
	 */
	this.generateSummary = function(mutationCount)
	{
		var summary = "[";
		var rate;
        var germlineDenominator = mutationCount.numCases;
                
		if (mutationCount.numGermline > 0)
		{
            if (mutationCount.numGermlineCases !== undefined)
            {
                if (mutationCount.numGermlineCases > 0) {
                    germlineDenominator = mutationCount.numGermlineCases;
                }                        
            }
			rate = (mutationCount.numGermline / germlineDenominator) * 100;
			summary += "Germline Mutation Rate: " + rate.toFixed(1) + "%, ";
		}

		rate = (mutationCount.numSomatic / mutationCount.numCases) * 100;
		summary += "Somatic Mutation Rate: " + rate.toFixed(1) + "%]";

		return summary;
	};

	/**
	 * Counts the number of total cases, number of mutated cases, number of cases
	 * with somatic mutation, and number of cases with germline mutation.
	 *
	 * Returns an object with these values.
	 *
	 * @param gene  hugo gene symbol
	 * @param cases array of cases (strings)
	 * @return {{numCases: number,
	 *          numMutated: number,
	 *          numSomatic: number,
	 *          numGermline: number}}
	 */
	this.countMutations = function(gene, cases)
	{
		var numCases = cases.length;
		var numMutated = 0;
		var numSomatic = 0;
		var numGermline = 0;

		// count mutated cases (also count somatic and germline mutations)
		for (var i=0; i < cases.length; i++)
		{
			// get the mutations for the current case
			var mutations = _mutationCaseMap[cases[i].toLowerCase()];

			// check if case has a mutation
			if (mutations != null)
			{
				var somatic = 0;
				var germline = 0;

				for (var j=0; j < mutations.length; j++)
				{
					// skip mutations with different genes
					if (mutations[j].get("geneSymbol").toLowerCase() != gene.toLowerCase())
					{
						continue;
					}

					if (mutations[j].get("mutationStatus") &&
						mutations[j].get("mutationStatus").toLowerCase() === GERMLINE)
					{
						// case has at least one germline mutation
						germline = 1;
					}
					else
					{
						// case has at least one somatic mutation
						somatic = 1;
					}
				}

				// update counts
				numSomatic += somatic;
				numGermline += germline;
				numMutated++;
			}
		}

		// return an array of calculated values
		return {numCases: numCases,
			numMutated: numMutated,
			numSomatic: numSomatic,
			numGermline: numGermline};
	};

    /**
     * Checks if there all mutations come from a single cancer study
     *
     * @param gene  hugo gene symbol
     */
    this.cancerStudyAllTheSame = function(gene)
    {
        var self = this;
        gene = gene.toUpperCase();
	    var mutations = _mutationGeneMap[gene];

        if (mutations != null)
        {
            var prevStudy = null;

            for (var i=0; i < mutations.length; i++)
            {
                var cancerStudy = mutations[i].get("cancerStudy");
                if(prevStudy == null) {
                    prevStudy = cancerStudy;
                } else if(prevStudy != cancerStudy) {
                    return false;
                }
            }
        }

        return true;
    };

	this._contains = function(gene, matchFn)
	{
		var contains = false;

		gene = gene.toUpperCase();

		var mutations = _mutationGeneMap[gene];

		if (mutations != null)
		{
			for (var i=0; i < mutations.length; i++)
			{
				contains = matchFn(mutations[i]);

				if (contains)
				{
					break;
				}
			}
		}

		return contains;
	};

    /**
	 * Checks if there is a germline mutation for the given gene.
	 *
	 * @param gene  hugo gene symbol
	 */
	this.containsGermline = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("mutationStatus") &&
			        mutation.get("mutationStatus").toLowerCase() == GERMLINE);
		});
	};

	/**
	 * Checks if there is a "valid" validation status for the given gene.
	 *
	 * @param gene  hugo gene symbol
	 */
	this.containsValidStatus = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("validationStatus") &&
			        mutation.get("validationStatus").toLowerCase() == VALID);
		});
	};

	/**
	 * Checks if there is a link to IGV BAM file for the given gene.
	 *
	 * @param gene  hugo gene symbol
	 */
	this.containsIgvLink = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("igvLink") &&
			        mutation.get("igvLink") != "NA");
		});
	};

	/**
	 * Checks if there is valid allele frequency data for the given gene.
	 *
	 * @param gene  hugo gene symbol
	 */
	this.containsAlleleFreqT = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("tumorFreq") &&
			        mutation.get("tumorFreq") != "NA");
		});
	};

	/**
	 * Checks if there is valid copy number data for the given gene.
	 *
	 * @param gene  hugo gene symbol
	 */
	this.containsCnaData = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("cna") &&
			        mutation.get("cna") != "NA" &&
			        mutation.get("cna") != "unknown");
		});
	};

	this.containsProteinChange = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("proteinChange") &&
			        mutation.get("proteinChange") != "NA" &&
			        mutation.get("proteinChange") != "unknown");
		});
	};

	this.containsCaseId = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("caseId") &&
			        mutation.get("caseId") != "NA");
		});
	};

	this.containsChr = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("chr") &&
			        mutation.get("chr") != "NA");
		});
	};

	this.containsStartPos = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("startPos") &&
			        mutation.get("startPos") > 0);
		});
	};

	this.containsRefAllele = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("referenceAllele") &&
			        mutation.get("referenceAllele") != "NA");
		});
	};

	this.containsVarAllele = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("variantAllele") &&
			        mutation.get("variantAllele") != "NA");
		});
	};

	this.containsEndPos = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("endPos") &&
			        mutation.get("endPos") > 0);
		});
	};

	this.containsFis = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("functionalImpactScore") &&
			        mutation.get("functionalImpactScore") != "NA");
		});
	};

	this.containsCosmic = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("cosmic") &&
			        mutation.getCosmicCount() &&
					mutation.getCosmicCount() > 0);
		});
	};

	this.containsMutationType = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("mutationType") &&
			        mutation.get("mutationType") != "NA");
		});
	};

	this.containsMutationCount = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("mutationCount") &&
			        mutation.get("mutationCount") > 0);
		});
	};

	this.containsKeyword = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("keyword") &&
			        mutation.get("keyword") != "NA");
		});
	};

	this.containsMutationEventId = function(gene)
	{
		return this._contains(gene, function(mutation) {
			return (mutation.get("mutationEventId") &&
			        mutation.get("mutationEventId") != "NA");
		});
	};

	/**
	 * Returns the number of distinct cancer type values for
	 * the given gene
	 *
	 * @param gene  hugo gene symbol
	 * @returns {Number}    number of distinct cancer type values
	 */
	this.distinctTumorTypeCount = function(gene)
	{
		gene = gene.toUpperCase();
		var mutations = _mutationGeneMap[gene];
		var tumorTypeMap = {};

		if (mutations != null)
		{
			for (var i=0; i < mutations.length; i++)
			{
				if (mutations[i].get("tumorType"))
				{
					tumorTypeMap[mutations[i].get("tumorType")] = true;
				}
			}
		}

		return _.keys(tumorTypeMap).length;
	};

	/**
	 * Returns a sorted array of data field counts for the given gene.
	 * Does not include counts for the values provided within
	 * the exclude list.
	 *
	 * @param gene          hugo gene symbol
	 * @param dataField     data field name
	 * @param excludeList   data values to exclude while counting
	 * @return {Array}  array of data value count info
	 */
	this.dataFieldCount = function(gene, dataField, excludeList)
	{
		gene = gene.toUpperCase();
		var mutations = _mutationGeneMap[gene];
		var valueCountMap = {};

		if (mutations != null)
		{
			for (var i=0; i < mutations.length; i++)
			{
				var value = mutations[i].get(dataField);

				if (value &&
				    !_.contains(excludeList, value))
				{
					if (valueCountMap[value] === undefined)
					{
						valueCountMap[value] = 0;
					}

					valueCountMap[value]++;
				}
			}
		}

		var pairs = _.pairs(valueCountMap);

		pairs.sort(function(a, b) {
			return (b[1] - a[1]);
		});

		var result = [];

		_.each(pairs, function(pair, i) {
			var obj = {count: pair[1]};
			obj[dataField] = pair[0];
			result.push(obj);
		});

		return result;
	};

	// init maps by processing the initial mutations
	if (mutations != null)
	{
		this.processMutationData(mutations);
	}
};

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Utility class to parse the custom mutation input data.
 *
 * @author Selcuk Onur Sumer
 */
function MutationInputParser ()
{
	var _data = null; // MutationCollection
	var _geneList = null;
	var _sampleList = null;
	var _idCounter = 0;

	// TODO add column name alternatives?
	// map of <mutation model field name, input header name> pairs
	var _headerMap = {
		"proteinPosEnd": "protein_position_end",
		"uniprotId": "uniprot_id",
		"cancerType": "cancer_type",
		"tumorType": "tumor_type",
		"cancerStudyLink": "cancer_study_link",
		"codonChange": "codon_change",
		"proteinPosStart": "protein_position_start",
		"linkToPatientView": "patient_view_link",
		"geneticProfileId": "genetic_profile_id",
		"mutationCount": "mutation_count",
		"mutationType": "mutation_type", // "variant_classification"
		"referenceAllele": "reference_allele",
		"uniprotAcc": "uniprot_accession",
		"fisValue": "fis_value",
		"functionalImpactScore": "fis",
		"cancerStudy": "cancer_study",
		"normalRefCount": "normal_ref_count",
		"ncbiBuildNo": "ncbi_build",
		"normalFreq": "normal_frequency",
		"cancerStudyShort": "cancer_study_short",
		"msaLink": "msa_link",
		"mutationStatus": "mutation_status",
		"cna": "copy_number",
		"proteinChange": "protein_change",
		"aminoAcidChange": "amino_acid_change",
		"endPos": "end_position",
		//"refseqMrnaId": "",
		"geneSymbol": "hugo_symbol",
		"tumorFreq": "tumor_frequency",
		"startPos": "start_position",
		"keyword": "keyword",
		"cosmic": "cosmic",
		"validationStatus": "validation_status",
		"mutationSid": "mutation_sid",
		//"canonicalTranscript": "",
		"normalAltCount": "normal_alt_count",
		"variantAllele": "variant_allele",
		//"mutationEventId": "",
		"mutationId": "mutation_id",
		"caseId": "sample_id", // "tumor_sample_barcode"
		"xVarLink": "xvar_link",
		"pdbLink": "pdb_link",
		"tumorAltCount": "tumor_alt_count",
		"tumorRefCount": "tumor_ref_count",
		"sequencingCenter": "center",
		"chr": "chromosome"
	};

	/**
	 * Initializes a default mutation object where all data fields are empty strings.
	 *
	 * @returns {Object}    a default "empty" mutation object
	 */
	function initMutation()
	{
		return {
			"proteinPosEnd": "",
			"uniprotId": "",
			"cancerType": "",
			"tumorType": "",
			"cancerStudyLink": "",
			"codonChange": "",
			"proteinPosStart": "",
			"linkToPatientView": "",
			"geneticProfileId": "",
			"mutationCount": "",
			"mutationType": "",
			"referenceAllele": "",
			"uniprotAcc": "",
			"fisValue": "",
			"functionalImpactScore": "",
			"cancerStudy": "",
			"normalRefCount": "",
			"ncbiBuildNo": "",
			"normalFreq": "",
			"cancerStudyShort": "",
			"msaLink": "",
			"mutationStatus": "",
			"cna": "",
			"proteinChange": "",
			"aminoAcidChange": "",
			"endPos": "",
			"refseqMrnaId": "",
			"geneSymbol": "",
			"tumorFreq": "",
			"startPos": "",
			"keyword": "",
			"cosmic": "",
			"validationStatus": "",
			"mutationSid": "",
			//"canonicalTranscript": "",
			"normalAltCount": "",
			"variantAllele": "",
			//"mutationEventId": "",
			"mutationId": "",
			"caseId": "",
			"xVarLink": "",
			"pdbLink": "",
			"tumorAltCount": "",
			"tumorRefCount": "",
			"sequencingCenter": "",
			"chr": ""
		};
	}

	/**
	 * Parses the entire input data and creates an array of mutation objects.
	 *
	 * @param input     input string/file.
	 * @returns {MutationCollection} an array of mutation objects.
	 */
	function parseInput(input)
	{
		var mutationData = new MutationCollection();

		var lines = input.split("\n");

		if (lines.length > 0)
		{
			// assuming first line is a header
			// TODO allow comments?
			var indexMap = buildIndexMap(lines[0]);

			// rest should be data
			for (var i=1; i < lines.length; i++)
			{
				// skip empty lines
				if (lines[i].length > 0)
				{
					mutationData.push(parseLine(lines[i], indexMap));
				}
			}
		}

		_data = mutationData;

		return mutationData;
	}

	/**
	 * Parses a single line of the input and returns a new mutation object.
	 *
	 * @param line      single line of the input data
	 * @param indexMap  map of <header name, index> pairs
	 * @returns {MutationModel}    a mutation model object
	 */
	function parseLine(line, indexMap)
	{
		//var mutation = initMutation();
		// init an empty mutation object
		var mutation = new MutationModel();

		// assuming values are separated by tabs
		var values = line.split("\t");
		var attributes = {};

		// find the corresponding column for each field, and set the value
		_.each(_.keys(_headerMap), function(key) {
			var value = parseValue(key, values, indexMap);

			if (value)
			{
				attributes[key] = value;
			}
		});

		attributes.mutationId = attributes.mutationId || nextId();

		// TODO mutationSid?
		attributes.mutationSid = attributes.mutationSid || attributes.mutationId;

		attributes.variantKey = VariantAnnotationUtil.generateVariantKey(attributes);

		mutation.set(attributes);
		return mutation;
	}

	/**
	 * Parses the value of a single input cell.
	 *
	 * @param field     name of the mutation model field
	 * @param values    array of values for a single input line
	 * @param indexMap  map of <header name, index> pairs
	 * @returns {string|undefined}    data value for the given field name.
	 */
	function parseValue(field, values, indexMap)
	{
		// get the column name for the given field name
		var column = _headerMap[field];
		var index = indexMap[column];
		var value = undefined;

		if (index != null &&
		    values[index] != null)
		{
			value = values[index].trim();
		}

		return value;
	}

	/**
	 * Builds a map of <header name, index> pairs, to use header names
	 * instead of index constants.
	 *
	 * @param header    header line (first line) of the input
	 * @returns {object} map of <header name, index> pairs
	 */
	function buildIndexMap(header)
	{
		var columns = header.split("\t");
		var map = {};

		_.each(columns, function(column, index) {
			map[column.trim().toLowerCase()] = index;
		});

		return map;
	}

	/**
	 * Processes the input data and creates a list of sample (case) ids.
	 *
	 * @returns {Array} an array of sample ids
	 */
	function getSampleArray()
	{
		if (_data == null)
		{
			return [];
		}

		if (_sampleList == null)
		{
			var sampleSet = {};

			_data.each(function(mutation, idx) {
				if (mutation.get("caseId") != null &&
				    mutation.get("caseId").length > 0)
				{
					sampleSet[mutation.get("caseId")] = mutation.get("caseId");
				}
			});

			_sampleList = _.values(sampleSet);
		}

		return _sampleList;
	}

	function getGeneList()
	{
		if (_data == null)
		{
			return [];
		}

		if (_geneList == null)
		{
			var geneSet = {};

			_data.each(function(mutation, idx) {
				if (mutation.get("geneSymbol") != null &&
				    mutation.get("geneSymbol").length > 0)
				{
					geneSet[mutation.get("geneSymbol").toUpperCase()] =
						mutation.get("geneSymbol").toUpperCase();
				}
			});

			_geneList = _.values(geneSet);
		}

		return _geneList;
	}

	function nextId()
	{
	    _idCounter++;

		return "stalone_mut_" + _idCounter;
	}

	return {
		parseInput: parseInput,
		getSampleArray: getSampleArray,
		getGeneList: getGeneList
	};
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility function for Pancancer Mutation Data.
 *
 * @author Selcuk Onur Sumer
 */
var PancanMutationDataUtil = (function()
{
	function munge(response, key)
	{
		// munge data to get it into the format: keyword -> corresponding datum
		return d3.nest()
			.key(function(d) {
				return d[key];
			})
			.entries(response)
			.reduce(function(acc, next) {
				acc[next.key] = next.values;
				return acc;
			},
			{});
	}

	function getMutationFrequencies(data)
	{
		var frequencies = {};

		_.each(_.keys(data), function(key, i) {
			frequencies = _.extend(frequencies, munge(data[key], key));
		});

		return frequencies;
	}

	/**
	 * Counts number of total mutations for the given frequencies and key.
	 *
	 * @param frequencies   pancan mutation frequencies
	 * @param key           key (keyword, gene symbol or protein change)
	 * @returns {Object}    mutation count
	 */
	function countByKey(frequencies, key)
	{
		var data = frequencies[key];

		return _.reduce(data, function(acc, next) {
			return acc + next.count;
		}, 0);
	}

	return {
		getMutationFrequencies: getMutationFrequencies,
		countByKey: countByKey
	};
})();

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility class for PDB data related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var PdbDataUtil = (function()
{
	// constants
	var ALIGNMENT_GAP = "*";
	var ALIGNMENT_PLUS = "+";
	var ALIGNMENT_MINUS = "-";
	var ALIGNMENT_SPACE = " ";

	/**
	 * Processes the pdb data (received from the server) to create
	 * a collection of PdbModel instances.
	 *
	 * @param data  pdb alignment data with a position map
	 * @return {PdbCollection}   PdbModel instances representing the processed data
	 */
	function processPdbData(data)
	{
		// ascending sort
		// TODO do not sort if already sorted?
		data.sort(function(a, b) {
			var diff = a.uniprotFrom - b.uniprotFrom;

			// for consistency sort alphabetically if positions are same
			if (diff === 0)
			{
				if (a.pdbId > b.pdbId)
				{
					diff = -1;
				}
				else
				{
					diff = 1;
				}
			}

			return diff;
		});

		var alignmentModel = null;
		var pdbList = [];
		var pdbMap = {};

		_.each(data, function(alignment, idx) {
			alignmentModel = new PdbAlignmentModel(alignment);

			if (pdbMap[alignmentModel.pdbId] == undefined)
			{
				pdbMap[alignmentModel.pdbId] = {};
			}

			if (pdbMap[alignmentModel.pdbId][alignmentModel.chain] == undefined)
			{
				pdbMap[alignmentModel.pdbId][alignmentModel.chain] = [];
			}

			pdbMap[alignmentModel.pdbId][alignmentModel.chain].push(alignmentModel);
		});

		// instantiate chain models
		_.each(_.keys(pdbMap), function(pdbId) {
			var chains = [];

			_.each(_.keys(pdbMap[pdbId]), function(chain) {
				var chainModel = new PdbChainModel({chainId: chain,
					alignments: pdbMap[pdbId][chain]});

				chains.push(chainModel);
			});

			var pdbModel = new PdbModel({pdbId: pdbId,
				chains: chains});

			pdbList.push(pdbModel);
		});

		// return new pdb model
		return new PdbCollection(pdbList);
	}

	function alignmentString(attributes)
	{
		var sb = [];

		// process 3 alignment strings and create a visualization string
		var midline = attributes.midlineAlign;
		var uniprot = attributes.uniprotAlign;
		var pdb = attributes.pdbAlign;

		if (midline.length === uniprot.length &&
		    midline.length === pdb.length)
		{
			for (var i = 0; i < midline.length; i++)
			{
				// do not append anything if there is a gap in uniprot alignment
				if (uniprot[i] !== '-')
				{
					if (pdb[i] === '-')
					{
						sb.push('-');
					}
					else
					{
						sb.push(midline[i]);
					}
				}
			}
		}
		else
		{
			// the execution should never reach here,
			// if everything is OK with the data...
			sb.push("NA");
		}

		return sb.join("");
	}

	/**
	 * Generates a pdb info summary for the given full pdb info object
	 * and the chain id.
	 *
	 * @param pdbInfo   pdb info data (retrieved from server)
	 * @param chainId   chain id as a string
	 * @returns {Object} pdb summary for the given chain
	 */
	function generatePdbInfoSummary(pdbInfo, chainId)
	{
		var summary = {};
		summary.title = pdbInfo.title;

		// TODO cache?

		// get chain specific molecule info
		_.find(pdbInfo.compound, function(mol) {
			if (mol.molecule &&
			    _.indexOf(mol.chain, chainId.toLowerCase()) != -1)
			{
				// chain is associated with this mol,
				// get the organism info from the source
				summary.molecule = mol.molecule;
				return mol;
			}
		});

		return summary;
	}

	/**
	 * Finds the organism for the given full pdb info object
	 * and the chain id.
	 *
	 * @param pdbInfo   pdb info data (retrieved from server)
	 * @param chainId   chain id as a string
	 * @returns {String} organism data corresponding to the given chain
	 */
	function getOrganism(pdbInfo, chainId)
	{
		var organism = "NA";

		// TODO cache?
		_.find(pdbInfo.compound, function(mol) {
			if (_.indexOf(mol.chain, chainId.toLowerCase()) != -1 &&
			    pdbInfo.source[mol.mol_id] != null)
			{
				// chain is associated with this mol,
				// get the organism info from the source
				organism = pdbInfo.source[mol.mol_id].organism_scientific ||
				           organism;
				return mol;
			}
		});

		return organism;
	}

	/**
	 * Merge alignments in the given array.
	 *
	 * @param alignments    an array of PdbAlignmentModel instances
	 */
	function mergeAlignments(alignments)
	{
		// TODO merge without assuming it is sorted (write a new algorithm)
		return mergeSortedAlignments(alignments);
	}

	/**
	 * Merge alignments in the given array, assuming that
	 * they are sorted by uniprotFrom field.
	 *
	 * @param alignments    an array of PdbAlignmentModel instances
	 * @return {Object}     merged alignment object
	 */
	function mergeSortedAlignments(alignments)
	{
		var mergedAlignment = {mergedString: "", uniprotFrom: -1, uniprotTo: -1, pdbFrom: -1};
		var mergedStr = "";
		var end = -1;
		var prev;

		if (alignments.length > 0)
		{
			mergedStr += alignments[0].alignmentString;
			end = alignments[0].uniprotTo;
			prev = alignments[0];
		}
		else
		{
			return mergedAlignment;
		}

		_.each(alignments, function(alignment, idx) {
			var distance = alignment.uniprotFrom - end - 1;

			var str = alignment.alignmentString;

			// check for overlapping uniprot positions...

			// no overlap, and the next alignment starts exactly after the current merge
			if (distance == 0)
			{
				// just concatenate two strings
				mergedStr += str;
			}
			// no overlap, but there is a gap
			else if (distance > 0)
			{
				var gap = [];

				// add gap characters (character count = distance)
				for (var i=0; i<distance; i++)
				{
					gap.push(ALIGNMENT_GAP);
				}

				// also add the actual string
				gap.push(str);

				mergedStr += gap.join("");

			}
			// overlapping
			else
			{
				var overlap = [];
				var subLength = Math.min(-1 * distance, str.length);

				overlap.push(mergedStr.substr(mergedStr.length + distance, subLength));
				overlap.push(str.substr(0, subLength));

				if (overlap[0] != overlap[1])
				{
					console.log("[warning] alignment mismatch: " +
					            prev.alignmentId + " & " + alignment.alignmentId);
					console.log(overlap[0]);
					console.log(overlap[1]);
				}

				// merge two strings
				mergedStr += str.substr(-1 * distance);
			}

			// update the end position
			end = Math.max(end, alignment.uniprotTo);

			if (end == alignment.uniprotTo)
			{
				// keep reference to the previous alignment
				prev = alignment;
			}
		});

		mergedAlignment.uniprotFrom = alignments[0].uniprotFrom;
		mergedAlignment.uniprotTo = mergedAlignment.uniprotFrom + mergedStr.length;
		mergedAlignment.pdbFrom = alignments[0].pdbFrom;
		mergedAlignment.mergedString = mergedStr;
		mergedAlignment.identityPerc = calcIdentityPerc(mergedStr);
		mergedAlignment.identity = calcIdentity(mergedStr);

		return mergedAlignment;
	}

	/**
	 * Finds the first matching pdb id & chain for the given mutation and
	 * row of chains.
	 *
	 * @param mutation  a MutationModel instance
	 * @param rowData   ranked chain data (2D array)
	 * @return {Object} {pdbId, chainId}
	 */
	function mutationToPdb(mutation, rowData)
	{
		var pdbMatch = null;

		var location = mutation.getProteinStartPos();
		var type = mutation.get("mutationType") || "";
		type = type.trim().toLowerCase();

		// skip fusions or invalid locations
		if (location == null ||
		    type === "fusion")
		{
			return pdbMatch;
		}

		// iterate all chains to find the first matching position
		for (var i=0;
		     i < rowData.length && !pdbMatch;
		     i++)
		{
			var allocation = rowData[i];

			for (var j=0;
			     j < allocation.length && !pdbMatch;
			     j++)
			{
				var datum = allocation[j];

				var alignment = datum.chain.mergedAlignment;

				// use merged alignment to see if there is a match
				var rangeWithin = location >= alignment.uniprotFrom &&
				                  location <= alignment.uniprotTo;

				// check for match condition
				if (rangeWithin && alignmentMatch(alignment, location))
				{
					pdbMatch = {pdbId: datum.pdbId,
						chainId: datum.chain.chainId};

					// found a matching pdb residue, break the inner loop
					break;
				}
			}

			if (pdbMatch)
			{
				// found a matching pdb residue, break the outer loop
				break;
			}
		}

		return pdbMatch;
	}

	/**
	 * Processes mutation data to add pdb match data
	 *
	 * @param mutationData  array of MutationModel instances
	 * @param pdbRowData    pdb row data for the corresponding uniprot id
	 * @return {Array}      mutation data array with additional attrs
	 */
	function addPdbMatchData(mutationData, pdbRowData)
	{
		if (!pdbRowData)
		{
			return mutationData;
		}

		//var map = mutationUtil.getMutationIdMap();

		_.each(mutationData, function(mutation, idx) {
			if (mutation == null)
			{
				console.log('warning [processMutationData]: mutation (at index %d) is null.', idx);
				return;
			}

			// find the matching pdb
			var match = PdbDataUtil.mutationToPdb(mutation, pdbRowData);
			// update the raw mutation object
			mutation.set({pdbMatch: match});
		});

		return mutationData;
	}

	/**
	 * Checks for a match for the specified location on the
	 * given merged alignment.
	 *
	 * @param alignment merged alignment
	 * @param location  protein change location
	 * @return {boolean}    true if match, false otherwise
	 */
	function alignmentMatch(alignment, location)
	{
		var index = location - alignment.uniprotFrom;

		var symbol = alignment.mergedString[index];

		var mismatch = (symbol == ALIGNMENT_GAP);

		return !mismatch;
	}

	/**
	 * Calculates the identity percentage of the given alignment string
	 * based on mismatch ratio.
	 *
	 * @param mergedStr merged alignment string
	 * @return {Number} identity percentage value
	 */
	function calcIdentityPerc(mergedStr)
	{
		var gap = 0;
		var mismatch = 0;

		for (var count=0; count < mergedStr.length; count++)
		{
			var symbol = mergedStr[count];
			if (symbol == ALIGNMENT_GAP)
			{
				// increment gap count (gaps excluded from ratio calculation)
				gap++;
			}
			else if (symbol == ALIGNMENT_MINUS ||
				symbol == ALIGNMENT_PLUS ||
				symbol == ALIGNMENT_SPACE)
			{
				// any special symbol other than a gap is considered as a mismatch
				// TODO is it better to assign a different weight for each symbol?
				mismatch++;
			}
		}

		return 1.0 - (mismatch / (count - gap));
	}

	/**
	 * Calculates the identity (number of matches) for
	 * the given alignment string.
	 *
	 * @param mergedStr merged alignment string
	 * @return {Number} identity value
	 */
	function calcIdentity(mergedStr)
	{
		mergedStr = mergedStr.toLowerCase();

		var match = 0;

		for (var count=0; count < mergedStr.length; count++)
		{
			var symbol = mergedStr[count];

			if (symbol.match(/[a-z]/))
			{
				match++;
			}
		}

		return match;
	}

	/**
	 * Creates row data by allocating position for each chain.
	 * A row may have multiple chains if there is no overlap
	 * between chains.
	 *
	 * @param pdbColl   a PdbCollection instance
	 * @return {Array}  a 2D array of chain allocation
	 */
	function allocateChainRows(pdbColl)
	{
		// sort chains by rank (high to low)
		var chainData = sortChainsDesc(pdbColl);

		var rows = [];

		_.each(chainData, function(datum, idx) {
			var chain = datum.chain;

			if (chain.alignments.length > 0)
			{
				var inserted = false;

				// find the first available row for this chain
				for (var i=0; i < rows.length; i++)
				{
					var row = rows[i];
					var conflict = false;

					// check for conflict for this row
					for (var j=0; j < row.length; j++)
					{
						if (overlaps(chain, row[j].chain))
						{
							// set the flag, and break the loop
							conflict = true;
							break;
						}
					}

					// if there is space available in this row,
					// insert the chain into the current row
					if (!conflict)
					{
						// insert the chain, set the flag, and break the loop
						row.push(datum);
						inserted = true;
						break;
					}
				}

				// if there is no available space in any row,
				// then insert the chain to the next row
				if (!inserted)
				{
					var newAllocation = [];
					newAllocation.push(datum);
					rows.push(newAllocation);
				}
			}
		});

		// sort alignments in each row by start position (lowest comes first)
//		_.each(rows, function(allocation, idx) {
//			allocation.sort(function(a, b){
//				return (a.chain.mergedAlignment.uniprotFrom -
//				        b.chain.mergedAlignment.uniprotFrom);
//			});
//		});

		// sort alignments in the first row by alignment length
		if (rows.length > 0)
		{
			rows[0].sort(function(a, b){
				return (b.chain.mergedAlignment.mergedString.length -
				        a.chain.mergedAlignment.mergedString.length);
			});
		}

		return rows;
	}

	/**
	 * Checks if the given two chain alignments (positions) overlaps
	 * with each other.
	 *
	 * @param chain1    first chain
	 * @param chain2    second chain
	 * @return {boolean}    true if intersects, false if distinct
	 */
	function overlaps(chain1, chain2)
	{
		var overlap = true;

		if (chain1.mergedAlignment.uniprotFrom >= chain2.mergedAlignment.uniprotTo ||
		    chain2.mergedAlignment.uniprotFrom >= chain1.mergedAlignment.uniprotTo)
		{
			// no conflict
			overlap = false;
		}

		return overlap;
	}

	/**
	 * Creates a sorted array of chain datum (a {pdbId, PdbChainModel} pair).
	 * The highest ranked chain will be the first element of the returned
	 * data array.
	 *
	 * @param pdbColl   a PdbCollection instance
	 * @return {Array}  an array of <pdb id, PdbChainModel> pairs
	 */
	function sortChainsDesc(pdbColl)
	{
		var chains = [];

		// put all chains in a single array
		pdbColl.each(function(pdb, idx) {
			// create rectangle(s) for each chain
			pdb.chains.each(function(chain, idx) {
				var datum = {pdbId: pdb.pdbId, chain: chain};
				chains.push(datum);
			});
		});

		// rank the chains
		sortChains(chains, [
			compareIdentity, // first, sort by identity
			compareMergedLength, // then by length
			compareIdentityPerc, // then by identity percentage
			comparePdbId, // then by pdb id (A-Z)
			compareChainId // then by chain id (A-Z)
		]);

		return chains;
	}

	/**
	 * Sort chains wrt the given comparator functions.
	 *
	 * @param chains        an array of PDB chain data
	 * @param comparators   an array of comparator functions
	 */
	function sortChains(chains, comparators)
	{
		// compare using given comparator functions
		chains.sort(function(a, b) {
			var result = 0;

			// continue to compare until the result is different than zero
			for (var i=0;
			     i < comparators.length && result == 0;
			     i++)
			{
				var fn = comparators[i];
				result = fn(a, b);
			}

			return result;
		});
	}

	function chainKey(pdbId, chainId)
	{
		return pdbId + ":" + chainId;
	}

	function compareIdentity(a, b)
	{
		// higher value should comes first
		return (b.chain.mergedAlignment.identity -
		        a.chain.mergedAlignment.identity);
	}

	function compareIdentityPerc(a, b)
	{
		// higher value should comes first
		return (b.chain.mergedAlignment.identityPerc -
		        a.chain.mergedAlignment.identityPerc);
	}

	function compareMergedLength(a, b)
	{
		// longer string should comes first in the sorted array
		return (b.chain.mergedAlignment.mergedString.length -
		        a.chain.mergedAlignment.mergedString.length);
	}

	function comparePdbId(a, b)
	{
		// A-Z sort
		if (b.pdbId > a.pdbId) {
			return -1;
		} else if (b.pdbId < a.pdbId) {
			return 1;
		} else {
			return 0;
		}

		//return (a.pdbId - b.pdbId);
	}

	function compareChainId(a, b)
	{
		// A-Z sort
		if (b.chain.chainId > a.chain.chainId) {
			return -1;
		} else if (b.chain.chainId < a.chain.chainId) {
			return 1;
		} else {
			return 0;
		}

		//return (a.chain.chainId - b.chain.chainId);
	}

	function compareEValue(a, b)
	{
		// lower e value should comes first in the sorted array
		return (getMinValue(a.chain.alignments, "eValue") -
		        getMinValue(b.chain.alignments, "eValue"));
	}

	function compareIdentP(a, b)
	{
		// higher percentage should comes first in the sorted array
		return (getMinValue(b.chain.alignments, "identityPerc") -
		        getMinValue(a.chain.alignments, "identityPerc"));
	}

	/**
	 * Calculates total number of chains for the given PDB data.
	 *
	 * @param data      PDB data (collection of PdbModel instances)
	 * @return {number} total number of chains
	 */
	function calcChainCount(data)
	{
		var chainCount = 0;

		data.each(function(pdb, idx) {
			chainCount += pdb.chains.length;
		});

		return chainCount;
	}

	function getMinValue(alignments, field)
	{
		var min = Infinity;

		alignments.each(function(ele, idx) {
			if (ele[field] < min)
			{
				min = ele[field];
			}
		});

		return min;
	}

	function getMaxValue(alignments, field)
	{
		var max = -Infinity;

		alignments.each(function(ele, idx) {
			if (ele[field] > max)
			{
				max = ele[field];
			}
		});

		return max;
	}

	return {
		// public constants
		ALIGNMENT_GAP: ALIGNMENT_GAP,
		ALIGNMENT_PLUS: ALIGNMENT_PLUS,
		ALIGNMENT_MINUS: ALIGNMENT_MINUS,
		ALIGNMENT_SPACE: ALIGNMENT_SPACE,
		// public functions
		alignmentString: alignmentString,
		processPdbData: processPdbData,
		mutationToPdb: mutationToPdb,
		addPdbMatchData: addPdbMatchData,
		allocateChainRows: allocateChainRows,
		mergeAlignments: mergeAlignments,
		generatePdbInfoSummary: generatePdbInfoSummary,
		getOrganism: getOrganism,
		chainKey: chainKey
	};
})();
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * PymolScriptGenerator class (extends JmolScriptGenerator)
 *
 * Script generator for the PyMOL application.
 *
 * @author Selcuk Onur Sumer
 */
function PymolScriptGenerator()
{
	// Predefined style scripts for Jmol
	var _styleScripts = {
		ballAndStick: "hide everything; show spheres; show sticks; alter all, vdw=0.50",
		spaceFilling: "hide everything; show spheres;",
		ribbon: "hide everything; show ribbon;",
		cartoon: "hide everything; show cartoon;",
		// TODO there is no "trace" in PyMOL, ribbon is the most similar one
		trace: "hide everything; show ribbon;"
	};

	function reinitialize()
	{
		return "reinitialize;";
	}

	function bgColor(color)
	{
		return "bg_color " + formatColor(color) + ";";
	}

	function loadPdb(pdbId)
	{
		return "fetch " + pdbId + ", async=0;";
	}

	function setScheme(schemeName)
	{
		return _styleScripts[schemeName];
	}

	function setColor (color)
	{
		return "color " + formatColor(color) + ", sele;";
	}

	function selectChain(chainId)
	{
		return "select chain " + chainId + ";";
	}

	function selectAlphaHelix(chainId)
	{
		return "select (chain " + chainId + ") and (ss h);";
	}

	function selectBetaSheet(chainId)
	{
		return "select (chain " + chainId + ") and (ss s);";
	}

	function selectPositions(scriptPositions, chainId)
	{
		return "select (resi " + scriptPositions.join(",") + ") and (chain " + chainId + ");";
	}

	function selectSideChains(scriptPositions, chainId)
	{
		return "select ((resi " + scriptPositions.join(",") + ") and (chain " + chainId + ") and (not name c+n+o));";
	}

	function setTransparency(transparency)
	{
		// TODO cartoon_transparency doesn't work for chain or residue selections
		// see issue:  http://sourceforge.net/p/pymol/bugs/129/
		return ("set transparency, " + (transparency / 10) + ", sele;\n" +
		        "set cartoon_transparency, " + (transparency / 10) + ", sele;\n" +
		        "set sphere_transparency, " + (transparency / 10) + ", sele;\n" +
		        "set stick_transparency, " + (transparency / 10) + ", sele;");
	}

	function enableBallAndStick()
	{
		return "show spheres, sele; show sticks, sele; alter sele, vdw=0.50;";
	}

	function disableBallAndStick()
	{
		return "hide spheres, sele; hide sticks, sele;";
	}

	function rainbowColor(chainId)
	{
		return "spectrum count, rainbow_rev, sele";
	}

	function cpkColor(chainId)
	{
		return "util.cbaw sele;";
	}

	function hideBoundMolecules()
	{
		// restrict to protein only
		return "hide everything," +
		       "not resn asp+glu+arg+lys+his+asn+thr+cys+gln+tyr+ser+gly+ala+leu+val+ile+met+trp+phe+pro";
	}

	function formatColor(color)
	{
		// this is for Pymol compatibility
		// (colors should start with an "0x" instead of "#")
		return color.replace("#", "0x");
	}

	// override required functions
	this.loadPdb = loadPdb;
	this.setScheme = setScheme;
	this.setColor = setColor;
	this.selectChain = selectChain;
	this.selectAlphaHelix = selectAlphaHelix;
	this.selectBetaSheet = selectBetaSheet;
	this.rainbowColor = rainbowColor;
	this.cpkColor = cpkColor;
	this.hideBoundMolecules = hideBoundMolecules;
	this.setTransparency = setTransparency;
	this.selectPositions = selectPositions;
	this.selectSideChains = selectSideChains;
	this.enableBallAndStick = enableBallAndStick;
	this.disableBallAndStick = disableBallAndStick;
	this.reinitialize = reinitialize;
	this.bgColor = bgColor;
}

// PymolScriptGenerator extends JmolScriptGenerator...
PymolScriptGenerator.prototype = new JmolScriptGenerator();
PymolScriptGenerator.prototype.constructor = PymolScriptGenerator;


/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A simple queue implementation for serializing requests.
 *
 * @author Selcuk Onur Sumer
 */
function RequestQueue(options)
{
	var self = this;

	var _defaultOpts = {
		completeEvent: "requestQueueProcessComplete",
		newRequestEvent: "requestQueueNewRequest"
	};

	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	var _queryQueue = [];
	var _queryInProgress = false;
	var _dispatcher = {};
	_.extend(_dispatcher, Backbone.Events);

	/**
	 * Initializes the queue with the provided process function.
	 *
	 * @param processFn function to be invoked to process queue elements
	 */
	function init(processFn)
	{
		_dispatcher.on(_options.newRequestEvent, function() {
			// no query in progress, ready to consume
			if (!_queryInProgress)
			{
				processQueue(processFn);
			}
		});

		_dispatcher.on(_options.completeEvent, function() {
			processQueue(processFn);
		});
	}

	// TODO find an efficient way to avoid hitting the server more than once
	// for the exact same simultaneous query

	/**
	 * Processes the queue by invoking the given process function
	 * for the current element in the queue.
	 *
	 * @param processFn function to process the queue element
	 */
	function processQueue(processFn)
	{
		// get the first element from the queue
		var element = _.first(_queryQueue);
		_queryQueue = _.rest(_queryQueue);

		// still elements in queue
		if (element)
		{
			_queryInProgress = element;

			if (_.isFunction(processFn))
			{
				processFn(element);
			}
		}
		// no more query to process
		else
		{
			_queryInProgress = false;
		}
	}

	/**
	 * Function to be invoked upon completion of the process of a queue element.
	 */
	function complete()
	{
		_queryInProgress = false;
		_dispatcher.trigger(_options.completeEvent);
	}

	/**
	 * Adds a new element into the queue, and triggers a new request event.
	 *
	 * @param element   a new queue element
	 */
	function add(element)
	{
		_queryQueue.push(element);
		_dispatcher.trigger(_options.newRequestEvent);
	}

	self.add = add;
	self.complete = complete;
	self.init = init;
}

/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility class for variant annotation related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var VariantAnnotationUtil = (function()
{
	function addAnnotationData(mutations, annotationData, parseFn)
	{
		var indexedData = _.indexBy(annotationData, "variant");

		if (!_.isFunction(parseFn))
		{
			parseFn = defaultParseAnnotationData;
		}

		_.each(mutations, function(mutation, idx) {
			var annotation = indexedData[mutation.get("variantKey")];
			var parsed = null;

			// check if annotation has an id field
			if (annotation && annotation.id)
			{
				parsed = parseFn(annotation);
			}
			// if no id field, then try the annotationJSON field
			else if (annotation && annotation.annotationJSON)
			{
				parsed = parseFn(annotation.annotationJSON);
			}

			if (parsed)
			{
				// only update undefined fields!
				setUndefinedFields(mutation, parsed);
			}
		});
	}

	/**
	 * Updates only the undefined fields of the given mutation.
	 *
	 * @param mutation  a MutationModel instance
	 * @param annotation    annotation data for single variant
	 */
	function setUndefinedFields(mutation, annotation)
	{
		var update = {};

		_.each(_.keys(annotation), function(fieldName) {
			if (_.isUndefined(mutation.get(fieldName)))
			{
				update[fieldName] = annotation[fieldName];
			}
		});

		if (!_.isEmpty(update))
		{
			mutation.set(update);
		}
	}

	/**
	 * Default parse function that retrieves the partial data from
	 * the raw annotation data.
	 *
	 * @param annotation    raw annotation data (from VEP)
	 * @returns {object} parsed annotation data
	 */
	function defaultParseAnnotationData(annotation)
	{
		var vepData = VepParser.parseJSON(annotation);
		var canonical = vepData.canonicalTranscript;

		// in case of empty annotation data (possible error),
		// corresponding data fields will be empty string

		// TODO define a proper VariantAnnotation model instead?
		var empty = {
			startPos: "",
			endPos: "",
			chr: "",
			referenceAllele: "",
			variantAllele: "",
			proteinChange: ""
		};

		// remove unused fields
		delete(vepData.rawData);
		delete(vepData.transcripts);
		delete(vepData.refseqIds);
		delete(vepData.canonicalTranscript);

		// copy canonical data properties
		return _.extend(empty, vepData, canonical);
	}

	/**
	 * Generates variant key for annotation queries.
	 * This function assumes that basic mutation data (chromosome number,
	 * start position, reference allele, variant allele) is available
	 * for the provided mutation. If not, returns undefined.
	 *
	 * Example keys: 10:g.152595854G>A
	 *               17:g.36002278_36002277insA
	 *               1:g.206811015_206811016delAC
	 *
	 * @param mutation mutation attributes or a MutationModel instance
	 * @returns {string|undefined} variant key (to be used for annotation query)
	 */
	function generateVariantKey(mutation)
	{
		var key = undefined;

		var chr = mutation.chr;
		var startPos = mutation.startPos;
		var endPos = mutation.endPos;
		var referenceAllele = mutation.referenceAllele;
		var variantAllele = mutation.variantAllele;

		// if mutation has a get function, assume that it is a MutationModel instance
		if (_.isFunction(mutation.get))
		{
			chr = mutation.get("chr");
			startPos = mutation.get("startPos");
			endPos = mutation.get("endPos");
			referenceAllele = mutation.get("referenceAllele");
			variantAllele = mutation.get("variantAllele");
		}

		if (referenceAllele != null &&
		    referenceAllele === variantAllele)
		{
			console.log("[VariantAnnotationUtil.generateVariantKey] " +
			            "Warning: Reference allele (" + referenceAllele + ") for " +
			            chr + ":" + startPos + "-" + endPos + " is the same as variant allele");
		}

		function adjustPosition()
		{
			var start = parseInt(startPos);
			var end = parseInt(endPos);

			if (_.isNaN(start) && _.isNaN(end))
			{
				// start or end position is not a number,
				// cannot process further
				return;
			}

			// remove common prefix and adjust variant position accordingly

			var prefix = cbio.util.lcss(referenceAllele, variantAllele);

			if (prefix.length > 0)
			{
				referenceAllele = referenceAllele.substring(prefix.length);
				variantAllele = variantAllele.substring(prefix.length);

				start += prefix.length;
				// TODO end position may already be correct
				// (no need to update in that case)
				end += prefix.length;

				startPos = start.toString();
				endPos = end.toString();
			}
		}

		if (chr && startPos && referenceAllele && variantAllele)
		{
			adjustPosition();

			// this is what we will end up with if there is no endPos is provided
			// example SNP: 2 216809708 216809708 C T
			// example key: 2:g.216809708C>T
			key = chr + ":g." + startPos + referenceAllele + ">" + variantAllele;

			if (endPos)
			{
				// example insertion: 17 36002277 36002278 - A
				// example key:       17:g.36002278_36002277insA
				if (referenceAllele === "-" ||
				    referenceAllele.length === 0)
				{
					key = chr+ ":g." + endPos + "_" + startPos + "ins" + variantAllele;
				}
				// Example deletion: 1 206811015 206811016  AC -
				// Example key:      1:g.206811015_206811016delAC
				else if(variantAllele === "-" ||
				        variantAllele.length === 0)
				{
					key = chr + ":g." + startPos + "_" + endPos + "del" + referenceAllele;
				}
			}
		}

		return key;
	}

	return {
		generateVariantKey: generateVariantKey,
		addAnnotationData: addAnnotationData
	};
})();

/**
 * Parses JSON Retrieved from VEP web service.
 *
 * @author Selcuk Onur Sumer
 */
var VepParser = (function()
{
	var _aa3to1 = {
		"Ala": "A",
		"Arg": "R",
		"Asn": "N",
		"Asp": "D",
		"Asx": "B",
		"Cys": "C",
		"Glu": "E",
		"Gln": "Q",
		"Glx": "Z",
		"Gly": "G",
		"His": "H",
		"Ile": "I",
		"Leu": "L",
		"Lys": "K",
		"Met": "M",
		"Phe": "F",
		"Pro": "P",
		"Ser": "S",
		"Thr": "T",
		"Trp": "W",
		"Tyr": "Y",
		"Val": "V",
		"Xxx": "X",
		"Ter": "*"
	};

	var _variantMap = {
		"splice_acceptor_variant": "Splice_Site",
		"splice_donor_variant": "Splice_Site",
		"transcript_ablation": "Splice_Site",
		"stop_gained": "Nonsense_Mutation",
		"frameshift_variant": "Frame_Shift",
		"stop_lost": "Nonstop_Mutation",
		"initiator_codon_variant": "Translation_Start_Site",
		"start_lost": "Translation_Start_Site",
		"inframe_insertion": "In_Frame_Ins",
		"inframe_deletion": "In_Frame_Del",
		"missense_variant": "Missense_Mutation",
		"protein_altering_variant": "Missense_Mutation", // TODO Not sure if this is correct
		"coding_sequence_variant": "Missense_Mutation",
		"conservative_missense_variant": "Missense_Mutation",
		"rare_amino_acid_variant": "Missense_Mutation",
		"transcript_amplification": "Intron",
		"splice_region_variant": "Intron",
		"intron_variant": "Intron",
		"INTRAGENIC": "Intron",
		"intragenic_variant": "Intron",
		"incomplete_terminal_codon_variant": "Silent",
		"synonymous_variant": "Silent",
		"stop_retained_variant": "Silent",
		"NMD_transcript_variant": "Silent",
		"mature_miRNA_variant": "RNA",
		"non_coding_exon_variant": "RNA",
		"non_coding_transcript_exon_variant": "RNA",
		"non_coding_transcript_variant": "RNA",
		"nc_transcript_variant": "RNA",
		"5_prime_UTR_variant": "5'UTR",
		"5_prime_UTR_premature_start_codon_gain_variant": "5'UTR",
		"3_prime_UTR_variant": "3'UTR",
		"TF_binding_site_variant": "IGR",
		"regulatory_region_variant": "IGR",
		"regulatory_region": "IGR",
		"intergenic_variant": "IGR",
		"intergenic_region": "IGR",
		"upstream_gene_variant": "5'Flank",
		"downstream_gene_variant": "3'Flank",
		"TFBS_ablation": "Targeted_Region",
		"TFBS_amplification": "Targeted_Region",
		"regulatory_region_ablation": "Targeted_Region",
		"regulatory_region_amplification": "Targeted_Region",
		"feature_elongation": "Targeted_Region",
		"feature_truncation": "Targeted_Region"
	};

	/**
	 * Parses the raw annotation JSON object.
	 *
	 * @param annotation  JSON object returned by the web service
	 * @return {object}  parsed JSON, or null in case of an error
	 */
	function parseJSON(annotation)
	{
		var vepData = {};

		if (!annotation)
		{
			console.log("[warning] VEP parser error");
			return {};
		}
		else if (annotation.error)
		{
			console.log("[warning] VEP parser error: " + annotation.error);
			return {};
		}

		// proceed in case of no JSON error
		var alleleString = annotation["allele_string"];
		var alleles = alleleString.split("/", -1);

		if (alleles.length === 2)
		{
			vepData.referenceAllele = alleles[0];
			//vepData.put(AnnoMafProcessor.VEP_REFERENCE_ALLELE.toLowerCase(), alleles[0]);
			//vepData.put(AnnoMafProcessor.VEP_TUMOR_SEQ_ALLELE.toLowerCase(), alleles[1]);

			//vepData.put(AnnoMafProcessor.VEP_VARIANT_TYPE.toLowerCase(), variantType);
			vepData.variantType = getVariantType(alleles[0], alleles[1]);
		}

		vepData.ncbiBuildNo = annotation["assembly_name"];
		vepData.chr = annotation["seq_region_name"];
		vepData.startPos = annotation["start"];
		vepData.endPos = annotation["end"];
		vepData.strand = strandSign(annotation["strand"]);

		var transcripts = annotation["transcript_consequences"];
		var mostSevereConsequence = annotation["most_severe_consequence"];

		// parse all transcripts
		vepData.transcripts = [];
		_.each(transcripts, function(transcript, idx) {
			vepData.transcripts.push(
				parseTranscript(transcript, mostSevereConsequence, vepData.variantType));
		});

		// TODO what to do in case no canonical transcript can be determined?
		var canonicalTranscript = getCanonicalTranscript(transcripts, mostSevereConsequence);

		if (canonicalTranscript &&
		    vepData.transcripts[canonicalTranscript.index])
		{
			vepData.canonicalTranscript = vepData.transcripts[canonicalTranscript.index];
		}

		// also attach the original raw data
		vepData.rawData = annotation;

		return vepData;
	}

	function parseTranscript(transcript, mostSevereConsequence, variantType, vepData)
	{
		vepData = vepData || {};

		vepData.geneSymbol = transcript["gene_symbol"];

		// JsonNode variantAllele = transcript.path("variant_allele");
		// if (!variantAllele.isMissingNode()) {
		// vepData.put(AnnoMafProcessor.VEP_TUMOR_SEQ_ALLELE.toLowerCase(), variantAllele.asText());
		// }

		var consequenceTerms = transcript["consequence_terms"];

		if (consequenceTerms != null &&
		    consequenceTerms.length > 0)
		{
			// TODO what if more than one consequence term?
			var variantClass = variantClassification(consequenceTerms[0]);

			if(variantClass === "Frame_Shift") {
				if (variantType != null && variantType === "INS") {
					variantClass += "_Ins";
				}
				else if (variantType === "DEL") {
					variantClass += "_Del";
				}
			}

			vepData.variantClassification = variantClass;
		}

		var refseqIds = transcript["refseq_transcript_ids"];

		if (refseqIds != null &&
		    refseqIds.length > 0)
		{
			vepData.refseqIds = refseqIds;
		}

		var hgvsc = transcript["hgvsc"];
		if (hgvsc != null) {
			vepData.hgvsc = hgvsc.substr(hgvsc.indexOf(":")+1);
		}

		var hgvsp = transcript["hgvsp"];
		if (hgvsp != null)
		{
			// TODO (p.%3D) ?
			//if (hgvsp.indexOf("(p.%3D)") != -1) {
			//	vepData.put(AnnoMafProcessor.VEP_HGVSP.toLowerCase(), "p.=");
			//}

			vepData.hgvsp = hgvsp.substr(hgvsp.indexOf(":")+1);
		}

		vepData.transcriptId = transcript["transcript_id"];
		vepData.proteinPosStart = transcript["protein_start"];
		vepData.proteinPosEnd = transcript["protein_end"];
		vepData.codons = transcript["codons"];

		// create a shorter HGVS protein format
		var hgvspShort;

		if (hgvsp != null)
		{
			hgvspShort = hgvsp.substr(hgvsp.indexOf(":")+1);

			_.each(_.pairs(_aa3to1), function(pair, idx) {
				hgvspShort = hgvspShort.replace(new RegExp(pair[0], 'g'), pair[1]);
			});

			vepData.hgvspShort = hgvspShort;
		}

		if (mostSevereConsequence === "splice_acceptor_variant" ||
		    mostSevereConsequence === "splice_donor_variant")
		{
			//Pattern pattern = Pattern.compile("^c.([0-9]+)*");
			//Matcher matcher = pattern.matcher(hgvsc.asText().substring(iHgsvc+1));

			//if( matcher.find() ) {
			//	int cPos = Integer.parseInt(matcher.group(1));
			//	if( cPos < 1 ) {
			//		cPos = 1;
			//	}
			//
			//	var pPos = Integer.toString(( cPos + cPos % 3 ) / 3 );
			//
			//	vepData.hgvspShort = "p.X" + pPos + "_splice";
			//}

			if (vepData.hgvsc)
			{
				var match = /c\.([0-9]+)*/.exec(vepData.hgvsc);

				if (match && match.length == 2)
				{
					var cPos = parseInt(match[1]);

					if (cPos < 1) {
						cPos = 1;
					}

					var pPos = cPos + (cPos % 3) / 3;

					vepData.hgvspShort = "p.X" + pPos + "_splice";
				}
			}
		}

		if (mostSevereConsequence === "synonymous_variant")
		{
			hgvspShort = "p." +
				transcript["amino_acids"] +
				transcript["protein_start"] +
				transcript["amino_acids"];

			vepData.hgvspShort = hgvspShort;
		}

		// set aliases
		vepData.mutationType = vepData.variantClassification;
		vepData.proteinChange = vepData.hgvspShort;
		if (vepData.refseqIds && vepData.refseqIds.length > 0) {
			// TODO is it okay to pick the first one as the default refseq id?
			vepData.refseqMrnaId = vepData.refseqIds[0];
		}

		return vepData;
	}

	/**
	 * Finds and returns the canonical transcript within the given transcript list.
	 * Returns null in case no canonical transcript can be determined.
	 *
	 * @param transcripts list of transcript nodes
	 * @param  mostSevereConsequence
	 * @return {object} canonical transcript node
	 */
	function getCanonicalTranscript(transcripts, mostSevereConsequence)
	{
		var list = [];

		_.each(transcripts, function(transcript, idx) {
			if (transcript["canonical"] == 1)
			{
				list.push({index: idx, transcript:transcript});
			}
		});

		// trivial case: only one transcript marked as canonical
		if (list.length === 1)
		{
			return list[0];
		}
		// more than one transcript is marked as canonical,
		// use most severe consequence to decide which one to pick
		// among the ones marked as canonical
		else if (list.length > 1)
		{
			return transcriptWithMostSevereConsequence(list, mostSevereConsequence);
		}
		// no transcript is marked as canonical (list.size() == 0),
		// use most severe consequence to decide which one to pick
		// among all available transcripts
		else
		{
			_.each(transcripts, function(transcript, idx) {
				list.push({index: idx, transcript:transcript});
			});

			return transcriptWithMostSevereConsequence(list, mostSevereConsequence);
		}
	}

	/**
	 * Finds and returns the transcript node which has the given
	 * most severe consequence in its consequence terms. Returns
	 * null in case no match.
	 *
	 * @param transcripts           list of transcript nodes
	 * @param mostSevereConsequence most severe consequence
	 * @return transcript node containing most severe consequence
	 */
	function transcriptWithMostSevereConsequence(transcripts, mostSevereConsequence)
	{
		// default value is null in case of no match
		var transcriptWithMSC = null;

		_.each(transcripts, function(ele, idx) {
			var consequenceTerms = ele.transcript["consequence_terms"];

			if (transcriptWithMSC == null &&
			    consequenceTerms != null &&
			    mostSevereConsequence != null)
			{
				_.each(consequenceTerms, function(consequence, idx) {
					if (consequence.trim().toLowerCase() ===
					    mostSevereConsequence.trim().toLowerCase())
					{
						transcriptWithMSC = ele;
					}
				});
			}
		});

		return transcriptWithMSC;
	}

	function getVariantType(refAllele, varAllele)
	{
		var refLength = refAllele.length;
		var varLength = varAllele.length;
		refLength = refAllele === "-" ? 0 : refLength;
		varLength = varAllele === "-" ? 0 : varLength;

		if (refLength === varLength) {
			var npType = ["SNP", "DNP", "TNP"];
			return (refLength < 3 ? npType[refLength - 1] : "ONP");
		}
		else {
			if (refLength < varLength) {
				return "INS";
			}
			else {
				return "DEL";
			}
		}
	}

	function variantClassification(variant)
	{
		return _variantMap[variant.toLowerCase()];
	}

	function strandSign(strand)
	{
		var sign;

		if (strand == null ||
		    strand === "+" ||
		    strand === "-")
		{
			sign = strand;
		}
		else
		{
			if (strand < 0)
			{
				sign = "-";
			}
			else if (strand > 0)
			{
				sign = "+";
			}
			else
			{
				sign = strand;
			}
		}

		return sign;
	}

	return {
		parseJSON: parseJSON
	};
})();


/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Mutation Model.
 *
 * Current model is sufficient to visualize both the table and the diagram.
 * Later we may add more data if necessary.
 *
 * @author Selcuk Onur Sumer
 */
var MutationModel = Backbone.Model.extend({
	// TODO update initialize method when all done!
	_initialize: function(attributes) {
		this.mutationId = attributes.mutationId;
        this.mutationSid = attributes.mutationSid;
		this.geneticProfileId = attributes.geneticProfileId;
		this.mutationEventId = attributes.mutationEventId;
		this.caseId = attributes.caseId;
		this.geneSymbol = attributes.geneSymbol;
		this.linkToPatientView = attributes.linkToPatientView;
        this.cancerType = attributes.cancerType;
        this.cancerStudy = attributes.cancerStudy;
        this.cancerStudyShort = attributes.cancerStudyShort;
        this.cancerStudyLink = attributes.cancerStudyLink;
		this.tumorType = attributes.tumorType;
		this.proteinChange = attributes.proteinChange;
		this.aminoAcidChange = attributes.aminoAcidChange;
		this.mutationType = attributes.mutationType;
		this.cosmic = attributes.cosmic;
		this.cosmicCount = this.calcCosmicCount(attributes.cosmic);
		this.functionalImpactScore = attributes.functionalImpactScore;
		this.fisValue = attributes.fisValue;
		this.msaLink = attributes.msaLink;
		this.xVarLink = attributes.xVarLink;
		this.pdbLink = attributes.pdbLink;
		this.pdbMatch = attributes.pdbMatch; // {pdbId, chainId} pair
		this.igvLink = attributes.igvLink;
		this.mutationStatus = attributes.mutationStatus;
		this.validationStatus = attributes.validationStatus;
		this.sequencingCenter = attributes.sequencingCenter;
		this.ncbiBuildNo = attributes.ncbiBuildNo;
		this.chr = attributes.chr;
		this.startPos = attributes.startPos;
		this.endPos = attributes.endPos;
		this.referenceAllele = attributes.referenceAllele;
		this.variantAllele = attributes.variantAllele;
		this.tumorFreq = attributes.tumorFreq;
		this.normalFreq = attributes.normalFreq;
		this.tumorRefCount = attributes.tumorRefCount;
		this.tumorAltCount = attributes.tumorAltCount;
		this.normalRefCount = attributes.normalRefCount;
		this.normalAltCount = attributes.normalAltCount;
		this.canonicalTranscript = attributes.canonicalTranscript;
		this.refseqMrnaId = attributes.refseqMrnaId;
		this.codonChange = attributes.codonChange;
		this.uniprotId = attributes.uniprotId;
		this.uniprotAcc = attributes.uniprotAcc;
		this.proteinPosStart = attributes.proteinPosStart;
		this.proteinPosEnd = attributes.proteinPosEnd;
		this.mutationCount = attributes.mutationCount;
		this.specialGeneData = attributes.specialGeneData;
		this.keyword = attributes.keyword;
		this.cna = attributes.cna;
		this.myCancerGenome = attributes.myCancerGenome;
		this.isHotspot = attributes.isHotspot;
	},
	url: function() {
		// TODO implement this to get the data from a web service
		var urlStr = "webservice.do?cmd=...";
	},
	/**
	 * Finds out the protein start position for this mutation.
	 * The field proteinPosStart has a priority over proteinChange.
	 * If none of these has a valid value, then this function
	 * returns null.
	 *
	 * @return protein start position
	 */
	getProteinStartPos: function()
	{
		// first try protein start pos
		var position = this.get("proteinPosStart");

		// if not valid, then try protein change value
		if (position == null ||
		    position.length == 0 ||
		    position == "NA" ||
		    position < 0)
		{
			position = this.getProteinChangeLocation();
		}

		return position;
	},
	/**
	 * Finds the uniprot location for the protein change of
	 * the given mutation.
	 *
	 * @return {String} protein location as a string value
	 */
	getProteinChangeLocation: function()
	{
		var location = null;
		var result = null;
		var proteinChange = this.get("proteinChange");

		if (proteinChange != null)
		{
			result = proteinChange.match(/[0-9]+/);
		}

		if (result && result.length > 0)
		{
			location = result[0];
		}

		return location;
	},
	getCosmicCount: function()
	{
		// if already set, return the current value
		if (this.get("cosmicCount")) {
			return this.get("cosmicCount");
		}
		// if not set yet, calculate & set & return the value
		else if (this.get("cosmic")) {
			var cosmicCount = this.calcCosmicCount(this.get("cosmic"));
			this.set({cosmicCount: cosmicCount});
			return cosmicCount;
		}
		// NA
		else {
			return null;
		}
	},
	calcCosmicCount: function(cosmic)
	{
		var cosmicCount = 0;

		if (cosmic)
		{
			cosmic.forEach(function(c) {
				cosmicCount += c[2];
			});
		}

		return cosmicCount;
	}
});

/**
 * Collection of mutations (MutationModel instances).
 */
var MutationCollection = Backbone.Collection.extend({
	model: MutationModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	},
	parse: function(response) {
		// TODO parse response (returned from web service)
		// this.attributes = function() { return response.attributes; };   // save the attributes
		// return response.data;    // but the data is what is to be model-ed
	},
	url: function() {
		// TODO implement this to get the data from a web service
		var urlStr = "webservice.do?cmd=...";
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var PdbAlignmentModel = Backbone.Model.extend({
	initialize: function(attributes) {
		this.alignmentId = attributes.alignmentId;
		this.pdbId = attributes.pdbId;
		this.chain = attributes.chain;
		this.uniprotId = attributes.uniprotId;
		this.pdbFrom = attributes.pdbFrom;
		this.pdbTo = attributes.pdbTo;
		this.uniprotFrom = attributes.uniprotFrom;
		this.uniprotTo = attributes.uniprotTo;
		this.alignmentString = attributes.alignmentString ||
		                       PdbDataUtil.alignmentString(attributes);
		this.eValue = attributes.eValue;
		this.identityPerc = attributes.identityPerc;
	}
});

/**
 * Collection of pdb alignment data (PdbAlignmentModel instances).
 */
var PdbAlignmentCollection = Backbone.Collection.extend({
	model: PdbAlignmentModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	}
});
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * PDB Chain Model.
 *
 * @author Selcuk Onur Sumer
 */
var PdbChainModel = Backbone.Model.extend({
	initialize: function(attributes) {
		// chain id (A, B, C, X, etc.)
		this.chainId = attributes.chainId;
		//  map of (mutation id, pdb position) pairs
		this.positionMap = attributes.positionMap;
		// collection of PdbAlignmentModel instances
		this.alignments = new PdbAlignmentCollection(attributes.alignments);
		// summary of all alignments (merged alignments)
		// TODO define a model for merged alignments (PdbMergedAlignment) ?
		this.mergedAlignment = PdbDataUtil.mergeAlignments(attributes.alignments);
	}
});

/**
 * Collection of pdb data (PdbModel instances).
 */
var PdbChainCollection = Backbone.Collection.extend({
	model: PdbChainModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	}
});
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * PDB data model.
 *
 * Contains PDB id and a chain list.
 *
 * @author Selcuk Onur Sumer
 */
var PdbModel = Backbone.Model.extend({
	initialize: function(attributes) {
		// pdb id (e.g: 1d5r)
		this.pdbId = attributes.pdbId;
		// collection of PdbChainModel instances
		this.chains = new PdbChainCollection(attributes.chains);
	}
});

/**
 * Collection of pdb data (PdbModel instances).
 */
var PdbCollection = Backbone.Collection.extend({
	model: PdbModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Pileup Model.
 *
 * This model is designed to represent multiple mutations at the same
 * position. This is intended to be used for mutation diagram.
 *
 * @author Selcuk Onur Sumer
 */
var Pileup = Backbone.Model.extend({
	initialize: function(attributes) {
		this.pileupId = attributes.pileupId; // incremental id (client-side generated)
		this.mutations = attributes.mutations; // array of mutations at this data point
		this.count = attributes.count; // number of mutations at this data point
		this.location = attributes.location; // the location of the mutations
		this.label = attributes.label; // text label for this data point
		this.stats = attributes.stats;
	}
});
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Tooltip view for the mutation table's cosmic column.
 *
 * options: {el: [target container],
 *           model: {cosmic: [raw cosmic text],
 *                   geneSymbol: [hugo gene symbol],
 *                   keyword: [mutation keyword],
 *                   total: [number of total cosmic occurrences]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var CosmicTipView = Backbone.View.extend({
	render: function()
	{
		// compile the template
		var template = this.compileTemplate();

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);
		this.format();
	},
	format: function()
	{
		// initialize cosmic details table
		this.$el.find(".cosmic-details-table").dataTable({
			"aaSorting" : [[2, "desc"]], // sort by count at init
			"sDom": 'pt', // show the table and the pagination buttons
			"aoColumnDefs": [
				{"mRender": function ( data, type, full ) {
						// TODO move this link into the template
                        return '<a href="http://cancer.sanger.ac.uk/cosmic/mutation/overview?id='+data+'">'+data+'</a>';
                    }, "aTargets": [0]},
				{"sType": "aa-change-col", "sClass": "left-align-td", "aTargets": [1]},
				{"sType": "numeric", "sClass": "left-align-td", "aTargets": [2]}],
			"bDestroy": false,
			"bPaginate": true,
			"bJQueryUI": true,
			"bFilter": false});
	},
	_parseCosmic: function(cosmic)
	{
		var dataRows = [];
		// TODO create a backbone template for the cosmic table row
		// COSMIC data (as AA change & frequency pairs)
		cosmic.forEach(function(c) {
                        dataRows.push(c[0]+"</td><td>"+c[1]+"</td><td>"+c[2]);
                    });

		return "<tr><td>" + dataRows.join("</td></tr><tr><td>") + "</td></tr>";
	},
	compileTemplate: function()
	{
		var dataRows = this._parseCosmic(this.model.cosmic);

		// pass variables in using Underscore.js template
		var variables = {cosmicDataRows: dataRows,
			cosmicTotal: this.model.total,
			mutationKeyword: this.model.keyword};

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_details_cosmic_tip_template");
		return templateFn(variables);
	}
});
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This view will add new columns to the mutation stats table
 * model: { cancerType: "", count: 0 }
 */
var LollipopTipStatsView = Backbone.View.extend({
	initialize: function()
	{

	},
    render: function()
    {
        var templateFn = BackboneTemplateCache.getTemplateFn("mutation_details_lollipop_tip_stats_template");
        var thatEl = this.$el.find("table tbody");
        _.each(this.model, function(statItem) {
            thatEl.append(templateFn(statItem));
        });
        return this;
    }
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Tooltip view for the mutation diagram's lollipop circles.
 *
 * options: {el: [target container],
 *           model: {count: [number of mutations],
 *                   label: [info for that location]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var LollipopTipView = Backbone.View.extend({
	render: function()
	{
		// compile the template
		var template = this.compileTemplate();

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);
		this.format();
	},
	format: function()
	{
		// implement if necessary...
	},

    showStats: false,
    setShowStats: function(showStats) {
        this.showStats = showStats;
    },
    getShowStats: function(showStats) {
        return this.showStats;
    },

    compileTemplate: function()
	{
        var thatModel = this.model;
        var mutationStr = thatModel.count > 1 ? "mutations" : "mutation";

		// pass variables in using Underscore.js template
		var variables = {count: thatModel.count,
			mutationStr: mutationStr,
			label: thatModel.label
        };

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_details_lollipop_tip_template");
        var compiledEl = $(templateFn(variables));

        var statsEl = compiledEl.find(".lollipop-stats");
        if(this.showStats)
        {
            (new LollipopTipStatsView({ el: statsEl, model: thatModel.stats })).render();
            statsEl.find("table").dataTable({
                "sDom": 't',
                "bJQueryUI": true,
                "bDestroy": true,
                "aaSorting": [[ 1, "desc" ]],
                "aoColumns": [
                    { "bSortable": false },
                    { "bSortable": false }
                ]
            });
        } else {
            statsEl.hide();
        }

        return compiledEl.html();
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Default mutation view for a single gene.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: [hugo gene symbol],
 *                   mutationData: [mutation data for a specific gene]
 *                   dataProxies: [all available data proxies],
 *                   dataManager: global mutation data manager,
 *                   uniprotId: uniprot identifier,
 *                   sampleArray: [list of case ids as an array of strings]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MainMutationView = Backbone.View.extend({
	initialize : function (options) {
		var defaultOpts = {
			config: {
				loaderImage: "images/ajax-loader.gif"
			}
		};

		this.options = jQuery.extend(true, {}, defaultOpts, options);

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function() {
		var self = this;

		// pass variables in using Underscore.js template
		var variables = {geneSymbol: self.model.geneSymbol,
			uniprotId: self.model.uniprotId};

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_view_template");
		var template = templateFn(variables);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	format: function() {
		var self = this;

		// initially hide all components by default
		// they will be activated wrt selected options
		self.$el.find(".mutation-details-filter-info").hide();
		self.$el.find(".mutation-details-no-data-info").hide();
		self.$el.find(".mutation-3d-initializer").hide();
		self.$el.find(".mutation-info-panel-container").hide();
		self.$el.find(".mutation-summary-view").hide();
		self.$el.find(".mutation-table-container").hide();
		self.$el.find(".mutation-diagram-view").hide();
	},
	initPdbPanelView: function(renderOpts, panelOpts, tableOpts, pdbColl)
	{
		var self = this;
		var diagram = null;

		// diagram can be null/disabled
		if (self.diagramView && self.diagramView.mutationDiagram)
		{
			diagram = self.diagramView.mutationDiagram;
		}

		// TODO we should not be overwriting the render options...
		renderOpts.loaderImage = self.options.config.loaderImage;

		// allow initializing the pdb panel even if there is no diagram
		var viewOpts = {
			//el: "#mutation_pdb_panel_view_" + gene.toUpperCase(),
			el: self.$el.find(".mutation-pdb-panel-view"),
			config: renderOpts,
			model: {geneSymbol: self.model.geneSymbol,
				pdbColl: pdbColl,
				pdbProxy: self.model.dataProxies.pdbProxy,
				pdbPanelOpts: panelOpts,
				pdbTableOpts: tableOpts},
			diagram: diagram
		};

		var pdbPanelView = new PdbPanelView(viewOpts);
		pdbPanelView.render();

		self._pdbPanelView = pdbPanelView;

		return pdbPanelView;
	},
	initSummaryView: function()
	{
		var self = this;
		var target = self.$el.find(".mutation-summary-view");
		target.show();

		var summaryOpts = {
			el: target,
			model: {
				mutationProxy: self.model.dataProxies.mutationProxy,
				clinicalProxy: self.model.dataProxies.clinicalProxy,
				geneSymbol: self.model.geneSymbol,
				sampleArray: self.model.sampleArray
			}
		};

		var summaryView = new MutationSummaryView(summaryOpts);
		summaryView.render();

		self.summaryView = summaryView;

		return summaryView;
	},
	init3dView: function(mut3dVisView)
	{
		var self = this;

		return self._init3dView(self.model.geneSymbol,
			self.model.uniprotId,
			self.model.dataProxies.pdbProxy,
			mut3dVisView);
	},
	/**
	 * Initializes the 3D view initializer.
	 *
	 * @param gene
	 * @param uniprotId
	 * @param pdbProxy
	 * @param mut3dVisView
	 * @return {Object}     a Mutation3dView instance
	 */
	_init3dView: function(gene, uniprotId, pdbProxy, mut3dVisView)
	{
		var self = this;

		var target = self.$el.find(".mutation-3d-initializer");
		target.show();

		// init the 3d view (button)
		var view3d = new Mutation3dView({
			el: target,
			model: {uniprotId: uniprotId,
				geneSymbol: gene,
				pdbProxy: pdbProxy}
		});

		view3d.render();

		// also reset (init) the 3D view if the 3D panel is already active
		if (mut3dVisView &&
		    mut3dVisView.isVisible())
		{
			view3d.resetView();
		}

		return view3d;
	},
	/**
	 * Initializes the mutation diagram view for the given diagram options
	 * and sequence data.
	 *
	 * @param options   mutation diagram options
	 * @param sequence  PFAM sequence data
	 * @returns {MutationDiagramView} mutation diagram view instance
	 */
	initMutationDiagramView: function(options, sequence)
	{
		var self = this;

		//mutationData = mutationData || self.model.mutationData;

		self.diagramView = self._initMutationDiagramView(
			self.model.geneSymbol,
			self.model.mutationData,
			sequence,
			self.model.dataProxies,
		    options);

		if (!self.diagramView)
		{
			console.log("Error initializing mutation diagram: %s", self.model.geneSymbol);
		}
		else
		{
			self.dispatcher.trigger(
				MutationDetailsEvents.DIAGRAM_INIT,
				self.diagramView.mutationDiagram);
		}

		return self.diagramView;
	},
	/**
	 * Initializes the mutation diagram view.
	 *
	 * @param gene          hugo gene symbol
	 * @param mutationData  mutation data (array of JSON objects)
	 * @param sequenceData  sequence data (as a JSON object)
	 * @param dataProxies   all available data proxies
	 * @param options       [optional] diagram options
	 * @return {Object}     initialized mutation diagram view
	 */
	_initMutationDiagramView: function (gene, mutationData, sequenceData, dataProxies, options)
	{
		var self = this;
		var target = self.$el.find(".mutation-diagram-view");
		target.show();

		var model = {mutations: mutationData,
			sequence: sequenceData,
			geneSymbol: gene,
			dataProxies: dataProxies,
			diagramOpts: options};

		var diagramView = new MutationDiagramView({
			el: target,
			model: model});

		diagramView.render();

		return diagramView;
	},
	initMutationTableView: function(options)
	{
		var self = this;

		self.tableView = self._initMutationTableView(self.model.geneSymbol,
			self.model.mutationData,
			self.model.dataProxies,
			self.model.dataManager,
		    options);

		if (!self.tableView)
		{
			console.log("Error initializing mutation table: %s", self.model.geneSymbol);
		}

		return self.tableView;
	},
	/**
	 * Initializes the mutation table view.
	 *
	 * @param gene          hugo gene symbol
	 * @param mutationData  mutation data (array of JSON objects)
	 * @param dataProxies   all available data proxies
	 * @param dataManager   global mutation data manager
	 * @param options       [optional] table options
	 * @return {Object}     initialized mutation table view
	 */
	_initMutationTableView: function(gene, mutationData, dataProxies, dataManager, options)
	{
		var self = this;
		var target = self.$el.find(".mutation-table-container");
		target.show();

		var mutationTableView = new MutationDetailsTableView({
			el: target,
			config: {loaderImage: self.options.config.loaderImage},
			model: {geneSymbol: gene,
				mutations: mutationData,
				dataProxies: dataProxies,
				dataManager: dataManager,
				tableOpts: options}
		});

		mutationTableView.render();

		return mutationTableView;
	},
	initMutationInfoView: function(options)
	{
		var self = this;
		var target = self.$el.find(".mutation-info-panel-container");
		target.show();

		var model = {
			mutations: self.model.mutationData,
			infoPanelOpts: options
		};

		var infoView = new MutationInfoPanelView({
			el: target,
			model: model
		});

		infoView.render();

		self.infoView = infoView;

		self.dispatcher.trigger(
			MutationDetailsEvents.INFO_PANEL_INIT,
			self.infoView);

		return infoView;
	},
	/**
	 * Initializes the filter reset link, which is a part of filter info
	 * text on top of the diagram, with the given callback function.
	 *
	 * @param callback      function to be invoked on click
	 */
	addResetCallback: function(callback) {
		var self = this;
		var resetLink = self.$el.find(".mutation-details-filter-reset");

		// add listener to diagram reset link
		resetLink.click(callback);
	},
	showFilterInfo: function() {
		this.$el.find(".mutation-details-filter-info").slideDown();
	},
	hideFilterInfo: function() {
		this.$el.find(".mutation-details-filter-info").slideUp();
	},
	showNoDataInfo: function() {
		this.$el.find(".mutation-details-no-data-info").slideDown();
	},
	hideNoDataInfo: function() {
		this.$el.find(".mutation-details-no-data-info").slideUp();
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 3D visualizer controls view.
 *
 * This view is designed to provide controls to initialize, show or hide
 * the actual 3D visualizer panel.
 *
 * IMPORTANT NOTE: This view does not initialize the actual 3D visualizer.
 * 3D visualizer is a global instance bound to MutationDetailsView
 * and it is a part of Mutation3dVisView.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: hugo gene symbol,
 *                   uniprotId: uniprot identifier for this gene,
 *                   pdbProxy: pdb data proxy}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;
		var gene = self.model.geneSymbol;

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_3d_view_template");
		var template = templateFn({});

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);

		// format after rendering
		this.format();
	},
	format: function()
	{
		var self = this;
		var button3d = self.$el.find(".mutation-3d-vis");

		// initially disable the 3D button
		button3d.attr("disabled", "disabled");

		var formatButton = function(hasData) {
			if (hasData)
			{
				// enable button if there is PDB data
				button3d.removeAttr("disabled");
			}
			else
			{
				var gene = self.model.geneSymbol;
				var content = "No structure data for " + gene;

				// set tooltip options
				var qtipOpts = {content: {text: content},
					hide: {fixed: true, delay: 100, event: 'mouseout'},
					show: {event: 'mouseover'},
					style: {classes: 'qtip-light qtip-rounded qtip-shadow cc-ui-tooltip'},
					position: {my:'bottom center', at:'top center', viewport: $(window)}};

				// disabled buttons do not trigger mouse events,
				// so add tooltip to the wrapper div instead
				self.$el.qtip(qtipOpts);
			}
		};

		var pdbProxy = self.model.pdbProxy;
		var uniprotId = self.model.uniprotId;

		pdbProxy.hasPdbData(uniprotId, formatButton);
	},
	/**
	 * Adds a callback function for the 3D visualizer init button.
	 *
	 * @param callback      function to be invoked on click
	 */
	addInitCallback: function(callback) {
		var self = this;
		var button3d = self.$el.find(".mutation-3d-vis");

		// add listener to 3D init button
		button3d.click(callback);
	},
	/**
	 * Resets the 3D view to its initial state.
	 */
	resetView: function()
	{
		var self = this;
		var button3d = self.$el.find(".mutation-3d-vis");

		// TODO this might not be safe, since we are relying on the callback function

		// just simulate click function on the 3d button to reset the view
		button3d.click();
	},
	isVisible: function()
	{
		var self = this;
		return self.$el.is(":visible");
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Information view for the 3D Visualization panel.
 *
 * options: {el: [target container],
 *           model: {pdbId: String,
 *                   chainId: String,
 *                   pdbInfo: String,
 *                   molInfo: String}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dVisInfoView = Backbone.View.extend({
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_3d_vis_info_template");
		var template = templateFn(self.model);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;
		var pdbInfo = self.model.pdbInfo;
		var molInfo = self.model.molInfo;

		// if no info provided, then hide the corresponding span
		if (pdbInfo == null ||
		    pdbInfo.length == 0)
		{
			self.$el.find(".mutation-3d-pdb-info").hide();
		}
		else
		{
			// make information text expandable/collapsible
			self._addExpander(".mutation-3d-pdb-info");
		}

		if (molInfo == null ||
		    molInfo.length == 0)
		{
			self.$el.find(".mutation-3d-mol-info").hide();
		}
		else
		{
			// make information text expandable/collapsible
			self._addExpander(".mutation-3d-mol-info");
		}
	},
	/**
	 * Applies expander plugin to the PDB info area. The options are
	 * optimized to have 1 line of description at init.
	 */
	_addExpander: function(selector)
	{
		var self = this;

		var expanderOpts = {slicePoint: 40, // default is 100
			widow: 2,
			expandPrefix: ' ',
			expandText: '[...]',
			//collapseTimer: 5000, // default is 0, so no re-collapsing
			userCollapseText: '[^]',
			moreClass: 'expander-read-more',
			lessClass: 'expander-read-less',
			detailClass: 'expander-details',
			// do not use default effects
			// (see https://github.com/kswedberg/jquery-expander/issues/46)
			expandEffect: 'fadeIn',
			collapseEffect: 'fadeOut'};

		//self.$el.find(".mutation-3d-info-main").expander(expanderOpts);
		self.$el.find(selector).expander(expanderOpts);
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Actual 3D Visualizer view. This view is designed to contain the 3D
 * structure visualizer app and its control buttons.
 *
 * options: {el: [target container],
 *           mut3dVis: reference to the Mutation3dVis instance,
 *           pdbProxy: PDB data proxy,
 *           mutationProxy: mutation data proxy
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dVisView = Backbone.View.extend({
	initialize : function (options) {
		var defaultOpts = {
			config: {
				loaderImage: "images/ajax-loader.gif",
				helpImage: "images/help.png",
				border: {
					top: 0,
					left: 0
				}
			}
		};

		this.options = jQuery.extend(true, {}, defaultOpts, options);

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_3d_vis_template");

		var template = templateFn({
			loaderImage: self.options.config.loaderImage,
			helpImage: self.options.config.helpImage
		});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		var container3d = self.$el;

		// initially hide the residue warning message
		self.hideResidueWarning();
		self.hideNoMapWarning();

		// initially hide the help content
		var helpContent = self.$el.find(".mutation-3d-vis-help-content");

		// TODO use the self.options.viewer object to determine which content to display!
		var helpTemplateFn = BackboneTemplateCache.getTemplateFn("3Dmol_basic_interaction");
		helpContent.html(helpTemplateFn({}));
		helpContent.hide();

		// update the container of 3d visualizer
		if (mut3dVis != null)
		{
			mut3dVis.updateContainer(container3d);
			mut3dVis.show();
		}

		// add listeners to panel (header) buttons

		self.$el.find(".mutation-3d-close").click(function() {
			self.hideView();
		});

		self.$el.find(".mutation-3d-minimize").click(function(){
			if (mut3dVis != null)
			{
				mut3dVis.toggleSize();
			}
		});

		// format toolbar elements

		// mutation style controls
		self._initMutationControls();

		// protein style controls
		self._initProteinControls();

		// zoom slider
		//self._initZoomSlider();

		// init buttons
		self._initButtons();

		self.showMainLoader();

		// make the main container draggable
		container3d.draggable({
			handle: ".mutation-3d-info-title",
//			start: function(event, ui) {
//				// fix the width to prevent resize during drag
//				var width = container3d.css("width");
//				container3d.css("width", width);
//			},
			stop: function(event, ui) {
				var top = parseInt(container3d.css("top"));
				var left = parseInt(container3d.css("left"));
				//var width = parseInt(container3d.css("width"));

				// if the panel goes beyond the visible area, get it back!

				if (top < parseInt(self.options.config.border.top))
				{
					container3d.css("top", self.options.config.border.top);
				}

				//if (left < -width)
				if (left < parseInt(self.options.config.border.left))
				{
					container3d.css("left", self.options.config.border.left);
				}

				// TODO user can still take the panel out by dragging it to the bottom or right
			}
		});

		//TODO something like this might be safer for "alsoResize" option:
		// container3d.find(".mutation-3d-vis-container,.mutation-3d-vis-container div:eq(0)")

		// make the container resizable
		container3d.resizable({
			alsoResize: ".mutation-3d-vis-container,.mutation-3d-vis-container div:eq(0)",
			//alsoResize: ".mutation-3d-vis-container",
			handles: "sw, s, w",
			minWidth: 400,
			minHeight: 300,
			start: function(event, ui) {
				// a workaround to properly redraw the 3d-info area
				container3d.find(".mutation-3d-vis-help-content").css("width", "auto");

				// a workaround to prevent position to be set to absolute
				container3d.css("position", "fixed");
			},
			stop: function(event, ui) {
				// a workaround to properly redraw the 3d-info area
				container3d.css("height", "auto");

				// a workaround to prevent position to be set to absolute
				container3d.css("position", "fixed");
			},
			resize: function(event, ui) {
				// this is to prevent window resize event to trigger
				event.stopPropagation();

				// resize (redraw) the 3D viewer
				// (since we don't propagate resize event up to window anymore)
				mut3dVis.resizeViewer();
			}
		})
		.on('resize', function(event) {
			// this is to prevent window resize event to trigger
			event.stopPropagation();
		});
	},
	/**
	 * Initializes the control buttons.
	 */
	_initButtons: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// init help text controls

		var helpContent = self.$el.find(".mutation-3d-vis-help-content");
		var helpInit = self.$el.find(".mutation-3d-vis-help-init");
		var helpInitLink = self.$el.find(".mutation-3d-vis-help-open");
		var helpClose = self.$el.find(".mutation-3d-vis-help-close");
		var pymolDownload = self.$el.find(".mutation-3d-pymol-dload");

		// add listener to help link
		helpInitLink.click(function(event) {
			event.preventDefault();
			helpContent.slideToggle();
			helpInit.slideToggle();
		});

		// add listener to help close button
		helpClose.click(function(event) {
			event.preventDefault();
			helpContent.slideToggle();
			helpInit.slideToggle();
		});

		// add listener to download link
		pymolDownload.click(function(event) {
			event.preventDefault();

			var script = mut3dVis.generatePymolScript();
			var filename = self.$el.find(".mutation-3d-pdb-id").text().trim() + "_" +
			               self.$el.find(".mutation-3d-chain-id").text().trim() + ".pml";

			var downloadOpts = {
				filename: filename,
				contentType: "text/plain;charset=utf-8",
				preProcess: false};

			// send download request with filename & file content info
			cbio.download.initDownload(script, downloadOpts);
		});

		pymolDownload.qtip(self._generateTooltipOpts("Download PyMOL script"));
	},
	/**
	 * Initializes the mutation style options UI and
	 * the corresponding event handlers.
	 */
	_initMutationControls: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		var sideChain = self.$el.find(".mutation-3d-side-chain-select");

		// handler for side chain checkbox
		sideChain.change(function() {
			//var display = sideChain.is(":checked");
			var selected = $(this).val();

			if (mut3dVis)
			{
				// update flag
				mut3dVis.updateOptions({displaySideChain: selected});
				mut3dVis.reapplyStyle();
			}
		});

		var colorMenu = self.$el.find(".mutation-3d-mutation-color-select");

		colorMenu.change(function() {
			var selected = $(this).val();

			if (mut3dVis)
			{
				// update color options
				mut3dVis.updateOptions({colorMutations: selected});
				// refresh view with new options
				mut3dVis.reapplyStyle();
			}
		});

		// add info tooltip for the color and side chain checkboxes
		self._initMutationColorInfo();
		self._initSideChainInfo();
	},
	/**
	 * Initializes the protein style options UI and
	 * the corresponding event handlers.
	 */
	_initProteinControls: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		var displayNonProtein = self.$el.find(".mutation-3d-display-non-protein");

		// handler for hide non protein checkbox
		displayNonProtein.change(function() {
			var display = displayNonProtein.is(":checked");

			if (mut3dVis)
			{
				// update flag
				mut3dVis.updateOptions({restrictProtein: !display});
				// refresh view with new options
				mut3dVis.reapplyStyle();
			}
		});

		// add info tooltip for the checkbox
		self._initHideNonProteinInfo();

		// protein scheme selector
		self._initProteinSchemeSelector();

		// protein color selector
		self._initProteinColorSelector();
	},
	/**
	 * Initializes the protein color selector drop-down menu
	 * with its default action handler.
	 */
	_initProteinColorSelector: function()
	{
		var self = this;
		var colorMenu = self.$el.find(".mutation-3d-protein-color-select");
		var mut3dVis = self.options.mut3dVis;

		colorMenu.change(function() {
			var selected = $(this).val();

			// update color options
			mut3dVis.updateOptions({colorProteins: selected});

			// refresh view with new options
			mut3dVis.reapplyStyle();
		});
	},
	/**
	 * Initializes the protein scheme selector dropdown menu
	 * with its default action handler.
	 */
	_initProteinSchemeSelector: function()
	{
		var self = this;

		var mut3dVis = self.options.mut3dVis;

		// selection menus
		var styleMenu = self.$el.find(".mutation-3d-protein-style-select");
		var colorMenu = self.$el.find(".mutation-3d-protein-color-select");

		// TODO chosen is somehow problematic...
		//styleMenu.chosen({width: 120, disable_search: true});

		// add info tooltip for the color selector
		self._initProteinColorInfo();

		// bind the change event listener
		styleMenu.change(function() {

			var selectedScheme = $(this).val();
			var selectedColor = false;

			// re-enable every color selection for protein
			colorMenu.find("option").removeAttr("disabled");

			var toDisable = [];

			// find the option to disable
			if (selectedScheme == "spaceFilling")
			{
				// disable color by secondary structure option
				toDisable.push(colorMenu.find("option[value='bySecondaryStructure']"));
				toDisable.push(colorMenu.find("option[value='byChain']"));
			}
			else
			{
				// disable color by atom type option
				toDisable.push(colorMenu.find("option[value='byAtomType']"));
			}

			_.each(toDisable, function(ele, idx) {
				// if the option to disable is currently selected, select the default option
				if (ele.is(":selected"))
				{
					ele.removeAttr("selected");
					colorMenu.find("option[value='uniform']").attr("selected", "selected");
					selectedColor = "uniform";
				}

				ele.attr("disabled", "disabled");
			});

			if (mut3dVis)
			{
				var opts = {};

				opts.proteinScheme = selectedScheme;

				if (selectedColor)
				{
					opts.colorProteins = selectedColor;
				}

				mut3dVis.updateOptions(opts);

				// reapply view with new settings
				//mut3dVis.changeStyle(selectedScheme);
				mut3dVis.reapplyStyle();
			}
		});
	},
	/**
	 * Updates the 3D visualizer content for the given gene,
	 * pdb id, and chain.
	 *
	 * @param geneSymbol    hugo gene symbol
	 * @param pdbId         pdb id
	 * @param chain         PdbChainModel instance
	 */
	updateView: function(geneSymbol, pdbId, chain)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;
		var pdbProxy = self.options.pdbProxy;

		var mapCallback = function(positionMap) {
			// update position map of the chain
			chain.positionMap = positionMap;

			// reload the selected pdb and chain data
			self.refreshView(pdbId, chain);

			// store pdb id and chain for future reference
			self.pdbId = pdbId;
			self.chain = chain;
		};

		var infoCallback = function(pdbInfo) {
			var model = {pdbId: pdbId,
				chainId: chain.chainId,
				pdbInfo: "",
				molInfo: ""};

			if (pdbInfo && pdbInfo[pdbId])
			{
				var summary = PdbDataUtil.generatePdbInfoSummary(
					pdbInfo[pdbId], chain.chainId);

				model.pdbInfo = summary.title;
				model.molInfo = summary.molecule;
			}

			self.hideMainLoader();

			// init info view
			var infoView = new Mutation3dVisInfoView(
				{el: self.$el.find(".mutation-3d-info"), model: model});
			infoView.render();

			// update positionMap for the chain
			// (retrieve data only once)
			pdbProxy.getPositionMap(geneSymbol, chain, mapCallback);
		};

		self.showMainLoader();
		mut3dVis.show();
		pdbProxy.getPdbInfo(pdbId, infoCallback);
	},
	/**
	 * Refreshes (reloads) the 3D visualizer for the given
	 * pdb id and chain.
	 *
	 * If no pdb id and chain provided, then reloads with
	 * the last known pdb id and chain.
	 *
	 * @param pdbId     pdb id
	 * @param chain     PdbChainModel instance
	 */
	refreshView: function(pdbId, chain)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// hide warning messages
		self.hideResidueWarning();
		self.hideNoMapWarning();

		// helper function to show/hide mapping information
		var showMapInfo = function(mapped)
		{
			if (mapped.length == 0)
			{
				// show the warning text
				self.showNoMapWarning();
			}
			else
			{
				// TODO display exactly what is mapped?
//				var proxy = self.options.mutationProxy;
//				var types = [];
//
//				_.each(mapped, function(id, idx) {
//					var mutation = proxy.getMutationUtil().getMutationIdMap()[id];
//					types.push(mutation.mutationType);
//				});
//
//				types = _.unique(types);

				// hide the warning text
				self.hideNoMapWarning();
			}
		};

		// do not reload (just refresh) if no pdb id or chain is provided,
		// or the provided chain and the previous chain are the same
		if ((pdbId == null && chain == null) ||
		    (pdbId == self.pdbId && chain == self.chain))
		{
			// just refresh
			var mapped = mut3dVis.refresh();

			// update mapping info
			showMapInfo(mapped);

			// trigger corresponding event
			self.dispatcher.trigger(
				MutationDetailsEvents.VIEW_3D_STRUCTURE_RELOADED);
		}
		// reload the new pdb structure
		else
		{
			// show loader image
			self.showLoader();

			// set a short delay to allow loader image to appear
			setTimeout(function() {
				// reload the visualizer
				var mapped = mut3dVis.reload(pdbId, chain, function() {
					// hide the loader image after reload complete
					self.hideLoader();
					// trigger corresponding event
					self.dispatcher.trigger(
						MutationDetailsEvents.VIEW_3D_STRUCTURE_RELOADED);
				});
				// update mapping info if necessary
				showMapInfo(mapped);
			}, 50);
		}
	},
	/**
	 * Initializes the mutation color information as a tooltip
	 * for the corresponding checkbox.
	 */
	_initMutationColorInfo: function()
	{
		var self = this;

		var info = self.$el.find(".mutation-type-color-help");

		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_3d_type_color_tip_template");
		var content = templateFn({});
		var options = self._generateTooltipOpts(content);

		// make it wider
		options.style.classes += " qtip-wide";

		info.qtip(options);
	},
	/**
	 * Initializes the protein structure color information as a tooltip
	 * for the corresponding selection menu.
	 */
	_initProteinColorInfo: function()
	{
		var self = this;

		var info = self.$el.find(".protein-struct-color-help");

		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_3d_structure_color_tip_template");
		var content = templateFn({});
		var options = self._generateTooltipOpts(content);

		// make it wider
		options.style.classes += " qtip-wide";

		info.qtip(options);
	},
	/**
	 * Initializes the side chain information as a tooltip
	 * for the corresponding checkbox.
	 */
	_initSideChainInfo: function()
	{
		var self = this;

		var info = self.$el.find(".display-side-chain-help");

		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_3d_side_chain_tip_template");
		var content = templateFn({});

		var options = self._generateTooltipOpts(content);
		info.qtip(options);
	},
	/**
	 * Initializes the side chain information as a tooltip
	 * for the corresponding checkbox.
	 */
	_initHideNonProteinInfo: function()
	{
		var self = this;

		var info = self.$el.find(".display-non-protein-help");

		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_3d_non_protein_tip_template");
		var content = templateFn({});

		var options = self._generateTooltipOpts(content);
		info.qtip(options);
	},
	/**
	 * Generates the default tooltip (qTip) options for the given
	 * tooltip content.
	 *
	 * @param content  actual tooltip content
	 * @return {Object}    qTip options for the given content
	 */
	_generateTooltipOpts: function(content)
	{
		return {content: {text: content},
			hide: {fixed: true, delay: 100, event: 'mouseout'},
			show: {event: 'mouseover'},
			style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
			position: {my:'top right', at:'bottom center', viewport: $(window)}};
	},
	/**
	 * Minimizes the 3D visualizer panel.
	 */
	minimizeView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		if (mut3dVis)
		{
			mut3dVis.minimize();
		}
	},
	/**
	 * Restores the 3D visualizer panel to its full size.
	 */
	maximizeView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		if (mut3dVis)
		{
			mut3dVis.maximize();
		}
	},
	/**
	 * Resets the position of the 3D panel to its initial state.
	 */
	resetPanelPosition: function()
	{
		var self = this;
		var container3d = self.$el;

		container3d.css({"left": "", position: "", "top": self.options.config.border.top});
	},
	/**
	 * Hides the 3D visualizer panel.
	 */
	hideView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// hide the vis pane
		if (mut3dVis != null)
		{
			mut3dVis.hide();
		}

		// trigger corresponding event
		self.dispatcher.trigger(
			MutationDetailsEvents.VIEW_3D_PANEL_CLOSED);
	},
	/**
	 * Shows the 3D visualizer panel.
	 */
	showView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// hide the vis pane
		if (mut3dVis != null)
		{
			mut3dVis.show();
		}
	},
	isVisible: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		return mut3dVis.isVisible();
	},
	/**
	 * Focuses the 3D visualizer on the residue
	 * corresponding to the given pileup of mutations.
	 *
	 * If this function is invoked without a parameter,
	 * then resets the focus to the default state.
	 *
	 * @param pileup    Pileup instance
	 * @return {boolean} true if focus successful, false otherwise
	 */
	focusView: function(pileup)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		if (pileup)
		{
			return mut3dVis.focusOn(pileup);
		}
		else
		{
			mut3dVis.resetFocus();
			return true;
		}
	},
	/**
	 * Highlights the 3D visualizer for the residue
	 * corresponding to the given array of pileups of mutations.
	 *
	 * @param pileups   an array of Pileup instances
	 * @param reset     whether to reset previous highlights
	 * @return {Number} number of mapped residues
	 */
	highlightView: function(pileups, reset)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		return mut3dVis.highlight(pileups, reset);
	},
	/**
	 * Resets all residue highlights.
	 */
	resetHighlight: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		mut3dVis.resetHighlight();
	},
	/**
	 * Shows the loader image for the 3D vis container.
	 */
	showLoader: function()
	{
		var self = this;
		var loaderImage = self.$el.find(".mutation-3d-vis-loader");
		var container = self.$el.find(".mutation-3d-vis-container");

		// hide actual vis container
		// (jQuery.hide function is problematic with 3D visualizer,
		// instead we are changing height)
		var height = container.css("height");

		if (!(height === 0 || height === "0px"))
		{
			self._actualHeight = height;
			container.css("height", 0);
		}

		// show image
		loaderImage.show();
	},
	/**
	 * Hides the loader image and shows the actual 3D visualizer content.
	 */
	hideLoader: function()
	{
		var self = this;
		var loaderImage = self.$el.find(".mutation-3d-vis-loader");
		var container = self.$el.find(".mutation-3d-vis-container");

		// hide image
		loaderImage.hide();

		// show actual vis container
		container.css("height", self._actualHeight);
	},
	/**
	 * Shows the loader for the entire panel body.
	 */
	showMainLoader: function()
	{
		var self = this;
		var loaderImage = self.$el.find(".mutation-3d-vis-main-loader");
		var mainContent = self.$el.find(".mutation-3d-vis-body");

		// show the image
		loaderImage.show();

		// hide the main body
		mainContent.hide();
	},
	/**
	 * Hides the loader image and shows the main content (panel body).
	 */
	hideMainLoader: function()
	{
		var self = this;
		var loaderImage = self.$el.find(".mutation-3d-vis-main-loader");
		var mainContent = self.$el.find(".mutation-3d-vis-body");

		// show the image
		loaderImage.hide();

		// hide the main body
		mainContent.show();
	},
	/**
	 * Shows a warning message for unmapped residues.
	 *
	 * @param unmappedCount  number of unmapped selections
	 * @param selectCount    total number of selections
	 */
	showResidueWarning: function(unmappedCount, selectCount)
	{
		var self = this;
		var warning = self.$el.find(".mutation-3d-residue-warning");
		var unmapped = self.$el.find(".mutation-3d-unmapped-info");

		// show warning only if no other warning is visible
		if (!self.$el.find(".mutation-3d-nomap-warning").is(":visible"))
		{
			if (selectCount > 1)
			{
				unmapped.text(unmappedCount + " of the selections");
			}
			else
			{
				unmapped.text("Selected mutation");
			}

			warning.show();
		}
	},
	/**
	 * Hides the residue warning message.
	 */
	hideResidueWarning: function()
	{
		var self = this;
		var warning = self.$el.find(".mutation-3d-residue-warning");

		warning.hide();
	},
	/**
	 * Shows a warning message for unmapped residues.
	 */
	showNoMapWarning: function()
	{
		var self = this;
		var warning = self.$el.find(".mutation-3d-nomap-warning");

		warning.show();
	},
	/**
	 * Hides the residue warning message.
	 */
	hideNoMapWarning: function()
	{
		var self = this;
		var warning = self.$el.find(".mutation-3d-nomap-warning");

		warning.hide();
	}
});
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Mutation Details Customization Panel View.
 *
 * This view is designed to provide a customization panel for Mutation Details page.
 *
 * options: {el: [target container],
 *           model: {},
 *           diagram: reference to the MutationDiagram instance
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationCustomizePanelView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};
	},
	render: function()
	{
		var self = this;
		var diagram = self.options.diagram;

		// template vars
		var variables = {minY: 2,
			maxY: diagram.getInitialMaxY()};

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_customize_panel_template");
		var template = templateFn(variables);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;
		var diagram = self.options.diagram;

		// hide the view initially
		self.$el.hide();

		// format panel controls

		var customizeClose = self.$el.find(".diagram-customize-close");
		var yAxisSlider = self.$el.find(".diagram-y-axis-slider");
		var yAxisInput = self.$el.find(".diagram-y-axis-limit-input");

		// add listener to close button
		customizeClose.click(function(event) {
			event.preventDefault();
			self.toggleView();
		});

		// set initial value of the input field
		yAxisInput.val(diagram.getMaxY());

		// init y-axis slider controls
		yAxisSlider.slider({
			value: diagram.getMaxY(), // set value to current max
			min: 2, // anything below 2 doesn't make much sense
			max: diagram.getInitialMaxY(), // set max value to initial max
			change: function(event, ui) {
				var value = ui.value;

				// adjust the slider value to the threshold
				// and stop execution, because this will trigger
				// this event (change event) again...
				if (value > diagram.getThreshold()) {
					value = diagram.getThreshold();
					$(this).slider('value', value);
					return;
				}

				// update input field
				yAxisInput.val(value);

				// update diagram
				diagram.updateOptions({maxLengthY: value});
				diagram.rescaleYAxis();
			},
			slide: function(event, ui) {
				// update input field only
				yAxisInput.val(ui.value);
			}
		});

		yAxisInput.keypress(function(event) {
			var enterCode = 13;

			if (event.keyCode == enterCode)
			{
				var input = yAxisInput.val();
				var value = input;

				// not a valid value, update with defaults
				if (isNaN(value) ||
				    value > diagram.getThreshold())
				{
					value = diagram.getThreshold();
				}
				else if (input < 2)
				{
					value = 2;
				}

				// update weight slider and input value
				yAxisInput.val(value);
				yAxisSlider.slider("option", "value", Math.floor(value));
			}
		});
	},
	toggleView: function() {
		var self = this;
		self.$el.slideToggle();
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Default table view for the mutations.
 *
 * options: {el: [target container],
 *           model: {mutations: mutation data as an array of JSON objects,
 *                   dataProxies: all available data proxies,
 *                   dataManager: global mutation data manager
 *                   geneSymbol: hugo gene symbol as a string,
 *                   tableOpts: mutation table options (optional)}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsTableView = Backbone.View.extend({
	initialize : function (options) {
		var defaultOpts = {
			config: {
				loaderImage: "images/ajax-loader.gif"
			}
		};

		this.options = jQuery.extend(true, {}, defaultOpts, options);

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_details_table_template");
		// TODO customize loader image
		var template = templateFn({loaderImage: self.options.config.loaderImage});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// init mutation table
		self._initMutationTable();

		// format after rendering
		self.format();
	},
	/**
	 * Initializes the PDB chain table.
	 *
	 * @return {MutationDetailsTable}   table instance
	 */
	_initMutationTable: function(callback)
	{
		var self = this;

		var options = jQuery.extend(true, {}, self.model.tableOpts);
		options.el = options.el || self.$el.find(".mutation_details_table");

		var mutationColl = new MutationCollection(self.model.mutations);
		var mutationUtil = new MutationDetailsUtil(mutationColl);

		var table = new MutationDetailsTable(
			options,
			self.model.geneSymbol,
			mutationUtil,
			self.model.dataProxies,
			self.model.dataManager);

		self.mutationTable = table;

		if (_.isFunction(callback))
		{
			callback(self, table);
		}

		self._generateRowData(table, table.getColumnOptions(), mutationColl, function(rowData) {
			// init table with the row data
			table.renderTable(rowData);
			// hide loader image
			//self.$el.find(".mutation-details-table-loader").hide();
		});

		return table;
	},
	_generateRowData: function(table, headers, mutationColl, callback)
	{
		var rows = [];

		mutationColl.each(function(mutation) {
			// only set the datum
			var datum = {
				table: table, // reference to the actual table instance
				mutation: mutation // actual mutation corresponding to the row
			};
			var row = [datum];

			// set everything else to null...
			for (var i=0; i < _.size(headers) - 1; i++)
			{
				row.push(null);
			}

			rows.push(row);
		});

		callback(rows);
	},
	format: function()
	{
		// TODO format table if required
	},
	hideView: function()
	{
		var self = this;
		self.$el.hide();
	},
	showView: function()
	{
		var self = this;
		self.$el.show();
	},
	/**
	 * Highlights the given mutations in the table.
	 *
	 * @param mutations mutations to highlight
	 */
	highlight: function(mutations)
	{
		var self = this;
		var tableSelector = self.$el.find('.mutation_details_table');

		for (var i = 0; i < mutations.length; i++)
		{
			//var row = tableSelector.find("#" + mutations[i].mutationId);
            var row = tableSelector.find("tr." + mutations[i].get("mutationSid"));
            row.addClass("mutation-table-highlight");
		}
	},
	/**
	 * Clears all highlights from the mutation table.
	 */
	clearHighlights: function()
	{
		var self = this;
		var tableSelector = self.$el.find('.mutation_details_table');

		// TODO this depends on highlight function
		tableSelector.find('tr').removeClass("mutation-table-highlight");
	},
	/**
	 * Filters out all other mutations than the given mutations.
	 *
	 * @param mutations mutations to keep
	 * @param updateBox [optional] show the filter text in the search box
	 * @param limit     [optional] column to limit filtering to
	 */
	filter: function(mutations, updateBox, limit)
	{
		var self = this;
		var oTable = self.mutationTable.getDataTable();

		// construct regex
		var ids = [];

		for (var i = 0; i < mutations.length; i++)
		{
			ids.push(mutations[i].get("mutationSid"));
		}

		var regex = "(" + ids.join("|") + ")";
		var asRegex = true;

		// empty mutation list, just show everything
		if (ids.length == 0)
		{
			regex = "";
			asRegex = false;
		}

		// disable event triggering before filtering, otherwise it creates a chain reaction
		self.mutationTable.setFilterEventActive(false);

		// apply filter
		self._applyFilter(oTable, regex, asRegex, updateBox, limit);

		// enable events after filtering
		self.mutationTable.setFilterEventActive(true);
	},
	/**
	 * Resets all table filters (rolls back to initial state)
	 */
	resetFilters: function()
	{
		var self = this;
		// pass an empty array to show everything
		self.filter([], true);
		// also clean filter related variables
		self.mutationTable.cleanFilters();
	},
	/**
	 * Rolls back the table to the last state where a manual search
	 * (manual filtering) performed. This function is required since
	 * we also filter the table programmatically.
	 */
	rollBack: function()
	{
		var self = this;
		var oTable = self.mutationTable.getDataTable();

		// disable event triggering before filtering, otherwise it creates a chain reaction
		self.mutationTable.setFilterEventActive(false);

		// re-apply last manual filter string
		var searchStr = self.mutationTable.getManualSearch();
		self._applyFilter(oTable, searchStr, false);

		// enable events after filtering
		self.mutationTable.setFilterEventActive(true);
	},
	clearSearchBox: function() {
		var self = this;
		var searchBox = self.$el.find(".mutation_datatables_filter input[type=search]");
		searchBox.val("");
	},
	/**
	 * Filters the given data table with the provided filter string.
	 *
	 * @param oTable    target data table to be filtered
	 * @param filterStr filter string to apply with the filter
	 * @param asRegex   indicates if the given filterStr is a regex or not
	 * @param updateBox [optional] show the filter text in the search box
	 * @param limit     [optional] column to limit filtering to
	 * @private
	 */
	_applyFilter: function(oTable, filterStr, asRegex, updateBox, limit)
	{
		var self = this;

		if (limit == undefined)
		{
			limit = null;
		}

		// TODO not updating the filter text in the box may be confusing
		if (updateBox == undefined)
		{
			updateBox = false;
		}

		var smartFilter = true;
		var caseInsensitive = true;

		var searchBox = self.$el.find(".mutation_datatables_filter input[type=search]");
		var prevValue = searchBox.val();

		oTable.fnFilter(filterStr, limit, asRegex, smartFilter, updateBox, caseInsensitive);

		// reset to previous value if updateBox is set to false
		if (!updateBox)
		{
			searchBox.val(prevValue);
		}
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Default mutation details view for the entire mutation details tab.
 * Creates a separate MainMutationView (another Backbone view) for each gene.
 *
 * options: {el: [target container],
 *           model: {mutationProxy: [mutation data proxy]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsView = Backbone.View.extend({
	initialize : function (options) {
		var defaultOpts = {
			config: {
				loaderImage: "images/ajax-loader.gif",
				coreTemplate: "default_mutation_details_template",
				mainContentTemplate: "default_mutation_details_main_content_template",
				listContentTemplate: "default_mutation_details_list_content_template"
			}
		};

		this.options = jQuery.extend(true, {}, defaultOpts, options);

		this._3dPanelInitialized = false;

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function() {
		var self = this;

		var content = self._generateContent();

		var variables = {loaderImage: self.options.config.loaderImage,
			listContent: content.listContent,
			mainContent: content.mainContent};

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn(self.options.config.coreTemplate);
		var template = templateFn(variables);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		if (self.model.mutationProxy.hasData())
		{
			if (_.isFunction(self.options.config.init))
			{
				self.options.config.init(self);
			}
			else
			{
				// init default view, if no custom init function is provided
				self._initDefaultView();
			}
		}

		// format after render

		if (self.options.config.format)
		{
			self.options.config.format(self);
		}
		else
		{
			self.format();
		}
	},
	/**
	 * Formats the contents of the view after the initial rendering.
	 */
	format: function()
	{
		var self = this;

		if (self.model.mutationProxy.hasData())
		{
			var mainContent = self.$el.find(".mutation-details-content");
			mainContent.tabs();
			mainContent.tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
			mainContent.tabs("option", "active", 0);
			self.$el.find(".mutation-details-tabs-ref").tipTip(
				{defaultPosition: "bottom", delay:"100", edgeOffset: 10, maxWidth: 200});
		}
	},
	/**
	 * Refreshes the genes tab.
	 * (Intended to fix a resize problem with ui.tabs.paging plugin)
	 */
	refreshGenesTab: function()
	{
		// tabs("refresh") is problematic...
//		var self = this;
//		var mainContent = self.$el.find(".mutation-details-content");
//		mainContent.tabs("refresh");

        // just trigger the window resize event,
        // rest is handled by the resize handler in ui.tabs.paging plugin.
		// it would be better to directly call the resize handler of the plugin,
		// but the function doesn't have public access...
		$(window).trigger('resize');
	},
	init3dPanel: function()
	{
		var self = this;

		self.dispatcher.trigger(
			MutationDetailsEvents.VIS_3D_PANEL_INIT);

		self._3dPanelInitialized = true;
	},
	is3dPanelInitialized: function()
	{
		var self = this;

		return self._3dPanelInitialized;
	},
	/**
	 * Generates the content structure by creating div elements for each
	 * gene.
	 *
	 * @return {Object} content backbone with div elements for each gene
	 */
	_generateContent: function()
	{
		var self = this;
		var mainContent = "";
		var listContent = "";

		// create a div for for each gene
		_.each(self.model.mutationProxy.getGeneList(), function(gene, idx) {
			var templateFn = BackboneTemplateCache.getTemplateFn(self.options.config.mainContentTemplate);

			mainContent += templateFn(
					{loaderImage: self.options.config.loaderImage,
						geneSymbol: gene,
						geneId: cbio.util.safeProperty(gene)});

			templateFn = BackboneTemplateCache.getTemplateFn(self.options.config.listContentTemplate);

			listContent += templateFn(
				{geneSymbol: gene,
					geneId: cbio.util.safeProperty(gene)});
		});

		return {mainContent: mainContent,
			listContent: listContent};
	},
	/**
	 * Initializes the mutation view for the current mutation data.
	 * Use this function if you want to have a default view of mutation
	 * details composed of different backbone views (by default params).
	 *
	 * If you want to have more customized components, it is better
	 * to initialize all the component separately.
	 */
	_initDefaultView: function()
	{
		var self = this;

		var contentSelector = self.$el.find(".mutation-details-content");

		// reset all previous tabs related listeners (if any)
		contentSelector.bind('tabscreate', false);
		contentSelector.bind('tabsactivate', false);

		// init view for the first gene only
		contentSelector.bind('tabscreate', function(event, ui) {
			// hide loader image
			self.$el.find(".mutation-details-loader").hide();

			// trigger corresponding event
			self.dispatcher.trigger(
				MutationDetailsEvents.GENE_TABS_CREATED);
		});

		// init other views upon selecting the corresponding tab
		contentSelector.bind('tabsactivate', function(event, ui) {
			// note: ui.index is replaced with ui.newTab.index() after jQuery 1.9
			//var gene = genes[ui.newTab.index()];

			// using index() causes problems with ui.tabs.paging plugin,
			// get the gene name directly from the html content
			var gene = ui.newTab.text().trim();

			// trigger corresponding event
			self.dispatcher.trigger(
				MutationDetailsEvents.GENE_TAB_SELECTED,
				gene);
		});
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Mutation Diagram view.
 *
 * options: {el: [target container],
 *           model: {mutations: [mutation data as an array of JSON objects],
 *                   sequence: [sequence data as an array of JSON objects],
 *                   geneSymbol: [hugo gene symbol as a string],
 *                   dataProxies: all available data proxies,
 *                   diagramOpts: [mutation diagram options -- optional]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationDiagramView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;

		// pass variables in using Underscore.js template
		var variables = {geneSymbol: self.model.geneSymbol,
			uniprotId: self.model.sequence.metadata.identifier};

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_diagram_view_template");
		var template = templateFn(variables);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// init the actual diagram component
		self.mutationDiagram = self._initMutationDiagram(
			self.model.geneSymbol,
			self.model.mutations,
			self.model.sequence,
			self.model.dataProxies,
			self.model.diagramOpts);

		self.format();
	},
	/**
	 * Formats the contents of the view after the initial rendering.
	 */
	format: function()
	{
		var self = this;

		// hide the toolbar & customization panel by default
		self.$el.find(".mutation-diagram-toolbar").hide();
		self.$el.find(".mutation-diagram-customize").hide();
		self.$el.find(".mutation-diagram-help").hide();
		self.$el.find(".mutation-diagram-toolbar-buttons").css("visibility", "hidden");

		// init toolbar if the diagram is initialized successfully
		if (self.mutationDiagram)
		{
			// init diagram toolbar
			self._initToolbar(self.mutationDiagram,
			                  self.model.geneSymbol);
		}
	},
	/**
	 * Initializes the mutation diagram view.
	 *
	 * @param gene          hugo gene symbol
	 * @param mutationData  mutation data (array of JSON objects)
	 * @param sequenceData  sequence data (as a JSON object)
	 * @param dataProxies   all available data proxies
	 * @param options       [optional] diagram options
	 * @return {Object}     initialized mutation diagram view
	 */
	_initMutationDiagram: function (gene, mutationData, sequenceData, dataProxies, options)
	{
		var self = this;

		// use defaults if no options provided
		if (!options)
		{
			options = {};
		}

		// do not draw the diagram if there is a critical error with
		// the sequence data
		if (sequenceData["length"] == "" ||
		    parseInt(sequenceData["length"]) <= 0)
		{
			// return null to indicate an error
			return null;
		}

		// overwrite container in any case (for consistency with the default view)
		options.el = self.$el.find(".mutation-diagram-container");

		// create a backbone collection for the given data
		var mutationColl = new MutationCollection(mutationData);

		// create a data object
		var diagramData = {
			mutations: mutationColl,
			sequence: sequenceData
		};

		var mutationDiagram = new MutationDiagram(gene, options, diagramData, dataProxies);

		// if no sequence data is provided, try to get it from the servlet
		if (sequenceData == null)
		{
			// TODO use PfamDataProxy instance!!
			$.getJSON("getPfamSequence.json",
			{geneSymbol: self.geneSymbol},
				function(data) {
					if (data)
					{
						mutationDiagram.updateSequenceData(data[0]);
					}

					mutationDiagram.initDiagram();
			});
		}
		// if data is already there just init the diagram
		else
		{
			mutationDiagram.initDiagram();
		}

		return mutationDiagram;
	},
	/**
	 * Initializes the toolbar over the mutation diagram.
	 *
	 * @param diagram       the mutation diagram instance
	 * @param geneSymbol    gene symbol as a string
	 */
	_initToolbar: function(diagram, geneSymbol) {
		var self = this;

		var toolbar = self.$el.find(".mutation-diagram-toolbar");
		var pdfButton = self.$el.find(".diagram-to-pdf");
		var svgButton = self.$el.find(".diagram-to-svg");
		var customizeButton = self.$el.find(".diagram-customize");
		var helpButton = self.$el.find(".diagram-help");

		// helper function to trigger submit event for the svg and pdf button clicks
		var submitForm = function(alterFn, diagram, type)
		{
			var filename = "mutation_diagram_" + geneSymbol + "." + type;

			// alter diagram to have the desired output
			alterFn(diagram, false);

			if (type == "svg")
			{
				cbio.download.initDownload(diagram.svg[0][0], {filename: filename});
			}
			else if (type == "pdf")
			{
				var params = {filename: filename,
					contentType: "application/pdf",
					servletName: "svgtopdf.do"
				};

				cbio.download.initDownload(diagram.svg[0][0], params);
			}

			// restore previous settings after generating xml string
			alterFn(diagram, true);

//			// set actual value of the form element (svgelement)
//			var form = self.$el.find("." + formClass);
//			form.find('input[name="svgelement"]').val(svgString);
//
//			// submit form
//			form.submit();
		};

		// helper function to adjust SVG for file output
		var alterDiagramForSvg = function(diagram, rollback)
		{
			var topLabel = geneSymbol;

			if (rollback)
			{
				topLabel = "";
			}

			// adding a top left label (to include a label in the file)
			diagram.updateTopLabel(topLabel);
		};

		// helper function to adjust SVG for PDF output
		var alterDiagramForPdf = function(diagram, rollback)
		{
			// we also need the same changes (top label) in pdf
			alterDiagramForSvg(diagram, rollback);
		};

		//add listener to the svg button
		svgButton.click(function (event) {
			// submit svg form
			//submitForm(alterDiagramForSvg, diagram, "svg-to-file-form");
			submitForm(alterDiagramForSvg, diagram, "svg");
		});

		// add listener to the pdf button
		pdfButton.click(function (event) {
			// submit pdf form
			//submitForm(alterDiagramForPdf, diagram, "svg-to-pdf-form");
			submitForm(alterDiagramForPdf, diagram, "pdf");
		});

		// add listeners to customize button
		customizeButton.click(function(event) {
			var panel = self.customizePanelView;

			// init view if not init yet
			if (!panel)
			{
				panel = new MutationCustomizePanelView({
					el: self.$el.find(".mutation-diagram-customize"),
					diagram: diagram});
				panel.render();

				self.customizePanelView = panel;
			}

			// toggle view
			panel.toggleView();
		});

		// add listeners to customize button
		helpButton.click(function(event) {
			var panel = self.helpPanelView;

			// init view if not init yet
			if (!panel)
			{
				panel = new MutationHelpPanelView({
					el: self.$el.find(".mutation-diagram-help")});
				panel.render();

				self.helpPanelView = panel;
			}

			// toggle view
			panel.toggleView();
		});

		// hide buttons initially, show on mouse over
		self._autoHideToolbarButtons();

		toolbar.show();
	},
	/**
	 * Shows the toolbar buttons only on mouse over.
	 * And hides them on mouse out.
	 */
	_autoHideToolbarButtons: function()
	{
		var self = this;
		var buttons = self.$el.find(".mutation-diagram-toolbar-buttons");

		cbio.util.autoHideOnMouseLeave(self.$el, buttons);
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Mutation Details Legend Panel View.
 *
 * This view is designed to provide a legend panel for Mutation Details page.
 *
 * options: {el: [target container],
 *           model: {},
 *           diagram: reference to the MutationDiagram instance
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationHelpPanelView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};
	},
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_help_panel_template");
		var template = templateFn({});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;

		// format panel controls
		var helpClose = self.$el.find(".diagram-help-close");

		// add listener to close button
		helpClose.click(function(event) {
			event.preventDefault();
			self.toggleView();
		});
	},
	toggleView: function() {
		var self = this;
		self.$el.slideToggle();
	}
});

/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Mutation Details Customization Panel View.
 *
 * This view is designed to provide a customization panel for Mutation Details page.
 *
 * options: {el: [target container],
 *           model: {},
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationInfoPanelView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);

		// initial count by type map
		//this.initialMapByType = this._mapMutationsByType(this.model.mutations);
		this.initialMapByType = this._mapMutationsByMainType(this.model.mutations);
		//this.selectionMap = this.resetSelectionMap();
	},
	render: function()
	{
		var self = this;
		self.updateView(self.model.mutations);
	},
	format: function()
	{
		var self = this;

		self.$el.find(".mutation-type-info-link").on('click', function(evt) {
			evt.preventDefault();
			var mutationType = $(this).attr("alt");

			//if (self.selectionMap[mutationType] != null)
			//{
			//	self.selectionMap[mutationType] += 1;
			//}

			self.dispatcher.trigger(
				MutationDetailsEvents.INFO_PANEL_MUTATION_TYPE_SELECTED,
				mutationType);
		});
	},
	updateView: function(mutations) {
		var self = this;
		//self.currentMapByType = self._mapMutationsByType(mutations);
		self.currentMapByType = self._mapMutationsByMainType(mutations);
		var countByType = self._countMutationsByType(self.currentMapByType);
		var mutationTypeStyle = MutationViewsUtil.getVisualStyleMaps().mutationType;
		var content = [];

		countByType = _.extend(self._generateZeroCountMap(self.initialMapByType), countByType);

		// sort mutation types by priority
		var keys = _.keys(countByType).sort(function(a, b) {
			var priorityA = 1024;
			var priorityB = 1024;

			if (mutationTypeStyle[a] && mutationTypeStyle[a].priority) {
				priorityA = mutationTypeStyle[a].priority;
			}

			if (mutationTypeStyle[b] && mutationTypeStyle[b].priority) {
				priorityB = mutationTypeStyle[b].priority;
			}

			return priorityA - priorityB;
		});

		_.each(keys, function(mutationType) {
			var templateFn = BackboneTemplateCache.getTemplateFn("mutation_info_panel_type_template");

			var text = "Other";
			var textStyle = mutationTypeStyle["other"].style;

			var view = mutationTypeStyle[mutationType];

			if (view && view.mainType)
			{
				view = mutationTypeStyle[view.mainType];
			}

			if (view)
			{
				text = view.longName || text;
				textStyle = view.style || textStyle;
			}

			var count = countByType[mutationType];

			var variables = {
				mutationType: mutationType,
				type: text,
				textStyle: textStyle,
				count: count,
				countStyle: textStyle + "_count"
			};

			var template = templateFn(variables);
			content.push(template);
		});

		// template vars
		var variables = {
			mutationTypeContent: content.join("\n")
		};

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_info_panel_template");
		var template = templateFn(variables);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	_generateZeroCountMap: function(mapByType) {
		var zeroCountMap = {};

		_.each(_.keys(mapByType), function (key) {
			zeroCountMap[key] = 0;
		});

		return zeroCountMap;
	},
	resetSelectionMap: function() {
		var self = this;

		self.selectionMap = self._generateZeroCountMap(self.initialMapByType);
	},
	// TODO move these into a utility class
	_mapMutationsByType: function(mutations) {
		return _.groupBy(mutations, function(mutation) {
			return mutation.get("mutationType").toLowerCase();
		});
	},
	_mapMutationsByMainType: function(mutations) {
		var mutationTypeStyle = MutationViewsUtil.getVisualStyleMaps().mutationType;

		return _.groupBy(mutations, function(mutation) {
			var type = mutation.get("mutationType");
			if (type) {
				type = type.toLowerCase();
			}
			else {
				type = "other";
			}

			var mainType;

			if (mutationTypeStyle[type]) {
				mainType = mutationTypeStyle[type].mainType;
			}
			else {
				mainType = "other";
			}

			return mainType;
		});
	},
	_countMutationsByType: function(mapByType) {
		var countByType = {};

		_.each(_.keys(mapByType), function(type) {
			countByType[type] = _.size(mapByType[type]);
		});

		return countByType;
	}
});


/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Mutation Details Customization Panel View.
 *
 * This view is designed to provide a customization panel for Mutation Details page.
 *
 * options: {el: [target container],
 *           model: {},
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationSummaryView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};

		// custom event dispatcher
		//this.dispatcher = {};
		//_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;
        var mutationSummary;

		self.model.clinicalProxy.getPatientData(self.model.sampleArray, function(data) {
			if (!data) {
				mutationSummary = self._mutationSummary();
			}
			else {
				mutationSummary = self._germlineMutationSummary(data);
			}

			var variables = {
				mutationSummary: mutationSummary,
				geneSymbol: self.model.geneSymbol
			};

			// compile the template using underscore
			var templateFn = BackboneTemplateCache.getTemplateFn('mutation_summary_view_template');
			var template = templateFn(variables);

			// load the compiled HTML into the Backbone "el"
			self.$el.html(template);

			self.format();
		});
	},
	format: function()
	{
		var self = this;
	},
	/**
	 * Generates a one-line summary of the mutation data.
	 *
	 * @return {string} summary string
	 */
	_mutationSummary: function()
	{
		var self = this;
		var mutationUtil = self.model.mutationProxy.getMutationUtil();
		var gene = self.model.geneSymbol;
		var cases = self.model.sampleArray;

		var summary = "";

		if (cases.length > 0)
		{
			// calculate somatic & germline mutation rates
			var mutationCount = mutationUtil.countMutations(gene, cases);
			// generate summary string for the calculated mutation count values
			summary = mutationUtil.generateSummary(mutationCount);
		}

		return summary;
	},
    _germlineMutationSummary: function(clinicalGermlineData) {
        var self = this;
        var mutationUtil = self.model.mutationProxy.getMutationUtil();
        var gene = self.model.geneSymbol;
        var cases = self.model.sampleArray;
        var numGermlineCases = 0;            
        var summary = "";
                    
        if(cases.length > 0) {
            var mutationCount = mutationUtil.countMutations(gene, cases);
                        
            for (var i = 0; i < clinicalGermlineData.length; i++) {
                var clinicalData = clinicalGermlineData[i];
                if (clinicalData.attr_val === "YES") {
                    numGermlineCases++;
                }
            }
                        
            mutationCount.numGermlineCases = numGermlineCases;
            summary = mutationUtil.generateSummary(mutationCount);
        }
                    
        return summary;
    }
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Tooltip view for the mutation table's cosmic column.
 *
 * options: {el: [target container],
 *           model: {pancanMutationFreq: [pancan mutation frequency map]
 *                   cancerStudyMetaData: [cancer study meta data],
 *                   cancerStudyName: [cancer study name],
 *                   geneSymbol: [hugo gene symbol],
 *                   keyword: [mutation keyword],
 *                   qtipApi: [api reference for the rendered qtip]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PancanMutationHistTipView = Backbone.View.extend({
	render: function()
	{
//		var gene = $thumbnail.attr('gene');
//		var keyword = $thumbnail.attr('keyword');
//		var metaData = window.cancer_study_meta_data;
//		var cancerStudy = window.cancerStudyName;
//		var byKeywordData = genomicEventObs.pancan_mutation_frequencies[keyword];
//		var byHugoData = genomicEventObs.pancan_mutation_frequencies[gene];

		var self = this;

		var variables = {
			gene: self.model.geneSymbol
		};

		// render view
		var templateFn = BackboneTemplateCache.getTemplateFn("pancan_mutation_hist_tip_template");
		var content = templateFn(variables);

		self.model.qtipApi.set('content.text', content);

		// format after rendering
		this.format();
	},
	format: function()
	{
		var self = this;

		var gene = self.model.geneSymbol;
		//var keyword = self.model.keyword;
		var proteinPosStart = self.model.proteinPosStart;
		var metaData = self.model.cancerStudyMetaData;
		var cancerStudy = self.model.cancerStudyName;

		//var byKeywordData = self.model.pancanMutationFreq[keyword];
		var byProteinPosData = self.model.pancanMutationFreq[proteinPosStart];
		var byHugoData = self.model.pancanMutationFreq[gene];

		var container = self.$el.find(".pancan-histogram-container");

		// init the histogram
		var histogram = PancanMutationHistogram(byProteinPosData,
		                                        byHugoData,
		                                        metaData,
		                                        container[0],
		                                        {this_cancer_study: cancerStudy});

		// update the overall count text
		self.$el.find(".overall-count").html(histogram.overallCountText());

		// correct the qtip width
		var svgWidth = $(container).find('svg').attr('width');
		self.$el.css('max-width', parseInt(svgWidth));

		// add histogram tooltips (inner tooltips)
		var svg = self.$el.find('svg')[0];
		histogram.qtip(svg);

		// add click functionality for the buttons
		$(".cross-cancer-download").click(function() {
			var fileType = $(this).attr("file-type");
			var filename = gene + "_mutations." + fileType;

			if (fileType == "pdf")
			{
				cbio.download.initDownload(svg, {
					filename: filename,
					contentType: "application/pdf",
					servletName: "svgtopdf.do"
				});
			}
			else // svg
			{
				cbio.download.initDownload(svg, {
					filename: filename
				});
			}
		});
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Tooltip view for the PDB panel chain rectangles.
 *
 * options: {el: [target container],
 *           model: {pdbId, chain, pdbInfo, molInfo}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PdbChainTipView = Backbone.View.extend({
	render: function()
	{
		// compile the template
		var template = this.compileTemplate();

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);
		this.format();
	},
	format: function()
	{
		// implement if necessary...
	},
	compileTemplate: function()
	{
		var self = this;
		var pdbInfo = self.model.pdbInfo;
		var molInfo = self.model.molInfo;

		// pass variables in using Underscore.js template
		var variables = {pdbId: self.model.pdbId,
			chainId: self.model.chain.chainId,
			pdbInfo: "",
			molInfo: ""};

		// TODO this can be implemented in a better way

		if (pdbInfo != null &&
		    pdbInfo.length > 0)
		{
			variables.pdbInfo = ": " + pdbInfo;
		}

		if (molInfo != null &&
		    molInfo.length > 0)
		{
			variables.molInfo = ": " + molInfo;
		}

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_details_pdb_chain_tip_template");
		return templateFn(variables);
	}
});
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * PDB Panel View.
 *
 * This view is designed to function in parallel with the 3D visualizer.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: hugo gene symbol,
 *                   pdbColl: collection of PdbModel instances,
 *                   pdbProxy: pdb data proxy,
 *                   pdbPanelOpts: MutationPdbPanel options,
 *                   pdbTableOpts: MutationPdbTable options},
 *           diagram: [optional] reference to the MutationDiagram instance
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PdbPanelView = Backbone.View.extend({
	initialize : function (options) {
		var defaultOpts = {
			config: {
				loaderImage: "images/ajax-loader.gif",
				autoExpand: true
			}
		};

		this.options = jQuery.extend(true, {}, defaultOpts, options);
		this.collapseTimer = null;
		this.expandTimer = null;
	},
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("pdb_panel_view_template");
		var template = templateFn({});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// init pdb panel
		self.pdbPanel = self._initPdbPanel();

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;

		// hide view initially
		self.$el.hide();

		// format panel controls
		var expandButton = self.$el.find(".expand-collapse-pdb-panel");
		var pdbTableInit = self.$el.find(".init-pdb-table");
		var pdbTableControls = self.$el.find(".pdb-table-controls");
		var triangleDown = self.$el.find(".triangle-down");
		var triangle = self.$el.find(".triangle");

		// format the expand button if there are more chains to show
		if (self.pdbPanel.hasMoreChains())
		{
			expandButton.button({
				icons: {primary: "ui-icon-triangle-2-n-s"},
				text: false});
			expandButton.css({width: "300px", height: "12px"});

			expandButton.click(function() {
				self.pdbPanel.toggleHeight();
			});
		}

		// initially hide controls
		expandButton.hide();
		pdbTableControls.hide();

		triangleDown.hide();

		// make triangles clickable
		triangle.click(function(event) {
			// same as clicking on the link
			pdbTableInit.click();
		});

		if (self.options.config.autoExpand)
		{
			self.$el.find(".mutation-pdb-main-container").mouseenter(function(evt) {
				self.autoExpand();
			});

			self.$el.find(".mutation-pdb-main-container").mouseleave(function(evt) {
				self.autoCollapse();
			});
		}
	},
	hideView: function()
	{
		var self = this;
		self.$el.slideUp();
	},
	showView: function()
	{
		var self = this;
		self.$el.slideDown();
	},
	initPdbTableView: function(pdbColl, callback)
	{
		var self = this;

		var tableOpts = {
			el: self.$el.find(".mutation-pdb-table-view"),
			config: {loaderImage: self.options.config.loaderImage},
			model: {geneSymbol: self.model.geneSymbol,
				pdbColl: pdbColl,
				pdbProxy: self.model.pdbProxy}
		};

		tableOpts = jQuery.extend(true, {}, self.model.pdbTableOpts, tableOpts);
		var pdbTableView = new PdbTableView(tableOpts);
		self.pdbTableView = pdbTableView;

		pdbTableView.render(callback);

		return pdbTableView;
	},
	/**
	 * Adds a callback function for the PDB table init button.
	 *
	 * @param callback  function to be invoked on click
	 */
	addInitCallback: function(callback) {
		var self = this;
		var pdbTableInit = self.$el.find(".init-pdb-table");

		// add listener to pdb table init button
		pdbTableInit.click(function(event) {
			event.preventDefault();
			callback(event);
		});
	},
	toggleTableControls: function()
	{
		var self = this;

		// just toggle triangle orientation
		self.$el.find(".triangle").toggle();
	},
	/**
	 * Selects the default pdb and chain for the 3D visualizer.
	 * Default chain is one of the chains in the first row.
	 */
	selectDefaultChain: function()
	{
		var self = this;
		var panel = self.pdbPanel;
		var gChain = panel.getDefaultChainGroup();

		// clear previous timers
		self.clearTimers();

		// restore chain positions
		panel.restoreChainPositions(function() {
			// highlight the default chain
			panel.highlight(gChain);
		});
	},
	/**
	 * Selects the given pdb and chain for the 3D visualizer.
	 *
	 * @param pdbId     pdb to be selected
	 * @param chainId   chain to be selected
	 */
	selectChain: function(pdbId, chainId)
	{
		var self = this;
		var panel = self.pdbPanel;

		// clear previous timers
		self.clearTimers();

		// restore to original positions & highlight the chain
		panel.restoreChainPositions(function() {
			// expand the panel up to the level of the given chain
			panel.expandToChainLevel(pdbId, chainId);

			// get the chain group
			var gChain = panel.getChainGroup(pdbId, chainId);

			// highlight the chain group
			if (gChain)
			{
				panel.highlight(gChain);
			}
		});
	},
	getSelectedChain: function()
	{
		var self = this;
		var panel = self.pdbPanel;

		return panel.getHighlighted();
	},
	/**
	 * Initializes the auto collapse process.
	 *
	 * @delay time to minimization
	 */
	autoCollapse: function(delay)
	{
		if (delay == null)
		{
			delay = 2000;
		}

		var self = this;
		var expandButton = self.$el.find(".expand-collapse-pdb-panel");
		var pdbTableControls = self.$el.find(".pdb-table-controls");
		var pdbTableWrapper = self.$el.find(".pdb-table-wrapper");

		// clear previous timers
		self.clearTimers();

		// set new timer
		self.collapseTimer = setTimeout(function() {
			self.pdbPanel.minimizeToHighlighted();
			expandButton.slideUp();
			pdbTableControls.slideUp();
			pdbTableWrapper.slideUp();
		}, delay);
	},
	/**
	 * Initializes the auto expand process.
	 *
	 * @delay time to minimization
	 */
	autoExpand: function(delay)
	{
		if (delay == null)
		{
			delay = 400;
		}

		var self = this;
		var expandButton = self.$el.find(".expand-collapse-pdb-panel");
		var pdbTableControls = self.$el.find(".pdb-table-controls");
		var pdbTableWrapper = self.$el.find(".pdb-table-wrapper");

		// clear previous timers
		self.clearTimers();

		// set new timer
		self.expandTimer = setTimeout(function() {
			self.pdbPanel.restoreToFull();

			if (self.pdbPanel.hasMoreChains())
			{
				expandButton.slideDown();
			}

			pdbTableControls.slideDown();
			pdbTableWrapper.slideDown();

			if (self.pdbTableView != null)
			{
				self.pdbTableView.refreshView();
			}
		}, delay);
	},
	/**
	 * Limits the size of the panel by the given max height value,
	 * and adds a scroll bar for the y-axis. If max height is not
	 * a valid value, then disables the scroll bar.
	 *
	 * @param maxHeight desired max height value
	 */
	toggleScrollBar: function(maxHeight)
	{
		var self = this;
		var container = self.$el.find(".mutation-pdb-panel-container");

		if (maxHeight > 0)
		{
			container.css("max-height", maxHeight);
			container.css("overflow", "");
			container.css("overflow-y", "scroll");
		}
		else
		{
			container.css("max-height", "");
			container.css("overflow-y", "");
			container.css("overflow", "hidden");
		}
	},
	/**
	 * Moves the scroll bar to the selected chain's position.
	 */
	scrollToSelected: function()
	{
		var self = this;
		var container = self.$el.find(".mutation-pdb-panel-container");

		// TODO make scroll parameters customizable?
		container.scrollTo($(".pdb-selection-rectangle-group"),
		                   {axis: 'y', duration: 800, offset: -150});
	},
	clearTimers: function()
	{
		var self = this;

		if (self.collapseTimer != null)
		{
			clearTimeout(self.collapseTimer);
		}

		if (self.expandTimer != null)
		{
			clearTimeout(self.expandTimer);
		}
	},
	/**
	 * Initializes the PDB chain panel.
	 *
	 * @return {MutationPdbPanel}   panel instance
	 */
	_initPdbPanel: function()
	{
		var self = this;

		var pdbColl = self.model.pdbColl;
		var pdbProxy = self.model.pdbProxy;
		var mutationDiagram = self.options.diagram;

		var options = {el: self.$el.find(".mutation-pdb-panel-container"),
				maxHeight: 200};
		var xScale = null;

		// if mutation diagram is enabled,
		// get certain values from mutation diagram for consistent rendering!
		if (mutationDiagram != null)
		{
			xScale = mutationDiagram.xScale;

			// set margin same as the diagram margin for correct alignment with x-axis

			options.marginLeft = mutationDiagram.options.marginLeft;
			options.marginRight = mutationDiagram.options.marginRight;
		}

		// init panel
		options = jQuery.extend(true, {}, self.model.pdbPanelOpts, options);
		var panel = new MutationPdbPanel(options, pdbColl, pdbProxy, xScale);
		panel.init();

		return panel;
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * PDB Table View.
 *
 * This view is designed to function in parallel with the 3D visualizer.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: hugo gene symbol,
 *                   pdbColl: collection of PdbModel instances,
 *                   pdbProxy: pdb data proxy,
 *                   tableOpts: pdb table options (optional)},
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PdbTableView = Backbone.View.extend({
	initialize : function (options) {
		var defaultOpts = {
			config: {
				loaderImage: "images/ajax-loader.gif"
			}
		};

		this.options = jQuery.extend(true, {}, defaultOpts, options);
	},
	render: function(callback)
	{
		var self = this;

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("pdb_table_view_template");
		var template = templateFn({loaderImage: self.options.config.loaderImage});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// init pdb table
		self._initPdbTable(callback);

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;

		// hide view initially
		self.$el.hide();
	},
	hideView: function()
	{
		var self = this;
		self.$el.slideUp();
	},
	showView: function()
	{
		var self = this;
		self.$el.slideDown();
	},
	toggleView: function()
	{
		var self = this;
		self.$el.slideToggle();
	},
	refreshView: function()
	{
		var self = this;
		self.pdbTable.getDataTable().fnAdjustColumnSizing();
	},
	/**
	 * Resets all table filters (rolls back to initial state)
	 */
	resetFilters: function()
	{
		var self = this;

		// TODO do not clean filters if not filtered
		self.pdbTable.cleanFilters();
	},
	selectChain: function(pdbId, chainId)
	{
		var self = this;

		if (self.pdbTable != null)
		{
			self.pdbTable.selectRow(pdbId, chainId);
		}
	},
	/**
	 * Moves the scroll bar to the selected chain's position.
	 */
	scrollToSelected: function()
	{
		var self = this;
		var selected = self.pdbTable.getSelectedRow();

		var container = self.$el.find(".dataTables_scrollBody");

		// TODO make scroll parameters customizable?
		container.scrollTo($(selected),
		                   {axis: 'y', duration: 800});
	},
	/**
	 * Initializes the PDB chain table.
	 *
	 * @return {MutationPdbTable}   table instance
	 */
	_initPdbTable: function(callback)
	{
		var self = this;

		var pdbColl = self.model.pdbColl;
		var pdbProxy = self.model.pdbProxy;

		var options = jQuery.extend(true, {}, self.model.tableOpts);
		options.el = options.el || self.$el.find(".pdb-chain-table");

		var table = new MutationPdbTable(options);
		self.pdbTable = table;

		if (_.isFunction(callback))
		{
			callback(self, table);
		}

		self._generateRowData(table.getColumnOptions(), pdbColl, pdbProxy, function(rowData) {
			// init table with the row data
			table.renderTable(rowData);
			// hide loader image
			self.$el.find(".pdb-chain-table-loader").hide();
		});

		return table;
	},
	_generateRowData: function(headers, pdbColl, pdbProxy, callback)
	{
		var rows = [];
		var pdbIds = [];

		pdbColl.each(function(pdb) {
			pdbIds.push(pdb.pdbId);
		});

		pdbProxy.getPdbInfo(pdbIds.join(" "), function(data) {
			pdbColl.each(function(pdb) {
				pdb.chains.each(function(chain) {
					// this is the data of the hidden column "datum"
					var datum = {
						pdbId: pdb.pdbId,
						chain: chain,
						organism: PdbDataUtil.getOrganism(data[pdb.pdbId], chain.chainId),
						summary: PdbDataUtil.generatePdbInfoSummary(data[pdb.pdbId], chain.chainId)
					};

					// only set the datum
					var row = [datum];

					// set everything else to null...
					for (var i=0; i < _.size(headers) - 1; i++)
					{
						row.push(null);
					}

					rows.push(row);
				})
			});

			callback(rows);
		});
	}
});


/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Tooltip view for the mutation table's FIS column.
 *
 * options: {el: [target container],
 *           model: {xvia: [link to Mutation Assessor],
 *                   impact: [impact text or value]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PredictedImpactTipView = Backbone.View.extend({
	render: function()
	{
		// compile the template
		var template = this.compileTemplate();

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);
		this.format();
	},
	format: function()
	{
		var isValidLink = function(url)
		{
			var valid = true;

			if (url == null || url == "NA" || url.length == 0)
			{
				valid = false;
			}

			return valid;
		};

		var xvia = this.model.xvia;

		if (!isValidLink(xvia))
		{
			this.$el.find(".mutation-assessor-main-link").hide();
		}

		var pdbLink = this.model.pdbLink;

		if (!isValidLink(pdbLink))
		{
			this.$el.find(".mutation-assessor-3d-link").hide();
		}

		var msaLink = this.model.msaLink;

		if (!isValidLink(msaLink))
		{
			this.$el.find(".mutation-assessor-msa-link").hide();
		}
	},
	compileTemplate: function()
	{
		// pass variables in using Underscore.js template
		var variables = {linkOut: this.model.xvia,
			msaLink: this.model.msaLink,
			pdbLink: this.model.pdbLink,
			impact: this.model.impact};

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_details_fis_tip_template");
		return templateFn(variables);
	}
});


/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Tooltip view for the mutation diagram's region rectangles.
 *
 * options: {el: [target container],
 *           model: {identifier: [region identifier],
 *                   type: [region type],
 *                   description: [region description],
 *                   start: [start position],
 *                   end: [end position]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var RegionTipView = Backbone.View.extend({
	render: function()
	{
		// compile the template
		var template = this.compileTemplate();

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);
		this.format();
	},
	format: function()
	{
		// implement if necessary...
	},
	compileTemplate: function()
	{
		// pass variables in using Underscore.js template
		var variables = {identifier: this.model.identifier,
			type: this.model.type.toLowerCase(),
			description: this.model.description,
			start: this.model.start,
			end: this.model.end,
			pfamAccession: this.model.pfamAccession,
			mutationAlignerInfo: this.model.mutationAlignerInfo};

		// compile the template using underscore
		var templateFn = BackboneTemplateCache.getTemplateFn("mutation_details_region_tip_template");
		return templateFn(variables);
	}
});

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Base class for data proxy instances.
 *
 * @author Selcuk Onur Sumer
 */
function AbstractDataProxy(options)
{
	var self = this;

	// default options
	self._defaultOpts = {
		initMode: "lazy", // "lazy" or "full"
		servletName: "",  // name of the servlet to retrieve the actual data (used for AJAX query)
		data: {}          // actual data, will be used only if it is a full init, i.e {initMode: "full"}
	};

	self._queryQueue = new RequestQueue();

	// merge options with default options to use defaults for missing values
	self._options = jQuery.extend(true, {}, self._defaultOpts, options);

	/**
	 * Initializes the data proxy with respect to init mode.
	 */
	self.init = function()
	{
		self._queryQueue.init(function(options) {
			$.ajax(options);
		});

		if (self.isFullInit())
		{
			self.fullInit(self._options);
		}
		else
		{
			self.lazyInit(self._options);
		}
	};

	/**
	 * Initializes the proxy without actually grabbing anything from the server.
	 * Provided servlet name will be used later.
	 *
	 * @param options   data proxy options
	 */
	self.lazyInit = function(options)
	{
		// no default implementation, can be overridden by subclasses
	};

	/**
	 * Initializes with full data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional data.
	 *
	 * @param options   data proxy options
	 */
	self.fullInit = function(options)
	{
		// method body should be overridden by subclasses
	};

	/**
	 * Checks if the initialization is full or lazy.
	 *
	 * @return {boolean} true if full init, false otherwise
	 */
	self.isFullInit = function()
	{
		return !(self._options.initMode.toLowerCase() === "lazy");
	};


	/**
	 * This function ensures that at most only one ajax request is
	 * sent from a particular DataProxy instance. This is to prevent
	 * too many simultaneous requests.
	 *
	 * @ajaxOptions jQuery ajax options
	 */
	self.requestData = function(ajaxOptions)
	{
		var complete = ajaxOptions.complete;

		var defaultOpts = {
			complete: function(request, status)
			{
				self._queryQueue.complete();

				if (_.isFunction(complete))
				{
					complete(request, status);
				}
			}
		};

		// extend options with default options
		var options = jQuery.extend(true, {}, ajaxOptions, defaultOpts);

		self._queryQueue.add(options);
	};
}

/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is designed to retrieve cBio Portal specific data on demand.
 *
 * @param options  additional options
 *
 * @author Selcuk Onur Sumer
 */
function ClinicalDataProxy(options)
{
	var self = this;

	// default options
	var _defaultOpts = {
		servletName: "api-legacy/clinicaldata",
		subService: {
			patients: "patients"
		}
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	// cache
	var _data = {};

	/**
	 * Initializes with full data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional data.
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
		_data = options.data;
	}

	function getPatientData(samples, callback)
	{
		// TODO full init & cache...

		var cancerStudyId;
		var patientSampleMap = {};
		var patientIds = [];
		var querySession = null;

		// TODO we need to find a better way to plug portal data into MutationMapper!
		// workaround: since QuerySession is actually live in cBioPortal
		// we need to make sure that it doesn't break the standalone MutationMapper instances
		try {
			querySession = window.QuerySession;
		} catch (e) {
			// undefined reference: QuerySession
		}

		if (querySession) {
			cancerStudyId = querySession.cancer_study_ids[0];
			querySession.getPatientSampleIdMap().then(function (patientSampleMap){
                for (var i = 0; i < samples.length; i++) {
                    patientIds.push(patientSampleMap[samples[i]]);
                }
                makePatientData();
            });
		}
		else {
			cancerStudyId = window.cancer_study_id;
            makePatientData();
		}

        function makePatientData() {
            // no cancer study id or patient information...
		    if (!cancerStudyId || _.size(patientIds) === 0)
		    {
			    callback(null);
			    return;
		    }

		    var args = {study_id:cancerStudyId, attribute_ids:["12_245_PARTC_CONSENTED"], patient_ids:patientIds};
		    var arg_strings = [];
		    for (var k in args) {
			    if (args.hasOwnProperty(k)) {
			        arg_strings.push(k + '=' + [].concat(args[k]).join(","));
			    }
		    }

		    var arg_string = arg_strings.join("&") || "?";

		    var ajaxOpts = {
			    type: "POST",
			    url: _options.servletName + "/" + _options.subService.patients,
			    data: arg_string,
			    dataType: "json",
			    success: function(data) {
				    callback(data);
			    },
			    error: function(data) {
				    callback(null);
			    }
		    };

		    self.requestData(ajaxOpts);
        }
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.getPatientData = getPatientData;
}

// ClinicalDataProxy extends AbstractDataProxy...
ClinicalDataProxy.prototype = new AbstractDataProxy();
ClinicalDataProxy.prototype.constructor = ClinicalDataProxy;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is designed to retrieve Mutation Aligner data on demand.
 *
 * @param options  additional options
 *
 * @author Selcuk Onur Sumer
 */
function MutationAlignerDataProxy(options)
{
	var self = this;

	// default options
	var _defaultOpts = {
		servletName: "getMutationAligner.json"
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	// map of <gene, data> pairs
	var _maDataCache = {};

	/**
	 * Initializes with full PFAM data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional data.
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
		//assuming the given data is a map of <gene, sequence data> pairs
		_maDataCache = options.data;
	}

	function getMutationAlignerData(servletParams, callback)
	{
		// TODO allow more than one accession at a time? (see MutationDataProxy)
		var pfamAccession = servletParams.pfamAccession;

		if (pfamAccession == null)
		{
			// no gene symbol provided, nothing to retrieve
			callback(null);
			return;
		}

		// retrieve data from the server if not cached
		if (_maDataCache[pfamAccession] == undefined)
		{
			if (self.isFullInit())
			{
				callback(null);
				return;
			}

			// process & cache the raw data
			var processData = function(data) {
				_maDataCache[pfamAccession] = data;

				// forward the processed data to the provided callback function
				callback(data);
			};

			// retrieve data from the servlet
			var ajaxOpts = {
				type: "POST",
				url: _options.servletName,
				data: servletParams,
				success: processData,
				dataType: "json"
			};

			self.requestData(ajaxOpts);
		}
		else
		{
			// data is already cached, just forward it
			callback(_maDataCache[pfamAccession]);
		}
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.getMutationAlignerData = getMutationAlignerData;
}

// MutationAlignerDataProxy extends AbstractDataProxy...
MutationAlignerDataProxy.prototype = new AbstractDataProxy();
MutationAlignerDataProxy.prototype.constructor = MutationAlignerDataProxy;

/**
 * Global data manager for Mutation Data, and for other data proxies.
 *
 * @param options   data manager options (proxies, views, etc.)
 *
 * @author Selcuk Onur Sumer
 */
function MutationDataManager(options)
{
	var _viewMap = {};

	// default options
	var _defaultOpts = {
		dataFn: {
			variantAnnotation: function(dataProxies, params, callback) {
				//var mutations = params.mutationTable.getMutations();
				var mutations = params.mutations || params.mutationTable.getMutations();
				var annotationProxy = dataProxies.variantAnnotationProxy;
				var variants = [];

				_.each(mutations, function(mutation, idx) {
					var variantKey = mutation.get("variantKey") ||
					                 VariantAnnotationUtil.generateVariantKey(mutation);

					if (!_.isUndefined(variantKey))
					{
						variants.push(variantKey);
					}
				});

				if (variants.length > 0 && annotationProxy)
				{
					// make variants a comma separated list
					variants = variants.join(",");

					annotationProxy.getAnnotationData(variants, function(annotationData) {
						// enrich current mutation data with the annotation data
						VariantAnnotationUtil.addAnnotationData(mutations, annotationData);

						if (_.isFunction(callback))
						{
							callback(params);
						}
					});
				}
				else if (_.isFunction(callback))
				{
					callback(params);
				}
			},
			pdbMatch: function(dataProxies, params, callback) {
				var mutations = params.mutations || params.mutationTable.getMutations();
				var gene = params.gene || params.mutationTable.getGene();
				var pdbProxy = dataProxies.pdbProxy;
				//var uniprotId = params.uniprotId;

				// TODO this is not a safe way of getting the uniprot ID!
				var mainView = _viewMap[gene];
				var uniprotId = mainView.model.uniprotId;

				if (mutations && pdbProxy && uniprotId)
				{
					pdbProxy.getPdbRowData(uniprotId, function(pdbRowData) {
						PdbDataUtil.addPdbMatchData(mutations, pdbRowData);

						if (_.isFunction(callback))
						{
							callback(params);
						}
					});
				}
				else if (_.isFunction(callback))
				{
					callback(params);
				}
			},
			cBioPortal: function(dataProxies, params, callback) {
				var pancanProxy = dataProxies.pancanProxy;
				var mutationUtil = params.mutationUtil || params.mutationTable.getMutationUtil();
				var mutations = params.mutations || params.mutationTable.getMutations();

				// get the pancan data and update the data & display values
				pancanProxy.getPancanData({cmd: "byProteinPos"}, mutationUtil, function(dataByPos) {
					pancanProxy.getPancanData({cmd: "byHugos"}, mutationUtil, function(dataByGeneSymbol) {
						var frequencies = PancanMutationDataUtil.getMutationFrequencies(
							{protein_pos_start: dataByPos, hugo: dataByGeneSymbol});

						// update mutation counts (cBioPortal data field) for each datum
						_.each(mutations, function(ele, i) {
							//var proteinPosStart = ele[indexMap["datum"]].mutation.get("proteinPosStart");
							var proteinPosStart = ele.get("proteinPosStart");

							// update the value of the datum only if proteinPosStart value is valid
							if (proteinPosStart > 0)
							{
								var value = PancanMutationDataUtil.countByKey(frequencies, proteinPosStart) || 0;
								//ele[indexMap["datum"]].mutation.set({cBioPortal: value});
								ele.set({cBioPortal: value});
							}
							else
							{
								//ele[indexMap["datum"]].mutation.set({cBioPortal: 0});
								ele.set({cBioPortal: 0});
							}
						});

						if (_.isFunction(callback))
						{
							// frequencies is the custom data, that we should not attach to the
							// mutation object directly, so passing it to the callback function
							callback(params, frequencies);
						}
					});
				});
			}
		},
		dataProxies : {}
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// list of request queues keyed by data request type
	// <type, RequestQueue instance> pairs
	var _requestManager = {};

	/**
	 * Retrieves the data for the given data type by invoking the corresponding
	 * data retrieval function
	 *
	 * @param type      data type
	 * @param params    params to be passed over the callback function
	 * @param callback  callback function to be invoked after data retrieval
	 */
	function getData(type, params, callback)
	{
		// init a different queue for each distinct type
		if (_requestManager[type] == null)
		{
			_requestManager[type] = new RequestQueue();

			// init with a custom request process function
			_requestManager[type].init(function(element) {
				// corresponding data retrieval function
				var dataFn = _options.dataFn[element.type];

				if (_.isFunction(dataFn))
				{
					// call the function, with a special callback
					dataFn(_options.dataProxies, element.params, function(params, data) {
						// call the actual callback function
						element.callback(params, data);

						// process of the current element complete
						_requestManager[element.type].complete();
					});
				}
				// no data function is registered for this data field
				else
				{
					element.callback(element.params, null);
					// process of the current element complete
					_requestManager[type].complete();
				}
			});
		}

		// add the request to the corresponding queue.
		// this helps preventing simultaneously requests to the server for the same type
		// (NOTE: this does not check if the parameters are exactly the same or not)
		_requestManager[type].add({type: type, params: params, callback: callback});
	}

	function addView(gene, mainView)
	{
		_viewMap[gene] = mainView;
	}

	this.getData = getData;
	this.addView = addView;
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is designed to retrieve mutation data on demand, but it can be also
 * initialized with the full mutation data already retrieved from the server.
 *
 * @param options  additional options
 *
 * @author Selcuk Onur Sumer
 */
function MutationDataProxy(options)
{
	var self = this;

	// default options
	var _defaultOpts = {
		servletName: "getMutationData.json",
		geneList: "", // list of target genes (genes of interest) as a string
		params: {},    // fixed servlet params
		paramsPromise: null // alternative servlet params as a promise object
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	// MutationDetailsUtil instance
	var _util = new MutationDetailsUtil();
	// list of target genes as an array of strings (in the exact input order)
	var _unsortedGeneList = _options.geneList.trim().split(/\s+/);
	// alphabetically sorted list of target genes as an array of strings
	var _geneList = _options.geneList.trim().split(/\s+/).sort();

	/**
	 * Initializes with full mutation data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional mutation data.
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
		var data = options.data;
		var mutations = data;

		// convert to a collection if required
		// (if not an array, assuming it is a MutationCollection)
		if (_.isArray(data))
		{
			mutations = new MutationCollection(data);
		}

		_util.processMutationData(mutations);
	}

	function getGeneList()
	{
		// TODO lazy init: to find out genes with mutation data ONLY,
		// we need to query the server before hand. Otherwise,
		// we cannot remove the genes without data from the list until
		// the corresponding gene tab is clicked.
		return _geneList;
	}

	function getUnsortedGeneList()
	{
		return _unsortedGeneList;
	}

	function getRawGeneList()
	{
		return _options.geneList;
	}

	function getMutationUtil()
	{
		return _util;
	}

	/**
	 * Returns the mutation data for the given gene(s).
	 *
	 * @param geneList  list of hugo gene symbols as a whitespace delimited string
	 * @param callback  callback function to be invoked after retrieval
	 */
	function getMutationData(geneList, callback)
	{
		var genes = geneList.trim().split(/\s+/);
		var genesToQuery = [];

		// get previously grabbed data (if any)
		var mutationData = [];
		var mutationMap = _util.getMutationGeneMap();

		// process each gene in the given list
		_.each(genes, function(gene, idx) {
			gene = gene.toUpperCase();

			var data = mutationMap[gene];

			if (data == undefined ||
			    data.length == 0)
			{
				// mutation data does not exist for this gene, add it to the list
				genesToQuery.push(gene);
			}
			else
			{
				// data is already cached for this gene, update the data array
				mutationData = mutationData.concat(data);
			}
		});

		// all data is already retrieved (full init)
		if (self.isFullInit())
		{
			// just forward the call the callback function
			callback(mutationData);
		}
		// we need to retrieve missing data (lazy init)
		else
		{
			var process = function(data) {
				// process new data retrieved from server
				var mutations = new MutationCollection(data);
				_util.processMutationData(mutations);

				// concat new data with already cached data,
				// and forward it to the callback function
				mutationData = mutationData.concat(mutations.models);
				callback(mutationData);
			};

			var paramsPromise = _options.paramsPromise ||
			                    (new $.Deferred()).resolve(_options.params);

			paramsPromise.then(function(servletParams) {
				// some (or all) data is missing,
				// send ajax request for missing genes
				if (genesToQuery.length > 0)
				{
					// add genesToQuery to the servlet params
					servletParams.geneList = genesToQuery.join(" ");

					// retrieve data from the server
					//$.post(_options.servletName, servletParams, process, "json");
					var ajaxOpts = {
						type: "POST",
						url: _options.servletName,
						data: servletParams,
						success: process,
						error: function() {
							console.log("[MutationDataProxy.getMutationData] " +
								"error retrieving mutation data for genetic profiles: " + servletParams.geneticProfiles);
							process([]);
						},
						dataType: "json"
					};

					self.requestData(ajaxOpts);
				}
				// data for all requested genes already cached
				else
				{
					// just forward the data to the callback function
					callback(mutationData);
				}
			});
		}
	}

	/**
	 * Checks if there is mutation data for the current query
	 * (For the current gene list, case list, and cancer study).
	 *
	 * @return {boolean} true if there is mutation data, false otherwise.
	 */
	function hasData()
	{
		// TODO returning true in any case for now
		// we need to query server side for lazy init
		// since initially there is definitely no data
		//return (_util.getMutations().length > 0);
		return true;
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.getMutationData = getMutationData;
	self.getGeneList = getGeneList;
	self.getRawGeneList = getRawGeneList;
	self.getUnsortedGeneList = getUnsortedGeneList;
	self.getMutationUtil = getMutationUtil;
	self.hasData = hasData;
}

// MutationDataProxy extends AbstractDataProxy...
MutationDataProxy.prototype = new AbstractDataProxy();
MutationDataProxy.prototype.constructor = MutationDataProxy;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is designed to retrieve PFAM data on demand.
 *
 * @param options  additional options
 *
 * @author Selcuk Onur Sumer
 */
function PancanMutationDataProxy(options)
{
	var self = this;

	// default options
	var _defaultOpts = {
		servletName: "pancancerMutations.json"
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	// map of <keyword, data> pairs
	var _cacheByKeyword = {};
	// map of <proteinChange, data> pairs
	var _cacheByProteinChange = {};
	// map of <proteinPosStart, data> pairs
	var _cacheByProteinPosition = {};
	// map of <gene, data> pairs
	var _cacheByGeneSymbol = {};

	/**
	 * Initializes with full data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional data.
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
		var data = options.data;

		_cacheByKeyword = data.byKeyword;
		_cacheByProteinChange = data.byProteinChange;
		_cacheByGeneSymbol = data.byGeneSymbol;
		_cacheByProteinPosition = data.byProteinPosition;
	}

	function getPancanData(servletParams, mutationUtil, callback)
	{
		var cmd = servletParams.cmd;
		var q = servletParams.q;

		var data = null;
		var toQuery = null;

		if (cmd == null)
		{
			// no command provided, nothing to retrieve
			callback(null);
		}
		else if (cmd == "byKeywords")
		{
			// if no query params (keywords) provided, use all available
			var keywords = (q == null) ? mutationUtil.getAllKeywords() : q.split(",");
			getData(cmd, keywords, _cacheByKeyword, ["keyword"], callback);
		}
		else if (cmd == "byHugos")
		{
			// if no query params (genes) provided, use all available
			var genes = (q == null) ? mutationUtil.getAllGenes() : q.split(",");
			getData(cmd, genes, _cacheByGeneSymbol, ["hugo"], callback);
		}
		else if (cmd == "byProteinChanges")
		{
			// if no query params (genes) provided, use all available
			var proteinChanges = (q == null) ? mutationUtil.getAllProteinChanges() : q.split(",");
			getData(cmd, proteinChanges, _cacheByProteinChange, ["protein_change"], callback);
		}
		else if (cmd == "byProteinPos")
		{
			// if no query params (genes) provided, use all available
			var proteinPositions = (q == null) ? mutationUtil.getAllProteinPosStarts() : q.split(",");
			getData(cmd, proteinPositions, _cacheByProteinPosition, ["hugo", "protein_pos_start"], callback);
		}
		else
		{
			// invalid command
			callback(null);
		}
	}

	/**
	 * Retrieves the data from the cache and/or server.
	 *
	 * @param cmd       cmd (byHugos or byKeyword)
	 * @param keys      keys used to get cached data
	 * @param cache     target cache (byKeyword or byGeneSymbol)
	 * @param fields     field names to be used as a cache key
	 * @param callback  callback function to forward the data
	 */
	function getData(cmd, keys, cache, fields, callback)
	{
		// get cached data
		var data = getCachedData(keys, cache);
		// get keywords to query
		var toQuery = getQueryContent(data);

		if (toQuery.length > 0 &&
		    !self.isFullInit())
		{
			// retrieve missing data from the servlet
			var ajaxOpts = {
				type: "POST",
				url: _options.servletName,
				data: {cmd: cmd, q: toQuery.join(",")},
				success: function(response) {
					processData(response, data, cache, fields, callback);
				},
				dataType: "json"
			};

			self.requestData(ajaxOpts);
		}
		// everything is already cached (or full init)
		else
		{
			processData([], data, cache, fields, callback);
		}
	}

	/**
	 * Processes and caches the raw data.
	 *
	 * @param response  raw data
	 * @param data      previously cached data (for provided keys)
	 * @param cache     target cache (byKeyword or byGeneSymbol)
	 * @param fields     field names to be used as a cache key
	 * @param callback  callback function to forward the processed data
	 */
	function processData (response, data, cache, fields, callback) {
		_.each(response, function(ele, idx) {
			var keyValues = [];

			_.each(fields, function(field, idx){
				keyValues.push(ele[field]);
			});

			var key = keyValues.join("_");

			// init the list if not init yet
			if (cache[key] == null)
			{
				cache[key] = [];
			}

			if (data[key] == null)
			{
				data[key] = [];
			}

			// add data to the cache
			cache[key].push(ele);
			data[key].push(ele);
		});

		var dataArray = [];
		_.each(data, function(ele) {
			dataArray = dataArray.concat(ele);
		});

		// forward the processed data to the provided callback function
		callback(dataArray);
	}

	/**
	 * Get already cached data for the given keys.
	 * Returned object has null data for not-yet-cached keys.
	 *
	 * @param keys      cache keys
	 * @param cache     data cache
	 * @returns {Object} cached data as a map
	 */
	function getCachedData(keys, cache)
	{
		var data = {};

		_.each(keys, function(key) {
			data[key] = cache[key];
		});

		return data;
	}

	/**
	 * Returns the list of keys to query.
	 *
	 * @param data  map of <key, data> pairs
	 * @returns {Array}     list of keys to query
	 */
	function getQueryContent(data)
	{
		// keys to query
		var toQuery = [];

		_.each(_.keys(data), function(key) {
			// data not cached yet for the given key
			if (data[key] == null)
			{
				toQuery.push(key);
			}
		});

		return toQuery
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.getPancanData = getPancanData;
}

// PancanMutationDataProxy extends AbstractDataProxy...
PancanMutationDataProxy.prototype = new AbstractDataProxy();
PancanMutationDataProxy.prototype.constructor = PancanMutationDataProxy;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is designed to retrieve PDB data on demand.
 *
 * @param options  additional options
 *
 * @author Selcuk Onur Sumer
 */
function PdbDataProxy(options)
{
	var self = this;

	// default options
	var _defaultOpts = {
		//servletName: "get3dPdb.json",
		servletName: "pdb_annotation",
		subService: {
			alignmentByPdb: "alignment/byPdb",
			alignmentByUniprot: "alignment/byUniprot",
			header: "header",
			map: "map",
			summary: "summary"
		},
		listJoiner: ",",
		mutationUtil: {} // an instance of MutationDetailsUtil class
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	var _util = _options.mutationUtil;

	// cache for PDB data:

	// map of <uniprot id, PdbCollection> pairs
	var _pdbDataCache = {};

	// map of <uniprot id, PdbChain[][]> pairs
	var _pdbRowDataCache = {};

	// map of <pdb id, pdb info> pairs
	var _pdbInfoCache = {};

	// map of <uniprot id, pdb data summary> pairs
	var _pdbDataSummaryCache = {};

	// map of <gene_pdbId_chainId, positionMap> pairs
	var _positionMapCache = {};

	/**
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
		var data = options.data;

		// process pdb data
		_.each(_.keys(data.pdbData), function(uniprotId) {
			var pdbColl = PdbDataUtil.processPdbData(data.pdbData[uniprotId]);
			_pdbDataCache[uniprotId] = pdbColl;
			_pdbRowDataCache[uniprotId] = PdbDataUtil.allocateChainRows(pdbColl);
		});

		// set info data
		_pdbInfoCache = data.infoData;

		// set summary data
		_pdbDataSummaryCache = data.summaryData;

		// process position data
//		_.each(_.keys(data.positionData), function(key) {
//			// TODO this is a bit tricky so let the user provide whole cache for now...
//		});

		// set position data
		_positionMapCache = data.positionData;
	}

	/**
	 * Retrieves the position map for the given gene and chain.
	 * Invokes the given callback function after retrieving the data.
	 *
	 * @param gene          hugo gene symbol
	 * @param chain         a PdbChainModel instance
	 * @param callbackFn    function to be invoked after data retrieval
	 */
	function getPositionMap(gene, chain, callbackFn)
	{
		// collection of alignments (PdbAlignmentCollection)
		var alignments = chain.alignments;
		var cacheKey = generatePositionMapCacheKey(gene, chain);

		// do not retrieve data if it is already there
		if (self.isFullInit() || _positionMapCache[cacheKey] != null)
		{
			callbackFn(_positionMapCache[cacheKey] || {});
			return;
		}

		// get protein positions for current mutations
		var positions = _util.getProteinPositions(gene);

		// populate position data array
		// first create as an object (map),
		// then convert to an array to avoid duplicate positions
		var positionObj = {};

		// only add positions which fall between chain start & end positions
		_.each(positions, function(ele, i) {
			if (ele.start > -1 &&
			    ele.start >= chain.mergedAlignment.uniprotFrom &&
			    ele.start <= chain.mergedAlignment.uniprotTo)
			{
				positionObj[ele.start] = ele.start;
			}

			if (ele.end > ele.start &&
			    ele.end >= chain.mergedAlignment.uniprotFrom &&
			    ele.end <= chain.mergedAlignment.uniprotTo)
			{
				positionObj[ele.end] = ele.end;
			}
		});

		// convert object to array
		var positionData = _.values(positionObj);

		// populate alignment data array
		var alignmentData = [];

		alignments.each(function(ele, i) {
			alignmentData.push(ele.alignmentId);
		});

		// callback function for the AJAX call
		var processData = function(data) {
			var positionMap = {};
			var mutations = _util.getMutationGeneMap()[gene];

			// this is to be compatible with both old and the new services...
			var positionData = data.positionMap || data;

			if (positionData != null &&
			    _.size(positionData) > 0)
			{
				// re-map mutation ids with positions by using the raw position map
				for(var i=0; i < mutations.length; i++)
				{
					var start = positionData[mutations[i].getProteinStartPos()];

					// TODO if the data is an array pick the longest one...
					if (_.isArray(start) && _.size(start) > 0)
					{
						start = start[0];
					}

					var end = start;

					var type = mutations[i].get("mutationType");

					// ignore end position for mutation other than in frame del
					if (type != null &&
						type.toLowerCase() === "in_frame_del")
					{
						end = positionData[mutations[i].get("proteinPosEnd")] || end;

						// TODO if array pick the longest one...
						if (_.isArray(end) && _.size(end) > 0)
						{
							end = end[0];
						}
					}

					// if no start and end position found for this mutation,
					// then it means this mutation position is not in this chain
					if (start != null &&
					    end != null)
					{
						positionMap[mutations[i].get("mutationId")] =
							{start: start, end: end};
					}
				}
			}

			// cache the map
			if (cacheKey)
			{
				_positionMapCache[cacheKey] = positionMap;
				//console.log("%s", JSON.stringify(_positionMapCache));
			}

			// call the callback function with the updated position map
			callbackFn(positionMap);
		};

		// check if there are positions to map
		if (positionData.length > 0)
		{
			var url = _options.servletName;

			// this is to be compatible with both old and the new services...
			if (_options.subService && _options.subService.map)
			{
				url = url + "/" + _options.subService.map;
			}

			// get pdb data for the current mutations
			var ajaxOpts = {
				type: "POST",
				url: url,
				data: {
					positions: positionData.join(_options.listJoiner),
					alignments: alignmentData.join(_options.listJoiner)
				},
				success: processData,
				dataType: "json"
			};

			self.requestData(ajaxOpts);
		}
		// no position data: no need to query the server
		else
		{
			// just forward to callback with empty data
			callbackFn({});
		}
	}

	/**
	 * Generates a cache key for the position map
	 * by the given gene and chain information.
	 *
	 * @param gene  hugo gene symbol
	 * @param chain a PdbChainModel instance
	 * @returns {String} cache key as a string
	 */
	function generatePositionMapCacheKey(gene, chain)
	{
		var key = null;

		if (chain.alignments.length > 0)
		{
			// TODO make sure that the key is unique!
			key = gene + "_" + chain.alignments.at(0).pdbId + "_" + chain.chainId;
		}

		return key;
	}

	/**
	 * Retrieves the PDB data for the provided uniprot id. Passes
	 * the retrieved data as a parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callback      callback function to be invoked
	 */
	function getPdbData(uniprotId, callback)
	{
		if (self.isFullInit())
		{
			callback(_pdbDataCache[uniprotId]);
			return;
		}

		// retrieve data from the server if not cached
		if (_pdbDataCache[uniprotId] == undefined)
		{
			// process & cache the raw data
			var processData = function(data) {
				var pdbColl = PdbDataUtil.processPdbData(data);
				_pdbDataCache[uniprotId] = pdbColl;

				// forward the processed data to the provided callback function
				callback(pdbColl);
			};

			var url = _options.servletName;

			if (_options.subService &&
			    _options.subService.alignmentByUniprot)
			{
				url = url + "/" + _options.subService.alignmentByUniprot;
			}

			//retrieve data from the servlet
			var ajaxOpts = {
				type: "POST",
				url: url,
				data: {uniprotId: uniprotId, uniprotIds: uniprotId},
				success: processData,
				dataType: "json"
			};

			self.requestData(ajaxOpts);
		}
		else
		{
			// data is already cached, just forward it
			callback(_pdbDataCache[uniprotId]);
		}
	}

	/**
	 * Retrieves the PDB data for the provided uniprot id, and creates
	 * a 2D-array of pdb chains ranked by length and other criteria.
	 *
	 * Forwards the processed data to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callback      callback function to be invoked
	 */
	function getPdbRowData(uniprotId, callback)
	{
		// retrieve data if not cached yet
		if (!self.isFullInit() &&
		    _pdbRowDataCache[uniprotId] == undefined)
		{
			getPdbData(uniprotId, function(pdbColl) {
				// get the data & cache
				var rowData = PdbDataUtil.allocateChainRows(pdbColl);
				_pdbRowDataCache[uniprotId] = rowData;

				// forward to the callback
				callback(rowData);
			});
		}
		else
		{
			// data is already cached, just forward it
			callback(_pdbRowDataCache[uniprotId]);
		}
	}

	/**
	 * Retrieves the PDB data summary for the provided uniprot id. Passes
	 * the retrieved data as a parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callback      callback function to be invoked
	 */
	function getPdbDataSummary(uniprotId, callback)
	{
		// retrieve data from the server if not cached
		if (!self.isFullInit() &&
			_pdbDataSummaryCache[uniprotId] == undefined)
		{
			// process & cache the raw data
			var processData = function(data) {
				var summaryData = data;

				if (_.isArray(summaryData) &&
				    _.size(summaryData) > 0)
				{
					summaryData = summaryData[0];
				}

				_pdbDataSummaryCache[uniprotId] = summaryData;

				// forward the processed data to the provided callback function
				callback(summaryData);
			};

			var url = _options.servletName;

			if (_options.subService &&
			    _options.subService.summary)
			{
				url = url + "/" + _options.subService.summary;
			}

			// retrieve data from the servlet
			var ajaxOpts = {
				type: "POST",
				url: url,
				data: {
					uniprotId: uniprotId,
					uniprotIds: uniprotId,
					type: "summary"
				},
				success: processData,
				dataType: "json"
			};

			self.requestData(ajaxOpts);
		}
		else
		{
			// data is already cached, just forward it
			callback(_pdbDataSummaryCache[uniprotId]);
		}
	}

	/**
	 * Checks if there is structure (PDB) data available for the provided
	 * uniprot id. Passes a boolean parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callback      callback function to be invoked
	 */
	function hasPdbData(uniprotId, callback)
	{
		var processData = function(data) {
			var hasData = data && (data.alignmentCount > 0);
			callback(hasData);
		};

		getPdbDataSummary(uniprotId, processData);
	}

	/**
	 * Retrieves the PDB information for the provided PDB id(s). Passes
	 * the retrieved data as a parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param pdbIdList list of PDB ids as a white-space delimited string
	 * @param callback  callback function to be invoked
	 */
	function getPdbInfo(pdbIdList, callback)
	{
		var pdbIds = pdbIdList.trim().split(/\s+/);
		var pdbToQuery = [];

		// get previously grabbed data (if any)

		var pdbData = {};

		// process each pdb id in the given list
		_.each(pdbIds, function(pdbId, idx) {
			//pdbId = pdbId.toLowerCase();

			var data = _pdbInfoCache[pdbId];

			if (data == undefined ||
			    data.length == 0)
			{
				// data does not exist for this pdb, add it to the list
				pdbToQuery.push(pdbId);
			}
			else
			{
				// data is already cached for this pdb id, update the data object
				pdbData[pdbId] = data;
			}
		});

		if (self.isFullInit())
		{
			// no additional data to retrieve
			callback(pdbData);
			return;
		}

		var servletParams = {};

		// some (or all) data is missing,
		// send ajax request for missing ids
		if (pdbToQuery.length > 0)
		{
			// process & cache the raw data
			var processData = function(data) {
				var pdbInfoData = data;

				if (_.isArray(data))
				{
					pdbInfoData = _.indexBy(data, 'pdbId');
				}

				_.each(pdbIds, function(pdbId, idx) {
					if (pdbInfoData[pdbId] != null)
					{
						_pdbInfoCache[pdbId] = pdbInfoData[pdbId];

						// concat new data with already cached data
						pdbData[pdbId] = pdbInfoData[pdbId];
					}
				});

				// forward the final data to the callback function
				callback(pdbData);
			};

			// add pdbToQuery to the servlet params
			servletParams.pdbIds = pdbToQuery.join(_options.listJoiner);

			var url = _options.servletName;

			if (_options.subService &&
			    _options.subService.header)
			{
				url = url + "/" + _options.subService.header;
			}

			// retrieve data from the server
			var ajaxOpts = {
				type: "POST",
				url: url,
				data: servletParams,
				success: processData,
				dataType: "json"
			};

			self.requestData(ajaxOpts);
		}
		// data for all requested chains already cached
		else
		{
			// just forward the data to the callback function
			callback(pdbData);
		}
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.hasPdbData = hasPdbData;
	self.getPdbData = getPdbData;
	self.getPdbRowData = getPdbRowData;
	self.getPdbInfo = getPdbInfo;
	self.getPositionMap = getPositionMap;
}

// PdbDataProxy extends AbstractDataProxy...
PdbDataProxy.prototype = new AbstractDataProxy();
PdbDataProxy.prototype.constructor = PdbDataProxy;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is designed to retrieve PFAM data on demand.
 *
 * @param options  additional options
 *
 * @author Selcuk Onur Sumer
 */
function PfamDataProxy(options)
{
	var self = this;

	// default options
	var _defaultOpts = {
		servletName: "getPfamSequence.json"
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	// map of <gene, data> pairs
	var _pfamDataCache = {};

	/**
	 * Initializes with full PFAM data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional data.
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
		//assuming the given data is a map of <gene, sequence data> pairs
		_pfamDataCache = options.data;
	}

	function getPfamData(servletParams, callback)
	{
		// TODO allow more than one gene at a time? (see MutationDataProxy)
		var gene = servletParams.geneSymbol;

		if (gene == null)
		{
			// no gene symbol provided, nothing to retrieve
			callback(null);
			return;
		}

		// retrieve data from the server if not cached
		if (_pfamDataCache[gene] == undefined)
		{
			if (self.isFullInit())
			{
				callback(null);
				return;
			}

			// process & cache the raw data
			var processData = function(data) {
				_pfamDataCache[gene] = data;

				// forward the processed data to the provided callback function
				callback(data);
			};

			// retrieve data from the servlet
			var ajaxOpts = {
				type: "POST",
				url: _options.servletName,
				data: servletParams,
				success: processData,
				dataType: "json"
			};

			self.requestData(ajaxOpts);
		}
		else
		{
			// data is already cached, just forward it
			callback(_pfamDataCache[gene]);
		}
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.getPfamData = getPfamData;
}

// PdbDataProxy extends AbstractDataProxy...
PfamDataProxy.prototype = new AbstractDataProxy();
PfamDataProxy.prototype.constructor = PfamDataProxy;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is designed to retrieve cBio Portal specific data on demand.
 *
 * @param options  additional options
 *
 * @author Selcuk Onur Sumer
 */
function PortalDataProxy(options)
{
	var self = this;

	// default options
	var _defaultOpts = {
		servletName: "portalMetadata.json"
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	// cache
	var _data = {};

	/**
	 * Initializes with full portal data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional data.
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
		_data = options.data;
	}

	function getPortalData(servletParams, callback)
	{
		// for each servlet param, retrieve data (if not cached yet)
		var metadata = {};
		var queryParams = {};

		_.each(_.keys(servletParams), function(key, idx) {
			// not cached yet
			if (_data[key] == null)
			{
				// update query params
				queryParams[key] = servletParams[key];
			}
			// already cached
			else
			{
				// get data from cache
				metadata[key] = _data[key];
			}
		});

		var processData = function(data)
		{
			// update the cache
			_.each(_.keys(data), function(key, idx) {
				_data[key] = data[key];
			});

			// forward data to the callback function
			if(_.isFunction(callback))
			{
				callback(jQuery.extend(true, {}, metadata, data));
			}
		};

		// TODO full init...

		// everything is cached
		if (_.isEmpty(queryParams))
		{
			// just forward
			processData(metadata);
		}
		else
		{
			// retrieve data from the servlet
			var ajaxOpts = {
				type: "POST",
				url: _options.servletName,
				data: queryParams,
				success: processData,
				dataType: "json"
			};

			self.requestData(ajaxOpts);
		}
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.getPortalData = getPortalData;
}

// PdbDataProxy extends AbstractDataProxy...
PortalDataProxy.prototype = new AbstractDataProxy();
PortalDataProxy.prototype.constructor = PortalDataProxy;

/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class is designed to retrieve annotation data on demand,
 * but it can be also initialized with the full annotation data.
 *
 * @param options  proxy options
 *
 * @author Selcuk Onur Sumer
 */
function VariantAnnotationDataProxy(options)
{
	var self = this;

	// map of <variant, data> pairs
	var _annotationDataCache = {};

	// default options
	var _defaultOpts = {
		servletName: "variant_annotation/hgvs"
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	/**
	 * Initializes with full annotation data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional data.
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
		//assuming the given data is a map of <variant, annotation data> pairs
		_annotationDataCache = options.data;
	}

	/**
	 * Returns the mutation data for the given gene(s).
	 *
	 * @param variantList  list of variants as a comma separated string
	 * @param callback  callback function to be invoked after retrieval
	 */
	function getAnnotationData(variantList, callback)
	{
		var variants = variantList.trim().split(",");
		var variantsToQuery = [];

		// get previously grabbed data (if any)
		var annotationData = [];

		// process each variant in the given list
		_.each(variants, function(variant, idx) {
			// variant annotator is case sensitive!
			//variant = variant.toUpperCase();

			var data = _annotationDataCache[variant];

			if (data == undefined || _.isEmpty(data))
			{
				// annotation data does not exist for this variant, add it to the list
				variantsToQuery.push(variant);
			}
			else
			{
				// data is already cached for this variant, update the data array
				annotationData = annotationData.concat(data);
			}
		});

		// all data is already retrieved (full init)
		if (self.isFullInit())
		{
			// just forward the call the callback function
			callback(annotationData);
		}
		// we need to retrieve missing data (lazy init)
		else
		{
			var process = function(data) {
				// cache data (assuming data is an array)
				_.each(data, function(variant, idx) {
					// parse annotation JSON string
					processAnnotationJSON(variant);

					// first check if variant.id exists
					if (variant.id)
					{
						_annotationDataCache[variant.id] = variant;
					}
					// if not then try annotationJSON
					else if (variant.annotationJSON.id)
					{
						_annotationDataCache[variant.annotationJSON.id] = variant;
					}
				});

				// concat new data with already cached data,
				// and forward it to the callback function
				annotationData = annotationData.concat(data);
				callback(annotationData);
			};

			// some (or all) data is missing,
			// send ajax request for missing genes
			if (variantsToQuery.length > 0)
			{
				var variantsData = variantsToQuery.join(",");
				// retrieve data from the server
				//$.post(_options.servletName, servletParams, process, "json");
				var ajaxOpts = {
					type: "POST",
					url: _options.servletName,
					data: {variants: variantsData},
					success: process,
					error: function() {
						console.log("[VariantDataProxy.getAnnotationData] " +
						            "error retrieving annotation data for variants: " +
						            variantsData);
						process([]);
					},
					//processData: false,
					//contentType: false,
					dataType: "json"
				};

				self.requestData(ajaxOpts);
			}
			// data for all requested genes already cached
			else
			{
				// just forward the data to the callback function
				callback(annotationData);
			}
		}
	}

	/**
	 * Processes the annotationJSON string and converts it to a regular JSON.
	 *
	 * @param variant   variant to process
	 */
	function processAnnotationJSON(variant)
	{
		if (_.isString(variant.annotationJSON))
		{
			// assuming it is a JSON string
			var annotation = JSON.parse(variant.annotationJSON);

			if (_.isArray(annotation) &&
			    annotation.length > 0)
			{
				annotation = annotation[0];
			}

			variant.annotationJSON = annotation;
		}
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.getAnnotationData = getAnnotationData;
}

// VariantAnnotationDataProxy extends AbstractDataProxy...
VariantAnnotationDataProxy.prototype = new AbstractDataProxy();
VariantAnnotationDataProxy.prototype.constructor = VariantAnnotationDataProxy;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

	// column index map
	var _indexMap = null;

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
		var indexMap = _indexMap = DataTableUtil.buildColumnIndexMap(columnOrder);
		var nameMap = DataTableUtil.buildColumnNameMap(self._options.columns);

		// build a visibility map for column headers
		var visibilityMap = DataTableUtil.buildColumnVisMap(columnOrder, self._visibilityValue);
		self._visiblityMap = visibilityMap;

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
		// add listeners only if the data table is initialized
		if (self.getDataTable() != null)
		{
			_.each(self._options.eventListeners, function(listenerFn) {
				listenerFn(self.getDataTable(), self._dispatcher, indexMap);
			});
		}
	};

	/**
	 * Adds column (data) tooltips provided within the options object.
	 *
	 * @param helper    may contain additional info, functions, etc.
	 */
	self._addColumnTooltips = function(helper)
	{
		helper = helper || {};

		var tableSelector = $(self._options.el);

		_.each(_.keys(self._options.columnTooltips), function(key) {
			// do not add tooltip for excluded columns
			if (self._visiblityMap[key] != "excluded")
			{
				var tooltipFn = self._options.columnTooltips[key];

				if (_.isFunction(tooltipFn))
				{
					tooltipFn(tableSelector, helper);
				}
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

	self.setDataTable = function(dataTable)
	{
		self._dataTable = dataTable;
	};

	self.getIndexMap = function()
	{
		return _indexMap;
	};
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * 3D Mutation Visualizer, currently built on Jmol/JSmol lib.
 *
 * @param name      name of the visualizer (applet/application name)
 * @param options   visualization (Jmol) options
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function Mutation3dVis(name, options)
{
	// main container -- html element
	var _container = null;

	// actual 3D application wrapper
	var _3dApp = null;

	// flag to indicate panel size minimization
	var _minimized = false;

	// current selection (mutation positions as Jmol script compatible strings)
	// this is a map of <color, position array> pairs
	var _selection = null;

	// map of mutation ids to corresponding residue positions
	var _highlighted = {};

	// current chain (PdbChainModel instance)
	var _chain = null;

	// current pdb id
	var _pdbId = null;

	// spin indicator (initially off)
	var _spin = "OFF";

	// used for glow effect on highlighted mutations
	var _glowInterval = null;

	// default visualization options
	var defaultOpts = {
		// applet/application (Jmol/JSmol) options
		appOptions: {
			width: 400,
			height: 300,
			debug: false,
			color: "white"
		},
		pdbUri: "http://www.rcsb.org/pdb/files/", // default PDB database URI
		frame: "jsmol_frame.html",  // default JSmol frame target
		proteinScheme: "cartoon", // default style of the protein structure
		restrictProtein: false, // restrict to protein only (hide other atoms)
		defaultColor: "#DDDDDD", // default color of the whole structure
		structureColors: { // default colors for special structures
			alphaHelix: "#FFA500",
			betaSheet: "#0000FF",
			loop: "#DDDDDD"
		}, // structure color takes effect only when corresponding flag is set
		defaultTranslucency: 5, // translucency (opacity) of the whole structure
		chainColor: "#888888", // color of the selected chain
		chainTranslucency: 0, // translucency (opacity) of the selected chain
		colorProteins: "uniform", // "uniform": single color, effective for all schemes
		                          // "bySecondaryStructure": not effective for space-filling scheme
		                          // "byAtomType": effective only for space-filling scheme
		                          // "byChain": not effective for space-filling scheme
		colorMutations: "byMutationType", // "byMutationType": use mutation colors for type
		                                  // "uniform": use a single color
		                                  // "none": do not color (use default atom colors)
		mutationColor: "#8A2BE2",  // uniform color of the mutated residues
		highlightColor: "#FFDD00", // color of the user-selected mutations
		highlightGradient: ["#FFDD00", "#000000"], // gradient highlight colors used for glow effect
		addGlowEffect: false, // whether to add glow effect to highlighted mutations
		displaySideChain: "highlighted", // highlighted: display side chain for only selected mutations
		                                 // all: display side chain for all mapped mutations
		                                 // none: do not display side chain atoms
		defaultZoom: 100, // default (unfocused) zoom level
		focusZoom: 250, // focused zoom level
		containerPadding: 10, // padding for the vis container (this is to prevent overlapping)
		// TODO minimized length is actually depends on padding values, it might be better to calculate it
		minimizedHeight: 10, // minimized height of the container (assuming this will hide everything but the title)
		// color mapper function for mutations
		mutationColorMapper: function (mutationId, pdbId, chain) {
			return "#FF0000"; // just return the default color for all
		}
	};

	var _options = jQuery.extend(true, {}, defaultOpts, options);

	// main script generator for the embedded visualizer
	//var _scriptGen = new JmolScriptGenerator();
	var _scriptGen = new Mol3DScriptGenerator();

	/**
	 * Initializes the visualizer.
	 */
	function init()
	{
		// TODO make init optional (Jmol, JSmol, 3Dmol, etc.)
		// init html5 version (Jsmol)
		//_3dApp = new JmolWrapper(false);

		// init framed JSmol version
		//_3dApp = new JSmolWrapper();

		// init app (with frames)
		//_3dApp.init(name, _options.appOptions, _options.frame);

		// init app (without frames frames)
		//_3dApp.init(name, _options.appOptions);

		_3dApp = new Mol3DWrapper();
		_3dApp.init(name, _options.appOptions);
		_scriptGen.setViewer(_3dApp.getViewer());
		_scriptGen.setPdbUri(_options.pdbUri);

		// TODO memory leak -- eventually crashes the browser
//		if (_options.addGlowEffect)
//		{
//			addGlowEffect();
//		}
	}

	/**
	 * Updates visualizer container.
	 *
	 * @param container html element
	 */
	function updateContainer(container)
	{
		// update reference
		_container = $(container);

		var appContainer = _container.find(".mutation-3d-vis-container");

		// set width
		appContainer.css("width", _options.appOptions.width);
		// set height (should be slightly bigger than the app height)
		appContainer.css("height", _options.appOptions.height + _options.containerPadding);
		// update app container
		_3dApp.updateContainer(appContainer);
	}

	/**
	 * Toggles the spin.
	 */
	function toggleSpin()
	{
		_spin == "ON" ? _spin = "OFF" : _spin = "ON";

		var script = _scriptGen.spin(_spin);

		_3dApp.script(script);
	}

	/**
	 * Reapply the visual style for the current options.
	 */
	function reapplyStyle()
	{
//		var script = "select all;" +
//		             _styleScripts[style];
		// regenerate visual style script
		var script = _scriptGen.generateVisualStyleScript(_selection, _chain, _options);

		// regenerate highlight script
		script = script.concat(generateHighlightScript(_highlighted));

		// convert array to a single string
		script = script.join(" ");
		_3dApp.script(script);
	}

	/**
	 * Shows the visualizer panel.
	 */
	function show()
	{
		if (_container != null)
		{
			_container.show();
		}
	}

	/**
	 * Hides the visualizer panel.
	 */
	function hide()
	{
		if (_container != null)
		{
			_container.hide();
		}
	}

	/**
	 * Minimizes the container (only title will be shown)
	 */
	function minimize()
	{
		// minimize container
		if (_container != null)
		{
			_container.css({"overflow": "hidden",
				"height": _options.minimizedHeight});
			_minimized = true;
		}
	}

	/**
	 * Maximizes the container to its full height
	 */
	function maximize()
	{
		if (_container != null)
		{
			_container.css({"overflow": "", "height": ""});
			_minimized = false;
		}
	}

	function toggleSize()
	{
		if (_container != null)
		{
			if(_minimized)
			{
				maximize();
			}
			else
			{
				minimize();
			}
		}
	}

	function isVisible()
	{
		var top = parseInt(_container.css("top"));

		var hidden = (top == -9999) || _container.is(":hidden");

		return !hidden;
	}

	/**
	 * Reloads the protein view for the given PDB id and the chain.
	 *
	 * This function returns an array of mapping mutations (residues).
	 * If there is no mapping residue for currently visible mutations on
	 * the diagram, then function returns false. Note that this function
	 * returns without waiting the callback function to be invoked.
	 *
	 * @param pdbId     PDB id
	 * @param chain     PdbChainModel instance
	 * @param callback  function to call after reload
	 * @return  {Array} array of mapped mutation ids
	 */
	function reload(pdbId, chain, callback)
	{
		var mappedMutations = [];

		// reset highlights
		_highlighted = {};

		// pdbId and/or chainId may be null
		if (!pdbId || !chain)
		{
			// nothing to load
			return mappedMutations;
		}

		// save current pdb id & chain for a possible future access
		_chain = chain;
		_pdbId = pdbId;

		// update selection map
		mappedMutations = updateSelectionMap(pdbId, chain);

		// construct Jmol script string
		var script = [];

		// this callback is required for 3Dmol, since loadPdb function is async!
		var loadCallback = function() {
			script.push(loadPdb); // load the corresponding pdb

			script = script.concat(
				_scriptGen.generateVisualStyleScript(_selection, _chain, _options));

			// TODO spin is currently disabled...
			//script.push("spin " + _spin + ";");

			// convert array into a string (to pass to Jmol)
			script = script.join(" ");

			// run script
			_3dApp.script(script, callback);

			// workaround to fix the problem where canvas is initially invisible
			resizeViewer();

			//if (_container != null)
			//{
			//	$(_container).resize();
			//}
		};

		var loadPdb = _scriptGen.loadPdb(pdbId, loadCallback);

		// any other script generator should return the actual script value,
		// so this means callback function is NOT called within the script generator
		// we need to call it explicitly
		if (loadPdb != "$3Dmol")
		{
			loadCallback();
		}

		return mappedMutations;
	}

	/**
	 * Refreshes the view without reloading the pdb structure.
	 * Using this function instead of reload makes things
	 * a lot faster for filtering operations.
	 *
	 * @return  {Array} array of mapped mutation ids
	 */
	function refresh()
	{
		var mappedMutations = [];

		// reset highlights
		_highlighted = {};

		// pdbId and/or chainId may be null
		if (_pdbId == null || _chain == null)
		{
			// nothing to refresh
			return mappedMutations;
		}

		// update selection map
		mappedMutations = updateSelectionMap(_pdbId, _chain);

		var script = [];

		// update visual style by using the updated selection map
		script = script.concat(
			_scriptGen.generateVisualStyleScript(_selection, _chain, _options));

		// convert array into a string (to pass to Jmol)
		script = script.join(" ");

		// run script
		_3dApp.script(script);

		return mappedMutations;
	}

	/**
	 * Updates the selection map for the current pdbId and chain
	 * by using the corresponding color mapper function.
	 *
	 * @param pdbId     PDB id
	 * @param chain     PdbChainModel instance
	 * @return {Array}  array of mapped mutation ids
	 */
	function updateSelectionMap(pdbId, chain)
	{
		// update selection for a possible future restore
		var result = generateColorMap(pdbId, chain, _options, _scriptGen);

		_selection = result.colorMap;

		return result.mappedMutations;
	}

	/**
	 * Generates color mapping for the mutations within the position map
	 * of the given chain.
	 *
	 * @param pdbId     pdb id as a string
	 * @param chain     a PdbChainModel instance
	 * @param options   visual style options
	 * @param scriptGen a MolScriptGenerator instance
	 * @returns {Object} map of <color, script position array> and mapped mutations
	 */
	function generateColorMap (pdbId, chain, options, scriptGen)
	{
		var mappedMutations = [];
		var colorMap = {};
		var color = options.mutationColor;

		// update the residue selection map wrt mutation color mapper
		_.each(_.keys(chain.positionMap), function(mutationId) {
			var position = chain.positionMap[mutationId];

			if (_.isFunction(options.mutationColorMapper))
			{
				color = options.mutationColorMapper(mutationId, pdbId, chain);
			}

			// do not color at all if the color is null,
			// this automatically hides user-filtered mutations
			// TODO but this also hides unmapped mutations (if any)
			if (color != null)
			{
				if (colorMap[color] == null)
				{
					// using an object instead of an array (to avoid duplicates)
					colorMap[color] = {};
				}

				var scriptPos = scriptGen.scriptPosition(position);
				colorMap[color][scriptPos] = scriptPos;
				mappedMutations.push(mutationId);
			}
			//else
			//{
			//	color = defaultOpts.mutationColor;
			//}
		});

		// convert maps to arrays
		_.each(colorMap, function(value, key, list) {
			// key is a "color"
			// value is a "position script string" map
			list[key] = _.values(value);
		});

		return {
			colorMap: colorMap,
			mappedMutations: mappedMutations
		};
	}

	/**
	 * Centers the view onto the currently highlighted residue.
	 *
	 * @return {boolean} true if center successful, false otherwise
	 */
	function centerOnHighlighted()
	{
		// perform action if there is only one highlighted position
		if (_.size(_highlighted) != 1)
		{
			return false;
		}

		var script = [];

		_.each(_highlighted, function (position) {
			script = script.concat(generateCenterScript(position));
		});

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);

		return true;
	}

	/**
	 * Resets the current center to the default position.
	 */
	function resetCenter()
	{
		var script = [];

		// center to default position
		script.push(_scriptGen.defaultCenter());

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);
	}

	function resizeViewer()
	{
		if (_3dApp.getViewer)
		{
			_3dApp.getViewer().resize();
		}
	}

	/**
	 * Focuses on the residue corresponding to the given pileup. If there is
	 * no corresponding residue for the given pileup, this function does not
	 * perform a focus operation, and returns false.
	 *
	 * @param pileup    Pileup instance
	 * @return {boolean}    true if there there a matching residue, false o.w.
	 */
	function focus(pileup)
	{
		// no chain selected yet, terminate
		if (_chain == null)
		{
			return false;
		}

		// assuming all other mutations in the same pileup have
		// the same (or very close) mutation position.
		var id = pileup.mutations[0].get("mutationId");

		// get script
		var script = generateFocusScript(id);
		//script = script.concat(generateHighlightScript(id));

		// check if the script is valid
		if (script.length > 0)
		{
			// convert array to a single string
			script = script.join(" ");

			// send script string to the app
			_3dApp.script(script);
		}
		// no mapping position for this mutation on this chain
		else
		{
			// just reset focus
			resetFocus();
			return false;
		}

		return true;
	}

	/**
	 * Resets the current focus to the default position and zoom level.
	 */
	function resetFocus()
	{
		// zoom out to default zoom level, center to default position,
		// and remove all selection highlights
		var script = [];
		script.push(_scriptGen.zoom(_options.defaultZoom)); // zoom to default zoom level
		script.push(_scriptGen.defaultCenter()); // center to default position

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);
	}

	/**
	 * Highlights the residue corresponding to the given pileups. This
	 * function returns the number of successfully mapped residues
	 * for the given pileups (returns zero if no mapping at all).
	 *
	 * @param pileups   an array of Pileup instances
	 * @param reset     indicates whether to reset previous highlights
	 * @return {Number} number of mapped pileups (residues)
	 */
	function highlight(pileups, reset)
	{
		// no chain selected yet, terminate
		if (_chain == null)
		{
			return 0;
		}

		if (reset)
		{
			// reset all previous highlights
			_highlighted = {};
		}

		// init script generation
		var script = _scriptGen.generateVisualStyleScript(_selection, _chain, _options);

		var numMapped = 0;

		_.each(pileups, function(pileup, i) {
			// assuming all other mutations in the same pileup have
			// the same (or very close) mutation position.
			var id = pileup.mutations[0].get("mutationId");
			var position = _chain.positionMap[id];

			if (position != null)
			{
				// add position to the highlighted ones
				_highlighted[id] = position;
				numMapped++;
			}
		});

		// add highlight script string
		script = script.concat(generateHighlightScript(_highlighted));

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);

		// return number of mapped residues for the given pileups
		return numMapped;
	}

	/**
	 * Refreshes the current highlights.
	 */
	function refreshHighlight()
	{
		var script = generateHighlightScript(_highlighted);

		// convert array to a single string
		script = script.join(" ");

		// send script string to the app
		_3dApp.script(script);
	}

	/**
	 * Remove all highlights.
	 */
	function resetHighlight()
	{
		// reset highlight map
		_highlighted = {};

		// remove all selection highlights
		var script = _scriptGen.generateVisualStyleScript(_selection, _chain, _options);

		// convert array to a single string
		script = script.join(" ");

		_3dApp.script(script);
	}

	/**
	 * Generates the highlight script to be sent to the 3D app.
	 *
	 * @param positions mutation positions to highlight
	 * @return {Array}  script lines as an array
	 */
	function generateHighlightScript(positions)
	{
		return _scriptGen.generateHighlightScript(
			positions, _options.highlightColor, _options, _chain);
	}

	/**
	 * Generates the center script to be sent to the 3D app.
	 *
	 * @param position  position to center onto
	 * @return {Array}  script lines as an array
	 */
	function generateCenterScript(position)
	{
		var script = [];

		// center to the selection
		script.push(_scriptGen.center(position, _chain.chainId));

		return script;
	}

	/**
	 * Generates the focus script to be sent to the 3D app.
	 *
	 * @param mutationId    id of the mutation to highlight
	 * @return {Array}      script lines as an array
	 */
	function generateFocusScript(mutationId)
	{
		var script = [];
		var position = _chain.positionMap[mutationId];

		// check if the mutation maps on this chain
		if (position != null)
		{
			// center and zoom to the selection
			script.push(_scriptGen.zoom(_options.focusZoom));
			script.push(_scriptGen.center(position, _chain.chainId));
		}

		return script;
	}

	/**
	 * Performs the default zoom in operation.
	 * (Uses default zoom level defined by the underlying 3D visualizer)
	 */
	function zoomIn()
	{
		_3dApp.script(_scriptGen.defaultZoomIn());
	}

	/**
	 * Performs the default zoom out operation.
	 * (Uses default zoom value defined by the underlying 3D visualizer)
	 */
	function zoomOut()
	{
		_3dApp.script(_scriptGen.defaultZoomOut());
	}

	/**
	 * Zooms to default zoom level.
	 */
	function zoomActual()
	{
		_3dApp.script(_scriptGen.zoom(_options.defaultZoom));
	}

	/**
	 * Zooms to the given zoom level.
	 *
	 * @param value desired zoom level
	 */
	function zoomTo(value)
	{
		_3dApp.script(_scriptGen.zoom(value));
	}

	/**
	 * Updates the options of the 3D visualizer.
	 *
	 * @param options   new options object
	 */
	function updateOptions(options)
	{
		_options = jQuery.extend(true, {}, _options, options);
	}

	/**
	 * Adds glow effect to the user selected (highlighted) mutations.
	 */
	function addGlowEffect()
	{
		// clear previous glow interval (if any)
		if (_glowInterval != null)
		{
			clearInterval(_glowInterval);
		}

		// create gradient color generator
		var gradient = new Rainbow();
		var range = 16;
		var index = 0;
		gradient.setNumberRange(0, range - 1);
		gradient.setSpectrum(_options.highlightGradient[0].replace("#", ""),
		                     _options.highlightGradient[1].replace("#", ""));

		// convert positions to script positions
		var scriptPositions = null;

		// set new interval
		_glowInterval = setInterval(function() {
			var highlightCount = _.size(_highlighted);

			if (highlightCount > 0)
			{
				// TODO update script position each time _highlighted is updated
				if (scriptPositions == null ||
				    scriptPositions.length != highlightCount)
				{
					scriptPositions = _scriptGen.highlightScriptPositions(_highlighted);
				}
			}

			if (scriptPositions != null &&
			    scriptPositions.length > 0)
			{
				var color = "#" + gradient.colorAt(index);
				var script = _scriptGen.highlightScript(
					scriptPositions, color, _options, _chain);

				// convert array to a single string
				script = script.join(" ");

				// send script string to the app
				_3dApp.script(script);

				index = (index + 1) % range;
			}
		}, 50);
	}

	/**
	 * Generates a PymolScript from the current state of the 3D visualizer.
	 */
	function generatePymolScript()
	{
		var scriptGen = new PymolScriptGenerator();
		var script = [];

		// reinitialize
		script.push(scriptGen.reinitialize());

		// set background color
		script.push(scriptGen.bgColor(_options.appOptions.color));

		// load current pdb
		script.push(scriptGen.loadPdb(_pdbId));

		// generate visual style from current options
		script = script.concat(
			scriptGen.generateVisualStyleScript(
				_selection, _chain, _options));

		// generate highlight script from current highlights
		script = script.concat(
			scriptGen.generateHighlightScript(
				_highlighted, _options.highlightColor, _options, _chain));

		script.push(scriptGen.selectNone());

		// convert array to line of scripts
		script = script.join("\n");

		return script;
	}

	// return public functions
	return {
		init: init,
		show: show,
		hide: hide,
		minimize: minimize,
		maximize: maximize,
		toggleSize: toggleSize,
		isVisible: isVisible,
		reload: reload,
		refresh: refresh,
		resizeViewer: resizeViewer,
		focusOn: focus,
		center: centerOnHighlighted,
		resetCenter: resetCenter,
		highlight: highlight,
		resetHighlight: resetHighlight,
		refreshHighlight: refreshHighlight,
		zoomIn: zoomIn,
		zoomOut: zoomOut,
		zoomActual: zoomActual,
		zoomTo: zoomTo,
		resetFocus: resetFocus,
		updateContainer: updateContainer,
		toggleSpin: toggleSpin,
		reapplyStyle : reapplyStyle,
		updateOptions: updateOptions,
		generatePymolScript: generatePymolScript
	};
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * MutationDetailsTable class (extends AdvancedDataTable)
 *
 * Highly customizable table view built on DataTables plugin.
 * See default options object (_defaultOpts) for details.
 *
 * With its default configuration, following events are dispatched by this class:
 * - MutationDetailsEvents.PDB_LINK_CLICKED:
 *   dispatched when clicked on a 3D link (in the protein change column)
 * - MutationDetailsEvents.PROTEIN_CHANGE_LINK_CLICKED:
 *   dispatched when clicked on the protein change link (in the protein change column)
 * - MutationDetailsEvents.MUTATION_TABLE_FILTERED:
 *   dispatched when the table is filter by a user input (via the search box)
 *
 * @param options       visual options object
 * @param gene          hugo gene symbol
 * @param mutationUtil  mutation details util
 * @param dataProxies   all available data proxies
 * @param dataManager   mutation data manager for additional data requests
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationDetailsTable(options, gene, mutationUtil, dataProxies, dataManager)
{
	var self = this;

	// default options object
	var _defaultOpts = {
		el: "#mutation_details_table_d3",
		//elWidth: 740, // width of the container
		// default column options
		//
		// sTitle: display value
		// tip: tooltip value of the column header
		//
		// [data table options]: sType, sClass, sWidth, asSorting, ...
		columns: {
			datum: {sTitle: "datum",
				tip: ""},
			mutationId: {sTitle: "Mutation ID",
				tip: "Mutation ID",
				sType: "string"},
			mutationSid: {sTitle: "Mutation SID",
				tip: "",
				sType: "string"},
			caseId: {sTitle: "Sample ID",
				tip: "Sample ID",
				sType: "string"},
			cancerStudy: {sTitle: "Cancer Study",
				tip: "Cancer Study",
				sType: "string"},
			tumorType: {sTitle: "Cancer Type",
				tip: "Cancer Type",
				sType: "string"},
			proteinChange: {sTitle: "AA change",
				tip: "Protein Change",
				sType: "numeric"},
			mutationType: {sTitle: "Type",
				tip: "Mutation Type",
				sType: "string",
				sClass: "center-align-td"},
			cna: {sTitle: "Copy #",
				tip: "Copy-number status of the mutated gene",
				sType: "numeric",
				sClass: "center-align-td"},
			cosmic: {sTitle: "COSMIC",
				tip: "Overlapping mutations in COSMIC",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"]},
			mutationStatus: {sTitle: "MS",
				tip: "Mutation Status",
				sType: "string",
				sClass: "center-align-td"},
			validationStatus: {sTitle: "VS",
				tip: "Validation Status",
				sType: "string",
				sClass: "center-align-td"},
			mutationAssessor: {sTitle: "Mutation Assessor",
				tip: "Predicted Functional Impact Score (via Mutation Assessor) for missense mutations",
				sType: "numeric",
				sClass: "center-align-td",
				asSorting: ["desc", "asc"],
				sWidth: "2%"},
			sequencingCenter: {sTitle: "Center",
				tip: "Sequencing Center",
				sType: "string",
				sClass: "center-align-td"},
			chr: {sTitle: "Chr",
				tip: "Chromosome",
				sType: "string"},
			startPos: {sTitle: "Start Pos",
				tip: "Start Position",
				sType: "numeric",
				sClass: "right-align-td"},
			endPos: {sTitle: "End Pos",
				tip: "End Position",
				sType: "numeric",
				sClass: "right-align-td"},
			referenceAllele: {sTitle: "Ref",
				tip: "Reference Allele",
				sType: "string"},
			variantAllele: {sTitle: "Var",
				tip: "Variant Allele",
				sType: "string"},
			tumorFreq: {sTitle: "Allele Freq (T)",
				tip: "Variant allele frequency<br> in the tumor sample",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"]},
			normalFreq: {sTitle: "Allele Freq (N)",
				tip: "Variant allele frequency<br> in the normal sample",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"]},
			tumorRefCount: {sTitle: "Var Ref",
				tip: "Variant Ref Count",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"]},
			tumorAltCount: {sTitle: "Var Alt",
				tip: "Variant Alt Count",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"]},
			normalRefCount: {sTitle: "Norm Ref",
				tip: "Normal Ref Count",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"]},
			normalAltCount: {sTitle: "Norm Alt",
				tip: "Normal Alt Count",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"]},
			igvLink: {sTitle: "BAM",
				tip: "Link to BAM file",
				sType: "string",
				sClass: "center-align-td"},
			mutationCount: {sTitle: "#Mut in Sample",
				tip: "Total number of<br> nonsynonymous mutations<br> in the sample",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"],
				sWidth: "2%"},
			cBioPortal: {sTitle: "cBioPortal",
				tip: "Mutation frequency in cBioPortal",
				sType: "numeric",
				sClass: "right-align-td",
				asSorting: ["desc", "asc"]}
		},
		// display order of column headers
		columnOrder: [
			"datum", "mutationId", "mutationSid", "caseId", "cancerStudy", "tumorType",
			"proteinChange", "mutationType", "cna", "cBioPortal", "cosmic", "mutationStatus",
			"validationStatus", "mutationAssessor", "sequencingCenter", "chr",
			"startPos", "endPos", "referenceAllele", "variantAllele", "tumorFreq",
			"normalFreq", "tumorRefCount", "tumorAltCount", "normalRefCount",
			"normalAltCount", "igvLink", "mutationCount"
		],
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
			"proteinChange": "visible",
			"caseId": function (util, gene) {
				if (util.containsCaseId(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"mutationType": function (util, gene) {
				if (util.containsMutationType(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"mutationAssessor": function (util, gene) {
				if (util.containsFis(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
//			"cosmic": function (util, gene) {
//				if (util.containsCosmic(gene)) {
//					return "visible";
//				}
//				else {
//					return "hidden";
//				}
//			},
			"cosmic": "visible",
			"mutationCount": function (util, gene) {
				if (util.containsMutationCount(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"mutationId": "excluded",
			"mutationSid": "excluded",
			"cancerStudy": "excluded",
			// TODO we may need more parameters than these two (util, gene)
			"cna" : function (util, gene) {
				if (util.containsCnaData(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"tumorFreq": function (util, gene) {
				if (util.containsAlleleFreqT(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"igvLink": function (util, gene) {
				if (util.containsIgvLink(gene)) {
					return "visible";
				}
				else {
					//return "excluded";
					return "hidden";
				}
			},
			"mutationStatus": function (util, gene) {
				if (util.containsGermline(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"validationStatus": function (util, gene) {
				if (util.containsValidStatus(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"tumorType": function (util, gene) {
				var count = util.distinctTumorTypeCount(gene);

				if (count > 1) {
					return "visible";
				}
				else if (count > 0) {
					return "hidden";
				}
				else { // if (count <= 0)
					//return "excluded";
					return "hidden";
				}
			},
			//"cBioPortal": function (util, gene) {
			//	if (util.containsKeyword(gene) ||
			//	    util.containsMutationEventId(gene))
			//	{
			//		return "visible";
			//	}
			//	else {
			//		return "excluded";
			//	}
			//}
			"cBioPortal": "excluded"
		},
		// Indicates whether a column is searchable or not.
		// Should be a boolean value or a function.
		//
		// All other columns will be initially non-searchable by default.
		columnSearch: {
			"caseId": true,
			"mutationId": true,
			"mutationSid": true,
			"cancerStudy": true,
			"proteinChange": true,
			"tumorType": true,
			"mutationType": true
		},
		// renderer functions:
		// returns the display value for a column (may contain html elements)
		// if no render function is defined for a column,
		// then we rely on a custom "mData" function.
		columnRender: {
			"mutationId": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("mutationId");
				if (value === undefined) {
					return "";
				}
				return value;
				//return (mutation.mutationId + "-" + mutation.mutationSid);
			},
			"mutationSid": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("mutationSid");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"caseId": function(datum) {
				var mutation = datum.mutation;
				var caseIdFormat = MutationDetailsTableFormatter.getCaseId(mutation.get("caseId"));
				var vars = {};
				vars.linkToPatientView = mutation.get("linkToPatientView");
				vars.caseId = caseIdFormat.text;
				vars.caseIdClass = caseIdFormat.style;
				vars.caseIdTip = caseIdFormat.tip;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_case_id_template");
				return templateFn(vars);
			},
			"proteinChange": function(datum) {
				var mutation = datum.mutation;

				// check if data exists,
				// if not we need to retrieve it from the data manager
				if (_.isUndefined(mutation.get("proteinChange")))
				{
					self.requestColumnData("variantAnnotation", "proteinChange");
					return MutationViewsUtil.renderTablePlaceHolder();
				}
				else
				{
					var proteinChange = MutationDetailsTableFormatter.getProteinChange(mutation);
					var vars = {};

					vars.proteinChange = proteinChange.text;
					vars.proteinChangeClass = proteinChange.style;
					vars.proteinChangeTip = proteinChange.tip;
					vars.additionalProteinChangeTip = proteinChange.additionalTip;

					// check if pdbMatch data exists,
					// if not we need to retrieve it from the data manager
					if (_.isUndefined(mutation.get("pdbMatch")))
					{
						self.requestColumnData("pdbMatch", "proteinChange");
					}

					vars.pdbMatchLink = MutationDetailsTableFormatter.getPdbMatchLink(mutation);

					var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_protein_change_template");
					return templateFn(vars);
				}
			},
			"cancerStudy": function(datum) {
				var mutation = datum.mutation;
				var vars = {};
				//vars.cancerType = mutation.cancerType;
				vars.cancerStudy = mutation.get("cancerStudy");
				vars.cancerStudyShort = mutation.get("cancerStudyShort");
				vars.cancerStudyLink = mutation.get("cancerStudyLink");

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_cancer_study_template");
				return templateFn(vars);
			},
			"tumorType": function(datum) {
				var mutation = datum.mutation;
				var tumorType = MutationDetailsTableFormatter.getTumorType(mutation);
				var vars = {};
				vars.tumorType = tumorType.text;
				vars.tumorTypeClass = tumorType.style;
				vars.tumorTypeTip = tumorType.tip;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_tumor_type_template");
				return templateFn(vars);
			},
			"mutationType": function(datum) {
				var mutation = datum.mutation;

				// check if data exists,
				// if not we need to retrieve it from the data manager
				if (_.isUndefined(mutation.get("mutationType")))
				{
					self.requestColumnData("variantAnnotation", "mutationType");
					return MutationViewsUtil.renderTablePlaceHolder();
				}
				else
				{
					var mutationType = MutationDetailsTableFormatter.getMutationType(mutation.get("mutationType"));
					var vars = {};
					vars.mutationTypeClass = mutationType.style;
					vars.mutationTypeText = mutationType.text;

					var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_mutation_type_template");
					return templateFn(vars);
				}
			},
			"cosmic": function(datum) {
				var mutation = datum.mutation;
				var cosmic = MutationDetailsTableFormatter.getCosmic(mutation.getCosmicCount());
				var vars = {};
				vars.cosmicClass = cosmic.style;
				vars.cosmicCount = cosmic.count;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_cosmic_template");
				return templateFn(vars);
			},
			"cna": function(datum) {
				var mutation = datum.mutation;
				var cna = MutationDetailsTableFormatter.getCNA(mutation.get("cna"));
				var vars = {};
				vars.cna = cna.text;
				vars.cnaClass = cna.style;
				vars.cnaTip = cna.tip;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_cna_template");
				return templateFn(vars);
			},
			"mutationCount": function(datum) {
				var mutation = datum.mutation;
				var mutationCount = MutationDetailsTableFormatter.getIntValue(mutation.get("mutationCount"));
				var vars = {};
				vars.mutationCount = mutationCount.text;
				vars.mutationCountClass = mutationCount.style;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_mutation_count_template");
				return templateFn(vars);
			},
			"normalFreq": function(datum) {
				var mutation = datum.mutation;
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.get("normalAltCount"));
				var normalFreq = MutationDetailsTableFormatter.getAlleleFreq(mutation.get("normalFreq"),
					mutation.get("normalAltCount"),
					mutation.get("normalRefCount"),
					"simple-tip");
				var vars = {};
				vars.normalFreq = normalFreq.text;
				vars.normalFreqClass = normalFreq.style;
				vars.normalFreqTipClass = normalFreq.tipClass;
				vars.normalTotalCount = normalFreq.total;
				vars.normalAltCount = alleleCount.text;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_normal_freq_template");
				return templateFn(vars);
			},
			"tumorFreq": function(datum) {
				var mutation = datum.mutation;
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.get("tumorAltCount"));
				var tumorFreq = MutationDetailsTableFormatter.getAlleleFreq(mutation.get("tumorFreq"),
					mutation.get("tumorAltCount"),
					mutation.get("tumorRefCount"),
					"simple-tip");
				var vars = {};
				vars.tumorFreq = tumorFreq.text;
				vars.tumorFreqClass = tumorFreq.style;
				vars.tumorFreqTipClass = tumorFreq.tipClass;
				vars.tumorTotalCount = tumorFreq.total;
				vars.tumorAltCount = alleleCount.text;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_tumor_freq_template");
				return templateFn(vars);
			},
			"mutationAssessor": function(datum) {
				var mutation = datum.mutation;
				var fis = MutationDetailsTableFormatter.getFis(
					mutation.get("functionalImpactScore"), mutation.get("fisValue"));
				var vars = {};
				vars.fisClass = fis.fisClass;
				vars.omaClass = fis.omaClass;
				vars.fisText = fis.text;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_mutation_assessor_template");
				return templateFn(vars);
			},
			"mutationStatus": function(datum) {
				var mutation = datum.mutation;
				var mutationStatus = MutationDetailsTableFormatter.getMutationStatus(mutation.get("mutationStatus"));
				var vars = {};
				vars.mutationStatusTip = mutationStatus.tip;
				vars.mutationStatusClass = mutationStatus.style;
				vars.mutationStatusText = mutationStatus.text;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_mutation_status_template");
				return templateFn(vars);
			},
			"validationStatus": function(datum) {
				var mutation = datum.mutation;
				var validationStatus = MutationDetailsTableFormatter.getValidationStatus(mutation.get("validationStatus"));
				var vars = {};
				vars.validationStatusTip = validationStatus.tip;
				vars.validationStatusClass = validationStatus.style;
				vars.validationStatusText = validationStatus.text;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_validation_status_template");
				return templateFn(vars);
			},
			"normalRefCount": function(datum) {
				var mutation = datum.mutation;
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.get("normalRefCount"));
				var vars = {};
				vars.normalRefCount = alleleCount.text;
				vars.normalRefCountClass = alleleCount.style;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_normal_ref_count_template");
				return templateFn(vars);
			},
			"normalAltCount": function(datum) {
				var mutation = datum.mutation;
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.get("normalAltCount"));
				var vars = {};
				vars.normalAltCount = alleleCount.text;
				vars.normalAltCountClass = alleleCount.style;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_normal_alt_count_template");
				return templateFn(vars);
			},
			"tumorRefCount": function(datum) {
				var mutation = datum.mutation;
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.get("tumorRefCount"));
				var vars = {};
				vars.tumorRefCount = alleleCount.text;
				vars.tumorRefCountClass = alleleCount.style;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_tumor_ref_count_template");
				return templateFn(vars);
			},
			"tumorAltCount": function(datum) {
				var mutation = datum.mutation;
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.get("tumorAltCount"));
				var vars = {};
				vars.tumorAltCount = alleleCount.text;
				vars.tumorAltCountClass = alleleCount.style;

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_tumor_alt_count_template");
				return templateFn(vars);
			},
			"startPos": function(datum) {
				var mutation = datum.mutation;

				// check if data exists,
				// if not we need to retrieve it from the data manager
				if (_.isUndefined(mutation.get("startPos")))
				{
					self.requestColumnData("variantAnnotation", "startPos");
					return MutationViewsUtil.renderTablePlaceHolder();
				}
				else
				{
					var startPos = MutationDetailsTableFormatter.getIntValue(mutation.get("startPos"));
					var vars = {};
					vars.startPos = startPos.text;
					vars.startPosClass = startPos.style;

					var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_start_pos_template");
					return templateFn(vars);
				}
			},
			"endPos": function(datum) {
				var mutation = datum.mutation;

				// check if data exists,
				// if not we need to retrieve it from the data manager
				if (_.isUndefined(mutation.get("endPos")))
				{
					self.requestColumnData("variantAnnotation", "endPos");
					return MutationViewsUtil.renderTablePlaceHolder();
				}
				else
				{
					var endPos = MutationDetailsTableFormatter.getIntValue(mutation.get("endPos"));
					var vars = {};
					vars.endPos = endPos.text;
					vars.endPosClass = endPos.style;

					var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_end_pos_template");
					return templateFn(vars);
				}
			},
			"sequencingCenter": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("sequencingCenter");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"chr": function(datum) {
				var mutation = datum.mutation;

				// check if data exists,
				// if not we need to retrieve it from the data manager
				if (_.isUndefined(mutation.get("chr")))
				{
					self.requestColumnData("variantAnnotation", "chr");
					return MutationViewsUtil.renderTablePlaceHolder();
				}
				else
				{
					return mutation.get("chr") || "";
				}
			},
			"referenceAllele": function(datum) {
				var mutation = datum.mutation;

				// check if data exists,
				// if not we need to retrieve it from the data manager
				if (_.isUndefined(mutation.get("referenceAllele")))
				{
					self.requestColumnData("variantAnnotation", "referenceAllele");
					return MutationViewsUtil.renderTablePlaceHolder();
				}
				else
				{
					return mutation.get("referenceAllele") || "";
				}
			},
			"variantAllele": function(datum) {
				var mutation = datum.mutation;

				// check if data exists,
				// if not we need to retrieve it from the data manager
				if (_.isUndefined(mutation.get("variantAllele")))
				{
					self.requestColumnData("variantAnnotation", "variantAllele");
					return MutationViewsUtil.renderTablePlaceHolder();
				}
				else
				{
					return mutation.get("variantAllele") || "";
				}
			},
			"igvLink": function(datum) {
				//vars.xVarLink = mutation.xVarLink;
				//vars.msaLink = mutation.msaLink;
				//vars.igvLink = mutation.igvLink;
				var mutation = datum.mutation;
				var vars = {};
				vars.igvLink = MutationDetailsTableFormatter.getIgvLink(mutation);

				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_igv_link_template");
				return templateFn(vars);
			},
			"cBioPortal": function(datum) {
				var mutation = datum.mutation;

				// check if cBioPortal data exists,
				// if not we need to retrieve it from the data manager
				if (_.isUndefined(mutation.get("cBioPortal")))
				{
					self.requestColumnData("cBioPortal");
					// TODO make the image customizable?
					return MutationViewsUtil.renderTablePlaceHolder();
				}
				else
				{
					var portal = MutationDetailsTableFormatter.getCbioPortal(mutation.get("cBioPortal"));

					var vars = {};
					vars.portalFrequency = portal.frequency;
					vars.portalClass = portal.style;

					var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_cbio_portal_template");
					return templateFn(vars);
				}
			}
		},
		// default tooltip functions
		columnTooltips: {
			"simple": function(selector, helper) {
				var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();
				cbio.util.addTargetedQTip($(selector).find('.simple-tip'), qTipOptions);

				//tableSelector.find('.best_effect_transcript').qtip(qTipOptions);
				//tableSelector.find('.cc-short-study-name').qtip(qTipOptions);
				//$('#mutation_details .mutation_details_table td').qtip(qTipOptions);
			},
			"cosmic": function(selector, helper) {
				var gene = helper.gene;
				var mutationUtil = helper.mutationUtil;
				var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

				// add tooltip for COSMIC value
				$(selector).find('.mutation_table_cosmic').each(function() {
					var label = this;
					var mutationId = $(label).closest("tr.mutation-table-data-row").attr("id");
					var mutation = mutationUtil.getMutationIdMap()[mutationId];

					// copy default qTip options and modify "content" to customize for cosmic
					var qTipOptsCosmic = {};
					jQuery.extend(true, qTipOptsCosmic, qTipOptions);

					qTipOptsCosmic.content = {text: "NA"}; // content is overwritten on render
					qTipOptsCosmic.events = {render: function(event, api) {
						var model = {cosmic: mutation.get("cosmic"),
							keyword: mutation.get("keyword"),
							geneSymbol: gene,
							total: $(label).text()};

						var container = $(this).find('.qtip-content');

						// create & render cosmic tip view
						var cosmicView = new CosmicTipView({el: container, model: model});
						cosmicView.render();
					}};

					cbio.util.addTargetedQTip(label, qTipOptsCosmic);
				});
			},
			"mutationAssessor": function(selector, helper) {
				var gene = helper.gene;
				var mutationUtil = helper.mutationUtil;
				var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

				// add tooltip for Predicted Impact Score (FIS)
				$(selector).find('.oma_link').each(function() {
					var mutationId = $(this).closest("tr.mutation-table-data-row").attr("id");
					var mutation = mutationUtil.getMutationIdMap()[mutationId];
					var fis = MutationDetailsTableFormatter.getFis(
						mutation.get("functionalImpactScore"), mutation.get("fisValue"));

					// copy default qTip options and modify "content"
					// to customize for predicted impact score
					var qTipOptsOma = {};
					jQuery.extend(true, qTipOptsOma, qTipOptions);

					qTipOptsOma.content = {text: "NA"}; // content is overwritten on render
					qTipOptsOma.events = {render: function(event, api) {
						// TODO this is a quickfix for dead getma.org links,
						// need to update corresponding data sources properly
						var model = {
							impact: fis.value,
							xvia: mutation.get("xVarLink").replace("getma.org", "mutationassessor.org/r2"),
							msaLink: mutation.get("msaLink").replace("getma.org", "mutationassessor.org/r2"),
							pdbLink: mutation.get("pdbLink").replace("getma.org", "mutationassessor.org/r2")
						};

						var container = $(this).find('.qtip-content');

						// create & render FIS tip view
						var fisTipView = new PredictedImpactTipView({el:container, model: model});
						fisTipView.render();
					}};

					cbio.util.addTargetedQTip(this, qTipOptsOma);
				});
			},
			"cBioPortal": function(selector, helper) {
				var gene = helper.gene;
				var mutationUtil = helper.mutationUtil;
				var portalProxy = helper.dataProxies.portalProxy;
				var mutationTable = helper.table;

				var addTooltip = function (frequencies, cancerStudyMetaData, cancerStudyName)
				{
					$(selector).find('.mutation_table_cbio_portal').each(function(idx, ele) {
						var mutationId = $(this).closest("tr.mutation-table-data-row").attr("id");
						var mutation = mutationUtil.getMutationIdMap()[mutationId];
						var cancerStudy = cancerStudyName || mutation.get("cancerStudy");

						cbio.util.addTargetedQTip(ele, {
							content: {text: 'pancancer mutation bar chart is broken'},
							events: {
								render: function(event, api) {
									var model = {pancanMutationFreq: frequencies,
										cancerStudyMetaData: cancerStudyMetaData,
										cancerStudyName: cancerStudy,
										geneSymbol: gene,
										keyword: mutation.get("keyword"),
										proteinPosStart: mutation.get("proteinPosStart"),
										mutationType: mutation.get("mutationType"),
										qtipApi: api};

									//var container = $(this).find('.qtip-content');
									var container = $(this);

									// create & render the view
									var pancanTipView = new PancanMutationHistTipView({el:container, model: model});
									pancanTipView.render();
								}
							},
							hide: {fixed: true, delay: 100 },
							style: {classes: 'qtip-light qtip-rounded qtip-shadow', tip: true},
							position: {my:'center right',at:'center left',viewport: $(window)}
						});
					});
				};

				if (mutationTable.getCustomData()["cBioPortal"] != null)
				{
					// TODO always get the cancerStudyName from the mutation data?
					portalProxy.getPortalData(
						{cancerStudyMetaData: true, cancerStudyName: true}, function(portalData) {
							addTooltip(mutationTable.getCustomData()["cBioPortal"],
							           portalData.cancerStudyMetaData,
							           portalData.cancerStudyName);
					});
				}
			}
		},
		// default event listener config
		// TODO add more params if necessary
		eventListeners: {
			"windowResize": function(dataTable, dispatcher, mutationUtil, gene) {
				// add resize listener to the window to adjust column sizing
				$(window).one('resize', function () {
					if (dataTable.is(":visible"))
					{
						dataTable.fnAdjustColumnSizing();
					}
				});
			},
			"igvLink": function(dataTable, dispatcher, mutationUtil, gene) {
				// add click listener for each igv link to get the actual parameters
				// from another servlet
				$(dataTable).find('.igv-link').off("click").on("click", function(evt) {
					evt.preventDefault();

					var mutationId = $(this).closest("tr.mutation-table-data-row").attr("id");
					var mutation = mutationUtil.getMutationIdMap()[mutationId];
					var url = mutation.get("igvLink");

					// get parameters from the server and call related igv function
					$.getJSON(url, function(data) {
						prepIGVLaunch(data.bamFileUrl,
						              data.encodedLocus,
						              data.referenceGenome,
						              data.trackName);
					});
				});
			},
			"proteinChange3d": function(dataTable, dispatcher, mutationUtil, gene) {
				// add click listener for each 3D link
				$(dataTable).find('.mutation-table-3d-link').off("click").on("click", function(evt) {
					evt.preventDefault();

					var mutationId = $(this).closest("tr.mutation-table-data-row").attr("id");

					dispatcher.trigger(
						MutationDetailsEvents.PDB_LINK_CLICKED,
						mutationId);
				});
			},
			"proteinChange": function(dataTable, dispatcher, mutationUtil, gene) {
				// add click listener for each protein change link
				$(dataTable).find('.mutation-table-protein-change a').off("click").on("click", function(evt) {
					evt.preventDefault();

					var mutationId = $(this).closest("tr.mutation-table-data-row").attr("id");

					dispatcher.trigger(
						MutationDetailsEvents.PROTEIN_CHANGE_LINK_CLICKED,
						mutationId);
				});
			}
		},
		// column sort functions:
		// returns the value to be used for column sorting purposes.
		// if no sort function is defined for a column,
		// then uses the render function for sorting purposes.
		columnSort: {
			"mutationId": function(datum) {
				var mutation = datum.mutation;
				if (mutation.get("mutationId") === undefined) {
					return "";
				}
				return mutation.get("mutationId");
			},
			"mutationSid": function(datum) {
				var mutation = datum.mutation;
				if (mutation.get("mutationSid") === undefined) {
					return "";
				}
				return mutation.get("mutationSid");
			},
			"caseId": function(datum) {
				var mutation = datum.mutation;
				if (mutation.get("caseId") === undefined) {
					return "";
				}
				return mutation.get("caseId");
			},
			"proteinChange": function(datum) {
				var proteinChange = datum.mutation.get("proteinChange");
				//var matched = proteinChange.match(/.*[A-Z]([0-9]+)[^0-9]+/);
				var alleleAndPosition = /[A-Za-z][0-9]+./g;
				var position = /[0-9]+/g;
				var nonNumerical = /[^0-9]+/g;

				var extractNonNumerical = function(matched) {
					// this is to sort alphabetically
					// in case the protein position values are the same
					var buffer = matched[0].match(nonNumerical);

					if (buffer && buffer.length > 0)
					{
						var str = buffer.join("");
						buffer = [];

						// since we are returning a float value
						// assigning numerical value for each character.
						// we have at most 2 characters, so this should be safe...
						for (var i=0; i<str.length; i++)
						{
							buffer.push(str.charCodeAt(i));
						}
					}

					return buffer;
				};

				// first priority is to match values like V600E , V600, E747G, E747, X37_, X37, etc.
				var matched = proteinChange.match(alleleAndPosition);
				var buffer = [];

				// if no match, then search for numerical (position) match only
				if (!matched || matched.length === 0)
				{
					matched = proteinChange.match(position);
				}
				// if match, then extract the first numerical value for sorting purposes
				else
				{
					// this is to sort alphabetically
					buffer = extractNonNumerical(matched);
					matched = matched[0].match(position);
				}

				// if match, then use the first integer value as sorting data
				if (matched && matched.length > 0)
				{
					var toParse =  matched[0];

					// this is to sort alphabetically
					if (buffer && buffer.length > 0)
					{
						// add the alphabetical information as the decimal part...
						// (not the best way to ensure alphabetical sorting,
						// but in this method we are only allowed to return a numerical value)
						toParse += "." + buffer.join("");
					}

					return parseFloat(toParse);
				}
				else
				{
					// no match at all: do not sort
					return -Infinity;
				}
			},
			"cancerStudy": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("cancerStudy");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"tumorType": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("tumorType");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"mutationType": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("mutationType");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"cosmic": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.getCosmicCount());
			},
			"cna": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.get("cna"));
			},
			"mutationCount": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.get("mutationCount"));
			},
			"normalFreq": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignFloatValue(mutation.get("normalFreq"));
			},
			"tumorFreq": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignFloatValue(mutation.get("tumorFreq"));
			},
			"mutationAssessor": function(datum) {
				var mutation = datum.mutation;

				return MutationDetailsTableFormatter.assignValueToPredictedImpact(
					mutation.get("functionalImpactScore"),
					mutation.get("fisValue"));
			},
			"mutationStatus": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("mutationStatus");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"validationStatus": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("validationStatus");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"normalRefCount": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.get("normalRefCount"));
			},
			"normalAltCount": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.get("normalAltCount"));
			},
			"tumorRefCount": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.get("tumorRefCount"));
			},
			"tumorAltCount": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.get("tumorAltCount"));
			},
			"startPos": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.get("startPos"));
			},
			"endPos": function(datum) {
				var mutation = datum.mutation;
				return MutationDetailsTableFormatter.assignIntValue(mutation.get("endPos"));
			},
			"sequencingCenter": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("sequencingCenter");
				if (value === undefined) {
					value = "";
				}
				return value;
			},
			"chr": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("chr");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"referenceAllele": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("referenceAllele");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"variantAllele": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("variantAllele");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"igvLink": function(datum) {
				var mutation = datum.mutation;
				var value = mutation.get("igvLink");
				if (value === undefined) {
					return "";
				}
				return value;
			},
			"cBioPortal": function(datum) {
				var portal = datum.cBioPortal;

				// portal value may be null,
				// because we are retrieving it through another ajax call...
				return portal || 0;
			}
		},
		// column filter functions:
		// returns the value to be used for column sorting purposes.
		// if no filter function is defined for a column,
		// then uses the sort function value for filtering purposes.
		// if no sort function is defined either, then uses
		// the value returned by the render function.
		columnFilter: {
			"proteinChange": function(datum) {
				return datum.mutation.get("proteinChange") || "";
			},
			"mutationType": function(datum) {
				// use display value for mutation type, not the sort value
				var mutationType = MutationDetailsTableFormatter.getMutationType(
					datum.mutation.get("mutationType"));

				return mutationType.text;
			},
			"cosmic": function(datum) {
				return datum.mutation.getCosmicCount() || "";
			},
			"cna": function(datum) {
				return datum.mutation.get("cna") || "";
			},
			"mutationCount": function(datum) {
				return datum.mutation.get("mutationCount") || "";
			},
			"normalFreq": function(datum) {
				return datum.mutation.get("normalFreq") || "";
			},
			"tumorFreq": function(datum) {
				return datum.mutation.get("tumorFreq") || "";
			},
			"mutationAssessor": function(datum) {
				return datum.mutation.get("functionalImpactScore") || "";
			},
			"normalRefCount": function(datum) {
				return datum.mutation.get("normalRefCount") || "";
			},
			"normalAltCount": function(datum) {
				return datum.mutation.get("normalAltCount") || "";
			},
			"tumorRefCount": function(datum) {
				return datum.mutation.get("tumorRefCount") || "";
			},
			"tumorAltCount": function(datum) {
				return datum.mutation.get("tumorAltCount") || "";
			},
			"startPos": function(datum) {
				return datum.mutation.get("startPos") || "";
			},
			"endPos": function(datum) {
				return datum.mutation.get("endPos") || "";
			}
		},
		// native "mData" function for DataTables plugin. if this is implemented,
		// functions defined in columnRender and columnSort will be ignored.
		// in addition to default source, type, and val parameters,
		// another parameter "indexMap" will also be passed to the function.
		columnData: {
			// not implemented by default:
			// default config relies on columnRender,
			// columnSort, and columnFilter functions
		},
		// delay amount before applying the user entered filter query
		filteringDelay: 600,
		// WARNING: overwriting advanced DataTables options such as
		// aoColumnDefs, oColVis, and fnDrawCallback may break column
		// visibility, sorting, and filtering. Proceed wisely ;)
		dataTableOpts: {
			"sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t<"F"<"mutation_datatables_download"T>>',
			"bJQueryUI": true,
			"bPaginate": false,
			//"sPaginationType": "two_button",
			"bFilter": true,
			"sScrollY": "600px",
			"bScrollCollapse": true,
			"oLanguage": {
				"sInfo": "Showing _TOTAL_ mutation(s) in <span class='mutation-table-samples-info'></span> sample(s)",
				"sInfoFiltered": "(out of _MAX_ total mutations)",
				"sInfoEmpty": "No mutations to show"
			}
		}
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AdvancedDataTable.call(this, _options);
	_options = self._options;

	// custom event dispatcher
	var _dispatcher = self._dispatcher;

	// flag used to switch filter event on/off
	var _filterEventActive = true;

	// this is used to check if search string is changed after each redraw
	var _prevSearch = "";

	// last search string manually entered by the user
	var _manualSearch = "";

	var _rowMap = {};

	var _selectedRow = null;

	// optional table specific data
	var _customData = {};

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
	 * @private
	 */
	function initDataTableOpts(tableSelector, rows, columnOpts, nameMap,
		indexMap, hiddenCols, excludedCols, nonSearchableCols)
	{
		// generate column options for the data table
		var columns = DataTableUtil.getColumnOptions(columnOpts,
			indexMap);

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
			"oTableTools": {
				"aButtons": [{
					"sExtends": "text",
					"sButtonText": "Download",
					"mColumns": getExportColumns(columnOpts, excludedCols),
					"fnCellRender": function(sValue, iColumn, nTr, iDataIndex) {
						// return actual data value for sample id column,
						// since we show truncated data values when it is too long
						if (iColumn === indexMap["caseId"])
						{
							var rowData = self.getDataTable().fnGetData()[iDataIndex];
							return rowData[0].mutation.get("caseId");
						}

						var value = sValue;

						// strip HTML content and use the main (visible) text only
						if(sValue.indexOf("<") != -1 &&
						   sValue.indexOf(">") != -1)
						{
							value = $(sValue).text();
						}

						// also remove the text of "3D" link from the protein change column
						if (iColumn === indexMap["proteinChange"])
						{
							value = value.replace(/(\s)3D/, '');
						}

						return value.trim();
					},
					"fnClick": function(nButton, oConfig) {
						// get the file data (formatted by 'fnCellRender' function)
						var content = this.fnGetTableData(oConfig);

						var downloadOpts = {
							filename: "mutation_table_" + gene + ".tsv",
							contentType: "text/plain;charset=utf-8",
							preProcess: false};

						// send download request with filename & file content info
						cbio.download.initDownload(content, downloadOpts);
					}
				}]
			},
			"fnDrawCallback": function(oSettings) {
				self._addColumnTooltips({gene: gene,
					mutationUtil: mutationUtil,
					dataProxies: dataProxies,
					table: self});
				self._addEventListeners(indexMap);

				var currSearch = oSettings.oPreviousSearch.sSearch;

				// trigger the event only if the corresponding flag is set
				// and there is a change in the search term
				if (_filterEventActive &&
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

				// trigger redraw event
				_dispatcher.trigger(
					MutationDetailsEvents.MUTATION_TABLE_REDRAWN,
					tableSelector);

				// get the unique number of samples for the current visible data
				var rowData = $(tableSelector).DataTable().rows({filter: "applied"}).data();
				$(oSettings.nTableWrapper).find('.mutation-table-samples-info').text(
					_.size(uniqueSamples(rowData)));

				// TODO this may not be safe: prevent rendering of invalid links in the corresponding render function
				// remove invalid links
				$(tableSelector).find('a[href=""]').remove();

				// remove invalid protein change tips
				$(tableSelector).find('span.mutation-table-additional-protein-change[alt=""]').remove();
			},
			"fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
				var mutation = aData[indexMap["datum"]].mutation;
				// TODO mapping on mutationId and mutationSid...
				//var key = mutation.mutationId;
				//_rowMap[key] = nRow;
				$(nRow).attr("id", mutation.get("mutationId"));
				$(nRow).addClass(mutation.get("mutationSid"));
				$(nRow).addClass("mutation-table-data-row");
			},
			//"fnCreatedRow": function(nRow, aData, iDataIndex) {
			//
			//},
			"fnInitComplete": function(oSettings, json) {
				//$(tableSelector).find('a[href=""]').remove();
				//$(tableSelector).find('a[alt=""]').remove();
				//$(tableSelector).find('a.igv-link[alt=""]').remove();

				// TODO append the footer
				// (there is no API to init the footer, we need a custom function)
				//$(tableSelector).append('<tfoot></tfoot>');
				//$(tableSelector).find('thead tr').clone().appendTo($(tableSelector).find('tfoot'));

				// set the data table instance as soon as the table is initialized
				self.setDataTable(this);

				// 508 compliance: add a title to each of the checkboxes provided by
				// the ColVis library. As the offending checkboxes don't become visible
				// until the button is clicked, bind it to the click event
				$(oSettings.nTableWrapper).find(".ColVis_MasterButton").one("click", function() {
					jQuery.each($(".ColVis_radio"), function(key, value) {
						// title is the first sibling's text
						var title = $(value).siblings(':first').text();
						$(value).children(':first').attr('title', title);
					});
				});

				// trigger corresponding event
				_dispatcher.trigger(
					MutationDetailsEvents.MUTATION_TABLE_INITIALIZED,
					tableSelector);
			},
			"fnHeaderCallback": function(nHead, aData, iStart, iEnd, aiDisplay) {
			    $(nHead).find('th').addClass("mutation-details-table-header");
				self._addHeaderTooltips(nHead, nameMap);

				//Trigger fnHeader callback function
				_dispatcher.trigger(
					MutationDetailsEvents.MUTATION_TABLE_HEADER_CREATED,
					tableSelector);
		    }
//		    "fnFooterCallback": function(nFoot, aData, iStart, iEnd, aiDisplay) {
//			    addFooterTooltips(nFoot, nameMap);
//		    }
		};

		return tableOpts;
	}

	/**
	 * Creates an array of indices for the columns to be exported for download.
	 *
	 * @param columnOpts    basic column options
	 * @param excludedCols  indices of the excluded columns
	 * @returns {Array}     an array of column indices
	 */
	function getExportColumns(columnOpts, excludedCols)
	{
		var exportCols = [];

		for (var i = 0; i <= _.keys(columnOpts).length; i++) {
			exportCols.push(i);
		}

		return _.difference(exportCols, excludedCols);
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

	/**
	 * Determines the search value for the given column name
	 *
	 * @param columnName    name of the column (header)
	 * @return {Boolean}    whether searchable or not
	 */
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
	 * Adds default event listeners for the table.
	 *
	 * @param indexMap  column index map
	 */
	function addEventListeners(indexMap)
	{
		// add listeners only if the data table is initialized
		if (self.getDataTable() != null)
		{
			_.each(_options.eventListeners, function(listenerFn) {
				listenerFn(self.getDataTable(), _dispatcher, mutationUtil, gene);
			});
		}
	}

	function selectRow(mutationId)
	{
		// remove previous highlights
		removeAllSelection();

		// highlight selected
		var nRow = _rowMap[mutationId];
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

	/**
	 * Enables/disables event triggering.
	 *
	 * @param active    boolean value
	 */
	function setFilterEventActive(active)
	{
		_filterEventActive = active;
	}

	/**
	 * Resets filtering related variables to their initial state.
	 * Does not remove actual table filters.
	 */
	function cleanFilters()
	{
		_prevSearch = "";
		_manualSearch = "";
	}

	function getManualSearch()
	{
		return _manualSearch;
	}

	/**
	 * Adds tooltips for the table header cells.
	 *
	 * @param nHead     table header
	 * @param nameMap   map of <column display name, column name>
	 * @private
	 */
	function addHeaderTooltips(nHead, nameMap)
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

		var qTipOptionsHeader = {};
		jQuery.extend(true, qTipOptionsHeader, qTipOptions);
		qTipOptionsHeader.position = {my:'bottom center', at:'top center', viewport: $(window)};

		//tableSelector.find('thead th').qtip(qTipOptionsHeader);
		$(nHead).find("th").each(function(){
			var displayName = $(this).text();
			var colName = nameMap[displayName];

			if (colName != null)
			{
				var tip = _options.columns[colName].tip;
				var opts = {};

				// if string, convert to an object
				if(_.isString(tip))
				{
					//$(this).attr("alt", tip);
					tip = {content: tip};
				}

				// merge qTip options with the provided options object
				jQuery.extend(true, opts, qTipOptionsHeader, tip);

				//$(this).qtip(opts);
				cbio.util.addTargetedQTip(this, opts);
			}
		});
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
		qTipOptionsFooter.position = {my:'top center', at:'bottom center', viewport: $(window)};

		cbio.util.addTargetedQTip($(nFoot).find("th"), qTipOptionsFooter);
	}

	// class instance to keep track of previous requests
	var _requestHistory = {};

	/**
	 * Requests column data from the data manager for the given data field name,
	 * and updates the corresponding column.
	 *
	 * @param dataFnName    data function name for data manager request
	 * @param columnName    name of the column to be updated/rendered
	 * @param callback      [optional] callback to be invoked after data retrieval
	 */
	function requestColumnData(dataFnName, columnName, callback)
	{
		columnName = columnName || dataFnName;

		// do not request data at all for excluded columns, and
		// only request once for the same dataFnName and columnName combination
		if (self._visiblityMap[columnName] === "excluded" ||
			_requestHistory[dataFnName + ":" + columnName])
		{
			return;
		}
		else
		{
			_requestHistory[dataFnName + ":" + columnName] = true;
		}

		callback = callback || function(params, data) {
			var mutationTable = params.mutationTable;

			// TODO is this the right place to store the custom table data?
			if (data)
			{
				self.getCustomData()[dataFnName] = data;
			}

			MutationViewsUtil.refreshTableColumn(
				mutationTable.getDataTable(),
				mutationTable.getIndexMap(),
				columnName);
		};

		function getColumnData()
		{
			_dispatcher.off(
				MutationDetailsEvents.MUTATION_TABLE_INITIALIZED,
				getColumnData);

			// get the pdb data for the entire table
			dataManager.getData(dataFnName,
				{mutationTable: self},
				// TODO instead of a callback,
				// listen to the data change/update events, and update the corresponding column?
			    callback
			);
		}

		// if table is not initialized yet, wait for the init event
		if (self.getDataTable() == null)
		{
			_dispatcher.on(
				MutationDetailsEvents.MUTATION_TABLE_INITIALIZED,
				getColumnData);
		}
		else
		{
			getColumnData();
		}
	}

	function uniqueSamples(rowData)
	{
		var samples = {};

		_.each(rowData, function(data, index) {
			// assuming only the first element contains the datum
			var mutation = data[0].mutation;

			if (mutation &&
			    !_.isEmpty(mutation.get('caseId')))
			{
				samples[mutation.get('caseId').toLowerCase()] = true;
			}
		});

		return samples;
	}

	function getMutations()
	{
		var mutations = null;

		if (mutationUtil)
		{
			mutations = mutationUtil.getMutations();
		}

		return mutations;
	}

	function getCustomData()
	{
		return _customData;
	}

	function getMutationUtil()
	{
		return mutationUtil;
	}

	function getGene()
	{
		return gene;
	}

	// override required functions
	this._initDataTableOpts = initDataTableOpts;
	this._visibilityValue = visibilityValue;
	this._searchValue = searchValue;
	this._addEventListeners = addEventListeners;
	this._addHeaderTooltips = addHeaderTooltips;

	// additional public functions
	this.setFilterEventActive = setFilterEventActive;
	this.getManualSearch = getManualSearch;
	this.cleanFilters = cleanFilters;
	this.requestColumnData = requestColumnData;
	this.getCustomData = getCustomData;
	this.getMutations = getMutations;
	this.getMutationUtil = getMutationUtil;
	this.getGene = getGene;

	//this.selectRow = selectRow;
	//this.getSelectedRow = getSelectedRow;
	this.dispatcher = this._dispatcher;
}

// MutationDetailsTable extends AdvancedDataTable...
MutationDetailsTable.prototype = new AdvancedDataTable();
MutationDetailsTable.prototype.constructor = MutationDetailsTable;


/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Constructor for the MutationDiagram class.
 *
 * @param geneSymbol    hugo gene symbol
 * @param options       visual options object
 * @param data          object: {mutations: a MutationCollection instance,
 *                               sequence: sequence data as a JSON object}
 * @param dataProxies   all available data proxies
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationDiagram(geneSymbol, options, data, dataProxies)
{
	var self = this;

	// event listeners
	self.listeners = {};

	// custom event dispatcher
	self.dispatcher = {};
	_.extend(self.dispatcher, Backbone.Events);

	// merge options with default options to use defaults for missing values
	self.options = jQuery.extend(true, {}, self.defaultOpts, options);

	self.dataProxies = dataProxies;
	self.geneSymbol = geneSymbol; // hugo gene symbol
	self.data = data; // processed initial (unfiltered) data
	self.pileups = (data == null) ? null : // current pileups (updated after each filtering)
		PileupUtil.convertToPileups(data.mutations, options.pileupConverter);
	self.initialPileups = self.pileups;
	self.highlighted = {}; // map of highlighted data points (initially empty)
	self.multiSelect = false; // indicates if multiple lollipop selection is active

	// init other class members as null, will be assigned later
	self.svg = null;    // svg element (d3)
	self.bounds = null; // bounds of the plot area
	self.gData = null; // svg group for lollipop data points
	self.gLine = null;   // svg group for lollipop lines
	self.gLabel = null;  // svg group for lollipop labels
	self.xScale = null;  // scale function for x-axis
	self.yScale = null;  // scale function for y-axis
	self.topLabel = null;   // label on top-left corner of the diagram
	self.xAxisLabel = null; // label for x-axis
	self.yAxisLabel = null; // label for y-axis
	self.xMax = null; // max value on the x-axis
	self.yMax = null; // max value on the y-axis
	self.maxCount = null; // mutation count of the highest data point

	// color mapping for mutations: <mutation id, (pileup) color> pairs
	self.mutationColorMap = {};

	// mutation id to pileup mapping: <mutation sid, pileup group> pairs
	self.mutationPileupMap = {};
}

// TODO use percent values instead of pixel values for some components?
// TODO allow "auto" or a function as an option where applicable

/**
 * Default visual options.
 */
MutationDiagram.prototype.defaultOpts = {
	el: "#mutation_diagram_d3", // id of the container
	elWidth: 740,               // width of the container
	elHeight: 180,              // height of the container
	marginLeft: 45,             // left margin for the plot area
	marginRight: 30,            // right margin for the plot area
	marginTop: 30,              // top margin for the plot area
	marginBottom: 60,           // bottom margin for the plot area
	labelTop: "",                 // informative label on top of the diagram (false means "do not draw")
	labelTopFont: "sans-serif",   // font type of the top label
	labelTopFontColor: "#2E3436", // font color of the top label
	labelTopFontSize: "12px",     // font size of the top label
	labelTopFontWeight: "bold",   // font weight of the top label
	labelTopMargin: 2,            // left margin for the top label
	labelX: false,              // informative label of the x-axis (false means "do not draw")
	labelXFont: "sans-serif",   // font type of the x-axis label
	labelXFontColor: "#2E3436", // font color of the x-axis label
	labelXFontSize: "12px",     // font size of x-axis label
	labelXFontWeight: "normal", // font weight of x-axis label
	labelY: "# Mutations",      // informative label of the y-axis (false means "do not draw")
	labelYFont: "sans-serif",   // font type of the y-axis label
	labelYFontColor: "#2E3436", // font color of the y-axis label
	labelYFontSize: "12px",     // font size of y-axis label
	labelYFontWeight: "normal", // font weight of y-axis label
	minLengthX: 0,              // min value of the largest x value to show
	minLengthY: 5,              // min value of the largest y value to show
	maxLengthX: Infinity,       // max value of the largest x value to show (infinity: no upper value)
	maxLengthY: Infinity,       // max value of the largest y value to show (infinity: no upper value)
	seqFillColor: "#BABDB6",    // color of the sequence rectangle
	seqHeight: 14,              // height of the sequence rectangle
	seqPadding: 5,              // padding between sequence and plot area
	regionHeight: 24,           // height of a region (drawn on the sequence)
	regionFont: "sans-serif",   // font of the region text
	regionFontColor: "#FFFFFF", // font color of the region text
	regionFontSize: "12px",     // font size of the region text
	regionTextAnchor: "middle", // text anchor (alignment) for the region label
	showRegionText: true,       // show/hide region text
	showStats: false,           // show/hide mutation stats in the lollipop tooltip
	multiSelectKeycode: 16,     // shift (default multiple selection key)
	lollipopLabelCount: 1,          // max number of lollipop labels to display
	lollipopLabelThreshold: 2,      // y-value threshold: points below this value won't be labeled
	lollipopFont: "sans-serif",     // font of the lollipop label
	lollipopFontColor: "#2E3436",   // font color of the lollipop label
	lollipopFontSize: "10px",       // font size of the lollipop label
	lollipopTextAnchor: "auto",     // text anchor (alignment) for the lollipop label
	lollipopTextPadding: 8,         // padding between the label and the data point
	lollipopTextAngle: 0,           // rotation angle for the lollipop label
//	lollipopFillColor: "#B40000",
	lollipopFillColor: {            // color of the lollipop data point
		missense: "#008000",
		truncating: "#000000",
		inframe: "#8B4513",
		fusion: "#8B00C9",
		other: "#8B00C9",       // all other mutation types
		default: "#BB0000"      // default is used when there is a tie
	},
	lollipopBorderColor: "#BABDB6", // border color of the lollipop data points
	lollipopBorderWidth: 0.5,       // border width of the lollipop data points
	lollipopSize: 30,               // size of the lollipop data points
	lollipopHighlightSize: 100,     // size of the highlighted lollipop data points
	lollipopStrokeWidth: 1,         // width of the lollipop lines
	lollipopStrokeColor: "#BABDB6", // color of the lollipop line
	lollipopShapeRegular: "circle", // shape of the regular lollipop data points
	lollipopShapeSpecial: "circle", // shape of the special lollipop data points
	xAxisPadding: 10,           // padding between x-axis and the sequence
	xAxisTickIntervals: [       // valid major tick intervals for x-axis
		100, 200, 400, 500, 1000, 2000, 5000, 10000, 20000, 50000
	],
	xAxisTicks: 8,              // maximum number of major ticks for x-axis
								// (a major tick may not be labeled if it is too close to the max)
	xAxisTickSize: 6,           // size of the major ticks of x-axis
	xAxisStroke: "#AAAAAA",     // color of the x-axis lines
	xAxisFont: "sans-serif",    // font type of the x-axis labels
	xAxisFontSize: "10px",      // font size of the x-axis labels
	xAxisFontColor: "#2E3436",  // font color of the x-axis labels
	yAxisPadding: 5,            // padding between y-axis and the plot area
	yAxisLabelPadding: 15,      // padding between y-axis and its label
	yAxisTicks: 10,             // maximum number of major ticks for y-axis
	yAxisTickIntervals: [       // valid major tick intervals for y-axis
		1, 2, 5, 10, 20, 50, 100, 200, 500
	],
	yAxisTickSize: 6,           // size of the major ticks of y-axis
	yAxisStroke: "#AAAAAA",     // color of the y-axis lines
	yAxisFont: "sans-serif",    // font type of the y-axis labels
	yAxisFontSize: "10px",      // font size of the y-axis labels
	yAxisFontColor: "#2E3436",  // font color of the y-axis labels
	yAxisAutoAdjust: true,      // indicates whether to adjust max y-axis value after plot update
	animationDuration: 1000,    // transition duration (in ms) used for highlight animations
	fadeDuration: 1500,         // transition duration (in ms) used for fade animations
	pileupConverter: false,
	/**
	 * Default lollipop tooltip function.
	 *
	 * @param element   target svg element (lollipop data point)
	 * @param pileup    a pileup model instance
     * @param showStats whether to show cancer type distribution in the tooltip
	 */
	lollipopTipFn: function (element, pileup, showStats) {
		var tooltipView = new LollipopTipView({model: pileup});
        tooltipView.setShowStats(showStats);
		var content = tooltipView.compileTemplate();

		var options = {content: {text: content},
			hide: {fixed: true, delay: 100, event: 'mouseout'},
			show: {event: 'mouseover'},
			style: {classes: 'qtip-light qtip-rounded qtip-shadow cc-ui-tooltip'},
			position: {my:'bottom left', at:'top center',viewport: $(window)}};

		//$(element).qtip(options);
		cbio.util.addTargetedQTip(element, options);
	},
	/**
	 * Default region tooltip function.
	 *
	 * @param element   target svg element (region rectangle)
	 * @param region    a JSON object representing the region
	 * @param maProxy   mutation aligner proxy for additional region data
	 */
	regionTipFn: function (element, region, maProxy) {
		var model = {identifier: region.metadata.identifier,
			type: region.type,
			description: region.metadata.description,
			start: region.metadata.start,
			end: region.metadata.end,
			pfamAccession: region.metadata.accession,
			mutationAlignerInfo: ""};

		maProxy.getMutationAlignerData(
			{pfamAccession: region.metadata.accession},
			function(data) {
				// if the link is valid update model.mutationAligner
				if (data != null &&
				    data.linkToMutationAligner != null &&
				    data.linkToMutationAligner.length > 0)
				{
					var templateFn = BackboneTemplateCache.getTemplateFn("mutation_aligner_info_template");
					model.mutationAlignerInfo = templateFn({
						linkToMutationAligner: data.linkToMutationAligner
					});
				}

				var tooltipView = new RegionTipView({model: model});
				var content = tooltipView.compileTemplate();

				var options = {content: {text: content},
					hide: {fixed: true, delay: 100, event: 'mouseout'},
					show: {event: 'mouseover'},
					style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'},
					position: {my:'bottom left', at:'top center',viewport: $(window)}};

				//$(element).qtip(options);
				cbio.util.addTargetedQTip(element, options);
			}
		);
	}
};

/**
 * Updates the diagram options object with the given one.
 * This function does not update (re-render) the actual view
 * with the new options. only updates some class fields.
 *
 * @param options   diagram options object
 */
MutationDiagram.prototype.updateOptions = function(options)
{
	var self = this;

	// merge options with current options to use existing ones for missing values
	self.options = jQuery.extend(true, {}, self.options, options);

	// recalculate global values
	self.updateGlobals();
};

/**
 * Rescales the y-axis by using the updated options and
 * latest (filtered) data.
 *
 * @param noUpdatePlot if set true, plot contents are NOT updated
 */
MutationDiagram.prototype.rescaleYAxis = function(noUpdatePlot)
{
	var self = this;

	// recalculate global values
	self.updateGlobals();

	// remove & draw y-axis
	self.svg.select(".mut-dia-y-axis").remove();
	self.drawYAxis(self.svg, self.yScale, self.yMax, self.options, self.bounds);

	if (!noUpdatePlot)
	{
		// re-draw the plot with new scale
		self.updatePlot();
	}
};


/**
 * Update global class fields such as bounds, scales, max, etc.
 * wrt the given options.
 *
 * @param options   diagram options
 */
MutationDiagram.prototype.updateGlobals = function(options)
{
	var self = this;
	options = options || self.options;

	var pileups = self.initialPileups; // initial pileup data

	// in case auto adjust is enabled,
	// use current pileup data instead of the initial pileup data
	if (options.yAxisAutoAdjust)
	{
		pileups = self.pileups;
	}

	var maxCount = self.maxCount = self.calcMaxCount(pileups);

	var xMax = self.xMax = self.calcXMax(options, self.data);
	var yMax = self.yMax = self.calcYMax(options, maxCount);

	self.bounds = this.calcBounds(options);
	self.xScale = this.xScaleFn(self.bounds, xMax);
	self.yScale = this.yScaleFn(self.bounds, yMax);
};

/**
 * Updates the sequence data associated with this diagram.
 *
 * @param sequenceData  sequence data as a JSON object
 */
MutationDiagram.prototype.updateSequenceData = function(sequenceData)
{
	var self = this;

	self.data.sequence = sequenceData;
};

/**
 * Initializes the diagram with the given sequence data.
 * If no sequence data is provided, then tries to retrieve
 * the data from the default servlet.
 */
MutationDiagram.prototype.initDiagram = function()
{
	var self = this;

	// selecting using jQuery node to support both string and jQuery selector values
	var node = $(self.options.el)[0];
	var container = d3.select(node);

	// calculate bounds & save a reference for future access
	var bounds = self.bounds = self.calcBounds(self.options);

	self.mutationPileupMap = PileupUtil.mapToMutations(self.initialPileups);

	// init svg container
	var svg = self.createSvg(container,
	                         self.options.elWidth,
	                         self.options.elHeight);

	// save a reference for future access
	self.svg = svg;

	// draw the whole diagram
	self.drawDiagram(svg,
	                 bounds,
	                 self.options,
	                 self.data);

	// add default listeners
	self.addDefaultListeners();
};

/**
 * Calculates the bounds of the actual plot area excluding
 * axes, sequence, labels, etc. So, this is the bounds for
 * the data points (lollipops) only.
 *
 * @param options   options object
 * @return {object} bounds as an object
 */
MutationDiagram.prototype.calcBounds = function(options)
{
	var bounds = {};

	bounds.width = options.elWidth -
	               (options.marginLeft + options.marginRight);
	bounds.height = options.elHeight -
	                (options.marginBottom + options.marginTop);
	bounds.x = options.marginLeft;
	bounds.y = options.elHeight - options.marginBottom;

	return bounds;
};

/**
 * Draws the mutation diagram.
 *
 * @param svg       svg container for the diagram
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param options   options object
 * @param data      data to visualize
 */
MutationDiagram.prototype.drawDiagram = function (svg, bounds, options, data)
{
	var self = this;
	var sequenceLength = parseInt(data.sequence["length"]);
	var pileups = self.initialPileups || PileupUtil.convertToPileups(data.mutations, options.pileupConverter);

	var maxCount = self.maxCount = self.calcMaxCount(pileups);
	var xMax = self.xMax = self.calcXMax(options, data);
	var yMax = self.yMax = self.calcYMax(options, maxCount);

	var regions = data.sequence.regions;
	var seqTooltip = self.generateSequenceTooltip(data);

	var xScale = self.xScale = self.xScaleFn(bounds, xMax);
	var yScale = self.yScale = self.yScaleFn(bounds, yMax);

	// draw x-axis
	self.drawXAxis(svg, xScale, xMax, options, bounds);

	if (options.labelX != false)
	{
		//TODO self.xAxisLabel = self.drawXAxisLabel(svg, options, bounds);
	}

	// draw y-axis
	self.drawYAxis(svg, yScale, yMax, options, bounds);

	if (options.labelY != false)
	{
		self.yAxisLabel = self.drawYAxisLabel(svg, options, bounds);
	}

	if (options.topLabel != false)
	{
		self.topLabel = self.drawTopLabel(svg, options, bounds);
	}

	// draw a fully transparent rectangle for proper background click handling
	var rect = svg.append('rect')
		.attr('fill', '#FFFFFF')
		.attr('opacity', 0)
		.attr('x', bounds.x)
		.attr('y', bounds.y - bounds.height)
		.attr('width', bounds.width)
		.attr('height', bounds.height)
		.attr('class', 'mut-dia-background');

	// draw the plot area content
	self.drawPlot(svg,
		pileups,
		options,
		bounds,
		xScale,
		yScale);

	// draw sequence
	var sequence = self.drawSequence(svg, options, bounds);
	// add a regular tooltip (not qtip)
	sequence.attr("title", seqTooltip);

	// draw regions
	for (var i = 0, size = regions.length; i < size; i++)
	{
		self.drawRegion(svg, regions[i], options, bounds, xScale);
	}
};

/**
 * Generates an x-scale function for the current bounds
 * and the max value of the x-axis.
 *
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param max       maximum value for the x-axis
 * @return {function} scale function for the x-axis
 */
MutationDiagram.prototype.xScaleFn = function(bounds, max)
{
	return d3.scale.linear()
		.domain([0, max])
		.range([bounds.x, bounds.x + bounds.width]);
};

/**
 * Generates a y-scale function for the current bounds
 * and the max value of the y-axis.
 *
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param max       maximum value for the y-axis
 * @return {function} scale function for the y-axis
 */
MutationDiagram.prototype.yScaleFn = function(bounds, max)
{
	return d3.scale.linear()
		.domain([0, max])
		.range([bounds.y, bounds.y - bounds.height]);
};

/**
 * Finds out the maximum value for the x-axis.
 *
 * @param options   options object
 * @param data      data to visualize
 * @return {Number} maximum value for the x-axis
 */
MutationDiagram.prototype.calcXMax = function(options, data)
{
	var sequenceLength = parseInt(data.sequence["length"]);

	return Math.min(options.maxLengthX,
		Math.max(sequenceLength, options.minLengthX));
};

/**
 * Finds out the maximum value for the y-axis.
 *
 * @param options   options object
 * @param maxCount  number of mutations in the highest data point
 * @return {Number} maximum value for the y-axis
 */
MutationDiagram.prototype.calcYMax = function(options, maxCount)
{
	return Math.min(options.maxLengthY,
		Math.max(maxCount, options.minLengthY));
};

/**
 * Generates the tooltip content for the sequence rectangle.
 *
 * @param data      data to visualize
 * @return {string} tooltip content
 */
MutationDiagram.prototype.generateSequenceTooltip = function(data)
{
	var seqTooltip = "";
	var sequenceLength = parseInt(data.sequence["length"]);

	if (data.sequence.metadata.identifier)
	{
		seqTooltip += data.sequence.metadata.identifier;

		if (data.sequence.metadata.description)
		{
			seqTooltip += ", " + data.sequence.metadata.description;
		}
	}

	seqTooltip += " (" + sequenceLength + "aa)";

	return seqTooltip;
};

/**
 * Draw lollipop lines, data points and labels on the plot area
 * for the provided mutations (pileups).
 *
 * @param svg       svg container for the diagram
 * @param pileups   array of mutations (pileups)
 * @param options   options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 */
MutationDiagram.prototype.drawPlot = function(svg, pileups, options, bounds, xScale, yScale)
{
	var self = this;

	// group for lollipop labels (draw labels first)
	var gText = self.gLabel;
	if (gText === null)
	{
		gText = svg.append("g").attr("class", "mut-dia-lollipop-labels");
		self.gLabel = gText;
	}

	// group for lollipop lines (lines should be drawn before the data point)
	var gLine = self.gLine;
	if (gLine === null)
	{
		gLine = svg.append("g").attr("class", "mut-dia-lollipop-lines");
		self.gLine = gLine;
	}

	// group for lollipop data points (points should be drawn later)
	var gData = self.gData;
	if (gData === null)
	{
		gData = svg.append("g").attr("class", "mut-dia-lollipop-points");
		self.gData = gData;
	}

	// draw lollipop lines and data points
	for (var i = 0; i < pileups.length; i++)
	{
		self.drawLollipop(gData,
				gLine,
				pileups[i],
				options,
				bounds,
				xScale,
				yScale);
	}

	// draw lollipop labels
	self.drawLollipopLabels(gText, pileups, options, xScale, yScale);
};

/**
 * Creates the main svg (graphical) component.
 *
 * @param container main container (div, etc.)
 * @param width     width of the svg area
 * @param height    height of the svg area
 * @return {object} svg component
 */
MutationDiagram.prototype.createSvg = function (container, width, height)
{
	var svg = container.append("svg");

	svg.attr('width', width);
	svg.attr('height', height);

	return svg;
};

// helper function to calculate major tick interval for the axis
/**
 * Calculates major tick interval for the given possible interval values,
 * maximum value on the axis, and the desired maximum tick count.
 *
 * @param intervals     possible interval values
 * @param maxValue      highest value on the axis
 * @param maxTickCount  desired maximum tick count
 * @return {number}     interval value
 */
MutationDiagram.prototype.calcTickInterval = function(intervals, maxValue, maxTickCount)
{
	var interval = -1;

	for (var i=0; i < intervals.length; i++)
	{
		interval = intervals[i];
		var count = maxValue / interval;

		//if (Math.round(count) <= maxLabelCount)
		if (count < maxTickCount - 1)
		{
			break;
		}
	}

	return interval;
};

/**
 * Calculates all tick values for the given max and interval values.
 *
 * @param maxValue  maximum value for the axis
 * @param interval  interval (increment) value
 * @return {Array}  an array of all tick values
 */
MutationDiagram.prototype.getTickValues = function(maxValue, interval)
{
	// determine tick values
	var tickValues = [];
	var value = 0;

	while (value < maxValue)
	{
		tickValues.push(value);
		// use half interval value for generating minor ticks
		// TODO change back to full value when there is a fix for d3 minor ticks
		value += interval / 2;
	}

	// add the max value in any case
	tickValues.push(maxValue);

	return tickValues;
};

/**
 * Draws the x-axis on the bottom side of the plot area.
 *
 * @param svg       svg to append the axis
 * @param xScale    scale function for the y-axis
 * @param xMax      max y value for the axis
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return {object} svg group containing all the axis components
 */
MutationDiagram.prototype.drawXAxis = function(svg, xScale, xMax, options, bounds)
{
	var self = this;

	var interval = self.calcTickInterval(options.xAxisTickIntervals,
		xMax,
		options.xAxisTicks);

	var tickValues = self.getTickValues(xMax, interval);

	// formatter to hide labels
	var formatter = function(value) {
//		var displayInterval = calcDisplayInterval(interval,
//			xMax,
//			options.xAxisMaxTickLabel);

		// always display max value
		if (value == xMax)
		{
			return value + " aa";
		}
		// do not display minor values
		// (this is custom implementation of minor ticks,
		// minor ticks don't work properly for custom values)
		else if (value % interval != 0)
		{
			return "";
		}
		// display major tick value if its not too close to the max value
		else if (xMax - value > interval / 3)
		{
			return value;
		}
		// hide remaining labels
		else
		{
			return "";
		}
	};

	var tickSize = options.xAxisTickSize;

	var xAxis = d3.svg.axis()
		.scale(xScale)
		.orient("bottom")
		.tickValues(tickValues)
		.tickFormat(formatter)
		//.tickSubdivide(true) TODO minor ticks have a problem with custom values
		.tickSize(tickSize, tickSize/2, 0);

	// calculate y-coordinate of the axis
	var position = bounds.y + options.regionHeight + options.xAxisPadding;

	// append axis
	var axis = svg.append("g")
		.attr("class", "mut-dia-x-axis")
		.attr("transform", "translate(0," + position + ")")
		.call(xAxis);

	// format axis
	self.formatAxis(".mut-dia-x-axis",
		options.xAxisStroke,
		options.xAxisFont,
		options.xAxisFontSize,
		options.xAxisFontColor);

	return axis;
};

/**
 * Draws the y-axis on the left side of the plot area.
 *
 * @param svg       svg to append the axis
 * @param yScale    scale function for the y-axis
 * @param yMax      max y value for the axis
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return {object} svg group containing all the axis components
 */
MutationDiagram.prototype.drawYAxis = function(svg, yScale, yMax, options, bounds)
{
	var self = this;

	var interval = self.calcTickInterval(options.yAxisTickIntervals,
		yMax,
		options.yAxisTicks);

	// passing 2 * interval to avoid non-integer values
	// (this is also related to minor tick issue)
	var tickValues = self.getTickValues(yMax, 2 * interval);

	// formatter to hide all except first and last
	// also determines to put a '>' sign before the max value
	var formatter = function(value) {
		var formatted = '';

		if (value === yMax)
		{
			formatted = value;

			if (self.maxCount > yMax)
			{
				formatted = ">" + value;
			}
		}
		else if (value === 0)
		{
			formatted = value;
		}

		return formatted;
	};

	var tickSize = options.yAxisTickSize;

	var yAxis = d3.svg.axis()
		.scale(yScale)
		.orient("left")
		.tickValues(tickValues)
		.tickFormat(formatter)
		//.tickSubdivide(true) TODO minor ticks have a problem with custom values
		.tickSize(tickSize, tickSize/2, 0);

	// calculate y-coordinate of the axis
	var position = bounds.x - options.yAxisPadding;

	// append axis
	var axis = svg.append("g")
		.attr("class", "mut-dia-y-axis")
		.attr("transform", "translate(" + position + ",0)")
		.call(yAxis);

	// format axis
	self.formatAxis(".mut-dia-y-axis",
		options.yAxisStroke,
		options.yAxisFont,
		options.yAxisFontSize,
		options.yAxisFontColor);

	return axis;
};

/**
 * Draws the label of the y-axis.
 *
 * @param svg       svg to append the label element
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return {object} text label (svg element)
 */
MutationDiagram.prototype.drawTopLabel = function(svg, options, bounds)
{
	// set x, y of the label as the middle of the top left margins
	var x = options.labelTopMargin;
	var y = options.marginTop / 2;

	// append label
	var label = svg.append("text")
		.attr("fill", options.labelTopFontColor)
		.attr("text-anchor", "start")
		.attr("x", x)
		.attr("y", y)
		.attr("class", "mut-dia-top-label")
		.style("font-family", options.labelTopFont)
		.style("font-size", options.labelTopFontSize)
		.style("font-weight", options.labelTopFontWeight)
		.text(options.labelTop);

	return label;
};

/**
 * Draws the label of the y-axis.
 *
 * @param svg       svg to append the label element
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return {object} text label (svg element)
 */
MutationDiagram.prototype.drawYAxisLabel = function(svg, options, bounds)
{
	// set x, y of the label as the middle of the y-axis

	var x = bounds.x -
		options.yAxisPadding -
		options.yAxisTickSize -
		options.yAxisLabelPadding;

	var y =  bounds.y - (bounds.height / 2);

	// append label
	var label = svg.append("text")
		.attr("fill", options.labelYFontColor)
		.attr("text-anchor", "middle")
		.attr("x", x)
		.attr("y", y)
		.attr("class", "mut-dia-y-axis-label")
		.attr("transform", "rotate(270, " + x + "," + y +")")
		.style("font-family", options.labelYFont)
		.style("font-size", options.labelYFontSize)
		.style("font-weight", options.labelYFontWeight)
		.text(options.labelY);

	return label;
};

/**
 * Formats the style of the plot axis defined by the given selector.
 *
 * @param axisSelector  selector for the axis components
 * @param stroke        line color of the axis
 * @param font          font type of the axis value labels
 * @param fontSize      font size of the axis value labels
 * @param fontColor     font color of the axis value labels
 */
MutationDiagram.prototype.formatAxis = function(axisSelector, stroke, font, fontSize, fontColor)
{
	var selector = d3.selectAll(axisSelector + ' line');

	selector.style("fill", "none")
		.style("stroke", stroke)
		.style("shape-rendering", "crispEdges");

	selector = d3.selectAll(axisSelector + ' path');

	selector.style("fill", "none")
		.style("stroke", stroke)
		.style("shape-rendering", "crispEdges");

	selector = d3.selectAll(axisSelector + ' text');

	selector.attr("fill", fontColor)
		.style("font-family", font)
		.style("font-size", fontSize);
};

/**
 * Draws the lollipop data point and its line (from sequence to the lollipop top)
 * on the plot area.
 *
 * @param points    group (svg element) to append the lollipop data point
 * @param lines     line group (svg element) to append the lollipop lines
 * @param pileup    list (array) of mutations (pileup) at a specific location
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 * @return {object} lollipop data point & line as svg elements
 */
MutationDiagram.prototype.drawLollipop = function (points, lines, pileup, options, bounds, xScale, yScale)
{
	var self = this;

	// default data point type is circle
	var type = options.lollipopShapeRegular;

	var count = pileup.count;
	var start = pileup.location;

	var x = xScale(start);
	var y = yScale(count);

	// check if y-value (count) is out of the range
	if (count > options.maxLengthY)
	{
		// set a different shape for out-of-the-range values
		//type = "triangle-up";
		type = options.lollipopShapeSpecial;

		// set y to the max value
		y = yScale(options.maxLengthY);
	}

	var lollipopFillColor = self.getLollipopFillColor(options, pileup);
	self.updateColorMap(pileup, lollipopFillColor);

	var dataPoint = points.append('path')
		.attr('d', d3.svg.symbol().size(options.lollipopSize).type(type))
		.attr("transform", "translate(" + x + "," + y + ")")
		.attr('fill', lollipopFillColor)
		.attr('stroke', options.lollipopBorderColor)
		.attr('stroke-width', options.lollipopBorderWidth)
		.attr('id', pileup.pileupId)
		.attr('class', 'mut-dia-data-point')
		.attr('opacity', 0);

	// TODO add transition for y value to have a nicer effect
	self.fadeIn(dataPoint);

	// bind pileup data with the lollipop data point
	dataPoint.datum(pileup);

	var addTooltip = options.lollipopTipFn;
	addTooltip(dataPoint, pileup, options.showStats);

	var line = lines.append('line')
		.attr('x1', x)
		.attr('y1', y)
		.attr('x2', x)
		.attr('y2', self.calcSequenceBounds(bounds, options).y)
		.attr('stroke', options.lollipopStrokeColor)
		.attr('stroke-width', options.lollipopStrokeWidth)
		.attr('class', 'mut-dia-data-line')
		.attr('opacity', 0);

	// TODO add transition for y2 value to have a nicer effect
	self.fadeIn(line);

	return {"dataPoint": dataPoint, "line": line};
};

/**
 * Updates the mutation color map by adding a new entry for each mutation
 * in the given pile up.
 *
 * Mapped color of a mutation is NOT determined by its type, instead it is
 * determined by the color of the pileup. This is why we create a mapping
 * based on the pileup, otherwise a simple mapping (based on mutation type)
 * could be used.
 *
 * @param pileup    pileup of mutations
 * @param color     color of the given pileup
 */
MutationDiagram.prototype.updateColorMap = function(pileup, color)
{
	var self = this;

	// iterate all mutations in this pileup
	for (var i=0; i < pileup.mutations.length; i++)
	{
		// assign the same color to all mutations in this pileup
		self.mutationColorMap[pileup.mutations[i].get("mutationId")] = color;
	}
};

/**
 * Returns the shape (type) function to determine the shape of a
 * data point in the diagram. This implementation is required in order
 * to access "options" class member within the returned function.
 *
 * @return {Function}   shape function (for d3 symbol type)
 */
MutationDiagram.prototype.getLollipopShapeFn = function()
{
	var self = this;

	// actual function to use with d3.symbol.type(...)
	var shapeFunction = function(datum)
	{
		var type = self.options.lollipopShapeRegular;

		// set a different shape for out-of-the-range values
		if (datum.count > self.options.maxLengthY)
		{
			type = self.options.lollipopShapeSpecial;
		}

		return type;
	};

	return shapeFunction;
};

/**
 * Returns the fill color of the lollipop data point for the given pileup
 * of mutations.
 *
 * @param options   general options object
 * @param pileup    list (array) of mutations (pileup) at a specific location
 * @return {String} fill color
 */
MutationDiagram.prototype.getLollipopFillColor = function(options, pileup)
{
	var self = this;
	var color = options.lollipopFillColor;
	var value;

	if (_.isFunction(color))
	{
		value = color(pileup);
	}
	// check if the color is fixed
	else if (_.isString(color))
	{
		value = color;
	}
	// assuming color is an object
	else
	{
		var mutationsByMainType = PileupUtil.groupMutationsByMainType(pileup);

		// no main type for the given mutations (this should not happen)
		if (mutationsByMainType.length === 0)
		{
			// use default color
			value = color.default;
		}
		// color with the main type color
		else
		{
			// mutationsByMainType array is sorted by mutation count,
			// under tie condition certain types have priority over others
			value = color[mutationsByMainType[0].type];
		}
	}

	return value;
};

/**
 * Put labels over the lollipop data points. The number of labels to be displayed is defined
 * by options.lollipopLabelCount.
 *
 * @param labels        text group (svg element) for labels
 * @param pileups       array of mutations (pileups)
 * @param options       general options object
 * @param xScale        scale function for the x-axis
 * @param yScale        scale function for the y-axis
 */
MutationDiagram.prototype.drawLollipopLabels = function (labels, pileups, options, xScale, yScale)
{
	var self = this;

	// helper function to adjust text position to prevent overlapping with the y-axis
	var getTextAnchor = function(text, textAnchor)
	{
		var anchor = textAnchor;

		// adjust if necessary and (if it is set to auto only)
		if (anchor.toLowerCase() == "auto")
		{
			// calculate distance of the label to the y-axis (assuming the anchor will be "middle")
			var distance = text.attr("x") - (text.node().getComputedTextLength() / 2);

			// adjust label to prevent overlapping with the y-axis
			if (distance < options.marginLeft)
			{
				anchor = "start";
			}
			else
			{
				anchor = "middle";
			}
		}

		return anchor;
	};

	var count = options.lollipopLabelCount;
	var maxAllowedTie = 2; // TODO refactor as an option?

	// do not show any label if there are too many ties
	// exception: if there is only one mutation then display the label in any case
	if (pileups.length > 1)
	{
		var max = pileups[0].count;

		// at the end of this loop, numberOfTies will be the number of points with
		// max y-value (number of tied points)
		for (var numberOfTies = 0; numberOfTies < pileups.length; numberOfTies++)
		{
			if (pileups[numberOfTies].count < max)
			{
				break;
			}
		}

		// do not display any label if there are too many ties
		if (count < numberOfTies &&
		    numberOfTies > maxAllowedTie)
		{
			count = 0;
		}

	}

	// show (lollipopLabelCount) label(s)
	for (var i = 0;
	     i < count && i < pileups.length;
	     i++)
	{
		// check for threshold value
		if (pileups.length > 1 &&
		    pileups[i].count < options.lollipopLabelThreshold)
		{
			// do not processes remaining values below threshold
			// (assuming mutations array is sorted)
			break;
		}

		var x = xScale(pileups[i].location);
		var y = yScale(Math.min(pileups[i].count, options.maxLengthY)) -
		        (options.lollipopTextPadding);

		// init text
		var text = labels.append('text')
			.attr("fill", options.lollipopFontColor)
			.attr("x", x)
			.attr("y", y)
			.attr("class", "mut-dia-lollipop-text")
			.attr("transform", "rotate(" + options.lollipopTextAngle + ", " + x + "," + y +")")
			.style("font-size", options.lollipopFontSize)
			.style("font-family", options.lollipopFont)
			.text(pileups[i].label)
			.attr("opacity", 0);

		self.fadeIn(text);

		// adjust anchor
		var textAnchor = getTextAnchor(text, options.lollipopTextAnchor);
		text.attr("text-anchor", textAnchor);
	}
};

/**
 * Draws the given region on the sequence.
 *
 * @param svg       target svg to append region rectangle
 * @param region    region data
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @return {object} region rectangle & its text (as an svg group element)
 */
MutationDiagram.prototype.drawRegion = function(svg, region, options, bounds, xScale)
{
	var self = this;

	var start = region.metadata.start;
	var end = region.metadata.end;
	var label = region.text;
	var color = region.colour;

	var width = Math.abs(xScale(start) - xScale(end));
	var height = options.regionHeight;
	var y = bounds.y + options.seqPadding;
	var x = xScale(start);

	// group region and its label
	var group = svg.append("g")
		.attr("class", "mut-dia-region")
		.attr("transform", "translate(" + x + "," + y +")");

	var rect = group.append('rect')
		.attr('fill', color)
		.attr('x', 0)
		.attr('y', 0)
		.attr('width', width)
		.attr('height', height);

	var addTooltip = options.regionTipFn;

	// add tooltip to the rect
	addTooltip(rect, region, self.dataProxies.mutationAlignerProxy);

	if (options.showRegionText)
	{
		var text = self.drawRegionText(label, group, options, width);

		// add tooltip if the text fits
		if (text)
		{
			// add tooltip to the text
			addTooltip(text, region, self.dataProxies.mutationAlignerProxy);
		}
	}

	return group;
};

/**
 * Draws the text for the given svg group (which represents the region).
 * Returns null if neither the text nor its truncated version fits
 * into the region rectangle.
 *
 * @param label     text contents
 * @param group     target svg group to append the text
 * @param options   general options object
 * @param width     width of the region rectangle
 * @return {object} region text (svg element)
 */
MutationDiagram.prototype.drawRegionText = function(label, group, options, width)
{
	var xText = width/2;
	var height = options.regionHeight;

	if (options.regionTextAnchor === "start")
	{
		xText = 0;
	}
	else if (options.regionTextAnchor === "end")
	{
		xText = width;
	}

	// truncate or hide label if it is too long to fit
	var fits = true;

	// init text
	var text = group.append('text')
		.style("font-size", options.regionFontSize)
		.style("font-family", options.regionFont)
		.text(label)
		.attr("text-anchor", options.regionTextAnchor)
		.attr("fill", options.regionFontColor)
		.attr("x", xText)
		.attr("y", 2*height/3)
		.attr("class", "mut-dia-region-text");

	// check if the text fits into the region rectangle
	// adjust it if necessary
	if (text.node().getComputedTextLength() > width)
	{
		// truncate text if not fits
		label = label.substring(0,3) + "..";
		text.text(label);

		// check if truncated version fits
		if (text.node().getComputedTextLength() > width)
		{
			// remove if the truncated version doesn't fit either
			text.remove();
			text = null;
		}
	}

	return text;
};

/**
 * Draws the sequence just below the plot area.
 *
 * @param svg       target svg to append sequence rectangle
 * @param options   general options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @return {object} sequence rectangle (svg element)
 */
MutationDiagram.prototype.drawSequence = function(svg, options, bounds)
{
	var seqBounds = this.calcSequenceBounds(bounds, options);

	return svg.append('rect')
		.attr('fill', options.seqFillColor)
		.attr('x', seqBounds.x)
		.attr('y', seqBounds.y)
		.attr('width', seqBounds.width)
		.attr('height', seqBounds.height)
		.attr('class', 'mut-dia-sequence');
};

/**
 * Returns the number of mutations at the hottest spot.
 *
 * @param pileups array of piled up mutation data
 * @return {Number} number of mutations at the hottest spot
 */
MutationDiagram.prototype.calcMaxCount = function(pileups)
{
	var maxCount = -1;
//
//	for (var i = 0; i < mutations.length; i++)
//	{
//		if (mutations[i].count >= maxCount)
//		{
//			maxCount = mutations[i].count;
//		}
//	}
//
//	return maxCount;

	// assuming the list is sorted (descending)
	if (pileups.length > 0)
	{
		maxCount = pileups[0].count;
	}

	return maxCount;
};

/**
 * Calculates the bounds of the sequence.
 *
 * @param bounds    bounds of the plot area
 * @param options   diagram options
 */
MutationDiagram.prototype.calcSequenceBounds = function (bounds, options)
{
	var x = bounds.x;
	var y = bounds.y +
	        Math.abs(options.regionHeight - options.seqHeight) / 2 +
	        options.seqPadding;
	var width = bounds.width;
	var height = options.seqHeight;

	return {x: x,
		y: y,
		width: width,
		height: height};
};

/**
 * Updates the plot area of the diagram for the given set of pileup data.
 * This function assumes that the provided mutation data is a subset
 * of the original data. Therefore this function only modifies the plot area
 * elements (lollipops, labels, etc.). If the provided data set is not a subset
 * of the original data, then the behavior of this function is unpredicted.
 *
 * If the number of mutations provided in pileupData is less than the number
 * mutation in the original data set, this function returns true to indicate
 * the provided data set is a subset of the original data. If the number of
 * mutations is the same, then returns false.
 *
 * @param mutationColl  a MutationCollection instance
 * @return {boolean}  true if the diagram is filtered, false otherwise
 */
MutationDiagram.prototype.updatePlot = function(mutationColl)
{
	var self = this;
	var pileups = self.pileups;

	// TODO for a safer update, verify the provided data
	var pileupData = [];

	// update current data & pileups
	if (mutationColl)
	{
		pileupData = PileupUtil.convertToPileups(mutationColl, self.options.pileupConverter);
		self.pileups = pileups = pileupData;
		self.mutationPileupMap = PileupUtil.mapToMutations(pileups);
	}

	// remove all elements in the plot area
	self.cleanPlotArea();

	// reset color mapping (for the new data we may have different pileup colors)
	self.mutationColorMap = {};

	if (self.options.yAxisAutoAdjust)
	{
		// rescale y-axis without updating the plot,
		// otherwise... infinite recursion!
		self.rescaleYAxis(true);
	}

	// re-draw plot area contents for new data
	self.drawPlot(self.svg,
	              pileups,
	              self.options,
	              self.bounds,
	              self.xScale,
	              self.yScale);

	// also re-add listeners
	//for (var selector in self.listeners)
	_.each(_.keys(self.listeners), function(selector) {
		var target = self.svg.selectAll(selector);

		//for (var event in self.listeners[selector])
		_.each(_.keys(self.listeners[selector]), function(event) {
			target.on(event,
				self.listeners[selector][event]);
		});
	});

	// reset highlight map
	self.highlighted = {};

	// trigger corresponding event
	self.dispatcher.trigger(
		MutationDetailsEvents.DIAGRAM_PLOT_UPDATED);

	return self.isFiltered();
};

/**
 * Removes all elements of the plot area.
 */
MutationDiagram.prototype.cleanPlotArea = function()
{
	var self = this;

	// select all plot area elements
	var labels = self.gLabel.selectAll("text");
	var lines = self.gLine.selectAll("line");
	var dataPoints = self.gData.selectAll(".mut-dia-data-point");

	// remove all plot elements (no animation)
//	labels.remove();
//	lines.remove();
//	dataPoints.remove();

	self.fadeOut(labels, function(element) {
		$(element).remove();
	});

	self.fadeOut(lines, function(element) {
		$(element).remove();
	});

	self.fadeOut(dataPoints, function(element) {
		$(element).remove();
	});

	// alternative animated version:
	// fade out and then remove all
//	labels.transition()
//		.style("opacity", 0)
//		.duration(1000)
//		.each("end", function() {
//			$(this).remove();
//		});
//
//	lines.transition()
//		.style("opacity", 0)
//		.duration(1000)
//		.each("end", function() {
//			$(this).remove();
//		});
//
//	points.transition()
//		.style("opacity", 0)
//		.duration(1000)
//		.each("end", function() {
//			$(this).remove();
//		});

	// for the alternative animated version
	// plot re-drawing should also be delayed to have a nicer effect
};

/**
 * Resets the plot area back to its initial state.
 */
MutationDiagram.prototype.resetPlot = function()
{
	var self = this;

	self.updatePlot(self.data.mutations);

	// trigger corresponding event
	self.dispatcher.trigger(
		MutationDetailsEvents.DIAGRAM_PLOT_RESET);
};

/**
 * Updates the text of the top label.
 *
 * @param text  new text to set as the label value
 */
MutationDiagram.prototype.updateTopLabel = function(text)
{
	var self = this;

	// if no text value is passed used gene symbol to update the value
	if (text == undefined || text == null)
	{
		text = "";
	}

	self.topLabel.text(text);
};

/**
 * Adds an event listener for specific diagram elements.
 *
 * @param selector  selector string for elements
 * @param event     name of the event
 * @param handler   event handler function
 */
MutationDiagram.prototype.addListener = function(selector, event, handler)
{
	var self = this;

	self.svg.selectAll(selector).on(event, handler);

	// save the listener for future reference
	if (self.listeners[selector] == null)
	{
		self.listeners[selector] = {};
	}

	self.listeners[selector][event] = handler;

};

/**
 * Removes an event listener for specific diagram elements.
 *
 * @param selector  selector string for elements
 * @param event     name of the event
 */
MutationDiagram.prototype.removeListener = function(selector, event)
{
	var self = this;

	self.svg.selectAll(selector).on(event, null);

	// remove listener from the map
	if (self.listeners[selector] &&
	    self.listeners[selector][event])
	{
		delete self.listeners[selector][event];
	}
};

MutationDiagram.prototype.addDefaultListeners = function()
{
	var self = this;

	// diagram background click
	self.addListener(".mut-dia-background", "click", function(datum, index) {
		// ignore the action (do not dispatch an event) if:
		//  1) the diagram is already in a graphical transition:
		// this is to prevent inconsistency due to fast clicks on the diagram.
		//  2) there is no previously highlighted data point
		//  3) multi selection mode is on:
		// this is to prevent reset due to an accidental click on background
		var ignore = !self.isHighlighted() ||
		             self.multiSelect;

		if (!ignore)
		{
			// remove all diagram highlights
			self.clearHighlights();

			// trigger corresponding event
			self.dispatcher.trigger(
				MutationDetailsEvents.ALL_LOLLIPOPS_DESELECTED);
		}
	});

	// lollipop circle click
	self.addListener(".mut-dia-data-point", "click", function(datum, index) {
		// if already highlighted, remove highlight on a second click
		if (self.isHighlighted(this))
		{
			// remove highlight for the target circle
			self.removeHighlight(this);

			// also clear previous highlights if multiple selection is not active
			if (!self.multiSelect)
			{
				// remove all diagram highlights
				self.clearHighlights();
			}

			// trigger corresponding event
			self.dispatcher.trigger(
				MutationDetailsEvents.LOLLIPOP_DESELECTED,
				datum, index);
		}
		else
		{
			// clear previous highlights if multiple selection is not active
			if (!self.multiSelect)
			{
				// remove all diagram highlights
				self.clearHighlights();
			}

			// highlight the target circle on the diagram
			self.highlight(this);

			// trigger corresponding event
			self.dispatcher.trigger(
				MutationDetailsEvents.LOLLIPOP_SELECTED,
				datum, index);
		}
	});

	// lollipop circle mouse out
	self.addListener(".mut-dia-data-point", "mouseout", function(datum, index) {
		// if not highlighted, make the lollipop smaller
		if (!self.isHighlighted(this))
		{
			self.resizeLollipop(d3.select(this), self.options.lollipopSize);
		}

		// trigger corresponding event
		self.dispatcher.trigger(
			MutationDetailsEvents.LOLLIPOP_MOUSEOUT,
			datum, index);
	});

	// lollipop circle mouse over
	self.addListener(".mut-dia-data-point", "mouseover", function(datum, index) {
		// if not highlighted, make the lollipop bigger
		// (if highlighted, it should be already bigger by default)
		if (!self.isHighlighted(this))
		{
			self.resizeLollipop(d3.select(this), self.options.lollipopHighlightSize);
		}

		// trigger corresponding event
		self.dispatcher.trigger(
			MutationDetailsEvents.LOLLIPOP_MOUSEOVER,
			datum, index);
	});

	// listener that prevents text selection
	// when multi selection is activated by the shift key
	var preventSelection = function (datum, index)
	{
		if (self.multiSelect)
		{
			// current event is stored under d3.event
			d3.event.preventDefault();
		}
	};

	self.addListener(".mut-dia-data-point", "mousedown", preventSelection);
	self.addListener(".mut-dia-background", "mousedown", preventSelection);

	// TODO listen to the key events only on the diagram (if possible)
	// ...it might be better to bind window key event handlers in a global util class

	$(window).on("keydown", function(event) {
		if (event.keyCode == self.options.multiSelectKeycode)
		{
			self.multiSelect = true;
		}
	});

	$(window).on("keyup", function(event) {
		if (event.keyCode == self.options.multiSelectKeycode)
		{
			self.multiSelect = false;
		}
	});
};

/**
 * Checks whether a diagram data point is highlighted or not.
 * If no selector provided, then checks if the there is
 * at least one highlighted data point.
 *
 * @param selector  [optional] selector for a specific data point element
 * @return {boolean} true if highlighted, false otherwise
 */
MutationDiagram.prototype.isHighlighted = function(selector)
{
	var self = this;
	var highlighted = false;

	if (selector == undefined)
	{
		highlighted = !(_.isEmpty(self.highlighted));
	}
	else
	{
		var element = d3.select(selector);
		var location = element.datum().location;

		if (self.highlighted[location] != undefined)
		{
			highlighted = true;
		}
	}

	return highlighted;
};

/**
 * Resets all highlighted data points back to their original state.
 */
MutationDiagram.prototype.clearHighlights = function()
{
	var self = this;
	var dataPoints = self.gData.selectAll(".mut-dia-data-point");

	self.resizeLollipop(dataPoints, self.options.lollipopSize);
	self.highlighted = {};
};

/**
 * Highlights the pileup containing the given mutation.
 *
 * @param mutationSid    id of the mutation
 */
MutationDiagram.prototype.highlightMutation = function(mutationSid)
{
	var self = this;

	var pileupId = self.mutationPileupMap[mutationSid];

	// there may not be a pileup corresponding to the given sid,
	// because not every mutation is mapped onto the diagram
	if (pileupId != null)
	{
		var pileup = self.svg.select("#" + pileupId);

		if (pileup.length > 0)
		{
			self.highlight(pileup[0][0]);
		}
	}
};

/**
 * Highlights a single data point. This function assumes that the provided
 * selector is a selector for one of the SVG data point elements on the
 * diagram.
 *
 * @param selector  selector for a specific data point element
 */
MutationDiagram.prototype.highlight = function(selector)
{
	var self = this;
	var element = d3.select(selector);

	// resize lollipop to the highlight size
	self.resizeLollipop(element, self.options.lollipopHighlightSize);

	// add data point to the map
	var location = element.datum().location;
	self.highlighted[location] = element;
};

/**
 * Removes highlight of a single data point. This function assumes that
 * the provided selector is a selector for one of the SVG data point
 * elements on the diagram.
 *
 * @param selector  selector for a specific data point element
 */
MutationDiagram.prototype.removeHighlight = function(selector)
{
	var self = this;
	var element = d3.select(selector);

	// resize lollipop to the regular size
	self.resizeLollipop(element, self.options.lollipopSize);

	// remove data point from the map
	var location = element.datum().location;
	delete self.highlighted[location];
};

MutationDiagram.prototype.resizeLollipop = function(lollipop, size)
{
	var self = this;

	lollipop.transition()
		.ease("elastic")
		.duration(self.options.animationDuration)
		// TODO see if it is possible to update ONLY size, not the whole 'd' attr
		.attr("d", d3.svg.symbol()
			.size(size)
			.type(self.getLollipopShapeFn()));
};

MutationDiagram.prototype.fadeIn = function(element, callback)
{
	var self = this;

	element.transition()
		.style("opacity", 1)
		.duration(self.options.fadeDuration)
		.each("end", function() {
			      if(_.isFunction(callback)) {
				      callback(this);
			      }
		      });
};

MutationDiagram.prototype.fadeOut = function(element, callback)
{
	var self = this;

	element.transition()
		.style("opacity", 0)
		.duration(self.options.fadeDuration)
		.each("end", function() {
			      if(_.isFunction(callback)) {
				      callback(this);
			      }
		      });
};

/**
 * Returns selected (highlighted) elements as a list of svg elements.
 *
 * @return {Array}  a list of SVG elements
 */
MutationDiagram.prototype.getSelectedElements = function()
{
	var self = this;

	return _.values(self.highlighted);
};

/**
 * Checks the diagram for filtering. If the current data set
 * is a subset of the initial data set, then it means
 * the diagram is filtered. If the current data set is the
 * initial data set, then the diagram is not filtered.
 *
 * @return {boolean} true if current view is filtered, false otherwise
 */
MutationDiagram.prototype.isFiltered = function()
{
	var self = this;
	var filtered = false;

	if (PileupUtil.countMutations(self.pileups) <
	    PileupUtil.countMutations(self.initialPileups))
	{
		filtered = true;
	}

	return filtered;
};

MutationDiagram.prototype.getThreshold = function()
{
	return Math.max(this.maxCount, this.options.minLengthY);
};

MutationDiagram.prototype.getMaxY = function()
{
	return this.yMax;
};

MutationDiagram.prototype.getInitialMaxY = function()
{
	var self = this;

	if (!self.initialYMax)
	{
		var maxCount = self.calcMaxCount(self.initialPileups);
		self.initialYMax = self.calcYMax(self.options, maxCount);
	}

	return self.initialYMax;
};

MutationDiagram.prototype.getMinY = function()
{
	return this.options.minLengthY;
};
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Constructor for MutationHistogram class.
 *
 * @param geneSymbol    hugo gene symbol
 * @param options       visual options object
 * @param data          collection of Mutation models (MutationCollection)
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationHistogram(geneSymbol, options, data)
{
	// call super constructor
	MutationDiagram.call(this, geneSymbol, options, data);
}

// this is for inheritance (MutationHistogram extends MutationDiagram)
MutationHistogram.prototype = new MutationDiagram();
MutationHistogram.prototype.constructor = MutationHistogram;

/**
 * Draws histogram bars on the plot area.
 *
 * @param svg       svg container for the diagram
 * @param pileups   array of mutations (pileups)
 * @param options   options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 * @override
 */
MutationHistogram.prototype.drawPlot = function(svg, pileups, options, bounds, xScale, yScale)
{
	// TODO draw multi color animated histogram lines
};
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Constructor for the MutationPdbPanel class.
 *
 * @param options   visual options object
 * @param data      PDB data (collection of PdbModel instances)
 * @param proxy     PDB data proxy
 * @param xScale    scale function for the x axis
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationPdbPanel(options, data, proxy, xScale)
{
	/**
	 * Default visual options.
	 */
	var _defaultOpts = {
		el: "#mutation_pdb_panel_d3", // id of the container
		elWidth: 740,       // width of the container
		elHeight: "auto",   // height of the container
		maxHeight: 200,     // max height of the container
		numRows: [Infinity], // number of rows to be to be displayed for each expand request
		marginLeft: 45,     // left margin
		marginRight: 30,    // right margin
		marginTop: 2,       // top margin
		marginBottom: 0,    // bottom margin
		chainHeight: 6,     // height of a rectangle representing a single pdb chain
		chainPadding: 3,    // padding between chain rectangles
		labelY: ["PDB", "Chains"],  // label of the y-axis. use array for multi lines, false: "do not draw"
		labelYFont: "sans-serif",   // font type of the y-axis label
		labelYFontColor: "#2E3436", // font color of the y-axis label
		labelYFontSize: "12px",     // font size of y-axis label
		labelYFontWeight: "normal", // font weight of y-axis label
		labelYPaddingRightH: 45, // padding between y-axis and its label (horizontal alignment)
		labelYPaddingTopH: 7,    // padding between y-axis and its label (horizontal alignment)
		labelYPaddingRightV: 25, // padding between y-axis and its label (vertical alignment)
		labelYPaddingTopV: 20,   // padding between y-axis and its label (vertical alignment)
		labelAlignThreshold: 5,  // threshold to determine horizontal or vertical alignment
		chainBorderColor: "#666666", // border color of the chain rectangles
		chainBorderWidth: 0.5,       // border width of the chain rectangles
		highlightBorderColor: "#FF9900", // color of the highlight rect border
		highlightBorderWidth: 2.0,       // width of the highlight rect border
		colors: ["#3366cc"],  // rectangle colors
		animationDuration: 1000, // transition duration (in ms) used for resize animations
		/**
		 * Default chain tooltip function.
		 *
		 * @param element   target svg element (rectangle)
		 */
		chainTipFn: function (element) {
			var datum = element.datum();

			proxy.getPdbInfo(datum.pdbId, function(pdbInfo) {
				var summary = null;

				if (pdbInfo)
				{
					summary = PdbDataUtil.generatePdbInfoSummary(
						pdbInfo[datum.pdbId], datum.chain.chainId);
				}

				// init tip view
				var tipView = new PdbChainTipView({model: {
					pdbId: datum.pdbId,
					pdbInfo: summary.title,
					molInfo: summary.molecule,
					chain: datum.chain
				}});

				var content = tipView.compileTemplate();

				var options = {content: {text: content},
					hide: {fixed: true, delay: 100},
					style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'},
					position: {my:'bottom left', at:'top center',viewport: $(window)}};

				//$(element).qtip(options);
				cbio.util.addTargetedQTip(element, options);
			});
		},
		/**
		 * Default y-axis help tooltip function.
		 *
		 * @param element   target svg element (help icon)
		 */
		yHelpTipFn: function (element) {
			var templateFn = BackboneTemplateCache.getTemplateFn(
				"mutation_details_pdb_help_tip_template");

			var content = templateFn({});

			var options = {content: {text: content},
				hide: {fixed: true, delay: 100},
				style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow qtip-wide'},
				position: {my:'bottom left', at:'top center',viewport: $(window)}};

			//$(element).qtip(options);
			cbio.util.addTargetedQTip(element, options);
		}
	};

	// event listeners
	var _listeners = {};

	// custom event dispatcher
	var _dispatcher = {};
	_.extend(_dispatcher, Backbone.Events);

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// reference to the main svg element
	var _svg = null;

	// row data (allocation of chains wrt rows)
	var _rowData = null;

	// default chain group svg element
	var _defaultChainGroup = null;

	// expansion level indicator (initially 0)
	var _expansion = 0;

	// max expansion level:
	// assume numRows = [0, 100, 500], and number of total rows = 20
	// in this case max level should be 2 (although numRows has 3 elements)
	var _maxExpansionLevel = null;

	// number of total rectangles drawn (initially 0)
	// this number is being updated after each panel expand
	var _rectCount = 0;

	// indicator for an expansion level whether the rectangles drawn
	var _levelDrawn = [];

	// <pdbId:chainId> to <chain group (svg element)> map
	var _chainMap = {};

	// <pdbId:chainId> to <row index> map
	var _rowMap = {};

	// previous height before auto collapse
	var _levelHeight = 0;

	// currently highlighted chain
	var _highlighted = null;

	/**
	 * Draws the actual content of the panel, by drawing a rectangle
	 * for each chain
	 *
	 * @param svg       svg element (D3)
	 * @param options   visual options object
	 * @param data      row data
	 * @param xScale    scale function for the x-axis
	 * @param rowStart  starting index for the first row
	 */
	function drawPanel(svg, options, data, xScale, rowStart)
	{
		// chain counter
		var count = _rectCount;

		// add a rectangle group for each chain
		_.each(data, function(allocation, rowIdx) {
			_.each(allocation, function(datum, idx) {
				var chain = datum.chain;

				// create the rectangle group
				if (chain.alignments.length > 0)
				{
					// assign a different color to each chain
					var color = options.colors[idx % options.colors.length];
					//datum.color = color;

					var y = options.marginTop +
					        (rowStart + rowIdx) * (options.chainHeight + options.chainPadding);

					var gChain = drawChainRectangles(svg, chain, color, options, xScale, y);
					gChain.datum(datum);
					_chainMap[PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId)] = gChain;

					// set the first drawn chain as the default chain
					if (_defaultChainGroup == null)
					{
						_defaultChainGroup = gChain;
					}

					// increment chain counter
					count++;
				}
			});
		});

		// update global rectangle counter in the end
		_rectCount = count;

		// add chain tooltips
		addChainTooltips(data, options);
	}

	/**
	 * Adds tooltips to the chain rectangles.
	 *
	 * @param data      row data containing pdb and chain information
	 * @param options   visual options object
	 */
	function addChainTooltips(data, options)
	{
		// this is to prevent chain tooltip functions to send
		// too many separate requests to the server

		var pdbIds = [];
		var chains = [];

		// collect pdb ids and chains
		_.each(data, function(allocation, rowIdx) {
			_.each(allocation, function(datum, idx) {
				pdbIds.push(datum.pdbId);
				chains.push(_chainMap[
					PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId)]);
			});
		});

		// this caches pdb info before adding the tooltips
		proxy.getPdbInfo(pdbIds.join(" "), function(data) {
			// add tooltip to the chain groups
			_.each(chains, function(chain, idx) {
				var addTooltip = options.chainTipFn;
				addTooltip(chain);
			});
		});
	}

	/**
	 * Draws a group of rectangles for a specific chain.
	 *
	 * @param svg       svg element (D3)
	 * @param chain     a PdbChainModel instance
	 * @param color     rectangle color
	 * @param options   visual options object
	 * @param xScale    scale function for the x-axis
	 * @param y         y coordinate of the rectangle group
	 * @return {object} group for the chain (svg element)
	 */
	function drawChainRectangles(svg, chain, color, options, xScale, y)
	{
		var gChain = svg.append("g")
			.attr("class", "pdb-chain-group")
			.attr("opacity", 1);

		var height = options.chainHeight;

		// init the segmentor for the merged alignment object
		var segmentor = new MergedAlignmentSegmentor(chain.mergedAlignment);

		// iterate all segments for this merged alignment
		while (segmentor.hasNextSegment())
		{
			var segment = segmentor.getNextSegment();

			var width = Math.abs(xScale(segment.start) - xScale(segment.end));
			var x = xScale(segment.start);

			// draw a line (instead of a rectangle) for an alignment gap
			if (segment.type == PdbDataUtil.ALIGNMENT_GAP)
			{
				var line = gChain.append('line')
					.attr('stroke', options.chainBorderColor)
					.attr('stroke-width', options.chainBorderWidth)
					.attr('x1', x)
					.attr('y1', y + height/2)
					.attr('x2', x + width)
					.attr('y2', y + height/2);

				// store initial position for future use
				// TODO this is not a good way of using datum
				line.datum({initPos: {x: x, y: (y + height/2)}});
			}
			// draw a rectangle for any other segment type
			else
			{
				var rect = gChain.append('rect')
					.attr('fill', color)
					.attr('opacity', chain.mergedAlignment.identityPerc)
					.attr('stroke', options.chainBorderColor)
					.attr('stroke-width', options.chainBorderWidth)
					.attr('x', x)
					.attr('y', y)
					.attr('width', width)
					.attr('height', height);

				// store initial position for future use
				// TODO this is not a good way of using datum
				rect.datum({initPos: {x: x, y: y}});
			}
		}

		return gChain;
	}

	/**
	 * Draws the label of the y-axis.
	 *
	 * @param svg       svg to append the label element
	 * @param options   general options object
	 * @return {object} label group (svg element)
	 */
	function drawYAxisLabel(svg, options)
	{
		// default (vertical) orientation
		var x = options.marginLeft - options.labelYPaddingRightV;
		var y =  options.marginTop + options.labelYPaddingTopV;
		var textAnchor = "middle";
		var rotation = "rotate(270, " + x + "," + y +")";
		var orient = "vertical";

		// horizontal orientation for small number of rows
		if (_rowData.length < options.labelAlignThreshold)
		{
			x = options.marginLeft - options.labelYPaddingRightH;
			y = options.marginTop + options.labelYPaddingTopH;
			textAnchor = "start";
			rotation = "rotate(0, " + x + "," + y +")";
			orient = "horizontal";
		}

		var gLabel = svg.append("g")
			.attr("class", "pdb-panel-y-axis-label-group")
			.attr("opacity", 1);

		// append label
		var label = gLabel.append("text")
			.attr("fill", options.labelYFontColor)
			.attr("text-anchor", textAnchor)
			.attr("x", x)
			.attr("y", y)
			.attr("class", "pdb-panel-y-axis-label")
			.attr("transform", rotation)
			.style("font-family", options.labelYFont)
			.style("font-size", options.labelYFontSize)
			.style("font-weight", options.labelYFontWeight);

		// for an array, create multi-line label
		if (_.isArray(options.labelY))
		{
			_.each(options.labelY, function(text, idx) {
				var dy = (idx == 0) ? 0 : 10;

				// TODO this is an adjustment to fit the help icon image
				var dx = (idx == 0 && orient == "vertical") ? -5 : 0;

				label.append('tspan')
					.attr('x', x + dx)
					.attr('dy', dy).
					text(text);
			});
		}
		// regular string, just set the text
		else
		{
			label.text(options.labelY);
		}

		var help = drawYAxisHelp(gLabel, x, y, orient, options);

		var addTooltip = options.yHelpTipFn;
		addTooltip(help);

		return label;
	}

	/**
	 *
	 * @param svg       svg to append the label element
	 * @param labelX    x coord of y-axis label
	 * @param labelY    y coord of y-axis label
	 * @param orient    orientation of the label (vertical or horizontal)
	 * @param options   general options object
	 * @return {object} help image (svg element)
	 */
	function drawYAxisHelp(svg, labelX, labelY, orient, options)
	{
		// TODO all these values are fine tuned for the label "PDB Chains",
		// ...setting another label text would probably mess things up
		var w = 12;
		var h = 12;
		var x = labelX - w + 2;
		var y = options.marginTop;

		if (orient == "horizontal")
		{
			x = options.marginLeft - w - 5;
			y = labelY - h + 2;
		}

		return svg.append("svg:image")
			.attr("xlink:href", "images/help.png")
			.attr("class", "pdb-panel-y-axis-help")
			.attr("x", x)
			.attr("y", y)
			.attr("width", w)
			.attr("height", h);
	}

	/**
	 * Returns the group svg element for the default chain.
	 *
	 * @return chain group for the default chain.
	 */
	function getDefaultChainGroup()
	{
		return _defaultChainGroup;
	}

	/**
	 * Returns the group svg element for the given pdb id
	 * and chain id pair.
	 *
	 * @param pdbId
	 * @param chainId
	 * @return chain group for the specified chain.
	 */
	function getChainGroup(pdbId, chainId)
	{
		return _chainMap[pdbId + ":" + chainId];
	}

	/**
	 * Calculates the max expansion level for the given data.
	 *
	 * @param totalNumRows      total number of rows
	 * @param expansionLevels   expansion level array
	 *                          (number of rows to be displayed for each level)
	 * @return {number}     max level for the current data
	 */
	function calcMaxExpansionLevel(totalNumRows, expansionLevels)
	{
		var max = -1;

		// try to find the first value within the level array
		// which is bigger than the total number of rows
		for (var i=0; i < expansionLevels.length; i++)
		{

			if (expansionLevels[i] > totalNumRows)
			{
				max = i;
				break;
			}
		}

		// if the total number of rows is bigger than all values
		// than max should be the highest available level
		if (max == -1)
		{
			max = expansionLevels.length - 1;
		}

		return max;
	}

	/**
	 * Calculates the full height of the panel wrt to provided elHeight option.
	 *
	 * @param elHeight  provided height value
	 * @return {number}
	 */
	function calcHeight(elHeight)
	{
		var height = 0;
		var rowCount = _rowData.length;

		// if not auto, then just copy the value
		if (elHeight != "auto")
		{
			height = elHeight;
		}
		else
		{
			height = _options.marginTop + _options.marginBottom +
				rowCount * (_options.chainHeight + _options.chainPadding) -
				(_options.chainPadding / 2); // no need for the full padding for the last row
		}

		return height;
	}

	/**
	 * Calculates the collapsed height of the panel wrt to provided
	 * maxChain option.
	 *
	 * @param maxChain  maximum number of rows to be displayed
	 * @return {number} calculated collapsed height
	 */
	function calcCollapsedHeight(maxChain)
	{
		var height = 0;
		var rowCount = _rowData.length;

		if (maxChain < rowCount)
		{
			height = _options.marginTop +
				maxChain * (_options.chainHeight + _options.chainPadding) -
				(_options.chainPadding / 2); // no need for full padding for the last row
		}
		// total number of chains is less than max, set to full height
		else
		{
			height = calcHeight("auto");
		}

		return height;
	}

	/**
	 * Creates the main svg element.
	 *
	 * @param container target container (html element)
	 * @param width     widht of the svg
	 * @param height    height of the svg
	 * @return {object} svg instance (D3)
	 */
	function createSvg(container, width, height)
	{
		var svg = container.append("svg");

		svg.attr('width', width);
		svg.attr('height', height);

		return svg;
	}

	function xScaleFn(data)
	{
		var width = _options.elWidth -
		        (_options.marginLeft + _options.marginRight);

		var x = _options.marginLeft;

		return d3.scale.linear()
			.domain([0, calcXMax(data)])
			.range([x, x + width]);
	}

	function calcXMax(data)
	{
		var values = [];

		_.each(data, function(row) {
			_.each(row, function(pdb) {
				values.push(pdb.chain.mergedAlignment.uniprotTo);
			});
		});

		return _.max(values);
	}

	/**
	 * Initializes the panel.
	 */
	function init()
	{
		// TODO get pdbRowData (or uniprot id?) as a model parameter
		// generate row data (one row may contain more than one chain)
		_rowData = PdbDataUtil.allocateChainRows(data);
		_maxExpansionLevel = calcMaxExpansionLevel(_rowData.length, _options.numRows);

		// in case no xScale function provided, generate the scale by using the row data
		if (xScale == null)
		{
			xScale = xScaleFn(_rowData);
		}

		// selecting using jQuery node to support both string and jQuery selector values
		var node = $(_options.el)[0];
		var container = d3.select(node);

		// number of rows to be shown initially
		var numRows = _options.numRows[0];
		_levelHeight = calcCollapsedHeight(numRows);

		// create svg element & update its reference
		var svg = createSvg(container,
		                    _options.elWidth,
		                    _levelHeight);

		_svg = svg;

		// (partially) draw the panel
		drawPanel(svg, _options, _rowData.slice(0, numRows), xScale, 0);
		_levelDrawn[0] = true;

		// draw the labels
		if (_options.labelY != false)
		{
			drawYAxisLabel(svg, _options);
		}

		// build row map
		_rowMap = buildRowMap(_rowData);

		// add default listeners
		addDefaultListeners();
	}

	/**
	 * Builds a map of <pdbId:chainId>, <row index> pairs
	 * for the given row data.
	 *
	 * @param rowData   rows of chain data
	 * @return {Object} <pdbId:chainId> to <row index> map
	 */
	function buildRowMap(rowData)
	{
		var map = {};

		// add a rectangle group for each chain
		_.each(rowData, function(allocation, rowIdx) {
			_.each(allocation, function(datum, idx) {
				map[PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId)] = rowIdx;
			});
		});

		return map;
	}

	/**
	 * Adds an event listener for specific diagram elements.
	 *
	 * @param selector  selector string for elements
	 * @param event     name of the event
	 * @param handler   event handler function
	 */
	function addListener(selector, event, handler)
	{
		_svg.selectAll(selector).on(event, handler);

		// save the listener for future reference
		if (_listeners[selector] == null)
		{
			_listeners[selector] = {};
		}

		_listeners[selector][event] = handler;
	}

	/**
	 * Removes an event listener for specific diagram elements.
	 *
	 * @param selector  selector string for elements
	 * @param event     name of the event
	 */
	function removeListener(selector, event)
	{
		_svg.selectAll(selector).on(event, null);

		// remove listener from the map
		if (_listeners[selector] &&
		    _listeners[selector][event])
		{
			delete _listeners[selector][event];
		}
	}

	function addDefaultListeners()
	{
		addListener(".pdb-chain-group", "click", function(datum, index) {
			// highlight the selected chain on the pdb panel
			highlight(d3.select(this));
		});
	}

	/**
	 * Reapplies current listeners to the diagram. This function should be
	 * called while adding new diagram elements after initialization.
	 */
	function reapplyListeners()
	{
		_.each(_.keys(_listeners), function(selector) {
			var target = _svg.selectAll(selector);

			_.each(_.keys(_listeners[selector]), function(event) {
				target.on(event, _listeners[selector][event]);
			});
		});
	}

	/**
	 * Shows the panel.
	 */
	function showPanel()
	{
		$(_options.el).show();
	}

	/**
	 * Hides the panel.
	 */
	function hidePanel()
	{
		$(_options.el).hide();
	}

	/**
	 * Resizes the panel height to show only a limited number of chains.
	 *
	 * @param index level index
	 */
	function resizePanel(index)
	{
		// resize to collapsed height
		var collapsedHeight = calcCollapsedHeight(_options.numRows[index]);
		var prevHeight = _svg.attr("height");

		dispatchResizeStartEvent(collapsedHeight, prevHeight);

		_svg.transition()
			.duration(_options.animationDuration)
			.attr("height", collapsedHeight)
			.each("end", function() {
				dispatchResizeEndEvent(collapsedHeight, prevHeight);
			});

		_levelHeight = collapsedHeight;
	}

	/**
	 * Resizes the panel to its full height (to show all chains).
	 */
	function expandPanel()
	{
		// resize to full size
		var fullHeight = calcHeight(_options.elHeight);
		_svg.transition().duration(_options.animationDuration).attr("height", fullHeight);
	}

	/**
	 * Expands/Collapses the panel.
	 */
	function toggleHeight()
	{
		 var nextLevel = drawNextLevel();

		// resize panel
		resizePanel(nextLevel);
	}

	/**
	 * Draws the next level of rectangles.
	 *
	 * @return {Number} next level number
	 */
	function drawNextLevel()
	{
		// do not try to draw any further levels than max level
		// (no rectangle to draw beyond max level)
		var nextLevel = (_expansion + 1) % (_maxExpansionLevel + 1);

		// draw the rectangles if not drawn yet
		if (!_levelDrawn[nextLevel])
		{
			// draw rectangles for the next level
			drawPanel(_svg,
			          _options,
			          _rowData.slice(_options.numRows[_expansion], _options.numRows[nextLevel]),
			          xScale,
			          _options.numRows[_expansion]);

			// also reapply the listeners for the new elements
			reapplyListeners();

			// mark the indicator for the next level
			_levelDrawn[nextLevel] = true;
		}

		// update expansion level
		_expansion = nextLevel;

		return nextLevel;
	}

	/**
	 * Expands the panel to a specific level.
	 *
	 * @param level
	 */
	function expandToLevel(level)
	{
		var nextLevel = -1;

		// expand until desired level
		for (var i = _expansion;
		     i < level && i < _maxExpansionLevel;
		     i++)
		{
			nextLevel = drawNextLevel();
		}

		// if already expanded (or beyond) that level,
		// no need to update or resize
		if (nextLevel !== -1)
		{
			// resize panel
			resizePanel(nextLevel);
		}
	}

	/**
	 * Expands the panel to the level of the specified chain.
	 *
	 * @param pdbId
	 * @param chainId
	 */
	function expandToChainLevel(pdbId, chainId)
	{
		var chainLevel = -1;
		var chainRow = _rowMap[PdbDataUtil.chainKey(pdbId, chainId)];

		for (var i=0; i < _options.numRows.length; i++)
		{
			if (chainRow < _options.numRows[i])
			{
				chainLevel = i;
				break;
			}
		}

		// TODO chainLevel is beyond the visible levels, expand all?
		if (chainLevel !== -1)
		{
			expandToLevel(chainLevel);
		}
	}

	/**
	 * Checks if there are more chains (more rows) to show. This function
	 * returns true if the number of total rows exceeds the initial number
	 * of rows to be displayed (which is determined by numRows option).
	 *
	 * @return {boolean} true if there are more rows to show, false otherwise
	 */
	function hasMoreChains()
	{
		return (_rowData.length > _options.numRows[0]);
	}

	/**
	 * Highlights a group of chain rectangles by drawing an outline
	 * border around the bounding box of all group elements.
	 *
	 * @param chainGroup    a group of rectangles representing the pdb chain
	 */
	function highlight(chainGroup)
	{
		// update the reference
		_highlighted = chainGroup;

		// calculate the bounding box
		var bbox = boundingBox(chainGroup);

		// remove the previous selection rectangle(s)
		_svg.selectAll(".pdb-selection-rectangle-group").remove();
		var gRect = _svg.append('g')
			.attr('class', "pdb-selection-rectangle-group")
			.attr('opacity', 0);

		// add the selection rectangle
		var rect = gRect.append('rect')
			.attr('fill', "none")
			.attr('stroke', _options.highlightBorderColor)
			.attr('stroke-width', _options.highlightBorderWidth)
			.attr('x', bbox.x)
			.attr('y', bbox.y)
			.attr('width', bbox.width)
			.attr('height', bbox.height);

		gRect.transition().duration(_options.animationDuration).attr('opacity', 1);

		// store initial position for future use
		// TODO this is not a good way of using datum
		rect.datum({initPos: {x: bbox.x, y: bbox.y}});

		// ...alternatively we can just use a yellowish color
		// to highlight the whole background

		// trigger corresponding event
		_dispatcher.trigger(
			MutationDetailsEvents.PANEL_CHAIN_SELECTED,
			chainGroup);
	}

	function boundingBox(rectGroup)
	{
		var left = Infinity;
		var right = -1;
		var y = -1;
		var height = -1;

		rectGroup.selectAll("rect").each(function(datum, idx) {
			var rect = d3.select(this);
			// assuming height and y are the same for all rects
			y = parseFloat(rect.attr("y"));
			height = parseFloat(rect.attr("height"));

			var x = parseFloat(rect.attr("x"));
			var width = parseFloat(rect.attr("width"));

			if (x < left)
			{
				left = x;
			}

			if (x + width > right)
			{
				right = x + width;
			}
		});

		return {x: left,
			y: y,
			width: right - left,
			height: height};
	}

	/**
	 * Collapses the view to the currently highlighted chain group
	 *
	 * @param callback  function to invoke after the transition
	 */
	function minimizeToHighlighted(callback)
	{
		if (_highlighted != null)
		{
			minimizeToChain(_highlighted, callback);
		}
	}

	/**
	 * Collapses the view to the given chain group by hiding
	 * everything other than the given chain. Also reduces
	 * the size of the diagram to fit only a single row.
	 *
	 * @param chainGroup    chain group (svg element)
	 * @param callback      function to invoke after the transition
	 */
	function minimizeToChain(chainGroup, callback)
	{
		var duration = _options.animationDuration;

		// 3 transitions in parallel:

		// TODO shifting all chains causes problems with multiple transitions
		// 1) shift all chains up, such that selected chain will be on top
		//shiftToChain(chainGroup);

		// 1) reposition the given chain..
		moveToFirstRow(chainGroup, callback);

		//..and the selection rectangle if the chain is highlighted
		if (chainGroup == _highlighted)
		{
			moveToFirstRow(_svg.selectAll(".pdb-selection-rectangle-group"));
		}

		// 2) fade-out all chains (except selected) and labels
		fadeOutOthers(chainGroup);

		// 3) resize the panel to a single row size
		var collapsedHeight = calcCollapsedHeight(1);
		var prevHeight = _svg.attr("height");

		dispatchResizeStartEvent(collapsedHeight, prevHeight);

		_svg.transition().duration(duration)
			.attr("height", collapsedHeight)
			.each("end", function(){
				dispatchResizeEndEvent(collapsedHeight, prevHeight);
			});
	}

	/**
	 * Shift all the chain rectangles, such that the given chain
	 * will be in the first row.
	 *
	 * @param chainGroup    chain group (svg element)
	 * @param callback      function to invoke after the transition
	 */
	function shiftToChain(chainGroup, callback)
	{
		var duration = _options.animationDuration;
		var datum = chainGroup.datum();
		var key = PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId);
		var chainRow = _rowMap[key];

		// if chains are not at their original positions, then shift value should be different
//		var shift = chainRow * (_options.chainHeight + _options.chainPadding);

		// calculate shift value relative to the current position of the given chain group
		var shift = 0;

		chainGroup.selectAll("rect").each(function(datum, idx) {
			var rect = d3.select(this);
			shift = parseInt(rect.attr("y")) - _options.marginTop;
		});

		var shiftFn = function(target, d, attr) {
			var ele = d3.select(target);
			return (parseInt(ele.attr(attr)) - shift);
		};

		// shift up every chain on the y-axis
		yShiftRect(".pdb-chain-group rect", shiftFn, duration);
		yShiftRect(".pdb-selection-rectangle-group rect", shiftFn, duration);
		yShiftLine(".pdb-chain-group line", shiftFn, duration);

		// TODO it is better to bind this to a d3 transition
		// ..safest way is to call after the selected chain's transition ends
		setTimeout(callback, duration + 50);
	}

	/**
	 * Moves the given chainGroup to the first row.
	 *
	 * @param chainGroup
	 * @param callback
	 */
	function moveToFirstRow(chainGroup, callback)
	{
		var duration = _options.animationDuration;

		// first row coordinates...
		// (we can also use the default chain coordinates)
		var y = _options.marginTop;
		var height = _options.chainHeight;

		// move chain group rectangles and lines
		chainGroup.selectAll("line")
			.transition().duration(duration)
			.attr('y1', y + height/2)
			.attr('y2', y + height/2);

		chainGroup.selectAll("rect")
			.transition().duration(duration)
			.attr('y', y)
			.each("end", function() {
                if (_.isFunction(callback)) {
					callback();
				}
			});
	}

	/**
	 * Fades out all other element except the ones in
	 * the given chain group.
	 *
	 * @param chainGroup    chain group to exclude from fade out
	 * @param callback      function to invoke after the transition
	 */
	function fadeOutOthers(chainGroup, callback)
	{
		var duration = _options.animationDuration;
		var datum = chainGroup.datum();
		var key = PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId);

		// fade out all chain rectangles but the given
		_svg.selectAll(".pdb-chain-group")
			.transition().duration(duration)
			.attr("opacity", function(datum) {
				if (PdbDataUtil.chainKey(datum.pdbId, datum.chain.chainId) === key) {
					// do not hide the provided chain
					return 1;
				} else {
					// hide all the others
					return 0;
				}
			});

		// also fade out selection rectangle if the given chain is not selected
		_svg.selectAll(".pdb-selection-rectangle-group")
			.transition().duration(duration)
			.attr("opacity", function(datum) {
				if (_highlighted == chainGroup) {
					// do not hide the selection rectangle
					return 1;
				} else {
					// hide the selection rectangle
					return 0;
				}
			});

		_svg.select(".pdb-panel-y-axis-label-group")
			.transition().duration(duration)
			.attr("opacity", 0)
			.each("end", function() {
				if (_.isFunction(callback)) {
					callback();
				}
			});
	}

	function yShiftLine(selector, shiftFn, duration)
	{
		_svg.selectAll(selector)
			.transition().duration(duration)
			.attr("y1", function(d) {
				return shiftFn(this, d, "y1");
			})
			.attr("y2", function(d) {
				return shiftFn(this, d, "y2");
			});
	}

	function yShiftRect(selector, shiftFn, duration)
	{
		_svg.selectAll(selector)
			.transition().duration(duration)
			.attr("y", function(d) {
				return shiftFn(this, d, "y");
			});
	}

	/**
	 * Reverses the changes back to the state before calling
	 * the minimizeToChain function.
	 *
	 * @param callback  function to invoke after the transition
	 */
	function restoreToFull(callback)
	{
		var duration = _options.animationDuration;

		// put everything back to its original position
		restoreChainPositions();

		// fade-in hidden elements
		fadeInAll();

		var prevHeight = _svg.attr("height");

		dispatchResizeStartEvent(_levelHeight, prevHeight);

		// restore to previous height
		_svg.transition().duration(duration)
			.attr("height", _levelHeight)
			.each("end", function(){
				if (_.isFunction(callback)) {
					callback();
				}
				dispatchResizeEndEvent(_levelHeight, prevHeight);
			});
	}

	/**
	 * Restores all chains back to their initial positions.
	 */
	function restoreChainPositions(callback)
	{
		var duration = _options.animationDuration;

		var shiftFn = function(target, d, attr) {
			return d.initPos.y;
		};

		// put everything back to its original position
		yShiftRect(".pdb-chain-group rect", shiftFn, duration);
		yShiftLine(".pdb-chain-group line", shiftFn, duration);
		yShiftRect(".pdb-selection-rectangle-group rect", shiftFn, duration);

		// TODO it is better to bind this to a d3 transition
		// ..safest way is to call after the selected chain's transition ends
		setTimeout(callback, duration + 50);
	}

	/**
	 * Fades in all hidden components.
	 */
	function fadeInAll(callback)
	{
		var duration = _options.animationDuration;

		// fade-in hidden elements

		_svg.selectAll(".pdb-chain-group")
			.transition().duration(duration)
			.attr("opacity", 1);

		_svg.selectAll(".pdb-selection-rectangle-group")
			.transition().duration(duration)
			.attr("opacity", 1);

		_svg.selectAll(".pdb-panel-y-axis-label-group")
			.transition().duration(duration)
			.attr("opacity", 1)
			.each("end", function(){
				if (_.isFunction(callback)) {
					callback();
				}
			});
	}

	function getHighlighted()
	{
		return _highlighted;
	}

	function dispatchResizeStartEvent(newHeight, prevHeight)
	{
		_dispatcher.trigger(
			MutationDetailsEvents.PDB_PANEL_RESIZE_STARTED,
			newHeight, prevHeight, _options.maxHeight);
	}

	function dispatchResizeEndEvent(newHeight, prevHeight)
	{
		_dispatcher.trigger(
			MutationDetailsEvents.PDB_PANEL_RESIZE_ENDED,
			newHeight, prevHeight, _options.maxHeight);
	}

	return {init: init,
		addListener: addListener,
		removeListener: removeListener,
		getChainGroup: getChainGroup,
		getDefaultChainGroup: getDefaultChainGroup,
		show: showPanel,
		hide: hidePanel,
		toggleHeight: toggleHeight,
		expandToChainLevel: expandToChainLevel,
		minimizeToChain: minimizeToChain,
		minimizeToHighlighted: minimizeToHighlighted,
		restoreToFull: restoreToFull,
		restoreChainPositions: restoreChainPositions,
		fadeInAll: fadeInAll,
		hasMoreChains: hasMoreChains,
		highlight: highlight,
		getHighlighted: getHighlighted,
		dispatcher: _dispatcher};
}


/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * MutationPdbTable class (extends AdvancedDataTable)
 *
 * Highly customizable table view built on DataTables plugin.
 * See default options object (_defaultOpts) for details.
 *
 * With its default configuration, following events are dispatched by this class:
 * - MutationDetailsEvents.TABLE_CHAIN_SELECTED:
 *   dispatched when a PDB chain selected on the table
 * - MutationDetailsEvents.PDB_TABLE_READY:
 *   dispatched when the table initialization complete
 *
 * @param options   visual options object
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationPdbTable(options)
{
	var self = this;

	// default options object
	var _defaultOpts = {
		el: "#mutation_pdb_table_d3",
		elWidth: 740, // width of the container
		// default column options
		//
		// name: internal name used to define column specific properties
		// sTitle: display value
		// tip: tooltip value
		// [data table options]: sType, sClass, sWidth, asSorting, ...
		columns: {
			datum: {sTitle: "datum",
				tip:""},
			pdbId: {sTitle: "PDB Id",
				tip:"",
				sType: "string"},
			chain: {sTitle: "Chain",
				tip:"",
				sType: "string"},
			uniprotPos: {sTitle: "Uniprot Positions",
				tip:"",
				sType: "numeric"},
			identityPercent: {sTitle: "Identity Percent",
				tip:"",
				sType: "numeric"},
			organism: {sTitle: "Organism",
				tip:"",
				sType: "string"},
			summary: {sTitle: "Summary",
				tip:"",
				sType: "string",
				sWidth: "65%"}
		},
		// display order of column headers
		columnOrder: ["datum", "pdbId", "chain", "uniprotPos",
			"identityPercent", "organism", "summary"],
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
		// renderer function for each column
		columnRender: {
			identityPercent: function(datum) {
				// format as a percentage value
				return Math.round(datum.chain.mergedAlignment.identityPerc * 100);
			},
			pdbId: function(datum) {
				// format using the corresponding template
				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_pdb_table_pdb_cell_template");
				return templateFn({pdbId: datum.pdbId});
			},
			chain: function(datum) {
				// format using the corresponding template
				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_pdb_table_chain_cell_template");
				return templateFn({chainId: datum.chain.chainId});
			},
			organism: function(datum) {
				return datum.organism;
			},
			summary: function(datum) {
				var vars = {summary: datum.summary.title,
					molecule: datum.summary.molecule};

				// format using the corresponding template
				var templateFn = BackboneTemplateCache.getTemplateFn("mutation_pdb_table_summary_cell_template");
				return templateFn(vars);
			},
			uniprotPos: function(datum) {
				// there is no data (null) for uniprot positions,
				// so set the display value by using the hidden
				// column "datum"
				return datum.chain.mergedAlignment.uniprotFrom + "-" +
				       datum.chain.mergedAlignment.uniprotTo;
			}
		},
		// default tooltip functions
		columnTooltips: {
			"simple": function(selector) {
				var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();
				//$(selector).find('.simple-tip').qtip(qTipOptions);
				cbio.util.addTargetedQTip($(selector).find('.simple-tip'), options);
			}
		},
		// default event listener config
		// TODO add more params if necessary
		eventListeners: {
			"pdbLink": function(dataTable, dispatcher, indexMap) {
				$(dataTable).on("click", ".pbd-chain-table-chain-cell a", function (event) {
					event.preventDefault();

					// remove previous highlights
					removeAllSelection();

					// get selected row via event target
					var selectedRow = $(event.target).closest("tr.pdb-chain-table-data-row");

					// highlight selected row
					selectedRow.addClass('row_selected');

					//var data = _dataTable.fnGetData(this);
					var data = dataTable.fnGetData(selectedRow[0]);
					var datum = data[indexMap["datum"]];

					// trigger corresponding event
					dispatcher.trigger(
						MutationDetailsEvents.TABLE_CHAIN_SELECTED,
						datum.pdbId,
						datum.chain.chainId);
				});
			}
		},
		// column sort functions
		columnSort: {
			identityPercent: function(datum) {
				return MutationDetailsTableFormatter.assignFloatValue(
					Math.round(datum.chain.mergedAlignment.identityPerc * 100));
			},
			pdbId: function(datum) {
				return datum.pdbId;
			},
			chain: function(datum) {
				return datum.chain.chainId;
			},
			organism: function(datum) {
				return datum.organism;
			},
			summary: function(datum) {
				return datum.summary.title + datum.summary.molecule;
			},
			uniprotPos: function(datum) {
				return MutationDetailsTableFormatter.assignIntValue(
					datum.chain.mergedAlignment.uniprotFrom);
			}
		},
		// column filter functions
		columnFilter: {
			identityPercent: function(datum) {
				return Math.round(datum.chain.mergedAlignment.identityPerc * 100);
			},
			summary: function(datum) {
				return datum.summary.title + " " + datum.summary.molecule;
			},
			uniprotPos: function(datum) {
				return datum.chain.mergedAlignment.uniprotFrom + "-" +
				       datum.chain.mergedAlignment.uniprotTo;
			}
		},
		// delay amount before applying the user entered filter query
		filteringDelay: 0,
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

	// call super constructor to init options and other params
	AdvancedDataTable.call(this, _options);
	_options = self._options;

	// custom event dispatcher
	var _dispatcher = self._dispatcher;

	var _rowMap = {};

	var _selectedRow = null;

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
	 * @private
	 */
	function initDataTableOpts(tableSelector, rows, columnOpts, nameMap,
		indexMap, hiddenCols, excludedCols, nonSearchableCols)
	{
		// generate column options for the data table
		var columns = DataTableUtil.getColumnOptions(columnOpts,
			indexMap);

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
				self._addColumnTooltips();
			},
			"fnHeaderCallback": function(nHead, aData, iStart, iEnd, aiDisplay) {
				$(nHead).find('th').addClass("mutation-pdb-table-header");
			},
			"fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
				var datum = aData[indexMap["datum"]];
				var key = PdbDataUtil.chainKey(datum.pdbId,
				                               datum.chain.chainId);
				_rowMap[key] = nRow;
				$(nRow).addClass("pdb-chain-table-data-row");
			},
			"fnInitComplete": function(oSettings, json) {
				// trigger corresponding event
				_dispatcher.trigger(
					MutationDetailsEvents.PDB_TABLE_READY);
			}
		};

		return tableOpts;
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
			// TODO determine function params (if needed)
			value = vis();
		}

		return value;
	}

	/**
	 * Determines the search value for the given column name
	 *
	 * @param columnName    name of the column (header)
	 * @return {Boolean}    whether searchable or not
	 */
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

	function addEventListeners(indexMap)
	{
		// super.addEventListeners(indexMap);

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
		self.getDataTable().fnFilter("");
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

	// override required functions
	this._initDataTableOpts = initDataTableOpts;
	this._visibilityValue = visibilityValue;
	this._searchValue = searchValue;

	// additional public functions
	this.selectRow = selectRow;
	this.cleanFilters = cleanFilters;
	this.getSelectedRow = getSelectedRow;
	this.dispatcher = this._dispatcher;
}

// MutationPdbTable extends AdvancedDataTable...
MutationPdbTable.prototype = new AdvancedDataTable();
MutationPdbTable.prototype.constructor = MutationPdbTable;
/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Makes a Pancancer Mutation Histogram on the DOM el.
 *
 * @param byProteinPosData          [list of {cancer_study, cancer_type, hugo, protein_pos_start, count} ]
 * @param byGeneData                [list of {cancer_study, cancer_type, hugo, count} ]
 * @param cancer_study_meta_data    [list of {cancer_study, cancer_type, num_sequenced_samples} ]
 * @param el                        DOM element
 * @param params                    overrides default parameters: { margin: { top, bottom, right, left }, width, height, this_cancer_study }
 * @return  an object {el, qtip} where qtip is a function: svg ->
 *          undefined, creates qtips and their corresponding `rect .mouseOver`
 *          elements
 *
 * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
 * September 2013
 */
// TODO make the histogram compatible for different data types (keyword, position data, mutation type, etc)
function PancanMutationHistogram(byProteinPosData, byGeneData, cancer_study_meta_data, el, params) {

    params = params || {};
    if (params.sparkline) {
        params = _.extend({
            margin: {top: -12, right: 0, bottom: 0, left: 0},
            width: 30,
            height: 12,
            this_cancer_study: undefined
        }, params);
    } else {
        params = _.extend({
            margin: {top:6, right: 10, bottom: 20, left: 40},
            width: 600,
            height: 300,
            this_cancer_study: undefined
        }, params);
    }

    var cancer_study2meta_data = generate_cancer_study2datum(cancer_study_meta_data);
    var all_cancer_studies = _.keys(cancer_study2meta_data);

    // --- data munging --- //

    // copy
    var bykeyword_data = deep_copy(byProteinPosData);
    var bygene_data = deep_copy(byGeneData);

    // extend
	//var keyword = bykeyword_data[0].keyword;
	var keyword = bykeyword_data[0].hugo + " " + bykeyword_data[0].protein_pos_start;

    bykeyword_data = extend_by_zero_set(bykeyword_data)
        .map(function(d) { d.keyword = keyword; return d; });     // make sure everything has a key.  TODO: remove this extra list traversal
    bygene_data = extend_by_zero_set(bygene_data);

    var cancer_study2datum = {
        bykeyword: generate_cancer_study2datum(bykeyword_data),
        bygene: generate_cancer_study2datum(bygene_data)
    };
    
    var commonKeys = _.intersection( _.keys(cancer_study2datum.bykeyword), _.keys(cancer_study2datum.bygene) );
    bykeyword_data = [];
    bygene_data = [];
    _.each(commonKeys, function(aKey) {
	bykeyword_data.push(cancer_study2datum.bykeyword[aKey]);
        bygene_data.push(cancer_study2datum.bygene[aKey]);
    });


    if (bygene_data.length !== bykeyword_data.length) {
        throw new Error("must be same length");
    }

    if (bygene_data.length !== all_cancer_studies.length) {
        throw new Error("there must be a datum for every cancer study and visa versa");
    }

    // subtract off counts in bykeyword_data from bygene_data
    // because the counts in bygene_data include the ones in bykeyword_data
    // and we don't want to count the same thing twice.
    bygene_data.forEach(function(bygene_datum) {
        var bykeyword_datum = cancer_study2datum.bykeyword[bygene_datum.cancer_study];
        var new_count = bygene_datum.count - bykeyword_datum.count;

        if (new_count < 0) {
            throw new Error("more mutations for a particular keyword than "
                + "for all keywords of a particular gene");
        }

        bygene_datum.count = new_count;
    });
    
    var totalByGene = _.reduce(bygene_data, function(memo, datum){ return memo + datum.count; }, 0);
    var totalByKeyword = _.reduce(bykeyword_data, function(memo, datum){ return memo + datum.count; }, 0);
    var totalSequenced = _.reduce(cancer_study2meta_data, function(memo, datum){ return memo + datum.num_sequenced_samples; }, 0);

    _.mixin({
        unzip: function(array) {
            return _.zip.apply(_, array);
        }
    });

    var all_data = bykeyword_data.concat(bygene_data);
    try {
        all_data = _.chain(all_data)
            .map(compute_frequency)
            .groupBy(function(d) {
                return d.cancer_study;
            })
            .map(_.identity)    // extract groups
            .sortBy(cancer_type)
            .unzip()            // turn into layers for d3.stack
            .value();
    } catch(e) {
        throw new Error(e);
    }

    function deep_copy(list_of_objects) {
        return list_of_objects.map(_.clone);
    }

    function generate_cancer_study2datum(data) {
        return _.reduce(data, function(acc, next) {
            acc[next.cancer_study] = next;
            return acc;
        }, {});
    }

    function compute_frequency(d) {
        var num_sequenced_samples = cancer_study2meta_data[d.cancer_study].num_sequenced_samples;
        d.num_sequenced_samples = num_sequenced_samples;
        d.frequency = d.count / num_sequenced_samples;
        return d;
    }

    // takes a list of cancer studies (presumably one which contains all the
    // cancer studies for a cancer type) and returns the total frequency in
    // that list
    //
    // *signature:* `array -> number`
    function total_frequency(group) {
        var total_frequency = _.reduce(group, function(acc, next) { return acc + next.frequency }, 0);
        return -1 * total_frequency;
    }

    // returns the cancer type of a group
    // *throws* error if not all elements in the list have the same cancer type
    //
    // *signature:* `array -> string`
    function cancer_type(group) {
        var cancerType = group[0].cancer_type;
        if (!_.every(group, function(d) { return d.cancer_type === cancerType; })) {
            throw new Error("not all data in a group have the same cancer type");
        }

        return cancerType;
    }

    // add in missing cancer studies as data points with count = 0
    function zero_set(data) {
        var cancer_study2datum = generate_cancer_study2datum(data);
        // TODO: this could be optimized by referring to the `cancer_study2datum` object

        function zero_datum(cancer_study) {
            return {
                cancer_study: cancer_study,
                count: 0,
                cancer_type: cancer_study2meta_data[cancer_study].cancer_type,
                num_sequenced_samples: cancer_study2meta_data[cancer_study].num_sequenced_samples
            };
        }

        return _.chain(all_cancer_studies)
            .reduce(function(acc, study) {
                if (!_.has(cancer_study2datum, study)) {
                    // do all_cancer_studies *setminus* cancer_study2datum
                    acc.push(study);
                }
                return acc;
            }, [])
            .map(zero_datum)
            .value();
    }

    function extend_by_zero_set(data) {
        return data.concat(zero_set(data));
    }

    // --- visualization --- //

    // margin conventions http://bl.ocks.org/mbostock/3019563
    var width = params.width - params.margin.left - params.margin.left;
    var height = params.height - params.margin.top - params.margin.bottom;

    var svg = d3.select(el).append("svg")
        .attr("width", params.width)
        .attr("height", params.height)
        .append("g")
        .attr("transform", "translate(" + params.margin.left + "," + params.margin.top + ")");

    var stack = d3.layout.stack()
            .x(function(d) { return d.cancer_study; })
            .y(function(d) { return d.frequency; })
        ;

    var layers = stack(all_data);
//    console.log(layers);

    var x = d3.scale.ordinal()
        .domain(all_data[0].map(function(d) { return d.cancer_study; }))
        .rangeBands([0, width], .1);

    // sparkline y axis does not scale: will always be from 0 to 1
    var sparkline_y_threshold = .2
    var yStackMax = params.sparkline ? sparkline_y_threshold
        : d3.max(layers, function(layer) { return d3.max(layer, function(d) { return d.y0 + d.y; }); });

    var y = d3.scale.linear()
        .domain([0, yStackMax])
        .range([height, 0])
        .clamp(true)
        ;

    // --- bar chart ---

    var googleblue = "LimeGreen";
    var googlered = "Green";

    var layer = svg.selectAll(".layer")
        .data(layers)
        .enter().append("g")
        .attr("class", "layer")
        .style("fill", function(d, i) { return [googlered, googleblue][i]; });

    var rect = layer.selectAll("rect")
        .data(function(d) { return d; })
        .enter().append("rect")
        .attr("x", function(d) { return x(d.cancer_study); })
        .attr("y", function(d) { return y(d.y0 + d.y); })
        .attr("width", function(d) { return x.rangeBand(); })
        .attr("height", function(d) { return y(d.y0) - y(d.y0 + d.y); })

    // *** kill process, do nothing more ***
    if (params.sparkline) {
        return {
            el: el,
            qtip: function() { throw new Error("don't qtip a sparkline"); }
        };
    }

    // --- axises --- //

    var percent_format = d3.format(yStackMax > .1 ? ".0%" : ".1%");
    var yAxis = d3.svg.axis()
        .scale(y)
        .tickFormat(percent_format)
        .orient("left");
    yAxis.tickSize(yAxis.tickSize(), 0, 0);

    // list of element that represent the start and end of each cancer type in
    // the sorted list of cancer studies
    var study_start_ends = (function() {
        var first = all_data[0][0];

        function new_element_from_datum(d) {
            return {
                cancer_type: d.cancer_type,
                start: d.cancer_study,
                end: d.cancer_study,
                color: cancer_study2meta_data[d.cancer_study].color
            };
        }

        return _.chain(all_data[0])
            .reduce(function(acc, next) {
                var last = _.last(acc);

                // beginning of a new cancer type, create a first cancer_study
                if (last.cancer_type !== next.cancer_type) {
                    return acc.concat(new_element_from_datum(next));
                }

                // within a cancer type, continue updating the last
                // cancer_study
                if (last.cancer_type === next.cancer_type) {
                    last.end = next.cancer_study;
                    return acc;
                }

            }, [ new_element_from_datum(first) ])
            .value();
    }());

    // add the cancer type axis
    svg.selectAll('line')
        .data(study_start_ends)
        .enter()
        .append('line')
        .attr('x1', function(d) { return x(d.start); })
        .attr('x2', function(d) { return x(d.end) + x.rangeBand(); })
        .attr('y1', height + params.margin.bottom / 3)
        .attr('y2', height + params.margin.bottom / 3)
        .style('stroke-width', 5)
        .style('stroke', function(d) { return d.color; })
    ;

    // append y axis

    var yAxisEl = svg.append("g")
        .call(yAxis)
        .attr('stroke', '#000')
        .attr('shape-rendering', 'crispEdges');

    var hugo_gene_name = _.find(layers[0], function(d) { return d.hugo !== undefined; }).hugo;
    var keyword = _.find(layers[0], function(d) { return d.keyword !== undefined; }).keyword;

    // star the current cancer study if this_cancer_study is provided.
    if (!_.isUndefined(params.this_cancer_study)) {
        star_this_cancer_study();
    }

    function star_this_cancer_study() {
        var this_cancer_study_data = _.find(all_data[0], function(d) {
            return d.cancer_study === params.this_cancer_study;
        });

        var this_cancer_type;
        try {
            this_cancer_type = this_cancer_study_data.cancer_type;
        } catch(e) {
            throw new Error(e + ": could not find this the corresponding datum for this cancer study, [" + params.this_cancer_study + "]");
        }

        var find_this_cancer_studdy_datum = function(group) {
            return _.find(group, function(d) {
                return d.cancer_study === params.this_cancer_study;
            });
        };

        var this_cancer_type_group = _.zip.apply(null, all_data);
        this_cancer_type_group = _.find(this_cancer_type_group, find_this_cancer_studdy_datum);

        var total_freq = total_frequency(this_cancer_type_group);

        svg.append('text')
            .text('*')
            .attr('id', 'star')
            .attr('x', x(params.this_cancer_study))
            .attr('y', y(-1 * total_freq) + 10)
            .style("font-family", "Helvetica Neue, Helvetica, Arial, sans-serif")
            .style("font-size", (x.rangeBand()*3) + "px");
    }

    function qtip(svg) {
        var mouseOverBar = d3.select(svg).selectAll('.mouseOver')
            .data(all_cancer_studies)
            .enter()
            .append('rect')
            .attr('class', 'mouseOver')
            .attr('y', params.margin.top)
            .attr('x', function(d) {
                return x(d) + params.margin.left;
            })
            .attr('opacity', '0')
            .attr('height', height + 5)
            .attr('width', x.rangeBand())
            .on('mouseover', function() { d3.select(this).attr('opacity', '0.25'); })
            .on('mouseout', function() { d3.select(this).attr('opacity', '0'); });

        // add qtips for each bar
        mouseOverBar.each(function(d) {
            $(this).qtip({
                content: {text: 'mouseover failed'},
                position: {my:'left top', at:'center right', viewport: $(window)},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-wide' },
                hide: { fixed: true, delay: 100 },
                events: {
                    render: function(event, api) {
                        var data = getRectsByCancerStudy(d).map(function(rect) { return rect[0].__data__; });
                        var bykeyword = data.filter(function(d) { return _.has(d, "keyword"); })[0] || {};
                        var bygene = data.filter(function(d) { return !_.has(d, "keyword"); })[0] || {};
                        var cancer_study = bygene.cancer_study;     // there should always be a bygene datum
                        var total = cancer_study2meta_data[cancer_study].num_sequenced_samples;
                        var text = "<p style='font-weight:bold;'>" + cancer_study + "</p>"
                            + countText(bykeyword, bygene, total);

                        api.set('content.text', text);
                    }
                }
            });
        });
    }

    function qtip_template(d, total) {
        var count = d.count || 0;
        if (!('frequency' in d)) d.frequency = count / total;
        var percent = (d.frequency * 100).toFixed(1)+'%';
        return (_.template("<span><b>{{percent}}</b> (<b>{{count}}</b> of {{total}} sequenced samples)</span>"))({percent: percent, count: count, total: total});
    }
    
    function countText(bykeyword, bygene, total) {
        return "<p style='color: " + googlered + "; margin-bottom:0;'>"
                + keyword  + ": "  + qtip_template(bykeyword, total) + "</p>"
                + "<p style='color: " + googleblue + "; margin-top:0;'>"
                + "Other " + hugo_gene_name +  " mutations: "  + qtip_template(bygene, total) + "</p>";
    }

    function getRectsByCancerStudy(cancer_study) {
        return rect.filter(function(d) { return d.cancer_study === cancer_study; });
    }

    return {
        el: el,
        qtip: qtip,
        overallCountText: function() {return countText({count:totalByKeyword}, {count:totalByGene}, totalSequenced);}
    };
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Controller class for the Main Mutation view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param mainMutationView  a MainMutationView instance
 *
 * @author Selcuk Onur Sumer
 */
function MainMutationController(mainMutationView)
{
	var _mutationDiagram = null;

	function init()
	{
		if (mainMutationView.diagramView)
		{
			diagramInitHandler(mainMutationView.diagramView.mutationDiagram);
		}
		else
		{
			mainMutationView.dispatcher.on(
				MutationDetailsEvents.DIAGRAM_INIT,
				diagramInitHandler);
		}

		// also init reset link call back
		mainMutationView.addResetCallback(handleReset);
	}

	function diagramInitHandler(mutationDiagram)
	{
		// update class variable
		_mutationDiagram = mutationDiagram;

		// add listeners to the custom event dispatcher of the diagram
		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.ALL_LOLLIPOPS_DESELECTED,
			allDeselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_DESELECTED,
			deselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_SELECTED,
			selectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_UPDATED,
			diagramUpdateHandler);
	}

	function handleReset(event)
	{
		// reset the diagram contents
		if (_mutationDiagram)
		{
			_mutationDiagram.resetPlot();
		}

		// hide the filter info text
		mainMutationView.hideFilterInfo();
	}

	function diagramUpdateHandler()
	{
		if (_mutationDiagram &&
		    _mutationDiagram.isFiltered())
		{
			// display info text
			mainMutationView.showFilterInfo();
		}
		else
		{
			// hide info text
			mainMutationView.hideFilterInfo();
		}
	}

	function allDeselectHandler()
	{
		// hide filter reset info
		if (_mutationDiagram &&
		    !_mutationDiagram.isFiltered())
		{
			mainMutationView.hideFilterInfo();
		}
	}

	function deselectHandler(datum, index)
	{
		// check if all deselected
		// (always show text if still there is a selected data point)
		if (_mutationDiagram &&
		    _mutationDiagram.getSelectedElements().length == 0)
		{
			// hide filter reset info
			allDeselectHandler();
		}
	}

	function selectHandler(datum, index)
	{
		// show filter reset info
		mainMutationView.showFilterInfo();
	}

	init();
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Controller class for the 3D Mutation view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param mutationDetailsView   a MutationDetailsView instance
 * @param mainMutationView      a MainMutationView instance
 * @param viewOptions           view component options
 * @param renderOptions         view class options
 * @param mut3dVisView          a Mutation3dVisView instance
 * @param mut3dView             a Mutation3dView instance
 * @param pdbProxy              proxy for pdb data
 * @param mutationUtil          data utility class (having the related mutations)
 * @param geneSymbol            hugo gene symbol (string value)
 *
 * @author Selcuk Onur Sumer
 */
function Mutation3dController(mutationDetailsView, mainMutationView, viewOptions, renderOptions,
	mut3dVisView, mut3dView, pdbProxy, mutationUtil, geneSymbol)
{
	// we cannot get pdb panel view as a constructor parameter,
	// since it is initialized after initializing this controller
	var _pdbPanelView = null;
	var _pdbTableView = null;

	var _mut3dVisView = null; // a Mutation3dVisView instance
	var _mut3dVis = null;     // singleton Mutation3dVis instance
	var _mutationDiagram = null;

	// TODO this can be implemented in a better/safer way
	// ...find a way to bind the source info to the actual event

	// flags for distinguishing actual event sources
	var _chainSelectedByTable = false;

	function init()
	{
		if (mainMutationView.diagramView)
		{
			diagramInitHandler(mainMutationView.diagramView.mutationDiagram);
		}
		else
		{
			mainMutationView.dispatcher.on(
				MutationDetailsEvents.DIAGRAM_INIT,
				diagramInitHandler);
		}

		if (mainMutationView.tableView &&
		    mainMutationView.tableView.mutationTable)
		{
			// add listeners for the mutation table view
			mainMutationView.tableView.mutationTable.dispatcher.on(
				MutationDetailsEvents.PDB_LINK_CLICKED,
				pdbLinkHandler);

			mainMutationView.tableView.mutationTable.dispatcher.on(
				MutationDetailsEvents.PROTEIN_CHANGE_LINK_CLICKED,
				proteinChangeLinkHandler);
		}

		// add listeners for the mutation 3d view
		mut3dView.addInitCallback(mut3dInitHandler);

		// add listeners for the mutation details view
		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.GENE_TAB_SELECTED,
			geneTabSelectHandler);

		// set mut3dVisView instance if it is already initialized
		if (mut3dVisView)
		{
			vis3dCreateHandler(mut3dVisView)
		}
		// if not init yet, wait for the init event
		else
		{
			mutationDetailsView.dispatcher.on(
				MutationDetailsEvents.VIS_3D_PANEL_CREATED,
				vis3dCreateHandler);
		}
	}

	function diagramInitHandler(mutationDiagram)
	{
		// update class variable
		_mutationDiagram = mutationDiagram;

		// add listeners to the custom event dispatcher of the diagram
		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.ALL_LOLLIPOPS_DESELECTED,
			allDeselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_DESELECTED,
			diagramDeselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_SELECTED,
			diagramSelectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_MOUSEOVER,
			diagramMouseoverHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_MOUSEOUT,
			diagramMouseoutHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_UPDATED,
			diagramUpdateHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_RESET,
			diagramResetHandler);
	}

	function vis3dCreateHandler(mutation3dVisView)
	{
		// init the 3d view initializer & 3D controller
		if (mutation3dVisView)
		{
			_mut3dVisView = mutation3dVisView;
			_mut3dVis = mutation3dVisView.options.mut3dVis;

			// add listeners for the mutation 3d vis view
			_mut3dVisView.dispatcher.on(
				MutationDetailsEvents.VIEW_3D_PANEL_CLOSED,
				view3dPanelCloseHandler);

			_mut3dVisView.dispatcher.on(
				MutationDetailsEvents.VIEW_3D_STRUCTURE_RELOADED,
				view3dReloadHandler);
		}
	}

	function geneTabSelectHandler(gene)
	{
//		var sameGene = (gene.toLowerCase() == geneSymbol.toLowerCase());
//		var reset = sameGene &&
//		            mut3dView &&
//					mut3dVisView &&
//		            mut3dVisView.isVisible();

		// reset if the 3D panel is visible,
		// and selected gene is this controller's gene
//		if (reset)
//		{
//			// TODO instead of reset, restore to previous config:
//			// may need to update resetView and loadDefaultChain methods
//			// (see issue #456)
//			mut3dView.resetView();
//		}

		// just hide the 3D view for now

		if (_mut3dVisView)
		{
			_mut3dVisView.resetPanelPosition();
			_mut3dVisView.hideView();
		}
	}

	function view3dPanelCloseHandler()
	{
		// hide the corresponding pdb panel and table views

		if (_pdbPanelView)
		{
			_pdbPanelView.hideView();
		}
	}

	function mut3dInitHandler(event)
	{
		reset3dView();

		if (_mut3dVisView != null)
		{
			_mut3dVisView.resetPanelPosition();
			_mut3dVisView.maximizeView();
		}
	}

	function panelResizeStartHandler(newHeight, prevHeight, maxHeight)
	{
		// check if it is expanded beyond the max height
		if (newHeight > maxHeight)
		{
			// add the toggle bar at the beginning of the resize
			_pdbPanelView.toggleScrollBar(maxHeight);
		}
	}

	function panelResizeEndHandler(newHeight, prevHeight, maxHeight)
	{
		// check if it is collapsed
		if (newHeight <= maxHeight)
		{
			// remove the toggle bar at the end of the resize
			_pdbPanelView.toggleScrollBar(-1);
		}

		// if there is a change in the size,
		// then also scroll to the correct position
		if (prevHeight != newHeight)
		{
			_pdbPanelView.scrollToSelected();
		}
	}

	function panelChainSelectHandler(element)
	{
		// scroll to the selected chain if selection triggered by the table
		// (i.e: exclude manual selection for the sake of user-friendliness)
		if (_chainSelectedByTable)
		{
			// scroll the view
			_pdbPanelView.scrollToSelected();
		}

		// update 3D view with the selected chain data
		var datum = element.datum();

		if (_mut3dVisView != null)
		{
			_mut3dVisView.maximizeView();
			_mut3dVisView.updateView(geneSymbol, datum.pdbId, datum.chain);
		}

		// also update the pdb table (highlight the corresponding row)
		if (!_chainSelectedByTable &&
		    _pdbTableView != null)
		{
			_pdbTableView.resetFilters();
			_pdbTableView.selectChain(datum.pdbId, datum.chain.chainId);
			_pdbTableView.scrollToSelected();
		}

		// reset the flag
		_chainSelectedByTable = false;
	}

	function view3dReloadHandler()
	{
		// highlight mutations on the 3D view
		// (highlight only if the corresponding view is visible)
		if (mut3dView.isVisible() &&
		    _mutationDiagram &&
		    _mutationDiagram.isHighlighted())
		{
			highlightSelected();
		}
	}

	function tableChainSelectHandler(pdbId, chainId)
	{
		if (pdbId && chainId)
		{
			_pdbPanelView.selectChain(pdbId, chainId);
			_chainSelectedByTable = true;
		}
	}

	function tableMouseoutHandler()
	{
		_pdbPanelView.pdbPanel.minimizeToHighlighted();
	}

	function tableMouseoverHandler(pdbId, chainId)
	{
		if (pdbId && chainId)
		{
			_pdbPanelView.pdbPanel.minimizeToChain(
				_pdbPanelView.pdbPanel.getChainGroup(pdbId, chainId));
		}
	}

	function initPdbPanel(pdbColl)
	{
		// init pdb panel view if not initialized yet
		if (_pdbPanelView == null)
		{
			_pdbPanelView = mainMutationView.initPdbPanelView(renderOptions.pdbPanel,
				viewOptions.pdbPanel, viewOptions.pdbTable, pdbColl);

			if (_pdbPanelView.pdbPanel)
			{
				// add listeners to the custom event dispatcher of the pdb panel
				_pdbPanelView.pdbPanel.dispatcher.on(
					MutationDetailsEvents.PANEL_CHAIN_SELECTED,
					panelChainSelectHandler);

				_pdbPanelView.pdbPanel.dispatcher.on(
					MutationDetailsEvents.PDB_PANEL_RESIZE_STARTED,
					panelResizeStartHandler);

				_pdbPanelView.pdbPanel.dispatcher.on(
					MutationDetailsEvents.PDB_PANEL_RESIZE_ENDED,
					panelResizeEndHandler);
			}

			// add listeners for the mutation 3d view
			if (viewOptions.pdbTable) {
				_pdbPanelView.addInitCallback(function(event) {
					initPdbTable(pdbColl);
				});
			}
			else {
				// TODO not an ideal way of disabling a view component...
				_pdbPanelView.$el.find(".pdb-table-controls").remove();
			}
		}
	}

	function initPdbTable(pdbColl)
	{
		// init pdb table view if not initialized yet
		if (_pdbTableView == null &&
		    _pdbPanelView != null &&
		    pdbColl.length > 0)
		{
			_pdbTableView = _pdbPanelView.initPdbTableView(pdbColl, function(view, table) {
				// we need to register a callback to add this event listener
				table.dispatcher.on(
					MutationDetailsEvents.PDB_TABLE_READY,
					pdbTableReadyHandler);

				_pdbTableView = view;
			});

			// add listeners to the custom event dispatcher of the pdb table

			_pdbTableView.pdbTable.dispatcher.on(
				MutationDetailsEvents.TABLE_CHAIN_SELECTED,
				tableChainSelectHandler);

			_pdbTableView.pdbTable.dispatcher.on(
				MutationDetailsEvents.TABLE_CHAIN_MOUSEOUT,
				tableMouseoutHandler);

			_pdbTableView.pdbTable.dispatcher.on(
				MutationDetailsEvents.TABLE_CHAIN_MOUSEOVER,
				tableMouseoverHandler);
		}

		if (_pdbPanelView != null &&
		    _pdbTableView != null)
		{
			_pdbPanelView.toggleTableControls();
			_pdbTableView.toggleView();
		}
	}

	function pdbTableReadyHandler()
	{
		if (_pdbPanelView != null)
		{
			// find currently selected chain in the panel
			var gChain = _pdbPanelView.getSelectedChain();

			// select the corresponding row on the table
			if (gChain != null)
			{
				var datum = gChain.datum();
				_pdbTableView.selectChain(datum.pdbId, datum.chain.chainId);
				_pdbTableView.scrollToSelected();
			}
		}
	}
	function diagramResetHandler()
	{
		if (_mut3dVisView && _mut3dVisView.isVisible())
		{
			// reset all previous visualizer filters
			_mut3dVisView.refreshView();
		}
	}

	function diagramUpdateHandler()
	{
		// refresh 3d view with filtered positions
		if (_mut3dVisView && _mut3dVisView.isVisible())
		{
			_mut3dVisView.refreshView();
		}
	}

	function allDeselectHandler()
	{
		if (_mut3dVisView && _mut3dVisView.isVisible())
		{
			_mut3dVisView.resetHighlight();
			_mut3dVisView.hideResidueWarning();
		}
	}

	function diagramDeselectHandler(datum, index)
	{
		// check if the diagram is still highlighted
		if (_mutationDiagram &&
		    _mutationDiagram.isHighlighted())
		{
			// reselect with the reduced selection
			diagramSelectHandler();
		}
		else
		{
			// no highlights (all deselected)
			allDeselectHandler();
		}
	}

	function diagramSelectHandler(datum, index)
	{
		// highlight the corresponding residue in 3D view
		if (_mut3dVisView && _mut3dVisView.isVisible())
		{
			highlightSelected();
		}
	}

	function diagramMouseoverHandler(datum, index)
	{
		// highlight the corresponding residue in 3D view
		if (_mut3dVisView && _mut3dVisView.isVisible())
		{
			// selected pileups (mutations) on the diagram
			var pileups = getSelectedPileups();

			// add the mouse over datum
			pileups.push(datum);

			// highlight (selected + mouseover) residues
			highlight3dResidues(pileups, true);
		}
	}

	function diagramMouseoutHandler(datum, index)
	{
		// same as the deselect action...
		diagramDeselectHandler(datum, index);
	}

	function proteinChangeLinkHandler(mutationId)
	{
		var mutation = highlightDiagram(mutationId);

		if (mutation)
		{
			// highlight the corresponding residue in 3D view
			if (_mut3dVisView && _mut3dVisView.isVisible())
			{
				highlightSelected();
			}
		}
	}

	function pdbLinkHandler(mutationId)
	{
		var mutation = highlightDiagram(mutationId);

		if (mutation)
		{
			// reset the view with the selected chain
			reset3dView(mutation.get("pdbMatch").pdbId,
				mutation.get("pdbMatch").chainId);
		}
	}

	// TODO ideally diagram should be highlighted by MutationDiagramController,
	// but we need to make sure that diagram is highlighted before refreshing the 3D view
	// (this needs event handler prioritization which is not trivial)
	function highlightDiagram(mutationId)
	{
		var mutationMap = mutationUtil.getMutationIdMap();
		var mutation = mutationMap[mutationId];

		if (mutation && _mutationDiagram)
		{
			// highlight the corresponding pileup (without filtering the table)
			_mutationDiagram.clearHighlights();
			_mutationDiagram.highlightMutation(mutation.get("mutationSid"));
		}

		return mutation;
	}

	/**
	 * Retrieves the pileup data from the selected mutation diagram
	 * elements.
	 *
	 * @return {Array} an array of Pileup instances
	 */
	function getSelectedPileups()
	{
		var pileups = [];

		if (_mutationDiagram)
		{
			// get mutations for all selected elements
			_.each(_mutationDiagram.getSelectedElements(), function (ele, i) {
				pileups = pileups.concat(ele.datum());
			});
		}

		return pileups;
	}

	/**
	 * Highlights 3D residues for the selected diagram elements.
	 */
	function highlightSelected()
	{
		// selected pileups (mutations) on the diagram
		var selected = getSelectedPileups();

		// highlight residues
		highlight3dResidues(selected);
	}

	/**
	 * Highlight residues on the 3D diagram for the given pileup data.
	 *
	 *
	 * @param pileupData    pileups to be highlighted
	 * @param noWarning     if set true, warning messages are not be updated
	 */
	function highlight3dResidues(pileupData, noWarning)
	{
		// highlight 3D residues for the initially selected diagram elements
		var mappedCount = _mut3dVisView.highlightView(pileupData, true);

		var unmappedCount = pileupData.length - mappedCount;

		// no warning flag is provided, do not update the warning text
		if (noWarning)
		{
			return;
		}

		// show a warning message if there is at least one unmapped selection
		if (unmappedCount > 0)
		{
			_mut3dVisView.showResidueWarning(unmappedCount, pileupData.length);
		}
		else
		{
			_mut3dVisView.hideResidueWarning();
		}
	}

	/**
	 * Resets the 3D view to its initial state. This function also initializes
	 * the PDB panel view if it is not initialized yet.
	 *
	 * @param pdbId     initial pdb structure to select
	 * @param chainId   initial chain to select
	 */
	function reset3dView(pdbId, chainId)
	{
		var gene = geneSymbol;
		var uniprotId = mut3dView.model.uniprotId; // TODO get this from somewhere else

		// init (singleton) 3D panel if not initialized yet
		if (!mutationDetailsView.is3dPanelInitialized())
		{
			mutationDetailsView.init3dPanel();
		}

		var initView = function(pdbColl)
		{
			// init pdb panel view if not initialized yet
			if (_pdbPanelView == null)
			{
				initPdbPanel(pdbColl);
			}

			// reload the visualizer content with the given pdb and chain
			if (_mut3dVisView != null &&
			    _pdbPanelView != null &&
			    pdbColl.length > 0)
			{
				updateColorMapper();
				_pdbPanelView.showView();

				if (pdbId && chainId)
				{
					_pdbPanelView.selectChain(pdbId, chainId);
				}
				else
				{
					// select default chain if none provided
					_pdbPanelView.selectDefaultChain();
				}

				// initiate auto-collapse
				_pdbPanelView.autoCollapse();
			}
		};

		if (mut3dView != null &&
		    _mut3dVisView != null)
		{
			_mut3dVisView.showMainLoader();
			_mut3dVisView.showView();
		}

		// init view with the pdb data
		pdbProxy.getPdbData(uniprotId, initView);
	}

	/**
	 * Updates the color mapper of the 3D visualizer.
	 */
	function updateColorMapper()
	{
		// TODO this is not an ideal solution, but...
		// ...while we have multiple diagrams, the 3d visualizer is a singleton
		if (_mutationDiagram)
		{
			var colorMapper = function(mutationId, pdbId, chain) {
				return _mutationDiagram.mutationColorMap[mutationId];
			};

			_mut3dVis.updateOptions({mutationColorMapper: colorMapper});
		}
	}

	init();

	this.reset3dView = reset3dView;
	this.highlightSelected = highlightSelected;
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Controller class for the Mutation Details view.
 *
 * @author Selcuk Onur Sumer
 */
function MutationDetailsController(
	mutationDetailsView, dataManager, dataProxies, options)
{
	var sampleArray = options.data.sampleList;
	var viewOptions = options.view;
	var renderOptions = options.render;
	var mutationProxy = dataProxies.mutationProxy;
	var pfamProxy = dataProxies.pfamProxy;
	var pdbProxy = dataProxies.pdbProxy;

	var _geneTabView = {};

	// a single 3D view instance shared by all MainMutationView instances
	var _mut3dVisView = null;

	var _3dController = null;

	function init()
	{
		// add listeners to the custom event dispatcher of the view
		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.GENE_TAB_SELECTED,
			geneTabSelectHandler);

		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.GENE_TABS_CREATED,
			geneTabCreateHandler);

		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.VIS_3D_PANEL_INIT,
			vis3dInitHandler);
	}

	function vis3dInitHandler(container)
	{
		var vis3dOpts = viewOptions.vis3d;

		if (!vis3dOpts)
		{
			return;
		}

		var basicOpts = {
			appOptions: {el: container || "#mutation_details"}
		};

		var options = jQuery.extend(true, {}, basicOpts, vis3dOpts);
		var mut3dVis = new Mutation3dVis("default3dView", options);
		mut3dVis.init();
		init3dView(mut3dVis);
	}

	function geneTabSelectHandler(gene)
	{
		if (_geneTabView[gene] == null)
		{
			initView(gene, sampleArray, viewOptions);
		}
	}

	function geneTabCreateHandler()
	{
		// initially hide 3d container
		//init3dView(null);
		mutationDetailsView.$el.find(".mutation-3d-container").hide();

		// init the view for the first gene only
		var genes = mutationProxy.getGeneList();
		initView(genes[0], sampleArray, viewOptions);
	}

	function init3dView(mut3dVis)
	{
		var container3d = mutationDetailsView.$el.find(".mutation-3d-container");

		// init 3D view if the visualizer is available
		if (mut3dVis)
		{
			// TODO remove mutationProxy?
			var mutation3dVisView = new Mutation3dVisView({
				el: container3d,
				config: renderOptions.mutation3dVis,
				mut3dVis: mut3dVis,
				pdbProxy: pdbProxy,
				mutationProxy: mutationProxy
			});

			mutation3dVisView.render();

			// update reference to the 3d vis view
			_mut3dVisView = mutation3dVisView;

			mutationDetailsView.dispatcher.trigger(
				MutationDetailsEvents.VIS_3D_PANEL_CREATED,
				mutation3dVisView);
		}
		// if no visualizer, hide the 3D vis container
		else
		{
			$(container3d).hide();
		}
	}

	/**
	 * Initializes mutation view for the given gene and cases.
	 *
	 * @param gene          hugo gene symbol
     * @param cases         array of case ids (samples)
     * @param viewOptions   [optional] view options
	 */
	function initView(gene, cases, viewOptions)
	{
		// callback function to init view after retrieving
		// sequence information.
		var init = function(sequenceData, mutationData)
		{
			// TODO a new util for each instance instead?
//			var mutationUtil = new MutationDetailsUtil(
//				new MutationCollection(mutationData));
			var mutationUtil = mutationProxy.getMutationUtil();

			var uniprotId = "";

			// TODO get uniprot id(s) from elsewhere
			if (sequenceData) {
				uniprotId = sequenceData.metadata.identifier;
			}

			// prepare data for mutation view
			var model = {geneSymbol: gene,
				mutationData: mutationData,
				dataProxies: dataProxies,
				dataManager: dataManager,
				uniprotId: uniprotId,
				sampleArray: cases};

			// init the main view
			var mainView = new MainMutationView({
				el: "#mutation_details_" + cbio.util.safeProperty(gene),
				config: renderOptions.mainMutation,
				model: model});

			mutationDetailsView.dispatcher.trigger(
				MutationDetailsEvents.MAIN_VIEW_INIT,
				mainView);

			mainView.render();

			// update the references after rendering the view
			_geneTabView[gene].mainMutationView = mainView;
			dataManager.addView(gene, mainView);

			// no mutation data, nothing to show...
			if (mutationData == null ||
			    mutationData.length == 0)
			{
				mainView.showNoDataInfo();
			}
			else
			{
				initComponents(mainView, gene, mutationUtil, sequenceData, viewOptions);
			}
		};

		// get mutation data for the current gene
		mutationProxy.getMutationData(gene, function(data) {
			// init reference mapping
			_geneTabView[gene] = {};

			// create an empty array if data is null
			if (data == null)
			{
				data = [];
			}

			// get the sequence data for the current gene & init view

			// get the most frequent uniprot accession string (excluding "NA")
			var uniprotInfo = mutationProxy.getMutationUtil().dataFieldCount(
				gene, "uniprotAcc", ["NA"]);

			var uniprotAcc = null;
			var servletParams = {geneSymbol: gene};

			if (uniprotInfo.length > 0)
			{
				uniprotAcc = uniprotInfo[0].uniprotAcc;
			}

			// if exists, also add uniprotAcc to the servlet params
			if (uniprotAcc)
			{
				servletParams.uniprotAcc = uniprotAcc;
			}

			// TODO table can be initialized without the PFAM data...
			pfamProxy.getPfamData(servletParams, function(sequenceData) {
				// sequenceData may be null for unknown genes...
				var sequence = null;

				if (sequenceData == null) {
					console.log("[warning] no pfam data found: %o", servletParams);
				}
				else {
					// get the first sequence from the response
					sequence = sequenceData[0];
				}

				// get annotation data in any case
				dataManager.getData("variantAnnotation",
                    {mutations: data},
                    function(params, data) {
	                    init(sequence, params.mutations);
                    });

			});
		});
	}

	function initComponents(mainView, gene, mutationUtil, sequenceData, viewOptions)
	{
		var diagramOpts = viewOptions.mutationDiagram;
		var tableOpts = viewOptions.mutationTable;
		var vis3dOpts = viewOptions.vis3d;
		var infoPanelOpts = viewOptions.infoPanel;
		var summaryOpts = viewOptions.mutationSummary;

		// init mutation table
		var tableView = null;

		if (tableOpts)
		{
			tableView = mainView.initMutationTableView(tableOpts);
			new MutationDetailsTableController(mainView, mutationDetailsView);
		}

		var summaryView = null;

		if (summaryOpts)
		{
			summaryView = mainView.initSummaryView(tableOpts);
		}

		// init mutation diagram
		var diagramView = null;

		function initDiagram()
		{
			if (diagramOpts && sequenceData)
			{
				diagramView = mainView.initMutationDiagramView(diagramOpts, sequenceData);

				var mutationTable = null;

				if (tableView)
				{
					mutationTable = tableView.mutationTable;
				}

				var infoView = null;

				// TODO info view can be initialized without depending on diagram view!
				if (infoPanelOpts)
				{
					infoView = mainView.initMutationInfoView(infoPanelOpts);
					new MutationInfoController(mainView);
				}

				new MutationDiagramController(
					diagramView.mutationDiagram, mutationTable, infoView, mutationUtil);
			}
		}

		if (mutationUtil.containsProteinChange(gene))
		{
			initDiagram();
		}
		// cannot initialize mutation diagram without protein change data
		else
		{
			dataManager.getData("variantAnnotation",
				//{mutationTable: tableView.mutationTable},
				{mutations: mainView.model.mutationData},
			    function(params, data) {
					initDiagram();
				});

			// TODO diagram place holder?
		}

		// init main mutation controller
		new MainMutationController(mainView);

		if (vis3dOpts)
		{
			// just init the 3D button
			var view3d = mainView.init3dView(null);

			_3dController = new Mutation3dController(mutationDetailsView, mainView, viewOptions, renderOptions,
				_mut3dVisView, view3d, pdbProxy, mutationUtil, gene);

			if (renderOptions.mutationDetails.activate3dOnInit)
			{
				_3dController.reset3dView(renderOptions.mutationDetails.activate3dOnInit.pdbId,
				                          renderOptions.mutationDetails.activate3dOnInit.chain);
			}
		}
	}

	init();

	// public functions
	this.getMainView = function(key)
	{
		return _geneTabView[key];
	};

	this.get3dController = function() {return _3dController;};
	this.get3dVisView = function() {return _mut3dVisView;};
	this.getMainViews = function() {return _geneTabView;};
	this.getDataManager = function() {return dataManager};
	this.getDataProxies = function() {return dataProxies};
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Singleton utility class to define custom events triggered by
 * Mutation Details components.
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsEvents = (function()
{
	var _lollipopSelected = "mutationDiagramLollipopSelected";
	var _lollipopDeselected = "mutationDiagramLollipopDeselected";
	var _allLollipopsDeselected = "mutationDiagramAllDeselected";
	var _lollipopMouseover = "mutationDiagramLollipopMouseover";
	var _lollipopMouseout = "mutationDiagramLollipopMouseout";
	var _mainViewInit = "mainMutationViewInit";
	var _diagramInit = "mutationDiagramInitialized";
	var _diagramPlotUpdated = "mutationDiagramPlotUpdated";
	var _diagramPlotReset = "mutationDiagramPlotReset";
	var _mutationTableFiltered = "mutationTableFiltered";
	var _mutationTableInitialized = "mutationTableInitialized";
	var _mutationTableRedrawn = "mutationTableRedrawn";
	var _mutationTableHeaderCreated = "mutationTableHeaderCreated";
	var _proteinChangeLinkClicked = "mutationTableProteinChangeLinkClicked";
	var _mutationTypeSelected = "infoPanelMutationTypeSelected";
	var _infoPanelInit = "infoPanelInit";
	var _pdbLinkClicked = "mutationTablePdbLinkClicked";
	var _pdbPanelResizeStarted = "mutationPdbPanelResizeStarted";
	var _pdbPanelResizeEnded = "mutationPdbPanelResizeEnded";
	var _panelChainSelected = "mutationPdbPanelChainSelected";
	var _tableChainSelected = "mutationPdbTableChainSelected";
	var _tableChainMouseout = "mutationPdbTableChainMouseout";
	var _tableChainMouseover = "mutationPdbTableChainMouseover";
	var _pdbTableReady = "mutationPdbTableReady";
	var _geneTabSelected = "mutationDetailsGeneTabSelected";
	var _geneTabsCreated = "mutationDetailsGeneTabsCreated";
	var _3dVisInit = "mutation3dPanelInit";
	var _3dVisCreated = "mutation3dPanelCreated";
	var _3dPanelClosed = "mutation3dPanelClosed";
	var _3dStructureReloaded = "mutation3dStructureReloaded";

	return {
		LOLLIPOP_SELECTED: _lollipopSelected,
		LOLLIPOP_DESELECTED: _lollipopDeselected,
		LOLLIPOP_MOUSEOVER: _lollipopMouseover,
		LOLLIPOP_MOUSEOUT: _lollipopMouseout,
		ALL_LOLLIPOPS_DESELECTED: _allLollipopsDeselected,
		MAIN_VIEW_INIT: _mainViewInit,
		DIAGRAM_INIT: _diagramInit,
		DIAGRAM_PLOT_UPDATED: _diagramPlotUpdated,
		DIAGRAM_PLOT_RESET: _diagramPlotReset,
		MUTATION_TABLE_INITIALIZED: _mutationTableInitialized,
		MUTATION_TABLE_FILTERED: _mutationTableFiltered,
		MUTATION_TABLE_REDRAWN: _mutationTableRedrawn,
		MUTATION_TABLE_HEADER_CREATED: _mutationTableHeaderCreated,
		PROTEIN_CHANGE_LINK_CLICKED: _proteinChangeLinkClicked,
		INFO_PANEL_MUTATION_TYPE_SELECTED: _mutationTypeSelected,
		INFO_PANEL_INIT: _infoPanelInit,
		PDB_LINK_CLICKED: _pdbLinkClicked,
		PDB_PANEL_RESIZE_STARTED: _pdbPanelResizeStarted,
		PDB_PANEL_RESIZE_ENDED: _pdbPanelResizeEnded,
		PANEL_CHAIN_SELECTED: _panelChainSelected,
		TABLE_CHAIN_SELECTED: _tableChainSelected,
		TABLE_CHAIN_MOUSEOUT: _tableChainMouseout,
		TABLE_CHAIN_MOUSEOVER: _tableChainMouseover,
		PDB_TABLE_READY: _pdbTableReady,
		GENE_TAB_SELECTED: _geneTabSelected,
		GENE_TABS_CREATED: _geneTabsCreated,
		VIS_3D_PANEL_INIT: _3dVisInit,
		VIS_3D_PANEL_CREATED: _3dVisCreated,
		VIEW_3D_STRUCTURE_RELOADED: _3dStructureReloaded,
		VIEW_3D_PANEL_CLOSED: _3dPanelClosed
	};
})();

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Controller class for the Mutation Table view.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @param mainMutationView  a MainMutationView instance
 * @param mutationDetailsView   a MutationDetailsView instance
 *
 * @author Selcuk Onur Sumer
 */
function MutationDetailsTableController(mainMutationView, mutationDetailsView)
{
	var _mutationDiagram = null;

	function init()
	{
		if (mainMutationView.diagramView)
		{
			diagramInitHandler(mainMutationView.diagramView.mutationDiagram);
		}
		else
		{
			mainMutationView.dispatcher.on(
				MutationDetailsEvents.DIAGRAM_INIT,
				diagramInitHandler);
		}

		if (mainMutationView.infoView)
		{
			infoPanelInitHandler(mainMutationView.infoView);
		}
		else
		{
			mainMutationView.dispatcher.on(
				MutationDetailsEvents.INFO_PANEL_INIT,
				infoPanelInitHandler);
		}

		// add listeners for the mutation details view
		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.GENE_TAB_SELECTED,
			geneTabSelectHandler);
	}

	function diagramInitHandler(mutationDiagram)
	{
		// update class variable
		_mutationDiagram = mutationDiagram;

		// add listeners to the custom event dispatcher of the diagram
		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.ALL_LOLLIPOPS_DESELECTED,
			allDeselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_DESELECTED,
			deselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_SELECTED,
			selectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_MOUSEOVER,
			mouseoverHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_MOUSEOUT,
			mouseoutHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_RESET,
			diagramResetHandler);
	}

	function infoPanelInitHandler(infoView)
	{
		// add listeners to the custom event dispatcher of the info panel view
		if (infoView)
		{
			infoView.dispatcher.on(
				MutationDetailsEvents.INFO_PANEL_MUTATION_TYPE_SELECTED,
				infoPanelFilterHandler);
		}
	}

	function diagramResetHandler()
	{
		if (mainMutationView.tableView)
		{
			// reset all previous table filters
			mainMutationView.tableView.resetFilters();
		}
	}

	function allDeselectHandler()
	{
		if (mainMutationView.tableView)
		{
			// remove all table highlights
			mainMutationView.tableView.clearHighlights();

			// filter with all visible diagram mutations
			mainMutationView.tableView.filter(PileupUtil.getPileupMutations(
				_mutationDiagram.pileups));
		}
	}

	function deselectHandler(datum, index)
	{
		if (mainMutationView.tableView)
		{
			// remove all table highlights
			mainMutationView.tableView.clearHighlights();

			var mutations = [];

			// get mutations for all selected elements
			if (_mutationDiagram)
			{
				_.each(_mutationDiagram.getSelectedElements(), function (ele, i) {
					mutations = mutations.concat(ele.datum().mutations);
				});
			}

			// reselect with the reduced selection
			if (mutations.length > 0)
			{
				// filter table for the selected mutations
				mainMutationView.tableView.filter(mutations);
			}
			// rollback only if none selected
			else
			{
				// filter with all visible diagram mutations
				mainMutationView.tableView.filter(PileupUtil.getPileupMutations(
					_mutationDiagram.pileups));
			}
		}
	}

	function selectHandler(datum, index)
	{
		if (mainMutationView.tableView)
		{
			// remove all table highlights
			mainMutationView.tableView.clearHighlights();

			var mutations = [];

			// get mutations for all selected elements
			if (_mutationDiagram)
			{
				_.each(_mutationDiagram.getSelectedElements(), function (ele, i)
				{
					mutations = mutations.concat(ele.datum().mutations);
				});
			}

			// filter table for the selected mutations
			mainMutationView.tableView.filter(mutations);
		}
	}

	function infoPanelFilterHandler(mutationType)
	{
		if (mainMutationView.tableView !== null)
		{
			// get currently filtered mutations
			var mutations = mainMutationView.infoView.currentMapByType[mutationType];

			if (_.size(mutations) > 0)
			{
				mainMutationView.tableView.filter(mutations);
			}
			// if all the mutations of this type are already filtered out,
			// then show all mutations of this type
			else
			{
				mutations = mainMutationView.infoView.initialMapByType[mutationType];
				mainMutationView.tableView.filter(mutations);
				// clear search box value since the filtering with that value is no longer valid
				mainMutationView.tableView.clearSearchBox();
			}
		}
	}

	function mouseoverHandler(datum, index)
	{
		if (mainMutationView.tableView)
		{
			// highlight mutations for the provided mutations
			mainMutationView.tableView.highlight(datum.mutations);
		}
	}

	function mouseoutHandler(datum, index)
	{
		if (mainMutationView.tableView)
		{
			// remove all highlights
			mainMutationView.tableView.clearHighlights();
		}
	}

	function geneTabSelectHandler(gene)
	{
		if (mainMutationView.tableView)
		{
			var oTable = mainMutationView.tableView.mutationTable.getDataTable();

			// alternatively we can check if selected gene is this view's gene
			if (oTable.is(":visible"))
			{
				oTable.fnAdjustColumnSizing();
			}
		}
	}

	init();
}

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Controller class for the Mutation Diagram.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @author Selcuk Onur Sumer
 */
function MutationDiagramController(mutationDiagram, mutationTable, infoPanelView, mutationUtil)
{
	function init()
	{
		// add listeners to the custom event dispatcher of the mutation table
		if (mutationTable)
		{
			mutationTable.dispatcher.on(
				MutationDetailsEvents.MUTATION_TABLE_FILTERED,
				tableFilterHandler);
		}

		// TODO add info panel init handler, this will require controller parameter modification/simplification
		// add listeners to the custom event dispatcher of the info panel view
		if (infoPanelView)
		{
			infoPanelView.dispatcher.on(
				MutationDetailsEvents.INFO_PANEL_MUTATION_TYPE_SELECTED,
				infoPanelFilterHandler);
		}

		// TODO make sure to call these event handlers before 3D controller's handler,
		// otherwise 3D update will not work properly.
		// (this requires event handler prioritization which is not trivial)

		// add listeners for the mutation table view

//		mutationTable.dispatcher.on(
//			MutationDetailsEvents.PROTEIN_CHANGE_LINK_CLICKED,
//			proteinChangeLinkHandler);

//		mutationTable.dispatcher.on(
//			MutationDetailsEvents.PDB_LINK_CLICKED,
//			proteinChangeLinkHandler);
	}

	function tableFilterHandler(tableSelector)
	{
		var currentMutations = [];

		// add current (filtered) mutations into an array
		var rowData = [];

		// TODO this try/catch block is for backward compatibility,
		// we will no longer need this once we completely migrate to DataTables 1.10
		try {
			// first, try new API.
			// this is not backward compatible, requires DataTables 1.10 or later.
			rowData = $(tableSelector).DataTable().rows({filter: "applied"}).data();
		} catch(err) {
			// if DataTables 1.10 is not available, try the old API function.
			// DataTables 1.9.4 compatible code (which doesn't work with deferRender):
			rowData = $(tableSelector).dataTable()._('tr', {filter: "applied"});
		}

		_.each(rowData, function(data, index) {
			// assuming only the first element contains the datum
			var mutation = data[0].mutation;

			if (mutation)
			{
				currentMutations.push(mutation);
			}
		});

		// update mutation diagram with the current mutations
		if (mutationDiagram !== null)
		{
			var mutationData = new MutationCollection(currentMutations);
			mutationDiagram.updatePlot(mutationData);
		}
	}

	function infoPanelFilterHandler(mutationType)
	{
		if (mutationDiagram !== null)
		{
			// get currently filtered mutations
			var mutations = infoPanelView.currentMapByType[mutationType];

			if (_.size(mutations) > 0)
			{
				mutationDiagram.updatePlot(new MutationCollection(mutations));
			}
			// if all the mutations of this type are already filtered out,
			// then show all mutations of this type
			else
			{
				mutations = infoPanelView.initialMapByType[mutationType];
				mutationDiagram.updatePlot(new MutationCollection(mutations));
			}
		}
	}

	function proteinChangeLinkHandler(mutationId)
	{
		var mutationMap = mutationUtil.getMutationIdMap();
		var mutation = mutationMap[mutationId];

		if (mutation)
		{
			// highlight the corresponding pileup (without filtering the table)
			mutationDiagram.clearHighlights();
			mutationDiagram.highlightMutation(mutation.get("mutationSid"));
		}
	}

	init();
}

/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Controller class for the Mutation Diagram.
 * Listens to the various events and make necessary changes
 * on the view wrt each event type.
 *
 * @author Selcuk Onur Sumer
 */
function MutationInfoController(mainMutationView)
{
	var _mutationDiagram = null;

	function init()
	{
		// TODO if diagram is disabled, use table data instead...

		if (mainMutationView.diagramView)
		{
			diagramInitHandler(mainMutationView.diagramView.mutationDiagram);
		}
		else
		{
			mainMutationView.dispatcher.on(
				MutationDetailsEvents.DIAGRAM_INIT,
				diagramInitHandler);
		}
	}

	function diagramInitHandler(mutationDiagram)
	{
		// update class variable
		_mutationDiagram = mutationDiagram;

		// add listeners to the custom event dispatcher of the diagram
		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_RESET,
			diagramResetHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.DIAGRAM_PLOT_UPDATED,
			diagramUpdateHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_SELECTED,
			selectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.LOLLIPOP_DESELECTED,
			deselectHandler);

		mutationDiagram.dispatcher.on(
			MutationDetailsEvents.ALL_LOLLIPOPS_DESELECTED,
			allDeselectHandler);
	}

	function allDeselectHandler()
	{
		diagramUpdateHandler();
	}

	function deselectHandler(datum, index)
	{
		if (mainMutationView.infoView)
		{
			var pileups = [];

			// get pileups for all selected elements
			if (_mutationDiagram)
			{
				_.each(_mutationDiagram.getSelectedElements(), function (ele, i) {
					pileups = pileups.concat(ele.datum());
				});
			}

			// reselect with the reduced selection
			if (pileups.length > 0)
			{
				mainMutationView.infoView.updateView(
					PileupUtil.getPileupMutations(pileups));
			}
			// rollback only if none selected
			else
			{
				// roll back the table to its previous state
				// (to the last state when a manual filtering applied)
				diagramUpdateHandler();
			}
		}
	}

	function selectHandler(datum, index)
	{
		deselectHandler(datum, index);
	}

	function diagramResetHandler()
	{
		diagramUpdateHandler();
	}

	function diagramUpdateHandler()
	{
		if (mainMutationView.infoView)
		{
			mainMutationView.infoView.updateView(
				PileupUtil.getPileupMutations(_mutationDiagram.pileups));
		}
	}

	init();
}


/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Main wrapper for the whole mutation mapper instance.
 *
 * @param options   data, view options, and proxy settings
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationMapper(options)
{
	var self = this;
	var _mutationDetailsView = null;
	var _mutationDetailsController = null;

	// default options object
	var _defaultOpts = {
		// target html container
		el: "#mutation_details",
		// initial data (genes & samples)
		data: {
			geneList: [],
			sampleList: []
		},
		// view component options
		view: {
			mutationDiagram: {},
			mutationTable: {},
			mutationSummary: {},
		    pdbPanel: {},
			pdbTable: {},
			infoPanel: {},
			vis3d: {}
		},
		// TODO make all backbone view classes customizable this way!
		// this is mainly to override the default rendering behavior of backbone views
		render: {
			// MutationDetailsView options
			mutationDetails: {
				init: null, // function for custom init
				format: null, // function for custom format
				activate3dOnInit: false
			},
			mainMutation: {},
			pdbPanel: {},
			mutation3dVis: {}
		},
		// data proxy configuration
		// instance: custom instance, if provided all other parameters are ignored
		// instanceClass: constructor to initialize the data proxy
		// options: options to be passed to the data proxy constructor (see AbstractDataProxy default options)
		proxy: {
			pfamProxy: {
				instance: null,
				instanceClass: PfamDataProxy,
				options: {
					data: {}
				}
			},
			variantAnnotationProxy: {
				instance: null,
				instanceClass: VariantAnnotationDataProxy,
				options: {
					data: {}
				}
			},
			mutationProxy: {
				instance: null,
				instanceClass: MutationDataProxy,
				options: {
					data: {},
					params: {},
					geneList: ""
				}
			},
			clinicalProxy: {
				instance: null,
				instanceClass: ClinicalDataProxy,
				options: {
					data: {}
				}
			},
			pdbProxy: {
				instance: null,
				instanceClass: PdbDataProxy,
				options: {
					data: {
						pdbData: {},
						infoData: {},
						summaryData: {},
						positionData: {}
					},
					mutationUtil: {}
				}
			},
			pancanProxy: {
				instance: null,
				instanceClass: PancanMutationDataProxy,
				options: {
					data: {
						byKeyword: {},
						byProteinChange: {},
						byProteinPosition: {},
						byGeneSymbol: {}
					}
				}
			},
			mutationAlignerProxy: {
				instance: null,
				instanceClass: MutationAlignerDataProxy,
				options: {
					data: {}
				}
			},
			portalProxy: {
				instance: null,
				instanceClass: PortalDataProxy,
				options: {
					data: {}
				}
			}
		},
		// data manager configuration,
		// dataFn: additional custom data retrieval functions
		// dataProxies: additional data proxies
		dataManager: {
			dataFn: {},
			dataProxies: {}
		}
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	function init()
	{
		_options.proxy.mutationProxy.options.geneList = _options.data.geneList.join(" ");

		// init all data proxies & data manager
		var dataProxies = DataProxyUtil.initDataProxies(_options.proxy);
		_options.dataManager = jQuery.extend(true, {}, _options.dataManager, {dataProxies: dataProxies});
		var dataManager = new MutationDataManager(_options.dataManager);

		// TODO pass other view options (pdb table, pdb diagram, etc.)

		var model = {
			mutationProxy: dataProxies.mutationProxy
		};

		var viewOptions = {el: _options.el,
			config: _options.render.mutationDetails,
			model: model};

		var mutationDetailsView = new MutationDetailsView(viewOptions);
		_mutationDetailsView = mutationDetailsView;

		// init main controller...
		var controller = new MutationDetailsController(
			mutationDetailsView,
			dataManager,
			dataProxies,
			_options);

		_mutationDetailsController = controller;

		// ...and let the fun begin!
		mutationDetailsView.render();
	}

	this.init = init;
	this.getView = function() {return _mutationDetailsView;};
	this.getController = function() {return _mutationDetailsController;};
}
