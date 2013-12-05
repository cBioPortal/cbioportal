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
    <div id="user_selection"></div>
    <div id="co_exp_data_table_div">

        <table id="co_exp_data_table" cellpadding="0" cellspacing="0" border="0" class="display">
            <thead style="font-size:70%;" >
            <tr>
                <th>Queried Gene</th>
                <th>Compared Gene</th>
                <th>Pearson's Score</th>
            </tr>
            </thead>
            <tbody></tbody>
        </table>

    </div>
</div>

<script>
    $(document).ready(function() {
        CoExpTable.init();
    });
</script>
