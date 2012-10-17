
<%@ page import="org.mskcc.cbio.portal.servlet.TumorMapServlet" %>

<%
request.setAttribute("tumormap", true);
%>

<jsp:include page="../global/header.jsp" flush="true" />
<!--content start-->
<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
</style>

<script type="text/javascript">
    var tumormap_cancerstudies = null;
    $(document).ready(function(){
        $('#cancer_study_wrapper_table').hide();
        var params = {
            <%=TumorMapServlet.CMD%>:'<%=TumorMapServlet.GET_STUDY_STATISTICS_CMD%>'
        };
                        
        $.post("tumormap.json", 
            params,
            function(data){
                tumormap_cancerstudies = data;
                var ids = [];
                for (var id in data) {
                    ids.push([id]);
                }
                
                var oTable = $('#cancer_study_table').dataTable({
                    "sDom": '<"H"fr>t>',
                    "bJQueryUI": true,
                    "bDestroy": true,
                    "aaData": ids,
                    "aoColumnDefs":[
                        {// study id
                            "aTargets": [0],
                            "bVisible": false,
                            "mData": 0
                        },
                        {// study name
                            "aTargets": [1],
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    var id = source[0];
                                    var name = tumormap_cancerstudies[id]['name'];
                                    if (type==='display') {
                                        return "<a href='study.do?cancer_study_id="+id+"'><b>"+name+"</b></a>";
                                    } else {
                                        return tumormap_cancerstudies[source[0]]['name'];
                                    }
                                }
                            }
                        },
                        {// ref
                            "aTargets": [2],
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    var ref = tumormap_cancerstudies[source[0]]['ref']
                                    return ref ? ref : '';
                                }
                            }
                        },
                        {// cases
                            "aTargets": [3],
                            "sClass": "right-align-td",
                            "bSearchable": false,
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return tumormap_cancerstudies[source[0]]['cases'];
                                }
                            }
                        },
                        {// mut
                            "aTargets": [4],
                            "sClass": "right-align-td",
                            "bSearchable": false,
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    var id = source[0];
                                    var mut = tumormap_cancerstudies[id]['mut'];
                                    if (type==='display') {
                                        if (mut==null) return '<img src="images/ajax-loader2.gif"/>';
                                        return mut ? mut.toFixed(0) : '';
                                    } else {
                                        return mut ? mut : 0.0;
                                    }
                                }
                            }
                        },
                        {// cna
                            "aTargets": [5],
                            "sClass": "right-align-td",
                            "bSearchable": false,
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    var id = source[0];
                                    var cna = tumormap_cancerstudies[id]['cna'];
                                    if (type==='display') {
                                        if (cna==null) return '<img src="images/ajax-loader2.gif"/>';
                                        return cna ? ((cna*100).toFixed(1)+'%') : '';
                                    } else {
                                        return cna ? cna : 0.0;
                                    }
                                }
                            }
                        }
                    ],
                    "aaSorting": [[1,'asc']],
                    "oLanguage": {
                        "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                        "sInfoFiltered": "",
                        "sLengthMenu": "Show _MENU_ per page"
                    },
                    "iDisplayLength": -1
                });
                oTable.css("width","100%");
                $('#cancer_study_wait').hide();
                $('#cancer_study_wrapper_table').show();
                
                updateMutCnaStatistics('mut',4,oTable);
                updateMutCnaStatistics('cna',5,oTable);
            }
            ,"json"
        );
    });
    
    function updateMutCnaStatistics(type, col, oTable) {
        var params = {
            <%=TumorMapServlet.CMD%>:'<%=TumorMapServlet.GET_STUDY_STATISTICS_CMD%>',
            <%=TumorMapServlet.GET_STUDY_STATISTICS_TYPE%>:type
        };
        
        $.post("tumormap.json", 
            params,
            function(data){
                var row = 0;
                for (var id in tumormap_cancerstudies) {
                    $.extend(tumormap_cancerstudies[id],data[id]);
                    oTable.fnUpdate(null,row++,col,false,false);
                }
                oTable.fnDraw();
            }
            ,"json"
        );
    }
    
</script>

<div id="cancer_study_wait"><img src="images/ajax-loader.gif"/> Loading cancer studies</div>
<table cellpadding="0" cellspacing="0" border="0" id="cancer_study_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="cancer_study_table">
                <thead>
                    <tr valign="bottom">
                        <th>Cancer Study ID</th>
                        <th class="cancer-study-header" alt="Cohort cancer studies">Cancer Study</th>
                        <th class="cancer-study-header" alt="Reference for published studies">Ref.</th>
                        <th class="cancer-study-header" alt="Number of cases">Cases</th>
                        <th class="cancer-study-header" alt="Average number of mutations across samples">Avg. # Mut.</th>
                        <th class="cancer-study-header" alt="Average copy-number-altered genome fraction across samples">Avg. CN-Altered Frac.</th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>
<!--content end-->
        </div>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="../global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="../global/xdebug.jsp" flush="true" />

</body>
</html>