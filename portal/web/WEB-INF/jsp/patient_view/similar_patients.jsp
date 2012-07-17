<%@ page import="org.mskcc.portal.servlet.SimilarPatientsJSON" %>

<%if(showPlaceHoder){%>
A genomic overview with events aligned across patients goes here...
<%}%>

<script type="text/javascript">
    function renderSharedEvents(events) {
        var mut = events['<%=SimilarPatientsJSON.MUTATION%>'];
        var cna = events['<%=SimilarPatientsJSON.CNA%>'];
        var s = [];
        if (mut != null) {
            s.push('<a href="#" onclick="filterMutationsTableByIds(\''+idRegEx(mut)+'\');switchToTab(\'mutations\');return false;">'+mut.length+' mutations</a>');
        }
        if (cna != null) {
            s.push('<a href="#" onclick="filterCnaTableByIds(\''+idRegEx(cna)+'\');switchToTab(\'cna\');return false;">'+cna.length+' copy number alterations</a>');
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
                                + (<%=(isDemoMode==null)%>?"":"&demo=<%=isDemoMode%>")+"'><b>"+patientId+"</b></a>";
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
    
    function ajaxBuildSimilarPatientsDataTable() {
        var params = {<%=PatientView.PATIENT_ID%>:'<%=patient%>'};
        if (geObs.hasMut) {
            params['<%=SimilarPatientsJSON.MUTATION%>'] = overviewMutEventIds;
        }
        if (geObs.hasCna) {
            params['<%=SimilarPatientsJSON.CNA%>'] = overviewCnaEventIds;
        }
        
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
    
    $(document).ready(function(){
        $('#similar_patients_wrapper_table').hide();
        geObs.subscribeMutCna(ajaxBuildSimilarPatientsDataTable);
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
                        <th>Shared Events of Interest</th>
                        <th># Shared Events</th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>