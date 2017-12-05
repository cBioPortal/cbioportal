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

<div class='section cbioportal-frontend' id='mutation_details'>
    <jsp:include page="global/no_oql_warning.jsp" flush="true" />
    <div id="mutation_details_inner">
        <img src='images/ajax-loader.gif' alt='loading'/>
    </div>
</div>

<script type="text/javascript">
    window.onReactAppReady(function() {
        var initialized = false;

        function initMutations() {
            var mutationsTab = $('#mutation_details');

            if (mutationsTab.hasClass('cbioportal-frontend')) {
                // backwards compatible with the case when mutationDetailLimitReached
                var props = {
                    genes: QuerySession.getQueryGenes()
                };
                var samplesSpecification = [];
                if (["-1", "all"].indexOf(QuerySession.getCaseSetId()) > -1) {
		// "-1" means custom case id, "all" means all cases in the queried stud(y/ies). Neither is an actual case set that could eg be queried
                    var studyToSampleMap = QuerySession.getStudySampleMap();
                    var studies = Object.keys(studyToSampleMap);
                    for (var i=0; i<studies.length; i++) {
                        var study = studies[i];
                        samplesSpecification = samplesSpecification.concat(studyToSampleMap[study].map(function(sampleId) {
                            return {
                                sampleId: sampleId,
                                studyId: study
                            };
                        }));
                    }
                } else {
                    var studyToSampleListIdMap = QuerySession.getStudySampleListMap();
                    var studies = Object.keys(studyToSampleListIdMap);
                    for (var i=0; i<studies.length; i++) {
                        samplesSpecification.push({
                            sampleListId: studyToSampleListIdMap[studies[i]],
                            studyId: studies[i]
                        });
                    }
                }
                props.samplesSpecification = samplesSpecification;
                window.renderMutationsTab(mutationsTab.find('#mutation_details_inner')[0], props);
                return true;
            }
            return false;
        }

        // if we are already on this tab, init the tab content
        if ($("div.section#mutation_details").is(":visible")) {
            initMutations();
        }
        // otherwise delay initialization until mutations tab is clicked
        else {
            $("a.result-tab").click(function() {
                // initialize only once
                if (!initialized && $(this).attr("href") == "#mutation_details") {
                    initialized = initMutations();
                }
            });
        }
    });
</script>

<style type="text/css">
    /* HACK: Use specific id for cbioportal-frontend overrides */
    #mutation_details {
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
        font-size: 14px
    }
    #mutation_details .table {
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
        font-size: 14px
    }
    #mutation_details th,
    #mutation_details td {
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
        font-size: 14px
    }
    #mutation_details .btn-default {
        background-image: linear-gradient(to bottom, #fff 0, #eee 100%);
    }
    #mutation_details .fa-cloud-download,
    #mutation_details .fa-clipboard {
        padding-top: 3px;
        padding-bottom: 3px;
    }
    .rc-tooltip {
        opacity:1 !important;
    }
    /* END HACK */
</style>
