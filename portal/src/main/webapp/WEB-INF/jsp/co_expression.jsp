<script type="text/javascript" src="js/src/coExpression.js"></script>
<style>
    #coexp .dataTables_length {
        float: left;
    }
    #coexp .dataTables_paginate {
        float: right;
    }
    #coexp .dataTables_info {
        float: left;
    }
    #coexp .dataTables_filter {
        float: right;
    }
    #coexp .coexp-tabs{
        height: 685px;
    }
    #coexp .coexp-tabs-ref{
        font-size: 11px !important;
    }
</style>
<div class="section" id="coexp">
    <div id="coexp-tabs" class="coexp-tabs">
        <ul id='coexp-tabs-list'></ul>
        <div id='coexp-tabs-content'></div>
    </div>

    <!--div id="co_exp_data_table_div">
        <table id="co_exp_data_table" cellpadding="0" cellspacing="0" border="0" class="display">
        </table>
    </div-->


</div>

<script>
    $(document).ready( function() {
        CoExpTable.initTabs();
        CoExpTable.initSummary();
    });
</script>
