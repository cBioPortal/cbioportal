<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.google.common.base.Joiner" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.IGVLinking" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoGeneOptimized" %>
<%@ page import="org.mskcc.cbio.portal.model.CanonicalGene" %>
<%
      // construct gene list parameter to IGV
      // use geneWithScoreList so we don't get any OQL
      List<String> onlyGenesList = new ArrayList<String>();
      for (GeneWithScore geneWithScore : geneWithScoreList) {
          CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(geneWithScore.getGene());
           
          if (gene!=null && !gene.isMicroRNA() && !gene.isPhosphoProtein()) {
              onlyGenesList.add(geneWithScore.getGene());
          }
      }
      String encodedGeneList = "";
      if (onlyGenesList.size() > 0) {
          try {
              encodedGeneList = URLEncoder.encode(Joiner.on(' ').join(onlyGenesList), "UTF-8");
          }
          catch(UnsupportedEncodingException e) {
          }
      }
%>

<div class="section" id="igv_tab">
    <table>
        <tr>
            <td style="padding-right:25px; vertical-align:top;"><img src="images/IGVlogo.png" alt=""/></td>
            <td style="vertical-align:top">

				<P>Use the <a href="http://www.broadinstitute.org/igv/home">Integrative Genomics
                Viewer (IGV)</a> to explore and visualize copy number data.
                <p>
                    The <a href="http://www.broadinstitute.org/igv/home">Integrative Genomics
                    Viewer (IGV)</a> is a high-performance visualization tool for interactive exploration
                    of large, integrated datasets. It supports a wide variety of data types including sequence alignments, 
					gene expression, copy number amplifications and deletions, mutations, and genomic annotations
                </p>

                <p>Clicking the launch button below will:</p>

                <p>
                    <ul>
                        <li>start IGV via Java Web Start.</li>
                        <li>load copy number data (segmented) for your selected cancer study; and</li>
                        <li>automatically highlight your query genes.</li>
                    </ul>
                </p>

                <br>
                    <% String[] segViewingArgs = IGVLinking.getIGVArgsForSegViewing(cancerTypeId, encodedGeneList); %>
                    <a id="igvLaunch" href="#" onclick="prepIGVLaunch('<%= segViewingArgs[0] %>','<%= segViewingArgs[1] %>','<%= segViewingArgs[2] %>','<%= segViewingArgs[3] %>')"><img src="images/webstart.jpg" alt=""/></a>
                <br>

                <p>
                    Once you click the launch button, you may need to select Open with Java&#8482;
                    Web Start and click OK. If the system displays messages about trusting the application,
                    confirm that you trust the application. Web Start will then download and start IGV.
                    This process can take a few minutes.
                </p>
                <br>
                <p>
                    For information regarding IGV, please see:
                    <ul>
                        <li><a href="http://www.broadinstitute.org/software/igv/QuickStart">IGV Quick Start Tutorial</a></li>
                        <li><a href="http://www.broadinstitute.org/software/igv/UserGuide">IGV User Guide</a></li>
                    </ul>
                </p>
                
                <p>
                    IGV is developed at the <a href="http://www.broadinstitute.org/">Broad Institute of MIT and Harvard</a>.
                </p>
            </td>
        </tr>
    </table>
</div>

