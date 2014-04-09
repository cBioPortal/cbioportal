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
		// TODO there is no "trace" in PyMOL, ribbon is the most similar one
		trace: "hide everything; show ribbon;"
	};

	function reinitialize()
	{
		return "reinitialize;";
	}

	function bgColor(color)
	{
		return "bg_color " + formatColor(color) + ";";
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
		return "color " + formatColor(color) + ", sele;";
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
		// TODO cartoon_transparency doesn't work for chain or residue selections
		// see issue:  http://sourceforge.net/p/pymol/bugs/129/
		return ("set transparency, " + (transparency / 10) + ", sele;\n" +
		        "set cartoon_transparency, " + (transparency / 10) + ", sele;\n" +
		        "set sphere_transparency, " + (transparency / 10) + ", sele;\n" +
		        "set stick_transparency, " + (transparency / 10) + ", sele;");
	}

	function makeOpaque()
	{
		return setTransparency(0);
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
		// restrict to protein only
		return "hide everything," +
		       "not resn asp+glu+arg+lys+his+asn+thr+cys+gln+tyr+ser+gly+ala+leu+val+ile+met+trp+phe+pro";
	}

	function formatColor(color)
	{
		// this is for Pymol compatibility
		// (colors should start with an "0x" instead of "#")
		return color.replace("#", "0x");
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
	this.bgColor = bgColor;
}

// PymolScriptGenerator extends JmolScriptGenerator...
PymolScriptGenerator.prototype = new JmolScriptGenerator();
PymolScriptGenerator.prototype.constructor = PymolScriptGenerator;

