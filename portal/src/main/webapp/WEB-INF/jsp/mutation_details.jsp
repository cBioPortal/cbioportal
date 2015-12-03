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
    String myCancerGenomeUrl = (String) GlobalProperties.getMyCancerGenomeUrl();
    String oncokbGeneStatus = (String) GlobalProperties.getOncoKBGeneStatus();
    boolean showHotspot = (Boolean) GlobalProperties.showHotspot();
%>

<div class='section' id='mutation_details'>
    <img src='images/ajax-loader.gif'/>
</div>

<script type="text/template" id="mutation_table_protein_change_oncokb_template">
    <span class='{{proteinChangeClass}}' alt='{{proteinChangeTip}}'>
		<a>{{proteinChange}}</a>
	</span>
    <span class='mutation-table-additional-protein-change simple-tip'
          alt='{{additionalProteinChangeTip}}'>
        <img height=12 width=12 style='opacity:0.2' src='images/warning.gif'>
    </span>
    <span class='oncokb oncokb_alteration oncogenic' oncokbId='{{oncokbId}}'>
        <img class='oncokb oncogenic loader' width="13" height="13" class="loader" src="images/ajax-loader.gif"/>
    </span>
    <span class='mcg' alt='{{mcgAlt}}'>
        <img src='images/mcg_logo.png'>
    </span>
    <span class='chang_hotspot' alt='{{changHotspotAlt}}'>
        <img width='13' height='13' src='images/oncokb-flame.svg'>
    </span>
    <a href='{{pdbMatchLink}}' class="mutation-table-3d-link">
        <span class="mutation-table-3d-icon">3D</span>
    </a>
</script>

<script type="text/template" id="mutation_table_oncokb_template">
    <span class='oncokb oncokb_column' oncokbId='{{uniqueId}}'></span>
</script>

<style type="text/css" title="currentStyle">
    @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
    @import "css/mutationMapper.min.css?<%=GlobalProperties.getAppVersion()%>";
</style>
<script type="text/javascript"
        src="js/src/patient-view/OncoKBConnector.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript">

    // TODO 3d Visualizer should be initialized before document get ready
    // ...due to incompatible Jmol initialization behavior
    var _mut3dVis = null;
    var myCancerGenomeUrl = '<%=myCancerGenomeUrl%>';
    var oncokbGeneStatus = <%=oncokbGeneStatus%>;
    var showHotspot = <%=showHotspot%>;
    var enableMyCancerGenome = myCancerGenomeUrl?true:false;

    _mut3dVis = new Mutation3dVis("default3dView");
    _mut3dVis.init();

    // Set up Mutation View
    $(document).ready(function () {
        OncoKB.setUrl('<%=oncokbUrl%>');
        var sampleArray = _.keys(PortalGlobals.getPatientSampleIdMap());
        var mutationProxy = DataProxyFactory.getDefaultMutationDataProxy();


        var options = {
            el: "#mutation_details",
            data: {
                sampleList: sampleArray
            },
            proxy: {
                mutationProxy: {
                    instance: mutationProxy
                }
            }
        };

        if(OncoKB.getAccess()) {
            var oncokbInstance = new OncoKB.Instance();
            if(oncokbGeneStatus) {
                oncokbInstance.setGeneStatus(oncokbGeneStatus);
            }
            options.view = {
                mutationTable: {
                    columns: {
                        oncokb: {
                            sTitle: "oncokb",
                            tip: "OncoKB Annotation",
                            sType: "string",
                            sClass: "center-align-td"
                        }
                    },
                    columnOrder: [
                        "datum", "mutationId", "mutationSid", "caseId", "cancerStudy", "tumorType",
                        "proteinChange", "mutationType", "cna", "cBioPortal", "cosmic", "mutationStatus",
                        "validationStatus", "mutationAssessor", "sequencingCenter", "chr",
                        "startPos", "endPos", "referenceAllele", "variantAllele", "tumorFreq",
                        "normalFreq", "tumorRefCount", "tumorAltCount", "normalRefCount",
                        "normalAltCount", "igvLink", "mutationCount", 'oncokb'
                    ],
                    columnVisibility: {
                        oncokb: 'visible'
                    },
                    columnRender: {
                        oncokb: function (datum) {
                            var mutation = datum.mutation;

                            if (datum.oncokb == null) {
                                // TODO make the image customizable?
                                var vars = {loaderImage: "images/ajax-loader.gif", width: 15, height: 15};
                                var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_placeholder_template");
                                return templateFn(vars);
                            }
                            else {

                                var vars = {};
                                vars.uniqueId = datum.mutation.mutationSid;
                                var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_oncokb_template");
                                return templateFn(vars);
                            }
                        },
                        proteinChange: function (datum) {
                            var mutation = datum.mutation;
                            var proteinChange = MutationDetailsTableFormatter.getProteinChange(mutation);
                            var vars = {};
                            vars.proteinChange = proteinChange.text;
                            vars.proteinChangeClass = proteinChange.style;
                            vars.proteinChangeTip = proteinChange.tip;
                            vars.additionalProteinChangeTip = proteinChange.additionalTip;
                            vars.pdbMatchLink = MutationDetailsTableFormatter.getPdbMatchLink(mutation);
                            vars.oncokbId = mutation.mutationSid;
                            vars.mcgAlt = '';
                            vars.changHotspotAlt = '';

                            if(enableMyCancerGenome && mutation.myCancerGenome instanceof Array && mutation.myCancerGenome.length > 0) {
                                vars.mcgAlt = "<b>My Cancer Genome links:</b><br/><ul style=\"list-style-position: inside;padding-left:0;\"><li>"+mutation.myCancerGenome.join("</li><li>")+"</li></ul>";
                            }

                            if(showHotspot && mutation['isHotspot']) {
                                vars.changHotspotAlt = "<b>Recurrent Hotspot</b><br/>This mutated amino acid was identified as a recurrent hotspot (statistical significance, q-value < 0.01) in a set of 11,119 tumor samples of various cancer types (based on <a href=&quot;http://www.ncbi.nlm.nih.gov/pubmed/26619011&quot; target=&quot;_blank&quot;>Chang, M. et al. Nature Biotech. 2015</a>).";
                            }

                            var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_protein_change_oncokb_template");
                            return templateFn(vars);
                        }
                    },
                    columnTooltips: {
                        oncokb: function (selector, helper) {
                            oncokbInstance.addEvents(selector, 'column');
                        },
                        proteinChange: function (selector, helper) {
                            $(selector).find('span.mcg[alt=""]').remove();
                            $(selector).find('span.chang_hotspot[alt=""]').remove();
                            $(selector).find('span.mutation-table-additional-protein-change[alt=""]').remove();
                            oncokbInstance.addEvents(selector, 'alteration');

                            $(selector).find('span.mcg').qtip({
                                content: {attr: 'alt'},
                                show: {event: "mouseover"},
                                hide: {fixed: true, delay: 100, event: "mouseout"},
                                style: { classes: 'qtip-light qtip-rounded qtip-wide' },
                                position: {my:'top left',at:'bottom center',viewport: $(window)}
                            });

                            $(selector).find('span.chang_hotspot').qtip({
                                content: {attr: 'alt'},
                                show: {event: "mouseover"},
                                hide: {fixed: true, delay: 100, event: "mouseout"},
                                style: { classes: 'qtip-light qtip-rounded qtip-wide' },
                                position: {my:'top left',at:'bottom center',viewport: $(window)}
                            });
                        }
                    },
                    additionalData: {
                        oncokb: function (helper) {
                            var indexMap = helper.indexMap;
                            var dataTable = helper.dataTable;
                            var tableData = dataTable.fnGetData();
                            if (tableData.length > 0) {
                                _.each(tableData, function (ele, i) {
                                    var _datum = ele[indexMap["datum"]];
                                    var _mutation = ele[indexMap["datum"]].mutation;
                                    oncokbInstance.addVariant(_mutation.mutationSid, _mutation.geneSymbol, _mutation.proteinChange, _mutation.tumorType, null);
                                });
                                oncokbInstance.getEvidence().done(function () {
                                    var tableData = dataTable.fnGetData();
                                    if (tableData.length > 0) {
                                        _.each(tableData, function (ele, i) {
                                            if (oncokbInstance.getVariant(ele[indexMap['datum']].mutation.mutationSid)) {
                                                if (oncokbInstance.getVariant(ele[indexMap['datum']].mutation.mutationSid).hasOwnProperty('evidence')) {
                                                    ele[indexMap["datum"]].oncokb = oncokbInstance.getVariant(ele[indexMap['datum']].mutation.mutationSid).evidence;
                                                    dataTable.fnUpdate(null, i, indexMap["oncokb"], false, false);
                                                }
                                            }
                                        });
                                        dataTable.fnUpdate(null, 0, indexMap['oncokb']);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }else{
            options.view = {
                mutationTable: {
                    columnRender: {
                        proteinChange: function (datum) {
                            var mutation = datum.mutation;
                            var proteinChange = MutationDetailsTableFormatter.getProteinChange(mutation);
                            var vars = {};
                            vars.proteinChange = proteinChange.text;
                            vars.proteinChangeClass = proteinChange.style;
                            vars.proteinChangeTip = proteinChange.tip;
                            vars.additionalProteinChangeTip = proteinChange.additionalTip;
                            vars.pdbMatchLink = MutationDetailsTableFormatter.getPdbMatchLink(mutation);
                            vars.oncokbId = mutation.mutationSid;
                            vars.mcgAlt = '';
                            vars.changHotspotAlt = '';

                            if(enableMyCancerGenome && mutation.myCancerGenome instanceof Array && mutation.myCancerGenome.length > 0) {
                                vars.mcgAlt = "<b>My Cancer Genome links:</b><br/><ul style=\"list-style-position: inside;padding-left:0;\"><li>"+mutation.myCancerGenome.join("</li><li>")+"</li></ul>";
                            }

                            if(showHotspot && mutation['isHotspot']) {
                                vars.changHotspotAlt = "<b>Recurrent Hotspot</b><br/>This mutated amino acid was identified as a recurrent hotspot (statistical significance, q-value < 0.01) in a set of 11,119 tumor samples of various cancer types (based on <a href=&quot;http://www.ncbi.nlm.nih.gov/pubmed/26619011&quot; target=&quot;_blank&quot;>Chang, M. et al. Nature Biotech. 2015</a>).";
                            }

                            var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_protein_change_oncokb_template");
                            return templateFn(vars);
                        }
                    },
                    columnTooltips: {
                        proteinChange: function (selector, helper) {
                            $(selector).find('span.oncokb').remove();
                            $(selector).find('span.mcg[alt=""]').remove();
                            $(selector).find('span.chang_hotspot[alt=""]').remove();
                            $(selector).find('span.mutation-table-additional-protein-change[alt=""]').remove();
                            $(selector).find('span.mcg').qtip({
                                content: {attr: 'alt'},
                                show: {event: "mouseover"},
                                hide: {fixed: true, delay: 100, event: "mouseout"},
                                style: { classes: 'qtip-light qtip-rounded qtip-wide' },
                                position: {my:'top left',at:'bottom center',viewport: $(window)}
                            });

                            $(selector).find('span.chang_hotspot').qtip({
                                content: {attr: 'alt'},
                                show: {event: "mouseover"},
                                hide: {fixed: true, delay: 100, event: "mouseout"},
                                style: { classes: 'qtip-light qtip-rounded qtip-wide' },
                                position: {my:'top left',at:'bottom center',viewport: $(window)}
                            });
                        }
                    }
                }
            }
        }
        var defaultView = MutationViewsUtil.initMutationMapper("#mutation_details",
                options,
                "#tabs",
                "Mutations",
                _mut3dVis);
    });

</script>