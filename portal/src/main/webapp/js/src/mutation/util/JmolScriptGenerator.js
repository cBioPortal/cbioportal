/**
 * JmolScriptGenerator class (extends MolScriptGenerator)
 *
 * Script generator for Jmol/JSmol applications.
 *
 * @author Selcuk Onur Sumer
 */
function JmolScriptGenerator()
{
	// Predefined style scripts for Jmol
	var _styleScripts = {
		ballAndStick: "wireframe ONLY; wireframe 0.15; spacefill 20%;",
		spaceFilling: "spacefill ONLY; spacefill 100%;",
		ribbon: "ribbon ONLY;",
		cartoon: "cartoon ONLY;",
		trace: "trace ONLY;"
	};

	function loadPdb(pdbId)
	{
		return "load=" + pdbId + ";";
	}

	function selectAll()
	{
		return "select all;";
	}

	function selectNone()
	{
		return "select none;";
	}

	function setScheme(schemeName)
	{
		return _styleScripts[schemeName];
	}

	function setColor (color)
	{
		return "color [" + formatColor(color) + "];"
	}

	function selectChain(chainId)
	{
		return "select :" + chainId + ";";
	}

	function selectAlphaHelix(chainId)
	{
		return "select :" + chainId + " and helix;";
	}

	function selectBetaSheet(chainId)
	{
		return "select :" + chainId + " and sheet;";
	}

	function rainbowColor(chainId)
	{
		// min atom no within the selected chain
		var rangeMin = "@{{:" + chainId + "}.atomNo.min}";
		// max atom no within the selected chain
		var rangeMax = "@{{:" + chainId + "}.atomNo.max}";

		// max residue no within the selected chain
		//var rangeMin = "@{{:" + chain.chainId + "}.resNo.min}";
		// max residue no within the selected chain
		//var rangeMax = "@{{:" + chain.chainId + "}.resNo.max}";

		// color the chain by rainbow coloring scheme (gradient coloring)
		return 'color atoms property atomNo "roygb" ' +
			'range ' + rangeMin + ' ' + rangeMax + ';';
	}

	function cpkColor(chainId)
	{
		return "color atoms CPK;";
	}

	function hideBoundMolecules()
	{
		return "restrict protein;";
	}

	function setTransparency(transparency)
	{
		// TODO we should use the given transparency value...
		return "color translucent;";
	}

	function makeOpaque()
	{
		return "color opaque;";
	}

	/**
	 * Generates a position string for Jmol scripting.
	 *
	 * @position object containing PDB position info
	 * @return {string} position string for Jmol
	 */
	function scriptPosition(position)
	{
		var insertionStr = function(insertion) {
			var posStr = "";

			if (insertion != null &&
			    insertion.length > 0)
			{
				posStr += "^" + insertion;
			}

			return posStr;
		};

		var posStr = position.start.pdbPos +
		             insertionStr(position.start.insertion);

		if (position.end.pdbPos > position.start.pdbPos)
		{
			posStr += "-" + position.end.pdbPos +
			          insertionStr(position.end.insertion);
		}

		return posStr;
	}

	function selectPositions(scriptPositions, chainId)
	{
		return "select (" + scriptPositions.join(", ") + ") and :" + chainId + ";";
	}

	function selectSideChains(scriptPositions, chainId)
	{
		return "select ((" + scriptPositions.join(", ") + ") and :" + chainId + " and sidechain) or " +
		"((" + scriptPositions.join(", ") + ") and :" + chainId + " and *.CA);"
	}

	function enableBallAndStick()
	{
		return "wireframe 0.15; spacefill 25%;";
	}

	function disableBallAndStick()
	{
		return "wireframe OFF; spacefill OFF;";
	}

	function center(position, chainId)
	{
		var self = this;
		var scriptPos = self.scriptPosition(position);
		return "center " + scriptPos + ":" + chainId + ";"
	}

	function defaultCenter()
	{
		return "center;";
	}

	function zoom(zoomValue)
	{
		// center and zoom to the selection
		return "zoom " + zoomValue + ";";
	}

	function defaultZoomIn()
	{
		return "zoom in;"
	}

	function defaultZoomOut()
	{
		return "zoom out;"
	}

	function spin(value)
	{
		return "spin " + value + ";";
	}

	/**
	 * Generates highlight script by using the converted highlight positions.
	 *
	 * @param scriptPositions   script positions
	 * @param color             highlight color
	 * @param options           visual style options
	 * @param chain             a PdbChainModel instance
	 * @return {Array} script lines as an array
	 */
	function highlightScript(scriptPositions, color, options, chain)
	{
		var self = this;
		var script = [];

		// add highlight color
		// "select (" + scriptPositions.join(", ") + ") and :" + chain.chainId + ";"
		script.push(self.selectPositions(scriptPositions, chain.chainId));
		script.push(self.setColor(color));

		var displaySideChain = options.displaySideChain != "none";

		// show/hide side chains
		script = script.concat(
			self.generateSideChainScript(scriptPositions, displaySideChain, options, chain));

		return script;
	}

	function formatColor(color)
	{
		// this is for Jmol compatibility
		// (colors should start with an "x" instead of "#")
		return color.replace("#", "x");
	}

	// override required functions
	this.loadPdb = loadPdb;
	this.selectAll = selectAll;
	this.selectNone = selectNone;
	this.setScheme = setScheme;
	this.setColor = setColor;
	this.selectChain = selectChain;
	this.selectAlphaHelix = selectAlphaHelix;
	this.selectBetaSheet = selectBetaSheet;
	this.rainbowColor = rainbowColor;
	this.cpkColor = cpkColor;
	this.hideBoundMolecules = hideBoundMolecules;
	this.setTransparency = setTransparency;
	this.makeOpaque = makeOpaque;
	this.scriptPosition = scriptPosition;
	this.selectPositions = selectPositions;
	this.selectSideChains = selectSideChains;
	this.enableBallAndStick = enableBallAndStick;
	this.disableBallAndStick = disableBallAndStick;
	this.highlightScript = highlightScript;
	this.center = center;
	this.defaultZoomIn = defaultZoomIn;
	this.defaultZoomOut = defaultZoomOut;
	this.defaultCenter = defaultCenter;
	this.spin = spin;
}

// JmolScriptGenerator extends MolScriptGenerator...
JmolScriptGenerator.prototype = new MolScriptGenerator();
JmolScriptGenerator.prototype.constructor = JmolScriptGenerator;
