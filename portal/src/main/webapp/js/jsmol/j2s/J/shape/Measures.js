Clazz.declarePackage ("J.shape");
Clazz.load (["J.api.JmolMeasurementClient", "J.shape.AtomShape", "J.util.JmolList"], "J.shape.Measures", ["java.lang.Float", "java.util.Hashtable", "J.modelset.Measurement", "$.MeasurementData", "J.util.BS", "$.BSUtil", "$.C", "$.Escape"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bsSelected = null;
this.strFormat = null;
this.mustBeConnected = false;
this.mustNotBeConnected = false;
this.radiusData = null;
this.intramolecular = null;
this.measurementCount = 0;
this.measurements = null;
this.measurementPending = null;
this.colix = 0;
this.font3d = null;
this.tickInfo = null;
this.defaultTickInfo = null;
this.tokAction = 0;
Clazz.instantialize (this, arguments);
}, J.shape, "Measures", J.shape.AtomShape, J.api.JmolMeasurementClient);
Clazz.prepareFields (c$, function () {
this.measurements =  new J.util.JmolList ();
});
Clazz.overrideMethod (c$, "initModelSet", 
function () {
for (var i = this.measurements.size (); --i >= 0; ) {
var m = this.measurements.get (i);
if (m != null) m.modelSet = this.modelSet;
}
this.atoms = this.modelSet.atoms;
});
Clazz.overrideMethod (c$, "initShape", 
function () {
this.font3d = this.gdata.getFont3D (15);
});
Clazz.overrideMethod (c$, "setSize", 
function (size, bsSelected) {
this.mad = size;
}, "~N,J.util.BS");
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bsIgnored) {
var mt;
if ("clearModelIndex" === propertyName) {
for (var i = 0; i < this.measurementCount; i++) this.measurements.get (i).setModelIndex (0);

return;
}if ("color" === propertyName) {
this.setColor (value == null ? 0 : J.util.C.getColixO (value));
return;
}if ("delete" === propertyName) {
this.deleteO (value);
this.setIndices ();
return;
}if ("font" === propertyName) {
this.font3d = value;
return;
}if ("hideAll" === propertyName) {
this.showHide ((value).booleanValue ());
return;
}if ("pending" === propertyName) {
this.pending (value);
return;
}var isRefresh;
if ((isRefresh = ("refresh" === propertyName)) || "refreshTrajectories" === propertyName) {
for (var i = this.measurements.size (); --i >= 0; ) if ((mt = this.measurements.get (i)) != null && (isRefresh || mt.isTrajectory)) mt.refresh ();

return;
}if ("select" === propertyName) {
var bs = value;
if (bs == null || J.util.BSUtil.cardinalityOf (bs) == 0) {
this.bsSelected = null;
} else {
this.bsSelected =  new J.util.BS ();
this.bsSelected.or (bs);
}return;
}if ("setFormats" === propertyName) {
this.setFormats (value);
return;
}this.bsSelected = null;
if ("maps" === propertyName) {
var maps = value;
for (var i = 0; i < maps.length; i++) {
var len = maps[i].length;
if (len < 2 || len > 4) continue;
var v =  Clazz.newIntArray (len + 1, 0);
v[0] = len;
System.arraycopy (maps[i], 0, v, 1, len);
this.toggleOn (v);
}
} else if ("measure" === propertyName) {
var md = value;
this.tickInfo = md.tickInfo;
if (md.tickInfo != null && md.tickInfo.id.equals ("default")) {
this.defaultTickInfo = md.tickInfo;
return;
}this.radiusData = md.radiusData;
this.mustBeConnected = md.mustBeConnected;
this.mustNotBeConnected = md.mustNotBeConnected;
this.intramolecular = md.intramolecular;
this.strFormat = md.strFormat;
if (md.isAll) {
if (this.tickInfo != null) this.define (md, 12291);
this.define (md, md.tokAction);
this.setIndices ();
return;
}var pt = this.setSingleItem (md.points);
switch (md.tokAction) {
case 12291:
this.defineAll (-2147483648, pt, true, false, false);
this.setIndices ();
break;
case 1048589:
this.showHideM (pt, false);
break;
case 1048588:
this.showHideM (pt, true);
break;
case 1060866:
this.deleteM (pt);
if (md.colix != 0) pt.colix = md.colix;
this.toggle (pt);
break;
case 269484114:
this.toggle (pt);
}
return;
}if ("clear" === propertyName) {
this.clear ();
return;
}if ("deleteModelAtoms" === propertyName) {
this.atoms = (value)[1];
var modelIndex = ((value)[2])[0];
var firstAtomDeleted = ((value)[2])[1];
var nAtomsDeleted = ((value)[2])[2];
var atomMax = firstAtomDeleted + nAtomsDeleted;
for (var i = this.measurementCount; --i >= 0; ) {
mt = this.measurements.get (i);
var indices = mt.getCountPlusIndices ();
for (var j = 1; j <= indices[0]; j++) {
var iAtom = indices[j];
if (iAtom >= firstAtomDeleted) {
if (iAtom < atomMax) {
this.deleteI (i);
break;
}indices[j] -= nAtomsDeleted;
} else if (iAtom < 0) {
var pt = mt.getAtom (j);
if (pt.modelIndex > modelIndex) {
pt.modelIndex--;
} else if (pt.modelIndex == modelIndex) {
this.deleteI (i);
break;
}}}
}
return;
}if ("hide" === propertyName) {
this.showHideM ( new J.modelset.Measurement ().setPoints (this.modelSet, value, null, null), true);
return;
}if ("reformatDistances" === propertyName) {
this.reformatDistances ();
return;
}if ("show" === propertyName) {
this.showHideM ( new J.modelset.Measurement ().setPoints (this.modelSet, value, null, null), false);
return;
}if ("toggle" === propertyName) {
this.toggle ( new J.modelset.Measurement ().setPoints (this.modelSet, value, null, null));
return;
}if ("toggleOn" === propertyName) {
this.toggleOn (value);
return;
}}, "~S,~O,J.util.BS");
$_M(c$, "setSingleItem", 
($fz = function (vector) {
var points =  new Array (4);
var indices =  Clazz.newIntArray (5, 0);
indices[0] = vector.size ();
for (var i = vector.size (); --i >= 0; ) {
var value = vector.get (i);
if (Clazz.instanceOf (value, J.util.BS)) {
var atomIndex = (value).nextSetBit (0);
if (atomIndex < 0) return null;
indices[i + 1] = atomIndex;
} else {
points[i] = value;
indices[i + 1] = -2 - i;
}}
return  new J.modelset.Measurement ().setPoints (this.modelSet, indices, points, this.tickInfo == null ? this.defaultTickInfo : this.tickInfo);
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
Clazz.overrideMethod (c$, "getProperty", 
function (property, index) {
if ("pending".equals (property)) return this.measurementPending;
if ("count".equals (property)) return Integer.$valueOf (this.measurementCount);
if ("countPlusIndices".equals (property)) return (index < this.measurementCount ? this.measurements.get (index).getCountPlusIndices () : null);
if ("stringValue".equals (property)) return (index < this.measurementCount ? this.measurements.get (index).getString () : null);
if ("pointInfo".equals (property)) return this.measurements.get (Clazz.doubleToInt (index / 10)).getLabel (index % 10, false, false);
if ("info".equals (property)) return this.getAllInfo ();
if ("infostring".equals (property)) return this.getAllInfoAsString ();
return null;
}, "~S,~N");
$_M(c$, "clear", 
($fz = function () {
if (this.measurementCount == 0) return;
this.measurementCount = 0;
this.measurements.clear ();
this.viewer.setStatusMeasuring ("measureDeleted", -1, "all", 0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setColor", 
($fz = function (colix) {
if (this.bsColixSet == null) this.bsColixSet =  new J.util.BS ();
if (this.bsSelected == null) this.colix = colix;
var mt;
for (var i = this.measurements.size (); --i >= 0; ) if ((mt = this.measurements.get (i)) != null && (this.bsSelected != null && this.bsSelected.get (i) || this.bsSelected == null && (colix == 0 || mt.colix == 0))) {
mt.colix = colix;
this.bsColixSet.set (i);
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setFormats", 
($fz = function (format) {
if (format != null && format.length == 0) format = null;
for (var i = this.measurements.size (); --i >= 0; ) if (this.bsSelected == null || this.bsSelected.get (i)) this.measurements.get (i).formatMeasurementAs (format, null, false);

}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "showHide", 
($fz = function (isHide) {
for (var i = this.measurements.size (); --i >= 0; ) if (this.bsSelected == null || this.bsSelected.get (i)) this.measurements.get (i).isHidden = isHide;

}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "showHideM", 
($fz = function (m, isHide) {
var i = this.find (m);
if (i >= 0) this.measurements.get (i).isHidden = isHide;
}, $fz.isPrivate = true, $fz), "J.modelset.Measurement,~B");
$_M(c$, "toggle", 
($fz = function (m) {
this.radiusData = null;
var i = this.find (m);
var mt;
if (i >= 0 && !(mt = this.measurements.get (i)).isHidden) this.defineAll (i, mt, true, false, false);
 else this.defineAll (-1, m, false, true, false);
this.setIndices ();
}, $fz.isPrivate = true, $fz), "J.modelset.Measurement");
$_M(c$, "toggleOn", 
($fz = function (indices) {
this.radiusData = null;
this.bsSelected =  new J.util.BS ();
this.defineAll (-2147483648,  new J.modelset.Measurement ().setPoints (this.modelSet, indices, null, this.defaultTickInfo), false, true, true);
this.setIndices ();
this.reformatDistances ();
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "deleteM", 
($fz = function (m) {
this.radiusData = null;
var i = this.find (m);
if (i >= 0) this.defineAll (i, this.measurements.get (i), true, false, false);
this.setIndices ();
}, $fz.isPrivate = true, $fz), "J.modelset.Measurement");
$_M(c$, "deleteO", 
($fz = function (value) {
if ((Clazz.instanceOf (value, Integer))) {
this.deleteI ((value).intValue ());
} else if (J.util.Escape.isAI (value)) {
this.defineAll (-2147483648,  new J.modelset.Measurement ().setPoints (this.modelSet, value, null, null), true, false, false);
}}, $fz.isPrivate = true, $fz), "~O");
$_M(c$, "defineAll", 
($fz = function (iPt, m, isDelete, isShow, doSelect) {
if (!this.viewer.getMeasureAllModelsFlag ()) {
if (isDelete) {
if (iPt == -2147483648) iPt = this.find (m);
if (iPt >= 0) this.deleteI (iPt);
return;
}this.defineMeasurement (iPt, m, doSelect);
return;
}if (isShow) {
this.defineAll (iPt, m, true, false, false);
if (isDelete) return;
}var points =  new J.util.JmolList ();
var nPoints = m.getCount ();
for (var i = 1; i <= nPoints; i++) {
var atomIndex = m.getAtomIndex (i);
points.addLast (atomIndex >= 0 ? this.viewer.getAtomBits (1095763969, Integer.$valueOf (this.atoms[atomIndex].getAtomNumber ())) : m.getAtom (i));
}
this.define (( new J.modelset.MeasurementData (this.viewer, points)).set (this.tokAction, this.radiusData, this.strFormat, null, this.tickInfo, this.mustBeConnected, this.mustNotBeConnected, this.intramolecular, true), (isDelete ? 12291 : 1060866));
}, $fz.isPrivate = true, $fz), "~N,J.modelset.Measurement,~B,~B,~B");
$_M(c$, "find", 
($fz = function (m) {
return J.modelset.Measurement.find (this.measurements, m);
}, $fz.isPrivate = true, $fz), "J.modelset.Measurement");
$_M(c$, "setIndices", 
($fz = function () {
for (var i = 0; i < this.measurementCount; i++) this.measurements.get (i).index = i;

}, $fz.isPrivate = true, $fz));
$_M(c$, "define", 
($fz = function (md, tokAction) {
this.tokAction = tokAction;
md.define (this, this.modelSet);
}, $fz.isPrivate = true, $fz), "J.modelset.MeasurementData,~N");
Clazz.overrideMethod (c$, "processNextMeasure", 
function (m) {
var iThis = this.find (m);
if (iThis >= 0) {
if (this.tokAction == 12291) {
this.deleteI (iThis);
} else if (this.strFormat != null) {
this.measurements.get (iThis).formatMeasurementAs (this.strFormat, null, true);
} else {
this.measurements.get (iThis).isHidden = (this.tokAction == 1048588);
}} else if (this.tokAction == 1060866 || this.tokAction == 269484114) {
m.tickInfo = (this.tickInfo == null ? this.defaultTickInfo : this.tickInfo);
this.defineMeasurement (-1, m, true);
}}, "J.modelset.Measurement");
$_M(c$, "defineMeasurement", 
($fz = function (i, m, doSelect) {
var value = m.getMeasurement ();
if (this.radiusData != null && !m.isInRange (this.radiusData, value)) return;
if (i == -2147483648) i = this.find (m);
if (i >= 0) {
this.measurements.get (i).isHidden = false;
if (doSelect) this.bsSelected.set (i);
return;
}var measureNew =  new J.modelset.Measurement ().setM (this.modelSet, m, value, (m.colix == 0 ? this.colix : m.colix), this.strFormat, this.measurementCount);
this.measurements.addLast (measureNew);
this.viewer.setStatusMeasuring ("measureCompleted", this.measurementCount++, measureNew.toVector (false).toString (), measureNew.getValue ());
}, $fz.isPrivate = true, $fz), "~N,J.modelset.Measurement,~B");
$_M(c$, "deleteI", 
($fz = function (i) {
var msg = this.measurements.get (i).toVector (true).toString ();
this.measurements.remove (i);
this.measurementCount--;
this.viewer.setStatusMeasuring ("measureDeleted", i, msg, 0);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "pending", 
($fz = function (measurementPending) {
this.measurementPending = measurementPending;
if (measurementPending == null) return;
if (measurementPending.getCount () > 1) this.viewer.setStatusMeasuring ("measurePending", measurementPending.getCount (), measurementPending.toVector (false).toString (), measurementPending.getValue ());
}, $fz.isPrivate = true, $fz), "J.modelset.MeasurementPending");
$_M(c$, "reformatDistances", 
($fz = function () {
for (var i = this.measurementCount; --i >= 0; ) this.measurements.get (i).reformatDistanceIfSelected ();

}, $fz.isPrivate = true, $fz));
$_M(c$, "getAllInfo", 
($fz = function () {
var info =  new J.util.JmolList ();
for (var i = 0; i < this.measurementCount; i++) {
info.addLast (this.getInfo (i));
}
return info;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getAllInfoAsString", 
($fz = function () {
var info = "Measurement Information";
for (var i = 0; i < this.measurementCount; i++) {
info += "\n" + this.getInfoAsString (i);
}
return info;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getInfo", 
($fz = function (index) {
var m = this.measurements.get (index);
var count = m.getCount ();
var info =  new java.util.Hashtable ();
info.put ("index", Integer.$valueOf (index));
info.put ("type", (count == 2 ? "distance" : count == 3 ? "angle" : "dihedral"));
info.put ("strMeasurement", m.getString ());
info.put ("count", Integer.$valueOf (count));
info.put ("value", Float.$valueOf (m.getValue ()));
var tickInfo = m.tickInfo;
if (tickInfo != null) {
info.put ("ticks", tickInfo.ticks);
if (tickInfo.scale != null) info.put ("tickScale", tickInfo.scale);
if (tickInfo.tickLabelFormats != null) info.put ("tickLabelFormats", tickInfo.tickLabelFormats);
if (!Float.isNaN (tickInfo.first)) info.put ("tickStart", Float.$valueOf (tickInfo.first));
}var atomsInfo =  new J.util.JmolList ();
for (var i = 1; i <= count; i++) {
var atomInfo =  new java.util.Hashtable ();
var atomIndex = m.getAtomIndex (i);
atomInfo.put ("_ipt", Integer.$valueOf (atomIndex));
atomInfo.put ("coord", J.util.Escape.eP (m.getAtom (i)));
atomInfo.put ("atomno", Integer.$valueOf (atomIndex < 0 ? -1 : this.atoms[atomIndex].getAtomNumber ()));
atomInfo.put ("info", (atomIndex < 0 ? "<point>" : this.atoms[atomIndex].getInfo ()));
atomsInfo.addLast (atomInfo);
}
info.put ("atoms", atomsInfo);
return info;
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "getInfoAsString", 
function (index) {
return this.measurements.get (index).getInfoAsString (null);
}, "~N");
$_M(c$, "setVisibilityInfo", 
function () {
var bsModels = this.viewer.getVisibleFramesBitSet ();
out : for (var i = this.measurementCount; --i >= 0; ) {
var m = this.measurements.get (i);
m.isVisible = false;
if (this.mad == 0 || m.isHidden) continue;
for (var iAtom = m.getCount (); iAtom > 0; iAtom--) {
var atomIndex = m.getAtomIndex (iAtom);
if (atomIndex >= 0) {
if (!this.modelSet.atoms[atomIndex].isClickable ()) continue out;
} else {
var modelIndex = m.getAtom (iAtom).modelIndex;
if (modelIndex >= 0 && !bsModels.get (modelIndex)) continue out;
}}
m.isVisible = true;
}
});
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return this.viewer.getMeasurementState (this, this.measurements, this.measurementCount, this.font3d, this.defaultTickInfo);
});
});
