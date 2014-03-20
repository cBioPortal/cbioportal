Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.MopacSlaterReader", "J.util.BS"], "J.adapter.readers.quantum.MoldenReader", ["java.lang.Exception", "$.Float", "java.util.Arrays", "$.Hashtable", "J.adapter.readers.quantum.BasisFunctionReader", "J.api.JmolAdapter", "J.util.ArrayUtil", "$.JmolList", "$.Logger", "$.Parser"], function () {
c$ = Clazz.decorateAsClass (function () {
this.loadGeometries = false;
this.loadVibrations = false;
this.vibOnly = false;
this.optOnly = false;
this.doSort = true;
this.orbitalType = "";
this.modelAtomCount = 0;
this.bsAtomOK = null;
this.bsBadIndex = null;
this.nSPDF = null;
this.haveEnergy = true;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "MoldenReader", J.adapter.readers.quantum.MopacSlaterReader);
Clazz.prepareFields (c$, function () {
this.bsAtomOK =  new J.util.BS ();
this.bsBadIndex =  new J.util.BS ();
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.vibOnly = this.checkFilterKey ("VIBONLY");
this.optOnly = this.checkFilterKey ("OPTONLY");
this.doSort = !this.checkFilterKey ("NOSORT");
this.loadGeometries = !this.vibOnly && this.desiredVibrationNumber < 0 && !this.checkFilterKey ("NOOPT");
this.loadVibrations = !this.optOnly && this.desiredModelNumber < 0 && !this.checkFilterKey ("NOVIB");
if (this.checkFilterKey ("ALPHA")) this.filter = "alpha";
 else if (this.checkFilterKey ("BETA")) this.filter = "beta";
 else this.filter = this.getFilter ("SYM=");
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (!this.line.contains ("[")) return true;
this.line = this.line.toUpperCase ().trim ();
if (!this.line.startsWith ("[")) return true;
J.util.Logger.info (this.line);
if (this.line.indexOf ("[ATOMS]") == 0) {
this.readAtoms ();
this.modelAtomCount = this.atomSetCollection.getFirstAtomSetAtomCount ();
if (this.atomSetCollection.getAtomSetCount () == 1 && this.moData != null) this.finalizeMOData (this.moData);
return false;
}if (this.line.indexOf ("[GTO]") == 0) return this.readGaussianBasis ();
if (this.line.indexOf ("[MO]") == 0) return (!this.doReadMolecularOrbitals || this.readMolecularOrbitals ());
if (this.line.indexOf ("[FREQ]") == 0) return (!this.loadVibrations || this.readFreqsAndModes ());
if (this.line.indexOf ("[GEOCONV]") == 0) return (!this.loadGeometries || this.readGeometryOptimization ());
this.checkOrbitalType (this.line);
return true;
});
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
if (this.bsBadIndex.isEmpty ()) return;
try {
var ilast = 0;
var atoms = this.atomSetCollection.getAtoms ();
var nAtoms = this.atomSetCollection.getAtomCount ();
this.bsAtomOK.set (nAtoms);
var n = this.shells.size ();
for (var i = 0; i < n; i++) {
var iatom = this.shells.get (i)[0];
if (iatom != 2147483647) {
ilast = atoms[iatom].elementNumber;
continue;
}for (var j = this.bsAtomOK.nextClearBit (0); j >= 0; j = this.bsAtomOK.nextClearBit (j + 1)) {
if (atoms[j].elementNumber == ilast) {
this.shells.get (i)[0] = j;
J.util.Logger.info ("MoldenReader assigning shells starting with " + i + " for ** to atom " + (j + 1) + " z " + ilast);
for (; ++i < n && !this.bsBadIndex.get (i) && this.shells.get (i)[0] == 2147483647; ) this.shells.get (i)[0] = j;

i--;
this.bsAtomOK.set (j);
break;
}}
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("Molden reader could not assign shells -- abandoning MOs");
this.atomSetCollection.setAtomSetAuxiliaryInfo ("moData", null);
} else {
throw e;
}
}
});
$_M(c$, "readAtoms", 
($fz = function () {
var coordUnit = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line.$replace (']', ' '))[1];
var nPrevAtom = 0;
var nCurAtom = 0;
var isAU = (coordUnit.indexOf ("ANGS") < 0);
if (isAU && coordUnit.indexOf ("AU") < 0) {
throw  new Exception ("invalid coordinate unit " + coordUnit + " in [Atoms]");
}var f = (isAU ? 0.5291772 : 1);
while (this.readLine () != null && this.line.indexOf ('[') < 0) {
var tokens = this.getTokens ();
if (tokens.length < 6) continue;
var atom = this.atomSetCollection.addNewAtom ();
atom.atomName = tokens[0];
nCurAtom = this.parseIntStr (tokens[1]);
if (nPrevAtom > 0 && nCurAtom != nPrevAtom + 1) {
throw  new Exception ("out of order atom in [Atoms]");
}nPrevAtom = nCurAtom;
atom.elementNumber = this.parseIntStr (tokens[2]);
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[3]) * f, this.parseFloatStr (tokens[4]) * f, this.parseFloatStr (tokens[5]) * f);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readGaussianBasis", 
($fz = function () {
this.shells =  new J.util.JmolList ();
var gdata =  new J.util.JmolList ();
var atomIndex = 0;
var gaussianPtr = 0;
this.nCoef = 0;
this.nSPDF =  Clazz.newIntArray (12, 0);
this.discardLinesUntilNonBlank ();
while (this.line != null && !((this.line = this.line.trim ()).length == 0 || this.line.charAt (0) == '[')) {
var tokens = this.getTokens ();
atomIndex = this.parseIntStr (tokens[0]) - 1;
if (atomIndex == 2147483647) {
this.bsBadIndex.set (this.shells.size ());
} else {
this.bsAtomOK.set (atomIndex);
}while (this.readLine () != null && (this.line = this.line.trim ()).length > 0 && this.line.charAt (0) != '[') {
tokens = this.getTokens ();
var shellLabel = tokens[0].toUpperCase ();
var type = J.api.JmolAdapter.getQuantumShellTagID (shellLabel);
var nPrimitives = this.parseIntStr (tokens[1]);
var slater =  Clazz.newIntArray (4, 0);
this.nSPDF[type]++;
slater[0] = atomIndex;
slater[1] = type;
slater[2] = gaussianPtr;
slater[3] = nPrimitives;
var n = this.getDfCoefMaps ()[type].length;
System.out.println ("adding " + n + " coefficients type " + J.api.JmolAdapter.getQuantumShellTag (type) + " for atom " + atomIndex);
this.nCoef += n;
for (var ip = nPrimitives; --ip >= 0; ) {
var primTokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var nTokens = primTokens.length;
var orbData =  Clazz.newFloatArray (nTokens, 0);
for (var d = 0; d < nTokens; d++) orbData[d] = this.parseFloatStr (primTokens[d]);

gdata.addLast (orbData);
gaussianPtr++;
}
this.shells.addLast (slater);
}
if (this.line.length > 0 && this.line.charAt (0) == '[') break;
this.readLine ();
}
var garray = J.util.ArrayUtil.newFloat2 (gaussianPtr);
for (var i = 0; i < gaussianPtr; i++) {
garray[i] = gdata.get (i);
}
this.moData.put ("shells", this.shells);
this.moData.put ("gaussians", garray);
J.util.Logger.info (this.shells.size () + " slater shells read");
J.util.Logger.info (garray.length + " gaussian primitives read");
J.util.Logger.info (this.nCoef + " MO coefficients expected for orbital type " + this.orbitalType);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("moData", this.moData);
return false;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readMolecularOrbitals", 
($fz = function () {
while (this.checkOrbitalType (this.readLine ())) {
}
this.fixOrbitalType ();
var tokens = this.getMoTokens (this.line);
while (tokens != null && tokens.length > 0 && tokens[0].indexOf ('[') < 0) {
var mo =  new java.util.Hashtable ();
var data =  new J.util.JmolList ();
var energy = NaN;
var occupancy = NaN;
var symmetry = null;
var key;
while (this.parseIntStr (key = tokens[0]) == -2147483648) {
if (key.startsWith ("Ene")) {
energy = this.parseFloatStr (tokens[1]);
} else if (key.startsWith ("Occup")) {
occupancy = this.parseFloatStr (tokens[1]);
} else if (key.startsWith ("Sym")) {
symmetry = tokens[1];
} else if (key.startsWith ("Spin")) {
this.alphaBeta = tokens[1].toLowerCase ();
}tokens = this.getMoTokens (null);
}
while (tokens != null && tokens.length > 0 && this.parseIntStr (tokens[0]) != -2147483648) {
if (tokens.length != 2) throw  new Exception ("invalid MO coefficient specification");
data.addLast (tokens[1]);
tokens = this.getMoTokens (null);
}
var coefs =  Clazz.newFloatArray (data.size (), 0);
if (this.orbitalType.equals ("") && coefs.length < this.nCoef) {
J.util.Logger.info ("too few orbital coefficients for 6D");
this.checkOrbitalType ("[5D]");
}for (var i = data.size (); --i >= 0; ) coefs[i] = this.parseFloatStr (data.get (i));

var l = this.line;
this.line = "" + symmetry;
if (this.filterMO ()) {
mo.put ("coefficients", coefs);
if (Float.isNaN (energy)) {
this.haveEnergy = false;
} else {
mo.put ("energy", Float.$valueOf (energy));
}if (!Float.isNaN (occupancy)) mo.put ("occupancy", Float.$valueOf (occupancy));
if (symmetry != null) mo.put ("symmetry", symmetry);
if (this.alphaBeta.length > 0) mo.put ("type", this.alphaBeta);
this.setMO (mo);
if (J.util.Logger.debugging) {
J.util.Logger.debug (coefs.length + " coefficients in MO " + this.orbitals.size ());
}}this.line = l;
}
if (J.util.Logger.debugging) J.util.Logger.debug ("read " + this.orbitals.size () + " MOs");
this.setMOs ("eV");
if (this.haveEnergy && this.doSort) this.sortMOs ();
return false;
}, $fz.isPrivate = true, $fz));
$_M(c$, "sortMOs", 
($fz = function () {
var list = this.orbitals.toArray ();
java.util.Arrays.sort (list, Clazz.innerTypeInstance (J.adapter.readers.quantum.BasisFunctionReader.MOEnergySorter, this, null));
this.orbitals.clear ();
for (var i = 0; i < list.length; i++) this.orbitals.addLast (list[i]);

}, $fz.isPrivate = true, $fz));
$_M(c$, "getMoTokens", 
($fz = function (line) {
return (line == null && (line = this.readLine ()) == null ? null : J.adapter.smarter.AtomSetCollectionReader.getTokensStr (line.$replace ('=', ' ')));
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "checkOrbitalType", 
($fz = function (line) {
if (line.length > 3 && "5D 6D 7F 10 9G 15 11 21".indexOf (line.substring (1, 3)) >= 0) {
if (this.orbitalType.indexOf (line) >= 0) return true;
this.orbitalType += line;
J.util.Logger.info ("Orbital type set to " + this.orbitalType);
this.fixOrbitalType ();
return true;
}return false;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "fixOrbitalType", 
($fz = function () {
if (this.orbitalType.contains ("5D")) {
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_D_CARTESIAN, J.api.JmolAdapter.SHELL_D_SPHERICAL);
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_F_CARTESIAN, J.api.JmolAdapter.SHELL_F_SPHERICAL);
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_G_CARTESIAN, J.api.JmolAdapter.SHELL_G_SPHERICAL);
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_H_CARTESIAN, J.api.JmolAdapter.SHELL_H_SPHERICAL);
}if (this.orbitalType.contains ("10F")) {
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_F_SPHERICAL, J.api.JmolAdapter.SHELL_F_CARTESIAN);
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_G_SPHERICAL, J.api.JmolAdapter.SHELL_G_CARTESIAN);
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_H_SPHERICAL, J.api.JmolAdapter.SHELL_H_CARTESIAN);
}if (this.orbitalType.contains ("15G")) {
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_G_SPHERICAL, J.api.JmolAdapter.SHELL_G_CARTESIAN);
this.fixSlaterTypes (J.api.JmolAdapter.SHELL_H_SPHERICAL, J.api.JmolAdapter.SHELL_H_CARTESIAN);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "readFreqsAndModes", 
($fz = function () {
var tokens;
var frequencies =  new J.util.JmolList ();
while (this.readLine () != null && this.line.indexOf ('[') < 0) {
var f = this.getTokens ()[0];
frequencies.addLast (f);
}
var nFreqs = frequencies.size ();
this.skipTo ("[FR-COORD]");
if (!this.vibOnly) this.readAtomSet ("frequency base geometry", true, true);
this.skipTo ("[FR-NORM-COORD]");
var haveVib = false;
for (var nFreq = 0; nFreq < nFreqs; nFreq++) {
this.skipTo ("vibration");
this.doGetVibration (++this.vibrationNumber);
if (haveVib) this.atomSetCollection.cloneLastAtomSet ();
haveVib = true;
this.atomSetCollection.setAtomSetFrequency (null, null, "" + J.util.Parser.dVal (frequencies.get (nFreq)), null);
var i0 = this.atomSetCollection.getLastAtomSetAtomIndex ();
for (var i = 0; i < this.modelAtomCount; i++) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
this.atomSetCollection.addVibrationVector (i + i0, this.parseFloatStr (tokens[0]) * 0.5291772, this.parseFloatStr (tokens[1]) * 0.5291772, this.parseFloatStr (tokens[2]) * 0.5291772);
}
}
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readGeometryOptimization", 
($fz = function () {
var energies =  new J.util.JmolList ();
this.readLine ();
while (this.readLine () != null && this.line.indexOf ("force") < 0) energies.addLast ("" + J.util.Parser.dVal (this.line.trim ()));

this.skipTo ("[GEOMETRIES] XYZ");
var nGeom = energies.size ();
var firstModel = (this.optOnly || this.desiredModelNumber >= 0 ? 0 : 1);
this.modelNumber = firstModel;
var haveModel = false;
if (this.desiredModelNumber == 0 || this.desiredModelNumber == nGeom) this.desiredModelNumber = nGeom;
 else if (this.atomSetCollection.getAtomSetCount () > 0) this.finalizeMOData (null);
for (var i = 0; i < nGeom; i++) {
this.readLines (2);
if (this.doGetModel (++this.modelNumber, null)) {
this.readAtomSet ("Step " + (this.modelNumber - firstModel) + "/" + nGeom + ": " + energies.get (i), false, !this.optOnly || haveModel);
haveModel = true;
} else {
this.readLines (this.modelAtomCount);
}}
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "skipTo", 
($fz = function (key) {
key = key.toUpperCase ();
if (this.line == null || !this.line.toUpperCase ().contains (key)) while (this.readLine () != null && this.line.toUpperCase ().indexOf (key) < 0) {
}
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "readAtomSet", 
($fz = function (atomSetName, isBohr, asClone) {
if (asClone && this.desiredModelNumber < 0) this.atomSetCollection.cloneFirstAtomSet (0);
var f = (isBohr ? 0.5291772 : 1);
this.atomSetCollection.setAtomSetName (atomSetName);
if (this.atomSetCollection.getAtomCount () == 0) {
while (this.readLine () != null && this.line.indexOf ('[') < 0) {
var tokens = this.getTokens ();
if (tokens.length != 4) continue;
var atom = this.atomSetCollection.addNewAtom ();
atom.atomName = tokens[0];
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[1]) * f, this.parseFloatStr (tokens[2]) * f, this.parseFloatStr (tokens[3]) * f);
}
this.modelAtomCount = this.atomSetCollection.getLastAtomSetAtomCount ();
return;
}var atoms = this.atomSetCollection.getAtoms ();
var i0 = this.atomSetCollection.getLastAtomSetAtomIndex ();
for (var i = 0; i < this.modelAtomCount; i++) {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var atom = atoms[i + i0];
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[1]) * f, this.parseFloatStr (tokens[2]) * f, this.parseFloatStr (tokens[3]) * f);
}
}, $fz.isPrivate = true, $fz), "~S,~B,~B");
});
