/**
 * Mutation Model.
 *
 * Current model is sufficient to visualize both the table and the diagram.
 * Later we may add more data if necessary.
 *
 * @author Selcuk Onur Sumer
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
		this.tumorType = attributes.tumorType;
		this.proteinChange = attributes.proteinChange;
		this.mutationType = attributes.mutationType;
		this.cosmic = attributes.cosmic;
		this.cosmicCount = this.calcCosmicCount(attributes.cosmic);
		this.functionalImpactScore = attributes.functionalImpactScore;
		this.fisValue = attributes.fisValue;
		this.msaLink = attributes.msaLink;
		this.xVarLink = attributes.xVarLink;
		this.pdbLink = attributes.pdbLink;
		this.pdbMatch = attributes.pdbMatch; // {pdbId, chainId} pair
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
		this.uniprotAcc = attributes.uniprotAcc;
		this.proteinPosStart = attributes.proteinPosStart;
		this.proteinPosEnd = attributes.proteinPosEnd;
		this.mutationCount = attributes.mutationCount;
		this.specialGeneData = attributes.specialGeneData;
		this.keyword = attributes.keyword;
		this.cna = attributes.cna;
	},
	url: function() {
		// TODO implement this to get the data from a web service
		var urlStr = "webservice.do?cmd=...";
	},
	/**
	 * Finds out the protein start position for this mutation.
	 * The field proteinPosStart has a priority over proteinChange.
	 * If none of these has a valid value, then this function
	 * returns null.
	 *
	 * @return protein start position
	 */
	getProteinStartPos: function()
	{
		// first try protein start pos
		var position = this.proteinPosStart;

		// if not valid, then try protein change value
		if (position == null ||
		    position.length == 0 ||
		    position == "NA" ||
		    position < 0)
		{
			position = this.getProteinChangeLocation();
		}

		return position;
	},
	/**
	 * Finds the uniprot location for the protein change of
	 * the given mutation.
	 *
	 * @return {String} protein location as a string value
	 */
	getProteinChangeLocation: function()
	{
		var location = null;
		var proteinChange = this.proteinChange;
		var result = proteinChange.match(/[0-9]+/);

		if (result && result.length > 0)
		{
			location = result[0];
		}

		return location;
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
