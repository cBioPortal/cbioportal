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
 * Helper function for predicted impact score sorting.
 */
function _assignValueToPredictedImpact(str)
{
    str = str.toLowerCase();

    if (str == "low" || str == "l") {
        return 2;
    } else if (str == "medium" || str == "m") {
        return 3;
    } else if (str == "high" || str == "h") {
        return 4;
    } else if (str == "neutral" || str == "n") {
        return 1;
    } else {
        return 0;
    }
}

/**
 * Ascending sort function for predicted impact column.
 */
jQuery.fn.dataTableExt.oSort['predicted-impact-col-asc']  = function(a,b) {
    var av = _assignValueToPredictedImpact(a.replace(/<[^>]*>/g,""));
    var bv = _assignValueToPredictedImpact(b.replace(/<[^>]*>/g,""));

    return _compareSortAsc(a, b, av, bv);
};

/**
 * Descending sort function for predicted impact column.
 */
jQuery.fn.dataTableExt.oSort['predicted-impact-col-desc']  = function(a,b) {
    var av = _assignValueToPredictedImpact(a.replace(/<[^>]*>/g,""));
    var bv = _assignValueToPredictedImpact(b.replace(/<[^>]*>/g,""));

    return _compareSortDesc(a, b, av, bv);
};

/**
 * Ascending sort function for COSMIC column.
 */
jQuery.fn.dataTableExt.oSort['cosmic-col-asc'] = function(a,b) {
    var av = _getCosmicTextValue(a);
    var bv = _getCosmicTextValue(b);

    return _compareSortAsc(a, b, av, bv);
};

/**
 * Descending sort function for COSMIC column.
 */
jQuery.fn.dataTableExt.oSort['cosmic-col-desc'] = function(a,b) {
    var av = _getCosmicTextValue(a);
    var bv = _getCosmicTextValue(b);

    return _compareSortDesc(a, b, av, bv);
};

/**
 * Helper function for COSMIC sorting.
 */
function _getCosmicTextValue(a)
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
    if (av>0) {
        if (bv>0) {
            return av==bv ? 0 : (av<bv ? -1:1);
        } else {
            return -1;
        }
    } else {
        if (bv>0) {
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
    if (av>0) {
        if (bv>0) {
            return av==bv ? 0 : (av<bv ? 1:-1);
        } else {
            return -1;
        }
    } else {
        if (bv>0) {
            return 1;
        } else {
            return a==b ? 0 : (a<b ? -1:1);
        }
    }
}

function delayedMutationTable(data)
{
    //TODO temporary work-around for the missing columns in the filter (issue 429)
    setTimeout(function(){drawMutationTable(data);},
               3000);
}

/**
 * Generates an HTML mutation details table for the given data.
 *
 * @param data  mutation data for a single gene
 */
function drawMutationTable(data)
{
    var divId = "mutation_table_" + data.hugoGeneSymbol.toUpperCase();
    var tableId = "mutation_details_table_" + data.hugoGeneSymbol.toUpperCase();

    // generate mutation table HTML for the provided data
    $("#" + divId).empty();
    $("#" + divId).append(_generateMutationTable(tableId, data));

//    $("#" + divId).append("<table cellpadding='0' cellspacing='0' border='0' " +
//                          "class='display mutation_details_table' " +
//                          "id='" + tableId + "'></table>");
//
//    var rows = _getMutationTableRows(data);
//    var columns = _getMutationTableColumns(data);

    // format the table with the dataTable plugin
    var oTable = $("#mutation_details #" + tableId).dataTable({
        "sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
        "bJQueryUI": true,
        "bPaginate": false,
        "bFilter": true,
//      "aaData" : rows,
//      "aoColumns" : columns,
        "aoColumnDefs":[
            {"sType": 'aa-change-col',
                "aTargets": [ 1 ]},
            {"sType": 'cosmic-col',
                "sClass": "right-align-td",
                "aTargets": [ 3 ]},
            {"sType": 'predicted-impact-col',
                "aTargets": [ 4 ]},
            {"asSorting": ["desc", "asc"],
                "aTargets": [3,4,5]}
        ],
        "fnDrawCallback": function(oSettings) {
            // add tooltips to the table
            addMutationTableTooltips(tableId);
        }
    });

    var cols = oTable.fnSettings().aoColumns.length;

    // hide special gene columns and less important columns by default
    for (var col=9; col<cols; col++)
    {
        oTable.fnSetColumnVis( col, false );
    }

	// show frequency columns by default
	oTable.fnSetColumnVis(12, true);
	oTable.fnSetColumnVis(13, true);

    oTable.css("width", "100%");
}

/**
 * Generates and HTML table with the specified id for the given data.
 *
 * @param tableId   HTML id for the table
 * @param data      mutation data for a single gene
 * @return          HTML representation of the table
 */
function _generateMutationTable(tableId, data)
{
    var i, j;
    var headers = _getMutationTableHeaders(data);
    var dataRows = _getMutationTableRows(data);

    var table = "<table cellpadding='0' cellspacing='0' border='0' " +
    "class='display mutation_details_table' " +
    "id='" + tableId + "'>";

    // column headers in table head

    table += '<thead>';

    for (i = 0; i < headers.length; i++)
    {
        table += '<th alt="' + _getMutationTableHeaderTip(headers[i]) + '"><b>' +
                 headers[i] + '</b></th>';
    }

    table += '</thead>';

    // data rows

    for (i = 0; i < dataRows.length; i++)
    {
        table += '<tr>';

        for (j = 0; j < dataRows[i].length; j++)
        {
            table += '<td>' + dataRows[i][j] + '</td>'
        }

        table += '</tr>';
    }

    // column headers in table foot

    table += '<tfoot>';

    for (i = 0; i < headers.length; i++)
    {
        table += '<th alt="' + _getMutationTableHeaderTip(headers[i]) + '"><b>' +
                 headers[i] + '</b></th>';
    }

    table += '</tfoot></table>';

    // footer message for special genes
    table += '<p><br>' + data.footerMsg + '<br>';

    return table;
}

/**
 * Returns an array of header display names for the given mutation data.
 *
 * @param data  mutation data for a single gene
 * @return      array of column header names
 */
function _getMutationTableHeaders(data)
{
    var headers = new Array();

    // default headers
    headers.push(data.header.caseId);
    headers.push(data.header.proteinChange);
    headers.push(data.header.mutationType);
    headers.push(data.header.cosmic);
    headers.push(data.header.functionalImpactScore);
    headers.push(data.header.pdbLink);
    headers.push(data.header.mutationStatus);
    headers.push(data.header.validationStatus);
    headers.push(data.header.sequencingCenter);
    headers.push(data.header.position);
    headers.push(data.header.referenceAllele);
    headers.push(data.header.variantAllele);
    headers.push(data.header.tumorFreq);
    headers.push(data.header.normalFreq);
    headers.push(data.header.tumorRefCount);
    headers.push(data.header.tumorAltCount);
    headers.push(data.header.normalRefCount);
    headers.push(data.header.normalAltCount);

    // special gene headers
    for (var i=0; i < data.header.specialGeneHeaders.length; i++)
    {
        headers.push(data.header.specialGeneHeaders[i]);
    }

    return headers;
}

/**
 * Returns the tooltip for the given display name of a table column header.
 *
 * @param header    display name of a table column header
 * @return          corresponding tooltip
 */
function _getMutationTableHeaderTip(header)
{
    var tooltipMap = {"case id" : "Case ID",
        "aa change": "Protein Change",
        "type": "Mutation Type",
        "cosmic": "Overlapping mutations in COSMIC",
        "fis": "Predicted Functional Impact Score (via Mutation Assessor) for missense mutations",
        "3d": "3-D Structure",
        "ms": "Mutation Status",
        "vs": "Validation Status",
        "center": "Sequencing Center",
        "build": "NCBI Build Number",
        "position": "Position",
        "ref": "Reference Allele",
        "var": "Variant Allele",
        "var freq": "Variant Frequency",
        "norm freq": "Normal Frequency",
        "var ref": "Variant Ref Count",
        "var alt": "Variant Alt Count",
        "norm ref": "Normal Ref Count",
        "norm alt": "Normal Alt Count"};

    return tooltipMap[header.toLowerCase()];
}

/**
 * Generates all data rows (main content) of the mutation table.
 *
 * @param data  mutation data for a single gene
 * @return      an array of row data as HTML
 */
function _getMutationTableRows(data)
{
    /**
     * Mapping between the mutation type (data) values and
     * view values. The first element of an array corresponding to a
     * data value is the display text (html), and the second one
     * is style (css).
     */
    var mutationTypeMap = {
        missense_mutation: {label: "Missense", style: "missense_mutation"},
        nonsense_mutation: {label: "Nonsense", style: "trunc_mutation"},
        frame_shift_del: {label: "Nonstop", style: "trunc_mutation"},
        frame_shift_ins: {label: "FS del", style: "trunc_mutation"},
        in_frame_ins: {label: "IF ins", style: "inframe_mutation"},
        in_frame_del: {label: "IF del", style: "inframe_mutation"},
        splice_site: {label: "Splice", style: "trunc_mutation"},
        other: {style: "other_mutation"}
    };

    /**
     * Mapping between the validation status (data) values and
     * view values. The first element of an array corresponding to a
     * data value is the display text (html), and the second one
     * is style (css).
     */
    var validationStatusMap = {
        valid: {label: "V", style: "valid", tooltip: "Valid"},
        validated: {label: "V", style: "valid", tooltip: "Valid"},
        wildtype: {label: "W", style: "wildtype", tooltip: "Wildtype"},
        unknown: {label: "U", style: "unknown", tooltip: "Unknown"},
        not_tested: {label: "U", style: "unknown", tooltip: "Unknown"},
        none: {label: "U", style: "unknown", tooltip: "Unknown"},
        na: {label: "U", style: "unknown", tooltip: "Unknown"}
    };

    /**
     * Mapping between the mutation status (data) values and
     * view values. The first element of an array corresponding to a
     * data value is the display text (html), and the second one
     * is style (css).
     */
    var mutationStatusMap = {
        somatic: {label: "S", style: "somatic", tooltip: "Somatic"},
        germline: {label: "G", style: "germline", tooltip: "Germline"},
        unknown: {label: "U", style: "unknown", tooltip: "Unknown"},
        none: {label: "U", style: "unknown", tooltip: "Unknown"},
        na: {label: "U", style: "unknown", tooltip: "Unknown"}
    };

    var omaScoreMap = {
        h: {label: "H", style: "oma_high", tooltip: "High"},
        m: {label: "M", style: "oma_medium", tooltip: "Medium"},
        l: {label: "L", style: "oma_low", tooltip: "Low"},
        n: {label: "N", style: "oma_neutral", tooltip: "Neutral"}
    };

    // inner functions used for html generation

    var getMutationTypeHtml = function(value) {
        var style, label;

        if (mutationTypeMap[value] != null)
        {
            style = mutationTypeMap[value].style;
            label = mutationTypeMap[value].label;
        }
        else
        {
            style = mutationTypeMap.other.style;
            label = value;
        }

        return '<span class="' + style + '"><label>' + label + '</label></span>';
    };

    var getMutationStatusHtml = function(value) {
        var style, label, tip;

        if (mutationStatusMap[value] != null)
        {
            style = mutationStatusMap[value].style;
            label = mutationStatusMap[value].label;
            tip = mutationStatusMap[value].tooltip;
        }
        else
        {
            style = " ";
            label = value;
        }

        return '<span alt="' + tip + '" class="simple-tip ' + style + '"><label>' +
               label + '</label></span>';
    };

    var getValidationStatusHtml = function(value) {
        var style, label, tip;

        if (validationStatusMap[value] != null)
        {
            style = validationStatusMap[value].style;
            label = validationStatusMap[value].label;
            tip = validationStatusMap[value].tooltip;
        }
        else
        {
            style = validationStatusMap.unknown.style;
            label = validationStatusMap.unknown.label;
            tip = validationStatusMap.unknown.tooltip;
        }

        return '<span alt="' + tip + '" class="simple-tip ' + style + '"><label>' +
               label + '</label></span>';
    };

    var getFisHtml = function(value, msaLink, xVarLink) {

        var html;

        if (omaScoreMap[value] != null)
        {
            html = '<span class="oma_link ' + omaScoreMap[value].style + '" alt="' +
                   omaScoreMap[value].tooltip + "|" + xVarLink + '|' + msaLink + '">' +
                   '<label>' + omaScoreMap[value].label + '</label>' +
                   '</span>';
        }
        else
        {
            html = "";
        }

        return html;
    };

    var getPdbLinkHtml = function(value) {
        var html;

        if (value != null)
        {
            html = '<a href="' + value + '">' +
                     '<span style="background-color:#88C;color:white;">&nbsp;3D&nbsp;</span>' +
                     '</a>';
        }
        else
        {
            html = "";
        }

        return html;
    };

    var getCosmicHtml = function(value, count) {
        var html;

        if (count <= 0)
        {
            html = "<label></label>";
        }
        else
        {
            html = '<label class="mutation_table_cosmic" alt="' + value + '">' +
                   '<b>' + count + '</b></label>';
        }

        return html;
    };

    // generate rows as HTML

    var row;
    var rows = new Array();

    for (var i=0; i<data.mutations.length; i++)
    {
        row = new Array();

        row.push('<a href="' + data.mutations[i].linkToPatientView + '">' +
                 '<b>' + data.mutations[i].caseId + "</b></a>");
        row.push('<span class="protein_change">' +
                 data.mutations[i].proteinChange +
                '</span>');
        row.push(getMutationTypeHtml(data.mutations[i].mutationType.toLowerCase()));
        row.push(getCosmicHtml(data.mutations[i].cosmic, data.mutations[i].cosmicCount));
        row.push(getFisHtml(data.mutations[i].functionalImpactScore.toLowerCase(),
                            data.mutations[i].xVarLink,
                            data.mutations[i].msaLink));
        row.push(getPdbLinkHtml(data.mutations[i].pdbLink));
        row.push(getMutationStatusHtml(data.mutations[i].mutationStatus.toLowerCase()));
        row.push(getValidationStatusHtml(data.mutations[i].validationStatus.toLowerCase()));
        row.push(data.mutations[i].sequencingCenter);
        row.push(data.mutations[i].position);
        row.push(data.mutations[i].referenceAllele);
        row.push(data.mutations[i].variantAllele);
        // TODO write get functions to convert 'null's to 'NA's
        row.push(data.mutations[i].tumorFreq);
        row.push(data.mutations[i].normalFreq);
        row.push(data.mutations[i].tumorRefCount);
        row.push(data.mutations[i].tumorAltCount);
        row.push(data.mutations[i].normalRefCount);
        row.push(data.mutations[i].normalAltCount);

        //special gene data
        for (var j=0; j < data.mutations[i].specialGeneData.length; j++)
        {
            row.push(data.mutations[i].specialGeneData[j]);
        }

        rows.push(row);
    }

    return rows;
}

// not used anymore, using dataTable's own column show/hide plugin instead.
function _mutationTableToggleText(options)
{
    var selectedOptions = options.filter(":selected");
    var numberOfSelected = selectedOptions.size();
    var text = "";

    if (0 == numberOfSelected)
    {
        text = "No column selected";
    }
    else if (options.size() == numberOfSelected)
    {
        text = "All columns selected";
    }
    else
    {
        text = numberOfSelected +
               " (out of " +
               (options.size() - 1) + // excluding "select all"
               ") columns selected";
    }

    return text;
}

/**
 * Add tooltips for the table header and the table data rows.
 *
 * @param tableId   id of the target table
 */
function addMutationTableTooltips(tableId)
{
    var qTipOptions = {content: {attr: 'alt'},
        hide: { fixed: true, delay: 100 },
        style: { classes: 'mutation-details-tooltip ui-tooltip-shadow ui-tooltip-light ui-tooltip-rounded' },
        position: {my:'top center',at:'bottom center'}};

    $('#' + tableId + ' th').qtip(qTipOptions);
    //$('#mutation_details .mutation_details_table td').qtip(qTipOptions);

    $('#' + tableId + ' .simple-tip').qtip(qTipOptions);

    // copy default qTip options and modify "content" to customize for cosmic
    var qTipOptsCosmic = new Object();
    jQuery.extend(true, qTipOptsCosmic, qTipOptions);

    qTipOptsCosmic.content = { text: function(api) {
        var cosmic = $(this).attr('alt');
        var parts = cosmic.split("|");

        var cosmicTable =
            "<table class='" + tableId + "_cosmic_table cosmic_details_table display' " +
            "cellpadding='0' cellspacing='0' border='0'>" +
            "<thead><tr><th>Mutation</th><th>Count</th></tr></thead>";

        // COSMIC data (as AA change & frequency pairs)
        for (var i=0; i < parts.length; i++)
        {
            var values = parts[i].split(/\(|\)/, 2);

            if (values.length < 2)
            {
                // skip values with no count information
                continue;
            }

            // skip data starting with p.? or ?
            var unknownCosmic = values[0].indexOf("p.?") == 0 ||
                                values[0].indexOf("?") == 0;

            if (!unknownCosmic)
            {
                cosmicTable += "<tr><td>" + values[0] + "</td><td>" + values[1] + "</td></tr>";

                //$("#cosmic_details_table").dataTable().fnAddData(values);
            }
        }

        cosmicTable += "</table>";

        return cosmicTable;
    }};

    qTipOptsCosmic.events = {render: function(event, api)
    {
        // TODO data table doesn't initialize properly
        // initialize cosmic details table
        $('.' + tableId + '_cosmic_table').dataTable({
             "aaSorting" : [ ], // do not sort by default
             "sDom": 't', // show only the table
             "aoColumnDefs": [{ "sType": "aa-change-col", "aTargets": [0]},
                 { "sType": "numeric", "aTargets": [1]}],
             //"bJQueryUI": true,
             //"fnDrawCallback": function (oSettings) {console.log("cosmic datatable is ready?");},
             "bDestroy": true,
             "bPaginate": false,
             "bFilter": false});
    }};

    $('#' + tableId + ' .mutation_table_cosmic').qtip(qTipOptsCosmic);

    // copy default qTip options and modify "content"
    // to customize for predicted impact score
    var qTipOptsOma = new Object();
    jQuery.extend(true, qTipOptsOma, qTipOptions);

    qTipOptsOma.content = { text: function(api) {
        var links = $(this).attr('alt');
        var parts = links.split("|");

        var impact = parts[0];

        var tip = "Predicted impact: <b>"+impact+"</b>";

        var xvia = parts[1];
        if (xvia&&xvia!='NA')
            tip += "<br/><a href='"+xvia+"'><img height=15 width=19 src='images/ma.png'> Go to Mutation Assessor</a>";

        var msa = parts[2];
        if (msa&&msa!='NA')
            tip += "<br/><a href='"+msa+"'><img src='images/msa.png'> View Multiple Sequence Alignment</a>";

//		    var pdb = parts[3];
//		    if (pdb&&pdb!='NA')
//			    tip += "<br/><a href='"+pdb+"'><img src='images/pdb.png'> View Protein Structure</a>";

        return tip;
    }};

    $('#' + tableId + ' .oma_link').qtip(qTipOptsOma);
}