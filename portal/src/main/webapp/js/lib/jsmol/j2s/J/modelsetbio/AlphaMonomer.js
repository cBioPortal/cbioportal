Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.Monomer"], "J.modelsetbio.AlphaMonomer", ["J.constant.EnumStructure", "J.modelsetbio.Helix", "$.Sheet", "$.Turn", "J.util.Quaternion", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.proteinStructure = null;
this.nitrogenHydrogenPoint = null;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "AlphaMonomer", J.modelsetbio.Monomer);
Clazz.overrideMethod (c$, "isProtein", 
function () {
return true;
});
c$.validateAndAllocateA = $_M(c$, "validateAndAllocateA", 
function (chain, group3, seqcode, firstIndex, lastIndex, specialAtomIndexes) {
return (firstIndex != lastIndex || specialAtomIndexes[2] != firstIndex ? null :  new J.modelsetbio.AlphaMonomer ().set2 (chain, group3, seqcode, firstIndex, lastIndex, J.modelsetbio.AlphaMonomer.alphaOffsets));
}, "J.modelset.Chain,~S,~N,~N,~N,~A");
Clazz.overrideConstructor (c$, 
function () {
});
$_M(c$, "isAlphaMonomer", 
function () {
return true;
});
Clazz.overrideMethod (c$, "getProteinStructure", 
function () {
return this.proteinStructure;
});
Clazz.overrideMethod (c$, "getStructure", 
function () {
return this.getProteinStructure ();
});
Clazz.overrideMethod (c$, "setStructure", 
function (proteinStructure) {
this.proteinStructure = proteinStructure;
if (proteinStructure == null) this.nitrogenHydrogenPoint = null;
}, "J.modelsetbio.ProteinStructure");
Clazz.overrideMethod (c$, "setStrucNo", 
function (n) {
if (this.proteinStructure != null) this.proteinStructure.strucNo = n;
}, "~N");
Clazz.overrideMethod (c$, "getProteinStructureType", 
function () {
return this.proteinStructure == null ? J.constant.EnumStructure.NONE : this.proteinStructure.type;
});
Clazz.overrideMethod (c$, "getProteinStructureSubType", 
function () {
return this.proteinStructure == null ? J.constant.EnumStructure.NONE : this.proteinStructure.subtype;
});
Clazz.overrideMethod (c$, "getStrucNo", 
function () {
return this.proteinStructure != null ? this.proteinStructure.strucNo : 0;
});
Clazz.overrideMethod (c$, "isHelix", 
function () {
return this.proteinStructure != null && this.proteinStructure.type === J.constant.EnumStructure.HELIX;
});
Clazz.overrideMethod (c$, "isSheet", 
function () {
return this.proteinStructure != null && this.proteinStructure.type === J.constant.EnumStructure.SHEET;
});
Clazz.overrideMethod (c$, "setProteinStructureType", 
function (type, monomerIndexCurrent) {
if (monomerIndexCurrent < 0 || monomerIndexCurrent > 0 && this.monomerIndex == 0) {
if (this.proteinStructure != null) {
var nAbandoned = this.proteinStructure.removeMonomer (this.monomerIndex);
if (nAbandoned > 0) this.getBioPolymer ().removeProteinStructure (this.monomerIndex + 1, nAbandoned);
}switch (type) {
case J.constant.EnumStructure.HELIX:
case J.constant.EnumStructure.HELIXALPHA:
case J.constant.EnumStructure.HELIX310:
case J.constant.EnumStructure.HELIXPI:
this.setStructure ( new J.modelsetbio.Helix (this.bioPolymer, this.monomerIndex, 1, type));
break;
case J.constant.EnumStructure.SHEET:
this.setStructure ( new J.modelsetbio.Sheet (this.bioPolymer, this.monomerIndex, 1, type));
break;
case J.constant.EnumStructure.TURN:
this.setStructure ( new J.modelsetbio.Turn (this.bioPolymer, this.monomerIndex, 1));
break;
case J.constant.EnumStructure.NONE:
this.setStructure (null);
}
} else {
this.setStructure (this.getBioPolymer ().getProteinStructure (monomerIndexCurrent));
if (this.proteinStructure != null) this.proteinStructure.addMonomer (this.monomerIndex);
}return this.monomerIndex;
}, "J.constant.EnumStructure,~N");
$_M(c$, "getAtom", 
function (specialAtomID) {
return (specialAtomID == 2 ? this.getLeadAtom () : null);
}, "~N");
$_M(c$, "getAtomPoint", 
function (specialAtomID) {
return (specialAtomID == 2 ? this.getLeadAtom () : null);
}, "~N");
Clazz.overrideMethod (c$, "isConnectedAfter", 
function (possiblyPreviousMonomer) {
if (possiblyPreviousMonomer == null) return true;
var atom1 = this.getLeadAtom ();
var atom2 = possiblyPreviousMonomer.getLeadAtom ();
return atom1.isBonded (atom2) || atom1.distance (atom2) <= 4.2;
}, "J.modelsetbio.Monomer");
Clazz.overrideMethod (c$, "getQuaternionFrameCenter", 
function (qType) {
return this.getQuaternionFrameCenterAlpha (qType);
}, "~S");
$_M(c$, "getQuaternionFrameCenterAlpha", 
function (qType) {
switch (qType) {
case 'b':
case 'c':
case 'C':
case 'x':
return this.getLeadAtom ();
default:
case 'a':
case 'n':
case 'p':
case 'P':
case 'q':
return null;
}
}, "~S");
Clazz.overrideMethod (c$, "getHelixData", 
function (tokType, qType, mStep) {
return this.getHelixData2 (tokType, qType, mStep);
}, "~N,~S,~N");
Clazz.overrideMethod (c$, "getQuaternion", 
function (qType) {
return this.getQuaternionAlpha (qType);
}, "~S");
$_M(c$, "getQuaternionAlpha", 
function (qType) {
var vA =  new J.util.V3 ();
var vB =  new J.util.V3 ();
var vC = null;
switch (qType) {
default:
case 'a':
case 'n':
case 'p':
case 'q':
return null;
case 'b':
case 'c':
case 'x':
if (this.monomerIndex == 0 || this.monomerIndex == this.bioPolymer.monomerCount - 1) return null;
var ptCa = this.getLeadAtom ();
var ptCaNext = this.bioPolymer.getLeadPoint (this.monomerIndex + 1);
var ptCaPrev = this.bioPolymer.getLeadPoint (this.monomerIndex - 1);
vA.sub2 (ptCaNext, ptCa);
vB.sub2 (ptCaPrev, ptCa);
break;
}
return J.util.Quaternion.getQuaternionFrameV (vA, vB, vC, false);
}, "~S");
Clazz.defineStatics (c$,
"alphaOffsets", [0]);
});
