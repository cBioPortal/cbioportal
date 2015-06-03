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

var SurvivalCurveBroilerPlate = {
	elem : {
	    svg : "",
	    xScale : "",
	    yScale : "",
	    xAxis : "",
	    yAxis : "",
	    line: "", 
	    curve: "",
	    dots: [], //The invisible dots laied on top of the curve for mouse over effect
	    censoredDots: "" 
	},
	settings : {
	    canvas_width: 1005,
	    canvas_height: 620,
	    chart_width: 600,
	    chart_height: 500,
	    chart_left: 100,
	    chart_top: 50,
	    include_info_table: false, //Statistic Results from the curve
		include_legend: true,
		include_pvalue: true,
		pval_x: 710,
		pval_y: 110
	},
	divs : {
		curveDivId : "",
		headerDivId: "",
		infoTableDivId: ""
	},
	subGroupSettings : {
		line_color: "red",
		mouseover_color: "#F5BCA9",
		legend: "",
                curveId: ''//curve unique ID
	},
	text : {
	    xTitle: "",
	    yTitle: "",
		qTips: {
			estimation: "", //example: Survival Estimate: 69.89%
			censoredEvent: "", //example: Time of last observation: 186.7 (months)
			failureEvent: "" //example: Time of death: 86.2 (months)
		},
		infoTableTitles: {
			total_cases: "#total cases",
			num_of_events_cases: "",
			median: ""
		},
		pValTitle: 'Logrank Test P-Value: '
	},
	style : {
	    censored_sign_size: 5,
	    axis_stroke_width: 1,
	    axisX_title_pos_x: 380,
	    axisX_title_pos_y: 600,
	    axisY_title_pos_x: -270,
	    axisY_title_pos_y: 45,
	    axis_color: "black",
		pval_font_size: 12,
		pval_font_style: 'normal'
	},
	vals: {
		pVal: 0
	}	
};

