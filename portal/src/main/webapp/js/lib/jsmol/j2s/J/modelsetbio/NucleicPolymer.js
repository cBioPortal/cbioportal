Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.BioPolymer"], "J.modelsetbio.NucleicPolymer", ["J.modelset.HBond", "J.util.Measure", "$.P4", "$.V3"], function () {
c$ = Clazz.declareType (J.modelsetbio, "NucleicPolymer", J.modelsetbio.BioPolymer);
Clazz.makeConstructor (c$, 
function (monomers) {
Clazz.superConstructor (this, J.modelsetbio.NucleicPolymer, [monomers]);
this.type = 2;
this.hasWingPoints = true;
}, "~A");
$_M(c$, "getNucleicPhosphorusAtom", 
function (monomerIndex) {
return this.monomers[monomerIndex].getLeadAtom ();
}, "~N");
Clazz.overrideMethod (c$, "calcEtaThetaAngles", 
function () {
var eta = NaN;
for (var i = 0; i < this.monomerCount - 2; ++i) {
var m1 = this.monomers[i];
var m2 = this.monomers[i + 1];
var p1 = m1.getP ();
var c41 = m1.getC4P ();
var p2 = m2.getP ();
var c42 = m2.getC4P ();
if (i > 0) {
var m0 = this.monomers[i - 1];
var c40 = m0.getC4P ();
eta = J.util.Measure.computeTorsion (c40, p1, c41, p2, true);
}var theta = J.util.Measure.computeTorsion (p1, c41, p2, c42, true);
if (eta < 0) eta += 360;
if (theta < 0) theta += 360;
m1.setGroupParameter (1112539141, eta);
m1.setGroupParameter (1112539152, theta);
}
return true;
});
Clazz.overrideMethod (c$, "calcRasmolHydrogenBonds", 
function (polymer, bsA, bsB, vAtoms, nMaxPerResidue, min, checkDistances, dsspIgnoreHydrogens) {
var other = polymer;
var vNorm =  new J.util.V3 ();
var vAB =  new J.util.V3 ();
var vAC =  new J.util.V3 ();
for (var i = this.monomerCount; --i >= 0; ) {
var myNucleotide = this.monomers[i];
if (!myNucleotide.isPurine ()) continue;
var myN3 = myNucleotide.getN3 ();
var isInA = bsA.get (myN3.index);
if (!isInA && !bsB.get (myN3.index)) continue;
var myN1 = myNucleotide.getN1 ();
var myN9 = myNucleotide.getN0 ();
var plane =  new J.util.P4 ();
J.util.Measure.getPlaneThroughPoints (myN3, myN1, myN9, vNorm, vAB, vAC, plane);
var bestN3 = null;
var minDist2 = 25;
var bestNucleotide = null;
for (var j = other.monomerCount; --j >= 0; ) {
var otherNucleotide = other.monomers[j];
if (!otherNucleotide.isPyrimidine ()) continue;
var otherN3 = otherNucleotide.getN3 ();
if (isInA ? !bsB.get (otherN3.index) : !bsA.get (otherN3.index)) continue;
var otherN1 = otherNucleotide.getN0 ();
var dist2 = myN1.distanceSquared (otherN3);
if (dist2 < minDist2 && myN9.distanceSquared (otherN1) > 50 && Math.abs (J.util.Measure.distanceToPlane (plane, otherN3)) < 1) {
bestNucleotide = otherNucleotide;
bestN3 = otherN3;
minDist2 = dist2;
}}
var n = 0;
if (bestN3 != null) {
n += J.modelsetbio.NucleicPolymer.addHydrogenBond (vAtoms, myN1, bestN3);
if (n >= nMaxPerResidue) continue;
if (myNucleotide.isGuanine ()) {
n += J.modelsetbio.NucleicPolymer.addHydrogenBond (vAtoms, myNucleotide.getN2 (), bestNucleotide.getO2 ());
if (n >= nMaxPerResidue) continue;
n += J.modelsetbio.NucleicPolymer.addHydrogenBond (vAtoms, myNucleotide.getO6 (), bestNucleotide.getN4 ());
if (n >= nMaxPerResidue) continue;
} else {
n += J.modelsetbio.NucleicPolymer.addHydrogenBond (vAtoms, myNucleotide.getN6 (), bestNucleotide.getO4 ());
}}}
}, "J.modelsetbio.BioPolymer,J.util.BS,J.util.BS,J.util.JmolList,~N,~A,~B,~B");
c$.addHydrogenBond = $_M(c$, "addHydrogenBond", 
function (vAtoms, atom1, atom2) {
if (atom1 == null || atom2 == null) return 0;
vAtoms.addLast ( new J.modelset.HBond (atom1, atom2, 18432, 1, 0, 0));
return 1;
}, "J.util.JmolList,J.modelset.Atom,J.modelset.Atom");
$_M(c$, "getPdbData", 
function (viewer, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten) {
J.modelsetbio.BioPolymer.getPdbData (viewer, this, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten);
}, "J.viewer.Viewer,~S,~S,~N,~N,J.util.BS,J.util.BS,~B,~B,~B,~A,J.io.OutputStringBuilder,J.util.SB,J.util.BS");
});
