
function CivicService() {
    
    var service = {
        
        getCivicGenes: function(geneSymbols) {
            var ids = geneSymbols.join(',');
            return $.ajax({
                type: 'GET',
                url: 'api-legacy/proxy/civic/genes/' + ids,
                dataType: 'json',
                contentType: 'application/json',
                data: {
                    identifier_type: 'entrez_symbol'
                }
            }).then(function(result) {
                if (_.isString(result)) {
                    result = $.parseJSON(result);
                }

                var civicGenes = {};
                // Wrap the result in an Array if it is not already one
                if (!(result instanceof Array)) {
                    result = [result];
                } 
                if (result) {
                    result.forEach(function(record) {
                        var civicGene = {
                            id: record.id,
                            name: record.name,
                            description: record.description,
                            url: 'https://civic.genome.wustl.edu/#/events/genes/'
                            + record.id + '/summary',
                            variants: {}
                        };
                        civicGenes[record.name] = civicGene;
                        if (record.variants && record.variants.length > 0) {
                            var variants = civicGene.variants;
                            record.variants.forEach(function(variant) {
                                variants[variant.name] = {
                                    id: variant.id,
                                    name: variant.name,
                                    geneId: civicGene.id
                                };
                            });
                        }
                    });
                }
                return civicGenes;
            });
        },
        
        getMatchingCivicVariants: function(civicVariants, proteinChange) {
            var matchingCivicVariants = [];
            
            // If present, add the exact match first
            var civicVariant = civicVariants[proteinChange];
            if (typeof civicVariant !== 'undefined') {
                matchingCivicVariants.push(civicVariant);
            }

            // Match any other variants after splitting the name on + or /
            $.each(civicVariants, function(name, civicVariant) {
                var split = name.split(/[+\/]/);
                if (split.length > 1 && split.indexOf(proteinChange) >= 0) {
                    matchingCivicVariants.push(civicVariant);
                }
            });
            
            return matchingCivicVariants;
        },
        
        getCivicVariant: function(civicVariant) {
            var deferred = $.Deferred();

            if (civicVariant.hasOwnProperty('description')) {
                // Variant info has already been loaded
                deferred.resolve();
            }
            else {
                $.ajax({
                        type: 'GET',
                        url: 'api-legacy/proxy/civic/variants/' + civicVariant.id,
                        dataType: 'json',
                        contentType: 'application/json'
                    })
                    .done(function (result) {
                        if (_.isString(result)) {
                            result = $.parseJSON(result);
                        }
                        civicVariant.description = result.description;

                        civicVariant.url = 'https://civic.genome.wustl.edu/#/events/genes/' + civicVariant.geneId +
                            '/summary/variants/' + civicVariant.id + '/summary#variant';

                        // Aggregate evidence items per type
                        civicVariant.evidence = {};
                        var evidence = civicVariant.evidence;
                        result.evidence_items.forEach(function (evidenceItem) {
                            var evidenceType = evidenceItem.evidence_type;
                            if (evidence.hasOwnProperty(evidenceType)) {
                                evidence[evidenceType] += 1;
                            }
                            else {
                                evidence[evidenceType] = 1;
                            }
                        });
                        deferred.resolve();
                    })
                    .fail(function () {
                        deferred.reject();
                    });
            }

            return deferred.promise();
        }
    };

    return service;
}
