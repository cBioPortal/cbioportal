/**
 * Mutation Model.
 *
 * Current model is sufficient to visualize both the table and the diagram.
 * Later we may add more data if necessary.
 */
var MutationModel = Backbone.Model.extend({
	initialize: function(attributes) {
		this.geneticProfileId = attributes.geneticProfileId;
		this.mutationEventId = attributes.mutationEventId;
		this.caseId = attributes.caseId;
		this.geneSymbol = attributes.geneSymbol;
		this.linkToPatientView = attributes.linkToPatientView;
		this.proteinChange = attributes.proteinChange;
		this.mutationType = attributes.mutationType;
		this.cosmic = attributes.cosmic;
		this.functionalImpactScore = attributes.functionalImpactScore;
		this.fisValue = attributes.fisValue;
		this.msaLink = attributes.msaLink;
		this.xVarLink = attributes.xVarLink;
		this.pdbLink = attributes.pdbLink;
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
		this.mutationCount = attributes.mutationCount;
		this.cosmicCount = attributes.cosmicCount; // TODO calculate this on the client side?
		this.specialGeneData = attributes.specialGeneData;
	},
	url: function() {
		// TODO implement this to get the data from a web service
		var urlStr = "webservice.do?cmd=...";
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
 * Utility class for processing a collection of mutations.
 *
 * @param mutations     MutationCollection (list of mutations)
 * @constructor
 */
var MutationDetailsUtil = function(mutations)
{
	this.GERMLINE = "germline"; // germline mutation constant

	this.getMutationGeneMap = function()
	{
		return this._mutationGeneMap;
	};

	this.getMutationCaseMap = function()
	{
		return this._mutationCaseMap;
	};

	/**
	 * Processes the collection of mutations, and creates a map of
	 * <geneSymbol, mutation array> pairs.
	 *
	 * @param mutations collection of mutations
	 * @return {object} map of mutations (keyed on gene symbol)
	 * @private
	 */
	this._generateGeneMap = function(mutations)
	{
		var mutationMap = {};

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
	this._generateCaseMap = function(mutations)
	{
		var mutationMap = {};

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
			var mutations = this._mutationCaseMap[cases[i].toLowerCase()];

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
	 * Checks if there is a germline mutation for the given gene.
	 *
	 * @param gene  hugo gene symbol
	 */
	this.containsGermline = function(gene)
	{
		var self = this;
		var contains = false;

		gene = gene.toLowerCase();

		if (self._mutationGeneMap[gene] != undefined)
		{
			var mutations = self._mutationGeneMap[gene];

			for (var i=0; i < mutations.length; i++)
			{
				if (mutations[i].mutationStatus.toLowerCase() == self.GERMLINE)
				{
					contains = true;
					break;
				}
			}
		}

		return contains;
	};


	// init class variables
	this._mutationGeneMap = this._generateGeneMap(mutations);
	this._mutationCaseMap = this._generateCaseMap(mutations);
	this._mutations = mutations;
};
