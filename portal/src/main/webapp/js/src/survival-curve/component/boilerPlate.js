var SurvivalCurveBroilerPlate = {
	elem : {
	    svg : "",
	    xScale : "",
	    yScale : "",
	    xAxis : "",
	    yAxis : "",
	    line: "",
	    dots: "",
	    censoredDots: ""
	},
	settings : {
	    canvas_width: 1000,
	    canvas_height: 620,
	    altered_line_color: "red",
	    unaltered_line_color: "blue",
	    altered_mouseover_color: "#F5BCA9",
	    unaltered_mouseover_color: "#81BEF7"
	},
	text : {
	    glyph1: "Cases with Alteration(s) in Query Gene(s)",
	    glyph2: "Cases without Alteration(s) in Query Gene(s)",
	    xTitle: "",
	    yTitle: "",
		qTips: {
			estimation: "", //example: Survival Estimate: 69.89%
			censoredEvent: "", //example: Time of last observation: 186.7 (months)
			failureEvent: "" //example: Time of death: 86.2 (months)
		},
	},
	style : {
	    censored_sign_size: 5,
	    axis_stroke_width: 1,
	    axisX_title_pos_x: 380,
	    axisX_title_pos_y: 600,
	    axisY_title_pos_x: -270,
	    axisY_title_pos_y: 45,
	    axis_color: "black"
	}	
};

