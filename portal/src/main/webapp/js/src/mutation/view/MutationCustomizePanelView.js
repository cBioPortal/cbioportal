/**
 * Mutation Details Customization Panel View.
 *
 * This view is designed to provide a customization panel for Mutation Details page.
 *
 * options: {el: [target container],
 *           model: {},
 *           diagram: reference to the MutationDiagram instance
 *          }
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
			maxY: diagram.getMaxY()};

		// compile the template using underscore
		var template = _.template(
				$("#mutation_customize_panel_template").html(),
				variables);

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

		customizeClose.click(function(event) {
			event.preventDefault();
			self.toggleView();
		});

		// init y-axis slider controls
		yAxisSlider.slider({value: diagram.getMaxY(),
			min: 2,
			max: diagram.getMaxY(),
			change: function(event, ui) {
				diagram.updateOptions({maxLengthY: ui.value});
				diagram.rescaleYAxis();
		}});
	},
	toggleView: function() {
		var self = this;
		self.$el.slideToggle();
	}
});
