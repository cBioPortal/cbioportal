Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.xtal.VaspOutcarReader", ["java.lang.Double", "J.util.JmolList", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atomNames = null;
this.elementNames = null;
this.atomCount = 0;
this.inputOnly = false;
this.mDsimulation = false;
this.isVersion5 = false;
this.unitCellData = null;
this.gibbsEnergy = null;
this.gibbsEntropy = null;
this.electronEne = null;
this.kinEne = null;
this.totEne = null;
this.temp = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "VaspOutcarReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.unitCellData =  Clazz.newFloatArray (18, 0);
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.setSpaceGroupName ("P1");
this.setFractionalCoordinates (true);
this.inputOnly = this.checkFilterKey ("INPUT");
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.contains (" vasp.5")) {
this.isVersion5 = true;
} else if (this.line.toUpperCase ().contains ("INCAR:")) {
this.readElementNames ();
} else if (this.line.contains ("ions per type")) {
this.readAtomCountAndSetNames ();
} else if (this.line.contains ("molecular dynamics for ions")) {
this.mDsimulation = true;
} else if (this.line.contains ("direct lattice vectors")) {
this.readUnitCellVectors ();
} else if (this.line.contains ("position of ions in fractional coordinates")) {
this.readInitialCoordinates ();
if (this.inputOnly) this.continuing = false;
} else if (this.line.contains ("POSITION")) {
this.readPOSITION ();
return true;
} else if (this.line.startsWith ("  FREE ENERGIE") && !this.mDsimulation) {
this.readEnergy ();
} else if (this.line.contains ("ENERGIE OF THE ELECTRON-ION-THERMOSTAT") && this.mDsimulation) {
this.readMdyn ();
} else if (this.line.startsWith (" Eigenvectors and eigenvalues of the dynamical matrix")) {
this.readFrequency ();
}return true;
});
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
this.setSymmetry ();
});
$_M(c$, "readElementNames", 
($fz = function () {
this.elementNames =  new J.util.JmolList ();
var elementList = "";
while (this.readLine () != null && this.line.indexOf ("VRHFIN") < 0) {
var pt = (this.line.contains ("_") ? 2 : 1);
if (pt == 2) this.line = this.line.$replace ('_', ' ');
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line.substring (this.line.indexOf (":") + 1));
var sym = tokens[pt];
var key = ";" + sym + ";";
if (elementList.indexOf (key) >= 0) continue;
elementList += key;
this.elementNames.addLast (sym);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readAtomCountAndSetNames", 
($fz = function () {
var numofElement =  Clazz.newIntArray (100, 0);
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line.substring (this.line.indexOf ("=") + 1));
this.atomCount = 0;
for (var i = 0; i < tokens.length; i++) this.atomCount += (numofElement[i] = this.parseIntStr (tokens[i].trim ()));

this.atomNames =  new Array (this.atomCount);
var nElements = this.elementNames.size ();
for (var pt = 0, i = 0; i < nElements; i++) for (var j = 0; j < numofElement[i]; j++) this.atomNames[pt++] = this.elementNames.get (i);


}, $fz.isPrivate = true, $fz));
$_M(c$, "readUnitCellVectors", 
($fz = function () {
if (this.atomSetCollection.getAtomCount () > 0) {
this.setSymmetry ();
this.atomSetCollection.newAtomSet ();
this.setAtomSetInfo ();
}this.fillFloatArray (null, 0, this.unitCellData);
this.setUnitCell ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setUnitCell", 
($fz = function () {
this.addPrimitiveLatticeVector (0, this.unitCellData, 0);
this.addPrimitiveLatticeVector (1, this.unitCellData, 6);
this.addPrimitiveLatticeVector (2, this.unitCellData, 12);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setSymmetry", 
($fz = function () {
this.applySymmetryAndSetTrajectory ();
this.setSpaceGroupName ("P1");
this.setFractionalCoordinates (false);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readInitialCoordinates", 
($fz = function () {
var counter = 0;
while (this.readLine () != null && this.line.length > 10) {
var atom = this.atomSetCollection.addNewAtom ();
var tokens = this.getTokens ();
atom.atomName = this.atomNames[counter++];
var x = this.parseFloatStr (tokens[0]);
var y = this.parseFloatStr (tokens[1]);
var z = this.parseFloatStr (tokens[2]);
this.setAtomCoordXYZ (atom, x, y, z);
}
this.atomSetCollection.setAtomSetName ("Initial Coordinates");
}, $fz.isPrivate = true, $fz));
$_M(c$, "readPOSITION", 
($fz = function () {
var counter = 0;
this.readLines (1);
while (this.readLine () != null && this.line.indexOf ("----------") < 0) {
var atom = this.atomSetCollection.addNewAtom ();
var tokens = this.getTokens ();
atom.atomName = this.atomNames[counter];
var x = this.parseFloatStr (tokens[0]);
var y = this.parseFloatStr (tokens[1]);
var z = this.parseFloatStr (tokens[2]);
this.setAtomCoordXYZ (atom, x, y, z);
counter++;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readEnergy", 
($fz = function () {
this.readLine ();
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
this.gibbsEnergy = Double.$valueOf (Double.parseDouble (tokens[4]));
this.readLine ();
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var enthalpy = Double.parseDouble (tokens[3]);
this.gibbsEntropy = Double.$valueOf (enthalpy - this.gibbsEnergy.doubleValue ());
}, $fz.isPrivate = true, $fz));
$_M(c$, "setAtomSetInfo", 
($fz = function () {
if (this.gibbsEnergy == null) return;
this.atomSetCollection.setAtomSetEnergy ("" + this.gibbsEnergy, this.gibbsEnergy.floatValue ());
this.atomSetCollection.setAtomSetAuxiliaryInfo ("Energy", this.gibbsEnergy);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("Entropy", this.gibbsEntropy);
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("Energy", this.gibbsEnergy);
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("Entropy", this.gibbsEntropy);
this.atomSetCollection.setAtomSetName ("G = " + this.gibbsEnergy + " eV, T*S = " + this.gibbsEntropy + " eV");
}, $fz.isPrivate = true, $fz));
$_M(c$, "readMdyn", 
($fz = function () {
var tokens = this.getTokens ();
this.readLine ();
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
this.electronEne = Double.$valueOf (Double.parseDouble (tokens[4]));
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
this.kinEne = Double.$valueOf (Double.parseDouble (tokens[4]));
this.temp = this.parseFloatStr (tokens[6]);
this.readLines (3);
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
this.totEne = Double.$valueOf (Double.parseDouble (tokens[4]));
this.setAtomSetInfoMd ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setAtomSetInfoMd", 
($fz = function () {
this.atomSetCollection.setAtomSetName ("Temp. = " + J.util.TextFormat.formatDecimal ((this.temp), 2) + " K, Energy = " + this.totEne + " eV");
this.atomSetCollection.setAtomSetAuxiliaryInfo ("Energy", this.totEne);
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("Energy", this.totEne);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("EleEnergy", this.kinEne);
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("EleEnergy", this.electronEne);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("Kinetic", this.electronEne);
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("Kinetic", this.kinEne);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("Temperature", J.util.TextFormat.formatDecimal ((this.temp), 2));
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("Temperature", J.util.TextFormat.formatDecimal ((this.temp), 2));
}, $fz.isPrivate = true, $fz));
$_M(c$, "readFrequency", 
($fz = function () {
var pt = this.atomSetCollection.getCurrentAtomSetIndex ();
this.atomSetCollection.setBaseSymmetryAtomCount (this.atomCount);
if (this.isVersion5) {
this.readLines (3);
} else {
this.discardLinesUntilContains ("Eigenvectors after division by SQRT(mass)");
this.readLines (5);
}var ignore =  Clazz.newBooleanArray (1, false);
while (this.readLine () != null && (this.line.contains ("f  = ") || this.line.contains ("f/i= "))) {
this.applySymmetryAndSetTrajectory ();
var iAtom0 = this.atomSetCollection.getAtomCount ();
this.cloneLastAtomSet (this.atomCount, null);
if (!ignore[0]) {
this.atomSetCollection.setCurrentAtomSetIndex (++pt);
this.atomSetCollection.setAtomSetFrequency (null, null, this.line.substring (this.line.indexOf ("2PiTHz") + 6, this.line.indexOf ("c") - 1).trim (), null);
}this.readLine ();
this.fillFrequencyData (iAtom0, this.atomCount, this.atomCount, ignore, true, 35, 12, null, 0);
this.readLine ();
}
}, $fz.isPrivate = true, $fz));
});
