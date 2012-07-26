
function GenomicEventObserver(hasMut, hasCna) {
    this.fns_mut_cna = [];
    this.fns_mut = [];
    this.fns_cna = [];
    this.hasMut = hasMut;
    this.hasCna = hasCna;
    this.mutBuilt = false;
    this.cnaBuilt = false;
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