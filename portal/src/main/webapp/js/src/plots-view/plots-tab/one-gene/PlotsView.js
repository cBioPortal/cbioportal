var PlotsView = (function () {

    var text = { //Text for the general view
            mutations_alias : {
                frameshift : "frameshift",
                in_frame : "in_frame",
                missense : "missense",
                nonsense : "nonsense",
                splice : "splice",
                nonstop : "nonstop",
                nonstart : "nonstart",
                non : "non"
            },
            gistic_txt_val : {
                "-2": "Homdel",
                "-1": "Hetloss",
                "0": "Diploid",
                "1": "Gain",
                "2": "Amp"
            }
        },   
        userSelection = {
            gene: "",
            plots_type: "",
            copy_no_type : "",
            mrna_type : "",
            dna_methylation_type : "",
            rppa_type : "",
            clinical_attribute: ""
        };   //current user selection from the side menu

    var discretizedDataTypeIndicator = "";
    
    var profileDataResult = "";
    function getMutationTypeCallBack(mutationTypeResult) {
        PlotsData.init(profileDataResult, mutationTypeResult);
    }
    
    function getProfileDataCallBack(_profileDataResult) {
        profileDataResult = _profileDataResult;
        var resultObj = _profileDataResult[userSelection.gene];
        var _hasMutationProfile = true;
        for (var key in resultObj) {  //key is case id
            var _obj = resultObj[key];
            if (!_obj.hasOwnProperty(cancer_study_id + "_mutations")) {
                _hasMutationProfile = false;
            } else {
                _hasMutationProfile = true;
            }
        }

        if (_hasMutationProfile) {
            Plots.getMutationType(
                userSelection.gene,
                cancer_study_id + "_mutations",
                patient_set_id,
                patient_ids_key,
                getMutationTypeCallBack
            );
        } else {
            PlotsData.init(profileDataResult, "");
        }
    }
    
    function getProfileData() {
        var sel = document.getElementById("data_type_copy_no");
        var vals = [];
        for (var i = 0; i < sel.children.length; ++i) {
            var child = sel.children[i];
            if (child.tagName === 'OPTION') vals.push(child.value.split("|")[0]);
        }
        if (vals.indexOf(cancer_study_id + "_gistic") !== -1) {
            discretizedDataTypeIndicator = cancer_study_id + "_gistic";
        } else if (vals.indexOf(cancer_study_id + "_cna") !== -1) {
            discretizedDataTypeIndicator = cancer_study_id + "_cna";
        } else if (vals.indexOf(cancer_study_id + "_CNA") !== -1) {
            discretizedDataTypeIndicator = cancer_study_id + "_CNA";
        } else if (vals.indexOf(cancer_study_id + "_cna_rae") !== -1) {
            discretizedDataTypeIndicator = cancer_study_id + "_cna_rae";
        }
        var _profileIdsStr = cancer_study_id + "_mutations" + " " +
            discretizedDataTypeIndicator + " " +
            userSelection.copy_no_type + " " +
            userSelection.mrna_type + " " +
            userSelection.rppa_type + " " +
            userSelection.dna_methylation_type;
        Plots.getProfileData(
            userSelection.gene,
            _profileIdsStr,
            patient_set_id,
            patient_ids_key,
            getProfileDataCallBack
        );
    }
    
    function generatePlots() {
        getProfileData();
    }

    function getUserSelection() {
        userSelection.gene = document.getElementById("gene").value;
        userSelection.plots_type = document.getElementById("plots_type").value;
        userSelection.copy_no_type = document.getElementById("data_type_copy_no").value.split("|")[0];
        userSelection.mrna_type = document.getElementById("data_type_mrna").value.split("|")[0];
        userSelection.rppa_type = document.getElementById("data_type_rppa").value.split("|")[0];
        userSelection.dna_methylation_type = document.getElementById("data_type_dna_methylation").value.split("|")[0];
        userSelection.clinical_attribute = document.getElementById("data_type_clinical").value.split("|")[0];
    }

    return {
        init: function(){
            $('#view_title').empty();
            $('#plots_box').empty();
            $('#loading-image').show();
            $('#view_title').hide();
            $('#plots_box').hide();

            var _status = PlotsMenu.getStatus();
            if (_status.has_mrna && (_status.has_copy_no || _status.has_dna_methylation || _status.has_rppa || _status.has_clinical_data)) {
                getUserSelection();
                generatePlots();
            } else {
                $('#loading-image').hide();
            }
        },
        applyLogScaleX: function() {
            var applyLogScale = document.getElementById("log_scale_option_x").checked;
            View.applyLogScaleX(applyLogScale);
        },
        applyLogScaleY: function() {
            var applyLogScale = document.getElementById("log_scale_option_y").checked;
            View.applyLogScaleY(applyLogScale);
        },
        getUserSelection: function() {
            return userSelection;
        },
        getDiscretizedDataTypeIndicator: function() {
            return discretizedDataTypeIndicator;
        },
        getText: function() {
            return text;
        }
    };

}());//Closing PlotsView