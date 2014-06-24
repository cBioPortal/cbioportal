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
			end: this.model.end};

		// compile the template using underscore
		return _.template(
				$("#mutation_details_region_tip_template").html(),
				variables);
	}
});
