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
 * Generate Simple Plots for specific row in co-expression table
 * (when "expand" button's clicked)
 *
 * Author: Yichao
 * Date: Dec, 2013
 *
 * @param: DivName
 */

var SimplePlot = (function() {
    var canvas = {
            width: 720,
            height: 600
        },
        elem = {
            svg: "",
            xScale: "",
            yScale: "",
            xAxis: "",
            yAxis: ""
        }

    function initScales() {
        elem.xScale = d3.scale.linear()
            .domain([-0.7, 1.7])
            .range([100, 600]);

        elem.yScale = d3.scale.linear()
            .domain([0, 100])
            .range([520, 20]);
    }

    function initAxis() {
        elem.xAxis = d3.svg.axis()
            .scale(elem.xScale)
            .orient("bottom");

        elem.yAxis = d3.svg.axis()
            .scale(elem.yScale)
            .orient("left");
    }

    function initCanvas(divName) {
        $("#" + divName + "_loading_img").hide();
        elem.svg = d3.select("#" + divName).append("svg")
            .attr("width", canvas.width)
            .attr("height", canvas.height);
    }

    function drawAxis() {
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 520)")
            .call(elem.xAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "13px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(0, 20)")
            .call(elem.xAxis.orient("bottom").ticks(0));
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(100, 0)")
            .call(elem.yAxis)
            .selectAll("text")
            .style("font-family", "sans-serif")
            .style("font-size", "13px")
            .style("stroke-width", 0.5)
            .style("stroke", "black")
            .style("fill", "black");
        elem.svg.append("g")
            .style("stroke-width", 2)
            .style("fill", "none")
            .style("stroke", "grey")
            .style("shape-rendering", "crispEdges")
            .attr("transform", "translate(600, 0)")
            .call(elem.yAxis.orient("left").ticks(0));
    }

    function getAlterationData(divName, gene1, gene2) {
        var paramsGetAlterationData = {
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
            gene_list: gene1 + " " + gene2,
            case_set_id: window.PortalGlobals.getCaseSetId(),
            case_ids_key: window.PortalGlobals.getCaseIdsKey()
        };
        $.post("getAlterationData.json", paramsGetAlterationData, getAlterationDataCallBack(divName, gene1, gene2), "json");
    }

    function getAlterationDataCallBack(divName, gene1, gene2) {
        return function(result) {
            console.log(divName);
            console.log(gene1);
            console.log(gene2);
            console.log(result);
            initCanvas(divName);
            initScales();
            initAxis();
            drawAxis();
        }
    }

    return {
        init: function(divName, gene1, gene2) {
            getAlterationData(divName, gene1, gene2);
        }
    }

}());



