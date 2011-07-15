// flags
var _autoLayout = false;
var _visChanged = false;
var _nodeLabelsVisible = true;
var _edgeLabelsVisible = false;
var _panZoomVisible = true;
var _linksMerged = false;
var _profileDataVisible = true;

// array of control functions
var _controlFunctions;

// class constants for css visualization
const CHECKED_CLASS = "checked-menu-item";
const SEPARATOR_CLASS = "separator-menu-item";
const LAST_CLASS = "last-menu-item";
const MENU_CLASS = "main-menu-item";
const SUB_MENU_CLASS = "sub-menu-item";

// name of the graph layout
var _graphLayout = {name: "ForceDirected"};

// force directed layout options
var _layoutOptions;	

// array of selected elements, used by the visibility function for filtering
var _selectedElements;

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
	_vis = vis;
	_linkMap = _xrefArray();
	_initControlFunctions();
	_initLayoutOptions();
	_initMainMenu();
	
	// TODO cannot init the tab, because the network is not drawn at this point
	// we may need to use the original graphml file, instead of relying on 
	// CytoscapeWeb.Visualization instance.
	_initGenesTab();
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
	
	if (_autoLayout &&
		_visChanged)
	{
		// re-apply layout
		_performLayout();
	}
	
	// reset flag
	_visChanged = false;
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
			// TODO find the selected option
		}
		else if (_layoutOptions[i].id == "autoStabilize")
		{
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
	$("#node_inspector").dialog("option",
		"position",
		[evt.target.x, evt.target.y]);
	
	// update the contents of the inspector by using the target node
	
	var data = evt.target.data;
	
	// set title
	$("#node_inspector").dialog("option",
		"title",
		data.id);
	
	// clean xref & data rows
	$("#node_inspector_content .data .data_row").remove();
	$("#node_inspector_content .xref .xref_row").remove();
	
	// for each data field, add a new row to inspector
	
	_addDataRow("node", "id", data.id);
	_addDataRow("node", "label", data.label);
	
	for (var field in data)
	{
		if (field == "xref")
		{
			// parse the xref data, and construct the link and its label
			
			var link = _resolveXref(data[field]);
			
			// add to xref table
			if (link != null)
			{				
				_addXrefRow("node", link.href, link.text);
			}
		}
		// TODO what to do with percent values?
		/*
		else if (!field.startsWith("PERCENT"))
		{
			$("#node_inspector_content .node_data").append(
				'<tr class="data_row"><td>' +
				field + ': ' + data[field] + 
				'</td></tr>');
		}
		*/
	}
	
	// open inspector panel
	$("#node_inspector").dialog("open");
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
	$("#edge_inspector").dialog({position: [evt.target.x, evt.target.y]});

	// TODO update the contents of the inspector by using the target edge

	// update the contents of the inspector by using the target node
	
	var data = evt.target.data;
	
	// set title
	$("#edge_inspector").dialog("option",
		"title",
		data.id);
	
	// clean xref & data rows
	$("#edge_inspector_content .data .data_row").remove();
	
	for (var field in data)
	{
		_addDataRow("edge", field, data[field]);
	}
	
	// open inspector panel
	$("#edge_inspector").dialog("open");
}

/**
 * This function returns false if the given graph element is selected,
 * returns true otherwise. This function is used to hide (filter) selected
 * nodes.
 * 
 * @param element	element to be checked
 * @return			false if selected, true otherwise
 */
function visibility(element)
{	
	// TODO find a better way (?) to check if it is selected, do not traverse
	// all selected elements
	
	for (var i=0; i < _selectedElements.length; i++)
	{
		if (element.data.id == _selectedElements[i].data.id)
		{
			return false;
		}
	}
	
	return true;
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
		'<tr class="data_row"><td>' +
		label + ': ' + value + 
		'</td></tr>');
}

/**
 * Adds a cross reference row to the node or edge inspector.
 * 
 * @param type		type of the inspector (should be "node" or "edge")
 * @param href		URL of the reference 
 * @param label		label to be displayed
 */
function _addXrefRow(type, href, label)
{
	$("#" + type + "_inspector_content .xref").append(
		'<tr class="xref_row"><td>' +
		'<a href="' + href + '" target="_blank">' +
		label + '</a>' + 
		'</td></tr>');
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
	
	linkMap["entrez gene"] = "http://www.ncbi.nlm.nih.gov/gene?term=";	
	linkMap["hgnc"] = "";
	linkMap["nucleotide sequence database"] = "";	
	linkMap["refseq"] =	"";
	linkMap["uniprot"] = "http://www.uniprot.org/uniprot/";
	
	return linkMap;
}

/**
 * Initializes the main menu by adjusting its style. Also, initializes the
 * inspector panels and tabs.
 */
function _initMainMenu()
{	
	$("#nav ul").css({display: "none"}); // Opera fix
	
	$("#nav li").hover(
		function() {
			$(this).find('ul:first').css(
					{visibility: "visible",display: "none"}).show(400);
		},
		function() {
			$(this).find('ul:first').css({visibility: "hidden"});
		});
	
	// adjust separators between menu items
	
	$("#network_menu_file").addClass(MENU_CLASS);
	$("#network_menu_topology").addClass(MENU_CLASS);
	$("#network_menu_view").addClass(MENU_CLASS);
	
	$("#save_as_png").addClass(SEPARATOR_CLASS);
	$("#save_as_svg").addClass(LAST_CLASS);
	
	$("#hide_selected").addClass(SEPARATOR_CLASS);	
	$("#auto_layout").addClass(SEPARATOR_CLASS);
	$("#auto_layout").addClass(LAST_CLASS);
	
	$("#perform_layout").addClass(SEPARATOR_CLASS);
	$("#layout_properties").addClass(SUB_MENU_CLASS);
	$("#show_profile_data").addClass(SEPARATOR_CLASS);
	$("#highlight_neighbors").addClass(SEPARATOR_CLASS);
	$("#merge_links").addClass(SEPARATOR_CLASS);
	$("#show_pan_zoom_control").addClass(LAST_CLASS);
	
	// init check icons for checkable menu items
	
	if (_autoLayout)
	{
		$("#auto_layout").addClass(CHECKED_CLASS);
	}
	
	if (_nodeLabelsVisible)
	{
		$("#show_node_labels").addClass(CHECKED_CLASS);
	}
	
	if (_edgeLabelsVisible)
	{
		$("#show_edge_labels").addClass(CHECKED_CLASS);
	}
	
	if (_panZoomVisible)
	{
		$("#show_pan_zoom_control").addClass(CHECKED_CLASS);
	}
	
	if (_linksMerged)
	{
		$("#merge_links").addClass(CHECKED_CLASS);
	}
	
	if (_profileDataVisible)
	{
		$("#show_profile_data").addClass(CHECKED_CLASS);
	}
	
	// adjust settings panel
	$("#settings_dialog").dialog({autoOpen: false, 
		resizable: false, 
		width: 300});
	
	// adjust node inspector
	$("#node_inspector").dialog({autoOpen: false, 
		resizable: false, 
		width: 300});
	
	// adjust edge inspector
	$("#edge_inspector").dialog({autoOpen: false, 
		resizable: false, 
		width: 300});

	
//	$("#node_inspector a").qtip(
//		{
//			content: 'Some basic content for the tooltip'
//		});
	
	// adjust tabs
	$("#network_tabs").tabs();

	// add listener for double click actions
	
	_vis.addListener("dblclick",
		"nodes",
		showNodeInspector);
	
	_vis.addListener("dblclick",
		"edges",
		showEdgeInspector);

}

function _initGenesTab()
{
	var nodes = _vis.nodes();
	
	//$("#genes_tab .genes_list li").remove();
	$("#genes_tab table").remove();
	
	/*
	for (var i=0; i < nodes.length; i++)
	{
		$("#genes_tab .genes_list").append(
			"<li> " + nodes[i].data.id + "</li>");
	}
	*/
	
	$("#genes_tab").append('<table></table>');
	
	for (var i=0; i < nodes.length; i++)
	{
		var shortId = _shortId(nodes[i].data.id);
		
		$("#genes_tab table").append( '<tr><td>' +
			'<input type="checkbox" value="' + nodes[i].data.id + '">' + 
			'<label>' + shortId + '</label>' +
			'</input>' + '</td></tr>');
	}
}

/**
 * Creates a map with <command, function> pairs.
 * 
 * @return an array (map) of control functions
 */
function _initControlFunctions()
{
	_controlFunctions = new Array();
	
	_controlFunctions["hide_selected"] = _hideSelected;
	_controlFunctions["unhide_all"] = _unhideAll;
	_controlFunctions["perform_layout"] = _performLayout;
	_controlFunctions["show_node_labels"] = _toggleNodeLabels;
	_controlFunctions["show_edge_labels"] = _toggleEdgeLabels;
	_controlFunctions["merge_links"] = _toggleMerge;
	_controlFunctions["show_pan_zoom_control"] = _togglePanZoom;
	_controlFunctions["auto_layout"] = _toggleAutoLayout;
	_controlFunctions["show_profile_data"] = _toggleProfileData;
	_controlFunctions["save_as_png"] = _saveAsPng;
	_controlFunctions["save_as_svg"] = _saveAsSvg;
	_controlFunctions["layout_properties"] = _openProperties;
	
	// TODO temp test button, remove when done
	_controlFunctions["joker_button"] = jokerAction;
	
	$("#save_layout_settings").click(saveSettings);
	$("#default_layout_settings").click(defaultSettings);
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
    
	_visChanged = true;
}

/**
 * Removes any existing filters to unhide filtered nodes & edges.
 */
function _unhideAll()
{
	_vis.removeFilter(null, true);
	_visChanged = true;
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
	_initGenesTab();
	
	/*
	var selectedElements = _vis.selected();
	
	var str = "";
	
	if (selectedElements.length > 0)
	{
		str += "fields: ";
		
		for (var field in selectedElements[0])
		{
			str += field + ";";
		}
		
		str += "\n";
		str += "data: \n";
		
		for (var field in selectedElements[0].data)
		{
			str += field + ": " +  selectedElements[0].data[field] + "\n";
		}
	}
	
	alert(str);
	*/
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
	// TODO update visibility of profile data
	
	_profileDataVisible = !_profileDataVisible;
	
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
			// TODO set the correct option as selected
			
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
 * @param id	id of a node
 * @return		a shortened version of the id
 */
function _shortId(id)
{
	var shortId = id;
	
	if (id.contains("#"))
	{
		var pieces = shortId.split("#");
		shortId = pieces[pieces.length - 1];
	}
	else if (id.contains(":"))
	{
		var pieces = shortId.split(":");
		shortId = pieces[pieces.length - 1];
	}
	
	return shortId;
}
