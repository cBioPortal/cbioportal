Clazz.declarePackage ("J.adapter.smarter");
Clazz.load (["J.api.JmolAdapterAtomIterator"], "J.adapter.smarter.AtomIterator", ["java.lang.Float", "J.api.JmolAdapter"], function () {
c$ = Clazz.decorateAsClass (function () {
this.iatom = 0;
this.atom = null;
this.atomCount = 0;
this.atoms = null;
this.bsAtoms = null;
Clazz.instantialize (this, arguments);
}, J.adapter.smarter, "AtomIterator", J.api.JmolAdapterAtomIterator);
Clazz.makeConstructor (c$, 
function (atomSetCollection) {
Clazz.superConstructor (this, J.adapter.smarter.AtomIterator, []);
this.atomCount = atomSetCollection.getAtomCount ();
this.atoms = atomSetCollection.getAtoms ();
this.bsAtoms = atomSetCollection.bsAtoms;
this.iatom = 0;
}, "J.adapter.smarter.AtomSetCollection");
Clazz.overrideMethod (c$, "hasNext", 
function () {
if (this.iatom == this.atomCount) return false;
while ((this.atom = this.atoms[this.iatom++]) == null || (this.bsAtoms != null && !this.bsAtoms.get (this.atom.atomIndex))) if (this.iatom == this.atomCount) return false;

this.atoms[this.iatom - 1] = null;
return true;
});
Clazz.overrideMethod (c$, "getAtomSetIndex", 
function () {
return this.atom.atomSetIndex;
});
Clazz.overrideMethod (c$, "getAtomSymmetry", 
function () {
return this.atom.bsSymmetry;
});
Clazz.overrideMethod (c$, "getAtomSite", 
function () {
return this.atom.atomSite + 1;
});
Clazz.overrideMethod (c$, "getUniqueID", 
function () {
return Integer.$valueOf (this.atom.atomIndex);
});
Clazz.overrideMethod (c$, "getElementNumber", 
function () {
return (this.atom.elementNumber > 0 ? this.atom.elementNumber : J.api.JmolAdapter.getElementNumber (this.atom.getElementSymbol ()));
});
Clazz.overrideMethod (c$, "getAtomName", 
function () {
return this.atom.atomName;
});
Clazz.overrideMethod (c$, "getFormalCharge", 
function () {
return this.atom.formalCharge;
});
Clazz.overrideMethod (c$, "getPartialCharge", 
function () {
return this.atom.partialCharge;
});
Clazz.overrideMethod (c$, "getEllipsoid", 
function () {
return this.atom.ellipsoid;
});
Clazz.overrideMethod (c$, "getRadius", 
function () {
return this.atom.radius;
});
Clazz.overrideMethod (c$, "getX", 
function () {
return this.atom.x;
});
Clazz.overrideMethod (c$, "getY", 
function () {
return this.atom.y;
});
Clazz.overrideMethod (c$, "getZ", 
function () {
return this.atom.z;
});
Clazz.overrideMethod (c$, "getVectorX", 
function () {
return this.atom.vectorX;
});
Clazz.overrideMethod (c$, "getVectorY", 
function () {
return this.atom.vectorY;
});
Clazz.overrideMethod (c$, "getVectorZ", 
function () {
return this.atom.vectorZ;
});
Clazz.overrideMethod (c$, "getBfactor", 
function () {
return Float.isNaN (this.atom.bfactor) && this.atom.anisoBorU != null ? this.atom.anisoBorU[7] * 100 : this.atom.bfactor;
});
Clazz.overrideMethod (c$, "getOccupancy", 
function () {
return this.atom.occupancy;
});
Clazz.overrideMethod (c$, "getIsHetero", 
function () {
return this.atom.isHetero;
});
Clazz.overrideMethod (c$, "getAtomSerial", 
function () {
return this.atom.atomSerial;
});
Clazz.overrideMethod (c$, "getChainID", 
function () {
return J.api.JmolAdapter.canonizeChainID (this.atom.chainID);
});
Clazz.overrideMethod (c$, "getAlternateLocationID", 
function () {
return J.api.JmolAdapter.canonizeAlternateLocationID (this.atom.alternateLocationID);
});
Clazz.overrideMethod (c$, "getGroup3", 
function () {
return this.atom.group3;
});
Clazz.overrideMethod (c$, "getSequenceNumber", 
function () {
return this.atom.sequenceNumber;
});
Clazz.overrideMethod (c$, "getInsertionCode", 
function () {
return J.api.JmolAdapter.canonizeInsertionCode (this.atom.insertionCode);
});
Clazz.overrideMethod (c$, "getXYZ", 
function () {
return this.atom;
});
});
