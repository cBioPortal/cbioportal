<%--
 - Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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


<%--  For the PATIENT view  --%>

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>


<script type="text/javascript">

    $(function () {

        /*
         * <li id="tab-heatmap-viewer"><a id="link-heatmap-viewer" href="#tab_chm" class="patient-tab">Heatmap</a></li>
         * _and_
         * <div class="patient-section" id="tab_chm">
         *     etc etc
         * </div>
         */

        var tabLI = $('#tab-heatmap-viewer'),             // the <li> for the tab
            heatmapDiv = $('div#tab_chm'),
            enabled = false,
            patientId = '<%=patientID%>';

        // start with tab hidden.  Div is already display:none
        tabLI.attr('style', 'display:none');

        if (patientId === 'null') {
            console.log ('patientID is null.  How did we get here?');
            return;
        }

        var metaUrl = '<%=GlobalProperties.getPatientHeatmapMetaUrl("")%>';
        if (metaUrl === 'null') return;                 // not configured in properties

        metaUrl += patientId;        // patientID set in patient_view.jsp

        // AJAX call to DyCE (index service), then test if result is empty.
        $.getJSON(metaUrl, function(data){
            // Check if there were any heatmaps for this study
            if (data.jobStatus === "completed" && data.fileContent != undefined) {
                enabled = !( data.fileContent.startsWith('[]') );   // array not empty
            }

            if (enabled) {  // Only enable tab if there are heatmaps to show
                data.fileContent = JSON.parse(data.fileContent);
                var viewerUrl = '<%=GlobalProperties.getPatientHeatmapViewerUrl("")%>';
                if (viewerUrl === 'null') return;          // not configured in properties

                viewerUrl += patientId;

                // So we display the tab
                tabLI.attr('style', 'display:list-item');
                heatmapDiv.attr('style', 'display:block');

                // And give it a click handler to insert the iframe
                $("#link-heatmap-viewer").click(function() {
                    if (!$(this).hasClass("tab-clicked")) {
                        $(this).addClass("tab-clicked");

                        heatmapDiv.html('<iframe id="frame" src="'+viewerUrl+'" width="100%" height="700px"></iframe>');
                    }
                });
            }
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            // Unsure what else to do for errors.  Tab just won't be displayed.
            console.log ('AJAX Error: ' + textStatus);
        });
    });
</script>

<div id="chm-viewer-div"><img src="images/ajax-loader.gif"/></div>