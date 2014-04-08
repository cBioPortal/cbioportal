Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.util.V3"], "J.modelsetbio.ProteinStructure", ["J.util.ArrayUtil", "$.Logger", "$.P3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.type = null;
this.subtype = null;
this.structureID = null;
this.strucNo = 0;
this.serialID = 0;
this.strandCount = 0;
this.apolymer = null;
this.monomerIndexFirst = 0;
this.monomerCount = 0;
this.axisA = null;
this.axisB = null;
this.axisUnitVector = null;
this.vectorProjection = null;
this.monomerIndexLast = 0;
this.segments = null;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "ProteinStructure");
Clazz.prepareFields (c$, function () {
this.vectorProjection =  new J.util.V3 ();
});
$_M(c$, "setupPS", 
function (apolymer, type, monomerIndex, monomerCount) {
this.strucNo = ($t$ = ++ J.modelsetbio.ProteinStructure.globalStrucNo, J.modelsetbio.ProteinStructure.prototype.globalStrucNo = J.modelsetbio.ProteinStructure.globalStrucNo, $t$);
this.apolymer = apolymer;
this.type = type;
this.monomerIndexFirst = monomerIndex;
this.addMonomer (monomerIndex + monomerCount - 1);
if (J.util.Logger.debugging) J.util.Logger.debug ("Creating ProteinStructure " + this.strucNo + " " + type.getBioStructureTypeName (false) + " from " + this.monomerIndexFirst + " through " + this.monomerIndexLast + " in polymer " + apolymer);
}, "J.modelsetbio.AlphaPolymer,J.constant.EnumStructure,~N,~N");
$_M(c$, "addMonomer", 
function (index) {
this.monomerIndexFirst = Math.min (this.monomerIndexFirst, index);
this.monomerIndexLast = Math.max (this.monomerIndexLast, index);
this.monomerCount = this.monomerIndexLast - this.monomerIndexFirst + 1;
}, "~N");
$_M(c$, "removeMonomer", 
function (monomerIndex) {
if (monomerIndex > this.monomerIndexLast || monomerIndex < this.monomerIndexFirst) return 0;
var ret = this.monomerIndexLast - monomerIndex;
this.monomerIndexLast = Math.max (this.monomerIndexFirst, monomerIndex) - 1;
this.monomerCount = this.monomerIndexLast - this.monomerIndexFirst + 1;
return ret;
}, "~N");
$_M(c$, "calcAxis", 
function () {
});
$_M(c$, "calcSegments", 
function () {
if (this.segments != null) return;
this.calcAxis ();
this.segments =  new Array (this.monomerCount + 1);
this.segments[this.monomerCount] = this.axisB;
this.segments[0] = this.axisA;
var axis = J.util.V3.newV (this.axisUnitVector);
axis.scale (this.axisB.distance (this.axisA) / this.monomerCount);
for (var i = 1; i < this.monomerCount; i++) {
var point = this.segments[i] =  new J.util.P3 ();
point.setT (this.segments[i - 1]);
point.add (axis);
}
});
$_M(c$, "lowerNeighborIsHelixOrSheet", 
function () {
if (this.monomerIndexFirst == 0) return false;
return this.apolymer.monomers[this.monomerIndexFirst - 1].isHelix () || this.apolymer.monomers[this.monomerIndexFirst - 1].isSheet ();
});
$_M(c$, "upperNeighborIsHelixOrSheet", 
function () {
var upperNeighborIndex = this.monomerIndexFirst + this.monomerCount;
if (upperNeighborIndex == this.apolymer.monomerCount) return false;
return this.apolymer.monomers[upperNeighborIndex].isHelix () || this.apolymer.monomers[upperNeighborIndex].isSheet ();
});
$_M(c$, "getMonomerCount", 
function () {
return this.monomerCount;
});
$_M(c$, "isWithin", 
function (monomerIndex) {
return (monomerIndex > this.monomerIndexFirst && monomerIndex < this.monomerIndexLast);
}, "~N");
$_M(c$, "getMonomerIndex", 
function () {
return this.monomerIndexFirst;
});
$_M(c$, "getIndex", 
function (monomer) {
var monomers = this.apolymer.monomers;
var i;
for (i = this.monomerCount; --i >= 0; ) if (monomers[this.monomerIndexFirst + i] === monomer) break;

return i;
}, "J.modelsetbio.Monomer");
$_M(c$, "getSegments", 
function () {
if (this.segments == null) this.calcSegments ();
return this.segments;
});
$_M(c$, "getAxisStartPoint", 
function () {
this.calcAxis ();
return this.axisA;
});
$_M(c$, "getAxisEndPoint", 
function () {
this.calcAxis ();
return this.axisB;
});
$_M(c$, "getStructureMidPoint", 
function (index) {
if (this.segments == null) this.calcSegments ();
return this.segments[index];
}, "~N");
$_M(c$, "getInfo", 
function (info) {
info.put ("type", this.type.getBioStructureTypeName (false));
var leadAtomIndices = this.apolymer.getLeadAtomIndices ();
var iArray = J.util.ArrayUtil.arrayCopyRangeI (leadAtomIndices, this.monomerIndexFirst, this.monomerIndexFirst + this.monomerCount);
info.put ("leadAtomIndices", iArray);
this.calcAxis ();
if (this.axisA == null) return;
info.put ("axisA", this.axisA);
info.put ("axisB", this.axisB);
info.put ("axisUnitVector", this.axisUnitVector);
}, "java.util.Map");
$_M(c$, "resetAxes", 
function () {
this.axisA = null;
this.segments = null;
});
Clazz.defineStatics (c$,
"globalStrucNo", 1000);
});
