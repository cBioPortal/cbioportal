<fieldset style="padding:0px 1px">
    <legend style="color:blue;font-weight:bold;">Mutation Count VS. Copy Number Alteration</legend>
    <div style="display:none">
        <form name="input" action="patient.do" method="get">
            <select id="case-select" name="<%=PatientView.PATIENT_ID%>"><option id="null_case_select"></option></select>
            <input type="submit" id="submit-patient-btn" value="More About This Case" />
        </form>
    </div>
    <div id="mut-cna-scatter-plot" class="large-plot-div">
        <img src="images/ajax-loader.gif"/>
    </div>
    <table style="display:none;width:100%;" id="mut-cna-config">
        <tr width="100%">
                <td>
                    H-Axis scale: <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>Normal &nbsp;
                    <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-haxis-log"/>log<br/>
                    V-Axis scale: <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>Normal &nbsp;
                    <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-vaxis-log"/>log
                </td>
                <td id="case-id-div" align="right">
                </td>
        </tr>
    </table>
</fieldset>