/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License,
 * version 3, or (at your option) any later version.
 *
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

// This code is based on mutsig.js written by Gideon Dresdner 
// Sander Tan, Oleguer Plantalech and Pieter Lukasse, The Hyve
// December 2016

// Save selected checkboxes
var selectedBoxes;

// initialize and bind for
// geneset toggle button and geneset dialog box
var initGenesetDialogue = function() {
	"use strict";
	
	console.log("Setting up gene set popup event");

    // initialize geneset button
    // as hidden, and with JQuery UI style
    $('#toggle_geneset_dialog').hide();
    $('#toggle_geneset_dialog').button();

    // set up popup for gene set hierarchy
    $('#geneset_dialog').dialog({autoOpen: false,
        resizable: false,
        modal: true,
        minHeight: 600,
        minWidth: 800,
        maxHeight: 600,
        maxWidth: 800,       
        
    	// destroy the tree so that it is renewed upon next dialog pop-up and set the values to default
        close: function() {
            $('#select_gsva_percentile').val("75");
            $('#gsva_score_threshold_box').val("");
            $('#gsva_pvalue_threshold_box').val("");
            $('#jstree_genesets').jstree('destroy');
        }
    });

    // set listener for geneset select button
    $('#select_geneset').click(function() {
    	
    	// save selected checkboxes
    	selectedBoxes = $('#jstree_genesets').jstree("get_selected", true);
        // Update gene sets in query box
    	updateGenesetList();

        // close dialog box
        $('#geneset_dialog').dialog('close');
    });

    // set listener for geneset cancel button
    $('#cancel_geneset').click(function() {

        // close dialog box
        $('#geneset_dialog').dialog('close');
    });
    
    // set listener for filter button
    $('#filter_hierarchy').click(function() {

    	var percentile = $('#select_gsva_percentile').val();
    	var scoreThreshold = $('#gsva_score_threshold_box').val();
    	var pvalueThreshold = $('#gsva_pvalue_threshold_box').val();
    	
    	// Defaults: 
    	pvalueThreshold = (pvalueThreshold == '' ? "0.05" : pvalueThreshold);
    	scoreThreshold = (scoreThreshold == '' ? "0.5" : scoreThreshold);
    	
    	console.log("Filtering hierarchy data for: p-value < " + pvalueThreshold + ", score > " + scoreThreshold + ", percentile = " + percentile);
    	
    	// Destroy the previous tree
    	$('#jstree_genesets').jstree('destroy');
    	
    	// Call the webservice and render the tree again with webservice response:
    	initializeGenesetJstree(percentile, scoreThreshold, pvalueThreshold);
    });
};

// Inititalize gene set hierarchical tree
var initializeGenesetJstree = function (percentile, scoreThreshold, pvalueThreshold) {
	console.log("Initializing hierarchical tree for gene set popup");
	//defaults:
	percentile = percentile || "75";
	scoreThreshold = scoreThreshold || "0.5";
	pvalueThreshold = pvalueThreshold || "0.05";

	// Construct URL
	var hierarchyJSON = "api/geneset-hierarchy/fetch?geneticProfileId=" + gsvaGenomicProfileId;
	
	if (percentile != "") {
		hierarchyJSON = hierarchyJSON + "&percentile=" + percentile;
	}
	if (scoreThreshold != "") {
		hierarchyJSON = hierarchyJSON + "&scoreThreshold=" + scoreThreshold;
	}
	if (pvalueThreshold != "") {
		hierarchyJSON = hierarchyJSON + "&pvalueThreshold=" + pvalueThreshold;
	}
	
	var sampleIds = [];
	
	var sampleListId = $("#select_case_set").val();
	if (sampleListId == "-1") {
	    if ($("#main_form").find("input[name=patient_case_select]:checked").val() === "sample") {
	        sampleIds = $("#custom_case_set_ids").val().split(/[\s]+/);
	        postJSON(hierarchyJSON, sampleIds, hierarchyServiceCallback);
	    } else if ($("#main_form").find("input[name=patient_case_select]:checked").val() === "patient") {
	        window.cbioportal_client.getSamplesByPatient({study_id: [$("#select_single_study").val()],
	            patient_ids: $("#custom_case_set_ids").val().trim().replace(/"/g,'').split(/\s+/)}).then(function(result){
	                patientSampleMapping = result;
	                sampleIds = "";
	                for (var i=0,_len=patientSampleMapping.length; i<_len; i++) 
	                {
	                    var d = patientSampleMapping[i];
	                    d = d.id.trim();
	                    sampleIds += d + " ";
	                };
	                sampleIds = sampleIds.trim();
	                sampleIds = sampleIds.split(" ");
	                postJSON(hierarchyJSON, sampleIds, hierarchyServiceCallback);
	            })
	        }
	} else {
	    hierarchyJSON = hierarchyJSON + "&sampleListId=" + sampleListId;
	    postJSON(hierarchyJSON, sampleIds, hierarchyServiceCallback);
	}
	
	// Show loading image
	loadingImage.show();
 }	

var postJSON = function(url, data, callback) {
    return jQuery.ajax({
    headers: { 
        'Accept': 'application/json',
        'Content-Type': 'application/json' 
    },
    'type': 'POST',
    'url': url,
    'data': JSON.stringify(data),
    'dataType': 'json',
    'success': callback
    });
};

// Callback when gene set hierarchical tree is retrieved
var hierarchyServiceCallback = function(result_data) {
	// Hide loading image
	loadingImage.hide();
	
	var data = result_data;
	
	// Loop over JSON file to make it flat to input it in jsTree
	var flatData = [];
	
	// Leafs can be non-unique, therefor a unique ID is necessary,
	// because selecting a duplicate leaf results in a visualization issue.
	var leafId = 0;

	for (var i = 0; i < data.length; i++ ) {		
		// Read the node
		var nodeName = data[i].nodeName;
		var nodeParent = data[i].parentNodeName;
		
		// Convert node information to a flat format suitable for jstree
		if (nodeParent == null) {
			nodeParent = "#";
		}
		flatData.push({
			id : nodeName,
			parent : nodeParent,
			li_attr: {
				name: nodeName,
			},
			geneset : false,
			state : {
				opened : true,
				selected : false,
			}
		});
		
		// Check if node has any gene sets
		if (_.has(data[i], 'genesets')) {
			
			// Read the genesets in the node
			for (var j = 0; j < data[i].genesets.length; j++ ) {
				
				// Convert gene set information to a flat format suitable for jstree
				var genesetId = leafId ++;
				var genesetName = data[i].genesets[j].genesetId;
				var genesetDescription = data[i].genesets[j].description;
				var genesetRepresentativeScore = data[i].genesets[j].representativeScore;
				var genesetRepresentativePvalue = data[i].genesets[j].representativePvalue;
				var genesetRefLink = data[i].genesets[j].refLink;
	
				// Create leaf description
				
				// TODO Add number of genes to leaf description
//				var genesetNrGenes = data[i].genesets[j].nrGenes;
				
//				// Decide for geneset or genesets
//				var genePlurality;
//				if (genesetNrGenes == 1) {
//					genePlurality = 'gene';
//				} else {
//					genePlurality = 'genes';
//				}
//
//				// Add nr of genes to leaf
//				var genesetInfo = genesetNrGenes + ' ' + genePlurality + ',';
				var genesetInfo = '';

				// Add score to leaf
				genesetInfo = genesetInfo + 'score = ' +  parseFloat(genesetRepresentativeScore).toFixed(2);
				
				// Round pvalue
				// 0.005 is rounded to 0.01 and 0.0049 to 0.00, so below 0.005 should be exponential (5e-3)
				if (parseFloat(genesetRepresentativePvalue) < 0.005) {
					genesetRepresentativePvalue = parseFloat(genesetRepresentativePvalue).toExponential(0);
				} else {
					genesetRepresentativePvalue = parseFloat(genesetRepresentativePvalue).toFixed(2)
				}
				
				// Add pvalue to leaf 
				genesetInfo = genesetInfo + ', p-value = ' +  genesetRepresentativePvalue;
	
				// Build label and add styling
				var genesetNameText = genesetName + '<span style="font-weight:normal;font-style:italic;"> ' + genesetInfo + '</span>';
				
				flatData.push({
					// Add compulsary characteristics
					id : genesetId.toString(),
					parent : nodeName,
					text: genesetNameText, 
					state : {
						selected : false
					},
					li_attr : {
						name: genesetName,
					},
				
					// Also add additional data which might be useful later
					description : genesetDescription,
					representativeScore : genesetRepresentativeScore,
					representativePvalue : genesetRepresentativePvalue,
					refLink : genesetRefLink,
					// nrGenes : genesetNrGenes
					geneset : true,
					
				});
			}
		}
	}

	// Build the tree
	$('#jstree_genesets').jstree({
		"plugins": ['checkbox', 'search'],
		"search": {
			'show_only_matches': true,
			},
		"core" : {
			"data" : flatData,
			"themes": {
				"icons" : false
			},
			"check_callback" : true
		}
			
	// This keeps nodes open after searching for them
	}).on('search.jstree before_open.jstree', function (e, data) {
	    if(data.instance.settings.search.show_only_matches) {
	        data.instance._data.search.dom.find('.jstree-node')
	            .show().filter('.jstree-last').filter(function() { return this.nextSibling; }).removeClass('jstree-last')
	            .end().end().end().find(".jstree-children").each(function () { $(this).children(".jstree-node:visible").eq(-1).addClass("jstree-last"); });
	    }
	});
	
	// Search function
	var to = false;
	$('#jstree_genesets_searchbox').keyup(function () {
	    if(to) { clearTimeout(to); }
	    to = setTimeout(function () {
	    	var v = $('#jstree_genesets_searchbox').val();
	    	
	    	if (v == "") {
	        	$('#jstree_genesets').jstree(true).show_all();
	    	} else {
		    	$('#jstree_genesets').jstree(true).search(v);
	    	}
	    }, 250);	    
	});
}	


var updateGenesetList = function() {
    "use strict";

    // push all the genes in the gene_list onto a list
    var genesetList = $('#geneset_list').val();
    
    // Create list of objects with selected gene sets
    var selectedGenesets = [];

	// After we get the selection, we can remove the tree object
	$('#jstree_genesets').jstree('destroy');
	
	// Loop over the selected checkboxes
	for (var i = 0; i < selectedBoxes.length; i++ ){
		
		// Check if selected checkbox is not a node
		if (selectedBoxes[i].original.geneset) {
			var boxName = selectedBoxes[i].original.li_attr.name;
			
			// Select a geneset only once
			if (selectedGenesets.indexOf(boxName) == -1) {
				selectedGenesets.push(boxName);
			}
		}
	}
	
    // if gene_list is currently empty put all the checked geneset genes into it.
    if (genesetList === "") {
        genesetList = [];
        $.each(selectedGenesets, function(index, value) {     // don't select the Select All checkbox
            genesetList.push(value);
        });
        genesetList = genesetList.join(" ");
    }

    else {
        // look for the selected genesets in gene_list
        // if they're not there, append them
        $.each(selectedGenesets, function(index, value) {
            var checked = value;

            if ( genesetList.search(new RegExp(checked, "i")) === -1 ) {
                genesetList = $.trim(genesetList);
                checked = " " + checked;
                genesetList += checked;
            }
        });
    }
    
    // remove spaces around gene_list
    genesetList = $.trim(genesetList);
    
    // replace 2 or more spaces in a row by 1 space
    genesetList = genesetList.replace(/\s{1,}/, " ");
    
    // Update the gene set list in the query box
    $('#geneset_list').val(genesetList);
};


// todo: refactor this and put it in with other init functions in step3.json
$(document).ready( function () {
    "use strict";
    initGenesetDialogue();
});
