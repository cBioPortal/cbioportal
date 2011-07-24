/******************************************************************************************
* Dynamic Query Javascript, built with JQuery
* @author Ethan Cerami
*
* This code performs the following functions:
*
* 1.  Connects to the portal via AJAX and downloads a JSON document containing information
*     regarding all cancer studies stored in the CGDS.
* 2.  Creates event handlers for when user selects a cancer study.  This triggers updates
      in the genomic profiles and case lists displayed.
******************************************************************************************/

//  Triggered only when document is ready.
$(document).ready(function(){
    //  Get Cancer Studies JSON Data via JQuery AJAX
    jQuery.getJSON("cancer_studies.json",function(json){
        //  Store JSON Data in global variable for later use
        window.cancerStudyJson = json;

        //  Add Cancer Studies to current page
        addCancerStudiesToPage();
    });  //  end getJSON function

     //  Set up Event Handler for User Selecting Cancer Study from Pull-Down Menu
     $("#select_cancer_type").change(function() {
         cancerStudySelected();
     });

    // Set up Event Handler for User Selecting a Case Set
    $("#select_case_set").change(function() {
        caseSetSelected();
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
    var cancer_study = cancerStudyJson[cancerStudyId];

    //  Update Cancer Study Description
    $("#cancer_study_desc").html("<p> " + cancer_study.description + "</p>");

    //  Iterate through all genomic profiles
    //  Add all non-expression profiles where show_in_analysis_tab = true
    //  First, clear all existing options
    $("#genomic_profiles").html("");

    //  Then iterate through all genomic profiles
    jQuery.each(cancer_study.genomic_profiles,function(key, genomic_profile) {
        if (genomic_profile.show_in_analysis_tab == true
                && genomic_profile.alteration_type != "MRNA_EXPRESSION") {
            $("#genomic_profiles").append("<input type='checkbox' name='genetic_profile_ids' "
                + "value='" + genomic_profile.id +"'>" + genomic_profile.name + "</input><br/>");
        }
    }); //  end for each genomic profile loop

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
    $("#select_case_set").append("<option class='case_set_option' value='user_defined' "
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
    if (caseSetId == "user_defined") {
        $("#custom_case_list_section").show();
    } else {
        $("#custom_case_list_section").hide();
    }
}

//  Adds Cancer Studies to the Page.
//  Tiggered at the end of successful AJAX/JSON request.
function addCancerStudiesToPage() {
    //  Iterate through all cancer studies
    json = window.cancerStudyJson;
    jQuery.each(json,function(key,cancer_study){
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

    //  Now set things...
    jQuery.each(json,function(key,cancer_study){
        // Set Selected Cancer Type, Based on User Parameter
        if (key == window.cancer_study_id_selected) {
            $("#select_cancer_type").val(key);
            cancerStudySelected();
        }
    });  //  end 2nd for each cancer study loop
}