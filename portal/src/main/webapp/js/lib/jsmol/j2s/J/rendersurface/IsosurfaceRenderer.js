Clazz.declarePackage ("J.rendersurface");
Clazz.load (["J.render.MeshRenderer", "J.util.P3", "$.P3i"], "J.rendersurface.IsosurfaceRenderer", ["java.lang.Boolean", "$.Float", "J.util.C", "$.Normix", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.iHideBackground = false;
this.isBicolorMap = false;
this.backgroundColix = 0;
this.nError = 0;
this.vertexValues = null;
this.imesh = null;
this.isosurface = null;
this.isNavigationMode = false;
this.iShowNormals = false;
this.showNumbers = false;
this.$showKey = null;
this.hasColorRange = false;
this.meshScale = -1;
this.mySlabValue = 0;
this.globalSlabValue = 0;
this.ptTemp = null;
this.ptTempi = null;
Clazz.instantialize (this, arguments);
}, J.rendersurface, "IsosurfaceRenderer", J.render.MeshRenderer);
Clazz.prepareFields (c$, function () {
this.ptTemp =  new J.util.P3 ();
this.ptTempi =  new J.util.P3i ();
});
Clazz.overrideMethod (c$, "render", 
function () {
return this.renderIso ();
});
$_M(c$, "renderIso", 
function () {
this.setGlobals ();
for (var i = this.isosurface.meshCount; --i >= 0; ) {
this.imesh = this.isosurface.meshes[i];
if (this.imesh.connections != null && !this.viewer.getModelSet ().atoms[this.imesh.connections[0]].isVisible (0)) continue;
this.hasColorRange = false;
if (this.renderMeshSlab ()) {
if (!this.isExport) this.renderInfo ();
if (this.isExport && this.isGhostPass) {
this.exportPass = 1;
this.renderMeshSlab ();
this.exportPass = 2;
}}}
return this.needTranslucent;
});
$_M(c$, "setGlobals", 
($fz = function () {
this.needTranslucent = false;
this.iShowNormals = this.viewer.getTestFlag (4);
this.showNumbers = this.viewer.getTestFlag (3);
this.isosurface = this.shape;
this.exportPass = (this.isExport ? 2 : 0);
this.isNavigationMode = this.viewer.getBoolean (603979887);
this.$showKey = (this.viewer.getBoolean (603979870) ? Boolean.TRUE : null);
this.isosurface.keyXy = null;
this.meshScale = -1;
this.globalSlabValue = this.g3d.getSlab ();
this.mySlabValue = (this.isNavigationMode ? Clazz.floatToInt (this.viewer.getNavigationOffset ().z) : 2147483647);
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderInfo", 
function () {
if (this.hasColorRange && this.imesh.colorEncoder != null && Boolean.TRUE === this.$showKey) this.showKey ();
});
$_M(c$, "showKey", 
($fz = function () {
this.$showKey = Boolean.FALSE;
var colors = null;
var colixes = null;
var vContours = null;
var n = 0;
var type = 0;
if (this.imesh.showContourLines) {
vContours = this.imesh.getContours ();
if (vContours == null) {
colixes = this.imesh.jvxlData.contourColixes;
if (colixes == null) return;
n = colixes.length;
} else {
n = vContours.length;
type = 1;
}} else {
colors = this.imesh.colorEncoder.getColorSchemeArray (this.imesh.colorEncoder.currentPalette);
n = (colors == null ? 0 : colors.length);
type = 2;
}if (n < 2) return;
var factor = (this.g3d.isAntialiased () ? 2 : 1);
var height = this.viewer.getScreenHeight () * factor;
var dy = Clazz.doubleToInt (Clazz.doubleToInt (height / 2) / (n - 1));
var y = Clazz.doubleToInt (height / 4) * 3 - dy;
var x = 10 * factor;
var dx = 20 * factor;
this.isosurface.keyXy = [Clazz.doubleToInt (x / factor), 0, Clazz.doubleToInt ((x + dx) / factor), Clazz.doubleToInt ((y + dy) / factor), Clazz.doubleToInt (dy / factor)];
for (var i = 0; i < n; i++, y -= dy) {
switch (type) {
case 0:
if (!this.g3d.setColix (colixes[i])) return;
break;
case 1:
if (!this.g3d.setColix ((vContours[i].get (3))[0])) return;
break;
case 2:
this.g3d.setColor (colors[i]);
break;
}
this.g3d.fillRect (x, y, 5, -2147483648, dx, dy);
}
this.isosurface.keyXy[1] = Clazz.doubleToInt ((y + dy) / factor);
}, $fz.isPrivate = true, $fz));
$_M(c$, "renderMeshSlab", 
($fz = function () {
this.volumeRender = (this.imesh.jvxlData.colorDensity && this.imesh.jvxlData.allowVolumeRender);
var thisSlabValue = this.mySlabValue;
if (!this.isNavigationMode) {
var meshSlabValue = this.imesh.jvxlData.slabValue;
if (meshSlabValue != -2147483648 && this.imesh.jvxlData.isSlabbable) {
var points = this.imesh.jvxlData.boundingBox;
this.pt2f.setT (points[0]);
this.pt2f.add (points[1]);
this.pt2f.scale (0.5);
this.viewer.transformPt3f (this.pt2f, this.pt2f);
var r = this.viewer.scaleToScreen (Clazz.floatToInt (this.pt2f.z), Math.round (points[0].distance (points[1]) * 500));
thisSlabValue = Math.round (this.pt2f.z + r * (1 - meshSlabValue / 50));
}}var tcover = this.g3d.getTranslucentCoverOnly ();
this.g3d.setTranslucentCoverOnly (this.imesh.frontOnly || !this.viewer.getBoolean (603979967));
this.thePlane = this.imesh.jvxlData.jvxlPlane;
this.vertexValues = this.imesh.vertexValues;
var isOK;
if (thisSlabValue != 2147483647 && this.imesh.jvxlData.isSlabbable) {
this.g3d.setSlab (thisSlabValue);
isOK = this.renderMesh (this.imesh);
this.g3d.setSlab (this.globalSlabValue);
} else {
isOK = this.renderMesh (this.imesh);
}this.g3d.setTranslucentCoverOnly (tcover);
return isOK;
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "render2", 
function (isExport) {
if (this.volumeRender) {
this.renderPoints ();
return;
}switch (this.imesh.dataType) {
case 70:
this.renderLonePair (false);
return;
case 71:
this.renderLonePair (true);
return;
}
this.isBicolorMap = this.imesh.jvxlData.isBicolorMap;
this.render2b (isExport);
if (!this.g3d.setColix (4)) return;
if (this.imesh.showContourLines) this.renderContourLines ();
}, "~B");
$_M(c$, "renderLonePair", 
($fz = function (isRadical) {
this.pt2f.setT (this.vertices[1]);
this.viewer.transformPt3f (this.pt2f, this.pt2f);
var r = Clazz.floatToInt (this.viewer.scaleToScreen (Clazz.floatToInt (this.pt2f.z), 100));
if (r < 1) r = 1;
if (!isRadical) {
var v1 =  new J.util.V3 ();
var v2 =  new J.util.V3 ();
this.pt1f.setT (this.vertices[0]);
this.viewer.transformPt3f (this.pt1f, this.pt1f);
v1.sub2 (this.pt2f, this.pt1f);
v2.set (v1.x, v1.y, v1.z + 1);
v2.cross (v2, v1);
v2.normalize ();
var f = this.viewer.scaleToScreen (Clazz.floatToInt (this.pt1f.z), 100);
v2.scale (f);
this.pt1f.setT (this.pt2f);
this.pt1f.add (v2);
this.pt2f.sub (v2);
this.screens[0].set (Math.round (this.pt1f.x), Math.round (this.pt1f.y), Math.round (this.pt1f.z));
this.g3d.fillSphereI (r, this.screens[0]);
}this.screens[1].set (Math.round (this.pt2f.x), Math.round (this.pt2f.y), Math.round (this.pt2f.z));
this.g3d.fillSphereI (r, this.screens[1]);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "renderContourLines", 
($fz = function () {
var vContours = this.imesh.getContours ();
if (vContours == null) {
if (this.imesh.jvxlData.contourValues != null) this.hasColorRange = true;
return;
}this.hasColorRange = (this.imesh.meshColix == 0);
for (var i = vContours.length; --i >= 0; ) {
var v = vContours[i];
if (v.size () < 6) continue;
this.colix = (this.imesh.meshColix == 0 ? (v.get (3))[0] : this.imesh.meshColix);
if (!this.g3d.setColix (this.colix)) return;
var n = v.size () - 1;
for (var j = 6; j < n; j++) {
var pt1 = v.get (j);
var pt2 = v.get (++j);
this.viewer.transformPtScr (pt1, this.pt1i);
this.viewer.transformPtScr (pt2, this.pt2i);
if (Float.isNaN (pt1.x) || Float.isNaN (pt2.x)) break;
this.pt1i.z -= 2;
this.pt2i.z -= 2;
this.g3d.drawLineAB (this.pt1i, this.pt2i);
}
}
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "renderPoints", 
function () {
try {
if (this.volumeRender) this.g3d.volumeRender (true);
var slabPoints = ((this.volumeRender || this.imesh.polygonCount == 0) && this.selectedPolyOnly);
var incr = this.imesh.vertexIncrement;
var diam;
if (this.imesh.diameter <= 0) {
diam = this.viewer.getInt (553648144);
this.frontOnly = false;
} else {
diam = Clazz.doubleToInt (this.viewer.getScreenDim () / (this.volumeRender ? 50 : 100));
}var ptSize = Math.round (Float.isNaN (this.imesh.volumeRenderPointSize) ? 150 : this.imesh.volumeRenderPointSize * 1000);
if (diam < 1) diam = 1;
var cX = (this.showNumbers ? Clazz.doubleToInt (this.viewer.getScreenWidth () / 2) : 0);
var cY = (this.showNumbers ? Clazz.doubleToInt (this.viewer.getScreenHeight () / 2) : 0);
if (this.showNumbers) this.g3d.setFontFid (this.g3d.getFontFidFS ("Monospaced", 24));
for (var i = (!this.imesh.hasGridPoints || this.imesh.firstRealVertex < 0 ? 0 : this.imesh.firstRealVertex); i < this.vertexCount; i += incr) {
if (this.vertexValues != null && Float.isNaN (this.vertexValues[i]) || this.frontOnly && this.transformedVectors[this.normixes[i]].z < 0 || this.imesh.jvxlData.thisSet >= 0 && this.imesh.vertexSets[i] != this.imesh.jvxlData.thisSet || !this.imesh.isColorSolid && this.imesh.vertexColixes != null && !this.setColix (this.imesh.vertexColixes[i]) || this.haveBsDisplay && !this.imesh.bsDisplay.get (i) || slabPoints && !this.bsPolygons.get (i)) continue;
this.hasColorRange = true;
if (this.showNumbers && this.screens[i].z > 10 && Math.abs (this.screens[i].x - cX) < 150 && Math.abs (this.screens[i].y - cY) < 150) {
var s = i + (this.imesh.isColorSolid ? "" : " " + this.imesh.vertexValues[i]);
this.g3d.setColix (4);
this.g3d.drawStringNoSlab (s, null, this.screens[i].x, this.screens[i].y, this.screens[i].z - 30, 0);
}if (this.volumeRender) {
diam = Clazz.floatToInt (this.viewer.scaleToScreen (this.screens[i].z, ptSize));
if (diam < 1) diam = 1;
this.g3d.volumeRender4 (diam, this.screens[i].x, this.screens[i].y, this.screens[i].z);
} else {
this.g3d.fillSphereI (diam, this.screens[i]);
}}
if (incr == 3) {
this.g3d.setColix (this.isTranslucent ? J.util.C.getColixTranslucent3 (12, true, 0.5) : 12);
for (var i = 1; i < this.vertexCount; i += 3) this.g3d.fillCylinder (3, Clazz.doubleToInt (diam / 4), this.screens[i], this.screens[i + 1]);

this.g3d.setColix (this.isTranslucent ? J.util.C.getColixTranslucent3 (21, true, 0.5) : 21);
for (var i = 1; i < this.vertexCount; i += 3) this.g3d.fillSphereI (diam, this.screens[i]);

this.g3d.setColix (this.isTranslucent ? J.util.C.getColixTranslucent3 (7, true, 0.5) : 7);
for (var i = 2; i < this.vertexCount; i += 3) {
this.g3d.fillSphereI (diam, this.screens[i]);
}
}} catch (e) {
}
if (this.volumeRender) this.g3d.volumeRender (false);
});
Clazz.overrideMethod (c$, "renderTriangles", 
function (fill, iShowTriangles, isExport) {
var polygonIndexes = this.imesh.polygonIndexes;
this.colix = (this.isGhostPass ? this.imesh.slabColix : !fill && this.imesh.meshColix != 0 ? this.imesh.meshColix : this.imesh.colix);
var vertexColixes = (!fill && this.imesh.meshColix != 0 ? null : this.imesh.vertexColixes);
if (this.isTranslucentInherit) this.colix = J.util.C.copyColixTranslucency (this.mesh.slabColix, this.mesh.colix);
this.g3d.setColix (this.colix);
var diam = -2147483648;
var generateSet = isExport;
if (generateSet) {
if (this.frontOnly && fill) this.frontOnly = false;
this.bsPolygonsToExport.clearAll ();
}if (this.exportType == 1) {
this.frontOnly = false;
}var colorSolid = (this.isGhostPass && (!this.isBicolorMap) || vertexColixes == null || this.imesh.isColorSolid);
var noColor = (this.isGhostPass && !this.isBicolorMap || vertexColixes == null || !fill && this.imesh.meshColix != 0);
var isPlane = (this.imesh.jvxlData.jvxlPlane != null);
var colix = this.colix;
if (isPlane && !colorSolid && !fill && this.imesh.fillTriangles) {
colorSolid = true;
colix = 4;
}var colorArrayed = (colorSolid && this.imesh.polygonColixes != null);
if (colorArrayed && !fill && this.imesh.fillTriangles) colorArrayed = false;
var contourColixes = this.imesh.jvxlData.contourColixes;
this.hasColorRange = !colorSolid && !this.isBicolorMap;
for (var i = this.imesh.polygonCount; --i >= 0; ) {
var polygon = polygonIndexes[i];
if (polygon == null || this.selectedPolyOnly && !this.bsPolygons.get (i)) continue;
var iA = polygon[0];
var iB = polygon[1];
var iC = polygon[2];
if (this.imesh.jvxlData.thisSet >= 0 && this.imesh.vertexSets != null && this.imesh.vertexSets[iA] != this.imesh.jvxlData.thisSet) continue;
if (this.haveBsDisplay && (!this.imesh.bsDisplay.get (iA) || !this.imesh.bsDisplay.get (iB) || !this.imesh.bsDisplay.get (iC))) continue;
var nA = this.normixes[iA];
var nB = this.normixes[iB];
var nC = this.normixes[iC];
var check = this.checkNormals (nA, nB, nC);
if (fill && check == 0) continue;
var colixA;
var colixB;
var colixC;
if (colorSolid) {
if (colorArrayed && i < this.imesh.polygonColixes.length) {
var c = this.imesh.polygonColixes[i];
if (c == 0) continue;
colix = c;
}colixA = colixB = colixC = colix;
} else {
colixA = vertexColixes[iA];
colixB = vertexColixes[iB];
colixC = vertexColixes[iC];
if (this.isBicolorMap) {
if (colixA != colixB || colixB != colixC) continue;
if (this.isGhostPass) {
colixA = colixB = colixC = J.util.C.copyColixTranslucency (this.imesh.slabColix, colixA);
}}}if (diam == -2147483648) {
if (this.imesh.diameter <= 0) {
diam = (this.meshScale < 0 ? this.meshScale = this.viewer.getInt (553648151) : this.meshScale);
if (this.g3d.isAntialiased ()) diam *= 2;
} else {
diam = Clazz.doubleToInt (this.viewer.getScreenDim () / 100);
}if (diam < 1) diam = 1;
}if (fill) {
if (generateSet) {
this.bsPolygonsToExport.set (i);
continue;
}if (iB == iC) {
this.setColix (colixA);
if (iA == iB) this.g3d.fillSphereI (diam, this.screens[iA]);
 else this.g3d.fillCylinder (3, diam, this.screens[iA], this.screens[iB]);
} else if (iShowTriangles) {
this.g3d.fillTriangle (this.screens[iA], colixA, nA, this.screens[iB], colixB, nB, this.screens[iC], colixC, nC, 0.1);
} else {
if (this.isTranslucentInherit && vertexColixes != null) {
colixA = J.util.C.copyColixTranslucency (this.mesh.slabColix, vertexColixes[iA]);
colixB = J.util.C.copyColixTranslucency (this.mesh.slabColix, vertexColixes[iB]);
colixC = J.util.C.copyColixTranslucency (this.mesh.slabColix, vertexColixes[iC]);
}this.g3d.fillTriangle3CN (this.screens[iA], colixA, nA, this.screens[iB], colixB, nB, this.screens[iC], colixC, nC);
}if (this.iShowNormals) this.renderNormals ();
} else {
check &= polygon[3];
if (iShowTriangles) check = 7;
if (check == 0) continue;
this.pt1i.setT (this.screens[iA]);
this.pt2i.setT (this.screens[iB]);
this.pt3i.setT (this.screens[iC]);
this.pt1i.z -= 2;
this.pt2i.z -= 2;
this.pt3i.z -= 2;
if (noColor) {
} else if (colorArrayed) {
this.g3d.setColix (this.mesh.fillTriangles ? 4 : contourColixes[polygon[4] % contourColixes.length]);
} else {
this.drawTriangle (this.pt1i, colixA, this.pt2i, colixB, this.pt3i, colixC, check, diam);
continue;
}this.drawTriangle (this.pt1i, colix, this.pt2i, colix, this.pt3i, colix, check, diam);
}}
if (generateSet) this.exportSurface (colorSolid ? colix : 0);
}, "~B,~B,~B");
$_M(c$, "renderNormals", 
($fz = function () {
if (!this.g3d.setColix (8)) return;
this.g3d.setFontFid (this.g3d.getFontFidFS ("Monospaced", 24));
var vertexVectors = J.util.Normix.getVertexVectors ();
for (var i = this.vertexCount; --i >= 0; ) {
if (this.vertexValues != null && Float.isNaN (this.vertexValues[i])) continue;
if (i > 100) continue;
this.ptTemp.setT (this.vertices[i]);
var n = this.mesh.normixes[i];
if (n >= 0) {
this.ptTemp.add (vertexVectors[n]);
this.ptTemp.add (vertexVectors[n]);
this.ptTemp.add (vertexVectors[n]);
this.viewer.transformPtScr (this.ptTemp, this.ptTempi);
this.g3d.drawLineAB (this.screens[i], this.ptTempi);
}}
}, $fz.isPrivate = true, $fz));
});
