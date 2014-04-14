
<%@ page import="org.mskcc.cbio.portal.servlet.GisticJSON" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<div id="gistic-msg"><img src="images/ajax-loader.gif"/></div><br/>
<table cellpadding="0" cellspacing="0" border="0" id="gistic_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="gistic_table">
                <thead>
                    <tr valign="bottom">
                        <th><b>data</b></th>
                        <th><b><font color="red">AMP</font>/<font color="blue">DEL</font></b></th>
                        <th><b>Chr</b></th>
                        <th><b>Cytoband</b></th>
                        <th><b># Genes</b></th>
                        <th><b>Genes</b></th>
                        <th><b>Q-value</b></th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>