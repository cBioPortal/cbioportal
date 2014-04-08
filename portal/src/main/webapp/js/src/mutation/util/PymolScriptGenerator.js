/**
 * PymolScriptGenerator class (extends JmolScriptGenerator)
 *
 * Script generator for the PyMOL application.
 *
 * @author Selcuk Onur Sumer
 */
function PymolScriptGenerator()
{
	// Predefined style scripts for Jmol
	var _styleScripts = {
		ballAndStick: "hide everything; show spheres; show sticks; alter all, vdw=0.50",
		spaceFilling: "hide everything; show spheres;",
		ribbon: "hide everything; show ribbon;",
		cartoon: "hide everything; show cartoon;",
		trace: "hide everything; show lines;"
	};

	function reinitialize()
	{
		return "reinitialize;";
	}

	function loadPdb(pdbId)
	{
		return "fetch " + pdbId + ";";
	}

	function setScheme(schemeName)
	{
		return _styleScripts[schemeName];
	}

	function setColor (color)
	{
		// this is for Jmol compatibility
		// (colors should start with an "x" instead of "#")
		color = color.replace("#", "0x");

		return "color " + color + ", sele;";
	}

	function selectChain(chainId)
	{
		return "select chain " + chainId + ";";
	}

	function selectAlphaHelix(chainId)
	{
		return "select (chain " + chainId + ") and (ss h);";
	}

	function selectBetaSheet(chainId)
	{
		return "select (chain " + chainId + ") and (ss s);";
	}

	function selectPositions(scriptPositions, chainId)
	{
		return "select (resi " + scriptPositions.join(",") + ") and (chain " + chainId + ");";
	}

	function selectSideChains(scriptPositions, chainId)
	{
		return "select ((resi " + scriptPositions.join(",") + ") and (chain " + chainId + ") and (not name c+n+o));";
	}

	function setTransparency(transparency)
	{
		// TODO this doesn't work...
		return "set transparency=" + (transparency / 10) + ", sele;";
	}

	function makeOpaque()
	{
		return "set transparency=" + 1.0 + ", sele;";
	}

	function enableBallAndStick()
	{
		return "show spheres, sele; show sticks, sele; alter sele, vdw=0.50;";
	}

	function disableBallAndStick()
	{
		return "hide spheres, sele; hide sticks, sele;";
	}

	function rainbowColor(chainId)
	{
		return "spectrum count, rainbow_rev, sele";
	}

	function cpkColor(chainId)
	{
		return "util.cbaw sele;";
	}

	function hideBoundMolecules()
	{
		// TODO restrict to protein only
		return "";
	}

	// override required functions
	this.loadPdb = loadPdb;
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
	this.selectPositions = selectPositions;
	this.selectSideChains = selectSideChains;
	this.enableBallAndStick = enableBallAndStick;
	this.disableBallAndStick = disableBallAndStick;
	this.reinitialize = reinitialize;
}

// PymolScriptGenerator extends JmolScriptGenerator...
PymolScriptGenerator.prototype = new JmolScriptGenerator();
PymolScriptGenerator.prototype.constructor = PymolScriptGenerator;

