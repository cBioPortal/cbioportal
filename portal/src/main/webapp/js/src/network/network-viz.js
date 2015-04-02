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

// send graphml to cytoscape web for visualization
function send2cytoscapeweb(graphml, cwDivId, networkDivId)
{
    var visual_style = 
    {
        global: 
        {
            backgroundColor: "#fefefe", //#F7F6C9 //#F3F7FE
            tooltipDelay: 250
        },
        nodes: 
        {
            shape: 
            {
               discreteMapper: 
               {
            	   attrName: "type",
            	   entries: [
            	             { attrValue: "Protein", value: "ELLIPSE" },
            	             { attrValue: "SmallMolecule", value: "DIAMOND" },
            	             { attrValue: "Unknown", value: "TRIANGLE" },
            	             { attrValue: "Drug", value: "HEXAGON"}	]
                }
            },
            borderWidth: 1,
            borderColor: 
            {            	
            	discreteMapper: 
            	{
            		attrName: "type",
            		entries: [
            		          { attrValue: "Protein", value: "#000000" },
            		          { attrValue: "SmallMolecule", value: "#000000" },
            		          { attrValue: "Drug", value: "#000000"},
            		          { attrValue: "Unknown", value: "#000000" } ]
                    }	
            },
            size: 
            {
            	defaultValue: 24,
            	discreteMapper: 
            	{
            		attrName: "type",
            		entries: [ { attrValue: "Drug", value: 16}	]
            	}
            },
            color: {customMapper: {functionName: "colorFunc"}},
            label: {customMapper: {functionName: "labelFunc"}},
            labelHorizontalAnchor: "center",
            labelVerticalAnchor: "bottom",
            labelFontSize: 10,
            selectionGlowColor: "#f6f779",
            selectionGlowOpacity: 0.8,
            hoverGlowColor: "#cbcbcb", //#ffff33
            hoverGlowOpacity: 1.0,
            hoverGlowStrength: 8,
            tooltipFont: "Verdana",
            tooltipFontSize: 12,
            tooltipFontColor:
            {
            	defaultValue: "#EE0505", // color of all other types
            	discreteMapper: 
            	{
	         	   attrName: "type",
	        	   entries: 
	        		   [
	        	             { attrValue: "Drug", value: "#E6A90F"}	
	        	       ]
            	}
            },           
            tooltipBackgroundColor:
            {
            	defaultValue: "#000000", // color of all other types
            	discreteMapper: 
            	{
	         	   attrName: "type",
	        	   entries: 
	        		   [
	        	             { attrValue: "Drug", value: "#000000"}	
	        	             //"#979694"
	        	       ]
            	}
            },         
            tooltipBorderColor: "#000000"
        },
        edges: 
        {
        	width: 1,
	        mergeWidth: 2,
	        mergeColor: "#666666",
		    targetArrowShape: 
		    {
		    	defaultValue: "NONE",
		        discreteMapper: 
		        {
		        	attrName: "type",
			        entries: [
			                  { attrValue: "STATE_CHANGE", value: "DELTA" },
			                  { attrValue: "DRUG_TARGET", value: "T" } ]
		        }
	        },
            color: 
            {
            	defaultValue: "#A583AB", // color of all other types
            	discreteMapper: 
            	{
            		attrName: "type",
            		entries: [
            		          { attrValue: "IN_SAME_COMPONENT", value: "#904930" },
            		          { attrValue: "REACTS_WITH", value: "#7B7EF7" },
            		          { attrValue: "DRUG_TARGET", value: "#E6A90F" },
            		          { attrValue: "STATE_CHANGE", value: "#67C1A9" } ]
            	}	
            }
        }
    };    
    
    // initialization options
    var options = {
        swfPath: "swf/CytoscapeWeb",
        flashInstallerPath: "swf/playerProductInstall"
    };

    var vis = new org.cytoscapeweb.Visualization(cwDivId, options);

    
    
    /*
     * This function truncates the drug names on the graph
     * if their name length is less than 10 characters.
     */
    vis["labelFunc"] = function(data)
    {
    	var name = data["label"];
    	
    	if (data["type"] == "Drug") 
    	{
    		name = data["NAME"];
    		
    		var truncateIndicator = '...';
    		var nameSize = name.length;
    		
    		if (nameSize > 10) 
    		{
    			name = name.substring(0, 10);
    			name = name.concat(truncateIndicator);
    		}
    		
		}
    	
    	return name;
    };
    
    /* 
     * FDA approved drugs are shown with orange like color
     * non FDA approved ones are shown with white color
     */
    vis["colorFunc"] = function(data)
    {
    	if (data["type"] == "Drug") 
    	{
			if (data["FDA_APPROVAL"] == "true") 
			{
				return "#E6A90F";
			}
			else
			{
				return	"#FFFFFF";
			}
				
		}
    	else 
    		return "#FFFFFF";
    };

    
    vis.ready(function() {
        var netVis = new NetworkVis(networkDivId);

        // init UI of the network tab
        netVis.initNetworkUI(vis);
   
        //to hide drugs initially
        netVis._changeListener();
	     
	    // set the style programmatically
	    document.getElementById("color").onclick = function(){
	        vis.visualStyle(visual_style);
	    };
	    
	});

    var draw_options = {
        // your data goes here
        network: graphml,
        edgeLabelsVisible: false,
        edgesMerged: true,
        layout: "ForceDirected",
        visualStyle: visual_style,
        panZoomControlVisible: true
    };

    vis.draw(draw_options);
}

function send2cytoscapewebSbgn(sbgnml, cwDivId, networkDivId, genomicData)
{
    var visualStyle =
    {
        nodes:
        {
            label: {customMapper: {functionName: "labelFunction"}},
            compoundColor: "#FFFFFF",
            compoundOpacity: 1.0,
            opacity: 1.0,
            compoundShape: {customMapper: {functionName: "compoundShapeFunction"}},
            color: "#FFFFFF",
            //shape: {customMapper: {functionName: "shapeFunction"}},
            labelVerticalAnchor: "middle",
            labelHorizontalAnchor: "center",
            compoundLabelVerticalAnchor: "middle",
            compoundLabelHorizontalAnchor: "center",
            compoundLabelYOffset: 7.0
            	
        },

        edges:
        {
            targetArrowShape:
            {
                defaultValue: "NONE",
                discreteMapper:
                {
                    attrName: "arc_class",
                    entries:
                        [
                            {attrValue:"consumption", value: "NONE"},
                            {attrValue:"modulation", value: "DIAMOND"},
                            {attrValue:"catalysis", value: "CIRCLE"},
                            {attrValue:"inhibition", value: "T"},
                            {attrValue:"production", value: "ARROW"},
                            {attrValue:"stimulation", value: "ARROW"},
                            {attrValue:"necessary stimulation", value: "T-ARROW"}
                        ]
                }
            },
            targetArrowColor:
            {
                defaultValue: "#ffffff",
                discreteMapper:
                {
                    attrName: "arc_class",
                    entries:
                        [
                            {attrValue:"production", value: "#000000"}
                        ]
                }
            }
        }
    };

    // initialization options
    var options = {
        swfPath: "swf/CytoWebSbgn",
        flashInstallerPath: "swf/playerProductInstall"
    };

    var vis = new org.cytoscapeweb.Visualization(cwDivId, options);

    vis["compoundShapeFunction"] = function (data)
    {
        var retValue = "COMPLEX";

        if(data["glyph_class"] == "compartment")
        {
            retValue = "ROUNDRECT";
        }

        return retValue;
    };

    vis["labelFunction"] = function (data)
    {
        var retValue = data["glyph_label_text"];

        if(data["glyph_class"] == "omitted process")
        {
            retValue = "\\\\";
        }

        if(data["glyph_class"] == "uncertain process")
        {
            retValue = "?";
        }

        if(data["glyph_class"] == "and" || data["glyph_class"] == "or" || data["glyph_class"] == "not" )
        {
            retValue = data["glyph_class"].toUpperCase();
        }


        return retValue;
    };

    vis.ready(function() {
        var netVis = new NetworkSbgnVis(networkDivId);

        // init UI of the network tab
        netVis.initNetworkUI(vis);

        // parse and add genomic data to cytoscape nodes
        netVis.parseGenomicData(genomicData); 
        
        //to hide drugs initially
        netVis._changeListener();

        // set the style programmatically
        document.getElementById("color").onclick = function(){
            vis.visualStyle(visualStyle);
        };
    });

    var draw_options = {
        // your data goes here
        network: sbgnml,
        //edgeLabelsVisible: false,
        //edgesMerged: true,
        layout: "Preset",
        visualStyle: visualStyle,
        panZoomControlVisible: true
    };

    vis.draw(draw_options);
}

