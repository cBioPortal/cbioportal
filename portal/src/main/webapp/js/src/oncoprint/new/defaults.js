window.oncoprint_defaults = (function() {
	var utils = window.oncoprint_utils;
	var makeGeneticAlterationComparator = function(distinguish_mutations) {
		var cna_key = 'cna';
		var cna_order = utils.invert_array(['AMPLIFIED', 'HOMODELETED', 'GAINED', 'HEMIZYGOUSLYDELETED', 'DIPLOID', undefined]);
		var mut_type_key = 'mut_type';
		var mut_order = (function() {
			if (!distinguish_mutations) {
				return function(m) {
					return +(typeof m === 'undefined');
				}
			} else {
				var _order = utils.invert_array(['TRUNC', 'INFRAME', 'MISSENSE', undefined]); 
				return function(m) {
					return _order[m];
				}
			}
		})();
		var mrna_key = 'mrna';
		var rppa_key = 'rppa';
		var regulation_order = utils.invert_array(['UPREGULATED', 'DOWNREGULATED', undefined]);

		return function(d1, d2) {
			var cna_diff = utils.sign(cna_order[d1[cna_key]] - cna_order[d2[cna_key]]);
			if (cna_diff !== 0) {
				return cna_diff;
			}

			var mut_type_diff = utils.sign(mut_order(d1[mut_type_key]) - mut_order(d2[mut_type_key]));
			if (mut_type_diff !== 0) {
				return mut_type_diff;
			}

			var mrna_diff = utils.sign(regulation_order[d1[mrna_key]] - regulation_order[d2[mrna_key]]);
			if (mrna_diff !== 0) {
				return mrna_diff;
			}

			var rppa_diff = utils.sign(regulation_order[d1[rppa_key]] - regulation_order[d2[rppa_key]]);
			if (rppa_diff !== 0) {
				return rppa_diff;
			}

			return 0;
		};
	};

	var genetic_alteration_config_base = {
		default: [{shape: 'full-rect', color: '#D3D3D3', z_index: -1}],
		altered: {
			'cna': {
				'AMPLIFIED': {
					shape: 'full-rect',
					color: 'red',
					legend_label: 'Amplification'
				},
				'GAINED': {
					shape: 'full-rect',
					color: '#FFB6C1',
					legend_label: 'Gain'
				},
				'HOMODELETED':{
					shape: 'full-rect',
					color: '#0000FF',
					legend_label: 'Deep Deletion'
				},
				'HEMIZYGOUSLYDELETED': {
					shape: 'full-rect',
					color: '#8FD8D8',
					legend_label: 'Shallow Deletion'
				}
			},
			'mrna': {
				'UPREGULATED': {
					shape: 'outline',
					color: '#FF9999',
					legend_label: 'mRNA Upregulation'
				},
				'DOWNREGULATED': {
					shape: 'outline',
					color: '#6699CC',
					legend_label: 'mRNA Downregulation'
				}
			},
			'rppa': {
				'UPREGULATED': {
					shape: 'small-up-arrow',
					color: 'black',
					legend_label: 'Protein Upregulation'
				},
				'DOWNREGULATED': {
					shape: 'small-down-arrow',
					color: 'black',
					legend_label: 'Protein Downregulation'
				}
			}
		},
		legend_label: "Genetic Alteration",
	};
	var genetic_alteration_config_nondistinct_mutations = $.extend(true,{},genetic_alteration_config_base);
	genetic_alteration_config_nondistinct_mutations.altered.mut_type = {
		'*': {
			shape: 'middle-rect',
			color: 'green',
			legend_label: 'Mutation'
		}
	};
	var genetic_alteration_config = $.extend(true,{},genetic_alteration_config_base);
	genetic_alteration_config.altered.mut_type = {
		'MISSENSE': {
			shape: 'middle-rect',
			color: 'green',
			legend_label: 'Missense Mutation'
		},
		'INFRAME': {
			shape: 'middle-rect',
			color: '#9F8170',
			legend_label: 'Inframe Mutation'
		},
		'TRUNC': {
			shape: 'middle-rect',
			color: 'black',
			legend_label: 'Truncating Mutation'
		},
		'FUSION':{
			shape: 'large-right-arrow',
			color: 'black',
			legend_label: 'Fusion'
		}
	};
	
	return {
		genetic_alteration_config: genetic_alteration_config,
		genetic_alteration_config_nondistinct_mutations: genetic_alteration_config_nondistinct_mutations,
		genetic_alteration_comparator: makeGeneticAlterationComparator(true),
		genetic_alteration_comparator_nondistinct_mutations: makeGeneticAlterationComparator(false)
	};
})();
