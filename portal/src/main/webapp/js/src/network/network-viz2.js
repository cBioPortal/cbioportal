function send2cytoscapeweb(elements, cytoscapeDivId, networkDivId)
{
	$('#'+cytoscapeDivId).empty();
	$('#'+cytoscapeDivId).cytoscape({
		style: cytoscape.stylesheet()
		.selector('node')
		.css({
			'border-width': function(ele){
				return (ele._private.data['IN_QUERY'] === "true") ? 5 : 1;
			},
			'mouse-position-x': 0,
			'mouse-position-y': 0,
			'show-details': false,
			'show-details-selected': false,
			'show-total-alteration': false,
			'opacity': 0.9,
			'shape': function(ele)
			{
				switch(ele._private.data['type'])
				{
					case "Protein": return "ellipse"; break;
					case "SmallMolecule": return "triangle"; break;
					case "Unknown": return "diamond"; break;
					case "Drug": return "hexagon"; break;
				}
			},
			'content': 'data(label)',
			'text-valign': 'top',
			'text-halign': 'bottom',
			'font-size': 10,
			'width': function(ele)
			{
				return ele._private.data['type'] === 'Drug' ? 15 : 25;
			},
			'height': function(ele)
			{
				return ele._private.data['type'] === 'Drug' ? 15 : 25;
			},
			'background-color': function(ele)
            {
				var value = ele._private.data['PERCENT_ALTERED']*100;
				var high = 100;
				var low = 0;

				var highCRed = 255;
				var highCGreen = 0;
				var highCBlue = 0;

				var lowCRed = 255;
				var lowCGreen = 255;
				var lowCBlue = 255;

				// transform percentage value by using the formula:
				// y = 0.000166377 x^3  +  -0.0380704 x^2  +  3.14277x
				// instead of linear scaling, we use polynomial scaling to better
				// emphasize lower alteration frequencies
				value = (0.000166377 * value * value * value) -
					(0.0380704 * value * value) +
					(3.14277 * value);

				// check boundary values
				if (value > 100)
				{
					value = 100;
				}
				else if (value < 0)
				{
					value = 0;
				}

				if (value >= high)
				{
					return "rgb"+"("+highCRed +"," + highCGreen + "," + highCBlue + ")";
				}
				else if (value > low)
				{
					return "rgb"+"("+getValueByRatio(value, low, high, lowCRed, highCRed) + "," +
						getValueByRatio(value, low, high, lowCGreen, highCGreen) + "," +
						getValueByRatio(value, low, high, lowCBlue, highCBlue) + ")";
				}
				else
				{
					return "rgb"+"("+lowCRed +"," + lowCGreen + "," + lowCBlue +")";
				}
			},
            'content': function(ele){
                var name = ele._private.data["label"];

                if (ele._private.data["type"] == "Drug")
                {
                    name = ele._private.data["NAME"];

                    var truncateIndicator = '...';
                    var nameSize = name.length;

                    if (nameSize > 10)
                    {
                        name = name.substring(0, 10);
                        name = name.concat(truncateIndicator);
                    }

                }

                return name;
            },
			'text-halign': "center",
			'taxt-valign': "bottom",
			'total-alteration-font': "Verdana",
			'total-alteration-color': function(ele){
				return (ele._private.data['type'] === 'Drug') ?  "#E6A90F" : "#EE0505";
			},
			'total-alteration-font-size': 12
		})
		.selector('edge')
		.css({
			'line-color': function(ele){
				switch (ele._private.data['type']){
					case "IN_SAME_COMPONENT": return "#904930"; break;
					case "REACTS_WITH": return "#7B7EF7"; break;
					case "DRUG_TARGET": return "#E6A90F"; break;
					case "STATE_CHANGE": return "#67C1A9"; break;
					default: return "#A583AB"; break;
				}
			},
			"target-arrow-shape": function(ele){
				switch (ele._private.data['type']){
					case "STATE_CHANGE": return "triangle"; break;
					case "DRUG_TARGET": return "tee"; break;
					default: return "none"; break;
				}
			},
			'target-arrow-color': function(ele){
				switch (ele._private.data['type']){
					case "IN_SAME_COMPONENT": return "#904930"; break;
					case "REACTS_WITH": return "#7B7EF7"; break;
					case "DRUG_TARGET": return "#E6A90F"; break;
					case "STATE_CHANGE": return "#67C1A9"; break;
					default: return "#A583AB"; break;
				}
			},
			'opacity': 0.8,
			'curve-style': 'bezier',
		//	"haystack-radius":"0",
			'width': 2
		})
		.selector('node:selected')
		.css({

			'shadow-color': 'yellow',
			'shadow-opacity': 1
		}),
/*		.selector('.faded')
		.css({
			'opacity': 0.25,
			'text-opacity': 0
		}),
*/
		elements: elements,

		layout: {
			name: 'cose',
			animate: false
		},

		// on graph initial layout done (could be async depending on layout...)
		ready: function()
		{
			window.cy = this;


			// giddy up...
			cy.fit();
			cy.on('mouseover', 'node', function(evt){
				if (!this.isParent()) {
					this._private.style['show-details'] = true;
					this._private.style['show-total-alteration'] = true;
					cy.layout({
						name: 'preset',
						fit: false
					});
				}
			});
			cy.on('mousemove', 'node', function(evt){
				if (!this.isParent()) {
					this._private.style['mouse-position-x'] = evt.cyPosition.x;
					this._private.style['mouse-position-y'] = evt.cyPosition.y;
					cy.layout({
						name: 'preset',
						fit: false
					});
				}
			});
			cy.on('mouseout', 'node', function(evt){
				this._private.style['show-total-alteration'] = false;
				if (this.selected() === false) {
					this.css("opacity", 0.8);
				}
				if (!this.selected()) {
					this.css('show-details', 'false');
					cy.layout({
						name: 'preset',
						fit: false
					});
				}
			});
			var tapped = false;
			var edge = false;
			cy.on('tap', 'node', function(evt){
				tapped = true;
			});
			cy.on('tap', 'edge', function(evt){
				edge = true;
			});
			cy.on('tap', '', function (event) {
				if (tapped || edge ) {
					edge = false;
					return;
				}
				else {
					var nodes = cy.nodes();
					var exist = false;
					for (var i = 0 ; i < nodes.length; i++){
						if (nodes[i]._private.style['show-details-selected'] === true)
						{
							exist = true;
							break;
						}
					}
					for (var i = 0 ; exist && i < nodes.length; i++){
						nodes[i].css('show-details', 'false');
						nodes[i].css('show-details-selected', 'false');
					}
					cy.layout({
						name: 'preset',
						fit: false
					});
					entered = false;
				}
			});
			cy.on('tap', 'node', function(evt){
				tapped = false;
				if (!this.isParent()) {
					var nodes = cy.filter(function(i, element){
						return element.isNode() && element._private.style['show-details-selected']=== true;
					});
					for (var i = 0; i < nodes.length; i++){
						nodes[i]._private.style['show-details'] = false;
						nodes[i]._private.style['show-details-selected'] = false;
					}

					this._private.style['show-details'] = true;
					this._private.style['show-details-selected'] = true;
					this._private.style['show-total-alteration'] = false;

					cy.layout({
						name: 'preset',
						fit: false
					});
				}
			});

			var netVis = new NetworkVis(networkDivId);

			// init UI of the network tab
			netVis.initNetworkUI(cy);

			//to hide drugs initially
			netVis._changeListener();

		}});
}

var rgb2hex = function(r, g, b)
{
	return(r<<16 | g<<8 | b);
}

var getValueByRatio = function(num, numLow, numHigh, colorNum1, colorNum2)
{
	return ((((num - numLow) / (numHigh - numLow)) * (colorNum2 - colorNum1)) + colorNum1)
}
