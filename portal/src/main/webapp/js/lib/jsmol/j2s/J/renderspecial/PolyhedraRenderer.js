Clazz.declarePackage ("J.renderspecial");
Clazz.load (["J.render.ShapeRenderer"], "J.renderspecial.PolyhedraRenderer", ["JU.P3i", "JM.Atom", "JU.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.drawEdges = 0;
this.isAll = false;
this.frontOnly = false;
this.screens = null;
this.vibs = false;
Clazz.instantialize (this, arguments);
}, J.renderspecial, "PolyhedraRenderer", J.render.ShapeRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
var polyhedra = this.shape;
var polyhedrons = polyhedra.polyhedrons;
this.drawEdges = polyhedra.drawEdges;
this.g3d.addRenderer (1073742182);
this.vibs = (this.ms.vibrations != null && this.tm.vibrationOn);
var colixes = polyhedra.colixes;
var needTranslucent = false;
for (var i = polyhedra.polyhedronCount; --i >= 0; ) {
var iAtom = polyhedrons[i].centralAtom.i;
var colix = (colixes == null || iAtom >= colixes.length ? 0 : polyhedra.colixes[iAtom]);
if (this.render1 (polyhedrons[i], colix)) needTranslucent = true;
}
return needTranslucent;
});
Clazz.defineMethod (c$, "render1", 
 function (p, colix) {
if (p.visibilityFlags == 0) return false;
colix = JU.C.getColixInherited (colix, p.centralAtom.getColix ());
var needTranslucent = false;
if (JU.C.isColixTranslucent (colix)) {
needTranslucent = true;
} else if (!this.g3d.setC (colix)) {
return false;
}var vertices = p.vertices;
var planes;
if (this.screens == null || this.screens.length < vertices.length) {
this.screens =  new Array (vertices.length);
for (var i = vertices.length; --i >= 0; ) this.screens[i] =  new JU.P3i ();

}planes = p.planes;
for (var i = vertices.length; --i >= 0; ) {
var atom = (Clazz.instanceOf (vertices[i], JM.Atom) ? vertices[i] : null);
if (atom == null) {
this.tm.transformPtScr (vertices[i], this.screens[i]);
} else if (!atom.isVisible (this.myVisibilityFlag)) {
this.screens[i].setT (this.vibs && atom.hasVibration () ? this.tm.transformPtVib (atom, this.ms.vibrations[atom.i]) : this.tm.transformPt (atom));
} else {
this.screens[i].set (atom.sX, atom.sY, atom.sZ);
}}
this.isAll = (this.drawEdges == 1);
this.frontOnly = (this.drawEdges == 2);
if (!needTranslucent || this.g3d.setC (colix)) for (var i = 0, j = 0; j < planes.length; ) this.fillFace (p.normixes[i++], this.screens[planes[j++]], this.screens[planes[j++]], this.screens[planes[j++]]);

if (this.g3d.setC (JU.C.getColixTranslucent3 (colix, false, 0))) for (var i = 0, j = 0; j < planes.length; ) this.drawFace (p.normixes[i++], this.screens[planes[j++]], this.screens[planes[j++]], this.screens[planes[j++]]);

return needTranslucent;
}, "J.shapespecial.Polyhedron,~N");
Clazz.defineMethod (c$, "drawFace", 
 function (normix, A, B, C) {
if (this.isAll || this.frontOnly && this.g3d.isDirectedTowardsCamera (normix)) {
this.drawCylinderTriangle (A.x, A.y, A.z, B.x, B.y, B.z, C.x, C.y, C.z);
}}, "~N,JU.P3i,JU.P3i,JU.P3i");
Clazz.defineMethod (c$, "drawCylinderTriangle", 
 function (xA, yA, zA, xB, yB, zB, xC, yC, zC) {
var d = (this.g3d.isAntialiased () ? 6 : 3);
this.g3d.fillCylinderScreen (3, d, xA, yA, zA, xB, yB, zB);
this.g3d.fillCylinderScreen (3, d, xB, yB, zB, xC, yC, zC);
this.g3d.fillCylinderScreen (3, d, xA, yA, zA, xC, yC, zC);
}, "~N,~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "fillFace", 
 function (normix, A, B, C) {
this.g3d.fillTriangleTwoSided (normix, A.x, A.y, A.z, B.x, B.y, B.z, C.x, C.y, C.z);
}, "~N,JU.P3i,JU.P3i,JU.P3i");
});
