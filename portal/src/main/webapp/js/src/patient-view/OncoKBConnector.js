/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
                'alteration': alterationStr.substring(0, alterationStr.length - 1),
                'geneStatus': 'complete'
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
                callback([]);
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