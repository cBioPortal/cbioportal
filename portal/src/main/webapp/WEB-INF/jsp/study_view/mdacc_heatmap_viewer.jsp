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


<%--  For the STUDY view  --%>

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>


<script type="text/javascript">


    $(function () {

        /* This function is probably redundant. It shouldn't need to be here. */
        function getRequests() {
            var s1 = location.search.substring(1, location.search.length).split('&'),
                r = {}, s2, i;
            for (i = 0 ; i < s1.length ; i += 1) {
                s2 = s1[i].split('=');
                r[decodeURIComponent(s2[0]).toLowerCase()] = decodeURIComponent(s2[1]);
            }
            return r;
        }

        var tabLI = $('#study-tab-heatmap-li'),             // the <li> for the tab
            heatmapDiv = $('div#heatmap'),
            enabled = false;

        tabLI.attr('style', 'display:none');
        //heatmapDiv.attr('style', 'display:none');     // display is already none

        var metaUrl = '<%=GlobalProperties.getStudyHeatmapMetaUrl()%>';
        if (metaUrl === 'null') return;                 // not configured in properties

        var queryString = getRequests();

        // Next line no longer works -- QueryBuilder hasn't kept up with change to study page
        //var studyId = '"' + queryString['<%=QueryBuilder.CANCER_STUDY_ID%>'] + '"';

        var studyId = queryString['id'];
        // ### Next line for testing ###  To be removed...
        if (studyId === 'study_es_0') studyId = 'brca_tcga_pub';

        metaUrl += studyId;

        /*
         * <li><a href="#heatmap" id="study-tab-heatmap-a" class="study-tab">Heatmap</a></li>
         * _and_
         * <div class="study-section" id="heatmap">
         *     etc etc
         * </div>
         */

        // AJAX call to DyCE (index service), then test if result is empty.
        $.getJSON(metaUrl, function(data){

            // Check if there were any heatmaps for this study
            if (data.jobStatus === "completed" && data.fileContent != undefined) {
                enabled = !( data.fileContent.startsWith('[]') );   // array not empty
                // ### Just for testing
                //enabled = true;
            }

            if (enabled) {  // Only enable tab if there are heatmaps to show
                data.fileContent = JSON.parse(data.fileContent);
                var viewerUrl = '<%=GlobalProperties.getStudyHeatmapViewerUrl()%>';
                if (viewerUrl === 'null') return;          // not configured in properties

                viewerUrl += data.fileContent[0];

                // So we display the tab
                tabLI.attr('style', 'display:list-item');
                heatmapDiv.attr('style', 'display:block');

                // And give it a click handler to insert the iframe
                $("#study-tab-heatmap-a").click(function() {
                    if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {

                        $(this).addClass("tab-clicked");

                        heatmapDiv.html('<iframe id="frame" src="'+viewerUrl+'" width="100%" height="700px"></iframe>');
                    }
                    window.location.hash = '#heatmap';
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
