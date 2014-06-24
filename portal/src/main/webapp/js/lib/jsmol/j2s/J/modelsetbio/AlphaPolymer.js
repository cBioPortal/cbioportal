Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["java.lang.Enum", "J.modelsetbio.BioPolymer"], "J.modelsetbio.AlphaPolymer", ["J.constant.EnumStructure", "J.modelsetbio.Helix", "$.Sheet", "$.Turn", "J.util.BS", "$.JmolList", "$.Logger", "$.Measure", "$.P3"], function () {
c$ = Clazz.declareType (J.modelsetbio, "AlphaPolymer", J.modelsetbio.BioPolymer);
Clazz.overrideMethod (c$, "getControlPoint", 
function (i, v) {
if (!this.monomers[i].isSheet ()) return this.leadPoints[i];
v.sub2 (this.leadMidpoints[i], this.leadPoints[i]);
v.scale (this.sheetSmoothing);
var pt = J.util.P3.newP (this.leadPoints[i]);
pt.add (v);
return pt;
}, "~N,J.util.V3");
$_M(c$, "getPdbData", 
function (viewer, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten) {
J.modelsetbio.BioPolymer.getPdbData (viewer, this, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten);
}, "J.viewer.Viewer,~S,~S,~N,~N,J.util.BS,J.util.BS,~B,~B,~B,~A,J.io.OutputStringBuilder,J.util.SB,J.util.BS");
$_M(c$, "addStructure", 
function (type, structureID, serialID, strandCount, startChainID, startSeqcode, endChainID, endSeqcode, istart, iend, bsAssigned) {
var i0 = -1;
var i1 = -1;
if (istart < iend) {
if (this.monomers[0].firstAtomIndex > iend || this.monomers[this.monomerCount - 1].lastAtomIndex < istart) return;
i0 = istart;
i1 = iend;
}var indexStart;
var indexEnd;
if ((indexStart = this.getIndex (startChainID, startSeqcode, i0, i1)) == -1 || (indexEnd = this.getIndex (endChainID, endSeqcode, i0, i1)) == -1) return;
if (istart >= 0 && bsAssigned != null) {
var pt = bsAssigned.nextSetBit (this.monomers[indexStart].firstAtomIndex);
if (pt >= 0 && pt < this.monomers[indexEnd].lastAtomIndex) return;
}this.addStructureProtected (type, structureID, serialID, strandCount, indexStart, indexEnd);
if (istart >= 0) bsAssigned.setBits (istart, iend + 1);
}, "J.constant.EnumStructure,~S,~N,~N,~N,~N,~N,~N,~N,~N,J.util.BS");
$_M(c$, "addStructureProtected", 
function (type, structureID, serialID, strandCount, indexStart, indexEnd) {
if (indexEnd < indexStart) {
J.util.Logger.error ("AlphaPolymer:addSecondaryStructure error:  indexStart:" + indexStart + " indexEnd:" + indexEnd);
return;
}var structureCount = indexEnd - indexStart + 1;
var proteinstructure = null;
switch (type) {
case J.constant.EnumStructure.HELIX:
case J.constant.EnumStructure.HELIXALPHA:
case J.constant.EnumStructure.HELIX310:
case J.constant.EnumStructure.HELIXPI:
proteinstructure =  new J.modelsetbio.Helix (this, indexStart, structureCount, type);
break;
case J.constant.EnumStructure.SHEET:
proteinstructure =  new J.modelsetbio.Sheet (this, indexStart, structureCount, type);
break;
case J.constant.EnumStructure.TURN:
proteinstructure =  new J.modelsetbio.Turn (this, indexStart, structureCount);
break;
default:
J.util.Logger.error ("unrecognized secondary structure type");
return;
}
proteinstructure.structureID = structureID;
proteinstructure.serialID = serialID;
proteinstructure.strandCount = strandCount;
for (var i = indexStart; i <= indexEnd; ++i) {
this.monomers[i].setStructure (proteinstructure);
}
}, "J.constant.EnumStructure,~S,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "calculateStruts", 
function (modelSet, bs1, bs2, vCA, thresh, delta, allowMultiple) {
return this.calculateStrutsStatic (modelSet, bs1, bs2, vCA, thresh, delta, allowMultiple);
}, "J.modelset.ModelSet,J.util.BS,J.util.BS,J.util.JmolList,~N,~N,~B");
$_M(c$, "calculateStrutsStatic", 
($fz = function (modelSet, bs1, bs2, vCA, thresh, delta, allowMultiple) {
var vStruts =  new J.util.JmolList ();
var thresh2 = thresh * thresh;
var n = vCA.size ();
var nEndMin = 3;
var bsStruts =  new J.util.BS ();
var bsNotAvailable =  new J.util.BS ();
var bsNearbyResidues =  new J.util.BS ();
var a1 = vCA.get (0);
var a2;
var nBiopolymers = modelSet.getBioPolymerCountInModel (a1.modelIndex);
var biopolymerStartsEnds =  Clazz.newIntArray (nBiopolymers, nEndMin * 2, 0);
for (var i = 0; i < n; i++) {
a1 = vCA.get (i);
var polymerIndex = a1.getPolymerIndexInModel ();
var monomerIndex = a1.getMonomerIndex ();
var bpt = monomerIndex;
if (bpt < nEndMin) biopolymerStartsEnds[polymerIndex][bpt] = i + 1;
bpt = (a1.getGroup ()).getBioPolymerLength () - monomerIndex - 1;
if (bpt < nEndMin) biopolymerStartsEnds[polymerIndex][nEndMin + bpt] = i + 1;
}
var d2 =  Clazz.newFloatArray (Clazz.doubleToInt (n * (n - 1) / 2), 0);
for (var i = 0; i < n; i++) {
a1 = vCA.get (i);
for (var j = i + 1; j < n; j++) {
var ipt = J.modelsetbio.AlphaPolymer.strutPoint (i, j, n);
a2 = vCA.get (j);
var resno1 = a1.getResno ();
var polymerIndex1 = a1.getPolymerIndexInModel ();
var resno2 = a2.getResno ();
var polymerIndex2 = a2.getPolymerIndexInModel ();
if (polymerIndex1 == polymerIndex2 && Math.abs (resno2 - resno1) < delta) bsNearbyResidues.set (ipt);
var d = d2[ipt] = a1.distanceSquared (a2);
if (d >= thresh2) bsNotAvailable.set (ipt);
}
}
for (var t = 5; --t >= 0; ) {
thresh2 = (thresh - t) * (thresh - t);
for (var i = 0; i < n; i++) if (allowMultiple || !bsStruts.get (i)) for (var j = i + 1; j < n; j++) {
var ipt = J.modelsetbio.AlphaPolymer.strutPoint (i, j, n);
if (!bsNotAvailable.get (ipt) && !bsNearbyResidues.get (ipt) && (allowMultiple || !bsStruts.get (j)) && d2[ipt] <= thresh2) J.modelsetbio.AlphaPolymer.setStrut (i, j, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
}

}
for (var b = 0; b < nBiopolymers; b++) {
for (var k = 0; k < nEndMin * 2; k++) {
var i = biopolymerStartsEnds[b][k] - 1;
if (i >= 0 && bsStruts.get (i)) {
for (var j = 0; j < nEndMin; j++) {
var pt = (Clazz.doubleToInt (k / nEndMin)) * nEndMin + j;
if ((i = biopolymerStartsEnds[b][pt] - 1) >= 0) bsStruts.set (i);
biopolymerStartsEnds[b][pt] = -1;
}
}}
if (biopolymerStartsEnds[b][0] == -1 && biopolymerStartsEnds[b][nEndMin] == -1) continue;
var okN = false;
var okC = false;
var iN = 0;
var jN = 0;
var iC = 0;
var jC = 0;
var minN = 3.4028235E38;
var minC = 3.4028235E38;
for (var j = 0; j < n; j++) for (var k = 0; k < nEndMin * 2; k++) {
var i = biopolymerStartsEnds[b][k] - 1;
if (i == -2) {
k = (Clazz.doubleToInt (k / nEndMin) + 1) * nEndMin - 1;
continue;
}if (j == i || i == -1) continue;
var ipt = J.modelsetbio.AlphaPolymer.strutPoint (i, j, n);
if (bsNearbyResidues.get (ipt) || d2[ipt] > (k < nEndMin ? minN : minC)) continue;
if (k < nEndMin) {
if (bsNotAvailable.get (ipt)) okN = true;
jN = j;
iN = i;
minN = d2[ipt];
} else {
if (bsNotAvailable.get (ipt)) okC = true;
jC = j;
iC = i;
minC = d2[ipt];
}}

if (okN) J.modelsetbio.AlphaPolymer.setStrut (iN, jN, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
if (okC) J.modelsetbio.AlphaPolymer.setStrut (iC, jC, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
}
return vStruts;
}, $fz.isPrivate = true, $fz), "J.modelset.ModelSet,J.util.BS,J.util.BS,J.util.JmolList,~N,~N,~B");
c$.strutPoint = $_M(c$, "strutPoint", 
($fz = function (i, j, n) {
return (j < i ? Clazz.doubleToInt (j * (2 * n - j - 1) / 2) + i - j - 1 : Clazz.doubleToInt (i * (2 * n - i - 1) / 2) + j - i - 1);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
c$.setStrut = $_M(c$, "setStrut", 
($fz = function (i, j, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta) {
var a1 = vCA.get (i);
var a2 = vCA.get (j);
if (!bs1.get (a1.index) || !bs2.get (a2.index)) return;
vStruts.addLast ([a1, a2]);
bsStruts.set (i);
bsStruts.set (j);
for (var k1 = Math.max (0, i - delta); k1 <= i + delta && k1 < n; k1++) {
for (var k2 = Math.max (0, j - delta); k2 <= j + delta && k2 < n; k2++) {
if (k1 == k2) {
continue;
}var ipt = J.modelsetbio.AlphaPolymer.strutPoint (k1, k2, n);
if (!bsNearbyResidues.get (ipt)) {
bsNotAvailable.set (ipt);
}}
}
}, $fz.isPrivate = true, $fz), "~N,~N,~N,J.util.JmolList,J.util.BS,J.util.BS,J.util.JmolList,J.util.BS,J.util.BS,J.util.BS,~N");
$_M(c$, "calculateStructures", 
function (alphaOnly) {
if (this.monomerCount < 4) return;
var angles = this.calculateAnglesInDegrees ();
var codes = this.calculateCodes (angles);
this.checkBetaSheetAlphaHelixOverlap (codes, angles);
var tags = this.calculateRunsFourOrMore (codes);
this.extendRuns (tags);
this.searchForTurns (codes, angles, tags);
this.addStructuresFromTags (tags);
}, "~B");
$_M(c$, "calculateAnglesInDegrees", 
($fz = function () {
var angles =  Clazz.newFloatArray (this.monomerCount, 0);
for (var i = this.monomerCount - 1; --i >= 2; ) angles[i] = J.util.Measure.computeTorsion (this.monomers[i - 2].getLeadAtom (), this.monomers[i - 1].getLeadAtom (), this.monomers[i].getLeadAtom (), this.monomers[i + 1].getLeadAtom (), true);

return angles;
}, $fz.isPrivate = true, $fz));
$_M(c$, "calculateCodes", 
($fz = function (angles) {
var codes =  new Array (this.monomerCount);
for (var i = this.monomerCount - 1; --i >= 2; ) {
var degrees = angles[i];
codes[i] = ((degrees >= 10 && degrees < 120) ? J.modelsetbio.AlphaPolymer.Code.RIGHT_HELIX : ((degrees >= 120 || degrees < -90) ? J.modelsetbio.AlphaPolymer.Code.BETA_SHEET : ((degrees >= -90 && degrees < 0) ? J.modelsetbio.AlphaPolymer.Code.LEFT_HELIX : J.modelsetbio.AlphaPolymer.Code.NADA)));
}
return codes;
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "checkBetaSheetAlphaHelixOverlap", 
($fz = function (codes, angles) {
for (var i = this.monomerCount - 2; --i >= 2; ) if (codes[i] === J.modelsetbio.AlphaPolymer.Code.BETA_SHEET && angles[i] <= 140 && codes[i - 2] === J.modelsetbio.AlphaPolymer.Code.RIGHT_HELIX && codes[i - 1] === J.modelsetbio.AlphaPolymer.Code.RIGHT_HELIX && codes[i + 1] === J.modelsetbio.AlphaPolymer.Code.RIGHT_HELIX && codes[i + 2] === J.modelsetbio.AlphaPolymer.Code.RIGHT_HELIX) codes[i] = J.modelsetbio.AlphaPolymer.Code.RIGHT_HELIX;

}, $fz.isPrivate = true, $fz), "~A,~A");
$_M(c$, "calculateRunsFourOrMore", 
($fz = function (codes) {
var tags =  new Array (this.monomerCount);
var tag = J.constant.EnumStructure.NONE;
var code = J.modelsetbio.AlphaPolymer.Code.NADA;
var runLength = 0;
for (var i = 0; i < this.monomerCount; ++i) {
if (codes[i] === code && code !== J.modelsetbio.AlphaPolymer.Code.NADA && code !== J.modelsetbio.AlphaPolymer.Code.BETA_SHEET) {
++runLength;
if (runLength == 4) {
tag = (code === J.modelsetbio.AlphaPolymer.Code.BETA_SHEET ? J.constant.EnumStructure.SHEET : J.constant.EnumStructure.HELIX);
for (var j = 4; --j >= 0; ) tags[i - j] = tag;

} else if (runLength > 4) tags[i] = tag;
} else {
runLength = 1;
code = codes[i];
}}
return tags;
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "extendRuns", 
($fz = function (tags) {
for (var i = 1; i < this.monomerCount - 4; ++i) if (tags[i] === J.constant.EnumStructure.NONE && tags[i + 1] !== J.constant.EnumStructure.NONE) tags[i] = tags[i + 1];

tags[0] = tags[1];
tags[this.monomerCount - 1] = tags[this.monomerCount - 2];
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "searchForTurns", 
($fz = function (codes, angles, tags) {
for (var i = this.monomerCount - 1; --i >= 2; ) {
codes[i] = J.modelsetbio.AlphaPolymer.Code.NADA;
if (tags[i] == null || tags[i] === J.constant.EnumStructure.NONE) {
var angle = angles[i];
if (angle >= -90 && angle < 0) codes[i] = J.modelsetbio.AlphaPolymer.Code.LEFT_TURN;
 else if (angle >= 0 && angle < 90) codes[i] = J.modelsetbio.AlphaPolymer.Code.RIGHT_TURN;
}}
for (var i = this.monomerCount - 1; --i >= 0; ) {
if (codes[i] !== J.modelsetbio.AlphaPolymer.Code.NADA && codes[i + 1] === codes[i] && tags[i] === J.constant.EnumStructure.NONE) tags[i] = J.constant.EnumStructure.TURN;
}
}, $fz.isPrivate = true, $fz), "~A,~A,~A");
$_M(c$, "addStructuresFromTags", 
($fz = function (tags) {
var i = 0;
while (i < this.monomerCount) {
var tag = tags[i];
if (tag == null || tag === J.constant.EnumStructure.NONE) {
++i;
continue;
}var iMax;
for (iMax = i + 1; iMax < this.monomerCount && tags[iMax] === tag; ++iMax) {
}
this.addStructureProtected (tag, null, 0, 0, i, iMax - 1);
i = iMax;
}
}, $fz.isPrivate = true, $fz), "~A");
Clazz.pu$h ();
c$ = Clazz.declareType (J.modelsetbio.AlphaPolymer, "Code", Enum);
Clazz.defineEnumConstant (c$, "NADA", 0, []);
Clazz.defineEnumConstant (c$, "RIGHT_HELIX", 1, []);
Clazz.defineEnumConstant (c$, "BETA_SHEET", 2, []);
Clazz.defineEnumConstant (c$, "LEFT_HELIX", 3, []);
Clazz.defineEnumConstant (c$, "LEFT_TURN", 4, []);
Clazz.defineEnumConstant (c$, "RIGHT_TURN", 5, []);
c$ = Clazz.p0p ();
});
