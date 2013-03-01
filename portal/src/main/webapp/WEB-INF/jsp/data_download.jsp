<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.util.ValueParser" %>
<%
    String debugStr = request.getParameter("xdebug");
    boolean debug = false;
    if (debugStr != null && debugStr.equals("1")) {
        debug = true;
    }
    String textOnly = request.getParameter("text_only");
%>

    <div class="section" id="data_download">
          <%@ include file="download_links.jsp" %>
            <br><br>

      <div class='copy_tables'>
        <h4>Contents can be copied and pasted into Excel.</h4>
        <p>Frequency of Gene Alteration:<p/>
        <textarea rows="10" cols="40">
<%
        out.print ("GENE_SYMBOL\tNUM_CASES_ALTERED\tPERCENT_CASES_ALTERED\n");
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            out.print (geneWithScore.getGene() + "\t");
            double fractionAltered = dataSummary.getPercentCasesWhereGeneIsAltered
                            (geneWithScore.getGene());
            out.print (dataSummary.getNumCasesWhereGeneIsAltered(geneWithScore.getGene()) + "\t");
            out.println (percentFormat.format(fractionAltered));
        }
             %>
        </textarea>
<%
        out.println ("<P>Type of Genetic alterations across all cases:  (Alterations are summarized as MUT, Gain, HetLoss, etc.)");
        out.println ("<P><textarea rows=10 cols=80 id=\"heat_map\">");
        out.print ("Case ID\t");
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            out.print(geneWithScore.getGene().toUpperCase().trim() + "\t");
        }
        out.print ("\n");
        for (String caseId : mergedCaseList) {
            out.print(caseId + "\t");
            for (GeneWithScore geneWithScore : geneWithScoreList) {
                String value = mergedProfile.getValue(geneWithScore.getGene(), caseId);
                ValueParser parser = ValueParser.generateValueParser( geneWithScore.getGene(), value, 
                         zScoreThreshold, rppaScoreThreshold, theOncoPrintSpecification );
                if( null == parser){
                   System.err.println( "null valueParser: cannot find: " + geneWithScore.getGene() );
                   break;
                }
                if (parser.isCnaAmplified()) {
                    out.print ("AMP;");
                }
                if (parser.isCnaHomozygouslyDeleted()) {
                   out.print ("HOMDEL;");
               }
                if (parser.isCnaGained()) {
                   out.print ("GAIN;");
               }
                if (parser.isCnaHemizygouslyDeleted()) {
                   out.print ("HETLOSS;");
               }
                if (parser.isMutated()) {
                    out.print ("MUT;");
                }
                if (parser.isMRNAWayUp()) {
                    out.print ("UP;");
                }
                if (parser.isMRNAWayDown()) {
                    out.print ("DOWN;");
                }
                if (parser.isRPPAWayUp()) {
                    out.print("RPPA-UP;");
                }
                if (parser.isRPPAWayDown()) {
                    out.print("RPPA-DOWN;");
                }
                out.print ("\t");
            }
            out.print ("\n");
        }
        out.println ("</textarea>");

        out.println ("<P>Cases affected:  (Only cases with an alteration are included)");
        out.println ("<P><textarea rows=10 cols=80>");
        for (String caseId : mergedCaseList) {
            if (dataSummary.isCaseAltered(caseId)) {
                out.println (caseId);
            }
        }
        out.println ("</textarea>");

        out.println ("<P>Case matrix: (1= Case harbors alteration in one of the input genes)</P>");
        out.println ("<P><textarea rows=10 cols=80>");
        for (String caseId : mergedCaseList) {
            if (dataSummary.isCaseAltered(caseId)) {
                out.println (caseId + "\t1");
            } else {
                out.println (caseId + "\t0");
            }
        }
        out.println ("</textarea>");
%>
      </div><!-- end copy table div -->
    </div><!-- end data download div -->
