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

var RightMenuStudyStatsUtil = (function($) {
    var plotTree = function(portalData) {
        function convertToTree(someData) {
            var treeData = {
                name: "All studies",
                children: []
            };

            var cancerTypes = {};
            $.each(portalData.type_of_cancers, function(key, typeStr) {
                cancerTypes[key] = [];
            });

            $.each(portalData.cancer_studies, function(key, cancer_study){
                if(key == "all") { return; }

                var size = 0;
                $.each(cancer_study.case_sets, function(index, caselist){
                    size = Math.max(size, caselist.size);
                });

                cancerTypes[cancer_study.type_of_cancer].push({
                    name: cancer_study.name.replace(/ \(.*\)/g, ""),
                    fullName: cancer_study.name,
                    size: size,
                    parentName: portalData.type_of_cancers[cancer_study.type_of_cancer],
                    linkId: key
                });
            });

            $.each(portalData.type_of_cancers, function(key, typeStr) {
                treeData.children.push({
                    typeId: key,
                    name: typeStr,
                    children: cancerTypes[key]
                });
            });

            return treeData;
        }

        function size(d) {
            return d.size;
        }

        function count(d) {
            return 1;
        }

        function zoom(d) {
            var kx = w / d.dx, ky = h / d.dy;
            x.domain([d.x, d.x + d.dx]);
            y.domain([d.y, d.y + d.dy]);

            var t = svg.selectAll("g.cell").transition()
                .duration(d3.event.altKey ? 7500 : 750)
                .attr("transform", function(d) { return "translate(" + x(d.x) + "," + y(d.y) + ")"; });

            t.select("rect")
                .attr("width", function(d) { return kx * d.dx - 1; })
                .attr("height", function(d) { return ky * d.dy - 1; })

            node = d;
            d3.event.stopPropagation();
        }

        var treeData = convertToTree(portalData);

        var w = 300 - 15,
            h = 300 - 15,
            x = d3.scale.linear().range([0, w]),
            y = d3.scale.linear().range([0, h]),
            color = function(cType) {
                return portalData.cancer_colors[cType];
            },
            root,
            node;

        var treemap = d3.layout.treemap()
            .round(false)
            .size([w, h])
            .sticky(true)
            .value(function(d) { return d.size; });

        var svg = d3.select("#rightmenu-stats-box").append("div")
            .attr("class", "chart")
            .style("width", w + "px")
            .style("height", h + "px")
            .append("svg:svg")
            .attr("width", w)
            .attr("height", h)
            .append("svg:g")
            .attr("transform", "translate(.5,.5)");

        node = root = treeData;

        var nodes = treemap.nodes(root)
            .filter(function(d) { return !d.children; });

        var cell = svg.selectAll("g")
            .data(nodes)
            .enter().append("svg:g")
            .attr("class", "cell")
            .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
            .on("click", function(d) { return zoom(node == d.parent ? root : d.parent); });

        function addTooltip(d, i) {
            $(this).qtip({
                content: "<font size='2'>"
                    + "<center><a href='study.do?cancer_study_id="
                    + d.linkId + "' title='click to see the details of this study'>"
                    + d.fullName.replace("(", "<br>(")
                    + "</a><br>" + d.size + " cases</font></center>",
                show: 'mouseover',
                hide: {
                    fixed:true,
                    delay: 100,
	                event: 'mouseout'
                },
                style: {
                    classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'
                },
                position: {
                    at: 'left bottom',
                    my: 'top right',
                    viewport: $(window)
                }
            });
        }

        cell.append("svg:rect")
            .attr("width", function(d) { return d.dx - 1; })
            .attr("height", function(d) { return d.dy - 1; })
            .style("fill", function(d) { return color(d.parent.typeId); })
            .style("opacity", .6)
            .style("cursor", "pointer")
            .each(addTooltip);

        d3.select(window).on("click", function() { zoom(root); });
    };

    return {plotTree: plotTree}
})(jQuery);



