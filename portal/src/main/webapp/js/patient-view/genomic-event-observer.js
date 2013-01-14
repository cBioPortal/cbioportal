
function GenomicEventObserver(hasMut, hasCna, hasSeg) {
    this.fns_mut_cna = [];
    this.fns_mut = [];
    this.fns_cna = [];
    this.hasMut = hasMut;
    this.hasCna = hasCna;
    this.hasSeg = hasSeg;
    this.mutBuilt = false;
    this.cnaBuilt = false;
    this.mutations = new GenomicEventContainer;
    this.cnas = new GenomicEventContainer;
}
GenomicEventObserver.prototype = {
    subscribeMutCna : function(fn) {
        this.fns_mut_cna.push(fn);
    },
    subscribeMut : function(fn) {
        this.fns_mut.push(fn);
    },
    subscribeCna : function(fn) {
        this.fns_cna.push(fn);
    },
    fire : function(o, thisObj) {
        var scope = thisObj || window;

        if (o=="mutations-built") {
            this.mutBuilt = true;
            this.fns_mut.forEach(
                function(el) {
                    el.call(scope);
                }
            );
        } else if (o=="cna-built") {
            this.cnaBuilt = true;
            this.fns_cna.forEach(
                function(el) {
                    el.call(scope);
                }
            );
        }

        if (this.hasMut==this.mutBuilt && this.hasCna==this.cnaBuilt) {
            this.fns_mut_cna.forEach(
                function(el) {
                    el.call(scope);
                }
            );
        }
    }
};

/**
 * Copyright (c) Mozilla Foundation http://www.mozilla.org/
 * This code is available under the terms of the MIT License
 */
if (!Array.prototype.forEach) {
    Array.prototype.forEach = function(fun /*, thisp*/) {
        var len = this.length >>> 0;
        if (typeof fun != "function") {
            throw new TypeError();
        }

        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
            if (i in this) {
                fun.call(thisp, this[i], i, this);
            }
        }
    };
}

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
    colAllNull: function(colName) {
        for (var i=0; i<this.numEvents; i++) {
            if (this.data[colName][i]!=null) return false;
        }
        return true;
    },
    getDrugIDs: function() {
        var drugs = [];
        for (var i=0; i<this.numEvents; i++) {
            if (this.data['drug'][i]!=null) drugs.push(this.data[drug][i]);
        }

        return drugs;
    }
}