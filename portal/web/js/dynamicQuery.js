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

     //  Load Portal JSON Meta Data while showing loader image in place of query form
     loadMetaData();

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
    $("#json_cancer_studies").click(function(event) {
      event.preventDefault();
      $('#cancer_results').toggle();
    });

    //  Set up Event Handler for View/Hide Query Form, when it is on the results page
    $("#toggle_query_form").click(function(event) {
      event.preventDefault();
      $('#query_form_on_results_page').toggle();

      //  Toggle the icons
      $(".query-toggle").toggle();
    });

    //  Set up an Event Handler to intercept form submission
    $("#main_form").submit(function() {
       chooseAction();
    });

    //  Set up an Event Handler for the Query / Data Download Tabs
    $("#query_tab").click(function(event) {
       event.preventDefault();
        userClickedMainTab("tab_visualize")
    });
    $("#download_tab").click(function(event) {
       event.preventDefault();
       userClickedMainTab("tab_download");
    });

    //  set toggle Step 5: Optional arguments
    $("#optional_args").hide();
    /*$("#step5_toggle").click(function(event) {
        event.preventDefault();
        $("#optional_args").toggle( "blind" );
    });*/

    $('#step5 > .step_header').click(function(){
         $(".ui-icon", this).toggle();
         $("#optional_args").toggle();
    });

    // unset cookie for results tabs, so that a new query
    // always goes to summary tab first
    $.cookie("results-tab",null);

});  //  end document ready function


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
        jQuery.getJSON("portal_meta_data.json",function(json){
            //  Store JSON Data in global variable for later use
            window.metaDataJson = json;

            //  Add Meta Data to current page
            addMetaDataToPage();
            showNewContent();
        });
    }
    function showNewContent() {
        //show content, hide loader only after content is shown
        $('#main_query_form').fadeIn('fast',hideLoader());
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

    //  Change hidden field value
    $("#tab_index").val(tabAction);

    //  Then, submit the form
    $("#main_form").submit();
}

//  When the page is first loaded, the default query will be a cross-cancer type
//  search in which the user will enter ONLY a gene list; Also when "All Cancer Studies"
//  is selected in Step 1
function crossCancerStudySelected() {
     $('#step2').hide();
     $('#step3').hide();
     $('#step5').hide();
     $('#cancer_study_desc').hide();
}

//  Display extra steps when an individual cancer study is selected
function singleCancerStudySelected() {
     $("#step2").show();
     $("#step3").show();
     $("#step5").show();
     $("#cancer_study_desc").show();
}

//  Determine whether to submit a cross-cancer query or
//  a study-specific query
function chooseAction() {
    if ($("#select_cancer_type").val() == 'all'){
       $("#main_form").get(0).setAttribute('action','cross_cancer.do');
    } else {
       $("#main_form").get(0).setAttribute('action','index.do');
    }
}

//  Determine which radio button was clicked and
// automatically select the corresponding checkbox
function selectCheckbox(subgroupClicked) {
    var subgroupClass = subgroupClicked.attr('class');
    if (subgroupClass != undefined && subgroupClass != "") {
        var checkboxSelector = "input."+subgroupClass+"[type=checkbox]";
        if (checkboxSelector != undefined) {
            $(checkboxSelector).attr('checked',true);
        }
    }
}

//  Triggered when a genomic profile is unselected;
//  make sure subrgoups are also unselected
function unselectAllSubgroups(profileUnselected) {
    var profileClass = profileUnselected.attr('class');
    var radioSelector = "input."+profileClass+"[type=radio]";
    $(radioSelector).attr('checked',false);
}

// Show or hide mRNA threshold field based on mRNA profile selected
function toggle_threshold(profileClicked) {
    var selectedProfile = profileClicked.val();
    var inputType = profileClicked.attr('type');

    // when a radio button is clicked, show threshold input unless user chooses expression outliers
    if(inputType == 'radio'){
        if(selectedProfile.indexOf("outlier")==-1){
            $("#z_score_threshold").slideDown();
        } else {
            $("#z_score_threshold").slideUp();
        }
    } else if(inputType == 'checkbox'){
        var subgroup = $("input.MRNA_EXPRESSION[type=radio]");
        // if there are NO subgroups, show threshold input when mRNA checkbox is selected.
        // if there ARE subgroups, do nothing when checkbox is selected. Wait until subgroup is chosen.
        if (profileClicked.attr('checked') && (subgroup = null || subgroup.length==0)){
            $("#z_score_threshold").slideDown();
        }
        // if checkbox is unselected, hide threshold input regardless of whether there are subgroups
        else if(!profileClicked.attr('checked')){
            $("#z_score_threshold").slideUp();
        }
    }
}

//  Triggered when a cancer study has been selected, either by the user
//  or programatically.
function cancerStudySelected() {
    var cancerStudyId = $("#select_cancer_type").val();
    if (cancerStudyId=='all'){
        crossCancerStudySelected();
        return;
    }

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
    
    //  show protein level rppa data in the download tab
    if (window.tab_index == "tab_download") {
        addGenomicProfiles(cancer_study.genomic_profiles, "PROTEIN_ARRAY_PROTEIN_LEVEL", "RPPA");;
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
                + case_set.description + "'>" + case_set.name + "</option>");
    }); //  end for each case study loop

    //  Add the user-defined case list option
    $("#select_case_set").append("<option class='case_set_option' value='-1' "
        + "title='Specify you own case list'>User-defined Case List</option>");

    //  Set up Tip-Tip Event Handler for Case Set Pull-Down Menu
    $(".case_set_option").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});

    //  Set up Tip-Tip Event Handler for Genomic Profiles help
    $(".profile_help").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});

    //  Show any hidden form fields
    if(!$("#step2").is(":visible")){
        singleCancerStudySelected();
    }

    //  Set up Event Handler for checking checkboxes associated with radio buttons
    //  This can not be done on page load, because radio buttons are not found when
    //  cancer study selected is "all"
    $("input[type=radio]").click(function(){
        selectCheckbox($(this));
    });

    //  Set up an Event Handler for User unselecting a genomic profile
    $('input[type=checkbox]').click(function(){
        unselectAllSubgroups($(this));
    });

    //  Set up an Event Handler for showing/hiding mRNA threshold input
    $(".MRNA_EXPRESSION").click(function(){
       toggle_threshold($(this));
    });
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

    json = window.metaDataJson;

    //  Iterate through all cancer studies
    jQuery.each(json.cancer_studies,function(key,cancer_study){

        //  Append to Cancer Study Pull-Down Menu
        var addCancerStudy = true;

        //  If the tab index is selected, and this is the all cancer studies option, do not show
        if (window.tab_index == "tab_download" && key == "all") {
            addCancerStudy = false;
        }
        if (addCancerStudy) {
            $("#select_cancer_type").append("<option value='" + key + "'>" + cancer_study.name + "</option>");
        }
        
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
    } else {
        $("#select_gene_set").val("user-defined-list");
    }

    //  Set things up, based on all currently selected genomic profiles
    //  To do so, we iterate through all input elements with the name = 'genetic_profile_ids*'
    $("input:[name*=genetic_profile_ids]").each(function(index) {
        //  val() is the value that or stable ID of the genetic profile ID
        var currentValue = $(this).val();

        //  if the user has this stable ID already selected, mark it as checked
        if (window.genomic_profile_id_selected[currentValue] == 1) {
            $(this).attr('checked','checked');
            //  Select the surrounding checkbox
            selectCheckbox($(this));
        }
    });  //  end for each genomic profile option
}

// Adds the specified genomic profiles to the page.
// Code checks for three possibilities:
// 1.  0 profiles of targetType --> show nothing
// 2.  1 profile of targetType --> show as checkbox
// 3.  >1 profiles of targetType --> show group checkbox plus radio buttons
function addGenomicProfiles (genomic_profiles, targetAlterationType, targetTitle) {
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
            if (downloadTab || genomic_profile.show_in_analysis_tab == true)
            numProfiles++;
        }
    }); //  end for each genomic profile loop

    if (numProfiles == 0) {
        return;
    } else if(numProfiles >1 && downloadTab == false) {
        //  If we have more than 1 profile, output group checkbox
        //  assign a class to associate the checkbox with any subgroups (radio buttons)
        profileHtml += "<input type='checkbox' class='" + targetAlterationType + "'>"
         + targetTitle + " data."
            + " Select one of the profiles below:";
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
                profileHtml += outputGenomicProfileOption (downloadTab, optionType, targetAlterationType,
                        genomic_profile.id, genomic_profile.name, genomic_profile.description);
            }
        }
    }); //  end for each genomic profile loop

    if(numProfiles >1) {
        //  If we have more than 1 profile, output the end div tag
        profileHtml += "</div>";
    }

    if(targetAlterationType == 'MRNA_EXPRESSION' && downloadTab == false){
        var inputName = 'Z_SCORE_THRESHOLD';
        profileHtml += "<div id='z_score_threshold'>Enter a z-score threshold &#177: "
        + "<input type='text' name='" + inputName + "' size='6' value='"
                + window.zscore_threshold + "'>"
        + "</div>";
    }

    $("#genomic_profiles").append(profileHtml);

}

// Outputs a Single Genomic Profile Options
function outputGenomicProfileOption (downloadTab, optionType, targetAlterationType, id, name,
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
        paramName = "genetic_profile_ids_" + targetAlterationType;
    }

    var html =  "<input type='" + optionType + "' "
        + " name='" + paramName + "'"
        + " class='" + targetAlterationType + "'"
        + " value='" + id +"'>" + name + "</input>"
        + "  <img class='profile_help' src='images/help.png' title='"
        + description + "'><br/>";
    return html;
}
