// Feed this in as 

// Mutation colors
var MISSENSE = '#008000';
var MISSENSE_NONREC = '#53D400';
var INFRAME = '#708090';
var INFRAME_NONREC = '#A9A9A9';
var TRUNC = '#000000';
var FUSION = '#8B00C9';

var non_mutation_rule_params = {
    // Default: gray rectangle
    '*': {
	shapes: [{
		'type': 'rectangle',
		'fill': 'rgba(211, 211, 211, 1)',
		'z': 1
	    }],
	exclude_from_legend: true,
    },
    // Copy number alteration
    'cna': {
	// Red rectangle for amplification
	'AMPLIFIED': {
	    shapes: [{
		    'type': 'rectangle',
		    'fill': 'rgba(255,0,0,1)',
		    'x': '0%',
		    'y': '0%',
		    'width': '100%',
		    'height': '100%',
		    'z': 2,
		}],
	    legend_label: 'Amplification',
	},
	// Light red rectangle for gain
	'GAINED': {
	    shapes: [{
		    'type': 'rectangle',
		    'fill': 'rgba(255,182,193,1)',
		    'x': '0%',
		    'y': '0%',
		    'width': '100%',
		    'height': '100%',
		    'z': 2,
		}],
	    legend_label: 'Gain',
	},
	// Blue rectangle for deep deletion 
	'HOMODELETED': {
	    shapes: [{
		    'type': 'rectangle',
		    'fill': 'rgba(0,0,255,1)',
		    'x': '0%',
		    'y': '0%',
		    'width': '100%',
		    'height': '100%',
		    'z': 2,
		}],
	    legend_label: 'Deep Deletion',
	},
	// Light blue rectangle for shallow deletion
	'HEMIZYGOUSLYDELETED': {
	    shapes: [{
		    'type': 'rectangle',
		    'fill': 'rgba(143, 216, 216,1)',
		    'x': '0%',
		    'y': '0%',
		    'width': '100%',
		    'height': '100%',
		    'z': 2,
		}],
	    legend_label: 'Shallow Deletion',
	}
    },
    // mRNA regulation
    'mrna': {
	// Light red outline for upregulation
	'UPREGULATED': {
	    shapes: [{
		    'type': 'rectangle',
		    'fill': 'rgba(0, 0, 0, 0)',
		    'stroke': 'rgba(255, 153, 153, 1)',
		    'stroke-width': '2',
		    'x': '0%',
		    'y': '0%',
		    'width': '100%',
		    'height': '100%',
		    'z': 3,
		}],
	    legend_label: 'mRNA Upregulation',
	},
	// Light blue outline for downregulation
	'DOWNREGULATED': {
	    shapes: [{
		    'type': 'rectangle',
		    'fill': 'rgba(0, 0, 0, 0)',
		    'stroke': 'rgba(102, 153, 204, 1)',
		    'stroke-width': '2',
		    'x': '0%',
		    'y': '0%',
		    'width': '100%',
		    'height': '100%',
		    'z': 3,
		}],
	    legend_label: 'mRNA Downregulation',
	},
    },
    // protein expression regulation
    'rppa': {
	// small up arrow for upregulated
	'UPREGULATED': {
	    shapes: [{
		    'type': 'triangle',
		    'x1': '50%',
		    'y1': '0%',
		    'x2': '100%',
		    'y2': '33.33%',
		    'x3': '0%',
		    'y3': '33.33%',
		    'fill': 'rgba(0,0,0,1)',
		    'z': 4,
		}],
	    legend_label: 'Protein Upregulation',
	},
	// small down arrow for upregulated
	'DOWNREGULATED': {
	    shapes: [{
		    'type': 'triangle',
		    'x1': '50%',
		    'y1': '100%',
		    'x2': '100%',
		    'y2': '66.66%',
		    'x3': '0%',
		    'y3': '66.66%',
		    'fill': 'rgba(0,0,0,1)',
		    'z': 4,
		}],
	    legend_label: 'Protein Downregulation',
	}
    },
};

window.geneticrules = {};
window.geneticrules.genetic_rule_set_same_color_for_all_no_recurrence = {
    'type':'gene',
    'legend_label': 'Genetic Alteration',
    'rule_params': $.extend({}, non_mutation_rule_params, {
	'mut_type': {
	    'FUSION': {
		shapes: [{
			'type': 'rectangle',
			'fill': FUSION,
			'x': '0%',
			'y': '20%',
			'width': '100%',
			'height': '60%',
			'z': 5.1
		    }],
		legend_label: 'Fusion'
	    },
	    'TRUNC,INFRAME,MISSENSE': {
		shapes: [{
			'type': 'rectangle',
			'fill': MISSENSE,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2
		}],
		legend_label: 'Mutation'
	    }
	}
    })
};
window.geneticrules.genetic_rule_set_same_color_for_all_recurrence = {
    'type':'gene',
    'legend_label': 'Genetic Alteration',
    'rule_params': $.extend({}, non_mutation_rule_params, {
	'mut_type_recurrence': {
	    // only need to show recurrence for missense and inframe
	    'FUSION_rec,FUSION': {
		shapes: [{
			'type': 'rectangle',
			'fill': FUSION,
			'x': '0%',
			'y': '20%',
			'width': '100%',
			'height': '60%',
			'z': 5.1
		    }],
		legend_label: 'Fusion'
	    },
	    'MISSENSE_rec,INFRAME_rec': {
		shapes: [{
			'type': 'rectangle',
			'fill': MISSENSE,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2
		}],
		legend_label: 'Mutation (recurrent)'
	    },
	    'MISSENSE,INFRAME,TRUNC,TRUNC_rec': { 
		shapes: [{
			'type': 'rectangle',
			'fill': MISSENSE_NONREC,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2
		}],
		legend_label: 'Mutation (non-recurrent)'
	    },
	},
    })
};
window.geneticrules.genetic_rule_set_different_colors_no_recurrence = {
    'type':'gene',
    'legend_label': 'Genetic Alteration',
    'rule_params': $.extend({}, non_mutation_rule_params, {
	'mut_type': {
	    'FUSION': {
		shapes: [{
			'type': 'rectangle',
			'fill': FUSION,
			'x': '0%',
			'y': '20%',
			'width': '100%',
			'height': '60%',
			'z': 5.1
		    }],
		legend_label: 'Fusion'
	    },
	    'TRUNC': {
		shapes: [{
			'type': 'rectangle',
			'fill': TRUNC,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2,
		    }],
		legend_label: 'Truncating Mutation',
	    },
	    'INFRAME': {
		shapes: [{
			'type': 'rectangle',
			'fill': INFRAME,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2,
		    }],
		legend_label: 'Inframe Mutation',
	    },
	    'MISSENSE': {
		shapes: [{
			'type': 'rectangle',
			'fill': MISSENSE,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2,
		    }],
		legend_label: 'Missense Mutation',
	    },
	}
    })
};
window.geneticrules.genetic_rule_set_different_colors_recurrence = {
    'type':'gene',
    'legend_label': 'Genetic Alteration',
    'rule_params': $.extend({}, non_mutation_rule_params, {
	'mut_type_recurrence': {
	    'FUSION,FUSION_rec': {
		shapes: [{
			'type': 'rectangle',
			'fill': FUSION,
			'x': '0%',
			'y': '20%',
			'width': '100%',
			'height': '60%',
			'z': 5.1
		    }],
		legend_label: 'Fusion'
	    },
	    'TRUNC,TRUNC_rec': {
		shapes: [{
			'type': 'rectangle',
			'fill': TRUNC,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2,
		    }],
		legend_label: 'Truncating Mutation',
	    },
	    'INFRAME_rec': {
		shapes: [{
			'type': 'rectangle',
			'fill': INFRAME,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2,
		    }],
		legend_label: 'Inframe Mutation (recurrent)',
	    },
	    'INFRAME': {
		shapes: [{
			'type': 'rectangle',
			'fill': INFRAME_NONREC,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2,
		    }],
		legend_label: 'Inframe Mutation (non-recurrent)',
	    },
	    'MISSENSE_rec': {
		shapes: [{
			'type': 'rectangle',
			'fill': MISSENSE,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2,
		    }],
		legend_label: 'Missense Mutation (recurrent)',
	    },
	    'MISSENSE': {
		shapes: [{
			'type': 'rectangle',
			'fill': MISSENSE_NONREC,
			'x': '0%',
			'y': '33.33%',
			'width': '100%',
			'height': '33.33%',
			'z': 5.2,
		    }],
		legend_label: 'Missense Mutation (non-recurrent)',
	    },
	}
    })
};
