// flags
var _autoLayout;
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

// node type constants
var PROTEIN = "Protein";
var SMALL_MOLECULE = "SmallMolecule";
var UNKNOWN = "Unknown";

// class constants for css visualization
var CHECKED_CLASS = "checked-menu-item";
var MENU_SEPARATOR_CLASS = "separator-menu-item";
var FIRST_CLASS = "first-menu-item";
var LAST_CLASS = "last-menu-item";
var MENU_CLASS = "main-menu-item";
var SUB_MENU_CLASS = "sub-menu-item";
var HOVERED_CLASS = "hovered-menu-item";
var PERCENT_SEPARATOR_CLASS = "percent-separator";

// name of the graph layout
var _graphLayout = {name: "ForceDirected"};

// force directed layout options
var _layoutOptions;

// array of selected elements, used by the visibility function for filtering
var _selectedElements;

// array of previously filtered elements
var _alreadyFiltered;

// array of filtered edge types
var _edgeTypeVisibility;

// map used to resolve cross-references
var _linkMap;

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
	//TODO debug for IE (alert does not work use prompt instead!)
	//prompt("initing network");
	
	_vis = vis;
	_linkMap = _xrefArray();
	_alreadyFiltered = new Array();
	_edgeTypeVisibility = _edgeTypeArray();
	_resetFlags();
	
	_initControlFunctions();
	_initLayoutOptions();

	_initMainMenu();
	_initDialogs();

	// init tabs
	$("#network_tabs").tabs();
	_refreshGenesTab();
	_refreshRelationsTab();

	// make UI visible
	_setVisibility(true);
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
	$("#genes_tab select option").each(
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
		if (_layoutOptions[i].id == "weightNorm")
		{
			// find the selected option and update the corresponding value
			
			if ($("#norm_linear").is(":selected"))
			{
				_layoutOptions[i].value = $("#norm_linear").val(); 
			}
			else if ($("#norm_invlinear").is(":selected"))
			{
				_layoutOptions[i].value = $("#norm_invlinear").val(); 
			}
			else if ($("#norm_log").is(":selected"))
			{
				_layoutOptions[i].value = $("#norm_log").val(); 
			}
		}
		else if (_layoutOptions[i].id == "autoStabilize")
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
	
	_updateNodeInspectorContent(data);
	
	// open inspector panel
	$("#node_inspector").dialog("open");
}

/**
 * Updates the content of the node inspector with respect to the provided data.
 * Data is assumed to be the data of a node.
 * 
 * @param data	node data containing necessary fields
 */
function _updateNodeInspectorContent(data)
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
	
	$("#node_inspector_content .data .data-row").remove();
	$("#node_inspector_content .xref .xref-row").remove();
	$("#node_inspector_content .profile .percent-row").remove();
	$("#node_inspector_content .profile-header .header-row").remove();
	
	// add id
	
	_addDataRow("node", "ID", data.id);
	
	if (data.type == PROTEIN)
	{
		_addDataRow("node", "Gene Symbol", data.label);
		// TODO add description?
		//_addDataRow("node", "Description", "TODO");
		_addDataRow("node", "Gene specified by user", data.IN_QUERY);
	
		// add percentage information
		_addPercentages(data);
	}
	
	// add cross references
	
	var links = new Array();
	
	for (var field in data)
	{
		if (field == "xref")
		{
			// parse the xref data, and construct the link and its label
			
			var link;
			
			if (data[field] != null)
			{
				link = _resolveXref(data[field]);
				links.push(link);
			}
		}
	}
	
	if (links.length > 0)
	{
		$("#node_inspector_content .xref").append(
				'<tr class="xref-row"><td><strong>More at: </strong></td></tr>');
		
		_addXrefEntry('node', links[0].href, links[0].text);
	}
	
	for (var i=1; i < links.length; i++)
	{
		$("#node_inspector_content .xref-row td").append(', ');
		_addXrefEntry('node', links[i].href, links[i].text);
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
	
	$("#node_inspector .profile-header").append('<tr class="header-row">' +
		'<td><div class="total-alteration">Genomic Profiles:</div></td></tr>');
	
	// add total alteration frequency
	
	percent = (data["PERCENT_ALTERED"] * 100);
	
	var row = '<tr class="percent-row">' +
		'<td><div class="total-alteration">Total Alteration</div></td>' +
		'<td class="percent-cell"></td>' +
		'<td><div class="percent-value">' + percent.toFixed(1) + '%</div></td>' +
		'</tr>';

	$("#node_inspector .profile").append(row);

	// add other percentages
	
	if (data["PERCENT_CNA_AMPLIFIED"] != null)
	{
		percent = (data["PERCENT_CNA_AMPLIFIED"] * 100);
		_addPercentRow("cna-amplified", "Amplification", percent, "#FF2500");
		$("#node_inspector .profile .cna-amplified").addClass(PERCENT_SEPARATOR_CLASS);
		
		percent = (data["PERCENT_CNA_GAINED"] * 100);
		_addPercentRow("cna-gained", "Gain", percent, "#FFC5CC");
		
		percent = (data["PERCENT_CNA_HOMOZYGOUSLY_DELETED"] * 100);
		_addPercentRow("cna-homozygously-deleted", "Homozygous Deletion", percent, "#0332FF");
		
		percent = (data["PERCENT_CNA_HEMIZYGOUSLY_DELETED"] * 100);
		_addPercentRow("cna-hemizygously-deleted", "Hemizygous Deletion", percent, "#9EDFE0");
	}
	
	if (data["PERCENT_MRNA_WAY_DOWN"] != null)
	{
		percent = (data["PERCENT_MRNA_WAY_DOWN"] * 100);
		_addPercentRow("mrna-way-down", "Up-regulation", percent, "#FFACA9");
		$("#node_inspector .profile .mrna-way-down").addClass(PERCENT_SEPARATOR_CLASS);
		
		percent = (data["PERCENT_MRNA_WAY_UP"] * 100);
		_addPercentRow("mrna-way-up", "Down-regulation", percent, "#78AAD6");
	}
	
	if (data["PERCENT_MUTATED"] != null)
	{
		percent = (data["PERCENT_MUTATED"] * 100);
		_addPercentRow("mutated", "Mutation", percent, "#008F00");
		$("#node_inspector .profile .mutated").addClass(PERCENT_SEPARATOR_CLASS);
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
		"width", (parseInt(percent) + 1) + "%");
	
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
	
	// set title
	$("#edge_inspector").dialog("option",
		"title",
		_vis.node(data.source).data.label + " -> " + 
		_vis.node(data.target).data.label);
	
	// clean xref & data rows
	$("#edge_inspector_content .data .data-row").remove();
	
	_addDataRow("edge", "Source", data["INTERACTION_DATA_SOURCE"]);
	_addDataRow("edge", "Type", data["type"]);
	
	if (data["INTERACTION_PUBMED_ID"] != null)
	{
		_addDataRow("edge", "PubMed ID", data["INTERACTION_PUBMED_ID"]);
	}
	
	// open inspector panel
	$("#edge_inspector").dialog("open");
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
	_updateNodeInspectorContent(node.data);
	
	$("#node_inspector").dialog("open");
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
	if(!_selectFromTab)
	{
		var selected = _vis.selected("nodes");
		
		// deselect all options
		$("#genes_tab select option").each(
			function(index)
			{
				$(this).removeAttr("selected");
			});
		
		// select options for selected nodes
		for (var i=0; i < selected.length; i++)
		{
			$("#" +  _safeId(selected[i].data.id)).attr(
				"selected", "selected");
		}
	}
}

/**
 * Searches for genes by using the input provided within the search text field.
 * Also, selects matching genes both from the canvas and gene list.
 */
function searchGene()
{
	var query = $("#genes_tab #search").val();
	
	var genes = _visibleGenes();
	var matched = new Array();
	var i;
	
	for (i=0; i < genes.length; i++)
	{
		if (genes[i].data.label.toLowerCase().indexOf(
			query.toLowerCase()) != -1)
		{
			matched.push(genes[i].data.id);
		}
		else if (genes[i].data.id.toLowerCase().indexOf(
			query.toLowerCase()) != -1)
		{
			matched.push(genes[i].data.id);
		}
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
	// update selected elements
	_selectedElements = _vis.selected("nodes");

	// filter out selected elements
    _vis.filter("nodes", visibility, true);
    
    // refresh genes tab
    _refreshGenesTab();
    
    // visualization changed, perform layout if necessary
	_visChanged();
}

/**
 * Filters out all non-selected genes.
 */
function filterNonSelectedGenes()
{
	// update selected elements
	_selectedElements = _vis.selected("nodes");

	// filter out non-selected elements
    _vis.filter('nodes', geneVisibility, true);
    
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
		$(".in-same-component input").is(":checked");
	
	_edgeTypeVisibility[REACTS_WITH] =
		$(".reacts-with input").is(":checked");
	
	_edgeTypeVisibility[STATE_CHANGE] =
		$(".state-change input").is(":checked");
	
	// remove current edge filters
	_vis.removeFilter("edges");
	
	// filter selected types
	_vis.filter("edges", edgeVisibility, true);
	
	// visualization changed, perform layout if necessary
	_visChanged();
}

/**
 * Determines the visibility of an edge for filtering purposes.
 * 
 * @param element	egde to be checked for visibility criteria
 * @return			true if the edge should be visible, false otherwise
 */
function edgeVisibility(element)
{
	var visible;
	
	// if an element is already filtered then it should remain invisible
	if (_alreadyFiltered[element.data.id] != null)
	{
		visible = false;
	}
	// unknown edge type, do not filter
	else if (_edgeTypeVisibility[element.data.type] == null)
	{
		visible = true;
	}
	// check for the visibility of the edge type
	else
	{
		visible = _edgeTypeVisibility[element.data.type];
	}
	
	return visible;
}

/**
 * Determines the visibility of a gene (node) for filtering purposes.
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
		// TODO find a better way (?) to check if it is selected.
		
		// filter non-selected nodes
		
		for (var i=0; i < _selectedElements.length; i++)
		{
			if (element.data.id == _selectedElements[i].data.id)
			{
				visible = true;
				break;
			}
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
 * This function returns false if the given graph element is selected,
 * returns true otherwise. This function is used to hide (filter) selected
 * nodes & edges.
 * 
 * @param element	element to be checked
 * @return			false if selected, true otherwise
 */
function visibility(element)
{
	// if an element is already filtered then it should remain invisible
	// until the filters are reset
	if (_alreadyFiltered[element.data.id] != null)
	{
		return false;
	}
	// if an edge type is hidden all edges of that type should be invisible
	else if (_edgeTypeVisibility[element.data.type] != null
			&& !_edgeTypeVisibility[element.data.type])
	{
		return false;
	}
	
	// TODO find a better way (?) to check if it is selected, do not traverse
	// all selected elements
	
	for (var i=0; i < _selectedElements.length; i++)
	{
		if (element.data.id == _selectedElements[i].data.id)
		{
			_alreadyFiltered[element.data.id] = element;
			return false;
		}
	}
	
	return true;
}

/**
 * Performs layout if auto layout flag is set. 
 */
function _visChanged()
{
	if (_autoLayout)
	{
		// re-apply layout
		_performLayout();
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
 * Adds a data row to the node or edge inspector.
 * 
 * @param type		type of the inspector (should be "node" or "edge")
 * @param label		label of the data field
 * @param value		value of the data field
 */
function _addDataRow(type, label, value)
{
	$("#" + type + "_inspector_content .data").append(
		'<tr class="data-row"><td>' +
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
		link.href = _linkMap[pieces[0].toLowerCase()] + "" + pieces[1];
		link.text = xref;
	}
	
	return link;
}

/**
 * Set default values of the control flags.
 */
function _resetFlags()
{
	_autoLayout = false;
	_nodeLabelsVisible = true;
	_edgeLabelsVisible = false;
	_panZoomVisible = true;
	_linksMerged = true;
	_profileDataVisible = false;
	_selectFromTab = false;
}

/**
 * Set visibility of the UI.
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
			$("#network_tabs").removeClass("hidden-network-ui");
			$("#node_inspector").removeClass("hidden-network-ui");
			$("#edge_inspector").removeClass("hidden-network-ui");
			$("#settings_dialog").removeClass("hidden-network-ui");
		}
	}
	else
	{
		if (!$("#network_menu_div").hasClass("hidden-network-ui"))
		{
			$("#network_menu_div").addClass("hidden-network-ui");
			$("#network_tabs").addClass("hidden-network-ui");
			$("#node_inspector").addClass("hidden-network-ui");
			$("#edge_inspector").addClass("hidden-network-ui");
			$("#settings_dialog").addClass("hidden-network-ui");
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
		[ { id: "gravitation", label: "Gravitation",       value: -500,   tip: "The gravitational constant. Negative values produce a repulsive force." },
		  { id: "mass",        label: "Node mass",         value: 3,      tip: "The default mass value for nodes." },
		  { id: "tension",     label: "Edge tension",      value: 0.1,    tip: "The default spring tension for edges." },
		  { id: "restLength",  label: "Edge rest length",  value: "auto", tip: "The default spring rest length for edges." },
		  { id: "drag",        label: "Drag co-efficient", value: 0.4,    tip: "The co-efficient for frictional drag forces." },
		  { id: "minDistance", label: "Minimum distance",  value: 1,      tip: "The minimum effective distance over which forces are exerted." },
		  { id: "maxDistance", label: "Maximum distance",  value: 10000,  tip: "The maximum distance over which forces are exerted." },
		  { id: "weightAttr",  label: "Weight Attribute",  value: "",  tip: "The name of the edge attribute that contains the weights." },
		  { id: "weightNorm",  label: "Weight Normalization", value: "linear",  tip: "How to interpret weight values." },
		  { id: "iterations",  label: "Iterations",        value: 400,    tip: "The number of iterations to run the simulation." },
		  { id: "maxTime",     label: "Maximum time",      value: 30000,  tip: "The maximum time to run the simulation, in milliseconds." },
		  { id: "autoStabilize", label: "Auto stabilize",  value: true,   tip: "If checked, Cytoscape Web automatically tries to stabilize results that seems unstable after running the regular iterations." } ];
	
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
	
	// TODO find missing links
	linkMap["entrez gene"] = "http://www.ncbi.nlm.nih.gov/gene?term=";	
	linkMap["hgnc"] = "http://www.genenames.org/cgi-bin/quick_search.pl?.cgifields=type&type=equals&num=50&search=";
	linkMap["nucleotide sequence database"] = "";	
	linkMap["refseq"] =	"";
	linkMap["uniprot"] = "http://www.uniprot.org/uniprot/";
	linkMap["chebi"] = "";
	
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
	
	return typeArray;
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
	
	$("#save_as_png").addClass(FIRST_CLASS);
	$("#save_as_png").addClass(MENU_SEPARATOR_CLASS);
	$("#save_as_png").addClass(LAST_CLASS);
	
	$("#hide_selected").addClass(FIRST_CLASS);
	$("#hide_selected").addClass(MENU_SEPARATOR_CLASS);	
	$("#auto_layout").addClass(MENU_SEPARATOR_CLASS);
	$("#auto_layout").addClass(LAST_CLASS);
	
	$("#perform_layout").addClass(FIRST_CLASS);
	$("#perform_layout").addClass(MENU_SEPARATOR_CLASS);
	//$("#layout_properties").addClass(SUB_MENU_CLASS);
	$("#show_profile_data").addClass(MENU_SEPARATOR_CLASS);
	$("#highlight_neighbors").addClass(MENU_SEPARATOR_CLASS);
	$("#merge_links").addClass(MENU_SEPARATOR_CLASS);
	$("#show_pan_zoom_control").addClass(LAST_CLASS);
	
	// init check icons for checkable menu items
	
	if (_autoLayout)
	{
		$("#auto_layout").addClass(CHECKED_CLASS);
	}
	else
	{
		$("#auto_layout").removeClass(CHECKED_CLASS);
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
		width: 400});
	
	// adjust edge inspector
	$("#edge_inspector").dialog({autoOpen: false, 
		resizable: false, 
		width: 350});
}

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

/**
 * Refreshes the content of the genes tab, by populating the list with visible
 * (i.e. non-filtered) genes.
 */
function _refreshGenesTab()
{
	// get visible genes
	var geneList = _visibleGenes();
	
	// clear old content
	$("#genes_tab select").remove();
	
	$("#genes_tab").append('<select multiple></select>');
		
	// add new content
	
	for (var i=0; i < geneList.length; i++)
	{
		// use the safe version of the gene id as an id of an HTML object
		var safeId = _safeId(geneList[i].data.id);
		
		var classContent;
		
		if (geneList[i].data["IN_QUERY"] == "true")
		{
			classContent = 'class="in-query" ';
		}
		else
		{
			classContent = 'class="not-in-query" ';
		}
		
		$("#genes_tab select").append(
			'<option id="' + safeId + '" ' +
			classContent + 
			'value="' + geneList[i].data.id + '" ' + '>' + 
			'<label>' + geneList[i].data.label + '</label>' +
			'</option>');
		
		$("#genes_tab #" + safeId).click(updateSelectedGenes);
		$("#genes_tab #" + safeId).select(updateSelectedGenes);
		$("#genes_tab #" + safeId).dblclick(showGeneDetails);
		
		// TODO qtip does not work with Chrome because of the restrictions of
		// the <select><option> structure.
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
		
		//$("#genes_tab #" + safeId).qtip(qtipOpts);
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
	
	// calculate percentages and add content to the tab 
	
	var percent;
	
	percent = (percentages[IN_SAME_COMPONENT] * 100 / edges.length);
	
	$("#relations_tab .in-same-component .percent-bar").css(
		"width", (parseInt(percent * 0.85) + 1) + "%");
	
	$("#relations_tab .in-same-component .percent-bar").css(
		"background-color", "#CD976B");
	
	$("#relations_tab .in-same-component .percent-value").text(
		percent.toFixed(1) + "%");
	
	percent = (percentages[REACTS_WITH] * 100 / edges.length);
	
	$("#relations_tab .reacts-with .percent-bar").css(
		"width", (parseInt(percent * 0.85) + 1) + "%");
	
	$("#relations_tab .reacts-with .percent-bar").css(
		"background-color", "#7B7EF7");
	
	$("#relations_tab .reacts-with .percent-value").text(
		percent.toFixed(1) + "%");
	
	percent = (percentages[STATE_CHANGE] * 100 / edges.length);
	
	$("#relations_tab .state-change .percent-bar").css(
		"width", (parseInt(percent * 0.85) + 1) + "%");
		
	$("#relations_tab .state-change .percent-bar").css(
		"background-color", "#67C1A9");
	
	$("#relations_tab .state-change .percent-value").text(
		percent.toFixed(1) + "%")
}


/**
 * Creates a map (an array) with <command, function> pairs. Also, adds listener
 * functions for the buttons and for the CytoscapeWeb canvas.
 */
function _initControlFunctions()
{
	_controlFunctions = new Array();
	
	_controlFunctions["hide_selected"] = _hideSelected;
	_controlFunctions["unhide_all"] = _unhideAll;
	_controlFunctions["perform_layout"] = _performLayout;
	_controlFunctions["show_node_labels"] = _toggleNodeLabels;
	//_controlFunctions["show_edge_labels"] = _toggleEdgeLabels;
	_controlFunctions["merge_links"] = _toggleMerge;
	_controlFunctions["show_pan_zoom_control"] = _togglePanZoom;
	_controlFunctions["auto_layout"] = _toggleAutoLayout;
	_controlFunctions["show_profile_data"] = _toggleProfileData;
	_controlFunctions["save_as_png"] = _saveAsPng;
	_controlFunctions["save_as_svg"] = _saveAsSvg;
	_controlFunctions["layout_properties"] = _openProperties;
	_controlFunctions["highlight_neighbors"] = _highlightNeighbors;
	_controlFunctions["remove_highlights"] = _removeHighlights;
	
	// TODO temp test button, remove when done
	_controlFunctions["joker_button"] = jokerAction;
	
	// add button listeners
	
	$("#save_layout_settings").click(saveSettings);
	$("#default_layout_settings").click(defaultSettings);
	
	$("#search_genes").click(searchGene);
	$("#filter_genes").click(filterSelectedGenes);
	$("#crop_genes").click(filterNonSelectedGenes);
	
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
	// update selected elements
	_selectedElements = _vis.selected();
	
	// filter out selected elements
    _vis.filter('all', visibility, true);
    
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
	
	// re-apply filtering based on edge types
	updateEdges();
	
	// refresh & update genes tab 
	_refreshGenesTab();
	updateGenesTab();
	
	// visualization changed, perform layout if necessary
	_visChanged();
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
function jokerAction()
{
	var selectedElements = _vis.selected();
	
	var str;
	
	if (selectedElements.length > 0)
	{
		str = _nodeDetails(selectedElements[0]);
	}
	
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
		str += "data: \n";
		
		for (var field in node.data)
		{
			str += field + ": " +  node.data[field] + "\n";
		}
	}
	
	str += "short id: " + _shortId(node.data.id) + "\n";
	str += "safe id: " + _safeId(node.data.id) + "\n";
	
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
 * Toggle auto layout options on or off. If auto layout is active, then the
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
	$("#settings_dialog").dialog("open");
}

/**
 * Updates the contents of the layout properties panel.
 */
function _updatePropsUI()
{
	// update settings panel UI
	
	for (var i=0; i < _layoutOptions.length; i++)
	{
		if (_layoutOptions[i].id == "weightNorm")
		{
			// clean all selections
			$("#norm_linear").removeAttr("selected");
			$("#norm_invlinear").removeAttr("selected");
			$("#norm_log").removeAttr("selected");
			
			// set the correct option as selected
			
			$("#norm_" + _layoutOptions[i].value).attr("selected", "selected");
		}
		else if (_layoutOptions[i].id == "autoStabilize")
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
 * Those characters cause problems with the "id" property of an HTML object.
 * 
 * @param id	id to be modified
 * @return		safe version of the given id
 */
function _safeId(id)
{
	var safeId = id;
	
	safeId = _replaceAll(safeId, "/", "_");
	safeId = _replaceAll(safeId, "\\", "_");
	safeId = _replaceAll(safeId, "#", "_");
	safeId = _replaceAll(safeId, ".", "_");
	safeId = _replaceAll(safeId, ":", "_");
	safeId = _replaceAll(safeId, '"', "_");
	safeId = _replaceAll(safeId, "'", "_");
	
	return safeId;
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
