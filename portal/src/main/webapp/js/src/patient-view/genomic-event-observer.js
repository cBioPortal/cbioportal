
function GenomicEventObserver(hasMut, hasCna, hasSeg) {
    this.fns_mut_cna = [];
    this.fns_mut = [];
    this.fns_cna = [];
    this.fns_pancan_mutation_frequency = [];
    this.pancan_mutation_frequencies = new PancanMutationFrequencies;   // keyword or hugo -> datum { cancer_study, cancer_type, count, hugo, [keyword] }
    this.hasMut = hasMut;
    this.hasCna = hasCna;
    this.hasSeg = hasSeg;
    this.mutBuilt = false;
    this.cnaBuilt = false;
    this.pancanFreqBuilt = false;
    this.oncoKBuilt = false;
    this.mutations = new GenomicEventContainer;
    this.cnas = new GenomicEventContainer;
}
GenomicEventObserver.prototype = {
    subscribeMutCna : function(fn) {
        this.fns_mut_cna.push(fn);
        if (this.hasMut===this.mutBuilt && this.hasCna===this.cnaBuilt) {
            fn.call(window);
        }
    },
    subscribeMut : function(fn) {
        this.fns_mut.push(fn);
        if (this.mutBuilt) {
            fn.call(window);
        }
    },
    subscribeCna : function(fn) {
        this.fns_cna.push(fn);
        if (this.pancanFreqBuilt) {
            fn.call(window);
        }
    },
    subscribePancanMutationsFrequency : function(fn) {
        this.fns_pancan_mutation_frequency.push(fn);
        if (this.pancanFreqBuilt) {
            fn.call(window);
        }
    },
    fire : function(o, thisObj) {
        var scope = thisObj || window;

        if (o==="mutations-built") {
            this.mutBuilt = true;
            this.fns_mut.forEach(
                function(el) {
                    el.call(scope);
                }
            );
        }

        else if (o==="cna-built") {
            this.cnaBuilt = true;
            this.fns_cna.forEach(
                function(el) {
                    el.call(scope);
                }
            );
        }

        else if (o==="pancan-mutation-frequency-built") {
            this.pancanFreqBuilt = true;
            this.fns_pancan_mutation_frequency.forEach(
                function(el) {
                    el.call(scope);
                }
            );
        }

        if (this.hasMut===this.mutBuilt && this.hasCna===this.cnaBuilt) {
            this.fns_mut_cna.forEach(
                function(el) {
                    el.call(scope);
                }
            );
        }
    }
};

function PancanMutationFrequencies() {
    this.data = null;
}
PancanMutationFrequencies.prototype = {
    setData: function(data) {
        this.data = data;
    },
    countByKey : function(key) {
        var byHugoData = this.data[key];

        var total_mutation_count = _.reduce(byHugoData, function(acc, next) {
            return acc + next.count;
        }, 0);

        return total_mutation_count;
    }
};

function GenomicEventContainer() {
    this.data = null;
    this.numEvents = 0;
    this.eventIdMap = {};
    this.overviewEventIds = [];
    this.overviewEventGenes = [];
}
GenomicEventContainer.prototype = {
    setData: function(data) {
        this.data = data;
        var ids = data['id'];
        var genes = data['gene'];
        this.numEvents = ids.length;
        for (var i=0; i<this.numEvents; i++) {
            this.eventIdMap[ids[i]] = i;
            if (this.data['overview'][i]) {
                this.overviewEventIds.push(ids[i]);
                this.overviewEventGenes.push(genes[i]);
            }
        }
    },
    addData: function(name, newData) {
        this.data[name] = newData;
    },
    addDataMap: function(name, dataMap, key) {
        var newData = [];
        var keyColValue = this.data[key];
        for (var i=0; i<this.numEvents; i++) {
            newData.push(dataMap[keyColValue[i]]);
        }
        this.data[name] = newData;
    },
    getEventIds: function(overview) {
        return overview ? this.overviewEventIds : this.data['id'];
    },
    getNumEvents: function(overview) {
        return overview ? this.overviewEventIds.length : this.numEvents;
    },
    getValue: function(eventId,colName) {
        return this.data[colName][this.eventIdMap[eventId]];
    },
    colExists: function(colName) {
        return this.data[colName]!=null;
    },
    colAllNull: function(colName,nullValue) {
        for (var i=0; i<this.numEvents; i++) {
            if (typeof nullValue === 'undefined') {
                if ((typeof this.data[colName][i]!=="undefined")
                    && this.data[colName][i]!==null) return false;
            } else {
                if (this.data[colName][i]!==nullValue) return false;
            }
        }
        return true;
    },
    getDrugs: function() {
        var drugs = {
            ids: [],
            genes: []
        };

        for (var i=0; i<this.numEvents; i++) {
            var drugSet = this.data['drug'][i];
            var gene = this.data['gene'][i];

            if (drugSet != null) {
                for(var j=0; j < drugSet.length; j++) {
                    var aDrug = drugSet[j];
                    drugs.ids.push(aDrug);
                    drugs.genes.push(gene);
                }
            }
        }

        return drugs;
    }

}