<script type="text/javascript" src="js/src/over-representation-analysis/main.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/data.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/view.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/util.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/over-representation-analysis/boilerplate.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div class="section" id="or_analysis">
</div>

<script>
    $(document).ready( function() {
        
        <%
            JSONObject result = new JSONObject();
            for (String patientId : mergedPatientList) {
                //Is altered or not (x value)
                if (dataSummary.isCaseAltered(patientId)) {
                    result.put(patientId, "altered");
                } else {
                    result.put(patientId, "unaltered");
                }
            }
        %>
        var caseListObj = jQuery.parseJSON('<%=result%>');
        
        var or_tab_init = false;
        if ($("#or_analysis").is(":visible")) {
            or_tab.init(caseListObj);
            or_tab_init = true;
        } else {
            $(window).trigger("resize");
        }
        $("#tabs").bind("tabsactivate", function(event, ui) {
            if (ui.newTab.text().trim().toLowerCase() === "over-representation analysis") {
                if (or_tab_init === false) {
                    or_tab.init(caseListObj);
                    or_tab_init = true;
                    $(window).trigger("resize");
                } else {
                    $(window).trigger("resize");
                }
            }
        });
    });
</script>
