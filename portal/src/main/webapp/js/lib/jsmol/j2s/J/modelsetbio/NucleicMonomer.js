Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.PhosphorusMonomer", "J.viewer.JC"], "J.modelsetbio.NucleicMonomer", ["J.constant.EnumStructure", "J.util.P3", "$.Quaternion", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.hasRnaO2Prime = false;
this.baseCenter = null;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "NucleicMonomer", J.modelsetbio.PhosphorusMonomer);
Clazz.overrideConstructor (c$, 
($fz = function () {
}, $fz.isPrivate = true, $fz));
c$.validateAndAllocate = $_M(c$, "validateAndAllocate", 
function (chain, group3, seqcode, firstAtomIndex, lastAtomIndex, specialAtomIndexes) {
var offsets = J.modelsetbio.Monomer.scanForOffsets (firstAtomIndex, specialAtomIndexes, J.modelsetbio.NucleicMonomer.interestingNucleicAtomIDs);
if (offsets == null) return null;
if (!J.modelsetbio.Monomer.checkOptional (offsets, 19, firstAtomIndex, specialAtomIndexes[73])) return null;
J.modelsetbio.Monomer.checkOptional (offsets, 20, firstAtomIndex, specialAtomIndexes[89]);
J.modelsetbio.Monomer.checkOptional (offsets, 18, firstAtomIndex, specialAtomIndexes[90]);
J.modelsetbio.Monomer.checkOptional (offsets, 23, firstAtomIndex, specialAtomIndexes[75]);
J.modelsetbio.Monomer.checkOptional (offsets, 24, firstAtomIndex, specialAtomIndexes[77]);
return ( new J.modelsetbio.NucleicMonomer ()).set4 (chain, group3, seqcode, firstAtomIndex, lastAtomIndex, offsets);
}, "J.modelset.Chain,~S,~N,~N,~N,~A");
$_M(c$, "set4", 
($fz = function (chain, group3, seqcode, firstAtomIndex, lastAtomIndex, offsets) {
this.set3 (chain, group3, seqcode, firstAtomIndex, lastAtomIndex, offsets);
if (!J.modelsetbio.Monomer.have (offsets, 15)) {
offsets[0] = offsets[19];
var offset = offsets[0] & 0xFF;
if (offset != 255) this.leadAtomIndex = firstAtomIndex + offset;
}this.hasRnaO2Prime = J.modelsetbio.Monomer.have (offsets, 2);
this.$isPyrimidine = J.modelsetbio.Monomer.have (offsets, 8);
this.$isPurine = J.modelsetbio.Monomer.have (offsets, 9) && J.modelsetbio.Monomer.have (offsets, 10) && J.modelsetbio.Monomer.have (offsets, 11);
return this;
}, $fz.isPrivate = true, $fz), "J.modelset.Chain,~S,~N,~N,~N,~A");
$_M(c$, "isNucleicMonomer", 
function () {
return true;
});
Clazz.overrideMethod (c$, "isDna", 
function () {
return !this.hasRnaO2Prime;
});
Clazz.overrideMethod (c$, "isRna", 
function () {
return this.hasRnaO2Prime;
});
Clazz.overrideMethod (c$, "isPurine", 
function () {
return this.$isPurine;
});
Clazz.overrideMethod (c$, "isPyrimidine", 
function () {
return this.$isPyrimidine;
});
$_M(c$, "isGuanine", 
function () {
return J.modelsetbio.Monomer.have (this.offsets, 17);
});
Clazz.overrideMethod (c$, "getProteinStructureType", 
function () {
return (this.hasRnaO2Prime ? J.constant.EnumStructure.RNA : J.constant.EnumStructure.DNA);
});
$_M(c$, "getC1P", 
function () {
return this.getAtomFromOffsetIndex (25);
});
$_M(c$, "getC2", 
function () {
return this.getAtomFromOffsetIndex (5);
});
$_M(c$, "getC4P", 
function () {
return this.getAtomFromOffsetIndex (26);
});
$_M(c$, "getN1", 
function () {
return this.getAtomFromOffsetIndex (4);
});
$_M(c$, "getN3", 
function () {
return this.getAtomFromOffsetIndex (6);
});
$_M(c$, "getN2", 
function () {
return this.getAtomFromOffsetIndex (17);
});
$_M(c$, "getN4", 
function () {
return this.getAtomFromOffsetIndex (14);
});
$_M(c$, "getN6", 
function () {
return this.getAtomFromOffsetIndex (16);
});
$_M(c$, "getO2", 
function () {
return this.getAtomFromOffsetIndex (8);
});
$_M(c$, "getO4", 
function () {
return this.getAtomFromOffsetIndex (12);
});
$_M(c$, "getO6", 
function () {
return this.getAtomFromOffsetIndex (13);
});
Clazz.overrideMethod (c$, "getTerminatorAtom", 
function () {
return this.getAtomFromOffsetIndex (J.modelsetbio.Monomer.have (this.offsets, 20) ? 20 : 21);
});
$_M(c$, "getBaseRing6Points", 
function (ring6Points) {
for (var i = 6; --i >= 0; ) ring6Points[i] = this.getAtomFromOffsetIndex (J.modelsetbio.NucleicMonomer.ring6OffsetIndexes[i]);

}, "~A");
$_M(c$, "maybeGetBaseRing5Points", 
function (ring5Points) {
if (this.$isPurine) for (var i = 5; --i >= 0; ) ring5Points[i] = this.getAtomFromOffsetIndex (J.modelsetbio.NucleicMonomer.ring5OffsetIndexes[i]);

return this.$isPurine;
}, "~A");
Clazz.overrideMethod (c$, "isConnectedAfter", 
function (possiblyPreviousMonomer) {
if (possiblyPreviousMonomer == null) return true;
var myPhosphorusAtom = this.getAtomFromOffsetIndex (15);
if (myPhosphorusAtom == null) return false;
return ((possiblyPreviousMonomer).getAtomFromOffsetIndex (21).isBonded (myPhosphorusAtom) || this.isCA2 (possiblyPreviousMonomer));
}, "J.modelsetbio.Monomer");
Clazz.overrideMethod (c$, "findNearestAtomIndex", 
function (x, y, closest, madBegin, madEnd) {
var competitor = closest[0];
var lead = this.getLeadAtom ();
var o5prime = this.getAtomFromOffsetIndex (19);
var c3prime = this.getAtomFromOffsetIndex (22);
var mar = (Clazz.doubleToInt (madBegin / 2));
if (mar < 1900) mar = 1900;
var radius = Clazz.floatToInt (this.scaleToScreen (lead.screenZ, mar));
if (radius < 4) radius = 4;
if (this.isCursorOnTopOf (lead, x, y, radius, competitor) || this.isCursorOnTopOf (o5prime, x, y, radius, competitor) || this.isCursorOnTopOf (c3prime, x, y, radius, competitor)) closest[0] = lead;
}, "~N,~N,~A,~N,~N");
$_M(c$, "setModelClickability", 
function () {
var atom;
if (this.isAtomHidden (this.leadAtomIndex)) return;
for (var i = 6; --i >= 0; ) {
atom = this.getAtomFromOffsetIndex (J.modelsetbio.NucleicMonomer.ring6OffsetIndexes[i]);
atom.setClickable (J.modelsetbio.NucleicMonomer.CARTOON_VISIBILITY_FLAG);
}
if (this.$isPurine) for (var i = 4; --i >= 1; ) {
atom = this.getAtomFromOffsetIndex (J.modelsetbio.NucleicMonomer.ring5OffsetIndexes[i]);
atom.setClickable (J.modelsetbio.NucleicMonomer.CARTOON_VISIBILITY_FLAG);
}
});
$_M(c$, "getN0", 
function () {
return (this.getAtomFromOffsetIndex (this.$isPurine ? 11 : 4));
});
Clazz.overrideMethod (c$, "getHelixData", 
function (tokType, qType, mStep) {
return this.getHelixData2 (tokType, qType, mStep);
}, "~N,~S,~N");
Clazz.overrideMethod (c$, "getQuaternionFrameCenter", 
function (qType) {
switch (qType) {
case 'x':
case 'a':
case 'b':
case 'p':
return this.getP ();
case 'c':
if (this.baseCenter == null) {
var n = 0;
this.baseCenter =  new J.util.P3 ();
for (var i = 0; i < J.modelsetbio.NucleicMonomer.heavyAtomIndexes.length; i++) {
var a = this.getAtomFromOffsetIndex (J.modelsetbio.NucleicMonomer.heavyAtomIndexes[i]);
if (a == null) continue;
this.baseCenter.add (a);
n++;
}
this.baseCenter.scale (1 / n);
}return this.baseCenter;
case 'n':
default:
return this.getN0 ();
}
}, "~S");
Clazz.overrideMethod (c$, "getQuaternion", 
function (qType) {
var ptA = null;
var ptB = null;
var ptNorP;
var yBased = false;
var reverseY = false;
switch (qType) {
case 'a':
ptNorP = this.getP ();
if (this.monomerIndex == 0 || ptNorP == null) return null;
yBased = true;
ptA = (this.bioPolymer.monomers[this.monomerIndex - 1]).getC4P ();
ptB = this.getC4P ();
break;
case 'x':
ptNorP = this.getP ();
if (this.monomerIndex == this.bioPolymer.monomerCount - 1 || ptNorP == null) return null;
ptA = (this.bioPolymer.monomers[this.monomerIndex + 1]).getP ();
ptB = this.getC4P ();
break;
case 'b':
return this.getQuaternionP ();
case 'c':
case 'n':
ptNorP = this.getN0 ();
if (ptNorP == null) return null;
yBased = true;
reverseY = true;
ptA = this.getAtomFromOffsetIndex (5);
ptB = this.getAtomFromOffsetIndex (25);
break;
case 'p':
ptNorP = this.getP ();
if (ptNorP == null) return null;
var p1 = this.getAtomFromOffsetIndex (23);
var p2 = this.getAtomFromOffsetIndex (24);
var bonds = ptNorP.getBonds ();
if (bonds == null) return null;
var g = ptNorP.getGroup ();
for (var i = 0; i < bonds.length; i++) {
var atom = bonds[i].getOtherAtom (ptNorP);
if (p1 != null && atom.index == p1.index) continue;
if (p2 != null && atom.index == p2.index) continue;
if (atom.getGroup () === g) ptB = atom;
 else ptA = atom;
}
break;
case 'q':
return null;
default:
ptNorP = this.getN0 ();
if (ptNorP == null) return null;
if (this.$isPurine) {
ptA = this.getAtomFromOffsetIndex (5);
ptB = this.getAtomFromOffsetIndex (9);
} else {
ptA = this.getAtomFromOffsetIndex (6);
ptB = this.getAtomFromOffsetIndex (1);
}break;
}
if (ptA == null || ptB == null) return null;
var vA = J.util.V3.newVsub (ptA, ptNorP);
var vB = J.util.V3.newVsub (ptB, ptNorP);
if (reverseY) vB.scale (-1);
return J.util.Quaternion.getQuaternionFrameV (vA, vB, null, yBased);
}, "~S");
Clazz.overrideMethod (c$, "isCrossLinked", 
function (g) {
if (!(Clazz.instanceOf (g, J.modelsetbio.NucleicMonomer)) || this.$isPurine == g.isPurine ()) return false;
var otherNucleotide = (this.$isPurine ? g : this);
var myNucleotide = (this.$isPurine ? this : g);
var myN1 = myNucleotide.getN1 ();
var otherN3 = otherNucleotide.getN3 ();
return (myN1.isBonded (otherN3));
}, "J.modelset.Group");
Clazz.overrideMethod (c$, "getCrossLinkLead", 
function (vReturn) {
var N = (this.$isPurine ? this.getN1 () : this.getN3 ());
var bonds = N.getBonds ();
if (bonds == null) return false;
var haveCrossLinks = false;
for (var i = 0; i < bonds.length; i++) {
if (bonds[i].isHydrogen ()) {
var N2 = bonds[i].getOtherAtom (N);
var g = N2.getGroup ();
if (!(Clazz.instanceOf (g, J.modelsetbio.NucleicMonomer))) continue;
var m = g;
if ((this.$isPurine ? m.getN3 () : m.getN1 ()) === N2) {
if (vReturn == null) return true;
vReturn.addLast (Integer.$valueOf (m.leadAtomIndex));
haveCrossLinks = true;
}}}
return haveCrossLinks;
}, "J.util.JmolList");
$_M(c$, "getEdgePoints", 
function (pts) {
pts[0] = this.getLeadAtom ();
pts[1] = this.getC4P ();
pts[2] = pts[5] = this.getC1P ();
switch (this.getGroup1 ()) {
case 'C':
pts[3] = this.getO2 ();
pts[4] = this.getN4 ();
return true;
case 'A':
pts[3] = this.getC2 ();
pts[4] = this.getN6 ();
return true;
case 'G':
case 'I':
pts[3] = this.getC2 ();
pts[4] = this.getO6 ();
return true;
case 'T':
case 'U':
pts[3] = this.getO2 ();
pts[4] = this.getO4 ();
return true;
default:
return false;
}
}, "~A");
Clazz.defineStatics (c$,
"C6", 1,
"O2Pr", 2,
"C5", 3,
"N1", 4,
"C2", 5,
"N3", 6,
"C4", 7,
"O2", 8,
"N7", 9,
"C8", 10,
"N9", 11,
"O4", 12,
"O6", 13,
"N4", 14,
"NP", 15,
"N6", 16,
"N2", 17,
"H5T", 18,
"O5Pr", 19,
"H3T", 20,
"O3Pr", 21,
"C3Pr", 22,
"O1P", 23,
"O2P", 24,
"C1P", 25,
"C4P", 26,
"interestingNucleicAtomIDs", [-14, 37, -80, 36, 32, 33, 34, 35, -39, -40, -41, -42, -48, -47, -43, -14, -45, -44, -73, -7, -89, 10, 9, -75, -77, -13, -9],
"ring6OffsetIndexes", [3, 1, 4, 5, 6, 7],
"ring5OffsetIndexes", [3, 9, 10, 11, 7],
"heavyAtomIndexes", [3, 1, 4, 5, 6, 7, 11, 10, 9, 16, 14, 8, 12, 17, 13]);
c$.CARTOON_VISIBILITY_FLAG = c$.prototype.CARTOON_VISIBILITY_FLAG = J.viewer.JC.getShapeVisibilityFlag (11);
});
