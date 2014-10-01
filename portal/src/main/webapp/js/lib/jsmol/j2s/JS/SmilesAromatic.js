Clazz.declarePackage ("JS");
Clazz.load (null, "JS.SmilesAromatic", ["JU.BS", "$.V3"], function () {
c$ = Clazz.declareType (JS, "SmilesAromatic");
c$.isFlatSp2Ring = Clazz.defineMethod (c$, "isFlatSp2Ring", 
function (atoms, bsSelected, bs, cutoff) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var ringAtom = atoms[i];
var bonds = ringAtom.getEdges ();
if (bonds.length < 3) continue;
if (bonds.length > 3) return false;
}
if (cutoff == 3.4028235E38) return true;
if (cutoff <= 0) cutoff = 0.01;
var vTemp =  new JU.V3 ();
var vA =  new JU.V3 ();
var vB =  new JU.V3 ();
var vMean = null;
var nPoints = bs.cardinality ();
var vNorms =  new Array (nPoints * 2);
var nNorms = 0;
var maxDev = (1 - cutoff * 5);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var ringAtom = atoms[i];
var bonds = ringAtom.getEdges ();
var iSub = -1;
var r1 = -1;
var r2 = -1;
for (var k = bonds.length; --k >= 0; ) {
var iAtom = ringAtom.getBondedAtomIndex (k);
if (!bsSelected.get (iAtom)) continue;
if (!bs.get (iAtom)) iSub = iAtom;
 else if (r1 < 0) r1 = iAtom;
 else r2 = iAtom;
}
JS.SmilesAromatic.getNormalThroughPoints (atoms[r1], atoms[i], atoms[r2], vTemp, vA, vB);
if (vMean == null) vMean =  new JU.V3 ();
if (!JS.SmilesAromatic.addNormal (vTemp, vMean, maxDev)) return false;
vNorms[nNorms++] = JU.V3.newV (vTemp);
if (iSub >= 0) {
JS.SmilesAromatic.getNormalThroughPoints (atoms[r1], atoms[iSub], atoms[r2], vTemp, vA, vB);
if (!JS.SmilesAromatic.addNormal (vTemp, vMean, maxDev)) return false;
vNorms[nNorms++] = JU.V3.newV (vTemp);
}}
var isFlat = JS.SmilesAromatic.checkStandardDeviation (vNorms, vMean, nNorms, cutoff);
return isFlat;
}, "~A,JU.BS,JU.BS,~N");
c$.addNormal = Clazz.defineMethod (c$, "addNormal", 
 function (vTemp, vMean, maxDev) {
var similarity = vMean.dot (vTemp);
if (similarity != 0 && Math.abs (similarity) < maxDev) return false;
if (similarity < 0) vTemp.scale (-1);
vMean.add (vTemp);
vMean.normalize ();
return true;
}, "JU.V3,JU.V3,~N");
c$.checkStandardDeviation = Clazz.defineMethod (c$, "checkStandardDeviation", 
 function (vNorms, vMean, n, cutoff) {
var sum = 0;
var sum2 = 0;
for (var i = 0; i < n; i++) {
var v = vNorms[i].dot (vMean);
sum += v;
sum2 += (v) * v;
}
sum = Math.sqrt ((sum2 - sum * sum / n) / (n - 1));
return (sum < cutoff);
}, "~A,JU.V3,~N,~N");
c$.getNormalThroughPoints = Clazz.defineMethod (c$, "getNormalThroughPoints", 
function (pointA, pointB, pointC, vNorm, vAB, vAC) {
vAB.sub2 (pointB, pointA);
vAC.sub2 (pointC, pointA);
vNorm.cross (vAB, vAC);
vNorm.normalize ();
vAB.setT (pointA);
return -vAB.dot (vNorm);
}, "JU.Node,JU.Node,JU.Node,JU.V3,JU.V3,JU.V3");
c$.checkAromaticDefined = Clazz.defineMethod (c$, "checkAromaticDefined", 
function (jmolAtoms, bsAtoms) {
var bsDefined =  new JU.BS ();
for (var i = bsAtoms.nextSetBit (0); i >= 0; i = bsAtoms.nextSetBit (i + 1)) {
var bonds = jmolAtoms[i].getEdges ();
for (var j = 0; j < bonds.length; j++) {
switch (bonds[j].order) {
case 515:
case 514:
case 513:
bsDefined.set (bonds[j].getAtomIndex1 ());
bsDefined.set (bonds[j].getAtomIndex2 ());
}
}
}
return bsDefined;
}, "~A,JU.BS");
c$.checkAromaticStrict = Clazz.defineMethod (c$, "checkAromaticStrict", 
function (jmolAtoms, bsAromatic, v5, v6) {
var bsStrict =  new JU.BS ();
var bsTest =  new JU.BS ();
for (var i = v5.size (); --i >= 0; ) {
var bs = v5.get (i);
if (JS.SmilesAromatic.isAromaticRing (bsAromatic, bsTest, bs, 5)) JS.SmilesAromatic.checkAromaticStrict2 (jmolAtoms, bsStrict, v5, v6, bs, true);
}
for (var i = v6.size (); --i >= 0; ) {
var bs = v6.get (i);
if (JS.SmilesAromatic.isAromaticRing (bsAromatic, bsTest, bs, 6)) JS.SmilesAromatic.checkAromaticStrict2 (jmolAtoms, bsStrict, v5, v6, bs, false);
}
bsAromatic.clearAll ();
bsAromatic.or (bsStrict);
}, "~A,JU.BS,JU.Lst,JU.Lst");
c$.isAromaticRing = Clazz.defineMethod (c$, "isAromaticRing", 
 function (bsAromatic, bsTest, bs, n) {
bsTest.clearAll ();
bsTest.or (bs);
bsTest.and (bsAromatic);
return (bsTest.cardinality () == n);
}, "JU.BS,JU.BS,JU.BS,~N");
c$.checkAromaticStrict2 = Clazz.defineMethod (c$, "checkAromaticStrict2", 
 function (jmolAtoms, bsStrict, v5, v6, bsRing, is5) {
var piElectronCount = JS.SmilesAromatic.countInternalPairs (jmolAtoms, bsRing, is5) << 1;
switch (piElectronCount) {
case -3:
break;
default:
for (var i = bsRing.nextSetBit (0); i >= 0; i = bsRing.nextSetBit (i + 1)) {
var bonds = jmolAtoms[i].getEdges ();
for (var j = 0; j < bonds.length; j++) if (bonds[j].order == 2) {
var i2 = bonds[j].getOtherAtomNode (jmolAtoms[i]).getIndex ();
if (!bsRing.get (i2)) {
var piShared = false;
for (var k = v5.size (); --k >= 0 && !piShared; ) {
var bs = v5.get (k);
if (bs.get (i2) && (bsStrict.get (i2) || Math.abs (JS.SmilesAromatic.countInternalPairs (jmolAtoms, bs, true)) == 3)) piShared = true;
}
for (var k = v6.size (); --k >= 0 && !piShared; ) {
var bs = v6.get (k);
if (bs.get (i2) && (bsStrict.get (i2) || Math.abs (JS.SmilesAromatic.countInternalPairs (jmolAtoms, bs, false)) == 3)) piShared = true;
}
if (!piShared) return;
piElectronCount++;
}}
}
break;
}
if (piElectronCount == 6) bsStrict.or (bsRing);
}, "~A,JU.BS,JU.Lst,JU.Lst,JU.BS,~B");
c$.countInternalPairs = Clazz.defineMethod (c$, "countInternalPairs", 
 function (jmolAtoms, bsRing, is5) {
var nDouble = 0;
var nAromatic = 0;
var nLonePairs = 0;
for (var i = bsRing.nextSetBit (0); i >= 0; i = bsRing.nextSetBit (i + 1)) {
var atom = jmolAtoms[i];
var bonds = atom.getEdges ();
var haveDouble = false;
for (var k = 0; k < bonds.length; k++) {
var j = bonds[k].getOtherAtomNode (atom).getIndex ();
if (bsRing.get (j)) {
switch (bonds[k].order) {
case 514:
case 513:
case 515:
nAromatic++;
break;
case 2:
nDouble++;
haveDouble = true;
}
}}
if (is5 && nAromatic == 0) {
switch (atom.getElementNumber ()) {
case 7:
case 8:
case 16:
if (!haveDouble) nLonePairs++;
break;
}
}}
return (nAromatic == 0 ? Clazz.doubleToInt (nDouble / 2) + nLonePairs : nAromatic == (is5 ? 5 : 6) ? -3 : 0);
}, "~A,JU.BS,~B");
});
