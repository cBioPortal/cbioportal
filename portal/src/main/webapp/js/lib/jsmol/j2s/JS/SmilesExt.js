Clazz.declarePackage ("JS");
Clazz.load (["JS.JmolSmilesExtension"], "JS.SmilesExt", ["java.lang.Float", "JU.AU", "$.BS", "$.Lst", "$.M4", "$.Measure", "$.P3", "JU.Escape", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.e = null;
this.sm = null;
Clazz.instantialize (this, arguments);
}, JS, "SmilesExt", null, JS.JmolSmilesExtension);
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "init", 
function (se) {
this.e = se;
this.sm = this.e.vwr.getSmilesMatcher ();
return this;
}, "~O");
Clazz.overrideMethod (c$, "getSmilesCorrelation", 
function (bsA, bsB, smiles, ptsA, ptsB, m4, vReturn, isSmarts, asMap, mapSet, center, firstMatchOnly, bestMap) {
var tolerance = (mapSet == null ? 0.1 : 3.4028235E38);
try {
if (ptsA == null) {
ptsA =  new JU.Lst ();
ptsB =  new JU.Lst ();
}var m =  new JU.M4 ();
var c =  new JU.P3 ();
var atoms = this.e.vwr.ms.at;
var ac = this.e.vwr.getAtomCount ();
var maps = this.sm.getCorrelationMaps (smiles, atoms, ac, bsA, isSmarts, true);
if (maps == null) this.e.evalError (this.sm.getLastException (), null);
if (maps.length == 0) return NaN;
var mapFirst = maps[0];
for (var i = 0; i < mapFirst.length; i++) ptsA.addLast (atoms[mapFirst[i]]);

maps = this.sm.getCorrelationMaps (smiles, atoms, ac, bsB, isSmarts, firstMatchOnly);
if (maps == null) this.e.evalError (this.sm.getLastException (), null);
if (maps.length == 0) return NaN;
JU.Logger.info (maps.length + " mappings found");
if (bestMap || !asMap) {
var lowestStdDev = 3.4028235E38;
var mapBest = null;
for (var i = 0; i < maps.length; i++) {
ptsB.clear ();
for (var j = 0; j < maps[i].length; j++) ptsB.addLast (atoms[maps[i][j]]);

var stddev = JU.Measure.getTransformMatrix4 (ptsA, ptsB, m, c);
JU.Logger.info ("getSmilesCorrelation stddev=" + stddev);
if (vReturn != null) {
if (stddev < tolerance) {
var bs =  new JU.BS ();
for (var j = 0; j < maps[i].length; j++) bs.set (maps[i][j]);

vReturn.addLast (bs);
}}if (stddev < lowestStdDev) {
mapBest = maps[i];
if (m4 != null) m4.setM4 (m);
if (center != null) center.setT (c);
lowestStdDev = stddev;
}}
if (mapSet != null) {
mapSet[0] = mapFirst;
mapSet[1] = mapBest;
}ptsB.clear ();
for (var i = 0; i < mapBest.length; i++) ptsB.addLast (atoms[mapBest[i]]);

return lowestStdDev;
}for (var i = 0; i < maps.length; i++) for (var j = 0; j < maps[i].length; j++) ptsB.addLast (atoms[maps[i][j]]);


} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
this.e.evalError (ex.getMessage (), null);
} else {
throw ex;
}
}
return 0;
}, "JU.BS,JU.BS,~S,JU.Lst,JU.Lst,JU.M4,JU.Lst,~B,~B,~A,JU.P3,~B,~B");
Clazz.overrideMethod (c$, "getSmilesMatches", 
function (pattern, smiles, bsSelected, bsMatch3D, isSmarts, asOneBitset) {
if (pattern.length == 0 || pattern.equals ("H")) {
var isBioSmiles = (!asOneBitset);
try {
return this.e.vwr.getSmilesOpt (bsSelected, 0, 0, pattern.equals ("H"), isBioSmiles, false, true, true);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
this.e.evalError (ex.getMessage (), null);
} else {
throw ex;
}
}
}var asAtoms = true;
var b;
if (bsMatch3D == null) {
asAtoms = (smiles == null);
try {
if (asAtoms) b = this.sm.getSubstructureSetArray (pattern, this.e.vwr.ms.at, this.e.vwr.getAtomCount (), bsSelected, null, isSmarts, false);
 else b = this.sm.find (pattern, smiles, isSmarts, false);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
this.e.evalError (ex.getMessage (), null);
return null;
} else {
throw ex;
}
}
} else {
var vReturn =  new JU.Lst ();
var stddev = this.getSmilesCorrelation (bsMatch3D, bsSelected, pattern, null, null, null, vReturn, isSmarts, false, null, null, false, false);
if (Float.isNaN (stddev)) {
if (asOneBitset) return  new JU.BS ();
return [];
}this.e.showString ("RMSD " + stddev + " Angstroms");
b = vReturn.toArray ( new Array (vReturn.size ()));
}if (asOneBitset) {
var bs =  new JU.BS ();
for (var j = 0; j < b.length; j++) bs.or (b[j]);

if (asAtoms) return bs;
if (!isSmarts) return Integer.$valueOf (bs.cardinality ());
var iarray =  Clazz.newIntArray (bs.cardinality (), 0);
var pt = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) iarray[pt++] = i + 1;

return iarray;
}var matches =  new Array (b.length);
for (var j = 0; j < b.length; j++) matches[j] = (asAtoms ? JU.Escape.eBS (b[j]) : JU.Escape.eBond (b[j]));

return matches;
}, "~S,~S,JU.BS,JU.BS,~B,~B");
Clazz.overrideMethod (c$, "getFlexFitList", 
function (bs1, bs2, smiles1, isSmarts) {
var mapSet = JU.AU.newInt2 (2);
this.getSmilesCorrelation (bs1, bs2, smiles1, null, null, null, null, isSmarts, false, mapSet, null, false, false);
if (mapSet[0] == null) return null;
var bondMap1 = this.e.vwr.getDihedralMap (mapSet[0]);
var bondMap2 = (bondMap1 == null ? null : this.e.vwr.getDihedralMap (mapSet[1]));
if (bondMap2 == null || bondMap2.length != bondMap1.length) return null;
var angles =  Clazz.newFloatArray (bondMap1.length, 3, 0);
var atoms = this.e.vwr.ms.at;
JS.SmilesExt.getTorsions (atoms, bondMap2, angles, 0);
JS.SmilesExt.getTorsions (atoms, bondMap1, angles, 1);
var data =  Clazz.newFloatArray (bondMap1.length * 6, 0);
for (var i = 0, pt = 0; i < bondMap1.length; i++) {
var map = bondMap1[i];
data[pt++] = map[0];
data[pt++] = map[1];
data[pt++] = map[2];
data[pt++] = map[3];
data[pt++] = angles[i][0];
data[pt++] = angles[i][1];
}
return data;
}, "JU.BS,JU.BS,~S,~B");
c$.getTorsions = Clazz.defineMethod (c$, "getTorsions", 
 function (atoms, bondMap, diff, pt) {
for (var i = bondMap.length; --i >= 0; ) {
var map = bondMap[i];
var v = JU.Measure.computeTorsion (atoms[map[0]], atoms[map[1]], atoms[map[2]], atoms[map[3]], true);
if (pt == 1) {
if (v - diff[i][0] > 180) v -= 360;
 else if (v - diff[i][0] <= -180) v += 360;
}diff[i][pt] = v;
}
}, "~A,~A,~A,~N");
});
