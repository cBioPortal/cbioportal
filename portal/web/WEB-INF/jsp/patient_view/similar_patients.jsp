<%@ page import="org.mskcc.portal.servlet.SimilarPatientsJSON" %>

<%if(showPlaceHoder){%>
A genomic overview with events aligned across patients goes here...
<%}%>

<script type="text/javascript">
    function getMutationsString(mutationsTableData) {
        var s = [];
        for (var i=0; i<mutationsTableData.length; i++) {
            s.push(mutationsTableData[i][0]+":"+mutationsTableData[i][1]);
        }
        return s.join(",");
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
                                + (<%=(isDemoMode==null)%>?"":"&demo=<%=isDemoMode%>")+"'>"+patientId+"<a>";;
                        }
                    },
                    {// # Shared Mutations
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
    
    $(document).ready(function(){
        $('#mutation_wrapper_table').hide();
        $('#similar_patients_table').live('mutations-built', function() {
            if (<%=showMutations%>) {
                var mutationsTable = $('#mutation_summary_table').dataTable();
                var mutationsTableData = mutationsTable.fnGetData();
                var strMutations = getMutationsString(mutationsTableData);
                
                // similar patients
                var params = {<%=PatientView.PATIENT_ID%>:'<%=patient%>',
                    <%=SimilarPatientsJSON.MUTATIONS%>:strMutations
                };
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
                        <th>Shared Mutations</th>
                        <th># Shared Mutations</th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>