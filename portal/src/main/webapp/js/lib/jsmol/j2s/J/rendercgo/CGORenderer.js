Clazz.declarePackage ("J.rendercgo");
Clazz.load (["J.renderspecial.DrawRenderer", "J.util.P3"], "J.rendercgo.CGORenderer", ["J.shapecgo.CGOMesh", "J.util.C", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.cgoMesh = null;
this.cmds = null;
this.pt3 = null;
this.colix0 = 0;
this.colix1 = 0;
this.colix2 = 0;
this.normix0 = 0;
this.normix1 = 0;
this.normix2 = 0;
this.normix = 0;
this.doColor = false;
this.ptNormal = 0;
this.ptColor = 0;
Clazz.instantialize (this, arguments);
}, J.rendercgo, "CGORenderer", J.renderspecial.DrawRenderer);
Clazz.prepareFields (c$, function () {
this.pt3 =  new J.util.P3 ();
});
Clazz.overrideMethod (c$, "render", 
function () {
this.needTranslucent = false;
this.imageFontScaling = this.viewer.getImageFontScaling ();
var cgo = this.shape;
for (var i = cgo.meshCount; --i >= 0; ) this.renderMesh (this.cgoMesh = cgo.meshes[i]);

return this.needTranslucent;
});
Clazz.overrideMethod (c$, "renderMesh", 
function (mesh) {
this.mesh = mesh;
this.cmds = this.cgoMesh.cmds;
if (this.cmds == null || !this.cgoMesh.visible) return false;
if (!this.g3d.setColix (this.cgoMesh.colix)) return this.needTranslucent = true;
var n = this.cmds.size ();
var glMode = -1;
var nPts = 0;
this.ptNormal = 0;
this.ptColor = 0;
this.doColor = !mesh.useColix;
var pt;
var spt;
for (var i = 0; i < n; i++) {
var type = this.cgoMesh.getInt (i);
if (type == 0) break;
var len = J.shapecgo.CGOMesh.getSize (type);
if (len < 0) {
J.util.Logger.error ("CGO unknown type: " + type);
return false;
}switch (type) {
default:
System.out.println ("CGO ? " + type);
break;
case 28:
break;
case 1:
this.getPoint (i + 2, this.pt0, this.pt0i);
this.getPoint (i + 5, this.pt1, this.$pt1i);
this.drawLine (1, 2, false, this.pt0, this.pt1, this.pt0i, this.$pt1i);
len = 8;
break;
case 2:
glMode = this.cgoMesh.getInt (i + 1);
nPts = 0;
break;
case 3:
if (glMode == 2 && nPts >= 3) this.drawLine (1, 2, false, this.pt1, this.pt3, this.$pt1i, this.pt3i);
nPts = 0;
break;
case 10:
break;
case 6:
this.getColix (true);
break;
case 5:
this.normix = this.getNormix ();
break;
case 4:
if (nPts++ == 0) this.getPoint (i, this.pt0, this.pt0i);
switch (glMode) {
case -1:
break;
case 0:
this.drawLine (1, 1, false, this.pt0, this.pt0, this.pt0i, this.pt0i);
break;
case 1:
if (nPts == 2) {
this.getPoint (i, this.pt1, this.$pt1i);
this.drawLine (1, 2, false, this.pt0, this.pt0, this.$pt1i, this.$pt1i);
nPts = 0;
}break;
case 2:
case 3:
if (nPts == 1) {
if (glMode == 2) {
this.vTemp.setT (this.pt0);
this.pt3i.setT (this.pt0i);
}break;
}this.getPoint (i, this.pt1, this.$pt1i);
pt = this.pt0;
this.pt0 = this.pt1;
this.pt1 = pt;
spt = this.pt0i;
this.pt0i = this.$pt1i;
this.$pt1i = spt;
this.drawLine (1, 2, false, this.pt0, this.pt1, this.pt0i, this.$pt1i);
break;
case 4:
switch (nPts) {
case 1:
this.normix1 = this.normix2 = this.normix0 = this.normix;
this.colix1 = this.colix2 = this.colix0 = this.colix;
break;
case 2:
this.getPoint (i, this.pt1, this.$pt1i);
break;
case 3:
this.getPoint (i, this.pt2, this.pt2i);
this.fillTriangle ();
nPts = 0;
break;
}
break;
case 5:
switch (nPts) {
case 1:
this.normix1 = this.normix2 = this.normix0 = this.normix;
this.colix1 = this.colix2 = this.colix0 = this.colix;
break;
case 2:
this.getPoint (i, this.pt2, this.pt2i);
break;
default:
if (nPts % 2 == 0) {
pt = this.pt0;
this.pt0 = this.pt2;
spt = this.pt0i;
this.pt0i = this.pt2i;
} else {
pt = this.pt1;
this.pt1 = this.pt2;
spt = this.$pt1i;
this.$pt1i = this.pt2i;
}this.pt2 = pt;
this.pt2i = spt;
this.getPoint (i, this.pt2, this.pt2i);
this.fillTriangle ();
break;
}
break;
case 6:
switch (nPts) {
case 1:
this.normix1 = this.normix2 = this.normix0 = this.normix;
this.colix1 = this.colix2 = this.colix0 = this.colix;
this.pt1.setT (this.pt0);
this.$pt1i.setT (this.pt0i);
break;
case 2:
this.getPoint (i, this.pt0, this.pt0i);
break;
default:
this.pt2.setT (this.pt0);
this.pt2i.setT (this.pt0i);
this.getPoint (i, this.pt0, this.pt0i);
this.fillTriangle ();
break;
}
break;
}
break;
case 14:
this.getPoint (i, this.pt0, this.pt0i);
this.getPoint (i + 3, this.pt1, this.$pt1i);
this.width = this.cgoMesh.getFloat (i + 7);
this.getColix (true);
this.getColix (false);
this.drawLine (1, 2, false, this.pt0, this.pt1, this.pt0i, this.$pt1i);
this.width = 0;
break;
case 8:
this.getPoint (i, this.pt0, this.pt0i);
this.getPoint (i + 3, this.pt1, this.$pt1i);
this.getPoint (i + 6, this.pt2, this.pt2i);
this.normix0 = this.getNormix ();
this.normix1 = this.getNormix ();
this.normix2 = this.getNormix ();
this.colix0 = this.getColix (false);
this.colix1 = this.getColix (false);
this.colix2 = this.getColix (false);
this.fillTriangle ();
break;
}
i += len;
}
return true;
}, "J.shape.Mesh");
$_M(c$, "getNormix", 
($fz = function () {
return this.cgoMesh.nList.get (this.ptNormal++).shortValue ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "getColix", 
($fz = function (doSet) {
if (this.doColor) {
this.colix = J.util.C.copyColixTranslucency (this.cgoMesh.colix, this.cgoMesh.cList.get (this.ptColor++).shortValue ());
if (doSet) this.g3d.setColix (this.colix);
}return this.colix;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getPoint", 
($fz = function (i, pt, pti) {
this.cgoMesh.getPoint (i, pt);
this.viewer.transformPtScr (pt, pti);
}, $fz.isPrivate = true, $fz), "~N,J.util.P3,J.util.P3i");
$_M(c$, "fillTriangle", 
($fz = function () {
this.g3d.fillTriangle3CN (this.pt0i, this.colix0, this.normix0, this.$pt1i, this.colix1, this.normix1, this.pt2i, this.colix2, this.normix2);
}, $fz.isPrivate = true, $fz));
});
