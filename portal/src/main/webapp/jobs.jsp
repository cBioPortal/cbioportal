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

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Jobs"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />

<script>
    $( "#div-jobs" ).ready(function() {
        $("#div-jobs").accordion({
            active: false,
            collapsible: true            
        });
    });

</script>

<div id="main">
    <table cellspacing="2px">
        <tr>
            <td>
                <p>
                    To take the Portal to the next level and to transition it towards <b>use in the clinic</b>,
                    we are looking to <b>expand its analysis and visualization options</b> as well as <b>improve
                    and expand its data content</b>. We are looking for talented and team oriented people to
                    join us for the following positions.
                </p>
                
            <div id="div-jobs">
                           
            <h2><b>Software Engineer - Front End</b></h2>
            <div id="div-job-1">

                <p>As <b>one of the core engineers on the team</b>, you will be architecting
                    the next generation of cBioPortal towards <b>rich client web applications</b>
                    with more interactive analysis and visualization features. You
                    will be designing new interactive features, taking them quickly
                    from prototype to production. You'll need to understand front-end
                    web architecture, web UI design, and web-based visualization, and
                    adopt cutting edge web technologies.</p>
                
                <p>
                <h3>Responsibilities</h3>
                <ul>
                    <li>Work closely with our existing team including researchers, back-end
                        engineers, and other front-end engineers to implement and redesign
                        web UI for analysis and visualization of cancer genomics and clinical data</li>
                    <li>Build next-generation web applications with a focus on the client side</li>
                    <li>Explore and evaluate client side frameworks and technologies</li>
                    <li>Design and build UI architecture and components</li>
                    <li>Quickly generate a prototype from UI concepts for testing and team feedback</li>
                    <li>Pick up Java as necessary and engage with back-end systems</li>
                </ul>
                </p>
                
                <p>
                <h3>Minimum Qualifications</h3>
                <ul>
                    <li>Bachelor's degree in Computer Science or related field and 2+ years
                        of software development experience, or a master's degree</li>
                    <li>Web application development experience</li>
                    <li>Experience with JQuery, AJAX, HTML, and CSS</li>
                    <li>Programming experience in Java or C/C++</li>
                    <li>Experience developing in teams</li>
                    <li>Self-motivated, responsible and goal-oriented</li>
                    <li>Interest in contributing to biological research with clinical applications</li>
                </ul>
                </p>

                <p>
                <h3>Preferred Qualifications</h3>
                <ul>
                    <li>Experience with front-end framework such as Angular, Backbone, or Amber</li>
                    <li>Experience designing modular object-oriented JavaScript</li>
                    <li>Experience with HTML5, CSS3, and Javascript visualization libraries such as D3.js</li>
                    <li>Experience with server-side web frameworks such as Java/Spring/Hibernate, Python/Django, Ruby/Rails etc</li>
                    <li>User interface design knowledge</li>
                    <li>Strong ability to learn new tools, technologies and languages as needed</li>
                    <li>Prior involvement or interest in bioinformatics or cancer genomics domain</li>
                </ul>
                </p>
                
                <p>To apply please send your resume to: cbioportaljobs@gmail.com.
                   In the subject line please add #SoftwareEngineer...</p>
            </div>
                            
                
            <h2><b>Genomic Data and Scientific Content Curator</b></h2>
            <div id="div-job-2">
                
            <p>We are looking for a <b>Genomic Data and Scientific Content Curator</b> to support cBioPortal.
                The Curator will be responsible for the <b>collection, maintenance, and quality control of
                genomic alteration data</b> and related <b>clinical annotation</b> for the cBioPortal.
            </p>
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
