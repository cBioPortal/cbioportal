Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.MopacSlaterReader"], "J.adapter.readers.quantum.WebMOReader", ["java.lang.Boolean", "$.Exception", "$.Float", "java.util.Hashtable", "J.adapter.smarter.Bond", "J.api.JmolAdapter", "J.util.ArrayUtil", "$.JmolList", "$.Logger"], function () {
c$ = Clazz.declareType (J.adapter.readers.quantum, "WebMOReader", J.adapter.readers.quantum.MopacSlaterReader);
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.equals ("[HEADER]")) {
this.readHeader ();
return true;
}if (this.line.equals ("[ATOMS]")) {
this.readAtoms ();
return false;
}if (this.line.equals ("[BONDS]")) {
this.readBonds ();
return false;
}if (!this.doReadMolecularOrbitals) return true;
if (this.line.equals ("[AO_ORDER]")) {
this.readAtomicOrbitalOrder ();
return false;
}if (this.line.equals ("[GTO]")) {
this.readGaussianBasis ();
return false;
}if (this.line.equals ("[STO]")) {
this.readSlaterBasis ();
return false;
}if (this.line.indexOf ("[MO") == 0) {
if (!this.doGetModel (++this.modelNumber, null)) return this.checkLastModel ();
this.readMolecularOrbital ();
return false;
}return true;
});
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
this.finalizeReaderASCR ();
if (this.nOrbitals > 0) this.setMOs ("eV");
if (J.util.Logger.debugging) J.util.Logger.debug (this.orbitals.size () + " molecular orbitals read");
});
$_M(c$, "readHeader", 
function () {
this.moData.put ("isNormalized", Boolean.TRUE);
while (this.readLine () != null && this.line.length > 0) {
this.moData.put ("calculationType", "?");
var tokens = this.getTokens ();
tokens[0] = tokens[0].substring (0, 1).toLowerCase () + tokens[0].substring (1, tokens[0].length);
var str = "";
for (var i = 1; i < tokens.length; i++) str += (i == 1 ? "" : " ") + tokens[i].toLowerCase ();

this.moData.put (tokens[0], str);
}
});
$_M(c$, "readAtoms", 
function () {
while (this.getLine ()) {
var tokens = this.getTokens ();
if (tokens.length == 0) continue;
var atNo = this.parseIntStr (this.line);
var atom = this.atomSetCollection.addNewAtom ();
if (atNo == -2147483648) atom.elementSymbol = tokens[0];
 else atom.elementSymbol = J.adapter.smarter.AtomSetCollectionReader.getElementSymbol (atNo);
this.setAtomCoordXYZ (atom, this.parseFloatStr (tokens[1]) * 0.5291772, this.parseFloatStr (tokens[2]) * 0.5291772, this.parseFloatStr (tokens[3]) * 0.5291772);
}
});
$_M(c$, "readBonds", 
function () {
while (this.getLine ()) {
var tokens = this.getTokens ();
if (tokens.length == 0) continue;
var atomIndex1 = this.parseIntStr (tokens[0]);
var atomIndex2 = this.parseIntStr (tokens[1]);
var order = this.parseIntStr (tokens[2]);
this.atomSetCollection.addBond ( new J.adapter.smarter.Bond (atomIndex1 - 1, atomIndex2 - 1, order));
}
});
$_M(c$, "readAtomicOrbitalOrder", 
function () {
while (this.getLine ()) {
var tokens = this.getTokens ();
if (tokens.length == 0) continue;
var data = this.line.substring (9).trim ().toLowerCase ();
var isOK = false;
switch (tokens.length - 1) {
case 3:
case 4:
isOK = true;
break;
case 5:
isOK = (tokens[0].equals ("DOrbitals") && this.getDFMap (data, J.api.JmolAdapter.SHELL_D_SPHERICAL, J.adapter.readers.quantum.WebMOReader.DS_LIST, 99));
break;
case 6:
isOK = (tokens[0].equals ("DOrbitals") && this.getDFMap (data, J.api.JmolAdapter.SHELL_D_CARTESIAN, J.adapter.readers.quantum.WebMOReader.DC_LIST, 2));
break;
case 7:
isOK = (tokens[0].equals ("FOrbitals") && this.getDFMap (data, J.api.JmolAdapter.SHELL_F_SPHERICAL, J.adapter.readers.quantum.WebMOReader.FS_LIST, 99));
break;
case 10:
isOK = (tokens[0].equals ("FOrbitals") && this.getDFMap (data, J.api.JmolAdapter.SHELL_F_CARTESIAN, J.adapter.readers.quantum.WebMOReader.FC_LIST, 3));
break;
}
if (!isOK) {
J.util.Logger.error ("atomic orbital order is unrecognized -- skipping reading of MOs due to line: " + this.line);
this.orbitals = null;
}}
});
$_M(c$, "getLine", 
($fz = function () {
return (this.readLine () != null && (this.line.length == 0 || this.line.charAt (0) != '['));
}, $fz.isPrivate = true, $fz));
$_M(c$, "readGaussianBasis", 
function () {
var sdata =  new J.util.JmolList ();
var gdata =  new J.util.JmolList ();
var atomIndex = 0;
var gaussianPtr = 0;
while (this.getLine ()) {
var tokens = this.getTokens ();
if (tokens.length == 0) continue;
if (tokens.length != 1) throw  new Exception ("Error reading GTOs: missing atom index");
var slater =  Clazz.newIntArray (4, 0);
atomIndex = this.parseIntStr (tokens[0]) - 1;
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var nGaussians = this.parseIntStr (tokens[1]);
slater[0] = atomIndex;
slater[1] = J.api.JmolAdapter.getQuantumShellTagID (tokens[0]);
slater[2] = gaussianPtr;
slater[3] = nGaussians;
for (var i = 0; i < nGaussians; i++) {
var strData = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.readLine ());
var nData = strData.length;
var data =  Clazz.newFloatArray (nData, 0);
for (var d = 0; d < nData; d++) {
data[d] = this.parseFloatStr (strData[d]);
}
gdata.addLast (data);
gaussianPtr++;
}
sdata.addLast (slater);
}
var garray = J.util.ArrayUtil.newFloat2 (gaussianPtr);
for (var i = 0; i < gaussianPtr; i++) {
garray[i] = gdata.get (i);
}
this.moData.put ("shells", sdata);
this.moData.put ("gaussians", garray);
if (J.util.Logger.debugging) {
J.util.Logger.debug (sdata.size () + " slater shells read");
J.util.Logger.debug (garray.length + " gaussian primitives read");
}this.atomSetCollection.setAtomSetAuxiliaryInfo ("moData", this.moData);
});
$_M(c$, "readSlaterBasis", 
function () {
while (this.getLine ()) {
var tokens = this.getTokens ();
if (tokens.length < 7) continue;
this.addSlater (this.parseIntStr (tokens[0]) - 1, this.parseIntStr (tokens[1]), this.parseIntStr (tokens[2]), this.parseIntStr (tokens[3]), this.parseIntStr (tokens[4]), this.parseFloatStr (tokens[5]), this.parseFloatStr (tokens[6]));
}
this.setSlaters (false, false);
});
$_M(c$, "readMolecularOrbital", 
function () {
if (this.orbitals == null) {
J.util.Logger.error ("MOLECULAR ORBITALS SKIPPED");
while (this.getLine ()) {
}
return;
}var mo =  new java.util.Hashtable ();
var data =  new J.util.JmolList ();
var energy = this.parseFloatStr (this.readLine ());
var occupancy = this.parseFloatStr (this.readLine ());
while (this.getLine ()) {
var tokens = this.getTokens ();
if (tokens.length == 0) {
continue;
}data.addLast (tokens[1]);
}
var coefs =  Clazz.newFloatArray (data.size (), 0);
for (var i = data.size (); --i >= 0; ) {
coefs[i] = this.parseFloatStr (data.get (i));
}
mo.put ("energy", Float.$valueOf (energy));
mo.put ("occupancy", Float.$valueOf (occupancy));
mo.put ("coefficients", coefs);
this.orbitals.addLast (mo);
this.nOrbitals++;
if (occupancy > 0) this.moData.put ("HOMO", Integer.$valueOf (this.nOrbitals));
});
Clazz.defineStatics (c$,
"DS_LIST", "NOT IMPLEMENTED IN THIS READER",
"DC_LIST", "xx    yy    zz    xy    xz    yz",
"FS_LIST", "NOT IMPLEMENTED IN THIS READER",
"FC_LIST", "xxx   yyy   zzz   yyx   xxy   xxz   zzx   zzy   yyz   xyz");
});
