var OncoPrint = function(div, params) {
    var appendOncoPrint = function( params ) {
        var oncoPrintDiv = $('<div/>', {
            class: "oncoprint", id: params.cancer_study_id
        }).appendTo(ONCOPRINTS_DIV);

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
                + " out of "
                + params.total_num_cases
                + " cases ("
                + params.percent_cases_affected + ")"
        });

        oncoPrintDiv.append(getSVG);
        oncoPrintDiv.append(customizeOncoPrint);
        oncoPrintDiv.append(caseSet);
        oncoPrintDiv.append(alteredIn);

        // draw the actual oncoprint already!
        var oncoprint = OncoPrintInit([], oncoPrintDiv, []);
        DrawOncoPrintBody(oncoprint, geneAlterations);
    };
};


OncoPrint.help = function() {
//  div is the HTML div element to append an oncoprint to
//
//  params is an object literal like this:
// { cancer_study_id,
// case_set_str,
// num_cases_affected,
// total_num_cases,
// percent_cases_affected,
// geneAlterations_l }
}

