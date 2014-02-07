Clazz.declarePackage ("J.modelset");
Clazz.load (null, "J.modelset.Measurement", ["java.lang.Float", "J.atomdata.RadiusData", "J.constant.EnumVdw", "J.modelset.LabelToken", "J.util.AxisAngle4f", "$.Escape", "$.JmolList", "$.Measure", "$.P3", "$.SB", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.thisID = null;
this.modelSet = null;
this.index = 0;
this.isVisible = true;
this.isHidden = false;
this.isDynamic = false;
this.isTrajectory = false;
this.$isValid = true;
this.colix = 0;
this.labelColix = -1;
this.mad = 0;
this.tickInfo = null;
this.traceX = -2147483648;
this.traceY = 0;
this.count = 0;
this.countPlusIndices = null;
this.pts = null;
this.value = 0;
this.strFormat = null;
this.viewer = null;
this.strMeasurement = null;
this.aa = null;
this.pointArc = null;
this.text = null;
this.type = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "Measurement");
Clazz.prepareFields (c$, function () {
this.countPlusIndices =  Clazz.newIntArray (5, 0);
});
$_M(c$, "setM", 
function (modelSet, m, value, colix, strFormat, index) {
this.modelSet = modelSet;
this.index = index;
this.viewer = modelSet.viewer;
this.colix = colix;
this.strFormat = strFormat;
if (m != null) {
this.tickInfo = m.tickInfo;
this.pts = m.pts;
this.mad = m.mad;
this.thisID = m.thisID;
this.text = m.text;
if (this.thisID != null && this.text != null) this.labelColix = this.text.colix;
}if (this.pts == null) this.pts =  new Array (4);
var indices = (m == null ? null : m.countPlusIndices);
this.count = (indices == null ? 0 : indices[0]);
if (this.count > 0) {
System.arraycopy (indices, 0, this.countPlusIndices, 0, this.count + 1);
this.isTrajectory = modelSet.isTrajectoryMeasurement (this.countPlusIndices);
}this.value = (Float.isNaN (value) || this.isTrajectory ? this.getMeasurement () : value);
this.formatMeasurement (null);
return this;
}, "J.modelset.ModelSet,J.modelset.Measurement,~N,~N,~S,~N");
$_M(c$, "setPoints", 
function (modelSet, indices, points, tickInfo) {
this.modelSet = modelSet;
this.countPlusIndices = indices;
this.count = indices[0];
this.pts = (points == null ?  new Array (4) : points);
this.viewer = modelSet.viewer;
this.tickInfo = tickInfo;
return this;
}, "J.modelset.ModelSet,~A,~A,J.modelset.TickInfo");
$_M(c$, "getCount", 
function () {
return this.count;
});
$_M(c$, "setCount", 
function (count) {
this.setCountM (count);
}, "~N");
$_M(c$, "setCountM", 
function (count) {
this.count = this.countPlusIndices[0] = count;
}, "~N");
$_M(c$, "getCountPlusIndices", 
function () {
return this.countPlusIndices;
});
$_M(c$, "getPoints", 
function () {
return this.pts;
});
$_M(c$, "getAtomIndex", 
function (n) {
return (n > 0 && n <= this.count ? this.countPlusIndices[n] : -1);
}, "~N");
$_M(c$, "getAtom", 
function (i) {
var pt = this.countPlusIndices[i];
return (pt < -1 ? this.pts[-2 - pt] : this.modelSet.atoms[pt]);
}, "~N");
$_M(c$, "getLastIndex", 
function () {
return (this.count > 0 ? this.countPlusIndices[this.count] : -1);
});
$_M(c$, "getString", 
function () {
return this.strMeasurement;
});
$_M(c$, "getStringUsing", 
function (viewer, strFormat, units) {
this.viewer = viewer;
this.value = this.getMeasurement ();
this.formatMeasurementAs (strFormat, units, true);
if (strFormat == null) return this.getInfoAsString (units);
return this.strMeasurement;
}, "J.viewer.Viewer,~S,~S");
$_M(c$, "getStringDetail", 
function () {
return (this.count == 2 ? "Distance" : this.count == 3 ? "Angle" : "Torsion") + this.getMeasurementScript (" - ", false) + " : " + this.value;
});
$_M(c$, "getStrFormat", 
function () {
return this.strFormat;
});
$_M(c$, "getValue", 
function () {
return this.value;
});
$_M(c$, "getAxisAngle", 
function () {
return this.aa;
});
$_M(c$, "getPointArc", 
function () {
return this.pointArc;
});
$_M(c$, "refresh", 
function () {
this.value = this.getMeasurement ();
this.isTrajectory = this.modelSet.isTrajectoryMeasurement (this.countPlusIndices);
this.formatMeasurement (null);
});
$_M(c$, "getMeasurementScript", 
function (sep, withModelIndex) {
var str = "";
var asScript = (sep.equals (" "));
for (var i = 1; i <= this.count; i++) str += (i > 1 ? sep : " ") + this.getLabel (i, asScript, withModelIndex);

return str;
}, "~S,~B");
$_M(c$, "formatMeasurementAs", 
function (strFormat, units, useDefault) {
if (strFormat != null && strFormat.length == 0) strFormat = null;
if (!useDefault && strFormat != null && strFormat.indexOf (this.countPlusIndices[0] + ":") != 0) return;
this.strFormat = strFormat;
this.formatMeasurement (units);
}, "~S,~S,~B");
$_M(c$, "formatMeasurement", 
function (units) {
this.strMeasurement = null;
if (Float.isNaN (this.value) || this.count == 0) return;
switch (this.count) {
case 2:
this.strMeasurement = this.formatDistance (units);
return;
case 3:
if (this.value == 180) {
this.aa = null;
this.pointArc = null;
} else {
var vectorBA =  new J.util.V3 ();
var vectorBC =  new J.util.V3 ();
var radians = J.util.Measure.computeAngle (this.getAtom (1), this.getAtom (2), this.getAtom (3), vectorBA, vectorBC, false);
var vectorAxis =  new J.util.V3 ();
vectorAxis.cross (vectorBA, vectorBC);
this.aa = J.util.AxisAngle4f.new4 (vectorAxis.x, vectorAxis.y, vectorAxis.z, radians);
vectorBA.normalize ();
vectorBA.scale (0.5);
this.pointArc = J.util.P3.newP (vectorBA);
}case 4:
this.strMeasurement = this.formatAngle (this.value);
return;
}
}, "~S");
$_M(c$, "reformatDistanceIfSelected", 
function () {
if (this.count != 2) return;
if (this.viewer.isSelected (this.countPlusIndices[1]) && this.viewer.isSelected (this.countPlusIndices[2])) this.formatMeasurement (null);
});
$_M(c$, "formatDistance", 
($fz = function (units) {
var label = this.getLabelString ();
if (label == null) return "";
if (units == null) {
var pt = this.strFormat.indexOf ("//");
units = (pt >= 0 ? this.strFormat.substring (pt + 2) : null);
if (units == null) {
units = this.viewer.getMeasureDistanceUnits ();
this.strFormat += "//" + units;
}}units = J.modelset.Measurement.fixUnits (units);
var pt = label.indexOf ("//");
if (pt >= 0) {
label = label.substring (0, pt);
if (label.length == 0) label = "%VALUE";
}var f = this.fixValue (units, (label.indexOf ("%V") >= 0));
return this.formatString (f, units, label);
}, $fz.isPrivate = true, $fz), "~S");
c$.fixUnits = $_M(c$, "fixUnits", 
($fz = function (units) {
if (units.equals ("nanometers")) return "nm";
 else if (units.equals ("picometers")) return "pm";
 else if (units.equals ("angstroms")) return "\u00C5";
 else if (units.equals ("vanderwaals") || units.equals ("vdw")) return "%";
return units;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "fixValue", 
function (units, andRound) {
if (this.count != 2) return this.value;
var dist = this.value;
if (units != null) {
var isPercent = units.equals ("%");
if (isPercent || units.endsWith ("hz")) {
var i1 = this.getAtomIndex (1);
var i2 = this.getAtomIndex (2);
if (i1 >= 0 && i2 >= 0) {
var a1 = this.getAtom (1);
var a2 = this.getAtom (2);
var isDC = (!isPercent && J.modelset.Measurement.nmrType (units) == 1);
this.type = (isPercent ? "percent" : isDC ? "dipoleCouplingConstant" : "J-CouplingConstant");
dist = (isPercent ? dist / (a1.getVanderwaalsRadiusFloat (this.viewer, J.constant.EnumVdw.AUTO) + a2.getVanderwaalsRadiusFloat (this.viewer, J.constant.EnumVdw.AUTO)) : isDC ? this.viewer.getNMRCalculation ().getDipolarConstantHz (a1, a2) : this.viewer.getNMRCalculation ().getIsoOrAnisoHz (true, a1, a2, units, null));
this.$isValid = !Float.isNaN (dist);
if (isPercent) units = "pm";
}}if (units.equals ("nm")) return (andRound ? Math.round (dist * 100) / 1000 : dist / 10);
if (units.equals ("pm")) return (andRound ? Math.round (dist * 1000) / 10 : dist * 100);
if (units.equals ("au")) return (andRound ? Math.round (dist / 0.5291772 * 1000) / 1000 : dist / 0.5291772);
if (units.endsWith ("khz")) return (andRound ? Math.round (dist / 10) / 100 : dist / 1000);
}return (andRound ? Math.round (dist * 100) / 100 : dist);
}, "~S,~B");
c$.nmrType = $_M(c$, "nmrType", 
function (units) {
return (units.indexOf ("hz") < 0 ? 0 : units.startsWith ("dc_") || units.equals ("khz") ? 1 : 2);
}, "~S");
$_M(c$, "formatAngle", 
($fz = function (angle) {
var label = this.getLabelString ();
if (label.indexOf ("%V") >= 0) angle = Math.round (angle * 10) / 10;
return this.formatString (angle, "\u00B0", label);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getLabelString", 
($fz = function () {
var s = this.countPlusIndices[0] + ":";
var label = null;
if (this.strFormat != null) {
if (this.strFormat.length == 0) return null;
label = (this.strFormat.length > 2 && this.strFormat.indexOf (s) == 0 ? this.strFormat : null);
}if (label == null) {
this.strFormat = null;
label = this.viewer.getDefaultMeasurementLabel (this.countPlusIndices[0]);
}if (label.indexOf (s) == 0) label = label.substring (2);
if (this.strFormat == null) this.strFormat = s + label;
return label;
}, $fz.isPrivate = true, $fz));
$_M(c$, "formatString", 
($fz = function (value, units, label) {
return J.modelset.LabelToken.formatLabelMeasure (this.viewer, this, label, value, units);
}, $fz.isPrivate = true, $fz), "~N,~S,~S");
$_M(c$, "sameAsPoints", 
function (indices, points) {
if (this.count != indices[0]) return false;
var isSame = true;
for (var i = 1; i <= this.count && isSame; i++) isSame = (this.countPlusIndices[i] == indices[i]);

if (isSame) for (var i = 0; i < this.count && isSame; i++) {
if (points[i] != null) isSame = (this.pts[i].distance (points[i]) < 0.01);
}
if (isSame) return true;
switch (this.count) {
default:
return true;
case 2:
return this.sameAsIJ (indices, points, 1, 2) && this.sameAsIJ (indices, points, 2, 1);
case 3:
return this.sameAsIJ (indices, points, 1, 3) && this.sameAsIJ (indices, points, 2, 2) && this.sameAsIJ (indices, points, 3, 1);
case 4:
return this.sameAsIJ (indices, points, 1, 4) && this.sameAsIJ (indices, points, 2, 3) && this.sameAsIJ (indices, points, 3, 2) && this.sameAsIJ (indices, points, 4, 1);
}
}, "~A,~A");
$_M(c$, "sameAsIJ", 
($fz = function (atoms, points, i, j) {
var ipt = this.countPlusIndices[i];
var jpt = atoms[j];
return (ipt >= 0 || jpt >= 0 ? ipt == jpt : this.pts[-2 - ipt].distance (points[-2 - jpt]) < 0.01);
}, $fz.isPrivate = true, $fz), "~A,~A,~N,~N");
$_M(c$, "sameAs", 
function (i, j) {
return this.sameAsIJ (this.countPlusIndices, this.pts, i, j);
}, "~N,~N");
$_M(c$, "toVector", 
function (asBitSet) {
var V =  new J.util.JmolList ();
for (var i = 1; i <= this.count; i++) V.addLast (this.getLabel (i, asBitSet, false));

V.addLast (this.strMeasurement);
return V;
}, "~B");
$_M(c$, "getMeasurement", 
function () {
if (this.countPlusIndices == null) return NaN;
if (this.count < 2) return NaN;
for (var i = this.count; --i >= 0; ) if (this.countPlusIndices[i + 1] == -1) {
return NaN;
}
var ptA = this.getAtom (1);
var ptB = this.getAtom (2);
var ptC;
var ptD;
switch (this.count) {
case 2:
return ptA.distance (ptB);
case 3:
ptC = this.getAtom (3);
return J.util.Measure.computeAngleABC (ptA, ptB, ptC, true);
case 4:
ptC = this.getAtom (3);
ptD = this.getAtom (4);
return J.util.Measure.computeTorsion (ptA, ptB, ptC, ptD, true);
default:
return NaN;
}
});
$_M(c$, "getLabel", 
function (i, asBitSet, withModelIndex) {
var atomIndex = this.countPlusIndices[i];
return (atomIndex < 0 ? (withModelIndex ? "modelIndex " + this.getAtom (i).modelIndex + " " : "") + J.util.Escape.eP (this.getAtom (i)) : asBitSet ? "(({" + atomIndex + "}))" : this.viewer.getAtomInfo (atomIndex));
}, "~N,~B,~B");
$_M(c$, "setModelIndex", 
function (modelIndex) {
if (this.pts == null) return;
for (var i = 0; i < this.count; i++) {
if (this.pts[i] != null) this.pts[i].modelIndex = modelIndex;
}
}, "~N");
$_M(c$, "isValid", 
function () {
return !(this.sameAs (1, 2) || this.count > 2 && this.sameAs (1, 3) || this.count == 4 && this.sameAs (2, 4));
});
c$.find = $_M(c$, "find", 
function (measurements, m) {
var indices = m.getCountPlusIndices ();
var points = m.getPoints ();
for (var i = measurements.size (); --i >= 0; ) if (measurements.get (i).sameAsPoints (indices, points)) return i;

return -1;
}, "J.util.JmolList,J.modelset.Measurement");
$_M(c$, "isConnected", 
function (atoms, count) {
var atomIndexLast = -1;
for (var i = 1; i <= count; i++) {
var atomIndex = this.getAtomIndex (i);
if (atomIndex < 0) continue;
if (atomIndexLast >= 0 && !atoms[atomIndex].isBonded (atoms[atomIndexLast])) return false;
atomIndexLast = atomIndex;
}
return true;
}, "~A,~N");
$_M(c$, "getInfoAsString", 
function (units) {
var f = this.fixValue (units, true);
var sb =  new J.util.SB ();
sb.append (this.count == 2 ? (this.type == null ? "distance" : this.type) : this.count == 3 ? "angle" : "dihedral");
sb.append (" \t").appendF (f);
sb.append (" \t").append (J.util.Escape.eS (this.strMeasurement));
for (var i = 1; i <= this.count; i++) sb.append (" \t").append (this.getLabel (i, false, false));

if (this.thisID != null) sb.append (" \t").append (this.thisID);
return sb.toString ();
}, "~S");
$_M(c$, "isInRange", 
function (radiusData, value) {
if (radiusData.factorType === J.atomdata.RadiusData.EnumType.FACTOR) {
var atom1 = this.getAtom (1);
var atom2 = this.getAtom (2);
var d = (atom1.getVanderwaalsRadiusFloat (this.viewer, radiusData.vdwType) + atom2.getVanderwaalsRadiusFloat (this.viewer, radiusData.vdwType)) * radiusData.value;
return (value <= d);
}return (radiusData.values[0] == 3.4028235E38 || value >= radiusData.values[0] && value <= radiusData.values[1]);
}, "J.atomdata.RadiusData,~N");
$_M(c$, "isIntramolecular", 
function (atoms, count) {
var molecule = -1;
for (var i = 1; i <= count; i++) {
var atomIndex = this.getAtomIndex (i);
if (atomIndex < 0) continue;
var m = atoms[atomIndex].getMoleculeNumber (false);
if (molecule < 0) molecule = m;
 else if (m != molecule) return false;
}
return true;
}, "~A,~N");
$_M(c$, "isMin", 
function (htMin) {
var a1 = this.getAtom (1);
var a2 = this.getAtom (2);
var d = Clazz.floatToInt (a2.distanceSquared (a1) * 100);
var n1 = a1.getAtomName ();
var n2 = a2.getAtomName ();
var key = (n1.compareTo (n2) < 0 ? n1 + n2 : n2 + n1);
var min = htMin.get (key);
return (min != null && d == min.intValue ());
}, "java.util.Map");
Clazz.defineStatics (c$,
"NMR_NOT", 0,
"NMR_DC", 1,
"NMR_JC", 2);
});
