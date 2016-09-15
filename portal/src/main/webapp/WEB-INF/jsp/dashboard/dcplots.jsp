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

<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>


<style>
    /* This style needs to be moved to some scss/css files */
    #summary.study-section {
        padding-left: 5px;
        padding-right: 5px;
    }
</style>

<div class="container-fluid" id="complete-screen">
    <div id="main-header">
        <div id="iviz-header-left">
            <div style="float: left; margin-right: 10px;">
                <session-component :show-save-button="showSaveButton" :show-manage-button="showManageButton"
                                   :selected-patients-num="selectedPatientsNum"
                                   :selected-samples-num="selectedSamplesNum"
                                   :userid="userid" :stats="stats" :update-stats.sync="updateStats"></session-component>
            </div>

            <span class="iviz-header-left-case-name" style="display: block;">Samples selected: </span>
            <span id="iviz-header-left-sample-select" @click="openCases()"
                  class="iviz-header-left-case-number iviz-header-button"
                  :class="{'iviz-button-hover':highlightAllButtons||highlightCaseButtons}"
                  role="button" tabindex="0" style="display: block;">{{ selectedSamplesNum }}</span>
            <span class="iviz-header-left-case-name" style="display: block;">Patients selected: </span>
            <span id="iviz-header-left-patient-select" @click="openCases()"
                  class="iviz-header-left-case-number iviz-header-button"
                  :class="{'iviz-button-hover':highlightAllButtons||highlightCaseButtons}"
                  role="button" tabindex="0" style="display: block;">{{ selectedPatientsNum }}</span>

            <span id="iviz-header-left-case-download" class="iviz-header-button" @click="downloadCaseData()"
                  @mouseenter="highlightCaseButtons=true" @mouseleave="highlightCaseButtons=false" role="button"
                  tabindex="0" data-hasqtip="9" aria-describedby="qtip-9"><i class="fa fa-download" alt="download"></i></span>

            <span id="query-by-gene-span">
          <span id="queryByGeneTextSpan"></span>
          <textarea id="query-by-gene-textarea" class="expand expandFocusOut"
                    :class="{'iviz-button-hover':highlightAllButtons}" rows="1" cols="10"></textarea>
      </span>
            <i id="arrow_studyview" class="fa fa-arrow-right fa-lg" aria-hidden="true"></i>
            <form id="iviz-form" v-on:submit.prevent="submitForm" method="post" target="_blank" style="float: left;">
                <input type="submit" @mouseenter="highlightAllButtons=true" @mouseleave="highlightAllButtons=false"
                       id="iviz-header-left-1" value="Query" class="iviz-header-button" style="display: block;">
            </form>

        </div>

        <div id="iviz-header-right">
            <custom-case-input></custom-case-input>

            <select id="iviz-add-chart" class="chosen-select"
                    v-select :charts="charts" v-if="showDropDown">
                <option id='' value="">Add Chart</option>
                <option id="{{data.attr_id}}" v-if="!data.show" value="{{data.attr_id}}" v-for="(index,data) in charts">
                    {{data.display_name}}
                </option>
            </select>
        </div>

        <div id="breadcrumbs_container" v-if="hasfilters">
            <div style="float:left;">
                <span class="breadcrumb_container">Your selections: </span>
            </div>

            <span class="breadcrumb_container" v-if="customfilter.patientIds.length>0||customfilter.sampleIds.length>0">
          <span>{{customfilter.display_name}}</span>
          <i class="fa fa-times breadcrumb_remove"
             @click="clearAllCharts(true)"></i>
        </span>
            <div style="float:left" v-for="group in groups">
                <bread-crumb :attributes.sync="item"
                             :filters.sync="item.filter" v-for="(index1, item) in group.attributes"
                             v-if="item.filter.length>0"></bread-crumb>
            </div>
            <div>
                <button type='button' @click="clearAllCharts(true)"
                        class="btn btn-default btn-xs">Clear All
                </button>
            </div>
        </div>
    </div>
    <div class="grid" id="main-grid" :class="{loading:isloading}">
        <main-template :groups.sync="groups" :redrawgroups.sync="redrawgroups"
                       :selectedpatients.sync="selectedpatients"
                       :selectedsamples.sync="selectedsamples"
                       :hasfilters.sync="hasfilters" :customfilter.sync="customfilter"
                       :clear-all="clearAll"></main-template>
    </div>

</div>

<script>
    function initdcplots(data) {
        iViz.init(data);

        QueryByGeneTextArea.init('#query-by-gene-textarea', function(genes) {
            iViz.vue.manage.getInstance().$broadcast('gene-list-updated', genes);
        });
    }
</script>
