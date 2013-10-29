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
		// first create as an object (map),
		// then convert to an array to avoid duplicate positions
		var positionObj = {};

		// only add positions which fall between chain start & end positions
		_.each(positions, function(ele, i) {
			if (ele.start > -1 &&
			    ele.start >= chain.mergedAlignment.uniprotFrom &&
			    ele.start <= chain.mergedAlignment.uniprotTo)
			{
				positionObj[ele.start] = ele.start;
			}

			if (ele.end > ele.start &&
			    ele.end >= chain.mergedAlignment.uniprotFrom &&
			    ele.end <= chain.mergedAlignment.uniprotTo)
			{
				positionObj[ele.end] = ele.end;
			}
		});

		// convert object to array
		var positionData = [];

		for (var key in positionObj)
		{
			positionData.push(positionObj[key]);
		}

		// populate alignment data array
		var alignmentData = [];

		alignments.each(function(ele, i) {
			alignmentData.push(ele.alignmentId);
		});

		// callback function for the AJAX call
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

					// if no start and end position found for this mutation,
					// then it means this mutation position is not in this chain
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

		// check if there are positions to map
		if (positionData.length > 0)
		{
			// get pdb data for the current mutations
			$.getJSON(_servletName,
		          {positions: positionData.join(" "),
			          alignments: alignmentData.join(" ")},
		          processData);
		}
		// no position data: no need to query the server
		else
		{
			// just forward to callback with empty data
			callbackFn({});
		}
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
