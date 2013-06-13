/**
 * Constructor for the network (sbgn) visualization class.
 *
 * @param divId     target div id for this visualization.
 * @constructor
 */
function NetworkSbgnVis(divId)
{
	// call the parent constructor
	NetworkVis.call(this, divId);
}

//this simulates NetworkSbgnVis extends NetworkVis (inheritance)
NetworkSbgnVis.prototype = new NetworkVis("");

//update constructor
NetworkSbgnVis.prototype.constructor = NetworkSbgnVis;

//TODO override necessary methods (filters, inspectors, initializers, etc.) to have a proper UI.

//Genomic data parser method
NetworkSbgnVis.prototype.parseGenomicData = function(genomicData)
{
	var hugoToGene 			= "hugo_to_gene_index";
	var geneData   			= "gene_data";
	var cna 	   			= "cna";
	var hugo 	   			= "hugo";
	var mrna	   			= "mrna";
	var mutations  			= "mutations";
	var rppa	   			= "rppa";
	var percent_altered 	= "percent_altered";

	//first extend node fields to support genomic data
	addGenomicFields();

	// iterate for every hugo gene symbol in incoming data
	for(var hugoSymbol in genomicData[hugoToGene])
	{
		var geneDataIndex = genomicData[hugoToGene][hugoSymbol];		// gene data index for hugo gene symbol
		var _geneData 	  = genomicData[geneData][geneDataIndex];		// corresponding gene data

		// Arrays and percent altered data 
		var cnaArray   		= _geneData[cna];
		var mrnaArray  		= _geneData[mrna];
		var mutationsArray 	= _geneData[mutations];
		var rppa	  		= _geneData[rppa];
		var percentAltered 	= _geneData[percent_altered];

		// corresponding cytoscape web node
		var targetNode = findNodel(hugoSymbol);
		if(targetNode != null)
		{
			calcCNAPercents(cnaArray, targetNode);
			calcMutationPercent(mutationsArray, targetNode);
			calcRPPAorMRNAPercent(mrnaArray, mrna, targetNode);
			calcRPPAorMRNAPercent(mrnaArray, rppa, targetNode);
			targetNode.data['PERCENT_ALTERED'] = percentAltered;
		}	

	}

}

//Searches an sbgn node whose label fits with parameter hugoSymbol
function findNodel(hugoSymbol)
{
	var nodeArray = this._vis.nodes();
	var node = null;
	for ( var i = 0; i < nodeArray.length; i++) 
	{
		if(nodeArray[i].label == hugoSymbol)
		{
			node = nodeArray[i];
			break;
		}
	}
	return node;
}

// calculates cna percents ands adds them to target node
function calcCNAPercents(cnaArray, targetNode)
{  
	var amplified	= "AMPLIFIED";
	var diploid   	= "DIPLOID";
	var gained    	= "GAINED";
	var hemiDeleted 	= "HEMIZYGOUSLYDELETED";
	var homoDeleted	= "HOMODELETED";

	var percents = {};
	percents[amplified] = 0;
	percents[diploid] = 0;
	percents[gained] = 0;
	percents[hemiDeleted] = 0;
	percents[homoDeleted] = 0;

	var increment = 1/cnaArray.length;

	for(var i = 0; i < cnaArray.length; i++)
	{
		if(cnaArray[i] != null)
			percents[cnaArray[i]] += increment; 

	}

	targetNode.data['PERCENT_CNA_AMPLIFIED'] = percents[amplified];
	targetNode.data['PERCENT_CNA_GAINED'] = percents[gained];
	targetNode.data['PERCENT_CNA_HEMIZYGOUSLY_DELETED'] = percents[hemiDeleted];
	targetNode.data['PERCENT_CNA_HOMOZYGOUSLY_DELETED'] = percents[homoDeleted];
	targetNode.data['PERCENT_CNA_DIPLOID'] = percents[diploid];

}

//calculates rppa or mrna percents ands adds them to target node, data indicator determines which data will be set
function calcRPPAorMRNAPercent(dataArray, dataIndicator, targetNode)
{  
	var up		= "UPREGULATED";
	var down   	= "DOWNREGULATED";
	var normal   = "NORMAL";

	var percents = {};
	percents[up] = 0;
	percents[down] = 0;
	percents[normal] = 0;

	var increment = 1/dataArray.length;

	for(var i = 0; i < dataArray.length; i++)
	{
		if(dataArray[i] != null)
			percents[dataArray[i]] += increment; 
	}

	if (dataIndicator == "mrna") 
	{	
		targetNode.data['PERCENT_MRNA_UP'] = percents[up];
		targetNode.data['PERCENT_MRNA_DOWN'] = percents[down];
		targetNode.data['PERCENT_MRNA_NORMAL'] = percents[normal];
	} 
	else if(dataIndicator == "rppa") 
	{
		targetNode.data['PERCENT_RPPA_UP'] = percents[up];
		targetNode.data['PERCENT_RPPA_DOWN'] = percents[down];
		targetNode.data['PERCENT_RPPA_NORMAL'] = percents[normal];
	}
}

//calculates mutation percents ands adds them to target node
function calcMutationPercent(mutationArray, targetNode)
{  
	var percent = 0;
	var increment = 1/mutationArray.length
	for(var i = 0; i < mutationArray.length; i++)
	{
		if(mutationArray[i] != null)
			percent += increment;  
	}
	targetNode.data['PERCENT_MUTATED'] = percent;
}

// extends node fields by adding new fields according to genomic data
function addGenomicFields()
{
	var cna_amplified 	= {name:"PERCENT_CNA_AMPLIFIED", type:"number"};
	var cna_gained		= {name:"PERCENT_CNA_GAINED", type:"number"};
	var cna_homodel 	= {name:"PERCENT_CNA_HOMOZYGOUSLY_DELETED", type:"number"};
	var cna_hemydel		= {name:"PERCENT_CNA_HEMIZYGOUSLY_DELETED", type:"number"};
	var cna_diploid		= {name:"PERCENT_CNA_DIPLOID", type:"number"};

	var mrna_up 				= {name:"PERCENT_MRNA_UP", type:"number"};
	var mrna_down 				= {name:"PERCENT_MRNA_DOWN", type:"number"};
	var mrna_normal 			= {name:"PERCENT_MRNA_NORMAL", type:"number"};

	var rppa_up 				= {name:"PERCENT_RPPA_UP", type:"number"};
	var rppa_down 				= {name:"PERCENT_RPPA_DOWN", type:"number"};
	var rppa_normal	 			= {name:"PERCENT_RPPA_NORMAL", type:"number"};

	var mutated					= {name:"PERCENT_MUTATED", type:"number"};
	var altered					= {name:"PERCENT_ALTERED", type:"number"};


	this._vis.addDataField(cna_amplified);
	this._vis.addDataField(cna_gained);
	this._vis.addDataField(cna_homodel);
	this._vis.addDataField(cna_hemydel);
	this._vis.addDataField(cna_diploid);

	this._vis.addDataField(mrna_down);
	this._vis.addDataField(mrna_up);
	this._vis.addDataField(mrna_normal);

	this._vis.addDataField(rppa_down);
	this._vis.addDataField(rppa_up);
	this._vis.addDataField(rppa_normal);

	this._vis.addDataField(mutated);
	this._vis.addDataField(altered);
}



