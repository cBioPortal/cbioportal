<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%
    String faqHtml = GlobalProperties.getProperty("faq");
    String siteTitle = GlobalProperties.getTitle();

    if (faqHtml == null) {
        faqHtml = "content/faq.html";
    } else {
        faqHtml = "content/" + faqHtml;
    }

%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::FAQ"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
<div id="main">
    <table cellspacing="2px">
        <tr>
            <td>
            <h2>Genomic Data and Scientific Content Curator for cBioPortal</h2>
            <div>
                
            <p>We are looking for a <b>Genomic Data and Scientific Content Curator</b> to support cBioPortal.
                To take the Portal to the next level and to transition it towards use in the clinic,
                we are looking to improve and expand its data content. The Curator will be responsible
                for the <b>collection, maintenance, and quality control of genomic alteration data</b> and
                related <b>clinical annotation</b> for the cBioPortal.
            <p>
                <h3>Responsibilities</h3>
                <ul>
                <li>Identify and collect relevant data sets from the scientific literature and public databases</li>
                <li>Perform data quality control and conversion for import into cBioPortal</li>
                <li>Assist collaborators with data formatting</li>
                <li>Help with curating the known effects of recurrent oncogenic mutations (for incorporation into our clinical reports)</li>
                <li>Identify relevant information in clinical databases at MSKCC and help link this to the genomic data in a local version of cBioPortal</li>
                <li>Assist cBioPortal users with questions about the data and perform training sessions</li>
                <li>Update website content and help prepare educational documents</li>
                </ul>
            </p>
            <p>
                <h3>Required Skills</h3>
                <ul>
                <li>PhD in molecular biology, genetics, genomics, bioinformatics (or equivalent) (or a Masters Degree with two years of work experience)</li>
                <li>Knowledge of cancer genomics and genomic data, including next-generation sequencing and microarray data</li>
                <li>Bioinformatics skills (Unix, scripting)</li>
                <li>Strong communication skills (written and oral)</li>
                <li>Attention to detail</li>
                <li>Ability to work in a team</li>
                </ul>
            </p>
            <p>
                To apply please send your resume to: cbioportaljobs@gmail.com.
                In the subject line please add #Datacurator ....
            </p>
            </div>
            </td>
        </tr>
    </table>
    </div>
    </td>
    <td width="172">
	<jsp:include page="WEB-INF/jsp/global/right_column.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />
</body>
</html>
