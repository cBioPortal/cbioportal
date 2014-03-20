Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader", "J.util.JmolList"], "J.adapter.readers.xtal.VaspPoscarReader", ["J.util.Logger", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atomLabels = null;
this.atomCount = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "VaspPoscarReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.atomLabels =  new J.util.JmolList ();
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.readJobTitle ();
this.readUnitCellVectors ();
this.readMolecularFormula ();
this.readCoordinates ();
this.continuing = false;
});
$_M(c$, "readJobTitle", 
($fz = function () {
this.atomSetCollection.setAtomSetName (this.readLine ().trim ());
}, $fz.isPrivate = true, $fz));
$_M(c$, "readUnitCellVectors", 
($fz = function () {
this.setSpaceGroupName ("P1");
this.setFractionalCoordinates (true);
var scaleFac = this.parseFloatStr (this.readLine ().trim ());
var unitCellData =  Clazz.newFloatArray (9, 0);
this.fillFloatArray (null, 0, unitCellData);
if (scaleFac != 1) for (var i = 0; i < unitCellData.length; i++) unitCellData[i] *= scaleFac;

this.addPrimitiveLatticeVector (0, unitCellData, 0);
this.addPrimitiveLatticeVector (1, unitCellData, 3);
this.addPrimitiveLatticeVector (2, unitCellData, 6);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readMolecularFormula", 
($fz = function () {
var elementLabel = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.discardLinesUntilNonBlank ());
var elementCounts = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var mf =  new J.util.SB ();
for (var i = 0; i < elementCounts.length; i++) {
var n = Integer.parseInt (elementCounts[i]);
this.atomCount += n;
var label = elementLabel[i];
mf.append (" ").append (label).appendI (n);
for (var j = n; --j >= 0; ) this.atomLabels.addLast (label);

}
var s = mf.toString ();
J.util.Logger.info ("VaspPoscar reader: " + this.atomCount + " atoms identified for" + s);
this.appendLoadNote (s);
this.atomSetCollection.newAtomSet ();
this.atomSetCollection.setAtomSetName (s);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readCoordinates", 
($fz = function () {
if (this.discardLinesUntilNonBlank ().toLowerCase ().contains ("selective")) this.readLine ();
if (this.line.toLowerCase ().contains ("cartesian")) this.setFractionalCoordinates (false);
for (var i = 0; i < this.atomCount; i++) {
var atom = this.atomSetCollection.addNewAtom ();
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
atom.atomName = this.atomLabels.get (i);
var x = this.parseFloatStr (tokens[0]);
var y = this.parseFloatStr (tokens[1]);
var z = this.parseFloatStr (tokens[2]);
this.setAtomCoordXYZ (atom, x, y, z);
}
}, $fz.isPrivate = true, $fz));
});
