<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ page import="org.mskcc.cbio.cgds.model.ExtendedMutation" %>
<%@ page import="org.mskcc.cbio.portal.html.MutationTableUtil" %>
<%@ page import="org.mskcc.cbio.portal.model.ExtendedMutationMap" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.MutationCounter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.StringWriter" %>

<script type="text/javascript" src="js/raphael/raphael.js"></script>
<script type="text/javascript" src="js/mutation_diagram.js"></script>

<%
    ArrayList<ExtendedMutation> extendedMutationList = (ArrayList<ExtendedMutation>)
            request.getAttribute(QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST);
    ExtendedMutationMap mutationMap = new ExtendedMutationMap(extendedMutationList,
            mergedProfile.getCaseIdList());
%>
<div class='section' id='mutation_details'>

<%
    if (mutationMap.getNumGenesWithExtendedMutations() > 0) {
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            outputGeneTable(geneWithScore, mutationMap, out, mergedCaseList);
        }
    } else {
        outputNoMutationDetails(out);
    }
%>

</div>


<style type="text/css" title="currentStyle"> 
        .mutation_datatables_filter {
                width: 40%;
                float: right;
                padding-top:5px;
                padding-bottom:5px;
                padding-right:5px;
        }
        .mutation_datatables_info {
                width: 55%;
                float: left;
                padding-left:5px;
                padding-top:7px;
                font-size:90%;
        }
</style>

<script type="text/javascript">
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
        if (str=="Low") {
            return 1;
        } else if (str=="Medium") {
            return 2;
        } else if (str=="High") {
            return 3;
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

    //  Place mutation_details_table in a JQuery DataTable
    $(document).ready(function(){
        <%
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0) { %>
              $('#mutation_details_table_<%= geneWithScore.getGene().toUpperCase() %>').dataTable( {
					"sDom": '<"H"<"mutation_datatables_filter"f><"mutation_datatables_info"i>>t',
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
					      "aTargets": [ 3 ]},
					  {"sType": 'predicted-impact-col',
					          "aTargets": [ 4 ]}
					],
					"fnDrawCallback": function( oSettings ) {
						// add tooltips to the table
						addMutationTableTooltips('<%= geneWithScore.getGene().toUpperCase() %>');
					}
              } );

	            // hide nucleotide details by default
				var oTable = $('#mutation_details_table_<%= geneWithScore.getGene().toUpperCase() %>').dataTable();

	            for (var i=8; i<12; i++)
	            {
	                oTable.fnSetColumnVis(i, false)
	            }

            <% } %>
        <% } %>

	    // wrap the table contents with a div to enable scrolling, this is a workaround for
	    // DataTable's own scrolling, seems like there is a problem with its settings
	    $('.mutation_details_table').wrap("<div class='mutation_details_table_wrapper'></div>");

	    // to fit mutation table initially
	    fitMutationTableToWidth();

	    // TODO changing background requires additional settings for sort icons...
	    //$('#mutation_details .mutation_details_table th').addClass('ui-state-default');

	    // initialize table column filtering
	    var dropdownOptions = {firstItemChecksAll: true, // enables "select all" button
		    icon: {placement: 'left'}, // sets the position of the arrow
		    width: 220,
		    //emptyText: '(no columns selected)', // text to be displayed when no item is selected
		    onItemClick: toggleMutationTableColumns,
		    textFormatFunction: mutationTableToggleText}; // callback function for the action

	    // initialize the dropdown box
	    //$(".toggle_mutation_table_col").dropdownchecklist(dropdownOptions);
    });
    
    //  Set up Mutation Diagrams
    $(document).ready(function(){
	    // initially hide all tooltip boxes
	    $("div.mutation_diagram_details").hide();

	    // enable refitting of the mutation table on each click on mutation details tab
	    $("a.result-tab").click(function(event){
		    var tab = $(this).attr("href");

		    if (tab == "#mutation_details")
		    {
			    fitMutationTableToWidth();
		    }
	    });
    <%
    for (GeneWithScore geneWithScore : geneWithScoreList) {
        if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0) { %>
          $.ajax({ url: "mutation_diagram_data.json",
              dataType: "json",
              data: {hugoGeneSymbol: "<%= geneWithScore.getGene().toUpperCase() %>",
	              mutations: "<%= outputMutationsJson(geneWithScore, mutationMap) %>"},
              success: drawMutationDiagram,
              type: "POST"});
        <% } %>
    <% } %>
    });

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

    /**
     * Updates the table columns according to the new user selection.
     *
     * @param checkbox	target check box selected by the user
     * @param selector	target selection box modified by the user
     */
    function toggleMutationTableColumns(checkbox, selector)
    {
	    // extract geneId from the selector id
	    var lastDashIdx = selector.id.lastIndexOf("_");
	    var geneId = selector.id.substring(lastDashIdx + 1);

	    var colIdx = checkbox.val();
	    var checked = checkbox.prop("checked"); // checked or unchecked

	    // get the corresponding table for that gene
	    var targetTable = $('#mutation_details_table_' + geneId).dataTable();

	    // if colIdx is a number, then hide that column
	    if(!isNaN(colIdx))
	    {
		    targetTable.fnSetColumnVis(colIdx, checked);
	    }
	    // if it is not a number, then it is "show all columns"
	    else
	    {
		    for (var i=0;
		         i < selector.options.length - 1; // excluding "show all" option
		         i++)
		    {
			    targetTable.fnSetColumnVis(i, checked);
		    }
	    }
    }

    /**
     * Toggles the diagram between lollipop view and histogram view.
     *
	 * @param geneId    id of the target diagram
     */
	function toggleMutationDiagram(geneId)
	{
		var option = $("#mutation_diagram_select_" + geneId).val();

		if (option == "diagram")
		{
			$("#mutation_diagram_" + geneId).show();
			$("#mutation_histogram_" + geneId).hide();
		}
		else if (option == "histogram")
		{
			$("#mutation_diagram_" + geneId).hide();
			$("#mutation_histogram_" + geneId).show();
		}
	}

    function addMutationTableTooltips(geneId)
    {
	    var tableId = "mutation_details_table_" + geneId;

	    var qTipOptions = {content: {attr: 'alt'},
		    hide: { fixed: true, delay: 100 },
		    style: { classes: 'mutation-details-tooltip ui-tooltip-light ui-tooltip-rounded' },
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

		    var pdb = parts[3];
		    if (pdb&&pdb!='NA')
			    tip += "<br/><a href='"+pdb+"'><img src='images/pdb.png'> View Protein Structure</a>";

		    return tip;
	    }};

	    $('#' + tableId + ' .oma_link').qtip(qTipOptsOma);
    }

    // TODO this will only fit the table wrapper initially, it won't refit when page is resized!
	function fitMutationTableToWidth()
	{
		// fit the table wrapper and the filter bar to the width of page
		var fitWidth = $('#page_wrapper').width() - 60; // 60 is an approximation for total margins
		$('#mutation_details .mutation_details_table_wrapper').width(fitWidth); // fit the table wrapper
		$('#mutation_details .dataTables_wrapper .fg-toolbar').width(fitWidth); // fit the table toolbar
	}

</script>


<%!
	private void outputColumnFilter(JspWriter out,
			GeneWithScore geneWithScore,
			ArrayList<String> headers) throws IOException
	{
		String geneId = geneWithScore.getGene().toUpperCase();

		out.println("<table class='toggle_mutation_table_col_table'>" +
		            "<tr><td class='toggle_mut_table_col_td_label'>");
		out.println("Toggle Columns: ");
		out.println("</td><td class='toggle_mut_table_col_td_select'>");

		out.println("<select multiple class='toggle_mutation_table_col' " +
		            "id='column_select_" + geneId + "'>");

		out.println("<option selected id='" + geneId + "_column_selectAll " +
				"value='NA'>" +
				"<label>(show all columns)</label>" +
				"</option>");

		for (int i = 0; i < headers.size(); i++)
		{
			String header = headers.get(i);

			String[] parts = header.split("\t");

			if (parts.length >= 2)
			{
				header = parts[1];
			}

			//out.println("<option id='" + geneId + "_" + header + "'" +
			out.println("<option " +
			            "value='" + i + "'>" +
			            "<label>" + header + "</label>" +
			            "</option>");
		}

		out.println("</select>");

		out.println("</td></tr></table>");
	}

	private String outputMutationsJson(final GeneWithScore geneWithScore, final ExtendedMutationMap mutationMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        List<ExtendedMutation> mutations = mutationMap.getExtendedMutations(geneWithScore.getGene());
        try {
            objectMapper.writeValue(stringWriter, mutations);
        }
        catch (Exception e) {
            // ignore
        }
        return stringWriter.toString().replace("\"", "\\\"");
    }

    private void outputGeneTable(GeneWithScore geneWithScore,
            ExtendedMutationMap mutationMap, JspWriter out, 
            ArrayList<String> mergedCaseList) throws IOException {
        MutationTableUtil mutationTableUtil = new MutationTableUtil(geneWithScore.getGene());
        MutationCounter mutationCounter = new MutationCounter(geneWithScore.getGene(),
                mutationMap);

        if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0)
        {
            outputHeader(out, geneWithScore, mutationCounter);
	        //outputColumnFilter(out, geneWithScore, mutationTableUtil.getTableHeaders());

            out.println("<table cellpadding='0' cellspacing='0' border='0' " +
                    "class='display mutation_details_table' " +
                    "id='mutation_details_table_" + geneWithScore.getGene().toUpperCase()
                    +"'>");

            //  Table column headers
            out.println("<thead>");
            out.println(mutationTableUtil.getTableHeaderHtml() + "<BR>");
            out.println("</thead>");

            //  Mutations are sorted by case
            out.println("<tbody>");
            for (String caseId : mergedCaseList) {
                ArrayList<ExtendedMutation> mutationList =
                        mutationMap.getExtendedMutations(geneWithScore.getGene(), caseId);
                if (mutationList != null && mutationList.size() > 0) {
                    for (ExtendedMutation mutation : mutationList) {
                        out.println(mutationTableUtil.getDataRowHtml(mutation));
                    }
                }
            }
            out.println("</tbody>");

            //  Table column footer
            out.println("<tfoot>");
            out.println(mutationTableUtil.getTableHeaderHtml());
            out.println("</tfoot>");

            out.println("</table><p><br>");
            out.println(mutationTableUtil.getTableFooterMessage());
            out.println("<br>");
        }
    }

    private void outputHeader(JspWriter out, GeneWithScore geneWithScore,
            MutationCounter mutationCounter) throws IOException {
        out.print("<h4>" + geneWithScore.getGene().toUpperCase() + ": ");
        out.println(mutationCounter.getTextSummary());
        out.println("</h4>");
	    // TODO histogram is disabled (will be enabled in the next release)
//	    out.println("<select class='mutation_diagram_toggle' " +
//	                "id='mutation_diagram_select_" + geneWithScore.getGene().toUpperCase() + "'" +
//	                "onchange='toggleMutationDiagram(\"" + geneWithScore.getGene().toUpperCase() + "\")'>" +
//	               "<option value='diagram'>Lollipop Diagram</option>" +
//	               "<option value='histogram'>Histogram</option>" +
//	               "</select>");
        out.println("<div id='mutation_diagram_" + geneWithScore.getGene().toUpperCase() + "'></div>");
	    out.println("<div id='mutation_histogram_" + geneWithScore.getGene().toUpperCase() + "'></div>");
        out.println("<div class='mutation_diagram_details' id='mutation_diagram_details_" + geneWithScore.getGene().toUpperCase() + "'>The height of the bars indicates the number of mutations at each position.<BR>Roll-over the dots and domains to view additional details.<BR>Domain details derived from <a href='http://pfam.sanger.ac.uk/'>Pfam</a>.</div>");
    }

    private void outputNoMutationDetails(JspWriter out) throws IOException {
        out.println("<p>There are no mutation details available for the gene set entered.</p>");
        out.println("<br><br>");
    }
%>
