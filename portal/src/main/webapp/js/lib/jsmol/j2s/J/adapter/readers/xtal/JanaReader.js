Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.readers.cif.ModulationReader"], "J.adapter.readers.xtal.JanaReader", ["java.util.Hashtable", "J.adapter.smarter.Atom", "J.io.JmolBinary", "J.util.BS", "$.JmolList", "$.Logger", "$.Matrix4f", "$.P3", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.lattvecs = null;
this.thisSub = 0;
this.qicount = 0;
this.LABELS = "xyz";
this.floats = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "JanaReader", J.adapter.readers.cif.ModulationReader);
Clazz.prepareFields (c$, function () {
this.floats =  Clazz.newFloatArray (6, 0);
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.setFractionalCoordinates (true);
this.initializeModulation ();
this.atomSetCollection.newAtomSet ();
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.length < 3) return true;
J.util.Logger.info (this.line);
this.parseTokenStr (this.line);
switch ("tit  cell ndim qi   lat  sym  spg  end  wma".indexOf (this.line.substring (0, 3))) {
case 0:
this.atomSetCollection.setAtomSetName (this.line.substring (5).trim ());
break;
case 5:
this.cell ();
this.setSymmetryOperator ("x,y,z");
break;
case 10:
this.ndim ();
break;
case 20:
this.lattvec (this.line.substring (8));
break;
case 30:
this.setSpaceGroupName (this.getTokens ()[1]);
break;
case 25:
this.symmetry ();
break;
case 15:
if (!this.modAverage) this.qi ();
break;
case 35:
this.continuing = false;
break;
case 40:
var m =  new J.util.Matrix4f ();
if (this.thisSub++ == 0) {
m.setIdentity ();
this.addSubsystem ("1", m, null);
this.thisSub++;
m =  new J.util.Matrix4f ();
}var data =  Clazz.newFloatArray (16, 0);
this.fillFloatArray (null, 0, data);
m.setA (data, 0);
this.addSubsystem ("" + this.thisSub, m, null);
}
return true;
});
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
this.readM40Data ();
if (this.lattvecs != null) this.atomSetCollection.getSymmetry ().addLatticeVectors (this.lattvecs);
this.applySymmetryAndSetTrajectory ();
this.adjustM40Occupancies ();
this.setModulation ();
this.finalizeModulation ();
this.finalizeReaderASCR ();
});
$_M(c$, "cell", 
($fz = function () {
for (var ipt = 0; ipt < 6; ipt++) this.setUnitCellItem (ipt, this.parseFloat ());

}, $fz.isPrivate = true, $fz));
$_M(c$, "ndim", 
($fz = function () {
this.setModDim (this.parseIntStr (this.getTokens ()[1]) - 3);
}, $fz.isPrivate = true, $fz));
$_M(c$, "qi", 
($fz = function () {
var pt = J.util.P3.new3 (this.parseFloat (), this.parseFloat (), this.parseFloat ());
this.addModulation (null, "W_" + (++this.qicount), pt, -1);
pt =  new J.util.P3 ();
switch (this.qicount) {
case 1:
pt.x = 1;
break;
case 2:
pt.y = 1;
break;
case 3:
pt.z = 1;
break;
}
this.addModulation (null, "F_" + this.qicount + "_q_", pt, -1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "lattvec", 
($fz = function (data) {
var a;
var c = data.charAt (0);
switch (c) {
case 'P':
case 'X':
return;
case 'A':
case 'B':
case 'C':
case 'I':
a = [0.5, 0.5, 0.5];
if (c != 'I') a[c.charCodeAt (0) - 65] = 0;
break;
case 'F':
this.lattvec ("A");
this.lattvec ("B");
this.lattvec ("C");
return;
case '0':
if (data.indexOf (".") < 0) return;
a = J.adapter.smarter.AtomSetCollectionReader.getTokensFloat (data, null, this.modDim + 3);
break;
default:
this.appendLoadNote (this.line + " not supported");
return;
}
if (this.lattvecs == null) this.lattvecs =  new J.util.JmolList ();
this.lattvecs.addLast (a);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "symmetry", 
($fz = function () {
this.setSymmetryOperator (J.util.TextFormat.simpleReplace (this.line.substring (9).trim (), " ", ","));
}, $fz.isPrivate = true, $fz));
$_M(c$, "readM40Data", 
($fz = function () {
var name = this.filePath;
var ipt = name.lastIndexOf (".");
if (ipt < 0) return;
name = name.substring (0, ipt + 2) + "40";
var id = name.substring (0, ipt);
ipt = id.lastIndexOf ("/");
id = id.substring (ipt + 1);
var r = J.io.JmolBinary.getBufferedReaderForString (this.viewer.getLigandModel (id, name, "_file", "----"));
if (this.readM40Floats (r).startsWith ("command")) this.readM40WaveVectors (r);
var newSub = this.getSubSystemList ();
var iSub = (newSub == null ? 0 : 1);
var nAtoms = -1;
while (this.readM40Floats (r) != null) {
while (this.line != null && (this.line.length == 0 || this.line.charAt (0) == ' ' || this.line.charAt (0) == '-')) {
this.readM40Floats (r);
}
if (this.line == null) break;
nAtoms++;
var atom =  new J.adapter.smarter.Atom ();
atom.atomName = this.line.substring (0, 9).trim ();
J.util.Logger.info (this.line);
if (!this.filterAtom (atom, 0)) continue;
if (iSub > 0) {
if (newSub.get (nAtoms)) iSub++;
this.addSubsystem ("" + iSub, null, atom.atomName);
}var o_site = atom.foccupancy = this.floats[2];
this.setAtomCoordXYZ (atom, this.floats[3], this.floats[4], this.floats[5]);
this.atomSetCollection.addAtom (atom);
if (!this.incommensurate) continue;
var label = ";" + atom.atomName;
var haveSpecialOcc = (this.getInt (60, 61) > 0);
var haveSpecialDisp = (this.getInt (61, 62) > 0);
var haveSpecialUij = (this.getInt (62, 63) > 0);
var nOcc = this.getInt (65, 68);
var nDisp = this.getInt (68, 71);
var nUij = this.getInt (71, 74);
this.readM40Floats (r);
var isIso = true;
for (var j = 1; j < 6; j++) if (this.floats[j] != 0) {
isIso = false;
break;
}
if (isIso) {
if (this.floats[0] != 0) this.setU (atom, 7, this.floats[0]);
} else {
for (var j = 0; j < 6; j++) this.setU (atom, j, this.floats[j]);

}var pt;
var o_0 = (nOcc > 0 && !haveSpecialOcc ? this.parseFloatStr (r.readLine ()) : 1);
if (o_0 != 1) {
this.addModulation (null, "J_O#0;" + atom.atomName, J.util.P3.new3 (o_site, o_0, 0), -1);
}atom.foccupancy = o_0 * o_site;
var wv = 0;
var a1;
var a2;
for (var j = 0; j < nOcc; j++) {
if (haveSpecialOcc) {
var data = this.readM40FloatLines (2, 1, r);
a2 = data[0][0];
a1 = data[1][0];
} else {
wv = j + 1;
this.readM40Floats (r);
a2 = this.floats[0];
a1 = this.floats[1];
}id = "O_" + wv + "#0" + label;
pt = J.util.P3.new3 (a1, a2, 0);
if (a1 != 0 || a2 != 0) this.addModulation (null, id, pt, -1);
}
for (var j = 0; j < nDisp; j++) {
if (haveSpecialDisp) {
this.readM40Floats (r);
var c = this.floats[3];
var w = this.floats[4];
for (var k = 0; k < 3; k++) if (this.floats[k] != 0) this.addModulation (null, "D_S#" + "xyz".charAt (k) + label, J.util.P3.new3 (c, w, this.floats[k]), -1);

} else {
this.addSinCos (j, "D_", label, r);
}}
for (var j = 0; j < nUij; j++) {
this.checkFourier (j);
if (isIso) {
this.addSinCos (j, "U_", label, r);
} else {
if (haveSpecialUij) {
J.util.Logger.error ("JanaReader -- not interpreting SpecialUij flag: " + this.line);
} else {
var data = this.readM40FloatLines (2, 6, r);
for (var k = 0, p = 0; k < 6; k++, p += 3) this.addModulation (null, "U_" + (j + 1) + "#" + "U11U22U33U12U13U23OTPUISO".substring (p, p + 3) + label, J.util.P3.new3 (data[1][k], data[0][k], 0), -1);

}}}
}
r.close ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "getSubSystemList", 
($fz = function () {
if (this.htSubsystems == null) return null;
var bs =  new J.util.BS ();
var tokens = this.getTokens ();
for (var i = 0, n = 0; i < tokens.length; i += 2) {
var nAtoms = this.parseIntStr (tokens[i]);
if (nAtoms == 0) break;
bs.set (n = n + nAtoms);
}
return bs;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readM40WaveVectors", 
($fz = function (r) {
while (!this.readM40Floats (r).contains ("end")) if (this.line.startsWith ("wave")) {
var tokens = this.getTokens ();
var pt =  new J.util.P3 ();
switch (this.modDim) {
case 3:
pt.z = this.parseFloatStr (tokens[4]);
case 2:
pt.y = this.parseFloatStr (tokens[3]);
case 1:
pt.x = this.parseFloatStr (tokens[2]);
}
this.addModulation (null, "F_" + this.parseIntStr (tokens[1]) + "_q_", pt, -1);
}
this.readM40Floats (r);
}, $fz.isPrivate = true, $fz), "java.io.BufferedReader");
$_M(c$, "addSinCos", 
($fz = function (j, key, label, r) {
this.checkFourier (j);
this.readM40Floats (r);
for (var k = 0; k < 3; ++k) {
var ccos = this.floats[k + 3];
var csin = this.floats[k];
if (csin == 0 && ccos == 0) continue;
var axis = "" + "xyz".charAt (k % 3);
if (this.modAxes != null && this.modAxes.indexOf (axis.toUpperCase ()) < 0) continue;
var id = key + (j + 1) + "#" + axis + label;
var pt = J.util.P3.new3 (ccos, csin, 0);
this.addModulation (null, id, pt, -1);
}
}, $fz.isPrivate = true, $fz), "~N,~S,~S,java.io.BufferedReader");
$_M(c$, "checkFourier", 
($fz = function (j) {
var pt;
if (j > 0 && this.getModulationVector ("F_" + (j + 1) + "_q_") == null && (pt = this.getModulationVector ("F_1_q_")) != null) {
pt = J.util.P3.newP (pt);
pt.scale (j + 1);
this.addModulation (null, "F_" + (j + 1) + "_q_", pt, -1);
}}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getInt", 
($fz = function (col1, col2) {
var n = this.line.length;
return (n > col1 ? this.parseIntStr (this.line.substring (col1, Math.min (n, col2))) : 0);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "readM40Floats", 
($fz = function (r) {
if ((this.line = r.readLine ()) == null || this.line.indexOf ("-------") >= 0) return (this.line = null);
if (J.util.Logger.debugging) J.util.Logger.debug (this.line);
var ptLast = this.line.length - 10;
for (var i = 0, pt = 0; i < 6 && pt <= ptLast; i++, pt += 9) this.floats[i] = this.parseFloatStr (this.line.substring (pt, pt + 9));

return this.line;
}, $fz.isPrivate = true, $fz), "java.io.BufferedReader");
$_M(c$, "readM40FloatLines", 
($fz = function (nLines, nFloats, r) {
var data =  Clazz.newFloatArray (nLines, nFloats, 0);
for (var i = 0; i < nLines; i++) {
this.readM40Floats (r);
for (var j = 0; j < nFloats; j++) data[i][j] = this.floats[j];

}
return data;
}, $fz.isPrivate = true, $fz), "~N,~N,java.io.BufferedReader");
$_M(c$, "adjustM40Occupancies", 
($fz = function () {
var htSiteMult =  new java.util.Hashtable ();
var atoms = this.atomSetCollection.getAtoms ();
for (var i = this.atomSetCollection.getAtomCount (); --i >= 0; ) {
var a = atoms[i];
var ii = htSiteMult.get (a.atomName);
if (ii == null) {
htSiteMult.put (a.atomName, ii = Integer.$valueOf (this.atomSetCollection.getSymmetry ().getSiteMultiplicity (a)));
}a.foccupancy *= ii.intValue ();
}
}, $fz.isPrivate = true, $fz));
Clazz.defineStatics (c$,
"records", "tit  cell ndim qi   lat  sym  spg  end  wma",
"TITLE", 0,
"CELL", 5,
"NDIM", 10,
"QI", 15,
"LATT", 20,
"SYM", 25,
"SPG", 30,
"END", 35,
"WMATRIX", 40);
});
