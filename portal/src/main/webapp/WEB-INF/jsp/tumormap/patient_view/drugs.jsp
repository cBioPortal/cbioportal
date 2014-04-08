<%--
  ~ Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
  ~ This library is free software; you can redistribute it and/or modify it
  ~ under the terms of the GNU Lesser General Public License as published
  ~ by the Free Software Foundation; either version 2.1 of the License, or
  ~ any later version.
  ~
  ~ This library is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ~ MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ~ documentation provided hereunder is on an "as is" basis, and
  ~ Memorial Sloan-Kettering Cancer Center
  ~ has no obligations to provide maintenance, support,
  ~ updates, enhancements or modifications.  In no event shall
  ~ Memorial Sloan-Kettering Cancer Center
  ~ be liable to any party for direct, indirect, special,
  ~ incidental or consequential damages, including lost profits, arising
  ~ out of the use of this software and its documentation, even if
  ~ Memorial Sloan-Kettering Cancer Center
  ~ has been advised of the possibility of such damage.  See
  ~ the GNU Lesser General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public License
  ~ along with this library; if not, write to the Free Software Foundation,
  ~ Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  --%>

<%@ page import="org.mskcc.cbio.portal.dao.DaoTypeOfCancer" %>
<%@ page import="org.mskcc.cbio.portal.model.TypeOfCancer" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoException" %><%
    String cancerTypeId = cancerStudy.getTypeOfCancerId().trim();
    TypeOfCancer typeOfCancerById = DaoTypeOfCancer.getTypeOfCancerById(cancerTypeId);
    String trialKeywords = typeOfCancerById.getClinicalTrialKeywords();
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
    .annotated-target, .drug-synoynms {
        font-weight: bold;
    }

</style>
<script type="text/javascript" src="js/lib/jquery.highlight-4.js"></script>
<script type="text/javascript">
    var keywords = [];
    // A map from drug names to drug ids
    var drugMap = {};

    var cnaDrugs;
    var mutDrugs;

    var extractTargetsOfInterests = function(id, drugs) {
        var genes = [];

        for(var i=0; i < drugs.ids.length; i++) {
            if(drugs.ids[i] == id) {
                genes.push(drugs.genes[i]);
            }
        }

        return genes;
    };

    var populateDrugTable = function() {
        var drugIds = [];

        cnaDrugs = genomicEventObs.cnas.getDrugs();
        mutDrugs = genomicEventObs.mutations.getDrugs();

        drugIds = drugIds.concat(cnaDrugs.ids);
        drugIds = drugIds.concat(mutDrugs.ids);

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

                        var upstreamTxt = ", which is upstream of at least one of the indicated target genes for this drug.";
                        var drugTargets = "";
                        var cnaTargets = extractTargetsOfInterests(drug[0], cnaDrugs);
                        var mutTargets = extractTargetsOfInterests(drug[0], mutDrugs);
                        var targets = drug[1].split(", ");
                        var usedTargets = [];
                        var j, aTarget, altText;
                        for(j=0; j < cnaTargets.length; j++) {
                            aTarget = cnaTargets[j];
                            usedTargets.push(aTarget);
                            if($.inArray(aTarget, mutTargets) > 0) {
                                altText = "There are both a mutation and a copy-number alteration in this gene";
                            } else {
                                altText = "There is a copy-number alteration in this gene";
                            }

                            if($.inArray(aTarget, targets) < 0) {
                                altText += upstreamTxt;
                            } else {
                                altText += ".";
                            }

                            drugTargets += "<span class='annotated-target' title='" + altText + "'>" + aTarget + "</span><br/>";
                        }

                        for(j=0; j < mutTargets.length; j++) {
                            aTarget = mutTargets[j];
                            if($.inArray(aTarget, usedTargets) > 0) {
                                continue;
                            } else {
                                usedTargets.push(aTarget);
                            }

                            altText = "There is a mutation in this gene.";

                            if($.inArray(aTarget, targets) < 0) {
                                altText += upstreamTxt;
                            } else {
                                altText += ".";
                            }

                            drugTargets += "<span class='annotated-target' title='" + altText + "'>" + aTarget + "</span><br/>";
                        }

                        var leftDrugs = [];
                        for(j=0; j < targets.length; j++) {
                            if($.inArray(targets[j], usedTargets) > 0) {
                                continue;
                            }

                            leftDrugs.push(targets[j]);
                        }

                        var moreText = "";
                        if(leftDrugs.length > 0) {
                            moreText = "<small class='drug-targets' title='" + leftDrugs.join(", ") + "'>(" + leftDrugs.length + " more)</small>";
                        }

                        $("#pv-drugs-table").append(
                            '<tr>'
                                + '<td>'
                                    + '<span title="<b>Synonyms: </b>' + drug[3].replace(";", ",") + '" class="drug-synoynms"> <br/>'
                                        + drug[2]
                                    + '</span>'
                                + '</td>'
                                + '<td align="center">' + drugTargets + moreText + '</td>'
                                + '<td>' + drug[5] + '</td>'
                                + '<td>' + (drug[4] ? "Yes" : "No") + '</td>'
                                + '<td>' + xref.join(", ") + '</td>'
                            + '</tr>'
                        );

                        drugMap[drug[2].toLowerCase()] = drug[0];
                        keywords.push(drug[2]);
                    }

                    var drugsTable = $("#pv-drugs-table").dataTable({
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
                    drugsTable.css("width","100%");

                    $(".drug-synoynms").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'qtip-light qtip-rounded' },
                        position: { viewport: $(window) }
                    });

                    $(".drug-targets").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'qtip-light qtip-rounded' },
                        position: { viewport: $(window) }
                    });

                    $(".annotated-target").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'qtip-light qtip-rounded' },
                        position: { viewport: $(window) }
                    });

                    populateClinicalTrialsTable(keywords, 'both');

                    var infoBox = "<img id='drug-summary-help' src='images/help.png' title='"
                            + "These drugs of interest were selected based on the patient's "
                            + "genomic alterations (mutations and copy-number alterations). "
                            + "They do not represent treatment(s) that the patient was enrolled in."
                            + "'>";
                    $(".drugs-summary-table-name").html("" + data.length + " drugs of interest " + infoBox);
                    $("#drug-summary-help").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'qtip-light qtip-rounded' },
                        position: { viewport: $(window) }
                    });

                }
        );
    };

    var clinicalTrialsDataTable = null;
    var populateClinicalTrialsTable = function(keywords, filterBy) {
        if ($("#trials_wait").length==0) return; // deal with clinical trials tab hidden
        
        // Remove all the current rows from the table before population it
        $(".trials-row").remove();
        if(clinicalTrialsDataTable != null) {
            clinicalTrialsDataTable.fnClearTable();
            clinicalTrialsDataTable.fnDestroy();
            $("#pv-trials-table").css({ width: "100%"});
        }
        $("#trials_wait").show();

        var studyTerms = "<%=trialKeywords%>";
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
                        var drugId = drugMap[drugName.toLowerCase()];
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
                    clinicalTrialsDataTable.css("width","100%");

                    // Done with the loading. Hide the image.
                    $("#trials_wait").hide();

                    var infoBox = "<img id='trial-summary-help' src='images/help.png' title='"
                            + "The following clinical trials are listed because they match with "
                            + (filterBy == "both" ? "both the drugs and the cancer type" : (filterBy == "study") ? "the cancer type" : "the drugs")
                            + " of interest. "
                            + "The list does not represent trial(s) that the patient was enrolled in."
                            + "<br/><br/>"
                            + "The data for the clinical trials listed on this page was "
                            + "kindly provided by NCI, Cancer.gov through the content dissemination program."
                            + "'>";

                    $(".trials-summary-table-name").html(data.length + " clinical trials of interest " + infoBox);
                    $("#trial-summary-help").qtip({
                        content: { attr: 'title' },
                        style: { classes: 'qtip-light qtip-rounded' },
                        position: { viewport: $(window) }
                    });

                }
        );
    };

    $(document).ready(function() {
        genomicEventObs.subscribeMutCna(populateDrugTable);

        $("#trial-filtering-options").change(function() {
            populateClinicalTrialsTable(keywords, $("#trial-filtering-options").val());
        });
    });
</script>

  <table id="pv-drugs-table" class="dataTable display" style="width: 100%;">
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

