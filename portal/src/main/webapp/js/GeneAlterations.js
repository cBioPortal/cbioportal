var sendData = {
    cancer_study_id: "tcga_gbm",
    genes:"EGFR MDM2",
    cases: "TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 TCGA-02-0006 TCGA-02-0007 TCGA-02-0009",
    geneticProfileIds: "gbm_mutations gbm_cna_consensus"
};

//{{{
//"TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 TCGA-02-0006 TCGA-02-0007 TCGA-02-0009\
//TCGA-02-0010 TCGA-02-0011 TCGA-02-0014 TCGA-02-0015 TCGA-02-0016 TCGA-02-0021\
//TCGA-02-0023 TCGA-02-0024 TCGA-02-0025 TCGA-02-0026 TCGA-02-0027 TCGA-02-0028\
//TCGA-02-0033 TCGA-02-0034 TCGA-02-0037 TCGA-02-0038 TCGA-02-0039 TCGA-02-0043\
//TCGA-02-0046 TCGA-02-0047 TCGA-02-0048 TCGA-02-0052 TCGA-02-0054 TCGA-02-0055\
//TCGA-02-0057 TCGA-02-0058 TCGA-02-0060 TCGA-02-0064 TCGA-02-0068 TCGA-02-0069\
//TCGA-02-0070 TCGA-02-0071 TCGA-02-0074 TCGA-02-0075 TCGA-02-0079 TCGA-02-0080\
//TCGA-02-0083 TCGA-02-0084 TCGA-02-0085 TCGA-02-0086 TCGA-02-0087 TCGA-02-0089\
//TCGA-02-0099 TCGA-02-0102 TCGA-02-0104 TCGA-02-0106 TCGA-02-0107 TCGA-02-0111\
//TCGA-02-0113 TCGA-02-0114 TCGA-02-0115 TCGA-02-0116 TCGA-02-0258 TCGA-02-0260\
//TCGA-02-0266 TCGA-02-0269 TCGA-02-0271 TCGA-02-0281 TCGA-02-0285 TCGA-02-0289\
//TCGA-02-0290 TCGA-02-0317 TCGA-02-0321 TCGA-02-0324 TCGA-02-0325 TCGA-02-0326\
//TCGA-02-0330 TCGA-02-0332 TCGA-02-0333 TCGA-02-0337 TCGA-02-0338 TCGA-02-0339\
//TCGA-02-0422 TCGA-02-0430 TCGA-02-0432 TCGA-02-0439 TCGA-02-0440 TCGA-02-0446\
//TCGA-02-0451 TCGA-02-0456 TCGA-06-0122 TCGA-06-0124 TCGA-06-0125 TCGA-06-0126\
//TCGA-06-0127 TCGA-06-0128 TCGA-06-0129 TCGA-06-0130 TCGA-06-0132 TCGA-06-0133\
//TCGA-06-0137 TCGA-06-0138 TCGA-06-0139 TCGA-06-0141 TCGA-06-0143 TCGA-06-0145\
//TCGA-06-0146 TCGA-06-0147 TCGA-06-0148 TCGA-06-0149 TCGA-06-0152 TCGA-06-0154\
//TCGA-06-0156 TCGA-06-0157 TCGA-06-0158 TCGA-06-0162 TCGA-06-0164 TCGA-06-0166\
//TCGA-06-0168 TCGA-06-0169 TCGA-06-0171 TCGA-06-0173 TCGA-06-0174 TCGA-06-0175\
//TCGA-06-0176 TCGA-06-0177 TCGA-06-0178 TCGA-06-0179 TCGA-06-0182 TCGA-06-0184\
//TCGA-06-0185 TCGA-06-0187 TCGA-06-0188 TCGA-06-0189 TCGA-06-0190 TCGA-06-0194\
//TCGA-06-0195 TCGA-06-0197 TCGA-06-0201 TCGA-06-0206 TCGA-06-0208 TCGA-06-0209\
//TCGA-06-0210 TCGA-06-0211 TCGA-06-0213 TCGA-06-0214 TCGA-06-0219 TCGA-06-0221\
//TCGA-06-0237 TCGA-06-0238 TCGA-06-0241 TCGA-06-0394 TCGA-06-0397 TCGA-06-0402\
//TCGA-06-0409 TCGA-06-0410 TCGA-06-0412 TCGA-06-0413 TCGA-06-0414 TCGA-06-0644\
//TCGA-06-0645 TCGA-06-0646 TCGA-06-0648 TCGA-08-0244 TCGA-08-0246 TCGA-08-0344\
//TCGA-08-0345 TCGA-08-0346 TCGA-08-0347 TCGA-08-0348 TCGA-08-0349 TCGA-08-0350\
//TCGA-08-0351 TCGA-08-0352 TCGA-08-0353 TCGA-08-0354 TCGA-08-0355 TCGA-08-0356\
//TCGA-08-0357 TCGA-08-0358 TCGA-08-0359 TCGA-08-0360 TCGA-08-0373 TCGA-08-0375\
//TCGA-08-0380 TCGA-08-0385 TCGA-08-0386 TCGA-08-0389 TCGA-08-0390 TCGA-08-0392\
//TCGA-08-0509 TCGA-08-0510 TCGA-08-0511 TCGA-08-0512 TCGA-08-0514 TCGA-08-0516\
//TCGA-08-0517 TCGA-08-0518 TCGA-08-0520 TCGA-08-0521 TCGA-08-0522 TCGA-08-0524\
//TCGA-08-0525 TCGA-08-0529 TCGA-08-0531 TCGA-12-0615 TCGA-12-0616 TCGA-12-0618\
//TCGA-12-0619 TCGA-12-0620"
//}}}

// {{{todo: write a wrapper to allow for overloading of parameters
//var sendData = function( ... ) {
//    return {
//        ...
//    };
//}
// }}}
$.get('GeneAlterations.json', sendData, function(returnData) {
    console.log(eval(returnData));
});

var GeneAlterations = function(sendData) {

    var json = 'GeneAlterations.json',
        alterations = {},
        listeners = [],
        that,
        REQUESTED = false;

    var fireAll = function(data) {
        // kaboom!

        for (var fun = listeners.pop(); fun = listeners.pop(); fun !== undefined) {
            fun(data);
        }
    };

    var doRequest = function() {
        $.get(json, sendData, function(returnData) {
            fireAll(returnData);

            alterations = returnData;
        });
    };

    that.addListener = function(fun) {
        alterations.unshift(fun);
    }

    that.getAlterations = function(callback) {
        // if there's no data, add callback to the list of callbacks,
        // get the data, and fire
        if ($.isEmptyObject(alterations)) {
            that.addListener(callback);

            doRequest();
        } else {
            return alterations;
        }
    };

    that.redo = function(new_SendData) {
        // set the data to the new data do another request and fire 
        // NB : you probably need to add listeners first
        sendData = new_SendData;

        doRequest();
    };

    return that;
};
