/*
 * Copyright (c) 2017 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Hongxin Zhang
 * @author Selcuk Onur Sumer
 */
function AnnotationColumn(oncokbInstanceManager, showHotspot, enableMyCancerGenome)
{
    function render(datum)
    {
        var mutation = datum.mutation;
        var vars = {};
        vars.oncokbId = mutation.get("mutationSid");
        vars.mcgAlt = '';
        vars.changHotspotAlt = '';
        vars.hotspotsImgSrc = "images/cancer-hotspots.svg";
        vars.hotspotsImgWidth = 14;
        vars.hotspotsImgHeight = 14;
        vars.geneSymbol = mutation.get("geneSymbol")
        vars.proteinChange = mutation.get("proteinChange");

        if (enableMyCancerGenome &&
            mutation.get("myCancerGenome") instanceof Array &&
            mutation.get("myCancerGenome").length > 0) {
            vars.mcgAlt = "<b>My Cancer Genome links:</b><br/><ul style=\"list-style-position: inside;padding-left:0;\"><li>" + mutation.get("myCancerGenome").join("</li><li>") + "</li></ul>";
        }

        if (showHotspot && (mutation.get('isHotspot') || mutation.get('is3dHotspot'))) {
            vars.changHotspotAlt = cbio.util.getHotSpotDesc(
                mutation.get('isHotspot'), mutation.get('is3dHotspot'));

            // if it is a 3D hotspot but not a recurrent hotspot, show the 3D hotspot icon
            if (!mutation.get('isHotspot')) {
                vars.hotspotsImgSrc = "images/3d-hotspots.svg";
                vars.hotspotsImgHeight = 18;
            }
        }

        if (_.isUndefined(mutation.get("is3dHotspot")) || _.isUndefined(mutation.get("oncokb")))
        {
            // nested data requests, actual re-rendering is triggered after annotation data is retrieved
            // (by the default callback defined within the requestColumnData function
            datum.table.requestColumnData("hotspot3d", "annotation", function(){
                datum.table.requestColumnData("annotation");
            });
        }

        var templateFn = BackboneTemplateCache.getTemplateFn("mutation_table_annotation_template");
        return templateFn(vars);
    }

    function tooltip(selector, helper)
    {
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

    function tooltipWithManager(selector, helper)
    {
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

    function annotationData(dataProxies, params, callback)
    {
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

                if (_.isFunction(callback)) {
                    callback(params);
                }
            });
        }
    }

    function hotspotData(dataProxies, params, callback)
    {
        var hotspotProxy = dataProxies.hotspots3dProxy;
        var mutations = params.mutations || params.mutationTable.getMutations();

        var geneList = _.uniq(_.map(mutations, function(mutation) {
            return mutation.get("geneSymbol");
        }));

        hotspotProxy.getHotspotsByGene(geneList, function(data) {
            // key => geneSymbol_proteinPosition
            // protienPosition => start[_end]
            var map = {};

            // create a map for a faster lookup
            _.each(data, function(hotspot) {
                var positions = hotspot.residue.match(/[0-9]+/g) || []; // start (and optionally end) positions
                var key = [hotspot.hugoSymbol.toUpperCase()].concat(positions).join("_");
                map[key] = true;
            });

            // check each mutation for 3D hotspot
            _.each(mutations, function(mutation) {
                var key = mutation.get("geneSymbol") + "_" + mutation.get("proteinPosStart");

                if (mutation.get("proteinPosEnd") &&
                    (mutation.get("proteinPosEnd") !== mutation.get("proteinPosStart")))
                {
                    key = key + "_" + mutation.get("proteinPosEnd");
                }

                if (map[key]) {
                    mutation.set({"is3dHotspot": true});
                }
                else {
                    mutation.set({"is3dHotspot": false});
                }

            });

            if (_.isFunction(callback)) {
                callback(params);
            }
        });
    }

    this.render = render;
    this.tooltip = tooltip;
    this.tooltipWithManager = tooltipWithManager;
    this.annotationData = annotationData;
    this.hotspotData = hotspotData;
}
