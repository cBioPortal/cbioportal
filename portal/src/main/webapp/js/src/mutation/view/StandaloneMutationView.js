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
		var inputField = self.$el.find(".standalone-mutation-input");
		var visualize = self.$el.find(".submit-custom-mutations");
		var textArea = self.$el.find(".mutation-file-example");
		var toggleFullList = self.$el.find(".toggle-full-header-list");
		var toggleInputField = self.$el.find(".toggle-mutation-input-field");
		var listTriangleDown = self.$el.find(".full-header-list-expander .triangle-down");
		var inputTriangleRight = self.$el.find(".mutation-input-field-expander .triangle-right");
		var listTriangle = self.$el.find(".full-header-list-expander .triangle");
		var inputTriangle = self.$el.find(".mutation-input-field-expander .triangle");
		var inputExpander = self.$el.find(".mutation-input-field-expander");

		self._initInputHeaderTable();
		fullList.hide();
		inputExpander.hide();

		textArea.resizable();

		toggleFullList.click(function(event) {
			event.preventDefault();
			fullList.slideToggle();
			listTriangle.toggle();
		});

		toggleInputField.click(function(event) {
			event.preventDefault();
			inputField.slideToggle();
			inputTriangle.toggle();
		});

		listTriangleDown.hide();
		inputTriangleRight.hide();

		// make triangles clickable, too

		listTriangle.click(function(event) {
			// same as clicking on the link
			toggleFullList.click();
		});

		inputTriangle.click(function(event) {
			// same as clicking on the link
			toggleInputField.click();
		});

		visualize.click(function() {
			var mutationForm = self.$el.find(".mutation-file-form");

			// hide the input field and show the expander link
			inputExpander.slideDown();
			toggleInputField.click();

			// upload input data and init view
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
			{columnHeader: 'Reference_Allele',
				description: 'The plus strand reference allele at this position',
				example: 'A'},
			{columnHeader: 'Variant_Allele',
				description: 'Tumor sequencing (discovery) allele',
				example: 'C'},
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
