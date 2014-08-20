<script type="text/template" id="main-controls-template">
    <style>
        .onco-customize {
        color:#2153AA; font-weight: bold; cursor: pointer;
        }
        .onco-customize:hover { text-decoration: underline; }
    </style>
    <p onclick="$('#oncoprint_controls #main').toggle(); $('#oncoprint_controls .triangle').toggle();"
    style="margin-bottom: 0px;">
    <span class="triangle ui-icon ui-icon-triangle-1-e" style="float: left; display: block;"></span>
        <span class="triangle ui-icon ui-icon-triangle-1-s" style="float: left; display: none;"></span>
        <span class='onco-customize'>Customize</span>
    </p>

    <div id="main" style="display:none;">
    <table style="padding-left:13px; padding-top:5px">
    <tr>
                <td style="padding-right: 15px;"><span>Zoom</span><div id="zoom" style="display: inline-table;"></div></td>
                <td><label><input id='toggle_unaltered_cases' type='checkbox'>Remove Unaltered Cases</label></td>
                <td><label><input id='toggle_whitespace' type='checkbox'><label>Remove Whitespace</label></td>
            </tr>
            <tr>
                <td>
                    <div id="disable_select_clinical_attributes" style="display: none; z-index: 1000; opacity: 0.7; background-color: grey; width: 22.5%; height: 6%; position: absolute;"></div>
                    <select data-placeholder="add clinical attribute track" id="select_clinical_attributes" style="width: 350px;">
    <option value=""></option>
                    </select>
                </td>
                <td>
                    <span>Sort by: </span>
                    <select id="sort_by" style="width: 200px;">
    <option value="genes">gene data</option>
                        <option value="clinical" disabled>clinical data</option>
                        <option value="alphabetical">alphabetically by case id</option>
                        <option value="custom">user-defined case list / default</option>
                    </select>
                </td>
            </tr>
        </table>
    </div>
</script>

<script type="text/template" id="custom-controls-template">
    <style>
        .onco-customize {
        color:#2153AA; font-weight: bold; cursor: pointer;
    }
    .onco-customize:hover { text-decoration: underline; }
    </style>
    <p onclick="$('#oncoprint_controls #main').toggle(); $('#oncoprint_controls .triangle').toggle();"
       style="margin-bottom: 0px;">
        <span class="triangle ui-icon ui-icon-triangle-1-e" style="float: left; display: block;"></span>
        <span class="triangle ui-icon ui-icon-triangle-1-s" style="float: left; display: none;"></span>
        <span class='onco-customize'>Customize</span>
    </p>

    <div id="main" style="display:none;">
        <table style="padding-left:13px; padding-top:5px">
            <tr>
                <td style="padding-right: 15px;"><span>Zoom&nbsp;</span><div id="zoom" style="display: inline-table;"></div></td>
            </tr>
            <tr>
                <td><label><input id='toggle_unaltered_cases' type='checkbox'>Remove Unaltered Cases</label></td>
            </tr>
            <tr>
                <td><label><input id='toggle_whitespace' type='checkbox'>Remove Whitespace</label></td>
            </tr>
            <%--<tr>
                <td><label><input id='all_cna_levels' type='checkbox' >Show All CNA levels</label></td>
            </tr>--%>
            <%--<td>--%>
            <%--<span>Sort by: </span>--%>
            <%--<select id="sort_by" style="width: 200px;">--%>
            <%--<option value="genes">gene data</option>--%>
            <%--<option value="alphabetical">alphabetically by case id</option>--%>
            <%--<option value="custom">user-defined case list / default</option>--%>
            <%--</select>--%>
            <%--</td>--%>
        </table>
    </div>
</script>
