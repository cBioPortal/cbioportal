Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.Monomer"], "J.modelsetbio.PhosphorusMonomer", ["J.constant.EnumStructure", "J.util.Quaternion", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.$isPurine = false;
this.$isPyrimidine = false;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "PhosphorusMonomer", J.modelsetbio.Monomer);
Clazz.overrideMethod (c$, "isNucleic", 
function () {
return true;
});
Clazz.overrideConstructor (c$, 
function () {
});
c$.validateAndAllocateP = $_M(c$, "validateAndAllocateP", 
function (chain, group3, seqcode, firstIndex, lastIndex, specialAtomIndexes) {
return (firstIndex != lastIndex || specialAtomIndexes[13] != firstIndex ? null :  new J.modelsetbio.PhosphorusMonomer ().set3 (chain, group3, seqcode, firstIndex, lastIndex, J.modelsetbio.PhosphorusMonomer.phosphorusOffsets));
}, "J.modelset.Chain,~S,~N,~N,~N,~A");
$_M(c$, "set3", 
function (chain, group3, seqcode, firstAtomIndex, lastAtomIndex, offsets) {
this.set2 (chain, group3, seqcode, firstAtomIndex, lastAtomIndex, offsets);
if (group3.indexOf ('T') >= 0) chain.isDna = true;
if (group3.indexOf ('U') + group3.indexOf ('I') > -2) chain.isRna = true;
this.$isPurine = (group3.indexOf ('A') + group3.indexOf ('G') + group3.indexOf ('I') > -3);
this.$isPyrimidine = (group3.indexOf ('T') + group3.indexOf ('C') + group3.indexOf ('U') > -3);
return this;
}, "J.modelset.Chain,~S,~N,~N,~N,~A");
$_M(c$, "getP", 
function () {
return this.getAtomFromOffsetIndex (0);
});
$_M(c$, "isPhosphorusMonomer", 
function () {
return true;
});
Clazz.overrideMethod (c$, "isDna", 
function () {
return this.chain.isDna;
});
Clazz.overrideMethod (c$, "isRna", 
function () {
return this.chain.isRna;
});
Clazz.overrideMethod (c$, "isPurine", 
function () {
return this.$isPurine;
});
Clazz.overrideMethod (c$, "isPyrimidine", 
function () {
return this.$isPyrimidine;
});
Clazz.overrideMethod (c$, "getStructure", 
function () {
return this.chain;
});
Clazz.overrideMethod (c$, "getProteinStructureType", 
function () {
return J.constant.EnumStructure.NONE;
});
Clazz.overrideMethod (c$, "isConnectedAfter", 
function (possiblyPreviousMonomer) {
return this.isCA2 (possiblyPreviousMonomer);
}, "J.modelsetbio.Monomer");
$_M(c$, "isCA2", 
function (possiblyPreviousMonomer) {
if (possiblyPreviousMonomer == null) return true;
var distance = this.getLeadAtom ().distance (possiblyPreviousMonomer.getLeadAtom ());
return distance <= J.modelsetbio.PhosphorusMonomer.MAX_ADJACENT_PHOSPHORUS_DISTANCE;
}, "J.modelsetbio.Monomer");
Clazz.overrideMethod (c$, "getQuaternion", 
function (qType) {
return this.getQuaternionP ();
}, "~S");
$_M(c$, "getQuaternionP", 
function () {
var i = this.monomerIndex;
if (i == 0 || i >= this.bioPolymer.monomerCount - 1) return null;
var ptP = this.bioPolymer.monomers[i].getAtomFromOffsetIndex (0);
var ptA;
var ptB;
ptA = this.bioPolymer.monomers[i + 1].getAtomFromOffsetIndex (0);
ptB = this.bioPolymer.monomers[i - 1].getAtomFromOffsetIndex (0);
if (ptP == null || ptA == null || ptB == null) return null;
var vA =  new J.util.V3 ();
var vB =  new J.util.V3 ();
vA.sub2 (ptA, ptP);
vB.sub2 (ptB, ptP);
return J.util.Quaternion.getQuaternionFrameV (vA, vB, null, false);
});
Clazz.overrideMethod (c$, "getQuaternionFrameCenter", 
function (qType) {
return this.getAtomFromOffsetIndex (0);
}, "~S");
Clazz.overrideMethod (c$, "getHelixData", 
function (tokType, qType, mStep) {
return this.getHelixData2 (tokType, qType, mStep);
}, "~N,~S,~N");
Clazz.defineStatics (c$,
"P", 0,
"phosphorusOffsets", [0],
"MAX_ADJACENT_PHOSPHORUS_DISTANCE", 8.0);
});
