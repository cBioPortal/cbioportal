<%@ page import="org.mskcc.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cgds.model.ExtendedMutation" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.util.MutationCounter" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.SequenceCenterUtil" %>
<%@ page import="org.mskcc.portal.util.UrlFixer" %>
<%@ page import="org.mskcc.portal.mapback.Brca1" %>
<%@ page import="org.mskcc.portal.mapback.MapBack" %>
<%@ page import="org.mskcc.portal.mapback.Brca2" %>
<%
    int numGenesWithMutationDetails = 0;
    for (GeneWithScore geneWithScore : geneWithScoreList) {
        MutationCounter mutationCounter = new MutationCounter(geneWithScore.getGene(),
                mutationMap, mergedCaseList);
        if (mutationCounter.getMutationRate() > 0) {
            numGenesWithMutationDetails++;
        }
    }
%>

<% if (numGenesWithMutationDetails > 0) { %>
<div class="section" id="mutation_details">
    <% if (numGenesWithMutationDetails > 0) {
        //out.println ("* Details regarding germline mutations cannot be publicly displayed and are " +
        //        "currently listed as [FILTERED].<BR>");
        out.println("** Predicted functional impact (via " +
         "<a href=\"http://mutationassessor.org\">Mutation Assessor</a>)" +
          " is provided for missense mutations only.  ");
        out.println("<br><br>");
    }

    %>
<div class="map">
<% }else {
        out.println("<div class=\"section\" id=\"mutation_details\">");
        out.println("<p>There are no mutation details available for the gene set entered.</p>");
        out.println("<br><br>");
        out.println("</div>");
} %>
    <%
        numGenesWithMutationDetails = 0;
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            MutationCounter mutationCounter = new MutationCounter(geneWithScore.getGene(),
                    mutationMap, mergedCaseList);
            if (mutationCounter.getMutationRate() > 0) {
                numGenesWithMutationDetails++;
                out.print("<h5>" + geneWithScore.getGene().toUpperCase() + ": ");
                out.print("[");
                if (mutationCounter.getGermlineMutationRate() > 0) {
                    out.print("Germline Mutation Rate:  ");
                    out.print(percentFormat.format(mutationCounter.getGermlineMutationRate()));
                }
                if (mutationCounter.getGermlineMutationRate() > 0
                        && mutationCounter.getSomaticMutationRate() > 0) {
                    out.print(", ");
                }
                if (mutationCounter.getSomaticMutationRate() > 0) {
                    out.print("Somatic Mutation Rate:  ");
                    out.print(percentFormat.format(mutationCounter.getSomaticMutationRate()));
                }
                if (mutationCounter.getGermlineMutationRate() <=0 && mutationCounter.getSomaticMutationRate() <=0) {
                    out.print("Mutation Rate:  ");
                    out.print(percentFormat.format(mutationCounter.getMutationRate()));
                }
                out.print("]");
                out.println("</h5>");
                out.println("<table width='100%' cellspacing='0px'>");
                out.println("<tr>");
                

                out.println("<thead>");
                out.println("<td>Case ID</td>");
                out.println("<td>Mutation Status</td>");
                out.println("<td>Mutation Type</td>");
                out.println("<td>Validation Status</td>");
                out.println("<td>Sequencing Center</td>");
                out.println("<td>Amino Acid Change</td>");
                out.println("<td>Predicted Functional Impact**</td>");
                out.println("<td>Alignment</td>");
                out.println("<td>Structure</td>");
                if (geneWithScore.getGene().equalsIgnoreCase("BRCA1")
                    || geneWithScore.getGene().equalsIgnoreCase("BRCA2")) {
                    out.println("<td>Nucleotide Position *</td>");
                }
                out.println("</thead>");


                if (geneWithScore.getGene().equalsIgnoreCase("BRCA1")
                    || (geneWithScore.getGene().equalsIgnoreCase("BRCA2"))) {
                    out.println ("<th>Details</th>");
                }

                out.println("</tr>");
                int masterRowCounter = 0;
                for (String caseId : mergedCaseList) {
                    ArrayList<ExtendedMutation> mutationList =
                            mutationMap.getMutations(geneWithScore.getGene(), caseId);
                    if (mutationList != null && mutationList.size() > 0) {
                        int numRows = mutationList.size();
                        String bgcolor = "";
                        String bgheadercolor = "#B9B9FC";

                        if (masterRowCounter % 2 == 0) {
                            //bgcolor = "#bbbbbb";
                            bgcolor = "#eeeeee";
                            bgheadercolor = "#dddddd";
                        }


                        out.println("<tr bgcolor='" + bgcolor + "'>");

                        masterRowCounter++;
                        out.println("<td style=\"border-bottom:1px solid #AEAEFF; background:"+bgheadercolor+ ";\" rowspan='" + numRows + "'>" + caseId);
                        if (numRows > 1) {
                            out.println("<br><br>" + numRows + " mutations");
                        }
                        out.println("</td>");
                        int rowCounter = 0;
                        String newCell = "";
                        for (ExtendedMutation mutation : mutationList) {

                            if (rowCounter > 0) {
                                out.println("<tr bgcolor='" + bgcolor + "'>");
                            }

                            if (rowCounter == numRows-1){
                                newCell = "<td class='last_mut'>";
                            } else {
                                newCell = "<td>";
                            }


                            out.println(newCell);

                            if (mutation.getMutationStatus().equalsIgnoreCase("somatic")) {
                                out.println("<span class='somatic'>");
                            } else if (mutation.getMutationStatus().equalsIgnoreCase("germline")) {
                                out.println("<span class='germline'>");
                            } else {
                                out.println("<span>");
                            }
                            out.println(mutation.getMutationStatus());
                            out.println("</span></td>");
                            out.println(newCell + mutation.getMutationType() + "</td>");
                            out.println(newCell);
                            if (mutation.getValidationStatus().equalsIgnoreCase("valid")) {
                                out.println("<span class='valid'>");

                            } else {
                                out.println("<span>");
                            }
                            out.println(mutation.getValidationStatus());
                            out.println("</span:></td>");
                            String center = SequenceCenterUtil.getSequencingCenterAbbrev
                                    (mutation.getCenter());
                                    out.println(newCell + center + "</td>");
                            out.println(newCell + mutation.getAminoAcidChange() + "</td>");

                            String faScore = mutation.getFunctionalImpactScore();

                            String href = "";
                            String xVarLink;
                            if( !mutation.getLinkXVar().equalsIgnoreCase("na")){
                               xVarLink = UrlFixer.fixVarLink(mutation.getLinkXVar());
                               href = "<a href=\"" + xVarLink + "\">";
                            }
                            
                            out.println(newCell);
                            String impact = "";
                            if (faScore.equalsIgnoreCase("H")) {
                               impact = "<span class='high'>" + href + "High";
                            } else if (faScore.equalsIgnoreCase("M")) {
                                impact = "<span class='medium'>" + href + "Medium";
                            } else if (faScore.equalsIgnoreCase("L")) {
                                impact = "<span class='low'>" + href + "Low";
                            } else if (faScore.equals("N")) {
                                impact = "<span class='neutral'>" + href + "Neutral";
                            } else {
                                impact = "<span></span>";
                            }
                            if( !href.equals("") ){
                               impact = impact + "</a>";
                            }
                            out.println( impact + "</span>" + "</td>");

                            out.println(newCell);
                            if (mutation.getLinkMsa() != null && mutation.getLinkMsa().length() > 0) {
                               if (!mutation.getLinkMsa().equalsIgnoreCase("NA")) {
                                    String urlMsa = UrlFixer.fixVarLink(mutation.getLinkMsa());
                                    out.println("<a href=\"" + urlMsa + "\">Alignment</a>");
                               } else {
                                    out.println("&nbsp;");
                               }
                            } else {
                                out.println("&nbsp;");
                            }

                            out.println("</td>");
                            out.println(newCell);
                            if (mutation.getLinkPdb() != null && mutation.getLinkPdb().length() > 0) {
                               if( !mutation.getLinkPdb().equalsIgnoreCase("NA") ){
                                  String urlPdb = UrlFixer.fixVarLink(mutation.getLinkPdb());
                                  out.println("<a href=\"" + urlPdb + "\">Structure</a>");
                               } else {
                                    out.println("&nbsp;");
                               }
                            } else {
                                out.println("&nbsp;");
                            }
                            out.println("</td>");
                            
                            // TODO: remove gene-specific code and generalize 
                            if (geneWithScore.getGene().equalsIgnoreCase("BRCA1")) {
                                out.println(newCell);
                                if (mutation.getChr() != null && mutation.getChr().length() > 0) {
                                    out.println (mutation.getChr() + ":" + mutation.getStartPosition()
                                        + "-" + mutation.getEndPosition());
                                    Brca1 brca1 = new Brca1();
                                    MapBack mapBack = new MapBack(brca1, mutation.getEndPosition());
                                    long ntPosition = mapBack.getNtPositionWhereMutationOccurs();
                                    out.print ("<BR>NT Position:  " + ntPosition);
                                    if (ntPosition >= 185 && ntPosition <= 188) {
                                        out.println ("<BR><b>Known BRCA1 185/187DelAG Founder Mutation</b>");
                                    } else if (ntPosition >= 5382 && ntPosition <= 5385) {
                                        out.println ("<BR><b>Known BRCA1 5382/5385 insC Founder Mutation</b>");
                                    }
                                }
                                out.println("</td>");
                            } else if (geneWithScore.getGene().equalsIgnoreCase("BRCA2")) {
                                out.println(newCell);
                                if (mutation.getChr() != null && mutation.getChr().length() > 0) {
                                    out.println (mutation.getChr() + ":" + mutation.getStartPosition()
                                        + "-" + mutation.getEndPosition());
                                    Brca2 brca2 = new Brca2();
                                    MapBack mapBack = new MapBack(brca2, mutation.getEndPosition());
                                    long ntPosition = mapBack.getNtPositionWhereMutationOccurs();
                                    if (ntPosition == 6174) {
                                        out.println ("<BR><b>Known BRCA2 6174delT founder mutation.</b></a>");
                                    }
                                }
                                out.println("</td>");
                            }
                            //out.println("</td>");

                            out.println("</tr>");
                            rowCounter++;
                        }
                    }
                }
                out.println("</table><P>");
                if (geneWithScore.getGene().equalsIgnoreCase("BRCA1")) {
                    out.println("* Known BRCA1 185/187DelAG and 5382/5385 insC founder mutations are shown in bold.");
                }
                if (geneWithScore.getGene().equalsIgnoreCase("BRCA2")) {
                    out.println("* Known BRCA2 6174delT founder mutation are shown in bold.");
                }
            }
        }
    %>
    <% if (numGenesWithMutationDetails > 0) {
        out.println("</div></div>");      //end map div, end section div
    } %>
