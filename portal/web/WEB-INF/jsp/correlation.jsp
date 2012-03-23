<%@ page import="org.mskcc.portal.stats.OddsRatio" %>
<%@ page import="org.mskcc.portal.model.GeneWithScore" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.apache.commons.lang.math.DoubleRange" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.portal.stats.OddsRatioTemp" %>
<%
    DecimalFormat decimalFormat = new DecimalFormat("###,###.######");
    out.println("<div class=\"section\" id=\"gene_correlation\">");
    out.println("<div class=\"map\">");

    DoubleRange range0 = new DoubleRange (0.0, 0.1);
    DoubleRange range1 = new DoubleRange (0.1, 0.5);
    DoubleRange range2 = new DoubleRange (0.5, 1.5);
    DoubleRange range3 = new DoubleRange (1.5, 100);

    //OddsRatioTemp utilTemp = new OddsRatioTemp(dataSummary);
    //out.println("<P>R Code:  <br>" + utilTemp.getRCommand() + "</P>");
    
    if (geneWithScoreList.size() == 2) {
        GeneWithScore gene0 = geneWithScoreList.get(0);
        GeneWithScore gene1 = geneWithScoreList.get(1);
        OddsRatio util = new OddsRatio(dataSummary, gene0.getGene(), gene1.getGene());
        out.println("<h4>Odds Ratio:  ");
        outputFormattedDouble (out, util.getOddsRatio());

        if (Double.isNaN(util.getOddsRatio())) {
            out.println(" [zero events recorded for one or both genes");
        } else if (range0.containsDouble(util.getOddsRatio())) {
            out.println(" [strong tendency toward mutual exclusivity]");
        } else if (range1.containsDouble(util.getOddsRatio())) {
            out.println(" [some tendency toward mutual exclusivity]");
        } else if (range2.containsDouble(util.getOddsRatio())) {
            out.println(" [no association]");
        } else if (range3.containsDouble(util.getOddsRatio())) {
            out.println(" [tendency toward co-occurrence]");
        } else if (util.getOddsRatio() > 100.0) {
            out.println(" [strong tendendency towards co-occurrence]");
        }

        out.println ("</h4><br>95% Confidence Interval:  ");
        outputFormattedDouble (out, util.getLowerConfidenceInterval());
        out.println ("-");
        outputFormattedDouble (out, util.getUpperConfidenceInterval());
        out.println ("<br> p-value:  " + decimalFormat.format(util.getCumulativeP())
                + " [Fisher's Exact Test]</P>");

        //out.println("<P>R Code:  <br>" + util.getRCommand() + "</P>");
    } else if (geneWithScoreList.size() > 1) {
        //  Create Header
        out.println("<table>");
        out.println("<tr><th>Gene</th>");
        for (GeneWithScore geneA : geneWithScoreList) {
            out.println("<th>" + geneA.getGene().toUpperCase() + "</th>");
        }
        out.println("</tr>");

        //  Create odds ratio table
        Set<String> testMap = new HashSet<String>();
        StringBuffer pValues = new StringBuffer("<table>");
        pValues.append ("<tr><th>p-values <0.05, as derived via Fisher's Exact test are " +
                "outlined in <font color=red>red</font>." +
                "<br>p-values are <i>not</i> adjusted for FDR." +
                "</th></tr>");
        int pValueCounter = 1;
        for (GeneWithScore geneA : geneWithScoreList) {
            out.println("<tr>");
            out.println("<th>" + geneA.getGene().toUpperCase() + "</th>");
            for (GeneWithScore geneB : geneWithScoreList) {
                if (geneA.getGene().equals(geneB.getGene())) {
                    out.println("<td bgcolor=lightgray>---</td>");
                } else {
                    String key = createKey (geneA, geneB);
                    if (!testMap.contains(key)) {
                        OddsRatio util = new OddsRatio(dataSummary, geneA.getGene(), geneB.getGene());

                        String style = "";
                        if (util.getCumulativeP() <= 0.05) {
                            style = "significant";
                        }

                        if (Double.isNaN(util.getOddsRatio())) {
                            out.println("<td class='" + style + "' bgcolor=#FFCCCC><nobr><font>");
                        } else if (range0.containsDouble(util.getOddsRatio())) {
                            out.println("<td class='" + style + "' bgcolor=#6666FF><nobr><font color='white'>");
                        } else if (range1.containsDouble(util.getOddsRatio())) {
                            out.println("<td class='" + style + "' bgcolor=#9999FF><nobr><font>");
                        } else if (range2.containsDouble(util.getOddsRatio())) {
                            out.println("<td class='" + style + "' bgcolor=white><nobr><font>");
                        } else if (range3.containsDouble(util.getOddsRatio())) {
                            out.println("<td class='" + style + "' bgcolor=#FFFF99><nobr><font>");
                        } else if (util.getOddsRatio() > 100.0) {
                            out.println("<td class='" + style + "' bgcolor=#FF9933><nobr><font>");
                        }
                        // outputFormattedDouble (out, util.getOddsRatio());
                        // out.println (" ");
                        outputFormattedDouble (out, util.getCumulativeP());
                        out.println ("</nobr></font></td>");
                        testMap.add(key);
                    } else {
                        out.println ("<td bgcolor=white></td>");
                    }
                }
            }
            out.println("</tr>");
        }
        out.println("</table>");
        pValues.append("</table>");
        out.println ("<P>" + pValues.toString() + "</P>");
        out.println("<P/>");
    }
    if (geneWithScoreList.size() > 2) {
        out.println("<table >");
        out.println("<tr><th>Legend</th></tr>");
        out.println("<tr bgcolor=#6666FF>");
        out.println("<td><font color='white'>strong tendency toward mutual exclusivity</font></td></tr>");
        out.println("<tr bgcolor=#9999FF>");
        out.println("<td>some tendency toward mutual exclusivity</td></tr>");
        out.println("<tr bgcolor=white>");
        out.println("<td>no association</td></tr>");
        out.println("<tr bgcolor=#FFFF99>");
        out.println("<td>tendency toward co-occurrence</td></tr>");
        out.println("<tr bgcolor=#FF9933>");
        out.println("<td>strong tendendency towards co-occurrence</td></tr>");
        out.println("<tr bgcolor=#FFCCCC>");
        out.println("<td>zero events recorded for one or both genes</td></tr>");
        out.println("</table>");
    }
    out.println("</div>");
    out.println("</div>");
%>

<%!
    public String createKey(GeneWithScore geneA, GeneWithScore geneB) {
        if (geneA.getGene().compareTo(geneB.getGene()) < 0) {
            return geneA.getGene() + ":" + geneB.getGene();
        } else {
            return geneB.getGene() + ":" + geneA.getGene();
        }

    }

    private void outputFormattedDouble(JspWriter out, double value) throws IOException {
        DecimalFormat decimalFormat = new DecimalFormat("###,###.######");
        if (Double.isInfinite(value)) {
            out.println("INF");
        } else if (Double.isNaN(value)) {
            out.println("NaN");
        } else {
            out.println(decimalFormat.format(value));
        }
    }
%>