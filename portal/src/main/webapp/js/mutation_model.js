/**
 * Mutation Model.
 *
 * Current model is sufficient to visualize both the table and the diagram.
 * Later we may add a few more
 * attributes: {...}
 */
var MutationModel = Backbone.Model.extend({
	initialize: function(attributes) {
		this.mutationId = attributes.mutationId;
		this.caseId = attributes.caseId;
		this.linkToPatientView = attributes.linkToPatientView;
		this.proteinChange = attributes.proteinChange;
		this.mutationType = attributes.mutationType;
		this.cosmic = attributes.cosmic;
		this.functionalImpactScore = attributes.functionalImpactScore;
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
	},
	url: function() {
		// TODO implement this to get the data from a web service
		var urlStr = "webservice.do?cmd=...";
	}
});

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
