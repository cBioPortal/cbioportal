<%
    String cancerStudyName = "glioblastoma";
%>
<script type="text/javascript">
    var populateDrugTable = function() {
        var drugIds = [];
        drugIds.push(genomicEventObs.cnas.getDrugIDs());
        drugIds.push(genomicEventObs.mutations.getDrugIDs());

        var keywords = [];
        $.post("drug.json",
                { drug_ids: drugIds },
                function(data) {
                    for(var i=0; i < data.length; i++) {
                        var drug = data[i];
                        $("#pv-drugs-table").append(
                            '<tr>'
                                + '<td>' + drug[2] + '</td>'
                                + '<td>' + drug[1] + '</td>'
                                + '<td>' + drug[4] + '<td>'
                                + '<td>' + (drug[3] ? "yes" : "no") + '</td>'
                                + '<td>' + drug[7] + '</td>'
                            + '</tr>'
                        );

                        keywords.push(drug[2]);
                    }

                    $("pv-drugs-table").dataTable();
                    populateClinicalTrialsTable(keywords);
                }
        );
    };

    var populateClinicalTrialsTable = function(keywords) {
        keywords.push("<%=cancerStudyName%>");
        $.post("clinicaltrials.json",
                { keywords: keywords },
                function(data) {
                    for(var i=0; i < data.length; i++) {
                        var trial = data[i];
                        $("#pv-trials-table").append(
                            '<tr>'
                                + '<td>' + trial[0] + '</td>'
                                + '<td>' + trial[1] + '</td>'
                                + '<td>' + trial[2] + '<td>'
                                + '<td>' + trial[3] + '</td>'
                                + '<td>' + trial[4] + '</td>'
                            + '</tr>'
                        );
                    }

                    $("pv-trials-table").dataTable();
                }
        );
    };

    $(document).ready(function() {
        genomicEventObs.subscribeMutCna(populateDrugTable);
    });
</script>


<h2 class="pv-drugs-header">Drugs of interest</h2>
<table id="pv-drugs-table">
    <tr>
        <th>Drug Name</th>
        <th>Drug Target</th>
        <th>Description</th>
        <th>FDA approved?</th>
        <th>Data Sources</th>
    </tr>
</table>


<h2 class="pv-drugs-header">Clinical trials of interest</h2>
<table id="pv-trials-table">
    <tr>
        <th>Trial ID</th>
        <th>Title</th>
        <th>Status</th>
        <th>Phase</th>
        <th>Location</th>
    </tr>
</table>