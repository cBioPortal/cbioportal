/**
 * Tooltip view for the mutation diagram's lollipop circles.
 *
 * options: {el: [target container],
 *           model: {count: [number of mutations],
 *                   label: [info for that location]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var LollipopTipView = Backbone.View.extend({
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

    showStats: false,
    setShowStats: function(showStats) {
        this.showStats = showStats;
    },
    getShowStats: function(showStats) {
        return this.showStats;
    },

    compileTemplate: function()
	{
        var thatModel = this.model;
        var mutationStr = thatModel.count > 1 ? "mutations" : "mutation";

		// pass variables in using Underscore.js template
		var variables = {count: thatModel.count,
			mutationStr: mutationStr,
			label: thatModel.label
        };

		// compile the template using underscore
        var compiledEl = $(_.template( $("#mutation_details_lollipop_tip_template").html(), variables));

        var statsEl = compiledEl.find(".lollipop-stats");
        if(this.showStats)
        {
            (new LollipopTipStatsView({ el: statsEl, model: thatModel.stats })).render();
            statsEl.find("table").dataTable({
                "sDom": 't',
                "bJQueryUI": true,
                "bDestroy": true,
                "aaSorting": [[ 1, "desc" ]],
                "aoColumns": [
                    { "bSortable": false },
                    { "bSortable": false }
                ]
            });
        } else {
            statsEl.hide();
        }

        return compiledEl.html();
	}
});
