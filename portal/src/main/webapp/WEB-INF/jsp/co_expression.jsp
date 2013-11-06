<style type="text/css" title="coexpTableStyle">
    @import "css/data_table_jui.css";
    .co_exp_table_paging {
        width: 70%;
        float: right;
    }
    .co_exp_table_info {
        float: left;
    }
</style>
<div class="section" id="co_exp" class="plots">
    <div id="user_selection"></div>
    <div id="co_exp_gene_list">
        <table id="co_exp_data_table" cellpadding="0" cellspacing="0" border="0" class="display">
            <thead style="font-size:80%">
            <tr>
                <th>Column 1</th>
                <th>Column 2</th>
                <th>etc</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>            <tr>
                <td>Row 1 Data 1</td>
                <td>Row 1 Data 2</td>
                <td>etc</td>
            </tr>
            <tr>
                <td>Row 2 Data 1</td>
                <td>Row 2 Data 2</td>
                <td>etc</td>
            </tr>
            </tbody>
        </table>
    </div>

</div>

<script>
    $(document).ready(function() {
        $('#co_exp_data_table').dataTable({
            "sDom": '<"H"<"co_exp_table_paging"i><"co_exp_table_paging"lfp>>t',
            "sPaginationType": "full_numbers",
            "bJQueryUI": true
        });
    });
</script>
