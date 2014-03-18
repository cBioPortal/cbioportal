Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.MOReader", "J.util.BS"], "J.adapter.readers.quantum.GaussianReader", ["java.lang.Character", "$.Exception", "$.Float", "java.util.Hashtable", "J.adapter.smarter.SmarterJmolAdapter", "J.api.JmolAdapter", "J.util.ArrayUtil", "$.Escape", "$.JmolList", "$.Logger", "$.Parser", "$.TextFormat", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.energyString = "";
this.energyKey = "";
this.calculationNumber = 1;
this.scanPoint = -1;
this.equivalentAtomSets = 0;
this.stepNumber = 0;
this.moModelSet = -1;
this.namedSets = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "GaussianReader", J.adapter.readers.quantum.MOReader);
Clazz.prepareFields (c$, function () {
this.namedSets =  new J.util.BS ();
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.startsWith (" Step number")) {
this.equivalentAtomSets = 0;
this.stepNumber++;
var scanPointIndex = this.line.indexOf ("scan point");
if (scanPointIndex > 0) {
this.scanPoint = this.parseIntAt (this.line, scanPointIndex + 10);
} else {
this.scanPoint = -1;
}return true;
}if (this.line.indexOf ("-- Stationary point found") > 0) {
if (this.scanPoint >= 0) this.scanPoint++;
return true;
}if (this.line.indexOf ("Input orientation:") >= 0 || this.line.indexOf ("Z-Matrix orientation:") >= 0 || this.line.indexOf ("Standard orientation:") >= 0) {
if (!this.doGetModel (++this.modelNumber, null)) return this.checkLastModel ();
this.equivalentAtomSets++;
J.util.Logger.info (this.atomSetCollection.getAtomSetCount () + " model " + this.modelNumber + " step " + this.stepNumber + " equivalentAtomSet " + this.equivalentAtomSets + " calculation " + this.calculationNumber + " scan point " + this.scanPoint + this.line);
this.readAtoms ();
return false;
}if (!this.doProcessLines) return true;
if (this.line.startsWith (" Energy=")) {
this.setEnergy ();
return true;
}if (this.line.startsWith (" SCF Done:")) {
this.readSCFDone ();
return true;
}if (this.line.startsWith (" Harmonic frequencies")) {
this.readFrequencies ();
return true;
}if (this.line.startsWith (" Total atomic charges:") || this.line.startsWith (" Mulliken atomic charges:")) {
this.readPartialCharges ();
return true;
}if (this.line.startsWith (" Dipole moment")) {
this.readDipoleMoment ();
return true;
}if (this.line.startsWith (" Standard basis:") || this.line.startsWith (" General basis read from")) {
this.energyUnits = "";
this.calculationType = this.line.substring (this.line.indexOf (":") + 1).trim ();
return true;
}if (this.line.startsWith (" AO basis set")) {
this.readBasis ();
return true;
}if (this.line.indexOf ("Molecular Orbital Coefficients") >= 0 || this.line.indexOf ("Natural Orbital Coefficients") >= 0 || this.line.indexOf ("Natural Transition Orbitals") >= 0) {
if (!this.filterMO ()) return true;
this.readMolecularOrbitals ();
J.util.Logger.info (this.orbitals.size () + " molecular orbitals read");
return true;
}if (this.line.startsWith (" Normal termination of Gaussian")) {
++this.calculationNumber;
this.equivalentAtomSets = 0;
return true;
}return this.checkNboLine ();
});
$_M(c$, "readSCFDone", 
($fz = function () {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensAt (this.line, 11);
if (tokens.length < 4) return;
this.energyKey = tokens[0];
this.atomSetCollection.setAtomSetEnergy (tokens[2], this.parseFloatStr (tokens[2]));
this.energyString = tokens[2] + " " + tokens[3];
this.atomSetCollection.setAtomSetNames (this.energyKey + " = " + this.energyString, this.equivalentAtomSets, this.namedSets);
this.atomSetCollection.setAtomSetPropertyForSets (this.energyKey, this.energyString, this.equivalentAtomSets);
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
if (tokens.length > 2) {
this.atomSetCollection.setAtomSetPropertyForSets (tokens[0], tokens[2], this.equivalentAtomSets);
if (tokens.length > 5) this.atomSetCollection.setAtomSetPropertyForSets (tokens[3], tokens[5], this.equivalentAtomSets);
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
}if (tokens.length > 2) this.atomSetCollection.setAtomSetPropertyForSets (tokens[0], tokens[2], this.equivalentAtomSets);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setEnergy", 
($fz = function () {
var tokens = this.getTokens ();
this.energyKey = "Energy";
this.energyString = tokens[1];
this.atomSetCollection.setAtomSetNames ("Energy = " + tokens[1], this.equivalentAtomSets, this.namedSets);
this.atomSetCollection.setAtomSetEnergy (this.energyString, this.parseFloatStr (this.energyString));
}, $fz.isPrivate = true, $fz));
$_M(c$, "readAtoms", 
($fz = function () {
this.atomSetCollection.newAtomSet ();
if (this.energyKey.length != 0) this.atomSetCollection.setAtomSetName (this.energyKey + " = " + this.energyString);
this.atomSetCollection.setAtomSetEnergy (this.energyString, this.parseFloatStr (this.energyString));
var path = this.getTokens ()[0];
this.readLines (4);
var tokens;
while (this.readLine () != null && !this.line.startsWith (" --")) {
tokens = this.getTokens ();
var atom = this.atomSetCollection.addNewAtom ();
atom.elementNumber = this.parseIntStr (tokens[1]);
if (atom.elementNumber < 0) atom.elementNumber = 0;
var offset = tokens.length - 3;
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[offset]), this.parseFloatStr (tokens[++offset]), this.parseFloatStr (tokens[++offset]));
}
this.atomSetCollection.setAtomSetModelProperty (".PATH", "Calculation " + this.calculationNumber + (this.scanPoint >= 0 ? (J.adapter.smarter.SmarterJmolAdapter.PATH_SEPARATOR + "Scan Point " + this.scanPoint) : "") + J.adapter.smarter.SmarterJmolAdapter.PATH_SEPARATOR + path);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readBasis", 
($fz = function () {
this.shells =  new J.util.JmolList ();
var gdata =  new J.util.JmolList ();
var atomCount = 0;
this.gaussianCount = 0;
this.shellCount = 0;
var lastAtom = "";
var tokens;
var doSphericalD = (this.calculationType != null && (this.calculationType.indexOf ("5D") > 0));
var doSphericalF = (this.calculationType != null && (this.calculationType.indexOf ("7F") > 0));
var isGeneral = (this.line.indexOf ("general basis input") >= 0);
if (isGeneral) {
while (this.readLine () != null && this.line.length > 0) {
this.shellCount++;
tokens = this.getTokens ();
atomCount++;
while (this.readLine ().indexOf ("****") < 0) {
var slater =  Clazz.newIntArray (4, 0);
slater[0] = atomCount - 1;
tokens = this.getTokens ();
var oType = tokens[0];
if (doSphericalF && oType.indexOf ("F") >= 0 || doSphericalD && oType.indexOf ("D") >= 0) slater[1] = J.api.JmolAdapter.getQuantumShellTagIDSpherical (oType);
 else slater[1] = J.api.JmolAdapter.getQuantumShellTagID (oType);
var nGaussians = this.parseIntStr (tokens[1]);
slater[2] = this.gaussianCount;
slater[3] = nGaussians;
if (J.util.Logger.debugging) J.util.Logger.debug ("Slater " + this.shells.size () + " " + J.util.Escape.eAI (slater));
this.shells.addLast (slater);
this.gaussianCount += nGaussians;
for (var i = 0; i < nGaussians; i++) {
this.readLine ();
this.line = J.util.TextFormat.simpleReplace (this.line, "D ", "D+");
tokens = this.getTokens ();
if (J.util.Logger.debugging) J.util.Logger.debug ("Gaussians " + (i + 1) + " " + J.util.Escape.eAS (tokens, true));
gdata.addLast (tokens);
}
}
}
} else {
while (this.readLine () != null && this.line.startsWith (" Atom")) {
this.shellCount++;
tokens = this.getTokens ();
var slater =  Clazz.newIntArray (4, 0);
if (!tokens[1].equals (lastAtom)) atomCount++;
lastAtom = tokens[1];
slater[0] = atomCount - 1;
var oType = tokens[4];
if (doSphericalF && oType.indexOf ("F") >= 0 || doSphericalD && oType.indexOf ("D") >= 0) slater[1] = J.api.JmolAdapter.getQuantumShellTagIDSpherical (oType);
 else slater[1] = J.api.JmolAdapter.getQuantumShellTagID (oType);
var nGaussians = this.parseIntStr (tokens[5]);
slater[2] = this.gaussianCount;
slater[3] = nGaussians;
this.shells.addLast (slater);
this.gaussianCount += nGaussians;
for (var i = 0; i < nGaussians; i++) {
gdata.addLast (J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ()));
}
}
}if (atomCount == 0) atomCount = 1;
this.gaussians = J.util.ArrayUtil.newFloat2 (this.gaussianCount);
for (var i = 0; i < this.gaussianCount; i++) {
tokens = gdata.get (i);
this.gaussians[i] =  Clazz.newFloatArray (tokens.length, 0);
for (var j = 0; j < tokens.length; j++) this.gaussians[i][j] = this.parseFloatStr (tokens[j]);

}
J.util.Logger.info (this.shellCount + " slater shells read");
J.util.Logger.info (this.gaussianCount + " gaussian primitives read");
}, $fz.isPrivate = true, $fz));
$_M(c$, "readMolecularOrbitals", 
($fz = function () {
if (this.shells == null) return;
var mos = J.util.ArrayUtil.createArrayOfHashtable (5);
var data = J.util.ArrayUtil.createArrayOfArrayList (5);
var nThisLine = 0;
var isNOtype = this.line.contains ("Natural Orbital");
while (this.readLine () != null && this.line.toUpperCase ().indexOf ("DENS") < 0) {
var tokens;
if (this.line.indexOf ("                    ") == 0) {
this.addMOData (nThisLine, data, mos);
if (isNOtype) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line);
nThisLine = tokens.length;
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
} else {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
nThisLine = tokens.length;
}for (var i = 0; i < nThisLine; i++) {
mos[i] =  new java.util.Hashtable ();
data[i] =  new J.util.JmolList ();
var sym;
if (isNOtype) {
mos[i].put ("occupancy", Float.$valueOf (J.util.Parser.parseFloatStr (tokens[i + 2])));
} else {
sym = tokens[i];
mos[i].put ("symmetry", sym);
if (sym.indexOf ("O") >= 0) mos[i].put ("occupancy", Float.$valueOf (2));
 else if (sym.indexOf ("V") >= 0) mos[i].put ("occupancy", Float.$valueOf (0));
}}
if (isNOtype) continue;
this.line = this.readLine ().substring (21);
tokens = this.getTokens ();
if (tokens.length != nThisLine) tokens = J.adapter.smarter.AtomSetCollectionReader.getStrings (this.line, nThisLine, 10);
for (var i = 0; i < nThisLine; i++) {
mos[i].put ("energy", Float.$valueOf (J.util.Parser.fVal (tokens[i])));
System.out.println (i + " gaussian energy " + mos[i].get ("energy"));
}
continue;
} else if (this.line.length < 21 || (this.line.charAt (5) != ' ' && !Character.isDigit (this.line.charAt (5)))) {
continue;
}try {
this.line = J.util.TextFormat.simpleReplace (this.line, " 0 ", "0  ");
tokens = this.getTokens ();
var type = tokens[tokens.length - nThisLine - 1].substring (1);
if (Character.isDigit (type.charAt (0))) type = type.substring (1);
if (!this.isQuantumBasisSupported (type.charAt (0)) && "XYZ".indexOf (type.charAt (0)) >= 0) type = (type.length == 2 ? "D" : "F") + type;
if (!this.isQuantumBasisSupported (type.charAt (0))) continue;
tokens = J.adapter.smarter.AtomSetCollectionReader.getStrings (this.line.substring (this.line.length - 10 * nThisLine), nThisLine, 10);
for (var i = 0; i < nThisLine; i++) data[i].addLast (tokens[i]);

} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("Error reading Gaussian file Molecular Orbitals at line: " + this.line);
break;
} else {
throw e;
}
}
}
this.addMOData (nThisLine, data, mos);
this.setMOData (this.moModelSet != this.atomSetCollection.getAtomSetCount ());
this.moModelSet = this.atomSetCollection.getAtomSetCount ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "readFrequencies", 
($fz = function () {
this.discardLinesUntilContains (":");
if (this.line == null) throw ( new Exception ("No frequencies encountered"));
while ((this.line = this.readLine ()) != null && this.line.length > 15) {
var symmetries = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var frequencies = J.adapter.smarter.AtomSetCollectionReader.getTokensAt (this.discardLinesUntilStartsWith (" Frequencies"), 15);
var red_masses = J.adapter.smarter.AtomSetCollectionReader.getTokensAt (this.discardLinesUntilStartsWith (" Red. masses"), 15);
var frc_consts = J.adapter.smarter.AtomSetCollectionReader.getTokensAt (this.discardLinesUntilStartsWith (" Frc consts"), 15);
var intensities = J.adapter.smarter.AtomSetCollectionReader.getTokensAt (this.discardLinesUntilStartsWith (" IR Inten"), 15);
var iAtom0 = this.atomSetCollection.getAtomCount ();
var atomCount = this.atomSetCollection.getLastAtomSetAtomCount ();
var frequencyCount = frequencies.length;
var ignore =  Clazz.newBooleanArray (frequencyCount, false);
for (var i = 0; i < frequencyCount; ++i) {
ignore[i] = !this.doGetVibration (++this.vibrationNumber);
if (ignore[i]) continue;
this.atomSetCollection.cloneLastAtomSet ();
var name = this.atomSetCollection.setAtomSetFrequency ("Calculation " + this.calculationNumber, symmetries[i], frequencies[i], null);
this.appendLoadNote ("model " + this.atomSetCollection.getAtomSetCount () + ": " + name);
this.namedSets.set (this.atomSetCollection.getCurrentAtomSetIndex ());
this.atomSetCollection.setAtomSetModelProperty ("ReducedMass", red_masses[i] + " AMU");
this.atomSetCollection.setAtomSetModelProperty ("ForceConstant", frc_consts[i] + " mDyne/A");
this.atomSetCollection.setAtomSetModelProperty ("IRIntensity", intensities[i] + " KM/Mole");
}
this.discardLinesUntilContains (" AN ");
this.fillFrequencyData (iAtom0, atomCount, atomCount, ignore, true, 0, 0, null, 0);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readDipoleMoment", 
function () {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
if (tokens.length != 8) return;
var dipole = J.util.V3.new3 (this.parseFloatStr (tokens[1]), this.parseFloatStr (tokens[3]), this.parseFloatStr (tokens[5]));
J.util.Logger.info ("Molecular dipole for model " + this.atomSetCollection.getAtomSetCount () + " = " + dipole);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("dipole", dipole);
});
$_M(c$, "readPartialCharges", 
function () {
this.readLine ();
var atomCount = this.atomSetCollection.getAtomCount ();
var i0 = this.atomSetCollection.getLastAtomSetAtomIndex ();
var atoms = this.atomSetCollection.getAtoms ();
for (var i = i0; i < atomCount; ++i) {
while (atoms[i].elementNumber == 0) ++i;

var charge = this.parseFloatStr (J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ())[2]);
atoms[i].partialCharge = charge;
}
J.util.Logger.info ("Mulliken charges found for Model " + this.atomSetCollection.getAtomSetCount ());
});
Clazz.defineStatics (c$,
"STD_ORIENTATION_ATOMIC_NUMBER_OFFSET", 1);
});
