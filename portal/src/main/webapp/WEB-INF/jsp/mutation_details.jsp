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
	<div id="cosmic_details_dialog" title="Cosmic Details" class="dataTables_wrapper">
		<table id="cosmic_details_table" class="display">
			<thead>
				<tr>
					<th>Overlapping COSMIC AA change</th>
					<th>Frequency</th>
				</tr>
			</thead>
		</table>
	</div>

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
    jQuery.fn.dataTableExt.oSort['aa-change-col-asc']  = function(a,b) {
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
    };
    
    jQuery.fn.dataTableExt.oSort['predicted-impact-col-desc']  = function(a,b) {
        var av = assignValueToPredictedImpact(a.replace(/<[^>]*>/g,""));
        var bv = assignValueToPredictedImpact(b.replace(/<[^>]*>/g,""));
        
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
    };

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
                              "aTargets": [ 5 ]},
                      {"sType": 'predicted-impact-col',
                              "aTargets": [ 13 ]}
                  ]
              } );
            <% } %>
        <% } %>

	    // wrap the table contents with a div to enable scrolling, this is a workaround for
	    // DataTable's own scrolling, seems like there is a problem with its settings
	    $('.mutation_details_table').wrap("<div class='mutation_details_table_wrapper'></div>");

	    // to fit mutation table initially
	    fitMutationTableToWidth();

	    $("#cosmic_details_dialog").dialog({autoOpen: false,
			resizable: false,
			width: 300});

	    $('a.mutation_table_cosmic').unbind(); // TODO temporary work-around, should fix the listener in MakeOncoPrint

	    // initialize mutation details table
	    $("#cosmic_details_table").dataTable({
		    "aaSorting" : [ ], // do not sort by default
			"sDom": 't', // show only the table
			"aoColumnDefs": [{ "sType": "aa-change-col", "aTargets": [0]},
				{ "sType": "numeric", "aTargets": [1]}],
			"bPaginate": false,
			"bFilter": false});

	    $('a.mutation_table_cosmic').click(function(event){
		    var cosmic = this.id;
		    var parts = cosmic.split("|");

		    $("#cosmic_details_table").dataTable().fnClearTable();

		    // COSMIC data (as AA change & frequency pairs)
		    for (var i=0; i < parts.length; i++)
		    {
			    var values = parts[i].split(/\(|\)/, 2);

			    $("#cosmic_details_table").dataTable().fnAddData(values);
		    }

		    $("#cosmic_details_dialog").dialog("open").height("auto");
	    });
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
              data: { hugoGeneSymbol: "<%= geneWithScore.getGene().toUpperCase() %>", mutations: "<%= outputMutationsJson(geneWithScore, mutationMap) %>" },
              success: drawMutationDiagram,
              type: "POST"});
        <% } %>
    <% } %>
    });

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

    // TODO this will only fit the table wrapper initially, it won't refit when page is resized!
	function fitMutationTableToWidth()
	{
		// fit the table wrapper and the filter bar to the width of page
		var fitWidth = $('#page_wrapper').width() - 60; // 60 is an approximation for total margins
		$('.mutation_details_table_wrapper').width(fitWidth); // fit the table wrapper
		$('.dataTables_wrapper .fg-toolbar').width(fitWidth); // fit the table toolbar
	}

</script>


<%!

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

        if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0) {
            outputHeader(out, geneWithScore, mutationCounter);
            outputOmaHeader(out);
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
	    out.println("<select class='mutation_diagram_toggle' " +
	                "id='mutation_diagram_select_" + geneWithScore.getGene().toUpperCase() + "'" +
	                "onchange='toggleMutationDiagram(\"" + geneWithScore.getGene().toUpperCase() + "\")'>" +
	               "<option value='diagram'>Lollipop Diagram</option>" +
	               "<option value='histogram'>Histogram</option>" +
	               "</select>");
        out.println("<div id='mutation_diagram_" + geneWithScore.getGene().toUpperCase() + "'></div>");
	    out.println("<div id='mutation_histogram_" + geneWithScore.getGene().toUpperCase() + "'></div>");
        out.println("<div class='mutation_diagram_details' id='mutation_diagram_details_" + geneWithScore.getGene().toUpperCase() + "'>The height of the bars indicates the number of mutations at each position.<BR>Roll-over the dots and domains to view additional details.<BR>Domain details derived from <a href='http://pfam.sanger.ac.uk/'>Pfam</a>.</div>");
    }

    private void outputNoMutationDetails(JspWriter out) throws IOException {
        out.println("<p>There are no mutation details available for the gene set entered.</p>");
        out.println("<br><br>");
    }

    private void outputOmaHeader(JspWriter out) throws IOException {
        out.println("<br>** Predicted functional impact (via " +
                "<a href='http://mutationassessor.org'>Mutation Assessor</a>)" +
                " is provided for missense mutations only.  ");
        out.println("<br>");
    }
%>
