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
<div class="section" id="co_exp">
    <div id="user_selection"></div>
    <div id="co_exp_gene_list">
        <table id="co_exp_data_table" cellpadding="0" cellspacing="0" border="0" class="display">
            <thead style="font-size:70%;" >
            <tr>
                <th>Queried Gene</th>
                <th>Compared Gene</th>
                <th>Pearson's Score</th>
                <th>Spearman's Score</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>TP53</td>
                <td>EGFR</td>
                <td>0.35135123</td>
                <td>-0.144123</td>
            </tr>
            <tr>
                <td>BRCA1</td>
                <td>EGFR</td>
                <td>-0.152735123</td>
                <td>0.172323</td>
            </tr>
            <tr>
                <td>CCNE1</td>
                <td>EGFR</td>
                <td>0.351235123</td>
                <td>-0.1172323</td>
            </tr>
            <tr>
                <td>ERBB2</td>
                <td>EGFR</td>
                <td>-0.3192</td>
                <td>0.283723</td>
            </tr>

            </tbody>
        </table>
    </div>
</div>

<script>
    $(document).ready(function() {
        $('#co_exp_data_table').dataTable({
            "sDom": '<"H"if>t<"F"lp>',
            "sPaginationType": "full_numbers",
            "bJQueryUI": true,
            "bAutoWidth": false
        });
    });
</script>
