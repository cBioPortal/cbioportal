Clazz.declarePackage ("J.exportjs");
Clazz.load (["J.api.JmolRendererInterface", "J.util.P3", "$.P3i"], "J.exportjs.Export3D", ["J.g3d.HermiteRenderer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.exporter = null;
this.privateKey = 0;
this.g3d = null;
this.colix = 0;
this.hermite3d = null;
this.width = 0;
this.height = 0;
this.slab = 0;
this.exportName = null;
this.ptA = null;
this.ptB = null;
this.ptC = null;
this.ptD = null;
this.ptAi = null;
this.ptBi = null;
Clazz.instantialize (this, arguments);
}, J.exportjs, "Export3D", null, J.api.JmolRendererInterface);
Clazz.prepareFields (c$, function () {
this.ptA =  new J.util.P3 ();
this.ptB =  new J.util.P3 ();
this.ptC =  new J.util.P3 ();
this.ptD =  new J.util.P3 ();
this.ptAi =  new J.util.P3i ();
this.ptBi =  new J.util.P3i ();
});
Clazz.makeConstructor (c$, 
function () {
this.hermite3d =  new J.g3d.HermiteRenderer (this);
});
Clazz.overrideMethod (c$, "getExportType", 
function () {
return this.exporter.exportType;
});
Clazz.overrideMethod (c$, "getExportName", 
function () {
return this.exportName;
});
Clazz.overrideMethod (c$, "initializeExporter", 
function (type, viewer, privateKey, gdata, output) {
this.exportName = type;
try {
var name = "J.exportjs." + type + "Exporter";
var exporterClass = Class.forName (name);
this.exporter = exporterClass.newInstance ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
this.g3d = gdata;
this.exporter.setRenderer (this);
this.g3d.setNewWindowParametersForExport ();
this.slab = this.g3d.getSlab ();
this.width = this.g3d.getRenderWidth ();
this.height = this.g3d.getRenderHeight ();
this.privateKey = privateKey;
return (this.exporter.initializeOutput (viewer, privateKey, this.g3d, output) ? this.exporter : null);
}, "~S,J.viewer.Viewer,~N,J.util.GData,~O");
Clazz.overrideMethod (c$, "initializeOutput", 
function (type, viewer, privateKey, gdata, output) {
return this.exporter.initializeOutput (viewer, privateKey, this.g3d, output);
}, "~S,J.viewer.Viewer,~N,J.util.GData,~O");
Clazz.overrideMethod (c$, "finalizeOutput", 
function () {
return this.exporter.finalizeOutput ();
});
Clazz.overrideMethod (c$, "setSlab", 
function (slabValue) {
this.slab = slabValue;
this.g3d.setSlab (slabValue);
}, "~N");
Clazz.overrideMethod (c$, "setDepth", 
function (depthValue) {
this.g3d.setDepth (depthValue);
}, "~N");
Clazz.overrideMethod (c$, "renderBackground", 
function (me) {
if (this.exporter.exportType == 2) this.g3d.renderBackground (me);
}, "J.api.JmolRendererInterface");
Clazz.overrideMethod (c$, "drawAtom", 
function (atom) {
this.exporter.drawAtom (atom);
}, "J.modelset.Atom");
Clazz.overrideMethod (c$, "drawFilledCircle", 
function (colixRing, colixFill, diameter, x, y, z) {
if (this.isClippedZ (z)) return;
this.exporter.drawFilledCircle (colixRing, colixFill, diameter, x, y, z);
}, "~N,~N,~N,~N,~N,~N");
$_M(c$, "drawCircle", 
function (colix, diameter, x, y, z, doFill) {
if (this.isClippedZ (z)) return;
this.exporter.drawCircle (x, y, z, diameter, colix, doFill);
}, "~N,~N,~N,~N,~N,~B");
Clazz.overrideMethod (c$, "fillSphereXYZ", 
function (diameter, x, y, z) {
this.ptA.set (x, y, z);
this.fillSphere (diameter, this.ptA);
}, "~N,~N,~N,~N");
Clazz.overrideMethod (c$, "fillSphereI", 
function (diameter, center) {
this.ptA.set (center.x, center.y, center.z);
this.fillSphere (diameter, this.ptA);
}, "~N,J.util.P3i");
Clazz.overrideMethod (c$, "fillSphere", 
function (diameter, center) {
if (diameter == 0) return;
this.exporter.fillSphere (this.colix, diameter, center);
}, "~N,J.util.P3");
Clazz.overrideMethod (c$, "drawRect", 
function (x, y, z, zSlab, rWidth, rHeight) {
if (zSlab != 0 && this.isClippedZ (zSlab)) return;
var w = rWidth - 1;
var h = rHeight - 1;
var xRight = x + w;
var yBottom = y + h;
if (y >= 0 && y < this.height) this.drawHLine (x, y, z, w);
if (yBottom >= 0 && yBottom < this.height) this.drawHLine (x, yBottom, z, w);
if (x >= 0 && x < this.width) this.drawVLine (x, y, z, h);
if (xRight >= 0 && xRight < this.width) this.drawVLine (xRight, y, z, h);
}, "~N,~N,~N,~N,~N,~N");
$_M(c$, "drawHLine", 
($fz = function (x, y, z, w) {
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
$_M(c$, "drawVLine", 
($fz = function (x, y, z, h) {
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
Clazz.overrideMethod (c$, "fillRect", 
function (x, y, z, zSlab, widthFill, heightFill) {
if (this.isClippedZ (zSlab)) return;
this.ptA.set (x, y, z);
this.ptB.set (x + widthFill, y, z);
this.ptC.set (x + widthFill, y + heightFill, z);
this.ptD.set (x, y + heightFill, z);
this.fillQuadrilateral (this.ptA, this.ptB, this.ptC, this.ptD);
}, "~N,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "drawString", 
function (str, font3d, xBaseline, yBaseline, z, zSlab, bgcolix) {
if (str == null) return;
if (this.isClippedZ (zSlab)) return;
this.drawStringNoSlab (str, font3d, xBaseline, yBaseline, z, bgcolix);
}, "~S,J.util.JmolFont,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "drawStringNoSlab", 
function (str, font3d, xBaseline, yBaseline, z, bgColix) {
if (str == null) return;
z = Math.max (this.slab, z);
if (font3d == null) font3d = this.g3d.getFont3DCurrent ();
 else this.g3d.setFont (font3d);
this.exporter.plotText (xBaseline, yBaseline, z, this.colix, str, font3d);
}, "~S,J.util.JmolFont,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "drawImage", 
function (objImage, x, y, z, zSlab, bgcolix, width, height) {
if (objImage == null || width == 0 || height == 0) return;
if (this.isClippedZ (zSlab)) return;
z = Math.max (this.slab, z);
this.exporter.plotImage (x, y, z, objImage, bgcolix, width, height);
}, "~O,~N,~N,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "drawPixel", 
function (x, y, z) {
this.plotPixelClipped (x, y, z);
}, "~N,~N,~N");
$_M(c$, "plotPixelClipped", 
function (x, y, z) {
if (this.isClipped (x, y, z)) return;
this.exporter.drawPixel (this.colix, x, y, z, 1);
}, "~N,~N,~N");
Clazz.overrideMethod (c$, "plotImagePixel", 
function (argb, x, y, z, shade, bgargb) {
}, "~N,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "plotPixelClippedP3i", 
function (screen) {
if (this.isClipped (screen.x, screen.y, screen.z)) return;
this.exporter.drawPixel (this.colix, screen.x, screen.y, screen.z, 1);
}, "J.util.P3i");
Clazz.overrideMethod (c$, "drawPoints", 
function (count, coordinates, scale) {
for (var i = count * 3; i > 0; ) {
var z = coordinates[--i];
var y = coordinates[--i];
var x = coordinates[--i];
if (this.isClipped (x, y, z)) continue;
this.exporter.drawPixel (this.colix, x, y, z, scale);
}
}, "~N,~A,~N");
Clazz.overrideMethod (c$, "drawDashedLine", 
function (run, rise, pointA, pointB) {
this.drawLineAB (pointA, pointB);
}, "~N,~N,J.util.P3i,J.util.P3i");
Clazz.overrideMethod (c$, "drawDottedLine", 
function (pointA, pointB) {
this.drawLineAB (pointA, pointB);
}, "J.util.P3i,J.util.P3i");
Clazz.overrideMethod (c$, "drawLineXYZ", 
function (x1, y1, z1, x2, y2, z2) {
this.ptAi.set (x1, y1, z1);
this.ptBi.set (x2, y2, z2);
this.drawLineAB (this.ptAi, this.ptBi);
}, "~N,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "drawLine", 
function (colixA, colixB, xA, yA, zA, xB, yB, zB) {
this.fillCylinderXYZ (colixA, colixB, 2, this.exporter.lineWidthMad, xA, yA, zA, xB, yB, zB);
}, "~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "drawLineAB", 
function (pointA, pointB) {
this.ptA.set (pointA.x, pointA.y, pointA.z);
this.ptB.set (pointB.x, pointB.y, pointB.z);
this.exporter.fillCylinderScreenMad (this.colix, 2, this.exporter.lineWidthMad, this.ptA, this.ptB);
}, "J.util.P3i,J.util.P3i");
Clazz.overrideMethod (c$, "drawBond", 
function (atomA, atomB, colixA, colixB, endcaps, mad, bondOrder) {
if (mad == 1) mad = this.exporter.lineWidthMad;
this.exporter.drawCylinder (atomA, atomB, colixA, colixB, endcaps, mad, bondOrder);
}, "J.util.P3,J.util.P3,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "fillCylinderXYZ", 
function (colixA, colixB, endcaps, mad, xA, yA, zA, xB, yB, zB) {
this.ptA.set (xA, yA, zA);
this.ptB.set (xB, yB, zB);
this.exporter.drawCylinder (this.ptA, this.ptB, colixA, colixB, endcaps, mad, 1);
}, "~N,~N,~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "fillCylinderScreen", 
function (endcaps, screenDiameter, xA, yA, zA, xB, yB, zB) {
this.ptA.set (xA, yA, zA);
this.ptB.set (xB, yB, zB);
this.exporter.fillCylinderScreen (this.colix, endcaps, screenDiameter, this.ptA, this.ptB, null, null, 0);
}, "~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "fillCylinderScreen3I", 
function (endcaps, diameter, pointA, pointB, pt0f, pt1f, radius) {
if (diameter <= 0) return;
this.ptA.set (pointA.x, pointA.y, pointA.z);
this.ptB.set (pointB.x, pointB.y, pointB.z);
this.exporter.fillCylinderScreen (this.colix, endcaps, diameter, this.ptA, this.ptB, pt0f, pt1f, radius);
}, "~N,~N,J.util.P3i,J.util.P3i,J.util.P3,J.util.P3,~N");
Clazz.overrideMethod (c$, "fillCylinder", 
function (endcaps, diameter, pointA, pointB) {
if (diameter <= 0) return;
this.ptA.set (pointA.x, pointA.y, pointA.z);
this.ptB.set (pointB.x, pointB.y, pointB.z);
this.exporter.fillCylinderScreenMad (this.colix, endcaps, diameter, this.ptA, this.ptB);
}, "~N,~N,J.util.P3i,J.util.P3i");
Clazz.overrideMethod (c$, "fillCylinderBits", 
function (endcaps, diameter, pointA, pointB) {
if (diameter <= 0) return;
this.exporter.fillCylinderScreenMad (this.colix, endcaps, diameter, pointA, pointB);
}, "~N,~N,J.util.P3,J.util.P3");
Clazz.overrideMethod (c$, "fillConeScreen", 
function (endcap, screenDiameter, pointBase, screenTip, isBarb) {
this.ptA.set (pointBase.x, pointBase.y, pointBase.z);
this.ptB.set (screenTip.x, screenTip.y, screenTip.z);
this.exporter.fillConeScreen (this.colix, endcap, screenDiameter, this.ptA, this.ptB, isBarb);
}, "~N,~N,J.util.P3i,J.util.P3i,~B");
Clazz.overrideMethod (c$, "fillConeSceen3f", 
function (endcap, screenDiameter, pointBase, screenTip) {
this.exporter.fillConeScreen (this.colix, endcap, screenDiameter, pointBase, screenTip, false);
}, "~N,~N,J.util.P3,J.util.P3");
Clazz.overrideMethod (c$, "drawHermite4", 
function (tension, s0, s1, s2, s3) {
this.hermite3d.renderHermiteRope (false, tension, 0, 0, 0, s0, s1, s2, s3);
}, "~N,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i");
Clazz.overrideMethod (c$, "fillHermite", 
function (tension, diameterBeg, diameterMid, diameterEnd, s0, s1, s2, s3) {
this.hermite3d.renderHermiteRope (true, tension, diameterBeg, diameterMid, diameterEnd, s0, s1, s2, s3);
}, "~N,~N,~N,~N,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i");
Clazz.overrideMethod (c$, "drawHermite7", 
function (fill, border, tension, s0, s1, s2, s3, s4, s5, s6, s7, aspectRatio, colixBack) {
this.hermite3d.renderHermiteRibbon (fill, border, tension, s0, s1, s2, s3, s4, s5, s6, s7, aspectRatio, 0);
}, "~B,~B,~N,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i,~N,~N");
Clazz.overrideMethod (c$, "drawTriangle3C", 
function (screenA, colixA, screenB, colixB, screenC, colixC, check) {
if ((check & 1) == 1) this.drawLine (colixA, colixB, screenA.x, screenA.y, screenA.z, screenB.x, screenB.y, screenB.z);
if ((check & 2) == 2) this.drawLine (colixB, colixC, screenB.x, screenB.y, screenB.z, screenC.x, screenC.y, screenC.z);
if ((check & 4) == 4) this.drawLine (colixA, colixC, screenA.x, screenA.y, screenA.z, screenC.x, screenC.y, screenC.z);
}, "J.util.P3i,~N,J.util.P3i,~N,J.util.P3i,~N,~N");
Clazz.overrideMethod (c$, "drawTriangle3I", 
function (screenA, screenB, screenC, check) {
if ((check & 1) == 1) this.drawLine (this.colix, this.colix, screenA.x, screenA.y, screenA.z, screenB.x, screenB.y, screenB.z);
if ((check & 2) == 2) this.drawLine (this.colix, this.colix, screenB.x, screenB.y, screenB.z, screenC.x, screenC.y, screenC.z);
if ((check & 4) == 4) this.drawLine (this.colix, this.colix, screenA.x, screenA.y, screenA.z, screenC.x, screenC.y, screenC.z);
}, "J.util.P3i,J.util.P3i,J.util.P3i,~N");
Clazz.overrideMethod (c$, "fillTriangle3CN", 
function (pointA, colixA, normixA, pointB, colixB, normixB, pointC, colixC, normixC) {
if (colixA != colixB || colixB != colixC) {
return;
}this.ptA.set (pointA.x, pointA.y, pointA.z);
this.ptB.set (pointB.x, pointB.y, pointB.z);
this.ptC.set (pointC.x, pointC.y, pointC.z);
this.exporter.fillTriangle (colixA, this.ptA, this.ptB, this.ptC, false, false);
}, "J.util.P3i,~N,~N,J.util.P3i,~N,~N,J.util.P3i,~N,~N");
Clazz.overrideMethod (c$, "fillTriangleTwoSided", 
function (normix, xpointA, ypointA, zpointA, xpointB, ypointB, zpointB, xpointC, ypointC, zpointC) {
this.ptA.set (xpointA, ypointA, zpointA);
this.ptB.set (xpointB, ypointB, zpointB);
this.ptC.set (xpointC, ypointC, zpointC);
this.exporter.fillTriangle (this.colix, this.ptA, this.ptB, this.ptC, true, false);
}, "~N,~N,~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "fillTriangle3f", 
function (pointA, pointB, pointC, setNoisy) {
this.exporter.fillTriangle (this.colix, pointA, pointB, pointC, false, false);
}, "J.util.P3,J.util.P3,J.util.P3,~B");
Clazz.overrideMethod (c$, "fillTriangle3i", 
function (pointA, pointB, pointC, ptA, ptB, ptC) {
this.exporter.fillTriangle (this.colix, ptA, ptB, ptC, true, true);
}, "J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3,J.util.P3,J.util.P3");
Clazz.overrideMethod (c$, "fillTriangle", 
function (pointA, colixA, normixA, pointB, colixB, normixB, pointC, colixC, normixC, factor) {
this.fillTriangle3CN (pointA, colixA, normixA, pointB, colixB, normixB, pointC, colixC, normixC);
}, "J.util.P3i,~N,~N,J.util.P3i,~N,~N,J.util.P3i,~N,~N,~N");
Clazz.overrideMethod (c$, "drawQuadrilateral", 
function (colix, pointA, pointB, pointC, screenD) {
this.setColix (colix);
this.drawLineAB (pointA, pointB);
this.drawLineAB (pointB, pointC);
this.drawLineAB (pointC, screenD);
this.drawLineAB (screenD, pointA);
}, "~N,J.util.P3i,J.util.P3i,J.util.P3i,J.util.P3i");
Clazz.overrideMethod (c$, "fillQuadrilateral", 
function (pointA, pointB, pointC, pointD) {
this.exporter.fillTriangle (this.colix, pointA, pointB, pointC, false, false);
this.exporter.fillTriangle (this.colix, pointA, pointC, pointD, false, false);
}, "J.util.P3,J.util.P3,J.util.P3,J.util.P3");
Clazz.overrideMethod (c$, "fillQuadrilateral3i", 
function (pointA, colixA, normixA, pointB, colixB, normixB, pointC, colixC, normixC, screenD, colixD, normixD) {
this.fillTriangle3CN (pointA, colixA, normixA, pointB, colixB, normixB, pointC, colixC, normixC);
this.fillTriangle3CN (pointA, colixA, normixA, pointC, colixC, normixC, screenD, colixD, normixD);
}, "J.util.P3i,~N,~N,J.util.P3i,~N,~N,J.util.P3i,~N,~N,J.util.P3i,~N,~N");
Clazz.overrideMethod (c$, "drawSurface", 
function (meshSurface, colix) {
this.exporter.drawSurface (meshSurface, colix);
}, "J.util.MeshSurface,~N");
Clazz.overrideMethod (c$, "getBgColixes", 
function (bgcolixes) {
return this.exporter.exportType == 1 ? null : bgcolixes;
}, "~A");
Clazz.overrideMethod (c$, "fillEllipsoid", 
function (center, points, x, y, z, diameter, mToEllipsoidal, coef, mDeriv, selectedOctant, octantPoints) {
this.exporter.fillEllipsoid (center, points, this.colix, x, y, z, diameter, mToEllipsoidal, coef, mDeriv, octantPoints);
}, "J.util.P3,~A,~N,~N,~N,~N,J.util.Matrix3f,~A,J.util.Matrix4f,~N,~A");
Clazz.overrideMethod (c$, "drawEllipse", 
function (ptAtom, ptX, ptY, fillArc, wireframeOnly) {
return this.exporter.drawEllipse (ptAtom, ptX, ptY, this.colix, fillArc);
}, "J.util.P3,J.util.P3,J.util.P3,~B,~B");
Clazz.overrideMethod (c$, "getGData", 
function () {
return this.g3d;
});
Clazz.overrideMethod (c$, "isAntialiased", 
function () {
return false;
});
Clazz.overrideMethod (c$, "checkTranslucent", 
function (isAlphaTranslucent) {
return true;
}, "~B");
Clazz.overrideMethod (c$, "haveTranslucentObjects", 
function () {
return true;
});
Clazz.overrideMethod (c$, "setColor", 
function (color) {
this.g3d.setColor (color);
}, "~N");
Clazz.overrideMethod (c$, "getRenderWidth", 
function () {
return this.g3d.getRenderWidth ();
});
Clazz.overrideMethod (c$, "getRenderHeight", 
function () {
return this.g3d.getRenderHeight ();
});
Clazz.overrideMethod (c$, "isPass2", 
function () {
return this.g3d.isPass2 ();
});
Clazz.overrideMethod (c$, "getSlab", 
function () {
return this.g3d.getSlab ();
});
Clazz.overrideMethod (c$, "getDepth", 
function () {
return this.g3d.getDepth ();
});
Clazz.overrideMethod (c$, "setColix", 
function (colix) {
this.colix = colix;
this.g3d.setColix (colix);
return true;
}, "~N");
Clazz.overrideMethod (c$, "setFontFid", 
function (fid) {
this.g3d.setFontFid (fid);
}, "~N");
Clazz.overrideMethod (c$, "getFont3DCurrent", 
function () {
return this.g3d.getFont3DCurrent ();
});
Clazz.overrideMethod (c$, "isInDisplayRange", 
function (x, y) {
if (this.exporter.exportType == 1) return true;
return this.g3d.isInDisplayRange (x, y);
}, "~N,~N");
Clazz.overrideMethod (c$, "isClippedZ", 
function (z) {
return this.g3d.isClippedZ (z);
}, "~N");
$_M(c$, "clipCode", 
function (x, y, z) {
return (this.exporter.exportType == 1 ? this.g3d.clipCode (z) : this.g3d.clipCode3 (x, y, z));
}, "~N,~N,~N");
Clazz.overrideMethod (c$, "isClippedXY", 
function (diameter, x, y) {
if (this.exporter.exportType == 1) return false;
return this.g3d.isClippedXY (diameter, x, y);
}, "~N,~N,~N");
$_M(c$, "isClipped", 
function (x, y, z) {
return (this.g3d.isClippedZ (z) || this.isClipped (x, y));
}, "~N,~N,~N");
$_M(c$, "isClipped", 
function (x, y) {
if (this.exporter.exportType == 1) return false;
return this.g3d.isClipped (x, y);
}, "~N,~N");
Clazz.overrideMethod (c$, "getColorArgbOrGray", 
function (colix) {
return this.g3d.getColorArgbOrGray (colix);
}, "~N");
Clazz.overrideMethod (c$, "setNoisySurfaceShade", 
function (pointA, pointB, pointC) {
this.g3d.setNoisySurfaceShade (pointA, pointB, pointC);
}, "J.util.P3i,J.util.P3i,J.util.P3i");
Clazz.overrideMethod (c$, "getFontFidFS", 
function (fontFace, fontSize) {
return this.g3d.getFontFidFS (fontFace, fontSize);
}, "~S,~N");
Clazz.overrideMethod (c$, "isDirectedTowardsCamera", 
function (normix) {
return this.g3d.isDirectedTowardsCamera (normix);
}, "~N");
Clazz.overrideMethod (c$, "getTransformedVertexVectors", 
function () {
return this.g3d.getTransformedVertexVectors ();
});
Clazz.overrideMethod (c$, "getFont3DScaled", 
function (font, scale) {
return this.g3d.getFont3DScaled (font, scale);
}, "J.util.JmolFont,~N");
Clazz.overrideMethod (c$, "getFontFid", 
function (fontSize) {
return this.g3d.getFontFid (fontSize);
}, "~N");
Clazz.overrideMethod (c$, "setTranslucentCoverOnly", 
function (TF) {
}, "~B");
$_M(c$, "getPrivateKey", 
function () {
return this.privateKey;
});
Clazz.overrideMethod (c$, "volumeRender4", 
function (diam, x, y, z) {
this.fillSphereXYZ (diam, x, y, z);
}, "~N,~N,~N,~N");
Clazz.overrideMethod (c$, "currentlyRendering", 
function () {
return false;
});
Clazz.overrideMethod (c$, "renderCrossHairs", 
function (minMax, screenWidth, screenHeight, navigationOffset, navigationDepthPercent) {
}, "~A,~N,~N,J.util.P3,~N");
Clazz.overrideMethod (c$, "volumeRender", 
function (TF) {
}, "~B");
Clazz.overrideMethod (c$, "renderAllStrings", 
function (jmolRenderer) {
}, "~O");
Clazz.overrideMethod (c$, "getTranslucentCoverOnly", 
function () {
return this.g3d.getTranslucentCoverOnly ();
});
});
