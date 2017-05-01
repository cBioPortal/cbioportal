/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


/**
 *
 * Collection of default/templates for setting/data objects
 *
 * @author: yichao S
 * @date: Jan 2014
 *
 */

var PlotsBoilerplate = {
    datum : {
        x_val: "",
        y_val: "",
        case_id: "",
        qtip: "", //content for qtip
        stroke: "",
        fill: ""
    },
    style : { //Default style setting for data points
        fill: "#58ACFA", //light blue
        stroke: "#0174DF", //dark blue
        stroke_width: "1.2",
        geneset_fill: "#00AAF8",
        geneset_stroke: "grey",
        size: "20",
        shape: "circle" //may vary for different mutation types
    },
    canvas : {  //position of components
        width: 755,
        height: 595,
        xLeft: 120,     //The left/starting point for x axis
        xRight: 620,   //The right/ending point for x axis
        yTop: 15,      //The top/ending point for y axis
        yBottom: 515   //The bottom/starting point for y axis
    },
    elem : {
        svg: "",
        xScale: "",
        yScale: "",
        xAxis: "",
        yAxis: "",
        dotsGroup: "",
        axisGroup: "",
        axisTitleGroup: "",
        brush: ""
    },
    names: { //naming conventions
        header: "_header", 
        body: "_body",  // the actual plots
        div: "", // the overall div name
        loading_img: "_loading_img", //place holding img
        log_scale_x: "_log_scale_x",
        log_scale_y: "_log_scale_y",
        show_mutations: "_show_mutations",
        download_pdf: "_download_pdf",
        download_svg: "_download_svg" 
    },
    text: {
        xTitle: "",
        yTitle: "",
        xTitleHelp: "",
        yTitleHelp: "",
        title: "",
        fileName: "",
        legends: {
            gene_x_mut: "gene_x mutated",
            gene_y_mut: "gene_y mutated",
            gene_both_mut: "Both genes mutated",
            non_mut: "Genes not mutated",
            gene_x_not_mutated: "gene_x not mutated",
            gene_y_not_mutated: "gene_y not mutated"
        }
    }
};
