Clazz.declarePackage ("J.api");
Clazz.load (["J.c.QS"], "J.api.JmolAdapter", ["java.util.Hashtable", "J.api.JmolViewer", "JU.Elements", "JV.JC"], function () {
c$ = Clazz.declareType (J.api, "JmolAdapter");
c$.getShellEnumeration = Clazz.defineMethod (c$, "getShellEnumeration", 
function (i) {
return J.c.QS.getItem (i);
}, "~N");
c$.getNewDfCoefMap = Clazz.defineMethod (c$, "getNewDfCoefMap", 
function () {
return J.c.QS.getNewDfCoefMap ();
});
c$.getElementSymbol = Clazz.defineMethod (c$, "getElementSymbol", 
function (elementNumber) {
return JU.Elements.elementSymbolFromNumber (elementNumber);
}, "~N");
c$.getElementNumber = Clazz.defineMethod (c$, "getElementNumber", 
function (elementSymbol) {
return JU.Elements.elementNumberFromSymbol (elementSymbol, false);
}, "~S");
c$.getNaturalIsotope = Clazz.defineMethod (c$, "getNaturalIsotope", 
function (elementNumber) {
return JU.Elements.getNaturalIsotope (elementNumber);
}, "~N");
c$.isHetero = Clazz.defineMethod (c$, "isHetero", 
function (group3) {
return JV.JC.isHetero (group3);
}, "~S");
c$.getQuantumShellTag = Clazz.defineMethod (c$, "getQuantumShellTag", 
function (id) {
return J.c.QS.getQuantumShellTag (id);
}, "~N");
c$.getQuantumShellTagID = Clazz.defineMethod (c$, "getQuantumShellTagID", 
function (tag) {
return J.c.QS.getQuantumShellTagID (tag);
}, "~S");
c$.getQuantumShellTagIDSpherical = Clazz.defineMethod (c$, "getQuantumShellTagIDSpherical", 
function (tag) {
return J.c.QS.getQuantumShellTagIDSpherical (tag);
}, "~S");
c$.getBondingRadius = Clazz.defineMethod (c$, "getBondingRadius", 
function (atomicNumberWithIsotope, charge) {
return JU.Elements.getBondingRadius (atomicNumberWithIsotope, charge);
}, "~N,~N");
Clazz.defineMethod (c$, "getAtomSetCollectionFromReader", 
function (name, type, bufferedReader, htParams) {
if (htParams == null) htParams =  new java.util.Hashtable ();
if (!htParams.containsKey ("vwr")) htParams.put ("vwr", J.api.JmolViewer.allocateViewer (null, this));
var a = this.getAtomSetCollectionReader (name, type, bufferedReader, htParams);
if (Clazz.instanceOf (a, String)) return a;
return this.getAtomSetCollection (a);
}, "~S,~S,~O,java.util.Map");
Clazz.defineMethod (c$, "openBufferedReader", 
function (name, bufferedReader) {
return this.getAtomSetCollectionFromReader (name, null, bufferedReader, null);
}, "~S,java.io.BufferedReader");
Clazz.defineMethod (c$, "openBufferedReader", 
function (name, bufferedReader, htParams) {
return this.getAtomSetCollectionFromReader (name, null, bufferedReader, htParams);
}, "~S,java.io.BufferedReader,java.util.Map");
Clazz.defineMethod (c$, "openBufferedReader", 
function (name, type, bufferedReader) {
return this.getAtomSetCollectionFromReader (name, type, bufferedReader, null);
}, "~S,~S,java.io.BufferedReader");
c$.canonizeAlphaDigit = Clazz.defineMethod (c$, "canonizeAlphaDigit", 
 function (ch) {
return ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') ? ch : '\0');
}, "~S");
c$.canonizeInsertionCode = Clazz.defineMethod (c$, "canonizeInsertionCode", 
function (insertionCode) {
return J.api.JmolAdapter.canonizeAlphaDigit (insertionCode);
}, "~S");
c$.canonizeAlternateLocationID = Clazz.defineMethod (c$, "canonizeAlternateLocationID", 
function (altLoc) {
return J.api.JmolAdapter.canonizeAlphaDigit (altLoc);
}, "~S");
Clazz.defineStatics (c$,
"ORDER_COVALENT_SINGLE", 1,
"ORDER_COVALENT_DOUBLE", 2,
"ORDER_COVALENT_TRIPLE", 3,
"ORDER_AROMATIC", 515,
"ORDER_AROMATIC_SINGLE", 513,
"ORDER_AROMATIC_DOUBLE", 514,
"ORDER_HBOND", 2048,
"ORDER_STEREO_NEAR", 1025,
"ORDER_STEREO_FAR", 1041,
"ORDER_PARTIAL01", 33,
"ORDER_PARTIAL12", 66,
"ORDER_PARTIAL23", 97,
"ORDER_PARTIAL32", 100,
"ORDER_UNSPECIFIED", 17,
"ORDER_PYMOL_SINGLE", 65536,
"ORDER_PYMOL_MULT", 98304);
c$.SHELL_S = c$.prototype.SHELL_S = J.c.QS.S.id;
c$.SHELL_P = c$.prototype.SHELL_P = J.c.QS.P.id;
c$.SHELL_SP = c$.prototype.SHELL_SP = J.c.QS.SP.id;
c$.SHELL_L = c$.prototype.SHELL_L = J.c.QS.SP.id;
c$.SHELL_D_SPHERICAL = c$.prototype.SHELL_D_SPHERICAL = J.c.QS.D_SPHERICAL.id;
c$.SHELL_D_CARTESIAN = c$.prototype.SHELL_D_CARTESIAN = J.c.QS.D_CARTESIAN.id;
c$.SHELL_F_SPHERICAL = c$.prototype.SHELL_F_SPHERICAL = J.c.QS.F_SPHERICAL.id;
c$.SHELL_F_CARTESIAN = c$.prototype.SHELL_F_CARTESIAN = J.c.QS.F_CARTESIAN.id;
c$.SHELL_G_SPHERICAL = c$.prototype.SHELL_G_SPHERICAL = J.c.QS.G_SPHERICAL.id;
c$.SHELL_G_CARTESIAN = c$.prototype.SHELL_G_CARTESIAN = J.c.QS.G_CARTESIAN.id;
c$.SHELL_H_SPHERICAL = c$.prototype.SHELL_H_SPHERICAL = J.c.QS.H_SPHERICAL.id;
c$.SHELL_H_CARTESIAN = c$.prototype.SHELL_H_CARTESIAN = J.c.QS.H_CARTESIAN.id;
c$.SUPPORTED_BASIS_FUNCTIONS = c$.prototype.SUPPORTED_BASIS_FUNCTIONS = "SPLDF";
c$.NOTE_SCRIPT_FILE = c$.prototype.NOTE_SCRIPT_FILE = "NOTE: file recognized as a script file: ";
Clazz.defineStatics (c$,
"cellParamNames", ["_cell_length_a", "_cell_length_b", "_cell_length_c", "_cell_angle_alpha", "_cell_angle_beta", "_cell_angle_gamma"]);
});
