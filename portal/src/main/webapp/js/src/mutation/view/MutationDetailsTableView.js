/**
 * Default table view for the mutations.
 *
 * options: {el: [target container],
 *           model: {mutations: mutation data as an array of JSON objects,
 *                   geneSymbol: hugo gene symbol as a string,
 *                   tableOpts: mutation table options (optional)}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsTableView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var template = _.template($("#mutation_details_table_template").html(),
		                          {loaderImage: "images/ajax-loader.gif"});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// init pdb table
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
			options, self.model.geneSymbol, mutationUtil);

		// TODO self.mutationTable = table;
		self.tableUtil = table;

		if (_.isFunction(callback))
		{
			callback(self, table);
		}

		self._generateRowData(table.getColumnOptions(), mutationColl, function(rowData) {
			// init table with the row data
			table.renderTable(rowData);
			// hide loader image
			//self.$el.find(".mutation-details-table-loader").hide();
		});

		return table;
	},
	_generateRowData: function(headers, mutationColl, callback)
	{
		// TODO make all additional ajax calls here?

		var rows = [];

		mutationColl.each(function(mutation) {
			// only set the datum
			var datum = {mutation: mutation};
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
            var row = tableSelector.find("tr." + mutations[i].mutationSid);
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
		var oTable = self.tableUtil.getDataTable();

		// construct regex
		var ids = [];

		for (var i = 0; i < mutations.length; i++)
		{
			ids.push(mutations[i].mutationSid);
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
		self.tableUtil.setEventActive(false);

		// apply filter
		self._applyFilter(oTable, regex, asRegex, updateBox, limit);

		// enable events after filtering
		self.tableUtil.setEventActive(true);
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
		self.tableUtil.cleanFilters();
	},
	/**
	 * Rolls back the table to the last state where a manual search
	 * (manual filtering) performed. This function is required since
	 * we also filter the table programmatically.
	 */
	rollBack: function()
	{
		var self = this;
		var oTable = self.tableUtil.getDataTable();

		// disable event triggering before filtering, otherwise it creates a chain reaction
		self.tableUtil.setEventActive(false);

		// re-apply last manual filter string
		var searchStr = self.tableUtil.getManualSearch();
		self._applyFilter(oTable, searchStr, false);

		// enable events after filtering
		self.tableUtil.setEventActive(true);
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

		oTable.fnFilter(filterStr, limit, asRegex, smartFilter, updateBox, caseInsensitive);
	}
});
