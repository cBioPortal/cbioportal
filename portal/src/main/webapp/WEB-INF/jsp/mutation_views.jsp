<script type="text/template" id="mutation_view_template">
	<h4>{{geneSymbol}}: {{mutationSummary}}</h4>
	<div id='uniprot_link_{{geneSymbol}}' class='diagram_uniprot_link'>
		<a href='http://www.uniprot.org/uniprot/{{uniprotId}}' target='_blank'>{{uniprotId}}</a>
	</div>
	<div id='mutation_diagram_{{geneSymbol}}' class='mutation-diagram-container'></div>
	<div id='mutation_table_{{geneSymbol}}'>
		<img src='images/ajax-loader.gif'/>
	</div>
</script>

<script type="text/javascript">
	// TODO this is duplicate! it is better to put this into a global js
	// This is for the moustache-like templates
	// prevents collisions with JSP tags
	_.templateSettings = {
		interpolate : /\{\{(.+?)\}\}/g
	};

	var MainMutationView = Backbone.View.extend({
		render: function() {
			// pass variables in using Underscore.js template
			var variables = { geneSymbol: this.model.geneSymbol,
				mutationSummary: this.model.mutationSummary,
				uniprotId: this.model.uniprotId};

			// compile the template using underscore
			var template = _.template( $("#mutation_view_template").html(), variables);

			// load the compiled HTML into the Backbone "el"
			this.$el.html(template);
		}
	});

	//TODO views & templates for mutation table
</script>