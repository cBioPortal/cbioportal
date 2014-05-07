/**
 * Base (abstract) script generator class for molecular structure visualizers
 * such as Jmol and Pymol.
 *
 * @author Selcuk Onur Sumer
 */
function MolScriptGenerator()
{
	this.loadPdb = function(pdbId) {
		return "";
	};

	this.selectAll = function() {
		return "";
	};

	this.selectNone = function() {
		return "";
	};

	this.setScheme = function(schemeName) {
		return "";
	};

	this.setColor = function(color) {
		return "";
	};

	this.selectChain = function(chainId) {
		return "";
	};

	this.selectAlphaHelix = function(chainId) {
		return "";
	};

	this.selectBetaSheet = function(chainId) {
		return "";
	};

	this.rainbowColor = function(chainId) {
		return "";
	};

	this.cpkColor = function(chainId) {
		return "";
	};

	this.hideBoundMolecules = function() {
		return "";
	};

	this.setTransparency = function(transparency) {
		return "";
	};

	this.makeOpaque = function() {
		return "";
	};

	this.scriptPosition = function(position) {
		return "";
	};

	this.selectPositions = function(scriptPositions, chainId) {
		return "";
	};

	this.selectSideChains = function(scriptPositions, chainId) {
		return "";
	};

	this.enableBallAndStick = function() {
		return "";
	};

	this.disableBallAndStick = function() {
		return "";
	};

	this.center = function(position, chainId) {
		return "";
	};

	this.zoom = function(zoomValue) {
		return "";
	};

	this.defaultZoomIn = function() {
		return "";
	};

	this.defaultZoomOut = function() {
		return "";
	};

	this.defaultCenter = function() {
		return "";
	};

	this.spin = function(value) {
		return "";
	};

	/**
	 * Generates highlight script by using the converted highlight positions.
	 *
	 * @param scriptPositions   script positions
	 * @param color             highlight color
	 * @param options           visual style options
	 * @param chain             a PdbChainModel instance
	 * @return {Array} script lines as an array
	 */
	this.highlightScript = function(scriptPositions, color, options, chain)
	{
		return [];
	};

	/**
	 * Generates the visual style (scheme, coloring, selection, etc.) script
	 * to be sent to the 3D app.
	 *
	 * @param selection map of script positions
	 * @param chain     a PdbChainModel instance
	 * @param options   visual style options
	 *
	 * @return {Array}  script lines as an array
	 */
	this.generateVisualStyleScript = function(selection, chain, options)
	{
		var self = this;
		var script = [];

		script.push(self.selectAll()); // select everything
		script.push(self.setScheme(options.proteinScheme)); // show selected style view

		// do the initial (uniform) coloring

		script.push(self.setColor(options.defaultColor)); // set default color
		//script.push("translucent [" + _options.defaultTranslucency + "];"); // set default opacity
		script.push(self.selectChain(chain.chainId)); // select the chain
		script.push(self.setColor(options.chainColor)); // set chain color
		//script.push("translucent [" + _options.chainTranslucency + "];"); // set chain opacity

		// additional coloring for the selected chain
		script.push(self.selectChain(chain.chainId));

		if (options.colorProteins == "byAtomType")
		{
			script.push(self.cpkColor(chain.chainId));
		}
		else if (options.colorProteins == "bySecondaryStructure")
		{
			// color secondary structure (for the selected chain)
			script.push(self.selectAlphaHelix(chain.chainId)); // select alpha helices
			script.push(self.setColor(options.structureColors.alphaHelix)); // set color
			script.push(self.selectBetaSheet(chain.chainId)); // select beta sheets
			script.push(self.setColor(options.structureColors.betaSheet)); // set color
		}
		else if (options.colorProteins == "byChain")
		{
			// select the chain
			script.push(self.selectChain(chain.chainId));

			// color the chain by rainbow coloring scheme (gradient coloring)
			script.push(self.rainbowColor(chain.chainId));
		}

		// process mapped residues
		for (var color in selection)
		{
			// select positions (mutations)
			script.push(self.selectPositions(selection[color], chain.chainId));

			// color each residue with a mapped color (this is to sync with diagram colors)

			// use the actual mapped color
			if (options.colorMutations == "byMutationType")
			{
				// color with corresponding mutation color
				script.push(self.setColor(color));
			}
			// use a uniform color
			else if (options.colorMutations == "uniform")
			{
				// color with a uniform mutation color
				script.push(self.setColor(options.mutationColor));
			}

			// show/hide side chains
			script = script.concat(
				self.generateSideChainScript(selection[color],
					options.displaySideChain == "all",
					options,
					chain));
		}

		// TODO see if it is possible to set translucency value without specifying a color
		// ...right now ignoring _options.defaultTranslucency and _options.chainTranslucency

		// adjust structure transparency
		script.push(self.selectAll());
		script.push(self.setTransparency(options.defaultTranslucency));
		//script.push("color translucent [" + _options.defaultTranslucency + "];");
		script.push(self.selectChain(chain.chainId));
		script.push(self.makeOpaque());

		if (options.restrictProtein)
		{
			script.push(self.hideBoundMolecules());
		}

		return script;
	};

	/**
	 * Generates the script to show/hide the side chain for the given positions.
	 * Positions can be in the form of "666" or "666:C", both are fine.
	 *
	 * @param scriptPositions   an array of already generated script positions
	 * @param displaySideChain  flag to indicate to show/hide the side chain
	 * @param options           visual style options
	 * @param chain             a PdbChainModel instance
	 */
	this.generateSideChainScript = function(scriptPositions, displaySideChain, options, chain)
	{
		var self = this;
		var script = [];

		// display side chain (no effect for space-filling)
		if (!(options.proteinScheme == "spaceFilling"))
		{
			// select the corresponding side chain and also the CA atom on the backbone
			script.push(self.selectSideChains(scriptPositions, chain.chainId));

			if (displaySideChain)
			{
				// display the side chain with ball&stick style
				script.push(self.enableBallAndStick());

				// TODO also color side chain wrt atom type (CPK)?
			}
			else
			{
				// hide the side chain
				script.push(self.disableBallAndStick());
			}
		}

		return script;
	};

	/**
	 * Generates the highlight script to be sent to the 3D app.
	 *
	 * @param positions mutation positions to highlight
	 * @param color     highlight color
	 * @param options   visual style options
	 * @param chain     a PdbChainModel instance
	 * @return {Array}  script lines as an array
	 */
	this.generateHighlightScript = function(positions, color, options, chain)
	{
		var self = this;
		var script = [];

		// highlight the selected positions
		if (!_.isEmpty(positions))
		{
			// convert positions to script positions
			var scriptPositions = self.highlightScriptPositions(positions);

			script = script.concat(self.highlightScript(
				scriptPositions, color, options, chain));
		}

		return script;
	};

	this.highlightScriptPositions = function(positions)
	{
		var self = this;
		var scriptPositions = [];

		// convert positions to script positions
		_.each(positions, function(position) {
			scriptPositions.push(self.scriptPosition(position));
		});

		return scriptPositions;
	};
}
