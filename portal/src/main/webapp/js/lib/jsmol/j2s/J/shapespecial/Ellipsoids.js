Clazz.declarePackage ("J.shapespecial");
Clazz.load (["J.shape.Shape", "java.util.Hashtable"], "J.shapespecial.Ellipsoids", ["J.constant.EnumPalette", "J.shapespecial.Ellipsoid", "J.util.BS", "$.BSUtil", "$.C", "$.Escape", "$.SB", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.simpleEllipsoids = null;
this.atomEllipsoids = null;
this.typeSelected = "1";
this.ellipsoidSelected = null;
this.selectedAtoms = null;
Clazz.instantialize (this, arguments);
}, J.shapespecial, "Ellipsoids", J.shape.Shape);
Clazz.prepareFields (c$, function () {
this.simpleEllipsoids =  new java.util.Hashtable ();
this.atomEllipsoids =  new java.util.Hashtable ();
});
$_M(c$, "isActive", 
function () {
return !this.atomEllipsoids.isEmpty () || !this.simpleEllipsoids.isEmpty ();
});
Clazz.overrideMethod (c$, "getIndexFromName", 
function (thisID) {
return ((this.ellipsoidSelected = this.simpleEllipsoids.get (thisID)) == null ? -1 : 1);
}, "~S");
Clazz.overrideMethod (c$, "setSize", 
function (size, bsSelected) {
if (this.modelSet.atoms == null || size == 0 && this.modelSet.atomTensors == null) return;
var isAll = (bsSelected == null);
if (!isAll && this.selectedAtoms != null) bsSelected = this.selectedAtoms;
var tensors = this.viewer.modelSet.getAllAtomTensors (this.typeSelected);
if (tensors == null) return;
var atoms = this.modelSet.atoms;
for (var i = tensors.size (); --i >= 0; ) {
var t = tensors.get (i);
if (isAll || t.isSelected (bsSelected, -1)) {
var e = this.atomEllipsoids.get (t);
var isNew = (size != 0 && e == null);
if (isNew) this.atomEllipsoids.put (t, e = J.shapespecial.Ellipsoid.getEllipsoidForAtomTensor (t, atoms[t.atomIndex1]));
if (e != null && (isNew || size != 2147483647)) {
e.setScale (size, true);
}}}
}, "~N,J.util.BS");
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if (propertyName === "thisID") {
if (this.initEllipsoids (value) && this.ellipsoidSelected == null) {
var id = value;
this.ellipsoidSelected = J.shapespecial.Ellipsoid.getEmptyEllipsoid (id, this.viewer.getCurrentModelIndex ());
this.simpleEllipsoids.put (id, this.ellipsoidSelected);
}return;
}if (propertyName === "deleteModelAtoms") {
var modelIndex = ((value)[2])[0];
var e = this.simpleEllipsoids.values ().iterator ();
while (e.hasNext ()) if (e.next ().tensor.modelIndex == modelIndex) e.remove ();

e = this.atomEllipsoids.values ().iterator ();
while (e.hasNext ()) if (e.next ().modelIndex == modelIndex) e.remove ();

this.ellipsoidSelected = null;
return;
}if ("atoms" === propertyName) {
this.selectedAtoms = value;
return;
}if (this.ellipsoidSelected != null) {
if ("delete" === propertyName) {
this.simpleEllipsoids.remove (this.ellipsoidSelected.id);
return;
}if ("modelindex" === propertyName) {
this.ellipsoidSelected.tensor.modelIndex = (value).intValue ();
return;
}if ("on" === propertyName) {
this.ellipsoidSelected.isOn = (value).booleanValue ();
return;
}if ("options" === propertyName) {
this.ellipsoidSelected.options = (value).toLowerCase ();
}if ("points" === propertyName) {
return;
}if ("axes" === propertyName) {
this.ellipsoidSelected.setAxes (value);
return;
}if ("equation" === propertyName) {
this.ellipsoidSelected.setEquation (value);
return;
}if ("center" === propertyName) {
this.ellipsoidSelected.setCenter (value);
return;
}if ("scale" === propertyName) {
this.ellipsoidSelected.setScale ((value).floatValue (), false);
return;
}if ("color" === propertyName) {
this.ellipsoidSelected.colix = J.util.C.getColixO (value);
return;
}if ("translucentLevel" === propertyName) {
this.setPropS (propertyName, value, bs);
return;
}if ("translucency" === propertyName) {
var isTranslucent = (value.equals ("translucent"));
this.ellipsoidSelected.colix = J.util.C.getColixTranslucent3 (this.ellipsoidSelected.colix, isTranslucent, this.translucentLevel);
return;
}}if ("select" === propertyName) {
this.typeSelected = (value).toLowerCase ();
return;
}if ("scale" === propertyName) {
this.setSize (Clazz.floatToInt ((value).floatValue () * 100), bs);
return;
}if ("params" === propertyName) {
var data = value;
data[2] = null;
this.typeSelected = "0";
this.setSize (50, bs);
}if ("on" === propertyName) {
var isOn = (value).booleanValue ();
if (this.selectedAtoms != null) bs = this.selectedAtoms;
if (isOn) this.setSize (2147483647, bs);
for (var e, $e = this.atomEllipsoids.values ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var t = e.tensor;
if ((t.type.equals (this.typeSelected) || this.typeSelected.equals (t.altType)) && t.isSelected (bs, -1)) {
e.isOn = isOn;
}}
return;
}if ("options" === propertyName) {
var options = (value).toLowerCase ().trim ();
if (options.length == 0) options = null;
if (this.selectedAtoms != null) bs = this.selectedAtoms;
if (options != null) this.setSize (2147483647, bs);
for (var e, $e = this.atomEllipsoids.values ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) if (e.tensor.type.equals (this.typeSelected) && e.tensor.isSelected (bs, -1)) e.options = options;

return;
}if ("color" === propertyName) {
var colix = J.util.C.getColixO (value);
var pid = J.constant.EnumPalette.pidOf (value);
if (this.selectedAtoms != null) bs = this.selectedAtoms;
for (var e, $e = this.atomEllipsoids.values ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) if (e.tensor.type.equals (this.typeSelected) && e.tensor.isSelected (bs, -1)) {
e.colix = this.getColixI (colix, pid, e.tensor.atomIndex1);
e.pid = pid;
}
return;
}if ("translucency" === propertyName) {
var isTranslucent = (value.equals ("translucent"));
for (var e, $e = this.atomEllipsoids.values ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) if (e.tensor.type.equals (this.typeSelected) && e.tensor.isSelected (bs, -1)) e.colix = J.util.C.getColixTranslucent3 (e.colix, isTranslucent, this.translucentLevel);

return;
}this.setPropS (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "initEllipsoids", 
($fz = function (value) {
var haveID = (value != null);
this.ellipsoidSelected = (haveID ? this.simpleEllipsoids.get (value) : null);
if (haveID) {
this.typeSelected = null;
}this.selectedAtoms = null;
return haveID;
}, $fz.isPrivate = true, $fz), "~O");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
var sc = this.viewer.getStateCreator ();
if (sc == null || !this.isActive ()) return "";
var sb =  new J.util.SB ();
sb.append ("\n");
if (!this.simpleEllipsoids.isEmpty ()) this.getStateID (sb);
if (!this.atomEllipsoids.isEmpty ()) this.getStateAtoms (sb, sc);
return sb.toString ();
});
$_M(c$, "getStateID", 
($fz = function (sb) {
var v1 =  new J.util.V3 ();
for (var ellipsoid, $ellipsoid = this.simpleEllipsoids.values ().iterator (); $ellipsoid.hasNext () && ((ellipsoid = $ellipsoid.next ()) || true);) {
var t = ellipsoid.tensor;
if (!ellipsoid.isValid || t == null) continue;
sb.append ("  Ellipsoid ID ").append (ellipsoid.id).append (" modelIndex ").appendI (t.modelIndex).append (" center ").append (J.util.Escape.eP (ellipsoid.center)).append (" axes");
for (var i = 0; i < 3; i++) {
v1.setT (t.eigenVectors[i]);
v1.scale (ellipsoid.lengths[i]);
sb.append (" ").append (J.util.Escape.eP (v1));
}
sb.append (" " + J.shape.Shape.getColorCommandUnk ("", ellipsoid.colix, this.translucentAllowed));
if (ellipsoid.options != null) sb.append (" options ").append (J.util.Escape.eS (ellipsoid.options));
if (!ellipsoid.isOn) sb.append (" off");
sb.append (";\n");
}
}, $fz.isPrivate = true, $fz), "J.util.SB");
$_M(c$, "getStateAtoms", 
($fz = function (sb, sc) {
var bsDone =  new J.util.BS ();
var temp =  new java.util.Hashtable ();
var temp2 =  new java.util.Hashtable ();
for (var e, $e = this.atomEllipsoids.values ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var iType = e.tensor.iType;
if (bsDone.get (iType + 1)) continue;
bsDone.set (iType + 1);
var isADP = (e.tensor.iType == 1);
var cmd = (isADP ? null : "Ellipsoids set " + J.util.Escape.eS (e.tensor.type));
for (var e2, $e2 = this.atomEllipsoids.values ().iterator (); $e2.hasNext () && ((e2 = $e2.next ()) || true);) {
if (e2.tensor.iType != iType || isADP && !e2.isOn) continue;
var i = e2.tensor.atomIndex1;
J.util.BSUtil.setMapBitSet (temp, i, i, (isADP ? "Ellipsoids " + e2.percent : cmd + " scale " + e2.scale + (e2.options == null ? "" : " options " + J.util.Escape.eS (e2.options)) + (e2.isOn ? " ON" : " OFF")));
if (e2.colix != 0) J.util.BSUtil.setMapBitSet (temp2, i, i, J.shape.Shape.getColorCommand (cmd, e2.pid, e2.colix, this.translucentAllowed));
}
}
sb.append (sc.getCommands (temp, temp2, "select"));
}, $fz.isPrivate = true, $fz), "J.util.SB,J.api.JmolStateCreator");
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
if (!this.isActive ()) return;
var atoms = this.viewer.modelSet.atoms;
this.setVis (this.simpleEllipsoids, bs, atoms);
if (this.atomEllipsoids != null) for (var i = atoms.length; --i >= 0; ) atoms[i].setShapeVisibility (this.myVisibilityFlag, false);

this.setVis (this.atomEllipsoids, bs, atoms);
}, "J.util.BS");
$_M(c$, "setVis", 
($fz = function (ellipsoids, bs, atoms) {
for (var e, $e = ellipsoids.values ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var t = e.tensor;
var isOK = true;
if (t.atomIndex1 >= 0) {
if (t.iType == 1) {
var isModTensor = t.isModulated;
var isUnmodTensor = t.isUnmodulated;
var isModAtom = this.modelSet.isModulated (t.atomIndex1);
isOK = (!isModTensor && !isUnmodTensor || isModTensor == isModAtom);
}atoms[t.atomIndex1].setShapeVisibility (this.myVisibilityFlag, true);
}e.visible = isOK && e.isValid && e.isOn && (e.modelIndex < 0 || bs.get (e.modelIndex));
}
}, $fz.isPrivate = true, $fz), "java.util.Map,J.util.BS,~A");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
if (this.atomEllipsoids.isEmpty ()) return;
for (var e, $e = this.atomEllipsoids.values ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var i = e.tensor.atomIndex1;
var atom = this.modelSet.atoms[i];
if ((atom.getShapeVisibilityFlags () & this.myVisibilityFlag) == 0 || this.modelSet.isAtomHidden (i)) continue;
atom.setClickable (this.myVisibilityFlag);
}
});
});
