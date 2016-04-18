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

/**
 * Created by zhangh2 on 12/2/15.
 */


var OncoKB = (function(_, $) {
    var self = {};
    self.url = '';
    self.accessible = false;
    self.instances = [];
    self.customObject = {};
    self.levels = {
        sensitivity: ['4', '3B', '3A', '2B', '2A', '1', '0'],
        resistance: ['R3', 'R2', 'R1'],
        all: ['4', 'R3', '3B', '3A', 'R2', '2B', '2A', 'R1', '1', '0']
    };
    self.levelColors = {
        '1': '#008D14',
        '2A': '#019192',
        '2B': '#2A5E8E',
        '3A': '#794C87',
        '3B': '#9B7EB6',
        '4': '#000000',
        'R1': '#FF0000'
    };
    self.levelsInfo = {
        '1': '<b>FDA-approved</b> biomarker and drug <b>in this indication</b>.',
        '2A': '<b>Standard-of-care</b> biomarker and drug <b>in this indication</b> but not FDA-approved.',
        '2B': '<b>FDA-approved</b> biomarker and drug <b>in another indication</b> but not FDA or Standard-of-care for this indication.',
        '3A': '<b>Clinical evidence</b> links biomarker to drug response <b>in this indication</b> but neither biomarker or drug are FDA-approved or Standard-of-care.',
        '3B': '<b>Clinical evidence</b> links biomarker to drug response <b>in another indication</b> but neither biomarker or drug are FDA-approved or Standard-of-care.',
        '4': '<b>Preclinical evidence</b> associates this biomarker to drug response but neither biomarker or drug are FDA-approved or Standard-of-care.',
        'R1': 'NCCN-compendium listed biomarker for resistance to a FDA-approved drug.'
    };
    self.instanceManagers = {};

    self.oncogenic = ['Unknown', 'Likely Neutral', 'Likely Oncogenic', 'Oncogenic'];

    function InstanceManager(id) {
        this.id = id || 'OncoKB-InstanceManager-' + new Date().getTime(); //Manager ID, maybe used to distinguish between different managers
        this.instances = {}; //key is the instance id
    }

    InstanceManager.prototype = {
        addInstance: function(instanceId) {
            var instance = new Instance(instanceId);
            this.instances[instance.getId()] = instance;
            return instance;
        },
        removeInstance: function(instanceId) {
            if (this.instances.hasOwnProperty(instanceId)) {
                delete this.instances[instanceId];
                return true;
            } else {
                return false;
            }
        },
        getInstance: function(instanceId) {
            return this.instances[instanceId];
        },
        getId: function() {
            return this.id;
        }
    };

    function VariantPair() {
        this.id = '';
        this.gene = '';
        this.entrezGeneId = '';
        this.alteration = '';
        this.geneStatus = '';
        this.tumorType = '';//tumor type
        this.consequence = '';
        this.proteinStart = '';
        this.proteinEnd = '';
        this.evidenceType = '';
        this.source = 'cbioportal';
        this.evidence = new OncoKB.Evidence();
        this.cosmicCount = '';
        this.isHotspot = false;
        this.civicVariantSummaries = [];
        this.civicClinicalEvidenceStats = [];
        this.isVUS = false;
        this.highestSensitiveLevel = '';
        this.highestResistanceLevel = '';
        this.hasGene = true;
        this.hasVariant = false;
        this.updatedEvidence = false; //Parameter indicates whether this variant has received evidence from OncoKB service.
    }

    VariantPair.prototype.getVariantId = function() {
        return this.gene + this.alteration + this.tumorType 
            + this.consequence + this.proteinStart + this.proteinEnd;
    };

    function Evidence() {
        this.id = '';
        this.gene = {};
        this.alteration = [];
        this.prevalence = [];
        this.progImp = [];
        this.treatments = {
            sensitivity: [],
            resistance: []
        }; //separated by level type
        this.trials = [];
        this.oncogenic = '';
        this.mutationEffect = {};
        this.summary = '';
        this.drugs = {
            sensitivity: {
                current: [],
                inOtherTumor: []
            },
            resistance: []
        }
    }

    function Instance(id) {
        this.initialized = false;
        this.dataReady = false;
        this.tumorType = ''; //Global tumor types
        this.source = 'cbioportal';
        this.geneStatus = 'Complete';
        this.evidenceTypes = 'GENE_SUMMARY,GENE_BACKGROUND,ONCOGENIC,MUTATION_EFFECT,VUS,STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY,STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE,INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY';
        this.evidenceLevels = ['LEVEL_1', 'LEVEL_2A', 'LEVEL_2B', 'LEVEL_3A', 'LEVEL_3B', 'LEVEL_R1'];
        this.variantCounter = 0;
        this.variants = {};
        this.evidence = {};
        this.id = id || 'OncoKB-Instance-' + new Date().getTime();
        this.variantUniqueIds = {}; // Unique variant list.
    }

    function EvidenceRequestItem(variant) {
        this.ids = [];
        this.hugoSymbol = variant.gene || '';
        this.alteration = variant.alteration || '';
        if(variant.tumorType) {
            this.tumorType = variant.tumorType || '';//tumor type
        }
        if(variant.consequence) {
            this.consequence = variant.consequence || '';//tumor type
        }
        if(variant.proteinStart) {
            this.proteinStart = variant.proteinStart || '';//tumor type
        }
        if(variant.proteinEnd) {
            this.proteinEnd = variant.proteinEnd || '';//tumor type
        }
    }

    function addInstanceManager(instanceManagerId) {
        var manager = new InstanceManager(instanceManagerId);
        self.instanceManagers[manager.getId()] = manager;
        return manager;
    }

    self.utils = (function() {
        /**
         * Insert PUBMED and Clinical Trial link into input string
         * @param str
         * @returns {*}
         */
        function findRegex(str) {

            if (typeof str === 'string' && str) {
                var regex = [/PMID:\s*([0-9]+,*\s*)+/ig, /NCT[0-9]+/ig],
                    links = ['http://www.ncbi.nlm.nih.gov/pubmed/',
                        'http://clinicaltrials.gov/show/'];
                for (var j = 0, regexL = regex.length; j < regexL; j++) {
                    var result = str.match(regex[j]);

                    if (result) {
                        var uniqueResult = result.filter(function(elem, pos) {
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
                str = '';
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
                'Targeted_Region': ['inframe_deletion', 'inframe_insertion'],
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

        function getLevel(level) {
            if (level) {
                var _level = level.match(/LEVEL_(R?\d[AB]?)/);
                if (_level instanceof Array && _level.length >= 2) {
                    return _level[1];
                } else {
                    return level;
                }
            } else {
                return "";
            }
        }

        function getNumberLevel(level) {
            if (level) {
                var _level = level.match(/LEVEL_R?(\d)[AB]?/);
                if (_level instanceof Array && _level.length >= 2) {
                    return _level[1];
                } else {
                    return level;
                }
            } else {
                return "";
            }
        }

        /**
         *
         * @param treatments
         * @param type The level type: sensitivity and resistance
         * @returns {*}
         */
        function getHighestLevel(treatments, type) {
            if (OncoKB.levels.hasOwnProperty(type)) {
                if (treatments instanceof Array) {
                    var highestLevelIndex = getHighestLevelIndex(treatments, type);
                    if (highestLevelIndex === -1) {
                        return '';
                    } else {
                        return OncoKB.levels[type][highestLevelIndex];
                    }
                }
            } else {
                console.log('Level type: only sensitivity and resistance are supported at this moment.');
            }
            return '';
        }

        /**
         *
         * @param treatments
         * @param type The level type: sensitivity and resistance
         * @returns {number}
         */
        function getHighestLevelIndex(treatments, type) {
            var highestLevelIndex = -1;
            if (OncoKB.levels.hasOwnProperty(type)) {
                var treatmentsL = treatments.length;
                for (var i = 0; i < treatmentsL; i++) {
                    var _index = OncoKB.levels[type].indexOf(getLevel(treatments[i].level));
                    if (_index > highestLevelIndex) {
                        highestLevelIndex = _index;
                    }
                }
            } else {
                console.log('Level type: only sensitivity and resistance are supported at this moment.');
            }
            return highestLevelIndex;
        }

        //Compare the highest level in x and y
        function compareHighestLevel(x, y, type) {
            var highestLevelIndexX = OncoKB.levels[type].indexOf(getLevel(x));
            var highestLevelIndexY = OncoKB.levels[type].indexOf(getLevel(y));
            return highestLevelIndexX === highestLevelIndexY ? 0 : (highestLevelIndexX < highestLevelIndexY ? 1 : -1);
        }

        function getTumorTypeFromClinicalDataMap(clinicalDataMap) {
            if (typeof clinicalDataMap === 'object') {
                var keys = Object.keys(clinicalDataMap);
                if (keys.length > 0 && clinicalDataMap[keys[0]].hasOwnProperty('CANCER_TYPE') && clinicalDataMap[keys[0]].CANCER_TYPE) {
                    return clinicalDataMap[keys[0]].CANCER_TYPE;
                }
            }
            return '';
        }

        function compareOncogenic(x, y) {
            var indexX = getOncogenicIndex(x);
            var indexY = getOncogenicIndex(y);
            return indexX === indexY ? 0 : (indexX < indexY ? 1 : -1);
        }

        function getOncogenicIndex(oncogenic) {
            if (oncogenic === 'Likely Oncogenic') {
                oncogenic = 'Oncogenic'; //Group likely oncogenic with oncogenic
            }
            return OncoKB.oncogenic.indexOf(oncogenic);
        }

        function getTreatmentsLength(oncokbTreatments) {
            var count = 0;
            for (var type in oncokbTreatments) {
                if (oncokbTreatments[type] instanceof Array) {
                    count += oncokbTreatments[type].length;
                }
            }
            return count;
        }

        function compareIcons(category, x, y, type, levelType) {
            var xWeight = -1, yWeight = 1;

            if (type === 'desc') {
                xWeight = 1;
                yWeight = -1;
            }

            if (!x.oncokb || !x.oncokb.hasVariant) {
                return yWeight;
            }
            if (!y.oncokb || !y.oncokb.hasVariant) {
                return xWeight;
            }

            if (category === 'oncogenic') {
                if (!x.oncokb || !x.oncokb.evidence.hasOwnProperty('oncogenic') || OncoKB.utils.getOncogenicIndex(x.oncokb.evidence.oncogenic) === -1) {
                    if (!y.oncokb || !y.oncokb.evidence.hasOwnProperty('oncogenic') || OncoKB.utils.getOncogenicIndex(y.oncokb.evidence.oncogenic) === -1) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!y.oncokb || !y.oncokb.evidence.hasOwnProperty('oncogenic') || OncoKB.utils.getOncogenicIndex(y.oncokb.evidence.oncogenic) === -1) {
                    return xWeight;
                }
                return OncoKB.utils.compareOncogenic(x.oncokb.evidence.oncogenic, y.oncokb.evidence.oncogenic);
            }
            if (category === 'oncokb') {
                if (!x.oncokb || !x.oncokb.hasOwnProperty(levelType) || !x.oncokb[levelType]) {
                    if (!y.oncokb || !y.oncokb.hasOwnProperty(levelType) || !y.oncokb[levelType]) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!y.oncokb || !y.oncokb.hasOwnProperty(levelType) || !y.oncokb[levelType]) {
                    return xWeight;
                }
                if (levelType === 'highestSensitiveLevel') {
                    return OncoKB.utils.compareHighestLevel(x.oncokb[levelType], y.oncokb[levelType], 'sensitivity');
                } else if (levelType === 'highestSensitiveLevel') {
                    return OncoKB.utils.compareHighestLevel(x.oncokb[levelType], y.oncokb[levelType], 'resistance');
                }
            }
            if (category === 'mycancergenome') {
                if (!x.mutation || !x.mutation.myCancerGenome || x.mutation.myCancerGenome.length === 0) {
                    if (!y.mutation.myCancerGenome || y.mutation.myCancerGenome.length === 0) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!y.mutation || !y.mutation.myCancerGenome || y.mutation.myCancerGenome.length === 0) {
                    return xWeight;
                }
                return 0;
            }
            if (category === 'hotspot') {
                if (!x.mutation || !x.mutation.hasOwnProperty('isHotspot') || !x.mutation.isHotspot) {
                    if (!y.mutation || !y.mutation.hasOwnProperty('isHotspot') || !y.mutation.isHotspot) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!y.mutation || !y.mutation.hasOwnProperty('isHotspot') || !y.mutation.isHotspot) {
                    return xWeight;
                }
                return 0;
            }
        }

        function variantNotExist(variantInfo) {
            if (_.isObject(variantInfo) && !variantInfo.isVUS && variantInfo.oncogenic === '' &&
                Object.keys(variantInfo.mutationEffect) &&
                variantInfo.treatments.resistance.length === 0 &&
                variantInfo.trials.length === 0 &&
                variantInfo.prevalence.length === 0 &&
                variantInfo.progImp.length === 0 &&
                variantInfo.treatments.sensitivity.length === 0) {

                return true;
            } else {
                return false;
            }
        }

        function processEvidence(evidences) {
            var result = {}; //id based.
            if (evidences && evidences.length > 0) {
                evidences.forEach(function(record) {
                    var id = record.query.id;
                    var datum = new OncoKB.Evidence();
                    var hasHigherLevelEvidence = false;
                    var sensitivityTreatments = [];
                    var resistanceTreatments = [];

                    record.evidences.forEach(function(evidence) {
                        var description = '';
                        if (evidence.shortDescription) {
                            description = evidence.shortDescription;
                        } else {
                            description = evidence.description;
                        }
                        if (evidence.evidenceType === 'GENE_SUMMARY') {
                            datum.gene.summary = OncoKB.utils.findRegex(description);
                        } else if (evidence.evidenceType === 'GENE_BACKGROUND') {
                            datum.gene.background = OncoKB.utils.findRegex(description);
                        } else if (evidence.evidenceType === 'MUTATION_EFFECT') {
                            var _datum = {};
                            if (evidence.knownEffect) {
                                _datum.knownEffect = evidence.knownEffect;
                            }
                            if (description) {
                                _datum.description = OncoKB.utils.findRegex(description);
                            }
                            datum.alteration.push(_datum);
                        } else if (evidence.levelOfEvidence) {
                            //if evidence has level information, that means this is treatment evidence.
                            if (['LEVEL_0'].indexOf(evidence.levelOfEvidence) === -1) {
                                var _treatment = {};
                                _treatment.tumorType = evidence.tumorType.name;
                                _treatment.level = evidence.levelOfEvidence;
                                _treatment.content = evidence.treatments;
                                _treatment.description = OncoKB.utils.findRegex(description) || 'No yet curated';

                                if (OncoKB.levels.sensitivity.indexOf(OncoKB.utils.getLevel(evidence.levelOfEvidence)) !== -1) {
                                    sensitivityTreatments.push(_treatment);
                                } else {
                                    resistanceTreatments.push(_treatment);
                                }

                                if (_treatment.level === 'LEVEL_1' || _treatment.level === 'LEVEL_2A') {
                                    hasHigherLevelEvidence = true;
                                }
                            }
                        }
                    });

                    if (datum.alteration.length > 0) {
                        datum.mutationEffect = datum.alteration[0];
                    }

                    if (hasHigherLevelEvidence) {
                        sensitivityTreatments.forEach(function(treatment, index) {
                            if (treatment.level !== 'LEVEL_2B') {
                                datum.treatments.sensitivity.push(treatment);
                            }
                        });
                    } else {
                        datum.treatments.sensitivity = sensitivityTreatments;
                    }
                    datum.treatments.resistance = resistanceTreatments;
                    datum.treatments.sensitivity.forEach(function(treatment, index) {
                        if (treatment.level === 'LEVEL_2B') {
                            datum.drugs.sensitivity.inOtherTumor.push(treatment);
                        } else if (treatment.level === 'LEVEL_2A' || treatment.level === 'LEVEL_1') {
                            datum.drugs.sensitivity.current.push(treatment);
                        }
                    });
                    datum.treatments.resistance.forEach(function(treatment, index) {
                        if (treatment.level === 'LEVEL_R1') {
                            datum.drugs.resistance.push(treatment);
                        }
                    });
                    id.split('*ONCOKB*').forEach(function(_id) {
                        result[_id] = datum;
                    })
                });
            }

            return result;
        }

        return {
            findRegex: findRegex,
            consequenceConverter: consequenceConverter,
            getLevel: getLevel,
            getNumberLevel: getNumberLevel,
            getHighestLevel: getHighestLevel,
            compareHighestLevel: compareHighestLevel,
            compareOncogenic: compareOncogenic,
            compareIcons: compareIcons,
            getOncogenicIndex: getOncogenicIndex,
            getTumorTypeFromClinicalDataMap: getTumorTypeFromClinicalDataMap,
            getTreatmentsLength: getTreatmentsLength,
            variantNotExist: variantNotExist,
            processEvidence: processEvidence
        };
    })();

    /**
     * OncoKB web services. Each service should always return jQuery promise
     * @type {{init, oncokbAccess, getEvidence}}
     */
    self.connector = (function() {

        /**
         * Use to decide whether OncoKB web service is accessible
         * @returns {*|{then, catch, finally}} jQuery promise
         */
        function access() {
            var deferred = $.Deferred();
            if (self.url) {
                $.get('api/proxy/oncokbAccess', function() {
                        OncoKB.accessible = true;
                        deferred.resolve();
                    })
                    .fail(function() {
                        deferred.reject();
                    });
            } else {
                deferred.reject();
            }

            return deferred.promise();
        }


        return {
            access: access
        };
    })();

    self.str = (function() {
        function getOncogenicitySummary(oncokbInfo) {
            var oncogenic = _.isObject(oncokbInfo) ? (oncokbInfo.hasOwnProperty('oncogenic') ? oncokbInfo.oncogenic : '') : '';
            var str = '<div class="oncokb"><div><span><b style="font-size:12px;color:#';
            switch (oncogenic) {
                case 'Likely Neutral':
                    str += '2f4f4f">Likely neutral';
                    break;
                case 'Oncogenic':
                    str += '007fff">Known oncogenic';
                    break;
                case 'Likely Oncogenic':
                    str += '007fff">Likely oncogenic';
                    break;
                default:
                    str += '2f4f4f">Unknown to be oncogenic';
                    break;
            }
            str += '</b></span>';

            if (oncokbInfo.mutationEffect.hasOwnProperty('knownEffect')) {
                str += '<span style="float: right"><b>' + (oncokbInfo.mutationEffect.knownEffect.indexOf('Activating') === 0 ? 'Activating' : oncokbInfo.mutationEffect.knownEffect) + '</b>';
                if (oncokbInfo.mutationEffect.hasOwnProperty('description') && oncokbInfo.mutationEffect.description) {
                    str += ' <i class="fa fa-chevron-right oncokb_alt_moreInfo"></i><i class="fa fa-chevron-down oncokb_alt_lessInfo" style="display: none;"></i></span></div><div style="background: #EEE; display: none; float:left;" class="oncokb_mutation_effect">' + oncokbInfo.mutationEffect.description + '</div>';
                } else {
                    str += '</span></div>';
                }
            }

            // Always attach oncogenic description. It will be filled after
            // user hovering OncoKB icon. Also attach a loading gif.
            str += '<br/><span class="oncogenic-description" style="float:left;"><span class="oncogenic-description-loading" style="display: none;"><img src="images/ajax-loader.gif" height="50px" width="50px"/></span><span class="oncogenic-description-content">';
            if (oncokbInfo.oncogenicDescription && oncokbInfo.oncogenicDescription !== 'null') {
                str += oncokbInfo.oncogenicDescription;
            }

            str += '</span></span><br/></div>';

            return str;
        }

        function getOncogenicityFooterStr() {
            return '<div class="oncokb"><button class="oncokbFeedback-btn">Feedback</button><span style="float: right;"><i>   Powered by OncoKB(Beta)</i> <i class="fa fa-chevron-right oncokb_footer_moreInfo"></i><i class="fa fa-chevron-down oncokb_footer_lessInfo" style="display: none;"></i></span>' +
                '<br/><div class="oncokb_footer" style="color: grey; display: none;"><i>OncoKB is under development. To report errors or missing annotation about this variant, please ' +
                '<span class="oncokbFeedback">send us feedback</span>. For general feedback, please send an email to ' +
                '<a href="mailto:oncokb@cbio.mskcc.org" title="Contact us">oncokb@cbio.mskcc.org</a></i></div></div>';
        }

        function getMutationSummaryStrByTreatments(oncokbInfo) {
            var strArray = [];
            var numOfTreatments = oncokbInfo.treatments.sensitivity.length + oncokbInfo.treatments.resistance.length;
            var treatments = {};
            var treatmentsArray = [];
            if (numOfTreatments > 0) {
                strArray.push('<table class="oncokb-treatments-table table table-condensed"><thead><tr><th>Level</th><th>Drug(s)</th><th>Level-associated cancer type(s)</th></tr></thead><tbody>');

                _.each(oncokbInfo.treatments, function(content, type) {
                    _.each(content, function(item, index) {
                        var _level = OncoKB.utils.getLevel(item.level);
                        var _treatment = treatmentsToStr(item.content);
                        var _tumorType = item.tumorType;
                        if (!treatments.hasOwnProperty(_level)) {
                            treatments[_level] = {};
                        }
                        if (!treatments[_level].hasOwnProperty(_treatment)) {
                            treatments[_level][_treatment] = {};
                        }
                        treatments[_level][_treatment][_tumorType] = 1;
                    });
                });
                _.each(_.keys(treatments).sort(function(a, b) {
                    return OncoKB.levels.all.indexOf(a) > OncoKB.levels.all.indexOf(b) ? -1 : 1;
                }), function(level) {
                    _.each(_.keys(treatments[level]).sort(), function(_treatment) {
                        treatmentsArray.push({
                            level: level,
                            treatment: _treatment,
                            tumorTypes: _.keys(treatments[level][_treatment]).map(function(item) {
                                return cbio.util.toTitleCase(item) + '<br/>';
                            }).sort()
                        });
                    });
                });
                _.each(treatmentsArray, function(treatment) {
                    strArray.push('<tr><td><i class="oncokb-level-icon-image level-' + treatment.level + ' qtip-oncokb-level-icon" alt="' + OncoKB.levelsInfo[treatment.level] + '"/></td><td>' + treatment.treatment + '</td><td>' + treatment.tumorTypes.join('') + '</td></tr>')
                });
                strArray.push('</table></tbody>')
            }

            return strArray.join('');
        }

        function getMutationSummaryStrByDrugs(oncokbInfo) {
            var str = '';

            if (oncokbInfo.drugs.sensitivity.current.length > 0) {
                str += '<div class="oncokb"><span><b>FDA approved drugs:</b><br/>';
                oncokbInfo.drugs.sensitivity.current.forEach(function(list) {
                    str += '- ' + treatmentsToStr(list.content) + '<br/>';
                });
                str += '</span></div>';
            }

            if (oncokbInfo.drugs.sensitivity.inOtherTumor.length > 0) {
                str += '<div class="oncokb"><span><b>FDA approved drugs in another cancer:</b><br/>';
                oncokbInfo.drugs.sensitivity.inOtherTumor.forEach(function(list) {
                    str += '- ' + treatmentsToStr(list.content) + '<br/>';
                });
                str += '</span></div>';
            }

            if (oncokbInfo.drugs.resistance.length > 0) {
                str += '<div class="oncokb"><span><b>Confers resistance to:</b><br/>';
                oncokbInfo.drugs.resistance.forEach(function(list) {
                    str += '- ' + treatmentsToStr(list.content) + '<br/>';
                });
                str += '</span></div>';
            }

            return str;
        }

        function getTreatmentStr(treatments) {
            var str = '', i;
            if (treatments instanceof Array) {
                var treatmentsL = treatments.length;
                str += '<table class="oncokb-treatments-datatable"><thead><tr><th>TREATMENTS</th><th>LEVEL</th><th>TUMOR TYPE</th><th>DESCRIPTION</th></tr></thead><tbody>';
                for (i = 0; i < treatmentsL; i++) {
                    str += '<tr>';
                    str += '<td>' + createDrugsStr(treatments[i].content) + '</td>';
                    str += '<td>' + OncoKB.utils.getLevel(treatments[i].level) + '</td>';
                    str += '<td>' + treatments[i].tumorType + '</td>';
//                str += '<td>' + (treatments.length>2?shortDescription(treatments[i].description): treatments[i].description)+ '</td>';
                    str += '<td>' + shortDescription(treatments[i].description) + '</td>';
                    str += '</tr>';
                }
                str += '</tbody>';
            }
            return str;
        }

        function treatmentsToStr(data) {
            if (_.isArray(data)) {
                var treatments = [];

                data.forEach(function(treatment) {
                    treatments.push(drugToStr((treatment.drugs)));
                });

                return treatments.join(',');
            }
        }

        function drugToStr(data) {
            var drugs = [];

            data.forEach(function(drug) {
                drugs.push(drug.drugName);
            });

            return drugs.join('+');
        }

        function createDrugsStr(drugs) {
            var str = '', i, j;
            if (drugs instanceof Array) {
                var drugsL = drugs.length;
                for (i = 0; i < drugsL; i++) {
                    var _drugsL = drugs[i].drugs.length;

                    for (j = 0; j < _drugsL; j++) {
                        str += drugs[i].drugs[j].drugName;
                        if (j != _drugsL - 1) {
                            str += '+';
                        }
                    }

                    if (i != drugsL - 1) {
                        str += ', ';
                    }
                }
            }
            return str;
        }

        function shortDescription(description) {
            var str = '';
            var threshold = 80;
            var shortStr = description.substring(0, threshold - 8);
            //Need to identify <a> tag, you do not want to cut the string in mid of <a> tag
            var aIndex = {
                start: -1,
                end: -1
            };
            if (description && description.length > threshold) {
                if (shortStr.indexOf('<a') !== -1) {
                    aIndex.start = shortStr.indexOf('<a');
                    if (shortStr.indexOf('</a>') !== -1 && shortStr.indexOf('</a>') < (threshold - 8 - 3)) {
                        aIndex.end = shortStr.indexOf('</a>');
                    }
                }

                if (aIndex.start > -1) {
                    //Means the short description has part of <a> tag
                    if (aIndex.end == -1) {
                        shortStr = description.substring(0, (aIndex.start));
                    }
                }
                str = '<span><span class="oncokb-shortDescription">' + shortStr + '<span class="oncokb-description-more" >... <a>more</a></span></span>';
                str += '<span class="oncokb-fullDescription" style="display:none">' + description + '</span></span>';
            } else {
                str = '<span class="oncokb-fullDescriotion">' + description + '</span>';
            }

            return str;
        }

        function getTrialsStr(trials) {
            var i, str = '';

            if (trials instanceof Array) {
                var trialsL = trials.length;
                for (i = 0; i < trialsL; i++) {
                    str += OncoKB.utils.findRegex(trials[i].nctId);
                    if (i != trialsL - 1) {
                        str += ' ';
                    }
                }
            }
            return str;
        }

        function getVUSsummary(isRecurrent, isHotspot, variantNotExist) {
            var str = ['<div><span>'];
            if (variantNotExist) {
                str.push('<b>No information.</b>');
            } else {
                if (isRecurrent || isHotspot) {
                    var types = [];

                    str.push('Due to its recurrence in cancer (');

                    if (isRecurrent) {
                        types.push('COSMIC');
                    }
                    if (isHotspot) {
                        types.push('<a href=&quot;http://www.ncbi.nlm.nih.gov/pubmed/26619011&quot; target=&quot;_blank&quot;>Chang, M. et al. Nature Biotech. 2015</a>');
                    }

                    str.push(types.join(', '));
                    str.push('), this variant may be oncogenic.');
                } else {
                    str.push('This rare variant is of unknown significance.');
                }
            }
            str.push('</span></div>');
            return str.join('');
        }

        /**
         * Get gene qtip content.
         * @param gene {Object} It should include summary and background info.
         * @returns {string} Gene qtip content
         */
        function getGeneSummaryBackground(gene) {
            var tooltip = '';

            if (_.isObject(gene)) {
                if (gene.summary) {
                    tooltip += '<b>Gene Summary</b><br/>' + gene.summary;
                }
                if (gene.background) {
                    tooltip += '<br/><div><span class="oncokb_gene_moreInfo"><br/><a>More Info</a><i style="float:right">Powered by OncoKB(Beta)</i></span><br/><span class="oncokb_gene_background" style="display:none"><b>Gene Background</b><br/>' + gene.background + '<br/><i style="float:right">Powered by OncoKB(Beta)</i></span></div>';
                }
            }

            return tooltip;
        }

        function getNCBIGeneLink(entrezGeneId) {
            if (_.isNumber(entrezGeneId)) {
                return "<a href=\"http://www.ncbi.nlm.nih.gov/gene/"
                    + entrezGeneId + "\">NCBI Gene</a>";
            } else {
                return "";
            }
        }

        return {
            getOncogenicitySummary: getOncogenicitySummary,
            getOncogenicityFooterStr: getOncogenicityFooterStr,
            getMutationSummaryStrByTreatments: getMutationSummaryStrByTreatments,
            getMutationSummaryStrByDrugs: getMutationSummaryStrByDrugs,
            getTreatmentStr: getTreatmentStr,
            getTrialsStr: getTrialsStr,
            getShortDescription: shortDescription,
            getVUSsummary: getVUSsummary,
            getGeneSummaryBackground: getGeneSummaryBackground,
            getNCBIGeneLink: getNCBIGeneLink
        };
    })();

    self.svgs = (function() {
        function oncokbIcon(g, text, fill, fontSize, textX, textY) {
            textX = textX || 7;
            textY = textY || 11;
            g.append("rect")
                .attr("rx", '3')
                .attr("ry", '3')
                .attr('width', '14')
                .attr('height', '14')
                .attr("fill", fill);
            g.append("text")
                .attr('transform', 'translate(' + textX + ', ' + textY + ')')
                .attr('text-anchor', 'middle')
                .attr("font-size", fontSize)
                .attr('font-family', 'Sans-serif')
                .attr('stroke-width', 0)
                .attr("fill", '#ffffff')
                .text(text);
        }

        function oncokbLevelIcon(g, level, fill, x, y, textX, textY) {
            x = x || 13;
            y = y || 0;
            textX = textX || x || 13;
            textY = textY || (y + 3) || 0;
            g.append("circle")
                .attr('transform', 'translate(' + x + ', ' + y + ')')
                .attr('r', '6')
                .attr("fill", fill);
            g.append("text")
                .attr('transform', 'translate(' + textX + ', ' + textY + ')')
                .attr('text-anchor', 'middle')
                .attr("font-size", '10')
                .attr('font-family', 'Sans-serif')
                .attr('stroke-width', 0)
                .attr("fill", '#ffffff')
                .text(level);
        }

        function calSvgWidth(data) {
            var numOfItems = 0;
            var types = calNumTreatmentTypes(data.treatments);
            if (types > 0) {
                numOfItems += 0.8 + types * 0.2;
            }
            if (data.progImp instanceof Array && data.progImp.length > 0) {
                ++numOfItems;
            }
            if (data.trials instanceof Array && data.trials.length > 0) {
                ++numOfItems;
            }
            if (data.prevalence instanceof Array && data.prevalence.length > 0) {
                ++numOfItems;
            }
            return 20 * numOfItems + (numOfItems > 0 ? 0 : (10 * (numOfItems - 1)));
        }

        function calNumTreatmentTypes(treatments) {
            var types = 0;
            _.each(treatments, function(treatment, type) {
                if (_.isArray(treatment) && treatment.length > 0) {
                    ++types;
                }
            });
            return types;
        }

        function getAllTreatments(treatments) {
            var allTreatments = []
            _.each(treatments, function(treatment, type) {
                if (_.isArray(treatment) && treatment.length > 0) {
                    allTreatments = allTreatments.concat(treatment);
                }
            });
            return allTreatments;
        }

        function createOncoKBColumnCell(target, data) {
            var svgWidth = calSvgWidth(data);
            if (svgWidth > 0) {
                svgWidth = 22;
                var svg = d3.select($(target)[0])
                    .append("svg")
                    .attr("width", svgWidth)
                    .attr("height", 20)
                    .style('vertical-align', 'bottom');

                var qtipContext = '', i, g;
                var itemNum = 0;
                var types = calNumTreatmentTypes(data.treatments);

                if (types > 0) {
                    var treatmentDataTable;
                    var typeOrder = ['resistance', 'sensitivity'];
                    g = svg.append("g")
                        .attr("transform", "translate(" + itemNum * 20 + ", 6)");
                    oncokbIcon(g, 'Tx', "#5555CC", 9, 7, types > 1 ? 12 : 11);
                    if (types === 1) {
                        _.each(data.treatments, function(treatment, type) {
                            if (treatment instanceof Array && treatment.length > 0) {
                                var level = OncoKB.utils.getHighestLevel(treatment, type);
                                var numberLevel = level.match(/\d+/)[0];
                                oncokbLevelIcon(g, numberLevel, type === 'resistance' ? '#ff0000' : '#008000');
                            }
                        });
                    } else {
                        var x = 8, y = 0;
                        _.each(typeOrder, function(type, index) {
                            var treatments = data.treatments[type];
                            if (treatments instanceof Array && treatments.length > 0) {
                                var level = OncoKB.utils.getHighestLevel(treatments, type);
                                var numberLevel = level.match(/\d+/)[0];
                                oncokbLevelIcon(g, numberLevel, type === 'resistance' ? '#ff0000' : '#008000', x + index * 6, y, (x + index * 6) - ((index === (typeOrder.length - 1)) ? 0 : 1), (y + 3));
                            }
                        })
                    }
                    qtipContext = OncoKB.str.getTreatmentStr(getAllTreatments(data.treatments));
                    g.on('mouseover', function() {
                        if (!$(this).hasClass('qtip')) {
                            $(this).qtip({
                                content: {text: qtipContext},
                                hide: {
                                    fixed: true,
                                    delay: 100,
                                    event: "mouseleave"
                                },
                                style: {
                                    classes: 'qtip-light qtip-rounded qtip-shadow oncokb-qtip',
                                    tip: true
                                },
                                show: {
                                    event: "mouseover",
                                    solo: true,
                                    delay: 0,
                                    ready: true
                                },
                                position: {
                                    my: 'center right',
                                    at: 'center left',
                                    viewport: $(window)
                                },
                                events: {
                                    render: function(event, api) {
                                        $(this).find('.oncokb-description-more').click(function() {
                                            $(this).parent().parent().find('.oncokb-fullDescription').css('display', 'block');
                                            $(this).parent().parent().find('.oncokb-shortDescription').css('display', 'none');
                                            if (treatmentDataTable) {
                                                treatmentDataTable.fnAdjustColumnSizing();
                                            }
                                        });
                                        treatmentDataTable = $(this).find('.oncokb-treatments-datatable').dataTable({
                                            "columnDefs": [
                                                {
                                                    "orderDataType": "oncokb-level",
                                                    "targets": 1
                                                },
                                                {
                                                    "type": "oncokb-level",
                                                    "targets": 1
                                                },
                                                {
                                                    "orderData": [1, 0],
                                                    "targets": 1
                                                }
                                            ],
                                            "sDom": 'rt',
                                            "bPaginate": false,
                                            "bScrollCollapse": true,
                                            "sScrollY": 400,
                                            "autoWidth": true,
                                            "order": [[1, "asc"]]
                                        });
                                    },
                                    visible: function(event, api) {
                                        if (treatmentDataTable) {
                                            treatmentDataTable.fnAdjustColumnSizing();
                                        }
                                    }
                                }
                            });
                        }
                    });
                    itemNum += 0.5 + 0.5 * calNumTreatmentTypes;
                }
            }
        }

        function createOncogenicImage(target, oncogenic, notExist, highestSensitiveLevel, highestResistanceLevel) {
            var iconType = ["", "unknown-oncogenic"];

            var sl = OncoKB.utils.getNumberLevel(highestSensitiveLevel);
            var rl = OncoKB.utils.getNumberLevel(highestResistanceLevel);

            if (!rl) {
                switch (sl) {
                    case '1':
                        iconType[0] = 'level1';
                        break;
                    case '2':
                        iconType[0] = 'level2';
                        break;
                    case '3':
                        iconType[0] = 'level3';
                        break;
                    case '4':
                        iconType[0] = 'level4';
                        break;
                    default:
                        break;
                }
            } else {
                if (!sl) {
                    switch (rl) {
                        case '1':
                            iconType[0] = 'levelR1';
                            break;
                        case '2':
                            iconType[0] = 'levelR2';
                            break;
                        case '3':
                            iconType[0] = 'levelR3';
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (sl) {
                        case '1':
                            iconType[0] = 'level1R';
                            break;
                        case '2':
                            iconType[0] = 'level2R';
                            break;
                        case '3':
                            iconType[0] = 'level3R';
                            break;
                        case '4':
                            iconType[0] = 'level4R';
                            break;
                        default:
                            break;
                    }
                }
            }

            if (_.isBoolean(notExist) && notExist) {
                iconType[1] = 'unknown-oncogenic';
            } else if (iconType[1] === 'unknown-oncogenic') {
                switch (oncogenic) {
                    case 'Likely Neutral':
                        iconType[1] = 'likely-neutral';
                        break;
                    case 'Unknown':
                        iconType[1] = 'vus';
                        break;
                    case 'Likely Oncogenic':
                        iconType[1] = 'oncogenic';
                        break;
                    case 'Oncogenic':
                        iconType[1] = 'oncogenic';
                        break;
                    default:
                        iconType[1] = 'vus';
                        break;
                }
            }
            var icon = $(target).append('<i class="oncogenic-icon-image ' + iconType.join(' ') + '"/>');
        }

        function createOncogenicIcon(target, oncogenic, notExist, highestSensitiveLevel, highestResistanceLevel) {
            var svg = d3.select(target)
                .append("svg")
                .attr("width", 17)
                .attr("height", 16);
            var numOflevels = 0;

            var g = svg.append('g')
                .attr('transform', 'translate(7, 9)');

            var color;
            switch (oncogenic) {
                case 0:
                    color = '#696969';
                    break;
                case -1:
                    color = '#AAAAAA';
                    break;
                case 2:
                    color = '#007FFF';
                    break;
                case 1:
                    color = '#007FFF';
                    break;
                default:
                    color = '#AAAAAA';
                    break;
            }

            if (_.isBoolean(notExist) && notExist) {
                color = '#CCC';
            }

            //Append three circals
            g.append('circle')
                .attr('r', '6')
                .attr('fill', 'none')
                .attr('stroke-width', '2')
                .attr('stroke', color);

            if (!_.isBoolean(notExist) || !notExist) {
                g.append('circle')
                    .attr('r', '3')
                    .attr('fill', 'none')
                    .attr('stroke-width', '2')
                    .attr('stroke', color);

                g.append('circle')
                    .attr('r', '1.5')
                    .attr('fill', color)
                    .attr('stroke', 'none');

            }

            if (hasResistanceDrugs && oncogenic === 2) {
                var resistanceDot = svg.append('g')
                    .attr('transform', 'translate(13, 4)');

                resistanceDot.append('circle')
                    .attr('r', '4')
                    .attr('fill', '#ffa500');
            }
        }

        return {
            createOncokbColumnCell: createOncoKBColumnCell,
            createOncogenicIcon: createOncogenicIcon,
            createOncogenicImage: createOncogenicImage
        };
    })();

    self.tooltips = (function() {
        function evidenceLevels() {
            var levels = {
//            '0': 'FDA-approved drug in this indication irrespective of gene/variant biomarker.',
                '1': 'FDA-approved biomarker and drug association in this indication.',
                '2A': 'FDA-approved biomarker and drug association in another indication, and NCCN-compendium listed for this indication.',
                '2B': 'FDA-approved biomarker in another indication, but not FDA or NCCN-compendium-listed for this indication.',
                '3': 'Clinical evidence links this biomarker to drug response but no FDA-approved or NCCN compendium-listed biomarker and drug association.',
                '4': 'Preclinical evidence potentially links this biomarker to response but no FDA-approved or NCCN compendium-listed biomarker and drug association.',
                'R1': 'NCCN-compendium listed biomarker for resistance to a FDA-approved drug.',
                'R2': 'Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance.',
                'R3': 'Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance.'
            };
            var str = '<b>Level of therapeutic implications explanations:</b><br/>';

            for (var level in levels) {
                str += '<b>' + level + '</b>: ' + levels[level] + '<br/>';
            }

            return str;
        }

        return {

            /**
             *
             * @param array this is object array, the object should have tumorType and description attributes
             */
            oncokbColumn: function(array, title, tableClass) {
                var str = '', i;
                if (array instanceof Array) {
                    var arrayL = array.length;
                    str += '<table class="oncokb-' + tableClass + '-datatable"><thead><tr><th style="white-space:nowrap">TUMOR TYPE</th><th>' + title + '</th></tr></thead><tbody>';
                    for (i = 0; i < arrayL; i++) {
                        str += '<tr>';
                        str += '<td style="white-space:nowrap">' + array[i].tumorType + '</td>';
                        str += '<td>' + self.str.getShortDescription(array[i].description) + '</td>';
                        str += '</tr>';
                    }
                    str += '</tbody></table>';
                }
                return str;
            },

            levelTooltip: function(target) {
                target.qtip({
                    content: {text: this.evidenceLevels()},
                    hide: {fixed: true, delay: 100},
                    style: {
                        classes: 'qtip-light qtip-rounded qtip-shadow',
                        tip: true
                    },
                    position: {
                        my: 'center right',
                        at: 'center left',
                        viewport: $(window)
                    }
                });
            }
        };
    })();

    return {
        VariantPair: VariantPair,
        Instance: Instance,
        addInstanceManager: addInstanceManager,
        EvidenceRequestItem: EvidenceRequestItem,
        Evidence: Evidence,
        access: self.connector.access,
        getAccess: function() {
            return self.accessible;
        },
        levels: self.levels,
        levelColors: self.levelColors,
        levelsInfo: self.levelsInfo,
        oncogenic: self.oncogenic,
        utils: self.utils,
        tooltips: self.tooltips,
        connector: self.connector,
        str: self.str,
        svgs: self.svgs,
        setUrl: function(url) {
            self.url = url;
            if (url) {
                self.accessible = true;
            }
        }
    };
})(window._, (window.$ || window.jQuery));

OncoKB.Instance.prototype = {
    getHash: function(gene, mutation, tt, consequence) {

    },

    addVariant: function(id, entrezGeneId, gene, mutation, tt, consequence, cosmicCount, isHotspot, civicVariantSummaries, civicClinicalEvidenceStats, proteinStart, proteinEnd) {
        var _variant = new OncoKB.VariantPair();

        if (!isNaN(id)) {
            id = id.toString();
        }

        _variant.id = id;
        _variant.entrezGeneId = Number(entrezGeneId);
        _variant.gene = gene || '';
        _variant.alteration = mutation || '';
        _variant.consequence = _.isString(consequence) ? OncoKB.utils.consequenceConverter(consequence) : '';
        _variant.proteinStart = proteinStart || '';
        _variant.proteinEnd = proteinEnd || '';

        if (tt) {
            _variant.tumorType = tt;
        } else if (this.tumorType) {
            _variant.tumorType = this.tumorType;
        }
        if (!isNaN(cosmicCount)) {
            _variant.cosmicCount = cosmicCount;
        }
        if (isHotspot) {
            _variant.isHotspot = true;
        }
        this.variants[_variant.id] = _variant;
        return _variant.id;
    },

    removeVariant: function(id) {
        if (this.variants.hasOwnProperty(id)) {
            delete this.variants[id];
        }
    },

    getVariant: function(id) {
        if (this.variants.hasOwnProperty(id)) {
            return this.variants[id];
        } else {
            return null;
        }
    },

    getVariantStr: function() {
        var gene = [], mutation = [], consequence = [];
        for (var variant in this.variants) {
            gene.push(variant.gene);
            mutation.push(variant.mutation);
            consequence.push(variant.consequence);
        }
        return {
            gene: gene.join(','),
            mutation: mutation.join(','),
            consequence: consequence.join(',')
        };
    },

    /**
     * Get OncoKB basic info including Oncogenic, hasGene, highest resistance
     * level, highest sensitive level, is VUS and hasVariant
     * @param variants
     * @returns {*|{then, catch, finally}} jQuery promise
     */
    getIndicator: function() {
        var oncokbServiceData = {
            'geneStatus': this.geneStatus,
            'source': this.source,
            'evidenceTypes': this.evidenceTypes,
            'queries': [],
            'levels': this.evidenceLevels
        };
        var oncokbEvidenceRequestItems = {};
        var oncokbSummaryData = {};
        var deferred = $.Deferred();
        var str = this.getVariantStr();
        var self = this;

        self.setVariantUniqueIds();
        
        for (var key in this.variants) {
            var variant = this.variants[key];
            var uniqueStr = variant.gene + variant.alteration + variant.tumorType + variant.consequence;
            if (!oncokbEvidenceRequestItems.hasOwnProperty(uniqueStr)) {
                oncokbEvidenceRequestItems[uniqueStr] = new OncoKB.EvidenceRequestItem(variant);
            }
            oncokbEvidenceRequestItems[uniqueStr].ids.push(variant.id);
        }

        oncokbServiceData.queries = $.map(oncokbEvidenceRequestItems, function(value, index) {
            value.id = value.ids.join('*ONCOKB*');
            delete value.ids;
            return value;
        });

        $.ajax({
            type: 'POST',
            url: 'api/proxy/oncokb',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(oncokbServiceData)
        }).done(function(d1) {
                if (d1 && d1.length > 0) {
                    d1.forEach(function(record) {
                        var id = record.query.id;
                        var datum = new OncoKB.Evidence();

                        datum.oncogenic = record.oncogenic;

                        id.split('*ONCOKB*').forEach(function(_id) {
                            if (self.variants.hasOwnProperty(_id)) {
                                self.variants[_id].hasGene = record.geneExist;
                                self.variants[_id].highestResistanceLevel = record.highestResistanceLevel;
                                self.variants[_id].highestSensitiveLevel = record.highestSensitiveLevel;
                                self.variants[_id].isVUS = record.isVUS;
                                self.variants[_id].hasVariant = record.variantExist;
                                self.variants[_id].evidence = $.extend(self.variants[_id].evidence, datum);
                            }
                        })
                    });
                }
                self.dataReady = true;

                deferred.resolve();
            })
            .fail(function() {
                console.log('POST failed.');
                deferred.reject();
            });

        return deferred.promise();
    },
    setVariantUniqueIds: function() {
        var uniqueIds = {};
        var self = this;
        
        for (var key in self.variants) {
            var variant = self.variants[key];
            var variantId = variant.getVariantId();
            if (!uniqueIds.hasOwnProperty(variantId)) {
                uniqueIds[variantId] = [];
            }
            uniqueIds[variantId].push(variant.id);
        }
        
        _.each(uniqueIds, function(item, key) {
            var str = item.join('*ONCOKB*');
            _.each(item, function(variant, index) {
                self.variantUniqueIds[variant] = str;
            })
        });
    },
    getVariantUniqueIds: function(oncokbId) {
        return this.variantUniqueIds[oncokbId];
    },
    getEvidence: function(oncokbId) {
        var deferred = $.Deferred();
        var self = this;

        var variant = self.variants[oncokbId];
        var query = new OncoKB.EvidenceRequestItem(variant);
        var oncokbServiceData = {
            'geneStatus': self.geneStatus,
            'source': self.source,
            'evidenceTypes': self.evidenceTypes,
            'queries': [],
            'levels': self.evidenceLevels
        };

        query.id = self.getVariantUniqueIds(oncokbId);
        delete query.ids;

        oncokbServiceData.queries.push(query);

        if (variant.updatedEvidence) {
            deferred.resolve();
        } else {
            $.ajax({
                    type: 'POST',
                    url: 'api/proxy/oncokbEvidence',
                    dataType: 'json',
                    contentType: 'application/json',
                    data: JSON.stringify(oncokbServiceData)
                })
                .done(function(d1) {
                    var result = OncoKB.utils.processEvidence(d1);
                    _.each(result, function(item, index) {
                        self.variants[index].evidence.gene = item.gene;
                        self.variants[index].evidence.alteration = item.alteration;
                        self.variants[index].evidence.treatments = item.treatments;
                        self.variants[index].evidence.drugs = item.drugs;
                        self.variants[index].evidence.mutationEffect = item.mutationEffect;
                    });
                    variant.updatedEvidence = true;
                    deferred.resolve();
                })
                .fail(function() {
                    variant.updatedEvidence = true;
                    deferred.reject();
                });

        }

        return deferred.promise();
    },

    getSummary: function(oncokbId) {
        var deferred = $.Deferred();
        var self = this;
        var variant = self.variants[oncokbId];
        var oncokbSummaryData = {
            'source': 'cbioportal',
            'type': 'variantCustomized',
            'queries': [{
                id: self.getVariantUniqueIds(oncokbId),
                hugoSymbol: variant.gene,
                alteration: variant.alteration,
                tumorType: variant.tumorType,
            }]
        };


        if(variant.consequence) {
            oncokbSummaryData.queries[0].consequence = variant.consequence;
        }
        if(variant.proteinStart) {
            oncokbSummaryData.queries[0].proteinStart = variant.proteinStart;
        }
        if(variant.proteinEnd) {
            oncokbSummaryData.queries[0].proteinEnd = variant.proteinEnd;//tumor type
        }
        
        if (self.variants[oncokbId].evidence.summary) {
            deferred.resolve();
        } else {
            $.ajax({
                    type: 'POST',
                    url: 'api/proxy/oncokbSummary',
                    dataType: 'json',
                    contentType: 'application/json',
                    data: JSON.stringify(oncokbSummaryData)
                })
                .done(function(d) {
                    var data = _.isArray(d) && _.isObject(d[0]) ? d[0] : null;
                    if(data && data.summary) {
                        if(data.id) {
                            data.id.split('*ONCOKB*').forEach(function(_id) {
                                self.variants[_id].evidence.summary = data.summary;
                            })
                        }else {
                            self.variants[oncokbId].evidence.summary = data.summary;
                        }
                    }else {
                        self.variants[oncokbId].evidence.summary = '';
                    }
                    deferred.resolve();
                })
                .fail(function() {
                    deferred.reject();
                });
        }
        return deferred.promise();
    },
    setTumorType: function(tumorType) {
        this.tumorType = tumorType;
    },

    setGeneStatus: function(geneStatus) {
        this.geneStatus = geneStatus;
    },

    setEvidenceType: function(evidenceType) {
        this.evidenceType = evidenceType;
    },

    addEvents: function(target, type) {
        var self = this;
        if (self.dataReady) {
            if (typeof  type === 'undefined' || type === 'gene') {
                $(target).find('.oncokb_gene').each(function() {
                    var oncokbId = $(this).attr('oncokbId');
                    var gene = self.variants[oncokbId].evidence.gene;
                    var hasGene = self.variants[oncokbId].hasGene;
                    var _tip = '';

                    if (_.isObject(gene) && Object.keys(gene).length > 0) {
                        _tip = OncoKB.str.getGeneSummaryBackground(gene);
                    } else if (hasGene) {
                        _tip = '<span class="oncogenic-loading"><img src="images/ajax-loader.gif" height="50px" width="50px"/></span>'
                    } else {
                        _tip = OncoKB.str.getNCBIGeneLink(self.variants[oncokbId].entrezGeneId);
                    }
                    if (_tip !== '') {
                        $(this).css('display', '');
                        $(this).one('mouseenter', function() {
                            $(this).qtip({
                                content: {text: _tip},
                                show: {ready: true},
                                hide: {fixed: true, delay: 100},
                                style: {
                                    classes: 'qtip-light qtip-rounded qtip-shadow oncokb-qtip',
                                    tip: true
                                },
                                position: {
                                    my: 'top left',
                                    at: 'bottom right',
                                    viewport: $(window)
                                },
                                events: {
                                    render: function(event, api) {
                                        self.getEvidence(oncokbId)
                                            .done(function() {
                                                var variant = self.variants[oncokbId];
                                                var tooltip = OncoKB.str.getGeneSummaryBackground(variant.evidence.gene);
                                                var ncbiLink = OncoKB.str.getNCBIGeneLink(self.variants[oncokbId].entrezGeneId);
                                                api.set('content.text', tooltip ? tooltip : (ncbiLink ? ncbiLink : '<b>No information.</b>'));
                                                api.elements.content.find(".oncokb_gene_moreInfo").click(function() {
                                                    $(this).css('display', 'none');
                                                    $(this).parent().find('.oncokb_gene_background').css('display', '');
                                                });
                                            })
                                            .fail(function() {
                                                api.set('content.text', 'OncoKB service is not available at this moment.');
                                            });
                                    }
                                }
                            });
                        });
                    }
                });
            }

            if (typeof  type === 'undefined' || type === 'alteration') {
                $(target).find('.oncokb_alteration').each(function() {
                    var oncokbId = $(this).attr('oncokbId');

                    $(this).empty();
                    if (self.variants.hasOwnProperty(oncokbId)) {
                        var _tip = '', _oncogenicTip = '<span class="oncogenic-loading"><img src="images/ajax-loader.gif" height="50px" width="50px"/></span>', _hotspotTip = '';
                        var variantNotExist = !self.variants[oncokbId].hasVariant;
                        var qtipMaxWidthClass = '';

                        if (self.variants[oncokbId].evidence.hasOwnProperty('oncogenic')) {
                            OncoKB.svgs.createOncogenicImage(
                                this, self.variants[oncokbId].evidence.oncogenic,
                                variantNotExist,
                                self.variants[oncokbId].highestSensitiveLevel,
                                self.variants[oncokbId].highestResistanceLevel
                            );
                        } else {
                            OncoKB.svgs.createOncogenicImage(this, -1, false);
                        }

                        _hotspotTip = "<b>Recurrent Hotspot</b><br/>This mutated amino acid was identified as a recurrent hotspot (statistical significance, q-value < 0.01) in a set of 11,119 tumor samples of various cancer types (based on <a href='http://www.ncbi.nlm.nih.gov/pubmed/26619011' target='_blank'>Chang, M. et al. Nature Biotech. 2015</a>).";


                        if ($(this).hasClass('oncogenic')) {
                            _tip = _oncogenicTip;
                        } else if ($(this).hasClass('hotspot')) {
                            _tip = _hotspotTip;
                        }

                        //Decide qtip width based on length of mutation effect
                        if (self.variants[oncokbId].evidence.hasOwnProperty('mutationEffect') && self.variants[oncokbId].evidence.mutationEffect.hasOwnProperty('knownEffect')) {
                            var length = self.variants[oncokbId].evidence.mutationEffect.knownEffect.length;
                            if (length > 60) {
                                qtipMaxWidthClass = 'oncokb-qtip'
                            } else if (length > 30) {
                                qtipMaxWidthClass = 'oncokb-qtip-sm'
                            } else {
                                qtipMaxWidthClass = 'oncokb-qtip-xs'
                            }
                        }

                        if (_tip !== '') {
                            $(this).css('display', '');
                            $(this).one('mouseenter', function() {
                                $(this).qtip({
                                    content: {text: _tip},
                                    show: {ready: true},
                                    hide: {fixed: true, delay: 100},
                                    style: {
                                        classes: 'qtip-light qtip-rounded qtip-shadow oncokb-qtip',
                                        tip: true
                                    },
                                    position: {
                                        my: 'top left',
                                        at: 'bottom right',
                                        viewport: $(window)
                                    },
                                    events: {
                                        render: function(event, api) {
                                            $.when(self.getEvidence(oncokbId), self.getSummary(oncokbId))
                                                .done(function() {
                                                    var tooltip = '';
                                                    var variant = self.variants[oncokbId];

                                                    //if (variant.hasVariant) {
                                                        tooltip += OncoKB.str.getOncogenicitySummary(self.variants[oncokbId].evidence);
                                                    //}

                                                    if (variant.isVUS) {
                                                        tooltip += OncoKB.str.getVUSsummary(self.variants[oncokbId].cosmicCount >= 10, self.variants[oncokbId].isHotspot, !variant.hasVariant);
                                                    } else {
                                                        tooltip += OncoKB.str.getMutationSummaryStrByTreatments(self.variants[oncokbId].evidence);
                                                    }

                                                    if (tooltip == '') {
                                                        tooltip += '<b>No information.</b><br/>';
                                                    }

                                                    tooltip += OncoKB.str.getOncogenicityFooterStr();

                                                    api.set('content.text', tooltip);

                                                    api.elements.content.find('.oncogenic-description-content').text(variant.evidence.summary);

                                                    var user = userName === 'anonymousUser' ? '' : userName;
                                                    var dialog = new BootstrapDialog({
                                                        message: function(dialogRef) {
                                                            var div = $('<div></div>');
                                                            var closeIcon = $('<div><span class="bootstrap-dialog-close">x</span></div>');
                                                            var message = $('<div><iframe src="https://docs.google.com/forms/d/1lt6TtecxHrhIE06gAKVF_JW4zKFoowNFzxn6PJv4g7A/viewform?entry.1744186665=' + self.variants[oncokbId].gene + '&entry.1671960263=' + self.variants[oncokbId].alteration + '&entry.118699694&entry.1568641202&entry.1381123986=' + user + '&embedded=true" width="550" height="880" frameborder="0" marginheight="0" marginwidth="0">Loading...</iframe>');

                                                            closeIcon
                                                                .find('.bootstrap-dialog-close')
                                                                .on('click', {dialogRef: dialogRef}, function(event) {
                                                                    event.data.dialogRef.close();
                                                                });
                                                            div.append(closeIcon);
                                                            div.append(message);
                                                            return div;
                                                        },
                                                        closable: true
                                                    });

                                                    api.elements.content.find('.oncokbFeedback, .oncokbFeedback-btn').click(function() {
                                                        api.hide();
                                                        dialog.realize();
                                                        dialog.getModalHeader().hide();
                                                        dialog.getModalFooter().hide();
                                                        dialog.open();
                                                    });

                                                    api.elements.content.find(".oncokb_oncogenic_moreInfo").click(function() {
                                                        $(this).css('display', 'none');
                                                        $(this).parent().find('.oncokb_oncogenic_description').css('display', '');
                                                        $(this).parent().find('.oncokb_oncogenic_lessInfo').css('display', '');
                                                    });
                                                    api.elements.content.find(".oncokb_oncogenic_lessInfo").click(function() {
                                                        $(this).css('display', 'none');
                                                        $(this).parent().find('.oncokb_oncogenic_description').css('display', 'none');
                                                        $(this).parent().find('.oncokb_oncogenic_moreInfo').css('display', '');
                                                    });
                                                    api.elements.content.find(".oncokb_alt_moreInfo").click(function() {
                                                        $(this).css('display', 'none');
                                                        $(this).parent().parent().parent().find('.oncokb_mutation_effect').css('display', '');
                                                        $(this).parent().parent().find('.oncokb_alt_lessInfo').css('display', '');
                                                    });
                                                    api.elements.content.find(".oncokb_alt_lessInfo").click(function() {
                                                        $(this).css('display', 'none');
                                                        $(this).parent().parent().parent().find('.oncokb_mutation_effect').css('display', 'none');
                                                        $(this).parent().parent().find('.oncokb_alt_moreInfo').css('display', '');
                                                    });
                                                    api.elements.content.find(".oncokb_footer_moreInfo").click(function() {
                                                        $(this).css('display', 'none');
                                                        $(this).parent().parent().find('.oncokb_footer').css('display', '');
                                                        $(this).parent().find('.oncokb_footer_lessInfo').css('display', '');
                                                    });
                                                    api.elements.content.find(".oncokb_footer_lessInfo").click(function() {
                                                        $(this).css('display', 'none');
                                                        $(this).parent().parent().find('.oncokb_footer').css('display', 'none');
                                                        $(this).parent().find('.oncokb_footer_moreInfo').css('display', '');
                                                    });
                                                    api.elements.content.find(".qtip-oncokb-level-icon").qtip({
                                                        content: {attr: 'alt'},
                                                        show: {
                                                            event: "mouseover",
                                                            delay: 0
                                                        },
                                                        hide: {
                                                            fixed: true,
                                                            delay: 100
                                                        },
                                                        style: {
                                                            classes: 'qtip-light qtip-rounded qtip-shadow',
                                                            tip: true
                                                        },
                                                        position: {
                                                            my: 'right center',
                                                            at: 'left center',
                                                            viewport: $(window)
                                                        }
                                                    });
                                                })
                                                .fail(function() {
                                                    api.set('content.text', 'OncoKB service is not available at this moment.');
                                                });
                                            //}
                                        }
                                    }
                                });
                            });
                        }
                    }
                });
            }
        }
    },

    getId: function() {
        return this.id;
    }
};

$.fn.dataTableExt.oSort['oncokb-level-asc'] = function(x, y) {
    var xIndex = OncoKB.levels.all.indexOf(x);
    var yIndex = OncoKB.levels.all.indexOf(y);
    if (xIndex < yIndex) {
        return 1;
    } else {
        return -1;
    }
};

$.fn.dataTableExt.oSort['oncokb-level-desc'] = function(x, y) {
    var xIndex = OncoKB.levels.all.indexOf(x);
    var yIndex = OncoKB.levels.all.indexOf(y);
    if (xIndex < yIndex) {
        return -1;
    } else {
        return 1;
    }
};

$.fn.dataTableExt.oSort['sort-icons-asc'] = function(x, y) {
    var categories = ['oncogenic', 'oncokb-sensitivity', 'oncokb-resistance', 'mycancergenome', 'hotspot'];
    var categoryL = categories.length;

    for (var i = 0; i < categoryL; i++) {
        var item = categories[i];
        var result;

        if (item === 'oncokb-sensitivity') {
            result = OncoKB.utils.compareIcons('oncokb', x, y, 'asc', 'highestSensitiveLevel')
        } else if (item === 'oncokb-resistance') {
            result = OncoKB.utils.compareIcons('oncokb', x, y, 'asc', 'highestResistanceLevel')
        } else {
            result = OncoKB.utils.compareIcons(item, x, y, 'asc');
        }

        if (result !== 0) {
            return result;
        }
    }

    //Compare cosmicCount
    if (x.mutation && _.isNumber(x.mutation.cosmicCount)) {
        if (y.mutation && _.isNumber(y.mutation.cosmicCount)) {
            return x.mutation.cosmicCount < y.mutation.cosmicCount ? 1 : -1;
        } else {
            return -1;
        }
    } else {
        return 1;
    }

    return -1;
};

$.fn.dataTableExt.oSort['sort-icons-desc'] = function(x, y) {
    var categories = ['oncogenic', 'oncokb-sensitivity', 'oncokb-resistance', 'mycancergenome', 'hotspot'];
    var categoryL = categories.length;

    for (var i = 0; i < categoryL; i++) {
        var item = categories[i];
        var result;

        if (item === 'oncokb-sensitivity') {
            result = OncoKB.utils.compareIcons('oncokb', x, y, 'desc', 'highestSensitiveLevel')
        } else if (item === 'oncokb-resistance') {
            result = OncoKB.utils.compareIcons('oncokb', x, y, 'desc', 'highestResistanceLevel')
        } else {
            result = OncoKB.utils.compareIcons(item, x, y, 'desc');
        }

        if (result !== 0) {
            return result;
        }
    }

    //Compare cosmicCount
    if (x.mutation && _.isNumber(x.mutation.cosmicCount)) {
        if (y.mutation && _.isNumber(y.mutation.cosmicCount)) {
            return x.mutation.cosmicCount < y.mutation.cosmicCount ? -1 : 1;
        } else {
            return 1;
        }
    } else {
        return -1;
    }

    return 1;
};