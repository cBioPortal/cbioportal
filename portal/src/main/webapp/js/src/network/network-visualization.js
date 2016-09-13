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

function NetworkVis(divId)
{
    // div id for the network vis html content
    this.divId = divId;

    // edge type constants
    this.edgeTypeConstants =
    {
      CONTROLS_STATE_CHANGE_OF :             { name: "controls-state-change-of",        color: '#5F77D4' , desc: 'Controls State Change of'},
      CONTROLS_TRANSPORT_OF :                { name: "controls-transport-of",           color: '#e96e6e' , desc: 'Controls Transport of'},
      CONTROLS_PHOSPHORYLATION_OF :          { name: "controls-phosphorylation-of",     color: '#A8AD6F' , desc: 'Controls Phosphorylation of'},
      CONTROLS_EXPRESSION_OF :               { name: "controls-expression-of",          color: '#62B36D' , desc: 'Controls Expression of'},
      CATALYSIS_PRECEDES :                   { name: "catalysis-precedes",              color: '#4F4F8F' , desc: 'Catalysis Precedes'},
      CONSUMPTION_CONTROLED_BY :             { name: "consumption-controled-by",        color: '#359C96' , desc: 'Consumption Controled by'},
      CONTROLS_PRODUCTION_OF :               { name: "controls-production-of",          color: '#397187' , desc: 'Controls Production of'},
      CONTROLS_TRANSPORT_OF_CHEMICAL :       { name: "controls-transport-of-chemical",  color: '#A968D9' , desc: 'Controls Transport of Chemical'},
      USED_TO_PRODUCE :                      { name: "used-to-produce",                 color: '#8F4A86' , desc: 'Used to Produce'},
      CHEMICAL_AFFECTS :                     { name: "chemical-affects",                color: '#8A8262' , desc: 'Chemical Affects'},
      IN_COMPLEX_WITH :                      { name: "in-complex-with",                 color: '#825E51' , desc: 'In Complex With'},
      INTERACTS_WITH :                       { name: "interacts-with",                  color: '#6AB0CC' , desc: 'Interacts With'},
      NEIGHBOR_OF :                          { name: "neighbor-of",                     color: '#AB5471' , desc: 'Neighbor of'},
      REACTS_WITH :                          { name: "reacts-with",                     color: '#B2D180' , desc: 'Reacts With'},
      DRUG_TARGET :                          { name: "DRUG_TARGET",                     color: '#CCAB5A' , desc: 'Targeted by Drug'},
      OTHER :                                { name: "other",                           color: '#999999' , desc: 'Other'},
    };

    //
    this.visibilityOfType =
    {
      CONTROLS_STATE_CHANGE_OF : true,
      CONTROLS_TRANSPORT_OF :false,
      CONTROLS_PHOSPHORYLATION_OF :false,
      CONTROLS_EXPRESSION_OF :true,
      CATALYSIS_PRECEDES : false,
      CONSUMPTION_CONTROLED_BY :false,
      CONTROLS_PRODUCTION_OF : false,
      CONTROLS_TRANSPORT_OF_CHEMICAL : false,
      USED_TO_PRODUCE :false,
      CHEMICAL_AFFECTS :false,
      IN_COMPLEX_WITH :true,
      INTERACTS_WITH :false,
      NEIGHBOR_OF :false,
      REACTS_WITH :false,
      DRUG_TARGET :true,
      OTHER :false
    };

    //
    this.visibilityOfSource ={};
    this.percentageFilterMap = {};

    // relative selectors for the given div id
    this.edgeInspectorSelector = this._createEdgeInspector(divId);
    this.geneLegendSelector = this._createGeneLegend(divId);
    this.drugLegendSelector = this._createDrugLegend(divId);
    this.edgeLegendSelector = this._createEdgeLegend(divId);
    this.settingsDialogSelector = this._createSettingsDialog(divId);

    this.mainMenuSelector = "#" + this.divId + " #network_menu_div";
    this.quickInfoSelector = "#" + this.divId + " #quick_info_div";

    this.networkTabsSelector = "#" + this.divId + " #network_tabs";
    this.relationsTabSelector = "#" + this.divId + " #relations_tab";
    this.genesTabSelector = "#" + this.divId + " #genes_tab";
    this.detailsTabSelector = "#" + this.divId + " #element_details_tab";
    this.geneListAreaSelector = "#" + this.divId + " #gene_list_area";
    this.drugFilterSelector = "#" + this.divId + " #drop_down_select";

    // flags
    this._autoLayout = true;
    this._removeDisconnected = true;
    this._nodeLabelsVisible = false;
    this._edgeLabelsVisible = false;
    this._panZoomVisible = false;
    this._linksMerged = false;
    this._profileDataVisible = false;
    this._selectFromTab = false;

    // array of control functions
    this._controlFunctions = null;


    // node type constants
    this.PROTEIN = "Protein";
    this.SMALL_MOLECULE = "SmallMolecule";
    this.DRUG = "Drug";
    this.UNKNOWN = "Unknown";

    // default values for sliders
    this.WEIGHT_COEFF = 0;
    this.ALTERATION_PERCENT = 0;

    // class constants for css visualization
    this.CHECKED_CLASS = "checked-menu-item";
    this.MENU_SEPARATOR_CLASS = "separator-menu-item";
    this.FIRST_CLASS = "first-menu-item";
    this.LAST_CLASS = "last-menu-item";
    this.MENU_CLASS = "main-menu-item";
    this.SUB_MENU_CLASS = "sub-menu-item";
    this.HOVERED_CLASS = "hovered-menu-item";
    this.SECTION_SEPARATOR_CLASS = "section-separator";
    this.TOP_ROW_CLASS = "top-row";
    this.BOTTOM_ROW_CLASS = "bottom-row";
    this.INNER_ROW_CLASS = "inner-row";

    // string constants
    this.ID_PLACE_HOLDER = "REPLACE_WITH_ID";
    this.ENTER_KEYCODE = "13";

    // name of the graph layout
    this._graphLayout = {name: "cose-bilkent", randomize: true};
    //var _graphLayout = {name: "ForceDirected", options:{weightAttr: "weight"}};

    // force directed layout options
    this._layoutOptions = null;

    // map of selected elements, used by the filtering functions
    this._selectedElements = null;

    // map of connected nodes, used by filtering functions
    this._connectedNodes = null;

    // array of previously filtered elements
    this._alreadyFiltered = null;

    // array of genes filtered due to slider
    this._filteredBySlider = null;

    // array of drugs filtered due to drop down
    this._filteredByDropDown = null;

    // array of nodes filtered due to disconnection
    this._filteredByIsolation = null;

    // array of filtered edge types
    this._edgeTypeVisibility = null;

    // array of filtered edge sources
    this._edgeSourceVisibility = null;

    // map used to resolve cross-references
    this._linkMap = null;

    // map used to filter genes by weight slider
    this._geneWeightMap = null;

    // threshold value used to filter genes by weight slider
    this._geneWeightThreshold = null;

    // maximum alteration value among the non-seed genes in the network
    this._maxAlterationPercent = null;

    // CytoscapeWeb.Visualization instance
    this._vis = null;

    this.isEdgeClicked = false;

    this.metaEdges = [];
    this.edgesToBeMerged = [];

    //CSS styles for higlight operation
    this.notHighlightNodeCSS = {'border-opacity': 0.3, 'text-opacity' : 0.3, 'background-opacity': 0.3};
    this.notHighlightEdgeCSS = {'opacity':0.3, 'text-opacity' : 0.3, 'background-opacity': 0.3};
}


/**
 * Initializes all necessary components. This function should be invoked, before
 * calling any other function in this script.
 *
 * @param vis   CytoscapeWeb.Visualization instance associated with this UI
 */
NetworkVis.prototype.initNetworkUI = function(vis)
{
    this._vis = vis;
    this._linkMap = this._xrefArray();

    // init filter arrays
    this._alreadyFiltered = new Array();
    this._filteredBySlider = new Array();
    this._filteredByDropDown = new Array();
    this._filteredByIsolation = new Array();
    this._edgeTypeVisibility = this._edgeTypeArray();
    this._edgeSourceVisibility = this._edgeSourceArray();

    this._geneWeightMap = this._geneWeightArray(this.WEIGHT_COEFF);
    this._geneWeightThreshold = this.ALTERATION_PERCENT;
    this._maxAlterationPercent = this._maxAlterValNonSeed(this._geneWeightMap);

    this._resetFlags();
    //Toggle auto layout initially so that initial additional layout is prevented !
    this._toggleAutoLayout();
    this._initMainMenu();

    this._initPropsUI();
    this._initSliders();
    this._initDropDown();
    this._initTooltipStyle();

    // add listener for the main tabs to hide dialogs when user selects
    // a tab other than the Network tab

    var self = this;
    this._createMergingEdges();
    this._toggleMerge();
    var hideDialogs = function(evt, ui){
        self.hideDialogs(evt, ui);
    };


    $("#tabs").bind("tabsactivate", hideDialogs);

    // this is required to prevent hideDialogs function to be invoked
    // when clicked on a network tab
    $(this.networkTabsSelector).bind("tabsactivate", false);

    // init tabs
    $(this.networkTabsSelector).tabs();
    $(this.networkTabsSelector + " .network-tab-ref").tipTip(
        {defaultPosition: "top", delay:"100", edgeOffset: 10, maxWidth: 200});

    this._initGenesTab();
    this._initRelationsTab();
    this._refreshGenesTab();
    this._refreshRelationsTab();
    this._refreshRelationsTabUIVisibility();

    //Since edge sources and types are dynamic create pop-ups here !
    this.interactionSourceVisibilitySelector = this._createInteractionSourceVisibilityWindow(this.divId);
    this.interactionTypeVisibilitySelector = this._createInteractionTypeVisibilityWindow(this.divId);
    this._initDialogs();


    this._initControlFunctions();
    this._initLayoutOptions();

    // adjust things for IE
    this._adjustIE();

    // make UI visible
    this._setVisibility(true);
    $(".layout-properties").css("width", "85px");

    this.updateEdges();
    //Make interactions merged initially
    //this._toggleMerge();
};


NetworkVis.prototype._createMergingEdges = function()
{
    var edges = this._vis.edges();
    var tempEdges = {};
    var names = [];

    //Meta edges
    var mergingEdges = [];

    //Edges that will be merged
    var mergedEdges = {};

    for (var i = 0; i < edges.length; i++)
    {
      if (tempEdges['source:' + edges[i]._private.data.target + ';target:' + edges[i]._private.data.source] != undefined)
      {
        tempEdges['source:' + edges[i]._private.data.target + ';target:' + edges[i]._private.data.source].push(edges[i]);
      }
      else
      {
        if (tempEdges['source:' + edges[i]._private.data.source + ';target:' + edges[i]._private.data.target] == undefined)
        {
          tempEdges['source:' + edges[i]._private.data.source + ';target:' + edges[i]._private.data.target] = [];
        }
        tempEdges['source:' + edges[i]._private.data.source + ';target:' + edges[i]._private.data.target].push(edges[i]);
      }

    }

    for (var k in tempEdges)
      names.push(k);

    for (var i = 0; i < names.length; i++)
    {
      if (tempEdges[names[i]].length > 1)
      {
        var src = tempEdges[names[i]][0]._private.data.source;
        var trgt= tempEdges[names[i]][0]._private.data.target;
        var metaEdge =
        {
          group: 'edges',
          data:
          {
            id: "e" + src + "" + trgt,
            source: src,
            target: trgt,
            mergeStatus: 'merged', //Mark meta edges like that !
            INTERACTION_PUBMED_ID: 'NA',
            type: 'MERGED'
          },

        }

        mergingEdges.push(metaEdge);

        for (var j = 0; j < tempEdges[names[i]].length; j++)
        {
          if (mergedEdges[names[i]] == undefined)
          {
            mergedEdges[names[i]] = [];
          }
          mergedEdges[names[i]].push(tempEdges[names[i]][j]);
          //Mark edges to be merged like that !
          tempEdges[names[i]][j]._private.data['mergeStatus'] = 'toBeMerged';
        }
      }
    }

    // Initially map the edges to be merged and meta edges, so that we can use eles.restore()
    // functionality of cytoscape.js
    this._vis.add(mergingEdges);
    this.metaEdges = this._vis.remove("[mergeStatus='merged']");
    this.edgesToBeMerged = this._vis.edges("[mergeStatus='toBeMerged']");
}

/**
* Hides interaction source popup
 */
NetworkVis.prototype._closeInteractionSourcePopUp = function (closeWithLayout)
{
  this.updateEdges();
  $(this.interactionSourceVisibilitySelector).dialog('close');

  if (closeWithLayout) {
    // visualization changed, perform layout if necessary
    this._visChanged();
  }
}

/**
 * Hides interaction type popup
 */
NetworkVis.prototype._closeInteractionTypePopUp = function ()
{
  this.updateEdges();
  $(this.interactionTypeVisibilitySelector).dialog('close');
}

/**
 * Hides all dialogs upon selecting a tab other than the network tab.
 */
NetworkVis.prototype.hideDialogs = function (evt, ui)
{
    // get the index of the tab that is currently selected
    // var selectIdx = $("#tabs").tabs("option", "selected");

    // close all dialogs
    $(this.settingsDialogSelector).dialog("close");
    $(this.edgeInspectorSelector).dialog("close");
    $(this.geneLegendSelector).dialog("close");
    $(this.drugLegendSelector).dialog("close");
    $(this.edgeLegendSelector).dialog("close");
    $(this.interactionTypeVisibilitySelector).dialog("close");
    $(this.interactionSourceVisibilitySelector).dialog("close");

};

/**
 * This function handles incoming commands from the menu UI. All menu items
 * is forwarded to this function with a specific command string.
 *
 * @param command   command as a string
 */
NetworkVis.prototype.handleMenuEvent = function(command)
{
    // execute the corresponding function
    var func = this._controlFunctions[command];

    // try to call the handler if it is defined
    if (func != null)
    {
        func();
    }
};

/**
 * Updates selected genes when clicked on a gene on the Genes Tab. This function
 * helps the synchronization between the genes tab and the visualization.
 *
 * @param evt   target event that triggered the action
 */
NetworkVis.prototype.updateSelectedGenes = function(evt)
{
    // this flag is set to prevent updateGenesTab function to update the tab
    // when _vis.select function is called.
    this._selectFromTab = true;

    // deselect all nodes
    this._vis.nodes().deselect();
    var visInstance = this._vis;
    // select all checked nodes
    $(this.geneListAreaSelector + " select option").each(
        function(index)
        {
            if ($(this).is(":selected"))
            {
                var nodeId = $(this).val();
                visInstance.$('#'+nodeId).select();
            }
        });

    // reset flag
    this._selectFromTab = false;
};

NetworkVis.prototype._selectAll_InteractionTypeVisibility = function()
{
  for (var key in this.edgeTypeConstants)
  {
      //If the percentage of interaction is not 0 !
      if (!this.percentageFilterMap[key])
      {
        $(this.interactionTypeVisibilitySelector + " #"+this.edgeTypeConstants[key].name+'_check').attr('checked', true);
        this.visibilityOfType[key] = true;
      }

  }
  this._refreshRelationsTabUIVisibility();
}

NetworkVis.prototype._unselectAll_InteractionTypeVisibility = function()
{
  for (var key in this.edgeTypeConstants)
  {
    //If the percentage of interaction is not 0 !
    if (!this.percentageFilterMap[key])
    {
      $(this.interactionTypeVisibilitySelector + " #"+this.edgeTypeConstants[key].name+'_check').removeAttr('checked');
      this.visibilityOfType[key] = false;
    }
  }
  this._refreshRelationsTabUIVisibility();
}

NetworkVis.prototype._selectAll_InteractionSourceVisibility = function()
{
  for (var key in this._edgeSourceVisibility)
  {
      $(this.interactionSourceVisibilitySelector + " #"+key+'_check').attr('checked', true);
      this.visibilityOfSource[key] = true;
  }
  this._refreshRelationsTabUIVisibility();
}

NetworkVis.prototype._unselectAll_InteractionSourceVisibility = function()
{
  for (var key in this._edgeSourceVisibility)
  {
      $(this.interactionSourceVisibilitySelector + " #"+key+'_check').removeAttr('checked');
      this.visibilityOfSource[key] = false;
  }
  this._refreshRelationsTabUIVisibility();
}

/**
 * Saves layout settings when clicked on the "Save" button of the
 * "Layout Options" panel.
 */
NetworkVis.prototype.saveSettings = function()
{
    // update layout option values
    for (var i=0; i < (this._layoutOptions).length; i++)
    {
//      if (_layoutOptions[i].id == "weightNorm")
//      {
//          // find the selected option and update the corresponding value
//
//          if ($("#norm_linear").is(":selected"))
//          {
//              _layoutOptions[i].value = $("#norm_linear").val();
//          }
//          else if ($("#norm_invlinear").is(":selected"))
//          {
//              _layoutOptions[i].value = $("#norm_invlinear").val();
//          }
//          else if ($("#norm_log").is(":selected"))
//          {
//              _layoutOptions[i].value = $("#norm_log").val();
//          }
//      }

        if (this._layoutOptions[i].id == "animate")
        {
            // check if the auto stabilize box is checked

            if($(this.settingsDialogSelector + " #animate").is(":checked"))
            {
                this._layoutOptions[i].value = true;
                $(this.settingsDialogSelector + " #animate").val(true);
            }
            else
            {
                this._layoutOptions[i].value = false;
                $(this.settingsDialogSelector + " #animate").val(false);
            }
        }
        else if (this._layoutOptions[i].id == "tile")
        {
            // check if the auto stabilize box is checked

            if($(this.settingsDialogSelector + " #tile").is(":checked"))
            {
                this._layoutOptions[i].value = true;
                $(this.settingsDialogSelector + " #tile").val(true);
            }
            else
            {
                this._layoutOptions[i].value = false;
                $(this.settingsDialogSelector + " #tile").val(false);
            }
        }
        else if (this._layoutOptions[i].id == "randomize")
        {
            // check if the auto stabilize box is checked

            if($(this.settingsDialogSelector + " #randomize").is(":checked"))
            {
                this._layoutOptions[i].value = true;
                $(this.settingsDialogSelector + " #randomize").val(true);
            }
            else
            {
                this._layoutOptions[i].value = false;
                $(this.settingsDialogSelector + " #randomize").val(false);
            }
        }
        else if (this._layoutOptions[i].id == "fit")
        {
            // check if the auto stabilize box is checked

            if($(this.settingsDialogSelector + " #fit").is(":checked"))
            {
                this._layoutOptions[i].value = true;
                $(this.settingsDialogSelector + " #fit").val(true);
            }
            else
            {
                this._layoutOptions[i].value = false;
                $(this.settingsDialogSelector + " #fit").val(false);
            }
        }
        else
        {
            // simply copy the text field value
            this._layoutOptions[i].value =
                Number($(this.settingsDialogSelector + " #" + this._layoutOptions[i].id).val());
        }

    }

    // update graphLayout options
    this._updateLayoutOptions();

    // close the settings panel
    $(this.settingsDialogSelector).dialog("close");
};

/**
 * Reverts to default layout settings when clicked on "Default" button of the
 * "Layout Options" panel.
 */
NetworkVis.prototype.defaultSettings = function()
{
    this._layoutOptions = this._defaultOptsArray();
    this._updateLayoutOptions();
    this._updatePropsUI();
};

/**
 * Updates the contents of the details tab according to
 * the currently selected elements.
 *
 * @param evt
 */
NetworkVis.prototype.updateDetailsTab = function(evt)
{
    // TODO also consider selected edges?
    var selected = this._vis.$(':selected');
    var data;
    var areEdges = true;
    var self = this;

    for (var i = 0; i < selected.length; i++)
    {
        if(selected[i]._private.group != "edges")
        {
            areEdges = false;
            break;
        }
    }

    // clean previous content
    $(self.detailsTabSelector + " div").empty();
    $(self.detailsTabSelector + " .error").hide();

    if (selected.length == 1 && !areEdges)
    {
        data = selected[0]._private.data;
    }
    else if (selected.length > 1 && !areEdges)
    {
        //$(self.detailsTabSelector + " div").empty();
        $(self.detailsTabSelector + " .error").append(
            "Currently more than one node/edge is selected. Please, select only one node/edge to see details.");
        $(self.detailsTabSelector + " .error").show();
        return;
    }
    else if (selected.length >= 1 && areEdges)
    {
        if(this.isEdgeClicked)
        {
          //TODO false will be replaced with evt.target.merged after merge implemented
            this.addInteractionInfo(evt, this.detailsTabSelector, "click");
        }
        else{
          //TODO false will be replaced with this._linksMerged after merge implemented
            this.addInteractionInfo(evt, this.detailsTabSelector, "select");
        }

        return;
    }
    else
    {
        //$(self.detailsTabSelector + " div").empty();
        $(self.detailsTabSelector + " .error").append(
            "Currently there is no selected node/edge. Please, select a node/edge to see details.");
        $(self.detailsTabSelector + " .error").show();
        return;
    }

    var handler = function(queryResult) {
        // check the initial conditions, if they don't match do nothing
        // (they may not match because of a delay in the ajax request)

        selected = self._vis.nodes(':selected');

        if (selected.length != 1 ||
            data.id != selected[0]._private.data.id)
        {
            return;
        }

        // update tab content
        $(self.detailsTabSelector + " .biogene-content").empty();

        if (queryResult.returnCode != "SUCCESS")
        {
            $(self.detailsTabSelector + " .error").append(
                "Error retrieving data: " + queryResult.returnCode);
            $(self.detailsTabSelector + " .error").show();
        }
        else
        {
            if (queryResult.count > 0)
            {
                // generate the view by using backbone
                var biogeneView = new BioGeneView(
                    {el: self.detailsTabSelector + " .biogene-content",
                    data: queryResult.geneInfo[0]});
            }
            else
            {
                $(self.detailsTabSelector + " .error").append(
                    "No additional information available for the selected node.");
            }
        }

        // generate view for genomic profile data
        var genomicProfileView = new GenomicProfileView(
            {el: self.detailsTabSelector + " .genomic-profile-content",
            data: data});
    };


    if (data.type == this.DRUG)
    {
        // update tab content
        $(self.detailsTabSelector + " div").empty();

        var drugView = new DrugInfoView({el: this.detailsTabSelector + " .drug-info-content",
            data: data,
            linkMap: this._linkMap,
            idPlaceHolder: this.ID_PLACE_HOLDER,
            edges: this._vis.edges()});
    }
    // send biogene query for only genes
    else
    {
        var queryParams = {"query": data.label,
            "org": "human",
            "format": "json"};

        $(self.detailsTabSelector + " .biogene-content").append(
            '<img src="images/ajax-loader.gif" alt="loading" />');

        $.post("bioGeneQuery.do",
            queryParams,
            handler);
    }
};

NetworkVis.prototype.addInteractionInfo = function(evt, selector, selectType)
{
    //this.createEdgeDetailsSelector(selector);
    var dataRow;

    var data = (evt.cyTarget._private.data);

    if(evt.cyTarget._private.group == "nodes")
        return;

    // clean inspector view here !
    $(selector + " .edge-inspector-content").empty();
    var edgeType = data.type;
    var sourceLabel = this._vis.nodes('#' + data.source).data().label;
    var targetLabel = this._vis.nodes('#' + data.target).data().label;
    var self = this;

    var showSBGNViewButton = $('<button/>',
    {
        "class": "SBGNPopUpButton",
        text: 'Detailed process (SBGN)',
        click: function () { self.popUpSBGNView(sourceLabel, targetLabel, edgeType) }
    });

    var title =  sourceLabel + " - " + targetLabel;

        //TODO When merge is implemented
    if (evt.cyTarget._private.data.mergeStatus != undefined && evt.cyTarget._private.data.mergeStatus === "merged")
    {
        title += ' (Summary Edge)';
        var text = '<div class="header"><span class="title"><label>';
            text +=  title;
            text += '</label></span></div>';
        $(selector + " .edge-inspector-content").append(text);


        var edgesOfMetaEdge = this.edgesToBeMerged.filter(function(i, element)
        {
            if( element.isEdge()
            && element.data("source") == evt.cyTarget._private.data.source
            && element.data("target") == evt.cyTarget._private.data.target)
            {
              return true;
            }
            return false;
        });

        var edges = (selectType == "select") ? (edgesOfMetaEdge) : (evt.cyTarget);

        // add information for each edge

        for (var i = 0; i < edges.length; i++)
        {
            // skip filtered-out edges
/*            if (edges[i].visible() == false)
            {
                continue;
            }
*/
            data = edges[i]._private.data;

            // add an empty row for better edge separation
            $(selector + " .edge-inspector-content").append(
                '<tr align="left" class="empty-row data-row"><td> </td></tr>');

            // add edge data
            dataRow = this._addDataRow2("edge",
                        "Source(s)",
                        (data["INTERACTION_DATA_SOURCE"]).replace(/;/g, ', '),
                        this.TOP_ROW_CLASS);
            $(selector + " .edge-inspector-content").append(dataRow);

            if (data["INTERACTION_PUBMED_ID"] == "NA")
            {
                // no PubMed ID, add only type information
                dataRow = this._addDataRow2("edge",
                            "Type",
                            _toTitleCase(data["type"]),
                            this.BOTTOM_ROW_CLASS);
                $(selector + " .edge-inspector-content").append(dataRow);
            }
            else
            {
                // add type information
                dataRow = this._addDataRow2("edge",
                            "Type",
                            _toTitleCase(data["type"]),
                            this.INNER_ROW_CLASS);
                $(selector + " .edge-inspector-content").append(dataRow);

                dataRow = this._addPubMedIds2(data, true);
                $(selector + " .edge-inspector-content").append(dataRow);
            }
        }
        // add an empty row for the last edge
        $(selector + " .edge-inspector-content").append(
            '<tr align="left" class="empty-row data-row"><td> </td></tr>');
    }
    else
    {
        var text = '<div class="header"><span class="title"><label>';
            text +=  title;
            text += '</label></span></div>';

        $(selector + " .edge-inspector-content").append(text);

        // add an empty row for better edge separation
        $(selector + " .edge-inspector-content").append(
                '<tr align="left" class="empty-row data-row"><td> </td></tr>');

        dataRow = this._addDataRow2("edge",
                                    "Source(s)",
                                    (data["INTERACTION_DATA_SOURCE"]).replace(/;/g, ', '));
        $(selector + " .edge-inspector-content").append(dataRow);
        dataRow = this._addDataRow2("edge", "Type", _toTitleCase(data["type"]));
        $(selector + " .edge-inspector-content").append(dataRow);

        if (data["INTERACTION_PUBMED_ID"] != "NA")
        {
            dataRow = this._addPubMedIds2(data, false);
            $(selector + " .edge-inspector-content").append(dataRow);
        }
    }

    //Finally append sbgn view button
    $(selector + " .edge-inspector-content").append(showSBGNViewButton);



    //$(selector).show();
}

NetworkVis.prototype._addDataRow2 = function(type, label, value /*, section*/)
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
        value = this.UNKNOWN;
    }

    var info ='<tr align="left"><td>' +
        '<strong>' + label + ':</strong> ' + value +
        '</td></tr>';

    return info;
};

/**
 * Adds PubMed ID's as new data rows to the edge inspector.
 *
 * @param data          edge's data
 * @param summaryEdge   indicated whether the given edge is a summary edge or
 *                      a regular edge
 */
NetworkVis.prototype._addPubMedIds2 = function(data, summaryEdge)
{
    var ids = data["INTERACTION_PUBMED_ID"].split(";");
    var link, xref;
    var links = new Array();
    // collect pubmed id(s) into an array

    for (var i = 0; i < ids.length; i++)
    {
        link = this._resolveXref(ids[i]);

        if (link.href == "#")
        {
            // skip unknown sources
            continue;
        }

        xref = '<a href="' + link.href + '" target="_blank">' +
               link.pieces[1] + '</a>';

        links.push(xref);
    }

    var xrefList = links[0];

    for (var i = 1; i < links.length; i++)
    {
        xrefList += ", " + links[i];
    }

    if (summaryEdge)
    {
        // class of this row should be BOTTOM_ROW_CLASS (this is needed
        // to separate edges visually)

        return this._addDataRow2("edge",
                    "PubMed ID(s)",
                    xrefList,
                    this.BOTTOM_ROW_CLASS);
    }
    else
    {
        return this._addDataRow2("edge",
                    "PubMed ID(s)",
                    xrefList);
    }
};


/**
 * Shows the edge inspector when double clicked on an edge.
 *
 * @param evt   event that triggered this function
 */
NetworkVis.prototype.showEdgeInspector = function(evt)
{
    // set the position of the inspector
    // TODO same coordinate problems as node inspector
    //$(this.edgeInspectorSelector).dialog({position: [_mouseX(evt), _mouseY(evt)]});

    // TODO update the contents of the inspector by using the target edge

    var data = evt.target._private.data;
    var title = this._vis.$('#'+data.source)._private.data.label + " - " +
                this._vis.$('#'+data.target)._private.data.label;

    // clean xref & data rows
    $(this.edgeInspectorSelector + " .edge_inspector_content .data .data-row").remove();
    $(this.edgeInspectorSelector + " .edge_inspector_content .xref .xref-row").remove();

    // if the target is a merged edge, then add information of all edges
    // between the source and target
    if (evt.target.merged)
    {
        // update title
        title += ' (Summary Edge)';

        var edges = evt.target.edges;

//      _addDataRow(this.edgeInspectorSelector, "edge", "Weight", _toTitleCase(evt.target.data["weight"]));

        // add information for each edge

        for (var i = 0; i < edges.length; i++)
        {
            // skip filtered-out edges
            if (!this.edgeVisibility(edges[i]))
            {
                continue;
            }

            data = edges[i]._private.data;

            // add an empty row for better edge separation
            $(this.edgeInspectorSelector + " .edge_inspector_content .data").append(
                '<tr align="left" class="empty-row data-row"><td> </td></tr>');

            // add edge data

            this._addDataRow(this.edgeInspectorSelector,
                        "edge",
                        "Source",
                        data["INTERACTION_DATA_SOURCE"],
                        this.TOP_ROW_CLASS);

//          _addDataRow(this.edgeInspectorSelector, "edge", "Weight", _toTitleCase(data["weight"]));

            if (data["INTERACTION_PUBMED_ID"] == "NA")
            {
                // no PubMed ID, add only type information
                this._addDataRow(this.edgeInspectorSelector,
                            "edge",
                            "Type",
                            _toTitleCase(data["type"]),
                            this.BOTTOM_ROW_CLASS);
            }
            else
            {
                // add type information
                this._addDataRow(this.edgeInspectorSelector,
                            "edge",
                            "Type",
                            _toTitleCase(data["type"]),
                            this.INNER_ROW_CLASS);

                this._addPubMedIds(data, true);
            }
        }

        // add an empty row for the last edge
        $(this.edgeInspectorSelector + " .edge_inspector_content .data").append(
            '<tr align="left" class="empty-row data-row"><td> </td></tr>');
    }
    // target is a regular edge
    else
    {
        this._addDataRow(this.edgeInspectorSelector, "edge", "Source", data["INTERACTION_DATA_SOURCE"]);
        this._addDataRow(this.edgeInspectorSelector, "edge", "Type", _toTitleCase(data["type"]));
//      _addDataRow(this.edgeInspectorSelector, "edge", "Weight", _toTitleCase(data["weight"]));

        if (data["INTERACTION_PUBMED_ID"] != "NA")
        {
            this._addPubMedIds(data, false);
        }
    }

    // set title
    $(this.edgeInspectorSelector).dialog("option",
                                "title",
                                title);

    // open inspector panel
    $(this.edgeInspectorSelector).dialog("open").height("auto");

    // if the inspector panel height exceeds the max height value
    // adjust its height (this also adds scroll bars by default)
    if ($(this.edgeInspectorSelector).height() >
        $(this.edgeInspectorSelector).dialog("option", "maxHeight"))
    {
        $(this.edgeInspectorSelector).dialog("open").height(
            $(this.edgeInspectorSelector).dialog("option", "maxHeight"));
    }
};

/**
 * Adds PubMed ID's as new data rows to the edge inspector.
 *
 * @param data          edge's data
 * @param summaryEdge   indicated whether the given edge is a summary edge or
 *                      a regular edge
 */
NetworkVis.prototype._addPubMedIds = function(data, summaryEdge)
{
    var ids = data["INTERACTION_PUBMED_ID"].split(";");
    var link, xref;
    var links = new Array();
    // collect pubmed id(s) into an array

    for (var i = 0; i < ids.length; i++)
    {
        link = this._resolveXref(ids[i]);

        if (link.href == "#")
        {
            // skip unknown sources
            continue;
        }

        xref = '<a href="' + link.href + '" target="_blank">' +
               link.pieces[1] + '</a>';

        links.push(xref);
    }

    var xrefList = links[0];

    for (var i = 1; i < links.length; i++)
    {
        xrefList += ", " + links[i];
    }

    if (summaryEdge)
    {
        // class of this row should be BOTTOM_ROW_CLASS (this is needed
        // to separate edges visually)

        this._addDataRow(this.edgeInspectorSelector,
                    "edge",
                    "PubMed ID(s)",
                    xrefList,
                    this.BOTTOM_ROW_CLASS);
    }
    else
    {
        this._addDataRow(this.edgeInspectorSelector,
                    "edge",
                    "PubMed ID(s)",
                    xrefList);
    }
};

/**
 * Updates the gene tab if at least one node is selected or deselected on the
 * network. This function helps the synchronization between the genes tab and
 * visualization.
 *
 * @param evt   event that triggered the action
 */
NetworkVis.prototype.updateGenesTab = function(evt)
{
    var selected = this._vis.nodes(":selected");

    // do not perform any action on the gene list,
    // if the selection is due to the genes tab
    if(!this._selectFromTab)
    {
        if (_isIE())
        {
            this._setComponentVis($(this.geneListAreaSelector + " select"), false);
        }

        // deselect all options
        $(this.geneListAreaSelector + " select option").each(
            function(index)
            {
                $(this).removeAttr("selected");
            });

        // select options for selected nodes
        for (var i=0; i < selected.length; i++)
        {
            $(this.geneListAreaSelector + " #" +  _safeProperty(selected[i]._private.data.id)).attr(
                "selected", "selected");
        }

        if (_isIE())
        {
            this._setComponentVis($(this.geneListAreaSelector + " select"), true);
        }
    }

    // also update Re-submit button
    if (selected.length > 0)
    {
        // enable the button
        $(this.genesTabSelector + " #re-submit_query").button("enable");
    }
    else
    {
        // disable the button
        $(this.genesTabSelector + " #re-submit_query").button("disable");
    }
};

NetworkVis.prototype.reRunQuery = function()
{
    // TODO get the list of currently interested genes
    var currentGenes = "";
    var nodeMap = this._selectedElementsMap("nodes");

    for (var key in nodeMap)
    {
        currentGenes += nodeMap[key]._private.data.label + " ";
    }

    if (currentGenes.length > 0)
    {
        // update the list of seed genes for the query
        $("#main_form #gene_list").val(currentGenes);

        // re-run query by performing click action on the submit button
        $("#main_form #main_submit").click();
    }
};

/**
 * Searches for genes by using the input provided within the search text field.
 * Also, selects matching genes both from the canvas and gene list.
 */
NetworkVis.prototype.searchGene = function()
{
    var query = $(this.genesTabSelector + " #search_box").val();

    // do not perform search for an empty string
    if (query.length == 0)
    {
        return;
    }

    var genes = this._visibleGenes();
    var i;

    // deselect all nodes
    this._vis.nodes().deselect();

    // linear search for the input text
    for (i=0; i < genes.length; i++)
    {
        if (genes[i]._private.data.label.toLowerCase().indexOf(
            query.toLowerCase()) != -1)
        {
            this._vis.$('#'+genes[i]._private.data.id).select()
        }
    }


};


/**
 * Filters out all selected genes.
 */
NetworkVis.prototype.filterSelectedGenes = function()
{
    var self = this;

    // this is required to pass "this" instance to the listener
    var selectionVisibility = function(element) {
        return self.selectionVisibility(element);
    };

    // update selected elements map
    this._selectedElements = this._selectedElementsMap("nodes");

    // filter out selected elements
    // this._vis.filter("nodes", selectionVisibility);
    this._vis.nodes().forEach(function( ele ){
        if (selectionVisibility(ele) === false || ele._private.style.visibility.strValue == 'hidden'){
          ele.css('visibility', 'hidden');
          ele.unselect();
          ele._private.selectable = false;
        }
        else{
          ele._private.selectable = true;
          ele.css('visibility', 'visible');
        }
    });

    // also, filter disconnected nodes if necessary
    this._filterDisconnected();

    // refresh genes tab
    this._refreshGenesTab();

    this._vis.edges(':selected').unselect();

    // visualization changed, perform layout if necessary
    this._visChanged();
};

/**
 * Filters out all non-selected nodes.
 */
NetworkVis.prototype.filterNonSelected = function()
{
    var self = this;

    // this is required to pass "this" instance to the listener
    var geneVisibility = function(element) {
        return self.geneVisibility(element);
    };

    // update selected elements map
    this._selectedElements = this._selectedElementsMap("nodes");

    // filter out non-selected elements
    this._vis.nodes().forEach(function( ele ){
        if (geneVisibility(ele) === false)
        {
          ele.css('visibility', 'hidden');
          ele._private.selectable = false;
        }
        else
        {
          ele._private.selectable = true;
          ele.css('visibility', 'visible');
        }
    });

    // also, filter disconnected nodes if necessary
    this._filterDisconnected();

    // refresh Genes tab
    this._refreshGenesTab();
    this.updateGenesTab();

    // visualization changed, perform layout if necessary
    this._visChanged();
};

/**
 * Updates the visibility (by filtering mechanism) of edges.
 */
NetworkVis.prototype.updateEdges = function()
{
    // update filtered edge types

    for (var key in this.edgeTypeConstants)
    {
      this._edgeTypeVisibility[this.edgeTypeConstants[key].name] = this.visibilityOfType[key];
    }

    for (var key in this._edgeSourceVisibility)
    {
        this._edgeSourceVisibility[key] = this.visibilityOfSource[key];
    }

    // remove previous node filters due to disconnection
    for (var key in this._filteredByIsolation)
    {
        this._alreadyFiltered[key] = null;
    }

    // clear isolation filter array
    this._filteredByIsolation = new Array();

    // this is required to pass "this" instance to the listener
    var self = this;

    var edgeVisibility = function(element){
        return self.edgeVisibility(element);
    };

    var showAllNodeVisibility = function(element){
        return self.dropDownVisibility(element) &&
            self.currentVisibility(element) &&
            self.sliderVisibility(element);
    }

    // re-apply filter to update nodes
    //_vis.removeFilter("nodes", false);
    this._vis.nodes().forEach(function( ele ){
        if (showAllNodeVisibility(ele) === false){
          ele.css('visibility', 'hidden');
          ele.unselect();
          ele._private.selectable=false;
        }
        else {
          ele.css('visibility', 'visible');
          ele._private.selectable=true;
        }
    });


    // remove current edge filters
    //_vis.removeFilter("edges", false);

    // filter selected types
    this._vis.edges().forEach(function( ele ){
        if (edgeVisibility(ele) === false){
          ele.css('visibility', 'hidden');
          ele.unselect();
          ele._private.selectable=false;
        }
        else {
          ele.css('visibility', 'visible');
          ele._private.selectable=true;

        }
    });

    this.edgesToBeMerged.forEach(function( ele ){
        if (edgeVisibility(ele) === false){
          ele.css('visibility', 'hidden');
          ele._private.selectable=false;
          ele.unselect();
        }
        else {
          ele.css('visibility', 'visible');
          ele._private.selectable=true;
        }
    });

    // remove previous filters due to disconnection
    for (var key in this._filteredByIsolation)
    {
        this._alreadyFiltered[key] = null;
    }

    // filter disconnected nodes if necessary
    this._filterDisconnected();

    // visualization changed, perform layout if necessary
    this._visChanged();
};

/**
 * Determines the visibility of a gene (node) for filtering purposes. This
 * function is designed to filter only the genes which are in the array
 * _alreadyFiltered.
 *
 * @param element   gene to be checked for visibility criteria
 * @return          true if the gene should be visible, false otherwise
 */
NetworkVis.prototype.currentVisibility = function(element)
{
    var visible;

    // if the node is in the array of already filtered elements,
    // then it should be invisibile
    if (this._alreadyFiltered[element._private.data.id] != null)
    {
        visible = false;
    }
    // any other node should be visible
    else
    {
        visible = true;
    }

    return visible;
};

/**
 * Determines the visibility of an edge for filtering purposes.
 *
 * @param element   egde to be checked for visibility criteria
 * @return          true if the edge should be visible, false otherwise
 */
NetworkVis.prototype.edgeVisibility = function(element)
{
    var visible = true;
    var typeVisible = true;
    var sourceVisible = true;

    // TODO currently we do not allow edge filtering by selection, so
    // there should not be any edge in the array _alreadyFiltered

    // if an element is already filtered then it should remain invisible
    if (this._alreadyFiltered[element._private.data.id] != null)
    {
        visible = false;
    }

    // unknown edge type, check for the OTHER flag
    if (this._edgeTypeVisibility[element._private.data.type] == null)
    {
        typeVisible = this._edgeTypeVisibility[this.OTHER];
    }
    // check the visibility of the edge type
    else
    {
        typeVisible = this._edgeTypeVisibility[element._private.data.type];
    }


    if(element._private.data['INTERACTION_DATA_SOURCE'] == null)
    	return;

    var sources = (element._private.data['INTERACTION_DATA_SOURCE']).split(';');


    for (var i = 0; i < sources.length; i++)
    {
          if (this._edgeSourceVisibility[sources[i]] != null)
          {
              sourceVisible = this._edgeSourceVisibility[sources[i]];
              if (!sourceVisible)
              {
                continue;
              }
          }
          else
          {
              // no source specified, check the unknown flag
              sourceVisible = this._edgeSourceVisibility[this.UNKNOWN];
          }
      }


    return (visible && typeVisible && sourceVisible);
};

/**
 * Determines the visibility of a gene (node) for filtering purposes. This
 * function is designed to filter non-selected genes.
 *
 * @param element   gene to be checked for visibility criteria
 * @return          true if the gene should be visible, false otherwise
 */
NetworkVis.prototype.geneVisibility = function(element)
{
    var visible = false;

    // if an element is already filtered then it should remain invisible
    if (this._alreadyFiltered[element._private.data.id] != null)
    {
        visible = false;
    }
    else
    {
        // filter non-selected nodes

        if (this._selectedElements[element._private.data.id] != null)
        {
            visible = true;
        }

        if (!visible)
        {
            // if the element should be filtered, then add it to the map
            this._alreadyFiltered[element._private.data.id] = element;
        }
    }

    return visible;
};

/**
 * Determines the visibility of a drug (node) for filtering purposes. This
 * function is designed to filter drugs by the drop down selection.
 *
 * @param element   gene to be checked for visibility criteria
 * @return          true if the gene should be visible, false otherwise
 */
NetworkVis.prototype.dropDownVisibility = function(element)
{
    var visible = false;
    var weight;
    var selectedOption = $(this.drugFilterSelector).val();

    // if an element is already filtered then it should remain invisible
    if (this._alreadyFiltered[element._private.data.id] != null )
    {
        visible = false;
    }
    // if an element is a seed node, then it should be visible
    // (if it is not filtered manually)
    else if (element._private.data["IN_QUERY"] != null &&
             element._private.data["IN_QUERY"].toLowerCase() == "true")
    {
        visible = true;
    }
    else
    {
        //if the node is a drug then check the drop down selection

        if(element._private.data.type == "Drug"){
            if(selectedOption.toString() == "HIDE_DRUGS") {
                visible = false;
            }
            else if(selectedOption.toString() == "SHOW_ALL") {
                visible = true;
            }
            else if(selectedOption.toString() == "SHOW_CANCER") {
                if( element._private.data["CANCER_DRUG"] == "true")
                    visible = true;
                else
                    visible = false;
            }
            else {  // check FDA approved
                if( element._private.data["FDA_APPROVAL"] == "true")
                    visible = true;
                else
                    visible = false;
            }
        }
        else
        {
            visible = true;
        }

        if (!visible)
        {
            // if the element should be filtered,
            // then add it to the required maps
            this._filteredByDropDown[element._private.data.id] = element;
            this._alreadyFiltered[element._private.data.id] = element;
        }
    }

    return visible;
};


/**
 * Determines the visibility of a gene(node) for filtering purposes. This
 * function is designed to filter nodes by the slider value.
 *
 * @param element   node to be checked for visibility criteria
 * @return          true if the gene should be visible, false otherwise
 */
NetworkVis.prototype.sliderVisibility = function(element)
{
    var visible = false;
    var weight;

    // if an element is already filtered then it should remain invisible
    if (this._alreadyFiltered[element._private.data.id] != null)
    {
        visible = false;
    }
    // if an element is a seed node, then it should be visible
    // (if it is not filtered manually)
    else if (element._private.data["IN_QUERY"] != null &&
             element._private.data["IN_QUERY"].toLowerCase() == "true")
    {
        visible = true;
    }

    else
    {
        // get the weight of the node
        weight = this._geneWeightMap[element._private.data.id];

        // if the weight of the current node is below the threshold value
        // then it should be filtered (also check the element is not a drug)

        if (weight != null && element._private.data.type != "Drug")
        {
            if (weight >= this._geneWeightThreshold)
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

            this._alreadyFiltered[element._private.data.id] = element;
            this._filteredBySlider[element._private.data.id] = element;
        }
    }

    return visible;
};

/**
 * Determines the visibility of a node for filtering purposes. This function is
 * designed to filter disconnected nodes.
 *
 * @param element   node to be checked for visibility criteria
 * @return          true if the node should be visible, false otherwise
 */
NetworkVis.prototype.isolation = function(element)
{
    var visible = false;

    // if an element is already filtered then it should remain invisible
    if (this._alreadyFiltered[element._private.data.id] != null)
    {
        visible = false;
    }
    else
    {
        // check if the node is connected, if it is disconnected it should be
        // filtered out
        if (this._connectedNodes[element._private.data.id] != null)
        {
            visible = true;
        }

        if (!visible)
        {
            // if the node should be filtered, then add it to the map
            this._alreadyFiltered[element._private.data.id] = element;
            this._filteredByIsolation[element._private.data.id] = element;
        }
    }

    return visible;
};

/**
 * This function returns false if the given graph element is selected,
 * returns true otherwise. This function is used to hide (filter) selected
 * nodes & edges.
 *
 * @param element   element to be checked
 * @return          false if selected, true otherwise
 */
NetworkVis.prototype.selectionVisibility = function(element)
{
    // if an element is already filtered then it should remain invisible
    // until the filters are reset
    if (this._alreadyFiltered[element._private.data.id] != null)
    {
        return false;
    }
    // if an edge type is hidden, all edges of that type should be invisible
    else if (this._edgeTypeVisibility[element._private.data.type] != null
        && !this._edgeTypeVisibility[element._private.data.type])
    {
        return false;
    }
    // if an edge source is hidden, all edges of that source should be invisible
    // else if (...)

    // TODO this function is not called anymore for edges (no edge filtering via selecting)
    // so the edge visibility check can be omitted

    // if the element is selected, then it should be filtered
    if (this._selectedElements[element._private.data.id] != null)
    {
        this._alreadyFiltered[element._private.data.id] = element;
        return false;
    }

    return true;
};

/**
 * Creates a map (on element id) of selected elements.
 *
 * @param group     data group (nodes, edges, all)
 * @return          a map of selected elements
 */
NetworkVis.prototype._selectedElementsMap = function(group)
{
    var selected;
    if (group === 'nodes'){
      selected = this._vis.nodes(":selected");
    }
    else if( group === 'edges'){
      selected = this._vis.edges(":selected");
    }
    else if (group === 'all'){
      selected = this._vis.$(":selected");
    }

    var map = new Array();

    for (var i=0; i < selected.length; i++)
    {
        var key = selected[i]._private.data.id;
        map[key] = selected[i];
    }

    return map;
};

/**
 * Creates a map (on element id) of connected nodes.
 *
 * @return  a map of connected nodes
 */

NetworkVis.prototype._connectedNodesMap = function()
{
    var map = new Array();
    var edges;

    // if edges merged, traverse over merged edges for a better performance
    //TODO will be examined after implementation of merged edge!!
/*    if (this._vis.edges(":merged"))
    {
        edges = this._vis.edges(":merged");
    }
    // else traverse over regular edges
    else
*/    {
        edges = this._vis.edges();
    }



    var source;
    var target;


    // for each edge, add the source and target to the map of connected nodes
    for (var i=0; i < edges.length; i++)
    {
        if (edges[i].visible)
        {
            source = this._vis.$("#" + edges[i]._private.data.source)[0];
            target = this._vis.$("#" + edges[i]._private.data.target)[0];

            map[source._private.data.id] = source;
            map[target._private.data.id] = target;
        }

    }

    return map;
};

/**
 * This function is designed to be invoked after an operation (such as filtering
 * nodes or edges) that changes the graph topology.
 */
NetworkVis.prototype._visChanged = function()
{
    // perform layout if auto layout flag is set

    if (this._autoLayout)
    {
	//Disable animation for auto layout
	var tempAnimateFlag = this._graphLayout.animate;
	var tempRandomizeFlag = this._graphLayout.randomize;
	this._graphLayout.animate = false;
	this._graphLayout.randomize = false;

        // re-apply layout
        this._performLayout();

	//Write back original flag
	this._graphLayout.animate = tempAnimateFlag;
	this._graphLayout.randomize = tempRandomizeFlag;
    }
};

/**
 * This function is designed to be invoked after an operation that filters
 * nodes or edges.
 */
NetworkVis.prototype._filterDisconnected = function()
{
    // filter disconnected nodes if the flag is set
    if (this._removeDisconnected)
    {
        // update connected nodes map
        this._connectedNodes = this._connectedNodesMap();

        // this is required to pass "this" instance to the listener
        var self = this;

        var isolation = function(element){
            return self.isolation(element);
        };

        // filter disconnected
        var self = this;
        this._vis.nodes().forEach(function( ele ){
            var check = false;
            var edges = self._vis.edges();
            for (var i = 0; i < edges.length; i++){
              if (edges[i].visible() && (ele._private.data.id === edges[i]._private.data.source ||
                  ele._private.data.id === edges[i]._private.data.target)){
                    check = true;
                    break;
                  }
            }
            if (!check || ele._private.style.visibility.strValue == 'hidden'){
              ele.css('visibility', 'hidden');
              ele._private.selectable=false;
            }
            else{
              ele.css('visibility', 'visible');
              ele._private.selectable=true;

            }
        });
    }
};

/**
 * Highlights the neighbors of the selected nodes.
 *
 */
NetworkVis.prototype._highlightNeighbors = function(/*nodes*/)
{

    var nodes = this._vis.nodes(":selected");

    if (nodes != null && nodes.length > 0)
    {
      for (var i = 0; i < nodes.length; i++)
      {
        var highlightedNodes = nodes[i].neighborhood().nodes(':visible');
        var highlightedEdges = nodes[i].neighborhood().edges(':visible');
        var highlightedElements = highlightedNodes.union(highlightedEdges);

        highlightedElements = highlightedElements.add(nodes[i]);
        highlightedElements.data('highlighted', 'true');
      }
    }

    this._vis.nodes(":visible").nodes("[highlighted!='true']").css(this.notHighlightNodeCSS);
    this._vis.edges(":visible").edges("[highlighted!='true']").css(this.notHighlightEdgeCSS);
    this._vis.nodes(":visible").nodes("[highlighted='true']").removeCss();
    this._vis.edges(":visible").edges("[highlighted='true']").removeCss();
    this._vis.nodes(":visible").css("show-details", "false");

    this._vis.layout({
      'name': 'preset',
      'fit':  false
    });

};

/**
 * Removes all highlights from the visualization.
 *
  */
NetworkVis.prototype._removeHighlights = function()
{
  // this._vis.nodes(":visible").nodes("[highlighted!='true']").removeCss();
  // this._vis.edges(":visible").edges("[highlighted!='true']").removeCss();
  // this._vis.nodes(":visible").nodes().removeData("highlighted");
  // this._vis.edges(":visible").edges().removeData("highlighted");
  // this._vis.nodes(":visible").css("show-details", "false");

  this._vis.nodes(":visible").nodes("[highlighted!='true']").removeCss();
  this._vis.edges(":visible").edges("[highlighted!='true']").removeCss();
  this._vis.nodes().removeData("highlighted");
  this._vis.edges().removeData("highlighted");
  this._vis.nodes().css("show-details", "false");


  this._vis.layout({
    'name': 'preset',
    'fit':  false
  });
};

/**
 * Displays the gene legend in a separate panel.
 */
NetworkVis.prototype._showNodeLegend = function()
{
    // open legend panel
    $(this.geneLegendSelector).dialog("open").height("auto");
};

/**
 * Displays the drug legend in a separate panel.
 */
NetworkVis.prototype._showDrugLegend = function()
{
    // open legend panel
    $(this.drugLegendSelector).dialog("open").height("auto");
};

/**
 * Displays the edge legend in a separate panel.
 */
NetworkVis.prototype._showEdgeLegend = function()
{

//  $("#edge_legend .in-same-component .color-bar").css(
//      "background-color", "#CD976B");
//
//  $("#edge_legend .reacts-with .color-bar").css(
//      "background-color", "#7B7EF7");
//
//  $("#edge_legend .state-change .color-bar").css(
//      "background-color", "#67C1A9");
//
//  $("#edge_legend .other .color-bar").css(
//          "background-color", "#A583AB");
//
//  $("#edge_legend .merged-edge .color-bar").css(
//      "background-color", "#666666");

    // open legend panel
    //$("#edge_legend").dialog("open").height("auto");
    $(this.edgeLegendSelector).dialog("open");
};

/**
 * Displays the pop-up window in a separate panel.
 */
NetworkVis.prototype._showInteractionTypeVisibility = function()
{

    $(this.interactionTypeVisibilitySelector).dialog("open");
};

/**
 * Displays the edge legend in a separate panel.
 */
NetworkVis.prototype._showInteractionSourceVisibility = function()
{
    $(this.interactionSourceVisibilitySelector).dialog("open");
};

/**
 * Adds a data row to the node or edge inspector.
 *
 * @param selector  node or edge inspector selector (div id)
 * @param type      type of the inspector (should be "node" or "edge")
 * @param label     label of the data field
 * @param value     value of the data field
 * @param section   optional class value for row element
 */
NetworkVis.prototype._addDataRow = function(selector, type, label, value /*, section*/)
{
    var section = arguments[4];

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
        value = this.UNKNOWN;
    }

    $(selector + " ." + type + "_inspector_content .data").append(
        '<tr align="left" class="' + section + 'data-row"><td>' +
        '<strong>' + label + ':</strong> ' + value +
        '</td></tr>');
};

/**
 * Adds a cross reference entry to the node or edge inspector.
 *
 * @param selector  node or edge inspector selector (div id)
 * @param type      type of the inspector (should be "node" or "edge")
 * @param href      URL of the reference
 * @param label     label to be displayed
 */
NetworkVis.prototype._addXrefEntry = function(selector, type, href, label)
{
    $(selector + " ." + type + "_inspector_content .xref-row td").append(
        '<a href="' + href + '" target="_blank">' +
        label + '</a>');
};

/**
 * Generates the URL and the display text for the given xref string.
 *
 * @param xref  xref as a string
 * @return      array of href and text pairs for the given xref
 */
NetworkVis.prototype._resolveXref = function(xref)
{
    var link = null;

    if (xref != null)
    {
        // split the string into two parts
        var pieces = xref.split(":", 2);

        // construct the link object containing href and text
        link = new Object();

        link.href = this._linkMap[pieces[0].toLowerCase()];

        if (link.href == null)
        {
            // unknown source
            link.href = "#";
        }
        // else, check where search id should be inserted
        else if (link.href.indexOf(this.ID_PLACE_HOLDER) != -1)
        {
            link.href = link.href.replace(this.ID_PLACE_HOLDER, pieces[1]);
        }
        else
        {
            link.href += pieces[1];
        }

        link.text = xref;
        link.pieces = pieces;
    }

    return link;
};

/**
 * Sets the default values of the control flags.
 */
NetworkVis.prototype._resetFlags = function()
{
    this._autoLayout = true;
    this._removeDisconnected = true;
    this._nodeLabelsVisible = true;
    this._edgeLabelsVisible = false;
    this._panZoomVisible = true;
    this._linksMerged = true;
    this._profileDataVisible = false;
    this._selectFromTab = false;
};

/**
 * Sets the visibility of the complete UI.
 *
 * @param visible   a boolean to set the visibility.
 */
NetworkVis.prototype._setVisibility = function(visible)
{
    if (visible)
    {
        if ($(this.networkTabsSelector).hasClass("hidden-network-ui"))
        //if ($("#network_menu_div").hasClass("hidden-network-ui"))
        {
            $(this.mainMenuSelector).removeClass("hidden-network-ui");
            $(this.quickInfoSelector).removeClass("hidden-network-ui");
            $(this.networkTabsSelector).removeClass("hidden-network-ui");
            $(this.edgeInspectorSelector).removeClass("hidden-network-ui");
            $(this.geneLegendSelector).removeClass("hidden-network-ui");
            $(this.drugLegendSelector).removeClass("hidden-network-ui");
            $(this.edgeLegendSelector).removeClass("hidden-network-ui");
            $(this.settingsDialogSelector).removeClass("hidden-network-ui");
        }
    }
    else
    {
        if (!$(this.networkTabsSelector).hasClass("hidden-network-ui"))
        //if (!$("#network_menu_div").hasClass("hidden-network-ui"))
        {
            $(this.mainMenuSelector).addClass("hidden-network-ui");
            $(this.quickInfoSelector).addClass("hidden-network-ui");
            $(this.networkTabsSelector).addClass("hidden-network-ui");
            $(this.edgeInspectorSelector).addClass("hidden-network-ui");
            $(this.geneLegendSelector).addClass("hidden-network-ui");
            $(this.drugLegendSelector).addClass("hidden-network-ui");
            $(this.edgeLegendSelector).addClass("hidden-network-ui");
            $(this.settingsDialogSelector).addClass("hidden-network-ui");
        }
    }
};

/**
 * Sets visibility of the given UI component.
 *
 * @param component an html UI component
 * @param visible   a boolean to set the visibility.
 */
NetworkVis.prototype._setComponentVis = function(component, visible)
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
};

/**
 * Creates an array containing default option values for the ForceDirected
 * layout.
 *
 * @return  an array of default layout options
 */
NetworkVis.prototype._defaultOptsArray = function()
{
    var defaultOpts =
        [ { id: "gravity", label: "Gravity",       value: 0.4,   tip: "Gravitonal constant to keep graph components together" },
            { id: "fit",         label: "Graph fitting",      value: true,    tip: "Whether or not to fit the network into canvas after layout" },
            { id: "edgeElasticity",  label: "Edge elasticity",  value: 0.45, tip: "Divisor to computer edge forces" },
            { id: "animate",        label: "Animation", value: false,    tip: "Whether or not to display network during layout" },
            { id: "randomize",        label: "Randomize", value: true,    tip: "Uncheck for incremental layout" },
            { id: "padding", label: "Padding value",  value: 10,      tip: "Padding on fit" },
            { id: "nodeRepulsion", label: "Node Repulsion",  value: 4500,  tip: "Node repulsion (non-overlapping) multiplier" },
            { id: "idealEdgeLength",     label: "Ideal edge length",      value: 50,  tip: "Ideal length of an edge" },
            { id: "nestingFactor", label: "Nesting factor",  value: 0.1,   tip: "Nesting factor (multiplier) to compute ideal edge length for nested edges" },
            { id: "numIter",     label: "Iteration num",      value: 2500,  tip: "Maximum number of iterations" },
            { id: "tile",        label: "Tile Disconnected",  value: true,    tip: "Whether or not to tile disconnected nodes on layout" }
];

    return defaultOpts;
};

/**
 * Creates a map for xref entries.
 *
 * @return  an array (map) of xref entries
 */
NetworkVis.prototype._xrefArray = function()
{
    var linkMap = {};

    // TODO find missing links (Nucleotide Sequence Database)
    //linkMap["refseq"] =   "http://www.genome.jp/dbget-bin/www_bget?refseq:";
    linkMap["refseq"] = "http://www.ncbi.nlm.nih.gov/protein/";
    linkMap["entrez gene"] = "http://www.ncbi.nlm.nih.gov/gene?term=";
    linkMap["hgnc"] = "http://www.genenames.org/cgi-bin/quick_search.pl?.cgifields=type&type=equals&num=50&search=" + this.ID_PLACE_HOLDER + "&submit=Submit";
    linkMap["uniprot"] = "http://www.uniprot.org/uniprot/";
    linkMap["uniprotkb"] = "http://www.uniprot.org/uniprot/";
    //linkMap["chebi"] = "http://www.ebi.ac.uk/chebi/advancedSearchFT.do?searchString=" + this.ID_PLACE_HOLDER + "&queryBean.stars=3&queryBean.stars=-1";
    linkMap["chebi"] = "http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI%3A" + this.ID_PLACE_HOLDER;
    linkMap["pubmed"] = "http://www.ncbi.nlm.nih.gov/pubmed?term=";
    linkMap["drugbank"] = "http://www.drugbank.ca/drugs/" + this.ID_PLACE_HOLDER;
    linkMap["kegg"] = "http://www.kegg.jp/dbget-bin/www_bget?dr:" + this.ID_PLACE_HOLDER;
    linkMap["kegg drug"] = "http://www.kegg.jp/dbget-bin/www_bget?dr:" + this.ID_PLACE_HOLDER;
    linkMap["chebi"] = "http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI%3A" + this.ID_PLACE_HOLDER;
    linkMap["chemspider"] = "http://www.chemspider.com/Chemical-Structure." + this.ID_PLACE_HOLDER + ".html";
    linkMap["kegg compund"] = "http://www.genome.jp/dbget-bin/www_bget?cpd:" + this.ID_PLACE_HOLDER;
    linkMap["doi"] = "http://www.nature.com/nrd/journal/v10/n8/full/nrd3478.html?";
    linkMap["nci_drug"] = "http://www.cancer.gov/drugdictionary?CdrID=" + this.ID_PLACE_HOLDER;
    linkMap["national drug code directory"] = "http://www.fda.gov/Safety/MedWatch/SafetyInformation/SafetyAlertsforHumanMedicalProducts/ucm" + this.ID_PLACE_HOLDER + ".htm";
    linkMap["pharmgkb"] = "http://www.pharmgkb.org/gene/" + this.ID_PLACE_HOLDER;
    linkMap["pubchem compund"] = "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=" + this.ID_PLACE_HOLDER + "&loc=ec_rcs";
    linkMap["pubchem substance"] = "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid=" + this.ID_PLACE_HOLDER + "&loc=ec_rss";
    linkMap["pdb"] = "http://www.rcsb.org/pdb/explore/explore.do?structureId=" + this.ID_PLACE_HOLDER;
    linkMap["bindingdb"] = "http://www.bindingdb.org/data/mols/tenK3/MolStructure_" + this.ID_PLACE_HOLDER  + ".html";
    linkMap["genbank"] = "http://www.ncbi.nlm.nih.gov/nucleotide?term=" + this.ID_PLACE_HOLDER;
    linkMap["iuphar"] = "http://www.iuphar-db.org/DATABASE/ObjectDisplayForward?objectId=" + this.ID_PLACE_HOLDER;
    linkMap["drugs product database (dpd)"] = "http://205.193.93.51/dpdonline/searchRequest.do?din=" + this.ID_PLACE_HOLDER;
    linkMap["guide to pharmacology"] = "http://www.guidetopharmacology.org/GRAC/LigandDisplayForward?ligandId=" + this.ID_PLACE_HOLDER;
    linkMap["nucleotide sequence database"] = "";

    return linkMap;
};

/**
 * Creates a map for edge type visibility.
 *
 * @return  an array (map) of edge type visibility.
 */
NetworkVis.prototype._edgeTypeArray = function()
{
    var typeArray = {};

    var edges = this._vis.edges();

    for (var i = 0; i < edges.length; i++)
    {
       if(edges[i]._private.data.type != null)
       {
            var type = edges[i]._private.data.type.toUpperCase();
            type = type.replace(/-/g, '_');
            // by default every edge type is visible
            typeArray[type] = true;
            //this.visibilityOfType[type] = true;
        }
    }

    return typeArray;
};

/**
 * Creates a map for edge source visibility.
 *
 * @return  an array (map) of edge source visibility.
 */
NetworkVis.prototype._edgeSourceArray = function()
{
    var sourceArray = {};

    // dynamically collect all sources

    var edges = this._vis.edges();
    var source;

    for (var i = 0; i < edges.length; i++)
    {
    	if(edges[i]._private.data.INTERACTION_DATA_SOURCE != null)
    	{
            sources = (edges[i]._private.data.INTERACTION_DATA_SOURCE).split(';');

            if (sources != null)
            {
                for (var j = 0; j < sources.length; j++)
                {
                  // by default every edge source is visible
                  sourceArray[sources[j]] = true;
                  this.visibilityOfSource[sources[j]] = true;
                }
            }
    	}

    }

    // also set a flag for unknown (undefined) sources
    sourceArray[this.UNKNOWN] = true;
    this.visibilityOfSource[this.UNKNOWN] = true;

    return sourceArray;
};

/**
 * Calculates weight values for each gene by using the formula:
 *
 * weight = Max[(Total Alteration of a node),
 *    Max(Total Alteration of its neighbors) * coeff] * 100
 *
 * @param coeff coefficient value used in the weight function
 * @returns     a map (array) containing weight values for each gene
 */
NetworkVis.prototype._geneWeightArray = function(coeff)
{
    var weightArray = {};

    if (coeff > 1)
    {
        coeff = 1;
    }
    else if (coeff < 0)
    {
        coeff = 0;
    }

    // calculate weight values for each gene

    var nodes = this._vis.nodes();
    var max, weight, neighbors;

    for (var i = 0; i < nodes.length; i++)
    {
        // get the total alteration of the current node

        if (nodes[i]._private.data["PERCENT_ALTERED"] != null)
        {
            weight = nodes[i]._private.data["PERCENT_ALTERED"];
        }
        else
        {
            weight = 0;
        }

        // get first neighbors of the current node
        //TODO uncomment the line and replace with neighbor function of Cytoscape js
//        neighbors = this._vis.firstNeighbors([nodes[i]]).neighbors;
        max = 0;

        // find the max of the total alteration of its neighbors,
        // if coeff is not 0
        if (coeff > 0)
        {
            for (var j = 0; j < neighbors.length; j++)
            {
                if (neighbors[j]._private.data["PERCENT_ALTERED"] != null)
                {
                    if (neighbors[j]._private.data["PERCENT_ALTERED"] > max)
                    {
                        max = neighbors[j]._private.data["PERCENT_ALTERED"];
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
        weightArray[nodes[i]._private.data.id] = weight * 100;
    }

    return weightArray;
};

/**
 * Finds the non-seed gene having the maximum alteration percent in
 * the network, and returns the maximum alteration percent value.
 *
 * @param map   weight map for the genes in the network
 * @return      max alteration percent of non-seed genes
 */
NetworkVis.prototype._maxAlterValNonSeed = function(map)
{
    var max = 0.0;

    for (var key in map)
    {
        // skip seed genes

        var node = this._vis.$("#" + key)[0];

        if (node != null &&
            node._private.data["IN_QUERY"] == "true")
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
};

/**
 * Initializes the main menu by adjusting its style. Also, initializes the
 * inspector panels and tabs.
 */
NetworkVis.prototype._initMainMenu = function()
{
    _initMenuStyle(this.divId, this.HOVERED_CLASS);

    // adjust separators between menu items

    $(this.mainMenuSelector + " #network_menu_file").addClass(this.MENU_CLASS);
    $(this.mainMenuSelector + " #network_menu_topology").addClass(this.MENU_CLASS);
    $(this.mainMenuSelector + " #network_menu_view").addClass(this.MENU_CLASS);
    $(this.mainMenuSelector + " #network_menu_layout").addClass(this.MENU_CLASS);
    $(this.mainMenuSelector + " #network_menu_legends").addClass(this.MENU_CLASS);

    $(this.mainMenuSelector + " #save_as_png").addClass(this.FIRST_CLASS);
    $(this.mainMenuSelector + " #save_as_png").addClass(this.MENU_SEPARATOR_CLASS);
    $(this.mainMenuSelector + " #save_as_png").addClass(this.LAST_CLASS);

    $(this.mainMenuSelector + " #hide_selected").addClass(this.FIRST_CLASS);
    $(this.mainMenuSelector + " #hide_selected").addClass(this.MENU_SEPARATOR_CLASS);
    $(this.mainMenuSelector + " #remove_disconnected").addClass(this.MENU_SEPARATOR_CLASS);
    $(this.mainMenuSelector + " #remove_disconnected").addClass(this.LAST_CLASS);

    $(this.mainMenuSelector + " #show_profile_data").addClass(this.FIRST_CLASS);
    $(this.mainMenuSelector + " #show_profile_data").addClass(this.MENU_SEPARATOR_CLASS);
    $(this.mainMenuSelector + " #highlight_neighbors").addClass(this.MENU_SEPARATOR_CLASS);
    $(this.mainMenuSelector + " #remove_highlights").addClass(this.LAST_CLASS);

    $(this.mainMenuSelector + " #perform_layout").addClass(this.FIRST_CLASS);
    $(this.mainMenuSelector + " #perform_layout").addClass(this.MENU_SEPARATOR_CLASS);
    //$("#layout_properties").addClass(SUB_MENU_CLASS);
    $(this.mainMenuSelector + " #auto_layout").addClass(this.MENU_SEPARATOR_CLASS);
    $(this.mainMenuSelector + " #auto_layout").addClass(this.LAST_CLASS);

    $(this.mainMenuSelector + " #show_gene_legend").addClass(this.FIRST_CLASS);
    $(this.mainMenuSelector + " #show_edge_legend").addClass(this.LAST_CLASS);

    // init check icons for checkable menu items
    this._updateMenuCheckIcons();
};

/**
 * Updates check icons of the checkable menu items.
 */
NetworkVis.prototype._updateMenuCheckIcons = function()
{
    if (this._autoLayout)
    {
        $(this.mainMenuSelector + " #auto_layout").addClass(this.CHECKED_CLASS);
    }
    else
    {
        $(this.mainMenuSelector + " #auto_layout").removeClass(this.CHECKED_CLASS);
    }

    if (this._removeDisconnected)
    {
        $(this.mainMenuSelector + " #remove_disconnected").addClass(this.CHECKED_CLASS);
    }
    else
    {
        $(this.mainMenuSelector + " #remove_disconnected").removeClass(this.CHECKED_CLASS);
    }

    if (this._nodeLabelsVisible)
    {
        $(this.mainMenuSelector + " #show_node_labels").addClass(this.CHECKED_CLASS);
    }
    else
    {
        $(this.mainMenuSelector + " #show_node_labels").removeClass(this.CHECKED_CLASS);
    }

    if (this._edgeLabelsVisible)
    {
        $(this.mainMenuSelector + " #show_edge_labels").addClass(this.CHECKED_CLASS);
    }
    else
    {
        $(this.mainMenuSelector + " #show_edge_labels").removeClass(this.CHECKED_CLASS);
    }

    if (this._panZoomVisible)
    {
        $(this.mainMenuSelector + " #show_pan_zoom_control").addClass(this.CHECKED_CLASS);
    }
    else
    {
        $(this.mainMenuSelector + " #show_pan_zoom_control").removeClass(this.CHECKED_CLASS);
    }

    if (this._linksMerged)
    {
        $(this.mainMenuSelector + " #merge_links").addClass(this.CHECKED_CLASS);
    }
    else
    {
        $(this.mainMenuSelector + " #merge_links").removeClass(this.CHECKED_CLASS);
    }

    if (this._profileDataVisible)
    {
        $(this.mainMenuSelector + " #show_profile_data").addClass(this.CHECKED_CLASS);
    }
    else
    {
        $(this.mainMenuSelector + " #show_profile_data").removeClass(this.CHECKED_CLASS);
    }
};

/**
 * Initializes dialog panels for node inspector, edge inspector, and layout
 * settings.
 */
NetworkVis.prototype._initDialogs = function()
{
    var self = this;
    // adjust settings panel
    $(this.settingsDialogSelector).dialog({autoOpen: false,
                                     resizable: false,
                                     width: 333});

    // adjust edge inspector
    $(this.edgeInspectorSelector).dialog({autoOpen: false,
                                    resizable: false,
                                    width: 366,
                                    maxHeight: 300});

    // adjust node legend
    $(this.geneLegendSelector).dialog({autoOpen: false,
                                 resizable: false,
                                 width: 500});

    // adjust drug legend
    $(this.drugLegendSelector).dialog({autoOpen: false,
                                 resizable: false,
                                 width: 320});

    // adjust edge legend
    $(this.edgeLegendSelector).dialog({autoOpen: false,
                                 resizable: false,
                                 width: 400});

    //adjust edge typ2 UI visibility dialog
    $(this.interactionTypeVisibilitySelector).dialog({autoOpen: false,
                                   resizable: false,
                                   width: 300,
                                   close: function( event, ui ) {self._closeInteractionTypePopUp(false)}});

   //adjust edge source UI visibility dialog
   $(this.interactionSourceVisibilitySelector).dialog({autoOpen: false,
                                  resizable: false,
                                  width: 300,
                                  close: function( event, ui ) {self._closeInteractionSourcePopUp(false)}});
};

/**
 * Initializes the drop down menu.
 */
NetworkVis.prototype._initDropDown = function()
{
    var self = this;

    var changeListener = function(){
        self._changeListener();
    };

    // add select listener for drop down menu
    $(this.drugFilterSelector).change(changeListener);
    //_changeListener();
};


/**
 * Initializes the gene filter sliders.
 */
NetworkVis.prototype._initSliders = function()
{
    var self = this;

    var keyPressListener = function(evt) {
      self._keyPressListener(evt);
    };

    var weightSliderStop = function(evt, ui) {
        self._weightSliderStop(evt, ui);
    };

    var weightSliderMove = function(evt, ui) {
        self._weightSliderMove(evt, ui);
    };


    // add key listeners for input fields

    $(this.genesTabSelector + " #weight_slider_field").keypress(keyPressListener);
    $(this.genesTabSelector + " #affinity_slider_field").keypress(keyPressListener);

    // show gene filtering slider
    $(this.genesTabSelector + " #weight_slider_bar").slider(
        {value: this.ALTERATION_PERCENT,
            stop: weightSliderStop,
            slide: weightSliderMove});

    // set max alteration value label
    //$("#weight_slider_area .slider-max label").text(
    //  _maxAlterationPercent.toFixed(1));

    // show affinity slider (currently disabled)
//  $("#affinity_slider_bar").slider(
//      {value: WEIGHT_COEFF * 100,
//      change: _affinitySliderChange,
//      slide: _affinitySliderMove});
};

/**
 * Recursive function, that adds a new line after each 60 characters in given parameter and returns it
 * */
NetworkVis.prototype._adjustToolTipText = function(text)
{
    if (text.length > 60)
    {
        return text.substr(0,60) + "\n" +  _adjustToolTipText(text.substr(60,text.length));
    }
    else
        return text;
};

/**
 * Initializes tooltip style for genes.
 *
 *
 */
 //TODO look at this part again!!!
NetworkVis.prototype._initTooltipStyle = function()
{
    // create a function and add it to the Visualization object
/*    this._vis["customTooltip"] = function (data)
    {
        var text;

        if (data.type != this.DRUG)
        {
            if (data["PERCENT_ALTERED"] == null)
            {
                text = "n/a";
            }
            else
            {
                text = Math.round(100 * data["PERCENT_ALTERED"]) + "%";
            }
        }
        // For Drug Nodes, their full label are shown on mouse over, in tool tip
        else
        {

            text = this._adjustToolTipText(data.label);
        }

        return "<b>" + text + "</b>";
        //return text;
    };

    // register the custom mapper to the tooltipText

    var style = this._vis.visualStyle();
    style.nodes.tooltipText = { customMapper: { functionName: "customTooltip" } };

    // set the visual style again
    this._vis.visualStyle(style);

    // enable node tooltips
    this._vis.nodeTooltipsEnabled(true);
    */
};

NetworkVis.prototype._adjustIE = function()
{
    if (_isIE())
    {
        // this is required to position scrollbar on IE
        //var width = $("#help_tab").width();
        //$("#help_tab").width(width * 1.15);
    }
};

/**
 * Listener for weight slider movement. Updates current value of the slider
 * after each mouse move.
 */
NetworkVis.prototype._weightSliderMove = function(event, ui)
{
    // get slider value
    var sliderVal = ui.value;

    // update current value field
    $(this.genesTabSelector + "#weight_slider_field").val(
        (_transformValue(sliderVal) * (this._maxAlterationPercent / 100)).toFixed(1));
};

/**
 * Listener for weight slider value change. Updates filters with respect to
 * the new slider value.
 */
NetworkVis.prototype._weightSliderStop = function(event, ui)
{
    // get slider value
    var sliderVal = ui.value;

    // apply transformation to prevent filtering of low values
    // with a small change in the position of the cursor.
    sliderVal = _transformValue(sliderVal) * (this._maxAlterationPercent / 100);

    // update threshold
    this._geneWeightThreshold = sliderVal;

    // update current value field
    $(this.genesTabSelector + " #weight_slider_field").val(sliderVal.toFixed(1));

    // update filters
    this._filterBySlider();
};

/**
 *Filters drugs by the drop down menu.
 */
NetworkVis.prototype._filterByDropDown = function()
{
    // remove previous filters due to slider
    for (var key in this._filteredByDropDown)
    {
        this._alreadyFiltered[key] = null;
    }

    // remove previous filters due to disconnection
    for (var key in this._filteredByIsolation)
    {
        this._alreadyFiltered[key] = null;
    }

    // reset required filter arrays
    this._filteredByDropDown = new Array();
    this._filteredByIsolation = new Array();

    // remove filters
    //_vis.removeFilter("nodes", false);

    // this is required to pass "this" instance to the listener
    var self = this;

    var dropDownVisibility = function(element){
        return self.dropDownVisibility(element);
    };

    // filter with new drop down selection
    this._vis.nodes().forEach(function( ele ){
        if (dropDownVisibility(ele) === false){
          ele.css('visibility', 'hidden');
          ele._private.selectable = false;
        }
        else{
          ele._private.selectable = true;

          ele.css('visibility', 'visible');
        }
    });

    // also, filter disconnected nodes if necessary
    this._filterDisconnected();

    // visualization changed, perform layout if necessary
    this._visChanged();
};

/**
 * Filters genes by the current gene weight threshold value determined by
 * the weight slider.
 */
NetworkVis.prototype._filterBySlider = function()
{
    // remove previous filters due to slider
    for (var key in this._filteredBySlider)
    {
        this._alreadyFiltered[key] = null;
    }

    // remove previous filters due to disconnection
    for (var key in this._filteredByIsolation)
    {
        this._alreadyFiltered[key] = null;
    }

    // reset required filter arrays
    this._filteredBySlider = new Array();
    this._filteredByIsolation = new Array();

    // remove filters
    //_vis.removeFilter("nodes", false);

    // this is required to pass "this" instance to the listener
    var self = this;

    var sliderVisibility = function(element){
        return self.sliderVisibility(element);
    };

    // filter with new slider value
    this._vis.nodes().forEach(function( ele ){
        if (sliderVisibility(ele) === false){
          ele.css('visibility', 'hidden');
          ele.unselect();
          ele._private.selectable = false;
        }
        else{
          ele._private.selectable = true;

          ele.css('visibility', 'visible');
        }
    });

    // also, filter disconnected nodes if necessary
    this._filterDisconnected();

    // refresh & update genes tab
    this._refreshGenesTab();
    this.updateGenesTab();

    // visualization changed, perform layout if necessary
    this._visChanged();
};

/**
 * Listener for affinity slider movement. Updates current value of the slider
 * after each mouse move.
 */
NetworkVis.prototype._affinitySliderMove = function(event, ui)
{
    // get slider value
    var sliderVal = ui.value;

    // update current value field
    $(this.genesTabSelector + " #affinity_slider_field").val((sliderVal / 100).toFixed(2));
};

/**
 * Listener for affinity slider value change. Updates filters with respect to
 * the new slider value.
 */
NetworkVis.prototype._affinitySliderChange = function(event, ui)
{
    var sliderVal = ui.value;

    // update current value field
    $(this.genesTabSelector + " #affinity_slider_field").val((sliderVal / 100).toFixed(2));

    // re-calculate gene weights
    this._geneWeightMap = this._geneWeightArray(sliderVal / 100);

    // update filters
    this._filterBySlider();
};

/**
 *changeListener for the changes on the drop down menu
 *
 */
NetworkVis.prototype._changeListener = function()
{
    //update drug filters
    this._filterByDropDown();
};

/**
 * Key listener for input fields on the genes tab.
 * Updates the slider values (and filters if necessary), if the input
 * value is valid.
 *
 * @param event     event triggered the action
 */
NetworkVis.prototype._keyPressListener = function(event)
{
    var input;

    // check for the ENTER key first
    if (event.keyCode == this.ENTER_KEYCODE)
    {
        if (event.target.id == "weight_slider_field")
        {
            input = $(this.genesTabSelector + " #weight_slider_field").val();

            // update weight slider position if input is valid

            if (isNaN(input))
            {
                // not a numeric value, update with defaults
                input = this.ALTERATION_PERCENT;
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

            $(this.genesTabSelector + " #weight_slider_bar").slider("option", "value",
                                           _reverseTransformValue(input / (this._maxAlterationPercent / 100)));

            // update threshold value
            this._geneWeightThreshold = input;

            // also update filters
            this._filterBySlider();
        }
        else if (event.target.id == "affinity_slider_field")
        {
            input = $(this.genesTabSelector + " #affinity_slider_field").val();

            var value;
            // update affinity slider position if input is valid
            // (this will also update filters if necessary)

            if (isNaN(input))
            {
                // not a numeric value, update with defaults
                value = this.WEIGHT_COEFF;
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

            $(this.genesTabSelector + " #affinity_slider_bar").slider("option",
                                             "value",
                                             Math.round(input * 100));
        }
        else if (event.target.id == "search_box")
        {
            this.searchGene();
        }
    }
};

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


 // for (var i=0; i < nodes.length; i++)
 // {
 //     $("#genes_tab .genes_list").append(
 //         "<li> " + nodes[i].data.id + "</li>");
 // }


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
 * Initializes Genes tab.
 */
NetworkVis.prototype._initGenesTab = function()
{
    // init buttons

    $(this.genesTabSelector + " #filter_genes").button({icons: {primary: 'ui-icon-circle-minus'},
                                  text: false});

    $(this.genesTabSelector + " #crop_genes").button({icons: {primary: 'ui-icon-crop'},
                                text: false});

    $(this.genesTabSelector + " #unhide_genes").button({icons: {primary: 'ui-icon-circle-plus'},
                                  text: false});

    $(this.genesTabSelector + " #search_genes").button({icons: {primary: 'ui-icon-search'},
                                  text: false});

    /*$(this.relationsTabSelector + " #update_edges").button({icons: {primary: 'ui-icon-refresh'},
                                  text: false});*/

    // re-submit button is initially disabled
    $(this.genesTabSelector + " #re-submit_query").button({icons: {primary: 'ui-icon-play'},
                                     text: false,
                                     disabled: true});

    // $(this.genesTabSelector + " #re-run_query").button({label: "Re-run query with selected genes"});

    // apply tiptip to all buttons on the network tabs
    $(this.networkTabsSelector + " button").tipTip({edgeOffset:8});
};


/**
 * Refreshes the content of the genes tab, by populating the list with visible
 * (i.e. non-filtered) genes.
 */
NetworkVis.prototype._refreshGenesTab = function()
{
    // (this is required to pass "this" instance to the listener functions)
    var self = this;

    var showGeneDetails = function(evt){
        $(self.networkTabsSelector).tabs("option", "active", 2);
    };

    // get visible genes
    var geneList = this._visibleGenes();

    // clear old content
    $(this.geneListAreaSelector + " select").remove();

    $(this.geneListAreaSelector).append('<select multiple title="Select genes"></select>');

    // add new content

    for (var i=0; i < geneList.length; i++)
    {
        // use the safe version of the gene id as an id of an HTML object
        var safeId = _safeProperty(geneList[i]._private.data.id);

        var classContent;

        if (geneList[i]._private.data["IN_QUERY"] == "true")
        {
            classContent = 'class="in-query" ';
        }
        else
        {
            classContent = 'class="not-in-query" ';
        }

        $(this.geneListAreaSelector + " select").append(
            '<option id="' + safeId + '" ' +
            classContent +
            'value="' + geneList[i]._private.data.id + '" ' + '>' +
            '<label>' + geneList[i]._private.data.label + '</label>' +
            '</option>');

        // add double click listener for each gene

        $(this.genesTabSelector + " #" + safeId).dblclick(showGeneDetails);

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

    var updateSelectedGenes = function(evt){
        self.updateSelectedGenes(evt);
    };

    // add change listener to the select box
    $(this.geneListAreaSelector + " select").change(updateSelectedGenes);

    if (_isIE())
    {
        // listeners on <option> elements do not work in IE, therefore add
        // double click listener to the select box
        $(this.geneListAreaSelector + " select").dblclick(showGeneDetails);

        // TODO if multiple genes are selected, double click always shows
        // the first selected gene's details in IE
    }
};



/**
 * Refreshes the content of the relations tab, by calculating percentages for
 * each edge type.
 */
NetworkVis.prototype._refreshRelationsTabUIVisibility = function()
{
    //Hide interaction type data from UI if it is selected to be !
    for (var key in this.visibilityOfType)
    {
      if (this.visibilityOfType[key] && !this.percentageFilterMap[key])
      {
          // do not display OTHER if its percentage is zero
          this._setComponentVis($(this.relationsTabSelector + " ."+ this.edgeTypeConstants[key].name), true);

          // also do not display it in the edge legend
          //_setComponentVis($("#edge_legend .other"), false);
      }
      else
      {
          this._setComponentVis($(this.relationsTabSelector + " ."+ this.edgeTypeConstants[key].name), false);
          //_setComponentVis($("#edge_legend .other"), true);
      }
    }

    //Hide interaction source data from UI if it is selected to be !
    for (var key in this.visibilityOfSource)
    {
      if (this.visibilityOfSource[key])
      {
          // do not display OTHER if its percentage is zero
          this._setComponentVis($(this.relationsTabSelector + " ."+ key), true);

          // also do not display it in the edge legend
          //_setComponentVis($("#edge_legend .other"), false);
      }
      else
      {
          this._setComponentVis($(this.relationsTabSelector + " ."+ key), false);
          //_setComponentVis($("#edge_legend .other"), true);
      }
    }
}
/**
 * Initializes the content of the relations tab, by adding content according to
 * interaction types and sources.
 */
NetworkVis.prototype._initRelationsTab = function()
{
  $(this.relationsTabSelector + " #edge_type_filter").empty();
  $(this.relationsTabSelector + " #edge_source_filter").empty();

  $(this.relationsTabSelector + " #edge_type_filter").append('<tr class="edge-type-header">\
      <td>\
        <label class="heading">Type:</label>\
      </td>\
    </tr>');

  for (var key in this.edgeTypeConstants)
  {
    $(this.relationsTabSelector + " #edge_type_filter").append
    (
      '<tr  class="'+this.edgeTypeConstants[key].name +'">\
        <td class="edge-type-checkbox" style="padding-left: 10px;">\
          <label>'+this.edgeTypeConstants[key].desc +'</label>\
        </td>\
      </tr>\
      <tr class="'+this.edgeTypeConstants[key].name+'">\
        <td style="padding-left: 10px;">\
          <div class="percent-bar"></div>\
        </td>\
        <td style="padding-left: 10px;">\
          <div class="percent-value"></div>\
        </td>\
      </tr>'
    );
  }

  $(this.relationsTabSelector + " #edge_type_filter").append('<tr><td><button id="showInteractionTypeVisibilityUI" style="margin-top: 10px; margin-bottom: 20px;" type="button">Modify...</button></td></tr>');

  // add source filtering options
  $(this.relationsTabSelector + " #edge_source_filter").append('<tr class="edge-source-header">\
  		<td>\
  			<label class="heading">Source:</label>\
  		</td>\
  	</tr>');

  for (var key in this._edgeSourceVisibility)
  {
      $(this.relationsTabSelector + " #edge_source_filter").append(
          '<tr class="' + _safeProperty(key) + '">' +
          '<td class="edge-source-checkbox" style="padding-left: 10px;">' +
          '<label>' + key + '</label>' +
          '</td></tr>');
  }

  $(this.relationsTabSelector + " #edge_source_filter").append('<tr><td><button id="showInteractionSourceVisibilityUI" style="margin-top: 10px" type="button">Modify...</button></td></tr>');
}

/**
 * Refreshes the content of the relations tab, by calculating percentages for
 * each edge type.
 */
NetworkVis.prototype._refreshRelationsTab = function()
{
    var edges = this._vis.edges();

    // initialize percentages of each edge type
    var percentages = {};

    //Init all percentages according to edgeType
    for (var key in this.edgeTypeConstants)
    {
      percentages[this.edgeTypeConstants[key].name] = 0;
    }

    // for each edge increment count of the correct edge type
    for (var i=0; i < edges.length; i++)
    {
    	percentages[edges[i]._private.data.type] += 1;
    }


    //Hide interaction types with 0 percent !
    for (var key in this.edgeTypeConstants)
    {
      if (percentages[this.edgeTypeConstants[key].name] === 0)
      {
          // do not display OTHER if its percentage is zero
          this._setComponentVis($(this.relationsTabSelector + " ."+ this.edgeTypeConstants[key].name), false);
          this.percentageFilterMap[key] = true;
          // also do not display it in the edge legend
          //_setComponentVis($("#edge_legend .other"), false);
      }
      else
      {
          this._setComponentVis($(this.relationsTabSelector + " ."+ this.edgeTypeConstants[key].name), true);
          this.percentageFilterMap[key] = false;
          //_setComponentVis($("#edge_legend .other"), true);
      }
    }

    // calculate percentages and add content to the tab
    var percent;

    for (var key in this.edgeTypeConstants)
    {
      percent = (percentages[this.edgeTypeConstants[key].name] * 100 / edges.length);

      $(this.relationsTabSelector + " ."+ this.edgeTypeConstants[key].name +" .percent-bar").css(
          "width", Math.ceil(percent * 0.85) + "%");

      $(this.relationsTabSelector + " ."+ this.edgeTypeConstants[key].name +" .percent-bar").css(
          "background-color", this.edgeTypeConstants[key].color);

      $(this.relationsTabSelector + " ."+ this.edgeTypeConstants[key].name +" .percent-value").text(
          percent.toFixed(1) + "%");
    }

    // TODO remove old source filters?
    //$(this.relationsTabSelector + " #edge_source_filter tr").remove();


    // <tr class="unknown">
    //      <td class="edge-source-checkbox">
    //              <input type="checkbox" checked="checked">
    //              <label> Unknown </label>
    //      </td>
    // </tr>
};

/**
 * Creates a map (an array) with <command, function> pairs. Also, adds listener
 * functions for the buttons and for the CytoscapeWeb canvas.
 */
NetworkVis.prototype._initControlFunctions = function()
{
    var self = this;

    // define listeners as local variables
    // (this is required to pass "this" instance to the listener functions)
    var showNodeDetails = function(evt) {
        // open details tab instead
        self._vis.elements(':selected').unselect();
        evt.cyTarget.select();
        $(self.networkTabsSelector).tabs("option", "active", 2);
    };

    var handleEdgeSelect = function(evt) {
        self.isEdgeClicked = false;
        self.updateDetailsTab(evt);
    };

    var showEdgeDetails = function(evt) {
        self.isEdgeClicked = true;
        self._vis.elements(':selected').unselect();
        evt.cyTarget.select();
        $(self.networkTabsSelector).tabs("option", "active", 2);
        self.updateDetailsTab(evt);
    };

    var handleNodeSelect = function(evt) {
        self.updateGenesTab(evt);
        self.updateDetailsTab(evt);
    };

    var filterSelectedGenes = function() {
        self.filterSelectedGenes();
    };

    var unhideAll = function() {
        self._unhideAll();
    };

    var performLayout = function() {
        self._performLayout();
    };

    var toggleNodeLabels = function() {
        self._toggleNodeLabels();
    };

    var toggleEdgeLabels = function() {
        self._toggleEdgeLabels();
    };

    var toggleMerge = function() {
        self._toggleMerge();
    };

    var togglePanZoom = function() {
        self._togglePanZoom();
    };

    var toggleAutoLayout = function() {
        self._toggleAutoLayout();
    };

    var toggleRemoveDisconnected = function() {
        self._toggleRemoveDisconnected();
    };

    var toggleProfileData = function() {
        self._toggleProfileData();
    };

    var saveAsPng = function() {
        self._saveAsPng();
    };

	var saveAsSvg = function() {
		self._saveAsSvg();
	};

    var openProperties = function() {
        self._openProperties();
    };

    var highlightNeighbors = function() {
        self._highlightNeighbors();
    };

    var removeHighlights = function() {
        self._removeHighlights();
    };

    var filterNonSelected = function() {
        self.filterNonSelected();
    };

    var showNodeLegend = function() {
        self._showNodeLegend();
    };

    var showDrugLegend = function() {
        self._showDrugLegend();
    };

    var showEdgeLegend = function() {
        self._showEdgeLegend();
    };

    var saveSettings = function() {
        self.saveSettings();
    };

    var defaultSettings = function() {
        self.defaultSettings();
    };

    var searchGene = function() {
        self.searchGene();
    };

    var reRunQuery = function() {
        self.reRunQuery();
    };

    var updateEdges = function() {
        self.updateEdges();
    };

    var keyPressListener = function(evt) {
        self._keyPressListener(evt);
    };

    var handleMenuEvent = function(evt){
        self.handleMenuEvent(evt.target.id);
    };

    var showInteractionTypeVisibility = function () {
        self._showInteractionTypeVisibility();
    };

    var showInteractionSourceVisibility = function () {
        self._showInteractionSourceVisibility();
    };

    var closeInteractionTypePopUp = function () {
        self._closeInteractionTypePopUp(true);
    };

    var closeInteractionSourcePopUp = function () {
        self._closeInteractionSourcePopUp(true);
    };

    var selectAll_InteractionTypeVisibility = function () {
        self._selectAll_InteractionTypeVisibility();
    };

    var unselectAll_InteractionTypeVisibility = function () {
        self._unselectAll_InteractionTypeVisibility();
    };

    var selectAll_InteractionSourceVisibility = function () {
        self._selectAll_InteractionSourceVisibility();
    };

    var unselectAll_InteractionSourceVisibility = function () {
        self._unselectAll_InteractionSourceVisibility();
    };

    this._controlFunctions = {};

    //_controlFunctions["hide_selected"] = _hideSelected;
    this._controlFunctions["hide_selected"] = filterSelectedGenes;
    this._controlFunctions["unhide_all"] = unhideAll;
    this._controlFunctions["perform_layout"] = performLayout;
    this._controlFunctions["show_node_labels"] = toggleNodeLabels;
    //_controlFunctions["show_edge_labels"] = toggleEdgeLabels;
    this._controlFunctions["merge_links"] = toggleMerge;
    this._controlFunctions["show_pan_zoom_control"] = togglePanZoom;
    this._controlFunctions["auto_layout"] = toggleAutoLayout;
    this._controlFunctions["remove_disconnected"] = toggleRemoveDisconnected;
    this._controlFunctions["show_profile_data"] = toggleProfileData;
    this._controlFunctions["save_as_png"] = saveAsPng;
	//this._controlFunctions["save_as_svg"] = saveAsSvg;
    this._controlFunctions["layout_properties"] = openProperties;
    this._controlFunctions["highlight_neighbors"] = highlightNeighbors;
    this._controlFunctions["remove_highlights"] = removeHighlights;
    this._controlFunctions["hide_non_selected"] = filterNonSelected;
    this._controlFunctions["show_node_legend"] = showNodeLegend;
    this._controlFunctions["show_drug_legend"] = showDrugLegend;
    this._controlFunctions["show_edge_legend"] = showEdgeLegend;

    // add menu listeners
    $(this.mainMenuSelector + " #network_menu a").unbind(); // TODO temporary workaround (there is listener attaching itself to every 'a's)
    $(this.mainMenuSelector + " #network_menu a").click(handleMenuEvent);

    $(this.networkTabsSelector + " #help_tab a").click(handleMenuEvent);

    // add button listeners

    $(this.settingsDialogSelector + " #save_layout_settings").click(saveSettings);
    $(this.settingsDialogSelector + " #default_layout_settings").click(defaultSettings);

    $(this.genesTabSelector + " #search_genes").click(searchGene);
    $(this.genesTabSelector + " #search_box").keypress(keyPressListener);
    $(this.genesTabSelector + " #filter_genes").click(filterSelectedGenes);
    $(this.genesTabSelector + " #crop_genes").click(filterNonSelected);
    $(this.genesTabSelector + " #unhide_genes").click(unhideAll);
    $(this.genesTabSelector + " #re-submit_query").click(reRunQuery);

    /*$(this.relationsTabSelector + " #update_edges").click(updateEdges);*/

    $('#showInteractionTypeVisibilityUI').click(showInteractionTypeVisibility);
    $('#showInteractionSourceVisibilityUI').click(showInteractionSourceVisibility);


    $(this.interactionTypeVisibilitySelector + ' input').change
    (
      function()
      {
        var isChecked = $(this).is(':checked');
        var key = $(this).attr('id').substr(0, $(this).attr('id').lastIndexOf('_')).replace(/-/g, '_');
        key = key.toUpperCase();
        self.visibilityOfType[key] = isChecked;
        self._edgeTypeVisibility[self.edgeTypeConstants[key].name] = isChecked;
        self._refreshRelationsTabUIVisibility();
      }
    );

    $(this.interactionSourceVisibilitySelector + ' input').change
    (
      function()
      {
        var isChecked = $(this).is(':checked');
        var key = $(this).attr('id').substr(0, $(this).attr('id').indexOf('_'));
        self.visibilityOfSource[key] = isChecked;
        self._edgeSourceVisibility[key] = isChecked;
        self._refreshRelationsTabUIVisibility();
      }
    );

    $(this.interactionTypeVisibilitySelector + " #close_InteractionTypeVisibilityButton").click(closeInteractionTypePopUp);
    $(this.interactionTypeVisibilitySelector + " #selectAll_InteractionTypeVisibilityButton").click(selectAll_InteractionTypeVisibility);
    $(this.interactionTypeVisibilitySelector + " #unselectAll_InteractionTypeVisibilityButton").click(unselectAll_InteractionTypeVisibility);

    $(this.interactionSourceVisibilitySelector + " #close_InteractionSourceVisibilityButton").click(closeInteractionSourcePopUp);
    $(this.interactionSourceVisibilitySelector + " #selectAll_InteractionSourceVisibilityButton").click(selectAll_InteractionSourceVisibility);
    $(this.interactionSourceVisibilitySelector + " #unselectAll_InteractionSourceVisibilityButton").click(unselectAll_InteractionSourceVisibility);


    // add listener for double click action

    this._vis.on("doubleTap",
                     "node",
                     showNodeDetails);

    this._vis.on("doubleTap",
                     "edge",
                     showEdgeDetails);

    // add listener for edge select & deselect actions
    this._vis.on("select",
                    "edge",
                     handleEdgeSelect);

    this._vis.on("deselect",
                    "edge",
                     handleEdgeSelect);

    // add listener for node select & deselect actions
    this._vis.on("select",
                    "node",
                     handleNodeSelect);

    this._vis.on("deselect",
                    "node",
                     handleNodeSelect);

    // TODO temp debug option, remove when done
    //_vis.addContextMenuItem("node details", "nodes", jokerAction);
};
/**
 * Initializes the layout options by default values and updates the
 * corresponding UI content.
 */
NetworkVis.prototype._initLayoutOptions = function()
{
    this._layoutOptions = this._defaultOptsArray();
    this._updateLayoutOptions();
};

/**
 * Hides (filters) selected nodes and edges.
 */
NetworkVis.prototype._hideSelected = function()
{
    // update selected elements map
    this._selectedElements = _selectedElementsMap("all");

    var self = this;

    var selectionVisibility = function(element){
        return self.selectionVisibility(element);
    };

    // filter out selected elements
    this._vis.elements().forEach(function( ele ){
        if (selectionVisibility(ele) === false || ele._private.style.visibility.strValue == 'hidden'){
          ele.css('visibility', 'hidden');
          self._vis.elements(':selected').unselect();
          ele._private.selectable = false;
        }
        else{
          ele._private.selectable = true;
          ele.css('visibility', 'visible');
        }
    });

    // also, filter disconnected nodes if necessary
    this._filterDisconnected();

    // refresh genes tab
    this._refreshGenesTab();

    // visualization changed, perform layout if necessary
    this._visChanged();
};

/**
 * Removes any existing filters to unhide filtered nodes & edges. However,
 * this operation does not remove the filtering based on edge types.
 */
NetworkVis.prototype._unhideAll = function()
{
    // reset array of already filtered elements
    this._alreadyFiltered = new Array();

    // re-apply filtering based on edge types
    this.updateEdges();

    // refresh & update genes tab
    this._refreshGenesTab();
    this.updateGenesTab();

    // no need to call _visChanged(), since it is already called by updateEdges
    //_visChanged();
};

/**
 * Creates an array of visible (i.e. non-filtered) genes.
 *
 * @return      array of visible genes
 */
NetworkVis.prototype._visibleGenes = function()
{
    var genes = new Array();
    var nodes = this._vis.nodes();

    for (var i=0; i < nodes.length; i++)
    {
        // check if the node is already filtered.
        // also, include only genes, not small molecules or unknown types.
        if (this._alreadyFiltered[nodes[i]._private.data.id] == null &&
            nodes[i]._private.data.type == this.PROTEIN)
        {
            genes.push(nodes[i]);
        }
    }

    // sort genes by label (alphabetically)
    genes.sort(_geneSort);

    return genes;
};

/**
 * Performs the current layout on the graph.
 */
NetworkVis.prototype._performLayout = function()
{
//    var field = { name: "weight", type: "number", defValue: 1.0 };
//    _vis.addDataField("edges", field);
//
//    var edges = _vis.edges();
//
//    for (var i=0; i < edges.length; i++)
//    {
//      if (edges[i].data.type == "DRUG_TARGET")
//      {
//          edges[i].data.weight = 0.2;
//      }
//      else
//      {
//          edges[i].data.weight = 1.0;
//      }
//
//      _vis.updateData("edges", [edges[i]], edges[i].data);
//    }
      //TODO layout options will be changed

    //Perform layout only on visible elements !
    this._vis.filter(':visible').layout(this._graphLayout);
};

/**
 * Toggles the visibility of the node labels.
 */
NetworkVis.prototype._toggleNodeLabels = function()
{
    // update visibility of labels

    this._nodeLabelsVisible = !this._nodeLabelsVisible;
    var tempLabelVisibility = this._nodeLabelsVisible;
    this._vis.nodes().forEach(function( ele ){
        if (tempLabelVisibility === false){
          ele._private.style['content'].strValue= '';
          ele._private.data['tempLabel'] = ele._private.data['label'];
          ele._private.data['label'] = '';
        }
        else{
          ele._private.data['label'] = ele._private.data['tempLabel'];
          ele._private.style['content'].strValue= ele._private.data['label'];
        }
    });
    this._vis.layout({
      name:"preset",
      fit:false
    })
    // update check icon of the corresponding menu item

    var item = $(this.mainMenuSelector + " #show_node_labels");

    if (this._nodeLabelsVisible)
    {
        item.addClass(this.CHECKED_CLASS);
    }
    else
    {
        item.removeClass(this.CHECKED_CLASS);
    }
};

/**
 * Toggles the visibility of the edge labels.
 */
NetworkVis.prototype._toggleEdgeLabels = function()
{
    // update visibility of labels

    this._edgeLabelsVisible = !this._edgeLabelsVisible;
    this._vis.edges().forEach(function( ele ){
        if (_edgeLabelsVisible === false){
          ele.css('content', '');
        }
        else{
          ele.css('content', 'data(label)');
        }
    });

    // update check icon of the corresponding menu item

    var item = $(this.mainMenuSelector + " #show_edge_labels");

    if (this._edgeLabelsVisible)
    {
        item.addClass(this.CHECKED_CLASS);
    }
    else
    {
        item.removeClass(this.CHECKED_CLASS);
    }
};

/**
 * Toggles the visibility of the pan/zoom control panel.
 */
NetworkVis.prototype._togglePanZoom = function()
{
    // update visibility of the pan/zoom control
    //TODO

    this._panZoomVisible = !this._panZoomVisible;

//    this._vis.panZoomControlVisible(this._panZoomVisible);

    // update check icon of the corresponding menu item

    var item = $(this.mainMenuSelector + " #show_pan_zoom_control");

    if (this._panZoomVisible)
    {
        item.addClass(this.CHECKED_CLASS);
        $('.cy-panzoom').css('visibility', 'visible');
    }
    else
    {
        item.removeClass(this.CHECKED_CLASS);
        $('.cy-panzoom').css('visibility', 'hidden');
    }

};

/**
 * Merges the edges, if not merged. If edges are already merges, then show all
 * edges.
 */

NetworkVis.prototype._toggleMerge = function()
{
    // merge/unmerge the edges
    //TODO
    this._linksMerged = !this._linksMerged;
  //  this._vis.edgesMerged(this._linksMerged);

    // update check icon of the corresponding menu item

    //Unselect selected edges beforehand !
    this._vis.edges(':selected').unselect();


    var edgesToBeRemoved = this._vis.edges("[mergeStatus='toBeMerged']");

    var item = $(this.mainMenuSelector + " #merge_links");

    if (this._linksMerged)
    {
        item.addClass(this.CHECKED_CLASS);

        //Add meta edges again
        this.metaEdges.restore();
        //Remove edges to be merged
        this._vis.remove(this.edgesToBeMerged);
    }
    else
    {
        item.removeClass(this.CHECKED_CLASS);

        //Add original edges
        this.edgesToBeMerged.restore();
        //Remove meta edges
        this._vis.remove(this.metaEdges);
    }
    this._vis.layout({
      name:'preset',
      fit: false
    })
    // this._visChanged();
};

/**
 * Toggle auto layout option on or off. If auto layout is active, then the
 * graph is laid out automatically upon any change.
 */
NetworkVis.prototype._toggleAutoLayout = function()
{
    // toggle autoLayout option

    this._autoLayout = !this._autoLayout;

    // update check icon of the corresponding menu item

    var item = $(this.mainMenuSelector + " #auto_layout");

    if (this._autoLayout)
    {
        item.addClass(this.CHECKED_CLASS);
    }
    else
    {
        item.removeClass(this.CHECKED_CLASS);
    }
};

/**
 * Toggle "remove disconnected on hide" option on or off. If this option is
 * active, then any disconnected node will also be hidden after the hide action.
 */
NetworkVis.prototype._toggleRemoveDisconnected = function()
{
    // toggle removeDisconnected option

    this._removeDisconnected = !this._removeDisconnected;

    // update check icon of the corresponding menu item

    var item = $(this.mainMenuSelector + " #remove_disconnected");

    if (this._removeDisconnected)
    {
        item.addClass(this.CHECKED_CLASS);
    }
    else
    {
        item.removeClass(this.CHECKED_CLASS);
    }
};

/**
 * Toggles the visibility of the profile data for the nodes.
 */
NetworkVis.prototype._toggleProfileData = function()
{
    // toggle value and pass to CW

    this._profileDataVisible = !this._profileDataVisible;
    var nodeVisibility = this._profileDataVisible;
/*    this._vis.nodes().forEach(function( ele ){
        ele.css('show-details', "" + nodeVisibility);
    });
*/
    showDetailsForAll = !showDetailsForAll;
    // update check icon of the corresponding menu item

    var item = $(this.mainMenuSelector + " #show_profile_data");

    if (this._profileDataVisible)
    {
        item.addClass(this.CHECKED_CLASS);
    }
    else
    {
        item.removeClass(this.CHECKED_CLASS);
    }
    this._vis.layout({
      'name': 'preset',
      'fit':  false
    });
};

/**
 * Saves the network as a PNG image.
 */
NetworkVis.prototype._saveAsPng = function()
{
  var pngContent = this._vis.png({scale: 3, full: true});
  //Remove additional data from beginning of png image data
  var b64data = pngContent.substr(pngContent.indexOf(",") + 1);
  var content = cbio.util.b64ToByteArrays(b64data);
  cbio.download.clientSideDownload(content, "network.png", "image/png");
};

/**
 * Saves the network as a SVG image.
 */
NetworkVis.prototype._saveAsSvg = function()
{
  //TODO SVG
/*	var downloadOpts = {
		filename: "network.svg",
		preProcess: null
	};

	cbio.download.initDownload(this._vis.svg(), downloadOpts);
  */
};

/**
 * Displays the layout properties panel.
 */
NetworkVis.prototype._openProperties = function()
{
    this._updatePropsUI();
    $(this.settingsDialogSelector).dialog("open").height("auto");
};

/**
 * Initializes the layout settings panel.
 */
NetworkVis.prototype._initPropsUI = function()
{
    $(this.settingsDialogSelector + " #fd_layout_settings tr").tipTip();
};

/**
 * Updates the contents of the layout properties panel.
 */
 //TODO
NetworkVis.prototype._updatePropsUI = function()
{
    // update settings panel UI

    for (var i=0; i < this._layoutOptions.length; i++)
    {
//      if (_layoutOptions[i].id == "weightNorm")
//      {
//          // clean all selections
//          $("#norm_linear").removeAttr("selected");
//          $("#norm_invlinear").removeAttr("selected");
//          $("#norm_log").removeAttr("selected");
//
//          // set the correct option as selected
//
//          $("#norm_" + _layoutOptions[i].value).attr("selected", "selected");
//      }

        if (this._layoutOptions[i].id == "animate")
        {
            if (this._layoutOptions[i].value == true)
            {
                // check the box
                $(this.settingsDialogSelector + " #animate").attr("checked", true);
                $(this.settingsDialogSelector + " #animate").val(true);
            }
            else
            {
                // uncheck the box
                $(this.settingsDialogSelector + " #animate").attr("checked", false);
                $(this.settingsDialogSelector + " #animate").val(false);
            }
        }
        else if(this._layoutOptions[i].id == "randomize"){
          if (this._layoutOptions[i].value == true)
          {
              // check the box
              $(this.settingsDialogSelector + " #randomize").attr("checked", true);
              $(this.settingsDialogSelector + " #randomize").val(true);
          }
          else
          {
              // uncheck the box
              $(this.settingsDialogSelector + " #randomize").attr("checked", false);
              $(this.settingsDialogSelector + " #randomize").val(false);
          }
        }
        else if(this._layoutOptions[i].id == "tile"){
          if (this._layoutOptions[i].value == true)
          {
              // check the box
              $(this.settingsDialogSelector + " #tile").attr("checked", true);
              $(this.settingsDialogSelector + " #tile").val(true);
          }
          else
          {
              // uncheck the box
              $(this.settingsDialogSelector + " #tile").attr("checked", false);
              $(this.settingsDialogSelector + " #tile").val(false);
          }
        }
        else if(this._layoutOptions[i].id == "fit"){
          if (this._layoutOptions[i].value == true)
          {
              // check the box
              $(this.settingsDialogSelector + " #fit").attr("checked", true);
              $(this.settingsDialogSelector + " #fit").val(true);
          }
          else
          {
              // uncheck the box
              $(this.settingsDialogSelector + " #fit").attr("checked", false);
              $(this.settingsDialogSelector + " #fit").val(false);
          }
        }
        else
        {
            $(this.settingsDialogSelector + " #" + this._layoutOptions[i].id).val(
                this._layoutOptions[i].value);
        }
    }
};

/**
 * Updates the graphLayout options for CytoscapeWeb.
 */
NetworkVis.prototype._updateLayoutOptions = function()
{
    // update graphLayout object

    var options = new Object();

    for (var i=0; i < this._layoutOptions.length; i++)
    {
        options[this._layoutOptions[i].id] = this._layoutOptions[i].value;
    }
    options['name']="cose-bilkent";
    this._graphLayout = options;
};

NetworkVis.prototype._createEdgeInspector = function(divId)
{
    var id = "edge_inspector_" + divId;

    var html =
        '<div id="' + id + '" class="network_edge_inspector hidden-network-ui" title="Edge Inspector">' +
            '<div class="edge_inspector_content content ui-widget-content">' +
                '<table class="data"></table>' +
                '<table class="xref"></table>' +
            '</div>' +
        '</div>';

    $("#" + divId).append(html);

    return "#" + id;
};

NetworkVis.prototype._createGeneLegend = function(divId)
{
    var id = "node_legend_" + divId;

    var html =
        '<div id="' + id + '" class="network_node_legend hidden-network-ui" title="Gene Legend">' +
            '<div id="node_legend_content" class="content ui-widget-content">' +
                '<img src="images/network/gene_legend.png" alt="gene legend"/>' +
            '</div>' +
        '</div>';

    $("#" + divId).append(html);

    return "#" + id;
};

NetworkVis.prototype._createDrugLegend = function(divId)
{
    var id = "drug_legend_" + divId;

    var html =
        '<div id="' + id + '" class="network_drug_legend hidden-network-ui" title="Drug Legend">' +
            '<div id="drug_legend_content" class="content ui-widget-content">' +
                '<img src="images/network/drug_legend.png" alt="drug legend"/>' +
            '</div>' +
        '</div>';

    $("#" + divId).append(html);

    return "#" + id;
};

NetworkVis.prototype._createEdgeLegend = function(divId)
{
    var id = "edge_legend_" + divId;

    var html =
        '<div id="' + id + '" class="network_edge_legend hidden-network-ui" title="Interaction Legend">' +
            '<div id="edge_legend_content" class="content ui-widget-content">' +
                '<img src="images/network/interaction_legend.png" alt="interaction legend"/>' +
            '</div>' +
        '</div>';

    $("#" + divId).append(html);

    return "#" + id;
};

NetworkVis.prototype._createSettingsDialog = function(divId)
{
    var id = "settings_dialog_" + divId;

    var html =
        '<div id="' + id + '" style="width: auto;" class="settings_dialog hidden-network-ui" title="Layout Properties">' +
            '<div id="fd_layout_settings" class="content ui-widget-content">' +
                '<table>' +
                    '<tr title="Gravitational constant to keep graph components together">' +
                        '<td align="right">' +
                            '<label>Gravity</label>' +
                        '</td>' +
                        '<td>' +
                            '<input type="text" id="gravity" class="layout-properties" value="" title="Gravitational constant"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Padding on fit">' +
                        '<td align="right">' +
                            '<label>Padding</label>' +
                        '</td>' +
                        '<td>' +
                            '<input type="text" id="padding" class="layout-properties" value="" title="Padding on fit"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Uncheck for incremental layout">' +
                        '<td align="right">' +
                            '<label>Randomize</label>'+
                        '</td>' +
                        '<td align="left">' +
                            '<input type="checkbox" id="randomize" value="true" checked="checked" title="Incremental layout"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Node Repulsion (non-overlapping) multiplier">' +
                        '<td align="right">' +
                            '<label>Node Repulsion</label>' +
                        '</td>' +
                        '<td>' +
                            '<input type="text" id="nodeRepulsion" class="layout-properties" value=""  title="Node repulsion multiplier"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Maximum number of iterations">' +
                        '<td align="right">' +
                            '<label>Iteration Number</label>' +
                        '</td>' +
                        '<td>' +
                            '<input type="text" id="numIter" class="layout-properties" value=""  title="Maximum number of iterations"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Ideal length of an edge">' +
                        '<td align="right">' +
                            '<label>Ideal Edge Length</label>' +
                        '</td>' +
                        '<td>' +
                            '<input type="text" id="idealEdgeLength" class="layout-properties" value="" title="Ideal edge length"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Divisor to computer edge forces">' +
                        '<td align="right">' +
                            '<label>Edge Elasticity</label>' +
                        '</td>' +
                        '<td>' +
                            '<input type="text" id="edgeElasticity" class="layout-properties" value="" title="Edge elasticity"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Nesting factor (multiplier) to compute ideal edge length for nested edges">' +
                        '<td align="right">' +
                            '<label>Nesting Factor</label>' +
                        '</td>' +
                        '<td>' +
                            '<input type="text" id="nestingFactor" class="layout-properties" value="" title="Nesting factor"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Whether or not to display network during layout">' +
                        '<td align="right">' +
                            '<label>Animate</label>' +
                        '</td>' +
                        '<td align="left">' +
                            '<input type="checkbox" id="animate" value="true" checked="checked"  title="Display network during layout"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Whether or not to fit the network into canvas after layout">' +
                        '<td align="right">' +
                            '<label>Fit</label>' +
                        '</td>' +
                        '<td align="left">' +
                            '<input type="checkbox" id="fit" value="true" checked="checked"  title="Fit to canvas after layout"/>' +
                        '</td>' +
                    '</tr>' +
                    '<tr title="Whether or not to tile disconnected nodes on layout">' +
                        '<td align="right">' +
                            '<label>Tile Disconnected</label>' +
                        '</td>' +
                        '<td align="left">' +
                            '<input type="checkbox" id="tile" value="true" checked="checked" title="Tile disconnected nodes on layout"/>' +
                        '</td>' +
                    '</tr>' +
                '</table>' +
            '</div>' +
            '<div class="footer">' +
                '<input type="button" id="save_layout_settings" value="Save" title="Save layout settings"/>' +
                '<input type="button" id="default_layout_settings" value="Default" title="Default layout settings"/>' +
            '</div>' +
        '</div>';

    $("#" + divId).append(html);

    return "#" + id;
};

NetworkVis.prototype._createInteractionSourceVisibilityWindow = function(divId)
{
  var id = "interactionSource_visibility" + divId;

  var table = "";

  for (var key in this._edgeSourceVisibility)
  {
      var checkedOrNot = ((this.visibilityOfSource[key] === true) ? 'checked' : '');
      table += '<tr class="' + _safeProperty(key) + '">' +
                '<td class="edge-type-checkbox">'+
                  '<input id="'+key+'_check" type="checkbox" checked="checked">'+
                  '<label style="font-size: 10pt; font-weight: normal;">'+key +'</label>'+
                '</td>';
  }

  var html =
      '<div id="' + id + '" title="Interactions to Show by Source">' +
          '<div id="interactionSource_visibility" class="content ui-widget-content">' +
            '<table id="edge_source_filter">' +
            table +
            '</table>'+
          '</div>' +
          '<div class="footer" style=" text-align: center; margin-top: 2px;">' +
              '<input type="button" style="font-size: 10pt;" id="close_InteractionSourceVisibilityButton" value="OK"/>' +
              '<input type="button" style="font-size: 10pt; margin-left: 5px;" id="selectAll_InteractionSourceVisibilityButton" value="Select All"/>' +
              '<input type="button" style="font-size: 10pt; margin-left: 5px;" id="unselectAll_InteractionSourceVisibilityButton" style="margin-left: 2px"  value="Unselect All"/>' +
          '</div>'+
          '<div style="text-align: center; margin-top: 5px">'+
        '</div>'+
      '</div>';

  $("#" + divId).append(html);
  return "#" + id;
}



NetworkVis.prototype._createInteractionTypeVisibilityWindow = function(divId)
{
    var id = "interactionType_visibility" + divId;


    var table = "";
    for (var key in this.edgeTypeConstants)
    {
      if (!this.percentageFilterMap[key])
      {
        var checkedOrNot = ((this.visibilityOfType[key] === true) ? 'checked' : '');
          table +=
            '<tr class="'+this.edgeTypeConstants[key].name +'_UI">\n'+
            '<td class="edge-type-checkbox">'+
              '<input id="'+this.edgeTypeConstants[key].name+'_check" type="checkbox" '+checkedOrNot+'>\n'+
              '<label style="font-size: 10pt; font-weight: normal;" >'+this.edgeTypeConstants[key].desc +'</label>\n'+
            '</td>'+
            '</tr>\n';
      }
    }

    var html =
        '<div id="' + id + '" title="Interactions to Show by Type">' +
            '<div id="interactionType_visibility" class="content ui-widget-content">' +
              '<table id="edge_type_filter">' +
              table +
              '</table>'+
            '</div>' +
            '<div class="footer" style=" text-align: center; margin-top: 2px;">' +
                '<input type="button" style="font-size: 10pt;" id="close_InteractionTypeVisibilityButton" value="OK"/>' +
                '<input type="button" style="font-size: 10pt; margin-left: 5px;" id="selectAll_InteractionTypeVisibilityButton" value="Select All"/>' +
                '<input type="button" style="font-size: 10pt; margin-left: 5px;" id="unselectAll_InteractionTypeVisibilityButton" style="margin-left: 2px"  value="Unselect All"/>' +
            '</div>'+
            '<div style="text-align: center; margin-top: 5px">'+
        	'</div>'+
        '</div>';

    $("#" + divId).append(html);
    return "#" + id;
};

/**
 * Pops up a SBGN View window using SBGNViz.js, resultant graph represents
 * paths between graph of given source node and target node
 *
 * @param sourceNodeID source node id
 * @param targetNodeID target node id
 */
NetworkVis.prototype.popUpSBGNView = function(sourceNodeID, targetNodeID, edgeType)
{

  if (edgeType == 'DRUG_TARGET') {
    return;
  }

  var popUpWidth = 1200;
  var popupHeight = 760;

  var strWindowFeatures =
    "menubar=no,\
    location=no,\
    resizable=no,\
    scrollbars=no,\
    status=no,\
    width="+popUpWidth+"px,\
    height="+popupHeight+"px,\
    left="+((window.innerWidth/2)-popUpWidth/2)+"px,\
    top="+((window.innerHeight/2)-popupHeight/2)+"px";
    var sbgnPageURL = "js/lib/SBGNViz.js/sample-app/index.html";
    windowObjectReference = window.open(sbgnPageURL+"?"+sourceNodeID+"&"+targetNodeID,"SBGN View", strWindowFeatures);
}

/*
 * ##################################################################
 * ##################### Utility Functions ##########################
 * ##################################################################
 */

/**
 * Parses the given xmlDoc representing the BioGene query result, and
 * returns a corresponding JSON object.
 * (Not used anymore, using JSON service of the BioGene instead)
 *
 * @param xmlDoc
 * @private
 */
function _parseBioGeneXml(xmlDoc)
{
    var json = new Object();

    // check the return code
    var returnCode = xmlDoc.getElementsByTagName("return_code")[0].childNodes[0].nodeValue;

    json.returnCode = returnCode;

    if(returnCode != "SUCCESS")
    {
        return json;
    }

    // work on the first result only
    var geneInfo = xmlDoc.getElementsByTagName("gene_info")[0];

    var geneIdNode = geneInfo.getElementsByTagName("gene_id");
    var geneSymbolNode = geneInfo.getElementsByTagName("gene_symbol");
    var geneLocationNode = geneInfo.getElementsByTagName("gene_location");
    var geneChromosomeNode = geneInfo.getElementsByTagName("gene_chromosome");
    var geneDescriptionNode = geneInfo.getElementsByTagName("gene_description");
    var geneAliasesNode = geneInfo.getElementsByTagName("gene_aliases");
    var geneSummaryNode = geneInfo.getElementsByTagName("gene_summary");
    var geneDesignationsNode = geneInfo.getElementsByTagName("gene_designations");
    var geneMIMNode = geneInfo.getElementsByTagName("gene_mim");

    if (geneIdNode.length > 0)
        json.geneId = geneIdNode[0].childNodes[0].nodeValue;

    if (geneSymbolNode.length > 0)
        json.geneSymbol = geneSymbolNode[0].childNodes[0].nodeValue;

    if (geneLocationNode.length > 0)
        json.geneLocation = geneLocationNode[0].childNodes[0].nodeValue;

    if (geneChromosomeNode.length > 0)
        json.geneChromosome = geneChromosomeNode[0].childNodes[0].nodeValue;

    if (geneDescriptionNode.length > 0)
        json.geneDescription = geneDescriptionNode[0].childNodes[0].nodeValue;

    if (geneAliasesNode.length > 0)
        json.geneAliases = _parseDelimitedInfo(geneAliasesNode[0].childNodes[0].nodeValue, ":", ",");

    if (geneSummaryNode.length > 0)
        json.geneSummary = geneSummaryNode[0].childNodes[0].nodeValue;

    if (geneDesignationsNode.length > 0)
        json.geneDesignations = _parseDelimitedInfo(geneDesignationsNode[0].childNodes[0].nodeValue, ":", ",");

    if (geneMIMNode.length > 0)
        json.geneMIM = geneMIMNode[0].childNodes[0].nodeValue;

    return json;
}

/**
 * Initializes the style of the network menu by adjusting hover behaviour.
 *
 * @param divId
 * @param hoverClass
 * @private
 */
function _initMenuStyle(divId, hoverClass)
{
    // Opera fix
    $("#" + divId + " #network_menu ul").css({display: "none"});

    // adds hover effect to main menu items (File, Topology, View)

    $("#" + divId + " #network_menu li").hover(
        function() {
            $(this).find('ul:first').css(
                {visibility: "visible",display: "none"}).show(400);
        },
        function() {
            $(this).find('ul:first').css({visibility: "hidden"});
        });


    // adds hover effect to menu items

    $("#" + divId + " #network_menu ul a").hover(
        function() {
            $(this).addClass(hoverClass);
        },
        function() {
            $(this).removeClass(hoverClass);
        });
}
/**
 * Comparison function to sort genes alphabetically.
 *
 * @param node1 node to compare to node2
 * @param node2 node to compare to node1
 * @return      positive integer if node1 is alphabetically greater than node2
 *              negative integer if node2 is alphabetically greater than node1
 *              zero if node1 and node2 are alphabetically equal
 */
function _geneSort (node1, node2)
{
    if (node1._private.data.label > node2._private.data.label)
    {
        return 1;
    }
    else if (node1._private.data.label < node2._private.data.label)
    {
        return -1;
    }
    else
    {
        return 0;
    }
}

/**
 * Generates a shortened version of the given node id.
 *
 * @param id    id of a node
 * @return      a shortened version of the id
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
 * @param str   string to be modified
 * @return      safe version of the given string
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
 * @param source        string to be modified
 * @param toFind        string to match
 * @param toReplace     string to be replaced with the matched string
 * @return              modified version of the source string
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
 * @return  true if IE, false otherwise
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
 * @param source    source string to be converted to title case
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
 * @param map   map that contains real numbers
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
 * @param value     input value to be transformed
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
 * @param value value to be reverse transformed
 * @returns     reverse transformed value
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
 * @param a coefficient of the term x^3
 * @param b coefficient of the term x^2
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
    //  Math.pow(q - sqrt, 1/3) +
    //  p;

    var x = _cubeRoot(q + sqrt) +
            _cubeRoot(q - sqrt) +
            p;

    return x;
}

/**
 * Evaluates the cube root of the given value. This function also handles
 * negative values unlike the built-in Math.pow() function.
 *
 * @param value source value
 * @returns     cube root of the source value
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
