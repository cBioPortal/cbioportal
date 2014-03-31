/**
 * Constructor for the MutationDetailsTable class.
 *
 * @param options       visual options object
 * @param headers       array of header names
 * @param gene          hugo gene symbol
 * @param mutationUtil  mutation details util
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationDetailsTable(options, gene, mutationUtil)
{
	// default options object
	var _defaultOpts = {
		el: "#mutation_details_table_d3",
		//elWidth: 740, // width of the container
		// default column header information
		//
		// name: internal name used to define column specific properties
		// display: display value
		// tip: tooltip value
		headers: {
			datum: {display: "", // never display datum...
				tip: ""},
			mutationId: {display: "Mutation ID",
				tip: "Mutation ID"},
			caseId: {display: "Case ID",
				tip:"Case ID"},
			cancerStudy: {display: "Cancer Study",
				tip:"Cancer Study"},
			tumorType: {display: "Tumor Type",
				tip:"Tumor Type"},
			proteinChange: {display: "AA change",
				tip:"Protein Change"},
			mutationType: {display: "Type",
				tip:"Mutation Type"},
			cna: {display: "Copy #",
				tip:"Copy-number status of the mutated gene"},
			cosmic: {display: "COSMIC",
				tip:"Overlapping mutations in COSMIC"},
			mutationStatus: {display: "MS",
				tip:"Mutation Status"},
			validationStatus: {display: "VS",
				tip:"Validation Status"},
			mutationAssessor: {display: "Mutation Assessor",
				tip:"Predicted Functional Impact Score (via Mutation Assessor) for missense mutations"},
			sequencingCenter: {display: "Center",
				tip:"Sequencing Center"},
			chr: {display: "Chr",
				tip:"Chromosome"},
			startPos: {display: "Start Pos",
				tip:"Start Position"},
			endPos: {display: "End Pos",
				tip:"End Position"},
			referenceAllele: {display: "Ref",
				tip:"Reference Allele"},
			variantAllele: {display: "Var",
				tip:"Variant Allele"},
			tumorFreq: {display: "Allele Freq (T)",
				tip:"Variant allele frequency<br> in the tumor sample"},
			normalFreq: {display: "Allele Freq (N)",
				tip:"Variant allele frequency<br> in the normal sample"},
			tumorRefCount: {display: "Var Ref",
				tip:"Variant Ref Count"},
			tumorAltCount: {display: "Var Alt",
				tip:"Variant Alt Count"},
			normalRefCount: {display: "Norm Ref",
				tip:"Normal Ref Count"},
			normalAltCount: {display: "Norm Alt",
				tip:"Normal Alt Count"},
			igvLink: {display: "BAM",
				tip:"Link to BAM file"},
			mutationCount: {display: "#Mut in Sample",
				tip:"Total number of<br> nonsynonymous mutations<br> in the sample"}
		},
		columnOrder: ["datum", "mutationId", "caseId", "cancerStudy", "tumorType",
			"proteinChange", "mutationType", "cna", "cosmic", "mutationStatus",
			"validationStatus", "mutationAssessor", "sequencingCenter", "chr",
			"startPos", "endPos", "referenceAllele", "variantAllele", "tumorFreq",
			"normalFreq", "tumorRefCount", "tumorAltCount", "normalRefCount",
			"normalAltCount", "igvLink", "mutationCount"],
		// Indicates the visibility of columns
		//
		// - Valid string constants:
		// "visible": column will be visible initially
		// "hidden":  column will be hidden initially,
		// but user can unhide the column via show/hide option
		// "excluded": column will be hidden initially,
		// and the user cannot unhide the column via show/hide option
		//
		// - Custom function: It is also possible to set a custom function
		// to determine the visibility of a column. A custom function
		// should return one of the valid string constants defined above.
		// For any unknown visibility value, column will be hidden by default.
		//
		// All other columns will be initially hidden by default.
		columnVisibility: {
			"datum": "excluded",
			"proteinChange": "visible",
			"caseId": "visible",
			"mutationType": "visible",
			"cosmic": "visible",
			"mutationAssessor": "visible",
			"mutationCount": "visible",
			"mutationId": "excluded",
			"cancerStudy": "excluded",
			// TODO we may need more parameters than these two (util, gene)
			"cna" : function (util, gene) {
				if (util.containsCnaData(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"tumorFreq": function (util, gene) {
				if (util.containsAlleleFreqT(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"igvLink": function (util, gene) {
				if (util.containsIgvLink(gene)) {
					return "visible";
				}
				else {
					return "excluded";
				}
			},
			"mutationStatus": function (util, gene) {
				if (util.containsGermline(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"validationStatus": function (util, gene) {
				if (util.containsValidStatus(gene)) {
					return "visible";
				}
				else {
					return "hidden";
				}
			},
			"tumorType": function (util, gene) {
				var count = util.distinctTumorTypeCount(gene);

				if (count > 1) {
					return "visible";
				}
				else if (count > 0) {
					return "hidden";
				}
				else { // if (count <= 0)
					return "excluded";
				}
			}
		},
		// Indicates whether a column is searchable or not.
		// Should be a boolean value or a function.
		//
		// All other columns will be initially non-searchable by default.
		columnSearch: {
			"caseId": true,
			"mutationId": true,
			"cancerStudy": true,
			"proteinChange": true,
			"tumorType": true,
			"mutationType": true
		},
		// custom width values for columns
		columnWidth: {
			"mutationAssessor": "2%",
			"mutationCount": "2%"
		},
		columnRender: {
			"mutationId": function(obj, mutation) {
				// TODO define 2 separate columns?
				return (mutation.mutationId + "-" + mutation.mutationSid);
			},
			"caseId": function(obj, mutation) {
				var caseIdFormat = MutationDetailsTableFormatter.getCaseId(mutation.caseId);
				var vars = {};
				vars.linkToPatientView = mutation.linkToPatientView;
				vars.caseId = caseIdFormat.text;
				vars.caseIdClass = caseIdFormat.style;
				vars.caseIdTip = caseIdFormat.tip;

				return _.template(
					$("#mutation_table_case_id_template").html(), vars);
			},
			"proteinChange": function(obj, mutation) {
				var proteinChange = MutationDetailsTableFormatter.getProteinChange(mutation);
				var vars = {};
				vars.proteinChange = proteinChange.text;
				vars.proteinChangeClass = proteinChange.style;
				vars.proteinChangeTip = proteinChange.tip;
				vars.pdbMatchId = MutationDetailsTableFormatter.getPdbMatchId(mutation);

				return _.template(
					$("#mutation_table_protein_change_template").html(), vars);
			},
			"cancerStudy": function(obj, mutation) {
				var vars = {};
				//vars.cancerType = mutation.cancerType;
				vars.cancerStudy = mutation.cancerStudy;
				vars.cancerStudyShort = mutation.cancerStudyShort;
				vars.cancerStudyLink = mutation.cancerStudyLink;

				return _.template(
					$("#mutation_table_cancer_study_template").html(), vars);
			},
			"tumorType": function(obj, mutation) {
				var tumorType = MutationDetailsTableFormatter.getTumorType(mutation);
				var vars = {};
				vars.tumorType = tumorType.text;
				vars.tumorTypeClass = tumorType.style;
				vars.tumorTypeTip = tumorType.tip;

				return _.template(
					$("#mutation_table_tumor_type_template").html(), vars);
			},
			"mutationType": function(obj, mutation) {
				var mutationType = MutationDetailsTableFormatter.getMutationType(mutation.mutationType);
				var vars = {};
				vars.mutationTypeClass = mutationType.style;
				vars.mutationTypeText = mutationType.text;

				return _.template(
					$("#mutation_table_mutation_type_template").html(), vars);
			},
			"cosmic": function(obj, mutation) {
				var cosmic = MutationDetailsTableFormatter.getCosmic(mutation.cosmicCount);
				var vars = {};
				vars.cosmicClass = cosmic.style;
				vars.cosmicCount = cosmic.count;
				vars.mutationId = mutation.mutationId;

				return _.template(
					$("#mutation_table_cosmic_template").html(), vars);
			},
			"cna": function(obj, mutation) {
				var cna = MutationDetailsTableFormatter.getCNA(mutation.cna);
				var vars = {};
				vars.cna = cna.text;
				vars.cnaClass = cna.style;
				vars.cnaTip = cna.tip;

				return _.template(
					$("#mutation_table_cna_template").html(), vars);
			},
			"mutationCount": function(obj, mutation) {
				var mutationCount = MutationDetailsTableFormatter.getIntValue(mutation.mutationCount);
				var vars = {};
				vars.mutationCount = mutationCount.text;
				vars.mutationCountClass = mutationCount.style;

				return _.template(
					$("#mutation_table_mutation_count_template").html(), vars);
			},
			"normalFreq": function(obj, mutation) {
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.normalAltCount);
				var normalFreq = MutationDetailsTableFormatter.getAlleleFreq(mutation.normalFreq,
					mutation.normalAltCount,
					mutation.normalRefCount,
					"simple-tip-left");
				var vars = {};
				vars.normalFreq = normalFreq.text;
				vars.normalFreqClass = normalFreq.style;
				vars.normalFreqTipClass = normalFreq.tipClass;
				vars.normalTotalCount = normalFreq.total;
				vars.normalAltCount = alleleCount.text;

				return _.template(
					$("#mutation_table_normal_freq_template").html(), vars);
			},
			"tumorFreq": function(obj, mutation) {
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.tumorAltCount);
				var tumorFreq = MutationDetailsTableFormatter.getAlleleFreq(mutation.tumorFreq,
					mutation.tumorAltCount,
					mutation.tumorRefCount,
					"simple-tip-left");
				var vars = {};
				vars.tumorFreq = tumorFreq.text;
				vars.tumorFreqClass = tumorFreq.style;
				vars.tumorFreqTipClass = tumorFreq.tipClass;
				vars.tumorTotalCount = tumorFreq.total;
				vars.tumorAltCount = alleleCount.text;

				return _.template(
					$("#mutation_table_tumor_freq_template").html(), vars);
			},
			"mutationAssessor": function(obj, mutation) {
				var fis = MutationDetailsTableFormatter.getFis(mutation.functionalImpactScore, mutation.fisValue);
				var vars = {};
				vars.fisClass = fis.fisClass;
				vars.omaClass = fis.omaClass;
				vars.fisValue = fis.value;
				vars.fisText = fis.text;
				vars.mutationId = mutation.mutationId;

				return _.template(
					$("#mutation_table_mutation_assessor_template").html(), vars);
			},
			"mutationStatus": function(obj, mutation) {
				var mutationStatus = MutationDetailsTableFormatter.getMutationStatus(mutation.mutationStatus);
				var vars = {};
				vars.mutationStatusTip = mutationStatus.tip;
				vars.mutationStatusClass = mutationStatus.style;
				vars.mutationStatusText = mutationStatus.text;

				return _.template(
					$("#mutation_table_mutation_status_template").html(), vars);
			},
			"validationStatus": function(obj, mutation) {
				var validationStatus = MutationDetailsTableFormatter.getValidationStatus(mutation.validationStatus);
				var vars = {};
				vars.validationStatusTip = validationStatus.tip;
				vars.validationStatusClass = validationStatus.style;
				vars.validationStatusText = validationStatus.text;

				return _.template(
					$("#mutation_table_validation_status_template").html(), vars);
			},
			"normalRefCount": function(obj, mutation) {
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.normalRefCount);
				var vars = {};
				vars.normalRefCount = alleleCount.text;
				vars.normalRefCountClass = alleleCount.style;

				return _.template(
					$("#mutation_table_normal_ref_count_template").html(), vars);
			},
			"normalAltCount": function(obj, mutation) {
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.normalAltCount);
				var vars = {};
				vars.normalAltCount = alleleCount.text;
				vars.normalAltCountClass = alleleCount.style;

				return _.template(
					$("#mutation_table_normal_alt_count_template").html(), vars);
			},
			"tumorRefCount": function(obj, mutation) {
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.tumorRefCount);
				var vars = {};
				vars.tumorRefCount = alleleCount.text;
				vars.tumorRefCountClass = alleleCount.style;

				return _.template(
					$("#mutation_table_tumor_ref_count_template").html(), vars);
			},
			"tumorAltCount": function(obj, mutation) {
				var alleleCount = MutationDetailsTableFormatter.getAlleleCount(mutation.tumorAltCount);
				var vars = {};
				vars.tumorAltCount = alleleCount.text;
				vars.tumorAltCountClass = alleleCount.style;

				return _.template(
					$("#mutation_table_tumor_alt_count_template").html(), vars);
			},
			"startPos": function(obj, mutation) {
				var startPos = MutationDetailsTableFormatter.getIntValue(mutation.startPos);
				var vars = {};
				vars.startPos = startPos.text;
				vars.startPosClass = startPos.style;

				return _.template(
					$("#mutation_table_start_pos_template").html(), vars);
			},
			"endPos": function(obj, mutation) {
				var endPos = MutationDetailsTableFormatter.getIntValue(mutation.endPos);
				var vars = {};
				vars.endPos = endPos.text;
				vars.endPosClass = endPos.style;

				return _.template(
					$("#mutation_table_end_pos_template").html(), vars);
			},
			"sequencingCenter": function(obj, mutation) {
				return mutation.sequencingCenter;
			},
			"chr": function(obj, mutation) {
				return mutation.chr;
			},
			"referenceAllele": function(obj, mutation) {
				return mutation.referenceAllele;
			},
			"variantAllele": function(obj, mutation) {
				return mutation.variantAllele;
			},
			"igvLink": function(obj, mutation) {
				//vars.xVarLink = mutation.xVarLink;
				//vars.msaLink = mutation.msaLink;
				//vars.igvLink = mutation.igvLink;
				return mutation.igvLink;
			}
		},
		// WARNING: overwriting advanced DataTables options such as
		// aoColumnDefs, oColVis, and fnDrawCallback may break column
		// visibility, sorting, and filtering. Proceed wisely ;)
		dataTableOpts: {
			"sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
			"bJQueryUI": true,
			"bPaginate": false,
			"bFilter": true,
			"sScrollY": "600px",
			"bScrollCollapse": true,
			"oLanguage": {
				"sInfo": "Showing _TOTAL_ mutation(s)",
				"sInfoFiltered": "(out of _MAX_ total mutations)",
				"sInfoEmpty": "No mutations to show"
			}
		}
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// custom event dispatcher
	var _dispatcher = {};
	_.extend(_dispatcher, Backbone.Events);

	// reference to the data table object
	var _dataTable = null;

	// flag used to switch events on/off
	var _eventActive = true;

	// this is used to check if search string is changed after each redraw
	var _prevSearch = "";

	// last search string manually entered by the user
	var _manualSearch = "";

	var _rowMap = {};

	var _selectedRow = null;

	/**
	 * Initializes the data tables plug-in for the given table selector.
	 *
	 * @param tableSelector jQuery selector for the target table
	 * @param rows          data rows
	 * @param headers       column headers
	 * @param indexMap      map of <column name, column index>
	 * @param hiddenCols    indices of the hidden columns
	 * @param excludedCols  indices of the excluded columns
	 * @param nonSearchableCols    indices of the columns excluded from search
	 * @return {object}     DataTable instance
	 * @private
	 */
	function initDataTable(tableSelector, rows, headers,
		indexMap, hiddenCols, excludedCols, nonSearchableCols)
	{
		// set column options
		var columns = DataTableUtil.getColumnOptions(headers,
			_options.columnWidth);

		// these are the parametric data tables options
		var tableOpts = {
			"aaData" : rows,
			"aoColumns" : columns,
			"aoColumnDefs":[
				{"sType": 'aa-change-col',
					"aTargets": [ indexMap["proteinChange"] ]},
				{"sType": 'label-int-col',
					"sClass": "right-align-td",
					"aTargets": [indexMap["cosmic"],
						indexMap["startPos"],
						indexMap["endPos"],
						indexMap["tumorAltCount"],
						indexMap["tumorRefCount"],
						indexMap["normalAltCount"],
						indexMap["normalRefCount"],
						indexMap["mutationCount"]]},
				{"sType": 'string',
					"sClass": "center-align-td",
					"aTargets": [indexMap["validationStatus"],
						indexMap["mutationStatus"],
						indexMap["mutationType"],
						indexMap["sequencingCenter"]]},
				{"sType": 'label-float-col',
					"sClass": "right-align-td",
					"aTargets": [indexMap["tumorFreq"],
						indexMap["normalFreq"]]},
				{"sType": 'predicted-impact-col',
					"sClass": "center-align-td",
					"aTargets": [indexMap["mutationAssessor"]]},
				{"sType": 'copy-number-col',
					"sClass": "center-align-td",
					"aTargets": [indexMap["cna"]]},
				{"asSorting": ["desc", "asc"],
					"aTargets": [indexMap["cosmic"],
						indexMap["mutationAssessor"],
						indexMap["mutationCount"]]},
				{"bVisible": false,
					"aTargets": hiddenCols},
				{"bSearchable": false,
					"aTargets": nonSearchableCols}
				// TODO may need to define sort targets as well
				//{"iDataSort": indexMap["uniprot from"],
				//	"aTargets": [indexMap["uniprot positions"]]}
			],
			"oColVis": {"aiExclude": excludedCols}, // columns to always hide
			"fnDrawCallback": function(oSettings) {
				addMutationTableTooltips();

				var currSearch = oSettings.oPreviousSearch.sSearch;

				// trigger the event only if the corresponding flag is set
				// and there is a change in the search term
				if (_eventActive &&
				    _prevSearch != currSearch)
				{
					// trigger corresponding event
					_dispatcher.trigger(
						MutationDetailsEvents.MUTATION_TABLE_FILTERED,
						tableSelector);

					// assuming events are active for only manual filtering
					// so update manual search string only after triggering the event
					_manualSearch = currSearch;
				}

				// update prev search string reference for future use
				_prevSearch = currSearch;
			},
			"fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
				var mutation = aData[indexMap["datum"]];
				// TODO mapping on mutationId and mutationSid...
				//var key = mutation.mutationId;
				//_rowMap[key] = nRow;
				$(nRow).attr("id", mutation.mutationId);
				$(nRow).addClass(mutation.mutationSid);
			},
			"fnInitComplete": function(oSettings, json) {
				// remove invalid links
				$(tableSelector).find('a[href=""]').remove();
				$(tableSelector).find('a[alt=""]').remove();

				// TODO append the footer (there is no API to init the footer, we need a custom function)
				//$(tableSelector).append('<tfoot></tfoot>');
				//$(tableSelector).find('thead tr').clone().appendTo($(tableSelector).find('tfoot'));

//				// trigger corresponding event
//				_dispatcher.trigger(
//					MutationDetailsEvents.MUTATION_TABLE_READY);
			},
			"fnHeaderCallback": function(nHead, aData, iStart, iEnd, aiDisplay) {
			    $(nHead).find('th').addClass("mutation-details-table-header");
				addHeaderTooltips(nHead);
		    },
		    "fnFooterCallback": function(nFoot, aData, iStart, iEnd, aiDisplay) {
			    addFooterTooltips(nFoot);
		    }
		};

		// also add column renderers
		var renderers = DataTableUtil.getColumnRenderers(_options.columnRender, indexMap);
		tableOpts.aoColumnDefs = tableOpts.aoColumnDefs.concat(renderers);

		// merge with the one in the main options object
		tableOpts = jQuery.extend(true, {}, _defaultOpts.dataTableOpts, tableOpts);

		// format the table with the dataTable plugin and return the table instance
		return tableSelector.dataTable(tableOpts);
	}

	/**
	 * Determines the visibility value for the given column name
	 *
	 * @param columnName    name of the column (header)
	 * @return {String}     visibility value for the given column
	 */
	function visibilityValue(columnName)
	{
		var vis = _options.columnVisibility[columnName];
		var value = vis;

		// if not in the list, hidden by default
		if (!vis)
		{
			value = "hidden";
		}
		// if function, then evaluate the value
		else if (_.isFunction(vis))
		{
			value = vis(mutationUtil, gene);
		}

		return value;
	}

	function searchValue(columnName)
	{
		var searchVal = _options.columnSearch[columnName];
		var value = searchVal;

		// if not in the list, hidden by default
		if (searchVal == null)
		{
			value = false;
		}
		// if function, then evaluate the value
		else if (_.isFunction(searchVal))
		{
			// TODO determine function params (if needed)
			value = searchVal();
		}

		return value;
	}

	/**
	 * Formats the table with data tables plugin for the given
	 * row data array (each element represents a single row).
	 *
	 * @rows    row data as an array
	 */
	function renderTable(rows)
	{
		var columns = _options.columnOrder;

		// build a map, to be able to use string constants
		// instead of integer constants for table columns
		var indexMap = DataTableUtil.buildColumnIndexMap(columns);

		// build a visibility map for column headers
		var visibilityMap = DataTableUtil.buildColumnVisMap(columns, visibilityValue);

		// build a map to determine searchable columns
		var searchMap = DataTableUtil.buildColumnSearchMap(columns, searchValue);

		// determine hidden and excluded columns
		var hiddenCols = DataTableUtil.getHiddenColumns(columns, indexMap, visibilityMap);
		var excludedCols = DataTableUtil.getExcludedColumns(columns, indexMap, visibilityMap);

		// determine columns to exclude from filtering (through the search box)
		var nonSearchableCols = DataTableUtil.getNonSearchableColumns(columns, indexMap, searchMap);

		// add custom sort functions for specific columns
		addSortFunctions();

		// actual initialization of the DataTables plug-in
		_dataTable = initDataTable($(_options.el), rows, _options.headers,
		                           indexMap, hiddenCols, excludedCols, nonSearchableCols);

		//_dataTable.css("width", "100%");

		addDefaultListeners(indexMap, _dataTable);

		// add a delay to the filter
		_dataTable.fnSetFilteringDelay(600);
	}

	/**
	 * Adds default event listeners for the table.
	 *
	 * @param indexMap  column index map
	 */
	function addDefaultListeners(indexMap)
	{
		// add resize listener to the window to adjust column sizing
		$(window).bind('resize', function () {
			if (_dataTable.is(":visible"))
			{
				_dataTable.fnAdjustColumnSizing();
			}
		});

		// add click listener for each igv link to get the actual parameters
		// from another servlet
		_.each($(_options.el).find('.igv-link'), function(element, index) {
			// TODO use mutation id, and dispatch an event
			var url = $(element).attr("alt");

			$(element).click(function(evt) {
				// get parameters from the server and call related igv function
				$.getJSON(url, function(data) {
					//console.log(data);
					// TODO this call displays warning message (resend)
					prepIGVLaunch(data.bamFileUrl,
					              data.encodedLocus,
					              data.referenceGenome,
					              data.trackName);
				});
			});
		});

		// add click listener for each 3D link
		$(_options.el).find('.mutation-table-3d-link').click(function(evt) {
			evt.preventDefault();

			var mutationId = $(this).attr("alt");

			_dispatcher.trigger(
				MutationDetailsEvents.PDB_LINK_CLICKED,
				mutationId);
		});

		// add click listener for each 3D link
		$(_options.el).find('.mutation-table-protein-change a').click(function(evt) {
			evt.preventDefault();

			var mutationId = $(this).closest("tr").attr("id");

			_dispatcher.trigger(
				MutationDetailsEvents.PROTEIN_CHANGE_LINK_CLICKED,
				mutationId);
		});
	}

	function selectRow(pdbId, chainId)
	{
		var key = PdbDataUtil.chainKey(pdbId, chainId);

		// remove previous highlights
		removeAllSelection();

		// highlight selected
		var nRow = _rowMap[key];
		$(nRow).addClass("row_selected");

		_selectedRow = nRow;
	}

	function removeAllSelection()
	{
		$(_options.el).find("tr").removeClass("row_selected");
	}

	function getSelectedRow()
	{
		return _selectedRow;
	}

	/**
	 * Enables/disables event triggering.
	 *
	 * @param active    boolean value
	 */
	function setEventActive(active)
	{
		_eventActive = active;
	}

	/**
	 * Resets filtering related variables to their initial state.
	 * Does not remove actual table filters.
	 */
	function cleanFilters()
	{
		_prevSearch = "";
		_manualSearch = "";
	}

//	function cleanFilters()
//	{
//		// just show everything
//		_dataTable.fnFilter("");
//	}

	function getManualSearch()
	{
		return _manualSearch;
	}

	function getDataTable()
	{
		return _dataTable;
	}

	function getHeaders()
	{
		return _options.headers;
	}

	function addMutationTableTooltips()
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

	    var qTipOptionsLeft = {};
	    jQuery.extend(true, qTipOptionsLeft, qTipOptions);
	    qTipOptionsLeft.position = {my:'top right', at:'bottom left'};

		var tableSelector = $(_options.el);

	    //$('#mutation_details .mutation_details_table td').qtip(qTipOptions);

		tableSelector.find('.simple-tip').qtip(qTipOptions);
		tableSelector.find('.best_effect_transcript').qtip(qTipOptions);
        tableSelector.find('.cc-short-study-name').qtip(qTipOptions);
        tableSelector.find('.simple-tip-left').qtip(qTipOptionsLeft);

		// add tooltip for COSMIC value
		tableSelector.find('.mutation_table_cosmic').each(function() {
			var label = this;
			var mutationId = $(label).attr('alt');
			var mutation = mutationUtil.getMutationIdMap()[mutationId];

			// copy default qTip options and modify "content" to customize for cosmic
			var qTipOptsCosmic = {};
			jQuery.extend(true, qTipOptsCosmic, qTipOptions);

			qTipOptsCosmic.content = {text: "NA"}; // content is overwritten on render
			qTipOptsCosmic.events = {render: function(event, api) {
				var model = {cosmic: mutation.cosmic,
					keyword: mutation.keyword,
					geneSymbol: gene,
					total: $(label).text()};

				var container = $(this).find('.qtip-content');

				// create & render cosmic tip view
				var cosmicView = new CosmicTipView({el: container, model: model});
				cosmicView.render();
			}};

			$(label).qtip(qTipOptsCosmic);
		});

		// add tooltip for Predicted Impact Score (FIS)
		tableSelector.find('.oma_link').each(function() {
			var links = $(this).attr('alt');
			var parts = links.split("|");

			var mutationId = parts[1];
			var mutation = mutationUtil.getMutationIdMap()[mutationId];

			// copy default qTip options and modify "content"
			// to customize for predicted impact score
			var qTipOptsOma = {};
			jQuery.extend(true, qTipOptsOma, qTipOptionsLeft);

			qTipOptsOma.content = {text: "NA"}; // content is overwritten on render
			qTipOptsOma.events = {render: function(event, api) {
				var model = {impact: parts[0],
					xvia: mutation.xVarLink,
					msaLink: mutation.msaLink,
					pdbLink: mutation.pdbLink};

				var container = $(this).find('.qtip-content');

				// create & render FIS tip view
				var fisTipView = new PredictedImpactTipView({el:container, model: model});
				fisTipView.render();
			}};

			$(this).qtip(qTipOptsOma);
		});
	}

	/**
	 * Adds tooltips for the table header cells.
	 *
	 * @param nHead table header
	 * @private
	 */
	function addHeaderTooltips(nHead)
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

		var qTipOptionsHeader = {};
		jQuery.extend(true, qTipOptionsHeader, qTipOptions);
		qTipOptionsHeader.position = {my:'bottom center', at:'top center'};

		//tableSelector.find('thead th').qtip(qTipOptionsHeader);
		$(nHead).find("th").qtip(qTipOptionsHeader);
	}

	/**
	 * Adds tooltips for the table footer cells.
	 *
	 * @param nFoot table footer
	 * @private
	 */
	function addFooterTooltips(nFoot)
	{
		var qTipOptions = MutationViewsUtil.defaultTableTooltipOpts();

		var qTipOptionsFooter = {};
		jQuery.extend(true, qTipOptionsFooter, qTipOptions);
		qTipOptionsFooter.position = {my:'top center', at:'bottom center'};

		//tableSelector.find('tfoot th').qtip(qTipOptionsFooter);
		$(nFoot).find("th").qtip(qTipOptionsFooter);
	}

	/**
	 * Helper function for predicted impact score sorting.
	 */
	function _assignValueToPredictedImpact(text, score)
	{
		// using score by itself may be sufficient,
		// but sometimes we have no numerical score value

		var value;
		text = text.toLowerCase();

		if (text == "low" || text == "l") {
			value = 2;
		} else if (text == "medium" || text == "m") {
			value = 3;
		} else if (text == "high" || text == "h") {
			value = 4;
		} else if (text == "neutral" || text == "n") {
			value = 1;
		} else {
			value = -1;
		}

		if (value > 0 && !isNaN(score))
		{
			//assuming FIS values cannot exceed 1000
			value += score / 1000;
		}

		return value;
	}

	function _assignValueToCna(text)
	{
		var value;
		text = text.toLowerCase();

		// TODO this is actually reverse mapping of MutationDetailsUtil._cnaMap
		if (text == "homdel") {
			value = 1;
		} else if (text == "hetloss") {
			value = 2;
		} else if (text == "diploid") {
			value = 3;
		} else if (text == "gain") {
			value = 4;
		} else if (text == "amp") {
			value = 5;
		} else { // unknown
			value = -1;
		}

		return value;
	}

	function _getAltTextValue(a)
	{
		var altValue = $(a).attr("alt");
		var value = parseFloat(altValue);

		if (isNaN(value))
		{
			value = "";
		}

		return value;
	}

	/**
	 * Helper function for predicted impact score sorting.
	 * Gets the score from the "alt" property within the given html string.
	 */
	function _getFisValue(a)
	{
		var score = "";
		var altValue = $(a).attr("alt");

		var parts = altValue.split("|");

		if (parts.length > 0)
		{
			if (parts[0].length > 0)
			{
				score = parseFloat(parts[0]);
			}
		}

		return score;
	}

	/**
	 * Helper function for sorting string values within label tag.
	 */
	function _getLabelTextValue(a)
	{
		if (a.indexOf("label") != -1)
		{
			// TODO temp workaround
			return $(a).find("label").text().trim() || $(a).text().trim();
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Helper function for sorting int values within label tag.
	 */
	function _getLabelTextIntValue(a)
	{
		if (a.indexOf("label") != -1)
		{
			return parseInt($(a).text());
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Helper function for sorting float values within label tag.
	 */
	function _getLabelTextFloatValue(a)
	{
		if (a.indexOf("label") != -1)
		{
			return parseFloat($(a).text());
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Comparison function for ascending sort operations.
	 *
	 * @param a
	 * @param b
	 * @param av
	 * @param bv
	 * @return
	 * @private
	 */
	function _compareSortAsc(a, b, av, bv)
	{
		if (av >= 0) {
			if (bv >= 0) {
				return av==bv ? 0 : (av<bv ? -1:1);
			} else {
				return -1;
			}
		} else {
			if (bv >= 0) {
				return 1;
			} else {
				return a==b ? 0 : (a<b ? 1:-1);
			}
		}
	}

	/**
	 * Comparison function for descending sort operations.
	 *
	 * @param a
	 * @param b
	 * @param av
	 * @param bv
	 * @return
	 * @private
	 */
	function _compareSortDesc(a, b, av, bv)
	{
		if (av >= 0) {
			if (bv >= 0) {
				return av==bv ? 0 : (av<bv ? 1:-1);
			} else {
				return -1;
			}
		} else {
			if (bv >= 0) {
				return 1;
			} else {
				return a==b ? 0 : (a<b ? -1:1);
			}
		}
	}

	/**
	 * Adds custom DataTables sort function for specific columns.
	 *
	 * @private
	 */
	function addSortFunctions()
	{
		/**
		 * Ascending sort function for protein (amino acid) change column.
		 */
		jQuery.fn.dataTableExt.oSort['aa-change-col-asc'] = function(a,b) {
			var ares = a.match(/.*[A-Z]([0-9]+)[^0-9]+/);
			var bres = b.match(/.*[A-Z]([0-9]+)[^0-9]+/);

			if (ares) {
				if (bres) {
					var ia = parseInt(ares[1]);
					var ib = parseInt(bres[1]);
					return ia==ib ? 0 : (ia<ib ? -1:1);
				} else {
					return -1;
				}
			} else {
				if (bres) {
					return 1;
				} else {
					return a==b ? 0 : (a<b ? -1:1);
				}
			}
		};

		/**
		 * Descending sort function for protein (amino acid) change column.
		 */
		jQuery.fn.dataTableExt.oSort['aa-change-col-desc'] = function(a,b) {
			var ares = a.match(/.*[A-Z]([0-9]+)[^0-9]+/);
			var bres = b.match(/.*[A-Z]([0-9]+)[^0-9]+/);

			if (ares) {
				if (bres) {
					var ia = parseInt(ares[1]);
					var ib = parseInt(bres[1]);
					return ia==ib ? 0 : (ia<ib ? 1:-1);
				} else {
					return -1;
				}
			} else {
				if (bres) {
					return 1;
				} else {
					return a==b ? 0 : (a<b ? 1:-1);
				}
			}
		};

		/**
		 * Ascending sort function for the copy number column.
		 */
		jQuery.fn.dataTableExt.oSort['copy-number-col-asc']  = function(a,b) {
			var av = _assignValueToCna(_getLabelTextValue(a));
			var bv = _assignValueToCna(_getLabelTextValue(b));

			return _compareSortAsc(a, b, av, bv);
		};

		/**
		 * Descending sort function for the copy number column.
		 */
		jQuery.fn.dataTableExt.oSort['copy-number-col-desc']  = function(a,b) {
			var av = _assignValueToCna(_getLabelTextValue(a));
			var bv = _assignValueToCna(_getLabelTextValue(b));

			return _compareSortDesc(a, b, av, bv);
		};

		/**
		 * Ascending sort function for predicted impact column.
		 */
		jQuery.fn.dataTableExt.oSort['predicted-impact-col-asc']  = function(a,b) {
			var av = _assignValueToPredictedImpact(_getLabelTextValue(a), _getFisValue(a));
			var bv = _assignValueToPredictedImpact(_getLabelTextValue(b), _getFisValue(b));

			return _compareSortAsc(a, b, av, bv);
		};

		/**
		 * Descending sort function for predicted impact column.
		 */
		jQuery.fn.dataTableExt.oSort['predicted-impact-col-desc']  = function(a,b) {
			var av = _assignValueToPredictedImpact(_getLabelTextValue(a), _getFisValue(a));
			var bv = _assignValueToPredictedImpact(_getLabelTextValue(b), _getFisValue(b));

			return _compareSortDesc(a, b, av, bv);
		};

		/**
		 * Ascending sort function for columns having int within label tag.
		 */
		jQuery.fn.dataTableExt.oSort['label-int-col-asc'] = function(a,b) {
			var av = _getLabelTextIntValue(a);
			var bv = _getLabelTextIntValue(b);

			return _compareSortAsc(a, b, av, bv);
		};

		/**
		 * Descending sort function for columns having int within label tag.
		 */
		jQuery.fn.dataTableExt.oSort['label-int-col-desc'] = function(a,b) {
			var av = _getLabelTextIntValue(a);
			var bv = _getLabelTextIntValue(b);

			return _compareSortDesc(a, b, av, bv);
		};

		/**
		 * Ascending sort function for columns having float within label tag.
		 */
		jQuery.fn.dataTableExt.oSort['label-float-col-asc'] = function(a,b) {
			var av = _getLabelTextFloatValue(a);
			var bv = _getLabelTextFloatValue(b);

			return _compareSortAsc(a, b, av, bv);
		};

		/**
		 * Descending sort function for columns having float within label tag.
		 */
		jQuery.fn.dataTableExt.oSort['label-float-col-desc'] = function(a,b) {
			var av = _getLabelTextFloatValue(a);
			var bv = _getLabelTextFloatValue(b);

			return _compareSortDesc(a, b, av, bv);
		};
	}

	return {
		renderTable: renderTable,
		selectRow: selectRow,
		cleanFilters: cleanFilters,
		getSelectedRow: getSelectedRow,
		getDataTable: getDataTable,
		getHeaders: getHeaders,
		setEventActive: setEventActive,
		getManualSearch: getManualSearch,
		dispatcher: _dispatcher
	};
}

