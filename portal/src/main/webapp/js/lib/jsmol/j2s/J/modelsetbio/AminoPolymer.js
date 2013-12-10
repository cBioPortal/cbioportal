Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.AlphaPolymer"], "J.modelsetbio.AminoPolymer", ["java.lang.Boolean", "java.util.Hashtable", "J.constant.EnumStructure", "J.i18n.GT", "J.modelset.HBond", "J.modelsetbio.APBridge", "J.util.ArrayUtil", "$.BS", "$.Escape", "$.JmolList", "$.Logger", "$.Measure", "$.P3", "$.SB", "$.TextFormat", "$.V3", "J.viewer.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.structureList = null;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "AminoPolymer", J.modelsetbio.AlphaPolymer);
Clazz.makeConstructor (c$, 
function (monomers) {
Clazz.superConstructor (this, J.modelsetbio.AminoPolymer, [monomers]);
this.type = 1;
for (var i = 0; i < this.monomerCount; ++i) if (!(monomers[i]).hasOAtom ()) return;

this.hasWingPoints = true;
}, "~A");
Clazz.overrideMethod (c$, "resetHydrogenPoints", 
function () {
var ps;
var psLast = null;
for (var i = 0; i < this.monomerCount; i++) {
if ((ps = this.getProteinStructure (i)) != null && ps !== psLast) (psLast = ps).resetAxes ();
(this.monomers[i]).resetHydrogenPoint ();
}
});
Clazz.overrideMethod (c$, "calcPhiPsiAngles", 
function () {
for (var i = 0; i < this.monomerCount - 1; ++i) this.calcPhiPsiAngles2 (this.monomers[i], this.monomers[i + 1]);

return true;
});
$_M(c$, "calcPhiPsiAngles2", 
($fz = function (residue1, residue2) {
var nitrogen1 = residue1.getNitrogenAtom ();
var alphacarbon1 = residue1.getLeadAtom ();
var carbon1 = residue1.getCarbonylCarbonAtom ();
var nitrogen2 = residue2.getNitrogenAtom ();
var alphacarbon2 = residue2.getLeadAtom ();
var carbon2 = residue2.getCarbonylCarbonAtom ();
residue2.setGroupParameter (1112539145, J.util.Measure.computeTorsion (carbon1, nitrogen2, alphacarbon2, carbon2, true));
residue1.setGroupParameter (1112539146, J.util.Measure.computeTorsion (nitrogen1, alphacarbon1, carbon1, nitrogen2, true));
residue1.setGroupParameter (1112539144, J.util.Measure.computeTorsion (alphacarbon1, carbon1, nitrogen2, alphacarbon2, true));
}, $fz.isPrivate = true, $fz), "J.modelsetbio.AminoMonomer,J.modelsetbio.AminoMonomer");
Clazz.overrideMethod (c$, "calculateRamachandranHelixAngle", 
function (m, qtype) {
var psiLast = (m == 0 ? NaN : this.monomers[m - 1].getGroupParameter (1112539146));
var psi = this.monomers[m].getGroupParameter (1112539146);
var phi = this.monomers[m].getGroupParameter (1112539145);
var phiNext = (m == this.monomerCount - 1 ? NaN : this.monomers[m + 1].getGroupParameter (1112539145));
var psiNext = (m == this.monomerCount - 1 ? NaN : this.monomers[m + 1].getGroupParameter (1112539146));
switch (qtype) {
default:
case 'p':
case 'r':
case 'P':
var dPhi = ((phiNext - phi) / 2 * 3.141592653589793 / 180);
var dPsi = ((psiNext - psi) / 2 * 3.141592653589793 / 180);
return (57.29577951308232 * 2 * Math.acos (Math.cos (dPsi) * Math.cos (dPhi) - Math.sin (dPsi) * Math.sin (dPhi) / 3));
case 'c':
case 'C':
return (psi - psiLast + phiNext - phi);
}
}, "~N,~S");
$_M(c$, "calcRasmolHydrogenBonds", 
function (polymer, bsA, bsB, vHBonds, nMaxPerResidue, min, checkDistances, dsspIgnoreHydrogens) {
if (polymer == null) polymer = this;
if (!(Clazz.instanceOf (polymer, J.modelsetbio.AminoPolymer))) return;
var pt =  new J.util.P3 ();
var vNH =  new J.util.V3 ();
var source;
var min1 = (min == null ?  Clazz.newIntArray (2, 3, 0) : null);
for (var i = 1; i < this.monomerCount; ++i) {
if (min == null) {
min1[0][0] = min1[1][0] = this.bioPolymerIndexInModel;
min1[0][1] = min1[1][1] = -2147483648;
min1[0][2] = min1[1][2] = 0;
} else {
min1 = min[i];
}if ((source = (this.monomers[i])).getNHPoint (pt, vNH, checkDistances, dsspIgnoreHydrogens)) {
var isInA = (bsA == null || bsA.get (source.getNitrogenAtom ().index));
if (!isInA) continue;
if (!checkDistances && source.getCarbonylOxygenAtom () == null) continue;
this.checkRasmolHydrogenBond (source, polymer, i, pt, (isInA ? bsB : bsA), vHBonds, min1, checkDistances);
}}
}, "J.modelsetbio.BioPolymer,J.util.BS,J.util.BS,J.util.JmolList,~N,~A,~B,~B");
$_M(c$, "checkRasmolHydrogenBond", 
($fz = function (source, polymer, indexDonor, hydrogenPoint, bsB, vHBonds, min, checkDistances) {
var sourceAlphaPoint = source.getLeadAtom ();
var sourceNitrogenPoint = source.getNitrogenAtom ();
var nitrogen = source.getNitrogenAtom ();
var m;
for (var i = polymer.monomerCount; --i >= 0; ) {
if (polymer === this && (i == indexDonor || i + 1 == indexDonor)) continue;
var target = polymer.monomers[i];
var oxygen = target.getCarbonylOxygenAtom ();
if (oxygen == null || bsB != null && !bsB.get (oxygen.index)) continue;
var targetAlphaPoint = target.getLeadAtom ();
var dist2 = sourceAlphaPoint.distanceSquared (targetAlphaPoint);
if (dist2 >= 81.0) continue;
var energy = this.calcHbondEnergy (sourceNitrogenPoint, hydrogenPoint, target, checkDistances);
if (energy < min[0][2]) {
m = min[1];
min[1] = min[0];
min[0] = m;
} else if (energy < min[1][2]) {
m = min[1];
} else {
continue;
}m[0] = polymer.bioPolymerIndexInModel;
m[1] = (energy < -500 ? i : -1 - i);
m[2] = energy;
}
if (vHBonds != null) for (var i = 0; i < 2; i++) if (min[i][1] >= 0) this.addResidueHydrogenBond (nitrogen, ((polymer).monomers[min[i][1]]).getCarbonylOxygenAtom (), (polymer === this ? indexDonor : -99), min[i][1], min[i][2] / 1000, vHBonds);

}, $fz.isPrivate = true, $fz), "J.modelsetbio.AminoMonomer,J.modelsetbio.BioPolymer,~N,J.util.P3,J.util.BS,J.util.JmolList,~A,~B");
$_M(c$, "calcHbondEnergy", 
($fz = function (nitrogenPoint, hydrogenPoint, target, checkDistances) {
var targetOxygenPoint = target.getCarbonylOxygenAtom ();
if (targetOxygenPoint == null) return 0;
var distON2 = targetOxygenPoint.distanceSquared (nitrogenPoint);
if (distON2 < 0.25) return 0;
var distOH2 = targetOxygenPoint.distanceSquared (hydrogenPoint);
if (distOH2 < 0.25) return 0;
var targetCarbonPoint = target.getCarbonylCarbonAtom ();
var distCH2 = targetCarbonPoint.distanceSquared (hydrogenPoint);
if (distCH2 < 0.25) return 0;
var distCN2 = targetCarbonPoint.distanceSquared (nitrogenPoint);
if (distCN2 < 0.25) return 0;
var distOH = Math.sqrt (distOH2);
var distCH = Math.sqrt (distCH2);
var distCN = Math.sqrt (distCN2);
var distON = Math.sqrt (distON2);
var energy = J.modelset.HBond.getEnergy (distOH, distCH, distCN, distON);
var isHbond = (energy < -500 && (!checkDistances || distCN > distCH && distOH <= 3.0));
return (!isHbond && checkDistances || energy < -9900 ? 0 : energy);
}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3,J.modelsetbio.AminoMonomer,~B");
$_M(c$, "addResidueHydrogenBond", 
($fz = function (nitrogen, oxygen, indexAminoGroup, indexCarbonylGroup, energy, vHBonds) {
var order;
switch (indexAminoGroup - indexCarbonylGroup) {
case 2:
order = 6144;
break;
case 3:
order = 8192;
break;
case 4:
order = 10240;
break;
case 5:
order = 12288;
break;
case -3:
order = 14336;
break;
case -4:
order = 16384;
break;
default:
order = 4096;
}
vHBonds.addLast ( new J.modelset.HBond (nitrogen, oxygen, order, 1, 0, energy));
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,J.modelset.Atom,~N,~N,~N,J.util.JmolList");
c$.calculateStructuresDssp = $_M(c$, "calculateStructuresDssp", 
function (bioPolymers, bioPolymerCount, vHBonds, doReport, dsspIgnoreHydrogens, setStructure) {
var bsAmino =  new J.util.BS ();
for (var i = 0; i < bioPolymerCount; i++) if (Clazz.instanceOf (bioPolymers[i], J.modelsetbio.AminoPolymer)) bsAmino.set (i);

if (bsAmino.isEmpty ()) return "";
var m = bioPolymers[0].model;
var sb =  new J.util.SB ();
sb.append ("Jmol ").append (J.viewer.Viewer.getJmolVersion ()).append (" DSSP analysis for model ").append (m.getModelNumberDotted ()).append (" - ").append (m.getModelTitle ()).append ("\n");
if (m.modelIndex == 0) sb.append ("\nW. Kabsch and C. Sander, Biopolymers, vol 22, 1983, pp 2577-2637\n").append ("\nWe thank Wolfgang Kabsch and Chris Sander for writing the DSSP software,\n").append ("and we thank the CMBI for maintaining it to the extent that it was easy to\n").append ("re-engineer for our purposes. At this point in time, we make no guarantee\n").append ("that this code gives precisely the same analysis as the code available via license\n").append ("from CMBI at http://swift.cmbi.ru.nl/gv/dssp\n");
if (setStructure && m.modelIndex == 0) sb.append ("\nAll bioshapes have been deleted and must be regenerated.\n");
if (m.nAltLocs > 0) sb.append ("\nNote: This model contains alternative locations. Use  'CONFIGURATION 1' to be consistent with CMBI DSSP.\n");
var labels =  Clazz.newCharArray (bioPolymerCount, '\0');
var bsDone =  new Array (bioPolymerCount);
var bsBad =  new J.util.BS ();
var haveWarned = false;
for (var i = bsAmino.nextSetBit (0); i >= 0; i = bsAmino.nextSetBit (i + 1)) {
var ap = bioPolymers[i];
if (!haveWarned && (ap.monomers[0]).getExplicitNH () != null) {
if (dsspIgnoreHydrogens) sb.append (J.i18n.GT._ ("NOTE: Backbone amide hydrogen positions are present and will be ignored. Their positions will be approximated, as in standard DSSP analysis.\nUse {0} to not use this approximation.\n\n", "SET dsspCalculateHydrogenAlways FALSE"));
 else sb.append (J.i18n.GT._ ("NOTE: Backbone amide hydrogen positions are present and will be used. Results may differ significantly from standard DSSP analysis.\nUse {0} to ignore these hydrogen positions.\n\n", "SET dsspCalculateHydrogenAlways TRUE"));
haveWarned = true;
}bioPolymers[i].recalculateLeadMidpointsAndWingVectors ();
labels[i] =  Clazz.newCharArray (bioPolymers[i].monomerCount, '\0');
bsDone[i] =  new J.util.BS ();
for (var j = 0; j < ap.monomerCount; j++) if ((ap.monomers[j]).getCarbonylOxygenAtom () == null) bsBad.set (ap.monomers[j].leadAtomIndex);

}
var min = J.modelsetbio.AminoPolymer.getDualHydrogenBondArray (bioPolymers, bioPolymerCount, dsspIgnoreHydrogens);
var bridgesA =  new J.util.JmolList ();
var bridgesP =  new J.util.JmolList ();
var htBridges =  new java.util.Hashtable ();
var htLadders =  new java.util.Hashtable ();
J.modelsetbio.AminoPolymer.getBridges (bioPolymers, min, bridgesA, bridgesP, htBridges, htLadders, bsBad, vHBonds, bsDone);
J.modelsetbio.AminoPolymer.getSheetStructures (bioPolymers, bridgesA, bridgesP, htBridges, htLadders, labels, bsDone, doReport, setStructure);
var reports =  new Array (bioPolymerCount);
for (var i = bsAmino.nextSetBit (0); i >= 0; i = bsAmino.nextSetBit (i + 1)) if (min[i] != null) reports[i] = (bioPolymers[i]).findHelixes (min[i], i, bsDone[i], labels[i], doReport, setStructure, vHBonds, bsBad);

if (doReport) {
var sbSummary =  new J.util.SB ();
sb.append ("\n------------------------------\n");
for (var i = bsAmino.nextSetBit (0); i >= 0; i = bsAmino.nextSetBit (i + 1)) if (labels[i] != null) {
var ap = bioPolymers[i];
sbSummary.append (ap.dumpSummary (labels[i]));
sb.append (reports[i]).append (ap.dumpTags ("$.1: " + String.valueOf (labels[i]), bsBad, 2));
}
if (bsBad.nextSetBit (0) >= 0) sb.append ("\nNOTE: '!' indicates a residue that is missing a backbone carbonyl oxygen atom.\n");
sb.append ("\n").append ("SUMMARY:" + sbSummary);
}return sb.toString ();
}, "~A,~N,J.util.JmolList,~B,~B,~B");
c$.getDualHydrogenBondArray = $_M(c$, "getDualHydrogenBondArray", 
($fz = function (bioPolymers, bioPolymerCount, dsspIgnoreHydrogens) {
var min = J.util.ArrayUtil.newInt4 (bioPolymerCount);
for (var i = 0; i < bioPolymerCount; i++) {
if (!(Clazz.instanceOf (bioPolymers[i], J.modelsetbio.AminoPolymer))) continue;
var n = bioPolymers[i].monomerCount;
min[i] =  Clazz.newIntArray (n, 2, 3, 0);
for (var j = 0; j < n; ++j) {
min[i][j][0][1] = min[i][j][1][1] = -2147483648;
min[i][j][0][2] = min[i][j][1][2] = 0;
}
}
for (var i = 0; i < bioPolymerCount; i++) if (min[i] != null) for (var j = 0; j < bioPolymerCount; j++) if (min[j] != null) bioPolymers[i].calcRasmolHydrogenBonds (bioPolymers[j], null, null, null, 2, min[i], false, dsspIgnoreHydrogens);


return min;
}, $fz.isPrivate = true, $fz), "~A,~N,~B");
$_M(c$, "findHelixes", 
($fz = function (min, iPolymer, bsDone, labels, doReport, setStructure, vHBonds, bsBad) {
if (J.util.Logger.debugging) for (var j = 0; j < this.monomerCount; j++) J.util.Logger.debug (iPolymer + "." + this.monomers[j].getResno () + "\t" + J.util.Escape.e (min[j]));

var bsTurn =  new J.util.BS ();
var line4 = this.findHelixes2 (4, min, iPolymer, J.constant.EnumStructure.HELIXALPHA, 10240, bsDone, bsTurn, labels, doReport, setStructure, vHBonds, bsBad);
var line3 = this.findHelixes2 (3, min, iPolymer, J.constant.EnumStructure.HELIX310, 8192, bsDone, bsTurn, labels, doReport, setStructure, vHBonds, bsBad);
var line5 = this.findHelixes2 (5, min, iPolymer, J.constant.EnumStructure.HELIXPI, 12288, bsDone, bsTurn, labels, doReport, setStructure, vHBonds, bsBad);
if (setStructure) this.setStructure (bsTurn, J.constant.EnumStructure.TURN);
if (doReport) {
this.setTag (labels, bsTurn, 'T');
return this.dumpTags ("$.5: " + line5 + "\n" + "$.4: " + line4 + "\n" + "$.3: " + line3, bsBad, 1);
}return "";
}, $fz.isPrivate = true, $fz), "~A,~N,J.util.BS,~A,~B,~B,J.util.JmolList,J.util.BS");
$_M(c$, "findHelixes2", 
($fz = function (pitch, min, thisIndex, subtype, type, bsDone, bsTurn, labels, doReport, setStructure, vHBonds, bsBad) {
var bsStart =  new J.util.BS ();
var bsNNN =  new J.util.BS ();
var bsX =  new J.util.BS ();
var bsStop =  new J.util.BS ();
var bsHelix =  new J.util.BS ();
var warning = "";
for (var i = pitch; i < this.monomerCount; ++i) {
var i0 = i - pitch;
var bpt = 0;
if (min[i][0][0] == thisIndex && min[i][0][1] == i0 || min[i][bpt = 1][0] == thisIndex && min[i][1][1] == i0) {
var ia = this.monomers[i0].leadAtomIndex;
var ipt = bsBad.nextSetBit (ia);
if (ipt >= ia && ipt <= this.monomers[i].leadAtomIndex) continue;
bsStart.set (i0);
bsNNN.setBits (i0 + 1, i);
bsStop.set (i);
ipt = bsDone.nextSetBit (i0);
var isClear = (ipt < 0 || ipt >= i);
var addH = false;
if (i0 > 0 && bsStart.get (i0 - 1) && (pitch == 4 || isClear)) {
bsHelix.setBits (i0, i);
if (!isClear) warning += "  WARNING! Bridge to helix at " + this.monomers[ipt];
addH = true;
} else if (isClear || bsDone.nextClearBit (ipt) < i) {
addH = true;
}if (bsStop.get (i0)) bsX.set (i0);
if (addH && vHBonds != null) {
J.modelsetbio.AminoPolymer.addHbond (vHBonds, this.monomers[i], this.monomers[i0], min[i][bpt][2], type, null);
}}}
var taglines;
if (doReport) {
taglines =  Clazz.newCharArray (this.monomerCount, '\0');
this.setTag (taglines, bsNNN, String.fromCharCode (48 + pitch));
this.setTag (taglines, bsStart, '>');
this.setTag (taglines, bsStop, '<');
this.setTag (taglines, bsX, 'X');
} else {
taglines = null;
}bsDone.or (bsHelix);
bsNNN.andNot (bsDone);
bsTurn.or (bsNNN);
bsTurn.andNot (bsHelix);
if (setStructure) this.setStructure (bsHelix, subtype);
if (doReport) {
this.setTag (labels, bsHelix, String.fromCharCode (68 + pitch));
return String.valueOf (taglines) + warning;
}return "";
}, $fz.isPrivate = true, $fz), "~N,~A,~N,J.constant.EnumStructure,~N,J.util.BS,J.util.BS,~A,~B,~B,J.util.JmolList,J.util.BS");
c$.getBridges = $_M(c$, "getBridges", 
($fz = function (bioPolymers, min, bridgesA, bridgesP, htBridges, htLadders, bsBad, vHBonds, bsDone) {
var atoms = bioPolymers[0].model.getModelSet ().atoms;
var bridge = null;
var htTemp =  new java.util.Hashtable ();
for (var p1 = 0; p1 < min.length; p1++) if (Clazz.instanceOf (bioPolymers[p1], J.modelsetbio.AminoPolymer)) {
var ap1 = (bioPolymers[p1]);
var n = min[p1].length - 1;
for (var a = 1; a < n; a++) {
var ia = ap1.monomers[a].leadAtomIndex;
if (bsBad.get (ia)) continue;
for (var p2 = p1; p2 < min.length; p2++) if (Clazz.instanceOf (bioPolymers[p2], J.modelsetbio.AminoPolymer)) for (var b = (p1 == p2 ? a + 3 : 1); b < min[p2].length - 1; b++) {
var ap2 = bioPolymers[p2];
var ib = ap2.monomers[b].leadAtomIndex;
if (bsBad.get (ib)) continue;
if ((bridge = J.modelsetbio.AminoPolymer.getBridge (min, p1, a, p2, b, bridgesP, atoms[ia], atoms[ib], ap1, ap2, vHBonds, htTemp, false, htLadders)) != null) {
} else if ((bridge = J.modelsetbio.AminoPolymer.getBridge (min, p1, a, p2, b, bridgesA, atoms[ia], atoms[ib], ap1, ap2, vHBonds, htTemp, true, htLadders)) != null) {
bridge.isAntiparallel = true;
} else {
continue;
}if (J.util.Logger.debugging) J.util.Logger.debug ("Bridge found " + bridge);
bsDone[p1].set (a);
bsDone[p2].set (b);
htBridges.put (ia + "-" + ib, bridge);
}

}
}
}, $fz.isPrivate = true, $fz), "~A,~A,J.util.JmolList,J.util.JmolList,java.util.Map,java.util.Map,J.util.BS,J.util.JmolList,~A");
c$.getBridge = $_M(c$, "getBridge", 
($fz = function (min, p1, a, p2, b, bridges, atom1, atom2, ap1, ap2, vHBonds, htTemp, isAntiparallel, htLadders) {
var b1 = null;
var b2 = null;
var ipt = 0;
var offsets = (isAntiparallel ? J.modelsetbio.AminoPolymer.sheetOffsets[1] : J.modelsetbio.AminoPolymer.sheetOffsets[0]);
if ((b1 = J.modelsetbio.AminoPolymer.isHbonded (a + offsets[0], b + offsets[1], p1, p2, min)) != null && (b2 = J.modelsetbio.AminoPolymer.isHbonded (b + offsets[2], a + offsets[3], p2, p1, min)) != null || (b1 = J.modelsetbio.AminoPolymer.isHbonded (a + offsets[ipt = 4], b + offsets[5], p1, p2, min)) != null && (b2 = J.modelsetbio.AminoPolymer.isHbonded (b + offsets[6], a + offsets[7], p2, p1, min)) != null) {
var bridge =  new J.modelsetbio.APBridge (atom1, atom2, htLadders);
bridges.addLast (bridge);
if (vHBonds != null) {
var type = (isAntiparallel ? 14336 : 6144);
J.modelsetbio.AminoPolymer.addHbond (vHBonds, ap1.monomers[a + offsets[ipt]], ap2.monomers[b + offsets[++ipt]], b1[2], type, htTemp);
J.modelsetbio.AminoPolymer.addHbond (vHBonds, ap2.monomers[b + offsets[++ipt]], ap1.monomers[a + offsets[++ipt]], b2[2], type, htTemp);
}return bridge;
}return null;
}, $fz.isPrivate = true, $fz), "~A,~N,~N,~N,~N,J.util.JmolList,J.modelset.Atom,J.modelset.Atom,J.modelsetbio.AminoPolymer,J.modelsetbio.AminoPolymer,J.util.JmolList,java.util.Map,~B,java.util.Map");
c$.addHbond = $_M(c$, "addHbond", 
($fz = function (vHBonds, donor, acceptor, iEnergy, type, htTemp) {
var nitrogen = (donor).getNitrogenAtom ();
var oxygen = (acceptor).getCarbonylOxygenAtom ();
if (htTemp != null) {
var key = nitrogen.index + " " + oxygen.index;
if (htTemp.containsKey (key)) return;
htTemp.put (key, Boolean.TRUE);
}vHBonds.addLast ( new J.modelset.HBond (nitrogen, oxygen, type, 1, 0, iEnergy / 1000));
}, $fz.isPrivate = true, $fz), "J.util.JmolList,J.modelsetbio.Monomer,J.modelsetbio.Monomer,~N,~N,java.util.Map");
c$.getSheetStructures = $_M(c$, "getSheetStructures", 
($fz = function (bioPolymers, bridgesA, bridgesP, htBridges, htLadders, labels, bsDone, doReport, setStructure) {
if (bridgesA.size () == 0 && bridgesP.size () == 0) return;
J.modelsetbio.AminoPolymer.createLadders (bridgesA, htBridges, htLadders, true);
J.modelsetbio.AminoPolymer.createLadders (bridgesP, htBridges, htLadders, false);
var bsEEE =  new J.util.BS ();
var bsB =  new J.util.BS ();
for (var ladder, $ladder = htLadders.keySet ().iterator (); $ladder.hasNext () && ((ladder = $ladder.next ()) || true);) {
if (ladder[0][0] == ladder[0][1] && ladder[1][0] == ladder[1][1]) {
bsB.set (ladder[0][0]);
bsB.set (ladder[1][0]);
} else {
bsEEE.setBits (ladder[0][0], ladder[0][1] + 1);
bsEEE.setBits (ladder[1][0], ladder[1][1] + 1);
}}
var bsSheet =  new J.util.BS ();
var bsBridge =  new J.util.BS ();
for (var i = bioPolymers.length; --i >= 0; ) {
if (!(Clazz.instanceOf (bioPolymers[i], J.modelsetbio.AminoPolymer))) continue;
bsSheet.clearAll ();
bsBridge.clearAll ();
var ap = bioPolymers[i];
for (var iStart = 0; iStart < ap.monomerCount; ) {
var index = ap.monomers[iStart].leadAtomIndex;
if (bsEEE.get (index)) {
var iEnd = iStart + 1;
while (iEnd < ap.monomerCount && bsEEE.get (ap.monomers[iEnd].leadAtomIndex)) iEnd++;

bsSheet.setBits (iStart, iEnd);
iStart = iEnd;
} else {
if (bsB.get (index)) bsBridge.set (iStart);
++iStart;
}}
if (doReport) {
ap.setTag (labels[i], bsBridge, 'B');
ap.setTag (labels[i], bsSheet, 'E');
}if (setStructure) {
ap.setStructure (bsSheet, J.constant.EnumStructure.SHEET);
}bsDone[i].or (bsSheet);
bsDone[i].or (bsBridge);
}
}, $fz.isPrivate = true, $fz), "~A,J.util.JmolList,J.util.JmolList,java.util.Map,java.util.Map,~A,~A,~B,~B");
c$.createLadders = $_M(c$, "createLadders", 
($fz = function (bridges, htBridges, htLadders, isAntiparallel) {
var dir = (isAntiparallel ? -1 : 1);
var n = bridges.size ();
for (var i = 0; i < n; i++) J.modelsetbio.AminoPolymer.checkBridge (bridges.get (i), htBridges, htLadders, isAntiparallel, 1, dir);

for (var i = 0; i < n; i++) J.modelsetbio.AminoPolymer.checkBulge (bridges.get (i), htBridges, htLadders, isAntiparallel, 1);

}, $fz.isPrivate = true, $fz), "J.util.JmolList,java.util.Map,java.util.Map,~B");
c$.checkBridge = $_M(c$, "checkBridge", 
($fz = function (bridge, htBridges, htLadders, isAntiparallel, n1, n2) {
var b = htBridges.get (bridge.a.getOffsetResidueAtom ("0", n1) + "-" + bridge.b.getOffsetResidueAtom ("0", n2));
return (b != null && bridge.addBridge (b, htLadders));
}, $fz.isPrivate = true, $fz), "J.modelsetbio.APBridge,java.util.Map,java.util.Map,~B,~N,~N");
c$.checkBulge = $_M(c$, "checkBulge", 
($fz = function (bridge, htBridges, htLadders, isAntiparallel, dir) {
var dir1 = (isAntiparallel ? -1 : 1);
for (var i = 0; i < 3; i++) for (var j = (i == 0 ? 1 : 0); j < 6; j++) {
J.modelsetbio.AminoPolymer.checkBridge (bridge, htBridges, htLadders, isAntiparallel, i * dir, j * dir1);
if (j > i) J.modelsetbio.AminoPolymer.checkBridge (bridge, htBridges, htLadders, isAntiparallel, j * dir, i * dir1);
}

}, $fz.isPrivate = true, $fz), "J.modelsetbio.APBridge,java.util.Map,java.util.Map,~B,~N");
$_M(c$, "setStructure", 
($fz = function (bs, type) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var i2 = bs.nextClearBit (i);
if (i2 < 0) i2 = this.monomerCount;
this.addStructureProtected (type, null, 0, 0, i, i2 - 1);
i = i2;
}
}, $fz.isPrivate = true, $fz), "J.util.BS,J.constant.EnumStructure");
c$.isHbonded = $_M(c$, "isHbonded", 
($fz = function (indexDonor, indexAcceptor, pDonor, pAcceptor, min) {
if (indexDonor < 0 || indexAcceptor < 0) return null;
var min1 = min[pDonor];
var min2 = min[pAcceptor];
if (indexDonor >= min1.length || indexAcceptor >= min2.length) return null;
return (min1[indexDonor][0][0] == pAcceptor && min1[indexDonor][0][1] == indexAcceptor ? min1[indexDonor][0] : min1[indexDonor][1][0] == pAcceptor && min1[indexDonor][1][1] == indexAcceptor ? min1[indexDonor][1] : null);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~A");
$_M(c$, "setTag", 
($fz = function (tags, bs, ch) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) tags[i] = ch;

}, $fz.isPrivate = true, $fz), "~A,J.util.BS,~S");
$_M(c$, "dumpSummary", 
($fz = function (labels) {
var a = this.monomers[0].getLeadAtom ();
var id = a.getChainID ();
var prefix = (id == 0 ? "" : a.getChainIDStr () + ":");
var sb =  new J.util.SB ();
var lastChar = '\u0000';
var insCode1 = '\u0000';
var insCode2 = '\u0000';
var firstResno = -1;
var lastResno = -1;
for (var i = 0; i <= this.monomerCount; i++) {
if (i == this.monomerCount || labels[i] != lastChar) {
if (lastChar != '\0') sb.appendC ('\n').appendC (lastChar).append (" : ").append (prefix).appendI (firstResno).append (insCode1 == '\0' ? "" : String.valueOf (insCode1)).append ("_").append (prefix).appendI (lastResno).append (insCode2 == '\0' ? "" : String.valueOf (insCode2));
if (i == this.monomerCount) break;
lastChar = labels[i];
firstResno = this.monomers[i].getResno ();
insCode1 = this.monomers[i].getInsertionCode ();
}lastResno = this.monomers[i].getResno ();
insCode2 = this.monomers[i].getInsertionCode ();
}
return sb.toString ();
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "dumpTags", 
($fz = function (lines, bsBad, mode) {
var prefix = this.monomers[0].getLeadAtom ().getChainID () + "." + (this.bioPolymerIndexInModel + 1);
lines = J.util.TextFormat.simpleReplace (lines, "$", prefix);
var iFirst = this.monomers[0].getResno ();
var pre = "\n" + prefix;
var sb =  new J.util.SB ();
var sb0 =  new J.util.SB ().append (pre + ".8: ");
var sb1 =  new J.util.SB ().append (pre + ".7: ");
var sb2 =  new J.util.SB ().append (pre + ".6: ");
var sb3 =  new J.util.SB ().append (pre + ".0: ");
var i = iFirst;
for (var ii = 0; ii < this.monomerCount; ii++) {
i = this.monomers[ii].getResno ();
sb0.append (i % 100 == 0 ? "" + ((Clazz.doubleToInt (i / 100)) % 100) : " ");
sb1.append (i % 10 == 0 ? "" + ((Clazz.doubleToInt (i / 10)) % 10) : " ");
sb2.appendI (i % 10);
sb3.appendC (bsBad.get (this.monomers[ii].leadAtomIndex) ? '!' : this.monomers[ii].getGroup1 ());
}
if ((mode & 1) == 1) sb.appendSB (sb0).appendSB (sb1).appendSB (sb2);
sb.append ("\n");
sb.append (lines);
if ((mode & 2) == 2) {
sb.appendSB (sb3);
sb.append ("\n\n");
}return sb.toString ().$replace ('\0', '.');
}, $fz.isPrivate = true, $fz), "~S,J.util.BS,~N");
Clazz.overrideMethod (c$, "calculateStructures", 
function (alphaOnly) {
if (alphaOnly) return;
if (this.structureList == null) this.structureList = this.model.getModelSet ().getStructureList ();
var structureTags =  Clazz.newCharArray (this.monomerCount, '\0');
for (var i = 0; i < this.monomerCount - 1; ++i) {
var leadingResidue = this.monomers[i];
var trailingResidue = this.monomers[i + 1];
var phi = trailingResidue.getGroupParameter (1112539145);
var psi = leadingResidue.getGroupParameter (1112539146);
if (this.isHelix (psi, phi)) {
structureTags[i] = (phi < 0 && psi < 25 ? '4' : '3');
} else if (this.isSheet (psi, phi)) {
structureTags[i] = 's';
} else if (this.isTurn (psi, phi)) {
structureTags[i] = 't';
} else {
structureTags[i] = 'n';
}if (J.util.Logger.debugging) J.util.Logger.debug ((0 + this.monomers[0].getChainID ()) + " aminopolymer:" + i + " " + trailingResidue.getGroupParameter (1112539145) + "," + leadingResidue.getGroupParameter (1112539146) + " " + structureTags[i]);
}
for (var start = 0; start < this.monomerCount; ++start) {
if (structureTags[start] == '4') {
var end;
for (end = start + 1; end < this.monomerCount && structureTags[end] == '4'; ++end) {
}
end--;
if (end >= start + 3) {
this.addStructureProtected (J.constant.EnumStructure.HELIX, null, 0, 0, start, end);
}start = end;
}}
for (var start = 0; start < this.monomerCount; ++start) {
if (structureTags[start] == '3') {
var end;
for (end = start + 1; end < this.monomerCount && structureTags[end] == '3'; ++end) {
}
end--;
if (end >= start + 3) {
this.addStructureProtected (J.constant.EnumStructure.HELIX, null, 0, 0, start, end);
}start = end;
}}
for (var start = 0; start < this.monomerCount; ++start) {
if (structureTags[start] == 's') {
var end;
for (end = start + 1; end < this.monomerCount && structureTags[end] == 's'; ++end) {
}
end--;
if (end >= start + 2) {
this.addStructureProtected (J.constant.EnumStructure.SHEET, null, 0, 0, start, end);
}start = end;
}}
for (var start = 0; start < this.monomerCount; ++start) {
if (structureTags[start] == 't') {
var end;
for (end = start + 1; end < this.monomerCount && structureTags[end] == 't'; ++end) {
}
end--;
if (end >= start + 2) {
this.addStructureProtected (J.constant.EnumStructure.TURN, null, 0, 0, start, end);
}start = end;
}}
}, "~B");
$_M(c$, "isTurn", 
($fz = function (psi, phi) {
return J.modelsetbio.AminoPolymer.checkPhiPsi (this.structureList.get (J.constant.EnumStructure.TURN), psi, phi);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "isSheet", 
($fz = function (psi, phi) {
return J.modelsetbio.AminoPolymer.checkPhiPsi (this.structureList.get (J.constant.EnumStructure.SHEET), psi, phi);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "isHelix", 
($fz = function (psi, phi) {
return J.modelsetbio.AminoPolymer.checkPhiPsi (this.structureList.get (J.constant.EnumStructure.HELIX), psi, phi);
}, $fz.isPrivate = true, $fz), "~N,~N");
c$.checkPhiPsi = $_M(c$, "checkPhiPsi", 
($fz = function (list, psi, phi) {
for (var i = 0; i < list.length; i += 4) if (phi >= list[i] && phi <= list[i + 1] && psi >= list[i + 2] && psi <= list[i + 3]) return true;

return false;
}, $fz.isPrivate = true, $fz), "~A,~N,~N");
Clazz.overrideMethod (c$, "setStructureList", 
function (structureList) {
this.structureList = structureList;
}, "java.util.Map");
Clazz.defineStatics (c$,
"maxHbondAlphaDistance", 9,
"maxHbondAlphaDistance2", 81.0,
"minimumHbondDistance2", 0.25,
"sheetOffsets", [[0, -1, 1, 0, 1, 0, 0, -1], [0, 0, 0, 0, 1, -1, 1, -1]]);
});
