<%@ page import="org.mskcc.portal.servlet.SimilarPatientsJSON" %>

<%if(showPlaceHoder){%>
A genomic overview with events aligned across patients goes here...
<%}%>

<script type="text/javascript">
    function getEventIdString(eventTableData) {
        var s = [];
        for (var i=0; i<eventTableData.length; i++) {
            s.push(eventTableData[i][0]);
        }
        return s.join(" ");
    }
    
    function renderSharedEvents(events) {
        var mut = events['<%=SimilarPatientsJSON.MUTATION%>'];
        var cna = events['<%=SimilarPatientsJSON.CNA%>'];
        var s = [];
        if (mut != null) {
            s.push(''+mut.length+' mutations');
        }
        if (cna != null) {
            s.push(''+cna.length+' copy number alterations')
        }
        return s.join("<br/>");
    }
    
    function buildSimilarPatientsDataTable(aDataSet, table_id, sDom, iDisplayLength) {
        var oTable = $(table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": aDataSet,
                "aoColumnDefs":[
                    {// patient
                        "aTargets": [ 0 ],
                        "fnRender": function(obj) {
                            var patientId = obj.aData[ obj.iDataColumn ];
                            return "<a href='patient.do?patient="+patientId
                                + (<%=(isDemoMode==null)%>?"":"&demo=<%=isDemoMode%>")+"'>"+patientId+"<a>";
                        }
                    },
                    {// Shared events
                        "aTargets": [ 2 ],
                        "iDataSort": 3,
                        "fnRender": function(obj) {
                            return renderSharedEvents(obj.aData[ obj.iDataColumn ]);
                        }
                    },
                    {// # Shared events
                        "bVisible": false,
                        "aTargets": [ 3 ]
                    }
                ],
                "aaSorting": [[3,'desc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page"
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        // help
        //$('.mutations_help').tipTip();

        $(table_id).css("width","100%");
        return oTable;
    }
        
    var mutations_built = false;
    var cna_built = false;
    
    var params = {<%=PatientView.PATIENT_ID%>:'<%=patient%>'};
            
    function waitAndBuildSimilarPatientsDataTable() {
        if (mutations_built==<%=showMutations%> && cna_built==<%=showCNA%>) {
                // similar patients
                $.post("similar_patients.json",
                    params,
                    function (simPatient) {
                        buildSimilarPatientsDataTable(simPatient, '#similar_patients_table', '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
                        $('#similar_patients_wrapper_table').show();
                        $('#similar_patients_wait').remove();
                    }
                    ,"json"
                );
        }
    }
    
    $(document).ready(function(){
        $('#similar_patients_wrapper_table').hide();
        $('#similar_patients_table').live('mutations-built', function() {
            var mutationsTable = $('#mutation_table').dataTable();
            var mutationsTableData = mutationsTable.fnGetData();
            params['<%=SimilarPatientsJSON.MUTATION%>'] = getEventIdString(mutationsTableData);
            mutations_built = true;
            waitAndBuildSimilarPatientsDataTable();
        });
        $('#similar_patients_table').live('cna-built', function() {
            var cnaTable = $('#cna_table').dataTable();
            var cnaTableData = cnaTable.fnGetData();
            params['<%=SimilarPatientsJSON.CNA%>'] = getEventIdString(cnaTableData);
            cna_built = true;
            waitAndBuildSimilarPatientsDataTable();
        });
    }
    );
</script>

<div id="similar_patients_wait"><img src="images/ajax-loader.gif"/></div>

<table cellpadding="0" cellspacing="0" border="0" id="similar_patients_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="similar_patients_table">
                <thead>
                    <tr valign="bottom">
                        <th>Patient</th>
                        <th>Cancer Study</th>
                        <th>Shared Events</th>
                        <th># Shared Events</th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>