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
            gene_both_mut: "Both mutated",
            non_mut: "Neither mutated"
        }
    },
};
