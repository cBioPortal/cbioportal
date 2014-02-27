/**
 * Utility class for processing collection of mutations.
 *
 * @param mutations     [optional] a MutationCollection instance
 * @constructor
 *
 * @author Selcuk Onur Sumer
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
	};

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
				start: mutations[i].getProteinStartPos(),
				end: mutations[i].proteinPosEnd};

			positions.push(position);
		}

		return positions;
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

					if (mutations[j].mutationStatus.toLowerCase() === GERMLINE)
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

	this.distinctTumorTypeCount = function(gene)
	{
		gene = gene.toUpperCase();

		var tumorTypeMap = {};

		if (_mutationGeneMap[gene] != undefined)
		{
			var mutations = _mutationGeneMap[gene];

			for (var i=0; i < mutations.length; i++)
			{
				if (mutations[i].tumorType)
				{
					tumorTypeMap[mutations[i].tumorType] = true;
				}
			}
		}

		return _.keys(tumorTypeMap).length;
	};

	/**
	 * Returns a sorted array of data field counts for the given gene.
	 * Does not include counts for the values provided within
	 * the exclude list.
	 *
	 * @param gene          hugo gene symbol
	 * @param dataField     data field name
	 * @param excludeList   data values to exclude while counting
	 * @return {Array}  array of uniprot id count info
	 */
	this.dataFieldCount = function(gene, dataField, excludeList)
	{
		gene = gene.toUpperCase();

		var uniprotMap = {};

		if (_mutationGeneMap[gene] != undefined)
		{
			var mutations = _mutationGeneMap[gene];

			for (var i=0; i < mutations.length; i++)
			{
				var uniprot = mutations[i][dataField];

				if (uniprot &&
				    !_.contains(excludeList, uniprot))
				{
					if (uniprotMap[uniprot] === undefined)
					{
						uniprotMap[uniprot] = 0;
					}

					uniprotMap[uniprot]++;
				}
			}
		}

		var pairs = _.pairs(uniprotMap);

		pairs.sort(function(a, b) {
			return (b[1] - a[1]);
		});

		var result = [];

		_.each(pairs, function(pair, i) {
			var obj = {count: pair[1]};
			obj[dataField] = pair[0];
			result.push(obj);
		});

		return result;
	};

	// init maps by processing the initial mutations
	if (mutations != null)
	{
		this.processMutationData(mutations);
	}
};