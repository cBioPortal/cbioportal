/**
 * Created by suny1 on 11/18/15.
 */
var PlotlyCCplots = (function (Plotly, _, $) {

    return {
        init: function() {

            var profile_arr = [
                "acc_tcga_rna_seq_v2_mrna",
                "blca_tcga_rna_seq_v2_mrna",
                "brca_tcga_rna_seq_v2_mrna",
                "cesc_tcga_rna_seq_v2_mrna",
                "chol_tcga_rna_seq_v2_mrna",
                "coadread_tcga_rna_seq_v2_mrna",
                "laml_tcga_rna_seq_v2_mrna",
                "lgg_tcga_rna_seq_v2_mrna",
                "gbm_tcga_rna_seq_v2_mrna",
                "hnsc_tcga_rna_seq_v2_mrna",
                "kich_tcga_rna_seq_v2_mrna",
                "kirc_tcga_rna_seq_v2_mrna",
                "kirp_tcga_rna_seq_v2_mrna",
                "lihc_tcga_rna_seq_v2_mrna",
                "luad_tcga_rna_seq_v2_mrna",
                "lusc_tcga_rna_seq_v2_mrna",
                "dlbc_tcga_rna_seq_v2_mrna",
                "meso_tcga_rna_seq_v2_mrna",
                "ov_tcga_rna_seq_v2_mrna",
                "paad_tcga_rna_seq_v2_mrna",
                "pcpg_tcga_rna_seq_v2_mrna",
                "prad_tcga_rna_seq_v2_mrna",
                "luad_tcga_rna_seq_v2_mrna",
                "skcm_tcga_rna_seq_v2_mrna",
                "sarc_tcga_rna_seq_v2_mrna",
                "tgct_tcga_rna_seq_v2_mrna",
                "thym_tcga_rna_seq_v2_mrna",
                "thca_tcga_rna_seq_v2_mrna",
                "ucec_tcga_rna_seq_v2_mrna",
                "ucs_tcga_rna_seq_v2_mrna",
                "uvm_tcga_rna_seq_v2_mrna",
                "brca_tcga_pub2015_rna_seq_v2_mrna"
            ];

            var params = {
                genes: ["SOX9"],
                genetic_profile_ids: profile_arr
            };

            window.cbioportal_client.getGeneticProfileData(params).then(
                function(_result) {

                    var data = [];

                    var non_mut_dots = {
                        x: _.map(_.pluck(_result, "genetic_profile_id"), function(_profile_id){ return profile_arr.indexOf(_profile_id) + Math.random() * 0.3 - 0.15; }),
                        y: _.pluck(_result, "profile_data"),
                        mode: 'markers',
                        type: 'scatter',
                        name: 'Non Mut',
                        marker: {
                            size: 6,
                            symbol: 'circle',
                            color: "#00AAF8",
                            opacity: 0.7,
                            line: {
                                color: "#0089C6",
                                width: 0.1
                            }
                        },
                        hoverinfo: "x+y"
                    };
                    data.push(non_mut_dots);

                    _.each(profile_arr, function(_profile_id) {
                        var _box = {
                            y: _.pluck(_.filter(_result, function(_result_obj) { return _result_obj.genetic_profile_id == _profile_id; }), "profile_data"),
                            x0: profile_arr.indexOf(_profile_id),
                            type: 'box',
                            opacity: 0.6,
                            boxpoints: false,
                            showlegend: false
                        }
                        data.push(_box);
                    });

                    var layout = {
                        yaxis: {
                            range: [ Math.min(_.pluck(_result, "profile_data")), Math.max(_.pluck(_result, "profile_data")) ],
                            title: 'SOX9 Expression -- RNA Seq V2'
                        }
                    };

                    Plotly.newPlot('plotly_cc_plots_box', data, layout);

                });
        }
    }

}(window.Plotly, window._, window.jQuery));

