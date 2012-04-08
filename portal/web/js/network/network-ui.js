// flags
var _autoLayout;
var _removeDisconnected;
var _nodeLabelsVisible;
var _edgeLabelsVisible;
var _panZoomVisible;
var _linksMerged;
var _profileDataVisible;
var _selectFromTab;

// array of control functions
var _controlFunctions;

// edge type constants
var IN_SAME_COMPONENT = "IN_SAME_COMPONENT";
var REACTS_WITH = "REACTS_WITH";
var STATE_CHANGE = "STATE_CHANGE";
var OTHER = "OTHER";

// node type constants
var PROTEIN = "Protein";
var DRUG	= "Drug";
var SMALL_MOLECULE = "SmallMolecule";
var UNKNOWN = "Unknown";

// default values for sliders
var WEIGHT_COEFF = 0;
var ALTERATION_PERCENT = 0;

// class constants for css visualization
var CHECKED_CLASS = "checked-menu-item";
var MENU_SEPARATOR_CLASS = "separator-menu-item";
var FIRST_CLASS = "first-menu-item";
var LAST_CLASS = "last-menu-item";
var MENU_CLASS = "main-menu-item";
var SUB_MENU_CLASS = "sub-menu-item";
var HOVERED_CLASS = "hovered-menu-item";
var SECTION_SEPARATOR_CLASS = "section-separator";
var TOP_ROW_CLASS = "top-row";
var BOTTOM_ROW_CLASS = "bottom-row";
var INNER_ROW_CLASS = "inner-row";

// string constants
var ID_PLACE_HOLDER = "REPLACE_WITH_ID";
var ENTER_KEYCODE = "13";

// name of the graph layout
var _graphLayout = {name: "ForceDirected"};

// force directed layout options
var _layoutOptions;

// map of selected elements, used by the filtering functions
var _selectedElements;

// map of connected nodes, used by filtering functions
var _connectedNodes;

// array of previously filtered elements
var _alreadyFiltered;

// array of genes filtered due to slider
var _filteredBySlider;

// array of nodes filtered due to disconnection
var _filteredByIsolation;

// array of filtered edge types
var _edgeTypeVisibility;

// array of filtered edge sources
var _edgeSourceVisibility;

// map used to resolve cross-references
var _linkMap;

// map used to filter genes by weight slider
var _geneWeightMap;

// threshold value used to filter genes by weight slider
var _geneWeightThreshold;

// maximum alteration value among the non-seed genes in the network
var _maxAlterationPercent;

// CytoscapeWeb.Visualization instance
var _vis;


/**
 * Initializes all necessary components. This function should be invoked, before
 * calling any other function in this script.
 * 
 * @param vis	CytoscapeWeb.Visualization instance associated with this UI
 */
function initNetworkUI(vis)
{
	_vis = vis;
	_linkMap = _xrefArray();
	_alreadyFiltered = new Array();
	_filteredBySlider = new Array();
	_filteredByIsolation = new Array();
	_edgeTypeVisibility = _edgeTypeArray();
	_edgeSourceVisibility = _edgeSourceArray();
	
	_geneWeightMap = _geneWeightArray(WEIGHT_COEFF);
	_geneWeightThreshold = ALTERATION_PERCENT;
	_maxAlterationPercent = _maxAlterValNonSeed(_geneWeightMap);
	
	_resetFlags();
	
	_initControlFunctions();
	_initLayoutOptions();

	_initMainMenu();
	_initDialogs();
	_initPropsUI();
	_initSliders();
	_initTooltipStyle();
	
	// add listener for the main tabs to hide dialogs when user selects
	// a tab other than the Network tab
	$("#tabs").bind("tabsshow", hideDialogs);
	
	// this is required to prevent hideDialogs function to be invoked
	// when clicked on a network tab
	$("#network_tabs").bind("tabsshow", false);
	
	// init tabs	
	$("#network_tabs").tabs();
	
	_initGenesTab();
	_refreshGenesTab();
	_refreshRelationsTab();
	
	// adjust things for IE
	_adjustIE();
	
	// make UI visible
	_setVisibility(true);
}

/**
 * Hides all dialogs upon selecting a tab other than the network tab.
 */
function hideDialogs(evt, ui)
{
	// get the index of the tab that is currently selected
	// var selectIdx = $("#tabs").tabs("option", "selected");
	
	// close all dialogs
	$("#settings_dialog").dialog("close");
	$("#node_inspector").dialog("close");
	$("#edge_inspector").dialog("close");
	$("#node_legend").dialog("close");
	$("#edge_legend").dialog("close");
}

/**
 * This function handles incoming commands from the menu UI. All menu items 
 * is forwarded to this function with a specific command string.
 * 
 * @param command	command as a string
 */
function handleMenuEvent(command)
{
	// execute the corresponding function
	
	var func = _controlFunctions[command];
	func();
}

/**
 * Updates selected genes when clicked on a gene on the Genes Tab. This function
 * helps the synchronization between the genes tab and the visualization.
 * 
 * @param evt	target event that triggered the action
 */
function updateSelectedGenes(evt)
{
	// this flag is set to prevent updateGenesTab function to update the tab
	// when _vis.select function is called.
	_selectFromTab = true;
	
	var nodeIds = new Array();
	
	// deselect all nodes
	_vis.deselect("nodes");
	
	// collect id's of selected node's on the tab
	$("#gene_list_area select option").each(
		function(index)
		{
			if ($(this).is(":selected"))
			{
				nodeId = $(this).val();
				nodeIds.push(nodeId);
			}
		});
	
	// select all checked nodes
	_vis.select("nodes", nodeIds);
	
	// reset flag
	_selectFromTab = false;
}

/**
 * Saves layout settings when clicked on the "Save" button of the
 * "Layout Options" panel.
 */
function saveSettings()
{
	// update layout option values 
	
	for (var i=0; i < (_layoutOptions).length; i++)
	{
//		if (_layoutOptions[i].id == "weightNorm")
//		{
//			// find the selected option and update the corresponding value
//			
//			if ($("#norm_linear").is(":selected"))
//			{
//				_layoutOptions[i].value = $("#norm_linear").val(); 
//			}
//			else if ($("#norm_invlinear").is(":selected"))
//			{
//				_layoutOptions[i].value = $("#norm_invlinear").val(); 
//			}
//			else if ($("#norm_log").is(":selected"))
//			{
//				_layoutOptions[i].value = $("#norm_log").val(); 
//			}
//		}
		
		if (_layoutOptions[i].id == "autoStabilize")
		{
			// check if the auto stabilize box is checked
			
			if($("#autoStabilize").is(":checked"))
			{
				_layoutOptions[i].value = true;
				$("#autoStabilize").val(true);
			}
			else
			{
				_layoutOptions[i].value = false;
				$("#autoStabilize").val(false);
			}
		}
		else
		{
			// simply copy the text field value
			_layoutOptions[i].value = 
				$("#" + _layoutOptions[i].id).val();
		}
	}
	
	// update graphLayout options
	_updateLayoutOptions();
	
	// close the settings panel
	$("#settings_dialog").dialog("close");
}

/**
 * Reverts to default layout settings when clicked on "Default" button of the
 * "Layout Options" panel.
 */
function defaultSettings()
{
	_layoutOptions = _defaultOptsArray();
	_updateLayoutOptions();
	_updatePropsUI();
}

/**
 * Shows the node inspector when double clicked on a node.
 * 
 * @param evt	event that triggered this function
 */
function showNodeInspector(evt)
{
	// set the position of the inspector
	
	// TODO evt.target.x and evt.target.y are local (relative) coordiates inside
	// the CytoscapeWeb flash object, however those values are used as global
	// coordinate by the dialog() function. We need to transform the local
	// coordinates to global coordinates.
	//$("#node_inspector").dialog("option",
	//	"position",
	//	[_mouseX(evt), _mouseY(evt)]);
	
	// update the contents of the inspector by using the target node
	
	var data = evt.target.data;
	
	_updateNodeInspectorContent(data, evt.target);

	// open inspector panel
	$("#node_inspector").dialog("open").height("auto");
	
	// if the inspector panel height exceeds the max height value
	// adjust its height (this also adds scroll bars by default)
	if ($("#node_inspector").height() >
		$("#node_inspector").dialog("option", "maxHeight"))
	{
		$("#node_inspector").dialog("open").height(
			$("#node_inspector").dialog("option", "maxHeight"));
	}
}


/**
 * Updates node inspector data for drug node type
 * @param data double clicked node's ( drug for this method ) data
 * */
function _updateNodeInspectorForDrug(data, node)
{
	var targets = new Array();
	var atc_codes = new Array();
	var synonyms = new Array();
	var description;
	
	//For number of targeted genes
	if (data["TARGETS"] != "") 
	{	
		targets = data["TARGETS"].split(";");
		
		$("#node_inspector_content .data").append(
		'<tr align="left" class="targets-data-row"><td>' +
		'<strong>Targeted gene number: </strong> ' + targets.length + 
		'</td></tr>');	
		$("#node_inspector_content .targets-data-row td").append('<br><br>');

	}
	
	// For drug atc code
	$("#node_inspector_content .data").append(
			'<tr align="left" class="atc_codes-data-row"><td>' +
			'<strong>Drug Class(ATC codes): </strong></td></tr>');
	
	atc_codes = data["ATC_CODE"].split(",");
	
	for ( var i = 0; i < atc_codes.length; i++) 
	{	
		$("#node_inspector_content .atc_codes-data-row td").append(atc_codes[i]);
		if (i != atc_codes.length - 1) 
		{
			$("#node_inspector_content .atc_codes-data-row td").append(', ');
		}
	}
	
	if (data["ATC_CODE"] == "") 
	{
		$("#node_inspector_content .atc_codes-data-row td").append("Unknown");
	}
	$("#node_inspector_content .atc_codes-data-row td").append('<br><br>');
	
	
	// For drug Synonyms
	$("#node_inspector_content .data").append(
			'<tr align="left" class="synonyms-data-row"><td>' +
			'<strong>Synonyms: </strong></td></tr>');
	
	
	if (data["SYNONYMS"] == "") 
	{
		$("#node_inspector_content .synonyms-data-row td").append("Unknown");
		$("#node_inspector_content .synonyms-data-row td").append('<br>');
	}
	else
	{
		synonyms = data["SYNONYMS"].split(";");	
		for ( var i = 0; i < synonyms.length; i++) 
		{
			$("#node_inspector_content .synonyms-data-row td").append('<p style="margin: 0px;"> -' + synonyms[i] + '</p>');
		}
	}
	$("#node_inspector_content .synonyms-data-row td").append('<br>');
	
	
	// For Drug description
	$("#node_inspector_content .data").append(
			'<tr align="left" class="description-data-row"><td>' +
			'<strong>Description: </strong></td></tr>');
	
	
	description = data["DESCRIPTION"];
	
	if (description != "") 
	{
		$("#node_inspector_content .description-data-row td").append(description);
	}
	else
		$("#node_inspector_content .description-data-row td").append("Unknown");
	
	
	$("#node_inspector_content .description-data-row td").append('<br><br>');
	
	
	// For FDA approval
	$("#node_inspector_content .data").append(
			'<tr align="left" class="fda-data-row"><td>' +
			'<strong>FDA Approval: </strong></td></tr>');
	
	var fda_approval = ((data["FDA_APPROVAL"] == "true")? "Approved":"Not Approved");
	
	$("#node_inspector_content .fda-data-row td").append(fda_approval);
	$("#node_inspector_content .fda-data-row td").append('<br><br>');
	
	
	// For Pub Med IDs			
	$("#node_inspector_content .data").append(
			'<tr align="left" class="pubmed-data-row"><td>' +
			'<strong>PubMed IDs: </strong></td></tr>');
	
	var pubmeds = new Array();			
	var edges = _vis.edges();
	
	for ( var i = 0; i < edges.length; i++) 
	{
		if (edges[i].data.source == node.data.id) 
		{
			$("#node_inspector_content .pubmed-data-row td").append(edges[i].data["INTERACTION_PUBMED_ID"]);
		}
	}
	
	if (pubmeds.length == 0) 
	{			
		$("#node_inspector_content .pubmed-data-row td").append("Unknown");
	}
}

/**
 * Updates the content of the node inspector with respect to the provided data.
 * Data is assumed to be the data of a node.
 * 
 * @param data	node data containing necessary fields
 */
function _updateNodeInspectorContent(data,node)
{
	// set title
	
	var title = data.label;
	
	if (title == null)
	{
		title = data.id;
	}
	
	$("#node_inspector").dialog("option",
		"title",
		title);
	
	// clean xref, percent, and data rows
	
	// These rows for drug view of node inspector.
	$("#node_inspector_content .data .targets-data-row").remove();
	$("#node_inspector_content .data .atc_codes-data-row").remove();
	$("#node_inspector_content .data .synonyms-data-row").remove();
	$("#node_inspector_content .data .description-data-row").remove();
	$("#node_inspector_content .data .fda-data-row").remove();
	$("#node_inspector_content .data .pubmed-data-row").remove();
	
	// For non drug view of node inspector
	$("#node_inspector_content .data .data-row").remove();
	
	$("#node_inspector_content .xref .xref-row").remove();
	$("#node_inspector_content .profile .percent-row").remove();
	$("#node_inspector_content .profile-header .header-row").remove();
	
	if (data.type == DRUG) 
	{
		_updateNodeInspectorForDrug(data, node);
	}
	
		
	//_addDataRow("node", "ID", data.id);
	
	if (data.type == PROTEIN)
	{
		_addDataRow("node", "Gene Symbol", data.label);
		//_addDataRow("node", "User-Specified", data.IN_QUERY);
	
		// add percentage information
		_addPercentages(data);
	}
	
	// add cross references
	
	var links = new Array();
	
	// parse the xref data, and construct link and labels
	
	var xrefData = new Array();
	
	if (data["UNIFICATION_XREF"] != null)
	{
		xrefData = data["UNIFICATION_XREF"].split(";");
	}
	
	if (data["RELATIONSHIP_XREF"] != null)
	{
		xrefData = xrefData.concat(data["RELATIONSHIP_XREF"].split(";"));
	}
		
	var link, xref;
			
	for (var i = 0; i < xrefData.length; i++)
	{
		link = _resolveXref(xrefData[i]);
		links.push(link);
	}
	
	// add each link as an xref entry
	
	if (links.length > 0)
	{
		$("#node_inspector_content .xref").append(
			'<tr class="xref-row"><td><strong>More at: </strong></td></tr>');
	}
	
	for (var i=0; i < links.length; i++)
	{
		_addXrefEntry('node', links[i].href, links[i].text);
				
		if (i != links.length - 1) 
		{
			$("#node_inspector_content .xref-row td").append(', ');
		}
	}
}



/**
 * Add percentages (genomic profile data) to the node inspector with their
 * corresponding colors & names.
 * 
 * @param data	node (gene) data
 */
function _addPercentages(data)
{
	var percent;
	
	// init available profiles array
	var available = new Array();
	available['CNA'] = new Array();
	available['MRNA'] = new Array();
	available['MUTATED'] = new Array();
	
	// add percentage values
	
	if (data["PERCENT_CNA_AMPLIFIED"] != null)
	{
		percent = (data["PERCENT_CNA_AMPLIFIED"] * 100);
		_addPercentRow("cna-amplified", "Amplification", percent, "#FF2500");
		available['CNA'].push("cna-amplified");
	}	
	
	if (data["PERCENT_CNA_HOMOZYGOUSLY_DELETED"] != null)
	{
		percent = (data["PERCENT_CNA_HOMOZYGOUSLY_DELETED"] * 100);
		_addPercentRow("cna-homozygously-deleted", "Homozygous Deletion", percent, "#0332FF");
		available['CNA'].push("cna-homozygously-deleted");
	}
	
	if (data["PERCENT_CNA_GAINED"] != null)
	{
		percent = (data["PERCENT_CNA_GAINED"] * 100);
		_addPercentRow("cna-gained", "Gain", percent, "#FFC5CC");
		available['CNA'].push("cna-gained");
	}
	
	if (data["PERCENT_CNA_HEMIZYGOUSLY_DELETED"] != null)
	{
		percent = (data["PERCENT_CNA_HEMIZYGOUSLY_DELETED"] * 100);
		_addPercentRow("cna-hemizygously-deleted", "Hemizygous Deletion", percent, "#9EDFE0");
		available['CNA'].push("cna-hemizygously-deleted");
	}
	
	if (data["PERCENT_MRNA_WAY_UP"] != null)
	{
		percent = (data["PERCENT_MRNA_WAY_UP"] * 100);
		_addPercentRow("mrna-way-up", "Up-regulation", percent, "#FFACA9");
		available['MRNA'].push("mrna-way-up");
	}
	
	if (data["PERCENT_MRNA_WAY_DOWN"] != null)
	{
		percent = (data["PERCENT_MRNA_WAY_DOWN"] * 100);
		_addPercentRow("mrna-way-down", "Down-regulation", percent, "#78AAD6");
		available['MRNA'].push("mrna-way-down");
	}
	
	if (data["PERCENT_MUTATED"] != null)
	{
		percent = (data["PERCENT_MUTATED"] * 100);
		_addPercentRow("mutated", "Mutation", percent, "#008F00");
		available['MUTATED'].push("mutated");
	}
	
	// add separators
	
	if (available['CNA'].length > 0)
	{
		$("#node_inspector .profile ." + available['CNA'][0]).addClass(
			SECTION_SEPARATOR_CLASS);
	}
	
	if (available['MRNA'].length > 0)
	{
		$("#node_inspector .profile ." + available['MRNA'][0]).addClass(
			SECTION_SEPARATOR_CLASS);
	}
	
	if (available['MUTATED'].length > 0)
	{
		$("#node_inspector .profile ." + available['MUTATED'][0]).addClass(
			SECTION_SEPARATOR_CLASS);
	}
	
	
	// add header & total alteration value if at least one of the profiles is
	// available
	
	if (available['CNA'].length > 0
		|| available['MRNA'].length > 0
		|| available['MUTATED'].length > 0)
	{
		
		// add header
		$("#node_inspector .profile-header").append('<tr class="header-row">' +
			'<td><div>Genomic Profile(s):</div></td></tr>');
		
		// add total alteration frequency
		
		percent = (data["PERCENT_ALTERED"] * 100);
		
		var row = '<tr class="total-alteration percent-row">' +
			'<td><div class="percent-label">Total Alteration</div></td>' +
			'<td class="percent-cell"></td>' +
			'<td><div class="percent-value">' + percent.toFixed(1) + '%</div></td>' +
			'</tr>';

		// append as a first row
		$("#node_inspector .profile").prepend(row);
	}
}

/**
 * Adds a row to the genomic profile table of the node inspector.
 * 
 * @param section	class name of the percentage
 * @param label		label to be displayed
 * @param percent	percentage value
 * @param color		color of the percent bar
 */
function _addPercentRow(section, label, percent, color)
{
	var row = '<tr class="' + section + ' percent-row">' +
		'<td><div class="percent-label"></div></td>' +
		'<td class="percent-cell"><div class="percent-bar"></div></td>' +
		'<td><div class="percent-value"></div></td>' +
		'</tr>';
	
	$("#node_inspector .profile").append(row);
	
	$("#node_inspector .profile ." + section + " .percent-label").text(label);

	$("#node_inspector .profile ." + section + " .percent-bar").css(
		"width", Math.ceil(percent) + "%");
	
	$("#node_inspector .profile ." + section + " .percent-bar").css(
		"background-color", color);
	
	$("#node_inspector .profile ." + section + " .percent-value").text(
		percent.toFixed(1) + "%");
}

/**
 * Shows the edge inspector when double clicked on an edge.
 * 
 * @param evt	event that triggered this function
 */
function showEdgeInspector(evt)
{
	// set the position of the inspector
	// TODO same coordinate problems as node inspector
	//$("#edge_inspector").dialog({position: [_mouseX(evt), _mouseY(evt)]});

	// TODO update the contents of the inspector by using the target edge
	
	var data = evt.target.data;
	var title = _vis.node(data.source).data.label + " - " + 
		_vis.node(data.target).data.label;
	
	// clean xref & data rows
	$("#edge_inspector_content .data .data-row").remove();
	$("#edge_inspector_content .xref .xref-row").remove();
	
	// if the target is a merged edge, then add information of all edges
	// between the source and target
	if (evt.target.merged)
	{
		// update title
		title += ' (Summary Edge)';
		
		var edges = evt.target.edges;
		
		// add information for each edge
		
		for (var i = 0; i < edges.length; i++)
		{
			// skip filtered-out edges
			if (!edgeVisibility(edges[i]))
			{
				continue;
			}
			
			data = edges[i].data;
			
			// add an empty row for better edge separation
			$("#edge_inspector_content .data").append(
				'<tr align="left" class="empty-row data-row"><td> </td></tr>');
			
			// add edge data
			
			_addDataRow("edge",
				"Source",
				data["INTERACTION_DATA_SOURCE"],
				TOP_ROW_CLASS);
			
			if (data["INTERACTION_PUBMED_ID"] == null)
			{
				// no PubMed ID, add only type information
				_addDataRow("edge",
					"Type",
					_toTitleCase(data["type"]),
					BOTTOM_ROW_CLASS);
			}
			else
			{
				// add type information
				_addDataRow("edge",
					"Type",
					_toTitleCase(data["type"]),
					INNER_ROW_CLASS);
		
				_addPubMedIds(data, true);
			}
		}
		
		// add an empty row for the last edge
		$("#edge_inspector_content .data").append(
			'<tr align="left" class="empty-row data-row"><td> </td></tr>');
	}
	// target is a regular edge
	else
	{
		_addDataRow("edge", "Source", data["INTERACTION_DATA_SOURCE"]);
		_addDataRow("edge", "Type", _toTitleCase(data["type"]));
		
		if (data["INTERACTION_PUBMED_ID"] != null)
		{
			_addPubMedIds(data, false);
		}
	}
	
	// set title
	$("#edge_inspector").dialog("option",
		"title",
		title);
	
	// open inspector panel
	$("#edge_inspector").dialog("open").height("auto");
	
	// if the inspector panel height exceeds the max height value
	// adjust its height (this also adds scroll bars by default)
	if ($("#edge_inspector").height() >
		$("#edge_inspector").dialog("option", "maxHeight"))
	{
		$("#edge_inspector").dialog("open").height(
			$("#edge_inspector").dialog("option", "maxHeight"));
	}
}

/**
 * Adds PubMed ID's as new data rows to the edge inspector.
 * 
 * @param data			edge's data
 * @param summaryEdge	indicated whether the given edge is a summary edge or
 * 						a regular edge
 */
function _addPubMedIds(data, summaryEdge)
{
	var ids = data["INTERACTION_PUBMED_ID"].split(";");
	var link, xref;
	var links = new Array();
	
	// collect pubmed id(s) into an array
	
	for (var i = 0; i < ids.length; i++)
	{
		link = _resolveXref(ids[i]);
		
		if (link.href == "#")
		{
			// skip unknown sources
			continue;
		}
		
		xref = '<a href="' + link.href + '" target="_blank">' +
			link.pieces[1] + '</a>';
		
		links.push(xref);
	}
	
	xrefList = links[0];
	
	for (var i = 1; i < links.length; i++)
	{
		xrefList += ", " + links[i];
	}
	
	if (summaryEdge)
	{
		// class of this row should be BOTTOM_ROW_CLASS (this is needed
		// to separate edges visually)
		
		_addDataRow("edge",
			"PubMed ID(s)",
			xrefList,
			BOTTOM_ROW_CLASS);
	}
	else
	{
		_addDataRow("edge",
			"PubMed ID(s)",
			xrefList);
	}
}

/**
 * This function shows gene details when double clicked on a node name on the
 * genes tab.
 * 
 * @param evt	event that triggered the action
 */
function showGeneDetails(evt)
{
	// retrieve the selected node
	var node = _vis.node(evt.target.value);
	
	// TODO position the inspector, (also center the selected gene?)
	
	// update inspector content
	_updateNodeInspectorContent(node.data,node);
	
	// open inspector panel
	$("#node_inspector").dialog("open").height("auto");
	
	// if the inspector panel height exceeds the max height value
	// adjust its height (this also adds scroll bars by default)
	if ($("#node_inspector").height() >
		$("#node_inspector").dialog("option", "maxHeight"))
	{
		$("#node_inspector").dialog("open").height(
			$("#node_inspector").dialog("option", "maxHeight"));
	}
}

/**
 * Updates the gene tab if at least one node is selected or deselected on the 
 * network. This function helps the synchronization between the genes tab and
 * visualization.
 * 
 * @param evt	event that triggered the action
 */
function updateGenesTab(evt)
{
	var selected = _vis.selected("nodes");
	
	// do not perform any action on the gene list,
	// if the selection is due to the genes tab
	if(!_selectFromTab)
	{	
		if (_isIE())
		{
			_setComponentVis($("#gene_list_area select"), false);
		}
		
		// deselect all options
		$("#gene_list_area select option").each(
			function(index)
			{
				$(this).removeAttr("selected");
			});
		
		// select options for selected nodes
		for (var i=0; i < selected.length; i++)
		{
			$("#" +  _safeProperty(selected[i].data.id)).attr(
				"selected", "selected");
		}
		
		if (_isIE())
		{
			_setComponentVis($("#gene_list_area select"), true);
		}
	}
	
	// also update Re-submit button
	if (selected.length > 0)
	{
		// enable the button
		$("#re-submit_query").button("enable");
	}
	else
	{
		// disable the button
		$("#re-submit_query").button("disable");
	}
}

function reRunQuery()
{
	// TODO get the list of currently interested genes
	var currentGenes = "";
	var nodeMap = _selectedElementsMap("nodes");
	
	for (var key in nodeMap)
	{
		currentGenes += nodeMap[key].data.label + " ";
	}
	
	if (currentGenes.length > 0)
	{
		// update the list of seed genes for the query
		$("#main_form #gene_list").val(currentGenes);
		
		// re-run query by performing click action on the submit button
		$("#main_form #main_submit").click();
	}
}

/**
 * Searches for genes by using the input provided within the search text field.
 * Also, selects matching genes both from the canvas and gene list.
 */
function searchGene()
{
	var query = $("#genes_tab #search_box").val();
	
	// do not perform search for an empty string
	if (query.length == 0)
	{
		return;
	}
	
	var genes = _visibleGenes();
	var matched = new Array();
	var i;
	
	// linear search for the input text
	
	for (i=0; i < genes.length; i++)
	{
		if (genes[i].data.label.toLowerCase().indexOf(
			query.toLowerCase()) != -1)
		{
			matched.push(genes[i].data.id);
		}
//		else if (genes[i].data.id.toLowerCase().indexOf(
//			query.toLowerCase()) != -1)
//		{
//			matched.push(genes[i].data.id);
//		}
	}
	
	// deselect all nodes
	_vis.deselect("nodes");
	
	// select all matched nodes
	_vis.select("nodes", matched);
}


/**
 * Filters out all selected genes.
 */
function filterSelectedGenes()
{
	// update selected elements map
	_selectedElements = _selectedElementsMap("nodes");

	// filter out selected elements
    _vis.filter("nodes", selectionVisibility);
    
    // also, filter disconnected nodes if necessary
    _filterDisconnected();
    
    // refresh genes tab
    _refreshGenesTab();
    
    // visualization changed, perform layout if necessary
	_visChanged();
}

/**
 * Filters out all non-selected nodes.
 */
function filterNonSelected()
{
	// update selected elements map
	_selectedElements = _selectedElementsMap("nodes");

	// filter out non-selected elements
    _vis.filter('nodes', geneVisibility);
    
    // also, filter disconnected nodes if necessary
    _filterDisconnected();
    
    // refresh Genes tab
    _refreshGenesTab();
    updateGenesTab();
    
    // visualization changed, perform layout if necessary
	_visChanged();
}

/**
 * Updates the visibility (by filtering mechanism) of edges.
 */
function updateEdges()
{
	// update filtered edge types
	
	_edgeTypeVisibility[IN_SAME_COMPONENT] =
		$("#relations_tab .in-same-component input").is(":checked");
	
	_edgeTypeVisibility[REACTS_WITH] =
		$("#relations_tab .reacts-with input").is(":checked");
	
	_edgeTypeVisibility[STATE_CHANGE] =
		$("#relations_tab .state-change input").is(":checked");
	
	_edgeTypeVisibility[OTHER] =
		$("#relations_tab .other input").is(":checked");
	
	for (var key in _edgeSourceVisibility)
	{
		_edgeSourceVisibility[key] =
			$("#relations_tab ." + _safeProperty(key) +
				" input").is(":checked");
	}
	
	// remove previous node filters due to disconnection
	for (var key in _filteredByIsolation)
	{
		_alreadyFiltered[key] = null;
	}
	
	// clear isolation filter array
	_filteredByIsolation = new Array();
	
	// re-apply filter to update nodes
	//_vis.removeFilter("nodes", false);
	_vis.filter("nodes", currentVisibility);
	
	// remove current edge filters
	//_vis.removeFilter("edges", false);
	
	// filter selected types
	_vis.filter("edges", edgeVisibility);
	
	// remove previous filters due to disconnection
	for (var key in _filteredByIsolation)
	{
		_alreadyFiltered[key] = null;
	}
	
    // filter disconnected nodes if necessary
    _filterDisconnected();
    
	// visualization changed, perform layout if necessary
	_visChanged();
}

/**
 * Determines the visibility of a gene (node) for filtering purposes. This
 * function is designed to filter only the genes which are in the array
 * _alreadyFiltered.
 * 
 * @param element	gene to be checked for visibility criteria
 * @return			true if the gene should be visible, false otherwise
 */
function currentVisibility(element)
{
	var visible;
	
	// if the node is in the array of already filtered elements,
	// then it should be invisibile
	if (_alreadyFiltered[element.data.id] != null)
	{
		visible = false;
	}
	// any other node should be visible
	else
	{
		visible = true;
	}
	
	return visible;
}

/**
 * Determines the visibility of an edge for filtering purposes.
 * 
 * @param element	egde to be checked for visibility criteria
 * @return			true if the edge should be visible, false otherwise
 */
function edgeVisibility(element)
{
	var visible = true;
	var typeVisible = true;
	var sourceVisible = true;
	
	// TODO currently we do not allow edge filtering by selection, so
	// there should not be any edge in the array _alreadyFiltered
	
	// if an element is already filtered then it should remain invisible
	if (_alreadyFiltered[element.data.id] != null)
	{
		visible = false;
	}
	
	// unknown edge type, check for the OTHER flag
	if (_edgeTypeVisibility[element.data.type] == null)
	{
		typeVisible = _edgeTypeVisibility[OTHER];
	}
	// check the visibility of the edge type
	else
	{
		typeVisible = _edgeTypeVisibility[element.data.type];
	}
	
	var source = element.data['INTERACTION_DATA_SOURCE'];
	
	if (_edgeSourceVisibility[source] != null)
	{
		sourceVisible = _edgeSourceVisibility[source];
	}
	else
	{
		// no source specified, check the unknown flag
		sourceVisible = _edgeSourceVisibility[UNKNOWN];
	}
	
	return (visible && typeVisible && sourceVisible);
}

/**
 * Determines the visibility of a gene (node) for filtering purposes. This
 * function is designed to filter non-selected genes.
 * 
 * @param element	gene to be checked for visibility criteria
 * @return			true if the gene should be visible, false otherwise
 */
function geneVisibility(element)
{
	var visible = false;
	
	// if an element is already filtered then it should remain invisible
	if (_alreadyFiltered[element.data.id] != null)
	{
		visible = false;
	}
	else
	{
		// filter non-selected nodes
		
		if (_selectedElements[element.data.id] != null)
		{
			visible = true;
		}
		
		if (!visible)
		{
			// if the element should be filtered, then add it to the map
			_alreadyFiltered[element.data.id] = element;
		}
	}
	
	return visible;
}

/**
 * Determines the visibility of a gene (node) for filtering purposes. This
 * function is designed to filter genes by the slider value.
 * 
 * @param element	gene to be checked for visibility criteria
 * @return			true if the gene should be visible, false otherwise
 */
function sliderVisibility(element)
{
	var visible = false;
	var weight;
	
	// if an element is already filtered then it should remain invisible
	if (_alreadyFiltered[element.data.id] != null)
	{
		visible = false;
	}
	// if an element is a seed node, then it should be visible
	// (if it is not filtered manually)
	else if (element.data["IN_QUERY"] != null &&
			element.data["IN_QUERY"].toLowerCase() == "true")
	{
		visible = true;
	}
	else
	{	
		// get the weight of the node
		weight = _geneWeightMap[element.data.id];
		
		// if the weight of the current node is below the threshold value
		// then it should be filtered
		
		if (weight != null)
		{
			if (weight >= _geneWeightThreshold)
			{
				visible = true;
			}
		}
		else
		{
			// no weight value, filter not applicable
			visible = true;
		}
		
		if (!visible)
		{
			// if the element should be filtered,
			// then add it to the required maps
			
			_alreadyFiltered[element.data.id] = element;
			_filteredBySlider[element.data.id] = element;
		}
	}
	
	return visible;
}

/**
 * Determines the visibility of a node for filtering purposes. This function is
 * designed to filter disconnected nodes.
 * 
 * @param element	node to be checked for visibility criteria
 * @return			true if the node should be visible, false otherwise
 */
function isolation(element)
{
	var visible = false;
	
	// if an element is already filtered then it should remain invisible
	if (_alreadyFiltered[element.data.id] != null)
	{
		visible = false;
	}
	else
	{
		// check if the node is connected, if it is disconnected it should be
		// filtered out
		if (_connectedNodes[element.data.id] != null)
		{
			visible = true;
		}
		
		if (!visible)
		{
			// if the node should be filtered, then add it to the map
			_alreadyFiltered[element.data.id] = element;
			_filteredByIsolation[element.data.id] = element;
		}
	}
	
	return visible;
}

/**
 * This function returns false if the given graph element is selected,
 * returns true otherwise. This function is used to hide (filter) selected
 * nodes & edges.
 * 
 * @param element	element to be checked
 * @return			false if selected, true otherwise
 */
function selectionVisibility(element)
{
	// if an element is already filtered then it should remain invisible
	// until the filters are reset
	if (_alreadyFiltered[element.data.id] != null)
	{
		return false;
	}
	// if an edge type is hidden, all edges of that type should be invisible
	else if (_edgeTypeVisibility[element.data.type] != null
			&& !_edgeTypeVisibility[element.data.type])
	{
		return false;
	}
	// if an edge source is hidden, all edges of that source should be invisible
	// else if (...)
	
	// TODO this function is not called anymore for edges (no edge filtering via selecting)
	// so the edge visibility check can be omitted
	
	// if the element is selected, then it should be filtered
	if (_selectedElements[element.data.id] != null)
	{
		_alreadyFiltered[element.data.id] = element;
		return false;
	}
	
	return true;
}

/**
 * Creates a map (on element id) of selected elements.
 *  
 * @param group		data group (nodes, edges, all)
 * @return			a map of selected elements
 */
function _selectedElementsMap(group)
{
	var selected = _vis.selected(group);
	var map = new Array();
	
	for (var i=0; i < selected.length; i++)
	{
		var key = selected[i].data.id;
		map[key] = selected[i];
	}
	
	return map;
}

/**
 * Creates a map (on element id) of connected nodes.
 * 
 * @return	a map of connected nodes
 */
function _connectedNodesMap()
{
	var map = new Array();
	var edges;
	
	// if edges merged, traverse over merged edges for a better performance
	if (_vis.edgesMerged())
	{
		edges = _vis.mergedEdges();
	}
	// else traverse over regular edges
	else
	{
		edges = _vis.edges();
	}
	
	var source;
	var target;
	
	
	// for each edge, add the source and target to the map of connected nodes
	for (var i=0; i < edges.length; i++)
	{
		if (edges[i].visible)
		{
			source = _vis.node(edges[i].data.source);
			target = _vis.node(edges[i].data.target);
		
			map[source.data.id] = source;
			map[target.data.id] = target;
		}
	}
	
	return map;
}

/**
 * This function is designed to be invoked after an operation (such as filtering
 * nodes or edges) that changes the graph topology. 
 */
function _visChanged()
{
	// perform layout if auto layout flag is set
	
	if (_autoLayout)
	{
		// re-apply layout
		_performLayout();
	}
}

/**
 * This function is designed to be invoked after an operation that filters
 * nodes or edges.
 */
function _filterDisconnected()
{
	// filter disconnected nodes if the flag is set
	if (_removeDisconnected)
	{
		// update connected nodes map
		_connectedNodes = _connectedNodesMap();
		
		// filter disconnected
		_vis.filter('nodes', isolation);
	}
}

/**
 * Highlights the neighbors of the selected nodes.
 * 
 * The content of this method is copied from GeneMANIA (genemania.org) sources.
 */
function _highlightNeighbors(/*nodes*/)
{
	/*
	if (nodes == null)
	{
		nodes = _vis.selected("nodes");
	}
	*/
	
	var nodes = _vis.selected("nodes");
	
	if (nodes != null && nodes.length > 0)
	{
		var fn = _vis.firstNeighbors(nodes, true);
		var neighbors = fn.neighbors;
		var edges = fn.edges;
		edges = edges.concat(fn.mergedEdges);
		neighbors = neighbors.concat(fn.rootNodes);
        var bypass = _vis.visualStyleBypass() || {};
		
		if( ! bypass.nodes )
		{
            bypass.nodes = {};
        }
        if( ! bypass.edges )
        {
            bypass.edges = {};
        }

		var allNodes = _vis.nodes();
		
		$.each(allNodes, function(i, n) {
		    if( !bypass.nodes[n.data.id] ){
		        bypass.nodes[n.data.id] = {};
		    }
			bypass.nodes[n.data.id].opacity = 0.25;
	    });
		
		$.each(neighbors, function(i, n) {
		    if( !bypass.nodes[n.data.id] ){
		        bypass.nodes[n.data.id] = {};
		    }
			bypass.nodes[n.data.id].opacity = 1;
		});

		var opacity;
		var allEdges = _vis.edges();
		allEdges = allEdges.concat(_vis.mergedEdges());
		
		$.each(allEdges, function(i, e) {
		    if( !bypass.edges[e.data.id] ){
		        bypass.edges[e.data.id] = {};
		    }
		    /*
		    if (e.data.networkGroupCode === "coexp" || e.data.networkGroupCode === "coloc") {
		    	opacity = AUX_UNHIGHLIGHT_EDGE_OPACITY;
		    } else {
		    	opacity = DEF_UNHIGHLIGHT_EDGE_OPACITY;
		    }
		    */
		    
		    opacity = 0.15;
		    
			bypass.edges[e.data.id].opacity = opacity;
			bypass.edges[e.data.id].mergeOpacity = opacity;
	    });
		
		$.each(edges, function(i, e) {
		    if( !bypass.edges[e.data.id] ){
		        bypass.edges[e.data.id] = {};
		    }
		    /*
		    if (e.data.networkGroupCode === "coexp" || e.data.networkGroupCode === "coloc") {
		    	opacity = AUX_HIGHLIGHT_EDGE_OPACITY;
		    } else {
		    	opacity = DEF_HIGHLIGHT_EDGE_OPACITY;
		    }
		    */
		    
		    opacity = 0.85;
		    
			bypass.edges[e.data.id].opacity = opacity;
			bypass.edges[e.data.id].mergeOpacity = opacity;
		});

		_vis.visualStyleBypass(bypass);
		//CytowebUtil.neighborsHighlighted = true;
		
		//$("#menu_neighbors_clear").removeClass("ui-state-disabled");
	}
}

/**
 * Removes all highlights from the visualization.
 * 
 * The content of this method is copied from GeneMANIA (genemania.org) sources.
 */
function _removeHighlights()
{
	var bypass = _vis.visualStyleBypass();
	bypass.edges = {};
	
	var nodes = bypass.nodes;
	
	for (var id in nodes)
	{
		var styles = nodes[id];
		delete styles["opacity"];
		delete styles["mergeOpacity"];
	}
	
	_vis.visualStyleBypass(bypass);

	//CytowebUtil.neighborsHighlighted = false;
	//$("#menu_neighbors_clear").addClass("ui-state-disabled");
}

/**
 * Displays the node legend in a separate panel.
 */
function _showNodeLegend()
{
	// open legend panel
	$("#node_legend").dialog("open").height("auto");
}

/**
 * Displays the edge legend in a separate panel.
 */
function _showEdgeLegend()
{

//	$("#edge_legend .in-same-component .color-bar").css(
//		"background-color", "#CD976B");
//	
//	$("#edge_legend .reacts-with .color-bar").css(
//		"background-color", "#7B7EF7");
//	
//	$("#edge_legend .state-change .color-bar").css(
//		"background-color", "#67C1A9");
//	
//	$("#edge_legend .other .color-bar").css(
//			"background-color", "#A583AB");
//	
//	$("#edge_legend .merged-edge .color-bar").css(
//		"background-color", "#666666");
	
	// open legend panel
	//$("#edge_legend").dialog("open").height("auto");
	$("#edge_legend").dialog("open");
}

/**
 * Adds a data row to the node or edge inspector.
 * 
 * @param type		type of the inspector (should be "node" or "edge")
 * @param label		label of the data field
 * @param value		value of the data field
 * @param section	optional class value for row element
 */
function _addDataRow(type, label, value /*, section*/)
{
	var section = arguments[3];
	
	if (section == null)
	{
		section = "";
	}
	else
	{
		section += " ";
	}
	
	// replace null string with a predefined string
	
	if (value == null)
	{
		value = UNKNOWN;
	}
	
	$("#" + type + "_inspector_content .data").append(
		'<tr align="left" class="' + section + 'data-row"><td>' +
		'<strong>' + label + ':</strong> ' + value + 
		'</td></tr>');
}

/**
 * Adds a cross reference entry to the node or edge inspector.
 * 
 * @param type		type of the inspector (should be "node" or "edge")
 * @param href		URL of the reference 
 * @param label		label to be displayed
 */
function _addXrefEntry(type, href, label)
{
	$("#" + type + "_inspector_content .xref-row td").append(
		'<a href="' + href + '" target="_blank">' +
		label + '</a>');
}

/**
 * Generates the URL and the display text for the given xref string.
 * 
 * @param xref	xref as a string
 * @return		array of href and text pairs for the given xref
 */
function _resolveXref(xref)
{
	var link = null;
	
	if (xref != null)
	{
		// split the string into two parts
		var pieces = xref.split(":", 2);
		
		// construct the link object containing href and text
		link = new Object();
		
		link.href = _linkMap[pieces[0].toLowerCase()];
		 
		if (link.href == null)
		{
			// unknown source
			link.href = "#";
		}
		// else, check where search id should be inserted
		else if (link.href.indexOf(ID_PLACE_HOLDER) != -1)
		{
			link.href = link.href.replace(ID_PLACE_HOLDER, pieces[1]);
		}
		else
		{
			link.href += pieces[1];
		}
		
		link.text = xref;
		link.pieces = pieces;
	}
	
	return link;
}

/**
 * Sets the default values of the control flags.
 */
function _resetFlags()
{
	_autoLayout = false;
	_removeDisconnected = false;
	_nodeLabelsVisible = true;
	_edgeLabelsVisible = false;
	_panZoomVisible = true;
	_linksMerged = true;
	_profileDataVisible = false;
	_selectFromTab = false;
}

/**
 * Sets the visibility of the complete UI.
 * 
 * @param visible	a boolean to set the visibility.
 */
function _setVisibility(visible)
{
	if (visible)
	{
		//if ($("#network_tabs").hasClass("hidden-network-ui"))
		if ($("#network_menu_div").hasClass("hidden-network-ui"))
		{
			$("#network_menu_div").removeClass("hidden-network-ui");
			$("#quick_info_div").removeClass("hidden-network-ui");
			$("#network_tabs").removeClass("hidden-network-ui");
			$("#node_inspector").removeClass("hidden-network-ui");
			$("#edge_inspector").removeClass("hidden-network-ui");
			$("#node_legend").removeClass("hidden-network-ui");
			$("#edge_legend").removeClass("hidden-network-ui");
			$("#settings_dialog").removeClass("hidden-network-ui");
		}
	}
	else
	{
		if (!$("#network_menu_div").hasClass("hidden-network-ui"))
		{
			$("#network_menu_div").addClass("hidden-network-ui");
			$("#quick_info_div").addClass("hidden-network-ui");
			$("#network_tabs").addClass("hidden-network-ui");
			$("#node_inspector").addClass("hidden-network-ui");
			$("#edge_inspector").addClass("hidden-network-ui");
			$("#node_legend").addClass("hidden-network-ui");
			$("#edge_legend").addClass("hidden-network-ui");
			$("#settings_dialog").addClass("hidden-network-ui");
		}
	}
}

/**
 * Sets visibility of the given UI component.
 * 
 * @param component	an html UI component
 * @param visible	a boolean to set the visibility.
 */
function _setComponentVis(component, visible)
{
	// set visible
	if (visible)
	{
		if (component.hasClass("hidden-network-ui"))
		{
			component.removeClass("hidden-network-ui");
		}
	}
	// set invisible
	else
	{
		if (!component.hasClass("hidden-network-ui"))
		{
			component.addClass("hidden-network-ui");
		}
	}
}

/**
 * Creates an array containing default option values for the ForceDirected
 * layout.
 * 
 * @return	an array of default layout options
 */
function _defaultOptsArray()
{
	var defaultOpts = 
		[ { id: "gravitation", label: "Gravitation",       value: -350,   tip: "The gravitational constant. Negative values produce a repulsive force." },
		  { id: "mass",        label: "Node mass",         value: 3,      tip: "The default mass value for nodes." },
		  { id: "tension",     label: "Edge tension",      value: 0.1,    tip: "The default spring tension for edges." },
		  { id: "restLength",  label: "Edge rest length",  value: "auto", tip: "The default spring rest length for edges." },
		  { id: "drag",        label: "Drag co-efficient", value: 0.4,    tip: "The co-efficient for frictional drag forces." },
		  { id: "minDistance", label: "Minimum distance",  value: 1,      tip: "The minimum effective distance over which forces are exerted." },
		  { id: "maxDistance", label: "Maximum distance",  value: 10000,  tip: "The maximum distance over which forces are exerted." },
		  { id: "iterations",  label: "Iterations",        value: 400,    tip: "The number of iterations to run the simulation." },
		  { id: "maxTime",     label: "Maximum time",      value: 30000,  tip: "The maximum time to run the simulation, in milliseconds." },
		  { id: "autoStabilize", label: "Auto stabilize",  value: true,   tip: "If checked, layout automatically tries to stabilize results that seems unstable after running the regular iterations." } ];
	
	return defaultOpts;
}

/**
 * Creates a map for xref entries.
 * 
 * @return	an array (map) of xref entries
 */
function _xrefArray()
{
	var linkMap = new Array();
	
	// TODO find missing links (Nucleotide Sequence Database)
	//linkMap["refseq"] =	"http://www.genome.jp/dbget-bin/www_bget?refseq:";
	linkMap["refseq"] = "http://www.ncbi.nlm.nih.gov/protein/";
	linkMap["entrez gene"] = "http://www.ncbi.nlm.nih.gov/gene?term=";	
	linkMap["hgnc"] = "http://www.genenames.org/cgi-bin/quick_search.pl?.cgifields=type&type=equals&num=50&search=" + ID_PLACE_HOLDER + "&submit=Submit";
	linkMap["uniprot"] = "http://www.uniprot.org/uniprot/";
	linkMap["chebi"] = "http://www.ebi.ac.uk/chebi/advancedSearchFT.do?searchString=" + ID_PLACE_HOLDER + "&queryBean.stars=3&queryBean.stars=-1";
	linkMap["pubmed"] = "http://www.ncbi.nlm.nih.gov/pubmed?term=";
	linkMap["drugbank"] = "http://www.drugbank.ca/drugs/" + ID_PLACE_HOLDER;
	linkMap["nucleotide sequence database"] = "";
	
	return linkMap;
}

/**
 * Creates a map for edge type visibility.
 * 
 * @return	an array (map) of edge type visibility.
 */
function _edgeTypeArray()
{
	var typeArray = new Array();
	
	// by default every edge type is visible
	typeArray[IN_SAME_COMPONENT] = true;
	typeArray[REACTS_WITH] = true;
	typeArray[STATE_CHANGE] = true;
	typeArray[OTHER] = true;
	
	return typeArray;
}

/**
 * Creates a map for edge source visibility.
 * 
 * @return	an array (map) of edge source visibility.
 */
function _edgeSourceArray()
{
	var sourceArray = new Array();
	
	// dynamically collect all sources
	
	var edges = _vis.edges();
	var source;
	
	for (var i = 0; i < edges.length; i++)
	{
		source = edges[i].data.INTERACTION_DATA_SOURCE;
		
		if (source != null
			&& source != "")
		{
			// by default every edge source is visible
			sourceArray[source] = true;
		}
	}
	
	// also set a flag for unknown (undefined) sources
	sourceArray[UNKNOWN] = true;
	
	return sourceArray;
}

/**
 * Calculates weight values for each gene by using the formula:
 * 
 * weight = Max[(Total Alteration of a node), 
 *    Max(Total Alteration of its neighbors) * coeff] * 100
 * 
 * @param coeff	coefficient value used in the weight function
 * @returns		a map (array) containing weight values for each gene
 */
function _geneWeightArray(coeff)
{
	var weightArray = new Array();
	
	if (coeff > 1)
	{
		coeff = 1;
	}
	else if (coeff < 0)
	{
		coeff = 0;
	}
	
	// calculate weight values for each gene
	
	var nodes = _vis.nodes();
	var max, weight, neighbors;
	
	for (var i = 0; i < nodes.length; i++)
	{
		// get the total alteration of the current node
		
		if (nodes[i].data["PERCENT_ALTERED"] != null)
		{
			weight = nodes[i].data["PERCENT_ALTERED"];
		}
		else
		{
			weight = 0;
		}
		
		// get first neighbors of the current node
		
		neighbors = _vis.firstNeighbors([nodes[i]]).neighbors;
		max = 0;
		
		// find the max of the total alteration of its neighbors,
		// if coeff is not 0
		if (coeff > 0)
		{
			for (var j = 0; j < neighbors.length; j++)
			{
				if (neighbors[j].data["PERCENT_ALTERED"] != null)
				{
					if (neighbors[j].data["PERCENT_ALTERED"] > max)
					{
						max = neighbors[j].data["PERCENT_ALTERED"];
					}
				}
			}
			
			// calculate the weight of the max value by using the coeff 
			max = max * (coeff);
			
			// if maximum weight due to the total alteration of its neighbors
			// is greater than its own weight, then use max instead
			if (max > weight)
			{
				weight = max;
			}
		}
		
		// add the weight value to the map
		weightArray[nodes[i].data.id] = weight * 100;
	}
	
	return weightArray;
}

/**
 * Finds the non-seed gene having the maximum alteration percent in
 * the network, and returns the maximum alteration percent value.
 * 
 * @param map	weight map for the genes in the network
 * @return		max alteration percent of non-seed genes
 */
function _maxAlterValNonSeed(map)
{
	var max = 0.0;
	
	for (var key in map)
	{
		// skip seed genes
		
		var node = _vis.node(key);
		
		if (node != null &&
			node.data["IN_QUERY"] == "true")
		{
			continue;
		}
		
		// update max value if necessary
		if (map[key] > max)
		{
			max = map[key];
		}
	}
	
	return max;
}

/**
 * Initializes the main menu by adjusting its style. Also, initializes the
 * inspector panels and tabs.
 */
function _initMainMenu()
{
	// Opera fix
	$("#network_menu ul").css({display: "none"});
	
	// adds hover effect to main menu items (File, Topology, View)
	
	$("#network_menu li").hover(
		function() {
			$(this).find('ul:first').css(
					{visibility: "visible",display: "none"}).show(400);
		},
		function() {
			$(this).find('ul:first').css({visibility: "hidden"});
		});
	
	
	// adds hover effect to menu items
	
	$("#network_menu ul a").hover(
		function() {
			$(this).addClass(HOVERED_CLASS);
		},
		function() {
			$(this).removeClass(HOVERED_CLASS);
		});
	
	// adjust separators between menu items
	
	$("#network_menu_file").addClass(MENU_CLASS);
	$("#network_menu_topology").addClass(MENU_CLASS);
	$("#network_menu_view").addClass(MENU_CLASS);
	$("#network_menu_layout").addClass(MENU_CLASS);
	$("#network_menu_legends").addClass(MENU_CLASS);
	
	$("#save_as_png").addClass(FIRST_CLASS);
	$("#save_as_png").addClass(MENU_SEPARATOR_CLASS);
	$("#save_as_png").addClass(LAST_CLASS);
	
	$("#hide_selected").addClass(FIRST_CLASS);
	$("#hide_selected").addClass(MENU_SEPARATOR_CLASS);	
	$("#remove_disconnected").addClass(MENU_SEPARATOR_CLASS);
	$("#remove_disconnected").addClass(LAST_CLASS);
	
	$("#show_profile_data").addClass(FIRST_CLASS);
	$("#show_profile_data").addClass(MENU_SEPARATOR_CLASS);
	$("#highlight_neighbors").addClass(MENU_SEPARATOR_CLASS);
	$("#remove_highlights").addClass(LAST_CLASS);
	
	$("#perform_layout").addClass(FIRST_CLASS);
	$("#perform_layout").addClass(MENU_SEPARATOR_CLASS);
	//$("#layout_properties").addClass(SUB_MENU_CLASS);
	$("#auto_layout").addClass(MENU_SEPARATOR_CLASS);
	$("#auto_layout").addClass(LAST_CLASS);
	
	$("#show_node_legend").addClass(FIRST_CLASS);
	$("#show_node_legend").addClass(MENU_SEPARATOR_CLASS);	
	$("#show_edge_legend").addClass(LAST_CLASS);
	
	// init check icons for checkable menu items
	
	if (_autoLayout)
	{
		$("#auto_layout").addClass(CHECKED_CLASS);
	}
	else
	{
		$("#auto_layout").removeClass(CHECKED_CLASS);
	}
	
	if (_removeDisconnected)
	{
		$("#remove_disconnected").addClass(CHECKED_CLASS);
	}
	else
	{
		$("#remove_disconnected").removeClass(CHECKED_CLASS);
	}
	
	if (_nodeLabelsVisible)
	{
		$("#show_node_labels").addClass(CHECKED_CLASS);
	}
	else
	{
		$("#show_node_labels").removeClass(CHECKED_CLASS);
	}
	
	if (_edgeLabelsVisible)
	{
		$("#show_edge_labels").addClass(CHECKED_CLASS);
	}
	else
	{
		$("#show_edge_labels").removeClass(CHECKED_CLASS);
	}
	
	if (_panZoomVisible)
	{
		$("#show_pan_zoom_control").addClass(CHECKED_CLASS);
	}
	else
	{
		$("#show_pan_zoom_control").removeClass(CHECKED_CLASS);
	}
	
	if (_linksMerged)
	{
		$("#merge_links").addClass(CHECKED_CLASS);
	}
	else
	{
		$("#merge_links").removeClass(CHECKED_CLASS);
	}
	
	if (_profileDataVisible)
	{
		$("#show_profile_data").addClass(CHECKED_CLASS);
	}
	else
	{
		$("#show_profile_data").removeClass(CHECKED_CLASS);
	}
}

/**
 * Initializes dialog panels for node inspector, edge inspector, and layout
 * settings.
 */
function _initDialogs()
{
	// adjust settings panel
	$("#settings_dialog").dialog({autoOpen: false, 
		resizable: false, 
		width: 333});
	
	// adjust node inspector
	$("#node_inspector").dialog({autoOpen: false, 
		resizable: false, 
		width: 366,
		maxHeight: 300});
	
	// adjust edge inspector
	$("#edge_inspector").dialog({autoOpen: false, 
		resizable: false, 
		width: 366,
		maxHeight: 300});
	
	// adjust node legend
	$("#node_legend").dialog({autoOpen: false, 
		resizable: false, 
		width: 440});
	
	// adjust edge legend
	$("#edge_legend").dialog({autoOpen: false, 
		resizable: false, 
		width: 280,
		height: 140});
}

/**
 * Initializes the gene filter sliders.
 */
function _initSliders()
{
	// add key listeners for input fields
	$("#weight_slider_field").keypress(_keyPressListener);
	$("#affinity_slider_field").keypress(_keyPressListener);
	
	// show gene filtering slider	
	$("#weight_slider_bar").slider(
		{value: ALTERATION_PERCENT,
		stop: _weightSliderStop,
		slide: _weightSliderMove});
	
	// set max alteration value label
	//$("#weight_slider_area .slider-max label").text(
	//	_maxAlterationPercent.toFixed(1));
	
	// show affinity slider (currently disabled)
//	$("#affinity_slider_bar").slider(
//		{value: WEIGHT_COEFF * 100,
//		change: _affinitySliderChange,
//		slide: _affinitySliderMove});
}

/**
 * Initializes tooltip style for genes.
 */
function _initTooltipStyle()
{	
	// create a function and add it to the Visualization object
	_vis["customTooltip"] = function (data) {
		var text;
		
		if (data["PERCENT_ALTERED"] == null)
		{
			text = "n/a";
		}
		else
		{
			text = Math.round(100 * data["PERCENT_ALTERED"]) + "%";
		}
		
		return "<b>" + text + "</b>";
		//return text;
	};

	// register the custom mapper to the tooltipText
	
	var style = _vis.visualStyle();
	style.nodes.tooltipText = { customMapper: { functionName: "customTooltip" } };

	// set the visual style again
	_vis.visualStyle(style);
	
	// enable node tooltips
	_vis.nodeTooltipsEnabled(true);
}

function _adjustIE()
{
	if (_isIE())
	{
		// this is required to position scrollbar on IE
		//var width = $("#help_tab").width();
		//$("#help_tab").width(width * 1.15);
	}
}

/**
 * Listener for weight slider movement. Updates current value of the slider
 * after each mouse move.
 */
function _weightSliderMove(event, ui)
{
	// get slider value
	var sliderVal = ui.value;
	
	// update current value field
	$("#weight_slider_field").val(
		(_transformValue(sliderVal) * (_maxAlterationPercent / 100)).toFixed(1));
}

/**
 * Listener for weight slider value change. Updates filters with respect to
 * the new slider value.
 */
function _weightSliderStop(event, ui)
{
	// get slider value
	var sliderVal = ui.value;
		
	// apply transformation to prevent filtering of low values 
	// with a small change in the position of the cursor.
	sliderVal = _transformValue(sliderVal) * (_maxAlterationPercent / 100);
	
	// update threshold
	_geneWeightThreshold = sliderVal;
	
	// update current value field
	$("#weight_slider_field").val(sliderVal.toFixed(1));
	
	// update filters
	_filterBySlider();
}

/**
 * Filters genes by the current gene weight threshold value determined by
 * the weight slider.
 */
function _filterBySlider()
{
	// remove previous filters due to slider
	for (var key in _filteredBySlider)
	{
		_alreadyFiltered[key] = null;
	}
	
	// remove previous filters due to disconnection
	for (var key in _filteredByIsolation)
	{
		_alreadyFiltered[key] = null;
	}
	
	// reset required filter arrays
	_filteredBySlider = new Array();
	_filteredByIsolation = new Array();
	
	// remove filters
	//_vis.removeFilter("nodes", false);
	
	// filter with new slider value
	_vis.filter("nodes", sliderVisibility);
	
    // also, filter disconnected nodes if necessary
    _filterDisconnected();
    
    // refresh & update genes tab
    _refreshGenesTab();
    updateGenesTab();
    
    // visualization changed, perform layout if necessary
	_visChanged();
}

/**
 * Listener for affinity slider movement. Updates current value of the slider
 * after each mouse move.
 */
function _affinitySliderMove(event, ui)
{
	// get slider value
	var sliderVal = ui.value;
	
	// update current value field
	$("#affinity_slider_field").val((sliderVal / 100).toFixed(2));
}

/**
 * Listener for affinity slider value change. Updates filters with respect to
 * the new slider value.
 */
function _affinitySliderChange(event, ui)
{
	var sliderVal = ui.value;
	
	// update current value field
	$("#affinity_slider_field").val((sliderVal / 100).toFixed(2));
	
	// re-calculate gene weights
	_geneWeightMap = _geneWeightArray(sliderVal / 100);
	
	// update filters
	_filterBySlider();
}

/**
 * Key listener for input fields on the genes tab.
 * Updates the slider values (and filters if necessary), if the input
 * value is valid.
 * 
 * @param event		event triggered the action
 */
function _keyPressListener(event)
{
	var input;
	
	// check for the ENTER key first 
	if (event.keyCode == ENTER_KEYCODE)
	{
		if (event.target.id == "weight_slider_field")
		{
			input = $("#weight_slider_field").val();
			
			// update weight slider position if input is valid
			
			if (isNaN(input))
			{
				// not a numeric value, update with defaults
				input = ALTERATION_PERCENT;
			}
			else if (input < 0)
			{
				// set values below zero to zero
				input = 0;
			}
			else if (input > 100)
			{
				// set values above 100 to 100
				input = 100;
			}
			
			$("#weight_slider_bar").slider("option", "value",
				_reverseTransformValue(input / (_maxAlterationPercent / 100)));
			
			// update threshold value
			_geneWeightThreshold = input;
			
			// also update filters
			_filterBySlider();
		}
		else if (event.target.id == "affinity_slider_field")
		{
			input = $("#affinity_slider_field").val();
			
			// update affinity slider position if input is valid
			// (this will also update filters if necessary)
			
			if (isNaN(input))
			{
				// not a numeric value, update with defaults
				value = WEIGHT_COEFF;
			}
			else if (input < 0)
			{
				// set values below zero to zero
				value = 0;
			}
			else if (input > 1)
			{
				// set values above 1 to 1
				value = 1;
			}
			
			$("#affinity_slider_bar").slider("option",
				"value",
				Math.round(input * 100));
		}
		else if (event.target.id == "search_box")
		{
			searchGene();
		}
	}
}

/*
function _getSliderValue()
{
	var value = $("#slider_bar").slider("option", "value");
	var sourceInterval, targetInterval;
	
	// transform values between 0-50 on the bar to 0-20
	if (value <= 50)
	{
		sourceInterval = {start: 0, end: 50};
		targetInterval = {start: 0, end: 20};
	}
	// transform values between 50-75 on the bar to 20-50
	else if (value <= 75)
	{
		sourceInterval = {start: 50, end: 75};
		targetInterval = {start: 20, end: 50};
	}
	// transform values between 75-100 on the bar to 50-100
	else
	{
		sourceInterval = {start: 75, end: 100};
		targetInterval = {start: 50, end: 100};
	}
	
	return _transformValue(value, sourceInterval, targetInterval);
}
*/

/*
 * Alternative version with checkboxes..
 *
function _refreshGenesTab()
{
	var nodes = _vis.nodes();
	
	//$("#genes_tab .genes_list li").remove();
	$("#genes_tab table").remove();
	
	
//	for (var i=0; i < nodes.length; i++)
//	{
//		$("#genes_tab .genes_list").append(
//			"<li> " + nodes[i].data.id + "</li>");
//	}
	
	
	$("#genes_tab").append('<table></table>');
	
	for (var i=0; i < nodes.length; i++)
	{
		var shortId = _shortId(nodes[i].data.id);
		
		$("#genes_tab table").append( '<tr><td>' +
			'<input type="checkbox" id="' + nodes[i].data.id + 
			'" onClick="handleCheckEvent(\'' + nodes[i].data.id + '\')" >' + 
			'<label>' + shortId + '</label>' +
			'</input>' + '</td></tr>');
	}
}
*/

function _initGenesTab()
{
	// init buttons
	
	$("#filter_genes").button({icons: {primary: 'ui-icon-circle-minus'},
		text: false});
	
	$("#crop_genes").button({icons: {primary: 'ui-icon-crop'},
		text: false});
	
	$("#unhide_genes").button({icons: {primary: 'ui-icon-circle-plus'},
		text: false});
	
	$("#search_genes").button({icons: {primary: 'ui-icon-search'},
		text: false});
	
	$("#update_edges").button({icons: {primary: 'ui-icon-refresh'},
		text: false});
	
	// re-submit button is initially disabled
	$("#re-submit_query").button({icons: {primary: 'ui-icon-play'},
		text: false,
		disabled: true});
	
	// $("#re-run_query").button({label: "Re-run query with selected genes"});
	
	// apply tiptip to all buttons on the network tabs
	$("#network_tabs button").tipTip({edgeOffset:8});
}


/**
 * Refreshes the content of the genes tab, by populating the list with visible
 * (i.e. non-filtered) genes.
 */
function _refreshGenesTab()
{
	// get visible genes
	var geneList = _visibleGenes();
	
	// clear old content
	$("#gene_list_area select").remove();
	
	$("#gene_list_area").append('<select multiple></select>');
		
	// add new content
	
	for (var i=0; i < geneList.length; i++)
	{
		// use the safe version of the gene id as an id of an HTML object
		var safeId = _safeProperty(geneList[i].data.id);
		
		var classContent;
		
		if (geneList[i].data["IN_QUERY"] == "true")
		{
			classContent = 'class="in-query" ';
		}
		else
		{
			classContent = 'class="not-in-query" ';
		}
		
		$("#gene_list_area select").append(
			'<option id="' + safeId + '" ' +
			classContent + 
			'value="' + geneList[i].data.id + '" ' + '>' + 
			'<label>' + geneList[i].data.label + '</label>' +
			'</option>');
		
		// add double click listener for each gene
		$("#genes_tab #" + safeId).dblclick(showGeneDetails);
		
		// TODO qtip does not work with Chrome&IE because of the restrictions of
		// the <select><option> structure.
		/*
		var qtipOpts =
		{
			content: "id: " + safeId,
			position:
			{
				corner:
				{
					tooltip: 'bottomRight', // the corner
					target: 'topLeft' // opposite corner
				}
			},
			style:
			{
				border:
				{
                     width: 3,
                     radius: 10
				},
				padding: 10,
				textAlign: 'center',
				'font-size': '10pt',
				tip: true // speech bubble tip with automatic corner detection
				//name: 'cream' // preset 'cream' style
			}
		};
		*/
		
		// TODO try tipTip?
		//$("#genes_tab #" + safeId).qtip(qtipOpts);
	}
	
	// add change listener to the select box
	$("#gene_list_area select").change(updateSelectedGenes);
	
	if (_isIE())
	{
		// listeners on <option> elements do not work in IE, therefore add 
		// double click listener to the select box
		$("#gene_list_area select").dblclick(showGeneDetails);
		
		// TODO if multiple genes are selected, double click always shows
		// the first selected genes details in IE
	}
}

/**
 * Refreshes the content of the relations tab, by calculating percentages for
 * each edge type.
 */
function _refreshRelationsTab()
{
	var edges = _vis.edges();

	// initialize percentages of each edge type
	var percentages = new Array();
	
	percentages[IN_SAME_COMPONENT] = 0;
	percentages[REACTS_WITH] = 0;
	percentages[STATE_CHANGE] = 0;	
	
	// for each edge increment count of the correct edge type 
	for (var i=0; i < edges.length; i++)
	{
		percentages[edges[i].data.type] += 1;
	}
	
	percentages[OTHER] = edges.length -
		(percentages[IN_SAME_COMPONENT] +
		percentages[REACTS_WITH] +
		percentages[STATE_CHANGE]);
	
	if (percentages[OTHER] == 0)
	{
		// do not display OTHER if its percentage is zero
		_setComponentVis($("#relations_tab .other"), false);
		
		// also do not display it in the edge legend
		//_setComponentVis($("#edge_legend .other"), false);
	}
	else
	{
		_setComponentVis($("#relations_tab .other"), true);
		//_setComponentVis($("#edge_legend .other"), true);
	}
	
	// calculate percentages and add content to the tab 
	
	var percent;
	
	percent = (percentages[IN_SAME_COMPONENT] * 100 / edges.length);
	
	$("#relations_tab .in-same-component .percent-bar").css(
		"width", Math.ceil(percent * 0.85) + "%");
	
	$("#relations_tab .in-same-component .percent-bar").css(
		"background-color", "#CD976B");
	
	$("#relations_tab .in-same-component .percent-value").text(
		percent.toFixed(1) + "%");
	
	percent = (percentages[REACTS_WITH] * 100 / edges.length);
	
	$("#relations_tab .reacts-with .percent-bar").css(
		"width", Math.ceil(percent * 0.85) + "%");
	
	$("#relations_tab .reacts-with .percent-bar").css(
		"background-color", "#7B7EF7");
	
	$("#relations_tab .reacts-with .percent-value").text(
		percent.toFixed(1) + "%");
	
	percent = (percentages[STATE_CHANGE] * 100 / edges.length);
	
	$("#relations_tab .state-change .percent-bar").css(
		"width", Math.ceil(percent * 0.85) + "%");
		
	$("#relations_tab .state-change .percent-bar").css(
		"background-color", "#67C1A9");
	
	$("#relations_tab .state-change .percent-value").text(
		percent.toFixed(1) + "%");
	
	percent = (percentages[OTHER] * 100 / edges.length);
	
	$("#relations_tab .other .percent-bar").css(
		"width", Math.ceil(percent * 0.85) + "%");
		
	$("#relations_tab .other .percent-bar").css(
		"background-color", "#A583AB");
	
	$("#relations_tab .other .percent-value").text(
		percent.toFixed(1) + "%");
	
	// TODO remove old source filters?
	//$("#relations_tab #edge_source_filter tr").remove();
	
	// add source filtering options
	
	for (var key in _edgeSourceVisibility)
	{
		$("#relations_tab #edge_source_filter").append(
			'<tr class="' + _safeProperty(key) + '">' +
			'<td class="edge-source-checkbox">' +
			'<input type="checkbox" checked="checked">' +
			'<label>' + key + '</label>' +
			'</td></tr>');
	}
	
	// <tr class="unknown">
	//		<td class="edge-source-checkbox">
	//				<input type="checkbox" checked="checked">
	//				<label> Unknown </label>
	//		</td>
	// </tr>
}


/**
 * Creates a map (an array) with <command, function> pairs. Also, adds listener
 * functions for the buttons and for the CytoscapeWeb canvas.
 */
function _initControlFunctions()
{
	_controlFunctions = new Array();
	
	//_controlFunctions["hide_selected"] = _hideSelected;
	_controlFunctions["hide_selected"] = filterSelectedGenes;
	_controlFunctions["unhide_all"] = _unhideAll;
	_controlFunctions["perform_layout"] = _performLayout;
	_controlFunctions["show_node_labels"] = _toggleNodeLabels;
	//_controlFunctions["show_edge_labels"] = _toggleEdgeLabels;
	_controlFunctions["merge_links"] = _toggleMerge;
	_controlFunctions["show_pan_zoom_control"] = _togglePanZoom;
	_controlFunctions["auto_layout"] = _toggleAutoLayout;
	_controlFunctions["remove_disconnected"] = _toggleRemoveDisconnected;
	_controlFunctions["show_profile_data"] = _toggleProfileData;
	_controlFunctions["save_as_png"] = _saveAsPng;
	//_controlFunctions["save_as_svg"] = _saveAsSvg;
	_controlFunctions["layout_properties"] = _openProperties;
	_controlFunctions["highlight_neighbors"] = _highlightNeighbors;
	_controlFunctions["remove_highlights"] = _removeHighlights;
	_controlFunctions["hide_non_selected"] = filterNonSelected;
	_controlFunctions["node_legend"] = _showNodeLegend;
	_controlFunctions["edge_legend"] = _showEdgeLegend;
	
	
	
	// add button listeners
	
	$("#save_layout_settings").click(saveSettings);
	$("#default_layout_settings").click(defaultSettings);
	
	$("#search_genes").click(searchGene);
	$("#genes_tab #search_box").keypress(_keyPressListener);
	$("#filter_genes").click(filterSelectedGenes);
	$("#crop_genes").click(filterNonSelected);
	$("#unhide_genes").click(_unhideAll);
	$("#re-submit_query").click(reRunQuery);
	
	$("#update_edges").click(updateEdges);
	
	
	// add listener for double click action
	
	_vis.addListener("dblclick",
		"nodes",
		showNodeInspector);
	
	_vis.addListener("dblclick",
		"edges",
		showEdgeInspector);
	
	// add listener for node select & deselect actions
	
	_vis.addListener("select",
		"nodes",
		updateGenesTab);
	
	_vis.addListener("deselect",
		"nodes",
		updateGenesTab);
	
	// TODO temp debug option, remove when done
	//_vis.addContextMenuItem("node details", "nodes", jokerAction);
}

/**
 * Initializes the layout options by default values and updates the
 * corresponding UI content.
 */
function _initLayoutOptions()
{
	_layoutOptions = _defaultOptsArray();
	_updateLayoutOptions();
}

/**
 * Hides (filters) selected nodes and edges.
 */
function _hideSelected()
{
	// update selected elements map
	_selectedElements = _selectedElementsMap("all");
	
	// filter out selected elements
    _vis.filter('all', selectionVisibility);
    
    // also, filter disconnected nodes if necessary
    _filterDisconnected();
    
    // refresh genes tab
    _refreshGenesTab();
    
    // visualization changed, perform layout if necessary
	_visChanged();
}

/**
 * Removes any existing filters to unhide filtered nodes & edges. However, 
 * this operation does not remove the filtering based on edge types.
 */
function _unhideAll()
{
	// remove all filters
	_vis.removeFilter(null);
	
	// reset array of already filtered elements
	_alreadyFiltered = new Array();
	
	// reset slider UI
	$("#weight_slider_field").val(0.0);
	$("#weight_slider_bar").slider("option",
		"value", 0);
	
	// re-apply filtering based on edge types
	updateEdges();
	
	// refresh & update genes tab 
	_refreshGenesTab();
	updateGenesTab();

	// no need to call _visChanged(), since it is already called by updateEdges
	//_visChanged();
}

/**
 * Creates an array of visible (i.e. non-filtered) genes.
 * 
 * @return		array of visible genes
 */
function _visibleGenes()
{
	var genes = new Array();
	var nodes = _vis.nodes();

    for (var i=0; i < nodes.length; i++)
    {
    	// check if the node is already filtered.
    	// also, include only genes, not small molecules or unknown types.
    	if (_alreadyFiltered[nodes[i].data.id] == null &&
    		nodes[i].data.type == PROTEIN)
    	{
    		genes.push(nodes[i]);
    	}
    }
    
    // sort genes by label (alphabetically)
    genes.sort(_geneSort);
    
    return genes;
}

/**
 * Comparison function to sort genes alphabetically.
 * 
 * @param node1	node to compare to node2
 * @param node2 node to compare to node1
 * @return 		positive integer if node1 is alphabetically greater than node2
 * 				negative integer if node2 is alphabetically greater than node1
 * 				zero if node1 and node2 are alphabetically equal
 */
function _geneSort(node1, node2)
{
	if (node1.data.label > node2.data.label)
	{
		return 1;
	}
	else if (node1.data.label < node2.data.label)
	{
		return -1;
	}
	else
	{
		return 0;
	}
}

/**
 * Performs the current layout on the graph.
 */
function _performLayout()
{
	_vis.layout(_graphLayout);
}

/**
 * Temporary function for debugging purposes
 */
function jokerAction(evt)
{
	var node = evt.target;		
	str = _nodeDetails(node);		
	alert(str);	
}

/**
 * Temporary function for debugging purposes
 */
function _nodeDetails(node)
{
	var str = "";
	
	if (node != null)
	{
		str += "fields: ";
		
		for (var field in node)
		{
			str += field + ";";
		}
		
		str += "\n";
		//str += "data len: " + node.data.length " \n";
		str += "data: \n";
		
		
		for (var field in node.data)
		{
			str += field + ": " +  node.data[field] + "\n";
		}
	}
	
	str += "short id: " + _shortId(node.data.id) + "\n";
	str += "safe id: " + _safeProperty(node.data.id) + "\n";
	
	return str;
}

/**
 * Toggles the visibility of the node labels.
 */
function _toggleNodeLabels()
{
	// update visibility of labels 
	
	_nodeLabelsVisible = !_nodeLabelsVisible;
	_vis.nodeLabelsVisible(_nodeLabelsVisible);
	
	// update check icon of the corresponding menu item
	
	var item = $("#show_node_labels");
	
	if (_nodeLabelsVisible)
	{
		item.addClass(CHECKED_CLASS);
	}
	else
	{
		item.removeClass(CHECKED_CLASS);
	}
}

/**
 * Toggles the visibility of the edge labels.
 */
function _toggleEdgeLabels()
{
	// update visibility of labels 
	
	_edgeLabelsVisible = !_edgeLabelsVisible;
	_vis.edgeLabelsVisible(_edgeLabelsVisible);
	
	// update check icon of the corresponding menu item
	
	var item = $("#show_edge_labels");
	
	if (_edgeLabelsVisible)
	{
		item.addClass(CHECKED_CLASS);
	}
	else
	{
		item.removeClass(CHECKED_CLASS);
	}
}

/**
 * Toggles the visibility of the pan/zoom control panel.
 */
function _togglePanZoom()
{
	// update visibility of the pan/zoom control
	
	_panZoomVisible = !_panZoomVisible;
	
	_vis.panZoomControlVisible(_panZoomVisible);
	
	// update check icon of the corresponding menu item
	
	var item = $("#show_pan_zoom_control");
	
	if (_panZoomVisible)
	{
		item.addClass(CHECKED_CLASS);
	}
	else
	{
		item.removeClass(CHECKED_CLASS);
	}
}

/**
 * Merges the edges, if not merged. If edges are already merges, then show all
 * edges.
 */
function _toggleMerge()
{
	// merge/unmerge the edges
	
	_linksMerged = !_linksMerged;
	
	_vis.edgesMerged(_linksMerged);
	
	// update check icon of the corresponding menu item
	
	var item = $("#merge_links");
	
	if (_linksMerged)
	{
		item.addClass(CHECKED_CLASS);
	}
	else
	{
		item.removeClass(CHECKED_CLASS);
	}
}

/**
 * Toggle auto layout option on or off. If auto layout is active, then the
 * graph is laid out automatically upon any change.
 */
function _toggleAutoLayout()
{
	// toggle autoLayout option
	
	_autoLayout = !_autoLayout;
	
	// update check icon of the corresponding menu item
	
	var item = $("#auto_layout");
	
	if (_autoLayout)
	{
		item.addClass(CHECKED_CLASS);
	}
	else
	{
		item.removeClass(CHECKED_CLASS);
	}
}

/**
 * Toggle "remove disconnected on hide" option on or off. If this option is 
 * active, then any disconnected node will also be hidden after the hide action.
 */
function _toggleRemoveDisconnected()
{
	// toggle removeDisconnected option
	
	_removeDisconnected = !_removeDisconnected;
	
	// update check icon of the corresponding menu item
	
	var item = $("#remove_disconnected");
	
	if (_removeDisconnected)
	{
		item.addClass(CHECKED_CLASS);
	}
	else
	{
		item.removeClass(CHECKED_CLASS);
	}
}

/**
 * Toggles the visibility of the profile data for the nodes.
 */
function _toggleProfileData()
{
    // toggle value and pass to CW

    _profileDataVisible = !_profileDataVisible;
    _vis.profileDataAlwaysShown(_profileDataVisible);

	// update check icon of the corresponding menu item
	
	var item = $("#show_profile_data");
	
	if (_profileDataVisible)
	{
		item.addClass(CHECKED_CLASS);
	}
	else
	{
		item.removeClass(CHECKED_CLASS);
	}
}

/**
 * Saves the network as a PNG image.
 */
function _saveAsPng()
{
	_vis.exportNetwork('png', 'export_network.jsp?type=png');
}

/**
 * Saves the network as a SVG image.
 */
function _saveAsSvg()
{
	_vis.exportNetwork('svg', 'export_network.jsp?type=svg');
}

/**
 * Displays the layout properties panel.
 */
function _openProperties()
{	
	_updatePropsUI();
	$("#settings_dialog").dialog("open").height("auto");
}

/**
 * Initializes the layout settings panel.
 */
function _initPropsUI()
{
	$("#fd_layout_settings tr").tipTip();
}

/**
 * Updates the contents of the layout properties panel.
 */
function _updatePropsUI()
{
	// update settings panel UI
	
	for (var i=0; i < _layoutOptions.length; i++)
	{
//		if (_layoutOptions[i].id == "weightNorm")
//		{
//			// clean all selections
//			$("#norm_linear").removeAttr("selected");
//			$("#norm_invlinear").removeAttr("selected");
//			$("#norm_log").removeAttr("selected");
//			
//			// set the correct option as selected
//			
//			$("#norm_" + _layoutOptions[i].value).attr("selected", "selected");
//		}
		
		if (_layoutOptions[i].id == "autoStabilize")
		{
			if (_layoutOptions[i].value == true)
			{
				// check the box
				$("#autoStabilize").attr("checked", true);
				$("#autoStabilize").val(true);
			}
			else
			{
				// uncheck the box
				$("#autoStabilize").attr("checked", false);
				$("#autoStabilize").val(false);
			}
		}
		else
		{
			$("#" + _layoutOptions[i].id).val(_layoutOptions[i].value);
		}
	}
}

/**
 * Updates the graphLayout options for CytoscapeWeb.
 */
function _updateLayoutOptions()
{
	// update graphLayout object
	
	var options = new Object();
	
	for (var i=0; i < _layoutOptions.length; i++)
	{
		options[_layoutOptions[i].id] = _layoutOptions[i].value;
	}
	
	_graphLayout.options = options;
}



/*
 * ##################################################################
 * ##################### Utility Functions ##########################
 * ##################################################################
 */

/**
 * Generates a shortened version of the given node id.
 * 
 * @param id	id of a node
 * @return		a shortened version of the id
 */
function _shortId(id)
{
	var shortId = id;
	
	if (id.indexOf("#") != -1)
	{
		var pieces = id.split("#");
		shortId = pieces[pieces.length - 1];
	}
	else if (id.indexOf(":") != -1)
	{
		var pieces = id.split(":");
		shortId = pieces[pieces.length - 1];
	}
	
	return shortId;
}

/**
 * Replaces all occurrences of a problematic character with an under dash.
 * Those characters cause problems with the properties of an HTML object.
 * 
 * @param str	string to be modified
 * @return		safe version of the given string
 */
function _safeProperty(str)
{
	var safeProperty = str;
	
	safeProperty = _replaceAll(safeProperty, " ", "_");
	safeProperty = _replaceAll(safeProperty, "/", "_");
	safeProperty = _replaceAll(safeProperty, "\\", "_");
	safeProperty = _replaceAll(safeProperty, "#", "_");
	safeProperty = _replaceAll(safeProperty, ".", "_");
	safeProperty = _replaceAll(safeProperty, ":", "_");
	safeProperty = _replaceAll(safeProperty, ";", "_");
	safeProperty = _replaceAll(safeProperty, '"', "_");
	safeProperty = _replaceAll(safeProperty, "'", "_");
	
	return safeProperty;
}

/**
 * Replaces all occurrences of the given string in the source string.
 * 
 * @param source		string to be modified
 * @param toFind		string to match
 * @param toReplace		string to be replaced with the matched string
 * @return				modified version of the source string
 */
function _replaceAll(source, toFind, toReplace)
{
	var target = source;
	var index = target.indexOf(toFind);

	while (index != -1)
	{
		target = target.replace(toFind, toReplace);
		index = target.indexOf(toFind);
	}

	return target;
}

/**
 * Checks if the user browser is IE.
 * 
 * @return	true if IE, false otherwise
 */
function _isIE()
{
	var result = false;
	
	if (navigator.appName.toLowerCase().indexOf("microsoft") != -1)
	{
		result = true;
	}
	
	return result;
}

/**
 * Converts the given string to title case format. Also replaces each
 * underdash with a space.
 * 
 * @param source	source string to be converted to title case
 */
function _toTitleCase(source)
{
	var str;
	
	if (source == null)
	{
		return source;
	}
	
	// first, trim the string
	str = source.replace(/\s+$/, "");
	
	// replace each underdash with a space
	str = _replaceAll(str, "_", " ");
	
	// change to lower case
	str = str.toLowerCase();
	
	// capitalize starting character of each word
	
	var titleCase = new Array();
	
	titleCase.push(str.charAt(0).toUpperCase());
	
	for (var i = 1; i < str.length; i++)
	{
		if (str.charAt(i-1) == ' ')
		{
			titleCase.push(str.charAt(i).toUpperCase());
		}
		else
		{
			titleCase.push(str.charAt(i));
		}
	}
	
	return titleCase.join("");
}

/*
function _transformIntervalValue(value, sourceInterval, targetInterval)
{ 
	var sourceRange = sourceInterval.end - sourceInterval.start;
	var targetRange = targetInterval.end - targetInterval.start;
	
	var transformed = targetInterval.start + 
		(value - sourceInterval.start) * (targetRange / sourceRange);
	
	return transformed;
}
*/

/**
 * Finds and returns the maximum value in a given map.
 * 
 * @param map	map that contains real numbers
 */
function _getMaxValue(map)
{
	var max = 0.0;
	
	for (var key in map)
	{
		if (map[key] > max)
		{
			max = map[key];
		}
	}
	
	return max;
}

/**
 * Transforms the input value by using the function: 
 * y = (0.000230926)x^3 - (0.0182175)x^2 + (0.511788)x
 * 
 * This function is designed to transform slider input, which is between
 * 0 and 100, to provide a better filtering.
 * 
 * @param value		input value to be transformed
 */
function _transformValue(value)
{
	// previous function: y = (0.000166377)x^3 - (0.0118428)x^2 + (0.520007)x
	
	var transformed = 0.000230926 * Math.pow(value, 3) -
		0.0182175 * Math.pow(value, 2) +
		0.511788 * value;
	
	if (transformed < 0)
	{
		transformed = 0;
	}
	else if (transformed > 100)
	{
		transformed = 100;
	}
	
	return transformed;
}

/**
 * Transforms the given value by solving the equation
 * 
 *   y = (0.000230926)x^3 - (0.0182175)x^2 + (0.511788)x
 * 
 * where y = value
 * 
 * @param value	value to be reverse transformed
 * @returns		reverse transformed value
 */
function _reverseTransformValue(value)
{
	// find x, where y = value
	
	var reverse = _solveCubic(0.000230926,
		-0.0182175,
		0.511788,
		-value);
	
	return reverse;
}

/**
 * Solves the cubic function
 * 
 *   a(x^3) + b(x^2) + c(x) + d = 0
 *    
 * by using the following formula
 * 
 *   x = {q + [q^2 + (r-p^2)^3]^(1/2)}^(1/3) + {q - [q^2 + (r-p^2)^3]^(1/2)}^(1/3) + p
 *   
 * where
 * 
 *   p = -b/(3a), q = p^3 + (bc-3ad)/(6a^2), r = c/(3a)
 * 
 * @param a	coefficient of the term x^3
 * @param b	coefficient of the term x^2
 * @param c coefficient of the term x^1
 * @param d coefficient of the term x^0
 * 
 * @returns one of the roots of the cubic function
 */
function _solveCubic(a, b, c, d)
{
	var p = (-b) / (3*a);
	var q = Math.pow(p, 3) + (b*c - 3*a*d) / (6 * Math.pow(a,2));
	var r = c / (3*a);
	
	//alert(q*q + Math.pow(r - p*p, 3));
	
	var sqrt = Math.pow(q*q + Math.pow(r - p*p, 3), 1/2);
	
	//var root = Math.pow(q + sqrt, 1/3) +
	//	Math.pow(q - sqrt, 1/3) +
	//	p;
	
	var x = _cubeRoot(q + sqrt) +
		_cubeRoot(q - sqrt) +
		p;
	
	return x;
}

/**
 * Evaluates the cube root of the given value. This function also handles
 * negative values unlike the built-in Math.pow() function.
 * 
 * @param value	source value
 * @returns		cube root of the source value
 */
function _cubeRoot(value)
{
	var root = Math.pow(Math.abs(value), 1/3);
	
	if (value < 0)
	{
		root = -root;
	}
	
	return root;
}

// TODO get the x-coordinate of the event target (with respect to the window). 
function _mouseX(evt)
{
	if (evt.pageX)
	{
		return evt.pageX;
	}
	else if (evt.clientX)
	{
		return evt.clientX + (document.documentElement.scrollLeft ?
			   document.documentElement.scrollLeft :
				   document.body.scrollLeft);
	}	
	else
	{
		return 0;
	}
}

//TODO get the y-coordinate of the event target (with respect to the window).
function _mouseY(evt)
{
	if (evt.pageY)
	{
		return evt.pageY;
	}
	else if (evt.clientY)
	{
		return evt.clientY + (document.documentElement.scrollTop ?
			   document.documentElement.scrollTop :
				   document.body.scrollTop);
	}
	else
	{
		return 0;
	}
}
