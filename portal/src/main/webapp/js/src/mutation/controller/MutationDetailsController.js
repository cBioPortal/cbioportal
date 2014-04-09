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
		if (mut3dVis &&
		    mutationProxy.hasData())
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

		var container3d = mutationDetailsView.$el.find(".mutation-3d-container");

		if (mut3dVis)
		{
			// TODO remove mutationProxy?
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
		// callback function to init view after retrieving
		// sequence information.
		var init = function(sequenceData, mutationData, pdbRowData)
		{
			// process data to add 3D match information
			mutationData = processMutationData(mutationData,
			                                   mutationProxy.getMutationUtil(),
			                                   pdbRowData);

			// TODO a new util for each instance instead?
//			var mutationUtil = new MutationDetailsUtil(
//				new MutationCollection(mutationData));
			var mutationUtil = mutationProxy.getMutationUtil();

			// prepare data for mutation view
			var model = {geneSymbol: gene,
				mutationData: mutationData,
				mutationProxy: mutationProxy, // TODO pass mutationUtil instead?
				pdbProxy: _pdbProxy,
				sequence: sequenceData,
				sampleArray: cases,
				diagramOpts: diagramOpts,
				tableOpts: tableOpts};

			// init the main view
			var mainView = new MainMutationView({
				el: "#mutation_details_" + cbio.util.safeProperty(gene),
				model: model});

			mainView.render();

			// update the reference after rendering the view
			_geneTabView[gene].mainMutationView = mainView;

			// TODO this can be implemented in a better way in the MainMutationView class
			var components = mainView.initComponents(_mut3dVisView);


			// TODO init controllers in their corresponding view classes' init() method instead?

			// init controllers
			new MainMutationController(mainView, components.diagram);
			new MutationDetailsTableController(
				components.tableView, components.diagram, mutationDetailsView);

			if (mut3dVis &&
			    _mut3dVisView)
			{
				new Mutation3dController(mutationDetailsView, mainView,
					_mut3dVisView, components.view3d, mut3dVis,
					_pdbProxy, mutationUtil,
					components.diagram, components.tableView, gene);
			}

			new MutationDiagramController(
				components.diagram, components.tableView.tableUtil, mutationUtil, components.tableView);
		};

		// get mutation data for the current gene
		mutationProxy.getMutationData(gene, function(data) {
			// init reference mapping
			_geneTabView[gene] = {};

			// display a message if there is no mutation data available for this gene
			if (data == null || data.length == 0)
			{
				mutationDetailsView.$el.find(
					"#mutation_details_" + cbio.util.safeProperty(gene)).html(
						_.template($("#default_mutation_details_gene_info_template").html(), {}));
			}
			// get the sequence data for the current gene & init view
			else
			{
				// get the most frequent uniprot accession string (excluding "NA")
				var uniprotInfo = mutationProxy.getMutationUtil().dataFieldCount(
					gene, "uniprotAcc", ["NA"]);

				var uniprotAcc = null;
				var servletParams = {geneSymbol: gene};

				if (uniprotInfo.length > 0)
				{
					uniprotAcc = uniprotInfo[0].uniprotAcc;
				}

				if (uniprotAcc)
				{
					servletParams = {uniprotAcc: uniprotAcc};
				}

				$.getJSON("getPfamSequence.json", servletParams, function(sequenceData) {
					// TODO sequenceData may be null for unknown genes...
					// get the first sequence from the response
					var sequence = sequenceData[0];

					if (_pdbProxy)
					{
						var uniprotId = sequence.metadata.identifier;
						_pdbProxy.getPdbRowData(uniprotId, function(pdbRowData) {
							init(sequence, data, pdbRowData);
						});
					}
					else
					{
						init(sequence, data);
					}

				});
			}
		});
	}

	/**
	 * Processes mutation data to add additional information.
	 *
	 * @param mutationData  raw mutation data array
	 * @param mutationUtil  mutation util
	 * @param pdbRowData    pdb row data for the corresponding uniprot id
	 * @return {Array}      mutation data array with additional attrs
	 */
	function processMutationData(mutationData, mutationUtil, pdbRowData)
	{
		if (!pdbRowData)
		{
			return mutationData;
		}

		var map = mutationUtil.getMutationIdMap();

		_.each(mutationData, function(mutation, idx) {
			// use model instance, since raw mutation data won't work with mutationToPdb
			var mutationModel = map[mutation.mutationId];
			// find the matching pdb
			var match = PdbDataUtil.mutationToPdb(mutationModel, pdbRowData);
			// update the raw mutation object
			mutation.pdbMatch = match;
			// also update the corresponding MutationModel within the util
			mutationModel.pdbMatch = match;
		});

		return mutationData;
	}

	init();
};
