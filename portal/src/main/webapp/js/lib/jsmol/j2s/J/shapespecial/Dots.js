Clazz.declarePackage ("J.shapespecial");
Clazz.load (["J.shape.AtomShape", "J.atomdata.RadiusData", "J.util.BS"], "J.shapespecial.Dots", ["java.util.Hashtable", "J.constant.EnumVdw", "J.geodesic.EnvelopeCalculation", "J.util.BSUtil", "$.C", "$.Escape", "$.Matrix3f", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ec = null;
this.isSurface = false;
this.bsOn = null;
this.bsSelected = null;
this.bsIgnore = null;
this.thisAtom = 0;
this.thisRadius = 0;
this.thisArgb = 0;
this.rdLast = null;
Clazz.instantialize (this, arguments);
}, J.shapespecial, "Dots", J.shape.AtomShape);
Clazz.prepareFields (c$, function () {
this.bsOn =  new J.util.BS ();
this.rdLast =  new J.atomdata.RadiusData (null, 0, null, null);
});
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapespecial.Dots, "initShape", []);
this.translucentAllowed = false;
this.ec =  new J.geodesic.EnvelopeCalculation (this.viewer, this.atomCount, this.mads);
});
Clazz.overrideMethod (c$, "getSize", 
function (atomIndex) {
return (this.mads != null ? this.mads[atomIndex] * 2 : this.bsOn.get (atomIndex) ? Clazz.doubleToInt (Math.floor (this.ec.getRadius (atomIndex) * 2000)) : 0);
}, "~N");
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if ("init" === propertyName) {
this.initialize ();
return;
}if ("translucency" === propertyName) {
if (!this.translucentAllowed) return;
}if ("ignore" === propertyName) {
this.bsIgnore = value;
return;
}if ("select" === propertyName) {
this.bsSelected = value;
return;
}if ("radius" === propertyName) {
this.thisRadius = (value).floatValue ();
if (this.thisRadius > 16) this.thisRadius = 16;
return;
}if ("colorRGB" === propertyName) {
this.thisArgb = (value).intValue ();
return;
}if ("atom" === propertyName) {
this.thisAtom = (value).intValue ();
if (this.thisAtom >= this.atoms.length) return;
this.atoms[this.thisAtom].setShapeVisibility (this.myVisibilityFlag, true);
this.ec.allocDotsConvexMaps (this.atomCount);
return;
}if ("dots" === propertyName) {
if (this.thisAtom >= this.atoms.length) return;
this.isActive = true;
this.ec.setFromBits (this.thisAtom, value);
this.atoms[this.thisAtom].setShapeVisibility (this.myVisibilityFlag, true);
if (this.mads == null) {
this.ec.setMads (null);
this.mads =  Clazz.newShortArray (this.atomCount, 0);
for (var i = 0; i < this.atomCount; i++) if (this.atoms[i].isInFrame () && this.atoms[i].isShapeVisible (this.myVisibilityFlag)) try {
this.mads[i] = Clazz.floatToShort (this.ec.getAppropriateRadius (i) * 1000);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}

this.ec.setMads (this.mads);
}this.mads[this.thisAtom] = Clazz.floatToShort (this.thisRadius * 1000);
if (this.colixes == null) {
this.colixes =  Clazz.newShortArray (this.atomCount, 0);
this.paletteIDs =  Clazz.newByteArray (this.atomCount, 0);
}this.colixes[this.thisAtom] = J.util.C.getColix (this.thisArgb);
this.bsOn.set (this.thisAtom);
return;
}if ("refreshTrajectories" === propertyName) {
bs = (value)[1];
var m4 = (value)[2];
if (m4 == null) return;
var m =  new J.util.Matrix3f ();
m4.getRotationScale (m);
this.ec.reCalculate (bs, m);
return;
}if (propertyName === "deleteModelAtoms") {
var firstAtomDeleted = ((value)[2])[1];
var nAtomsDeleted = ((value)[2])[2];
J.util.BSUtil.deleteBits (this.bsOn, bs);
this.ec.deleteAtoms (firstAtomDeleted, nAtomsDeleted);
}this.setPropAS (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "initialize", 
function () {
this.bsSelected = null;
this.bsIgnore = null;
this.isActive = false;
if (this.ec == null) this.ec =  new J.geodesic.EnvelopeCalculation (this.viewer, this.atomCount, this.mads);
});
Clazz.overrideMethod (c$, "setSizeRD", 
function (rd, bsSelected) {
if (rd == null) rd =  new J.atomdata.RadiusData (null, 0, J.atomdata.RadiusData.EnumType.ABSOLUTE, null);
if (this.bsSelected != null) bsSelected = this.bsSelected;
var isVisible = true;
var setRadius = 3.4028235E38;
this.isActive = true;
switch (rd.factorType) {
case J.atomdata.RadiusData.EnumType.OFFSET:
break;
case J.atomdata.RadiusData.EnumType.ABSOLUTE:
if (rd.value == 0) isVisible = false;
setRadius = rd.value;
default:
rd.valueExtended = this.viewer.getCurrentSolventProbeRadius ();
}
var maxRadius;
switch (rd.vdwType) {
case J.constant.EnumVdw.ADPMIN:
case J.constant.EnumVdw.ADPMAX:
case J.constant.EnumVdw.HYDRO:
case J.constant.EnumVdw.TEMP:
maxRadius = setRadius;
break;
case J.constant.EnumVdw.IONIC:
maxRadius = this.modelSet.getMaxVanderwaalsRadius () * 2;
break;
default:
maxRadius = this.modelSet.getMaxVanderwaalsRadius ();
}
var newSet = (this.rdLast.value != rd.value || this.rdLast.valueExtended != rd.valueExtended || this.rdLast.factorType !== rd.factorType || this.rdLast.vdwType !== rd.vdwType || this.ec.getDotsConvexMax () == 0);
if (isVisible) {
for (var i = bsSelected.nextSetBit (0); i >= 0; i = bsSelected.nextSetBit (i + 1)) if (!this.bsOn.get (i)) {
this.bsOn.set (i);
newSet = true;
}
} else {
var isAll = (bsSelected == null);
var i0 = (isAll ? this.atomCount - 1 : bsSelected.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsSelected.nextSetBit (i + 1))) this.bsOn.setBitTo (i, false);

}for (var i = this.atomCount; --i >= 0; ) {
this.atoms[i].setShapeVisibility (this.myVisibilityFlag, this.bsOn.get (i));
}
if (!isVisible) return;
if (newSet) {
this.mads = null;
this.ec.newSet ();
}var dotsConvexMaps = this.ec.getDotsConvexMaps ();
if (dotsConvexMaps != null) {
for (var i = this.atomCount; --i >= 0; ) if (this.bsOn.get (i)) {
dotsConvexMaps[i] = null;
}
}if (dotsConvexMaps == null && (this.colixes == null || this.colixes.length != this.atomCount)) {
this.colixes =  Clazz.newShortArray (this.atomCount, 0);
this.paletteIDs =  Clazz.newByteArray (this.atomCount, 0);
}this.ec.calculate (rd, maxRadius, this.bsOn, this.bsIgnore, !this.viewer.getBoolean (603979830), this.viewer.getBoolean (603979829), this.isSurface, true);
this.rdLast = rd;
}, "J.atomdata.RadiusData,J.util.BS");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
if ((atom.getShapeVisibilityFlags () & this.myVisibilityFlag) == 0 || this.modelSet.isAtomHidden (i)) continue;
atom.setClickable (this.myVisibilityFlag);
}
});
Clazz.overrideMethod (c$, "getShapeState", 
function () {
var dotsConvexMaps = this.ec.getDotsConvexMaps ();
if (dotsConvexMaps == null || this.ec.getDotsConvexMax () == 0) return "";
var sc = this.viewer.getStateCreator ();
if (sc == null) return "";
var s =  new J.util.SB ();
var temp =  new java.util.Hashtable ();
var atomCount = this.viewer.getAtomCount ();
var type = (this.isSurface ? "geoSurface " : "dots ");
for (var i = 0; i < atomCount; i++) {
if (!this.bsOn.get (i) || dotsConvexMaps[i] == null) continue;
if (this.bsColixSet != null && this.bsColixSet.get (i)) J.util.BSUtil.setMapBitSet (temp, i, i, J.shape.Shape.getColorCommand (type, this.paletteIDs[i], this.colixes[i], this.translucentAllowed));
var bs = dotsConvexMaps[i];
if (!bs.isEmpty ()) {
var r = this.ec.getAppropriateRadius (i);
J.shape.Shape.appendCmd (s, type + i + " radius " + r + " " + J.util.Escape.eBS (bs));
}}
return s.append (sc.getCommands (temp, null, "select")).toString ();
});
Clazz.defineStatics (c$,
"SURFACE_DISTANCE_FOR_CALCULATION", 10,
"MAX_LEVEL", 3);
});
