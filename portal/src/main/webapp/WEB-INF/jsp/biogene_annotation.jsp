<script type="text/javascript" src="js/underscore-min.js"></script>

<script type="text/template" id="biogene_template">
	<div class='node-details-info'>
		<table>
			<tr class='biogene-symbol'><td><b>Gene Symbol:</b></td><td>{{geneSymbol}}</td></tr>
			<tr class='biogene-description'><td><b>Description:</b></td><td>{{geneDescription}}</td></tr>
			<tr class='biogene-aliases'><td><b>Aliases:</b></td><td>{{geneAliases}}</td></tr>
			<tr class='biogene-designations'><td><b>Designations:</b></td><td>{{geneDesignations}}</td></tr>
			<tr class='biogene-chromosome'><td><b>Chromosome:</b></td><td>{{geneChromosome}}</td></tr>
			<tr class='biogene-location'><td><b>Location:</b></td><td>{{geneLocation}}</td></tr>
			<tr class='biogene-mim'><td><b>MIM:</b></td><td>{{geneMIM}}</td></tr>
			<tr class='biogene-id'><td><b>Gene ID:</b></td><td>{{geneId}}</td></tr>
		</table>
	</div>
	<div class='node-details-summary'>
		<span class='title'>
			<label>Gene Summary:</label>
		</span>
		<p class='regular'>{{geneSummary}}</p>
	</div>
</script>

<script type="text/javascript">

	// This is for the moustache-like templates
	// prevents collisions with JSP tags
	_.templateSettings = {
		interpolate : /\{\{(.+?)\}\}/g
	};

	var BioGeneView = Backbone.View.extend({
		initialize: function(options){
			this.render(options);
		},
		render: function(options){
			// pass variables in using Underscore.js template
			var variables = { geneSymbol: options.data.geneSymbol,
				geneDescription: options.data.geneDescription,
				geneAliases: options.data.geneAliases,
				geneDesignations: options.data.geneDesignations,
				geneChromosome: options.data.geneChromosome,
				geneLocation: options.data.geneLocation,
				geneMIM: options.data.geneMIM,
				geneId: options.data.geneId,
				geneSummary: options.data.geneSummary};

			// compile the template using underscore
			var template = _.template( $("#biogene_template").html(), variables );

			// load the compiled HTML into the Backbone "el"
			this.$el.html( template );

			// hide rows with undefined data

			if (options.data.geneSymbol == undefined)
				$(options.el + " .biogene-symbol").hide();

            if (options.data.geneDescription == undefined)
	            $(options.el + " .biogene-description").hide();

            if (options.data.geneAliases == undefined)
	            $(options.el + " .biogene-aliases").hide();

            if (options.data.geneDesignations == undefined)
	            $(options.el + " .biogene-designations").hide();

            if (options.data.geneChromosome == undefined)
	            $(options.el + " .biogene-chromosome").hide();

            if (options.data.geneLocation == undefined)
	            $(options.el + " .biogene-location").hide();

            if (options.data.geneMIM == undefined)
	            $(options.el + " .biogene-mim").hide();

            if (options.data.geneId == undefined)
	            $(options.el + " .biogene-id").hide();

			if (options.data.geneSummary == undefined)
				$(options.el + " .node-details-summary").hide();
		}
	});
</script>