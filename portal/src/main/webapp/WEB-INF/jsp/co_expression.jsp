<style type="text/css" title="coexpTableStyle">
    #co_exp .dataTables_length {
        float: left;
    }
    #co_exp .dataTables_paginate {
        float: right;
    }
    #co_exp .dataTables_info {
        float: left;
    }
    #co_exp .dataTables_filter {
        float: right;
    }
</style>
<script type="text/javascript" src="js/src/coExpression.js"></script>
<div class="section" id="co_exp">
    <div id='co-exp-loading-image'>
    	<img style='padding:200px;' src='images/ajax-loader.gif'>
    </div>
    <div id="co_exp_data_table_div">
        <table id="co_exp_data_table" cellpadding="0" cellspacing="0" border="0" class="display">
        </table>
    </div>
</div>

<script>
    $(document).ready(function() {
        CoExpTable.init();
    });
</script>
