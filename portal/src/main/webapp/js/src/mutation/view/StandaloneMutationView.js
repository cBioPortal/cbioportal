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
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

		var fullList = self.$el.find(".mutation-data-info");
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
		var loadExampleData = self.$el.find(".load-example-data-link");
		var releaseNotes = self.$el.find(".standalone-release-notes");

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

		loadExampleData.click(function(event) {
			event.preventDefault();
			var templateFn = _.template($("#example_mutation_data_template").html());
			textArea.val(templateFn({}).trim());
		});

		releaseNotes.click(function(event) {
			event.preventDefault();

			var url = "release_notes_mutation_mapper.jsp";

			var newWindow = window.open(url,
				"MutationMapperReleaseNotes",
				"height=600,width=800,left=400,top=0,scrollbars=yes");

			if (window.focus) {
				newWindow.focus();
			}
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
				description: 'Tumor sample ID',
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
				description: 'Center/Institute reporting the variant',
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
	},
	popitup: function(url)
	{


		return false;
	}
});
