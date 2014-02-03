Clazz.declarePackage ("J.adapter.readers.molxyz");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.molxyz.MolReader", ["java.lang.Exception", "$.Float", "J.adapter.smarter.Bond", "J.api.JmolAdapter", "J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.is2D = false;
this.isV3000 = false;
this.dimension = null;
this.allow2D = true;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.molxyz, "MolReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.is2D = this.checkFilterKey ("2D");
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
var isMDL = (this.line.startsWith ("$MDL"));
if (isMDL) {
this.discardLinesUntilStartsWith ("$HDR");
this.readLine ();
if (this.line == null) {
J.util.Logger.warn ("$HDR not found in MDL RG file");
this.continuing = false;
return false;
}}if (this.doGetModel (++this.modelNumber, null)) {
this.processMolSdHeader ();
this.processCtab (isMDL);
if (this.isLastModel (this.modelNumber)) {
this.continuing = false;
return false;
}return true;
}this.discardLinesUntilStartsWith ("$$$$");
return true;
});
$_M(c$, "readUserData", 
($fz = function (atom0) {
if (this.isV3000) return;
while (this.readLine () != null && this.line.indexOf ("$$$$") != 0) {
if (this.line.toUpperCase ().contains ("_PARTIAL_CHARGES")) {
try {
var atoms = this.atomSetCollection.getAtoms ();
for (var i = this.parseIntStr (this.readLine ()); --i >= 0; ) {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var atomIndex = this.parseIntStr (tokens[0]) + atom0 - 1;
var partialCharge = this.parseFloatStr (tokens[1]);
if (!Float.isNaN (partialCharge)) atoms[atomIndex].partialCharge = partialCharge;
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return;
} else {
throw e;
}
}
}}
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
this.finalizeReaderMR ();
});
$_M(c$, "finalizeReaderMR", 
function () {
if (this.is2D) this.set2D ();
this.isTrajectory = false;
this.finalizeReaderASCR ();
});
$_M(c$, "processMolSdHeader", 
function () {
var header = "";
var thisDataSetName = this.line;
header += this.line + "\n";
this.atomSetCollection.setCollectionName (this.line);
this.readLine ();
if (this.line == null) return;
header += this.line + "\n";
this.dimension = (this.line.length < 22 ? "3D" : this.line.substring (20, 22));
if (!this.allow2D && this.dimension.equals ("2D")) throw  new Exception ("File is 2D, not 3D");
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("dimension", this.dimension);
this.readLine ();
if (this.line == null) return;
header += this.line + "\n";
J.util.Logger.info (header);
this.checkCurrentLineForScript ();
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("fileHeader", header);
this.newAtomSet (thisDataSetName);
});
$_M(c$, "processCtab", 
function (isMDL) {
var tokens = null;
if (isMDL) this.discardLinesUntilStartsWith ("$CTAB");
this.isV3000 = (this.readLine () != null && this.line.indexOf ("V3000") >= 0);
if (this.isV3000) {
this.is2D = (this.dimension.equals ("2D"));
this.discardLinesUntilContains ("COUNTS");
tokens = this.getTokens ();
}if (this.line == null) return;
var atomCount = (this.isV3000 ? this.parseIntStr (tokens[3]) : this.parseIntRange (this.line, 0, 3));
var bondCount = (this.isV3000 ? this.parseIntStr (tokens[4]) : this.parseIntRange (this.line, 3, 6));
var atom0 = this.atomSetCollection.getAtomCount ();
this.readAtoms (atomCount);
this.readBonds (atom0, bondCount);
this.readUserData (atom0);
this.applySymmetryAndSetTrajectory ();
}, "~B");
$_M(c$, "readAtoms", 
function (atomCount) {
if (this.isV3000) this.discardLinesUntilContains ("BEGIN ATOM");
for (var i = 0; i < atomCount; ++i) {
this.readLine ();
var elementSymbol;
var x;
var y;
var z;
var charge = 0;
var isotope = 0;
if (this.isV3000) {
this.checkLineContinuation ();
var tokens = this.getTokens ();
elementSymbol = tokens[3];
x = this.parseFloatStr (tokens[4]);
y = this.parseFloatStr (tokens[5]);
z = this.parseFloatStr (tokens[6]);
for (var j = 7; j < tokens.length; j++) {
var s = tokens[j].toUpperCase ();
if (s.startsWith ("CHG=")) charge = this.parseIntStr (tokens[j].substring (4));
 else if (s.startsWith ("MASS=")) isotope = this.parseIntStr (tokens[j].substring (5));
}
if (isotope > 1 && elementSymbol.equals ("H")) isotope = 1 - isotope;
} else {
if (this.line.length > 34) {
elementSymbol = this.line.substring (31, 34).trim ();
} else {
elementSymbol = this.line.substring (31).trim ();
}x = this.parseFloatRange (this.line, 0, 10);
y = this.parseFloatRange (this.line, 10, 20);
z = this.parseFloatRange (this.line, 20, 30);
if (this.line.length >= 39) {
var code = this.parseIntRange (this.line, 36, 39);
if (code >= 1 && code <= 7) charge = 4 - code;
code = this.parseIntRange (this.line, 34, 36);
if (code != 0 && code >= -3 && code <= 4) {
isotope = J.api.JmolAdapter.getNaturalIsotope (J.api.JmolAdapter.getElementNumber (elementSymbol));
switch (isotope) {
case 0:
break;
case 1:
isotope = -code;
break;
default:
isotope += code;
}
}}}switch (isotope) {
case 0:
break;
case -1:
elementSymbol = "D";
break;
case -2:
elementSymbol = "T";
break;
default:
elementSymbol = isotope + elementSymbol;
}
if (this.is2D && z != 0) this.is2D = false;
var atom = this.atomSetCollection.addNewAtom ();
atom.elementSymbol = elementSymbol;
atom.formalCharge = charge;
this.setAtomCoordXYZ (atom, x, y, z);
}
}, "~N");
$_M(c$, "checkLineContinuation", 
($fz = function () {
while (this.line.endsWith ("-")) {
var s = this.line;
this.readLine ();
this.line = s + this.line;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readBonds", 
function (atom0, bondCount) {
if (this.isV3000) this.discardLinesUntilContains ("BEGIN BOND");
for (var i = 0; i < bondCount; ++i) {
this.readLine ();
var atomIndex1;
var atomIndex2;
var order;
var stereo = 0;
if (this.isV3000) {
this.checkLineContinuation ();
var tokens = this.getTokens ();
order = this.parseIntStr (tokens[3]);
atomIndex1 = this.parseIntStr (tokens[4]);
atomIndex2 = this.parseIntStr (tokens[5]);
for (var j = 6; j < tokens.length; j++) {
var s = tokens[j].toUpperCase ();
if (s.startsWith ("CFG=")) {
stereo = this.parseIntStr (tokens[j].substring (4));
break;
}}
} else {
atomIndex1 = this.parseIntRange (this.line, 0, 3);
atomIndex2 = this.parseIntRange (this.line, 3, 6);
order = this.parseIntRange (this.line, 6, 9);
if (this.is2D && order == 1 && this.line.length >= 12) stereo = this.parseIntRange (this.line, 9, 12);
}switch (order) {
case 0:
case -10:
order = 1;
break;
case 1:
switch (stereo) {
case 1:
order = 1025;
break;
case 3:
case 6:
order = 1041;
break;
}
break;
case 2:
case 3:
break;
case 4:
order = 515;
break;
case 5:
order = 66;
break;
case 6:
order = 513;
break;
case 7:
order = 514;
break;
case 8:
order = 33;
break;
}
this.atomSetCollection.addBond ( new J.adapter.smarter.Bond (atom0 + atomIndex1 - 1, atom0 + atomIndex2 - 1, order));
}
}, "~N,~N");
});
