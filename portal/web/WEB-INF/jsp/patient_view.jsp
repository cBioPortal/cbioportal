<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cgds.model.GeneticProfile" %>

<%
String patient = (String)request.getAttribute(PatientView.PATIENT_ID);
String patient_view_error = (String)request.getAttribute(PatientView.ERROR);
CancerStudy cancerStudy = (CancerStudy)request.getAttribute(PatientView.CANCER_STUDY);
GeneticProfile mutationProfile = (GeneticProfile)request.getAttribute(PatientView.MUTATION_PROFILE);
%>

<jsp:include page="global/header_above_bar.jsp" flush="true" />

<tr>
    <td>
<%
if (patient_view_error!=null) {
    out.print(patient);
    out.print(": ");
    out.println();
    out.print(patient_view_error);
} else {
%>
<div id="patient-tabs">
    <ul>
    <li><a href='#summary' class='patient-tab' title='Events of Interest'>Summary</a></li>
    <li><a href='#mutations' class='patient-tab' title='Mutations'>Mutations</a></li>
    <li><a href='#cna' class='patient-tab' title='Copy Number Alterations'>Copy Number Alteration</a></li>
    <li><a href='#pathways' class='patient-tab' title='Pathway View'>Pathways (under construction)</a></li>
    <li><a href='#similar-patients' class='patient-tab' title='Similar Patients'>Similar Patients (under construction)</a></li>
    </ul>

    <div class="patient-section" id="summary">
        <%@ include file="patient_view/summary.jsp" %>
    </div>

    <%if(mutationProfile!=null){%>
    <div class="patient-section" id="mutations">
        <%@ include file="patient_view/mutations.jsp" %>
    </div>
    <%}%>

    <div class="patient-section" id="cna">
        <%@ include file="patient_view/cna.jsp" %>
    </div>

    <div class="patient-section" id="pathways">
        <%@ include file="patient_view/pathways.jsp" %>
    </div>

    <div class="patient-section" id="similar-patients">
        <%@ include file="patient_view/similar_patients.jsp" %>
    </div>

</div>
<%  
}
%>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true" />    

<script type="text/javascript">
$(document).ready(function(){
    setUpPatientTabs();
    initTabs();
});

function setUpPatientTabs() {
     // generate tabs for results page; set cookies to preserve
    // state of tabs when user navigates away from page and back
    $('#patient-tabs').tabs({cookie:{expires:1, name:"patient-tab"}});
    $('#patient-tabs').show();
}

function initTabs() {
    var tabContainers = $('.patient-section');
    tabContainers.hide().filter(':first').show();

    $('#patient-tabs ul a').click(function () {
            tabContainers.hide();
            tabContainers.filter(this.hash).show();
            $('#patient-tabs ul a').removeClass('selected');
            $(this).addClass('selected');
            return false;
    }).filter(':first').click();
    
}
</script>

</body>
</html>