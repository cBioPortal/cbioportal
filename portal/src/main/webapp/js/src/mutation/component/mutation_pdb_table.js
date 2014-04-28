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
				tip:""},
			chain: {sTitle: "Chain",
				tip:""},
			uniprotPos: {sTitle: "Uniprot Positions",
				tip:"",
				sType: "numeric"},
			identityPercent: {sTitle: "Identity Percent",
				tip:"",
				sType: "numeric"},
			organism: {sTitle: "Organism",
				tip:""},
			summary: {sTitle: "Summary",
				tip:"",
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
				return _.template($("#mutation_pdb_table_pdb_cell_template").html(),
		                  {pdbId: datum.pdbId});
			},
			chain: function(datum) {
				// format using the corresponding template
				return _.template($("#mutation_pdb_table_chain_cell_template").html(),
		                  {chainId: datum.chain.chainId});
			},
			organism: function(datum) {
				return datum.organism;
			},
			summary: function(datum) {
				var vars = {summary: datum.summary.title,
					molecule: datum.summary.molecule};

				// format using the corresponding template
				return _.template(
					$("#mutation_pdb_table_summary_cell_template").html(),
					vars);
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
				$(selector).find('.simple-tip').qtip(qTipOptions);
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