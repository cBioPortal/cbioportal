Clazz.declarePackage ("J.adapter.readers.simple");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.simple.MopacReader", ["java.lang.Exception", "$.Float", "J.util.BS", "$.Logger", "$.Parser"], function () {
c$ = Clazz.decorateAsClass (function () {
this.baseAtomIndex = 0;
this.chargesFound = false;
this.haveHeader = false;
this.mopacVersion = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.simple, "MopacReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
while (this.mopacVersion == 0) {
this.discardLinesUntilContains ("MOPAC");
if (this.line.indexOf ("2009") >= 0) this.mopacVersion = 2009;
 else if (this.line.indexOf ("6.") >= 0) this.mopacVersion = 6;
 else if (this.line.indexOf ("7.") >= 0) this.mopacVersion = 7;
 else if (this.line.indexOf ("93") >= 0) this.mopacVersion = 93;
 else if (this.line.indexOf ("2002") >= 0) this.mopacVersion = 2002;
 else if (this.line.indexOf ("MOPAC2") >= 0) this.mopacVersion = J.util.Parser.parseInt (this.line.substring (this.line.indexOf ("MOPAC2") + 5));
}
J.util.Logger.info ("MOPAC version " + this.mopacVersion);
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (!this.haveHeader) {
if (this.line.trim ().equals ("CARTESIAN COORDINATES")) {
this.processCoordinates ();
this.atomSetCollection.setAtomSetName ("Input Structure");
return true;
}this.haveHeader = this.line.startsWith (" ---");
return true;
}if (this.line.indexOf ("TOTAL ENERGY") >= 0) {
this.processTotalEnergy ();
return true;
}if (this.line.indexOf ("ATOMIC CHARGES") >= 0) {
this.processAtomicCharges ();
return true;
}if (this.line.trim ().equals ("CARTESIAN COORDINATES")) {
this.processCoordinates ();
return true;
}if (this.line.indexOf ("ORIENTATION OF MOLECULE IN FORCE") >= 0) {
this.processCoordinates ();
this.atomSetCollection.setAtomSetName ("Orientation in Force Field");
return true;
}if (this.line.indexOf ("NORMAL COORDINATE ANALYSIS") >= 0) {
this.readFrequencies ();
return true;
}return true;
});
$_M(c$, "processTotalEnergy", 
function () {
});
$_M(c$, "processAtomicCharges", 
function () {
this.readLines (2);
this.atomSetCollection.newAtomSet ();
this.baseAtomIndex = this.atomSetCollection.getAtomCount ();
var expectedAtomNumber = 0;
while (this.readLine () != null) {
var atomNumber = this.parseIntStr (this.line);
if (atomNumber == -2147483648) break;
++expectedAtomNumber;
if (atomNumber != expectedAtomNumber) throw  new Exception ("unexpected atom number in atomic charges");
var atom = this.atomSetCollection.addNewAtom ();
atom.elementSymbol = this.parseToken ();
atom.partialCharge = this.parseFloat ();
}
this.chargesFound = true;
});
$_M(c$, "processCoordinates", 
function () {
this.readLines (3);
var expectedAtomNumber = 0;
if (!this.chargesFound) {
this.atomSetCollection.newAtomSet ();
this.baseAtomIndex = this.atomSetCollection.getAtomCount ();
} else {
this.chargesFound = false;
}var atoms = this.atomSetCollection.getAtoms ();
while (this.readLine () != null) {
var atomNumber = this.parseIntStr (this.line);
if (atomNumber == -2147483648) break;
++expectedAtomNumber;
if (atomNumber != expectedAtomNumber) throw  new Exception ("unexpected atom number in coordinates");
var elementSymbol = this.parseToken ();
var atom = atoms[this.baseAtomIndex + atomNumber - 1];
if (atom == null) {
atom = this.atomSetCollection.addNewAtom ();
}atom.atomSerial = atomNumber;
this.setAtomCoordXYZ (atom, this.parseFloat (), this.parseFloat (), this.parseFloat ());
var atno = this.parseIntStr (elementSymbol);
if (atno != -2147483648) elementSymbol = J.adapter.smarter.AtomSetCollectionReader.getElementSymbol (atno);
atom.elementSymbol = elementSymbol;
}
});
$_M(c$, "readFrequencies", 
($fz = function () {
var bsOK =  new J.util.BS ();
var n0 = this.atomSetCollection.getCurrentAtomSetIndex () + 1;
var tokens;
var done = false;
while (!done && this.readLine () != null && this.line.indexOf ("DESCRIPTION") < 0 && this.line.indexOf ("MASS-WEIGHTED") < 0) if (this.line.toUpperCase ().indexOf ("ROOT") >= 0) {
this.discardLinesUntilNonBlank ();
tokens = this.getTokens ();
if (Float.isNaN (J.util.Parser.parseFloatStrict (tokens[tokens.length - 1]))) {
this.discardLinesUntilNonBlank ();
tokens = this.getTokens ();
}var frequencyCount = tokens.length;
this.readLine ();
var iAtom0 = this.atomSetCollection.getAtomCount ();
var atomCount = this.atomSetCollection.getLastAtomSetAtomCount ();
var ignore =  Clazz.newBooleanArray (frequencyCount, false);
var freq1 = J.util.Parser.parseFloatStrict (tokens[0]);
var ignoreNegative = (freq1 < 0);
for (var i = 0; i < frequencyCount; ++i) {
ignore[i] = done || (done = (!ignoreNegative && J.util.Parser.parseFloatStrict (tokens[i]) < 1)) || !this.doGetVibration (++this.vibrationNumber);
if (ignore[i]) continue;
bsOK.set (this.vibrationNumber - 1);
this.atomSetCollection.cloneLastAtomSet ();
}
this.fillFrequencyData (iAtom0, atomCount, atomCount, ignore, false, 0, 0, null, 2);
}
var info =  new Array (this.vibrationNumber);
if (this.line.indexOf ("DESCRIPTION") < 0) this.discardLinesUntilContains ("DESCRIPTION");
while (this.discardLinesUntilContains ("VIBRATION") != null) {
tokens = this.getTokens ();
var freqNo = this.parseIntStr (tokens[1]);
tokens[0] = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ())[1];
if (tokens[2].equals ("ATOM")) tokens[2] = null;
info[freqNo - 1] = tokens;
if (freqNo == this.vibrationNumber) break;
}
for (var i = this.vibrationNumber - 1; --i >= 0; ) if (info[i] == null) info[i] = info[i + 1];

for (var i = 0, n = n0; i < this.vibrationNumber; i++) {
if (!bsOK.get (i)) continue;
this.atomSetCollection.setCurrentAtomSetIndex (n++);
this.atomSetCollection.setAtomSetFrequency (null, info[i][2], info[i][0], null);
}
}, $fz.isPrivate = true, $fz));
});
