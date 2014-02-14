/**
 * Controller class for the Mutation Details view.
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsController = function(
	mutationDetailsView, mutationProxy, sampleArray, diagramOpts, tableOpts, mut3dVis)
{
	var _pdbProxy = null;
	var _geneTabView = {};

	// a single 3D view instance shared by all MainMutationView instances
	var _mut3dVisView = null;

	function init()
	{
		// init pdb proxy
		if (mutationProxy.hasData())
		{
			_pdbProxy = new PdbDataProxy(mutationProxy.getMutationUtil());
		}

		// add listeners to the custom event dispatcher of the view
		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.GENE_TAB_SELECTED,
			geneTabSelectHandler);

		mutationDetailsView.dispatcher.on(
			MutationDetailsEvents.GENE_TABS_CREATED,
			geneTabCreateHandler);
	}

	function geneTabSelectHandler(gene)
	{
		if (_geneTabView[gene] == null)
		{
			initView(gene, sampleArray, diagramOpts, tableOpts);
		}
	}

	function geneTabCreateHandler()
	{
		// init 3D view if the visualizer is available

		var container3d = mutationDetailsView.$el.find("#mutation_3d_container");

		if (mut3dVis)
		{
			var mutation3dVisView = new Mutation3dVisView(
				{el: container3d,
					mut3dVis: mut3dVis,
					pdbProxy: _pdbProxy,
					mutationProxy: mutationProxy});

			mutation3dVisView.render();

			// update reference to the 3d vis view
			_mut3dVisView = mutation3dVisView;
		}
		// if no visualizer, hide the 3D vis container
		else
		{
			$(container3d).hide();
		}

		// init the view for the first gene only

		var genes = mutationProxy.getGeneList();

		initView(genes[0], sampleArray, diagramOpts, tableOpts);
	}

	/**
	 * Initializes mutation view for the given gene and cases.
	 *
	 * @param gene          hugo gene symbol
     * @param cases         array of case ids (samples)
     * @param diagramOpts   [optional] mutation diagram options
     * @param tableOpts     [optional] mutation table options
	 */
	function initView(gene, cases, diagramOpts, tableOpts)
	{
		var mutationDiagram = null;
		var mainMutationView = null;
		var mutationData = null;
		var mutationUtil = mutationProxy.getMutationUtil();

		// callback function to init view after retrieving
		// sequence information.
		var init = function(sequenceData)
		{
			// TODO sequenceData may be null for unknown genes...
			// get the first sequence from the response
			var sequence = sequenceData[0];

            var summary = "";

            if(cases.length > 0) {
                // calculate somatic & germline mutation rates
                var mutationCount = mutationUtil.countMutations(gene, cases);
                // generate summary string for the calculated mutation count values
                summary = mutationUtil.generateSummary(mutationCount);
            }

			// prepare data for mutation view
			var model = {geneSymbol: gene,
				mutationSummary: summary,
				uniprotId: sequence.metadata.identifier};

			// init the main view
			var mainView = new MainMutationView({
				el: "#mutation_details_" + gene,
				model: model});

			mainView.render();

			// update the references after rendering the view
			mainMutationView = mainView;
			_geneTabView[gene].mainMutationView = mainView;

			// TODO everything beyond this point is the part of the main mutation view,
			// main mutation view (or its controller) should handle these initializations


			// draw mutation diagram
			var diagram = drawMutationDiagram(
					gene, mutationData, sequence, diagramOpts);

			var view3d = null;

			// check if diagram is initialized successfully.
			if (diagram)
			{
				// init diagram toolbar
				mainView.initToolbar(diagram, gene);

				// init the 3d view
				if (_mut3dVisView)
				{
					view3d = new Mutation3dView({
						el: "#mutation_3d_" + gene,
						model: {uniprotId: sequence.metadata.identifier,
							geneSymbol: gene,
							pdbProxy: _pdbProxy},
						mut3dVisView: _mut3dVisView,
						diagram: diagram});

					view3d.render();

					// also reset (init) the 3D view if the 3D panel is already active
					if (_mut3dVisView.isVisible())
					{
						view3d.resetView();
					}
				}
			}
			else
			{
				console.log("Error initializing mutation diagram: %s", gene);
			}

			// init mutation table view

			var mutationTableView = new MutationDetailsTableView(
					{el: "#mutation_table_" + gene,
					model: {geneSymbol: gene,
						mutations: mutationData,
						tableOpts: tableOpts}});

			mutationTableView.render();

			// update diagram reference after rendering the table
			mutationDiagram = diagram;

			// TODO init controllers in their corresponding view classes' init() method instead?

			// init controllers
			new MainMutationController(mainMutationView, mutationDiagram);
			new MutationDetailsTableController(mutationTableView, mutationDiagram);
			new Mutation3dController(mutationDetailsView, _mut3dVisView, view3d, mutationDiagram, gene);
			new MutationDiagramController(mutationDiagram, mutationTableView.tableUtil, mutationUtil);
		};

		// get mutation data for the current gene
		mutationProxy.getMutationData(gene, function(data) {
			// init reference mapping
			_geneTabView[gene] = {};

			// update mutation data reference
			mutationData = data;

			// display a message if there is no mutation data available for this gene
			if (mutationData == null ||
			    mutationData.length == 0)
			{
				mutationDetailsView.$el.find("#mutation_details_" + gene).html(
					_.template($("#default_gene_mutation_details_info_template").html(), {}));
			}
			// get the sequence data for the current gene & init view
			else
			{
				$.getJSON("getPfamSequence.json", {geneSymbol: gene}, init);
			}
		});
	}

	// TODO move this method into MainMutationController...
	/**
	 * Initializes the mutation diagram view.
	 *
	 * @param gene          hugo gene symbol
	 * @param mutationData  mutation data (array of JSON objects)
	 * @param sequenceData  sequence data (as a JSON object)
	 * @param options       [optional] diagram options
	 */
	function drawMutationDiagram(gene, mutationData, sequenceData, options)
	{
		// use defaults if no options provided
		if (!options)
		{
			options = {};
		}

		// do not draw the diagram if there is a critical error with
		// the sequence data
		if (sequenceData["length"] == "" ||
		    parseInt(sequenceData["length"]) <= 0)
		{
			// return null to indicate an error
			return null;
		}

		// overwrite container in any case (for consistency with the default view)
		options.el = "#mutation_diagram_" + gene.toUpperCase();

		// create a backbone collection for the given data
		var mutationColl = new MutationCollection(mutationData);

		var mutationDiagram = new MutationDiagram(gene, options, mutationColl);
		mutationDiagram.initDiagram(sequenceData);

		return mutationDiagram;
	}

	init();
};
