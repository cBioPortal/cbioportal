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
        <img width='14' height='14' src='images/oncokb-flame.svg' alt='Recurrent Hotspot Symbol'>
    </span>
</script>

<style type="text/css" title="currentStyle">
    @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/mutationMapper.min.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/oncokb.css?<%=GlobalProperties.getAppVersion()%>";
</style>

<%@ include file="oncokb/oncokb-card-template.html" %>
<script type="text/javascript" src="js/src/oncokb/OncoKBCard.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/oncokb/OncoKBConnector.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">

    var oncokbGeneStatus = <%=oncokbGeneStatus%>;
    var showHotspot = <%=showHotspot%>;
    var userName = '<%=userName%>';
    var enableMyCancerGenome = <%=showMyCancerGenomeUrl%>;

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
                vis3d: {
	                //for https, use a proxy since rcsb.org is not serving https and browsers will complain about the mixed https/http content
                    pdbUri: (document.location.protocol != "https:"? "http://files.rcsb.org/view/" : "api/proxy/jsmol/")
                },       
                mutationTable: {
                    columnRender: {
                        annotation: function (datum) {
                            var mutation = datum.mutation;
                            var vars = {};
                            vars.oncokbId = mutation.get("mutationSid");
                            vars.mcgAlt = '';
                            vars.changHotspotAlt = '';

                            if (enableMyCancerGenome &&
                                mutation.get("myCancerGenome") instanceof Array && 
                                mutation.get("myCancerGenome").length > 0) {
                                vars.mcgAlt = "<b>My Cancer Genome links:</b><br/><ul style=\"list-style-position: inside;padding-left:0;\"><li>" + mutation.get("myCancerGenome").join("</li><li>") + "</li></ul>";
                            }

                            if (showHotspot && mutation.get('isHotspot')) {
                                vars.changHotspotAlt = cbio.util.getHotSpotDesc();
                            }

                            if (_.isUndefined(mutation.get("oncokb")))
                            {
                                datum.table.requestColumnData("annotation");
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
                        }
                    },
                    dataTableOpts: {
                        'aaSorting': [[columnOrder.indexOf('annotation'), 'asc']]
                    }
                }
            }
        };

        options = jQuery.extend(true, cbio.util.baseMutationMapperOpts(), options);
        
        if(OncoKB.getAccess()) {
            var oncokbInstanceManager = new OncoKB.addInstanceManager();
            _.each(mutationProxy.getGeneList(), function (gene) {
                var instance = oncokbInstanceManager.addInstance(gene);
                if(oncokbGeneStatus) {
                    instance.setGeneStatus(oncokbGeneStatus);
                }
            });
            jQuery.extend(true, options, {
                dataManager: {
                    dataFn: {
                        annotation: function(dataProxies, params, callback) {
                            var indexMap = params.mutationTable.getIndexMap();
                            var dataTable = params.mutationTable.getDataTable();
                            var tableData = dataTable.fnGetData();
                            var oncokbInstance = oncokbInstanceManager.getInstance(params.mutationTable.getGene());
                            if (tableData.length > 0) {
                                _.each(tableData, function (ele, i) {
                                    var _mutation = ele[indexMap["datum"]].mutation;
                                    oncokbInstance.addVariant(_mutation.get("mutationSid"), '', 
                                                              _mutation.get("geneSymbol"),
                                                              _mutation.get("proteinChange"),
                                                              _mutation.get("tumorType") ? _mutation.get("tumorType") : _mutation.get("cancerType"),
                                                              _mutation.get("mutationType"), 
                                                              _mutation.get("cosmicCount"), 
                                                              _mutation.get("isHotspot"),
                                                              _mutation.get("proteinPosStart"), 
                                                              _mutation.get("proteinPosEnd"));
                                });
                                oncokbInstance.getIndicator().done(function () {
                                    var tableData = dataTable.fnGetData();
                                    if (tableData.length > 0) {
                                        _.each(tableData, function (ele, i) {
                                            if (oncokbInstance.getVariant(ele[indexMap['datum']].mutation.get("mutationSid"))) {
                                                if (oncokbInstance.getVariant(ele[indexMap['datum']].mutation.get("mutationSid")).hasOwnProperty('evidence')) {
                                                    ele[indexMap["datum"]].oncokb = oncokbInstance.getVariant(ele[indexMap['datum']].mutation.get("mutationSid"));
                                                    ele[indexMap['datum']].mutation.set({oncokb: true});
                                                    //dataTable.fnUpdate(null, i, indexMap["annotation"], false, false);
                                                }
                                            }
                                        });
                                        //dataTable.fnUpdate(null, 0, indexMap['annotation']);
                                    }

                                    if (_.isFunction(callback))
                                    {
                                        callback(params);
                                    }
                                });
                            }
                        }
                    }
                },
                view: {
                    mutationTable: {
                        columnTooltips: {
                            annotation: function (selector, helper) {
                                $(selector).find('span.mcg[alt=""]').empty();
                                $(selector).find('span.chang_hotspot[alt=""]').empty();
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
                            }
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
