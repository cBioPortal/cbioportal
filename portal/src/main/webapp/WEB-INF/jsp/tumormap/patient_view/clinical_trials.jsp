<%@ page import="org.mskcc.cbio.cgds.dao.DaoTypeOfCancer" %>
<%@ page import="org.mskcc.cbio.cgds.model.TypeOfCancer" %>
<%@ page import="org.mskcc.cbio.cgds.dao.DaoException" %><%
    String cancerStudyName = cancerStudy.getName();
    // Try to find a better and general name for this one -- if any.
    try {
        for (TypeOfCancer typeOfCancer : DaoTypeOfCancer.getAllTypesOfCancer()) {
            if(typeOfCancer.getTypeOfCancerId().equalsIgnoreCase(cancerStudy.getTypeOfCancerId()))
                cancerStudyName = typeOfCancer.getName();
        }
    } catch (DaoException e) {
        // Ignore it
    }
%>

<style type="text/css">
    .drugs-summary-table-name, .trials-summary-table-name {
        float: left;
        font-weight: bold;
        vertical-align: middle;
    }
    .highlight {
        font-weight: bold;
        text-decoration: underline;
    }
    #trial-filtering {
        float: right;
        margin-bottom: 10px;
    }

</style>
<script type="text/javascript" src="js/jquery.highlight-4.js"></script>
<script type="text/javascript">
    var keywords = [];
    // A map from drug names to drug ids
    var drugMap = {};

    var populateDrugTable = function() {
        var drugIds = [];
        drugIds = drugIds.concat(genomicEventObs.cnas.getDrugIDs());
        drugIds = drugIds.concat(genomicEventObs.mutations.getDrugIDs());

        // reset the keywords
        keywords = [];

        $.post("drugs.json",
                { drug_ids: drugIds.join(",") },
                function(data) {
                    // Reset the map
                    drugMap = {};

                    $("#drugs_wait").hide();
                    for(var i=0; i < data.length; i++) {
                        var drug = data[i];

                        var xref = ["N/A"];
                        if (drug[7]) { // xref
                            xref = [];
                            var nci = drug[7]['NCI_Drug'];
                            if(nci)
                                xref.push("<a href='http://www.cancer.gov/drugdictionary?CdrID="+nci+"' target='_blank'>NCI</a>");
                            var pharmgkb = drug[7]['PharmGKB'];
                            if(pharmgkb)
                                xref.push("<a href='http://www.pharmgkb.org/views/index.jsp?objId="+pharmgkb+"' target='_blank'>PharmGKB</a>");
                            var drugbank = drug[7]['DrugBank'];
                            if(drugbank)
                                xref.push("<a href='http://www.drugbank.ca/drugs/"+drugbank+"' target='_blank'>DrugBank</a>");
                            var keggdrug = drug[7]['KEGG Drug'];
                            if(keggdrug)
                                xref.push("<a href='http://www.genome.jp/dbget-bin/www_bget?dr:"+keggdrug+"' target='_blank'>KEGG Drug</a>");
                        }

                        var drugTargets = "";
                        var targets = drug[1].split(",");
                        if(targets.length > 3) {
                            drugTargets = targets.slice(0, 3).join(",");
                            drugTargets += ' <br/><small title="' + drug[1] + '" class="drug-targets">('
                                    + (targets.length-3) + ' more)</small>';
                        } else {
                            drugTargets = drug[1];
                        }

                        $("#pv-drugs-table").append(
                            '<tr>'
                                + '<td>'
                                    + drug[2]
                                    + '<small title="' + drug[3].replace(";", ",") + '" class="drug-synoynms"> <br/>'
                                        + "(" + drug[3].split(";").length + " more)"
                                    + '</small>'
                                + '</td>'
                                + '<td>' + drugTargets + '</td>'
                                + '<td>' + drug[5] + '</td>'
                                + '<td>' + (drug[4] ? "Yes" : "No") + '</td>'
                                + '<td>' + xref.join(", ") + '</td>'
                            + '</tr>'
                        );

                        drugMap[drug[2]] = drug[0];
                        keywords.push(drug[2]);
                    }

                    $("#pv-drugs-table").dataTable({
                        "sDom": '<"H"<"drugs-summary-table-name">fr>t<"F"<"drugs-show-more"><"datatable-paging"pl>>',
                        "bJQueryUI": true,
                        "bDestroy": true,
                        "aaSorting": [[0, 'asc']],
                        "oLanguage": {
                            "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                            "sInfoFiltered": "",
                            "sLengthMenu": "Show _MENU_ per page",
                            "sEmptyTable": "Could not find any drugs of interest."
                        },
                        "iDisplayLength": 25,
                        "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
                    });

                    $(".drug-synoynms").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded' }
                    });

                    $(".drug-targets").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded' }
                    });


                    populateClinicalTrialsTable(keywords, 'both');

                    var infoBox = "<img id='drug-summary-help' src='images/help.png' title='"
                            + "These drugs were selected based on the patient's genomic alteration. "
                            + "'>";
                    $(".drugs-summary-table-name").html("" + data.length + " drugs of interest " + infoBox);
                    $("#drug-summary-help").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded' }
                    });

                }
        );
    };

    var clinicalTrialsDataTable = null;
    var populateClinicalTrialsTable = function(keywords, filterBy) {
        // Remove all the current rows from the table before population it
        $(".trials-row").remove();
        if(clinicalTrialsDataTable != null) {
            clinicalTrialsDataTable.fnClearTable();
            clinicalTrialsDataTable.fnDestroy();
            $("#pv-trials-table").css({ width: "100%"});
        }
        $("#trials_wait").show();

        var studyOfInterest = "<%=cancerStudyName%>";
        var studyTokens = studyOfInterest.split(" ");
        var studyTerms = (studyOfInterest.search(" and ") > 0) ? studyTokens[0] + "," + studyTokens[2] : studyTokens[0];
        $.post("clinicaltrials.json",
                {
                    keywords: keywords.join(","),
                    filter: filterBy,
                    study: studyTerms
                },

                function(data) {

                    for(var i=0; i < data.length; i++) {
                        var trial = data[i];

                        $("#pv-trials-table").append(
                            '<tr class="trial-row">'
                                + '<td align="center">'
                                    + '<a href="http://cancer.gov/clinicaltrials/search/view?version=healthprofessional&cdrid=' + trial[5] + '" target="_blank">'
                                        + trial[0]
                                    + '</a>'
                                + '</td>'
                                + '<td>' + trial[1] + '</td>'
                                + '<td>' + trial[2] + '</td>'
                                + '<td>' + trial[3] + '</td>'
                                + '<td>' + trial[4] + '</td>'
                            + '</tr>'
                        );
                    }

                    // highlight keywords within the table
                    for(var k=0; k < keywords.length; k++) {
                        $("#pv-trials-table").find("td").highlight(keywords[k]);
                    }

                    // Add tooltips to the drug-keywords
                    $(".highlight").each(function(idx) {
                        var drugName = $(this).text();
                        var drugId = drugMap[drugName];
                        if(drugId != undefined) {
                            $(this).attr("alt", drugId);
                        }
                    });
                    addDrugsTooltip(".highlight", 'top left', 'bottom center');

                    // Build the table
                    clinicalTrialsDataTable = $("#pv-trials-table").dataTable({
                        "sDom": '<"H"<"trials-summary-table-name">fr>t<"F"<"trials-show-more"><"datatable-paging"pl>>',
                        "bJQueryUI": true,
                        "bDestroy": true,
                        "aaSorting": [[2, 'asc'], [0, 'desc']],
                        "oLanguage": {
                            "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                            "sInfoFiltered": "",
                            "sLengthMenu": "Show _MENU_ per page",
                            "sEmptyTable": "Could not find any clinical trials of interest."
                        },
                        "iDisplayLength": 25,
                        "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
                    });

                    // Done with the loading. Hide the image.
                    $("#trials_wait").hide();

                    var infoBox = "<img id='trial-summary-help' src='images/help.png' title='"
                            + "The following clinical trials are listed because they match with "
                            + (filterBy == "both" ? "both the drugs and the cancer type" : (filterBy == "study") ? "the cancer type" : "the drugs")
                            + " of interest. <br/><br/>"
                            + "The data for the clinical trials listed on this page was "
                            + "kindly provided by NCI, Cancer.gov through the content dissemination program."
                            + "'>";

                    $(".trials-summary-table-name").html(data.length + " clinical trials of interest " + infoBox);
                    $("#trial-summary-help").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded' }
                    });

                }
        );
    };

    $(document).ready(function() {
        genomicEventObs.subscribeMutCna(populateDrugTable);

        $("#trial-filtering-options").change(function() {
            populateClinicalTrialsTable(keywords, $("#trial-filtering-options").val() == "all");
        });
    });
</script>

<div id="trial-filtering">
    <b>Filter trials by</b>:
    <select id="trial-filtering-options">
        <option value="both" selected="true">Drugs and cancer type</option>
        <option value="drugs">Drugs</option>
        <option value="study">Cancer type</option>
    </select>
</div>

<table id="pv-trials-table" class="dataTable display" style="width: 100%;">
   <thead>
    <tr>
        <th>Trial ID</th>
        <th>Title</th>
        <th>Status</th>
        <th>Phase</th>
        <th>Location</th>
    </tr>
   </thead>
</table>
<div id="trials_wait"><img src="images/ajax-loader.gif"/></div>
<p><small><b>*</b> The data for the clinical trials listed on this page was kindly provided
    by NCI's <a href="http://cancer.gov">Cancer.Gov</a> website
    through <a href="http://www.cancer.gov/global/syndication/content-use">the content dissemination program</a>.
    </small>
</p>
