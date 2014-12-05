<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticAlterationType" %>

<script type="text/javascript" src="js/lib/angular.min.js"></script> 
<script type="text/javascript" src="js/src/plots-tab/view/menu.js"></script>

<style>
    #plots .plots {
        border: 1px solid #aaaaaa;
        border-radius: 4px;
        margin: 10px;
    }
    #plots-sidebar {
        width: 350px;
        height: 610px;
    }
    #plots-sidebar-x {
        width: inherit;
        height: 250px;
    }
    #plots-sidebar-y {
        width: inherit;
        height: 250px;
    }
    #plots-sidebar-search {
        width: inherit;
        height: 50px;
    }
    #plots-sidebar h3 {
        margin: 10px;
    }
    #plots-sidebar h4 {
        margin: 10px;
    }
    #plots-sidebar h5 {
        margin-top: -5px;
        margin-left: 20px;
        padding-left: 5px;
        padding-right: 5px;
        display: inline-block;
        font-weight: bold;  
        margin-bottom: 20px;
    }
    #plots-sidebar select {
        max-width: 180px;
    }
    #plots-content {
        width: 800px;
        height: 610px;
    }
</style>


<div id="plots" class="section" np-app="menuApp">
    <div id="plots-sidebar" ng-controller='MenuController as menu'>
        <h3>Plots Parameters</h3>
        <div id="plots-sidebar-x" class="plots">
            <h4>X Axis</h4>
            <br><h5>{{plotsType.title}}</h5>
            <select id="plots-x-plots-type" ng-model="form.plotsTypeX" ng-options="option.value as option.label for option in plotsType.options"></select>
            <!--genomic profile specs-->
            <div ng-show="form.plotsTypeX==='genomic_profile'">
                <h5>{{profileType.title}}</h5>
                <select id="plots-x-profile-type" ng-model="form.profileTypeX" ng-options="option.value as option.label for option in profileType.options"></select>
                <br><h5>{{profileName.title}}</h5>
                <select id="plots-x-profile-name" ng-model="form.profileNameX" ng-options="option.value as option.label for option in profileName.options | filter: {value: }"></select>
                <br><h5>{{gene.title}}</h5>
                <select id="plots-x-gene" ng-model="form.geneX" ng-options="option.value as option.label for option in gene.options"></select>
            </div>
            <!--clinical attr specs-->
            <div ng-show="form.plotsTypeX==='clinical_attr'">
                <h5>{{clinicalAttr.title}}</h5>
                <select id="plots-x-clincal-attr" ng-model="form.clinicalAttrX" ng-options="option.value as option.label for option in clinicalAttr.options"></select>
            </div>
        </div>
        <div id="plots-sidebar-y" class="plots">
            <h4>Y Axis</h4>
            <br><h5>{{plotsType.title}}</h5>
            <select id="plots-y-plots-type" ng-model="form.plotsTypeY" ng-options="option.value as option.label for option in plotsType.options"></select>
            <!--genomic profile specs-->
            <div ng-show="form.plotsTypeY==='genomic_profile'">
                <h5>{{profileType.title}}</h5>
                <select id="plots-y-profile-type" ng-model="form.profileTypeY" ng-options="option.value as option.label for option in profileType.options"></select>
                <br><h5>{{profileName.title}}</h5>
                <select id="plots-y-profile-name" ng-model="form.profileNameY" ng-options="option.value as option.label for option in profileName.options"></select>
                <br><h5>{{gene.title}}</h5>
                <select id="plots-y-gene" ng-model="form.geneY" ng-options="option.value as option.label for option in gene.options"></select>
            </div>
            <!--clinical attr specs-->
            <div ng-show="form.plotsTypeY==='clinical_attr'">
                <h5>{{clinicalAttr.title}}</h5>
                <select id="plots-y-clincal-attr" ng-model="form.clinicalAttrY" ng-options="option.value as option.label for option in clinicalAttr.options"></select>
            </div>
        </div>
        <div id="plots-sidebar-search" class="plots">
            <h4>Search</h4>
        </div>
    </div>
</div>
