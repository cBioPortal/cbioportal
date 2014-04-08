Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.MOReader"], "J.adapter.readers.quantum.JaguarReader", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "J.api.JmolAdapter", "J.util.ArrayUtil", "$.JmolList", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.moCount = 0;
this.lumoEnergy = 3.4028235E38;
this.haveLine = false;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "JaguarReader", J.adapter.readers.quantum.MOReader);
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.startsWith (" Input geometry:") || this.line.startsWith (" Symmetrized geometry:") || this.line.startsWith ("  final geometry:")) {
this.readAtoms ();
return true;
}if (this.line.startsWith ("  Atomic charges from electrostatic potential:")) {
this.readCharges ();
return true;
}if (this.line.startsWith ("  number of basis functions....")) {
this.moCount = this.parseIntStr (this.line.substring (32).trim ());
return true;
}if (this.line.startsWith ("  basis set:")) {
this.moData.put ("energyUnits", "");
this.moData.put ("calculationType", this.calculationType = this.line.substring (13).trim ());
return true;
}if (this.line.indexOf ("XXXXXShell information") >= 0) {
this.readUnnormalizedBasis ();
return true;
}if (this.line.indexOf ("Normalized coefficients") >= 0) {
this.readBasisNormalized ();
return true;
}if (this.line.startsWith (" LUMO energy:")) {
this.lumoEnergy = this.parseFloatStr (this.line.substring (13));
return true;
}if (this.line.indexOf ("final wvfn") >= 0) {
this.readJaguarMolecularOrbitals ();
return true;
}if (this.line.startsWith ("  harmonic frequencies in")) {
this.readFrequencies ();
this.continuing = false;
return false;
}return this.checkNboLine ();
});
$_M(c$, "readAtoms", 
($fz = function () {
this.atomSetCollection.discardPreviousAtoms ();
this.readLines (2);
var atomCount = 0;
while (this.readLine () != null && this.line.length >= 60 && this.line.charAt (2) != ' ') {
var tokens = this.getTokens ();
var atomName = tokens[0];
var x = this.parseFloatStr (tokens[1]);
var y = this.parseFloatStr (tokens[2]);
var z = this.parseFloatStr (tokens[3]);
if (Float.isNaN (x) || Float.isNaN (y) || Float.isNaN (z) || atomName.length < 2) return;
var elementSymbol;
var ch2 = atomName.charAt (1);
if (ch2 >= 'a' && ch2 <= 'z') elementSymbol = atomName.substring (0, 2);
 else elementSymbol = atomName.substring (0, 1);
var atom = this.atomSetCollection.addNewAtom ();
atom.elementSymbol = elementSymbol;
atom.atomName = atomName;
this.setAtomCoordXYZ (atom, x, y, z);
atomCount++;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readCharges", 
($fz = function () {
var iAtom = 0;
while (this.readLine () != null && this.line.indexOf ("sum") < 0) {
if (this.line.indexOf ("Charge") < 0) continue;
var tokens = this.getTokens ();
for (var i = 1; i < tokens.length; i++) this.atomSetCollection.getAtom (iAtom++).partialCharge = this.parseFloatStr (tokens[i]);

}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readUnnormalizedBasis", 
($fz = function () {
var lastAtom = "";
var iAtom = -1;
var sdata =  Clazz.newIntArray (this.moCount, 4, 0);
var sgdata = J.util.ArrayUtil.createArrayOfArrayList (this.moCount);
var tokens;
this.gaussianCount = 0;
this.discardLinesUntilContains ("--------");
while (this.readLine () != null && (tokens = this.getTokens ()).length == 9) {
var jCont = this.parseIntStr (tokens[2]);
if (jCont > 0) {
if (!tokens[0].equals (lastAtom)) iAtom++;
lastAtom = tokens[0];
var iFunc = this.parseIntStr (tokens[5]);
var iType = this.parseIntStr (tokens[4]);
if (iType <= 2) iType--;
if (sgdata[iFunc] == null) {
sdata[iFunc][0] = iAtom;
sdata[iFunc][1] = iType;
sdata[iFunc][2] = 0;
sdata[iFunc][3] = 0;
sgdata[iFunc] =  new J.util.JmolList ();
}var factor = 1;
sgdata[iFunc].addLast ([this.parseFloatStr (tokens[6]), this.parseFloatStr (tokens[8]) * factor]);
this.gaussianCount += jCont;
for (var i = jCont - 1; --i >= 0; ) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
sgdata[iFunc].addLast ([this.parseFloatStr (tokens[6]), this.parseFloatStr (tokens[8]) * factor]);
}
}}
var garray = J.util.ArrayUtil.newFloat2 (this.gaussianCount);
var sarray =  new J.util.JmolList ();
this.gaussianCount = 0;
for (var i = 0; i < this.moCount; i++) if (sgdata[i] != null) {
var n = sgdata[i].size ();
sdata[i][2] = this.gaussianCount;
sdata[i][3] = n;
for (var j = 0; j < n; j++) {
garray[this.gaussianCount++] = sgdata[i].get (j);
}
sarray.addLast (sdata[i]);
}
this.moData.put ("shells", sarray);
this.moData.put ("gaussians", garray);
if (J.util.Logger.debugging) {
J.util.Logger.debug (sarray.size () + " slater shells read");
J.util.Logger.debug (this.gaussianCount + " gaussian primitives read");
}}, $fz.isPrivate = true, $fz));
$_M(c$, "readBasisNormalized", 
($fz = function () {
var lastAtom = "";
var iAtom = -1;
var id;
var iFunc = 0;
var iFuncLast = -1;
var sarray =  new J.util.JmolList ();
var gdata =  new J.util.JmolList ();
this.gaussianCount = 0;
var sdata = null;
this.discardLinesUntilContains ("--------");
while (this.readLine () != null && this.line.length > 3) {
var tokens = this.getTokens ();
if (tokens.length == 4) {
id = tokens[0];
continue;
}if (!tokens[0].equals (lastAtom)) iAtom++;
lastAtom = tokens[0];
id = tokens[2];
var iType = J.api.JmolAdapter.getQuantumShellTagID (id);
iFunc = this.parseIntStr (tokens[3]) - 1;
if (iFunc == iFuncLast) {
} else {
sdata = [iAtom, iType, this.gaussianCount, 0];
sarray.addLast (sdata);
iFuncLast = iFunc;
}this.gaussianCount++;
sdata[3]++;
var z = this.parseFloatStr (tokens[4]);
var rCoef = this.parseFloatStr (tokens[5]);
if (id.equals ("XX")) rCoef *= 1.7320508;
gdata.addLast ([z, rCoef]);
}
var garray = J.util.ArrayUtil.newFloat2 (this.gaussianCount);
for (var i = gdata.size (); --i >= 0; ) garray[i] = gdata.get (i);

this.moData.put ("shells", sarray);
this.moData.put ("gaussians", garray);
if (J.util.Logger.debugging) {
J.util.Logger.debug (sarray.size () + " slater shells read");
J.util.Logger.debug (this.gaussianCount + " gaussian primitives read");
}this.moData.put ("isNormalized", Boolean.TRUE);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readJaguarMolecularOrbitals", 
($fz = function () {
var dataBlock =  new Array (this.moCount);
this.readLine ();
this.readLine ();
this.readLine ();
var nMo = 0;
while (this.line != null) {
this.readLine ();
this.readLine ();
this.readLine ();
if (this.line == null || this.line.indexOf ("eigenvalues-") < 0) break;
var eigenValues = this.getTokens ();
var n = eigenValues.length - 1;
this.fillDataBlock (dataBlock, 0);
for (var iOrb = 0; iOrb < n; iOrb++) {
var coefs =  Clazz.newFloatArray (this.moCount, 0);
var mo =  new java.util.Hashtable ();
var energy = this.parseFloatStr (eigenValues[iOrb + 1]);
mo.put ("energy", Float.$valueOf (energy));
if (Math.abs (energy - this.lumoEnergy) < 0.0001) {
this.moData.put ("HOMO", Integer.$valueOf (nMo));
this.lumoEnergy = 3.4028235E38;
}nMo++;
for (var i = 0, pt = 0; i < this.moCount; i++) {
coefs[pt++] = this.parseFloatStr (dataBlock[i][iOrb + 3]);
}
mo.put ("coefficients", coefs);
this.setMO (mo);
}
}
this.moData.put ("mos", this.orbitals);
this.finalizeMOData (this.moData);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readFrequencies", 
($fz = function () {
var atomCount = this.atomSetCollection.getLastAtomSetAtomCount ();
this.discardLinesUntilStartsWith ("  frequencies ");
while (this.line != null && this.line.startsWith ("  frequencies ")) {
var iAtom0 = this.atomSetCollection.getAtomCount ();
var frequencies = this.getTokens ();
var frequencyCount = frequencies.length - 1;
var ignore =  Clazz.newBooleanArray (frequencyCount, false);
var symmetries = null;
var intensities = null;
while (this.line != null && this.line.charAt (2) != ' ') {
if (this.line.indexOf ("symmetries") >= 0) symmetries = this.getTokens ();
 else if (this.line.indexOf ("intensities") >= 0) intensities = this.getTokens ();
this.readLine ();
}
for (var i = 0; i < frequencyCount; i++) {
ignore[i] = !this.doGetVibration (++this.vibrationNumber);
if (ignore[i]) continue;
this.atomSetCollection.cloneFirstAtomSet (0);
this.atomSetCollection.setAtomSetFrequency (null, symmetries == null ? null : symmetries[i + 1], frequencies[i + 1], null);
if (intensities != null) this.atomSetCollection.setAtomSetModelProperty ("IRIntensity", intensities[i + 1] + " km/mol");
}
this.haveLine = true;
this.fillFrequencyData (iAtom0, atomCount, atomCount, ignore, false, 0, 0, null, 0);
this.readLine ();
this.readLine ();
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readLine", 
function () {
if (!this.haveLine) return Clazz.superCall (this, J.adapter.readers.quantum.JaguarReader, "readLine", []);
this.haveLine = false;
return this.line;
});
Clazz.defineStatics (c$,
"ROOT3", 1.73205080756887729);
});
