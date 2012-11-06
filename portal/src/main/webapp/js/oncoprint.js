var OncoPrint = function(params) {
    var that = {};

    that.insert = function(div) {
        var oncoPrintDiv = $('<div/>', {
            class: "oncoprint", id: params.cancer_study_id
        }).appendTo(div);

        var getSVG = $('<p/>', {
            text: "Get OncoPrint"
        }).append($("<input type='submit' value='SVG'>"));

        var customizeOncoPrint =  $('<p/>', {
            text: "Customize OncoPrint:"
        });

        var caseSet =  $('<p/>', {
            text: params.case_set_str
        });

        var alteredIn = $('<p/>', {
            text: "Altered in "
                + params.num_cases_affected
                + " (" + params.percent_cases_affected + ")"
                + " of cases"
        });

        oncoPrintDiv.append(getSVG);
        oncoPrintDiv.append(customizeOncoPrint);
        oncoPrintDiv.append(caseSet);
        oncoPrintDiv.append(alteredIn);

        // draw the actual oncoprint already!
        var oncoprint = OncoPrintInit([], oncoPrintDiv, []);
        DrawOncoPrintBody(oncoprint, geneAlterations);
    };

    return that;
};


OncoPrint.help = function() {
//  div is the HTML div element to append an oncoprint to
//
//  params is an object literal like this:
// { cancer_study_id,
// case_set_str,
// num_cases_affected,
// percent_cases_affected,
// geneAlterations_l }
}

