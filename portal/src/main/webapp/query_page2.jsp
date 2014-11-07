<%-- 
    Document   : query_page
    Created on : Sep 26, 2014, 1:19:14 PM
    Author     : abeshoua
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html ng-app="query-page-module" ng-controller="mainController">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link type="text/css" rel="stylesheet" href="css/bootstrap-chosen.css"/>
        <link type="text/css" rel="stylesheet" href="css/chosen-spinner.css"/>
        <link type="text/css" rel="stylesheet" href="css/query_page.css"/>
    </head>
    <body>
        <jsp:include page="header.html" /> 
        <!-- STEP 1 -->
        <div id="mainForm" class="container">
            <div class="row">
                <div class="col-lg-4 col-md-4 col-sm-4 col-xs-4 leftColumn"><span>Select Cancer Study:</span></div>
                <div class="col-lg-8 col-md-8 col-sm-8 col-xs-8">
                    <select chosen ng-model="formVars.cancer_study_id"
                            ng-options="csId as csObj.name group by appVars.vars.types_of_cancer[csObj.type_of_cancer] for (csId, csObj) in appVars.vars.cancer_study_stubs"
                    >
                    </select>
                </div>
            </div>
            <div class="row hidden" ng-hide="formVars.cancer_study_id === 'all'">
                <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                    <p class="leftFloat" ng-bind-html="appVars.vars.cancer_studies[formVars.cancer_study_id].description | to_trusted_html"></p>
                    <button class="btn btn-sm leftFloat">Study Summary</button>
                </div>
            </div>
            <!-- STEP 2 (CROSS-STUDY) -->
            <div class="row" ng-show="formVars.cancer_study_id === 'all'">
                <div class="col-lg-4 col-md-4 col-sm-4 col-xs-4 leftColumn"><span>Select Data Type Priority:</span></div>
                <div class="col-lg-8 col-md-8 col-sm-8 col-xs-8">
                    <div ng-repeat="datap in appVars.vars.data_priorities">
                        <input type="radio" name="data_priority" ng-model="formVars.data_priority" value="{{datap.id}}">{{datap.label}}
                    </div>
                </div>
            </div>
            <!-- STEP 2 (SINGLE STUDY) -->
            <div class="row" ng-hide="formVars.cancer_study_id === 'all'">
                <div class="col-lg-4 col-md-4 col-sm-4 col-xs-4 leftColumn"><span>Select Genomic Profiles:</span></div>
                <div class="col-lg-8 col-md-8 col-sm-8 col-xs-8">
                    <!-- we have to do (index, profgp) instead of just track by index because otherwise angular doesnt register the change -->
                    <!-- , even though we don't actually use 'index' -->
                    <div ng-repeat="(index, profgp) in appVars.vars.ordered_profile_groups">
                        <div profile-group>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row"  ng-hide="formVars.cancer_study_id === 'all'">
                <div class="col-lg-4 col-md-4 col-sm-4 col-xs-4 leftColumn"><span>Select Patient/Case Set:</span></div>
                <div class="col-lg-8 col-md-8 col-sm-8 col-xs-8">
                    <select chosen ng-model="formVars.case_set_id"
                            ng-options="cs.id as cs.label for cs in appVars.vars.case_sets"
                            >
                    </select>
                    <div ng-show="formVars.case_set_id === '-1'">
                        <textarea class="form-control" ng-model="formVars.custom_case_list" rows="6" cols="80" placeholder="Enter case IDs"></textarea>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-4 col-md-4 col-sm-4 col-xs-4 leftColumn"><span>Enter Gene Set:</span><br/><a target="_blank" href="http://www.cbioportal.org/public-portal/onco_query_lang_desc.jsp">Advanced: Onco Query Language (OQL)</a></div>
                <div class="col-lg-8 col-md-8 col-sm-8 col-xs-8">
                    <select chosen ng-model="appVars.vars.gene_set_id"
                    ng-options="id as gs.name for (id,gs) in appVars.vars.gene_set_stubs"
                    ng-change="insertGeneList()"
                    >
                    </select>
                    <textarea class="form-control" ng-model="formVars.oql_query" rows="6" cols="80" placeholder="Enter HUGO Gene Symbols or Gene Aliases"></textarea>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-4 col-md-4 col-sm-4 col-xs-4 leftColumn"></div>
                <div class="col-lg-8 col-md-8 col-sm-8 col-xs-8">
                    <button type="button" class="btn btn-mskcc btn-sm" ng-click="submitForm()">Submit</button>
                    &nbsp;&nbsp;
                    <button type="button" class="btn btn-mskcc btn-sm" ng-click="formVars.clear()">Clear</button>
                    <!--<button type="button" class="btn btn-mskcc btn-sm" ng-click="syncToUrl()">Update URL</button>
                    <button type="button" class="btn btn-mskcc btn-sm" ng-click="syncFromUrl()">Get From URL</button>-->
                </div>
            </div>
            <div class="row">
                <p ng-show="appVars.vars.error_msg.length > 0" style="color:red">{{appVars.vars.error_msg}}</p>
            </div>
            <div class="row">
                <table ng-show="!isEmpty(appVars.vars.query_result.samples)"  class="table table-responsive table-striped">
                    <tbody>
                        <tr>
                            <td rowspan="2">Sample\ Gene</td>
                            <td ng-repeat="gene in appVars.vars.query_result.genes" colspan="7">{{gene}}</td>
                        </tr>
                        <tr>
                            <!-- 7 is magic number: the number of event types (AMP, GAIN, HOMDEL, HETLOSS, MUT, EXP, PROT) -->
                            <td ng-repeat="i in range(7 * appVars.vars.query_result.genes.length) track by $index">
                                {{appVars.vars.event_types[$index % 7]}}
                            </td>
                        </tr>
                        <tr ng-repeat="(id, samp) in appVars.vars.query_result.samples">
                            <td>{{id}}</td>
                            <td ng-repeat="i in range(7 * appVars.vars.query_result.genes.length) track by $index">
                                {{samp[appVars.vars.query_result.genes[Math.floor($index / 7)]][appVars.vars.event_types[$index % 7]]}}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <script type="text/javascript" src="js/lib/jquery.min.js"></script>
        <script type="text/javascript" src="js/src/query-page/angular.min.js"></script>
        <script type="text/javascript" src="js/src/query-page/ui-bootstrap-tpls-0.11.2.min.js"></script>
        <script type="text/javascript" src="js/lib/chosen.jquery.angular.js"></script>
        <script type="text/javascript" src="js/lib/chosen-angular.js"></script>
        <script type="text/javascript" src="js/api/cbioportal-webservice.js"></script>
        <script type="text/javascript" src="js/api/cbioportal-datamanager.js"></script>
        
        <script type="text/javascript" src="js/lib/oql-parser.js"></script>
        <script type="text/javascript" src="js/lib/oql.js"></script>
        <script type="text/javascript" src="js/src/query-page/query_page.js"></script>
    </body>
</html>