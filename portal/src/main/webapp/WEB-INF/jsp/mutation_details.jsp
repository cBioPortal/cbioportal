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
    String oncokbUrl = (String) GlobalProperties.getOncoKBUrl();
    boolean showMyCancerGenomeUrl = (Boolean) GlobalProperties.showMyCancerGenomeUrl();
    String oncokbGeneStatus = (String) GlobalProperties.getOncoKBGeneStatus();
    boolean showHotspot = (Boolean) GlobalProperties.showHotspot();
    boolean showCivic = (Boolean) GlobalProperties.showCivic();
    String userName = GlobalProperties.getAuthenticatedUserName();
%>

<div class='section' id='mutation_details'>
    <img src='images/ajax-loader.gif'/>
</div>

<script type="text/template" id="mutation_table_annotation_template">
    <span class='annotation-item oncokb oncokb_alteration oncogenic' oncokbId='{{oncokbId}}'>
        <img class='oncokb oncogenic' width="14" height="14" src="images/ajax-loader.gif"/>
    </span>
    <span class='annotation-item mcg' alt='{{mcgAlt}}'>
        <img width='14' height='14' src='images/mcg_logo.png'>
    </span>
    <span class='annotation-item chang_hotspot' alt='{{changHotspotAlt}}'>
        <img width='14' height='14' src='images/oncokb-flame.svg'>
    </span>
    <span class='annotation-item civic' alt='{{civicAlt}}'>
        <img width='14' height='14' src='images/civic-logo.png'>
    </span>
</script>

<style type="text/css" title="currentStyle">
    @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/mutationMapper.min.css?<%=GlobalProperties.getAppVersion()%>";
</style>
<script type="text/javascript"
        src="js/src/OncoKBConnector.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript">

    // TODO 3d Visualizer should be initialized before document get ready
    // ...due to incompatible Jmol initialization behavior
    var _mut3dVis = null;
    var oncokbGeneStatus = <%=oncokbGeneStatus%>;
    var showHotspot = <%=showHotspot%>;
    var showCivic = <%=showCivic%>;
    var userName = '<%=userName%>';
    var enableMyCancerGenome = <%=showMyCancerGenomeUrl%>;

    // temporary fix for webGL incompatibility 
    try {
        _mut3dVis = new Mutation3dVis("default3dView", {
            pdbUri: "api/proxy/jsmol/"
        });
        _mut3dVis.init();
    } catch (e) {
        console.log(e);
    }

    // Set up Mutation View
    $(document).ready(function () {
        var sampleArray = window.QuerySession.getSampleIds();
        OncoKB.setUrl('<%=oncokbUrl%>');
        var mutationProxy = DataProxyFactory.getDefaultMutationDataProxy();

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
                }
            },
            view: {
                mutationTable: {
                    columnRender: {
                        annotation: function (datum) {
                            var mutation = datum.mutation;
                            var vars = {};
                            vars.oncokbId = mutation.mutationSid;
                            vars.mcgAlt = '';
                            vars.changHotspotAlt = '';
                            vars.civicAlt = '';

                            if (enableMyCancerGenome && mutation.myCancerGenome instanceof Array && mutation.myCancerGenome.length > 0) {
                                vars.mcgAlt = "<b>My Cancer Genome links:</b><br/><ul style=\"list-style-position: inside;padding-left:0;\"><li>" + mutation.myCancerGenome.join("</li><li>") + "</li></ul>";
                            }

                            if (showHotspot && mutation['isHotspot']) {
                                vars.changHotspotAlt = "<b>Recurrent Hotspot</b><br/>This mutated amino acid was identified as a recurrent hotspot (statistical significance, q-value < 0.01) in a set of 11,119 tumor samples of various cancer types (based on <a href=&quot;http://www.ncbi.nlm.nih.gov/pubmed/26619011&quot; target=&quot;_blank&quot;>Chang, M. et al. Nature Biotech. 2015</a>).";
                            }

                            if (showCivic && (mutation.civicVariantSummaries.length > 0 ||
                                        mutation.civicClinicalEvidenceStats.length > 0)) {
                                var variantSummaries = mutation.civicVariantSummaries;
                                var clinicalEvidenceStats = mutation.civicClinicalEvidenceStats;

                                if ($(variantSummaries).size() == 0 && $(clinicalEvidenceStats).size() == 0)
                                    return;

                                // Get summary counts for clinical evidence
                                var numDiagnostic = 0;
                                var numPrognostic = 0;
                                var numPredictive = 0;
                                if ($(clinicalEvidenceStats).size() > 0) {
                                    numDiagnostic = clinicalEvidenceStats[0].numDiagnostic;
                                    numPrognostic = clinicalEvidenceStats[0].numPrognostic;
                                    numPrognostic = clinicalEvidenceStats[0].numPrognostic;
                                }

                                // Link out to CIVIC
                                var variantCivicUrl = "https://civic.genome.wustl.edu/#/home";
                                if ($(variantSummaries).size())
                                    variantCivicUrl = variantSummaries[0].variantCivicUrl;

                                civicHTML = "<div>";
                                civicHTML += "<a href=\"" + variantCivicUrl + "\" target=\"_blank\">CIVIC</a> has ";
                                civicHTML += numDiagnostic + " diagnostic, " + numPredictive + " predictive, and ";
                                civicHTML += numPrognostic + " prognostic entries for this variant.";
                                if ($(variantSummaries).size() > 0)
                                    civicHTML += "Variant Summaries: <ul>";
                                $(variantSummaries).each(function(key, info) {
                                    civicHTML += "<li>" + info.summary + "</li>";
                                });
                                if ($(variantSummaries).size() > 0) {
                                    civicHTML += "</ul>";
                                    civicHTML += "More and updated information in <a href=\"" + variantCivicUrl + "\" target=\"_blank\">CIVIC</a>.";
                                }
                                civicHTML += "</div>";

                                vars.civicAlt = civicHTML;
                            }

                            var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_annotation_template");
                            return templateFn(vars);
                        }
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
                        annotation: function (selector, helper) {
                            $(selector).find('span.oncokb').remove();
                            $(selector).find('span.mcg[alt=""]').empty();
                            $(selector).find('span.chang_hotspot[alt=""]').empty();
                            $(selector).find('span.civic[alt=""]').empty();
                            $(selector).find('span.mutation-table-additional-protein-change[alt=""]').remove();
                            $(selector).find('span.mcg').one('mouseenter', function () {
                                $(this).qtip({
                                    content: {attr: 'alt'},
                                    show: {event: "mouseover", ready: true},
                                    hide: {fixed: true, delay: 100, event: "mouseout"},
                                    style: {classes: 'qtip-light qtip-rounded qtip-wide'},
                                    position: {my: 'top left', at: 'bottom center', viewport: $(window)}
                                });

                            });

                            $(selector).find('span.chang_hotspot').one('mouseenter', function () {
                                $(this).qtip({
                                    content: {attr: 'alt'},
                                    show: {event: "mouseover", ready: true},
                                    hide: {fixed: true, delay: 100, event: "mouseout"},
                                    style: {classes: 'qtip-light qtip-rounded qtip-wide'},
                                    position: {my: 'top left', at: 'bottom center', viewport: $(window)}
                                });
                            });

                            $(selector).find('span.civic').one('mouseenter', function () {
                                $(this).qtip({
                                    content: {attr: 'alt'},
                                    show: {event: "mouseover", ready: true},
                                    hide: {fixed: true, delay: 100, event: "mouseout"},
                                    style: {classes: 'qtip-civic qtip-light qtip-rounded qtip-wide'},
                                    position: {my: 'top left', at: 'bottom center', viewport: $(window)}
                                });
                            });

                        }
                    },
                    dataTableOpts: {
                        'aaSorting': [[columnOrder.indexOf('annotation'), 'asc']]
                    }
                }
            }
        };

        if(OncoKB.getAccess()) {
            var oncokbInstanceManager = new OncoKB.addInstanceManager();
            _.each(mutationProxy.getGeneList(), function (gene) {
                var instance = oncokbInstanceManager.addInstance(gene);
                if(oncokbGeneStatus) {
                    instance.setGeneStatus(oncokbGeneStatus);
                }
            });
            jQuery.extend(true, options, {
                view: {
                    mutationTable: {
                        columnTooltips: {
                            annotation: function (selector, helper) {
                                $(selector).find('span.mcg[alt=""]').empty();
                                $(selector).find('span.chang_hotspot[alt=""]').empty();
                                $(selector).find('span.civic[alt=""]').empty();
                                oncokbInstanceManager.getInstance(helper.gene).addEvents(selector, 'column');
                                oncokbInstanceManager.getInstance(helper.gene).addEvents(selector, 'alteration');

                                $(selector).find('span.mcg').one('mouseenter', function () {
                                    $(this).qtip({
                                        content: {attr: 'alt'},
                                        show: {event: "mouseover", ready: true},
                                        hide: {fixed: true, delay: 100, event: "mouseout"},
                                        style: {classes: 'qtip-light qtip-rounded qtip-wide'},
                                        position: {my: 'top left', at: 'bottom center', viewport: $(window)}
                                    });

                                });

                                $(selector).find('span.chang_hotspot').one('mouseenter', function () {
                                    $(this).qtip({
                                        content: {attr: 'alt'},
                                        show: {event: "mouseover", ready: true},
                                        hide: {fixed: true, delay: 100, event: "mouseout"},
                                        style: {classes: 'qtip-light qtip-rounded qtip-wide'},
                                        position: {my: 'top left', at: 'bottom center', viewport: $(window)}
                                    });
                                });

                                $(selector).find('span.civic').one('mouseenter', function () {
                                    $(this).qtip({
                                        content: {attr: 'alt'},
                                        show: {event: "mouseover", ready: true},
                                        hide: {fixed: true, delay: 100, event: "mouseout"},
                                        style: {classes: 'qtip-civic qtip-light qtip-rounded qtip-wide'},
                                        position: {my: 'top left', at: 'bottom center', viewport: $(window)}
                                    });
                                });
                            }
                        },
                        additionalData: {
                            annotation: function (helper) {
                                var indexMap = helper.indexMap;
                                var dataTable = helper.dataTable;
                                var tableData = dataTable.fnGetData();
                                var oncokbInstance = oncokbInstanceManager.getInstance(helper.gene);
                                if (tableData.length > 0) {
                                    _.each(tableData, function (ele, i) {
                                        var _datum = ele[indexMap["datum"]];
                                        var _mutation = ele[indexMap["datum"]].mutation;
                                        oncokbInstance.addVariant(_mutation.mutationSid, '', _mutation.geneSymbol, 
                                            _mutation.proteinChange,
                                            _mutation.tumorType ? _mutation.tumorType : _mutation.cancerType, 
                                            _mutation.mutationType, _mutation.cosmicCount, _mutation.isHotspot,
                                            _mutation.civicVariantSummaries, _mutation.civicClinicalEvidenceStats,
                                            _mutation.proteinPosStart, _mutation.proteinPosEnd);
                                    });
                                    oncokbInstance.getIndicator().done(function () {
                                        var tableData = dataTable.fnGetData();
                                        if (tableData.length > 0) {
                                            _.each(tableData, function (ele, i) {
                                                if (oncokbInstance.getVariant(ele[indexMap['datum']].mutation.mutationSid)) {
                                                    if (oncokbInstance.getVariant(ele[indexMap['datum']].mutation.mutationSid).hasOwnProperty('evidence')) {
                                                        ele[indexMap["datum"]].oncokb = oncokbInstance.getVariant(ele[indexMap['datum']].mutation.mutationSid);
                                                        dataTable.fnUpdate(null, i, indexMap["annotation"], false, false);
                                                    }
                                                }
                                            });
                                            dataTable.fnUpdate(null, 0, indexMap['annotation']);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
        }

        var defaultView = MutationViewsUtil.initMutationMapper("#mutation_details",
                options,
                "#tabs",
                "Mutations",
                _mut3dVis);
    });

</script>
