Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.BasisFunctionReader"], "J.adapter.readers.quantum.SpartanReader", ["java.lang.Float", "java.util.Hashtable", "J.adapter.readers.quantum.SpartanArchive"], function () {
c$ = Clazz.declareType (J.adapter.readers.quantum, "SpartanReader", J.adapter.readers.quantum.BasisFunctionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
var cartesianHeader = "Cartesian Coordinates (Ang";
if (this.isSpartanArchive (cartesianHeader)) {
this.moData =  new java.util.Hashtable ();
var spartanArchive =  new J.adapter.readers.quantum.SpartanArchive (this);
var atomCount = spartanArchive.readArchive (this.line, true, 0, true);
if (atomCount > 0) this.atomSetCollection.setAtomSetName ("Spartan file");
} else if (this.line.indexOf (cartesianHeader) >= 0) {
this.readAtoms ();
this.discardLinesUntilContains ("Vibrational Frequencies");
if (this.line != null) this.readFrequencies ();
}this.continuing = false;
});
$_M(c$, "isSpartanArchive", 
($fz = function (strNotArchive) {
var lastLine = "";
while (this.readLine () != null) {
if (this.line.equals ("GEOMETRY")) {
this.line = lastLine;
return true;
}if (this.line.indexOf (strNotArchive) >= 0) return false;
lastLine = this.line;
}
return false;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "readAtoms", 
($fz = function () {
this.discardLinesUntilBlank ();
while (this.readLine () != null && (this.parseIntRange (this.line, 0, 3)) > 0) {
var elementSymbol = this.parseTokenRange (this.line, 4, 6);
var atomName = this.parseTokenRange (this.line, 7, 13);
var atom = this.atomSetCollection.addNewAtom ();
atom.elementSymbol = elementSymbol;
atom.atomName = atomName;
this.setAtomCoordXYZ (atom, this.parseFloatRange (this.line, 17, 30), this.parseFloatRange (this.line, 31, 44), this.parseFloatRange (this.line, 45, 58));
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readFrequencies", 
($fz = function () {
var atomCount = this.atomSetCollection.getFirstAtomSetAtomCount ();
while (true) {
this.discardLinesUntilNonBlank ();
var lineBaseFreqCount = this.vibrationNumber;
this.next[0] = 16;
var lineFreqCount;
var ignore =  Clazz.newBooleanArray (3, false);
for (lineFreqCount = 0; lineFreqCount < 3; ++lineFreqCount) {
var frequency = this.parseFloat ();
if (Float.isNaN (frequency)) break;
ignore[lineFreqCount] = !this.doGetVibration (++this.vibrationNumber);
if (!ignore[lineFreqCount]) {
if (this.vibrationNumber > 1) this.atomSetCollection.cloneFirstAtomSet (0);
this.atomSetCollection.setAtomSetFrequency (null, null, "" + frequency, null);
}}
if (lineFreqCount == 0) return;
this.readLines (2);
for (var i = 0; i < atomCount; ++i) {
this.readLine ();
for (var j = 0; j < lineFreqCount; ++j) {
var ichCoords = j * 23 + 10;
var x = this.parseFloatRange (this.line, ichCoords, ichCoords + 7);
var y = this.parseFloatRange (this.line, ichCoords + 7, ichCoords + 14);
var z = this.parseFloatRange (this.line, ichCoords + 14, ichCoords + 21);
if (!ignore[j]) this.atomSetCollection.addVibrationVector (i + (lineBaseFreqCount + j) * atomCount, x, y, z);
}
}
}
}, $fz.isPrivate = true, $fz));
});
