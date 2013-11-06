<style type="text/css" title="currentStyle">
    @import "css/data_table_jui.css";
    @import "css/data_table_ColVis.css";
</style>

<div class="section" id="co_exp" class="plots">
    <div id="user_selection"></div>
    <div id="co_exp_gene_list">
        <table id="co_exp_data_table" cellpadding="0" cellspacing="0" border="0" class="display">
            <thead>
            <tr style="font-size:80%">
                <th>Queried Gene</th>
                <th>Compared Gene</th>
                <th>Pearson Score</th>
                <th>Spearman Score</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Gene1</td>
                <td>TP53</td>
                <td>0.35555</td>
                <td>-0.19</td>
            </tr>
            <tr>
                <td>Gene2</td>
                <td>EGFR</td>
                <td>0.15</td>
                <td>-0.45</td>
            </tr>
            </tbody>
            <tfoot>
            <tr style="font-size:80%">
                <th>Queried Gene</th>
                <th>Compared Gene</th>
                <th>Pearson Score</th>
                <th>Spearman Score</th>
            </tr>
            </tfoot>
        </table>
    </div>
</div>

<script>
    $(document).ready(function() {
        $('#co_exp_data_table').dataTable();
    } );
</script>
