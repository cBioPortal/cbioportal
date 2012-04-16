// threshold for the size of the distinct category set 
var CATEGORY_SET_THRESHOLD = 10;

/**
 * Initializes the Modal Dialog and event handlers for custom case set building.
 */
function initCustomCaseSetUI()
{
	// set up modal dialog box for custom case set building (for step 3)
    $("#custom_case_set_dialog").dialog({autoOpen: false, 
		resizable: false,
		modal: true,
		width: 666,
		maxHeight: 300});
    
    $("#submit_custom_case_set").click(buildCustomCaseSet);
    
    $("#cancel_custom_case_set").click(function(evt){
    	 $("#custom_case_set_dialog").dialog("close");
    });
}

/**
 * Displays the modal dialog for building custom case set.
 */
function promptCustomCaseSetBuilder()
{
	var cancerStudyId = $("#select_cancer_type").val();
    var cancerStudy = window.metaDataJson.cancer_studies[cancerStudyId];

    // update cancer study name
    $("#case_set_dialog_header #selected_cancer_study").empty();
    $("#case_set_dialog_header #selected_cancer_study").append("Build a Custom Case Set for: " + cancerStudy.name);
	
	// populate contents of the dialog box
    jQuery.getJSON("ClinicalFreeForm.json?studyId=" + cancerStudyId, function(json){
    	$("#case_set_dialog_header #number_of_cases").empty();
    	$("#case_set_dialog_header #number_of_cases").append("Number of Matching Cases: " +
    		json.sizeOfSet + " (out of " + json.sizeOfSet + ")");
    	
    	// TODO add the selectable content
    	$("#case_set_dialog_content").empty();
    	
    	for (var category in json)
    	{
    		// skip numeric values if the size of the distinct category set exceeds the threshold value
    		// TODO instead of checking the first element, it may be proper to check first CATEGORY_SET_THRESHOLD elements to avoid incorrect classification of text values such as "Missing".
    		if (json[category].length > 0 &&
    			(json[category].length < CATEGORY_SET_THRESHOLD || isNaN(json[category][0])))
    		{
    			$("#case_set_dialog_content").append("<tr><td>" + category + "</td><td></td></tr>");
          		
    			// TODO create comboboxes & checkboxes here
	       		for (var i=0; i < json[category].length; i++)
	       		{
	       			$("#case_set_dialog_content").append("<tr><td></td><td>" + json[category][i] + "</td></tr>");
	       		}
    		}
    	}
    });
    
	$("#custom_case_set_dialog").dialog("open");
}

/**
 * Creates a custom case set with respect to the input from the user
 */
function buildCustomCaseSet()
{
	//TODO get the case set from the server (AJAX query) matching user selection
	
	// close custom case set builder dialog
	$("#custom_case_set_dialog").dialog("close");
	
	//select "User-defined Case List"
	$("#select_case_set .case_set_option").each(function(index){		
		if ($(this).val() == -1)
		{
			$(this).attr("selected", "selected");
		}
		else
		{
			$(this).removeAttr("selected");
		}
	})
	
	// this is necessary to show the custom case list
	caseSetSelected();
	
	//TODO enter case ids into the text field
	
	
}