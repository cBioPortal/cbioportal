Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.xtal.XcrysdenReader", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.nAtoms = 0;
this.unitCellData = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "XcrysdenReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.unitCellData =  Clazz.newFloatArray (9, 0);
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.doApplySymmetry = true;
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.contains ("CRYSTAL")) {
this.setFractionalCoordinates (false);
} else if (this.line.contains ("PRIMVEC")) {
this.readUnitCell ();
} else if (this.line.contains ("PRIMCOORD")) {
this.readCoordinates ();
}return true;
});
$_M(c$, "readUnitCell", 
($fz = function () {
this.setSymmetry ();
this.fillFloatArray (null, 0, this.unitCellData);
this.setUnitCell ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setUnitCell", 
($fz = function () {
this.addPrimitiveLatticeVector (0, this.unitCellData, 0);
this.addPrimitiveLatticeVector (1, this.unitCellData, 3);
this.addPrimitiveLatticeVector (2, this.unitCellData, 6);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setSymmetry", 
($fz = function () {
this.applySymmetryAndSetTrajectory ();
this.atomSetCollection.newAtomSet ();
this.setSpaceGroupName ("P1");
this.setFractionalCoordinates (false);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readCoordinates", 
($fz = function () {
var atomStr = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
this.nAtoms = Integer.parseInt (atomStr[0]);
this.setFractionalCoordinates (false);
var counter = 0;
while (counter < this.nAtoms && this.readLine () != null) {
var atom = this.atomSetCollection.addNewAtom ();
var tokens = this.getTokens ();
atom.atomName = J.adapter.smarter.AtomSetCollectionReader.getElementSymbol (Integer.parseInt (tokens[0]));
var x = this.parseFloatStr (tokens[1]);
var y = this.parseFloatStr (tokens[2]);
var z = this.parseFloatStr (tokens[3]);
this.setAtomCoordXYZ (atom, x, y, z);
counter++;
}
this.atomSetCollection.setAtomSetName ("Initial Coordinates");
}, $fz.isPrivate = true, $fz));
});
