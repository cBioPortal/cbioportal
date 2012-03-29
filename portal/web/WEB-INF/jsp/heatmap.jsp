<%@ page import="org.mskcc.portal.model.GeneWithScore" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.mskcc.portal.util.ValueParser" %>
<%@ page import="org.mskcc.portal.util.ZScoreUtil" %>
<%
    String debugStr = request.getParameter("xdebug");
    boolean debug = false;
    if (debugStr != null && debugStr.equals("1")) {
        debug = true;
    }
    String textOnly = request.getParameter("text_only");
%>
<div class="map">
            <table>
                <%
                    StringBuffer dataTable = new StringBuffer();
                    // Output table header
                    out.println("<tr>");
                    out.println("<th>Case Id</th>");
                    for (GeneWithScore geneWithScore : geneWithScoreList) {
                        out.println("<th> ");
                        String geneId = "";
                        if (geneWithScore.getGene().startsWith("hsa")) {
                            geneId = geneWithScore.getGene().trim();
                        } else {
                            geneId = geneWithScore.getGene().toUpperCase().trim();
                        }
                        out.println(geneId);
                        out.println("</th>");
                    }
                    out.println("<th>Gene Set</th>");
                    out.println("</tr>");

                    //  Output frequency of alterations

                    out.println("<tr>");
                    out.println("<th>Number of cases with altered gene</th>");
                    for (GeneWithScore geneWithScore : geneWithScoreList) {
                        out.println("<th align=right> ");
                        int numCasesAltered = dataSummary.getNumCasesWhereGeneIsAltered(geneWithScore.getGene());
                        out.println(numCasesAltered);
                        out.println("</th>");
                    }
                    out.println("<th align=right>");
                    out.println(dataSummary.getNumCasesAffected());
                    out.println("</th>");
                    out.println("</tr>");

                    out.println("<tr>");
                    out.println("<th>% of cases with altered gene</th>");
                    for (GeneWithScore geneWithScore : geneWithScoreList) {
                        out.println("<th> ");
                        double fractionAltered = dataSummary.getPercentCasesWhereGeneIsAltered
                                (geneWithScore.getGene());
                        out.println(percentFormat.format(fractionAltered));
                        out.println("</th>");
                    }
                    out.println("<th>");
                    out.println(percentFormat.format(dataSummary.getPercentCasesAffected()));
                    out.println("</th>");
                    out.println("</tr>");

                    //  Output One Case per Row
                    for (String caseId : mergedCaseList) {
                        out.println("<tr>");
                        out.println("<th bgcolor='#BBBBBB'><nobr>" + caseId + "&nbsp;</nobr></th>");
                        int dataCounter=0;
                        for (GeneWithScore geneWithScore : geneWithScoreList) {
                            String value = mergedProfile.getValue(geneWithScore.getGene(), caseId);
                            // was: ValueParser parser = new ValueParser(value, zScoreThreshold);
                            ValueParser valueParser = ValueParser.generateValueParser( geneWithScore.getGene(), value, 
                                     zScoreThreshold, rppaScoreThreshold, theOncoPrintSpecification );
                            if( null == valueParser){
                               System.err.println( "null valueParser: cannot find: " + geneWithScore.getGene() );
                               break;
                            }



                            //ORIGINAL
                            out.println("<td class=" + valueParser.getCopyNumberStyle() + ">");
                            out.println( "<div class=\"nowrap\">" );
                            out.println( valueParser.getMutationGlyph() );
                            out.println( valueParser.getMRNAGlyph() );
                            out.println( "<div>" );



                            //TESTING
                            /*out.println("<td class=" + valueParser.getCopyNumberStyle() + ">");
                            out.println( "<div class=\"nowrap\">" );
                            out.println("<div class=\"glyph_table\">");
                            out.println("<table><tr><td>");
                            out.println( valueParser.getMutationGlyph() );
                            out.println("</td><td>");
                            out.println( valueParser.getMRNAGlyph() );
                            out.println("</td></tr></table>");
                            out.println( "</div>" );
                            out.println( "<div>" );
                            */

                            if (debug) {
                                out.println ("<br><small>");
                                String fields[] = valueParser.getOriginalValue().split(";");
                                for (String field:  fields) {
                                    String parts[] = field.split(":");
                                    if (parts.length<2) {
                                        continue;
                                    }
                                    if (parts[1].contains(",")) {
                                        String pieces[] = parts[1].split(",");
                                        out.println ("<br><nobr>" + parts[0] +  " [" + pieces.length + " probe sets]:</nobr>");
                                        out.println ("<ul>");
                                        for (String piece:  pieces) {
                                            out.println ("<li>" + piece);
                                        }
                                        out.println ("</ul>");
                                    } else {
                                       // TODO: use CSS nowrap; nobr's deprecated
                                        out.println ("<br><nobr>" + parts[0] + ":  " + parts[1] +"</nobr>");
                                    }
                                }
                                out.println ("</small>");
                            }
                            out.println("</td>");
                            boolean isAltered = dataSummary.isGeneAltered(geneWithScore.getGene(), caseId);
                            if (isAltered) {
                                dataTable.append ("1");
                            } else {
                                dataTable.append ("0");
                            }
                            if (dataCounter < geneWithScoreList.size() -1) {
                                dataTable.append (",");
                            }
                            dataCounter++;
                        }
                        dataTable.append("\n");
                        out.println("<td>");
                        boolean caseIsAltered = dataSummary.isCaseAltered(caseId);
                        if (caseIsAltered) {
                            out.println("<img src='images/altered.gif'>");
                        }
                        out.println("</td>");
                        out.println("</tr>");
                    }
                %>
            </table>
            </div>


        </div>

        <div class="section" id="data_download">
          <%@ include file="download_links.jsp" %>
            <br><br>

    <%
        out.println ("<div class='copy_tables'>");
        out.println ("<h4>Contents can be copied and pasted into Excel.</h4>");
        out.println ("<P>Frequency of Gene Alteration:");
%>
                <p/>
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
        </div><!-- end data download div -->
