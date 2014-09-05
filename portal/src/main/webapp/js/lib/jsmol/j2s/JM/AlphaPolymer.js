Clazz.declarePackage ("JM");
Clazz.load (["java.lang.Enum", "JM.BioPolymer"], "JM.AlphaPolymer", ["JU.BS", "$.Lst", "$.Measure", "$.P3", "J.c.STR", "JM.Helix", "$.Sheet", "$.Turn", "JU.Logger"], function () {
c$ = Clazz.declareType (JM, "AlphaPolymer", JM.BioPolymer);
Clazz.makeConstructor (c$, 
function (monomers) {
Clazz.superConstructor (this, JM.AlphaPolymer, [monomers]);
this.hasStructure = true;
}, "~A");
Clazz.overrideMethod (c$, "getProteinStructure", 
function (monomerIndex) {
return this.monomers[monomerIndex].getStructure ();
}, "~N");
Clazz.overrideMethod (c$, "getControlPoint", 
function (i, v) {
if (!this.monomers[i].isSheet ()) return this.leadPoints[i];
v.sub2 (this.leadMidpoints[i], this.leadPoints[i]);
v.scale (this.sheetSmoothing);
var pt = JU.P3.newP (this.leadPoints[i]);
pt.add (v);
return pt;
}, "~N,JU.V3");
Clazz.defineMethod (c$, "addStructure", 
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
}if (this.addStructureProtected (type, structureID, serialID, strandCount, indexStart, indexEnd) && istart >= 0) bsAssigned.setBits (istart, iend + 1);
}, "J.c.STR,~S,~N,~N,~N,~N,~N,~N,~N,~N,JU.BS");
Clazz.defineMethod (c$, "addStructureProtected", 
function (type, structureID, serialID, strandCount, indexStart, indexEnd) {
if (indexEnd < indexStart) {
JU.Logger.error ("AlphaPolymer:addSecondaryStructure error:  indexStart:" + indexStart + " indexEnd:" + indexEnd);
return false;
}var structureCount = indexEnd - indexStart + 1;
var ps = null;
switch (type) {
case J.c.STR.HELIX:
case J.c.STR.HELIXALPHA:
case J.c.STR.HELIX310:
case J.c.STR.HELIXPI:
ps =  new JM.Helix (this, indexStart, structureCount, type);
break;
case J.c.STR.SHEET:
ps =  new JM.Sheet (this, indexStart, structureCount, type);
break;
case J.c.STR.TURN:
ps =  new JM.Turn (this, indexStart, structureCount);
break;
default:
JU.Logger.error ("unrecognized secondary structure type");
return false;
}
ps.structureID = structureID;
ps.serialID = serialID;
ps.strandCount = strandCount;
for (var i = indexStart; i <= indexEnd; ++i) {
(this.monomers[i]).setStructure (ps);
}
return true;
}, "J.c.STR,~S,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "clearStructures", 
function () {
for (var i = 0; i < this.monomerCount; i++) (this.monomers[i]).setStructure (null);

});
Clazz.overrideMethod (c$, "calculateStruts", 
function (modelSet, bs1, bs2, vCA, thresh, delta, allowMultiple) {
return this.calculateStrutsStatic (modelSet, bs1, bs2, vCA, thresh, delta, allowMultiple);
}, "JM.ModelSet,JU.BS,JU.BS,JU.Lst,~N,~N,~B");
Clazz.defineMethod (c$, "calculateStrutsStatic", 
 function (modelSet, bs1, bs2, vCA, thresh, delta, allowMultiple) {
var vStruts =  new JU.Lst ();
var thresh2 = thresh * thresh;
var n = vCA.size ();
var nEndMin = 3;
var bsStruts =  new JU.BS ();
var bsNotAvailable =  new JU.BS ();
var bsNearbyResidues =  new JU.BS ();
var a1 = vCA.get (0);
var a2;
var nBiopolymers = modelSet.getBioPolymerCountInModel (a1.mi);
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
var ipt = JM.AlphaPolymer.strutPoint (i, j, n);
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
var ipt = JM.AlphaPolymer.strutPoint (i, j, n);
if (!bsNotAvailable.get (ipt) && !bsNearbyResidues.get (ipt) && (allowMultiple || !bsStruts.get (j)) && d2[ipt] <= thresh2) JM.AlphaPolymer.setStrut (i, j, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
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
var ipt = JM.AlphaPolymer.strutPoint (i, j, n);
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

if (okN) JM.AlphaPolymer.setStrut (iN, jN, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
if (okC) JM.AlphaPolymer.setStrut (iC, jC, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
}
return vStruts;
}, "JM.ModelSet,JU.BS,JU.BS,JU.Lst,~N,~N,~B");
c$.strutPoint = Clazz.defineMethod (c$, "strutPoint", 
 function (i, j, n) {
return (j < i ? Clazz.doubleToInt (j * (2 * n - j - 1) / 2) + i - j - 1 : Clazz.doubleToInt (i * (2 * n - i - 1) / 2) + j - i - 1);
}, "~N,~N,~N");
c$.setStrut = Clazz.defineMethod (c$, "setStrut", 
 function (i, j, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta) {
var a1 = vCA.get (i);
var a2 = vCA.get (j);
if (!bs1.get (a1.i) || !bs2.get (a2.i)) return;
vStruts.addLast ([a1, a2]);
bsStruts.set (i);
bsStruts.set (j);
for (var k1 = Math.max (0, i - delta); k1 <= i + delta && k1 < n; k1++) {
for (var k2 = Math.max (0, j - delta); k2 <= j + delta && k2 < n; k2++) {
if (k1 == k2) {
continue;
}var ipt = JM.AlphaPolymer.strutPoint (k1, k2, n);
if (!bsNearbyResidues.get (ipt)) {
bsNotAvailable.set (ipt);
}}
}
}, "~N,~N,~N,JU.Lst,JU.BS,JU.BS,JU.Lst,JU.BS,JU.BS,JU.BS,~N");
Clazz.defineMethod (c$, "calculateStructures", 
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
Clazz.defineMethod (c$, "calculateAnglesInDegrees", 
 function () {
var angles =  Clazz.newFloatArray (this.monomerCount, 0);
for (var i = this.monomerCount - 1; --i >= 2; ) angles[i] = JU.Measure.computeTorsion (this.monomers[i - 2].getLeadAtom (), this.monomers[i - 1].getLeadAtom (), this.monomers[i].getLeadAtom (), this.monomers[i + 1].getLeadAtom (), true);

return angles;
});
Clazz.defineMethod (c$, "calculateCodes", 
 function (angles) {
var codes =  new Array (this.monomerCount);
for (var i = this.monomerCount - 1; --i >= 2; ) {
var degrees = angles[i];
codes[i] = ((degrees >= 10 && degrees < 120) ? JM.AlphaPolymer.Code.RIGHT_HELIX : ((degrees >= 120 || degrees < -90) ? JM.AlphaPolymer.Code.BETA_SHEET : ((degrees >= -90 && degrees < 0) ? JM.AlphaPolymer.Code.LEFT_HELIX : JM.AlphaPolymer.Code.NADA)));
}
return codes;
}, "~A");
Clazz.defineMethod (c$, "checkBetaSheetAlphaHelixOverlap", 
 function (codes, angles) {
for (var i = this.monomerCount - 2; --i >= 2; ) if (codes[i] === JM.AlphaPolymer.Code.BETA_SHEET && angles[i] <= 140 && codes[i - 2] === JM.AlphaPolymer.Code.RIGHT_HELIX && codes[i - 1] === JM.AlphaPolymer.Code.RIGHT_HELIX && codes[i + 1] === JM.AlphaPolymer.Code.RIGHT_HELIX && codes[i + 2] === JM.AlphaPolymer.Code.RIGHT_HELIX) codes[i] = JM.AlphaPolymer.Code.RIGHT_HELIX;

}, "~A,~A");
Clazz.defineMethod (c$, "calculateRunsFourOrMore", 
 function (codes) {
var tags =  new Array (this.monomerCount);
var tag = J.c.STR.NONE;
var code = JM.AlphaPolymer.Code.NADA;
var runLength = 0;
for (var i = 0; i < this.monomerCount; ++i) {
if (codes[i] === code && code !== JM.AlphaPolymer.Code.NADA && code !== JM.AlphaPolymer.Code.BETA_SHEET) {
++runLength;
if (runLength == 4) {
tag = (code === JM.AlphaPolymer.Code.BETA_SHEET ? J.c.STR.SHEET : J.c.STR.HELIX);
for (var j = 4; --j >= 0; ) tags[i - j] = tag;

} else if (runLength > 4) tags[i] = tag;
} else {
runLength = 1;
code = codes[i];
}}
return tags;
}, "~A");
Clazz.defineMethod (c$, "extendRuns", 
 function (tags) {
for (var i = 1; i < this.monomerCount - 4; ++i) if (tags[i] === J.c.STR.NONE && tags[i + 1] !== J.c.STR.NONE) tags[i] = tags[i + 1];

tags[0] = tags[1];
tags[this.monomerCount - 1] = tags[this.monomerCount - 2];
}, "~A");
Clazz.defineMethod (c$, "searchForTurns", 
 function (codes, angles, tags) {
for (var i = this.monomerCount - 1; --i >= 2; ) {
codes[i] = JM.AlphaPolymer.Code.NADA;
if (tags[i] == null || tags[i] === J.c.STR.NONE) {
var angle = angles[i];
if (angle >= -90 && angle < 0) codes[i] = JM.AlphaPolymer.Code.LEFT_TURN;
 else if (angle >= 0 && angle < 90) codes[i] = JM.AlphaPolymer.Code.RIGHT_TURN;
}}
for (var i = this.monomerCount - 1; --i >= 0; ) {
if (codes[i] !== JM.AlphaPolymer.Code.NADA && codes[i + 1] === codes[i] && tags[i] === J.c.STR.NONE) tags[i] = J.c.STR.TURN;
}
}, "~A,~A,~A");
Clazz.defineMethod (c$, "addStructuresFromTags", 
 function (tags) {
var i = 0;
while (i < this.monomerCount) {
var tag = tags[i];
if (tag == null || tag === J.c.STR.NONE) {
++i;
continue;
}var iMax;
for (iMax = i + 1; iMax < this.monomerCount && tags[iMax] === tag; ++iMax) {
}
this.addStructureProtected (tag, null, 0, 0, i, iMax - 1);
i = iMax;
}
}, "~A");
Clazz.pu$h(self.c$);
c$ = Clazz.declareType (JM.AlphaPolymer, "Code", Enum);
Clazz.defineEnumConstant (c$, "NADA", 0, []);
Clazz.defineEnumConstant (c$, "RIGHT_HELIX", 1, []);
Clazz.defineEnumConstant (c$, "BETA_SHEET", 2, []);
Clazz.defineEnumConstant (c$, "LEFT_HELIX", 3, []);
Clazz.defineEnumConstant (c$, "LEFT_TURN", 4, []);
Clazz.defineEnumConstant (c$, "RIGHT_TURN", 5, []);
c$ = Clazz.p0p ();
});
