/**
* This view will add new columns to the mutation stats table
* model: { cancerType: "", count: 0 }
*/
var LollipopTipStatsView = Backbone.View.extend({
    template: _.template($("#mutation_details_lollipop_tip_stats_template").html()),
    render: function()
    {
        var template = this.template;
        var thatEl = this.$el.find("table tbody");
        _.each(this.model, function(statItem) {
            thatEl.append(template(statItem));
        });
        return this;
    }
});
