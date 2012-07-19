<%@ page import="org.mskcc.portal.model.GeneWithScore" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ArrayList" %>
<%
    //  Output Chart that contains frequency of gene alterations
    //  Chart generated via Google Charts API
    DecimalFormat numFormat = new DecimalFormat("###.##");

    //  Begin chart parameters
    StringBuffer chd = new StringBuffer("chd=t:");
    StringBuffer chl = new StringBuffer("chl=");
    double maxValue = 0.0;

    //  Iterate through ranked order list of genes
    int counter = 0;

    //  Temporary Hack to Support Hidden Feature to not do sorting.
    int maxShown = 10;
    int maxWidth = 700;
    String noSort = request.getParameter("no_sort");
    if (noSort != null) {
        String geneListTemp = xssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
        HashMap<String, GeneWithScore> tempMap = new HashMap <String, GeneWithScore>();
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            tempMap.put(geneWithScore.getGene(), geneWithScore);
        }
        String geneSymbols[] = geneListTemp.split("\\s");
        ArrayList<GeneWithScore> newGeneWithScoreList = new ArrayList <GeneWithScore>();
        for (int i=0; i<geneSymbols.length; i++) {
            GeneWithScore geneWithScore = tempMap.get(geneSymbols[i]);
            if (geneWithScore != null) {
                newGeneWithScoreList.add(geneWithScore);
            }
        }
        geneWithScoreList = newGeneWithScoreList;
        maxShown = 20;
        maxWidth = 1000;
    }

    for (GeneWithScore geneWithScore : geneWithScoreList) {
        double value = geneWithScore.getScore() * 100.0;

        if (counter < maxShown) {
            //  Store max value
            if (value > maxValue) {
                maxValue = value;
            }

            //  Append value
            chd.append(numFormat.format(value) + ",");

            //  Append label
            chl.append(geneWithScore.getGene().toUpperCase() + "|");
        }
        counter++;
    }

    //  Remove last delimiter
    String chdStr = chd.toString();
    if (chdStr.length() > 0) {
        chdStr = chdStr.substring(0, chdStr.length() -1);
    }
    String chlStr = chl.toString();
    if (chlStr.length() > 0) {
        chlStr = chlStr.substring(0, chlStr.length() -1);
    }

    //  Set Y-axis labels
    int interval = 0;
    StringBuffer chx1 = new StringBuffer("&chxl=0:");
    while (interval < maxValue) {
        chx1.append("|" + interval);
        interval += 10;
    }
    int maxInterval = interval;
    chx1.append ("|" + maxInterval);

    //  Create Google Chart URL
    String url = "http://chart.apis.google.com/chart?cht=bvg&" +
            chdStr + "&chs=" + maxWidth + "x200&" + chlStr +
            "&chbh=45,5,15&chxt=y&chco=CC6699&chg=10,10&chds=0," + maxInterval +
            "&" + chx1.toString() + "&chf=bg,s,FFFFFF";

%>
<div class="frequency_section" id="frequency_plot">
<p><h4>Most Frequently Altered Genes:</h4></p>
<p>y-axis indicates percentage of cases where gene is altered.</p>
<img src="<%= url%>"/>
<br>
</div>