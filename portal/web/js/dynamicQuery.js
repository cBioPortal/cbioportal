/******************************************************************************************
* Dynamic Query Javascript, built with JQuery
* @author Ethan Cerami
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

//  Triggered only when document is ready.
$(document).ready(function(){

    //  Get Portal JSON Meta Data via JQuery AJAX
    jQuery.getJSON("portal_meta_data.json",function(json){
        //  Store JSON Data in global variable for later use
        window.metaDataJson = json;

        //  Add Meta Data to current page
        addMetaDataToPage();
    });  //  end getJSON function

     //  Set up Event Handler for User Selecting Cancer Study from Pull-Down Menu
     $("#select_cancer_type").change(function() {
         cancerStudySelected();
     });

    // Set up Event Handler for User Selecting a Case Set
    $("#select_case_set").change(function() {
        caseSetSelected();
    });

    // Set up Event Handler for User Selecting a Get Set
    $("#select_gene_set").change(function() {
        geneSetSelected();
    });

    //  Set up Event Handler for View/Hide JSON Debug Information
    $('#json_cancer_studies').click(function(event) {
      event.preventDefault();
      $('#cancer_results').toggle();
    });
});  //  end document ready function

//  Triggered when a cancer study has been selected, either by the user
//  or programatically.
function cancerStudySelected() {
    var cancerStudyId = $("#select_cancer_type").val();
    var cancer_study = window.metaDataJson.cancer_studies[cancerStudyId];

    //  Update Cancer Study Description
    $("#cancer_study_desc").html("<p> " + cancer_study.description + "</p>");

    //  Iterate through all genomic profiles
    //  Add all non-expression profiles where show_in_analysis_tab = true
    //  First, clear all existing options
    $("#genomic_profiles").html("");

    //  Add Genomic Profiles, in this order
    addGenomicProfiles(cancer_study.genomic_profiles, "MUTATION", "Mutation");
    addGenomicProfiles(cancer_study.genomic_profiles, "MUTATION_EXTENDED", "Mutation");
    addGenomicProfiles(cancer_study.genomic_profiles, "COPY_NUMBER_ALTERATION", "Copy Number");
    addGenomicProfiles(cancer_study.genomic_profiles, "MRNA_EXPRESSION", "mRNA Expression");

    //  Update the Case Set Pull-Down Menu
    //  First, clear all existing pull-down menus
    $("#select_case_set").html("");

    //  Iterate through all case sets
    //  Add each case set as an option, and include description as title, so that it appears
    //  as a tool-tip.
    jQuery.each(cancer_study.case_sets,function(i, case_set) {
        $("#select_case_set").append("<option class='case_set_option' value='"
                + case_set.id + "' title='"
                + case_set.description + "'>" + case_set.name + "</option>");
    }); //  end for each case study loop

    //  Add the user-defined case list option
    $("#select_case_set").append("<option class='case_set_option' value='-1' "
        + "title='Specify you own case list'>User-defined Case List</option>");

    //  Set up Tip-Tip Event Handler for Case Set Pull-Down Menu
    $(".case_set_option").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});
}

//  Triggered when a case set has been selected, either by the user
//  or programatically.
function caseSetSelected() {
    var caseSetId = $("#select_case_set").val();

    //  If user has selected the user-defined option, show the case list div
    //  Otherwise, make sure to hide it.
    if (caseSetId == "-1") {
        $("#custom_case_list_section").show();
    } else {
        $("#custom_case_list_section").hide();
    }
}

//  Triggered when a gene set has been selected, either by the user
//  or programatically.
function geneSetSelected() {
    //  Get the selected ID from the pull-down menu
    var geneSetId = $("#select_gene_set").val();

    //  Get the gene set meta data from global JSON variable
    var gene_set = window.metaDataJson.gene_sets[geneSetId];

    //  Set the gene list text area
    $("#gene_list").html(gene_set.gene_list);
}

//  Adds Meta Data to the Page.
//  Tiggered at the end of successful AJAX/JSON request.
function addMetaDataToPage() {
    //  Iterate through all cancer studies
    json = window.metaDataJson;
    jQuery.each(json.cancer_studies,function(key,cancer_study){
        $("#cancer_results").append('<h1>Cancer Study:  ' + cancer_study.name + '</h1>');

        //  Append to Cancer Study Pull-Down Menu
        $("#select_cancer_type").append("<option value='" + key + "'>" + cancer_study.name + "</option>");

        $("#cancer_results").append('<p>' + cancer_study.description + '</p>');
        $("#cancer_results").append('<h2>Genomic Profiles:' + '</h2>');
        $("#cancer_results").append('<ul>');
        jQuery.each(cancer_study.genomic_profiles,function(i, genomic_profile) {
            $("#cancer_results").append('<li>' + genomic_profile.name + ': ' + genomic_profile.description + "</li>'");
        }); //  end for each genomic profile loop

        $("#cancer_results").append('</ul>');
        $("#cancer_results").append('<h2>Case Sets:' + '</h2>');
        $("#cancer_results").append('<ul>');
        jQuery.each(cancer_study.case_sets,function(i, case_set) {
            $("#cancer_results").append('<li>' + case_set.name + ': ' + case_set.description + "</li>'");
        }); //  end for each genomic profile loop
        
        $("#cancer_results").append('</ul>');
    });  //  end 1st for each cancer study loop

    //  Add Gene Sets to Pull-down Menu
    jQuery.each(json.gene_sets,function(key,gene_set){
        $("#select_gene_set").append("<option value='" + key + "'>"
                + gene_set.name + "</option>");
    });  //  end for each gene set loop

    //  Set things up, based on currently selected cancer type
    jQuery.each(json.cancer_studies,function(key,cancer_study){
        // Set Selected Cancer Type, Based on User Parameter
        if (key == window.cancer_study_id_selected) {
            $("#select_cancer_type").val(key);
            cancerStudySelected();
        }
    });  //  end 2nd for each cancer study loop

    //   Set things up, based on currently selected case set id
    if (window.case_set_id_selected != null && window.case_set_id_selected != "") {
        $("#select_case_set").val(window.case_set_id_selected);
    }
    caseSetSelected();

    //  Set things up, based on currently selected gene set id
    if (window.gene_set_id_selected != null && window.gene_set_id_selected != "") {
        $("#select_gene_set").val(window.gene_set_id_selected);    
    }
}

// Adds the specified genomic profiles to the page.
// Code checks for three possibilities:
// 1.  0 profiles of targetType --> show nothing
// 2.  1 profile of targetType --> show as checkbox
// 3.  >1 profiles of targetType --> show group checkbox plus radio buttons
function addGenomicProfiles (genomic_profiles, targetAlterationType, targetTitle) {
    var numProfiles = 0;
    var profileHtml = "";

    //  First count how many profiles match the targetAltertion type
    jQuery.each(genomic_profiles,function(key, genomic_profile) {
        if (genomic_profile.show_in_analysis_tab == true
                && genomic_profile.alteration_type == targetAlterationType) {
            numProfiles++;
        }
    }); //  end for each genomic profile loop

    if (numProfiles ==0) {
        return;
    } else if(numProfiles >1) {
        //  If we have more than 1 profile, output group checkbox
        profileHtml += "<input type='checkbox'>" + targetTitle + " data."
            + " Select one of the profiles below:";
        profileHtml += "<div class='genomic_profiles_subgroup'>";
    }

    //  First count how many profiles match the targetAltertion type
    jQuery.each(genomic_profiles,function(key, genomic_profile) {
        if (genomic_profile.show_in_analysis_tab == true
                && genomic_profile.alteration_type == targetAlterationType) {
            //  Branch depending on number of profiles
            if (numProfiles == 1) {
                profileHtml += "<input type='checkbox' name='genetic_profile_ids' "
                    + "value='" + genomic_profile.id +"'>" + genomic_profile.name + "</input><br/>";
            } else if (numProfiles > 1) {
                profileHtml += "<input type='radio' name='genetic_profile_ids' "
                    + "value='" + genomic_profile.id +"'>" + genomic_profile.name + "</input><br/>";
            }
        }
    }); //  end for each genomic profile loop

    if(numProfiles >1) {
        //  If we have more than 1 profile, output the end div tag
        profileHtml += "</div>";
    }

    $("#genomic_profiles").append(profileHtml);
}