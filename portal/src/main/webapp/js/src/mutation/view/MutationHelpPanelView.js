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
		var template = _.template(
				$("#mutation_help_panel_template").html(),
				{});

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
