Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.MOReader"], "J.adapter.readers.quantum.GenNBOReader", ["java.io.BufferedReader", "$.StringReader", "java.lang.Boolean", "$.Character", "$.Exception", "$.Float", "java.util.Hashtable", "J.api.JmolAdapter", "J.util.ArrayUtil", "$.JmolList", "$.Logger", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.isOutputFile = false;
this.moType = "";
this.nOrbitals0 = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "GenNBOReader", J.adapter.readers.quantum.MOReader);
$_M(c$, "initializeReader", 
function () {
var line1 = this.readLine ().trim ();
this.readLine ();
this.isOutputFile = (this.line.indexOf ("***") >= 0);
var isOK;
if (this.isOutputFile) {
isOK = this.readFile31 ();
Clazz.superCall (this, J.adapter.readers.quantum.GenNBOReader, "initializeReader", []);
this.moData.put ("isNormalized", Boolean.TRUE);
} else if (this.line.indexOf ("s in the AO basis:") >= 0) {
this.moType = this.line.substring (1, this.line.indexOf ("s"));
this.atomSetCollection.setCollectionName (line1 + ": " + this.moType + "s");
isOK = this.readFile31 ();
} else {
this.moType = "AO";
this.atomSetCollection.setCollectionName (line1 + ": " + this.moType + "s");
isOK = this.readData31 (line1, this.line);
}if (!isOK) J.util.Logger.error ("Unimplemented shell type -- no orbitals avaliable: " + this.line);
if (this.isOutputFile) return;
if (isOK) {
this.readMOs ();
}this.continuing = false;
});
$_M(c$, "readMOs", 
($fz = function () {
this.nOrbitals0 = this.orbitals.size ();
this.readFile46 ();
this.readOrbitalData (!this.moType.equals ("AO"));
this.setMOData (false);
this.moData.put ("isNormalized", Boolean.TRUE);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.indexOf ("SECOND ORDER PERTURBATION THEORY ANALYSIS") >= 0 && !this.orbitalsRead) {
this.moType = "NBO";
var data = this.getFileData (".37");
if (data == null) {
this.moType = "PNBO";
data = this.getFileData (".36");
if (data == null) return true;
}var readerSave = this.reader;
this.reader =  new java.io.BufferedReader ( new java.io.StringReader (data));
this.readLine ();
this.readLine ();
this.readMOs ();
this.reader = readerSave;
this.orbitalsRead = false;
return true;
}return this.checkNboLine ();
});
$_M(c$, "getFileData", 
($fz = function (ext) {
var fileName = this.htParams.get ("fullPathName");
var pt = fileName.lastIndexOf (".");
if (pt < 0) pt = fileName.length;
fileName = fileName.substring (0, pt) + ext;
var data = this.viewer.getFileAsString (fileName);
if (data.length == 0 || data.indexOf ("java.io.FileNotFound") >= 0) throw  new Exception (" supplemental file " + fileName + " was not found");
return data;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "readFile31", 
($fz = function () {
var data = this.getFileData (".31");
var readerSave = this.reader;
this.reader =  new java.io.BufferedReader ( new java.io.StringReader (data));
if (!this.readData31 (null, null)) return false;
this.reader = readerSave;
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readFile46", 
($fz = function () {
var data = this.getFileData (".46");
var readerSave = this.reader;
this.reader =  new java.io.BufferedReader ( new java.io.StringReader (data));
this.readData46 ();
this.reader = readerSave;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readData31", 
($fz = function (line1, line2) {
if (line1 == null) line1 = this.readLine ();
if (line2 == null) line2 = this.readLine ();
this.readLine ();
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var atomCount = this.parseIntStr (tokens[0]);
this.shellCount = this.parseIntStr (tokens[1]);
this.gaussianCount = this.parseIntStr (tokens[2]);
this.readLine ();
this.atomSetCollection.newAtomSet ();
this.atomSetCollection.setAtomSetName (this.moType + "s: " + line1.trim ());
for (var i = 0; i < atomCount; i++) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var z = this.parseIntStr (tokens[0]);
if (z < 0) continue;
var atom = this.atomSetCollection.addNewAtom ();
atom.elementNumber = z;
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[1]), this.parseFloatStr (tokens[2]), this.parseFloatStr (tokens[3]));
}
this.shells =  new J.util.JmolList ();
this.gaussians = J.util.ArrayUtil.newFloat2 (this.gaussianCount);
for (var i = 0; i < this.gaussianCount; i++) this.gaussians[i] =  Clazz.newFloatArray (6, 0);

this.readLine ();
this.nOrbitals = 0;
for (var i = 0; i < this.shellCount; i++) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var slater =  Clazz.newIntArray (4, 0);
slater[0] = this.parseIntStr (tokens[0]) - 1;
var n = this.parseIntStr (tokens[1]);
this.nOrbitals += n;
this.line = this.readLine ().trim ();
switch (n) {
case 1:
slater[1] = J.api.JmolAdapter.SHELL_S;
break;
case 3:
if (!this.getDFMap (this.line, J.api.JmolAdapter.SHELL_P, J.adapter.readers.quantum.GenNBOReader.$P_LIST, 3)) return false;
slater[1] = J.api.JmolAdapter.SHELL_P;
break;
case 4:
if (!this.getDFMap (this.line, J.api.JmolAdapter.SHELL_SP, J.adapter.readers.quantum.GenNBOReader.SP_LIST, 1)) return false;
slater[1] = J.api.JmolAdapter.SHELL_SP;
break;
case 5:
if (!this.getDFMap (this.line, J.api.JmolAdapter.SHELL_D_SPHERICAL, J.adapter.readers.quantum.GenNBOReader.$DS_LIST, 3)) return false;
slater[1] = J.api.JmolAdapter.SHELL_D_SPHERICAL;
break;
case 6:
if (!this.getDFMap (this.line, J.api.JmolAdapter.SHELL_D_CARTESIAN, J.adapter.readers.quantum.GenNBOReader.$DC_LIST, 3)) return false;
slater[1] = J.api.JmolAdapter.SHELL_D_CARTESIAN;
break;
case 7:
if (!this.getDFMap (this.line, J.api.JmolAdapter.SHELL_F_SPHERICAL, J.adapter.readers.quantum.GenNBOReader.$FS_LIST, 3)) return false;
slater[1] = J.api.JmolAdapter.SHELL_F_SPHERICAL;
break;
case 10:
if (!this.getDFMap (this.line, J.api.JmolAdapter.SHELL_F_CARTESIAN, J.adapter.readers.quantum.GenNBOReader.$FC_LIST, 3)) return false;
slater[1] = J.api.JmolAdapter.SHELL_F_CARTESIAN;
break;
}
slater[2] = this.parseIntStr (tokens[2]) - 1;
slater[3] = this.parseIntStr (tokens[3]);
this.shells.addLast (slater);
}
for (var j = 0; j < 5; j++) {
this.readLine ();
var temp = this.fillFloatArray (null, 0,  Clazz.newFloatArray (this.gaussianCount, 0));
for (var i = 0; i < this.gaussianCount; i++) {
this.gaussians[i][j] = temp[i];
if (j > 1) this.gaussians[i][5] += temp[i];
}
}
for (var i = 0; i < this.gaussianCount; i++) {
if (this.gaussians[i][1] == 0) this.gaussians[i][1] = this.gaussians[i][5];
}
if (J.util.Logger.debugging) {
J.util.Logger.debug (this.shells.size () + " slater shells read");
J.util.Logger.debug (this.gaussians.length + " gaussian primitives read");
}return true;
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "readData46", 
($fz = function () {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var ipt = 1;
if (tokens[1].equals ("ALPHA")) {
ipt = 2;
if (this.haveNboOrbitals) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.discardLinesUntilContains ("BETA"));
this.alphaBeta = "beta";
} else {
this.alphaBeta = "alpha";
this.haveNboOrbitals = true;
}}if (this.parseIntStr (tokens[ipt]) != this.nOrbitals) {
J.util.Logger.error ("file 46 number of orbitals does not match nOrbitals: " + this.nOrbitals);
return false;
}var ntype = null;
if (this.moType.equals ("AO")) ntype = "AO";
 else if (this.moType.indexOf ("NHO") >= 0) ntype = "NHO";
 else if (this.moType.indexOf ("NBO") >= 0) ntype = "NBO";
 else if (this.moType.indexOf ("NAO") >= 0) ntype = "NAO";
 else if (this.moType.indexOf ("MO") >= 0) ntype = "MO";
if (ntype == null) {
J.util.Logger.error ("uninterpretable type " + this.moType);
return false;
}if (!ntype.equals ("AO")) this.discardLinesUntilContains (ntype.equals ("MO") ? "NBO" : ntype);
var sb =  new J.util.SB ();
while (this.readLine () != null && this.line.indexOf ("O    ") < 0 && this.line.indexOf ("ALPHA") < 0 && this.line.indexOf ("BETA") < 0) sb.append (this.line);

sb.appendC (' ');
var data = sb.toString ();
var n = data.length - 1;
sb =  new J.util.SB ();
for (var i = 0; i < n; i++) {
var c = data.charAt (i);
switch (c) {
case '(':
case '-':
if (data.charAt (i + 1) == ' ') i++;
break;
case ' ':
if (Character.isDigit (data.charAt (i + 1)) || data.charAt (i + 1) == '(') continue;
break;
}
sb.appendC (c);
}
J.util.Logger.info (sb.toString ());
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (sb.toString ());
for (var i = 0; i < tokens.length; i++) {
var mo =  new java.util.Hashtable ();
this.setMO (mo);
}
if (ntype.equals ("MO")) return true;
for (var i = 0; i < tokens.length; i++) {
var mo = this.orbitals.get (i + this.nOrbitals0);
var type = tokens[i];
mo.put ("type", this.moType + " " + type);
mo.put ("occupancy", Float.$valueOf (type.indexOf ("*") >= 0 ? 0 : 2));
}
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readOrbitalData", 
($fz = function (isMO) {
var nAOs = this.nOrbitals;
this.nOrbitals = this.orbitals.size ();
this.line = null;
for (var i = this.nOrbitals0; i < this.nOrbitals; i++) {
var mo = this.orbitals.get (i);
var coefs =  Clazz.newFloatArray (nAOs, 0);
mo.put ("coefficients", coefs);
if (isMO) {
if (this.line == null) {
while (this.readLine () != null && Float.isNaN (this.parseFloatStr (this.line))) {
}
} else {
this.line = null;
}this.fillFloatArray (this.line, 0, coefs);
this.line = null;
} else {
coefs[i] = 1;
}}
if (this.moType.equals ("NBO")) {
var occupancies =  Clazz.newFloatArray (this.nOrbitals - this.nOrbitals0, 0);
this.fillFloatArray (null, 0, occupancies);
for (var i = this.nOrbitals0; i < this.nOrbitals; i++) {
var mo = this.orbitals.get (i);
mo.put ("occupancy", Float.$valueOf (Clazz.floatToInt (occupancies[i - this.nOrbitals0] + 0.2)));
}
}}, $fz.isPrivate = true, $fz), "~B");
Clazz.defineStatics (c$,
"$P_LIST", "101   102   103",
"SP_LIST", "1     101   102   103",
"$DS_LIST", "255   252   253   254   251",
"$DC_LIST", "201   204   206   202   203   205",
"$FS_LIST", "351   352   353   354   355   356   357",
"$FC_LIST", "301   307   310   304   302   303   306   309   308   305");
});
