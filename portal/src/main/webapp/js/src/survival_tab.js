var survivalCurves = (function() {

    var data = (function() {
        return {
            init: function(result) {
                console.log(result);
            }
        }
    }());

    var view = (function() {

    }());

    var util = (function() {

    }());

    return {
        init: function(cancerStudyId, caseSetId, caseIdsKey) {
            var paramsGetSurvivalData = {
                cancer_study_id: cancerStudyId,
                case_set_id: caseSetId,
                case_ids_key: caseIdsKey
            };
            $.post("getSurvivalData.json", paramsGetSurvivalData, data.init(), "json");
        }
    }
}());