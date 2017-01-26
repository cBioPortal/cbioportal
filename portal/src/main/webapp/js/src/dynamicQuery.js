/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
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


/******************************************************************************************
* Dynamic Query Javascript, built with JQuery
* @author Ethan Cerami, Caitlin Byrne. 
*
* This code performs the following functions:
*
* 1.  Connects to the portal via AJAX and downloads a JSON document containing information
*     regarding all cancer studies and gene sets stored in the CGDS.
* 2.  Creates event handler for when user selects a cancer study.  This triggers updates
      in the genomic profiles and case lists displayed.
* 3.  Creates event handler for when user selects a gene set.  This triggers updates to the
      gene set text area.
******************************************************************************************/

// Create Constants
var PROFILE_MUTATION = "PROFILE_MUTATION";
var PROFILE_MUTATION_EXTENDED = "PROFILE_MUTATION_EXTENDED";
var PROFILE_COPY_NUMBER_ALTERATION = "PROFILE_COPY_NUMBER_ALTERATION"
var PROFILE_MRNA_EXPRESSION = "PROFILE_MRNA_EXPRESSION";
var PROFILE_PROTEIN = "PROFILE_PROTEIN";
var PROFILE_PROTEIN_EXPRESSION = "PROFILE_PROTEIN_EXPRESSION";
var PROFILE_METHYLATION = "PROFILE_METHYLATION"

var caseSetSelectionOverriddenByUser = false;
var selectedStudiesStorageKey = "cbioportal_selected_studies";

//  Create Log Function, if FireBug is not Installed.
if(typeof(console) === "undefined" || typeof(console.log) === "undefined")
    var console = { log: function() { } };

//  Triggered only when document is ready.
$(document).ready(function(){
	
     //  Load Portal JSON Meta Data while showing loader image in place of query form
     loadMetaData();

     //  Set up Event Handler for User Selecting Cancer Study from Pull-Down Menu
     $("#select_single_study").change(function() {
         caseSetSelectionOverriddenByUser = false; // reset
         console.log("#select_single_study change ( cancerStudySelected() )");
         cancerStudySelected();
         
         caseSetSelected();
         $('#custom_case_set_ids').empty(); // reset the custom case set textarea
	 $('#select_single_study').trigger('doneChanging');
     });

    // Set up Event Handler for User Selecting a Case Set
    $("#select_case_set").change(function() {
        caseSetSelected();
        caseSetSelectionOverriddenByUser = true;
    });

    // Set up Event Handler for User Selecting a Get Set
    $("#select_gene_set").change(function() {
        geneSetSelected();
    });

    //  Set up Event Handler for View/Hide JSON Debug Information
    $("#json_cancer_studies").click(function(event) {
      event.preventDefault();
      $('#cancer_results').toggle();
    });

    //  Set up an Event Handler to intercept form submission
    $("#main_form").submit(chooseAction);

    //  Set up an Event Handler for the Query / Data Download Tabs
    $("#query_tab").click(function(event) {
       event.preventDefault();
        userClickedMainTab("tab_visualize")
    });
    $("#download_tab").click(function(event) {
       event.preventDefault();
       userClickedMainTab("tab_download");
    });
    
    $("#gene_list").on('input', function(event) {
	$('.oql_error').remove();
    });
    
    // Set up custom case set related GUI & event handlers (step 3)
    initCustomCaseSetUI();

    //  set toggle Step 5: Optional arguments
    //$("#optional_args").hide();
    /*$("#step5_toggle").click(function(event) {
        event.preventDefault();
        $("#optional_args").toggle( "blind" );
    });*/
    
    $('.netsize_help').tipTip();

    $('#step5 > .step_header').click(function(){
         $(".ui-icon", this).toggle();
         $("#optional_args").toggle();
    });

    // unset cookie for results tabs, so that a new query
    // always goes to summary tab first
    $.cookie("results-tab",null);

});  //  end document ready function


function supportsHTML5Storage() {
	// from diveintohtml5.info/storage.html
	try {
		return 'localStorage' in window && window['localStorage'] !== null;
	} catch (e) {
		return false;
	}
}
//  Load study Meta Data, i.e. everything except the name, which we load earlier to
//  	populate the dropdown menu.
function loadStudyMetaData(cancerStudyId) {
    console.log("loadStudyMetaData ("+cancerStudyId+")");
    $('.main_query_panel').fadeTo("fast",0.6);
	
    $.getJSON("portal_meta_data.json?study_id="+cancerStudyId, function(json){
        window.metaDataJson.cancer_studies[cancerStudyId] = json;
        updateCancerStudyInformation();
        $('.main_query_panel').stop().fadeTo("fast",1);
    });
}

// Load geneset gene list
function loadGeneList(geneSetId) {
    $('.main_query_panel').fadeTo("fast",0.6);

    $.getJSON("portal_meta_data.json?geneset_id="+geneSetId.replace(/\//g,""), function(json){
        window.metaDataJson.gene_sets[geneSetId].gene_list = json.list;
        $("#gene_list").val(json.list);
        $('.main_query_panel').stop().fadeTo("fast",1);
    });
}
	
//  Load Portal JSON Meta Data, while showing loader image
function loadMetaData() {
    $('#load').remove();
    //  show ajax loader image; loader is background image of div 'load' as set in css
    $('.main_query_panel').append('<div id="load">&nbsp;</div>');
    $('#load').fadeIn('slow');

    //  hide the main query form until all meta data is loaded and added to page
    $('#main_query_form').hide('fast',loadContent);

    function loadContent() {
        //  Get Portal JSON Meta Data via JQuery AJAX
	window.metaDataPromise = $.Deferred();

        jQuery.getJSON("portal_meta_data.json?partial_studies=true&partial_genesets=true",function(json){
            //  Store JSON Data in global variable for later use
            window.metaDataJson = json;
	    window.metaDataPromise.resolve(json);

            // Load data of selected study right at the outset before continuing
            $.getJSON("portal_meta_data.json?study_id="+window.cancer_study_id_selected, function(json) {
                console.log("Loading metadata for "+window.cancer_study_id_selected);
                // this code should be about the same as in loadStudyMetaData
                window.metaDataJson.cancer_studies[window.cancer_study_id_selected] = json;
                //  Add Meta Data to current page
                addMetaDataToPage();
                showNewContent();
            });
        });
    }

    function showNewContent() {
        //show content, hide loader only after content is shown
        $('#main_query_form').fadeIn('fast', hideLoader);
    }
    function hideLoader() {
        //hide loader image
        $('#load').fadeOut('fast',removeLoader());
    }
    function removeLoader() {
        // remove loader image so that it will not appear in the
        // modify-query section on results page
        $('#load').remove();
    }
}

//  Triggered when the User Selects one of the Main Query or Download Tabs
function userClickedMainTab(tabAction) {

    window.changingTabs = true;
    //  Change hidden field value
    $("#tab_index").val(tabAction);
    $("#main_form").get(0).elements["Action"].setAttribute("value","");

    //  Then, submit the form
    $("#main_form").submit();
}

//  When the page is first loaded, the default query will be a cross-cancer type
//  search in which the user will enter ONLY a gene list; Also when "All Cancer Studies"
//  is selected in Step 1
function crossCancerStudySelected() {
     $('#step2').hide();
     $('#step2cross').show();
     $('#step3').hide();
     $('#step5').hide();
}

//  Display extra steps when an individual cancer study is selected
function singleCancerStudySelected() {
    $("#step2").show();
    $('#step2cross').hide();
    $("#step3").show();
    //$("#step5").show();
}

//  Select default genomic profiles
function makeDefaultSelections(){

    $('.' + PROFILE_MUTATION_EXTENDED).prop('checked',true);
    $('.' + PROFILE_COPY_NUMBER_ALTERATION +':checkbox').prop('checked',true);
    $('.' + PROFILE_COPY_NUMBER_ALTERATION +':radio').first().prop('checked',true);

}

// Triggered after meta data is added to page in case page is
// re-drawn after query error and also any time a new cancer
// type is selected; Assesses the need for default selections
// and sets the visibility of each step based on current selections
function reviewCurrentSelections(){

   //HACK TO DEAL WITH ASYNCHRONOUS STUFF SO WE DONT DO THIS UNTIL AFTER METADATA ADDED
   if (window.metaDataAdded === true) {  
    // Unless the download tab has been chosen or 'All Cancer Studies' is
    // selected, iterate through checkboxes to see if any are selected; if not,
    // make default selections
    if (window.tab_index !== "tab_download" && $("#select_single_study").val() !== 'all'){
         var setDefaults = true;

         // if no checkboxes are checked, make default selections
         $('#genomic_profiles input:checkbox').each(function(){
             if ($(this).prop('checked')){
                 setDefaults = false;
                 return;
             }
         });

         if (setDefaults){
             console.log("reviewCurrentSelections ( makeDefaultSelections() )");
             makeDefaultSelections();
         }
    } 

    updateDefaultCaseList();

    // determine whether mRNA threshold field should be shown or hidden
    // based on which, if any mRNA profiles are selected
    toggleThresholdPanel($("." + PROFILE_MRNA_EXPRESSION+"[type=checkbox]"), PROFILE_MRNA_EXPRESSION, "#z_score_threshold");

    // similarly with RPPA
    toggleThresholdPanel($("." + PROFILE_PROTEIN_EXPRESSION+"[type=checkbox]"), PROFILE_PROTEIN_EXPRESSION, "#rppa_score_threshold");

    // determine whether optional arguments section should be shown or hidden
 //   if ($("#optional_args > input").length >= 1){
 //       $("#optional_args > input").each(function(){
 //           if ($(this).prop('checked')){
 //               // hide/show is ugly, but not sure exactly how toggle works
 //               // and couldn't get it to work.. this will do for now
 //               $("#step5 > .step_header > .ui-icon-triangle-1-e").hide();
 //               $("#step5 > .step_header > .ui-icon-triangle-1-s").show();
 //               $("#optional_args").toggle();
 //               return;
 //           }
 //       });
 //   }
   }
}


var submitHandler = (function() {
	var sample_mapping_completed = false;
	return function() {
		if (sample_mapping_completed) {
			$('#main_form').submit();
			sample_mapping_completed = false;
		} else {
			getMapping().then(function() {
				sample_mapping_completed = true;
				submitHandler();
			});
		}
	};
})();
// Get mapping if necessary
function getMapping() {
    function setPatientSampleIdMap(_sampleMap) {
        var samples_string = "";
        for (var i=0,_len=_sampleMap.length; i<_len; i++) 
        {
            var d = _sampleMap[i];
            samples_string += d.id + "\n";
        };
        return samples_string;
    }
    
    function getMap() {
	var def = new $.Deferred();
        // Get input selection
        var caseIds = $("#custom_case_set_ids").val().trim().replace(/"/g,'').split(/\s+/);
        // Get study selection
        var studyId = $("#select_single_study").val();
        if (caseIds[0] !== "")
        {
            window.cbioportal_client.getSamplesByPatient({study_id: [studyId],patient_ids: caseIds}).then(function(sampleMap){
                $("#custom_case_set_ids").val(setPatientSampleIdMap(sampleMap));
		def.resolve();
            });                
        }
        else {
            def.resolve();
        }
        
        return def;
    }
    if ($("#main_form").find("input[name=patient_case_select]:checked").val() === "patient") {
	    return getMap();
    } else {	
	var def = new $.Deferred();
	def.resolve();
	return def.promise();
    }
}
//  Determine whether to submit a cross-cancer query or
//  a study-specific query
function chooseAction(evt) {
    $(".error_box").remove();
       var haveExpInQuery = false;
       if (!window.changingTabs) {
		// validate OQL
		try {
			var parsed_result = oql_parser.parse($('#gene_list').val());
			for (var i = 0; i < parsed_result.length; i++) {
			    for (var j = 0; j < parsed_result[i].alterations.length; j++) {
				if (parsed_result[i].alterations[j].constr_val === "EXP") {
				    haveExpInQuery = true;
				    break;
				}
			    }
			    if (haveExpInQuery) {
				break;
			    }
			}
		} catch (err) {
			var offset = err.offset;
			if (offset === $('#gene_list').val().length) {
			    createAnError("OQL syntax error after selected character; please fix and submit again.", $('#gene_list'), "oql_error");
			    $('#gene_list')[0].setSelectionRange(err.offset-1, err.offset);
			} else if (offset === 0) {
			    createAnError("OQL syntax error before selected character; please fix and submit again.", $('#gene_list'), "oql_error");
			    $('#gene_list')[0].setSelectionRange(err.offset, err.offset+1);
			} else {
			    createAnError("OQL syntax error at selected character; please fix and submit again.", $('#gene_list'), "oql_error");
			    $('#gene_list')[0].setSelectionRange(err.offset, err.offset+1);
			}
			return false;
		}
       }
    var selected_studies = $("#jstree").jstree(true).get_selected_leaves();
    if (selected_studies.length === 0 && !window.changingTabs) {
            // select all by default
            $("#jstree").jstree(true).select_node(window.jstree_root_id);
            selected_studies = $("#jstree").jstree(true).get_selected_leaves()
    }    
    if (selected_studies.length > 1) {
	if ( haveExpInQuery ) {
            createAnError("Expression filtering in the gene list is not supported when doing cross cancer queries.",  $('#gene_list'));
            return false;
        }
        $("#main_form").find("#select_multiple_studies").val("");
        if ($("#tab_index").val() == 'tab_download') {
            $("#main_form").get(0).setAttribute('action','index.do');
        }
        else {
                var dataPriority = $('#main_form').find('input[name=data_priority]:checked').val();
                var newSearch = $('#main_form').serialize() + '&Action=Submit#crosscancer/overview/'+dataPriority+'/'+encodeURIComponent($('#gene_list').val())+'/'+encodeURIComponent(selected_studies.join(","));
                evt.preventDefault();
                window.location = 'cross_cancer.do?' + newSearch;
            //$("#main_form").get(0).setAttribute('action','cross_cancer.do');
        }
        
    } else if (selected_studies.length === 1) {
        $("#main_form").find("#select_single_study").val(selected_studies[0]);
        $("#main_form").get(0).setAttribute('action','index.do');

        if ( haveExpInQuery ) {
            var expCheckBox = $("." + PROFILE_MRNA_EXPRESSION);

            if( expCheckBox.length > 0 && expCheckBox.prop('checked') == false) {
                    createAnError("Expression specified in the list of genes, but not selected in the" +
                                        " Genetic Profile Checkboxes.",  $('#gene_list'));
                    evt.preventDefault();
            } else if( expCheckBox.length == 0 ) {
                createAnError("Expression specified in the list of genes, but not selected in the" +
                                    " Genetic Profile Checkboxes.",  $('#gene_list'));
                evt.preventDefault();
            }
        }
    }

}

function createAnError(errorText, targetElt, optClassStr) {
	var errorBox = $("<div class='error_box'>").addClass("ui-state-error ui-corner-all exp_error_box");
	if (optClassStr) {
	    errorBox.addClass(optClassStr);
	}
	var errorButton = $("<span>").addClass("ui-icon ui-icon-alert exp_error_button");
	var strongErrorText = $("<small>").html("Error: " + errorText + "<br>");
	var errorTextBox = $("<span>").addClass("exp_error_text");
	
	errorButton.appendTo(errorBox);
	strongErrorText.appendTo(errorTextBox);
	errorTextBox.appendTo(errorBox);
	
	errorBox.insertBefore(targetElt);
	errorBox.slideDown();
	
	return errorBox;
}

//  Triggered when a genomic profile radio button is selected
function genomicProfileRadioButtonSelected(subgroupClicked) {
    var subgroupClass = subgroupClicked.attr('class');
    if (subgroupClass != undefined && subgroupClass != "") {
        var checkboxSelector = "input."+subgroupClass+"[type=checkbox]";
        if (checkboxSelector != undefined) {
            $(checkboxSelector).prop('checked',true);
        }
    }
    updateDefaultCaseList();
}

//  Triggered when a genomic profile group check box is selected.
function profileGroupCheckBoxSelected(profileGroup) {
    var profileClass = profileGroup.attr('class');
    $("input."+profileClass+"[type=radio]").prop('checked',false);
    if (profileGroup.prop('checked')) {
        var rnaSeqRadios = $("input."+profileClass+"[type=radio][value*='rna_seq']");
        if (rnaSeqRadios.length>0) {
            rnaSeqRadios.first().prop('checked',true);
        } else {
            $("input."+profileClass+"[type=radio]").first().prop('checked',true);
        }
    }
    updateDefaultCaseList();
}

// update default case list based on selected profiles
function updateDefaultCaseList() {
    if (caseSetSelectionOverriddenByUser) return;
    var mutSelect = $("input.PROFILE_MUTATION_EXTENDED[type=checkbox]").prop('checked');
    var cnaSelect = $("input.PROFILE_COPY_NUMBER_ALTERATION[type=checkbox]").prop('checked');
    var expSelect = $("input.PROFILE_MRNA_EXPRESSION[type=checkbox]").prop('checked');
    var rppaSelect = $("input.PROFILE_PROTEIN_EXPRESSION[type=checkbox]").prop('checked');
    var selectedCancerStudy = $('#select_single_study').val();
    var defaultCaseList = selectedCancerStudy+"_all";
    if (mutSelect && cnaSelect && !expSelect && !rppaSelect) {
        defaultCaseList = selectedCancerStudy+"_cnaseq";
        if ($("#select_case_set option[value='"+defaultCaseList+"']").length == 0) {
            defaultCaseList = selectedCancerStudy+"_cna_seq";  //TODO: Better to unify to this one
        }
    } else if (mutSelect && !cnaSelect && !expSelect && !rppaSelect) {
        defaultCaseList = selectedCancerStudy+"_sequenced";
    } else if (!mutSelect && cnaSelect && !expSelect && !rppaSelect) {
        defaultCaseList = selectedCancerStudy+"_acgh";
    } else if (!mutSelect && !cnaSelect && expSelect && !rppaSelect) {
        if ($('#'+selectedCancerStudy+'_mrna_median_Zscores').prop('checked')) {
            defaultCaseList = selectedCancerStudy+"_mrna";
        } else if ($('#'+selectedCancerStudy+'_rna_seq_mrna_median_Zscores').prop('checked')) {
            defaultCaseList = selectedCancerStudy+"_rna_seq_mrna";
        } else if ($('#'+selectedCancerStudy+'_rna_seq_v2_mrna_median_Zscores').prop('checked')) {
            defaultCaseList = selectedCancerStudy+"_rna_seq_v2_mrna";
        }
    } else if ((mutSelect || cnaSelect) && expSelect && !rppaSelect) {
        defaultCaseList = selectedCancerStudy+"_3way_complete";
    } else if (!mutSelect && !cnaSelect && !expSelect && rppaSelect) {
        defaultCaseList = selectedCancerStudy+"_rppa";
    }
    
    $('#select_case_set').val(defaultCaseList);
    
    // HACKY CODE START -- TO SOLVE THE PROBLEM THAT WE HAVE BOTH _complete and _3way_complete
    if (!$('#select_case_set').val()) {
        if (defaultCaseList===selectedCancerStudy+"_3way_complete") {
            $('#select_case_set').val(selectedCancerStudy+"_complete");
        }
    }// HACKY CODE END
    
    if (!$('#select_case_set').val()) {     
        // in case no match
        $('#select_case_set').val(selectedCancerStudy+"_all");
    }
    
    updateCaseListSmart();
}

//  Print message and disable submit if use choosed a cancer type
//  for which no genomic profiles are available
function genomicProfilesUnavailable(){
    $("#genomic_profiles").html("<strong>No Genomic Profiles available for this Cancer Study</strong>");
    $('#main_submit').attr('disabled',true);
}

// Show or hide mRNA threshold field based on mRNA profile selected
function toggleThresholdPanel(profileClicked, profile, threshold_div) {
    var selectedProfile = profileClicked.val();
    var inputType = profileClicked.attr('type');

    // when a radio button is clicked, show threshold input unless user chooses expression outliers
    if(inputType == 'radio'){
        if(selectedProfile.indexOf("outlier")==-1){
            $(threshold_div).slideDown();
        } else {
            $(threshold_div).slideUp();
        }
    } else if(inputType == 'checkbox'){

        // if there are NO subgroups, show threshold input when mRNA checkbox is selected.
        if (profileClicked.prop('checked')){
            $(threshold_div).slideDown();
        }
        // if checkbox is unselected, hide threshold input regardless of whether there are subgroups
        else {
            $(threshold_div).slideUp();
        }
    }
}

// toggle:
//      gistic button
//      mutsig button
// according to the cancer_study
function toggleByCancerStudy(cancer_study) {
    var mutsig = $('#toggle_mutsig_dialog');
    var gistic = $('#toggle_gistic_dialog_button');
    if (cancer_study.has_mutsig_data) {
        mutsig.show();
    } else {
        mutsig.hide();
    }
    if (cancer_study.has_gistic_data) {
        gistic.show();
    } else {
        gistic.hide();
    }
}

function updateCaseListSmart() {
    $("#select_case_set").trigger("liszt:updated");
    $("#select_case_set_chzn .chzn-drop ul.chzn-results li")
        .each(function(i, e) {
        	//make qtip for an element on first mouseenter:
        	cbio.util.addTargetedQTip($(e), {
        		content: "<font size='2'>" + $($("#select_case_set option")[i]).attr("title") + "</font>",
        		style: {
        			classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'
                }
        	}); 
      });
}

// Called when and only when a cancer study is selected from the dropdown menu
function updateCancerStudyInformation() {
    var cancerStudyId = $("#main_form").find("#select_single_study").val();
    var cancer_study = window.metaDataJson.cancer_studies[cancerStudyId];

    // toggle every time a new cancer study is selected
    toggleByCancerStudy(cancer_study);

    if (cancerStudyId=='all'){
        crossCancerStudySelected();
        return;
    }

    //  Update Cancer Study Description
    var citation = cancer_study.citation;
    if (!citation) {
        citation="";
    }
    else {
        var pmid = cancer_study.pmid;
        if (pmid) {
            citation = " <a href='http://www.ncbi.nlm.nih.gov/pubmed/"+pmid+"'>"+citation+"</a>";
        }
    }

    //  Iterate through all genomic profiles
    //  Add all non-expression profiles where show_in_analysis_tab = true
    //  First, clear all existing options
    $("#genomic_profiles").html("");

    //  Add Genomic Profiles, in this order
    addGenomicProfiles(cancer_study.genomic_profiles, "MUTATION", PROFILE_MUTATION, "Mutation");
    addGenomicProfiles(cancer_study.genomic_profiles, "MUTATION_EXTENDED", PROFILE_MUTATION_EXTENDED, "Mutation");
    addGenomicProfiles(cancer_study.genomic_profiles, "COPY_NUMBER_ALTERATION", PROFILE_COPY_NUMBER_ALTERATION, "Copy Number");
    addGenomicProfiles(cancer_study.genomic_profiles, "MRNA_EXPRESSION", PROFILE_MRNA_EXPRESSION, "mRNA Expression");
    addGenomicProfiles(cancer_study.genomic_profiles, "METHYLATION", PROFILE_METHYLATION, "DNA Methylation");
    addGenomicProfiles(cancer_study.genomic_profiles, "METHYLATION_BINARY", PROFILE_METHYLATION, "DNA Methylation");
    //addGenomicProfiles(cancer_study.genomic_profiles, "PROTEIN_LEVEL", PROFILE_PROTEIN, "Protein Level");
    addGenomicProfiles(cancer_study.genomic_profiles, "PROTEIN_LEVEL", PROFILE_PROTEIN_EXPRESSION, "Protein/phosphoprotein level");


    //  if no genomic profiles available, set message and disable submit button
    if ($("#genomic_profiles").html()==""){
        console.log("cancerStudySelected ( genomicProfilesUnavailable() )");
        genomicProfilesUnavailable();
    }

    //  Update the Case Set Pull-Down Menu
    //  First, clear all existing pull-down menus
    $("#select_case_set").html("");

    //  Iterate through all case sets
    //  Add each case set as an option, and include description as title, so that it appears
    //  as a tool-tip.
    jQuery.each(cancer_study.case_sets,function(i, case_set) {
        $("#select_case_set").append("<option class='case_set_option' value='"
                + case_set.id + "' title='"
                + case_set.description + "'>" + case_set.name + " ("+ case_set.size +")" + "</option>");
    }); //  end for each case study loop

    //  Add the user-defined case list option
    $("#select_case_set").append("<option class='case_set_option' value='-1' "
        + "title='Specify you own case list'>User-defined Case List</option>");
    
    //  Set up Tip-Tip Event Handler for Case Set Pull-Down Menu
    //  commented out for now, as this did not work in Chrome or Safari
    //  $(".case_set_option").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});

    //  Set up Tip-Tip Event Handler for Genomic Profiles help
    $(".profile_help").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});

    //  Set up Event Handler for user selecting a genomic profile radio button
    $("input[type='radio'][name*='genetic_profile_']").click(function(){
        genomicProfileRadioButtonSelected($(this));
    });

    //  Set up an Event Handler for user selecting a genomic profile checkbox
    $("input[type='checkbox'][class*='PROFILE_']").click(function(){
        profileGroupCheckBoxSelected($(this));
    });

    //  Set up an Event Handler for showing/hiding mRNA threshold input
    $("." + PROFILE_MRNA_EXPRESSION).click(function(){
       toggleThresholdPanel($(this), PROFILE_MRNA_EXPRESSION, "#z_score_threshold");
    });

    //  Set up an Event Handler for showing/hiding RPPA threshold input
    $("." + PROFILE_PROTEIN_EXPRESSION).click(function(){
       toggleThresholdPanel($(this), PROFILE_PROTEIN_EXPRESSION, "#rppa_score_threshold");
    });

    // Set default selections and make sure all steps are visible
    console.log("cancerStudySelected ( singleCancerStudySelected() )");
    singleCancerStudySelected();
    console.log("cancerStudySelected ( reviewCurrentSelections() )");
    reviewCurrentSelections();
    // check if cancer study has a clinical_free_form data to filter,
    // if there is data to filter, then enable "build custom case set" link,
    // otherwise disable the button
    /*jQuery.getJSON("ClinicalFreeForm.json",
		{studyId: $("#select_single_study").val()},
		function(json){
			var noDataToFilter = false;
			
			if (json.freeFormData.length == 0)
			{
				noDataToFilter = true;
			}
			else
			{
				noDataToFilter = true;
				
				var categorySet = json.categoryMap;
				
				// check if there is at least one category to filter
		    	for (var category in categorySet)
		    	{
		    		// continue if the category is qualified as a filter parameter
		    		if (isEligibleForFiltering(categorySet[category]))
		    		{
		    			noDataToFilter = false;
		    			break;
		    		}
		    	}
			}
			
			if (noDataToFilter)
			{
				// no clinical_free_form data to filter for the current
	*/			// cancer study, so disable the button
				$("#build_custom_case_set").hide();
	/*		}
			else
			{
				$("#build_custom_case_set").tipTip({defaultPosition: "right",
					delay:"100",
					edgeOffset: 10,
					maxWidth: 100});
				
				$("#build_custom_case_set").hide();//.show(); temporarily disabled build case list
			}
		});*/
}
//  Triggered when a cancer study has been selected, either by the user
//  or programatically.
function cancerStudySelected() {

    //  make sure submit button is enabled unless determined otherwise by lack of data
    $("#main_submit").attr("disabled",false);

    var cancerStudyId = $("#select_single_study").val() || "all";

    if (window.metaDataJson.cancer_studies[cancerStudyId].partial==="true") {
            console.log("cancerStudySelected( loadStudyMetaData )");
	    loadStudyMetaData(cancerStudyId);
    } else {
	    updateCancerStudyInformation();
    }
}

//  Triggered when a case set has been selected, either by the user
//  or programatically.
function caseSetSelected() {
    var caseSetId = $("#select_case_set").val();

    //  If user has selected the user-defined option, show the case list div
    //  Otherwise, make sure to hide it.
    if (caseSetId == "-1") {
        $("#custom_case_list_section").show();
        // if custom case list was selected, post to avoid long url problem.
        $("#main_form").attr("method","post");
    } else {
        $("#custom_case_list_section").hide();
        $("#main_form").attr("method","get");
    }
}

//  Triggered when a gene set has been selected, either by the user
//  or programatically.
function geneSetSelected() {
    //  Get the selected ID from the pull-down menu
    var geneSetId = $("#select_gene_set").val();

    if (window.metaDataJson.gene_sets[geneSetId].gene_list == "") {
        loadGeneList(geneSetId);
    } else {
        //  Get the gene set meta data from global JSON variable
        var gene_set = window.metaDataJson.gene_sets[geneSetId];

        //  Set the gene list text area
        $("#gene_list").val(gene_set.gene_list);
    }
}

//  Adds Meta Data to the Page.
//  Tiggered at the end of successful AJAX/JSON request.
function addMetaDataToPage() {
    console.log("Adding Meta Data to Query Form");
    json = window.metaDataJson;

    // Construct oncotree
    var oncotree = {'tissue':{code:'tissue', studies:[], children:[], parent: false, desc_studies_count:0, tissue:''}};
    var parents = json.parent_type_of_cancers;
    // First add everything to the tree
    for (var tumortype in parents) {
	if (parents.hasOwnProperty(tumortype)) {
		oncotree[tumortype] = {code:tumortype, studies:[], children:[], parent: false, desc_studies_count: 0, tissue: false};
	}
    }
    // Link parents and insert initial tissue info
    for (var tumortype in oncotree) {
	if (oncotree.hasOwnProperty(tumortype) && tumortype !== 'tissue') {
		oncotree[tumortype].parent = oncotree[parents[tumortype]];
		oncotree[tumortype].parent.children.push(oncotree[tumortype]);
		if (parents[tumortype] === "tissue") {
			oncotree[tumortype].tissue = tumortype;
		}
	}
    }
    // Insert tissue information in a "union-find" type way
    for (var elt in oncotree) {
        if (oncotree.hasOwnProperty(elt) && elt !== 'tissue') {
            var to_modify = [];
            var currelt = oncotree[elt];
            while (!currelt.tissue && currelt.code !== 'tissue') {
                to_modify.push(currelt);
                currelt = currelt.parent;
            }
            for (var i=0; i<to_modify.length; i++) {
                to_modify[i].tissue = currelt.tissue;
            }
        }
    }
    // Add studies to tree, and climb up adding one to each level's descendant studies
    // Insert priority studies
    // 
    var priority_study_ids = {};
    for (var i=0; i<window.priority_studies.length; i++) {
	for (var j=0; j<window.priority_studies[i].studies.length; j++) {
	    priority_study_ids[window.priority_studies[i].studies[j]] = true;
	}
    }
    // 
    for (var study in json.cancer_studies) {
	if (priority_study_ids.hasOwnProperty(study)) {
	    continue;
	} else if (study !== 'all') { // don't re-add 'all'
            try {
                var code = json.cancer_studies[study].type_of_cancer.toLowerCase();
		var lineage = [];
		var currCode = code;
		while (currCode !== 'tissue') {
			lineage.push(currCode);
			currCode = oncotree[currCode].parent.code;
		}
                oncotree[code].studies.push({id:study, lineage:lineage});
                var node = oncotree[code];
                while (node) {
                    node.desc_studies_count += 1;
                    node = node.parent;
                }
            } catch (err) {
                console.log("Unable to add study");
                console.log(json.cancer_studies[study]);
            }
        }
    }
    // Sort all the children alphabetically
    for (var node in oncotree) {
        if (oncotree.hasOwnProperty(node)) {
            oncotree[node].children.sort(function(a,b) {
                try {
                    return json.type_of_cancers[a.code].localeCompare(json.type_of_cancers[b.code]);
                } catch(err) {
                    return a.code.localeCompare(b.code);
                }
            });
            oncotree[node].studies.sort(function(a,b) {
                return a.id.localeCompare(b.id);
            });
        }
    }
    var splitAndCapitalize = function(s) {
	    return s.split("_").map(function(x) { return (x.length > 0 ? x[0].toUpperCase()+x.slice(1) : x);}).join(" ");
    };
    var truncateStudyName = function(n) {
	    var maxLength = 80;
	    if (n.length < maxLength) {
		    return n;
	    } else {
		    var suffix = '';
		    var suffixStart = n.lastIndexOf('(');
		    if (suffixStart !== -1) {
			    suffix = n.slice(suffixStart);
		    }
		    var ellipsis = '... ';
		    return n.slice(0,maxLength-suffix.length-ellipsis.length)+ellipsis+suffix;
	    }			    
    };
    window.jstree_root_id = 'tissue';
    var jstree_data = [];
    var flat_jstree_data = [];
    jstree_data.push({'id': jstree_root_id, parent: '#', text: 'All', state: {opened: true}, li_attr: {name: 'All'}});
    flat_jstree_data.push({'id': jstree_root_id, parent: '#', text: 'All', state: {opened: true}, li_attr: {name: 'All'}});
    var node_queue = [].concat(oncotree['tissue'].children);
    var currNode;
    if (window.priority_studies.length > 0) {
	for (var i = 0; i < window.priority_studies.length; i++) {
	    var priority_study_obj = window.priority_studies[i];
	    if (priority_study_obj.studies.filter(function(s) { return json.cancer_studies.hasOwnProperty(s); }).length > 0) {
		var category_id = 'priority-group-' + i;
		jstree_data.push({'id': category_id, 'parent': jstree_root_id, 'text': priority_study_obj.category, 'li_attr': {name: priority_study_obj.category}});
		$.each(priority_study_obj.studies, function (ind, id) {
		    if (json.cancer_studies.hasOwnProperty(id)) {
			studyName = truncateStudyName(json.cancer_studies[id].name);
			numSamplesInStudy = json.cancer_studies[id].num_samples;
			if (numSamplesInStudy == 1) {
			    samplePlurality = 'sample';
			} else if (numSamplesInStudy > 1) {
			    samplePlurality = 'samples';
			} else {
			    samplePlurality = '';
			    numSamplesInStudy = '';
			}
			jstree_data.push({'id': id, 'parent': category_id, 'text': studyName.concat('<span style="font-weight:normal;font-style:italic;"> ' + numSamplesInStudy + ' ' + samplePlurality + '</span>'),
			    'li_attr': {name: studyName, description: metaDataJson.cancer_studies[id].description, search_terms: priority_study_obj.category}});

			flat_jstree_data.push({'id': id, 'parent': jstree_root_id, 'text': truncateStudyName(json.cancer_studies[id].name),
			    'li_attr': {name: studyName, description: metaDataJson.cancer_studies[id].description, search_terms: priority_study_obj.category}});
		    }
		});
	    }
	}
    }
    
    while (node_queue.length > 0) {
	    currNode = node_queue.shift();
	    if (currNode.desc_studies_count > 0) {
		var name = splitAndCapitalize(metaDataJson.type_of_cancers[currNode.code] || currNode.code);
		jstree_data.push({'id':currNode.code, 
			'parent':((currNode.parent && currNode.parent.code) || '#'), 
			'text':name,
			'li_attr':{name:name}
		});
		var numSamplesInStudy;
		var samplePlurality;
		$.each(currNode.studies, function(ind, elt) {
			    name = truncateStudyName(splitAndCapitalize(metaDataJson.cancer_studies[elt.id].name));
		            numSamplesInStudy = json.cancer_studies[elt.id].num_samples;
			    if (numSamplesInStudy == 1) {
			        samplePlurality = 'sample';
			    }
			    else if (numSamplesInStudy > 1) {
				samplePlurality = 'samples';
			    }
			    else {
				samplePlurality = '';
				numSamplesInStudy = '';
			    }
			    jstree_data.push({'id':elt.id, 
				    'parent':currNode.code, 
				    'text':name.concat('<span style="font-weight:normal;font-style:italic;"> '+ numSamplesInStudy + ' ' + samplePlurality + '</span>'),
				    'li_attr':{name: name, description:metaDataJson.cancer_studies[elt.id].description}});
			    
			    flat_jstree_data.push({'id':elt.id, 
				    'parent':jstree_root_id,
				    'text':name,
				    'li_attr':{name: name, description:metaDataJson.cancer_studies[elt.id].description, search_terms: elt.lineage.join(" ")}});
		});
		node_queue = node_queue.concat(currNode.children);
	    }
    }
    var precomputed_search = {query: '', results: {}};
    var parse_search_query = function(query) {
		// First eliminate trailing whitespace and reduce every whitespace
		//	to a single space.
		query = query.toLowerCase().trim().split(/\s+/g).join(' ');
		// Now factor out quotation marks and inter-token spaces
		var phrases = [];
		var currInd = 0;
		var nextSpace, nextQuote;
		while (currInd < query.length) {
			if (query[currInd] === '"') {
				nextQuote = query.indexOf('"', currInd+1);
				if (nextQuote === -1) {
					phrases.push(query.substring(currInd + 1));
					currInd = query.length;
				} else {
					phrases.push(query.substring(currInd + 1, nextQuote));
					currInd = nextQuote + 1;
				}
			} else if (query[currInd] === ' ') {
				currInd += 1;
			} else if (query[currInd] === '-') {
				phrases.push('-');
				currInd += 1;
			} else {
				nextSpace = query.indexOf(' ', currInd);
				if (nextSpace === -1) {
					phrases.push(query.substring(currInd));
					currInd = query.length;
				} else {
					phrases.push(query.substring(currInd, nextSpace));
					currInd = nextSpace + 1;
				}
			}
		}
		// Now get the conjunctive clauses, and the negative clauses
		var clauses = [];
		currInd = 0;
		var nextOr, nextDash;
		while (currInd < phrases.length) {
			if (phrases[currInd] === '-') {
				if (currInd < phrases.length - 1) {
					clauses.push({"type":"not", "data":phrases[currInd+1]});
				}
				currInd = currInd + 2;
			} else {
				nextOr = phrases.indexOf('or', currInd);
				nextDash = phrases.indexOf('-', currInd);
				if (nextOr === -1 && nextDash === -1) {
					clauses.push({"type":"and","data":phrases.slice(currInd)});
					currInd = phrases.length;
				} else if (nextOr === -1 && nextDash > 0) {
					clauses.push({"type":"and", "data":phrases.slice(currInd, nextDash)});
					currInd = nextDash;
				} else if (nextOr > 0 && nextDash === -1) {
					clauses.push({"type":"and", "data":phrases.slice(currInd, nextOr)});
					currInd = nextOr + 1;
				} else {
					if (nextOr < nextDash) {
						clauses.push({"type":"and", "data":phrases.slice(currInd, nextOr)});
						currInd = nextOr + 1;
					} else {
						clauses.push({"type":"and", "data":phrases.slice(currInd, nextDash)});
						currInd = nextDash;
					}
				}
			}
		}	
		return clauses;
    };
    var matchPhrase = function(phrase, node) {
	    phrase = phrase.toLowerCase();
		return !!((node.li_attr && node.li_attr.name && node.li_attr.name.toLowerCase().indexOf(phrase) > -1) 
			    || (node.li_attr && node.li_attr.description && node.li_attr.description.toLowerCase().indexOf(phrase) > -1)
			    || (node.li_attr && node.li_attr.search_terms && node.li_attr.search_terms.toLowerCase().indexOf(phrase) > -1));
	};
    var perform_search_single = function(parsed_query, node) {
	    // in: a jstree node
	    // text to search is node.text and node.li_attr.description and node.li_attr.search_terms
	    // return true iff the query, considering quotation marks, 'and' and 'or' logic, matches
	    var match = false;
	    var hasPositiveClauseType = false;
	    var forced = false;
	    
	    $.each(parsed_query, function(ind, clause) {
		    if (clause.type !== 'not') {
			    hasPositiveClauseType = true;
			    return 0;
		    }
	    });
	    if (!hasPositiveClauseType) {
		    // if only negative clauses, match by default
		    match = true;
	    }
	    $.each(parsed_query, function(ind, clause) {
		    if (clause.type === 'not') {
			    if (matchPhrase(clause.data, node)) {
				    match = false;
				    forced = true;
				    return 0;
			    }
		    } else if (clause.type === 'and') {
			    hasPositiveClauseType = true;
			    var clauseMatch = true;
			    $.each(clause.data, function(ind2, phrase) {
				    clauseMatch = clauseMatch && matchPhrase(phrase, node);
			    });
			    match = match || clauseMatch;
		    }
	    });
	    return {result:match, forced:forced};
    };
    var perform_search = function(query) {
	    // IN: query, a string
	    // void method
	    // when this ends, the object precomputed_search has been updated
	    //	so that results[node.id] = true iff the query directly matches it
	    var parsed_query = parse_search_query(query);
	    $.each($('#jstree').jstree(true)._model.data, function(key, node) {
		    precomputed_search.results[node.id] = perform_search_single(parsed_query, node);
	    });
	    precomputed_search.query = query;
    };
    var jstree_search = function(query, node) {
	    if (query === "") {
		    return true;
	    }
	    if (precomputed_search.query !== query) {
		    perform_search(query);
	    }
	    if (!precomputed_search.results[node.id].result && precomputed_search.results[node.id].forced) {
		    return false;
	    }
	    var nodes_to_consider = [node.id].concat(node.parents.slice());
	    var ret = false;
	    $.each(nodes_to_consider, function(ind, elt) {
		    if (elt === jstree_root_id || elt === '#') {
			    return 0;
		    }
		    if (!precomputed_search.results[elt].result && precomputed_search.results[elt].forced) {
			    ret = false;
			    return 0;
		    }
		    ret = ret || precomputed_search.results[elt].result;
	    });
	    return ret;
    };
	var initialize_jstree = function (data) {
		console.log("Initializing jstree");
		$("#jstree").jstree({
			"themes": {
				"theme": "default",
				"dots": false,
				"icons": false,
				"url": "../../css/jstree.style.css"
			},
			"plugins": ['checkbox','search'],
			"search": {'show_only_matches': true,
				'search_callback': jstree_search,
				'search_leaves_only': true},
			"checkbox": {},
			'core': {'data': data, 'check_callback': true, 'dblclick_toggle': false, 'multiple': (window.tab_index !== "tab_download")}
		});
		$('#jstree').on('ready.jstree', function () {
			$('#jstree').jstree(true).num_leaves = $('#jstree').jstree(true).get_leaves().length;
			$('#jstree').jstree(true).get_matching_nodes = function (phrase) {
				var ret = [];
				$.each($('#jstree').jstree(true)._model.data, function (key, node) {
					if (matchPhrase(phrase, node)) {
						ret.push(key);
					}
				});
				return ret;
			};
			
			$('#jstree').jstree(true).get_node(jstree_root_id, true).children('.jstree-anchor').after($jstree_flatten_btn());
			$('#jstree').jstree(true).open_all();
		});
		$('#jstree').on('changed.jstree', function() { onJSTreeChange(); /*saveSelectedStudiesLocalStorage();*/ });
		$('#jstree').jstree(true).hide_icons();
	}	
initialize_jstree(window.tab_index === "tab_download" ? flat_jstree_data : jstree_data);
	var jstree_is_flat = false;
	var $jstree_flatten_btn = (function() {
		if (window.tab_index === "tab_download") {
			return false;
		}
		var ret = $('<i class="fa fa-lg fa-code-fork jstree-external-node-decorator" style="display:none; cursor:pointer; padding-left:0.6em"></i>');
		ret.mouseenter(function () {
			ret.fadeTo('fast', 0.7);
		});
		ret.mouseleave(function () {
			ret.fadeTo('fast', 1);
		});
		ret.click(function () {
			var selected_studies = $("#jstree").jstree(true).get_selected_leaves();
			$('#jstree').jstree(true).destroy();
			jstree_is_flat = !jstree_is_flat;
			initialize_jstree((jstree_is_flat ? flat_jstree_data : jstree_data));
			$('#jstree').on('ready.jstree', function () {
				$('#jstree').jstree(true).select_node(selected_studies);
				if ($("#jstree_search_input").val() !== "") {
					precomputed_search.query = false; // force re-search
					do_jstree_search();
				}
			});
		});
		ret.qtip({
			content: {text: (jstree_is_flat ? "Unflatten tree" : "Flatten tree")},
			style: {classes: 'qtip-light qtip-rounded'},
			position: {my: 'bottom center', at: 'top center', viewport: $(window)},
			show: {delay: 600},
			hide: {delay: 10, fixed: true},
		});
		return ret;
	});
	
	var do_jstree_search = function() {
		$("#jstree").jstree(true).search($("#jstree_search_input").val());
		$('#jstree').jstree(true).get_node(jstree_root_id, true).children('.jstree-anchor').after($jstree_flatten_btn());
	}
	var jstree_search_timeout = null;
	$("#jstree_search_input").on('input', function () {
		if (jstree_search_timeout) {
			clearTimeout(jstree_search_timeout);
		}
		jstree_search_timeout = setTimeout(function () {
			if ($("#jstree_search_input").val() === "") {
                                $("#step_header_first_line_empty_search").css("display", "none");
				$("#jstree").jstree(true)._model.data['tissue'].li_attr.name = "All";
			} else {
                                $("#step_header_first_line_empty_search").css("display", "block");
				$("#jstree").jstree(true)._model.data['tissue'].li_attr.name = "All Search Results";
			}
			$("#jstree").fadeTo(100, 0.5, function () {
				$('#jstree_search_none_found_msg').hide();
				do_jstree_search();
				if ($('#jstree_search_input').val() !== "" && $('#jstree').jstree(true)._data.search.res.length === 0) {
					$('#jstree_search_none_found_msg').show();
				}
				$("#jstree").fadeTo(100, 1);
			});
		}, 400); // wait for a bit with no typing before searching
	});
        $('#step_header_first_line_empty_search').click(function() { 
            $("#jstree_search_input").val("");
            $("#step_header_first_line_empty_search").css("display", "none");
            $("#jstree").fadeTo(100, 0.5, function () {
                do_jstree_search();
                $("#jstree").fadeTo(100, 1);
            });
        });
	var saveSelectedStudiesLocalStorage = function() {
		if (!supportsHTML5Storage()) {
			return false;
		}
		var selected_studies = $("#jstree").jstree(true).get_selected_leaves();
		// selectedStudiesStorageKey
		window.localStorage.setItem(selectedStudiesStorageKey, selected_studies.join(","));
		//$.removeCookie(selectedStudiesCookieName);
		//$.cookie(selectedStudiesCookieName, selected_studies.join(","), {expires:10});
		return true;
	};
	var getSelectedStudiesLocalStorage = function() {
		if (!supportsHTML5Storage()) {
			return false;
		}
		return window.localStorage.getItem(selectedStudiesStorageKey);
	}
	var onJSTreeChange = function () {
		$(".error_box").remove();
		var select_single_study = $("#main_form").find("#select_single_study");
		var select_multiple_studies = $("#main_form").find("#select_multiple_studies");
		var selected_studies = $("#jstree").jstree(true).get_selected_leaves();
		var selected_ct = selected_studies.length;
		$('#jstree_selected_study_count').html((selected_ct === 0 ? "No" : (selected_ct === $('#jstree').jstree(true).num_leaves ? "All" : selected_ct)) + " stud" + (selected_ct === 1 ? "y" : "ies") + " selected.");
		$('#jstree_deselect_all_btn')[selected_ct > 0 ? 'show' : 'hide']();
		var old_select_single_study_val = select_single_study.val();
		if (selected_studies.length === 1) {
			select_single_study.val(selected_studies[0]);
		} else {
			select_single_study.val("all");
		}
		select_multiple_studies.val(selected_studies.join(","));
		if (select_single_study.val() !== old_select_single_study_val) {
			select_single_study.trigger('change');
		}
		select_multiple_studies.trigger('change');
	};
	

    //  Add Gene Sets to Pull-down Menu
    jQuery.each(json.gene_sets,function(key,gene_set){
        $("#select_gene_set").append("<option value='" + key + "'>"
                + gene_set.name + "</option>");
    });  //  end for each gene set loop

    // Set the placeholder for the autocomplete select box
    $("#example_gene_set").children("span:first").children("input:first")
        .attr("placeholder", $("#select_gene_set").children("option:first").text());

    //  Set things up, based on currently selected cancer type
    // hacky; order of preference
    var selected_study_map = {};
    if (!window.cancer_study_list_selected || window.cancer_study_list_selected === '') {
	var windowParams = window.location.search.substring(1).split("&");
	$.each(windowParams, function(ind, elt) {
		var pair = elt.split("=");
		if (pair[0] === window.cancer_study_list_param) {
			window.cancer_study_list_selected = pair[1];
			return 0;
		}
	});
    }
    if (!window.cancer_study_list_selected || window.cancer_study_list_selected === '') {
	    var split_url_on_hash = window.location.href.split('#');
	    if (split_url_on_hash.length > 1) {
		window.cancer_study_list_selected = split_url_on_hash[1].split("/")[4];
		}
    }
    if (!window.cancer_study_list_selected || window.cancer_study_list_selected === '') {
	var windowParams = window.location.search.substring(1).split("&");
	$.each(windowParams, function(ind, elt) {
		var pair = elt.split("=");
		if (pair[0] === 'cancer_study_id') {
			window.cancer_study_list_selected = pair[1];
			return 0;
		}
	});
    }
	    
    //var selected_study_list = decodeURIComponent(window.selected_cancer_study_list || ( getSelectedStudiesLocalStorage() || '')).split(",");
    var selected_study_list = (window.tab_index !== "tab_download" ? decodeURIComponent(window.cancer_study_list_selected || '').split(",") : []);
	    $.each(selected_study_list, function(ind, elt) {
		    if (elt !== '') {
			selected_study_map[elt] = false;
		}
	    });
    if (window.cancer_study_id_selected !== 'all') {
	    selected_study_map[window.cancer_study_id_selected] = false;
    }
    jQuery.each(json.cancer_studies,function(key,cancer_study){
        // Set Selected Cancer Type, Based on User Parameter
        if (selected_study_map.hasOwnProperty(key)) {
		selected_study_map[key] = true;
	}
    });
	$("#jstree").on('ready.jstree', function() {
		// Chosenize the select boxes
		var minSearchableItems = 10;
		$("#select_gene_set").chosen({ width: '620px', search_contains: true});
		$("#select_case_set").chosen({ width: '420px', disable_search_threshold: minSearchableItems, search_contains: true });
		// add a title to the text input fields generated by Chosen for
		// Section 508 accessibility compliance
		$("div.chzn-search > input:first-child").attr("title", "Search");
		$.each(selected_study_map, function(key, val) {
			if (val) {
				$("#jstree").jstree(true).select_node(key, true);
			}
		});
			//   Set things up, based on currently selected case set id
		$('#select_single_study').one('doneChanging', function() {
			if (window.case_set_id_selected != null && window.case_set_id_selected != "") {
				$("#select_case_set").val(window.case_set_id_selected);
				$("#select_case_set").trigger('liszt:updated');
				caseSetSelectionOverriddenByUser = true;
			}
			caseSetSelected();
			if (window.case_ids_selected !== '') {
				$('#custom_case_set_ids').val(window.case_ids_selected);
			}

			//  Set things up, based on currently selected gene set id
			if (window.gene_set_id_selected != null && window.gene_set_id_selected != "") {
				$("#select_gene_set").val(window.gene_set_id_selected);
			} else {
				$("#select_gene_set").val("user-defined-list");
			}
			$("#select_gene_set").trigger('liszt:updated');
			//  Set things up, based on all currently selected genomic profiles

			//  To do so, we iterate through all input elements with the name = 'genetic_profile_ids*'
			$("input[name^=genetic_profile_ids]").each(function (index) {
				//  val() is the value that or stable ID of the genetic profile ID
				var currentValue = $(this).val();

				//  if the user has this stable ID already selected, mark it as checked
				if (window.genomic_profile_id_selected[currentValue] == 1) {
					console.log("Checking " + $(this).attr('id') + "... (inside addMetaDataToPage())");
					$(this).prop('checked', true);
					//  Select the surrounding checkbox
					genomicProfileRadioButtonSelected($(this));
				}
			});  //  end for each genomic profile option


			// HACK TO DEAL WITH ASYNCHRONOUS STUFF
			window.metaDataAdded = true;
			// determine whether any selections have already been made
			// to make sure all of the fields are shown/hidden as appropriate
			console.log("addMetaDataToPage ( reviewCurrentSelections() )");
			reviewCurrentSelections();
		});
		onJSTreeChange();
	});
}

// Adds the specified genomic profiles to the page.
// Code checks for three possibilities:
// 1.  0 profiles of targetType --> show nothing
// 2.  1 profile of targetType --> show as checkbox
// 3.  >1 profiles of targetType --> show group checkbox plus radio buttons
function addGenomicProfiles (genomic_profiles, targetAlterationType, targetClass, targetTitle) {
    var numProfiles = 0;
    var profileHtml = "";
    var downloadTab = false;

    //  Determine whether we are in the download tab
    if (window.tab_index == "tab_download") {
        downloadTab = true;
    }

    //  First count how many profiles match the targetAltertion type
    jQuery.each(genomic_profiles,function(key, genomic_profile) {
        if (genomic_profile.alteration_type == targetAlterationType) {
            if (downloadTab || genomic_profile.show_in_analysis_tab == true) {
                numProfiles++;
            }
        }
    }); //  end for each genomic profile loop

    if (numProfiles == 0) {
        return;
    } else if(numProfiles >1 && downloadTab == false) {
        // enable submit button
        $('#main_submit').attr('disabled', false);
        //  If we have more than 1 profile, output group checkbox
        //  assign a class to associate the checkbox with any subgroups (radio buttons)
        profileHtml += "<div class='checkbox'><label>"
            + "<input type='checkbox' class='" + targetClass + "'>"
            + targetTitle + " data."
            + " Select one of the profiles below:"
            + "</label></div>";
        profileHtml += "<div class='genomic_profiles_subgroup'>";
    }

    //  Iterate through all genomic profiles
    jQuery.each(genomic_profiles,function(key, genomic_profile) {

        if (genomic_profile.alteration_type == targetAlterationType) {
            if (downloadTab || genomic_profile.show_in_analysis_tab == true) {
                //  Branch depending on number of profiles
                var optionType = "checkbox";
                if (downloadTab) {
                    optionType = "radio";
                } else {
                    if (numProfiles == 1) {
                        optionType = "checkbox";
                    } else if (numProfiles > 1) {
                        optionType = "radio";
                    }
                }
                profileHtml += outputGenomicProfileOption (downloadTab, optionType,
                        targetClass, genomic_profile.id, genomic_profile.name, genomic_profile.description);
            }
        }
    }); //  end for each genomic profile loop

    if(numProfiles >1) {
        //  If we have more than 1 profile, output the end div tag
        profileHtml += "</div>";
    }

    if(targetClass == PROFILE_MRNA_EXPRESSION && downloadTab == false){
        var inputName = 'Z_SCORE_THRESHOLD';
        profileHtml += "<div id='z_score_threshold' class='score_threshold'>"
        + "<label>Enter a z-score threshold &#177: "
        + "<input type='text' name='" + inputName + "' size='6' value='"
                + window.zscore_threshold + "'>"
        + "</label></div>";
    }

    if(targetClass == PROFILE_PROTEIN_EXPRESSION && downloadTab == false){
        var inputName = 'RPPA_SCORE_THRESHOLD';
        profileHtml += "<div id='rppa_score_threshold' class='score_threshold'>"
        + "<label>Enter a z-score threshold &#177: "
        + "<input type='text' name='" + inputName + "' size='6' value='"
                + window.rppa_score_threshold + "'>"
        + "</label></div>";
    }
    
    $("#genomic_profiles").append(profileHtml);
}

// Outputs a Single Genomic Profile Options
function outputGenomicProfileOption (downloadTab, optionType, targetClass, id, name,
                     description) {
    //  This following if/else requires some explanation.
    //  If we are in the download tab, all the input fields must use the same name.
    //  This enforces all inputs to work as a single group of radio buttons.
    //  If we are in the query tab, the input fields must be specified by alteration type.
    //  This enforces all the inputs of the same alteration type to work as a single group of radio
    //  buttons.
    var paramName;
    if (downloadTab) {
        paramName =  "genetic_profile_ids";
    } else {
        paramName = "genetic_profile_ids_" + targetClass;
    }

    var html =  "<div class='" + optionType + "'>"
        + "<label>"
        + "<input type='" + optionType + "'"
        + " id='" + id + "'"
        + " name='" + paramName + "'"
        + " class='" + targetClass + "'"
        + " value='" + id + "' />"
        + name
        + "  <img class='profile_help' alt='help' src='images/help.png' title='"
        + description + "'></label></div>";
    return html;
}

