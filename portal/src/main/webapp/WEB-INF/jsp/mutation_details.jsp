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
<%
    String oncokbUrl = (String) GlobalProperties.getOncoKBApiUrl();
    boolean showMyCancerGenomeUrl = (Boolean) GlobalProperties.showMyCancerGenomeUrl();
    String oncokbGeneStatus = (String) GlobalProperties.getOncoKBGeneStatus();
    boolean showHotspot = (Boolean) GlobalProperties.showHotspot();
    boolean showCivic = (Boolean) GlobalProperties.showCivic();
    String civicUrl = (String) GlobalProperties.getCivicUrl();
    String userName = GlobalProperties.getAuthenticatedUserName();
%>

<div class='section' id='mutation_details'>
    <img src='images/ajax-loader.gif' alt='loading'/>
</div>

<script type="text/template" id="mutation_table_annotation_template">
    <span class='annotation-item oncokb oncokb_alteration oncogenic' oncokbId='{{oncokbId}}'>
        <img class='oncokb oncogenic' width="14" height="14" src="images/ajax-loader.gif" alt='loading'/>
    </span>
    <span class='annotation-item mcg' alt='{{mcgAlt}}'>
        <img width='14' height='14' src='images/mcg_logo.png' alt='My Cancer Genome Symbol'>
    </span>
    <span class='annotation-item chang_hotspot' alt='{{changHotspotAlt}}'>
        <img width='{{hotspotsImgWidth}}' height='{{hotspotsImgHeight}}' src='{{hotspotsImgSrc}}' alt='Recurrent Hotspot Symbol'>
    </span>
    <% if (showCivic) { %>
    <span class='annotation-item civic' proteinChange='{{proteinChange}}' geneSymbol='{{geneSymbol}}'>
        <img width='14' height='14' src='images/ajax-loader.gif' alt='Civic Variant Entry'>
    </span>
    <% } %>
</script>

<style type="text/css" title="currentStyle">
    @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/mutationMapper.min.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/oncokb.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/civic.css?<%=GlobalProperties.getAppVersion()%>";
</style>

<%@ include file="oncokb/oncokb-card-template.html" %>
<%@ include file="civic/civic-qtip-template.html" %>
<script type="text/javascript" src="js/src/civic/civicservice.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/oncokb/OncoKBCard.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/oncokb/OncoKBConnector.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/data/Hotspots3dDataProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/mutation/column/AnnotationColumn.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">

    var oncokbGeneStatus = <%=oncokbGeneStatus%>;
    var showHotspot = <%=showHotspot%>;
    var showCivic = <%=showCivic%>;
    var civicUrl = '<%=civicUrl%>';
    var userName = '<%=userName%>';
    var enableMyCancerGenome = <%=showMyCancerGenomeUrl%>;

    // Set up Mutation View
    $(document).ready(function () {
        var sampleArray = window.QuerySession.getSampleIds();
        OncoKB.setUrl('<%=oncokbUrl%>');
        var mutationProxy = DataProxyFactory.getDefaultMutationDataProxy();
        var annotationCol = null;

        if(OncoKB.getAccess()) {
            var oncokbInstanceManager = new OncoKB.addInstanceManager();

            _.each(mutationProxy.getGeneList(), function (gene) {
                var instance = oncokbInstanceManager.addInstance(gene);
                if (oncokbGeneStatus) {
                    instance.setGeneStatus(oncokbGeneStatus);
                }
            });

            annotationCol = new AnnotationColumn(oncokbInstanceManager, showHotspot, enableMyCancerGenome);
        }
        else {
            annotationCol = new AnnotationColumn(null, showHotspot, enableMyCancerGenome);
        }

        var columnOrder = [
            "datum", "mutationId", "mutationSid", "caseId", "cancerStudy", "tumorType",
            "proteinChange", "annotation", "mutationType", "cna", "cBioPortal", "cosmic", "mutationStatus",
            "validationStatus", "mutationAssessor", "sequencingCenter", "chr",
            "startPos", "endPos", "referenceAllele", "variantAllele", "tumorFreq",
            "normalFreq", "tumorRefCount", "tumorAltCount", "normalRefCount",
            "normalAltCount", "igvLink", "mutationCount"
        ];
        var options = {
            el: "#mutation_details",
            data: {
                sampleList: sampleArray
            },
            proxy: {
                mutationProxy: {
                    instance: mutationProxy
                },
                hotspots3dProxy: {
                    instanceClass: Hotspots3dDataProxy
                }
            },
            view: {
                vis3d: {
	                // use https for all portal instances
                    pdbUri: "https://files.rcsb.org/view/"
                },       
                mutationTable: {
                    columnRender: {
                        annotation: annotationCol.render
                    },
                    columnVisibility: {
                        annotation: 'visible'
                    },
                    columns: {
                        annotation: {
                            sTitle: "Annotation",
                            tip: "",
                            sType: "sort-icons",
                            sClass: "left-align-td"
                        }
                    },
                    columnOrder: columnOrder,
                    columnSort: {
                        "annotation": function (datum) {
                            return datum;
                        }
                    },
                    columnTooltips: {
                        annotation: annotationCol.tooltip
                    },
                    dataTableOpts: {
                        'aaSorting': [[columnOrder.indexOf('annotation'), 'asc']]
                    }
                }
            }
        };

        options = jQuery.extend(true, cbio.util.baseMutationMapperOpts(), options);
        
        if(OncoKB.getAccess()) {
            jQuery.extend(true, options, {
                dataManager: {
                    dataFn: {
                        annotation: annotationCol.annotationData,
                        hotspot3d: annotationCol.hotspotData
                    }
                },
                view: {
                    mutationTable: {
                        columnTooltips: {
                            annotation: annotationCol.tooltipWithManager
                        }
                    }
                }
            });
        }

        var defaultView = MutationViewsUtil.initMutationMapper("#mutation_details",
                options,
                "#tabs",
                "Mutations");
    });

</script>
