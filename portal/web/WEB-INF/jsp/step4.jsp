<%@ page import="org.mskcc.portal.util.GeneSetUtil" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.Utilities" %>


<script type="text/javascript">

    //  Update the gene list to reflect user's selection
    function updateGeneSet() {

        //  Set up the array to store all gene list
        var geneSetArray = new Array();
    <%
        int i = 0;
        for (GeneSet geneSet:  geneSetList) {
            out.println ("geneSetArray['" + geneSet.getId() +"']='" + geneSet.getGeneList() + "';");
            i++;
        }
    %>

    var geneSetChoice = YAHOO.util.Dom.get("<%= QueryBuilder.GENE_SET_CHOICE %>");
    var value = geneSetChoice.value;
    var geneList = geneSetArray[value];
    var geneListTextArea = YAHOO.util.Dom.get("<%= QueryBuilder.GENE_LIST %>");
    //alert('geneSetChoice: ' + geneSetChoice + ' value: ' + value + ' geneList:  ' + geneList);
    geneListTextArea.value = geneList;
}
</script>
<table>
<tr>
    <td><span class="step">Enter Gene Set:</span></td>
</tr>
</table>
<%
   String step4ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP4_ERROR_MSG);
   out.println("<table FRAME=void WIDTH=100% > <tr> <td ALIGN=left><P>Enter gene symbols below:</p></td>");
   %>

    <script language="javascript" type="text/javascript">
    <!--
    function popitup(url) {
        newwindow=window.open(url,'OncoSpecLangInstructions','height=1000,width=1000,left=400,top=0,scrollbars=yes');
        if (window.focus) {newwindow.focus()}
        return false;
    }

    // -->
    </script>

    <td ALIGN=right><P> <A href="onco_query_lang_desc.jsp" onclick="return popitup('onco_query_lang_desc.jsp')">Advanced:  Onco Query Language (OQL)</A></p></td> </tr> </table>
   <%

   if (step4ErrorMsg != null) {
      out.println("<span class='error'>" + step4ErrorMsg + "</span></p>");
   }

   // display text area
   out.println("<textarea rows='10' cols='80' id='" + QueryBuilder.GENE_LIST + "' name='"
            + QueryBuilder.GENE_LIST + "'>");

    //  Output Gene Set from User
    String inputGeneList = "";
    if (geneSetChoice.equals("user-defined_list")) {
     if (geneList != null && geneList.trim().length() > 0) {
        inputGeneList = geneList;
     }
    } else {
     for (GeneSet geneSet : geneSetList) {
        if (geneSetChoice.equals(geneSet.getId())) {
           inputGeneList = geneSet.getGeneList();
        }
     }
    }
    out.println(Utilities.appendSemis(inputGeneList));
    out.print("</textarea>");
%>
<table>
<tr valign="top">
<td>
<P>Or Select from Example Gene Sets:</P> 
<!--</td>
<td>--><P><select onchange="JavaScript:updateGeneSet()" id="<%= QueryBuilder.GENE_SET_CHOICE %>"
              name="<%= QueryBuilder.GENE_SET_CHOICE %>">

    <%
        for (GeneSet geneSet:  geneSetList) {
            String selected = "";
            if (geneSetChoice.equals(geneSet.getId())) {
                selected = " selected ";
            }
            out.print("<option " + selected + " value='" + geneSet.getId() + "'>");
            out.println (geneSet.getName() + "</option>");
        }
    %>
</select>
</P>    
</td>
</tr>
</table>
