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
	    canvas_width: 1000,
	    canvas_height: 620,
	    chart_width: 600,
	    chart_height: 500,
	    chart_left: 100,
	    chart_top: 50,
	    include_info_table: false, //Statistic Results from the curve
            include_legend: true,
            include_pvalue: true
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
		}
	},
	style : {
	    censored_sign_size: 5,
	    axis_stroke_width: 1,
	    axisX_title_pos_x: 380,
	    axisX_title_pos_y: 600,
	    axisY_title_pos_x: -270,
	    axisY_title_pos_y: 45,
	    axis_color: "black"
	},
	vals: {
		pVal: 0
	}	
};

