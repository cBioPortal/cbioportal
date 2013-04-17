Clazz.declarePackage ("J.adapter.readers.simple");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.simple.AmpacReader", ["J.util.P3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atomCount = 0;
this.freqAtom0 = -1;
this.partialCharges = null;
this.atomPositions = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.simple, "AmpacReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.indexOf ("CARTESIAN COORDINATES") >= 0) {
if (!this.doGetModel (++this.modelNumber, null)) return this.checkLastModel ();
this.readCoordinates ();
return true;
}if (!this.doProcessLines) return true;
if (this.line.indexOf ("NET ATOMIC CHARGES") >= 0) {
this.readPartialCharges ();
return true;
}if (this.line.indexOf ("VIBRATIONAL FREQUENCIES") >= 0) {
this.readFrequencies ();
return true;
}return true;
});
$_M(c$, "readCoordinates", 
($fz = function () {
var haveFreq = (this.freqAtom0 >= 0);
if (haveFreq) {
this.atomPositions =  new Array (this.atomCount);
} else {
this.atomSetCollection.newAtomSet ();
}this.readLine ();
this.atomCount = 0;
while (this.readLine () != null) {
var tokens = this.getTokens ();
if (tokens.length < 5) break;
if (haveFreq) {
this.atomPositions[this.atomCount] = J.util.P3.new3 (this.parseFloatStr (tokens[2]), this.parseFloatStr (tokens[3]), this.parseFloatStr (tokens[4]));
} else {
var symbol = tokens[1];
var atom = this.atomSetCollection.addNewAtom ();
atom.elementSymbol = symbol;
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[2]), this.parseFloatStr (tokens[3]), this.parseFloatStr (tokens[4]));
}this.atomCount++;
}
if (haveFreq) this.setPositions ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setPositions", 
($fz = function () {
var maxAtom = this.atomSetCollection.getAtomCount ();
var atoms = this.atomSetCollection.getAtoms ();
for (var i = this.freqAtom0; i < maxAtom; i++) {
atoms[i].setT (this.atomPositions[i % this.atomCount]);
atoms[i].partialCharge = this.partialCharges[i % this.atomCount];
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readPartialCharges", 
($fz = function () {
this.readLine ();
this.partialCharges =  Clazz.newFloatArray (this.atomCount, 0);
var tokens;
for (var i = 0; i < this.atomCount; i++) {
if (this.readLine () == null || (tokens = this.getTokens ()).length < 4) break;
this.partialCharges[i] = this.parseFloatStr (tokens[2]);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readFrequencies", 
($fz = function () {
while (this.readLine () != null && this.line.indexOf ("FREQ  :") < 0) {
}
while (this.line != null && this.line.indexOf ("FREQ  :") >= 0) {
var frequencies = this.getTokens ();
while (this.readLine () != null && this.line.indexOf ("IR I") < 0) {
}
var iAtom0 = this.atomSetCollection.getAtomCount ();
if (this.vibrationNumber == 0) this.freqAtom0 = iAtom0;
var frequencyCount = frequencies.length - 2;
var ignore =  Clazz.newBooleanArray (frequencyCount, false);
for (var i = 0; i < frequencyCount; ++i) {
ignore[i] = !this.doGetVibration (++this.vibrationNumber);
if (ignore[i]) continue;
this.atomSetCollection.cloneLastAtomSet ();
this.atomSetCollection.setAtomSetName (frequencies[i + 2] + " cm^-1");
this.atomSetCollection.setAtomSetModelProperty ("Frequency", frequencies[i + 2] + " cm^-1");
this.atomSetCollection.setAtomSetModelProperty (".PATH", "Frequencies");
}
this.fillFrequencyData (iAtom0, this.atomCount, this.atomCount, ignore, false, 8, 9, null, 0);
this.readLine ();
this.readLine ();
}
}, $fz.isPrivate = true, $fz));
});
