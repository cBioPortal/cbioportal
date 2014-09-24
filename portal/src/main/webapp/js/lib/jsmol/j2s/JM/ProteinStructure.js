Clazz.declarePackage ("JM");
Clazz.load (["JU.V3"], "JM.ProteinStructure", ["java.util.Hashtable", "JU.AU", "$.P3", "JU.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.type = null;
this.subtype = null;
this.structureID = null;
this.strucNo = 0;
this.serialID = 0;
this.strandCount = 0;
this.apolymer = null;
this.monomerIndexFirst = 0;
this.nRes = 0;
this.axisA = null;
this.axisB = null;
this.axisUnitVector = null;
this.vectorProjection = null;
this.monomerIndexLast = 0;
this.segments = null;
this.resMap = null;
Clazz.instantialize (this, arguments);
}, JM, "ProteinStructure");
Clazz.prepareFields (c$, function () {
this.vectorProjection =  new JU.V3 ();
});
Clazz.defineMethod (c$, "setupPS", 
function (apolymer, type, monomerIndex, monomerCount) {
this.strucNo = ++JM.ProteinStructure.globalStrucNo;
this.apolymer = apolymer;
this.type = type;
this.monomerIndexFirst = monomerIndex;
this.addMonomer (monomerIndex + monomerCount - 1);
if (JU.Logger.debugging) JU.Logger.info ("Creating ProteinStructure " + this.strucNo + " " + type.getBioStructureTypeName (false) + " from " + this.monomerIndexFirst + " through " + this.monomerIndexLast + " in polymer " + apolymer);
}, "JM.AlphaPolymer,J.c.STR,~N,~N");
Clazz.defineMethod (c$, "addMonomer", 
function (index) {
this.resMap = null;
this.monomerIndexFirst = Math.min (this.monomerIndexFirst, index);
this.monomerIndexLast = Math.max (this.monomerIndexLast, index);
this.nRes = this.monomerIndexLast - this.monomerIndexFirst + 1;
}, "~N");
Clazz.defineMethod (c$, "removeMonomer", 
function (index) {
this.resMap = null;
if (index > this.monomerIndexLast || index < this.monomerIndexFirst) return;
if (index == this.monomerIndexFirst) {
this.monomerIndexFirst++;
this.nRes--;
} else if (index == this.monomerIndexLast) {
this.monomerIndexLast--;
this.nRes--;
} else {
var n = this.monomerIndexLast - index;
this.monomerIndexLast = index - 1;
this.nRes = index - this.monomerIndexFirst;
var monomers = this.apolymer.monomers;
var type = monomers[++index].getProteinStructureType ();
var mLast = -1;
for (var i = 0, pt = index; i < n; i++, pt++) {
(monomers[pt]).setStructure (null);
mLast = monomers[pt].setProteinStructureType (type, mLast);
}
}}, "~N");
Clazz.defineMethod (c$, "calcAxis", 
function () {
});
Clazz.defineMethod (c$, "calcSegments", 
function () {
if (this.segments != null) return;
this.calcAxis ();
this.segments =  new Array (this.nRes + 1);
this.segments[this.nRes] = this.axisB;
this.segments[0] = this.axisA;
var axis = JU.V3.newV (this.axisUnitVector);
axis.scale (this.axisB.distance (this.axisA) / this.nRes);
for (var i = 1; i < this.nRes; i++) {
var point = this.segments[i] =  new JU.P3 ();
point.add2 (this.segments[i - 1], axis);
}
});
Clazz.defineMethod (c$, "lowerNeighborIsHelixOrSheet", 
function () {
if (this.monomerIndexFirst == 0) return false;
return this.apolymer.monomers[this.monomerIndexFirst - 1].isHelix () || this.apolymer.monomers[this.monomerIndexFirst - 1].isSheet ();
});
Clazz.defineMethod (c$, "upperNeighborIsHelixOrSheet", 
function () {
var upperNeighborIndex = this.monomerIndexFirst + this.nRes;
if (upperNeighborIndex == this.apolymer.monomerCount) return false;
return this.apolymer.monomers[upperNeighborIndex].isHelix () || this.apolymer.monomers[upperNeighborIndex].isSheet ();
});
Clazz.defineMethod (c$, "getMonomerCount", 
function () {
return this.nRes;
});
Clazz.defineMethod (c$, "isWithin", 
function (monomerIndex) {
return (monomerIndex > this.monomerIndexFirst && monomerIndex < this.monomerIndexLast);
}, "~N");
Clazz.defineMethod (c$, "getMonomerIndex", 
function () {
return this.monomerIndexFirst;
});
Clazz.defineMethod (c$, "getIndex", 
function (monomer) {
if (this.resMap == null) {
this.resMap =  new java.util.Hashtable ();
for (var i = this.nRes; --i >= 0; ) this.resMap.put (this.apolymer.monomers[this.monomerIndexFirst + i], Integer.$valueOf (i));

}var ii = this.resMap.get (monomer);
return (ii == null ? -1 : ii.intValue ());
}, "JM.Monomer");
Clazz.defineMethod (c$, "getSegments", 
function () {
if (this.segments == null) this.calcSegments ();
return this.segments;
});
Clazz.defineMethod (c$, "getAxisStartPoint", 
function () {
this.calcAxis ();
return this.axisA;
});
Clazz.defineMethod (c$, "getAxisEndPoint", 
function () {
this.calcAxis ();
return this.axisB;
});
Clazz.defineMethod (c$, "getStructureMidPoint", 
function (index) {
if (this.segments == null) this.calcSegments ();
return this.segments[index];
}, "~N");
Clazz.defineMethod (c$, "getInfo", 
function (info) {
info.put ("type", this.type.getBioStructureTypeName (false));
var leadAtomIndices = this.apolymer.getLeadAtomIndices ();
var iArray = JU.AU.arrayCopyRangeI (leadAtomIndices, this.monomerIndexFirst, this.monomerIndexFirst + this.nRes);
info.put ("leadAtomIndices", iArray);
this.calcAxis ();
if (this.axisA == null) return;
info.put ("axisA", this.axisA);
info.put ("axisB", this.axisB);
info.put ("axisUnitVector", this.axisUnitVector);
}, "java.util.Map");
Clazz.defineMethod (c$, "resetAxes", 
function () {
this.axisA = null;
this.segments = null;
});
Clazz.defineStatics (c$,
"globalStrucNo", 1000);
});
