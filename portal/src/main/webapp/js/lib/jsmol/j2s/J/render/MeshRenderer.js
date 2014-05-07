Clazz.declarePackage ("J.render");
Clazz.load (["J.render.ShapeRenderer", "J.util.BS", "$.P3", "$.P3i"], "J.render.MeshRenderer", ["J.util.BSUtil", "$.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.mesh = null;
this.vertices = null;
this.normixes = null;
this.screens = null;
this.transformedVectors = null;
this.vertexCount = 0;
this.imageFontScaling = 0;
this.scalePixelsPerMicron = 0;
this.diameter = 0;
this.width = 0;
this.isTranslucent = false;
this.frontOnly = false;
this.antialias = false;
this.haveBsDisplay = false;
this.selectedPolyOnly = false;
this.isGhostPass = false;
this.thePlane = null;
this.latticeOffset = null;
this.pt1f = null;
this.pt2f = null;
this.pt1i = null;
this.pt2i = null;
this.pt3i = null;
this.exportPass = 0;
this.needTranslucent = false;
this.doRender = false;
this.volumeRender = false;
this.bsPolygons = null;
this.isTranslucentInherit = false;
this.renderLow = false;
this.bsPolygonsToExport = null;
Clazz.instantialize (this, arguments);
}, J.render, "MeshRenderer", J.render.ShapeRenderer);
Clazz.prepareFields (c$, function () {
this.latticeOffset =  new J.util.P3 ();
this.pt1f =  new J.util.P3 ();
this.pt2f =  new J.util.P3 ();
this.pt1i =  new J.util.P3i ();
this.pt2i =  new J.util.P3i ();
this.pt3i =  new J.util.P3i ();
this.bsPolygonsToExport =  new J.util.BS ();
});
Clazz.overrideMethod (c$, "render", 
function () {
this.needTranslucent = false;
this.antialias = this.g3d.isAntialiased ();
var mc = this.shape;
for (var i = mc.meshCount; --i >= 0; ) this.renderMesh (mc.meshes[i]);

return this.needTranslucent;
});
$_M(c$, "renderMesh", 
function (mesh) {
return this.renderMesh2 (mesh);
}, "J.shape.Mesh");
$_M(c$, "renderMesh2", 
function (mesh) {
this.mesh = mesh;
if (!this.setVariables ()) return false;
if (!this.doRender) return mesh.title != null;
this.latticeOffset.set (0, 0, 0);
for (var i = this.vertexCount; --i >= 0; ) if (this.vertices[i] != null) this.viewer.transformPtScr (this.vertices[i], this.screens[i]);

if (mesh.lattice == null || mesh.modelIndex < 0) {
this.render2 (this.isExport);
} else {
var unitcell = mesh.unitCell;
if (unitcell == null) unitcell = this.viewer.getModelUnitCell (mesh.modelIndex);
if (unitcell == null) unitcell = mesh.getUnitCell ();
if (unitcell != null) {
var vTemp =  new J.util.P3 ();
var minXYZ =  new J.util.P3i ();
var maxXYZ = J.util.P3i.new3 (Clazz.floatToInt (mesh.lattice.x), Clazz.floatToInt (mesh.lattice.y), Clazz.floatToInt (mesh.lattice.z));
unitcell.setMinMaxLatticeParameters (minXYZ, maxXYZ);
for (var tx = minXYZ.x; tx < maxXYZ.x; tx++) for (var ty = minXYZ.y; ty < maxXYZ.y; ty++) for (var tz = minXYZ.z; tz < maxXYZ.z; tz++) {
this.latticeOffset.set (tx, ty, tz);
unitcell.toCartesian (this.latticeOffset, false);
for (var i = this.vertexCount; --i >= 0; ) {
vTemp.setT (this.vertices[i]);
vTemp.add (this.latticeOffset);
this.viewer.transformPtScr (vTemp, this.screens[i]);
}
this.render2 (this.isExport);
}


}}if (this.screens != null) this.viewer.freeTempScreens (this.screens);
return true;
}, "J.shape.Mesh");
$_M(c$, "setVariables", 
($fz = function () {
if (this.mesh.visibilityFlags == 0) return false;
if (this.mesh.bsSlabGhost != null) this.g3d.setColix (this.mesh.slabColix);
this.isGhostPass = (this.mesh.bsSlabGhost != null && (this.isExport ? this.exportPass == 2 : this.g3d.isPass2 ()));
this.isTranslucentInherit = (this.isGhostPass && J.util.C.getColixTranslucent3 (this.mesh.slabColix, false, 0) == 1);
this.isTranslucent = this.isGhostPass || J.util.C.isColixTranslucent (this.mesh.colix);
if (this.isTranslucent || this.volumeRender || this.mesh.bsSlabGhost != null) this.needTranslucent = true;
this.doRender = (this.setColix (this.mesh.colix) || this.mesh.showContourLines);
if (!this.doRender || this.isGhostPass && !(this.doRender = this.g3d.setColix (this.mesh.slabColix))) {
this.vertices = this.mesh.vertices;
return true;
}this.vertices = (this.mesh.scale3d == 0 && this.mesh.mat4 == null ? this.mesh.vertices : this.mesh.getOffsetVertices (this.thePlane));
if (this.mesh.lineData == null) {
if ((this.vertexCount = this.mesh.vertexCount) == 0) return false;
this.normixes = this.mesh.normixes;
if (this.normixes == null || this.vertices == null) return false;
this.haveBsDisplay = (this.mesh.bsDisplay != null);
this.selectedPolyOnly = (this.isGhostPass || this.mesh.bsSlabDisplay != null);
this.bsPolygons = (this.isGhostPass ? this.mesh.bsSlabGhost : this.selectedPolyOnly ? this.mesh.bsSlabDisplay : null);
this.renderLow = (!this.isExport && !this.viewer.checkMotionRendering (1073742018));
this.frontOnly = this.renderLow || !this.viewer.getSlabEnabled () && this.mesh.frontOnly && !this.mesh.isTwoSided && !this.selectedPolyOnly;
this.screens = this.viewer.allocTempScreens (this.vertexCount);
if (this.frontOnly) this.transformedVectors = this.g3d.getTransformedVertexVectors ();
if (this.transformedVectors == null) this.frontOnly = false;
}return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "setColix", 
function (colix) {
if (this.isGhostPass) return true;
if (this.volumeRender && !this.isTranslucent) colix = J.util.C.getColixTranslucent3 (colix, true, 0.8);
this.colix = colix;
if (J.util.C.isColixLastAvailable (colix)) this.g3d.setColor (this.mesh.color);
return this.g3d.setColix (colix);
}, "~N");
$_M(c$, "isPolygonDisplayable", 
function (i) {
return true;
}, "~N");
$_M(c$, "render2", 
function (generateSet) {
this.render2b (generateSet);
}, "~B");
$_M(c$, "render2b", 
function (generateSet) {
if (!this.g3d.setColix (this.isGhostPass ? this.mesh.slabColix : this.colix)) return;
if (this.renderLow || this.mesh.showPoints || this.mesh.polygonCount == 0) this.renderPoints ();
if (!this.renderLow && (this.isGhostPass ? this.mesh.slabMeshType == 1073742018 : this.mesh.drawTriangles)) this.renderTriangles (false, this.mesh.showTriangles, false);
if (!this.renderLow && (this.isGhostPass ? this.mesh.slabMeshType == 1073741938 : this.mesh.fillTriangles)) this.renderTriangles (true, this.mesh.showTriangles, generateSet);
}, "~B");
$_M(c$, "renderPoints", 
function () {
if (this.mesh.isTriangleSet) {
var polygonIndexes = this.mesh.polygonIndexes;
var bsPoints = J.util.BSUtil.newBitSet (this.mesh.vertexCount);
if (this.haveBsDisplay) {
bsPoints.setBits (0, this.mesh.vertexCount);
bsPoints.andNot (this.mesh.bsDisplay);
}for (var i = this.mesh.polygonCount; --i >= 0; ) {
if (!this.isPolygonDisplayable (i)) continue;
var p = polygonIndexes[i];
if (this.frontOnly && this.transformedVectors[this.normixes[i]].z < 0) continue;
for (var j = p.length - 1; --j >= 0; ) {
var pt = p[j];
if (bsPoints.get (pt)) continue;
bsPoints.set (pt);
if (this.renderLow) {
var s = this.screens[pt];
this.g3d.drawPixel (s.x, s.y, s.z);
} else {
this.g3d.fillSphereI (4, this.screens[pt]);
}}
}
return;
}for (var i = this.vertexCount; --i >= 0; ) if (!this.frontOnly || this.transformedVectors[this.normixes[i]].z >= 0) this.g3d.fillSphereI (4, this.screens[i]);

});
$_M(c$, "renderTriangles", 
function (fill, iShowTriangles, generateSet) {
var polygonIndexes = this.mesh.polygonIndexes;
this.colix = (this.isGhostPass ? this.mesh.slabColix : this.mesh.colix);
if (this.isTranslucentInherit) this.colix = J.util.C.copyColixTranslucency (this.mesh.slabColix, this.mesh.colix);
this.g3d.setColix (this.colix);
if (generateSet) {
if (this.frontOnly && fill) this.frontOnly = false;
this.bsPolygonsToExport.clearAll ();
}for (var i = this.mesh.polygonCount; --i >= 0; ) {
if (!this.isPolygonDisplayable (i)) continue;
var vertexIndexes = polygonIndexes[i];
var iA = vertexIndexes[0];
var iB = vertexIndexes[1];
var iC = vertexIndexes[2];
if (this.haveBsDisplay && (!this.mesh.bsDisplay.get (iA) || !this.mesh.bsDisplay.get (iB) || !this.mesh.bsDisplay.get (iC))) continue;
if (iB == iC) {
this.drawLine (iA, iB, fill, this.vertices[iA], this.vertices[iB], this.screens[iA], this.screens[iB]);
continue;
}var check;
if (this.mesh.isTriangleSet) {
var normix = this.normixes[i];
if (!this.g3d.isDirectedTowardsCamera (normix)) continue;
if (fill) {
if (iShowTriangles) {
this.g3d.fillTriangle (this.screens[iA], this.colix, normix, this.screens[iB], this.colix, normix, this.screens[iC], this.colix, normix, 0.1);
} else {
this.g3d.fillTriangle3CN (this.screens[iA], this.colix, normix, this.screens[iB], this.colix, normix, this.screens[iC], this.colix, normix);
}continue;
}check = vertexIndexes[3];
if (iShowTriangles) check = 7;
if ((check & 1) == 1) this.drawLine (iA, iB, true, this.vertices[iA], this.vertices[iB], this.screens[iA], this.screens[iB]);
if ((check & 2) == 2) this.drawLine (iB, iC, true, this.vertices[iB], this.vertices[iC], this.screens[iB], this.screens[iC]);
if ((check & 4) == 4) this.drawLine (iA, iC, true, this.vertices[iA], this.vertices[iC], this.screens[iA], this.screens[iC]);
continue;
}var nA = this.normixes[iA];
var nB = this.normixes[iB];
var nC = this.normixes[iC];
check = this.checkNormals (nA, nB, nC);
if (fill && check != 7) continue;
switch (vertexIndexes.length) {
case 3:
if (fill) {
if (generateSet) {
this.bsPolygonsToExport.set (i);
continue;
}if (iShowTriangles) {
this.g3d.fillTriangle (this.screens[iA], this.colix, nA, this.screens[iB], this.colix, nB, this.screens[iC], this.colix, nC, 0.1);
continue;
}this.g3d.fillTriangle3CN (this.screens[iA], this.colix, nA, this.screens[iB], this.colix, nB, this.screens[iC], this.colix, nC);
continue;
}this.drawTriangle (this.screens[iA], this.colix, this.screens[iB], this.colix, this.screens[iC], this.colix, check, 1);
continue;
case 4:
var iD = vertexIndexes[3];
var nD = this.normixes[iD];
if (this.frontOnly && (check != 7 || this.transformedVectors[nD].z < 0)) continue;
if (fill) {
if (generateSet) {
this.bsPolygonsToExport.set (i);
continue;
}this.g3d.fillQuadrilateral3i (this.screens[iA], this.colix, nA, this.screens[iB], this.colix, nB, this.screens[iC], this.colix, nC, this.screens[iD], this.colix, nD);
continue;
}this.g3d.drawQuadrilateral (this.colix, this.screens[iA], this.screens[iB], this.screens[iC], this.screens[iD]);
}
}
if (generateSet) this.exportSurface (this.colix);
}, "~B,~B,~B");
$_M(c$, "drawTriangle", 
function (screenA, colixA, screenB, colixB, screenC, colixC, check, diam) {
if (!this.antialias && diam == 1) {
this.g3d.drawTriangle3C (screenA, colixA, screenB, colixB, screenC, colixC, check);
return;
}if (this.antialias) diam <<= 1;
if ((check & 1) == 1) this.g3d.fillCylinderXYZ (colixA, colixB, 1, diam, screenA.x, screenA.y, screenA.z, screenB.x, screenB.y, screenB.z);
if ((check & 2) == 2) this.g3d.fillCylinderXYZ (colixB, colixC, 1, diam, screenB.x, screenB.y, screenB.z, screenC.x, screenC.y, screenC.z);
if ((check & 4) == 4) this.g3d.fillCylinderXYZ (colixA, colixC, 1, diam, screenA.x, screenA.y, screenA.z, screenC.x, screenC.y, screenC.z);
}, "J.util.P3i,~N,J.util.P3i,~N,J.util.P3i,~N,~N,~N");
$_M(c$, "checkNormals", 
function (nA, nB, nC) {
var check = 7;
if (this.frontOnly) {
if (this.transformedVectors[nA].z < 0) check ^= 1;
if (this.transformedVectors[nB].z < 0) check ^= 2;
if (this.transformedVectors[nC].z < 0) check ^= 4;
}return check;
}, "~N,~N,~N");
$_M(c$, "drawLine", 
function (iA, iB, fill, vA, vB, sA, sB) {
var endCap = (iA != iB && !fill ? 0 : this.width < 0 || this.width == -0.0 || iA != iB && this.isTranslucent ? 2 : 3);
if (this.width == 0) {
if (this.diameter == 0) this.diameter = (this.mesh.diameter > 0 ? this.mesh.diameter : iA == iB ? 7 : 3);
if (this.exportType == 1) {
this.pt1f.setT (vA);
this.pt1f.add (vB);
this.pt1f.scale (0.5);
this.viewer.transformPtScr (this.pt1f, this.pt1i);
this.diameter = Clazz.doubleToInt (Math.floor (this.viewer.unscaleToScreen (this.pt1i.z, this.diameter) * 1000));
}if (iA == iB) {
this.g3d.fillSphereI (this.diameter, sA);
} else {
this.g3d.fillCylinder (endCap, this.diameter, sA, sB);
}} else {
this.pt1f.setT (vA);
this.pt1f.add (vB);
this.pt1f.scale (0.5);
this.viewer.transformPtScr (this.pt1f, this.pt1i);
var mad = Clazz.doubleToInt (Math.floor (Math.abs (this.width) * 1000));
this.diameter = Clazz.floatToInt (this.exportType == 1 ? mad : this.viewer.scaleToScreen (this.pt1i.z, mad));
if (this.diameter == 0) this.diameter = 1;
this.viewer.transformPt3f (vA, this.pt1f);
this.viewer.transformPt3f (vB, this.pt2f);
this.g3d.fillCylinderBits (endCap, this.diameter, this.pt1f, this.pt2f);
}}, "~N,~N,~B,J.util.P3,J.util.P3,J.util.P3i,J.util.P3i");
$_M(c$, "exportSurface", 
function (colix) {
this.mesh.normals = this.mesh.getNormals (this.vertices, null);
this.mesh.bsPolygons = this.bsPolygonsToExport;
this.mesh.offset = this.latticeOffset;
this.g3d.drawSurface (this.mesh, colix);
this.mesh.normals = null;
this.mesh.bsPolygons = null;
}, "~N");
});
