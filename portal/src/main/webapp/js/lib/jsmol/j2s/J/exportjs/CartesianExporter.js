Clazz.declarePackage ("J.exportjs");
Clazz.load (["J.exportjs.Exporter", "J.util.AxisAngle4f"], "J.exportjs.CartesianExporter", ["java.lang.Float", "java.util.Hashtable", "J.util.C", "$.P3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewpoint = null;
Clazz.instantialize (this, arguments);
}, J.exportjs, "CartesianExporter", J.exportjs.Exporter);
Clazz.prepareFields (c$, function () {
this.viewpoint =  new J.util.AxisAngle4f ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.exportjs.CartesianExporter, []);
this.exportType = 1;
this.lineWidthMad = 100;
});
$_M(c$, "getModelCenter", 
function () {
return this.referenceCenter;
});
$_M(c$, "getCameraPosition", 
function () {
var ptCamera =  new J.util.P3 ();
var pt = J.util.P3.new3 (Clazz.doubleToInt (this.screenWidth / 2), Clazz.doubleToInt (this.screenHeight / 2), 0);
this.viewer.unTransformPoint (pt, ptCamera);
ptCamera.sub (this.center);
this.tempP3.set (Clazz.doubleToInt (this.screenWidth / 2), Clazz.doubleToInt (this.screenHeight / 2), this.cameraDistance * this.scalePixelsPerAngstrom);
this.viewer.unTransformPoint (this.tempP3, this.tempP3);
this.tempP3.sub (this.center);
ptCamera.add (this.tempP3);
return this.cameraPosition;
});
$_M(c$, "setTempPoints", 
($fz = function (ptA, ptB, isCartesian) {
if (isCartesian) {
this.tempP1.setT (ptA);
this.tempP2.setT (ptB);
} else {
this.viewer.unTransformPoint (ptA, this.tempP1);
this.viewer.unTransformPoint (ptB, this.tempP2);
}}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3,~B");
$_M(c$, "getCoordinateMap", 
function (vertices, coordMap, bsValid) {
var n = 0;
for (var i = 0; i < coordMap.length; i++) {
if (bsValid != null && !bsValid.get (i) || Float.isNaN (vertices[i].x)) {
if (bsValid != null) bsValid.clear (i);
continue;
}coordMap[i] = n++;
}
return n;
}, "~A,~A,J.util.BS");
$_M(c$, "getNormalMap", 
function (normals, nNormals, bsValid, vNormals) {
var htNormals =  new java.util.Hashtable ();
var normalMap =  Clazz.newIntArray (nNormals, 0);
for (var i = 0; i < nNormals; i++) {
var s;
if (bsValid != null && !bsValid.get (i) || Float.isNaN (normals[i].x)) {
if (bsValid != null) bsValid.clear (i);
continue;
}s = this.getTriad (normals[i]) + "\n";
if (htNormals.containsKey (s)) {
normalMap[i] = htNormals.get (s).intValue ();
} else {
normalMap[i] = vNormals.size ();
vNormals.addLast (s);
htNormals.put (s, Integer.$valueOf (normalMap[i]));
}}
return normalMap;
}, "~A,~N,J.util.BS,J.util.JmolList");
$_M(c$, "outputIndices", 
function (indices, map, nPolygons, bsPolygons, faceVertexMax) {
var isAll = (bsPolygons == null);
var i0 = (isAll ? nPolygons - 1 : bsPolygons.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsPolygons.nextSetBit (i + 1))) this.outputFace (indices[i], map, faceVertexMax);

}, "~A,~A,~N,J.util.BS,~N");
$_M(c$, "outputFace", 
function (is, coordMap, faceVertexMax) {
}, "~A,~A,~N");
Clazz.overrideMethod (c$, "drawAtom", 
function (atom) {
var colix = atom.getColix ();
this.outputSphere (atom, atom.madAtom / 2000, colix, J.util.C.isColixTranslucent (colix));
}, "J.modelset.Atom");
Clazz.overrideMethod (c$, "drawCircle", 
function (x, y, z, diameter, colix, doFill) {
this.tempP3.set (x, y, z);
this.viewer.unTransformPoint (this.tempP3, this.tempP1);
var radius = this.viewer.unscaleToScreen (z, diameter) / 2;
this.tempP3.set (x, y, z + 1);
this.viewer.unTransformPoint (this.tempP3, this.tempP3);
this.outputCircle (this.tempP1, this.tempP3, radius, colix, doFill);
}, "~N,~N,~N,~N,~N,~B");
Clazz.overrideMethod (c$, "drawEllipse", 
function (ptCenter, ptX, ptY, colix, doFill) {
this.tempV1.setT (ptX);
this.tempV1.sub (ptCenter);
this.tempV2.setT (ptY);
this.tempV2.sub (ptCenter);
this.tempV2.cross (this.tempV1, this.tempV2);
this.tempV2.normalize ();
this.tempV2.scale (doFill ? 0.002 : 0.005);
this.tempP1.setT (ptCenter);
this.tempP1.sub (this.tempV2);
this.tempP2.setT (ptCenter);
this.tempP2.add (this.tempV2);
return this.outputCylinder (ptCenter, this.tempP1, this.tempP2, colix, doFill ? 2 : 0, 1.01, ptX, ptY, true);
}, "J.util.P3,J.util.P3,J.util.P3,~N,~B");
Clazz.overrideMethod (c$, "drawPixel", 
function (colix, x, y, z, scale) {
this.tempP3.set (x, y, z);
this.viewer.unTransformPoint (this.tempP3, this.tempP1);
this.outputSphere (this.tempP1, 0.02 * scale, colix, true);
}, "~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "fillConeScreen", 
function (colix, endcap, screenDiameter, screenBase, screenTip, isBarb) {
this.viewer.unTransformPoint (screenBase, this.tempP1);
this.viewer.unTransformPoint (screenTip, this.tempP2);
var radius = this.viewer.unscaleToScreen (screenBase.z, screenDiameter) / 2;
if (radius < 0.05) radius = 0.05;
this.outputCone (this.tempP1, this.tempP2, radius, colix);
}, "~N,~N,~N,J.util.P3,J.util.P3,~B");
Clazz.overrideMethod (c$, "drawCylinder", 
function (ptA, ptB, colix1, colix2, endcaps, mad, bondOrder) {
this.setTempPoints (ptA, ptB, bondOrder < 0);
var radius = mad / 2000;
if (colix1 == colix2) {
this.outputCylinder (null, this.tempP1, this.tempP2, colix1, endcaps, radius, null, null, bondOrder != -1);
} else {
this.tempV2.setT (this.tempP2);
this.tempV2.add (this.tempP1);
this.tempV2.scale (0.5);
this.tempP3.setT (this.tempV2);
this.outputCylinder (null, this.tempP1, this.tempP3, colix1, (endcaps == 3 ? 0 : endcaps), radius, null, null, true);
this.outputCylinder (null, this.tempP3, this.tempP2, colix2, (endcaps == 3 ? 0 : endcaps), radius, null, null, true);
if (endcaps == 3) {
this.outputSphere (this.tempP1, radius * 1.01, colix1, bondOrder != -2);
this.outputSphere (this.tempP2, radius * 1.01, colix2, bondOrder != -2);
}}}, "J.util.P3,J.util.P3,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "fillCylinderScreenMad", 
function (colix, endcaps, mad, screenA, screenB) {
var radius = mad / 2000;
this.setTempPoints (screenA, screenB, false);
this.outputCylinder (null, this.tempP1, this.tempP2, colix, endcaps, radius, null, null, true);
}, "~N,~N,~N,J.util.P3,J.util.P3");
Clazz.overrideMethod (c$, "fillCylinderScreen", 
function (colix, endcaps, screenDiameter, screenA, screenB, ptA, ptB, radius) {
if (ptA != null) {
this.drawCylinder (ptA, ptB, colix, colix, endcaps, Math.round (radius * 2000), -1);
return;
}var mad = Math.round (this.viewer.unscaleToScreen ((screenA.z + screenB.z) / 2, screenDiameter) * 1000);
this.fillCylinderScreenMad (colix, endcaps, mad, screenA, screenB);
}, "~N,~N,~N,J.util.P3,J.util.P3,J.util.P3,J.util.P3,~N");
Clazz.overrideMethod (c$, "fillEllipsoid", 
function (center, points, colix, x, y, z, diameter, toEllipsoidal, coef, deriv, octantPoints) {
this.outputEllipsoid (center, points, colix);
}, "J.util.P3,~A,~N,~N,~N,~N,~N,J.util.Matrix3f,~A,J.util.Matrix4f,~A");
Clazz.overrideMethod (c$, "fillSphere", 
function (colix, diameter, pt) {
this.viewer.unTransformPoint (pt, this.tempP1);
this.outputSphere (this.tempP1, this.viewer.unscaleToScreen (pt.z, diameter) / 2, colix, true);
}, "~N,~N,J.util.P3");
Clazz.overrideMethod (c$, "fillTriangle", 
function (colix, ptA, ptB, ptC, twoSided, isCartesian) {
if (isCartesian) {
this.tempP1.setT (ptA);
this.tempP2.setT (ptB);
this.tempP3.setT (ptC);
} else {
this.viewer.unTransformPoint (ptA, this.tempP1);
this.viewer.unTransformPoint (ptB, this.tempP2);
this.viewer.unTransformPoint (ptC, this.tempP3);
}this.outputTriangle (this.tempP1, this.tempP2, this.tempP3, colix);
if (twoSided) this.outputTriangle (this.tempP1, this.tempP3, this.tempP2, colix);
}, "~N,J.util.P3,J.util.P3,J.util.P3,~B,~B");
});
