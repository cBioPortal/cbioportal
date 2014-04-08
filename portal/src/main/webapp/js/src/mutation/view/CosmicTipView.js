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
		return _.template(
				$("#mutation_details_cosmic_tip_template").html(),
				variables);
	}
});