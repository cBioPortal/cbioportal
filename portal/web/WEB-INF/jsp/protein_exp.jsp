<%@ page import="org.mskcc.portal.servlet.ProteinArraySignificanceTestJSON" %>

<link href="css/data_table.css" type="text/css" rel="stylesheet"/>

<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script> 

<script type="text/javascript">
    $(document).ready(function(){
        var heat_map = $("textarea#heat_map").html();
        
        $.post("ProteinArraySignificanceTest.json", 
            {<%=ProteinArraySignificanceTestJSON.HEAT_MAP%>:heat_map
            },
            function(aDataSet){
                //$("div#protein_exp").html(data);
                //alert(data);
                $('div#protein_exp').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="protein_expr"></table>' );
                $('table#protein_expr').dataTable( {
                        "aaData": aDataSet,
                        "aoColumns": [
                                { "sTitle": "Type" },
                                { "sTitle": "Target Gene" },
                                { "sTitle": "Target Residue" },
                                { "sTitle": "Source organism" },
                                { "sTitle": "Validated?"},
                                { "sTitle": "p-value"
                                }
                        ]
                } );
            },
            "json"
        );
    });
</script>

<div class="section" id="protein_exp">
    <img src="images/ajax-loader.gif"/>
</div>