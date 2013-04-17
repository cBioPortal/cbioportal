Clazz.declarePackage ("J.shapespecial");
Clazz.load (["J.shape.AtomShape", "java.util.Hashtable", "J.util.P3"], "J.shapespecial.Ellipsoids", ["J.util.ArrayUtil", "$.BSUtil", "$.C", "$.Eigen", "$.Escape", "$.Matrix3f", "$.Quadric", "$.SB", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.htEllipsoids = null;
this.haveEllipsoids = false;
this.colixset = null;
this.paletteIDset = null;
this.madset = null;
this.ellipsoid = null;
this.iSelect = 0;
Clazz.instantialize (this, arguments);
}, J.shapespecial, "Ellipsoids", J.shape.AtomShape);
Clazz.prepareFields (c$, function () {
this.htEllipsoids =  new java.util.Hashtable ();
});
Clazz.overrideMethod (c$, "getIndexFromName", 
function (thisID) {
return ((this.ellipsoid = this.htEllipsoids.get (thisID)) == null ? -1 : 1);
}, "~S");
Clazz.overrideMethod (c$, "setSize", 
function (size, bsSelected) {
this.setSize2 (size, bsSelected);
this.checkSets ();
this.madset[this.iSelect] = this.mads;
for (var i = bsSelected.nextSetBit (0); i >= 0; i = bsSelected.nextSetBit (i + 1)) {
if (size != 0) this.atoms[i].scaleEllipsoid (size, this.iSelect);
var isVisible = (this.madset[0] != null && this.madset[0].length > i && this.madset[0][i] > 0 || this.madset[1] != null && this.madset[1].length > i && this.madset[1][i] > 0 || this.madset[2] != null && this.madset[2].length > i && this.madset[2][i] > 0);
this.bsSizeSet.setBitTo (i, isVisible);
this.atoms[i].setShapeVisibility (this.myVisibilityFlag, isVisible);
}
}, "~N,J.util.BS");
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if (propertyName === "thisID") {
this.ellipsoid = (value == null ? null : this.htEllipsoids.get (value));
if (value == null) return;
if (this.ellipsoid == null) {
var id = value;
this.ellipsoid =  new J.shapespecial.Ellipsoids.Ellipsoid (id, this.viewer.getCurrentModelIndex ());
this.htEllipsoids.put (id, this.ellipsoid);
this.haveEllipsoids = true;
}return;
}if (propertyName === "deleteModelAtoms") {
var modelIndex = ((value)[2])[0];
var e = this.htEllipsoids.values ().iterator ();
while (e.hasNext ()) {
var ellipsoid = e.next ();
if (ellipsoid.modelIndex > modelIndex) ellipsoid.modelIndex--;
 else if (ellipsoid.modelIndex == modelIndex) e.remove ();
}
this.haveEllipsoids = !this.htEllipsoids.isEmpty ();
this.ellipsoid = null;
return;
}if (this.ellipsoid != null) {
if ("delete" === propertyName) {
this.htEllipsoids.remove (this.ellipsoid.id);
this.haveEllipsoids = !this.htEllipsoids.isEmpty ();
return;
}if ("modelindex" === propertyName) {
this.ellipsoid.modelIndex = (value).intValue ();
return;
}if ("on" === propertyName) {
this.ellipsoid.isOn = (value).booleanValue ();
return;
}if ("atoms" === propertyName) {
this.setAtoms (value);
return;
}if ("points" === propertyName) {
this.setPoints (value);
return;
}if ("axes" === propertyName) {
this.ellipsoid.isValid = false;
this.ellipsoid.axes = value;
this.ellipsoid.lengths =  Clazz.newFloatArray (3, 0);
this.ellipsoid.scale = 1;
for (var i = 0; i < 2; i++) {
if (this.ellipsoid.axes[i].length () > this.ellipsoid.axes[i + 1].length ()) {
var v = this.ellipsoid.axes[i];
this.ellipsoid.axes[i] = this.ellipsoid.axes[i + 1];
this.ellipsoid.axes[i + 1] = v;
if (i == 1) i = -1;
}}
for (var i = 0; i < 3; i++) {
this.ellipsoid.lengths[i] = this.ellipsoid.axes[i].length ();
if (this.ellipsoid.lengths[i] == 0) return;
this.ellipsoid.axes[i].normalize ();
}
if (Math.abs (this.ellipsoid.axes[0].dot (this.ellipsoid.axes[1])) > 0.0001 || Math.abs (this.ellipsoid.axes[0].dot (this.ellipsoid.axes[1])) > 0.0001 || Math.abs (this.ellipsoid.axes[0].dot (this.ellipsoid.axes[1])) > 0.0001) return;
J.shapespecial.Ellipsoids.updateEquation (this.ellipsoid);
return;
}if ("equation" === propertyName) {
this.ellipsoid.coef = value;
this.ellipsoid.axes =  new Array (3);
this.ellipsoid.lengths =  Clazz.newFloatArray (3, 0);
J.util.Quadric.getAxesForEllipsoid (this.ellipsoid.coef, this.ellipsoid.axes, this.ellipsoid.lengths);
return;
}if ("center" === propertyName) {
this.ellipsoid.center = value;
J.shapespecial.Ellipsoids.updateEquation (this.ellipsoid);
return;
}if ("scale" === propertyName) {
var scale = (value).floatValue ();
if (scale <= 0 || this.ellipsoid.lengths == null) {
this.ellipsoid.isValid = false;
} else {
for (var i = 0; i < 3; i++) this.ellipsoid.lengths[i] *= scale / this.ellipsoid.scale;

this.ellipsoid.scale = scale;
J.shapespecial.Ellipsoids.updateEquation (this.ellipsoid);
}return;
}if ("color" === propertyName) {
this.ellipsoid.colix = J.util.C.getColixO (value);
return;
}if ("translucentLevel" === propertyName) {
this.setPropAS (propertyName, value, bs);
return;
}if ("translucency" === propertyName) {
var isTranslucent = (value.equals ("translucent"));
this.ellipsoid.colix = J.util.C.getColixTranslucent3 (this.ellipsoid.colix, isTranslucent, this.translucentLevel);
return;
}}if ("select" === propertyName) {
this.iSelect = (value).intValue () - 1;
this.checkSets ();
this.colixes = this.colixset[this.iSelect];
this.paletteIDs = this.paletteIDset[this.iSelect];
this.mads = this.madset[this.iSelect];
return;
}this.setPropAS (propertyName, value, bs);
if (this.colixset != null) {
if ("color" === propertyName || "translucency" === propertyName || "deleteModelAtoms" === propertyName) {
this.colixset[this.iSelect] = this.colixes;
this.paletteIDset[this.iSelect] = this.paletteIDs;
this.madset[this.iSelect] = this.mads;
}}}, "~S,~O,J.util.BS");
$_M(c$, "setPoints", 
($fz = function (data) {
var value = data[1];
if (value == null) return;
var bs = data[2];
var n = bs.cardinality ();
if (n < 3) return;
var ptCenter =  new J.util.P3 ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) ptCenter.add (value[i]);

ptCenter.scale (1.0 / n);
this.ellipsoid.center = ptCenter;
var Sxx = 0;
var Syy = 0;
var Szz = 0;
var Sxy = 0;
var Sxz = 0;
var Syz = 0;
var pt =  new J.util.P3 ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
pt.setT (value[i]);
pt.sub (ptCenter);
Sxx += pt.x * pt.x;
Syy += pt.y * pt.y;
Szz += pt.z * pt.z;
Sxy += pt.x * pt.y;
Sxz += pt.x * pt.z;
Syz += pt.y * pt.z;
}
var N =  Clazz.newDoubleArray (3, 3, 0);
N[0][0] = Syy + Szz;
N[0][1] = N[1][0] = -Sxy;
N[0][2] = N[2][0] = -Sxz;
N[1][1] = Sxx + Szz;
N[1][2] = N[2][1] = -Syz;
N[2][2] = Sxx + Syy;
var eigen = J.util.Eigen.newM (N);
this.ellipsoid.axes = eigen.getEigenVectors3 ();
var v = eigen.getEigenvalues ();
this.ellipsoid.lengths =  Clazz.newFloatArray (3, 0);
for (var i = 0; i < 3; i++) this.ellipsoid.lengths[i] = v[i] / n / 3;

this.ellipsoid.scale = 1;
J.shapespecial.Ellipsoids.updateEquation (this.ellipsoid);
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "setAtoms", 
($fz = function (bs) {
var n = bs.cardinality ();
if (n == 0) return;
var atoms = this.viewer.modelSet.atoms;
var ptCenter =  new J.util.P3 ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) ptCenter.add (atoms[i]);

ptCenter.scale (1.0 / n);
this.ellipsoid.center = ptCenter;
var Sxx = 0;
var Syy = 0;
var Szz = 0;
var Sxy = 0;
var Sxz = 0;
var Syz = 0;
var pt =  new J.util.P3 ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
pt.setT (atoms[i]);
pt.sub (ptCenter);
Sxx += pt.x * pt.x;
Syy += pt.y * pt.y;
Szz += pt.z * pt.z;
Sxy += pt.x * pt.y;
Sxz += pt.x * pt.z;
Syz += pt.y * pt.z;
}
var N =  Clazz.newDoubleArray (3, 3, 0);
N[0][0] = Syy + Szz;
N[0][1] = N[1][0] = -Sxy;
N[0][2] = N[2][0] = -Sxz;
N[1][1] = Sxx + Szz;
N[1][2] = N[2][1] = -Syz;
N[2][2] = Sxx + Syy;
var eigen = J.util.Eigen.newM (N);
this.ellipsoid.axes = eigen.getEigenVectors3 ();
var v = eigen.getEigenvalues ();
this.ellipsoid.lengths =  Clazz.newFloatArray (3, 0);
for (var i = 0; i < 3; i++) this.ellipsoid.lengths[i] = v[i] / n / 3;

this.ellipsoid.scale = 1;
J.shapespecial.Ellipsoids.updateEquation (this.ellipsoid);
}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "checkSets", 
($fz = function () {
if (this.colixset == null) {
this.colixset = J.util.ArrayUtil.newShort2 (3);
this.paletteIDset = J.util.ArrayUtil.newByte2 (3);
this.madset = J.util.ArrayUtil.newShort2 (3);
}}, $fz.isPrivate = true, $fz));
c$.updateEquation = $_M(c$, "updateEquation", 
($fz = function (ellipsoid) {
if (ellipsoid.axes == null || ellipsoid.lengths == null) return;
var mat =  new J.util.Matrix3f ();
var mTemp =  new J.util.Matrix3f ();
var v1 =  new J.util.V3 ();
ellipsoid.coef =  Clazz.newDoubleArray (10, 0);
J.util.Quadric.getEquationForQuadricWithCenter (ellipsoid.center.x, ellipsoid.center.y, ellipsoid.center.z, mat, v1, mTemp, ellipsoid.coef, null);
ellipsoid.isValid = true;
}, $fz.isPrivate = true, $fz), "J.shapespecial.Ellipsoids.Ellipsoid");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
var sb =  new J.util.SB ();
this.getStateID (sb);
this.getStateAtoms (sb);
return sb.toString ();
});
$_M(c$, "getStateID", 
($fz = function (sb) {
if (!this.haveEllipsoids) return;
var e = this.htEllipsoids.values ().iterator ();
var v1 =  new J.util.V3 ();
while (e.hasNext ()) {
var ellipsoid = e.next ();
if (ellipsoid.axes == null || ellipsoid.lengths == null) continue;
sb.append ("  Ellipsoid ID ").append (ellipsoid.id).append (" modelIndex ").appendI (ellipsoid.modelIndex).append (" center ").append (J.util.Escape.eP (ellipsoid.center)).append (" axes");
for (var i = 0; i < 3; i++) {
v1.setT (ellipsoid.axes[i]);
v1.scale (ellipsoid.lengths[i]);
sb.append (" ").append (J.util.Escape.eP (v1));
}
sb.append (" " + J.shape.Shape.getColorCommandUnk ("", ellipsoid.colix, this.translucentAllowed));
if (!ellipsoid.isOn) sb.append (" off");
sb.append (";\n");
}
}, $fz.isPrivate = true, $fz), "J.util.SB");
$_M(c$, "getStateAtoms", 
($fz = function (sb) {
if (this.madset == null) return;
var sc = this.viewer.getStateCreator ();
if (sc == null) return;
for (var ii = 0; ii < 3; ii++) {
if (this.madset[ii] == null) continue;
J.shape.Shape.appendCmd (sb, "Ellipsoids set " + (ii + 1) + "\n");
var temp =  new java.util.Hashtable ();
var temp2 =  new java.util.Hashtable ();
if (this.bsSizeSet != null) for (var i = this.bsSizeSet.nextSetBit (0); i >= 0; i = this.bsSizeSet.nextSetBit (i + 1)) J.util.BSUtil.setMapBitSet (temp, i, i, "Ellipsoids " + this.madset[ii][i]);

if (this.bsColixSet != null && this.colixset[ii] != null) for (var i = this.bsColixSet.nextSetBit (0); i >= 0; i = this.bsColixSet.nextSetBit (i + 1)) J.util.BSUtil.setMapBitSet (temp2, i, i, J.shape.Shape.getColorCommand ("Ellipsoids", this.paletteIDset[ii][i], this.colixset[ii][i], this.translucentAllowed));

sb.append (sc.getCommands (temp, temp2, "select"));
}
}, $fz.isPrivate = true, $fz), "J.util.SB");
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
if (!this.haveEllipsoids) return;
var e = this.htEllipsoids.values ().iterator ();
while (e.hasNext ()) {
var ellipsoid = e.next ();
ellipsoid.visible = ellipsoid.isOn && (ellipsoid.modelIndex < 0 || bs.get (ellipsoid.modelIndex));
}
}, "J.util.BS");
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
this.id = null;
this.axes = null;
this.lengths = null;
this.center = null;
this.coef = null;
this.colix = 23;
this.modelIndex = 0;
this.scale = 1;
this.visible = false;
this.isValid = false;
this.isOn = true;
Clazz.instantialize (this, arguments);
}, J.shapespecial.Ellipsoids, "Ellipsoid");
Clazz.prepareFields (c$, function () {
this.center = J.util.P3.new3 (0, 0, 0);
});
Clazz.makeConstructor (c$, 
function (a, b) {
this.id = a;
this.modelIndex = b;
}, "~S,~N");
c$ = Clazz.p0p ();
});
