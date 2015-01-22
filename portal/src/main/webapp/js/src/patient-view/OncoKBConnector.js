/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var OncoKBConnector = (function(){
    var oncokbUrl = '';
    
    function init(data) {
        oncokbUrl = data.url || '';
    }
    
    function oncokbAccess(callback) {
        if(oncokbUrl && oncokbUrl !== 'null'){
            $.get(oncokbUrl+'access', function(){callback(true);})
                .fail(function(){callback(false);});
        }else {
            callback(false);
        }
    }
    
    function getEvidence(mutations, callback) {
        var mutationEventIds = mutations.getEventIds(false),
            searchPairs = [],
            geneStr = "",
            alterationStr="";
    
        for(var i=0, mutationL = mutationEventIds.length; i < mutationL; i++) {
            var datum = {},
                gene = mutations.getValue(mutationEventIds[i], 'gene'),
                alteration = mutations.getValue(mutationEventIds[i], 'aa');
            datum.gene = gene;
            datum.alteration = alteration;
            searchPairs.push(datum);
            
            geneStr+=gene+",";
            alterationStr+=alteration+",";
        }
        $.ajax({
            type: 'POST',
            url: oncokbUrl + 'evidence.json',
            data: {
                'hugoSymbol' : geneStr.substring(0, geneStr.length - 1),
                'alteration': alterationStr.substring(0, alterationStr.length - 1)
            },
            crossDomain: true,
            dataType: 'json',
            success: function(evidenceList) {
                var evidenceCollection = [],
                    evidenceL = evidenceList.length;
                searchPairs.forEach(function(searchPair, i) {
                    var datum = {
                        'gene': {},
                        'alteration': [],
                        'oncogenic': false
                    };
                    
                    for(var i=0; i<evidenceL; i++) {
                        var evidence = evidenceList[i];
                        if(evidence.gene.hugoSymbol === searchPair.gene) {
                            if(evidence.evidenceType === 'GENE_SUMMARY') {
                                datum.gene.summary = findRegex(evidence.description);
                            }else if(evidence.evidenceType === 'GENE_BACKGROUND') {
                                datum.gene.background = findRegex(evidence.description);
                            }else if(evidence.evidenceType === 'MUTATION_EFFECT'){
                                for(var j=0, alterationL = evidence.alterations.length; j<alterationL; j++) {
                                    var alteration = evidence.alterations[j];
                                    if(alteration.name === searchPair.alteration) {
                                        if(alteration.oncogenic) {
                                            datum.oncogenic = true;
                                        }
                                        datum.alteration.push({
                                            knownEffect: evidence.knownEffect,
                                            description: findRegex(evidence.description)
                                        });
                                    }
                                }
                            }
                        }
                    }
                    evidenceCollection.push(datum);
                });
                callback(evidenceCollection);
            },
            error: function (responseData, textStatus, errorThrown) {
                console.log('POST failed.');
            }
        });
    }
    
    function findRegex(str) {

        if(typeof str === 'string' && str !== '') {
            var regex = [/PMID:\s*([0-9]+,*\s*)+/ig, /NCT[0-9]+/ig],
                links = ['http://www.ncbi.nlm.nih.gov/pubmed/',
                         'http://clinicaltrials.gov/show/'];
            for (var j = 0, regexL = regex.length; j < regexL; j++) {
                var result = str.match(regex[j]);

                if(result) {
                    var uniqueResult = result.filter(function(elem, pos) {
                        return result.indexOf(elem) === pos;
                    });
                    for(var i = 0, resultL = uniqueResult.length; i < resultL; i++) {
                        var _datum = uniqueResult[i];

                        switch(j) {
                            case 0:
                                var _number = _datum.split(':')[1].trim();
                                _number = _number.replace(/\s+/g, '');
                                str = str.replace(new RegExp(_datum, 'g'), '<a class="withUnderScore" target="_blank" href="'+ links[j] + _number+'">' + _datum + '</a>');
                                break;
                            default:
                                str = str.replace(_datum, '<a class="withUnderScore" target="_blank" href="'+ links[j] + _datum+'">' + _datum + '</a>');
                                break;
                        }

                    }
                }
            }
        }
        return str;
    }
    return {
        init: init,
        oncokbAccess: oncokbAccess,
        getEvidence: getEvidence
    };
})();