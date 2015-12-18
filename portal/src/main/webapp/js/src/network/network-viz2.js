function send2cytoscapeweb(elements, cytoscapeDivId, networkDivId)
{
	$('#'+cytoscapeDivId).empty();
	$('#'+cytoscapeDivId).cytoscape({
		style: cytoscape.stylesheet()
		.selector('node')
		.css({
			'border-width': function(ele)
			{
				if (ele._private.data['IN_QUERY'] === undefined)
				{
					return 1;
				}
				else
				{
					return (ele._private.data['IN_QUERY'] === "true") ? 5 : 1;
				}
			},
			'mouse-position-x': 0,
			'mouse-position-y': 0,
			'show-details': 'false',
			'show-details-selected': 'false',
			'show-total-alteration': 'false',
			'main-opacity': 0.8,
			'opacity': 0.8,						// Must be the same with the main-opacity in load state!!!
			'shape': function(ele)
			{
				if (ele._private.data['type'] === undefined)
				{
					return "ellipse";
				}

				switch(ele._private.data['type'])
				{
					case "Protein": return "ellipse"; break;
					case "SmallMolecule": return "triangle"; break;
					case "Unknown": return "diamond"; break;
					case "Drug": return "hexagon"; break;
					default: return "ellipse"; break;
				}
			},
			'content': function(ele)
			{
				if (ele._private.data['label'] === undefined)
				{
					return "";

				}
				else
				{
					return ele._private.data['label'];
				}
			},
			'text-valign': 'top',
			'text-halign': 'bottom',
			'font-size': 10,
			'width': function(ele)
			{
				if (ele._private.data['type'] === undefined)
				{
					 return 15;
				}
				else
				{
					 return ele._private.data['type'] === 'Drug' ? 15 : 25;
				}

			},
			'height': function(ele)
			{
				if (ele._private.data['type'] === undefined)
				{
					return 15;

				}
				else
				{
					return ele._private.data['type'] === 'Drug' ? 15 : 25;
				}
			},
			'background-color': function(ele)
			{
				if (ele._private.data['PERCENT_ALTERED'] === undefined)
				{
					return "#ffffff";
				}

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
			'content': function(ele)
			{
				if (ele._private.data["label"] === undefined ||
						ele._private.data["type"] === undefined )
				{
					return "";
				}

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
			'total-alteration-color': function(ele)
			{
				if (ele._private.data['type'] === undefined)
				{
					return "#FF0000";
				}
				else
				{
					return (ele._private.data['type'] === 'Drug') ?  "#E6A90F" : "#FF0000";
				}
			},
			'total-alteration-font-size': 12
		})
		.selector('edge')
		.css({
			'line-color': function(ele)
			{
				if (ele._private.data['type'] === undefined)
				{
					return "#A583AB";
				}

				switch (ele._private.data['type']){
					case "IN_SAME_COMPONENT": return "#904930"; break;
					case "REACTS_WITH": return "#7B7EF7"; break;
					case "DRUG_TARGET": return "#E6A90F"; break;
					case "STATE_CHANGE": return "#67C1A9"; break;
					case "MERGED": return "#646464"; break;
					default: return "#A583AB"; break;
				}
			},
			"target-arrow-shape": function(ele)
			{
				if (ele._private.data['type'] === undefined)
				{
					return "#A583AB";
				}

				switch (ele._private.data['type']){
					case "STATE_CHANGE": return "triangle"; break;
					case "DRUG_TARGET": return "tee"; break;
					default: return "none"; break;
				}
			},
			'target-arrow-color': function(ele)
			{
				if (ele._private.data['type'] === undefined)
				{
					return "#A583AB";
				}

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
			'control-point-step-size': 10,
			//	"haystack-radius":"0",
			'width': 2
		})
		.selector(':selected')
		.css({

			'shadow-color': 'yellow',
			'shadow-opacity': 1
		}),

		/*.selector('.faded')
		.css({
		'opacity': 0.25,
		'text-opacity': 0
	}),*/

	elements: elements,

	layout: {
		name: 'cose-bilkent',
		randomize: true,
		animate: false,
		fit:true,
	},
	wheelSensitivity: 0.2,

	// on graph initial layout done (could be async depending on layout...)
	ready: function()
	{
		window.cy = this;

		var netVis = new NetworkVis(networkDivId);

		// init UI of the network tab
		netVis.initNetworkUI(cy);

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
				this.css("opacity", this._private.style['main-opacity'].value);
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
		var tappedBeforeNode = null;
		var tappedBeforeEdge = null;

		cy.on('tap', 'node', function(evt){
			tapped = true;

			//Double click workaround
			var tappedNow = evt.cyTarget;
			setTimeout(function(){ tappedBeforeNode = null; }, 300);
			if(tappedBeforeNode === tappedNow) {
				tappedNow.trigger('doubleTap');
				tappedBefore = null;
			} else {
				tappedBeforeNode = tappedNow;
			}
		});

		cy.on('tap', 'edge', function(evt){
			edge = true;
			var nodes = cy.nodes(":selected");
			for (var i = 0; i < nodes.length; i++){
				nodes[i]._private.style['show-details'] = false;
			}

			//Double click workaround
			var tappedNow = evt.cyTarget;
			setTimeout(function(){ tappedBeforeEdge = null; }, 300);
			if(tappedBeforeEdge === tappedNow) {
				tappedNow.trigger('doubleTap');
				tappedBefore = null;
			} else {
				tappedBeforeEdge = tappedNow;
			}
		});

		function updateDetailsTab(event)
		{
			if (tapped || edge ) {
				edge = false;
				return;
			}
			else {
				var nodes = cy.nodes();
				for (var i = 0 ; i < nodes.length; i++){
					nodes[i].css('show-details', 'false');
					nodes[i].css('show-details-selected', 'false');
					nodes[i].css('opacity', nodes[i]._private.style['main-opacity'].value);
					if (nodes[i].selected() && nodes[i]._private.selectable === false){
						nodes[i]._private.selectable = true;
						nodes[i].unselect();
						nodes[i]._private.selectable = false;
					}
					netVis.updateDetailsTab();
				}
				cy.layout({
					name: 'preset',
					fit: false
				});
				entered = false;
			}
		}

		cy.on('doubleTap', 'node', function (event) {updateDetailsTab(event)});
		cy.on('doubleTap', 'edge', function (event) {updateDetailsTab(event)});

		cy.on('unselect', 'node', function(event)
		{
			var tmpNode = event.cyTarget;

			tmpNode.css('show-details', 'false');
			tmpNode.css('show-details-selected', 'false');
			tmpNode.css('opacity', tmpNode._private.style['main-opacity'].value);

			if (tmpNode.selected() && tmpNode._private.selectable === false)
			{
				tmpNode._private.selectable = true;
				tmpNode.unselect();
				tmpNode._private.selectable = false;
			}
			netVis.updateDetailsTab();

		});

		cy.on('unselect', 'edge', function(event)
		{
			netVis.updateDetailsTab();
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
					if (nodes[i].selected() && nodes[i]._private.selectable === false){
						nodes[i]._private.selectable = true;
						nodes[i].unselect();
						nodes[i]._private.selectable = false;
					}
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
		cy.boxSelectionEnabled( true );
		cy.userZoomingEnabled(true);


		//to hide drugs initially
		netVis._changeListener();
		var panProps = ({
			zoomFactor: 0.05, // zoom factor per zoom tick
			zoomDelay: 45, // how many ms between zoom ticks
			minZoom: 0.1, // min zoom level
			maxZoom: 10, // max zoom level
			fitPadding: 50, // padding when fitting
			panSpeed: 10, // how many ms in between pan ticks
			panDistance: 10, // max pan distance per tick
			panDragAreaSize: 75, // the length of the pan drag box in which the vector for panning is calculated (bigger = finer control of pan speed and direction)
			panMinPercentSpeed: 0.25, // the slowest speed we can pan by (as a percent of panSpeed)
			panInactiveArea: 3, // radius of inactive area in pan drag box
			panIndicatorMinOpacity: 0.5, // min opacity of pan indicator (the draggable nib); scales from this to 1.0
			autodisableForMobile: true, // disable the panzoom completely for mobile (since we don't really need it with gestures like pinch to zoom)

			// icon class names
			sliderHandleIcon: 'fa fa-minus',
			zoomInIcon: 'fa fa-plus',
			zoomOutIcon: 'fa fa-minus',
			resetIcon: 'fa fa-expand'    });

			cy.panzoom(panProps);

			cy.zoom(1.3);
			//cy.fit([,100]);
			cy.center();

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
