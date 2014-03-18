Clazz.declarePackage ("J.shapespecial");
Clazz.load (["J.shape.Mesh", "J.shapespecial.Draw", "J.util.BS", "$.V3"], "J.shapespecial.DrawMesh", ["J.util.ArrayUtil", "$.BSUtil", "$.P3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.modelFlags = null;
this.drawType = null;
this.drawTypes = null;
this.ptCenters = null;
this.axis = null;
this.axes = null;
this.drawVertexCount = 0;
this.drawVertexCounts = null;
this.isFixed = false;
this.isVector = false;
this.drawArrowScale = 0;
this.noHead = false;
this.isBarb = false;
this.scale = 1;
this.isScaleSet = false;
this.bsMeshesVisible = null;
Clazz.instantialize (this, arguments);
}, J.shapespecial, "DrawMesh", J.shape.Mesh);
Clazz.prepareFields (c$, function () {
this.drawType = J.shapespecial.Draw.EnumDrawType.NONE;
this.axis = J.util.V3.new3 (1, 0, 0);
this.bsMeshesVisible =  new J.util.BS ();
});
Clazz.makeConstructor (c$, 
function (thisID, colix, index) {
Clazz.superConstructor (this, J.shapespecial.DrawMesh, []);
this.mesh1 (thisID, colix, index);
}, "~S,~N,~N");
Clazz.overrideMethod (c$, "clear", 
function (meshType) {
this.clearMesh (meshType);
this.scale = 1;
this.isScaleSet = false;
}, "~S");
$_M(c$, "setCenters", 
function () {
if (this.ptCenters == null) this.setCenter (-1);
 else for (var i = this.ptCenters.length; --i >= 0; ) this.setCenter (i);

});
$_M(c$, "setCenter", 
function (iModel) {
var center = J.util.P3.new3 (0, 0, 0);
var iptlast = -1;
var ipt = 0;
var n = 0;
for (var i = this.polygonCount; --i >= 0; ) {
if (iModel >= 0 && i != iModel || this.polygonIndexes[i] == null) continue;
iptlast = -1;
for (var iV = (this.drawType === J.shapespecial.Draw.EnumDrawType.POLYGON) ? 3 : this.polygonIndexes[i].length; --iV >= 0; ) {
ipt = this.polygonIndexes[i][iV];
if (ipt == iptlast) continue;
iptlast = ipt;
center.add (this.vertices[ipt]);
n++;
}
if (n > 0 && (i == iModel || i == 0)) {
center.scale (1.0 / n);
if (this.mat4 != null) this.mat4.transform (center);
break;
}}
if (iModel < 0) {
this.ptCenter.setT (center);
} else {
this.ptCenters[iModel] = center;
}}, "~N");
$_M(c$, "offset", 
function (offset) {
this.rotateTranslate (null, offset, false);
this.setCenters ();
}, "J.util.V3");
$_M(c$, "deleteAtoms", 
function (modelIndex) {
if (modelIndex >= this.polygonCount) return;
this.polygonCount--;
this.polygonIndexes = J.util.ArrayUtil.deleteElements (this.polygonIndexes, modelIndex, 1);
this.drawTypes = J.util.ArrayUtil.deleteElements (this.drawTypes, modelIndex, 1);
this.drawVertexCounts = J.util.ArrayUtil.deleteElements (this.drawVertexCounts, modelIndex, 1);
this.ptCenters = J.util.ArrayUtil.deleteElements (this.ptCenters, modelIndex, 1);
this.axes = J.util.ArrayUtil.deleteElements (this.axes, modelIndex, 1);
var bs = J.util.BSUtil.newAndSetBit (modelIndex);
J.util.BSUtil.deleteBits (this.modelFlags, bs);
}, "~N");
$_M(c$, "isRenderScalable", 
function () {
switch (this.drawType) {
case J.shapespecial.Draw.EnumDrawType.ARROW:
return (this.connections != null);
case J.shapespecial.Draw.EnumDrawType.ARC:
case J.shapespecial.Draw.EnumDrawType.CIRCLE:
case J.shapespecial.Draw.EnumDrawType.CIRCULARPLANE:
return true;
default:
return this.haveXyPoints;
}
});
});
