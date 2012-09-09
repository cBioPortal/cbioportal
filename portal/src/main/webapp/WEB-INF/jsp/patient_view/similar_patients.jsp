<%@ page import="org.mskcc.cbio.portal.servlet.SimilarPatientsJSON" %>

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
    
    function countSharedEvents(events) {
        var mut = events['<%=SimilarPatientsJSON.MUTATION%>'];
        var cna = events['<%=SimilarPatientsJSON.CNA%>'];
        var n = 0;
        if (mut != null) {
            n += mut.length;
        }
        if (cna != null) {
            n += cna.length;
        }
        return n;
    }
    
    function buildSimilarPatientsDataTable(aDataSet, table_id, sDom, iDisplayLength) {
        var oTable = $('#'+table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": aDataSet,
                "aoColumnDefs":[
                    {// patient
                        "aTargets": [ 0 ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[0]=value;
                            } else if (type==='display') {
                                var patientId = source[ 0 ];
                                return "<a href='patient.do?<%=PatientView.PATIENT_ID%>="+patientId
                                    + (<%=(isDemoMode==null)%>?"":"&demo=<%=isDemoMode%>")+"'><b>"+patientId+"</b></a>";
                            } else {
                                return source[0];
                            }
                        }
                    },
                    {// study
                        "aTargets": [ 1 ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[1]=value;
                            } else if (type==='display') {
                                var study = source[ 1 ];
                                return "<a href='study.do?cancer_study_id="+study[0]
                                    + (<%=(isDemoMode==null)%>?"":"&demo=<%=isDemoMode%>")+"'><b>"+study[1]+"</b></a>";
                            } else if (type==='sort') {
                                return source[1][1];
                            } else {
                                return source[1];
                            }
                        }
                    },
                    {// Shared events
                        "aTargets": [ 2 ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[2]=value;
                            } else if (type==='display') {
                                return renderSharedEvents(source[2]);
                            } else if (type==='sort') {
                                var n = '' + countSharedEvents(source[2]);
                                var pad = '000000';
                                return pad.substring(0, pad.length - n.length) + n;
                            } else if (type==='filter') {
                                return renderSharedEvents(source[2]);
                            } else {
                                return source[2];
                            }
                        }
                    }
                ],
                "aaSorting": [[2,'desc']],
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

        oTable.css("width","100%");
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
                buildSimilarPatientsDataTable(simPatient, 'similar_patients_table', '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
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
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>