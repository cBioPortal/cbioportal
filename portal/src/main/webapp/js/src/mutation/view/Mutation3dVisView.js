/**
 * Actual 3D Visualizer view. This view is designed to contain the 3D
 * structure visualizer app and its control buttons.
 *
 * options: {el: [target container],
 *           mut3dVis: reference to the Mutation3dVis instance,
 *           pdbProxy: PDB data proxy,
 *           mutationProxy: mutation data proxy
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dVisView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var template = _.template(
			$("#mutation_3d_vis_template").html(),
			// TODO make the images customizable?
			{loaderImage: "images/ajax-loader.gif",
				helpImage: "images/help.png"});

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// initially hide the 3d visualizer container
		var container3d = self.$el;
		container3d.hide();

		// initially hide the residue warning message
		self.hideResidueWarning();
		self.hideNoMapWarning();

		// initially hide the help content
		self.$el.find(".mutation-3d-vis-help-content").hide();

		// update the container of 3d visualizer
		if (mut3dVis != null)
		{
			mut3dVis.updateContainer(container3d);
		}

		// add listeners to panel (header) buttons

		self.$el.find(".mutation-3d-close").click(function() {
			self.hideView();
		});

		self.$el.find(".mutation-3d-minimize").click(function(){
			if (mut3dVis != null)
			{
				mut3dVis.toggleSize();
			}
		});

		// format toolbar elements

//		var spinChecker = self.$el.find(".mutation-3d-spin");
//		spinChecker.change(function(){
//			if (mut3dVis != null)
//			{
//				mut3dVis.toggleSpin();
//			}
//		});

		// mutation style controls
		self._initMutationControls();

		// protein style controls
		self._initProteinControls();

		// zoom slider
		//self._initZoomSlider();

		// init buttons
		self._initButtons();
	},
	/**
	 * Initializes the control buttons.
	 */
	_initButtons: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

//		var centerSelected = self.$el.find(".mutation-3d-center-selected");
//
//		centerSelected.button(
//			{icons: {primary: "ui-icon-arrow-4"},
//			text: false});
//
//		centerSelected.click(function() {
//			// center on the selected mutation
//			mut3dVis.center();
//		});
//
//		var centerDefault = self.$el.find(".mutation-3d-center-default");
//
//		centerDefault.button(
//			{icons: {primary: "ui-icon-arrowreturn-1-w"},
//			text: false});
//
//		centerDefault.click(function() {
//			// restore to the default center
//			mut3dVis.resetCenter();
//		});
//
//		var qtipOpts = self._generateTooltipOpts("NA");
//		qtipOpts.content = {attr: 'alt'};
//
//		centerSelected.qtip(qtipOpts);
//		centerDefault.qtip(qtipOpts);

		// init help text controls

		var helpContent = self.$el.find(".mutation-3d-vis-help-content");
		var helpInit = self.$el.find(".mutation-3d-vis-help-init");
		var helpInitLink = self.$el.find(".mutation-3d-vis-help-init a");
		var helpClose = self.$el.find(".mutation-3d-vis-help-close");

		// add listener to help link
		helpInitLink.click(function(event) {
			event.preventDefault();
			helpContent.slideToggle();
			helpInit.slideToggle();
		});

		// add listener to help close button
		helpClose.click(function(event) {
			event.preventDefault();
			helpContent.slideToggle();
			helpInit.slideToggle();
		});
	},
	/**
	 * Initializes the mutation style options UI and
	 * the corresponding event handlers.
	 */
	_initMutationControls: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		var sideChain = self.$el.find(".mutation-3d-side-chain-select");

		// handler for side chain checkbox
		sideChain.change(function() {
			//var display = sideChain.is(":checked");
			var selected = $(this).val();

			if (mut3dVis)
			{
				// update flag
				mut3dVis.updateOptions({displaySideChain: selected});
				mut3dVis.reapplyStyle();
			}
		});

		var colorMenu = self.$el.find(".mutation-3d-mutation-color-select");

		colorMenu.change(function() {
			var selected = $(this).val();

			if (mut3dVis)
			{
				// update color options
				mut3dVis.updateOptions({colorMutations: selected});
				// refresh view with new options
				mut3dVis.reapplyStyle();
			}
		});

//		var colorByType = self.$el.find(".mutation-3d-mutation-color-by-type");
//		// handler for color type checkbox
//		colorByType.change(function() {
//			var color = colorByType.is(":checked");
//			var type = "byMutationType";
//
//			// if not coloring by mutation type, then use default atom colors
//			if (!color)
//			{
//				type = "byAtomType";
//			}
//
//			if (mut3dVis)
//			{
//				// update and reapply visual style
//				mut3dVis.updateOptions({colorMutations: type});
//				mut3dVis.reapplyStyle();
//			}
//		});

		// add info tooltip for the color and side chain checkboxes
		self._initMutationColorInfo();
		self._initSideChainInfo();
	},
	/**
	 * Initializes the protein style options UI and
	 * the corresponding event handlers.
	 */
	_initProteinControls: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		var displayNonProtein = self.$el.find(".mutation-3d-display-non-protein");

		// handler for hide non protein checkbox
		displayNonProtein.change(function() {
			var display = displayNonProtein.is(":checked");

			if (mut3dVis)
			{
				// update flag
				mut3dVis.updateOptions({restrictProtein: !display});
				// refresh view with new options
				mut3dVis.reapplyStyle();
			}
		});

		// add info tooltip for the checkbox
		self._initHideNonProteinInfo();

		// protein scheme selector
		self._initProteinSchemeSelector();

		// protein color selector
		self._initProteinColorSelector();
	},
	/**
	 * Initializes the protein color selector drop-down menu
	 * with its default action handler.
	 */
	_initProteinColorSelector: function()
	{
		var self = this;
		var colorMenu = self.$el.find(".mutation-3d-protein-color-select");
		var mut3dVis = self.options.mut3dVis;

		colorMenu.change(function() {
			var selected = $(this).val();

			// update color options
			mut3dVis.updateOptions({colorProteins: selected});

			// refresh view with new options
			mut3dVis.reapplyStyle();
		});
	},
	/**
	 * Initializes the protein scheme selector dropdown menu
	 * with its default action handler.
	 */
	_initProteinSchemeSelector: function()
	{
		var self = this;

		var mut3dVis = self.options.mut3dVis;

		// selection menus
		var styleMenu = self.$el.find(".mutation-3d-protein-style-select");
		var colorMenu = self.$el.find(".mutation-3d-protein-color-select");

		// TODO chosen is somehow problematic...
		//styleMenu.chosen({width: 120, disable_search: true});

		// add info tooltip for the color selector
		self._initProteinColorInfo();

		// bind the change event listener
		styleMenu.change(function() {

			var selectedScheme = $(this).val();
			var selectedColor = false;

			// re-enable every color selection for protein
			colorMenu.find("option").removeAttr("disabled");

			var toDisable = null;

			// find the option to disable
			if (selectedScheme == "spaceFilling")
			{
				// disable color by secondary structure option
				toDisable = colorMenu.find("option[value='bySecondaryStructure']");
			}
			else
			{
				// disable color by atom type option
				toDisable = colorMenu.find("option[value='byAtomType']");
			}

			// if the option to disable is currently selected, select the default option
			if (toDisable.is(":selected"))
			{
				toDisable.removeAttr("selected");
				colorMenu.find("option[value='uniform']").attr("selected", "selected");
				selectedColor = "uniform";
			}

			toDisable.attr("disabled", "disabled");

			if (mut3dVis)
			{
				var opts = {};

				opts.proteinScheme = selectedScheme;

				if (selectedColor)
				{
					opts.colorProteins = selectedColor;
				}

				mut3dVis.updateOptions(opts);

				// reapply view with new settings
				//mut3dVis.changeStyle(selectedScheme);
				mut3dVis.reapplyStyle();
			}
		});
	},
	/**
	 * Initializes the zoom slider with default values.
	 */
	_initZoomSlider: function()
	{
		var self = this;
		var zoomSlider = self.$el.find(".mutation-3d-zoom-slider");
		var mut3dVis = self.options.mut3dVis;

		// TODO make slider values customizable?

		// helper function to transform slider value into an actual zoom value
		var transformValue = function (value)
		{
			if (value < 0)
			{
				return 100 + value;
			}
			else
			{
				return 100 + (value * 5);
			}
		};

		// handler function for zoom slider events
		var zoomHandler = function(event, ui)
		{
			if (mut3dVis)
			{
				mut3dVis.zoomTo(transformValue(ui.value));
			}
		};

		// init y-axis slider controls
		zoomSlider.slider({value: 0,
			min: -80,
			max: 80,
			stop: zoomHandler,
			slide: zoomHandler
		});
	},
	/**
	 * Updates the 3D visualizer content for the given gene,
	 * pdb id, and chain.
	 *
	 * @param geneSymbol    hugo gene symbol
	 * @param pdbId         pdb id
	 * @param chain         PdbChainModel instance
	 * @param callback      function to be called after update
	 */
	updateView: function(geneSymbol, pdbId, chain, callback)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;
		var pdbProxy = self.options.pdbProxy;

		var mapCallback = function(positionMap) {
			// update position map of the chain
			chain.positionMap = positionMap;

			// reload the selected pdb and chain data
			mut3dVis.show();
			self.refreshView(pdbId, chain, callback);

			// store pdb id and chain for future reference
			self.pdbId = pdbId;
			self.chain = chain;
		};

		var infoCallback = function(pdbInfo) {
			var model = {pdbId: pdbId,
				chainId: chain.chainId,
				pdbInfo: ""};

			if (pdbInfo)
			{
				model.pdbInfo = pdbInfo;
			}

			// init info view
			var infoView = new Mutation3dVisInfoView(
				{el: self.$el.find(".mutation-3d-info"), model: model});
			infoView.render();

			// update positionMap for the chain
			// (retrieve data only once)
			pdbProxy.getPositionMap(geneSymbol, chain, mapCallback);
		};

		pdbProxy.getPdbInfo(pdbId, infoCallback);
	},
	/**
	 * Refreshes (reloads) the 3D visualizer for the given
	 * pdb id and chain.
	 *
	 * If no pdb id and chain provided, then reloads with
	 * the last known pdb id and chain.
	 *
	 * @param pdbId     pdb id
	 * @param chain     PdbChainModel instance
	 * @param callback  function to be called after refresh
	 */
	refreshView: function(pdbId, chain, callback)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// hide warning messages
		self.hideResidueWarning();
		self.hideNoMapWarning();

		// helper function to show/hide mapping information
		var showMapInfo = function(mapped)
		{
			if (mapped.length == 0)
			{
				// show the warning text
				self.showNoMapWarning();
			}
			else
			{
				// TODO display exactly what is mapped?
//				var proxy = self.options.mutationProxy;
//				var types = [];
//
//				_.each(mapped, function(id, idx) {
//					var mutation = proxy.getMutationUtil().getMutationIdMap()[id];
//					types.push(mutation.mutationType);
//				});
//
//				types = _.unique(types);

				// hide the warning text
				self.hideNoMapWarning();
			}
		};

		// do not reload (just refresh) if no pdb id or chain is provided,
		// or the provided chain and the previous chain are the same
		if ((pdbId == null && chain == null) ||
		    (pdbId == self.pdbId && chain == self.chain))
		{
			// just refresh
			var mapped = mut3dVis.refresh();

			// update mapping info
			showMapInfo(mapped);

			// call the provided custom callback function
			if (_.isFunction(callback))
			{
				callback();
			}
		}
		// reload the new pdb structure
		else
		{
			// reset zoom slider
			//var zoomSlider = self.$el.find(".mutation-3d-zoom-slider");
			//zoomSlider.slider("value", 0);

			// show loader image
			self.showLoader();

			// set a short delay to allow loader image to appear
			setTimeout(function() {
				// reload the visualizer
				var mapped = mut3dVis.reload(pdbId, chain, function() {
					// hide the loader image after reload complete
					self.hideLoader();
					// call the provided custom callback function
					if (_.isFunction(callback))
					{
						callback();
					}
				});
				// update mapping info if necessary
				showMapInfo(mapped);
			}, 50);
		}
	},
	/**
	 * Initializes the mutation color information as a tooltip
	 * for the corresponding checkbox.
	 */
	_initMutationColorInfo: function()
	{
		var self = this;

		var info = self.$el.find(".mutation-type-color-help");

		var content = _.template($("#mutation_3d_type_color_tip_template").html());
		var options = self._generateTooltipOpts(content);

		// make it wider
		options.style.classes += " qtip-wide";

		info.qtip(options);
	},
	/**
	 * Initializes the protein structure color information as a tooltip
	 * for the corresponding selection menu.
	 */
	_initProteinColorInfo: function()
	{
		var self = this;

		var info = self.$el.find(".protein-struct-color-help");

		var content = _.template($("#mutation_3d_structure_color_tip_template").html());
		var options = self._generateTooltipOpts(content);

		// make it wider
		options.style.classes += " qtip-wide";

		info.qtip(options);
	},
	/**
	 * Initializes the side chain information as a tooltip
	 * for the corresponding checkbox.
	 */
	_initSideChainInfo: function()
	{
		var self = this;

		var info = self.$el.find(".display-side-chain-help");

		var content = _.template($("#mutation_3d_side_chain_tip_template").html());

		var options = self._generateTooltipOpts(content);
		info.qtip(options);
	},
	/**
	 * Initializes the side chain information as a tooltip
	 * for the corresponding checkbox.
	 */
	_initHideNonProteinInfo: function()
	{
		var self = this;

		var info = self.$el.find(".display-non-protein-help");

		var content = _.template($("#mutation_3d_non_protein_tip_template").html());

		var options = self._generateTooltipOpts(content);
		info.qtip(options);
	},
	/**
	 * Generates the default tooltip (qTip) options for the given
	 * tooltip content.
	 *
	 * @param content  actual tooltip content
	 * @return {Object}    qTip options for the given content
	 */
	_generateTooltipOpts: function(content)
	{
		return {content: {text: content},
			hide: {fixed: true, delay: 100, event: 'mouseout'},
			show: {event: 'mouseover'},
			style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
			position: {my:'top right', at:'bottom center'}};
	},
	/**
	 * Minimizes the 3D visualizer panel.
	 */
	minimizeView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		if (mut3dVis)
		{
			mut3dVis.minimize();
		}
	},
	/**
	 * Restores the 3D visualizer panel to its full size.
	 */
	maximizeView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		if (mut3dVis)
		{
			mut3dVis.maximize();
		}
	},
	/**
	 * Hides the 3D visualizer panel.
	 */
	hideView: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		// hide the vis pane
		if (mut3dVis != null)
		{
			mut3dVis.hide();
		}

		// trigger corresponding event
		self.dispatcher.trigger(
			MutationDetailsEvents.VIEW_3D_PANEL_CLOSED);
	},
	isVisible: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		return mut3dVis.isVisible();
	},
	/**
	 * Focuses the 3D visualizer on the residue
	 * corresponding to the given pileup of mutations.
	 *
	 * If this function is invoked without a parameter,
	 * then resets the focus to the default state.
	 *
	 * @param pileup    Pileup instance
	 * @return {boolean} true if focus successful, false otherwise
	 */
	focusView: function(pileup)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		if (pileup)
		{
			return mut3dVis.focusOn(pileup);
		}
		else
		{
			mut3dVis.resetFocus();
			return true;
		}
	},
	/**
	 * Highlights the 3D visualizer for the residue
	 * corresponding to the given array of pileups of mutations.
	 *
	 * @param pileups   an array of Pileup instances
	 * @param reset     whether to reset previous highlights
	 * @return {Number} number of mapped residues
	 */
	highlightView: function(pileups, reset)
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		return mut3dVis.highlight(pileups, reset);
	},
	/**
	 * Resets all residue highlights.
	 */
	resetHighlight: function()
	{
		var self = this;
		var mut3dVis = self.options.mut3dVis;

		mut3dVis.resetHighlight();
	},
	/**
	 * Shows the loader image for the 3D vis container.
	 */
	showLoader: function()
	{
		var self = this;
		var loaderImage = self.$el.find(".mutation-3d-vis-loader");
		var container = self.$el.find(".mutation-3d-vis-container");

		// hide actual vis container
		// (jQuery.hide function is problematic with 3D visualizer,
		// instead we are changing height)
		var height = container.css("height");

		if (!(height === 0 || height === "0px"))
		{
			self._actualHeight = height;
			container.css("height", 0);
		}

		// show image
		loaderImage.show();
	},
	/**
	 * Hides the loader image and shows the actual 3D visualizer content.
	 */
	hideLoader: function()
	{
		var self = this;
		var loaderImage = self.$el.find(".mutation-3d-vis-loader");
		var container = self.$el.find(".mutation-3d-vis-container");

		// hide image
		loaderImage.hide();

		// show actual vis container
		container.css("height", self._actualHeight);
	},
	/**
	 * Shows a warning message for unmapped residues.
	 *
	 * @param unmappedCount  number of unmapped selections
	 * @param selectCount    total number of selections
	 */
	showResidueWarning: function(unmappedCount, selectCount)
	{
		var self = this;
		var warning = self.$el.find(".mutation-3d-residue-warning");
		var unmapped = self.$el.find(".mutation-3d-unmapped-info");

		// show warning only if no other warning is visible
		if (!self.$el.find(".mutation-3d-nomap-warning").is(":visible"))
		{
			if (selectCount > 1)
			{
				unmapped.text(unmappedCount + " of the selections");
			}
			else
			{
				unmapped.text("Selected mutation");
			}

			warning.show();
		}
	},
	/**
	 * Hides the residue warning message.
	 */
	hideResidueWarning: function()
	{
		var self = this;
		var warning = self.$el.find(".mutation-3d-residue-warning");

		warning.hide();
	},
	/**
	 * Shows a warning message for unmapped residues.
	 */
	showNoMapWarning: function()
	{
		var self = this;
		var warning = self.$el.find(".mutation-3d-nomap-warning");

		warning.show();
	},
	/**
	 * Hides the residue warning message.
	 */
	hideNoMapWarning: function()
	{
		var self = this;
		var warning = self.$el.find(".mutation-3d-nomap-warning");

		warning.hide();
	}
});