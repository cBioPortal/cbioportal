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
        padding-right: 0;
    }
</style>

<!-- This loading is used to before all JS/CSS files loaded -->
<div id="study-tabs-loading-wait">
    <img src="images/ajax-loader.gif" alt="loading"/>
</div>

<div class="container-fluid" id="complete-screen">
    <div id="complete-screen-loader"
         :class="{'show-loading': showScreenLoad}"
         class="chart-loader" style="top: 30%; left: 30%; display: none;"><img
        src="images/ajax-loader.gif" alt="loading"></div>
    <div id="main-header" style="display: none" :class="{show:!isloading}">
        <div id="iviz-header-left">
            <div class="iviz-cohort-component" style="float: left; margin-right: 10px;">
                <session-component :show-save-button="showSaveButton" :show-manage-button="showManageButton"
                                   :selected-patients-num="selectedPatientsNum"
                                   :selected-samples-num="selectedSamplesNum"
                                   :userid="userid" :stats="stats"
                                   :update-stats.sync="updateStats"></session-component>
            </div>

            <div class="iviz-header-left-case">
                <span class="name" style="display: block;">Selected:</span>
                <span class="content">
            <span>{{ selectedSamplesNum }} samples / {{ selectedPatientsNum }} patients</span>
          </span>
            </div>
            <span id="iviz-header-left-patient-select" class="iviz-header-button"
                  @click="openCases('patient')" class="number"
                  role="button" tabindex="0" style="display: block;"><i class="fa fa-user"
                                                                        aria-hidden="true"></i></span>
            <span id="iviz-header-left-case-download" class="iviz-header-button" @click="downloadCaseData()"
                  role="button"
                  tabindex="0"><i class="fa fa-download" alt="download"></i></span>

            <span id="query-by-gene-span">
          <textarea id="query-by-gene-textarea" class="expand expandFocusOut" rows="1" cols="10"></textarea>
      </span>
            <span class="iviz-header-arrow">
          <i class="fa fa-arrow-right fa-lg" aria-hidden="true"></i>
        </span>
            <input type="button" id="iviz-header-left-1" value="Query" class="iviz-header-button" style="display: block;"
                   v-on:click="submitForm">

        </div>

        <div id="iviz-header-right">
            <div style="display: inline-flex">
                <custom-case-input></custom-case-input>
            </div>

            <select id="iviz-add-chart" class="chosen-select"
                    v-select :charts="charts" v-if="showDropDown">
                <option id='' value="">Add Chart</option>
                <option id="{{data.attr_id}}" v-if="!data.show" value="{{data.attr_id}}"
                        v-for="(index,data) in charts">{{data.display_name}}
                </option>
            </select>
        </div>

        <div id="breadcrumbs_container" v-if="hasfilters">
            <div style="float:left;">
                <span class="breadcrumb_container">Your selections: </span>
            </div>

            <span class="breadcrumb_container"
                  v-if="customfilter.patientIds.length>0||customfilter.sampleIds.length>0">
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
    <div :class="{'start-loading': showScreenLoad}">
        <div class="grid" id="main-grid"
             :class="{loading:isloading}">
            <main-template :groups.sync="groups" :redrawgroups.sync="redrawgroups"
                           :selectedpatients.sync="selectedpatients"
                           :selectedsamples.sync="selectedsamples"
                           :hasfilters.sync="hasfilters"
                           :customfilter.sync="customfilter"
                           :clear-all="clearAll"></main-template>
        </div>
    </div>

</div>

<script>
    function initdcplots(data, opts) {
        iViz.init(data, opts);

        QueryByGeneTextArea.init('#query-by-gene-textarea', function(genes) {
            iViz.vue.manage.getInstance().$broadcast('gene-list-updated', genes);
        });
    }
</script>
