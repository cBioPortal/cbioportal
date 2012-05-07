
// send graphml to cytoscape web for visualization
function send2cytoscapeweb(graphml, div_id) 
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
            tooltipFontColor: "#EE0505",
            tooltipBackgroundColor: "#000000",
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

    var vis = new org.cytoscapeweb.Visualization(div_id, options);

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

//    var forceDirectedLayout = 
//    {
// 	   name: "ForceDirected",
// 	   options:
// 	   {
//		   weightAttr: "weight"
// 	   }
//    };
    
    vis.ready(function() {
        // init UI of the network tab
        initNetworkUI(vis);

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
//        layout: forceDirectedLayout,
        visualStyle: visual_style,
        panZoomControlVisible: true
    };

    vis.draw(draw_options);
};