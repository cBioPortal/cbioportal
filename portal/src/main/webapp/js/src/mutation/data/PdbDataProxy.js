/**
 * This class is designed to retrieve PDB data on demand.
 *
 * @param mutationUtil  an instance of MutationDetailsUtil class
 */
var PdbDataProxy = function(mutationUtil)
{
	// TODO get servlet name as a param?
	var _servletName = "get3dPdb.json";

	var _util = mutationUtil;

	// cache for PDB data:
	// map of <uniprot id, PdbCollection> pairs
	var _pdbDataCache = {};

	/**
	 * Retrieves the position map for the given gene and chain.
	 * Invokes the given callback function after retrieving the data.
	 *
	 * @param gene          hugo gene symbol
	 * @param chain         a PdbChainModel instance
	 * @param callbackFn    function to be invoked after data retrieval
	 */
	function getPositionMap(gene, chain, callbackFn)
	{
		// collection of alignments (PdbAlignmentCollection)
		var alignments = chain.alignments;

		// TODO use a proper cache instead of checking/reflecting a chain attribute?
		// do not retrieve data if it is already there
		if (chain.positionMap != undefined)
		{
			callbackFn(chain.positionMap);
			return;
		}

		// get protein positions for current mutations
		var positions = _util.getProteinPositions(gene);

		// populate position data array
		var positionData = [];

		// TODO remove duplicates from position data
		_.each(positions, function(ele, i) {
			if (ele.start > -1)
			{
				positionData.push(ele.start);
			}

			if (ele.end > ele.start)
			{
				positionData.push(ele.end);
			}
		});

		// populate alignment data array
		var alignmentData = [];

		alignments.each(function(ele, i) {
			alignmentData.push(ele.alignmentId);
		});

		var processData = function(data) {
			var positionMap = {};
			var mutations = _util.getMutationGeneMap()[gene];

			if (data.positionMap != null)
			{
				// re-map mutation ids with positions by using the raw position map
				for(var i=0; i < mutations.length; i++)
				{
					var start = data.positionMap[mutations[i].proteinPosStart];
					var end = data.positionMap[mutations[i].proteinPosEnd];

					if (start != undefined &&
					    end != undefined)
					{
						positionMap[mutations[i].mutationId] =
							{start: start, end: end};
					}
				}
			}

			// call the callback function with the updated position map
			callbackFn(positionMap);
		};

		// get pdb data for the current mutations
		$.getJSON(_servletName,
		          {positions: positionData.join(" "),
			          alignments: alignmentData.join(" ")},
		          processData);
	}

	/**
	 * Retrieves the PDB data for the provided uniprot id. Passes
	 * the retrieved data as a parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callbackFn    callback function to be invoked
	 */
	function getPdbData(uniprotId, callbackFn)
	{
		// retrieve data from the server if not cached
		if (_pdbDataCache[uniprotId] == undefined)
		{
			// process & cache the raw data
			var processData = function(data) {
				var pdbColl = PdbDataUtil.processPdbData(data);
				_pdbDataCache[uniprotId] = pdbColl;

				// forward the processed data to the provided callback function
				callbackFn(pdbColl);
			};

			// retrieve data from the servlet
			$.getJSON(_servletName,
					{uniprotId: uniprotId},
					processData);
		}
		else
		{
			// data is already cached, just forward it
			callbackFn(_pdbDataCache[uniprotId]);
		}
	}

	return {
		getPdbData: getPdbData,
		getPositionMap: getPositionMap
	};
};
