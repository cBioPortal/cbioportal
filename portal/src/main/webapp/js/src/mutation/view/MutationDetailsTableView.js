/**
 * Default table view for the mutations.
 *
 * options: {el: [target container],
 *           model: {mutations: [mutation data as an array of JSON objects],
 *                   geneSymbol: [hugo gene symbol as a string],
 *                   tableOpts: [mutation table options -- optional]}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsTableView = Backbone.View.extend({
	initialize : function (options) {
		this.options = options || {};

		// custom event dispatcher
		this.dispatcher = {};
		_.extend(this.dispatcher, Backbone.Events);
	},
	render: function()
	{
		var self = this;

		var mutations = new MutationCollection(self.model.mutations);

		var tableHeaders = _.template(
				$("#mutation_details_table_header_row_template").html(), {});

		var tableRows = "";

		for (var i=0; i < mutations.length; i++)
		{
			var dataRowVariables = self._getDataRowVars(mutations.at(i));

			var tableDataTemplate = _.template(
					$("#mutation_details_table_data_row_template").html(),
					dataRowVariables);

			tableRows += tableDataTemplate;
		}

		var tableVariables = {geneSymbol: self.model.geneSymbol,
			tableHeaders: tableHeaders,
			tableRows: tableRows};

		// compile the table template
		var tableTemplate = _.template(
				$("#mutation_details_table_template").html(),
				tableVariables);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(tableTemplate);

		self.format();
	},
	/**
	 * Formats the contents of the view after the initial rendering.
	 */
	format: function()
	{
		var self = this;

		// remove invalid links
		self.$el.find('a[href=""]').remove();
		self.$el.find('a[alt=""]').remove();

		// add click listener for each igv link to get the actual parameters
		// from another servlet
		_.each(self.$el.find('.igv-link'), function(element, index) {
			// TODO use mutation id, and dispatch an event
			var url = $(element).attr("alt");

			$(element).click(function(evt) {
				// get parameters from the server and call related igv function
				$.getJSON(url, function(data) {
					//console.log(data);
					// TODO this call displays warning message (resend)
					prepIGVLaunch(data.bamFileUrl, data.encodedLocus, data.referenceGenome, data.trackName);
				});
			});
		});

		// add click listener for each 3D link
		self.$el.find('.mutation-table-3d-link').click(function(evt) {
			evt.preventDefault();

			var mutationId = $(this).attr("alt");

			self.dispatcher.trigger(
				MutationDetailsEvents.PDB_LINK_CLICKED,
				mutationId);
		});

		// add click listener for each 3D link
		self.$el.find('.mutation-table-protein-change a').click(function(evt) {
			evt.preventDefault();

			var mutationId = $(this).closest("tr").attr("id");

			self.dispatcher.trigger(
				MutationDetailsEvents.PROTEIN_CHANGE_LINK_CLICKED,
				mutationId);
		});

		var tableSelector = self.$el.find('.mutation_details_table');

		var tableUtil = new MutationTable(tableSelector,
			self.model.geneSymbol,
			self.model.mutations,
			self.model.tableOpts);

		// format the table (convert to a DataTable)
		tableUtil.formatTable();

		// save a reference to the util for future access
		self.tableUtil = tableUtil;
	},
	/**
	 * Highlights the given mutations in the table.
	 *
	 * @param mutations mutations to highlight
	 */
	highlight: function(mutations)
	{
		var self = this;
		var tableSelector = self.$el.find('.mutation_details_table');

		for (var i = 0; i < mutations.length; i++)
		{
			//var row = tableSelector.find("#" + mutations[i].mutationId);
            var row = tableSelector.find("tr." + mutations[i].mutationSid);
            row.addClass("mutation-table-highlight");
		}
	},
	/**
	 * Clears all highlights from the mutation table.
	 */
	clearHighlights: function()
	{
		var self = this;
		var tableSelector = self.$el.find('.mutation_details_table');

		// TODO this depends on highlight function
		tableSelector.find('tr').removeClass("mutation-table-highlight");
	},
	/**
	 * Filters out all other mutations than the given mutations.
	 *
	 * @param mutations mutations to keep
	 * @param updateBox [optional] show the filter text in the search box
	 * @param limit     [optional] column to limit filtering to
	 */
	filter: function(mutations, updateBox, limit)
	{
		var self = this;
		var oTable = self.tableUtil.getDataTable();

		// construct regex
		var ids = [];

		for (var i = 0; i < mutations.length; i++)
		{
			ids.push(mutations[i].mutationSid);
		}

		var regex = "(" + ids.join("|") + ")";
		var asRegex = true;

		// empty mutation list, just show everything
		if (ids.length == 0)
		{
			regex = "";
			asRegex = false;
		}

		// disable callbacks before filtering, otherwise it creates a chain reaction
		self.tableUtil.setCallbackActive(false);

		// apply filter
		self._applyFilter(oTable, regex, asRegex, updateBox, limit);

		// enable callbacks after filtering
		self.tableUtil.setCallbackActive(true);
	},
	/**
	 * Resets all table filters (rolls back to initial state)
	 */
	resetFilters: function()
	{
		var self = this;
		// pass an empty array to show everything
		self.filter([], true);
		// also clean filter related variables
		self.tableUtil.cleanFilters();
	},
	/**
	 * Rolls back the table to the last state where a manual search
	 * (manual filtering) performed. This function is required since
	 * we also filter the table programmatically.
	 */
	rollBack: function()
	{
		var self = this;
		var oTable = self.tableUtil.getDataTable();

		// disable callbacks before filtering, otherwise it creates a chain reaction
		self.tableUtil.setCallbackActive(false);

		// re-apply last manual filter string
		var searchStr = self.tableUtil.getManualSearch();
		self._applyFilter(oTable, searchStr, false);

		// enable callbacks after filtering
		self.tableUtil.setCallbackActive(true);
	},
	/**
	 * Filters the given data table with the provided filter string.
	 *
	 * @param oTable    target data table to be filtered
	 * @param filterStr filter string to apply with the filter
	 * @param asRegex   indicates if the given filterStr is a regex or not
	 * @param updateBox [optional] show the filter text in the search box
	 * @param limit     [optional] column to limit filtering to
	 * @private
	 */
	_applyFilter: function(oTable, filterStr, asRegex, updateBox, limit)
	{
		if (limit == undefined)
		{
			limit = null;
		}

		// TODO not updating the filter text in the box may be confusing
		if (updateBox == undefined)
		{
			updateBox = false;
		}

		var smartFilter = true;
		var caseInsensitive = true;

		oTable.fnFilter(filterStr, limit, asRegex, smartFilter, updateBox, caseInsensitive);
	},
	/**
	 * Extract & generates data required to visualize a single row of the table.
	 * The data returned by this function can be used to compile a mutation data
	 * table row template.
	 *
	 * @param mutation  a MutationModel instance
	 * @return {object} template variables as a single object
	 * @private
	 */
	_getDataRowVars: function(mutation)
	{
		var self = this;

		var visualStyleMaps = MutationViewsUtil.getVisualStyleMaps();

		var mutationTypeMap = visualStyleMaps.mutationType;
		var validationStatusMap = visualStyleMaps.validationStatus;
		var mutationStatusMap = visualStyleMaps.mutationStatus;
		var omaScoreMap = visualStyleMaps.omaScore;
		var cnaMap = visualStyleMaps.cna;

		var vars = {};

		vars.mutationId = mutation.mutationId;
        vars.mutationSid = mutation.mutationSid;
		vars.linkToPatientView = mutation.linkToPatientView;
        vars.cancerType = mutation.cancerType;
        vars.cancerStudy = mutation.cancerStudy;
        vars.cancerStudyShort = mutation.cancerStudyShort;
        vars.cancerStudyLink = mutation.cancerStudyLink;

		var caseId = self._getCaseId(mutation.caseId);
		vars.caseId = caseId.text;
		vars.caseIdClass = caseId.style;
		vars.caseIdTip = caseId.tip;

        var proteinChange = self._getProteinChange(mutation);
		vars.proteinChange = proteinChange.text;
		vars.proteinChangeClass = proteinChange.style;
		vars.proteinChangeTip = proteinChange.tip;

		var tumorType = self._getTumorType(mutation);
		vars.tumorType = tumorType.text;
		vars.tumorTypeClass = tumorType.style;
		vars.tumorTypeTip = tumorType.tip;

		var mutationType = self._getMutationType(mutationTypeMap, mutation.mutationType);
		vars.mutationTypeClass = mutationType.style;
		vars.mutationTypeText = mutationType.text;

		var cosmic = self._getCosmic(mutation.cosmicCount);
		vars.cosmicClass = cosmic.style;
		vars.cosmicCount = cosmic.count;

		var fis = self._getFis(omaScoreMap, mutation.functionalImpactScore, mutation.fisValue);
		vars.fisClass = fis.fisClass;
		vars.omaClass = fis.omaClass;
		vars.fisValue = fis.value;
		vars.fisText = fis.text;

		//vars.xVarLink = mutation.xVarLink;
		//vars.msaLink = mutation.msaLink;
		vars.igvLink = mutation.igvLink;

		vars.pdbMatchId = self._getPdbMatchId(mutation);

		var mutationStatus = self._getMutationStatus(mutationStatusMap, mutation.mutationStatus);
		vars.mutationStatusTip = mutationStatus.tip;
		vars.mutationStatusClass = mutationStatus.style;
		vars.mutationStatusText = mutationStatus.text;

		var validationStatus = self._getValidationStatus(validationStatusMap, mutation.validationStatus);
		vars.validationStatusTip = validationStatus.tip;
		vars.validationStatusClass = validationStatus.style;
		vars.validationStatusText = validationStatus.text;

		vars.sequencingCenter = mutation.sequencingCenter;
		vars.chr = mutation.chr;

		var startPos = self._getIntValue(mutation.startPos);
		vars.startPos = startPos.text;
		vars.startPosClass = startPos.style;

		var endPos = self._getIntValue(mutation.endPos);
		vars.endPos = endPos.text;
		vars.endPosClass = endPos.style;

		vars.referenceAllele = mutation.referenceAllele;
		vars.variantAllele = mutation.variantAllele;

		var alleleCount = self._getAlleleCount(mutation.tumorAltCount);
		vars.tumorAltCount = alleleCount.text;
		vars.tumorAltCountClass = alleleCount.style;

		alleleCount = self._getAlleleCount(mutation.tumorRefCount);
		vars.tumorRefCount = alleleCount.text;
		vars.tumorRefCountClass = alleleCount.style;

		alleleCount = self._getAlleleCount(mutation.normalAltCount);
		vars.normalAltCount = alleleCount.text;
		vars.normalAltCountClass = alleleCount.style;

		alleleCount = self._getAlleleCount(mutation.normalRefCount);
		vars.normalRefCount = alleleCount.text;
		vars.normalRefCountClass = alleleCount.style;

		var tumorFreq = self._getAlleleFreq(mutation.tumorFreq,
				mutation.tumorAltCount,
				mutation.tumorRefCount,
				"simple-tip-left");
		vars.tumorFreq = tumorFreq.text;
		vars.tumorFreqClass = tumorFreq.style;
		vars.tumorFreqTipClass = tumorFreq.tipClass;
		vars.tumorTotalCount = tumorFreq.total;

		var normalFreq = self._getAlleleFreq(mutation.normalFreq,
				mutation.normalAltCount,
				mutation.normalRefCount,
				"simple-tip-left");
		vars.normalFreq = normalFreq.text;
		vars.normalFreqClass = normalFreq.style;
		vars.normalFreqTipClass = normalFreq.tipClass;
		vars.normalTotalCount = normalFreq.total;

		var mutationCount = self._getIntValue(mutation.mutationCount);
		vars.mutationCount = mutationCount.text;
		vars.mutationCountClass = mutationCount.style;

		var cna = self._getCNA(cnaMap, mutation.cna);
		vars.cna = cna.text;
		vars.cnaClass = cna.style;
		vars.cnaTip = cna.tip;

		return vars;
	},
	// TODO identify duplicate/similar get functions
	_getCNA : function(map, value)
	{
		var style, label, tip;

		if (map[value] != null)
		{
			style = map[value].style;
			label = map[value].label;
			tip = map[value].tooltip;
		}
		else
		{
			style = map.unknown.style;
			label = map.unknown.label;
			tip = map.unknown.tooltip;
		}

		return {style: style, tip: tip, text: label};
	},
    /**
     * Returns the text content, the css class, and the tooltip
     * for the given case id value. If the length of the actual
     * case id string is too long, then creates a short form of
     * the case id ending with an ellipsis.
     *
     * @param caseId    actual case id string
     * @return {{style: string, text: string, tip: string}}
     * @private
     */
	_getCaseId: function(caseId)
	{
		// TODO customize this length?
		var maxLength = 16;

		var text = caseId;
		var style = ""; // no style for short case id strings
		var tip = caseId; // display full case id as a tip

		// no need to bother with clipping the text for 1 or 2 chars.
		if (caseId.length > maxLength + 2)
		{
			text = caseId.substring(0, maxLength) + "...";
			style = "simple-tip"; // enable tooltip for long strings
		}

		return {style: style, tip: tip, text: text};
	},
    /**
     * Returns the text content and the css class for the given
     * mutation type value.
     *
     * @param map   map of <mutationType, {label, style}>
     * @param value actual string value of the mutation type
     * @return {{style: string, text: string}}
     * @private
     */
	_getMutationType: function(map, value)
	{
		var style, text;
		value = value.toLowerCase();

		if (map[value] != null)
		{
			style = map[value].style;
			text = map[value].label;
		}
		else
		{
			style = map.other.style;
			text = value;
		}

		return {style: style, text: text};
	},
	/**
     * Returns the text content, the css class, and the tooltip
	 * for the given mutation type value.
     *
     * @param map   map of <mutationStatus, {label, style, tooltip}>
     * @param value actual string value of the mutation status
     * @return {{style: string, text: string, tip: string}}
     * @private
     */
	_getMutationStatus: function(map, value)
	{
		var style = "simple-tip";
		var text = value;
		var tip = "";
		value = value.toLowerCase();

		if (map[value] != null)
		{
			style = map[value].style;
			text = map[value].label;
			tip = map[value].tooltip;
		}

		return {style: style, tip: tip, text: text};
	},
	/**
	 * Returns the text content, the css class, and the tooltip
	 * for the given validation status value.
	 *
	 * @param map   map of <validationStatus, {label, style, tooltip}>
	 * @param value actual string value of the validation status
	 * @return {{style: string, text: string, tip: string}}
	 * @private
	 */
	_getValidationStatus: function(map, value)
	{
		var style, label, tip;
		value = value.toLowerCase();

		if (map[value] != null)
		{
			style = map[value].style;
			label = map[value].label;
			tip = map[value].tooltip;
		}
		else
		{
			style = map.unknown.style;
			label = map.unknown.label;
			tip = map.unknown.tooltip;
		}

		return {style: style, tip: tip, text: label};
	},
	/**
	 * Returns the text content, the css classes, and the tooltip
	 * for the given string and numerical values of a
	 * functional impact score.
	 *
	 * @param map       map of <FIS, {label, style, tooltip}>
	 * @param fis       string value of the functional impact (h, l, m or n)
	 * @param fisValue  numerical value of the functional impact score
	 * @return {{fisClass: string, omaClass: string, value: string, text: string}}
	 * @private
	 */
	_getFis: function(map, fis, fisValue)
	{
		var text = "";
		var fisClass = "";
		var omaClass = "";
		var value = "";
		fis = fis.toLowerCase();

		if (map[fis] != null)
		{
			value = map[fis].tooltip;

			if (fisValue != null)
			{
				value = fisValue.toFixed(2);
			}

			text = map[fis].label;
			fisClass = map[fis].style;
			omaClass = "oma_link";
		}

		return {fisClass: fisClass, omaClass: omaClass, value: value, text: text};
	},
	/**
	 * Returns the text content, the css classes, and the total
	 * allele count for the given allele frequency.
	 *
	 * @param frequency allele frequency
	 * @param alt       alt allele count
	 * @param ref       ref allele count
	 * @param tipClass  css class for the tooltip
	 * @return {{text: string, total: number, style: string, tipClass: string}}
	 * @private
	 */
	_getAlleleFreq: function(frequency, alt, ref, tipClass)
	{
		var text = "NA";
		var total = alt + ref;
		var style = "";
		var tipStyle = "";

		if (frequency)
		{
			style = "mutation_table_allele_freq";
			text = frequency.toFixed(2);
			tipStyle = tipClass;
		}

		return {text: text, total: total, style: style, tipClass: tipStyle};
	},
	_getPdbMatchId: function(mutation)
	{
		if (mutation.pdbMatch)
		{
			return mutation.mutationId;
		}
		else
		{
			return "";
		}
	},
	_getProteinChange: function(mutation)
	{
		var style = "mutation-table-protein-change";
		var tip = "click to highlight the position on the diagram";

		// TODO disabled temporarily, enable when isoform support completely ready
//        if (!mutation.canonicalTranscript)
//        {
//            style = "best_effect_transcript " + style;
//            // TODO find a better way to display isoform information
//            tip = "Specified protein change is for the best effect transcript " +
//                "instead of the canonical transcript.<br>" +
//                "<br>RefSeq mRNA id: " + "<b>" + mutation.refseqMrnaId + "</b>" +
//                "<br>Codon change: " + "<b>" + mutation.codonChange + "</b>" +
//                "<br>Uniprot id: " + "<b>" + mutation.uniprotId + "</b>";
//        }

		return {text: mutation.proteinChange,
			style : style,
			tip: tip};
	},
	_getTumorType: function(mutation)
	{
		var style = "tumor_type";
		var tip = "";

		return {text: mutation.tumorType,
			style : style,
			tip: tip};
	},
	/**
	 * Returns the css class and text for the given cosmic count.
	 *
	 * @param count number of occurrences
	 * @return {{style: string, count: string}}
	 * @private
	 */
	_getCosmic: function(count)
	{
		var style = "";
		var text = "";

		if (count > 0)
		{
			style = "mutation_table_cosmic";
			text = count;
		}

		return {style: style,
			count: text};
    },
	/**
	 * Returns the text and css class values for the given integer value.
	 *
	 * @param value an integer value
	 * @return {{text: *, style: string}}
	 * @private
	 */
	_getIntValue: function(value)
	{
		var text = value;
		var style = "mutation_table_int_value";

		if (value == null)
		{
			text = "NA";
			style = "";
		}

		return {text: text, style: style};
	},
	/**
	 * Returns the text and css class values for the given allele count value.
	 *
	 * @param count an integer value
	 * @return {{text: *, style: string}}
	 * @private
	 */
	_getAlleleCount: function(count)
	{
		var text = count;
		var style = "mutation_table_allele_count";

		if (count == null)
		{
			text = "NA";
			style = "";
		}

		return {text: text, style: style};
    }
});
