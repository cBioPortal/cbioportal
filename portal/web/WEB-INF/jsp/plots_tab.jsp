<%@ page import="org.mskcc.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.portal.servlet.GeneratePlots" %>
<%@ page import="org.mskcc.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cgds.model.GeneticAlterationType" %>

<script type="text/javascript">
$(document).ready(function() {

     $('#plot_it').click(function(){
        var cancer_study_id = $('#plots input:hidden:eq(0)').val();
        var case_set_id =  $('#plots input:hidden:eq(1)').val();
        var case_ids =  $('#plots input:hidden:eq(2)').val();
        var normal_case_set_id =  $('#plots input:hidden:eq(3)').val();
        var gene = $('select[name="gene"] option:selected').val();
        var mutation_profile_id = $('select[name="mutation_profile_id"] option:selected').val();
        var mrna_profile_id = $('select[name="mrnra_profile_id"] option:selected').val();
        var cna_profile_id = $('select[name="cna_profile_id"] option:selected').val();
        var methylation_profile_id = $('select[name="methylation_profile_id"] option:selected').val();
        var rppa_protein_profile_id = $('select[name="rppa_protein_profile_id"] option:selected').val();
        var plot_type = $('select[name="plot_type"] option:selected').val();
        var includeNormals = $('input:checkbox[name=include_normals]:checked').val();

        var toLoad = "generatePlots.do?cancer_study_id="+cancer_study_id+
                "&case_set_id="+case_set_id+
                "&case_ids="+encodeURIComponent(case_ids)+
                "&gene="+gene+
                "&mutation_profile_id="+mutation_profile_id+
                "&mrnra_profile_id="+mrna_profile_id+
                "&cna_profile_id="+cna_profile_id+
                "&normal_case_set_id="+normal_case_set_id+
                "&include_normals="+includeNormals;
                if(methylation_profile_id){
                    toLoad = toLoad+"&methylation_profile_id="+methylation_profile_id;
                }
                if(rppa_protein_profile_id){
                    toLoad = toLoad+"&rppa_protein_profile_id="+rppa_protein_profile_id;
                }
                toLoad = toLoad + "&plot_type="+plot_type;

                $('#load').remove();
         //hide div that will contain html page
         //show ajax loader image
        $('#progress_bar').append('<div id="load">&nbsp;</div>');
        $('#load').fadeIn('slow');
        $('#plot_images').hide('fast',loadContent);

        function loadContent() {
            //load html content into div (while still hidden)
            //call showNewContent only after content is finished loading
            $('#plot_images').load(toLoad,'',showNewContent());
        }
        function showNewContent() {
            //show content, hide loader only after content is shown
            $('#plot_images').delay(500).fadeIn('slow',hideLoader());
        }
        function hideLoader() {
            //hide loader image
            $('#load').fadeOut('fast');
        }
        return false;

     });

     var dropdowns = $('#plots select');

     $.each(dropdowns, function(){
         var num = $('option', this).length;
         var position = $(this).position();
         if (num < 2){
             $(this).hide();
         }
     })
    
     $('#tabs li a[href="#plots"]').click(function(){
        $('#plot_it').trigger('click');
     });
});
</script>

<div class="section" id="plots">

<%
    //  Output List of Target Genes
    out.println ("<table cellpadding=0 cellspacing=0>");
    out.println ("<tr valign='top'><td bgcolor=#FFFFFF valign=top>");

    out.println("<form id=\"plot_form\" action=\"generatePlots.do\" method=\"GET\">");
    out.println("<fieldset><legend>Plot Parameters</legend>");

    out.println("<b>Gene:");

    if (geneWithScoreList.size() == 1) {
        out.println (geneWithScoreList.get(0).getGene().toUpperCase());
    }
    out.println("</b><br><br>");

    //  Output Cancer Type ID and Case Set ID
    out.println ("<input type='hidden' name='" + QueryBuilder.CANCER_STUDY_ID
            + "' value='" + cancerTypeId + "'>");

    out.println ("<input type='hidden' name='" + QueryBuilder.CASE_SET_ID
            + "' value='" + caseSetId + "'>");


    out.println ("<input type='hidden' name='" + QueryBuilder.CASE_IDS
            + "' value='" + caseIds + "'>");


    out.println ("<select style=\"width:180;\" name='gene'>");
    for (GeneWithScore geneWithScore : geneWithScoreList) {
        out.println("<option value='" + geneWithScore.getGene() + "'>"
                + geneWithScore.getGene().toUpperCase() + "</option>");

    }
    out.println("</select>");

    int mutationProfileCounter = countProfiles(profileList, GeneticAlterationType.MUTATION_EXTENDED);
    int mRNAProfileCounter = countProfiles(profileList, GeneticAlterationType.MRNA_EXPRESSION);
    int cnaProfileCounter = countProfiles(profileList, GeneticAlterationType.COPY_NUMBER_ALTERATION);
    int methylationProfileCounter = countProfiles(profileList, GeneticAlterationType.METHYLATION);
    int rppaProteinProfileCounter = countProfiles(profileList, GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL);

    if (mutationProfileCounter <=1 && mRNAProfileCounter <=1 && cnaProfileCounter <= 1
            && methylationProfileCounter <=1 && rppaProteinProfileCounter <= 1) {
        out.println ("<BR><BR>");
    } else {
        out.println ("<BR><BR><b>Data Types:</b><br><br>");
    }


    //  Output Mutation Profiles
    if (mutationProfileCounter > 0) {
        outputProfiles(GeneratePlots.MUTATION_PROFILE_ID, profileList,
            GeneticAlterationType.MUTATION_EXTENDED, geneticProfileIdSet, out);
        out.println("<BR>");
    }

    //  Output mRNA Profiles
    if (mRNAProfileCounter > 0) {
        outputProfiles(GeneratePlots.MRNA_PROFILE_ID, profileList,
            GeneticAlterationType.MRNA_EXPRESSION, geneticProfileIdSet, out);
        out.println("<BR>");
    }

    //  Output CNA Profiles
    if (cnaProfileCounter > 0) {
        outputProfiles(GeneratePlots.CNA_PROFILE_ID, profileList,
            GeneticAlterationType.COPY_NUMBER_ALTERATION, geneticProfileIdSet, out);
        out.println("<BR>");
    }

    //  Output Methylation Profiles
    if (methylationProfileCounter > 0) {
        outputProfiles(GeneratePlots.METHYLATION_PROFILE_ID, profileList,
            GeneticAlterationType.METHYLATION, geneticProfileIdSet, out);
    }

    //  Output rppa Profiles
    if (rppaProteinProfileCounter > 0) {
        outputProfiles(GeneratePlots.RPPA_PROTEIN_PROFILE_ID, profileList,
            GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL, geneticProfileIdSet, out);
    }

    if (methylationProfileCounter > 0 || rppaProteinProfileCounter > 0) {
        out.println ("<BR><BR><b>Plot Type:</b><br><br>");
    }
    out.println ("<select style=\"width:180;\"name='plot_type'>");
    out.println ("<option value='mrna_cna'>Copy Number v. mRNA</option>");
    if (methylationProfileCounter > 0) {
        out.println ("<option value='mrna_methylation'>DNA Methylation v. mRNA</option>");
    }
    if (rppaProteinProfileCounter > 0) {
        out.println ("<option value='mrna_rppa_protein'>mRNA v. RPPA protein level</option>");
    }
    out.println ("</select>");

    for (CaseList caseSet:  caseSets) {
        if (caseSet.getName().toLowerCase().contains("normal")) {
            out.println ("&nbsp;<BR><INPUT TYPE=CHECKBOX NAME='" + GeneratePlots.INCLUDE_NORMALS +"' VALUE='INCLUDE_NORMALS'/>Include Normals");
            out.println ("<INPUT TYPE=HIDDEN NAME=" + GeneratePlots.NORMAL_CASE_SET_ID + "' VALUE='"
                    + caseSet.getStableId() + "'/>");
        }
    }

    if (mRNAProfileCounter > 0 && cnaProfileCounter > 0) {
        out.println ("<br><br><a href='javascript:void(); return false;' id='plot_it'><img src='images/next_button.gif'></a>");
    } else {
        out.println ("<B>Unfortunately, this cancer types is missing certain data types, and the plot feature is therefore not available.</B>");
    }
    out.println ("</fieldset>");
    out.println ("</form>");
    out.println ("</td>");
    out.println ("<td valign=top width=650px>");
    out.println ("<div id='progress_bar'>");
    out.println ("<div id=\"plot_images\"></div></div>");
    out.println ("</td>");
    out.println ("</tr>");
%>
</table>
</div>

<%!
    // Counts Number of Genetic Profiles of the Specified Alteration Type.
    public int countProfiles(ArrayList<GeneticProfile> profileList,
            GeneticAlterationType type) {
        int counter = 0;
        for (int i = 0; i < profileList.size(); i++) {
            GeneticProfile profile = profileList.get(i);
            if (profile.getGeneticAlterationType() == type) {
                counter++;
            }
        }
        return counter;
    }

    // Outputs Genetic Profiles of the Specified Alteration Type
    public void outputProfiles(String id, ArrayList<GeneticProfile> profileList,
            GeneticAlterationType type, HashSet<String> geneticProfileIdSet,
            JspWriter out) throws IOException {
        out.println("<select style=\"width:180px;\" name='" + id + "'>");
        boolean mRNASelected = false;
        for (int i = 0; i < profileList.size(); i++) {
            GeneticProfile profile = profileList.get(i);
            if (profile.getGeneticAlterationType() == type) {
                out.print("<option value='" + profile.getStableId()
                        + "' title='" + profile.getProfileDescription() + "' ");
                if (geneticProfileIdSet.contains(profile.getStableId())) {
                    out.print ("SELECTED ");
                } else if (type.equals(GeneticAlterationType.MRNA_EXPRESSION)) {
                    //  Output the first Non-Z-Score mRNA Profile as Default
                    if (!profile.getProfileName().toLowerCase().contains("z-score")
                            && mRNASelected == false) {
                        out.print ("SELECTED ");
                        mRNASelected = true;
                    }
                }
                out.print (">");
                out.print (profile.getProfileName() + "</option>");
            }
        }
        out.println("</select>");
    }
%>