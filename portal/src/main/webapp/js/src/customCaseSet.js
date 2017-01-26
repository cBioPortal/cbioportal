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

// threshold for the size of the distinct category set (TODO subject to change)
var CATEGORY_SET_THRESHOLD = 10;

// map of category names for more readable labels
var _categoryLabelMap;

// map to store user selected category parameters (used for case filtering)
var _customCaseSelection;

// free form data array for the selected cancer study, in other words
// a slice from the clinical_free_form table
var _freeFormData;

// set of all clinical cases (case IDs) for a specific cancer study
var _clinicalCaseSet;

// set of all categories (parameters) and their values 
var _categorySet;

// this (two-dimensional array) filter is used to filter each category set individually,
// the actual filtered case set is the intersection of all arrays (sets) in this filter  
var _caseSetFilter;

// custom case set containing filter (final) result of user selection 
var _customCaseSet;

// variable to store previous cancer study id
var _previousCancerStudyId = -1;

/**
 * Initializes the Modal Dialog and event handlers for custom case set building.
 */
function initCustomCaseSetUI()
{
	// set up modal dialog box for custom case set building (for step 3)
    $("#custom_case_set_dialog").dialog({autoOpen: false, 
		resizable: false,
		modal: true,
		width: 580});
    
    // set listener function for submit button
    $("#submit_custom_case_set").click(_buildCustomCaseSet);
    
    // set listener function for cancel button
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
	
	// if the current cancer study id is equal to the previous one,
	// no need to update the content of the dialog, just display it
	if (cancerStudyId == _previousCancerStudyId)
	{
		$("#custom_case_set_dialog").dialog("open");
		return;
	}
	
    var cancerStudy = window.metaDataJson.cancer_studies[cancerStudyId];

    // update cancer study name
    $("#case_set_dialog_header #selected_cancer_study").empty();
    $("#case_set_dialog_header #selected_cancer_study").append(cancerStudy.name);
	
    // prepare data to be sent to server
    var data = {studyId: cancerStudyId};
    
    // clear content and show loader image
    $("#case_set_dialog_content").empty();
	$("#case_set_dialog_content").append('<tr><td><img src="images/ajax-loader.gif" alt="loading" /></td></tr>');
	
	// populate contents of the dialog box
    jQuery.getJSON("ClinicalFreeForm.json", data, function(json){
    	// store required data as global variables for future reference
    	_freeFormData = json.freeFormData;
    	_clinicalCaseSet = json.clinicalCaseSet;
    	_categoryLabelMap = json.categoryLabelMap;
    	_categorySet = json.categoryMap;
    	
    	var categorySet = json.categoryMap;
    	
    	// initialize custom case selection map
    	_initCustomCaseSelectionMap(categorySet);
    	
    	// initialize case filter sets
    	_initCaseSetFilter(categorySet, json.clinicalCaseSet);
    	
    	// update the case set by taking intersection of all individual parameter sets
    	_customCaseSet = _intersectAllCaseSets(_caseSetFilter);
    	
    	// TODO temporary limiter for the number of cases
    	//_limitNumberOfCases();
    	
    	// update total number of cases & selected number of cases
    	
    	$("#case_set_dialog_header #number_of_cases").empty();
    	
    	$("#case_set_dialog_header #number_of_cases").append(
    			'<span id="current_number_of_cases">' + _customCaseSet.length + '</span>' +
    			' (out of ' + json.clinicalCaseSet.length + ')');
    	
    	// clear the dialog content
    	$("#case_set_dialog_content").empty();
    	
    	// update the dialog content
    	for (var category in categorySet)
    	{
    		// continue if the category is qualified as a filter parameter
    		if (isEligibleForFiltering(categorySet[category]))
    		{
    			// append selection (multi dropdown) box for the current category (parameter)
    			$("#case_set_dialog_content").append('<tr><td align="right">' + _humanReadableCategory(category) + '</td>' +
    				'<td align="left"><select multiple id="select_' + category + '"></select></td></tr>');    			
    			
    			var selected = "";
    			
    			// check if all options are selected
    			// if all selected, mark the corresponding 'select all' option as selected
    			if(_isAllSelected(categorySet, category))
    			{
    				selected = "selected";
    			}
    			
    			// append special (select all) checkbox to enable
    			// selection/deselection of values
    			$("#case_set_dialog_content #select_" + category).append(
       					'<option ' + selected + ' id="' + category + '_selectAll" ' +
       					'value="' + category + '_selectAll" ' + '>' +
       					'<label>(select all)</label>' +
       					'</option>');
    			
    			// append all other parameter values for the current category    			
	       		for (var i=0; i < categorySet[category].length; i++)
	       		{
	       			if (_customCaseSelection["select_" + category][categorySet[category][i]])
	       			{
	       				selected = "selected";
	       			}
	       			else
	       			{
	       				selected = "";
	       			}
	       			
	       			$("#case_set_dialog_content #select_" + category).append(
	       					'<option ' + selected + ' id="' + categorySet[category][i] + '" ' +
	       					'value="' + categorySet[category][i] + '" ' + '>' +
	       					'<label>' + categorySet[category][i] + '</label>' +
	       					'</option>');
	       		}
	       		
	       		// set the dropdown box options
	       		var dropdownOptions = {firstItemChecksAll: true, // enables "select all" button
	       				icon: {placement: 'left'}, // sets the position of the arrow
	       				width: 268,
	       				emptyText: '(none selected)', // text to be displayed when no item is selected
	       				onItemClick: refreshCustomCaseSet}; // callback function for the action
	       		
	       		// initialize the dropdown box
	       		$("#case_set_dialog_content #select_" + category).dropdownchecklist(dropdownOptions);
    		}
    	}
    	
    	// update the previous cancer study id for future use
    	_previousCancerStudyId = cancerStudyId;
    });
    
	$("#custom_case_set_dialog").dialog("open");
}

/**
 * Checks whether the given array of values (for a specific category) are
 * eligible for filtering.
 * 
 * Any array with less than some threshold elements are considered eligible.
 * 
 * If an array size is bigger than the threshold value, then even a single
 * numeric value in the array is enough for classification as non-eligible
 * category.
 * 
 * An array containing no value or only one distinct value is also considered
 * as non-eligible
 * 
 * @param values	array of values for a specific category
 * @returns			true if eligible for filtering, false otherwise
 */
function isEligibleForFiltering(values)
{
	var skip = false;
	
	// skip categories containing no value or only one distinct value
	if (values.length < 2)
	{
		skip = true;
	}
	// skip numeric values if the size of the distinct category set exceeds the threshold value
	else if (values.length >= CATEGORY_SET_THRESHOLD)
	{
		// check first CATEGORY_SET_THRESHOLD elements to be sure it contains numeric values
		// (this is to avoid incorrect classification of text values such as "Missing" or "NA".)
		for (var i=0; i < CATEGORY_SET_THRESHOLD; i++)
		{
			// if at least one of the values is numeric, then skip the category
			if (!isNaN(values[i]))
			{
				skip = true;
				break;
			}
		}
	}
	
	return !skip;
}

/**
 * Initializes custom case selection map for the given category set.
 * 
 * @param categorySet	category set containing parameters (categories)
 */
function _initCustomCaseSelectionMap(categorySet)
{
	_customCaseSelection = new Array();
	
	// first, set everything as selected
	for (var category in categorySet)
	{
		_customCaseSelection["select_" + category] = new Array();
		
		for (var i=0; i < categorySet[category].length; i++)
		{
			_customCaseSelection["select_" + category][categorySet[category][i]] = true;
		}
	}
	
	// second, update selection according to previous selection
	// stored in a hidden variable
	//
	// if _previousCancerStudyId is -1, then it means the page is just loaded,
	// so we should update the selection by using the hidden variable
	// clinical_param_selection. if _previousCancerStudyId is a valid id,
	// then the previous selection should be ignored to reset the selection
	// if the selected study id mismatches the study id from the get request,
	// the previous selection should also be ignored in this case.
	if (_previousCancerStudyId == -1 &&
		window.cancer_study_id_selected == $("#select_cancer_type").val())
	{
		var selection = JSON.parse($("#clinical_param_selection").val());
		
		for (var category in selection)
		{
			for (var i=0; i < selection[category].length; i++)
			{
				_customCaseSelection["select_" + category][selection[category][i]] = false;
			}
		}
	}

	//console.log("summary: " + $("#clinical_param_selection").val());
}

/**
 * Initializes the case set filter.
 */
function _initCaseSetFilter(categorySet, clinicalCaseSet)
{
	_caseSetFilter = new Array();
	
	var allSelected = new Object();
	
	var category;
	
	// initialize arrays first
	for (category in categorySet)
	{
		if (_isAllSelected(categorySet, category))
		{
			// add all cases for the current category
			_caseSetFilter[category] = clinicalCaseSet.slice();
			
			// mark the category as all selected
			allSelected[category] = true;
		}
		else
		{
			// just init a new array, it will be populated according to the selection
			_caseSetFilter[category] = new Array();
			
			// mark the category as not all selected
			allSelected[category] = false;
		}
	}
	
	// populate filters by iterating all the free form data
	for (var i = 0; i < _freeFormData.length; i++)
	{
		category = _freeFormData[i].paramName;
		
		if (allSelected[category])
		{
			// skip 'all selected' categories
			continue;
		}
		
		// get the category map corresponding to the current parameter name
		var categoryMap = _customCaseSelection["select_" + category];
		
		// check if parameter value (corresponding to the current case) is selected
		if (categoryMap != null &&
			categoryMap[_freeFormData[i].paramValue])
		{	
			// add the case (patient) to the set
			_caseSetFilter[category].push(_freeFormData[i].caseId);
		}
	}
}

/**
 * Updates the custom case set according to the new user selection.
 * 
 * @param checkbox	target check box selected by the user
 * @param selector	target selection box modified by the user
 */
function refreshCustomCaseSet(checkbox, selector)
{
	// extract the category name from selector id
	var category = selector.id.substring(selector.id.indexOf('_') + 1);
	
	// reset flags
	var selectAll = false;
	var selectNone = false;
	
	// this only checks if 'select all' button is explicitly clicked by the user
	if (checkbox.val() == (category + "_selectAll"))
	{
		// add all cases without filtering if it is checked
		if (checkbox.prop("checked"))
		{
			selectAll = true;
		}
		else
		{
			selectNone = true;
		}
	}
	// it is also possible that select all will be programmatically selected,
	// if with the current click all options become selected. so we should
	// also check that condition (since the 'select all' box is not updated yet
	// at the time this function is called)
	else if (checkbox.prop("checked"))
	{
		selectAll = true;
		
		// iterate all options (except 'select all', so start from 1 instead of 0)
		for(var i = 1; i < selector.options.length; i++)
		{
			// skip current selection (it may not be updated yet)
			if (selector.options[i].value == checkbox.val())
			{
				continue;
			}
			
			// if at least one option is unselected, then all is not selected
			if (!selector.options[i].selected)
			{
				selectAll = false;
				break;
			}
		}
	}
	// also check if none is selected (without checking the 'select all' option) 
	else
	{
		selectNone = true;
		
		// iterate all options (except 'select all', so start from 1 instead of 0)
		for(var i = 1; i < selector.options.length; i++)
		{
			// skip current selection (it may not be updated yet)
			if (selector.options[i].value == checkbox.val())
			{
				continue;
			}
			
			// if at least one option is unselected, then all is not selected
			if (selector.options[i].selected)
			{
				selectNone = false;
				break;
			}
		}
	}
	
	if (selectAll)
	{
		// set all map values to true, start from index 1 (to skip 'select all') 
		for(var i = 1; i < selector.options.length; i++)
		{
			// update selection map
			_customCaseSelection[selector.id][selector.options[i].value] = true;
		}
		
		// add all cases without filtering
		_caseSetFilter[category] = _clinicalCaseSet.slice();
	}
	else if (selectNone)
	{
		// set all map values to false, start from index 1 (to skip 'select all')
		for(var i = 1; i < selector.options.length; i++)
		{
			// update selection map
			_customCaseSelection[selector.id][selector.options[i].value] = false;
		}
		
		// remove all cases without filtering
		_caseSetFilter[category] = new Array();
	}
	else
	{	
		// update selection map for the current checkbox
		_customCaseSelection[selector.id][checkbox.val()] =
			checkbox.prop("checked");
		
		_caseSetFilter[category] = new Array();
		
		// since free form data contains a single parameter (category) and
		// value pair per row, we should iterate all the table to filter cases
		for (var i = 0; i < _freeFormData.length; i++)
		{
			if (_freeFormData[i].paramName != category)
			{
				// skip parameters other than the selected category
				continue;
			}
			
			// get the category map corresponding to the current parameter name
			var categoryMap = _customCaseSelection["select_" + category];
			
			// check if parameter value (corresponding to the current case) is
			// selected by the user
			if (categoryMap != null &&
				categoryMap[_freeFormData[i].paramValue])
			{	
				// add the case (patient) to the set
				_caseSetFilter[category].push(_freeFormData[i].caseId);
			}
		}
	}
	
	// update the case set by taking intersection of all individual parameter sets
	_customCaseSet = _intersectAllCaseSets(_caseSetFilter);
	
	// TODO temporary limit to the number of cases
	//_limitNumberOfCases();
	
	// update current number of included cases
	$("#case_set_dialog_header #current_number_of_cases").text(_customCaseSet.length);
}

/**
 * Intersects all sets, each of which is filtered individually for
 * a specific category, in the given caseSetFilter array, and returns
 * the resulting set as an array.
 *
 * @param caseSetFiler	array of sets containing case IDs
 * @returns				intersection of all sets as an array
 */
function _intersectAllCaseSets(caseSetFilter)
{		
	// initial intersection is the whole clinical case set
	var intersection = _clinicalCaseSet.slice();
	
	for (var key in caseSetFilter)
	{
		// skip sets that are not filtered (i.e. contains all cases)
		if (caseSetFilter[key].length < _clinicalCaseSet.length)
		{
			// empty set (none selected)
			if (caseSetFilter[key].length == 0)
			{
				// intersection will be an empty set,
				intersection = new Array();
			}
			// if the current intersection contains all the cases,
			// just set intersection as the current case set without
			// calling the "expensive" intersect function
			else if (intersection.length == _clinicalCaseSet.length)
			{
				intersection = caseSetFilter[key];
			}
			else
			{
				intersection = _.intersection(intersection,
						caseSetFilter[key]);
			}
			
			// check if intersection is an empty set
			if (intersection.length == 0)
			{
				// no need to continue, final set will be empty
				break;
			}
		}
	}
	
	return intersection;
}

/**
 * TODO Temporary work-around for upper limit of number of user-defined cases.
 */
function _limitNumberOfCases()
{
	console.log("number of filtered cases: " + _customCaseSet.length);
	
	if (_customCaseSet.length > 400)
	{
		// disable the build button
		$("#submit_custom_case_set").attr('disabled', true);
		
		// show the warning message
		$("#case_set_dialog_header .custom_case_set_warning").text("Too many cases (max: 400)");
	}
	else
	{
		// enable the build button
		$("#submit_custom_case_set").attr('disabled', false);
		
		// hide the warning message
		$("#case_set_dialog_header .custom_case_set_warning").empty();
	}
}

/**
 * Creates a custom case set with respect to the input from the user
 */
function _buildCustomCaseSet()
{
	// final update on the custom case set
	_customCaseSet = _intersectAllCaseSets(_caseSetFilter);
	
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
	
	//enter case ids into the text field of custom case set ids
	
	var caseIds = "";
	
	$("#custom_case_set_ids").empty();
	
	for (var i = 0; i < _customCaseSet.length; i++)
	{
		caseIds += _customCaseSet[i] + " ";
	}
	
	$("#custom_case_set_ids").append(jQuery.trim(caseIds));
	
	// this is necessary to show the custom case list
	caseSetSelected();
	
	// also update the value of the hidden variable to keep
	// the current selection after submit
	var summary = _selectionSummary(_categorySet);
	
	$("#clinical_param_selection").val(summary);
}

/**
 * Constructs a summary string for current category selections to store the
 * selection in the session for future use. Constructed string will only
 * contain NON-SELECTED category names.
 * 
 * @param categorySet	categorySet containing category names
 */
function _selectionSummary(categorySet)
{
	var summary = new Object();
	
	for (var category in categorySet)
	{
		var current = new Array();
		
		// first, set everything as selected
		for (var i=0; i < categorySet[category].length; i++)
		{
			// add the parameter to the list if it is not selected
			if (!(_customCaseSelection["select_" + category][categorySet[category][i]]))
			{
				current.push(categorySet[category][i]);
			}
		}
		
		if (current.length > 0)
		{
			summary[category] = current;
		}
	}
	
	// TODO instead of JSON, use something similar to the case set parameter?
	return JSON.stringify(summary);
}

/**
 * Checks if all options selected for a certain category (parameter) by using the
 * _customCaseSelection map.
 * 
 * @param categorySet	set of all categories
 * @param category		name of the category
 */
function _isAllSelected(categorySet, category)
{
	var allSelected = true;
	
	for (var i=0; i < categorySet[category].length; i++)
	{
		// if at least one of the options is not selected, then all is NOT selected
		if (!(_customCaseSelection["select_" + category][categorySet[category][i]]))
		{
			allSelected = false;
			break;
		}
	}
	
	return allSelected;
}

/**
 * Converts the given category name to a more readable text.
 * 
 * @param category	category name from the data
 * @returns			nicely formatted label for the given category
 */
function _humanReadableCategory(category)
{
	var label = _categoryLabelMap[category];
	
	if (label == null)
	{
		label = category;
	}
	
	return label;
}

function openStudyView(){
	// retrieve the currently selected cancer study id
	var cancerStudyId = $("#main_form").find("#select_single_study").val();

	// go to the study view
	window.open("study?id="+cancerStudyId, "_self");
}