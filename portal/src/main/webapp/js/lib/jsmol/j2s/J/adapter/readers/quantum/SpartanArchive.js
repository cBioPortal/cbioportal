Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (null, "J.adapter.readers.quantum.SpartanArchive", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "J.adapter.smarter.AtomSetCollectionReader", "$.Bond", "J.api.JmolAdapter", "J.constant.EnumQuantumShell", "J.util.ArrayUtil", "$.JmolList", "$.Logger", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atomCount = 0;
this.bondData = null;
this.moCount = 0;
this.coefCount = 0;
this.shellCount = 0;
this.gaussianCount = 0;
this.endCheck = null;
this.r = null;
this.modelAtomCount = 0;
this.line = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "SpartanArchive");
Clazz.makeConstructor (c$, 
function (r) {
this.initialize (r, "");
}, "J.adapter.readers.quantum.BasisFunctionReader");
Clazz.makeConstructor (c$, 
function (r, bondData, endCheck) {
this.initialize (r, bondData);
this.endCheck = endCheck;
}, "J.adapter.readers.quantum.BasisFunctionReader,~S,~S");
$_M(c$, "initialize", 
($fz = function (r, bondData) {
this.r = r;
r.moData.put ("isNormalized", Boolean.TRUE);
r.moData.put ("energyUnits", "");
this.bondData = bondData;
}, $fz.isPrivate = true, $fz), "J.adapter.readers.quantum.BasisFunctionReader,~S");
$_M(c$, "readArchive", 
function (infoLine, haveGeometryLine, atomCount0, doAddAtoms) {
this.modelAtomCount = this.setInfo (infoLine);
this.line = (haveGeometryLine ? "GEOMETRY" : "");
var haveMOData = false;
while (this.line != null) {
if (this.line.equals ("GEOMETRY")) {
this.readAtoms (atomCount0, doAddAtoms);
if (doAddAtoms && this.bondData.length > 0) this.addBonds (this.bondData, atomCount0);
} else if (this.line.indexOf ("BASIS") == 0) {
this.readBasis ();
} else if (this.line.indexOf ("WAVEFUNC") == 0 || this.line.indexOf ("BETA") == 0) {
if (this.r.doReadMolecularOrbitals) {
this.readMolecularOrbital ();
haveMOData = true;
}} else if (this.line.indexOf ("ENERGY") == 0) {
this.readEnergy ();
} else if (this.line.equals ("ENDARCHIVE") || this.endCheck != null && this.line.indexOf (this.endCheck) == 0) {
break;
}this.readLine ();
}
if (haveMOData) this.r.finalizeMOData (this.r.moData);
return this.atomCount;
}, "~S,~B,~N,~B");
$_M(c$, "readEnergy", 
($fz = function () {
var tokens = this.getTokens (this.readLine ());
var value = this.parseFloat (tokens[0]);
this.r.atomSetCollection.setAtomSetAuxiliaryInfo ("energy", Float.$valueOf (value));
if (Clazz.instanceOf (this.r, J.adapter.readers.quantum.SpartanSmolReader)) {
var prefix = (this.r).constraints;
this.r.atomSetCollection.setAtomSetName (prefix + (prefix.length == 0 ? "" : " ") + "Energy=" + value + " KJ");
}this.r.atomSetCollection.setAtomSetEnergy (tokens[0], value);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setInfo", 
($fz = function (info) {
var tokens = this.getTokens (info);
if (J.util.Logger.debugging) {
J.util.Logger.debug ("reading Spartan archive info :" + info);
}this.modelAtomCount = this.parseInt (tokens[0]);
this.coefCount = this.parseInt (tokens[1]);
this.shellCount = this.parseInt (tokens[2]);
this.gaussianCount = this.parseInt (tokens[3]);
this.moCount = this.parseInt (tokens[6]);
this.r.calculationType = tokens[9];
var s = this.r.moData.get ("calculationType");
if (s == null) s = this.r.calculationType;
 else if (s.indexOf (this.r.calculationType) < 0) s = this.r.calculationType + s;
this.r.moData.put ("calculationType", this.r.calculationType = s);
return this.modelAtomCount;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "readAtoms", 
($fz = function (atomCount0, doAddAtoms) {
for (var i = 0; i < this.modelAtomCount; i++) {
var tokens = this.getTokens (this.readLine ());
var atom = (doAddAtoms ? this.r.atomSetCollection.addNewAtom () : this.r.atomSetCollection.getAtom (atomCount0 - this.modelAtomCount + i));
atom.elementSymbol = J.adapter.smarter.AtomSetCollectionReader.getElementSymbol (this.parseInt (tokens[0]));
this.r.setAtomCoordXYZ (atom, this.parseFloat (tokens[1]) * 0.5291772, this.parseFloat (tokens[2]) * 0.5291772, this.parseFloat (tokens[3]) * 0.5291772);
}
if (doAddAtoms && J.util.Logger.debugging) {
J.util.Logger.debug (this.atomCount + " atoms read");
}}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "addBonds", 
function (data, atomCount0) {
var tokens = this.getTokens (data);
for (var i = this.modelAtomCount; i < tokens.length; ) {
var sourceIndex = this.parseInt (tokens[i++]) - 1 + atomCount0;
var targetIndex = this.parseInt (tokens[i++]) - 1 + atomCount0;
var bondOrder = this.parseInt (tokens[i++]);
if (bondOrder > 0) {
this.r.atomSetCollection.addBond ( new J.adapter.smarter.Bond (sourceIndex, targetIndex, bondOrder < 4 ? bondOrder : bondOrder == 5 ? 515 : 1));
}}
var bondCount = this.r.atomSetCollection.getBondCount ();
if (J.util.Logger.debugging) {
J.util.Logger.debug (bondCount + " bonds read");
}}, "~S,~N");
$_M(c$, "readBasis", 
function () {
var shells =  new J.util.JmolList ();
var gaussians = J.util.ArrayUtil.newFloat2 (this.gaussianCount);
var typeArray =  Clazz.newIntArray (this.gaussianCount, 0);
for (var i = 0; i < this.shellCount; i++) {
var tokens = this.getTokens (this.readLine ());
var flag4 = (tokens[4].charAt (0) == '1');
var slater =  Clazz.newIntArray (4, 0);
slater[0] = this.parseInt (tokens[3]) - 1;
var iBasis = this.parseInt (tokens[0]);
switch (iBasis) {
case 0:
iBasis = J.api.JmolAdapter.SHELL_S;
break;
case 1:
iBasis = J.api.JmolAdapter.SHELL_SP;
break;
case 2:
iBasis = (flag4 ? J.api.JmolAdapter.SHELL_D_SPHERICAL : J.api.JmolAdapter.SHELL_D_CARTESIAN);
break;
case 3:
iBasis = (flag4 ? J.api.JmolAdapter.SHELL_F_SPHERICAL : J.api.JmolAdapter.SHELL_F_CARTESIAN);
break;
}
slater[1] = iBasis;
var gaussianPtr = slater[2] = this.parseInt (tokens[2]) - 1;
var nGaussians = slater[3] = this.parseInt (tokens[1]);
for (var j = 0; j < nGaussians; j++) typeArray[gaussianPtr + j] = iBasis;

shells.addLast (slater);
}
for (var i = 0; i < this.gaussianCount; i++) {
var alpha = this.parseFloat (this.readLine ());
var tokens = this.getTokens (this.readLine ());
var nData = tokens.length;
var data =  Clazz.newFloatArray (nData + 1, 0);
data[0] = alpha;
switch (J.api.JmolAdapter.getShellEnumeration (typeArray[i])) {
case J.constant.EnumQuantumShell.S:
data[1] = this.parseFloat (tokens[0]);
break;
case J.constant.EnumQuantumShell.SP:
data[1] = this.parseFloat (tokens[0]);
data[2] = this.parseFloat (tokens[1]);
if (data[1] == 0) {
data[1] = data[2];
typeArray[i] = J.api.JmolAdapter.SHELL_P;
}break;
case J.constant.EnumQuantumShell.D_CARTESIAN:
case J.constant.EnumQuantumShell.D_SPHERICAL:
data[1] = this.parseFloat (tokens[2]);
break;
case J.constant.EnumQuantumShell.F_CARTESIAN:
case J.constant.EnumQuantumShell.F_SPHERICAL:
data[1] = this.parseFloat (tokens[3]);
break;
}
gaussians[i] = data;
}
var nCoeff = 0;
for (var i = 0; i < this.shellCount; i++) {
var slater = shells.get (i);
switch (J.api.JmolAdapter.getShellEnumeration (typeArray[slater[2]])) {
case J.constant.EnumQuantumShell.S:
nCoeff++;
break;
case J.constant.EnumQuantumShell.P:
slater[1] = J.api.JmolAdapter.SHELL_P;
nCoeff += 3;
break;
case J.constant.EnumQuantumShell.SP:
nCoeff += 4;
break;
case J.constant.EnumQuantumShell.D_SPHERICAL:
nCoeff += 5;
break;
case J.constant.EnumQuantumShell.D_CARTESIAN:
nCoeff += 6;
break;
case J.constant.EnumQuantumShell.F_SPHERICAL:
nCoeff += 7;
break;
case J.constant.EnumQuantumShell.F_CARTESIAN:
nCoeff += 10;
break;
}
}
var isD5F7 = (nCoeff < this.coefCount);
if (isD5F7) for (var i = 0; i < this.shellCount; i++) {
var slater = shells.get (i);
switch (J.api.JmolAdapter.getShellEnumeration (typeArray[i])) {
case J.constant.EnumQuantumShell.D_CARTESIAN:
slater[1] = J.api.JmolAdapter.SHELL_D_SPHERICAL;
break;
case J.constant.EnumQuantumShell.F_CARTESIAN:
slater[1] = J.api.JmolAdapter.SHELL_F_SPHERICAL;
break;
}
}
this.r.moData.put ("shells", shells);
this.r.moData.put ("gaussians", gaussians);
if (J.util.Logger.debugging) {
J.util.Logger.debug (shells.size () + " slater shells read");
J.util.Logger.debug (gaussians.length + " gaussian primitives read");
}});
$_M(c$, "readMolecularOrbital", 
function () {
var tokenPt = 0;
this.r.orbitals =  new J.util.JmolList ();
var tokens = this.getTokens ("");
var energies =  Clazz.newFloatArray (this.moCount, 0);
var coefficients =  Clazz.newFloatArray (this.moCount, this.coefCount, 0);
for (var i = 0; i < this.moCount; i++) {
if (tokenPt == tokens.length) {
tokens = this.getTokens (this.readLine ());
tokenPt = 0;
}energies[i] = this.parseFloat (tokens[tokenPt++]);
}
for (var i = 0; i < this.moCount; i++) {
for (var j = 0; j < this.coefCount; j++) {
if (tokenPt == tokens.length) {
tokens = this.getTokens (this.readLine ());
tokenPt = 0;
}coefficients[i][j] = this.parseFloat (tokens[tokenPt++]);
}
}
for (var i = 0; i < this.moCount; i++) {
var mo =  new java.util.Hashtable ();
mo.put ("energy", Float.$valueOf (energies[i]));
mo.put ("coefficients", coefficients[i]);
this.r.setMO (mo);
}
if (J.util.Logger.debugging) {
J.util.Logger.debug (this.r.orbitals.size () + " molecular orbitals read");
}this.r.moData.put ("mos", this.r.orbitals);
});
$_M(c$, "readProperties", 
function () {
if (J.util.Logger.debugging) J.util.Logger.debug ("Reading PROPARC properties records...");
while (this.readLine () != null && !this.line.startsWith ("ENDPROPARC") && !this.line.startsWith ("END Directory Entry ")) {
if (this.line.startsWith ("PROP")) this.readProperty ();
 else if (this.line.startsWith ("DIPOLE")) this.readDipole ();
 else if (this.line.startsWith ("VIBFREQ")) this.readVibFreqs ();
}
this.setVibrationsFromProperties ();
});
$_M(c$, "readDipole", 
function () {
this.setDipole (this.getTokens (this.readLine ()));
});
$_M(c$, "setDipole", 
($fz = function (tokens) {
if (tokens.length != 3) return;
var dipole = J.util.V3.new3 (this.parseFloat (tokens[0]), this.parseFloat (tokens[1]), this.parseFloat (tokens[2]));
this.r.atomSetCollection.setAtomSetAuxiliaryInfo ("dipole", dipole);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "readProperty", 
($fz = function () {
var tokens = this.getTokens (this.line);
if (tokens.length == 0) return;
var isString = (tokens[1].startsWith ("STRING"));
var keyName = tokens[2];
var isDipole = (keyName.equals ("DIPOLE_VEC"));
var value =  new JavaObject ();
var vector =  new J.util.JmolList ();
if (tokens[3].equals ("=")) {
if (isString) {
value = this.getQuotedString (tokens[4].substring (0, 1));
} else {
value = Float.$valueOf (this.parseFloat (tokens[4]));
}} else if (tokens[tokens.length - 1].equals ("BEGIN")) {
var nValues = this.parseInt (tokens[tokens.length - 2]);
if (nValues == 0) nValues = 1;
var isArray = (tokens.length == 6);
var atomInfo =  new J.util.JmolList ();
var ipt = 0;
while (this.readLine () != null && !this.line.substring (0, 3).equals ("END")) {
if (isString) {
value = this.getQuotedString ("\"");
vector.addLast (value);
} else {
var tokens2 = this.getTokens (this.line);
if (isDipole) this.setDipole (tokens2);
for (var i = 0; i < tokens2.length; i++, ipt++) {
if (isArray) {
atomInfo.addLast (Float.$valueOf (this.parseFloat (tokens2[i])));
if ((ipt + 1) % nValues == 0) {
vector.addLast (atomInfo);
atomInfo =  new J.util.JmolList ();
}} else {
value = Float.$valueOf (this.parseFloat (tokens2[i]));
vector.addLast (value);
}}
}}
value = null;
} else {
if (J.util.Logger.debugging) {
J.util.Logger.debug (" Skipping property line " + this.line);
}}if (value != null) this.r.atomSetCollection.setAtomSetCollectionAuxiliaryInfo (keyName, value);
if (vector.size () != 0) this.r.atomSetCollection.setAtomSetCollectionAuxiliaryInfo (keyName, vector);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readVibFreqs", 
function () {
this.readLine ();
var label = "";
var frequencyCount = this.parseInt (this.line);
var vibrations =  new J.util.JmolList ();
var freqs =  new J.util.JmolList ();
if (J.util.Logger.debugging) {
J.util.Logger.debug ("reading VIBFREQ vibration records: frequencyCount = " + frequencyCount);
}var ignore =  Clazz.newBooleanArray (frequencyCount, false);
for (var i = 0; i < frequencyCount; ++i) {
var atomCount0 = this.r.atomSetCollection.getAtomCount ();
ignore[i] = !this.r.doGetVibration (i + 1);
if (!ignore[i] && this.r.desiredVibrationNumber <= 0) {
this.r.atomSetCollection.cloneLastAtomSet ();
this.addBonds (this.bondData, atomCount0);
}this.readLine ();
var info =  new java.util.Hashtable ();
var freq = this.parseFloat (this.line);
info.put ("freq", Float.$valueOf (freq));
if (this.line.length > 15 && !(label = this.line.substring (15, this.line.length)).equals ("???")) info.put ("label", label);
freqs.addLast (info);
if (!ignore[i]) {
this.r.atomSetCollection.setAtomSetFrequency (null, label, "" + freq, null);
}}
this.r.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("VibFreqs", freqs);
var atomCount = this.r.atomSetCollection.getFirstAtomSetAtomCount ();
var vib =  new J.util.JmolList ();
var vibatom =  new J.util.JmolList ();
var ifreq = 0;
var iatom = atomCount;
var nValues = 3;
var atomInfo =  Clazz.newFloatArray (3, 0);
while (this.readLine () != null) {
var tokens2 = this.getTokens (this.line);
for (var i = 0; i < tokens2.length; i++) {
var f = this.parseFloat (tokens2[i]);
atomInfo[i % nValues] = f;
vibatom.addLast (Float.$valueOf (f));
if ((i + 1) % nValues == 0) {
if (!ignore[ifreq]) {
this.r.atomSetCollection.addVibrationVector (iatom, atomInfo[0], atomInfo[1], atomInfo[2]);
vib.addLast (vibatom);
vibatom =  new J.util.JmolList ();
}++iatom;
}}
if (iatom % atomCount == 0) {
if (!ignore[ifreq]) {
vibrations.addLast (vib);
}vib =  new J.util.JmolList ();
if (++ifreq == frequencyCount) {
break;
}}}
this.r.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("vibration", vibrations);
});
$_M(c$, "setVibrationsFromProperties", 
($fz = function () {
var freq_modes = this.r.atomSetCollection.getAtomSetCollectionAuxiliaryInfo ("FREQ_MODES");
if (freq_modes == null) {
return;
}var freq_lab = this.r.atomSetCollection.getAtomSetCollectionAuxiliaryInfo ("FREQ_LAB");
var freq_val = this.r.atomSetCollection.getAtomSetCollectionAuxiliaryInfo ("FREQ_VAL");
var frequencyCount = freq_val.size ();
var vibrations =  new J.util.JmolList ();
var freqs =  new J.util.JmolList ();
if (J.util.Logger.debugging) {
J.util.Logger.debug ("reading PROP VALUE:VIB FREQ_MODE vibration records: frequencyCount = " + frequencyCount);
}var v;
for (var i = 0; i < frequencyCount; ++i) {
var atomCount0 = this.r.atomSetCollection.getAtomCount ();
this.r.atomSetCollection.cloneLastAtomSet ();
this.addBonds (this.bondData, atomCount0);
var info =  new java.util.Hashtable ();
info.put ("freq", (v = freq_val.get (i)));
var freq = v.floatValue ();
var label = freq_lab.get (i);
if (!label.equals ("???")) {
info.put ("label", label);
}freqs.addLast (info);
this.r.atomSetCollection.setAtomSetName (label + " " + freq + " cm^-1");
this.r.atomSetCollection.setAtomSetModelProperty ("Frequency", freq + " cm^-1");
this.r.atomSetCollection.setAtomSetModelProperty (".PATH", "Frequencies");
}
this.r.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("VibFreqs", freqs);
var atomCount = this.r.atomSetCollection.getFirstAtomSetAtomCount ();
var iatom = atomCount;
for (var i = 0; i < frequencyCount; i++) {
if (!this.r.doGetVibration (i + 1)) continue;
var ipt = 0;
var vib =  new J.util.JmolList ();
var mode = freq_modes.get (i);
for (var ia = 0; ia < atomCount; ia++, iatom++) {
var vibatom =  new J.util.JmolList ();
var vx = (v = mode.get (ipt++)).floatValue ();
vibatom.addLast (v);
var vy = (v = mode.get (ipt++)).floatValue ();
vibatom.addLast (v);
var vz = (v = mode.get (ipt++)).floatValue ();
vibatom.addLast (v);
this.r.atomSetCollection.addVibrationVector (iatom, vx, vy, vz);
vib.addLast (vibatom);
}
vibrations.addLast (vib);
}
this.r.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("vibration", vibrations);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getQuotedString", 
($fz = function (strQuote) {
var i = this.line.indexOf (strQuote);
var j = this.line.lastIndexOf (strQuote);
return (j == i ? "" : this.line.substring (i + 1, j));
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "parseInt", 
($fz = function (info) {
return this.r.parseIntStr (info);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "parseFloat", 
($fz = function (info) {
return this.r.parseFloatStr (info);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getTokens", 
($fz = function (s) {
return J.adapter.smarter.AtomSetCollectionReader.getTokensStr (s);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "readLine", 
($fz = function () {
return (this.line = this.r.readLine ());
}, $fz.isPrivate = true, $fz));
});
