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
</style>
<script type="text/javascript">
    var populateDrugTable = function() {
        var drugIds = [];
        drugIds = drugIds.concat(genomicEventObs.cnas.getDrugIDs());
        drugIds = drugIds.concat(genomicEventObs.mutations.getDrugIDs());

        var keywords = [];
        $.post("drugs.json",
                { drug_ids: drugIds.join(",") },
                function(data) {
                    $("#drugs_wait").hide();
                    for(var i=0; i < data.length; i++) {
                        var drug = data[i];

                        var xref = ["N/A"];
                        if (drug[7]) { // xref
                            xref = [];
                            var nci = drug[7]['NCI_Drug'];
                            if(nci)
                                xref.push("<a href='http://www.cancer.gov/drugdictionary?CdrID="+nci+"'>NCI</a>");
                            var pharmgkb = drug[7]['PharmGKB'];
                            if(pharmgkb)
                                xref.push("<a href='http://www.pharmgkb.org/views/index.jsp?objId="+pharmgkb+"'>PharmGKB</a>");
                            var drugbank = drug[7]['DrugBank'];
                            if(drugbank)
                                xref.push("<a href='http://www.drugbank.ca/drugs/"+drugbank+"'>DrugBank</a>");
                            var keggdrug = drug[7]['KEGG Drug'];
                            if(keggdrug)
                                xref.push("<a href='http://www.genome.jp/dbget-bin/www_bget?dr:"+keggdrug+"'>KEGG Drug</a>");
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


                    populateClinicalTrialsTable(keywords);

                    var infoBox = "<img id='drug-summary-help' src='images/help.png' title='"
                            + "These drugs were selected based on the patient's genomic alteration. "
                            + "'>";
                    $(".drugs-summary-table-name").html("" + data.length + " drugs " + infoBox);
                    $("#drug-summary-help").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded' }
                    });

                }
        );
    };

    var populateClinicalTrialsTable = function(keywords) {
        keywords.push("<%=cancerStudyName%>");
        $.post("clinicaltrials.json",
                { keywords: keywords.join(",") },
                function(data) {
                    $("#trials_wait").hide();

                    for(var i=0; i < data.length; i++) {
                        var trial = data[i];
                        $("#pv-trials-table").append(
                            '<tr>'
                                + '<td>'
                                    + '<a href="http://clinicaltrials.gov/show/' + trial[0] + '" target="_blank">'
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

                    $("#pv-trials-table").dataTable({
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

                    var infoBox = "<img id='trial-summary-help' src='images/help.png' title='"
                            + "The following clinical trials are listed because they are associated "
                            + " with either the drugs or the cancer type of interest. <br/><br/> "
                            + "The data was acquired from the <a href=\"http://cancer.gov\">cancer.gov</a> website."
                            + "'>";

                    $(".trials-summary-table-name").html(data.length + " clinical trials " + infoBox);
                    $("#trial-summary-help").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded' }
                    });

                }
        );
    };

    $(document).ready(function() {
        genomicEventObs.subscribeMutCna(populateDrugTable);
    });
</script>

<h2>Drugs of interest</h2>

<table id="pv-drugs-table" class="dataTable display">
   <thead>
    <tr>
        <th>Drug Name</th>
        <th>Drug Target(s)</th>
        <th class="drug-description">Description</th>
        <th>FDA approved?</th>
        <th>Data Sources</th>
    </tr>
   </thead>

</table>
<div id="drugs_wait"><img src="images/ajax-loader.gif"/></div>

<h2>Clinical trials of interest</h2>

<table id="pv-trials-table" class="dataTable display">
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
