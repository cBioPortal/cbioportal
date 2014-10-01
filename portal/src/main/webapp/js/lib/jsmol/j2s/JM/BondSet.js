Clazz.declarePackage ("JM");
Clazz.load (["JU.BS"], "JM.BondSet", ["JU.BSUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.associatedAtoms = null;
Clazz.instantialize (this, arguments);
}, JM, "BondSet", JU.BS);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, JM.BondSet, []);
});
Clazz.defineMethod (c$, "getAssociatedAtoms", 
function () {
return this.associatedAtoms;
});
Clazz.makeConstructor (c$, 
function (bs) {
Clazz.superConstructor (this, JM.BondSet, []);
JU.BSUtil.copy2 (bs, this);
}, "JU.BS");
Clazz.makeConstructor (c$, 
function (bs, atoms) {
this.construct (bs);
this.associatedAtoms = atoms;
}, "JU.BS,~A");
});
