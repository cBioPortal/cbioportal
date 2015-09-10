

/**
 * Created by suny1 on 9/2/15.
 */

var ccPlots = (function($, _, Backbone, d3) {

    var ProfileMeta = Backbone.Model.extend({
        defaults: {
            GENETIC_ALTERATION_TYPE: "",
            NAME: "",
            DESCRIPTION: "",
            STABLE_ID: "",
            CANCER_STUDY_STABLE_ID: "",
            CASE_SET_ID: ""
        }
    });

    var ProfileMetaListTmp = Backbone.Collection.extend({
        defaults: { cancer_study_id: ""},
        model: ProfileMeta,
        urlRoot: "getGeneticProfile.json",
        url: function() {
            return this.urlRoot + "?cancer_study_id=" + this.cancer_study_id;
        },
        initialize: function(_study_id) {
            this.cancer_study_id = _study_id;
            this.models.CANCER_STUDY_STABLE_ID = _study_id;
        },
        parse: function(response) {
            return _.filter(_(response).toArray(), function(obj) {
                return (obj.STABLE_ID.indexOf("_rna_seq_v2_mrna") !== -1 &&
                        obj.STABLE_ID.toLowerCase().indexOf("zscore") === -1);
            });

        }
    });

    var ProfileMetaList = Backbone.Collection.extend({ model: ProfileMeta });

    var ProfileData = Backbone.Model.extend({
        default: {
            gene: "",
            caseId: "",
            profileId: "",
            value: "",
            mutation: ""
        }
    });

    var ProfileDataListTmp = Backbone.Collection.extend({
        defaults: { is_last_study: false },
        model: ProfileData,
        urlRoot: "getProfileData.json",
        url: function() {
            return this.urlRoot + "?cancer_study_id=" + this.cancer_study_id +
                    "&gene_list=" + this.gene_list +
                    "&genetic_profile_id=" + this.genetic_profile_id +
                    "&case_set_id=" + this.case_set_id +
                    "&case_ids_key=" + this.case_ids_key
        },
        initialize: function(_study_id, _gene_list, _profile_id, _case_set_id, _case_ids_key) {
            this.cancer_study_id = _study_id;
            this.gene_list = _gene_list;
            this.genetic_profile_id = _profile_id;
            this.case_set_id = _case_set_id;
            this.case_ids_key = _case_ids_key;
        },
        parse: function(response) {
            var _results = [];
            var genes = _.keys(response);
            _.each(genes, function(gene) {
                _.each(Object.keys(response[gene]), function(_case_id) {
                    var _profile_id = Object.keys(response[gene][_case_id])[0];
                    var _value = response[gene][_case_id][_profile_id];

                    var _result_obj = {};
                    _result_obj.gene = gene;
                    _result_obj.caseId = _case_id;
                    _result_obj.profileId = _profile_id;
                    _result_obj.value = _value;
                    _results.push(_result_obj);
                });
            });
            return _results;
        }
    });

    var ProfileDataList = Backbone.Collection.extend({ model: ProfileData });

    return {
        init: function() {

            // --------------- data -------------------
            $("#cc-plots").empty();

            var profileMetaList = new ProfileMetaList();
            var profileDataList = new ProfileDataList();

            //retrieve profiles meta
            $.each(_.pluck(window.studies.models, "attributes"), function(_study_index, _study_obj) {
                var profileMetaListTmp = new ProfileMetaListTmp(_study_obj.studyId);
                profileMetaListTmp.fetch({
                    success:
                        function(profileMetaListTmp) {
                            _.each(_.pluck(profileMetaListTmp.models, "attributes"), function(_profile_obj) {
                                _profile_obj.CANCER_STUDY_STABLE_ID = profileMetaListTmp.cancer_study_id;
                                _profile_obj.CASE_SET_ID = _study_obj.caseSetId;
                            });
                            profileMetaList.add(profileMetaListTmp.models);
                            if (_study_index + 1 === window.studies.length) { //reach the end of the iteration
                                retrieve_profile_data();
                            }
                        }
                });
            });

            //retrieve profile data
            var retrieve_profile_data = function() {
                $.each(_.pluck(profileMetaList.models, "attributes"), function(_profile_index, _profile_obj) {
                    var profileDataListTmp = new ProfileDataListTmp(_profile_obj.CANCER_STUDY_STABLE_ID, window.studies.gene_list, _profile_obj.STABLE_ID, _profile_obj.CASE_SET_ID, "-1");
                    profileDataListTmp.fetch({
                        success: function(profileDataListTmp) {
                            profileDataList.add(profileDataListTmp.models);
                            if (_profile_index + 1 === profileMetaList.length) {
                                //TODO init view
                            }
                        }
                    });
                });
            }

            // ----------------- view ---------------------


        }


    }

}(window.jQuery, window._, window.Backbone, window.d3));

$(function() {
    var cc_plots_init = false;
    if ($("#cc-plots").is(":visible")) {
        ccPlots.init();
        cc_plots_init = true;
    } else {
        $(window).trigger("resize");
    }
    $("#tabs").bind("tabsactivate", function(event, ui) {
        if (ui.newTab.text().trim().toLowerCase() === "plots") {
            if (cc_plots_init === false) {
                ccPlots.init();
                cc_plots_init = true;
                $(window).trigger("resize");
            } else {
                $(window).trigger("resize");
            }
        }
    });
});