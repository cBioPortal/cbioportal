<%@ page import="org.mskcc.portal.servlet.PatientView" %>

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        
        .dataTables_length {
                width: auto;
                float: right;
        }
        .dataTables_info {
                width: auto;
                float: right;
        }
        .div.datatable-paging {
                width: auto;
                float: right;
        }
</style>

<script type="text/javascript">
    
    $(document).ready(function(){
        $('#mutation_wrapper_table').hide();
        var params = {<%=PatientView.PATIENT_ID%>:'<%=patient%>'
        };
                        
        $.post("mutations.json", 
            params,
            function(aDataSet){
                //$("div#protein_exp").html(aDataSet);
                //alert(aDataSet);
                if (aDataSet.length==0)
                    return;
                
                //var aiExclude = [1,2,3,10];
                var oTable = $('#mutation_table').dataTable( {
                        "sDom": '<"H"fr>t<"F"<"datatable-paging"pil>>', // selectable columns
                        "bJQueryUI": true,
                        "bDestroy": true,
                        "aaData": aDataSet,
                        "oLanguage": {
                            "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                            "sInfoFiltered": "",
                            "sLengthMenu": "Show _MENU_ per page"
                        },
                        "iDisplayLength": -1,
                        "aLengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]]
                } );
                
                
                
                // widen the rppa data
                $('#mutation_table').css("width","100%");
                
                $('#mutation_wait').remove();
                $('table#mutation_wrapper_table').show();
            }
            ,"json"
        );
    });
</script>

<div id="mutation_wait"><img src="images/ajax-loader.gif"/></div>

<table cellpadding="0" cellspacing="0" border="0" id="mutation_wrapper_table" width="100%">
        <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="mutation_table">
                <thead style="font-size:80%">
                    <tr valign="bottom">
                        <th>Gene</th>
                        <th>Amino Acid Change</th>
                        <th>Mutaiton Type</th>
                        <th>Mutation Status</th>
                        <th>Clinical Trial</th>
                        <th>Context</th>
                        <th>Notes</th>
                    </tr>
                </thead>
                <tfoot>
                    <tr valign="bottom">
                        <th>Gene</th>
                        <th>Amino Acid Change</th>
                        <th>Mutaiton Type</th>
                        <th>Mutation Status</th>
                        <th>Clinical Trial</th>
                        <th>Context</th>
                        <th>Notes</th>
                    </tr>
                </tfoot>
            </table>
        </td></tr>
</table>