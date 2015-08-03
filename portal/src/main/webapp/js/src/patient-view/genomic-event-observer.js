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