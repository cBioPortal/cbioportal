/**
 * Mutation Model.
 *
 * Current model is sufficient to visualize both the table and the diagram.
 * Later we may add more data if necessary.
 */
var MutationModel = Backbone.Model.extend({
	initialize: function(attributes) {
		this.mutationId = attributes.mutationId;
        this.mutationSid = attributes.mutationSid;
		this.geneticProfileId = attributes.geneticProfileId;
		this.mutationEventId = attributes.mutationEventId;
		this.caseId = attributes.caseId;
		this.geneSymbol = attributes.geneSymbol;
		this.linkToPatientView = attributes.linkToPatientView;
        this.cancerType = attributes.cancerType;
        this.cancerStudy = attributes.cancerStudy;
        this.cancerStudyShort = attributes.cancerStudyShort;
        this.cancerStudyLink = attributes.cancerStudyLink;
		this.proteinChange = attributes.proteinChange;
		this.mutationType = attributes.mutationType;
		this.cosmic = attributes.cosmic;
		this.cosmicCount = this.calcCosmicCount(attributes.cosmic);
		this.functionalImpactScore = attributes.functionalImpactScore;
		this.fisValue = attributes.fisValue;
		this.msaLink = attributes.msaLink;
		this.xVarLink = attributes.xVarLink;
		this.pdbLink = attributes.pdbLink;
		this.igvLink = attributes.igvLink;
		this.mutationStatus = attributes.mutationStatus;
		this.validationStatus = attributes.validationStatus;
		this.sequencingCenter = attributes.sequencingCenter;
		this.ncbiBuildNo = attributes.ncbiBuildNo;
		this.chr = attributes.chr;
		this.startPos = attributes.startPos;
		this.endPos = attributes.endPos;
		this.referenceAllele = attributes.referenceAllele;
		this.variantAllele = attributes.variantAllele;
		this.tumorFreq = attributes.tumorFreq;
		this.normalFreq = attributes.normalFreq;
		this.tumorRefCount = attributes.tumorRefCount;
		this.tumorAltCount = attributes.tumorAltCount;
		this.normalRefCount = attributes.normalRefCount;
		this.normalAltCount = attributes.normalAltCount;
		this.canonicalTranscript = attributes.canonicalTranscript;
		this.refseqMrnaId = attributes.refseqMrnaId;
		this.codonChange = attributes.codonChange;
		this.uniprotId = attributes.uniprotId;
		this.proteinPosStart = attributes.proteinPosStart;
		this.proteinPosEnd = attributes.proteinPosEnd;
		this.mutationCount = attributes.mutationCount;
		this.specialGeneData = attributes.specialGeneData;
		this.keyword = attributes.keyword;
	},
	url: function() {
		// TODO implement this to get the data from a web service
		var urlStr = "webservice.do?cmd=...";
	},
	calcCosmicCount: function(cosmic)
	{
		var cosmicCount = 0;

		if (cosmic)
		{
			cosmic.forEach(function(c) {
				cosmicCount += c[2];
			});
		}

		return cosmicCount;
	}
});

/**
 * Pileup Model.
 *
 * This model is designed to represent multiple mutations at the same
 * position. This is intended to be used for mutation diagram.
 */
var Pileup = Backbone.Model.extend({
	initialize: function(attributes) {
		this.mutations = attributes.mutations; // array of mutations at this data point
		this.count = attributes.count; // number of mutations at this data point
		this.location = attributes.location; // the location of the mutations
		this.label = attributes.label; // text label for this data point
        this.stats = attributes.stats;
	}
});

/**
 * PDB data model.
 *
 * Contains PDB id and a chain list (where each element in the list has
 * a chain id and a mapping for pdb positions to uniprot positions).
 */
var PdbModel = Backbone.Model.extend({
	initialize: function(attributes) {
		// pdb id (e.g: 1d5r)
		this.pdbId = attributes.pdbId;
		// collection of PdbChainModel instances
		this.chains = new PdbChainCollection(attributes.chains);
	}
});

/**
 * Collection of pdb data (PdbModel instances).
 */
var PdbCollection = Backbone.Collection.extend({
	model: PdbModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	}
});

var PdbChainModel = Backbone.Model.extend({
	initialize: function(attributes) {
		// chain id (A, B, C, X, etc.)
		this.chainId = attributes.chainId;
		//  map of (uniprot position, pdb position) pairs
		this.positionMap = attributes.positionMap;
		// array of start position and end position pairs
		this.segments = attributes.segments;
	}
});

/**
 * Collection of pdb data (PdbModel instances).
 */
var PdbChainCollection = Backbone.Collection.extend({
	model: PdbChainModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	}
});

/**
 * Collection of mutations (MutationModel instances).
 */
var MutationCollection = Backbone.Collection.extend({
	model: MutationModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	},
	parse: function(response) {
		// TODO parse response (returned from web service)
		// this.attributes = function() { return response.attributes; };   // save the attributes
		// return response.data;    // but the data is what is to be model-ed
	},
	url: function() {
		// TODO implement this to get the data from a web service
		var urlStr = "webservice.do?cmd=...";
	}
});

/**
 * Utility class for processing collection of mutations.
 *
 * @param mutations     [optional] a MutationCollection instance
 * @constructor
 */
var MutationDetailsUtil = function(mutations)
{
	var GERMLINE = "germline"; // germline mutation constant

	// init class variables
	var _mutationGeneMap = {};
	var _mutationCaseMap = {};
	var _mutationIdMap = {};
	var _mutations = [];

	this.getMutationGeneMap = function()
	{
		return _mutationGeneMap;
	};

	this.getMutationCaseMap = function()
	{
		return _mutationCaseMap;
	};

	this.getMutationIdMap = function()
	{
		return _mutationIdMap;
	};

	this.getMutations = function()
	{
		return _mutations;
	}

	/**
	 * Updates existing maps and collections by processing the given mutations.
	 * This method appends given mutations to the existing ones, it does not
	 * reset previous mutations.
	 *
	 * @param mutations a MutationCollection instance (list of mutations)
	 */
	this.processMutationData = function(mutations)
	{
		// update collections, arrays, maps, etc.
		_mutationGeneMap = this._updateGeneMap(mutations);
		_mutationCaseMap = this._updateCaseMap(mutations);
		_mutationIdMap = this._updateIdMap(mutations);
		_mutations = _mutations.concat(mutations);
	};

	/**
	 * Retrieves protein positions corresponding to the mutations
	 * for the given gene symbol.
	 *
	 * @param gene      hugo gene symbol
	 * @return {Array}  array of protein positions
	 */
	this.getProteinPositions = function(gene)
	{
		var mutations = _mutationGeneMap[gene];

		var positions = [];

		for(var i=0; i < mutations.length; i++)
		{
			var position = {id: mutations[i].id,
				start: mutations[i].proteinPosStart,
				end: mutations[i].proteinPosEnd};

			positions.push(position);
		}

		return positions;
	};

	/**
	 * Processes the pdb data (received from the server) to map positions
	 * to mutation ids.
	 *
	 * @param gene  hugo gene symbol
	 * @param data  pdb data with a position map
	 * @return {PdbCollection}   PdbModel instances representing the processed data
	 */
	this.processPdbData = function(gene, data)
	{
		var mutations = _mutationGeneMap[gene];
		var pdbModel = null;
		var pdbList = [];

		_.each(data, function(pdb, idx) {
			_.each(pdb.chains, function(ele, idx) {
				var positionMap = {};

				if (ele.positionMap != null)
				{
					// re-map mutation ids with positions by using the raw position map
					for(var i=0; i < mutations.length; i++)
					{
						positionMap[mutations[i].mutationId] = {
							start: ele.positionMap[mutations[i].proteinPosStart],
							end: ele.positionMap[mutations[i].proteinPosEnd]};
					}
				}

				// update position map
				ele.positionMap = positionMap;
			});

			pdbModel = new PdbModel(pdb);
			pdbList.push(pdbModel);
		});

		// return new pdb model
		return new PdbCollection(pdbList);
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <geneSymbol, mutation array> pairs.
	 *
	 * @param mutations collection of mutations
	 * @return {object} map of mutations (keyed on gene symbol)
	 * @private
	 */
	this._updateGeneMap = function(mutations)
	{
		var mutationMap = _mutationGeneMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			var gene = mutations.at(i).geneSymbol.toUpperCase();

			if (mutationMap[gene] == undefined)
			{
				mutationMap[gene] = [];
			}

			mutationMap[gene].push(mutations.at(i));
		}

		return mutationMap;
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <case id, mutation array> pairs.
	 *
	 * @param mutations collection of mutations
	 * @return {object} map of mutations (keyed on case id)
	 * @private
	 */
	this._updateCaseMap = function(mutations)
	{
		var mutationMap = _mutationCaseMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			var caseId = mutations.at(i).caseId.toLowerCase();

			if (mutationMap[caseId] == undefined)
			{
				mutationMap[caseId] = [];
			}

			mutationMap[caseId].push(mutations.at(i));
		}

		return mutationMap;
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <mutation id, mutation> pairs.
	 *
	 * @param mutations collection of mutations
	 * @return {object} map of mutations (keyed on mutation id)
	 * @private
	 */
	this._updateIdMap = function(mutations)
	{
		var mutationMap = _mutationIdMap;

		// process raw data to group mutations by genes
		for (var i=0; i < mutations.length; i++)
		{
			var mutationId = mutations.at(i).mutationId;
			mutationMap[mutationId] = mutations.at(i);
		}

		return mutationMap;
	};

	/**
	 * Generates a single line summary with mutation rate.
	 *
	 * @param mutationCount mutation count values as an object
	 *                      {numCases, numMutated, numSomatic, numGermline}
	 * @return {string}     single line summary string
	 */
	this.generateSummary = function(mutationCount)
	{
		var summary = "[";
		var rate;

		if (mutationCount.numGermline > 0)
		{
			rate = (mutationCount.numGermline / mutationCount.numCases) * 100;
			summary += "Germline Mutation Rate: " + rate.toFixed(1) + "%, ";
		}

		rate = (mutationCount.numSomatic / mutationCount.numCases) * 100;
		summary += "Somatic Mutation Rate: " + rate.toFixed(1) + "%]";

		return summary;
	};

	/**
	 * Counts the number of total cases, number of mutated cases, number of cases
	 * with somatic mutation, and number of cases with germline mutation.
	 *
	 * Returns an object with these values.
	 *
	 * @param gene  hugo gene symbol
	 * @param cases array of cases (strings)
	 * @return {{numCases: number,
	 *          numMutated: number,
	 *          numSomatic: number,
	 *          numGermline: number}}
	 */
	this.countMutations = function(gene, cases)
	{
		var numCases = cases.length;
		var numMutated = 0;
		var numSomatic = 0;
		var numGermline = 0;

		// count mutated cases (also count somatic and germline mutations)
		for (var i=0; i < cases.length; i++)
		{
			// get the mutations for the current case
			var mutations = _mutationCaseMap[cases[i].toLowerCase()];

			// check if case has a mutation
			if (mutations != null)
			{
				var somatic = 0;
				var germline = 0;

				for (var j=0; j < mutations.length; j++)
				{
					// skip mutations with different genes
					if (mutations[j].geneSymbol.toLowerCase() != gene.toLowerCase())
					{
						continue;
					}

					if (mutations[j].mutationStatus.toLowerCase() === this.GERMLINE)
					{
						// case has at least one germline mutation
						germline = 1;
					}
					else
					{
						// case has at least one somatic mutation
						somatic = 1;
					}
				}

				// update counts
				numSomatic += somatic;
				numGermline += germline;
				numMutated++;
			}
		}

		// return an array of calculated values
		return {numCases: numCases,
			numMutated: numMutated,
			numSomatic: numSomatic,
			numGermline: numGermline};
	};

    /**
     * Checks if there all mutations come from a single cancer study
     *
     * @param gene  hugo gene symbol
     */
    this.cancerStudyAllTheSame = function(gene)
    {
        var self = this;
        gene = gene.toUpperCase();
        if (_mutationGeneMap[gene] != undefined)
        {
            var mutations = _mutationGeneMap[gene];
            var prevStudy = null;

            for (var i=0; i < mutations.length; i++)
            {
                var cancerStudy = mutations[i].cancerStudy;
                if(prevStudy == null) {
                    prevStudy = cancerStudy;
                } else if(prevStudy != cancerStudy) {
                    return false;
                }
            }
        }

        return true;
    };


    /**
	 * Checks if there is a germline mutation for the given gene.
	 *
	 * @param gene  hugo gene symbol
	 */
	this.containsGermline = function(gene)
	{
		var self = this;
		var contains = false;

		gene = gene.toUpperCase();

		if (_mutationGeneMap[gene] != undefined)
		{
			var mutations = _mutationGeneMap[gene];

			for (var i=0; i < mutations.length; i++)
			{
				if (mutations[i].mutationStatus.toLowerCase() == GERMLINE)
				{
					contains = true;
					break;
				}
			}
		}

		return contains;
	};

	/**
	 * Checks if there is a link to IGV BAM file for the given gene.
	 *
	 * @param gene  hugo gene symbol
	 */
	this.containsIgvLink = function(gene)
	{
		var self = this;
		var contains = false;

		gene = gene.toUpperCase();

		if (_mutationGeneMap[gene] != undefined)
		{
			var mutations = _mutationGeneMap[gene];

			for (var i=0; i < mutations.length; i++)
			{
				if (mutations[i].igvLink)
				{
					contains = true;
					break;
				}
			}
		}

		return contains;
	};

	// init maps by processing the initial mutations
	if (mutations != null)
	{
		this.processMutationData(mutations);
	}
};

/**
 * Singleton utility class for pileup related tasks.
 */
var PileupUtil = (function()
{
	/**
	 * Processes a Pileup instance, and creates a map of
	 * <mutation type, mutation array> pairs.
	 *
	 * @param pileup    a pileup instance
	 * @return {object} map of mutations (keyed on mutation type)
	 * @private
	 */
	var generateTypeMap = function(pileup)
	{
		var mutations = pileup.mutations;
		var mutationMap = {};

		// process raw data to group mutations by types
		for (var i=0; i < mutations.length; i++)
		{
			var type = mutations[i].mutationType.toLowerCase();

			if (mutationMap[type] == undefined)
			{
				mutationMap[type] = [];
			}

			mutationMap[type].push(mutations[i]);
		}

		return mutationMap;
	};

	/**
	 * Processes a Pileup instance, and creates an array of
	 * <mutation type, count> pairs. The final array is sorted
	 * by mutation count.
	 *
	 * @param pileup    a pileup instance
	 * @return {Array}  array of mutation type and count pairs
	 */
	var generateTypeArray = function (pileup)
	{
		var map = generateTypeMap(pileup);
		var typeArray = [];

		// convert to array and sort by length (count)
		for (var key in map)
		{
			typeArray.push({type: key, count: map[key].length});
		}

		typeArray.sort(function(a, b) {
			// descending sort
			return b.count - a.count;
		});

		return typeArray;
	};

	/**
	 * Processes a Pileup instance, and creates an array of
	 * <mutation type group, count> pairs. The final array
	 * is sorted by mutation count.
	 *
	 * @param pileup    a pileup instance
	 * @return {Array}  array of mutation type group and count pairs
	 */
	var generateTypeGroupArray = function (pileup)
	{
		// TODO a very similar mapping is also used in the mutation table view
		// ...it might be better to merge these two mappings to avoid duplication
		var typeToGroupMap = {
			missense_mutation: "missense_mutation",
			nonsense_mutation: "trunc_mutation",
			nonstop_mutation: "trunc_mutation",
			frame_shift_del: "trunc_mutation",
			frame_shift_ins: "trunc_mutation",
			in_frame_ins: "inframe_mutation",
			in_frame_del: "inframe_mutation",
			splice_site: "trunc_mutation",
			other: "other_mutation"
		};

		var typeMap = generateTypeMap(pileup);
		var groupArray = [];
		var groupCountMap = {};

		// group mutation types by using the type map
		// and count number of mutations in a group

		for (var type in typeMap)
		{
			var group = typeToGroupMap[type];

			if (group == undefined)
			{
				group = typeToGroupMap.other;
			}

			if (groupCountMap[group] == undefined)
			{
				// init count
				groupCountMap[group] = 0;
			}

			groupCountMap[group]++;
		}

		// convert to array and sort by length (count)

		for (var group in groupCountMap)
		{
			groupArray.push({group: group, count: groupCountMap[group]});
		}

		groupArray.sort(function(a, b) {
			// descending sort
			return b.count - a.count;
		});

		return groupArray;
	};

	return {
		getMutationTypeMap: generateTypeMap,
		getMutationTypeArray: generateTypeArray,
		getMutationTypeGroups: generateTypeGroupArray
	};
})();


/**
 * This class is designed to retrieve mutation data on demand, but it can be also
 * initialized with the full mutation data already retrieved from the server.
 *
 * @param geneList  list of target genes (genes of interest) as a string
 */
var MutationDataProxy = function(geneList)
{
	// MutationDetailsUtil instance
	var _util = new MutationDetailsUtil();
	// list of target genes as an array of strings (in the exact input order)
	var _unsortedGeneList = geneList.trim().split(/\s+/);
	// alphabetically sorted list of target genes as an array of strings
	var _geneList = geneList.trim().split(/\s+/).sort();
	// name of the mutation data servlet
	var _servletName;
	// parameters to be sent to the mutation data servlet
	var _servletParams;
	// flag to indicate if the initialization is full or lazy
	var _fullInit;

	/**
	 * Initializes the proxy without actually grabbing anything from the server.
	 * Provided servlet name and servlet parameters will be used for later invocation
	 * of getMutationData function.
	 *
	 * @param servletName   name of the mutation data servlet (used for AJAX query)
	 * @param servletParams servlet (query) parameters
	 */
	function lazyInit(servletName, servletParams)
	{
		_servletName = servletName;
		_servletParams = servletParams;
		_fullInit = false;
	}

	/**
	 * Initializes with full mutation data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional mutation data.
	 *
	 * @param mutationData  full mutation data
	 */
	function fullInit(mutationData)
	{
		var mutations = new MutationCollection(mutationData);
		_util.processMutationData(mutations);
		_fullInit = true;
	}

	function getGeneList()
	{
		// TODO lazy init: to find out genes with mutation data ONLY,
		// we need to query the server before hand. Otherwise,
		// we cannot remove the genes without data from the list until
		// the corresponding gene tab is clicked.
		return _geneList;
	}

	function getUnsortedGeneList()
	{
		return _unsortedGeneList;
	}

	function getMutationUtil()
	{
		return _util;
	}

	/**
	 * Returns the mutation data for the given gene(s).
	 *
	 * @param geneList  list of hugo gene symbols as a whitespace delimited string
	 * @param callback  callback function to be invoked after retrieval
	 */
	function getMutationData(geneList, callback)
	{
		var genes = geneList.trim().split(/\s+/);
		var genesToQuery = [];

		// get previously grabbed data (if any)
		var mutationData = [];
		var mutationMap = _util.getMutationGeneMap();

		_.each(genes, function(gene, idx) {
			var data = mutationMap[gene];

			if (data == undefined ||
			    data.length == 0)
			{
				// mutation data does not exist for this gene, add it to the list
				genesToQuery.push(gene);
			}
			else
			{
				// update the data
				mutationData = mutationData.concat(data);
			}
		});

		// all data is already retrieved (full init)
		if (_fullInit)
		{
			// just forward the call the callback function
			callback(mutationData);
		}
		// we need to retrieve missing data (lazy init)
		else
		{
			var process = function(data) {
				var mutations = new MutationCollection(data);
				_util.processMutationData(mutations);
				callback(data);
			};

			// add genesToQuery to the servlet params
			_servletParams.geneList = genesToQuery.join(" ");

			// retrieve data from the server
			$.post(_servletName, _servletParams, process, "json");
		}
	}

	/**
	 * Checks if there is mutation data for the current query
	 * (For the current gene list, case list, and cancer study).
	 *
	 * @return {boolean} true if there is mutation data, false otherwise.
	 */
	function hasData()
	{
		// TODO returning true in any case for now
		// we need to query server side for lazy init
		// since initially there is definitely no data
		//return (_util.getMutations().length > 0);
		return true;
	}

	return {
		initWithData : fullInit,
		initWithoutData: lazyInit,
		getMutationData: getMutationData,
		getGeneList: getGeneList,
		getUnsortedGeneList: getUnsortedGeneList,
		getMutationUtil: getMutationUtil,
		hasData: hasData
	};
};