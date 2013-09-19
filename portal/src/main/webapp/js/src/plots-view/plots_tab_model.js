/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

/**
 *
 * - Generate the plots tab "global" data object (Being used in every sub tabs)
 * - AJAX data retrieving function (using JSON servlet)
 * - Cache every generated data set in a global variable
 *
 */

var Plots = (function(){

    var genetic_profile = {
            genetic_profile_mutations : [],
            genetic_profile_mrna : [],
            genetic_profile_copy_no : [],
            genetic_profile_rppa : [],
            genetic_profile_dna_methylation : []
        },
        genetic_profiles = {};

    function getGeneticProfileCallback(result) {
        for (var gene in result) {
            var _obj = result[gene];
            var _genetic_profile = jQuery.extend(true, {}, genetic_profile);
            for (var key in _obj) {
                var obj = _obj[key];
                var profile_type = obj.GENETIC_ALTERATION_TYPE;
                if (profile_type === "MUTATION_EXTENDED") {
                    _genetic_profile.genetic_profile_mutations.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
                } else if(profile_type === "COPY_NUMBER_ALTERATION") {
                    _genetic_profile.genetic_profile_copy_no.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
                } else if(profile_type === "MRNA_EXPRESSION") {
                    _genetic_profile.genetic_profile_mrna.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
                } else if(profile_type === "METHYLATION") {
                    _genetic_profile.genetic_profile_dna_methylation.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
                } else if(profile_type === "PROTEIN_ARRAY_PROTEIN_LEVEL") {
                    _genetic_profile.genetic_profile_rppa.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
                }
            }
            genetic_profiles[gene] = _genetic_profile;
        }

        PlotsMenu.init();
        PlotsTwoGenesMenu.init();
        PlotsCustomMenu.init();
        PlotsView.init();

        $('#plots-menus').bind('tabsshow', function(event, ui) {
            if (ui.index === 0) {
                PlotsView.init();
            } else if (ui.index === 1) {
                PlotsTwoGenesView.init();
            } else if (ui.index === 2) {
                PlotsCustomView.init();
            } else {
                //TODO: error handle
            }
        });

    }

    function addAxisHelp(svg, axisGroupSvg, xTitle, yTitle, xTitleClass, yTitleClass, xText, yText) {  //Append description for selected genetic profile
        axisGroupSvg.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", xTitleClass)
            .attr("x", 350 + xTitle.length / 2 * 8)
            .attr("y", 567)
            .attr("width", "16")
            .attr("height", "16");
        axisGroupSvg.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", yTitleClass)
            .attr("x", 34)
            .attr("y", 255 - yTitle.length / 2 * 8)
            .attr("width", "16")
            .attr("height", "16");
        svg.select("." + xTitleClass).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + xText + "</font>" },
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right'}
                    }
                );
            }
        );
        svg.select("." + yTitleClass).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + yText + "</font>"},
                        style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'right bottom',at:'top left'}
                    }
                );
            }
        );
    }

    return {
        init: function() {
            var paramsGetProfiles = {
                cancer_study_id: cancer_study_id,
                case_set_id: case_set_id,
                case_ids_key: case_ids_key,
                gene_list: gene_list_str
            };
            $.post("getGeneticProfile.json", paramsGetProfiles, getGeneticProfileCallback, "json");
        },
        getGeneticProfiles: function(selectedGene) {
            return genetic_profiles[selectedGene];
        },
        getProfileData: function(gene, genetic_profile_id, case_set_id, case_ids_key, callback_func) {
            var paramsGetProfileData = {
                gene_list: gene,
                genetic_profile_id: genetic_profile_id,
                case_set_id: case_set_id,
                case_ids_key: case_ids_key
            };
            $.post("getProfileData.json", paramsGetProfileData, callback_func, "json");
        },
        getMutationType: function(gene, genetic_profile_id, case_set_id, case_ids_key, callback_func) {
            var paramsGetMutationType = {
                geneList: gene,
                geneticProfiles: genetic_profile_id,  //Here is simply cancer_study_id + "_mutations"
                caseSetId: case_set_id,
                caseIdsKey: case_ids_key
            };
            $.post("getMutationData.json", paramsGetMutationType, callback_func, "json");
        },
        addAxisHelp: addAxisHelp
    };

}());    //Closing Plots








