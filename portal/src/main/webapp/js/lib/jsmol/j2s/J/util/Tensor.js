Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.Tensor", ["java.lang.Float", "java.util.Arrays", "$.Hashtable", "J.util.Eigen", "$.EigenSort", "$.Escape", "$.Matrix3f", "$.P3", "$.Parser", "$.Quaternion", "$.TextFormat", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.id = null;
this.type = null;
this.iType = -1;
this.asymMatrix = null;
this.symMatrix = null;
this.eigenVectors = null;
this.eigenValues = null;
this.altType = null;
this.isIsotropic = false;
this.forThermalEllipsoid = false;
this.eigenSignMask = 7;
this.typeFactor = 1;
this.sortIso = false;
this.modelIndex = 0;
this.atomIndex1 = -1;
this.atomIndex2 = -1;
this.isModulated = false;
this.isUnmodulated = false;
Clazz.instantialize (this, arguments);
}, J.util, "Tensor");
c$.getType = $_M(c$, "getType", 
($fz = function (type) {
var pt = type.indexOf ("_");
if (pt >= 0) type = type.substring (0, pt);
pt = ";iso........;adp........;tls-u......;tls-r......;ms.........;efg........;isc........;charge.....;quadrupole.".indexOf (";" + type.toLowerCase () + ".");
return (pt < 0 ? -1 : Clazz.doubleToInt (pt / 11));
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getInfo", 
function (infoType) {
if (infoType.charAt (0) != ';') infoType = ";" + infoType + ".";
switch (Clazz.doubleToInt (";.............;eigenvalues..;eigenvectors.;asymmatrix...;symmatrix....;value........;isotropy.....;anisotropy...;asymmetry....;eulerzyz.....;eulerzxz.....;quaternion...;indices......;string.......;type.........;id...........;span.........;skew.........".indexOf (infoType) / 14)) {
default:
var info =  new java.util.Hashtable ();
var s = J.util.Parser.getTokens (J.util.TextFormat.replaceAllCharacter (";.............;eigenvalues..;eigenvectors.;asymmatrix...;symmatrix....;value........;isotropy.....;anisotropy...;asymmetry....;eulerzyz.....;eulerzxz.....;quaternion...;indices......;string.......;type.........;id...........;span.........;skew.........", ";.", ' ').trim ());
java.util.Arrays.sort (s);
for (var i = 0; i < s.length; i++) {
var o = this.getInfo (s[i]);
if (o != null) info.put (s[i], o);
}
return info;
case 1:
return this.eigenValues;
case 2:
var list =  new Array (3);
for (var i = 0; i < 3; i++) list[i] = J.util.P3.newP (this.eigenVectors[i]);

return list;
case 3:
if (this.asymMatrix == null) return null;
var a =  Clazz.newFloatArray (9, 0);
var pt = 0;
for (var i = 0; i < 3; i++) for (var j = 0; j < 3; j++) a[pt++] = this.asymMatrix[i][j];


return J.util.Matrix3f.newA (a);
case 4:
if (this.symMatrix == null) return null;
var b =  Clazz.newFloatArray (9, 0);
var p2 = 0;
for (var i = 0; i < 3; i++) for (var j = 0; j < 3; j++) b[p2++] = this.symMatrix[i][j];


return J.util.Matrix3f.newA (b);
case 5:
return Float.$valueOf (this.eigenValues[2]);
case 6:
return Float.$valueOf (this.isotropy ());
case 7:
return Float.$valueOf (this.anisotropy ());
case 8:
return Float.$valueOf (this.asymmetry ());
case 9:
return (this.getInfo ("quaternion")).getEulerZYZ ();
case 10:
return (this.getInfo ("quaternion")).getEulerZXZ ();
case 11:
return J.util.Quaternion.getQuaternionFrame (null, this.eigenVectors[0], this.eigenVectors[1]);
case 12:
return [this.modelIndex, this.atomIndex1, this.atomIndex2];
case 13:
return this.toString ();
case 14:
return this.type;
case 15:
return this.id;
case 16:
return Float.$valueOf (this.span ());
case 17:
return Float.$valueOf (this.skew ());
}
}, "~S");
$_M(c$, "isotropy", 
function () {
return (this.eigenValues[0] + this.eigenValues[1] + this.eigenValues[2]) / 3;
});
$_M(c$, "span", 
function () {
return Math.abs (this.eigenValues[2] - this.eigenValues[0]);
});
$_M(c$, "skew", 
function () {
return (this.span () == 0 ? 0 : 3 * (this.eigenValues[1] - this.isotropy ()) / this.span ());
});
$_M(c$, "anisotropy", 
function () {
return this.eigenValues[2] - (this.eigenValues[0] + this.eigenValues[1]) / 2;
});
$_M(c$, "reducedAnisotropy", 
function () {
return this.anisotropy () * 2 / 3;
});
$_M(c$, "asymmetry", 
function () {
return this.span () == 0 ? 0 : (this.eigenValues[1] - this.eigenValues[0]) / this.reducedAnisotropy ();
});
c$.copyTensor = $_M(c$, "copyTensor", 
function (t0) {
var t =  new J.util.Tensor ();
t.setType (t0.type);
t.eigenValues = t0.eigenValues;
t.eigenVectors = t0.eigenVectors;
t.asymMatrix = t0.asymMatrix;
t.symMatrix = t0.symMatrix;
t.eigenSignMask = t0.eigenSignMask;
t.modelIndex = t0.modelIndex;
t.atomIndex1 = t0.atomIndex1;
t.atomIndex2 = t0.atomIndex2;
t.id = t0.id;
return t;
}, "J.util.Tensor");
Clazz.makeConstructor (c$, 
($fz = function () {
}, $fz.isPrivate = true, $fz));
c$.getTensorFromAsymmetricTensor = $_M(c$, "getTensorFromAsymmetricTensor", 
function (asymmetricTensor, type, id) {
var a =  Clazz.newDoubleArray (3, 3, 0);
for (var i = 3; --i >= 0; ) for (var j = 3; --j >= 0; ) a[i][j] = asymmetricTensor[i][j];


if (a[0][1] != a[1][0]) {
a[0][1] = a[1][0] = (a[0][1] + a[1][0]) / 2;
}if (a[1][2] != a[2][1]) {
a[1][2] = a[2][1] = (a[1][2] + a[2][1]) / 2;
}if (a[0][2] != a[2][0]) {
a[0][2] = a[2][0] = (a[0][2] + a[2][0]) / 2;
}var eigen =  new J.util.Eigen (3);
eigen.calc (a);
var m =  new J.util.Matrix3f ();
var mm =  Clazz.newFloatArray (9, 0);
for (var i = 0, p = 0; i < 3; i++) for (var j = 0; j < 3; j++) mm[p++] = a[i][j];


m.setA (mm);
var evec = eigen.getEigenVectors3 ();
var n =  new J.util.V3 ();
var cross =  new J.util.V3 ();
for (var i = 0; i < 3; i++) {
n.setT (evec[i]);
m.transform (n);
cross.cross (n, evec[i]);
n.setT (evec[i]);
n.normalize ();
cross.cross (evec[i], evec[(i + 1) % 3]);
}
var vectors =  new Array (3);
var values =  Clazz.newFloatArray (3, 0);
eigen.fillArrays (vectors, values);
var t = J.util.Tensor.newTensorType (vectors, values, type, id);
t.asymMatrix = asymmetricTensor;
t.symMatrix = a;
t.id = id;
return t;
}, "~A,~S,~S");
c$.getTensorFromEigenVectors = $_M(c$, "getTensorFromEigenVectors", 
function (eigenVectors, eigenValues, type, id) {
var values =  Clazz.newFloatArray (3, 0);
var vectors =  new Array (3);
for (var i = 0; i < 3; i++) {
vectors[i] = J.util.V3.newV (eigenVectors[i]);
values[i] = eigenValues[i];
}
return J.util.Tensor.newTensorType (vectors, values, type, id);
}, "~A,~A,~S,~S");
c$.getTensorFromAxes = $_M(c$, "getTensorFromAxes", 
function (axes) {
var t =  new J.util.Tensor ();
t.eigenValues =  Clazz.newFloatArray (3, 0);
t.eigenVectors =  new Array (3);
for (var i = 0; i < 3; i++) {
t.eigenVectors[i] = J.util.V3.newV (axes[i]);
t.eigenValues[i] = axes[i].length ();
if (t.eigenValues[i] == 0) return null;
t.eigenVectors[i].normalize ();
}
if (Math.abs (t.eigenVectors[0].dot (t.eigenVectors[1])) > 0.0001 || Math.abs (t.eigenVectors[1].dot (t.eigenVectors[2])) > 0.0001 || Math.abs (t.eigenVectors[2].dot (t.eigenVectors[0])) > 0.0001) return null;
t.setType ("other");
t.sortAndNormalize ();
return t;
}, "~A");
c$.getTensorFromThermalEquation = $_M(c$, "getTensorFromThermalEquation", 
function (coefs, id) {
var t =  new J.util.Tensor ();
t.eigenValues =  Clazz.newFloatArray (3, 0);
t.eigenVectors =  new Array (3);
t.id = (id == null ? "coefs=" + J.util.Escape.eAD (coefs) : id);
var mat =  Clazz.newDoubleArray (3, 3, 0);
mat[0][0] = coefs[0];
mat[1][1] = coefs[1];
mat[2][2] = coefs[2];
mat[0][1] = mat[1][0] = coefs[3] / 2;
mat[0][2] = mat[2][0] = coefs[4] / 2;
mat[1][2] = mat[2][1] = coefs[5] / 2;
J.util.Eigen.getUnitVectors (mat, t.eigenVectors, t.eigenValues);
t.setType ("adp");
t.sortAndNormalize ();
return t;
}, "~A,~S");
$_M(c$, "setType", 
function (type) {
if (this.type == null || type == null) this.type = type;
if (type != null) this.processType ();
return this;
}, "~S");
$_M(c$, "getFactoredValue", 
function (i) {
var f = Math.abs (this.eigenValues[i]);
return (this.forThermalEllipsoid ? Math.sqrt (f) : f) * this.typeFactor;
}, "~N");
$_M(c$, "setAtomIndexes", 
function (index1, index2) {
this.atomIndex1 = index1;
this.atomIndex2 = index2;
}, "~N,~N");
$_M(c$, "isSelected", 
function (bsSelected, iAtom) {
return (iAtom >= 0 ? (this.atomIndex1 == iAtom || this.atomIndex2 == iAtom) : bsSelected.get (this.atomIndex1) && (this.atomIndex2 < 0 || bsSelected.get (this.atomIndex2)));
}, "J.util.BS,~N");
c$.newTensorType = $_M(c$, "newTensorType", 
($fz = function (vectors, values, type, id) {
var t =  new J.util.Tensor ();
t.eigenValues = values;
t.eigenVectors = vectors;
for (var i = 0; i < 3; i++) t.eigenVectors[i].normalize ();

t.setType (type);
t.id = id;
t.sortAndNormalize ();
t.eigenSignMask = (t.eigenValues[0] >= 0 ? 1 : 0) + (t.eigenValues[1] >= 0 ? 2 : 0) + (t.eigenValues[2] >= 0 ? 4 : 0);
return t;
}, $fz.isPrivate = true, $fz), "~A,~A,~S,~S");
$_M(c$, "processType", 
($fz = function () {
this.forThermalEllipsoid = false;
this.isIsotropic = false;
this.altType = null;
this.typeFactor = 1;
this.sortIso = false;
switch (this.iType = J.util.Tensor.getType (this.type)) {
case 0:
this.forThermalEllipsoid = true;
this.isIsotropic = true;
this.altType = "1";
this.type = "adp";
break;
case 1:
this.forThermalEllipsoid = true;
this.typeFactor = J.util.Tensor.ADP_FACTOR;
this.altType = "1";
break;
case 4:
this.sortIso = true;
this.typeFactor = 0.01;
break;
case 5:
this.sortIso = true;
break;
case 6:
this.sortIso = true;
this.typeFactor = 0.04;
break;
case 3:
this.altType = "2";
break;
case 2:
this.altType = "3";
break;
case 7:
case 8:
break;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "sortAndNormalize", 
($fz = function () {
var o = [[J.util.V3.newV (this.eigenVectors[0]), Float.$valueOf (this.eigenValues[0])], [J.util.V3.newV (this.eigenVectors[1]), Float.$valueOf (this.eigenValues[1])], [J.util.V3.newV (this.eigenVectors[2]), Float.$valueOf (this.eigenValues[2])]];
java.util.Arrays.sort (o, J.util.Tensor.getEigenSort ());
for (var i = 0; i < 3; i++) {
var pt = i;
this.eigenVectors[i] = o[pt][0];
this.eigenValues[i] = (o[pt][1]).floatValue ();
}
if (this.sortIso && this.eigenValues[2] - this.eigenValues[1] < this.eigenValues[1] - this.eigenValues[0]) {
var vTemp = this.eigenVectors[0];
this.eigenVectors[0] = this.eigenVectors[2];
this.eigenVectors[2] = vTemp;
var f = this.eigenValues[0];
this.eigenValues[0] = this.eigenValues[2];
this.eigenValues[2] = f;
}for (var i = 0; i < 3; i++) this.eigenVectors[i].normalize ();

}, $fz.isPrivate = true, $fz));
$_M(c$, "isEquiv", 
function (t) {
if (t.iType != this.iType) return false;
var f = Math.abs (this.eigenValues[0] + this.eigenValues[1] + this.eigenValues[2]);
for (var i = 0; i < 3; i++) if (Math.abs (t.eigenValues[i] - this.eigenValues[i]) / f > 0.0003) return false;

return true;
}, "J.util.Tensor");
c$.getEigenSort = $_M(c$, "getEigenSort", 
($fz = function () {
return (J.util.Tensor.tSort == null ? (($t$ = J.util.Tensor.tSort =  new J.util.EigenSort (), J.util.Tensor.prototype.tSort = J.util.Tensor.tSort, $t$)) : J.util.Tensor.tSort);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "toString", 
function () {
return (this.type + " " + this.modelIndex + " " + this.atomIndex1 + " " + this.atomIndex2 + "\n" + (this.eigenVectors == null ? "" + this.eigenValues[0] : this.eigenVectors[0] + "\t" + this.eigenValues[0] + "\t" + "\n" + this.eigenVectors[1] + "\t" + this.eigenValues[1] + "\t" + "\n" + this.eigenVectors[2] + "\t" + this.eigenValues[2] + "\t" + "\n"));
});
c$.ADP_FACTOR = c$.prototype.ADP_FACTOR = (Math.sqrt (0.5) / 3.141592653589793);
Clazz.defineStatics (c$,
"MAGNETIC_SUSCEPTIBILITY_FACTOR", 0.01,
"INTERACTION_FACTOR", 0.04,
"tSort", null,
"KNOWN_TYPES", ";iso........;adp........;tls-u......;tls-r......;ms.........;efg........;isc........;charge.....;quadrupole.",
"TYPE_OTHER", -1,
"TYPE_ISO", 0,
"TYPE_ADP", 1,
"TYPE_TLS_U", 2,
"TYPE_TLS_R", 3,
"TYPE_MS", 4,
"TYPE_EFG", 5,
"TYPE_ISC", 6,
"TYPE_CHARGE", 7,
"TYPE_QUADRUPOLE", 8,
"infoList", ";.............;eigenvalues..;eigenvectors.;asymmatrix...;symmatrix....;value........;isotropy.....;anisotropy...;asymmetry....;eulerzyz.....;eulerzxz.....;quaternion...;indices......;string.......;type.........;id...........;span.........;skew.........");
});
