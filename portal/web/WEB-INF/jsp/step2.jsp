<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.ZScoreUtil" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.portal.model.GeneticProfile" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.ArrayList" %>

<%
    String step2ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP2_ERROR_MSG);
    double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
    String mRNAProfileSelected = request.getParameter(QueryBuilder.MRNA_PROFILES_SELECTED);
    if (mRNAProfileSelected != null && mRNAProfileSelected.equals("on")) {
        mRNAProfileSelected = " checked ";
    }
%>

<%
    String step2Header = "Select Genetic Profiles:";
    if (tabIndex.equals(QueryBuilder.TAB_DOWNLOAD)) {
        step2Header = "Select Genetic Profile:";
    }
%>
<TABLE>
<tr>
    <tr>
    <td><img class="step_image" src="images/step_2<%=stepImageSuffix%>.png" alt="Step 2:"></td>
    <td><span class="step">Select Genomic Profiles:</span></td>
</tr>
</TABLE>
<%
    if (profileList.size() == 0) {
        out.println ("<p><div class='error'>No profiles available for selected cancer type.</div></p>");    
    }
    if (step2ErrorMsg != null) {
        out.println ("<p><div class='error'>" + step2ErrorMsg + "</div></p>");
    }
%>

<%
    ArrayList <GeneticProfile> finalProfileList = new ArrayList <GeneticProfile>();
    ArrayList <GeneticProfile> mRNAProfileList = new ArrayList <GeneticProfile>();

    //  First pass through all profiles
    for (int i = 0; i < profileList.size(); i++) {
        boolean showProfile = true;
        boolean isMRNAProfile = false;
        GeneticProfile profile = profileList.get(i);
        if (tabIndex.equals(QueryBuilder.TAB_VISUALIZE)) {
            if (profile.getAlterationType() == GeneticAlterationType.MRNA_EXPRESSION) {
                isMRNAProfile = true;
            }
            if (profile.showProfileInAnalysisTab() == false) {
                showProfile = false;
            }
        }
        if (showProfile) {
            if (isMRNAProfile) {
                mRNAProfileList.add(profile);
            } else {
                finalProfileList.add(profile);
            }
        }
    }

    outputProfileList(out, geneticProfileIdSet, finalProfileList, tabIndex, false);
    if (mRNAProfileList.size() > 0) {
            boolean indentmRNA = false;
            out.println ("<div id=\"mRNAcheckbox\">");
            if (mRNAProfileList.size() > 1) {
                out.println ("<table><tr><td>");
                out.println("<input type='checkbox' onclick='toggleMRNAProfile()' name='"
                        + QueryBuilder.MRNA_PROFILES_SELECTED + "'" + mRNAProfileSelected + ">");
                out.println ("<b>Gene Expression data</b>.  Select one of the profiles below:");

                out.println ("</td></tr></table>");
                indentmRNA = true;
            }
            outputProfileList(out, geneticProfileIdSet, mRNAProfileList, tabIndex, indentmRNA);
        out.println ("</div>");
            out.println ("<div class=\"toggler\">");
            out.println ("<div id=\"threshold\">");
            out.println ("<P>Enter a z-score threshold &#177:  ");
            out.println ("<input type='text' name='" + QueryBuilder.Z_SCORE_THRESHOLD
                + "' size='6' value='" + zScoreThreshold + "'></P>");
            out.println ("</div></div>");
    }
    finalProfileList.addAll(mRNAProfileList);

    if (finalProfileList.size() ==0) {
        out.println ("<div id=\"cancer_type_desc\">");
        out.println ("<P>This cancer type does not have any genomic profiles available for query.  "
            + "Try checking the Download Data Tab.");
        out.println ("</div>");
    }
%>
</p>







<script type="text/javascript">
    function toggleMRNA() {
        //  When user selects an MRNA Radio Button, auto-check the mRNA checkbox
        checkbox1 = document.getElementsByName("<%= QueryBuilder.MRNA_PROFILES_SELECTED %>")[0];
        checkbox1.checked = true;
    }
    function toggleMRNAProfile() {

        //  Keep track of mRNA Genetic Profiles
        var profile_list = new Array();
        <% for (int i=0; i<finalProfileList.size(); i++) {
            GeneticProfile geneticProfile = finalProfileList.get(i);
            int isMRNAProfile = 0;
            if (geneticProfile.getAlterationType()==GeneticAlterationType.MRNA_EXPRESSION) {
                isMRNAProfile = 1;
            }
            %>
            profile_list[<%= i%>] = <%= isMRNAProfile%>;
        <% } %>

        radio_list = document.getElementsByName("<%= QueryBuilder.GENETIC_PROFILE_IDS %>");
        //  When the user unchecks the mRNA checkbox, deselect all mRNA radio buttons
        for (i=0; i<radio_list.length; i++) {
            radio = radio_list[i]
            //  Only uncheck mRNA profiles
            if (profile_list[i] == 1) {
                radio.checked = false;
            }
        }
    }
</script>
<%!
    private void outputProfileList(JspWriter out, HashSet<String> geneticProfileIdSet,
            ArrayList<GeneticProfile> profileList,
            String tabIndex, boolean indent) throws IOException {
        out.println("<div class='tooltips' id='genomic_profiles'><table>");
        for (int i = 0; i < profileList.size(); i++) {
            GeneticProfile profile = profileList.get(i);
            String checked = "";
            if (geneticProfileIdSet.contains(profile.getId())) {
                checked = " checked ";
            }
            String type = "checkbox";
            if (tabIndex.equals(QueryBuilder.TAB_DOWNLOAD)) {
                type = "radio";
            }
            out.println("<tr>");
            if (indent) {
                out.println("<td width=10>&nbsp;</td>");
            }
            out.println("<td>");
            if (indent) {
                type = "radio";
                out.println("<input type='" + type + "' " + checked + " name='"
                        + QueryBuilder.GENETIC_PROFILE_IDS + "' value='" +
                        profile.getId() + "' onclick='toggleMRNA()'>");
            } else {
                out.println("<input type='" + type + "' " + checked + " name='"
                        + QueryBuilder.GENETIC_PROFILE_IDS + "' value='" +
                        profile.getId() + "'>");
            }
            out.println("<b>" + profile.getName().trim() + "</b>  ");
            out.println("<img src='images/help.png' class=\"Tips1\" title=\"");
            if (profile.getDescription().endsWith(".")) {
                out.println(profile.getDescription());
            } else {
                out.println(profile.getDescription() + ".");
            }
            out.println("\"></input>");
            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table></div>");
    }
%>
<script>
    window.addEvent('domready', function(){
                /* Tips 1 */
                var Tips1 = new Tips($$('.Tips1'));
    });
</script>
