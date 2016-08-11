parser = require("./oql-parser.js");

var checkDeepEquality = function(expected, given) {
	try {
		for (var i = 0; i < expected.length; i++) {
			var exp_alt = expected[i];
			var giv_alt = given[i];

			var same_keys = true;
			var same_values = true;
			for (var key in exp_alt) {
				if (exp_alt.hasOwnProperty(key)) {
					same_keys = same_keys && giv_alt.hasOwnProperty(key);
					var giv_val = giv_alt[key];
					var exp_val = exp_alt[key];
					if (typeof giv_val !== typeof exp_val) {
					    same_values = false;
					    break;
					} else if (typeof giv_val === "object") {
					    // Recursively check if nested objects are equal
					    same_values = same_values && checkDeepEquality(giv_val, exp_val);
					} else {
					    same_values = same_values && (giv_val === exp_val);
					}
				}
			}
			for (var key in giv_alt) {
				if (giv_alt.hasOwnProperty(key)) {
					same_keys = same_keys && exp_alt.hasOwnProperty(key);
				}
			}
			if (!same_keys || !same_values) {
				return false;
			}
		}	
		return true;
	} catch (err) {
		return false;
	}
};

var testCmd = function(cmd, expected) {
	try {
		var given = parser.parse(cmd);
		for (var i = 0; i < expected.length; i++) {
			if (!(expected[i].gene === given[i].gene && checkDeepEquality(expected[i].alterations, given[i].alterations))) {
				return false;
			}
		}	
		return true;
	} catch (err) {
		return false;
	}
};

var failed_a_test = false;

var doTest = function(cmd, expected) {
	if (!testCmd(cmd, expected)) {	
		failed_a_test = true;
		console.log("Test failed for command: "+cmd);
		console.log("Got "+JSON.stringify(parser.parse(cmd)));
	}
};

doTest("TP53", [{gene:"TP53", alterations:false}]);
doTest("TP53;", [{gene:"TP53", alterations:false}]);
doTest("TP53\n", [{gene:"TP53", alterations:false}]);
doTest("TP53 BRCA1 KRAS NRAS", [{gene:"TP53", alterations:false}, {gene:"BRCA1", alterations:false}, {gene:"KRAS", alterations:false}, {gene:"NRAS", alterations:false}]);
doTest("TP53:MUT", [{gene:"TP53", alterations:[{alteration_type: "mut"}]}])
doTest("TP53:MISSENSE PROMOTER", [{gene:"TP53", alterations:[{alteration_type: "mut", constr_rel: "=", constr_type: "class", constr_val: "MISSENSE", info: {}},{alteration_type: "mut", constr_rel: "=", constr_type: "class", constr_val: "PROMOTER", info: {}}]}])
doTest("TP53:MUT;", [{gene:"TP53", alterations:[{alteration_type: "mut"}]}])
doTest("TP53:MUT\n", [{gene:"TP53", alterations:[{alteration_type: "mut"}]}])
doTest("TP53:MUT; BRCA1: gAiN hetloss EXP>=3 PROT<1", [{gene:"TP53", alterations:[{alteration_type: "mut"}]},
							{gene:"BRCA1", alterations:[{alteration_type: "cna", constr_rel: "=", constr_val: "GAIN"}, 
										    {alteration_type: "cna", constr_rel: "=", constr_val: "HETLOSS"},
										    {alteration_type: "exp", constr_rel: ">=", constr_val: 3},
										    {alteration_type: "prot", constr_rel: "<", constr_val: 1}]}])
doTest("TP53:MUT;;;\n BRCA1: AMP HOMDEL EXP>=3 PROT<1", [{gene:"TP53", alterations:[{alteration_type: "mut"}]},
							{gene:"BRCA1", alterations:[{alteration_type: "cna", constr_rel: "=", constr_val: "AMP"}, 
										    {alteration_type: "cna", constr_rel: "=", constr_val: "HOMDEL"},
										    {alteration_type: "exp", constr_rel: ">=", constr_val: 3},
										    {alteration_type: "prot", constr_rel: "<", constr_val: 1}]}])
doTest("TP53:MUT;\n BRCA1: amp GAIN EXP>=3 PROT<1", [{gene:"TP53", alterations:[{alteration_type: "mut"}]},
							{gene:"BRCA1", alterations:[{alteration_type: "cna", constr_rel: "=", constr_val: "AMP"}, 
										    {alteration_type: "cna", constr_rel: "=", constr_val: "GAIN"},
										    {alteration_type: "exp", constr_rel: ">=", constr_val: 3},
										    {alteration_type: "prot", constr_rel: "<", constr_val: 1}]}])
doTest("TP53:MUT\n BRCA1: AMP HOMDEL EXP>=3 PROT<1;", [{gene:"TP53", alterations:[{alteration_type: "mut"}]},
							{gene:"BRCA1", alterations:[{alteration_type: "cna", constr_rel: "=", constr_val: "AMP"}, 
										    {alteration_type: "cna", constr_rel: "=", constr_val: "HOMDEL"},
										    {alteration_type: "exp", constr_rel: ">=", constr_val: 3},
										    {alteration_type: "prot", constr_rel: "<", constr_val: 1}]}])

doTest("TP53:PROT<=-2\n", [{gene:"TP53", alterations:[{alteration_type: "prot", constr_rel: "<=", constr_val:-2}]}])

doTest("BRAF:MUT=V600E", [{gene:"BRAF", alterations:[{alteration_type: "mut", constr_rel: "=", constr_type:"name", constr_val:"V600E", info:{}}]}])
doTest("BRAF:MUT=V600", [{gene:"BRAF", alterations:[{alteration_type: "mut", constr_rel: "=", constr_type:"position", constr_val:600, info:{amino_acid:"V"}}]}])
doTest("BRAF:FUSION MUT=V600", [{gene:"BRAF", alterations:[{alteration_type:'fusion'}, {alteration_type: "mut", constr_rel: "=", constr_type:"position", constr_val:600, info:{}}]}])
doTest("BRAF:FUSION", [{gene:"BRAF", alterations:[{alteration_type:'fusion'}]}])
doTest("MIR-493*:MUT=V600", [{gene:"MIR-493*", alterations:[{alteration_type: "mut", constr_rel: "=", constr_type:"position", constr_val:600, info:{amino_acid:"V"}}]}])

doTest("BRAF:CNA >= gain", [{gene:"BRAF", alterations:[{alteration_type:"cna", constr_rel:">=", constr_val:"GAIN"}]}])
doTest("BRAF:CNA < homdel", [{gene:"BRAF", alterations:[{alteration_type:"cna", constr_rel:"<", constr_val:"HOMDEL"}]}])

if (!failed_a_test) {
	console.log("Passed all tests!");
}
