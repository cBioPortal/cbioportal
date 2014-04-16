/**
 * Standalone Mutation View.
 *
 * This view is designed to visualize mutations in a separate page.
 *
 * options: {el: [target container]}
 *
 * @author Selcuk Onur Sumer
 */
var StandaloneMutationView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};
	},
	render: function() {
		var self = this;

		// compile the template using underscore
		var template = _.template($("#standalone_mutation_view_template").html(), {});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;

		var fullList = self.$el.find(".full-list-of-headers");
		var visualize = self.$el.find(".submit-custom-mutations");
		var textArea = self.$el.find(".mutation-file-example");

		self._initInputHeaderTable();
		fullList.hide();

		textArea.resizable();

		self.$el.find(".toggle-full-header-list").click(function() {
			fullList.slideToggle();
		});

		visualize.click(function() {
			var mutationForm = self.$el.find(".mutation-file-form");

			self._postFile('echofile', new FormData(mutationForm[0]), function(data) {
				var inputVal = textArea.val();

				// if no file selected, use the text area input
				var input = _.isEmpty(data) ? inputVal : data.mutation;

				// TODO fire an event instead?
				if (_.isFunction(self._initCallback))
				{
					self._initCallback(input);
				}
			});
		});
	},
	addInitCallback: function(callback) {
		var self = this;

		self._initCallback = callback;
	},
	_initInputHeaderTable: function()
	{
		var self = this;

		// TODO add missing columns...
		var tableData = [
			{columnHeader: 'Hugo_Symbol',
				description: 'HUGO symbol for the gene',
				example: 'TP53'},
			{columnHeader: 'Protein_Change',
				description: 'Amino acid change',
				example: 'V600E'},
			{columnHeader: 'Sample_ID',
				description: 'Tumor sample barcode',
				example: 'TCGA-B5-A11E'},
			{columnHeader: 'Mutation_Type',
				description: 'Translational effect of variant allele',
				example: 'Missense_Mutation, Nonsense_Mutation, etc.'},
			{columnHeader: 'Chromosome',
				description: 'Chromosome number',
				example: 'X, Y, M, 1, 2, etc.'},
			{columnHeader: 'Start_Position',
				description: 'Lowest numeric position of the reported variant on the genomic reference sequence',
				example: '666'},
			{columnHeader: 'End_Position',
				description: 'Highest numeric position of the reported variant on the genomic reference sequence',
				example: '667'},
			{columnHeader: 'Validation_Status',
				description: 'Second pass results from orthogonal technology',
				example: 'Valid'},
			{columnHeader: 'Mutation_Status',
				description: 'Mutation status',
				example: 'Somatic, Germline, etc.'},
			{columnHeader: 'Center',
				description: 'Genome sequencing center reporting the variant',
				example: 'mskcc.org'}
		];

		var rows = [];

		_.each(tableData, function(ele, idx) {
			rows.push([ele.columnHeader, ele.description, ele.example]);
		});

		var tableOpts = {
			"sDom": '<t>',
			"bJQueryUI": true,
			"bPaginate": false,
			"bFilter": true,
			"sScrollY": "600px",
			"bScrollCollapse": true,
			"aaData" : rows,
			"aoColumns" : [{sTitle: "Column Header", sWidth: "25%"},
				{sTitle: "Description", sWidth: "50%"},
				{sTitle: "Example", sWidth: "25%"}],
			aaSorting: [] // initially turn off sorting
		};

		// init table & return the instance
		return self.$el.find(".header-details-table").dataTable(tableOpts);
	},
	// TODO this is a duplicate code: see oncoprint/custom-boilerplate.js
	_postFile: function (url, formData, callback)
	{
		$.ajax({
			url: url,
			type: 'POST',
			success: callback,
			data: formData,
			//Options to tell jQuery not to process data or worry about content-type.
			cache: false,
			contentType: false,
			processData: false
		});
	}
});
