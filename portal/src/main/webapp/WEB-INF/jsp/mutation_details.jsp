<div class='section' id='mutation_details'>
	<img src='images/ajax-loader.gif'/>
</div>

<style type="text/css" title="currentStyle">
	@import "css/data_table_jui.css";
	@import "css/data_table_ColVis.css";
	@import "css/mutation_details.css";
</style>

<script type="text/javascript">

// TODO 3d Visualizer should be initialized before document get ready
// ...due to incompatible Jmol initialization behavior
var _mut3dVis = null;
//_mut3dVis = new Mutation3dVis("default3dView", {});
//_mut3dVis.init();

// Set up Mutation View
$(document).ready(function(){
	var sampleArray = PortalGlobals.getCases().trim().split(/\s+/);

	var servletParams = {geneticProfiles: geneticProfiles,
		caseList: PortalGlobals.getCases()};

	var servletName = "getMutationData.json";

	// TODO make proxy instance global (it should be shared with all other tabs)
	// init mutation data proxy with the data servlet config
	var proxy = new MutationDataProxy(PortalGlobals.getGeneListString());
	proxy.initWithoutData(servletName, servletParams);

	// init default mutation details view

	var model = {mutationProxy: proxy,
		sampleArray: sampleArray};

	var defaultView = null;

	var initDefaultView = function() {
		defaultView = new MutationDetailsView(
				{el: "#mutation_details", model: model, mut3dVis: _mut3dVis});

		defaultView.render();
	};

	// init view without a delay if the mutation details tab is already visible
	if ($("#mutation_details").is(":visible"))
	{
		initDefaultView();
	}

	// add a click listener for the "mutations" tab
	$("#tabs").bind("tabsactivate", function(event, ui){
		// init when clicked on the mutations tab, and init only once
		if (ui.newTab.text().trim().toLowerCase() == "mutations")
		{
			// init only if it is not initialized yet
			if (defaultView == null)
			{
				initDefaultView();
			}
			// if already init, then refresh genes tab
			// (a fix for ui.tabs.plugin resize problem)
			else
			{
				defaultView.refreshGenesTab();
			}
		}
	});
});

</script>