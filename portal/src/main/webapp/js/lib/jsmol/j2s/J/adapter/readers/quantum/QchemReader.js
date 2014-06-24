Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.MOReader", "$.BasisFunctionReader"], "J.adapter.readers.quantum.QchemReader", ["java.lang.Float", "java.util.Hashtable", "J.api.JmolAdapter", "J.util.ArrayUtil", "$.JmolList", "$.Logger", "$.Parser"], function () {
c$ = Clazz.decorateAsClass (function () {
this.calculationNumber = 1;
this.alphas = null;
this.betas = null;
this.nBasis = 0;
this.dFixed = false;
this.fFixed = false;
this.dList = "";
this.fList = "";
this.dSpherical = false;
this.fSpherical = false;
if (!Clazz.isClassDefined ("J.adapter.readers.quantum.QchemReader.MOInfo")) {
J.adapter.readers.quantum.QchemReader.$QchemReader$MOInfo$ ();
}
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "QchemReader", J.adapter.readers.quantum.MOReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.energyUnits = "au";
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.indexOf ("Standard Nuclear Orientation") >= 0) {
this.readAtoms ();
this.moData = null;
return true;
}if (this.line.indexOf ("Requested basis set is") >= 0) {
this.readCalculationType ();
return true;
}if (this.line.indexOf ("VIBRATIONAL FREQUENCIES") >= 0) {
this.readFrequencies ();
return true;
}if (this.line.indexOf ("Mulliken Net Atomic Charges") >= 0) {
this.readPartialCharges ();
return true;
}if (this.line.startsWith ("Job ")) {
this.calculationNumber++;
this.moData = null;
return true;
}if (this.line.indexOf ("Basis set in general basis input format") >= 0) {
if (this.moData == null) {
this.readBasis ();
}return true;
}if (this.moData == null) return true;
if (this.line.indexOf ("Orbital Energies (a.u.) and Symmetries") >= 0) {
this.readESym (true);
return true;
}if (this.line.indexOf ("Orbital Energies (a.u.)") >= 0) {
this.readESym (false);
return true;
}if (this.line.indexOf ("MOLECULAR ORBITAL COEFFICIENTS") >= 0) {
if (this.filterMO ()) this.readQchemMolecularOrbitals ();
return true;
}return this.checkNboLine ();
});
$_M(c$, "readCalculationType", 
($fz = function () {
this.calculationType = this.line.substring (this.line.indexOf ("set is") + 6).trim ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "readAtoms", 
($fz = function () {
this.atomSetCollection.newAtomSet ();
this.setMOData (true);
this.readLines (2);
var tokens;
while (this.readLine () != null && !this.line.startsWith (" --")) {
tokens = this.getTokens ();
if (tokens.length < 5) continue;
var symbol = tokens[1];
if (J.api.JmolAdapter.getElementNumber (symbol) < 1) continue;
var x = this.parseFloatStr (tokens[2]);
var y = this.parseFloatStr (tokens[3]);
var z = this.parseFloatStr (tokens[4]);
if (Float.isNaN (x) || Float.isNaN (y) || Float.isNaN (z)) continue;
var atom = this.atomSetCollection.addNewAtom ();
atom.elementSymbol = symbol;
this.setAtomCoordXYZ (atom, x, y, z);
this.atomSetCollection.setAtomSetModelProperty (".PATH", "Calculation " + this.calculationNumber);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readFrequencies", 
($fz = function () {
while (this.readLine () != null && this.line.indexOf ("STANDARD") < 0) {
if (!this.line.startsWith (" Frequency:")) this.discardLinesUntilStartsWith (" Frequency:");
var frequencies = this.getTokens ();
var frequencyCount = frequencies.length - 1;
var ignore =  Clazz.newBooleanArray (frequencyCount, false);
var atomCount = this.atomSetCollection.getLastAtomSetAtomCount ();
var iAtom0 = this.atomSetCollection.getAtomCount ();
for (var i = 0; i < frequencyCount; ++i) {
ignore[i] = !this.doGetVibration (++this.vibrationNumber);
if (ignore[i]) continue;
this.atomSetCollection.cloneLastAtomSet ();
this.atomSetCollection.setAtomSetFrequency ("Calculation " + this.calculationNumber, null, frequencies[i + 1], null);
}
this.discardLinesUntilStartsWith ("               X");
this.fillFrequencyData (iAtom0, atomCount, atomCount, ignore, true, 0, 0, null, 0);
this.discardLinesUntilBlank ();
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readPartialCharges", 
($fz = function () {
this.readLines (3);
var atoms = this.atomSetCollection.getAtoms ();
var atomCount = this.atomSetCollection.getLastAtomSetAtomCount ();
for (var i = 0; i < atomCount && this.readLine () != null; ++i) atoms[i].partialCharge = this.parseFloatStr (this.getTokens ()[2]);

}, $fz.isPrivate = true, $fz));
$_M(c$, "readBasis", 
($fz = function () {
this.moData =  new java.util.Hashtable ();
var atomCount = 0;
var shellCount = 0;
var gaussianCount = 0;
this.shells =  new J.util.JmolList ();
var gdata =  new J.util.JmolList ();
var tokens;
this.discardLinesUntilStartsWith ("$basis");
this.readLine ();
while (this.readLine () != null) {
if (this.line.startsWith ("****")) {
atomCount++;
if (this.readLine () != null && this.line.startsWith ("$end")) break;
continue;
}shellCount++;
var slater =  Clazz.newIntArray (4, 0);
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line);
slater[0] = atomCount;
slater[1] = J.api.JmolAdapter.getQuantumShellTagID (tokens[0]);
slater[2] = gaussianCount;
var nGaussians = this.parseIntStr (tokens[1]);
slater[3] = nGaussians;
this.shells.addLast (slater);
gaussianCount += nGaussians;
for (var i = 0; i < nGaussians; i++) {
gdata.addLast (J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ()));
}
}
this.gaussians = J.util.ArrayUtil.newFloat2 (gaussianCount);
for (var i = 0; i < gaussianCount; i++) {
tokens = gdata.get (i);
this.gaussians[i] =  Clazz.newFloatArray (tokens.length, 0);
for (var j = 0; j < tokens.length; j++) this.gaussians[i][j] = this.parseFloatStr (tokens[j]);

}
if (J.util.Logger.debugging) {
J.util.Logger.debug (shellCount + " slater shells read");
J.util.Logger.debug (gaussianCount + " gaussian primitives read");
}this.discardLinesUntilStartsWith (" There are");
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line);
this.nBasis = this.parseIntStr (tokens[5]);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readESym", 
($fz = function (haveSym) {
this.alphas =  new Array (this.nBasis);
this.betas =  new Array (this.nBasis);
var moInfos;
var ne = 0;
var readBetas = false;
this.discardLinesUntilStartsWith (" Alpha");
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line);
moInfos = this.alphas;
for (var e = 0; e < 2; e++) {
var nMO = 0;
while (this.readLine () != null) {
if (this.line.startsWith (" -- ")) {
ne = 0;
if (this.line.indexOf ("Vacant") < 0) {
if (this.line.indexOf ("Occupied") > 0) ne = 1;
}this.readLine ();
}if (this.line.startsWith (" -------")) {
e = 2;
break;
}var nOrbs = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line).length;
if (nOrbs == 0 || this.line.startsWith (" Warning")) {
this.discardLinesUntilStartsWith (" Beta");
readBetas = true;
moInfos = this.betas;
break;
}if (haveSym) tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
for (var i = 0, j = 0; i < nOrbs; i++, j += 2) {
var info = Clazz.innerTypeInstance (J.adapter.readers.quantum.QchemReader.MOInfo, this, null);
info.ne = ne;
if (haveSym) info.moSymmetry = tokens[j] + tokens[j + 1] + " ";
moInfos[nMO] = info;
nMO++;
}
}
}
if (!readBetas) this.betas = this.alphas;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "readQchemMolecularOrbitals", 
($fz = function () {
var nMOs;
var orbitalType = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line)[0];
this.alphaBeta = (orbitalType.equals ("RESTRICTTED") ? "" : "A");
nMOs = this.readMOs (orbitalType.equals ("RESTRICTED"), this.alphas);
if (orbitalType.equals ("ALPHA")) {
this.discardLinesUntilContains ("BETA");
this.alphaBeta = "B";
nMOs += this.readMOs (false, this.betas);
}var isOK = true;
if (this.dList.length > 0) {
if (this.dSpherical) isOK = this.getDFMap (this.dList, J.api.JmolAdapter.SHELL_D_SPHERICAL, J.adapter.readers.quantum.QchemReader.$DS_LIST, 2);
 else isOK = this.getDFMap (this.dList, J.api.JmolAdapter.SHELL_D_CARTESIAN, J.adapter.readers.quantum.QchemReader.$DC_LIST, 3);
if (!isOK) {
J.util.Logger.error ("atomic orbital order is unrecognized -- skipping reading of MOs. dList=" + this.dList);
this.shells = null;
}}if (this.fList.length > 0) {
if (this.fSpherical) isOK = this.getDFMap (this.fList, J.api.JmolAdapter.SHELL_F_SPHERICAL, J.adapter.readers.quantum.QchemReader.$FS_LIST, 2);
 else isOK = this.getDFMap (this.fList, J.api.JmolAdapter.SHELL_F_CARTESIAN, J.adapter.readers.quantum.QchemReader.$FC_LIST, 3);
if (!isOK) {
J.util.Logger.error ("atomic orbital order is unrecognized -- skipping reading of MOs. fList=" + this.fList);
this.shells = null;
}}this.setMOData (this.shells == null);
this.shells = null;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readMOs", 
($fz = function (restricted, moInfos) {
var mos = J.util.ArrayUtil.createArrayOfHashtable (6);
var mocoef = J.util.ArrayUtil.newFloat2 (6);
var moid =  Clazz.newIntArray (6, 0);
var tokens;
var energy;
var nMOs = 0;
while (this.readLine ().length > 2) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line);
var nMO = tokens.length;
energy = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ().substring (13));
for (var i = 0; i < nMO; i++) {
moid[i] = this.parseIntStr (tokens[i]) - 1;
mocoef[i] =  Clazz.newFloatArray (this.nBasis, 0);
mos[i] =  new java.util.Hashtable ();
}
for (var i = 0, pt = 0; i < this.nBasis; i++) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var s = this.line.substring (12, 17).trim ();
var ch = s.charAt (0);
switch (ch) {
case 'd':
s = s.substring (s.length - 3).toUpperCase ();
if (s.startsWith ("D ")) {
if (!this.dFixed) this.fixSlaterTypes (J.api.JmolAdapter.SHELL_D_CARTESIAN, J.api.JmolAdapter.SHELL_D_SPHERICAL);
s = "D" + s.charAt (2);
this.dSpherical = true;
}if (this.dList.indexOf (s) < 0) this.dList += s + " ";
this.dFixed = true;
break;
case 'f':
s = s.substring (s.length - 3).toUpperCase ();
if (s.startsWith ("F ")) {
if (!this.fFixed) this.fixSlaterTypes (J.api.JmolAdapter.SHELL_F_CARTESIAN, J.api.JmolAdapter.SHELL_F_SPHERICAL);
s = "F" + s.charAt (2);
this.fSpherical = true;
}if (this.fList.indexOf (s) < 0) this.fList += s + " ";
this.fFixed = true;
break;
default:
if (!this.isQuantumBasisSupported (ch)) continue;
break;
}
for (var j = tokens.length - nMO, k = 0; k < nMO; j++, k++) mocoef[k][pt] = this.parseFloatStr (tokens[j]);

pt++;
}
for (var i = 0; i < nMO; i++) {
var moInfo = moInfos[moid[i]];
mos[i].put ("energy", Float.$valueOf (J.util.Parser.fVal (energy[i])));
mos[i].put ("coefficients", mocoef[i]);
var label = this.alphaBeta;
var ne = moInfo.ne;
if (restricted) ne = this.alphas[moid[i]].ne + this.betas[moid[i]].ne;
mos[i].put ("occupancy", Float.$valueOf (ne));
switch (ne) {
case 2:
label = "AB";
break;
case 1:
break;
case 0:
if (restricted) label = "V";
 else label = "V" + label;
break;
}
mos[i].put ("symmetry", moInfo.moSymmetry + label + "(" + (moid[i] + 1) + ")");
this.orbitals.addLast (mos[i]);
}
nMOs += nMO;
}
return nMOs;
}, $fz.isPrivate = true, $fz), "~B,~A");
c$.$QchemReader$MOInfo$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.ne = 0;
this.moSymmetry = "";
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum.QchemReader, "MOInfo");
c$ = Clazz.p0p ();
};
c$.$DC_LIST = c$.prototype.$DC_LIST = J.adapter.readers.quantum.BasisFunctionReader.CANONICAL_DC_LIST;
Clazz.defineStatics (c$,
"$DS_LIST", "D3    D4    D2    D5    D1");
c$.$FC_LIST = c$.prototype.$FC_LIST = J.adapter.readers.quantum.BasisFunctionReader.CANONICAL_FC_LIST;
Clazz.defineStatics (c$,
"$FS_LIST", "F4    F5    F3    F6    F2    F7    F1");
});
