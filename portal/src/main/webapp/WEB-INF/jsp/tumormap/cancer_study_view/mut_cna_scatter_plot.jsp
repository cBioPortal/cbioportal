<style type="text/css">
.large-plot-div {
    width:540px;
    height:400px;
    display:block;
}
</style>

<fieldset style="padding:0px 1px">
    <legend style="color:blue;font-weight:bold;">Mutation Count vs Copy Number Alterations</legend>
    <div id="mut-cna-scatter-plot" class="large-plot-div">
        <img src="images/ajax-loader.gif"/>
    </div>
    <table style="display:none;width:100%;" id="mut-cna-config">
        <tr width="100%">
                <td align="left">
                    H-Axis scale: <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>linear &nbsp;
                    <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-haxis-log"/>log<br/>
                    V-Axis scale: <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>linear &nbsp;
                    <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-vaxis-log"/>log
                </td>
                <td id="case-id-div" align="right">
                </td>
        </tr>
    </table>
</fieldset>