Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.xtal.MagResReader", ["J.util.Eigen", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.cellParams = null;
this.maxIso = 10000;
this.atom = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "MagResReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.setFractionalCoordinates (false);
this.doApplySymmetry = false;
this.atomSetCollection.newAtomSet ();
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.startsWith ("lattice")) {
this.readCellParams ();
} else if (this.line.contains ("Coordinates")) {
this.readAtom ();
} else if (this.line.contains ("J-coupling Total") || this.line.contains ("TOTAL tensor")) {
this.readTensor ();
}return true;
});
$_M(c$, "readCellParams", 
($fz = function () {
var tokens = this.getTokens ();
this.cellParams =  Clazz.newFloatArray (9, 0);
for (var i = 0; i < 9; i++) this.cellParams[i] = this.parseFloatStr (tokens[i + 1]) * 0.5291772;

this.addPrimitiveLatticeVector (0, this.cellParams, 0);
this.addPrimitiveLatticeVector (1, this.cellParams, 3);
this.addPrimitiveLatticeVector (2, this.cellParams, 6);
this.setSpaceGroupName ("P1");
}, $fz.isPrivate = true, $fz));
$_M(c$, "readAtom", 
($fz = function () {
var f = this.line.trim ().endsWith ("A") ? 1 : 0.5291772;
var tokens = this.getTokens ();
this.atom = this.atomSetCollection.addNewAtom ();
this.atom.elementSymbol = tokens[0];
this.atom.atomName = tokens[0] + tokens[1];
var x = this.parseFloatStr (tokens[3]) * f;
var y = this.parseFloatStr (tokens[4]) * f;
var z = this.parseFloatStr (tokens[5]) * f;
this.atom.set (x, y, z);
this.setAtomCoord (this.atom);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readTensor", 
($fz = function () {
var isJ = (this.line.indexOf ("J-") >= 0);
this.atomSetCollection.setAtomSetName (this.line.trim ());
var data =  Clazz.newFloatArray (9, 0);
this.readLine ();
var s = J.util.TextFormat.simpleReplace (this.readLine () + this.readLine () + this.readLine (), "-", " -");
this.fillFloatArray (s, 0, data);
var f = 3;
if (isJ) {
this.discardLinesUntilContains ("Isotropic");
var iso = this.parseFloatStr (this.getTokens ()[3]);
if (Math.abs (iso) > this.maxIso) return;
f = 0.04;
}var a =  Clazz.newDoubleArray (3, 3, 0);
for (var i = 0, pt = 0; i < 3; i++) for (var j = 0; j < 3; j++) a[i][j] = data[pt++];


this.atom.setEllipsoid (J.util.Eigen.getEllipsoidDD (a));
this.atom.ellipsoid[0].scale (f);
}, $fz.isPrivate = true, $fz));
});
