
<%@ page import="org.mskcc.cbio.portal.servlet.MutSigJSON" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>  

<div id="mut-sig-msg"><img src="images/ajax-loader.gif"/></div><br/>
<table cellpadding="0" cellspacing="0" border="0" id="smg_wrapper_table" width="60%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="smg_table">
                <thead>
                    <tr valign="bottom">
                        <th><b>data</b></th>
                        <th><b>Gene</b></th>
                        <th><b>Cytoband</b></th>
                        <th><b>Gene size (Nucleotides)</b></th>
                        <th><b># Mutations</b></th>
                        <th><b># Mutations / Nucleotide</b></th>
                        <th><b>Mutsig Q-value</b></th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>