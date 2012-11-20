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

function assignValueToPredictedImpact(str) {
    if (str == "Low" || str == "L") {
        return 2;
    } else if (str == "Medium" || str == "M") {
        return 3;
    } else if (str == "High" || str == "H") {
        return 4;
    } else if (str == "Neutral" || str == "N") {
        return 1;
    } else {
        return 0;
    }
}

jQuery.fn.dataTableExt.oSort['predicted-impact-col-asc']  = function(a,b) {
    var av = assignValueToPredictedImpact(a.replace(/<[^>]*>/g,""));
    var bv = assignValueToPredictedImpact(b.replace(/<[^>]*>/g,""));

    return _compareSortAsc(a, b, av, bv);
};

jQuery.fn.dataTableExt.oSort['predicted-impact-col-desc']  = function(a,b) {
    var av = assignValueToPredictedImpact(a.replace(/<[^>]*>/g,""));
    var bv = assignValueToPredictedImpact(b.replace(/<[^>]*>/g,""));

    return _compareSortDesc(a, b, av, bv);
};

jQuery.fn.dataTableExt.oSort['cosmic-col-asc'] = function(a,b) {
    var av = _getCosmicTextValue(a);
    var bv = _getCosmicTextValue(b);

    return _compareSortAsc(a, b, av, bv);
};

jQuery.fn.dataTableExt.oSort['cosmic-col-desc'] = function(a,b) {
    var av = _getCosmicTextValue(a);
    var bv = _getCosmicTextValue(b);

    return _compareSortDesc(a, b, av, bv);
};

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

function drawMutationTable(data)
{
    var divId = "mutation_table_" + data.hugoGeneSymbol;
    var tableId = "mutation_details_table_" + data.hugoGeneSymbol;

    $("#" + divId).empty();

    // TODO gene.touppercase?
    $("#" + divId).append(_generateMutationTable(tableId, data));


//			    $("#" + divId).append("<table cellpadding='0' cellspacing='0' border='0' " +
//			                          "class='display mutation_details_table' " +
//			                          "id='" + tableId + "'></table>");
//
//		        var rows = _getMutationTableRows(data);
//		        var columns = _getMutationTableColumns(data);

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
        "fnDrawCallback": function( oSettings ) {
            // add tooltips to the table
            //addMutationTableTooltips(tableId);
        }
    });

    var cols = oTable.fnSettings().aoColumns.length;

    for (var col=9; col<cols; col++)
    {
        oTable.fnSetColumnVis( col, false );
    }

    oTable.css("width", "100%");
}

// TODO we may generate everything within dataTables instead.
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
        table += '<th>' + headers[i] + '</th>';
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
        table += '<th>' + headers[i] + '</th>';
    }

    table += '</tfoot>';

    table += '</table>';

    // TODO footer msg

    return table;
}

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

    // special gene headers
    for (var i=0; i < data.header.specialGeneHeaders.length; i++)
    {
        headers.push(data.header.specialGeneHeaders[i]);
    }

    return headers;
}

function _getMutationTableColumns(data)
{
    var columns = new Array();
    var headers = _getMutationTableHeaders(data);

    for (var i=0; i < headers.length; i++)
    {
        columns.push({"sTitle" : headers[i]});
    }

    return columns;

}
function _getMutationTableRows(data)
{
    var row;
    var rows = new Array();

    for (var i=0; i<data.mutations.length; i++)
    {
        row = new Array();

        row.push(data.mutations[i].caseId);
        row.push(data.mutations[i].proteinChange);
        row.push(data.mutations[i].mutationType);
        row.push(data.mutations[i].cosmicCount);
        row.push(data.mutations[i].functionalImpactScore);
        row.push(data.mutations[i].pdbLink);
        row.push(data.mutations[i].mutationStatus);
        row.push(data.mutations[i].validationStatus);
        row.push(data.mutations[i].sequencingCenter);
        row.push(data.mutations[i].position);
        row.push(data.mutations[i].referenceAllele);
        row.push(data.mutations[i].variantAllele);

        //special gene data
        for (var j=0; j < data.mutations[i].specialGeneData.length; j++)
        {
            row.push(data.mutations[i].specialGeneData[j]);
        }

        rows.push(row);
    }

    return rows;
}

//  Place mutation_details_table in a JQuery DataTable
function _drawMutationTableOld(){
//$(document).ready(function(){
//    <%
//    for (GeneWithScore geneWithScore : geneWithScoreList) {
//        if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0) { %>
        var oTable = $('#mutation_details_table_<%= geneWithScore.getGene().toUpperCase() %>').dataTable( {
        "sDom": '<"H"<"mutation_datatables_filter"f>C<"mutation_datatables_info"i>>t',
        "bJQueryUI": true,
        "bPaginate": false,
        "bFilter": true,
        // TODO DataTable's own scroll doesn't work as expected (probably because of bad CSS settings)
        //"sScrollX": "100%",
        //"sScrollXInner": "105%",
        //"bScrollCollapse": true,
        //"bScrollAutoCss": false,
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
    "fnDrawCallback": function( oSettings ) {
						// add tooltips to the table
						addMutationTableTooltips('<%= geneWithScore.getGene().toUpperCase() %>');
					}
              } );

              var cols = oTable.fnSettings().aoColumns.length;
              for (var col=9; col<cols; col++) {
                 oTable.fnSetColumnVis( col, false );
              }

              oTable.css("width","100%");
//
//        <% } %>
//            <% } %>
//
//            // wrap the table contents with a div to enable scrolling, this is a workaround for
//            // DataTable's own scrolling, seems like there is a problem with its settings
//	    //$('.mutation_details_table').wrap("<div class='mutation_details_table_wrapper'></div>");
//
}

function mutationTableToggleText(options)
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

function addMutationTableTooltips(geneId)
{
    var tableId = "mutation_details_table_" + geneId;

    var qTipOptions = {content: {attr: 'alt'},
        hide: { fixed: true, delay: 100 },
        style: { classes: 'mutation-details-tooltip ui-tooltip-shadow ui-tooltip-light ui-tooltip-rounded' },
        position: {my:'top center',at:'bottom center'}};

    $('#' + tableId + ' th').qtip(qTipOptions);
    //$('#mutation_details .mutation_details_table td').qtip(qTipOptions);

    $('#' + tableId + ' .somatic').qtip(qTipOptions);
    $('#' + tableId + ' .germline').qtip(qTipOptions);

    $('#' + tableId + ' .unknown').qtip(qTipOptions);
    $('#' + tableId + ' .valid').qtip(qTipOptions);
    $('#' + tableId + ' .wildtype').qtip(qTipOptions);

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