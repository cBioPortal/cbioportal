/**
 * PDB Table View.
 *
 * This view is designed to function in parallel with the 3D visualizer.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: hugo gene symbol,
 *                   pdbColl: collection of PdbModel instances,
 *                   pdbProxy: pdb data proxy},
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PdbTableView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};
	},
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var template = _.template($("#pdb_table_view_template").html(), {});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// init pdb table
		self.pdbTable = self._initPdbTable();

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;

		// TODO hide view initially
		//self.$el.hide();
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
	/**
	 * Initializes the PDB chain table.
	 *
	 * @return {MutationPdbTable}   table instance
	 */
	_initPdbTable: function()
	{
		var self = this;
		var table = null;

		var pdbColl = self.model.pdbColl;
		var pdbProxy = self.model.pdbProxy;

		var options = {el: self.$el.find(".mutation-pdb-table-container")};
		var rows = self._generateRowData(pdbColl);
		var headers = ["PDB id", "Chain"];

		// init panel
		table = new MutationPdbTable(options, rows, headers);
		table.renderTable();

		return table;
	},
	_generateRowData: function(pdbColl)
	{
		var rows = [];

		pdbColl.each(function(pdb) {
			pdb.chains.each(function(chain) {
				rows.push([pdb.pdbId, chain.chainId]);
			})
		});

		return rows;
	}
});

