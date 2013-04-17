Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.xtal.SiestaReader", ["java.lang.Double"], function () {
c$ = Clazz.decorateAsClass (function () {
this.noAtoms = 0;
this.unitCellData = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "SiestaReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.unitCellData =  Clazz.newFloatArray (9, 0);
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.doApplySymmetry = true;
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.contains ("%block LatticeVectors")) {
if (this.doGetModel (++this.modelNumber, null)) this.readCellThenAtomsCartesian ();
return true;
} else if (this.line.contains ("outcoor: Atomic coordinates")) {
if (this.doGetModel (++this.modelNumber, null)) this.readAtomsCartGeomThenCell ();
return true;
}return true;
});
$_M(c$, "setCell", 
($fz = function () {
this.fillFloatArray (null, 0, this.unitCellData);
this.addPrimitiveLatticeVector (0, this.unitCellData, 0);
this.addPrimitiveLatticeVector (1, this.unitCellData, 3);
this.addPrimitiveLatticeVector (2, this.unitCellData, 6);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readCellThenAtomsCartesian", 
($fz = function () {
this.newAtomSet ();
this.setCell ();
this.discardLinesUntilContains ("AtomicCoordinatesFormat Ang");
this.readLine ();
this.setFractionalCoordinates (false);
while (this.readLine () != null && this.line.indexOf ("%endblock Atomic") < 0) {
var tokens = this.getTokens ();
var atom = this.atomSetCollection.addNewAtom ();
atom.atomName = tokens[4];
var x = this.parseFloatStr (tokens[0]);
var y = this.parseFloatStr (tokens[1]);
var z = this.parseFloatStr (tokens[2]);
this.setAtomCoordXYZ (atom, x, y, z);
}
this.noAtoms = this.atomSetCollection.getAtomCount ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "newAtomSet", 
($fz = function () {
this.applySymmetryAndSetTrajectory ();
this.atomSetCollection.newAtomSet ();
this.setSpaceGroupName ("P1");
this.setFractionalCoordinates (false);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readAtomsCartGeomThenCell", 
($fz = function () {
this.readLines (1);
this.newAtomSet ();
var atom0 = this.atomSetCollection.getAtomCount ();
for (var i = 0; i < this.noAtoms; i++) {
var tokens = this.getTokens ();
var atom = this.atomSetCollection.addNewAtom ();
atom.atomName = tokens[4];
var x = this.parseFloatStr (tokens[0]);
var y = this.parseFloatStr (tokens[1]);
var z = this.parseFloatStr (tokens[2]);
atom.set (x, y, z);
this.readLine ();
}
this.discardLinesUntilContains ("outcell: Unit cell vectors");
this.setCell ();
var atoms = this.atomSetCollection.getAtoms ();
var atomCount = this.atomSetCollection.getAtomCount ();
for (var i = atom0; i < atomCount; i++) this.setAtomCoord (atoms[i]);

this.discardLinesUntilContains ("siesta: E_KS(eV) = ");
var tokens = this.getTokens ();
var energy = Double.$valueOf (Double.parseDouble (tokens[3]));
this.atomSetCollection.setAtomSetEnergy ("" + energy, energy.floatValue ());
this.atomSetCollection.setAtomSetAuxiliaryInfo ("Energy", energy);
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("Energy", energy);
this.atomSetCollection.setAtomSetName ("Energy = " + energy + " eV");
}, $fz.isPrivate = true, $fz));
});
