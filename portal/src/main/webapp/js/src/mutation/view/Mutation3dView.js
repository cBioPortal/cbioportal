/**
 * 3D visualizer controls view.
 *
 * This view is designed to provide controls to initialize, show or hide
 * the actual 3D visualizer panel.
 *
 * IMPORTANT NOTE: This view does not initialize the actual 3D visualizer.
 * 3D visualizer is a global instance bound to MutationDetailsView
 * and it is a part of Mutation3dVisView.
 *
 * options: {el: [target container],
 *           model: {geneSymbol: hugo gene symbol,
 *                   uniprotId: uniprot identifier for this gene,
 *                   pdbProxy: pdb data proxy}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;
		var gene = self.model.geneSymbol;

		// compile the template using underscore
		var template = _.template(
				$("#mutation_3d_view_template").html(), {});

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);

		// format after rendering
		this.format();
	},
	format: function()
	{
		var self = this;
		var button3d = self.$el.find(".mutation-3d-vis");

		// initially disable the 3D button
		button3d.attr("disabled", "disabled");

		var formatButton = function(hasData) {
			if (hasData)
			{
				// enable button if there is PDB data
				button3d.removeAttr("disabled");
			}
			else
			{
				var gene = self.model.geneSymbol;
				var content = "No structure data for " + gene;

				// set tooltip options
				var qtipOpts = {content: {text: content},
					hide: {fixed: true, delay: 100, event: 'mouseout'},
					show: {event: 'mouseover'},
					style: {classes: 'qtip-light qtip-rounded qtip-shadow cc-ui-tooltip'},
					position: {my:'bottom center', at:'top center', viewport: $(window)}};

				// disabled buttons do not trigger mouse events,
				// so add tooltip to the wrapper div instead
				self.$el.qtip(qtipOpts);
			}
		};

		var pdbProxy = self.model.pdbProxy;
		var uniprotId = self.model.uniprotId;

		pdbProxy.hasPdbData(uniprotId, formatButton);
	},
	/**
	 * Adds a callback function for the 3D visualizer init button.
	 *
	 * @param callback      function to be invoked on click
	 */
	addInitCallback: function(callback) {
		var self = this;
		var button3d = self.$el.find(".mutation-3d-vis");

		// add listener to 3D init button
		button3d.click(callback);
	},
	/**
	 * Resets the 3D view to its initial state.
	 */
	resetView: function()
	{
		var self = this;
		var button3d = self.$el.find(".mutation-3d-vis");

		// TODO this might not be safe, since we are relying on the callback function

		// just simulate click function on the 3d button to reset the view
		button3d.click();
	},
	isVisible: function()
	{
		var self = this;
		return self.$el.is(":visible");
	}
});
