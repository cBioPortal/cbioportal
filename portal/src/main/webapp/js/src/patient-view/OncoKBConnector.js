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


var OncoKBConnector = (function () {
    var oncokbUrl = '';

    function init(data) {
        oncokbUrl = data.url || '';
    }

    function oncokbAccess(callback) {
        if (oncokbUrl && oncokbUrl !== 'null') {
            $.get('api/proxy/oncokbAccess', function () {
                callback(true);
            })
                .fail(function () {
                    callback(false);
                });
        } else {
            callback(false);
        }
    }

    function getEvidence(variants, callback) {
        var mutationEventIds = variants.mutations.getEventIds(false),
            searchPairs = [],
            geneStr = "",
            alterationStr = "",
            consequenceStr = ""
        var oncokbServiceData = {};
        var oncokbSummaryData = {};

        for (var i = 0, mutationL = mutationEventIds.length; i < mutationL; i++) {
            var datum = {},
                gene = variants.mutations.getValue(mutationEventIds[i], 'gene'),
                alteration = variants.mutations.getValue(mutationEventIds[i], 'aa'),
                consequence = consequenceConverter(variants.mutations.getValue(mutationEventIds[i], 'type'));
            datum.gene = gene;
            datum.alteration = alteration;
            datum.consequence = consequence;
            searchPairs.push(datum);

            geneStr += gene + ",";
            alterationStr += alteration + ",";
            consequenceStr += consequence + ",";
        }

        oncokbServiceData = {
            'hugoSymbol': geneStr.substring(0, geneStr.length - 1),
            'alteration': alterationStr.substring(0, alterationStr.length - 1),
            'consequence': consequenceStr.substring(0, consequenceStr.length - 1),
            'evidenceType': 'GENE_SUMMARY,GENE_BACKGROUND,MUTATION_EFFECT,STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY'
        };

        //Read Global environment parameter
        if (OncoKB.geneStatus && OncoKB.geneStatus != 'null') {
            oncokbServiceData.geneStatus = OncoKB.geneStatus;
        }

        if (variants.tumorType) {
            oncokbServiceData.tumorType = variants.tumorType;
            oncokbServiceData.source = 'cbioportal';
        }

        oncokbSummaryData = $.extend({}, oncokbServiceData);
        oncokbSummaryData.type = 'variant';

        $.when(
            $.ajax({
                type: 'POST',
                url: 'api/proxy/oncokb',
                data: oncokbServiceData,
                crossDomain: true,
                dataType: 'json'
            }),
            $.ajax({
                type: 'POST',
                url: 'api/proxy/oncokbSummary',
                data: oncokbSummaryData,
                crossDomain: true,
                dataType: 'json'
            })
        ).then(function (d1, d2) {
                var evidenceCollection = [];
                if (d1[0].length ===  d2[0].length && d2[0].length === searchPairs.length) {
                    searchPairs.forEach(function (searchPair, pairIndex) {
                        var datum = {
                            'variantSummary': '',
                            'gene': {},
                            'alteration': [],
                            'prevalence': [],
                            'progImp': [],
                            'treatments': [],
                            'trials': [],
                            'oncogenic': 0,
                            'hotspot': 0
                        };
                        var evidenceL = d1[0][pairIndex].length;

                        for (var i = 0; i < evidenceL; i++) {
                            var evidence = d1[0][pairIndex][i];
                            if (evidence.gene.hugoSymbol === searchPair.gene) {
                                if (evidence.evidenceType === 'GENE_SUMMARY') {
                                    datum.gene.summary = findRegex(evidence.description);
                                } else if (evidence.evidenceType === 'GENE_BACKGROUND') {
                                    datum.gene.background = findRegex(evidence.description);
                                } else if (evidence.evidenceType === 'MUTATION_EFFECT') {
                                    var description = '';
                                    for (var j = 0, alterationL = evidence.alterations.length; j < alterationL; j++) {
                                        var alteration = evidence.alterations[j];
                                        //if(alteration.name === searchPair.alteration) {
                                        if (alteration.hasOwnProperty('oncogenic')) {
                                            if (datum.hasOwnProperty('oncogenic') && [1, 2].indexOf(datum.oncogenic) === -1) {
                                                datum.oncogenic = Number(alteration.oncogenic);
                                            }
                                        }
                                        if (alteration.hasOwnProperty('hotspot')) {
                                            if (datum.hasOwnProperty('hotspot') && datum.hotspot === 0) {
                                                datum.hotspot = Number(alteration.hotspot);
                                            }
                                        }
                                    }

                                    if(evidence.shortDescription) {
                                        description = evidence.shortDescription;
                                    }else{
                                        description = evidence.description;
                                    }
                                    datum.alteration.push({
                                        knownEffect: evidence.knownEffect,
                                        description: findRegex(description)
                                    });
                                } else if (evidence.evidenceType === 'PREVALENCE') {
                                    datum.prevalence.push({
                                        tumorType: evidence.tumorType.name,
                                        description: findRegex(evidence.description) || 'No yet curated'
                                    });
                                } else if (evidence.evidenceType === 'PROGNOSTIC_IMPLICATION') {
                                    datum.progImp.push({
                                        tumorType: evidence.tumorType.name,
                                        description: findRegex(evidence.description) || 'No yet curated'
                                    });
                                } else if (evidence.evidenceType === 'CLINICAL_TRIAL') {
                                    datum.trials.push({
                                        tumorType: evidence.tumorType.name,
                                        list: evidence.clinicalTrials
                                    });
                                } else if (evidence.levelOfEvidence) {
                                    //if evidence has level information, that means this is treatment evidence.
                                    if (['LEVEL_0', 'LEVEL_R3'].indexOf(evidence.levelOfEvidence) === -1) {
                                        var _treatment = {};
                                        _treatment.tumorType = evidence.tumorType.name;
                                        _treatment.level = evidence.levelOfEvidence;
                                        _treatment.content = evidence.treatments;
                                        _treatment.description = findRegex(evidence.description) || 'No yet curated';
                                        datum.treatments.push(_treatment);
                                    }
                                }
                            }

                            if(d2[0][pairIndex]){
                                datum.variantSummary = d2[0][pairIndex];
                            }
                        }
                        evidenceCollection.push(datum);
                    });

                }
                callback(evidenceCollection);
            }, function () {
                console.log('POST failed.');
                callback([]);
            });
    }

    function findRegex(str) {

        if (typeof str === 'string' && str) {
            var regex = [/PMID:\s*([0-9]+,*\s*)+/ig, /NCT[0-9]+/ig],
                links = ['http://www.ncbi.nlm.nih.gov/pubmed/',
                    'http://clinicaltrials.gov/show/'];
            for (var j = 0, regexL = regex.length; j < regexL; j++) {
                var result = str.match(regex[j]);

                if (result) {
                    var uniqueResult = result.filter(function (elem, pos) {
                        return result.indexOf(elem) === pos;
                    });
                    for (var i = 0, resultL = uniqueResult.length; i < resultL; i++) {
                        var _datum = uniqueResult[i];

                        switch (j) {
                            case 0:
                                var _number = _datum.split(':')[1].trim();
                                _number = _number.replace(/\s+/g, '');
                                str = str.replace(new RegExp(_datum, 'g'), '<a class="withUnderScore" target="_blank" href="' + links[j] + _number + '">' + _datum + '</a>');
                                break;
                            default:
                                str = str.replace(_datum, '<a class="withUnderScore" target="_blank" href="' + links[j] + _datum + '">' + _datum + '</a>');
                                break;
                        }

                    }
                }
            }
        } else {
            str = ''
        }
        return str;
    }

    /**
     * Convert cBioPortal consequence to OncoKB consequence
     *
     * @param consequence cBioPortal consequence
     * @returns
     */
    function consequenceConverter(consequence) {
        var matrix = {
            '3\'Flank': ['any'],
            '5\'Flank ': ['any'],
            'COMPLEX_INDEL': ['inframe_deletion', 'inframe_insertion'],
            'ESSENTIAL_SPLICE_SITE': ['feature_truncation'],
            'Exon skipping': ['inframe_deletion'],
            'Frameshift deletion': ['frameshift_variant'],
            'Frameshift insertion': ['frameshift_variant'],
            'FRAMESHIFT_CODING': ['frameshift_variant'],
            'Frame_Shift_Del': ['frameshift_variant'],
            'Frame_Shift_Ins': ['frameshift_variant'],
            'Fusion': ['fusion'],
            'Indel': ['frameshift_variant', 'inframe_deletion', 'inframe_insertion'],
            'In_Frame_Del': ['inframe_deletion', 'feature_truncation'],
            'In_Frame_Ins': ['inframe_insertion'],
            'Missense': ['missense_variant'],
            'Missense_Mutation': ['missense_variant'],
            'Nonsense_Mutation': ['stop_gained'],
            'Nonstop_Mutation': ['stop_lost'],
            'Splice_Site': ['splice_region_variant'],
            'Splice_Site_Del': ['splice_region_variant'],
            'Splice_Site_SNP': ['splice_region_variant'],
            'splicing': ['splice_region_variant'],
            'Translation_Start_Site': ['start_lost'],
            'vIII deletion': ['any']
        };
        if (matrix.hasOwnProperty(consequence)) {
            return matrix[consequence].join('+');
        } else {
            return 'any';
        }
    }

    return {
        init: init,
        oncokbAccess: oncokbAccess,
        getEvidence: getEvidence,
        findRegex: findRegex,
        consequenceConverter: consequenceConverter
    };
})();

$.fn.dataTableExt.oSort['oncokb-level-asc'] = function (x, y) {
    var levels = ['4', '3', '2B', '2A', '1', '0', 'R3', 'R2', 'R1'];
    var xIndex = levels.indexOf(x);
    var yIndex = levels.indexOf(y);
    if (xIndex < yIndex) {
        return 1;
    } else {
        return -1;
    }
};

$.fn.dataTableExt.oSort['oncokb-level-desc'] = function (x, y) {
    var levels = ['4', '3', '2B', '2A', '1', '0', 'R3', 'R2', 'R1'];
    var xIndex = levels.indexOf(x);
    var yIndex = levels.indexOf(y);
    if (xIndex < yIndex) {
        return -1;
    } else {
        return 1;
    }
};