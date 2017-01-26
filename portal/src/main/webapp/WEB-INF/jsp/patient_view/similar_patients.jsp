<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

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
            s.push('<a href="#" onclick="filterMutationsTableByIds(\''+idRegEx(mut)+'\');switchToTab(\'tab_mutations\');return false;">'+mut.length+' mutations</a>');
        }
        if (cna != null) {
            s.push('<a href="#" onclick="filterCnaTableByIds(\''+idRegEx(cna)+'\');switchToTab(\'tab_cna\');return false;">'+cna.length+' copy number alterations</a>');
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
                                var caseId = source[ 0 ];
                                var study = source[ 1 ];
                                return "<a href='"+cbio.util.getLinkToSampleView(study,caseId)+"'>"+caseId+"</a>";
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
                                return "<a href='study?id="+study[0]
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
                "bPaginate": true,
                "sPaginationType": "two_button",
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
        var params = {<%=PatientView.SAMPLE_ID%>:caseIdsStr,cancer_study_id:cancerStudyId};
        if (genomicEventObs.hasMut) {
            params['<%=SimilarPatientsJSON.MUTATION%>'] = genomicEventObs.mutations.getEventIds(true).join(',');
        }
        if (genomicEventObs.hasCna) {
            params['<%=SimilarPatientsJSON.CNA%>'] = genomicEventObs.cnas.getEventIds(true).join(',');
        }
        
        // similar patients
        $.post("similar_patients.json",
            params,
            function (simPatient) {
                buildSimilarPatientsDataTable(simPatient, 'similar_patients_table', '<"H"<"similar-patients-table-name">fr>t<"F"<"datatable-paging"pil>>', 100);
                $('.similar-patients-table-name').html(simPatient.length+" other tumors with similar genomic alterations");
                $('.similar-patients-table-name').addClass("datatable-name");
                $('#similar_patients_wrapper_table').show();
                $('#similar_patients_wait').remove();
            }
            ,"json"
        );
    }
    
    $(document).ready(function(){
        $('#similar_patients_wrapper_table').hide();
        genomicEventObs.subscribeMutCna(ajaxBuildSimilarPatientsDataTable);
    }
    );
</script>

<div id="similar_patients_wait"><img src="images/ajax-loader.gif" alt="loading" /></div>

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