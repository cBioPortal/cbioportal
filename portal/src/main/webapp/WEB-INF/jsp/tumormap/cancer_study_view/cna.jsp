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


<%@ page import="org.mskcc.cbio.portal.servlet.GisticJSON" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<script type="text/javascript">   
    $(document).ready(function(){
        $('#gistic_wrapper_table').hide();
        loadGisticData(cancerStudyId);
    });
    
    function loadGisticData(cancerStudyId) {
        var params = {<%=GisticJSON.SELECTED_CANCER_STUDY%>: cancerStudyId};
        $.get("Gistic.json",
            params,
            function(data){
                if (data==null || (typeof data) != (typeof []) || data.length==0) {
                    $("#gistic-msg").html("Gistic data is not available for this cancer study.");
                    return;
                }
                $("#gistic-msg").hide();
                
                var ix = [];
                var n = data.length;
                for (var i=0; i<n; i++) {
                        ix.push([i]);
                }
                
                var oTable = $('#gistic_table').dataTable( {
                    "sDom": '<"H"<"gistic-table-name">fr>t<"F"<"datatable-paging"pil>>', // selectable columns
                    "bJQueryUI": true,
                    "bDestroy": true,
                    "aaData": ix,
                    "aoColumnDefs":[
                        {// data
                            "aTargets": [ 0 ],
                            "bVisible": false,
                            "mData" : 0
                        },
                        {// amp/del
                            "aTargets": [ 1 ],
                            "sClass": "center-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    if (data[source[0]]['ampdel'])
                                        return "<font color='red'>AMP</font>";
                                    else
                                        return "<font color='blue'>DEL</font>";
                                } else {
                                    return data[source[0]]['ampdel'];
                                }
                            }
                        },
                        {// chr
                            "aTargets": [ 2 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return data[source[0]]['chromosome'];
                                }
                            }
                        },
                        {// cytoband
                            "aTargets": [ 3 ],
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return data[source[0]]['cytoband'];
                                }
                            }
                        },
                        {// #gene
                            "aTargets": [ 4 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return data[source[0]]['sangerGenes'].length
                                        + data[source[0]]['nonSangerGenes'].length;
                                }
                            }
                        },
                        {// gene
                            "aTargets": [ 5 ],
                            "iDataSort": 4,
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    var genes = data[source[0]]['sangerGenes'].slice(0).concat(data[source[0]]['nonSangerGenes']);
                                   if (type==='display') {
                                        var linkedGenes = [];
                                        for (var i=0; i<genes.length; i++) {
                                            linkedGenes.push(genes[i]);
                                        }
                                        return linkedGenes.join(" ");
                                    } else {
                                        return genes.join(" ");
                                    }
                                }
                            }
                        },
                        {// qval
                            "aTargets": [ 6 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    return cbio.util.toPrecision(data[source[0]]['qval'], 3, 0.01);
                                } else {
                                    return data[source[0]]['qval'];
                                }
                            },
                            "asSorting": ["asc", "desc"]
                        }
                    ],
                    "aaSorting": [[6,'asc']],
                    "oLanguage": {
                        "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                        "sInfoFiltered": "",
                        "sLengthMenu": "Show _MENU_ per page"
                    },
                    "iDisplayLength": 25,
                    "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
                });
                
                oTable.css("width","100%");
                
                $('.gistic-table-name').html(n+" copy number alteration peaks by <a href='http://www.broadinstitute.org/cancer/pub/GISTIC2/'>GISTIC2</a>");
                $('.gistic-table-name').addClass('data-table-name');
                
                $('#gistic_wrapper_table').show();
            });
    }
</script>

<div id="gistic-msg"><img src="images/ajax-loader.gif"/></div><br/>
<table cellpadding="0" cellspacing="0" border="0" id="gistic_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="gistic_table">
                <thead>
                    <tr valign="bottom">
                        <th><b>data</b></th>
                        <th><b><font color="red">AMP</font>/<font color="blue">DEL</font></b></th>
                        <th><b>Chr</b></th>
                        <th><b>Cytoband</b></th>
                        <th><b># Genes</b></th>
                        <th><b>Genes</b></th>
                        <th><b>Q-value</b></th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>