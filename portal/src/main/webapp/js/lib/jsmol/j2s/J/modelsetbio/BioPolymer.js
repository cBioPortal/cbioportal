Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.util.V3"], "J.modelsetbio.BioPolymer", ["java.lang.Float", "java.util.Hashtable", "J.modelset.LabelToken", "J.util.BS", "$.Escape", "$.JmolList", "$.Logger", "$.P3", "$.Quaternion", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.monomers = null;
this.model = null;
this.leadMidpoints = null;
this.leadPoints = null;
this.controlPoints = null;
this.wingVectors = null;
this.leadAtomIndices = null;
this.type = 0;
this.bioPolymerIndexInModel = 0;
this.monomerCount = 0;
this.invalidLead = false;
this.invalidControl = false;
this.sheetSmoothing = 0;
this.hasWingPoints = false;
this.reversed = null;
this.twistedSheets = false;
this.unitVectorX = null;
this.selectedMonomerCount = 0;
this.bsSelectedMonomers = null;
this.haveParameters = false;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "BioPolymer");
Clazz.prepareFields (c$, function () {
this.unitVectorX = J.util.V3.new3 (1, 0, 0);
});
$_M(c$, "getGroups", 
function () {
return this.monomers;
});
Clazz.makeConstructor (c$, 
function (monomers) {
this.monomers = monomers;
this.monomerCount = monomers.length;
for (var i = this.monomerCount; --i >= 0; ) monomers[i].setBioPolymer (this, i);

this.model = monomers[0].getModel ();
}, "~A");
$_M(c$, "getRange", 
function (bs) {
if (this.monomerCount == 0) return;
bs.setBits (this.monomers[0].firstAtomIndex, this.monomers[this.monomerCount - 1].lastAtomIndex + 1);
}, "J.util.BS");
$_M(c$, "clearStructures", 
function () {
for (var i = 0; i < this.monomerCount; i++) this.monomers[i].setStructure (null);

});
$_M(c$, "removeProteinStructure", 
function (monomerIndex, count) {
var m = this.monomers[monomerIndex];
var type = m.getProteinStructureType ();
var mLast = -1;
for (var i = 0, pt = monomerIndex; i < count && pt < this.monomerCount; i++, pt++) {
this.monomers[pt].setStructure (null);
mLast = this.monomers[pt].setProteinStructureType (type, mLast);
}
}, "~N,~N");
$_M(c$, "getLeadAtomIndices", 
function () {
if (this.leadAtomIndices == null) {
this.leadAtomIndices =  Clazz.newIntArray (this.monomerCount, 0);
this.invalidLead = true;
}if (this.invalidLead) {
for (var i = this.monomerCount; --i >= 0; ) this.leadAtomIndices[i] = this.monomers[i].leadAtomIndex;

this.invalidLead = false;
}return this.leadAtomIndices;
});
$_M(c$, "getIndex", 
function (chainID, seqcode, istart, iend) {
var i;
for (i = this.monomerCount; --i >= 0; ) {
var m = this.monomers[i];
if (m.chain.chainID == chainID && m.seqcode == seqcode && (istart < 0 || istart == m.firstAtomIndex || iend == m.lastAtomIndex)) break;
}
return i;
}, "~N,~N,~N,~N");
$_M(c$, "getLeadPoint", 
function (monomerIndex) {
return this.monomers[monomerIndex].getLeadAtom ();
}, "~N");
$_M(c$, "getInitiatorPoint", 
($fz = function () {
return this.monomers[0].getInitiatorAtom ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "getTerminatorPoint", 
($fz = function () {
return this.monomers[this.monomerCount - 1].getTerminatorAtom ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "getLeadMidPoint", 
function (groupIndex, midPoint) {
if (groupIndex == this.monomerCount) {
--groupIndex;
} else if (groupIndex > 0) {
midPoint.setT (this.getLeadPoint (groupIndex));
midPoint.add (this.getLeadPoint (groupIndex - 1));
midPoint.scale (0.5);
return;
}midPoint.setT (this.getLeadPoint (groupIndex));
}, "~N,J.util.P3");
$_M(c$, "getWingPoint", 
function (polymerIndex) {
return this.monomers[polymerIndex].getWingAtom ();
}, "~N");
$_M(c$, "getConformation", 
function (bsConformation, conformationIndex) {
var atoms = this.model.getModelSet ().atoms;
for (var i = this.monomerCount; --i >= 0; ) this.monomers[i].getConformation (atoms, bsConformation, conformationIndex);

this.recalculateLeadMidpointsAndWingVectors ();
}, "J.util.BS,~N");
$_M(c$, "setConformation", 
function (bsSelected) {
var atoms = this.model.getModelSet ().atoms;
for (var i = this.monomerCount; --i >= 0; ) this.monomers[i].updateOffsetsForAlternativeLocations (atoms, bsSelected);

this.recalculateLeadMidpointsAndWingVectors ();
}, "J.util.BS");
$_M(c$, "recalculateLeadMidpointsAndWingVectors", 
function () {
this.invalidLead = this.invalidControl = true;
this.getLeadAtomIndices ();
this.resetHydrogenPoints ();
this.calcLeadMidpointsAndWingVectors ();
});
$_M(c$, "resetHydrogenPoints", 
function () {
});
$_M(c$, "getLeadMidpoints", 
function () {
if (this.leadMidpoints == null) this.calcLeadMidpointsAndWingVectors ();
return this.leadMidpoints;
});
$_M(c$, "getLeadPoints", 
function () {
if (this.leadPoints == null) this.calcLeadMidpointsAndWingVectors ();
return this.leadPoints;
});
$_M(c$, "getControlPoints", 
function (isTraceAlpha, sheetSmoothing, invalidate) {
if (invalidate) this.invalidControl = true;
return (!isTraceAlpha ? this.leadMidpoints : sheetSmoothing == 0 ? this.leadPoints : this.getControlPoints2 (sheetSmoothing));
}, "~B,~N,~B");
$_M(c$, "getControlPoints2", 
($fz = function (sheetSmoothing) {
if (!this.invalidControl && sheetSmoothing == this.sheetSmoothing) return this.controlPoints;
this.getLeadPoints ();
var v =  new J.util.V3 ();
if (this.controlPoints == null) this.controlPoints =  new Array (this.monomerCount + 1);
if (!Float.isNaN (sheetSmoothing)) this.sheetSmoothing = sheetSmoothing;
for (var i = 0; i < this.monomerCount; i++) this.controlPoints[i] = this.getControlPoint (i, v);

this.controlPoints[this.monomerCount] = this.getTerminatorPoint ();
this.invalidControl = false;
return this.controlPoints;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getControlPoint", 
function (i, v) {
return this.leadPoints[i];
}, "~N,J.util.V3");
$_M(c$, "getWingVectors", 
function () {
if (this.leadMidpoints == null) this.calcLeadMidpointsAndWingVectors ();
return this.wingVectors;
});
$_M(c$, "calcLeadMidpointsAndWingVectors", 
($fz = function () {
if (this.leadMidpoints == null) {
this.leadMidpoints =  new Array (this.monomerCount + 1);
this.leadPoints =  new Array (this.monomerCount + 1);
this.wingVectors =  new Array (this.monomerCount + 1);
this.sheetSmoothing = 1.4E-45;
}if (this.reversed == null) this.reversed = J.util.BS.newN (this.monomerCount);
 else this.reversed.clearAll ();
this.twistedSheets = this.model.modelSet.viewer.getBoolean (603979968);
var vectorA =  new J.util.V3 ();
var vectorB =  new J.util.V3 ();
var vectorC =  new J.util.V3 ();
var vectorD =  new J.util.V3 ();
var leadPointPrev;
var leadPoint;
this.leadMidpoints[0] = this.getInitiatorPoint ();
this.leadPoints[0] = leadPoint = this.getLeadPoint (0);
var previousVectorD = null;
for (var i = 1; i < this.monomerCount; ++i) {
leadPointPrev = leadPoint;
this.leadPoints[i] = leadPoint = this.getLeadPoint (i);
var midpoint = J.util.P3.newP (leadPoint);
midpoint.add (leadPointPrev);
midpoint.scale (0.5);
this.leadMidpoints[i] = midpoint;
if (this.hasWingPoints) {
vectorA.sub2 (leadPoint, leadPointPrev);
vectorB.sub2 (leadPointPrev, this.getWingPoint (i - 1));
vectorC.cross (vectorA, vectorB);
vectorD.cross (vectorA, vectorC);
vectorD.normalize ();
if (!this.twistedSheets && previousVectorD != null && previousVectorD.angle (vectorD) > 1.5707963267948966) {
this.reversed.set (i);
vectorD.scale (-1);
}previousVectorD = this.wingVectors[i] = J.util.V3.newV (vectorD);
}}
this.leadPoints[this.monomerCount] = this.leadMidpoints[this.monomerCount] = this.getTerminatorPoint ();
if (!this.hasWingPoints) {
if (this.monomerCount < 3) {
this.wingVectors[1] = this.unitVectorX;
} else {
var previousVectorC = null;
for (var i = 1; i < this.monomerCount; ++i) {
vectorA.sub2 (this.leadMidpoints[i], this.leadPoints[i]);
vectorB.sub2 (this.leadPoints[i], this.leadMidpoints[i + 1]);
vectorC.cross (vectorA, vectorB);
vectorC.normalize ();
if (previousVectorC != null && previousVectorC.angle (vectorC) > 1.5707963267948966) vectorC.scale (-1);
previousVectorC = this.wingVectors[i] = J.util.V3.newV (vectorC);
}
}}this.wingVectors[0] = this.wingVectors[1];
this.wingVectors[this.monomerCount] = this.wingVectors[this.monomerCount - 1];
}, $fz.isPrivate = true, $fz));
$_M(c$, "findNearestAtomIndex", 
function (xMouse, yMouse, closest, mads, myVisibilityFlag, bsNot) {
for (var i = this.monomerCount; --i >= 0; ) {
if ((this.monomers[i].shapeVisibilityFlags & myVisibilityFlag) == 0) continue;
var a = this.monomers[i].getLeadAtom ();
if (!a.isVisible (0) || bsNot != null && bsNot.get (a.index)) continue;
if (mads[i] > 0 || mads[i + 1] > 0) this.monomers[i].findNearestAtomIndex (xMouse, yMouse, closest, mads[i], mads[i + 1]);
}
}, "~N,~N,~A,~A,~N,J.util.BS");
$_M(c$, "getSelectedMonomerCount", 
function () {
return this.selectedMonomerCount;
});
$_M(c$, "calcSelectedMonomersCount", 
function (bsSelected) {
this.selectedMonomerCount = 0;
if (this.bsSelectedMonomers == null) this.bsSelectedMonomers =  new J.util.BS ();
this.bsSelectedMonomers.clearAll ();
for (var i = 0; i < this.monomerCount; i++) {
if (this.monomers[i].isSelected (bsSelected)) {
++this.selectedMonomerCount;
this.bsSelectedMonomers.set (i);
}}
}, "J.util.BS");
$_M(c$, "isMonomerSelected", 
function (i) {
return (i >= 0 && this.bsSelectedMonomers.get (i));
}, "~N");
$_M(c$, "getPolymerPointsAndVectors", 
function (last, bs, vList, isTraceAlpha, sheetSmoothing) {
var points = this.getControlPoints (isTraceAlpha, sheetSmoothing, false);
var vectors = this.getWingVectors ();
var count = this.monomerCount;
for (var j = 0; j < count; j++) if (bs.get (this.monomers[j].leadAtomIndex)) {
vList.addLast ([points[j], J.util.P3.newP (vectors[j])]);
last = j;
} else if (last != 2147483646) {
vList.addLast ([points[j], J.util.P3.newP (vectors[j])]);
last = 2147483646;
}
if (last + 1 < count) vList.addLast ([points[last + 1], J.util.P3.newP (vectors[last + 1])]);
return last;
}, "~N,J.util.BS,J.util.JmolList,~B,~N");
$_M(c$, "getSequence", 
function () {
var buf =  Clazz.newCharArray (this.monomerCount, '\0');
for (var i = 0; i < this.monomerCount; i++) buf[i] = this.monomers[i].getGroup1 ();

return String.valueOf (buf);
});
$_M(c$, "getPolymerInfo", 
function (bs) {
var returnInfo =  new java.util.Hashtable ();
var info =  new J.util.JmolList ();
var structureInfo = null;
var ps;
var psLast = null;
var n = 0;
for (var i = 0; i < this.monomerCount; i++) {
if (bs.get (this.monomers[i].leadAtomIndex)) {
var monomerInfo = this.monomers[i].getMyInfo ();
monomerInfo.put ("monomerIndex", Integer.$valueOf (i));
info.addLast (monomerInfo);
if ((ps = this.getProteinStructure (i)) != null && ps !== psLast) {
var psInfo =  new java.util.Hashtable ();
(psLast = ps).getInfo (psInfo);
if (structureInfo == null) {
structureInfo =  new J.util.JmolList ();
}psInfo.put ("index", Integer.$valueOf (n++));
structureInfo.addLast (psInfo);
}}}
if (info.size () > 0) {
returnInfo.put ("sequence", this.getSequence ());
returnInfo.put ("monomers", info);
if (structureInfo != null) returnInfo.put ("structures", structureInfo);
}return returnInfo;
}, "J.util.BS");
$_M(c$, "getPolymerSequenceAtoms", 
function (group1, nGroups, bsInclude, bsResult) {
for (var i = Math.min (this.monomerCount, group1 + nGroups); --i >= group1; ) this.monomers[i].getMonomerSequenceAtoms (bsInclude, bsResult);

}, "~N,~N,J.util.BS,J.util.BS");
$_M(c$, "getProteinStructure", 
function (monomerIndex) {
return this.monomers[monomerIndex].getProteinStructure ();
}, "~N");
$_M(c$, "calcParameters", 
function () {
this.haveParameters = true;
return this.calcEtaThetaAngles () || this.calcPhiPsiAngles ();
});
$_M(c$, "calcEtaThetaAngles", 
function () {
return false;
});
$_M(c$, "calcPhiPsiAngles", 
function () {
return false;
});
c$.getPdbData = $_M(c$, "getPdbData", 
function (viewer, p, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten) {
var calcRamachandranStraightness = (qtype == 'C' || qtype == 'P');
var isRamachandran = (ctype == 'R' || ctype == 'S' && calcRamachandranStraightness);
if (isRamachandran && !p.calcPhiPsiAngles ()) return;
var isAmino = (Clazz.instanceOf (p, J.modelsetbio.AminoPolymer));
var isRelativeAlias = (ctype == 'r');
var quaternionStraightness = (!isRamachandran && ctype == 'S');
if (derivType == 2 && isRelativeAlias) ctype = 'w';
if (quaternionStraightness) derivType = 2;
var useQuaternionStraightness = (ctype == 'S');
var writeRamachandranStraightness = ("rcpCP".indexOf (qtype) >= 0);
if (J.util.Logger.debugging && (quaternionStraightness || calcRamachandranStraightness)) {
J.util.Logger.debug ("For straightness calculation: useQuaternionStraightness = " + useQuaternionStraightness + " and quaternionFrame = " + qtype);
}if (addHeader && !isDraw) {
pdbATOM.append ("REMARK   6    AT GRP CH RESNO  ");
switch (ctype) {
default:
case 'w':
pdbATOM.append ("x*10___ y*10___ z*10___      w*10__       ");
break;
case 'x':
pdbATOM.append ("y*10___ z*10___ w*10___      x*10__       ");
break;
case 'y':
pdbATOM.append ("z*10___ w*10___ x*10___      y*10__       ");
break;
case 'z':
pdbATOM.append ("w*10___ x*10___ y*10___      z*10__       ");
break;
case 'R':
if (writeRamachandranStraightness) pdbATOM.append ("phi____ psi____ theta         Straightness");
 else pdbATOM.append ("phi____ psi____ omega-180    PartialCharge");
break;
}
pdbATOM.append ("    Sym   q0_______ q1_______ q2_______ q3_______");
pdbATOM.append ("  theta_  aaX_______ aaY_______ aaZ_______");
if (ctype != 'R') pdbATOM.append ("  centerX___ centerY___ centerZ___");
if (qtype == 'n') pdbATOM.append ("  NHX_______ NHY_______ NHZ_______");
pdbATOM.append ("\n\n");
}var factor = (ctype == 'R' ? 1 : 10);
bothEnds = false;
for (var j = 0; j < (bothEnds ? 2 : 1); j++, factor *= -1) for (var i = 0; i < (mStep < 1 ? 1 : mStep); i++) J.modelsetbio.BioPolymer.getData (viewer, i, mStep, p, ctype, qtype, derivType, bsAtoms, bsSelected, isDraw, isRamachandran, calcRamachandranStraightness, useQuaternionStraightness, writeRamachandranStraightness, quaternionStraightness, factor, isAmino, isRelativeAlias, tokens, pdbATOM, pdbCONECT, bsWritten);


}, "J.viewer.Viewer,J.modelsetbio.BioPolymer,~S,~S,~N,~N,J.util.BS,J.util.BS,~B,~B,~B,~A,J.io.OutputStringBuilder,J.util.SB,J.util.BS");
c$.getData = $_M(c$, "getData", 
($fz = function (viewer, m0, mStep, p, ctype, qtype, derivType, bsAtoms, bsSelected, isDraw, isRamachandran, calcRamachandranStraightness, useQuaternionStraightness, writeRamachandranStraightness, quaternionStraightness, factor, isAmino, isRelativeAlias, tokens, pdbATOM, pdbCONECT, bsWritten) {
var prefix = (derivType > 0 ? "dq" + (derivType == 2 ? "2" : "") : "q");
var q;
var aprev = null;
var qprev = null;
var dq = null;
var dqprev = null;
var qref = null;
var atomLast = null;
var x = 0;
var y = 0;
var z = 0;
var w = 0;
var strExtra = "";
var val1 = NaN;
var val2 = NaN;
var pt = (isDraw ?  new J.util.P3 () : null);
var dm = (mStep <= 1 ? 1 : mStep);
for (var m = m0; m < p.monomerCount; m += dm) {
var monomer = p.monomers[m];
if (bsAtoms == null || bsAtoms.get (monomer.leadAtomIndex)) {
var a = monomer.getLeadAtom ();
var id = monomer.getUniqueID ();
if (isRamachandran) {
if (ctype == 'S') monomer.setGroupParameter (1112539150, NaN);
x = monomer.getGroupParameter (1112539145);
y = monomer.getGroupParameter (1112539146);
z = monomer.getGroupParameter (1112539144);
if (z < -90) z += 360;
z -= 180;
if (Float.isNaN (x) || Float.isNaN (y) || Float.isNaN (z)) {
if (bsAtoms != null) bsAtoms.clear (a.getIndex ());
continue;
}var angledeg = (writeRamachandranStraightness ? p.calculateRamachandranHelixAngle (m, qtype) : 0);
var straightness = (calcRamachandranStraightness || writeRamachandranStraightness ? J.modelsetbio.BioPolymer.getStraightness (Math.cos (angledeg / 2 / 180 * 3.141592653589793)) : 0);
if (ctype == 'S') {
monomer.setGroupParameter (1112539150, straightness);
continue;
}if (isDraw) {
if (bsSelected != null && !bsSelected.get (a.getIndex ())) continue;
var aa = monomer;
pt.set (-x, x, 0.5);
pdbATOM.append ("draw ID \"phi").append (id).append ("\" ARROW ARC ").append (J.util.Escape.eP (aa.getNitrogenAtom ())).append (J.util.Escape.eP (a)).append (J.util.Escape.eP (aa.getCarbonylCarbonAtom ())).append (J.util.Escape.eP (pt)).append (" \"phi = ").append (String.valueOf (Math.round (x))).append ("\" color ").append (J.modelsetbio.BioPolymer.qColor[2]).append ("\n");
pt.set (0, y, 0.5);
pdbATOM.append ("draw ID \"psi").append (id).append ("\" ARROW ARC ").append (J.util.Escape.eP (a)).append (J.util.Escape.eP (aa.getCarbonylCarbonAtom ())).append (J.util.Escape.eP (aa.getNitrogenAtom ())).append (J.util.Escape.eP (pt)).append (" \"psi = ").append (String.valueOf (Math.round (y))).append ("\" color ").append (J.modelsetbio.BioPolymer.qColor[1]).append ("\n");
pdbATOM.append ("draw ID \"planeNCC").append (id).append ("\" ").append (J.util.Escape.eP (aa.getNitrogenAtom ())).append (J.util.Escape.eP (a)).append (J.util.Escape.eP (aa.getCarbonylCarbonAtom ())).append (" color ").append (J.modelsetbio.BioPolymer.qColor[0]).append ("\n");
pdbATOM.append ("draw ID \"planeCNC").append (id).append ("\" ").append (J.util.Escape.eP ((p.monomers[m - 1]).getCarbonylCarbonAtom ())).append (J.util.Escape.eP (aa.getNitrogenAtom ())).append (J.util.Escape.eP (a)).append (" color ").append (J.modelsetbio.BioPolymer.qColor[1]).append ("\n");
pdbATOM.append ("draw ID \"planeCCN").append (id).append ("\" ").append (J.util.Escape.eP (a)).append (J.util.Escape.eP (aa.getCarbonylCarbonAtom ())).append (J.util.Escape.eP ((p.monomers[m + 1]).getNitrogenAtom ())).append (" color ").append (J.modelsetbio.BioPolymer.qColor[2]).append ("\n");
continue;
}if (Float.isNaN (angledeg)) {
strExtra = "";
if (writeRamachandranStraightness) continue;
} else {
q = J.util.Quaternion.newVA (J.util.P3.new3 (1, 0, 0), angledeg);
strExtra = q.getInfo ();
if (writeRamachandranStraightness) {
z = angledeg;
w = straightness;
} else {
w = a.getPartialCharge ();
}}} else {
q = monomer.getQuaternion (qtype);
if (q != null) {
q.setRef (qref);
qref = J.util.Quaternion.newQ (q);
}if (derivType == 2) monomer.setGroupParameter (1112539150, NaN);
if (q == null) {
qprev = null;
qref = null;
} else if (derivType > 0) {
var anext = a;
var qnext = q;
if (qprev == null) {
q = null;
dqprev = null;
} else {
if (isRelativeAlias) {
dq = qprev.leftDifference (q);
} else {
dq = q.rightDifference (qprev);
}if (derivType == 1) {
q = dq;
} else if (dqprev == null) {
q = null;
} else {
q = dq.rightDifference (dqprev);
val1 = J.modelsetbio.BioPolymer.getQuaternionStraightness (id, dqprev, dq);
val2 = J.modelsetbio.BioPolymer.get3DStraightness (id, dqprev, dq);
aprev.getGroup ().setGroupParameter (1112539150, useQuaternionStraightness ? val1 : val2);
}dqprev = dq;
}aprev = anext;
qprev = qnext;
}if (q == null) {
atomLast = null;
continue;
}switch (ctype) {
default:
x = q.q1;
y = q.q2;
z = q.q3;
w = q.q0;
break;
case 'x':
x = q.q0;
y = q.q1;
z = q.q2;
w = q.q3;
break;
case 'y':
x = q.q3;
y = q.q0;
z = q.q1;
w = q.q2;
break;
case 'z':
x = q.q2;
y = q.q3;
z = q.q0;
w = q.q1;
break;
}
var ptCenter = monomer.getQuaternionFrameCenter (qtype);
if (ptCenter == null) ptCenter =  new J.util.P3 ();
if (isDraw) {
if (bsSelected != null && !bsSelected.get (a.getIndex ())) continue;
var deg = Clazz.doubleToInt (Math.floor (Math.acos (w) * 360 / 3.141592653589793));
if (derivType == 0) {
pdbATOM.append (q.draw (prefix, id, ptCenter, 1));
if (qtype == 'n' && isAmino) {
var ptH = (monomer).getNitrogenHydrogenPoint ();
if (ptH != null) pdbATOM.append ("draw ID \"").append (prefix).append ("nh").append (id).append ("\" width 0.1 ").append (J.util.Escape.eP (ptH)).append ("\n");
}}if (derivType == 1) {
pdbATOM.append (monomer.getHelixData (135176, qtype, mStep)).append ("\n");
continue;
}pt.set (x * 2, y * 2, z * 2);
pdbATOM.append ("draw ID \"").append (prefix).append ("a").append (id).append ("\" VECTOR ").append (J.util.Escape.eP (ptCenter)).append (J.util.Escape.eP (pt)).append (" \">").append (String.valueOf (deg)).append ("\" color ").append (J.modelsetbio.BioPolymer.qColor[derivType]).append ("\n");
continue;
}strExtra = q.getInfo () + J.util.TextFormat.sprintf ("  %10.5p %10.5p %10.5p", "p", [ptCenter]);
if (qtype == 'n' && isAmino) {
strExtra += J.util.TextFormat.sprintf ("  %10.5p %10.5p %10.5p", "p", [(monomer).getNitrogenHydrogenPoint ()]);
} else if (derivType == 2 && !Float.isNaN (val1)) {
strExtra += J.util.TextFormat.sprintf (" %10.5f %10.5f", "F", [[val1, val2]]);
}}if (pdbATOM == null) continue;
bsWritten.set ((a.getGroup ()).leadAtomIndex);
pdbATOM.append (J.modelset.LabelToken.formatLabelAtomArray (viewer, a, tokens, '\0', null));
pdbATOM.append (J.util.TextFormat.sprintf ("%8.2f%8.2f%8.2f      %6.3f          %2s    %s\n", "ssF", [a.getElementSymbolIso (false).toUpperCase (), strExtra, [x * factor, y * factor, z * factor, w * factor]]));
if (atomLast != null && atomLast.getPolymerIndexInModel () == a.getPolymerIndexInModel ()) {
pdbCONECT.append ("CONECT").append (J.util.TextFormat.formatStringI ("%5i", "i", atomLast.getAtomNumber ())).append (J.util.TextFormat.formatStringI ("%5i", "i", a.getAtomNumber ())).appendC ('\n');
}atomLast = a;
}}
}, $fz.isPrivate = true, $fz), "J.viewer.Viewer,~N,~N,J.modelsetbio.BioPolymer,~S,~S,~N,J.util.BS,J.util.BS,~B,~B,~B,~B,~B,~B,~N,~B,~B,~A,J.io.OutputStringBuilder,J.util.SB,J.util.BS");
$_M(c$, "calculateRamachandranHelixAngle", 
function (m, qtype) {
return NaN;
}, "~N,~S");
c$.get3DStraightness = $_M(c$, "get3DStraightness", 
($fz = function (id, dq, dqnext) {
return dq.getNormal ().dot (dqnext.getNormal ());
}, $fz.isPrivate = true, $fz), "~S,J.util.Quaternion,J.util.Quaternion");
c$.getQuaternionStraightness = $_M(c$, "getQuaternionStraightness", 
($fz = function (id, dq, dqnext) {
return J.modelsetbio.BioPolymer.getStraightness (dq.dot (dqnext));
}, $fz.isPrivate = true, $fz), "~S,J.util.Quaternion,J.util.Quaternion");
c$.getStraightness = $_M(c$, "getStraightness", 
($fz = function (cosHalfTheta) {
return (1 - 2 * Math.acos (Math.abs (cosHalfTheta)) / 3.141592653589793);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "isDna", 
function () {
return (this.monomerCount > 0 && this.monomers[0].isDna ());
});
$_M(c$, "isRna", 
function () {
return (this.monomerCount > 0 && this.monomers[0].isRna ());
});
$_M(c$, "getRangeGroups", 
function (nResidues, bsAtoms, bsResult) {
var bsTemp =  new J.util.BS ();
for (var i = 0; i < this.monomerCount; i++) {
if (!this.monomers[i].isSelected (bsAtoms)) continue;
bsTemp.setBits (Math.max (0, i - nResidues), i + nResidues + 1);
i += nResidues - 1;
}
for (var i = bsTemp.nextSetBit (0); i >= 0 && i < this.monomerCount; i = bsTemp.nextSetBit (i + 1)) this.monomers[i].selectAtoms (bsResult);

}, "~N,J.util.BS,J.util.BS");
$_M(c$, "calculateDssp", 
function (bioPolymers, bioPolymerCount, vHBonds, doReport, dsspIgnoreHydrogens, setStructure) {
return J.modelsetbio.AminoPolymer.calculateStructuresDssp (bioPolymers, bioPolymerCount, vHBonds, doReport, dsspIgnoreHydrogens, setStructure);
}, "~A,~N,J.util.JmolList,~B,~B,~B");
$_M(c$, "calcRasmolHydrogenBonds", 
function (polymer, bsA, bsB, vHBonds, nMaxPerResidue, min, checkDistances, dsspIgnoreHydrogens) {
}, "J.modelsetbio.BioPolymer,J.util.BS,J.util.BS,J.util.JmolList,~N,~A,~B,~B");
$_M(c$, "setStructureList", 
function (structureList) {
}, "java.util.Map");
$_M(c$, "getPdbData", 
function (viewer, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten) {
return;
}, "J.viewer.Viewer,~S,~S,~N,~N,J.util.BS,J.util.BS,~B,~B,~B,~A,J.io.OutputStringBuilder,J.util.SB,J.util.BS");
$_M(c$, "getType", 
function () {
return this.type;
});
$_M(c$, "calculateStruts", 
function (modelSet, bs1, bs2, vCA, thresh, delta, allowMultiple) {
return null;
}, "J.modelset.ModelSet,J.util.BS,J.util.BS,J.util.JmolList,~N,~N,~B");
Clazz.defineStatics (c$,
"TYPE_NOBONDING", 0,
"TYPE_AMINO", 1,
"TYPE_NUCLEIC", 2,
"TYPE_CARBOHYDRATE", 3,
"qColor", ["yellow", "orange", "purple"]);
});
