var log = function (data) {
    console.log(data);
};
dataman = (function () {
    // CLASS DEFS
    var DataFormatter = function () {
        var structureHelper = function (hierarchy, data, injective) {
            var ret = {};
            var key = hierarchy[0];
            if (hierarchy.length === 1) {
                // bottom of tree
                for (var i = 0; i < data.length; i++) {
                    if (injective) {
                        ret[data[i][key]] = data[i];
                    } else {
                        ret[data[i][key]] = ret[data[i][key]] || [];
                        ret[data[i][key]].push(data[i]);
                    }
                }
            } else {
                // haven't reached bottom - keep recursing
                var buckets = {};
                // gather
                for (var i = 0; i < data.length; i++) {
                    buckets[data[i][key]] = buckets[data[i][key]] || [];
                    buckets[data[i][key]].push(data[i]);
                }
                // recurse
                var newhier = hierarchy.slice(1, hierarchy.length);
		$.each(buckets, function(v, _) {
                    ret[v] = structureHelper(newhier, buckets[v], injective);
                });
            }
            return ret;
        };
        var formatHelper = function (template, datum) {
            var ret = {};
            for (var k in template) {
                ret[k] = template[k](datum);
		if (ret[k] === undefined) {
			delete ret[k];
		}
            }
            return ret;
        };
	var format = function (template, data) {
                var ret = [];
                for (var i = 0; i < data.length; i++) {
                    ret.push(formatHelper(template, data[i]));
                }
                return ret;
        };
	var toOncoprintFormat = function(data, geneMap, sampleMap, profileTypes) {
		// geneMap - object, maps entrez gene id to hugo gene symbol
		// sampleMap - object, maps internal sample id to stable sample id
		// profileTypes - object, maps internal genetic profile id to genetic profile type (e.g. 'MUTATION_EXTENDED')
		var cnaType = {'-2': 'HOMODELETED', '-1':'HEMIZYGOUSLYDELETED', '0':'DIPLOID', '1':'GAINED', '2':'AMPLIFIED'};
		var samples = {};
		var genes = {};
		
		var oncoprintTemplate = {
			sample: function(datum) {
				return sampleMap[datum.internal_sample_id];
			},
			gene: function(datum) {
				genes[geneMap[datum.entrez_gene_id]] = true;
				return geneMap[datum.entrez_gene_id];
			},
			mutation: function(datum) {
				if (profileTypes[datum.internal_id] === 'MUTATION_EXTENDED') {
					return datum.amino_acid_change;
				} else {
					return undefined;
				}
			},
			cna: function(datum) {
				if (profileTypes[datum.internal_id] === 'COPY_NUMBER_ALTERATION') {
					return (datum.profile_data === '0' ? undefined : cnaType[datum.profile_data.toString()]);
				} else {
					return undefined;
				}
			},
			mrna: function(datum) {
				if (profileTypes[datum.internal_id] === 'MRNA_EXPRESSION') {
					return datum.profile_data;
				} else {
					return undefined;
				}
			},
			rppa: function(datum) {
				if (profileTypes[datum.internal_id] === 'PROTEIN_ARRAY_PROTEIN_LEVEL') {
					return datum.profile_data;
				} else {
					return undefined;
				}
			}
		};
		var oncoprintDataWithDuplicates = format(oncoprintTemplate, data);
		// merge duplicates
		var oncoDataMap = {}; // sample -> gene -> new datum
		$.each(oncoprintDataWithDuplicates, function(ind, elt) {
			oncoDataMap[elt.sample] = oncoDataMap[elt.sample] || {};
			oncoDataMap[elt.sample][elt.gene] = $.extend(oncoDataMap[elt.sample][elt.gene] || {}, elt);
		});
		var oncoprintData = [];
		$.each(oncoDataMap, function(sample, obj) {
			$.each(obj, function(gene, oncoDatum) {
				oncoprintData.push(oncoDatum);
			});
		});
		// add sample/gene pairs so that each gene has data for each sample
		$.each(oncoprintData, function(ind, elt) {
			samples[elt.sample] = $.extend({}, genes);
		});
		$.each(oncoprintData, function(ind, elt) {
			samples[elt.sample][elt.gene] = false;
		});
		$.each(samples, function(key, val) {
			$.each(val, function(gene, needToAddData) {
				if (needToAddData) {
					oncoprintData.push({sample:key, gene:gene});
				}
			});
		});
		return oncoprintData;
	};
        return {
            format: format,
            addFields: function (template, data) {
                for (var i = 0; i < data.length; i++) {
                    for (var k in template) {
                        data[i][k] = template[k](data[i]);
                    }
                }
                return data;
            },
            structure: function (hierarchy, data, injective) {
                return structureHelper(hierarchy, data, injective);
            },
	    toOncoprintFormat: toOncoprintFormat
        }
    };
    
    var df = new DataFormatter();
    
    function Cache() {
        this.data = [];
        this.indexes = {};
    }
    Cache.prototype = {
        constructor: Cache,
        getAll: function () {
            return this.data;
        },
        addIndex: function (name, cacheBy, mapType) {
            this.indexes[name] = new Index(cacheBy, this.data, mapType);
        },
        addData: function (data, indexNames) {
            this.data = this.data.concat(data);
            indexNames = indexNames || Object.keys(this.indexes);
            for (var i = 0; i < indexNames.length; i++) {
                this.indexes[indexNames[i]].add(data);
            }
        }
    }
    function Index(cacheBy, data, mapType) {
        this.map = {};
        this.cacheBy = cacheBy;
        this.mapType = mapType;
    }
    Index.mapType = {ONE_TO_ONE: 0, ONE_TO_MANY: 1, MANY_TO_ONE: 2, MANY_TO_MANY: 3};
    Index.prototype = {
        constructor: Index,
        add: function (objs) {
            for (var i = 0; i < objs.length; i++) {
                if (this.mapType === Index.mapType.ONE_TO_ONE) {
                    this.map[this.cacheBy(objs[i])] = objs[i];
                } else if (this.mapType === Index.mapType.ONE_TO_MANY) {
                    var key = this.cacheBy(objs[i]);
                    this.map[key] = this.map[key] || [];
                    this.map[key].push(objs[i]);
                } else if (this.mapType === Index.mapType.MANY_TO_ONE) {
                    var keys = this.cacheBy(objs[i]);
                    var currentMap = this.map;
                    for (var j = 0; j < keys.length - 1; j++) {
                        currentMap[keys[j]] = currentMap[keys[j]] || {};
                        currentMap = currentMap[keys[j]];
                    }
                    currentMap[keys[keys.length - 1]] = objs[i];
                } else if (this.mapType === Index.mapType.MANY_TO_MANY) {
                    var keys = this.cacheBy(objs[i]);
                    var currentMap = this.map;
                    for (var j = 0; j < keys.length - 1; j++) {
                        currentMap[keys[j]] = currentMap[keys[j]] || {};
                        currentMap = currentMap[keys[j]];
                    }
                    currentMap[keys[keys.length - 1]] = currentMap[keys[keys.length - 1]] || [];
                    currentMap[keys[keys.length - 1]].push(objs[i]);
                }
            }
        },
        missingKeys: function (keys) {
            var ret = [];
            if (this.mapType === Index.mapType.ONE_TO_ONE || this.mapType === Index.mapType.ONE_TO_MANY) {
                for (var i = 0; i < keys.length; i++) {
                    if (!(keys[i] in this.map)) {
                        ret.push(keys[i]);
                    }
                }
            } else if (this.mapType === Index.mapType.MANY_TO_ONE || this.mapType === Index.mapType.MANY_TO_MANY) {
                for (var i = 0; i < keys.length; i++) {
                    var currentMap = this.map;
                    var key = keys[i];
                    for (var j = 0; j < key.length; j++) {
                        if (!(key[j] in currentMap)) {
                            ret.push(key);
                            break;
                        } else {
                            currentMap = currentMap[key[j]];
                        }
                    }
                }
            }
            return ret;
        },
        get: function (keys) {
            var ret = [];
            if (this.mapType === Index.mapType.ONE_TO_ONE) {
                for (var i = 0; i < keys.length; i++) {
                    if (keys[i] in this.map) {
                        ret.push(this.map[keys[i]]);
                    }
                }
            } else if (this.mapType === Index.mapType.ONE_TO_MANY) {
                for (var i = 0; i < keys.length; i++) {
                    if (keys[i] in this.map) {
                        ret = ret.concat(this.map[keys[i]]);
                    }
                }
            } else if (this.mapType === Index.mapType.MANY_TO_ONE) {
                for (var i = 0; i < keys.length; i++) {
                    var key = keys[i];
                    var currentMap = this.map;
                    for (var j = 0; j < key.length; j++) {
                        if (key[j] in currentMap) {
                            if (j == key.length - 1) {
                                ret.push(currentMap[key[j]]);
                            } else {
                                currentMap = currentMap[key[j]];
                            }
                        } else {
                            break;
                        }
                    }
                }
            } else if (this.mapType === Index.mapType.MANY_TO_MANY) {
                for (var i = 0; i < keys.length; i++) {
                    var key = keys[i];
                    var currentMap = this.map;
                    for (var j = 0; j < key.length; j++) {
                        if (key[j] in currentMap) {
                            if (j == key.length - 1) {
                                ret = ret.concat(currentMap[key[j]]);
                            } else {
                                currentMap = currentMap[key[j]];
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            return ret;
        }
    }

    // CACHE VARIABLE DECL/INIT
    var cbio = cbio_webservice;
    var cache = {meta: {}, data: {}};
    var history = {meta: {}, data: {}};
    var metacalls = ['cancerTypes', 'genes', 'patients', 'samples', 'studies', 'patientLists',
        'profiles', 'clinicalPatients', 'clinicalSamples', 'geneSets'];
    var datacalls = ['clinicalPatients', 'clinicalSamples', 'profiles'];
    for (var i = 0; i < metacalls.length; i++) {
        cache.meta[metacalls[i]] = new Cache();
        history.meta[metacalls[i]] = {};
    }
    for (var i = 0; i < datacalls.length; i++) {
        cache.data[datacalls[i]] = new Cache();
        history.data[datacalls[i]] = {};
    }
    cache.meta.cancerTypes.addIndex('id', function (x) {
        return x.id;
    }, Index.mapType.ONE_TO_ONE);

    cache.meta.genes.addIndex('hugo', function (x) {
        return x.hugoGeneSymbol;
    }, Index.mapType.ONE_TO_ONE);
    cache.meta.genes.addIndex('entrez', function (x) {
        return x.entrezGeneId;
    }, Index.mapType.ONE_TO_ONE);

    cache.meta.patients.addIndex('study', function (x) {
        return x.study_id;
    }, Index.mapType.ONE_TO_MANY);
    cache.meta.patients.addIndex('internal_id', function (x) {
        return x.internal_id;
    }, Index.mapType.ONE_TO_ONE);
    cache.meta.patients.addIndex('stable_id', function (x) {
        return x.study_id + "_" + x.stable_id;
    }, Index.mapType.ONE_TO_ONE);

    cache.meta.samples.addIndex('study', function (x) {
        return x.study_id;
    }, Index.mapType.ONE_TO_MANY);
    cache.meta.samples.addIndex('internal_id', function (x) {
        return x.internal_id;
    }, Index.mapType.ONE_TO_ONE);
    cache.meta.samples.addIndex('stable_id', function (x) {
        return x.study_id + "_" + x.stable_id;
    }, Index.mapType.ONE_TO_ONE);

    cache.meta.studies.addIndex('internal_id', function (x) {
        return x.internal_id;
    }, Index.mapType.ONE_TO_ONE);
    cache.meta.studies.addIndex('stable_id', function (x) {
        return x.id;
    }, Index.mapType.ONE_TO_ONE);

    cache.meta.profiles.addIndex('internal_id', function (x) {
        return x.internal_id;
    }, Index.mapType.ONE_TO_ONE);
    cache.meta.profiles.addIndex('stable_id', function (x) {
        return x.id;
    }, Index.mapType.ONE_TO_ONE);
    cache.meta.profiles.addIndex('study', function (x) {
        return x.internal_study_id;
    }, Index.mapType.ONE_TO_MANY);

    cache.data.clinicalPatients.addIndex('internal_id', function (x) {
        return x.patient_id;
    }, Index.mapType.ONE_TO_MANY);
    cache.data.clinicalPatients.addIndex('study', function (x) {
        return x.study_id;
    }, Index.mapType.ONE_TO_MANY);

    cache.data.clinicalSamples.addIndex('internal_id', function (x) {
        return x.sample_id;
    }, Index.mapType.ONE_TO_MANY);
    cache.data.clinicalSamples.addIndex('study', function (x) {
        return x.study_id;
    }, Index.mapType.ONE_TO_MANY);

    cache.data.profiles.addIndex('geneprofilepatient', function (x) {
        return [x.entrez_gene_id, x.internal_id, x.internal_sample_id]
    }, Index.mapType.MANY_TO_MANY);


    // API METHODS
    // -- meta.cancerTypes --
    var getAllCancerTypes = function (callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        if (history.meta.cancerTypes.all) {
            callback(cache.meta.cancerTypes.getAll());
        } else {
            cbio.meta.cancerTypes({}, function (data) {
                cache.meta.cancerTypes.addData(data);
                history.meta.cancerTypes.all = true;
                callback(cache.meta.cancerTypes.getAll());
            }, fail);
        }
        return dfd.promise();
    }
    var getCancerTypesById = function (ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.cancerTypes.indexes['id'];
        var toQuery = index.missingKeys(ids);
        if (toQuery.length === 0) {
            callback(index.get(ids));
        } else {
            cbio.meta.cancerTypes({'ids': toQuery}, function (data) {
                cache.meta.cancerTypes.addData(data);
                callback(index.get(ids));
            }, fail);
        }
        return dfd.promise();
    }

    // -- meta.genes --
    var getAllGenes = function (callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        if (history.meta.genes.all) {
            callback(cache.meta.genes.getAll());
        } else {
            cbio.meta.genes({}, function (data) {
                cache.meta.genes.addData(data);
                history.meta.genes.all = true;
                callback(cache.meta.genes.getAll());
            }, fail);
        }
        return dfd.promise();
    }
    var getGenesHelper = function (ids, indexName, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.genes.indexes[indexName];
        var toQuery = index.missingKeys(ids);
        if (toQuery.length === 0) {
            callback(index.get(ids));
        } else {
            cbio.meta.genes({'ids': toQuery}, function (data) {
                cache.meta.genes.addData(data);
                callback(index.get(ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getGenesByHugoGeneSymbol = function (ids, callback, fail) {
        return getGenesHelper(ids, 'hugo', callback, fail);
    }
    var getGenesByEntrezGeneId = function (ids, callback, fail) {
        return getGenesHelper(ids, 'entrez', callback, fail);
    }

    // -- meta.patients --
    var getPatientsByStableStudyId = function (study_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.studies({'ids': study_ids}, function (data) {
            var internal_ids = [];
            for (var i = 0; i < data.length; i++) {
                internal_ids.push(data[i].internal_id);
            }
            getPatientsByInternalStudyId(internal_ids, callback, fail);
        }, fail);
        return dfd.promise();
    }
    var getPatientsByInternalStudyId = function (study_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.patients.indexes['study'];
        var toQuery = index.missingKeys(study_ids);
        if (toQuery.length === 0) {
            callback(index.get(study_ids));
        } else {
            cbio.meta.patients({'study_ids': study_ids}, function (data) {
                cache.meta.patients.addData(data);
                callback(index.get(study_ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getPatientsByInternalId = function (internal_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.patients.indexes['internal_id'];
        var toQuery = index.missingKeys(internal_ids);
        if (toQuery.length === 0) {
            callback(index.get(internal_ids));
        } else {
            cbio.meta.patients({'patient_ids': internal_ids}, function (data) {
                cache.meta.patients.addData(data, ['internal_id', 'stable_id']);
                callback(index.get(internal_ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getPatientsByStableIdStableStudyId = function (study_id, stable_ids, callback, fail) {
	var dfd = new $.Deferred();
        cbio.meta.studies({'ids': [study_id]}, function (data) {
            getPatientsByStableIdInternalStudyId(data[0].internal_id, stable_ids, callback, fail).then(function(data) {
		    dfd.resolve(data);
	    });
        }, fail);
	return dfd.promise();
    }
    var getPatientsByStableIdInternalStudyId = function (study_id, stable_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.patients.indexes['stable_id'];
        var goodIds = stable_ids.map(function (x) {
            return study_id + "_" + x;
        });
        var toQuery = index.missingKeys(goodIds)
        if (toQuery.length === 0) {
            callback(index.get(goodIds));
        } else {
            cbio.meta.patients({'study_ids': [study_id], 'patient_ids': stable_ids}, function (data) {
                cache.meta.patients.addData(data, ['internal_id', 'stable_id']);
                callback(index.get(goodIds));
            }, fail);
        }
        return dfd.promise();
    }

    // -- meta.samples --
    var getSamplesByStableStudyId = function (study_ids, callback, fail) {
	var dfd = new $.Deferred();
        getStudiesByStableId(study_ids, function (data) {
            var internal_ids = [];
            for (var i = 0; i < data.length; i++) {
                internal_ids.push(data[i].internal_id);
            }
            getSamplesByInternalStudyId(internal_ids, callback, fail).then(function(data) {
		    dfd.resolve(data);
	    });
        }, fail);
	return dfd.promise();
    }
    var getSamplesByInternalStudyId = function (study_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.samples.indexes['study'];
        var toQuery = index.missingKeys(study_ids);
        if (toQuery.length === 0) {
            callback(index.get(study_ids));
        } else {
            cbio.meta.samples({'study_ids': study_ids}, function (data) {
                cache.meta.samples.addData(data);
                callback(index.get(study_ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getSamplesByInternalId = function (internal_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.samples.indexes['internal_id'];
        var toQuery = index.missingKeys(internal_ids);
        if (toQuery.length === 0) {
            callback(index.get(internal_ids));
        } else {
            cbio.meta.samples({'sample_ids': internal_ids}, function (data) {
                cache.meta.samples.addData(data, ['internal_id', 'stable_id']);
                callback(index.get(internal_ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getSamplesByStableIdStableStudyId = function (study_id, stable_ids, callback, fail) {
	var dfd = new $.Deferred();
        getStudiesByStableId([study_id], function (data) {
            getSamplesByStableIdInternalStudyId(data[0].internal_id, stable_ids, callback, fail).then(function(data) {
		    dfd.resolve(data);
	    });
        }, fail);
	return dfd.promise();
    }
    var getSamplesByStableIdInternalStudyId = function (study_id, stable_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.samples.indexes['stable_id'];
        var goodIds = stable_ids.map(function (x) {
            return study_id + "_" + x;
        });
        var toQuery = index.missingKeys(goodIds)
        if (toQuery.length === 0) {
            callback(index.get(goodIds));
        } else {
            cbio.meta.samples({'study_ids': [study_id], 'sample_ids': stable_ids}, function (data) {
                cache.meta.samples.addData(data, ['internal_id', 'stable_id']);
                callback(index.get(goodIds));
            }, fail);
        }
        return dfd.promise();
    }

    // -- meta.studies --
    var getAllStudies = function (callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        if (history.meta.studies.all) {
            callback(cache.meta.studies.getAll());
        } else {
            cbio.meta.studies({}, function (data) {
                cache.meta.studies.addData(data);
                history.meta.studies.all = true;
                callback(cache.meta.studies.getAll());
            }, fail);
        }
        return dfd.promise();
    }

    var getStudiesHelper = function (ids, indexName, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.studies.indexes[indexName];
        var toQuery = index.missingKeys(ids);
        if (toQuery.length === 0) {
            callback(index.get(ids));
        } else {
            cbio.meta.studies({'ids': toQuery}, function (data) {
                cache.meta.studies.addData(data);
                callback(index.get(ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getStudiesByStableId = function (ids, callback, fail) {
        return getStudiesHelper(ids, 'stable_id', callback, fail);
    }
    var getStudiesByInternalId = function (ids, callback, fail) {
        return getStudiesHelper(ids, 'internal_id', callback, fail);
    }


    // -- meta.geneSets --
    //TODO: caching
    var getAllGeneSets = function (omit_lists, callback, fail) {
        cbio.meta.geneSets({'omit_lists': omit_lists}, callback, fail);
    }
    var getGeneSetsById = function (ids, omit_lists, callback, fail) {
        cbio.meta.geneSets({'ids': ids, 'omit_lists': omit_lists}, callback, fail);
    }

    // -- meta.patientLists --
    //TODO: caching
    var getAllPatientLists = function (omit_lists, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.patientLists({'omit_lists': omit_lists}, callback, fail);
        return dfd.promise();
    }
    var getPatientListsByStableId = function (omit_lists, patient_list_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.patientLists({'omit_lists': omit_lists, 'patient_list_ids': patient_list_ids}, callback, fail);
        return dfd.promise();
    }
    var getPatientListsByInternalId = function (omit_lists, patient_list_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.patientLists({'omit_lists': omit_lists, 'patient_list_ids': patient_list_ids}, callback, fail);
        return dfd.promise();
    }
    var getPatientListsByStudy = function (omit_lists, study_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.patientLists({'omit_lists': omit_lists, 'study_ids': study_ids}, callback, fail);
        return dfd.promise();
    }

    // -- meta.profiles --
    var getAllProfiles = function (callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        if (history.meta.profiles.all) {
            callback(cache.meta.profiles.getAll());
        } else {
            cbio.meta.profiles({}, function (data) {
                cache.meta.profiles.addData(data);
                history.meta.profiles.all = true;
                callback(cache.meta.studies.getAll());
            }, fail);
        }
        return dfd.promise();
    }
    var getProfilesHelper = function (argname, ids, indexName, updateIndexes, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.meta.profiles.indexes[indexName];
        var toQuery = index.missingKeys(ids);
        if (toQuery.length === 0) {
            callback(index.get(ids));
        } else {
            var args = {};
            args[argname] = ids;
            cbio.meta.profiles(args, function (data) {
                cache.meta.profiles.addData(data, updateIndexes);
                callback(index.get(ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getProfilesByStableId = function (profile_ids, callback, fail) {
        return getProfilesHelper('profile_ids', profile_ids, 'stable_id', ['stable_id', 'internal_id'], callback, fail);
    }
    var getProfilesByInternalId = function (profile_ids, callback, fail) {
        return getProfilesHelper('profile_ids', profile_ids, 'internal_id', ['stable_id', 'internal_id'], callback, fail);
    }
    var getProfilesByInternalStudyId = function (study_ids, callback, fail) {
        return getProfilesHelper('study_ids', study_ids, 'study', undefined, callback, fail);
    }

    // -- meta.clinicalPatients --
    var getAllClinicalPatientFields = function (callback, fail) {
        //TODO?: caching
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.clinicalPatientsMeta({}, callback, fail);
        return dfd.promise();
    }
    var getClinicalPatientFieldsByStudy = function (study_ids, callback, fail) {
        //TODO? caching
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.clinicalPatientsMeta({'study_ids': study_ids}, callback, fail);
        return dfd.promise();
    }
    var getClinicalPatientFieldsByInternalId = function (patient_ids, callback, fail) {
        //TODO? caching
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.clinicalPatientsMeta({'patient_ids': patient_ids}, callback, fail);
        return dfd.promise();
    }
    var getClinicalPatientFieldsByStableId = function (study_id, patient_ids, callback, fail) {
        //TODO? caching
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.clinicalPatientsMeta({'study_ids': [study_id], 'patient_ids': patient_ids}, callback, fail);
        return dfd.promise();
    }

    // -- meta.clinicalSamples --
    var getAllClinicalSampleFields = function (callback, fail) {
        //TODO?: caching
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.clinicalSamplesMeta({}, callback, fail);
        return dfd.promise();
    }
    var getClinicalSampleFieldsByStudy = function (study_ids, callback, fail) {
        //TODO? caching
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.clinicalSamplesMeta({'study_ids': study_ids}, callback, fail);
        return dfd.promise();
    }
    var getClinicalSampleFieldsByInternalId = function (sample_ids, callback, fail) {
        //TODO? caching
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.clinicalSamplesMeta({'patient_ids': sample_ids}, callback, fail);
        return dfd.promise();
    }
    var getClinicalSampleFieldsByStableId = function (study_id, sample_ids, callback, fail) {
        //TODO? caching
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        cbio.meta.clinicalSamplesMeta({'study_ids': [study_id], 'sample_ids': sample_ids}, callback, fail);
        return dfd.promise();
    }

    // -- data.clinicalPatients --
    var getClinicalPatientDataByInternalStudyId = function (study_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.data.clinicalPatients.indexes['study'];
        var toQuery = index.missingKeys(study_ids);
        if (toQuery.length === 0) {
            callback(index.get(study_ids));
        } else {
            cbio.data.clinicalPatients({'study_ids': toQuery}, function (data) {
                cache.data.clinicalPatients.addData(data);
                callback(index.get(study_ids));
            }, fail)
        }
        return dfd.promise();
    }
    var getClinicalPatientDataByStableStudyId = function (study_ids, callback, fail) {
        getStudiesByStableId(study_ids, function (data) {
            var internal_ids = [];
            for (var i = 0; i < data.length; i++) {
                internal_ids.push(data[i].internal_id);
            }
            getClinicalPatientDataByInternalStudyId(internal_ids, callback, fail);
        }, fail);
    }
    var getClinicalPatientDataByInternalId = function (patient_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.data.clinicalPatients.indexes['internal_id'];
        var toQuery = index.missingKeys(patient_ids);
        if (toQuery.length === 0) {
            callback(index.get(study_ids));
        } else {
            cbio.data.clinicalPatients({'patient_ids': toQuery}, function (data) {
                cache.data.clinicalPatients.addData(data, ['internal_id']);
                callback(index.get(patient_ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getClinicalPatientDataByStableId = function (study_id, patient_ids, callback, fail) {
        var fetchFn = (typeof study_id === "number" ? getPatientsByStableIdInternalStudyId : getPatientsByStableIdStableStudyId);
        fetchFn(study_id, patient_ids, function (data) {
            var internal_ids = [];
            for (var i = 0; i < data.length; i++) {
                internal_ids.push(data[i].internal_id);
            }
            getClinicalPatientDataByInternalId(internal_ids, callback, fail);
        }, fail);
    }

    // -- data.clinicalSamples --
    var getClinicalSampleDataByInternalStudyId = function (study_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.data.clinicalSamples.indexes['study'];
        var toQuery = index.missingKeys(study_ids);
        if (toQuery.length === 0) {
            callback(index.get(study_ids));
        } else {
            cbio.data.clinicalSamples({'study_ids': toQuery}, function (data) {
                cache.data.clinicalSamples.addData(data);
                callback(index.get(study_ids));
            }, fail)
        }
        return dfd.promise();
    }
    var getClinicalSampleDataByStableStudyId = function (study_ids, callback, fail) {
        getStudiesByStableId(study_ids, function (data) {
            var internal_ids = [];
            for (var i = 0; i < data.length; i++) {
                internal_ids.push(data[i].internal_id);
            }
            getClinicalSampleDataByInternalStudyId(internal_ids, callback, fail);
        }, fail);
    }
    var getClinicalSampleDataByInternalId = function (sample_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var index = cache.data.clinicalSamples.indexes['internal_id'];
        var toQuery = index.missingKeys(sample_ids);
        if (toQuery.length === 0) {
            callback(index.get(study_ids));
        } else {
            cbio.data.clinicalSamples({'sample_ids': toQuery}, function (data) {
                cache.data.clinicalSamples.addData(data, ['internal_id']);
                callback(index.get(sample_ids));
            }, fail);
        }
        return dfd.promise();
    }
    var getClinicalSampleDataByStableId = function (study_id, sample_ids, callback, fail) {
        var fetchFn = (typeof study_id === "number" ? getSamplesByStableIdInternalStudyId : getSamplesByStableIdStableStudyId);
        fetchFn(study_id, sample_ids, function (data) {
            var internal_ids = [];
            for (var i = 0; i < data.length; i++) {
                internal_ids.push(data[i].internal_id);
            }
            getClinicalSampleDataByInternalId(internal_ids, callback, fail);
        }, fail);
    }

    // -- data.profiles --
    var cartProd2 = function (A, B) {
        var ret = [];
        for (var i = 0; i < A.length; i++) {
            for (var j = 0; j < B.length; j++) {
                ret.push([A[i], B[j]]);
            }
        }
        return ret;
    }
    var cartProd3 = function (A, B, C) {
        var ret = [];
        for (var i = 0; i < A.length; i++) {
            for (var j = 0; j < B.length; j++) {
                for (var h = 0; h < C.length; h++) {
                    ret.push([A[i], B[j], C[h]]);
                }
            }
        }
        return ret;
    }
    //helper function
    // in: output of cartProd on genes and profile_ids
    var getDataFromCache = function (gpcartprod) {
        var ret = [];
        var map = cache.data.profiles.indexes['geneprofilepatient'].map;
        for (var i = 0; i < gpcartprod.length; i++) {
            if (!(gpcartprod[i][0] in map)) {
                continue;
            } else if (!(gpcartprod[i][1] in map[gpcartprod[i][0]])) {
                continue;
            } else {
                for (var pat in map[gpcartprod[i][0]][gpcartprod[i][1]]) {
                    ret = ret.concat(map[gpcartprod[i][0]][gpcartprod[i][1]][pat]);
                }
            }
        }
        return ret;
    }
    var getAllProfileData = function (genes, profile_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var allCombs = cartProd2(genes, profile_ids);
        var toQuery = {};
        var index = cache.data.profiles.indexes['geneprofilepatient'];
        for (var i = 0; i < allCombs.length; i++) {
            var comb = allCombs[i];
            if (!(comb[0] in history.data.profiles) || !(comb[1] in history.data.profiles[comb[0]])) {
                toQuery[comb[0]] = toQuery[comb[0]] || {};
                toQuery[comb[0]][comb[1]] = true;
            }
        }
        var callsWaiting = Object.keys(toQuery).length;
        if (callsWaiting === 0) {
            callback(getDataFromCache(allCombs));
        } else {
            for (var gene in toQuery) {
                (function (g) {
                    cbio.data.profiles({'genes': [g], 'profile_ids': [Object.keys(toQuery[g])]}, function (data) {
                        cache.data.profiles.addData(data);
                        for (var prof in toQuery[g]) {
                            history.data.profiles[g] = history.data.profiles[g] || {};
                            history.data.profiles[g][prof] = true;
                        }
                        callsWaiting -= 1;
                        if (callsWaiting === 0) {
                            callback(getDataFromCache(allCombs));
                        }
                    }, fail)
                })(gene);
            }
        }
        return dfd.promise();
    }
    var getProfileDataBySampleId = function (genes, profile_ids, sample_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        var allCombs = cartProd3(genes, profile_ids, sample_ids);
        var index = cache.data.profiles.indexes['geneprofilepatient'];
        var toQuery = index.missingKeys(allCombs);
        var queryMap = {}; // what we'll query for
        var newDataMap = {}; // the data we actually want
        for (var i = 0; i < toQuery.length; i++) {
            var gene = toQuery[i][0];
            var profile = toQuery[i][1];
            var patient = toQuery[i][2];
            queryMap[gene] = queryMap[gene] || {profiles: {}, patients: {}};
            queryMap[gene].profiles[profile] = true;
            queryMap[gene].patients[patient] = true;
            newDataMap[gene] = newDataMap[gene] || {};
            newDataMap[gene][profile] = newDataMap[gene][profile] || {};
            newDataMap[gene][profile][patient] = true;
        }
        var callsWaiting = Object.keys(queryMap).length;
        if (callsWaiting === 0) {
            callback(index.get(allCombs));
        } else {
            for (var gene in queryMap) {
                (function (g) {
                    cbio.data.profiles({'genes': [g], 'profile_ids': Object.keys(queryMap[g].profiles), 'sample_ids': Object.keys(queryMap[g].patients)}, function (data) {
                        var newData = [];
                        for (var i = 0; i < data.length; i++) {
                            var gene = data[i].entrez_gene_id;
                            var profile = data[i].internal_id;
                            var patient = data[i].internal_sample_id;
                            if ((gene in newDataMap) && (profile in newDataMap[gene]) && (patient in newDataMap[gene][profile])) {
                                newData.push(data[i]);
                            }
                        }
                        cache.data.profiles.addData(newData);
                        callsWaiting -= 1;
                        if (callsWaiting === 0) {
                            callback(index.get(allCombs));
                        }
                    }, fail);
                })(gene);
            }
        }
        return dfd.promise();
    }
    var getProfileDataByPatientListId = function (genes, profile_ids, patient_list_ids, callback, fail) {
        var dfd = new $.Deferred();
        callback = callback || function(x) { dfd.resolve(x); };
        //TODO: caching
        cbio.data.profilesData({'genes': genes, 'profile_ids': profile_ids, 'patient_list_ids': patient_list_ids}, callback, fail);
        return dfd.promise();
    }
    return {
        cache: cache,
        df: df,
        getAllCancerTypes: getAllCancerTypes,
        getCancerTypesById: getCancerTypesById,
        getAllGenes: getAllGenes,
        getGenesByHugoGeneSymbol: getGenesByHugoGeneSymbol,
        getGenesByEntrezGeneId: getGenesByEntrezGeneId,
        getPatientsByStableStudyId: getPatientsByStableStudyId,
        getPatientsByInternalStudyId: getPatientsByInternalStudyId,
        getPatientsByInternalId: getPatientsByInternalId,
        getPatientsByStableIdStableStudyId: getPatientsByStableIdStableStudyId,
        getPatientsByStableIdInternalStudyId: getPatientsByStableIdInternalStudyId,
        getSamplesByStableStudyId: getSamplesByStableStudyId,
        getSamplesByInternalStudyId: getSamplesByInternalStudyId,
        getSamplesByInternalId: getSamplesByInternalId,
        getSamplesByStableIdStableStudyId: getSamplesByStableIdStableStudyId,
        getSamplesByStableIdInternalStudyId: getSamplesByStableIdInternalStudyId,
        getAllStudies: getAllStudies,
        getStudiesByStableId: getStudiesByStableId,
        getStudiesByInternalId: getStudiesByInternalId,
        getAllPatientLists: getAllPatientLists,
        getPatientListsByStableId: getPatientListsByStableId,
        getPatientListsByInternalId: getPatientListsByInternalId,
        getPatientListsByStudy: getPatientListsByStudy,
        getAllGeneSets: getAllGeneSets,
        getGeneSetsById: getGeneSetsById,
        getAllProfiles: getAllProfiles,
        getProfilesByStableId: getProfilesByStableId,
        getProfilesByInternalId: getProfilesByInternalId,
        getProfilesByInternalStudyId: getProfilesByInternalStudyId,
        getAllClinicalPatientFields: getAllClinicalPatientFields,
        getClinicalPatientFieldsByStudy: getClinicalPatientFieldsByStudy,
        getClinicalPatientFieldsByInternalId: getClinicalPatientFieldsByInternalId,
        getClinicalPatientFieldsByStableId: getClinicalPatientFieldsByStableId,
        getAllClinicalSampleFields: getAllClinicalSampleFields,
        getClinicalSampleFieldsByStudy: getClinicalSampleFieldsByStudy,
        getClinicalSampleFieldsByInternalId: getClinicalSampleFieldsByInternalId,
        getClinicalSampleFieldsByStableId: getClinicalSampleFieldsByStableId,
        getAllProfileData: getAllProfileData,
        getProfileDataBySampleId: getProfileDataBySampleId,
        getProfileDataByPatientListId: getProfileDataByPatientListId,
        getClinicalPatientDataByInternalStudyId: getClinicalPatientDataByInternalStudyId,
        getClinicalPatientDataByStableStudyId: getClinicalPatientDataByStableStudyId,
        getClinicalPatientDataByInternalId: getClinicalPatientDataByInternalId,
        getClinicalPatientDataByStableId: getClinicalPatientDataByStableId,
        getClinicalSampleDataByInternalStudyId: getClinicalSampleDataByInternalStudyId,
        getClinicalSampleDataByStableStudyId: getClinicalSampleDataByStableStudyId,
        getClinicalSampleDataByInternalId: getClinicalSampleDataByInternalId,
        getClinicalSampleDataByStableId: getClinicalSampleDataByStableId,
	
    };

})();